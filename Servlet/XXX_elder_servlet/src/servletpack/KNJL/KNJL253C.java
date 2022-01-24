// kanji=漢字
/*
 * $Id: fd42575e1cb0ba3e585c8ce4b5738eb032caa3a2 $
 *
 * 作成日: 2011/10/07 15:05:15 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: fd42575e1cb0ba3e585c8ce4b5738eb032caa3a2 $
 */
public class KNJL253C {

    private static final Log log = LogFactory.getLog("KNJL253C.class");

    private boolean _hasData;
    private final String SUBCLASS_2KA = "22";
    private final String SUBCLASS_ALL = "99";
    private final String SUBCLASS_3KA = "33";
    private final String FRM_NAME = "KNJL253C.frm";
    private final String FRM_NAME_NARACL = "KNJL253C_NARACL.frm";
    private final String FRM_NAME_GOJO = "KNJL253C_GOJO.frm";

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
            init(response, svf);

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
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        if (_param.isCollege()) {
            svf.VrSetForm(FRM_NAME_NARACL, 1);
        } else if (_param.isGojo()) {
            svf.VrSetForm(FRM_NAME_GOJO, 1);
        } else {
            svf.VrSetForm(FRM_NAME, 1);
        }
        final List subclassMap = getPrintMap(db2);
        final List list = getExamineeList(db2);
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final PretestExaminee e = (PretestExaminee) it.next();
            //ヘッダ
            svf.VrsOut("DAY", _param.getDate(db2));
            svf.VrsOut("NENDO", _param.getYear(db2));
            svf.VrsOut("APPLICANTDIV", _param._applicantdivname);
            svf.VrsOut("PRE_TESTDIV", _param._preTestdivName);
            svf.VrsOut("ZIPCD", e._zipcd);
            svf.VrsOut("ADDR1" + (getMS932ByteCount(e._address1) > 40 ? "_2" : ""), e._address1);
            svf.VrsOut("ADDR2" + (getMS932ByteCount(e._address2) > 40 ? "_2" : ""), e._address2);
            svf.VrsOut("ADDRESSEE" + (getMS932ByteCount(e._gname) > 24 ? "2" : ""), e._gname);
            svf.VrsOut("HR_ATTNO_NAME", e._name);
            svf.VrsOut("SCHOOLNAME", _param._schoolName);
            svf.VrsOut("EXAMNO", e._preReceptno);
            svf.VrsOut("NAME", e._name);
            //科目
            for (final Iterator itSub = subclassMap.iterator(); itSub.hasNext();) {
                final SubclassData subclassData = (SubclassData) itSub.next();
                final String fName = subclassData.getSetField();
                final ScoreRank scoreRank = (ScoreRank) subclassData._scoreRankMap.get(e._preReceptno);
                //理科と３科は、２型の受験者は出力しない
                if (_param.isCollege() && ("3".equals(subclassData._subclassCd) || SUBCLASS_ALL.equals(subclassData._subclassCd)) && "2".equals(e._preExamType)) {
                    continue;
                }
                //英語と４教科(国算理英)と国算英は、１型の受験者は出力しない
                if (_param.isGojo() && ("4".equals(subclassData._subclassCd) || SUBCLASS_ALL.equals(subclassData._subclassCd) || SUBCLASS_3KA.equals(subclassData._subclassCd)) && "1".equals(e._preExamType)) {
                    continue;
                }
                //上段
                svf.VrsOut(fName, subclassData._subclassName);
                svf.VrsOutn(fName + "_TOTAL", 1, subclassData._juken);
                if (null != scoreRank) {
                    svf.VrsOutn(fName + "_TOTAL", 2, scoreRank._score);
                    svf.VrsOutn(fName + "_TOTAL", 3, scoreRank._rank);
                }
                svf.VrsOut(fName + "_AVE", subclassData._avg);
                //下段
                int gyo = 1;
                for (final Iterator itPrint = subclassData._bunpuList.iterator(); itPrint.hasNext();) {
                    final PrintData printData = (PrintData) itPrint.next();
                    if (_param.isCollege() && "3".equals(subclassData._subclassCd) && gyo <= 5) {
                        // 表示無し
                    } else if (_param.isGojo() && ("3".equals(subclassData._subclassCd) || "4".equals(subclassData._subclassCd)) && gyo <= 4) {
                        // 表示無し
                    } else if (_param.isGojo() && (SUBCLASS_2KA.equals(subclassData._subclassCd) || SUBCLASS_3KA.equals(subclassData._subclassCd)) && gyo <= 1) {
                        // 表示無し
                    } else {
                        svf.VrsOutn(fName + "_NUM", gyo, printData._ninzu);
                        svf.VrsOutn(fName + "_SUM", gyo, printData._ruikei);
                    }
                    gyo++;
                }
            }
            _hasData = true;
            svf.VrEndPage();
        }
    }

    private int getMS932ByteCount(final String str) {
        if (null == str) return 0;
        int ret = 0;
        try {
            ret = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return ret;
    }

    private List getPrintMap(final DB2UDB db2) throws SQLException {
        final List retMap = new ArrayList();
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH KOTEI(SORT, NAMECD2, NAME1) AS ( ");
        stb.append("     VALUES('1', " + SUBCLASS_2KA + ", '国算') ");
        //プレテスト区分「2:表現力入試」の時、３教科合計は表示しない
        if (_param.isCollege() && "2".equals(_param._preTestdiv)) {
        } else {
            stb.append("     UNION ");
            stb.append("     VALUES('2', " + SUBCLASS_ALL + ", '全て') ");
        }
        if (_param.isGojo()) {
            stb.append("     UNION ");
            stb.append("     VALUES('3', " + SUBCLASS_3KA + ", '国算英') ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     '0' AS SORT, ");
        stb.append("     CAST(NAMECD2 AS SMALLINT) AS NAMECD2, ");
        stb.append("     NAME1 ");
        stb.append(" FROM ");
        stb.append("     V_NAME_MST ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND NAMECD1 = 'L109' ");
        stb.append("     AND ABBV3   = '" + _param._preTestdiv + "' ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     KOTEI ");
        stb.append(" ORDER BY ");
        stb.append("     SORT, ");
        stb.append("     NAMECD2 ");

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            int subcnt = 1;
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subclassCd = rs.getString("NAMECD2");
                final String subclassName = rs.getString("NAME1");
                final String subclassNo = String.valueOf(subcnt);
                int kizami = 10;
                int maxScore = (_param.isCollege()) ? 150 : 100;
                if (SUBCLASS_2KA.equals(subclassCd) || SUBCLASS_ALL.equals(subclassCd) || SUBCLASS_3KA.equals(subclassCd)) {
                    if (_param.isCollege()) {
                        kizami = 20;
                        maxScore = 400;
                    } else {
                        kizami = 25;
                        maxScore = 300;
                    }
                }
                final SubclassData subclassData = new SubclassData(db2, _param, subclassCd, subclassName, subclassNo, kizami, maxScore);
                retMap.add(subclassData);
                subcnt++;
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private class SubclassData {
        private final String _subclassCd;
        private final String _subclassName;
        private final String _subclassNo;
        private final List _bunpuList;

        private String _juken;
        private String _avg;
        private Map _scoreRankMap;

        public SubclassData(
                final DB2UDB db2,
                final Param param,
                final String subclassCd,
                final String subclassName,
                final String subclassNo,
                final int kizami,
                final int maxScore
        ) throws SQLException {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _subclassNo = subclassNo;
            _bunpuList = new ArrayList();
            int jougen = maxScore;
            int kagen = maxScore - kizami;
            if (param.isCollege() && !(SUBCLASS_2KA.equals(subclassCd) || SUBCLASS_ALL.equals(subclassCd))) {
                kagen += 1;
            }
            int ruikei = 0;
            int total = 0;
            for (int i = 0; i < maxScore; i += kizami) {
                log.info(" subclass " + subclassCd + " (" + kagen + " ~ " + jougen + ")");
                final String cntSql = getCntSql(subclassCd, kagen, jougen);
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = db2.prepareStatement(cntSql);
                    rs = ps.executeQuery();
                    String ninzu = "0";
                    while (rs.next()) {
                        ruikei = ruikei + rs.getInt("CNT");
                        ninzu = rs.getString("CNT");
                        total = total + rs.getInt("TOTAL");
                    }
                    String sruikei = String.valueOf(ruikei);
                    if (param.isGojo()) {
                        if (!(SUBCLASS_2KA.equals(subclassCd) || SUBCLASS_ALL.equals(subclassCd) || SUBCLASS_3KA.equals(subclassCd))) {
                            final String perfect = (String) _param._perfectMap.get(subclassCd);
                            if (NumberUtils.isDigits(perfect) && kagen > Integer.parseInt(perfect)) {
                                ninzu = "";
                                sruikei = "";
                            }
                        }
                    }
                    final PrintData printData = new PrintData(ninzu, sruikei, String.valueOf(kagen), String.valueOf(jougen));
                    _bunpuList.add(printData);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
                if (param.isCollege()) {
                    if (!(SUBCLASS_2KA.equals(subclassCd) || SUBCLASS_ALL.equals(subclassCd))) {
                        jougen = jougen - kizami;
                        kagen = kagen - kizami;
                        if (kagen == 1) {
                            kagen = 0;
                        }
                    } else {
                        if (jougen == maxScore) {
                            jougen = jougen - kizami - 1;
                        } else {
                            jougen = jougen - kizami;
                        }
                        if (jougen == 59) {
                            kagen = 0;
                        } else {
                            kagen = kagen - kizami;
                        }
                    }
                } else {
                    if (jougen == maxScore) {
                        jougen = jougen - kizami - 1;
                    } else {
                        jougen = jougen - kizami;
                    }
                    kagen = kagen - kizami;
                }
            }
            //受験者数・平均点
            setJukenAvg(db2, subclassCd, 0, maxScore);
            //得点・順位
            setScoreRankMap(db2, subclassCd);
        }

        private String getCntSql(final String subclassCd, final int kagen, final int jougen) {
            final StringBuffer stb = new StringBuffer();
            if (SUBCLASS_2KA.equals(subclassCd) || SUBCLASS_ALL.equals(subclassCd) || SUBCLASS_3KA.equals(subclassCd)) {
                stb.append(" SELECT ");
                stb.append("     COUNT(*) AS CNT, ");
                if (SUBCLASS_2KA.equals(subclassCd)) {
                    stb.append("     SUM(T1.TOTAL2) AS TOTAL, ");
                    stb.append("     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL2)),1),5,1) AS AVG ");
                } else if (SUBCLASS_3KA.equals(subclassCd)) {
                    stb.append("     SUM(T1.TOTAL3) AS TOTAL, ");
                    stb.append("     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL3)),1),5,1) AS AVG ");
                } else {
                    stb.append("     SUM(T1.TOTAL4) AS TOTAL, ");
                    stb.append("     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL4)),1),5,1) AS AVG ");
                }
                stb.append(" FROM ");
                stb.append("     ENTEXAM_RECEPT_PRE_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
                if (SUBCLASS_2KA.equals(subclassCd)) {
                    stb.append("     AND T1.TOTAL2 BETWEEN " + kagen + " AND " + jougen + " ");
                } else if (SUBCLASS_3KA.equals(subclassCd)) {
                    stb.append("     AND T1.TOTAL3 BETWEEN " + kagen + " AND " + jougen + " ");
                } else {
                    stb.append("     AND T1.TOTAL4 BETWEEN " + kagen + " AND " + jougen + " ");
                }
                stb.append("     AND T1.PRE_RECEPTNO IN (SELECT ");
                stb.append("                              I1.PRE_RECEPTNO ");
                stb.append("                          FROM ");
                stb.append("                              ENTEXAM_APPLICANTBASE_PRE_DAT I1 ");
                stb.append("                          WHERE ");
                stb.append("                              I1.ENTEXAMYEAR = '" + _param._year + "' ");
                stb.append("                              AND I1.APPLICANTDIV = '1' ");
                stb.append("                              AND I1.PRE_TESTDIV = '" + _param._preTestdiv + "' ");
                if (SUBCLASS_ALL.equals(subclassCd) || SUBCLASS_3KA.equals(subclassCd)) {
                    if (_param.isGojo()) {
                        stb.append("                              AND I1.PRE_EXAM_TYPE = '2' ");
                    } else {
                        stb.append("                              AND I1.PRE_EXAM_TYPE = '1' ");
                    }
                }
                stb.append("                          ) ");
            } else {
                stb.append(" SELECT ");
                stb.append("     COUNT(*) AS CNT, ");
                stb.append("     SUM(T1.SCORE) AS TOTAL, ");
                stb.append("     DECIMAL(ROUND(AVG(FLOAT(T1.SCORE)),1),5,1) AS AVG ");
                stb.append(" FROM ");
                stb.append("     ENTEXAM_SCORE_PRE_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
                stb.append("     AND T1.TESTSUBCLASSCD = '" + subclassCd + "' ");
                stb.append("     AND T1.SCORE BETWEEN " + kagen + " AND " + jougen + " ");
                stb.append("     AND T1.PRE_RECEPTNO IN (SELECT ");
                stb.append("                              I1.PRE_RECEPTNO ");
                stb.append("                          FROM ");
                stb.append("                              ENTEXAM_APPLICANTBASE_PRE_DAT I1 ");
                stb.append("                          WHERE ");
                stb.append("                              I1.ENTEXAMYEAR = '" + _param._year + "' ");
                stb.append("                              AND I1.APPLICANTDIV = '1' ");
                stb.append("                              AND I1.PRE_TESTDIV = '" + _param._preTestdiv + "') ");
            }
            return stb.toString();
        }

        private void setJukenAvg(final DB2UDB db2, final String subclassCd, final int kagen, final int maxScore) {
            _juken = null;
            _avg = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getCntSql(subclassCd, kagen, maxScore);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _juken = rs.getString("CNT");
                    _avg = rs.getString("AVG");
                }

            } catch (Exception ex) {
                log.error("setJukenAvg error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setScoreRankMap(final DB2UDB db2, final String subclassCd) {
            _scoreRankMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getScoreRankSql(subclassCd);
                //log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String receptno = rs.getString("PRE_RECEPTNO");
                    final String score = rs.getString("SCORE");
                    final String rank = rs.getString("RANK");

                    final ScoreRank scoreRank = new ScoreRank(receptno, score, rank);
                    _scoreRankMap.put(receptno, scoreRank);
                }

            } catch (Exception ex) {
                log.error("setScoreRankMap error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getScoreRankSql(final String subclassCd) {
            final StringBuffer stb = new StringBuffer();
            if (SUBCLASS_2KA.equals(subclassCd) || SUBCLASS_ALL.equals(subclassCd) || SUBCLASS_3KA.equals(subclassCd)) {
                stb.append(" SELECT ");
                stb.append("     T1.PRE_RECEPTNO, ");
                if (SUBCLASS_2KA.equals(subclassCd)) {
                    stb.append("     T1.TOTAL2 AS SCORE, ");
                    stb.append("     T1.TOTAL_RANK2 AS RANK ");
                } else if (SUBCLASS_3KA.equals(subclassCd)) {
                    stb.append("     T1.TOTAL3 AS SCORE, ");
                    stb.append("     T1.DIV_RANK3 AS RANK ");
                } else {
                    stb.append("     T1.TOTAL4 AS SCORE, ");
                    stb.append("     T1.DIV_RANK4 AS RANK ");
                }
                stb.append(" FROM ");
                stb.append("     ENTEXAM_RECEPT_PRE_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
                stb.append("     AND T1.PRE_RECEPTNO IN (SELECT ");
                stb.append("                              I1.PRE_RECEPTNO ");
                stb.append("                          FROM ");
                stb.append("                              ENTEXAM_APPLICANTBASE_PRE_DAT I1 ");
                stb.append("                          WHERE ");
                stb.append("                              I1.ENTEXAMYEAR = '" + _param._year + "' ");
                stb.append("                              AND I1.APPLICANTDIV = '1' ");
                stb.append("                              AND I1.PRE_TESTDIV = '" + _param._preTestdiv + "' ");
                if (SUBCLASS_ALL.equals(subclassCd) || SUBCLASS_3KA.equals(subclassCd)) {
                    if (_param.isGojo()) {
                        stb.append("                              AND I1.PRE_EXAM_TYPE = '2' ");
                    } else {
                        stb.append("                              AND I1.PRE_EXAM_TYPE = '1' ");
                    }
                }
                stb.append("                          ) ");
            } else {
                stb.append(" SELECT ");
                stb.append("     T1.PRE_RECEPTNO, ");
                stb.append("     T1.SCORE, ");
                stb.append("     T1.RANK ");
                stb.append(" FROM ");
                stb.append("     ENTEXAM_SCORE_PRE_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
                stb.append("     AND T1.TESTSUBCLASSCD = '" + subclassCd + "' ");
                stb.append("     AND T1.PRE_RECEPTNO IN (SELECT ");
                stb.append("                              I1.PRE_RECEPTNO ");
                stb.append("                          FROM ");
                stb.append("                              ENTEXAM_APPLICANTBASE_PRE_DAT I1 ");
                stb.append("                          WHERE ");
                stb.append("                              I1.ENTEXAMYEAR = '" + _param._year + "' ");
                stb.append("                              AND I1.APPLICANTDIV = '1' ");
                stb.append("                              AND I1.PRE_TESTDIV = '" + _param._preTestdiv + "') ");
            }
            return stb.toString();
        }

        private String getSetField() {
            if (SUBCLASS_2KA.equals(_subclassCd)) {
                return  "TWO";
            } else if (SUBCLASS_ALL.equals(_subclassCd)) {
                return  "THREE";
            } else if (SUBCLASS_3KA.equals(_subclassCd)) {
                return  "FOUR";
            } else {
                return  "CLASS" + _subclassNo;
            }
        }

        public String toString() {
            return _subclassCd + " : " + _subclassName;
        }
    }

    private class PrintData {
        final String _ninzu;
        final String _ruikei;
        final String _kagen;
        final String _jougen;

        public PrintData(final String ninzu, final String ruikei, final String kagen, final String jougen) {
            _ninzu = ninzu;
            _ruikei = ruikei;
            _kagen = kagen;
            _jougen = jougen;
        }

        public String toString() {
            return _kagen + " - " + _jougen + " : 人数 = " + _ninzu + " 累計 = " + _ruikei;
        }
    }

    private class ScoreRank {
        final String _receptno;
        final String _score;
        final String _rank;

        public ScoreRank(final String receptno, final String score, final String rank) {
            _receptno = receptno;
            _score = score;
            _rank = rank;
        }

        public String toString() {
            return _receptno + " : 得点 = " + _score + " 順位 = " + _rank;
        }
    }

    private List getExamineeList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            final String sql = sqlEntexamApplicantBasePreDat();
            //log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String preReceptno = rs.getString("PRE_RECEPTNO");
                final String preExamType = rs.getString("PRE_EXAM_TYPE");
                final String name = rs.getString("NAME");
                final String gname = rs.getString("GNAME");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");

                final PretestExaminee examinee = new PretestExaminee(preReceptno, preExamType, name, gname, zipcd, address1, address2);
                list.add(examinee);
            }

        } catch (Exception ex) {
            log.error("setSvfMain set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String sqlEntexamApplicantBasePreDat() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.PRE_RECEPTNO, ");
        stb.append("     T1.PRE_EXAM_TYPE, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.GNAME, ");
        stb.append("     T1.ZIPCD, ");
        stb.append("     T1.ADDRESS1, ");
        stb.append("     T1.ADDRESS2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_PRE_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.PRE_TESTDIV = '" + _param._preTestdiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.PRE_RECEPTNO ");
        return stb.toString();
    }

    private class PretestExaminee {
        final String _preReceptno;
        final String _preExamType;
        final String _name;
        final String _gname;
        final String _zipcd;
        final String _address1;
        final String _address2;

        PretestExaminee(
                final String preReceptno,
                final String preExamType,
                final String name,
                final String gname,
                final String zipcd,
                final String address1,
                final String address2
        ) {
            _preReceptno = preReceptno;
            _preExamType = preExamType;
            _name = name;
            _gname = gname;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70109 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _year;
        private final String _applicantdiv;
        private final String _applicantdivname;
        private final String _preTestdiv;
        private final String _preTestdivName;
        private final String _ctrlDate;
        private final boolean _seirekiFlg;
        private final String _schoolName;
        private final String _z010SchoolCode;
        private final Map _perfectMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _applicantdiv = "1";
            _applicantdivname = getNameMst(db2, "L103", _applicantdiv);
            _preTestdiv = request.getParameter("PRE_TESTDIV");
            _preTestdivName = getNameMst(db2, "L104", _preTestdiv);
            _ctrlDate = request.getParameter("CTRL_DATE");
            _seirekiFlg = getSeirekiFlg(db2);
            _schoolName = getSchoolName(db2);
            _z010SchoolCode = getSchoolCode(db2);
            _perfectMap = getPerfectMap(db2);
        }
        /**
         * 日付表示の和暦(年号)/西暦使用フラグ
         * @param db2
         * @return
         */
        private boolean getSeirekiFlg(DB2UDB db2) {
            boolean seirekiFlg = false;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if ( rs.next() ) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            }
            return seirekiFlg;
        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {

            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME1 ");
            sql.append(" FROM NAME_MST ");
            sql.append(" WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
            String name = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try{
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                   name = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

        public String getYear(final DB2UDB db2) {
            return _seirekiFlg ? _year + "年度": KNJ_EditDate.h_format_JP_N(db2, _year + "-01-01") + "度";
        }

        /**
         * 日付の名称を得る
         * @return 日付の名称
         */
        public String getDate(final DB2UDB db2) {
            return _seirekiFlg ?
                    _ctrlDate.substring(0,4) + "年" + KNJ_EditDate.h_format_JP_MD(_ctrlDate) : KNJ_EditDate.h_format_JP(db2, _ctrlDate);
        }

        /*
         * 年度と入試制度から学校名を返す
         */
        private String getSchoolName(DB2UDB db2) {
            String name = null;

            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME ");
            sql.append(" FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '"+_year+"' AND CERTIF_KINDCD = '105' ");
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                   name = rs.getString("SCHOOL_NAME");
                }
            } catch (SQLException ex) {
                log.error(ex);
            }

            return name;
        }

        private String getSchoolCode(DB2UDB db2) {
            String schoolCode = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   CASE WHEN NAME1 = 'CHIBEN' THEN NAME2 ELSE NULL END AS SCHOOLCODE ");
                stb.append(" FROM ");
                stb.append("   NAME_MST T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.NAMECD1 = 'Z010' ");
                stb.append("   AND T1.NAMECD2 = '00' ");

                PreparedStatement ps = db2.prepareStatement(stb.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    schoolCode = rs.getString("SCHOOLCODE");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSchoolCode Exception", e);
            }
            return schoolCode;
        }

        private Map getPerfectMap(DB2UDB db2) {
            final Map rtn = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT TESTSUBCLASSCD, PERFECT FROM ENTEXAM_PERFECT_PRE_MST ");
                stb.append(" WHERE ENTEXAMYEAR = '" + _year + "' ");
                stb.append("     AND APPLICANTDIV = '1' ");
                if (isGojo()) {
                    stb.append("     AND PRE_EXAM_TYPE = '2' ");
                } else if ("2".equals(_preTestdiv)) {
                    stb.append("     AND PRE_EXAM_TYPE = '3' ");
                } else {
                    stb.append("     AND PRE_EXAM_TYPE = '1' ");
                }

                PreparedStatement ps = db2.prepareStatement(stb.toString());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("TESTSUBCLASSCD"), rs.getString("PERFECT"));
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSchoolCode Exception", e);
            }
            return rtn;
        }

        boolean isGojo() {
            return "30290053001".equals(_z010SchoolCode);
        }

        boolean isCollege() {
            return "30290086001".equals(_z010SchoolCode);
        }
    }
}

// eof

/*
 * $Id: 511c0f3655ab3fe57e4a6c7f523c05a833782d96 $
 *
 * 作成日: 2017/10/24
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL348Q {

    private static final Log log = LogFactory.getLog(KNJL348Q.class);

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
        final String form = "P".equals(_param._schoolKind) ? "KNJL348Q_2.frm": "KNJL348Q.frm";
        svf.VrSetForm(form, 4);
        final List printList = getList(db2);
        final int maxLine = 30;
        final int pageSu = printList.size() / maxLine;
        final int pageAmari = (printList.size() % maxLine) > 0 ? 1 : 0;
        final int allPage = pageSu + pageAmari;
        int ninzu = 1;
        svf.VrsOut("PAGE2", String.valueOf(allPage));
        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + _param._testdivAbbv1 + "入学試験　結果(" + _param._sortName + ")");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));

        //科目名、平均
        int kamokuCnt = 1;
        for (Iterator itTestKamou = _param._testKamokuList.iterator(); itTestKamou.hasNext();) {
            final TestKamoku testKamoku = (TestKamoku) itTestKamou.next();
            final String cField = KNJ_EditEdit.getMS932ByteLength(testKamoku._name) > 6 ? "_2": "";
            svf.VrsOut("CLASS_NAME" + kamokuCnt + cField, testKamoku._name);
            //今年度平均
            final String strKamoku = String.valueOf(kamokuCnt);
            setAvgData(svf, strKamoku, "AVERAGE" + kamokuCnt, _param._avgYearMap);
            //去年平均
            setAvgData(svf, strKamoku, "LAST_AVERAGE" + kamokuCnt, _param._avgLastYearMap);
            kamokuCnt++;
        }
        svf.VrsOut("CLASS_NAME5", "計");
        setAvgData(svf, "B", "AVERAGE5", _param._avgYearMap);
        setAvgData(svf, "B", "LAST_AVERAGE5", _param._avgLastYearMap);

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();

            int page1Su = ninzu / maxLine;
            int page1Amari = (ninzu % maxLine) > 0 ? 1 : 0;
            int page1 = page1Su + page1Amari;
            svf.VrsOut("PAGE1", String.valueOf(page1));

            svf.VrsOut("NUM", String.valueOf(ninzu));
            svf.VrsOut("SEX", printData._sexName);
            svf.VrsOut("SEX_RANK", printData._divRank4);
            svf.VrsOut("EXAM_NO", printData._examno);
            svf.VrsOut("NAME", printData._name);
            svf.VrsOut("SEX_CD", printData._sex);
            svf.VrsOut("KANA", printData._nameKana);
            kamokuCnt = 1;
            for (Iterator itScore = printData._scoreList.iterator(); itScore.hasNext();) {
                final String score = (String) itScore.next();
                svf.VrsOut("SCORE" + kamokuCnt, score);
                kamokuCnt++;
            }
            svf.VrsOut("SCORE5", printData._total4);
            svf.VrsOut("SCORE6", printData._interviewValue);
            svf.VrsOut("SCORE7", printData._interviewValue2);
            svf.VrsOut("JHSCHOOL_CD", printData._fsCd);
            svf.VrsOut("JHSCHOOL_NAME", printData._finschoolName);
            String simaiInfo = null;
            if (null != printData._simaiSchoolKind1 && null != printData._simaiSchoolKind2) {simaiInfo = printData._simaiSchoolKind1 + "," + printData._simaiSchoolKind2;}
            else if (null != printData._simaiSchoolKind1) {simaiInfo = printData._simaiSchoolKind1;}
            else if (null != printData._simaiSchoolKind2) {simaiInfo = printData._simaiSchoolKind2;}
            svf.VrsOut("BROSYS", simaiInfo);

            ninzu++;
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private void setAvgData(final Vrw32alp svf, final String strKamoku, final String setField, final Map avgMap) {
        if (avgMap.containsKey(strKamoku)) {
            final AvgData avgData = (AvgData) avgMap.get(strKamoku);
            if (null == avgData._avg) {
                svf.VrsOut(setField, "");
            } else {
                final BigDecimal setVal = new BigDecimal(avgData._avg).setScale(2, BigDecimal.ROUND_HALF_UP);
                svf.VrsOut(setField, setVal.toString());
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String sexName = rs.getString("SEX_NAME");
                final String testdiv = rs.getString("TESTDIV");
                final String fsCd = rs.getString("FS_CD");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final List scoreList = new ArrayList();
                int kamokuCnt = 1;
                for (int i = 0; i < _param._testKamokuList.size(); i++) {
                    scoreList.add(rs.getString("SCORE" + kamokuCnt));
                    kamokuCnt++;
                }
                final String divRank4 = rs.getString("DIV_RANK4");
                final String total2 = rs.getString("TOTAL2");
                final String total4 = rs.getString("TOTAL4");
                final String interviewValue = rs.getString("INTERVIEW_VALUE");
                final String interviewValue2 = rs.getString("INTERVIEW_VALUE2");
                final String simaiSchoolKind1 = rs.getString("SIMAI_SCHOOL_KIND1");
                final String simaiSchoolKind2 = rs.getString("SIMAI_SCHOOL_KIND2");

                final PrintData printData = new PrintData(examno, name, nameKana, sex, sexName, testdiv, fsCd, finschoolName, scoreList, divRank4, total2, total4, interviewValue, interviewValue2, simaiSchoolKind1, simaiSchoolKind2);
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

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     B1.EXAMNO, ");
        stb.append("     B1.NAME, ");
        stb.append("     B1.NAME_KANA, ");
        stb.append("     B1.SEX, ");
        stb.append("     Z002.NAME2 AS SEX_NAME, ");
        stb.append("     B1.TESTDIV, ");
        stb.append("     B1.FS_CD, ");
        stb.append("     F1.FINSCHOOL_NAME, ");
        int kamokuCnt = 1;
        for (int i = 0; i < _param._testKamokuList.size(); i++) {
            stb.append("     S" + kamokuCnt + ".SCORE AS SCORE" + kamokuCnt + ", ");
            kamokuCnt++;
        }
        stb.append("     R1.DIV_RANK4, ");
        stb.append("     R1.TOTAL2, ");
        stb.append("     R1.TOTAL4, ");
        stb.append("     N1.NAME2 AS INTERVIEW_VALUE, ");
        stb.append("     N2.NAME2 AS INTERVIEW_VALUE2, ");
        stb.append("     CASE WHEN B1.SIMAI_SCHOOL_KIND1 = 'P' THEN '1' ");
        stb.append("          WHEN B1.SIMAI_SCHOOL_KIND1 = 'J' THEN '2' ");
        stb.append("          WHEN B1.SIMAI_SCHOOL_KIND1 = 'H' THEN '3' ");
        stb.append("     END AS SIMAI_SCHOOL_KIND1, ");
        stb.append("     CASE WHEN B1.SIMAI_SCHOOL_KIND2 = 'P' THEN '1' ");
        stb.append("          WHEN B1.SIMAI_SCHOOL_KIND2 = 'J' THEN '2' ");
        stb.append("          WHEN B1.SIMAI_SCHOOL_KIND2 = 'H' THEN '3' ");
        stb.append("     END AS SIMAI_SCHOOL_KIND2 ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = B1.SEX ");
        stb.append("     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT R1 ON R1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND R1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND R1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND R1.EXAMNO = B1.EXAMNO ");
        kamokuCnt = 1;
        for (Iterator itKamoku = _param._testKamokuList.iterator(); itKamoku.hasNext();) {
            final TestKamoku kamoku = (TestKamoku) itKamoku.next();
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT S" + kamokuCnt + " ON S" + kamokuCnt + ".ENTEXAMYEAR = R1.ENTEXAMYEAR ");
            stb.append("             AND S" + kamokuCnt + ".APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("             AND S" + kamokuCnt + ".TESTDIV = R1.TESTDIV ");
            stb.append("             AND S" + kamokuCnt + ".RECEPTNO = R1.RECEPTNO ");
            stb.append("             AND S" + kamokuCnt + ".TESTSUBCLASSCD = '" + kamoku._cd + "' ");
            kamokuCnt++;
        }
        stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT I1 ON I1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND I1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND I1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND I1.EXAMNO = B1.EXAMNO ");
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L027' AND N1.NAMECD2 = I1.INTERVIEW_VALUE ");
        stb.append("     LEFT JOIN V_NAME_MST N2 ON N2.YEAR = B1.ENTEXAMYEAR AND N2.NAMECD1 = 'L027' AND N2.NAMECD2 = I1.INTERVIEW_VALUE2 ");
        stb.append(" WHERE ");
        stb.append("     B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND B1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        if ("3".equals(_param._sortDiv)) {
            stb.append("     B1.FS_CD, ");
        } else if ("2".equals(_param._sortDiv)) {
            stb.append("     VALUE(R1.TOTAL4,-1) DESC, ");
        }
        stb.append("     B1.EXAMNO ");
        return stb.toString();
    }

    private class PrintData {
        final String _examno;
        final String _name;
        final String _nameKana;
        final String _sex;
        final String _sexName;
        final String _testdiv;
        final String _fsCd;
        final String _finschoolName;
        final List _scoreList;
        final String _divRank4;
        final String _total2;
        final String _total4;
        final String _interviewValue;
        final String _interviewValue2;
        final String _simaiSchoolKind1;
        final String _simaiSchoolKind2;
        public PrintData(
                final String examno,
                final String name,
                final String nameKana,
                final String sex,
                final String sexName,
                final String testdiv,
                final String fsCd,
                final String finschoolName,
                final List scoreList,
                final String divRank4,
                final String total2,
                final String total4,
                final String interviewValue,
                final String interviewValue2,
                final String simaiSchoolKind1,
                final String simaiSchoolKind2
        ) {
            _examno = examno;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _sexName = sexName;
            _testdiv = testdiv;
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _scoreList = scoreList;
            _divRank4 = divRank4;
            _total2 = total2;
            _total4 = total4;
            _interviewValue = interviewValue;
            _interviewValue2 = interviewValue2;
            _simaiSchoolKind1 = simaiSchoolKind1;
            _simaiSchoolKind2 = simaiSchoolKind2;
        }
    }

    private class TestKamoku {
        final String _cd;
        final String _name;
        public TestKamoku(
                final String cd,
                final String name
        ) {
            _cd = cd;
            _name = name;
        }
    }

    private class AvgData {
        final String _cd;
        final String _avg;
        public AvgData(
                final String cd,
                final String avg
        ) {
            _cd = cd;
            _avg = avg;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63201 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _loginYear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _sortDiv;
        private final String _sortName;
        private final String _loginDate;
        private final String _schoolKind;
        private final String _testDivNameCd2;
        final String _applicantdivName;
        final String _testdivAbbv1;
        private final String _schoolName;
        private final List _testKamokuList;
        private final Map _avgYearMap;
        private final Map _avgLastYearMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear      = request.getParameter("ENTEXAMYEAR");
            _loginYear        = request.getParameter("LOGIN_YEAR");
            _applicantDiv     = request.getParameter("APPLICANTDIV");
            _testDiv          = request.getParameter("TESTDIV");
            _sortDiv          = request.getParameter("TAISYOU");//1:受験番号順、2:高得点順、3:出身学校順
            _sortName         = "1".equals(_sortDiv) ? "受験番号順" : "2".equals(_sortDiv) ? "高得点順" : "出身学校順";
            _loginDate        = request.getParameter("LOGIN_DATE");
            _schoolKind       = request.getParameter("SCHOOLKIND");
            _testDivNameCd2   = "P".equals(_schoolKind) ? "LP24": "L024";
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivAbbv1     = StringUtils.defaultString(getNameMst(db2, "ABBV1", _testDivNameCd2, _testDiv));
            _schoolName       = getSchoolName(db2, _entexamyear);
            _testKamokuList   = getTestKamokuList(db2);
            _avgYearMap       = getAvgMap(db2, _entexamyear);
            _avgLastYearMap   = getAvgMap(db2, _loginYear);
        }

        private Map getAvgMap(final DB2UDB db2, final String year) {
            final Map retMap = new TreeMap();
            final String sql = getAvgSql(year);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd = rs.getString("TESTSUBCLASSCD");
                    final String avg = rs.getString("CALC_AVG");
                    final AvgData avgData = new AvgData(cd, avg);
                    retMap.put(cd, avgData);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return retMap;
        }

        private String getAvgSql(final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTSUBCLASSCD, ");
            stb.append("     CALC_AVG ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_JUDGE_AVARAGE_DAT ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + year + "' ");
            stb.append("     AND APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND EXAM_TYPE = '1' ");
            stb.append("     AND TESTDIV = '" + _testDiv + "' ");
            stb.append(" ORDER BY ");
            stb.append("     TESTSUBCLASSCD ");
            return stb.toString();
        }

        private List getTestKamokuList(final DB2UDB db2) {
            final List retList = new ArrayList();
            final String fieldName = "P".equals(_schoolKind) ? "NAME3": "NAME2";
            final String sql = "SELECT NAMECD2, " + fieldName + " FROM NAME_MST WHERE NAMECD1 = 'L009' AND " + fieldName + " IS NOT NULL ORDER BY NAMECD2";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd = rs.getString("NAMECD2");
                    final String name = rs.getString(fieldName);
                    final TestKamoku testKamoku = new TestKamoku(cd, name);
                    retList.add(testKamoku);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return retList;
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private String getSchoolName(final DB2UDB db2, final String year) {
            String retSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + year + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }

    }
}

// eof

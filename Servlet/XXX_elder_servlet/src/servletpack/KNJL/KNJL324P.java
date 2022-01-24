/*
 * $Id: 592543442485862cb184313929f3b01337e47920 $
 *
 * 作成日: 2017/07/03
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
import java.util.Map;
import java.util.TreeMap;

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

public class KNJL324P {

    private static final Log log = LogFactory.getLog(KNJL324P.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List jukuList = getJukuList(db2);
        for (Iterator itJuku = jukuList.iterator(); itJuku.hasNext();) {
            Juku juku = (Juku) itJuku.next();
            final List studentList = getStudentList(db2, juku._jukuCd, juku._kyoushitsuCd);
            printOut(svf, studentList, juku);
        }
    }

    private List getJukuList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = jukuSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String prischoolCd = rs.getString("PRISCHOOLCD");
                final String prischoolClassCd = rs.getString("PRISCHOOL_CLASS_CD");
                final String jukuName = rs.getString("JUKU");
                final String kyoushitsuName = rs.getString("KYOUSHITSU");

                final Juku juku = new Juku(prischoolCd, prischoolClassCd, jukuName, kyoushitsuName);
                retList.add(juku);
            }

            db2.commit();
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private String jukuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     PRI_M.PRISCHOOLCD, ");
        stb.append("     VALUE(PRI_CM.PRISCHOOL_CLASS_CD, '0000000') AS PRISCHOOL_CLASS_CD, ");
        stb.append("     PRI_M.PRISCHOOL_NAME AS JUKU, ");
        stb.append("     VALUE(PRI_CM.PRISCHOOL_NAME, '') AS KYOUSHITSU ");
        stb.append(" FROM ");
        stb.append("     PRISCHOOL_MST PRI_M ");
        stb.append("     LEFT JOIN PRISCHOOL_CLASS_MST PRI_CM ON PRI_M.PRISCHOOLCD = PRI_CM.PRISCHOOLCD ");
        stb.append(" WHERE ");
        stb.append("     PRI_M.PRISCHOOLCD || '-' || VALUE(PRI_CM.PRISCHOOL_CLASS_CD, '0000000') IN (" + _param._prischoolInState + ") ");
        stb.append(" ORDER BY ");
        stb.append("     PRI_M.PRISCHOOLCD, ");
        stb.append("     PRI_CM.PRISCHOOL_CLASS_CD ");

        return stb.toString();
    }

    private void setTitle(final Vrw32alp svf, final Juku juku) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamYear + "-04-01") + "度　" + _param._applicantDivName + "入試 " + _param._testDivName + "　塾別結果");
        svf.VrsOut("DATE", "実施日：" + KNJ_EditDate.h_format_JP(_param._testDate));
        svf.VrsOut("PRISCHOOL_NAME", juku._jukuName + "　" + juku._kyoushitsuName);

        int subclassCnt = 1;
        for (Iterator itSubclass = _param._subclassMap.keySet().iterator(); itSubclass.hasNext();) {
            final String subclassCd = (String) itSubclass.next();
            final Subclass subclass = (Subclass) _param._subclassMap.get(subclassCd);
            svf.VrsOut("CLASS_NAME1_" + subclassCnt, subclass._subclassName);
            svf.VrsOut("CLASS_NAME2_" + subclassCnt, subclass._subclassName);
            if (null != subclass._avg) {
                svf.VrsOut("AVE" + subclassCnt, String.valueOf(subclass._avg));
            }
            subclassCnt++;
        }
    }

    private void printOut(final Vrw32alp svf, final List studentList, final Juku juku) {
        svf.VrSetForm("KNJL324P.frm", 1);
        final int maxCnt = 30;
        final int pageCnt = studentList.size() >= maxCnt ? studentList.size() / maxCnt : 0;
        final int pageAmari = studentList.size() % maxCnt > 0 ? 1 : 0;
        final int allPage = pageCnt + pageAmari;
        setTitle(svf, juku);

        int lineCnt = 1;
        final int maxLine = 30;
        int page = 1;
        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            if (lineCnt > maxLine) {
                lineCnt = 1;
                page++;
                svf.VrEndPage();
                setTitle(svf, juku);
            }
            svf.VrsOut("PAGE1", String.valueOf(page));
            svf.VrsOut("PAGE2", String.valueOf(allPage));

            final Student student = (Student) itStudent.next();
            svf.VrsOutn("JUDGE", lineCnt, student._judgeName);
            svf.VrsOutn("JUDGE1", lineCnt, student._judgeName);
            svf.VrsOutn("JUDGE2", lineCnt, student._judgeName);
            svf.VrsOutn("EXAM_NO1", lineCnt, student._examNo);
            final String[] nameArray = StringUtils.split(student._name, "　");
            final String nameSeiField = getMS932ByteLength(nameArray[0]) > 12 ? "1_2" : "1_1";
            svf.VrsOutn("NAME" + nameSeiField, lineCnt, nameArray[0]);
            final String nameMeiField = getMS932ByteLength(nameArray[1]) > 12 ? "2_2" : "2_1";
            svf.VrsOutn("NAME" + nameMeiField, lineCnt, nameArray[1]);

            int subCnt = 1;
            for (Iterator itSubclass = student._scoreSubclassMap.keySet().iterator(); itSubclass.hasNext();) {
                final String subclassCd = (String) itSubclass.next();
                final Subclass subclass = (Subclass) student._scoreSubclassMap.get(subclassCd);
                svf.VrsOutn("POINT" + subCnt, lineCnt, subclass._score);
                subCnt++;
            }
            svf.VrsOutn("POINT_ALL", lineCnt, student._total4);

            subCnt = 1;
            for (Iterator itSubclass = student._devSubclassMap.keySet().iterator(); itSubclass.hasNext();) {
                final String subclassCd = (String) itSubclass.next();
                final Subclass subclass = (Subclass) student._devSubclassMap.get(subclassCd);
                svf.VrsOutn("DEVI" + subCnt, lineCnt, subclass._dev);
                subCnt++;
            }
            svf.VrsOutn("DEVI_ALL", lineCnt, student._deviation4);
            svf.VrsOutn("RANK1", lineCnt, student._totalRank4);

            lineCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private List getStudentList(final DB2UDB db2, final String juku, final String kyoushitsu) throws SQLException {
        final List retList = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = studentSql(juku, kyoushitsu);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String judgeDiv = rs.getString("JUDGEDIV");
                final String judgeName = rs.getString("JUDGENAME");
                final String total4 = rs.getString("TOTAL4");
                final String total2 = rs.getString("TOTAL2");
                final String deviation4 = rs.getString("DEVIATION4");
                final String deviation2 = rs.getString("DEVIATION2");
                final String totalRank4 = rs.getString("TOTAL_RANK4");
                final String totalRank2 = rs.getString("TOTAL_RANK2");

                final Student student = new Student(receptNo, examNo, name, judgeDiv, judgeName, total4, total2, deviation4, deviation2, totalRank4, totalRank2);
                student.setScoreMap(db2);
                retList.add(student);
            }

            db2.commit();
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private String studentSql(final String juku, final String kyoushitsu) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     RECEPT.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     PRI_M.PRISCHOOL_NAME AS JUKU, ");
        stb.append("     PRI_CM.PRISCHOOL_NAME AS KYOUSHITSU, ");
        stb.append("     RECEPT.JUDGEDIV, ");
        stb.append("     L013.NAME1 AS JUDGENAME, ");
        stb.append("     RECEPT.TOTAL4, ");
        stb.append("     RECEPT.TOTAL2, ");
        stb.append("     RECEPT.JUDGE_DEVIATION AS DEVIATION4, ");
        stb.append("     RECEPT.LINK_JUDGE_DEVIATION AS DEVIATION2, ");
        stb.append("     RECEPT.TOTAL_RANK4, ");
        stb.append("     RECEPT.TOTAL_RANK2, ");
        stb.append("     BASE_D012.REMARK1 AS ZENKI_EXAMNO, ");
        stb.append("     BASE_D014.REMARK2 AS KYOUDAI ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND RECEPT.EXAMNO = BASE.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
        stb.append("          AND RECEPT.JUDGEDIV = L013.NAMECD2 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D008 ON RECEPT.ENTEXAMYEAR = BASE_D008.ENTEXAMYEAR ");
        stb.append("           AND RECEPT.APPLICANTDIV = BASE_D008.APPLICANTDIV ");
        stb.append("           AND RECEPT.EXAMNO = BASE_D008.EXAMNO ");
        stb.append("           AND BASE_D008.SEQ = '008' ");
        stb.append("           AND BASE_D008.REMARK1 = '" + juku + "' ");
        stb.append("           AND VALUE(BASE_D008.REMARK3, '0000000') = '" + kyoushitsu + "' ");
        stb.append("     LEFT JOIN PRISCHOOL_MST PRI_M ON BASE_D008.REMARK1 = PRI_M.PRISCHOOLCD ");
        stb.append("     LEFT JOIN PRISCHOOL_CLASS_MST PRI_CM ON BASE_D008.REMARK1 = PRI_CM.PRISCHOOLCD ");
        stb.append("          AND BASE_D008.REMARK3 = PRI_CM.PRISCHOOL_CLASS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D012 ON RECEPT.ENTEXAMYEAR = BASE_D012.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = BASE_D012.APPLICANTDIV ");
        stb.append("          AND RECEPT.EXAMNO = BASE_D012.EXAMNO ");
        stb.append("          AND BASE_D012.SEQ = '012' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D014 ON RECEPT.ENTEXAMYEAR = BASE_D014.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = BASE_D014.APPLICANTDIV ");
        stb.append("          AND RECEPT.EXAMNO = BASE_D014.EXAMNO ");
        stb.append("          AND BASE_D014.SEQ = '014' ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     RECEPT.EXAMNO ");

        return stb.toString();
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
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
        final String _applicantDiv;
        final String _applicantDivName;
        final String _testDiv;
        final String _testDivName;
        final String _testDate;
        final String[] _schoolSelected;
        String _prischoolInState;
        final String _entexamYear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final Map _subclassMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _schoolSelected = request.getParameterValues("SCHOOL_SELECTED");
            String sep = "";
            _prischoolInState = "";
            for (int i = 0; i < _schoolSelected.length; i++) {
                final String jukuKyoushitsuCd = _schoolSelected[i];
                _prischoolInState += sep + "'" + jukuKyoushitsuCd + "'";
                sep = ",";
            }
            _entexamYear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantDivName = getNameMst(db2, "L003", _applicantDiv, "NAME1");
            final String testNameCd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            _testDivName = getNameMst(db2, testNameCd1, _testDiv, "NAME1");
            _testDate = getNameMst(db2, testNameCd1, _testDiv, "NAMESPARE1");
            _subclassMap = getSubclassMap(db2);
        }

        private String getNameMst(final DB2UDB db2, final String nameCd1, final String nameCd2, final String fieldName) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getNameMstSql(nameCd1, nameCd2);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString(fieldName);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getNameMstSql(final String namecd1, final String namecd2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = '" + namecd1 + "' ");
            stb.append("     AND NAMECD2 = '" + namecd2 + "' ");
            return stb.toString();
        }

        private Map getSubclassMap(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String subclassName = "1".equals(_applicantDiv) ? "NAME1" : "NAME2";
                final String sql = getNameMstSql(subclassName);
                log.debug(" sql =" + sql);

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String nameCd2 = rs.getString("NAMECD2");
                    final String name = rs.getString(subclassName);
                    final String cnt = rs.getString("COUNT");
                    final String avg = rs.getString("AVG");
                    if (null != name && !"".equals(name)) {
                        final Subclass subclass = new Subclass(nameCd2, name, cnt, avg);
                        retMap.put(nameCd2, subclass);
                    }
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private String getNameMstSql(final String subclassName) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     L009.NAMECD2, ");
            stb.append("     L009." + subclassName + ", ");
            stb.append("     AVG_DAT.COUNT, ");
            stb.append("     AVG_DAT.AVARAGE_TOTAL AS AVG ");
            stb.append(" FROM ");
            stb.append("     NAME_MST L009 ");
            stb.append("     LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT AVG_DAT ON AVG_DAT.ENTEXAMYEAR = '" + _entexamYear + "' ");
            stb.append("          AND AVG_DAT.APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("          AND AVG_DAT.TESTDIV = '" + _testDiv + "' ");
            stb.append("          AND L009.NAMECD2 = AVG_DAT.TESTSUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     L009.NAMECD1 = 'L009' ");
            stb.append("     AND L009." + subclassName + " IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     L009.NAMECD2 ");
            return stb.toString();
        }

        private Map getInterViewSubclassMap(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getInterViewAvgSql();
                log.debug(" sql =" + sql);

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String nameCd2 = rs.getString("TESTSUBCLASSCD");
                    final String name = rs.getString("SUBCLASSNAME");
                    final String cnt = rs.getString("COUNT");
                    final String avg = rs.getString("AVG");
                    final Subclass subclass = new Subclass(nameCd2, name, cnt, avg);
                    retMap.put(nameCd2, subclass);
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private String getInterViewAvgSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     AVG_DAT.TESTSUBCLASSCD, ");
            stb.append("     CASE WHEN AVG_DAT.TESTSUBCLASSCD = 'A' ");
            stb.append("          THEN '面接' ");
            stb.append("          WHEN AVG_DAT.TESTSUBCLASSCD = 'B' ");
            stb.append("          THEN '面接なし' ");
            stb.append("          ELSE '面接あり' ");
            stb.append("     END AS SUBCLASSNAME, ");
            stb.append("     AVG_DAT.COUNT, ");
            stb.append("     AVG_DAT.AVARAGE_TOTAL AS AVG ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_JUDGE_AVARAGE_DAT AVG_DAT ");
            stb.append(" WHERE ");
            stb.append("     AVG_DAT.ENTEXAMYEAR = '" + _entexamYear + "' ");
            stb.append("     AND AVG_DAT.APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND AVG_DAT.TESTDIV = '" + _testDiv + "' ");
            stb.append("     AND AVG_DAT.TESTSUBCLASSCD IN ('A', 'B', 'C') ");

            return stb.toString();
        }

    }

    /** 生徒クラス */
    private class Student {
        final String _receptNo;
        final String _examNo;
        final String _name;
        final String _judgeDiv;
        final String _judgeName;
        final String _total4;
        final String _total2;
        final String _deviation4;
        final String _deviation2;
        final String _totalRank4;
        final String _totalRank2;
        Map _scoreSubclassMap;
        Map _devSubclassMap;

        public Student(
                final String receptNo,
                final String examNo,
                final String name,
                final String judgeDiv,
                final String judgeName,
                final String total4,
                final String total2,
                final String deviation4,
                final String deviation2,
                final String totalRank4,
                final String totalRank2
        ) throws SQLException {
            _receptNo = receptNo;
            _examNo = examNo;
            _name = name;
            _judgeDiv = judgeDiv;
            _judgeName = judgeName;
            _total4 = total4;
            _total2 = total2;
            _totalRank4 = totalRank4;
            _totalRank2 = totalRank2;
            _deviation4 = deviation4;
            _deviation2 = deviation2;
            _scoreSubclassMap = setSubclassMap(_param._subclassMap);
            _devSubclassMap = setSubclassMap(_param._subclassMap);
        }

        private Map setSubclassMap(final Map subclassMap) {
            final Map retMap = new TreeMap();
            for (Iterator itSubMap = subclassMap.keySet().iterator(); itSubMap.hasNext();) {
                final String subKey = (String) itSubMap.next();
                final Subclass paraSub = (Subclass) subclassMap.get(subKey);
                final Subclass setSub = new Subclass(paraSub._subclassCd, paraSub._subclassName, paraSub._cnt, paraSub._avg);
                retMap.put(subKey, setSub);
            }
            return retMap;
        }

        public void setScoreMap(final DB2UDB db2) throws SQLException {
            setScoreData(db2, _scoreSubclassMap, _devSubclassMap);
        }

        private void setScoreData(final DB2UDB db2, final Map scoreMap, final Map devMap) throws SQLException {

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                String sql = "";
                sql = getScoreSql();
                log.debug(" sql =" + sql);

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = rs.getString("TESTSUBCLASSCD");
                    final String score = rs.getString("SCORE");
                    final String stdScore = rs.getString("STD_SCORE");
                    if (scoreMap.containsKey(subclassCd)) {
                        final Subclass subclass = (Subclass) scoreMap.get(subclassCd);
                        subclass._score = score;
                    }
                    if (devMap.containsKey(subclassCd)) {
                        final Subclass subclass = (Subclass) devMap.get(subclassCd);
                        subclass._dev = stdScore;
                    }
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getScoreSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCORE.TESTSUBCLASSCD, ");
            stb.append("     SCORE.SCORE, ");
            stb.append("     SCORE.STD_SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DAT SCORE ");
            stb.append(" WHERE ");
            stb.append("     SCORE.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            stb.append("     AND SCORE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND SCORE.TESTDIV = '" + _param._testDiv + "' ");
            stb.append("     AND SCORE.EXAM_TYPE = '1' ");
            stb.append("     AND SCORE.RECEPTNO = '" + _receptNo + "' ");
            return stb.toString();
        }
    }

    /** 科目 */
    private class Subclass {
        final String _subclassCd;
        final String _subclassName;
        final String _cnt;
        final String _avg;
        String _score;
        String _dev;

        public Subclass(
                final String subclassCd,
                final String subclassName,
                final String cnt,
                final String avg
        ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _cnt = cnt;
            _avg = avg;
        }
    }

    /** 塾 */
    private class Juku {
        final String _jukuCd;
        final String _kyoushitsuCd;
        final String _jukuName;
        final String _kyoushitsuName;

        public Juku(
                final String jukuCd,
                final String kyoushitsu,
                final String jukuName,
                final String kyoushitsuName
        ) {
            _jukuCd = jukuCd;
            _kyoushitsuCd = kyoushitsu;
            _jukuName = jukuName;
            _kyoushitsuName = kyoushitsuName;
        }
    }
}

// eof


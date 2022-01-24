package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;


/**
 * 皆勤・精勤者度数分布表
 *
 * @author yogi
 *
 */
public class KNJD665N {
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJD665N.class);

    /**
     * KNJD.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
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
            close(db2, svf);
        }

    }

    /**
     * 印刷処理メイン
     * @param db2    ＤＢ接続オブジェクト
     * @param svf    帳票オブジェクト
     * @return        無し
     * @throws Exception
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {

        //成績取得
        Map resultMap = getScoreDat(db2);
        //表の下部のデータを取得
        Map underDataMap = getUnderDat(db2);
        final String finSubclsCd = "99-H-99-999999";

        svf.VrSetForm("KNJD665N.frm", 1);
        setTitle(db2, svf);
        int colCnt = 0;
        for (Iterator itr = resultMap.keySet().iterator();itr.hasNext();) {
            String subclsCd = (String)itr.next();
            if (finSubclsCd.equals(subclsCd)) {
                continue; //all9は最後に出力するので、pass
            }
            PrintData outwk = (PrintData)resultMap.get(subclsCd);
            colCnt++;
            if (colCnt > 35) {
                colCnt = 1;
                svf.VrEndPage();
                setTitle(db2, svf);
            }
            svf.VrsOutn("SUBCLASS_NAME", colCnt, outwk._subclassName);
            //成績出力
            svf.VrsOutn("NUM1", colCnt, outwk._lv10);
            svf.VrsOutn("NUM2", colCnt, outwk._lv9);
            svf.VrsOutn("NUM3", colCnt, outwk._lv8);
            svf.VrsOutn("NUM4", colCnt, outwk._lv7);
            svf.VrsOutn("NUM5", colCnt, outwk._lv6);
            svf.VrsOutn("NUM6", colCnt, outwk._lv5);
            svf.VrsOutn("NUM7", colCnt, outwk._lv4);
            svf.VrsOutn("NUM8", colCnt, outwk._lvU3);
            if (underDataMap.containsKey(subclsCd)) {
                //下表出力
                PrtAvgData underout = (PrtAvgData)underDataMap.get(subclsCd);
                svf.VrsOutn("EXAM_NUM", colCnt, underout._count);
                svf.VrsOutn("AVERAGE", colCnt, underout._avg);
                svf.VrsOutn("MAX", colCnt, underout._highScore);
                svf.VrsOutn("MIN", colCnt, underout._lowScore);
            }
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
    	final String baseDate = ("2019".equals(_param._year)) ? "05-01" : "04-01";
        final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._year + "-" + baseDate ) + "度";
        svf.VrsOut("TITLE", nendo + " " + _param._semesterName + " " + _param._testName + " " + "度数分布表");
        //指示画面で選択された学年が中学ならコース名称を非表示
        if ("J".equals(_param._schoolKind)) {
        	svf.VrsOut("GRADE", _param._gradeName);
        } else {
        	svf.VrsOut("GRADE", _param._gradeName + " " + _param._courseName);
        }
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
    }

    //成績取得処理
    private Map getScoreDat(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = getScoreDatSql();
            log.debug("sql = " + sql);
            db2.query(sql);
            rs = db2.getResultSet();
            while( rs.next() ){
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String lv10 = StringUtils.defaultString(rs.getString("LV10"), "");
                final String lv9 = StringUtils.defaultString(rs.getString("LV9"), "");
                final String lv8 = StringUtils.defaultString(rs.getString("LV8"), "");
                final String lv7 = StringUtils.defaultString(rs.getString("LV7"), "");
                final String lv6 = StringUtils.defaultString(rs.getString("LV6"), "");
                final String lv5 = StringUtils.defaultString(rs.getString("LV5"), "");
                final String lv4 = StringUtils.defaultString(rs.getString("LV4"), "");
                final String lvU3 = StringUtils.defaultString(rs.getString("LVU3"), "");
                PrintData addobj = new PrintData(subclassCd, subclassName, lv10, lv9, lv8, lv7, lv6, lv5, lv4, lvU3);
                retMap.put(subclassCd, addobj);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;
    }

    private String getScoreDatSql() {
        StringBuffer stb = new StringBuffer();

        stb.append(" WITH BASE_SCOREDAT AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T1.SUBCLASSCD AS CHKCD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.SCORE, ");
        stb.append("     T1.AVG ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_SDIV_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("       ON T2.YEAR = T1.YEAR  ");
        stb.append("      AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._testKind_ItemCd + "' ");
        stb.append("     AND T2.GRADE = '" + _param._grade + "' ");
        stb.append("     AND T2.COURSECD || T2.MAJORCD = '" + _param._CourseMajorCd + "'");
        stb.append("     AND T2.COURSECODE = '" + _param._courseCode + "' ");
        stb.append("     AND T1.SUBCLASSCD NOT IN ('333333','555555', '99999A', '99999B') ");
        stb.append(" ORDER BY ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T2.SUBCLASSNAME, ");
        stb.append("     SUM(CASE WHEN T1.SCORE = 10 THEN 1 ELSE 0 END) AS LV10, ");
        stb.append("     SUM(CASE WHEN T1.SCORE = 9 THEN 1 ELSE 0 END) AS LV9, ");
        stb.append("     SUM(CASE WHEN T1.SCORE = 8 THEN 1 ELSE 0 END) AS LV8, ");
        stb.append("     SUM(CASE WHEN T1.SCORE = 7 THEN 1 ELSE 0 END) AS LV7, ");
        stb.append("     SUM(CASE WHEN T1.SCORE = 6 THEN 1 ELSE 0 END) AS LV6, ");
        stb.append("     SUM(CASE WHEN T1.SCORE = 5 THEN 1 ELSE 0 END) AS LV5, ");
        stb.append("     SUM(CASE WHEN T1.SCORE = 4 THEN 1 ELSE 0 END) AS LV4, ");
        stb.append("     SUM(CASE WHEN T1.SCORE <= 3 THEN 1 ELSE 0 END) AS LVU3 ");
        stb.append(" FROM ");
        stb.append("     BASE_SCOREDAT T1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST T2 ");
        stb.append("       ON T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.CHKCD <> '999999'");
        stb.append(" GROUP BY ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T2.SUBCLASSNAME ");
        stb.append(" ORDER BY SUBCLASSCD");

        return stb.toString();
    }

    private Map getUnderDat(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = getUnderDatSql();
            log.debug("getUnderDatSql = " + sql);
            db2.query(sql);
            rs = db2.getResultSet();
            while( rs.next() ){
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String chkcd = rs.getString("CHKCD");
                final String count = rs.getString("COUNT");
                final String avg = rs.getString("AVG");
                final String highscore = rs.getString("HIGHSCORE");
                final String lowscore = rs.getString("LOWSCORE");
                PrtAvgData addobj = new PrtAvgData(subclassCd, chkcd, count, avg, highscore, lowscore);
                retMap.put(subclassCd, addobj);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;
    }

    private String getUnderDatSql() {
        StringBuffer stb = new StringBuffer();

        //総合の最下段の値
        stb.append(" WITH SOUGOU AS ( ");
        stb.append("   SELECT ");
        stb.append("     SUM(INT (AVG + 0.5)) / (COUNT(*) * 1.0) AS SOUGOU_AVG ");
        stb.append("   FROM ");
        stb.append("     RECORD_RANK_SDIV_DAT T1 ");
        stb.append("   INNER JOIN ");
        stb.append("        SCHREG_REGD_DAT T2 ");
        stb.append("   ON T1.YEAR = T2.YEAR ");
        stb.append("      AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("      AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("   WHERE ");
        stb.append("     T1.YEAR = '"+_param._year+"' ");
        stb.append("     AND T1.SEMESTER = '"+ _param._semester +"' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '"+ _param._testKind_ItemCd +"' ");
        stb.append("      AND T2.GRADE = '"+_param._grade+"' ");
        stb.append("      AND T2.COURSECD || T2.MAJORCD = '"+ _param._CourseMajorCd +"' ");
        stb.append("      AND T2.COURSECODE = '" + _param._courseCode + "' ");
        stb.append("     AND T1.SUBCLASSCD = '999999' ");
        stb.append("   GROUP BY ");
        stb.append("     T1.YEAR ");
        stb.append("     , T1.SEMESTER ");
        stb.append("     , T1.TESTKINDCD ");
        stb.append("     , T1.TESTITEMCD ");
        stb.append("     , T1.SCORE_DIV ");
        stb.append("     , T1.CLASSCD ");
        stb.append("     , T1.SCHOOL_KIND ");
        stb.append("     , T1.CURRICULUM_CD ");
        stb.append("     , T1.SUBCLASSCD ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T1.SUBCLASSCD AS CHKCD, ");
        stb.append("     T1.COUNT, ");
        stb.append("     CASE WHEN T1.SUBCLASSCD = '999999' THEN RTRIM(LTRIM(CAST(DECIMAL((SELECT SOUGOU_AVG FROM SOUGOU), 4, 1) as VARCHAR(5)))) "); //小数1位を四捨五入(整数表示)
        stb.append("          ELSE RTRIM(LTRIM(CAST(DECIMAL(INT(T1.AVG*10+0.5)/10.0,4,1) AS VARCHAR(5)))) ");      //小数2位を四捨五入(１位まで表示)
        stb.append("          END AS AVG, ");
        stb.append("     T1.HIGHSCORE, ");
        stb.append("     T1.LOWSCORE ");
        stb.append(" FROM ");
        stb.append("     RECORD_AVERAGE_SDIV_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._testKind_ItemCd + "' ");
        stb.append("     AND T1.AVG_DIV = '3' ");  //固定
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("     AND T1.COURSECD || T1.MAJORCD = '" + _param._CourseMajorCd + "' ");
        stb.append("     AND T1.COURSECODE = '" + _param._courseCode + "' ");
        stb.append("     AND T1.SUBCLASSCD NOT IN ('333333','555555', '99999A', '99999B') ");
        stb.append(" ORDER BY ");
        stb.append("     SUBCLASSCD ");
        log.debug(stb.toString());
        return stb.toString();
    }

    private class PrtAvgData {
        final String _subclassCd;
        final String _chkCd;
        final String _count;
        final String _avg;
        final String _highScore;
        final String _lowScore;

        public PrtAvgData (final String subclassCd, final String chkCd, final String count, final String avg, final String highScore, final String lowScore) {
            _subclassCd = subclassCd;
            _chkCd = chkCd;
            _count = count;
            _avg = avg;
            _highScore = highScore;
            _lowScore = lowScore;
        }
    }

    private class PrintData {
        final String _subclassCd;
        final String _subclassName;
        final String _lv10;
        final String _lv9;
        final String _lv8;
        final String _lv7;
        final String _lv6;
        final String _lv5;
        final String _lv4;
        final String _lvU3;

        public PrintData (final String subclassCd, final String subclassName, final String lv10, final String lv9, final String lv8, final String lv7, final String lv6, final String lv5, final String lv4, final String lvU3) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _lv10 = lv10;
            _lv9 = lv9;
            _lv8 = lv8;
            _lv7 = lv7;
            _lv6 = lv6;
            _lv5 = lv5;
            _lv4 = lv4;
            _lvU3 = lvU3;
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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72204 $ $Date: 2020-02-05 15:35:06 +0900 (水, 05 2 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _semesterName;
        private final String _ctrlSemester;
        private final String _grade;
        private final String _schoolKind;
        private final String _gradeName;
        private final String _courseCode;
        private final String _courseName;
        private final String _testKind_ItemCd;
        private final String _testName;
        private final String _programid;
        private final String _loginDate;
        private final String _schoolCd;
        private final String _staffCd;
        private String _CourseMajorCd;
        private String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _grade = request.getParameter("GRADE");
            _courseCode = request.getParameter("COURSECODE");
            _testKind_ItemCd = request.getParameter("TESTKIND_ITEMCD");
            _programid = request.getParameter("PRGID");
            _loginDate = request.getParameter("LOGIN_DATE");
            _schoolCd = request.getParameter("SCHOOLCD");
            _staffCd = request.getParameter("STAFFCD");
            boolean setChkFlg = false;
            if (!"".equals(request.getParameter("COURSEMAJOR_LIST"))) {
                final String[] cmlist = StringUtils.split(request.getParameter("COURSEMAJOR_LIST"), ",");
                for (int cnt = 0;cnt < cmlist.length;cnt++) {
                    if (cmlist[cnt].substring(0, 4).equals(_courseCode)) {
                        _CourseMajorCd = cmlist[cnt].substring(5);
                        setChkFlg = true;
                        break;
                    }
                }
            }
            if (!setChkFlg) {
                log.warn("COURSECODE探索エラー");
                _CourseMajorCd = "";
            }
            _schoolName = StringUtils.defaultString(getSchoolName(db2), "");
            _semesterName = StringUtils.defaultString(getSemesterName(db2), "");
            _testName = StringUtils.defaultString(getTestName(db2), "");
            _gradeName = StringUtils.defaultString(getGradeName(db2), "");
            _courseName = StringUtils.defaultString(getCourseName(db2), "");
        }

        private String getSchoolName(final DB2UDB db2) {
            String retStr = "";
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.SCHOOLNAME1 FROM SCHOOL_MST T1 ");
            stb.append(" LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' AND T1.SCHOOLCD = '" + _schoolCd + "' AND T2.GRADE = '" + _grade + "' ");
            log.debug(" sql = " + stb.toString());
            retStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            return retStr;
        }

        private String getGradeName(final DB2UDB db2) {
            String retStr = "";
            String query = " SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
            log.debug(" sql = " + query);
            retStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, query));
            retStr = String.valueOf(Integer.parseInt(retStr));
            return retStr + "年";
        }

        private String getSemesterName(final DB2UDB db2) {
            String retStr = "";
            String query = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
            log.debug(" sql = " + query);
            retStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, query));
            return retStr;
        }

        private String getTestName(final DB2UDB db2) {
            String retStr = "";
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ");
            stb.append(" WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND SCORE_DIV = '08' ");
            stb.append("   AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testKind_ItemCd + "' ");
            log.debug(" sql = " + stb.toString());
            retStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            return retStr;
        }

        private String getCourseName(final DB2UDB db2) {
            String retStr = "";
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT COURSECODENAME FROM COURSECODE_MST WHERE COURSECODE = '" + _courseCode + "' ");
            log.debug(" sql = " + stb.toString());
            retStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            return retStr;
        }
    }

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (null != svf) {
            svf.VrQuit();
        }
    }
}

package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;


/**
 * 推薦名簿
 *
 * @author nakasone
 *
 */
public class KNJD617E {
    private static final String SEMEALL = "9";
    private static final String FORM_NAME = "KNJD645.frm";
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJD617E.class);

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
     * @param db2	ＤＢ接続オブジェクト
     * @param svf	帳票オブジェクト
     * @return		対象データ存在フラグ(有り：TRUE、無し：FALSE)
     * @throws Exception
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {

        // 学校コード取得
        //String sourse_div = getSourse_div(db2);
        Map schMap = getStudentInfo(db2);
        getSubclsInfo(db2, schMap);
        getScoreInfo(db2, schMap);
        Map avgMap = getAvgInfo(db2);
        getValidationInfo(db2, schMap);
        getTestInfo(db2, schMap);
        getAttendInfo(db2, schMap);
        getCommitteeInfo(db2, schMap);
        getClubInfo(db2, schMap);

        for (Iterator ite = schMap.keySet().iterator() ; ite.hasNext();) {
            final String schregno = (String)ite.next();
            final Student putObj = (Student)schMap.get(schregno);

            svf.VrSetForm("KNJD617E.frm", 4);
            setTitle(db2, svf, putObj);
            printData(db2, svf, putObj, avgMap);

            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final Student putObj) {
        svf.VrsOut("TITLE", _param._certSchoolName + " " + _param._gradeName + " " + _param._semesterName + "保護者会個人資料");
        svf.VrsOut("HR_NAME", putObj._hr_Name + " " + String.valueOf(Integer.parseInt(putObj._attendno)) + "番");
        svf.VrsOut("NAME", putObj._name);
        svf.VrsOut("SEMESTER", _param._semesterName + " " + _param._testName + "結果");
        //出欠のタイトル
        svf.VrsOut("ATTEND_TITLE", KNJ_EditDate.h_format_JP_MD(_param._edate) + "(" + _param._semesterName + _param._testName + "最終日)までの出欠記録");
    }
    private void printData(final DB2UDB db2, final Vrw32alp svf, final Student putObj, final Map avgMap) {
        //科目別の表
        int colCnt = 0;
        AvgSum hrAvgSum = new AvgSum();
        AvgSum grAvgSum = new AvgSum();
        for (Iterator ite = putObj._subclsMap.keySet().iterator();ite.hasNext();) {
            final String sclsCdStr = (String)ite.next();
            colCnt++;
            final SubclsInfo sclsObj = (SubclsInfo)putObj._subclsMap.get(sclsCdStr);
            int sclen = KNJ_EditEdit.getMS932ByteLength(sclsObj._subclassabbv);
            final String snField = sclen > 8 ? "3" : sclen > 6 ? "2" : "1";
            svf.VrsOutn("SUBCLASS_NAME"+snField, colCnt, sclsObj._subclassabbv);
            if (putObj._scoreMap.containsKey(sclsCdStr)) {
                ScoreInfo scoreObj = (ScoreInfo)putObj._scoreMap.get(sclsCdStr);
                svf.VrsOutn("SCORE", colCnt, scoreObj._score);
            }
            if (avgMap.containsKey("2:" + sclsCdStr)) {
                AvgInfo avgObj = (AvgInfo)avgMap.get("2:" + sclsCdStr);
                svf.VrsOutn("HR_AVE", colCnt, avgObj.getAvgRound(1));
                hrAvgSum._count++;
                hrAvgSum.addAvg(avgObj._avg);

            }
            if (avgMap.containsKey("1:" + sclsCdStr)) {
                AvgInfo avgObj = (AvgInfo)avgMap.get("1:" + sclsCdStr);
                svf.VrsOutn("GRADE_AVE", colCnt, avgObj.getAvgRound(1));
                grAvgSum._count++;
                grAvgSum.addAvg(avgObj._avg);
            }
        }
        //各種合計・平均
        svf.VrsOut("TOTAL_HR_AVE", hrAvgSum.getAvgRound(1));         //クラス平均の合計
        svf.VrsOut("AVE_HR_AVE", hrAvgSum.getAvgAverageRound(1));    //クラス平均の合計の平均
        svf.VrsOut("TOTAL_GRADE_AVE", grAvgSum.getAvgRound(1));      //学年平均の合計
        svf.VrsOut("AVE_GRADE_AVE", grAvgSum.getAvgAverageRound(1)); //学年平均の合計の平均

        final String getTotalCd = "99-" + _param._setschoolkind + "-99-999999";
        //クラス/学年順位・本人合計
        if (putObj._scoreMap.containsKey(getTotalCd)) {
            ScoreInfo scoreObj = (ScoreInfo)putObj._scoreMap.get(getTotalCd);
            svf.VrsOut("HR_RANK1", scoreObj._classRank);     //クラス順位
            svf.VrsOut("GRADE_RANK1", scoreObj._gradeRank);  //学年順位
            svf.VrsOut("TOTAL_SCORE", scoreObj._score);      //本人合計点
            svf.VrsOut("AVE_SCORE", scoreObj.getAvgRound(1));          //本人平均点

        }
        if (avgMap.containsKey("2:" + getTotalCd)) {
            AvgInfo avgObj = (AvgInfo)avgMap.get("2:"+getTotalCd);
            svf.VrsOut("HR_RANK2", avgObj._count);  //クラス人数
        }
        if (avgMap.containsKey("1:" + getTotalCd)) {
            AvgInfo avgObj = (AvgInfo)avgMap.get("1:"+getTotalCd);
            svf.VrsOut("GRADE_RANK2", avgObj._count);  //学年人数
        }
        //係・委員会
        if (putObj._committeeMap.size() > 0) {
            svf.VrsOut("COMMITTEE", putObj.getConnectCommitteeStr());
        }
        //クラブ
        if (putObj._clubMap.size() > 0) {
            svf.VrsOut("CLUB", putObj.getConnectClubStr());
        }

        //出欠
        if (putObj._Attendance != null) {
            svf.VrsOut("SICK", putObj._Attendance._sick);
            svf.VrsOut("LATE", putObj._Attendance._late);
            svf.VrsOut("EARLY", putObj._Attendance._early);
            svf.VrsOut("ABSENT", putObj._Attendance._absence);
            svf.VrsOut("MOURNING", putObj._Attendance._mourning);
            svf.VrsOut("SUSPEND", putObj._Attendance._suspend);
        }

        if (!"".equals(StringUtils.defaultString(putObj._hyotei, ""))) {
            svf.VrsOut("DEVI_AVE", putObj._hyotei);
        }

        //小テスト科目名(2つだけ採用する)
        final Map jituryokuTbl = putObj.decidePrintJituryokuTest(2);
        int cnt = 0;
        for (Iterator itt = jituryokuTbl.keySet().iterator();itt.hasNext();) {
            final String kStr = (String)itt.next();
            final TestInfo nObj = (TestInfo)jituryokuTbl.get(kStr);
            cnt++;
            svf.VrsOut("SMALL_TEST_NAME" + cnt, nObj._subclass_Abbv);
        }

        //小テスト出力
        String bakProfCdChk = null;
        int maxRec = 8;
        int recCnt = 0;
        for (Iterator itr = putObj._jituryokuTMap.keySet().iterator();itr.hasNext();) {
            final String kStr = (String)itr.next();
            if (kStr.indexOf(":") < 0) continue;
            final String[] cutWk = StringUtils.split(kStr, ':');
            if (cutWk.length > 1 && jituryokuTbl.containsKey(cutWk[1])) {
                final TestInfo nObj = (TestInfo)putObj._jituryokuTMap.get(kStr);
                if (recCnt >= maxRec) continue;
                if (bakProfCdChk != null && !nObj._proficiencycd.equals(bakProfCdChk)) {
                    //切り替わりなので、レコード切替
                    recCnt++;
                    svf.VrEndRecord();
                }
                int chkidx = nObj.findMapidx(jituryokuTbl);
                if (chkidx >= 0) {
                    svf.VrsOut("SMALL_TEST_NUM", nObj._proficiencyname1);
                    svf.VrsOut("SMALL_TEST_SCORE" + (chkidx+1), nObj._score);
                }
                bakProfCdChk = nObj._proficiencycd;
            }
        }
        if (recCnt < maxRec) {
            svf.VrEndRecord();  //最後の出力
        }
        _hasData = true;
    }

    private Map getStudentInfo(final DB2UDB db2) {
        Map retMap = new LinkedMap();
        final String sql = getStudentInfoSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String grade_Cd = rs.getString("GRADE_CD");
                final String hr_Class = rs.getString("HR_CLASS");
                final String hr_Name = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final Student addwk = new Student(grade, grade_Cd, hr_Class, hr_Name, attendno, schregno, name);
                retMap.put(schregno, addwk);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }
    private String getStudentInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T0.GRADE, ");
        stb.append("   T6.GRADE_CD, ");
        stb.append("   T0.HR_CLASS, ");
        stb.append("   T5.HR_NAME, ");
        stb.append("   T0.ATTENDNO, ");
        stb.append("   T0.SCHREGNO, ");
        stb.append("   T4.NAME ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T0 ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T4 ");
        stb.append("     ON T4.SCHREGNO = T0.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT T5 ");
        stb.append("     ON T5.YEAR = T0.YEAR ");
        stb.append("    AND T5.SEMESTER = T0.SEMESTER ");
        stb.append("    AND T5.GRADE = T0.GRADE ");
        stb.append("    AND T5.HR_CLASS = T0.HR_CLASS ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T6 ");
        stb.append("     ON T6.YEAR = T0.YEAR ");
        stb.append("    AND T6.GRADE = T0.GRADE ");
        stb.append(" WHERE ");
        stb.append("   T0.YEAR = '" + _param._year + "' ");
        stb.append("   AND T0.SEMESTER = '" + _param._semester + "' ");
        stb.append("   AND T0.SCHREGNO IN " + SQLUtils.whereIn(true, _param._category_selected));
        stb.append(" ORDER BY ");
        stb.append("   GRADE, HR_CLASS, ATTENDNO ");
        return stb.toString();
    }

    private void getValidationInfo(final DB2UDB db2, final Map schMap) {
        final String sql = getValidationInfoSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                if (schMap.containsKey(schregno)) {
                    final String avg = rs.getString("AVG");
                    Student setObj = (Student)schMap.get(schregno);
                    setObj._hyotei = avg;
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    private String getValidationInfoSql() {
        final StringBuffer stb = new StringBuffer();
        //過年度の範囲を特定する(SCHOOL_KINDが当年度と一致する過年度のもの)
        stb.append(" WITH PASTYEAR_CHK AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.GRADE, ");
        stb.append("   MAX(T1.YEAR) AS YEAR ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR < '2006' ");
        stb.append("   AND T2.SCHOOL_KIND = ( ");
        stb.append("                         SELECT ");
        stb.append("                           SCHOOL_KIND ");
        stb.append("                         FROM ");
        stb.append("                           SCHREG_REGD_GDAT ");
        stb.append("                         WHERE ");
        stb.append("                           YEAR = '2006' ");
        stb.append("                           AND GRADE = '06' ");
        stb.append("                        ) ");
        stb.append(" GROUP BY ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.GRADE ");
        stb.append(" ), DAT_UNI AS ( ");
        //過年度分
        stb.append(" SELECT ");
        stb.append("   SCHREGNO, ");
        stb.append("   COUNT(*) AS CNT, ");
        stb.append("   SUM(VALUATION) AS VALUATION ");
        stb.append(" FROM ");
        stb.append("   SCHREG_STUDYREC_DAT ");
        stb.append(" WHERE ");
        stb.append("   YEAR IN (SELECT YEAR FROM PASTYEAR_CHK) ");
        stb.append("   AND SCHREGNO IN ('20010001','20010002','20010003') ");
        stb.append("   AND CLASSCD < '90' ");
        stb.append(" GROUP BY ");
        stb.append("   SCHREGNO ");
        stb.append(" UNION ALL ");
        //当年度分(指定学期だけ)
        stb.append(" SELECT ");
        stb.append("   SCHREGNO, ");
        stb.append("   COUNT(*) AS CNT, ");
        stb.append("   SUM(SCORE) AS VALUATION ");
        stb.append(" FROM ");
        stb.append("   RECORD_RANK_SDIV_DAT ");
        stb.append(" WHERE ");
        stb.append("   YEAR = '2006' ");
        stb.append("   AND SEMESTER = '1' ");
        stb.append("   AND SCHREGNO IN ('20010001','20010002','20010003') ");
        stb.append("   AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '990009' ");
        stb.append("   AND SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B', '999999') ");
        stb.append("   AND CLASSCD < '90' ");
        stb.append(" GROUP BY ");
        stb.append("   SCHREGNO ");
        stb.append(" ) ");
        //集計
        stb.append(" SELECT ");
        stb.append("   SCHREGNO, ");
        stb.append("   ROUND(FLOAT(SUM(VALUATION)*1.0/SUM(CNT)*1.0),1) AS AVG ");
        stb.append(" FROM ");
        stb.append("   DAT_UNI ");
        stb.append(" GROUP BY ");
        stb.append("   SCHREGNO ");
        return stb.toString();
    }

    private void getSubclsInfo(final DB2UDB db2, final Map schMap) {
        final String sql = getSubclsInfoSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                if (schMap.containsKey(schregno)) {
                    final String classcd = rs.getString("CLASSCD");
                    final String school_Kind = rs.getString("SCHOOL_KIND");
                    final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String subclassabbv = rs.getString("SUBCLASSABBV");
                    final String credits = rs.getString("CREDITS");
                    SubclsInfo addWk = new SubclsInfo(schregno, classcd, school_Kind, curriculum_Cd, subclasscd, subclassname, subclassabbv, credits);
                    final String addKey = classcd + "-" + school_Kind + "-" + curriculum_Cd + "-" + subclasscd;
                    Student setObj = (Student)schMap.get(schregno);
                    setObj._subclsMap.put(addKey, addWk);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    private String getSubclsInfoSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH BDAT AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T3.CLASSCD, ");
        stb.append("   T3.SCHOOL_KIND, ");
        stb.append("   T3.CURRICULUM_CD, ");
        stb.append("   T3.SUBCLASSCD, ");
        stb.append("   T4.SUBCLASSNAME, ");
        stb.append("   T4.SUBCLASSABBV, ");
        stb.append("   T5.CREDITS ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   LEFT JOIN CHAIR_STD_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN CHAIR_DAT T3 ");
        stb.append("     ON T3.YEAR = T2.YEAR ");
        stb.append("    AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("    AND T3.CHAIRCD = T2.CHAIRCD ");
        stb.append("   LEFT JOIN SUBCLASS_MST T4 ");
        stb.append("     ON T4.CLASSCD = T3.CLASSCD ");
        stb.append("    AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("    AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("    AND T4.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("   LEFT JOIN CREDIT_MST T5 ");
        stb.append("     ON T5.YEAR = T1.YEAR ");
        stb.append("    AND T5.COURSECD = T1.COURSECD ");
        stb.append("    AND T5.MAJORCD = T1.MAJORCD ");
        stb.append("    AND T5.GRADE = T1.GRADE ");
        stb.append("    AND T5.COURSECODE = T1.COURSECODE ");
        stb.append("    AND T5.CLASSCD = T3.CLASSCD ");
        stb.append("    AND T5.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("    AND T5.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("    AND T5.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("   AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._category_selected));
        stb.append("   AND T3.CLASSCD < '90' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T3.* ");
        stb.append(" FROM ");
        stb.append("   BDAT T3 ");
        stb.append(" WHERE ");
        stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD NOT IN ( ");
        stb.append("     SELECT ");
        stb.append("       T2W.ATTEND_CLASSCD || '-' || T2W.ATTEND_SCHOOL_KIND || '-' || T2W.ATTEND_CURRICULUM_CD || '-' || T2W.ATTEND_SUBCLASSCD ");
        stb.append("     FROM ");
        stb.append("       SUBCLASS_REPLACE_COMBINED_DAT T2W ");
        stb.append("     WHERE ");
        stb.append("       T2W.REPLACECD = '1' AND T2W.YEAR = '" + _param._year + "' ");
        stb.append("       AND T2W.ATTEND_CLASSCD || '-' || T2W.ATTEND_SCHOOL_KIND || '-' || T2W.ATTEND_CURRICULUM_CD || '-' || T2W.ATTEND_SUBCLASSCD ");
        stb.append("           <> T2W.COMBINED_CLASSCD || '-' || T2W.COMBINED_SCHOOL_KIND || '-' || T2W.COMBINED_CURRICULUM_CD || '-' || T2W.COMBINED_SUBCLASSCD ");
        stb.append("       AND T2W.COMBINED_CLASSCD || '-' || T2W.COMBINED_SCHOOL_KIND || '-' || T2W.COMBINED_CURRICULUM_CD || '-' || T2W.COMBINED_SUBCLASSCD IN (");
        stb.append("         SELECT T2WW.CLASSCD || '-' || T2WW.SCHOOL_KIND || '-' || T2WW.CURRICULUM_CD || '-' || T2WW.SUBCLASSCD FROM BDAT T2WW ");
        stb.append("       ) ");
        stb.append("   ) ");
        stb.append(" ORDER BY ");
        stb.append("   T3.CLASSCD, ");
        stb.append("   T3.SCHOOL_KIND, ");
        stb.append("   T3.CURRICULUM_CD, ");
        stb.append("   T3.SUBCLASSCD ");
        return stb.toString();
    }

    private void getScoreInfo(final DB2UDB db2, final Map schMap) {
        final String sql = getScoreInfoSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                if (schMap.containsKey(schregno)) {
                    final String classcd = rs.getString("CLASSCD");
                    final String school_Kind = rs.getString("SCHOOL_KIND");
                    final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String score = rs.getString("SCORE");
                    final String avg = rs.getString("AVG");
                    final String classRank = rs.getString("CLASS_RANK");
                    final String gradeRank = rs.getString("GRADE_RANK");
                    ScoreInfo addWk = new ScoreInfo(schregno, classcd, school_Kind, curriculum_Cd, subclasscd, score, avg, classRank, gradeRank);
                    final String addKey = classcd + "-" + school_Kind + "-" + curriculum_Cd + "-" + subclasscd;
                    Student setObj = (Student)schMap.get(schregno);
                    setObj._scoreMap.put(addKey, addWk);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    private String getScoreInfoSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T0.SCHREGNO, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SCHOOL_KIND, ");
        stb.append("   T1.CURRICULUM_CD, ");
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   T1.SCORE, ");
        stb.append("   T1.AVG, ");
        stb.append("   T1.CLASS_RANK, ");
        stb.append("   T1.GRADE_RANK ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T0 ");
        stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T1 ");
        stb.append("     ON T1.YEAR = T0.YEAR ");
        stb.append("    AND T1.SEMESTER = T0.SEMESTER ");
        stb.append("    AND T1.SCHREGNO = T0.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   T0.YEAR = '" + _param._year + "' ");
        stb.append("   AND T0.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._test_cd + "' ");
        stb.append("   AND T0.SCHREGNO IN " + SQLUtils.whereIn(false, _param._category_selected));
        stb.append("   AND T1.SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B') ");
        return stb.toString();
    }

    private Map getAvgInfo(final DB2UDB db2) {
        Map retMap = new LinkedMap();
        final String sql = getAvgInfoSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String classcd = rs.getString("CLASSCD");
                final String school_Kind = rs.getString("SCHOOL_KIND");
                final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String avg_Div = rs.getString("AVG_DIV");
                final String avg = rs.getString("AVG");
                final String count = rs.getString("COUNT");
                AvgInfo addWk = new AvgInfo(avg_Div, classcd, school_Kind, curriculum_Cd, subclasscd, avg, count);
                final String addKey = avg_Div + ":" + classcd + "-" + school_Kind + "-" + curriculum_Cd + "-" + subclasscd;
                retMap.put(addKey, addWk);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }
    private String getAvgInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T2.CLASSCD, ");
        stb.append("   T2.SCHOOL_KIND, ");
        stb.append("   T2.CURRICULUM_CD, ");
        stb.append("   T2.SUBCLASSCD, ");
        stb.append("   T2.AVG_DIV, ");
        stb.append("   T2.AVG, ");
        stb.append("   T2.COUNT ");
        stb.append(" FROM ");
        stb.append("   RECORD_AVERAGE_SDIV_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("   T2.YEAR = '" + _param._year + "' ");
        stb.append("   AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '"+_param._test_cd+"' ");
        stb.append("   AND T2.SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B') ");
        stb.append("   AND ((T2.GRADE || T2.HR_CLASS = '" + _param._grade_hr_class + "' AND T2.AVG_DIV ='2') ");
        stb.append("        OR ");
        stb.append("        (T2.GRADE || T2.HR_CLASS = '" + _param._grade + "000' AND T2.AVG_DIV = '1')");
        stb.append("       ) ");
        return stb.toString();
    }

    private void getTestInfo(final DB2UDB db2, final Map schMap) {
        final String sql = getTestInfoSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                if (schMap.containsKey(schregno)) {
                    final String proficiencycd = rs.getString("PROFICIENCYCD");
                    final String proficiencyname1 = rs.getString("PROFICIENCYNAME1");
                    final String proficiencyname2 = rs.getString("PROFICIENCYNAME2");
                    final String proficiency_Subclass_Cd = rs.getString("PROFICIENCY_SUBCLASS_CD");
                    final String subclass_Abbv = rs.getString("SUBCLASS_ABBV");
                    final String score = rs.getString("SCORE");
                    TestInfo addWk = new TestInfo(schregno, proficiencycd, proficiencyname1, proficiencyname2, proficiency_Subclass_Cd, subclass_Abbv, score);
                    Student setObj = (Student)schMap.get(schregno);
                    final String fstKey = proficiencycd + ":" + proficiency_Subclass_Cd;
                    setObj._jituryokuTMap.put(fstKey, addWk);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    private String getTestInfoSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.PROFICIENCYCD, ");
        stb.append("     T3.PROFICIENCYNAME1, ");
        stb.append("     T3.PROFICIENCYNAME2, ");
        stb.append("     T2.PROFICIENCY_SUBCLASS_CD, ");
        stb.append("     T2.SUBCLASS_ABBV, ");
        stb.append("     T1.SCORE ");
        stb.append(" FROM ");
        stb.append("     PROFICIENCY_DAT T1 ");
        stb.append("     LEFT JOIN PROFICIENCY_SUBCLASS_MST T2 ");
        stb.append("       ON T2.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
        stb.append("     LEFT JOIN PROFICIENCY_MST T3 ");
        stb.append("       ON T3.PROFICIENCYDIV = T1.PROFICIENCYDIV ");
        stb.append("      AND T3.PROFICIENCYCD = T1.PROFICIENCYCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR     = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" +  _param._semester + "' ");
        stb.append("     AND T1.PROFICIENCYDIV = '02' ");
        stb.append("     AND T1.PROFICIENCYCD IN " + SQLUtils.whereIn(false, _param._category_selected2));
        stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(false, _param._category_selected));
        stb.append(" ORDER BY ");
        stb.append("     T1.PROFICIENCYCD, T2.PROFICIENCY_SUBCLASS_CD ");
        return stb.toString();
    }

    private void getCommitteeInfo(final DB2UDB db2, final Map schMap) {
        final String sql = getCommitteeInfoSql();
        log.debug(" committee info sql = " + sql);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                if (schMap.containsKey(schregno)) {
                    final String committee_Flg = rs.getString("COMMITTEE_FLG");
                    final String committeecd = rs.getString("COMMITTEECD");
                    final String committeename = rs.getString("COMMITTEENAME");
                    CommitteeInfo addWk = new CommitteeInfo(schregno, committee_Flg, committeecd, committeename);
                    Student setObj = (Student)schMap.get(schregno);
                    setObj._committeeMap.put(committeecd, addWk);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    private String getCommitteeInfoSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.COMMITTEE_FLG, ");
        stb.append("   T1.COMMITTEECD, ");
        stb.append("   T2.COMMITTEENAME ");
        stb.append(" FROM ");
        stb.append("   SCHREG_COMMITTEE_HIST_DAT T1 ");
        stb.append("   INNER JOIN COMMITTEE_YDAT T3 ");
        stb.append("     ON T3.COMMITTEECD = T1.COMMITTEECD ");
        stb.append("    AND T3.SCHOOLCD = T1.SCHOOLCD ");
        stb.append("    AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("    AND T3.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
        stb.append("    AND T3.YEAR = T1.YEAR ");
        stb.append("   LEFT JOIN COMMITTEE_MST T2 ");
        stb.append("     ON T2.COMMITTEECD = T3.COMMITTEECD ");
        stb.append("    AND T2.SCHOOLCD = T3.SCHOOLCD ");
        stb.append("    AND T2.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("    AND T2.COMMITTEE_FLG = T3.COMMITTEE_FLG ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER IN ('" + _param._semester + "', '" + SEMEALL + "') ");
        stb.append("   AND T1.SCHREGNO IN " + SQLUtils.whereIn(false, _param._category_selected));
        stb.append("   AND T1.COMMITTEE_FLG IN ('1', '2') ");
        stb.append(" ORDER BY ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.COMMITTEE_FLG, ");
        stb.append("   T1.COMMITTEECD ");
        return stb.toString();
    }

    private void getClubInfo(final DB2UDB db2, final Map schMap) {
        final String sql = getClubInfoSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                if (schMap.containsKey(schregno)) {
                    final String clubcd = rs.getString("CLUBCD");
                    final String clubname = rs.getString("CLUBNAME");
                    ClubInfo addWk = new ClubInfo(schregno, clubcd, clubname);
                    Student setObj = (Student)schMap.get(schregno);
                    setObj._clubMap.put(clubcd, addWk);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    private String getClubInfoSql() {
        String edate = StringUtils.replace(_param._edate, "/", "-");
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.CLUBCD, ");
        stb.append("   T2.CLUBNAME, ");
        stb.append("   T1.SDATE, ");
        stb.append("   T1.EDATE ");
        stb.append(" FROM ");
        stb.append("   SCHREG_CLUB_HIST_DAT T1 ");
        stb.append("   INNER JOIN CLUB_YDAT T3 ");
        stb.append("     ON T3.CLUBCD = T1.CLUBCD ");
        stb.append("    AND T3.SCHOOLCD = T1.SCHOOLCD ");
        stb.append("    AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("    AND T3.YEAR = '" + _param._year + "' ");
        stb.append("   LEFT JOIN CLUB_MST T2 ");
        stb.append("     ON T2.CLUBCD = T1.CLUBCD ");
        stb.append("    AND T2.SCHOOLCD = T1.SCHOOLCD ");
        stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("   T1.SCHREGNO IN " + SQLUtils.whereIn(false, _param._category_selected));
        stb.append("   AND (T1.EDATE IS NULL OR '" + edate + "' BETWEEN T1.SDATE AND T1.EDATE) ");
        stb.append(" ORDER BY ");
        stb.append(" EDATE DESC, ");
        stb.append(" SDATE, ");
        stb.append(" CLUBCD ");
        return stb.toString();
    }

    private void getAttendInfo(final DB2UDB db2, final Map schMap) {
        String edate = StringUtils.replace(_param._edate, "/", "-");
        String sdate = StringUtils.replace(_param._sdate, "/", "-");
        String sYearMonth = _param._sdate.substring(0, _param._sdate.length() - 3);
        String eYearMonth = _param._edate.substring(0, _param._edate.length() - 3);

        PreparedStatement psAtSeme = null;
        ResultSet rsAtSeme = null;
        _param._attendSemeParamMap.put("schregno", "?");
        final String attendSemesSql = AttendAccumulate.getAttendSemesSql(
                _param._year,
                _param._semester,
                sdate,
                edate,
                _param._attendSemeParamMap
        );
        log.debug(" attend semes sql = " + attendSemesSql);

        final String attendSubclassSql = getAttendInfoSql(sYearMonth, eYearMonth);
        log.debug(" attend subclass sql = " + attendSubclassSql);

        for (int cnt = 0; cnt < _param._category_selected.length; cnt++) {
            try {
                psAtSeme = db2.prepareStatement(attendSemesSql);
                DbUtils.closeQuietly(rsAtSeme);

                psAtSeme.setString(1, _param._category_selected[cnt]);
                rsAtSeme = psAtSeme.executeQuery();
                while (rsAtSeme.next()) {
                    if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                        continue;
                    }

                    final String schregno = rsAtSeme.getString("SCHREGNO");
                    if (schMap.containsKey(schregno)) {
                        final Student schInfos = (Student)schMap.get(schregno);
                        if (schInfos._Attendance == null) {
                            final Attendance attendance = new Attendance(
                                rsAtSeme.getString("LESSON"),
                                rsAtSeme.getString("MLESSON"),
                                rsAtSeme.getString("SUSPEND"),
                                rsAtSeme.getString("MOURNING"),
                                rsAtSeme.getString("SICK"),
                                "0",
                                rsAtSeme.getString("PRESENT"),
                                rsAtSeme.getString("LATE"),
                                rsAtSeme.getString("EARLY")
                            );
                            schInfos._Attendance = attendance;
                        } else {
                            schInfos._Attendance._lesson   = rsAtSeme.getString("LESSON");
                            schInfos._Attendance._mLesson  = rsAtSeme.getString("MLESSON");
                            schInfos._Attendance._suspend  = rsAtSeme.getString("SUSPEND");
                            schInfos._Attendance._mourning = rsAtSeme.getString("MOURNING");
                            schInfos._Attendance._sick     = rsAtSeme.getString("SICK");
                            schInfos._Attendance._present  = rsAtSeme.getString("PRESENT");
                            schInfos._Attendance._late     = rsAtSeme.getString("LATE");
                            schInfos._Attendance._early    = rsAtSeme.getString("EARLY");
                        }
                    }
                }
                DbUtils.closeQuietly(rsAtSeme);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(psAtSeme);
                db2.commit();
            }

            try {
                psAtSeme = db2.prepareStatement(attendSubclassSql);

                psAtSeme.setString(1, _param._category_selected[cnt]);
                rsAtSeme = psAtSeme.executeQuery();
                while (rsAtSeme.next()) {
                    if (schMap.containsKey(_param._category_selected[cnt])) {
                        final Student schInfos = (Student)schMap.get(_param._category_selected[cnt]);
                        if (schInfos._Attendance == null) {
                            final Attendance attendance = new Attendance (
                                "",
                                "",
                                "",
                                "",
                                "",
                                StringUtils.defaultString(rsAtSeme.getString("ABSENCE"), "0"),
                                "",
                                "",
                                ""
                            );
                            schInfos._Attendance = attendance;
                        } else {
                            int sourceAbsenceH = Integer.parseInt(schInfos._Attendance._absence);
                            int absenceH = Integer.parseInt(StringUtils.defaultString(rsAtSeme.getString("ABSENCE"), "0"));
                            schInfos._Attendance._absence = String.valueOf(sourceAbsenceH + absenceH);
                        }
                    }
                }
                DbUtils.closeQuietly(rsAtSeme);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(psAtSeme);
                db2.commit();
            }
        }
    }

    private String getAttendInfoSql(final String sYearMonth, final String eYearMonth) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUM(VALUE(SICK, 0) + VALUE(NOTICE, 0) + VALUE(NONOTICE, 0) + VALUE(NURSEOFF, 0) ");
        if ("1".equals(_param._knjSchoolMst._subOffDays)) {
            stb.append(                        "+ VALUE(OFFDAYS, 0) ");
        }
        if ("1".equals(_param._knjSchoolMst._subSuspend)) {
            stb.append(                        "+ VALUE(SUSPEND, 0) ");
        }
        if ("1".equals(_param._knjSchoolMst._subVirus)) {
            stb.append(                        "+ VALUE(VIRUS, 0) ");
        }
        if ("1".equals(_param._knjSchoolMst._subKoudome)) {
            stb.append(                        "+ VALUE(KOUDOME, 0) ");
        }
        if ("1".equals(_param._knjSchoolMst._subMourning)) {
            stb.append(                        "+ VALUE(MOURNING, 0) ");
        }
        if ("1".equals(_param._knjSchoolMst._subAbsent)) {
            stb.append(                        "+ VALUE(ABSENT, 0) ");
        }
        stb.append(" ) AS ABSENCE ");
        stb.append(" FROM ");
        stb.append("     ATTEND_SUBCLASS_DAT ");
        stb.append(" WHERE ");
        stb.append("         (YEAR || '/' || MONTH BETWEEN '" + sYearMonth + "' AND '" + eYearMonth + "') ");
        stb.append("     AND SEMESTER <= '" + _param._semester + "' ");
        stb.append("     AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_T + "') ");
        stb.append("     AND SCHREGNO = ? ");
        return stb.toString();
    }

    private void preStatClose(final PreparedStatement ps1) {
        try {
            ps1.close();
        } catch( Exception e ){
            log.error("preStatClose error!");
        }
    }

    /** 生徒クラス */
    private class Student {
        final String _grade;
        final String _grade_Cd;
        final String _hr_Class;
        final String _hr_Name;
        final String _attendno;
        final String _schregno;
        final String _name;

        Map _subclsMap;  //科目名称
        Map _scoreMap;  //得点
        Map _committeeMap;  //係・委員会
        Map _clubMap;  //係・委員会
        Map _jituryokuTMap;  //実力テスト
        String _hyotei;  //評定平均
        Attendance _Attendance;
        public Student (final String grade, final String grade_Cd, final String hr_Class, final String hr_Name, final String attendno, final String schregno, final String name)
        {
            _grade = grade;
            _grade_Cd = grade_Cd;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _hyotei = "";
            _subclsMap = new LinkedMap();
            _scoreMap = new LinkedMap();
            _committeeMap = new LinkedMap();
            _clubMap = new LinkedMap();
            _jituryokuTMap = new LinkedMap();
            _Attendance = null;
        }
        private String getConnectCommitteeStr() {
            String retStr = "";
            String sep = "";
            for(Iterator ite = _committeeMap.keySet().iterator();ite.hasNext();) {
                final String kStr = (String)ite.next();
                final CommitteeInfo iObj = (CommitteeInfo)_committeeMap.get(kStr);
                retStr += sep + StringUtils.defaultString(iObj._committeename, "");
                if (!"".equals(retStr)) {
                    sep = "・";
                }
            }
            return retStr;
        }
        private String getConnectClubStr() {
            String retStr = "";
            String sep = "";
            for(Iterator ite = _clubMap.keySet().iterator();ite.hasNext();) {
                final String kStr = (String)ite.next();
                final ClubInfo iObj = (ClubInfo)_clubMap.get(kStr);
                retStr += sep + StringUtils.defaultString(iObj._clubname);
                if (!"".equals(retStr)) {
                    sep = "・";
                }
            }
            return retStr;
        }
        private Map decidePrintJituryokuTest(int addMaxCnt) {
            final Map retMap = new LinkedMap();
            for (Iterator ite = _jituryokuTMap.keySet().iterator();ite.hasNext();) {
                final String kStr = (String)ite.next();
                final TestInfo decWk = (TestInfo)_jituryokuTMap.get(kStr);
                if (!retMap.containsKey(decWk._proficiency_Subclass_Cd)) {
                    retMap.put(decWk._proficiency_Subclass_Cd, decWk);
                }
                if (retMap.size() >= addMaxCnt) {
                    break;
                }
            }
            return retMap;
        }
    }

    private class SubclsInfo {
        final String _schregno;
        final String _classcd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;
        final String _credits;
        public SubclsInfo (final String schregno, final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String subclassname, final String subclassabbv, final String credits)
        {
            _schregno = schregno;
            _classcd = classcd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _credits = credits;
        }
    }

    private class ScoreInfo {
        final String _schregno;
        final String _classcd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclasscd;
        final String _score;
        final String _avg;
        final String _classRank;
        final String _gradeRank;
        public ScoreInfo (final String schregno, final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String score, final String avg, final String classRank, final String gradeRank)
        {
            _schregno = schregno;
            _classcd = classcd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _classRank = classRank;
            _gradeRank = gradeRank;
        }
        private String getAvgRound(int nScale) {
            return (new BigDecimal(_avg)).setScale(nScale, BigDecimal.ROUND_HALF_UP).toString();
        }
    }
    private class AvgInfo {
        final String _classcd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclasscd;
        final String _avg_Div;
        final String _avg;
        final String _count;
        public AvgInfo (final String avg_Div, final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String avg, final String count)
        {
            _classcd = classcd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclasscd = subclasscd;
            _avg_Div = avg_Div;
            _avg = avg;
            _count = count;
        }
        private String getAvgRound(int nScale) {
            return (new BigDecimal(_avg)).setScale(nScale, BigDecimal.ROUND_HALF_UP).toString();
        }
    }

    private class AvgSum {
        BigDecimal _avg;
        int _count;
        AvgSum() {
            _avg = new BigDecimal("0");
            _count = 0;
        }
        void addAvg(final String addVal) {
            _avg = _avg.add(new BigDecimal(addVal));
        }
        private String getAvgRound(int nScale) {
            return _avg.setScale(nScale, BigDecimal.ROUND_HALF_UP).toString();
        }
        private String getAvgAverageRound(int nScale) {
            return _count == 0 ? "0" : _avg.divide(new BigDecimal(_count), BigDecimal.ROUND_HALF_UP).setScale(nScale, BigDecimal.ROUND_HALF_UP).toString();
        }
    }

    private static class Attendance {
        String _lesson;
        String _mLesson;
        String _suspend;
        String _mourning;
        String _sick;
        String _absence;
        String _present;
        String _late;
        String _early;

        Attendance(
            final String lesson,
            final String mLesson,
            final String suspend,
            final String mourning,
            final String sick,
            final String absence,
            final String present,
            final String late,
            final String early
        ) {
            _lesson    = lesson;
            _mLesson   = mLesson;
            _suspend   = suspend;
            _mourning  = mourning;
            _sick      = sick;
            _absence   = absence;
            _present   = present;
            _late      = late;
            _early     = early;
        }

        private String getMourning() {
            return String.valueOf(Integer.parseInt(_suspend) + Integer.parseInt(_mourning));
        }
    }
    private class TestInfo {
        final String _schregno;
        final String _proficiencycd;
        final String _proficiencyname1;
        final String _proficiencyname2;
        final String _proficiency_Subclass_Cd;
        final String _subclass_Abbv;
        final String _score;
        public TestInfo (final String schregno, final String proficiencycd, final String proficiencyname1, final String proficiencyname2, final String proficiency_Subclass_Cd, final String subclass_Abbv, final String score)
        {
            _schregno = schregno;
            _proficiencycd = proficiencycd;
            _proficiencyname1 = proficiencyname1;
            _proficiencyname2 = proficiencyname2;
            _proficiency_Subclass_Cd = proficiency_Subclass_Cd;
            _subclass_Abbv = subclass_Abbv;
            _score = score;
        }
        private int findMapidx(final Map searchMap) {
            int retCnt = -1;
            for (Iterator ite = searchMap.keySet().iterator();ite.hasNext();) {
                final String kStr = (String)ite.next();
                final TestInfo srchObj = (TestInfo)searchMap.get(kStr);
                retCnt++;
                if (srchObj._proficiency_Subclass_Cd.equals(_proficiency_Subclass_Cd)) {
                    break;
                }
            }
            return retCnt;
        }
    }
    private class CommitteeInfo {
        final String _schregno;
        final String _committee_Flg;
        final String _committeecd;
        final String _committeename;
        public CommitteeInfo (final String schregno, final String committee_Flg, final String committeecd, final String committeename)
        {
            _schregno = schregno;
            _committee_Flg = committee_Flg;
            _committeecd = committeecd;
            _committeename = committeename;
        }
    }
    private class ClubInfo {
        final String _schregno;
        final String _clubcd;
        final String _clubname;
        public ClubInfo (final String schregno, final String clubcd, final String clubname)
        {
            _schregno = schregno;
            _clubcd = clubcd;
            _clubname = clubname;
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
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        int yokuNen = 0;

        private final String _year;
        private final String _ctrl_semester;
        private final String _semester;
        private final String _ctrl_date;
        private final String _grade;
        private final String _grade_hr_class;
        private final String _test_cd;
        private final String[] _category_selected;
        private final String[] _category_selected2;
        private final String _date_div;
        private final String _edate;
        private final String _setschoolkind;  //選択学年から紐づけた校種
        private final String _sdate;
        private final String _date;
        private final String _usecurriculumcd;
        private final String _seme_date;
        private final String _usevirus;
        private final String _usekekkajisu;
        private final String _usekekka;
        private final String _uselatedetail;
        private final String _usekoudome;

        private final String _certSchoolName;  //学校名
        private final String _gradeName;
        private final String _testName;
        private final String _semesterName;

        final Map _attendSemeParamMap;

        private KNJSchoolMst _knjSchoolMst;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade_hr_class = request.getParameter("GRADE_HR_CLASS");
            _grade = _grade_hr_class.substring(0,2);
            _test_cd = request.getParameter("TEST_CD");
            _category_selected = request.getParameterValues("CATEGORY_SELECTED");
            _category_selected2 = request.getParameterValues("CATEGORY_SELECTED2");
            _date_div = request.getParameter("DATE_DIV");
            _edate = request.getParameter("EDATE");

            _setschoolkind = request.getParameter("setSchoolKind");  //選択学年から紐づけた校種

            _sdate = request.getParameter("SDATE");
            _date = request.getParameter("DATE");
            _ctrl_semester = request.getParameter("CTRL_SEMESTER");
            _ctrl_date = request.getParameter("CTRL_DATE");
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _seme_date = request.getParameter("SEME_DATE");
            _usevirus = request.getParameter("useVirus");
            _usekekkajisu = request.getParameter("useKekkaJisu");
            _usekekka = request.getParameter("useKekka");
            _uselatedetail = request.getParameter("useLatedetail");
            _usekoudome = request.getParameter("useKoudome");

            _certSchoolName = getCertSchoolName(db2);
            _gradeName = getGradeName(db2);
            _testName = getTestName(db2);
            _semesterName = getSemesterName(db2);

            _attendSemeParamMap = new HashMap();
            _attendSemeParamMap.put("DB2UDB", db2);
            _attendSemeParamMap.put("HttpServletRequest", request);
            _attendSemeParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendSemeParamMap.put("useCurriculumcd", "1");
            _attendSemeParamMap.put("grade", _grade);

            final Map smParamMap = new HashMap();
            smParamMap.put("SCHOOL_KIND", _setschoolkind);
            _knjSchoolMst = new KNJSchoolMst(db2, _year, smParamMap);
        }

        private String getCertSchoolName(final DB2UDB db2) {
            final String key = "H".equals(_setschoolkind) ? "109" : "110";
            String retStr = "";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + key + "' "));
            retStr = KnjDbUtils.getString(row, "SCHOOL_NAME");
            retStr = StringUtils.defaultString(retStr);

            return retStr;
        }
        private String getGradeName(final DB2UDB db2) {
            String retStr = "";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT GRADE_NAME2 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            retStr = KnjDbUtils.getString(row, "GRADE_NAME2");
            retStr = StringUtils.defaultString(retStr);

            return retStr;
        }
        private String getSemesterName(final DB2UDB db2) {
            String retStr = "";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR='" + _year + "' AND SEMESTER = '" + _semester + "' "));
            retStr = KnjDbUtils.getString(row, "SEMESTERNAME");
            return retStr;
        }
        private String getTestName(final DB2UDB db2) {
            String retStr = "";
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.TESTITEMNAME ");
            stb.append(" FROM ");
            stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
            stb.append("     INNER JOIN ADMIN_CONTROL_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T2.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("         AND T2.CLASSCD      = '00' ");
            stb.append("         AND T2.SCHOOL_KIND  = '" + _setschoolkind + "' ");
            stb.append("         AND T2.CURRICULUM_CD  = '00' ");
            stb.append("         AND T2.SUBCLASSCD  = '000000' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR     = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _test_cd + "' ");
            stb.append("     AND T1.TESTKINDCD <> '99' ");
            stb.append("     AND T1.SCORE_DIV <> '09' ");

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));
            retStr = KnjDbUtils.getString(row, "TESTITEMNAME");
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

    /**
     * 半角数字を全角数字に変換する
     */
    private String convZenkakuToHankaku(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.setCharAt(i, (char) (c - '0' + 0xff10));
            }
        }
        return sb.toString();
    }

    /**
     * 帳票に設定する文字数が制限文字超の場合
     * 帳票設定エリアの変更を行う
     * @param area_name	帳票出力エリア
     * @param sval			値
     * @param maxVal		最大値
     * @return
     */
    private String setformatArea(String area_name, String sval, int maxVal) {

        String retAreaName = "";
        // 値がnullの場合はnullが返される
        if (sval == null) {
            return null;
        }
        // 設定値が10文字超の場合、帳票設定エリアの変更を行う
        if(maxVal >= sval.length()){
               retAreaName = area_name + "_1";
        } else {
               retAreaName = area_name + "_2";
        }
        return retAreaName;
    }
}

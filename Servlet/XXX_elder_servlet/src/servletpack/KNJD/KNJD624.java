// kanji=漢字
/*
 * $Id: 8909f7bd365280d84308b5b9180498a271c5a9b9 $
 *
 * 作成日: 2007/05/07 16:07:30 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 * 定期試験度数分布表
 * @author m-yama
 * @version $Id: 8909f7bd365280d84308b5b9180498a271c5a9b9 $
 */
public class KNJD624 {

    /**
     * 1頁辺りの最大行数
     */
    private static final int LINEMAX = 51;

    /**
     * 得点データ区切り
     */
    private static final int SCOREDIV1 = 1;
    private static final int SCOREDIV5 = 5;

    /**
     * HomeRoomによる改ページの数
     */
    private static final int CHANGE_HOMEROOM_CNT = 6;

    /**
     * 降順ソート用
     */
    private static final ReverseComparator REVERSE_COMPARATOR = new ReverseComparator();

    private static final Log log = LogFactory.getLog(KNJD624.class);

    private static final String FORM_FILE = "KNJD624.frm";

    private static final String SUBCLASS_3 = "00-0-0-333333";
    private static final String SUBCLASS_5 = "00-0-0-555555";
    private static final String SUBCLASS_ALL = "00-0-0-999999";
    
    private static final String DIV_SMALL = "Small";
    private static final String DIV_ALL = "All";
    private static final String DIV_SCORE = "SCORE";
    
    private static final String SEX_M = "1";
    private static final String SEX_L = "2";
    private static final String SEX_T = "9";

    Param _param;

    /**
     * KNJD.classから呼ばれる処理。
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
        final HttpServletRequest request,
        final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();    //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        try {
            init(response, svf);

            // DB接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);  // パラメータ作成

            // 印字メイン
            boolean hasData = false;   // 該当データ無しフラグ
            hasData = printMain(db2, svf);
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(svf, db2);
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
        log.debug("学校種別=" + param._schooldiv + " テスト=" + param._testname + " 日付=" + param._date + " Form-File=" + FORM_FILE);
    }

    private void init(final HttpServletResponse response, final Vrw32alp svf) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final Vrw32alp svf, final DB2UDB db2) {
        if (null != svf) {
            svf.VrQuit();
        }
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private String getTestName(
            final DB2UDB db2,
            final String year,
            final String semester,
            final String testcd
    ) throws Exception {
        String rtnVal = "";
        final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW WHERE YEAR = '" + year + "'"
            + " AND SEMESTER = '" + semester + "' AND TESTKINDCD || TESTITEMCD = '" + testcd + "'";

        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            rs.next();
            rtnVal = rs.getString("TESTITEMNAME");
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnVal;
    }

    private String getSemesterName(
            final DB2UDB db2,
            final String year,
            final String semester
    ) throws Exception {
        String rtnVal = "";

        if ("9".equals(semester)) {
            return rtnVal;
        }

        final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "'";
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            rs.next();
            rtnVal = rs.getString("SEMESTERNAME");
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnVal;
    }

    /** 印刷処理メイン */
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws Exception {
        boolean rtnflg = false;

        // データ取得
        final List subclassesData = new ArrayList();
        for (int i = 0; i < _param._subclassCd.length; i++) {
            final SubClass subclass = createSubClass(db2, _param._subclassCd[i]);
            subclassesData.add(subclass);

            rtnflg = true;
        }

        // データ出力
        printOut(svf, subclassesData);

        return rtnflg;
    }

    /** 出力処理 */
    private void printOut(final Vrw32alp svf, final List subclassesData) {
        
        /** ブロック番号 1:左側 2:右側 */
        int _blockNum;
        /** 列番号 */
        int _rowNum;
        /** 行番号 */
        int _lineNum;

        _blockNum = 1;
        int i = 1;
        for (final Iterator it = subclassesData.iterator(); it.hasNext();) {
            _rowNum = 1;
            _lineNum = 1;
            final SubClass subclass = (SubClass) it.next();
            log.debug(" SubClass = " + subclass);
            
            if (_blockNum == 1) {
                setHead(svf, subclass);
            }

            int homeCnt = 1;
            for (final Iterator ithr = _param._homeroom.iterator(); ithr.hasNext();) {
                final HomeRoom homeroom = (HomeRoom) ithr.next();
                _lineNum = 1;
                if (homeCnt == CHANGE_HOMEROOM_CNT + 1) {
                    _blockNum++;
                    _rowNum = 1;
                    _lineNum = 1;
                    homeCnt = 1;
                }

                if (isGakunenmatu() && _blockNum == 2) {
                    _blockNum = 1;
                }

                if (_blockNum == 3) {
                    svf.VrEndPage();
                    _blockNum = 1;
                    setHead(svf, subclass);
                    _rowNum = 1;
                }

                if (_lineNum == 1) {
                    printOther(svf, subclass, homeroom, _blockNum, _rowNum, _lineNum);
                }

                final List sroreSorted = new ArrayList(subclass._distribute.keySet());
                Collections.sort(sroreSorted, REVERSE_COMPARATOR);

                for (final Iterator itss = sroreSorted.iterator(); itss.hasNext();) {
                    final Integer keyScore = (Integer) itss.next();

                    printDistribute(svf, keyScore, subclass, homeroom, _blockNum, _rowNum, _lineNum);
                    
                    _lineNum++;
                    if (_lineNum - 1 == LINEMAX) {
                        _lineNum = 1;
                        _blockNum++;
                        printOther(svf, subclass, homeroom, _blockNum, _rowNum, _lineNum);
                    }
                }
                homeCnt++;
                _rowNum++;
            }

            if (log.isDebugEnabled()) {
                debugData(subclass);
            }

            if ((isGakunenmatu() || _blockNum == 2) && i < subclassesData.size()) {
                svf.VrEndPage();
                _blockNum = 1;
            } else {
                _blockNum++;
            }
            i++;
        }
        svf.VrEndPage();
    }

    private void debugData(final SubClass subclass) {
        log.debug(subclass._name + subclass._avg);

        final List totalSorted = new ArrayList(subclass._total.keySet());
        Collections.sort(totalSorted);
        for (final Iterator it = totalSorted.iterator(); it.hasNext();) {
            final HomeRoom subHr = (HomeRoom) it.next();
            final DistributeValue subDis = (DistributeValue) subclass._total.get(subHr);
            log.debug(subHr.toString() + ":" + subDis);
        }

        final List scoreSorted = new ArrayList(subclass._distribute.keySet());
        Collections.sort(scoreSorted, REVERSE_COMPARATOR);

        for (final Iterator it = scoreSorted.iterator(); it.hasNext();) {
            final Integer keyScore = (Integer) it.next();
            final OutputData subOut = (OutputData) subclass._distribute.get(keyScore);
            log.debug(keyScore + "点:順位" + subOut._rank + ":小計" + subOut._distTotalSmall + "累計:" + subOut._distTotalAll);

            final List homeRoomSorted = new ArrayList(subOut._hrValue.keySet());
            Collections.sort(homeRoomSorted);

            for (final Iterator itout = homeRoomSorted.iterator(); itout.hasNext();) {
                final HomeRoom keyHome = (HomeRoom) itout.next();
                final DistributeValue subOutDis = (DistributeValue) subOut._hrValue.get(keyHome);
                log.debug(keyHome.toString() + ":" + subOutDis);
            }
        }
    }

    /** ヘッダデータセット */
    private void setHead(final Vrw32alp svf, final SubClass subclass) {
        svf.VrSetForm(FORM_FILE, 1);
        svf.VrsOut("SCHOOLDIV", _param._schooldiv);
        svf.VrsOut("YEAR", _param._year);
        svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(_param._grade)));
        svf.VrsOut("SEMESTER", _param._semesterName);
        svf.VrsOut("TITLE", _param._testname);
        svf.VrsOut("DATE", _param._date);
    }

    /** 得点データ印字 */
    private void printDistribute(
            final Vrw32alp svf,
            final Integer keyScore,
            final SubClass subclass,
            final HomeRoom homeroom,
            final int _blockNum,
            final int _rowNum,
            final int _lineNum
    ) {

        final OutputData outScore = (OutputData) subclass._distribute.get(keyScore);
        final DistributeValue distSmall = (DistributeValue) outScore._distTotalSmall;
        final DistributeValue distAll = (DistributeValue) outScore._distTotalAll;
        final DistributeValue distScore = (DistributeValue) outScore._hrValue.get(homeroom);

        // 得点
        if (isGakunenmatu() || keyScore.intValue() == 100) {
            svf.VrsOutn("SCORE" + _blockNum, _lineNum, keyScore.toString());
        } else {
            final int scoreRange = keyScore.intValue() + _param._scoreRange;
            svf.VrsOutn("SCORERANGE" + _blockNum, _lineNum, keyScore.toString() + ":" + scoreRange);
        }

        // 明細
        String lineSeq = _blockNum + "_" + _rowNum;
        printLineData(svf, lineSeq, _lineNum, distScore);

        // 小計
        lineSeq = _blockNum + "_S";
        printLineData(svf, lineSeq, _lineNum, distSmall);

        // 累計
        lineSeq = _blockNum + "_T";
        printLineData(svf, lineSeq, _lineNum, distAll);

        // 席次
        if (isGakunenmatu() && !isSubclassAll(subclass._subclassCd)) {
            final String rank = (outScore._rank == 0) ? "" : String.valueOf(outScore._rank);
            svf.VrsOutn("RANK" + _blockNum, _lineNum, rank);
        }
    }

    /** 明細一行分印字 */
    private void printLineData(
            final Vrw32alp svf,
            final String lineSeq,
            final int _lineNum,
            final DistributeValue distScore
    ) {
        printLineData(svf, lineSeq, _lineNum, distScore, "");
    }

    private void printLineData(
            final Vrw32alp svf,
            final String lineSeq,
            final int _lineNum,
            final DistributeValue distScore,
            final String frontField
    ) {
        final String mcnt = String.valueOf(distScore._mcnt).equals("0") ? "" : String.valueOf(distScore._mcnt);
        final String lcnt = String.valueOf(distScore._lcnt).equals("0") ? "" : String.valueOf(distScore._lcnt);
        final String tcnt = String.valueOf(distScore._tcnt).equals("0") ? "" : String.valueOf(distScore._tcnt);
        if (frontField.equals("")) {
            svf.VrsOutn(frontField + "MCNT" + lineSeq, _lineNum, mcnt);
            svf.VrsOutn(frontField + "LCNT" + lineSeq, _lineNum, lcnt);
            svf.VrsOutn(frontField + "TCNT" + lineSeq, _lineNum, tcnt);
        } else {
            svf.VrsOut(frontField + "MCNT" + lineSeq, mcnt);
            svf.VrsOut(frontField + "LCNT" + lineSeq, lcnt);
            svf.VrsOut(frontField + "TCNT" + lineSeq, tcnt);
        }
    }

    /** 得点データ以外の明細印字 */
    private void printOther(
            final Vrw32alp svf,
            final SubClass subclass,
            final HomeRoom homeroom,
            final int _blockNum,
            final int _rowNum,
            final int _lineNum
    ) {
        final String fieldIndex = _blockNum + "_" + _rowNum;

        svf.VrsOut("SUBCLASSNAME" + _blockNum, subclass._name);
        svf.VrsOut("HR_NAMEABBV" + fieldIndex, homeroom._nameabbv);

        if (!isGakunenmatu() || _blockNum == 2) {
            final DistributeValue distributeValue = (DistributeValue) subclass._total.get(homeroom);

            printLineData(svf, fieldIndex, _lineNum, distributeValue, "TOTAL_");
            svf.VrsOut("AVERAGE" + _blockNum, String.valueOf(subclass._avg));
        }
    }

    /** HomeRoom作成 */
    private List createHomeRooms(
            final DB2UDB db2,
            final String year,
            final String semester,
            final String grade
    ) throws Exception {
        final List rtn = new ArrayList();
        final String sql = "SELECT HR_CLASS,HR_NAMEABBV FROM SCHREG_REGD_HDAT WHERE YEAR = '" + year + "'"
                + " AND SEMESTER = '" + semester + "' AND GRADE = '" + grade + "'";
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String hrnameabbv = rs.getString("HR_NAMEABBV");
                final String hrclass = rs.getString("HR_CLASS");
                final HomeRoom hr = new HomeRoom(grade, hrclass, hrnameabbv);
                rtn.add(hr);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtn;
    }

    /** Homeroomの得点単位 DistributeValueを返す */
    private DistributeValue createDistributeValue(
            final HomeRoom homeroom,
            final String subcd,
            final int score,
            final HomeRoomScoreCnt homeRoomScore
    ) {
        final DistributeValue rtndistVal = new DistributeValue();
        int mcnt = 0;
        int lcnt = 0;
        int tcnt = 0;

        if (!isGakunenmatu() && score + _param._scoreRange < 100) {
            for (int i = score; i <= score + _param._scoreRange; i++) {
                final Integer seq = new Integer(i);
                mcnt += Integer.parseInt((null == homeRoomScore._mcnt.get(seq)) ? "0" : (String) homeRoomScore._mcnt.get(seq));
                lcnt += Integer.parseInt((null == homeRoomScore._lcnt.get(seq)) ? "0" : (String) homeRoomScore._lcnt.get(seq));
                tcnt += Integer.parseInt((null == homeRoomScore._tcnt.get(seq)) ? "0" : (String) homeRoomScore._tcnt.get(seq));
            }
        } else {
            final Integer seq = new Integer(score);
            mcnt = Integer.parseInt((null == homeRoomScore._mcnt.get(seq)) ? "0" : (String) homeRoomScore._mcnt.get(seq));
            lcnt = Integer.parseInt((null == homeRoomScore._lcnt.get(seq)) ? "0" : (String) homeRoomScore._lcnt.get(seq));
            tcnt = Integer.parseInt((null == homeRoomScore._tcnt.get(seq)) ? "0" : (String) homeRoomScore._tcnt.get(seq));
        }
        rtndistVal.set(mcnt, lcnt, tcnt);
        return rtndistVal;
    }

    /** SQLの結果取得 */
    private Map getCountValueMap(
            final DB2UDB db2,
            final String distValSql
    ) throws Exception {
        final Map map = new HashMap();
        db2.query(distValSql);
        final ResultSet rs = db2.getResultSet();
        try {
            while(rs.next()) {
                final Integer score = new Integer(rs.getInt("SCORE"));
                final Integer cnt = new Integer(rs.getInt("CNT"));
                map.put(score, cnt);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }

        return map;
    }

    /** SQLの結果取得 */
    private DistributeValue getCountValue2(
            final DB2UDB db2,
            final String distValSql
    ) throws Exception {
        int mcnt = 0;
        int lcnt = 0;
        int tcnt = 0;
        db2.query(distValSql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String sex = rs.getString("SEX");
                if (SEX_M.equals(sex)) {
                    mcnt = rs.getInt("CNT");
                } else if (SEX_L.equals(sex)) {
                    lcnt = rs.getInt("CNT");
                } else if (SEX_T.equals(sex)) {
                    tcnt = rs.getInt("CNT");
                }
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        final DistributeValue distributeValue = new DistributeValue();
        distributeValue.set(mcnt, lcnt, tcnt);
        return distributeValue;
    }

    /** SQLの結果取得 */
    private DistributeValue getCountValue3(
            final DB2UDB db2,
            final PreparedStatement ps
    ) throws Exception {
        int mcnt = 0;
        int lcnt = 0;
        int tcnt = 0;
        final ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                final String sex = rs.getString("SEX");
                if (SEX_M.equals(sex)) {
                    mcnt = rs.getInt("CNT");
                } else if (SEX_L.equals(sex)) {
                    lcnt = rs.getInt("CNT");
                } else if (SEX_T.equals(sex)) {
                    tcnt = rs.getInt("CNT");
                }
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        final DistributeValue distributeValue = new DistributeValue();
        distributeValue.set(mcnt, lcnt, tcnt);
        return distributeValue;
    }

    /** 席次データの取得 */
    private String getRankValSql(
            final String subcd
    ) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SCORE, MAX(GRADE_RANK) AS CNT ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND TESTKINDCD || TESTITEMCD = '" + _param._testcd + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' ||  SUBCLASSCD = '" + subcd + "' ");
        } else {
            stb.append("     AND SUBCLASSCD = '" + subcd + "' ");
        }
        stb.append("     AND SCHREGNO IN (SELECT ");
        stb.append("                          T1.SCHREGNO ");
        stb.append("                      FROM ");
        stb.append("                          SCHREG_REGD_DAT T1 ");
        stb.append("                      WHERE ");
        stb.append("                          T1.YEAR = '" + _param._year + "' ");
        stb.append("                          AND T1.SEMESTER = '" + _param.getSemester() + "' ");
        stb.append("                          AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("                     ) ");
        stb.append(" GROUP BY ");
        stb.append("     SCORE ");

        return stb.toString();
    }

    /** Homeroom単位のデータを取得 */
    // CSOFF: ExecutableStatementCount
    private String getDistValSql(
            final HomeRoom homeroom,
            final String subcd,
            final String div
    ) {

        final String field = (isSubclassAll(subcd)) ? "AVG" : "SCORE";

        final StringBuffer stb = new StringBuffer();
        stb.append("     WITH SCHREGNOS AS (SELECT ");
        stb.append("                          T1.SCHREGNO, T2.SEX ");
        stb.append("                      FROM ");
        stb.append("                          SCHREG_REGD_DAT T1, ");
        stb.append("                          SCHREG_BASE_MST T2 ");
        stb.append("                      WHERE ");
        stb.append("                          T1.YEAR = '" + _param._year + "' ");
        stb.append("                          AND T1.SEMESTER = '" + _param.getSemester() + "' ");
        stb.append("                          AND T1.GRADE = '" + homeroom._grade + "' ");
        stb.append("                          AND T1.HR_CLASS = '" + homeroom._room + "' ");
        stb.append("                          AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("                     ) ");
        stb.append(" SELECT ");
        stb.append("     VALUE(T2.SEX, '9') AS SEX, ");
        if (div.equals(DIV_SCORE)) {
            stb.append("     " + field + ", ");
        }
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT T1, SCHREGNOS T2 ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND TESTKINDCD || TESTITEMCD = '" + _param._testcd + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '" + subcd + "' ");
        } else {
            stb.append("     AND SUBCLASSCD = '" + subcd + "' ");
        }
        if (div.equals(DIV_ALL)) {
            stb.append("     AND " + field + " BETWEEN 0 AND 100 ");
        }
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" GROUP BY ");
        if (div.equals(DIV_SCORE)) {
            stb.append("  GROUPING SETS((SEX, " + field + "), (" + field + ")) ");
        } else {
            stb.append("  GROUPING SETS((SEX), ()) ");
        }
        stb.append(" ORDER BY ");
        stb.append("     SEX ");
        if (div.equals(DIV_SCORE)) {
            stb.append("  ," + field + " ");
        }

        return stb.toString();
    }
    // CSON: ExecutableStatementCount

    /** 得点毎の小計、累計データの取得 */
    // CSOFF: ExecutableStatementCount
    private String getDistValSql(
            final String div,
            final int score,
            final String subcd
    ) {

        final String field = (isSubclassAll(subcd)) ? "AVG " : "SCORE";

        double score1 = score;
        double score2 = score;
        double score3 = score;

        if (isSubclassAll(subcd)) {
            if (!isGakunenmatu() && score + _param._scoreRange < 100) {
                score2 = score + _param._scoreRange + 1;
            } else {
                score2 = score + 1;
            }
        }

        final StringBuffer stb = new StringBuffer();
        stb.append("     WITH SCHREGNOS AS (SELECT ");
        stb.append("                          T1.SCHREGNO, T2.SEX ");
        stb.append("                      FROM ");
        stb.append("                          SCHREG_REGD_DAT T1, ");
        stb.append("                          SCHREG_BASE_MST T2 ");
        stb.append("                      WHERE ");
        stb.append("                          T1.YEAR = '" + _param._year + "' ");
        stb.append("                          AND T1.SEMESTER = '" + _param.getSemester() + "' ");
        stb.append("                          AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("                          AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("                     ) ");
        stb.append(" , SCORES AS ( SELECT ");
        stb.append("     T2.SEX, ");
        stb.append("     " + field + " AS SCORE ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT T1, SCHREGNOS T2 ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND TESTKINDCD || TESTITEMCD = '" + _param._testcd + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '" + subcd + "' ");
        } else {
            stb.append("     AND SUBCLASSCD = '" + subcd + "' ");
        }
        if (div.equals(DIV_SMALL)) {
            if (isSubclassAll(subcd)) {
                stb.append("     AND " + field + " >= " + score1 + " ");
                stb.append("     AND " + field + " < " + score2 + " ");
            } else {
                if (!isGakunenmatu() && score + _param._scoreRange < 100) {
                    stb.append("     AND " + field + " BETWEEN " + score + " AND " + (score + _param._scoreRange) + " ");
                } else {
                    stb.append("     AND " + field + " = " + score + " ");
                }
            }
        } else {
            stb.append("     AND " + field + " >= " + score3 + " ");
        }
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     VALUE(T1.SEX, '9') AS SEX, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     SCORES T1 ");
        stb.append(" GROUP BY ");
        stb.append("  GROUPING SETS((SEX), ()) ");
        stb.append(" ORDER BY ");
        stb.append("     SEX ");
        return stb.toString();
    }
    // CSON: ExecutableStatementCount

    private boolean isGakunenmatu() {
        return _param._testcd.equals("9900");
    }

    private boolean isSubclassAll(final String subcd) {
        return SUBCLASS_3.equals(subcd) || SUBCLASS_5.equals(subcd) || SUBCLASS_ALL.equals(subcd);
    }

    /** 科目単位の印字データ */
    private SubClass createSubClass(
            final DB2UDB db2,
            final String subcd
    ) throws Exception {
        final int scoreMemory;
        if (!isGakunenmatu()) {
            scoreMemory = SCOREDIV5;
        } else {
            scoreMemory = SCOREDIV1;
        }
        final SubClass rtnSubClass = new SubClass(db2, subcd, scoreMemory);
        return rtnSubClass;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _semesterName;
        private final String _ctrlSemester;
        private final String _grade;
        private final String _testcd;
        private final String _schooldiv;
        private final String[] _subclassCd;
        private final String _testname;
        private final String _date;
        private final List _homeroom;
        private final int _scoreRange;
        private final String _useCurriculumcd;
        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTCD");
            _subclassCd = request.getParameterValues("CATEGORY_SELECTED");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            if (3 >= Integer.parseInt(_grade)) {
                _schooldiv = "中学";
            } else {
                _schooldiv = "高校";
            }

            _scoreRange = SCOREDIV5 - 1;

            // DBより取得
            _semesterName = getSemesterName(db2, _year, _semester);

            final KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            _date = KNJ_EditDate.h_format_US_Y(returnval.val3) + "年" + KNJ_EditDate.h_format_JP_MD(returnval.val3);

            _testname = getTestName(db2, _year, _semester, _testcd);

            _homeroom = createHomeRooms(db2, _year, getSemester(), _grade);
            Collections.sort(_homeroom);
        }

        public String getSemester() {
            return _semester.equals("9") ? _ctrlSemester : _semester;
        }
    }

    /** 科目単位の印字データ */
    private class SubClass {
        private final String _name;
        private final String _subclassCd;
        private Map _distribute = new HashMap();
        private Map _total = new HashMap();
        private double _avg;
        SubClass(
                final DB2UDB db2,
                final String subcd,
                final int scoreMemory
        ) throws Exception {
            _subclassCd = subcd;
            _name = getSubclassName(db2, subcd);
            _avg = getAvg(db2, subcd);
            final Map homeRoomScore = new HashMap();

            for (final Iterator it = _param._homeroom.iterator(); it.hasNext();) {
                final HomeRoom homeroom = (HomeRoom) it.next();
                _total.put(homeroom, getCountValue2(db2, getDistValSql(homeroom, subcd, DIV_ALL)));

                homeRoomScore.put(homeroom, getCnt(db2, subcd, homeroom, getDistValSql(homeroom, subcd, DIV_SCORE)));
            }

            final Map rankMap = getCountValueMap(db2, getRankValSql(subcd));

            for (int score = 0; score <= 100; score += scoreMemory) {
                _distribute.put(new Integer(score), new OutputData(db2, subcd, score, rankMap, homeRoomScore));
            }
        }

        /**
         * 科目名称取得
         */
        private String getSubclassName(
                final DB2UDB db2,
                final String subcd
        ) throws Exception {
            String rtnVal = "";
            if (isSubclassAll(subcd)) {
                rtnVal = getAllSubclassName(subcd);
            } else {
                String sql = "SELECT SUBCLASSNAME FROM SUBCLASS_MST WHERE SUBCLASSCD = '" + subcd + "'";
                if ("1".equals(_param._useCurriculumcd)) {
                    sql = "SELECT SUBCLASSNAME FROM SUBCLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '" + subcd + "'";
                }
log.debug(sql);
                db2.query(sql);
                final ResultSet rs = db2.getResultSet();
                try {
                    rs.next();
                    rtnVal = rs.getString("SUBCLASSNAME");
                } finally {
                    DbUtils.closeQuietly(rs);
                    db2.commit();
                }
            }
            return rtnVal;
        }

        private String getAllSubclassName(final String subcd) {
            final String rtn;
            if (SUBCLASS_3.equals(subcd)) {
                rtn = "平均点(３科目)";
            } else if (SUBCLASS_5.equals(subcd)) {
                rtn = "平均点(５科目)";
            } else {
                rtn = "平均点(全科目)";
            }
            return rtn;
        }

        /**
         * 平均点取得
         */
        private double getAvg(
                final DB2UDB db2,
                final String subcd
        ) throws Exception {
            double rtnVal = 0;

            db2.query(createAvgSql(subcd));
            final ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    rtnVal = KNJServletUtils.roundHalfUp(rs.getDouble("AVG"), 1);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnVal;
        }

        /** 平均点取得SQL */
        private String createAvgSql(final String subcd) {
            String rtn;
            rtn = " SELECT "
                    + "     AVG"
                    + " FROM"
                    + "     RECORD_AVERAGE_DAT"
                    + " WHERE"
                    + "     YEAR = '" + _param._year + "'"
                    + "     AND SEMESTER = '" + _param._semester + "'"
                    + "     AND TESTKINDCD || TESTITEMCD = '" + _param._testcd + "'";
                    if ("1".equals(_param._useCurriculumcd)) {
                        rtn = rtn + "     AND CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '" + subcd + "'";
                    } else {
                        rtn = rtn + "     AND SUBCLASSCD = '" + subcd + "'";
                    }
            rtn = rtn + "     AND AVG_DIV = '1'"
                    + "     AND GRADE = '" + _param._grade + "'";
            return rtn;
        }
        
        public String toString() {
            return "SubClass(" + _subclassCd + ":" + _name + ")";
        }
    }
    
    private HomeRoomScoreCnt getCnt(
            final DB2UDB db2,
            final String subcd,
            final HomeRoom homeroom,
            final String sql
    ) throws Exception {

        final Map cntMap = new HashMap();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                if (null == cntMap.get(rs.getString("SEX"))) {
                    cntMap.put(rs.getString("SEX"), new HashMap());
                }
                final Map cnt = (Map) cntMap.get(rs.getString("SEX"));
                
                if (isSubclassAll(subcd)) {
                    final BigDecimal bd = new BigDecimal(rs.getString("AVG"));
                    final Integer key = new Integer(bd.setScale(0, BigDecimal.ROUND_DOWN).intValue());

                    int addCnt = rs.getInt("CNT");
                    if (null != cnt.get(key)) {
                        addCnt += Integer.parseInt((String) cnt.get(key));
                    }
                    cnt.put(key, String.valueOf(addCnt));

                } else {
                    cnt.put(new Integer(rs.getInt("SCORE")), rs.getString("CNT"));
                }
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        final Map mcntMap = null == cntMap.get(SEX_M) ? new HashMap() : (Map) cntMap.get(SEX_M);
        final Map lcntMap = null == cntMap.get(SEX_L) ? new HashMap() : (Map) cntMap.get(SEX_L);
        final Map tcntMap = null == cntMap.get(SEX_T) ? new HashMap() : (Map) cntMap.get(SEX_T);
        return new HomeRoomScoreCnt(mcntMap, lcntMap, tcntMap);
    }

    /** HOMEROOM単位の得点単位のデータ */
    private class HomeRoomScoreCnt {
        private final Map _mcnt;
        private final Map _lcnt;
        private final Map _tcnt;
        HomeRoomScoreCnt(
                final Map mcnt,
                final Map lcnt,
                final Map tcnt
        ) throws Exception {
            _mcnt = mcnt;
            _lcnt = lcnt;
            _tcnt = tcnt;
        }
    }

    /** 得点単位の出力データ */
    private class OutputData {
        private final int _rank;
        private final DistributeValue _distTotalSmall;
        private final DistributeValue _distTotalAll;
        private Map _hrValue = new HashMap();

        OutputData(
                final DB2UDB db2,
                final String subcd,
                final int score,
                final Map rankMap,
                final Map homeRoomScore
        ) throws Exception {
            int rank = 0;
            final Integer iScore = new Integer(score);
            if (null != rankMap.get(iScore)) {
                rank = ((Integer) rankMap.get(iScore)).intValue();
            }
            _rank = rank;

            _distTotalSmall = getCountValue2(db2, getDistValSql(DIV_SMALL, score, subcd));
            _distTotalAll = getCountValue2(db2, getDistValSql(DIV_ALL, score, subcd));
            _hrValue = new HashMap();
            for (final Iterator it = _param._homeroom.iterator(); it.hasNext();) {
                final HomeRoom homeroom = (HomeRoom) it.next();

                final HomeRoomScoreCnt homeRoomScoreCnt = (HomeRoomScoreCnt) homeRoomScore.get(homeroom);
                final DistributeValue v = createDistributeValue(homeroom, subcd, score, homeRoomScoreCnt);
                _hrValue.put(homeroom, v);
            }
        }
    }

    /** 年組データ */
    private class HomeRoom implements Comparable {
        private final String _grade;
        private final String _room;
        private final String _nameabbv;

        HomeRoom(
                final String grade,
                final String room,
                final String name
        ) {
            _grade = grade;
            _room = room;
            _nameabbv = name;
        }
        public String toString() {
            return _grade + "-" + _room + ":" + _nameabbv;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof HomeRoom)) {
                return -1;
            }
            final HomeRoom that = (HomeRoom) o;

            // 学年
            if (!this._grade.equals(that._grade)) {
                return this._grade.compareTo(that._grade);
            }

            // 組（学年が同じだった場合）
            return this._room.compareTo(that._room);
        }
    }

    /** データの最小単位 */
    private class DistributeValue {
        private int _mcnt;
        private int _lcnt;
        private int _tcnt;
        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _mcnt + "/" + _lcnt + "/" + _tcnt;
        }
        public void set(final int mcnt, final int lcnt, final int tcnt) {
            _mcnt = mcnt;
            _lcnt = lcnt;
            _tcnt = tcnt;
        }
    }
}

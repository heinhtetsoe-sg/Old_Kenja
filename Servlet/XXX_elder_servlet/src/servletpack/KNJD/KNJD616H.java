/*
 * $Id: 128a69d8c66687d7f655512f5fa669a530570a62 $
 *
 * 作成日: 2018/05/22
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJD616H {

    private static final Log log = LogFactory.getLog(KNJD616H.class);

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
        final int maxLine = 50;

        final Map subClassMap     = getSubclassInfoMap(db2);    // 科目リスト
        final Map schSubClassMap  = getSchSubclassInfoMap(db2); // 生徒の評価データ（科目ごと）
        final List schInfoListAll = getSchList(db2);            //対象リスト
        final List setListList    = getStudentListList(schInfoListAll, maxLine);

        for (Iterator it0 = setListList.iterator(); it0.hasNext();) {
            List setSchInfoList = (List) it0.next();

            svf.VrSetForm("KNJD616H.frm", 4);
            int line = 1;
            final Map setLineMap = new TreeMap();

            final String setStrDiv = ("1".equals(_param._dataDiv)) ? "成績優秀者　": "成績不振者　";
            final String setTitle  = "成績評価集計　" + setStrDiv + _param._gradeName + "　" + _param._semeName + "評価";
            svf.VrsOut("year2", _param._ctrlYear + "年度"); // 年度
            svf.VrsOut("TITLE", setTitle);                  // タイトル
            svf.VrsOut("ymd1", KNJ_EditDate.h_format_thi(_param._ctrlDate, 0)); // 作成日付 TODO ローカルで実行するとPDFが壊れる（サーバー上は問題なし）

            for (Iterator iterator = setSchInfoList.iterator(); iterator.hasNext();) {
                final SchInfo schInfo = (SchInfo) iterator.next();

                final String setHrNameAttNo = schInfo._hrName + schInfo._attendNo;
                svf.VrsOutn("HR_NAME",   line, setHrNameAttNo);     // 年組番
                svf.VrsOut("name"+line,  schInfo._name);            // 氏名

                svf.VrsOut("TOTAL"+line,     schInfo._totalScore);  // 総合
                svf.VrsOut("AVERAGE"+line,   schInfo._totalAvg);    // (AVG)
                svf.VrsOut("TOTAL_AVERAGE", _param._gradeAvg);      // 学年平均

                final SchScoreCnt scoreCnt = (SchScoreCnt) schSubClassMap.get(schInfo._schregNo + "-CNT");
                svf.VrsOut("VAL5_"+line,     scoreCnt._cntScore5);  // 5
                svf.VrsOut("VAL4_"+line,     scoreCnt._cntScore4);  // 4
                svf.VrsOut("VAL3_"+line,     scoreCnt._cntScore3);  // 3
                svf.VrsOut("VAL2_"+line,     scoreCnt._cntScore2);  // 2
                svf.VrsOut("VAL1_"+line,     scoreCnt._cntScore1);  // 1
                svf.VrsOut("DORMITORY"+line, schInfo._domitory);    // 寮生
                svf.VrsOut("RECOMMEND"+line, schInfo._suisen);      // 推薦
                svf.VrsOut("OVERSEA"+line,   schInfo._kaigai);      // 海外
                svf.VrsOut("IB"+line,        schInfo._ib);          // IB
                svf.VrsOut("METHOD"+line,    schInfo._aHousiki);    // A方式

                setLineMap.put(String.valueOf(line), schInfo._schregNo);
                line++;
            }

            //レコード部分
            for (Iterator it1 = subClassMap.keySet().iterator(); it1.hasNext();) {
                String subClassCd = (String) it1.next();

                final String subClassNameAvg = (String) subClassMap.get(subClassCd);
                final String[] nameAvgArr = StringUtils.split(subClassNameAvg, "-AVG-");
                final String subClassName = nameAvgArr[0];
                final String subClassAvg = nameAvgArr[1];
                final String setSubjectField = KNJ_EditEdit.getMS932ByteLength(subClassName) > 4 ? "2": "1";
                svf.VrsOut("subject"+setSubjectField, subClassName);                      // 科目名称
                svf.VrsOut("AVE_CLASS", (!"null".equals(subClassAvg)) ? subClassAvg: ""); // 科目平均

                for (Iterator itLineMap = setLineMap.keySet().iterator(); itLineMap.hasNext();) {
                    String seLine = (String) itLineMap.next();

                    final String setSchNo = (String) setLineMap.get(seLine);
                    final String setScore = (String) schSubClassMap.get(setSchNo + "-" + subClassCd);
                    svf.VrsOut("SCORE"+seLine, setScore); // 評価
                }

                svf.VrEndRecord();
                _hasData = true;
            }

        }
    }

    /**
     * ListのListを作成、maxLineで区切ってリストに格納
     * @param students
     * @param maxLine
     * @return
     */
    private static List getStudentListList(final List students, final int maxLine) {
        final List rtn = new ArrayList();
        List current = null;
        int line = 0;
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final SchInfo student = (SchInfo) it.next();
            if (null == current || line >= maxLine) {
                line = 0;
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(student);
            line++;
        }
        return rtn;
    }

    private Map getSubclassInfoMap(final DB2UDB db2) {
        final Map retMap = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSubclassSql(true);
            log.debug(" SubClassSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String subClassCd = rs.getString("CLASSCD") + rs.getString("SCHOOL_KIND") + rs.getString("CURRICULUM_CD") + rs.getString("SUBCLASSCD");

                retMap.put(subClassCd, rs.getString("SUBCLASSABBV") + "-AVG-" + rs.getString("AVG"));
            }

        } catch (SQLException ex) {
            log.debug("subClassSQL", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return retMap;
    }

    private Map getSchSubclassInfoMap(final DB2UDB db2) {
        final Map retMap = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSubclassSql(false);
            log.debug(" SchSubClassSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befSchregNo = "";
            int cntScore5 = 0;
            int cntScore4 = 0;
            int cntScore3 = 0;
            int cntScore2 = 0;
            int cntScore1 = 0;
            while (rs.next()) {
                if (!"".equals(befSchregNo) && !befSchregNo.equals(rs.getString("SCHREGNO"))) {
                    final SchScoreCnt schScoreCnt = new SchScoreCnt(String.valueOf(cntScore5), String.valueOf(cntScore4), String.valueOf(cntScore3), String.valueOf(cntScore2), String.valueOf(cntScore1));
                    retMap.put(befSchregNo+ "-CNT", schScoreCnt);
                    cntScore5 = 0;
                    cntScore4 = 0;
                    cntScore3 = 0;
                    cntScore2 = 0;
                    cntScore1 = 0;
                }
                final String subClassCd = rs.getString("CLASSCD") + rs.getString("SCHOOL_KIND") + rs.getString("CURRICULUM_CD") + rs.getString("SUBCLASSCD");

                if (!"99".equals(rs.getString("CURRICULUM_CD"))) {
                    if ("5".equals(rs.getString("SCORE"))) cntScore5++;
                    if ("4".equals(rs.getString("SCORE"))) cntScore4++;
                    if ("3".equals(rs.getString("SCORE"))) cntScore3++;
                    if ("2".equals(rs.getString("SCORE"))) cntScore2++;
                    if ("1".equals(rs.getString("SCORE"))) cntScore1++;
                }

                retMap.put(rs.getString("SCHREGNO")+ "-" + subClassCd, rs.getString("SCORE"));
                befSchregNo = rs.getString("SCHREGNO");
            }
            if (!"".equals(befSchregNo)) {
                final SchScoreCnt schScoreCnt = new SchScoreCnt(String.valueOf(cntScore5), String.valueOf(cntScore4), String.valueOf(cntScore3), String.valueOf(cntScore2), String.valueOf(cntScore1));
                retMap.put(befSchregNo + "-CNT", schScoreCnt);
            }
        } catch (SQLException ex) {
            log.debug("subClassSQL", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return retMap;
    }

    private class SchScoreCnt {
        final String _cntScore5;
        final String _cntScore4;
        final String _cntScore3;
        final String _cntScore2;
        final String _cntScore1;
        public SchScoreCnt(
                final String cntScore5,
                final String cntScore4,
                final String cntScore3,
                final String cntScore2,
                final String cntScore1
                ) {
            _cntScore5 = cntScore5;
            _cntScore4 = cntScore4;
            _cntScore3 = cntScore3;
            _cntScore2 = cntScore2;
            _cntScore1 = cntScore1;
        }
    }

    /**
     * 科目情報取得SQL<br>
     * 　　true：科目リスト取得（科目名称、科目平均評価）<br>
     * 　　false：生徒の評価取得（科目ごと）
     * @param header
     * @return
     */
    private String getSubclassSql(final boolean header) {
        final String setSemester = ("9".equals(_param._semester)) ? _param._ctrlSemester: _param._semester;

        final StringBuffer stb = new StringBuffer();
        if (header) {
            stb.append(" SELECT ");
            stb.append("     SDIV.CLASSCD, ");
            stb.append("     SDIV.SCHOOL_KIND, ");
            stb.append("     SDIV.CURRICULUM_CD, ");
            stb.append("     SDIV.SUBCLASSCD, ");
            stb.append("     SMST.SUBCLASSABBV, ");
            stb.append("     AVGD.AVG ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT SDIV ");
            stb.append("     INNER JOIN SUBCLASS_MST SMST ON SDIV.CLASSCD       = SMST.CLASSCD ");
            stb.append("                                 AND SDIV.SCHOOL_KIND   = SMST.SCHOOL_KIND ");
            stb.append("                                 AND SDIV.CURRICULUM_CD = SMST.CURRICULUM_CD ");
            stb.append("                                 AND SDIV.SUBCLASSCD    = SMST.SUBCLASSCD ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT AVGD ON AVGD.YEAR          = SDIV.YEAR ");
            stb.append("                                           AND AVGD.SEMESTER      = SDIV.SEMESTER ");
            stb.append("                                           AND AVGD.TESTKINDCD    = SDIV.TESTKINDCD ");
            stb.append("                                           AND AVGD.TESTITEMCD    = SDIV.TESTITEMCD ");
            stb.append("                                           AND AVGD.SCORE_DIV     = SDIV.SCORE_DIV ");
            stb.append("                                           AND AVGD.CLASSCD       = SDIV.CLASSCD ");
            stb.append("                                           AND AVGD.SCHOOL_KIND   = SDIV.SCHOOL_KIND ");
            stb.append("                                           AND AVGD.CURRICULUM_CD = SDIV.CURRICULUM_CD ");
            stb.append("                                           AND AVGD.SUBCLASSCD    = SDIV.SUBCLASSCD ");
            stb.append("                                           AND AVGD.AVG_DIV       = '1' ");
            stb.append("                                           AND AVGD.GRADE         = '" + _param._grade + "' ");
            stb.append("                                           AND AVGD.HR_CLASS      = '000' ");
            stb.append("                                           AND AVGD.COURSECD      = '0' ");
            stb.append("                                           AND AVGD.MAJORCD       = '000' ");
            stb.append("                                           AND AVGD.COURSECODE    = '0000' ");
            stb.append(" WHERE ");
            stb.append("         SDIV.YEAR     = '" + _param._ctrlYear + "' ");
            stb.append("     AND SDIV.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '990009' ");
            stb.append("     AND SDIV.CLASSCD <= '90' ");
            stb.append("     AND SCHREGNO IN (SELECT ");
            stb.append("                          SCHREGNO ");
            stb.append("                      FROM ");
            stb.append("                          SCHREG_REGD_DAT ");
            stb.append("                      WHERE ");
            stb.append("                              YEAR     = '" + _param._ctrlYear + "' ");
            stb.append("                          AND SEMESTER = '" + setSemester + "' ");
            stb.append("                          AND GRADE    = '" + _param._grade + "' ");
            stb.append("                      ) ");
            stb.append(" GROUP BY ");
            stb.append("     SDIV.CLASSCD, ");
            stb.append("     SDIV.SCHOOL_KIND, ");
            stb.append("     SDIV.CURRICULUM_CD, ");
            stb.append("     SDIV.SUBCLASSCD, ");
            stb.append("     SMST.SUBCLASSABBV, ");
            stb.append("     AVGD.AVG ");
            stb.append(" ORDER BY ");
            stb.append("     SDIV.CLASSCD, ");
            stb.append("     SDIV.SCHOOL_KIND, ");
            stb.append("     SDIV.CURRICULUM_CD, ");
            stb.append("     SDIV.SUBCLASSCD ");
        } else {
            stb.append(" SELECT ");
            stb.append("     SDIV.SCHREGNO, ");
            stb.append("     SDIV.CLASSCD, ");
            stb.append("     SDIV.SCHOOL_KIND, ");
            stb.append("     SDIV.CURRICULUM_CD, ");
            stb.append("     SDIV.SUBCLASSCD, ");
            stb.append("     SDIV.SCORE ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT SDIV ");
            stb.append(" WHERE ");
            stb.append("         SDIV.YEAR     = '" + _param._ctrlYear + "' ");
            stb.append("     AND SDIV.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '990009' ");
            stb.append("     AND SCHREGNO IN (SELECT ");
            stb.append("                          SCHREGNO ");
            stb.append("                      FROM ");
            stb.append("                          SCHREG_REGD_DAT ");
            stb.append("                      WHERE ");
            stb.append("                             YEAR     = '" + _param._ctrlYear + "' ");
            stb.append("                         AND SEMESTER = '" + setSemester + "' ");
            stb.append("                         AND GRADE    = '" + _param._grade + "' ");
            stb.append("                      ) ");
            stb.append(" ORDER BY ");
            stb.append("     SDIV.SCHREGNO ");
        }

        return stb.toString();
    }

    private List getSchList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchListSql();
            log.debug(" SchListSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo   = rs.getString("SCHREGNO");
                final String hrName     = rs.getString("HR_NAMEABBV");
                final String attendNo   = rs.getString("ATTENDNO");
                final String name       = rs.getString("NAME");
                final String totalScore = rs.getString("SCORE");
                final String totalAvg   = rs.getString("AVG");
                final String domitory   = rs.getString("DOMITORY");
                final String suisen     = rs.getString("SUISEN");
                final String kaigai     = rs.getString("KAIGAI");
                final String ib         = rs.getString("IB");
                final String aHousiki   = rs.getString("A_HOUSIKI");

                final SchInfo schInfo = new SchInfo(schregNo, hrName, attendNo, name, totalScore, totalAvg,domitory , suisen, kaigai, ib, aHousiki);
                retList.add(schInfo);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    /**
     * 対象者リスト取得SQL
     */
    private String getSchListSql() {
        final String setSemester = ("9".equals(_param._semester)) ? _param._ctrlSemester: _param._semester;

        final StringBuffer stb = new StringBuffer();
        if ("1".equals(_param._dataDiv)) {
            stb.append(" WITH JOUI_DATA AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            stb.append("         SCORE, ");
            stb.append("         AVG ");
            stb.append("     FROM ");
            stb.append("         RECORD_RANK_SDIV_DAT ");
            stb.append("     WHERE ");
            stb.append("             YEAR          = '" + _param._ctrlYear + "' ");
            stb.append("         AND SEMESTER      = '" + _param._semester + "' ");
            stb.append("         AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '990009' ");
            stb.append("         AND CLASSCD       = '99' ");
            stb.append("         AND SCHOOL_KIND   = '" + _param._schoolKind + "' ");
            stb.append("         AND CURRICULUM_CD = '99' ");
            stb.append("         AND SUBCLASSCD    = '999999' ");
            stb.append("         AND (GRADE_RANK   <= " + _param._rank + " OR AVG >= " + _param._hyoutei + ") ");
        } else {
            //評定２の指定個数を持つ生徒
            stb.append(" WITH COUNT_2 AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         RECORD_RANK_SDIV_DAT ");
            stb.append("     WHERE ");
            stb.append("             YEAR          = '" + _param._ctrlYear + "' ");
            stb.append("         AND SEMESTER      = '" + _param._semester + "' ");
            stb.append("         AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '990009' ");
            stb.append("         AND SCORE    = 2 ");
            stb.append("     GROUP BY ");
            stb.append("         SCHREGNO ");
            stb.append("     HAVING ");
            stb.append("         count(SCORE) >= " + _param._hyoutei2 + " ");
            //評定１がある生徒
            stb.append(" ), SCORE1_DATA AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         RECORD_RANK_SDIV_DAT ");
            stb.append("     WHERE ");
            stb.append("             YEAR          = '" + _param._ctrlYear + "' ");
            stb.append("         AND SEMESTER      = '" + _param._semester + "' ");
            stb.append("         AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '990009' ");
            stb.append("         AND SCORE    = 1 ");
            stb.append("     GROUP BY ");
            stb.append("         SCHREGNO ");
            stb.append(" ), KAI_LIST AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         COUNT_2 ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         SCORE1_DATA ");
            stb.append(" ), KAI_DATA AS ( ");
            stb.append("     SELECT ");
            stb.append("         SDIV.SCHREGNO, ");
            stb.append("         SCORE, ");
            stb.append("         AVG ");
            stb.append("     FROM ");
            stb.append("         RECORD_RANK_SDIV_DAT SDIV ");
            stb.append("         INNER JOIN KAI_LIST LIST ON SDIV.SCHREGNO = LIST.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("             YEAR          = '" + _param._ctrlYear + "' ");
            stb.append("         AND SEMESTER      = '" + _param._semester + "' ");
            stb.append("         AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '990009' ");
            stb.append("         AND CLASSCD       = '99' ");
            stb.append("         AND SCHOOL_KIND   = '" + _param._schoolKind + "' ");
            stb.append("         AND CURRICULUM_CD = '99' ");
            stb.append("         AND SUBCLASSCD    = '999999' ");
        }
        stb.append(" ), DOMITORY_DATA AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         SCHREGNO, ");
        stb.append("         '寮' AS DOMITORY ");
        stb.append("     FROM ");
        stb.append("         SCHREG_DOMITORY_HIST_DAT ");
        stb.append("     WHERE ");
        stb.append("         '" + _param._ctrlDate + "' BETWEEN DOMI_ENTDAY AND value(DOMI_OUTDAY, '9999-12-31') ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     HDAT.HR_NAMEABBV, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     SDIV.SCORE, ");
        stb.append("     SDIV.AVG, ");
        stb.append("     DOMI.DOMITORY, ");
        stb.append("     case when SEQ014.BASE_REMARK1 = '4'    then '推' else '' end AS SUISEN, ");
        stb.append("     case when SEQ014.BASE_REMARK1 = '3'    then '海' else '' end AS KAIGAI, ");
        stb.append("     case when REGD.COURSECODE     = '0002' then 'IB' else '' end AS IB, ");
        stb.append("     case when SEQ014.BASE_REMARK2 IN ('01','05','08')    then 'A'  else '' end AS A_HOUSIKI ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR     = HDAT.YEAR ");
        stb.append("                                    AND REGD.SEMESTER = HDAT.SEMESTER ");
        stb.append("                                    AND REGD.GRADE    = HDAT.GRADE ");
        stb.append("                                    AND REGD.HR_CLASS = HDAT.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR  = GDAT.YEAR ");
        stb.append("                                    AND REGD.GRADE = GDAT.GRADE ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        if ("1".equals(_param._dataDiv)) {
            stb.append("     INNER JOIN JOUI_DATA SDIV ON REGD.SCHREGNO = SDIV.SCHREGNO ");
        } else {
            stb.append("     INNER JOIN KAI_DATA SDIV ON REGD.SCHREGNO = SDIV.SCHREGNO ");
        }
        stb.append("     LEFT JOIN DOMITORY_DATA DOMI ON REGD.SCHREGNO = DOMI.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST SEQ014 ON SEQ014.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                                            AND SEQ014.BASE_SEQ = '014'  ");
        stb.append(" WHERE ");
        stb.append("         REGD.YEAR        = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER    = '" + setSemester + "' ");
        stb.append("     AND HDAT.GRADE       = '" + _param._grade + "' ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        return stb.toString();
    }

    private class SchInfo {
        final String _schregNo;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _totalScore;
        final String _totalAvg;
        final String _domitory;
        final String _suisen;
        final String _kaigai;
        final String _ib;
        final String _aHousiki;
        public SchInfo(
                final String schregNo,
                final String hrName,
                final String attendNo,
                final String name,
                final String totalScore,
                final String totalAvg,
                final String domitory,
                final String suisen,
                final String kaigai,
                final String ib,
                final String aHousiki
        ) {
            _schregNo   = schregNo;
            _hrName     = hrName;
            _attendNo   = attendNo;
            _name       = name;
            _totalScore = totalScore;
            _totalAvg   = totalAvg;
            _domitory   = domitory;
            _suisen     = suisen;
            _kaigai     = kaigai;
            _ib         = ib;
            _aHousiki   = aHousiki;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63572 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _semester;
        private final String _grade;
        private final String _schoolKind;
        private final String _dataDiv;
        private final String _rank;
        private final String _hyoutei;
        private final String _hyoutei2;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _semeName;
        private final String _gradeName;
        private final String _gradeAvg;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester       = request.getParameter("SEMESTER");
            _grade          = request.getParameter("GRADE").substring(0, 2);
            _schoolKind     = request.getParameter("GRADE").substring(3, 4);
            _dataDiv        = request.getParameter("DATA_DIV");
            _rank           = request.getParameter("RANK");
            _hyoutei        = request.getParameter("HYOUTEI");
            _hyoutei2       = request.getParameter("HYOUTEI_2");
            _ctrlYear       = request.getParameter("CTRL_YEAR");
            _ctrlSemester   = request.getParameter("CTRL_SEMESTER");
            _ctrlDate       = request.getParameter("CTRL_DATE");
            _semeName       = getSemeName(db2, _ctrlYear, _semester);
            _gradeName      = getGradeName(db2, _ctrlYear, _grade);
            _gradeAvg       = getGradeAvg(db2, _ctrlYear, _semester, _schoolKind, _grade);
        }

        private String getSemeName(final DB2UDB db2, final String year, final String semester) {
            String retSemeeName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSemeeName = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException ex) {
                log.debug("getSemesterName exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSemeeName;
        }

        private String getGradeName(final DB2UDB db2, final String year, final String grade) {
            String retGradeName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retGradeName = rs.getString("GRADE_NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getGDAT exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retGradeName;
        }

        private String getGradeAvg(final DB2UDB db2, final String year, final String semester, final String shoolKind, final String grade) {
            String retGradeAvg = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     AVG ");
                stb.append(" FROM ");
                stb.append("     RECORD_AVERAGE_SDIV_DAT ");
                stb.append(" WHERE ");
                stb.append("         YEAR     = '" + year + "' ");
                stb.append("     AND SEMESTER = '" + semester + "' ");
                stb.append("     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '990009' ");
                stb.append("     AND CLASSCD       = '99' ");
                stb.append("     AND SCHOOL_KIND   = '" + shoolKind + "' ");
                stb.append("     AND CURRICULUM_CD = '99'  ");
                stb.append("     AND SUBCLASSCD    = '999999' ");
                stb.append("     AND AVG_DIV       = '1' ");
                stb.append("     AND GRADE         = '" + grade + "' ");
                stb.append("     AND HR_CLASS      = '000' ");
                stb.append("     AND COURSECD      = '0' ");
                stb.append("     AND MAJORCD       = '000' ");
                stb.append("     AND COURSECODE    = '0000' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retGradeAvg = rs.getString("AVG");
                }
            } catch (SQLException ex) {
                log.debug("getGradeAvg exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retGradeAvg;
        }

    }
}

// eof

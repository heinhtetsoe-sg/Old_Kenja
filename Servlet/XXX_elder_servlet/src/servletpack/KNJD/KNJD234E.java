package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;


/**
 * 推薦名簿
 *
 * @author nakasone
 *
 */
public class KNJD234E {
    private static final String FORM_NAME = "KNJD234E.frm";
    private static final String FORM_NAME_J = "KNJD234E_J.frm";
    private boolean _hasData;
    private Param _param;

    private static final Log log = LogFactory.getLog(KNJD234E.class);
    private static final String SEME1 = "1";
    private static final String SEME3 = "3";
    private static final String SEMEALL = "9";

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
        List outputList = getOutputData(db2);
        if (_param._isFormJ) {
            svf.VrSetForm(FORM_NAME_J, 1);
        } else {
            svf.VrSetForm(FORM_NAME, 1);
        }

        int maxtbl1Cnt1 = 3;
        int maxtbl1Cnt2 = 3;
        int maxtbl2Cnt = _param._isFormJ ? 10 : 14;
        int maxtbl2JFushinCnt = 4;
        int maxtbl3Cnt = 28;
        int maxtbl4RowCnt1 = 7;
        int maxtbl4ColCnt1 = 3;
        int maxtbl4RowCnt2 = 7;
        int maxtbl4ColCnt2 = 3;
        int maxtbl5Cnt = 7;
        boolean printendflg = false;
        for (Iterator ite = outputList.iterator();ite.hasNext();) {
            HrInfo hrobj = (HrInfo)ite.next();
            printendflg = false;
            Iterator its11 = hrobj._tbl1Info._moveInStudentMap.keySet().iterator();
            Iterator its12 = hrobj._tbl1Info._moveOutStudentMap.keySet().iterator();
            Iterator its2 = hrobj._tbl2InfoMap.keySet().iterator();
            Iterator its2JFushin = hrobj._tbl2JFushinInfoMap.keySet().iterator();
            Iterator its3 = hrobj._tbl3InfoList.iterator();
            Iterator its41 = hrobj._tbl4Info._kaikinMap.keySet().iterator();
            Iterator its42 = hrobj._tbl4Info._seikinMap.keySet().iterator();
            Iterator its5 = hrobj._tbl5InfoList.iterator();
            Iterator its5j = hrobj._tbl5InfoJList.iterator();
            while (!printendflg) {
                //ヘッダ出力
                setTitle(db2, svf, hrobj);
                //テーブル1出力
                final String regdName1 = (SEME3.equals(_param._semester)) ? "学年始在籍" : "学期始在籍";
                svf.VrsOut("REGD_NAME1", regdName1);
                final String regdName2 = (SEME3.equals(_param._semester)) ? "学年末在籍" : "学期末在籍";
                svf.VrsOut("REGD_NAME2", regdName2);
                svf.VrsOut("START_ENROLL_NUM", hrobj._tbl1Info._strtCnt);
                svf.VrsOut("IN_NUM", hrobj._tbl1Info._transInCnt);
                svf.VrsOut("OUT_NUM", hrobj._tbl1Info._transOutCnt);
                svf.VrsOut("END_ENROLL_NUM", hrobj._tbl1Info._endCnt);
                // 編転入
                int loctbl1Cnt1 = 0;
                int loctbl1Cnt2 = 0;
                while (loctbl1Cnt1 < maxtbl1Cnt1 && its11.hasNext()) {
                    final String key = (String)its11.next();
                    final tbl1DetailInfo det1obj = (tbl1DetailInfo)hrobj._tbl1Info._moveInStudentMap.get(key);
                    final int nlen = KNJ_EditEdit.getMS932ByteLength(det1obj._name);
                    final String nfield = nlen > 14 ? "2" : "1";
                    loctbl1Cnt1++;
                    svf.VrsOutn("IN_NAME" + nfield, loctbl1Cnt1, det1obj._name);
                    svf.VrsOutn("IN_REASON", loctbl1Cnt1, det1obj._reason);
                    svf.VrsOutn("IN_DATE", loctbl1Cnt1, det1obj._date);
                }
                // 転退学
                while (loctbl1Cnt1 < maxtbl1Cnt2 && its12.hasNext()) {
                    final String key = (String)its12.next();
                    final tbl1DetailInfo det2obj = (tbl1DetailInfo)hrobj._tbl1Info._moveOutStudentMap.get(key);
                    final int nlen = KNJ_EditEdit.getMS932ByteLength(det2obj._name);
                    final String nfield = nlen > 14 ? "2" : "1";
                    loctbl1Cnt2++;
                    svf.VrsOutn("OUT_NAME" + nfield, loctbl1Cnt2, det2obj._name);
                    svf.VrsOutn("OUT_REASON", loctbl1Cnt2, det2obj._reason);
                    svf.VrsOutn("OUT_DATE", loctbl1Cnt2, det2obj._date);
                }

                //テーブル2出力
                // 学業成績
                //if () {★★★
//		        	svf.VrsOut("REC_TITLE1", "７科目７００点満点（自由選択科目を除く）");
                //} else {
                //	svf.VrsOut("REC_TITLE2_1", data);
                //	svf.VrsOut("REC_TITLE2_2", data);
                //}
                setRecTitle(db2, svf, hrobj._grHrClass); //成績タイトル
                int loctbl2Cnt = 0;
                while (loctbl2Cnt < maxtbl2Cnt && its2.hasNext()) {
                    final String key = (String)its2.next();
                    tbl2Info outobj = (tbl2Info)hrobj._tbl2InfoMap.get(key);
                    loctbl2Cnt++;
                    svf.VrsOutn("CLASS_RANK", loctbl2Cnt, outobj._classRank);
                    svf.VrsOutn("GRADE_RANK", loctbl2Cnt, outobj._gradeRank);
                    final int nlen = KNJ_EditEdit.getMS932ByteLength(outobj._name);
                    final String nfield = nlen > 14 ? "2" : "1";
                    svf.VrsOutn("NAME" + nfield, loctbl2Cnt, outobj._name);
                    svf.VrsOutn("TOTAL", loctbl2Cnt, outobj._score);
                    svf.VrsOutn("AVERAGE", loctbl2Cnt, outobj._avg);
                    svf.VrsOutn("REMARK", loctbl2Cnt, outobj._remark);
                }
                if (_param._isFormJ) {
                    // 成績不振者
                    int loctbl2JFushinCnt = 0;
                    while (loctbl2JFushinCnt < maxtbl2JFushinCnt && its2JFushin.hasNext()) {
                        final String key = (String)its2JFushin.next();
                        tbl2Info outobj = (tbl2Info)hrobj._tbl2JFushinInfoMap.get(key);
                        loctbl2JFushinCnt++;
                        svf.VrsOutn("FUSHIN_CLASS_RANK", loctbl2JFushinCnt, outobj._classRank);
                        svf.VrsOutn("FUSHIN_GRADE_RANK", loctbl2JFushinCnt, outobj._gradeRank);
                        final int nlen = KNJ_EditEdit.getMS932ByteLength(outobj._name);
                        final String nfield = nlen > 14 ? "2" : "1";
                        svf.VrsOutn("FUSHIN_NAME" + nfield, loctbl2JFushinCnt, outobj._name);
                        svf.VrsOutn("FUSHIN_TOTAL", loctbl2JFushinCnt, outobj._score);
                        svf.VrsOutn("FUSHIN_AVERAGE", loctbl2JFushinCnt, outobj._avg);
                        svf.VrsOutn("FUSHIN_REMARK", loctbl2JFushinCnt, outobj._remark);
                    }
                }
                //テーブル3出力
                // 科目別成績不振者(不振科目数)
                int loctbl3Cnt = 0;
                while (loctbl3Cnt < maxtbl3Cnt && its3.hasNext()) {
                    tbl3Info outobj = (tbl3Info)its3.next();
                    loctbl3Cnt++;
                    final int nlen = KNJ_EditEdit.getMS932ByteLength(outobj._name);
                    final String nfield = nlen > 14 ? "2" : "1";
                    svf.VrsOutn("SLUMP_NAME"+nfield, loctbl3Cnt, outobj._name);
                    svf.VrsOutn("SLUMP_SUBCLASS", loctbl3Cnt, outobj._subclassNameAbbv);
                    svf.VrsOutn("SLUMP_SCORE", loctbl3Cnt, outobj._score);
                }
                //テーブル4出力
                // 皆勤者
                int loctbl4RowCnt1 = 1;
                int loctbl4ColCnt1 = 0;
                svf.VrsOut("LESSON", hrobj._tbl4Info._lessonCnt);
                svf.VrsOut("SUSPEND", hrobj._tbl4Info._stopCnt);
                svf.VrsOut("MUST", hrobj._tbl4Info._mustCnt);
                svf.VrsOut("PERFECT_NUM", String.valueOf(hrobj._tbl4Info._kaikinMap.size()));
                while (loctbl4RowCnt1 < maxtbl4RowCnt1 && its41.hasNext()) {
                    final String key = (String)its41.next();
                    final Student student = (Student)hrobj._tbl4Info._kaikinMap.get(key);
                    if (loctbl4ColCnt1 == maxtbl4ColCnt1) {
                        loctbl4ColCnt1 = 0;
                        loctbl4RowCnt1++;
                    }
                    loctbl4ColCnt1++;
                    final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
                    final String nfield = nlen > 14 ? "2" : "1";
                    svf.VrsOutn("PERFECT_NAME" + loctbl4ColCnt1 + "_" + nfield, loctbl4RowCnt1, student._name);
                }
                // 精勤者
                int loctbl4RowCnt2 = 1;
                int loctbl4ColCnt2 = 0;
                svf.VrsOut("GOOD_NUM", String.valueOf(hrobj._tbl4Info._seikinMap.size()));
                while (loctbl4RowCnt2 < maxtbl4RowCnt2 && its42.hasNext()) {
                    final String key = (String)its42.next();
                    final Student student = (Student)hrobj._tbl4Info._seikinMap.get(key);
                    if (loctbl4ColCnt2 == maxtbl4ColCnt2) {
                        loctbl4ColCnt2 = 0;
                        loctbl4RowCnt2++;
                    }
                    loctbl4ColCnt2++;
                    final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
                    final String nfield = nlen > 14 ? "2" : "1";
                    svf.VrsOutn("GOOD_NAME" + loctbl4ColCnt2 + "_" + nfield, loctbl4RowCnt2, student._name);
                }
                //テーブル5出力
                if (_param._isFormJ) {
                    // 出席が常でない者
                    int loctbl5Cnt = 0;
                    while (loctbl5Cnt < maxtbl5Cnt && its5j.hasNext()) {
                        final tbl5InfoJ outobj = (tbl5InfoJ)its5j.next();

                        loctbl5Cnt++;
                        final int nlen = KNJ_EditEdit.getMS932ByteLength(outobj._name);
                        final String nfield = nlen > 14 ? "3" : nlen > 8 ? "2" : "1";
                        svf.VrsOutn("BAD_NAME" + nfield, loctbl5Cnt, outobj._name);

                        svf.VrsOutn("BAD_SICK", loctbl5Cnt, outobj._sick.toString());
                        svf.VrsOutn("BAD_EARLY", loctbl5Cnt, outobj._early.toString());
                        svf.VrsOutn("BAD_LATE", loctbl5Cnt, outobj._late.toString());
                    }

                } else {
                    // 欠課時数が多い者（科目数）
                    int loctbl5Cnt = 0;
                    while (loctbl5Cnt < maxtbl5Cnt && its5.hasNext()) {
                        final tbl5Info outobj = (tbl5Info)its5.next();

                        loctbl5Cnt++;
                        final int nlen = KNJ_EditEdit.getMS932ByteLength(outobj._name);
                        final String nfield = nlen > 14 ? "3" : nlen > 8 ? "2" : "1";
                        svf.VrsOutn("BAD_NAME" + nfield, loctbl5Cnt, outobj._name);

                        final int sclen = KNJ_EditEdit.getMS932ByteLength(outobj._subclassNameAbbv);
                        final String scfield = sclen > 14 ? "3" : sclen > 8 ? "2" : "1";
                        svf.VrsOutn("BAD_SUBCLASS" + scfield, loctbl5Cnt, outobj._subclassNameAbbv);
                        svf.VrsOutn("BAD_CREDIT1", loctbl5Cnt, String.valueOf(outobj._credits));
                        svf.VrsOutn("BAD_KEKKA", loctbl5Cnt, String.valueOf(outobj._sick));
                        svf.VrsOutn("BAD_REMARK", loctbl5Cnt, outobj._courseNameAbbv);
                    }
                }
                //出力終了チェック
                if (
                    !its11.hasNext()
                    && !its12.hasNext()
                    && !its2.hasNext()
                    && !its3.hasNext()
                    && !its41.hasNext()
                    && !its42.hasNext()
                    && !its5.hasNext()
                   ) {
                    printendflg = true;
                }
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final HrInfo hrobj) {
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
        final Semester semesObj = (Semester)_param._semesterMap.get(_param._semester);
        svf.VrsOut("TITLE", nendo + " " + semesObj._semestername + " 査定会資料");
        svf.VrsOut("HR_NAME", _param._schoolKindName + " " + hrobj._hrName);
        svf.VrsOut("TEACHER_NAME", hrobj._staffName);
    }


    private List getOutputData(final DB2UDB db2) throws SQLException {
        List retList = new ArrayList();
        //選択クラス分回る
        for (int clsCnt = 0;clsCnt < _param._categorySelected.length;clsCnt++) {
            HrInfo addobj = new HrInfo();
            addobj._grHrClass = _param._categorySelected[clsCnt];
            //クラス名称、担任名称
            getHrBaseInfo(db2, addobj);
            //table1情報取得
            gethrTbl1Info(db2, addobj);
            //table2情報取得
            gethrTbl2Info(db2, addobj);
            //table3情報取得
            gethrTbl3Info(db2, addobj);
            addobj._studentMap = getStudentMap(db2, addobj);
            //table4情報取得
            gethrTbl4Info(db2, addobj);
            if (!_param._isFormJ) {
                //table5情報取得
                gethrTbl5Info(db2, addobj);
            }
            retList.add(addobj);
        }
        return retList;
    }

    private void getHrBaseInfo(final DB2UDB db2, final HrInfo addobj) throws SQLException {
        final String sql = "SELECT HR_NAME, STAFFNAME FROM SCHREG_REGD_HDAT T1 "
                          + "LEFT JOIN STAFF_MST T2 ON T1.TR_CD1 = T2.STAFFCD "
                          + "WHERE T1.YEAR = '"+_param._year+"' "
                          + " AND T1.SEMESTER = '"+_param._convsemester+"' "
                          + " AND T1.GRADE || T1.HR_CLASS = '"+addobj._grHrClass+"' ";
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                addobj._hrName = rs.getString("HR_NAME");
                addobj._staffName = rs.getString("STAFFNAME");
            }

        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
    }

    private void gethrTbl1Info(final DB2UDB db2, final HrInfo addobj) throws SQLException {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.STRT_CNT, ");
        stb.append("   T4.TRANS_IN_CNT, ");
        stb.append("   T3.TRANS_OUT_CNT, ");
        stb.append("   T2.END_CNT ");
        stb.append(" FROM ");
        stb.append("   ( ");
        stb.append("    select ");
        stb.append("      count(T1.SCHREGNO) AS STRT_CNT ");
        stb.append("    from ");
        stb.append("      SCHREG_REGD_DAT T1 ");
        stb.append("      LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("        ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      LEFT JOIN SEMESTER_MST M1 ");
        stb.append("        ON M1.YEAR = T1.YEAR ");
        if(SEME3.equals(_param._semester)){
            stb.append("       AND M1.SEMESTER = '" + SEMEALL + "' ");
        } else {
            stb.append("       AND M1.SEMESTER = T1.SEMESTER ");
        }
        stb.append("    WHERE ");
        stb.append("      T1.YEAR = '" + _param._year + "' ");
        stb.append("      AND T1.SEMESTER = '" + _param._convsemester + "' ");
        stb.append("      AND T1.GRADE || T1.HR_CLASS = '"+addobj._grHrClass+"' ");
        if(SEME1.equals(_param._semester)){
            stb.append("      AND T2.ENT_DATE BETWEEN M1.SDATE AND M1.EDATE ");
            stb.append("      AND T2.ENT_DIV IN ('1', '2')  ");
        } else {
            stb.append("      AND T2.ENT_DATE <= M1.SDATE ");
        }
        stb.append("      AND (M1.SDATE <= T2.GRD_DATE OR T2.GRD_DATE IS NULL) ");
        stb.append("   ) T1, ");
        stb.append("   ( ");
        stb.append("    select ");
        stb.append("      count(T1.SCHREGNO) AS END_CNT ");
        stb.append("    from ");
        stb.append("      SCHREG_REGD_DAT T1 ");
        stb.append("      LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("        ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      LEFT JOIN SEMESTER_MST M1 ");
        stb.append("        ON M1.YEAR = T1.YEAR ");
        if(SEME3.equals(_param._semester)){
            stb.append("       AND M1.SEMESTER = '" + SEMEALL + "' ");
        } else {
            stb.append("       AND M1.SEMESTER = T1.SEMESTER ");
        }
        stb.append("    WHERE ");
        stb.append("      T1.YEAR = '" + _param._year + "' ");
        stb.append("      AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("      AND T1.GRADE || T1.HR_CLASS = '"+addobj._grHrClass+"' ");
        if(SEME1.equals(_param._semester)){
            stb.append("      AND T2.ENT_DATE BETWEEN M1.SDATE AND M1.EDATE ");
            stb.append("      AND T2.ENT_DIV IN ('1', '2')  ");
        } else {
            stb.append("      AND T2.ENT_DATE <= M1.SDATE ");
        }
        stb.append("      AND (M1.EDATE <= T2.GRD_DATE OR T2.GRD_DATE IS NULL) ");
        stb.append("   ) T2, ");
        stb.append("   ( ");
        stb.append("    select ");
        stb.append("      count(T1.SCHREGNO) AS TRANS_OUT_CNT ");
        stb.append("    FROM ");
        stb.append("      SCHREG_REGD_DAT T1 ");
        stb.append("      LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("        ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      LEFT JOIN SEMESTER_MST M1 ");
        stb.append("        ON M1.YEAR = T1.YEAR ");
        if(SEME3.equals(_param._semester)){
            stb.append("       AND M1.SEMESTER = '" + SEMEALL + "' ");
        } else {
            stb.append("       AND M1.SEMESTER = T1.SEMESTER ");
        }
        stb.append("    WHERE ");
        stb.append("      T1.YEAR = '" + _param._year + "' ");
        stb.append("      AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("      AND T1.GRADE || T1.HR_CLASS = '"+addobj._grHrClass+"' ");
        stb.append("      AND M1.SDATE <= T2.GRD_DATE ");
        stb.append("      AND T2.GRD_DATE <= M1.EDATE ");
        stb.append("      AND T2.GRD_DIV IN ('4', '5', '7') ");
        stb.append("   ) T3, ");
        stb.append("   ( ");
        stb.append("    select ");
        stb.append("      count(T1.SCHREGNO) AS TRANS_IN_CNT ");
        stb.append("    FROM ");
        stb.append("      SCHREG_REGD_DAT T1 ");
        stb.append("      LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("        ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      LEFT JOIN SEMESTER_MST M1 ");
        stb.append("        ON M1.YEAR = T1.YEAR ");
        if(SEME3.equals(_param._semester)){
            stb.append("       AND M1.SEMESTER = '" + SEMEALL + "' ");
        } else {
            stb.append("       AND M1.SEMESTER = T1.SEMESTER ");
        }
        stb.append("    WHERE ");
        stb.append("      T1.YEAR = '" + _param._year + "' ");
        stb.append("      AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("      AND T1.GRADE || T1.HR_CLASS = '"+addobj._grHrClass+"' ");
        stb.append("      AND M1.SDATE <= T2.GRD_DATE ");
        stb.append("      AND T2.GRD_DATE <= M1.EDATE ");
        stb.append("      AND T2.GRD_DIV IS NOT NULL ");
        stb.append("      AND T2.GRD_DIV NOT IN ('4', '5', '7') ");
        stb.append("   ) T4 ");
        db2.query(stb.toString());
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                  final String strtCnt = rs.getString("STRT_CNT");
                  final String transInCnt = rs.getString("TRANS_IN_CNT");
                  final String transOutCnt = rs.getString("TRANS_OUT_CNT");
                  final String endCnt = rs.getString("END_CNT");
                  addobj._tbl1Info = new tbl1Info(strtCnt, transInCnt, transOutCnt, endCnt);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        gethrTbl1Detail1Info(db2, addobj);
        gethrTbl1Detail2Info(db2, addobj);
    }

    private void gethrTbl1Detail1Info(final DB2UDB db2, final HrInfo addobj) throws SQLException {
        StringBuffer stb = new StringBuffer();
        stb.append("    select ");
        stb.append("      T1.SCHREGNO,");
        stb.append("      T2.NAME, ");
        stb.append("      T2.GRD_REASON, ");
        stb.append("      T2.GRD_DATE ");
        stb.append("    FROM ");
        stb.append("      SCHREG_REGD_DAT T1 ");
        stb.append("      LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("        ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      LEFT JOIN SEMESTER_MST M1 ");
        stb.append("        ON M1.YEAR = T1.YEAR ");
        stb.append("       AND M1.SEMESTER = T1.SEMESTER ");
        stb.append("    WHERE ");
        stb.append("      T1.YEAR = '" + _param._year + "' ");
        stb.append("      AND T1.SEMESTER = '" + _param._convsemester + "' ");
        stb.append("      AND T1.GRADE || T1.HR_CLASS = '" + addobj._grHrClass + "' ");
        stb.append("      AND M1.SDATE <= T2.GRD_DATE ");
        stb.append("      AND T2.GRD_DATE <= M1.EDATE ");
        stb.append("      AND T2.GRD_DIV IN ('4', '5', '7') ");
        log.debug(" gethrTbl1Detail1Info sql = " + stb.toString());
        db2.query(stb.toString());
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                  final String name = rs.getString("NAME");
                  final String reason = rs.getString("GRD_REASON");
                  final String dateStr = rs.getString("GRD_DATE");
                  addobj._tbl1Info._moveOutStudentMap.put(schregNo, new tbl1DetailInfo(schregNo, name, reason, dateStr));
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
    }

    private void gethrTbl1Detail2Info(final DB2UDB db2, final HrInfo addobj) throws SQLException {
        StringBuffer stb = new StringBuffer();
        stb.append(" select ");
        stb.append("   T1.SCHREGNO,");
        stb.append("   T2.NAME, ");
        stb.append("   T2.GRD_REASON, ");
        stb.append("   T2.GRD_DATE ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SEMESTER_MST M1 ");
        stb.append("     ON M1.YEAR = T1.YEAR ");
        stb.append("    AND M1.SEMESTER = T1.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + _param._convsemester + "' ");
        stb.append("   AND T1.GRADE || T1.HR_CLASS = '" + addobj._grHrClass + "' ");
        stb.append("   AND M1.SDATE <= T2.GRD_DATE ");
        stb.append("   AND T2.GRD_DATE <= M1.EDATE ");
        stb.append("   AND T2.GRD_DIV IS NOT NULL ");
        stb.append("   AND T2.GRD_DIV NOT IN ('4', '5', '7') ");
        db2.query(stb.toString());
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                  final String name = rs.getString("NAME");
                  final String reason = rs.getString("GRD_REASON");
                  final String dateStr = rs.getString("GRD_DATE");
                  addobj._tbl1Info._moveInStudentMap.put(schregNo, new tbl1DetailInfo(schregNo, name, reason, dateStr));
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
    }

    private void gethrTbl2Title(final DB2UDB db2, final HrInfo addobj) throws SQLException {
        //★表上部分のタイトルの情報を取得しないといけない。
    }

    private void gethrTbl2Info(final DB2UDB db2, final HrInfo addobj) throws SQLException {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.CHAIR_GROUP_AVG_RANK AS CLASSRANK, ");
        stb.append("   T1.CHAIR_GROUP_AVG_RANK AS GRADERANK, ");
        stb.append("   T3.NAME, ");
        stb.append("   T1.SCORE, ");
        stb.append("   T1.AVG, ");
        stb.append("   M1.COURSECODENAME AS REMARK ");
        stb.append(" FROM ");
        stb.append("   RECORD_RANK_SDIV_DAT T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T3 ");
        stb.append("     ON T3.SCHREGNO = T2.SCHREGNO ");
        stb.append("   LEFT JOIN COURSECODE_MST M1 ");
        stb.append("     ON M1.COURSECODE = T2.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("   AND T2.GRADE || T2.HR_CLASS = '" + addobj._grHrClass + "' ");
        stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || SCORE_DIV = '990008' ");
        stb.append("   AND T1.SUBCLASSCD = '999999' ");
        stb.append("   AND T1.AVG >= 80 ");
        stb.append(" ORDER BY ");
        stb.append("   T1.CHAIR_GROUP_AVG_RANK, ");
        stb.append("   T1.SCORE DESC ");
        log.debug(" gethrTbl2Info sql = " + stb.toString());
        db2.query(stb.toString());
        addobj._tbl2InfoMap = new LinkedMap();
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                  final String schregNo = rs.getString("SCHREGNO");
                  final String name = rs.getString("NAME");
                  final String classRank = StringUtils.defaultString(rs.getString("CLASSRANK"), "");
                  final String gradeRank = StringUtils.defaultString(rs.getString("GRADERANK"), "");
                  final String score = rs.getString("SCORE");
                  final String avg = rs.getString("AVG");
                  final String remark = StringUtils.defaultString(rs.getString("REMARK"), "");
                  addobj._tbl2InfoMap.put(schregNo, new tbl2Info(schregNo, name, classRank, gradeRank, score, avg, remark));
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        if (_param._isFormJ) {
            StringBuffer stbJ = new StringBuffer();
            stbJ.append(" SELECT ");
            stbJ.append("   T1.SCHREGNO, ");
            stbJ.append("   T1.CHAIR_GROUP_AVG_RANK AS CLASSRANK, ");
            stbJ.append("   T1.CHAIR_GROUP_AVG_RANK AS GRADERANK, ");
            stbJ.append("   T3.NAME, ");
            stbJ.append("   T1.SCORE, ");
            stbJ.append("   T1.AVG, ");
            stbJ.append("   M1.COURSECODENAME AS REMARK ");
            stbJ.append(" FROM ");
            stbJ.append("   RECORD_RANK_SDIV_DAT T1 ");
            stbJ.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
            stbJ.append("     ON T2.YEAR = T1.YEAR ");
            stbJ.append("    AND T2.SEMESTER = T1.SEMESTER ");
            stbJ.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stbJ.append("   LEFT JOIN SCHREG_BASE_MST T3 ");
            stbJ.append("     ON T3.SCHREGNO = T2.SCHREGNO ");
            stbJ.append("   LEFT JOIN COURSECODE_MST M1 ");
            stbJ.append("     ON M1.COURSECODE = T2.COURSECODE ");
            stbJ.append(" WHERE ");
            stbJ.append("   T1.YEAR = '" + _param._year + "' ");
            stbJ.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
            stbJ.append("   AND T2.GRADE || T2.HR_CLASS = '" + addobj._grHrClass + "' ");
            stbJ.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || SCORE_DIV = '990008' ");
            stbJ.append("   AND T1.SUBCLASSCD = '999999' ");
            stbJ.append("   AND T1.AVG < 40 ");
            stbJ.append(" ORDER BY ");
            stbJ.append("   VALUE(T1.CHAIR_GROUP_AVG_RANK, -1) DESC, ");
            stbJ.append("   T1.SCORE ");
            log.debug(" gethrTbl2Info sql = " + stbJ.toString());
            db2.query(stbJ.toString());
            addobj._tbl2JFushinInfoMap = new LinkedMap();
            final ResultSet rs2 = db2.getResultSet();
            try {
                while (rs2.next()) {
                      final String schregNo = rs2.getString("SCHREGNO");
                      final String name = rs2.getString("NAME");
                      final String classRank = StringUtils.defaultString(rs2.getString("CLASSRANK"), "");
                      final String gradeRank = StringUtils.defaultString(rs2.getString("GRADERANK"), "");
                      final String score = rs2.getString("SCORE");
                      final String avg = rs2.getString("AVG");
                      final String remark = StringUtils.defaultString(rs2.getString("REMARK"), "");
                      addobj._tbl2JFushinInfoMap.put(schregNo, new tbl2Info(schregNo, name, classRank, gradeRank, score, avg, remark));
                }
            } finally {
                DbUtils.closeQuietly(rs2);
                db2.commit();
            }

        }
    }

    private void gethrTbl3Info(final DB2UDB db2, final HrInfo addobj) throws SQLException {
        final String borderScore = "39";
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH ATTEND_SUBCLASS AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD AS SUBCLASSCD ");
        stb.append(" FROM  ");
        stb.append("   SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append(" WHERE ");
        stb.append("   YEAR = '" + _param._year + "' ");
        stb.append(" ), NGCNT_TBL AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   SUM(CASE WHEN T1.SCORE <= " + borderScore + " THEN 1 ELSE 0 END) AS NG_CNT ");
        stb.append(" FROM ");
        stb.append("   RECORD_RANK_SDIV_DAT T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("   AND T2.GRADE || T2.HR_CLASS = '" + addobj._grHrClass + "' ");
        stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || SCORE_DIV = '990008' ");
        stb.append("   AND T1.SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B', '999999') ");
        stb.append("   AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD NOT IN ( SELECT SUBCLASSCD FROM ATTEND_SUBCLASS ) "); //合併元科目は除外
        stb.append(" GROUP BY ");
        stb.append("   T1.SCHREGNO ");
        stb.append(" ), FILTER_NGCNT AS ( ");
        stb.append(" SELECT ");
        stb.append("   SCHREGNO ");
        stb.append(" FROM ");
        stb.append("   NGCNT_TBL ");
        stb.append(" WHERE ");
        stb.append("   NG_CNT >= 2 "); //2科目以上の生徒を対象とする。
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T2.ATTENDNO, ");
        stb.append("   T3.NAME, ");
        stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("   M1.SUBCLASSABBV, ");
        stb.append("   T1.SCORE ");
        stb.append(" FROM ");
        stb.append("   RECORD_RANK_SDIV_DAT T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T3 ");
        stb.append("     ON T3.SCHREGNO = T2.SCHREGNO ");
        stb.append("   LEFT JOIN SUBCLASS_MST M1 ");
        stb.append("     ON M1.CLASSCD = T1.CLASSCD ");
        stb.append("    AND M1.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("    AND M1.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("    AND M1.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("   AND T2.GRADE || T2.HR_CLASS = '" + addobj._grHrClass + "' ");
        stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || SCORE_DIV = '990008' ");
        stb.append("   AND T1.SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B', '999999') ");
        stb.append("   AND T1.SCORE <= " + borderScore + " ");
        stb.append("   AND T1.SCHREGNO IN (SELECT SCHREGNO FROM FILTER_NGCNT) ");
        stb.append(" ORDER BY ");
        stb.append("   T2.ATTENDNO, ");
        stb.append("   T1.SUBCLASSCD ");
        db2.query(stb.toString());
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String attendNo = rs.getString("ATTENDNO");
                  final String schregNo = rs.getString("SCHREGNO");
                  final String name = rs.getString("NAME");
                  final String subclassCd = rs.getString("SUBCLASSCD");
                  final String subclassAbbv = rs.getString("SUBCLASSABBV");
                  final String score = rs.getString("SCORE");
                  if (_param._d040NotTargetSubclasscdList.contains(subclassCd)) {
                      continue;
                  }
                  addobj._tbl3InfoList.add(new tbl3Info(schregNo, attendNo, name, subclassCd, subclassAbbv, score));

            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
    }

    private void gethrTbl4Info(final DB2UDB db2, final HrInfo addobj) throws SQLException {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.LESSON_CNT, ");
        stb.append("   T2.MUST_CNT, ");
        stb.append("   T1.LESSON_CNT - T2.MUST_CNT AS STOP_CNT ");
        stb.append(" FROM ");
        stb.append(" ( ");
        stb.append(" select ");
        stb.append("   sum(LESSON) AS LESSON_CNT ");
        stb.append(" FROM ");
        stb.append("   ATTEND_LESSON_MST ");
        stb.append(" WHERE ");
        stb.append("   YEAR = '" + _param._year + "' ");
        stb.append("   AND SEMESTER = '" + _param._semester + "' ");
        stb.append("   AND GRADE = '" + _param._grade + "' ");
        if (!"".equals(_param._eDate)) {
            String[] cutMonth = StringUtils.split(_param._eDate, '-');
            if (cutMonth.length > 1) {
                stb.append("   AND MONTH <= '" + cutMonth[1] + "' ");
                if (Integer.parseInt(cutMonth[1]) <= 3) {
                    stb.append("   AND MONTH >= '04' ");
                }
            }
        }
        stb.append("   AND COURSECD = '0' ");
        stb.append("   AND MAJORCD = '000' ");
        stb.append(" ) T1, ");
        stb.append(" ( ");
        stb.append(" select ");
        stb.append("   sum(LESSON) AS MUST_CNT ");
        stb.append(" FROM ");
        stb.append("   ATTEND_SEMES_LESSON_DAT ");
        stb.append(" WHERE ");
        stb.append("   YEAR = '" + _param._year + "' ");
        stb.append("   AND SEMESTER = '" + _param._semester + "' ");
        stb.append("   AND GRADE || HR_CLASS = '" + addobj._grHrClass + "' ");
        if (!"".equals(_param._eDate)) {
            String[] cutMonth = StringUtils.split(_param._eDate, '-');
            if (cutMonth.length > 1) {
                stb.append("   AND MONTH <= '" + cutMonth[1] + "' ");
                if (Integer.parseInt(cutMonth[1]) <= 3) {
                    stb.append("   AND MONTH >= '04' ");
                }
            }
        }
        stb.append(" ) T2 ");
        log.debug(" gethrTbl4Info sql = " + stb.toString());
        db2.query(stb.toString());
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                  final String lessonCnt = rs.getString("LESSON_CNT");
                  final String mustCnt = rs.getString("MUST_CNT");
                  final String stopCnt = rs.getString("STOP_CNT");
                  addobj._tbl4Info = new tbl4Info(lessonCnt, mustCnt, stopCnt);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        gethrTbl4DetailInfo(db2, addobj);
    }

    private Map getStudentMap(final DB2UDB db2, final HrInfo addobj) throws SQLException {
        //指定クラスの生徒を取得して、その分だけ回す。
        Map studentMap = new LinkedMap();
        StringBuffer stb = new StringBuffer();
        stb.append("    select ");
        stb.append("      T1.SCHREGNO, ");
        stb.append("      T1.ATTENDNO, ");
        stb.append("      T2.NAME ");
        stb.append("    FROM ");
        stb.append("      SCHREG_REGD_DAT T1 ");
        stb.append("      LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("        ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      LEFT JOIN SEMESTER_MST M1 ");
        stb.append("        ON M1.YEAR = T1.YEAR ");
        stb.append("       AND M1.SEMESTER = T1.SEMESTER ");
        stb.append("    WHERE ");
        stb.append("      T1.YEAR = '" + _param._year + "' ");
        stb.append("      AND T1.SEMESTER = '" + _param._convsemester + "' ");
        stb.append("      AND T1.GRADE || T1.HR_CLASS = '"+addobj._grHrClass+"' ");
        if(SEME1.equals(_param._semester)){
            stb.append("      AND T2.ENT_DATE BETWEEN M1.SDATE AND M1.EDATE ");
            stb.append("      AND T2.ENT_DIV IN ('1', '2')  ");
        } else {
            stb.append("      AND T2.ENT_DATE <= M1.SDATE ");
        }
        stb.append("      AND (T2.GRD_DATE IS NULL ");
        stb.append("       OR M1.SDATE <= T2.GRD_DATE ");
        stb.append("      AND (M1.EDATE <= T2.GRD_DATE OR T2.GRD_DATE IS NULL) ");
        stb.append("          ) ");
        log.debug(" getStudentMap sql = " + stb.toString());
        db2.query(stb.toString());
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String attendNo = rs.getString("ATTENDNO");
                  final String name = rs.getString("NAME");
                  studentMap.put(schregNo, new Student(schregNo, attendNo, name));
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return studentMap;
    }

    private void gethrTbl4DetailInfo(final DB2UDB db2, final HrInfo addobj) throws SQLException {
        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
            final DateRange range = (DateRange) rit.next();
            //attendRange分回しているが、結局必要なのはSEMEALLのみ。
            if (!SEMEALL.equals(range._key)) {
                continue;
            }
            _param._attendParamMap.put("schregno", "?");
            _param._attendParamMap.put("grade", "?");
            _param._attendParamMap.put("hrClass", "?");

            addobj._tbl4Info._kaikinMap = new TreeMap();
            addobj._tbl4Info._seikinMap = new TreeMap();
            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            try {
                final String sql = AttendAccumulate.getAttendSemesSql(
                                                                       _param._year,
                                                                       _param._semester,
                                                                       range._sdate,
                                                                       _param._eDate,
                                                                       _param._attendParamMap
                                                                      );
                psAtSeme = db2.prepareStatement(sql);

                for (final Iterator it = addobj._studentMap.keySet().iterator(); it.hasNext();) {
                    final String schregNo = (String)it.next();
                    final Student student = (Student)addobj._studentMap.get(schregNo);

                    psAtSeme.setString(1, schregNo);
                    psAtSeme.setString(2, _param._grade);
                    psAtSeme.setString(3, StringUtils.substring(addobj._grHrClass, _param._grade.length()));
                    rsAtSeme = psAtSeme.executeQuery();

                    while (rsAtSeme.next()) {
                        if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                            continue;
                        }
                        //皆勤判定
                        if (rsAtSeme.getInt("SICK") + rsAtSeme.getInt("LATE") + rsAtSeme.getInt("EARLY") == 0) {
                            //皆勤者として登録
                            addobj._tbl4Info._kaikinMap.put(student._attendNo, student);
                        } else if (rsAtSeme.getInt("SICK") + rsAtSeme.getInt("LATE") + rsAtSeme.getInt("EARLY") <= 3) {
                            //精勤者として登録
                            addobj._tbl4Info._seikinMap.put(student._attendNo, student);
                        }

                        final int kessekiLimit;
                        if ("3".equals(_param._semester)) {
                            kessekiLimit = 35;
                        } else {
                            kessekiLimit = 12;
                        }
                        if (rsAtSeme.getInt("SICK") * 3 + rsAtSeme.getInt("LATE") + rsAtSeme.getInt("EARLY") >= kessekiLimit * 3) {
                            //精勤者として登録
                            addobj._tbl5InfoJList.add(new tbl5InfoJ(student._schregNo, student._name, rsAtSeme.getBigDecimal("SICK"), rsAtSeme.getBigDecimal("LATE"), rsAtSeme.getBigDecimal("EARLY")));
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

    private void gethrTbl5Info(final DB2UDB db2, final HrInfo addobj) throws SQLException {
        addobj._tbl5InfoList = new ArrayList();
        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
            final DateRange range = (DateRange) rit.next();
            //attendRange分回しているが、結局必要なのはSEMEALLのみ。
            if (!SEMEALL.equals(range._key)) {
                continue;
            }
            _param._attendParamMap.put("schregno", "?");
            _param._attendParamMap.put("grade", "?");
            _param._attendParamMap.put("hrClass", "?");
            final String edate = range._edate.compareTo(_param._eDate) > 0 ? _param._eDate : range._edate;

            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            try {
                final String sql = AttendAccumulate.getAttendSubclassSql(
                        _param._year,
                        range._key,
                        range._sdate,
                        edate,
                        _param._attendParamMap
                );

                psAtSeme = db2.prepareStatement(sql);

                for (final Iterator it = addobj._studentMap.keySet().iterator(); it.hasNext();) {
                    final String schregNo = (String)it.next();

                    psAtSeme.setString(1, schregNo);
                    psAtSeme.setString(2, _param._grade);
                    psAtSeme.setString(3, StringUtils.substring(addobj._grHrClass, _param._grade.length()));
                    rsAtSeme = psAtSeme.executeQuery();

                    while (rsAtSeme.next()) {
                        final String subclasscd = rsAtSeme.getString("SUBCLASSCD");
                        if (_param._d040NotTargetSubclasscdList.contains(subclasscd)) {
                            continue;
                        }
                        final BigDecimal lesson = rsAtSeme.getBigDecimal("MLESSON");
                        final BigDecimal rawReplacedSick = rsAtSeme.getBigDecimal("RAW_REPLACED_SICK");
                        final BigDecimal rawSick = rsAtSeme.getBigDecimal("SICK1");
                        final BigDecimal sick1 = "1".equals(rsAtSeme.getString("IS_COMBINED_SUBCLASS")) ? rawReplacedSick : rawSick;
                        final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                        final BigDecimal sick = rsAtSeme.getBigDecimal("SICK2");
                        final BigDecimal replacedSick = rsAtSeme.getBigDecimal("REPLACED_SICK");
                        final BigDecimal sick2 = "1".equals(rsAtSeme.getString("IS_COMBINED_SUBCLASS")) ? replacedSick : sick;  //欠課時数
                        final BigDecimal late = rsAtSeme.getBigDecimal("LATE");
                        final BigDecimal early = rsAtSeme.getBigDecimal("EARLY");
                        final BigDecimal credits = rsAtSeme.getBigDecimal("CREDITS");
                        //MAX学期指定以外なら(単位数/3)<欠課時数、つまり1/3学期分として判定。MAX学期(or"9"学期)なら(単位数/1)<欠課時数で判定
                        final BigDecimal baseCrdVal = (_param._convsemester.equals(_param._maxsemester)) ? new BigDecimal("1.0") : new BigDecimal("3.0");
                        if (credits != null && baseCrdVal != null && credits.multiply(new  BigDecimal(7)).divide(baseCrdVal, BigDecimal.ROUND_UP).compareTo(sick2) < 0) {
                            final String name = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME FROM SCHREG_BASE_MST WHERE SCHREGNO = '" + schregNo + "'"));
                            final String subclsNameAbbv = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SUBCLASSABBV FROM SUBCLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + subclasscd + "' "));
                            final String courseCode = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COURSECODE FROM SCHREG_REGD_DAT WHERE YEAR = '" + _param._year + "' AND SEMESTER = '" + _param._convsemester + "' AND SCHREGNO = '" + schregNo + "' "));
                            final String courseNameAbbv = !"".equals(StringUtils.defaultString(courseCode, "")) ? KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COURSECODEABBV1 FROM COURSECODE_MST WHERE COURSECODE = '" + courseCode + "' ")) : "";
                            final tbl5Info addwk = new tbl5Info(schregNo, name, subclasscd, StringUtils.defaultString(subclsNameAbbv, ""), StringUtils.defaultString(courseNameAbbv, ""), lesson, attend, sick2, late, early, credits);
                            addobj._tbl5InfoList.add(addwk);
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

    /** 生徒クラス */
    private class Student {
        final String _schregNo;
        final String _attendNo;
        final String _name;

        Student(
                final String schregNo,
                final String attendNo,
                final String name
        ) {
            _schregNo = schregNo;
            _attendNo = attendNo;
            _name = name;
        }
    }

    private class HrInfo {
        private String _grHrClass;
        private String _hrName;
        private String _staffName;
        private tbl1Info _tbl1Info;
        private Map _tbl2InfoMap;
        private Map _tbl2JFushinInfoMap;
        private List _tbl3InfoList;
        private tbl4Info _tbl4Info;
        private List _tbl5InfoList;
        private List _tbl5InfoJList;
        private Map _studentMap;
        HrInfo() {
            _grHrClass = "";
            _hrName = "";
            _staffName = "";
            _tbl2InfoMap = new LinkedMap();
            _tbl2JFushinInfoMap = new LinkedMap();
            _tbl3InfoList = new ArrayList();
            _tbl5InfoList = new ArrayList();
            _tbl5InfoJList = new ArrayList();
            _studentMap = new LinkedMap();
        }
    }

    private class tbl1Info {
        private final String _strtCnt;
        private final String _transInCnt;
        private final String _transOutCnt;
        private final String _endCnt;
        private final Map _moveInStudentMap;
        private final Map _moveOutStudentMap;
        tbl1Info(
                final String strtCnt,
                final String transInCnt,
                final String transOutCnt,
                final String endCnt
                ) {
            _strtCnt = strtCnt;
            _transInCnt = transInCnt;
            _transOutCnt = transOutCnt;
            _endCnt = endCnt;
            _moveInStudentMap = new LinkedMap();
            _moveOutStudentMap = new LinkedMap();
        }
    }
    private class tbl1DetailInfo {
        private final String _schregno;
        private final String _name;
        private final String _reason;
        private final String _date;
        tbl1DetailInfo(
                final String schregno,
                final String name,
                final String reason,
                final String date
                ) {
            _schregno = schregno;
            _name = name;
            _reason = reason;
            _date = date;

        }
    }

    private class tbl2Info {
        private final String _schregNo;
        private final String _name;
        private final String _classRank;
        private final String _gradeRank;
        private final String _score;
        private final String _avg;
        private final String _remark;
        tbl2Info(
                final String schregNo,
                final String name,
                final String classRank,
                final String gradeRank,
                final String score,
                final String avg,
                final String remark
                ) {
            _schregNo = schregNo;
            _name = name;
            _classRank = classRank;
            _gradeRank = gradeRank;
            _score = score;
            _avg = avg;
            _remark = remark;
        }
    }
    private class tbl3Info {
        private final String _schregNo;
        private final String _attendNo;
        private final String _name;
        private final String _subclassCd;
        private final String _subclassNameAbbv;
        private final String _score;
        tbl3Info(
                final String schregNo,
                final String attendNo,
                final String name,
                final String subclassCd,
                final String subclassNameAbbv,
                final String score
                ) {
            _schregNo = schregNo;
            _name = name;
            _subclassCd = subclassCd;
            _subclassNameAbbv = subclassNameAbbv;
            _score = score;
            _attendNo = attendNo;
        }
    }
    private class tbl4Info {
        private final String _lessonCnt;
        private final String _mustCnt;
        private final String _stopCnt;
        private Map _kaikinMap;
        private Map _seikinMap;
        tbl4Info(
                final String lessonCnt,
                final String mustCnt,
                final String stopCnt
                ) {
            _lessonCnt = lessonCnt;
            _mustCnt = mustCnt;
            _stopCnt = stopCnt;
            _kaikinMap = new TreeMap();
            _seikinMap = new TreeMap();
        }
    }

    private class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private class SemesterDetail implements Comparable {
        final Semester _semester;
        final String _cdSemesterDetail;
        final String _semestername;
        final String _sdate;
        final String _edate;
        public SemesterDetail(Semester semester, String cdSemesterDetail, String semestername, final String sdate, final String edate) {
            _semester = semester;
            _cdSemesterDetail = cdSemesterDetail;
            _semestername = StringUtils.defaultString(semestername);
            _sdate = sdate;
            _edate = edate;
        }
        public int compareTo(final Object o) {
            if (!(o instanceof SemesterDetail)) {
                return 0;
            }
            SemesterDetail sd = (SemesterDetail) o;
            int rtn;
            rtn = _semester.compareTo(sd._semester);
            if (rtn != 0) {
                return rtn;
            }
            rtn = _cdSemesterDetail.compareTo(sd._cdSemesterDetail);
            return rtn;
        }
        public String toString() {
            return "SemesterDetail(" + _semester._semester + ", " + _cdSemesterDetail + ", " + _semestername + ")";
        }
    }

    private class TestItem {
        final String _year;
        final Semester _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        final String _testitemabbv1;
        final String _sidouInput;
        final String _sidouInputInf;
        final SemesterDetail _semesterDetail;
        boolean _isGakunenKariHyotei;
        int _printKettenFlg; // -1: 表示しない（仮評定）、1: 値が1をカウント（学年評定）、2:換算した値が1をカウント（評価等）
        public TestItem(final String year, final Semester semester, final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname, final String testitemabbv1, final String sidouInput, final String sidouInputInf,
                final SemesterDetail semesterDetail) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
            _sidouInput = sidouInput;
            _sidouInputInf = sidouInputInf;
            _semesterDetail = semesterDetail;
        }
        public String getTestcd() {
            return _semester._semester +_testkindcd +_testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester._semester + _testkindcd + _testitemcd + "(" + _scoreDiv + "))";
        }
    }

    private static class tbl5Info {
        final String _schregNo;
        final String _name;
        final String _subclassCd;
        final String _subclassNameAbbv;
        final String _courseNameAbbv;
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;
        final BigDecimal _credits;

        public tbl5Info(
                         final String schregNo,
                         final String name,
                         final String subclassCd,
                         final String subclassNameAbbv,
                         final String courseNameAbbv,
                         final BigDecimal lesson,
                         final BigDecimal attend,
                         final BigDecimal sick,
                         final BigDecimal late,
                         final BigDecimal early,
                         final BigDecimal credits
                         ) {
            _schregNo = schregNo;
            _name = name;
            _subclassCd = subclassCd;
            _subclassNameAbbv = subclassNameAbbv;
            _courseNameAbbv = courseNameAbbv;
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
            _late = late;
            _early = early;
            _credits = credits;
        }
    }

    private static class tbl5InfoJ {
        final String _schregNo;
        final String _name;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;

        public tbl5InfoJ(
                final String schregNo,
                final String name,
                final BigDecimal sick,
                final BigDecimal late,
                final BigDecimal early
                ) {
            _schregNo = schregNo;
            _name = name;
            _sick = sick;
            _late = late;
            _early = early;
        }
    }

    private class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        final List _testItemList;
        final List _semesterDetailList;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
            _testItemList = new ArrayList();
            _semesterDetailList = new ArrayList();
        }
        public int getTestItemIdx(final TestItem testItem) {
            return _testItemList.indexOf(testItem);
        }
        public int getSemesterDetailIdx(final SemesterDetail semesterDetail) {
//          log.debug(" semesterDetail = " + semesterDetail + " , " + _semesterDetailList.indexOf(semesterDetail));
          return _semesterDetailList.indexOf(semesterDetail);
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Semester)) {
                return 0;
            }
            Semester s = (Semester) o;
            return _semester.compareTo(s._semester);
        }
    }

    //成績タイトルの出力
    private void setRecTitle(final DB2UDB db2, final Vrw32alp svf, final String grHrClass) {
        String recTitle1 = "";
        String recTitle2 = "";
        String defPerfect = "";
        final Map map = gerHrClassPerfect(db2, grHrClass);
        for (final Iterator it = map.keySet().iterator(); it.hasNext();) {
            final String key = (String) it.next();
            final Map perfectMap = (Map) map.get(key);
            final String count = (String) perfectMap.get("COUNT");
            final String perfect = (String) perfectMap.get("PERFECT");
            if("".equals(recTitle1)) {
                recTitle1 = count+"科目"+perfect+"点満点（自由選択科目を除く）";
                defPerfect = count+perfect;
            } else {
                if(defPerfect.equals(count+perfect)) {
                    continue;
                } else {
                    recTitle2 = count+"科目"+perfect+"点満点（自由選択科目を除く）";
                }
            }
            if(!"".equals(recTitle1) && !"".equals(recTitle2)) break;
        }

        if(!"".equals(recTitle2)) {
            svf.VrsOut("REC_TITLE2_1", recTitle1);
            svf.VrsOut("REC_TITLE2_2", recTitle2);
        } else {
            svf.VrsOut("REC_TITLE1", recTitle1);
        }
    }

    //対象クラスの生徒が受講している科目数と満点計の取得
    private Map gerHrClassPerfect(final DB2UDB db2, final String grHrClass) {
        Map resultMap = new TreeMap() ;
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CHAIR_SUBCLASS AS( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T3.CLASSCD, ");
        stb.append("   T3.SCHOOL_KIND, ");
        stb.append("   T3.CURRICULUM_CD, ");
        stb.append("   T3.SUBCLASSCD ");
        stb.append(" FROM  ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   INNER JOIN CHAIR_STD_DAT T2 ");
        stb.append("           ON T2.YEAR     = T1.YEAR ");
        stb.append("          AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("          AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   INNER JOIN CHAIR_DAT T3 ");
        stb.append("           ON T3.YEAR     = T2.YEAR ");
        stb.append("          AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("          AND T3.CHAIRCD  = T2.CHAIRCD ");
        stb.append(" WHERE T1.YEAR     = '"+ _param._year +"'   ");
        stb.append("   AND T1.SEMESTER = '"+ _param._convsemester +"' ");
        stb.append("   AND T1.GRADE || T1.HR_CLASS = '"+ grHrClass +"' ");
        stb.append(" ), SUBCLASS_PERFECT AS( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SCHOOL_KIND, ");
        stb.append("   T1.CURRICULUM_CD, ");
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   CASE WHEN T3.PERFECT IS NOT NULL ");
        stb.append("        THEN T3.PERFECT ");
        stb.append("        ELSE '100' ");
        stb.append("   END AS PERFECT ");
        stb.append(" FROM ");
        stb.append("   CHAIR_SUBCLASS T1 ");
        stb.append("   LEFT JOIN PERFECT_RECORD_DAT T3 ");
        stb.append("          ON T3.YEAR          = T1.YEAR ");
        stb.append("         AND T3.SEMESTER      = '9' ");
        stb.append("         AND T3.TESTKINDCD    = '99' ");
        stb.append("         AND T3.TESTITEMCD    = '00' ");
        stb.append("         AND T3.CLASSCD       = T1.CLASSCD ");
        stb.append("         AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
        stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T3.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("         AND T3.DIV           = '01' ");
        stb.append("         AND T3.GRADE         = '00' ");
        stb.append("         AND T3.COURSECD      = '0' ");
        stb.append("         AND T3.MAJORCD       = '000' ");
        stb.append("         AND T3.COURSECODE    = '0000' ");
        stb.append(" ) ");
        stb.append("  SELECT ");
        stb.append("   SCHREGNO, ");
        stb.append("   COUNT(SUBCLASSCD) AS COUNT, ");
        stb.append("   SUM(PERFECT) AS PERFECT ");
        stb.append("  FROM ");
        stb.append("   SUBCLASS_PERFECT ");
        stb.append("  GROUP BY SCHREGNO ");
        stb.append("  ORDER BY COUNT");


        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Map map = new HashMap();
                map.put("SCHREGNO", StringUtils.defaultString(rs.getString("SCHREGNO")));
                map.put("COUNT", StringUtils.defaultString(rs.getString("COUNT"),"0"));
                map.put("PERFECT", StringUtils.defaultString(rs.getString("PERFECT"),"0"));
                final String key = StringUtils.defaultString(rs.getString("SCHREGNO"));
                resultMap.put(key, map);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return resultMap;
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
        log.fatal("$Revision: 74190 $ $Date: 2020-05-11 14:11:35 +0900 (月, 11 5 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private String _maxsemester;
        private final String _convsemester;
        private final String _programid;
        private final String _loginDate;
        private final String[] _categorySelected;
        private final String _nendo;
        private final String _grade;
        private final String _eDate;
        int yokuNen = 0;
        private Map _semesterMap;
        private Map _attendRanges;
        private final Map _attendParamMap;
        private final List<String> _d040NotTargetSubclasscdList;

        final String _schoolKind;
        final String _schoolKindName;
        final boolean _isFormJ;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("CTRL_YEAR");
            _grade = request.getParameter("GRADE");
            _semesterMap = loadSemester(db2);
            _semester = request.getParameter("SEMESTER");
            if (_maxsemester == null) _maxsemester = "3";
            _convsemester = SEMEALL.equals(request.getParameter("SEMESTER")) ? _maxsemester : request.getParameter("SEMESTER");
            _programid = request.getParameter("PRGID");
            _loginDate = request.getParameter("CTRL_DATE");
            yokuNen = Integer.parseInt(_year);
            String sNENDO = convZenkakuToHankaku(String.valueOf(yokuNen+1));
            _nendo = sNENDO + "年度";
            _categorySelected = request.getParameterValues("category_selected");
            _eDate = request.getParameter("EDATE").replace('/', '-');

            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");
            _d040NotTargetSubclasscdList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D040' ORDER BY NAME1 "), "NAME1");

            _schoolKind = getSchoolKind(db2);
            _schoolKindName = getSchoolKindName(db2);
            _isFormJ = "J".equals(_schoolKind);
        }
        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            String maxsemester = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + _year + "'"
                        + "   AND GRADE='" + _grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!SEMEALL.equals(rs.getString("SEMESTER"))) maxsemester = rs.getString("SEMESTER");
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            _maxsemester = maxsemester;
            return map;
        }

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private String getSchoolKindName(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = '" + _schoolKind + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
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

}

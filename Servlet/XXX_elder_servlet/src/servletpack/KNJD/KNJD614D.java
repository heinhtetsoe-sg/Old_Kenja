package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
public class KNJD614D {
    private static final String FORM_NAME = "KNJD614D_1.frm";
    private static final String FORM_NAME2 = "KNJD614D_2.frm";
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJD614D.class);

    private static final String SEMEALL = "9";

    private static final String OUTCOL0 = "04-0";
    private static final String OUTCOL1 = "04-1";
    private static final String BENE0 = "03-0";
    private static final String BENE1 = "03-1";
    private static final String SSUP0 = "02-0";
    private static final String SSUP1 = "02-1";
    private static final String SUND0 = "01-0";
    private static final String SUND1 = "01-1";
    private static final String KAWI0 = "05-0";
    private static final String KAWI1 = "05-1";

    private static final String PUTTYPE_OUTCOL = "1";
    private static final String PUTTYPE_BENE = "2";
    private static final String PUTTYPE_SSUP = "3";
    private static final String PUTTYPE_SUND = "4";
    private static final String PUTTYPE_KAWI = "5";

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
     * @return        対象データ存在フラグ(有り：TRUE、無し：FALSE)
     * @throws Exception
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {

        for (int i=0 ; i<_param._category_selected1.length ; i++) {
            final String grHrCd = _param._category_selected1[i];

            // 推薦名簿データ取得
            final Map students = createStudents(db2, grHrCd);
            final Map qualifyMap = getQualifiedInfo(db2, students);
            final Map mockMap =  getMockScore(db2, students);
            final Map result8020 = getCalcResult82(db2, students);

            // 帳票出力のメソッド
            outPutPrint(db2, svf, students, qualifyMap, mockMap, result8020);
        }
    }

    /**
     * 帳票出力処理
     * @param svf        帳票オブジェクト
     * @param student    帳票出力対象クラスオブジェクト
     * @param irow        帳票出力行数
     * @return        対象データ存在フラグ(有り：TRUE、無し：FALSE)
     */
    private void outPutPrint(final DB2UDB db2, final Vrw32alp svf, final Map students, final Map qualifyMap, final Map mockMap, final Map result8020) {
        final String[] test12Cds = {"1-0101", "1-0102", "1-9900", "2-0201", "2-0202", "2-9900", "3-0301", "3-9900", "9-9900"};
        final String[] ttl12TestCd = {"9-9900"};
        final String[] test3Cds = {"1-0101", "1-0102", "1-9900"};
        final String[] ttl3TestCd = {"9-9900"};

        final List testCdStr1 = Arrays.asList(test12Cds);
        final List ttlTestCdStr1 = Arrays.asList(ttl12TestCd);
        final List testCdStr3 = Arrays.asList(test3Cds);
        final List ttlTestCdStr3 = Arrays.asList(ttl3TestCd);

        final Map clsScoreMap = "1".equals(_param._category_is_inner_9_all) ? getClsScore(db2) : null;
        //1ページ目

        boolean prt1Flg = false;
        boolean prt2Flg = false;
        for (Iterator ite = students.keySet().iterator();ite.hasNext();) {
            final String schregno = (String)ite.next();
            final List schInfoList = (List)students.get(schregno);
            final Map clsCdMap = new LinkedMap();
            svf.VrSetForm(FORM_NAME, 4);
            if (schInfoList.size() > 0) {
                final student schInf = (student)schInfoList.get(schInfoList.size() - 1);
                setHead(db2, svf, schInf, schInfoList);
                prt1Flg = true;

                ////高1
                if ("1".equals(_param._category_is_inner_g1)) {
                    //高1の学籍番号が登録されている?
                    final student prtObj = (student)findGradeCd("01", schInfoList);
                    if (prtObj != null) {
                        //////素点
                        if ("1".equals(_param._category_is_inner_g1s1)) {
                            putTbl1(svf, "1", "01", testCdStr1, prtObj, "01", clsCdMap);
                            svf.VrEndRecord();
                        }
                        //////偏差値
                        ////////平均他
                        if ("1".equals(_param._category_is_inner_g1sh)) {
                            putTbl1(svf, "2", "01", testCdStr1, prtObj, "01", clsCdMap);
                            svf.VrEndRecord();
                        }
                        //////評定
                        ////////評定平均他
                        if ("1".equals(_param._category_is_inner_g1s9)) {
                            putTbl1(svf, "3", "01", ttlTestCdStr1, prtObj, "09", clsCdMap);
                            svf.VrEndRecord();
                        }
                    }
                }

                ////高2
                if ("1".equals(_param._category_is_inner_g2)) {
                    //高2の学籍番号が登録されている？
                    final student prtObj = (student)findGradeCd("02", schInfoList);
                    if (prtObj != null) {
                        //////素点
                        if ("1".equals(_param._category_is_inner_g2s1)) {
                            putTbl1(svf, "1", "02", testCdStr1, prtObj, "01", clsCdMap);
                            svf.VrEndRecord();
                        }
                        //////偏差値
                        ////////平均他
                        if ("1".equals(_param._category_is_inner_g2sh)) {
                            putTbl1(svf, "2", "02", testCdStr1, prtObj, "01", clsCdMap);
                            svf.VrEndRecord();
                        }
                        //////評定
                        ////////評定平均他
                        if ("1".equals(_param._category_is_inner_g2s9)) {
                            putTbl1(svf, "3", "02", ttlTestCdStr1, prtObj, "09", clsCdMap);
                            svf.VrEndRecord();
                        }
                    }
                }

                ////高3
                if ("1".equals(_param._category_is_inner_g3)) {
                    //高3の学籍番号が登録されている？
                    final student prtObj = (student)findGradeCd("03", schInfoList);
                    if (prtObj != null) {
                        //////素点
                        if ("1".equals(_param._category_is_inner_g3s1)) {
                            putTbl1(svf, "1", "03", testCdStr3, prtObj, "01", clsCdMap);
                            svf.VrEndRecord();
                        }
                        //////偏差値
                        if ("1".equals(_param._category_is_inner_g3sh)) {
                            putTbl1(svf, "2", "03", testCdStr3, prtObj, "01", clsCdMap);
                            svf.VrEndRecord();
                        }
                        //////評定
                        if ("1".equals(_param._category_is_inner_g3s9)) {
                            putTbl1(svf, "3", "03", ttlTestCdStr3, prtObj, "09", clsCdMap);
                            svf.VrEndRecord();
                        }
                    }
                }

                //高1～n評定合算
                if ("1".equals(_param._category_is_inner_9_all)) {
                    if (clsScoreMap.containsKey(schInf._schregno)) {
                        final Map getclsMap = (Map)clsScoreMap.get(schInf._schregno);
                        svf.VrsOut("TEST_NAME6", "2".equals(_param._category_is_inner_9_all123) ? "高1～高3評定合算" : "高1～高2評定合算");
                        int clsCol = 1;
                        BigDecimal hyoteiTtlCnt = new BigDecimal(0);
                        BigDecimal hyoteiTtlVal = new BigDecimal(0);
                        for (Iterator itk = getclsMap.keySet().iterator();itk.hasNext();) {
                            final String clsCd = (String)itk.next();
                            final ClsScoreData clsPutObj = (ClsScoreData)getclsMap.get(clsCd);
                            final BigDecimal hyoteiAvg = clsPutObj._cnt == 0 ? new BigDecimal("0") : new BigDecimal(clsPutObj._valuation).setScale(10, BigDecimal.ROUND_HALF_UP).divide((new BigDecimal(clsPutObj._cnt)).setScale(10, BigDecimal.ROUND_HALF_UP), BigDecimal.ROUND_HALF_UP).setScale(1, BigDecimal.ROUND_HALF_UP);
                            final BigDecimal hyoteiAvg2 = clsPutObj._cnt == 0 ? new BigDecimal("0") : new BigDecimal(clsPutObj._valuation).setScale(10, BigDecimal.ROUND_HALF_UP).divide((new BigDecimal(clsPutObj._cnt)).setScale(10, BigDecimal.ROUND_HALF_UP), BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP);
                            //if (clsCol < 8) {
                                svf.VrsOutn("SUBCLASS_NAME6", clsCol, clsPutObj._className);
                                svf.VrsOutn("SUBCLASS_NAME6_2", clsCol, "評定平均");
                                svf.VrsOutn("SCORE6_1_1", clsCol, hyoteiAvg.toString());
                                svf.VrsOutn("SCORE6_1_2", clsCol, "(" + hyoteiAvg2.toString() + ")");
                            //}
                            hyoteiTtlCnt = hyoteiTtlCnt.add(new BigDecimal(clsPutObj._cnt));
                            hyoteiTtlVal = hyoteiTtlVal.add(new BigDecimal(clsPutObj._valuation));
                            clsCol++;
                        }
                        ////総評定平均
                        svf.VrsOut("SCORE6_2_1", (hyoteiTtlCnt.equals(new BigDecimal(0)) ? "0.0" : hyoteiTtlVal.setScale(10, BigDecimal.ROUND_HALF_UP).divide(hyoteiTtlCnt, BigDecimal.ROUND_HALF_UP).setScale(1, BigDecimal.ROUND_HALF_UP).toString()));
                        svf.VrsOut("SCORE6_2_2", "(" + (hyoteiTtlCnt.equals(new BigDecimal(0)) ? "0.00" : hyoteiTtlVal.setScale(10, BigDecimal.ROUND_HALF_UP).divide(hyoteiTtlCnt, BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP).toString()) + ")");
                        svf.VrsOutn("SCORE6_2_3", 1, hyoteiTtlVal.toString());
                        svf.VrsOutn("SCORE6_2_3", 2, String.valueOf(hyoteiTtlCnt));
                        svf.VrEndRecord();
                    }
                }

                if (prt1Flg) {
                    svf.VrEndPage();
                    _hasData = true;
                }

                //2ページ目
                svf.VrSetForm(FORM_NAME2, 4);

                if ("1".equals(_param._category_is_prof_test) && mockMap.containsKey(schregno)) {
                    final Map mockDatMap = (Map)mockMap.get(schregno);
                    //2ページ目
                    //日大基礎学力テスト
                    if ("1".equals(_param._category_is_outer_college)) {
                        putTbl2(svf, PUTTYPE_OUTCOL, mockDatMap);
                    }

                    //進研模試・総合学力テスト
                    if ("1".equals(_param._category_is_benesse_test)) {
                        putTbl2(svf, PUTTYPE_BENE, mockDatMap);
                    }

                    //スタディサポート
                    if ("1".equals(_param._category_is_study_sup)) {
                        putTbl2(svf, PUTTYPE_SSUP, mockDatMap);
                    }

                    //駿台模試
                    if ("1".equals(_param._category_is_sundai)) {
                        putTbl2(svf, PUTTYPE_SUND, mockDatMap);
                    }

                    //河合塾
                    if ("1".equals(_param._category_is_kawai)) {
                        putTbl2(svf, PUTTYPE_KAWI, mockDatMap);
                    }
                }

                //資格
                if ("1".equals(_param._category_is_qualify)) {
                    if (qualifyMap.containsKey(schInf._schregno)) {
                        svf.VrsOut("TEST_NAME7", "資格");
                        prt2Flg = true;
                        svf.VrEndRecord();
                        final List putList = (List)qualifyMap.get(schInf._schregno);
                        int idxNo = 0;
                        for (Iterator itx = putList.iterator();itx.hasNext();) {
                            final QualifyData qObj = (QualifyData)itx.next();
                            idxNo++;
                            svf.VrsOut("NO", String.valueOf(idxNo));
                            svf.VrsOut("CERT_DATE", qObj._regddate == null ? "" : KNJ_EditDate.h_format_SeirekiJP(qObj._regddate));
                            svf.VrsOut("CERT_NAME", StringUtils.defaultString(qObj._qualified_Name, ""));
                            svf.VrsOut("CERT_ORG", StringUtils.defaultString(qObj._promoter, ""));
                            svf.VrsOut("CERT_RANK", StringUtils.defaultString(qObj._rankname, ""));
                            svf.VrsOut("CERT_SCORE", StringUtils.defaultString(qObj._score, ""));
                            svf.VrsOut("CERT_REMARK", StringUtils.defaultString(qObj._remark, ""));
                            svf.VrEndRecord();
                        }
                    }
                }
                //総合成績
                if ("1".equals(_param._category_is_inner_8020)) {
                    BigDecimal put1BD = null;
                    BigDecimal put2BD = null;
                    BigDecimal put3BD = null;
                    if (result8020.containsKey(schInf._schregno)) {
                        CalcResultData putCalcResObj = (CalcResultData)result8020.get(schInf._schregno);
                        svf.VrsOut("TEST_NAME8", "総合成績");
                        svf.VrsOut("TOTAL_SCORE_NAME1", "評定平均");
                        put1BD = null;
                        if (!"".equals(StringUtils.defaultString(putCalcResObj._test_Valuation_Avg, ""))) {
                            put1BD = new BigDecimal(putCalcResObj._test_Valuation_Avg).setScale(2, BigDecimal.ROUND_HALF_UP);
                            svf.VrsOut("TOTAL_SCORE1", put1BD.toString());
                        }
                        svf.VrEndRecord();
                        svf.VrsOut("TOTAL_SCORE_NAME2", "評定" + putCalcResObj._percentage + "％");
                        put2BD = null;
                        if (!"".equals(StringUtils.defaultString(putCalcResObj._test_Valuation_Percent_Score, ""))) {
                            put2BD = new BigDecimal(putCalcResObj._test_Valuation_Percent_Score).setScale(2, BigDecimal.ROUND_HALF_UP);
                            svf.VrsOut("TOTAL_SCORE2", put2BD.toString());
                        }
                        svf.VrEndRecord();
                        svf.VrsOut("TOTAL_SCORE_NAME2", "評定" + String.valueOf(100 - Integer.parseInt(putCalcResObj._percentage)) + "％");
                        put3BD = null;
                        if (!"".equals(StringUtils.defaultString(putCalcResObj._mock_Total_Percent_Score, ""))) {
                            put3BD = new BigDecimal(putCalcResObj._mock_Total_Percent_Score).setScale(2, BigDecimal.ROUND_HALF_UP);
                            svf.VrsOut("TOTAL_SCORE2", put3BD.toString());
                        }
                        svf.VrEndRecord();
                        svf.VrsOut("TOTAL_SCORE_NAME2", "総合成績");
                        svf.VrsOut("TOTAL_SCORE2", (put2BD != null && put3BD != null ? put2BD.add(put3BD).toString() : put2BD != null ? put2BD.toString() : put3BD != null ? put3BD.toString() : ""));
                        svf.VrEndRecord();
                    }
                }
                if (prt2Flg) {
                    svf.VrEndPage();
                    _hasData = true;
                }
            }
        }
    }

    private void putTbl1(final Vrw32alp svf, final String prtType, final String grdCd, final List testCdStr1, final student prtObj, final String getScoreDiv, final Map clsCdMap) {
        final String grdIdx = String.valueOf(Integer.parseInt(grdCd));
        final int fIdxBase = "03".equals(grdCd) ? ("3".equals(prtType) ? 0 : 3) : 0;
        final String fIdx = String.valueOf(Integer.parseInt(prtType) + fIdxBase);
        if ("1".equals(prtType)) {
            svf.VrsOut("TEST_NAME"+fIdx, "高" + grdIdx + "定期テスト");
        }
        boolean printHeadFlg = false;
        int tcCdIdx = 0;
        BigDecimal ttlVal = new BigDecimal("0.0");
        int ttlCnt = 0;
        for (Iterator itr = testCdStr1.iterator();itr.hasNext();) {
            final String testCds1 = (String)itr.next() + getScoreDiv;
            tcCdIdx++;
            if (prtObj._scoreMap.containsKey(testCds1)) {
                final Map sc1SubMap = (Map)prtObj._scoreMap.get(testCds1);
                int scCnt = 0;
                for (Iterator itp = prtObj._subclsMap.keySet().iterator();itp.hasNext();) {
                    final String subclsCd = (String)itp.next();
                    final SubclsData scObj = (SubclsData)prtObj._subclsMap.get(subclsCd);
                    scCnt++;
                    if (!printHeadFlg) {
                        final int scLen = KNJ_EditEdit.getMS932ByteLength(scObj._subclassAbbv);
                        if (scLen > 6) {
                            final String[] scName = KNJ_EditEdit.get_token(scObj._subclassAbbv, 6, 2);
                            svf.VrsOutn("SUBCLASS_NAME" + fIdx + "_2", scCnt, scName[0]);
                            svf.VrsOutn("SUBCLASS_NAME" + fIdx + "_3", scCnt, scName[1]);
                        } else {
                            svf.VrsOutn("SUBCLASS_NAME" + fIdx + "_1", scCnt, scObj._subclassAbbv);
                        }
                    }
                    if (sc1SubMap.containsKey(subclsCd)) {
                        final ScoreData scrObj = (ScoreData)sc1SubMap.get(subclsCd);
                        if (prtType == "1") {
                            svf.VrsOutn("SCORE" + fIdx + "_" + tcCdIdx, scCnt, scrObj._score);
                        } else if (prtType == "2") {
                            if (scrObj._grade_Deviation != null) {
                                BigDecimal ptWk = new BigDecimal(scrObj._grade_Deviation).setScale(2, BigDecimal.ROUND_HALF_UP);
                                svf.VrsOutn("SCORE" + fIdx + "_1_" + tcCdIdx, scCnt, ptWk.toString());
                            }
                        } else if (prtType == "3") {
                            if (scrObj._score != null) {
                                BigDecimal ptWk = new BigDecimal(scrObj._score).setScale(2, BigDecimal.ROUND_HALF_UP);
                                svf.VrsOutn("SCORE" + fIdx + "_" + tcCdIdx, scCnt, ptWk.toString());
                                ttlVal = ttlVal.add(ptWk);
                                ttlCnt++;

                                //ページ最後の評定合計の集計
                                if ("2".equals(_param._category_is_inner_9_all123) || ("1".equals(_param._category_is_inner_9_all123) && !"03".equals(grdCd))) {
                                    final String clsCdKey = scObj._classCd + "-" + scObj._schoolKind;
                                    if (!clsCdMap.containsKey(clsCdKey)) {
                                        final ClsCdData clsAddWK = new ClsCdData(clsCdKey, scObj._className);
                                        clsCdMap.put(clsCdKey, clsAddWK);
                                    }
                                    final ClsCdData clsWK = (ClsCdData)clsCdMap.get(clsCdKey);
                                    clsWK._kamokuCnt++;
                                    clsWK._ttlHyouka = clsWK._ttlHyouka.add(ptWk);
                                }
                            }
                        }
                    }
                }
                if (prtType == "2") {
                    ////////平均他
                    final String ttlSubclsCd = "99-H-99-999999";
                    if (sc1SubMap.containsKey(ttlSubclsCd)) {
                        final ScoreData scrObj = (ScoreData)sc1SubMap.get(ttlSubclsCd);
                        final BigDecimal putAvg = "".equals(StringUtils.defaultString(scrObj._avg)) ? null : (new BigDecimal(scrObj._avg)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        svf.VrsOutn("SCORE" + fIdx + "_2_" + tcCdIdx, 1, putAvg == null ? "" : putAvg.toString());
                        svf.VrsOutn("SCORE" + fIdx + "_2_" + tcCdIdx, 2, scrObj._class_Rank);
                        svf.VrsOutn("SCORE" + fIdx + "_2_" + tcCdIdx, 3, scrObj._course_Rank);
                        svf.VrsOutn("SCORE" + fIdx + "_2_" + tcCdIdx, 4, scrObj._grade_Rank);
                    }
                }
                if (prtType == "3") {
                    ////////評定平均他
                    svf.VrsOut("DIV_NAME", "評定平均 高" + grdIdx);
                    BigDecimal ttlAvg = ttlCnt == 0 ? new BigDecimal(0) : ttlVal.divide(new BigDecimal(ttlCnt), BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOutn("SCORE" + fIdx + "_2", 2, ttlAvg.toString());
                    svf.VrsOutn("SCORE" + fIdx + "_2", 3, ttlVal.toString());
                    svf.VrsOutn("SCORE" + fIdx + "_2", 4, String.valueOf(ttlCnt));
                }
                printHeadFlg = true;
            }
        }
        if (!printHeadFlg && prtType == "3") {
            //空行出力
            svf.VrsOut("DIV_NAME", "評定平均 高" + grdIdx);
        }
    }

    private void putTbl2(final Vrw32alp svf, final String prtType, final Map mockDatMap) {
        String getKey1 = "";
        String getKey2 = "";
        String putHeadTFieldName = "";
        String putScoreTFieldName = "";
        String putDatLineNameField = "";
        String putHeadSFieldName = "";
        String putScoreSFieldName = "";
        String putSumLineNameField = "";
        int putTHScoreFlg = 0;
        int putTHDevFlg = 0;
        int putTHTallDevFlg = 0;
        int putTHTRankFlg = 0;
        int putTDScoreFlg = 0;
        int putTDDevFlg = 0;
        int putTDRankFlg = 0;
        int putSHScoreFlg = 0;
        int putSHDevFlg = 0;
        int putSHallDevFlg = 0;
        int putSHGtzFlg = 0;
        int putSHschoolRankFlg = 0;
        int putSHRankFlg = 0;
        int putSDScoreFlg = 0;
        int putSDDevFlg = 0;
        int putSDGtzFlg = 0;
        int putSDschoolRankFlg = 0;
        int putSDRankFlg = 0;
        int cutTLen = 6;
        int cutSLen = 6;
        String[] filterStrs = null;
        Map useHeadMap = null;
        String putTtlField = "";
        String ttlStr = "";
        if (PUTTYPE_OUTCOL.equals(prtType)) {
            getKey1 = OUTCOL0;
            getKey2 = OUTCOL1;

            putTHScoreFlg = 1;
            putTHDevFlg = 1;

            putTDScoreFlg = 1;
            putTDDevFlg = 1;

            putSHScoreFlg = 1;
            putSHDevFlg = 1;
            putSHRankFlg = 1;

            putSDScoreFlg = 1;
            putSDDevFlg = 1;
            putSDRankFlg = 1;

            putDatLineNameField = "MOCK_NAME1";
            putSumLineNameField = "MOCK_NAME2";
            putHeadTFieldName = "SUBCLASS_NAME1";
            putHeadSFieldName = "SUBCLASS_NAME2_1";
            putScoreTFieldName = "SCORE1_1";
            putScoreSFieldName = "SCORE2_1";
            putTtlField = "TEST_NAME1";
            cutTLen = 8;
            cutSLen = 8;
            ttlStr = "日大基礎学力テスト";
            filterStrs = _param._category_selected2;
            useHeadMap = _param._outcolHeadMap;
        } else if (PUTTYPE_BENE.equals(prtType)) {
            getKey1 = BENE0;
            getKey2 = BENE1;

            putTHScoreFlg = 1;
            putTHTallDevFlg = 1;

            putTDScoreFlg = 1;
            putTDDevFlg = 1;

            putSHScoreFlg = 1;
            putSHallDevFlg = 1;
            putSHGtzFlg = 1;
            putSHschoolRankFlg = 1;
            putSHRankFlg = 1;

            putSDScoreFlg = 1;
            putSDDevFlg = 1;
            putSDGtzFlg = 1;
            putSDschoolRankFlg = 1;
            putSDRankFlg = 1;

            putDatLineNameField = "MOCK_NAME5";
            putSumLineNameField = "MOCK_NAME4";
            putHeadTFieldName = "SUBCLASS_NAME5";
            putHeadSFieldName = "SUBCLASS_NAME4_1";
            putScoreTFieldName = "SCORE5_1";
            putScoreSFieldName = "SCORE4_1";

            putTtlField = "TEST_NAME5";
            ttlStr = "進研模試・総合学力テスト";
            filterStrs = _param._category_selected3;
            useHeadMap = _param._beneHeadMap;
        } else if (PUTTYPE_SSUP.equals(prtType)) {
            getKey1 = SSUP0;
            getKey2 = SSUP1;

            putTHScoreFlg = 1;
            putTHTallDevFlg = 1;

            putTDScoreFlg = 1;
            putTDDevFlg = 1;

            putSHScoreFlg = 1;
            putSHallDevFlg = 1;
            putSHGtzFlg = 1;
            putSHschoolRankFlg = 1;

            putSDScoreFlg = 1;
            putSDDevFlg = 1;
            putSDGtzFlg = 1;
            putSDschoolRankFlg = 1;

            putDatLineNameField = "MOCK_NAME5";
            putSumLineNameField = "MOCK_NAME4";
            putHeadTFieldName = "SUBCLASS_NAME5";
            putHeadSFieldName = "SUBCLASS_NAME4_1";
            putScoreTFieldName = "SCORE5_1";
            putScoreSFieldName = "SCORE4_1";
            putTtlField = "TEST_NAME5";
            ttlStr = "スタディサポート";
            filterStrs = _param._category_selected4;
            useHeadMap = _param._ssupHeadMap;
        } else if (PUTTYPE_SUND.equals(prtType)) {
            getKey1 = SUND0;
            getKey2 = SUND1;

            putTHScoreFlg = 1;
            putTHDevFlg = 1;
            putTHTRankFlg = 1;

            putTDScoreFlg = 1;
            putTDDevFlg = 1;
            putTDRankFlg = 1;

            putSHScoreFlg = 1;
            putSHDevFlg = 1;
            putSHRankFlg = 1;

            putSDScoreFlg = 1;
            putSDDevFlg = 1;
            putSDRankFlg = 1;

            putDatLineNameField = "MOCK_NAME3";
            putSumLineNameField = "MOCK_NAME6";
            putHeadTFieldName = "SUBCLASS_NAME3";
            putHeadSFieldName = "SUBCLASS_NAME6_1";
            putScoreTFieldName = "SCORE3_1";
            putScoreSFieldName = "SCORE6_1";
            putTtlField = "TEST_NAME3";
            ttlStr = "駿台模試";
            filterStrs = _param._category_selected5;
            useHeadMap = _param._sundHeadMap;
        } else if (PUTTYPE_KAWI.equals(prtType)) {
            getKey1 = KAWI0;
            getKey2 = KAWI1;

            putTHScoreFlg = 1;
            putTHDevFlg = 1;

            putTDScoreFlg = 1;
            putTDDevFlg = 1;

            putSHScoreFlg = 1;
            putSHDevFlg = 1;
            putSHRankFlg = 1;

            putSDScoreFlg = 1;
            putSDDevFlg = 1;
            putSDRankFlg = 1;

            putDatLineNameField = "MOCK_NAME1";
            putSumLineNameField = "MOCK_NAME2";
            putHeadTFieldName = "SUBCLASS_NAME1";
            putHeadSFieldName = "SUBCLASS_NAME2_1";
            putScoreTFieldName = "SCORE1_1";
            putScoreSFieldName = "SCORE2_1";
            putTtlField = "TEST_NAME1";
            cutTLen = 8;
            cutSLen = 8;
            ttlStr = "河合塾";
            filterStrs = _param._category_selected6;
            useHeadMap = _param._kawiHeadMap;
        } else {
            return;
        }
        if (filterStrs == null) {
            return;
        }
        final List filterCodeChkList = Arrays.asList(filterStrs);
        Map outcolDatMap = null;
        Map outcolSumMap = null;
        if (mockDatMap.containsKey(getKey1)) {  //データが無ければヘッダ出力する科目が特定できないので、出力しない※選択科目を全て出すと、フォーマットを超えるはず。それは想定外のはず。集計結果も無いはず。
            outcolDatMap = (Map)mockDatMap.get(getKey1);

            if (mockDatMap.containsKey(getKey2)) {
                outcolSumMap = (Map)mockDatMap.get(getKey2);
            }
            //得点表(ヘッダ)
            boolean chkDExistFlg = false;
            for (Iterator itwv = outcolDatMap.keySet().iterator();itwv.hasNext();) {
                final String mockCd = (String)itwv.next();
                if (!filterCodeChkList.contains(mockCd)) {
                    continue;
                }
                final Map mockDetailMap = (Map)outcolDatMap.get(mockCd);
                if (mockDetailMap.size() > 0) {
                    chkDExistFlg = true;
                    break;
                }
            }
            if (chkDExistFlg) {
                svf.VrsOut(putTtlField, ttlStr);
            } else {
                return;  //出力するデータが無いので、(ヘッダを出力せずに)終了。
            }
            int putcol = 1;
            final Map putHeadMap = getHeadInfo(useHeadMap, "0", outcolDatMap, filterStrs);
            for (Iterator ity = putHeadMap.values().iterator();ity.hasNext();) {
                final MockHeadData headObj = (MockHeadData)ity.next();
                if (putTHScoreFlg > 0) {
                    putTitleOut(svf, putHeadTFieldName, putcol, headObj._tscore, cutTLen);
                    putcol++;
                }
                if (putTHDevFlg > 0) {
                    putTitleOut(svf, putHeadTFieldName, putcol, headObj._tdeviation, cutTLen);
                    putcol++;
                }
                if (putTHTallDevFlg > 0) {
                    putTitleOut(svf, putHeadTFieldName, putcol, headObj._tallDev, cutTLen);
                    putcol++;
                }
                if (putTHTRankFlg > 0) {
                    putTitleOut(svf, putHeadTFieldName, putcol, headObj._trank, cutTLen);
                    putcol++;
                }
            }
            svf.VrEndRecord();

            //得点表(データ)
            final List srchIdxList = new ArrayList(putHeadMap.keySet());
            putcol = 1;
            final int putCnt1 = putTDScoreFlg + putTDDevFlg + putTDRankFlg;
            for (Iterator itv = outcolDatMap.keySet().iterator();itv.hasNext();) {
                final String mockCd = (String)itv.next();
                if (!filterCodeChkList.contains(mockCd)) {
                    continue;
                }
                final Map mockDetailMap = (Map)outcolDatMap.get(mockCd);
                boolean putNameFlg = false;
                for (Iterator itx = mockDetailMap.keySet().iterator();itx.hasNext();) {
                    final String mockSubclsCd = (String)itx.next();
                      final MockData prtObj = (MockData)mockDetailMap.get(mockSubclsCd);
                    final int srchIdx = srchIdxList.indexOf(mockSubclsCd) + 1;
                    if (!putNameFlg) {
                        svf.VrsOut(putDatLineNameField, StringUtils.defaultString(prtObj._mockname1, ""));
                        putNameFlg = true;
                    }
                    if (srchIdx > 0) {
                        putcol = srchIdx * putCnt1 - (putCnt1 - 1);  //出力位置調整
                        if (putTDScoreFlg > 0) {
                            svf.VrsOutn(putScoreTFieldName, putcol, prtObj._score);
                            putcol++;
                        }
                        if (putTDDevFlg > 0) {
                            svf.VrsOutn(putScoreTFieldName, putcol, prtObj._deviation);
                            putcol++;
                        }
                        if (putTDRankFlg > 0) {
                            svf.VrsOutn(putScoreTFieldName, putcol, prtObj._rank);
                            putcol++;
                        }
                    }
                }
                svf.VrEndRecord();
            }

            //集計表(ヘッダ)
            final Map putHead2Map = getHeadInfo(useHeadMap, "1", outcolSumMap, filterStrs);
            putcol = 1;
            for (Iterator ity = putHead2Map.values().iterator();ity.hasNext();) {
                final MockHeadData headObj = (MockHeadData)ity.next();
                if (putSHScoreFlg > 0) {
                    putTitleOut(svf, putHeadSFieldName, putcol, headObj._tscore, cutSLen);
                    putcol++;
                }
                if (putSHDevFlg > 0 && "0".equals(headObj._tNot2DispFlg)) {
                    putTitleOut(svf, putHeadSFieldName, putcol, headObj._tdeviation, cutSLen);
                    putcol++;
                }
                if (putSHallDevFlg > 0 && "0".equals(headObj._tNot2DispFlg)) {
                    putTitleOut(svf, putHeadSFieldName, putcol, headObj._tallDev, cutSLen);
                    putcol++;
                }
                if (putSHGtzFlg > 0 && "0".equals(headObj._tNot2DispFlg)) {
                    putTitleOut(svf, putHeadSFieldName, putcol, headObj._tGtz, cutSLen);
                    putcol++;
                }
                if (putSHschoolRankFlg > 0 && "0".equals(headObj._tNot2DispFlg)) {
                    putTitleOut(svf, putHeadSFieldName, putcol, headObj._tSchoolRank, cutSLen);
                    putcol++;
                }
                if (putSHRankFlg > 0 && "0".equals(headObj._tNot2DispFlg)) {
                    putTitleOut(svf, putHeadSFieldName, putcol, headObj._trank, cutSLen);
                    putcol++;
                }
            }
            svf.VrEndRecord();

            //集計表(データ)
            final List srchIdx2List = new ArrayList(putHead2Map.keySet());
            final int putCnt2 = putSDScoreFlg + putSDDevFlg + putSDRankFlg;
            for (Iterator itv = outcolSumMap.keySet().iterator();itv.hasNext();) {
                final String mockCd = (String)itv.next();
                if (!filterCodeChkList.contains(mockCd)) {
                    continue;
                }
                final Map mockDetailMap = (Map)outcolSumMap.get(mockCd);
                boolean putNameFlg = false;
                for (Iterator itx = mockDetailMap.keySet().iterator();itx.hasNext();) {
                    final String mockSubclsCd = (String)itx.next();
                        final MockData prtObj = (MockData)mockDetailMap.get(mockSubclsCd);
                        final int srchIdx = srchIdx2List.indexOf(mockSubclsCd) + 1;
                        final MockHeadData headObj = (MockHeadData)putHead2Map.get(mockSubclsCd);
                        if (!putNameFlg) {
                            svf.VrsOut(putSumLineNameField, prtObj._mockname1);
                            putNameFlg = true;
                        }
                        if (srchIdx > 0) {
                            if ("0".equals(headObj._tNot2DispFlg)) {  //ヘッダの_tNot2DispFlgが"1"(河合塾、日大基礎学力テストで2レコード目に1項目だけ出力があるパターン)の場合は、前回の値を利用。
                                putcol = srchIdx * putCnt2 - (putCnt2 - 1);  //出力位置調整
                            }
                            if (putSDScoreFlg > 0) {
                                svf.VrsOutn(putScoreSFieldName, putcol, prtObj._score);
                                putcol++;
                            }
                            if (putSDDevFlg > 0 && "0".equals(headObj._tNot2DispFlg)) {
                                svf.VrsOutn(putScoreSFieldName, putcol, prtObj._deviation);
                                putcol++;
                            }
                            if (putSDGtzFlg > 0 && "0".equals(headObj._tNot2DispFlg)) {
                                svf.VrsOutn(putScoreSFieldName, putcol, prtObj._gtz);
                                putcol++;
                            }
                            if (putSDschoolRankFlg > 0 && "0".equals(headObj._tNot2DispFlg)) {
                                svf.VrsOutn(putScoreSFieldName, putcol, prtObj._inner_Rank);
                                putcol++;
                            }
                            if (putSDRankFlg > 0 && "0".equals(headObj._tNot2DispFlg)) {
                                svf.VrsOutn(putScoreSFieldName, putcol, prtObj._rank);
                                putcol++;
                            }
                        }
                }
                svf.VrEndRecord();
            }
        }
    }

    private Map getHeadInfo(final Map outcolHeadMap, final String putType, final Map outcolDatMap, final String[] csStrs) {
        Map retMap = new LinkedMap();
        if (!outcolHeadMap.containsKey(putType)) {
            return retMap;
        }

        final Map putTypeMap = (Map)outcolHeadMap.get(putType);
        for (Iterator itj = outcolDatMap.keySet().iterator();itj.hasNext();) {
            final String mkMockCd = (String)itj.next();
            final Map mockDetailMap = (Map)outcolDatMap.get(mkMockCd);
            for (Iterator itx = mockDetailMap.keySet().iterator();itx.hasNext();) {
                final String mkSubClsCd = (String)itx.next();
                if (putTypeMap.containsKey(mkSubClsCd)) {
                    MockHeadData chkWk = (MockHeadData)putTypeMap.get(mkSubClsCd);
                    chkWk._putTitleFlg = true;
                }
            }
        }
        for (Iterator itk = putTypeMap.values().iterator();itk.hasNext();) {
            final MockHeadData chkWk = (MockHeadData)itk.next();
            if (chkWk._putTitleFlg) {
                retMap.put(chkWk._mock_Subclass_Cd, chkWk);
            }
        }
        return retMap;
    }

    private void putTitleOut(final Vrw32alp svf, final String fieldFixName, final int col, final String putStr, final int cutLen) {
        final int hsLen = KNJ_EditEdit.getMS932ByteLength(putStr);
        //引数でcutLen==8なら8byte固定のフィールドに対する出力と判断。→つまり、4文字x2行(8Byte前/後)の判断をする。
        //引数でcutLen<8なら6/8byteを文字数で判断。→3文字(6Byte)以下は真ん中、4～6文字(7～12Byte)なら3文字x2行、7文字以降なら4文字x2行と判断する。
        if ((hsLen > 6 && cutLen < 8) || (hsLen > 8 && cutLen == 8)) {
            String fixFontField = "";
            int fixCutLen = cutLen;
            if (cutLen == 8) {
                fixFontField = "";
            } else if (hsLen > 12 && cutLen < 8) {
                fixFontField = "_2";
                fixCutLen = 8;
            } else {
                fixFontField = "_1";
            }
            final String[] hsCut = KNJ_EditEdit.get_token(putStr, fixCutLen, 2);
            svf.VrsOutn(fieldFixName + fixFontField + "_2", col, hsCut[0]);
            svf.VrsOutn(fieldFixName + fixFontField + "_3", col, hsCut[1]);
        } else {
            String fixFontField = "";
            if (cutLen < 8) {
                fixFontField = "_1";
            }
            svf.VrsOutn(fieldFixName + fixFontField + "_1", col, putStr);
        }
        return;
    }
    private student findGradeCd(final String findGrdCd, final List schInfoList) {
        student retWk = null;
        for (Iterator ite = schInfoList.iterator();ite.hasNext();) {
            final student chkWk = (student)ite.next();
            if (findGrdCd.equals(chkWk._gradeCd)) {
                retWk = chkWk;
                break;
            }
        }
        return retWk;
    }

    /**
     * ヘッダ・フッタ部の出力を行う
     * @param svf        帳票オブジェクト
     * @param sudent    帳票出力対象クラスオブジェクト
     */
    private void setHead(final DB2UDB db2, final Vrw32alp svf, final student schInf, final List schInfoList) {
        svf.VrsOut("TITLE", "個人成績票(" + _param._gradeName + ") [成績]");
        svf.VrsOut("SCHREGNO", schInf._schregno);
        svf.VrsOut("KANA", schInf._nameKana);
        svf.VrsOut("SCHOOL_KIND", "0".equals(schInf._inoutCd) ? "二中" : ("2".equals(schInf._entDiv) ? "推薦" : "一般"));
        svf.VrsOut("NAME", schInf._name);
        svf.VrsOut("SEX", schInf._sex);
        svf.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_JP(db2, _param._login_date));
        int grd = 1;
        for (Iterator ite = schInfoList.iterator();ite.hasNext();) {
            final student schObj = (student)ite.next();
            svf.VrsOut("HR_NAME" + String.valueOf(Integer.parseInt(schObj._gradeCd)), schObj._hr_Name);
            svf.VrsOut("ATTEND_NO" + String.valueOf(Integer.parseInt(schObj._gradeCd)), String.valueOf(Integer.parseInt(schObj._attendno)) + "番");
            svf.VrsOut("TR_NAME" + String.valueOf(Integer.parseInt(schObj._gradeCd)), schObj._staffname);
            svf.VrsOutn("GRADE1", grd, String.valueOf(grd));
            final Attendance att = getAttendInfo(db2, schObj._year, schObj._schregno);
            if (att != null) {
                svf.VrsOutn("LESSON", grd, String.valueOf(att._lesson));
                svf.VrsOutn("SUSPEND", grd, String.valueOf(att._suspend + att._mourning));
                svf.VrsOutn("ABROAD", grd, String.valueOf(att._transDays));
                svf.VrsOutn("MUST", grd, String.valueOf(att._mLesson));
                svf.VrsOutn("ABSENCE", grd, String.valueOf(att._sick));
                svf.VrsOutn("PRESENT", grd, String.valueOf(att._present));
                svf.VrsOutn("ATTEND_REMARK1", grd, schObj._attRemark);
            }
            grd++;
        }
    }

    /**
     * @param db2            ＤＢ接続オブジェクト
     * @param sbu_code        学部コード
     * @param ska_code        学科コード
     * @param sourse_div    学校コード
     * @return                帳票出力対象データリスト
     * @throws Exception
     */
    private Map createStudents(final DB2UDB db2, final String grHrCls) throws SQLException {

        final Map rtnMap = new LinkedMap();
        List subList = null;
        final String sql = getStudentSql(grHrCls);
        log.debug("createStudents sql:" + sql);
        db2.query(sql);
        ResultSet rs = null;
        rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String year = rs.getString("YEAR");
                final String grade = rs.getString("GRADE");
                final String gradeCd = rs.getString("GRADE_CD");
                final String grade_Name2 = rs.getString("GRADE_NAME2");
                final String hr_Class = rs.getString("HR_CLASS");
                final String hr_Name = rs.getString("HR_NAME");
                final String hr_NameAbbv = rs.getString("HR_NAMEABBV");
                final String attendno = rs.getString("ATTENDNO");
                final String staffname = rs.getString("STAFFNAME");
                final String sex = rs.getString("SEX");
                final String attRemark = rs.getString("ATTENDREC_REMARK");
                final String inoutCd = rs.getString("INOUTCD");
                final String entDiv = rs.getString("ENT_DIV");
                if (!rtnMap.containsKey(schregno)) {
                    subList = new ArrayList();
                    rtnMap.put(schregno, subList);
                } else {
                    subList = (List)rtnMap.get(schregno);
                }
                final student student = new student(schregno, name, nameKana, year, grade, gradeCd, grade_Name2, hr_Class, hr_Name, hr_NameAbbv, attendno, staffname, sex, attRemark, inoutCd, entDiv);
                subList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        getSubCls(db2, rtnMap);
        getScore(db2, rtnMap);
        return rtnMap;
    }

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @param sbu_code        学部コード
     * @param ska_code        学科コード
     * @param sourse_div    学校コード
     * @return                SQL文字列
     * @throws Exception
     */
    private String getStudentSql(final String grHrCls)
        {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_GRADESEMES AS ( ");
        stb.append(" SELECT ");
        stb.append("   GRADE, ");
        stb.append("   SCHREGNO, ");
        stb.append("   MAX(YEAR) AS YEAR, ");
        stb.append("   MAX(SEMESTER) AS SEMESTER ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT ");
        stb.append(" GROUP BY ");
        stb.append("   SCHREGNO, ");
        stb.append("   GRADE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T2.NAME, ");
        stb.append("   T2.NAME_KANA, ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.GRADE, ");
        stb.append("   T4.GRADE_CD, ");
        stb.append("   T4.GRADE_NAME2, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T5.HR_NAME, ");
        stb.append("   T5.HR_NAMEABBV, ");
        stb.append("   T1.ATTENDNO, ");
        stb.append("   T6.STAFFNAME, ");
        stb.append("   T7.ABBV1 AS SEX, ");
        stb.append("   T2.INOUTCD, ");
        stb.append("   T2.ENT_DIV, ");
        stb.append("   T8.ATTENDREC_REMARK ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_BASE_MST T2 ");
        stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   INNER JOIN MAX_GRADESEMES T3 ");
        stb.append("     ON T3.YEAR = T1.YEAR ");
        stb.append("    AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T4 ");
        stb.append("     ON T4.YEAR = T1.YEAR ");
        stb.append("    AND T4.GRADE = T1.GRADE ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT T5 ");
        stb.append("     ON T5.YEAR = T1.YEAR ");
        stb.append("    AND T5.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T5.GRADE = T1.GRADE ");
        stb.append("    AND T5.HR_CLASS = T1.HR_CLASS ");
        stb.append("   LEFT JOIN V_STAFF_MST T6 ");
        stb.append("     ON T6.YEAR = T5.YEAR ");
        stb.append("    AND T6.STAFFCD = T5.TR_CD1 ");
        stb.append("   LEFT JOIN V_NAME_MST T7 ");
        stb.append("     ON T7.YEAR = T1.YEAR ");
        stb.append("    AND T7.NAMECD1 = 'Z002' ");
        stb.append("    AND T7.NAMECD2 = T2.SEX ");
        stb.append("   LEFT JOIN HEXAM_ENTREMARK_DAT T8 ");
        stb.append("     ON T8.YEAR = T1.YEAR ");
        stb.append("    AND T8.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T8.ANNUAL = T1.ANNUAL ");
        stb.append(" WHERE ");
        stb.append("   T4.SCHOOL_KIND = 'H' ");
        stb.append("   AND T1.SCHREGNO IN ( ");
        stb.append("                SELECT ");
        stb.append("                  SCHREGNO ");
        stb.append("                FROM ");
        stb.append("                  SCHREG_REGD_DAT ");
        stb.append("                WHERE ");
        stb.append("                  YEAR = '" + _param._year + "' ");
        stb.append("                  AND GRADE || '-' || HR_CLASS = '" + grHrCls + "' ");
        stb.append(" ) ");
        stb.append(" ORDER BY ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.ATTENDNO, ");
        stb.append("   T1.SCHREGNO ");
        log.debug("帳票出力対象データ抽出 の SQL=" + stb.toString());
        return stb.toString();
    }

    private void getScore(final DB2UDB db2, final Map rtnMap) {
        Map subMap = null;
        for (Iterator ite = rtnMap.keySet().iterator();ite.hasNext();) {
            final String schregno = (String)ite.next();
            final List subList = (List)rtnMap.get(schregno);
            List years = new ArrayList();
            for (Iterator its = subList.iterator();its.hasNext();) {
                final student schInf = (student)its.next();
                years.add(schInf._year);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            final String psKeyStr = "getScore";

            try {
                if (!_param._psMap.containsKey(psKeyStr)) {
                    final String sql = getScoreSql();
                    log.info("get getScore sql() = " + sql);
                    _param._psMap.put(psKeyStr, db2.prepareStatement(sql));
                }

                ps = (PreparedStatement)_param._psMap.get(psKeyStr);
                for (Iterator itp = years.iterator();itp.hasNext();) {
                    final String year1 = (String)itp.next();
                    ps.setString(1, year1);
                    ps.setString(2, schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String year = rs.getString("YEAR");
                        final String semester = rs.getString("SEMESTER");
                        final String testkindcd = rs.getString("TESTKINDCD");
                        final String testitemcd = rs.getString("TESTITEMCD");
                        final String score_Div = rs.getString("SCORE_DIV");
                        final String subclscd = rs.getString("SUBCLSCD");
                        final String score = rs.getString("SCORE");
                        final String grade_Deviation = rs.getString("GRADE_DEVIATION");
                        final String avg = rs.getString("AVG");
                        final String class_Rank = rs.getString("CLASS_RANK");
                        final String course_Rank = rs.getString("COURSE_RANK");
                        final String grade_Rank = rs.getString("GRADE_RANK");
                        final ScoreData addwk = new ScoreData(year, semester, testkindcd, testitemcd, score_Div, subclscd, score, grade_Deviation, avg, class_Rank, course_Rank, grade_Rank);

                        final String fstKey = semester + "-" + testkindcd + testitemcd + score_Div;
                        final String sndKey = subclscd;
                        if (years.indexOf(year) >= 0) {
                            final student schObj = (student)subList.get(years.indexOf(year));
                            if (!schObj._scoreMap.containsKey(fstKey)) {
                                subMap = new LinkedMap();
                                schObj._scoreMap.put(fstKey, subMap);
                            } else {
                                subMap = (Map)schObj._scoreMap.get(fstKey);
                            }
                            subMap.put(sndKey, addwk);
                        }
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                //db2.commit();  //読むだけなので、何もしない。終了処理は最後に一括実施
            }
        }
    }

    private String getScoreSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   YEAR, ");
        stb.append("   SEMESTER, ");
        stb.append("   TESTKINDCD, ");
        stb.append("   TESTITEMCD, ");
        stb.append("   SCORE_DIV, ");
        stb.append("   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLSCD, ");
        stb.append("   SCORE, ");
        stb.append("   GRADE_DEVIATION, ");
        stb.append("   AVG, ");
        stb.append("   CLASS_RANK, ");
        stb.append("   COURSE_RANK, ");
        stb.append("   GRADE_RANK ");
        stb.append(" FROM ");
        stb.append("   RECORD_RANK_SDIV_DAT ");
        stb.append(" WHERE ");
        stb.append("   YEAR IN ? ");  //過年度分も一気に取得
        stb.append("   AND SCHOOL_KIND = 'H' "); //高校のみ
        stb.append("   AND SCORE_DIV IN ('01', '08', '09') ");
        stb.append("   AND SCHREGNO = ? ");
        stb.append(" ORDER BY ");
        stb.append("   SCHREGNO, ");
        stb.append("   YEAR, ");
        stb.append("   SCORE_DIV, ");
        stb.append("   SEMESTER, ");
        stb.append("   TESTKINDCD, ");
        stb.append("   TESTITEMCD, ");
        stb.append("   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ");
        return stb.toString();
    }

    private Map getMockScore(final DB2UDB db2, final Map rtnMap) {
        final Map mockDataMap = new LinkedMap();
        Map subMap = null;
        Map mockDetailMap = null;
        Map subclsDetailMap = null;
        for (Iterator ite = rtnMap.keySet().iterator();ite.hasNext();) {
            final String schregnoWk = (String)ite.next();

            final List subWkList = (List)rtnMap.get(schregnoWk);
            List years = new ArrayList();
            String prmMaxYear = "";
            String prmMinYear = "";
            for (Iterator its = subWkList.iterator();its.hasNext();) {
                final student schInf = (student)its.next();
                years.add(schInf._year);
                if ("".equals(prmMaxYear)) {
                    prmMaxYear = schInf._year;
                    prmMinYear = schInf._year;
                } else {
                    if (prmMaxYear.compareTo(schInf._year) < 0) {
                        prmMaxYear = schInf._year;
                    }
                    if (prmMinYear.compareTo(schInf._year) > 0) {
                        prmMinYear = schInf._year;
                    }
                }
            }

            final String psKeyStr = "getMockScore";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                if (!_param._psMap.containsKey(psKeyStr)) {
                    final String sql = getMockScoreSql(years);
                    log.info("getMockScore sql: " + sql);
                    _param._psMap.put(psKeyStr, db2.prepareStatement(sql));
                }

                ps = (PreparedStatement)_param._psMap.get(psKeyStr);
                ps.setString(1, prmMinYear);  //パラメータ指定はIN句に適用できないのでBETWEEN指定でやるように工夫。そのかわり、留年した年がBETWEEN間にあったら除外する。
                ps.setString(2, prmMaxYear);
                ps.setString(3, schregnoWk);
                // -- 以下、繰り返し設定する
                ps.setString(4, prmMinYear);  //パラメータ指定はIN句に適用できないのでBETWEEN指定でやるように工夫。そのかわり、留年した年がBETWEEN間にあったら除外する。
                ps.setString(5, prmMaxYear);
                ps.setString(6, schregnoWk);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    if (!years.contains(year)) {  //留年した年のデータを外す
                        continue;
                    }
                    final String gyoshatype = rs.getString("GYOSHATYPE");
                    final String mockcd = rs.getString("MOCKCD");
                    final String mockname1 = rs.getString("MOCKNAME1");
                    final String schregno = rs.getString("SCHREGNO");
                    final String mosi_Number = rs.getString("MOSI_NUMBER");
                    final String puttype = rs.getString("PUTTYPE");
                    final String mock_Subclass_Cd = rs.getString("MOCK_SUBCLASS_CD");
                    final String score = rs.getString("SCORE");
                    final String deviation = rs.getString("DEVIATION");
                    final String gtz = rs.getString("GTZ");
                    final String inner_Rank = rs.getString("INNER_RANK");
                    final String rank = rs.getString("RANK");

                    MockData addWk = new MockData(year, gyoshatype, mockcd, mockname1, schregno, mosi_Number, puttype, mock_Subclass_Cd, score, deviation, gtz, inner_Rank, rank);
                    if (!mockDataMap.containsKey(schregno)) {
                        subMap = new LinkedMap();
                        mockDataMap.put(schregno, subMap);
                    } else {
                        subMap = (Map)mockDataMap.get(schregno);
                    }
                    final String sndKey = mosi_Number + "-" + puttype;
                    if (!subMap.containsKey(sndKey)) {
                        mockDetailMap = new LinkedMap();
                        subMap.put(sndKey, mockDetailMap);
                    } else {
                        mockDetailMap = (Map)subMap.get(sndKey);
                    }
                    if (!mockDetailMap.containsKey(mockcd)) {
                        subclsDetailMap = new LinkedMap();
                        mockDetailMap.put(mockcd, subclsDetailMap);
                    } else {
                        subclsDetailMap = (Map)mockDetailMap.get(mockcd);
                    }
                    subclsDetailMap.put(mock_Subclass_Cd, addWk);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                //db2.commit();  //読むだけなので、何もしない。終了処理は最後に一括実施
            }
        }
        return mockDataMap;
    }
    private String getMockScoreSql(final List years) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH OTHER_RANKRANGE_AT AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   substr(T1.MOCKCD, 6,1) AS GYOSHATYPE, ");
        stb.append("   T1.MOCKCD, ");
        stb.append("   T3.MOCKNAME1, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   substr(T1.MOCK_SUBCLASS_CD, 1,2) AS MOSI_NUMBER, ");
        stb.append("   substr(T1.MOCK_SUBCLASS_CD, 3,1) AS PUTTYPE, ");
        stb.append("   T1.MOCK_SUBCLASS_CD, ");
        stb.append("   T1.SCORE, ");
        stb.append("   T1.DEVIATION, ");
        stb.append("   T1.GTZ, ");
        stb.append("   T1.RANK AS INNER_RANK ");
        stb.append(" FROM ");
        stb.append("   MOCK_RANK_RANGE_DAT T1 ");
        stb.append("   LEFT JOIN MOCK_MST T3 ");
        stb.append("    ON T3.MOCKCD = T1.MOCKCD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR BETWEEN ? AND ? ");
        stb.append("   AND T1.RANK_RANGE = '2' ");
        stb.append("   AND T1.RANK_DIV = '02' ");
        stb.append("   AND T1.MOCKDIV = '1' ");
        stb.append("   AND T1.SCHREGNO = ? ");
        stb.append(" ORDER BY ");
        stb.append("   T1.YEAR, ");
        stb.append("   MOCKCD, ");
        stb.append("   MOCK_SUBCLASS_CD ");
        stb.append(" ), OTHER_RANKRANGE_BT AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   substr(T1.MOCKCD, 6,1) AS GYOSHATYPE, ");
        stb.append("   T1.MOCKCD, ");
        stb.append("   T3.MOCKNAME1, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   substr(T1.MOCK_SUBCLASS_CD, 1,2) AS MOSI_NUMBER, ");
        stb.append("   substr(T1.MOCK_SUBCLASS_CD, 3,1) AS PUTTYPE, ");
        stb.append("   T1.MOCK_SUBCLASS_CD, ");
        stb.append("   T1.SCORE, ");
        stb.append("   T1.DEVIATION, ");
        stb.append("   T1.GTZ, ");
        stb.append("   T1.RANK ");
        stb.append(" FROM ");
        stb.append("   MOCK_RANK_RANGE_DAT T1 ");
        stb.append("   LEFT JOIN MOCK_MST T3 ");
        stb.append("    ON T3.MOCKCD = T1.MOCKCD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR BETWEEN ? AND ? ");
        stb.append("   AND T1.RANK_RANGE = '1' ");
        stb.append("   AND T1.RANK_DIV = '02' ");
        stb.append("   AND T1.MOCKDIV = '1' ");
        stb.append("   AND T1.SCHREGNO = ? ");
        stb.append(" ORDER BY ");
        stb.append("   T1.YEAR, ");
        stb.append("   MOCKCD, ");
        stb.append("   MOCK_SUBCLASS_CD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   CASE WHEN T1.YEAR IS NULL THEN T2.YEAR ELSE T1.YEAR END AS YEAR, ");
        stb.append("   CASE WHEN T1.GYOSHATYPE IS NULL THEN T2.GYOSHATYPE ELSE T1.GYOSHATYPE END AS GYOSHATYPE, ");
        stb.append("   CASE WHEN T1.MOCKCD IS NULL THEN T2.MOCKCD ELSE T1.MOCKCD END AS MOCKCD, ");
        stb.append("   CASE WHEN T1.MOCKNAME1 IS NULL THEN T2.MOCKNAME1 ELSE T1.MOCKNAME1 END AS MOCKNAME1, ");
        stb.append("   CASE WHEN T1.SCHREGNO IS NULL THEN T2.SCHREGNO ELSE T1.SCHREGNO END AS SCHREGNO, ");
        stb.append("   CASE WHEN T1.MOSI_NUMBER IS NULL THEN T2.MOSI_NUMBER ELSE T1.MOSI_NUMBER END AS MOSI_NUMBER, ");
        stb.append("   CASE WHEN T1.PUTTYPE IS NULL THEN T2.PUTTYPE ELSE T1.PUTTYPE END AS PUTTYPE, ");
        stb.append("   CASE WHEN T1.MOCK_SUBCLASS_CD IS NULL THEN T2.MOCK_SUBCLASS_CD ELSE T1.MOCK_SUBCLASS_CD END AS MOCK_SUBCLASS_CD, ");
        stb.append("   CASE WHEN T1.SCORE IS NULL THEN T2.SCORE ELSE T1.SCORE END AS SCORE, ");
        stb.append("   CASE WHEN T1.DEVIATION IS NULL THEN T2.DEVIATION ELSE T1.DEVIATION END AS DEVIATION, ");
        stb.append("   CASE WHEN T1.GTZ IS NULL THEN T2.GTZ ELSE T1.GTZ END AS GTZ, ");
        stb.append("   T1.INNER_RANK,");
        stb.append("   T2.RANK ");
        stb.append(" FROM ");
        stb.append("  OTHER_RANKRANGE_AT T1 ");
        stb.append("  FULL OUTER JOIN OTHER_RANKRANGE_BT T2 ");
        stb.append("    ON T2.YEAR = T1.YEAR ");
        stb.append("   AND T2.GYOSHATYPE = T1.GYOSHATYPE ");
        stb.append("   AND T2.MOCKCD = T1.MOCKCD ");
        stb.append("   AND T2.MOCKNAME1 = T1.MOCKNAME1 ");
        stb.append("   AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   AND T2.MOSI_NUMBER = T1.MOSI_NUMBER ");
        stb.append("   AND T2.PUTTYPE = T1.PUTTYPE ");
        stb.append("   AND T2.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        return stb.toString();
    }

    private Map getCalcResult82(final DB2UDB db2, final Map rtnMap) {
        final Map calcResMap = new LinkedMap();
        for (Iterator ite = rtnMap.keySet().iterator();ite.hasNext();) {
            final String schregnoWk = (String)ite.next();
            final String psKeyStr = "getCalcResult82";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                if (!_param._psMap.containsKey(psKeyStr)) {
                    final String sql = getCalcResult82Sql();
                    log.info("getCalcResult82 sql: " + sql);
                    _param._psMap.put(psKeyStr, db2.prepareStatement(sql));
                }

                ps = (PreparedStatement)_param._psMap.get(psKeyStr);
                ps.setString(1, schregnoWk);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String schregno = rs.getString("SCHREGNO");
                    final String test_Valuation_Avg = rs.getString("TEST_VALUATION_AVG");
                    final String test_Valuation_Percent_Score = rs.getString("TEST_VALUATION_PERCENT_SCORE");
                    final String mock_Total_Percent_Score = rs.getString("MOCK_TOTAL_PERCENT_SCORE");
                    final String percentage = rs.getString("PERCENTAGE");

                    CalcResultData addWk = new CalcResultData(year, schregno, test_Valuation_Avg, test_Valuation_Percent_Score, mock_Total_Percent_Score, percentage);
                    calcResMap.put(schregno, addWk);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                //db2.commit();  //読むだけなので、何もしない。終了処理は最後に一括実施
            }
        }
        return calcResMap;
    }

    private String getCalcResult82Sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" select ");
        stb.append("  T1.YEAR, ");
        stb.append("  T1.SCHREGNO, ");
        stb.append("  T1.TEST_VALUATION_AVG, ");
        stb.append("  T1.TEST_VALUATION_PERCENT_SCORE, ");
        stb.append("  T1.MOCK_TOTAL_PERCENT_SCORE, ");
        stb.append("  T2.PERCENTAGE ");
        stb.append(" FROM ");
        stb.append("  AFT_SCHREG_RECOMMENDATION_RANK_DAT T1 ");
        stb.append("  LEFT JOIN AFT_RECOMMENDATION_RANK_HEAD_DAT T2 ");
        stb.append("    ON T2.YEAR = T1.YEAR ");
        stb.append("   AND T2.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("  T1.YEAR = '" + _param._year + "' ");
        stb.append("  AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("  AND T1.SCHREGNO = ? ");
        return stb.toString();
    }

    private void getSubCls(final DB2UDB db2, final Map rtnMap) {
        for (Iterator ite = rtnMap.keySet().iterator();ite.hasNext();) {
            final String schregno = (String)ite.next();
            final List subList = (List)rtnMap.get(schregno);
            List years = new ArrayList();
            for (Iterator its = subList.iterator();its.hasNext();) {
                final student schInf = (student)its.next();
                years.add(schInf._year);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            final String psKeyStr = "getSubCls";

            try {
                if (!_param._psMap.containsKey(psKeyStr)) {
                    final String sql = getSubClsSql();
                    log.info("get getSubCls sql() = " + sql);
                    _param._psMap.put(psKeyStr, db2.prepareStatement(sql));
                }

                ps = (PreparedStatement)_param._psMap.get(psKeyStr);
                for (Iterator itp = years.iterator();itp.hasNext();) {
                    final String year1 = (String)itp.next();
                    ps.setString(1, year1);
                    ps.setString(2, schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String year = rs.getString("YEAR");
                        final String classCd = rs.getString("CLASSCD");
                        final String schoolKind = rs.getString("SCHOOL_KIND");
                        final String curriculumCd = rs.getString("CURRICULUM_CD");
                        final String subclassCd = rs.getString("SUBCLASSCD");
                        final String className = rs.getString("CLASSNAME");
                        final String subclassAbbv = rs.getString("SUBCLASSABBV");
                        final SubclsData addwk = new SubclsData(year, classCd, schoolKind, curriculumCd, subclassCd, className, subclassAbbv);

                        final String fstKey = classCd + "-" + schoolKind + "-"+ curriculumCd + "-" + subclassCd;

                        if (years.indexOf(year) >= 0) {
                            final student schObj = (student)subList.get(years.indexOf(year));
                            schObj._subclsMap.put(fstKey, addwk);
                        }
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                //db2.commit();  //読むだけなので、何もしない。終了処理は最後に一括実施
            }
        }
    }

    private String getSubClsSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T2.CLASSCD, ");
        stb.append("   T2.SCHOOL_KIND, ");
        stb.append("   T2.CURRICULUM_CD, ");
        stb.append("   T2.SUBCLASSCD, ");
        stb.append("   T3.SUBCLASSABBV, ");
        stb.append("   T4.CLASSNAME");
        stb.append(" FROM ");
        stb.append("   CHAIR_STD_DAT T1 ");
        stb.append("   LEFT JOIN CHAIR_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("   LEFT JOIN SUBCLASS_MST T3 ");
        stb.append("     ON T3.CLASSCD = T2.CLASSCD ");
        stb.append("    AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("    AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("    AND T3.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("   LEFT JOIN CLASS_MST T4 ");
        stb.append("     ON T4.CLASSCD = T2.CLASSCD ");
        stb.append("    AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = ? ");
        stb.append("   AND T1.SCHREGNO = ? ");
        stb.append("   AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD NOT IN ");
        stb.append("   ( ");
        stb.append("    SELECT ");
        stb.append("      TW.ATTEND_CLASSCD || '-' || TW.ATTEND_SCHOOL_KIND || '-' || TW.ATTEND_CURRICULUM_CD || '-' || TW.ATTEND_SUBCLASSCD ");
        stb.append("    FROM ");
        stb.append("      SUBCLASS_REPLACE_COMBINED_DAT TW ");
        stb.append("    WHERE ");
        stb.append("      TW.YEAR = T2.YEAR ");
        stb.append("      AND TW.COMBINED_CLASSCD || '-' || TW.COMBINED_SCHOOL_KIND || '-' || TW.COMBINED_CURRICULUM_CD || '-' || TW.COMBINED_SUBCLASSCD ");
        stb.append("          <> T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
        stb.append("   ) ");
        stb.append("   AND T2.CLASSCD < '90' ");
        stb.append(" ORDER BY ");
        stb.append("   T1.YEAR, ");
        stb.append("   T2.CLASSCD, ");
        stb.append("   T2.SCHOOL_KIND, ");
        stb.append("   T2.CURRICULUM_CD, ");
        stb.append("   T2.SUBCLASSCD ");
        return stb.toString();
    }

    private Attendance getAttendInfo(final DB2UDB db2, final String year, final String schregno) {
        if (!_param._semesMap.containsKey(year)) {
            return null;
        }
        final Map semInfo = _param.getSpecifyYearSemes(year);
        if (semInfo == null) {
            return null;
        }
        final String yStr = (String)semInfo.get("YEAR");
        final String semes = (String)semInfo.get("SEMESTER");
        final String sDate = (_param._year.equals(year)) ? _param._date_from : (String)semInfo.get("SDATE");  //当年度は指示画面の日付
        final String eDate = (_param._year.equals(year)) ? _param._date_to : (String)semInfo.get("EDATE");  //当年度は指示画面の日付

        if (semInfo == null || null == sDate || null == eDate || sDate.compareTo(eDate) > 0) {
            return null;
        }

        PreparedStatement psAtSeme = null;
        ResultSet rsAtSeme = null;

        _param._attendParamMap.put("schregno", "?");
        Attendance attendance = null;
        final String psKeyStr = "ATT" + yStr;
        try {
            if (!_param._psMap.containsKey(psKeyStr)) {
                log.info("get getAttendSemes sql_param = yStr = " + yStr + ", semes = " + semes + ", sDate = " + sDate + ", eDate = " + eDate + ";");
                final String sql = AttendAccumulate.getAttendSemesSql(
                        yStr,
                        semes,
                        sDate,
                        eDate,
                        _param._attendParamMap
                );
                log.info("get getAttendSemes sql(" + yStr + ") = " + sql);
                _param._psMap.put(psKeyStr, db2.prepareStatement(sql));
            }

            psAtSeme = (PreparedStatement)_param._psMap.get(psKeyStr);
            psAtSeme.setString(1, schregno);                 //生徒番号
            rsAtSeme = psAtSeme.executeQuery();
            while (rsAtSeme.next()) {
                if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                    continue;
                }
                attendance = new Attendance(
                        SEMEALL, //期間の集計値なので、固定値で指定。
                        rsAtSeme.getInt("LESSON"),        //授業日数
                        rsAtSeme.getInt("MLESSON"),       //出席すべき日数
                        rsAtSeme.getInt("SUSPEND"),       //出停
                        rsAtSeme.getInt("MOURNING"),      //忌引き
                        rsAtSeme.getInt("SICK"),          //欠席
                        rsAtSeme.getInt("SICK_ONLY"),     //欠席要因1
                        rsAtSeme.getInt("NOTICE_ONLY"),   //欠席要因2
                        rsAtSeme.getInt("NONOTICE_ONLY"), //欠席要因3
                        rsAtSeme.getInt("PRESENT"),       //出席日数
                        rsAtSeme.getInt("LATE"),          //遅刻
                        rsAtSeme.getInt("EARLY"),         //早退
                        rsAtSeme.getInt("TRANSFER_DATE")  //留学日数
                );
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            db2.commit();
        }
        return attendance;
    }

    private Map getQualifiedInfo(final DB2UDB db2, final Map rtnMap) {
        final Map qualifiedMap = new LinkedMap();
        List subList = null;
        for (Iterator ite = rtnMap.keySet().iterator();ite.hasNext();) {
            final String schregnoWk = (String)ite.next();
            final String psKeyStr = "getQualifiedInfo";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                if (!_param._psMap.containsKey(psKeyStr)) {
                    final String sql = getQualifiedInfoSql();
                    log.info("getQualifiedInfo sql() = " + sql);
                    _param._psMap.put(psKeyStr, db2.prepareStatement(sql));
                }

                ps = (PreparedStatement)_param._psMap.get(psKeyStr);
                ps.setString(1, schregnoWk);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String schregno = rs.getString("SCHREGNO");
                    final String seq = rs.getString("SEQ");
                    final String regddate = rs.getString("regddate");
                    final String qualified_Cd = rs.getString("qualified_cd");
                    final String qualified_Name = rs.getString("qualified_name");
                    final String promoter = rs.getString("promoter");
                    final String rank = rs.getString("rank");
                    final String rankname = rs.getString("RANKNAME");
                    final String score = rs.getString("score");
                    final String remark = rs.getString("remark");
                    QualifyData addWk = new QualifyData(year, schregno, seq, regddate, qualified_Cd, qualified_Name, promoter, rank, rankname, score, remark);
                    if (!qualifiedMap.containsKey(schregno)) {
                        subList = new ArrayList();
                        qualifiedMap.put(schregno, subList);
                    } else {
                        subList = (List)qualifiedMap.get(schregno);
                    }
                    subList.add(addWk);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                //db2.commit();  //読むだけなので、何もしない。終了処理は最後に一括実施
            }
        }
        return qualifiedMap;
    }

    private String getQualifiedInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG_YEAR AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("   T2.SCHOOL_KIND = 'H' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.SEQ, ");
        stb.append("   T1.regddate, ");
        stb.append("   T1.qualified_cd, ");
        stb.append("   T2.qualified_name, ");
        stb.append("   T2.promoter, ");
        stb.append("   T1.rank, ");
        stb.append("   H312.NAME1 AS RANKNAME, ");
        stb.append("   T1.score, ");
        stb.append("   T1.remark ");
        stb.append(" FROM ");
        stb.append("   schreg_qualified_hobby_dat T1 ");
        stb.append("   LEFT JOIN qualified_mst T2 ");
        stb.append("     ON T2.qualified_cd = T1.qualified_cd ");
        stb.append("   LEFT JOIN V_NAME_MST H312 ");
        stb.append("     ON H312.YEAR = T1.YEAR ");
        stb.append("    AND H312.NAMECD1 = 'H312' ");
        stb.append("    AND H312.NAMECD2 = T1.rank ");
        stb.append("   INNER JOIN SCHREG_YEAR T3 ");
        stb.append("     ON T3.YEAR = T1.YEAR ");
        stb.append("    AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   T1.SCHREGNO = ? ");
        stb.append(" ORDER BY ");
        stb.append("   YEAR, ");
        stb.append("   regddate ");
        return stb.toString();
    }

    private Map getClsScore(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        Map subMap = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getClsScoreSql();
            log.info("getClsScore sql() = " + sql);

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String classCd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String className = rs.getString("CLASSNAME");
                final int cnt = rs.getInt("CNT");
                final int valuation = rs.getInt("VALUATION");
                ClsScoreData addWk = new ClsScoreData(schregNo, classCd, schoolKind, className, cnt, valuation);
                if (!retMap.containsKey(schregNo)) {
                    subMap = new LinkedMap();
                    retMap.put(schregNo, subMap);
                } else {
                    subMap = (Map)retMap.get(schregNo);
                }
                final String sKey = classCd + "-" + schoolKind;
                subMap.put(sKey, addWk);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }
    private String getClsScoreSql() {
        final StringBuffer stb = new StringBuffer();
        final String getTestCdVal = SEMEALL + "-990009";
        stb.append(" WITH DELSKIPSCHREG_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   MAX(T1.YEAR) AS MYEAR, ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("   (T1.YEAR < '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER = (SELECT MAX(TW.SEMESTER) FROM SEMESTER_MST TW WHERE TW.YEAR = T1.YEAR AND TW.SEMESTER <> '" + SEMEALL + "') ");
        stb.append("   ) OR (T1.YEAR = '" + _param._year + "' AND T1.SEMESTER = '" + _param._semester + "') ");
        stb.append("   AND T1.SCHREGNO IN (SELECT TX.SCHREGNO FROM SCHREG_REGD_DAT TX WHERE TX.YEAR = '" + _param._year + "' AND TX.SEMESTER = '" + _param._semester + "' AND TX.GRADE || '-' || TX.HR_CLASS IN " + SQLUtils.whereIn(true, _param._category_selected1) + ") ");
        stb.append(" GROUP BY ");
        stb.append("   GRADE, ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   SCHREGNO ");
        stb.append(" ), USESCHREG_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   TN.* ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT TN ");
        stb.append("   INNER JOIN DELSKIPSCHREG_T TZ ");
        stb.append("     ON TZ.MYEAR = TN.YEAR ");
        stb.append("    AND TZ.SEMESTER = TN.SEMESTER ");
        stb.append("    AND TZ.SCHREGNO = TN.SCHREGNO ");
        stb.append(" ), RECSREC_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO ");
        stb.append("   , T1.CLASSCD ");
        stb.append("   , T1.SCHOOL_KIND ");
        stb.append("   , T1.CURRICULUM_CD ");
        stb.append("   , T1.SUBCLASSCD ");
        stb.append("   , SUM(T1.VALUATION) AS VALUATION ");
        stb.append(" FROM ");
        stb.append("   SCHREG_STUDYREC_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("   value(T1.PRINT_FLG, '0') <> '1' ");
        stb.append("   AND T1.YEAR < '" + _param._year + "' ");
        stb.append("   AND T1.VALUATION IS NOT NULL ");
        stb.append("   AND T1.SCHREGNO IN (SELECT TU.SCHREGNO FROM USESCHREG_T TU WHERE TU.YEAR = T1.YEAR) ");
        stb.append(" GROUP BY ");
        stb.append("   T1.SCHREGNO ");
        stb.append("   , T1.CLASSCD ");
        stb.append("   , T1.SCHOOL_KIND ");
        stb.append("   , T1.CURRICULUM_CD ");
        stb.append("   , T1.SUBCLASSCD ");
        if ("2".equals(_param._category_is_inner_9_all123)) {
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO ");
            stb.append("   , T1.CLASSCD ");
            stb.append("   , T1.SCHOOL_KIND ");
            stb.append("   , T1.CURRICULUM_CD ");
            stb.append("   , T1.SUBCLASSCD ");
            stb.append("   , SUM(T1.SCORE) AS VALUATION ");
            stb.append(" FROM ");
            stb.append("   RECORD_SCORE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _param._year + "' ");
            stb.append("   AND T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + getTestCdVal + "' ");
            stb.append("   AND T1.SCORE IS NOT NULL ");
            stb.append("   AND T1.SCHREGNO IN (SELECT TU.SCHREGNO FROM USESCHREG_T TU WHERE TU.YEAR = T1.YEAR) ");
            stb.append(" GROUP BY ");
            stb.append("   T1.SCHREGNO ");
            stb.append("   , T1.CLASSCD ");
            stb.append("   , T1.SCHOOL_KIND ");
            stb.append("   , T1.CURRICULUM_CD ");
            stb.append("   , T1.SUBCLASSCD ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO ");
        stb.append("   , T1.CLASSCD ");
        stb.append("   , T1.SCHOOL_KIND ");
        stb.append("   , T2.CLASSNAME ");
        stb.append("   , COUNT(SUBCLASSCD) AS CNT ");
        stb.append("   , SUM(T1.VALUATION) AS VALUATION ");
        stb.append(" FROM ");
        stb.append("   RECSREC_T T1 ");
        stb.append("   LEFT JOIN CLASS_MST T2 ");
        stb.append("     ON T2.CLASSCD = T1.CLASSCD ");
        stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append(" GROUP BY ");
        stb.append("   T1.SCHREGNO ");
        stb.append("   , T1.CLASSCD ");
        stb.append("   , T1.SCHOOL_KIND ");
        stb.append("   , T2.CLASSNAME ");
        stb.append(" ORDER BY ");
        stb.append("   T1.SCHREGNO ");
        stb.append("   , T1.CLASSCD ");
        return stb.toString();
    }

    /** 生徒クラス */
    private class student {
        final String _schregno;
        final String _name;
        final String _nameKana;
        final String _year;
        final String _grade;
        final String _gradeCd;
        final String _grade_Name2;
        final String _hr_Class;
        final String _hr_Name;
        final String _hr_NameAbbv;
        final String _attendno;
        final String _staffname;
        final String _sex;
        final String _attRemark;
        final String _inoutCd;
        final String _entDiv;
        final Map _scoreMap;
        final Map _subclsMap;

        public student (final String schregno, final String name, final String namekana, final String year, final String grade, final String gradeCd, final String grade_Name2, final String hr_Class, final String hr_Name, final String hr_NameAbbv, final String attendno, final String staffname, final String sex, final String attRemark, final String inoutCd, final String entDiv) {
            _schregno = schregno;
            _name = name;
            _nameKana = namekana;
            _year = year;
            _grade = grade;
            _gradeCd = gradeCd;
            _grade_Name2 = grade_Name2;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _hr_NameAbbv = hr_NameAbbv;
            _attendno = attendno;
            _staffname = staffname;
            _sex = sex;
            _attRemark = attRemark;
            _inoutCd = inoutCd;
            _entDiv = entDiv;
            _scoreMap = new LinkedMap();
            _subclsMap = new LinkedMap();
        }
    }

    private class Attendance {
        final String _semester;
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _sickOnly;
        final int _noticeOnly;
        final int _nonoticeOnly;
        final int _present;
        final int _late;
        final int _early;
        final int _transDays;
        Attendance(
                final String semester,
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int sickOnly,
                final int noticeOnly,
                final int nonoticeOnly,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _semester = semester;
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _sickOnly = sickOnly;
            _noticeOnly = noticeOnly;
            _nonoticeOnly = nonoticeOnly;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
        }
        private BigDecimal calcAttRate() {
            BigDecimal calcWk = new BigDecimal((double)((_mLesson - (_sickOnly + _noticeOnly + _nonoticeOnly)) / _mLesson)).setScale(1, BigDecimal.ROUND_HALF_UP);
            return calcWk;
        }
    }

    private class ScoreData {
        final String _year;
        final String _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _score_Div;
        final String _subclscd;
        final String _score;
        final String _grade_Deviation;
        final String _avg;
        final String _class_Rank;
        final String _course_Rank;
        final String _grade_Rank;
        public ScoreData (final String year, final String semester, final String testkindcd, final String testitemcd, final String score_Div, final String subclscd, final String score, final String grade_Deviation, final String avg, final String class_Rank, final String course_Rank, final String grade_Rank) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _score_Div = score_Div;
            _subclscd = subclscd;
            _score = score;
            _grade_Deviation = grade_Deviation;
            _avg = avg;
            _class_Rank = class_Rank;
            _course_Rank = course_Rank;
            _grade_Rank = grade_Rank;
        }
    }

    private class SubclsData {
        final String _year;
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _className;
        final String _subclassAbbv;
        public SubclsData (final String year, final String classCd, final String schoolKind, final String curriculumCd, final String subclassCd, final String className, final String subclassAbbv) {
            _year = year;
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _className = className;
            _subclassAbbv = subclassAbbv;
        }
    }

    private class ClsCdData {
        final String _clsCdKey;
        final String _className;
        int _kamokuCnt;
        BigDecimal _ttlHyouka;
        ClsCdData (final String clsCdKey, final String className) {
            _clsCdKey = clsCdKey;
            _className = className;
            _kamokuCnt = 0;
            _ttlHyouka = new BigDecimal(0);
        }
    }

    private class QualifyData {
        final String _year;
        final String _schregno;
        final String _seq;
        final String _regddate;
        final String _qualified_Cd;
        final String _qualified_Name;
        final String _promoter;
        final String _rank;
        final String _rankname;
        final String _score;
        final String _remark;
        public QualifyData (final String year, final String schregno, final String seq, final String regddate, final String qualified_Cd, final String qualified_Name, final String promoter, final String rank, final String rankname, final String score, final String remark)
        {
            _year = year;
            _schregno = schregno;
            _seq = seq;
            _regddate = regddate;
            _qualified_Cd = qualified_Cd;
            _qualified_Name = qualified_Name;
            _promoter = promoter;
            _rank = rank;
            _rankname = rankname;
            _score = score;
            _remark = remark;
        }
    }
    private class MockData {
        final String _year;
        final String _gyoshatype;
        final String _mockcd;
        final String _mockname1;
        final String _schregno;
        final String _mosi_Number;
        final String _puttype;
        final String _mock_Subclass_Cd;
        final String _score;
        final String _deviation;
        final String _gtz;
        final String _inner_Rank;
        final String _rank;
        public MockData (final String year, final String gyoshatype, final String mockcd, final String mockname1, final String schregno, final String mosi_Number, final String puttype, final String mock_Subclass_Cd, final String score, final String deviation, final String gtz, final String inner_Rank, final String rank)
        {
            _year = year;
            _gyoshatype = gyoshatype;
            _mockcd = mockcd;
            _mockname1 = mockname1;
            _schregno = schregno;
            _mosi_Number = mosi_Number;
            _puttype = puttype;
            _mock_Subclass_Cd = mock_Subclass_Cd;
            _score = score;
            _deviation = deviation;
            _gtz = gtz;
            _inner_Rank = inner_Rank;
            _rank = rank;
        }
    }

    private class MockHeadData {
        final String _puttype;
        final String _mock_Subclass_Cd;
        final String _tscore;
        final String _tdeviation;
        final String _tNot2DispFlg;  //河合塾、日大基礎学力テストのヘッダ出力で、score以外を出力しないレコードの場合、"1"が立つ。設定はSQL直で記載。
        final String _tallDev;
        final String _tGtz;
        final String _tSchoolRank;
        final String _trank;
        boolean _putTitleFlg;
        public MockHeadData (final String puttype, final String mock_Subclass_Cd, final String tscore, final String tdeviation, final String tNot2DispFlg, final String tallDev, final String tGtz, final String tSchoolRank, final String trank) {
            _puttype = puttype;
            _mock_Subclass_Cd = mock_Subclass_Cd;
            _tscore = tscore;
            _tdeviation = tdeviation;
            _tNot2DispFlg = tNot2DispFlg;
            _tallDev = tallDev;
            _tGtz = tGtz;
            _tSchoolRank = tSchoolRank;
            _trank = trank;
            _putTitleFlg = false;
        }
    }

    private class CalcResultData {
        final String _year;
        final String _schregno;
        final String _test_Valuation_Avg;
        final String _test_Valuation_Percent_Score;
        final String _mock_Total_Percent_Score;
        final String _percentage;
        public CalcResultData(final String year, final String schregno, final String test_Valuation_Avg, final String test_Valuation_Percent_Score, final String mock_Total_Percent_Score, final String percentage) {
            _year = year;
            _schregno = schregno;
            _test_Valuation_Avg = test_Valuation_Avg;
            _test_Valuation_Percent_Score = test_Valuation_Percent_Score;
            _mock_Total_Percent_Score = mock_Total_Percent_Score;
            _percentage = percentage;
        }
    }

    private class ClsScoreData {
        final String _schregNo;
        final String _classCd;
        final String _schoolKind;
        final String _className;
        final int _cnt;
        final int _valuation;
        public ClsScoreData (final String schregNo, final String classCd, final String schoolKind, final String className, final int cnt, final int valuation)
        {
            _schregNo = schregNo;
            _classCd = classCd;
            _schoolKind = schoolKind;
            _className = className;
            _cnt = cnt;
            _valuation = valuation;
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
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (2017/10/22 (日)) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _ctrl_seme;
        private final String _testcd;
        private final String _grade;
        private final String _gradeName;
        private final String _date_from;
        private final String _date_to;
        private final String[] _category_selected1;
        private final String _category_is_inner_g1;
        private final String _category_is_inner_g1s1;
        private final String _category_is_inner_g1sh;
        private final String _category_is_inner_g1s9;
        private final String _category_is_inner_g2;
        private final String _category_is_inner_g2s1;
        private final String _category_is_inner_g2sh;
        private final String _category_is_inner_g2s9;
        private final String _category_is_inner_g3;
        private final String _category_is_inner_g3s1;
        private final String _category_is_inner_g3sh;
        private final String _category_is_inner_g3s9;
        private final String _category_is_inner_9_all;
        private final String _category_is_inner_9_all123;
        private final String _category_is_outer_college;
        private final String _category_is_prof_test;
        private final String[] _category_selected2;
        private final String _category_is_benesse_test;
        private final String[] _category_selected3;
        private final String _category_is_study_sup;
        private final String[] _category_selected4;
        private final String _category_is_sundai;
        private final String[] _category_selected5;
        private final String _category_is_kawai;
        private final String[] _category_selected6;
        private final String _category_is_qualify;
        private final String _category_is_inner_8020;
        private final String _login_date;
        private final String _useCurriculumCd;

        private final Map _attendParamMap;
        private final Map _psMap;
        private Map _semesMap;

        private final Map _outcolHeadMap;
        private final Map _beneHeadMap;
        private final Map _ssupHeadMap;
        private final Map _sundHeadMap;
        private final Map _kawiHeadMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrl_seme = request.getParameter("CTRL_SEME");
            _testcd = request.getParameter("TESTCD");
            _grade = request.getParameter("GRADE");
            _date_from = request.getParameter("DATE_FROM");
            _date_to = request.getParameter("DATE_TO");

            //クラス選択
            _category_selected1 = request.getParameterValues("CATEGORY_SELECTED1");

            //高校1年
            _category_is_inner_g1 = request.getParameter("CATEGORY_IS_INNER_G1");
            _category_is_inner_g1s1 = request.getParameter("CATEGORY_IS_INNER_G1S1");
            _category_is_inner_g1sh = request.getParameter("CATEGORY_IS_INNER_G1SH");
            _category_is_inner_g1s9 = request.getParameter("CATEGORY_IS_INNER_G1S9");

            //高校2年
            _category_is_inner_g2 = request.getParameter("CATEGORY_IS_INNER_G2");
            _category_is_inner_g2s1 = request.getParameter("CATEGORY_IS_INNER_G2S1");
            _category_is_inner_g2sh = request.getParameter("CATEGORY_IS_INNER_G2SH");
            _category_is_inner_g2s9 = request.getParameter("CATEGORY_IS_INNER_G2S9");

            //高校3年
            _category_is_inner_g3 = request.getParameter("CATEGORY_IS_INNER_G3");
            _category_is_inner_g3s1 = request.getParameter("CATEGORY_IS_INNER_G3S1");
            _category_is_inner_g3sh = request.getParameter("CATEGORY_IS_INNER_G3SH");
            _category_is_inner_g3s9 = request.getParameter("CATEGORY_IS_INNER_G3S9");

            //評定合算
            _category_is_inner_9_all = request.getParameter("CATEGORY_IS_INNER_9_ALL");
            _category_is_inner_9_all123 = request.getParameter("CATEGORY_IS_INNER_9_ALL12_123");

            //日本大学基礎学力到達度テスト
            _category_is_outer_college = request.getParameter("CATEGORY_IS_OUTER_COLLEGE");
            _category_selected2 = request.getParameterValues("CATEGORY_SELECTED2");

            //実力テスト
            _category_is_prof_test = request.getParameter("CATEGORY_IS_PROF_TEST");
            //ベネッセ
            _category_is_benesse_test = request.getParameter("CATEGORY_IS_BENESSE_TEST");
            _category_selected3 = request.getParameterValues("CATEGORY_SELECTED3");

            //スタディサポート
            _category_is_study_sup = request.getParameter("CATEGORY_IS_STUDY_SUP");
            _category_selected4 = request.getParameterValues("CATEGORY_SELECTED4");

            //駿台
            _category_is_sundai = request.getParameter("CATEGORY_IS_SUNDAI");
            _category_selected5 = request.getParameterValues("CATEGORY_SELECTED5");

            //河合塾
            _category_is_kawai = request.getParameter("CATEGORY_IS_KAWAI");
            _category_selected6 = request.getParameterValues("CATEGORY_SELECTED6");

            //資格
            _category_is_qualify = request.getParameter("CATEGORY_IS_QUALIFY");
            //学内成績(評定80実力20)
            _category_is_inner_8020 = request.getParameter("CATEGORY_IS_INNER_8020");

            _login_date = request.getParameter("LOGIN_DATE");
            _useCurriculumCd = request.getParameter("useCurriculumcd");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", _useCurriculumCd);

            _semesMap = getSemesterMap(db2);

            _psMap = new HashMap();

            _outcolHeadMap = getOutColHeadInfo(db2);
            _beneHeadMap = getBeneHeadInfo(db2);
            _ssupHeadMap = getSSupHeadInfo(db2);
            _sundHeadMap = getSundHeadInfo(db2);
            _kawiHeadMap = getKawiHeadInfo(db2);
            _gradeName = getGradeName(db2);
        }

        private String getGradeName(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
        }
        private Map getSemesterMap(final DB2UDB db2) {
            final String sql = " SELECT YEAR, SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR < '" + _year + "' AND SEMESTER = '9' UNION ALL SELECT YEAR, SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER <= '" + _semester + "' ORDER BY YEAR, SEMESTER ";
            log.debug("getSemesterMap sql:"+ sql);
            final List Lst = KnjDbUtils.query(db2, sql);
            final Map rtn = new LinkedMap();
            String maxStr = "";
            for (Iterator ite = Lst.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                final String wkSems = (String)row.get("SEMESTER");
                if ("".equals(maxStr)) {
                    maxStr = wkSems;
                } else if (Integer.parseInt(wkSems) > Integer.parseInt(maxStr)) {
                    maxStr = wkSems;
                }
                rtn.put(row.get("YEAR"), row);
            }
            return rtn;
        }

        //指定年度の通年学期情報(!!当年度はCTRL_SEMESTERまで!!)を取得する。※当年度は、SDATEが
        private Map getSpecifyYearSemes(final String year) {
            Map retMap = null;
            String fstSDate = "";
            for (Iterator ite = _semesMap.keySet().iterator();ite.hasNext();) {
                final String kStr = (String)ite.next();
                if (kStr.length() >= 4) {
                    final String yStr = kStr.substring(0,4);
                    if (year.equals(yStr)) {
                        retMap = (Map)_semesMap.get(yStr);
                        if (!year.equals(_year)) {
                            break;
                        } else {
                            if ("".equals(fstSDate)) {
                                fstSDate = (String)retMap.get("SDATE");
                            }
                        }
                    }
                }
            }
            //_semesMapはソートされているので、当年度であれば、ループの最後がCTRL_SEMESTERのデータ。ただし、開始日は最初のSDATEに変える
            if (year.equals(_year) && retMap != null) {
                retMap.put("SDATE", fstSDate);
            }
            return retMap;
        }

        private Map getOutColHeadInfo(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            Map subMap = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH NICHIDAI_BASE_SUMFIX(PUTTYPE, MOCK_SUBCLASS_CD, TSCORE, TDEVIATION, TNOT2DISPFLG) AS ( ");
            stb.append(" VALUES('1', '041001', '3科合計素点', '3科合計化点', '0') ");
            stb.append(" UNION ");
            stb.append(" VALUES('1', '041002', '3科日大順位', '', '1') ");
            stb.append(" UNION ");
            stb.append(" VALUES('1', '041003', '5/4科合計素点', '5/4科合計化点', '0') ");
            stb.append(" UNION ");
            stb.append(" VALUES('1', '041004', '5/4科日大順位', '', '1') ");
            stb.append(" ), DATA_SUM_BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("   ROW_NUMBER() OVER(PARTITION BY T1.MOCK_SUBCLASS_CD) AS RNUM, ");
            stb.append("   CASE WHEN LENGTH(T1.MOCK_SUBCLASS_CD) > 3 THEN SUBSTR(T1.MOCK_SUBCLASS_CD, 3, 1) ELSE NULL END AS PUTTYPE, ");
            stb.append("   T1.MOCK_SUBCLASS_CD, ");
            stb.append("   T1.TSCORE, ");
            stb.append("   T1.TDEVIATION, ");
            stb.append("   '0' AS TNOT2DISPFLG ");
            stb.append(" FROM ");
            stb.append("   mock_csv_zkai_score_head_dat T1 ");
            stb.append(" WHERE ");
            stb.append("   YEAR BETWEEN '"+String.valueOf(Integer.parseInt(_year) - 2)+"' AND '" + _year + "' ");  //過去3年分
            stb.append(" GROUP BY ");
            stb.append("   T1.MOSI_CD, ");
            stb.append("   T1.MOCK_SUBCLASS_CD, ");
            stb.append("   T1.TSCORE, ");
            stb.append("   T1.TDEVIATION ");
            stb.append(" ORDER BY ");
            stb.append("   T1.MOSI_CD, ");
            stb.append("   T1.MOCK_SUBCLASS_CD ");
            stb.append(" ), DATA_SUM_TBL AS ( ");
            stb.append(" SELECT ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   NICHIDAI_BASE_SUMFIX ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("   T1.PUTTYPE, ");
            stb.append("   T1.MOCK_SUBCLASS_CD, ");
            stb.append("   T1.TSCORE, ");
            stb.append("   T1.TDEVIATION, ");
            stb.append("   T1.TNOT2DISPFLG ");
            stb.append(" FROM ");
            stb.append("   DATA_SUM_BASE T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.MOCK_SUBCLASS_CD IS NOT NULL ");
            stb.append("   AND T1.RNUM = 1 ");  //MOCK_SUBCLASS_CDで見つかったものの中で、MOSI_CDの1番若い名称を取得
            stb.append("   AND SUBSTR(T1.MOCK_SUBCLASS_CD, 1, 2) = '04' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   DATA_SUM_TBL T1 ");
            stb.append(" ORDER BY ");
            stb.append("   CASE WHEN LENGTH(T1.MOCK_SUBCLASS_CD) > 3 THEN SUBSTR(T1.MOCK_SUBCLASS_CD, 3, 1) ELSE NULL END, ");
            stb.append("   T1.MOCK_SUBCLASS_CD ");
            log.debug("getOutColHeadInfo sql:"+ stb.toString());
            final List Lst = KnjDbUtils.query(db2, stb.toString());
            for (Iterator ite = Lst.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                final String puttype = (String)row.get("PUTTYPE");

                if (!retMap.containsKey(puttype)) {
                    subMap = new LinkedMap();
                    retMap.put(puttype, subMap);
                } else {
                    subMap = (Map)retMap.get(puttype);
                }
                final String mock_Subclass_Cd = (String)row.get("MOCK_SUBCLASS_CD");
                final String tscore = (String)row.get("TSCORE");
                final String tdeviation = (String)row.get("TDEVIATION");
                final String tNot2DispFlg = (String)row.get("TNOT2DISPFLG");
                MockHeadData addWk = new MockHeadData(puttype, mock_Subclass_Cd, tscore, tdeviation, tNot2DispFlg, "", "", "", "");
                subMap.put(mock_Subclass_Cd, addWk);
            }
            return retMap;
        }

        private Map getBeneHeadInfo(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            Map subMap = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH DATA_SUM_BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("   ROW_NUMBER() OVER(PARTITION BY T1.MOCK_SUBCLASS_CD) AS RNUM, ");
            stb.append("   CASE WHEN LENGTH(T1.MOCK_SUBCLASS_CD) > 3 THEN SUBSTR(T1.MOCK_SUBCLASS_CD, 3, 1) ELSE NULL END AS PUTTYPE, ");
            stb.append("   T1.MOCK_SUBCLASS_CD, ");
            stb.append("   T1.TSCORE, ");
            stb.append("   T1.TALL_DEV, ");
            stb.append("   T1.TGTZ, ");
            stb.append("   T1.TSCHOOL_RANK, ");
            stb.append("   T1.TALL_RANK ");
            stb.append(" FROM ");
            stb.append("   mock_csv_bene_score_head_dat  T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR BETWEEN '"+String.valueOf(Integer.parseInt(_year) - 2)+"' AND '" + _year + "' ");  //過去3年分
            stb.append("   AND T1.MOCK_SUBCLASS_CD IS NOT NULL ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   PUTTYPE, ");
            stb.append("   MOCK_SUBCLASS_CD, ");
            stb.append("   TSCORE, ");
            stb.append("   TALL_DEV, ");
            stb.append("   TGTZ, ");
            stb.append("   TSCHOOL_RANK, ");
            stb.append("   TALL_RANK ");
            stb.append(" FROM ");
            stb.append("   DATA_SUM_BASE ");
            stb.append(" WHERE ");
            stb.append("   RNUM = 1 ");
            stb.append("   AND SUBSTR(MOCK_SUBCLASS_CD, 1, 2) = '03' ");
            stb.append(" ORDER BY ");
            stb.append("   PUTTYPE, ");
            stb.append("   MOCK_SUBCLASS_CD ");

            log.debug("getBeneHeadInfo sql:"+ stb.toString());
            final List Lst = KnjDbUtils.query(db2, stb.toString());
            for (Iterator ite = Lst.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                final String puttype = (String)row.get("PUTTYPE");

                if (!retMap.containsKey(puttype)) {
                    subMap = new LinkedMap();
                    retMap.put(puttype, subMap);
                } else {
                    subMap = (Map)retMap.get(puttype);
                }
                final String mock_Subclass_Cd = (String)row.get("MOCK_SUBCLASS_CD");
                final String tscore = (String)row.get("TSCORE");
                final String tall_Dev = (String)row.get("TALL_DEV");
                final String tgtz = (String)row.get("TGTZ");
                final String tschool_Rank = (String)row.get("TSCHOOL_RANK");
                final String tall_Rank = (String)row.get("TALL_RANK");
                MockHeadData addWk = new MockHeadData(puttype, mock_Subclass_Cd, tscore, "", "0", tall_Dev, tgtz, tschool_Rank, tall_Rank);
                subMap.put(mock_Subclass_Cd, addWk);
            }
            return retMap;
        }

        private Map getSSupHeadInfo(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            Map subMap = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH DATA_SUM_BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("   ROW_NUMBER() OVER(PARTITION BY T1.MOCK_SUBCLASS_CD) AS RNUM, ");
            stb.append("   CASE WHEN LENGTH(T1.MOCK_SUBCLASS_CD) > 3 THEN SUBSTR(T1.MOCK_SUBCLASS_CD, 3, 1) ELSE NULL END AS PUTTYPE, ");
            stb.append("   T1.MOCK_SUBCLASS_CD, ");
            stb.append("   T1.TSCORE, ");
            stb.append("   T1.TALL_DEV, ");
            stb.append("   T1.TGTZ, ");
            stb.append("   T1.TSCHOOL_RANK, ");
            stb.append("   T1.TALL_RANK ");
            stb.append(" FROM ");
            stb.append("   mock_csv_bene_score_head_dat  T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR BETWEEN '"+String.valueOf(Integer.parseInt(_year) - 2)+"' AND '" + _year + "' ");  //過去3年分
            stb.append("   AND T1.MOCK_SUBCLASS_CD IS NOT NULL ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   PUTTYPE, ");
            stb.append("   MOCK_SUBCLASS_CD, ");
            stb.append("   TSCORE, ");
            stb.append("   TALL_DEV, ");
            stb.append("   TGTZ, ");
            stb.append("   TSCHOOL_RANK, ");
            stb.append("   TALL_RANK ");
            stb.append(" FROM ");
            stb.append("   DATA_SUM_BASE ");
            stb.append(" WHERE ");
            stb.append("   RNUM = 1 ");
            stb.append("   AND SUBSTR(MOCK_SUBCLASS_CD, 1, 2) = '02' ");
            stb.append(" ORDER BY ");
            stb.append("   PUTTYPE, ");
            stb.append("   MOCK_SUBCLASS_CD ");

            log.debug("getSSupHeadInfo sql:"+ stb.toString());
            final List Lst = KnjDbUtils.query(db2, stb.toString());
            for (Iterator ite = Lst.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                final String puttype = (String)row.get("PUTTYPE");

                if (!retMap.containsKey(puttype)) {
                    subMap = new LinkedMap();
                    retMap.put(puttype, subMap);
                } else {
                    subMap = (Map)retMap.get(puttype);
                }
                final String mock_Subclass_Cd = (String)row.get("MOCK_SUBCLASS_CD");
                final String tscore = (String)row.get("TSCORE");
                final String tall_Dev = (String)row.get("TALL_DEV");
                final String tgtz = (String)row.get("TGTZ");
                final String tschool_Rank = (String)row.get("TSCHOOL_RANK");
                final String tall_Rank = (String)row.get("TALL_RANK");
                MockHeadData addWk = new MockHeadData(puttype, mock_Subclass_Cd, tscore, "", "0", tall_Dev, tgtz, tschool_Rank, tall_Rank);
                subMap.put(mock_Subclass_Cd, addWk);
            }
            return retMap;
        }
        private Map getSundHeadInfo(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            Map subMap = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH DATA_SUM_BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("   ROW_NUMBER() OVER(PARTITION BY T1.MOCK_SUBCLASS_CD) AS RNUM, ");
            stb.append("   CASE WHEN LENGTH(T1.MOCK_SUBCLASS_CD) > 3 THEN SUBSTR(T1.MOCK_SUBCLASS_CD, 3, 1) ELSE NULL END AS PUTTYPE, ");
            stb.append("   T1.MOCK_SUBCLASS_CD, ");
            stb.append("   T1.TSCORE, ");
            stb.append("   T1.TDEVIATION, ");
            stb.append("   T1.TRANK ");
            stb.append(" FROM ");
            stb.append("   mock_csv_sundai_score_head_dat  T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR BETWEEN '"+String.valueOf(Integer.parseInt(_year) - 2)+"' AND '" + _year + "' ");  //過去3年分
            stb.append("   AND T1.MOCK_SUBCLASS_CD IS NOT NULL ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   PUTTYPE, ");
            stb.append("   MOCK_SUBCLASS_CD, ");
            stb.append("   TSCORE, ");
            stb.append("   TDEVIATION, ");
            stb.append("   TRANK ");
            stb.append(" FROM ");
            stb.append("   DATA_SUM_BASE ");
            stb.append(" WHERE ");
            stb.append("   RNUM = 1 ");
            stb.append(" ORDER BY ");
            stb.append("   PUTTYPE, ");
            stb.append("   MOCK_SUBCLASS_CD ");
            log.debug("getSundHeadInfo sql:"+ stb.toString());
            final List Lst = KnjDbUtils.query(db2, stb.toString());
            for (Iterator ite = Lst.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                final String puttype = (String)row.get("PUTTYPE");

                if (!retMap.containsKey(puttype)) {
                    subMap = new LinkedMap();
                    retMap.put(puttype, subMap);
                } else {
                    subMap = (Map)retMap.get(puttype);
                }
                final String mock_Subclass_Cd = (String)row.get("MOCK_SUBCLASS_CD");
                final String tscore = (String)row.get("TSCORE");
                final String tDeviation = (String)row.get("TDEVIATION");
                final String tRank = (String)row.get("TRANK");
                MockHeadData addWk = new MockHeadData(puttype, mock_Subclass_Cd, tscore, tDeviation, "0", "", "", "", tRank);
                subMap.put(mock_Subclass_Cd, addWk);
            }
            return retMap;
        }

        private Map getKawiHeadInfo(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            Map subMap = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH NICHIDAI_BASE_SUMFIX(PUTTYPE, MOCK_SUBCLASS_CD, TSCORE, TDEVIATION, TNOT2DISPFLG) AS ( ");
            stb.append(" VALUES('1', '051001', '3科合計素点', '3科合計化点', '0') ");
            stb.append(" UNION ");
            stb.append(" VALUES('1', '051002', '3科日大順位', '', '1') ");
            stb.append(" UNION ");
            stb.append(" VALUES('1', '051003', '5/4科合計素点', '5/4科合計化点', '0') ");
            stb.append(" UNION ");
            stb.append(" VALUES('1', '051004', '5/4科日大順位', '', '1') ");
            stb.append(" ), DATA_SUM_BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("   ROW_NUMBER() OVER(PARTITION BY T1.MOCK_SUBCLASS_CD) AS RNUM, ");
            stb.append("   CASE WHEN LENGTH(T1.MOCK_SUBCLASS_CD) > 3 THEN SUBSTR(T1.MOCK_SUBCLASS_CD, 3, 1) ELSE NULL END AS PUTTYPE, ");
            stb.append("   T1.MOCK_SUBCLASS_CD, ");
            stb.append("   T1.TSCORE, ");
            stb.append("   T1.TDEVIATION, ");
            stb.append("   '0' AS TNOT2DISPFLG");
            stb.append(" FROM ");
            stb.append("   mock_csv_zkai_score_head_dat T1 ");
            stb.append(" WHERE ");
            stb.append("   YEAR BETWEEN '"+String.valueOf(Integer.parseInt(_year) - 2)+"' AND '" + _year + "' ");  //過去3年分
            stb.append(" GROUP BY ");
            stb.append("   T1.MOSI_CD, ");
            stb.append("   T1.MOCK_SUBCLASS_CD, ");
            stb.append("   T1.TSCORE, ");
            stb.append("   T1.TDEVIATION ");
            stb.append(" ORDER BY ");
            stb.append("   T1.MOSI_CD, ");
            stb.append("   T1.MOCK_SUBCLASS_CD ");
            stb.append(" ), DATA_SUM_TBL AS ( ");
            stb.append(" SELECT ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   NICHIDAI_BASE_SUMFIX ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("   T1.PUTTYPE, ");
            stb.append("   T1.MOCK_SUBCLASS_CD, ");
            stb.append("   T1.TSCORE, ");
            stb.append("   T1.TDEVIATION, ");
            stb.append("   T1.TNOT2DISPFLG ");
            stb.append(" FROM ");
            stb.append("   DATA_SUM_BASE T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.MOCK_SUBCLASS_CD IS NOT NULL ");
            stb.append("   AND T1.RNUM = 1 ");  //MOCK_SUBCLASS_CDで見つかったものの中で、MOSI_CDの1番若い名称を取得
            stb.append("   AND SUBSTR(T1.MOCK_SUBCLASS_CD, 1, 2) = '05' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   DATA_SUM_TBL T1 ");
            stb.append(" ORDER BY ");
            stb.append("   CASE WHEN LENGTH(T1.MOCK_SUBCLASS_CD) > 3 THEN SUBSTR(T1.MOCK_SUBCLASS_CD, 3, 1) ELSE NULL END, ");
            stb.append("   T1.MOCK_SUBCLASS_CD ");
            log.debug("getKawiHeadInfo sql:"+ stb.toString());
            final List Lst = KnjDbUtils.query(db2, stb.toString());
            for (Iterator ite = Lst.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                final String puttype = (String)row.get("PUTTYPE");

                if (!retMap.containsKey(puttype)) {
                    subMap = new LinkedMap();
                    retMap.put(puttype, subMap);
                } else {
                    subMap = (Map)retMap.get(puttype);
                }
                final String mock_Subclass_Cd = (String)row.get("MOCK_SUBCLASS_CD");
                final String tscore = (String)row.get("TSCORE");
                final String tdeviation = (String)row.get("TDEVIATION");
                final String tNot2DispFlg = (String)row.get("TNOT2DISPFLG");
                MockHeadData addWk = new MockHeadData(puttype, mock_Subclass_Cd, tscore, tdeviation, tNot2DispFlg, "", "", "", "");
                subMap.put(mock_Subclass_Cd, addWk);
            }
            return retMap;
        }
    }

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (_param._psMap.size() > 0) {
            for (Iterator ite = _param._psMap.values().iterator();ite.hasNext();) {
                final PreparedStatement psctl = (PreparedStatement)ite.next();
                DbUtils.closeQuietly(psctl);
            }
        }
        if (null != svf) {
            svf.VrQuit();
        }
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }
}

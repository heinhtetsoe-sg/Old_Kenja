/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: eba07729b95c2376119f39b4598fb41713d67c17 $
 *
 * 作成日: 2019/06/13
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD185P {

    private static final Log log = LogFactory.getLog(KNJD185P.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";
    private static final String SUBCLASSCD999999 = "999999";

    private static final String SDIV1010101 = "1010101"; //1学期中間
    private static final String SDIV1020101 = "1020101"; //1学期期末
    private static final String SDIV1990008 = "1990008"; //1学期評定
    private static final String SDIV2010101 = "2010101"; //2学期中間
    private static final String SDIV2020101 = "2020101"; //2学期期末
    private static final String SDIV2990008 = "2990008"; //2学期評定
    private static final String SDIV3020101 = "3020101"; //3学期期末
    private static final String SDIV3990008 = "3990008"; //3学期評定
    private static final String SDIV9990008 = "9990008"; //学年集計データ
    private static final String SDIV9990009 = "9990009"; //学年評定

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

    private boolean _hasData;

    private Param _param;

    KNJEditString knjobj = new KNJEditString();

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
        final List studentList = getList(db2);
        //下段の出欠
        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
            final DateRange range = (DateRange) rit.next();
            Attendance.load(db2, _param, studentList, range);
        }
        //欠課
        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
            final DateRange range = (DateRange) rit.next();
            SubclassAttendance.load(db2, _param, studentList, range);
        }
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            //表紙
            printSvfHyoshi(db2, svf, student);

            //通知票
            printSvfMain(db2, svf, student);
        }
    }


    /**
     * 表紙を印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printSvfHyoshi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD185P_1.frm", 1);

        final String nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._loginYear)) + "年度";
        svf.VrsOut("NENDO", nendo); //年度
        svf.VrsOut("HR_NAME", student._hrname + student._attendno); //年組番
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名

        final int pnlen = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName);
        final String pNameField = pnlen > 30 ? "3" : pnlen > 20 ? "2" : "1";
        svf.VrsOut("PRINCIPAL_NAME" + pNameField, convReverseStr(_param._certifSchoolPrincipalName)); //校長名
        final int snlen = KNJ_EditEdit.getMS932ByteLength(student._staffname);
        final String trNameField = snlen > 30 ? "3" : snlen > 20 ? "2" : "1";
        svf.VrsOut("STAFF_NAME" + trNameField, convReverseStr(student._staffname)); //担任名
        svf.VrEndPage();
    }

    private String convReverseStr(String srcStr) {
        StringBuffer retStr = new StringBuffer();
        for (int ii = srcStr.length() - 1;ii >= 0;ii--) {
            retStr.append(srcStr.charAt(ii));
        }
        return retStr.toString();
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        boolean hasData = false;
        svf.VrSetForm("KNJD185P_2.frm", 1);

        //明細部以外を印字
        printTitle(db2, svf, student);

        //明細部分
        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        final List<SubclassMst> studentSubclassList = new ArrayList<SubclassMst>();
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            if (!student._printSubclassMap.containsKey(subclassMst._subclasscd)) {
                continue;
            }
            studentSubclassList.add(subclassMst);
        }

        //■定期考査
        int idx = 1;
        int maxSeme = Integer.parseInt(_param._semester) > 3 ? 3 : Integer.parseInt(_param._semester);
        for (final SubclassMst subclassMst : studentSubclassList) {
            if (idx > 14) {
                idx = 1;
                //定期考査 合計の印字
                printTotal(db2, svf, student);
                //集計出力
                printTerminateResult(svf, student, studentSubclassList);
                //改ページ
                svf.VrEndPage();
                printTitle(db2, svf, student);
            }
            final String subclassCd = subclassMst._subclasscd;

            for (int semei = 1; semei <= maxSeme; semei++) {
                //成績がある時に出力
                final String semStr = String.valueOf(semei);

                final int subclsnlen = subclassMst._subclassname.length();
                final String subclsfield = subclsnlen > 7 ? "3" : subclsnlen > 5 ? "2" : "1";
                if (semei == 1) {
                    svf.VrsOutn("SUBCLASS_NAME1_" + subclsfield, idx, subclassMst._subclassname); //科目名
                } else if (semei == 3) {
                    svf.VrsOutn("SUBCLASS_NAME2_" + subclsfield, idx, subclassMst._subclassname); //科目名
                }
                final ScoreData scoreData =  (ScoreData) student._printSubclassMap.get(subclassCd);

                svf.VrsOutn("VAL" + semStr, idx, scoreData.getRankScore(semStr));
                svf.VrsOutn("CLASS_RANK" + semStr, idx, scoreData.getRankCAvg(semStr));
                svf.VrsOutn("GRADE_RANK" + semStr, idx, scoreData.getRankGAvg(semStr));
                if (!"".equals(scoreData.getAvgCAvg(semStr))) {
                    BigDecimal bdcls1 = new BigDecimal(Double.parseDouble(scoreData.getAvgCAvg(semStr))).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOutn("CLASS_AVE" + semStr, idx, bdcls1.toString());
                }
                if (!"".equals(scoreData.getAvgGAvg(semStr))) {
                    BigDecimal bdgrd1 = new BigDecimal(Double.parseDouble(scoreData.getAvgGAvg(semStr))).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOutn("GRADE_AVE" + semStr, idx, bdgrd1.toString());
                }

                svf.VrsOutn("CLASS_MAX" + semStr, idx, scoreData.getAvgCHighScore(semStr));
                svf.VrsOutn("CLASS_MIN" + semStr, idx, scoreData.getAvgCLowScore(semStr));
                svf.VrsOutn("GRADE_MAX" + semStr, idx, scoreData.getAvgGHighScore(semStr));
                svf.VrsOutn("GRADE_MIN" + semStr, idx, scoreData.getAvgGLowScore(semStr));
                svf.VrsOutn("CLASS_EXAM_NUM" + semStr, idx, scoreData.getAvgCCount(semStr));
                svf.VrsOutn("GRADE_EXAM_NUM" + semStr, idx, scoreData.getAvgGCount(semStr));

                svf.VrsOutn("NOTICE" + semStr, idx, getNotice(semStr, student, subclassCd));

                //学年の表出力
                if ("9".equals(_param._semester)) {
                    printTerminateSubclass(svf, student, subclassCd, idx);
                }
            }
            idx++;
            hasData = true;
        }

        if (hasData) {
            //集計出力
            printTerminateResult(svf, student, studentSubclassList);
            //定期考査 合計の印字
            printTotal(db2, svf, student);
        }

        svf.VrEndPage();
        _hasData = true;
    }

    private void printTerminateSubclass(final Vrw32alp svf, final Student student, final String subclassCd, final int idx) {

        final String ChkSubcls9 = SUBCLASSCD999999;

        final String key = subclassCd;
        if (!student._printSubclassMap.containsKey(key)) {
            return;
        }
        final ScoreData scoreData =  (ScoreData) student._printSubclassMap.get(subclassCd);

        if (!ChkSubcls9.equals(scoreData._subclassCd)) {
            svf.VrsOutn("VAL9", idx, scoreData._rank_Year_Score99);
            if (scoreData._avg_C_Year_Count99 != null) {
                svf.VrsOutn("CLASS_RANK9", idx, addSpaceStr(scoreData._rank_Year_Clsavg99) + "/" + addSpaceStr(scoreData._avg_C_Year_Count99));
            }
            if (scoreData._avg_G_Year_Count99 != null) {
                svf.VrsOutn("GRADE_RANK9", idx, addSpaceStr(scoreData._rank_Year_Grdavg99) + "/" + addSpaceStr(scoreData._avg_G_Year_Count99));
            }
            svf.VrsOutn("NOTICE9", idx, getNotice("9", student, subclassCd));
            svf.VrsOutn("CREDIT9", idx, StringUtils.defaultString(scoreData._comp_Credit, ""));
        }
    }

    private String addSpaceStr(final String baseStr) {
        String retStr;
        if (baseStr.length() == 1) {
            retStr = " " + baseStr + " ";
        } else if (baseStr.length() >= 3) {
            retStr = baseStr;
        } else {
            retStr = " " + baseStr;
        }
        return retStr;
    }

    private void printTerminateResult(final Vrw32alp svf, final Student student, final List subclassList) {

        int totalcredit = 0;
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclasscd;
            final String ChkSubcls9 = SUBCLASSCD999999;

            final String key = subclassCd;
            if (!student._printSubclassMap.containsKey(key)) {
                continue;
            }
            final ScoreData scoreData =  (ScoreData) student._printSubclassMap.get(subclassCd);

            if (!ChkSubcls9.equals(scoreData._subclassCd)) {
                if (!"".equals(StringUtils.defaultString(scoreData._comp_Credit, ""))) {
                    totalcredit += Integer.parseInt(scoreData._comp_Credit);
                }
            }
        }
        //合計データを取得
        final String ttlkey = "99-" + student._schoolKind + "-99-" + SUBCLASSCD999999;
        if (student._printSubclassMap.containsKey(ttlkey)) {
            final ScoreData ttlscoreData =  (ScoreData) student._printSubclassMap.get(ttlkey);
            //平均点
            svf.VrsOut("AVE_VAL9", StringUtils.defaultString(ttlscoreData._rank_Year_Avg99, ""));
            svf.VrsOut("AVE_CLASS_RANK9", StringUtils.defaultString(ttlscoreData._rank_Year_Clsavg99, ""));
            svf.VrsOut("AVE_GRADE_RANK9", StringUtils.defaultString(ttlscoreData._rank_Year_Grdavg99, ""));

            //合計点
            svf.VrsOut("TOTAL_VAL9", StringUtils.defaultString(ttlscoreData._rank_Year_Score99, ""));
            svf.VrsOut("TOTAL_CREDIT9", String.valueOf(totalcredit));
        }
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        //明細部以外を印字

        //ヘッダ,フッタ
        svf.VrsOut("HR_NAME", student._hrname + student._attendno); //年組番
        svf.VrsOut("NAME", student._name); //氏名

        //■項目タイトル(横)
        int lpmaxval = Integer.parseInt(_param._semester) > 3 ? 3 : Integer.parseInt(_param._semester);
        for (int semscnt = 1;semscnt <= lpmaxval;semscnt++) {
            svf.VrsOut("VAL_NAME" + semscnt, "評価");
            svf.VrsOut("CLASS_RANK_NAME" + semscnt, "クラス順位");
            svf.VrsOut("GRADE_RANK_NAME" + semscnt, "学年順位");
            svf.VrsOut("CLASS_AVE_NAME" + semscnt, "クラス平均");
            svf.VrsOut("GRADE_AVE_NAME" + semscnt, "学年平均");
            svf.VrsOut("CLASS_MAX_NAME" + semscnt, "クラス最高点");
            svf.VrsOut("CLASS_MIN_NAME" + semscnt, "クラス最低点");
            svf.VrsOut("GRADE_MAX_NAME" + semscnt, "学年最高点");
            svf.VrsOut("GRADE_MIN_NAME" + semscnt, "学年最低点");

            svf.VrsOut("CLASS_EXAM_NUM_NAME" + semscnt, "クラス受験者数");
            svf.VrsOut("GRADE_EXAM_NUM_NAME" + semscnt, "学年受験者数");
            svf.VrsOut("NOTICE_NAME" + semscnt, "欠課時数");
        }

        if ("9".equals(_param._semester)) {
            svf.VrsOut("VAL_NAME9", "評価");
            svf.VrsOut("CLASS_RANK_NAME9", "クラス順位");
            svf.VrsOut("GRADE_RANK_NAME9", "学年順位");
            svf.VrsOut("NOTICE_NAME9", "欠課時数");
            svf.VrsOut("CREDIT_NAME9", "単位数");
        }

        //■出欠の記録
        printAttend(svf, student);
    }

    private String getNotice(final String semester, final Student student, final String subclassCd) {
        String retStr = "";

        if (student._attendSubClassMap.containsKey(subclassCd)) {
            final Map attendSubMap = (Map) student._attendSubClassMap.get(subclassCd);
            int totalSemesAbsence = 0; //欠時 学期合計
            if(Integer.parseInt(semester) > Integer.parseInt(_param._semester)) return retStr;

            if ("9".equals(semester)) {
                //欠時 学年合計
                for (int subsemes=1;subsemes<=3;subsemes++) {
                    String lpsemes = String.valueOf(subsemes);
                    if (attendSubMap.containsKey(lpsemes)) {
                        final Map atSubSemMap= (Map) attendSubMap.get(lpsemes);
                        for (final Iterator it2 = _param._attendSemesterDetailList.iterator(); it2.hasNext();) {
                            final SemesterDetail semesDetail = (SemesterDetail) it2.next();
                            if (atSubSemMap.containsKey(semesDetail._cdSemesterDetail)) {
                                final SubclassAttendance attendance= (SubclassAttendance) atSubSemMap.get(semesDetail._cdSemesterDetail);

                                if(!"".equals(attendance._sick.toString())) {
                                    totalSemesAbsence = totalSemesAbsence + Integer.parseInt(attendance._sick.toString());
                                }
                            }
                        }
                    }
                }
            } else {
                if (attendSubMap.containsKey(semester)) {
                    final Map atSubSemMap= (Map) attendSubMap.get(semester);
                    for (final Iterator it2 = _param._attendSemesterDetailList.iterator(); it2.hasNext();) {
                        final SemesterDetail semesDetail = (SemesterDetail) it2.next();
                        if (atSubSemMap.containsKey(semesDetail._cdSemesterDetail)) {
                            final SubclassAttendance attendance= (SubclassAttendance) atSubSemMap.get(semesDetail._cdSemesterDetail);

                            if(!"".equals(attendance._sick.toString())) {
                                totalSemesAbsence = totalSemesAbsence + Integer.parseInt(attendance._sick.toString());
                            }
                        }
                    }
                }
            }
            retStr = String.valueOf(totalSemesAbsence);
        }
        return retStr;
    }

    private void printTotal(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        //■定期考査 合計の印字
        final String subclassCd = "99-" + student._schoolKind + "-99-"+SUBCLASSCD999999;
        final ScoreData scoreData =  (ScoreData) student._printSubclassMap.get(subclassCd);
        if (scoreData == null) return;

        int maxSeme = SEMEALL.equals(_param._semester) ? 3 : Integer.parseInt(_param._semester);

        final String avgDivCl = ("0".equals(student._entType)) ? "C" : ("1".equals(student._entType)) ? "D" : ""; //AVG_DIV(クラス) 高入:'C' 一貫:'D'
        final String avgDiv = ("0".equals(student._entType)) ? "E" : ("1".equals(student._entType)) ? "F" : ""; //AVG_DIV(学年) 高入:'E' 一貫:'F'
        final Map avgAvgClMap = getRecordAverageSdivMap(db2, _param, subclassCd, "B", student, student._hrClass); //平均点列 クラス
        final Map avgAvgGMap = getRecordAverageSdivMap(db2, _param, subclassCd, "3", student, "000"); //平均点列  学年
        final Map avgTotalClMap = getRecordAverageSdivMap(db2, _param, subclassCd, avgDivCl, student, student._hrClass); //合計点列  クラス
        final Map avgTotalGMap = getRecordAverageSdivMap(db2, _param, subclassCd, avgDiv, student, "000"); //合計点列  学年
        //1学期
        for (int semcnt = 1;semcnt <= maxSeme;semcnt++) {
            final String sdiv = (semcnt == 1) ? SDIV1990008 : (semcnt == 2) ? SDIV2990008 : (semcnt == 3) ? SDIV3990008 : (semcnt == 9) ? SDIV9990008 : "";

            final String score9 = scoreData.getRankScore(String.valueOf(semcnt));
            final String avg9 = scoreData.getRankAvg(String.valueOf(semcnt));
            //平均・合計
            //評価
            if (!"".equals(StringUtils.defaultString(avg9, ""))) {
                BigDecimal bdval1 = new BigDecimal(Double.parseDouble(avg9)).setScale(1, BigDecimal.ROUND_HALF_UP);
                svf.VrsOut("AVE_VAL"+semcnt, bdval1.toString());
            }
            svf.VrsOut("TOTAL_VAL"+semcnt, StringUtils.defaultString(score9, ""));

            //クラス順位
            final String avgRank1 = getVRecordRankSdiv(db2, _param, sdiv, SUBCLASSCD999999, student._schregno, "HRCLASS_COURSE_RANK");
            final String totalRank1Field = ("0".equals(student._entType)) ? "HRCLASS_COURSE_ENT_OUT_AVG_RANK" : ("1".equals(student._entType)) ? "HRCLASS_COURSE_ENT_IN_AVG_RANK" : ""; //0:高入 1:一貫
            final String totalRank1 = getVRecordRankSdiv(db2, _param, sdiv, SUBCLASSCD999999, student._schregno, totalRank1Field);
            svf.VrsOut("AVE_CLASS_RANK"+semcnt, avgRank1);
            svf.VrsOut("TOTAL_CLASS_RANK"+semcnt, totalRank1);

            //学年順位
            final String avgRank2 = getRecordRankSdiv(db2, _param, sdiv, SUBCLASSCD999999, student._schregno, "COURSE_AVG_RANK");
            final String totalRank2Field = ("0".equals(student._entType)) ? "GRADE_COURSE_ENT_OUT_AVG_RANK" : ("1".equals(student._entType)) ? "GRADE_COURSE_ENT_IN_AVG_RANK" : ""; //0:高入 1:一貫
            final String totalRank2 = getVRecordRankSdiv(db2, _param, sdiv, SUBCLASSCD999999, student._schregno, totalRank2Field);
            svf.VrsOut("AVE_GRADE_RANK"+semcnt, avgRank2);
            svf.VrsOut("TOTAL_GRADE_RANK"+semcnt, totalRank2);

            //平均点列 クラス
            if(avgAvgClMap.containsKey(sdiv)) {
                final Map map = (Map) avgAvgClMap.get(sdiv);
                //クラス平均(小数1位まで。2位を四捨五入)
                final String avg = (String) map.get("AVG");
                if (!"".equals(avg)) {
                    BigDecimal bdavgcls1 = new BigDecimal(Double.parseDouble(avg)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOut("AVE_CLASS_AVE"+semcnt, bdavgcls1.toString());
                }

                //クラス最高点
                final String highscore = (String) map.get("HIGHSCORE");
                if (!"".equals(highscore)) {
                    BigDecimal bdobj = new BigDecimal(Double.parseDouble(highscore)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOut("AVE_CLASS_MAX"+semcnt, bdobj.toString());
                }

                //クラス最低点
                final String lowscore = (String) map.get("LOWSCORE");
                if (!"".equals(lowscore)) {
                    BigDecimal bdobj = new BigDecimal(Double.parseDouble(lowscore)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOut("AVE_CLASS_MIN"+semcnt, bdobj.toString());
                }

                //クラス受験者数
                final String count = (String) map.get("COUNT");
                if (!"".equals(count)) {
                    BigDecimal bdobj = new BigDecimal(Double.parseDouble(count)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOut("AVE_CLASS_EXAM_NUM"+semcnt, bdobj.toString());
                }
            }

            //平均点列 学年
            if(avgAvgGMap.containsKey(sdiv)) {
                final Map map = (Map) avgAvgGMap.get(sdiv);
                //学年平均
                final String avg = (String) map.get("AVG");
                if (!"".equals(avg)) {
                    BigDecimal bdavgttl1 = new BigDecimal(Double.parseDouble(avg)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOut("AVE_GRADE_AVE"+semcnt, bdavgttl1.toString());
                }


                //学年最高点
                final String highscore = (String) map.get("HIGHSCORE");
                if (!"".equals(highscore)) {
                    BigDecimal bdobj = new BigDecimal(Double.parseDouble(highscore)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOut("AVE_GRADE_MAX"+semcnt, bdobj.toString());
                }

                //学年最低点
                final String lowscore = (String) map.get("LOWSCORE");
                if (!"".equals(lowscore)) {
                    BigDecimal bdobj = new BigDecimal(Double.parseDouble(lowscore)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOut("AVE_GRADE_MIN"+semcnt, bdobj.toString());
                }

                //クラス受験者数
                final String count = (String) map.get("COUNT");
                if (!"".equals(count)) {
                    BigDecimal bdobj = new BigDecimal(Double.parseDouble(count)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOut("AVE_GRADE_EXAM_NUM"+semcnt, bdobj.toString());
                }
            }

            //合計点列 クラス
            if(avgTotalClMap.containsKey(sdiv)) {
                final Map map = (Map) avgTotalClMap.get(sdiv);

                //クラス平均(小数1位まで。2位を四捨五入)
                final String avg = (String) map.get("AVG");
                if (!"".equals(avg)) {
                    BigDecimal bdavgcls1 = new BigDecimal(Double.parseDouble(avg)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOut("TOTAL_CLASS_AVE"+semcnt, bdavgcls1.toString());
                }

                //クラス最高点
                svf.VrsOut("TOTAL_CLASS_MAX"+semcnt, (String) map.get("HIGHSCORE"));

                //クラス最低点
                svf.VrsOut("TOTAL_CLASS_MIN"+semcnt, (String) map.get("LOWSCORE"));

                //クラス受験者数
                svf.VrsOut("TOTAL_CLASS_EXAM_NUM"+semcnt, (String) map.get("COUNT"));

            }

            //合計点列 学年
            if(avgTotalGMap.containsKey(sdiv)) {
                final Map map = (Map) avgTotalGMap.get(sdiv);

                //学年平均
                final String avg = (String) map.get("AVG");
                if (!"".equals(avg)) {
                    BigDecimal bdavgcls1 = new BigDecimal(Double.parseDouble(avg)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOut("TOTAL_GRADE_AVE"+semcnt, bdavgcls1.toString());
                }

                //学年最高点
                svf.VrsOut("TOTAL_GRADE_MAX"+semcnt, (String) map.get("HIGHSCORE"));

                //学年最低点
                svf.VrsOut("TOTAL_GRADE_MIN"+semcnt, (String) map.get("LOWSCORE"));

                //学年受験者数
                svf.VrsOut("TOTAL_GRADE_EXAM_NUM"+semcnt, (String) map.get("COUNT"));

            }

            //欠課時数は空白
        }

//        svf.VrsOut("TOTAL_SCORE1_1", scoreData._rank_score1_1); //合計得点 中間
//        svf.VrsOut("TOTAL_SCORE1_2", scoreData._rank_score1_2); //合計得点 期末
//        svf.VrsOut("TOTAL_SCORE1_9", scoreData._rank_score1_3); //合計得点 学期成績
//        svf.VrsOut("TOTAL_AVE1_1", scoreData._rank_avg1_1); //合計平均 中間
//        svf.VrsOut("TOTAL_AVE1_2", scoreData._rank_avg1_2); //合計平均 期末
//        svf.VrsOut("TOTAL_AVE1_9", scoreData._rank_avg1_3); //合計平均 学期成績
//        svf.VrsOut("AVE_SCORE1_1", scoreData._avg_score1_1); //平均得点 中間
//        svf.VrsOut("AVE_SCORE1_2", scoreData._avg_score1_2); //平均得点 期末
//        svf.VrsOut("AVE_SCORE1_9", scoreData._avg_score1_3); //平均得点 学期成績
//        svf.VrsOut("AVE_AVE1_1", scoreData._avg_avg1_1); //平均得点 中間
//        svf.VrsOut("AVE_AVE1_2", scoreData._avg_avg1_2); //平均得点 期末
//        svf.VrsOut("AVE_AVE1_9", scoreData._avg_avg1_3); //平均得点 学期成績
//        svf.VrsOut("RANK1_1", scoreData._grade_rank1_1); //順位 中間
//        svf.VrsOut("RANK1_2", scoreData._grade_rank1_2); //順位 期末
//        svf.VrsOut("RANK1_9", scoreData._grade_rank1_3); //順位 学期成績
//
//        //2学期
//        svf.VrsOut("TOTAL_SCORE2_1", scoreData._rank_score2_1); //合計得点 中間
//        svf.VrsOut("TOTAL_SCORE2_2", scoreData._rank_score2_2); //合計得点 期末
//        svf.VrsOut("TOTAL_SCORE2_9", scoreData._rank_score2_3); //合計得点 学期成績
//        svf.VrsOut("TOTAL_AVE2_1", scoreData._rank_avg2_1); //合計平均 中間
//        svf.VrsOut("TOTAL_AVE2_2", scoreData._rank_avg2_2); //合計平均 期末
//        svf.VrsOut("TOTAL_AVE2_9", scoreData._rank_avg2_3); //合計平均 学期成績
//        svf.VrsOut("AVE_SCORE2_1", scoreData._avg_score2_1); //平均得点 中間
//        svf.VrsOut("AVE_SCORE2_2", scoreData._avg_score2_2); //平均得点 期末
//        svf.VrsOut("AVE_SCORE2_9", scoreData._avg_score2_3); //平均得点 学期成績
//        svf.VrsOut("AVE_AVE2_1", scoreData._avg_avg2_1); //平均得点 中間
//        svf.VrsOut("AVE_AVE2_2", scoreData._avg_avg2_2); //平均得点 期末
//        svf.VrsOut("AVE_AVE2_9", scoreData._avg_avg2_3); //平均得点 学期成績
//        svf.VrsOut("RANK2_1", scoreData._grade_rank2_1); //順位 中間
//        svf.VrsOut("RANK2_2", scoreData._grade_rank2_2); //順位 期末
//        svf.VrsOut("RANK2_9", scoreData._grade_rank2_3); //順位 学期成績
//
//        //3学期
//        svf.VrsOut("TOTAL_SCORE3_1", scoreData._rank_score3_1); //合計得点 期末
//        svf.VrsOut("TOTAL_SCORE3_9", scoreData._rank_score3_3); //合計得点 学期成績
//        svf.VrsOut("TOTAL_AVE3_1", scoreData._rank_avg3_1); //合計平均 期末
//        svf.VrsOut("TOTAL_AVE3_9", scoreData._rank_avg3_3); //合計平均 学期成績
//        svf.VrsOut("AVE_SCORE3_1", scoreData._avg_score3_1); //平均得点 期末
//        svf.VrsOut("AVE_SCORE3_9", scoreData._avg_score3_3); //平均得点 学期成績
//        svf.VrsOut("AVE_AVE3_1", scoreData._avg_avg3_1); //平均得点 期末
//        svf.VrsOut("AVE_AVE3_9", scoreData._avg_avg3_3); //平均得点 学期成績
//        svf.VrsOut("RANK3_1", scoreData._grade_rank3_1); //順位 期末
//        svf.VrsOut("RANK3_9", scoreData._grade_rank3_3); //順位 学期成績
    }

    private BigDecimal getCalcDivRndBD(final int a, final int b) {
        BigDecimal bdwk1 = new BigDecimal(a);
        BigDecimal bdwk2 = new BigDecimal(b);
        return bdwk1.divide(bdwk2, 1, BigDecimal.ROUND_HALF_UP);
    }
    private List subclassListRemoveD026() {
        final List retList = new ArrayList(_param._subclassMstMap.values());
        for (final Iterator it = retList.iterator(); it.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) it.next();
            if (_param._d026List.contains(subclassMst._subclasscd)) {
                it.remove();
            } else if (_param._isNoPrintMoto &&  subclassMst._isMoto || !_param._isPrintSakiKamoku &&  subclassMst._isSaki) {
                it.remove();
            }
        }
        return retList;
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOutn(field, i + 1, (String) list.get(i));
            }
        }
    }


    // 出欠記録
    private void printAttend(final Vrw32alp svf, final Student student) {
    	//TODO
        for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final int line = getSemeLine(semester);
            final Attendance att = (Attendance) student._attendMap.get(semester);
            if (null != att) {
                svf.VrsOutn("LESSON", line, String.valueOf(att._lesson));   // 授業日数
                svf.VrsOutn("MOURNING", line, String.valueOf(att._suspend + att._mourning)); // 忌引等
                svf.VrsOutn("MUST", line, String.valueOf(att._mLesson));    // 出席すべき日数
                svf.VrsOutn("LATE", line, String.valueOf(att._late));       // 遅刻
                svf.VrsOutn("EARLY", line, String.valueOf(att._early));     // 早退
                svf.VrsOutn("ABSENT", line, String.valueOf(att._absent));   // 欠席
//              printAttendOrBackSlash(svf, line, att._abroad, "0", "ABROAD", "ABROAD_SLASH");     // 留学日数
//                svf.VrsOutn("APPOINT", line, String.valueOf(att._present)); // 出席日数
//                printAttendOrBackSlash(svf, line, att._det006, "0", "LHR_NOTICE", "LHR_SLASH");    // LHR欠課時数
//                printAttendOrBackSlash(svf, line, att._det007, "0", "EVENT_NOTICE", "EVENT_SLASH");// 行事欠課時数
            }
        }
    }

    //備考欄印刷
    private void printHaveNewLine(final Vrw32alp svf, final String propertie, final String printText, final String fieldName, final int defLen, final int defRow) {
        if (!StringUtils.isEmpty(printText)) {
            final String[] nums = StringUtils.split(StringUtils.replace(propertie, "+", " "), " * ");
            int rLen = defLen;
            int rRow = defRow;
            if (null != nums && nums.length == 2) {
                rLen = Integer.parseInt(nums[0]);
                rRow = Integer.parseInt(nums[1]);
            }
            final String[] remarkArray = KNJ_EditEdit.get_token(printText, rLen, rRow);
            for (int i = 0; i < remarkArray.length; i++) {
                final String setRemark = remarkArray[i];
                svf.VrsOut(fieldName + (i + 1), setRemark);
            }
        }
    }

    private int getSemeLine(final String semester) {
        final int line;
        if (SEMEALL.equals(semester)) {
            line = 4;
        } else {
            line = Integer.parseInt(semester);
        }
        return line;
    }

    private void printAttendOrBackSlash(final Vrw32alp svf, final int line, final int setVal, final String setBackSlash, final String attendField, final String bkSlashField) {
        if ("1".equals(setBackSlash)) {
            svf.VrsOut(bkSlashField + line, String.valueOf(_param._backSlashImagePath));
        } else {
            svf.VrsOutn(attendField, line, String.valueOf(setVal));   // 留学日数
        }
    }

    //RECORD_AVERAGE_SDIV_DATの取得
    private Map getRecordAverageSdivMap(final DB2UDB db2, final Param param, final String subclasscd, final String avgDiv, final Student student, final String hrClass) {
        Map rtnMap = new HashMap();

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS SDIV, ");
        stb.append("     AVG, ");
        stb.append("     HIGHSCORE, ");
        stb.append("     LOWSCORE, ");
        stb.append("     COUNT ");
        stb.append(" FROM ");
        stb.append("     RECORD_AVERAGE_SDIV_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + param._loginYear + "' ");
        stb.append("     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '990008' ");
        stb.append("     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '"+ subclasscd +"' ");
        stb.append("     AND AVG_DIV  = '"+ avgDiv +"' ");
        stb.append("     AND GRADE    = '"+ student._grade +"' ");
        stb.append("     AND HR_CLASS = '"+ hrClass+"' ");
        stb.append("     AND COURSECD || MAJORCD || COURSECODE = '"+ student._course +"' ");
        stb.append(" ORDER BY SDIV ");
        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map map = new HashMap();
                map.put("AVG", StringUtils.defaultString(rs.getString("AVG")));
                map.put("HIGHSCORE", StringUtils.defaultString(rs.getString("HIGHSCORE")));
                map.put("LOWSCORE", StringUtils.defaultString(rs.getString("LOWSCORE")));
                map.put("COUNT", StringUtils.defaultString(rs.getString("COUNT")));
                final String key = StringUtils.defaultString(rs.getString("SDIV"));
                rtnMap.put(key, map);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnMap;
    }

    //RECORD_RANK_SDIV_DATの取得
    private String getRecordRankSdiv(final DB2UDB db2, final Param param, final String sdiv, final String subclasscd, final String schregno, final String field) {
        String rtnStr = "";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("   * ");
        stb.append(" FROM  ");
        stb.append("   RECORD_RANK_SDIV_DAT ");
        stb.append(" WHERE YEAR       = '"+ param._loginYear +"' ");
        stb.append("   AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV  = '" + sdiv + "' ");
        if(SUBCLASSCD999999.equals(subclasscd)) {
            stb.append("   AND SUBCLASSCD = '"+ subclasscd +"' ");
        } else {
            stb.append("   AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '"+ subclasscd +"' ");
        }
        stb.append("   AND SCHREGNO   = '"+ schregno +"' ");
        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                rtnStr = StringUtils.defaultString(rs.getString(field));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnStr.toString();
    }

    //V_RECORD_RANK_SDIV_DATの取得
    private String getVRecordRankSdiv(final DB2UDB db2, final Param param, final String sdiv, final String subclasscd, final String schregno, final String field) {
        String rtnStr = "";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("   * ");
        stb.append(" FROM  ");
        stb.append("   V_RECORD_RANK_SDIV_DAT ");
        stb.append(" WHERE YEAR       = '"+ param._loginYear +"' ");
        stb.append("   AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV  = '" + sdiv + "' ");
        if(SUBCLASSCD999999.equals(subclasscd)) {
            stb.append("   AND SUBCLASSCD = '"+ subclasscd +"' ");
        } else {
            stb.append("   AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '"+ subclasscd +"' ");
        }
        stb.append("   AND SCHREGNO   = '"+ schregno +"' ");
        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                rtnStr = StringUtils.defaultString(rs.getString(field));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnStr.toString();
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            //log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._schoolKind = rs.getString("SCHOOL_KIND");
                student._gradename = rs.getString("GRADE_NAME");
                student._hrname = rs.getString("HR_NAME");
                student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                student._staffname2 = StringUtils.defaultString(rs.getString("STAFFNAME2"));
                student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO");
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._coursecd = rs.getString("COURSECD");
                student._majorcd = rs.getString("MAJORCD");
                student._course = rs.getString("COURSE");
                student._majorname = rs.getString("MAJORNAME");
                student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                student._entyear = rs.getString("ENT_YEAR");
                student._totalstudytime1 = rs.getString("TOTALSTUDYTIME1");
                student._totalstudytime2 = rs.getString("TOTALSTUDYTIME2");
                student._totalstudytime3 = rs.getString("TOTALSTUDYTIME3");
                student._committee= StringUtils.defaultString(rs.getString("COMMITTEE"));
                student._club1 = rs.getString("CLUB1");
                student._club2 = rs.getString("CLUB2");
                student._club3 = rs.getString("CLUB3");
                student._qualification = rs.getString("QUALIFICATION");
                student._communication = rs.getString("COMMUNICATION");
                student._entType = StringUtils.defaultString(rs.getString("ENT_TYPE"));

                //student.setSubclass(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.warn("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        setListSubclass(db2, retList);
        return retList;
    }

    private void setListSubclass(final DB2UDB db2, final List students) {
        if (students.size() == 0) return;

        String schregInState = "";
        String delimStr = "";
        Map schIndexMap = new HashMap();
        int idx = 0;
        for (Iterator ite = students.iterator();ite.hasNext();) {
            Student student = (Student)ite.next();
            Integer addwk = new Integer(idx);
            schIndexMap.put(student._schregno, addwk);
            schregInState += delimStr + "'" + student._schregno + "'";
            delimStr = ", ";
            idx++;
        }
        schregInState = " ( " + schregInState + " ) ";

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = prestatementSubclass(schregInState);
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                Integer getIdx = (Integer)schIndexMap.get(schregNo);
                Student student = (Student)students.get(getIdx.intValue());

                final String classCd = rs.getString("CLASSCD");
                final String className = rs.getString("CLASSNAME");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String rank_Score1 = rs.getString("RANK_SCORE1");
                final String rank_Avg1 = rs.getString("RANK_AVG1");
                final String rank_C_Avg1 = rs.getString("RANK_C_AVG1");
                final String rank_G_Avg1 = rs.getString("RANK_G_AVG1");
                final String avg_C_Avg1 = rs.getString("AVG_C_AVG1");
                final String avg_G_Avg1 = rs.getString("AVG_G_AVG1");
                final String avg_C_HighScore1 = rs.getString("AVG_C_HIGHSCORE1");
                final String avg_C_LowScore1 = rs.getString("AVG_C_LOWSCORE1");
                final String avg_G_HighScore1 = rs.getString("AVG_G_HIGHSCORE1");
                final String avg_G_LowScore1 = rs.getString("AVG_G_LOWSCORE1");
                final String avg_C_Count1 = rs.getString("AVG_C_COUNT1");
                final String avg_G_Count1 = rs.getString("AVG_G_COUNT1");
                final String rank_Score2 = rs.getString("RANK_SCORE2");
                final String rank_Avg2 = rs.getString("RANK_AVG2");
                final String rank_C_Avg2 = rs.getString("RANK_C_AVG2");
                final String rank_G_Avg2 = rs.getString("RANK_G_AVG2");
                final String avg_C_Avg2 = rs.getString("AVG_C_AVG2");
                final String avg_G_Avg2 = rs.getString("AVG_G_AVG2");
                final String avg_C_HighScore2 = rs.getString("AVG_C_HIGHSCORE2");
                final String avg_C_LowScore2 = rs.getString("AVG_C_LOWSCORE2");
                final String avg_G_HighScore2 = rs.getString("AVG_G_HIGHSCORE2");
                final String avg_G_LowScore2 = rs.getString("AVG_G_LOWSCORE2");
                final String avg_C_Count2 = rs.getString("AVG_C_COUNT2");
                final String avg_G_Count2 = rs.getString("AVG_G_COUNT2");
                final String rank_Score3 = rs.getString("RANK_SCORE3");
                final String rank_Avg3 = rs.getString("RANK_AVG3");
                final String rank_C_Avg3 = rs.getString("RANK_C_AVG3");
                final String rank_G_Avg3 = rs.getString("RANK_G_AVG3");
                final String avg_C_Avg3 = rs.getString("AVG_C_AVG3");
                final String avg_G_Avg3 = rs.getString("AVG_G_AVG3");
                final String avg_C_HighScore3 = rs.getString("AVG_C_HIGHSCORE3");
                final String avg_C_LowScore3 = rs.getString("AVG_C_LOWSCORE3");
                final String avg_G_HighScore3 = rs.getString("AVG_G_HIGHSCORE3");
                final String avg_G_LowScore3 = rs.getString("AVG_G_LOWSCORE3");
                final String avg_C_Count3 = rs.getString("AVG_C_COUNT3");
                final String avg_G_Count3 = rs.getString("AVG_G_COUNT3");
                final String rank_Total_Avg9 = rs.getString("RANK_TOTAL_AVG9");

                final String rank_Total_Score9 = rs.getString("RANK_TOTAL_SCORE9");

                final String rank_Year_Score99 = rs.getString("RANK_YEAR_SCORE99");
                final String rank_Year_Avg99 = rs.getString("RANK_YEAR_AVG99");
                final String rank_Year_Clsavg99 = rs.getString("RANK_YEAR_CLSAVG99");
                final String avg_C_Year_Count99 = rs.getString("AVG_C_YEAR_COUNT99");
                final String rank_Year_Grdavg99 = rs.getString("RANK_YEAR_GRDAVG99");
                final String avg_G_Year_Count99 = rs.getString("AVG_G_YEAR_COUNT99");
                final String comp_Credit = rs.getString("COMP_CREDIT");

                String key = "";
                final String curriculumCd;
                final String schoolKind;
                if ("1".equals(_param._useCurriculumcd)) {
                    curriculumCd = rs.getString("CURRICULUM_CD");
                    schoolKind = rs.getString("SCHOOL_KIND");
                    key = classCd + "-" + schoolKind + "-" + curriculumCd + "-" + subclassCd;
                } else {
                    curriculumCd = "";
                    schoolKind = "";
                    key = subclassCd;
                }
                ScoreData scoreData = new ScoreData(classCd, className, subclassCd, subclassName, rank_Score1, rank_Avg1, rank_C_Avg1, rank_G_Avg1, avg_C_Avg1, avg_G_Avg1, avg_C_HighScore1, avg_C_LowScore1, avg_G_HighScore1, avg_G_LowScore1, avg_C_Count1, avg_G_Count1, rank_Score2, rank_Avg2, rank_C_Avg2, rank_G_Avg2, avg_C_Avg2, avg_G_Avg2, avg_C_HighScore2, avg_C_LowScore2, avg_G_HighScore2, avg_G_LowScore2, avg_C_Count2, avg_G_Count2, rank_Score3, rank_Avg3, rank_C_Avg3, rank_G_Avg3, avg_C_Avg3, avg_G_Avg3, avg_C_HighScore3, avg_C_LowScore3, avg_G_HighScore3, avg_G_LowScore3, avg_C_Count3, avg_G_Count3, rank_Total_Avg9, rank_Total_Score9, rank_Year_Score99, rank_Year_Avg99, rank_Year_Clsavg99, avg_C_Year_Count99, rank_Year_Grdavg99, avg_G_Year_Count99, comp_Credit, curriculumCd, schoolKind);
                student._printSubclassMap.put(key, scoreData);
            }
        } catch (SQLException ex) {
            log.warn("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return;
    }

    private String prestatementSubclass(final String schregInState) {
        final StringBuffer stb = new StringBuffer();

        //TODO
        stb.append(" WITH SCHNO AS( ");
        //学籍の表
        stb.append(" SELECT ");
        stb.append("     T2.SEMESTER, ");
        stb.append("     T2.SCHREGNO, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T2.COURSECD, ");
        stb.append("     T2.MAJORCD, ");
        stb.append("     T2.COURSECODE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("     T2.YEAR         = '" + _param._loginYear + "'  ");
        stb.append("     AND T2.GRADE || T2.HR_CLASS    = '" + _param._gradeHrClass + "'  ");
        stb.append("     AND T2.SCHREGNO IN " + schregInState + " ");
        stb.append("     AND T2.SEMESTER = (SELECT ");
        stb.append("                          MAX(SEMESTER) ");
        stb.append("                        FROM ");
        stb.append("                          SCHREG_REGD_DAT W2 ");
        stb.append("                        WHERE ");
        stb.append("                          W2.YEAR = '" + _param._loginYear + "'  ");
        stb.append("                          AND W2.SEMESTER <= '" + _param._semester + "'  ");
        stb.append("                          AND W2.SCHREGNO = T2.SCHREGNO ");
        stb.append("                     ) ");
        //講座の表
        stb.append(" ) , CHAIR_A AS( ");
        stb.append(" SELECT ");
        stb.append("     S1.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" S2.CLASSCD, ");
            stb.append(" S2.SCHOOL_KIND, ");
            stb.append(" S2.CURRICULUM_CD, ");
        }
        stb.append("     S2.SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT S1, ");
        stb.append("     CHAIR_DAT S2 ");
        stb.append(" WHERE ");
        stb.append("     S1.YEAR         = '" + _param._loginYear + "' ");
        stb.append("     AND S1.SEMESTER <= '" + _param._semester + "' ");
        stb.append("     AND S2.YEAR     = S1.YEAR          ");
        stb.append("     AND S2.SEMESTER <= '" + _param._semester + "' ");
        stb.append("     AND S2.SEMESTER = S1.SEMESTER          ");
        stb.append("     AND S2.CHAIRCD  = S1.CHAIRCD          ");
        stb.append("     AND S1.SCHREGNO IN " + schregInState + " ");
        stb.append("     AND SUBCLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
        stb.append("     AND SUBCLASSCD NOT LIKE '50%' ");
        stb.append(" GROUP BY ");
        stb.append("     S1.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" S2.CLASSCD, ");
            stb.append(" S2.SCHOOL_KIND, ");
            stb.append(" S2.CURRICULUM_CD, ");
        }
        stb.append("     S2.SUBCLASSCD ");
        //成績明細データの表
        stb.append(" ) ,RECORD AS( ");
        stb.append(" SELECT DISTINCT");
        stb.append("     T1.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        //1学期データ
        stb.append("     LR19.SCORE            AS RANK_SCORE1, ");
        stb.append("     LR19.AVG              AS RANK_AVG1, ");
        stb.append("     LR19.CLASS_AVG_RANK   AS RANK_C_AVG1, ");
        stb.append("     LR19.GRADE_AVG_RANK   AS RANK_G_AVG1, ");
        stb.append("     LA19C.AVG             AS AVG_C_AVG1, ");
        stb.append("     LA19G.AVG             AS AVG_G_AVG1, ");
        stb.append("     LA19C.HIGHSCORE       AS AVG_C_HIGHSCORE1, ");
        stb.append("     LA19C.LOWSCORE        AS AVG_C_LOWSCORE1, ");
        stb.append("     LA19G.HIGHSCORE       AS AVG_G_HIGHSCORE1, ");
        stb.append("     LA19G.LOWSCORE        AS AVG_G_LOWSCORE1, ");
        stb.append("     LA19C.COUNT           AS AVG_C_COUNT1, ");
        stb.append("     LA19G.COUNT           AS AVG_G_COUNT1, ");
        //2学期データ
        stb.append("     LR29.SCORE            AS RANK_SCORE2, ");
        stb.append("     LR29.AVG              AS RANK_AVG2, ");
        stb.append("     LR29.CLASS_AVG_RANK   AS RANK_C_AVG2, ");
        stb.append("     LR29.GRADE_AVG_RANK   AS RANK_G_AVG2, ");
        stb.append("     LA29C.AVG             AS AVG_C_AVG2, ");
        stb.append("     LA29G.AVG             AS AVG_G_AVG2, ");
        stb.append("     LA29C.HIGHSCORE       AS AVG_C_HIGHSCORE2, ");
        stb.append("     LA29C.LOWSCORE        AS AVG_C_LOWSCORE2, ");
        stb.append("     LA29G.HIGHSCORE       AS AVG_G_HIGHSCORE2, ");
        stb.append("     LA29G.LOWSCORE        AS AVG_G_LOWSCORE2, ");
        stb.append("     LA29C.COUNT           AS AVG_C_COUNT2, ");
        stb.append("     LA29G.COUNT           AS AVG_G_COUNT2, ");
        //3学期データ
        stb.append("     LR39.SCORE            AS RANK_SCORE3, ");
        stb.append("     LR39.AVG              AS RANK_AVG3, ");
        stb.append("     LR39.CLASS_AVG_RANK   AS RANK_C_AVG3, ");
        stb.append("     LR39.GRADE_AVG_RANK   AS RANK_G_AVG3, ");
        stb.append("     LA39C.AVG             AS AVG_C_AVG3, ");
        stb.append("     LA39G.AVG             AS AVG_G_AVG3, ");
        stb.append("     LA39C.HIGHSCORE       AS AVG_C_HIGHSCORE3, ");
        stb.append("     LA39C.LOWSCORE        AS AVG_C_LOWSCORE3, ");
        stb.append("     LA39G.HIGHSCORE       AS AVG_G_HIGHSCORE3, ");
        stb.append("     LA39G.LOWSCORE        AS AVG_G_LOWSCORE3, ");
        stb.append("     LA39C.COUNT           AS AVG_C_COUNT3, ");
        stb.append("     LA39G.COUNT           AS AVG_G_COUNT3, ");
        //平均値利用データ
        stb.append("     LR98.AVG AS RANK_TOTAL_AVG9, ");
        //合計値利用データ
        stb.append("     LR98.SCORE AS RANK_TOTAL_SCORE9, ");
        //学年データ
        stb.append("     LR99.AVG AS RANK_YEAR_AVG99, ");    //平均点の列でのみで利用。
        stb.append("     LR99.SCORE AS RANK_YEAR_SCORE99, ");
        stb.append("     LR99.CLASS_AVG_RANK AS RANK_YEAR_CLSAVG99, ");
        stb.append("     LA99C.COUNT AS AVG_C_YEAR_COUNT99, ");
        stb.append("     LR99.GRADE_AVG_RANK AS RANK_YEAR_GRDAVG99, ");
        stb.append("     LA99G.COUNT AS AVG_G_YEAR_COUNT99, ");
        stb.append("     T2.COMP_CREDIT ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_SDIV_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD1 ON REGD1.YEAR = T1.YEAR AND REGD1.SEMESTER = '1' AND REGD1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD2 ON REGD2.YEAR = T1.YEAR AND REGD2.SEMESTER = '2' AND REGD2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD3 ON REGD3.YEAR = T1.YEAR AND REGD3.SEMESTER = '3' AND REGD3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD9 ON REGD9.YEAR = T1.YEAR AND REGD9.SEMESTER = '"+ _param._semester +"' AND REGD9.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN RECORD_SCORE_DAT T2 ");
        stb.append("       ON T2.YEAR = T1.YEAR ");
        stb.append("      AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '9990009' ");
        stb.append("      AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("      AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("      AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT LR19 ");
        stb.append("            ON LR19.YEAR          = T1.YEAR ");
        stb.append("           AND LR19.SEMESTER || LR19.TESTKINDCD || LR19.TESTITEMCD || LR19.SCORE_DIV = '" + SDIV1990008 + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND LR19.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND LR19.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND LR19.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND LR19.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("           AND LR19.SCHREGNO      = T1.SCHREGNO ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT LA19G ");
        stb.append("            ON LA19G.YEAR          = T1.YEAR ");
        stb.append("           AND LA19G.SEMESTER || LA19G.TESTKINDCD || LA19G.TESTITEMCD || LA19G.SCORE_DIV = '" + SDIV1990008 + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND LA19G.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND LA19G.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND LA19G.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND LA19G.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("           AND LA19G.AVG_DIV       = '1' ");
        stb.append("           AND LA19G.GRADE         = '" + _param._grade + "' ");
        stb.append("           AND LA19G.HR_CLASS      = '000' ");
        stb.append("           AND LA19G.COURSECD      = '0' ");
        stb.append("           AND LA19G.MAJORCD       = '000' ");
        stb.append("           AND LA19G.COURSECODE    = '0000' ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT LA19C ");
        stb.append("            ON LA19C.YEAR          = T1.YEAR ");
        stb.append("           AND LA19C.SEMESTER || LA19C.TESTKINDCD || LA19C.TESTITEMCD || LA19C.SCORE_DIV = '" + SDIV1990008 + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND LA19C.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND LA19C.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND LA19C.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND LA19C.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("           AND LA19C.AVG_DIV       = '2' ");
        stb.append("           AND LA19C.GRADE || LA19C.HR_CLASS         = '" + _param._gradeHrClass + "' ");
        stb.append("           AND LA19C.COURSECD      = '0' ");
        stb.append("           AND LA19C.MAJORCD       = '000' ");
        stb.append("           AND LA19C.COURSECODE    = '0000' ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT LR29 ");
        stb.append("            ON LR29.YEAR          = T1.YEAR ");
        stb.append("           AND LR29.SEMESTER || LR29.TESTKINDCD || LR29.TESTITEMCD || LR29.SCORE_DIV = '" + SDIV2990008 + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND LR29.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND LR29.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND LR29.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND LR29.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("           AND LR29.SCHREGNO      = T1.SCHREGNO ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT LA29G ");
        stb.append("            ON LA29G.YEAR          = T1.YEAR ");
        stb.append("           AND LA29G.SEMESTER || LA29G.TESTKINDCD || LA29G.TESTITEMCD || LA29G.SCORE_DIV = '" + SDIV2990008 + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND LA29G.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND LA29G.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND LA29G.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND LA29G.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("           AND LA29G.AVG_DIV       = '1' ");
        stb.append("           AND LA29G.GRADE         = '" + _param._grade + "' ");
        stb.append("           AND LA29G.HR_CLASS      = '000' ");
        stb.append("           AND LA29G.COURSECD      = '0' ");
        stb.append("           AND LA29G.MAJORCD       = '000' ");
        stb.append("           AND LA29G.COURSECODE    = '0000' ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT LA29C ");
        stb.append("            ON LA29C.YEAR          = T1.YEAR ");
        stb.append("           AND LA29C.SEMESTER || LA29C.TESTKINDCD || LA29C.TESTITEMCD || LA29C.SCORE_DIV = '" + SDIV2990008 + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND LA29C.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND LA29C.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND LA29C.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND LA29C.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("           AND LA29C.AVG_DIV       = '2' ");
        stb.append("           AND LA29C.GRADE || LA29C.HR_CLASS         = '" + _param._gradeHrClass + "' ");
        stb.append("           AND LA29C.COURSECD      = '0' ");
        stb.append("           AND LA29C.MAJORCD       = '000' ");
        stb.append("           AND LA29C.COURSECODE    = '0000' ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT LR39 ");
        stb.append("            ON LR39.YEAR          = T1.YEAR ");
        stb.append("           AND LR39.SEMESTER || LR39.TESTKINDCD || LR39.TESTITEMCD || LR39.SCORE_DIV = '" + SDIV3990008 + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND LR39.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND LR39.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND LR39.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND LR39.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("           AND LR39.SCHREGNO      = T1.SCHREGNO ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT LA39G ");
        stb.append("            ON LA39G.YEAR          = T1.YEAR ");
        stb.append("           AND LA39G.SEMESTER || LA39G.TESTKINDCD || LA39G.TESTITEMCD || LA39G.SCORE_DIV = '" + SDIV3990008 + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND LA39G.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND LA39G.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND LA39G.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND LA39G.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("           AND LA39G.AVG_DIV       = '1' ");
        stb.append("           AND LA39G.GRADE         = '" + _param._grade + "' ");
        stb.append("           AND LA39G.HR_CLASS      = '000' ");
        stb.append("           AND LA39G.COURSECD      = '0' ");
        stb.append("           AND LA39G.MAJORCD       = '000' ");
        stb.append("           AND LA39G.COURSECODE    = '0000' ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT LA39C ");
        stb.append("            ON LA39C.YEAR          = T1.YEAR ");
        stb.append("           AND LA39C.SEMESTER || LA39C.TESTKINDCD || LA39C.TESTITEMCD || LA39C.SCORE_DIV = '" + SDIV3990008 + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND LA39C.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND LA39C.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND LA39C.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND LA39C.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("           AND LA39C.AVG_DIV       = '2' ");
        stb.append("           AND LA39C.GRADE || LA39C.HR_CLASS         = '" + _param._gradeHrClass + "' ");
        stb.append("           AND LA39C.COURSECD      = '0' ");
        stb.append("           AND LA39C.MAJORCD       = '000' ");
        stb.append("           AND LA39C.COURSECODE    = '0000' ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT LR98 ");
        stb.append("            ON LR98.YEAR          = T1.YEAR ");
        stb.append("           AND LR98.SEMESTER || LR98.TESTKINDCD || LR98.TESTITEMCD || LR98.SCORE_DIV = '" + SDIV9990008 + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND LR98.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND LR98.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND LR98.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND LR98.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("           AND LR98.SCHREGNO      = T1.SCHREGNO ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT LR99 ");
        stb.append("            ON LR99.YEAR          = T1.YEAR ");
        stb.append("           AND LR99.SEMESTER || LR99.TESTKINDCD || LR99.TESTITEMCD || LR99.SCORE_DIV = '" + SDIV9990009 + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND LR99.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND LR99.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND LR99.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND LR99.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("           AND LR99.SCHREGNO      = T1.SCHREGNO ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT LA99G ");
        stb.append("            ON LA99G.YEAR          = T1.YEAR ");
        stb.append("           AND LA99G.SEMESTER || LA99G.TESTKINDCD || LA99G.TESTITEMCD || LA99G.SCORE_DIV = '" + SDIV9990009 + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND LA99G.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND LA99G.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND LA99G.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND LA99G.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("           AND LA99G.AVG_DIV       = '1' ");
        stb.append("           AND LA99G.GRADE         = '" + _param._grade + "' ");
        stb.append("           AND LA99G.HR_CLASS      = '000' ");
        stb.append("           AND LA99G.COURSECD      = '0' ");
        stb.append("           AND LA99G.MAJORCD       = '000' ");
        stb.append("           AND LA99G.COURSECODE    = '0000' ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT LA99C ");
        stb.append("            ON LA99C.YEAR          = T1.YEAR ");
        stb.append("           AND LA99C.SEMESTER || LA99C.TESTKINDCD || LA99C.TESTITEMCD || LA99C.SCORE_DIV = '" + SDIV9990009 + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND LA99C.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND LA99C.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND LA99C.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND LA99C.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("           AND LA99C.AVG_DIV       = '2' ");
        stb.append("           AND LA99C.GRADE || LA99C.HR_CLASS         = '" + _param._gradeHrClass + "' ");
        stb.append("           AND LA99C.COURSECD      = '0' ");
        stb.append("           AND LA99C.MAJORCD       = '000' ");
        stb.append("           AND LA99C.COURSECODE    = '0000' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._loginYear + "'  ");
        stb.append("     AND T1.SCHREGNO IN " + schregInState + " ");
        stb.append("     AND T1.SUBCLASSCD NOT LIKE '50%' ");
        stb.append("     AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
        //メイン表
        stb.append(" ) ");
//        stb.append(" ) ,T_MAIN AS( ");
        stb.append(" SELECT ");
        stb.append("     T2.SEMESTER, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T2.SCHREGNO, ");
        stb.append("     T3.CLASSCD, ");
        stb.append("     T3.CLASSNAME, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" T4.SCHOOL_KIND, ");
            stb.append(" T4.CURRICULUM_CD, ");
        }
        stb.append("     T4.SUBCLASSCD, ");
        stb.append("     T4.SUBCLASSNAME, ");
        //1学期
        stb.append("     T1.RANK_SCORE1, ");
        stb.append("     T1.RANK_AVG1, ");
        stb.append("     T1.RANK_C_AVG1, ");
        stb.append("     T1.RANK_G_AVG1, ");
        stb.append("     T1.AVG_C_AVG1, ");
        stb.append("     T1.AVG_G_AVG1, ");
        stb.append("     T1.AVG_C_HIGHSCORE1, ");
        stb.append("     T1.AVG_C_LOWSCORE1, ");
        stb.append("     T1.AVG_G_HIGHSCORE1, ");
        stb.append("     T1.AVG_G_LOWSCORE1, ");
        stb.append("     T1.AVG_C_COUNT1, ");
        stb.append("     T1.AVG_G_COUNT1, ");
        //2学期
        stb.append("     T1.RANK_SCORE2, ");
        stb.append("     T1.RANK_AVG2, ");
        stb.append("     T1.RANK_C_AVG2, ");
        stb.append("     T1.RANK_G_AVG2, ");
        stb.append("     T1.AVG_C_AVG2, ");
        stb.append("     T1.AVG_G_AVG2, ");
        stb.append("     T1.AVG_C_HIGHSCORE2, ");
        stb.append("     T1.AVG_C_LOWSCORE2, ");
        stb.append("     T1.AVG_G_HIGHSCORE2, ");
        stb.append("     T1.AVG_G_LOWSCORE2, ");
        stb.append("     T1.AVG_C_COUNT2, ");
        stb.append("     T1.AVG_G_COUNT2, ");
        //3学期
        stb.append("     T1.RANK_SCORE3, ");
        stb.append("     T1.RANK_AVG3, ");
        stb.append("     T1.RANK_C_AVG3, ");
        stb.append("     T1.RANK_G_AVG3, ");
        stb.append("     T1.AVG_C_AVG3, ");
        stb.append("     T1.AVG_G_AVG3, ");
        stb.append("     T1.AVG_C_HIGHSCORE3, ");
        stb.append("     T1.AVG_C_LOWSCORE3, ");
        stb.append("     T1.AVG_G_HIGHSCORE3, ");
        stb.append("     T1.AVG_G_LOWSCORE3, ");
        stb.append("     T1.AVG_C_COUNT3, ");
        stb.append("     T1.AVG_G_COUNT3, ");
        //平均値
        stb.append("     CAST(NULL AS DECIMAL(10)) AS RANK_TOTAL_AVG9, ");
        //合計
        stb.append("     CAST(NULL AS SMALLINT) RANK_TOTAL_SCORE9, ");
        stb.append("     T1.RANK_YEAR_SCORE99, ");
        stb.append("     CAST(NULL AS DECIMAL(10)) AS RANK_YEAR_AVG99, ");
        stb.append("     T1.RANK_YEAR_CLSAVG99, ");
        stb.append("     T1.AVG_C_YEAR_COUNT99, ");
        stb.append("     T1.RANK_YEAR_GRDAVG99, ");
        stb.append("     T1.AVG_G_YEAR_COUNT99, ");
        stb.append("     T1.COMP_CREDIT ");

        stb.append(" FROM ");
        stb.append("     SCHNO T2 ");
        stb.append("     LEFT JOIN RECORD T1 ");
        stb.append("            ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     INNER JOIN CHAIR_A T5 ");
        stb.append("            ON T5.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("           AND T5.SCHREGNO      = T1.SCHREGNO ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       AND T5.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("     INNER JOIN CLASS_MST T3 ");
        stb.append("            ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
        }
        stb.append("     INNER JOIN SUBCLASS_MST T4 ");
        stb.append("            ON T4.SUBCLASSCD    = T1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       AND T4.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        //合計
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T2.SEMESTER, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T2.SCHREGNO, ");
        stb.append("     SUBSTR(T1.SUBCLASSCD,1,2) AS CLASSCD, ");
        stb.append("     '' AS CLASSNAME, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     '' AS SUBCLASSNAME, ");
        //1学期
        stb.append("     T1.RANK_SCORE1, ");
        stb.append("     T1.RANK_AVG1, ");
        stb.append("     CAST(NULL AS DECIMAL(10)) AS RANK_C_AVG1, ");
        stb.append("     CAST(NULL AS DECIMAL(10)) AS RANK_G_AVG1, ");
        stb.append("     CAST(NULL AS DECIMAL(10)) AS AVG_C_AVG1, ");
        stb.append("     CAST(NULL AS DECIMAL(10)) AS AVG_G_AVG1, ");
        stb.append("     T1.AVG_C_HIGHSCORE1, ");
        stb.append("     T1.AVG_C_LOWSCORE1, ");
        stb.append("     T1.AVG_G_HIGHSCORE1, ");
        stb.append("     T1.AVG_G_LOWSCORE1, ");
        stb.append("     T1.AVG_C_COUNT1, ");
        stb.append("     T1.AVG_G_COUNT1, ");
        //2学期
        stb.append("     T1.RANK_SCORE2, ");
        stb.append("     T1.RANK_AVG2, ");
        stb.append("     CAST(NULL AS DECIMAL(10)) AS RANK_C_AVG2, ");
        stb.append("     CAST(NULL AS DECIMAL(10)) AS RANK_G_AVG2, ");
        stb.append("     CAST(NULL AS DECIMAL(10)) AS AVG_C_AVG2, ");
        stb.append("     CAST(NULL AS DECIMAL(10)) AS AVG_G_AVG2, ");
        stb.append("     T1.AVG_C_HIGHSCORE2, ");
        stb.append("     T1.AVG_C_LOWSCORE2, ");
        stb.append("     T1.AVG_G_HIGHSCORE2, ");
        stb.append("     T1.AVG_G_LOWSCORE2, ");
        stb.append("     T1.AVG_C_COUNT2, ");
        stb.append("     T1.AVG_G_COUNT2, ");
        //3学期
        stb.append("     T1.RANK_SCORE3, ");
        stb.append("     T1.RANK_AVG3, ");
        stb.append("     CAST(NULL AS DECIMAL(10)) AS RANK_C_AVG3, ");
        stb.append("     CAST(NULL AS DECIMAL(10)) AS RANK_G_AVG3, ");
        stb.append("     CAST(NULL AS DECIMAL(10)) AS AVG_C_AVG3, ");
        stb.append("     CAST(NULL AS DECIMAL(10)) AS AVG_G_AVG3, ");
        stb.append("     T1.AVG_C_HIGHSCORE3, ");
        stb.append("     T1.AVG_C_LOWSCORE3, ");
        stb.append("     T1.AVG_G_HIGHSCORE3, ");
        stb.append("     T1.AVG_G_LOWSCORE3, ");
        stb.append("     T1.AVG_C_COUNT3, ");
        stb.append("     T1.AVG_G_COUNT3, ");
        //平均値
        stb.append("     T1.RANK_TOTAL_AVG9, ");
        //合計
        stb.append("     T1.RANK_TOTAL_SCORE9, ");

        stb.append("     T1.RANK_YEAR_SCORE99, ");
        stb.append("     T1.RANK_YEAR_AVG99, ");
        stb.append("     T1.RANK_YEAR_CLSAVG99, ");
        stb.append("     CAST(NULL AS DECIMAL(10))  AS AVG_C_YEAR_COUNT99, ");
        stb.append("     T1.RANK_YEAR_GRDAVG99, ");
        stb.append("     CAST(NULL AS DECIMAL(10))  AS AVG_G_YEAR_COUNT99, ");
        stb.append("     CAST(NULL AS SMALLINT) AS COMP_CREDIT ");

        stb.append(" FROM ");
        stb.append("     SCHNO T2 ");
        stb.append("     INNER JOIN RECORD T1 ");
        stb.append("            ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("           AND T1.SUBCLASSCD = '" + SUBCLASSCD999999 + "' ");

        stb.append(" ORDER BY ");
        stb.append("     ATTENDNO, ");
        stb.append("     SUBCLASSCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" SCHOOL_KIND, ");
            stb.append(" CURRICULUM_CD, ");
        }
        stb.append("     SEMESTER ");

        return stb.toString();
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT  T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append("    FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
        stb.append("    WHERE   T1.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("        AND T1.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("        AND T1.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("    AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");

        //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END) ");
        stb.append("                             OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END)) ) ");
//        //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
//        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
//        stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//        stb.append(                           "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + parameter._date + "' THEN T2.EDATE ELSE '" + parameter._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        //                      異動者チェック：休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND S1.TRANSFERCD IN ('2') AND CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append("    ) ");

        //メイン表
        stb.append("     SELECT  REGD.SCHREGNO");
        stb.append("            ,REGD.SEMESTER ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,REGDG.SCHOOL_KIND ");
        stb.append("            ,REGDH.GRADE_NAME ");
        stb.append("            ,REGDH.HR_NAME ");
        stb.append("            ,STF1.STAFFNAME ");
        stb.append("            ,STF2.STAFFNAME AS STAFFNAME2 ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGD.GRADE ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,REGD.COURSECD ");
        stb.append("            ,REGD.MAJORCD ");
        stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
        stb.append("            ,MAJOR.MAJORNAME ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
        stb.append("            ,R1.TOTALSTUDYTIME AS TOTALSTUDYTIME1 ");
        stb.append("            ,R2.TOTALSTUDYTIME AS TOTALSTUDYTIME2 ");
        stb.append("            ,R3.TOTALSTUDYTIME AS TOTALSTUDYTIME3 ");
        stb.append("            ,RD01.REMARK1 AS COMMITTEE ");
        stb.append("            ,RD021.REMARK1 AS CLUB1 ");
        stb.append("            ,RD022.REMARK1 AS CLUB2 ");
        stb.append("            ,RD023.REMARK1 AS CLUB3 ");
        stb.append("            ,RD03.REMARK1 AS QUALIFICATION ");
        stb.append("            ,R9.COMMUNICATION ");
        stb.append("            ,CASE WHEN L6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS ENT_TYPE "); //1:一貫 0:高入
        stb.append("     FROM    SCHNO_A REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDG.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON MAJOR.COURSECD = REGD.COURSECD ");
        stb.append("                  AND MAJOR.MAJORCD = REGD.MAJORCD ");
        stb.append("     LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = REGDH.TR_CD2 ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT R1 ");
        stb.append("            ON R1.YEAR     = REGD.YEAR ");
        stb.append("           AND R1.SEMESTER = '1' ");
        stb.append("           AND R1.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT R2 ");
        stb.append("            ON R2.YEAR     = REGD.YEAR ");
        stb.append("           AND R2.SEMESTER = '2' ");
        stb.append("           AND R2.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT R3 ");
        stb.append("            ON R3.YEAR     = REGD.YEAR ");
        stb.append("           AND R3.SEMESTER = '3' ");
        stb.append("           AND R3.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT R9 ");
        stb.append("            ON R9.YEAR     = REGD.YEAR ");
        stb.append("           AND R9.SEMESTER = '" + SEMEALL + "' ");
        stb.append("           AND R9.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD01 ");
        stb.append("            ON RD01.YEAR     = REGD.YEAR ");
        stb.append("           AND RD01.SEMESTER = '" + SEMEALL + "' ");
        stb.append("           AND RD01.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD01.DIV      = '01' ");
        stb.append("           AND RD01.CODE     = '01' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD021 ");
        stb.append("            ON RD021.YEAR     = REGD.YEAR ");
        stb.append("           AND RD021.SEMESTER = '1' ");
        stb.append("           AND RD021.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD021.DIV      = '02' ");
        stb.append("           AND RD021.CODE     = '01' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD022 ");
        stb.append("            ON RD022.YEAR     = REGD.YEAR ");
        stb.append("           AND RD022.SEMESTER = '2' ");
        stb.append("           AND RD022.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD022.DIV      = '02' ");
        stb.append("           AND RD022.CODE     = '01' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD023 ");
        stb.append("            ON RD023.YEAR     = REGD.YEAR ");
        stb.append("           AND RD023.SEMESTER = '3' ");
        stb.append("           AND RD023.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD023.DIV      = '02' ");
        stb.append("           AND RD023.CODE     = '01' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD03 ");
        stb.append("            ON RD03.YEAR     = REGD.YEAR ");
        stb.append("           AND RD03.SEMESTER = '" + SEMEALL + "' ");
        stb.append("           AND RD03.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD03.DIV      = '03' ");
        stb.append("           AND RD03.CODE     = '01' ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT L6 ON L6.SCHREGNO = REGD.SCHREGNO AND L6.SCHOOL_KIND = 'J' ");
        stb.append("     WHERE   REGD.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO ");

        return stb.toString();
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private class Student {
        String _schregno;
        String _name;
        String _schoolKind;
        String _gradename;
        String _hrname;
        String _staffname;
        String _staffname2;
        String _attendno;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _course;
        String _majorname;
        String _hrClassName1;
        String _entyear;
        String _totalstudytime1;
        String _totalstudytime2;
        String _totalstudytime3;
        String _committee;
        String _club1;
        String _club2;
        String _club3;
        String _qualification;
        String _communication;
        String _entType;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map _attendSubClassMap = new TreeMap();

        public Student() {
        }
    }

    private class JviewRecord {
        final String _subclassCd;
        final String _semester;
        final String _viewCd;
        final String _status;
        final String _statusName;
        final String _score;
        final String _hyouka;
        private JviewRecord(
                final String subclassCd,
                final String semester,
                final String viewCd,
                final String status,
                final String statusName,
                final String score,
                final String hyouka
        ) {
            _subclassCd = subclassCd;
            _semester = semester;
            _viewCd = viewCd;
            _status = status;
            _statusName = statusName;
            _score = score;
            _hyouka = hyouka;
        }
    }

    private class RankSdiv {
        final String _score;
        final String _hyouka;
        private RankSdiv(
                final String score,
                final String hyouka
        ) {
            _score = score;
            _hyouka = hyouka;
        }
    }

    private class ScoreData {
        final String _classCd;
        final String _className;
        final String _subclassCd;
        final String _curriculumCd;
        final String _schoolKind;
        final String _subclassName;
        final String _rank_Score1;
        final String _rank_Avg1;
        final String _rank_C_Avg1;
        final String _rank_G_Avg1;
        final String _avg_C_Avg1;
        final String _avg_G_Avg1;
        final String _avg_C_HighScore1;
        final String _avg_C_LowScore1;
        final String _avg_G_HighScore1;
        final String _avg_G_LowScore1;
        final String _avg_C_Count1;
        final String _avg_G_Count1;
        final String _rank_Score2;
        final String _rank_Avg2;
        final String _rank_C_Avg2;
        final String _rank_G_Avg2;
        final String _avg_C_Avg2;
        final String _avg_G_Avg2;
        final String _avg_C_HighScore2;
        final String _avg_C_LowScore2;
        final String _avg_G_HighScore2;
        final String _avg_G_LowScore2;
        final String _avg_C_Count2;
        final String _avg_G_Count2;
        final String _rank_Score3;
        final String _rank_Avg3;
        final String _rank_C_Avg3;
        final String _rank_G_Avg3;
        final String _avg_C_Avg3;
        final String _avg_G_Avg3;
        final String _avg_C_HighScore3;
        final String _avg_C_LowScore3;
        final String _avg_G_HighScore3;
        final String _avg_G_LowScore3;
        final String _avg_C_Count3;
        final String _avg_G_Count3;

        final String _rank_Total_Avg9;

        final String _rank_Total_Score9;

        final String _rank_Year_Score99;
        final String _rank_Year_Avg99;
        final String _rank_Year_Clsavg99;
        final String _avg_C_Year_Count99;
        final String _rank_Year_Grdavg99;
        final String _avg_G_Year_Count99;
        final String _comp_Credit;

        public ScoreData (final String classCd, final String className, final String subclassCd, final String subclassName, final String rank_Score1, final String rank_Avg1, final String rank_C_Avg1, final String rank_G_Avg1, final String avg_C_Avg1, final String avg_G_Avg1, final String avg_C_HighScore1, final String avg_C_LowScore1, final String avg_G_HighScore1, final String avg_G_LowScore1, final String avg_C_Count1, final String avg_G_Count1, final String rank_Score2, final String rank_Avg2, final String rank_C_Avg2, final String rank_G_Avg2, final String avg_C_Avg2, final String avg_G_Avg2, final String avg_C_HighScore2, final String avg_C_LowScore2, final String avg_G_HighScore2, final String avg_G_LowScore2, final String avg_C_Count2, final String avg_G_Count2, final String rank_Score3, final String rank_Avg3, final String rank_C_Avg3, final String rank_G_Avg3, final String avg_C_Avg3, final String avg_G_Avg3, final String avg_C_HighScore3, final String avg_C_LowScore3, final String avg_G_HighScore3, final String avg_G_LowScore3, final String avg_C_Count3, final String avg_G_Count3, final String rank_Total_Avg9, final String rank_Total_Score9,
                final String rank_Year_Score99, final String rank_Year_Avg99, final String rank_Year_Clsavg99, final String avg_C_Year_Count99, final String rank_Year_Grdavg99, final String avg_G_Year_Count99, final String comp_Credit,
                final String curriculumCd, final String schoolKind
                )
        {
            _classCd = classCd;
            _className = className;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _rank_Score1 = rank_Score1;
            _rank_Avg1 = rank_Avg1;
            _rank_C_Avg1 = rank_C_Avg1;
            _rank_G_Avg1 = rank_G_Avg1;
            _avg_C_Avg1 = avg_C_Avg1;
            _avg_G_Avg1 = avg_G_Avg1;
            _avg_C_HighScore1 = avg_C_HighScore1;
            _avg_C_LowScore1 = avg_C_LowScore1;
            _avg_G_HighScore1 = avg_G_HighScore1;
            _avg_G_LowScore1 = avg_G_LowScore1;
            _avg_C_Count1 = avg_C_Count1;
            _avg_G_Count1 = avg_G_Count1;
            _rank_Score2 = rank_Score2;
            _rank_Avg2 = rank_Avg2;
            _rank_C_Avg2 = rank_C_Avg2;
            _rank_G_Avg2 = rank_G_Avg2;
            _avg_C_Avg2 = avg_C_Avg2;
            _avg_G_Avg2 = avg_G_Avg2;
            _avg_C_HighScore2 = avg_C_HighScore2;
            _avg_C_LowScore2 = avg_C_LowScore2;
            _avg_G_HighScore2 = avg_G_HighScore2;
            _avg_G_LowScore2 = avg_G_LowScore2;
            _avg_C_Count2 = avg_C_Count2;
            _avg_G_Count2 = avg_G_Count2;
            _rank_Score3 = rank_Score3;
            _rank_Avg3 = rank_Avg3;
            _rank_C_Avg3 = rank_C_Avg3;
            _rank_G_Avg3 = rank_G_Avg3;
            _avg_C_Avg3 = avg_C_Avg3;
            _avg_G_Avg3 = avg_G_Avg3;
            _avg_C_HighScore3 = avg_C_HighScore3;
            _avg_C_LowScore3 = avg_C_LowScore3;
            _avg_G_HighScore3 = avg_G_HighScore3;
            _avg_G_LowScore3 = avg_G_LowScore3;
            _avg_C_Count3 = avg_C_Count3;
            _avg_G_Count3 = avg_G_Count3;
            _rank_Total_Avg9 = rank_Total_Avg9;
            _rank_Total_Score9 = rank_Total_Score9;
            _rank_Year_Score99 = rank_Year_Score99;
            _rank_Year_Avg99 = rank_Year_Avg99;
            _rank_Year_Clsavg99 = rank_Year_Clsavg99;
            _avg_C_Year_Count99 = avg_C_Year_Count99;
            _rank_Year_Grdavg99 = rank_Year_Grdavg99;
            _avg_G_Year_Count99 = avg_G_Year_Count99;
            _comp_Credit = comp_Credit;
            _curriculumCd = curriculumCd;
            _schoolKind = schoolKind;
        }

        private String getConSubClassCd() {
            return "1".equals(_param._useCurriculumcd) ? _classCd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclassCd : _subclassCd;
        }
        private String getRankScore(final String semes) {
            return getRankScore(semes, false);
        }
        private String getRankScore(final String semes, final boolean retNullFlg) {
            final String retStr;
            if ("1".equals(semes)) {
                retStr = _rank_Score1;
            } else if ("2".equals(semes)) {
                retStr = _rank_Score2;
            } else if ("3".equals(semes)) {
                retStr = _rank_Score3;
            } else {
                retStr = "";
            }
            if (!retNullFlg) {
                return StringUtils.defaultString(retStr, "");
            } else {
                return retStr;
            }
        }
        private String getRankAvg(final String semes) {
            return getRankAvg(semes, false);
        }
        private String getRankAvg(final String semes, final boolean retNullFlg) {
            final String retStr;
            if ("1".equals(semes)) {
                retStr = _rank_Avg1;
            } else if ("2".equals(semes)) {
                retStr = _rank_Avg2;
            } else if ("3".equals(semes)) {
                retStr = _rank_Avg3;
            } else {
                retStr = "";
            }
            if (!retNullFlg) {
                return StringUtils.defaultString(retStr, "");
            } else {
                return retStr;
            }
        }

        private String getRankCAvg(final String semes) {
            return getRankCAvg(semes, false);
        }
        private String getRankCAvg(final String semes, final boolean retNullFlg) {
            final String retStr;
            if ("1".equals(semes)) {
                retStr = _rank_C_Avg1;
            } else if ("2".equals(semes)) {
                retStr = _rank_C_Avg2;
            } else if ("3".equals(semes)) {
                retStr = _rank_C_Avg3;
            } else {
                retStr = "";
            }
            if (!retNullFlg) {
                return StringUtils.defaultString(retStr, "");
            } else {
                return retStr;
            }
        }

        private String getRankGAvg(final String semes) {
            return getRankGAvg(semes, false);
        }
        private String getRankGAvg(final String semes, final boolean retNullFlg) {
            final String retStr;
            if ("1".equals(semes)) {
                retStr = _rank_G_Avg1;
            } else if ("2".equals(semes)) {
                retStr = _rank_G_Avg2;
            } else if ("3".equals(semes)) {
                retStr = _rank_G_Avg3;
            } else {
                retStr = "";
            }
            if (!retNullFlg) {
                return StringUtils.defaultString(retStr, "");
            } else {
                return retStr;
            }
        }

        private String getAvgCAvg(final String semes) {
            return getAvgCAvg(semes, false);
        }
        private String getAvgCAvg(final String semes, final boolean retNullFlg) {
            final String retStr;
            if ("1".equals(semes)) {
                retStr = _avg_C_Avg1;
            } else if ("2".equals(semes)) {
                retStr = _avg_C_Avg2;
            } else if ("3".equals(semes)) {
                retStr = _avg_C_Avg3;
            } else {
                retStr = "";
            }
            if (!retNullFlg) {
                return StringUtils.defaultString(retStr, "");
            } else {
                return retStr;
            }
        }
        private String getAvgGAvg(final String semes) {
            return getAvgGAvg(semes, false);
        }
        private String getAvgGAvg(final String semes, final boolean retNullFlg) {
            final String retStr;
            if ("1".equals(semes)) {
                retStr = _avg_G_Avg1;
            } else if ("2".equals(semes)) {
                retStr = _avg_G_Avg2;
            } else if ("3".equals(semes)) {
                retStr = _avg_G_Avg3;
            } else {
                retStr = "";
            }
            if (!retNullFlg) {
                return StringUtils.defaultString(retStr, "");
            } else {
                return retStr;
            }
        }

        private String getAvgCHighScore(final String semes) {
            return getAvgCHighScore(semes, false);
        }
        private String getAvgCHighScore(final String semes, final boolean retNullFlg) {
            final String retStr;
            if ("1".equals(semes)) {
                retStr = _avg_C_HighScore1;
            } else if ("2".equals(semes)) {
                retStr = _avg_C_HighScore2;
            } else if ("3".equals(semes)) {
                retStr = _avg_C_HighScore3;
            } else {
                retStr = "";
            }
            if (!retNullFlg) {
                return StringUtils.defaultString(retStr, "");
            } else {
                return retStr;
            }
        }

        private String getAvgCLowScore(final String semes) {
            return getAvgCLowScore(semes, false);
        }
        private String getAvgCLowScore(final String semes, final boolean retNullFlg) {
            final String retStr;
            if ("1".equals(semes)) {
                retStr = _avg_C_LowScore1;
            } else if ("2".equals(semes)) {
                retStr = _avg_C_LowScore2;
            } else if ("3".equals(semes)) {
                retStr = _avg_C_LowScore3;
            } else {
                retStr = "";
            }
            if (!retNullFlg) {
                return StringUtils.defaultString(retStr, "");
            } else {
                return retStr;
            }
        }

        private String getAvgGHighScore(final String semes) {
            return getAvgGHighScore(semes, false);
        }
        private String getAvgGHighScore(final String semes, final boolean retNullFlg) {
            final String retStr;
            if ("1".equals(semes)) {
                retStr = _avg_G_HighScore1;
            } else if ("2".equals(semes)) {
                retStr = _avg_G_HighScore2;
            } else if ("3".equals(semes)) {
                retStr = _avg_G_HighScore3;
            } else {
                retStr = "";
            }
            if (!retNullFlg) {
                return StringUtils.defaultString(retStr, "");
            } else {
                return retStr;
            }
        }

        private String getAvgGLowScore(final String semes) {
            return getAvgGLowScore(semes, false);
        }
        private String getAvgGLowScore(final String semes, final boolean retNullFlg) {
            final String retStr;
            if ("1".equals(semes)) {
                retStr = _avg_G_LowScore1;
            } else if ("2".equals(semes)) {
                retStr = _avg_G_LowScore2;
            } else if ("3".equals(semes)) {
                retStr = _avg_G_LowScore3;
            } else {
                retStr = "";
            }
            if (!retNullFlg) {
                return StringUtils.defaultString(retStr, "");
            } else {
                return retStr;
            }
        }

        private String getAvgCCount(final String semes) {
            return getAvgCCount(semes, false);
        }
        private String getAvgCCount(final String semes, final boolean retNullFlg) {
            final String retStr;
            if ("1".equals(semes)) {
                retStr = _avg_C_Count1;
            } else if ("2".equals(semes)) {
                retStr = _avg_C_Count2;
            } else if ("3".equals(semes)) {
                retStr = _avg_C_Count3;
            } else {
                retStr = "";
            }
            if (!retNullFlg) {
                return StringUtils.defaultString(retStr, "");
            } else {
                return retStr;
            }
        }

        private String getAvgGCount(final String semes) {
            return getAvgGCount(semes, false);
        }
        private String getAvgGCount(final String semes, final boolean retNullFlg) {
            final String retStr;
            if ("1".equals(semes)) {
                retStr = _avg_G_Count1;
            } else if ("2".equals(semes)) {
                retStr = _avg_G_Count2;
            } else if ("3".equals(semes)) {
                retStr = _avg_G_Count3;
            } else {
                retStr = "";
            }
            if (!retNullFlg) {
                return StringUtils.defaultString(retStr, "");
            } else {
                return retStr;
            }
        }
    }

    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
        final int _det006;
        final int _det007;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int abroad,
                final int det006,
                final int det007
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _abroad = abroad;
            _det006 = det006;
            _det007 = det007;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            PreparedStatement psAtDetail = null;
            ResultSet rsAtDetail = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._loginYear,
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                //log.debug(" attend sql = " + sql);
                psAtSeme = db2.prepareStatement(sql);

                final String detailSql = getDetailSql(param, dateRange);
                psAtDetail = db2.prepareStatement(detailSql);
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    psAtDetail.setString(1, student._schregno);
                    psAtDetail.setString(2, student._schregno);
                    rsAtDetail = psAtDetail.executeQuery();

                    int set006 = 0;
                    int set007 = 0;
                    while (rsAtDetail.next()) {
                        set006 = rsAtDetail.getInt("CNT006");
                        set007 = rsAtDetail.getInt("CNT007");
                    }
                    DbUtils.closeQuietly(rsAtSeme);

                    psAtSeme.setString(1, student._schregno);
                    rsAtSeme = psAtSeme.executeQuery();

                    while (rsAtSeme.next()) {
                        if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                            continue;
                        }

                        final Attendance attendance = new Attendance(
                                rsAtSeme.getInt("LESSON"),
                                rsAtSeme.getInt("MLESSON"),
                                rsAtSeme.getInt("SUSPEND"),
                                rsAtSeme.getInt("MOURNING"),
                                rsAtSeme.getInt("SICK"),
                                rsAtSeme.getInt("PRESENT"),
                                rsAtSeme.getInt("LATE"),
                                rsAtSeme.getInt("EARLY"),
                                rsAtSeme.getInt("TRANSFER_DATE"),
                                set006,
                                set007
                        );
                        student._attendMap.put(dateRange._key, attendance);
                    }
                    DbUtils.closeQuietly(rsAtSeme);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(psAtSeme);
                db2.commit();
            }
        }

        private static String getDetailSql(final Param param, final DateRange dateRange) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCH_T(SCHREGNO) AS ( ");
            stb.append("     VALUES(CAST(? AS VARCHAR(8))) ");
            stb.append(" ), DET_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     SEQ, ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(CNT) AS CNT ");
            stb.append(" FROM ");
            stb.append("     ATTEND_SEMES_DETAIL_DAT ");
            stb.append(" WHERE ");
            stb.append("     COPYCD = '0' ");
            stb.append("     AND YEAR || MONTH BETWEEN '" + param._loginYear + "04' AND '" + (Integer.parseInt(param._loginYear) + 1) + "03' ");
            stb.append("     AND SEMESTER = '" + dateRange._key + "' ");
            stb.append("     AND SCHREGNO = ? ");
            stb.append("     AND SEQ IN ('006', '007') ");
            stb.append(" GROUP BY ");
            stb.append("     SEQ, ");
            stb.append("     SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     VALUE(DET006.CNT, 0) AS CNT006, ");
            stb.append("     VALUE(DET007.CNT, 0) AS CNT007 ");
            stb.append(" FROM ");
            stb.append("     SCH_T ");
            stb.append("     LEFT JOIN DET_T DET006 ON SCH_T.SCHREGNO = DET006.SCHREGNO ");
            stb.append("          AND DET006.SEQ = '006' ");
            stb.append("     LEFT JOIN DET_T DET007 ON SCH_T.SCHREGNO = DET007.SCHREGNO ");
            stb.append("          AND DET007.SEQ = '007' ");

            return stb.toString();
        }

    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;
        boolean _isOver;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick, final BigDecimal late, final BigDecimal early) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
            _late = late;
            _early = early;
        }

        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString())  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange) {
            log.info(" subclass attendance dateRange = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }

            for (final Iterator it2 = param._attendSemesterDetailList.iterator(); it2.hasNext();) {
                final SemesterDetail semesDetail = (SemesterDetail) it2.next();
                if (null == semesDetail) {
                    continue;
                }
                if (null == semesDetail._sdate || null == semesDetail._edate || semesDetail._sdate.compareTo(param._date) >= 0) {
                    continue;
                }

                final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    param._attendParamMap.put("schregno", "?");

                    final String sql = AttendAccumulate.getAttendSubclassSql(
                            param._loginYear,
                            dateRange._key,
                            dateRange._sdate,
                            edate,
                            param._attendParamMap
                    );

                    ps = db2.prepareStatement(sql);

                    for (final Iterator it3 = studentList.iterator(); it3.hasNext();) {
                        final Student student = (Student) it3.next();

                        ps.setString(1, student._schregno);
                        rs = ps.executeQuery();

                        while (rs.next()) {
                            if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                                continue;
                            }
                            final String subclasscd = rs.getString("SUBCLASSCD");

                            final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                            if (null == mst) {
                                log.warn("no subclass : " + subclasscd);
                                continue;
                            }
                            final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                            if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                                if (null == student._printSubclassMap.get(subclasscd)) {
                                    continue;
                                }

                                final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                                final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                                final BigDecimal sick = rs.getBigDecimal("SICK2");
                                final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                                final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");
                                final BigDecimal late = rs.getBigDecimal("LATE");
                                final BigDecimal early = rs.getBigDecimal("EARLY");

                                final BigDecimal sick1 = mst._isSaki ? rawReplacedSick : rawSick;
                                final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                                final BigDecimal sick2 = mst._isSaki ? replacedSick : sick;

                                final BigDecimal absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");

                                final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2, late, early);

                                //欠課時数上限
                                final Double absent = Double.valueOf(mst._isSaki ? rs.getString("REPLACED_SICK"): rs.getString("SICK2"));
                                subclassAttendance._isOver = subclassAttendance.judgeOver(absent, absenceHigh);

                                Map setMap = null;
                                Map setSubAttendMap = null;

                                if (student._attendSubClassMap.containsKey(subclasscd)) {
                                    setSubAttendMap = (Map) student._attendSubClassMap.get(subclasscd);
                                    if (setSubAttendMap.containsKey(dateRange._key)) {
                                        setMap = (Map) setSubAttendMap.get(dateRange._key);
                                    } else {
                                        setMap = new TreeMap();
                                    }
                                } else {
                                    setSubAttendMap = new TreeMap();
                                    setMap = new TreeMap();
                                }

//                                if (student._attendSubClassMap.containsKey(subclasscd)) {
//                                    setSubAttendMap = (Map) student._attendSubClassMap.get(subclasscd);
//                                } else {
//                                    setSubAttendMap = new TreeMap();
//                                }

                                setMap.put(semesDetail._cdSemesterDetail, subclassAttendance);

                                setSubAttendMap.put(dateRange._key, setMap);

                                student._attendSubClassMap.put(subclasscd, setSubAttendMap);
                            }

                        }

                        DbUtils.closeQuietly(rs);
                    }

                } catch (Exception e) {
                    log.fatal("exception!", e);
                } finally {
                    DbUtils.closeQuietly(ps);
                    db2.commit();
                }
            }
        }

        /**
         * 欠課時数超過ならTrueを戻します。
         * @param absent 欠課時数
         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
         * @return
         */
        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0.0 == absenceHigh.doubleValue()) {
                return false;
            }
            if (absenceHigh.doubleValue() < absent.doubleValue()) {
                return true;
            }
            return false;
        }
    }

    private static class Semester {
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
        public int compareTo(final Object o) {
            if (!(o instanceof Semester)) {
                return 0;
            }
            Semester s = (Semester) o;
            return _semester.compareTo(s._semester);
        }
    }

    private static class SubclassMst implements Comparable {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        final String _calculateCreditFlg;
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final boolean isSaki, final boolean isMoto, final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
            int rtn;
            rtn = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _classcd && null == mst._classcd) {
                return 0;
            } else if (null == _classcd) {
                return 1;
            } else if (null == mst._classcd) {
                return -1;
            }
            rtn = _classcd.compareTo(mst._classcd);
            if (0 != rtn) { return rtn; }
            rtn = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _subclasscd && null == mst._subclasscd) {
                return 0;
            } else if (null == _subclasscd) {
                return 1;
            } else if (null == mst._subclasscd) {
                return -1;
            }
            return _subclasscd.compareTo(mst._subclasscd);
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }
    }

    private static class DateRange {
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

    private static class JviewGrade {
        final String _subclassCd;
        final String _viewCd;
        final String _viewName;
        public JviewGrade(final String subclassCd, final String viewCd, final String viewName) {
            _subclassCd = subclassCd;
            _viewCd = viewCd;
            _viewName = viewName;
        }
    }

    private static class SemesterDetail implements Comparable {
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

    private static class TestItem {
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


    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74543 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String[] _categorySelected;
        final String _date;
        final String _gradeHrClass;
        final String _grade;
        final String _loginSemester;
        final String _loginYear;
        final String _prgid;
        final String _semester;

        final Map _d082;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        final List _semesterList;
        private Map _semesterMap;
        private final Map _semesterDetailMap;
        private Map _subclassMstMap;
        private final Map _jviewGradeMap;
        private List _d026List = new ArrayList();
        Map _attendRanges;

        final String _documentroot;
        final String _imagepath;
//        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
//        final String _whiteSpaceImagePath;

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;

        final String _useCurriculumcd;

        final String _use_school_detail_gcm_dat;
        final List _attendTestKindItemList;
        final List _attendSemesterDetailList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categorySelected = request.getParameterValues("category_selected");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _grade = !"".equals(StringUtils.defaultString(_gradeHrClass)) ? _gradeHrClass.substring(0, 2) : "";
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginYear = request.getParameter("YEAR");
            _prgid = request.getParameter("PRGID");
            _semester = request.getParameter("SEMESTER");

            _d082 = getD082(db2);
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);

            setCertifSchoolDat(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _semesterList = getSemesterList(db2);
            _semesterMap = loadSemester(db2);
            _semesterDetailMap = new HashMap();
            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }
            setSubclassMst(db2);
            _jviewGradeMap = getJviewGradeMap(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
//            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
//            _whiteSpaceImagePath = getImageFilePath("whitespace.png");

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _attendTestKindItemList = getTestKindItemList(db2, false, false);
            _attendSemesterDetailList = getAttendSemesterDetailList();
        }

        private Map getD082(
                final DB2UDB db2
        ) {
            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     NAME_MST D082 ");
                stb.append(" WHERE ");
                stb.append("     NAMECD1 = 'D082' ");
                ps = db2.prepareStatement(stb.toString());

                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("NAME1"), rs.getString("NAME1"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + _loginYear + "'"
                        + "   AND GRADE='" + _grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new LinkedMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _loginYear + "' GROUP BY COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
                sql += " UNION ";
                sql += " SELECT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _loginYear + "' GROUP BY ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO, ";
                sql += " L2.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3 ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), isSaki, isMoto, rs.getString("CALCULATE_CREDIT_FLG"));
                    _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private Map getJviewGradeMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     VIEWCD, ");
            stb.append("     VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST ");
            stb.append(" WHERE ");
            stb.append("     GRADE = '" + _grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     VIEWCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                List jviewGradeList = null;
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String viewCd = rs.getString("VIEWCD");
                    final String viewName = rs.getString("VIEWNAME");

                    final JviewGrade jviewGrade = new JviewGrade(subclassCd, viewCd, viewName);
                    if (retMap.containsKey(subclassCd)) {
                        jviewGradeList = (List) retMap.get(subclassCd);
                    } else {
                        jviewGradeList = new ArrayList();
                    }
                    jviewGradeList.add(jviewGrade);
                    retMap.put(subclassCd, jviewGradeList);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '104' ");
            //log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        /**
         * 合併先科目を印刷するか
         */
        private void setPrintSakiKamoku(final DB2UDB db2) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;
            // 名称マスタ「D021」「01」から取得する
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _loginYear+ "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' "));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE3"))) {
                _isPrintSakiKamoku = false;
            }
            log.debug("合併先科目を印刷するか：" + _isPrintSakiKamoku);
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");

            _d026List.clear();
            _d026List.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD"));
            log.info("非表示科目:" + _d026List);
        }

        private List getSemesterList(DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _loginYear + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    Semester semes = new Semester(semester, semestername, sdate, edate);
                    list.add(semes);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private List getTestKindItemList(DB2UDB db2, final boolean useSubclassControl, final boolean addSemester) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map semesterMap = new HashMap();
                for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                    final String seme = (String) it.next();
                    if(!_semesterMap.containsKey(seme)) continue;
                    final Semester semester = (Semester) _semesterMap.get(seme);
                    semesterMap.put(semester._semester, semester);
                }
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH ADMIN_CONTROL_SDIV_SUBCLASSCD AS (");
                if (useSubclassControl) {
                    stb.append("   SELECT DISTINCT ");
                    stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                    stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                    stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                    stb.append("   UNION ALL ");
                }
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-00-00-000000' ");
                stb.append("   UNION ALL ");
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("     AND T1.CLASSCD = '00' ");
                stb.append("     AND T1.CURRICULUM_CD = '00' ");
                stb.append("     AND T1.SUBCLASSCD = '000000' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCORE_DIV, ");
                stb.append("     T1.TESTITEMNAME, ");
                stb.append("     T1.TESTITEMABBV1, ");
                stb.append("     T1.SIDOU_INPUT, ");
                stb.append("     T1.SIDOU_INPUT_INF, ");
                stb.append("     T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.SEMESTER_DETAIL, ");
                stb.append("     T2.SEMESTER AS SEMESTER_DETAIL_SEMESTER, ");
                stb.append("     T2.SEMESTERNAME AS SEMESTERDETAILNAME, ");
                stb.append("     T2.SDATE, ");
                stb.append("     T2.EDATE ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_GCM_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                } else {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                }
                stb.append("    AND T11.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T11.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("    AND T11.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("    AND T11.SCORE_DIV = T1.SCORE_DIV ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("    AND T11.GRADE = T1.GRADE ");
                    stb.append("    AND T11.COURSECD = T1.COURSECD ");
                    stb.append("    AND T11.MAJORCD = T1.MAJORCD ");
                }
                stb.append(" LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ");
                stb.append(" WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD IN "); // 指定の科目が登録されていれば登録された科目、登録されていなければ00-00-00-000000を使用する
                stb.append("    (SELECT MAX(SUBCLASSCD) FROM ADMIN_CONTROL_SDIV_SUBCLASSCD) ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
//                    stb.append("    AND T1.SCHOOLCD = '" + _PRINT_SCHOOLCD + "' ");
//                    stb.append("    AND T1.SCHOOL_KIND = '" + _PRINT_SCHOOLKIND + "' ");
                    stb.append("    AND T1.GRADE = '00' ");
//                    stb.append("    AND T1.COURSECD || '-' || T1.MAJORCD = '"  + _COURSE_MAJOR + "' ");
                }
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

                //log.debug(" testitem sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                String adminSdivSubclasscd = null;
                while (rs.next()) {
                    adminSdivSubclasscd = rs.getString("SUBCLASSCD");
                    final String year = rs.getString("YEAR");
                    final String testkindcd = rs.getString("TESTKINDCD");
                    final String testitemcd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String sidouInput = rs.getString("SIDOU_INPUT");
                    final String sidouInputInf = rs.getString("SIDOU_INPUT_INF");
                    Semester semester = (Semester) semesterMap.get(rs.getString("SEMESTER"));
                    if (null == semester) {
                        continue;
                    }
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final String testitemabbv1 = rs.getString("TESTITEMABBV1");
                    SemesterDetail semesterDetail = null;
                    final String cdsemesterDetail = rs.getString("SEMESTER_DETAIL");
                    if (null != cdsemesterDetail) {
                        if (_semesterDetailMap.containsKey(cdsemesterDetail)) {
                            semesterDetail = (SemesterDetail) _semesterDetailMap.get(cdsemesterDetail);
                        } else {
                            final String semesterdetailname = rs.getString("SEMESTERDETAILNAME");
                            final String sdate = rs.getString("SDATE");
                            final String edate = rs.getString("EDATE");
                            semesterDetail = new SemesterDetail(semester, cdsemesterDetail, semesterdetailname, sdate, edate);
                            Semester semesDetailSemester = (Semester) semesterMap.get(rs.getString("SEMESTER_DETAIL_SEMESTER"));
                            if (null != semesDetailSemester) {
                                semesDetailSemester._semesterDetailList.add(semesterDetail);
                            }
                            _semesterDetailMap.put(semesterDetail._cdSemesterDetail, semesterDetail);
                        }
                    }
                    final TestItem testItem = new TestItem(
                            year, semester, testkindcd, testitemcd, scoreDiv, testitemname, testitemabbv1, sidouInput, sidouInputInf,
                            semesterDetail);
                    final boolean isGakunenHyotei = SDIV9990009.equals(testItem.getTestcd());
                    if (isGakunenHyotei) {
                        testItem._printKettenFlg = 1;
                    } else if (!SEMEALL.equals(semester._semester) && "09".equals(scoreDiv)) { // 9学期以外の09=9学期以外の仮評定
                        testItem._printKettenFlg = -1;
                    } else {
                        testItem._printKettenFlg = 2;
                    }
                    if (isGakunenHyotei) {
                        final TestItem testItemKari = new TestItem(
                                year, semester, testkindcd, testitemcd, scoreDiv, "仮評定", "仮評定", sidouInput, sidouInputInf,
                                semesterDetail);
                        testItemKari._printKettenFlg = -1;
                        testItemKari._isGakunenKariHyotei = true;
                        if (addSemester) {
                            semester._testItemList.add(testItemKari);
                        }
                        list.add(testItemKari);
                    }
                    if (addSemester) {
                        semester._testItemList.add(testItem);
                    }
                    list.add(testItem);
                }
                log.debug(" testitem admin_control_sdiv_dat subclasscd = " + adminSdivSubclasscd);

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug(" testcd = " + list);
            return list;
        }

        private List getAttendSemesterDetailList() {
            final Map rtn = new TreeMap();
            for (final Iterator it = _attendTestKindItemList.iterator(); it.hasNext();) {
                final TestItem item = (TestItem) it.next();
                if (null != item._semesterDetail && null != item._semesterDetail._cdSemesterDetail) {
                    rtn.put(item._semesterDetail._cdSemesterDetail, item._semesterDetail);
                }
            }
            Semester semester9 = null;
            for (final Iterator it = _semesterList.iterator(); it.hasNext();) {
                final Semester semester = (Semester) it.next();
                if (semester._semester.equals(SEMEALL)) {
                    semester9 = semester;
                    final SemesterDetail semesterDetail9 = new SemesterDetail(semester9, SEMEALL, "学年", semester9._dateRange._sdate, semester9._dateRange._edate);
                    semester9._semesterDetailList.add(semesterDetail9);
                    rtn.put(SEMEALL, semesterDetail9);
                    break;
                }
            }
            return new ArrayList(rtn.values());
        }
    }
}

// eof

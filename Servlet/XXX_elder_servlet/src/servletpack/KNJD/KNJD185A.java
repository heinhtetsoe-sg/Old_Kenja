/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * 作成日: 2020/07/08
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD185A {

    private static final Log log = LogFactory.getLog(KNJD185A.class);

    private static final String SEMEALL = "9";
    private static final String SEME3 = "3";
    private static final String SELECT_CLASSCD_90 = "90";

    private static final String SCORE010101 = "01-01-01";
    private static final String SCORE020201 = "02-02-01";
    private static final String SCORE990008 = "99-00-08";
    private static final String SCORE990009 = "99-00-09";

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

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
        final List studentList = getList(db2);

        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
            final DateRange range = (DateRange) rit.next();
            //下段の出欠
            Attendance.load(db2, _param, studentList, range);
        }

        for (final Iterator itTest = _param._testItemMstSdivMap.keySet().iterator(); itTest.hasNext();) {
            final String testCd = (String) itTest.next();
            final TestItemMstSdiv itemMstSdiv = (TestItemMstSdiv) _param._testItemMstSdivMap.get(testCd);
            //欠課
            if (null != itemMstSdiv) {
                SubclassAttendance.load(db2, _param, studentList, testCd, itemMstSdiv._semester, itemMstSdiv._sDate, itemMstSdiv._eDate);
            }
        }
        if (null != _param._semester9) {
            SubclassAttendance.load(db2, _param, studentList, SEMEALL, SEMEALL, KnjDbUtils.getString(_param._semester9, "SDATE"), KnjDbUtils.getString(_param._semester9, "EDATE"));
        }
        ClubInfo.load(db2, _param, studentList);
        CommitteeInfo.load(db2, _param, studentList);
        printOut(db2, svf, studentList);
    }

    private void printOut(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        final String print_J = "J";
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            final String formName = print_J.equals(student._schoolKind) ? "KNJD185A_2.frm" : "KNJD185A.frm";
            svf.VrSetForm(formName, 4);

            svf.VrsOut("ZIPCD", student._gZip);
            final int gAddr1Len = KNJ_EditEdit.getMS932ByteLength(student._gAddr1);
            final int gAddr2Len = KNJ_EditEdit.getMS932ByteLength(student._gAddr2);
            final int gaddrLen = gAddr1Len > gAddr2Len ? gAddr1Len : gAddr2Len;

            final String gAddrField = gaddrLen > 50 ? "_3" : gaddrLen > 40 ? "_2" : "";
            svf.VrsOut("ADDR1" + gAddrField, student._gAddr1);
            svf.VrsOut("ADDR2" + gAddrField, student._gAddr2);

            final int gNameLen = KNJ_EditEdit.getMS932ByteLength(student._gName);
            final String gNameField = gNameLen > 34 ? "2" : "";
            svf.VrsOut("ADDRESSEE" + gNameField, StringUtils.defaultString(student._gName, "") + "　様");

            svf.VrsOut("NENDO", _param._ctrlYear + "年度");
            if(print_J.equals(student._schoolKind) && ("3".equals(_param._semester) || SEMEALL.equals(_param._semester))) {
                svf.VrsOut("CERT_NAME1", "修了証");
                String grade = "1".equals(_param._disp) ? StringUtils.defaultString(_param._grade) : _param._gradeHr.substring(0, 2);

                /**
                 * 0サプレス
                 */
                Matcher m = Pattern.compile("^0+([0-9]+.*)").matcher(grade);
                grade = m.matches() ? m.group(1) : grade;

                svf.VrsOut("CERT_NAME2", "第" + StringUtils.defaultString(grade, " ") + "学年の課程を修了したことを証明します。");
                svf.VrsOut("SCHOOLNAME2", _param._certifSchoolSchoolName);
            } else {
                svf.VrsOut("SCHOOLNAME", _param._certifSchoolSchoolName);
            }

            if (!print_J.equals(student._schoolKind)) {
                svf.VrsOut("SUBJECT", student._courseCodeName);
            }
            svf.VrsOut("HR_NAME", student._hrname + "　" + student._attendno + "番");
            final int nameLen = KNJ_EditEdit.getMS932ByteLength(student._name);
            final String nameField = nameLen > 30 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, student._name);

            svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath);

            final String jobName = print_J.equals(student._schoolKind) ? "学年主任" : "学年・コース主任";
            final String schoolStLogoImagePath = _param.getStampFilePath(student._stampNo + ".bmp");

            svf.VrsOut("JOB_NAME1", jobName + "　" + student._staffname);
            if(print_J.equals(student._schoolKind) && ("3".equals(_param._semester) || SEMEALL.equals(_param._semester)) ) {
                svf.VrsOut("PRINCIPAL_NAME2", _param._certifSchoolJobName + "　" + _param._certifSchoolPrincipalName);
            } else {
                svf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolJobName + "　" + _param._certifSchoolPrincipalName);
            }
            if(print_J.equals(student._schoolKind)) {
                if ("3".equals(_param._semester) || SEMEALL.equals(_param._semester)) {
                    if (_param._schoolPrLogoImagePath != null) {
                        svf.VrsOut("SCHOOL_STAMP2", _param._schoolPrLogoImagePath);
                    }
                } else {
                    if (_param._schoolPrLogoImagePath != null) {
                        svf.VrsOut("SCHOOL_STAMP1", _param._schoolPrLogoImagePath);
                    }
                }
            } else {
                if (_param._schoolPrLogoImagePath != null) {
                    svf.VrsOut("SCHOOL_STAMP", _param._schoolPrLogoImagePath);
                }
            }
            if (schoolStLogoImagePath != null) {
            svf.VrsOut("STAFF_STAMP", schoolStLogoImagePath);
            }
            //出欠記録
            printAttend(svf, student);

            //備考
            if (student != null && student._communication != null) {
                final String[]cutRem = KNJ_EditEdit.get_token(student._communication, 60, 5);
                if (cutRem != null) {
                    for (int cnt = 0;cnt < cutRem.length;cnt++) {
                        if (cutRem[cnt] != null) {
                            svf.VrsOutn("REMARK", cnt + 1, cutRem[cnt]);
                        }
                    }
                }
            }

            //HR係/委員会/部活動
            final int prtLineMax = 4;
            for (Iterator its = _param._getSemesList.iterator();its.hasNext();) {
                final String getSemes = (String)its.next();
                //  HR1/HR2
                if (student._clsCommitteeInfo.containsKey(getSemes)) {
                    Map subMap = (Map)student._clsCommitteeInfo.get(getSemes);
                    final String prtStr = CommitteeInfo.concatMapData(subMap);
                    final String[] cutStr = KNJ_EditEdit.get_token(prtStr, 18, prtLineMax);
                    for (int cnt = 0;cnt < prtLineMax;cnt++) {
                        if (cutStr != null && cutStr[cnt] != null) {
                            svf.VrsOutn("HR" + getSemes, cnt + 1, cutStr[cnt]);
                        }
                    }
                }
                //  COMM1/COMM2
                if (student._schoolCommitteeInfo.containsKey(getSemes)) {
                    Map subMap = (Map)student._schoolCommitteeInfo.get(getSemes);
                    final String prtStr = CommitteeInfo.concatMapData(subMap);
                    final String[] cutStr = KNJ_EditEdit.get_token(prtStr, 18, prtLineMax);
                    for (int cnt = 0;cnt < prtLineMax;cnt++) {
                        if (cutStr != null && cutStr[cnt] != null) {
                            svf.VrsOutn("COMM" + getSemes, cnt + 1, cutStr[cnt]);
                        }
                    }
                }
                //  CLUB1/CLUB2
                if (student._clubInfo.containsKey(getSemes)) {
                    Map subMap = (Map)student._clubInfo.get(getSemes);
                    final String prtStr = ClubInfo.concatMapData(subMap);
                    final String[] cutStr = KNJ_EditEdit.get_token(prtStr, 18, prtLineMax);
                    for (int cnt = 0;cnt < prtLineMax;cnt++) {
                        if (cutStr != null && cutStr[cnt] != null) {
                            svf.VrsOutn("CLUB" + getSemes, cnt + 1, cutStr[cnt]);
                        }
                    }
                }
            }

            //明細部分
            final List subclassList = subclassListRemoveD026();
            Collections.sort(subclassList);

            //欠課の合計
            //科目マスタでループ
            final Map kekkaTotalMap = new TreeMap();
            for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
                final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                final String subclassCd = subclassMst._subclasscd;

                //生徒が受けていない科目は読み飛ばす
                if (!student._printSubclassMap.containsKey(subclassCd)) {
                    continue;
                }

                //欠課
                if (!print_J.equals(student._schoolKind) && student._attendSubClassMap.containsKey(subclassCd)) {
                    final Map atSubSemeMap = (Map) student._attendSubClassMap.get(subclassCd);
                    for (final Iterator it = _param._testItemMstSdivMap.keySet().iterator(); it.hasNext();) {
                        final String testCd = (String) it.next();
                        int setKekka = 0;
                        if (kekkaTotalMap.containsKey(testCd)) {
                            setKekka = Integer.parseInt((String) kekkaTotalMap.get(testCd));
                        }
                        if (atSubSemeMap.containsKey(testCd)) {
                            final SubclassAttendance attendance = (SubclassAttendance) atSubSemeMap.get(testCd);
                            if (null != attendance._sick) {
                                setKekka += attendance._sick.intValue();
                            }
                        }
                        kekkaTotalMap.put(testCd, String.valueOf(setKekka));
                    }
                }
            }

            //「素点」欄 -- 名称, 合計/平均/順位
            int testCnt = 1;
            int totalKekka = 0;
            for (Iterator itTestItem = _param._testItemMstSdivMap.keySet().iterator(); itTestItem.hasNext();) {
                final String testCd = (String) itTestItem.next();
                final TestItemMstSdiv itemMstSdiv = (TestItemMstSdiv) _param._testItemMstSdivMap.get(testCd);
                svf.VrsOut("TESTITEM" + testCnt, itemMstSdiv._testitemName);
                if (!print_J.equals(student._schoolKind)) {
                    svf.VrsOut("TESTITEM" + testCnt + "_2", itemMstSdiv._testitemName);
                }

                final PrintSubclass printSubclass =  (PrintSubclass) student._printSubclassMap.get("99-" + _param._schoolKind + "-99-" + ALL9);
                if (testCnt < 6 && null != printSubclass && printSubclass._testScoreMap.containsKey(testCd)) {
                    final ScoreData scoreData = (ScoreData) printSubclass._testScoreMap.get(testCd);
                    svf.VrsOutn("TOTAL_POINT1", testCnt, scoreData._score);
                    svf.VrsOutn("AVE_POINT", testCnt, new BigDecimal(scoreData._avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    svf.VrsOutn("RANK1", testCnt, scoreData._gradeRank);
                    svf.VrsOutn("RANK2", testCnt, scoreData._count);
                }
                if (kekkaTotalMap.containsKey(testCd)) {
                    final String kekka = (String) kekkaTotalMap.get(testCd);
                    if (testCnt < 6) {
                        svf.VrsOutn("TOTAL_POINT1", testCnt + 8, kekka);
                    }
                    totalKekka += Integer.parseInt(kekka);
                }
                testCnt++;
            }
            if (!print_J.equals(student._schoolKind)) {
                svf.VrsOutn("TOTAL_POINT1", 14, String.valueOf(totalKekka));
            }

            //「評定」欄 -- 名称, 合計/平均/順位
            svf.VrsOut("SEMESTER9", "学年末");
            if ("9".equals(_param._semester)) {
                final String setSeme = "9";
                final PrintSubclass printSubclass =  (PrintSubclass) student._printSubclassMap.get("99-" + _param._schoolKind + "-99-" + ALL9);
                if (null != printSubclass && printSubclass._testScoreMap.containsKey(setSeme + "-" + SCORE990009)) {
                    final ScoreData scoreData = (ScoreData) printSubclass._testScoreMap.get(setSeme + "-" + SCORE990009);
                    svf.VrsOut("TOTAL_POINT2", scoreData._score);
                    svf.VrsOut("AVE_POINT2", new BigDecimal(scoreData._avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }

            //「評点」欄 -- 名称, 合計/平均/順位
            for (Iterator itSeme = _param._semesterMap.keySet().iterator(); itSeme.hasNext();) {
                final String semester = (String) itSeme.next();
                final Semester semeMst = (Semester) _param._semesterMap.get(semester);
                final String semesterFieldName = "3".equals(semester) ? "9" : semester;
                svf.VrsOut("SEMESTER1_" + semesterFieldName, semeMst._semestername);

                //指定学期外は出力しない
                if (Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
                    continue;
                }
                //指定テストがその学期の最終テスト以外は出力しない
                final String testCd = (String) _param._lastTestItemMap.get(semester);
                if (!_param._testItemMstSdivMap.containsKey(testCd)) {
                    continue;
                }

                final String setSeme = semester;
                final int testCnt2 = 5 + Integer.parseInt(semester);
                final PrintSubclass printSubclass =  (PrintSubclass) student._printSubclassMap.get("99-" + _param._schoolKind + "-99-" + ALL9);
                if (null != printSubclass && printSubclass._testScoreMap.containsKey(setSeme + "-" + SCORE990008)) {
                    final ScoreData scoreData = (ScoreData) printSubclass._testScoreMap.get(setSeme + "-" + SCORE990008);
                    svf.VrsOutn("TOTAL_POINT1", testCnt2, scoreData._score);
                    svf.VrsOutn("AVE_POINT", testCnt2, new BigDecimal(scoreData._avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
            }

            //学習の記録
            //科目マスタでループ
            boolean hasSubclass = false;
            for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
                final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                final String subclassCd = subclassMst._subclasscd;

                //生徒が受けていない科目は読み飛ばす
                if (!student._printSubclassMap.containsKey(subclassCd)) {
                    continue;
                }
                final PrintSubclass printSubclass =  (PrintSubclass) student._printSubclassMap.get(subclassCd);

                final int subclassFieldVal = subclassMst._subclassname.length() <= 9 ? 1 : (subclassMst._subclassname.length() <= 18 ? 2 : 3); //フィールド判定用
                if (subclassFieldVal == 1) {
                    svf.VrsOut("SUBCLASS1", subclassMst._subclassname);
                } else if (subclassFieldVal == 2) {
                    final String[] token = get_token( subclassMst._subclassname, 9, 2);
                    svf.VrsOut("SUBCLASS2_1", token[0]);
                    svf.VrsOut("SUBCLASS2_2", token[1]);
                } else {
                    final String[] token = get_token( subclassMst._subclassname, 16, 2);
                    svf.VrsOut("SUBCLASS3_1", token[0]);
                    svf.VrsOut("SUBCLASS3_2", token[1]);

                }
                svf.VrsOut("CREDIT", printSubclass._credit);

                //素点
                int itemCnt = 1;
                for (Iterator itTestItem = _param._testItemMstSdivMap.keySet().iterator(); itTestItem.hasNext();) {
                    final String testCd = (String) itTestItem.next();

                    if (printSubclass._testScoreMap.containsKey(testCd)) {
                        final ScoreData scoreData = (ScoreData) printSubclass._testScoreMap.get(testCd);
                        if (scoreData._isKesseki) {
                            svf.VrsOut("POINT_ABSENCE" + itemCnt, "欠");
                        } else {
                            if (_param.isPassUnder(scoreData._score, testCd, subclassCd, student)) {
                                svf.VrsOut("POINT_STAR" + itemCnt, "*");
                            }
                            if (!subclassMst._isSaki) { //素点は先科目でないもの
                                svf.VrsOut("POINT" + itemCnt, scoreData._score);
                            }
                        }
                    }
                    itemCnt++;
                }
                //評定 -- 得点
                for (Iterator itSeme = _param._semesterMap.keySet().iterator(); itSeme.hasNext();) {
                    final String semester = (String) itSeme.next();

                    //指定学期外は出力しない
                    if (Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
                        continue;
                    }
                    //指定テストがその学期の最終テスト以外は出力しない
                    final String testCd = (String) _param._lastTestItemMap.get(semester);
                    if (!_param._testItemMstSdivMap.containsKey(testCd)) {
                        continue;
                    }

                    final String getSDiv = SCORE990008;
                    if (printSubclass._testScoreMap.containsKey(semester + "-" + getSDiv)) {
                        final ScoreData scoreData = (ScoreData) printSubclass._testScoreMap.get(semester + "-" + getSDiv);
                        if (!subclassMst._isMoto) { //評点は元科目でないもの
                            svf.VrsOut("VAL" + semester, scoreData._score);
                        }
                    }
                }
                if ("9".equals(_param._semester)) {
                    if (printSubclass._testScoreMap.containsKey("9-" + SCORE990009)) {
                        final ScoreData scoreData = (ScoreData) printSubclass._testScoreMap.get("9-" + SCORE990009);
                        if (!subclassMst._isMoto) { //評点は元科目でないもの
                            svf.VrsOut("VAL9", scoreData._score);
                        }
                    }
                }

                //欠課
                int kekkaCnt = 1;
                if (!print_J.equals(student._schoolKind) && student._attendSubClassMap.containsKey(subclassCd)) {
                    final Map atSubSemeMap = (Map) student._attendSubClassMap.get(subclassCd);
                    for (final Iterator it = _param._testItemMstSdivMap.keySet().iterator(); it.hasNext();) {
                        final String testCd = (String) it.next();
                        if (atSubSemeMap.containsKey(testCd)) {
                            final SubclassAttendance attendance = (SubclassAttendance) atSubSemeMap.get(testCd);
                            final BigDecimal lesson20Percent = attendance._lesson.divide(new BigDecimal("5"), 0, BigDecimal.ROUND_HALF_UP);

                            //20%より大きい場合
                            if (attendance._sick.compareTo(lesson20Percent) == 1) {
                                svf.VrsOut("ABSENCE_STAR" + kekkaCnt, "*");
                            }
                            svf.VrsOut("ABSENCE" + kekkaCnt, StringUtils.defaultString(String.valueOf(attendance._sick)));
                        }
                        kekkaCnt++;
                    }
                    final SubclassAttendance attendance = (SubclassAttendance) atSubSemeMap.get(SEMEALL);
                    if (null != attendance && null != attendance._sick) {
                        svf.VrsOut("ABSENCE_TOTAL", attendance._sick.toString());
                    }
                }
                svf.VrEndRecord();
            }
            if (!hasSubclass) {
                svf.VrsOut("SUBCLASS1", "DUMMY");
                svf.VrAttribute("SUBCLASS1", "X=10000");
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }


    //縦文字列分割用(半角全角どちらも文字数でカウント)
    public static String[] get_token(String strx,int f_len,int f_cnt) {

        if( strx==null || strx.length()==0 )return null;

        String stoken[] = new String[f_cnt];        //分割後の文字列の配列
        int strLen = 0;                              //文字数カウント
        int slen = 0;                               //文字列のバイト数カウント
        int s_sta = 0;                              //文字列の開始位置
        int ib = 0;
        for( int s_cur=0 ; s_cur<strx.length() && ib<f_cnt ; s_cur++ ){
            //改行マークチェック
            if( strx.charAt(s_cur)=='\r' )continue;
            if( strx.charAt(s_cur)=='\n' ){
                stoken[ib++] = strx.substring(s_sta,s_cur);
                slen = 0;
                s_sta = s_cur+1;
            } else{
            //文字数チェック
                try{
                    strLen = (strx.substring(s_cur,s_cur+1)).length();
                } catch( Exception e ){
                    System.out.println("[KNJ_EditEdit]exam_out error!"+e );
                }
                slen+=strLen;
                if( slen>f_len ){
                    stoken[ib++] = strx.substring(s_sta,s_cur);     // 04/09/22Modify
                    slen = strLen;
                    s_sta = s_cur;
                }
            }
        }
        if( slen>0 && ib<f_cnt )stoken[ib] = strx.substring(s_sta);

        return stoken;

    }//String get_token()の括り

    private List subclassListRemoveD026() {
        final List retList = new ArrayList(_param._subclassMstMap.values());
        for (final Iterator it = retList.iterator(); it.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) it.next();
            if (_param._d026List.contains(subclassMst._subclasscd)) {
                it.remove();
            }
        }
        return retList;
    }

    /**
     * 『出欠の記録』を印字する
     * @param svf
     * @param student
     */

    private void printAttend(final Vrw32alp svf, final Student student) {
        int lessonTotal = 0;
        int suspendTotal = 0;
        int mlessonTotal = 0;
        int sickTotal = 0;
        int presentTotal = 0;
        int lateTotal = 0;
        int earlyTotal = 0;
        for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final Semester semesterObj2 = (Semester) _param._semesterMap.get(semester);
            final int intSeme = Integer.parseInt(semester);
            svf.VrsOutn("SEMESTER2", intSeme, semesterObj2._semestername);
            if (!"9".equals(semester) && intSeme > Integer.parseInt(_param._semester)) {
                continue;
            }

            final Attendance attendance = (Attendance) student._attendSemesMap.get(semester);
            if (null == attendance) {
                continue;
            }

            svf.VrsOutn("REC_LESSON", intSeme, attendVal(attendance._lesson));
            svf.VrsOutn("REC_MOURNING", intSeme, attendVal(attendance._suspend + attendance._mourning));
            svf.VrsOutn("REC_PRESENT", intSeme, attendVal(attendance._mLesson));
            svf.VrsOutn("REC_ABSENCE", intSeme, attendVal(attendance._absent));
            svf.VrsOutn("REC_ATTEND", intSeme, attendVal(attendance._present));
            svf.VrsOutn("LATE", intSeme, attendVal(attendance._late));
            svf.VrsOutn("EARLY", intSeme, attendVal(attendance._early));
            lessonTotal  += attendance._lesson;
            suspendTotal += attendance._suspend + attendance._mourning;
            mlessonTotal += attendance._mLesson;
            sickTotal    += attendance._absent;
            presentTotal += attendance._present;
            lateTotal    += attendance._late;
            earlyTotal   += attendance._early;
        }
        svf.VrsOutn("SEMESTER2", 4, "年間");
        svf.VrsOutn("REC_LESSON", 4, attendVal(lessonTotal));
        svf.VrsOutn("REC_MOURNING", 4, attendVal(suspendTotal));
        svf.VrsOutn("REC_PRESENT", 4, attendVal(mlessonTotal));
        svf.VrsOutn("REC_ABSENCE", 4, attendVal(sickTotal));
        svf.VrsOutn("REC_ATTEND", 4, attendVal(presentTotal));
        svf.VrsOutn("LATE", 4, attendVal(lateTotal));
        svf.VrsOutn("EARLY", 4, attendVal(earlyTotal));
    }

    private static String attendVal(int n) {
        return String.valueOf(n);
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
            final String[] remarkArray = KNJ_EditEdit.get_token(printText, rLen * 2, rRow);
            for (int i = 0; i < remarkArray.length; i++) {
                final String setRemark = remarkArray[i];
                svf.VrsOut(fieldName + (i + 1), setRemark);
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._hrname = rs.getString("HR_NAME");
                student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                student._staffCd = StringUtils.defaultString(rs.getString("TR_CD1"));
                student._stampNo = StringUtils.defaultString(rs.getString("STAMP_NO"));
                student._attendno = String.valueOf(Integer.parseInt(rs.getString("ATTENDNO")));
                student._gName = rs.getString("GUARD_NAME");
                student._gZip = StringUtils.defaultString(rs.getString("GUARD_ZIPCD"));
                student._gAddr1 = StringUtils.defaultString(rs.getString("GUARD_ADDR1"));
                student._gAddr2 = StringUtils.defaultString(rs.getString("GUARD_ADDR2"));
                student._schoolKind = rs.getString("SCHOOL_KIND");
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._coursecd = rs.getString("COURSECD");
                student._majorcd = rs.getString("MAJORCD");
                student._courseCode = rs.getString("COURSECODE");
                student._course = rs.getString("COURSE");
                student._courseCodeName = rs.getString("COURSECODENAME");
                student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                student._entyear = rs.getString("ENT_YEAR");
                student.setRankSdiv(db2);
                student.setHreport(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT  REGD.SCHREGNO");
        stb.append("            ,REGD.SEMESTER ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,REGDH.HR_NAME ");
        stb.append("            ,STF1.STAFFNAME ");
        stb.append("            ,REGDH.TR_CD1 ");
        stb.append("            ,INK1.STAMP_NO ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,GUARD.GUARD_NAME ");
        stb.append("            ,GUARD.GUARD_ZIPCD ");
        stb.append("            ,GUARD.GUARD_ADDR1 ");
        stb.append("            ,GUARD.GUARD_ADDR2 ");
        stb.append("            ,REGDG.SCHOOL_KIND ");
        stb.append("            ,REGD.GRADE ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,REGD.COURSECD ");
        stb.append("            ,REGD.MAJORCD ");
        stb.append("            ,REGD.COURSECODE ");
        stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
        stb.append("            ,COURSECODE_M.COURSECODENAME ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
        stb.append("     FROM    SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDG.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARD ON BASE.SCHREGNO = GUARD.SCHREGNO ");
        stb.append("     LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     LEFT JOIN COURSECODE_MST COURSECODE_M ON COURSECODE_M.COURSECODE = REGD.COURSECODE ");
        stb.append("     LEFT JOIN ATTEST_INKAN_DAT INK1 ON INK1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     WHERE   REGD.YEAR = '" + _param._ctrlYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        } else {
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        }
        if ("1".equals(_param._disp)) {
            stb.append("         AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }
        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO ");
        final String sql = stb.toString();
        log.debug(" student sql = " + sql);

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
        String _hrname;
        String _staffname;
        String _staffCd;
        String _stampNo;
        String _attendno;
        String _gName;
        String _gZip;
        String _gAddr1;
        String _gAddr2;
        String _schoolKind;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _courseCode;
        String _course;
        String _courseCodeName;
        String _hrClassName1;
        String _entyear;
        String _communication;
        final Map _printSubclassMap = new TreeMap();
        final Map _attendSubClassMap = new TreeMap();
        Map _attendSemesMap = new TreeMap(); // 出欠の記録
        final Map _clsCommitteeInfo = new TreeMap();
        final Map _schoolCommitteeInfo = new TreeMap();
        final Map _clubInfo = new TreeMap();

        public Student() {
        }

        private void setRankSdiv(final DB2UDB db2) {
            final String scoreSql = getRankSdivSql();
            log.debug(scoreSql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String semester = rs.getString("SEMESTER");
                    final String testkindCd = rs.getString("TESTKINDCD");
                    final String testitemCd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String credit = rs.getString("CREDITS");
                    final String score = rs.getString("SCORE");
                    final String avg = rs.getString("AVG");
                    final String gradeRank = rs.getString("GRADE_RANK");
                    final String courseRank = rs.getString("COURSE_RANK");
                    final String count = rs.getString("COUNT");
                    final String valueDi = rs.getString("VALUE_DI");
                    PrintSubclass printSubclass = null;
                    if (!_printSubclassMap.containsKey(subclassCd)) {
                        printSubclass = new PrintSubclass(subclassCd, credit);
                        _printSubclassMap.put(subclassCd, printSubclass);
                    } else {
                        printSubclass = (PrintSubclass) _printSubclassMap.get(subclassCd);
                    }

                    final String key = semester + "-" + testkindCd + "-" + testitemCd + "-" + scoreDiv;
                    ScoreData scoreData = new ScoreData(score, avg, gradeRank, courseRank, count, valueDi);
                    if (printSubclass._testScoreMap.containsKey(key)) {
                        scoreData = (ScoreData) printSubclass._semesJviewMap.get(key);
                    }
                    printSubclass._testScoreMap.put(key, scoreData);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

        }

        private String getRankSdivSql() {
            final StringBuffer stb = new StringBuffer();
            if (!"1".equals(_param._testsubclassonly)) {
                stb.append(" SELECT DISTINCT ");
                stb.append("     SBM.CLASSCD || '-' || SBM.SCHOOL_KIND || '-' || SBM.CURRICULUM_CD || '-' || SBM.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     SCORE.SEMESTER, ");
                stb.append("     SCORE.TESTKINDCD, ");
                stb.append("     SCORE.TESTITEMCD, ");
                stb.append("     SCORE.SCORE_DIV, ");
                stb.append("     CRE.CREDITS, ");
                stb.append("     RANK_SDIV.SCORE, ");
                stb.append("     RANK_SDIV.AVG, ");
                stb.append("     RANK_SDIV.GRADE_RANK, ");
                stb.append("     RANK_SDIV.COURSE_RANK, ");
                stb.append("     AVG.COUNT, ");
                stb.append("     SCORE.VALUE_DI ");
                stb.append(" FROM ");
                stb.append("     CHAIR_STD_DAT STD ");
                stb.append("     INNER JOIN CHAIR_DAT CHR ON CHR.YEAR = STD.YEAR AND CHR.SEMESTER = STD.SEMESTER AND CHR.CHAIRCD = STD.CHAIRCD ");
                stb.append("     INNER JOIN SUBCLASS_MST SBM ON SBM.CLASSCD = CHR.CLASSCD AND SBM.SCHOOL_KIND = CHR.SCHOOL_KIND AND SBM.CURRICULUM_CD = CHR.CURRICULUM_CD AND SBM.SUBCLASSCD = CHR.SUBCLASSCD ");
                stb.append("     LEFT JOIN CREDIT_MST CRE ON STD.YEAR = CRE.YEAR ");
                stb.append("          AND CRE.COURSECD || CRE.MAJORCD || CRE.COURSECODE = '" + _course + "' ");
                stb.append("          AND CRE.GRADE = '" + _grade + "' ");
                stb.append("          AND SBM.CLASSCD = CRE.CLASSCD AND SBM.SCHOOL_KIND = CRE.SCHOOL_KIND AND SBM.CURRICULUM_CD = CRE.CURRICULUM_CD AND SBM.SUBCLASSCD = CRE.SUBCLASSCD ");
                stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE ON STD.YEAR = SCORE.YEAR ");
                stb.append("          AND SBM.CLASSCD = SCORE.CLASSCD ");
                stb.append("          AND SBM.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
                stb.append("          AND SBM.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
                stb.append("          AND SBM.SUBCLASSCD = SCORE.SUBCLASSCD ");
                stb.append("          AND STD.SCHREGNO = SCORE.SCHREGNO ");
                stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT RANK_SDIV ON RANK_SDIV.YEAR = SCORE.YEAR ");
                stb.append("          AND RANK_SDIV.SEMESTER = SCORE.SEMESTER ");
                stb.append("          AND RANK_SDIV.TESTKINDCD = SCORE.TESTKINDCD ");
                stb.append("          AND RANK_SDIV.TESTITEMCD = SCORE.TESTITEMCD ");
                stb.append("          AND RANK_SDIV.SCORE_DIV = SCORE.SCORE_DIV ");
                stb.append("          AND RANK_SDIV.CLASSCD = SCORE.CLASSCD ");
                stb.append("          AND RANK_SDIV.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
                stb.append("          AND RANK_SDIV.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
                stb.append("          AND RANK_SDIV.SUBCLASSCD = SCORE.SUBCLASSCD ");
                stb.append("          AND RANK_SDIV.SCHREGNO = SCORE.SCHREGNO ");
                stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT AVG ON RANK_SDIV.YEAR = AVG.YEAR ");
                stb.append("          AND RANK_SDIV.SEMESTER = AVG.SEMESTER ");
                stb.append("          AND RANK_SDIV.TESTKINDCD = AVG.TESTKINDCD ");
                stb.append("          AND RANK_SDIV.TESTITEMCD = AVG.TESTITEMCD ");
                stb.append("          AND RANK_SDIV.SCORE_DIV = AVG.SCORE_DIV ");
                stb.append("          AND RANK_SDIV.CLASSCD = AVG.CLASSCD ");
                stb.append("          AND RANK_SDIV.SCHOOL_KIND = AVG.SCHOOL_KIND ");
                stb.append("          AND RANK_SDIV.CURRICULUM_CD = AVG.CURRICULUM_CD ");
                stb.append("          AND RANK_SDIV.SUBCLASSCD = AVG.SUBCLASSCD ");
                stb.append("          AND AVG.AVG_DIV = '1' ");
                stb.append("          AND AVG.GRADE = '" + _grade + "' ");
                stb.append(" WHERE ");
                stb.append("     STD.YEAR = '" + _param._ctrlYear + "' ");
                stb.append("     AND STD.SEMESTER <= '" + _param._semester + "' ");
                stb.append("     AND STD.SCHREGNO = '" + _schregno + "' ");
                stb.append("     AND SBM.CLASSCD <= '" + SELECT_CLASSCD_90 + "' ");
                stb.append("     AND (RANK_SDIV.YEAR IS NULL ");
                stb.append("          OR ");
                stb.append("          RANK_SDIV.SEMESTER || '-' || RANK_SDIV.TESTKINDCD || '-' || RANK_SDIV.TESTITEMCD || '-' || RANK_SDIV.SCORE_DIV <= '" + _param._semester + "-" +  _param._testcd + "' ");
                stb.append("          OR ");
                stb.append("          RANK_SDIV.TESTKINDCD = '99' AND RANK_SDIV.TESTITEMCD = '00' AND RANK_SDIV.SCORE_DIV = '09' ");
                stb.append("         ) ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     RANK_SDIV.CLASSCD || '-' || RANK_SDIV.SCHOOL_KIND || '-' || RANK_SDIV.CURRICULUM_CD || '-' || RANK_SDIV.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     RANK_SDIV.SEMESTER, ");
                stb.append("     RANK_SDIV.TESTKINDCD, ");
                stb.append("     RANK_SDIV.TESTITEMCD, ");
                stb.append("     RANK_SDIV.SCORE_DIV, ");
                stb.append("     CAST(NULL AS INT) AS CREDITS, ");
                stb.append("     RANK_SDIV.SCORE, ");
                stb.append("     RANK_SDIV.AVG, ");
                stb.append("     RANK_SDIV.GRADE_RANK, ");
                stb.append("     RANK_SDIV.COURSE_RANK, ");
                stb.append("     AVG.COUNT, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS VALUE_DI ");
                stb.append(" FROM ");
                stb.append("     RECORD_RANK_SDIV_DAT RANK_SDIV ");
                stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT AVG ON RANK_SDIV.YEAR = AVG.YEAR ");
                stb.append("          AND RANK_SDIV.SEMESTER = AVG.SEMESTER ");
                stb.append("          AND RANK_SDIV.TESTKINDCD = AVG.TESTKINDCD ");
                stb.append("          AND RANK_SDIV.TESTITEMCD = AVG.TESTITEMCD ");
                stb.append("          AND RANK_SDIV.SCORE_DIV = AVG.SCORE_DIV ");
                stb.append("          AND RANK_SDIV.CLASSCD = AVG.CLASSCD ");
                stb.append("          AND RANK_SDIV.SCHOOL_KIND = AVG.SCHOOL_KIND ");
                stb.append("          AND RANK_SDIV.CURRICULUM_CD = AVG.CURRICULUM_CD ");
                stb.append("          AND RANK_SDIV.SUBCLASSCD = AVG.SUBCLASSCD ");
                stb.append("          AND AVG.AVG_DIV = '1' ");
                stb.append("          AND AVG.GRADE = '" + _grade + "' ");
                stb.append(" WHERE ");
                stb.append("     RANK_SDIV.YEAR = '" + _param._ctrlYear + "' ");
                stb.append("     AND (RANK_SDIV.SEMESTER || '-' || RANK_SDIV.TESTKINDCD || '-' || RANK_SDIV.TESTITEMCD || '-' || RANK_SDIV.SCORE_DIV <= '" + _param._semester + "-" +  _param._testcd + "' ");
                stb.append("          OR ");
                stb.append("          RANK_SDIV.TESTKINDCD = '99' AND RANK_SDIV.TESTITEMCD = '00' AND RANK_SDIV.SCORE_DIV = '09' ");
                stb.append("         ) ");
                stb.append("     AND RANK_SDIV.SCHREGNO = '" + _schregno + "' ");
                stb.append("     AND RANK_SDIV.SUBCLASSCD = '" + ALL9 + "' ");
                stb.append(" ORDER BY ");
                stb.append("     SUBCLASSCD, ");
                stb.append("     SEMESTER, ");
                stb.append("     TESTKINDCD, ");
                stb.append("     TESTITEMCD, ");
                stb.append("     SCORE_DIV ");
            } else {
                stb.append(" SELECT ");
                stb.append("     RANK_SDIV.CLASSCD || '-' || RANK_SDIV.SCHOOL_KIND || '-' || RANK_SDIV.CURRICULUM_CD || '-' || RANK_SDIV.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     RANK_SDIV.SEMESTER, ");
                stb.append("     RANK_SDIV.TESTKINDCD, ");
                stb.append("     RANK_SDIV.TESTITEMCD, ");
                stb.append("     RANK_SDIV.SCORE_DIV, ");
                stb.append("     CRE.CREDITS, ");
                stb.append("     RANK_SDIV.SCORE, ");
                stb.append("     RANK_SDIV.AVG, ");
                stb.append("     RANK_SDIV.GRADE_RANK, ");
                stb.append("     RANK_SDIV.COURSE_RANK, ");
                stb.append("     AVG.COUNT, ");
                stb.append("     SCORE.VALUE_DI ");
                stb.append(" FROM ");
                stb.append("     RECORD_RANK_SDIV_DAT RANK_SDIV ");
                stb.append("     LEFT JOIN CREDIT_MST CRE ON RANK_SDIV.YEAR = CRE.YEAR ");
                stb.append("          AND CRE.COURSECD || CRE.MAJORCD || CRE.COURSECODE = '" + _course + "' ");
                stb.append("          AND CRE.GRADE = '" + _grade + "' ");
                stb.append("          AND RANK_SDIV.CLASSCD = CRE.CLASSCD AND RANK_SDIV.SCHOOL_KIND = CRE.SCHOOL_KIND AND RANK_SDIV.CURRICULUM_CD = CRE.CURRICULUM_CD AND RANK_SDIV.SUBCLASSCD = CRE.SUBCLASSCD ");
                stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE ON RANK_SDIV.YEAR = SCORE.YEAR ");
                stb.append("          AND RANK_SDIV.SEMESTER = SCORE.SEMESTER ");
                stb.append("          AND RANK_SDIV.TESTKINDCD = SCORE.TESTKINDCD ");
                stb.append("          AND RANK_SDIV.TESTITEMCD = SCORE.TESTITEMCD ");
                stb.append("          AND RANK_SDIV.SCORE_DIV = SCORE.SCORE_DIV ");
                stb.append("          AND RANK_SDIV.CLASSCD = SCORE.CLASSCD ");
                stb.append("          AND RANK_SDIV.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
                stb.append("          AND RANK_SDIV.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
                stb.append("          AND RANK_SDIV.SUBCLASSCD = SCORE.SUBCLASSCD ");
                stb.append("          AND RANK_SDIV.SCHREGNO = SCORE.SCHREGNO ");
                stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT AVG ON RANK_SDIV.YEAR = AVG.YEAR ");
                stb.append("          AND RANK_SDIV.SEMESTER = AVG.SEMESTER ");
                stb.append("          AND RANK_SDIV.TESTKINDCD = AVG.TESTKINDCD ");
                stb.append("          AND RANK_SDIV.TESTITEMCD = AVG.TESTITEMCD ");
                stb.append("          AND RANK_SDIV.SCORE_DIV = AVG.SCORE_DIV ");
                stb.append("          AND RANK_SDIV.CLASSCD = AVG.CLASSCD ");
                stb.append("          AND RANK_SDIV.SCHOOL_KIND = AVG.SCHOOL_KIND ");
                stb.append("          AND RANK_SDIV.CURRICULUM_CD = AVG.CURRICULUM_CD ");
                stb.append("          AND RANK_SDIV.SUBCLASSCD = AVG.SUBCLASSCD ");
                stb.append("          AND AVG.AVG_DIV = '1' ");
                stb.append("          AND AVG.GRADE = '" + _grade + "' ");
                stb.append(" WHERE ");
                stb.append("     RANK_SDIV.YEAR = '" + _param._ctrlYear + "' ");
                stb.append("     AND (RANK_SDIV.SEMESTER || '-' || RANK_SDIV.TESTKINDCD || '-' || RANK_SDIV.TESTITEMCD || '-' || RANK_SDIV.SCORE_DIV <= '" + _param._semester + "-" +  _param._testcd + "' ");
                stb.append("          OR ");
                stb.append("          RANK_SDIV.TESTKINDCD = '99' AND RANK_SDIV.TESTITEMCD = '00' AND RANK_SDIV.SCORE_DIV = '09' ");
                stb.append("         ) ");
                stb.append("     AND RANK_SDIV.SCHREGNO = '" + _schregno + "' ");
                stb.append("     AND (RANK_SDIV.CLASSCD <= '" + SELECT_CLASSCD_90 + "' OR RANK_SDIV.SUBCLASSCD = '" + ALL9 + "') ");
                stb.append("     AND RANK_SDIV.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
                stb.append(" ORDER BY ");
                stb.append("     SUBCLASSCD, ");
                stb.append("     RANK_SDIV.SEMESTER, ");
                stb.append("     RANK_SDIV.TESTKINDCD, ");
                stb.append("     RANK_SDIV.TESTITEMCD, ");
                stb.append("     RANK_SDIV.SCORE_DIV ");

            }

            return stb.toString();
        }

        private void setHreport(final DB2UDB db2) {
            _communication = "";
            final String hreportSemeSql = getHreportSemeSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(hreportSemeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _communication = rs.getString("COMMUNICATION");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        }

        private String getHreportSemeSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COMMUNICATION ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            if (SEMEALL.equals(_param._semester)) {
                stb.append("     AND SEMESTER = '3' ");
            } else {
                stb.append("     AND SEMESTER = '" + _param._semester + "' ");
            }
            stb.append("     AND SCHREGNO = '" + _schregno + "' ");

            return stb.toString();
        }

    }

    private class PrintSubclass {
        final String _subclassCd;
        final String _credit;
        final Map _semesJviewMap;
        final Map _semesHyoukaMap;
        /**
         * _semesScoreMap[学期]scoreMap[テストコード]ScoreData
         */
        final Map _testScoreMap;
        private PrintSubclass(
                final String subclassCd,
                final String credit
        ) {
            _subclassCd = subclassCd;
            _credit = credit;
            _semesJviewMap = new TreeMap();
            _semesHyoukaMap = new TreeMap();
            _testScoreMap = new TreeMap();
        }
    }

    private class ScoreData {
        final String _score;
        final String _avg;
        final String _gradeRank;
        final String _courseRank;
        final String _count;
        final boolean _isKesseki;
        private ScoreData(
                final String score,
                final String avg,
                final String gradeRank,
                final String courseRank,
                final String count,
                final String valueDi
        ) {
            _score = StringUtils.defaultString(score);
            _avg = avg;
            _gradeRank = gradeRank;
            _courseRank = courseRank;
            _count = count;
            _isKesseki = "*".equals(valueDi);
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
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int abroad
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
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            final String movedate = param._summaryDate.replace('/', '-');
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(movedate) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(movedate) > 0 ? movedate : dateRange._edate;
            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._ctrlYear,
                        dateRange._key,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);
                psAtSeme = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

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
                                rsAtSeme.getInt("TRANSFER_DATE")
                        );
                        student._attendSemesMap.put(dateRange._key, attendance);
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
    }

    private static class CommitteeInfo {
        final String _committeeFlg;
        final String _committeeCd;
        final String _executiveCd;
        final String _committeeName;
        final String _committeeFlgName;
        final String _executiveCdName;
        CommitteeInfo(final String committeeFlg, final String committeeCd, final String executiveCd, final String committeeName, final String committeeFlgName, final String executiveCdName) {
            _committeeFlg = committeeFlg;
            _committeeCd = committeeCd;
            _executiveCd = executiveCd;
            _committeeName = committeeName;
            _committeeFlgName = committeeFlgName;
            _executiveCdName = executiveCdName;
        }
        private static void load(final DB2UDB db2, final Param param, final List students) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map subMap = null;

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT  ");
                stb.append("  T1.COMMITTEE_FLG, ");
                stb.append("  T1.COMMITTEECD, ");
                stb.append("  T1.EXECUTIVECD, ");
                stb.append("  CM.COMMITTEENAME, ");
                stb.append("  NMJ003.NAME1 AS COMMITTEE_FLG_NAME, ");
                stb.append("  NMJ002.NAME1 AS EXECUTIVECD_NAME ");
                stb.append(" FROM  ");
                stb.append("  SCHREG_COMMITTEE_HIST_DAT T1 ");
                stb.append("  INNER JOIN COMMITTEE_MST CM ON CM.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
                stb.append("     AND CM.COMMITTEECD = T1.COMMITTEECD ");
                stb.append("  LEFT JOIN NAME_MST NMJ003 ON NMJ003.NAMECD1 = 'J003' AND NMJ003.NAMECD2 = T1.COMMITTEE_FLG ");
                stb.append("  LEFT JOIN NAME_MST NMJ002 ON NMJ002.NAMECD1 = 'J002' AND NMJ002.NAMECD2 = T1.EXECUTIVECD ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + param._ctrlYear + "' ");
                stb.append("   AND T1.SEMESTER = ? ");
                stb.append("   AND T1.SCHREGNO = ? ");
                stb.append(" ORDER BY  ");
                stb.append("  T1.COMMITTEE_FLG, ");
                stb.append("  T1.SEQ ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                for (Iterator ite = students.iterator();ite.hasNext();) {
                    final Student student = (Student)ite.next();
                    for (Iterator its = param._getSemesList.iterator();its.hasNext();) {
                        final String getSemes = (String)its.next();
                        ps.setString(1, getSemes);
                        ps.setString(2, student._schregno);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final String committeeFlg = rs.getString("COMMITTEE_FLG");
                            final String committeeCd = rs.getString("COMMITTEECD");
                            final String executiveCd = rs.getString("EXECUTIVECD");
                            final String committeeName = rs.getString("COMMITTEENAME");
                            final String committeeFlgName = rs.getString("COMMITTEE_FLG_NAME");
                            final String executiveCdName = rs.getString("EXECUTIVECD_NAME");
                            final CommitteeInfo addWk = new CommitteeInfo(committeeFlg, committeeCd, executiveCd, committeeName, committeeFlgName, executiveCdName);
                            if ("2".equals(committeeFlg)) {
                                if (student._clsCommitteeInfo.containsKey(getSemes)) {
                                    subMap = (Map)student._clsCommitteeInfo.get(getSemes);
                                } else {
                                    subMap = new LinkedMap();
                                    student._clsCommitteeInfo.put(getSemes, subMap);
                                }
                            } else {
                                if (student._schoolCommitteeInfo.containsKey(getSemes)) {
                                    subMap = (Map)student._schoolCommitteeInfo.get(getSemes);
                                } else {
                                    subMap = new LinkedMap();
                                    student._schoolCommitteeInfo.put(getSemes, subMap);
                                }
                            }
                            subMap.put(committeeCd, addWk);
                        }
                    }
                }
            } catch (SQLException e) {
                log.error("sql exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        private static String concatMapData(final Map subMap) {
            String retStr = "";
            String sep = "";
            for (Iterator ite = subMap.keySet().iterator();ite.hasNext();) {
                final String kStr = (String)ite.next();
                CommitteeInfo prtWk = (CommitteeInfo)subMap.get(kStr);
                retStr += sep + prtWk._committeeName;
                sep = "、";
            }
            return retStr;
        }
    }
    private static class ClubInfo {
        final String _cd;
        final String _clubName;
        public ClubInfo(final String cd, final String clubName) {
            _cd = cd;
            _clubName = clubName;
        }
        private static void load(final DB2UDB db2, final Param param, final List students) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map subMap = null;

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT  ");
                stb.append("  T1.CLUBCD AS CD, ");
                stb.append("  CM.CLUBNAME ");
                stb.append(" FROM  ");
                stb.append("  SCHREG_CLUB_HIST_DAT T1 ");
                stb.append("  INNER JOIN CLUB_MST CM ON CM.CLUBCD = T1.CLUBCD ");
                stb.append("  INNER JOIN SEMESTER_MST T2 ON  ");
                stb.append("     ( ");
                stb.append("      T1.SDATE <= T2.SDATE AND (T1.EDATE IS NULL OR T2.SDATE <= T1.EDATE) ");
                stb.append("     OR T1.SDATE BETWEEN T2.SDATE AND T2.EDATE ");
                stb.append("     OR T1.EDATE BETWEEN T2.SDATE AND T2.EDATE ");
                stb.append("    ) ");
                stb.append(" WHERE ");
                stb.append("   T2.YEAR = '" + param._ctrlYear + "' ");
                stb.append("   AND T2.SEMESTER = ? ");
                stb.append("   AND T1.SCHREGNO = ? ");
                stb.append(" ORDER BY  ");
                stb.append("  T1.SDATE, ");
                stb.append("  T1.CLUBCD ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                for (Iterator ite = students.iterator();ite.hasNext();) {
                    final Student student = (Student)ite.next();
                    for (Iterator its = param._getSemesList.iterator();its.hasNext();) {
                        final String getSemes = (String)its.next();
                        ps.setString(1, getSemes);
                        ps.setString(2, student._schregno);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final String cd = rs.getString("CD");
                            final String clubName = rs.getString("CLUBNAME");
                            final ClubInfo addWk = new ClubInfo(cd, clubName);
                            if (student._clubInfo.containsKey(getSemes)) {
                                subMap = (Map)student._clubInfo.get(getSemes);
                            } else {
                                subMap = new LinkedMap();
                                student._clubInfo.put(getSemes, subMap);
                            }
                            subMap.put(cd, addWk);
                        }
                    }
                }
            } catch (SQLException e) {
                log.error("sql exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        private static String concatMapData(final Map subMap) {
            String retStr = "";
            String sep = "";
            for (Iterator ite = subMap.keySet().iterator();ite.hasNext();) {
                final String kStr = (String)ite.next();
                ClubInfo prtWk = (ClubInfo)subMap.get(kStr);
                retStr += sep + prtWk._clubName;
                sep = "、";
            }
            return retStr;
        }
    }
    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;

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

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final String dataKey,
                final String semester,
                final String sDate,
                final String eDate
        ) {
            log.info(" subclass attendance dateRange (" + dataKey + ") = " + sDate + " - " + eDate);
            if (null == sDate || null == eDate || sDate.compareTo(param._summaryDate) > 0) {
                return;
            }
            final String edate = eDate.compareTo(param._summaryDate) > 0 ? param._summaryDate : eDate;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._ctrlYear,
                        semester,
                        sDate,
                        edate,
                        param._attendParamMap
                );

                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

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

                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2, late, early);
                            Map setSubAttendMap = null;
                            if (student._attendSubClassMap.containsKey(subclasscd)) {
                                setSubAttendMap = (Map) student._attendSubClassMap.get(subclasscd);
                            } else {
                                setSubAttendMap = new TreeMap();
                            }
                            setSubAttendMap.put(dataKey, subclassAttendance);

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

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
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
            final SubclassMst os = (SubclassMst) o;
            int rtn;
            rtn = _classShoworder3.compareTo(os._classShoworder3);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _classcd.compareTo(os._classcd);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _subclassShoworder3.compareTo(os._subclassShoworder3);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _subclasscd.compareTo(os._subclasscd);
            return rtn;
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

    private class TestItemMstSdiv {
        final String _semester;
        final String _testKindCd;
        final String _testItemCd;
        final String _scoreDiv;
        final String _testitemName;
        final String _sDate;
        final String _eDate;
        public TestItemMstSdiv(
                final String semester,
                final String testKindCd,
                final String testItemCd,
                final String scoreDiv,
                final String testitemName,
                final String sDate,
                final String eDate
        ) {
            _semester = semester;
            _testKindCd = testKindCd;
            _testItemCd = testItemCd;
            _scoreDiv = scoreDiv;
            _testitemName = testitemName;
            _sDate = sDate;
            _eDate = eDate;
        }
    }

    private class PerfectMst {
        final String _perfect;
        final String _passScore;
        public PerfectMst(
                final String perfect,
                final String passScore
        ) {
            _perfect = perfect;
            _passScore = passScore;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75678 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String[] _categorySelected;
        final String _ctrlDate;
        final String _ctrlSemester;
        final String _ctrlYear;
        final String _disp;
        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolPrLogoImagePath;
        final String _grade;
        final String _gradeHr;
        final String _schoolKind;
        final String _prgid;
        final String _printDate;
        final String _printLogRemoteAddr;
        final String _printLogRemoteIdent;
        final String _printLogStaffcd;
        final String _schoolcd;
        final String _loginSchoolKind;
        final String _semester;
        final String _summaryDate;
        final String _testcd;
        final String _selectSchoolKind;
        final String _useSchool_KindField;
        final String _use_prg_schoolkind;
        final String _testsubclassonly;
        final String _printCommunication;
        final Map _testItemMstSdivMap;
        final Map _perfectMap;
        Map _lastTestItemMap;
        final String _lastSemester;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolRemark7;

        /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;

        final DecimalFormat _df02 = new DecimalFormat("00");

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private Map _semesterMap;
        private Map<String, String> _semester9;
        private Map _subclassMstMap;
        private List _d026List = Collections.EMPTY_LIST;
        Map _attendRanges;

        final List _getSemesList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _disp = request.getParameter("DISP");
            if ("1".equals(_disp)) {
                _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            } else {
                final String[] schregs = request.getParameterValues("CATEGORY_SELECTED");
                _categorySelected = new String[schregs.length];
                for (int i = 0; i < schregs.length; i++) {
                    final String[] schregArray = StringUtils.split(schregs[i], "-");
                    _categorySelected[i] = schregArray[0];
                }
            }
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _grade = request.getParameter("GRADE");
            _gradeHr = request.getParameter("GRADE_HR_CLASS");
            _schoolKind = getSchoolKind(db2);
            _prgid = request.getParameter("PRGID");
            _printDate = request.getParameter("PRINT_DATE");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _schoolcd = request.getParameter("SCHOOLCD");
            _loginSchoolKind = request.getParameter("SCHOOLKIND");
            _semester = request.getParameter("SEMESTER");
            _summaryDate = request.getParameter("SUMMARY_DATE");
            _testcd = request.getParameter("TESTCD");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _testsubclassonly = request.getParameter("TESTSUBCLASSONLY");
            _printCommunication = request.getParameter("COMMUNICATION");

            setCertifSchoolDat(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _semesterMap = loadSemester(db2);
            _semester9 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT SEMESTER, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + SEMEALL + "' "));
            _lastSemester = getLastSemester();
            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _lastTestItemMap = getTestItemMstSdivMax(db2);
            _testItemMstSdivMap = getTestItemMstSdiv(db2);
            _perfectMap = getPerfectMap(db2);
            loadNameMstD026(db2);
            setSubclassMst(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            if("".equals(_certifSchoolRemark7)) {
                _schoolPrLogoImagePath = getImageFilePath("J".equals(_schoolKind) ? "SCHOOLSTAMP_J.bmp" : "SCHOOLSTAMP_H.bmp");
            } else {
                final String stampNo = getStampNo(db2);
                _schoolPrLogoImagePath = getStampFilePath(stampNo + ".bmp");
            }

            final String[] semesList1 = {"1"};
            final String[] semesList2 = {"1", "2"};
            _getSemesList = Arrays.asList("1".equals(_semester) ? semesList1 : semesList2);
        }

        private String getSchoolKind(final DB2UDB db2) {
            String grade = _grade;
            if ("2".equals(_disp)) {
                final String[] gradeHr = StringUtils.split(_gradeHr, "-");
                grade = gradeHr[0];
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND GRADE = '" + grade + "' ");
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));

            final String retStr = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_KIND"));
            return retStr;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKind = "J".equals(_schoolKind) ? "103" : "104";
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5, REMARK7 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '" + certifKind + "' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _certifSchoolRemark7 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK7")); //職員コード
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            try {
                String grade = _grade;
                if ("2".equals(_disp)) {
                    final String[] gradeHr = StringUtils.split(_gradeHr, "-");
                    grade = gradeHr[0];
                }
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SEMESTER, ");
                stb.append("     SEMESTERNAME, ");
                stb.append("     SDATE, ");
                stb.append("     EDATE ");
                stb.append(" FROM ");
                stb.append("     V_SEMESTER_GRADE_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _ctrlYear + "' ");
                stb.append("     AND GRADE = '" + grade + "' ");
                stb.append("     AND SEMESTER <> '9' ");
                stb.append("     AND SEMESTER <= '" + _semester + "' ");
                stb.append(" ORDER BY ");
                stb.append("     SEMESTER ");

                ps = db2.prepareStatement(stb.toString());
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
        private String getLastSemester() {
            String retStr = "0";
            for(Iterator ite = _semesterMap.keySet().iterator();ite.hasNext();) {
                final String semester = (String)ite.next();
                if (Integer.parseInt(retStr) < Integer.parseInt(semester)) {
                    retStr = semester;
                }
            }
            return retStr;
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' GROUP BY COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
                sql += " UNION ";
                sql += " SELECT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' GROUP BY ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
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

        private Map getTestItemMstSdivMax(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map retMap = new TreeMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SDIV.SEMESTER, ");
                stb.append("     SDIV.TESTKINDCD, ");
                stb.append("     SDIV.TESTITEMCD, ");
                stb.append("     SDIV.SCORE_DIV, ");
                stb.append("     SDIV.TESTITEMNAME ");
                stb.append(" FROM ");
                stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV SDIV ");
                stb.append(" WHERE ");
                stb.append("     SDIV.YEAR = '" + _ctrlYear + "' ");
                stb.append("     AND SDIV.TESTKINDCD <> '99' ");
                stb.append(" ORDER BY ");
                stb.append("     SDIV.SEMESTER, ");
                stb.append("     SDIV.TESTKINDCD, ");
                stb.append("     SDIV.TESTITEMCD, ");
                stb.append("     SDIV.SCORE_DIV ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String testKindCd = rs.getString("TESTKINDCD");
                    final String testItemCd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String testitemName = rs.getString("TESTITEMNAME");
                    final String key = semester + "-" + testKindCd + "-" + testItemCd + "-" + scoreDiv;
                    retMap.put(semester, key);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private Map getTestItemMstSdiv(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map retMap = new TreeMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SDIV.SEMESTER, ");
                stb.append("     SDIV.TESTKINDCD, ");
                stb.append("     SDIV.TESTITEMCD, ");
                stb.append("     SDIV.SCORE_DIV, ");
                stb.append("     SDIV.TESTITEMNAME, ");
                stb.append("     SEME_DETAIL.SDATE, ");
                stb.append("     SEME_DETAIL.EDATE ");
                stb.append(" FROM ");
                stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV SDIV ");
                stb.append("     LEFT JOIN SEMESTER_DETAIL_MST SEME_DETAIL ON SDIV.YEAR = SEME_DETAIL.YEAR ");
                stb.append("          AND SDIV.SEMESTER = SEME_DETAIL.SEMESTER ");
                stb.append("          AND SDIV.SEMESTER_DETAIL = SEME_DETAIL.SEMESTER_DETAIL ");
                stb.append(" WHERE ");
                stb.append("     SDIV.YEAR = '" + _ctrlYear + "' ");
                stb.append("     AND SDIV.SEMESTER || SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV IN ('1010101', '1020201', '2010101', '2020201', '3020201') ");
                stb.append("     AND SDIV.SEMESTER || '-' || SDIV.TESTKINDCD || '-' || SDIV.TESTITEMCD || '-' || SDIV.SCORE_DIV <= '" + _semester + "-" + _testcd + "' ");
                stb.append(" ORDER BY ");
                stb.append("     SDIV.SEMESTER, ");
                stb.append("     SDIV.TESTKINDCD, ");
                stb.append("     SDIV.TESTITEMCD, ");
                stb.append("     SDIV.SCORE_DIV ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String testKindCd = rs.getString("TESTKINDCD");
                    final String testItemCd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String testitemName = rs.getString("TESTITEMNAME");
                    final String sDate = rs.getString("SDATE");
                    final String eDate = rs.getString("EDATE");
                    final String key = semester + "-" + testKindCd + "-" + testItemCd + "-" + scoreDiv;
                    final TestItemMstSdiv itemMstSdiv = new TestItemMstSdiv(semester, testKindCd, testItemCd, scoreDiv, testitemName, sDate, eDate);
                    retMap.put(key, itemMstSdiv);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private Map getPerfectMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map retMap = new TreeMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     PERFECT_RECORD_SDIV_DAT PERFECT ");
                stb.append(" WHERE ");
                stb.append("     PERFECT.YEAR = '" + _ctrlYear + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String testKindCd = rs.getString("TESTKINDCD");
                    final String testItemCd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String div = rs.getString("DIV");
                    final String grade = rs.getString("GRADE");
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final String perfect = rs.getString("PERFECT");
                    final String passScore = rs.getString("PASS_SCORE");
                    final String key = semester + "-" + testKindCd + "-" + testItemCd + "-" + scoreDiv + "-" + classCd + "-" + schoolKind + "-" + curriculumCd + "-" + subclassCd + "-" + div + "-" + grade + "-" + courseCd + "-" + majorCd + "-" + courseCode;
                    final PerfectMst perfectMst = new PerfectMst(perfect, passScore);
                    retMap.put(key, perfectMst);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        public boolean isPassUnder(final String score, final String testCd, final String subclassCd, final Student student) {
            if (StringUtils.isEmpty(score)) {
                return false;
            }
            int passScore = 30;
            if (_perfectMap.containsKey(testCd + "-" + subclassCd + "-" + "03" + "-" + student._grade + "-" + student._coursecd + "-" + student._majorcd + "-" + student._courseCode)) {
                final PerfectMst perfectMst = (PerfectMst) _perfectMap.get(testCd + "-" + subclassCd + "-" + "03" + "-" + student._grade + "-" + student._coursecd + "-" + student._majorcd + "-" + student._courseCode);
                if (!StringUtils.isEmpty(perfectMst._passScore)) {
                    passScore = Integer.parseInt(perfectMst._passScore);
                }
            } else if (_perfectMap.containsKey(testCd + "-" + subclassCd + "-" + "02" + "-" + student._grade + "-0-000-0000")) {
                final PerfectMst perfectMst = (PerfectMst) _perfectMap.get(testCd + "-" + subclassCd + "-" + "02" + "-" + student._grade + "-0-000-0000");
                if (!StringUtils.isEmpty(perfectMst._passScore)) {
                    passScore = Integer.parseInt(perfectMst._passScore);
                }
            } else if (_perfectMap.containsKey(testCd + "-" + subclassCd + "-" + "01" + "-00-0-000-0000")) {
                final PerfectMst perfectMst = (PerfectMst) _perfectMap.get(testCd + "-" + subclassCd + "-" + "01" + "-00-0-000-0000");
                if (!StringUtils.isEmpty(perfectMst._passScore)) {
                    passScore = Integer.parseInt(perfectMst._passScore);
                }
            }
            return passScore > Integer.parseInt(score);
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
            final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
            sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            _d026List = new ArrayList();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    _d026List.add(subclasscd);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.info("非表示科目:" + _d026List);
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

        public String getStampFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/stamp/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private String getStampNo(final DB2UDB db2) {
            String stampNo = "";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     STAFFCD, ");
            stb.append("     MAX(STAMP_NO) AS STAMP_NO ");
            stb.append(" FROM ");
            stb.append("     ATTEST_INKAN_DAT ");
            stb.append(" WHERE ");
            stb.append("       STAFFCD = '"+ _certifSchoolRemark7 +"' ");
            stb.append("  AND START_DATE <= '"+ _ctrlDate +"' ");
            stb.append("  AND (STOP_DATE >= '"+ _ctrlDate +"' OR STOP_DATE IS NULL) ");
            stb.append(" GROUP BY ");
            stb.append("     STAFFCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    stampNo = StringUtils.defaultString(rs.getString("STAMP_NO"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return stampNo;
        }

    }
}

// eof

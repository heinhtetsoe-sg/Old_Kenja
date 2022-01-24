/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id$
 *
 * 作成日: 2019/04/10
 * 作成者: yamashiro
 *
 * Copyright(C) 2019-2024 ALP Okinawa Co.,Ltd. All rights reserved.
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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD429D {

    private static final Log log = LogFactory.getLog(KNJD429D.class);

    private static final String SEMEALL = "9";

    private boolean _hasData;
//    private final String HOUTEI = "1";
//    private final String JITSU = "2";

    private static final String PATTERN_A = "1";
    private static final String PATTERN_B = "2";
    private static final String PATTERN_C = "3";
    private static final String PATTERN_D = "4";
    private static final String PATTERN_E = "5";

    private static final String HYOSIDISP_1 = "1";
    private static final String HYOSIDISP_2 = "2";
    private static final String HYOSIDISP_3 = "3";

//    final int FIRST_PAGE_MAXLINE = 36;
//    final int PAGE_MAXLINE = 54;
//    int LINE_CNT = 0;

    private static final String PRINT_TYPE_G = "G";
    private static final String PRINT_TYPE_H = "H";

    private Param _param;
//    private int _printSetFrmCnt = 0;

    /**
     * @param request
     *            リクエスト
     * @param response
     *            レスポンス
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

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
//            _printSetFrmCnt = 0;

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
        final List studentList = getStudentList(db2);
        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
            final DateRange range = (DateRange) rit.next();
            //出欠データ
            Attendance.load(db2, _param, studentList, range);
            //欠課
            SubclassAttendance.load(db2, _param, studentList, range);
        }

        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            if (_param._printSideFlg1) {
                //A1-1.表紙
                printHyoushi(db2, svf, student);
                //B1-2.裏表紙
                printUraByoushi(db2, svf, student);
            }
            //後は帳票パターンで分かれる
            if (PATTERN_A.equals(_param._frmPatern)) {
                //G3.観点
                printTypeG(db2, svf, student);
                if (_param._printSideFlg2) {
                    //J2-1.特別な教科の文言評価
                    printTypeJ(db2, svf, student);
                }
                if (_param._printSideFlg3) {
                    //F2-2.出欠の記録
                    printTypeF(db2, svf, student);
                }
                _hasData = true;
            } else if (PATTERN_B.equals(_param._frmPatern)) {
                //H3.観点+所見
                printTypeH(db2, svf, student);
                if (_param._printSideFlg2) {
                    //J2-1.特別な教科の文言評価
                    printTypeJ(db2, svf, student);
                }
                if (_param._printSideFlg3) {
                    //F2-2.出欠の記録
                    printTypeF(db2, svf, student);
                }
                _hasData = true;
            } else if (PATTERN_C.equals(_param._frmPatern)) {
                //E4.自立活動
                printTypeE(db2, svf, student);
                //GH3.観点+所見+自立活動
                if ("H".equals(_param._schoolKind)) {
                    printTypeG(db2, svf, student);
                } else {
                    //H3-1.観点
                    printTypeH(db2, svf, student);
                }
                if (_param._printSideFlg2) {
                    //J2-1.特別な教科の文言評価
                    printTypeJ(db2, svf, student);
                }
                if (_param._printSideFlg3) {
                    //F2-2.出欠の記録
                    printTypeF(db2, svf, student);
                }
                _hasData = true;
            } else if (PATTERN_D.equals(_param._frmPatern)) {
                //I3-3.評定
                printTypeI(db2, svf, student);
                if (_param._printSideFlg2) {
                    //J2-1.特別な教科の文言評価
                    printTypeJ(db2, svf, student);
                }
                if (_param._printSideFlg3) {
                    //F2-2.出欠の記録
                    printTypeF(db2, svf, student);
                }
                _hasData = true;
            } else if (PATTERN_E.equals(_param._frmPatern)) {
                //I3-3.評定
                printTypeI(db2, svf, student);
                if (_param._printSideFlg3) {
                    //F2-2.総合所見・出欠の記録
                    printTypeF(db2, svf, student);
                }
                _hasData = true;
            }
        }

    }

    private void printHyoushi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String setForm;
        setForm = "KNJD429D_1_A.frm";  //A4表紙のみ
        setForm(svf, setForm, 1, "hyoushi");

        CertifSchool certInfo = (CertifSchool)_param._certifSchoolMap.get(_param._schoolKind);

        //(1)校章
        svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath);
        //(2)年度
        svf.VrsOut("NENDO", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._ctrlYear)) + "年度");
        //(3)表題
        svf.VrsOut("TITLE", _param.getHReportCondMapData(_param._schoolKind, "201", 10));
        //(4)学校名
        svf.VrsOut("SCHOOL_NAME", (certInfo == null || certInfo._schoolName == null ? "" : certInfo._schoolName));

        final String gakubuStr = getGakubuStr(student);
        final String hrNameStr = getHrName(student);
        //(5)(6)
        svf.VrsOut("HR_NAME", gakubuStr + " " + hrNameStr);
        //(9)氏名
        final int nLen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nfield = nLen > 30 ? "3" : nLen > 18 ? "2" : "1";
        svf.VrsOut("NAME" + nfield, student._name);
        //(10)校長名
        if (certInfo != null && certInfo._principalName != null) {
            final int pnLen = KNJ_EditEdit.getMS932ByteLength(certInfo._principalName);
            final String pnfield = pnLen > 30 ? "3" : pnLen > 20 ? "2" : "1";
            svf.VrsOut("PRINCIPAL_NAME" + pnfield, certInfo._principalName);
        }

        int putCnt = 1; //担任名出力位置
        //(11)担任名1
        if (!"".equals(StringUtils.defaultString(student._staff1, ""))) {
            final int sn1Len = KNJ_EditEdit.getMS932ByteLength(student._staff1);
            final String sn1field = sn1Len > 30 ? "3" : sn1Len > 20 ? "2" : "1";
            svf.VrsOutn("STAFF_NAME" + sn1field, putCnt++, student._staff1);
        }
        //(12)担任名2
        if (!"".equals(StringUtils.defaultString(student._staff2, ""))) {
            final int sn2Len = KNJ_EditEdit.getMS932ByteLength(student._staff2);
            final String sn2field = sn2Len > 30 ? "3" : sn2Len > 20 ? "2" : "1";
            svf.VrsOutn("STAFF_NAME" + sn2field, putCnt++, student._staff2);
        }
        //(13)担任名3
        if (!"".equals(StringUtils.defaultString(student._staff3, ""))) {
            final int sn3Len = KNJ_EditEdit.getMS932ByteLength(student._staff3);
            final String sn3field = sn3Len > 30 ? "3" : sn3Len > 20 ? "2" : "1";
            svf.VrsOutn("STAFF_NAME" + sn3field, putCnt++, student._staff3);
        }
        //(14)担任名4
        if (!"".equals(StringUtils.defaultString(student._staff4, ""))) {
            final int sn4Len = KNJ_EditEdit.getMS932ByteLength(student._staff4);
            final String sn4field = sn4Len > 30 ? "3" : sn4Len > 20 ? "2" : "1";
            svf.VrsOutn("STAFF_NAME" + sn4field, putCnt++, student._staff4);
        }
        //(15)担任名5
        if (!"".equals(StringUtils.defaultString(student._staff5, ""))) {
            final int sn5Len = KNJ_EditEdit.getMS932ByteLength(student._staff5);
            final String sn5field = sn5Len > 30 ? "3" : sn5Len > 20 ? "2" : "1";
            svf.VrsOutn("STAFF_NAME" + sn5field, putCnt++, student._staff5);
        }
        //(16)担任名6
        if (!"".equals(StringUtils.defaultString(student._staff6, ""))) {
            final int sn6Len = KNJ_EditEdit.getMS932ByteLength(student._staff6);
            final String sn6field = sn6Len > 30 ? "3" : sn6Len > 20 ? "2" : "1";
            svf.VrsOutn("STAFF_NAME" + sn6field, putCnt++, student._staff6);
        }

        svf.VrEndPage();
    }

    private void printUraByoushi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String setForm;
        setForm = "KNJD429D_2_B.frm";  //A4表紙のみ
        setForm(svf, setForm, 1, "urahyoushi");

        final String PRINTSYURYOU = "1";
        if (PRINTSYURYOU.equals(_param._output1)) {  //指示画面の修了証出力指定
            String gakubuStr = getGakubuStr(student);
            //(5)学部(6)学年
            svf.VrsOut("TEXT", gakubuStr + " " + student._grade_Name1 + "の過程を修了したことを証する。");

            //(17)日付(元号のみ設定。他は空白)
            Semester lastSemesObj = (Semester)_param._semesterMap.get(_param._lastSemester);
            final String[] gengouStr = KNJ_EditDate.tate_format4(db2, lastSemesObj._dateRange._edate); //最終学期の最終日で元号を割り出す
            svf.VrsOut("DATE", gengouStr[0] + "    年    月    日"); //月日の数値は設定しない？

            CertifSchool certInfo = (CertifSchool)_param._certifSchoolMap.get(_param._schoolKind);
            if (certInfo != null) {
                //(4)学校名
                svf.VrsOut("SCHOOL_NAME2", (certInfo == null || certInfo._schoolName == null ? "" : certInfo._schoolName));

                //(10)職名、校長名
                svf.VrsOut("JOB_NAME", (certInfo == null || certInfo._jobName == null ? "" : certInfo._jobName));
                if (certInfo != null && certInfo._principalName != null) {
                    final int pnlen = KNJ_EditEdit.getMS932ByteLength(certInfo._principalName);
                    final String pnfield = pnlen > 20 ? "3" : pnlen > 16 ? "2" : "1";
                    svf.VrsOut("PRINCIPAL_NAME2_" + pnfield, certInfo._principalName);
                }
            }

        } else {
            svf.VrsOut("MASK", _param._whiteSpaceImagePath);
        }

        svf.VrEndPage();
    }

    private String getGakubuStr(final Student student) {
        String retGakubuStr = student._coursename;
        if (PATTERN_D.equals(_param._frmPatern)) {
            retGakubuStr = "高等部保健理療科";
        } else if (PATTERN_E.equals(_param._frmPatern)) {
            retGakubuStr = "専攻科理療科";
        }
        return StringUtils.defaultString(retGakubuStr, "");
    }
    private String getHrName(final Student student) {
        String retHrName = "";
        final String gradePrintPattern = _param.getHReportCondMapData(_param._schoolKind, "204", 1);
        if (HYOSIDISP_1.equals(gradePrintPattern)) {
            //(6)学年
            retHrName = student._grade_Name1;
        } else if (HYOSIDISP_2.equals(gradePrintPattern)) {
            //(7)年組
            retHrName = student._hr_Name;
        } else if (HYOSIDISP_3.equals(gradePrintPattern)) {
            //(8)実クラスのクラス名
            retHrName = student._ghr_Name;
        }
        return StringUtils.defaultString(retHrName, "");
    }

    private String getGakkiStr() {
        return _param._semester + "学期";
    }
    private void printHeaderTitleName(final Vrw32alp svf, final Student student) {
        svf.VrsOut("TITLE", _param._semester + "学期の記録");
        String printgrdName = getHrName(student);
        svf.VrsOut("NAME", printgrdName + " " + student._name);
    }

    private int calcMaxLine(final String titleStr, final int titlegyo, final int titlelen, final String elm1Str, final int elm1gyo, final int elm1len, final String elm2Str, final int elm2gyo, final int elm2len, final String elm3Str, final int elm3gyo, final int elm3len ) {
        int retLineCnt = 0;
        int c1val = chkStrLengthLessMaxLine(titlegyo, titlelen, titleStr);
        retLineCnt = retLineCnt < c1val ? c1val : retLineCnt;
        int c2val = chkStrLengthLessMaxLine(elm1gyo, elm1len, elm1Str);
        retLineCnt = retLineCnt < c2val ? c2val : retLineCnt;
        int c3val = chkStrLengthLessMaxLine(elm2gyo, elm2len, elm2Str);
        retLineCnt = retLineCnt < c3val ? c3val : retLineCnt;
        int c4val = chkStrLengthLessMaxLine(elm3gyo, elm3len, elm3Str);
        retLineCnt = retLineCnt < c4val ? c4val : retLineCnt;
        return retLineCnt;
    }
    private int chkStrLengthLessMaxLine(final int pageMaxLine, final int f_len, final String chkStr) {
        if ("".equals(StringUtils.defaultString(chkStr, "")) || f_len == 0 || pageMaxLine == 0) return 0;
        final String[] cutStr = KNJ_EditKinsoku.getTokenList(chkStr, f_len, pageMaxLine).toArray(new String[0]);
        return realUseCalcCnt(cutStr, false);
    }
    private int realUseCalcCnt(final String[] useStr, final boolean ignoreKaraMoji) {
        int retCnt = 0;
        if (useStr != null) {
            for (int cCnt = 0;cCnt < useStr.length;cCnt++) {
                if (useStr[cCnt] != null && (ignoreKaraMoji || (!ignoreKaraMoji && !"".equals(useStr[cCnt])))) {
                    retCnt++;
                }
            }
        }
        return retCnt;
    }
    private int calcStartCenterLine(final int useLine, final int putLineCnt) {
        return (int)Math.ceil(useLine/2.0) - (int)Math.floor( (putLineCnt-(useLine % 2 == 0 ? 1 : 0)) / 2.0);
    }
    private String getKantenStr(final JviewRecord prtWk) {
        String retStr = "";
        if (_param._jviewGradeMap.containsKey(prtWk._followGrade)) {
            final Map subViewMap = (Map)_param._jviewGradeMap.get(prtWk._followGrade);
            if (subViewMap.containsKey(prtWk._subclassCd)) {
                final List vlist = (List)subViewMap.get(prtWk._subclassCd);
                for (Iterator itp = vlist.iterator();itp.hasNext();) {
                    JviewGrade vWk = (JviewGrade)itp.next();
                    if (vWk._viewCd.equals(prtWk._viewCd)) {
                        retStr = vWk._viewName;
                    }
                }
            }
        }
        return retStr;
    }

    private void printTypeJ(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        HreportRemarkDat prtWk = null;
        if (student._hreportRemarkDatMap.containsKey(_param._semester)) {
            prtWk = (HreportRemarkDat)student._hreportRemarkDatMap.get(_param._semester);
        }
        //※53～56は共通処理
        if ("P".equals(_param._schoolKind)) {
            if (student._grade_Cd.compareTo("03") < 0) { //1,2年
                setForm(svf, "KNJD429D_4_J_1.frm", 1, "printTypeJ_1");
                //50
                if (!"".equals(StringUtils.defaultString(student._hrepRemarkDetail0101R1, ""))) {
                    final String[] moralStr = KNJ_EditKinsoku.getTokenList(student._hrepRemarkDetail0101R1, 70, 3).toArray(new String[0]);
                    if (moralStr != null) {
                        for (int rCnt = 0;rCnt < moralStr.length;rCnt++) {
                            svf.VrsOutn("MORAL", rCnt + 1, moralStr[rCnt]);
                        }
                    }
                }
            } else if (student._grade_Cd.compareTo("05") < 0) { //3,4年
                setForm(svf, "KNJD429D_4_J_2.frm", 1, "printTypeJ_2");
                //50
                if (!"".equals(StringUtils.defaultString(student._hrepRemarkDetail0101R1, ""))) {
                    final String[] moralStr = KNJ_EditKinsoku.getTokenList(student._hrepRemarkDetail0101R1, 70, 3).toArray(new String[0]);
                    if (moralStr != null) {
                        for (int rCnt = 0;rCnt < moralStr.length;rCnt++) {
                            svf.VrsOutn("MORAL", rCnt + 1, moralStr[rCnt]);
                        }
                    }
                }
                //51
                if (prtWk != null && !"".equals(StringUtils.defaultString(prtWk._foreignlangact, ""))) {
                    final String[] foreignStr = KNJ_EditKinsoku.getTokenList(prtWk._foreignlangact, 70, 3).toArray(new String[0]);
                    if (foreignStr != null) {
                        for (int fCnt = 0;fCnt < foreignStr.length;fCnt++) {
                            svf.VrsOutn("FOREIGN", fCnt + 1, foreignStr[fCnt]);
                        }
                    }
                }
                //52
                if (prtWk != null && !"".equals(StringUtils.defaultString(prtWk._totalstudytime, ""))) {
                    final String[] tsStr = KNJ_EditKinsoku.getTokenList(prtWk._totalstudytime, 70, 3).toArray(new String[0]);
                    if (tsStr != null) {
                        for (int tsCnt = 0;tsCnt < tsStr.length;tsCnt++) {
                            svf.VrsOutn("TOTAL_STUDY", tsCnt + 1, tsStr[tsCnt]);
                        }
                    }
                }
            } else { //5,6年
                setForm(svf, "KNJD429D_4_J_3.frm", 1, "printTypeJ_3");
                //50
                if (!"".equals(StringUtils.defaultString(student._hrepRemarkDetail0101R1, ""))) {
                    final String[] moralStr = KNJ_EditKinsoku.getTokenList(student._hrepRemarkDetail0101R1, 70, 3).toArray(new String[0]);
                    if (moralStr != null) {
                        for (int rCnt = 0;rCnt < moralStr.length;rCnt++) {
                            svf.VrsOutn("MORAL", rCnt + 1, moralStr[rCnt]);
                        }
                    }
                }
                //52
                if (prtWk != null && !"".equals(StringUtils.defaultString(prtWk._totalstudytime, ""))) {
                    final String[] tsStr = KNJ_EditKinsoku.getTokenList(prtWk._totalstudytime, 70, 3).toArray(new String[0]);
                    if (tsStr != null) {
                        for (int tsCnt = 0;tsCnt < tsStr.length;tsCnt++) {
                            svf.VrsOutn("TOTAL_STUDY", tsCnt + 1, tsStr[tsCnt]);
                        }
                    }
                }
            }

            //行動の記録
            svf.VrsOut("SEMESTER1_1", getGakkiStr());
            final String DISPACTREC = "1";
            final String dispActRecFlg = _param.getHReportCondMapData(_param._schoolKind, "208", 1);
            if (!DISPACTREC.equals(dispActRecFlg)) {
                int prtVCnt = 1;
                for (Iterator ite = student._behaviorSemeMap.keySet().iterator();ite.hasNext();) {
                    final String code = (String)ite.next();
                    BehaviorSeme BSprtWk = (BehaviorSeme)student._behaviorSemeMap.get(code);
                    svf.VrsOutn("VIEW_NAME1", prtVCnt, BSprtWk._viewname);
                    svf.VrsOutn("VIEW1", prtVCnt, BSprtWk._record);
                    prtVCnt++;
                }
            } else {
                svf.VrsOut("BLANK2", _param._whiteSpaceImagePath);
            }
        } else if ("J".equals(_param._schoolKind)) {  //中学
            setForm(svf, "KNJD429D_4_J_4.frm", 1, "printTypeJ_4");
            //50
            if (!"".equals(StringUtils.defaultString(student._hrepRemarkDetail0101R1, ""))) {
                final String[] moralStr = KNJ_EditKinsoku.getTokenList(student._hrepRemarkDetail0101R1, 70, 6).toArray(new String[0]);  //中学は出力行数が違う
                if (moralStr != null) {
                    for (int rCnt = 0;rCnt < moralStr.length;rCnt++) {
                        svf.VrsOutn("MORAL", rCnt + 1, moralStr[rCnt]);
                    }
                }
            }
            //52
            if (prtWk != null && !"".equals(StringUtils.defaultString(prtWk._totalstudytime, ""))) {
                final String[] tsStr = KNJ_EditKinsoku.getTokenList(prtWk._totalstudytime, 70, 6).toArray(new String[0]);  //中学/高校は出力行数が違う
                if (tsStr != null) {
                    for (int tsCnt = 0;tsCnt < tsStr.length;tsCnt++) {
                        svf.VrsOutn("TOTAL_STUDY", tsCnt + 1, tsStr[tsCnt]);
                    }
                }
            }
        } else {  //高校
            setForm(svf, "KNJD429D_4_J_5.frm", 1, "printTypeJ_5");
            //52
            if (prtWk != null && !"".equals(StringUtils.defaultString(prtWk._totalstudytime, ""))) {
                final String[] tsStr = KNJ_EditKinsoku.getTokenList(prtWk._totalstudytime, 70, 6).toArray(new String[0]);  //中学/高校は出力行数が違う
                if (tsStr != null) {
                    for (int tsCnt = 0;tsCnt < tsStr.length;tsCnt++) {
                        svf.VrsOutn("TOTAL_STUDY", tsCnt + 1, tsStr[tsCnt]);
                    }
                }
            }
        }
        //以下、共通処理

        //タイトル
        printHeaderTitleName(svf, student);

        //53
        if (prtWk != null && !"".equals(StringUtils.defaultString(prtWk._specialactremark, ""))) {
            final String[] spActStr = KNJ_EditKinsoku.getTokenList(prtWk._specialactremark, 70, 6).toArray(new String[0]);
            if (spActStr != null) {
                for (int mCnt = 0;mCnt < spActStr.length;mCnt++) {
                    svf.VrsOutn("SP_ACT", mCnt + 1, spActStr[mCnt]);
                }
            }
        }
        //54
        if (!"".equals(StringUtils.defaultString(student._hrepRemarkDetail0102R1, ""))) {
            final String[] jirituStr = KNJ_EditKinsoku.getTokenList(student._hrepRemarkDetail0102R1, 70, 6).toArray(new String[0]);
            if (jirituStr != null) {
                for (int jCnt = 0;jCnt < jirituStr.length;jCnt++) {
                    svf.VrsOutn("ACTIVE1", jCnt + 1, jirituStr[jCnt]);
                }
            }
        }

        final String DISPTOTALREMARK = "1";
        final String dispTotalRemark = _param.getHReportCondMapData(_param._schoolKind, "210", 1);
        if (!DISPTOTALREMARK.equals(dispTotalRemark)) {
            //55,56
            final String TotalRemarkTitle = _param.getHReportCondMapData(_param._schoolKind, "210", 10);
            final int trtlen = KNJ_EditEdit.getMS932ByteLength(TotalRemarkTitle);
            final String trtfield = trtlen > 20 ? "2_1" : trtlen > 18 ? "1_2" : "1_1";
            svf.VrsOut("HREPORT_TITLE" + trtfield, TotalRemarkTitle);
            final String[] trStr = KNJ_EditKinsoku.getTokenList(student._hrepRemarkDetail0103R1, 70, 6).toArray(new String[0]);
            if (trStr != null) {
                for (int trCnt = 0;trCnt < trStr.length;trCnt++) {
                    svf.VrsOutn("ACTIVE2", trCnt + 1, trStr[trCnt]);
                }
            }
        } else {
            svf.VrsOut("BLANK1", _param._whiteSpaceImagePath);
        }
        svf.VrEndPage();
    }

    private void printTypeE(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        setForm(svf, "KNJD429D_3_E.frm", 4, "printTypeE");
        printHeaderTitleName(svf, student);

        final int pageMaxLine = 60;
        int useLine = 0;
        int remainLine = pageMaxLine;
        //重点項目・目標設定理由
        ////データ取得
        ////最大行数計算
        useLine = 0;
        //タイトル行を加味した行数チェックにしているので、行数算出で判断せず、データが入っているかどうかを先にチェックする。
        if (student._selfReliance._key_Goals != null && student._selfReliance._goals_Reason != null) {
            useLine = calcMaxLine((String)_param._jirituTitleMap.get("003"), 2, 8, student._selfReliance._key_Goals, 6, 40, (String)_param._jirituTitleMap.get("004") + "\n" + student._selfReliance._goals_Reason, 7, 40, "", 0, 0);
        }
        ////改ページ判定
        if (remainLine != pageMaxLine && remainLine < useLine) {
            svf.VrEndPage();
            setForm(svf, "KNJD429D_3_E.frm", 4, "printTypeE");
            printHeaderTitleName(svf, student);
            remainLine = pageMaxLine;
        }
        ////出力
        final String[]colA1Str = KNJ_EditKinsoku.getTokenList((String)_param._jirituTitleMap.get("003"), 8, 2).toArray(new String[0]);
        final String[]colA2Str = KNJ_EditKinsoku.getTokenList(student._selfReliance._key_Goals, 40, 6).toArray(new String[0]);
        final String[]colA3Str = KNJ_EditKinsoku.getTokenList(StringUtils.defaultString((String)_param._jirituTitleMap.get("004"), "") + "\n" + StringUtils.defaultString(student._selfReliance._goals_Reason, ""), 40, 7).toArray(new String[0]);
        int lineCnt = 0;
        int strtTitleidx = calcStartCenterLine(useLine, realUseCalcCnt(colA1Str, true));   //縦の出力位置を割り出す
        boolean outFlg = false;
        for (lineCnt = 0;lineCnt < useLine;lineCnt++) {
            outFlg = true;
            final int putIdx = lineCnt - (strtTitleidx - 1);
            if (putIdx >= 0 && colA1Str != null && putIdx < colA1Str.length) {
                outFlg = true;
                svf.VrsOut("CLASS_NAME1", colA1Str[putIdx]);
            }
            if (colA2Str != null && lineCnt < colA2Str.length) {
                outFlg = true;
                svf.VrsOut("CONTENT1_1", colA2Str[lineCnt]);
            }
            if (colA3Str != null && lineCnt < colA3Str.length && colA3Str[lineCnt] != null) {
                outFlg = true;
                svf.VrsOut("CONTENT1_2", colA3Str[lineCnt]);
            }
            if (outFlg) {
                svf.VrsOut("GRP1_1", "A1");
                svf.VrsOut("GRP1_2", lineCnt == 0 ? "A1" : "A2");
                svf.VrEndRecord();
                remainLine--;
            }
        }

        final int defOneDataGyo = 24;
        final int defTitleGyo = 1;
        final int defYokoTitleLen = 20;
        final int defTateTitleLen = 20;
        //最初のタイトル部分は、タイトル部分+次行で改ページが必要か、判定してから実施する。
        //   NG:タイトル出して足りなくなって改ページして、改ページしたからタイトル出してから出力。※の部分が出力。
        //とりあえず、先にタイトルの行数を取る。
        int useTitleLine = calcMaxLine("", 0, 0, (String)_param._jirituTitleMap.get("005"), defTitleGyo, defYokoTitleLen, (String)_param._jirituTitleMap.get("006"), defTitleGyo, defYokoTitleLen, (String)_param._jirituTitleMap.get("008"), defTitleGyo, defYokoTitleLen);

        int DGrp_1_gyo = 3;
        int DGrp_2 = 16;
        int DGrp_3 = 32;
        int DGrp_4 = 32;
        //わかる:GRP=2
        useLine = calcMaxLine((String)_param._jirituTitleMap.get("009"), DGrp_1_gyo, defYokoTitleLen, student._selfReliance._long_Goals1, defOneDataGyo, DGrp_2, student._selfReliance._short_Goals1, defOneDataGyo, DGrp_3, student._selfReliance._evaluation1, defOneDataGyo, DGrp_4);
        if (remainLine != pageMaxLine && remainLine < (useLine + useTitleLine)) {
            svf.VrEndPage();
            setForm(svf, "KNJD429D_3_E.frm", 4, "printTypeE");
            printHeaderTitleName(svf, student);
            remainLine = pageMaxLine;
        }
        //最初のタイトル部分は、次行の改ページが必要か、判定してから実施する。※
        remainLine = printTypeETblTitle(svf, defTitleGyo, remainLine, useTitleLine, defYokoTitleLen);
        ////出力
        final String[]colCTStr = KNJ_EditKinsoku.getTokenList((String)_param._jirituTitleMap.get("009"), defTateTitleLen, DGrp_1_gyo).toArray(new String[0]);
        final String[]colC1Str = KNJ_EditKinsoku.getTokenList(student._selfReliance._long_Goals1, DGrp_2, defOneDataGyo).toArray(new String[0]);
        final String[]colC2Str = KNJ_EditKinsoku.getTokenList(student._selfReliance._short_Goals1, DGrp_3, defOneDataGyo).toArray(new String[0]);
        final String[]colC3Str = KNJ_EditKinsoku.getTokenList(student._selfReliance._evaluation1, DGrp_4, defOneDataGyo).toArray(new String[0]);
        strtTitleidx = calcStartCenterLine(useLine, realUseCalcCnt(colCTStr, true));   //縦の出力位置を割り出す
        for (lineCnt = 0;lineCnt < useLine;lineCnt++) {
            outFlg = true;
            final int putIdx = lineCnt - (strtTitleidx - 1);
            if (colCTStr != null && putIdx >= 0 && putIdx < colCTStr.length) {
                outFlg = true;
                svf.VrsOut("CLASS_NAME3", colCTStr[putIdx]);
            }
            if (colC1Str != null && lineCnt < colC1Str.length) {
                outFlg = true;
                svf.VrsOut("CONTENT3_1", colC1Str[lineCnt]);
            }
            if (colC2Str != null && lineCnt < colC2Str.length) {
                outFlg = true;
                svf.VrsOut("CONTENT3_2", colC2Str[lineCnt]);
            }
            if (colC3Str != null && lineCnt < colC3Str.length) {
                outFlg = true;
                svf.VrsOut("CONTENT3_3", colC3Str[lineCnt]);
            }
            if (outFlg) {
                svf.VrsOut("GRP3_0", "C1");
                svf.VrsOut("GRP3_1", "C1");
                svf.VrsOut("GRP3_2", "C1");
                svf.VrsOut("GRP3_3", "C1");
                svf.VrEndRecord();
                remainLine--;
            }
        }

        //かかわり:GRP=3
        useLine = 0;
        useLine = calcMaxLine((String)_param._jirituTitleMap.get("010"), DGrp_1_gyo, defYokoTitleLen, student._selfReliance._long_Goals2, defOneDataGyo, DGrp_2, student._selfReliance._short_Goals2, defOneDataGyo, DGrp_3, student._selfReliance._evaluation2, defOneDataGyo, DGrp_4);
        if (remainLine != pageMaxLine && remainLine < useLine) {
            svf.VrEndPage();
            setForm(svf, "KNJD429D_3_E.frm", 4, "printTypeE");
            printHeaderTitleName(svf, student);
            remainLine = pageMaxLine;
            remainLine = printTypeETblTitle(svf, defTitleGyo, remainLine, useTitleLine, defYokoTitleLen);
        }
        ////出力
        final String[]colDTStr = KNJ_EditKinsoku.getTokenList((String)_param._jirituTitleMap.get("010"), defTateTitleLen, DGrp_1_gyo).toArray(new String[0]);
        final String[]colD1Str = KNJ_EditKinsoku.getTokenList(student._selfReliance._long_Goals2, DGrp_2, defOneDataGyo).toArray(new String[0]);
        final String[]colD2Str = KNJ_EditKinsoku.getTokenList(student._selfReliance._short_Goals2, DGrp_3, defOneDataGyo).toArray(new String[0]);
        final String[]colD3Str = KNJ_EditKinsoku.getTokenList(student._selfReliance._evaluation2, DGrp_4, defOneDataGyo).toArray(new String[0]);
        strtTitleidx = calcStartCenterLine(useLine, realUseCalcCnt(colDTStr, true));   //縦の出力位置を割り出す
        for (lineCnt = 0;lineCnt < useLine;lineCnt++) {
            outFlg = false;
            final int putIdx = lineCnt - (strtTitleidx - 1);
            if (colDTStr != null && putIdx >= 0 && putIdx < colDTStr.length) {
                outFlg = true;
                svf.VrsOut("CLASS_NAME3", colDTStr[putIdx]);
            }
            if (colD1Str != null && lineCnt < colD1Str.length) {
                outFlg = true;
                svf.VrsOut("CONTENT3_1", colD1Str[lineCnt]);
            }
            if (colD2Str != null && lineCnt < colD2Str.length) {
                outFlg = true;
                svf.VrsOut("CONTENT3_2", colD2Str[lineCnt]);
            }
            if (colD3Str != null && lineCnt < colD3Str.length) {
                outFlg = true;
                svf.VrsOut("CONTENT3_3", colD3Str[lineCnt]);
            }
            if (outFlg) {
                svf.VrsOut("GRP3_0", "D1");
                svf.VrsOut("GRP3_1", "D1");
                svf.VrsOut("GRP3_2", "D1");
                svf.VrsOut("GRP3_3", "D1");
                svf.VrEndRecord();
                remainLine--;
            }
        }

        //からだ:GRP=4
        useLine = 0;
        useLine = calcMaxLine((String)_param._jirituTitleMap.get("011"), DGrp_1_gyo, defYokoTitleLen, student._selfReliance._long_Goals3, defOneDataGyo, DGrp_2, student._selfReliance._short_Goals3, defOneDataGyo, DGrp_3, student._selfReliance._evaluation3, defOneDataGyo, DGrp_4);
        if (remainLine != pageMaxLine && remainLine < useLine) {
            svf.VrEndPage();
            setForm(svf, "KNJD429D_3_E.frm", 4, "printTypeE");
            printHeaderTitleName(svf, student);
            remainLine = pageMaxLine;
            remainLine = printTypeETblTitle(svf, defTitleGyo, remainLine, useTitleLine, defYokoTitleLen);
        }
        ////出力
        final String[]colETStr = KNJ_EditKinsoku.getTokenList((String)_param._jirituTitleMap.get("011"), defTateTitleLen, DGrp_1_gyo).toArray(new String[0]);
        final String[]colE1Str = KNJ_EditKinsoku.getTokenList(student._selfReliance._long_Goals3, DGrp_2, defOneDataGyo).toArray(new String[0]);
        final String[]colE2Str = KNJ_EditKinsoku.getTokenList(student._selfReliance._short_Goals3, DGrp_3, defOneDataGyo).toArray(new String[0]);
        final String[]colE3Str = KNJ_EditKinsoku.getTokenList(student._selfReliance._evaluation3, DGrp_4, defOneDataGyo).toArray(new String[0]);
        strtTitleidx = calcStartCenterLine(useLine, realUseCalcCnt(colETStr, true));   //縦の出力位置を割り出す
        for (lineCnt = 0;lineCnt < useLine;lineCnt++) {
            outFlg = false;
            final int putIdx = lineCnt - (strtTitleidx - 1);
            if (colETStr != null && putIdx >= 0 && putIdx < colETStr.length) {
                outFlg = true;
                svf.VrsOut("CLASS_NAME3", colETStr[putIdx]);
            }
            if (colE1Str != null && lineCnt < colE1Str.length) {
                outFlg = true;
                svf.VrsOut("CONTENT3_1", colE1Str[lineCnt]);
            }
            if (colE2Str != null && lineCnt < colE2Str.length) {
                outFlg = true;
                svf.VrsOut("CONTENT3_2", colE2Str[lineCnt]);
            }
            if (colE3Str != null && lineCnt < colE3Str.length) {
                outFlg = true;
                svf.VrsOut("CONTENT3_3", colE3Str[lineCnt]);
            }
            if (outFlg) {
                svf.VrsOut("GRP3_0", "E1");
                svf.VrsOut("GRP3_1", "E1");
                svf.VrsOut("GRP3_2", "E1");
                svf.VrsOut("GRP3_3", "E1");
                svf.VrEndRecord();
                remainLine--;
            }
        }

        //けんこう:GRP=5
        useLine = 0;
        useLine = calcMaxLine((String)_param._jirituTitleMap.get("012"), DGrp_1_gyo, defYokoTitleLen, student._selfReliance._long_Goals4, defOneDataGyo, DGrp_2, student._selfReliance._short_Goals4, defOneDataGyo, DGrp_3, student._selfReliance._evaluation4, defOneDataGyo, DGrp_4);
        if (remainLine != pageMaxLine && remainLine < useLine) {
            svf.VrEndPage();
            setForm(svf, "KNJD429D_3_E.frm", 4, "printTypeE");
            printHeaderTitleName(svf, student);
            remainLine = pageMaxLine;
            remainLine = printTypeETblTitle(svf, defTitleGyo, remainLine, useTitleLine, defYokoTitleLen);
        }
        ////出力
        final String[]colFTStr = KNJ_EditKinsoku.getTokenList((String)_param._jirituTitleMap.get("012"), defTateTitleLen, DGrp_1_gyo).toArray(new String[0]);
        final String[]colF1Str = KNJ_EditKinsoku.getTokenList(student._selfReliance._long_Goals4, DGrp_2, defOneDataGyo).toArray(new String[0]);
        final String[]colF2Str = KNJ_EditKinsoku.getTokenList(student._selfReliance._short_Goals4, DGrp_3, defOneDataGyo).toArray(new String[0]);
        final String[]colF3Str = KNJ_EditKinsoku.getTokenList(student._selfReliance._evaluation4, DGrp_4, defOneDataGyo).toArray(new String[0]);
        strtTitleidx = calcStartCenterLine(useLine, realUseCalcCnt(colFTStr, true));   //縦の出力位置を割り出す
        for (lineCnt = 0;lineCnt < useLine;lineCnt++) {
            outFlg = false;
            final int putIdx = lineCnt - (strtTitleidx - 1);
            if (colFTStr != null && putIdx >= 0 && putIdx < colFTStr.length) {
                outFlg = true;
                svf.VrsOut("CLASS_NAME3", colFTStr[putIdx]);
            }
            if (colF1Str != null && lineCnt < colF1Str.length) {
                outFlg = true;
                svf.VrsOut("CONTENT3_1", colF1Str[lineCnt]);
            }
            if (colF2Str != null && lineCnt < colF2Str.length) {
                outFlg = true;
                svf.VrsOut("CONTENT3_2", colF2Str[lineCnt]);
            }
            if (colF3Str != null && lineCnt < colF3Str.length) {
                outFlg = true;
                svf.VrsOut("CONTENT3_3", colF3Str[lineCnt]);
            }
            if (outFlg) {
                svf.VrsOut("GRP3_0", "F1");
                svf.VrsOut("GRP3_1", "F1");
                svf.VrsOut("GRP3_2", "F1");
                svf.VrsOut("GRP3_3", "F1");
                svf.VrEndRecord();
                remainLine--;
            }
        }

        svf.VrEndPage();
    }
    private int printTypeETblTitle(final Vrw32alp svf, final int defTitleGyo, int remainLine, final int useTitleLine, final int defYokoTitleLen) {
        //タイトル:GRP=B1
        final int useLine = useTitleLine;
        if (remainLine != defTitleGyo && remainLine < useLine) {
            svf.VrEndPage();
        }
        ////出力
        final String[]colB1Str = KNJ_EditKinsoku.getTokenList((String)_param._jirituTitleMap.get("005"), defYokoTitleLen, defTitleGyo).toArray(new String[0]);
        final String[]colB2Str = KNJ_EditKinsoku.getTokenList((String)_param._jirituTitleMap.get("006"), defYokoTitleLen, defTitleGyo).toArray(new String[0]);
        final String[]colB3Str = KNJ_EditKinsoku.getTokenList((String)_param._jirituTitleMap.get("008"), defYokoTitleLen, defTitleGyo).toArray(new String[0]);
        boolean outFlg;
        for (int lineCnt = 0;lineCnt < useLine;lineCnt++) {
            outFlg = false;
            if (colB1Str != null && lineCnt < colB1Str.length) {
                svf.VrsOut("TITLE2_1", colB1Str[lineCnt]);
                outFlg = true;
            }
            if (colB2Str != null && lineCnt < colB2Str.length) {
                svf.VrsOut("TITLE2_2", colB2Str[lineCnt]);
                outFlg = true;
            }
            if (colB3Str != null && lineCnt < colB3Str.length) {
                svf.VrsOut("TITLE2_3", colB3Str[lineCnt]);
                outFlg = true;
            }
            if (outFlg) {
                svf.VrsOut("GRP2_0", "B1");
                svf.VrsOut("GRP2_1", "B1");
                svf.VrsOut("GRP2_2", "B1");
                svf.VrsOut("GRP2_3", "B1");
                svf.VrEndRecord();
                remainLine--;
            }
        }
        return remainLine;
    }

    private void printTypeF(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        if (PATTERN_E.equals(_param._frmPatern)) {
            //F2-A.出欠の記録
            setForm(svf, "KNJD429D_4_F2.frm", 1, "printTypeF_2");
            //総合所見
            final String DISPTOTALREMARK = "1";
            final String dispTotalRemark = _param.getHReportCondMapData(_param._schoolKind, "210", 1);
            if (!DISPTOTALREMARK.equals(dispTotalRemark)) {
                final String TotalRemarkTitle = _param.getHReportCondMapData(_param._schoolKind, "210", 10);
                final int trtlen = KNJ_EditEdit.getMS932ByteLength(TotalRemarkTitle);
                final String trtfield = trtlen > 20 ? "2_1" : trtlen > 18 ? "1_2" : "1_1";
                svf.VrsOut("HREPORT_TITLE" + trtfield, TotalRemarkTitle);
                final String[] trStr = KNJ_EditKinsoku.getTokenList(student._hrepRemarkDetail0103R1, 70, 6).toArray(new String[0]);
                if (trStr != null) {
                    for (int trCnt = 0;trCnt < trStr.length;trCnt++) {
                        svf.VrsOutn("ACTIVE2", trCnt + 1, trStr[trCnt]);
                    }
                }
            } else {
                svf.VrsOut("BLANK1", _param._whiteSpaceImagePath);
            }
        } else {
            //F2-JHP.出欠の記録
            setForm(svf, "KNJD429D_5_F.frm", 1, "printTypeF_1");
        }
        printHeaderTitleName(svf, student);

        final String DISPCOMM = "1";
        final String dispCommunication = _param.getHReportCondMapData(_param._schoolKind, "211", 1);
        //出欠の記録
        for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final Semester semesterObj2 = (Semester) _param._semesterMap.get(semester);
            svf.VrsOut("SEMESTER2_" + semester, semesterObj2._semesterName);
            if (!"9".equals(semester) && Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
                continue;
            }

            int line = 1;
            final Attendance att = (Attendance) student._attendMap.get(semester);
            if (null != att) {
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._lesson));  // 授業日数
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._suspend + att._mourning)); // 忌引出停日数
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._mLesson)); // 出席しなければならない日数
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._absent));  // 欠席日数
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._present)); // 出席日数

                final String[] trStr = KNJ_EditKinsoku.getTokenList(att._remark, 20, 2).toArray(new String[0]);
                if (trStr != null) {
                    for (int trCnt = 0;trCnt < trStr.length;trCnt++) {
                        svf.VrsOutn("ATTEND_REMARK" + semester, trCnt + 1, trStr[trCnt]);
                    }
                }
                //
                if (!PATTERN_E.equals(_param._frmPatern) && !DISPCOMM.equals(dispCommunication)) {
                    if (semester.equals(_param._semester)) {
                        if (null != att._communication) {
                            final String[] communicationArray = KNJ_EditKinsoku.getTokenList(att._communication, 90, 8).toArray(new String[0]);
                            if (null != communicationArray) {
                                for (int i = 0; i < communicationArray.length; i++) {
                                    final String setText = communicationArray[i];
                                    svf.VrsOutn("FROM_SCHOOL", i + 1, setText); // 学校より
                                }
                            }
                        }
                    }
                } else {
                    svf.VrsOut("BLANK1", _param._whiteSpaceImagePath);
                }
            }
        }
        svf.VrEndPage();
    }

    private void printTypeG(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        int lineMaxCnt = 30;
        final int fixedPrintLine = 5;
        if ("P".equals(_param._schoolKind)) {
            //G3-1P.観点(小学校)
            setForm(svf, "KNJD429D_3_G_1.frm", 4, "printTypeG_1");
        } else {
            //G3-1JH.観点(中学高校)
            setForm(svf, "KNJD429D_3_G_2.frm", 4, "printTypeG_2");
        }
        printTypeGH(db2, svf, PRINT_TYPE_G, student, lineMaxCnt, fixedPrintLine);
    }

    private void printTypeH(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        int lineMaxCnt = 30;
        final int fixedPrintLine = 5;
        if ("P".equals(_param._schoolKind)) {
            //H3-2P.観点+所見
            setForm(svf, "KNJD429D_3_H_1.frm", 4, "printTypeH_1");
        } else {
            //H3-2JH.観点+所見
            setForm(svf, "KNJD429D_3_H_2.frm", 4, "printTypeH_2");
        }
        printTypeGH(db2, svf, PRINT_TYPE_H, student, lineMaxCnt, fixedPrintLine);
    }

    private void printTypeGH(final DB2UDB db2, final Vrw32alp svf, final String Type, final Student student, final int lineMaxCnt, final int fixedPrintLine) {
        printHeaderTitleName(svf, student);

        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        final Map useScoreSubclsMap = (Map) student._recordScoreSemeSdivMap.get(_param._semester + "08");

        int grpCnt = 0;
        int remainCnt = lineMaxCnt;
        for (Iterator ite = student._jviewRecordSemeMap.keySet().iterator();ite.hasNext();) {
            final String semester = (String)ite.next();
            final Map subMap = (Map)student._jviewRecordSemeMap.get(semester);
            for (Iterator itr = subMap.keySet().iterator();itr.hasNext();) {
                final String subclsCd = (String)itr.next();
                final String[] subclsArr = StringUtils.split(subclsCd, ":");
                final String convSubclsCd = student._jviewSubclsConvMap.containsKey(subclsArr[1]) ? (String)student._jviewSubclsConvMap.get(subclsArr[1]) : subclsArr[1];
                ScoreData scWk = getScoreData(convSubclsCd, useScoreSubclsMap, _param._semester);
                final RecordSyoken remarkWk = (RecordSyoken)student.getRecordSyoken(semester, convSubclsCd);
                final String[] remarkStr = remarkWk == null ? null : KNJ_EditKinsoku.getTokenList(remarkWk._remark1, 30, 8).toArray(new String[0]);
                ++grpCnt;
                final String grpStr = (grpCnt < 10 ? "0" : "") + String.valueOf(grpCnt);
                final Map detailMap = (Map)subMap.get(subclsCd);
                //改ページ判定
                if (remainCnt - fixedPrintLine < 0) {
                    svf.VrEndPage();
                    remainCnt = lineMaxCnt;
                }
                int prtLine = 0;
                String classname = "";
                String[] subclassNameStr = null;
                boolean printDataFirst = false;
                for (Iterator ity = detailMap.keySet().iterator();ity.hasNext();) {
                    final String kStr = (String)ity.next();
                    final JviewRecord prtWk = (JviewRecord)detailMap.get(kStr);
                    if (!prtWk._semester.equals(_param._semester)) {
                        continue;
                    }
                    printDataFirst = true;
                    //観点を取得する
                    String putViewStr = getKantenStr(prtWk);

                    svf.VrsOut("GRP1_1", grpStr);
                    svf.VrsOut("GRP1_2", grpStr);
                    svf.VrsOut("GRP1_3", grpStr);
                    if(!classname.equals(prtWk._nowSubclassName)) {
                        classname = prtWk._nowSubclassName;
                        subclassNameStr = KNJ_EditEdit.get_token(prtWk._nowSubclassName, 4, 8);
                    }
                    svf.VrsOut("CLASS_NAME1", subclassNameStr[prtLine]);
                    if (putViewStr != null) {
                        svf.VrsOut("CONTENT1", putViewStr);  //form側で、1_2にlink
                    }
                    svf.VrsOut("VIEW", prtWk._statusName);
                    if (!"P".equals(_param._schoolKind)) {
                        if (null != scWk) {
                            svf.VrsOut("VALUE", StringUtils.defaultString(scWk._score));
                        }
                    }
                    if (PRINT_TYPE_H.equals(Type)) {
                        svf.VrsOut("GRP1_1", grpStr);
                        svf.VrsOut("GRP1_2", grpStr);
                        svf.VrsOut("GRP1_3", grpStr);
                        if (null != remarkStr) {
                            if (prtLine * 2 < remarkStr.length) {
                                svf.VrsOut("CONTENT2", remarkStr[prtLine * 2]);
                            }
                            if (prtLine * 2 + 1 < remarkStr.length) {
                                svf.VrsOut("CONTENT2_2", remarkStr[prtLine * 2 + 1]);
                            }
                        }
                    }

                    svf.VrEndRecord();
                    remainCnt--;
                    prtLine++;
                }
                if (printDataFirst && prtLine < fixedPrintLine) {
                    for (int nCnt = prtLine;nCnt < fixedPrintLine;nCnt++) {
                        svf.VrsOut("GRP1_1", grpStr);
                        svf.VrsOut("GRP1_2", grpStr);
                        svf.VrsOut("GRP1_3", grpStr);

                        svf.VrsOut("CLASS_NAME1", subclassNameStr[prtLine]);
                        if (null != remarkStr) {
                            if (prtLine * 2 < remarkStr.length) {
                                svf.VrsOut("CONTENT2", remarkStr[prtLine * 2]);
                            }
                            if (prtLine * 2 + 1 < remarkStr.length) {
                                svf.VrsOut("CONTENT2_2", remarkStr[prtLine * 2 + 1]);
                            }
                        }
                        prtLine++;
                        svf.VrEndRecord();
                    }
                }
            }
        }
        svf.VrEndPage();

    }

    private void printTypeI(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        String formName = "";
        int lineMaxCnt = 0;
        int lineCnt = 0;
        if ("A".equals(_param._schoolKind)) {
            //I3-3A.評定
            formName = "KNJD429D_3_I_2.frm";
            setForm(svf, formName, 4, "printTypeI_2");
            lineMaxCnt = 26;
        } else {
            //I3-3JHP.評定
            formName = "KNJD429D_3_I_1.frm";
            setForm(svf, formName, 4, "printTypeI_1");
            lineMaxCnt = 23;
        }
        printHeaderTitleName(svf, student);

        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        final Map scoreSubclassMap108 = (Map) student._recordScoreSemeSdivMap.get("108");
        final Map scoreSubclassMap208 = (Map) student._recordScoreSemeSdivMap.get("208");
        final Map scoreSubclassMap308 = (Map) student._recordScoreSemeSdivMap.get("308");
        final Map scoreSubclassMap909 = (Map) student._recordScoreSemeSdivMap.get("909");

        int seme1TotalScore = 0;
        int seme1Cnt = 0;
        int seme2TotalScore = 0;
        int seme2Cnt = 0;
        int seme3TotalScore = 0;
        int seme3Cnt = 0;
        int seme9TotalScore = 0;
        int seme9Cnt = 0;

        //平均
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclassCd;

            //総合的な学習
            printTokubetsuKamoku(svf, student, student._sougakuSubClassCd, subclassCd, 1);
            //特別活動
            printTokubetsuKamoku(svf, student, student._tokkatsuSubClassCd, subclassCd, 2);
            //自立活動
            printTokubetsuKamoku(svf, student, student._jikatsuSubClassCd, subclassCd, 3);

            if ((null == scoreSubclassMap108 || !scoreSubclassMap108.containsKey(subclassCd)) &&
                (null == scoreSubclassMap208 || !scoreSubclassMap208.containsKey(subclassCd)) &&
                (null == scoreSubclassMap308 || !scoreSubclassMap308.containsKey(subclassCd)) &&
                (null == scoreSubclassMap909 || !scoreSubclassMap909.containsKey(subclassCd))
            ) {
                continue;
            }

            //評点
            final int[] score1 = getScore(svf, subclassCd, scoreSubclassMap108);
            seme1TotalScore += score1[0];
            seme1Cnt += score1[1];
            final int[] score2 = getScore(svf, subclassCd, scoreSubclassMap208);
            seme2TotalScore += score2[0];
            seme2Cnt += score2[1];
            final int[] score3 = getScore(svf, subclassCd, scoreSubclassMap308);
            seme3TotalScore += score3[0];
            seme3Cnt += score3[1];
            final int[] score9 = getScore(svf, subclassCd, scoreSubclassMap909);
            seme9TotalScore += score9[0];
            seme9Cnt += score3[1];
        }

        if (seme1Cnt != 0) {
            final BigDecimal setAvg1 = new BigDecimal(seme1TotalScore).divide(new BigDecimal(seme1Cnt), 0,  BigDecimal.ROUND_HALF_UP);
            svf.VrsOut("AVERAGE1", setAvg1.toString());
        }
        if (2 <= Integer.parseInt(_param._semester)) {
            if (seme2Cnt != 0) {
                final BigDecimal setAvg9 = new BigDecimal(seme2TotalScore).divide(new BigDecimal(seme2Cnt), 0,  BigDecimal.ROUND_HALF_UP);
                svf.VrsOut("AVERAGE2", setAvg9.toString());
            }
        }
        if (3 <= Integer.parseInt(_param._semester)) {
            if (seme3Cnt != 0) {
                final BigDecimal setAvg3 = new BigDecimal(seme3TotalScore).divide(new BigDecimal(seme3Cnt), 0,  BigDecimal.ROUND_HALF_UP);
                svf.VrsOut("AVERAGE3", setAvg3.toString());
            }
        }
        if (3 <= Integer.parseInt(_param._semester)) {
            if (seme9Cnt != 0) {
                final BigDecimal setAvgVal = new BigDecimal(seme9TotalScore).divide(new BigDecimal(seme9Cnt), 0,  BigDecimal.ROUND_HALF_UP);
                svf.VrsOut("AVERAGE9", setAvgVal.toString());
            }
        }

        //合計修得単位
        svf.VrsOut("GET_CREDIT", String.valueOf(student._totalGetCredit));

        final int maxLine = lineMaxCnt;
        //科目
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclassCd;
            if ((null == scoreSubclassMap108 || !scoreSubclassMap108.containsKey(subclassCd)) &&
                (null == scoreSubclassMap208 || !scoreSubclassMap208.containsKey(subclassCd)) &&
                (null == scoreSubclassMap308 || !scoreSubclassMap308.containsKey(subclassCd)) &&
                (null == scoreSubclassMap909 || !scoreSubclassMap909.containsKey(subclassCd))
            ) {
                continue;
            }

            if (lineCnt + 1 > maxLine) {
                svf.VrEndRecord();
                setForm(svf, formName, 4, "printTypeI_ADD");
                printHeaderTitleName(svf, student);
                lineCnt = 0;
            }
            svf.VrsOut("CLASS_NAME1", subclassMst._className);
            svf.VrsOut("SUBCLASS_NAME", subclassMst._subclassName);

            //単位
            if (3 <= Integer.parseInt(_param._semester)) {
                if(scoreSubclassMap909.containsKey(subclassCd)) {
                    final ScoreData scoreData = (ScoreData) scoreSubclassMap909.get(subclassCd);
                    final String setCredit = StringUtils.defaultString(scoreData._getCredit);
                    svf.VrsOut("CREDIT1", StringUtils.defaultString(setCredit));
                }
            }

            //評点
            printScoreValue(svf, subclassCd, scoreSubclassMap108, "VALUE1", "1");
            printScoreValue(svf, subclassCd, scoreSubclassMap208, "VALUE2", "2");
            printScoreValue(svf, subclassCd, scoreSubclassMap308, "VALUE3", "3");
            printScoreValue(svf, subclassCd, scoreSubclassMap909, "VALUE9", "3");

            //欠課
            printAttend(svf, student, subclassCd, "9", "ABSENCE1");

            svf.VrEndRecord();
            lineCnt++;
        }

        for (int i = lineCnt; i < maxLine; i++) {
            svf.VrAttribute("CLASS_NAME1", "Meido=100");
            svf.VrsOut("CLASS_NAME1", "1");
            svf.VrEndRecord();
        }
        svf.VrEndPage();
    }


    private void setForm(final Vrw32alp svf, final String setForm, final int no, final String comment) {
        svf.VrSetForm(setForm, no);
        log.info(" setform " + comment + " : " + setForm);
    }

    private void printAttend(final Vrw32alp svf, final Student student, final String subclassCd, final String semester, final String fieldName) {
        //欠課
        if (student._attendSubClassMap.containsKey(subclassCd)) {
            final Map atSubSemeMap = (Map) student._attendSubClassMap.get(subclassCd);
            if (atSubSemeMap.containsKey(semester)) {
                final SubclassAttendance attendance = (SubclassAttendance) atSubSemeMap.get(semester);
                svf.VrsOut(fieldName, attendance._sick.toString());
            }
        }
    }

    private void printTokubetsuKamoku(final Vrw32alp svf, final Student student, final String tokubetsuKamoku, final String subclassCd, final int line) {
        if (subclassCd.equals(tokubetsuKamoku)) {
            printCredit(svf, student, subclassCd, "CREDIT2", line);
            printAttendPaternLTokubetsu(svf, student, subclassCd, "9", "ABSENCE2", line);
        }
    }

    private void printCredit(final Vrw32alp svf, final Student student, final String subclassCd, final String fieldName, final int line) {
        final String setCredit = (String) _param._creditMap.get(student._grade + student._coursecd + student._majorcd + student._coursecode + subclassCd);
        svf.VrsOutn(fieldName, line, setCredit);
    }

    private void printAttendPaternLTokubetsu(final Vrw32alp svf, final Student student, final String subclassCd, final String semester, final String fieldName, final int line) {
        //欠課
        if (student._attendSubClassMap.containsKey(subclassCd)) {
            final Map atSubSemeMap = (Map) student._attendSubClassMap.get(subclassCd);
            if (atSubSemeMap.containsKey(semester)) {
                final SubclassAttendance attendance = (SubclassAttendance) atSubSemeMap.get(semester);
                svf.VrsOutn(fieldName, line, attendance._sick.toString());
            }
        }
    }

    private int[] getScore(final Vrw32alp svf, final String subclassCd, final Map scoreSubclassMap) {
        String score = "";
        int[] retInt = {0, 0};
        if (null == scoreSubclassMap) {
            score = "";
        } else {
            final ScoreData scoreData = (ScoreData) scoreSubclassMap.get(subclassCd);
            if (null == scoreData) {
                score = "";
            } else {
                score = StringUtils.defaultString(scoreData._score);
            }
        }
        if (!StringUtils.isEmpty(score)) {
            retInt[0] = Integer.parseInt(score);
            retInt[1] = 1;
        }
        return retInt;
    }

    private void printScoreValue(final Vrw32alp svf, final String subclassCd, final Map scoreSubclassMap, final String fieldName, final String semester) {
        if (Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
            return;
        }
        if (null == scoreSubclassMap) {
            return;
        }
        final ScoreData scoreData = (ScoreData) scoreSubclassMap.get(subclassCd);
        if (null != scoreData) {
            svf.VrsOut(fieldName, StringUtils.defaultString(scoreData._score));
        }
    }

    private ScoreData getScoreData(final String subclassCd, final Map scoreSubclassMap, final String semester) {
        if (Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
            return null;
        }
        if (null == scoreSubclassMap) {
            return null;
        }
        return  (ScoreData) scoreSubclassMap.get(subclassCd);
    }


    private List getStudentList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String studentSql = getStudentSql();
            log.debug(" sql =" + studentSql);
            ps = db2.prepareStatement(studentSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String coursename = rs.getString("COURSENAME");
                final String coursecd = rs.getString("COURSECD");
                final String majorcd = rs.getString("MAJORCD");
                final String coursecode = rs.getString("COURSECODE");
                final String grade = rs.getString("GRADE");
                final String grade_Cd = rs.getString("GRADE_CD");
                final String grade_Name1 = rs.getString("GRADE_NAME1");
                final String hr_Class = rs.getString("HR_CLASS");
                final String hr_Name = rs.getString("HR_NAME");
                final String hr_Nameabbv = rs.getString("HR_NAMEABBV");
                final String ghr_Cd = rs.getString("GHR_CD");
                final String ghr_Attendno = rs.getString("GHR_ATTENDNO");
                final String ghr_Name = rs.getString("GHR_NAME");
                final String ghr_Nameabbv = rs.getString("GHR_NAMEABBV");
                final String year = rs.getString("YEAR");
                final String semester = rs.getString("SEMESTER");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String staff1 = rs.getString("STAFF1");
                final String staff2 = rs.getString("STAFF2");
                final String staff3 = rs.getString("STAFF3");
                final String staff4 = rs.getString("STAFF4");
                final String staff5 = rs.getString("STAFF5");
                final String staff6 = rs.getString("STAFF6");

                final Student student = new Student(coursename, coursecd, majorcd, coursecode, grade, grade_Cd, grade_Name1, hr_Class, hr_Name, hr_Nameabbv, ghr_Cd, ghr_Attendno, ghr_Name, ghr_Nameabbv, year, semester, schregno, name, staff1, staff2, staff3, staff4, staff5, staff6);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        for (Iterator it = retList.iterator(); it.hasNext();) {
            Student student = (Student) it.next();
            student.setHreportRemarkDat(db2);
            student.setHreportRemarkDetail(db2);
            student.setBehaviorSeme(db2);
            student.setRecordScoreSemeSdivMap(db2);
            student.setRecordSyokenSemeMap(db2);
            student.setJview(db2);
            student.setTokusyuKamoku(db2);
            student.setJirituInfo(db2, student);
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH HREP_STAFF_INFOS AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.*, ");
        stb.append("   SM.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("   HREPORT_STAFF_DAT T1 ");
        stb.append("   LEFT JOIN STAFF_MST SM ");
        stb.append("     ON SM.STAFFCD = T1.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("   T1.SEMESTER = '9' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("  T8.COURSENAME, ");
        stb.append("  T1.COURSECD, ");
        stb.append("  T1.MAJORCD, ");
        stb.append("  T1.COURSECODE, ");
        stb.append("  T1.GRADE, ");
        stb.append("  T4.GRADE_CD, ");
        stb.append("  T4.GRADE_NAME1, ");
        stb.append("  T1.HR_CLASS, ");
        stb.append("  T3_1.HR_NAME, ");
        stb.append("  T3_1.HR_NAMEABBV, ");
        stb.append("  T2.GHR_CD, ");
        stb.append("  T2.GHR_ATTENDNO, ");
        stb.append("  T3_2.GHR_NAME, ");
        stb.append("  T3_2.GHR_NAMEABBV, ");
        stb.append("  T1.YEAR, ");
        stb.append("  T1.SEMESTER, ");
        stb.append("  T1.SCHREGNO, ");
        stb.append("  T5.NAME, ");
        stb.append("  T7_1.STAFFNAME AS STAFF1, ");
        stb.append("  T7_2.STAFFNAME AS STAFF2, ");
        stb.append("  T7_3.STAFFNAME AS STAFF3, ");
        stb.append("  T7_4.STAFFNAME AS STAFF4, ");
        stb.append("  T7_5.STAFFNAME AS STAFF5, ");
        stb.append("  T7_6.STAFFNAME AS STAFF6 ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_GHR_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT T3_1 ");
        stb.append("     ON T3_1.YEAR = T1.YEAR ");
        stb.append("    AND T3_1.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T3_1.GRADE = T1.GRADE ");
        stb.append("    AND T3_1.HR_CLASS = T1.HR_CLASS ");
        stb.append("   LEFT JOIN SCHREG_REGD_GHR_HDAT T3_2 ");
        stb.append("     ON T3_2.YEAR = T2.YEAR ");
        stb.append("    AND T3_2.SEMESTER = T2.SEMESTER ");
        stb.append("    AND T3_2.GHR_CD = T2.GHR_CD ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T4 ");
        stb.append("     ON T4.YEAR = T1.YEAR ");
        stb.append("    AND T4.GRADE = T1.GRADE ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T5 ");
        stb.append("     ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN HREP_STAFF_INFOS T7_1 ");
        stb.append("     ON T7_1.YEAR = T1.YEAR ");
        //stb.append("    AND T7_1.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T7_1.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T7_1.SEQ = '1' ");
        stb.append("   LEFT JOIN HREP_STAFF_INFOS T7_2 ");
        stb.append("     ON T7_2.YEAR = T1.YEAR ");
        //stb.append("    AND T7_2.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T7_2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T7_2.SEQ = '2' ");
        stb.append("   LEFT JOIN HREP_STAFF_INFOS T7_3 ");
        stb.append("     ON T7_3.YEAR = T1.YEAR ");
        //stb.append("    AND T7_3.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T7_3.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T7_3.SEQ = '3' ");
        stb.append("   LEFT JOIN HREP_STAFF_INFOS T7_4 ");
        stb.append("     ON T7_4.YEAR = T1.YEAR ");
        //stb.append("    AND T7_4.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T7_4.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T7_4.SEQ = '4' ");
        stb.append("   LEFT JOIN HREP_STAFF_INFOS T7_5 ");
        stb.append("     ON T7_5.YEAR = T1.YEAR ");
        //stb.append("    AND T7_5.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T7_5.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T7_5.SEQ = '5' ");
        stb.append("   LEFT JOIN HREP_STAFF_INFOS T7_6 ");
        stb.append("     ON T7_6.YEAR = T1.YEAR ");
        //stb.append("    AND T7_6.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T7_6.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T7_6.SEQ = '6' ");
        stb.append("   LEFT JOIN COURSE_MST T8 ");
        stb.append("     ON T8.COURSECD = T1.COURSECD ");
        stb.append(" WHERE ");
        stb.append("  T1.YEAR = '" + _param._ctrlYear + "' AND T1.SEMESTER = '" + _param._semester + "' AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append(" ORDER BY  ");
        stb.append("  SCHREGNO ");

        return stb.toString();
    }

    private class Student {
        final String _coursename;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _grade;
        final String _grade_Cd;
        final String _grade_Name1;
        final String _hr_Class;
        final String _hr_Name;
        final String _hr_Nameabbv;
        final String _ghr_Cd;
        final String _ghr_Attendno;
        final String _ghr_Name;
        final String _ghr_Nameabbv;
        final String _year;
        final String _semester;
        final String _schregno;
        final String _name;
        final String _staff1;
        final String _staff2;
        final String _staff3;
        final String _staff4;
        final String _staff5;
        final String _staff6;

        final Map _blockMap;
        final Map _attendMap = new TreeMap();
        final List _medexamMonthList = new ArrayList();
        Medexamdet _medexamdet;
        String _hrepRemarkDetail0101R1;
        String _hrepRemarkDetail0102R1;
        String _hrepRemarkDetail0103R1;
        String _hrepRemarkDetail0201R1;
        String _hrepRemarkDetail0201R2;
        final Map _behaviorSemeMap;
        final Map _recordSyokenSemeMap = new TreeMap();
        final Map _recordScoreSemeSdivMap = new TreeMap();
        final Map _jviewRecordSemeMap = new TreeMap();
        final Map _jviewSubclsConvMap = new TreeMap();
        final Map _attendSubClassMap = new TreeMap();
        final Map _hreportRemarkDatMap = new TreeMap();
        int _totalGetCredit = 0;
        String _sougakuSubClassCd;
        String _tokkatsuSubClassCd;
        String _jikatsuSubClassCd;
        SelfReliance _selfReliance = new SelfReliance();

        public Student (final String coursename, final String coursecd, final String majorcd, final String coursecode, final String grade, final String grade_Cd, final String grade_Name1, final String hr_Class, final String hr_Name, final String hr_Nameabbv, final String ghr_Cd, final String ghr_Attendno, final String ghr_Name, final String ghr_Nameabbv, final String year, final String semester, final String schregno, final String name, final String staff1, final String staff2, final String staff3, final String staff4, final String staff5, final String staff6)
        {
            _coursename = coursename;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;

            _grade = grade;
            _grade_Cd = grade_Cd;
            _grade_Name1 = grade_Name1;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _hr_Nameabbv = hr_Nameabbv;
            _ghr_Cd = ghr_Cd;
            _ghr_Attendno = ghr_Attendno;
            _ghr_Name = ghr_Name;
            _ghr_Nameabbv = ghr_Nameabbv;
            _year = year;
            _semester = semester;
            _schregno = schregno;
            _name = name;
            _staff1 = staff1;
            _staff2 = staff2;
            _staff3 = staff3;
            _staff4 = staff4;
            _staff5 = staff5;
            _staff6 = staff6;

            _blockMap = new TreeMap();
            _behaviorSemeMap = new TreeMap();
        }


        private void setHreportRemarkDat(final DB2UDB db2) {
            final String hreportRemarkDetailSql = getHreportRemarkSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(hreportRemarkDetailSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String totalstudytime = rs.getString("TOTALSTUDYTIME");
                    final String specialactremark = rs.getString("SPECIALACTREMARK");
                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final String remark3 = rs.getString("REMARK3");
                    final String foreignlangact = rs.getString("FOREIGNLANGACT");
                    final HreportRemarkDat hreportRemarkDat = new HreportRemarkDat(totalstudytime, specialactremark, remark1, remark2, remark3, foreignlangact);
                    _hreportRemarkDatMap.put(semester, hreportRemarkDat);
                }
            } catch (SQLException ex) {
                log.error("setHreportRemarkDat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getHreportRemarkSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER, ");
            stb.append("     TOTALSTUDYTIME, ");
            stb.append("     SPECIALACTREMARK, ");
//            stb.append("     COMMUNICATION, ");
            stb.append("     REMARK1, ");
            stb.append("     REMARK2, ");
            stb.append("     REMARK3, ");
            stb.append("     FOREIGNLANGACT ");
//            stb.append("     ATTENDREC_REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO = '" + _schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SEMESTER ");

            return stb.toString();
        }

        private void setHreportRemarkDetail(final DB2UDB db2) {
            _hrepRemarkDetail0101R1 = "";
            _hrepRemarkDetail0102R1 = "";
            _hrepRemarkDetail0103R1 = "";
            _hrepRemarkDetail0201R1 = "";
            _hrepRemarkDetail0201R2 = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            final String hreportRemarkDetail0201Sql = getHreportRemarkDetailSql("02", "01");
            try {
                ps = db2.prepareStatement(hreportRemarkDetail0201Sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _hrepRemarkDetail0201R1 = rs.getString("REMARK1");
                    _hrepRemarkDetail0201R2 = rs.getString("REMARK2");
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            ps = null;
            rs = null;
            final String hreportRemarkDetail0101Sql = getHreportRemarkDetailSql("01", "01");
            try {
                ps = db2.prepareStatement(hreportRemarkDetail0101Sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _hrepRemarkDetail0101R1 = rs.getString("REMARK1");
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            ps = null;
            rs = null;
            final String hreportRemarkDetail0102Sql = getHreportRemarkDetailSql("01", "02");
            try {
                ps = db2.prepareStatement(hreportRemarkDetail0102Sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _hrepRemarkDetail0102R1 = rs.getString("REMARK1");
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            ps = null;
            rs = null;
            final String hreportRemarkDetail0103Sql = getHreportRemarkDetailSql("01", "03");
            try {
                ps = db2.prepareStatement(hreportRemarkDetail0103Sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _hrepRemarkDetail0103R1 = rs.getString("REMARK1");
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getHreportRemarkDetailSql(final String div, final String code) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REMARK1, ");
            stb.append("     REMARK2 ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DETAIL_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND DIV = '" + div + "' ");
            stb.append("     AND CODE = '" + code + "' ");

            return stb.toString();
        }

        private void setRecordSyokenSemeMap(final DB2UDB db2) {
            if (!PATTERN_B.equals(_param._frmPatern) && !PATTERN_C.equals(_param._frmPatern)) {  //観点+所見、観点+所見+自立活動  以外は処理しない。
                return;
            }
            final String recordSyokenSemeSql = getRecordSyokenSemeSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(recordSyokenSemeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String remark1 = rs.getString("REMARK1");
                    final String setSubclassCd = classCd + "-" + schoolKind + "-" + curriculumCd + "-" + subclassCd;
                    final RecordSyoken recordSyoken = new RecordSyoken(classCd, setSubclassCd, remark1);
                    final Map setSubMap;
                    if (_recordSyokenSemeMap.containsKey(semester)) {
                        setSubMap = (Map) _recordSyokenSemeMap.get(semester);
                    } else {
                        setSubMap = new HashMap();
                        _recordSyokenSemeMap.put(semester, setSubMap);
                    }
                    setSubMap.put(setSubclassCd, recordSyoken);
                }
            } catch (SQLException ex) {
                log.error("setRecordSyokenSemeMap exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        private RecordSyoken getRecordSyoken(final String semes, final String subcls) {
            RecordSyoken retObj = null;
            if (_recordSyokenSemeMap.containsKey(semes)) {
                final Map subMap = (Map)_recordSyokenSemeMap.get(semes);
                if (subMap.containsKey(subcls)) {
                    retObj = (RecordSyoken)subMap.get(subcls);
                }
            }
            return retObj;
        }

        private String getRecordSyokenSemeSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER, ");
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     REMARK1 ");
            stb.append(" FROM ");
            stb.append("     JVIEWSTAT_REPORTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO = '" + _schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SEMESTER, ");
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     SUBCLASSCD ");
            return stb.toString();
        }

        private void setRecordScoreSemeSdivMap(final DB2UDB db2) {
            final String recordScoreSemeSql = getRecordScoreSemeSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(recordScoreSemeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String sDiv = rs.getString("SCORE_DIV");
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String getCredit = StringUtils.defaultString(rs.getString("CREDITS"));
                    final String score = rs.getString("SCORE");
                    final String remark = rs.getString("REMARK1");
                    final String setSubclassCd = classCd + "-" + schoolKind + "-" + curriculumCd + "-" + subclassCd;
                    final ScoreData scoreData = new ScoreData(classCd, setSubclassCd, getCredit, score, remark);
                    if ("9".equals(semester) && !StringUtils.isEmpty(getCredit)) {
                        _totalGetCredit += Integer.parseInt(getCredit);
                    }

                    final Map setSubMap;
                    if (_recordScoreSemeSdivMap.containsKey(semester + sDiv)) {
                        setSubMap = (Map) _recordScoreSemeSdivMap.get(semester + sDiv);
                    } else {
                        setSubMap = new HashMap();
                    }
                    setSubMap.put(setSubclassCd, scoreData);
                    _recordScoreSemeSdivMap.put(semester + sDiv, setSubMap);
                }
            } catch (SQLException ex) {
                log.error("setRecordScoreSemeSdivMap exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getRecordScoreSemeSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.SCORE_DIV, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T4.GET_CREDIT AS CREDITS, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T3.REMARK1 ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2");
            stb.append("       ON T2.YEAR = T1.YEAR ");
            stb.append("      AND T2.SEMESTER = '" + _param._semester + "' ");
            stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN JVIEWSTAT_REPORTREMARK_DAT T3 ");
            stb.append("       ON T3.YEAR = T1.YEAR ");
            stb.append("      AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("      AND T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("      AND T3.CLASSCD = T1.CLASSCD ");
            stb.append("      AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("      AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("      AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN RECORD_SCORE_DAT T4 ");
            stb.append("      ON T4.YEAR          = T1.YEAR ");
            stb.append("     AND T4.SEMESTER      = T1.SEMESTER ");
            stb.append("     AND T4.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("     AND T4.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("     AND T4.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("     AND T4.CLASSCD       = T1.CLASSCD ");
            stb.append("     AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("     AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     AND T4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("     AND T4.SCHREGNO      = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.TESTKINDCD = '99' ");
            stb.append("     AND T1.TESTITEMCD = '00' ");
            stb.append("     AND T1.SCORE_DIV IN ('08', '09') ");
            stb.append("     AND T1.SCHREGNO = '" + _schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.SCORE_DIV, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD ");

            return stb.toString();
        }

        private void setJview(final DB2UDB db2) {
            final String scoreSql = getJviewScoreSql();
            //log.fatal(scoreSql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String nowClassName = rs.getString("NOW_CLASSNAME");
                    final String nowSubclassName = rs.getString("NOW_SUBCLASSNAME");
                    final String baseSubclassCd = rs.getString("BASE_SUBCLASSCD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String semester = rs.getString("SEMESTER");
                    final String followGrade = rs.getString("FOLLOW_GRADE");
                    final String viewCd = rs.getString("VIEWCD");
                    final String status = rs.getString("STATUS");
                    final String statusName = rs.getString("MARK");
                    final JviewRecord jviewRecord = new JviewRecord(subclassCd, baseSubclassCd, nowClassName, nowSubclassName, semester, followGrade, viewCd, status, statusName);
                    final Map setSubMap;
                    if (_jviewRecordSemeMap.containsKey(semester)) {
                        setSubMap = (Map) _jviewRecordSemeMap.get(semester);
                    } else {
                        setSubMap = new LinkedMap();
                    }
                    final Map jviewMap;
                    final String sKey = followGrade + ":" + subclassCd;
                    if (setSubMap.containsKey(sKey)) {
                        jviewMap = (Map) setSubMap.get(sKey);
                    } else {
                        jviewMap = new LinkedMap();
                    }
                    jviewMap.put(viewCd, jviewRecord);
                    setSubMap.put(sKey, jviewMap);
                    _jviewRecordSemeMap.put(semester, setSubMap);

                    if (!_jviewSubclsConvMap.containsKey(subclassCd)) {
                        _jviewSubclsConvMap.put(subclassCd, baseSubclassCd);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

        }

        private String getJviewScoreSql() {
            final StringBuffer stb = new StringBuffer();

            final String d029NameCd1 = ("P".equals(_param._schoolKind) && _param._kantenHyoukaCnt_P > 0) ? "DP29" : "D029";

            stb.append(" SELECT ");
            stb.append("  C1.CLASSNAME AS NOW_CLASSNAME, ");
            stb.append("  C2.SUBCLASSNAME AS NOW_SUBCLASSNAME, ");
            stb.append("  T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS BASE_SUBCLASSCD, ");
            stb.append("  T1.TARGET_CLASSCD || '-' || T1.TARGET_SCHOOL_KIND || '-' || T1.TARGET_CURRICULUM_CD || '-' || T1.TARGET_SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("  T1.SEMESTER, ");
            stb.append("  T1.TARGET_VIEWCD AS VIEWCD, ");
            stb.append("  CASE WHEN T1.TARGET_GRADE IS NULL THEN T2.GRADE ELSE T1.TARGET_GRADE END  AS FOLLOW_GRADE, ");
            stb.append("  T1.STATUS, ");
            stb.append("  D029.NAMESPARE1 AS MARK ");
            stb.append(" FROM ");
            stb.append("  JVIEWSTAT_RECORD_TARGETGRADE_DAT T1 ");
            stb.append("  INNER JOIN SCHREG_REGD_DAT T2 ");
            stb.append("    ON T2.YEAR = T1.YEAR ");
            stb.append("   AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("   AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("  LEFT JOIN V_NAME_MST D029 ON D029.YEAR = T1.YEAR ");
            stb.append("   AND D029.NAMECD1 = '" + d029NameCd1 + "' ");
            stb.append("   AND D029.ABBV1   = T1.STATUS ");
            stb.append("  LEFT JOIN CLASS_MST C1 ");
            stb.append("    ON C1.CLASSCD = T1.CLASSCD ");
            stb.append("   AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("  LEFT JOIN SUBCLASS_MST C2 ");
            stb.append("    ON C2.CLASSCD = T1.CLASSCD ");
            stb.append("   AND C2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("   AND C2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("   AND C2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("  AND T1.SCHREGNO = '" + _schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("   T1.SEMESTER, ");
            stb.append("   T1.TARGET_CLASSCD || '-' || T1.TARGET_SCHOOL_KIND || '-' || T1.TARGET_CURRICULUM_CD || '-' || T1.TARGET_SUBCLASSCD, ");
            stb.append("   T1.TARGET_GRADE, ");
            stb.append("   T1.TARGET_VIEWCD ");

            return stb.toString();
        }

        private void setBehaviorSeme(final DB2UDB db2) {
            final String behaviorSemeSql = getBehaviorSemeSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(behaviorSemeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("CODE");
                    final String viewName = rs.getString("VIEWNAME");
                    final String record = rs.getString("RECORD");
                    final BehaviorSeme addwk = new BehaviorSeme(code, viewName, record);
                    _behaviorSemeMap.put(code, addwk);
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getBehaviorSemeSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.CODE, ");
            stb.append("   T1.VIEWNAME, ");
            stb.append("   N1.NAMESPARE1 AS RECORD ");
            stb.append(" FROM ");
            stb.append("     BEHAVIOR_SEMES_MST T1 ");
            stb.append("     LEFT JOIN BEHAVIOR_SEMES_DAT T2 ");
            stb.append("       ON T2.YEAR = T1.YEAR ");
            stb.append("      AND T2.SEMESTER = '" + _param._semester + "' ");
            stb.append("      AND T2.SCHREGNO = '" + _schregno + "' ");
            stb.append("      AND T2.CODE = T1.CODE ");
            stb.append("     LEFT JOIN NAME_MST N1 ");
            stb.append("       ON N1.NAMECD1 = 'D036' ");
            stb.append("      AND N1.NAMECD2 = T2.RECORD ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("   AND T1.GRADE = '" + _grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CODE ");

            return stb.toString();
        }

        private void setJirituInfo(final DB2UDB db2, final Student student) {
            final String jirituInfoSql1 = getJirituInfoSql1();
            PreparedStatement ps1 = null;
            ResultSet rs1 = null;
            final String jirituInfoSql2 = getJirituInfoSql2();
            PreparedStatement ps2 = null;
            ResultSet rs2 = null;
            try {
                ps1 = db2.prepareStatement(jirituInfoSql1);

                ps1.setString(1, student._schregno);
                rs1 = ps1.executeQuery();
                while (rs1.next()) {
                    _selfReliance._long_Goals1 = rs1.getString("LONG_GOALS1");
                    _selfReliance._short_Goals1 = rs1.getString("SHORT_GOALS1");
                    _selfReliance._evaluation1 = rs1.getString("EVALUATION1");
                    _selfReliance._long_Goals2 = rs1.getString("LONG_GOALS2");
                    _selfReliance._short_Goals2 = rs1.getString("SHORT_GOALS2");
                    _selfReliance._evaluation2 = rs1.getString("EVALUATION2");
                    _selfReliance._long_Goals3 = rs1.getString("LONG_GOALS3");
                    _selfReliance._short_Goals3 = rs1.getString("SHORT_GOALS3");
                    _selfReliance._evaluation3 = rs1.getString("EVALUATION3");
                    _selfReliance._long_Goals4 = rs1.getString("LONG_GOALS4");
                    _selfReliance._short_Goals4 = rs1.getString("SHORT_GOALS4");
                    _selfReliance._evaluation4 = rs1.getString("EVALUATION4");
                }

                ps2 = db2.prepareStatement(jirituInfoSql2);
                ps2.setString(1, student._schregno);
                rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    _selfReliance._key_Goals = rs2.getString("KEY_GOALS");
                    _selfReliance._goals_Reason = rs2.getString("GOALS_REASON");
                }
            } catch (SQLException ex) {
                log.error("setJirituInfo exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps1, rs1);
                DbUtils.closeQuietly(null, ps2, rs2);
                db2.commit();
            }
        }

        private String getJirituInfoSql1() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     LONG_GOALS1, ");
            stb.append("     SHORT_GOALS1, ");
            stb.append("     EVALUATION1, ");
            stb.append("     LONG_GOALS2, ");
            stb.append("     SHORT_GOALS2, ");
            stb.append("     EVALUATION2, ");
            stb.append("     LONG_GOALS3, ");
            stb.append("     SHORT_GOALS3, ");
            stb.append("     EVALUATION3, ");
            stb.append("     LONG_GOALS4, ");
            stb.append("     SHORT_GOALS4, ");
            stb.append("     EVALUATION4 ");
            stb.append(" FROM ");
            stb.append("     V_HREPORT_SELFRELIANCE_SCHREG_REMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' AND ");
            stb.append("     SEMESTER = '" + _param._semester + "' AND ");
            stb.append("     SCHREGNO = ? ");

            return stb.toString();
        }

        private String getJirituInfoSql2() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     KEY_GOALS, ");
            stb.append("     GOALS_REASON ");
            stb.append(" FROM ");
            stb.append("     V_HREPORT_SELFRELIANCE_SCHREG_REMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' AND ");
            stb.append("     SEMESTER = '9' AND ");
            stb.append("     SCHREGNO = ? ");

            return stb.toString();
        }


//        private String getMedexamMonthSql() {
//            final Map conditionMap = (Map) _param._hreportConditionMap.get(_schoolKind);
//            final HreportCondition hreportCondition5;
//            if (null != conditionMap) {
//                hreportCondition5 = (HreportCondition) conditionMap.get("205");
//            } else {
//                hreportCondition5 = new HreportCondition("", "", "", "", "", "", "", "", "", "");
//            }
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     SEMESTER, ");
//            stb.append("     MONTH, ");
//            stb.append("     HEIGHT, ");
//            stb.append("     WEIGHT, ");
//            stb.append("     R_BAREVISION_MARK, ");
//            stb.append("     R_VISION_MARK, ");
//            stb.append("     L_BAREVISION_MARK, ");
//            stb.append("     L_VISION_MARK ");
//            stb.append(" FROM ");
//            stb.append("     MEDEXAM_DET_MONTH_DAT ");
//            stb.append(" WHERE ");
//            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
//            stb.append("     AND SCHREGNO = '" + _schregNo + "' ");
//            stb.append("     AND SEMESTER || MONTH IN ('" + hreportCondition5._remark1 + "', '" + hreportCondition5._remark2 + "') ");
//            stb.append(" ORDER BY ");
//            stb.append("     SEMESTER, INT(MONTH) + CASE WHEN INT(MONTH) < 4 THEN 12 ELSE 0 END ");
//
//            return stb.toString();
//        }
//
//        private String getMedexamDetSql() {
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     F010R1.NAME1 AS R_EAR, ");
//            stb.append("     F010R2.NAME1 AS R_EAR_IN, ");
//            stb.append("     F010L1.NAME1 AS L_EAR, ");
//            stb.append("     F010L2.NAME1 AS L_EAR_IN ");
//            stb.append(" FROM ");
//            stb.append("     V_MEDEXAM_DET_DAT MEDEXAM ");
//            stb.append("     LEFT JOIN NAME_MST F010R1 ON F010R1.NAMECD1 = 'F010' ");
//            stb.append("          AND MEDEXAM.R_EAR = F010R1.NAMECD2 ");
//            stb.append("     LEFT JOIN NAME_MST F010R2 ON F010R2.NAMECD1 = 'F010' ");
//            stb.append("          AND MEDEXAM.R_EAR_IN = F010R2.NAMECD2 ");
//            stb.append("     LEFT JOIN NAME_MST F010L1 ON F010L1.NAMECD1 = 'F010' ");
//            stb.append("          AND MEDEXAM.L_EAR = F010L1.NAMECD2 ");
//            stb.append("     LEFT JOIN NAME_MST F010L2 ON F010L2.NAMECD1 = 'F010' ");
//            stb.append("          AND MEDEXAM.L_EAR_IN = F010L2.NAMECD2 ");
//            stb.append(" WHERE ");
//            stb.append("     MEDEXAM.YEAR = '" + _param._ctrlYear + "' ");
//            stb.append("     AND MEDEXAM.SCHREGNO = '" + _schregNo + "' ");
//            stb.append("  ");
//
//            return stb.toString();
//        }

        private void setTokusyuKamoku(final DB2UDB db2) {
            final String tokusyuKamokuSql = getTokusyuKamokuSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(tokusyuKamokuSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String divname = rs.getString("DIVNAME");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    if ("SOUGAKU".equals(divname)) {
                        _sougakuSubClassCd = subclassCd;
                    } else if ("TOKKATSU".equals(divname)) {
                        _tokkatsuSubClassCd = subclassCd;
                    } else if ("JIKATSU".equals(divname)) {
                        _jikatsuSubClassCd = subclassCd;
                    }
                }
            } catch (SQLException ex) {
                log.error("setTokusyuKamoku exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getTokusyuKamokuSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     'SOUGAKU' AS DIVNAME, ");
            stb.append("     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT CHAIR_STD ");
            stb.append("     INNER JOIN CHAIR_DAT CHAIR ON CHAIR_STD.YEAR = CHAIR.YEAR ");
            stb.append("           AND CHAIR_STD.SEMESTER = CHAIR.SEMESTER ");
            stb.append("           AND CHAIR_STD.CHAIRCD = CHAIR.CHAIRCD ");
            stb.append("           AND CHAIR.CLASSCD = '90' ");
            stb.append(" WHERE ");
            stb.append("     CHAIR_STD.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND CHAIR_STD.SCHREGNO = '" + _schregno + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     'TOKKATSU' AS DIVNAME, ");
            stb.append("     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT CHAIR_STD ");
            stb.append("     INNER JOIN CHAIR_DAT CHAIR ON CHAIR_STD.YEAR = CHAIR.YEAR ");
            stb.append("           AND CHAIR_STD.SEMESTER = CHAIR.SEMESTER ");
            stb.append("           AND CHAIR_STD.CHAIRCD = CHAIR.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     CHAIR_STD.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND CHAIR_STD.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD ");
            stb.append("         IN(SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _param._ctrlYear + "' AND NAMECD1 = 'E066') ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     'JIKATSU' AS DIVNAME, ");
            stb.append("     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT CHAIR_STD ");
            stb.append("     INNER JOIN CHAIR_DAT CHAIR ON CHAIR_STD.YEAR = CHAIR.YEAR ");
            stb.append("           AND CHAIR_STD.SEMESTER = CHAIR.SEMESTER ");
            stb.append("           AND CHAIR_STD.CHAIRCD = CHAIR.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     CHAIR_STD.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND CHAIR_STD.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD ");
            stb.append("         IN(SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _param._ctrlYear + "' AND NAMECD1 = 'E065') ");

            return stb.toString();
        }

    }

    private class MedexamMonth {
        final String _semester;
        final String _month;
        final String _height;
        final String _weight;
        final String _rBarevisionMark;
        final String _rVisionMark;
        final String _lBarevisionMark;
        final String _lVisionMark;

        public MedexamMonth(
                final String semester,
                final String month,
                final String height,
                final String weight,
                final String rBarevisionMark,
                final String rVisionMark,
                final String lBarevisionMark,
                final String lVisionMark
        ) {
            _semester = semester;
            _month = month;
            _height = height;
            _weight = weight;
            _rBarevisionMark = rBarevisionMark;
            _rVisionMark = rVisionMark;
            _lBarevisionMark = lBarevisionMark;
            _lVisionMark = lVisionMark;
        }
    }

    private class Medexamdet {
        final String _rEar;
        final String _rEarIn;
        final String _lEar;
        final String _lEarIn;

        public Medexamdet(
                final String rEar,
                final String rEarIn,
                final String lEar,
                final String lEarIn
        ) {
            _rEar = rEar;
            _rEarIn = rEarIn;
            _lEar = lEar;
            _lEarIn = lEarIn;
        }
    }

    private class UnitData {
        final String _unitCd;
        final String _unitName;
        final String _guidancePattern;
        final Map _unitSeqMap;

        public UnitData(final String unitCd, final String unitName, final String guidancePattern) {
            _unitCd = unitCd;
            _unitName = unitName;
            _guidancePattern = guidancePattern;
            _unitSeqMap = new HashMap();
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
        final String _remark;
        final String _communication;
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
                final String remark,
                final String communication
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
            _remark = remark;
            _communication = communication;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            final String movedate = param._moveDate.replace('/', '-');
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(movedate) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(movedate) > 0 ? movedate : dateRange._edate;
            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            PreparedStatement psHreportRemark = null;
            ResultSet rsHreportRemark = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._ctrlYear,
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                psAtSeme = db2.prepareStatement(sql);

                final String hreportRemarkSql = getHreportRemarkSql(param, dateRange);
                psHreportRemark = db2.prepareStatement(hreportRemarkSql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    psHreportRemark.setString(1, student._schregno);
                    rsHreportRemark = psHreportRemark.executeQuery();

                    String setRemark = "";
                    String setCommunication = "";
                    while (rsHreportRemark.next()) {
                        setRemark = rsHreportRemark.getString("ATTENDREC_REMARK");
                        setCommunication = rsHreportRemark.getString("COMMUNICATION");
                    }
                    DbUtils.closeQuietly(rsHreportRemark);

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
                                setRemark,
                                setCommunication
                        );
                        student._attendMap.put(dateRange._key, attendance);
                    }
                    DbUtils.closeQuietly(rsAtSeme);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(psHreportRemark);
                DbUtils.closeQuietly(psAtSeme);
                db2.commit();
            }
        }

        private static String getHreportRemarkSql(final Param param, final DateRange dateRange) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
//            stb.append("     TOTALSTUDYTIME, ");
//            stb.append("     SPECIALACTREMARK, ");
            stb.append("     COMMUNICATION, ");
//            stb.append("     REMARK1, ");
//            stb.append("     REMARK2, ");
//            stb.append("     REMARK3, ");
//            stb.append("     FOREIGNLANGACT, ");
            stb.append("     ATTENDREC_REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + dateRange._key + "' ");
            stb.append("     AND SCHREGNO = ? ");

            return stb.toString();
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

        private static void load(final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange) {
            log.info(" subclass attendance dateRange = " + dateRange);
            final String movedate = param._moveDate.replace('/', '-');
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(movedate) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(movedate) > 0 ? movedate : dateRange._edate;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._ctrlYear,
                        param._semester,
                        dateRange._sdate,
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
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))
                             || subclasscd.equals(student._tokkatsuSubClassCd) || subclasscd.equals(student._jikatsuSubClassCd)
                        ) {

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
                            setSubAttendMap.put(dateRange._key, subclassAttendance);

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

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static class RecordSyoken {
        final String _classCd;
        final String _subclassCd;
        final String _remark1;
        public RecordSyoken(final String classCd, final String subclassCd, final String remark1) {
            _classCd = classCd;
            _subclassCd = subclassCd;
            _remark1 = remark1;
        }
    }

    private class ScoreData {
        final String _classCd;
        final String _subclassCd;
        final String _getCredit;
        final String _score;
        final String _remark;
        private ScoreData(
                final String classCd,
                final String subclassCd,
                final String getCredit,
                final String score,
                final String remark
        ) {
            _classCd = classCd;
            _subclassCd = subclassCd;
            _getCredit = getCredit;
            _score = score;
            _remark = remark;
        }
    }

    private class JviewRecord {
        final String _subclassCd;
        final String _baseSubclassCd;
        final String _nowClassName;
        final String _nowSubclassName;
        final String _semester;
        final String _followGrade;
        final String _viewCd;
        final String _status;
        final String _statusName;
        private JviewRecord(
                final String subclassCd,
                final String baseSubclassCd,
                final String nowClassName,
                final String nowSubclassName,
                final String semester,
                final String followGrade,
                final String viewCd,
                final String status,
                final String statusName
        ) {
            _subclassCd = subclassCd;
            _baseSubclassCd = baseSubclassCd;
            _nowClassName = nowClassName;
            _nowSubclassName = nowSubclassName;
            _semester = semester;
            _followGrade = followGrade;
            _viewCd = viewCd;
            _status = status;
            _statusName = statusName;
        }
    }
    private class BehaviorSeme {
        final String _code;
        final String _viewname;
        final String _record;
        public BehaviorSeme (final String code, final String viewname, final String record)
        {
            _code = code;
            _viewname = viewname;
            _record = record;
        }
    }

    private static class Semester {
        final String _semester;
        final String _semesterName;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semesterName = semestername;
            _dateRange = new DateRange(_semester, _semesterName, sdate, edate);
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

    private class HreportCondition {
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _remark4;
        final String _remark5;
        final String _remark6;
        final String _remark7;
        final String _remark8;
        final String _remark9;
        final String _remark10;

        public HreportCondition(
                final String remark1,
                final String remark2,
                final String remark3,
                final String remark4,
                final String remark5,
                final String remark6,
                final String remark7,
                final String remark8,
                final String remark9,
                final String remark10
        ) {
            _remark1  = StringUtils.defaultString(remark1);
            _remark2  = StringUtils.defaultString(remark2);
            _remark3  = StringUtils.defaultString(remark3);
            _remark4  = StringUtils.defaultString(remark4);
            _remark5  = StringUtils.defaultString(remark5);
            _remark6  = StringUtils.defaultString(remark6);
            _remark7  = StringUtils.defaultString(remark7);
            _remark8  = StringUtils.defaultString(remark8);
            _remark9  = StringUtils.defaultString(remark9);
            _remark10 = StringUtils.defaultString(remark10);
        }
    }

    private class CertifSchool {
        final String _schoolName;
        final String _jobName;
        final String _principalName;

        public CertifSchool(
                final String schoolName,
                final String jobName,
                final String principalName
        ) {
            _schoolName  = StringUtils.defaultString(schoolName);
            _jobName  = StringUtils.defaultString(jobName);
            _principalName = StringUtils.defaultString(principalName);
        }
    }

    private class HreportRemarkDat {
        final String _totalstudytime;
        final String _specialactremark;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _foreignlangact;

        public HreportRemarkDat(
                final String totalstudytime,
                final String specialactremark,
                final String remark1,
                final String remark2,
                final String remark3,
                final String foreignlangact
        ) {
            _totalstudytime = totalstudytime;
            _specialactremark = specialactremark;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _foreignlangact = foreignlangact;
        }
    }

    private static class SubclassMst implements Comparable {
        final String _specialDiv;
        final String _classCd;
        final String _electDiv;
        final String _subclassCd;
        final String _classabbv;
        final String _className;
        final String _subclassName;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        final String _calculateCreditFlg;
        public SubclassMst(final String specialDiv, final String classcd, final String electDiv, final String subclasscd,
                final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3, final Integer subclassShoworder3,
                final boolean isSaki, final boolean isMoto, final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classCd = classcd;
            _electDiv = electDiv;
            _subclassCd = subclasscd;
            _classabbv = classabbv;
            _className = classname;
            _subclassName = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public int compareTo(final Object o) {
            final SubclassMst os = (SubclassMst) o;
            int rtn;
            rtn = _electDiv.compareTo(os._electDiv);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _classShoworder3.compareTo(os._classShoworder3);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _classCd.compareTo(os._classCd);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _subclassShoworder3.compareTo(os._subclassShoworder3);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _subclassCd.compareTo(os._subclassCd);
            return rtn;
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclassCd + ")";
        }
    }

    private List subclassListRemoveD026() {
        final List retList = new ArrayList(_param._subclassMstMap.values());
        for (final Iterator it = retList.iterator(); it.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) it.next();
            if (_param._d026List.contains(subclassMst._subclassCd)) {
                it.remove();
            }
        }
        return retList;
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

    private class SelfReliance {
        String _long_Goals1;
        String _short_Goals1;
        String _evaluation1;
        String _long_Goals2;
        String _short_Goals2;
        String _evaluation2;
        String _long_Goals3;
        String _short_Goals3;
        String _evaluation3;
        String _long_Goals4;
        String _short_Goals4;
        String _evaluation4;
        String _key_Goals;
        String _goals_Reason;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77018 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _hukusikiRadio;
        final String _schoolKind;
        final String _ghrCd;
        final String[] _categorySelected;
        final String _moveDate;
        final boolean _isPrintHyosi;
        final String _frmPatern;
//        final String _seisekiFrm;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
//        final String _prgid;
        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolCd;
        final String _selectGhr;
//        final String _printLogStaffcd;
//        final String _printLogRemoteIdent;
//        final String _schoolName;
//        final String _recordDate;
//        final String _printSize;
        final Map _semesterMap;
//        final boolean _is2Gakki;
        private Map _subclassMstMap;
        final Map _creditMap;
        private final Map _jviewGradeMap;
        private List _d026List = Collections.EMPTY_LIST;
        String _lastSemester;
        final Map _hreportConditionMap;
        final Map _certifSchoolMap;

        /** 行動の記録タイトル */
        final Map _d035;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;
        Map _attendRanges;

        /** 自立支援タイトル */
        private final Map _jirituTitleMap;
        private final String _output1;

        private final boolean _printSideFlg1;  //表紙・裏表紙出力フラグ
        private final boolean _printSideFlg2;  //学習の記録出力フラグ
        private final boolean _printSideFlg3;  //出欠の記録出力フラグ

        private int _kantenHyoukaCnt_P = 0;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _output1 =  request.getParameter("OUTPUT1");
            _hukusikiRadio = request.getParameter("HUKUSIKI_RADIO");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _ghrCd = request.getParameter("GHR_CD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _moveDate = request.getParameter("MOVE_DATE");
            _isPrintHyosi = "1".equals(request.getParameter("HYOSI"));
            final String paraFrmPatern = request.getParameter("PRINT_PATTERN");
            _frmPatern = getFormPatern(db2, paraFrmPatern);
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
//            _prgid = request.getParameter("PRGID");
            _schoolCd = request.getParameter("SCHOOLCD");
            _selectGhr = request.getParameter("SELECT_GHR");
//            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
//            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
//            _printSize = request.getParameter("PRINTSIZE");
            _printSideFlg1 = "1".equals(request.getParameter("PRINT_SIDE1"));
            _printSideFlg2 = "1".equals(request.getParameter("PRINT_SIDE2"));
            _printSideFlg3 = "1".equals(request.getParameter("PRINT_SIDE3"));
//            _schoolName = getSchoolName(db2);
//            _recordDate = "9999-03-31";
            _semesterMap = loadSemester(db2);
//            _is2Gakki = _semesterMap.size() == 3;
//            _seisekiFrm = getSeisekiFrm();
            setSubclassMst(db2);
            _creditMap = getCreditMap(db2);
            _jviewGradeMap = getJviewGradeMap(db2);
            _hreportConditionMap = getHreportConditionMap(db2);
            _certifSchoolMap = getCertifSchoolMap(db2);
            _d035 = getVNameMst(db2, "D035");
            loadNameMstD026(db2);
            loadNameMstD029(db2);
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");

            _jirituTitleMap = getJirituActTitles(db2);
        }

        private Map getJirituActTitles(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   KIND_SEQ, ");
            stb.append("   KIND_REMARK ");
            stb.append(" FROM ");
            stb.append("   HREPORT_GUIDANCE_KIND_NAME_DAT ");
            stb.append(" WHERE ");
            stb.append("   YEAR = '" + _ctrlYear + "' ");
            stb.append("   AND KIND_NO = '30' ");  //固定。仕様書に記載。

            Map retMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("KIND_SEQ"), rs.getString("KIND_REMARK"));
                }
            } catch (SQLException ex) {
                log.error("getJirituActTitles exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }



        private String getFormPatern(final DB2UDB db2, final String paraFrmPatern) {
            String retStr = "";
            if ("201".equals(paraFrmPatern)) {
                retStr = PATTERN_A;
            } else if ("202".equals(paraFrmPatern)) {
                retStr = PATTERN_B;
            } else if ("203".equals(paraFrmPatern)) {
                retStr = PATTERN_C;
            } else if ("204".equals(paraFrmPatern)) {
                retStr = PATTERN_D;
            } else if ("205".equals(paraFrmPatern)) {
                retStr = PATTERN_E;
            } else {
                log.error(" Not support formPatern : " + paraFrmPatern);
            }
            return retStr;
        }

        private String getHReportCondMapData(final String schoolKind, final String seq, final int remarkNo) {
            String retStr = null;
            final Map conditionMap = (Map) _hreportConditionMap.get(schoolKind);
            if (conditionMap != null && conditionMap.containsKey(seq)) {
                HreportCondition chkObj = (HreportCondition)conditionMap.get(seq);
                switch (remarkNo) {
                case 1:
                    retStr = chkObj._remark1;
                    break;
                case 2:
                    retStr = chkObj._remark2;
                    break;
                case 3:
                    retStr = chkObj._remark3;
                    break;
                case 4:
                    retStr = chkObj._remark4;
                    break;
                case 5:
                    retStr = chkObj._remark5;
                    break;
                case 6:
                    retStr = chkObj._remark6;
                    break;
                case 7:
                    retStr = chkObj._remark7;
                    break;
                case 8:
                    retStr = chkObj._remark8;
                    break;
                case 9:
                    retStr = chkObj._remark9;
                    break;
                case 10:
                    retStr = chkObj._remark10;
                    break;
                default:
                    break;
                }
            }
            return retStr;
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            _lastSemester = _semester;
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
                        + "   SEMESTER_MST"
                        + " where"
                        + "   YEAR='" + _ctrlYear + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    map.put(semester, new Semester(semester, rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                    if (!SEMEALL.equals(semester)) {
                        _lastSemester = semester;
                    }
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private Map getCreditMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GRADE, ");
            stb.append("     COURSECD, ");
            stb.append("     MAJORCD, ");
            stb.append("     COURSECODE, ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     CREDITS ");
            stb.append(" FROM ");
            stb.append("     CREDIT_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");

            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String credits = StringUtils.defaultString(rs.getString("CREDITS"));
                    retMap.put(grade + courseCd + majorCd + courseCode + subclassCd, credits);
                }
            } catch (SQLException ex) {
                log.error("getCreditMap exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getHreportConditionMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            //SEQ LIKE '1%'について。佐賀は知的障害(1)と知的以外(2)の設定があり、SEQが1××又は、2××と登録される。
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     HREPORT_CONDITION_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SCHOOLCD = '" + _schoolCd + "' ");
            stb.append("     AND SEQ LIKE '2%' ");
            stb.append(" ORDER BY ");
            stb.append("     SCHOOL_KIND ");

            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String setSchoolKind = rs.getString("SCHOOL_KIND");
                    final String seq = rs.getString("SEQ");
                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final String remark3 = rs.getString("REMARK3");
                    final String remark4 = rs.getString("REMARK4");
                    final String remark5 = rs.getString("REMARK5");
                    final String remark6 = rs.getString("REMARK6");
                    final String remark7 = rs.getString("REMARK7");
                    final String remark8 = rs.getString("REMARK8");
                    final String remark9 = rs.getString("REMARK9");
                    final String remark10 = rs.getString("REMARK10");
                    final HreportCondition condition = new HreportCondition(remark1, remark2, remark3, remark4, remark5, remark6, remark7, remark8, remark9, remark10);
                    final Map seqMap;
                    if (retMap.containsKey(setSchoolKind)) {
                        seqMap = (Map) retMap.get(setSchoolKind);
                    } else {
                        seqMap = new HashMap();
                    }
                    seqMap.put(seq, condition);
                    retMap.put(setSchoolKind, seqMap);
                }
            } catch (SQLException ex) {
                log.error("getHreportConditionMap exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getCertifSchoolMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CERTIF_KINDCD, ");
            stb.append("     SCHOOL_NAME, ");
            stb.append("     JOB_NAME, ");
            stb.append("     PRINCIPAL_NAME ");
            stb.append(" FROM ");
            stb.append("     CERTIF_SCHOOL_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND CERTIF_KINDCD IN ('103', '104', '117') ");

            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String certifKindcd = rs.getString("CERTIF_KINDCD");
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    final String jobName = rs.getString("JOB_NAME");
                    final String principalName = rs.getString("PRINCIPAL_NAME");
                    final CertifSchool certifSchol = new CertifSchool(schoolName, jobName, principalName);
                    final String setSchoolKind;

                    if ("104".equals(certifKindcd)) {
                        //高校と専攻は同じ証明書コードを使用
                        retMap.put("H", certifSchol);
                        retMap.put("A", certifSchol);
                    } else {
                        if ("117".equals(certifKindcd)) {
                            setSchoolKind = "P";
                        } else { //103
                            setSchoolKind = "J";
                        }
                        retMap.put(setSchoolKind, certifSchol);
                    }
                }
            } catch (SQLException ex) {
                log.error("getCertifSchoolMap exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getVNameMst(final DB2UDB db2, final String nameCd1) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMECD2, ");
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND NAMECD1 = '" + nameCd1 + "' ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD1 ");

            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String namecd2 = rs.getString("NAMECD2");
                    final String name1 = rs.getString("NAME1");
                    retMap.put(namecd2, name1);
                }
            } catch (SQLException ex) {
                log.error("getVNameMst exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
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
                sql += " VALUE(T1.ELECTDIV, '0') AS ELECTDIV, ";
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
                    final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("ELECTDIV"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), isSaki, isMoto, rs.getString("CALCULATE_CREDIT_FLG"));
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
            stb.append("     T1.GRADE, ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     T1.VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ");
            stb.append("       ON T2.GRADE = T1.GRADE ");
            stb.append("      AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("      AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("      AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("      AND T2.VIEWCD = T1.VIEWCD ");
            stb.append(" WHERE ");
            stb.append("   T2.YEAR = '" + _ctrlYear + "' ");
            stb.append(" ORDER BY ");
            stb.append("     GRADE, ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     VIEWCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            log.debug("sql jviewGrade = " + stb.toString());
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                List jviewGradeList = null;
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String viewCd = rs.getString("VIEWCD");
                    final String viewName = rs.getString("VIEWNAME");

                    final Map subclassMap;
                    if (retMap.containsKey(grade)) {
                        subclassMap = (Map) retMap.get(grade);
                    } else {
                        subclassMap = new TreeMap();
                    }
                    final JviewGrade jviewGrade = new JviewGrade(subclassCd, viewCd, viewName);
                    if (subclassMap.containsKey(subclassCd)) {
                        jviewGradeList = (List) subclassMap.get(subclassCd);
                    } else {
                        jviewGradeList = new ArrayList();
                    }
                    jviewGradeList.add(jviewGrade);
                    subclassMap.put(subclassCd, jviewGradeList);
                    retMap.put(grade, subclassMap);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
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

        private void loadNameMstD029(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
            final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
            sql.append(" SELECT COUNT(*) AS CNT FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'DP29' ");

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _kantenHyoukaCnt_P = rs.getInt("CNT");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

    }
}

// eof

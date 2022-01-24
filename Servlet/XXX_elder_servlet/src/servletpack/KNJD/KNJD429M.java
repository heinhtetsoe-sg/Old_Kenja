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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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

public class KNJD429M {

    private static final Log log = LogFactory.getLog(KNJD429M.class);

    private static final String SEMES_3TERM = "3";
    private static final String SEMEALL = "9";

    private static final String ATTR_CENTERING = "Hensyu=3";

    private boolean _hasData;

    final int FIRST_PAGE_MAXLINE = 36;
    final int PAGE_MAXLINE = 54;
    int LINE_CNT = 0;

    private static final String HC_101 = "101";  //タイトル(HreportCondition.remark10)
    private static final String HC_103 = "103";  //GHR?一般?(HreportCondition.remark1)
    private static final String HC_104 = "104";  //クラス名表示方法(//学年のみ/年組(法定)/年組(実クラス))(HreportCondition.remark1)
    private static final String HC_108 = "108";  //行動の記録を出力しない(HreportCondition.remark1)
    private static final String HC_109 = "109";  //重点目標を出力しない(HreportCondition.remark1)
    private static final String HC_112 = "112";  //家庭よりを出力しない(HreportCondition.remark1)
    private static final String HC_113 = "113";  //教育目標・生きる生徒像(HreportCondition._remark10)

    private static final String PT_PATTERN_1 = "101";
    private static final String PT_PATTERN_2 = "102";
    private static final String PT_PATTERN_3A = "103";
    private static final String PT_PATTERN_3B = "104";
    private Param _param;

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
        //生徒情報
        final List studentList = getStudentList(db2);
        //出欠
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
            LINE_CNT = 0;
            if (_param._isPrintHyosi) {
                printHyoushi(db2, svf, student);
            }
            if (_param._isPrintUraByosi) {
                printUraByoushi(db2, svf, student);
            }
            printSeiseki(svf, student);
            printAttend(svf, student);

            _hasData = true;
        }
    }

    private void printHyoushi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        boolean printFlg = false;
        final String setForm;
        setForm = "KNJD429M_1_A_1.frm";
        setForm(svf, setForm, 1, "hyoushi");
        final String gradeHrName = student._courseName + "　" + student._gradeName2;
        final CertifSchool certifSchool = (CertifSchool) _param._certifSchoolMap.get(student._schoolKind);
        final Map conditionMap = (Map) _param._hreportConditionMap.get(student._schoolKind);
        if (null != certifSchool) {
            //学校名
            svf.VrsOut("SCHOOL_NAME", certifSchool._schoolName);
            //校長名
            final int principalLen = KNJ_EditEdit.getMS932ByteLength(certifSchool._principalName);
            final String setPriField2 = principalLen > 30 ? "3" : principalLen > 20 ? "2" : "1";
            svf.VrsOut("PRINCIPAL_NAME" + setPriField2, certifSchool._principalName);
        }
        //氏名
        int nameLen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nameField = nameLen > 30 ? "3" : nameLen > 18 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name);

        //校章
        svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath);
        //年度
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度");

        //タイトル
        if (null != conditionMap) {
            final HreportCondition hreportCondition1 = (HreportCondition) conditionMap.get(HC_101);
            if (null != hreportCondition1) {
                svf.VrsOut("TITLE", hreportCondition1._remark10);
            }
            //クラス名
            final HreportCondition hreportCondition4 = (HreportCondition) conditionMap.get(HC_104);
            if (null != hreportCondition4) {
                if ("1".equals(hreportCondition4._remark1)) {//学年のみ表示
                    svf.VrsOut("HR_NAME", StringUtils.defaultString(gradeHrName, ""));
                } else if ("2".equals(hreportCondition4._remark1)) {//年組(法定)を表示
                    svf.VrsOut("HR_NAME", StringUtils.defaultString(gradeHrName, "") + StringUtils.defaultString(student._hrClassName2, ""));
                } else if ("3".equals(hreportCondition4._remark1)) {//年組(実クラス)を表示
                    svf.VrsOut("HR_NAME", StringUtils.defaultString(gradeHrName, "") + StringUtils.defaultString(student._ghrNameAbbv, ""));
                }
            }

            //教育目標・目指す生徒像
            final HreportCondition hreportCondition5 = (HreportCondition) conditionMap.get(HC_113);
            if (hreportCondition5 != null && hreportCondition5._remark10 != "") {
                final List<String> hope = KNJ_EditKinsoku.getTokenList(hreportCondition5._remark10, 120);
                for (int cnt = 0; cnt < hope.size(); cnt++) {
                    svf.VrsOutn("HOPE", cnt + 1, hope.get(cnt));
                }
            }

            //担任
            LinkedList<String> staffList = new LinkedList<String>();
            final HreportCondition hreportCondition3 = (HreportCondition) conditionMap.get(HC_103);
            final String stfStr1 = "2".equals(hreportCondition3._remark1) ? student._ghTannin1 : student._tannin1;
            if (!"".equals(StringUtils.defaultString(stfStr1))) {
                staffList.add(stfStr1);
            }
            final String stfStr2 = "2".equals(hreportCondition3._remark1) ? student._ghTannin2 : student._tannin2;
            if (!"".equals(StringUtils.defaultString(stfStr2))) {
                staffList.add(stfStr2);
            }
            final String stfStr3 = "2".equals(hreportCondition3._remark1) ? student._ghTannin3 : student._tannin3;
            if (!"".equals(StringUtils.defaultString(stfStr3))) {
                staffList.add(stfStr3);
            }
            final String stfStr4 = "2".equals(hreportCondition3._remark1) ? student._ghTannin4 : student._tannin4;
            if (!"".equals(StringUtils.defaultString(stfStr4))) {
                staffList.add(stfStr4);
            }
            final String stfStr5 = "2".equals(hreportCondition3._remark1) ? student._ghTannin5 : student._tannin5;
            if (!"".equals(StringUtils.defaultString(stfStr5))) {
                staffList.add(stfStr5);
            }
            final String stfStr6 = "2".equals(hreportCondition3._remark1) ? student._ghTannin6 : student._tannin6;
            if (!"".equals(StringUtils.defaultString(stfStr6))) {
                staffList.add(stfStr6);
            }

            if (staffList.size() == 1) {
                svf.VrsOut("TR_TITLE1", "担任");
                final String staffName = staffList.get(0);
                final int staffLen = KNJ_EditEdit.getMS932ByteLength(staffName);
                final String staffField = staffLen > 30 ? "3" : staffLen > 20 ? "2" : "1";
                svf.VrsOut("STAFF_NAME1_" + staffField, staffName);
                svf.VrEndRecord();
                printFlg = true;
            } else {
                for (String staffName : staffList) {
                    svf.VrsOut("TR_TITLE2", "担任");
                    final int staffLen = KNJ_EditEdit.getMS932ByteLength(staffName);
                    final String staffField = staffLen > 30 ? "3" : staffLen > 20 ? "2" : "1";
                    svf.VrsOut("STAFF_NAME2_" + staffField, staffName);
                    svf.VrEndRecord();
                    printFlg = true;
                }
            }
        }

        svf.VrEndPage();
    }

    private void printUraByoushi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String setForm;
        setForm = SEMES_3TERM.equals(_param._lastSemester) ? "KNJD429M_1_A_2_2.frm" : "KNJD429M_1_A_2_1.frm";
        setForm(svf, setForm, 1, "urabyoushi");
        //氏名タイトル
        svf.VrsOut("TITLE_NAME", "氏名");
        //氏名
        final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nfield = nlen > 20 ? "_3" : nlen > 16 ? "_2" : "_1";
        svf.VrsOut("NAME4" + nfield, student._name);

        //修了文言
        final String gradeHrName = student._courseName + "　" + student._gradeName3;
        svf.VrsOut("TEXT", gradeHrName + "の課程を修了したことを証します。");
        //日付フォーマットのみ
        final String[] dateArray = KNJ_EditDate.tate_format4(db2, _param._ctrlDate.replace('/', '-'));
        svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(db2, student._birthDay) + "生");
        svf.VrsOut("DATE", dateArray[0] + "　　年　　月　　日");
        final CertifSchool certifSchool = (CertifSchool) _param._certifSchoolMap.get(student._schoolKind);
        if (null != certifSchool) {
            //学校名
            svf.VrsOut("SCHOOL_NAME2", certifSchool._schoolName);
            //職種
            svf.VrsOut("JOB_NAME", certifSchool._jobName);
            //校長名
            final int principalLen = KNJ_EditEdit.getMS932ByteLength(certifSchool._principalName);
            final String setPriField = principalLen > 20 ? "3" : principalLen > 16 ? "2" : "1";
            svf.VrsOut("PRINCIPAL_NAME2_" + setPriField, certifSchool._principalName);
        }
        if (!_param._isPrintOuin) {
            svf.VrsOut("BLANK", _param._whiteSpaceImagePath);
        }
        svf.VrEndPage();
    }

    private void setForm(final Vrw32alp svf, final String setForm, final int no, final String comment) {
        svf.VrSetForm(setForm, no);
        log.info(" setform " + comment + " : " + setForm);
    }

    private int setStaff(final Vrw32alp svf, final String staffName, final int setLine) {
        int retInt = setLine;
        final int staffLen = KNJ_EditEdit.getMS932ByteLength(staffName);
        final String staffField = staffLen > 30 ? "3" : staffLen > 20 ? "2" : "1";
        if (staffLen > 0) {
            svf.VrsOutn("STAFF_NAME" + staffField, setLine, staffName);
            retInt++;
        }
        return retInt;
    }

    private void printTitleShimei(final Vrw32alp svf, final Student student, final Semester semesterObj) {
        svf.VrsOut("TITLE", semesterObj._semesterName + "の記録");
        svf.VrsOut("NAME", student._gradeName2 + "　" + student._name);
        svf.VrsOut("SEMESTER", semesterObj._semesterName);
    }

    private void printKoudounoKiroku(final Vrw32alp svf, final Student student, final Semester semesterObj) {
        final Map conditionMap = (Map) _param._hreportConditionMap.get(student._schoolKind);
        if (null != conditionMap) {
            final HreportCondition hreportCondition8 = (HreportCondition) conditionMap.get(HC_108);
            if ("2".equals(hreportCondition8._remark1)) {
                svf.VrsOut("VIEW_NAME1", "BLANK");
                svf.VrsOut("BLANK1", _param._whiteSpaceImagePath);
                svf.VrEndRecord();
                return;
            }
        }
        // 1件もデータが無いなら、表示しない。

        int koudouCnt = 1;
        final String getConvgrade = (String)student._behaviorConvGradeMap.get(student._grade);
        for (Iterator itbs = student._behaviorSemeMap.keySet().iterator();itbs.hasNext();) {
            final String kStr = (String)itbs.next();
            if (!kStr.startsWith(getConvgrade)) continue;
            if (koudouCnt > 13) continue;
            final HrBehaviorInfo record = (HrBehaviorInfo) student._behaviorSemeMap.get(kStr);
            svf.VrsOut("VIEW_NAME1", record._l_Name);
            svf.VrsOut("VIEW_NAME2", record._m_Name);
            if (record._behaviorDat.containsKey(_param._semester)) {
                final String resStr = (String)record._behaviorDat.get(_param._semester);
                final String resultStr = "3".equals(resStr) ? "◎" : ("2".equals(resStr) ? "○" : ("1".equals(resStr) ? "△" : ""));
                svf.VrsOut("VIEW", resultStr);
            }
            koudouCnt++;
            svf.VrEndRecord();
        }
    }

    // 行動、身体、出欠の記録
    private void printAttend(final Vrw32alp svf, final Student student) {
        final String formName;
        if (PT_PATTERN_3B.equals(_param._paraFrmPatern)) {
            formName = SEMES_3TERM.equals(_param._lastSemester) ? "KNJD429M_3_I_2.frm" : "KNJD429M_3_I_1.frm";
            setForm(svf, formName, 1, "attend");
        } else {
            formName = SEMES_3TERM.equals(_param._lastSemester) ? "KNJD429M_3_E_2.frm" : "KNJD429M_3_E.frm";
            setForm(svf, formName, 4, "attend");
        }

        final Semester semesterObj = (Semester) _param._semesterMap.get(_param._semester);
        printTitleShimei(svf, student, semesterObj);

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
            }
            if (student._hreportRemarkDatMap.containsKey(semester)) {
                final HreportRemarkDat hreportRemarkDat = (HreportRemarkDat) student._hreportRemarkDatMap.get(semester);
                final int attRFlen = KNJ_EditEdit.getMS932ByteLength(hreportRemarkDat._attendrecRemark);
                if (SEMES_3TERM.equals(_param._lastSemester)) {
                    final String attendRemarkField = attRFlen > 20 ? "_3" : (attRFlen > 14 ? "_2" : "_1");
                    if (attRFlen > 20) {
                        final String[] attCutStr = KNJ_EditEdit.get_token(hreportRemarkDat._attendrecRemark, 20, 2);
                        svf.VrsOut("ATTEND_REMARK" + semester + attendRemarkField, attCutStr[0]);
                        if (attCutStr.length > 1 && !"".equals(StringUtils.defaultString(attCutStr[1]))) {
                            svf.VrsOut("ATTEND_REMARK" + semester + "_4", attCutStr[1]);
                        }
                    } else {
                        svf.VrsOut("ATTEND_REMARK" + semester + attendRemarkField, hreportRemarkDat._attendrecRemark);
                    }
                } else {
                    final String attendRemarkField = attRFlen > 30 ? "_3" : (attRFlen > 20 ? "_2" : "_1");
                    svf.VrsOut("ATTEND_REMARK" + semester + attendRemarkField, hreportRemarkDat._attendrecRemark);
                }
            }
        }

        ////学校より
        final HreportRemarkDat hreportRemarkDat = (HreportRemarkDat) student._hreportRemarkDatMap.get(_param._semester);

        if (null != hreportRemarkDat) {
            if (null != hreportRemarkDat._communication) {
                final String[] communicationArray = KNJ_EditEdit.get_token(hreportRemarkDat._communication, 90, 6);
                for (int i = 0; i < communicationArray.length; i++) {
                    final String setText = communicationArray[i];
                    svf.VrsOutn("FROM_SCHOOL", i + 1, setText); // 学校より
                }
            }
        }
        ////家庭より[非表示制御あり]
        if (_param._lastSemester.equals(_param._semester)) {
            svf.VrsOut("BLANK2", _param._whiteSpaceImagePath);
        } else {
            final Map conditionMap = (Map) _param._hreportConditionMap.get(student._schoolKind);
            if (null != conditionMap) {
                final HreportCondition hreportCondition9 = (HreportCondition) conditionMap.get(HC_112);
                if ("2".equals(hreportCondition9._remark1)) {
                    svf.VrsOut("BLANK2", _param._whiteSpaceImagePath);
                }
            }
        }

        if (PT_PATTERN_3B.equals(_param._paraFrmPatern)) {
            ////生活の様子(行動の記録と同じ?)[非表示制御あり]
            printJissyuuTokukatu(svf, student, semesterObj);
        } else {
            ////生活の様子(行動の記録と同じ?)[非表示制御あり]
            printKoudounoKiroku(svf, student, semesterObj);
        }

        svf.VrEndPage();
    }

    private void printJissyuuTokukatu(final Vrw32alp svf, final Student student, final Semester semesterObj) {
        if (!student._hreportRemarkDatMap.containsKey(_param._semester)) {
            return;
        }
        final HreportRemarkDat hRepDat = (HreportRemarkDat)student._hreportRemarkDatMap.get(_param._semester);
        //実習の記録
        if ("1".equals(hRepDat._trSyaFlg1)) {
            svf.VrsOut("SLASH1", _param._slashImagePath);  //斜線
        } else {
            svfPrintRenzoku(svf, "VIEW_NAME1_1", hRepDat._training1, 9 * 2, 6);
            svfPrintRenzoku(svf, "VIEW_NAME1_2", hRepDat._trSyoken1, 36 * 2, 6);
        }
        if ("1".equals(hRepDat._trSyaFlg2)) {
            svf.VrsOut("SLASH2", _param._slashImagePath);  //斜線
        } else {
            svfPrintRenzoku(svf, "VIEW_NAME2_1", hRepDat._training2, 9 * 2, 6);
            svfPrintRenzoku(svf, "VIEW_NAME2_2", hRepDat._trSyoken2, 36 * 2, 6);
        }

        //特別活動の記録
        svfPrintRenzoku(svf, "SP_ACT1", hRepDat._hrAct, 13 * 2, 4);       //ホームルーム活動
        //svf.VrsOut(field, StringUtils.defaultString(hRepDat._hrAct));
        svfPrintRenzoku(svf, "SP_ACT2", hRepDat._studentAct, 13 * 2, 4);  //生徒会活動
        //svf.VrsOut(field, StringUtils.defaultString(hRepDat._studentAct));
        svfPrintRenzoku(svf, "SP_ACT3", hRepDat._clubOther, 19 * 2, 4);   //部活動・その他
        //svf.VrsOut(field, StringUtils.defaultString(hRepDat._clubOther));
    }

    private void svfPrintRenzoku(final Vrw32alp svf, final String field, final String putStr, final int f_len, final int f_cnt) {
        if (!"".equals(StringUtils.defaultString(putStr))) {
            final String[] tr1StrWk = KNJ_EditEdit.get_token(putStr, f_len, f_cnt);
            for (int cnt = 0;cnt < f_cnt;cnt++) {
                svf.VrsOutn(field, cnt + 1, tr1StrWk[cnt]);  //期日・実習先・実習内容1
            }
        }
    }
    private void printSeiseki(final Vrw32alp svf, final Student student) {
        if (StringUtils.isBlank(_param._seisekiFrm)) {
            log.warn(" seisekiFrm null.");
            return;
        }
        setForm(svf, _param._seisekiFrm, 4, "seiseki");
        printSeisekiTitle(svf, student);
        printPaternBCD(svf, student);
        svf.VrEndPage();
    }

    private void printSeisekiTitle(final Vrw32alp svf, final Student student) {
        final Semester semester;
        semester = (Semester) _param._semesterMap.get(_param._semester);
        printTitleShimei(svf, student, semester);
    }

    /** チェックして改ページ 【引数のcntは、出力行数】*/
    final void checkLineAndPageChange(final Vrw32alp svf, final int cnt, final int checkMaxLine, final Student student) {
        if (LINE_CNT + cnt > checkMaxLine) {
            setForm(svf, _param._seisekiFrm, 4, "checklineandpagechange");
            printSeisekiTitle(svf, student);
            LINE_CNT = 0;
        }
    }

    private void printPaternBCD(final Vrw32alp svf, Student student) {
        int lineMaxCnt = 52;
        int tgtCnt = 0;
        int tgtsubCnt = 0;
        //年間目標の出力可否を判断
        DecimalFormat df0Wk = new DecimalFormat("0000");
        boolean prtFlg = true;
        final Map conditionMap = (Map) _param._hreportConditionMap.get(student._schoolKind);
        if (null != conditionMap) {
            final HreportCondition hreportCondition9 = (HreportCondition) conditionMap.get(HC_109);
            if ("2".equals(hreportCondition9._remark1)) {
                prtFlg = false;
            }
        }
        if (prtFlg && _param._tatgetTtlMap.size() > 0) {
            final String kStr1 = (String)_param._tatgetTtlMap.keySet().iterator().next();
            final TargetInfo fstDat = (TargetInfo)_param._tatgetTtlMap.get(kStr1);
            //タイトル(重点目標)
            prtSubTitle(svf, "1", 0, fstDat._kind_Name, "", "");
            svf.VrEndRecord();
            //出力する目標の数だけ出力
            tgtCnt = _param._tatgetTtlMap.size() > 0 ? 1 : 0;
            tgtsubCnt = tgtCnt;
            String krStr[] = null;
            for (Iterator ite = _param._tatgetTtlMap.keySet().iterator();ite.hasNext();) {
                final String seq = (String)ite.next();
                log.debug("seq:" + seq);
                if ("".equals(StringUtils.defaultString(seq))) {
                    continue;
                }
                final TargetInfo tgtObj = (TargetInfo)_param._tatgetTtlMap.get(seq);
                String tgtStr = null;
                String[] prtDatas = null;
                int rowCnt = 0;
                final int oneLineMax = 10;
                if (student._targetDatMap.containsKey(String.valueOf(Integer.parseInt(seq)))) {
                    GoalsInfo gObj = (GoalsInfo)student._targetDatMap.get(String.valueOf(Integer.parseInt(seq)));
                    tgtStr = StringUtils.defaultString(gObj._goals, "");
                    //出力データの行数算出
                    prtDatas = KNJ_EditEdit.get_token(tgtStr, 40 * 2, oneLineMax);
                    rowCnt = cntUsefulRow(prtDatas, oneLineMax);
                }
                krStr = null;
                if (tgtObj._kind_Remark != null && tgtObj._kind_Remark.length() > 0) {
                    krStr = KNJ_EditEdit.get_token(tgtObj._kind_Remark, 10, 2);
                }
                rowCnt = Math.max(rowCnt, (krStr == null ? 0 : krStr.length <= 1 ? 1 : krStr[1] != null ? 2 : 1));
                //rowCntが0ならタイトル分だけは出力するので、1にする。
                if (rowCnt == 0) rowCnt = 1;
                //入るか入らないかを判定(入らないなら改ページ)
                if (tgtCnt + rowCnt > lineMaxCnt) {
                    svf.VrEndPage();
                    tgtCnt = 1;
                    tgtsubCnt = tgtCnt;
                }
                for (int dlCnt = 0;dlCnt < rowCnt;dlCnt++) {
                    //項目名の出力
                    if (krStr != null && dlCnt < krStr.length && krStr[dlCnt] != null) {
                        svf.VrsOut("CLASS_NAME2", krStr[dlCnt]);
                    }
                    //データの出力
                    svf.VrsOut("GRP2_1", df0Wk.format(tgtsubCnt));
                    svf.VrsOut("GRP2_3", df0Wk.format(tgtsubCnt));
                    if (tgtStr != null && prtDatas != null && dlCnt < prtDatas.length && prtDatas[dlCnt] != null) {
                        svf.VrsOut("CONTENT2_1", prtDatas[dlCnt]);
                    }
                    svf.VrEndRecord();
                    tgtCnt++;
                }
                tgtsubCnt++;
            }
        }

        int evalCnt = tgtCnt;
        //この時点で、フィールドの形式を確定しておく(1/2/3項目)
        String prtTitle1 = "";
        String prtTitle2 = "";
        String prtTitle3 = "";
        int fieldIdx = 0;
        //_param._paraFrmPaternの値と教科の単元登録で利用レコードがどれか決まる。
        if (PT_PATTERN_3A.equals(_param._paraFrmPatern)) {  //3項目出力
            fieldIdx += 6;
            prtTitle1 = (String)_param._evalTtlList.get(0);
            prtTitle2 = (String)_param._evalTtlList.get(1);
            prtTitle3 = (String)_param._evalTtlList.get(2);
        } else if (PT_PATTERN_3B.equals(_param._paraFrmPatern)) {  //3項目出力
                fieldIdx += 6;
                prtTitle1 = (String)_param._evalTtlList.get(0);
                prtTitle2 = (String)_param._evalTtlList.get(1);
                prtTitle3 = (String)_param._evalTtlList.get(2);
        } else if (PT_PATTERN_2.equals(_param._paraFrmPatern)) {  //2項目出力
            fieldIdx += 4;
            prtTitle1 = (String)_param._evalTtlList.get(0);
            prtTitle2 = (String)_param._evalTtlList.get(2);
        } else {  //1項目出力
            fieldIdx += 2;
            prtTitle1 = (String)_param._evalTtlList.get(2);
        }

        //タイトル
        int row1StrMax = 0;
        int row2StrMax = 0;
        int row3StrMax = 0;
        int col1StrMax = 0;
        int col2StrMax = 0;
        int col3StrMax = 0;
        if (!"".equals(prtTitle1)) {
            col1StrMax = 40;
            col2StrMax = 0;
            col3StrMax = 0;
            row1StrMax = 10;
            row2StrMax = 0;
            row3StrMax = 0;
        }
        if (!"".equals(prtTitle2)) {
            col1StrMax = 20;
            col2StrMax = 20;
            col3StrMax = 0;
            row1StrMax = 19;
            row2StrMax = 19;
            row3StrMax = 0;
        }
        if (!"".equals(prtTitle3)) {
            col1StrMax = 12;
            col2StrMax = 12;
            col3StrMax = 15;
            row1StrMax = 25;
            row2StrMax = 25;
            row3StrMax = 25;
        }
        prtSubTitle(svf, "2", fieldIdx, prtTitle1, prtTitle2, prtTitle3);

        int grp1 = 1;
        int grp2 = 1;
        final Map printSubclass = new HashMap();
        //科目
        if (student._blk8 != null) {
            for (Iterator itRemark = student._blk8._subclassMap.keySet().iterator(); itRemark.hasNext();) {
                final String subclassCd = (String) itRemark.next();
                final SubclsInfo subclass = (SubclsInfo) student._blk8._subclassMap.get(subclassCd);
                final int subclassLen = subclass._isUnit ? 2 : 10;
                final List setSubclassList = KNJ_EditKinsoku.getTokenList(subclass._subclassName, subclassLen);

                //学期
                final Map unitMap = (Map) subclass._semeUnitMap.get(_param._semester);

                if (null != unitMap) {
                    //単元
                    int putPtr = 0;
                    int tngnCnt = 1;
                    for (Iterator itUnit = unitMap.keySet().iterator(); itUnit.hasNext();) {
                        final String unitCd = (String) itUnit.next();
                        final UnitData unitData = (UnitData) unitMap.get(unitCd);

                        //単元名
                        ////登録データについて
                        ////帳票パターンによって変化する。
                        ////                                          1番目(101)           2番目(102)         3番目(103)
                        //// 出力が1項目(パターン101)なら、remark3 -> key="3"
                        //// 出力が2項目(パターン102)なら、remark1 -> key="1" 、remark3 -> key="3"
                        //// 出力が3項目(パターン103)なら、remark1 -> key="1" 、remark2 -> key="2" remark3 -> key="3"
                        final List setUnitList = KNJ_EditKinsoku.getTokenList(unitData._unitName, 1*2, 10);
                        //登録データ1(参照するデータは帳票パターンによって変わるが、それを "参照キーを切り替えて制御している"。後の出力フィールドでは変更していないので、注意。)
                        final String getkey1 = PT_PATTERN_1.equals(_param._paraFrmPatern) ? "3" : "1";
                        final String remark1;
                        final List setRemarkList1;
                        if (unitData._unitSeqMap.containsKey(getkey1)) {
                            remark1 = (String) unitData._unitSeqMap.get(getkey1);
                            setRemarkList1 = KNJ_EditKinsoku.getTokenList(remark1, col1StrMax*2, row1StrMax);
                        } else {
                            remark1 = "";
                            setRemarkList1 = new ArrayList();
                        }
                        //登録データ2(参照するデータは帳票パターンによって変わるが、それを "参照キーを切り替えて制御している"。後の出力フィールドでは変更していないので、注意。)
                        final String getkey2 = PT_PATTERN_2.equals(_param._paraFrmPatern) ? "3" : (PT_PATTERN_3A.equals(_param._paraFrmPatern) || PT_PATTERN_3B.equals(_param._paraFrmPatern))  ? "2" : "";
                        final String remark2;
                        final List setRemarkList2;
                        if (unitData._unitSeqMap.containsKey(getkey2)) {
                            remark2 = (String) unitData._unitSeqMap.get(getkey2);
                            setRemarkList2 = KNJ_EditKinsoku.getTokenList(remark2, col2StrMax*2, row2StrMax);
                        } else {
                            remark2 = "";
                            setRemarkList2 = new ArrayList();
                        }
                        //登録データ3(参照するデータは帳票パターンによって変わるが、それを "参照キーを切り替えて制御している"。後の出力フィールドでは変更していないので、注意。)
                        final String getkey3 = (PT_PATTERN_3A.equals(_param._paraFrmPatern) || PT_PATTERN_3B.equals(_param._paraFrmPatern)) ? "3" : "";
                        final String remark3;
                        final List setRemarkList3;
                        if (unitData._unitSeqMap.containsKey(getkey3)) {
                            remark3 = (String) unitData._unitSeqMap.get(getkey3);
                            setRemarkList3 = KNJ_EditKinsoku.getTokenList(remark3, col3StrMax*2, row3StrMax);
                        } else {
                            remark3 = "";
                            setRemarkList3 = new ArrayList();
                        }

                        int maxLine = setRemarkList1.size();
                        if (setRemarkList2.size() > maxLine) {
                            maxLine = setRemarkList2.size();
                        }
                        if (setRemarkList3.size() > maxLine) {
                            maxLine = setRemarkList3.size();
                        }
                        //科目名を縦に文字を出力する事、及び複数行に渡って出力する事から、科目名の出力制御を行う。
                        ////1.最終行の出力をするまでは、帳票で言う所の単元より右のMAXに従って出力。
                        ////2.最終行の出力をする際に、出力残とmaxLineとの差分を比較して、最大出力行を決める。
                        if (unitMap.size() == tngnCnt) {
                            if (setSubclassList.size() - putPtr > maxLine) {
                                maxLine = setSubclassList.size() - putPtr;
                            }
                        }
                        if (setUnitList.size() > maxLine) {
                            maxLine = setUnitList.size();
                        }

                        if (evalCnt + maxLine > lineMaxCnt) {
                            svf.VrEndRecord();
                            svf.VrEndPage();
                            setForm(svf, _param._seisekiFrm, 4, "seiseki");
                            printSeisekiTitle(svf, student);
                            prtSubTitle(svf, "2", fieldIdx, prtTitle1, prtTitle2, prtTitle3);
                            evalCnt = 1;
                        }
                        final int subFIdx = fieldIdx + (setUnitList.size() > 0 ? -1 : 0);
                        for (int i = 0; i < maxLine; i++) {
                            if (!printSubclass.containsKey(subclassCd) && putPtr < setSubclassList.size()) {
                                //科目名を出力するが、複数行に渡って出力位置を制御するため、変数iには依存しない制御となっている。
                                svf.VrsOut("CLASS_NAME" + subFIdx, (String) setSubclassList.get(putPtr++));
                            }
                            if (i < setUnitList.size()) {
                                svf.VrsOut("UNIT" + subFIdx, (String) setUnitList.get(i));
                            }
                            if (0 < setUnitList.size()) {
                                svf.VrsOut("GRP" + subFIdx + "_2", String.valueOf(grp2));
                            }
                            svf.VrsOut("GRP" + subFIdx + "_1", String.valueOf(grp1));
                            svf.VrsOut("GRP" + subFIdx + "_3", String.valueOf(grp2));
                            svf.VrsOut("GRP" + subFIdx + "_4", String.valueOf(grp2));
                            svf.VrsOut("GRP" + subFIdx + "_5", String.valueOf(grp2));
                            if (i < setRemarkList1.size()) {
                                svf.VrsOut("CONTENT" + subFIdx + "_1", (String) setRemarkList1.get(i));
                            }
                            if (i < setRemarkList2.size()) {
                                svf.VrsOut("CONTENT" + subFIdx + "_2", (String) setRemarkList2.get(i));
                            }
                            if (i < setRemarkList3.size()) {
                                svf.VrsOut("CONTENT" + subFIdx + "_3", (String) setRemarkList3.get(i));
                            }
                            svf.VrEndRecord();
                            evalCnt++;
                        }
                        printSubclass.put(subclassCd, subclassCd);
                        grp2++;
                        tngnCnt++;
                    }
                }
                grp1++;
            }
        }
    }

    private void prtSubTitle(final Vrw32alp svf, final String subTitlePattern, final int fieldIdx, final String prtTitle1, final String prtTitle2, final String prtTitle3) {
        if ("1".equals(subTitlePattern)) {
            svf.VrsOut("SUBTITLE1", prtTitle1);
        } else if ("2".equals(subTitlePattern)) {
            svf.VrsOut("CLASS_NAME"+fieldIdx, "教科等");
            svf.VrsOut("GRP"+fieldIdx+"_1", "0000");
            String subIdxStr = "";
            if (!"".equals(prtTitle1)) {
                if (!"".equals(prtTitle3) && KNJ_EditEdit.getMS932ByteLength(prtTitle1) > 24) {
                    subIdxStr = "_2";
                }
                svf.VrsOut("CONTENT"+fieldIdx+"_1" + subIdxStr, prtTitle1);
                svf.VrAttribute("CONTENT"+fieldIdx+"_1" + subIdxStr, ATTR_CENTERING);
                svf.VrsOut("GRP"+fieldIdx+"_3", "0000");
            }
            if (!"".equals(prtTitle2)) {
                if (!"".equals(prtTitle3) && KNJ_EditEdit.getMS932ByteLength(prtTitle1) > 24) {
                    subIdxStr = "_2";
                }
                svf.VrsOut("CONTENT"+fieldIdx+"_2" + subIdxStr, prtTitle2);
                svf.VrAttribute("CONTENT"+fieldIdx+"_2" + subIdxStr, ATTR_CENTERING);
                svf.VrsOut("GRP"+fieldIdx+"_4", "0000");
            }
            if (!"".equals(prtTitle3)) {
                svf.VrsOut("CONTENT"+fieldIdx+"_3", prtTitle3);
                svf.VrAttribute("CONTENT"+fieldIdx+"_3", ATTR_CENTERING);
                svf.VrsOut("GRP"+fieldIdx+"_5", "0000");
            }
            svf.VrEndRecord();
        }
    }

    private int cntUsefulRow(final String[] prtDatas, final int lCnt) {
        if (prtDatas == null) return 0;
        int cnt;
        for (cnt = lCnt;cnt > 0;cnt--) {
            if (prtDatas[cnt - 1] != null) break;
        }
        return cnt;
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
                final String schregNo = rs.getString("SCHREGNO");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String gradeName1 = rs.getString("GRADE_NAME1");
                final String gradeName2 = rs.getString("GRADE_NAME2");
                final String gradeName3 = rs.getString("GRADE_NAME3");
                final String gradeCd = rs.getString("GRADE_CD");
                final String ghrCd = rs.getString("GHR_CD");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String gakubuName = rs.getString("GAKUBU_NAME");
                final String hrName = rs.getString("HR_NAME");
                final String hrClassName2 = rs.getString("HR_CLASS_NAME2");
                final String courseCd = rs.getString("COURSECD");
                final String courseName = rs.getString("COURSENAME");
                final String majorCd = rs.getString("MAJORCD");
                final String courseCode = rs.getString("COURSECODE");
                final String tannin1 = rs.getString("TANNIN1");
                final String tannin2 = rs.getString("TANNIN2");
                final String tannin3 = rs.getString("TANNIN3");
                final String tannin4 = rs.getString("TANNIN4");
                final String tannin5 = rs.getString("TANNIN5");
                final String tannin6 = rs.getString("TANNIN6");
                final String ghrName = rs.getString("GHR_NAME");
                final String ghrNameAbbv = rs.getString("GHR_NAMEABBV");
                final String ghTannin1 = rs.getString("GHTANNIN1");
                final String ghTannin2 = rs.getString("GHTANNIN2");
                final String ghTannin3 = rs.getString("GHTANNIN3");
                final String ghTannin4 = rs.getString("GHTANNIN4");
                final String ghTannin5 = rs.getString("GHTANNIN5");
                final String ghTannin6 = rs.getString("GHTANNIN6");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String birthDay = rs.getString("BIRTHDAY");
                final String sexName = rs.getString("SEX_NAME");

                final Student student = new Student(schregNo, schoolKind, gradeName1, gradeName2, gradeName3, gradeCd, ghrCd, grade, hrClass, gakubuName, hrName, hrClassName2, courseCd, courseName, majorCd, courseCode, tannin1, tannin2, tannin3, tannin4, tannin5, tannin6,
                                                     ghrName, ghrNameAbbv, ghTannin1, ghTannin2, ghTannin3, ghTannin4, ghTannin5, ghTannin6, name, nameKana, birthDay, sexName);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        try {
            for (Iterator it = retList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();
                student.setHreportRemarkDat(db2);
                student.setBehaviorSeme(db2);
                student.setTokusyuKamoku(db2);
                student.setBlock8(db2);
                student.setHreportSchRemDat(db2);
            }
        } catch (Exception ex) {
            log.error("Exception:", ex);
        } finally {
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     GDAT.SCHOOL_KIND, ");
        stb.append("     GDAT.GRADE_NAME1, ");
        stb.append("     GDAT.GRADE_NAME2, ");
        stb.append("     GDAT.GRADE_NAME3, ");
        stb.append("     GDAT.GRADE_CD, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGDH.HR_CLASS_NAME2, ");
        stb.append("     REGD.COURSECD, ");
        stb.append("     REGD.MAJORCD, ");
        stb.append("     REGD.COURSECODE, ");
        stb.append("     TANNIN1.STAFFNAME AS TANNIN1, ");
        stb.append("     TANNIN2.STAFFNAME AS TANNIN2, ");
        stb.append("     TANNIN3.STAFFNAME AS TANNIN3, ");
        stb.append("     TANNIN4.STAFFNAME AS TANNIN4, ");
        stb.append("     TANNIN5.STAFFNAME AS TANNIN5, ");
        stb.append("     TANNIN6.STAFFNAME AS TANNIN6, ");
        stb.append("     GHR.GHR_CD, ");
        stb.append("     GHRH.GHR_NAME, ");
        stb.append("     GHRH.GHR_NAMEABBV, ");
        stb.append("     GHTANNIN1.STAFFNAME AS GHTANNIN1, ");
        stb.append("     GHTANNIN2.STAFFNAME AS GHTANNIN2, ");
        stb.append("     GHTANNIN3.STAFFNAME AS GHTANNIN3, ");
        stb.append("     GHTANNIN4.STAFFNAME AS GHTANNIN4, ");
        stb.append("     GHTANNIN5.STAFFNAME AS GHTANNIN5, ");
        stb.append("     GHTANNIN6.STAFFNAME AS GHTANNIN6, ");
        stb.append("     A023.ABBV1 AS GAKUBU_NAME, ");
        stb.append("     CM.COURSENAME, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     Z002.NAME2 AS SEX_NAME ");
        stb.append(" FROM ");
        stb.append("     V_SCHREG_BASE_MST BASE ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
        stb.append("                                  AND REGD.GRADE = GDAT.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("                                  AND REGD.SEMESTER = REGDH.SEMESTER  ");
        stb.append("                                  AND REGD.GRADE || REGD.HR_CLASS = REGDH.GRADE || REGDH.HR_CLASS  ");
        stb.append("     LEFT JOIN STAFF_MST TANNIN1 ON TANNIN1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST TANNIN2 ON TANNIN2.STAFFCD = REGDH.TR_CD2 ");
        stb.append("     LEFT JOIN STAFF_MST TANNIN3 ON TANNIN3.STAFFCD = REGDH.TR_CD3 ");
        stb.append("     LEFT JOIN STAFF_MST TANNIN4 ON TANNIN4.STAFFCD = REGDH.SUBTR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST TANNIN5 ON TANNIN5.STAFFCD = REGDH.SUBTR_CD2 ");
        stb.append("     LEFT JOIN STAFF_MST TANNIN6 ON TANNIN6.STAFFCD = REGDH.SUBTR_CD3 ");
        stb.append("     LEFT JOIN SCHREG_REGD_GHR_DAT GHR ON GHR.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                                     AND GHR.YEAR =REGD.YEAR ");
        stb.append("                                     AND GHR.SEMESTER = REGD.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GHR_HDAT GHRH ON GHRH.YEAR = GHR.YEAR ");
        stb.append("                                      AND GHRH.SEMESTER = GHR.SEMESTER ");
        stb.append("                                      AND GHRH.GHR_CD = GHR.GHR_CD ");
        stb.append("     LEFT JOIN STAFF_MST GHTANNIN1 ON GHTANNIN1.STAFFCD = GHRH.TR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST GHTANNIN2 ON GHTANNIN2.STAFFCD = GHRH.TR_CD2 ");
        stb.append("     LEFT JOIN STAFF_MST GHTANNIN3 ON GHTANNIN3.STAFFCD = GHRH.TR_CD3 ");
        stb.append("     LEFT JOIN STAFF_MST GHTANNIN4 ON GHTANNIN4.STAFFCD = GHRH.SUBTR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST GHTANNIN5 ON GHTANNIN5.STAFFCD = GHRH.SUBTR_CD2 ");
        stb.append("     LEFT JOIN STAFF_MST GHTANNIN6 ON GHTANNIN6.STAFFCD = GHRH.SUBTR_CD3 ");
        stb.append("     LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ");
        stb.append("                            AND A023.NAME1 = GDAT.SCHOOL_KIND ");
        stb.append("     LEFT JOIN COURSE_MST CM ON CM.COURSECD = REGD.COURSECD ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("                            AND BASE.SEX = Z002.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     BASE.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("     AND REGD.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append(" ORDER BY ");
        stb.append("     GHR.GHR_CD, ");
        stb.append("     GHR.GHR_ATTENDNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        return stb.toString();
    }

    private class Student {
        final String _schregNo;
        final String _schoolKind;
        final String _gradeName1;
        final String _gradeName2;
        final String _gradeName3;
        final String _gradeCd;
        final String _ghrCd;
        final String _grade;
        final String _hrClass;
        final String _gakubuName;
        final String _hrName;
        final String _hrClassName2;
        final String _courseCd;
        final String _courseName;
        final String _majorCd;
        final String _courseCode;
        final String _tannin1;
        final String _tannin2;
        final String _tannin3;
        final String _tannin4;
        final String _tannin5;
        final String _tannin6;
        final String _ghrName;
        final String _ghrNameAbbv;
        final String _ghTannin1;
        final String _ghTannin2;
        final String _ghTannin3;
        final String _ghTannin4;
        final String _ghTannin5;
        final String _ghTannin6;
        final String _name;
        final String _nameKana;
        final String _birthDay;
        final String _sexName;

        Block8 _blk8;
        final Map _attendMap = new TreeMap();
        final Map _behaviorConvGradeMap;
        final Map _behaviorSemeMap;
        final Map _attendSubClassMap = new TreeMap();
        final Map _hreportRemarkDatMap = new TreeMap();
        String _tokkatsuSubClassCd;
        String _jikatsuSubClassCd;
        final Map _targetDatMap = new LinkedMap();

        public Student(final String schregNo, final String schoolKind, final String gradeName1, final String gradeName2, final String gradeName3, final String gradeCd, final String ghrCd, final String grade,
                final String hrClass, final String gakubuName, final String hrName, final String hrClassName2, final String courseCd, final String courseName, final String majorCd, final String courseCode,
                final String tannin1, final String tannin2, final String tannin3, final String tannin4, final String tannin5, final String tannin6, final String ghrName, final String ghrNameAbbv,
                final String ghTannin1, final String ghTannin2, final String ghTannin3, final String ghTannin4, final String ghTannin5, final String ghTannin6,
                final String name, final String nameKana, final String birthDay, final String sexName) {
            _schregNo = schregNo;
            _schoolKind = schoolKind;
            _gradeName1 = gradeName1;
            _gradeName2 = gradeName2;
            _gradeName3 = gradeName3;
            _gradeCd = gradeCd;
            _ghrCd = StringUtils.isEmpty(ghrCd) ? "00" : ghrCd;
            _grade = grade;
            _hrClass = hrClass;
            _gakubuName = gakubuName;
            _hrName = hrName;
            _hrClassName2 = hrClassName2;
            _courseCd = courseCd;
            _courseName = courseName;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _tannin1 = StringUtils.defaultString(tannin1);
            _tannin2 = StringUtils.defaultString(tannin2);
            _tannin3 = StringUtils.defaultString(tannin3);
            _tannin4 = StringUtils.defaultString(tannin4);
            _tannin5 = StringUtils.defaultString(tannin5);
            _tannin6 = StringUtils.defaultString(tannin6);
            _ghrName = ghrName;
            _ghrNameAbbv = ghrNameAbbv;
            _ghTannin1 = StringUtils.defaultString(ghTannin1);
            _ghTannin2 = StringUtils.defaultString(ghTannin2);
            _ghTannin3 = StringUtils.defaultString(ghTannin3);
            _ghTannin4 = StringUtils.defaultString(ghTannin4);
            _ghTannin5 = StringUtils.defaultString(ghTannin5);
            _ghTannin6 = StringUtils.defaultString(ghTannin6);
            _name = name;
            _nameKana = nameKana;
            _birthDay = birthDay;
            _sexName = sexName;
            _blk8 = null;
            _behaviorSemeMap = new TreeMap();
            _behaviorConvGradeMap = new LinkedMap();
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
                    final String communication = rs.getString("COMMUNICATION");
                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final String remark3 = rs.getString("REMARK3");
                    final String foreignlangact = rs.getString("FOREIGNLANGACT");
                    final String attendrecRemark = rs.getString("ATTENDREC_REMARK");
                    final String hrAct = rs.getString("HR_ACT");
                    final String studentAct = rs.getString("STUDENTACT");
                    final String clubOther = rs.getString("CLUBOTHER");
                    final String training1 = rs.getString("TRAINING1");
                    final String trSyoken1 = rs.getString("TR_SYOKEN1");
                    final String trSyaFlg1 = rs.getString("TR_SYAFLG1");
                    final String training2 = rs.getString("TRAINING2");
                    final String trSyoken2 = rs.getString("TR_SYOKEN2");
                    final String trSyaFlg2 = rs.getString("TR_SYAFLG2");
                    final HreportRemarkDat hreportRemarkDat = new HreportRemarkDat(totalstudytime, specialactremark, communication, remark1, remark2, remark3, foreignlangact
                                                                                       , attendrecRemark, hrAct, studentAct, clubOther, training1, trSyoken1, trSyaFlg1
                                                                                       , training2, trSyoken2, trSyaFlg2);
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
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.FOREIGNLANGACT, ");
            stb.append("     T1.TOTALSTUDYTIME, ");
            stb.append("     T1.SPECIALACTREMARK, ");
            stb.append("     T1.COMMUNICATION, ");
            stb.append("     T1.REMARK1, ");
            stb.append("     T1.REMARK2, ");
            stb.append("     T1.REMARK3, ");
            stb.append("     T2_1.REMARK1 AS HR_ACT, ");      //MORAL->HR_ACT
            stb.append("     T2_2.REMARK1 AS STUDENTACT, ");  //OWNACT->STUDENTACT
            stb.append("     T2_3.REMARK1 AS CLUBOTHER, ");
            stb.append("     T3_1.REMARK1 AS TRAINING1, ");
            stb.append("     T3_1.REMARK2 AS TR_SYOKEN1, ");
            stb.append("     T3_1.REMARK3 AS TR_SYAFLG1, ");
            stb.append("     T3_2.REMARK1 AS TRAINING2, ");
            stb.append("     T3_2.REMARK2 AS TR_SYOKEN2, ");
            stb.append("     T3_2.REMARK3 AS TR_SYAFLG2, ");
            stb.append("     T1.ATTENDREC_REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT T2_1 ");
            stb.append("       ON T2_1.YEAR = T1.YEAR ");
            stb.append("      AND T2_1.SCHREGNO = T1.SCHREGNO ");
            stb.append("      AND T2_1.SEMESTER = T1.SEMESTER ");
            stb.append("      AND T2_1.DIV = '01' ");
            stb.append("      AND T2_1.CODE = '01' ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT T2_2 ");
            stb.append("       ON T2_2.YEAR = T1.YEAR ");
            stb.append("      AND T2_2.SCHREGNO = T1.SCHREGNO ");
            stb.append("      AND T2_2.SEMESTER = T1.SEMESTER ");
            stb.append("      AND T2_2.DIV = '01' ");
            stb.append("      AND T2_2.CODE = '02' ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT T2_3 ");
            stb.append("       ON T2_3.YEAR = T1.YEAR ");
            stb.append("      AND T2_3.SCHREGNO = T1.SCHREGNO ");
            stb.append("      AND T2_3.SEMESTER = T1.SEMESTER ");
            stb.append("      AND T2_3.DIV = '01' ");
            stb.append("      AND T2_3.CODE = '03' ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT T3_1 ");
            stb.append("       ON T3_1.YEAR = T1.YEAR ");
            stb.append("      AND T3_1.SCHREGNO = T1.SCHREGNO ");
            stb.append("      AND T3_1.SEMESTER = T1.SEMESTER ");
            stb.append("      AND T3_1.DIV = '02' ");
            stb.append("      AND T3_1.CODE = '01' ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT T3_2 ");
            stb.append("       ON T3_2.YEAR = T1.YEAR ");
            stb.append("      AND T3_2.SCHREGNO = T1.SCHREGNO ");
            stb.append("      AND T3_2.SEMESTER = T1.SEMESTER ");
            stb.append("      AND T3_2.DIV = '02' ");
            stb.append("      AND T3_2.CODE = '02' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SEMESTER ");

            return stb.toString();
        }

        private List getGradeList(final DB2UDB db2) {
            final String useSemester = "9".equals(_param._semester) ? _param._ctrlSemester : _param._semester;
            List retList = new ArrayList();
            final String sqlGetGrade = " SELECT DISTINCT GRADE FROM SCHREG_REGD_DAT WHERE YEAR = '" + _param._ctrlYear + "' AND SEMESTER = '" + useSemester + "' AND SCHREGNO = '" + _schregNo + "' ";
            for (final Iterator it = KnjDbUtils.query(db2, sqlGetGrade).iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                retList.add((String)m.get("GRADE"));
            }
            return retList;
        }
        private List getConvBehaviorGrade(final DB2UDB db2, final Map retMap) {
            List gradeList = getGradeList(db2);
            List sammaryList = new ArrayList();
            for (Iterator ite = gradeList.iterator();ite.hasNext();) {
                final String grade = (String)ite.next();
                final String sql = " SELECT COUNT(*) FROM HREPORT_BEHAVIOR_L_MST WLMST WHERE YEAR = '" + _param._ctrlYear + "' AND SCHOOL_KIND = '" + _param._schoolKind + "' AND GRADE = '" + grade + "' ";
                final String chkStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
                if (!"".equals(StringUtils.defaultString(chkStr, "")) && Integer.parseInt(chkStr) > 0) {
                    retMap.put(grade, grade);
                    if (!sammaryList.contains(grade)) {
                        sammaryList.add(grade);
                    }
                } else {
                    retMap.put(grade, "00");
                    if (!sammaryList.contains("00")) {
                        sammaryList.add("00");
                    }
                }
            }
            return sammaryList;
        }
        private void setBehaviorSeme(final DB2UDB db2) {
            List gradeList = getConvBehaviorGrade(db2, _behaviorConvGradeMap);
            final String behaviorSemeSql = getBehaviorSemeSql(gradeList);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(behaviorSemeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String l_Cd = rs.getString("L_CD");
                    final String l_Name = rs.getString("L_NAME");
                    final String m_Cd = rs.getString("M_CD");
                    final String m_Name = rs.getString("M_NAME");
                    final String semester = rs.getString("SEMESTER");
                    final String record = rs.getString("RECORD");
                    final String fstKey = grade + "-" + l_Cd + m_Cd;
                    HrBehaviorInfo addwk;
                    if (_behaviorSemeMap.containsKey(fstKey)) {
                        addwk =(HrBehaviorInfo)_behaviorSemeMap.get(fstKey);
                    } else {
                        addwk = new HrBehaviorInfo(grade, l_Cd, l_Name, m_Cd, m_Name);
                        _behaviorSemeMap.put(grade + "-" + l_Cd + m_Cd, addwk);
                    }
                    if (!"".equals(StringUtils.defaultString(semester, "")) && !"".equals(StringUtils.defaultString(record, ""))) {
                        addwk._behaviorDat.put(semester, record);
                    }
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getBehaviorSemeSql(List gradeList) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     LMST.GRADE, ");
            stb.append("     LMST.L_CD, ");
            stb.append("     LMST.L_NAME, ");
            stb.append("     MMST.M_CD, ");
            stb.append("     MMST.M_NAME, ");
            stb.append("     T3.SEMESTER, ");
            stb.append("     T3.RECORD ");
            stb.append(" FROM ");
            stb.append("     HREPORT_BEHAVIOR_L_MST LMST ");
            stb.append("     LEFT JOIN HREPORT_BEHAVIOR_M_MST MMST ");
            stb.append("       ON LMST.YEAR = MMST.YEAR ");
            stb.append("      AND LMST.SCHOOL_KIND = MMST.SCHOOL_KIND ");
            stb.append("      AND LMST.GRADE       = MMST.GRADE ");
            stb.append("      AND LMST.L_CD        = MMST.L_CD ");
            stb.append("     LEFT JOIN HREPORT_BEHAVIOR_LM_DAT T3 ");
            stb.append("       ON T3.YEAR = MMST.YEAR ");
            stb.append("      AND T3.SCHREGNO = '" + _schregNo + "' ");
            stb.append("      AND T3.L_CD = MMST.L_CD ");
            stb.append("      AND T3.M_CD = MMST.M_CD ");
            stb.append(" WHERE ");
            stb.append("         LMST.YEAR        = '" + _param._ctrlYear + "' ");
            stb.append("     AND LMST.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("     AND LMST.GRADE       IN " + SQLUtils.whereIn(false, (String[])gradeList.toArray(new String[gradeList.size()])));
            stb.append(" ORDER BY ");
            stb.append("     LMST.L_CD, ");
            stb.append("     MMST.M_CD, ");
            stb.append("     T3.SEMESTER ");

            return stb.toString();
        }

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
                    if ("TOKKATSU".equals(divname)) {
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
            stb.append("     AND CHAIR_STD.SCHREGNO = '" + _schregNo + "' ");
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
            stb.append("     AND CHAIR_STD.SCHREGNO = '" + _schregNo + "' ");
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
            stb.append("     AND CHAIR_STD.SCHREGNO = '" + _schregNo + "' ");
            stb.append("     AND CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD ");
            stb.append("         IN(SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _param._ctrlYear + "' AND NAMECD1 = 'E065') ");

            return stb.toString();
        }

        private void setBlock8(final DB2UDB db2) {
            Block8 block8 = null;
            final String aPaternSql = getBlock8PaternSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(aPaternSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String patern = rs.getString("PATERN");
                    final String useSemes = rs.getString("USE_SEMES");
                    final String guidancePattern = rs.getString("GUIDANCE_PATTERN");
                    final String gakubuSchoolKind = rs.getString("GAKUBU_SCHOOL_KIND");
                    final String ghrCd = rs.getString("GHR_CD");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String condition = rs.getString("CONDITION");
                    final String groupCd = rs.getString("GROUPCD");
                    block8 = new Block8(patern, useSemes, guidancePattern, gakubuSchoolKind, ghrCd, grade, hrClass, condition, groupCd, _schoolKind);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (null != block8) {
                block8.setSubclassMap(db2, _schregNo);
            }
            _blk8 = block8;
        }

        private String getBlock8PaternSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     SUBSTR(A035.NAMESPARE1, 2, 1) AS PATERN, ");
            stb.append("     A035.NAMESPARE2 AS USE_SEMES, ");
            stb.append("     L1.GUIDANCE_PATTERN, ");
            stb.append("     L1.GAKUBU_SCHOOL_KIND, ");
            stb.append("     L1.GHR_CD, ");
            stb.append("     L1.GRADE, ");
            stb.append("     L1.HR_CLASS, ");
            stb.append("     L1.CONDITION, ");
            stb.append("     T1.GROUPCD ");
            stb.append(" FROM ");
            stb.append("     GRADE_KIND_SCHREG_GROUP_DAT T1 ");
            stb.append("     INNER JOIN GRADE_KIND_COMP_GROUP_YMST L1 ON L1.YEAR = T1.YEAR ");
            stb.append("          AND T1.SEMESTER    = L1.SEMESTER ");
            stb.append("          AND T1.GAKUBU_SCHOOL_KIND = L1.GAKUBU_SCHOOL_KIND ");
            stb.append("          AND T1.GHR_CD      = L1.GHR_CD ");
            stb.append("          AND T1.GRADE       = L1.GRADE ");
            stb.append("          AND T1.HR_CLASS    = L1.HR_CLASS ");
            stb.append("          AND T1.CONDITION   = L1.CONDITION ");
            stb.append("          AND T1.GROUPCD     = L1.GROUPCD ");
            stb.append("     LEFT JOIN NAME_MST A035 ON A035.NAMECD1 = 'A035' ");
            stb.append("           AND L1.GUIDANCE_PATTERN = A035.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '9' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");

            return stb.toString();
        }
        private void setHreportSchRemDat(final DB2UDB db2) {
            final String aPaternSql = getHreportSchRemDatSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(aPaternSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String div = rs.getString("DIV");
                    final String goals = rs.getString("GOALS");
                    GoalsInfo gInfo = new GoalsInfo(div, goals);
                    _targetDatMap.put(div, gInfo);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getHreportSchRemDatSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     DIV,GOALS ");
            stb.append(" FROM ");
            stb.append("     HREPORT_SCHREG_REMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SEMESTER = '9' ");
            stb.append("     AND SCHREGNO = '" + _schregNo + "' ");

            return stb.toString();
        }
    }

    private class GoalsInfo {
        final String _div;
        final String _goals;
        GoalsInfo(final String div, final String goals) {
            _div = div;
            _goals = goals;
        }
    }

    private class HrBehaviorInfo {
        final String _grade;
        final String _l_Cd;
        final String _l_Name;
        final String _m_Cd;
        final String _m_Name;
        final Map _behaviorDat;
        public HrBehaviorInfo (final String grade, final String l_Cd, final String l_Name, final String m_Cd, final String m_Name)
        {
            _grade = grade;
            _l_Cd = l_Cd;
            _l_Name = l_Name;
            _m_Cd = m_Cd;
            _m_Name = m_Name;
            _behaviorDat = new LinkedMap();
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
            final String movedate = param._moveDate.replace('/', '-');
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
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);
                psAtSeme = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    DbUtils.closeQuietly(rsAtSeme);

                    psAtSeme.setString(1, student._schregNo);
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

                    ps.setString(1, student._schregNo);
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
        final String _communication;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _foreignlangact;
        final String _attendrecRemark;
        final String _hrAct;
        final String _studentAct;
        final String _clubOther;
        final String _training1;
        final String _trSyoken1;
        final String _trSyaFlg1;
        final String _training2;
        final String _trSyoken2;
        final String _trSyaFlg2;

        public HreportRemarkDat(
                final String totalstudytime,
                final String specialactremark,
                final String communication,
                final String remark1,
                final String remark2,
                final String remark3,
                final String foreignlangact,
                final String attendrecRemark,
                final String hrAct,
                final String studentAct,
                final String clubOther,
                final String training1,
                final String trSyoken1,
                final String trSyaFlg1,
                final String training2,
                final String trSyoken2,
                final String trSyaFlg2
        ) {
            _totalstudytime = totalstudytime;
            _specialactremark = specialactremark;
            _communication = communication;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _foreignlangact = foreignlangact;
            _attendrecRemark = attendrecRemark;
            _hrAct = hrAct;
            _studentAct = studentAct;
            _clubOther = clubOther;
            _training1 = training1;
            _trSyoken1 = trSyoken1;
            _trSyaFlg1 = trSyaFlg1;
            _training2 = training2;
            _trSyoken2 = trSyoken2;
            _trSyaFlg2 = trSyaFlg2;
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

    private class TargetInfo {
        final String _kind_Name;
        final String _kind_Seq;
        final String _kind_Remark;
        public TargetInfo (final String kind_Seq, final String kind_Name, final String kind_Remark)
        {
            _kind_Seq = kind_Seq;
            _kind_Name = kind_Name;
            _kind_Remark = kind_Remark;
        }
    }

    //_subclassMapだけ利用しているが、KNJD429との互換を維持するため、クラス構造を残している。
    private class Block8 {
        final String _patern;
        final String _useSemes;
        final String _guidancePattern;
        final String _gakubuSchoolKind;
        final String _ghrCd;
        final String _grade;
        final String _hrClass;
        final String _condition;
        final String _groupCd;
        Map _subclassMap;

        public Block8(final String patern, final String useSemes, final String guidancePattern,
                final String gakubuSchoolKind, final String ghrCd, final String grade, final String hrClass,
                final String condition, final String groupCd, final String schoolKind) {
            _patern = patern;
            _useSemes = useSemes;
            _guidancePattern = guidancePattern;
            _gakubuSchoolKind = gakubuSchoolKind;
            _ghrCd = ghrCd;
            _grade = grade;
            _hrClass = hrClass;
            _condition = condition;
            _groupCd = groupCd;
            _subclassMap = new LinkedMap();
        }

        public void setSubclassMap(final DB2UDB db2, final String schregNo) {
            final String subclassSql = getBlock8SubclassSql(schregNo);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String unitCd = rs.getString("UNITCD");
                    final String unitName = rs.getString("UNITNAME");
                    final String seq = rs.getString("SEQ");
                    final String remark = rs.getString("REMARK");
                    final SubclsInfo subclass;
                    final String key = classCd + schoolKind + curriculumCd + subclassCd;
                    if (_subclassMap.containsKey(key)) {
                        subclass = (SubclsInfo) _subclassMap.get(key);
                    } else {
                        subclass = new SubclsInfo(classCd, schoolKind, curriculumCd, subclassCd, subclassName, unitCd);
                    }
                    subclass.setSemeUnitMap(semester, unitCd, unitName, seq, remark);
                    _subclassMap.put(key, subclass);
                }
            } catch (SQLException ex) {
                log.error("setSubclassMap exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getBlock8SubclassSql(final String schregNo) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("    SCH_SUB.CLASSCD, ");
            stb.append("    SCH_SUB.SCHOOL_KIND, ");
            stb.append("    SCH_SUB.CURRICULUM_CD, ");
            stb.append("    SCH_SUB.SUBCLASSCD, ");
            stb.append("    SCH_SUB.SEMESTER, ");
            stb.append("    SCH_SUB.UNITCD, ");
            stb.append("    UCM.UNITNAME, ");
            stb.append("    SCH_SUB.SEQ, ");
            stb.append("    SCH_SUB.REMARK, ");
            stb.append("    V_SUB.SUBCLASSNAME AS SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN HREPORT_SCHREG_SUBCLASS_REMARK_DAT SCH_SUB ");
            stb.append("       ON SCH_SUB.YEAR = T1.YEAR ");
            stb.append("      AND SCH_SUB.SEMESTER = T1.SEMESTER ");
            stb.append("      AND SCH_SUB.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN V_SUBCLASS_MST V_SUB ");
            stb.append("         ON V_SUB.YEAR = SCH_SUB.YEAR ");
            stb.append("         AND V_SUB.CLASSCD = SCH_SUB.CLASSCD ");
            stb.append("         AND V_SUB.SCHOOL_KIND = SCH_SUB.SCHOOL_KIND ");
            stb.append("         AND V_SUB.CURRICULUM_CD = SCH_SUB.CURRICULUM_CD ");
            stb.append("         AND V_SUB.SUBCLASSCD = SCH_SUB.SUBCLASSCD ");
            stb.append("     LEFT JOIN GRADE_KIND_SCHREG_GROUP_DAT GSG ");
            stb.append("         ON GSG.YEAR = T1.YEAR ");
            stb.append("        AND GSG.SEMESTER = '9' ");
            stb.append("        AND GSG.GAKUBU_SCHOOL_KIND = SCH_SUB.SCHOOL_KIND ");
            stb.append("        AND GSG.GHR_CD = '00' ");
            stb.append("        AND GSG.GRADE || GSG.HR_CLASS = '00000' ");
            stb.append("        AND GSG.SCHREGNO = T1.SCHREGNO ");
            stb.append("        AND GSG.CONDITION = '1' ");
            stb.append("     LEFT JOIN GRADE_KIND_UNIT_GROUP_YMST UCM ");
            stb.append("         ON UCM.YEAR = GSG.YEAR ");
            stb.append("        AND UCM.SEMESTER = GSG.SEMESTER ");
            stb.append("        AND UCM.GAKUBU_SCHOOL_KIND = GSG.GAKUBU_SCHOOL_KIND ");
            stb.append("        AND UCM.GHR_CD = GSG.GHR_CD ");
            stb.append("        AND UCM.GRADE || UCM.HR_CLASS = GSG.GRADE || GSG.HR_CLASS ");
            stb.append("        AND UCM.CONDITION = GSG.CONDITION  ");
            stb.append("        AND UCM.GROUPCD = GSG.GROUPCD ");
            stb.append("        AND UCM.CLASSCD = SCH_SUB.CLASSCD ");
            stb.append("        AND UCM.SCHOOL_KIND = SCH_SUB.SCHOOL_KIND ");
            stb.append("        AND UCM.CURRICULUM_CD = SCH_SUB.CURRICULUM_CD ");
            stb.append("        AND UCM.SUBCLASSCD = SCH_SUB.SUBCLASSCD ");
            stb.append("        AND UCM.UNITCD = SCH_SUB.UNITCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SCH_SUB.CLASSCD, ");
            stb.append("     SCH_SUB.SCHOOL_KIND, ");
            stb.append("     SCH_SUB.CURRICULUM_CD, ");
            stb.append("     SCH_SUB.SUBCLASSCD, ");
            stb.append("     SCH_SUB.UNITCD, ");
            stb.append("     SCH_SUB.SEQ ");
            return stb.toString();
        }
    }

    private class UnitData {
        final String _unitCd;
        final String _unitName;
        final Map _unitSeqMap;

        public UnitData(final String unitCd, final String unitName) {
            _unitCd = unitCd;
            _unitName = unitName;
            _unitSeqMap = new HashMap();
        }
    }


    private class SubclsInfo {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final boolean _isUnit;
        final Map _semeUnitMap;

        public SubclsInfo(final String classCd, final String schoolKind, final String curriculumCd,
                final String subclassCd, final String subclassName, final String unitCd) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _isUnit = !"00".equals(unitCd);
            _semeUnitMap = new LinkedMap();
        }

        public void setSemeUnitMap(final String semester, final String unitCd, final String unitName,
                final String seq, final String remark
        ) {
            UnitData unitData;
            final Map unitDataMap;
            if (_semeUnitMap.containsKey(semester)) {
                unitDataMap = (Map) _semeUnitMap.get(semester);
            } else {
                unitDataMap = new LinkedMap();
                _semeUnitMap.put(semester, unitDataMap);
            }
            if (unitDataMap.containsKey(unitCd)) {
                unitData = (UnitData) unitDataMap.get(unitCd);
            } else {
                unitData = new UnitData(unitCd, unitName);
                unitDataMap.put(unitCd, unitData);
            }
            unitData._unitSeqMap.put(seq, remark);
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77045 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _hukusikiRadio;
        final String _schoolKind;
        final String[] _categorySelected;
        final String _moveDate;
        final boolean _isPrintHyosi;
        final boolean _isPrintUraByosi;
        final boolean _isPrintOuin;
        final String _frmPatern;
        final String _seisekiFrm;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _whiteSpaceImagePath;
        final String _slashImagePath;
        final String _schoolCd;
        final Map _semesterMap;
        private Map _subclassMstMap;
        private List _d026List = Collections.EMPTY_LIST;
        String _lastSemester;
        final Map _hreportConditionMap;
        final Map _certifSchoolMap;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;
        Map _attendRanges;

        final String _paraFrmPatern;
        final Map _tatgetTtlMap;  //目標の項目のタイトル情報
        final List _evalTtlList;  //教科別評価のタイトル情報(左,中,右の順で格納)

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _hukusikiRadio = request.getParameter("HUKUSIKI_RADIO");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _moveDate = request.getParameter("MOVE_DATE");
            _isPrintHyosi = "1".equals(request.getParameter("HYOSI"));
            _isPrintUraByosi = "1".equals(request.getParameter("URABYOSI"));
            _isPrintOuin = "1".equals(request.getParameter("OUIN"));
            _paraFrmPatern = request.getParameter("FRM_PATERN");
            _frmPatern = getFormPatern(db2, _paraFrmPatern);
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolCd = request.getParameter("SCHOOLCD");
            _semesterMap = loadSemester(db2);
            _seisekiFrm = getSeisekiFrm();
            setSubclassMst(db2);
            _hreportConditionMap = getHreportConditionMap(db2);
            _certifSchoolMap = getCertifSchoolMap(db2);
            loadNameMstD026(db2);

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
            _slashImagePath = getImageFilePath("slash_bs.jpg");
            _tatgetTtlMap = loadTargetInfo(db2);
            _evalTtlList = loadEvalTitleInfo(db2);
        }

        private String getFormPatern(final DB2UDB db2, final String paraFrmPatern) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMESPARE3 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = 'A035' ");
            stb.append("     AND NAMECD2 = '" + paraFrmPatern + "' ");

            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("NAMESPARE3");
                }
            } catch (SQLException ex) {
                log.error("certif_school_dat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (StringUtils.isBlank(retStr)) {
                log.info(" formPatern null : " + paraFrmPatern);
            }
            return retStr;
        }

        public String getSeisekiFrm() {
            String retFrm = "KNJD429M_2_BCD.frm";
            if (StringUtils.isBlank(retFrm)) {
                log.info(" seisekiFrm null : frmPatern = " + _frmPatern);
            }
            return retFrm;
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

        private Map getHreportConditionMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            //SEQ LIKE '1%'について。知的障害(1)と知的以外(2)の設定があり、SEQが1××又は、2××と登録される。
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     HREPORT_CONDITION_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SCHOOLCD = '" + _schoolCd + "' ");
            stb.append("     AND SEQ LIKE '1%' ");
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
                log.error("certif_school_dat exception!", ex);
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
                    if ("117".equals(certifKindcd)) {
                        setSchoolKind = "P";
                    } else if ("103".equals(certifKindcd)) {
                        setSchoolKind = "J";
                    } else {
                        setSchoolKind = "H";
                    }
                    retMap.put(setSchoolKind, certifSchol);
                }
            } catch (SQLException ex) {
                log.error("certif_school_dat exception!", ex);
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

        private Map loadTargetInfo(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     HDAT.YEAR ");
            stb.append("   , HDAT.KIND_NO ");
            stb.append("   , HDAT.KIND_NAME ");
            stb.append("   , KIND.KIND_SEQ ");
            stb.append("   , KIND.KIND_REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORT_GUIDANCE_KIND_NAME_HDAT HDAT ");
            stb.append("     LEFT JOIN HREPORT_GUIDANCE_KIND_NAME_DAT KIND ");
            stb.append("         ON KIND.YEAR     = HDAT.YEAR ");
            stb.append("         AND KIND.KIND_NO = HDAT.KIND_NO ");
            stb.append(" WHERE ");
            stb.append("         HDAT.YEAR    = '" + _ctrlYear + "' ");
            stb.append("     AND HDAT.KIND_NO = '02' ");
            stb.append(" ORDER BY ");
            stb.append("     KIND.KIND_SEQ ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String kind_Seq = rs.getString("KIND_SEQ");
                    final String kind_Name = rs.getString("KIND_NAME");
                    final String kind_Remark = rs.getString("KIND_REMARK");
                    TargetInfo addwk = new TargetInfo(kind_Seq, kind_Name, kind_Remark);
                    retMap.put(kind_Seq, addwk);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private List loadEvalTitleInfo(final DB2UDB db2) {
            final List retList = new ArrayList();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   ITEM_REMARK1 AS LEFT_TITILE, ");
            stb.append("   ITEM_REMARK2 AS MIDDLE_TITLE, ");
            stb.append("   ITEM_REMARK3 AS RIGHT_TITLE ");
            stb.append(" FROM ");
            stb.append("   HREPORT_GUIDANCE_ITEM_NAME_DAT ");
            stb.append(" WHERE ");
            stb.append("   YEAR = '" + _ctrlYear + "' ");
            stb.append("   AND SEMESTER = '9' ");
            stb.append("   AND GAKUBU_SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("   AND CONDITION = '1' ");
            stb.append("   AND GUIDANCE_PATTERN = '3' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String leftTtl = rs.getString("LEFT_TITILE");
                    final String midTtl = rs.getString("MIDDLE_TITLE");
                    final String rightTtl = rs.getString("RIGHT_TITLE");
                    retList.add(leftTtl);
                    retList.add(midTtl);
                    retList.add(rightTtl);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retList;
        }
    }
}

// eof

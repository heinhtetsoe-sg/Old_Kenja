/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
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

public class KNJD429L {

    private static final Log log = LogFactory.getLog(KNJD429L.class);

    private static final String SEMEALL = "9";

    private boolean _hasData;

    final int FIRST_PAGE_MAXLINE = 36;
    final int PAGE_MAXLINE = 54;
    int LINE_CNT = 0;

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
            if (_param._isPrintUra) {
                printUraByoushi(db2, svf, student);
            }
            printSeiseki(svf, student);
            printSpAct(svf, student);
            printAttend(svf, student);

            _hasData = true;
        }
    }

    private void printHyoushi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        boolean printFlg = false;
        final String setForm;
        setForm = "KNJD429L_1_A_1.frm";
        setForm(svf, setForm, 4, "hyoushi");
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
            final HreportCondition hreportCondition1 = (HreportCondition) conditionMap.get("201");
            if (null != hreportCondition1) {
                svf.VrsOut("TITLE", hreportCondition1._remark10);
            }
            //クラス名
            final HreportCondition hreportCondition4 = (HreportCondition) conditionMap.get("204");
            if (null != hreportCondition4) {
                if ("1".equals(hreportCondition4._remark1)) {//学年のみ表示
                    svf.VrsOut("HR_NAME", gradeHrName);
                } else if ("2".equals(hreportCondition4._remark1)) {//年組(法定)を表示
                    svf.VrsOut("HR_NAME", gradeHrName + student._hrClassName2);
                } else if ("3".equals(hreportCondition4._remark1)) {//年組(実クラス)を表示
                    svf.VrsOut("HR_NAME", gradeHrName + student._ghrNameAbbv);
                }

            }

            //教育目標・目指す生徒像
            final HreportCondition hreportCondition5 = (HreportCondition) conditionMap.get("213");
            if (hreportCondition5 != null && hreportCondition5._remark10 != "") {
                final List<String> hope = KNJ_EditKinsoku.getTokenList(hreportCondition5._remark10, 120);
                for (int cnt = 0; cnt < hope.size(); cnt++) {
                    svf.VrsOutn("HOPE", cnt + 1, hope.get(cnt));
                }
            }

            //担任
            LinkedList<String> staffList = new LinkedList<String>();
            final HreportCondition hreportCondition3 = (HreportCondition) conditionMap.get("203");
            staffList = setStaff("2".equals(hreportCondition3._remark1) ? student._ghTannin1 : student._tannin1, staffList);
            staffList = setStaff("2".equals(hreportCondition3._remark1) ? student._ghTannin2 : student._tannin2, staffList);
            staffList = setStaff("2".equals(hreportCondition3._remark1) ? student._ghTannin3 : student._tannin3, staffList);
            staffList = setStaff("2".equals(hreportCondition3._remark1) ? student._ghTannin4 : student._tannin4, staffList);
            staffList = setStaff("2".equals(hreportCondition3._remark1) ? student._ghTannin5 : student._tannin5, staffList);
            staffList = setStaff("2".equals(hreportCondition3._remark1) ? student._ghTannin6 : student._tannin6, staffList);

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

        //担任枠がないと帳票出力されないため念のため回避
        if (!printFlg){
            svf.VrsOut("TR_TITLE1", "担任");
            svf.VrEndRecord();
        }
        svf.VrEndPage();
    }

    private void printUraByoushi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String setForm = _param._is3Gakki ? "KNJD429L_1_A_2_2.frm" : "KNJD429L_1_A_2_1.frm";
        setForm(svf, setForm, 1, "ura");
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

        //押印枠
        if (!_param._isPrintOuin) {
            svf.VrsOut("BLANK", _param._whiteSpaceImagePath);
        }
        svf.VrEndPage();
    }

    private void setForm(final Vrw32alp svf, final String setForm, final int no, final String comment) {
        svf.VrSetForm(setForm, no);
        log.info(" setform " + comment + " : " + setForm);
    }

    private LinkedList setStaff(final String staffName, final LinkedList list) {
        if (staffName != "") {
            list.add(staffName);
        }
        return list;
    }


    private void printRemark(final Vrw32alp svf, final String fieldName, final List remark1List) {
        for (int i = 0; i < remark1List.size(); i++) {
            final String setRemark = (String) remark1List.get(i);
            svf.VrsOutn(fieldName, i + 1, StringUtils.defaultString(setRemark, ""));
        }
    }

    private void printTitleShimei(final Vrw32alp svf, final Student student, final Semester semesterObj) {
        svf.VrsOut("TITLE", semesterObj._semesterName + "の記録");
        svf.VrsOut("NAME", student._gradeName2 + "　" + student._name);
        svf.VrsOut("SEMESTER", semesterObj._semesterName);
    }

    // 学習の様子
    private void printSpAct(final Vrw32alp svf, final Student student) {
        setForm(svf, "KNJD429L_3_H.frm", 4, "gakushugakki3jk");
        final Semester semesterObj = (Semester) _param._semesterMap.get(_param._semester);

        printTitleShimei(svf, student, semesterObj);

        //ここからは、出力パターンが分かれる
        ////特別の教科、道徳  小(低/中/高),中
        ////〇外国語活動 小(中学年)
        ////〇総合的な学習 小(中/高),中
        ////特別活動  小(低/中/高),中
        ////自立活動  小(低/中/高),中
        ////1:表示 0:非表示
        final String[] pattern_PL = {"1", "0", "0", "1", "1"};
        final String[] pattern_PM = {"1", "1", "1", "1", "1"};
        final String[] pattern_PH = {"1", "0", "1", "1", "1"};
        final String[] pattern_J = {"1", "0", "1", "1", "1"};
        final String[] pattern_H = {"0", "0", "1", "1", "1"};
        final String[] pattern_A = {"0", "0", "0", "0", "0"};  //専攻科用(例外パターン含む)
        final String[] usePattern;
        if ("P".equals(_param._schoolKind)) {
            if ("".equals(StringUtils.defaultString(student._gradeCd, ""))) {
                usePattern = pattern_A;
            } else if (Integer.parseInt(student._gradeCd) <= 2) {
                usePattern = pattern_PL;
            } else if (Integer.parseInt(student._gradeCd) <= 4) {
                usePattern = pattern_PM;
            } else if (Integer.parseInt(student._gradeCd) <= 6) {
                usePattern = pattern_PH;
            } else {
                usePattern = pattern_A;
            }
        } else if ("J".equals(_param._schoolKind)) {
            usePattern = pattern_J;
        } else if ("H".equals(_param._schoolKind)) {
            usePattern = pattern_H;
        } else {
            usePattern = pattern_A;
        }

        final String semester = _param._semester;
        final HreportRemarkDat hreportRemarkDat = (HreportRemarkDat) student._hreportRemarkDatMap.get(semester);
        if ("1".equals(usePattern[0])) {
            svf.VrsOut("SP_ACT_NAME1", "特別の教科 道徳");
            if (hreportRemarkDat != null) {
                final List remark1List = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._moral, 70);
                if (remark1List.size() > 0) printRemark(svf, "MORAL", remark1List);
            }
            svf.VrEndRecord();
        }
        if ("1".equals(usePattern[1])) {
            svf.VrsOut("SP_ACT_NAME1", "外国語活動");
            if (hreportRemarkDat != null) {
                final List forList = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._foreignlangact, 70);
                if (forList.size() > 0) printRemark(svf, "MORAL", forList);
            }
            svf.VrEndRecord();
        }
        if ("1".equals(usePattern[2])) {
            svf.VrsOut("SP_ACT_NAME2_1", "総合的な");
            svf.VrsOut("SP_ACT_NAME2_2", "学習の時間");
            if (hreportRemarkDat != null) {
                final List remark3List = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._totalstudytime, 70);
                if (remark3List.size() > 0) printRemark(svf, "MORAL", remark3List);
            }
            svf.VrEndRecord();
        }
        if ("1".equals(usePattern[3])) {
            svf.VrsOut("SP_ACT_NAME1", "特別活動");
            if (hreportRemarkDat != null) {
                final List remark4List = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._specialactremark, 70);
                if (remark4List.size() > 0) printRemark(svf, "MORAL", remark4List);
            }
            svf.VrEndRecord();
        }
        if ("1".equals(usePattern[4])) {
            svf.VrsOut("SP_ACT_NAME1", "自立活動");
            if (hreportRemarkDat != null) {
                final List remark5List = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._ownact, 70);
                if (remark5List.size() > 0) printRemark(svf, "MORAL", remark5List);
            }
            svf.VrEndRecord();
        }

        svf.VrEndPage();
    }

    private void printKoudounoKiroku(final Vrw32alp svf, final Student student, final Semester semesterObj) {
        final Map conditionMap = (Map) _param._hreportConditionMap.get(student._schoolKind);
        if (null != conditionMap) {
            final HreportCondition hreportCondition8 = (HreportCondition) conditionMap.get("208");
            if ("2".equals(hreportCondition8._remark1)) {
                svf.VrsOut("VIEW_NAME1", "BLANK");
                svf.VrsOut("BLANK1", _param._whiteSpaceImagePath);
                svf.VrEndRecord();
                return;
            }
        }
        int koudouCnt = 1;
        final String getConvgrade = (String)student._behaviorConvGradeMap.get(student._grade);
        if (student._behaviorSemeMap.size() == 0) {
            svf.VrsOut("VIEW_NAME1", "BLANK");
            svf.VrsOut("BLANK1", _param._whiteSpaceImagePath);
            svf.VrEndRecord();
        } else {
            for (Iterator itbs = student._behaviorSemeMap.keySet().iterator();itbs.hasNext();) {
                final String kStr = (String)itbs.next();
                if (!kStr.startsWith(getConvgrade)) continue;
                if (koudouCnt > 13) continue;
                final HrBehaviorInfo record = (HrBehaviorInfo) student._behaviorSemeMap.get(kStr);
                svf.VrsOut("VIEW_NAME1", record._l_Name);
                svf.VrsOut("VIEW_NAME2", record._m_Name);
                final String resStr = (String)record._behaviorDat.get(_param._semester);
                final String resultStr = "3".equals(resStr) ? "◎" : ("2".equals(resStr) ? "○" : ("1".equals(resStr) ? "△" : ""));
                svf.VrsOut("VIEW", resultStr);
                koudouCnt++;
                svf.VrEndRecord();
            }
        }
    }

    // 行動、身体、出欠の記録
    private void printAttend(final Vrw32alp svf, final Student student) {
        final String form = _param._is3Gakki ? "KNJD429L_4_E_2.frm" : "KNJD429L_4_E.frm";
        setForm(svf, form, 4, "attend");
        if (_param._lastSemester.equals(_param._semester)) {
            svf.VrsOut("BLANK", _param._whiteSpaceImagePath);
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
                final int arLen = KNJ_EditEdit.getMS932ByteLength(hreportRemarkDat._attendrecRemark);
                final String attendRemarkField;
                if (_param._is3Gakki) {
                    if (arLen <= 20) {
                        attendRemarkField = arLen > 14 ? "_2" : "_1";
                        svf.VrsOut("ATTEND_REMARK" + semester + attendRemarkField, hreportRemarkDat._attendrecRemark);
                    } else {
                        final List<String> remark = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._attendrecRemark, 20);
                        svf.VrsOut("ATTEND_REMARK" + semester + "_3", remark.get(0));
                        svf.VrsOut("ATTEND_REMARK" + semester + "_4", remark.get(1));
                    }
                } else {
                    attendRemarkField = arLen > 30 ? "_3" : arLen > 20 ? "_2" : "_1";
                    svf.VrsOut("ATTEND_REMARK" + semester + attendRemarkField, hreportRemarkDat._attendrecRemark);
                }
            }
        }

        ////学校より
        final HreportRemarkDat hreportRemarkDat = (HreportRemarkDat) student._hreportRemarkDatMap.get(_param._semester);

        if (null != hreportRemarkDat) {
            if (null != hreportRemarkDat._communication) {
                final String[] communicationArray = KNJ_EditEdit.get_token(hreportRemarkDat._communication, 90, 8);
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
                final HreportCondition hreportCondition9 = (HreportCondition) conditionMap.get("212");
                if ("2".equals(hreportCondition9._remark1)) {
                    svf.VrsOut("BLANK2", _param._whiteSpaceImagePath);
                }
            }
        }

        ////生活の様子(行動の記録と同じ?)[非表示制御あり]
        printKoudounoKiroku(svf, student, semesterObj);

        svf.VrEndPage();
    }

    private void printSeiseki(final Vrw32alp svf, final Student student) {
        if (StringUtils.isBlank(_param._seisekiFrm)) {
            log.warn(" seisekiFrm null.");
            return;
        }
        setForm(svf, _param._seisekiFrm, 4, "seiseki");
        printSeisekiTitle(svf, student);
        if ("H".equals(_param._schoolKind)) {
            printPaternL(svf, student);
            svf.VrEndPage();
        } else {
            printPaternK(svf, student);
            svf.VrEndPage();
        }
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

    private void printAttend(final Vrw32alp svf, final Student student, final String subclassCd, final String semester, final String fieldName) {
        //欠課
        if (student._attendSubClassMap.containsKey(subclassCd)) {
            final Map atSubSemeMap = (Map) student._attendSubClassMap.get(subclassCd);
            if (atSubSemeMap.containsKey(semester)) {
                final SubclassAttendance attendance = (SubclassAttendance) atSubSemeMap.get(semester);
                svf.VrsOut(fieldName, attendance._sick.toString());
            } else {
                svf.VrsOut(fieldName, "0");
            }
        } else {
            svf.VrsOut(fieldName, "0");
        }
    }

    private void printPaternK(Vrw32alp svf, Student student) {

        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        int grp1 = 1;
        int grp2 = 1;

        final Map scoreSubclassMap108 = (Map) student._recordScoreSemeSdivMap.get("108");
        final Map scoreSubclassMap208 = (Map) student._recordScoreSemeSdivMap.get("208");
        final Map scoreSubclassMap308 = (Map) student._recordScoreSemeSdivMap.get("308");

        //科目
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclassCd;
            String putClassName = subclassMst._className;
            final String convGradeStr1 = getGradeConvSubcls(svf, student, subclassCd, "1");
            final String convGradeStr2 = getGradeConvSubcls(svf, student, subclassCd, "2");
            final String convGradeStr = "".equals(convGradeStr1) ? convGradeStr2 : convGradeStr1;
            final String[] spltStr = StringUtils.split(convGradeStr, ":");
            String convSubclsStr1 = "";
            Map jviewSubclassMap = null;
            if (spltStr.length != 3 || "".equals(spltStr[0]) || "".equals(spltStr[1]) || "".equals(spltStr[2])) {
                //変換出来ない場合、そのままとして処理
                convSubclsStr1 = subclassCd;
                jviewSubclassMap = (Map) _param._jviewGradeMap.get(student._grade);
                if (null == jviewSubclassMap || !jviewSubclassMap.containsKey(convSubclsStr1)) {
                    continue;
                }
            } else {
                //変換になる場合(変換前のままも含む)
                convSubclsStr1 = spltStr[1];
                jviewSubclassMap = (Map) _param._jviewGradeMap.get(spltStr[0]);
                if (null == jviewSubclassMap || !jviewSubclassMap.containsKey(convSubclsStr1)) {
                    continue;
                }

            }
            final List jviewGradeList = (List)jviewSubclassMap.get(convSubclsStr1);
            final List setClassNameList = KNJ_EditKinsoku.getTokenList(putClassName, 2);

            int maxLine = jviewGradeList.size();
            if (setClassNameList.size() > maxLine) {
                maxLine = setClassNameList.size();
            }

            checkLineAndPageChange(svf, maxLine, 36, student);
            int syokenCnt = 0;

            for (int i = 0; i < maxLine; i++) {
                if (i < setClassNameList.size()) {
                    svf.VrsOut("CLASS_NAME1", (String) setClassNameList.get(i));
                }
                svf.VrsOut("GRP1_2", String.valueOf(grp2));
                svf.VrsOut("GRP1_1", String.valueOf(grp1));
                svf.VrsOut("GRP2_1", String.valueOf(grp1));
                svf.VrsOut("GRP3_1", String.valueOf(grp1));
                if (i < jviewGradeList.size()) {
                    final JviewGrade jviewGrade = (JviewGrade) jviewGradeList.get(i);
                    final int vnLen = KNJ_EditEdit.getMS932ByteLength(jviewGrade._viewName);
                    final String vnField = vnLen > 60 ? "_2" : "";

                    svf.VrsOut("CONTENT1" + vnField, jviewGrade._viewName);

                    printJviewValue(svf, student, subclassCd, jviewGrade, "VIEW1", "1");
                    printJviewValue(svf, student, subclassCd, jviewGrade, "VIEW2", "2");
                    printJviewValue(svf, student, subclassCd, jviewGrade, "VIEW3", "3");
                }
                //評点
                printScoreValue(svf, subclassCd, scoreSubclassMap108, "VALUE1", "1", true);
                printScoreValue(svf, subclassCd, scoreSubclassMap208, "VALUE2", "2", true);
                printScoreValue(svf, subclassCd, scoreSubclassMap308, "VALUE3", "3", true);
                svf.VrEndRecord();
                syokenCnt = syokenCnt + 2;

                LINE_CNT++;
                grp2++;
            }
            grp1++;
        }

    }

    private void printPaternL(Vrw32alp svf, Student student) {

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

        //平均
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclassCd;

            if ((null == scoreSubclassMap108 || !scoreSubclassMap108.containsKey(subclassCd)) &&
                (null == scoreSubclassMap208 || !scoreSubclassMap208.containsKey(subclassCd)) &&
                (null == scoreSubclassMap308 || !scoreSubclassMap308.containsKey(subclassCd))
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
        }

        if (seme1Cnt != 0) {
            final BigDecimal setAvg1 = new BigDecimal(seme1TotalScore).divide(new BigDecimal(seme1Cnt), 0, BigDecimal.ROUND_HALF_UP);
            svf.VrsOut("AVERAGE1", setAvg1.toString());
        }
        if (2 <= Integer.parseInt(_param._semester)) {
            if (seme2Cnt != 0) {
                final BigDecimal setAvg9 = new BigDecimal(seme2TotalScore).divide(new BigDecimal(seme2Cnt), 0, BigDecimal.ROUND_HALF_UP);
                svf.VrsOut("AVERAGE2", setAvg9.toString());
            }
        }
        if (3 <= Integer.parseInt(_param._semester)) {
            if (seme3Cnt != 0) {
                final BigDecimal setAvgVal = new BigDecimal(seme3TotalScore).divide(new BigDecimal(seme3Cnt), 0, BigDecimal.ROUND_HALF_UP);
                svf.VrsOut("AVERAGE3", setAvgVal.toString());
            }
        }

        //合計修得単位
        svf.VrsOut("GET_CREDIT", String.valueOf(student._totalGetCredit));

        final int maxLine = 23;
        //科目
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclassCd;
            if ((null == scoreSubclassMap108 || !scoreSubclassMap108.containsKey(subclassCd)) &&
                (null == scoreSubclassMap208 || !scoreSubclassMap208.containsKey(subclassCd)) &&
                (null == scoreSubclassMap909 || !scoreSubclassMap909.containsKey(subclassCd))
            ) {
                continue;
            }

            checkLineAndPageChange(svf, 1, maxLine, student);
            svf.VrsOut("CLASS_NAME1", subclassMst._className);
            svf.VrsOut("SUBCLASS_NAME", subclassMst._subclassName);

            //単位
            final String setCredit = (String) _param._creditMap.get(student._grade + student._courseCd + student._majorCd + student._courseCode + subclassCd);
            svf.VrsOut("CREDIT1", StringUtils.defaultString(setCredit));

            //評点
            printScoreValue(svf, subclassCd, scoreSubclassMap108, "VALUE1", "1", false);
            printScoreValue(svf, subclassCd, scoreSubclassMap208, "VALUE2", "2", false);
            printScoreValue(svf, subclassCd, scoreSubclassMap308, "VALUE3", "3", false);
            printScoreValue(svf, subclassCd, scoreSubclassMap909, "VALUE9", _param._lastSemester, false);

            //欠課
            printAttend(svf, student, subclassCd, "9", "ABSENCE1");

            svf.VrEndRecord();
            LINE_CNT++;
        }

        for (int i = LINE_CNT; i < maxLine; i++) {
            svf.VrAttribute("CLASS_NAME1", "Meido=100");
            svf.VrsOut("CLASS_NAME1", "1");
            svf.VrEndRecord();
        }
    }

    private String getGradeConvSubcls(final Vrw32alp svf, Student student, final String subclassCd, final String semester) {
        if (Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
            return "";
        }
        if (null != student._jviewRecordSemeMap) {
            if (student._jviewRecordSemeMap.containsKey(semester)) {
                final Map jviewRecSubMap = (Map) student._jviewRecordSemeMap.get(semester);
                if (null != jviewRecSubMap) {
                    for (Iterator ite = jviewRecSubMap.keySet().iterator();ite.hasNext();) {
                        final String kStr = (String)ite.next();
                        final String[] subclsArr = StringUtils.split(kStr, ":");
                        final String convSubclsCd = student._jviewSubclsConvMap.containsKey(subclsArr[1]) ? (String)student._jviewSubclsConvMap.get(subclsArr[1]) : subclsArr[1];
                        if (convSubclsCd.equals(subclassCd)) {
                            return kStr + ":" + convSubclsCd;
                        }
                    }
                }
            }
        }
        return "";
    }

    private void printJviewValue(final Vrw32alp svf, Student student, final String subclassCd, final JviewGrade jviewGrade, final String fieldName, final String semester) {
        if (Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
            return;
        }
        if (null != student._jviewRecordSemeMap) {
            if (student._jviewRecordSemeMap.containsKey(semester)) {
                final Map jviewRecSubMap = (Map) student._jviewRecordSemeMap.get(semester);
                if (null != jviewRecSubMap) {
                    for (Iterator ite = jviewRecSubMap.keySet().iterator();ite.hasNext();) {
                        final String kStr = (String)ite.next();
                        final String[] subclsArr = StringUtils.split(kStr, ":");
                        final String convSubclsCd = student._jviewSubclsConvMap.containsKey(subclsArr[1]) ? (String)student._jviewSubclsConvMap.get(subclsArr[1]) : subclsArr[1];
                        if (convSubclsCd.equals(subclassCd)) {
                            final Map jviewRecViewMap = (Map) jviewRecSubMap.get(kStr);
                            if (jviewRecViewMap.containsKey(jviewGrade._viewCd)) {
                                final JviewRecord jviewRecord = (JviewRecord) jviewRecViewMap.get(jviewGrade._viewCd);
                                svf.VrsOut(fieldName, StringUtils.defaultString(jviewRecord._statusName));
                            }
                        }
                    }
                }
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

    private void printScoreValue(final Vrw32alp svf, final String subclassCd, final Map scoreSubclassMap, final String fieldName, final String semester, final boolean convertMarkFlg) {
        if (Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
            return;
        }
        if (null == scoreSubclassMap) {
            return;
        }
        final ScoreData scoreData = (ScoreData) scoreSubclassMap.get(subclassCd);
        if (null != scoreData) {
            final String scoreStr = StringUtils.defaultString(scoreData._score);
            final String putStr = convertMarkFlg ? scoreStr : ("1".equals(scoreStr) ? "◎": ("2".equals(scoreStr) ? "○": ("3".equals(scoreStr) ? "△" : "")));
            svf.VrsOut(fieldName, convertMarkFlg ? putStr : scoreStr);
        }
    }

    private List getStudentList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String studentSql = getStudentSql();
        log.debug(" sql =" + studentSql);
        try {
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
                student.setRecordSyokenSemeMap(db2);
                student.setRecordScoreSemeSdivMap(db2);
                student.setJview(db2);
                student.setTokusyuKamoku(db2);
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
        final Map _attendMap = new TreeMap();
        final Map _behaviorConvGradeMap;
        final Map _behaviorSemeMap;
        final Map _recordSyokenSemeMap = new TreeMap();
        final Map _recordScoreSemeSdivMap = new TreeMap();
        final Map _jviewRecordSemeMap = new TreeMap();
        final Map _attendSubClassMap = new TreeMap();
        final Map _hreportRemarkDatMap = new TreeMap();
        final Map _jviewSubclsConvMap = new TreeMap();
        int _totalGetCredit = 0;
        String _sougakuSubClassCd;
        String _tokkatsuSubClassCd;
        String _jikatsuSubClassCd;

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
                    final String moral = rs.getString("MORAL");
                    final String ownact = rs.getString("OWNACT");
                    final HreportRemarkDat hreportRemarkDat = new HreportRemarkDat(totalstudytime, specialactremark, communication, remark1, remark2, remark3, foreignlangact, attendrecRemark, moral, ownact);
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
            stb.append("     T2_1.REMARK1 AS MORAL, ");
            stb.append("     T2_2.REMARK1 AS OWNACT, ");
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
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SEMESTER ");

            return stb.toString();
        }

        private void setRecordSyokenSemeMap(final DB2UDB db2) {
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
                    }
                    setSubMap.put(setSubclassCd, recordSyoken);
                    _recordSyokenSemeMap.put(semester, setSubMap);
                }
            } catch (SQLException ex) {
                log.error("setRecordSyokenSemeMap exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getRecordSyokenSemeSql() {
            final StringBuffer stb = new StringBuffer();
            if ("K".equals(_param._frmPatern)) {
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
                stb.append("     AND SCHREGNO = '" + _schregNo + "' ");
                stb.append(" ORDER BY ");
                stb.append("     SEMESTER, ");
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
                stb.append("     SUBCLASSCD ");
            } else {
                stb.append(" SELECT ");
                stb.append("     SEMESTER, ");
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
                stb.append("     SUBCLASSCD, ");
                stb.append("     CONDUCT_EVAL AS REMARK1 ");
                stb.append(" FROM ");
                stb.append("     RECORD_EDUCATE_GUIDANCE_CONDUCT_DAT ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _param._ctrlYear + "' ");
                stb.append("     AND SCHREGNO = '" + _schregNo + "' ");
                stb.append(" ORDER BY ");
                stb.append("     SEMESTER, ");
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
                stb.append("     SUBCLASSCD ");
            }

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
                    final String getCredit = StringUtils.defaultString(rs.getString("GET_CREDIT"));
                    final String score = rs.getString("SCORE");
                    final String setSubclassCd = classCd + "-" + schoolKind + "-" + curriculumCd + "-" + subclassCd;
                    final ScoreData scoreData = new ScoreData(classCd, setSubclassCd, getCredit, score);
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
            stb.append("     SEMESTER, ");
            stb.append("     SCORE_DIV, ");
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     GET_CREDIT, ");
            stb.append("     SCORE ");
            stb.append(" FROM ");
            stb.append("     RECORD_SCORE_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND TESTKINDCD = '99' ");
            stb.append("     AND TESTITEMCD = '00' ");
            stb.append("     AND SCORE_DIV IN ('08', '09') ");
            stb.append("     AND SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SEMESTER, ");
            stb.append("     SCORE_DIV, ");
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     SUBCLASSCD ");

            return stb.toString();
        }

        private void setJview(final DB2UDB db2) {
            final String scoreSql = getJviewScoreSql();
            log.debug(scoreSql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String semester = rs.getString("SEMESTER");
                    final String viewCd = rs.getString("VIEWCD");
                    final String status = rs.getString("STATUS");
                    final String statusName = rs.getString("MARK");
                    final String nowClsName = rs.getString("NOW_CLASSNAME");
                    final String nowSubClsName = rs.getString("NOW_SUBCLASSNAME");
                    final String baseSubclsCd = rs.getString("BASE_SUBCLASSCD");
                    final String followGrade = rs.getString("FOLLOW_GRADE");
                    final JviewRecord jviewRecord = new JviewRecord(subclassCd, semester, viewCd, status, statusName, nowClsName, nowSubClsName, baseSubclsCd, followGrade);
                    final Map setSubMap;
                    if (_jviewRecordSemeMap.containsKey(semester)) {
                        setSubMap = (Map) _jviewRecordSemeMap.get(semester);
                    } else {
                        setSubMap = new HashMap();
                    }
                    final Map jviewMap;
                    final String sKey = followGrade + ":" + subclassCd;
                    if (setSubMap.containsKey(sKey)) {
                        jviewMap = (Map) setSubMap.get(sKey);
                    } else {
                        jviewMap = new HashMap();
                    }
                    jviewMap.put(viewCd, jviewRecord);
                    setSubMap.put(sKey, jviewMap);
                    _jviewRecordSemeMap.put(semester, setSubMap);

                    if (!_jviewSubclsConvMap.containsKey(subclassCd)) {
                        _jviewSubclsConvMap.put(subclassCd, baseSubclsCd);
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
            stb.append("  AND T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("   T1.SEMESTER, ");
            stb.append("   T1.TARGET_CLASSCD || '-' || T1.TARGET_SCHOOL_KIND || '-' || T1.TARGET_CURRICULUM_CD || '-' || T1.TARGET_SUBCLASSCD, ");
            stb.append("   T1.TARGET_GRADE, ");
            stb.append("   T1.TARGET_VIEWCD ");
//            stb.append(" SELECT ");
//            stb.append("     REC.CLASSCD || '-' || REC.SCHOOL_KIND || '-' || REC.CURRICULUM_CD || '-' || REC.SUBCLASSCD AS SUBCLASSCD, ");
//            stb.append("     REC.SEMESTER, ");
//            stb.append("     REC.VIEWCD, ");
//            stb.append("     REC.STATUS, ");
//            stb.append("     D029.NAMESPARE1 AS MARK ");
//            stb.append(" FROM ");
//            stb.append("     JVIEWSTAT_RECORD_DAT REC ");
//            stb.append("     LEFT JOIN NAME_MST D029 ON D029.NAMECD1 = 'D029' ");
//            stb.append("          AND REC.STATUS = D029.ABBV1 ");
//            stb.append(" WHERE ");
//            stb.append("     REC.YEAR = '" + _param._ctrlYear + "' ");
//            stb.append("     AND REC.SCHREGNO = '" + _schregNo + "' ");
//            stb.append(" ORDER BY ");
//            stb.append("     REC.SEMESTER, ");
//            stb.append("     SUBCLASSCD, ");
//            stb.append("     VIEWCD ");

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
        private ScoreData(
                final String classCd,
                final String subclassCd,
                final String getCredit,
                final String score
        ) {
            _classCd = classCd;
            _subclassCd = subclassCd;
            _getCredit = getCredit;
            _score = score;
        }
    }

    private class JviewRecord {
        final String _subclassCd;
        final String _semester;
        final String _viewCd;
        final String _status;
        final String _statusName;
        final String _nowClsName;
        final String _nowSubClsName;
        final String _baseSubclsCd;
        final String _followGrade;
        private JviewRecord(
                final String subclassCd,
                final String semester,
                final String viewCd,
                final String status,
                final String statusName,
                final String nowClsName, final String nowSubClsName, final String baseSubclsCd, final String followGrade
        ) {
            _subclassCd = subclassCd;
            _semester = semester;
            _viewCd = viewCd;
            _status = status;
            _statusName = statusName;
            _nowClsName = nowClsName;
            _nowSubClsName = nowSubClsName;
            _baseSubclsCd = baseSubclsCd;
            _followGrade = followGrade;
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
        final String _communication;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _foreignlangact;
        final String _attendrecRemark;
        final String _moral;
        final String _ownact;

        public HreportRemarkDat(
                final String totalstudytime,
                final String specialactremark,
                final String communication,
                final String remark1,
                final String remark2,
                final String remark3,
                final String foreignlangact,
                final String attendrecRemark,
                final String moral,
                final String ownact
        ) {
            _totalstudytime = totalstudytime;
            _specialactremark = specialactremark;
            _communication = communication;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _foreignlangact = foreignlangact;
            _attendrecRemark = attendrecRemark;
            _moral = moral;
            _ownact = ownact;
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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
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
        final boolean _isPrintHyosi; //表表紙ありなし
        final boolean _isPrintUra; //裏表紙ありなし
        final boolean _isPrintOuin; //押印枠ありなし
        final boolean _is3Gakki; //3学期制の学校
        final String _requestFrm;
        final String _frmPatern;
        final String _seisekiFrm;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolCd;
        final Map _semesterMap;
        private Map _subclassMstMap;
        final Map _creditMap;
        private final Map _jviewGradeMap;
        private List _d026List = Collections.EMPTY_LIST;
        String _lastSemester;
        final Map _hreportConditionMap;
        final Map _certifSchoolMap;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;
        Map _attendRanges;

        private int _kantenHyoukaCnt_P = 0;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _hukusikiRadio = request.getParameter("HUKUSIKI_RADIO");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _moveDate = request.getParameter("MOVE_DATE");
            _isPrintHyosi = "1".equals(request.getParameter("HYOSI"));
            _isPrintUra = "1".equals(request.getParameter("URA"));
            _isPrintOuin = "1".equals(request.getParameter("OUIN"));
            _requestFrm = request.getParameter("FRM_PATERN");
            _frmPatern = getFormPatern(db2, _requestFrm);
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolCd = request.getParameter("SCHOOLCD");
            _semesterMap = loadSemester(db2);
            _is3Gakki = "3".equals(_lastSemester);
            _seisekiFrm = getSeisekiFrm();
            setSubclassMst(db2);
            _creditMap = getCreditMap(db2);
            _jviewGradeMap = getJviewGradeMap(db2);
            _hreportConditionMap = getHreportConditionMap(db2);
            _certifSchoolMap = getCertifSchoolMap(db2);
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

        /**
         * 校種と3学期制で成績frmを切り替える
         */
        public String getSeisekiFrm() {
            String retFrm = "";
            if ("201H".equals(_requestFrm)) {
                retFrm = _is3Gakki ? "KNJD429L_2_G_2.frm" : "KNJD429L_2_G.frm";
            } else if ("201J".equals(_requestFrm)) {
                retFrm = _is3Gakki ? "KNJD429L_2_F_2.frm" : "KNJD429L_2_F.frm";
            } else {
                retFrm = _is3Gakki ? "KNJD429L_2_J_2.frm" : "KNJD429L_2_J_1.frm";
            }
            if (StringUtils.isBlank(retFrm)) {
                log.info(" seisekiFrm null : frmPatern = " + _requestFrm);
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
            try {
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
            //SEQ LIKE '1%'について。知的障害(1)と知的以外(2)の設定があり、SEQが1××又は、2××と登録される。
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
            try {
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
            stb.append("     GRADE, ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     VIEWCD, ");
            stb.append("     VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST ");
            stb.append(" ORDER BY ");
            stb.append("     GRADE, ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     VIEWCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
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

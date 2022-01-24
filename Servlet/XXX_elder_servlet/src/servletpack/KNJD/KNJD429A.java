/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 72d5db6c40cbda9ae923c555f5236ae1d0bd1f0e $
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

public class KNJD429A {

    private static final Log log = LogFactory.getLog(KNJD429A.class);

    private static final String SEMEALL = "9";

    private boolean _hasData;
    private final String HOUTEI = "1";
    private final String JITSU = "2";

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
        final List studentList = getStudentList(db2);
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
            if (_param._isPrintHyosi) {
                printHyoushi(db2, svf, student);
            }
            printSeiseki(svf, student);
            printGakushu(svf, student);
            printAttend(svf, student);

            if (_param._isPrintHyosi && !"1".equals(_param._printSize)) {  //(A4裏表紙)
                if (_param._is2Gakki) {
                    setForm(svf, "KNJD429A_1_4.frm", 1, "urabyousi");
                } else {
                    setForm(svf, "KNJD429A_1_5.frm", 1, "urabyousi");
                }
            	printUraByoushi(db2, svf, student);
                svf.VrEndPage();
            }
            _hasData = true;
        }
    }

    private void printHyoushi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String setForm;
        if ("1".equals(_param._printSize)) {  //A3出力
            if (_param._is2Gakki) {
                setForm = "KNJD429A_1_1.frm";
            } else {
                setForm = "KNJD429A_1_2.frm";
            }
        } else {
            setForm = "KNJD429A_1_3.frm";  //A4表紙のみ
        }
        setForm(svf, setForm, 1, "hyoushi");
        if ("1".equals(_param._printSize)) {  //A3出力
        	printUraByoushi(db2, svf, student);
        }
        final String gradeHrName = student._gakubuName + "　" + student._gradeName1;
        final CertifSchool certifSchool = (CertifSchool) _param._certifSchoolMap.get(student._schoolKind);
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

        //担任
        int setLine = 1;
        setLine = setStaff(svf, student._tannin1, setLine);
        setLine = setStaff(svf, student._tannin2, setLine);
        setLine = setStaff(svf, student._tannin3, setLine);
        setLine = setStaff(svf, student._tannin4, setLine);

        //タイトル
        final Map conditionMap = (Map) _param._hreportConditionMap.get(student._schoolKind);
        if (null != conditionMap) {
            final HreportCondition hreportCondition1 = (HreportCondition) conditionMap.get("201");
            if (null != hreportCondition1) {
                svf.VrsOut("TITLE", hreportCondition1._remark10);
            }
            //クラス名
            final HreportCondition hreportCondition4 = (HreportCondition) conditionMap.get("204");
            if (null != hreportCondition4) {
                final String setHrName = "1".equals(hreportCondition4._remark1) ? student._hrName : "";
                svf.VrsOut("HR_NAME", gradeHrName + "　" + setHrName);
            }
        }
        svf.VrEndPage();
    }

    private void printUraByoushi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        //修了文言
        final String gradeHrName = student._gakubuName + "　" + student._gradeName1;
        svf.VrsOut("TEXT", gradeHrName + "の課程を修了したことを証する。");
        //日付フォーマットのみ
        final String[] dateArray = KNJ_EditDate.tate_format4(db2, _param._ctrlDate.replace('/', '-'));
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
    }

	private void setForm(final Vrw32alp svf, final String setForm, final int no, final String comment) {
		svf.VrSetForm(setForm, no);
		log.info(" setform " + comment + " : " + setForm);
	}

    private int setStaff(final Vrw32alp svf, final String staffName, final int setLine) {
        int retInt = setLine;
        final int staffLen = KNJ_EditEdit.getMS932ByteLength(staffName);
        final String staffField = staffLen > 30 ? "3" : staffLen > 20 ? "2" : "1";
        svf.VrsOutn("STAFF_NAME" + staffField, setLine, staffName);
        retInt++;
        return retInt;
    }

    // 学習の様子
    private void printGakushu(final Vrw32alp svf, final Student student) {
        if (_param._is2Gakki) {
            printGakushuGakki2(svf, student);
        } else {
            if ("P".equals(student._schoolKind)) {
                printGakushuGakki3PaternJK(svf, student);
            } else if ("J".equals(student._schoolKind)) {
                printGakushuGakki3PaternJK(svf, student);
            } else if ("H".equals(student._schoolKind)) {
                printGakushuGakki3PaternL(svf, student);
            }
        }
    }

    private void printRemark(final Vrw32alp svf, final String fieldName, final List remark1List) {
        for (int i = 0; i < remark1List.size(); i++) {
            final String setRemark = (String) remark1List.get(i);
            svf.VrsOutn(fieldName, i + 1, setRemark);
        }
    }

    private void printTitleShimei(final Vrw32alp svf, final Student student, final Semester semesterObj) {
        svf.VrsOut("TITLE", semesterObj._semesterName + "の記録");
        svf.VrsOut("NAME", student._gradeName1 + "　" + student._name);
        if ("G1".equals(_param._frmPatern)) {
            svf.VrsOut("SEMESTERNAME", semesterObj._semesterName);
        }
    }

    // 学習の様子
    private void printGakushuGakki2(final Vrw32alp svf, final Student student) {
        setForm(svf, _param.getGakushuFrm(student), 1, "gakushugakki2");
        final Semester semesterObj = (Semester) _param._semesterMap.get(_param._semester);

        printTitleShimei(svf, student, semesterObj);

        final HreportRemarkDat hreportRemarkDat = (HreportRemarkDat) student._hreportRemarkDatMap.get(_param._semester);
        if (null != hreportRemarkDat) {
        	final List remark1List = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._remark1, 70);
        	printRemark(svf, "MORAL", remark1List);
        	final List forList = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._foreignlangact, 70);
        	printRemark(svf, "FOREIGN", forList);
        	final List studyTimeList = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._specialactremark, 70);
        	printRemark(svf, "TOTAL_STUDY", studyTimeList);
        	final List specialList = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._totalstudytime, 70);
        	printRemark(svf, "SP_ACT", specialList);
        }
        final List detail0201R1List = KNJ_EditKinsoku.getTokenList(student._hrepRemarkDetail0201R1, 70);
        printRemark(svf, "ACTIVE1", detail0201R1List);
        final List detail0201R2List = KNJ_EditKinsoku.getTokenList(student._hrepRemarkDetail0201R2, 70);
        printRemark(svf, "ACTIVE2", detail0201R2List);

        printKoudounoKiroku(svf, student, semesterObj);
        svf.VrEndPage();
    }

    // 学習の様子
    private void printGakushuGakki3PaternJK(final Vrw32alp svf, final Student student) {
        setForm(svf, _param.getGakushuFrm(student), 1, "gakushugakki3jk");
        final Semester semesterObj = (Semester) _param._semesterMap.get(_param._semester);

        printTitleShimei(svf, student, semesterObj);

        for (Iterator itSemester = _param._semesterMap.keySet().iterator(); itSemester.hasNext();) {
            final String semester = (String) itSemester.next();
            if (Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
                continue;
            }
            if (student._hreportRemarkDatMap.containsKey(semester)) {
                final HreportRemarkDat hreportRemarkDat = (HreportRemarkDat) student._hreportRemarkDatMap.get(semester);
                final List remark1List = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._remark1, 30);
                printRemark(svf, "MORAL" + semester, remark1List);
                final List forList = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._foreignlangact, 30);
                printRemark(svf, "FOREIGN" + semester, forList);
                final List remark3List = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._remark3, 30);
                printRemark(svf, "ACTIVE" + semester, remark3List);
            }
        }
        if (student._hreportRemarkDatMap.containsKey("9")) {
            final HreportRemarkDat hreportRemarkDat = (HreportRemarkDat) student._hreportRemarkDatMap.get("9");
            //学習活動
            final List specialList = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._specialactremark, 44);
            printRemark(svf, "TOTAL_STUDY1", specialList);
            //評価
            final List studyTimeList = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._totalstudytime, 44);
            printRemark(svf, "TOTAL_STUDY2", studyTimeList);
        }
        //委員会
        final List detail0101R1List = KNJ_EditKinsoku.getTokenList(student._hrepRemarkDetail0101R1, 40);
        printRemark(svf, "COMITTEE", detail0101R1List);
        //部活動
        final List detail0102R1List = KNJ_EditKinsoku.getTokenList(student._hrepRemarkDetail0102R1, 40);
        printRemark(svf, "CLUB", detail0102R1List);

        //行動の記録
        printKoudounoKiroku(svf, student, semesterObj);

        svf.VrEndPage();
    }

    // 学習の様子
    private void printGakushuGakki3PaternL(final Vrw32alp svf, final Student student) {
        setForm(svf, _param.getGakushuFrm(student), 1, "gakushugakki3l");
        final Semester semesterObj = (Semester) _param._semesterMap.get(_param._semester);

        printTitleShimei(svf, student, semesterObj);

        for (Iterator itSemester = _param._semesterMap.keySet().iterator(); itSemester.hasNext();) {
            final String semester = (String) itSemester.next();
            if (Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
                continue;
            }
            if (student._hreportRemarkDatMap.containsKey(semester)) {
                final HreportRemarkDat hreportRemarkDat = (HreportRemarkDat) student._hreportRemarkDatMap.get(semester);
                final List remark3List = KNJ_EditKinsoku.getTokenList(hreportRemarkDat._remark3, 30);
                printRemark(svf, "ACTIVE" + semester, remark3List);
            }
        }

        //委員会
        final List detail0101R1List = KNJ_EditKinsoku.getTokenList(student._hrepRemarkDetail0101R1, 30);
        printRemark(svf, "COMITTEE", detail0101R1List);
        //部活動
        final List detail0102R1List = KNJ_EditKinsoku.getTokenList(student._hrepRemarkDetail0102R1, 30);
        printRemark(svf, "CLUB", detail0102R1List);
        //その他
        final List detail0103R1List = KNJ_EditKinsoku.getTokenList(student._hrepRemarkDetail0103R1, 30);
        printRemark(svf, "ETC", detail0103R1List);

        //行動の記録
        printKoudounoKiroku(svf, student, semesterObj);

        svf.VrEndPage();
    }

    private void printKoudounoKiroku(final Vrw32alp svf, final Student student, final Semester semesterObj) {
        final Map conditionMap = (Map) _param._hreportConditionMap.get(student._schoolKind);
        if (null != conditionMap) {
            final HreportCondition hreportCondition8 = (HreportCondition) conditionMap.get("208");
            if ("1".equals(hreportCondition8._remark1)) {
                svf.VrsOut("BLANK", _param._whiteSpaceImagePath);
            }
        }
        svf.VrsOut("SEMESTER1_1", semesterObj._semesterName);
        svf.VrsOut("SEMESTER1_2", semesterObj._semesterName);
        int koudouCnt = 1;
        String koudouField = "1";
        for (Iterator itD035 = _param._d035.keySet().iterator(); itD035.hasNext();) {
            final String code = (String) itD035.next();
            final String viewName = (String) _param._d035.get(code);
            final String record = (String) student._behaviorSemeMap.get(code);
            if (koudouCnt > 5) {
                koudouField = "2";
                koudouCnt = 1;
            }
            svf.VrsOutn("VIEW_NAME" + koudouField, koudouCnt, viewName);
            svf.VrsOutn("VIEW" + koudouField, koudouCnt, StringUtils.isEmpty(record) ? "" : record);
            koudouCnt++;
        }
    }

    // 行動、身体、出欠の記録
    private void printAttend(final Vrw32alp svf, final Student student) {
        String attendFrm = _param.getAttendFrm(student);
        if (StringUtils.isBlank(attendFrm)) {
        	log.info(" attendFrm null !");
        	return;
        }
		setForm(svf, attendFrm, 1, "attend");
        if (_param._lastSemester.equals(_param._semester)) {
            svf.VrsOut("BLANK", _param._whiteSpaceImagePath);
        }
        final Semester semesterObj = (Semester) _param._semesterMap.get(_param._semester);

        printTitleShimei(svf, student, semesterObj);

        printKoudounoKiroku(svf, student, semesterObj);

        //身体の記録
        int monthCol = 1;
        for (Iterator itMedexamMonth = student._medexamMonthList.iterator(); itMedexamMonth.hasNext();) {
            final MedexamMonth medexamMonth = (MedexamMonth) itMedexamMonth.next();
            if (Integer.parseInt(medexamMonth._semester) <= Integer.parseInt(_param._semester)) {
                svf.VrsOut("MONTH" + monthCol, Integer.parseInt(medexamMonth._month) + "月");
                svf.VrsOut("HIGHT" + monthCol, medexamMonth._height);
                svf.VrsOut("WEIGHT" + monthCol, medexamMonth._weight);
                svf.VrsOut("EYE" + monthCol + "_1_1", medexamMonth._rBarevisionMark);
                svf.VrsOut("EYE" + monthCol + "_1_2", medexamMonth._rVisionMark);
                svf.VrsOut("EYE" + monthCol + "_2_1", medexamMonth._lBarevisionMark);
                svf.VrsOut("EYE" + monthCol + "_2_2", medexamMonth._lVisionMark);
                monthCol++;
            }
        }
        if (null != student._medexamdet) {
        	svf.VrsOut("EAR1_1", student._medexamdet._rEar);
        	svf.VrsOut("EAR2_1", student._medexamdet._rEarIn);
        	svf.VrsOut("EAR1_2", student._medexamdet._lEar);
        	svf.VrsOut("EAR2_2", student._medexamdet._lEarIn);
        }

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
                final String attendRemarkField = KNJ_EditEdit.getMS932ByteLength(hreportRemarkDat._attendrecRemark) > 20 ? "_2" : "_1";
                svf.VrsOut("ATTEND_REMARK" + semester + attendRemarkField, hreportRemarkDat._attendrecRemark);
            }
        }

        final HreportRemarkDat hreportRemarkDat = (HreportRemarkDat) student._hreportRemarkDatMap.get(_param._semester);

        if (null != hreportRemarkDat) {
            if (null != hreportRemarkDat._communication) {
                final String[] communicationArray = KNJ_EditEdit.get_token(hreportRemarkDat._communication, 66, 8);
                for (int i = 0; i < communicationArray.length; i++) {
                    final String setText = communicationArray[i];
                    svf.VrsOutn("FROM_SCHOOL", i + 1, setText); // 学校より
                }
            }
        }
        svf.VrEndPage();
    }

    private void printSeiseki(final Vrw32alp svf, final Student student) {
    	if (StringUtils.isBlank(_param._seisekiFrm)) {
    		log.warn(" seisekiFrm null.");
    		return;
    	}
        setForm(svf, _param._seisekiFrm, 4, "seiseki");
        printSeisekiTitle(svf, student);
        if ("E1".equals(_param._frmPatern)) {
            printPaternE1(svf, student);
        } else if ("E2".equals(_param._frmPatern)) {
            printPaternE2(svf, student);
        } else if ("F".equals(_param._frmPatern)) {
            printPaternF(svf, student);
        } else if ("G1".equals(_param._frmPatern)) {
        	printPaternG1(svf, student);
        } else if ("G2".equals(_param._frmPatern)) {
        	printPaternG2(svf, student);
        } else if ("J".equals(_param._frmPatern)) {
            printPaternJ(svf, student);
        } else if ("K".equals(_param._frmPatern)) {
            printPaternK(svf, student);
        } else if ("L".equals(_param._frmPatern)) {
            printPaternL(svf, student);
        }
    }

    private void printSeisekiTitle(final Vrw32alp svf, final Student student) {
        final Semester semester;
        if ("G2".equals(_param._frmPatern) && _param._semesterMap.size() > 1) {
            semester = (Semester) _param._semesterMap.get("9");
        } else {
            semester = (Semester) _param._semesterMap.get(_param._semester);
        }
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

    private void printPaternE1(Vrw32alp svf, Student student) {

        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        int grp1 = 1;
        final Map printSubclass = new HashMap();
        final Map syokenMap = (Map) student._recordSyokenSemeMap.get(_param._semester);
        if (null == syokenMap) {
            return;
        }
        //科目
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclassCd;
            if (null == syokenMap || !syokenMap.containsKey(subclassCd)) {
                continue;
            }

            final int subclassLen = 2;
            final List setSubclassNameList = KNJ_EditKinsoku.getTokenList(subclassMst._subclassName, subclassLen);

            final RecordSyoken recordSyoken = (RecordSyoken) syokenMap.get(subclassCd);

            //所見
            final String remark1 = StringUtils.defaultString(recordSyoken._remark1);
            final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(remark1, 70);

            int maxLine = setRemarkList1.size();
            if (!printSubclass.containsKey(subclassCd) && setSubclassNameList.size() > maxLine) {
                maxLine = setSubclassNameList.size();
            }

            checkLineAndPageChange(svf, maxLine, 39, student);
            for (int i = 0; i < maxLine; i++) {
                if (!printSubclass.containsKey(subclassCd) && i < setSubclassNameList.size()) {
                    svf.VrsOut("CLASS_NAME1", (String) setSubclassNameList.get(i));
                }
                svf.VrsOut("GRP1_1", String.valueOf(grp1));
                svf.VrsOut("GRP1_2", String.valueOf(grp1));
                svf.VrsOut("GRP1_3", String.valueOf(grp1));
                if (i < setRemarkList1.size()) {
                    svf.VrsOut("CONTENT1", (String) setRemarkList1.get(i));
                }
                if (null != student._recordScoreSemeSdivMap) {
                    if (student._recordScoreSemeSdivMap.containsKey(_param._semester + "08")) {
                        final Map subClassMap = (Map) student._recordScoreSemeSdivMap.get(_param._semester + "08");
                        if (subClassMap.containsKey(subclassCd)) {
                            final ScoreData scoreData = (ScoreData) subClassMap.get(subclassCd);
                            String kigou = "";
                            if ("3".equals(scoreData._score)) {
                            	kigou = "◎";
                            } else if ("2".equals(scoreData._score)) {
                            	kigou = "○";
                            } else if ("1".equals(scoreData._score)) {
                            	kigou = "△";
                            }
                            svf.VrsOut("VALUE", kigou);
                        }
                    }
                }
                svf.VrEndRecord();
                LINE_CNT++;
            }
            printSubclass.put(subclassCd, subclassCd);
            grp1++;
        }
    }

    private void printPaternE2(Vrw32alp svf, Student student) {

        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        int grp1 = 1;
        final Map printSubclass = new HashMap();

        final Map jviewSubclassMap = (Map) _param._jviewGradeMap.get(student._grade);
        if (null == jviewSubclassMap) {
            return;
        }
        //科目
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclassCd;
            if (null == jviewSubclassMap || !jviewSubclassMap.containsKey(subclassCd)) {
                continue;
            }

            final int subclassLen = 2;
            final List setSubclassNameList = KNJ_EditKinsoku.getTokenList(subclassMst._subclassName, subclassLen);

            final List jviewGradeList = (List) jviewSubclassMap.get(subclassCd);

            int maxLine = jviewGradeList.size();
            if (!printSubclass.containsKey(subclassCd) && setSubclassNameList.size() > maxLine) {
                maxLine = setSubclassNameList.size();
            }

            checkLineAndPageChange(svf, maxLine, 40, student);
            for (int i = 0; i < maxLine; i++) {
                if (!printSubclass.containsKey(subclassCd) && i < setSubclassNameList.size()) {
                    svf.VrsOut("CLASS_NAME1", (String) setSubclassNameList.get(i));
                }
                svf.VrsOut("GRP1_1", String.valueOf(grp1));
                if (i < jviewGradeList.size()) {
                    final JviewGrade jviewGrade = (JviewGrade) jviewGradeList.get(i);
                    svf.VrsOut("CONTENT1", jviewGrade._viewName);

                    printJviewValue(svf, student, subclassCd, jviewGrade, "VALUE", _param._semester);
                }

                svf.VrEndRecord();
                LINE_CNT++;
            }
            printSubclass.put(subclassCd, subclassCd);
            grp1++;
        }
    }

    private void printPaternF(Vrw32alp svf, Student student) {

        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        int grp1 = 1;
        final Map printSubclass = new HashMap();

        final Map jviewSubclassMap = (Map) _param._jviewGradeMap.get(student._grade);
        final Map syokenMap = (Map) student._recordSyokenSemeMap.get(_param._semester);
        //科目
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclassCd;
            if (null == jviewSubclassMap || !jviewSubclassMap.containsKey(subclassCd)) {
                continue;
            }

            final int subclassLen = 2;
            final List setSubclassNameList = KNJ_EditKinsoku.getTokenList(subclassMst._subclassName, subclassLen);

            final List jviewGradeList = (List) jviewSubclassMap.get(subclassCd);

            //所見
            final List setRemarkList1 = getSyokenList(syokenMap, subclassCd, 30);

            int maxLine = jviewGradeList.size();
            if (!printSubclass.containsKey(subclassCd) && setSubclassNameList.size() > maxLine) {
                maxLine = setSubclassNameList.size();
            }
            if (!printSubclass.containsKey(subclassCd) && setRemarkList1.size() > maxLine) {
                maxLine = setRemarkList1.size();
            }

            checkLineAndPageChange(svf, maxLine, 40, student);
            for (int i = 0; i < maxLine; i++) {
                if (!printSubclass.containsKey(subclassCd) && i < setSubclassNameList.size()) {
                    svf.VrsOut("CLASS_NAME1", (String) setSubclassNameList.get(i));
                }
                svf.VrsOut("GRP1_1", String.valueOf(grp1));
                svf.VrsOut("GRP1_2", String.valueOf(grp1));
                if (i < jviewGradeList.size()) {
                    final JviewGrade jviewGrade = (JviewGrade) jviewGradeList.get(i);
                    svf.VrsOut("CONTENT1", jviewGrade._viewName);

                    if (null != student._jviewRecordSemeMap) {
                        if (student._jviewRecordSemeMap.containsKey(_param._semester)) {
                            final Map jviewRecSubMap = (Map) student._jviewRecordSemeMap.get(_param._semester);
                            if (null != jviewRecSubMap) {
                                if (jviewRecSubMap.containsKey(subclassCd)) {
                                    final Map jviewRecViewMap = (Map) jviewRecSubMap.get(subclassCd);
                                    if (jviewRecViewMap.containsKey(jviewGrade._viewCd)) {
                                        final JviewRecord jviewRecord = (JviewRecord) jviewRecViewMap.get(jviewGrade._viewCd);
                                        svf.VrsOut("VIEW", StringUtils.defaultString(jviewRecord._status));
                                    }
                                }
                            }
                        }
                    }
                }
                if (null != student._recordScoreSemeSdivMap) {
                    if (student._recordScoreSemeSdivMap.containsKey(_param._semester + "08")) {
                        final Map subClassMap = (Map) student._recordScoreSemeSdivMap.get(_param._semester + "08");
                        if (subClassMap.containsKey(subclassCd)) {
                            final ScoreData scoreData = (ScoreData) subClassMap.get(subclassCd);
                            svf.VrsOut("VALUE", scoreData._score);
                        }
                    }
                }
                if (i < setRemarkList1.size()) {
                    svf.VrsOut("CONTENT2", (String) setRemarkList1.get(i));
                }

                svf.VrEndRecord();
                LINE_CNT++;
            }
            printSubclass.put(subclassCd, subclassCd);
            grp1++;
        }
    }

    private void printPaternG1(Vrw32alp svf, Student student) {
        final String title1 = "普通教育に関する各教科・科目";
        final List setTitleList1 = KNJ_EditKinsoku.getTokenList(title1, 8);
        final String title2 = "選択教科";
        final List setTitleList2 = KNJ_EditKinsoku.getTokenList(title2, 8);

        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        int grp1 = 1;
        int grp2 = 1;
        final Map printClass = new HashMap();
        String befElectDiv = "";

        final Map scoreSubclassMap = (Map) student._recordScoreSemeSdivMap.get(_param._semester + "08");
        final Map syokenMap = (Map) student._recordSyokenSemeMap.get(_param._semester);
        int printTitleCnt = 0;
        //科目
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclassCd;
            if (null == scoreSubclassMap || !scoreSubclassMap.containsKey(subclassCd)) {
                continue;
            }
            if (!befElectDiv.equals(subclassMst._electDiv)) {
                grp1++;
                printTitleCnt = 0;
            }
            final List setTitleList = !"0".equals(subclassMst._electDiv) ? setTitleList2 : setTitleList1;
            if (printTitleCnt < setTitleList.size()) {
                svf.VrsOut("CLASS_DIV", (String) setTitleList.get(printTitleCnt));
                printTitleCnt++;
            }
            svf.VrsOut("CLASS_NAME1", subclassMst._className);
            if (!printClass.containsKey(subclassMst._classCd)) {
                grp2++;
            }
            final String subclassField = KNJ_EditEdit.getMS932ByteLength(subclassMst._subclassName) > 18 ? "2_1" : "1";
            svf.VrsOut("SUBCLASS_NAME" + subclassField, subclassMst._subclassName);

            final String setCredit = (String) _param._creditMap.get(student._grade + student._courseCd + student._majorCd + student._courseCode + subclassCd);
            svf.VrsOut("CREDIT", StringUtils.defaultString(setCredit));

            final ScoreData scoreData = (ScoreData) scoreSubclassMap.get(subclassCd);
            svf.VrsOut("VALUE", StringUtils.defaultString(scoreData._score));

            if (student._attendSubClassMap.containsKey(subclassCd)) {
                final Map atSubSemeMap = (Map) student._attendSubClassMap.get(subclassCd);
                if (atSubSemeMap.containsKey(_param._semester)) {
                    final SubclassAttendance attendance = (SubclassAttendance) atSubSemeMap.get(_param._semester);
                    svf.VrsOut("ABSENCE", attendance._sick.toString());
                }
            }

            //所見
            final List setRemarkList1 = getSyokenList(syokenMap, subclassCd, 36);
            int syokenCnt = 1;
            for (Iterator itSyoken = setRemarkList1.iterator(); itSyoken.hasNext();) {
                final String setSyoken = (String) itSyoken.next();
                svf.VrsOut("CONTENT" + syokenCnt, setSyoken);
                syokenCnt++;
            }
            svf.VrsOut("GRP1_1", String.valueOf(grp1));
            svf.VrsOut("GRP1_2", String.valueOf(grp2));

            svf.VrEndRecord();
            LINE_CNT++;

            printClass.put(subclassMst._classCd, subclassMst._classCd);
            befElectDiv = subclassMst._electDiv;
        }
    }

    private void printPaternG2(Vrw32alp svf, Student student) {
        final String title1 = "普通教育に関する各教科・科目";
        final List setTitleList1 = KNJ_EditKinsoku.getTokenList(title1, 8);
        final String title2 = "選択教科";
        final List setTitleList2 = KNJ_EditKinsoku.getTokenList(title2, 8);

        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        int grp1 = 1;
        int grp2 = 1;
        final Map printClass = new HashMap();
        String befElectDiv = "";
        final Map scoreSubclassMap108 = (Map) student._recordScoreSemeSdivMap.get("108");
        final Map scoreSubclassMap109 = (Map) student._recordScoreSemeSdivMap.get("109");

        final Map scoreSubclassMap908 = (Map) student._recordScoreSemeSdivMap.get("908");
        final Map scoreSubclassMap909 = (Map) student._recordScoreSemeSdivMap.get("909");
        int printTitleCnt = 0;

        //合計
        int seme1TotalScore = 0;
        int seme1Cnt = 0;
        int seme9TotalScore = 0;
        int seme9Cnt = 0;
        int seme9TotalValue = 0;
        int seme9ValCnt = 0;
        //科目
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclassCd;
            if ((null == scoreSubclassMap108 || !scoreSubclassMap108.containsKey(subclassCd)) &&
                (null == scoreSubclassMap908 || !scoreSubclassMap908.containsKey(subclassCd))
            ) {
                continue;
            }
            if (!befElectDiv.equals(subclassMst._electDiv)) {
                grp1++;
                printTitleCnt = 0;
            }
            final List setTitleList = !"0".equals(subclassMst._electDiv) ? setTitleList2 : setTitleList1;
            if (printTitleCnt < setTitleList.size()) {
                svf.VrsOut("CLASS_DIV", (String) setTitleList.get(printTitleCnt));
                printTitleCnt++;
            }
            svf.VrsOut("CLASS_NAME1", subclassMst._className);
            if (!printClass.containsKey(subclassMst._classCd)) {
                grp2++;
            }
            final String subclassField = KNJ_EditEdit.getMS932ByteLength(subclassMst._subclassName) > 18 ? "2_1"
                    : "1";
            svf.VrsOut("SUBCLASS_NAME" + subclassField, subclassMst._subclassName);

            final String setCredit = (String) _param._creditMap.get(student._grade + student._courseCd + student._majorCd + student._courseCode + subclassCd);
            svf.VrsOut("CREDIT", StringUtils.defaultString(setCredit));

            //1学期
            final String[] score1 = printScoreAttend(svf, student, subclassCd, "1", scoreSubclassMap108, scoreSubclassMap109);
            if (!StringUtils.isEmpty(score1[0])) {
                seme1TotalScore += Integer.parseInt(score1[0]);
                seme1Cnt++;
            }

            //9学期
            final String[] score2 = printScoreAttend(svf, student, subclassCd, "9", scoreSubclassMap908, scoreSubclassMap909);
            if (!StringUtils.isEmpty(score2[0])) {
                seme9TotalScore += Integer.parseInt(score2[0]);
                seme9Cnt++;
            }
            if (!StringUtils.isEmpty(score2[1])) {
                seme9TotalValue += Integer.parseInt(score2[1]);
                seme9ValCnt++;
            }

            svf.VrsOut("GRP1_1", String.valueOf(grp1));
            svf.VrsOut("GRP1_2", String.valueOf(grp2));

            svf.VrEndRecord();
            LINE_CNT++;

            printClass.put(subclassMst._classCd, subclassMst._classCd);
            befElectDiv = subclassMst._electDiv;
        }
        svf.VrsOut("TOTAL_NAME", "合計");
        svf.VrsOut("TOTAL_VALUE1", String.valueOf(seme1TotalScore));
        svf.VrsOut("TOTAL_VALUE9", String.valueOf(seme9TotalScore));
        svf.VrsOut("TOTAL_DEVI", String.valueOf(seme9TotalValue));
        svf.VrEndRecord();

        svf.VrsOut("TOTAL_NAME", "平均");
        if (0 < seme1Cnt) {
        	final BigDecimal setAvg1 = new BigDecimal(seme1TotalScore).divide(new BigDecimal(seme1Cnt), 0,  BigDecimal.ROUND_HALF_UP);
        	svf.VrsOut("TOTAL_VALUE1", setAvg1.toString());
        }
        if (0 < seme9Cnt) {
        	final BigDecimal setAvg9 = new BigDecimal(seme9TotalScore).divide(new BigDecimal(seme9Cnt), 0,  BigDecimal.ROUND_HALF_UP);
        	svf.VrsOut("TOTAL_VALUE9", setAvg9.toString());
        }
        if (0 < seme9ValCnt) {
            final BigDecimal setAvgVal = new BigDecimal(seme9TotalValue).divide(new BigDecimal(seme9ValCnt), 0,  BigDecimal.ROUND_HALF_UP);
            svf.VrsOut("TOTAL_DEVI", setAvgVal.toString());
        }
        svf.VrEndRecord();
    }

    private String[] printScoreAttend(final Vrw32alp svf, final Student student, final String subclassCd, final String semester, final Map scoreSubclassMap08, final Map scoreSubclassMap09) {
        String[] retStr = {"", ""};
        if (null != scoreSubclassMap08) {
        	//評点
        	final ScoreData scoreData08 = (ScoreData) scoreSubclassMap08.get(subclassCd);
        	if (null != scoreData08) {
        		svf.VrsOut("VALUE" + semester, StringUtils.defaultString(scoreData08._score));
        		retStr[0] = StringUtils.defaultString(scoreData08._score);
        	}
        }

        if (null != scoreSubclassMap09) {
        	//評定
        	final ScoreData scoreData09 = (ScoreData) scoreSubclassMap09.get(subclassCd);
        	if ("9".equals(semester) && null != scoreData09) {
        		svf.VrsOut("DEVI", StringUtils.defaultString(scoreData09._score));
        		retStr[1] = StringUtils.defaultString(scoreData09._score);
        	}
        }

        printAttend(svf, student, subclassCd, semester, "ABSENCE" + semester);
        return retStr;
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

    private void printPaternJ(Vrw32alp svf, Student student) {

        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        int grp1 = 1;
        final Map printSubclass = new HashMap();

        final Map jviewSubclassMap = (Map) _param._jviewGradeMap.get(student._grade);
        if (null == jviewSubclassMap) {
            return;
        }
        //科目
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclassCd;
            if (null == jviewSubclassMap || !jviewSubclassMap.containsKey(subclassCd)) {
                continue;
            }

            final int subclassLen = 2;
            final List setSubclassNameList = KNJ_EditKinsoku.getTokenList(subclassMst._subclassName, subclassLen);

            final List jviewGradeList = (List) jviewSubclassMap.get(subclassCd);

            int maxLine = jviewGradeList.size();
            if (!printSubclass.containsKey(subclassCd) && setSubclassNameList.size() > maxLine) {
                maxLine = setSubclassNameList.size();
            }

            checkLineAndPageChange(svf, maxLine, 40, student);
            for (int i = 0; i < maxLine; i++) {
                if (!printSubclass.containsKey(subclassCd) && i < setSubclassNameList.size()) {
                    svf.VrsOut("CLASS_NAME1", (String) setSubclassNameList.get(i));
                }
                svf.VrsOut("GRP1_1", String.valueOf(grp1));
                if (i < jviewGradeList.size()) {
                    final JviewGrade jviewGrade = (JviewGrade) jviewGradeList.get(i);
                    svf.VrsOut("CONTENT1", jviewGrade._viewName);

                    printJviewValue(svf, student, subclassCd, jviewGrade, "VALUE1", "1");
                    printJviewValue(svf, student, subclassCd, jviewGrade, "VALUE2", "2");
                    printJviewValue(svf, student, subclassCd, jviewGrade, "VALUE3", "3");
                }

                svf.VrEndRecord();
                LINE_CNT++;
            }
            printSubclass.put(subclassCd, subclassCd);
            grp1++;
        }
    }

    private void printPaternK(Vrw32alp svf, Student student) {

        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        int grp1 = 1;
        final Map printClass = new HashMap();
        final Map syokenMap1 = (Map) student._recordSyokenSemeMap.get("1");
        final Map syokenMap2 = (Map) student._recordSyokenSemeMap.get("2");
        final Map syokenMap3 = (Map) student._recordSyokenSemeMap.get("3");

        final Map scoreSubclassMap108 = (Map) student._recordScoreSemeSdivMap.get("108");
        final Map scoreSubclassMap208 = (Map) student._recordScoreSemeSdivMap.get("208");
        final Map scoreSubclassMap308 = (Map) student._recordScoreSemeSdivMap.get("308");

        final Map jviewSubclassMap = (Map) _param._jviewGradeMap.get(student._grade);
        if (null == jviewSubclassMap) {
            return;
        }
        //科目
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclassCd;
            final String classCd = subclassMst._classCd;
            if (null == jviewSubclassMap || !jviewSubclassMap.containsKey(subclassCd)) {
                continue;
            }

            final List setClassNameList = KNJ_EditKinsoku.getTokenList(subclassMst._className, 2);

            final List jviewGradeList = (List) jviewSubclassMap.get(subclassCd);

            //所見1
            final List setRemarkList1 = getSyokenList(syokenMap1, subclassCd, 12).size() <= 8 ? getSyokenList(syokenMap1, subclassCd, 12) : getSyokenList(syokenMap1, subclassCd, 12).subList(0, 8);
            final int remark1Len = getSyoken2GyouCnt(setRemarkList1);
            //所見2
            final List setRemarkList2 = getSyokenList(syokenMap2, subclassCd, 12).size() <= 8 ? getSyokenList(syokenMap2, subclassCd, 12) : getSyokenList(syokenMap2, subclassCd, 12).subList(0, 8);
            final int remark2Len = getSyoken2GyouCnt(setRemarkList2);
            //所見3
            final List setRemarkList3 = getSyokenList(syokenMap3, subclassCd, 12).size() <= 8 ? getSyokenList(syokenMap3, subclassCd, 12) : getSyokenList(syokenMap3, subclassCd, 12).subList(0, 8);
            final int remark3Len = getSyoken2GyouCnt(setRemarkList3);

            int maxLine = jviewGradeList.size();
            if (setClassNameList.size() > maxLine) {
                maxLine = setClassNameList.size();
            }
            if (remark1Len > maxLine) {
                maxLine = remark1Len;
            }
            if (remark2Len > maxLine) {
                maxLine = remark2Len;
            }
            if (remark3Len > maxLine) {
                maxLine = remark3Len;
            }

            checkLineAndPageChange(svf, maxLine, 37, student);
            int syokenCnt = 0;
            for (int i = 0; i < maxLine; i++) {
                if (i < setClassNameList.size()) {
                    svf.VrsOut("CLASS_NAME1", (String) setClassNameList.get(i));
                }
                svf.VrsOut("GRP1_1", String.valueOf(grp1));
                svf.VrsOut("GRP2_2", String.valueOf(grp1));
                svf.VrsOut("GRP3_2", String.valueOf(grp1));
                svf.VrsOut("GRP4_2", String.valueOf(grp1));
                if (i < jviewGradeList.size()) {
                    final JviewGrade jviewGrade = (JviewGrade) jviewGradeList.get(i);
                    final String contentField = KNJ_EditEdit.getMS932ByteLength(jviewGrade._viewName) > 32 ? "2" : "1";
                    svf.VrsOut("CONTENT" + contentField, jviewGrade._viewName);

                    printJviewValue(svf, student, subclassCd, jviewGrade, "VIEW1", "1");
                    printJviewValue(svf, student, subclassCd, jviewGrade, "VIEW2", "2");
                    printJviewValue(svf, student, subclassCd, jviewGrade, "VIEW3", "3");
                }
                //評点
                printScoreValue(svf, subclassCd, scoreSubclassMap108, "VALUE1", "1");
                printScoreValue(svf, subclassCd, scoreSubclassMap208, "VALUE2", "2");
                printScoreValue(svf, subclassCd, scoreSubclassMap308, "VALUE3", "3");

                //所見
                printSyoken2Gyou(svf, setRemarkList1, syokenCnt, "1");
                printSyoken2Gyou(svf, setRemarkList2, syokenCnt, "2");
                printSyoken2Gyou(svf, setRemarkList3, syokenCnt, "3");
                syokenCnt = syokenCnt + 2;

                svf.VrEndRecord();
                LINE_CNT++;
            }
            printClass.put(classCd, classCd);
            grp1++;
        }

    }

    private void printPaternL(Vrw32alp svf, Student student) {

        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        final Map scoreSubclassMap108 = (Map) student._recordScoreSemeSdivMap.get("108");
        final Map scoreSubclassMap208 = (Map) student._recordScoreSemeSdivMap.get("208");
        final Map scoreSubclassMap908 = (Map) student._recordScoreSemeSdivMap.get("908");

        int seme1TotalScore = 0;
        int seme1Cnt = 0;
        int seme2TotalScore = 0;
        int seme2Cnt = 0;
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
                (null == scoreSubclassMap908 || !scoreSubclassMap908.containsKey(subclassCd))
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
            final int[] score3 = getScore(svf, subclassCd, scoreSubclassMap908);
            seme9TotalScore += score3[0];
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
            if (seme9Cnt != 0) {
                final BigDecimal setAvgVal = new BigDecimal(seme9TotalScore).divide(new BigDecimal(seme9Cnt), 0,  BigDecimal.ROUND_HALF_UP);
                svf.VrsOut("AVERAGE9", setAvgVal.toString());
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
                (null == scoreSubclassMap908 || !scoreSubclassMap908.containsKey(subclassCd))
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
            printScoreValue(svf, subclassCd, scoreSubclassMap108, "VALUE1", "1");
            printScoreValue(svf, subclassCd, scoreSubclassMap208, "VALUE2", "2");
            printScoreValue(svf, subclassCd, scoreSubclassMap908, "VALUE9", "3");

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

    private void printTokubetsuKamoku(final Vrw32alp svf, final Student student, final String tokubetsuKamoku, final String subclassCd, final int line) {
        if (subclassCd.equals(tokubetsuKamoku)) {
            printCredit(svf, student, subclassCd, "CREDIT2", line);
            printAttendPaternLTokubetsu(svf, student, subclassCd, "9", "ABSENCE2", line);
        }
    }

    private void printCredit(final Vrw32alp svf, final Student student, final String subclassCd, final String fieldName, final int line) {
        final String setCredit = (String) _param._creditMap.get(student._grade + student._courseCd + student._majorCd + student._courseCode + subclassCd);
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

    private void printJviewValue(final Vrw32alp svf, Student student, final String subclassCd, final JviewGrade jviewGrade, final String fieldName, final String semester) {
        if (Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
            return;
        }
        if (null != student._jviewRecordSemeMap) {
            if (student._jviewRecordSemeMap.containsKey(semester)) {
                final Map jviewRecSubMap = (Map) student._jviewRecordSemeMap.get(semester);
                if (null != jviewRecSubMap) {
                    if (jviewRecSubMap.containsKey(subclassCd)) {
                        final Map jviewRecViewMap = (Map) jviewRecSubMap.get(subclassCd);
                        if (jviewRecViewMap.containsKey(jviewGrade._viewCd)) {
                            final JviewRecord jviewRecord = (JviewRecord) jviewRecViewMap.get(jviewGrade._viewCd);
                            svf.VrsOut(fieldName, StringUtils.defaultString(jviewRecord._statusName));
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

    private List getSyokenList(final Map syokenMap, final String subclassCd, final int syokenLen) {
        final RecordSyoken recordSyoken;
        if (null == syokenMap) {
            recordSyoken = new RecordSyoken("", "", "");
        } else if (syokenMap.containsKey(subclassCd)) {
            recordSyoken = (RecordSyoken) syokenMap.get(subclassCd);
        } else {
            recordSyoken = new RecordSyoken("", "", "");
        }
        //所見
        final String remark = StringUtils.defaultString(recordSyoken._remark1);
        return KNJ_EditKinsoku.getTokenList(remark, syokenLen);
    }

    private int getSyoken2GyouCnt(final List syokenList) {
        final int remarkLen = syokenList.size() / 2;
        final int remarkAmari = syokenList.size() % 2;
        final int retRemarkLen = remarkAmari > 0 ? remarkLen + 1 : remarkLen;
        return retRemarkLen;
    }

    private void printSyoken2Gyou(final Vrw32alp svf, final List setRemarkList, final int syokenCnt, final String semester) {
        if (Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
            return;
        }
        if (syokenCnt < setRemarkList.size()) {
            svf.VrsOut("OPINION" + semester + "_1", (String) setRemarkList.get(syokenCnt));
            if ((syokenCnt + 1) < setRemarkList.size()) {
                svf.VrsOut("OPINION" + semester + "_2", (String) setRemarkList.get(syokenCnt + 1));
            }
        }
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
                final String ghrCd = rs.getString("GHR_CD");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String gakubuName = rs.getString("GAKUBU_NAME");
                final String hrName = rs.getString("HR_NAME");
                final String courseCd = rs.getString("COURSECD");
                final String majorCd = rs.getString("MAJORCD");
                final String courseCode = rs.getString("COURSECODE");
                final String tannin1 = rs.getString("TANNIN1");
                final String tannin2 = rs.getString("TANNIN2");
                final String tannin3 = rs.getString("TANNIN3");
                final String tannin4 = rs.getString("TANNIN4");
                final String ghrName = rs.getString("GHR_NAME");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String birthDay = rs.getString("BIRTHDAY");
                final String sexName = rs.getString("SEX_NAME");

                final Student student = new Student(schregNo, schoolKind, gradeName1, ghrCd, grade, hrClass, gakubuName, hrName, courseCd, majorCd, courseCode, tannin1, tannin2, tannin3, tannin4, ghrName, name, nameKana, birthDay, sexName);
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
                student.setHreportRemarkDetail(db2);
                student.setBehaviorSeme(db2);
                student.setMedexam(db2);
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
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.COURSECD, ");
        stb.append("     REGD.MAJORCD, ");
        stb.append("     REGD.COURSECODE, ");
        stb.append("     TANNIN1.STAFFNAME AS TANNIN1, ");
        stb.append("     TANNIN2.STAFFNAME AS TANNIN2, ");
        stb.append("     TANNIN3.STAFFNAME AS TANNIN3, ");
        stb.append("     TANNIN4.STAFFNAME AS TANNIN4, ");
        stb.append("     GHR.GHR_CD, ");
        stb.append("     GHRH.GHR_NAME, ");
        stb.append("     A023.ABBV1 AS GAKUBU_NAME, ");
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
        stb.append("     LEFT JOIN SCHREG_REGD_GHR_DAT GHR ON GHR.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                                     AND GHR.YEAR =REGD.YEAR ");
        stb.append("                                     AND GHR.SEMESTER = REGD.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GHR_HDAT GHRH ON GHRH.YEAR = GHR.YEAR ");
        stb.append("                                      AND GHRH.SEMESTER = GHR.SEMESTER ");
        stb.append("                                      AND GHRH.GHR_CD = GHR.GHR_CD ");
        stb.append("     LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ");
        stb.append("                            AND A023.NAME1 = GDAT.SCHOOL_KIND ");
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
        final String _ghrCd;
        final String _grade;
        final String _hrClass;
        final String _gakubuName;
        final String _hrName;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _tannin1;
        final String _tannin2;
        final String _tannin3;
        final String _tannin4;
        final String _ghrName;
        final String _name;
        final String _nameKana;
        final String _birthDay;
        final String _sexName;
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
        final Map _attendSubClassMap = new TreeMap();
        final Map _hreportRemarkDatMap = new TreeMap();
        int _totalGetCredit = 0;
        String _sougakuSubClassCd;
        String _tokkatsuSubClassCd;
        String _jikatsuSubClassCd;

        public Student(final String schregNo, final String schoolKind, final String gradeName1, final String ghrCd, final String grade,
                final String hrClass, final String gakubuName, final String hrName, final String courseCd, final String majorCd, final String courseCode,
                final String tannin1, final String tannin2, final String tannin3, final String tannin4, final String ghrName, final String name,
                final String nameKana, final String birthDay, final String sexName) {
            _schregNo = schregNo;
            _schoolKind = schoolKind;
            _gradeName1 = gradeName1;
            _ghrCd = StringUtils.isEmpty(ghrCd) ? "00" : ghrCd;
            _grade = grade;
            _hrClass = hrClass;
            _gakubuName = gakubuName;
            _hrName = hrName;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _tannin1 = StringUtils.defaultString(tannin1);
            _tannin2 = StringUtils.defaultString(tannin2);
            _tannin3 = StringUtils.defaultString(tannin3);
            _tannin4 = StringUtils.defaultString(tannin4);
            _ghrName = ghrName;
            _name = name;
            _nameKana = nameKana;
            _birthDay = birthDay;
            _sexName = sexName;
            _blockMap = new TreeMap();
            _behaviorSemeMap = new TreeMap();
        }

        private String getHrName() {
            if (HOUTEI.equals(_param._hukusikiRadio)) {
                return _hrName;
            } else {
                return _ghrName;
            }
        }

        private void setMedexam(final DB2UDB db2) throws SQLException {
            final String medexamMonthSql = getMedexamMonthSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(medexamMonthSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String month = rs.getString("MONTH");
                    final String height = rs.getString("HEIGHT");
                    final String weight = rs.getString("WEIGHT");
                    final String rBarevisionMark = rs.getString("R_BAREVISION_MARK");
                    final String rVisionMark = rs.getString("R_VISION_MARK");
                    final String lBarevisionMark = rs.getString("L_BAREVISION_MARK");
                    final String lVisionMark = rs.getString("L_VISION_MARK");
                    final MedexamMonth medexamMonth = new MedexamMonth(semester, month, height, weight, rBarevisionMark, rVisionMark, lBarevisionMark, lVisionMark);
                    _medexamMonthList.add(medexamMonth);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            final String medexamSql = getMedexamDetSql();
            ps = null;
            rs = null;
            try {
                ps = db2.prepareStatement(medexamSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String rEar = rs.getString("R_EAR");
                    final String rEarIn = rs.getString("R_EAR_IN");
                    final String lEar = rs.getString("L_EAR");
                    final String lEarIn = rs.getString("L_EAR_IN");
                    _medexamdet = new Medexamdet(rEar, rEarIn, lEar, lEarIn);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

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
                    final HreportRemarkDat hreportRemarkDat = new HreportRemarkDat(totalstudytime, specialactremark, communication, remark1, remark2, remark3, foreignlangact, attendrecRemark);
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
            stb.append("     COMMUNICATION, ");
            stb.append("     REMARK1, ");
            stb.append("     REMARK2, ");
            stb.append("     REMARK3, ");
            stb.append("     FOREIGNLANGACT, ");
            stb.append("     ATTENDREC_REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO = '" + _schregNo + "' ");
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
            if (_param._is2Gakki && "02".equals(div) && "01".equals(code)) {
                stb.append("     AND SEMESTER = '" + _param._semester + "' ");
            } else {
                stb.append("     AND SEMESTER = '9' ");
            }
            stb.append("     AND SCHREGNO = '" + _schregNo + "' ");
            stb.append("     AND DIV = '" + div + "' ");
            stb.append("     AND CODE = '" + code + "' ");

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
            if ("F".equals(_param._frmPatern) || "K".equals(_param._frmPatern)) {
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
            log.fatal(scoreSql);
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
                    final JviewRecord jviewRecord = new JviewRecord(subclassCd, semester, viewCd, status, statusName);
                    final Map setSubMap;
                    if (_jviewRecordSemeMap.containsKey(semester)) {
                        setSubMap = (Map) _jviewRecordSemeMap.get(semester);
                    } else {
                        setSubMap = new HashMap();
                    }
                    final Map jviewMap;
                    if (setSubMap.containsKey(subclassCd)) {
                        jviewMap = (Map) setSubMap.get(subclassCd);
                    } else {
                        jviewMap = new HashMap();
                    }
                    jviewMap.put(viewCd, jviewRecord);
                    setSubMap.put(subclassCd, jviewMap);
                    _jviewRecordSemeMap.put(semester, setSubMap);
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
            stb.append(" SELECT ");
            stb.append("     REC.CLASSCD || '-' || REC.SCHOOL_KIND || '-' || REC.CURRICULUM_CD || '-' || REC.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     REC.SEMESTER, ");
            stb.append("     REC.VIEWCD, ");
            stb.append("     REC.STATUS, ");
            stb.append("     D029.NAMESPARE1 AS MARK ");
            stb.append(" FROM ");
            stb.append("     JVIEWSTAT_RECORD_DAT REC ");
            stb.append("     LEFT JOIN NAME_MST D029 ON D029.NAMECD1 = 'D029' ");
            stb.append("          AND REC.STATUS = D029.ABBV1 ");
            stb.append(" WHERE ");
            stb.append("     REC.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND REC.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     REC.SEMESTER, ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     VIEWCD ");

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
                    final String record = rs.getString("RECORD");
                    _behaviorSemeMap.put(code, record);
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
            stb.append("     BEHAVIOR.CODE, ");
            stb.append("     D036.NAMESPARE1 AS RECORD ");
            stb.append(" FROM ");
            stb.append("     BEHAVIOR_SEMES_DAT BEHAVIOR ");
            stb.append("     LEFT JOIN NAME_MST D036 ON D036.NAMECD1 = 'D036' ");
            stb.append("          AND BEHAVIOR.RECORD = D036.NAME1 ");
            stb.append(" WHERE ");
            stb.append("     BEHAVIOR.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND BEHAVIOR.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND BEHAVIOR.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     BEHAVIOR.CODE ");

            return stb.toString();
        }

        private String getMedexamMonthSql() {
            final Map conditionMap = (Map) _param._hreportConditionMap.get(_schoolKind);
            final HreportCondition hreportCondition5;
            if (null != conditionMap) {
                hreportCondition5 = (HreportCondition) conditionMap.get("205");
            } else {
                hreportCondition5 = new HreportCondition("", "", "", "", "", "", "", "", "", "");
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER, ");
            stb.append("     MONTH, ");
            stb.append("     HEIGHT, ");
            stb.append("     WEIGHT, ");
            stb.append("     R_BAREVISION_MARK, ");
            stb.append("     R_VISION_MARK, ");
            stb.append("     L_BAREVISION_MARK, ");
            stb.append("     L_VISION_MARK ");
            stb.append(" FROM ");
            stb.append("     MEDEXAM_DET_MONTH_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO = '" + _schregNo + "' ");
            stb.append("     AND SEMESTER || MONTH IN ('" + hreportCondition5._remark1 + "', '" + hreportCondition5._remark2 + "') ");
            stb.append(" ORDER BY ");
            stb.append("     SEMESTER, INT(MONTH) + CASE WHEN INT(MONTH) < 4 THEN 12 ELSE 0 END ");

            return stb.toString();
        }

        private String getMedexamDetSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     F010R1.NAME1 AS R_EAR, ");
            stb.append("     F010R2.NAME1 AS R_EAR_IN, ");
            stb.append("     F010L1.NAME1 AS L_EAR, ");
            stb.append("     F010L2.NAME1 AS L_EAR_IN ");
            stb.append(" FROM ");
            stb.append("     V_MEDEXAM_DET_DAT MEDEXAM ");
            stb.append("     LEFT JOIN NAME_MST F010R1 ON F010R1.NAMECD1 = 'F010' ");
            stb.append("          AND MEDEXAM.R_EAR = F010R1.NAMECD2 ");
            stb.append("     LEFT JOIN NAME_MST F010R2 ON F010R2.NAMECD1 = 'F010' ");
            stb.append("          AND MEDEXAM.R_EAR_IN = F010R2.NAMECD2 ");
            stb.append("     LEFT JOIN NAME_MST F010L1 ON F010L1.NAMECD1 = 'F010' ");
            stb.append("          AND MEDEXAM.L_EAR = F010L1.NAMECD2 ");
            stb.append("     LEFT JOIN NAME_MST F010L2 ON F010L2.NAMECD1 = 'F010' ");
            stb.append("          AND MEDEXAM.L_EAR_IN = F010L2.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     MEDEXAM.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND MEDEXAM.SCHREGNO = '" + _schregNo + "' ");
            stb.append("  ");

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

        private static String getHreportRemarkSql(final Param param, final DateRange dateRange) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TOTALSTUDYTIME, ");
            stb.append("     SPECIALACTREMARK, ");
            stb.append("     COMMUNICATION, ");
            stb.append("     REMARK1, ");
            stb.append("     REMARK2, ");
            stb.append("     REMARK3, ");
            stb.append("     FOREIGNLANGACT, ");
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
        private JviewRecord(
                final String subclassCd,
                final String semester,
                final String viewCd,
                final String status,
                final String statusName
        ) {
            _subclassCd = subclassCd;
            _semester = semester;
            _viewCd = viewCd;
            _status = status;
            _statusName = statusName;
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

        public HreportRemarkDat(
                final String totalstudytime,
                final String specialactremark,
                final String communication,
                final String remark1,
                final String remark2,
                final String remark3,
                final String foreignlangact,
                final String attendrecRemark
        ) {
            _totalstudytime = totalstudytime;
            _specialactremark = specialactremark;
            _communication = communication;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _foreignlangact = foreignlangact;
            _attendrecRemark = attendrecRemark;
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
        log.fatal("$Revision: 72621 $");
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
        final String _seisekiFrm;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _prgid;
        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolCd;
        final String _selectGhr;
        final String _printLogStaffcd;
        final String _printLogRemoteIdent;
        final String _schoolName;
        final String _recordDate;
        final String _printSize;
        final Map _semesterMap;
        final boolean _is2Gakki;
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

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _hukusikiRadio = request.getParameter("HUKUSIKI_RADIO");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _ghrCd = request.getParameter("GHR_CD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _moveDate = request.getParameter("MOVE_DATE");
            _isPrintHyosi = "1".equals(request.getParameter("HYOSI"));
            final String paraFrmPatern = request.getParameter("FRM_PATERN");
            _frmPatern = getFormPatern(db2, paraFrmPatern);
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
            _schoolCd = request.getParameter("SCHOOLCD");
            _selectGhr = request.getParameter("SELECT_GHR");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
            _printSize = request.getParameter("PRINTSIZE");
            _schoolName = getSchoolName(db2);
            _recordDate = "9999-03-31";
            _semesterMap = loadSemester(db2);
            _is2Gakki = _semesterMap.size() == 3;
            _seisekiFrm = getSeisekiFrm();
            setSubclassMst(db2);
            _creditMap = getCreditMap(db2);
            _jviewGradeMap = getJviewGradeMap(db2);
            _hreportConditionMap = getHreportConditionMap(db2);
            _certifSchoolMap = getCertifSchoolMap(db2);
            _d035 = getVNameMst(db2, "D035");
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
            String retFrm = "";
            if ("E1".equals(_frmPatern)) {
                retFrm = "KNJD429A_2_E_1.frm";
            } else if ("E2".equals(_frmPatern)) {
                retFrm = "KNJD429A_2_E_2.frm";
            } else if ("F".equals(_frmPatern)) {
                retFrm = "KNJD429A_2_F.frm";
            } else if ("G1".equals(_frmPatern)) {
            	retFrm = "KNJD429A_2_G_1.frm";
            } else if ("G2".equals(_frmPatern)) {
                retFrm = "KNJD429A_2_G_2.frm";
            } else if ("J".equals(_frmPatern)) {
                retFrm = "KNJD429A_2_J.frm";
            } else if ("K".equals(_frmPatern)) {
                retFrm = "KNJD429A_2_K.frm";
            } else if ("L".equals(_frmPatern)) {
                retFrm = "KNJD429A_2_L.frm";
            }
            if (StringUtils.isBlank(retFrm)) {
            	log.info(" seisekiFrm null : frmPatern = " + _frmPatern);
            }
            return retFrm;
        }

        public String getGakushuFrm(final Student student) {
            String retFrm = "";
            if (_is2Gakki) {
                if ("P".equals(student._schoolKind)) {
                	if ("01".equals(student._grade) || "02".equals(student._grade)) {
                        retFrm = "KNJD429A_3_1_1_1.frm";
                	} else if ("03".equals(student._grade) || "04".equals(student._grade)) {
                        retFrm = "KNJD429A_3_1_1_2.frm";
                	} else { // if ("05".equals(student._grade) || "06".equals(student._grade)) {
                        retFrm = "KNJD429A_3_1_1_3.frm";
                	}
                } else if ("J".equals(student._schoolKind)) {
                    retFrm = "KNJD429A_3_1_2.frm";
                } else if ("H".equals(student._schoolKind)) {
                    retFrm = "KNJD429A_3_1_3.frm";
                }
            } else {
                if ("P".equals(student._schoolKind)) {
                	if ("01".equals(student._grade) || "02".equals(student._grade)) {
                        retFrm = "KNJD429A_3_2_J_1.frm";
                	} else if ("03".equals(student._grade) || "04".equals(student._grade)) {
                        retFrm = "KNJD429A_3_2_J_2.frm";
                	} else { // if ("05".equals(student._grade) || "06".equals(student._grade)) {
                        retFrm = "KNJD429A_3_2_J_3.frm";
                	}
                } else if ("J".equals(student._schoolKind)) {
                    retFrm = "KNJD429A_3_2_K.frm";
                } else if ("H".equals(student._schoolKind)) {
                    retFrm = "KNJD429A_3_2_L.frm";
                }
            }

            return retFrm;
        }

        public String getAttendFrm(final Student student) {
            String retFrm = "";
            final Map conditionMap = (Map) _param._hreportConditionMap.get(student._schoolKind);
            final String semeField = _is2Gakki ? "1" : "2";
            if (null == conditionMap) {
            	log.info(" conditionMap null !");
            } else {
                final HreportCondition hreportCondition6 = (HreportCondition) conditionMap.get("206");
                final HreportCondition hreportCondition7 = (HreportCondition) conditionMap.get("207");
                if ((null == hreportCondition6 || (null != hreportCondition6 && "".equals(hreportCondition6._remark1))) &&
                    (null == hreportCondition7 || (null != hreportCondition7 && "".equals(hreportCondition7._remark1)))
                ) {
                    retFrm = "KNJD429A_4_" + semeField + "_1.frm";
                } else if ((null != hreportCondition6 && "1".equals(hreportCondition6._remark1)) &&
                            (null != hreportCondition7 && "1".equals(hreportCondition7._remark1))
                ) {
                    retFrm = "KNJD429A_4_" + semeField + "_4.frm";
                } else if ((null == hreportCondition6 || (null != hreportCondition6 && "1".equals(hreportCondition6._remark1)))) {
                    retFrm = "KNJD429A_4_" + semeField + "_3.frm";
                } else {
                    retFrm = "KNJD429A_4_" + semeField + "_2.frm";
                }
            }

            return retFrm;
        }

        private String getSchoolName(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '103' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            String retSchoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOL_NAME");
                }
            } catch (SQLException ex) {
                log.error("certif_school_dat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
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

    }
}

// eof

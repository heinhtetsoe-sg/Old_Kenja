/*
 * 作成日: 2011/01/12
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJPropertiesShokenSize;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理] 京都府中学通知票
 */

public class KNJD181J {

    private static final Log log = LogFactory.getLog(KNJD181J.class);

    private static final String SEME_ALL = "9";

    private boolean _hasData;
    private boolean _hasDataForm3 = false;

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

        final List viewClassList = ViewClass.getViewClassList(db2, _param);

        final List studentList = Student.getStudentList(db2, _param);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.fatal(" schregno = " + student._schregno);

            if (_param._isPrintSyuryo) {
                // 修了証
                printSyuryo(db2, svf, student);
                _hasData = true;
            }
            if (_param._isPrintHyoshi) {
                // 表紙
                printHyoshi(db2, svf, student);
                _hasData = true;
            }
            if (_param._isPrintSeiseki) {
                // 学習のようす等
                printSeiseki(svf, student, viewClassList);
                _hasData = true;
            }
            if (_param._isPrintSyoken) {
                // 所見等
                printSyoken(svf, student);
                _hasData = true;
            }
        }
    }

    protected void VrsOutRenban(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOut(field + String.valueOf(i + 1), (String) list.get(i));
            }
        }
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOutn(field, i + 1, (String) list.get(i));
            }
        }
    }

    private static int getMS932ByteCount(final String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    /**
     * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
     * @param param サイズタイプのパラメータ文字列
     * @param pos split後のインデクス (0:w, 1:h)
     * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
     */
    private static int getParamSizeNum(final String param, final int pos) {
        int num = -1;
        String[] nums = StringUtils.split(param, " * ");
        if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
            num = -1;
        } else {
            try {
                num = Integer.valueOf(nums[pos]).intValue();
            } catch (Exception e) {
                log.error("Exception!", e);
            }
        }
        return num;
    }

    private static String trimLeft(final String s) {
        if (null == s) {
            return null;
        }
        String rtn = s;
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch != ' ' && ch != '　') {
                rtn = s.substring(i);
                break;
            }
        }
        return rtn;
    }

    /**
     * 表紙を印刷する
     * @param svf
     * @param student
     */
    private void printHyoshi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form;// = _param._isMiyagi ? "KNJD181J_1MIYA.frm" : "KNJD181J_1.frm";
        if (_param._isMiyagi) {
            form = "KNJD181J_1MIYA.frm";
        } else {
            if (_param._notoutput_edutarget) {
                form = "KNJD181J_1_2.frm";
            } else {
                form = "KNJD181J_1.frm";
            }
        }
        svf.VrSetForm(form, 1);
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度");
        svf.VrsOut("SCHOOLNAME", _param._certifSchoolSchoolName);
        if (_param._isFukuchiyama) {
            svf.VrsOut("LOGO2", _param.getImagePath("SCHOOLLOGO." + _param._extension));
        } else {
            svf.VrsOut("LOGO", _param.getImagePath("SCHOOLLOGO." + _param._extension));
        }
        svf.VrsOut("HR_NAME", _param._hyosiHrName);
        svf.VrsOut("ATTENDNO", student.getAttendno());
        svf.VrsOut("NAME" + (getMS932ByteCount(student._name) > 26 ? "2" : ""), student._name);
        svf.VrsOut("STAFFNAME1_" + (getMS932ByteCount(trimLeft(_param._certifSchoolPrincipalName)) > 26 ? "2" : "1"), trimLeft(_param._certifSchoolPrincipalName));
        svf.VrsOut("STAFFNAME2_" + (getMS932ByteCount(_param._tr1Name) > 26 ? "2" : "1"), _param._tr1Name);
        svf.VrsOut("EDU_HOPE_TITLE", "教育目標");
        if (_param._isFukuchiyama) {
            svf.VrsOut("TARGET1_2", _param._certifSchoolRemark4);
            svf.VrsOut("TARGET2_2", _param._certifSchoolRemark5);
            svf.VrsOut("TARGET3_2", _param._certifSchoolRemark6);
            svf.VrsOut("TARGET4_2", _param._certifSchoolRemark7);
        } else {
            svf.VrsOut("TARGET1", _param._certifSchoolRemark4);
            svf.VrsOut("TARGET2", _param._certifSchoolRemark5);
            svf.VrsOut("TARGET3", _param._certifSchoolRemark6);
        }
        svf.VrEndPage();
    }

    /**
     * 修了証を印刷する
     * @param svf
     * @param student
     */
    private void printSyuryo(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form;
        if (_param._semester.equals(_param._knjSchoolMst._semesterDiv) && !(!_param._isMiyagi && "03".equals(_param._grade))) {
            form = _param._isMiyagi ? "KNJD181J_4_2MIYA.frm" : "KNJD181J_4_2.frm";
            svf.VrSetForm(form, 1);
            log.fatal(" setForm " + form);
            if (getMS932ByteCount(student._name) > 20) {
                svf.VrsOut("NAME2", student._name);
            } else {
                svf.VrsOut("NAME", student._name);
            }
            if (_param._isMiyagi) {
                svf.VrsOut("GRADE", "第" + _param._gradeCdStr + "学年");
            } else {
                svf.VrsOut("GRADE", "　上記の者は　本校において第" + _param._gradeCdStr + "学年の課程を");
            }
            final String date =  KNJ_EditDate.h_format_JP(db2, _param._descDate);
            svf.VrsOut("DATE", date);
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolRemark3);
            svf.VrsOut("JOB_NAME", _param._certifSchoolJobName);
            svf.VrsOut("STAFF_NAME", _param._certifSchoolPrincipalName);
            svf.VrEndPage();
        } else {
            form = _param._isMiyagi ? "KNJD181J_4_1MIYA.frm" : "KNJD181J_4_1.frm";
            svf.VrSetForm(form, 1);
            log.fatal(" setForm " + form);
            svf.VrsOut("DATE", "　"); // 白文字 (ダミー)
            svf.VrEndPage();
        }
    }

    /**
     * 学習のようす等を印刷する
     * @param svf
     * @param student
     * @param viewClassList
     */
    private void printSeiseki(final Vrw32alp svf, final Student student, final List viewClassList) {

        final String form;
        if ("3".equals(_param._knjSchoolMst._semesterDiv)) {
            form = _param._isMiyagi ? "KNJD181J_2_2MIYA.frm" : "KNJD181J_2_2.frm"; // 3学期制用
        } else {
            form = _param._isMiyagi ? "KNJD181J_2_1MIYA.frm" : "KNJD181J_2_1.frm"; // 前後期制用
        }
        log.fatal(" setForm " + form);
        svf.VrSetForm(form, 4);

        printStudent(svf, student);

        printViewRecord(svf, student, viewClassList);
    }

    /**
     * 所見等を印刷する
     * @param svf
     * @param student
     */
    private void printSyoken(final Vrw32alp svf, final Student student) {
        final String form;
        if (!StringUtils.isBlank(_param._useFormNameD181J_3)) {
            form = _param._useFormNameD181J_3 + ".frm";
        } else if ("2".equals(_param._knjSchoolMst._semesterDiv)) {
            // 前後期制用
            if (_param._isMiyagi) {
                form =  "KNJD181J_3_1MIYA.frm";
            } else {
                if ("1".equals(_param._ouinranShuturyoku)) {
                    form =  "KNJD181J_3_1_2.frm";
                } else {
                    form =  "KNJD181J_3_1.frm";
                }
            }
        } else {
            // 3学期制用
            if (_param._isMiyagi) {
                form = "KNJD181J_3_2_1MIYA.frm";
            } else {
                if ("3".equals(_param._knjSchoolMst._semesterDiv) && "1".equals(_param._tutisyoShokennTunen)) {
                    if ("1".equals(_param._ouinranShuturyoku)) {
                        form = "KNJD181J_3_2_5.frm";
                    } else {
                        form = "KNJD181J_3_2_4.frm";
                    }
                } else {
                    if ("1".equals(_param._ouinranShuturyoku)) {
                        form = "1".equals(_param._tutisyoShokennSemesterTitle) ? "KNJD181J_3_2_2_2.frm" : "KNJD181J_3_2_2.frm";
                    } else {
                        form = "1".equals(_param._tutisyoShokennSemesterTitle) ? "KNJD181J_3_2_1_2.frm" : "KNJD181J_3_2_1.frm";
                    }
                }
            }
        }
        log.fatal(" setForm " + form);
        svf.VrSetForm(form, 1);
        _hasDataForm3 = false;

        if ("KNJD181J_3_2_3.frm".equals(form)) {
            final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(_param._documentMstSize_B4, 23, 14);
            final List token = KNJ_EditKinsoku.getTokenList(_param._documentMstB4Text, size.getKeta());
            for (int i = 0; i < token.size(); i++) {
                svf.VrsOutn("DOCUMENT", i + 1, (String) token.get(i));
            }
        }

        printReport(svf, student);

        printReportTotalstudytime(svf, student);

        if (!"1".equals(_param._notPrintAttendance)) {
            printAttendSemes(svf, student);
        }
        if (!_hasDataForm3) {
            //log.debug(" _hasDataForm3 = " + _hasDataForm3);
            svf.VrsOutn("LESSON", 1, "0"); // データがなければフォームが出力されないのでダミー出力
            svf.VrAttributen("LESSON", 1, "X=10000"); // 位置を変更し実際には表示しない
        }

        svf.VrEndPage();
    }

    private void printStudent(final Vrw32alp svf, final Student student) {
        svf.VrsOut("NAME", student._name);
        svf.VrsOut("ATTENDNO", student.getAttendno());
        svf.VrsOut("SEMESTER", _param._semester);
    }

    /**
     * 『出欠の記録』を印字する
     * @param svf
     * @param student
     */
    private void printAttendSemes(final Vrw32alp svf, final Student student) {
        boolean addflg = false;
        final AttendSemesDat total = new AttendSemesDat(SEME_ALL);
        for (final Iterator it = student._attendSemesDatList.iterator(); it.hasNext();) {
            final AttendSemesDat attendSemesDat = (AttendSemesDat) it.next();
            if (!NumberUtils.isDigits(attendSemesDat._semester)) {
                continue;
            }

            final int j;
            if (SEME_ALL.equals(attendSemesDat._semester)) {
                continue;
            } else {
                j = Integer.parseInt(attendSemesDat._semester);
                if (!_param._isMiyagi && j > Integer.parseInt(_param._semester)) {
                    continue;
                }
            }
            addflg = true;
            svf.VrsOutn("LESSON", j, String.valueOf(attendSemesDat._lesson));
            svf.VrsOutn("SPECIAL", j, String.valueOf(attendSemesDat._suspend + attendSemesDat._mourning + attendSemesDat._virus + attendSemesDat._koudome));
            svf.VrsOutn("PRESENT", j, String.valueOf(attendSemesDat._mlesson));
            svf.VrsOutn("ABSENCE", j, String.valueOf(attendSemesDat._sick));
            svf.VrsOutn("ATTEND", j, String.valueOf(attendSemesDat._present));
            svf.VrsOutn("LATE", j, String.valueOf(attendSemesDat._late));
            svf.VrsOutn("EARLY", j, String.valueOf(attendSemesDat._early));
            total.add(attendSemesDat);
            _hasDataForm3 = true;
        }
        if (addflg) {
            final int j = 1 + Integer.parseInt(StringUtils.defaultString(_param._knjSchoolMst._semesterDiv, "3"));
            svf.VrsOutn("LESSON", j, String.valueOf(total._lesson));
            svf.VrsOutn("SPECIAL", j, String.valueOf(total._suspend + total._mourning + total._virus + total._koudome));
            svf.VrsOutn("PRESENT", j, String.valueOf(total._mlesson));
            svf.VrsOutn("ABSENCE", j, String.valueOf(total._sick));
            svf.VrsOutn("ATTEND", j, String.valueOf(total._present));
            svf.VrsOutn("LATE", j, String.valueOf(total._late));
            svf.VrsOutn("EARLY", j, String.valueOf(total._early));
        }

        // 備考
        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat dat = (HReportRemarkDat) it.next();
            if (null == dat._semester || SEME_ALL.equals(dat._semester)) {
                continue;
            }
            final int semester = Integer.parseInt(dat._semester); // "計"の欄は表示しない
            final List remark = getTokenList(dat._attendrecRemark, 30, 2);
            if (null != remark) {
                for (int i = 0; i < remark.size(); i++) {
                    svf.VrsOutn("REMARK" + String.valueOf(i + 1), semester, (String) remark.get(i));
                }
                _hasDataForm3 = true;
            }
        }
    }

    /**
     * @param source 元文字列
     * @param bytePerLine 1行あたりのバイト数
     * @param maxLine 行数
     * @return bytePerLineのバイト数ごとの文字列リスト
     */
    public static List getTokenList(final String source, final int bytePerLine, final int maxLine) {
        if (maxLine == -1) {
            return KNJ_EditKinsoku.getTokenList(source, bytePerLine);
        }
        return KNJ_EditKinsoku.getTokenList(source, bytePerLine, maxLine);
//        if (source == null || source.length() == 0) {
//            return Collections.EMPTY_LIST;
//        }
//
//        List tokenList = new ArrayList();        //分割後の文字列の配列
//        int startIndex = 0;                         //文字列の分割開始位置
//        int byteLengthInLine = 0;                   //文字列の分割開始位置からのバイト数カウント
//        for (int idx = 0; idx < source.length(); idx += 1) {
//            //改行マークチェック    04/09/28Modify
//            if (source.charAt(idx) == '\r') {
//                continue;
//            }
//            if (source.charAt(idx) == '\n') {
//                // stoken[ib] = strx.substring(s_sta, s_cur);
//                tokenList.add(source.substring(startIndex, idx));
//                // lines += 1;
//                byteLengthInLine = 0;
//                startIndex = idx + 1;
//            } else {
//                final int sbytelen = getMS932ByteCount(source.substring(idx, idx + 1));
//                byteLengthInLine += sbytelen;
//                if (byteLengthInLine > bytePerLine) {
//                    // stoken[ib] = strx.substring(s_sta, s_cur);
//                    tokenList.add(source.substring(startIndex, idx));
//                    // lines += 1;
//                    byteLengthInLine = sbytelen;
//                    startIndex = idx;
//                }
//            }
//        }
//        // log.debug(" line = " + lines);
//        if (byteLengthInLine > 0) {
//            // stoken[lines] = strx.substring(s_sta);
//            tokenList.add(source.substring(startIndex));
//        }
//        if (maxLine > -1 && tokenList.size() > maxLine) {
//            tokenList = tokenList.subList(0, maxLine);
//        }
//        return tokenList;
    } //String get_token()の括り

    private String getFieldSemDiv(final String semester) {
        final String fSemDiv;
        if ("3".equals(_param._knjSchoolMst._semesterDiv)) {
            fSemDiv = SEME_ALL.equals(semester) ? "4" : semester;
        } else {
            fSemDiv = SEME_ALL.equals(semester) ? "2" : "1".equals(semester) ? "1" : null;
        }
        return fSemDiv;
    }

    /**
     * 学習の記録を印字する
     * @param svf
     * @param student
     */
    private void printViewRecord(final Vrw32alp svf, final Student student, final List viewClassList) {

        int sum = 0;
        for (final Iterator it = viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();

            if ("1".equals(viewClass._electDiv) && !student.hasChairSubclass(viewClass._subclasscd)) { // 選択教科は講座名簿がある科目のみ表示対象
                continue;
            }

            final int viewSize = viewClass.getViewSize();
            final String fn;
            if (viewSize >= 5) {
                fn = "1";
                sum += 5;
            } else if (viewSize == 4) {
                fn = "2";
                sum += 4;
            } else { // (viewSize <= 3) {
                fn = "3";
                sum += 3;
            }
            if (getMS932ByteCount(viewClass._subclassname) > 10) {
                final List tokens = getTokenList(viewClass._subclassname, 14, -1);
                if (null != tokens) {
                    if (tokens.size() > 0) {
                        svf.VrsOut("SUBJECTNAME" + fn + "_" + "2", (String) tokens.get(0)); // 科目名称
                    }
                    if (tokens.size() > 1) {
                        svf.VrsOut("SUBJECTNAME" + fn + "_" + "3", (String) tokens.get(1)); // 科目名称
                    }
                }
            } else {
                svf.VrsOut("SUBJECTNAME" + fn + "_" + "1", viewClass._subclassname); // 科目名称
            }
            for (int i = 0; i < viewSize; i++) {
                final String line = String.valueOf(i + 1);
                svf.VrsOut("VIEWNAME" + fn + "_" + line, viewClass.getViewName(i)); // 観点名称

                final List viewRecordList = student.getViewList(viewClass._subclasscd, viewClass.getViewCd(i));
                for (final Iterator itv = viewRecordList.iterator(); itv.hasNext();) {
                    final ViewRecord viewRecord = (ViewRecord) itv.next();
                    svf.VrsOut("VIEW" + fn + "_" + getFieldSemDiv(viewRecord._semester)  + "_" + line, viewRecord._status); // 観点
                }

                final List viewValuationList = student.getValueList(viewClass._subclasscd);
                for (final Iterator itv = viewValuationList.iterator(); itv.hasNext();) {
                    final ViewValuation viewValuation = (ViewValuation) itv.next();
                    final String value;
                    if (_param._d065Name1List.contains(viewClass._subclasscd)) {
                        if ("11".equals(viewValuation._value)) {
                            value = "A";
                        } else if ("22".equals(viewValuation._value)) {
                            value = "B";
                        } else if ("33".equals(viewValuation._value)) {
                            value = "C";
                        } else {
                            value = null;
                        }
                    } else if ("1".equals(viewClass._electDiv)) {
                        if ("11".equals(viewValuation._value)) {
                            value = "A";
                        } else if ("22".equals(viewValuation._value)) {
                            value = "B";
                        } else if ("33".equals(viewValuation._value)) {
                            value = "C";
                        } else {
                            value = viewValuation._value;
                        }
                    } else {
                        value = viewValuation._value;
                    }
                    svf.VrsOut("RATE" + fn + "_" + getFieldSemDiv(viewValuation._semester), value); // 評定
                }
            }
            svf.VrEndRecord();
        }

        // 空行表示
        for (int i = sum, max = 40; i < max; i++) {
            svf.VrsOut("DUMMY", "\n"); // 科目名称
            svf.VrEndRecord();
        }
    }

    private String mkString(final List sentakuSubclassnameList, final String comma) {
        final StringBuffer stb = new StringBuffer();
        String cm = "";
        for (final Iterator it = sentakuSubclassnameList.iterator(); it.hasNext();) {
            final String subclassname = (String) it.next();
            if (null != subclassname) {
                stb.append(cm).append(subclassname);
                cm = comma;
            }
        }
        return stb.toString();
    }

    /**
     * 『特別活動等』『部活動』『所見』を印字する
     * @param svf
     * @param student
     */
    private void printReport(final Vrw32alp svf, final Student student) {

        final int pcharsToku = getParamSizeNum(_param._reportSpecialSize02_01, 0);
        final int plinesToku = getParamSizeNum(_param._reportSpecialSize02_01, 1);
        final int charsToku = (-1 == pcharsToku || -1 == plinesToku) ? 27 : pcharsToku;
        final int linesToku = (-1 == pcharsToku || -1 == plinesToku) ?  8 : plinesToku;

        final int pcharsBukatu = getParamSizeNum(_param._reportSpecialSize03_01, 0);
        final int plinesBukatu = getParamSizeNum(_param._reportSpecialSize03_01, 1);
        final int charsBukatu = (-1 == pcharsBukatu || -1 == plinesBukatu) ? 30 : pcharsBukatu;
        final int linesBukatu = (-1 == pcharsBukatu || -1 == plinesBukatu) ?  7 : plinesBukatu;

        final int charsShokenWk = "2".equals(_param._knjSchoolMst._semesterDiv) ? 21 : 24;
        final int linesShokenWk = "2".equals(_param._knjSchoolMst._semesterDiv) ?  7 :  9;
        final int pcharsShoken = getParamSizeNum(_param._reportSpecialSize04_01, 0);
        final int plinesShoken = getParamSizeNum(_param._reportSpecialSize04_01, 1);
        final int charsShoken = (-1 == pcharsShoken || -1 == plinesShoken) ? charsShokenWk : pcharsShoken;
        final int linesShoken = (-1 == pcharsShoken || -1 == plinesShoken) ? linesShokenWk : plinesShoken;

        final int charsMoral = "2".equals(_param._knjSchoolMst._semesterDiv)  ? 23 : 24;
        final int linesMoral =  "2".equals(_param._knjSchoolMst._semesterDiv) ? 14 :  9;

        if (!"".equals(_param._reportSpecialSize04_01Title)) {
            svf.VrsOut("VIEW_TITLE", _param._reportSpecialSize04_01Title);
        }
        for (final Iterator it = student._hReportRemarkDetailDatList.iterator(); it.hasNext();) {
            final HReportRemarkDetailDat dat = (HReportRemarkDetailDat) it.next();

            if (SEME_ALL.equals(dat._semester)) {
                if ("03".equals(dat._div) && "01".equals(dat._code)) {
                    //部活動
                    VrsOutnRenban(svf, "CLUB", getTokenList(dat._remark1, charsBukatu * 2, linesBukatu));
                    if (!StringUtils.isBlank(dat._remark1)) {
                        _hasDataForm3 = true;
                    }
                }
                if ("04".equals(dat._div) && "01".equals(dat._code) && ("3".equals(_param._knjSchoolMst._semesterDiv) && "1".equals(_param._tutisyoShokennTunen))) {
                    //3学期制でtutisyoShokennTunenプロパティが立っていれば、"9"学期のデータを利用する。
                    if ("3".equals(_param._knjSchoolMst._semesterDiv) && "1".equals(_param._shokenShuturyoku)) {
                        svf.VrsOut("BLANK", _param._whiteSpaceImagePath);
                    } else {
                        // 担任からの所見
                        VrsOutnRenban(svf, "VIEW1", getTokenList(dat._remark1, charsShoken * 2, linesShoken));
                        if (!StringUtils.isBlank(dat._remark1)) {
                            _hasDataForm3 = true;
                        }
                    }
                }
            } else {
                if ("02".equals(dat._div) && "01".equals(dat._code)) {
                    // 特別活動
                    VrsOutnRenban(svf, "SP_ACT" + dat._semester, getTokenList(dat._remark1, charsToku * 2, linesToku));
                    if (!StringUtils.isBlank(dat._remark1)) {
                        _hasDataForm3 = true;
                    }
                } else if ("04".equals(dat._div) && "01".equals(dat._code) && !("3".equals(_param._knjSchoolMst._semesterDiv) && "1".equals(_param._tutisyoShokennTunen))) {
                    //3学期制でtutisyoShokennTunenプロパティが立っていれば、"9"学期のデータを利用するので、ここでは出力しない。
                    if ("3".equals(_param._knjSchoolMst._semesterDiv) && "1".equals(_param._shokenShuturyoku)) {
                        svf.VrsOut("BLANK", _param._whiteSpaceImagePath);
                    } else {
                        // 担任からの所見
                        VrsOutnRenban(svf, "VIEW" + dat._semester, getTokenList(dat._remark1, charsShoken * 2, linesShoken));
                        if (!StringUtils.isBlank(dat._remark1)) {
                            _hasDataForm3 = true;
                        }
                    }
                }
            }
        }

        if(_param._isKyoto) {
            // 道徳
            for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
                final HReportRemarkDat dat = (HReportRemarkDat) it.next();
                if (null == dat._semester || !SEME_ALL.equals(dat._semester)) {
                    continue;
                }
                VrsOutnRenban(svf, "MORAL", getTokenList(dat._remark1, charsMoral * 2, linesMoral));
                if (!StringUtils.isBlank(dat._remark1)) {
                    _hasDataForm3 = true;
                }
            }
        }

    }

    /**
     * 『総合的な学習の時間』を印字する
     * @param svf
     * @param student
     */
    private void printReportTotalstudytime(final Vrw32alp svf, final Student student) {

        final int pcharstt = getParamSizeNum(_param._reportSpecialSize01_01, 0);
        final int plinestt = getParamSizeNum(_param._reportSpecialSize01_01, 1);
        final int charstt = (-1 == pcharstt || -1 == plinestt) ? 50 : pcharstt;
        final int linestt = (-1 == pcharstt || -1 == plinestt) ?  9 : plinestt;

        final int pcharsrr = getParamSizeNum(_param._reportSpecialSize01_02, 0);
        final int plinesrr = getParamSizeNum(_param._reportSpecialSize01_02, 1);
        final int charsrr = (-1 == pcharsrr || -1 == plinesrr) ? 64 : pcharsrr;
        final int linesrr = (-1 == pcharsrr || -1 == plinesrr) ?  9 : plinesrr;

        for (final Iterator it = student._hReportRemarkDetailDatList.iterator(); it.hasNext();) {
            final HReportRemarkDetailDat data = (HReportRemarkDetailDat) it.next();
            final String field1;
            final String field2;
            if ("1".equals(_param._tutisyoSougouHyoukaTunen)) {
                // 通年
                if (!SEME_ALL.equals(data._semester)) {
                    continue;
                }
                field1 = "STUDY_ACT1";
                field2 = "STUDY_VAL1";
            } else {
                // 学期ごと
                if (SEME_ALL.equals(data._semester)) {
                    continue;
                }
                field1 = "STUDY_ACT" + data._semester;
                field2 = "STUDY_VAL" + data._semester;
            }
            if ("01".equals(data._div) && "01".equals(data._code)) {
                VrsOutnRenban(svf, field1, getTokenList(data._remark1, charstt * 2, linestt));
                if (!StringUtils.isBlank(data._remark1)) {
                    _hasDataForm3 = true;
                }
            }
            if ("01".equals(data._div) && "02".equals(data._code)) {
                VrsOutnRenban(svf, field2, getTokenList(data._remark1, charsrr * 2, linesrr));
                if (!StringUtils.isBlank(data._remark1)) {
                    _hasDataForm3 = true;
                }
            }
        }
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final List _viewRecordList = new ArrayList(); // 観点
        final List _viewValuationList = new ArrayList(); // 評定
        final List _attendSemesDatList = new ArrayList(); // 出欠の記録
        final List _hReportRemarkDatList = new ArrayList(); // 所見
        final List _hReportRemarkDetailDatList = new ArrayList(); // 所見(特別活動)
        final List _chairSubclassList = new ArrayList();

        public Student(final String schregno, final String name, final String hrName, final String attendno) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
        }

        public String getAttendno() {
            if (null == _attendno) {
                return "";
            }
            if (!NumberUtils.isDigits(_attendno)) {
                return _attendno;
            }
            final int no = Integer.parseInt(_attendno);
            return (no < 10 ? " " : "") + String.valueOf(no);
        }

        public boolean hasChairSubclass(final String subclasscd) {
            return null != ChairSubclass.getChairSubclass(_chairSubclassList, subclasscd);
        }

        /**
         * 観点コードの観点のリストを得る
         * @param subclasscd 科目コード
         * @param viewcd 観点コード
         * @return 観点コードの観点のリスト
         */
        public List getViewList(final String subclasscd, final String viewcd) {
            final List rtn = new ArrayList();
            if (null != viewcd) {
                for (Iterator it = _viewRecordList.iterator(); it.hasNext();) {
                    final ViewRecord viewRecord = (ViewRecord) it.next();
                    if (viewRecord._subclasscd.equals(subclasscd) && viewcd.equals(viewRecord._viewcd)) {
                        rtn.add(viewRecord);
                    }
                }
            }
            return rtn;
        }

        /**
         * 評定のリストを得る
         * @param subclasscd 評定の科目コード
         * @return 評定のリスト
         */
        public List getValueList(final String subclasscd) {
            final List rtn = new ArrayList();
            if (null != subclasscd) {
                for (Iterator it = _viewValuationList.iterator(); it.hasNext();) {
                    final ViewValuation viewValuation = (ViewValuation) it.next();
                    if (subclasscd.equals(viewValuation._subclasscd)) {
                        rtn.add(viewValuation);
                    }
                }
            }
            return rtn;
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentSql(param);
                log.debug(" student sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");

                    final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    final String hrName = rs.getString("HR_NAME");
                    final String attendno = rs.getString("ATTENDNO");
                    final Student student = new Student(schregno, name, hrName, attendno);
                    studentList.add(student);
                }

            } catch (SQLException e) {
                log.error("exception!!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (param._isPrintSeiseki) {
                ViewRecord.setViewRecordList(db2, param, studentList);
                ViewValuation.setViewValuationList(db2, param, studentList);
                ChairSubclass.setChairSubclass(db2, param, studentList);
            }
            if (param._isPrintSyoken) {
                AttendSemesDat.setAttendSemesDatList(db2, param, studentList);
                HReportRemarkDat.setHReportRemarkDatList(db2, param, studentList);
                HReportRemarkDetailDat.setHReportRemarkDetailDatList(db2, param, studentList);
            }
            return studentList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append("    FROM    SCHREG_REGD_DAT T1 ");
            stb.append("            , V_SEMESTER_GRADE_MST T2 ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SEMESTER = '"+ param._semester +"' ");
            stb.append("        AND T1.YEAR = T2.YEAR ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T1.GRADE = T2.GRADE ");
            stb.append("        AND T1.GRADE||T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("        AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
            //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("                           AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
            stb.append("                             OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
            //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("                           AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append("    ) ");
            //メイン表
            stb.append("SELECT  T1.SCHREGNO, ");
            stb.append("        T7.HR_NAME, ");
            stb.append("        T1.ATTENDNO, ");
            stb.append("        T5.NAME, ");
            stb.append("        T5.REAL_NAME, ");
            stb.append("        CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append("        LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
            stb.append("        LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
        }
    }

    /**
     * 観点の教科
     */
    private static class ViewClass {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final String _electDiv;
        final List _viewList;
        final List _valuationList;
        ViewClass(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname,
                final String electDiv) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _electDiv = electDiv;
            _viewList = new ArrayList();
            _valuationList = new ArrayList();
        }

        public void addView(final String viewcd, final String viewname) {
            _viewList.add(new String[]{viewcd, viewname});
        }

        public String getViewCd(final int i) {
            return ((String[]) _viewList.get(i))[0];
        }

        public String getViewName(final int i) {
            return ((String[]) _viewList.get(i))[1];
        }

        public int getViewSize() {
            return _viewList.size();
        }

        public static List getViewClassList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewClassSql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String electDiv = rs.getString("ELECTDIV");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");

                    ViewClass viewClass = null;
                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final ViewClass viewClass0 = (ViewClass) it.next();
                        if (viewClass0._subclasscd.equals(subclasscd)) {
                            viewClass = viewClass0;
                            break;
                        }
                    }

                    if (null == viewClass) {
                        viewClass = new ViewClass(classcd, classname, subclasscd, subclassname, electDiv);
                        list.add(viewClass);
                    }

                    viewClass.addView(viewcd, viewname);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getViewClassSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     T3.CLASSNAME, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     VALUE(T4.ELECTDIV, '0') AS ELECTDIV, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     T1.VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            }
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T3.CLASSCD < '90' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T4.ELECTDIV, '0'), ");
            stb.append("     VALUE(T3.SHOWORDER3, -1), ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
        }
        public String toString() {
            return _subclasscd + ":" + _subclassname + " " + _electDiv;
        }
    }

    /**
     * 学習の記録（観点）
     */
    private static class ViewRecord {

        final String _semester;
        final String _viewcd;
        final String _status;
        final String _grade;
        final String _viewname;
        final String _classcd;
        final String _subclasscd;
        final String _classMstShoworder;
        final String _showorder;
        ViewRecord(
                final String semester,
                final String viewcd,
                final String status,
                final String grade,
                final String viewname,
                final String classcd,
                final String subclasscd,
                final String classMstShoworder,
                final String showorder) {
            _semester = semester;
            _viewcd = viewcd;
            _status = status;
            _grade = grade;
            _viewname = viewname;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classMstShoworder = classMstShoworder;
            _showorder = showorder;
        }

        public static void setViewRecordList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getViewRecordSql(param);
                log.debug(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    final List list = student._viewRecordList;

                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        final String viewcd = rs.getString("VIEWCD");
                        final String status = rs.getString("STATUS");
                        final String grade = rs.getString("GRADE");
                        final String viewname = rs.getString("VIEWNAME");
                        final String classcd = rs.getString("CLASSCD");
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String classMstShoworder = rs.getString("CLASS_MST_SHOWORDER");
                        final String showorder = rs.getString("SHOWORDER");

                        final ViewRecord viewRecord = new ViewRecord(semester, viewcd, status, grade, viewname, classcd, subclasscd, classMstShoworder, showorder);

                        list.add(viewRecord);
                    }
                    DbUtils.closeQuietly(rs);
                    db2.commit();
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getViewRecordSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE   ");
            stb.append("     , T1.VIEWNAME ");
            stb.append("     , T1.VIEWCD ");
            stb.append("     , T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , T3.SCHREGNO ");
            stb.append("     , T3.STATUS ");
            stb.append("     , T4.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("     , T1.SUBCLASSCD ");
            }
            stb.append("     , T4.SHOWORDER AS CLASS_MST_SHOWORDER ");
            stb.append("     , T1.SHOWORDER ");
            stb.append(" FROM JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T3.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T3.YEAR = T2.YEAR ");
            if (param._semester.equals(param._knjSchoolMst._semesterDiv)) {
                stb.append("     AND T3.SEMESTER <= '" + SEME_ALL + "' ");
            } else {
                stb.append("     AND T3.SEMESTER <= '" + param._semester + "' ");
            }
            stb.append("         AND T3.SCHREGNO = ? ");
            stb.append("     LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            }
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , VALUE(T4.SHOWORDER, 0) ");
            stb.append("     , T4.CLASSCD ");
            stb.append("     , VALUE(T1.SHOWORDER, 0) ");
            stb.append("     , T1.VIEWCD ");
            return stb.toString();
        }
    }

    /**
     * 学習の記録（評定）
     */
    private static class ViewValuation {
        final String _semester;
        final String _classcd;
        final String _subclasscd;
        final String _subclassname;
        final String _value;
        ViewValuation(
                final String semester,
                final String classcd,
                final String subclasscd,
                final String subclassname,
                final String value) {
            _semester = semester;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _value = value;
        }

        public static void setViewValuationList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getViewValuationSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    if (param._semester.equals(param._knjSchoolMst._semesterDiv)) {
                        ps.setString(2, student._schregno);
                    }
                    final List list = student._viewValuationList;

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String classcd = rs.getString("CLASSCD");
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String value = rs.getString("VALUE");
                        final ViewValuation viewValuation = new ViewValuation(semester, classcd, subclasscd, subclassname, value);

                        list.add(viewValuation);
                    }
                    DbUtils.closeQuietly(rs);
                    db2.commit();
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getViewValuationSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH RECORD_RANK AS (");
            stb.append(" SELECT ");
            stb.append("     T2.YEAR, ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.SCHOOL_KIND, ");
                stb.append("     T2.CURRICULUM_CD, ");
            }
            stb.append("     T2.SUBCLASSCD, ");
            stb.append("     T2.SCORE AS VALUE ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T2.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T2.TESTKINDCD = '99' ");
            stb.append("     AND T2.TESTITEMCD = '00' ");
            stb.append("     AND T2.SCHREGNO = ? ");
            if (param._semester.equals(param._knjSchoolMst._semesterDiv)) {
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     T2.YEAR, ");
                stb.append("     T2.SEMESTER, ");
                stb.append("     SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("     T2.SCHOOL_KIND, ");
                    stb.append("     T2.CURRICULUM_CD, ");
                }
                stb.append("     T2.SUBCLASSCD, ");
               // stb.append("     T2.SCORE AS VALUE ");
                stb.append("     T2.VALUE ");
                stb.append(" FROM ");
                stb.append("     RECORD_SCORE_DAT T2 ");
                stb.append(" WHERE ");
                stb.append("     T2.YEAR = '" + param._year + "' ");
                stb.append("     AND T2.SEMESTER = '" + SEME_ALL + "' ");
                stb.append("     AND T2.TESTKINDCD = '99' ");
                stb.append("     AND T2.TESTITEMCD = '00' ");
                stb.append("     AND T2.SCHREGNO = ? ");
            }
            stb.append(" )");
            stb.append(" SELECT ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T2.SUBCLASSCD, ");
            }
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     T2.VALUE ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK T2 ");
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.CLASSCD = T2.CLASSCD  ");
                stb.append("         AND T4.SCHOOL_KIND = T2.SCHOOL_KIND  ");
                stb.append("         AND T4.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            }
            stb.append("     LEFT JOIN CLASS_MST T5 ON T5.CLASSCD = SUBSTR(T2.SUBCLASSCD, 1, 2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T5.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            }
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            if ("Y".equals(param._d016Namespare1)) {
                stb.append("     AND NOT EXISTS ( ");
                stb.append("         SELECT 'X' ");
                stb.append("         FROM ");
                stb.append("             SUBCLASS_REPLACE_COMBINED_DAT L1 ");
                stb.append("         WHERE ");
                stb.append("             L1.YEAR = T2.YEAR ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("             AND L1.ATTEND_CLASSCD = T2.CLASSCD ");
                    stb.append("             AND L1.ATTEND_SCHOOL_KIND = T2.SCHOOL_KIND ");
                    stb.append("             AND L1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD ");
                }
                stb.append("             AND L1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ");
                stb.append("     ) ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T5.SHOWORDER3, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD ");
            } else {
                stb.append("     T2.SUBCLASSCD ");
            }
            return stb.toString();
        }
    }



    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _semester;
        int _lesson;
        int _suspend;
        int _mourning;
        int _mlesson;
        int _sick;
        int _absent;
        int _present;
        int _late;
        int _early;
        int _transferDate;
        int _offdays;
        int _kekkaJisu;
        int _virus;
        int _koudome;

        public AttendSemesDat(
                final String semester
        ) {
            _semester = semester;
        }

        public void add(
                final AttendSemesDat o
        ) {
            _lesson += o._lesson;
            _suspend += o._suspend;
            _mourning += o._mourning;
            _mlesson += o._mlesson;
            _sick += o._sick;
            _absent += o._absent;
            _present += o._present;
            _late += o._late;
            _early += o._early;
            _transferDate += o._transferDate;
            _offdays += o._offdays;
            _kekkaJisu += o._kekkaJisu;
            _virus += o._virus;
            _koudome += o._koudome;
        }

        private static void setAttendSemesDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        null,
                        param._date,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    final List list = student._attendSemesDatList;

                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        final int lesson = rs.getInt("LESSON");
                        final int suspend = rs.getInt("SUSPEND");
                        final int mourning = rs.getInt("MOURNING");
                        final int mlesson = rs.getInt("MLESSON");
                        final int sick = rs.getInt("SICK");
                        final int absent = rs.getInt("ABSENT");
                        final int present = rs.getInt("PRESENT");
                        final int late = rs.getInt("LATE");
                        final int early = rs.getInt("EARLY");
                        final int transferDate = rs.getInt("TRANSFER_DATE");
                        final int offdays = rs.getInt("OFFDAYS");
                        final int kekkaJisu = rs.getInt("KEKKA_JISU");
                        final int virus = rs.getInt("VIRUS");
                        final int koudome = rs.getInt("KOUDOME");

                        final AttendSemesDat attendSemesDat = new AttendSemesDat(semester);
                        attendSemesDat._lesson = lesson;
                        attendSemesDat._suspend = suspend;
                        attendSemesDat._mourning = mourning;
                        attendSemesDat._mlesson = mlesson;
                        attendSemesDat._sick = sick;
                        attendSemesDat._absent = absent;
                        attendSemesDat._present = present;
                        attendSemesDat._late = late;
                        attendSemesDat._early = early;
                        attendSemesDat._transferDate = transferDate;
                        attendSemesDat._offdays = offdays;
                        attendSemesDat._kekkaJisu = kekkaJisu;
                        attendSemesDat._virus = virus;
                        attendSemesDat._koudome = koudome;

                        list.add(attendSemesDat);
                    }
                    DbUtils.closeQuietly(rs);
                    db2.commit();
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

    }

    /**
     * 通知表所見
     */
    private static class HReportRemarkDat {
        final String _semester;
        final String _totalstudytime;     // 総合的な学習の時間
        final String _specialactremark;   // 特別活動の記録・その他
        final String _communication;      // 担任からの所見
        final String _remark1;            // 特別活動の記録・学級活動
        final String _remark2;            // 特別活動の記録・生徒会活動
        final String _remark3;            // 特別活動の記録・学校行事
        final String _attendrecRemark;    // 出欠備考

        public HReportRemarkDat(
                final String semester,
                final String totalstudytime,
                final String specialactremark,
                final String communication,
                final String remark1,
                final String remark2,
                final String remark3,
                final String attendrecRemark) {
            _semester = semester;
            _totalstudytime = totalstudytime;
            _specialactremark = specialactremark;
            _communication = communication;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _attendrecRemark = attendrecRemark;
        }

        public static void setHReportRemarkDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    final List list = student._hReportRemarkDatList;

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String totalstudytime = StringUtils.defaultString(rs.getString("TOTALSTUDYTIME"));
                        final String specialactremark = StringUtils.defaultString(rs.getString("SPECIALACTREMARK"));
                        final String communication = StringUtils.defaultString(rs.getString("COMMUNICATION"));
                        final String remark1 = StringUtils.defaultString(rs.getString("REMARK1"));
                        final String remark2 = StringUtils.defaultString(rs.getString("REMARK2"));
                        final String remark3 = StringUtils.defaultString(rs.getString("REMARK3"));
                        final String attendrecRemark = StringUtils.defaultString(rs.getString("ATTENDREC_REMARK"));

                        final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, totalstudytime, specialactremark, communication,
                                remark1, remark2, remark3, attendrecRemark);
                        list.add(hReportRemarkDat);
                    }
                    DbUtils.closeQuietly(rs);
                    db2.commit();
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getHReportRemarkSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND (T1.SEMESTER <= '" + param._semester + "' OR T1.SEMESTER = '9') ");
            stb.append("     AND T1.SCHREGNO = ? ");
            return stb.toString();
        }
    }

    /**
     * 通知表所見(特別活動)
     */
    private static class HReportRemarkDetailDat {
        final String _semester;
        final String _div;
        final String _code;
        final String _remark1;

        public HReportRemarkDetailDat(
                final String semester,
                final String div,
                final String code,
                final String remark1) {
            _semester = semester;
            _div = div;
            _code = code;
            _remark1 = remark1;
        }

        public static void setHReportRemarkDetailDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkDetailSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);

                    final List list = student._hReportRemarkDetailDatList;
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String div = rs.getString("DIV");
                        final String code = rs.getString("CODE");
                        final String remark1 = rs.getString("REMARK1");

                        final HReportRemarkDetailDat hReportRemarkDetailDat = new HReportRemarkDetailDat(semester, div, code, remark1);
                        list.add(hReportRemarkDetailDat);
                    }
                    DbUtils.closeQuietly(rs);
                    db2.commit();
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getHReportRemarkDetailSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DETAIL_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND (T1.SEMESTER <= '" + param._semester + "' OR T1.SEMESTER = '9') ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     DIV, ");
            stb.append("     SEMESTER, ");
            stb.append("     CODE ");
            return stb.toString();
        }
    }

    private static class ChairSubclass {
        final String _subclasscd;
        final List _chaircdList;
        public ChairSubclass(final String subclasscd) {
            _subclasscd = subclasscd;
            _chaircdList = new ArrayList();
        }
        public static void setChairSubclass(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT DISTINCT ");
            sql.append("   T2.CHAIRCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                sql.append("     T2.SUBCLASSCD ");
            }
            sql.append(" FROM ");
            sql.append("   CHAIR_STD_DAT T1 ");
            sql.append("   INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
            sql.append("     AND T2.SEMESTER = T1.SEMESTER ");
            sql.append("     AND T2.CHAIRCD = T1.CHAIRCD ");
            sql.append(" WHERE ");
            sql.append("     T1.YEAR = '" + param._year + "' ");
            sql.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            sql.append("     AND T1.SCHREGNO = ? ");
            sql.append("     AND '" + param._date + "' BETWEEN T1.APPDATE AND T1.APPENDDATE ");

            try {

                ps = db2.prepareStatement(sql.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    final List list = student._chairSubclassList;
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        ChairSubclass cs = getChairSubclass(list, rs.getString("SUBCLASSCD"));
                        if (null == cs) {
                            cs = new ChairSubclass(rs.getString("SUBCLASSCD"));
                            list.add(cs);
                        }
                        cs._chaircdList.add(rs.getString("CHAIRCD"));
                    }
                    DbUtils.closeQuietly(rs);
                    db2.commit();
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        public static ChairSubclass getChairSubclass(final List chairSubclassList, final String subclasscd) {
            ChairSubclass chairSubclass = null;
            for (final Iterator it = chairSubclassList.iterator(); it.hasNext();) {
                final ChairSubclass cs = (ChairSubclass) it.next();
                if (null != cs._subclasscd && cs._subclasscd.equals(subclasscd)) {
                    chairSubclass = cs;
                    break;
                }
            }
            return chairSubclass;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _gradeHrclass;
        final String _grade;
        final String[] _categorySelected;
        final String _trCd1;
        final boolean _isPrintHyoshi;
        final boolean _isPrintSeiseki;
        final boolean _isPrintSyoken;
        final boolean _isPrintSyuryo;
        final String _documentRoot;
        final String _imagePath;
        final String _extension;
        final String _whiteSpaceImagePath;
        final String _descDate;
        final String _notPrintAttendance;
        final String _ouinranShuturyoku;
        final String _shokenShuturyoku;

        final String _z010;
        final boolean _isFukuchiyama;
        final boolean _isMiyagi;
        final boolean _isKyoto;

        final String _gradeCdStr;
        final String _certifSchoolSchoolName;
        final String _certifSchoolRemark3;
        final String _certifSchoolRemark4;
        final String _certifSchoolRemark5;
        final String _certifSchoolRemark6;
        final String _certifSchoolRemark7;
        final String _certifSchoolPrincipalName;
        final String _tr1Name;
        final String _hyosiHrName;
        final String _certifSchoolJobName;
        final String _d016Namespare1;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final Map _attendParamMap;

        final String _HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J;
        final String _reportSpecialSize01_01;
        final String _reportSpecialSize01_02;
        final String _reportSpecialSize02_01;
        final String _reportSpecialSize03_01;
        final String _reportSpecialSize04_01;
        final String _tutisyoSougouHyoukaTunen;
        final String _useFormNameD181J_3;
        final String _documentMstSize_B4;
        final String _documentMstB4Text;

       /** 各学校における定数等設定 */
        KNJDefineCode _definecode;
        KNJDefineSchool _defineSchoolCode;
        KNJSchoolMst _knjSchoolMst;

        final List _d065Name1List;
        final Map _d001Abbv1Map;

        final boolean _notoutput_edutarget;
        final String _reportSpecialSize04_01Title;

        final String _tutisyoShokennTunen;
        final String _tutisyoShokennSemesterTitle;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _categorySelected = request.getParameterValues("category_selected");
            _trCd1 = request.getParameter("TR_CD1");
            _isPrintHyoshi  = null != request.getParameter("PRINT_SIDE1");
            _isPrintSeiseki = null != request.getParameter("PRINT_SIDE2");
            _isPrintSyoken  = null != request.getParameter("PRINT_SIDE3");
            _isPrintSyuryo  = null != request.getParameter("PRINT_SIDE4");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _imagePath = request.getParameter("IMAGEPATH");

            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
            _whiteSpaceImagePath = getImagePath("whitespace.png");
            _descDate = request.getParameter("DESC_DATE");

            _notPrintAttendance = request.getParameter("NOT_PRINT_ATTEND");
            _ouinranShuturyoku = request.getParameter("OUINRAN_SHUTURYOKU");
            _shokenShuturyoku = request.getParameter("SHOKEN_SHUTURYOKU");

            _z010 = setNameMst(db2, "Z010", "00");
            _isFukuchiyama = null != _documentRoot && _documentRoot.indexOf("fukuchiyama-jhs") != -1;
            _isMiyagi = "miyagiken".equals(_z010);
            _isKyoto  = "kyoto".equals(_z010);

            _gradeCdStr = getGradeCd(db2, _grade);
            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolRemark3 = getCertifSchoolDat(db2, "REMARK3");
            _certifSchoolRemark4 = getCertifSchoolDat(db2, "REMARK4");
            _certifSchoolRemark5 = getCertifSchoolDat(db2, "REMARK5");
            _certifSchoolRemark6 = getCertifSchoolDat(db2, "REMARK6");
            _certifSchoolRemark7 = getCertifSchoolDat(db2, "REMARK7");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _tr1Name = getStaffname(db2, _trCd1);
            _hyosiHrName = "第" + _gradeCdStr + "学年" + getHrClassName1(db2, _year, _semester, _gradeHrclass) + "組";
            _certifSchoolJobName = getCertifSchoolDat(db2, "JOB_NAME");
            _d016Namespare1 = getNameMst(db2, _year, "D016", "01", "NAMESPARE1");

            _HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J = null == request.getParameter("HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"), "+", " "); // 出欠の記録備考
            _reportSpecialSize01_01 = null == request.getParameter("reportSpecialSize01_01") ? "" : StringUtils.replace(request.getParameter("reportSpecialSize01_01"), "+", " "); // 総合的な学習の時間学習活動
            _reportSpecialSize01_02 = null == request.getParameter("reportSpecialSize01_02") ? "" : StringUtils.replace(request.getParameter("reportSpecialSize01_02"), "+", " "); // 総合的な学習の時間評価
            _reportSpecialSize02_01 = null == request.getParameter("reportSpecialSize02_01") ? "" : StringUtils.replace(request.getParameter("reportSpecialSize02_01"), "+", " "); // 特別活動
            _reportSpecialSize03_01 = null == request.getParameter("reportSpecialSize03_01") ? "" : StringUtils.replace(request.getParameter("reportSpecialSize03_01"), "+", " "); // 部活動
            _reportSpecialSize04_01 = null == request.getParameter("reportSpecialSize04_01") ? "" : StringUtils.replace(request.getParameter("reportSpecialSize04_01"), "+", " "); // 所見
            _tutisyoSougouHyoukaTunen = request.getParameter("tutisyoSougouHyoukaTunen");
            _useFormNameD181J_3 = request.getParameter("useFormNameD181J_3");
            _documentMstSize_B4 = request.getParameter("documentMstSize_B4");
            _documentMstB4Text = getDocumentMst(db2, "B4", "TEXT");
            _tutisyoShokennTunen = request.getParameter("tutisyoShokenntunen");
            _tutisyoShokennSemesterTitle = request.getParameter("tutisyoShokennSemesterTitle");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));

            _d065Name1List = getD065Name1List(db2);
            _d001Abbv1Map = getD001Abbv1Map(db2);

            _notoutput_edutarget = "1".equals(request.getParameter("NOTOUTPUT_EDUTARGET"));
            String reportSpecialSize04_01Title = null;
            try {
                reportSpecialSize04_01Title = new String(request.getParameter("reportSpecialSize04_01Title").getBytes("ISO8859-1"));
            } catch (Exception e) {
                log.error("exception!", e);
            }
            _reportSpecialSize04_01Title = reportSpecialSize04_01Title;
        }

        /**
         * 写真データ格納フォルダの取得 --NO001
         */
        private KNJ_Control.ReturnVal getDocumentroot(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }

        private String getHrClassName1(final DB2UDB db2, final String year, final String semester, final String gradeHrclass) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT HR_CLASS_NAME1 FROM SCHREG_REGD_HDAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("HR_CLASS_NAME1")) {
                        rtn = rs.getString("HR_CLASS_NAME1");
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (null == rtn) {
                try {
                    final String sql = " SELECT HR_CLASS FROM SCHREG_REGD_DAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' ";
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        if (null == rtn && null != rs.getString("HR_CLASS")) {
                            rtn = NumberUtils.isDigits(rs.getString("HR_CLASS")) ? String.valueOf(Integer.parseInt(rs.getString("HR_CLASS"))) : rs.getString("HR_CLASS");
                        }
                    }
                } catch (SQLException e) {
                    log.error("exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
            if (null == rtn) {
                rtn = "　";
            } else {
                rtn = hankakuToZenkaku(rtn);
            }
            return rtn;
        }

        private String getNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    " + field + " ");
                sql.append(" FROM NAME_MST T1 ");
                sql.append("    INNER JOIN NAME_YDAT T2 ON T2.YEAR = '" + year + "' AND T2.NAMECD1 = T1.NAMECD1 AND T2.NAMECD2 = T1.NAMECD2 ");
                sql.append(" WHERE ");
                sql.append("    T1.NAMECD1 = '" + namecd1 + "' ");
                sql.append("    AND T1.NAMECD2 = '" + namecd2 + "' ");
                sql.append("   ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getStaffname(final DB2UDB db2, final String trCd1) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + trCd1 + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("STAFFNAME")) {
                        rtn = rs.getString("STAFFNAME");
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        public String getImagePath(final String name) {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '103' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private static String hankakuToZenkaku(final String str) {
            if (null == str) {
                return null;
            }
            final String[] nums = new String[]{"０", "１", "２", "３", "４", "５", "６", "７", "８", "９"};
            final StringBuffer stb = new StringBuffer();
            for (int i = 0; i < str.length(); i++) {
                final String s = String.valueOf(str.charAt(i));
                if (NumberUtils.isDigits(s)) {
                    final int j = Integer.parseInt(s);
                    stb.append(nums[j]);
                } else {
                    stb.append(s);
                }
            }
            return stb.toString();
        }


        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT "
                                     + "     * "
                                     + " FROM "
                                     + "     V_NAME_MST "
                                     + " WHERE "
                                     + "     YEAR = '" + _year + "' "
                                     + "     AND NAMECD1 = '" + namecd1 + "' "
                                     + "     AND NAMECD2 = '" + namecd2 + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtn;
        }

        private String getGradeCd(final DB2UDB db2, final String grade) {
            String gradeCd = "　";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.SCHOOL_KIND = 'J' ");
                stb.append("     AND T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String tmp = rs.getString("GRADE_CD");
                    if (NumberUtils.isDigits(tmp)) {
                        gradeCd = hankakuToZenkaku(String.valueOf(Integer.parseInt(tmp)));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradeCd;
        }

        private List getD065Name1List(final DB2UDB db2) {
            final List list = new ArrayList();
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D065' AND NAME1 IS NOT NULL ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(StringUtils.replace(rs.getString("NAME1"), "-", ""));
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private Map getD001Abbv1Map(final DB2UDB db2) {
            final Map list = new HashMap();
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAMECD2, ABBV1 FROM NAME_MST WHERE NAMECD1 = 'D001' AND ABBV1 IS NOT NULL ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    list.put(rs.getString("NAMECD2"), rs.getString("ABBV1"));
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }


        private String getDocumentMst(final DB2UDB db2, final String documentcd, final String field) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT " + field + " FROM DOCUMENT_MST WHERE DOCUMENTCD = '" + documentcd + "' "));
        }
    }
}

// eof


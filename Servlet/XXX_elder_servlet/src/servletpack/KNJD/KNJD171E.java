/*
 * $Id$
 *
 * 作成日: 2011/01/12
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
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
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;

/**
 * 学校教育システム 賢者 [成績管理] 学通知票
 */

public class KNJD171E {

    private static final Log log = LogFactory.getLog(KNJD171E.class);

    /** 右詰 */
    private static final String ATTR_RIGHT = "Hensyu=1";

    private final String ATTEND_MONTH89_REPLACE_YEAR = "2020";

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

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static TreeMap getMappedTreeMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (TreeMap) map.get(key1);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        _param._viewClassList = ViewClass.getViewClassList(db2, _param);
        if ("2".equals(_param._recordDiv)) {
            _param._subclassGradeMap = SubclassGrade.getSubclassGradeMap(db2, _param);
        }

        final List studentList = Student.getStudentList(db2, _param);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            log.info(" student = " + student._schregno);
            printStudent(svf, student);
            _hasData = true;
        }
    }

    private void VrsOutnRenban(final Vrw32alp svf, final String field, final List token) {
        if (null != token) {
            for (int i = 0 ; i < token.size(); i++) {
                svf.VrsOutn(field, i + 1, (String) token.get(i));
            }
        }
    }

    /**
     * "w * h"サイズタイプのパラメータの[w, h]を整数値配列で返す
     * @param parameter サイズタイプのパラメータ文字列
     * @param defwh デフォルトの[w, h]
     * @return "w * h"サイズタイプのパラメータの[w, h]の整数値配列
     */
    private static int[] getParamSize(final String parameter, final int[] defwh) {
        final int[] rtn = {defwh[0], defwh[1]};
        String[] nums = StringUtils.split(parameter, " * ");
        if (!StringUtils.isBlank(parameter)) {
            try {
                rtn[0] = Integer.valueOf(nums[0]).intValue();
                rtn[1] = Integer.valueOf(nums[1]).intValue();
            } catch (Exception e) {
                log.error("Exception! param = " + parameter, e);
            }
        }
        return rtn;
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
     * 学習のようす等を印刷する
     * @param svf
     * @param student
     */
    private void printStudent(final Vrw32alp svf, final Student student) {
        if ("2".equals(_param._recordDiv)) {
            final String form = "1".equals(_param._semester) ? "KNJD171E_4_1.frm" : "KNJD171E_4_2.frm";

            svf.VrSetForm(form, 4);
            _param._fieldMap = SvfField.getSvfFormFieldInfoMapGroupByName(svf);

            printKotei(svf, student);

            printAttendance(svf, student);

            printView2(svf, student);
        } else {
            final String form;
            final int viewmax;
            if ("01".equals(_param._gradeCd) || "02".equals(_param._gradeCd)) {
                form = "1".equals(_param._semester) ? "KNJD171E_1_1.frm" : "KNJD171E_1_2.frm";
                viewmax = 24;
            } else if ("03".equals(_param._gradeCd) || "04".equals(_param._gradeCd)) {
                form = "1".equals(_param._semester) ? "KNJD171E_2_1.frm" : "KNJD171E_2_2.frm";
                viewmax = 35;
            } else {
                form = "1".equals(_param._semester) ? "KNJD171E_3_1.frm" : "KNJD171E_3_2.frm";
                viewmax = 35;
            }

            svf.VrSetForm(form, 4);
            _param._fieldMap = SvfField.getSvfFormFieldInfoMapGroupByName(svf);

            printKotei(svf, student);

            printAttendance(svf, student);

            printShoken(svf, student);

            if ("01".equals(_param._gradeCd) || "02".equals(_param._gradeCd)) {
                printSubclassStaffname3456(svf, student);
            } else if ("03".equals(_param._gradeCd) || "04".equals(_param._gradeCd)) {
                printSubclassStaffname3456(svf, student);
            } else {
                printSubclassStaffname3456(svf, student);
            }

            printView(svf, student, viewmax);
        }
    }


    private void printSubclassStaffnameEtc(final Vrw32alp svf, final Student student) {
        int i = 1;
        for (final Iterator it = student._subclassStaffNameMap.keySet().iterator(); it.hasNext();) {
            final String subclasscd = (String) it.next();
            final String classname = getSubclassName(subclasscd);
            svf.VrsOut("SP_SUBJECT" + String.valueOf(i), classname);
            final TreeMap appendDateStaffnameMap = getMappedTreeMap(student._subclassStaffNameMap, subclasscd);
            if (!appendDateStaffnameMap.isEmpty()) {
                final List staffnameList = getMappedList(appendDateStaffnameMap, (String) appendDateStaffnameMap.lastKey());
                if (staffnameList.size() > 0) {
                    final String name0 = (String) staffnameList.get(0);
                    final int keta0 = KNJ_EditEdit.getMS932ByteLength(name0);
                    if (staffnameList.size() > 1) {
                        final String name1 = (String) staffnameList.get(1);
                        final int keta1 = KNJ_EditEdit.getMS932ByteLength(name1);
                        svf.VrsOut("SP_STAFF_NAME" + String.valueOf(i) + "_2_" + (keta0 > 14 ? "2" : "1"), name0);
                        svf.VrsOut("SP_STAFF_NAME" + String.valueOf(i) + "_3_" + (keta1 > 14 ? "2" : "1"), name1);
                    } else {
                        svf.VrsOut("SP_STAFF_NAME" + String.valueOf(i) + "_1_" + (keta0 > 14 ? "2" : "1"), name0);
                    }
                }
                i += 1;
            }
        }
    }

    private void printSubclassStaffname3456(final Vrw32alp svf, final Student student) {
        int i = 1;
        int col = 1;
        for (final Iterator it = student._subclassStaffNameMap.keySet().iterator(); it.hasNext();) {
            final String subclasscd = (String) it.next();
            final String classname = getSubclassName(subclasscd);
            svf.VrsOutn("SP_SUBJECT" + String.valueOf(i) + (KNJ_EditEdit.getMS932ByteLength(classname) > 6 ? "_2" : ""), col, classname);
            final TreeMap appendDateStaffnameMap = getMappedTreeMap(student._subclassStaffNameMap, subclasscd);
            if (!appendDateStaffnameMap.isEmpty()) {
                final List staffnameList = getMappedList(appendDateStaffnameMap, (String) appendDateStaffnameMap.lastKey());
                if (staffnameList.size() > 0) {
                    final String name0 = (String) staffnameList.get(0);
                    final int keta0 = KNJ_EditEdit.getMS932ByteLength(name0);
                    if (staffnameList.size() > 1) {
                        final String name1 = (String) staffnameList.get(1);
                        final int keta1 = KNJ_EditEdit.getMS932ByteLength(name1);
                        svf.VrsOutn("SP_STAFF_NAME" + String.valueOf(i) + "_2_" + (keta0 > 22 ? "3" : keta0 > 14 ? "2" : "1"), col, name0);
                        svf.VrsOutn("SP_STAFF_NAME" + String.valueOf(i) + "_3_" + (keta1 > 22 ? "3" : keta1 > 14 ? "2" : "1"), col, name1);
                    } else {
                        svf.VrsOutn("SP_STAFF_NAME" + String.valueOf(i) + "_1_" + (keta0 > 22 ? "3" : keta0 > 14 ? "2" : "1"), col, name0);
                    }
                }
                i += 1;
                if (i > 6) {
                    col += 1;
                    i = 1;
                }
            }
        }
    }

    private void printView2(final Vrw32alp svf, final Student student) {
        final int mongonHyoukaMax = 3;
        int kamokuIdx = 1;
//        for (final Iterator it = _param._viewClassList.iterator(); it.hasNext();) {
//            final ViewClass viewClass = (ViewClass) it.next();
//
//            final SubclassGrade subclassGrade = (SubclassGrade) _param._subclassGradeMap.get(viewClass._subclasscd);
//            if (null == subclassGrade || !"1".equals(subclassGrade._textHyokaFlg)) {
//                continue;
//            }
//
//            svf.VrsOut("SUBJECT" + String.valueOf(kamokuIdx), spaced(viewClass._classname));
//            printSubclassStaffname(svf, staffIdx++, viewClass._subclasscd, viewClass._classname, student);
//
//            final String remark1 = (String) student._jviewstatReportremarkRemark1Map.get(viewClass._subclasscd);
//            final String[] token = KNJ_EditEdit.get_token_1(remark1, 50 * 2, 2);
//            if (null != token) {
//                for (int i = 0; i < token.length; i++) {
//                    svf.VrsOut("EVALUATION" + String.valueOf(kamokuIdx), token[i]);
//                }
//            }
//            kamokuIdx += 1;
//            if (kamokuIdx > mongonHyoukaMax) {
//                // mongonHyoukaMax科目まで表示
//                break;
//            }
//        }

        printSubclassStaffnameEtc(svf, student);

        for (final Iterator it = _param._subclassGradeMap.keySet().iterator(); it.hasNext();) {
            final String subclasscd = (String) it.next();

            final SubclassGrade subclassGrade = (SubclassGrade) _param._subclassGradeMap.get(subclasscd);
            if (null != subclassGrade && "1".equals(subclassGrade._textHyokaFlg)) {

                svf.VrsOut("SUBJECT" + String.valueOf(kamokuIdx), spaced(getSubclassName(subclasscd)));

                final String remark1 = (String) student._jviewstatReportremarkRemark1Map.get(subclasscd);
                final List token = KNJ_EditKinsoku.getTokenList(remark1, 50 * 2, 3);
                if (null != token) {
                    for (int i = 0 ; i < token.size(); i++) {
                        svf.VrsOutn("EVALUATION" + String.valueOf(kamokuIdx), i + 1, (String) token.get(i));
                    }
                }
                kamokuIdx += 1;
                if (kamokuIdx > mongonHyoukaMax) {
                    // mongonHyoukaMax科目まで表示
                    break;
                }
            }
        }

        svf.VrsOut("SUBJECT" + String.valueOf(4), "　専科");
        for (final Iterator it = _param._viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();

            final SubclassGrade subclassGrade = (SubclassGrade) _param._subclassGradeMap.get(viewClass._subclasscd);
            if (null != subclassGrade && "1".equals(subclassGrade._textHyokaFlg)) {
                continue;
            }
            printRecord(svf, 2, student, viewClass);
        }
    }

    public String getSubclassName(final String subclasscd) {
        return (String) _param._subclassNameMap.get(subclasscd);
    }

    private String spaced(final String title) {
        if (null == title) {
            return title;
        }
        return "　" + StringUtils.defaultString(title);
//        final StringBuffer stb = new StringBuffer(subclassname);
//        for (int i = subclassname.length() - 1; i >= 0; i--) {
//            stb.insert(i, "　");
//        }
//        return stb.toString();
    }

    public void printShoken(final Vrw32alp svf, final Student student) {
        // 評価の観点
        for (final Iterator it = student._behaviorSemesDatList.iterator(); it.hasNext();) {
            final BehaviorSemesDat bsd = (BehaviorSemesDat) it.next();
            if (NumberUtils.isDigits(bsd._code)) {
                final int keta = KNJ_EditEdit.getMS932ByteLength(bsd._viewname);
                svf.VrsOutn("VIEWNAME4" + (keta > 40 ? "_3" : keta > 36 ? "_2" : ""), Integer.parseInt(bsd._code), bsd._viewname);
                if (null != bsd._record) {
                    svf.VrsOutn("VIEW4", Integer.parseInt(bsd._code), "○");
                }
            }
        }

        // 特別活動の記録
        final int[] spActMojisuGyo = getParamSize(_param._HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P, new int[] {17, 2});
        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();

            VrsOutnRenban(svf, "SPECIAL_ACT", KNJ_EditKinsoku.getTokenList(hReportRemarkDat._specialactremark, spActMojisuGyo[0] * 2, spActMojisuGyo[1]));
        }

        // 先生から
        final int[] commuMojisuGyo = getParamSize(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_P, new int[] {17, 8});
        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();

            VrsOutnRenban(svf, "TOTAL_ACT", KNJ_EditKinsoku.getTokenList(hReportRemarkDat._communication, commuMojisuGyo[0] * 2, commuMojisuGyo[1]));
        }
    }

    public void printAttendance(final Vrw32alp svf, final Student student) {
        String[] months;
        if ("1".equals(_param._semester)) {
            months = new String[] {"04", "05", "06", "07"};
        } else {
            months = new String[] {"09", "10", "11", "12", "01", "02", "03"};
        }

        for (int i = 0; i < months.length; i++) {
            AttendSemesDat attSemes = student._attendSemesMap.get(months[i]);
            final int j = i + 1;
            if (ATTEND_MONTH89_REPLACE_YEAR.equals(_param._ctrlYear) && "09".equals(months[i])) {
                svf.VrsOutn("MONTH", j, "月");
                final int addX = "2".equals(_param._recordDiv) ? 17 : 7;
                final SvfField monthField = _param._fieldMap.get("MONTH");
                svf.VrAttributen("MONTH", j, ATTR_RIGHT + ",X=" + String.valueOf(monthField.x() + addX));
                svf.VrsOut("MONTH2", "8･9");
                attSemes = AttendSemesDat.add(student._attendSemesMap.get("08"), attSemes);
            } else {
                svf.VrsOutn("MONTH", j, String.valueOf(Integer.parseInt(months[i])) + "月");
            }

            if (null == attSemes) {
                continue;
            }
            svf.VrsOutn("LESSON", j, attendVal(attSemes._lesson));
            svf.VrsOutn("SUSPEND", j, attendVal(attSemes._suspend + attSemes._mourning + attSemes._virus + attSemes._koudome));
            svf.VrsOutn("ATTEND", j, attendVal(attSemes._sick));
            svf.VrsOutn("ABSENCE", j, attendVal(attSemes._present));
            svf.VrsOutn("LATE", j, attendVal(attSemes._late));
            svf.VrsOutn("EARLY", j, attendVal(attSemes._early));
        }
    }

    public void printKotei(final Vrw32alp svf, final Student student) {
        svf.VrsOut("NENDO", _param._ctrlYear + "年度　" + StringUtils.defaultString(_param._semestername));
        svf.VrsOut("CORP_NAME", StringUtils.defaultString(_param._certifSchoolDatRemark1) + "　" + StringUtils.defaultString(_param._certifSchoolDatRemark2));
        svf.VrsOut("SCHOOL_NAME" + (KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolDatSchoolName) > 22 ? "_2" : ""), _param._certifSchoolDatSchoolName);
//        svf.VrsOut("LOGO", _param.getImagePath());
        svf.VrsOut("HR_NAME", student._hrName);
        svf.VrsOut("ATTENDNO", student.getAttendnoStr());
        final int namelen = KNJ_EditEdit.getMS932ByteLength(student._name);
        svf.VrsOut("NAME" + (namelen > 20 ? "3_1" : namelen > 16 ? "2" : "1"), student._name);
        svf.VrsOut("STAFF_NAME1" + (KNJ_EditEdit.getMS932ByteLength(trimLeft(_param._certifSchoolDatPrincipalName)) > 20 ? "_2" : ""), trimLeft(_param._certifSchoolDatPrincipalName));
        svf.VrsOut("STAFF_NAME2" + (KNJ_EditEdit.getMS932ByteLength(student._tr1Name) > 20 ? "_2" : ""), student._tr1Name);
        svf.VrsOut("STAFF_NAME3" + (KNJ_EditEdit.getMS932ByteLength(student._subtr1Name) > 20 ? "_2" : ""), student._subtr1Name);
        svf.VrsOut("STAFF_NAME4" + (KNJ_EditEdit.getMS932ByteLength(student._subtr2Name) > 20 ? "_2" : ""), student._subtr2Name);
    }

    private static String attendVal(int n) {
        return String.valueOf(n);
    }

    /**
     * 学習の記録を印字する
     * @param svf
     * @param student
     */
    private void printView(final Vrw32alp svf, final Student student, final int viewmax) {
//        int viewSize1 = 0;
//        int viewSize0 = 0;
//        for (final Iterator it = viewClassList.iterator(); it.hasNext();) {
//            final ViewClass viewClass = (ViewClass) it.next();
//            final int size = viewClass.getViewSize() + viewClass.getViewSize() % 2;
//            if ("1".equals(viewClass._electDiv)) {
//                if (student.hasChairSubclass(viewClass._subclasscd)) { // 選択教科は講座名簿がある科目のみ表示対象
//                    viewSize1 += size;
//                }
//            } else {
//                viewSize0 += size;
//            }
//        }
//        final String grpName = spacedName("必修教科", viewSize0) + spacedName("選択教科", viewSize1);

        int lineSum = 0;
        for (final Iterator it = _param._viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();
            lineSum += printRecord(svf, 1, student, viewClass);
        }
        if (lineSum == 0) {
            svf.VrsOut("CLASS", "00");
            svf.VrEndRecord();
        }
        //log.info(" student._kokoroSubclasscdList = " + student._kokoroSubclasscdList);
        for (final Iterator it = student._kokoroSubclasscdList.iterator(); it.hasNext();) {
            final String subclasscd = (String) it.next();
            final String subclassname = (String) _param._subclassNameMap.get(subclasscd);
            final String shoken = (String) student._kokoroShokenMap.get(subclasscd);
            svf.VrsOut("CLASSNAME1_3", subclassname);
            final String[] fields = {"VIEWNAME1_3", "VIEWNAME1_4"};
            final List tokenList = KNJ_EditKinsoku.getTokenList(shoken, 80);
            for (int i = 0; i < Math.min(fields.length, tokenList.size()); i++) {
                final String token = (String) tokenList.get(i);
                svf.VrsOut(fields[i], token);
            }
            svf.VrEndRecord();
        }
    }

    /**
     * 学習の記録を印字する
     * @param svf
     * @param student
     */
    private int printRecord(final Vrw32alp svf, final int flg, final Student student, final ViewClass viewClass) {

        int line = 0; // 観点の行数

//      if ("1".equals(viewClass._electDiv) && !student.hasChairSubclass(viewClass._subclasscd)) { // 選択教科は講座名簿がある科目のみ表示対象
//          continue;
//      }

        final int viewsize = viewClass.getViewSize();
        String classname = StringUtils.defaultString(getSubclassName(viewClass._subclasscd));
        if (viewsize > classname.length()) {
            final int nokori = viewsize - classname.length();
            if (flg == 2) {
                classname = classname + StringUtils.repeat("　", nokori);
            } else {
                final String space = StringUtils.repeat("　", nokori / 2);
                classname = space + classname + space;
                if (nokori % 2 == 1) {
                    classname += "　";
                }
            }
        }

        for (int i = 0, max = classname.length(); i < max; i++) {
            final String suf = i != 0 ? "_2" : "";
//            svf.VrsOut("SUBJECTGRPNAME" + suf, (line < grpName.length()) ? String.valueOf(grpName.charAt(line)) : " ");
//            svf.VrsOut("SUBJECTGRP" + suf, viewClass._electDiv);
            svf.VrsOut("CLASS" + suf, viewClass._classcd);
//            if (getMS932ByteCount(viewClass._subclassname) > 10) {
//                VrsOutRenban(svf, "SUBJECT2_", knjobj.retDividString(viewClass._subclassname, 12, 2));
//            } else {
//                svf.VrsOut("SUBJECT1", viewClass._subclassname);
//            }
            if (i < classname.length()) {
                svf.VrsOut("CLASSNAME1" + suf, classname.substring(i, i + 1));
            }
            if (i < viewsize) {
                final String viewname = viewClass.getViewName(i);
                final int viewnamelen = KNJ_EditEdit.getMS932ByteLength(viewname);
                if (flg == 2) {
                    svf.VrsOut("VIEWNAME" + (viewnamelen > 80 ? "3" : viewnamelen > 60 ? "2" : viewnamelen > 50 ? "1" : "0") + suf, viewname); // 観点名称
                } else {
                    svf.VrsOut("VIEWNAME" + (viewnamelen > 80 ? "3" : viewnamelen > 60 ? "2" : "1") + suf, viewname); // 観点名称
                }

                final List viewRecordList = student.getViewList(viewClass._subclasscd, viewClass.getViewCd(i));
                for (final Iterator itv = viewRecordList.iterator(); itv.hasNext();) {
                    final ViewRecord viewRecord = (ViewRecord) itv.next();
//                    log.debug(" status = " + viewRecord._status);
//                    if (!NumberUtils.isDigits(viewRecord._status)) {
//                        continue;
//                    }
                    String field = null;
                    if ("C".equals(viewRecord._status)) { // 「もうすこし」
                        field = "VIEW3";
                    } else if ("B".equals(viewRecord._status)) { // 「よい」
                        field = "VIEW2";
                    } else if ("A".equals(viewRecord._status)) { // 「とてもよい」
                        field = "VIEW1";
                    }
                    if (null != field) {
                        svf.VrsOut(field + suf, "○");
                    }
                }

            }

            svf.VrEndRecord();
            line += 1;
        }
        return line;
    }

    private static class Student {
        String _schregno;
        String _name;
        String _hrName;
        String _attendno;
        String _recordDiv;
        String _trCd1;
        String _subtrCd1;
        String _tr1Name;
        String _subtr1Name;
        String _subtr2Name;

        List _viewRecordList = Collections.EMPTY_LIST; // 観点
        Map<String, AttendSemesDat> _attendSemesMap = Collections.EMPTY_MAP; // 出欠の記録
        List _behaviorSemesDatList = Collections.EMPTY_LIST; // 学校生活のようす
        List _hReportRemarkDatList = Collections.EMPTY_LIST; // 所見
        Map _jviewstatReportremarkRemark1Map = Collections.EMPTY_MAP;
        List _kokoroSubclasscdList = Collections.EMPTY_LIST;
        Map _subclassStaffNameMap = Collections.EMPTY_MAP;
        Map _kokoroShokenMap = Collections.EMPTY_MAP;

        public String getAttendnoStr() {
            return null == _attendno ? "" : (NumberUtils.isDigits(_attendno)) ? String.valueOf(Integer.parseInt(_attendno)) : _attendno;
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

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentSql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = new Student();
                    student._schregno = rs.getString("SCHREGNO");
                    student._name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    student._hrName = "1".equals(param._printRegd) ? rs.getString("REGDH_HR_NAME") : rs.getString("HR_NAME");
                    student._attendno = rs.getString("ATTENDNO");
                    student._recordDiv = rs.getString("RECORD_DIV");
                    student._trCd1 = rs.getString("TR_CD1");
                    student._subtrCd1 = rs.getString("SUBTR_CD1");
                    student._tr1Name = rs.getString("TR_CD1_STAFFNAME");
                    student._subtr1Name = rs.getString("SUBTR_CD1_STAFFNAME");
                    student._subtr2Name = rs.getString("SUBTR_CD2_STAFFNAME");

                    studentList.add(student);
                }

            } catch (SQLException e) {
                log.error("exception!!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            ViewRecord.setViewRecordList(db2, param, studentList);
            AttendSemesDat.setAttendSemesDatMap(db2, param, studentList);
            BehaviorSemesDat.setBehaviourSemesDatList(db2, param, studentList);
            HReportRemarkDat.setHReportRemarkDatList(db2, param, studentList);
            HReportRemarkDat.setJviewstatReportremarkDat(db2, param, studentList);
            ChairSubclass.setSubclass(db2, param, studentList);
            return studentList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("    SELECT ");
            stb.append("          T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append("        , REGDH.HR_NAME AS REGDH_HR_NAME ");
            stb.append("        , CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME ");
            stb.append("        , BASE.NAME ");
            stb.append("        , BASE.REAL_NAME ");
            stb.append("        , FI_REGDH.HR_NAME ");
            stb.append("        , FI_REGDH.RECORD_DIV ");
            if ("1".equals(param._printRegd)) {
                stb.append("        , REGDH.TR_CD1 ");
                stb.append("        , REGDH.SUBTR_CD1 ");
                stb.append("        , REGDH.SUBTR_CD2 ");
            } else {
                stb.append("        , FI_REGDH.TR_CD1 ");
                stb.append("        , FI_REGDH.SUBTR_CD1 ");
                stb.append("        , FI_REGDH.SUBTR_CD2 ");
            }
            stb.append("        , STF1.STAFFNAME AS TR_CD1_STAFFNAME ");
            stb.append("        , STF1S.STAFFNAME AS SUBTR_CD1_STAFFNAME ");
            stb.append("        , STF2S.STAFFNAME AS SUBTR_CD2_STAFFNAME ");
            stb.append("    FROM    SCHREG_REGD_FI_DAT T1 ");
            stb.append("    INNER JOIN V_SEMESTER_GRADE_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T1.GRADE = T2.GRADE ");
            stb.append("    LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR ");
            stb.append("        AND T1.SEMESTER = REGD.SEMESTER ");
            stb.append("        AND T1.SCHREGNO = REGD.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("        AND REGD.SEMESTER = REGDH.SEMESTER ");
            stb.append("        AND REGD.GRADE = REGDH.GRADE ");
            stb.append("        AND REGD.HR_CLASS = REGDH.HR_CLASS ");
            stb.append("    LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
            stb.append("    INNER JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_REGD_FI_HDAT FI_REGDH ON FI_REGDH.YEAR = T1.YEAR AND FI_REGDH.SEMESTER = T1.SEMESTER AND FI_REGDH.GRADE = T1.GRADE AND FI_REGDH.HR_CLASS = T1.HR_CLASS ");
            if ("1".equals(param._printRegd)) {
                stb.append("    LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
                stb.append("    LEFT JOIN STAFF_MST STF1S ON STF1S.STAFFCD = REGDH.SUBTR_CD1 ");
                stb.append("    LEFT JOIN STAFF_MST STF2S ON STF2S.STAFFCD = REGDH.SUBTR_CD2 ");
            } else {
                stb.append("    LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = FI_REGDH.TR_CD1 ");
                stb.append("    LEFT JOIN STAFF_MST STF1S ON STF1S.STAFFCD = FI_REGDH.SUBTR_CD1 ");
                stb.append("    LEFT JOIN STAFF_MST STF2S ON STF2S.STAFFCD = FI_REGDH.SUBTR_CD2 ");
            }
            stb.append("    WHERE   T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("        AND T1.SEMESTER = '"+ param._semester +"' ");
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
            stb.append("    ORDER BY T1.ATTENDNO");
            return stb.toString();
        }
    }

    /**
     * 観点の教科
     */
    private static class ViewClass {
        final String _classcd;
        final String _subclasscd;
        final String _electDiv;
        final List _viewList;
        final List _valuationList;
        ViewClass(
                final String classcd,
                final String subclasscd,
                final String electDiv) {
            _classcd = classcd;
            _subclasscd = subclasscd;
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
                log.debug(" view class sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
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
                        viewClass = new ViewClass(classcd, subclasscd, electDiv);
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
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     VALUE(T4.ELECTDIV, '0') AS ELECTDIV, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     DET.REMARK1 AS VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._ctrlYear + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_DETAIL_MST DET ON DET.GRADE = T1.GRADE ");
            stb.append("         AND DET.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND DET.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND DET.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND DET.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND DET.VIEWCD = T1.VIEWCD ");
            stb.append("         AND DET.VIEW_SEQ = '00" + param._semester + "' ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_DETAIL_MST DET4 ON DET4.GRADE = T1.GRADE ");
            stb.append("         AND DET4.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND DET4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND DET4.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND DET4.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND DET4.VIEWCD = T1.VIEWCD ");
            stb.append("         AND DET4.VIEW_SEQ = '004' ");
            stb.append("         AND DET4.REMARK1 = '" + param._recordDiv + "' ");
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
            stb.append("     AND DET.REMARK1 IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T4.ELECTDIV, '0'), ");
            stb.append("     VALUE(T3.SHOWORDER3, -1), ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
        }
    }

    /**
     * 学習の記録（観点）
     */
    private static class ViewRecord {

//        String _semester;
        String _viewcd;
        String _status;
        String _grade;
        String _viewname;
        String _classcd;
        String _subclasscd;
        String _classMstShoworder;
        String _showorder;

        public static void setViewRecordList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewRecordSql(param);
                log.debug(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    student._viewRecordList = new ArrayList();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final ViewRecord viewRecord = new ViewRecord();
//                        viewRecord._semester = rs.getString("SEMESTER");
                        viewRecord._viewcd = rs.getString("VIEWCD");
                        viewRecord._status = rs.getString("STATUS");
                        viewRecord._grade = rs.getString("GRADE");
                        viewRecord._viewname = rs.getString("VIEWNAME");
                        viewRecord._classcd = rs.getString("CLASSCD");
                        viewRecord._subclasscd = rs.getString("SUBCLASSCD");
                        viewRecord._classMstShoworder = rs.getString("CLASS_MST_SHOWORDER");
                        viewRecord._showorder = rs.getString("SHOWORDER");

                        student._viewRecordList .add(viewRecord);
                    }
                    DbUtils.closeQuietly(rs);
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
                stb.append("     ,T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
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
            stb.append("         AND T3.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND T3.SCHREGNO = ? ");
            stb.append("     LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            }
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._ctrlYear + "' ");
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
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _month;
        int _lesson;
        int _suspend;
        int _mourning;
        int _mlesson;
        int _sick;
        int _absent;
        int _present;
        int _late;
        int _early;
        int _abroad;
        int _offdays;
//        int _kekkaJisu;
        int _virus;
        int _koudome;

        public AttendSemesDat(
                final String month
        ) {
            _month = month;
        }

        public static AttendSemesDat add(
                final AttendSemesDat a,
                final AttendSemesDat b
        ) {
            if (null == a) return b;
            if (null == b) return a;
            final AttendSemesDat rtn = new AttendSemesDat(a._month);
            rtn._lesson = a._lesson;
            rtn._suspend = a._suspend;
            rtn._mourning = a._mourning;
            rtn._mlesson = a._mlesson;
            rtn._sick = a._sick;
            rtn._absent = a._absent;
            rtn._present = a._present;
            rtn._late = a._late;
            rtn._early = a._early;
            rtn._abroad = a._abroad;
            rtn._offdays = a._offdays;
//            rtn._kekkaJisu = a._kekkaJisu;
            rtn._virus = a._virus;
            rtn._koudome = a._koudome;
            rtn.add(b);
            return rtn;
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
            _abroad += o._abroad;
            _offdays += o._offdays;
//            _kekkaJisu += o._kekkaJisu;
            _virus += o._virus;
            _koudome += o._koudome;
        }

        private static void setAttendSemesDatMap(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT * ");
                sql.append(" FROM ATTEND_SEMES_DAT ");
                sql.append(" WHERE YEAR = '" + param._ctrlYear + "' ");
                sql.append("   AND SEMESTER <= '" + param._semester + "' ");
                sql.append("   AND SCHREGNO = ? ");
                sql.append(" ORDER BY SEMESTER,  INT(MONTH) + CASE WHEN MONTH < '04' THEN 12 ELSE 0 END ");

                ps = db2.prepareStatement(sql.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    student._attendSemesMap = new HashMap<String, AttendSemesDat>();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final String month = rs.getString("MONTH");
                        final String year = String.valueOf(rs.getInt("YEAR") + (Integer.parseInt(month) < 4 ? 1 : 0));
                        final String semesDate = year + "-" + month + "-" + param._df02.format(Integer.parseInt(rs.getString("APPOINTED_DAY")));
                        if (semesDate.compareTo(param._date) > 0) {
                            break;
                        }

                        final int lesson0 = rs.getInt("LESSON");
                        final int abroad = rs.getInt("ABROAD");
                        final int offdays = rs.getInt("OFFDAYS");
                        final int lesson = lesson0 - abroad - offdays + ("1".equals(param._knjSchoolMst._semOffDays) ? offdays : 0);
                        final int suspend = rs.getInt("SUSPEND");
                        final int mourning = rs.getInt("MOURNING");
                        final int sick = rs.getInt("SICK") + rs.getInt("NOTICE") + rs.getInt("NONOTICE");
                        final int virus = "true".equals(param._useVirus) ? rs.getInt("VIRUS") : 0;
                        final int koudome = "true".equals(param._useKoudome) ? rs.getInt("KOUDOME") : 0;
                        final int mlesson = lesson - suspend - virus - koudome - mourning;
                        final int present = mlesson - sick;

                        final AttendSemesDat attendSemesDat = new AttendSemesDat(month);
                        attendSemesDat._lesson = lesson;
                        attendSemesDat._abroad = abroad;
                        attendSemesDat._offdays = offdays;
                        attendSemesDat._suspend = suspend;
                        attendSemesDat._mourning = mourning;
                        attendSemesDat._sick = sick;
                        attendSemesDat._absent = rs.getInt("ABSENT");
                        attendSemesDat._present = present;
                        attendSemesDat._late = rs.getInt("LATE");
                        attendSemesDat._early = rs.getInt("EARLY");
                        attendSemesDat._virus = virus;
                        attendSemesDat._koudome = koudome;

                        if (null != student._attendSemesMap.get(month)) {
                            final AttendSemesDat before = (AttendSemesDat) student._attendSemesMap.get(month);
                            before.add(attendSemesDat);
                        } else {
                            student._attendSemesMap.put(month, attendSemesDat);
                        }
                    }
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
     * 学校生活のようす
     */
    private static class BehaviorSemesDat {

        final String _semester;
        final String _code;
        final String _record;
        final String _viewname;

        public BehaviorSemesDat(
                final String semester,
                final String code,
                final String record,
                final String viewname) {
            _semester = semester;
            _code = code;
            _record = record;
            _viewname = viewname;
        }

        public static void setBehaviourSemesDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getBehaviorSemesDatSql(param);
                log.debug(" behavior sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    student._behaviorSemesDatList = new ArrayList();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String code = rs.getString("CODE");
                        final String record = rs.getString("RECORD");
                        final String viewname = rs.getString("VIEWNAME");
                        final BehaviorSemesDat behaviorSemesDat = new BehaviorSemesDat(semester, code, record, viewname);

                        student._behaviorSemesDatList.add(behaviorSemesDat);
                    }

                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getBehaviorSemesDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CODE ");
            stb.append("    ,T1.VIEWNAME ");
            stb.append("    ,L1.SEMESTER ");
            stb.append("    ,L1.RECORD ");
            stb.append(" FROM ");
            stb.append("     BEHAVIOR_SEMES_MST T1 ");
            stb.append("     LEFT JOIN BEHAVIOR_SEMES_DAT L1 ON L1.YEAR = T1.YEAR ");
            stb.append("         AND L1.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND L1.SCHREGNO = ? ");
            stb.append("         AND L1.CODE = T1.CODE ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.GRADE = '" + param._gradeCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CODE ");
            return stb.toString();
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
                    student._hReportRemarkDatList = new ArrayList();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String totalstudytime = rs.getString("TOTALSTUDYTIME");
                        final String specialactremark = rs.getString("SPECIALACTREMARK");
                        final String communication = rs.getString("COMMUNICATION");
                        final String remark1 = rs.getString("REMARK1");
                        final String remark2 = rs.getString("REMARK2");
                        final String remark3 = rs.getString("REMARK3");
                        final String attendrecRemark = rs.getString("ATTENDREC_REMARK");

                        final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, totalstudytime, specialactremark, communication,
                                remark1, remark2, remark3, attendrecRemark);
                        student._hReportRemarkDatList.add(hReportRemarkDat);

                    }
                    DbUtils.closeQuietly(rs);
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
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            return stb.toString();
        }

        public static void setJviewstatReportremarkDat(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T1.REMARK1 ");
                stb.append(" FROM JVIEWSTAT_REPORTREMARK_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + param._ctrlYear + "' ");
                stb.append("   AND T1.SEMESTER = '" + param._semester + "' ");
                stb.append("   AND T1.SCHREGNO = ? ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    student._jviewstatReportremarkRemark1Map = new HashMap();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        student._jviewstatReportremarkRemark1Map.put(rs.getString("SUBCLASSCD"), rs.getString("REMARK1"));
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    private static class ChairSubclass {

        public static void setSubclass(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                if ("1".equals(param._useCurriculumcd)) {
                    sql.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
                }
                sql.append("     T2.SUBCLASSCD AS SUBCLASSCD ");
                sql.append("     , T2.CLASSCD ");
                sql.append("     , T1.APPENDDATE ");
                sql.append("     , T3.CHARGEDIV ");
                sql.append("     , T3.STAFFCD ");
                sql.append("     , T4.STAFFNAME ");
                sql.append("     , D008.NAMECD2 AS " + param._d008Namecd1 + " ");
                sql.append(" FROM ");
                sql.append("   CHAIR_STD_DAT T1 ");
                sql.append("   INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
                sql.append("     AND T2.SEMESTER = T1.SEMESTER ");
                sql.append("     AND T2.CHAIRCD = T1.CHAIRCD ");
                sql.append("   INNER JOIN CHAIR_STF_DAT T3 ON T3.YEAR = T1.YEAR ");
                sql.append("     AND T3.SEMESTER = T1.SEMESTER ");
                sql.append("     AND T3.CHAIRCD = T1.CHAIRCD ");
                sql.append("   INNER JOIN STAFF_MST T4 ON T4.STAFFCD = T3.STAFFCD ");
                sql.append("   LEFT JOIN NAME_MST D008 ON D008.NAMECD1 = '" + param._d008Namecd1 + "' AND D008.NAMECD2 = T2.CLASSCD ");
                sql.append("   LEFT JOIN SUBCLASS_MST SM ON SM.SUBCLASSCD = T2.SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    sql.append(" AND SM.CLASSCD = T2.CLASSCD AND SM.SCHOOL_KIND = T2.SCHOOL_KIND AND SM.CURRICULUM_CD = T2.CURRICULUM_CD ");
                }
                sql.append(" WHERE ");
                sql.append("     T1.YEAR = '" + param._ctrlYear + "' ");
                sql.append("     AND T1.SEMESTER = '" + param._semester + "' ");
                sql.append("     AND T1.SCHREGNO = ? ");
                sql.append(" ORDER BY ");
                if ("1".equals(param._useCurriculumcd)) {
                    sql.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
                }
                sql.append("     T2.SUBCLASSCD ");
                sql.append("     ,  T3.CHARGEDIV DESC ");
                sql.append("     ,  T3.STAFFCD ");

                log.debug(" subclass staffname sql = " + sql);

                ps = db2.prepareStatement(sql.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    student._kokoroSubclasscdList = new ArrayList();
                    student._subclassStaffNameMap = new TreeMap();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        if (null == subclasscd) {
                            continue;
                        }
                        if (null != rs.getString(param._d008Namecd1)) {
                            if (!student._kokoroSubclasscdList.contains(subclasscd)) {
                                student._kokoroSubclasscdList.add(subclasscd);
                            }
                        }
                        if (Integer.parseInt(rs.getString("CLASSCD")) <= 90 || null != rs.getString(param._d008Namecd1)) {
                            if (null != rs.getString("STAFFNAME")) {
                                final List chairStaffNameList = getMappedList(getMappedTreeMap(student._subclassStaffNameMap, subclasscd), rs.getString("APPENDDATE"));
                                if (!chairStaffNameList.contains(rs.getString("STAFFNAME"))) {
                                    chairStaffNameList.add(rs.getString("STAFFNAME"));
                                }
                            }
                        }
                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                if ("1".equals(param._useCurriculumcd)) {
                    sql.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                sql.append("     T1.SUBCLASSCD AS SUBCLASSCD ");
                sql.append("     , T1.TOTALSTUDYACT ");
                sql.append(" FROM ");
                sql.append("   RECORD_TOTALSTUDYTIME_DAT T1 ");
                sql.append(" WHERE ");
                sql.append("     T1.YEAR = '" + param._ctrlYear + "' ");
                sql.append("     AND T1.SEMESTER = '" + param._semester + "' ");
                sql.append("     AND T1.SCHREGNO = ? ");
                sql.append(" ORDER BY ");
                if ("1".equals(param._useCurriculumcd)) {
                    sql.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                sql.append("     T1.SUBCLASSCD ");

                log.info(" totalstudy sql = " + sql);

                ps = db2.prepareStatement(sql.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._kokoroShokenMap = new HashMap();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        if (student._kokoroSubclasscdList.contains(subclasscd) && null != rs.getString("TOTALSTUDYACT")) {
                            student._kokoroShokenMap.put(rs.getString("SUBCLASSCD"), rs.getString("TOTALSTUDYACT"));
                        }
                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    private static class SubclassGrade {
        final String _subclasscd;
        final String _textHyokaFlg;
        final String _subclassname;

        SubclassGrade(
            final String subclasscd,
            final String textHyokaFlg,
            final String subclassname
        ) {
            _subclasscd = subclasscd;
            _textHyokaFlg = textHyokaFlg;
            _subclassname = subclassname;
        }

        private static Map getSubclassGradeMap(final DB2UDB db2, final Param param) {
            final Map map = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T3.SUBCLASSNAME, ");
                stb.append("     T1.TEXT_HYOKA_FLG ");
                stb.append(" FROM SUBCLASS_GRADE_DAT T1 ");
                stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("     AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + param._ctrlYear + "' ");
                stb.append("   AND T1.GRADE = '" + param._grade + "' ");
                stb.append("   AND T1.RECORD_DIV = '2' ");
                stb.append("   AND T1.TEXT_HYOKA_FLG = '1' ");
                final String sql = stb.toString();
                log.debug(" subclass grade sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String textHyokaFlg = rs.getString("TEXT_HYOKA_FLG");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final SubclassGrade subclassgrade = new SubclassGrade(subclasscd, textHyokaFlg, subclassname);
                    map.put(subclasscd, subclassgrade);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75584 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _ctrlYear;
        final String _semester;
        final String _date;
        final String _gradeHrclass;
        final String _grade;
        final String _recordDiv;
        final String[] _categorySelected;
        final String _printRegd;

        final String _gradeCd;
        final String _gradeCdStr;
        final String _schoolKind;
        final String _certifSchoolDatSchoolName;
        final String _certifSchoolDatRemark1;
        final String _certifSchoolDatRemark2;
        final String _certifSchoolDatPrincipalName;
        final String _certifSchoolDatJobName;

        final String _hyosiHrName;
        final String _semestername;
        final Map _subclassNameMap;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;

        final String _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_P;

       /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;

        List _viewClassList;
        Map _subclassGradeMap;
        Map<String, SvfField> _fieldMap;

        final String _d008Namecd1;

        final DecimalFormat _df02 = new DecimalFormat("00");

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _recordDiv = getRecordDiv(db2);
            log.debug(" _recordDiv = " + _recordDiv);
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _printRegd = request.getParameter("PRINT_REGD");

            _gradeCd = getGradeCd(db2, _grade);
            _gradeCdStr = NumberUtils.isDigits(_gradeCd) ? String.valueOf(Integer.parseInt(_gradeCd)) : "";
            _certifSchoolDatSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolDatRemark1 = getCertifSchoolDat(db2, "REMARK1");
            _certifSchoolDatRemark2 = getCertifSchoolDat(db2, "REMARK2");
            _certifSchoolDatPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _certifSchoolDatJobName = getCertifSchoolDat(db2, "JOB_NAME");
            _hyosiHrName = hankakuToZenkaku(_gradeCdStr) + "年 " + getHrClassName1(db2, _ctrlYear, _semester, _gradeHrclass) + "組";
            _semestername = getSemestername(db2);
            _subclassNameMap = getSubclassNameMap(db2);

            _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P"), "+", " "); // 特別活動の記録
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"), "+", " "); // 担任からの所見

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' "));
            final String tmpD008Cd = "D" + _schoolKind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = '" + tmpD008Cd + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd1 = d008Namecd2Cnt > 0 ? tmpD008Cd : "D008";
        }

        private String getRegdHdat(final DB2UDB db2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT " + field + " FROM SCHREG_REGD_FI_HDAT WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _semester + "' AND GRADE || HR_CLASS = '" + _gradeHrclass + "' ";
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
            String rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT HR_CLASS_NAME1 FROM SCHREG_REGD_FI_HDAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' "));
            if (null == rtn) {
                final String hrClass = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT HR_CLASS FROM SCHREG_REGD_FI_DAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' "));
                if (null == rtn && null != hrClass) {
                    rtn = NumberUtils.isDigits(hrClass) ? String.valueOf(Integer.parseInt(hrClass)) : hrClass;
                }
            }
            if (null == rtn) {
                rtn = "";
            }
            return rtn;
        }

        private String getSemestername(final DB2UDB db2) {
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' AND SEMESTER = '" + _semester + "' ")));
        }

//        public String getImagePath() {
//            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + "SCHOOLLOGO." + _extension;
//            if (new java.io.File(path).exists()) {
//                return path;
//            }
//            return null;
//        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '117' ")));
        }

        private String hankakuToZenkaku(final String str) {
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

        private String getGradeCd(final DB2UDB db2, final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GRADE_CD ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        }


        private String getRecordDiv(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     RECORD_DIV ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_FI_HDAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _semester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        }


        private Map getSubclassNameMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_MST T1 ");
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "SUBCLASSCD", "SUBCLASSNAME");
        }
    }
}

// eof


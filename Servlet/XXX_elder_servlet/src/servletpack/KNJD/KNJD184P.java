/*
 * $Id: 30c886977e94df0559815a61b990b1648148c95b $
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
import java.util.ArrayList;
import java.util.Collections;
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
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 学校教育システム 賢者 [成績管理] 小学通知票
 */

public class KNJD184P {

    private static final Log log = LogFactory.getLog(KNJD184P.class);

    private static final String SEMEALL = "9";

    private static final String BACKSLASH = "＼";

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List viewClassList = ViewClass.getViewClassList(db2, _param);
        final List behaviorSemesMstList = BehaviorSemesMst.getBehaviourSemesMstList(db2, _param);
        _param._attributeMap = getVrAttributeMap(behaviorSemesMstList);

        final List studentList = getStudentList(db2);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug(" student = " + student._schregno + " : " + student._name);
            student.load(db2, _param);

            // 表紙
            printSvfHyoshi(db2, svf, student);

            // 学習のようす等
            printSvfMainSeiseki(db2, svf, student, viewClassList, behaviorSemesMstList);
            _hasData = true;
        }
    }

    private List getStudentList(final DB2UDB db2) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql(_param);
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
        return studentList;
    }

    private String getStudentSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append("  SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SEMESTER ");
        stb.append("  FROM    SCHREG_REGD_DAT T1 ");
        stb.append("          , V_SEMESTER_GRADE_MST T2 ");
        stb.append("  WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '"+ param._semester +"' ");
        stb.append("     AND T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("     AND T1.GRADE = T2.GRADE ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
        stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
        //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
        stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                     WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                         AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
        stb.append("                           OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
        //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
        stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("                     WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                         AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append(" ) ");
        //メイン表
        stb.append(" SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T7.HR_NAME, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    T5.NAME, ");
        stb.append("    T5.REAL_NAME, ");
        stb.append("    CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME ");
        stb.append(" FROM ");
        stb.append("    SCHNO_A T1 ");
        stb.append("    INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
        stb.append(" ORDER BY ATTENDNO");
        return stb.toString();
    }

    private void VrsOutRenban(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOut(field + (i + 1), (String) list.get(i));
            }
        }
    }

    private static int getMS932ByteCount(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return ret;
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

    private static class CharMS932 {
        final String _char;
        final int _len;
        public CharMS932(final String v, final byte[] b) {
            _char = v;
            _len = b.length;
        }
        public String toString() {
            return "[" + _char + " : " + _len + "]";
        }
    }
    private static List toCharMs932List(final String src) throws Exception {
        final List rtn = new ArrayList();
        for (int j = 0; j < src.length(); j++) {
            final String z = src.substring(j, j + 1);             //1文字を取り出す
            final CharMS932 c = new CharMS932(z, z.getBytes("MS932"));
            rtn.add(c);
        }
        return rtn;
    }

    protected static List retDividString(String targetsrc, final int dividlen, final int dividnum) {
        if (targetsrc == null) {
            return null;
        }
        final List lines = new ArrayList(dividnum);         //編集後文字列を格納する配列
        int len = 0;
        StringBuffer stb = new StringBuffer();

        try {
            if (!StringUtils.replace(targetsrc, "\r\n", "\n").equals(targetsrc)) {
//                log.fatal("改行コードが\\r\\n!:" + targetsrc);
                targetsrc = StringUtils.replace(targetsrc, "\r\n", "\n");
            }

            final List charMs932List = toCharMs932List(targetsrc);

            for (final Iterator it = charMs932List.iterator(); it.hasNext();) {
                final CharMS932 c = (CharMS932) it.next();
                //log.debug(" c = " + c);

                if (("\n".equals(c._char) || "\r".equals(c._char))) {
                    if (len <= dividlen) {
                        lines.add(stb.toString());
                        len = 0;
                        stb.delete(0, stb.length());
                    }
                } else {
                    if (len + c._len > dividlen) {
                        lines.add(stb.toString());
                        len = 0;
                        stb.delete(0, stb.length());
                    }
                    stb.append(c._char);
                    len += c._len;
                }
            }
            if (0 < len) {
                lines.add(stb.toString());
            }
        } catch (Exception ex) {
            log.error("retDividString error! ", ex);
        }
        if (lines.size() > dividnum) {
            return lines.subList(0, dividnum);
        }
        return lines;
    }

    /**
     * 表紙を印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printSvfHyoshi(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        final String form1;
        if (_param._semester.equals(_param._knjSchoolMst._semesterDiv)) {
            form1 = "KNJD184P_1_2.frm";
        } else {
            form1 = "KNJD184P_1.frm";
        }
        svf.VrSetForm(form1, 1);

        final String attendno = null == student._attendno ? "" : ("　" + ((NumberUtils.isDigits(student._attendno)) ? (String.valueOf(Integer.parseInt(student._attendno)) + "番") : student._attendno));
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度");
        svf.VrsOut("SCHOOLNAME", _param._certifSchoolSchoolName);

        svf.VrsOut("LOGO", _param.getImagePath());
        svf.VrsOut("HR_NAME", _param._hyosiHrName + attendno);
        if (getMS932ByteCount(student._name) > 20) {
            svf.VrsOut("NAME2", student._name);
        } else {
            svf.VrsOut("NAME", student._name);
        }
        svf.VrsOut("STAFFNAME1_" + (getMS932ByteCount(trimLeft(_param._certifSchoolPrincipalName)) > 20 ? "2" : "1"), trimLeft(_param._certifSchoolPrincipalName));
        svf.VrsOut("STAFFNAME2_" + (getMS932ByteCount(_param._tr1Name) > 20 ? "2" : "1"), _param._tr1Name);

        // 修了証
        svf.VrsOut("BLANK", _param._semester);
        if (getMS932ByteCount(student._name) > 20) {
            svf.VrsOut("NAME4", student._name);
        } else {
            svf.VrsOut("NAME3", student._name);
        }
        if (NumberUtils.isNumber(_param._gradeCdStr)) {
            svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(_param._gradeCdStr)));
        }
        if (_param._semester.equals(_param._knjSchoolMst._semesterDiv)) {
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._descDate));
        }
        svf.VrsOut("SCHOOLNAME1", _param._certifSchoolRemark3);
        svf.VrsOut("JOB", _param._certifSchoolJobName);
        svf.VrsOut("STAFFNAME1_3", _param._certifSchoolPrincipalName);
        svf.VrEndPage();
    }

    /**
     * 学習等を印刷する
     * @param db2
     * @param svf
     * @param student
     * @param viewClassList
     * @param behaviorSemesMstList
     */
    private void printSvfMainSeiseki(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Student student,
            final List viewClassList,
            final List behaviorSemesMstList) {

        svf.VrSetForm("KNJD184P_2.frm", 4);

        printSvfStudent(svf, student);

        printSvfBehavior(svf, student, behaviorSemesMstList);

        printSvfSpecialAct(svf, student);

        printSvfMeishou(svf, viewClassList, student);

        printSvfAttendSemes(svf, student);

        printSvfReportCommunication(svf, student);

        printSvfViewRecord(svf, viewClassList, student);

        _hasData = true;
    }

    private void printSvfStudent(final Vrw32alp svf, final Student student) {
        final String attendno = null == student._attendno ? "" : ("　" + ((NumberUtils.isDigits(student._attendno)) ? (String.valueOf(Integer.parseInt(student._attendno)) + "番") : student._attendno));
        svf.VrsOut("HR_NAME", _param._hyosiHrName + attendno);
        if (getMS932ByteCount(student._name) > 20) {
            svf.VrsOut("NAME2", student._name);
        } else {
            svf.VrsOut("NAME", student._name);
        }
    }

    private void VrAttribute(final Vrw32alp svf) {

        for (final Iterator it = _param._attributeMap.keySet().iterator(); it.hasNext();) {
            final String field = (String) it.next();

            final String attribute = (String) _param._attributeMap.get(field);

            svf.VrAttribute(field, attribute);
        }
    }

    private Map getVrAttributeMap(final List behaviorSemesMstList) {
        final Map map = new HashMap();

        for (final Iterator itb = behaviorSemesMstList.iterator(); itb.hasNext();) {
            final BehaviorSemesMst bsm = (BehaviorSemesMst) itb.next();

            final int icode = Integer.valueOf(bsm._code).intValue();

            map.put("LIFE_VIEW"+ icode, getLifeViewAttribute(icode, bsm._viewname));
        }
        return map;
    }

    /**
     * 『生活』
     * @param svf
     * @param student
     * @param behaviorSemesMstList
     */
    private void printSvfBehavior(final Vrw32alp svf, final Student student, final List behaviorSemesMstList) {

        for (final Iterator itb = behaviorSemesMstList.iterator(); itb.hasNext();) {
            final BehaviorSemesMst bsm = (BehaviorSemesMst) itb.next();

            final int icode = Integer.valueOf(bsm._code).intValue();

            svf.VrsOut("LIFE_ITEM" + icode, bsm._codename);

            svf.VrsOut("LIFE_VIEW" + icode, bsm._viewname);

            if ("2020".equals(_param._year)) {
                svf.VrsOut("LIFE_VALUE_SLASH" + icode + "_1", "＼");
            }

            for (final Iterator it = student._behaviorSemesDatList.iterator(); it.hasNext();) {
                final BehaviorSemesDat bsd = (BehaviorSemesDat) it.next();

                if (bsm._code.equals(bsd._code)) {
                    svf.VrsOut("LIFE_VALUE" + icode + "_" + bsd._semester, bsd._mark);
                }
            }
        }
    }

    private String getLifeViewAttribute(final int line, final String data) {
        final int width = 1787, height = 79, ystart = 911, minnum = 58, maxnum = 100;
        final KNJSvfFieldModify modify = new KNJSvfFieldModify(width, height, ystart, minnum, maxnum);
        final float charSize = modify.getCharSize(data);
        final StringBuffer attr = new StringBuffer();
        attr.append("Size=" + charSize);  //文字サイズ
        attr.append(", Y="+ (int) modify.getYjiku(line - 1, charSize));  //開始Ｙ軸
        return attr.toString();
    }



    private static String notZero(int n) {
        return String.valueOf(n);
    }

    /**
     * 『出欠の記録』を印字する
     * @param svf
     * @param student
     */
    private void printSvfAttendSemes(final Vrw32alp svf, final Student student) {

        String closeDay = "";
        //closeDay = KNJ_EditDate.h_format_JP_MD(_param._date) + "現在";
        if ("1".equals(_param._semester)) {
            closeDay = "４月〜６月";
        } else if ("2".equals(_param._semester)) {
            closeDay = "７月〜１１月";
        } else if ("3".equals(_param._semester)) {
            closeDay = "１２月〜２月";
        }
        svf.VrsOut("CLOSE_DAY", closeDay);
        for (final Iterator it = student._attendSemesDatList.iterator(); it.hasNext();) {
            final AttendSemesDat attendSemesDat = (AttendSemesDat) it.next();
            if (!NumberUtils.isDigits(attendSemesDat._semester)) {
                continue;
            }

            final int j;
            if (SEMEALL.equals(attendSemesDat._semester)) {
                continue;
            } else {
                j = Integer.parseInt(attendSemesDat._semester);
            }
            svf.VrsOutn("PERIOD_LESSON", j, notZero(attendSemesDat._lesson));
            svf.VrsOutn("PERIOD_SUSPEND", j, notZero(attendSemesDat._suspend + attendSemesDat._mourning));
            svf.VrsOutn("PERIOD_PRESENT", j, notZero(attendSemesDat._mlesson));
            svf.VrsOutn("PERIOD_ABSENCE", j, notZero(attendSemesDat._sick));
            svf.VrsOutn("PERIOD_ATTEND", j, notZero(attendSemesDat._present));
        }
    }

    /**
     * 学習の記録を印字する
     * @param svf
     * @param student
     */
    private void printSvfViewRecord(final Vrw32alp svf, final List viewClassList, final Student student) {
        final int maxLine = 28;
        int line = 0;
        for (final Iterator it = viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();
            if (Integer.parseInt(viewClass._classcd) >= 90) {
                continue;
            }
            final List classnameCharList = viewClass.getSubclassnameCharacterList();

            int vi = 0;
            int l = 0;
            while (l < classnameCharList.size()) {
                final ViewClass.View view;
                final String viewName;
                final List viewRecordList;

                if (viewClass._viewList.size() - 1 < vi) {
                    view = null;
                    viewName = " ";
                    viewRecordList = Collections.EMPTY_LIST;
                } else {
                    view = viewClass.getView(vi);
                    viewName = view._viewname;
                    viewRecordList = student.getViewList(view._viewcd);
                }

                final List viewNameLineList = retDividString(viewName, ViewClass.viewnamesize, 1);
                final List classnameCharp = classnameCharList.subList(l, l + viewNameLineList.size());
//                log.debug(" dline = " + viewNameLineList + ", " + classnameCharp);
                for (int j = 0; j < viewNameLineList.size(); j++) {

                    final boolean isLastViewLine = vi >= viewClass._viewList.size() - 1 && j == viewNameLineList.size() - 1 && l >= classnameCharList.size() - 1;
                    final String fCLASS_VIEW1;
                    final String fCLASS_NAME1;
                    final String fCLASSVALUE;
                    final String fCLASSVALUESLASH;
                    final String fCLASSGRP;
                    if (isLastViewLine) {
                        fCLASS_VIEW1 = "CLASS_VIEW1_2";
                        fCLASS_NAME1 = "CLASS_NAME1_2";
                        fCLASSGRP = "CLASSGRP1_2";
                        fCLASSVALUE = "CLASS_VALUE2_";
                        fCLASSVALUESLASH = "CLASS_VALUE_SLASH2_";
                    } else {
                        fCLASS_VIEW1 = "CLASS_VIEW1";
                        fCLASS_NAME1 = "CLASS_NAME1";
                        fCLASSGRP = "CLASSGRP1";
                        fCLASSVALUE = "CLASS_VALUE1_";
                        fCLASSVALUESLASH = "CLASS_VALUE_SLASH1_";
                    }
                    svf.VrsOut(fCLASS_VIEW1, (String) viewNameLineList.get(j)); // 観点名称
                    svf.VrsOut(fCLASS_NAME1, (String) classnameCharp.get(j));
                    svf.VrsOut(fCLASSGRP, viewClass.getUniqueClassCd(_param));
                    if (null != view) {
                        for (final Iterator itflg = _param._semesterList.iterator(); itflg.hasNext();) {
                            final String semester = (String) itflg.next();
                            if (!"1".equals(view._semesterViewflgMap.get(semester))) {
                                for (int i = 1; i <= 3; i++) {
                                    svf.VrsOut(fCLASSVALUESLASH + semester + "_" + i, BACKSLASH); // 観点に"＼"表示
                                }
                            }
                        }
                    }
                    for (final Iterator itv = viewRecordList.iterator(); itv.hasNext();) {
                        final ViewRecord vr = (ViewRecord) itv.next();
                        final String s;
                        if ("A".equals(vr._status)) {
                            s = "1";
                        } else if ("B".equals(vr._status)) {
                            s = "2";
                        } else if ("C".equals(vr._status)) {
                            s = "3";
                        } else {
                            s = null;
                        }
                        svf.VrsOut(fCLASSVALUE + vr._semester + "_" + s, "○"); // 観点
                    }
                    svf.VrEndRecord();
                    VrAttribute(svf);
                }
                l += viewNameLineList.size();
                vi += 1;
            }
            line += l;
        }

        // 空行挿入
        for (int i = line % maxLine == 0 ? maxLine : line % maxLine; i < maxLine; i++) {
            svf.VrsOut("CLASSGRP1", String.valueOf(i));
            svf.VrEndRecord();
        }
    }

    /**
     * 『特別活動』
     * @param svf
     * @param student
     */
    private void printSvfSpecialAct(final Vrw32alp svf, final Student student) {

        final int gradeCdInt = Integer.parseInt(_param._gradeCdStr);
        if (5 == gradeCdInt || 6 == gradeCdInt) {
            // 委員会
            svf.VrsOut("COMMITEE1", "(　　　　　　　　　　　　　　　　委員会)");
        }

        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();
            final String semester = hReportRemarkDat._semester;
            if (!SEMEALL.equals(semester)) {
                continue;
            }

            if (5 == gradeCdInt || 6 == gradeCdInt) {
                // 委員会
                svf.VrsOut("COMMITTEE", hReportRemarkDat._remark1);
            }

            // 所見
            VrsOutRenban(svf, "SPECIAL", retDividString(hReportRemarkDat._remark3, 84, 4));
        }

        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();
            final String semester = hReportRemarkDat._semester;
            if (SEMEALL.equals(semester)) {
                continue;
            }

            // 係り
            svf.VrsOut("CHARGE" + semester, hReportRemarkDat._remark2);
        }
    }

    /**
     * 『明小タイム』
     * @param svf
     * @param student
     */
    private void printSvfMeishou(final Vrw32alp svf, final List viewClassList, final Student student) {
        // 観点
        int j = 0;
        if ("1".equals(_param._gradeCdStr) || "2".equals(_param._gradeCdStr)) {
            svf.VrsOut("MEISHO_SUBTITLE", "（生活科）");
        } else {
            svf.VrsOut("MEISHO_SUBTITLE", "（総合的な学習の時間）");
        }
        for (final Iterator it = viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();
            final String classcd = viewClass._classcd;
            if (Integer.parseInt(classcd) < 90) {
                continue;
            }
            for (int i = 0; i < viewClass._viewList.size(); i++) {
                final String viewName = viewClass.getView(i)._viewname;
                svf.VrsOut("MEISHO" + (j + i + 1), StringUtils.isBlank(viewName) ? "" : ("・" + viewName));
            }
            j += viewClass._viewList.size();
        }

        // 文言評価
        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();
            final String semester = hReportRemarkDat._semester;
            if (!SEMEALL.equals(semester)) {
                continue;
            }

//            final int pcharstotal = getParamSizeNum(_param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P, 0);
//            final int plinestotal = getParamSizeNum(_param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P, 1);
//            final int charstotal = (-1 == pcharstotal || -1 == plinestotal) ? 11 : pcharstotal;
//            final int linestotal = (-1 == pcharstotal || -1 == plinestotal) ?  8 : plinestotal;
            final int charstotal = 18;
            final int linestotal = 8;

            VrsOutRenban(svf, "MEISHO_VALUE", retDividString(hReportRemarkDat._totalstudytime, charstotal * 2, linestotal));
        }
    }

    /**
     * 『生活や学習について気がついたこと』
     * @param svf
     * @param student
     */
    private void printSvfReportCommunication(final Vrw32alp svf, final Student student) {
//        final int pchars = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_P, 0);
//        final int plines = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_P, 1);
//        final int chars = (-1 == pchars || -1 == plines) ? 25 : pchars;
//        final int lines = (-1 == pchars || -1 == plines) ? 4  : plines;
        final int chars = 25;
        final int lines =  6;

        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();
            final String semester = hReportRemarkDat._semester;
            if (!_param._semester.equals(semester)) {
                continue;
            }
            VrsOutRenban(svf, "REMARK", retDividString(hReportRemarkDat._communication, chars * 2 , lines));
        }
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        List _viewRecordList = Collections.EMPTY_LIST;
        List _attendSemesDatList = Collections.EMPTY_LIST; // 出欠のようす
        List _behaviorSemesDatList = Collections.EMPTY_LIST; // 生活・特別活動のようす
        List _hReportRemarkDatList = Collections.EMPTY_LIST; // 所見

        public Student(final String schregno, final String name, final String hrName, final String attendno) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
        }

        public void load(final DB2UDB db2, final Param param) {
            _viewRecordList = ViewRecord.getViewRecordList(db2, param, _schregno);
            _attendSemesDatList = AttendSemesDat.getAttendSemesDatList(db2, param, _schregno);
            _behaviorSemesDatList = BehaviorSemesDat.getBehaviourSemesDatList(db2, param, _schregno);
            _hReportRemarkDatList = HReportRemarkDat.getHReportRemarkDatList(db2, param, _schregno);
        }

        /**
         * 観点コードの観点のリストを得る
         * @param viewcd 観点コード
         * @return 観点コードの観点のリスト
         */
        public List getViewList(final String viewcd) {
            final List rtn = new ArrayList();
            if (null != viewcd) {
                for (final Iterator it = _viewRecordList.iterator(); it.hasNext();) {
                    final ViewRecord viewRecord = (ViewRecord) it.next();
                    if (viewcd.equals(viewRecord._viewcd)) {
                        rtn.add(viewRecord);
                    }
                }
            }
            return rtn;
        }
    }

    /**
     * 観点の教科
     */
    private static class ViewClass {

        static class View {
            final String _viewcd;
            final String _viewname;
            final int _viewnameLines;
            final Map _semesterViewflgMap;
            View(final String viewcd, final String viewname, final int viewnameLines) {
                _viewcd = viewcd;
                _viewname = viewname;
                _viewnameLines = viewnameLines;
                _semesterViewflgMap = new HashMap();
            }
            public String toString() {
                return "View(" + _viewcd + ":" + _viewname + ")";
            }
        }

        static final int viewnamesize = 64;

        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final List _viewList;
        ViewClass(
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _viewList = new ArrayList();
        }

        public String getUniqueClassCd(final Param param) {
            if ("1".equals(param._useCurriculumcd)) {
                return _classcd + "-" + _schoolKind + "-" + _curriculumCd;
            }
            return _classcd;
        }

        public boolean containsView(final String viewcd) {
            final List viewcdList = new ArrayList();
            for (final Iterator it = _viewList.iterator(); it.hasNext();) {
                final View view = (View) it.next();
                viewcdList.add(view._viewcd);
            }
            return viewcdList.contains(viewcd);
        }

        public void addView(final String viewcd, final String viewname) {
//            final int ms932byte = getMS932ByteCount(viewname);
//            final int viewnameLines = ms932byte / viewnamesize + (ms932byte % viewnamesize == 0 ? 0 : 1);
            final int viewnameLines = 1;
            _viewList.add(new View(viewcd, viewname, viewnameLines));
        }

        public List getSubclassnameCharacterList() {
            if (null == _subclassname || "".equals(_subclassname)) {
                return Collections.EMPTY_LIST;
            }
            List rtn = new ArrayList();
            final int viewLineSize = getViewNameLineSize();
            if (_subclassname.length() >= viewLineSize) {
                for (int i = 0; i < _subclassname.length(); i++) {
                    rtn.add(String.valueOf(_subclassname.charAt(i)));
                }
            } else {
                final int st = (viewLineSize / 2) - (_subclassname.length() / 2 + _subclassname.length() % 2); // センタリング
                for (int i = 0; i < st; i++) {
                    rtn.add("");
                }
                for (int i = st, ci = 0; i < st + _subclassname.length(); i++, ci++) {
                    rtn.add(String.valueOf(_subclassname.charAt(ci)));
                }
                for (int i = st + _subclassname.length(); i < viewLineSize; i++) {
                    rtn.add("");
                }
            }
            return rtn;
        }

        public View getView(final String viewcd) {
            for (final Iterator it = _viewList.iterator(); it.hasNext();) {
                final View view = (View) it.next();
                if (view._viewcd.equals(viewcd)) {
                    return view;
                }
            }
            return null;
        }

        public View getView(int i) {
            return (View) _viewList.get(i);
        }

        public int getViewNameLineSize() {
            int rtn = 0;
            for (int i = 0; i < _viewList.size(); i++) {
                rtn += getView(i)._viewnameLines;
            }
            return rtn;
        }

        public static List getViewClassList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewClassSql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String schoolKind;
                    final String curriculumCd;
                    if ("1".equals(param._useCurriculumcd)) {
                        schoolKind = rs.getString("SCHOOL_KIND");
                        curriculumCd = rs.getString("CURRICULUM_CD");
                    } else {
                        schoolKind = null;
                        curriculumCd = null;
                    }

                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String viewcd = StringUtils.defaultString(rs.getString("VIEWCD"));
                    final String viewname = StringUtils.defaultString(rs.getString("VIEWNAME"));
                    final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                    final String viewflg = StringUtils.defaultString(rs.getString("VIEWFLG"));

                    if ("1".equals(param._useCurriculumcd)) {
                        if (param._d026subclasscdList.contains(classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd)) {
                            continue;
                        }
                    } else {
                        if (param._d026subclasscdList.contains(subclasscd)) {
                            continue;
                        }
                    }

                    ViewClass viewClass = null;
                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final ViewClass viewClass0 = (ViewClass) it.next();
                        if ("1".equals(param._useCurriculumcd)) {
                            if (viewClass0._classcd.equals(classcd) && viewClass0._schoolKind.equals(schoolKind) && viewClass0._curriculumCd.equals(curriculumCd)) {
                                viewClass = viewClass0;
                                break;
                            }
                        } else {
                            if (viewClass0._classcd.equals(classcd)) {
                                viewClass = viewClass0;
                                break;
                            }
                        }
                    }

                    if (null == viewClass) {
                        viewClass = new ViewClass(classcd, schoolKind, curriculumCd, subclasscd, subclassname);
                        list.add(viewClass);
                    }

                    if (!viewClass.containsView(viewcd)) {
                        viewClass.addView(viewcd, viewname);
                    }

                    final View view = viewClass.getView(viewcd);
                    view._semesterViewflgMap.put(semester, viewflg);
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
                stb.append("     T3.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     T1.VIEWNAME, ");
            stb.append("     T5.SEMESTER, ");
            stb.append("     T5.VIEWFLG ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN CLASS_MST T3 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            } else {
                stb.append("         T3.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            }
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN JVIEWSTAT_INPUTSEQ_DAT T5 ON T5.YEAR = T2.YEAR ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T5.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T5.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T5.GRADE = T1.GRADE ");
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T3.SHOWORDER3, -1), ");
            stb.append("     T3.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T3.SCHOOL_KIND, ");
            }
            stb.append("     VALUE(T4.ELECTDIV, '0'), ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
        }
    }

    /**
     * 学習の記録（観点）
     */
    private static class ViewRecord {

        final String _semester;
        final String _viewcd;
        final String _status;
        final String _d029Namecd2;
        final String _d029Namespare1;
        final String _grade;
        final String _viewname;
        final String _classcd;
        final String _classMstShoworder;
        final String _showorder;
        ViewRecord(
                final String semester,
                final String viewcd,
                final String status,
                final String d029Namecd2,
                final String d029Namespare1,
                final String grade,
                final String viewname,
                final String classcd,
                final String classMstShoworder,
                final String showorder) {
            _semester = semester;
            _viewcd = viewcd;
            _status = status;
            _d029Namecd2 = d029Namecd2;
            _d029Namespare1 = d029Namespare1;
            _grade = grade;
            _viewname = viewname;
            _classcd = classcd;
            _classMstShoworder = classMstShoworder;
            _showorder = showorder;
        }

        public static List getViewRecordList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewRecordSql(param, schregno);
                log.debug(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String semester = rs.getString("SEMESTER");
                    final String viewcd = rs.getString("VIEWCD");
                    final String status = rs.getString("STATUS");
                    final String d029Namecd2 = rs.getString("D029NAMECD2");
                    final String d029Namespare1 = rs.getString("D029NAMESPARE1");
                    final String grade = rs.getString("GRADE");
                    final String viewname = rs.getString("VIEWNAME");
                    final String classcd = rs.getString("CLASSCD");
                    final String classMstShoworder = rs.getString("CLASS_MST_SHOWORDER");
                    final String showorder = rs.getString("SHOWORDER");

                    final ViewRecord viewRecord = new ViewRecord(semester, viewcd, status, d029Namecd2, d029Namespare1, grade, viewname, classcd, classMstShoworder, showorder);

                    list.add(viewRecord);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getViewRecordSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE   ");
            stb.append("     , T1.VIEWNAME ");
            stb.append("     , T1.VIEWCD ");
            stb.append("     , T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , T3.SCHREGNO ");
            stb.append("     , T3.STATUS ");
            stb.append("     , T5.NAMECD2 AS D029NAMECD2 ");
            stb.append("     , T5.NAMESPARE1 AS D029NAMESPARE1 ");
            stb.append("     , T4.CLASSCD ");
            stb.append("     , T4.SHOWORDER AS CLASS_MST_SHOWORDER ");
            stb.append("     , T1.SHOWORDER ");
            stb.append(" FROM JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND T3.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T3.YEAR = T2.YEAR ");
            stb.append("         AND T3.SEMESTER <= '" + param._semester + "' ");
            stb.append("         AND T3.SCHREGNO = '" + schregno + "' ");
            stb.append("     LEFT JOIN CLASS_MST T4 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         T4.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            } else {
                stb.append("         T4.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            }
            stb.append("     LEFT JOIN NAME_MST T5 ON T5.NAMECD1 = 'D029' ");
            stb.append("         AND T5.ABBV1 = T3.STATUS ");
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SUBSTR(T1.VIEWCD, 1, 2) ");
            stb.append("     , VALUE(T1.SHOWORDER, 0) ");
            return stb.toString();
        }
    }

    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _semester;
        final int _lesson;
        final int _suspend;
        final int _mourning;
        final int _mlesson;
        final int _sick;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _transferDate;
        final int _offdays;

        public AttendSemesDat(
                final String semester,
                final int lesson,
                final int suspend,
                final int mourning,
                final int mlesson,
                final int sick,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int transferDate,
                final int offdays
        ) {
            _semester = semester;
            _lesson = lesson;
            _suspend = suspend;
            _mourning = mourning;
            _mlesson = mlesson;
            _sick = sick;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _transferDate = transferDate;
            _offdays = offdays;
        }

        private static List getAttendSemesDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sqlAttendSemes = AttendSemesDat.sql(param._knjSchoolMst, param, schregno);
                log.debug(" sqllAttendSemes = " + sqlAttendSemes);
                ps = db2.prepareStatement(sqlAttendSemes);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String semester = rs.getString("SEMESTER");
                    if (null == semester || Integer.parseInt(semester) > Integer.parseInt(param._semester)) {
                        continue;
                    }
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

                    final AttendSemesDat attendSemesDat = new AttendSemesDat(semester, lesson, suspend, mourning, mlesson, sick, absent, present, late, early, transferDate, offdays);
                    list.add(attendSemesDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String sql(final KNJSchoolMst knjSchoolMst, final Param param, final String schregno) {

            final StringBuffer stb = new StringBuffer();
            stb.append("    SELECT ");
            stb.append("        CASE WHEN MONTH IN ('04', '05', '06') THEN '1' ");
            stb.append("             WHEN MONTH IN ('07', '08', '09', '10', '11') THEN '2' ");
            stb.append("             WHEN MONTH IN ('12', '01', '02') THEN '3' ");
            stb.append("        ELSE NULL END ");
            stb.append("          AS SEMESTER, ");
            stb.append("        SUM( VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            if ("1".equals(knjSchoolMst._semOffDays)) {
                stb.append("           + VALUE(OFFDAYS, 0) ");
            }
            stb.append("        ) AS LESSON, ");
            stb.append("        SUM(MOURNING) AS MOURNING, ");
            stb.append("        SUM(SUSPEND) ");
            if ("true".equals(param._useVirus)) {
                stb.append("        + SUM(VIRUS) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append("        + SUM(KOUDOME) ");
            }
            stb.append("        AS SUSPEND, ");
            stb.append("        SUM(ABSENT) AS ABSENT, ");
            stb.append("        SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
            if ("1".equals(knjSchoolMst._semOffDays)) {
                stb.append("           + VALUE(OFFDAYS, 0) ");
            }
            stb.append("        ) AS SICK, ");
            stb.append("        SUM( VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            if ("1".equals(knjSchoolMst._semOffDays)) {
                stb.append("           + VALUE(OFFDAYS, 0) ");
            }
            stb.append("        ) - SUM(SUSPEND) - SUM(MOURNING) ");
            if ("true".equals(param._useVirus)) {
                stb.append("        - SUM(VIRUS) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append("        - SUM(KOUDOME) ");
            }
            stb.append("        AS MLESSON, ");
            stb.append("        SUM( VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            if ("1".equals(knjSchoolMst._semOffDays)) {
                stb.append("           + VALUE(OFFDAYS, 0) ");
            }
            stb.append("        ) - SUM(SUSPEND) - SUM(MOURNING) ");
            if ("true".equals(param._useVirus)) {
                stb.append("        - SUM(VIRUS) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append("        - SUM(KOUDOME) ");
            }
            stb.append("          - SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
            if ("1".equals(knjSchoolMst._semOffDays)) {
                stb.append("           + VALUE(OFFDAYS, 0) ");
            }
            stb.append("        ) AS PRESENT, ");
            stb.append("        SUM(LATE) AS LATE, ");
            stb.append("        SUM(EARLY) AS EARLY, ");
            stb.append("        SUM(ABROAD) AS TRANSFER_DATE, ");
            stb.append("        SUM(OFFDAYS) AS OFFDAYS ");
            stb.append("    FROM ");
            stb.append("        V_ATTEND_SEMES_DAT W1 ");
            stb.append("    WHERE ");
            stb.append("        W1.YEAR = '" + param._year + "' ");
            stb.append("        AND W1.SCHREGNO = '" + schregno + "' ");
            stb.append("    GROUP BY ");
            stb.append("        CASE WHEN MONTH IN ('04', '05', '06') THEN '1' ");
            stb.append("             WHEN MONTH IN ('07', '08', '09', '10', '11') THEN '2' ");
            stb.append("             WHEN MONTH IN ('12', '01', '02') THEN '3' ");
            stb.append("        ELSE NULL END ");
            return stb.toString();
        }

    }
    /**
     * 学校生活のようす（マスタ）
     */
    private static class BehaviorSemesMst {

        final String _code;
        final String _codename;
        final String _viewname;

        public BehaviorSemesMst(
                final String code,
                final String codename,
                final String viewname) {
            _code = code;
            _codename = codename;
            _viewname = viewname;
        }

        public static List getBehaviourSemesMstList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getBehaviorSemesMstSql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("CODE");
                    final String codename = rs.getString("CODENAME");
                    final String viewname = rs.getString("VIEWNAME");

                    final BehaviorSemesMst behaviorSemesMst = new BehaviorSemesMst(code, codename, viewname);

                    list.add(behaviorSemesMst);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getBehaviorSemesMstSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CODE, ");
            stb.append("     T1.CODENAME, ");
            stb.append("     T1.VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     BEHAVIOR_SEMES_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CODE ");
            return stb.toString();
        }
    }

    /**
     * 学校生活のようす
     */
    private static class BehaviorSemesDat {

        final String _semester;
        final String _code;
        final String _record;
        final String _mark;

        public BehaviorSemesDat(
                final String semester,
                final String code,
                final String record,
                final String mark) {
            _semester = semester;
            _code = code;
            _record = record;
            _mark = mark;
        }

        public static List getBehaviourSemesDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getBehaviorSemesDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String code = rs.getString("CODE");
                    final String record = rs.getString("RECORD");
                    final String mark = rs.getString("MARK");

                    final BehaviorSemesDat behaviorSemesDat = new BehaviorSemesDat(semester, code, record, mark);

                    list.add(behaviorSemesDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getBehaviorSemesDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_MARK AS ( ");
            stb.append("     SELECT ");
            stb.append("         NAME1 AS RECORD, ");
            stb.append("         MAX(NAMESPARE1) AS MARK ");
            stb.append("     FROM ");
            stb.append("         NAME_MST ");
            stb.append("     WHERE ");
            stb.append("         NAMECD1='D036' AND ");
            stb.append("         NAME1 IS NOT NULL ");
            stb.append("     GROUP BY ");
            stb.append("         NAME1 ");
            stb.append("     ) ");
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append("    ,L1.MARK ");
            stb.append(" FROM ");
            stb.append("     BEHAVIOR_SEMES_DAT T1 ");
            stb.append("     LEFT JOIN T_MARK L1 ON L1.RECORD = T1.RECORD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
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

        public HReportRemarkDat(
                final String semester,
                final String totalstudytime,
                final String specialactremark,
                final String communication,
                final String remark1,
                final String remark2,
                final String remark3) {
            _semester = semester;
            _totalstudytime = totalstudytime;
            _specialactremark = specialactremark;
            _communication = communication;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
        }

        public static List getHReportRemarkDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String totalstudytime = rs.getString("TOTALSTUDYTIME");
                    final String specialactremark = rs.getString("SPECIALACTREMARK");
                    final String communication = rs.getString("COMMUNICATION");
                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final String remark3 = rs.getString("REMARK3");
                    final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, totalstudytime, specialactremark, communication, remark1, remark2, remark3);
                    list.add(hReportRemarkDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getHReportRemarkDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND (T1.SEMESTER <= '" + param._semester + "' OR T1.SEMESTER = '" + SEMEALL + "') ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            return stb.toString();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class KNJSvfFieldModify {

        private static final Log log = LogFactory.getLog(KNJSvfFieldModify.class);

        private final int _width;   //フィールドの幅(ドット)
        private final int _height;  //フィールドの高さ(ドット)
        private final int _ystart;  //開始位置(ドット)
        private final int _minnum;  //最小設定文字数
        private final int _maxnum;  //最大設定文字数

        public KNJSvfFieldModify(final int width, final int height, final int ystart, final int minnum, final int maxnum) {
            _width = width;
            _height = height;
            _ystart = ystart;
            _minnum = minnum;
            _maxnum = maxnum;
        }

        /**
         * 中央割付フィールドで文字の大きさ調整による中心軸のずれ幅の値を得る
         * @param posx1 フィールドの左端X
         * @param posx2 フィールドの右端X
         * @param num フィールド指定の文字数
         * @param charSize 変更後の文字サイズ
         * @return ずれ幅の値
         */
        public int getModifiedCenteringOffset(final int posx1, final int posx2, final int num, float charSize) {
            final int maxWidth = getStringLengthPixel(charSize, num); // 文字の大きさを考慮したフィールドの最大幅
            final int offset = (maxWidth / 2) - (posx2 - posx1) / 2;
            return offset;
        }

        private int getStringLengthPixel(final float charSize, final int num) {
            return charSizeToPixel(charSize) * num / 2;
        }

        /**
         *  ポイントの設定
         *  引数について  String str : 出力する文字列
         */
        public float getCharSize(final String str)
        {
            final int num = Math.min(Math.max(retStringByteValue( str ), _minnum), _maxnum);
            return Math.min((float) pixelToCharSize(_height), retFieldPoint(_width, num));  //文字サイズ
        }

        /**
         * 文字サイズをピクセルに変換した値を得る
         * @param charSize 文字サイズ
         * @return 文字サイズをピクセルに変換した値
         */
        public static int charSizeToPixel(final double charSize)
        {
            return (int) Math.round(charSize / 72 * 400);
        }

        /**
         * ピクセルを文字サイズに変換した値を得る
         * @param charSize ピクセル
         * @return ピクセルを文字サイズに変換した値
         */
        public static double pixelToCharSize(final int pixel)
        {
            return pixel / 400.0 * 72;
        }

        /**
         *  Ｙ軸の設定
         *  引数について  int hnum   : 出力位置(行)
         */
        public float getYjiku(final int hnum, final float charSize)
        {
            float jiku = 0;
            try {
                jiku = retFieldY(_height, charSize) + _ystart + _height * hnum;  //出力位置＋Ｙ軸の移動幅
            } catch (Exception ex) {
                log.error("setRetvalue error!", ex);
                log.debug(" jiku = " + jiku);
            }
            return jiku;
        }

        /**
         *  文字数を取得
         */
        private static int retStringByteValue(final String str)
        {
            if ( str == null )return 0;
            int ret = 0;
            try {
                ret = str.getBytes("MS932").length;   //文字列をbyte配列へ
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
            return ret;
        }

        /**
         *  文字サイズを設定
         */
        private static float retFieldPoint(final int width, final int num)
        {
            return (float) Math.round((float) width / (num / 2 + (num % 2 == 0 ? 0 : 1)) * 72 / 400 * 10) / 10;
        }

        /**
         *  Ｙ軸の移動幅算出
         */
        private static float retFieldY(final int height, final float charSize)
        {
            return (float) Math.round(((double) height - (charSize / 72 * 400)) / 2);
        }

        public String toString() {
            return "KNJSvfFieldModify: width = "+ _width + " , height = " + _height + " , ystart = " + _ystart + " , minnum = " + _minnum + " , maxnum = " + _maxnum;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75411 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _gradeHrclass;
        final String _grade;
        final String[] _categorySelected;
        String _trCd1;
        final String _documentRoot;
        final String _imagePath;
        final String _extension;
        final String _descDate;

        final String _certifSchoolSchoolName;
        final String _certifSchoolRemark3;
        final String _certifSchoolPrincipalName;
        final String _tr1Name;
        final String _hyosiHrName;
        final String _certifSchoolJobName;
        final String _d016Namespare1;
        final List _d026subclasscdList;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final String _useVirus;
        final String _useKoudome;

//        final String _HREPORTREMARK_DAT_FOREIGNLANGACT_SIZE_P;
//        final String _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P;
//        final String _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P;
//        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_P;
//        final String _HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P;

       /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;

        final String _gradeCdStr;

        Map _attributeMap = Collections.EMPTY_MAP;
        final List _semesterList;
        final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _categorySelected = request.getParameterValues("category_selected");
            _trCd1 = request.getParameter("TR_CD1");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _imagePath = request.getParameter("IMAGEPATH");

            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
            _descDate = request.getParameter("DESC_DATE");

            _gradeCdStr = getGradeCd(db2, _grade);
            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolRemark3 = getCertifSchoolDat(db2, "REMARK3");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _tr1Name = getStaffname(db2, _trCd1);
            _hyosiHrName = "第" + hankakuToZenkaku(_gradeCdStr) + "学年 " + getHrClassName1(db2, _year, _semester, _gradeHrclass) + "組";
            _certifSchoolJobName = getCertifSchoolDat(db2, "JOB_NAME");
            _d016Namespare1 = getNameMst(db2, _year, "D016", "01", "NAMESPARE1");

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _d026subclasscdList = getNameMstList(db2, _year);
            log.fatal(" d026 subclasscd = " + _d026subclasscdList);

//            _HREPORTREMARK_DAT_FOREIGNLANGACT_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_FOREIGNLANGACT_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_FOREIGNLANGACT_SIZE_P"), "+", " ");
//            _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P"), "+", " ");
//            _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P"), "+", " ");
//            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"), "+", " ");
//            _HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P"), "+", " ");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            // 学期名称 _arrsemesName をセットします。
            _semesterList = getSemesterList(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
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
                rtn = "";
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

        private List getNameMstList(final DB2UDB db2, final String year) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                    sql.append(" SELECT ");
                    sql.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS NAME1 ");
                    sql.append(" FROM SUBCLASS_DETAIL_DAT T1 ");
                    sql.append(" WHERE ");
                    sql.append("    T1.YEAR = '" + _year + "' ");
                    sql.append("    AND T1.SUBCLASS_SEQ = '007' ");
                    if ("9".equals(_semester)) {
                        sql.append("    AND T1.SUBCLASS_REMARK4 = '1' ");
                    } else if (Integer.parseInt(_semester) <= 3) {
                        sql.append("    AND T1.SUBCLASS_REMARK" + (Integer.parseInt(_semester))  + " = '1' ");
                    }
                } else {
                    sql.append(" SELECT ");
                    sql.append("    NAME1 ");
                    sql.append(" FROM NAME_MST T1 ");
                    sql.append("    INNER JOIN NAME_YDAT T2 ON T2.YEAR = '" + year + "' AND T2.NAMECD1 = T1.NAMECD1 AND T2.NAMECD2 = T1.NAMECD2 ");
                    sql.append(" WHERE ");
                    sql.append("    T1.NAMECD1 = 'D026' ");
                    if ("9".equals(_semester)) {
                        sql.append("    AND NAMESPARE1 = '1' ");
                    } else if (Integer.parseInt(_semester) <= 3) {
                        sql.append("    AND ABBV" + _semester + " = '1' ");
                    }
                }
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("NAME1")) {
                        rtn.add(rs.getString("NAME1"));
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

        public String getImagePath() {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + "SCHOOLLOGO." + _extension;
            if (new java.io.File(path).exists()) {
                return path;
            }
            return null;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '117' ";
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
         * 学期マスタ (SEMESTER_MST) をロードする
         * @param db2
         */
        private List getSemesterList(DB2UDB db2) {
            final String sql = "SELECT SEMESTER FROM SEMESTER_MST "
                + " WHERE YEAR = '" + _year + "' AND SEMESTER <> '" + SEMEALL + "' ORDER BY SEMESTER";
            //log.debug(" semester sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(rs.getString("SEMESTER"));
                }

            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
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
            String gradeCd = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.SCHOOL_KIND = 'P' ");
                stb.append("     AND T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String tmp = rs.getString("GRADE_CD");
                    if (NumberUtils.isDigits(tmp)) {
                        gradeCd = String.valueOf(Integer.parseInt(tmp));
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
    }
}

// eof


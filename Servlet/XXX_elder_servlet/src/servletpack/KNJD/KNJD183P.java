/*
 * $Id: c3ae1a6d03ad35ba9558cff097aac7b47242ee35 $
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

/**
 * 学校教育システム 賢者 [成績管理] 小学通知票
 */

public class KNJD183P {

    private static final Log log = LogFactory.getLog(KNJD183P.class);

    private static final String SEMEALL = "9";

    private static final String SLASH = "／";

    private boolean _hasData;

    private final DecimalFormat df2 = new DecimalFormat("00");

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

            final Param param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf, param);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {

        param._viewClassList = ViewClass.getViewClassList(db2, param);
        param._behaviorSemesMstList = BehaviorSemesLMst.getBehaviorSemesLMstList(db2, param, param._grade);
        if (param._behaviorSemesMstList.isEmpty()) {
            // 指定学年の設定がなければ"00"を使用する
            param._behaviorSemesMstList = BehaviorSemesLMst.getBehaviorSemesLMstList(db2, param, "00");
        }

        final List studentList = Student.getStudentList(db2, param);

        final boolean gradeCd3GreaterEqual = Integer.parseInt(param._gradeCd) >= 3;
        final boolean gradeCd4GreaterEqual = Integer.parseInt(param._gradeCd) >= 4;
        final String form2 = gradeCd3GreaterEqual ? "KNJD183P_2_2.frm" : "KNJD183P_2.frm";
        final int form2RecordMax = gradeCd3GreaterEqual ? 37 : 31;
        final String form3 = gradeCd4GreaterEqual ? "KNJD183P_3_3.frm" : gradeCd3GreaterEqual ? "KNJD183P_3_2.frm" : "KNJD183P_3.frm";

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug(" student = " + student._schregno + " : " + student._name);

            svf.VrSetForm("KNJD183P_1.frm", 1);
            printHyoshi(svf, param, student);
            svf.VrEndPage();

            svf.VrSetForm(form2, 4);
            printStudentName(svf, student);
            printViewRecord(svf, param, student, form2RecordMax);

            svf.VrSetForm(form3, 4);
            printStudentName(svf, student);
            svf.VrsOut("HR_NAME", param._hyosiHrName + student.getPrintAttendno());
            printSpecialAct(svf, param, student, gradeCd4GreaterEqual);
            printAttendSemes(svf, param, student);
            printReportCommunication(svf, param, student, gradeCd3GreaterEqual);
            printBehavior(svf, param, student);

            svf.VrSetForm("KNJD183P_4.frm", 1);
            svf.VrsOut("DUMMY", "1");
            svf.VrEndPage();
            _hasData = true;
        }
    }

    public void printStudentName(final Vrw32alp svf, final Student student) {
        final int nameKeta = getMS932ByteCount(student._name);
        svf.VrsOut("NAME" + (nameKeta > 30 ? "3" : nameKeta > 20 ?  "2" : "1"), student._name);
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
        if (true) {
            return KNJ_EditKinsoku.getTokenList(targetsrc, dividlen, dividnum);
        }
        if (targetsrc == null) {
            return Collections.EMPTY_LIST;
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
     * 表紙
     */
    private void printHyoshi(final Vrw32alp svf, final Param _param, final Student student) {

        //svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
        svf.VrsOut("NENDO", _param._year + "年度");
        svf.VrsOut("SCHOOLNAME", _param._certifSchoolSchoolName);

        svf.VrsOut("SCHOOL_LOGO", _param.getImagePath());
        svf.VrsOut("HR_NAME", _param._hyosiHrName + student.getPrintAttendno());
        printStudentName(svf, student);
        final int principalNameKeta = getMS932ByteCount(trimLeft(_param._certifSchoolPrincipalName));
        svf.VrsOut("TEACHER_NAME1_" + (principalNameKeta > 30 ? "3" : principalNameKeta > 26 ? "2" : "1"), trimLeft(_param._certifSchoolPrincipalName));
        final int tr1NameKeta = getMS932ByteCount(_param._tr1Name);
        svf.VrsOut("TEACHER_NAME2_" + (tr1NameKeta > 30 ? "3" : tr1NameKeta > 20 ? "2" : "1"), _param._tr1Name);
    }

    /**
     * 生活の様子
     */
    private void printBehavior(final Vrw32alp svf, final Param param, final Student student) {
        final int max = 12;
        int line = 1;
        for (final Iterator itb = param._behaviorSemesMstList.iterator(); itb.hasNext();) {
            final BehaviorSemesLMst bslm = (BehaviorSemesLMst) itb.next();

            if (bslm._mMstList.size() == 0) {
                svf.VrsOut("GRP", bslm._lCd);
                svf.VrsOut("VIEW_NAME", bslm._lName);
                svf.VrEndRecord();
                line += 1;
            } else {
                final int idxPrintLLine = bslm._mMstList.size() / 2 + (bslm._mMstList.size() % 2 == 0 ? -1 : 0);
                for (int i = 0; i < bslm._mMstList.size(); i++) {
                    final BehaviorSemesLMst.BehaviorSemesMMst bsmm = (BehaviorSemesLMst.BehaviorSemesMMst) bslm._mMstList.get(i);

                    svf.VrsOut("GRP", bslm._lCd);
                    if (i == idxPrintLLine) {
                        svf.VrsOut("VIEW_NAME", bslm._lName);
                    }
                    svf.VrsOut("TARGET" + (getMS932ByteCount(bsmm._mName) > 58 ? "2" : "1") , bsmm._mName);

                    for (final Iterator it = student._hreportBehaviorLmDatList.iterator(); it.hasNext();) {
                        final HreportBehaviorLmDat hbl = (HreportBehaviorLmDat) it.next();

                        if (bslm._lCd.equals(hbl._lCd) && bsmm._mCd.equals(hbl._mCd) && "1".equals(hbl._record)) {
                            svf.VrsOut("MARK1_" + hbl._semester, "○");
                        }
                    }
                    //svf.VrsOut("SLASH1_" + "", "／");
                    svf.VrEndRecord();
                    line += 1;
                }
            }
        }
        for (int i = line; i <= max; i++) {
            svf.VrsOut("GRP", df2.format(i));
            svf.VrEndRecord();
        }
    }

    private static String notZero(int n) {
        return String.valueOf(n);
    }

    /**
     * 出欠席の様子
     */
    private void printAttendSemes(final Vrw32alp svf, final Param param, final Student student) {

        AttendSemesDat total = new AttendSemesDat(null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        boolean isAdd = false;

        for (final Iterator it = student._attendSemesDatMap.values().iterator(); it.hasNext();) {
            final AttendSemesDat att = (AttendSemesDat) it.next();
            if (!NumberUtils.isDigits(att._month)) {
                continue;
            }

            final int imonth = Integer.parseInt(att._month);
            final String field = "ATTEND" + (imonth + (imonth < 4 ? 9 : -3));
            svf.VrsOutn(field, 1, notZero(att._lesson));
            svf.VrsOutn(field, 2, notZero(att._suspend + att._mourning));
            svf.VrsOutn(field, 3, notZero(att._mlesson));
            svf.VrsOutn(field, 4, notZero(att._sick));
            svf.VrsOutn(field, 5, notZero(att._present));
            svf.VrsOutn(field, 6, notZero(att._late));
            svf.VrsOutn(field, 7, notZero(att._early));

            total = total.add(att);
            isAdd = true;
        }
        if (isAdd) {
            final AttendSemesDat att = total;
            final String field = "ATTEND_TOTAL";
            svf.VrsOutn(field, 1, notZero(att._lesson));
            svf.VrsOutn(field, 2, notZero(att._suspend + att._mourning));
            svf.VrsOutn(field, 3, notZero(att._mlesson));
            svf.VrsOutn(field, 4, notZero(att._sick));
            svf.VrsOutn(field, 5, notZero(att._present));
            svf.VrsOutn(field, 6, notZero(att._late));
            svf.VrsOutn(field, 7, notZero(att._early));
        }
    }

    /**
     * 学習の様子
     */
    private void printViewRecord(final Vrw32alp svf, final Param param, final Student student, final int recordMax) {
        int line = 0;
        for (final Iterator it = param._viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();
            if (Integer.parseInt(viewClass._classcd) >= 90) {
                continue;
            }
            final List classnameCharList = viewClass.getSubclassnameLineList();

            int vi = 0;
            int l = 0;
            while (l < classnameCharList.size()) {
                final ViewClass.View view;
                final String viewName;
                final List viewRecordList;

                //String viewcd = null;
                if (viewClass._viewList.size() - 1 < vi) {
                    view = null;
                    viewName = " ";
                    viewRecordList = Collections.EMPTY_LIST;
                } else {
                    view = viewClass.getView(vi);
                    viewName = view._viewname;
                    viewRecordList = student.getViewList(view._viewcd);
                    //viewcd = view._viewcd;
                }
                //log.info(" viewClassCd = " + viewClass._classcd + ", viewList size = " + viewClass._viewList.size() + " => viewcd = " + viewcd);

                final String classnameCharp = (String) classnameCharList.get(l);
//                log.debug(" dline = " + viewNameLineList + ", " + classnameCharp);
                final boolean isLastViewLine = vi != 0;
                svf.VrsOut("CLASSGRP" + (isLastViewLine ? "2" : "1"), viewClass.getUniqueClassCd(param));
                svf.VrsOut("CLASS_NAME" + (isLastViewLine ? "2" : "1"), classnameCharp);
                final int viewKeta = getMS932ByteCount(viewName);
                svf.VrsOut("TARGET" + (isLastViewLine ? "2" : "1") + (viewKeta > 74 ? "_2" : ""), viewName); // 観点名称
                if (null != view) {
                    for (final Iterator itflg = param._semesterList.iterator(); itflg.hasNext();) {
                        final String semester = (String) itflg.next();
                        if (!"1".equals(view._semesterViewflgMap.get(semester))) {
                            svf.VrsOut("SLASH" + (isLastViewLine ? "2" : "1") + "_" + semester, SLASH); // 観点に／表示
                        }
                    }
                }
                for (final Iterator itv = viewRecordList.iterator(); itv.hasNext();) {
                    final ViewRecord vr = (ViewRecord) itv.next();
                    svf.VrsOut("MARK" + (isLastViewLine ? "2" : "1") + "_" + vr._semester, vr._d029Namespare1); // 観点
                }
                svf.VrEndRecord();
                l += 1;
                vi += 1;
            }
            line += l;
        }

//        // 空行挿入
//        for (int i = line % maxLine == 0 ? maxLine : line % maxLine; i < maxLine; i++) {
//            svf.VrsOut("CLASS_NAME1", "\n");
//            svf.VrEndRecord();
//        }
        if (line == 0) {
            svf.VrsOut("CLASS_NAME1", "\n");
            svf.VrEndRecord();
        }
    }

    /**
     * 特別活動の様子
     */
    private void printSpecialAct(final Vrw32alp svf, final Param param, final Student student, final boolean gradeCd4GreaterEqual) {
        final int pchars = getParamSizeNum(param._HREPORTREMARK_DAT_REMARK2_SIZE_P, 0);
        final int plines = getParamSizeNum(param._HREPORTREMARK_DAT_REMARK2_SIZE_P, 1);
        final int chars = (-1 == pchars || -1 == plines) ? 12 : pchars;
        final int lines = (-1 == pchars || -1 == plines) ? 3  : plines;

        if (gradeCd4GreaterEqual) {
            //4年生以上
            //_hReportRemarkDatListが空の場合に備え、画像を出力
            svf.VrsOut("SLASH_CLUB", param._SlashImagePath);
            svf.VrsOut("SLASH_COMMITTEE", param._SlashImagePath);
        }

        boolean outputFlg = false;
        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat dat = (HReportRemarkDat) it.next();
            if (param._semester.compareTo(dat._semester) < 0) {
                continue;
            }

            VrsOutfieldRenban(svf, "SP_ACT1", KNJ_EditEdit.get_token(dat._remark2, chars * 2, lines), Integer.parseInt(dat._semester));
            if (gradeCd4GreaterEqual && !outputFlg) {
                //委員会/クラブ活動のデータは通年データで、どのレコードにも同じデータが入るようになっているため、先頭データで出力する。
                if(dat._club != null && dat._club.length() > 0 ) {
                    VrsOutfieldRenban(svf, "SP_ACT2", KNJ_EditEdit.get_token(dat._club, chars * 2, lines), 0);
                    svf.VrsOut("SLASH_CLUB", ""); //画像をクリア
                }
                if(dat._committee != null && dat._committee.length() > 0 ) {
                    VrsOutfieldRenban(svf, "SP_ACT3", KNJ_EditEdit.get_token(dat._committee, chars * 2, lines), 0);
                    svf.VrsOut("SLASH_COMMITTEE", ""); //画像をクリア
                }
                outputFlg = true;
            }
        }
    }

    private void VrsOutfieldRenban(final Vrw32alp svf, final String field, final String[] Strs, final int gyo) {
        if (Strs == null) return;
        int lenCnt = 0;
        for (int i = 0; i < Strs.length; i++) {
            if (Strs[i] != null) {
                lenCnt++;
            }
        }
        for (int i = 0; i < Strs.length; i++) {
            if (!"".equals(Strs[i])) {
                if (gyo <= 0) {
                    //1行の場合は、真ん中にのみ出力する。それ以外は上から埋める。
                    if (lenCnt == 1) {
                        if (Strs[i] != null) {
                            svf.VrsOut(field + "_2", Strs[i]);
                        }
                    } else {
                        if (Strs[i] != null) {
                            svf.VrsOut(field + "_" + (i+1), Strs[i]);
                        }
                    }
                } else {
                    //1行の場合は、真ん中にのみ出力する。それ以外は上から埋める。
                    if (lenCnt == 1) {
                        if (Strs[i] != null) {
                            svf.VrsOutn(field + "_2", gyo, Strs[i]);
                        }
                    } else {
                        if (Strs[i] != null) {
                            svf.VrsOutn(field + "_" + (i+1), gyo, Strs[i]);
                        }
                    }
                }
            }
        }
    }

    /**
     * 通信欄
     */
    private void printReportCommunication(final Vrw32alp svf, final Param param, final Student student, final boolean gradeCd3GreaterEqual) {
        if (gradeCd3GreaterEqual) {

            svf.VrsOut("SP_SEMESTER", param._semestername);

            final int pchars = getParamSizeNum(param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P, 0);
            final int plines = getParamSizeNum(param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P, 1);
            final int chars = (-1 == pchars || -1 == plines) ? 44 : pchars;
            final int lines = (-1 == pchars || -1 == plines) ? 2  : plines;

            boolean bPrtFlg = false;
            for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
                final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();
                final String semester = hReportRemarkDat._semester;
                if (!param._semester.equals(semester)) {
                    continue;
                }

                bPrtFlg = true;
                if (hReportRemarkDat._totalstudytime != null && !"".equals(hReportRemarkDat._totalstudytime)) {
                    final List list = retDividString(hReportRemarkDat._totalstudytime, chars * 2 , lines);
                    for (int i = 0; i < list.size(); i++) {
                        svf.VrsOutn("SP_ACT", i + 1, (String) list.get(i));
                    }
                } else {
                    svf.VrsOut("SLASH_SP", param._SlashImagePath);
                }
                break;
            }
            if (!bPrtFlg) {
                svf.VrsOut("SLASH_SP", param._SlashImagePath);
            }
        }

        final int pchars = getParamSizeNum(param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_P, 0);
        final int plines = getParamSizeNum(param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_P, 1);
        final int chars = (-1 == pchars || -1 == plines) ? 30 : pchars;
        final int lines = (-1 == pchars || -1 == plines) ? 9  : plines;

        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();
            final String semester = hReportRemarkDat._semester;
            if (!param._semester.equals(semester)) {
                continue;
            }

            final List list = retDividString(hReportRemarkDat._communication, chars * 2 , lines);
            for (int i = 0; i < list.size(); i++) {
                svf.VrsOut("COMM1_" + String.valueOf(i + 1), (String) list.get(i));
            }
        }
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        List _viewRecordList = Collections.EMPTY_LIST;
        Map _attendSemesDatMap = Collections.EMPTY_MAP; // 出欠のようす
        List _hreportBehaviorLmDatList = Collections.EMPTY_LIST; // 生活・特別活動のようす
        List _hReportRemarkDatList = Collections.EMPTY_LIST; // 所見

        public Student(final String schregno, final String name, final String hrName, final String attendno) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
        }

        public String getPrintAttendno() {
            final String attendno = null == _attendno ? "" : ("　" + ((NumberUtils.isDigits(_attendno)) ? (String.valueOf(Integer.parseInt(_attendno)) + "番") : _attendno));
            return attendno;
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

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentSql(param);
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
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                student._viewRecordList = ViewRecord.getViewRecordList(db2, param, student._schregno);
                student._attendSemesDatMap = AttendSemesDat.getAttendSemesDatList(db2, param, student._schregno);
                student._hreportBehaviorLmDatList = HreportBehaviorLmDat.getHreportBehaviorLmDatList(db2, param, student._schregno);
                student._hReportRemarkDatList = HReportRemarkDat.getHReportRemarkDatList(db2, param, student._schregno);
            }
            return studentList;
        }

        private static String getStudentSql(final Param param) {
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
    }

    /**
     * 観点の教科
     */
    private static class ViewClass {

        static class View {
            final String _viewcd;
            final String _viewname;
            final Map _semesterViewflgMap;
            View(final String viewcd, final String viewname) {
                _viewcd = viewcd;
                _viewname = viewname;
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
                return _classcd + "-" + _schoolKind;
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

        public List getSubclassnameLineList() {
            if (null == _subclassname || "".equals(_subclassname)) {
                return Collections.EMPTY_LIST;
            }
            final int mojiPerLine = 1;
            List rtn = new ArrayList();
            final int viewLineSize = _viewList.size();
            final int nameLine = _subclassname.length() / mojiPerLine + (_subclassname.length() % mojiPerLine == 0 ? 0 : 1);
            if (nameLine >= viewLineSize) {
                for (int i = 0; i < nameLine; i++) {
                    rtn.add(_subclassname.substring(i * mojiPerLine, Math.min((i + 1) * mojiPerLine, _subclassname.length())));
                }
            } else {
                String subclassname = "";
//                subclassname += StringUtils.repeat(" ", (viewLineSize * mojiPerLine - _subclassname.length()) / 2);
                subclassname += _subclassname;
                subclassname += StringUtils.repeat(" ", viewLineSize * mojiPerLine - subclassname.length());

                for (int i = 0; i < viewLineSize; i++) {
                    rtn.add(subclassname.substring(i * mojiPerLine, Math.min(subclassname.length(), (i + 1) * mojiPerLine)));
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

                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");
                    final String semester = rs.getString("SEMESTER");
                    final String viewflg = rs.getString("VIEWFLG");

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
                            if (viewClass0._classcd.equals(classcd) && viewClass0._schoolKind.equals(schoolKind)) {
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
                        viewClass._viewList.add(new View(viewcd, viewname));
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
            ResultSet rs = null;
            try {
                final String psKey = "VIEW_REC";
                if (null == param._psMap.get(psKey) ) {
                    final String sql = getViewRecordSql(param);
                    log.debug(" view record sql = "+  sql);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                final PreparedStatement ps = (PreparedStatement) param._psMap.get(psKey);
                ps.setString(1, schregno);
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
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return list;
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
            stb.append("         AND T3.SCHREGNO = ? ");
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

        final String _month;
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
                final String month,
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
            _month = month;
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

        private AttendSemesDat add(final AttendSemesDat dat) {
            return new AttendSemesDat(
                    _month,
                    _lesson + dat._lesson,
                    _suspend + dat._suspend,
                    _mourning + dat._mourning,
                    _mlesson + dat._mlesson,
                    _sick + dat._sick,
                    _absent + dat._absent,
                    _present + dat._present,
                    _late + dat._late,
                    _early + dat._early,
                    _transferDate + dat._transferDate,
                    _offdays + dat._offdays
                    );
        }

        private static Map getAttendSemesDatList(final DB2UDB db2, final Param param, final String schregno) {
            final Map map = new HashMap();
            ResultSet rs = null;
            try {
                final String psKey = "ATTEND_SEMES";
                if (null == param._psMap.get(psKey) ) {
                    final String sql = AttendSemesDat.sql(param);
                    log.debug(" sqllAttendSemes = " + sql);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                final PreparedStatement ps = (PreparedStatement) param._psMap.get(psKey);
                ps.setString(1, schregno);

                rs = ps.executeQuery();
                while (rs.next()) {

                    final String month = rs.getString("MONTH");
                    final String appointedDate = (Integer.parseInt(month) < 4 ? String.valueOf(1 + Integer.parseInt(param._year)) : param._year) + "-" + month + "-" + rs.getString("APPOINTED_DAY");
                    if (null == month || null != param._date && param._date.compareTo(appointedDate) < 0) {
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

                    final AttendSemesDat att = new AttendSemesDat(month, lesson, suspend, mourning, mlesson, sick, absent, present, late, early, transferDate, offdays);
                    if (null == map.get(month)) {
                        map.put(month, att);
                    } else {
                        map.put(month, ((AttendSemesDat) map.get(month)).add(att));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return map;
        }

        private static String sql(final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append("    SELECT ");
            stb.append("        SEMESTER, ");
            stb.append("        MONTH, ");
            stb.append("        MAX(APPOINTED_DAY) AS APPOINTED_DAY, ");
            stb.append("        SUM( VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            if ("1".equals(param._knjSchoolMst._semOffDays)) {
                stb.append("           + VALUE(OFFDAYS, 0) ");
            }
            stb.append("        ) AS LESSON, ");
            stb.append("        SUM(VALUE(MOURNING, 0)) AS MOURNING, ");
            stb.append("        SUM(VALUE(SUSPEND, 0)) ");
            if ("true".equals(param._useVirus)) {
                stb.append("        + SUM(VALUE(VIRUS, 0)) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append("        + SUM(VALUE(KOUDOME, 0)) ");
            }
            stb.append("        AS SUSPEND, ");
            stb.append("        SUM(ABSENT) AS ABSENT, ");
            stb.append("        SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
            if ("1".equals(param._knjSchoolMst._semOffDays)) {
                stb.append("           + VALUE(OFFDAYS, 0) ");
            }
            stb.append("        ) AS SICK, ");
            stb.append("        SUM( VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            if ("1".equals(param._knjSchoolMst._semOffDays)) {
                stb.append("           + VALUE(OFFDAYS, 0) ");
            }
            stb.append("        ) - SUM(VALUE(SUSPEND,0)) - SUM(VALUE(MOURNING,0)) ");
            if ("true".equals(param._useVirus)) {
                stb.append("        - SUM(VALUE(VIRUS,0)) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append("        - SUM(VALUE(KOUDOME,0)) ");
            }
            stb.append("        AS MLESSON, ");
            stb.append("        SUM( VALUE(LESSON, 0) - VALUE(OFFDAYS, 0) - VALUE(ABROAD, 0) ");
            if ("1".equals(param._knjSchoolMst._semOffDays)) {
                stb.append("           + VALUE(OFFDAYS, 0) ");
            }
            stb.append("        ) - SUM(SUSPEND) - SUM(MOURNING) ");
            if ("true".equals(param._useVirus)) {
                stb.append("        - SUM(VALUE(VIRUS, 0)) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append("        - SUM(VALUE(KOUDOME, 0)) ");
            }
            stb.append("          - SUM( VALUE(SICK, 0) + VALUE(NOTICE, 0) + VALUE(NONOTICE, 0) ");
            if ("1".equals(param._knjSchoolMst._semOffDays)) {
                stb.append("           + VALUE(OFFDAYS, 0) ");
            }
            stb.append("        ) AS PRESENT, ");
            stb.append("        SUM(VALUE(LATE, 0)) AS LATE, ");
            stb.append("        SUM(VALUE(EARLY, 0)) AS EARLY, ");
            stb.append("        SUM(VALUE(ABROAD, 0)) AS TRANSFER_DATE, ");
            stb.append("        SUM(VALUE(OFFDAYS, 0)) AS OFFDAYS ");
            stb.append("    FROM ");
            stb.append("        V_ATTEND_SEMES_DAT W1 ");
            stb.append("    WHERE ");
            stb.append("        W1.YEAR = '" + param._year + "' ");
            stb.append("        AND W1.SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        SEMESTER, MONTH ");
            return stb.toString();
        }

    }

    /**
     * 学校生活のようす（マスタ）
     */
    private static class BehaviorSemesLMst {
        final String _lCd;
        final String _lName;
        final List _mMstList = new ArrayList();

        BehaviorSemesLMst(
            final String lCd,
            final String lName
        ) {
            _lCd = lCd;
            _lName = lName;
        }

        private static BehaviorSemesLMst getBehaviorSemesLMst(final List list, final String lCd) {
            if (null == lCd) {
                return null;
            }
            for (final Iterator it = list.iterator(); it.hasNext();) {
                BehaviorSemesLMst lMst = (BehaviorSemesLMst) it.next();
                if (lCd.equals(lMst._lCd)) {
                    return lMst;
                }
            }
            return null;
        }

        private static class BehaviorSemesMMst {
            final String _lCd;
            final String _mCd;
            final String _mName;
            public BehaviorSemesMMst(final String lCd, final String mCd, final String mName) {
                _lCd = lCd;
                _mCd = mCd;
                _mName = mName;
            }
        }

        public static List getBehaviorSemesLMstList(final DB2UDB db2, final Param param, final String grade) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param, grade);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String lCd = rs.getString("L_CD");

                    if (null == getBehaviorSemesLMst(list, lCd)) {
                        final String lName = rs.getString("L_NAME");
                        final BehaviorSemesLMst behaviorsemeslmst = new BehaviorSemesLMst(lCd, lName);
                        list.add(behaviorsemeslmst);
                    }

                    final String mCd = rs.getString("M_CD");
                    final String mName = rs.getString("M_NAME");
                    getBehaviorSemesLMst(list, lCd)._mMstList.add(new BehaviorSemesMMst(lCd, mCd, mName));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String sql(final Param param, final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.L_CD, ");
            stb.append("     T1.L_NAME, ");
            stb.append("     T2.M_CD, ");
            stb.append("     T2.M_NAME ");
            stb.append(" FROM HREPORT_BEHAVIOR_L_MST T1 ");
            stb.append(" LEFT JOIN HREPORT_BEHAVIOR_M_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND T2.GRADE = T1.GRADE ");
            stb.append("     AND T2.L_CD = T1.L_CD ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SCHOOL_KIND = 'P' ");
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.L_CD, ");
            stb.append("     T2.M_CD ");
            return stb.toString();
        }
    }

    /**
     * 学校生活のようす
     */
    private static class HreportBehaviorLmDat {

        final String _semester;
        final String _lCd;
        final String _mCd;
        final String _record;

        public HreportBehaviorLmDat(
                final String semester,
                final String lCd,
                final String mCd,
                final String record) {
            _semester = semester;
            _lCd = lCd;
            _mCd = mCd;
            _record = record;
        }

        public static List getHreportBehaviorLmDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            ResultSet rs = null;
            try {
                final String psKey = "BEHAVIORSEMES";
                if (null == param._psMap.get(psKey) ) {
                    final String sql = getHreportBehaviorLmDatSql(param);
                    log.debug(" sql HreportBehaviorLmDat = " + sql);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                final PreparedStatement ps = (PreparedStatement) param._psMap.get(psKey);
                ps.setString(1, schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String lCd = rs.getString("L_CD");
                    final String mCd = rs.getString("M_CD");
                    final String record = rs.getString("RECORD");

                    final HreportBehaviorLmDat behaviorSemesDat = new HreportBehaviorLmDat(semester, lCd, mCd, record);

                    list.add(behaviorSemesDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return list;
        }

        private static String getHreportBehaviorLmDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORT_BEHAVIOR_LM_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            return stb.toString();
        }
    }

    /**
     * 通知表所見
     */
    private static class HReportRemarkDat {
        final String _semester;
        final String _totalstudytime;
        final String _communication;      // 担任からの所見
        final String _remark2;            // 特別活動の記録・係活動
        final String _committee;
        final String _club;

        public HReportRemarkDat(
                final String semester,
                final String totalstudytime,
                final String communication,
                final String remark2,
                final String committee,
                final String club) {
            _totalstudytime = totalstudytime;
            _semester = semester;
            _communication = communication;
            _remark2 = remark2;
            _committee = committee;
            _club = club;
        }

        public static List getHReportRemarkDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            ResultSet rs = null;
            try {
                final String psKey = "HREPORTREMARK";
                if (null == param._psMap.get(psKey) ) {
                    final String sql = getHReportRemarkDatSql(param);
                    log.debug(" hreportremark = " + sql);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                final PreparedStatement ps = (PreparedStatement) param._psMap.get(psKey);
                ps.setString(1, schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String totalstudytime = rs.getString("TOTALSTUDYTIME");
                    final String communication = rs.getString("COMMUNICATION");
                    final String remark2 = rs.getString("REMARK2");
                    final String committee = rs.getString("COMMITTEE");
                    final String club = rs.getString("CLUB");
                    final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, totalstudytime, communication, remark2, committee, club);
                    list.add(hReportRemarkDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return list;
        }

        private static String getHReportRemarkDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append("     ,T2.REMARK1 AS COMMITTEE ");
            stb.append("     ,T2.REMARK3 AS CLUB ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append("     LEFT JOIN HREPORTREMARK_DAT T2 ");
            stb.append("       ON T2.YEAR = T1.YEAR ");
            stb.append("      AND T2.SEMESTER = '9' ");
            stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            return stb.toString();
        }
    }

//    //--- 内部クラス -------------------------------------------------------
//    private static class KNJSvfFieldModify {
//
//        private static final Log log = LogFactory.getLog(KNJSvfFieldModify.class);
//
//        private final int _width;   //フィールドの幅(ドット)
//        private final int _height;  //フィールドの高さ(ドット)
//        private final int _ystart;  //開始位置(ドット)
//        private final int _minnum;  //最小設定文字数
//        private final int _maxnum;  //最大設定文字数
//
//        public KNJSvfFieldModify(final int width, final int height, final int ystart, final int minnum, final int maxnum) {
//            _width = width;
//            _height = height;
//            _ystart = ystart;
//            _minnum = minnum;
//            _maxnum = maxnum;
//        }
//
//        /**
//         * 中央割付フィールドで文字の大きさ調整による中心軸のずれ幅の値を得る
//         * @param posx1 フィールドの左端X
//         * @param posx2 フィールドの右端X
//         * @param num フィールド指定の文字数
//         * @param charSize 変更後の文字サイズ
//         * @return ずれ幅の値
//         */
//        public int getModifiedCenteringOffset(final int posx1, final int posx2, final int num, float charSize) {
//            final int maxWidth = getStringLengthPixel(charSize, num); // 文字の大きさを考慮したフィールドの最大幅
//            final int offset = (maxWidth / 2) - (posx2 - posx1) / 2;
//            return offset;
//        }
//
//        private int getStringLengthPixel(final float charSize, final int num) {
//            return charSizeToPixel(charSize) * num / 2;
//        }
//
//        /**
//         *  ポイントの設定
//         *  引数について  String str : 出力する文字列
//         */
//        public float getCharSize(final String str)
//        {
//            final int num = Math.min(Math.max(retStringByteValue( str ), _minnum), _maxnum);
//            return Math.min((float) pixelToCharSize(_height), retFieldPoint(_width, num));  //文字サイズ
//        }
//
//        /**
//         * 文字サイズをピクセルに変換した値を得る
//         * @param charSize 文字サイズ
//         * @return 文字サイズをピクセルに変換した値
//         */
//        public static int charSizeToPixel(final double charSize)
//        {
//            return (int) Math.round(charSize / 72 * 400);
//        }
//
//        /**
//         * ピクセルを文字サイズに変換した値を得る
//         * @param charSize ピクセル
//         * @return ピクセルを文字サイズに変換した値
//         */
//        public static double pixelToCharSize(final int pixel)
//        {
//            return pixel / 400.0 * 72;
//        }
//
//        /**
//         *  Ｙ軸の設定
//         *  引数について  int hnum   : 出力位置(行)
//         */
//        public float getYjiku(final int hnum, final float charSize)
//        {
//            float jiku = 0;
//            try {
//                jiku = retFieldY(_height, charSize) + _ystart + _height * hnum;  //出力位置＋Ｙ軸の移動幅
//            } catch (Exception ex) {
//                log.error("setRetvalue error!", ex);
//                log.debug(" jiku = " + jiku);
//            }
//            return jiku;
//        }
//
//        /**
//         *  文字数を取得
//         */
//        private static int retStringByteValue(final String str)
//        {
//            if ( str == null )return 0;
//            int ret = 0;
//            try {
//                ret = str.getBytes("MS932").length;   //文字列をbyte配列へ
//            } catch (Exception ex) {
//                log.error("retStringByteValue error!", ex);
//            }
//            return ret;
//        }
//
//        /**
//         *  文字サイズを設定
//         */
//        private static float retFieldPoint(final int width, final int num)
//        {
//            return (float) Math.round((float) width / (num / 2 + (num % 2 == 0 ? 0 : 1)) * 72 / 400 * 10) / 10;
//        }
//
//        /**
//         *  Ｙ軸の移動幅算出
//         */
//        private static float retFieldY(final int height, final float charSize)
//        {
//            return (float) Math.round(((double) height - (charSize / 72 * 400)) / 2);
//        }
//
//        public String toString() {
//            return "KNJSvfFieldModify: width = "+ _width + " , height = " + _height + " , ystart = " + _ystart + " , minnum = " + _minnum + " , maxnum = " + _maxnum;
//        }
//    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75855 $");
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
        final String _documentRoot;
        final String _imagePath;
        final String _extension;
        //final String _descDate;

        final String _certifSchoolSchoolName;
        final String _certifSchoolRemark3;
        final String _certifSchoolPrincipalName;
        final String _tr1Name;
        final String _hyosiHrName;
        final String _certifSchoolJobName;
//        final String _d016Namespare1;
        final List _d026subclasscdList;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final String _useVirus;
        final String _useKoudome;

        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_P;
        final String _HREPORTREMARK_DAT_REMARK2_SIZE_P;
        final String _knjdBehaviorsd_UseText_P;
        final String _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P;

        /** 各学校における定数等設定 */
        private KNJSchoolMst _knjSchoolMst;

        final String _gradeCd;

        final String _semestername;
        final List _semesterList;
        final Map _psMap = new HashMap();

        List _viewClassList = Collections.EMPTY_LIST;
        List _behaviorSemesMstList = Collections.EMPTY_LIST;

        final String _SlashImagePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _categorySelected = request.getParameterValues("category_selected");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _imagePath = request.getParameter("IMAGEPATH");

            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
            //_descDate = request.getParameter("DESC_DATE");

            _gradeCd = getGradeCd(db2, _grade);
            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolRemark3 = getCertifSchoolDat(db2, "REMARK3");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _tr1Name = getStaffname(db2);
            _hyosiHrName = "第" + hankakuToZenkaku(NumberUtils.isDigits(_gradeCd) ? String.valueOf(Integer.parseInt(_gradeCd)) : " ") + "学年 " + getHrClassName1(db2, _year, _semester, _gradeHrclass) + "組";
            _certifSchoolJobName = getCertifSchoolDat(db2, "JOB_NAME");
//            _d016Namespare1 = getNameMst(db2, _year, "D016", "01", "NAMESPARE1");
            _d026subclasscdList = getNameMstList(db2, _year);
            log.fatal(" d026 subclasscd = " + _d026subclasscdList);

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"), "+", " ");
            _HREPORTREMARK_DAT_REMARK2_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_REMARK2_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_REMARK2_SIZE_P"), "+", " ");
            _knjdBehaviorsd_UseText_P = request.getParameter("knjdBehaviorsd_UseText_P");
            _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P"), "+", " ");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            // 学期名称 _arrsemesName をセットします。
            _semesterList = getSemesterList(db2);
            _semestername = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));

            _SlashImagePath = getImageFilePath("slash.jpg");
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

//        private String getNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
//            String rtn = "";
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final StringBuffer sql = new StringBuffer();
//                sql.append(" SELECT ");
//                sql.append("    " + field + " ");
//                sql.append(" FROM NAME_MST T1 ");
//                sql.append("    INNER JOIN NAME_YDAT T2 ON T2.YEAR = '" + year + "' AND T2.NAMECD1 = T1.NAMECD1 AND T2.NAMECD2 = T1.NAMECD2 ");
//                sql.append(" WHERE ");
//                sql.append("    T1.NAMECD1 = '" + namecd1 + "' ");
//                sql.append("    AND T1.NAMECD2 = '" + namecd2 + "' ");
//                sql.append("   ");
//                ps = db2.prepareStatement(sql.toString());
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    if (null != rs.getString(field)) {
//                        rtn = rs.getString(field);
//                    }
//                }
//            } catch (SQLException e) {
//                log.error("exception!", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return rtn;
//        }


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

        private String getStaffname(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "";
                sql += " SELECT T1.STAFFNAME ";
                sql += " FROM STAFF_MST T1 ";
                sql += " INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = '" + _year + "' AND T2.SEMESTER = '" + _semester + "' AND GRADE || HR_CLASS = '" + _gradeHrclass + "' AND T2.TR_CD1 = T1.STAFFCD ";
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


        public String getImageFilePath(final String name) {
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
                    gradeCd = rs.getString("GRADE_CD");
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


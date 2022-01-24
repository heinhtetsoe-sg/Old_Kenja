/*
 * $Id: ed442c5464afabf10231a5f7345103ff15e2f4f2 $
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理] 中学通知票
 */

public class KNJD171J {

    private static final Log log = LogFactory.getLog(KNJD171J.class);

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
        
        final List studentList = getStudentList(db2);
        AttendSemesDat.setAttendSemesDatList(db2, _param, studentList);
        //AttendSubclassDat.setAttendSubclassDatList(db2, _param, studentList);
        
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            student.load(db2, _param);
            
            if (_param._isPrintHyoshi) {
                // 表紙
                printSvfHyoshi(db2, svf, student);
                _hasData = true;
            } 
            if (_param._isPrintSeiseki) {
                // 学習のようす等
                printSvfMainSeiseki(db2, svf, student, viewClassList);
                _hasData = true;
            }
            if (_param._isPrintSyoken) {
                // 所見等
                printSvfMainSyoken(db2, svf, student);
                _hasData = true;
            }
            if (_param._isPrintSyuryo) {
                // 修了証
                printSvfSyuryo(db2, svf, student);
                _hasData = true;
            } 
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
        stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append("    FROM    SCHREG_REGD_DAT T1 ");
        stb.append("            , V_SEMESTER_GRADE_MST T2 ");
        stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
        stb.append("        AND T1.SEMESTER = '"+ param._semester +"' ");
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.GRADE = T2.GRADE ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
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
    
    private int getMS932ByteCount(final String str) {
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
    
    private String toZenkaku(final String semester) {
        if (StringUtils.isBlank(semester)) {
            return "";
        }
        final char n = semester.charAt(0);
        return String.valueOf((char) (0xFF10 + n - '0'));
    }
    
    /**
     * 表紙を印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printSvfHyoshi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD171J_1_1.frm", 1);
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
        final String schoolField;
        if (getMS932ByteCount(_param._certifSchoolSchoolName) > 22) {
            schoolField = "SCHOOLNAME_2";
        } else {
            schoolField = "SCHOOLNAME";
        }
        svf.VrsOut(schoolField, _param._certifSchoolSchoolName);
        svf.VrsOut("LOGO", _param.getImagePath());
        svf.VrsOut("HR_NAME", _param._hyosiHrName);
        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno;
        svf.VrsOut("ATTENDNO", attendno);
        svf.VrsOut("NAME" + (getMS932ByteCount(student._name) > 26 ? "2" : ""), student._name);
        svf.VrsOut("STAFFNAME1_" + (getMS932ByteCount(trimLeft(_param._certifSchoolPrincipalName)) > 20 ? "2" : "1"), trimLeft(_param._certifSchoolPrincipalName));
        svf.VrsOut("STAFFNAME2_" + (getMS932ByteCount(_param._tr1Name) > 20 ? "2" : "1"), _param._tr1Name);
        //表紙(観点の評価について)
        printSvfHyoshiView(svf);
        //表紙(選択科目の評価・評定について)
        printSvfHyoshiSelect(svf);
        svf.VrEndPage();
    }
    
    /**
     * 表紙(観点の評価について)を印刷する
     * @param svf
     */
    private void printSvfHyoshiView(final Vrw32alp svf) {
        String no = "1";
        String view = "";
        String brank = "　";
        int cnt = 0;
        for (final Iterator it = _param._nmD029List.iterator(); it.hasNext();) {
            final NMD029 nmD029 = (NMD029) it.next();
            final String name1 = null != nmD029._name1 ? nmD029._name1 : "";
            final String name2 = null != nmD029._name2 ? nmD029._name2 : "";

            cnt++;
            if (4 == cnt) {
                no = "2";
                view = "";
            }
            view = view + brank + name1 + "（" + name2 + "）";
            svf.VrsOut("VIEW" + no, view);
        }
    }
    
    /**
     * 表紙(選択科目の評価・評定について)を印刷する
     * @param svf
     */
    private void printSvfHyoshiSelect(final Vrw32alp svf) {
        String no = "1";
        String view = "";
        String brank = "　";
        int cnt = 0;
        for (final Iterator it = _param._nmD001List.iterator(); it.hasNext();) {
            final NMD001 nmD001 = (NMD001) it.next();
            final String name1 = null != nmD001._abbv1 ? nmD001._abbv1 : "";
            final String name2 = null != nmD001._name2 ? nmD001._name2 : "";

            cnt++;
            if (4 == cnt) {
                no = "2";
                view = "";
            }
            view = view + brank + name1 + "（" + name2 + "）";
            svf.VrsOut("SELECT" + no, view);
            svf.VrsOut("SELECT_NAME", "【選択科目の評価・評定について】");
        }
    }
    
    /**
     * 修了証を印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printSvfSyuryo(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD171J_4_1.frm", 1);
        svf.VrsOut("BLANK", _param._semester);
        if (getMS932ByteCount(student._name) > 26) {
            svf.VrsOut("NAME2", student._name);
            if (_param._semester.equals(_param._knjSchoolMst._semesterDiv)) {
                svf.VrAttribute("NAME2", "UnderLine=(0,3,1),keta=46");
            }
        } else {
            svf.VrsOut("NAME", student._name);
            if (_param._semester.equals(_param._knjSchoolMst._semesterDiv)) {
                svf.VrAttribute("NAME", "UnderLine=(0,3,1),keta=26");
            }
        }
        if (NumberUtils.isDigits(_param._gradeCdStr)) {
            svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(_param._gradeCdStr)));
        }
        if (_param._semester.equals(_param._knjSchoolMst._semesterDiv)) {
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._descDate));
        }
        final String schoolField;
        if (getMS932ByteCount(_param._certifSchoolRemark3) > 22) {
            schoolField = "SCHOOLNAME1_2";
        } else {
            schoolField = "SCHOOLNAME1";
        }
        svf.VrsOut(schoolField, _param._certifSchoolRemark3);
        svf.VrsOut("JOB", _param._certifSchoolJobName);
        svf.VrsOut("STAFFNAME1_3", _param._certifSchoolPrincipalName);
        svf.VrEndPage();
    }
    
    /**
     * 学習のようす等を印刷する
     * @param db2
     * @param svf
     * @param student
     * @param viewClassList
     */
    private void printSvfMainSeiseki(final DB2UDB db2, final Vrw32alp svf, final Student student, final List viewClassList) {
        
        svf.VrSetForm("KNJD171J_2_1.frm", 4);
        
        printSvfStudent(svf, student);
        
        printSvfReportTotalstudytime(svf, student);
        
        printSvfViewRecord(svf, student, viewClassList);
    }
    
    /**
     * 所見等を印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printSvfMainSyoken(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = (_param._semester.equals(_param._knjSchoolMst._semesterDiv)) ? "KNJD171J_3_1_2.frm" : "KNJD171J_3_1.frm";
        svf.VrSetForm(form, 1);
        
        printSvfStudent(svf, student);
        
        printSvfReport(svf, student);
        
        printSvfBehavior(svf, student);
        
        printSvfMedexam(svf, student);
        
        printSvfAttendSemes(svf, student);
        
        svf.VrEndPage();
    }
    
    private void printSvfStudent(final Vrw32alp svf, final Student student) {
        svf.VrsOut("NAME", student._name);
        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno;
        svf.VrsOut("ATTENDNO", attendno);
        svf.VrsOut("SEMESTER", toZenkaku(_param._semester));
    }
    
    /**
     * 『身体の記録』を印字する
     * @param svf
     * @param student
     */
    private void printSvfMedexam(final Vrw32alp svf, final Student student) {
        if (null != student._medexamDetDat) {
            final MedexamDat medexamDetDat = student._medexamDetDat;
            svf.VrsOut("HEIGHT", medexamDetDat._height);
            svf.VrsOut("WEIGHT", medexamDetDat._weight);
            svf.VrsOut("R_BAREVISION", medexamDetDat._rBarevisionMark);
            svf.VrsOut("R_VISION", medexamDetDat._rVisionMark);
            svf.VrsOut("L_BAREVISION", medexamDetDat._lBarevisionMark);
            svf.VrsOut("L_VISION", medexamDetDat._lVisionMark);

            final int adultTooth = NumberUtils.isDigits(medexamDetDat._remainAdultTooth) ? Integer.parseInt(medexamDetDat._remainAdultTooth) : 0;
            final int babyTooth = NumberUtils.isDigits(medexamDetDat._remainBabyTooth) ? Integer.parseInt(medexamDetDat._remainBabyTooth) : 0;
            svf.VrsOut("CARIES1", String.valueOf(adultTooth + babyTooth));
        }
        
        final int pchars = getParamSizeNum(_param._reportSpecialSize03_01, 0);
        final int plines = getParamSizeNum(_param._reportSpecialSize03_01, 1);
        final int chars = (-1 == pchars || -1 == plines) ? 10 : pchars;
        final int lines = (-1 == pchars || -1 == plines) ?  3 : plines;
        
        for (final Iterator it = student._hReportRemarkDetailDatList.iterator(); it.hasNext();) {
            final HReportRemarkDetailDat hReportRemarkDetailDat = (HReportRemarkDetailDat) it.next();
            final String div = hReportRemarkDetailDat._div;
            final String code = hReportRemarkDetailDat._code;
            final String remark = hReportRemarkDetailDat._remark1;

            //身体の記録(その他)
            if ("03".equals(div)) {
                if ("01".equals(code)) VrsOutRenban(svf, "note", knjobj.retDividString(remark, chars * 2, lines));
            }
        }
    }

    /**
     * 『学校生活のようす』を印字する
     * @param svf
     * @param student
     */
    private void printSvfBehavior(final Vrw32alp svf, final Student student) {
        for (final Iterator it = student._behaviorSemesDatList.iterator(); it.hasNext();) {
            final BehaviorSemesDat behaviorSemesDat = (BehaviorSemesDat) it.next();
            
            if (NumberUtils.isDigits(behaviorSemesDat._code)) {
                svf.VrsOutn("ACTION" + behaviorSemesDat._semester, Integer.parseInt(behaviorSemesDat._code), behaviorSemesDat._mark);
            }
        }
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
        boolean addflg = false;
        final AttendSemesDat total = new AttendSemesDat("9");
        for (final Iterator it = student._attendSemesDatList.iterator(); it.hasNext();) {
            final AttendSemesDat attendSemesDat = (AttendSemesDat) it.next();
            if (!NumberUtils.isDigits(attendSemesDat._semester)) {
                continue;
            }
            if (_param._notPrintAttendanceSemesterSet.contains(attendSemesDat._semester)) {
                continue;
            }
            
            final int j;
            if ("9".equals(attendSemesDat._semester)) {
                continue;
            } else {
                j = Integer.parseInt(attendSemesDat._semester);
            }
            addflg = true;
            svf.VrsOutn("LESSON", j, notZero(attendSemesDat._lesson));
            svf.VrsOutn("SUSPEND", j, notZero(attendSemesDat._suspend + attendSemesDat._mourning + attendSemesDat._virus + attendSemesDat._koudome));
            svf.VrsOutn("PRESENT", j, notZero(attendSemesDat._mlesson));
            svf.VrsOutn("ABSENCE", j, notZero(attendSemesDat._sick));
            svf.VrsOutn("ATTEND", j, notZero(attendSemesDat._present));
            svf.VrsOutn("KEKKA", j, notZero(attendSemesDat._kekkaJisu));
            total.add(attendSemesDat);
        }
        if (addflg) {
            final int j = 4;
            svf.VrsOutn("LESSON", j, notZero(total._lesson));
            svf.VrsOutn("SUSPEND", j, notZero(total._suspend + total._mourning + total._virus + total._koudome));
            svf.VrsOutn("PRESENT", j, notZero(total._mlesson));
            svf.VrsOutn("ABSENCE", j, notZero(total._sick));
            svf.VrsOutn("ATTEND", j, notZero(total._present));
            svf.VrsOutn("KEKKA", j, notZero(total._kekkaJisu));
        }

        // 備考
        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();
            if (null == hReportRemarkDat._semester) {
                continue;
            }
            final int semester = Integer.parseInt(hReportRemarkDat._semester); // "計"の欄は表示しない
            final String attendField = getMS932ByteCount(hReportRemarkDat._attendrecRemark) > 30 ? "2" : "1";
            svf.VrsOutn("ATTEND_REMARK" + attendField, semester, hReportRemarkDat._attendrecRemark);
        }
    }
    
    private String spacedName(final String name, final int max0) {
        final int max = max0 / 2; // 2行で1レコード
        final int spaceCount = (max - name.length()) / (name.length() + 1);
        final StringBuffer spacedName = new StringBuffer();
        for (int i = 0; i < name.length(); i++) {
            for (int j = 0; j < spaceCount; j++) {
                spacedName.append("　");
            }
            spacedName.append(name.charAt(i));
        }
        for (int j = 0; j < spaceCount; j++) {
            spacedName.append("　");
        }
        for (int i = 0; i < (max - spacedName.length()) / 2; i++) {
            spacedName.insert(0, "　");
            spacedName.append("　");
        }
        for (int i = 0; i < (max - spacedName.length()); i++) {
            spacedName.append("　");
        }
        return spacedName.toString();
    }
    
    /**
     * 学習の記録を印字する
     * @param svf
     * @param student
     */
    private void printSvfViewRecord(final Vrw32alp svf, final Student student, final List viewClassList) {
        int viewSize1 = 0;
        int viewSize0 = 0;
        for (final Iterator it = viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();
            final int size = viewClass.getViewSize() + viewClass.getViewSize() % 2;
            if ("1".equals(viewClass._electDiv)) {
                if (student.hasChairSubclass(viewClass._subclasscd)) { // 選択教科は講座名簿がある科目のみ表示対象
                    viewSize1 += size;
                }
            } else {
                viewSize0 += size;
            }
        }
        final String grpName = spacedName("必修教科", viewSize0) + spacedName("選択教科", viewSize1);
        
        int line = 0; // 観点の行数
        for (final Iterator it = viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();
            
            if ("1".equals(viewClass._electDiv) && !student.hasChairSubclass(viewClass._subclasscd)) { // 選択教科は講座名簿がある科目のみ表示対象
                continue;
            }
            
            int count = 0;
            for (int i = 0; i < viewClass.getViewSize(); i++) {
                svf.VrsOut("SUBJECTGRPNAME", (line < grpName.length()) ? String.valueOf(grpName.charAt(line)) : " ");
                svf.VrsOut("SUBJECTGRP", viewClass._electDiv);
                svf.VrsOut("CLASSGRP", viewClass._classcd);
                if (getMS932ByteCount(viewClass._subclassname) > 10) {
                    VrsOutRenban(svf, "SUBJECT2_", knjobj.retDividString(viewClass._subclassname, 12, 2));
                } else {
                    svf.VrsOut("SUBJECT1", viewClass._subclassname);
                }
                
                final String viewname = viewClass.getViewName(i);
                final String viewfield = "VIEWNAME" + String.valueOf(i % 2 + 1);
                svf.VrsOut(viewfield, viewname); // 観点名称
                
                final List viewRecordList = student.getViewList(viewClass._subclasscd, viewClass.getViewCd(i));
                for (final Iterator itv = viewRecordList.iterator(); itv.hasNext();) {
                    final ViewRecord viewRecord = (ViewRecord) itv.next();
                    svf.VrsOut("VIEW" + viewRecord._semester + "_" + String.valueOf(i % 2 + 1), viewRecord._status); // 観点
                }
                
                final List viewValuationList = student.getValueList(viewClass._subclasscd);
                for (final Iterator itv = viewValuationList.iterator(); itv.hasNext();) {
                    final ViewValuation viewValuation = (ViewValuation) itv.next();
                    final String value;
                    if ("1".equals(viewClass._electDiv)) {
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
                    svf.VrsOut("RATE" + viewValuation._semester, value); // 評定
                }
                
                if (i % 2 == 1) {
                    line += 1;
                    svf.VrEndRecord();
                }
                count = i % 2;
            }
            if (0 == count) {
                line += 1;
                svf.VrEndRecord();
            }
        }
        
        // 空行挿入
        final int maxLine = 19;
        for (int i = line == maxLine ? maxLine : line % maxLine; i < maxLine; i++) {
            svf.VrsOut("CLASS", String.valueOf(i));
            svf.VrEndRecord();
        }
    }
    
    /**
     * 『特別活動等の記録』『担任からの所見』を印字する
     * @param svf
     * @param student
     */
    private void printSvfReport(final Vrw32alp svf, final Student student) {
        
        final int pcharsrr = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_J, 0);
        final int plinesrr = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_J, 1);
        final int charsrr = (-1 == pcharsrr || -1 == plinesrr) ? 16 : pcharsrr;
        final int linesrr = (-1 == pcharsrr || -1 == plinesrr) ?  7 : plinesrr;
        
        // 担任からの所見
        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();
            final String semester = hReportRemarkDat._semester;
            
            VrsOutnRenban(svf, "OPTION" + semester, knjobj.retDividString(hReportRemarkDat._communication, charsrr * 2, linesrr));
        }
        for (final Iterator it = student._hReportRemarkDetailDatList.iterator(); it.hasNext();) {
            final HReportRemarkDetailDat hReportRemarkDetailDat = (HReportRemarkDetailDat) it.next();
            final String div = hReportRemarkDetailDat._div;
            final String semester = hReportRemarkDetailDat._semester;
            final String code = hReportRemarkDetailDat._code;
            final String remark = hReportRemarkDetailDat._remark1;

            //評価
            if ("01".equals(div)) {
                if ("01".equals(code)) svf.VrsOut("SPECIALACT" + semester + "_1", "1".equals(remark) ? "○" : "");
                if ("02".equals(code)) svf.VrsOut("SPECIALACT" + semester + "_2", "1".equals(remark) ? "○" : "");
                if ("03".equals(code)) svf.VrsOut("SPECIALACT" + semester + "_3", "1".equals(remark) ? "○" : "");
            }

            //部活動
            if ("02".equals(div)) {
                if ("01".equals(code)) VrsOutnRenban(svf, "CLUB1", knjobj.retDividString(remark, 32, 6));
                if ("02".equals(code)) VrsOutnRenban(svf, "CLUB2", knjobj.retDividString(remark, 32, 6));
            }
        }
        if (!_param._semester.equals(_param._knjSchoolMst._semesterDiv)) {
            svf.VrsOut("PARENTSTAMP", "保護者");
        }
    }
    
    /**
     * 『総合的な学習の時間』を印字する
     * @param svf
     * @param student
     */
    private void printSvfReportTotalstudytime(final Vrw32alp svf, final Student student) {
        
        final int pcharstt = getParamSizeNum(_param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J, 0);
        final int plinestt = getParamSizeNum(_param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J, 1);
        final int charstt = (-1 == pcharstt || -1 == plinestt) ? 16 : pcharstt;
        final int linestt = (-1 == pcharstt || -1 == plinestt) ?  7 : plinestt;
        
        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();
            final String semester = hReportRemarkDat._semester;
            
            VrsOutnRenban(svf, "TOTAL_ACT" + semester, knjobj.retDividString(hReportRemarkDat._totalstudytime, charstt * 2, linestt));
        }
        
        final int pcharsrr = getParamSizeNum(_param._reportSpecialSize03_02, 0);
        final int plinesrr = getParamSizeNum(_param._reportSpecialSize03_02, 1);
        final int charsrr = (-1 == pcharsrr || -1 == plinesrr) ? 16 : pcharsrr;
        final int linesrr = (-1 == pcharsrr || -1 == plinesrr) ?  7 : plinesrr;
        
        for (final Iterator it = student._hReportRemarkDetailDatList.iterator(); it.hasNext();) {
            final HReportRemarkDetailDat hReportRemarkDetailDat = (HReportRemarkDetailDat) it.next();
            final String div = hReportRemarkDetailDat._div;
            final String code = hReportRemarkDetailDat._code;
            final String remark = hReportRemarkDetailDat._remark1;

            //総合的な学習の時間(観点)
            if ("03".equals(div)) {
                if ("02".equals(code)) VrsOutnRenban(svf, "VIEW", knjobj.retDividString(remark, charsrr * 2, linesrr));
            }
        }
    }
    
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        List _viewRecordList = Collections.EMPTY_LIST; // 観点
        List _viewValuationList = Collections.EMPTY_LIST; // 評定
        List _attendSemesDatList = Collections.EMPTY_LIST; // 出欠の記録
        //List _attendSubclassDatList = Collections.EMPTY_LIST; // 出欠の記録
        List _behaviorSemesDatList = Collections.EMPTY_LIST; // 学校生活のようす
        List _hReportRemarkDatList = Collections.EMPTY_LIST; // 所見
        List _hReportRemarkDetailDatList = Collections.EMPTY_LIST; // 所見(特別活動)
        MedexamDat _medexamDetDat = null; // 身体の記録（本人）
        List _chairSubclassList = Collections.EMPTY_LIST;
        
        public Student(final String schregno, final String name, final String hrName, final String attendno) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
        }
        
        public void load(final DB2UDB db2, final Param param) {
            _viewRecordList = ViewRecord.getViewRecordList(db2, param, _schregno);
            _viewValuationList = ViewValuation.getViewValuationList(db2, param, _schregno);
            _behaviorSemesDatList = BehaviorSemesDat.getBehaviourSemesDatList(db2, param, _schregno);
            _hReportRemarkDatList = HReportRemarkDat.getHReportRemarkDatList(db2, param, _schregno);
            _hReportRemarkDetailDatList = HReportRemarkDetailDat.getHReportRemarkDetailDatList(db2, param, _schregno);
            _medexamDetDat = MedexamDat.getMedexamDetDat(db2, param, _schregno);
            _chairSubclassList = ChairSubclass.load(db2, param, _schregno);
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
    }
    
    /**
     * 観点の教科
     */
    private static class ViewClass {
        final String _classcd;
        final String _subclasscd;
        final String _subclassname;
        final String _electDiv;
        final List _viewList;
        final List _valuationList;
        ViewClass(
                final String classcd,
                final String subclasscd,
                final String subclassname,
                final String electDiv) {
            _classcd = classcd;
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
                        viewClass = new ViewClass(classcd, subclasscd, subclassname, electDiv);
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
                    final String grade = rs.getString("GRADE");
                    final String viewname = rs.getString("VIEWNAME");
                    final String classcd = rs.getString("CLASSCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String classMstShoworder = rs.getString("CLASS_MST_SHOWORDER");
                    final String showorder = rs.getString("SHOWORDER");
                    
                    final ViewRecord viewRecord = new ViewRecord(semester, viewcd, status, grade, viewname, classcd, subclasscd, classMstShoworder, showorder);
                    
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
            stb.append("         AND T3.SEMESTER <= '" + param._semester + "' ");
            stb.append("         AND T3.SCHREGNO = '" + schregno + "' ");
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
        
        public static List getViewValuationList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewValuationSql(param, schregno);
                ps = db2.prepareStatement(sql);
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
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getViewValuationSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T2.SUBCLASSCD, ");
            }
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     T2.SCORE AS VALUE ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_DAT T2 ");
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
            stb.append("     AND T2.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T2.TESTKINDCD = '99' ");
            stb.append("     AND T2.TESTITEMCD = '00' ");
            stb.append("     AND T2.SCHREGNO = '" + schregno + "' ");
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
                    
                    student._attendSemesDatList = new ArrayList();
                    
                    ps.setString(1, student._schregno);
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
                        
                        student._attendSemesDatList.add(attendSemesDat);
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
    
//    /**
//     * 出欠の記録
//     */
//    private static class AttendSubclassDat {
//        
//        final String _semester;
//        final String _subclasscd;
//        final int _kekka;
//        
//        public AttendSubclassDat(
//                final String semester,
//                final String subclasscd,
//                final int kekka
//        ) {
//            _semester = semester;
//            _subclasscd = subclasscd;
//            _kekka = kekka;
//        }
//        
//        private static void setAttendSubclassDatList(final DB2UDB db2, final Param param, final List studentList) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                param._attendParamMap.put("schregno", "?");
//                final String sql = AttendAccumulate.getAttendSubclassAbsenceSql(
//                        param._year,
//                        param._semester,
//                        null,
//                        param._date,
//                        param._attendParamMap
//                );
//                ps = db2.prepareStatement(sql);
//                
//                for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                    final Student student = (Student) it.next();
//                    
//                    student._attendSubclassDatList = new ArrayList();
//
//                    ps.setString(1, student._schregno);
//                    rs = ps.executeQuery();
//                    while (rs.next()) {
//                        final String semester = rs.getString("SEMESTER");
//                        final String subclasscd = rs.getString("SUBCLASSCD");
//                        final int kekka = rs.getInt("ABSENT_SEM");
//                        
//                        final AttendSubclassDat attendSubclassDat = new AttendSubclassDat(semester, subclasscd, kekka);
//                        student._attendSubclassDatList.add(attendSubclassDat);
//                    }
//                }
//            } catch (SQLException e) {
//                log.error("exception!", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }
//        
//    }
    
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

        public static List getHReportRemarkDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkSql(param, schregno);
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
                    final String attendrecRemark = rs.getString("ATTENDREC_REMARK");
                    
                    final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, totalstudytime, specialactremark, communication, 
                            remark1, remark2, remark3, attendrecRemark);
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
        
        private static String getHReportRemarkSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
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

        public static List getHReportRemarkDetailDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkDetailSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String div = rs.getString("DIV");
                    final String code = rs.getString("CODE");
                    final String remark1 = rs.getString("REMARK1");
                    
                    final HReportRemarkDetailDat hReportRemarkDetailDat = new HReportRemarkDetailDat(semester, div, code, remark1);
                    list.add(hReportRemarkDetailDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getHReportRemarkDetailSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DETAIL_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND T1.DIV = '01' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DETAIL_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '9' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND T1.DIV IN ('02','03') ");
            stb.append(" ORDER BY ");
            stb.append("     DIV, ");
            stb.append("     SEMESTER, ");
            stb.append("     CODE ");
            return stb.toString();
        }
    }
    
    /**
     * 身体の記録
     */
    private static class MedexamDat {
        
        final String _height;
        final String _weight;
        final String _sitheight;
        final String _rBarevision;
        final String _lBarevision;
        final String _rVision;
        final String _lVision;
        final String _rBarevisionMark;
        final String _lBarevisionMark;
        final String _rVisionMark;
        final String _lVisionMark;
        final String _toothOtherdiseasecd;
        final String _remainAdultTooth;
        final String _remainBabyTooth;
        
        public MedexamDat(
                final String height,
                final String weight,
                final String sitheight,
                final String rBarevision,
                final String lBarevision,
                final String rVision,
                final String lVision,
                final String rBarevisionMark,
                final String lBarevisionMark,
                final String rVisionMark,
                final String lVisionMark,
                final String toothOtherdiseasecd,
                final String remainAdultTooth,
                final String remainBabyTooth) {
            _height = height;
            _weight = weight;
            _sitheight = sitheight;
            _rBarevision = rBarevision;
            _lBarevision = lBarevision;
            _rVision = rVision;
            _lVision = lVision;
            _rBarevisionMark = rBarevisionMark;
            _lBarevisionMark = lBarevisionMark;
            _rVisionMark = rVisionMark;
            _lVisionMark = lVisionMark;
            _toothOtherdiseasecd = toothOtherdiseasecd;
            _remainAdultTooth = remainAdultTooth;
            _remainBabyTooth = remainBabyTooth;
        }

        public static MedexamDat getMedexamDetDat(final DB2UDB db2, final Param param, final String schregno) {
            MedexamDat medexamDetDat = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getMedexamDetDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    
                    final String height = rs.getString("HEIGHT");
                    final String weight = rs.getString("WEIGHT");
                    final String sitheight = rs.getString("SITHEIGHT");
                    final String rBarevision = rs.getString("R_BAREVISION");
                    final String lBarevision = rs.getString("L_BAREVISION");
                    final String rVision = rs.getString("R_VISION");
                    final String lVision = rs.getString("L_VISION");
                    final String rBarevisionMark = rs.getString("R_BAREVISION_MARK");
                    final String lBarevisionMark = rs.getString("L_BAREVISION_MARK");
                    final String rVisionMark = rs.getString("R_VISION_MARK");
                    final String lVisionMark = rs.getString("L_VISION_MARK");
                    final String toolthOtherdiseasecd = rs.getString("OTHERDISEASECD");
                    final String remainAdultTooth = rs.getString("REMAINADULTTOOTH");
                    final String remainBabyTooth = rs.getString("REMAINBABYTOOTH");
                    
                    medexamDetDat = new MedexamDat(height, weight, sitheight, rBarevision, lBarevision, rVision, lVision,
                            rBarevisionMark, lBarevisionMark, rVisionMark, lVisionMark, toolthOtherdiseasecd, remainAdultTooth, remainBabyTooth);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return medexamDetDat;
        }
        
        private static String getMedexamDetDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MEDEXAM_DET AS ( ");
            stb.append("   SELECT * FROM MEDEXAM_DET_DAT WHERE YEAR = '" + param._year + "' AND SCHREGNO = '" + schregno + "' ");
            stb.append(" ), MEDEXAM_TOOTH AS ( ");
            stb.append("   SELECT * FROM MEDEXAM_TOOTH_DAT WHERE YEAR = '" + param._year + "' AND SCHREGNO = '" + schregno + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.HEIGHT, ");
            stb.append("     T1.WEIGHT, ");
            stb.append("     T1.SITHEIGHT, ");
            stb.append("     T1.R_BAREVISION, ");
            stb.append("     T1.L_BAREVISION, ");
            stb.append("     T1.R_VISION, ");
            stb.append("     T1.L_VISION, ");
            stb.append("     T1.R_BAREVISION_MARK, ");
            stb.append("     T1.L_BAREVISION_MARK, ");
            stb.append("     T1.R_VISION_MARK, ");
            stb.append("     T1.L_VISION_MARK, ");
            stb.append("     T2.OTHERDISEASECD, ");
            stb.append("     T2.REMAINADULTTOOTH, ");
            stb.append("     T2.REMAINBABYTOOTH ");
            stb.append(" FROM ");
            stb.append("     MEDEXAM_DET T1 ");
            stb.append("     FULL OUTER JOIN MEDEXAM_TOOTH T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     VALUE(T1.YEAR, T2.YEAR) = '" + param._year + "' ");
            stb.append("     AND VALUE(T1.SCHREGNO, T2.SCHREGNO) = '" + schregno + "' ");
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
        public static List load(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
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
                sql.append("     AND T1.SCHREGNO = '" + schregno + "' ");
                sql.append("     AND '" + param._date + "' BETWEEN T1.APPDATE AND T1.APPENDDATE ");
                
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    ChairSubclass cs = getChairSubclass(list, rs.getString("SUBCLASSCD"));
                    if (null == cs) {
                        cs = new ChairSubclass(rs.getString("SUBCLASSCD"));
                        list.add(cs);
                    }
                    cs._chaircdList.add(rs.getString("CHAIRCD"));
                }
                
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
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
    
    /**
     * 表紙(観点の評価について)
     */
    private static class NMD029 {
        final String _namecd2;
        final String _name1;
        final String _name2;
        
        public NMD029(
                final String namecd2,
                final String name1,
                final String name2) {
            _namecd2 = namecd2;
            _name1 = name1;
            _name2 = name2;
        }

        public static List getNMD029List(final DB2UDB db2, final String year) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getNMD029Sql(year);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String namecd2 = rs.getString("NAMECD2");
                    final String name1 = rs.getString("NAME1");
                    final String name2 = rs.getString("NAME2");
                    
                    final NMD029 nmD029 = new NMD029(namecd2, name1, name2);
                    list.add(nmD029);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getNMD029Sql(final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("    T1.NAMECD2, ");
            stb.append("    T1.NAME1, ");
            stb.append("    T1.NAME2 ");
            stb.append(" FROM V_NAME_MST T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + year + "' ");
            stb.append("    AND T1.NAMECD1 = 'D029' ");
            stb.append(" ORDER BY ");
            stb.append("    T1.NAMECD2 ");
            return stb.toString();
        }
    }
    
    /**
     * 表紙(選択科目の評価・評定について)
     */
    private static class NMD001 {
        final String _namecd2;
        final String _name1;
        final String _name2;
        final String _abbv1;
        
        public NMD001(
                final String namecd2,
                final String name1,
                final String name2,
                final String abbv1
        ) {
            _namecd2 = namecd2;
            _name1 = name1;
            _name2 = name2;
            _abbv1 = abbv1;
        }

        public static List getNMD001List(final DB2UDB db2, final String year) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getNMD001Sql(year);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String namecd2 = rs.getString("NAMECD2");
                    final String name1 = rs.getString("NAME1");
                    final String name2 = rs.getString("NAME2");
                    final String abbv1 = rs.getString("ABBV1");
                    
                    final NMD001 nmD001 = new NMD001(namecd2, name1, name2, abbv1);
                    list.add(nmD001);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getNMD001Sql(final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("    T1.NAMECD2, ");
            stb.append("    T1.NAME1, ");
            stb.append("    T1.NAME2, ");
            stb.append("    T1.ABBV1 ");
            stb.append(" FROM V_NAME_MST T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + year + "' ");
            stb.append("    AND T1.NAMECD1 = 'D001' ");
            stb.append(" ORDER BY ");
            stb.append("    T1.NAMECD2 ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
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
        final String _trCd1;
        final boolean _isPrintHyoshi;
        final boolean _isPrintSeiseki;
        final boolean _isPrintSyoken;
        final boolean _isPrintSyuryo;
        final String _documentRoot;
        final String _imagePath;
        final String _extension;
        final String _descDate;
        final Set _notPrintAttendanceSemesterSet;
        
        final String _gradeCdStr;
        final String _certifSchoolSchoolName;
        final String _certifSchoolRemark3;
        final String _certifSchoolPrincipalName;
        final String _tr1Name;
        final String _hyosiHrName;
        final String _certifSchoolJobName;
        final String _d016Namespare1;
        
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final Map _attendParamMap;
        
        final String _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J;
        final String _HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_J;
        final String _reportSpecialSize03_02;
        final String _reportSpecialSize03_01;
       
       /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;
        
        final List _nmD029List;
        final List _nmD001List;
        
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
            _descDate = request.getParameter("DESC_DATE");
            
            _notPrintAttendanceSemesterSet = new HashSet();
            if (null != request.getParameter("CHECKS")) {
                final String[] checks = StringUtils.split(request.getParameter("CHECKS"), ",");
                for (int semesi = 0; semesi < checks.length; semesi++) {
                    if (!StringUtils.isBlank(checks[semesi])) {
                        _notPrintAttendanceSemesterSet.add(String.valueOf(checks[semesi])); 
                    }
                }
            }
            log.debug(" not print attendance semester = " + _notPrintAttendanceSemesterSet);
            
            _gradeCdStr = getGradeCd(db2, _grade);
            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolRemark3 = getCertifSchoolDat(db2, "REMARK3");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _tr1Name = getStaffname(db2, _trCd1);
            _hyosiHrName = "第" + hankakuToZenkaku(_gradeCdStr) + "学年 " + getHrClassName1(db2, _year, _semester, _gradeHrclass) + "組";
            _certifSchoolJobName = getCertifSchoolDat(db2, "JOB_NAME");
            _d016Namespare1 = getNameMst(db2, _year, "D016", "01", "NAMESPARE1");
            
            _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J = null == request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J"), "+", " "); // 総合的な学習の時間
            _HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J = null == request.getParameter("HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"), "+", " "); // 出欠の記録備考
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_J = null == request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"), "+", " "); // 担任からの所見
            _reportSpecialSize03_01 = null == request.getParameter("reportSpecialSize03_01") ? "" : StringUtils.replace(request.getParameter("reportSpecialSize03_01"), "+", " "); // 身体の記録
            _reportSpecialSize03_02 = null == request.getParameter("reportSpecialSize03_02") ? "" : StringUtils.replace(request.getParameter("reportSpecialSize03_02"), "+", " "); // 観点
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            
            //表紙
            _nmD029List = NMD029.getNMD029List(db2, _year);
            _nmD001List = NMD001.getNMD001List(db2, _year);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
         
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
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
                rtn = "";
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
                stb.append("     T1.SCHOOL_KIND = 'J' ");
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


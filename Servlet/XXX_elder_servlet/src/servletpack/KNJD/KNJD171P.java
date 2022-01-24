/*
 * $Id: 7cbb628dd791d4bfa0d9fe1266969a3b87aaa506 $
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

public class KNJD171P {

    private static final Log log = LogFactory.getLog(KNJD171P.class);

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
        
        final List studentList = getStudentList(db2);
        if (_param._isPrintAttendance) {
            AttendSemesDat.getAttendSemesDatList(db2, _param, studentList);
        }

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug(" student = " + student._schregno + " : " + student._name);
            student.load(db2, _param);
            if (_param._isPrintHyoshi) {
                // 表紙
                printSvfHyoshi(db2, svf, student);
                _hasData = true;
            } 
            if (_param._isPrintSeiseki) {
                // 学習のようす等
                printSvfMainSeiseki(db2, svf, student, viewClassList);
            }
            if (_param._isPrintSyoken) {
                // 所見等
                printSvfMainSyoken(db2, svf, student);
            }
            if (_param._isPrintSyuryo) {
                // 修了証
                printSvfSyuryo(db2, svf, student);
                _hasData = true;
            } 
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
    
    private void VrsOutnRenban(final Vrw32alp svf, final String field, final List list) {
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

    /**
     * 表紙を印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printSvfHyoshi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final int gradeCd = Integer.parseInt(_param._gradeCdStr);
        final String form;
        if ("kumamoto".equals(_param._z010Name1)) {
            if (1 == gradeCd || 2 == gradeCd) {
                form = "KNJD171P_1_1.frm";
            } else {
                form = "KNJD171P_1_3.frm";
            }
        } else {
            if (1 == gradeCd || 2 == gradeCd) {
                form = "KNJD171P_1_1P.frm";
            } else {
                form = "KNJD171P_1_3P.frm";
            }
        }
        svf.VrSetForm(form, 1);
        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)): student._attendno;
        svf.VrsOut("ATTENDNO", attendno);
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
        if (getMS932ByteCount(student._name) > 20) {
            svf.VrsOut("NAME2", student._name);
        } else {
            svf.VrsOut("NAME", student._name);
        }
        svf.VrsOut("STAFFNAME1_" + (getMS932ByteCount(trimLeft(_param._certifSchoolPrincipalName)) > 20 ? "2" : "1"), trimLeft(_param._certifSchoolPrincipalName));
        svf.VrsOut("STAFFNAME2_" + (getMS932ByteCount(_param._tr1Name) > 20 ? "2" : "1"), _param._tr1Name);
        svf.VrEndPage();
    }
    
    /**
     * 修了証を印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printSvfSyuryo(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD171P_4_1.frm", 1);
        svf.VrsOut("BLANK", _param._semester);
        if (getMS932ByteCount(student._name) > 20) {
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
        if (NumberUtils.isNumber(_param._gradeCdStr)) {
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
    private void printSvfMainSeiseki(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Student student,
            final List viewClassList) {
        
        if (!NumberUtils.isNumber(_param._gradeCdStr)) {
            return;
        }
        final String form;
        final int maxLine;
        final int gradeCd = Integer.parseInt(_param._gradeCdStr);
        if (1 == gradeCd || 2 == gradeCd) {
            form = "KNJD171P_2_1.frm";
            maxLine = 23;
        } else if (3 == gradeCd || 4 == gradeCd) {
            form = "KNJD171P_2_3.frm";
            maxLine = 29;
        } else if (5 == gradeCd || 6 == gradeCd) {
            form = "KNJD171P_2_5.frm";
            maxLine = 33;
        } else {
            return;
        }
        svf.VrSetForm(form, 4);
        
        printSvfStudent(svf, student);
        
        printSvfReportCommunication(svf, student);
        
        printSvfViewRecord(svf, viewClassList, maxLine, student);
        
        svf.VrEndPage();
        _hasData = true;
    }
    
    /**
     * 所見を印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printSvfMainSyoken(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Student student) {
        
        if (!NumberUtils.isNumber(_param._gradeCdStr)) {
            return;
        }
        final String form;
        final int gradeCd = Integer.parseInt(_param._gradeCdStr);
        if (1 == gradeCd || 2 == gradeCd) {
            form = (_param._semester.equals(_param._knjSchoolMst._semesterDiv)) ? "KNJD171P_3_1_2.frm" : "KNJD171P_3_1_1.frm";
        } else if (3 == gradeCd) {
            form = (_param._semester.equals(_param._knjSchoolMst._semesterDiv)) ? "KNJD171P_3_3_2.frm" : "KNJD171P_3_3_1.frm";
        } else if (4 == gradeCd) {
            form = (_param._semester.equals(_param._knjSchoolMst._semesterDiv)) ? "KNJD171P_3_4_2.frm" : "KNJD171P_3_4_1.frm";
        } else if (5 == gradeCd || 6 == gradeCd) {
            form = (_param._semester.equals(_param._knjSchoolMst._semesterDiv)) ? "KNJD171P_3_5_2.frm" : "KNJD171P_3_5_1.frm";
        } else {
            return;
        }
        svf.VrSetForm(form, 1);
        
        printSvfStudent(svf, student);
        
        printSvfReport(svf, student);
        
        printSvfBehavior(svf, student);
        
        printSvfMedexam(svf, student);
        
        printSvfAttendSemes(svf, student);
        
        svf.VrEndPage();
        _hasData = true;
    }
    
    private void printSvfStudent(final Vrw32alp svf, final Student student) {
        svf.VrsOut("NAME", student._name);
        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)): student._attendno;
        svf.VrsOut("ATTENDNO", attendno);
    }
    
    /**
     * 『身体の記録』を印字する
     * @param svf
     * @param student
     */
    private void printSvfMedexam(final Vrw32alp svf, final Student student) {
        if (null != student._medexamDetDat) {
            svf.VrsOut("HEIGHT", student._medexamDetDat._height);
            svf.VrsOut("WEIGHT", student._medexamDetDat._weight);
            svf.VrsOut("SIT_HEIGHT", student._medexamDetDat._sitheight);
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
        if (_param._isPrintAttendance) {
            for (final Iterator it = student._attendSemesDatList.iterator(); it.hasNext();) {
                final AttendSemesDat attendSemesDat = (AttendSemesDat) it.next();
                if (!NumberUtils.isDigits(attendSemesDat._semester)) {
                    continue;
                }
                
                final int j;
                if ("9".equals(attendSemesDat._semester)) {
                    j = 4; // 3学期制の場合
                } else {
                    j = Integer.parseInt(attendSemesDat._semester);
                }
                svf.VrsOutn("LESSON", j, notZero(attendSemesDat._lesson));
                svf.VrsOutn("SUSPEND", j, notZero(attendSemesDat._suspend + attendSemesDat._mourning + attendSemesDat._virus + attendSemesDat._koudome));
                svf.VrsOutn("PRESENT", j, notZero(attendSemesDat._mlesson));
                svf.VrsOutn("ATTEND", j, notZero(attendSemesDat._sick));
                svf.VrsOutn("ABSENCE", j, notZero(attendSemesDat._present));
            }
        }
        
        final int pchars = getParamSizeNum(_param._HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P, 0);
        final int plines = getParamSizeNum(_param._HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P, 1);
        final int chars = (-1 == pchars || -1 == plines) ? 15 : pchars;
        final int lines = (-1 == pchars || -1 == plines) ? 2 : plines;
        
        for (final Iterator it = student._hReportRemarkPdatList.iterator(); it.hasNext();) {
            final HReportRemarkPdat hReportRemarkPdat = (HReportRemarkPdat) it.next();
            
            if (getMS932ByteCount(hReportRemarkPdat._attendrecRemark) <= 15 * 2) {
                svf.VrsOutn("ATTEND_REMARK", Integer.parseInt(hReportRemarkPdat._semester), hReportRemarkPdat._attendrecRemark);
                
            } else {
                final List list = knjobj.retDividString(hReportRemarkPdat._attendrecRemark, chars * 2, lines);
                if (null != list) {
                    for (int i = 0 ; i < list.size(); i++) {
                        svf.VrsOutn("ATTEND_REMARK2_" + (i + 1), Integer.parseInt(hReportRemarkPdat._semester), (String) list.get(i));
                    }
                }
            }
        }
        
        if (!_param._semester.equals(_param._knjSchoolMst._semesterDiv)) {
            svf.VrsOut("PARENTSTAMP", "保護者");
        }
    }
    
    /**
     * 学習の記録を印字する
     * @param svf
     * @param student
     */
    private void printSvfViewRecord(final Vrw32alp svf, final List viewClassList, final int maxLine, final Student student) {
        
        int column = 0; // 評定のインデックス
        for (final Iterator it = viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();
            final List valueList0 = student.getValueListWithClasscd(viewClass._classcd);
            if (valueList0.isEmpty()) {
                column += 1;
                final String field = "CLASSNAME2_" + String.valueOf(column) + "_" + (getMS932ByteCount(viewClass._subclassname) > 6 ? "2" : "1");
                svf.VrsOut(field, viewClass._subclassname);
            } else {
                
                for (final Iterator itsub = student.getValueSubclassCdList(valueList0).iterator(); itsub.hasNext();) {
                    final String subclasscd = (String) itsub.next();
                    column += 1;
                    
                    for (final Iterator itv = student.getValueListWithSubclasscd(subclasscd).iterator(); itv.hasNext();) {
                        final ViewValuation viewValuation = (ViewValuation) itv.next();
                        final String field = "CLASSNAME2_" + String.valueOf(column) + "_" + (getMS932ByteCount(viewClass._subclassname) > 6 ? "2" : "1");
                        svf.VrsOut(field, viewClass._subclassname);
                        svf.VrsOutn("RATE" + String.valueOf(column), Integer.parseInt(viewValuation._semester), viewValuation._value); // 評定
                    }
                }
            }
        }
        
        int line = 0; // 観点の行数
        for (final Iterator it = viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();
            final List classnameCharList = viewClass.getSubclassnameCharacterList();
            
            for (int i = 0; i < classnameCharList.size(); i++) {
                final String classnameChar = (String) classnameCharList.get(i);
                final String sfx = (i == 0) ? "" : "_2";
                
                svf.VrsOut("CLASS" + sfx, viewClass._classcd);
                svf.VrsOut("CLASSNAME1" + sfx, classnameChar);
                
                if (i < viewClass.getViewSize()) {
                    final String viewname = viewClass.getViewName(i);
                    svf.VrsOut("VIEWNAME" + (getMS932ByteCount(viewname) > 84 ? "2" : "1") + sfx, viewname); // 観点名称
                    
                    final List viewRecordList = student.getViewList(viewClass.getViewCd(i));
                    for (final Iterator itv = viewRecordList.iterator(); itv.hasNext();) {
                        final ViewRecord viewRecord = (ViewRecord) itv.next();
                        final String s;
                        if ("A".equals(viewRecord._status)) {
                            s = "◎";
                        } else if ("B".equals(viewRecord._status)) {
                            s = "○";
                        } else if ("C".equals(viewRecord._status)) {
                            s = "△";
                        } else {
                            s = "";
                        }
                        svf.VrsOut("VIEW" + viewRecord._semester + sfx, s); // 観点
                    }
                }
                
                line += 1;
                svf.VrEndRecord();
            }
        }
        
        // 空行挿入
        for (int i = 0; i < maxLine - (line == 0 ? 0 : line % maxLine == 0 ? maxLine : line % maxLine); i++) {
            final String sfx = (i == 0) ? "" : "_3";
            svf.VrsOut("CLASS" + sfx, String.valueOf(i));
            svf.VrEndRecord();
        }
    }
    
    /**
     * 『総合的な学習の時間』『特別活動等の記録』を印字する
     * @param svf
     * @param student
     */
    private void printSvfReport(final Vrw32alp svf, final Student student) {
        for (final Iterator it = student._hReportRemarkPdatList.iterator(); it.hasNext();) {
            final HReportRemarkPdat hReportRemarkPdat = (HReportRemarkPdat) it.next();
            final String semester = hReportRemarkPdat._semester;
            if ("9".equals(semester)) {
                continue;
            }
            
            final int pcharssp = getParamSizeNum(_param._HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P, 0);
            final int plinessp = getParamSizeNum(_param._HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P, 1);
            final int charssp = (-1 == pcharssp || -1 == plinessp) ? 16 : pcharssp;
            final int linessp = (-1 == pcharssp || -1 == plinessp) ?  4 : plinessp;

            // 所見
            VrsOutnRenban(svf, "OPTION" + semester, knjobj.retDividString(hReportRemarkPdat._specialactremark, charssp * 2, linessp));
            
            final int pcharsfl = getParamSizeNum(_param._HREPORTREMARK_DAT_FOREIGNLANGACT_SIZE_P, 0);
            final int plinesfl = getParamSizeNum(_param._HREPORTREMARK_DAT_FOREIGNLANGACT_SIZE_P, 1);
            final int charsfl = (-1 == pcharsfl || -1 == plinesfl) ? 16 : pcharsfl;
            final int linesfl = (-1 == pcharsfl || -1 == plinesfl) ?  4 : plinesfl;
            
            // 外国語活動
            VrsOutnRenban(svf, "FOREIGN_ACT" + semester, knjobj.retDividString(hReportRemarkPdat._foreignLangAct, charsfl * 2, linesfl));
            
            final int pcharstotal = getParamSizeNum(_param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P, 0);
            final int plinestotal = getParamSizeNum(_param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P, 1);
            final int charstotal = (-1 == pcharstotal || -1 == plinestotal) ? 16 : pcharstotal;
            final int linestotal = (-1 == pcharstotal || -1 == plinestotal) ?  4 : plinestotal;

            // 総合的な学習の時間
            VrsOutnRenban(svf, "FIELD" + String.valueOf(7 + Integer.parseInt(semester)), knjobj.retDividString(hReportRemarkPdat._totalstudytime, charstotal * 2, linestotal));
        }
        
        // クラブ活動
        if (0 != student._clubHistDatList.size()) {
            if (1 == student._clubHistDatList.size()) {
                final ClubHistDat clubHistDat = (ClubHistDat) student._clubHistDatList.get(0);
                svf.VrsOut("CLUB1", clubHistDat._clubname);
            } else {
                int c = 0;
                for (Iterator it = student._clubHistDatList.iterator(); it.hasNext() && c < 2; c += 1) {
                    final ClubHistDat clubHistDat = (ClubHistDat) it.next();
                    svf.VrsOut("CLUB2_" + (c + 1), clubHistDat._clubname);
                }
            }
        }
        
        // 委員会活動
        if (0 != student._committeeHistDatList.size()) {
            if (1 == student._committeeHistDatList.size()) {
                final CommiteeHistDat committeeHistDat = (CommiteeHistDat) student._committeeHistDatList.get(0);
                svf.VrsOut("COMMITEE1", committeeHistDat._committeename);
            } else {
                int c = 0;
                for (Iterator it = student._committeeHistDatList.iterator(); it.hasNext() && c < 2; c += 1) {
                    final CommiteeHistDat committeeHistDat = (CommiteeHistDat) it.next();
                    svf.VrsOut("COMMITEE2_" + (c + 1), committeeHistDat._committeename);
                }
            }
        }
    }
    
    /**
     * 『担任からの所見』を印字する
     * @param svf
     * @param student
     */
    private void printSvfReportCommunication(final Vrw32alp svf, final Student student) {
        final int pchars = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_P, 0);
        final int plines = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_P, 1);
        final int chars = (-1 == pchars || -1 == plines) ? 16 : pchars;
        final int lines = (-1 == pchars || -1 == plines) ? 6  : plines;
        
        for (final Iterator it = student._hReportRemarkPdatList.iterator(); it.hasNext();) {
            final HReportRemarkPdat hReportRemarkPdat = (HReportRemarkPdat) it.next();
            final String semester = hReportRemarkPdat._semester;
            if ("9".equals(semester)) {
                continue;
            }
            VrsOutnRenban(svf, "TOTAL_ACT" + semester, knjobj.retDividString(hReportRemarkPdat._communication, chars * 2 , lines));
        }
    }
    
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        List _viewRecordList = Collections.EMPTY_LIST;
        List _viewValuationList = Collections.EMPTY_LIST; // 評定
        List _attendSemesDatList = Collections.EMPTY_LIST; // 出欠のようす
        List _behaviorSemesDatList = Collections.EMPTY_LIST; // 生活・特別活動のようす
        List _hReportRemarkPdatList = Collections.EMPTY_LIST; // 所見
        MedexamDetDat _medexamDetDat = null; // 身体・運動のようす
        List _clubHistDatList = Collections.EMPTY_LIST; // クラブ
        List _committeeHistDatList = Collections.EMPTY_LIST; // 委員会
        
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
            _hReportRemarkPdatList = HReportRemarkPdat.getHReportRemarkPdatList(db2, param, _schregno);
            _medexamDetDat = MedexamDetDat.getMedexamDetDat(db2, param, _schregno);
            _clubHistDatList = ClubHistDat.getClubHistDatList(db2, param, _schregno);
            _committeeHistDatList = CommiteeHistDat.getCommitteeHistDatList(db2, param, _schregno);
        }
        
        /**
         * 評定のリストの科目コードリストを得る
         * @return 評定のリスト
         * @return 評定の科目コードリスト
         */
        public List getValueSubclassCdList(final List viewValuationList) {
            final List rtn = new ArrayList();
            String subclasscdOld = "";
            for (Iterator it = viewValuationList.iterator(); it.hasNext();) {
                final ViewValuation viewValuation = (ViewValuation) it.next();
                if (null != viewValuation._subclasscd && !subclasscdOld.equals(viewValuation._subclasscd)) {
                    rtn.add(viewValuation._subclasscd);
                    subclasscdOld = viewValuation._subclasscd;
                }
            }
            return rtn;
        }
        
        /**
         * 評定のリストを得る
         * @param subclasscd 指定の科目コード
         * @return 評定のリスト
         */
        public List getValueListWithSubclasscd(final String subclasscd) {
            final List rtn = new ArrayList();
            if (null != subclasscd) {
                for (Iterator it = _viewValuationList.iterator(); it.hasNext();) {
                    final ViewValuation viewValuation = (ViewValuation) it.next();
                    if (null != viewValuation._subclasscd && subclasscd.equals(viewValuation._subclasscd)) {
                        rtn.add(viewValuation);
                    }
                }
            }
            return rtn;
        }
        
        /**
         * 評定のリストを得る
         * @param subclasscd 指定の科目コード
         * @return 評定のリスト
         */
        public List getValueListWithClasscd(final String classcd) {
            final List rtn = new ArrayList();
            if (null != classcd) {
                for (Iterator it = _viewValuationList.iterator(); it.hasNext();) {
                    final ViewValuation viewValuation = (ViewValuation) it.next();
                    if (null != viewValuation._classcd && classcd.equals(viewValuation._classcd)) {
                        rtn.add(viewValuation);
                    }
                }
            }
            return rtn;
        }
        
        /**
         * 観点コードの観点のリストを得る
         * @param viewcd 観点コード 
         * @return 観点コードの観点のリスト
         */
        public List getViewList(final String viewcd) {
            final List rtn = new ArrayList();
            if (null != viewcd) {
                for (Iterator it = _viewRecordList.iterator(); it.hasNext();) {
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
        final String _classcd;
        final String _subclassname;
        final List _viewList;
        final List _valuationList;
        ViewClass(
                final String classcd,
                final String subclassname) {
            _classcd = classcd;
            _subclassname = subclassname;
            _viewList = new ArrayList();
            _valuationList = new ArrayList();
        }
        
        public void addView(final String viewcd, final String viewname) {
            _viewList.add(new String[]{viewcd, viewname});
        }
        
        public List getSubclassnameCharacterList() {
            if (null == _subclassname || "".equals(_subclassname)) {
                return Collections.EMPTY_LIST;
            }
            List rtn = new ArrayList();
            if (_subclassname.length() >= getViewSize()) {
                for (int i = 0; i < _subclassname.length(); i++) {
                    rtn.add(String.valueOf(_subclassname.charAt(i)));
                }
            } else {
                final int st = 0; // (getViewSize() / 2 + getViewSize() % 2) - (_subclassname.length() / 2 + _subclassname.length() % 2); // センタリング
                for (int i = 0; i < st; i++) {
                    rtn.add("");
                }
                for (int i = st, ci = 0; i < st + _subclassname.length(); i++, ci++) {
                    rtn.add(String.valueOf(_subclassname.charAt(ci)));
                }
                for (int i = st + _subclassname.length(); i < getViewSize(); i++) {
                    rtn.add("");
                }
            }
            return rtn;
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
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String classcd = rs.getString("CLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");
                    
                    ViewClass viewClass = null;
                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final ViewClass viewClass0 = (ViewClass) it.next();
                        if (viewClass0._classcd.equals(classcd)) {
                            viewClass = viewClass0;
                            break;
                        }
                    }
                    
                    if (null == viewClass) {
                        viewClass = new ViewClass(classcd, subclassname);
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
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     T1.VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            } else {
                stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
            }
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T3.SHOWORDER3, -1), ");
            stb.append("     T3.CLASSCD, ");
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
        final String _grade;
        final String _viewname;
        final String _classcd;
        final String _classMstShoworder;
        final String _showorder;
        ViewRecord(
                final String semester,
                final String viewcd,
                final String status,
                final String grade,
                final String viewname,
                final String classcd,
                final String classMstShoworder,
                final String showorder) {
            _semester = semester;
            _viewcd = viewcd;
            _status = status;
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
                    final String grade = rs.getString("GRADE");
                    final String viewname = rs.getString("VIEWNAME");
                    final String classcd = rs.getString("CLASSCD");
                    final String classMstShoworder = rs.getString("CLASS_MST_SHOWORDER");
                    final String showorder = rs.getString("SHOWORDER");
                    
                    final ViewRecord viewRecord = new ViewRecord(semester, viewcd, status, grade, viewname, classcd, classMstShoworder, showorder);
                    
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
            stb.append("     LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
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
                stb.append("     AND T4.CLASSCD = T2.CLASSCD ");
                stb.append("     AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("     AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN CLASS_MST T5 ON T5.CLASSCD = SUBSTR(T2.SUBCLASSCD, 1, 2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T5.SCHOOL_KIND = T2.SCHOOL_KIND ");
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
                stb.append("             AND L1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("             AND L1.ATTEND_CLASSCD = T2.CLASSCD ");
                    stb.append("             AND L1.ATTEND_SCHOOL_KIND = T2.SCHOOL_KIND ");
                    stb.append("             AND L1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD ");
                }
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
        final int _virus;
        final int _koudome;
        
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
                final int offdays,
                final int virus,
                final int koudome
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
            _virus = virus;
            _koudome = koudome;
        }
        
        private static void getAttendSemesDatList(final DB2UDB db2, final Param param, final List studentList) {
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
                        final int virus = rs.getInt("VIRUS");
                        final int koudome = rs.getInt("KOUDOME");
                        
                        final AttendSemesDat attendSemesDat = new AttendSemesDat(semester, lesson, suspend, mourning, mlesson, sick, absent, present, late, early, transferDate, offdays, virus, koudome);
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
    private static class HReportRemarkPdat {
        final String _semester;
        final String _totalstudytime;     // 総合的な学習の時間
        final String _specialactremark;   // 特別活動の記録・その他
        final String _communication;      // 担任からの所見
        final String _remark1;            // 特別活動の記録・学級活動
        final String _remark2;            // 特別活動の記録・生徒会活動
        final String _remark3;            // 特別活動の記録・学校行事
        final String _foreignLangAct;     // 外国語活動
        final String _attendrecRemark;    // 出欠備考
        
        public HReportRemarkPdat(
                final String semester,
                final String totalstudytime,
                final String specialactremark,
                final String communication,
                final String remark1,
                final String remark2,
                final String remark3,
                final String foreignLangAct,
                final String attendrecRemark) {
            _semester = semester;
            _totalstudytime = totalstudytime;
            _specialactremark = specialactremark;
            _communication = communication;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _foreignLangAct = foreignLangAct;
            _attendrecRemark = attendrecRemark;
        }

        public static List getHReportRemarkPdatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkPdatSql(param, schregno);
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
                    final String foreignLangAct = rs.getString("FOREIGNLANGACT");
                    final String attendrecRemark = rs.getString("ATTENDREC_REMARK");
                    final HReportRemarkPdat hReportRemarkPdat = new HReportRemarkPdat(semester, totalstudytime, specialactremark, communication, remark1, remark2, remark3, foreignLangAct, attendrecRemark);
                    list.add(hReportRemarkPdat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getHReportRemarkPdatSql(final Param param, final String schregno) {
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
     * 身体の記録
     */
    private static class MedexamDetDat {
        
        final String _height;
        final String _weight;
        final String _sitheight;
        final String _chest;
        final String _rBarevision;
        final String _rBarevisionMark;
        final String _lBarevision;
        final String _lBarevisionMark;
        final String _rVision;
        final String _rVisionMark;
        final String _lVision;
        final String _lVisionMark;
        
        public MedexamDetDat(
                final String height,
                final String weight,
                final String sitheight,
                final String chest,
                final String rBarevision,
                final String rBarevisionMark,
                final String lBarevision,
                final String lBarevisionMark,
                final String rVision,
                final String rVisionMark,
                final String lVision,
                final String lVisionMark) {
            _height = height;
            _weight = weight;
            _sitheight = sitheight;
            _chest = chest;
            _rBarevision = rBarevision;
            _rBarevisionMark = rBarevisionMark;
            _lBarevision = lBarevision;
            _lBarevisionMark = lBarevisionMark;
            _rVision = rVision;
            _rVisionMark = rVisionMark;
            _lVision = lVision;
            _lVisionMark = lVisionMark;
        }

        public static MedexamDetDat getMedexamDetDat(final DB2UDB db2, final Param param, final String schregno) {
            MedexamDetDat medexamDetDat = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getMedexamDetDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String height = rs.getString("HEIGHT");
                    final String weight = rs.getString("WEIGHT");
                    final String sitheight = rs.getString("SITHEIGHT");
                    final String chest = rs.getString("CHEST");
                    final String rBarevision = rs.getString("R_BAREVISION");
                    final String rBarevisionMark = rs.getString("R_BAREVISION_MARK");
                    final String lBarevision = rs.getString("L_BAREVISION");
                    final String lBarevisionMark = rs.getString("L_BAREVISION_MARK");
                    final String rVision = rs.getString("R_VISION");
                    final String rVisionMark = rs.getString("R_VISION_MARK");
                    final String lVision = rs.getString("L_VISION");
                    final String lVisionMark = rs.getString("L_VISION_MARK");
                    
                    medexamDetDat = new MedexamDetDat(height, weight, sitheight, chest, 
                            rBarevision, rBarevisionMark, lBarevision, lBarevisionMark, rVision, rVisionMark, lVision, lVisionMark);
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
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     MEDEXAM_DET_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            return stb.toString();
        }
    }
    
    private static class ClubHistDat {
        
        final String _clubcd;
        final String _clubname;
        ClubHistDat(
                final String clubcd,
                final String clubname) {
            _clubcd = clubcd;
            _clubname = clubname;
        }
        
        public static List getClubHistDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getClubHistDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String clubcd = rs.getString("CLUBCD");
                    final String clubname = rs.getString("CLUBNAME");
                    
                    final ClubHistDat clubHistDat = new ClubHistDat(clubcd, clubname);
                    list.add(clubHistDat);
                    
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getClubHistDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CLUBCD, ");
            stb.append("     T2.CLUBNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_CLUB_HIST_DAT T1 ");
            stb.append("     INNER JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ");
            stb.append(" WHERE ");
            stb.append("     FISCALYEAR(T1.SDATE) = '" + param._year + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND T1.EDATE IS NULL ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLUBCD ");
            return stb.toString();
        }
    }
    
    private static class CommiteeHistDat {
        
        final String _committeeFlg;
        final String _committeecd;
        final String _committeename;
        CommiteeHistDat(
                final String committeeFlg,
                final String committeecd,
                final String committeename) {
            _committeeFlg = committeeFlg;
            _committeecd = committeecd;
            _committeename = committeename;
        }
        
        public static List getCommitteeHistDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getCommiteeHistDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String committeeFlg = rs.getString("COMMITTEE_FLG");
                    final String committeecd = rs.getString("COMMITTEECD");
                    final String committeename = rs.getString("COMMITTEENAME");
                    
                    final CommiteeHistDat commiteeHistDat = new CommiteeHistDat(committeeFlg, committeecd, committeename);
                    list.add(commiteeHistDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getCommiteeHistDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("    T1.COMMITTEE_FLG, ");
            stb.append("    T1.COMMITTEECD, ");
            stb.append("    T2.COMMITTEENAME ");
            stb.append(" FROM ");
            stb.append("    SCHREG_COMMITTEE_HIST_DAT T1 ");
            stb.append("    INNER JOIN COMMITTEE_MST T2 ON T2.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
            stb.append("        AND T2.COMMITTEECD = T1.COMMITTEECD ");
            stb.append(" WHERE ");
            stb.append("    T1.SCHREGNO = '" + schregno + "' ");
            stb.append("    AND T1.YEAR = '" + param._year + "' ");
            stb.append("    AND T1.SEMESTER = '9' ");
            stb.append("    AND T1.COMMITTEE_FLG IS NOT NULL ");
            stb.append("    AND T1.COMMITTEECD IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("    T1.SEQ ");
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
        final boolean _isPrintAttendance;
        
        final String _certifSchoolSchoolName;
        final String _certifSchoolRemark3;
        final String _certifSchoolPrincipalName;
        final String _tr1Name;
        final String _hyosiHrName;
        final String _certifSchoolJobName;
        final String _d016Namespare1;
        
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;

        final String _HREPORTREMARK_DAT_FOREIGNLANGACT_SIZE_P;
        final String _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P;
        final String _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_P;
        final String _HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P;
       
       /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;
        
        final String _gradeCdStr;
        
        String _z010Name1;
        
        final Map _attendParamMap = new HashMap();
        
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
            _isPrintAttendance = !"on".equals(request.getParameter("CHECK"));
            
            _gradeCdStr = getGradeCd(db2, _grade);
            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolRemark3 = getCertifSchoolDat(db2, "REMARK3");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _tr1Name = getStaffname(db2, _trCd1);
            _hyosiHrName = "第" + hankakuToZenkaku(_gradeCdStr) + "学年 " + getHrClassName1(db2, _year, _semester, _gradeHrclass) + "組";
            _certifSchoolJobName = getCertifSchoolDat(db2, "JOB_NAME");
            _d016Namespare1 = getNameMst(db2, _year, "D016", "01", "NAMESPARE1");
            
            _HREPORTREMARK_DAT_FOREIGNLANGACT_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_FOREIGNLANGACT_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_FOREIGNLANGACT_SIZE_P"), "+", " ");
            _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P"), "+", " ");
            _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P"), "+", " ");
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"), "+", " ");
            _HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P = null == request.getParameter("HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P"), "+", " ");
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            
            _z010Name1 = setZ010Name1(db2);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            
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
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
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


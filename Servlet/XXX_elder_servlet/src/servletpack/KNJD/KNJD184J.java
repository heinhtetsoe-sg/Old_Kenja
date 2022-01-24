/*
 * $Id: fb98b72ebbd655c98bd65de7008c1d00cad95712 $
 *
 * 作成日: 2012/02/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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

public class KNJD184J {

    private static final Log log = LogFactory.getLog(KNJD184J.class);

    private boolean _hasData;
    
    private static final String SUBCLASSALL = "999999";

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
        
        final List studentList = getStudentList(db2);
        if (_param._isPrintSeiseki) {
            AttendSemesDat.setAttendSemesDatList(db2, _param, studentList);
        }
        
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug(" student = " + student._schregno + " : " + student._name);
            student.load(db2, _param);
            if (_param._isPrintHyoshi) {
                // 表紙
                printSvfHyoshi(db2, svf, student);
            } 
            if (_param._isPrintSeiseki) {
                // 学習のようす等
                printSvfMainSeiseki(db2, svf, student);
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
                final String gradeCourse = rs.getString("COURSE");
                final Student student = new Student(schregno, name, hrName, attendno, gradeCourse);
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
        stb.append("     T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SEMESTER ");
        stb.append("  FROM    SCHREG_REGD_DAT T1 ");
        stb.append("          , SEMESTER_MST T2 ");
        stb.append("  WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '"+ param._semester +"' ");
        stb.append("     AND T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
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
        stb.append("    T1.COURSE, ");
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
        svf.VrSetForm("KNJD184J_1.frm", 1);

        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)): student._attendno;
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "(" + _param._year + ")年度");
        svf.VrsOut("SCHOOLNAME", _param._certifSchoolSchoolName);
        svf.VrsOut("HR_NAME", _param._hyosiHrName + " " + attendno + "番");
        if (getMS932ByteCount(student._name) > 20) {
            svf.VrsOut("NAME2", student._name);
        } else {
            svf.VrsOut("NAME", student._name);
        }
        
        if (_param._semester.equals(_param._knjSchoolMst._semesterDiv)) {
            if (NumberUtils.isNumber(_param._gradeCdStr)) {
                svf.VrsOut("GRADE", "第" + String.valueOf(Integer.parseInt(_param._gradeCdStr)) + "学年");
            }
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._descDate));
            svf.VrsOut("SCHOOLNAME1", _param._certifSchoolRemark3);
            svf.VrsOut("JOB", _param._certifSchoolJobName);
            svf.VrsOut("STAFFNAME", _param._certifSchoolPrincipalName);
            svf.VrsOut("STAMPC", _param.getStampImagePath());
        }

        svf.VrEndPage();
        _hasData = true;
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
            final Student student) {
        
        final String form = "KNJD184J_2.frm";
        svf.VrSetForm(form, 4);
        
        printSvfHeader(svf, student);
        
        printSvfAttendSemes(svf, student);

        printSvfReport(svf, student);

        printSvfReportCommunication(svf, student);
        
        printSvfRecordRank(svf, student);
        
        _hasData = true;
    }
    
    private void printSvfHeader(final Vrw32alp svf, final Student student) {
        svf.VrsOut("NAME", student._name);
        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)): student._attendno;
        svf.VrsOut("ATTENDNO", attendno);

        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._descDate));
        
        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolRemark3);
        
        svf.VrsOut("JOB_NAME", "学級担任");
        
        svf.VrsOut("TEACHER_NAME", _param._tr1Name);
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
            svf.VrsOutn("SUSPEND", j, notZero(attendSemesDat._suspend + attendSemesDat._mourning));
            svf.VrsOutn("PRESENT", j, notZero(attendSemesDat._mlesson));
            svf.VrsOutn("ATTEND", j, notZero(attendSemesDat._present));
            svf.VrsOutn("ABSENCE", j, notZero(attendSemesDat._sick));
            svf.VrsOutn("LATE", j, notZero(attendSemesDat._late));
            svf.VrsOutn("EARLY", j, notZero(attendSemesDat._early));
        }
        
        final int pchars = getParamSizeNum(_param._HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J, 0);
        final int plines = getParamSizeNum(_param._HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J, 1);
        final int chars = (-1 == pchars || -1 == plines) ? 14 : pchars;
        final int lines = (-1 == pchars || -1 == plines) ? 2 : plines;
        
        for (final Iterator it = student._hReportRemarkDatMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) student._hReportRemarkDatMap.get(semester);
            if ("9".equals(semester)) {
                continue;
            }
            
            final List list = knjobj.retDividString(hReportRemarkDat._attendrecRemark, chars * 2, lines);
            if (null != list) {
                if (list.size() == 1) {
                    svf.VrsOutn("REMARK2", Integer.parseInt(hReportRemarkDat._semester), (String) list.get(0));
                } else {
                    for (int i = 0 ; i < list.size(); i++) {
                        svf.VrsOutn("REMARK" + String.valueOf(i + 1), Integer.parseInt(hReportRemarkDat._semester), (String) list.get(i));
                    }
                }
            }
        }
    }
    
    private static String getTestkindcd(final String semtestcd) {
        return semtestcd.substring(semtestcd.length() - 4, semtestcd.length() - 2);
    }
    
    /**
     * 学習の記録
     * @param svf
     * @param student
     */
    private void printSvfRecordRank(final Vrw32alp svf, final Student student) {
        // 全教科
        final ScoreSubclass scoreSubclassAll = (ScoreSubclass.getScoreSubclass(student._scoreSubclassList, SUBCLASSALL));
        final String _10101 = ScoreSubclass.KOUSA + "10101"; // 1学期中間
        final String _10201 = ScoreSubclass.KOUSA + "10201"; // 1学期期末
        final String _20101 = ScoreSubclass.KOUSA + "20101"; // 2学期中間
        final String _20201 = ScoreSubclass.KOUSA + "20201"; // 2学期期末
        final String _30201 = ScoreSubclass.KOUSA + "30201"; // 3学期期末
        final String _19900 = ScoreSubclass.KOUSA + "19900"; // 1学期評価
        final String _29900 = ScoreSubclass.KOUSA + "29900"; // 2学期評価
        final String _99900 = ScoreSubclass.KOUSA + "99900"; // 学年末評定

        if (null != scoreSubclassAll) {
            final Map fieldst = new HashMap();
            fieldst.put(_10101, "1");
            fieldst.put(_10201, "2");
            fieldst.put(_20101, "3");
            fieldst.put(_20201, "4");
            fieldst.put(_30201, "5");
            for (int i = 0; i < _param._proficiencyCdList.size(); i++) {
                final String proficiencyCd = (String) _param._proficiencyCdList.get(i);
                fieldst.put(ScoreSubclass.JITURYOKU + proficiencyCd, String.valueOf(5 + 1 + i));
            }
            
            for (final Iterator its = scoreSubclassAll._scoreRankMap.keySet().iterator(); its.hasNext();) {
                final String semtestcd = (String) its.next();
                final ScoreRank scoreRank = (ScoreRank) scoreSubclassAll._scoreRankMap.get(semtestcd);
                final String n = (String) fieldst.get(semtestcd);
                if (null != n) {
                    final int i = Integer.parseInt(n);
                    svf.VrsOutn("TOTAL", i, scoreRank._score);
                    svf.VrsOutn("AVERAGE", i, scoreRank._avg);
                    if (1 == Integer.parseInt(_param._gradeCdStr) && 6 == i) {
                        // 中学1年の実力の一番上の段（'一学期'）の合計の順位は非表示とする
                    } else {
                        svf.VrsOutn("RANK", i, scoreRank._rank);
                    }
                }
            }
        }

        // 各教科
        final int maxLine = 12;
        int line = 0;
        final Map fields = new HashMap();
        fields.put(_10101, "SCORE1_1");
        fields.put(_10201, "SCORE1_2");
        fields.put(_20101, "SCORE2_1");
        fields.put(_20201, "SCORE2_2");
        fields.put(_30201, "SCORE3");
        for (int i = 0; i < _param._proficiencyCdList.size(); i++) {
            final String proficiencyCd = (String) _param._proficiencyCdList.get(i);
            fields.put(ScoreSubclass.JITURYOKU + proficiencyCd, "MOCK" + String.valueOf(1 + i));
        }
        fields.put(_19900, "VALUE1");
        fields.put(_29900, "VALUE2");
        fields.put(_99900, "VALUE3");
        

        for (final Iterator it = student._scoreSubclassList.iterator(); it.hasNext();) {
            final ScoreSubclass scoreSubclass = (ScoreSubclass) it.next();
            if (SUBCLASSALL.equals(scoreSubclass._subclasscd)) {
                continue;
            }
            
            boolean hasKousa = false;
            for (final Iterator its = fields.keySet().iterator(); its.hasNext();) {
                final String semtestcd = (String) its.next();
                final ScoreRank scoreRank = (ScoreRank) scoreSubclass._scoreRankMap.get(semtestcd);
                if (null != scoreRank && semtestcd.startsWith(ScoreSubclass.KOUSA)) {
                    hasKousa = true;
                }
            }
            if (!hasKousa) {
                continue;
            }
            final boolean isPrint = CombinedSubclass.isPrint(student._combinedSubclassList, scoreSubclass._scoreRankMap.keySet(), scoreSubclass._subclasscd, _param);
            if (!isPrint) {
                continue;
            }
            
            svf.VrsOut("CLASS", scoreSubclass._classname);
            
            for (final Iterator its = scoreSubclass._scoreRankMap.keySet().iterator(); its.hasNext();) {
                final String semtestcd = (String) its.next();
                final ScoreRank scoreRank = (ScoreRank) scoreSubclass._scoreRankMap.get(semtestcd);
                final String field = (String) fields.get(semtestcd);
                if (null != field) {
                    if (semtestcd.startsWith(ScoreSubclass.KOUSA) && semtestcd.endsWith("9900")) {
                        svf.VrsOut(field, scoreRank._value);
                    } else {
                        svf.VrsOut(field, scoreRank._score);
                    }
                }
            }
            svf.VrEndRecord();
            line++;
        }
        
        // 空行挿入
        final int st = line == 0 ? 0 : line % maxLine == 0 ? maxLine : line % maxLine;
        for (int i = st; i < maxLine; i++) {
            svf.VrsOut("CLASS", "");
            svf.VrEndRecord();
        }
    }
    
    /**
     * 『総合的な学習の時間』『特別活動等の記録』
     * @param svf
     * @param student
     */
    private void printSvfReport(final Vrw32alp svf, final Student student) {

        final HReportRemarkDat hReportRemarkDat9 = (HReportRemarkDat) student._hReportRemarkDatMap.get("9");

        if (null != hReportRemarkDat9) {

            final int pcharsfl = getParamSizeNum(_param._HREPORTREMARK_DAT_REMARK1_SIZE_J, 0);
            final int plinesfl = getParamSizeNum(_param._HREPORTREMARK_DAT_REMARK1_SIZE_J, 1);
            final int charsfl = (-1 == pcharsfl || -1 == plinesfl) ? 13 : pcharsfl;
            final int linesfl = (-1 == pcharsfl || -1 == plinesfl) ?  2 : plinesfl;
            
            // 部活動
            VrsOutRenban(svf, "CLUB1_", knjobj.retDividString(hReportRemarkDat9._remark1, charsfl * 2, linesfl));
            
            final int pcharstotal = getParamSizeNum(_param._HREPORTREMARK_DAT_REMARK2_SIZE_J, 0);
            final int plinestotal = getParamSizeNum(_param._HREPORTREMARK_DAT_REMARK2_SIZE_J, 1);
            final int charstotal = (-1 == pcharstotal || -1 == plinestotal) ? 13 : pcharstotal;
            final int linestotal = (-1 == pcharstotal || -1 == plinestotal) ? 10 : plinestotal;

            // 表彰
            VrsOutRenban(svf, "COMM", knjobj.retDividString(hReportRemarkDat9._remark2, charstotal * 2, linestotal));
        }

        for (final Iterator it = student._hReportRemarkDatMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) student._hReportRemarkDatMap.get(semester);
            if ("9".equals(semester)) {
                continue;
            }

            final int pcharssp = getParamSizeNum(_param._HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J, 0);
            final int plinessp = getParamSizeNum(_param._HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J, 1);
            final int charssp = (-1 == pcharssp || -1 == plinessp) ? 13 : pcharssp;
            final int linessp = (-1 == pcharssp || -1 == plinessp) ?  2 : plinessp;

            // 特別活動の記録
            VrsOutRenban(svf, "SPECIAL" + String.valueOf(Integer.parseInt(semester)) + "_", knjobj.retDividString(hReportRemarkDat._specialactremark, charssp * 2, linessp));
        }
    }
    
    /**
     * 『学級担任の所見』
     * @param svf
     * @param student
     */
    private void printSvfReportCommunication(final Vrw32alp svf, final Student student) {
        final int pchars = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_J, 0);
        final int plines = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_J, 1);
        final int chars = (-1 == pchars || -1 == plines) ? 16 : pchars;
        final int lines = (-1 == pchars || -1 == plines) ? 6  : plines;
        
        final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) student._hReportRemarkDatMap.get(_param._semester);
        if (null != hReportRemarkDat) {
            VrsOutRenban(svf, "VIEW", knjobj.retDividString(hReportRemarkDat._communication, chars * 2 , lines));
        }
    }
    
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final String _gradeCourse;
        List _attendSemesDatList = Collections.EMPTY_LIST; // 出欠のようす
        Map _hReportRemarkDatMap = Collections.EMPTY_MAP; // 所見
        List _scoreSubclassList = Collections.EMPTY_LIST;
        List _combinedSubclassList = Collections.EMPTY_LIST;
        
        public Student(final String schregno, final String name, final String hrName, final String attendno, final String gradeCourse) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
            _gradeCourse = gradeCourse;
        }
        
        public void load(final DB2UDB db2, final Param param) {
            _hReportRemarkDatMap = HReportRemarkDat.getHReportRemarkDatMap(db2, param, _schregno);
            _scoreSubclassList = ScoreSubclass.getScoreSubclassList(db2, param, _schregno);
            _combinedSubclassList = CombinedSubclass.loadCombinedSubclassCdList(db2, param, _gradeCourse);
//            log.debug(" attendSemesDatList = " + _attendSemesDatList);
//            log.debug(" hReportRemarkDatMap = " + _hReportRemarkDatMap);
//            log.debug(" scoreSubclassMap = " + _scoreSubclassMap);
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
                        final int suspend = rs.getInt("SUSPEND") + rs.getInt("VIRUS") + rs.getInt("KOUDOME");
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
                        student._attendSemesDatList.add(attendSemesDat);
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
        
        public String toString() {
            return "AttendSemesDat(" + _semester + ": [" + _lesson + ", " + _suspend  + ", " + _mourning  + ", " + _mlesson  + ", " + _sick  + ", " + _present + ", " + _late + ", " + _early + "])";
        }
    }
    
    private static class CombinedSubclass {
        final String _flg;
        final String _combinedSubclasscd;
        final List _attendSubclasscdList = new ArrayList();
        CombinedSubclass(final String flg, final String combinedSubclasscd) {
            _flg = flg;
            _combinedSubclasscd = combinedSubclasscd;
        }
        
        public String toString() {
            return "CombinedSubclass(" + _flg + ", " + _combinedSubclasscd + ", " + _attendSubclasscdList + ")"; 
        }

        private static final boolean isPrint(final List list, final Collection semtestcds, final String chkSubclasscd, final Param param) {
            boolean hasSoten = false;
            boolean hasGakkiSeiseki = false;
            for (final Iterator it = semtestcds.iterator(); it.hasNext();) {
                final String semtestcd = (String) it.next();
                final String testkindcd = getTestkindcd(semtestcd);
                if ("99".equals(testkindcd)) {
                    hasGakkiSeiseki = true;
                } else {
                    hasSoten = true;
                }
            }
            return hasSoten && isPrint(list, "1", chkSubclasscd, param) || hasGakkiSeiseki && isPrint(list, "2", chkSubclasscd, param);
        }
        
        
        private static final boolean isPrint(final List list, final String flg, final String chkSubclasscd, final Param param) {
            final List flgList = getCombinedSubclassFlg(list, flg);
            final boolean isMotoKamoku = isMotokamoku(flgList, chkSubclasscd);
            if (isMotoKamoku) {
                final List combinedSubclasscdList = getSakikamoku(flgList, chkSubclasscd);
                for (final Iterator it = combinedSubclasscdList.iterator(); it.hasNext();) {
                    final String combinedSubclasscd = (String) it.next();
                    if (param._nameMstD052List.contains(combinedSubclasscd)) {
                        return true;
                    }
                }
                return false;
            }
            if (param._nameMstD052List.contains(chkSubclasscd)) {
                return false;
            }
            return true;
        }
        
        private static boolean isMotokamoku(final List list, final String chkSubclasscd) {
            final List res = getSakikamoku(list, chkSubclasscd);
//            log.fatal("   " + chkSubclasscd + " is attendsubclass? :" + !res.isEmpty());
            if (!res.isEmpty()) {
//                log.fatal("   " + chkSubclasscd + " is attendsubclass :" + res);
                return true;
            }
            return false;
        }
        
        private static List getSakikamoku(final List list, final String chkSubclasscd) {
            final List res = new ArrayList();
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final CombinedSubclass cs = (CombinedSubclass) it.next();
                if (cs._attendSubclasscdList.contains(chkSubclasscd)) {
                    res.add(cs._combinedSubclasscd);
                }
            }
            return res;
        }

        private static List loadCombinedSubclassCdList(final DB2UDB db2, final Param param, final String gradeCourse) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append("   SELECT ");
                stb.append("       T1.FLG, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("       T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
                }
                stb.append("       T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append("       T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
                stb.append("   FROM ");
                stb.append("       SUBCLASS_WEIGHTING_COURSE_DAT T1 ");
                stb.append("   WHERE ");
                stb.append("       T1.YEAR = '" + param._year + "' ");
                stb.append("       AND T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + gradeCourse + "' ");
                
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String flg = rs.getString("FLG");
                    final String combinedSubclasscd = rs.getString("COMBINED_SUBCLASSCD");
                    CombinedSubclass cs = getCombinedSubclass(list, flg, combinedSubclasscd, param, false);
                    if (null == cs) {
                        cs = new CombinedSubclass(flg, combinedSubclasscd);
                        list.add(cs);
                    }
                    cs._attendSubclasscdList.add(rs.getString("ATTEND_SUBCLASSCD"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
//            log.fatal(" combined subclass list = " + list);
            return list;
        }
        
        private static List getCombinedSubclassFlg(final List list, final String flg) {
            final List rtn = new ArrayList();
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final CombinedSubclass cs = (CombinedSubclass) it.next();
                if (cs._flg.equals(flg)) {
                    rtn.add(cs);
                }
            }
            return rtn;
        }
        
        private static List getCombinedSubclassList(final List list, final String combinedSubclasscd, final Param param, final boolean isSubclasscd) {
            final List rtn = new ArrayList();
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final CombinedSubclass cs = (CombinedSubclass) it.next();
                if ("1".equals(param._useCurriculumcd) && isSubclasscd) {
                    final String cSubclasscd = StringUtils.split(cs._combinedSubclasscd, "-")[3];
                    if (cSubclasscd.equals(combinedSubclasscd)) {
                        rtn.add(cs);
                    }
                } else {
                    if (cs._combinedSubclasscd.equals(combinedSubclasscd)) {
                        rtn.add(cs);
                    }
                }
            }
            return rtn;
        }
        
        private static CombinedSubclass getCombinedSubclass(final List list, final String flg, final String combinedSubclasscd, final Param param, final boolean isSubclasscd) {
            final List l = getCombinedSubclassList(getCombinedSubclassFlg(list, flg), combinedSubclasscd, param, isSubclasscd);
            return l.isEmpty() ? null : (CombinedSubclass) l.get(0);
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

        public static Map getHReportRemarkDatMap(final DB2UDB db2, final Param param, final String schregno) {
            final Map map = new HashMap();
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
                    final String attendrecRemark = rs.getString("ATTENDREC_REMARK");
                    final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, totalstudytime, specialactremark, communication, remark1, remark2, remark3, attendrecRemark);
                    map.put(semester, hReportRemarkDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }
        
        private static String getHReportRemarkDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND (T1.SEMESTER <= '" + param._semester + "' OR T1.SEMESTER = '9') ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            return stb.toString();
        }
        
        public String toString() {
            return "HReportRemarkDat(" + _semester + ": totalstudytime=" + _totalstudytime + ", specialactremark=" + _specialactremark + ", communication=" + _communication + ", remark1=" + _remark1 + ", remark2=" + _remark2 + ", remark3=" + _remark3 + ", attendrecRemark= " + _attendrecRemark + ")";
        }
    }
    
    private static class ScoreSubclass {
        
        final static String KOUSA = "KOUSA";
        final static String JITURYOKU = "JITURYOKU";
        
        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final Map _scoreRankMap = new HashMap();
        public ScoreSubclass(final String subclasscd, final String classname, final String subclassname) {
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
        }
        
        private static ScoreSubclass getScoreSubclass(final List list, final String subclasscd) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final ScoreSubclass ss = (ScoreSubclass) it.next();
                if (subclasscd.equals(ss._subclasscd)) {
                    return ss;
                }
            }
            return null;
        }

        public static List getScoreSubclassList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getScoreSubclassSql(param, schregno);
//                log.debug(" subclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd;
                    if ("1".equals(param._useCurriculumcd) && !SUBCLASSALL.equals(rs.getString("SUBCLASSCD"))) {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    } else {
                        subclasscd = rs.getString("SUBCLASSCD");
                    }
                    
                    ScoreSubclass ss = getScoreSubclass(list, subclasscd);
                    if (null == ss) {
                        ss = new ScoreSubclass(subclasscd, rs.getString("CLASSNAME"), rs.getString("SUBCLASSNAME"));
                        list.add(ss);
                    }
                    
                    final String semtestcd = rs.getString("TEST_DIV_NAME") + rs.getString("SEMTESTCD");
                    final String score = rs.getString("SCORE");
                    final String value = rs.getString("VALUE");
                    final String avg = null == rs.getString("AVG") ? null : new BigDecimal(rs.getString("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    final String rank = rs.getString("RANK");
                    final ScoreRank scoreRank = new ScoreRank(score, value, avg, rank);
                    ss._scoreRankMap.put(semtestcd, scoreRank);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getScoreSubclassSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            
            stb.append(" WITH PROFICIENCY AS ( ");
            stb.append(" SELECT  ");
            stb.append("     '" + JITURYOKU + "' AS TEST_DIV_NAME, ");
            stb.append("     T1.SEMESTER || T1.PROFICIENCYDIV || T1.PROFICIENCYCD AS SEMTESTCD, ");
            stb.append("     T1.PROFICIENCY_SUBCLASS_CD, T1.YEAR, T1.SEMESTER, T1.PROFICIENCYDIV, T1.PROFICIENCYCD, ");
            stb.append("     T1.SCORE, T1.AVG, T1.RANK, T3.GRADE, T3.COURSECD, T3.MAJORCD, T3.COURSECODE, T4.GROUP_CD ");
            stb.append(" FROM  ");
            stb.append("     PROFICIENCY_RANK_DAT T1 ");
            stb.append(" LEFT JOIN SCHREG_REGD_DAT T3 ON  T3.SCHREGNO = T1.SCHREGNO AND T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");
            stb.append(" LEFT JOIN COURSE_GROUP_CD_DAT T4 ON T4.YEAR = T3.YEAR AND T4.GRADE = T3.GRADE AND  ");
            stb.append("  T4.COURSECD = T3.COURSECD AND T4.MAJORCD = T3.MAJORCD AND T4.COURSECODE = T3.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (!param._semester.equals(param._knjSchoolMst._semesterDiv)) {
                stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            }
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND T1.RANK_DATA_DIV = '01' "); // 総合点
            stb.append("     AND T1.RANK_DIV = '01' "); // 学年
            // 実力合併元科目は表示対象から除く
            stb.append("     AND NOT EXISTS (SELECT 'X' FROM PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT TT1 WHERE TT1.YEAR = T1.YEAR ");
            stb.append("          AND TT1.SEMESTER = T1.SEMESTER AND TT1.PROFICIENCYDIV = T1.PROFICIENCYDIV ");
            stb.append("          AND TT1.PROFICIENCYCD = T1.PROFICIENCYCD AND TT1.PROFICIENCYDIV = T1.PROFICIENCYDIV ");
            stb.append("          AND (TT1.DIV = '03' AND TT1.GRADE = T3.GRADE AND TT1.COURSECD = T3.COURSECD AND TT1.MAJORCD = T3.MAJORCD AND TT1.COURSECODE = T3.COURSECODE ");
            stb.append("               OR TT1.DIV = '04' AND TT1.GRADE = T3.GRADE AND TT1.COURSECD = '0' AND TT1.MAJORCD = T4.GROUP_CD AND TT1.COURSECODE = '0000') ");
            stb.append("          AND TT1.ATTEND_SUBCLASSCD = T1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("     )");
            stb.append(" ), PROFICIENCY_SUBCLASS AS ( ");
            stb.append(" SELECT  ");
            stb.append("   T1.PROFICIENCY_SUBCLASS_CD, T1.SEMTESTCD, T5.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        , T5.CLASSCD, T5.SCHOOL_KIND, T5.CURRICULUM_CD ");
            }
            stb.append(" FROM  ");
            stb.append("     PROFICIENCY T1 ");
            stb.append(" INNER JOIN PROFICIENCY_SUBCLASS_YDAT T5 ON T5.DIV = '03' AND "); // 課程学科コース
            stb.append("   T5.GRADE = T1.GRADE AND T5.COURSECD = T1.COURSECD AND T5.MAJORCD = T1.MAJORCD AND T5.COURSECODE = T1.COURSECODE AND  ");
            stb.append("   T5.YEAR = T1.YEAR AND T5.SEMESTER = T1.SEMESTER AND T5.PROFICIENCYDIV = T1.PROFICIENCYDIV AND ");
            stb.append("   T5.PROFICIENCYCD = T1.PROFICIENCYCD AND T5.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
            stb.append(" WHERE T5.SUBCLASSCD IS NOT NULL ");
            stb.append(" UNION ");
            stb.append(" SELECT  ");
            stb.append("   T1.PROFICIENCY_SUBCLASS_CD, T1.SEMTESTCD, T6.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        , T6.CLASSCD, T6.SCHOOL_KIND, T6.CURRICULUM_CD ");
            }
            stb.append(" FROM  ");
            stb.append("     PROFICIENCY t1  ");
            stb.append(" INNER JOIN PROFICIENCY_SUBCLASS_YDAT T6 ON T6.DIV = '04' AND "); // コースグループ
            stb.append("   T6.GRADE = T1.GRADE AND T6.COURSECD = '0' AND T6.MAJORCD = T1.GROUP_CD AND T6.COURSECODE = '0000' AND  ");
            stb.append("   T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER AND T6.PROFICIENCYDIV = T1.PROFICIENCYDIV AND ");
            stb.append("   T6.PROFICIENCYCD = T1.PROFICIENCYCD AND T6.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
            stb.append(" WHERE T6.SUBCLASSCD IS NOT NULL ");
            stb.append(" ), MAIN AS ( ");
            stb.append(" SELECT ");
            stb.append("     '" + KOUSA + "' AS TEST_DIV_NAME, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T1.CLASSCD, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD AS SEMTESTCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     CASE WHEN T1.SEMESTER <> '9' AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' ");
            stb.append("          THEN L4.ASSESSLEVEL ");
            stb.append("          ELSE T1.VALUE END AS VALUE, ");
            stb.append("     CAST(NULL AS DECIMAL(9,5)) AS AVG, ");
            stb.append("     T2.GRADE_RANK AS RANK ");
            stb.append(" FROM ");
            stb.append("     RECORD_SCORE_DAT T1 ");
            stb.append("     LEFT JOIN RECORD_RANK_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("        AND T2.TESTKINDCD = T1.TESTKINDCD AND T2.TESTITEMCD = T1.TESTITEMCD AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN ASSESS_LEVEL_SEMES_MST L4 ON  L4.YEAR = T1.YEAR ");
            stb.append("         AND L4.SEMESTER = T1.SEMESTER ");
            stb.append("         AND L4.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND L4.TESTITEMCD = T1.TESTITEMCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND L4.CLASSCD = T1.CLASSCD ");
                stb.append("         AND L4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND L4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND L4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND L4.DIV = '1' ");//学年
            stb.append("         AND L4.GRADE = '" + param._grade + "' ");
            stb.append("         AND L4.HR_CLASS = '000' ");
            stb.append("         AND L4.COURSECD = '0' ");
            stb.append("         AND L4.MAJORCD = '000' ");
            stb.append("         AND L4.COURSECODE = '0000' ");
            stb.append("         AND T1.VALUE BETWEEN L4.ASSESSLOW AND L4.ASSESSHIGH ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (!param._semester.equals(param._knjSchoolMst._semesterDiv)) {
                stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            }
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND (T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' AND T1.SCORE_DIV = '00' OR T1.SCORE_DIV = '01') ");
            stb.append("     AND NOT (T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '01') ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ");
            stb.append("     '" + KOUSA + "' AS TEST_DIV_NAME, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T1.CLASSCD, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD AS SEMTESTCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     CAST(NULL AS SMALLINT) AS VALUE, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.GRADE_RANK AS RANK ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (!param._semester.equals(param._knjSchoolMst._semesterDiv)) {
                stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            }
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND T1.SUBCLASSCD = '" + SUBCLASSALL + "' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '" + JITURYOKU + "' AS TEST_DIV_NAME, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     (CASE WHEN T1.PROFICIENCY_SUBCLASS_CD = '999999' THEN CAST(NULL AS VARCHAR(2)) ELSE T2.CLASSCD END) AS CLASSCD, ");
                stb.append("     (CASE WHEN T1.PROFICIENCY_SUBCLASS_CD = '999999' THEN CAST(NULL AS VARCHAR(1)) ELSE T2.SCHOOL_KIND END) AS SCHOOL_KIND, ");
                stb.append("     (CASE WHEN T1.PROFICIENCY_SUBCLASS_CD = '999999' THEN CAST(NULL AS VARCHAR(2)) ELSE T2.CURRICULUM_CD END) AS CURRICULUM_CD, ");
            }
            stb.append("     (CASE WHEN T1.PROFICIENCY_SUBCLASS_CD = '999999' THEN '999999' ELSE T2.SUBCLASSCD END) AS SUBCLASSCD, ");
            stb.append("     T1.SEMTESTCD, "); 
            stb.append("     T1.SCORE, ");
            stb.append("     CAST(NULL AS SMALLINT) AS VALUE, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.RANK ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY T1 ");
            stb.append("     LEFT JOIN PROFICIENCY_SUBCLASS T2 ON T2.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("         AND T2.SEMTESTCD = T1.SEMTESTCD ");
            stb.append(" WHERE ");
            stb.append("     (T1.PROFICIENCY_SUBCLASS_CD = '999999' OR T2.SUBCLASSCD IS NOT NULL) ");
            stb.append(" ), ORDER AS ( ");
            stb.append(" SELECT DISTINCT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T4.CLASSNAME, ");
            stb.append("     VALUE(T3.SUBCLASSORDERNAME2, T3.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("     VALUE(T4.SHOWORDER3, 99) AS ORDER1, ");
            stb.append("     VALUE(T3.SHOWORDER3, 99) AS ORDER2 ");
            stb.append(" FROM  ");
            stb.append("     MAIN T1 ");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            }
            stb.append(" T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN CLASS_MST T4 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            } else {
                stb.append(" T4.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
            }
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T4.SHOWORDER3, 99), VALUE(T4.SHOWORDER3, 99), T1.SUBCLASSCD ");
            stb.append(" ) ");
            stb.append(" SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T1.CLASSCD, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T2.CLASSNAME, ");
            stb.append("     T2.SUBCLASSNAME, ");
            stb.append("     T1.TEST_DIV_NAME, ");
            stb.append("     T1.SEMTESTCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.VALUE, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.RANK, ");
            stb.append("     VALUE(T2.ORDER1, 99) AS ORDER1, ");
            stb.append("     VALUE(T2.ORDER2, 99) AS ORDER2 ");
            stb.append(" FROM  ");
            stb.append("     MAIN T1 ");
            stb.append(" LEFT JOIN ORDER T2 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append("   T2.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("   T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            if ("1".equals(param._useCurriculumcd) && "1".equals(param._useClassDetailDat)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ");
                stb.append("         SELECT ");
                stb.append("             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
                stb.append("         FROM ");
                stb.append("             SUBCLASS_DETAIL_DAT T1 ");
                stb.append("         WHERE ");
                stb.append("             YEAR = '" + param._year + "' ");
                stb.append("             AND SUBCLASS_SEQ = '007' ");
                if ("1".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK1 = '1' ");
                } else if ("2".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK2 = '1' ");
                } else if ("3".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK3 = '1' ");
                } else if ("9".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK4 = '1' ");
                }
                stb.append("         ) ");
            } else {
                stb.append("     T1.SUBCLASSCD NOT IN ( ");
                stb.append("         SELECT ");
                stb.append("             NAME1 ");
                stb.append("         FROM ");
                stb.append("             NAME_MST ");
                stb.append("         WHERE ");
                stb.append("             NAMECD1 = 'D026' ");
                if ("1".equals(param._semester)) {
                    stb.append("         AND ABBV1 = '1' ");
                } else if ("2".equals(param._semester)) {
                    stb.append("         AND ABBV2 = '1' ");
                } else if ("3".equals(param._semester)) {
                    stb.append("         AND ABBV3 = '1' ");
                } else if ("9".equals(param._semester)) {
                    stb.append("         AND NAMESPARE1 = '1' ");
                }
                stb.append("         ) ");
            }
            stb.append(" ORDER BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     VALUE(T2.ORDER1, 99), T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, VALUE(T2.ORDER2, 99) ");
            } else {
                stb.append("     VALUE(T2.ORDER1, 99), SUBSTR(T1.SUBCLASSCD, 1, 2), VALUE(T2.ORDER2, 99) ");
            }
            stb.append("     , T1.SUBCLASSCD, T1.SEMTESTCD ");
            return stb.toString();
        }
        
        public String toString() {
            return "ScoreSubclass(" + _subclasscd + ":" + _subclassname + ":" + _scoreRankMap + ")";
        }
    }
    
    private static class ScoreRank {
        final String _score;
        final String _value;
        final String _avg;
        final String _rank;
        
        public ScoreRank(
                final String score,
                final String value,
                final String avg,
                final String rank) {
            _score = score;
            _value = value;
            _avg = avg;
            _rank = rank;
        }
        public String toString() {
            return "[score=" + _score + ", value=" + _value + ", avg=" + _avg + ", rank=" + _rank + "]";
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
        String _trCd1;
        final boolean _isPrintHyoshi;
        final boolean _isPrintSeiseki;
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
        
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useClassDetailDat;

        final String _HREPORTREMARK_DAT_REMARK1_SIZE_J;
        final String _HREPORTREMARK_DAT_REMARK2_SIZE_J;
        final String _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_J;
        final String _HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J;
        
        final List _nameMstD052List;
       
       /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;
        
        String _gradeCdStr;
        
        final Map _attendParamMap;
        
        /** 実力テストのコード */
        List _proficiencyCdList = Collections.EMPTY_LIST;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _categorySelected = request.getParameterValues("category_selected");
            setTrCd1(db2);
            _isPrintHyoshi  = null != request.getParameter("PRINT_SIDE1");
            _isPrintSeiseki = null != request.getParameter("PRINT_SIDE2");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            
            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
            _imagePath = null == returnval ? null : returnval.val4;
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
            _descDate = request.getParameter("DESC_DATE");
            
            setGrade(db2, _grade);
            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolRemark3 = getCertifSchoolDat(db2, "REMARK3");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _tr1Name = getStaffname(db2, _trCd1);
            _hyosiHrName = _gradeCdStr + "年 " + getHrClassName1(db2, _year, _semester, _gradeHrclass) + "組";
            _certifSchoolJobName = getCertifSchoolDat(db2, "JOB_NAME");
            
            _HREPORTREMARK_DAT_REMARK1_SIZE_J = null == request.getParameter("_HREPORTREMARK_DAT_REMARK1_SIZE_J") ? "" : StringUtils.replace(request.getParameter("_HREPORTREMARK_DAT_REMARK1_SIZE_J"), "+", " ");
            _HREPORTREMARK_DAT_REMARK2_SIZE_J = null == request.getParameter("_HREPORTREMARK_DAT_REMARK2_SIZE_J") ? "" : StringUtils.replace(request.getParameter("_HREPORTREMARK_DAT_REMARK2_SIZE_J"), "+", " ");
            _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J = null == request.getParameter("HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J"), "+", " ");
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_J = null == request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"), "+", " ");
            _HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J = null == request.getParameter("HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"), "+", " ");
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            
            // 学期名称 _arrsemesName をセットします。
            setProficiencyCdList(db2);
            log.fatal(" 表示する実力テスト : " + _proficiencyCdList);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            
            _nameMstD052List = getNameMstD052(db2, _year);
            
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
        
        private List getNameMstD052(final DB2UDB db2, final String year) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql;
                if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                    sql  = " SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS NAME1 ";
                    sql += " FROM SUBCLASS_DETAIL_DAT WHERE YEAR = '" + year + "' AND SUBCLASS_SEQ = '010' ";
                } else {
                    sql = " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + year + "' AND NAMECD1 = 'D052' ";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
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
        
        public String getStampImagePath() {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + "SCHOOLSTAMP_J." + _extension;
            if (new java.io.File(path).exists()) {
                return path;
            }
            return null;
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

        private void setGrade(final DB2UDB db2, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _gradeCdStr = null;
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
                        _gradeCdStr = String.valueOf(Integer.parseInt(tmp));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private void setTrCd1(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _trCd1 = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_HDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.SEMESTER = '" + _semester + "' ");
                stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _trCd1 = rs.getString("TR_CD1");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private void setProficiencyCdList(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _proficiencyCdList = new ArrayList();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.SEMESTER || T1.PROFICIENCYDIV || T1.PROFICIENCYCD AS TESTCD ");
                stb.append(" FROM ");
                stb.append("     RECORD_PROFICIENCY_ORDER_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.GRADE = '" + _grade + "' ");
                stb.append("     AND T1.TEST_DIV = '2' "); // 実力・模試
                stb.append(" ORDER BY ");
                stb.append("     T1.SEQ ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _proficiencyCdList.add(rs.getString("TESTCD"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}

// eof


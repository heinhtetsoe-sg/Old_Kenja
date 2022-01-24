/*
 * $Id: 451997af1f8b0ce4d4d6a2712500ae63c13c6383 $
 *
 * 作成日: 2011/12/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJC;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJC123 {

    private static final Log log = LogFactory.getLog(KNJC123.class);

    private boolean _hasData;

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

            Param param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf, param);

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

    private static int retStringByteValue(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;   //文字列をbyte配列へ
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        
        for (final Iterator itg = param._gradenameMap.keySet().iterator(); itg.hasNext();) {
            
            final String grade = (String) itg.next();
            
            svf.VrSetForm("KNJC123.frm", 4);
            
            String title = null;
            if ("1".equals(param._shubetu)) {
                title = "欠席日数超過者リスト";
            } else if ("2".equals(param._shubetu)) {
                title = "欠席日数要注意者リスト";
            } else if ("3".equals(param._shubetu)) {
                title = "遅刻・早退の多い生徒リスト";
            }

            boolean hasData = false;
            final List studentList = getTargetStudentList(db2, param, grade);
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                
                svf.VrsOut("TITLE", title);
                svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度");
                svf.VrsOut("GRADE", (String) param._gradenameMap.get(grade));
                svf.VrsOut("PERIOD", KNJ_EditDate.h_format_JP(param._sdate) + "\uFF5E" + KNJ_EditDate.h_format_JP(param._edate));
                svf.VrsOut("YMD", KNJ_EditDate.h_format_JP(param._ctrlDate));

                svf.VrsOut("HR_CLASS", student.getHrclassName());
                svf.VrsOut("NAME" + (retStringByteValue(student._name) > 20 ? "2" : "1"), student._name);
                svf.VrsOut("ATTEND_DAY", String.valueOf(student._mlesson));
                svf.VrsOut("ABSENCE_DAY", String.valueOf(student._sick));
                svf.VrsOut("LATE_DAY", String.valueOf(student._late));
                svf.VrsOut("EARLY_DAY", String.valueOf(student._early));
                svf.VrEndRecord();
                hasData = true;
            }
            
            if (hasData) {
                for (int i = 0; i < 50 - (studentList.size() % 50 + (0 == studentList.size() % 50 ? 50 : 0)); i++) {
                    svf.VrsOut("HR_CLASS", "\n");
                    svf.VrEndRecord();
                }
            }
            if (hasData) {
                _hasData = true;
            }
        }
    }
    
    private List getTargetStudentList(final DB2UDB db2, final Param param, final String grade) {
        
        final List studentList = getRegdStudentList(db2, param, grade);
        
        setAttendSemesDat(db2, studentList, param, grade);
        
        final List list = getPrintList(studentList, param);
        return list;
    }

    private List getRegdStudentList(final DB2UDB db2, final Param param, final String grade) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List studentList = new ArrayList();
        try {
            final String sql = getRegdSql(param, grade);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String hrname = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final Student student = new Student(schregno, name, hrname, attendno);
                
                studentList.add(student);
            }
        } catch (Exception ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }

    private List getPrintList(final List studentList, final Param param) {
        final List list = new ArrayList(); // 出力対象の生徒
        BigDecimal outBunsi = param.outBunsi;
        BigDecimal outBunbo = param.outBunbo;
        BigDecimal warnBunsi = param.warnBunsi;
        BigDecimal warnBunbo = param.warnBunbo;
        
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            
            // log.debug(" student = " + student);
            
            if ("1".equals(param._shubetu)) {
                final int absencehigh = null == outBunbo  || null == outBunsi  ? 0 : new BigDecimal(student._mlesson).multiply(outBunsi).divide(outBunbo, 0, BigDecimal.ROUND_UP).intValue(); 
                if (0 < absencehigh && absencehigh < student._sick) {
                    list.add(student);
                } else {
                    // log.debug(" 対象外 超過上限値=" + absencehigh);
                }
            } else if ("2".equals(param._shubetu)) {
                final int absencehigh = null == warnBunbo || null == warnBunsi ? 0 : new BigDecimal(student._mlesson).multiply(warnBunsi).divide(warnBunbo, 0, BigDecimal.ROUND_UP).intValue(); 
                if (0 < absencehigh && absencehigh < student._sick) {
                    list.add(student);
                } else {
                    // log.debug(" 対象外 要注意上限値=" + absencehigh);
                }
            } else if ("3".equals(param._shubetu)) {
                if (param._late <= student._late || param._early <= student._early) { // 生徒の遅刻・早退が指定の遅刻・早退以上
                    list.add(student);
                } else {
                    // log.debug(" 対象外 指定遅刻=" + _param._late + ", 指定早退=" + _param._early);
                }
            }
        }
        return list;
    }

    private void setAttendSemesDat(final DB2UDB db2, final List studentList, final Param param, final String grade) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map studentMap = new HashMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            studentMap.put(student._schregno, student);
        }
        
        try {
            final boolean semesFlg = ((Boolean) param._hasuuMap.get("semesFlg")).booleanValue();
            final String sql = AttendAccumulate.getAttendSemesSql(
                    semesFlg,
                    param._definecode,
                    param._knjSchoolMst,
                    param._year,
                    "1",
                    "9",
                    (String) param._hasuuMap.get("attendSemesInState"),
                    param._periodInState,
                    (String) param._hasuuMap.get("befDayFrom"),
                    (String) param._hasuuMap.get("befDayTo"),
                    (String) param._hasuuMap.get("aftDayFrom"),
                    (String) param._hasuuMap.get("aftDayTo"),
                    grade,
                    null,
                    null,
                    "SEMESTER",
                    param._useCurriculumcd,
                    param._useVirus,
                    param._useKoudome
            );

//            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                if (!"9".equals(rs.getString("SEMESTER")) || null == studentMap.get(rs.getString("SCHREGNO"))) {
                    continue;
                }
                
                final Student student = (Student) studentMap.get(rs.getString("SCHREGNO"));
                student._mlesson = rs.getInt("MLESSON");
                student._sick = rs.getInt("SICK");
                student._late = rs.getInt("LATE");
                student._early = rs.getInt("EARLY");
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /** 
     * <pre>
     * 生徒の学籍等の情報および総合的な学習の時間の所見・通信欄を取得するＳＱＬ文を戻します。
     * ・指定された生徒全員を対象とします。
     * </pre>
     */
    private String getRegdSql(final Param param, final String grade) {
        final StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append(    "SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append(    "FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
        stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
        stb.append(        "AND T1.SEMESTER = '"+ param._gakki +"' ");
        stb.append(        "AND T1.YEAR = T2.YEAR ");
        stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
        stb.append(        "AND T1.GRADE = '" + grade + "' ");
        //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合 
        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append(                           "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END) ");
        stb.append(                             "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END)) ) ");
        //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append(                           "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append(    ") ");
        
        //メイン表
        stb.append("SELECT  T1.SCHREGNO, T5.NAME, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T2.HR_NAME ");
        stb.append("FROM    SCHNO_A T1 ");
        stb.append(        "INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
        stb.append(        "INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = '" + param._year + "' AND ");
        stb.append(                                          "T2.SEMESTER = T1.SEMESTER AND ");
        stb.append(                                          "T2.GRADE = T1.GRADE ");
        stb.append(                                          "AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append("ORDER BY T1.HR_CLASS, T1.ATTENDNO");
        return stb.toString();
    }
    
    static class Student {
        final String _schregno;
        final String _name;
        final String _hrname;
        final String _attendno;
        int _mlesson = 0;
        int _sick = 0;
        int _late = 0;
        int _early = 0;
        public Student(final String schregno, final String name, final String hrname, final String attendno) {
            _schregno = schregno;
            _name = name;
            _hrname = hrname;
            _attendno = attendno;
        }
        public String getHrclassName() {
            return (null == _hrname ? "" : _hrname) + (null == _attendno && !StringUtils.isNumeric(_attendno) ? "" : (Integer.parseInt(_attendno) + "番"));
        }
        public String toString() {
            return "Student(" + _schregno + ", mlesson=" + _mlesson + ", sick=" + _sick + ", late=" + _late + ", early=" + _early + ")";
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
        final String _gakki;
        final String _grade;
        final String _shubetu;
        final int _late;
        final int _early;
        final String _sdate;
        final String _edate;
        final String _ctrlDate;
        final Map _gradenameMap;
        
        final KNJDefineSchool _definecode;
        final KNJSchoolMst _knjSchoolMst;
        final String _useVirus;
        final String _useKoudome;
        
        final String _periodInState;
        final Map _attendSemesMap;
        final Map _hasuuMap;

        BigDecimal outBunsi;
        BigDecimal outBunbo;
        BigDecimal warnBunsi;
        BigDecimal warnBunbo;
        
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _grade = request.getParameter("GRADE");
            _shubetu = request.getParameter("SHUBETU");
            _late = StringUtils.isBlank(request.getParameter("SYUKKETU_SYUKKETU_TIKOKU")) ? 99999 : Integer.parseInt(request.getParameter("SYUKKETU_SYUKKETU_TIKOKU"));
            _early = StringUtils.isBlank(request.getParameter("SYUKKETU_SYUKKETU_SOUTAI")) ? 99999 : Integer.parseInt(request.getParameter("SYUKKETU_SYUKKETU_SOUTAI"));
            _sdate = request.getParameter("SDATE").replace('/', '-');
            _edate = request.getParameter("EDATE").replace('/', '-');
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _gradenameMap = getGradenameMap(db2, _year, _grade);
            
            KNJDefineSchool definecode = new KNJDefineSchool();
            KNJSchoolMst knjSchoolMst = null;
            try {
                definecode.defineCode(db2, _year);
                knjSchoolMst = new KNJSchoolMst(db2, _year);
                
                if ("1".equals(_shubetu)) {
                    if (null != knjSchoolMst._kessekiOutBunsi && 0 != Integer.parseInt(knjSchoolMst._kessekiOutBunsi)) {
                        outBunsi = new BigDecimal(knjSchoolMst._kessekiOutBunsi);
                    }
                    if (null != knjSchoolMst._kessekiOutBunbo && 0 != Integer.parseInt(knjSchoolMst._kessekiOutBunbo)) {
                        outBunbo = new BigDecimal(knjSchoolMst._kessekiOutBunbo);
                    }
                    log.fatal(" kessekiOut bunsi = " + knjSchoolMst._kessekiOutBunsi + ", bunbo = " + knjSchoolMst._kessekiOutBunbo);
                } else if ("2".equals(_shubetu)) {
                    if (null != knjSchoolMst._kessekiWarnBunsi && 0 != Integer.parseInt(knjSchoolMst._kessekiWarnBunsi)) {
                        warnBunsi = new BigDecimal(knjSchoolMst._kessekiWarnBunsi);
                    }
                    if (null != knjSchoolMst._kessekiWarnBunbo && 0 != Integer.parseInt(knjSchoolMst._kessekiWarnBunbo)) {
                        warnBunbo = new BigDecimal(knjSchoolMst._kessekiWarnBunbo);
                    }
                    log.fatal(" kessekiWarn bunsi = " + knjSchoolMst._kessekiWarnBunsi + ", bunbo = " + knjSchoolMst._kessekiWarnBunbo);
                }

            } catch (Exception e) {
                log.error("exception!", e);
            }
            _definecode = definecode;
            _knjSchoolMst = knjSchoolMst;
            final KNJDefineCode definecode0 = setClasscode0(db2);
            final String z010Name1 = setZ010Name1(db2);

            _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, "1", _gakki);
            _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
            _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sdate, _edate);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }
        
        private KNJDefineCode setClasscode0(final DB2UDB db2) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
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
        
        public Map getGradenameMap(final DB2UDB db2, final String year, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map gradenameMap = new TreeMap();
            try {
                String sql = "SELECT GRADE, GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' ";
                if (!"99".equals(grade)) {
                    sql += " AND GRADE = '" + grade + "' ";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    gradenameMap.put(rs.getString("GRADE"), rs.getString("GRADE_NAME1"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradenameMap;
        }
    }
}

// eof


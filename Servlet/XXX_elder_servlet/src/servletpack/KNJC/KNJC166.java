// kanji=漢字
/*
 * $Id: 6dbc4adcbe8e660e5f3ee0a2dc1ea2e2e43c2642 $
 *
 */
package servletpack.KNJC;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [事務管理] 皆勤・精勤者一覧
 */

public class KNJC166 {

    private static final Log log = LogFactory.getLog(KNJC166.class);
    private static final String TARGET_GRADE = "Paint=(1,90,2),Bold=1";
    private Param _param;
    private boolean _hasdata = false;
    private int MAX_LINE = 40;
    
    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        
        log.debug(" $Id: 6dbc4adcbe8e660e5f3ee0a2dc1ea2e2e43c2642 $ ");
        KNJServletUtils.debugParam(request, log);

        try {
            response.setContentType("application/pdf");
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
            
            // ＤＢ接続
            DB2UDB db2 = null;
            try {
                db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
                db2.open();
            } catch (Exception ex) {
                log.error("db2 open error!", ex);
                return;
            }
            _param = new Param(request, db2);

            // 印刷処理
            final Map[] schoolKinds = getSchoolKinds(db2);
            for (int i = 0; i < schoolKinds.length; i++) {
                printGradeList(svf, db2, schoolKinds[i]);
            }
            
            for (final Iterator it = _param._psMap.values().iterator(); it.hasNext();) {
                PreparedStatement ps = (PreparedStatement) it.next();
                DbUtils.closeQuietly(ps);
            }

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            // 終了処理
            if (!_hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    private void printGradeList(final Vrw32alp svf, final DB2UDB db2, final Map schoolKindMap) throws Exception {
        final List gradeList = (List) schoolKindMap.get("GRADE_LIST");
        List allList = new ArrayList();
        for (final Iterator git = gradeList.iterator(); git.hasNext();) {
            final Map gradeMap = (Map) git.next();
            final String grade = (String) gradeMap.get("GRADE");
            final String gradeCd = (String) gradeMap.get("GRADE_CD");
            final String schoolKindMaxGrade = (String) gradeMap.get("SCHOOL_KIND_MAX_GRADE");
            if (schoolKindMaxGrade == null && "2".equals(_param._output)) { // 累計皆勤者指定は、3年生以外は出力対象外
                continue;
            }
            
            final List studentList = Student.loadStudentList(db2, _param, grade);
            // 生徒がいなければ処理をスキップ
            if (studentList.size() == 0) {
                continue;
            }
            final Map regdMap = Regd.getRegdMap(studentList);
            log.debug(" grade = " + grade + " (" + gradeMap.get("SCHOOL_KIND") + ") regd key = " + regdMap.keySet());
            for (final Iterator it = regdMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String key = (String) e.getKey();
                final Collection regdList = (Collection) e.getValue();
                //log.debug(" set attend " + grade + " : " + hrClass);
                
                final String[] split = StringUtils.split(key, "-");
                DayAttendance.setAttendData(db2, _param, regdList, split[0], split[1], split[2], split[3]);
            }
            final List targetList = getTargetList(_param, studentList);
            Map m = new HashMap();
            m.put("GRADE", grade);
            m.put("GRADE_CD", gradeCd);
            m.put("LIST", targetList);
            allList.add(m);
            
        }
        int allPage = 0;
        for (final Iterator it = allList.iterator(); it.hasNext();) {
            final Map m = (Map) it.next();
            final List list = (List) m.get("LIST");
            allPage += getPageList(list, MAX_LINE).size();
        }
        int page = 1;
        for (final Iterator it = allList.iterator(); it.hasNext();) {
            final Map m = (Map) it.next();
            final List list = (List) m.get("LIST");
            final String grade = (String) m.get("GRADE");
            final String sdate = Param.getSemester1Sdate(db2, _param._ctrlYear, grade);
            final String dateRange = "出欠集計範囲 " + StringUtils.defaultString(KNJ_EditDate.h_format_JP(sdate)) + "～" + StringUtils.defaultString(KNJ_EditDate.h_format_JP(_param._date));
            final String gradeCd = (String) m.get("GRADE_CD");
            final String title = ("2".equals(_param._output) ? "累計" : "") + ("2".equals(_param._outputKaikin) ? "精勤" : "皆勤") + "者一覧表";
            final List pageList = getPageList(list, MAX_LINE);
            for (int pi = 0; pi < pageList.size(); pi++) {
                final List studentList = (List) pageList.get(pi);
                //log.info(" page : " + (pi + 1) + "/" + pageList.size() + ", studentList size = " + studentList.size());
                printPage(svf, title, gradeCd, studentList, page + pi, allPage, gradeList.size(), dateRange);
            }
            page += pageList.size();
        }
    }
    
    private void printPage(final Vrw32alp svf, final String title, final String gradeCd, final List studentList, final int page, final int allPage, final int count, final String dateRange) {
        
        final String form;
        final String[] suffx;
        final String GRADE_ALL = "99";
        if (count <= 3) {
            form = "KNJC166_1.frm";
            suffx = new String[] {"1", "2", "3", GRADE_ALL};
        } else if (count == 4) {
            form = "KNJC166_2.frm";
            suffx = new String[] {"1", "2", "3", "4", GRADE_ALL};
        } else { // count == 6
            form = "KNJC166_3.frm";
            suffx = new String[] {"1", "2", "3", "4", "5", "6", GRADE_ALL};
        }
        svf.VrSetForm(form, 4);
        
        svf.VrsOut("nendo", KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　" + title); // 年度
        svf.VrsOut("PAGE1", String.valueOf(page)); // ページ(分子)
        svf.VrsOut("PAGE2", String.valueOf(allPage)); // ページ(分母)
        final int iGradeCd = Integer.parseInt(gradeCd);
        svf.VrsOut("GRADE_NAME", String.valueOf(iGradeCd) + "年"); // 学年名
        svf.VrsOut("ymd1", KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 作成日
        for (int sfi = 0; sfi < suffx.length; sfi++) {
            if (GRADE_ALL.equals(suffx[sfi])) {
                svf.VrsOut("SEMESTER" + suffx[sfi], "累計"); // 学期
                if ("2".equals(_param._output)) {
                    svf.VrAttribute("SEMESTER" + suffx[sfi], TARGET_GRADE);
                }
            } else {
                svf.VrsOut("SEMESTER" + suffx[sfi], String.valueOf(suffx[sfi]) + "年生"); // 学期
                if ("1".equals(_param._output) && (sfi + 1 == iGradeCd)) {
                    svf.VrAttribute("SEMESTER" + suffx[sfi], TARGET_GRADE);
                }
            }
        }
        svf.VrsOut("DATE_RANGE", dateRange); // 当年度出欠集計範囲
        
        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);
            final Regd ctrlRegd = student.getCtrlRegd(_param);
            if (null != ctrlRegd) {
                final String attendno = (NumberUtils.isDigits(ctrlRegd._attendNo) ? String.valueOf(Integer.parseInt(ctrlRegd._attendNo)) : StringUtils.defaultString(ctrlRegd._attendNo)) + "番";
                svf.VrsOut("NUMBER", StringUtils.defaultString(ctrlRegd._hrNameAbbv) + " " + attendno); // 番号
            }
            svf.VrsOut("SCHREGNO", student._schregno); // 番号
            svf.VrsOut("name", student._name); // 生徒氏名

            DayAttendance total = null;

            for (final Iterator rit = student._regdList.iterator(); rit.hasNext();) {
                final Regd regd = (Regd) rit.next();
                
                final String regdGrade = String.valueOf(Integer.parseInt(regd._gradeCd));
                final DayAttendance da = regd._dayAttendance;
                if (null == da) {
                    continue;
                }
                svf.VrsOut("TOTAL_PRESENT" + regdGrade, String.valueOf(da._mlesson)); // 出席すべき日数
                svf.VrsOut("TOTAL_ABSENCE" + regdGrade, String.valueOf(da._sick)); // 欠席計
                svf.VrsOut("TOTAL_ATTEND" + regdGrade, String.valueOf(da._attend)); // 出席数
                svf.VrsOut("TOTAL_LATE" + regdGrade, String.valueOf(da._late)); // 遅刻数
                svf.VrsOut("TOTAL_EARLY" + regdGrade, String.valueOf(da._early)); // 早退数
                svf.VrsOut("TOTAL_SUSPEND" + regdGrade, String.valueOf(da._suspend + da._mourning)); // 停止・忌引数
                svf.VrsOut("TOTAL_KEKKA" + regdGrade, kekka(da)); // 欠課数
                if (null == total) {
                    total = new DayAttendance();
                }
                total = total.add(da);
            }
            
            if (null != total) {
                svf.VrsOut("TOTAL_PRESENT" + GRADE_ALL, String.valueOf(total._mlesson)); // 出席すべき日数
                svf.VrsOut("TOTAL_ABSENCE" + GRADE_ALL, String.valueOf(total._sick)); // 欠席計
                svf.VrsOut("TOTAL_ATTEND" + GRADE_ALL, String.valueOf(total._attend)); // 出席数
                svf.VrsOut("TOTAL_LATE" + GRADE_ALL, String.valueOf(total._late)); // 遅刻数
                svf.VrsOut("TOTAL_EARLY" + GRADE_ALL, String.valueOf(total._early)); // 早退数
                svf.VrsOut("TOTAL_SUSPEND" + GRADE_ALL, String.valueOf(total._suspend + total._mourning)); // 停止・忌引数
                svf.VrsOut("TOTAL_KEKKA" + GRADE_ALL, kekka(total)); // 欠課数
            }
            
            svf.VrEndRecord();
        }
        for (int i = studentList.size(); i < MAX_LINE; i++) {
            svf.VrsOut("name", "\n"); // 生徒氏名
            svf.VrEndRecord();
        }
        _hasdata = true;
    }

    private String kekka(final DayAttendance da) {
        final int scale = "3".equals(_param._knjSchoolMst._absentCov) || "4".equals(_param._knjSchoolMst._absentCov) ? 1 : 0; 
        final String k;
        if (_param._isRakunan) {
            k = String.valueOf(da._mKekkaJisu); // 欠席した日の時数をひいた欠課時数を入力する
        } else {
            k = null == da._kekka ? "" : da._kekka.setScale(scale, BigDecimal.ROUND_HALF_UP).toString(); // 欠課時数総合計
        }
        return k;
    }

    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
            }
        }
        return rtn;
    }
    
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    /**
     * 
     * @param param
     * @param studentList
     * @return
     */
    private List getTargetList(final Param param, final List studentList) {
        final List targetList = new ArrayList();
        //int noTargetCount = 0;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._regdList.size() == 0) {
                log.warn(" 在籍が無い:" + student._schregno);
                continue;
            }
            boolean isAllKaikin = true;
            boolean isCtrlYearKaikin = false;
            final List attList = new ArrayList();
            for (final Iterator rit = student._regdList.iterator(); rit.hasNext();) {
                final Regd regd = (Regd) rit.next();
                final boolean targetKaikin = isTarget(student._schregno, regd._year, regd._dayAttendance, param);
                if (!targetKaikin) {
                    isAllKaikin = false;
                }
                if (param._ctrlYear.equals(regd._year)) {
                    isCtrlYearKaikin = targetKaikin;
                }
                attList.add(regd._dayAttendance);
            }
            //log.debug(" schregno = " + student._schregno + ": isAllKaikin = " + isAllKaikin + ", isCtrlYearKaikin = " + isCtrlYearKaikin + ", attList = " + attList);
            boolean isTarget = false;
            if ("2".equals(param._output)) {
                // すべて
                isTarget = isAllKaikin;
            } else if ("1".equals(param._output)) {
                if ("1".equals(param._output1Ruikei)) {
                    // ログイン年度のみ。ただし全皆勤をのぞく
                    isTarget = isCtrlYearKaikin && !isAllKaikin;
                } else {
                    // ログイン年度のみ
                    isTarget = isCtrlYearKaikin;
                }
            }
            if (isTarget) {
                targetList.add(student);
            } else {
                //noTargetCount += 1;
            }
        }
        //log.info(" noTargetCount = " + noTargetCount);
        return targetList;
    }

    private boolean isTarget(final String schregno, final String year, final DayAttendance da, final Param param) {
        if (null == da) {
            log.info(" no attend : schregno = " + schregno + ", year = " + year + ",  att = " + da);
            return false; // 出欠データのない生徒は対象外
        }
        if (da._lesson == 0) { // 対象外
            //log.info(" lesson 0 : schregno = " + schregno + ", year = " + year + ",  att = " + da);
            return false;
        }
        
        boolean isTarget;
        if (param._isBunkyo) {
            final int point = da._sick * param._bunkyoKansanCount + da._late + da._early;
            isTarget = point <= param._bunkyoKansanKesseki;
            
            log.info("  schregno = " + schregno + ", year = " + year + ",  point = " + point + ", isTarget = " + isTarget + ", att = " + da);

        } else if (param._isRakunan) {
            isTarget = da._sick <= param._kesseki
                    && (da._late + da._early) <= param._chikokuSoutai
                    && da._virus == 0
                    && da._late <= param._chikoku
                    && da._early <= param._soutai
                    && da._mKekkaJisu <= param._kekka
                    ;
        } else {
            isTarget = da._sick <= param._kesseki
                    && (da._late + da._early) <= param._chikokuSoutai
                    && da._late <= param._chikoku
                    && da._early <= param._soutai
                    && ((da._kekka == null ? 0 : da._kekka.doubleValue()) <= param._kekka)
                    ;
        }
        return isTarget; 
    }
    
    private Map[] getSchoolKinds(final DB2UDB db2) {
        List list = new ArrayList();

        String sql = "";
        sql += " SELECT T1.GRADE, T1.GRADE_CD, T1.SCHOOL_KIND, T2.GRADE AS SCHOOL_KIND_MAX_GRADE ";
        sql += " FROM SCHREG_REGD_GDAT T1 ";
        sql += " LEFT JOIN (SELECT YEAR, SCHOOL_KIND, MAX(GRADE) AS GRADE ";
        sql += "            FROM SCHREG_REGD_GDAT ";
        sql += "            GROUP BY YEAR, SCHOOL_KIND) T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        sql += " WHERE T1.YEAR = '" + _param._ctrlYear + "' ";
        sql += " ORDER BY T1.GRADE ";

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map tm = null;
                for (final Iterator it = list.iterator(); it.hasNext();) {
                    final Map m = (Map) it.next();
                    if (rs.getString("SCHOOL_KIND").equals(m.get("SCHOOL_KIND"))) {
                        tm = m;
                        break;
                    }
                }
                if (null == tm) {
                    tm = new HashMap();
                    tm.put("SCHOOL_KIND", rs.getString("SCHOOL_KIND"));
                    list.add(tm);
                }
                if (null == tm.get("GRADE_LIST")) {
                    tm.put("GRADE_LIST", new ArrayList());
                }
                final List gradeList = (List) tm.get("GRADE_LIST");
                final Map gradeMap = new HashMap();
                gradeList.add(gradeMap);
                gradeMap.put("GRADE", rs.getString("GRADE"));
                gradeMap.put("GRADE_CD", rs.getString("GRADE_CD"));
                gradeMap.put("SCHOOL_KIND", rs.getString("SCHOOL_KIND"));
                gradeMap.put("SCHOOL_KIND_MAX_GRADE", rs.getString("SCHOOL_KIND_MAX_GRADE"));
            }
        } catch (Exception e) {
            log.error("exception!" + sql, e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        
        final Map[] arr = new Map[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = (Map) list.get(i);
        }
        return arr;
    }

    private static class Regd {
        final Student _student;
        final String _year;
        final String _semester;
        final String _grade;
        final String _gradeCd;
        final String _hrClass;
        final String _hrName;
        final String _hrNameAbbv;
        final String _attendNo;

        DayAttendance _dayAttendance = null;
        public Regd(
                final Student student,
                final String year,
                final String semester,
                final String grade,
                final String gradeCd,
                final String hrClass,
                final String hrName,
                final String hrNameAbbv,
                final String attendNo) {
            _student = student;
            _year = year;
            _semester = semester;
            _grade = grade;
            _gradeCd = gradeCd;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _attendNo = attendNo;
        }
        
        private static Map getRegdMap(final List studentList) {
            final Map map = new HashMap();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                for (final Iterator git = student._regdList.iterator(); git.hasNext();) {
                    final Regd regd = (Regd) git.next();
                    final String key = regd._year + "-" + regd._semester + "-" + regd._grade + "-" + regd._hrClass;
                    if (null == map.get(key)) {
                        map.put(key, new ArrayList());
                    }
                    ((List) map.get(key)).add(regd);
                }
            }
            return map;
        }
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _sex;
        
        final List _regdList = new ArrayList();

        public Student(
                final String schregno,
                final String name, 
                final String sex) {
            _schregno = schregno;
            _name = name;
            _sex = sex;
        }
        
        public Regd getCtrlRegd(final Param param) {
            for (final Iterator it = _regdList.iterator(); it.hasNext();) {
                final Regd regd = (Regd) it.next();
                if (regd._year.equals(param._ctrlYear) && regd._semester.equals(param._ctrlSemester)) {
                    return regd;
                }
            }
            return null;
        }

        public static List loadStudentList(final DB2UDB db2, final Param param, final String grade) throws Exception {
            ResultSet rs = null;
            final List studentList = new ArrayList();
            final Map schregMap = new HashMap();
            
            try {
                // HRの生徒を取得
                final String psKey = "REGD_SQL";
                if (null == param._psMap.get(psKey)) {
                    final String sql = sqlSchregRegdDat(param);
                    log.debug("schreg_regd_dat sql = " + sql);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                PreparedStatement ps = (PreparedStatement) param._psMap.get(psKey);
                ps.setString(1, grade);
                ps.setString(2, grade);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == schregMap.get(schregno)) {
                        final Student st = new Student(
                                schregno, 
                                rs.getString("NAME"), 
                                rs.getString("SEX"));
                        schregMap.put(schregno, st);
                        studentList.add(st);
                    }
                    final Student student = (Student) schregMap.get(schregno);
                    student._regdList.add(new Regd(student, rs.getString("YEAR"), rs.getString("SEMESTER"), rs.getString("GRADE"), rs.getString("GRADE_CD"), rs.getString("HR_CLASS"), rs.getString("HR_NAME"), rs.getString("HR_NAMEABBV"), rs.getString("ATTENDNO")));
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            //log.info(" studentList size = " + studentList.size());
            return studentList;
        }
        
        /** 学生を得るSQL */
        private static String sqlSchregRegdDat(final Param param) {
            StringBuffer stb = new StringBuffer();
            
            stb.append(" WITH T_REGD0 AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._ctrlSemester + "' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1");
            stb.append("     INNER JOIN (SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER FROM SCHREG_REGD_DAT GROUP BY SCHREGNO, YEAR) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR < '" + param._ctrlYear + "' ");
            stb.append(" ), T_REGD AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     T_REGD0 T1");
            stb.append("     INNER JOIN (SELECT SCHREGNO, GRADE, MAX(YEAR) AS YEAR FROM T_REGD0 GROUP BY SCHREGNO, GRADE) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = T1.YEAR AND T4.GRADE = T1.GRADE ");
            stb.append("     INNER JOIN (SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + param._ctrlYear + "' AND GRADE = ?) T3 ON T3.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.SEX, ");
            stb.append("     T5.YEAR, ");
            stb.append("     T5.SEMESTER, ");
            stb.append("     T5.GRADE, ");
            stb.append("     REGDG.GRADE_CD, ");
            stb.append("     T5.HR_CLASS, ");
            stb.append("     T5.ATTENDNO, ");
            stb.append("     T3.HR_NAME, ");
            stb.append("     T3.HR_NAMEABBV ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     LEFT JOIN T_REGD T5 ON T5.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T5.YEAR AND REGDG.GRADE = T5.GRADE ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON ");
            stb.append("         T5.YEAR = T3.YEAR ");
            stb.append("         AND T5.SEMESTER = T3.SEMESTER ");
            stb.append("         AND T5.GRADE = T3.GRADE ");
            stb.append("         AND T5.HR_CLASS = T3.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._ctrlSemester + "' ");
            stb.append("     AND T1.GRADE = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T5.YEAR DESC ");
            return stb.toString();
        }
    }
    
    /** 1日出欠カウント */
    private static class DayAttendance {
        /** 授業日数 */
        private int _lesson;
        /** 忌引日数 */
        private int _mourning;
        /** 出停日数 */
        private int _suspend;
        private int _virus;
        private int _koudome;
        /** 出席すべき日数 */
        private int _mlesson;
        /** 欠席日数 */
        private int _sick;
        /** 出席日数 */
        private int _attend;
        /** 遅刻日数 */
        private int _late;
        /** 早退日数 */
        private int _early;
        /** 欠課時数 */
        private BigDecimal _kekka;
        
        private int _mKekkaJisu;

        public DayAttendance add(final DayAttendance a) {
            final DayAttendance n = new DayAttendance();
            n._lesson = _lesson + a._lesson;
            n._mourning = _mourning + a._mourning;
            n._suspend = _suspend + a._suspend;
            n._virus = _virus + a._virus;
            n._koudome = _koudome + a._koudome;
            n._mlesson = _mlesson + a._mlesson;
            n._sick = _sick + a._sick;
            n._attend = _attend + a._attend;
            n._late = _late + a._late;
            n._early = _early + a._early;
            n._mKekkaJisu = _mKekkaJisu + a._mKekkaJisu;
            if (null != _kekka || null != a._kekka) {
                n._kekka = (null == _kekka ? new BigDecimal(0) : _kekka).add(null == a._kekka ? new BigDecimal(0) : a._kekka);
            }
            return n;
        }
        
        public String toString() {
            DecimalFormat df5 = new DecimalFormat("000");
            return 
            "LESSON=" + df5.format(_lesson)
            + ", MOR=" + df5.format(_mourning)
            + ", SSP=" + df5.format(_suspend)
            + ", MLS=" + df5.format(_mlesson)
            + ", SCK=" + df5.format(_sick)
            + ", ATE=" + df5.format(_attend)
            + ", LAT=" + df5.format(_late)
            + ", EAL=" + df5.format(_early);
        }
        
        private static void setAttendData(final DB2UDB db2, final Param param, final Collection regdList, final String year, final String semester, final String grade, final String hrClass) {
            ResultSet rs = null;
            String sql = null;
            try {
                String psKey = "ATTEND" + year;
                if (null == param._psMap.get(psKey)) {
                    // 出欠の情報
                    String date;
                    if (year.equals(param._ctrlYear)) {
                        date = param._date;
                    } else {
                        date = String.valueOf(Integer.parseInt(year) + 1) + "-03-31";
                    }
                    sql = AttendAccumulate.getAttendSemesSql(year, semester, null, date, param._attendParamMap);
                    //log.debug(" sql = " + sql);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                    log.debug(" prepared.");
                }
                
                PreparedStatement ps = (PreparedStatement) param._psMap.get(psKey);
                ps.setString(1, grade);
                ps.setString(2, hrClass);

                final Map regdMap = new HashMap();
                for (final Iterator it = regdList.iterator(); it.hasNext();) {
                    final Regd regd = (Regd) it.next();
                    regdMap.put(regd._student._schregno, regd);
                }
                
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    
                    final Regd regd = (Regd) regdMap.get(rs.getString("SCHREGNO"));
                    if (null == regd) {
                        continue;
                    }
                    if (null == regd._dayAttendance) {
                        regd._dayAttendance = new DayAttendance();
                    }
                    
                    final int lesson   = rs.getInt("LESSON"); // 授業日数
                    final int sick     = rs.getInt("SICK"); // 病欠日数
                    final int special  = rs.getInt("MOURNING") + rs.getInt("SUSPEND") + rs.getInt("VIRUS") + rs.getInt("KOUDOME"); // 特別欠席
                    final int mlesson  = lesson - special; // 出席すべき日数
                    regd._dayAttendance._lesson   += lesson;
                    regd._dayAttendance._mourning += rs.getInt("MOURNING");
                    regd._dayAttendance._suspend  += rs.getInt("SUSPEND");
                    regd._dayAttendance._virus  += rs.getInt("VIRUS");
                    regd._dayAttendance._koudome  += rs.getInt("KOUDOME");
                    regd._dayAttendance._mlesson  += mlesson;
                    regd._dayAttendance._sick     += sick;
                    regd._dayAttendance._attend   += mlesson - sick; // 出席日数 = 出席すべき日数 - 欠席日数
                    regd._dayAttendance._late     += rs.getInt("LATE");
                    regd._dayAttendance._early    += rs.getInt("EARLY");
                    regd._dayAttendance._mKekkaJisu += rs.getInt("M_KEKKA_JISU");
                }
                DbUtils.closeQuietly(rs);

            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(rs);
            }
            
            try {
                String psKey = "ATTENDSUBCLASS" + year;
                if (null == param._psMap.get(psKey)) {
                    // 出欠の情報
                    String date;
                    if (year.equals(param._ctrlYear)) {
                        date = param._date;
                    } else {
                        date = String.valueOf(Integer.parseInt(year) + 1) + "-03-31";
                    }
                    sql = AttendAccumulate.getAttendSubclassSql(year, semester, null, date, param._attendParamMap);
                    //log.debug(" sql = " + sql);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                    log.debug(" prepared subclass.");
                }
                
                PreparedStatement ps = (PreparedStatement) param._psMap.get(psKey);
                ps.setString(1, grade);
                ps.setString(2, hrClass);

                final Map regdMap = new HashMap();
                for (final Iterator it = regdList.iterator(); it.hasNext();) {
                    final Regd regd = (Regd) it.next();
                    regdMap.put(regd._student._schregno, regd);
                }
                
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    
                    final Regd regd = (Regd) regdMap.get(rs.getString("SCHREGNO"));
                    if (null == regd) {
                        continue;
                    }
                    if (null == regd._dayAttendance) {
                        regd._dayAttendance = new DayAttendance();
                    }
                    
                    String[] split = StringUtils.split(rs.getString("SUBCLASSCD"), "-");
                    
                    // 先科目、90を超える科目を含めない
                    if ("1".equals(rs.getString("IS_COMBINED_SUBCLASS")) || split[0].compareTo("90") > 0) {
                        continue;
                    }
                    if (null == regd._dayAttendance._kekka) {
                        regd._dayAttendance._kekka = new BigDecimal(0);
                    }
                    regd._dayAttendance._kekka = regd._dayAttendance._kekka.add(rs.getBigDecimal("SICK2"));
                }
                DbUtils.closeQuietly(rs);

            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(rs);
            }
        }
    }
    
    private static class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _date;
        final String _ctrlDate;
        final String _outputKaikin; // 1:皆勤者 2:精勤者
        final String _output; // 1:学年皆勤者 2:累計皆勤者
        final String _output1Ruikei; // 1:学年皆勤者指定の際、累計皆勤者を除く
        final int _chikokuSoutai;
        final int _chikoku;
        final int _soutai;
        final int _kesseki;
        final int _kekka;
        final String _documentroot;
        final boolean _isRakunan;
        final boolean _isBunkyo;
        final int _bunkyoKansanCount;
        final int _bunkyoKansanKesseki;

        private KNJSchoolMst _knjSchoolMst;

        final Map _psMap;
        final Map _attendParamMap;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester  = request.getParameter("CTRL_SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _ctrlDate = null == request.getParameter("CTRL_DATE") ? null : request.getParameter("CTRL_DATE").replace('/', '-');

            _outputKaikin = request.getParameter("OUTPUT_KAIKIN");
            _output = request.getParameter("OUTPUT");
            _output1Ruikei = request.getParameter("OUTPUT1_RUIKEI");
            _chikokuSoutai = defval(request.getParameter("CHIKOKU_SOUTAI"), 9999);
            _chikoku = defval(request.getParameter("CHIKOKU"), 9999);
            _soutai = defval(request.getParameter("SOUTAI"), 9999);
            _kesseki = defval(request.getParameter("KESSEKI"), 9999);
            _kekka = defval(request.getParameter("KEKKA"), 9999);
            _bunkyoKansanCount = defval(request.getParameter("bunkyoKansanCount"), 9999);
            _bunkyoKansanKesseki = defval(request.getParameter("BUNKYO_KANSAN_KESSEKI"), 9999);
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            }
            _documentroot = request.getParameter("DOCUMENTROOT");
            final String z010 = getZ010(db2);
            log.info(" z010 = " + z010);
            _isRakunan = "rakunan".equals(z010);
            _isBunkyo = "bunkyo".equals(z010);
            _psMap = new HashMap();
            
            // 出欠の情報
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("absenceDiv", "2");
            _attendParamMap.put("grade", "?");
            _attendParamMap.put("hrClass", "?");
        }
        
        private static int defval(final String val, final int def) {
            return NumberUtils.isDigits(val) ? Integer.parseInt(val) : def;
        }
        
        private static String getZ010(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                final String sql = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        
        private static String getSemester1Sdate(final DB2UDB db2, final String year, final String grade) {
            final String[] semes = {"1", "9"};
            boolean hasRecord = false;
            String rtn = null;
            try {
                hasRecord = false;
                for (int i = 0; i < semes.length; i++) {
                    final String sql = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semes[i] + "' ORDER BY SEMESTER";
                    for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                        final Map row = (Map) it.next();
                        final String sdate = KnjDbUtils.getString(row, "SDATE");
    					rtn = sdate;
    					log.debug("set " + year + "-" + semes[i] + " sdate = " + sdate);
    					if (null != sdate) {
    						hasRecord = true;
    					}
                    }
                    if (hasRecord) {
                        break;
                    }
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            }
            if (null != grade) {
                try {
                    hasRecord = false;
                    for (int i = 0; i < semes.length; i++) {
                        final String sql = "SELECT GRADE, SDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semes[i] + "' AND GRADE = '" + grade + "' ORDER BY SEMESTER";
                        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                            final Map row = (Map) it.next();
                            final String sdate = KnjDbUtils.getString(row, "SDATE");
                            rtn = sdate;
                            if (null != sdate) {
                            	hasRecord = true;
                            }
                        }
                        if (hasRecord) {
                            break;
                        }
                    }
                } catch (Exception ex) {
                    log.error("V_SEMESTER_GRADE_MST取得エラー:", ex);
                }
            }
            return rtn;
        }
    }
}

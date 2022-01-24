// kanji=漢字
/*
 * $Id: 6f4e316fa2ed95731ac1343dd3bde153f8231721 $
 *
 * 作成日: 2009/09/24
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 仮評定一覧表
 * @version $Id: 6f4e316fa2ed95731ac1343dd3bde153f8231721 $
 */
public class KNJD619 {
    private static final Log log = LogFactory.getLog(KNJD619.class);

    private Param _param;
    
    private int page = 0;

    /**
     *  KNJD.classから最初に起動されます。
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        // ＤＢ接続
        DB2UDB db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
            return;
        }

        boolean hasData = false;

        try {
            sd.setSvfInit(request, response, svf);

            // パラメータの取得
            _param = createParam(request, db2);

            final List hrInfos = getHRInfos(db2);  //印刷対象HR組
            
            page = 0;

            for (Iterator it = hrInfos.iterator(); it.hasNext();) {
                HRInfo hrInfo = (HRInfo) it.next();
                hrInfo.loadPrevValue(db2);

                log.debug(" HRInfo =" + hrInfo);
                
                // 印刷処理
                hasData = printMain(svf, hrInfo) || hasData;
            }
            
            log.debug("hasData = " + hasData);
            
        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                sd.closeDb(db2);
            }
            if (null != svf) {
                sd.closeSvf(svf, hasData);
            }
        }
    }
    
    private List getHRInfos(final DB2UDB db2) {
        
        final List hrs = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sqlHrInfo(_param._year, _param._semester, _param._ctrlSemester, _param._classSelected);
            log.debug(" schreg regd sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            int line = 1;
            
            while(rs.next()) {
                
                final String hrClass = rs.getString("HR_CLASS");
                
                HRInfo hrInfo = null;
                for (Iterator it = hrs.iterator(); it.hasNext();) {
                    HRInfo hrInfo1 = (HRInfo) it.next();
                    if (hrInfo1._hrclassCd.equals(hrClass)) {
                        hrInfo = hrInfo1;
                    }
                }
                if (hrInfo == null) {
                    final String hrName = rs.getString("HR_NAME");
                    final String trName = rs.getString("TR_NAME");
                    hrInfo = new HRInfo(hrClass, trName, hrName);
                    hrs.add(hrInfo);
                    line = 1;
                }
                
                final String schregno = rs.getString("SCHREGNO");
                final String attendNo = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEXNAME");
                
                Student student = new Student(schregno, attendNo, name, sex, hrInfo, line);
                hrInfo._students.add(student);
                
                line += 1;
                
                if (line > _param._formMaxLine) {
                    line = 1;
                }
            }
            
        } catch (SQLException ex) {
            log.error("sqlexception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return hrs;
    }
    
    private String sqlHrInfo(String year, String semester, String ctrlSemester, String[] classSelected) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.GRADE || T2.HR_CLASS AS HR_CLASS, ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T3.NAME, ");
        stb.append("     T3.SEX, ");
        stb.append("     T5.NAME2 AS SEXNAME, ");
        stb.append("     T4.STAFFNAME AS TR_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T2 ON ");
        stb.append("         T1.YEAR = T2.YEAR ");
        stb.append("         AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("         AND T1.GRADE = T2.GRADE ");
        stb.append("         AND T1.HR_CLASS = T2.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON ");
        stb.append("         T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN STAFF_MST T4 ON ");
        stb.append("         T4.STAFFCD = T2.TR_CD1 ");
        stb.append("     LEFT JOIN NAME_MST T5 ON ");
        stb.append("         T5.NAMECD1 = 'Z002' ");
        stb.append("         AND T5.NAMECD2 = T3.SEX ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        if ("9".equals(semester)) {
            stb.append("     AND T1.SEMESTER = '" + ctrlSemester + "' ");
        } else {
            stb.append("     AND T1.SEMESTER = '" + semester + "' ");
        }
        stb.append("     AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, classSelected)+ " ");
        stb.append(" ORDER BY T2.GRADE, T2.HR_CLASS, T1.ATTENDNO");
        return stb.toString();
    }
    
    /**
     * 生徒の印刷。
     * @param svf
     * @param hrInfo：年組
     * @return データがあるか
     */
    private boolean printMain(
            final Vrw32alp svf,
            final HRInfo hrInfo
    ) {
        boolean hasData = false;
        
        List studentListList = new ArrayList();
        
        if (hrInfo._students.size() > _param._formMaxLine) {
            // 1ページを
            final int size = hrInfo._students.size();
            
            studentListList.add(hrInfo._students.subList(0, _param._formMaxLine));
            studentListList.add(hrInfo._students.subList(_param._formMaxLine, size));
        } else if (hrInfo._students.size() > 0){
            studentListList.add(hrInfo._students);
        }
        
        for (Iterator it3 = studentListList.iterator(); it3.hasNext();) {
            
            List students = (List) it3.next();

            int column = 1;
            for (Iterator it2 = hrInfo._subclasses.keySet().iterator(); it2.hasNext();) {
                final String subclassCd = (String) it2.next();
                final SubClass subclass = (SubClass) hrInfo._subclasses.get(subclassCd);
                
                if (column == 1) {
                    page += 1;
                    setForm(svf, hrInfo._hrName, students);
                    svf.VrsOut("PAGE", String.valueOf(page));
                }
                
                log.debug(subclass.toString());
                
                svf.VrsOut("COURSE", subclass._classabbv); //教科名
                svf.VrsOut("SUBJECT", subclass._subclassabbv); //科目名
                
                for (Iterator it = students.iterator(); it.hasNext();) {
                    Student student = (Student) it.next();
                    hasData = student.printSubclass(svf, subclassCd) || hasData;
                }
                svf.VrEndRecord();
                
                column += 1;
                if (column > _param._formMaxColumn) {
                    column = 1;
                }
            }
        }

        return hasData;
    }

    /**
     * フォームをセットして生徒名称等を出力する
     * @param svf
     * @param hrName HRクラスの名称
     * @param students 出力する生徒
     */
    private void setForm(Vrw32alp svf, String hrName, List students) {
        svf.VrSetForm(_param.getFormFile(), 4);

        svf.VrsOut("YEAR", nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
        
        svf.VrsOut("TITLE", _param._semesterName + "　" + "仮評定一覧表");
        svf.VrsOut("TESTITEM", "（" + _param._testItemName + "）");
        
        svf.VrsOut("YMD", KNJ_EditDate.h_format_JP(_param._loginDate)); // 作成日
        svf.VrsOut("HR_NAME", hrName);  //組名称
        svf.VrsOut("PAGE", String.valueOf(page));

        for (Iterator it = students.iterator(); it.hasNext();) {
            Student student = (Student) it.next();

            //log.debug(" student = " + student);

            int i = student._line;
            svf.VrsOutn("NUMBER", i, String.valueOf(i)); // No.
            svf.VrsOutn("CLASS", i, hrName);  //組名称
            svf.VrsOutn("FIELD2", i, (student._attendNo == null) ? "" : String.valueOf(Integer.parseInt(student._attendNo))); // 出席番号
            svf.VrsOutn("NAME", i, student._name); // 生徒名
            svf.VrsOutn("SEX", i, student._sex); // 性別

            svf.VrsOutn("SC_AVE", student._line, student.getAverageValue());
        }
    }

    /**
     * 仮評定データのSQL
     */
    private String sqlStdSubclassProvvalue(String hrclassCd) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD,");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("       T1.SUBCLASSCD AS SUBCLASSCD, T1.SCHREGNO, T1.VALUE ");
        stb.append("     , T3.SUBCLASSNAME, T4.CLASSNAME, T4.ELECTDIV ");
        stb.append(" FROM ");
        stb.append("     RECORD_PROV_RATE_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
        stb.append("        T2.YEAR = T1.YEAR AND ");
        if ("9".equals(_param._semester)) {
            stb.append("        T2.SEMESTER = '" + _param._ctrlSemester + "' AND ");
        } else {
            stb.append("        T2.SEMESTER = T1.SEMESTER AND ");
        }
        stb.append("        T2.SCHREGNO = T1.SCHREGNO AND ");
        stb.append("        T2.GRADE || T2.HR_CLASS = '" + hrclassCd + "' ");
        stb.append("     INNER JOIN SUBCLASS_MST T3 ON ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
        }
        stb.append("        T3.SUBCLASSCD = ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("        T1.SUBCLASSCD ");
        stb.append("     INNER JOIN CLASS_MST T4 ON ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        T4.CLASSCD || '-' || T4.SCHOOL_KIND = ");
            stb.append("        T3.CLASSCD || '-' || T3.SCHOOL_KIND ");
        } else {
            stb.append("        T4.CLASSCD = SUBSTR(T3.SUBCLASSCD, 1, 2) ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testKindItem + "' ");

        return stb.toString();
    }
    
    /**
     * 科目クラスの取得（教科名・科目名・単位・授業時数）
     * @param rs 生徒別科目別明細
     * @return 科目のクラス
     */
    SubClass getSubClass(
            final ResultSet rs,
            Map subclasses
    ) {
        String subclasscode = null;
        String classabbv = null;
        String subclassabbv = null;
        boolean electdiv = false;

        try {
            subclasscode = rs.getString("SUBCLASSCD");
            classabbv = rs.getString("CLASSNAME");
            subclassabbv = rs.getString("SUBCLASSNAME");
            electdiv = "1".equals(rs.getString("ELECTDIV"));
        } catch (SQLException e) {
             log.error("SQLException", e);
        }

        //科目クラスのインスタンスを更新して返す
        if (subclasses.containsKey(subclasscode)) {
            SubClass subclass = (SubClass) subclasses.get(subclasscode);
            return subclass;
        }

        final SubClass subClass = new SubClass(subclasscode, classabbv, subclassabbv, electdiv);
        subclasses.put(subclasscode, subClass);
        return subClass;
    }

    /**
     * 学級
     */
    private class HRInfo implements Comparable {
        private final String _hrclassCd;
        private final String _staffName;
        private final String _hrName;

        private final List _students = new LinkedList();
        private final Map _subclasses = new TreeMap();

        HRInfo(final String hrclassCd,
                final String staffName,
                final String hrName
        ) {
            _hrclassCd = hrclassCd;
            _staffName = staffName;
            _hrName = hrName;
        }

        private Student getStudent(String schregno) {
            if (schregno == null) {
                return null;
            }
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (schregno.equals(student._schregno)) {
                    return student;
                }
            }
            return null;
        }

        private void loadPrevValue(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdSubclassProvvalue(_hrclassCd);
                log.debug(" subclass provvalue sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (student == null) {
                        continue;
                    }
                    
                    String subclassCd = rs.getString("SUBCLASSCD");

                    HRInfo hrInfo = student._hrInfo;
                    getSubClass(rs, hrInfo._subclasses);

                    Integer provValue = rs.getString("VALUE") == null ? null : Integer.valueOf(rs.getString("VALUE"));

                    student.add(subclassCd, provValue);
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public int compareTo(final Object o) {
            if (!(o instanceof HRInfo)) return -1;
            final HRInfo that = (HRInfo) o;
            return _hrclassCd.compareTo(that._hrclassCd);
        }

        public String toString() {
            return _hrName + "[" + _staffName + "]";
        }
    }


    private class Student implements Comparable {

        private final String _schregno;  // 学籍番号
        private final HRInfo _hrInfo;
        private final String _attendNo;
        private final String _name;
        private final String _sex;
        private final int _line;
        
        private final Map _provValues = new TreeMap();
        
        Student(final String schregno, final String attendNo, final String name, final String sex, final HRInfo hrInfo, final int line) {
            _schregno = schregno;
            _hrInfo = hrInfo;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
            _line = line;
        }

        private boolean printSubclass(Vrw32alp svf, String subclassCd) {
            boolean hasData = false;

            if (_provValues.containsKey(subclassCd)) {
                Integer provValue = (Integer) _provValues.get(subclassCd);
                
                if (provValue != null) {
                    svf.VrsOut("SCORE" + _line, provValue.toString());
                    //log.debug("  schregno = " + _schregno + " , provValue = " + provValue);
                }
                hasData = true;
            }

            return hasData;
        }

        private void add(final String subclassCd,  final Integer provValue) { 
            _provValues.put(subclassCd, provValue);
        }

        /**
         * 出席番号順にソートします。
         * {@inheritDoc}
         */
        public int compareTo(final Object o) {
            if (!(o instanceof Student)) return -1;
            final Student that = (Student) o;
            int rtn;
            rtn = _hrInfo.compareTo(that._hrInfo);
            if (0 != rtn) return rtn;
            rtn = _attendNo.compareTo(that._attendNo);
            return rtn;
        }

        public String toString() {
            return _attendNo + ":" + _name;
        }
        
        public String getAverageValue() {
            int sum = 0;
            int count = 0;
            
            for (Iterator it = _provValues.keySet().iterator(); it.hasNext();) {
                String subclassCd = (String) it.next();
                Integer value = (Integer) _provValues.get(subclassCd);
                if (value != null) {
                    sum += value.intValue();
                    count += 1;
                }
            }
            final String avgStr = (count == 0) ? "0.0" : new BigDecimal(sum).divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP).toString();
            //log.debug(" schregno = "+ _schregno + " => avg = "+ avgStr);
            return avgStr;
        }
    }

    private class SubClass {
        private final String _classabbv;
        private final String _classcode;
        private final String _subclasscode;
        private final String _subclassabbv;
        private final boolean _electdiv; // 選択科目

        SubClass(
                final String subclasscode, 
                final String classabbv, 
                final String subclassabbv,
                final boolean electdiv
        ) {
            _classabbv = classabbv;
            _classcode = subclasscode.substring(0, 2); 
            
            _subclasscode = subclasscode;
            _subclassabbv = subclassabbv;
            _electdiv = electdiv;
        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof SubClass)) return false;
            final SubClass that = (SubClass) obj;
            return _subclasscode.equals(that._subclasscode);
        }

        public int hashCode() {
            return _subclasscode.hashCode();
        }

        public String toString() {
            return "["+_classabbv + " , " +_subclasscode + " , " +_subclassabbv + "]";
        }
    }
    
    private Param createParam(HttpServletRequest request, DB2UDB db2) {
        
        KNJServletUtils.debugParam(request, log);

        Param param = new Param(request);
        param.load(db2);
        
        return param;
    }

    private class Param {

        /** 年度 */
        final String _year;

        /** 学期 */
        final String _semester;

        /** ログイン学期 */
        final String _ctrlSemester;

        /** 学年 */
        final String _grade;
        
        /** テスト種別項目 */
        final String _testKindItem;

        final String _loginDate;

        /** フォーム1ページの生徒人数 */
        final String _formSelect;
        
        /** フォーム1ページの科目数 */
        final String _subclassMax;

        final int _formMaxColumn;
        final int _formMaxLine;

        final String[] _classSelected;
        
        final String _useCurriculumcd;

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        private KNJSchoolMst _knjSchoolMst;
        
        private String _testItemName;
        private String _semesterName;
        
        Param(final HttpServletRequest request) {

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("SEME_DATE");
            _grade = request.getParameter("GRADE");
            _loginDate = request.getParameter("LOGIN_DATE");

            _classSelected = request.getParameterValues("CLASS_SELECTED");
            
            _subclassMax = request.getParameter("SUBCLASS_MAX");
            _formSelect = request.getParameter("FORM_SELECT");

            _formMaxColumn = "1".equals(_subclassMax) ? 15 : 20;
            _formMaxLine = "1".equals(_formSelect) ? 45 : 50 ;

            _testKindItem = request.getParameter("TESTKINDCD");   //テスト・成績種別
            
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            
            log.debug(" semester + testKindCd = " + _semester  + _testKindItem);
        }

        public String getFormFile() {
            if ("1".equals(_formSelect)) {
                return "1".equals(_subclassMax) ? "KNJD619_1.frm" : "KNJD619_3.frm";
            } else {
                return "1".equals(_subclassMax) ? "KNJD619_2.frm" : "KNJD619_4.frm";
            }
        }

        public void load(DB2UDB db2) {
            _definecode = new KNJDefineSchool();
            _definecode .defineCode(db2, _year);         //各学校における定数等設定
            log.debug("semesdiv=" + _definecode.semesdiv + "   absent_cov=" + _definecode.absent_cov + "   absent_cov_late=" + _definecode.absent_cov_late);

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            try {
                _testItemName = "";
                
                final String sql = " SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW "
                    + "WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND TESTKINDCD || TESTITEMCD = '" + _testKindItem +"' ";
                
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                if (rs.next()) {
                    _testItemName = rs.getString("TESTITEMNAME");
                }
            } catch (SQLException e) {
                log.warn("テスト項目名称取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            try {
                _semesterName = "";
                
                final String sql = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
                log.debug(" semester sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                if (rs.next()) {
                    _semesterName = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException e) {
                log.warn("学期名称取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        }
    }
}

// eof

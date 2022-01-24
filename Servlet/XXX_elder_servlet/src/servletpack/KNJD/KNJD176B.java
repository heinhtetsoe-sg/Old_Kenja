/**
 *
 * 学校教育システム 賢者 [成績管理]  通知票(智辯高校)
 * @author nakasone
 * @version $Id: 24aec68e13e93c87fa86580b966d9c0fdfffe173 $
 *
 */

package servletpack.KNJD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJSvfFieldModify;
import servletpack.KNJZ.detail.dao.AttendAccumulate;


public class KNJD176B {

    private static final Log log = LogFactory.getLog(KNJD176B.class);
    private int ret;
    private int skekka1;                //欠課時数(出欠状況)
    private int skekka2;                //欠課時数(出欠状況)
    private int skekka9;                //欠課時数(出欠状況)
    private KNJObjectAbs knjobj;        //編集用クラス

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定
    private KNJSvfFieldModify svfobj;   // フォームのフィールド属性変更
    private static final String FORM_FILE_1 = "KNJD176B_1.frm";
    private static final String FORM_FILE_2 = "KNJD176B_2.frm";

    Param _param;

    /**
     *
     *  KNJD.classから最初に起動されるクラス
     *
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        boolean nonedata = false;

        // print svf設定
        sd.setSvfInit( request, response, svf);
        // ＤＢ接続
        db2 = sd.setDb(request);
        if( sd.openDb(db2) ){
            log.error("db open error! ");
            return;
        }
        // パラメータの取得
        _param = createParam(request, db2);
        // 印刷処理
        nonedata = printSvf(request, db2, svf);
        // 終了処理
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final Param param = new Param(request, db2);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _hrClass;
        private final String _grade;
        private final String[] _schregno;
        private final String _schregnoIn;
        private final String _date;
        private final String _gengou;
        private final String _groupDiv;

        private String _hrName;
        private String _staffName;
        private String _schoolName;
        private String _remark2;
        private boolean _useRankAvg;
        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;

        final Map _attendParamMap = new HashMap();
        
        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _hrClass = request.getParameter("GRADE_HR_CLASS");
            _grade = request.getParameter("GRADE_HR_CLASS").substring(0, 2);
            _schregno = request.getParameterValues("category_selected");
            _schregnoIn = setSchregnoIn(request.getParameterValues("category_selected"));
            _date = request.getParameter("DATE").replace('/', '-');
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(request.getParameter("YEAR")));
            _gengou = gengou + "年度";
            _groupDiv = request.getParameter("GROUP_DIV");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
        }

        private String setSchregnoIn(final String schno[]){
            final StringBuffer rtn = new StringBuffer();
            for (int ia = 0; ia < schno.length; ia++) {
                if (ia==0) rtn.append("('");
                else       rtn.append("','");
                rtn.append(schno[ia]);
            }
            rtn.append("')");
            return rtn.toString();
        }

        public void load(final DB2UDB db2) throws SQLException {
            createStaffName(db2);
            createSchoolInfo(db2);
            setUseRankAvg();
        }

        private void createStaffName(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlStaffName());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _hrName = rs.getString("HR_NAME");
                    _staffName = rs.getString("STAFFNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("組名称：" + _hrName + "、担任名：" + _staffName);
        }

        private String sqlStaffName() {
            return ""
                    + " SELECT  T1.HR_NAME, T2.STAFFNAME"
                    + " FROM    SCHREG_REGD_HDAT T1"
                    + "         LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1"
                    + " WHERE   T1.YEAR = '" + _year + "'"
                    + "   AND   T1.SEMESTER = '" + _semester + "'"
                    + "   AND   T1.GRADE || T1.HR_CLASS = '" + _hrClass + "'"
                ;
        }

        private void createSchoolInfo(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlSchoolInfo());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _remark2 = rs.getString("REMARK2");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("学校名：" + _schoolName + "、職種名：" + _remark2);
        }

        private String sqlSchoolInfo() {
            return ""
                    + " SELECT  SCHOOL_NAME, REMARK2"
                    + " FROM    CERTIF_SCHOOL_DAT"
                    + " WHERE   YEAR = '" + _year + "'"
                    + "   AND   CERTIF_KINDCD = '" + getCertifKindcd() + "'"
                ;
        }

        private void setUseRankAvg() {
            if (isJunior()) {
                _useRankAvg = false;
            } else {
                _useRankAvg = true;
            }
        }

        private String getCertifKindcd() {
            return isJunior() ? "103" : "104";
        }

        public String getSchoolStaffName() {
            return _remark2 != null ? _remark2 + _staffName : "";
        }

        public boolean isJunior() {
            return Integer.parseInt(_grade) < 4;
        }

        public int getGradeInt() {
            return isJunior() ? Integer.parseInt(_grade) : Integer.parseInt(_grade) - 3;
        }

        public String getGradeName() {
            return "第" + String.valueOf(getGradeInt()) + "学年";
        }

        public String getRankItemName() {
            return isRankGrade() ? "学年" : "コース";
        }

        public String getAvgDiv() {
            return isRankGrade() ? "1" : "3";
        }

        public boolean isRankGrade() {
            return _groupDiv.equals("1");
        }

        public String getRecordSemester() {
            return isGakunenmatu() ? "9" : _semester;
        }

        public boolean isGakunenmatu() {
            return _semester.equals("3");
        }
    }

    /**
     *  印刷処理
     */
    private boolean printSvf( HttpServletRequest request, DB2UDB db2, Vrw32alp svf )
    {
        boolean nonedata = false;
        try {
            _param.load(db2);
            setSvfForm(svf);
            if( printMain( db2, svf) )nonedata = true;        //SVF-FORM出力処理
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return nonedata;
    }


    /** 
     *  SVF-FORM SET
     *  
     */
    private void setSvfForm(Vrw32alp svf)
    {
        try {
            if (!_param.isJunior()) {
                // 年次が4年以上の場合は高校フォーム
                svf.VrSetForm( FORM_FILE_1, 1 );
            } else {
                // 年次が4年未満の場合は中学フォーム
                svf.VrSetForm( FORM_FILE_2, 1 );
            }
        } catch( Exception ex ){
            log.error("error! ", ex);
        }
    }

    /** 印刷処理メイン */
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws Exception {
        boolean rtnflg = false;

        final List students = createStudents(db2);
        log.debug("生徒数=" + students.size());

        knjobj = new KNJEditString();
        _param._attendParamMap.put("schregno", "?");
        String prestatementAttendSemes = AttendAccumulate.getAttendSemesSql(
                _param._year,
                _param._semester,
                null,
                _param._date,
                _param._attendParamMap
        );
        log.debug("AttendAccumulate.getAttendSemesSql sql = " + prestatementAttendSemes);
        PreparedStatement ps2 = db2.prepareStatement(prestatementAttendSemes);                        //出欠データ

        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            //ヘッダ等
            printHeader(svf, student);
            //成績明細データを出力
            skekka1 = 0;
            skekka2 = 0;
            skekka9 = 0;
//            printSvfRecDetail( svf, arrps[1], student._schregno );
            printSubClass(db2, svf, student);
            //順位等を出力
            printRank(db2, svf, student);
            printScoreMaxMin(db2, svf, student);
            //出欠データを出力
            printSvfAttend( svf, ps2, student._schregno );
            //出欠データ(備考)を出力
            printSvfRemark(db2, svf, student._schregno );
            //備考を出力
            printSvfBiko(db2, svf, student._schregno );
            //学校名・担任名を出力
            printSvfScool(svf);
            
            svf.VrEndPage();
            rtnflg = true;
        }
        
        DbUtils.closeQuietly(ps2);

        return rtnflg;
    }

    private void printHeader(final Vrw32alp svf, final Student student) {
        svf.VrsOut("NENDO" , _param._gengou);
        svf.VrsOut("HR_NAME" , student._hrName);
        svf.VrsOut("GRADE" , _param.getGradeName());
        svf.VrsOut("ATTENDNO" , student.getAttendNo());
        svf.VrsOut("NAME" , student._name);
        svf.VrsOut("ITEM" , _param.getRankItemName());
    }

    private void printRank(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlRank(student);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String semester = rs.getString("SEMESTER");
                String gradeRank = rs.getString("GRADE_RANK");
                String classRank = rs.getString("CLASS_RANK");
                String courseRank = rs.getString("COURSE_RANK");

                log.debug("semester="+semester+", gradeRank="+gradeRank+", classRank="+classRank+", courseRank="+courseRank);

                int gyo = semester.equals("9") ? 3 : Integer.parseInt(semester);
                svf.VrsOutn("CLASS_RANK" , gyo, classRank);
                svf.VrsOutn("RANK" , gyo, _param.isRankGrade() ? gradeRank : courseRank);
            }
        } catch (final Exception ex) {
            log.error("順位のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlRank(final Student student) {
        final StringBuffer stb = new StringBuffer();
        final String gradeRank = _param._useRankAvg ? "GRADE_AVG_RANK" : "GRADE_RANK";
        final String classRank = _param._useRankAvg ? "CLASS_AVG_RANK" : "CLASS_RANK";
        final String courseRank = _param._useRankAvg ? "COURSE_AVG_RANK" : "COURSE_RANK";
        
        stb.append(" SELECT ");
        stb.append("     SEMESTER, ");
        stb.append("     SCHREGNO, ");
        stb.append(" " + gradeRank + " AS GRADE_RANK, ");
        stb.append(" " + classRank + " AS CLASS_RANK, ");
        stb.append(" " + courseRank + " AS COURSE_RANK ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR='" + _param._year + "' AND ");
        stb.append("     SEMESTER not in('3') AND ");
        stb.append("     SEMESTER <= '" + _param.getRecordSemester() + "' AND ");
        stb.append("     TESTKINDCD='99' AND ");
        stb.append("     TESTITEMCD='00' AND ");
        stb.append("     SUBCLASSCD='999999' AND ");
        stb.append("     SCHREGNO='" + student._schregno + "' ");
        stb.append(" ORDER BY ");
        stb.append("     SEMESTER ");

        return stb.toString();
    }

    private void printScoreMaxMin(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlScoreMaxMin(student);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String avgDiv = rs.getString("AVG_DIV");
                String semester = rs.getString("SEMESTER");
                String count = rs.getString("COUNT");

                log.debug("avgDiv="+avgDiv+", semester="+semester+", count="+count);

                int gyo = semester.equals("9") ? 3 : Integer.parseInt(semester);
                String field = avgDiv.equals("2") ? "CLASS_COUNT" : "COUNT";
                svf.VrsOutn(field , gyo, count);
            }
        } catch (final Exception ex) {
            log.error("人数のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlScoreMaxMin(final Student student) {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO AS ( ");
        stb.append("     SELECT ");
        stb.append("         SEMESTER, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         COURSECD || MAJORCD || COURSECODE as COURSE ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT ");
        stb.append("     WHERE ");
        stb.append("         SCHREGNO='" + student._schregno + "' AND ");
        stb.append("         YEAR='" + _param._year + "' ");
        stb.append("     ) ");

        stb.append(" SELECT");
        stb.append("     AVG_DIV,");
        stb.append("     SEMESTER,");
        stb.append("     COUNT");
        stb.append(" FROM");
        stb.append("     RECORD_AVERAGE_DAT");
        stb.append(" WHERE");
        stb.append("     YEAR='" + _param._year + "' AND");
        stb.append("     SEMESTER not in ('3') AND");
        stb.append("     SEMESTER <= '" + _param.getRecordSemester() + "' AND");
        stb.append("     TESTKINDCD='99' AND");
        stb.append("     TESTITEMCD='00' AND");
        stb.append("     SUBCLASSCD='999999' AND");
        stb.append("     AVG_DIV='" + _param.getAvgDiv() + "' AND");
        stb.append("     GRADE='" + student._grade + "' AND");
        stb.append("     HR_CLASS='000' AND");
        if (_param.isRankGrade()) {
            stb.append(" COURSECD='0' AND");
            stb.append(" MAJORCD='000' AND");
            stb.append(" COURSECODE='0000'");
        } else {
            stb.append(" ((");
            stb.append(" SEMESTER = '1' AND COURSECD || MAJORCD || COURSECODE in (SELECT COURSE FROM SCHNO WHERE SEMESTER='1')");
            stb.append(" ) OR (");
            stb.append(" SEMESTER = '2' AND COURSECD || MAJORCD || COURSECODE in (SELECT COURSE FROM SCHNO WHERE SEMESTER='2')");
            stb.append(" ) OR (");
            stb.append(" SEMESTER = '9' AND COURSECD || MAJORCD || COURSECODE in (SELECT COURSE FROM SCHNO WHERE SEMESTER='3')");
            stb.append(" ))");
        }
        stb.append(" UNION");
        stb.append(" SELECT");
        stb.append("     AVG_DIV,");
        stb.append("     SEMESTER,");
        stb.append("     COUNT");
        stb.append(" FROM");
        stb.append("     RECORD_AVERAGE_DAT");
        stb.append(" WHERE");
        stb.append("     YEAR='" + _param._year + "' AND");
        stb.append("     SEMESTER not in ('3') AND");
        stb.append("     SEMESTER <= '" + _param.getRecordSemester() + "' AND");
        stb.append("     TESTKINDCD='99' AND");
        stb.append("     TESTITEMCD='00' AND");
        stb.append("     SUBCLASSCD='999999' AND");
        stb.append("     AVG_DIV='2' AND");
        stb.append("     GRADE='" + student._grade + "' AND");
        stb.append("     COURSECD='0' AND");
        stb.append("     MAJORCD='000' AND");
        stb.append("     COURSECODE='0000' AND");
        stb.append(" ((");
        stb.append(" SEMESTER = '1' AND HR_CLASS in (SELECT HR_CLASS FROM SCHNO WHERE SEMESTER='1')");
        stb.append(" ) OR (");
        stb.append(" SEMESTER = '2' AND HR_CLASS in (SELECT HR_CLASS FROM SCHNO WHERE SEMESTER='2')");
        stb.append(" ) OR (");
        stb.append(" SEMESTER = '9' AND HR_CLASS in (SELECT HR_CLASS FROM SCHNO WHERE SEMESTER='3')");
        stb.append(" ))");
        stb.append(" ORDER BY");
        stb.append("     AVG_DIV, SEMESTER");
        return stb.toString();
    }

    private void printSubClass(final DB2UDB db2, final Vrw32alp svf, final Student student) throws SQLException {
        final List subClasses = createSubClass(db2, student);
        int gyo = 0;
        for (final Iterator it = subClasses.iterator(); it.hasNext();) {
            final SubClass subClass = (SubClass) it.next();
            gyo++;
            svf.VrsOutn("CLASS1"    ,gyo ,  subClass._className); //教科
            svf.VrsOutn("SUBCLASS1" ,gyo ,  subClass._subclassName); //科目
            svf.VrsOutn("CREDIT" ,gyo ,  subClass._credits); //履修単位
            //成績
            printScore(db2, svf, student, subClass, gyo);
            //評定
            if (_param.isGakunenmatu()) printValue(db2, svf, student, subClass, gyo);
        }
    }

    private List createSubClass(final DB2UDB db2, final Student student) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlSubClass(student);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String classCd = rs.getString("CLASSCD");
                final String className = rs.getString("CLASSNAME");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String credits = rs.getString("CREDITS");

                final SubClass subclass = new SubClass(classCd, className, subclassCd, subclassName, credits);
                rtn.add(subclass);
            }
        } catch (final Exception ex) {
            log.error("科目のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlSubClass(final Student student) {
        StringBuffer stb = new StringBuffer();

        //講座名簿
        stb.append(" WITH T_CHAIR AS (");
        stb.append("     SELECT");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T2.CLASSCD, ");
            stb.append("         T2.SCHOOL_KIND, ");
            stb.append("         T2.CURRICULUM_CD, ");
        }
        stb.append("         T2.SUBCLASSCD");
        stb.append("     FROM");
        stb.append("         CHAIR_STD_DAT T1,");
        stb.append("         CHAIR_DAT T2");
        stb.append("     WHERE");
        stb.append("         T1.YEAR='" + _param._year + "' AND");
        stb.append("         T1.SEMESTER <= '" + _param._semester + "' AND");
        stb.append("         T1.SCHREGNO='" + student._schregno + "' AND");
        stb.append("         T2.YEAR=T1.YEAR AND");
        stb.append("         T2.SEMESTER=T1.SEMESTER AND");
        stb.append("         T2.CHAIRCD=T1.CHAIRCD");
        stb.append("     GROUP BY");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T2.CLASSCD, ");
            stb.append("         T2.SCHOOL_KIND, ");
            stb.append("         T2.CURRICULUM_CD, ");
        }
        stb.append("         T2.SUBCLASSCD");
        stb.append("     )");
        //成績
        stb.append(" , T_RECORD AS (");
        stb.append("     SELECT");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD");
        stb.append("     FROM");
        stb.append("         RECORD_SCORE_DAT");
        stb.append("     WHERE");
        stb.append("         YEAR='" + _param._year + "' AND");
        stb.append("         SEMESTER <= '" + _param.getRecordSemester() + "' AND");
        stb.append("         TESTKINDCD='99' AND");
        stb.append("         TESTITEMCD='00' AND");
        stb.append("         SCHREGNO='" + student._schregno + "'");
        stb.append("     GROUP BY");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD");
        stb.append("     )");
        //合併
        stb.append(" ,T_REPLACE AS (");
        stb.append("     SELECT");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         COMBINED_CLASSCD,");
            stb.append("         COMBINED_SCHOOL_KIND,");
            stb.append("         COMBINED_CURRICULUM_CD,");
        }
        stb.append("         COMBINED_SUBCLASSCD,");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         ATTEND_CLASSCD,");
            stb.append("         ATTEND_SCHOOL_KIND,");
            stb.append("         ATTEND_CURRICULUM_CD,");
        }
        stb.append("         ATTEND_SUBCLASSCD,");
        stb.append("         CALCULATE_CREDIT_FLG");
        stb.append("     FROM");
        stb.append("         SUBCLASS_REPLACE_COMBINED_DAT");
        stb.append("     WHERE");
        stb.append("         REPLACECD='1' AND");
        stb.append("         YEAR='" + _param._year + "'");
        stb.append("     )");
        //重み
        stb.append(" , T_WEIGHTING AS (");
        stb.append("     SELECT");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         COMBINED_CLASSCD,");
            stb.append("         COMBINED_SCHOOL_KIND,");
            stb.append("         COMBINED_CURRICULUM_CD,");
        }
        stb.append("         COMBINED_SUBCLASSCD,");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         ATTEND_CLASSCD,");
            stb.append("         ATTEND_SCHOOL_KIND,");
            stb.append("         ATTEND_CURRICULUM_CD,");
        }
        stb.append("         ATTEND_SUBCLASSCD");
        stb.append("     FROM");
        stb.append("         SUBCLASS_WEIGHTING_GRADES_DAT");
        stb.append("     WHERE");
        stb.append("         YEAR='" + _param._year + "' AND");
        stb.append("         GRADE='" + student._grade + "'");
        stb.append("     )");
        //無条件
        stb.append(" , T_CREDIT AS (");
        stb.append("     SELECT");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD,");
        stb.append("         CREDITS,");
        stb.append("         COMP_UNCONDITION_FLG");
        stb.append("     FROM");
        stb.append("         CREDIT_MST");
        stb.append("     WHERE");
        stb.append("         YEAR='" + _param._year + "' AND");
        stb.append("         GRADE='" + student._grade + "' AND");
        stb.append("         COURSECD || MAJORCD || COURSECODE = '" + student._course + "'");
        stb.append("     )");
        //教科・科目グループ設定
        stb.append(" ,T_GROUP AS (");
        stb.append("     SELECT");
        stb.append("         CLASSCD,");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD");
        stb.append("     FROM");
        stb.append("         REC_SUBCLASS_GROUP_DAT");
        stb.append("     WHERE");
        stb.append("         YEAR = '" + _param._year + "' AND");
        stb.append("         GRADE = '" + student._grade + "' AND");
        stb.append("         COURSECD || MAJORCD || COURSECODE = '" + student._course + "'");
        stb.append("     GROUP BY");
        stb.append("         CLASSCD,");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD");
        stb.append("     )");

        //メイン（科目リスト）
        stb.append(" SELECT");
        stb.append("     SUBSTR(TBL1.SUBCLASSCD,1,2) AS CLASSCD,");
        stb.append("     CASE WHEN TBL3.CLASSORDERNAME2 IS NOT NULL THEN TBL3.CLASSORDERNAME2");
        stb.append("          ELSE TBL3.CLASSNAME END AS CLASSNAME,");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     TBL1.CLASSCD || TBL1.SCHOOL_KIND || TBL1.CURRICULUM_CD || TBL1.SUBCLASSCD AS SUBCLASSCD,");
        } else {
            stb.append("     TBL1.SUBCLASSCD,");
        }
        stb.append("     CASE WHEN TBL2.SUBCLASSORDERNAME2 IS NOT NULL THEN TBL2.SUBCLASSORDERNAME2");
        stb.append("          ELSE TBL2.SUBCLASSNAME END AS SUBCLASSNAME,");
        stb.append("     TBL4.CREDITS");
        stb.append(" FROM");
        stb.append("     (");
        stb.append("     SELECT");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD");
        stb.append("     FROM");
        stb.append("         T_CHAIR T1");
        stb.append("         INNER JOIN T_RECORD T2 ON T2.SUBCLASSCD=T1.SUBCLASSCD");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND T2.CLASSCD=T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND=T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD=T1.CURRICULUM_CD ");
        }
        stb.append("     UNION");
        stb.append("     SELECT");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T3.COMBINED_CLASSCD AS CLASSCD, ");
            stb.append("         T3.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
            stb.append("         T3.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
        }
        stb.append("         T3.COMBINED_SUBCLASSCD AS SUBCLASSCD");
        stb.append("     FROM");
        stb.append("         T_CHAIR T1");
        stb.append("         INNER JOIN T_RECORD T2 ON T1.SUBCLASSCD=T2.SUBCLASSCD");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND T1.CLASSCD=T2.CLASSCD ");
            stb.append("         AND T1.SCHOOL_KIND=T2.SCHOOL_KIND ");
            stb.append("         AND T1.CURRICULUM_CD=T2.CURRICULUM_CD ");
        }
        stb.append("         INNER JOIN T_REPLACE T3 ON T1.SUBCLASSCD=T3.ATTEND_SUBCLASSCD");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND T1.CLASSCD=T3.ATTEND_CLASSCD ");
            stb.append("         AND T1.SCHOOL_KIND=T3.ATTEND_SCHOOL_KIND ");
            stb.append("         AND T1.CURRICULUM_CD=T3.ATTEND_CURRICULUM_CD ");
        }
        stb.append("     GROUP BY");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T3.COMBINED_CLASSCD , ");
            stb.append("         T3.COMBINED_SCHOOL_KIND , ");
            stb.append("         T3.COMBINED_CURRICULUM_CD , ");
        }
        stb.append("         T3.COMBINED_SUBCLASSCD");
        stb.append("     UNION");
        stb.append("     SELECT");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T3.COMBINED_CLASSCD AS CLASSCD, ");
            stb.append("         T3.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
            stb.append("         T3.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
        }
        stb.append("         T3.COMBINED_SUBCLASSCD AS SUBCLASSCD");
        stb.append("     FROM");
        stb.append("         T_RECORD T1");
        stb.append("         INNER JOIN T_WEIGHTING T3 ON T1.SUBCLASSCD=T3.ATTEND_SUBCLASSCD");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND T1.CLASSCD=T3.ATTEND_CLASSCD ");
            stb.append("         AND T1.SCHOOL_KIND=T3.ATTEND_SCHOOL_KIND ");
            stb.append("         AND T1.CURRICULUM_CD=T3.ATTEND_CURRICULUM_CD ");
        }
        stb.append("     GROUP BY");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T3.COMBINED_CLASSCD , ");
            stb.append("         T3.COMBINED_SCHOOL_KIND , ");
            stb.append("         T3.COMBINED_CURRICULUM_CD , ");
        }
        stb.append("         T3.COMBINED_SUBCLASSCD");
        if (!_param.isJunior()) {
            stb.append(" UNION");
            stb.append(" SELECT");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD");
            stb.append(" FROM");
            stb.append("     T_CREDIT T1");
            stb.append(" WHERE");
            stb.append("     T1.COMP_UNCONDITION_FLG='1'");
        }
        stb.append("     )TBL1");
        stb.append("     LEFT JOIN SUBCLASS_MST TBL2 ON TBL1.SUBCLASSCD=TBL2.SUBCLASSCD");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND TBL1.CLASSCD=TBL2.CLASSCD ");
            stb.append("     AND TBL1.SCHOOL_KIND=TBL2.SCHOOL_KIND ");
            stb.append("     AND TBL1.CURRICULUM_CD=TBL2.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN CLASS_MST TBL3 ON SUBSTR(TBL1.SUBCLASSCD,1,2)=TBL3.CLASSCD");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND TBL1.SCHOOL_KIND=TBL3.SCHOOL_KIND ");
        }
        stb.append("     LEFT JOIN T_CREDIT TBL4 ON TBL1.SUBCLASSCD=TBL4.SUBCLASSCD");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND TBL1.CLASSCD=TBL4.CLASSCD ");
            stb.append("     AND TBL1.SCHOOL_KIND=TBL4.SCHOOL_KIND ");
            stb.append("     AND TBL1.CURRICULUM_CD=TBL4.CURRICULUM_CD ");
        }
        stb.append(" WHERE");
        stb.append("     NOT EXISTS(SELECT 'X' FROM T_WEIGHTING W3 WHERE TBL1.SUBCLASSCD=W3.ATTEND_SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND TBL1.CLASSCD=W3.ATTEND_CLASSCD ");
            stb.append("     AND TBL1.SCHOOL_KIND=W3.ATTEND_SCHOOL_KIND ");
            stb.append("     AND TBL1.CURRICULUM_CD=W3.ATTEND_CURRICULUM_CD ");
        }
        stb.append("               ) AND");
        if (_param.isJunior()) {
            stb.append(" (");
            stb.append(" EXISTS(SELECT 'X' FROM T_GROUP W5 WHERE W5.SUBCLASSCD = TBL1.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     AND W5.CLASSCD = TBL1.CLASSCD ");
                stb.append("     AND W5.SCHOOL_KIND = TBL1.SCHOOL_KIND ");
                stb.append("     AND W5.CURRICULUM_CD = TBL1.CURRICULUM_CD ");
            }
            stb.append("               ) OR");
            stb.append(" NOT EXISTS(SELECT 'X' FROM T_GROUP W5 WHERE W5.CLASSCD = SUBSTR(TBL1.SUBCLASSCD,1,2)");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     AND W5.SCHOOL_KIND = TBL1.SCHOOL_KIND ");
                stb.append("     AND W5.CURRICULUM_CD = TBL1.CURRICULUM_CD ");
            }
            stb.append("           ) ");
            stb.append(" ) AND");
        }
        stb.append("     TBL1.SUBCLASSCD NOT IN('900000')");
        stb.append(" ORDER BY");
        stb.append("     VALUE(TBL3.SHOWORDER3,999),");
        stb.append("     SUBSTR(TBL1.SUBCLASSCD,1,2),");
        stb.append("     VALUE(TBL2.SHOWORDER3,999),");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     TBL1.CLASSCD || TBL1.SCHOOL_KIND || TBL1.CURRICULUM_CD || TBL1.SUBCLASSCD ");
        } else {
            stb.append("     TBL1.SUBCLASSCD ");
        }

        return stb.toString();
    }

    private class SubClass {
        private final String _classCd;
        private final String _className;
        private final String _subclassCd;
        private final String _subclassName;
        private final String _credits;

        SubClass(
                final String classCd,
                final String className,
                final String subclassCd,
                final String subclassName,
                final String credits
        ) {
            _classCd = classCd;
            _className = className;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _credits = credits;
        }

        public String toString() {
            return _subclassCd + ":" + _subclassName;
        }
    }

    private void printScore(final DB2UDB db2, final Vrw32alp svf, final Student student, final SubClass subClass, final int gyo) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlScore(student, subClass);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String semester = rs.getString("SEMESTER");
                String score = rs.getString("SCORE");

                log.debug(subClass+", semester="+semester+", score="+score);

                String seme = semester.equals("9") ? "3" : semester;
                svf.VrsOutn("RECORD" + seme, gyo, score);
            }
        } catch (final Exception ex) {
            log.error("成績のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlScore(final Student student, final SubClass subClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SEMESTER, ");
        stb.append("     SCORE ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR='" + _param._year + "' AND ");
        stb.append("     SEMESTER not in('3') AND ");
        stb.append("     SEMESTER <= '" + _param.getRecordSemester() + "' AND ");
        stb.append("     TESTKINDCD='99' AND ");
        stb.append("     TESTITEMCD='00' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD='" + subClass._subclassCd + "' AND ");
        } else {
            stb.append("     SUBCLASSCD='" + subClass._subclassCd + "' AND ");
        }
        stb.append("     SCHREGNO='" + student._schregno + "' ");
        stb.append(" ORDER BY ");
        stb.append("     SEMESTER ");

        return stb.toString();
    }

    private void printValue(final DB2UDB db2, final Vrw32alp svf, final Student student, final SubClass subClass, final int gyo) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlValue(student, subClass);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String semester = rs.getString("SEMESTER");
                String value = rs.getString("VALUE");
                String compCredit = rs.getString("COMP_CREDIT");

                log.debug(subClass+", semester="+semester+", value="+value+", compCredit="+compCredit);

                svf.VrsOutn("GRADING3", gyo, value);
                if (compCredit != null) svf.VrsOutn("CREDIT" ,gyo ,  compCredit);
            }
        } catch (final Exception ex) {
            log.error("評定のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlValue(final Student student, final SubClass subClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SEMESTER, ");
        stb.append("     VALUE, ");
        stb.append("     COMP_CREDIT ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR='" + _param._year + "' AND ");
        stb.append("     SEMESTER = '" + _param.getRecordSemester() + "' AND ");
        stb.append("     TESTKINDCD='99' AND ");
        stb.append("     TESTITEMCD='00' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD='" + subClass._subclassCd + "' AND ");
        } else {
            stb.append("     SUBCLASSCD='" + subClass._subclassCd + "' AND ");
        }
        stb.append("     SCHREGNO='" + student._schregno + "' ");
        stb.append(" ORDER BY ");
        stb.append("     SEMESTER ");

        return stb.toString();
    }

    /**
     * 評定を印刷します。
     * @param semester 学期
     * @param value 評定
     * @param attend テスト欠席
     * @return
     */
    private String setValue(
            final String semester,
            final String value,
            final String attend
    ){
        if ("3".equals(semester)) {
            if (StringUtils.isNotBlank(value)) { return value; }
            return "";
        }
        
        if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(attend)) { return attend + value; }
        if (StringUtils.isNotBlank(value)) { return value; }
        if (StringUtils.isNotBlank(attend)) { return attend; }
        return "";
    }
    
    /** 
     *
     * SVF-OUT 出欠明細印刷処理 
     */
    private void printSvfAttend( Vrw32alp svf, PreparedStatement ps, String schregno )
    {
        ResultSet rs = null;
        try {
            int pp = 0;
            ps.setString( ++pp, schregno );                 //生徒番号
            rs = ps.executeQuery();
            while ( rs.next() ) {
                printSvfAttendOut( svf, rs );
            }
        } catch( Exception ex ){
            log.error( "error! ", ex );
        } finally{
            DbUtils.closeQuietly(rs);
        }
    }

    /** 
     *
     * SVF-OUT 出欠明細印刷
     */
    private void printSvfAttendOut( Vrw32alp svf, ResultSet rs )
    {
        int iMlesson = 0;
        int iAbsent  = 0;
        
        try {
            int i = Integer.parseInt( rs.getString("SEMESTER") );
            if( i == 9 )i = 4;

            if( 0 <= Integer.parseInt(rs.getString("LESSON")) )
                ret = svf.VrsOutn("LESSON", i,   rs.getString("LESSON") );              //授業日数
            ret = svf.VrsOutn("KIBIKI",     i,   String.valueOf(rs.getInt("MOURNING") + rs.getInt("SUSPEND") + rs.getInt("VIRUS") + rs.getInt("KOUDOME")));    //出停・忌引日数
            ret = svf.VrsOutn("PRESENT",    i,   rs.getString("MLESSON") );             //出席しなければならない日数
            ret = svf.VrsOutn("ABSENCE",    i,   rs.getString("SICK") );              //欠席日数
            
            if(!nvlT(rs.getString("MLESSON")).equals("")){
                iMlesson = Integer.parseInt(rs.getString("MLESSON"));
            }
            if(!nvlT(rs.getString("ABSENT")).equals("")){
                iAbsent = Integer.parseInt(rs.getString("SICK"));
            }
            int iAttend = iMlesson - iAbsent;
            ret = svf.VrsOutn("ATTEND",    i,   String.valueOf(iAttend).toString());   //出席日数
            
            ret = svf.VrsOutn("LATE",      i,   rs.getString("LATE") );                //遅刻回数
            ret = svf.VrsOutn("LEAVE",     i,   rs.getString("EARLY") );               //早退回数

            String kekka = "";
            if ( i == 1 ) kekka = String.valueOf( skekka1 );
            if ( i == 2 ) kekka = String.valueOf( skekka2 );
            if ( i == 3 ) kekka = String.valueOf( skekka9 - skekka1 - skekka2 );
            if ( i == 4 ) kekka = String.valueOf( skekka9 );
            ret = svf.VrsOutn("KEKKA",      i,   kekka );               //欠課時数
        } catch( Exception ex ){
            log.error("printSvfAttendOut error!",ex);
        }

    }


    /** 
     *
     * SVF-OUT 出欠(備考)印刷処理 
     *
     */
    private void printSvfRemark(DB2UDB db2, Vrw32alp svf, String schregno ) throws SQLException {

        final List listData = getHreportRemark(db2, _param._year, _param._semester, schregno);
        for (Iterator it = listData.iterator(); it.hasNext();) {
            final HreportRemark reportRemark = (HreportRemark) it.next();
            
            String semester = reportRemark._semester;
            String communication = reportRemark._communication;
            ArrayList arrlist = knjobj.retDividString( communication, 12, 9 );
            if( arrlist != null ){
                for( int i = 0 ; i < arrlist.size() ; i++ ){
                    ret = svf.VrsOutn("MESSAGE1_" + String.valueOf(i+1), Integer.parseInt(semester),  (String)arrlist.get(i) );
                }
            }
        }
    }

    /** 
    *
    * SVF-OUT 備考印刷処理 
    *
    */
   private void printSvfBiko(DB2UDB db2, Vrw32alp svf, String schregno ) throws SQLException {

       final List listData = getHreportRemark(db2, _param._year, "9", schregno);
       for (Iterator it = listData.iterator(); it.hasNext();) {
           final HreportRemark reportRemark = (HreportRemark) it.next();
           
           String special = reportRemark._specialactremark;
           ArrayList arrlist = knjobj.retDividString( special, 52, 4 );
           if( arrlist != null ){
               for( int i = 0 ; i < arrlist.size() ; i++ ){
                   ret = svf.VrsOut("REMARK" + String.valueOf(i+1), (String)arrlist.get(i) );
               }
           }
       }
   }
   
   /** 
    * SVF-OUT 学校名・担任名印刷 
    */
   private void printSvfScool(Vrw32alp svf) throws SQLException {
       ret = svf.VrsOut("STAFFNAME" , _param.getSchoolStaffName()); // 担任名
       ret = svf.VrsOut("SCHOOLNAME" , _param._schoolName);    // 学校名
   }

    /**
     * 通知表所見データ情報取得。
     */
    public List getHreportRemark(DB2UDB db2, String paramYear, String paramSemester, String schregno)
        throws SQLException {

        final List rtnList = new ArrayList();
        final String sql = sqlHreportRemark(paramYear, paramSemester, schregno);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final HreportRemark hreportRemark = new HreportRemark(
                        rs.getString("SEMESTER"),
                        nvlT(rs.getString("COMMUNICATION")),
                        nvlT(rs.getString("SPECIALACTREMARK"))
                        );
                rtnList.add(hreportRemark);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String sqlHreportRemark(String paramYear, String paramSemester, String schregno) {

        StringBuffer stb = new StringBuffer();
        
        stb.append(" select");
        stb.append("     SEMESTER,");
        stb.append("     COMMUNICATION,");
        stb.append("     SPECIALACTREMARK");
        stb.append("  from");
        stb.append("     HREPORTREMARK_DAT");
        stb.append("  where");
        stb.append("     YEAR = '" + paramYear + "' and");
        if(paramSemester.equals("9")){
            stb.append("     SEMESTER = '"+ paramSemester +"' and");
        } else {
            stb.append("     SEMESTER <= '"+ paramSemester +"' and");
        }
        stb.append("     SCHREGNO = '"+ schregno +"'");
        stb.append("  order by SEMESTER");
        
        return stb.toString();
    }
    
    /**
     * 通知表所見データ。
     */
    private class HreportRemark {
        private final String _semester;
        private final String _communication;
        private final String _specialactremark;

        HreportRemark(
                final String semester,
                final String communication,
                final String specialactremark
        ) {
            _semester = semester;
            _communication = communication;
            _specialactremark = specialactremark;
        }
    }
    
    /**
     * NULL値を""として返す。
     */
    private String nvlT(String val) {

        if (val == null) {
            return "";
        } else {
            return val;
        }
    }

    private List createStudents(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlStudents();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String hrName = rs.getString("HR_NAME");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String course = rs.getString("COURSE");

                final Student student = new Student(schregno, name, attendNo, hrName, grade, hrClass, course);
                rtn.add(student);
            }
        } catch (final Exception ex) {
            log.error("生徒のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }
    
    private String sqlStudents() {
        final String sql;
        sql = "select"
            + "    T1.SCHREGNO,"
            + "    T1.GRADE,"
            + "    T1.HR_CLASS,"
            + "    T1.ATTENDNO,"
            + "    T1.COURSECD || T1.MAJORCD || T1.COURSECODE as COURSE,"
            + "    T2.NAME,"
            + "    T3.HR_NAME"
            + "  from SCHREG_REGD_DAT T1"
            + "       inner join SCHREG_BASE_MST T2 on T2.SCHREGNO = T1.SCHREGNO"
            + "       inner join SCHREG_REGD_HDAT T3 on T3.YEAR = T1.YEAR"
            + "                                     and T3.SEMESTER = T1.SEMESTER"
            + "                                     and T3.GRADE = T1.GRADE"
            + "                                     and T3.HR_CLASS = T1.HR_CLASS"
            + "  where"
            + "    T1.SCHREGNO in " + _param._schregnoIn + " and"
            + "    T1.YEAR = '" + _param._year + "' and"
            + "    T1.SEMESTER = '" + _param._semester + "' and"
            + "    T1.GRADE || T1.HR_CLASS = '" + _param._hrClass + "'"
            + "  order by"
            + "    T1.GRADE, T1.HR_CLASS, T1.ATTENDNO"
            ;
        return sql;
    }

    private class Student {
        private final String _schregno;
        private final String _name;
        private final String _attendNo;
        private final String _hrName;
        private final String _grade;
        private final String _hrClass;
        private final String _course;

        public Student(
                final String schregno,
                final String name,
                final String attendNo,
                final String hrName,
                final String grade,
                final String hrClass,
                final String course
        ) {
            _schregno = schregno;
            _name = name;
            _attendNo = attendNo;
            _hrName = hrName;
            _grade = grade;
            _hrClass = hrClass;
            _course = course;
        }

        public String getAttendNo() {
            return String.valueOf(Integer.parseInt(_attendNo));
        }

        public String toString() {
            return _schregno + ":" + _name;
        }
    }
    
}

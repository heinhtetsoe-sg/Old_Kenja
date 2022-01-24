/**
 *
 * 学校教育システム 賢者 [成績管理]  通知票(智辯五條)
 * @author nakamoto
 * @version $Id: a6d040c3086f16f25ec008c629a3248347d930ab $
 *
 */

package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;


public class KNJD658B {

    private static final Log log = LogFactory.getLog(KNJD658B.class);

    private KNJObjectAbs knjobj;        //編集用クラス

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定
    private ResultSet rs;
    private static final String FORM_FILE_15 = "KNJD658B_1.frm";
    private static final String FORM_FILE_20 = "KNJD658B_2.frm";

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

        // パラメータの取得
        _param = createParam(request);
        // print svf設定
        sd.setSvfInit( request, response, svf);
        // ＤＢ接続
        db2 = sd.setDb(request);
        if( sd.openDb(db2) ){
            log.error("db open error! ");
            return;
        }
        // 印刷処理
        nonedata = printSvf( request, db2, svf );
        // 終了処理
        sd.closeSvf( svf, nonedata );
        sd.closeDb(db2);
    }

    private Param createParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String testCd = request.getParameter("TESTKINDCD");
        final String hrClass = request.getParameter("GRADE_HR_CLASS");
        final String schregno[] = request.getParameterValues("CATEGORY_SELECTED");
        final String mockcd[] = request.getParameterValues("MOCK_SELECTED");
        final String rankDiv = request.getParameter("JUNI");
        final String rankKijun = request.getParameter("KIJUNTEN");
        final String subclassDiv = request.getParameter("SAIDAIKAMOKU");
        final String kansan = request.getParameter("KANSAN");
        final String useCurriculumcd = request.getParameter("useCurriculumcd");

        final Param param = new Param(
                year,
                semester,
                testCd,
                hrClass,
                schregno,
                mockcd,
                rankDiv,
                rankKijun,
                subclassDiv,
                kansan,
                useCurriculumcd
        );
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _testCd;
        private final String _hrClass;
        private final String _grade;
        private final String[] _schregno;
        private final String _schregnoIn;
        private final String[] _mockcd;
        private final String _gengou;
        private final String _rankDiv;
        private final String _rankKijun;
        private final boolean _print15;
        private final boolean _convert100;
        private final String _useCurriculumcd;

        /*** SCHREG_REGD_GDAT ***/
        private String _schoolKind;
        private String _gradeCd;
        private String _gradeName;

        /*** TESTITEM_MST_COUNTFLG_NEW ***/
        private List _testitemList;

        private String _hrName;
        private String _staffName;
        private String _schoolName;
        private String _remark2;

        public Param(
                final String year,
                final String semester,
                final String testCd,
                final String hrClass,
                final String[] schregno,
                final String[] mockcd,
                final String rankDiv,
                final String rankKijun,
                final String subclassDiv,
                final String kansan,
                final String useCurriculumcd
        ) {
            _year = year;
            _semester = semester;
            _testCd = testCd;
            _hrClass = hrClass;
            _grade = hrClass.substring(0, 2);
            _schregno = schregno;
            _schregnoIn = setSchregnoIn(schregno);
            _mockcd = mockcd;
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(year));
            _gengou = gengou + "年度";
            _rankDiv = rankDiv;
            _rankKijun = rankKijun;
            _print15 = "1".equals(subclassDiv);
            _convert100 = "1".equals(kansan);
            _useCurriculumcd = useCurriculumcd;
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
            createGdat(db2);
            createStaffName(db2);
            createSchoolInfo(db2);
            _testitemList = createTestitems(db2);
        }

        private void createGdat(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT SCHOOL_KIND, rtrim(GRADE_CD) as GRADE_CD, GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolKind = rs.getString("SCHOOL_KIND");
                    _gradeCd = rs.getString("GRADE_CD");
                    _gradeName = rs.getString("GRADE_NAME1");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug(_schoolKind + "：" + _gradeCd);
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

        private String getCertifKindcd() {
            return isJunior() ? "110" : "109";
        }

        public String getHrStaffName() {
            return _staffName != null ? _staffName : "";
        }

        public boolean isJunior() {
            return Integer.parseInt(_grade) < 4;
        }

        public int getGradeInt() {
            return isJunior() ? Integer.parseInt(_grade) : Integer.parseInt(_grade) - 3;
        }

        public boolean isRankGrade() {
            return "1".equals(_rankDiv);
        }

        public boolean isKijunScore() {
            return "1".equals(_rankKijun);
        }

        public String getRankItemName() {
            return isRankGrade() ? "学年" : "コース";
        }

        private String getGradeRank() {
            return isKijunScore() ? "GRADE_RANK" : "GRADE_AVG_RANK";
        }

        private String getCourseRank() {
            return isKijunScore() ? "COURSE_RANK" : "COURSE_AVG_RANK";
        }

        private String getGradeCourseRank() {
            return isRankGrade() ? getGradeRank() : getCourseRank();
        }

        public String getAvgDiv() {
            return isRankGrade() ? "1" : "3";
        }

        public String getRecordTestcd() {
            return _semester + _testCd;
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
            if (_param._print15) {
                svf.VrSetForm( FORM_FILE_15, 1 );
            } else {
                svf.VrSetForm( FORM_FILE_20, 1 );
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

        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            //フォーム
            setSvfForm(svf);
            //ヘッダ
            printHeader(svf, student);
            //３・５教科
            printTotal(db2, svf, student);
            //成績明細データを出力
            printSubClass(db2, svf, student);
            //備考
            printRemark(db2, svf, student);
            //模試
            printMock(db2, svf, student);

            rtnflg = true;
            svf.VrEndPage();
        }

        return rtnflg;
    }

    private void printHeader(final Vrw32alp svf, final Student student) {
        svf.VrsOut("GRADE"          , _param._gengou + "　" + _param._gradeName); //学年
        svf.VrsOut("STAFFNAME"      , _param.getHrStaffName()); //担任名
        svf.VrsOut("SCHOOLNAME"     , _param._schoolName); //学校名
        svf.VrsOut("COURSECODENAME" , student.getCoursecodeName()); //コース名
        svf.VrsOut("HR_NAME"        , student.getHrName());
        svf.VrsOut("ATTENDNO"       , student.getAttendNo());
        svf.VrsOut("NAME"           , student._name);
        svf.VrsOut("ITEM_AVE1"      , _param.getRankItemName());
        svf.VrsOut("ITEM_RANK1"     , _param.getRankItemName());
        for (final Iterator itTest = _param._testitemList.iterator(); itTest.hasNext();) {
            final Testitem testitem = (Testitem) itTest.next();
            svf.VrsOut("TESTNAME" + testitem._no , testitem._testName);
        }
    }

    private void printTotalRank(final DB2UDB db2, final Vrw32alp svf, final Student student, final Testitem testitem) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlTotalRank(student, testitem);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                int gyo = "333333".equals(rs.getString("SUBCLASSCD")) ? 1 : 2;
                if (_param._convert100) {
                    svf.VrsOutn("TEST_TOTALSCORE" + testitem._no, gyo, rs.getString("AVG"));
                } else {
                    svf.VrsOutn("TEST_TOTALSCORE" + testitem._no, gyo, rs.getString("SCORE"));
                }
                svf.VrsOutn("TEST_TOTALRANK"  + testitem._no, gyo, rs.getString("RANK"));
            }
        } catch (final Exception ex) {
            log.error("順位のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlTotalRank(final Student student, final Testitem testitem) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT");
        stb.append("     SUBCLASSCD,");
        stb.append("     SCHREGNO,");
        stb.append("     SCORE,");
        stb.append("     decimal(round(float(AVG)*10,0)/10,5,1) AS AVG,");
        stb.append("    " + _param.getGradeCourseRank() + " AS RANK");
        stb.append(" FROM");
        stb.append("     RECORD_RANK_DAT");
        stb.append(" WHERE");
        stb.append("     YEAR='" + _param._year + "' AND");
        stb.append("     SEMESTER = '" + testitem._semester + "' AND");
        stb.append("     TESTKINDCD || TESTITEMCD = '" + testitem._testCd + "' AND");
        stb.append("     SUBCLASSCD in ('333333','555555') AND");
        stb.append("     SCHREGNO='" + student._schregno + "'");
        stb.append(" ORDER BY");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ");
        } else {
            stb.append("     SUBCLASSCD");
        }
        return stb.toString();
    }

    private void printTotalAverage(final DB2UDB db2, final Vrw32alp svf, final Student student, final Testitem testitem) {
        ConvertedScore convScore = null;
        if (_param._convert100) {
            convScore = new ConvertedScore();
            convScore.load(db2, testitem._semester, testitem._testCd);
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlTotalAverage(student, testitem);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                int gyo = "333333".equals(rs.getString("SUBCLASSCD")) ? 1 : 2;
                if (_param._convert100) {
                    final String groupDiv = "333333".equals(rs.getString("SUBCLASSCD")) ? "3" : "5";
                    svf.VrsOutn("TEST_TOTALAVE"   + testitem._no, gyo, convScore.getAvg(groupDiv, student));
                } else {
                    svf.VrsOutn("TEST_TOTALAVE"   + testitem._no, gyo, rs.getString("AVG"));
                }
                svf.VrsOutn("TEST_TOTALCOUNT" + testitem._no, gyo, rs.getString("COUNT"));
            }
        } catch (final Exception ex) {
            log.error("人数のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlTotalAverage(final Student student, final Testitem testitem) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT");
        stb.append("     SUBCLASSCD,");
        stb.append("     decimal(round(float(AVG)*10,0)/10,5,1) AS AVG,");
        stb.append("     COUNT");
        stb.append(" FROM");
        stb.append("     RECORD_AVERAGE_DAT");
        stb.append(" WHERE");
        stb.append("     YEAR = '" + _param._year + "' AND");
        stb.append("     SEMESTER = '" + testitem._semester + "' AND");
        stb.append("     TESTKINDCD || TESTITEMCD = '" + testitem._testCd + "' AND");
        stb.append("     SUBCLASSCD in ('333333','555555') AND");
        stb.append("     AVG_DIV = '" + _param.getAvgDiv() + "' AND");
        stb.append("     GRADE = '" + student._grade + "' AND");
        stb.append("     HR_CLASS = '000' AND");
        if (_param.isRankGrade()) {
            stb.append("     COURSECD = '0' AND");
            stb.append("     MAJORCD = '000' AND");
            stb.append("     COURSECODE = '0000'");
        } else {
            stb.append("     COURSECD || MAJORCD || COURSECODE = '" + student._course + "'");
        }
        stb.append(" ORDER BY");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ");
        } else {
            stb.append("     SUBCLASSCD ");
        }
        return stb.toString();
    }

    private void printTotal(final DB2UDB db2, final Vrw32alp svf, final Student student) throws SQLException {
        for (final Iterator itTest = _param._testitemList.iterator(); itTest.hasNext();) {
            final Testitem testitem = (Testitem) itTest.next();
            printTotalAverage(db2, svf, student, testitem);
            printTotalRank(db2, svf, student, testitem);
        }
    }

    private void printSubClass(final DB2UDB db2, final Vrw32alp svf, final Student student) throws SQLException {
        final List subClasses = createSubClass(db2, student);
        int gyo = 0;
        int gyoMax = _param._print15 ? 15 : 20;
        for (final Iterator it = subClasses.iterator(); it.hasNext();) {
            final SubClass subClass = (SubClass) it.next();

            gyo++;
            if (gyoMax < gyo) break;
            //科目
            svf.VrsOutn("TEST_SUBCLASS1",  gyo,  subClass._subclassName);
            //成績
            for (final Iterator itTest = _param._testitemList.iterator(); itTest.hasNext();) {
                final Testitem testitem = (Testitem) itTest.next();
                printAverage(db2, svf, student, testitem, subClass, gyo);
                printRank(db2, svf, student, testitem, subClass, gyo);
            }
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
                final String subclassCdMoto = rs.getString("ATTEND_SUBCLASSCD");

                final SubClass subclass = new SubClass(classCd, className, subclassCd, subclassName, subclassCdMoto);
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
        stb.append("         SEMESTER || TESTKINDCD || TESTITEMCD <= '" + _param.getRecordTestcd() + "' AND");
        stb.append("         TESTKINDCD <> '99' AND");
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
            stb.append("         COMBINED_CLASSCD, ");
            stb.append("         COMBINED_SCHOOL_KIND, ");
            stb.append("         COMBINED_CURRICULUM_CD, ");
        }
        stb.append("         COMBINED_SUBCLASSCD,");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         ATTEND_CLASSCD, ");
            stb.append("         ATTEND_SCHOOL_KIND, ");
            stb.append("         ATTEND_CURRICULUM_CD, ");
        }
        stb.append("         ATTEND_SUBCLASSCD,");
        stb.append("         CALCULATE_CREDIT_FLG");
        stb.append("     FROM");
        stb.append("         SUBCLASS_REPLACE_COMBINED_DAT");
        stb.append("     WHERE");
        stb.append("         REPLACECD='1' AND");
        stb.append("         YEAR='" + _param._year + "'");
        stb.append("     )");

        //メイン（科目リスト）
        stb.append(" SELECT");
        stb.append("     SUBSTR(TBL1.SUBCLASSCD,1,2) AS CLASSCD,");
        stb.append("     TBL3.CLASSNAME,");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     TBL1.CLASSCD || TBL1.SCHOOL_KIND || TBL1.CURRICULUM_CD || TBL1.SUBCLASSCD AS SUBCLASSCD,");
        } else {
            stb.append("     TBL1.SUBCLASSCD,");
        }
        stb.append("     TBL2.SUBCLASSABBV as SUBCLASSNAME,");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     TBL5.ATTEND_CLASSCD || TBL5.ATTEND_SCHOOL_KIND || TBL5.ATTEND_CURRICULUM_CD || TBL5.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
        } else {
            stb.append("     TBL5.ATTEND_SUBCLASSCD");
        }
        stb.append(" FROM");
        stb.append("     (");
        stb.append("     SELECT");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD ");
        stb.append("     FROM");
        stb.append("         T_CHAIR T1");
        stb.append("         INNER JOIN T_RECORD T2 ON T2.SUBCLASSCD=T1.SUBCLASSCD");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("               AND T2.CLASSCD = T1.CLASSCD");
            stb.append("               AND T2.SCHOOL_KIND = T1.SCHOOL_KIND");
            stb.append("               AND T2.CURRICULUM_CD = T1.CURRICULUM_CD");
        }
        stb.append("     )TBL1");
        stb.append("     LEFT JOIN SUBCLASS_MST TBL2 ON TBL1.SUBCLASSCD=TBL2.SUBCLASSCD");
        stb.append("          AND TBL1.SUBCLASSCD=TBL2.SUBCLASSCD");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("          AND TBL1.CLASSCD = TBL2.CLASSCD");
            stb.append("          AND TBL1.SCHOOL_KIND = TBL2.SCHOOL_KIND");
            stb.append("          AND TBL1.CURRICULUM_CD = TBL2.CURRICULUM_CD");
        }
        stb.append("     LEFT JOIN CLASS_MST TBL3 ON SUBSTR(TBL1.SUBCLASSCD,1,2)=TBL3.CLASSCD");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("          AND TBL1.SCHOOL_KIND = TBL3.SCHOOL_KIND");
        }
        stb.append("     LEFT JOIN (");
        stb.append("         SELECT");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             ATTEND_CLASSCD, ");
            stb.append("             ATTEND_SCHOOL_KIND, ");
            stb.append("             ATTEND_CURRICULUM_CD, ");
        }
        stb.append("             ATTEND_SUBCLASSCD");
        stb.append("         FROM");
        stb.append("             T_REPLACE");
        stb.append("         GROUP BY");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             ATTEND_CLASSCD, ");
            stb.append("             ATTEND_SCHOOL_KIND, ");
            stb.append("             ATTEND_CURRICULUM_CD, ");
        }
        stb.append("             ATTEND_SUBCLASSCD");
        stb.append("     ) TBL5 ON TBL1.SUBCLASSCD=TBL5.ATTEND_SUBCLASSCD");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             AND TBL1.CLASSCD = TBL5.ATTEND_CLASSCD ");
            stb.append("             AND TBL1.SCHOOL_KIND = TBL5.ATTEND_SCHOOL_KIND ");
            stb.append("             AND TBL1.CURRICULUM_CD = TBL5.ATTEND_CURRICULUM_CD ");
        }
        stb.append(" WHERE");
        stb.append("     SUBSTR(TBL1.SUBCLASSCD,1,2) < '90' ");
        stb.append(" ORDER BY");
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
        private final String _subclassCdMoto;

        SubClass(
                final String classCd,
                final String className,
                final String subclassCd,
                final String subclassName,
                final String subclassCdMoto
        ) {
            _classCd = classCd;
            _className = className;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _subclassCdMoto = subclassCdMoto;
        }

        private boolean isSubclassMoto() {
            return null != _subclassCdMoto;
        }

        public String toString() {
            return _subclassCd + ":" + _subclassName;
        }
    }

    private void printRank(final DB2UDB db2, final Vrw32alp svf, final Student student, final Testitem testitem, final SubClass subClass, final int gyo) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlRank(student, testitem, subClass);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOutn("TEST_SCORE" + testitem._no, gyo, rs.getString("SCORE"));
                svf.VrsOutn("TEST_RANK"  + testitem._no, gyo, rs.getString("RANK"));
            }
        } catch (final Exception ex) {
            log.error("順位のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlRank(final Student student, final Testitem testitem, final SubClass subClass) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD,");
        } else {
            stb.append("     SUBCLASSCD,");
        }
        stb.append("     SCHREGNO,");
        stb.append("     SCORE,");
        stb.append("    " + _param.getGradeCourseRank() + " AS RANK");
        stb.append(" FROM");
        stb.append("     RECORD_RANK_DAT");
        stb.append(" WHERE");
        stb.append("     YEAR='" + _param._year + "' AND");
        stb.append("     SEMESTER = '" + testitem._semester + "' AND");
        stb.append("     TESTKINDCD || TESTITEMCD = '" + testitem._testCd + "' AND");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '" + subClass._subclassCd + "' AND");
        } else {
            stb.append("     SUBCLASSCD = '" + subClass._subclassCd + "' AND");
        }
        stb.append("     SCHREGNO='" + student._schregno + "'");
        stb.append(" ORDER BY");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ");
        } else {
            stb.append("     SUBCLASSCD ");
        }
        return stb.toString();
    }

    private void printAverage(final DB2UDB db2, final Vrw32alp svf, final Student student, final Testitem testitem, final SubClass subClass, final int gyo) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlAverage(student, testitem, subClass);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOutn("TEST_AVE"   + testitem._no, gyo, rs.getString("AVG"));
                svf.VrsOutn("TEST_COUNT" + testitem._no, gyo, rs.getString("COUNT"));
            }
        } catch (final Exception ex) {
            log.error("人数のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlAverage(final Student student, final Testitem testitem, final SubClass subClass) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CASE WHEN SUBCLASSCD IN ('333333', '555555', '999999') THEN SUBCLASSCD ELSE CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD END AS SUBCLASSCD,");
        } else {
            stb.append("     SUBCLASSCD,");
        }
        stb.append("     decimal(round(float(AVG)*10,0)/10,5,1) AS AVG,");
        stb.append("     COUNT");
        stb.append(" FROM");
        stb.append("     RECORD_AVERAGE_DAT");
        stb.append(" WHERE");
        stb.append("     YEAR = '" + _param._year + "' AND");
        stb.append("     SEMESTER = '" + testitem._semester + "' AND");
        stb.append("     TESTKINDCD || TESTITEMCD = '" + testitem._testCd + "' AND");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '" + subClass._subclassCd + "' AND");
        } else {
            stb.append("     SUBCLASSCD = '" + subClass._subclassCd + "' AND");
        }
        stb.append("     AVG_DIV = '" + _param.getAvgDiv() + "' AND");
        stb.append("     GRADE = '" + student._grade + "' AND");
        stb.append("     HR_CLASS = '000' AND");
        if (_param.isRankGrade()) {
            stb.append("     COURSECD = '0' AND");
            stb.append("     MAJORCD = '000' AND");
            stb.append("     COURSECODE = '0000'");
        } else {
            stb.append("     COURSECD || MAJORCD || COURSECODE = '" + student._course + "'");
        }
        stb.append(" ORDER BY");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ");
        } else {
            stb.append("     SUBCLASSCD ");
        }
        return stb.toString();
    }

    private void printMock(final DB2UDB db2, final Vrw32alp svf, final Student student) throws SQLException {
        if (null != _param._mockcd) {
            for (int i = 0; i < _param._mockcd.length; i++) {
                printMockName(db2, svf, _param._mockcd[i], String.valueOf(i+1));
                printMockSubclass(db2, svf, student, _param._mockcd[i], String.valueOf(i+1));
                printMockTotal(db2, svf, student, _param._mockcd[i], String.valueOf(i+1));
            }
        }
    }

    private void printMockName(final DB2UDB db2, final Vrw32alp svf, final String mockcd, final String no) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlMockName(mockcd);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("MOCK_NAME" + no ,  rs.getString("MOCKNAME1") );
            }
        } catch (final Exception ex) {
            log.error("模試テスト名のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlMockName(final String mockcd) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     MOCKNAME1 ");
        stb.append(" FROM ");
        stb.append("     MOCK_MST ");
        stb.append(" WHERE ");
        stb.append("     MOCKCD = '" + mockcd + "' ");
        return stb.toString();
    }

    private void printMockSubclass(final DB2UDB db2, final Vrw32alp svf, final Student student, final String mockcd, final String no) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlMockSubclass(student, mockcd);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int gyo = 0;
            String tmpsubcd = "";
            while (rs.next()) {
                final String subcd = rs.getString("MOCK_SUBCLASS_CD");
                if (!tmpsubcd.equals(subcd)) {
                    tmpsubcd = subcd;
                    gyo++;
                    svf.VrsOutn("MOCK_SUBCLASS" + no ,  gyo,  rs.getString("SUBCLASS_NAME") );
                }
                final String mockdiv = rs.getString("MOCKDIV");
                if ("0".equals(mockdiv)) {
                    svf.VrsOutn("MOCK_SCORE"  + no ,  gyo,  rs.getString("SCORE") );
                    svf.VrsOutn("MOCK_AVE"    + no ,  gyo,  rs.getString("AVG") );
                    svf.VrsOutn("MOCK_RANK"   + no ,  gyo,  rs.getString("RANK") );
                    svf.VrsOutn("MOCK_COUNT"  + no ,  gyo,  rs.getString("COUNT") );
                }
                if ("9".equals(mockdiv)) {
                    svf.VrsOutn("MOCK_TSCORE" + no ,  gyo,  rs.getString("DEVIATION") );
                }
            }
        } catch (final Exception ex) {
            log.error("模試科目のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlMockSubclass(final Student student, final String mockcd) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     L2.SUBCLASS_NAME, ");
        stb.append("     T1.MOCK_SUBCLASS_CD, ");
        stb.append("     T1.MOCKDIV, ");
        stb.append("     T1.SCORE, ");
        stb.append("     decimal(round(float(T1.AVG)*10,0)/10,5,1) AS AVG, ");
        stb.append("     T1.DEVIATION, ");
        stb.append("     T1.RANK, ");
        stb.append("     T1.COUNT ");
        stb.append(" FROM ");
        stb.append("     MOCK2_DAT T1 ");
        stb.append("     INNER JOIN MOCK_SUBCLASS_MST L2 ON L2.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        stb.append("     T1.MOCKCD = '" + mockcd + "' AND ");
        stb.append("     T1.SCHREGNO = '" + student._schregno + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.MOCK_SUBCLASS_CD, ");
        stb.append("     T1.MOCKDIV ");
        return stb.toString();
    }

    private void printMockTotal(final DB2UDB db2, final Vrw32alp svf, final Student student, final String mockcd, final String no) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlMockTotal(student, mockcd);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String mockdiv = rs.getString("MOCKDIV");
                if ("0".equals(mockdiv)) {
                    svf.VrsOut("MOCK_TOTALSCORE"  + no ,  rs.getString("SCORE") );
                    svf.VrsOut("MOCK_TOTALAVE"    + no ,  rs.getString("AVG") );
                    svf.VrsOut("MOCK_TOTALRANK"   + no ,  rs.getString("RANK") );
                    svf.VrsOut("MOCK_TOTALCOUNT"  + no ,  rs.getString("COUNT") );
                }
                if ("9".equals(mockdiv)) {
                    svf.VrsOut("MOCK_TOTALTSCORE" + no ,  rs.getString("DEVIATION") );
                }
            }
        } catch (final Exception ex) {
            log.error("模試３教科のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlMockTotal(final Student student, final String mockcd) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.MOCKDIV, ");
        stb.append("     T1.SCORE, ");
        stb.append("     decimal(round(float(T1.AVG)*10,0)/10,5,1) AS AVG, ");
        stb.append("     T1.DEVIATION, ");
        stb.append("     T1.RANK, ");
        stb.append("     T1.COUNT ");
        stb.append(" FROM ");
        stb.append("     MOCK2_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        stb.append("     T1.MOCKCD = '" + mockcd + "' AND ");
        stb.append("     T1.SCHREGNO = '" + student._schregno + "' AND ");
        stb.append("     T1.MOCK_SUBCLASS_CD = '333333' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.MOCKDIV ");
        return stb.toString();
    }

    private void printRemark(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlRemark(student);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                ArrayList arrlist = knjobj.retDividString(rs.getString("FOOTNOTE"), 100, 3);
                if (arrlist != null) {
                    for (int i = 0; i < arrlist.size(); i++) {
                        svf.VrsOutn("REMARK",  (i+1),  (String)arrlist.get(i) );
                    }
                }
            }
        } catch (final Exception ex) {
            log.error("人数のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlRemark(final Student student) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     GRADE, ");
        stb.append("     FOOTNOTE ");
        stb.append(" FROM ");
        stb.append("     RECORD_DOCUMENT_KIND_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR        = '" + _param._year + "' AND ");
        stb.append("     SEMESTER    = '0' AND ");
        stb.append("     TESTKINDCD  = '00' AND ");
        stb.append("     TESTITEMCD  = '00' AND ");
        stb.append("     GRADE       = '" + student._grade + "' AND ");
        stb.append("     HR_CLASS    = '000' AND ");
        stb.append("     COURSECD    = '0' AND ");
        stb.append("     MAJORCD     = '000' AND ");
        stb.append("     COURSECODE  = '0000' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD  = '00' AND ");
            stb.append("     SCHOOL_KIND  = '0' AND ");
            stb.append("     CURRICULUM_CD  = '0' AND ");
        }
        stb.append("     SUBCLASSCD  = '000000' AND ");
        stb.append("     KIND_DIV    = '4' "); //固定
        return stb.toString();
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
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String course = rs.getString("COURSE");
                final String hrName = rs.getString("HR_NAME");
                final String coursecodeName = rs.getString("COURSECODENAME");

                final Student student = new Student(schregno, name, attendNo, hrName, grade, hrClass, course, coursecodeName);
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
            + "    T3.HR_NAME,"
            + "    L1.COURSECODENAME"
            + "  from SCHREG_REGD_DAT T1"
            + "       inner join SCHREG_BASE_MST T2 on T2.SCHREGNO = T1.SCHREGNO"
            + "       inner join SCHREG_REGD_HDAT T3 on T3.YEAR = T1.YEAR"
            + "                                     and T3.SEMESTER = T1.SEMESTER"
            + "                                     and T3.GRADE = T1.GRADE"
            + "                                     and T3.HR_CLASS = T1.HR_CLASS"
            + "       left join COURSECODE_MST L1 on L1.COURSECODE = T1.COURSECODE"
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
        private final String _coursecodeName;

        public Student(
                final String schregno,
                final String name,
                final String attendNo,
                final String hrName,
                final String grade,
                final String hrClass,
                final String course,
                final String coursecodeName
        ) {
            _schregno = schregno;
            _name = name;
            _attendNo = attendNo;
            _hrName = hrName;
            _grade = grade;
            _hrClass = hrClass;
            _course = course;
            _coursecodeName = coursecodeName;
        }

        public String getCoursecodeName() {
            return (_coursecodeName != null) ? _coursecodeName : "";
        }

        public String getHrName() {
            return (_hrName != null) ? _hrName : "";
        }

        public String getAttendNo() {
            return String.valueOf(Integer.parseInt(_attendNo));
        }

        public String toString() {
            return _schregno + ":" + _name;
        }
    }

    private List createTestitems(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlTestitems();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int no = 0;
            while (rs.next()) {
                final String semester = rs.getString("SEMESTER");
                final String testCd = rs.getString("TESTCD");
                final String testName = rs.getString("TESTITEMNAME");

                no++;
                final Testitem testitem = new Testitem(semester, testCd, testName, String.valueOf(no));
                rtn.add(testitem);
            }
        } catch (final Exception ex) {
            log.error("テスト項目名のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlTestitems() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SEMESTER, ");
        stb.append("     TESTKINDCD || TESTITEMCD AS TESTCD, ");
        stb.append("     TESTITEMNAME ");
        stb.append(" FROM ");
        stb.append("     TESTITEM_MST_COUNTFLG_NEW ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' AND ");
        stb.append("     SEMESTER || TESTKINDCD || TESTITEMCD <= '" + _param.getRecordTestcd() + "' AND ");
        stb.append("     TESTKINDCD <> '99' ");
        stb.append(" ORDER BY ");
        stb.append("     SEMESTER, ");
        stb.append("     TESTKINDCD, ");
        stb.append("     TESTITEMCD ");
        return stb.toString();
    }

    private class Testitem {
        private final String _semester;
        private final String _testCd;
        private final String _testName;
        private final String _no;

        public Testitem(
                final String semester,
                final String testCd,
                final String testName,
                final String no
        ) {
            _semester = semester;
            _testCd = testCd;
            _testName = testName;
            _no = no;
        }

        public String toString() {
            return _semester + _testCd + ":" + _testName;
        }
    }

    /** 「100点に換算する」場合に表示するデータ */
    private class ConvertedScore {

        Map _avg3Map; // 3教科の学年/コース平均に表示する値のマップ
        Map _avg5Map; // 5教科の学年/コース平均に表示する値のマップ

        public void load(DB2UDB db2, final String semester, final String testCd) {
            try {
                _avg3Map = loadAvg(db2, "3", semester, testCd);
                _avg5Map = loadAvg(db2, "5", semester, testCd);
            } catch (SQLException e) {
                log.error("Exception! ConvertedScore#load ", e);
            }
        }

        /**
         * 生徒のデータからマップのキーを得る
         * @param student
         * @return
         */
        private String key(Student student) {
            String key = null;
            if (_param.isRankGrade()) {
                key = student._grade;
            } else {
                key = student._grade + student._course;
            }
            return key;
        }

        public String getAvg(String groupDiv, Student student) {
            final BigDecimal avg;
            if ("3".equals(groupDiv)) {
                avg = (BigDecimal) _avg3Map.get(key(student));
            } else {
                avg = (BigDecimal) _avg5Map.get(key(student));
            }
            return avg == null ? null : avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        /**
         * 学年/コースと3,5教科の学年/コース平均に表示する値のマップを得る。
         *  (最高得点 = 3,5教科の科目のRECORD_AVERAGE_DATのSCORE合計 / COUNT合計)
         * @param db2
         * @param groupDiv
         * @param semester
         * @param testKindCd
         * @param testItemCd
         * @return
         * @throws SQLException
         */
        private Map loadAvg(DB2UDB db2, String groupDiv, final String semester, final String testCd) throws SQLException {
            final Map map = new HashMap();
            final String sql = sqlAvg(groupDiv, semester, testCd);
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String key = _param.isRankGrade() ? _param._grade : _param._grade + rs.getString("COURSE");
                final BigDecimal totalScore = rs.getBigDecimal("SCORE");
                final BigDecimal count = rs.getBigDecimal("COUNT");
                final BigDecimal avg = totalScore == null ? null : totalScore.divide(count, 1, BigDecimal.ROUND_HALF_UP);
                map.put(key, avg);
            }
            return map;
        }

        private String sqlAvg(final String groupDiv, final String semester, final String testCd) {

            final String avgDiv = _param.isRankGrade() ? "1" : "3";

            final StringBuffer stb = new StringBuffer();
            stb.append(" with subclasscds as( ");
            stb.append("     select distinct ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
            } else {
                stb.append("          SUBCLASSCD ");
            }
            if (!_param.isRankGrade()) {
                stb.append("     , coursecd ");
                stb.append("     , majorcd ");
                stb.append("     , coursecode ");
            }
            stb.append("     from rec_subclass_group_dat t1 ");
            stb.append("     where ");
            stb.append("         t1.year = '" + _param._year + "' ");
            stb.append("         and t1.group_div = '" + groupDiv + "' ");
            stb.append("         and t1.grade = '" + _param._grade + "' ");
            stb.append(" ) select ");
            if (!_param.isRankGrade()) {
                stb.append("     coursecd || majorcd || coursecode as course, ");
            }
            stb.append("     sum(score) as score, ");
            stb.append("     sum(count) as count ");
            stb.append(" from ");
            stb.append("     record_average_dat t1 ");
            stb.append(" where ");
            stb.append("     t1.year = '" + _param._year + "' ");
            stb.append("     and t1.semester = '" + semester + "' ");
            stb.append("     and t1.testkindcd || t1.testitemcd = '" + testCd + "' ");
            stb.append("     and avg_div = '" + avgDiv + "' ");
            stb.append("     and t1.grade = '" + _param._grade + "' ");
            stb.append("     and t1.hr_class = '000' ");
            stb.append("     and exists (select 'X' from subclasscds where subclasscd = t1.subclasscd ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     AND CLASSCD = T1.CLASSCD ");
                stb.append("     AND SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            if (!_param.isRankGrade()) {
                stb.append("     and coursecd = t1.coursecd ");
                stb.append("     and majorcd = t1.majorcd ");
                stb.append("     and coursecode = t1.coursecode ");
            }
            stb.append("                   ) ");
            if (!_param.isRankGrade()) {
                stb.append("     group by t1.coursecd || t1.majorcd || t1.coursecode ");
            }
            return stb.toString();
        }
    }
}

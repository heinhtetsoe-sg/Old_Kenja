/**
 *
 * 学校教育システム 賢者 [成績管理]  通知票(智辯五條)
 * @author nakamoto
 * @version $Id: d7ccd86c2bd6edc95fe764394115996bfec10818 $
 *
 */

package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;


public class KNJD656 {

    private static final Log log = LogFactory.getLog(KNJD656.class);

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定

    private static final String FORM_FILE = "KNJD656.frm";

    private static final int MAX_LEN = 3;  //列数（３科目まで印刷）

    private static final int MAX_GYO = 30; //得点欄の行数

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
        final String groupCd = request.getParameter("GROUPCD");
        final String mockCd = request.getParameter("MOCKCD");
        final String grade[] = request.getParameterValues("CATEGORY_SELECTED");
        final String groupDiv = request.getParameter("GROUP_DIV");
        final String targetDiv = request.getParameter("TARGET_DIV");
        final String stfAuthCd = request.getParameter("STF_AUTH_CD");
        final String date = request.getParameter("DATE");

        final Param param = new Param(
                year,
                semester,
                groupCd,
                mockCd,
                grade,
                groupDiv,
                targetDiv,
                stfAuthCd,
                date
        );
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _groupCd;
        private final String _mockCd;
        private final String[] _grade;
        private final String _gradeIn;
        private final String _groupDiv;
        private final String _targetDiv;
        private final String _stfAuthCd;
        private final String _date;
        private final String _gengou;

        private String _mockGroupName;
        private String _mockName;
        
        public Param(
                final String year,
                final String semester,
                final String groupCd,
                final String mockCd,
                final String[] grade,
                final String groupDiv,
                final String targetDiv,
                final String stfAuthCd,
                final String date
        ) {
            _year = year;
            _semester = semester;
            _groupCd = groupCd;
            _mockCd = mockCd;
            _grade = grade;
            _gradeIn = setGradeIn(grade);
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(year));
            _gengou = gengou + "年度";
            _groupDiv = groupDiv;
            _targetDiv = targetDiv;
            _stfAuthCd = stfAuthCd;
            _date = date.replace('/', '-');
        }

        private String setGradeIn(final String grade[]){
            final StringBuffer rtn = new StringBuffer();
            for (int ia = 0; ia < grade.length; ia++) {
                if (ia==0) rtn.append("('");
                else       rtn.append("','");
                rtn.append(grade[ia]);
            }
            rtn.append("')");
            return rtn.toString();
        }

        private void load(final DB2UDB db2) throws SQLException {
            createMockGroupName(db2);
            createMockName(db2);
        }

        private void createMockGroupName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = sqlMockGroupName();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _mockGroupName = rs.getString("GROUPNAME1");
                }
            } catch (final Exception ex) {
                log.error("模試グループ名のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlMockGroupName() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GROUPNAME1 ");
            stb.append(" FROM ");
            stb.append("     MOCK_GROUP_MST ");
            stb.append(" WHERE ");
            stb.append("     GROUP_DIV = '" + _groupDiv + "' AND ");
            stb.append("     STF_AUTH_CD = '" + _stfAuthCd + "' AND ");
            stb.append("     GROUPCD = '" + _groupCd + "' ");
            return stb.toString();
        }

        private void createMockName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = sqlMockName();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _mockName = rs.getString("MOCKNAME2");
                }
            } catch (final Exception ex) {
                log.error("模試名のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlMockName() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MOCKNAME2 ");
            stb.append(" FROM ");
            stb.append("     MOCK_MST ");
            stb.append(" WHERE ");
            stb.append("     MOCKCD = '" + _mockCd + "' ");
            return stb.toString();
        }

        private String getMockGroupName() {
            return null != _mockGroupName ? _mockGroupName : "";
        }

        private String getMockName() {
            return null != _mockName ? _mockName : "";
        }
        
        private String getJishiDate() {
            return KNJ_EditDate.h_format_JP(_date) + "（" + KNJ_EditDate.h_format_W(_date) + "）実施";
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
            svf.VrSetForm( FORM_FILE, 4 );
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

        final List gdats = createGdats(db2);
        log.debug("学年数=" + gdats.size());

        for (final Iterator it = gdats.iterator(); it.hasNext();) {
            final Gdat gdat = (Gdat) it.next();
            //フォーム
            setSvfForm(svf);
            //ヘッダ
            printHeader(svf, gdat);
            //模試科目
            if (printSubClass(db2, svf, gdat)) rtnflg = true;
        }

        return rtnflg;
    }

    private void printHeader(final Vrw32alp svf, final Gdat gdat) {
        svf.VrsOut("NENDO"          ,  _param._gengou);
        svf.VrsOut("MOCK_GROUPNAME" ,  _param.getMockGroupName());
        svf.VrsOut("MOCK_NAME"      ,  _param.getMockName());
        svf.VrsOut("DATE"           ,  _param.getJishiDate());
        svf.VrsOut("GRADE"          ,  gdat._gradeName1);
    }

    private boolean printSubClass(final DB2UDB db2, final Vrw32alp svf, final Gdat gdat) throws SQLException {
        boolean rtnflg = false;

        final List subClasses = createSubClass(db2, gdat);
        int lenCnt = 0;
        for (final Iterator it = subClasses.iterator(); it.hasNext();) {
            final SubClass subclass = (SubClass) it.next();
            lenCnt++;
            //模試科目名
            svf.VrsOut("SUBCLASS" + String.valueOf(lenCnt) ,  subclass._subName );
            //到達度・得点
            int gyoCnt = printAssess(db2, svf, gdat, subclass);
            //合格・不合格
            printPass(svf, subclass.getPassCnt(), subclass.getFailCnt(), subclass.getPassPer(), subclass.getFailPer());
            //空行
            for (int gyo = 0; gyo < (MAX_GYO - gyoCnt); gyo++) {
                printKara(svf);
            }
            //印刷フラグ
            rtnflg = true;
        }
        //１or２科目なら３科目まで印刷
        if (rtnflg) {
            for (int len = 0; len < (MAX_LEN - lenCnt); len++) {
                //合格・不合格
                printPass(svf, "", "", "", "");
                //空行
                for (int gyo = 0; gyo < MAX_GYO; gyo++) {
                    printKara(svf);
                }
            }
        }

        return rtnflg;
    }

    private int printAssess(final DB2UDB db2, final Vrw32alp svf, final Gdat gdat, final SubClass subclass) throws SQLException {
        int gyoCnt = 0;
        final List assesses = createAssess(db2, gdat, subclass);
        for (final Iterator it = assesses.iterator(); it.hasNext();) {
            final Assess assess = (Assess) it.next();
            gyoCnt++;
            svf.VrsOut("LEVEL"      ,  assess._assessMark );
            svf.VrsOut("SCORE"      ,  assess.getScore() );
            svf.VrsOut("COUNT"      ,  assess._count );
            svf.VrsOut("CUMULATIVE" ,  assess._ruiseki );
            svf.VrsOut("PERCENTAGE" ,  assess._percent );
            svf.VrEndRecord();
            if (gyoCnt == MAX_GYO) break;
        }
        return gyoCnt;
    }

    private void printPass(final Vrw32alp svf, final String passCnt, final String failCnt, final String passPer, final String failPer) {
        svf.VrsOut("ITEM"           ,  "合格者数" );
        svf.VrsOut("TOTAL_COUNT"    ,  passCnt );
        svf.VrsOut("TOTALPERCENTAGE",  passPer );
        svf.VrEndRecord();
        svf.VrsOut("ITEM"           ,  "不合格者数" );
        svf.VrsOut("TOTAL_COUNT"    ,  failCnt );
        svf.VrsOut("TOTALPERCENTAGE",  failPer );
        svf.VrEndRecord();
    }

    private void printKara(final Vrw32alp svf) {
        svf.VrsOut("KARA",  "KARA");
        svf.VrEndRecord();
    }

    private List createGdats(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlGdats();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String gradeCd = rs.getString("GRADE_CD");
                final String gradeName1 = rs.getString("GRADE_NAME1");

                final Gdat gdat = new Gdat(grade, schoolKind, gradeCd, gradeName1);
                rtn.add(gdat);
            }
        } catch (final Exception ex) {
            log.error("学年のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }
    
    private String sqlGdats() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     GRADE, ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     rtrim(GRADE_CD) as GRADE_CD, ");
        stb.append("     GRADE_NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_GDAT ");
        stb.append(" WHERE ");
        stb.append("     GRADE in " + _param._gradeIn + " AND ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append(" ORDER BY ");
        stb.append("     GRADE ");
        return stb.toString();
    }

    private class Gdat {
        private final String _grade;
        private final String _schoolKind;
        private final String _gradeCd;
        private final String _gradeName1;

        public Gdat(
                final String grade,
                final String schoolKind,
                final String gradeCd,
                final String gradeName1
        ) {
            _grade = grade;
            _schoolKind = schoolKind;
            _gradeCd = gradeCd;
            _gradeName1 = gradeName1;
        }

        public String toString() {
            return _grade + ":" + _gradeName1;
        }
    }

    private List createSubClass(final DB2UDB db2, final Gdat gdat) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlSubClass(gdat);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int lenCnt = 0;
            while (rs.next()) {
                final String subCd = rs.getString("MOCK_SUBCLASS_CD");
                final String subName = rs.getString("SUBCLASS_NAME");
                final int allCnt = rs.getInt("ALL_CNT");
                final int passCnt = rs.getInt("PASS_CNT");
                final int failCnt = rs.getInt("FAIL_CNT");

                final SubClass subclass = new SubClass(subCd, subName, allCnt, passCnt, failCnt);
                rtn.add(subclass);
                //３科目まで印刷したら終了
                lenCnt++;
                if (lenCnt == MAX_LEN) break;
            }
        } catch (final Exception ex) {
            log.error("模試科目のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }
    
    private String sqlSubClass(final Gdat gdat) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.MOCK_SUBCLASS_CD, ");
        stb.append("     T3.SUBCLASS_NAME, ");
        stb.append("     count(T1.SCHREGNO) as ALL_CNT, ");
        stb.append("     sum(case when L1.PASS_SCORE <= T1.SCORE then 1 else 0 end) as PASS_CNT, ");
        stb.append("     sum(case when L1.PASS_SCORE >  T1.SCORE then 1 else 0 end) as FAIL_CNT ");
        stb.append(" FROM ");
        stb.append("     MOCK_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ");
        stb.append("             ON  T2.YEAR     = '" + _param._year + "' ");
        stb.append("             AND T2.SEMESTER = '" + _param._semester + "' ");
        stb.append("             AND T2.GRADE    = '" + gdat._grade + "' ");
        stb.append("             AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN MOCK_SUBCLASS_MST T3 ON T3.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("     LEFT JOIN MOCK_PERFECT_COURSE_DAT L1  ");
        stb.append("             ON  L1.YEAR = T1.YEAR ");
        stb.append("             AND L1.COURSE_DIV = '0' ");
        stb.append("             AND L1.MOCKCD = T1.MOCKCD ");
        stb.append("             AND L1.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("             AND L1.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE T2.GRADE END  ");
        stb.append("             AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = ");
        stb.append("                 CASE WHEN DIV = '01' OR DIV = '02' THEN '00000000' ");
        stb.append("                      ELSE T2.COURSECD || T2.MAJORCD || T2.COURSECODE END ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR   = '" + _param._year + "' AND ");
        stb.append("     T1.MOCKCD = '" + _param._mockCd + "' AND ");
        stb.append("     T1.SCORE IS NOT NULL ");
        stb.append(" GROUP BY ");
        stb.append("     T1.MOCK_SUBCLASS_CD, ");
        stb.append("     T3.SUBCLASS_NAME ");
        stb.append(" ORDER BY ");
        stb.append("     T1.MOCK_SUBCLASS_CD ");
        return stb.toString();
    }

    private class SubClass {
        private final String _subCd;
        private final String _subName;
        private final int _allCnt;
        private final int _passCnt;
        private final int _failCnt;

        public SubClass(
                final String subCd,
                final String subName,
                final int allCnt,
                final int passCnt,
                final int failCnt
        ) {
            _subCd = subCd;
            _subName = subName;
            _allCnt = allCnt;
            _passCnt = passCnt;
            _failCnt = failCnt;
        }

        private String getPassCnt() {
            return String.valueOf(_passCnt);
        }

        private String getFailCnt() {
            return String.valueOf(_failCnt);
        }

        private String getPassPer() {
            return getPercentage(_passCnt);
        }

        private String getFailPer() {
            return getPercentage(_failCnt);
        }
        
        private String getPercentage(int cnt) {
            return _allCnt == 0 ? "" : new BigDecimal(cnt * 100).divide(new BigDecimal(_allCnt), 1, BigDecimal.ROUND_HALF_UP).toString();
        }

        public String toString() {
            return _subCd + ":" + _subName;
        }
    }

    private List createAssess(final DB2UDB db2, final Gdat gdat, final SubClass subclass) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlAssess(gdat, subclass);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int ruiseki = 0;
            while (rs.next()) {
                final String seq = rs.getString("SEQ");
                final String assessMark = rs.getString("ASSESSMARK");
                final String assessLow = rs.getString("ASSESSLOW");
                final String assessHigh = rs.getString("ASSESSHIGH");
                final int count = rs.getInt("COUNT");
                final String percent = rs.getString("PERCENT");
                ruiseki = ruiseki + count;

                final Assess assess = new Assess(seq, assessMark, assessLow, assessHigh, String.valueOf(count), String.valueOf(ruiseki), percent);
                rtn.add(assess);
            }
        } catch (final Exception ex) {
            log.error("模試科目のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }
    
    private String sqlAssess(final Gdat gdat, final SubClass subclass) {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_ASSESS AS ( ");
        stb.append("     SELECT ");
        stb.append("         MOCK_SUBCLASS_CD, ");
        stb.append("         SEQ, ");
        stb.append("         ASSESSMARK, ");
        stb.append("         smallint(ASSESSLOW) as ASSESSLOW, ");
        stb.append("         smallint(ASSESSHIGH) as ASSESSHIGH ");
        stb.append("     FROM ");
        stb.append("         MOCK_SUBCLASS_ASSESS_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' AND ");
        stb.append("         GRADE = '" + gdat._grade + "' AND ");
        stb.append("         MOCK_SUBCLASS_CD = '" + subclass._subCd + "' ");
        stb.append("     ) ");
        stb.append(" , T_MOCK AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.MOCK_SUBCLASS_CD, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.SCORE ");
        stb.append("     FROM ");
        stb.append("         MOCK_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_REGD_DAT T2 ");
        stb.append("                 ON  T2.YEAR     = '" + _param._year + "' ");
        stb.append("                 AND T2.SEMESTER = '" + _param._semester + "' ");
        stb.append("                 AND T2.GRADE    = '" + gdat._grade + "' ");
        stb.append("                 AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         INNER JOIN MOCK_SUBCLASS_MST T3 ON T3.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR   = '" + _param._year + "' AND ");
        stb.append("         T1.MOCKCD = '" + _param._mockCd + "' AND ");
        stb.append("         T1.MOCK_SUBCLASS_CD = '" + subclass._subCd + "' AND ");
        stb.append("         T1.SCORE IS NOT NULL ");
        stb.append("     ) ");
        stb.append(" , T_SEQ_CNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         L2.SEQ, ");
        stb.append("         sum(case when T1.SCORE BETWEEN L2.ASSESSLOW AND L2.ASSESSHIGH then 1 else 0 end) as SEQ_CNT ");
        stb.append("     FROM ");
        stb.append("         T_MOCK T1 ");
        stb.append("         INNER JOIN T_ASSESS L2 ON L2.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("     GROUP BY ");
        stb.append("         L2.SEQ ");
        stb.append("     ) ");
        stb.append(" , T_MARK_CNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         L2.ASSESSMARK, ");
        stb.append("         count(distinct T1.SCHREGNO) as ALL_CNT, ");
        stb.append("         sum(case when T1.SCORE BETWEEN L2.ASSESSLOW AND L2.ASSESSHIGH then 1 else 0 end) as MARK_CNT ");
        stb.append("     FROM ");
        stb.append("         T_MOCK T1 ");
        stb.append("         INNER JOIN T_ASSESS L2 ON L2.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("     GROUP BY ");
        stb.append("         L2.ASSESSMARK ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.ASSESSMARK, ");
        stb.append("     T1.ASSESSLOW, ");
        stb.append("     T1.ASSESSHIGH, ");
        stb.append("     L1.SEQ_CNT AS COUNT, ");
        stb.append("     decimal(round((float(L2.MARK_CNT)/L2.ALL_CNT)*100*10,0)/10,5,1) AS PERCENT ");
        stb.append(" FROM ");
        stb.append("     T_ASSESS T1 ");
        stb.append("     LEFT JOIN T_SEQ_CNT L1 ON L1.SEQ = T1.SEQ ");
        stb.append("     LEFT JOIN T_MARK_CNT L2 ON L2.ASSESSMARK = T1.ASSESSMARK ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SEQ ");
        return stb.toString();
    }

    private class Assess {
        private final String _seq;
        private final String _assessMark;
        private final String _assessLow;
        private final String _assessHigh;
        private final String _count;
        private final String _ruiseki;
        private final String _percent;

        public Assess(
                final String seq,
                final String assessMark,
                final String assessLow,
                final String assessHigh,
                final String count,
                final String ruiseki,
                final String percent
        ) {
            _seq = seq;
            _assessMark = assessMark;
            _assessLow = assessLow;
            _assessHigh = assessHigh;
            _count = count;
            _ruiseki = ruiseki;
            _percent = percent;
        }

        private String getScore() {
            if (_assessLow == null) return "";
            if (_assessLow.equals(_assessHigh)) return _assessLow;
            return _assessLow + "\uFF5E" + _assessHigh;
        }

        public String toString() {
            return _seq + ":" + _assessMark;
        }
    }
    
}

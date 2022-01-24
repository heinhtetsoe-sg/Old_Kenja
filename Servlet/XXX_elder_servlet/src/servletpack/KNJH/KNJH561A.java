package servletpack.KNJH;

import java.io.IOException;
import java.math.BigDecimal;
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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 実力テスト　成績一覧
 * $Id: b5a5981c0966905ec94e8cba957dcdffdc6eb70b $
 * 
 * @author maesiro
 *
 */

public class KNJH561A {

    private static final Log log = LogFactory.getLog(KNJH561A.class);
    
    private static final String FORM_NAME  = "KNJH561A.frm";
    private boolean _hasData;
    private Param _param;
    
    /** 合計の科目コード */
    private final String SUBCLASSCD_ALL3 = "333333";
    private final String SUBCLASSCD_ALL5 = "555555";
    private final String SUBCLASSCD_ALL9 = "999999";
    
    
    /** 出力種別 */
    /**   : クラス */
    public final int PRINT_TYPE_CLASS = 1;
    /**   : コース */
    public final int PRINT_TYPE_COURSECD = 2;
    /**   : 学年 */
    public final int PRINT_TYPE_GRADE = 3;
    
    /** ソート種別 */
    /** : 3教科 */
    public final int SORT_TYPE_3SUBCLASSES = 2;
    /** : 年組番号順 */
    public final int SORT_TYPE_ATTENDNO = 3;

    public final String RANK_DATA_DIV_SCORE = "01";
    
    public final String RANK_DIV_GRADE = "01";
    public final String RANK_DIV_HRCLASS = "02";
    public final String RANK_DIV_COURSE = "03";
    public final String RANK_DIV_COURSEGROUP = "05";
    
    private final int INVALID_INDEX = -100; // 無効なインデックス
    private final int MAX_LINE = 45;
    private final int MAX_SUBCLASS = 5;
    
    /**
     * KNJH.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;

        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            
            _param = createParam(db2, request);
            
            _hasData = printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 1);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            close(db2, svf);
        }
    }
    
    /**
     * 印刷処理メイン
     * @param db2   ＤＢ接続オブジェクト
     * @param svf   帳票オブジェクト
     * @return      対象データ存在フラグ(有り：TRUE、無し：FALSE)
     * @throws Exception
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        boolean hasData = false;
        final List studentGroupList = new ArrayList();
        
        for (int i = 0; i < _param._categorySelected.length; i++) {
            studentGroupList.add(createStudents(db2, _param._categorySelected[i]));
        }

        for (final Iterator it = studentGroupList.iterator(); it.hasNext();) {
            final StudentGroup group = (StudentGroup) it.next();
            if (group._studentList.size() == 0) {
                log.debug(" group " + group._groupCode + ": " + group._groupName + " student list size 0 ");
                continue;
            }
            group.load(db2);
            hasData = outPutPrint(svf, group) || hasData;
        }
            
        // 帳票出力のメソッド
        return hasData;
    }
    
    private List getListList(final List list, final int countPerPage) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= countPerPage) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private int getMS932ByteLength(final String s) {
        int length = 0;
        if (null != s) {
            try {
                length = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return length;
    }
    
    /**
     * 帳票出力処理
     * @param svf       帳票オブジェクト
     * @param students   帳票出力対象クラスオブジェクト
     * @return      対象データ存在フラグ(有り：TRUE、無し：FALSE)
     */
    private boolean outPutPrint(final Vrw32alp svf, final StudentGroup group) {
        boolean hasdata = false;
        
        Collections.sort(group._studentList);
        
        svf.VrSetForm(FORM_NAME, 1);

        final List studentListList = getListList(group._studentList, MAX_LINE);
        final List subclassListList = getListList(group._subclassList, MAX_SUBCLASS);
        log.debug(" student all size = " + group._studentList.size());
        
        for (int stdPagei = 0; stdPagei < studentListList.size(); stdPagei++) {
            
            final boolean isLastStudentsPage = stdPagei == studentListList.size() - 1;
            
            final List studentsPerPage = (List) studentListList.get(stdPagei);
            log.debug(" studentPerPage = " + studentsPerPage.size());
            
            log.debug(" subclass list list = " + subclassListList);
            for (int subcPagei = 0; subcPagei < subclassListList.size(); subcPagei++) {

                setHead(svf, group);

                final boolean isLastSubclassPage = subcPagei == subclassListList.size() - 1;
                
                final List subclassPerPage = (List) subclassListList.get(subcPagei);
                // 科目名設定
                for(int i = 0; i < subclassPerPage.size(); i++) {
                    final String subclassName = group.getSubclassName((String) subclassPerPage.get(i));
                    svf.VrsOut("SUBCLASS" + (i + 1), subclassName);
                }
                
                int outputLineCount = stdPagei * MAX_LINE;
                for (int ki = 0; ki < studentsPerPage.size(); ki++) {
                    final Student student = (Student) studentsPerPage.get(ki);
                    final int k = ki + 1;
                    outputLineCount += 1;
                    
                    svf.VrsOutn("NUMBER", k, String.valueOf(outputLineCount)); // 連番
                    svf.VrsOutn("HR_NAME", k, student._hrname); // クラス
                    svf.VrsOutn("ATTENDNO", k, Integer.valueOf(student._attendno).toString()); // 出席番号
                    final int len = getMS932ByteLength(student._name);
                    svf.VrsOutn("NAME" + (len > 30 ? "3" : len > 20 ? "2" : ""), k, student._name); // 氏名
                    svf.VrsOutn("SEX", k, student._sexname); // 性別
                    
                    for(int i = 0; i < subclassPerPage.size(); i++) {
                        final String j = String.valueOf(i + 1);
                        final SubclassScore subScore = (SubclassScore) student._scores.get((String) subclassPerPage.get(i));
                        if (null != subScore) {
                            svf.VrsOutn("SCORE" + j, k, subScore._score); // 得点
                            final RankDev rankDev = "2".equals(_param._groupDiv) ? subScore._course : "3".equals(_param._groupDiv) ? subScore._coursegroup : subScore._grade;
                            svf.VrsOutn("DEV" + j, k, rankDev._dev); // 偏差値
                            svf.VrsOutn("RANK" + j, k, rankDev._rank); // 順位
                        }
                    }
                    
                    if (isLastSubclassPage) {
                        final SubclassScore all = student._all3;
                        final RankDev rank3 = "2".equals(_param._groupDiv) ? all._course : "3".equals(_param._groupDiv) ? all._coursegroup : all._grade;
                        if (group._print3subclassTotal) {
                            svf.VrsOutn("TOTAL_SCORE", k, all._score); // 合計
                            svf.VrsOutn("TOTAL_AVERAGE", k, bigDecimalToStr(all._avg)); // 平均
                            svf.VrsOutn("TOTAL_DEV", k, rank3._dev); // 偏差値
                            svf.VrsOutn("TOTAL_RANK", k, rank3._rank); // 順位
                        }
                    }
                    hasdata = true;
                }
                if (isLastStudentsPage) {
                    setGradeAverage(svf, group, subclassListList, group._studentList);
                }
                svf.VrEndPage();
            }
        }
        log.debug("hasdata = " + hasdata);
        return hasdata;
    }

    /**
     * 学年平均をセットする。
     * @param db2
     * @param svf
     * @param all3totalScore 全3教科の平均値の和。学生の'全3教科平均'の算出に用いる。
     * @param all5totalScore 全5教科の平均値の和。学生の'全5教科平均'の算出に用いる。
     * @param studentCount 全教科の平均値のレコードを持つ学生の数。学生の'全*教科平均'の算出に用いる。
     */
    private void setGradeAverage(final Vrw32alp svf, final StudentGroup group, final List subclassListList, final List studentList) {
        final int lastline = MAX_LINE + 1;
        for (int i = 0; i < subclassListList.size(); i++) {
            final List subclassList = (List) subclassListList.get(i);
            for (int j = 0; j < subclassList.size(); j++) {
                final String subclassCd = (String) subclassList.get(j);

                final AverageDat avgDat = (AverageDat) group._averageMap.get(subclassCd);
                if (null == avgDat) {
                    log.fatal(" no avgdat : " + subclassCd);
                    continue;
                }
                svf.VrsOutn("SCORE" + (j + 1), lastline, avgDat.getAvg()); // 得点
            }
        }
        final List avgavgList = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null != student._all3) {
                final SubclassScore all = student._all3;
                if (null != all._avg) {
                    avgavgList.add(all._avg);
                }
            }
        }
        svf.VrsOutn("TOTAL_AVERAGE", lastline, getListAvg(avgavgList)); // 平均
        final AverageDat avgDat = (AverageDat) group._averageMap.get(SUBCLASSCD_ALL3);
        if(null != avgDat) {
            svf.VrsOutn("TOTAL_SCORE", lastline, avgDat.getAvg()); // 合計
        }
    }
    
    /**
     * 数値のリストの平均(四捨五入)を算出する
     * @param valueList リスト
     * @return 数値のリストの平均
     */
    private String getListAvg(final List valueList) {
        BigDecimal sum = new BigDecimal("0");
        int count = 0;
        for (final Iterator it = valueList.iterator(); it.hasNext();) {
            final BigDecimal num = (BigDecimal) it.next();
            sum = sum.add(num);
            count += 1;
        }
        if (0 == count) {
            return null;
        }
        return sum.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    /**
     * ヘッダ部の出力を行う
     * @param svf       帳票オブジェクト
     */
    private void setHead(final Vrw32alp svf, final StudentGroup group) {
        final String course = PRINT_TYPE_COURSECD == _param._printType ?  group._coursecodename + "コース" : "";
        svf.VrsOut("NENDO", "第" + String.valueOf(Integer.parseInt(group._grade)) + "学年" + course + "　" + _param.getNendo());
        svf.VrsOut("DATE", _param.getDate());
        svf.VrsOut("SELECT_NAME", _param.getSelectName(group));
        svf.VrsOut("SORT", _param.getSortTypeName());
        svf.VrsOut("TESTNAME", _param._testname + "成績一覧表");
        
        if (group._print3subclassTotal) {
            svf.VrsOut("ITEM3", group._subclassGroupNameMap.get("3").toString());
        }
        if (group._print5subclassTotal) {
            svf.VrsOut("ITEM5", group._subclassGroupNameMap.get("5").toString());
        }
    }
    
    public boolean isAll(final String subclassCd) {
        return
        SUBCLASSCD_ALL3.equals(subclassCd) ||
        SUBCLASSCD_ALL5.equals(subclassCd) ||
        SUBCLASSCD_ALL9.equals(subclassCd);
    }

    /**
     * @param db2           ＤＢ接続オブジェクト
     * @return              帳票出力対象データリスト
     * @throws Exception
     */
    private StudentGroup createStudents( final DB2UDB db2, final String selected) throws SQLException {
        
        final StudentGroup group = new StudentGroup(selected);
        
        final String sql = getStudentSql(selected);
        log.debug("帳票出力対象データ抽出 の SQL=" + sql);

        final PreparedStatement ps = db2.prepareStatement(sql);
        final ResultSet rs = ps.executeQuery();
        try {
            final Map studentMap = new HashMap();
            while (rs.next()) {

                final String schregno = rs.getString("SCHREGNO");
                if (null == studentMap.get(schregno)) {
                    final String hrname = rs.getString("HR_NAME");
                    final Student student = new Student(
                            rs.getString("YEAR"),
                            schregno,
                            rs.getString("GRADE"),
                            rs.getString("CLASS1"),
                            hrname,
                            rs.getString("COURSECODE1"),
                            rs.getString("ATTENDNO"),
                            rs.getString("NAME"),
                            rs.getString("SEXNAME")
                    );
                    group._grade = rs.getString("GRADE");
                    group._gradehrclass = rs.getString("CLASS1");
                    group._hrname = hrname;
                    group._coursecode = rs.getString("COURSECODE1");
                    group._coursecodename = rs.getString("COURSECODENAME");
                    group._groupCode = rs.getString("GROUP_CD");
                    group._groupName = rs.getString("GROUP_NAME");
                    group._studentList.add(student);
                    studentMap.put(schregno, student);
                }

                final Student student = (Student) studentMap.get(schregno);
                
                //log.debug(" get Student="+student);
                
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String setScoreVal = null == rs.getString("SCORE") ? rs.getString("SCORE_DI") : rs.getString("SCORE");
                student.setScoreRank(
                        subclassCd,
                        setScoreVal,
                        rs.getBigDecimal("AVG"),
                        rs.getString("CLASS_RANK"),
                        rs.getString("GRADE_RANK"),
                        rs.getString("COURSE_RANK"),
                        rs.getString("COURSEGROUP_RANK"),
                        rs.getString("CLASS_DEVIATION"),
                        rs.getString("GRADE_DEVIATION"),
                        rs.getString("COURSE_DEVIATION"),
                        rs.getString("COURSEGROUP_DEVIATION"));
            }
           
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return group;
    }
    
    private class StudentGroup {
        final String _code;
        String _grade;
        String _gradehrclass;
        String _hrname;
        String _coursecode;
        String _coursecodename;
        String _groupCode;
        String _groupName;

        private boolean _print3subclassTotal;
        private boolean _print5subclassTotal;

        final List _studentList = new ArrayList();
        List _subclassList = Collections.EMPTY_LIST;
        Map _subclassMap = Collections.EMPTY_MAP;
        Map _averageMap = Collections.EMPTY_MAP;
        Map _subclassGroupNameMap = Collections.EMPTY_MAP;
//        Set _attendSubclasses = Collections.EMPTY_SET;
        
        StudentGroup(final String code) {
            _code = code;
        }
        
        public void load(DB2UDB db2) {
//            _attendSubclasses = getAttendSubclasses(db2);
            
            setSubclass(db2);

            _averageMap = getAverage(db2);
            
            _subclassGroupNameMap = getSubclassGroupNameMap(db2);
        }
        
//        private Set getAttendSubclasses(final DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            Set attendSubclass = new TreeSet();
//            try {
//                final String sql = getSubclassReplaceCmbSql();
//                log.debug(" replace_cmb sql = " + sql);
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    attendSubclass.add(rs.getString("ATTEND_SUBCLASSCD"));
//                }
//            } catch (final Exception e) {
//                log.error("exception!", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return attendSubclass;
//        }

        /**
         * 科目コードのセットされているとき、科目表示のインデクスを返す。
         * @param subclassCd
         * @return
         */
        private int getSubclassIndex(final String subclassCd) {
            if (null == subclassCd) {
                return INVALID_INDEX;
            }
            for(int i = 0; i < _subclassList.size(); i++) {
                if (subclassCd.equals(_subclassList.get(i))) {
                    return i;
                }
            }
            
            // 科目が見つからない
            log.debug("科目が見つかりません: 科目コード="+subclassCd);
            return INVALID_INDEX;
//            // for error message
//            StringBuffer stb = new StringBuffer();
//            for(int i=0; i<_subclassCds.length; i++)
//                stb.append(_subclassCds[i]+",");
//            throw new ArrayIndexOutOfBoundsException("error subclassCd="+subclassCd+ ",("+stb.toString()+")");
        }
        
        private String getSubclassName(final String subclassCd) {
            return (String) _subclassMap.get(subclassCd);
        }
        
        /**
         * 科目コードをセットする
         * @param db2   ＤＢ接続オブジェクト
         * @return
         */
        private void setSubclass(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            _print3subclassTotal = false;
            _print5subclassTotal = false;
            
            final String sql = getSubclassSql();
            log.debug("getSubclass sql = " + sql);
            _subclassList = new ArrayList();
//            log.debug("中高一貫? "+_param._isChuKoIkkan);
            _subclassMap = new HashMap();

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
//                int counter = 1;
                while (rs.next()) {
                    if (null == rs.getString("SUBCLASS_NAME")) {
                        continue;
                    }
                    
                    final String iv = rs.getString("GROUP_DIV");
                    if ("3".equals(iv)) { _print3subclassTotal = true; }
                    if ("5".equals(iv)) { _print5subclassTotal = true; }
                    final String subclassCd = rs.getString("SUBCLASSCD");
//                    if (_attendSubclasses.contains(subclassCd)) {
//                        continue;
//                    }
                    final String subclassName = rs.getString("SUBCLASS_NAME");
                    if (!_subclassList.contains(subclassCd)) {
                        _subclassList.add(subclassCd);
                        _subclassMap.put(subclassCd, subclassName);
//                        counter += 1;
                        log.debug("テスト科目名 = " + subclassName);
                    }
                }
                
            } catch (Exception ex) {
                log.debug("setSubclass exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private String getSubclassSql() {
            boolean isHighschool = !_param._isChuKoIkkan || _param._isChuKoIkkan && Integer.parseInt(_grade) >= 4 ;
            boolean outputBefore = false;
            
            StringBuffer stb = new StringBuffer();
            if (PRINT_TYPE_CLASS == _param._printType) {
                if (outputBefore) { stb.append(" , "); }
                else { stb.append(" with "); }
                outputBefore = true;
                stb.append(" T_SCHREG AS ( ");
                stb.append("   select  ");
                stb.append("       t1.GRADE, ");
                stb.append("       t1.COURSECD || t1.MAJORCD || t1.COURSECODE as code ");
                stb.append("   from ");
                stb.append("       SCHREG_REGD_DAT t1 ");
                stb.append("   where ");
                stb.append("     t1.YEAR = '" + _param._year + "' and ");
                stb.append("     t1.SEMESTER = '" + _param._semester + "' and ");
                stb.append("     t1.GRADE || t1.HR_CLASS = '" + _code + "' ");
                stb.append("   group by ");
                stb.append("     t1.GRADE, t1.COURSECD || t1.MAJORCD || t1.COURSECODE ");
                stb.append(" ) ");
            }
            if (PRINT_TYPE_COURSECD == _param._printType || PRINT_TYPE_CLASS == _param._printType) {
                if (outputBefore) { stb.append(" , "); }
                else { stb.append(" with "); }
                outputBefore = true;
                stb.append(" TA AS ( ");
                stb.append("   select  ");
                stb.append("       t1.GRADE, ");
                stb.append("       t1.GROUP_DIV, ");
                stb.append("       t1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD");
                stb.append("   from ");
                stb.append("       PROFICIENCY_SUBCLASS_GROUP_DAT t1 ");
                stb.append("   where ");
                stb.append("     t1.PROFICIENCYDIV = '" + _param._proficiencydiv + "' and ");
                stb.append("     t1.PROFICIENCYCD = '" + _param._proficiencycd + "' and ");
                stb.append("     t1.YEAR = '" + _param._year + "' and ");
                stb.append("     t1.SEMESTER = '" + _param._semester + "' and ");
                if (PRINT_TYPE_CLASS == _param._printType) {
                    stb.append("    exists (select 'X' from T_SCHREG where grade = t1.GRADE and code = t1.COURSECD || t1.MAJORCD || t1.COURSECODE) ");
                } else if (PRINT_TYPE_COURSECD == _param._printType) {
                    stb.append("    t1.COURSECD || t1.MAJORCD || t1.COURSECODE = '" + _code + "' ");
                }
                stb.append("   group by ");
                stb.append("     t1.GRADE, t1.GROUP_DIV, t1.PROFICIENCY_SUBCLASS_CD ");
                stb.append(" ) ");
            }
            if (isHighschool || true) {
                if (outputBefore) { stb.append(" , "); }
                else { stb.append(" with "); }
                outputBefore = true;
                stb.append(" T_HIGH AS ( ");
                stb.append("   select ");
                stb.append("     t1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD ");
                stb.append("   from ");
                stb.append("     PROFICIENCY_RANK_DAT t1");
                stb.append("     inner join SCHREG_REGD_DAT t2 on");
                stb.append("        t1.YEAR = t2.YEAR and");
                stb.append("        t1.SEMESTER = t2.SEMESTER and");
                stb.append("        t2.SCHREGNO = t1.SCHREGNO and");
                stb.append("        t2.GRADE = '" + _param._grade + "'");
                stb.append("   where ");
                stb.append("     t1.YEAR = '" + _param._year + "' ");
                stb.append("     and t1.SEMESTER = '" + _param._semester + "' ");
                if (PRINT_TYPE_CLASS == _param._printType) {
                    stb.append("    and t2.GRADE || t2.HR_CLASS = '" + _code + "' ");
                } else if (PRINT_TYPE_COURSECD == _param._printType) {
                    stb.append("    and t2.COURSECD || t2.MAJORCD || t2.COURSECODE = '" + _code + "' ");
                }
                stb.append("   group by ");
                stb.append("     t1.PROFICIENCY_SUBCLASS_CD ");
                stb.append(" ) ");
            }
            stb.append(" select ");
            stb.append("   t1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("   t1.GROUP_DIV, ");
            stb.append("   t2.SUBCLASS_ABBV AS SUBCLASS_NAME ");
            stb.append(" from PROFICIENCY_SUBCLASS_GROUP_DAT t1 ");
            if (PRINT_TYPE_COURSECD == _param._printType || PRINT_TYPE_CLASS == _param._printType) {
                stb.append("  inner join TA on t1.PROFICIENCY_SUBCLASS_CD = TA.SUBCLASSCD and t1.GROUP_DIV = TA.GROUP_DIV ");
            }
            if (isHighschool || true) {
                stb.append("  inner join T_HIGH on ");
                stb.append("    t1.PROFICIENCY_SUBCLASS_CD = T_HIGH.SUBCLASSCD ");
            }
            stb.append(" left join PROFICIENCY_SUBCLASS_MST t2 on ");
            stb.append("   t1.PROFICIENCY_SUBCLASS_CD = t2.PROFICIENCY_SUBCLASS_CD ");
            stb.append(" where ");
            stb.append("   t1.GRADE = '" + _param._grade + "' ");
            stb.append(" group by ");
            stb.append("     t1.PROFICIENCY_SUBCLASS_CD, t2.SUBCLASS_ABBV, t1.GROUP_DIV ");
            stb.append(" order by ");
            stb.append("     t1.PROFICIENCY_SUBCLASS_CD ");

            return stb.toString();
        }
        
        private Map getAverage(final DB2UDB db2) {
            final String sql = getAverageSql();
            log.debug("getAverageSql sql=" + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map _averageMap = new HashMap();
            try{
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final AverageDat avgDat = new AverageDat(subclassCd, rs.getString("SCORE"), rs.getBigDecimal("AVG"));
                    _averageMap.put(subclassCd, avgDat);
                }
            } catch(Exception ex) {
                log.debug("setGradeAverage exception", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return _averageMap;
        }
        
        /*
         * 学年平均のSQL
         */
        private String getAverageSql() {
            final String whereGradeClass = PRINT_TYPE_CLASS == _param._printType ? _gradehrclass : _grade + "000" ;
            final String whereCourseCdMajorCdCourseCode = PRINT_TYPE_COURSECD == _param._printType ? _coursecode : "00000000" ;
                
            final StringBuffer stb = new StringBuffer();
            stb.append(" select ");
            stb.append("     PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("     SCORE, ");
            stb.append("     AVG ");
            stb.append(" from ");
            stb.append("     PROFICIENCY_AVERAGE_DAT T1 ");
            stb.append(" where ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     and SEMESTER = '" + _param._semester + "' ");
            stb.append("     and t1.PROFICIENCYDIV = '" + _param._proficiencydiv + "' ");
            stb.append("     and t1.PROFICIENCYCD = '" + _param._proficiencycd + "' ");
            stb.append("     and DATA_DIV = '" + _param._avgDataDiv + "' ");
            stb.append("     and AVG_DIV = '" + _param._avgDiv + "' ");
            if (PRINT_TYPE_GRADE !=_param._printType) {
                stb.append("     and GRADE = '" + _grade + "' ");
            }
            stb.append("     and GRADE || HR_CLASS = '" + whereGradeClass + "' ");
            stb.append("     and COURSECD || MAJORCD || COURSECODE = '" + whereCourseCdMajorCdCourseCode + "' ");
            return stb.toString();
        }
        
        public Map getSubclassGroupNameMap(final DB2UDB db2) {
            final Map rtn = new HashMap();
            
            if (_param._printType == PRINT_TYPE_COURSECD && _param._useSubclassGroup) {
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    
                    final String sql = getSubclassGroupMstSql(); 
                    log.debug("sql = " + sql);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String groupDiv = rs.getString("GROUP_DIV");
                        final String groupName = rs.getString("GROUP_NAME");
                        rtn.put(groupDiv, groupName);
                    }
                } catch (Exception ex) {
                    log.error("getSubclassGroupNameMap exception", ex);
                } finally {
                    DbUtils.closeQuietly(rs);
                    db2.commit();
                }
            } else {
                rtn.put("3", "3教科");
                rtn.put("5", "5教科");
            }
            return rtn;
        }
        
        private String getSubclassGroupMstSql() {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     GROUP_DIV, GROUP_NAME "); 
            sql.append(" FROM ");
            sql.append("     PROFICIENCY_SUBCLASS_GROUP_MST ");
            sql.append(" WHERE YEAR = '" + _param._year + "' AND ");
            sql.append("   SEMESTER = '" + _param._semester + "' AND ");
            sql.append("   PROFICIENCYDIV = '" + _param._proficiencydiv + "' AND ");
            sql.append("   PROFICIENCYCD = '" + _param._proficiencycd + "' AND ");
            sql.append("   GRADE = '" +_grade + "' AND ");
            sql.append("   COURSECD || MAJORCD || COURSECODE = '" + _coursecode + "'");
            return sql.toString();
        }
        
//        /** 合併科目取得SQL */
//        private String getSubclassReplaceCmbSql() {
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     T1.DIV, T1.COMBINED_SUBCLASSCD, T1.ATTEND_SUBCLASSCD ");
//            stb.append(" FROM ");
//            stb.append("     PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT T1 ");
//            stb.append(" WHERE ");
//            stb.append("     T1.YEAR = '" + _param._year + "' ");
//            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
//            stb.append("     AND T1.PROFICIENCYDIV = '" + _param._proficiencydiv + "' ");
//            stb.append("     AND T1.PROFICIENCYCD = '" + _param._proficiencycd + "' ");
//            stb.append("     AND T1.GRADE = '" + _param._grade +"' ");
//            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ");
//            stb.append("       CASE WHEN T1.DIV = '04' THEN '0' || '" + _groupCode + "' || '0000' ");
//            stb.append("       ELSE '" + _coursecode + "' END ");
//            stb.append(" ORDER BY ");
//            stb.append("     T1.DIV, ");
//            stb.append("     T1.COMBINED_SUBCLASSCD, ");
//            stb.append("     T1.ATTEND_SUBCLASSCD ");
//            return stb.toString();
//        }
    }
    
    private static String bigDecimalToStr(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    private class AverageDat {
        final String _subclasscd;
        final String _score;
        final BigDecimal _avg;
        public AverageDat(final String subclasscd, final String score, final BigDecimal avg) {
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
        }
        public String getAvg() {
            return bigDecimalToStr(_avg);
        }
    }
    
    private class SubclassScore {
        String _score;
        BigDecimal _avg;
        RankDev _hr = new RankDev();
        RankDev _grade  = new RankDev();
        RankDev _course = new RankDev();
        RankDev _coursegroup = new RankDev();
    }

    private class RankDev {
        String _rank;
        String _dev;
        public String toString() {
            return "RankDev(" + _rank + ", " + _dev + ")";
        }
    }
    
    private String getRankStr(final Student student) {
        String rankStr = null;
        if (SORT_TYPE_3SUBCLASSES == _param._sortType) {

            SubclassScore _ss;
            _ss = student._all3;
            
            RankDev rd = null;
            if (PRINT_TYPE_CLASS == _param._printType) {
                rd = _ss._hr;
            } else if (PRINT_TYPE_GRADE == _param._printType) {
                rd = _ss._grade;
            } else if (PRINT_TYPE_COURSECD == _param._printType) {
                rd = _ss._course;
            }
            rankStr = rd._rank;

        } else if (SORT_TYPE_ATTENDNO == _param._sortType) {
            rankStr = "0";
        }
        return rankStr;
    }

    /** 生徒クラス */
    private class Student implements Comparable {
        final Map _scores = new HashMap();// 最大9科目
        final String _year;
        final String _schregno;
        final String _grade;
        final String _classNo;
        final String _hrname;
        final String _courseCd;
        final String _attendno;
        final String _name;
        final String _sexname;
        
        final SubclassScore _all3 = new SubclassScore();
//        final SubclassAll _all5 = new SubclassAll();
//        final SubclassAll _all9 = new SubclassAll();
        
        Student(final String year, final String schregno, final String grade, final String classNo, final String hrname, final String courseCd, final String attendno, 
                final String name, String sexname
        ) {
            _year = year;
            _schregno = schregno;
            _grade = grade;
            _classNo = classNo;
            _hrname = hrname;
            _courseCd = courseCd;
            _attendno = attendno;
            _name = name;
            _sexname  =sexname;
        }
                
        public void setScoreRank(final String subclassCd, final String setScoreVal, final BigDecimal avg,
                final String classRank, final String gradeRank, final String courseRank, final String coursegroupRank,
                final String classDev, final String gradeDev, final String courseDev, final String coursegroupDev) {
            
            if (!isAll(subclassCd)) {
                final SubclassScore score = new SubclassScore();
                score._score = setScoreVal;
                score._avg = avg;
                score._hr._rank = classRank;
                score._grade._rank = gradeRank;
                score._course._rank = courseRank;
                score._coursegroup._rank = coursegroupRank;
                score._hr._dev = classDev;
                score._grade._dev = gradeDev;
                score._course._dev = courseDev;
                score._coursegroup._dev = coursegroupDev;
                _scores.put(subclassCd, score);
                return;
            }            
            
            SubclassScore all = new SubclassScore();
            
            if (SUBCLASSCD_ALL3.equals(subclassCd)) {
                all = _all3;
//            } else if (SUBCLASSCD_ALL5.equals(subclassCd)) {
//                all = _all5;
//            } else if (SUBCLASSCD_ALL9.equals(subclassCd)) {
//                all = _all9;
            }
            all._score = setScoreVal;
            all._avg = avg;
            all._hr._rank = classRank;
            all._grade._rank = gradeRank;
            all._course._rank = courseRank;
            all._coursegroup._rank = coursegroupRank;
            all._hr._dev = classDev;
            all._grade._dev = gradeDev;
            all._course._dev = courseDev;
            all._coursegroup._dev = coursegroupDev;

        }
        
        public boolean equals(Object o) {
            if (!(o instanceof Student)) {
                return false;
            }
            Student other = (Student) o;
            return (other._year.equals(_year) && other._schregno.equals(_schregno));
        }
        
        public String toString() {
            return "[" + _classNo + "." + _attendno + "] (" + _all3._hr._rank + ") (" + _all3._grade._rank + ") (" + _all3._course._rank + ")";
        }
        
        public int compareTo(Object object) {
            if (!(object instanceof Student)) {
                return 1;
            }

            final Student an = (Student) object;

            final int cmpGrade = Integer.valueOf(_grade).compareTo(Integer.valueOf(an._grade));
            if (0 != cmpGrade) {
                return cmpGrade;
            }
            final int cmpclassno = Integer.valueOf(_classNo).compareTo(Integer.valueOf(an._classNo));
            if (PRINT_TYPE_CLASS == _param._printType && 0 != cmpclassno) {
                return cmpclassno;
            } else if (PRINT_TYPE_COURSECD == _param._printType && Integer.valueOf(_courseCd).compareTo(Integer.valueOf(an._courseCd)) != 0) {
                return Integer.valueOf(_courseCd).compareTo(Integer.valueOf(an._courseCd));
            }
            
            final String rankStr = getRankStr(this);
            final String anRankStr = getRankStr(an);

            final Integer thisRank = null == rankStr ? new Integer("9999") :Integer.valueOf(rankStr); 
            final Integer otherRank = null == anRankStr ? new Integer("9999") :Integer.valueOf(anRankStr); 
            
            final int cmpRank = thisRank.compareTo(otherRank);
            if (0 == cmpRank) {
                if (0 == cmpGrade) {
                    if (0 == cmpclassno) {
                        return Integer.valueOf(_attendno).compareTo(Integer.valueOf(an._attendno));
                    }
                    return cmpclassno;
                }
                return cmpGrade;
            }
            return cmpRank;
        }
    }
    
    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @return              SQL文字列
     * @throws Exception
     */
    private String getStudentSql(final String selected) {
        
        String gradeClass = "";
        String courseCdMajorCdCourseCode = "";
        
        if (_param._printType == PRINT_TYPE_CLASS) {
            gradeClass = selected;
        } else if (_param._printType == PRINT_TYPE_COURSECD) { 
            courseCdMajorCdCourseCode = selected;
        }

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH RANK_T AS ( ");
        stb.append(" select ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.SEMESTER, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     t1.PROFICIENCYDIV, ");
        stb.append("     t1.PROFICIENCYCD AS TESTCD, ");
        stb.append("     t1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
        if ("03".equals(_param._rankDataDiv)) {
            stb.append("     M1.SCORE, ");
            stb.append("     M1.AVG, ");
        } else {
            stb.append("     t1.SCORE, ");
            stb.append("     t1.AVG, ");
        }
        stb.append("     CAST(NULL AS VARCHAR(2)) AS SCORE_DI, ");
        stb.append("     L2.RANK AS CLASS_RANK, ");
        stb.append("     t1.RANK AS GRADE_RANK, ");
        stb.append("     L3.RANK AS COURSE_RANK, ");
        stb.append("     L4.RANK AS COURSEGROUP_RANK, ");
        stb.append("     L2.DEVIATION AS CLASS_DEVIATION, ");
        stb.append("     t1.DEVIATION AS GRADE_DEVIATION, ");
        stb.append("     L3.DEVIATION AS COURSE_DEVIATION, ");
        stb.append("     L4.DEVIATION AS COURSEGROUP_DEVIATION, ");
        stb.append("     t4.GRADE || t3.HR_CLASS AS CLASS1, ");
        stb.append("     t4.HR_NAME AS HR_NAME, ");
        stb.append("     t3.COURSECD || t3.MAJORCD || t3.COURSECODE AS COURSECODE1, ");
        stb.append("     t7.COURSECODENAME, ");
        stb.append("     L5.GROUP_CD, ");
        stb.append("     L6.GROUP_NAME, ");
        stb.append("     t4.GRADE, ");
        stb.append("     t5.NAME, ");
        stb.append("     t6.ABBV1 AS SEXNAME, ");
        stb.append("     t3.ATTENDNO ");
        stb.append(" from PROFICIENCY_RANK_DAT t1 ");
        stb.append("     inner join SCHREG_REGD_DAT t3 on ");
        stb.append("         t1.YEAR = t3.YEAR and ");
        stb.append("         t1.SEMESTER = t3.SEMESTER and ");
        stb.append("         t1.SCHREGNO = t3.SCHREGNO ");
        stb.append("     left join SCHREG_REGD_HDAT t4 on ");
        stb.append("         t3.YEAR = t4.YEAR and ");
        stb.append("         t3.SEMESTER = t4.SEMESTER and ");
        stb.append("         t3.GRADE = t4.GRADE and ");
        stb.append("         t3.HR_CLASS = t4.HR_CLASS ");
        stb.append("     left join SCHREG_BASE_MST t5 on ");
        stb.append("         t1.SCHREGNO = t5.SCHREGNO ");
        stb.append("     left join NAME_MST t6 on ");
        stb.append("         t6.NAMECD1 = 'Z002' and ");
        stb.append("         t6.NAMECD2 = t5.SEX ");
        stb.append("     left join COURSECODE_MST t7 on ");
        stb.append("         t3.COURSECODE = t7.COURSECODE ");
        stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT M1 ON  M1.YEAR = t1.YEAR ");
        stb.append("          AND M1.SEMESTER = t1.SEMESTER ");
        stb.append("          AND M1.PROFICIENCYCD = t1.PROFICIENCYCD ");
        stb.append("          AND M1.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
        stb.append("          AND M1.SCHREGNO = t1.SCHREGNO ");
        stb.append("          AND M1.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
        stb.append("          AND M1.RANK_DATA_DIV = '" + RANK_DIV_GRADE + "' ");
        stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT L2 ON  L2.YEAR = t1.YEAR ");
        stb.append("          AND L2.SEMESTER = t1.SEMESTER ");
        stb.append("          AND L2.PROFICIENCYCD = t1.PROFICIENCYCD ");
        stb.append("          AND L2.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
        stb.append("          AND L2.SCHREGNO = t1.SCHREGNO ");
        stb.append("          AND L2.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
        stb.append("          AND L2.RANK_DATA_DIV = t1.RANK_DATA_DIV ");
        stb.append("          AND L2.RANK_DIV = '" + RANK_DIV_HRCLASS + "' ");
        stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT L3 ON  L3.YEAR = t1.YEAR ");
        stb.append("          AND L3.SEMESTER = t1.SEMESTER ");
        stb.append("          AND L3.PROFICIENCYCD = t1.PROFICIENCYCD ");
        stb.append("          AND L3.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
        stb.append("          AND L3.SCHREGNO = t1.SCHREGNO ");
        stb.append("          AND L3.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
        stb.append("          AND L3.RANK_DATA_DIV = t1.RANK_DATA_DIV ");
        stb.append("          AND L3.RANK_DIV = '" + RANK_DIV_COURSE + "' ");
        stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT L4 ON  L4.YEAR = t1.YEAR ");
        stb.append("          AND L4.SEMESTER = t1.SEMESTER ");
        stb.append("          AND L4.PROFICIENCYCD = t1.PROFICIENCYCD ");
        stb.append("          AND L4.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
        stb.append("          AND L4.SCHREGNO = t1.SCHREGNO ");
        stb.append("          AND L4.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
        stb.append("          AND L4.RANK_DATA_DIV = t1.RANK_DATA_DIV ");
        stb.append("          AND L4.RANK_DIV = '" + RANK_DIV_COURSEGROUP + "' ");
        stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT L5 ON  L5.YEAR = t3.YEAR ");
        stb.append("          AND L5.GRADE = t3.GRADE ");
        stb.append("          AND L5.COURSECD = t3.COURSECD ");
        stb.append("          AND L5.MAJORCD = t3.MAJORCD ");
        stb.append("          AND L5.COURSECODE = t3.COURSECODE ");
        stb.append("    LEFT JOIN COURSE_GROUP_CD_HDAT L6 ON  L6.YEAR = L5.YEAR ");
        stb.append("          AND L6.GRADE = L5.GRADE ");
        stb.append("          AND L6.GROUP_CD = L5.GROUP_CD ");
        stb.append(" where ");
        stb.append("     t1.YEAR = '" + _param._year + "' ");
        stb.append("     and t1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     and t1.PROFICIENCYDIV = '" + _param._proficiencydiv + "' ");
        stb.append("     and t1.PROFICIENCYCD = '" + _param._proficiencycd + "' ");
        stb.append("     and t1.RANK_DATA_DIV = '" + _param._rankDataDiv + "' ");
        stb.append("     and t1.RANK_DIV = '" + RANK_DATA_DIV_SCORE + "' ");
        stb.append("     and t3.GRADE = '" + _param._grade + "' ");
        if (PRINT_TYPE_CLASS == _param._printType) {
            stb.append("     and t4.GRADE || t3.HR_CLASS = '" + gradeClass + "' ");
        }
        if (PRINT_TYPE_COURSECD == _param._printType) {
            stb.append("     and t3.COURSECD || t3.MAJORCD || t3.COURSECODE = '" + courseCdMajorCdCourseCode + "' ");
        }
        stb.append(" ), MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("    RANK_T ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.PROFICIENCYDIV, ");
        stb.append("     T1.PROFICIENCYCD AS TESTCD, ");
        stb.append("     T1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SCORE, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AVG, ");
        stb.append("     T1.SCORE_DI, ");
        stb.append("     CAST(NULL AS SMALLINT) AS CLASS_RANK, ");
        stb.append("     CAST(NULL AS SMALLINT) AS GRADE_RANK, ");
        stb.append("     CAST(NULL AS SMALLINT) AS COURSE_RANK, ");
        stb.append("     CAST(NULL AS SMALLINT) AS COURSEGROUP_RANK, ");
        stb.append("     CAST(NULL AS DECIMAL) AS CLASS_DEVIATION, ");
        stb.append("     CAST(NULL AS DECIMAL) AS GRADE_DEVIATION, ");
        stb.append("     CAST(NULL AS DECIMAL) AS COURSE_DEVIATION, ");
        stb.append("     CAST(NULL AS DECIMAL) AS COURSEGROUP_DEVIATION, ");
        stb.append("     T3.GRADE || T3.HR_CLASS AS CLASS1, ");
        stb.append("     T4.HR_NAME AS HR_NAME, ");
        stb.append("     T3.COURSECD || T3.MAJORCD || T3.COURSECODE AS COURSECODE1, ");
        stb.append("     T7.COURSECODENAME, ");
        stb.append("     L5.GROUP_CD, ");
        stb.append("     L6.GROUP_NAME, ");
        stb.append("     T3.GRADE, ");
        stb.append("     T5.NAME, ");
        stb.append("     T6.ABBV1 AS SEXNAME, ");
        stb.append("     T3.ATTENDNO ");
        stb.append(" FROM ");
        stb.append("    PROFICIENCY_DAT T1 ");
        stb.append("    INNER JOIN SCHREG_REGD_DAT T3 on ");
        stb.append("         T1.YEAR = T3.YEAR and ");
        stb.append("         T1.SEMESTER = T3.SEMESTER and ");
        stb.append("         T1.SCHREGNO = T3.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT T4 on ");
        stb.append("         T3.YEAR = T4.YEAR and ");
        stb.append("         T3.SEMESTER = T4.SEMESTER and ");
        stb.append("         T3.GRADE = T4.GRADE and ");
        stb.append("         T3.HR_CLASS = T4.HR_CLASS ");
        stb.append("    LEFT JOIN SCHREG_BASE_MST T5 on ");
        stb.append("         T1.SCHREGNO = T5.SCHREGNO ");
        stb.append("    LEFT JOIN NAME_MST T6 on ");
        stb.append("         T6.NAMECD1 = 'Z002' and ");
        stb.append("         T6.NAMECD2 = T5.SEX ");
        stb.append("    LEFT JOIN COURSECODE_MST T7 on ");
        stb.append("         T3.COURSECODE = T7.COURSECODE ");
        stb.append("    LEFT JOIN COURSE_GROUP_CD_DAT L5 ON  L5.YEAR = t3.YEAR ");
        stb.append("          AND L5.GRADE = t3.GRADE ");
        stb.append("          AND L5.COURSECD = t3.COURSECD ");
        stb.append("          AND L5.MAJORCD = t3.MAJORCD ");
        stb.append("          AND L5.COURSECODE = t3.COURSECODE ");
        stb.append("    LEFT JOIN COURSE_GROUP_CD_HDAT L6 ON  L6.YEAR = L5.YEAR ");
        stb.append("          AND L6.GRADE = L5.GRADE ");
        stb.append("          AND L6.GROUP_CD = L5.GROUP_CD ");
        stb.append(" WHERE ");
        stb.append("    T1.YEAR = '"+_param._year+"' ");
        stb.append("    AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("    AND T1.PROFICIENCYDIV = '" + _param._proficiencydiv + "' ");
        stb.append("    AND T1.PROFICIENCYCD = '" + _param._proficiencycd + "' ");
        stb.append("    AND T3.GRADE = '" + _param._grade + "' ");
        if (PRINT_TYPE_CLASS == _param._printType) {
            stb.append("     AND T3.GRADE || T3.HR_CLASS = '" + gradeClass + "' ");
        }
        if (PRINT_TYPE_COURSECD == _param._printType) {
            stb.append("     AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = '" + courseCdMajorCdCourseCode + "' ");
        }
        stb.append("    AND NOT EXISTS( ");
        stb.append("        SELECT ");
        stb.append("            * ");
        stb.append("        FROM ");
        stb.append("            RANK_T E1 ");
        stb.append("        WHERE ");
        stb.append("            T1.YEAR = E1.YEAR ");
        stb.append("            AND T1.SEMESTER = E1.SEMESTER ");
        stb.append("            AND T1.PROFICIENCYDIV = E1.PROFICIENCYDIV ");
        stb.append("            AND T1.PROFICIENCYCD = E1.TESTCD ");
        stb.append("            AND T1.SCHREGNO = E1.SCHREGNO ");
        stb.append("            AND T1.PROFICIENCY_SUBCLASS_CD = E1.SUBCLASSCD ");
        stb.append("    ) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" ORDER BY ");
        stb.append("     GRADE ");
        if(PRINT_TYPE_CLASS == _param._printType || PRINT_TYPE_GRADE == _param._printType) {
            stb.append("     , CLASS1 ");
        }
        if(PRINT_TYPE_COURSECD == _param._printType) {
            stb.append("     , COURSECODE1 ");
        }
        return stb.toString();
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _loginDate;
        
        private final String _grade;
        private final String[] _categorySelected;
        
        private final int _printType; // クラス(1)/コース(2)/学年(3) 
        private final int _sortType; // 5教科(1)/3教科(2) 順位
        
        private final boolean _isChuKoIkkan;
        private final String _proficiencydiv;
        private final String _proficiencycd;
        
        private final String _avgDiv;
        private final String _groupDiv;
        
        /** 順位の基準点: 総合点=01 / 平均点=02 / 偏差値=03 / 傾斜総合点=11 */
        private final String _rankDataDiv;
        /** 平均の基準点: 得点=1 / 傾斜総合点=2 */
        private final String _avgDataDiv;
        
        private final boolean _useSubclassGroup;
        
        private final boolean _seirekiFlg;
        
        private String _testname;
        private String _semesterName;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");
            
            _sortType = Integer.valueOf(request.getParameter("SORT")).intValue();
            
            if ("1".equals(request.getParameter("SELECT_DIV"))) {
                _printType = PRINT_TYPE_CLASS;
                _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
                _avgDiv = "02";
            } else if ("2".equals(request.getParameter("SELECT_DIV"))) { 
                _printType = PRINT_TYPE_COURSECD;
                _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
                _avgDiv = "03";
            } else { // _printType == '3'(grade) or default
                _printType = PRINT_TYPE_GRADE;
                _categorySelected = new String[]{_grade};
                _avgDiv = "01";
            }
            _groupDiv = request.getParameter("GROUP_DIV");

            _isChuKoIkkan = getChuKoIkkanFlg(db2);
            _seirekiFlg = getSeirekiFlg(db2);
            _proficiencydiv = request.getParameter("PROFICIENCYDIV");
            _proficiencycd = request.getParameter("PROFICIENCYCD");
            final String juni = request.getParameter("JUNI");
//            if ("4".equals(juni)) {
//                _rankDataDiv = "11";
//                _avgDataDiv = "2";
//            } else {
                final String rankDivTemp = request.getParameter("useKnjd106cJuni" + juni);
                final String rankDataDiv = (rankDivTemp == null) ? juni : rankDivTemp;
                _rankDataDiv = (null != rankDataDiv && rankDataDiv.length() < 2 ? "0" : "") + rankDataDiv;
                _avgDataDiv = "1";
//            }
            
            final String subclassGroup = null == request.getParameter("SUBCLASS_GROUP") ? "" : request.getParameter("SUBCLASS_GROUP");
            _useSubclassGroup = subclassGroup.indexOf("1") != -1;

            _testname = getTestName(db2);
            _semesterName = getSemesterName(db2);
        }
        
        public String toString() {
            return _year + "," + _semester + "," + _loginDate + "," + _grade + "," + _printType + "," + _sortType + "," + _isChuKoIkkan + "," + _proficiencycd+"," + _rankDataDiv;
        }

        /**
         * 画面で選択したクラス/ホームルーム/コースの名称を得る
         * @param student 表示する学生
         * @return クラス/ホームルーム/コースの名称
         */
        public String getSelectName(final StudentGroup student) {
            switch (_param._printType) {
            case PRINT_TYPE_CLASS:
                return student._hrname;
            case PRINT_TYPE_COURSECD:
                return student._coursecodename;
            case PRINT_TYPE_GRADE:
                return Integer.valueOf(_grade).toString() + "年生";
            default:
                return "";
            }
        }

        /**
         * ソート種別の名称を得る
         * @return ソート種別の名称
         */
        public String getSortTypeName() {
            switch( _param._sortType) {
            case SORT_TYPE_3SUBCLASSES:
                return "３教科順";
            case SORT_TYPE_ATTENDNO:
                return "年組番順";
            default:
                return "";
            }
        }
        
        /**
         * 日付表示の和暦(年号)/西暦使用フラグ
         * @param db2
         * @return 
         */
        private boolean getSeirekiFlg(DB2UDB db2) {
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if ("2".equals(rs.getString("NAME1"))) {
                        seirekiFlg = true; //西暦
                    }
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
        }

        /**
         * 中高一貫高か
         * @param db2 DB
         * @return 中高一貫高ならtrue、そうでなければfalse
         */
        private boolean getChuKoIkkanFlg(DB2UDB db2) {
            boolean seirekiFlg = true;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1='Z010' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAMESPARE2") == null) {
                        seirekiFlg = false;
                    }
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps ,rs);
                db2.commit();
            }
            return seirekiFlg;
        }

        /**
         * 年度の名称を得る
         * @return 年度の名称
         */
        public String getNendo() {
            // return _seirekiFlg ? _year + "年度" : KNJ_EditDate.h_format_JP_N(_year + "-01-01") + "度";
            return _year + "年度";
        }

        /**
         * 日付の名称を得る
         * @return 日付の名称
         */
        public String getDate() {
            // return _seirekiFlg ? (_loginDate.substring(0,4) + "年" + KNJ_EditDate.h_format_JP_MD(_loginDate)) : (KNJ_EditDate.h_format_JP( _loginDate));
            return _loginDate.substring(0,4) + "年" + KNJ_EditDate.h_format_JP_MD(_loginDate);
        }
        
        private String getTestName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String testName = "";
            try {
                final String sql = " select VALUE(PROFICIENCYNAME1, '') AS PROFICIENCYNAME1 from PROFICIENCY_MST where PROFICIENCYDIV = '" + _proficiencydiv + "' AND PROFICIENCYCD = '" + _proficiencycd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    testName = rs.getString("PROFICIENCYNAME1");
                }
            } catch (Exception ex) {
                log.debug("setTestName exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testName;
        }
        
        private String getSemesterName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String semesterName = "";
            try {
                final String sql = " select VALUE(SEMESTERNAME, '') AS SEMESTERNAME from SEMESTER_MST where YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    semesterName = rs.getString("SEMESTERNAME");
                }
            } catch (Exception ex) {
                log.debug("setSemesterName exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return semesterName;
        }
    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (null != svf) {
            svf.VrQuit();
        }
    }

}

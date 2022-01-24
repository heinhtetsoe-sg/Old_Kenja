package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * 学力テスト　成績一覧
 *
 * @author maesiro
 *
 */

public class KNJD648 {

    private static final Log log = LogFactory.getLog(KNJD648.class);

    private static final String FORM_NAME  = "KNJD648.frm";
    private static final String FORM_NAME2 = "KNJD648_2.frm";
    private boolean _hasData;
    private Param _param;
    private String[] _subclassCds;
    private HashMap _subclassMap; // 科目コードと科目名のマップ

    private final int INVALID_INDEX = -100; // 無効なインデックス
    private final int MAX_LINE = 50;
    private final int MAX_SUBCLASS = 9;


    /**
     * KNJD.classから呼ばれる処理
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
                //log.debug(" hasData="+_hasData);
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
     * 模試名称を得る
     * @param db2   ＤＢ接続オブジェクト
     * @return 模試名称
     */
    private String getMockName(final DB2UDB db2) {
        final String sql = " select MOCKNAME1 from MOCK_MST where MOCKCD = '"+_param._mockCd+"' ";
        log.debug("模試名称 SQL=" + sql);
        ResultSet rs = null;
        String mockName = null;
        try {
            db2.query(sql);
            rs = db2.getResultSet();
            if (rs.next()) {
                mockName = rs.getString("MOCKNAME1");
            }
        } catch (Exception ex) {
            log.debug("setMockName exception="+ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        log.debug("模試名称="+mockName);
        return mockName;
    }


    /**
     * 科目コードのセットされているとき、科目表示のインデクスを返す。
     * @param subclassCd
     * @return
     */
    private int getSubclassIndex(String subclassCd) {
        for(int i=0; i<_subclassCds.length; i++) {
            if (_subclassCds[i].equals(subclassCd)) {
                return i;
            }
        }

        // 科目が見つからない
        log.debug("科目が見つかりません: 科目コード="+subclassCd);
        return INVALID_INDEX;
//        // for error message
//        StringBuffer stb = new StringBuffer();
//        for(int i=0; i<_subclassCds.length; i++)
//            stb.append(_subclassCds[i]+",");
//        throw new ArrayIndexOutOfBoundsException("error subclassCd="+subclassCd+ ",("+stb.toString()+")");
    }

    /**
     * 科目コードをセットする
     * @param db2   ＤＢ接続オブジェクト
     * @return
     */
    private void setSubclass(final DB2UDB db2, final int grade, final String gradeClass, final String courseCdMajorCdCourseCode) {

        _param._print3subclassTotal = false;
        _param._print5subclassTotal = false;

        final String sql = getSubclassSql(grade, gradeClass, courseCdMajorCdCourseCode);
        log.debug("getSubclass sql="+sql);
        final List subclassList = new ArrayList();
        ResultSet rs = null;
        log.debug("中高一貫? "+_param._isChuKoIkkan);
        _subclassMap = new HashMap();
        try {
            db2.query(sql);
            rs = db2.getResultSet();
            int counter=1;
            while (rs.next()) {
                if (null == rs.getString("SUBCLASS_NAME"))
                    continue;

                final String groupDiv = rs.getString("GROUP_DIV");
                if ("3".equals(groupDiv)) { _param._print3subclassTotal = true; }
                if ("5".equals(groupDiv)) { _param._print5subclassTotal = true; }
                final String mockSubclassCd = rs.getString("MOCK_SUBCLASS_CD");
                final String mockSubclassName = rs.getString("SUBCLASS_NAME");
                if (!subclassList.contains(mockSubclassCd)) {
                    subclassList.add(mockSubclassCd);
                    _subclassMap.put(
                            mockSubclassCd,
                            mockSubclassName);
                    counter+=1;
                    log.debug("テスト科目名="+mockSubclassName);
                }
            }
            _subclassCds = new String[subclassList.size()];
            for(int i=0; i<subclassList.size(); i++) {
                _subclassCds[i] = (String) subclassList.get(i);
                //log.debug(i+","+_subclassCds[i]);
            }

        } catch (Exception ex) {
            log.debug("setSubclass exception="+ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
    }

    /**
     * 科目コードを読み込むSQL
     * @return              SQL文字列
     * @throws Exception
     */
    private String getSubclassSql(int grade, final String gradeClass, final String courseCdMajorCdCourseCode) {
        boolean isHighschool = !_param._isChuKoIkkan || _param._isChuKoIkkan && grade >= 4 ;
        boolean outputBefore = false;
        StringBuffer stb = new StringBuffer();
        if (_param.PRINT_TYPE_CLASS == _param._printType) {
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
            stb.append("     t1.YEAR='"+_param._year+"' and ");
            stb.append("     t1.SEMESTER = '"+_param._semester+"' and ");
            stb.append("     t1.GRADE || t1.HR_CLASS = '" + gradeClass + "' ");
            stb.append("   group by ");
            stb.append("     t1.GRADE, t1.COURSECD || t1.MAJORCD || t1.COURSECODE ");
            stb.append(" ) ");
        }
        if (_param.PRINT_TYPE_COURSECD == _param._printType || _param.PRINT_TYPE_CLASS == _param._printType) {
            if (outputBefore) { stb.append(" , "); }
            else { stb.append(" with "); }
            outputBefore = true;
            stb.append(" TA AS ( ");
            stb.append("   select  ");
            stb.append("       t1.GRADE, ");
            stb.append("       t1.GROUP_DIV, ");
            stb.append("       t1.MOCK_SUBCLASS_CD ");
            stb.append("   from ");
            stb.append("       MOCK_SUBCLASS_GROUP_DAT t1 ");
            stb.append("   where ");
            stb.append("     t1.MOCKCD = '"+_param._mockCd+"' and ");
            stb.append("     t1.YEAR='"+_param._year+"' and ");
            if (_param.PRINT_TYPE_CLASS == _param._printType) {
                stb.append("    exists (select 'X' from T_SCHREG where grade = t1.GRADE and code = t1.COURSECD || t1.MAJORCD || t1.COURSECODE) ");
            } else if (_param.PRINT_TYPE_COURSECD == _param._printType) {
                stb.append("    t1.COURSECD || t1.MAJORCD || t1.COURSECODE = '" + courseCdMajorCdCourseCode + "' ");
            }
            stb.append("   group by ");
            stb.append("     t1.GRADE, t1.GROUP_DIV, t1.MOCK_SUBCLASS_CD ");
            stb.append(" ) ");
        }
        if (isHighschool) {
            if (outputBefore) { stb.append(" , "); }
            else { stb.append(" with "); }
            outputBefore = true;
            stb.append(" T_HIGH AS ( ");
            stb.append("   select ");
            stb.append("     t1.MOCK_SUBCLASS_CD ");
            stb.append("   from ");
            stb.append("     MOCK_RANK_DAT t1");
            stb.append("     inner join SCHREG_REGD_DAT t2 on");
            stb.append("        t1.YEAR = t2.YEAR and");
            stb.append("        t2.SEMESTER = '"+_param._semester+"' and");
            stb.append("        t2.SCHREGNO = t1.SCHREGNO and");
            stb.append("        t2.GRADE = '" +_param._grade+ "'");
            stb.append("   where ");
            stb.append("     t1.YEAR = '" +_param._year+ "' ");
            if (_param.PRINT_TYPE_CLASS == _param._printType) {
                stb.append("    and t2.GRADE || t2.HR_CLASS = '" + gradeClass + "' ");
            } else if (_param.PRINT_TYPE_COURSECD == _param._printType) {
                stb.append("    and t2.COURSECD || t2.MAJORCD || t2.COURSECODE = '" + courseCdMajorCdCourseCode + "' ");
            }
            stb.append("   group by ");
            stb.append("     t1.MOCK_SUBCLASS_CD ");
            stb.append(" ) ");
        }
        stb.append(" select ");
        stb.append("   t1.MOCK_SUBCLASS_CD, ");
        stb.append("   t1.GROUP_DIV, ");
        stb.append("   t2.SUBCLASS_ABBV AS SUBCLASS_NAME ");
        stb.append(" from MOCK_SUBCLASS_GROUP_DAT t1 ");
        if (_param.PRINT_TYPE_COURSECD == _param._printType || _param.PRINT_TYPE_CLASS == _param._printType) {
            stb.append("  inner join TA on t1.MOCK_SUBCLASS_CD = TA.MOCK_SUBCLASS_CD and t1.GROUP_DIV = TA.GROUP_DIV ");
        }
        if (isHighschool) {
            stb.append("  inner join T_HIGH on ");
            stb.append("    t1.MOCK_SUBCLASS_CD = T_HIGH.MOCK_SUBCLASS_CD ");
        }
        stb.append(" left join MOCK_SUBCLASS_MST t2 on ");
        stb.append("   t1.MOCK_SUBCLASS_CD = t2.MOCK_SUBCLASS_CD ");
        stb.append(" where ");
        stb.append("   t1.GRADE = '" + _param._grade + "' ");
        stb.append("   and t1.YEAR='"+_param._year+"' ");
        stb.append(" group by ");
        stb.append("     t1.MOCK_SUBCLASS_CD, t2.SUBCLASS_ABBV, t1.GROUP_DIV ");
        stb.append(" order by ");
        stb.append("     t1.MOCK_SUBCLASS_CD ");

        return stb.toString();
    }

    /**
     * 印刷処理メイン
     * @param db2   ＤＢ接続オブジェクト
     * @param svf   帳票オブジェクト
     * @return      対象データ存在フラグ(有り：TRUE、無し：FALSE)
     * @throws Exception
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        boolean rtnFlg = false;

        if (_param._printType == _param.PRINT_TYPE_CLASS) {
            for (int i = 0; i < _param._gradeClass.length; i++) {
                List studentList = createStudents(db2, _param._gradeClass[i], null);
                rtnFlg = outPutPrint(db2, svf, studentList) || rtnFlg;
            }
        } else if (_param._printType == _param.PRINT_TYPE_COURSECD) {
            for (int i = 0; i < _param._courseCdMajorCdCourseCode.length; i++) {
                List studentList = createStudents(db2, null, _param._courseCdMajorCdCourseCode[i]);
                rtnFlg = outPutPrint(db2, svf, studentList) || rtnFlg;
            }
        } else { // _printType == '3'(grade) or default
            List studentList = createStudents(db2, null, null);
            rtnFlg = outPutPrint(db2, svf, studentList) || rtnFlg;
        }

        // 帳票出力のメソッド
        return rtnFlg;
    }

    /**
     * 帳票出力処理
     * @param db2       ＤＢ接続オブジェクト
     * @param svf       帳票オブジェクト
     * @param studentList   帳票出力対象クラスオブジェクト
     * @return      対象データ存在フラグ(有り：TRUE、無し：FALSE)
     */
    private boolean outPutPrint(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        boolean nonedata = false;

        if (studentList.size() == 0) {
            return nonedata;
        }

        Student[] students = new Student[studentList.size()];
        for(int i=0; i<studentList.size(); i++) {
            students[i] = (Student) studentList.get(i);
        }

        Arrays.sort(students);

        Student student0 = students[0];
        setHead(db2, svf, student0);
        setSubclass(db2, Integer.valueOf(student0._grade).intValue(), student0._classNo, student0._courseCd);

        final Map subclassGroupNameMap = _param.getSubclassGroupNameMap(db2, student0._courseCd);
        if (_param._print3subclassTotal) {
            svf.VrsOut("ITEM3", subclassGroupNameMap.get("3").toString());
        }
        if (_param._print5subclassTotal) {
            svf.VrsOut("ITEM5", subclassGroupNameMap.get("5").toString());
        }

        BigDecimal all3totalScore = new BigDecimal(0);
        BigDecimal all5totalScore = new BigDecimal(0);
        final int studentPage = students.length % MAX_LINE == 0 ? students.length / MAX_LINE : students.length / MAX_LINE + 1;
        final int subclassPage = _subclassCds.length % MAX_SUBCLASS == 0 ? _subclassCds.length / MAX_SUBCLASS : _subclassCds.length / MAX_SUBCLASS + 1;

        int studentCount3 = 0;
        int studentCount5 = 0;
        Student oldStudent = null;
        for (int page = 0; page < studentPage; page++) {
            boolean isLastStudentsPage = page == studentPage - 1;
            Student[] studentsPerPage = new Student[(students.length < (page + 1) * MAX_LINE ? students.length : (page + 1) * MAX_LINE)];
            for (int i = page * MAX_LINE; i < (page + 1) * MAX_LINE && i < students.length; i++) {
                studentsPerPage[i - page * MAX_LINE] = students[i];
                oldStudent = students[i];
            }

            for (int subPage = 0; subPage < subclassPage; subPage++) {
                boolean isLastSubclassPage = subPage == subclassPage - 1;

                // 科目名クリア
                for(int i = 1; i <= 9; i++) {
                    svf.VrsOut("SUBCLASS" + i, "");
                    svf.VrsOut("SUBCLASS" + i + "_2", "");
                }

                // 科目名設定
                for(int i = subPage * MAX_SUBCLASS; i < (subPage + 1)  * MAX_SUBCLASS && i < _subclassCds.length; i++) {
                    String subclassName = (String) _subclassMap.get(_subclassCds[i]);
                    if (subclassName == null) continue;
                    String fieldName = "SUBCLASS"+(i+1 - subPage * MAX_SUBCLASS)+  ((subclassName.length() < 4) ? "" : "_2");
                    svf.VrsOut(fieldName, subclassName);
                }

                int outputLineCount = page * MAX_LINE;
                for(int k = 0; k < studentsPerPage.length; k++) {
                    Student student = studentsPerPage[k];
                    if (student == null) { break; }
                    outputLineCount += 1;
                    svf.VrsOut("NUMBER", String.valueOf(outputLineCount));
                    svf.VrsOut("HR_NAME",  student._hr_name);
                    svf.VrsOut("ATTENDNO", Integer.valueOf(student._attendno).toString());
                    svf.VrsOut("NAME",     student._name);
                    svf.VrsOut("SEX",      student._sex);

                    for(int m = subPage * MAX_SUBCLASS; m < (subPage + 1) * MAX_SUBCLASS && m < _subclassCds.length; m++) {
                        int n = m - subPage * MAX_SUBCLASS;
                        svf.VrsOut("SCORE"+(n+1),  (String) student._score.get(_subclassCds[m]));
                        // 段階値
                        if (_param._outputAssessLevel) {
                            svf.VrsOut("VALUE"+(n+1),  (String) student._assessLevel.get(_subclassCds[m]));
                        }
                    }

                    if (isLastSubclassPage) {
                        String classRank5 = "2".equals(_param._groupDiv) ? student._all5._course._rank : student._all5._class._rank;
                        String classRank3 = "2".equals(_param._groupDiv) ? student._all3._course._rank : student._all3._class._rank;
                        String classDev5 = "2".equals(_param._groupDiv) ? student._all5._course._dev : student._all5._class._dev;
                        String classDev3 = "2".equals(_param._groupDiv) ? student._all3._course._dev : student._all3._class._dev;

                        if (_param._print5subclassTotal) {
                            svf.VrsOut("TOTAL5",      student._all5._sum);
                            String all5avg = (student._all5._avg==null) ? null : new BigDecimal(student._all5._avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            svf.VrsOut("AVERAGE5",    all5avg);
                            if (!_param._outputAssessLevel) {
                                svf.VrsOut("GRADE_RANK5", student._all5._grade._rank);
                                svf.VrsOut("CLASS_RANK5",  classRank5);
                            }
                            svf.VrsOut("GRADE_DEVE5",   student._all5._grade._dev);
                            svf.VrsOut("DEVIATION5",   classDev5);
                        }
                        if (_param._print3subclassTotal) {
                            svf.VrsOut("TOTAL3",      student._all3._sum);
                            String all3avg = (student._all3._avg==null) ? null : new BigDecimal(student._all3._avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            svf.VrsOut("AVERAGE3",    all3avg);
                            if (!_param._outputAssessLevel) {
                                svf.VrsOut("GRADE_RANK3", student._all3._grade._rank);
                                svf.VrsOut("CLASS_RANK3",  classRank3);
                            }
                            svf.VrsOut("GRADE_DEVE3",   student._all3._grade._dev);
                            svf.VrsOut("DEVIATION3",   classDev3);
                        }

                        if (null != student._all3._avg) {
                            all3totalScore = all3totalScore.add(new BigDecimal(student._all3._avg));
                            studentCount3 += 1;
                        }
                        if (null != student._all5._avg) {
                            all5totalScore = all5totalScore.add(new BigDecimal(student._all5._avg));
                            studentCount5 += 1;
                        }
                    }

                    nonedata = true;
                    // log.debug((k+1)+" student="+students[k]);
                    svf.VrEndRecord();
                }

                if(nonedata) {
                    final int rowsForAverage = (!isLastStudentsPage || !isLastSubclassPage) ? 2 : (1 - _subclassCds.length / MAX_SUBCLASS);
                    final int lineCount = (page + 1) * MAX_LINE - outputLineCount + rowsForAverage;
                    for(int i=1; i < lineCount; i++) {
                        svf.VrsOut("NUMBER","\n");
                        svf.VrEndRecord();
                    }
                }
            }

            if (isLastStudentsPage) {
                setGradeAverage(db2, svf, oldStudent, all3totalScore, all5totalScore, studentCount3, studentCount5);
                svf.VrEndRecord();
            }
        }
        log.debug("nonedata="+nonedata);
        return nonedata;
    }

    /**
     * 学年平均をセットする。
     * @param db2
     * @param svf
     * @param all3totalScore 全3教科の平均値の和。学生の'全3教科平均'の算出に用いる。
     * @param all5totalScore 全5教科の平均値の和。学生の'全5教科平均'の算出に用いる。
     * @param studentCount 全教科の平均値のレコードを持つ学生の数。学生の'全*教科平均'の算出に用いる。
     */

    private void setGradeAverage(DB2UDB db2, Vrw32alp svf, Student student, BigDecimal all3totalScore, BigDecimal all5totalScore, int studentCount3, int studentCount5) {
        if (0 != studentCount3) {
            if (_param._print3subclassTotal) {
                svf.VrsOut("AVE_AVERAGE3", all3totalScore.divide(new BigDecimal(studentCount3), 1, BigDecimal.ROUND_HALF_UP).toString());
            }
        }
        if (0 != studentCount5) {
            if (_param._print5subclassTotal) {
                svf.VrsOut("AVE_AVERAGE5", all5totalScore.divide(new BigDecimal(studentCount5), 1, BigDecimal.ROUND_HALF_UP).toString());
            }
        }

        final String sql = getAverageSql(student);
        log.debug("getAverageSql sql="+sql);
        ResultSet rs = null;
        final Map indexAverages = new TreeMap();
        try{
            PreparedStatement ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while(rs.next()) {
                final String mockSubclassCd = rs.getString("MOCK_SUBCLASS_CD");
                if (null == mockSubclassCd) continue;

                final String avgStr = new BigDecimal(rs.getString("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                if(_param.SUBCLASSCD_ALL5.equals(mockSubclassCd) && _param._print5subclassTotal) {
                    svf.VrsOut("AVE_TOTAL5", avgStr);
                } else if(_param.SUBCLASSCD_ALL3.equals(mockSubclassCd) && _param._print3subclassTotal) {
                    svf.VrsOut("AVE_TOTAL3", avgStr);
                } else {
                    int index = getSubclassIndex(mockSubclassCd);
                    if (index == INVALID_INDEX) continue;
                    svf.VrsOut("AVE_SCORE"+(index+1), avgStr);
                    indexAverages.put(new Integer(index+1), avgStr);
                }
            }
            int minus = 0;
            for (Iterator it = indexAverages.keySet().iterator(); it.hasNext();) {
                Integer index = (Integer) it.next();
                int i = index.intValue() - minus * MAX_SUBCLASS;
                while (i > MAX_SUBCLASS) {
                    svf.VrEndRecord();
                    minus += 1;
                    i -= MAX_SUBCLASS;
                }
                svf.VrsOut("AVE_SCORE"+(i), String.valueOf(indexAverages.get(index)));
                log.debug("AVE_SCORE"+(i)+" => " + indexAverages.get(index));
            }
        } catch(Exception ex) {
            log.debug("setGradeAverage exception", ex);
        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    /*
     * 学年平均のSQL
     */
    private String getAverageSql(Student student) {
        final StringBuffer stb = new StringBuffer();
        final String whereGradeClass = _param.PRINT_TYPE_CLASS == _param._printType ?
                student._classNo : student._grade+"000" ;
        final String whereCourseCdMajorCdCourseCode = _param.PRINT_TYPE_COURSECD == _param._printType ?
                student._courseCd : "00000000" ;

        stb.append(" select * ");
        stb.append(" from ");
        stb.append("     MOCK_AVERAGE_DAT ");
        stb.append(" where ");
        stb.append("     YEAR = '"+_param._year+"' and ");
        stb.append("     MOCKCD = '"+_param._mockCd+"' and ");
        stb.append("     AVG_DIV = '"+_param._avgDiv+"' ");
        if (_param.PRINT_TYPE_GRADE !=_param._printType)
            stb.append("     and GRADE = '"+student._grade+"' ");
        stb.append("     and GRADE || HR_CLASS = '"+whereGradeClass+"' ");
        stb.append("     and COURSECD || MAJORCD || COURSECODE = '"+whereCourseCdMajorCdCourseCode+"' ");
        return stb.toString();
    }

    /**
     * ヘッダ部の出力を行う
     * @param svf       帳票オブジェクト
     * @param db2       ＤＢ接続オブジェクト
     */
    private void setHead(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        try {
            final String form = (_param._outputAssessLevel) ? FORM_NAME2 : FORM_NAME;
            svf.VrSetForm(form, 4);

            svf.VrsOut("NENDO", _param.getYear(db2));
            svf.VrsOut("TITLE", "学力テスト成績一覧表");
            svf.VrsOut("DATE", _param.getDate(db2));

            svf.VrsOut("SELECT_NAME", _param.getSelectName(student));
            svf.VrsOut("SORT", _param.getSortTypeName());
            svf.VrsOut("TESTNAME", getMockName(db2));

            final String divTitle = "2".equals(_param._groupDiv) ? "コース" : "クラス";
            if (!_param._outputAssessLevel) {
                svf.VrsOut("RANK_DIV5", divTitle);
                svf.VrsOut("RANK_DIV3", divTitle);
            }
            svf.VrsOut("DEVI_DIV5", divTitle);
            svf.VrsOut("DEVI_DIV3", divTitle);

            if (_param.PRINT_TYPE_CLASS == _param._printType) {
                svf.VrAttribute("CLASS1", "FF=1");
            } else if (_param.PRINT_TYPE_COURSECD == _param._printType) {
                svf.VrAttribute("COURSECODE1", "FF=1");
            } else if (_param.PRINT_TYPE_GRADE == _param._printType) {
                svf.VrAttribute("GRADE", "FF=1");
            }

        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }
    }

    /**
     * @param db2           ＤＢ接続オブジェクト
     * @return              帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudents( final DB2UDB db2, final String gradeClass, final String courseCdmajorCdCourseCode) throws SQLException {

        final List rtnList = new ArrayList();
        final String sql = getStudentSql(gradeClass, courseCdmajorCdCourseCode);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final Student student1 = new Student(
                        rs.getString("YEAR"),
                        rs.getString("SCHREGNO"),
                        rs.getString("GRADE"),
                        rs.getString("CLASS1"),
                        rs.getString("COURSECODE1"),
                        rs.getString("COURSECODENAME"),
                        rs.getString("ATTENDNO"),
                        rs.getString("NAME"),
                        rs.getString("SEX"),
                        rs.getString("HR_NAME")
                );

                Student student = null;
                for(final Iterator it=rtnList.iterator(); it.hasNext(); ) {
                    Student s= (Student) it.next();
                    if (student1.equals(s)) {
                        student = s;
                        break;
                    }
                }
                if (student == null) {
                    rtnList.add(student1);
                    student = student1;
                }
                //log.debug(" get Student="+student);

                String subclassCd = rs.getString("MOCK_SUBCLASS_CD");
                final String setScoreVal = null == rs.getString("SCORE") ? rs.getString("SCORE_DI") : rs.getString("SCORE");
                final String assessLevel = rs.getString("ASSESSLEVEL");
                student.setScore(subclassCd, setScoreVal, assessLevel);
                if (student.isAll(subclassCd)) {
                    student.setAllData(
                            rs.getString("MOCK_SUBCLASS_CD"),
                            rs.getString("AVG"),
                            rs.getString("CLASS_RANK"),
                            rs.getString("GRADE_RANK"),
                            rs.getString("COURSE_RANK"),
                            rs.getString("CLASS_DEVIATION"),
                            rs.getString("GRADE_DEVIATION"),
                            rs.getString("COURSE_DEVIATION"));
                }
            }

        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private class Data {
        String _rank;
        String _dev;
    }

    private class AllSubclass {
        String _sum;
        String _avg;
        Data _class = new Data();
        Data _course = new Data();
        Data _grade = new Data();
    }

    /** 生徒クラス */
    private class Student implements Comparable {
        HashMap _score = new HashMap();// 最大9科目
        HashMap _assessLevel = new HashMap();// 段階値
        final String _year;
        final String _schregno;
        final String _grade;
        final String _classNo;
        final String _courseCd;
        final String _courseCdName;
        final String _attendno;
        final String _name;
        final String _sex;
        final String _hr_name;

        AllSubclass _all9 = new AllSubclass();
        AllSubclass _all5 = new AllSubclass();
        AllSubclass _all3 = new AllSubclass();

        Student(String year, String schregno, String grade, String classNo, String courseCd, String courseCdName, String attendno,String name, String sex, String hr_name
        ) {
            _year = year;
            _schregno = schregno;
            _grade = grade;
            _classNo = classNo;
            _courseCd = courseCd;
            _courseCdName = courseCdName;
            _attendno = attendno;
            _name = name;
            _sex  =sex;
            _hr_name = hr_name;
        }

        public void setScore(String subclassCd, String score, String assessLevel) {
            if (!isAll(subclassCd)) {
                _score.put(subclassCd, score);
                _assessLevel.put(subclassCd, assessLevel);
            } else {
                if (_param.SUBCLASSCD_ALL3.equals(subclassCd)) {
                    _all3._sum = score;
                } else if (_param.SUBCLASSCD_ALL5.equals(subclassCd)) {
                    _all5._sum = score;
                } else if (_param.SUBCLASSCD_ALL9.equals(subclassCd)) {
                    _all9._sum = score;
                }
            }
        }

        public void setAllData(String subclassCd, String avg, String classRank, String gradeRank, String courseRank, String classDev, String gradeDev, String courseDev) {
            AllSubclass all = new AllSubclass();
            if (_param.SUBCLASSCD_ALL3.equals(subclassCd)) {
                all = _all3;
            } else if (_param.SUBCLASSCD_ALL5.equals(subclassCd)) {
                all = _all5;
            } else if (_param.SUBCLASSCD_ALL9.equals(subclassCd)) {
                all = _all9;
            }
            all._avg = avg;
            all._class._rank = classRank;
            all._grade._rank = gradeRank;
            all._course._rank = courseRank;
            all._class._dev = classDev;
            all._grade._dev = gradeDev;
            all._course._dev = courseDev;
        }

        public boolean isAll(String subclassCd) {
            return
            _param.SUBCLASSCD_ALL3.equals(subclassCd) ||
            _param.SUBCLASSCD_ALL5.equals(subclassCd) ||
            _param.SUBCLASSCD_ALL9.equals(subclassCd);
        }

        public boolean equals(Object o) {
            if (!(o instanceof Student)) {
                return false;
            }
            Student other = (Student) o;
            return (other._year.equals(_year) && other._schregno.equals(_schregno));
        }

        public String toString() {
            return "["+_hr_name+"."+_attendno+"] ("+_all3._class._rank+","+_all5._class._rank+") ("+_all3._grade._rank+","+_all5._grade._rank+") ("+_all3._course._rank+","+_all5._course._rank+")";
        }

        public int compareTo(Object object) {
            if (!(object instanceof Student))
                return 1;

            Student other = (Student) object;
            String thisRankStr = null;
            String otherRankStr = null;

            int orderGrade = Integer.valueOf(_grade).compareTo(Integer.valueOf(other._grade));
            if ( orderGrade != 0)
                return orderGrade;

            if (_param.PRINT_TYPE_CLASS == _param._printType && Integer.valueOf(_classNo).compareTo(Integer.valueOf(other._classNo)) != 0)
                return Integer.valueOf(_classNo).compareTo(Integer.valueOf(other._classNo));
            else if (_param.PRINT_TYPE_COURSECD == _param._printType && Integer.valueOf(_courseCd).compareTo(Integer.valueOf(other._courseCd)) != 0)
                return Integer.valueOf(_courseCd).compareTo(Integer.valueOf(other._courseCd));

            if (_param.SORT_TYPE_ATTENDNO == _param._sortType) {
                thisRankStr = otherRankStr = "0";
            } else {
                AllSubclass all = new AllSubclass();
                AllSubclass otherAll = new AllSubclass();
                if (_param.SORT_TYPE_5SUBCLASSES == _param._sortType) {
                    all = _all5;
                    otherAll = other._all5;
                } else if (_param.SORT_TYPE_3SUBCLASSES == _param._sortType) {
                    all = _all3;
                    otherAll = other._all3;
                }

                Data d = new Data(), otherd = new Data();
                if (_param.PRINT_TYPE_CLASS == _param._printType) {
                    d = all._class;
                    otherd = otherAll._class;
                }else if (_param.PRINT_TYPE_GRADE == _param._printType) {
                    d = all._grade;
                    otherd = otherAll._grade;
                }else if (_param.PRINT_TYPE_COURSECD == _param._printType) {
                    d = all._course;
                    otherd = otherAll._course;
                }
                thisRankStr = d._rank;
                otherRankStr = otherd._rank;
            }

            Integer thisRank = null == thisRankStr ? new Integer("9999") :Integer.valueOf(thisRankStr);
            Integer otherRank = null == otherRankStr ? new Integer("9999") :Integer.valueOf(otherRankStr);

            if (thisRank.equals(otherRank)) {
                if (Integer.valueOf(_grade).equals(Integer.valueOf(other._grade))) {
                    if (Integer.valueOf(_classNo).equals(Integer.valueOf(other._classNo))) {
                        return Integer.valueOf(_attendno).compareTo(Integer.valueOf(other._attendno));
                    }
                    return Integer.valueOf(_classNo).compareTo(Integer.valueOf(other._classNo));
                }
                return Integer.valueOf(_grade).compareTo(Integer.valueOf(other._grade));
            }
            return thisRank.compareTo(otherRank);

        }
    }

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @return              SQL文字列
     * @throws Exception
     */
    private String getStudentSql(String gradeClass, String courseCdMajorCdCourseCode) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH RANK_T AS ( ");
        stb.append(" select ");
        stb.append("     t2.SUBCLASS_ABBV, ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     t1.MOCKCD, ");
        stb.append("     t1.MOCK_SUBCLASS_CD, ");
        stb.append("     t8.ASSESSLEVEL, ");
        if ("3".equals(_param._mockDiv)) {
            stb.append("     M1.SCORE, ");
            stb.append("     M1.AVG, ");
        } else {
            stb.append("     t1.SCORE, ");
            stb.append("     t1.AVG, ");
        }
        stb.append("     CAST(NULL AS VARCHAR(2)) AS SCORE_DI, ");
        stb.append("     t1.CLASS_RANK, ");
        stb.append("     t1.GRADE_RANK, ");
        stb.append("     t1.COURSE_RANK, ");
        stb.append("     t1.CLASS_DEVIATION, ");
        stb.append("     t1.GRADE_DEVIATION, ");
        stb.append("     t1.COURSE_DEVIATION, ");
        stb.append("     t4.GRADE || t3.HR_CLASS AS CLASS1, ");
        stb.append("     t4.HR_NAME AS HR_NAME, ");
        stb.append("     t3.COURSECD || t3.MAJORCD || t3.COURSECODE AS COURSECODE1, ");
        stb.append("     t7.COURSECODENAME, ");
        stb.append("     t4.GRADE, ");
        stb.append("     t5.NAME, ");
        stb.append("     t6.ABBV1 AS SEX, ");
        stb.append("     t3.ATTENDNO ");
        stb.append(" from MOCK_RANK_DAT t1 ");
        stb.append("     left join MOCK_SUBCLASS_MST t2 on ");
        stb.append("         t1.MOCK_SUBCLASS_CD = t2.MOCK_SUBCLASS_CD ");
        stb.append("     inner join SCHREG_REGD_DAT t3 on ");
        stb.append("         t1.YEAR = t3.YEAR and ");
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
        //段階値（1:学年 2:クラス 3:コース）
        stb.append("     LEFT JOIN MOCK_ASSESS_LEVEL_MST t8 ON  t8.YEAR = t1.YEAR ");
        stb.append("          AND t8.MOCKCD = t1.MOCKCD ");
        stb.append("          AND t8.MOCK_SUBCLASS_CD = t1.MOCK_SUBCLASS_CD ");
        stb.append("          AND t8.DIV = '"+_param._divAssessLevel+"' ");
        stb.append("          AND t8.GRADE = t3.GRADE ");
        if ("3".equals(_param._divAssessLevel)) {
            stb.append("          AND t8.HR_CLASS = '000' ");
            stb.append("          AND t8.COURSECD = t3.COURSECD ");
            stb.append("          AND t8.MAJORCD = t3.MAJORCD ");
            stb.append("          AND t8.COURSECODE = t3.COURSECODE ");
        } else if ("2".equals(_param._divAssessLevel)) {
            stb.append("          AND t8.HR_CLASS = t3.HR_CLASS ");
            stb.append("          AND t8.COURSECD = '0' ");
            stb.append("          AND t8.MAJORCD = '000' ");
            stb.append("          AND t8.COURSECODE = '0000' ");
        } else {
            stb.append("          AND t8.HR_CLASS = '000' ");
            stb.append("          AND t8.COURSECD = '0' ");
            stb.append("          AND t8.MAJORCD = '000' ");
            stb.append("          AND t8.COURSECODE = '0000' ");
        }
        stb.append("          AND t1.SCORE BETWEEN t8.ASSESSLOW AND t8.ASSESSHIGH ");
        stb.append("     LEFT JOIN MOCK_RANK_DAT M1 ON  M1.YEAR = t1.YEAR ");
        stb.append("          AND M1.MOCKCD = t1.MOCKCD ");
        stb.append("          AND M1.SCHREGNO = t1.SCHREGNO ");
        stb.append("          AND M1.MOCK_SUBCLASS_CD = t1.MOCK_SUBCLASS_CD ");
        stb.append("          AND M1.MOCKDIV = '1' ");
        stb.append(" where ");
        stb.append("     t1.YEAR = '"+_param._year+"' ");
        stb.append("     and t1.MOCKCD = '"+_param._mockCd+"' ");
        stb.append("     and t1.MOCKDIV = '"+_param._mockDiv+"' ");
        stb.append("     and t3.SEMESTER = '"+_param._semester+"' ");
        stb.append("     and t3.GRADE = '"+_param._grade+"' ");
        if (_param.PRINT_TYPE_CLASS == _param._printType)
            stb.append("     and t4.GRADE || t3.HR_CLASS = '" + gradeClass + "' ");
        if (_param.PRINT_TYPE_COURSECD == _param._printType)
            stb.append("     and t3.COURSECD || t3.MAJORCD || t3.COURSECODE = '" + courseCdMajorCdCourseCode + "' ");
        stb.append(" ), MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("    RANK_T ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T2.SUBCLASS_ABBV, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.MOCKCD, ");
        stb.append("     T1.MOCK_SUBCLASS_CD, ");
        stb.append("     CAST(NULL AS SMALLINT) AS ASSESSLEVEL, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SCORE, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AVG, ");
        stb.append("     T1.SCORE_DI, ");
        stb.append("     CAST(NULL AS SMALLINT) AS CLASS_RANK, ");
        stb.append("     CAST(NULL AS SMALLINT) AS GRADE_RANK, ");
        stb.append("     CAST(NULL AS SMALLINT) AS COURSE_RANK, ");
        stb.append("     CAST(NULL AS DECIMAL) AS CLASS_DEVIATION, ");
        stb.append("     CAST(NULL AS DECIMAL) AS GRADE_DEVIATION, ");
        stb.append("     CAST(NULL AS DECIMAL) AS COURSE_DEVIATION, ");
        stb.append("     T3.GRADE || T3.HR_CLASS AS CLASS1, ");
        stb.append("     T4.HR_NAME AS HR_NAME, ");
        stb.append("     T3.COURSECD || T3.MAJORCD || T3.COURSECODE AS COURSECODE1, ");
        stb.append("     T7.COURSECODENAME, ");
        stb.append("     T3.GRADE, ");
        stb.append("     T5.NAME, ");
        stb.append("     T6.ABBV1 AS SEX, ");
        stb.append("     T3.ATTENDNO ");
        stb.append(" FROM ");
        stb.append("    MOCK_DAT T1 ");
        stb.append("    LEFT JOIN MOCK_SUBCLASS_MST T2 on ");
        stb.append("         T1.MOCK_SUBCLASS_CD = T2.MOCK_SUBCLASS_CD ");
        stb.append("    INNER JOIN SCHREG_REGD_DAT T3 on ");
        stb.append("         T1.YEAR = T3.YEAR and ");
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
        stb.append(" WHERE ");
        stb.append("    T1.YEAR = '"+_param._year+"' ");
        stb.append("    AND T1.MOCKCD = '"+_param._mockCd+"' ");
        stb.append("    AND T3.SEMESTER = '"+_param._semester+"' ");
        stb.append("    AND T3.GRADE = '"+_param._grade+"' ");
        if (_param.PRINT_TYPE_CLASS == _param._printType) {
            stb.append("     AND T3.GRADE || T3.HR_CLASS = '" + gradeClass + "' ");
        }
        if (_param.PRINT_TYPE_COURSECD == _param._printType) {
            stb.append("     AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = '" + courseCdMajorCdCourseCode + "' ");
        }
        stb.append("    AND NOT EXISTS( ");
        stb.append("        SELECT ");
        stb.append("            * ");
        stb.append("        FROM ");
        stb.append("            RANK_T E1 ");
        stb.append("        WHERE ");
        stb.append("            T1.YEAR = E1.YEAR ");
        stb.append("            AND T1.MOCKCD = E1.MOCKCD ");
        stb.append("            AND T1.SCHREGNO = E1.SCHREGNO ");
        stb.append("            AND T1.MOCK_SUBCLASS_CD = E1.MOCK_SUBCLASS_CD ");
        stb.append("    ) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" ORDER BY ");
        stb.append("     GRADE ");
        if(_param.PRINT_TYPE_CLASS == _param._printType || _param.PRINT_TYPE_GRADE == _param._printType)
            stb.append("     , CLASS1 ");
        if(_param.PRINT_TYPE_COURSECD == _param._printType)
            stb.append("     , COURSECODE1 ");

        log.debug("帳票出力対象データ抽出 の SQL=" + stb.toString());
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _programid;
        private final String _loginDate;

        private final String _grade;
        private final String[] _gradeClass;
        private final String[] _courseCdMajorCdCourseCode;

        private final int _printType; // クラス(1)/コース(2)/学年(3)
        private final int _sortType; // 5教科(1)/3教科(2) 順位

        private final boolean _isChuKoIkkan;
        private final String _mockCd;

        private final String _avgDiv;
        private final String _groupDiv;

        /** 総合点の席次=1 / 平均点の席次=2 */
        private final String _mockDiv;

        private final boolean _useSubclassGroup;

        private final boolean _seirekiFlg;

        private boolean _print3subclassTotal;
        private boolean _print5subclassTotal;

        /** 出力種別: クラス */
        public final int PRINT_TYPE_CLASS = 1;
        /** 出力種別: コース */
        public final int PRINT_TYPE_COURSECD = 2;
        /** 出力種別: 学年 */
        public final int PRINT_TYPE_GRADE = 3;

        /** ソート種別: 5教科 */
        public final int SORT_TYPE_5SUBCLASSES = 1;
        /** ソート種別: 3教科 */
        public final int SORT_TYPE_3SUBCLASSES = 2;
        /** ソート種別: 年組番号順 */
        public final int SORT_TYPE_ATTENDNO = 3;

        /** 合計の科目コード */
        public final String SUBCLASSCD_ALL3 = "333333";
        public final String SUBCLASSCD_ALL5 = "555555";
        public final String SUBCLASSCD_ALL9 = "999999";

        /** 段階値（1:学年 2:クラス 3:コース） */
        private final boolean _outputAssessLevel;
        private final String _divAssessLevel;



        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _programid = request.getParameter("PRGID");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");

            _sortType = Integer.valueOf(request.getParameter("SORT")).intValue();

            if ("1".equals(request.getParameter("SELECT_DIV"))) {
                _printType = PRINT_TYPE_CLASS;
                _gradeClass = request.getParameterValues("CATEGORY_SELECTED");
                _courseCdMajorCdCourseCode = null;
                _avgDiv = "2";
            } else if ("2".equals(request.getParameter("SELECT_DIV"))) {
                _printType = PRINT_TYPE_COURSECD;
                _gradeClass = null;
                _courseCdMajorCdCourseCode = request.getParameterValues("CATEGORY_SELECTED");
                _avgDiv = "3";
            } else { // _printType == '3'(grade) or default
                _printType = PRINT_TYPE_GRADE;
                _gradeClass = null;
                _courseCdMajorCdCourseCode = null;
                _avgDiv = "1";
            }
            _groupDiv = request.getParameter("GROUP_DIV");

            _isChuKoIkkan = getChuKoIkkanFlg(db2);
            _seirekiFlg = getSeirekiFlg(db2);
            _mockCd = request.getParameter("MOCKCD");
            final String juni = request.getParameter("JUNI");
            final String rankDivTemp = request.getParameter("useKnjd106cJuni" + juni);
            _mockDiv = (rankDivTemp == null) ? juni : rankDivTemp;

            final String subclassGroup = request.getParameter("SUBCLASS_GROUP");
            _useSubclassGroup = subclassGroup.indexOf("1") != -1;
            log.debug("useSubclassGroup? \"" + request.getParameter("SUBCLASS_GROUP") + "\" => " +_useSubclassGroup);
            log.debug(" _param="+toString());

            _outputAssessLevel =  null != request.getParameter("OUTPUT_ASSESS_LEVEL");
            _divAssessLevel =  request.getParameter("DIV_ASSESS_LEVEL");
        };

        public String toString() {
            return _year+","+_semester+","+_loginDate+","+_grade+","+_printType+","+_sortType+","+_isChuKoIkkan+","+_mockCd+","+_mockDiv;
        }

        /**
         * 画面から指定された条件によって改ページするか
         * @param student 表示する学生
         * @param studentOld 前に表示した学生
         * @return 改ページするならtrue、そうでなければfalse
         */
        public boolean isNewPage(Student student, Student studentOld) {
            if (null == studentOld) return false;
            switch(_printType) {
            case PRINT_TYPE_GRADE:
                return !student._grade.equals(studentOld._grade);
            case PRINT_TYPE_CLASS:
                return !student._classNo.equals(studentOld._classNo);
            case PRINT_TYPE_COURSECD:
                return !student._courseCd.equals(studentOld._courseCd);
            default:
                return false;
            }
        }

        /**
         * 画面で選択したクラス/ホームルーム/コースの名称を得る
         * @param student 表示する学生
         * @return クラス/ホームルーム/コースの名称
         */
        public String getSelectName(final Student student) {
            switch( _param._printType) {
            case PRINT_TYPE_CLASS:
                return student._hr_name;
            case PRINT_TYPE_COURSECD:
                return student._courseCdName;
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
            case SORT_TYPE_5SUBCLASSES:
                return "５教科順）";
            case SORT_TYPE_3SUBCLASSES:
                return "３教科順）";
            case SORT_TYPE_ATTENDNO:
                return "年組番順）";
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
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if ( rs.next() ) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
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
            try {
                String sql = "SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1='Z010' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if ( rs.next() ) {
                    if (rs.getString("NAMESPARE2") == null) seirekiFlg = false;
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            }
            return seirekiFlg;
        }

        /**
         * 年度の名称を得る
         * @return 年度の名称
         */
        public String getYear(final DB2UDB db2) {
            if (_seirekiFlg) {
                return _year + "年度";
            } else {
                final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_year));
                return gengou + "年度";
            }
        }

        /**
         * 日付の名称を得る
         * @return 日付の名称
         */
        public String getDate(final DB2UDB db2) {
            return _seirekiFlg ?
                    (_loginDate.substring(0,4) + "年" + KNJ_EditDate.h_format_JP_MD(_loginDate)):
                        (KNJ_EditDate.h_format_JP(db2,  _loginDate));
        }

        public Map getSubclassGroupNameMap(final DB2UDB db2, final String courseCd) {
            final Map rtn = new HashMap();

            if (_printType == PRINT_TYPE_COURSECD && _useSubclassGroup) {
                ResultSet rs = null;
                try {
                    final String sql =
                        "SELECT GROUP_DIV, GROUP_NAME " +
                        " FROM MOCK_SUBCLASS_GROUP_MST " +
                        " WHERE YEAR = '" +_year + "' AND " +
                        "  MOCKCD = '" + _mockCd + "' AND " +
                        "  GRADE = '" +_grade + "' AND " +
                        "  COURSECD || MAJORCD || COURSECODE = '" + courseCd + "'";
                    log.debug("sql = " + sql);
                    db2.query(sql);
                    rs = db2.getResultSet();
                    while(rs.next()) {
                        String groupDiv = rs.getString("GROUP_DIV");
                        String groupName = rs.getString("GROUP_NAME");
                        rtn.put(groupDiv, groupName);
                        log.debug("groupDiv = " + groupDiv + ", groupName = " + groupName);
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

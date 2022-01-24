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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 実力テスト　成績一覧
 * $Id: 256fd495f3f9163b54c29a5779f06fe8ff647d54 $
 *
 * @author maesiro
 *
 */

public class KNJH561 {

    private static final Log log = LogFactory.getLog(KNJH561.class);

//    private static final String FORM_NAME  = "KNJH561.frm";
    private static final String FORM_NAME  = "KNJH561";
    private static final String FORM_NAME2 = "KNJH561_2";
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
    /**  : 5教科 */
    public final int SORT_TYPE_5SUBCLASSES = 1;
    /** : 3教科 */
    public final int SORT_TYPE_3SUBCLASSES = 2;
    /** : 年組番号順 */
    public final int SORT_TYPE_ATTENDNO = 3;

    public final String RANK_DATA_DIV_SCORE = "01";

    public final String RANK_DIV_GRADE = "01";
    public final String RANK_DIV_HRCLASS = "02";
    public final String RANK_DIV_COURSE = "03";
    public final String RANK_DIV_MAJOR = "04";
    public final String RANK_DIV_COURSEGROUP = "05";

    private final int INVALID_INDEX = -100; // 無効なインデックス
    private final int MAX_LINE = 50;
    private final int MAX_SUBCLASS1 = 9;
    private final int MAX_SUBCLASS2 = 13;

    private final int MAX_CSVLINE = 500;
    private final int MAX_CSVSUBCLASS = 999;

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

            _hasData = printMain(db2, svf, request, response);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 1);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
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
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        boolean hasData = false;
        final List studentGroupList = new ArrayList();

        for (int i = 0; i < _param._categorySelected.length; i++) {
            studentGroupList.add(createStudents(db2, _param._categorySelected[i]));
        }

        if ("csv".equals(_param._cmd)) {
            final List outPutCsvList = new ArrayList();
            for (final Iterator it = studentGroupList.iterator(); it.hasNext();) {
                final StudentGroup group = (StudentGroup) it.next();
                if (group._studentList.size() == 0) {
                    continue;
                }
                group.load(db2);
                hasData = outPutCsv(outPutCsvList, group, request, response) || hasData;
            }
        } else {
            for (final Iterator it = studentGroupList.iterator(); it.hasNext();) {
                final StudentGroup group = (StudentGroup) it.next();
                if (group._studentList.size() == 0) {
                    continue;
                }
                group.load(db2);
                hasData = outPutPrint(svf, group) || hasData;
            }
        }

        // 帳票出力のメソッド
        return hasData;
    }

    /**
     * 帳票出力処理
     * @param svf       帳票オブジェクト
     * @param students   帳票出力対象クラスオブジェクト
     * @return      対象データ存在フラグ(有り：TRUE、無し：FALSE)
     */
    private boolean outPutCsv(List outPutCsvList, final StudentGroup group, final HttpServletRequest request, final HttpServletResponse response) {
        boolean hasdata = false;

        final int studentSize = group._studentList.size();

        Collections.sort(group._studentList);

        setCsvHead(outPutCsvList, group);

        final List subclassCds = group._subclassList;

        final int studentPage = studentSize % MAX_CSVLINE == 0 ? studentSize / MAX_CSVLINE : studentSize / MAX_CSVLINE + 1;
        final int subclassSize = subclassCds.size();
        final int subclassPage = subclassSize % MAX_CSVSUBCLASS == 0 ? subclassSize / MAX_CSVSUBCLASS : subclassSize / MAX_CSVSUBCLASS + 1;

        for (int page = 0; page < studentPage; page++) {

            final Student[] studentsPerPage = new Student[(studentSize < (page + 1) * MAX_CSVLINE ? studentSize : (page + 1) * MAX_CSVLINE)];

            for (int i = page * MAX_CSVLINE; i < (page + 1) * MAX_CSVLINE && i < studentSize; i++) {
                studentsPerPage[i - page * MAX_CSVLINE] = (Student) group._studentList.get(i);
            }

            for (int subcPage = 0; subcPage < subclassPage; subcPage++) {
                final boolean isLastSubclassPage = subcPage == subclassPage - 1;

                int outputLineCount = page * MAX_CSVLINE;
                for (int k = 0; k < studentsPerPage.length; k++) {
                    final Student student = studentsPerPage[k];

                    if (student == null) { break; }

                    final List lineList = new ArrayList();
                    outputLineCount += 1;
                    lineList.add(String.valueOf(outputLineCount));

                    lineList.add(student._hrname);
                    lineList.add(Integer.valueOf(student._attendno).toString());
                    lineList.add(student._name);
                    lineList.add(student._sex);

                    // 得点
                    for (Iterator itSubclass = group._subclassList.iterator(); itSubclass.hasNext();) {
                        final String subclassCd = (String) itSubclass.next();
                        lineList.add((String) student._score.get(subclassCd));
                    }

                    if (isLastSubclassPage) {
                        if (group._isPrint5subclassTotal) {
                            lineList.add(student._all5._sum);
                            final RankDev rankDev = "2".equals(_param._formGroupDiv) ? student._all5._hr : "3".equals(_param._formGroupDiv) ? student._all5._course : "4".equals(_param._formGroupDiv) ? student._all5._major : "5".equals(_param._formGroupDiv) ?  student._all5._coursegroup : student._all5._grade;
                            final String allavg = (student._all5._avg == null) ? null : new BigDecimal(student._all5._avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            lineList.add(allavg);

                            lineList.add(student._all5._hr._dev);
                            lineList.add(student._all5._hr._rank);
                            lineList.add(rankDev._dev);
                            lineList.add(rankDev._rank);
                        }
                        if (_param._isTosajoForm && "H".equals(_param._schoolKind)) {
                            if (group._isPrint9subclassTotal) {
                                lineList.add(student._all9._sum);
                                final RankDev rankDev = "2".equals(_param._formGroupDiv) ? student._all9._hr : "3".equals(_param._formGroupDiv) ? student._all9._course : "4".equals(_param._formGroupDiv) ? student._all9._major : "5".equals(_param._formGroupDiv) ?  student._all9._coursegroup : student._all9._grade;
                                final String allavg = (student._all9._avg == null) ? null : new BigDecimal(student._all9._avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                                lineList.add(allavg);

                                lineList.add("");  //空白に変更  //student._all9._hr._dev);
                                lineList.add("");  //空白に変更  //student._all9._hr._rank);
                                lineList.add(rankDev._dev);
                                lineList.add(rankDev._rank);
                            }
                        } else {
                            if (group._isPrint3subclassTotal) {
                                lineList.add(student._all3._sum);
                                final RankDev rankDev = "2".equals(_param._formGroupDiv) ? student._all3._hr : "3".equals(_param._formGroupDiv) ? student._all3._course : "4".equals(_param._formGroupDiv) ? student._all3._major : "5".equals(_param._formGroupDiv) ?  student._all3._coursegroup : student._all3._grade;
                                final String allavg = (student._all3._avg == null) ? null : new BigDecimal(student._all3._avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                                lineList.add(allavg);

                                lineList.add(student._all3._hr._dev);
                                lineList.add(student._all3._hr._rank);
                                lineList.add(rankDev._dev);
                                lineList.add(rankDev._rank);
                            }
                        }
                    }

                    outPutCsvList.add(lineList);
                    hasdata = true;
                }

            }

        }
        if (!outPutCsvList.isEmpty()) {
            final Map csvParam = new HashMap();
            csvParam.put("HttpServletRequest", request);
            final String title = _param._nendo + "　" + _param._semesterName + "　" + _param._testname + "成績一覧";
            CsvUtils.outputLines(log, response, title + ".csv", outPutCsvList, csvParam);
        }
        log.debug("hasdata = " + hasdata);
        return hasdata;
    }

    /**
     * ヘッダ部の出力を行う
     * @param svf       帳票オブジェクト
     */
    private void setCsvHead(List outPutCsvList, final StudentGroup group) {
        final List titleList = new ArrayList();
        final String setGrdTerm = "sundaikoufu".equals(_param._z010) ? "　<第" + _param._grdTerm + "期生>" : "";
        titleList.add("");
        titleList.add("");
        titleList.add("");
        titleList.add(_param._nendo + "　" + _param._semesterName + "　" + _param._testname + "成績一覧" + setGrdTerm);
        outPutCsvList.add(titleList);

        final List subTitleList = new ArrayList();
        subTitleList.add("");
        subTitleList.add("");
        subTitleList.add("");
        subTitleList.add("（出力順：" + _param.getSortTypeName(group) + "）");
        outPutCsvList.add(subTitleList);

        final List hrNameDateList = new ArrayList();
        hrNameDateList.add(_param.getSelectName(group));
        hrNameDateList.add("");
        hrNameDateList.add("");
        hrNameDateList.add(_param._printDate);
        outPutCsvList.add(hrNameDateList);


        final List koumokuList1 = new ArrayList();
        final List koumokuList2 = new ArrayList();
        koumokuList1.add("");
        koumokuList1.add("");
        koumokuList1.add("");
        koumokuList1.add("");
        koumokuList1.add("");
        koumokuList2.add("NO");
        koumokuList2.add("クラス");
        koumokuList2.add("番号");
        koumokuList2.add("氏名");
        koumokuList2.add("性別");

        // 科目名設定
        for (Iterator itSubclass = group._subclassList.iterator(); itSubclass.hasNext();) {
            final String subclassCd = (String) itSubclass.next();
            final String subclassName = group.getSubclassName(subclassCd);
            koumokuList2.add(subclassName);
            koumokuList1.add("");
        }

        koumokuList2.add("合計");
        if (group._isPrint5subclassTotal) {
            koumokuList1.add(group.getGroupDivName("5"));
        } else {
            koumokuList1.add("");
        }
        koumokuList2.add("平均");
        koumokuList1.add("");
        koumokuList2.add("クラス偏差値");
        koumokuList1.add("");
        koumokuList2.add("クラス順位");
        koumokuList1.add("");
        final String divTitle = "2".equals(_param._formGroupDiv) ? "クラス" : "3".equals(_param._formGroupDiv) ? "コース" : "4".equals(_param._formGroupDiv) ? "学科" : "5".equals(_param._formGroupDiv) ? "グループ" : "学年";
        koumokuList2.add(divTitle + "偏差値");
        koumokuList1.add("");
        koumokuList2.add(divTitle + "順位");
        koumokuList1.add("");

        koumokuList2.add("合計");
        if (_param._isTosajoForm && "H".equals(_param._schoolKind)) {
            if (group._isPrint9subclassTotal) {
                koumokuList1.add(group.getGroupDivName("9"));
            } else {
                koumokuList1.add("");
            }
        } else {
            if (group._isPrint3subclassTotal) {
                koumokuList1.add(group.getGroupDivName("3"));
            } else {
                koumokuList1.add("");
            }
        }
        koumokuList2.add("平均");
        koumokuList2.add("クラス偏差値");
        koumokuList2.add("クラス順位");
        koumokuList2.add(divTitle + "偏差値");
        koumokuList2.add(divTitle + "順位");

        outPutCsvList.add(koumokuList1);
        outPutCsvList.add(koumokuList2);
    }

    /**
     * 帳票出力処理
     * @param svf       帳票オブジェクト
     * @param students   帳票出力対象クラスオブジェクト
     * @return      対象データ存在フラグ(有り：TRUE、無し：FALSE)
     */
    private boolean outPutPrint(final Vrw32alp svf, final StudentGroup group) {
        boolean hasdata = false;

        final int studentSize = group._studentList.size();

        Collections.sort(group._studentList);

        final String form = _param._useFormName + ".frm";
        svf.VrSetForm(form, 4);

        setHead(svf, group);

        final List subclassCds = group._subclassList;

        final int studentPage = studentSize % MAX_LINE == 0 ? studentSize / MAX_LINE : studentSize / MAX_LINE + 1;
        final int subclassSize = subclassCds.size();
        final int subclassPage = subclassSize % _param._maxSubClsCnt == 0 ? subclassSize / _param._maxSubClsCnt : subclassSize / _param._maxSubClsCnt + 1;

        for (int page = 0; page < studentPage; page++) {

            final boolean isLastStudentsPage = page == studentPage - 1;

            final Student[] studentsPerPage = new Student[(studentSize < (page + 1) * MAX_LINE ? studentSize : (page + 1) * MAX_LINE)];

            for (int i = page * MAX_LINE; i < (page + 1) * MAX_LINE && i < studentSize; i++) {
                studentsPerPage[i - page * MAX_LINE] = (Student) group._studentList.get(i);
            }

            for (int subcPage = 0; subcPage < subclassPage; subcPage++) {
                final boolean isLastSubclassPage = subcPage == subclassPage - 1;

                printSubclassname(svf, group, subclassCds, subclassSize, subcPage);

                int outputLineCount = page * MAX_LINE;
                for (int k = 0; k < studentsPerPage.length; k++) {
                    final Student student = studentsPerPage[k];

                    if (student == null) { break; }

                    outputLineCount += 1;
                    svf.VrsOut("NUMBER", String.valueOf(outputLineCount));

                    printStudent(svf, subclassCds, subclassSize, subcPage, isLastSubclassPage, student, group);

                    hasdata = true;
                    svf.VrEndRecord();
                }

                if (hasdata) {
                    final int rowsForAverage = (!isLastStudentsPage || !isLastSubclassPage) ? 2 : (1 - subclassSize / _param._maxSubClsCnt);
                    final int lineCount = (page + 1) * MAX_LINE - outputLineCount + rowsForAverage;
                    for(int i = 1; i < lineCount; i++) {
                        svf.VrsOut("NUMBER","\n");
                        svf.VrEndRecord();
                    }
                }
            }

            if (isLastStudentsPage) {
                setGradeAverage(svf, group);
                svf.VrEndRecord();
            }
        }
        log.debug("hasdata = " + hasdata);
        return hasdata;
    }

    private void printStudent(final Vrw32alp svf, final List subclassCds, final int subclassSize, int subPage,
            final boolean isLastSubclassPage,
            final Student student,
            final StudentGroup group) {
        svf.VrsOut("HR_NAME",  student._hrname);
        svf.VrsOut("ATTENDNO", Integer.valueOf(student._attendno).toString());
        int nameByte = KNJ_EditEdit.getMS932ByteLength(student._name);
        String nameFieldName = nameByte > 30 ? "3" : nameByte > 20 ? "2": "";
        svf.VrsOut("NAME" + nameFieldName,     student._name);
        svf.VrsOut("SEX",      student._sex);

        for (int m = subPage * _param._maxSubClsCnt; m < (subPage + 1) * _param._maxSubClsCnt && m < subclassSize; m++) {
            final String cd = (String) subclassCds.get(m);
            final int n = m - subPage * _param._maxSubClsCnt;
            svf.VrsOut("SCORE" + (n + 1), (String) student._score.get(cd));
        }

        if (isLastSubclassPage) {
            if (group._isPrint5subclassTotal) {
                printTotal(svf, "5", student._all5);
            }
            if (_param._isTosajoForm && "H".equals(_param._schoolKind)) {
                if (group._isPrint9subclassTotal) {
                    //全教科
                    printTotal(svf, "3", student._all9);
                }
            } else {
                if (group._isPrint3subclassTotal) {
                    printTotal(svf, "3", student._all3);
                }
            }
        }
    }

    private void printSubclassname(final Vrw32alp svf, final StudentGroup group, final List subclassCds, final int subcSize, final int subcPage) {
        // 科目名クリア
        for (int i = 1; i <= 9; i++) {
            svf.VrsOut("SUBCLASS" + i, "");
            svf.VrsOut("SUBCLASS" + i + "_2", "");
        }

        // 科目名設定
        for(int i = subcPage * _param._maxSubClsCnt; i < (subcPage + 1)  * _param._maxSubClsCnt && i < subcSize; i++) {
            final String subclassName = group.getSubclassName((String) subclassCds.get(i));
            if (subclassName == null) continue;
            final String fieldName = "SUBCLASS"+(i + 1 - subcPage * _param._maxSubClsCnt) +  ((subclassName.length() < 4) ? "" : "_2");
            svf.VrsOut(fieldName, subclassName);
        }
    }

    private void printTotal(final Vrw32alp svf, final String si, final SubclassAll all) {
        svf.VrsOut("TOTAL" + si, all._sum);

        final RankDev rankDev = "2".equals(_param._formGroupDiv) ? all._hr : "3".equals(_param._formGroupDiv) ? all._course : "4".equals(_param._formGroupDiv) ? all._major : "5".equals(_param._formGroupDiv) ?  all._coursegroup : all._grade;

        final String allavg = (all._avg == null) ? null : new BigDecimal(all._avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        svf.VrsOut("AVERAGE" + si,    allavg);
        if ("sundaikoufu".equals(_param._z010)) {
            final String color = getColor(allavg);
            if (!"".equals(color)) {
                svf.VrAttribute("AVERAGE" + si, color);
            }
        }
        //土佐女子高校の右側の出力(全教科)以外は出力する
        if (!_param._isTosajoForm || !"H".equals(_param._schoolKind) || !"3".equals(si)) {
            svf.VrsOut("DEVIATION" + si,   all._hr._dev);
            svf.VrsOut("CLASS_RANK" + si, all._hr._rank);
        }
        svf.VrsOut("GRADE_RANK" + si,  rankDev._rank);
        svf.VrsOut("GRADE_DEVE" + si,   rankDev._dev);
    }

    private String getColor(final String allavg) {
        if (!NumberUtils.isNumber(allavg)) {
            return "";
        }
        final Double avgVal = Double.valueOf(allavg);
        if (avgVal.compareTo(Double.valueOf("90.0")) >= 0) {
            return "PAINT=(8,0,2)";
        } else if (avgVal.compareTo(Double.valueOf("85.0")) >= 0) {
            return "PAINT=(9,0,2)";
        } else if (avgVal.compareTo(Double.valueOf("80.0")) >= 0) {
            return "PAINT=(10,0,2)";
        } else if (avgVal.compareTo(Double.valueOf("75.0")) >= 0) {
            return "PAINT=(11,0,2)";
        } else if (avgVal.compareTo(Double.valueOf("65.0")) >= 0) {
            return "";
        } else if (avgVal.compareTo(Double.valueOf("60.0")) >= 0) {
            return "PAINT=(12,0,2)";
        } else if (avgVal.compareTo(Double.valueOf("60.0")) < 0) {
            return "PAINT=(13,0,2)";
        } else {
            return "";
        }
    }

    private static String avg(final List scoreList) {
        BigDecimal sum = null;
        int count = 0;
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final BigDecimal score = (BigDecimal) it.next();
            if (null == score) {
                continue;
            }
            sum = null == sum ? score : sum.add(score);
            count += 1;
        }
        if (count == 0) {
            return null;
        }
        return sum.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    /**
     * 学年平均をセットする。
     * @param db2
     * @param svf
     * @param all3totalScore 全3教科の平均値の和。学生の'全3教科平均'の算出に用いる。
     * @param all5totalScore 全5教科の平均値の和。学生の'全5教科平均'の算出に用いる。
     * @param studentCount 全教科の平均値のレコードを持つ学生の数。学生の'全*教科平均'の算出に用いる。
     */

    private void setGradeAverage(final Vrw32alp svf, final StudentGroup group) {

        List all3avgList = new ArrayList();
        List all5avgList = new ArrayList();
        List all9avgList = new ArrayList();
        for (final Iterator it = group._studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            if (null != student._all3._avg) {
                all3avgList.add(new BigDecimal(student._all3._avg));
            }
            if (null != student._all5._avg) {
                all5avgList.add(new BigDecimal(student._all5._avg));
            }
            if (null != student._all9._avg) {
                all9avgList.add(new BigDecimal(student._all9._avg));
            }
        }

        if (_param._isTosajoForm && "H".equals(_param._schoolKind)) {
            if (group._isPrint9subclassTotal) {
                svf.VrsOut("AVE_AVERAGE3", avg(all9avgList));
            }
        } else {
            if (group._isPrint3subclassTotal) {
                svf.VrsOut("AVE_AVERAGE3", avg(all3avgList));
            }
        }
        if (group._isPrint5subclassTotal) {
            svf.VrsOut("AVE_AVERAGE5", avg(all5avgList));
        }

        final Map indexAverages = new TreeMap();
        for (final Iterator it = group._averageMap.keySet().iterator(); it.hasNext();) {
            final String subclassCd = (String) it.next();
            final String avgStr = (String) group._averageMap.get(subclassCd);
            final String stddevStr = (String) group._stddevMap.get(subclassCd);

//            log.info(" subclasscd = " + subclassCd + ", avg = " + avgStr + ", stdddev = " + stddevStr);

            boolean chkflg = false;
            if(SUBCLASSCD_ALL5.equals(subclassCd)) {
                chkflg = true;
                if (group._isPrint5subclassTotal) {
                    svf.VrsOut("AVE_TOTAL5", avgStr);
                    svf.VrsOut("AVE_DEVIATION5", stddevStr);
                }
            } else {
                if (_param._isTosajoForm && "H".equals(_param._schoolKind)) {
                    if(SUBCLASSCD_ALL9.equals(subclassCd)) {
                        chkflg = true;
                        if (group._isPrint9subclassTotal) {
                            svf.VrsOut("AVE_TOTAL3", avgStr);
                            svf.VrsOut("AVE_DEVIATION3", stddevStr);
                        }
                    }
                } else {
                    if(SUBCLASSCD_ALL3.equals(subclassCd)) {
                        chkflg = true;
                        if (group._isPrint3subclassTotal) {
                            svf.VrsOut("AVE_TOTAL3", avgStr);
                            svf.VrsOut("AVE_DEVIATION3", stddevStr);
                        }
                    }
                }

            }
            if (!chkflg) {
                final int index = group.getSubclassIndex(subclassCd);
                if (index == INVALID_INDEX) {
                    continue;
                }
                svf.VrsOut("AVE_SCORE" + (index + 1), avgStr);
                indexAverages.put(new Integer(index + 1), avgStr);
            }
        }

        int minus = 0;
        for (final Iterator it = indexAverages.keySet().iterator(); it.hasNext();) {
            final Integer index = (Integer) it.next();
            int i = index.intValue() - minus * _param._maxSubClsCnt;
            while (i > _param._maxSubClsCnt) {
                svf.VrEndRecord();
                minus += 1;
                i -= _param._maxSubClsCnt;
            }
            svf.VrsOut("AVE_SCORE" + (i), String.valueOf(indexAverages.get(index)));
        }
    }

    /**
     * ヘッダ部の出力を行う
     * @param svf       帳票オブジェクト
     */
    private void setHead(final Vrw32alp svf, final StudentGroup group) {
        svf.VrsOut("NENDO", _param._nendo + "　" + _param._semesterName);
        svf.VrsOut("DATE", _param._printDate);
        svf.VrsOut("SELECT_NAME", _param.getSelectName(group));
        svf.VrsOut("SORT", _param.getSortTypeName(group));
        final String setGrdTerm = "sundaikoufu".equals(_param._z010) ? "　<第" + _param._grdTerm + "期生>" : "";
        svf.VrsOut("TESTNAME", "　" + _param._testname + "成績一覧" + setGrdTerm);

        final String divTitle = "2".equals(_param._formGroupDiv) ? "クラス" : "3".equals(_param._formGroupDiv) ? "コース" : "4".equals(_param._formGroupDiv) ? "学科" : "5".equals(_param._formGroupDiv) ? "グループ" : "学年";
        svf.VrsOut("RANK_DIV5", "クラス");
        svf.VrsOut("RANK_DIV3", "クラス");
        svf.VrsOut("DEVI_DIV5", "クラス");
        svf.VrsOut("DEVI_DIV3", "クラス");
        svf.VrsOut("GRADE_RANK_DIV5", divTitle);
        svf.VrsOut("GRADE_RANK_DIV3", divTitle);
        svf.VrsOut("GRADE_DEVI_DIV5", divTitle);
        svf.VrsOut("GRADE_DEVI_DIV3", divTitle);

        if (_param._isTosajoForm && "H".equals(_param._schoolKind)) {
            if (group._isPrint9subclassTotal) {
                svf.VrsOut("ITEM3", group.getGroupDivName("9"));
            }
        } else {
            if (group._isPrint3subclassTotal) {
                svf.VrsOut("ITEM3", group.getGroupDivName("3"));
            }
        }
        if (group._isPrint5subclassTotal) {
            svf.VrsOut("ITEM5", group.getGroupDivName("5"));
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
                            rs.getString("SEX")
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
                student.setScore(subclassCd, setScoreVal);
                if (isAll(subclassCd)) {
                    student.setAllData(
                            subclassCd,
                            rs.getString("AVG"),
                            rs.getString("CLASS_RANK"),
                            rs.getString("GRADE_RANK"),
                            rs.getString("COURSE_RANK"),
                            rs.getString("COURSEGROUP_RANK"),
                            rs.getString("MAJOR_RANK"),
                            rs.getString("CLASS_DEVIATION"),
                            rs.getString("GRADE_DEVIATION"),
                            rs.getString("COURSE_DEVIATION"),
                            rs.getString("COURSEGROUP_DEVIATION"),
                            rs.getString("MAJOR_DEVIATION")
                            );
                }
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

        private boolean _isPrint3subclassTotal;
        private boolean _isPrint5subclassTotal;
        private boolean _isPrint9subclassTotal;

        final List _studentList = new ArrayList();
        List _subclassList = Collections.EMPTY_LIST;
        Map _subclassMap = Collections.EMPTY_MAP;
        Map _averageMap = Collections.EMPTY_MAP;
        Map _stddevMap = Collections.EMPTY_MAP;
        Map _subclassGroupNameMap = Collections.EMPTY_MAP;
        Set _attendSubclasses = Collections.EMPTY_SET;

        StudentGroup(final String code) {
            _code = code;
        }

        public void load(DB2UDB db2) {
            _attendSubclasses = getAttendSubclasses(db2);

            setSubclass(db2);

            _averageMap = getAverage(db2, "AVG");
            _stddevMap = getAverage(db2, "STDDEV");

            _subclassGroupNameMap = getSubclassGroupNameMap(db2);
        }

        private Set getAttendSubclasses(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Set attendSubclass = new TreeSet();
            try {
                final String sql = getSubclassReplaceCmbSql();
                log.debug(" replace_cmb sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    attendSubclass.add(rs.getString("ATTEND_SUBCLASSCD"));
                }
            } catch (final Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return attendSubclass;
        }

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

            _isPrint3subclassTotal = false;
            _isPrint5subclassTotal = false;
            _isPrint9subclassTotal = false;

            final String sql = getSubclassSql();
            log.debug("getSubclass sql = " + sql);
            _subclassList = new ArrayList();
//            log.debug("中高一貫? "+_param._isChuKoIkkan);
            _subclassMap = new HashMap();

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (null == rs.getString("SUBCLASS_NAME")) {
                        continue;
                    }

                    final String iv = rs.getString("GROUP_DIV");
                    if ("3".equals(iv)) { _isPrint3subclassTotal = true; }
                    if ("5".equals(iv)) { _isPrint5subclassTotal = true; }
                    if ("9".equals(iv)) { _isPrint9subclassTotal = true; }
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    if (_attendSubclasses.contains(subclassCd)) {
                        continue;
                    }
                    final String subclassName = rs.getString("SUBCLASS_NAME");
                    if (!_subclassList.contains(subclassCd)) {
                        _subclassList.add(subclassCd);
                        _subclassMap.put(subclassCd, subclassName);
                        log.debug("テスト科目名 = " + subclassName);
                    }
                }

            } catch (Exception ex) {
                log.error("setSubclass exception!", ex);
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

        private Map getAverage(final DB2UDB db2, final String field) {
            final String sql = getAverageSql();
            log.debug("getAverageSql sql=" + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            try{
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    if (null == subclassCd) continue;

                    final String avgStr = new BigDecimal(rs.getString(field)).setScale(1, BigDecimal.ROUND_HALF_UP).toString();

                    map.put(subclassCd, avgStr);
                }
            } catch(Exception ex) {
                log.error("setGradeAverage exception", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }

        /*
         * 学年平均のSQL
         */
        private String getAverageSql() {
            final String whereGradeClass = PRINT_TYPE_CLASS == _param._printType ? _gradehrclass : _grade + "000";
            final String whereCourseCdMajorCdCourseCode = PRINT_TYPE_COURSECD == _param._printType ? _coursecode : "00000000";

            final StringBuffer stb = new StringBuffer();
            stb.append(" select ");
            stb.append("     PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("     AVG, ");
            stb.append("     STDDEV ");
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

        public String getGroupDivName(final String groupDiv) {
            if (_param._isTosajoForm && "H".equals(_param._schoolKind)) {
                //"全教科"出力を最優先。後はグループがあれば出力し、それ以外は空白とする。
                if ("9".equals(groupDiv)) {
                    return "全教科";
                }
                if (_subclassGroupNameMap.size() == 1) {
                    // コースがひとつなら
                    return (String) getMappedMap(_subclassGroupNameMap, _coursecode).get(groupDiv);
                }
                if ("3".equals(groupDiv)) {
                    return "";
                }
                if ("5".equals(groupDiv)) {
                    return "";
                }
            } else {
                if (_subclassGroupNameMap.size() == 1) {
                    // コースがひとつなら
                    return (String) getMappedMap(_subclassGroupNameMap, _coursecode).get(groupDiv);
                }
                if ("3".equals(groupDiv)) {
                    return "3教科";
                }
                if ("5".equals(groupDiv)) {
                    return "5教科";
                }
            }
            return "";
        }

        public Map getSubclassGroupNameMap(final DB2UDB db2) {
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                final String sql = getSubclassGroupMstSql();
                log.debug("sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String course = rs.getString("COURSE");
                    final String groupDiv = rs.getString("GROUP_DIV");
                    final String groupName = rs.getString("GROUP_NAME");

                    getMappedMap(rtn, course).put(groupDiv, groupName);
                }
            } catch (Exception ex) {
                log.error("getSubclassGroupNameMap exception", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getSubclassGroupMstSql() {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     COURSECD || MAJORCD || COURSECODE AS COURSE, GROUP_DIV, GROUP_NAME ");
            sql.append(" FROM ");
            sql.append("     PROFICIENCY_SUBCLASS_GROUP_MST ");
            sql.append(" WHERE YEAR = '" + _param._year + "' AND ");
            sql.append("   SEMESTER = '" + _param._semester + "' AND ");
            sql.append("   PROFICIENCYDIV = '" + _param._proficiencydiv + "' AND ");
            sql.append("   PROFICIENCYCD = '" + _param._proficiencycd + "' AND ");
            sql.append("   GRADE = '" +_grade + "' ");
            return sql.toString();
        }

        /** 合併科目取得SQL */
        private String getSubclassReplaceCmbSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.DIV, T1.COMBINED_SUBCLASSCD, T1.ATTEND_SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.PROFICIENCYDIV = '" + _param._proficiencydiv + "' ");
            stb.append("     AND T1.PROFICIENCYCD = '" + _param._proficiencycd + "' ");
            stb.append("     AND T1.GRADE = '" + _param._grade +"' ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ");
            stb.append("       CASE WHEN T1.DIV = '04' THEN '0' || '" + _groupCode + "' || '0000' ");
            stb.append("       ELSE '" + _coursecode + "' END ");
            stb.append(" ORDER BY ");
            stb.append("     T1.DIV, ");
            stb.append("     T1.COMBINED_SUBCLASSCD, ");
            stb.append("     T1.ATTEND_SUBCLASSCD ");
            return stb.toString();
        }
    }

    private class SubclassAll {
        String _sum;
        String _avg;
        RankDev _hr = new RankDev();
        RankDev _grade  = new RankDev();
        RankDev _course = new RankDev();
        RankDev _major = new RankDev();
        RankDev _coursegroup = new RankDev();
    }

    private class RankDev {
        String _rank;
        String _dev;
    }

    private String getRankStr(final Student student) {
        String rankStr = null;
        if (SORT_TYPE_5SUBCLASSES == _param._sortType || SORT_TYPE_3SUBCLASSES == _param._sortType) {

            SubclassAll _sa;
            if (SORT_TYPE_5SUBCLASSES == _param._sortType) {
                _sa = student._all5;
            } else {
                _sa = student._all3;
            }

            RankDev rd = null;
            if (PRINT_TYPE_CLASS == _param._printType) {
                rd = _sa._hr;
            } else if (PRINT_TYPE_GRADE == _param._printType) {
                rd = _sa._grade;
            } else if (PRINT_TYPE_COURSECD == _param._printType) {
                rd = _sa._course;
            }
            rankStr = rd._rank;

        } else if (SORT_TYPE_ATTENDNO == _param._sortType) {
            rankStr = "0";
        }
        return rankStr;
    }

    /** 生徒クラス */
    private class Student implements Comparable {
        HashMap _score = new HashMap();// 最大9科目
        final String _year;
        final String _schregno;
        final String _grade;
        final String _classNo;
        final String _hrname;
        final String _courseCd;
        final String _attendno;
        final String _name;
        final String _sex;

        final SubclassAll _all3 = new SubclassAll();
        final SubclassAll _all5 = new SubclassAll();
        final SubclassAll _all9 = new SubclassAll();

        Student(final String year, final String schregno, final String grade, final String classNo, final String hrname, final String courseCd, final String attendno,
                final String name, String sex
        ) {
            _year = year;
            _schregno = schregno;
            _grade = grade;
            _classNo = classNo;
            _hrname = hrname;
            _courseCd = courseCd;
            _attendno = attendno;
            _name = name;
            _sex  =sex;
        }

        public void setScore(String subclassCd, String score) {
            if (!isAll(subclassCd)) {
                _score.put(subclassCd, score);
            } else {
                if (SUBCLASSCD_ALL3.equals(subclassCd)) {
                    _all3._sum = score;
                } else if (SUBCLASSCD_ALL5.equals(subclassCd)) {
                    _all5._sum = score;
                } else if (SUBCLASSCD_ALL9.equals(subclassCd)) {
                    _all9._sum = score;
                }
            }
        }

        public void setAllData(final String subclassCd, final String avg,
                final String classRank, final String gradeRank, final String courseRank, final String coursegroupRank, final String majorRank,
                final String classDev, final String gradeDev, final String courseDev, final String coursegroupDev, final String majorDev) {
            SubclassAll all = new SubclassAll();

            if (SUBCLASSCD_ALL3.equals(subclassCd)) {
                all = _all3;
            } else if (SUBCLASSCD_ALL5.equals(subclassCd)) {
                all = _all5;
            } else if (SUBCLASSCD_ALL9.equals(subclassCd)) {
                all = _all9;
            }
            all._avg = avg;
            all._hr._rank = classRank;
            all._grade._rank = gradeRank;
            all._course._rank = courseRank;
            all._coursegroup._rank = coursegroupRank;
            all._major._rank = majorRank;
            all._hr._dev = classDev;
            all._grade._dev = gradeDev;
            all._course._dev = courseDev;
            all._coursegroup._dev = coursegroupDev;
            all._major._dev = majorDev;

        }

        public boolean equals(Object o) {
            if (!(o instanceof Student)) {
                return false;
            }
            Student other = (Student) o;
            return (other._year.equals(_year) && other._schregno.equals(_schregno));
        }

        public String toString() {
            return "[" + _classNo + "." + _attendno + "] (" + _all3._hr._rank + "," + _all5._hr._rank + ") (" + _all3._grade._rank + "," + _all5._grade._rank + ") (" + _all3._course._rank + "," + _all5._course._rank + ")";
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
        stb.append("     L7.RANK AS MAJOR_RANK, ");
        stb.append("     L2.DEVIATION AS CLASS_DEVIATION, ");
        stb.append("     t1.DEVIATION AS GRADE_DEVIATION, ");
        stb.append("     L3.DEVIATION AS COURSE_DEVIATION, ");
        stb.append("     L4.DEVIATION AS COURSEGROUP_DEVIATION, ");
        stb.append("     L7.DEVIATION AS MAJOR_DEVIATION, ");
        stb.append("     t4.GRADE || t3.HR_CLASS AS CLASS1, ");
        stb.append("     t4.HR_NAME AS HR_NAME, ");
        stb.append("     t3.COURSECD || t3.MAJORCD || t3.COURSECODE AS COURSECODE1, ");
        stb.append("     t7.COURSECODENAME, ");
        stb.append("     L5.GROUP_CD, ");
        stb.append("     L6.GROUP_NAME, ");
        stb.append("     t4.GRADE, ");
        stb.append("     t5.NAME, ");
        stb.append("     t6.ABBV1 AS SEX, ");
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
        stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT L7 ON  L7.YEAR = t1.YEAR ");
        stb.append("          AND L7.SEMESTER = t1.SEMESTER ");
        stb.append("          AND L7.PROFICIENCYCD = t1.PROFICIENCYCD ");
        stb.append("          AND L7.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
        stb.append("          AND L7.SCHREGNO = t1.SCHREGNO ");
        stb.append("          AND L7.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
        stb.append("          AND L7.RANK_DATA_DIV = t1.RANK_DATA_DIV ");
        stb.append("          AND L7.RANK_DIV = '" + RANK_DIV_MAJOR + "' ");
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
        stb.append("     CAST(NULL AS SMALLINT) AS MAJOR_RANK, ");
        stb.append("     CAST(NULL AS DECIMAL) AS CLASS_DEVIATION, ");
        stb.append("     CAST(NULL AS DECIMAL) AS GRADE_DEVIATION, ");
        stb.append("     CAST(NULL AS DECIMAL) AS COURSE_DEVIATION, ");
        stb.append("     CAST(NULL AS DECIMAL) AS COURSEGROUP_DEVIATION, ");
        stb.append("     CAST(NULL AS DECIMAL) AS MAJOR_DEVIATION, ");
        stb.append("     T3.GRADE || T3.HR_CLASS AS CLASS1, ");
        stb.append("     T4.HR_NAME AS HR_NAME, ");
        stb.append("     T3.COURSECD || T3.MAJORCD || T3.COURSECODE AS COURSECODE1, ");
        stb.append("     T7.COURSECODENAME, ");
        stb.append("     L5.GROUP_CD, ");
        stb.append("     L6.GROUP_NAME, ");
        stb.append("     T3.GRADE, ");
        stb.append("     T5.NAME, ");
        stb.append("     T6.ABBV1 AS SEX, ");
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
        log.fatal("$Revision: 69525 $ $Date: 2019-09-03 20:09:50 +0900 (火, 03 9 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _loginDate;

        private final String _grade;
        private final String[] _categorySelected;
        private final String _schoolCd;
        private final String _selectSchoolKind;
        private final String _cmd;
        private final String _grdTerm;
        private final String _z010;

        private final int _printType; // クラス(1)/コース(2)/学年(3)
        private final int _sortType; // 5教科(1)/3教科(2) 順位

        private final boolean _isChuKoIkkan;
        private final String _proficiencydiv;
        private final String _proficiencycd;

        private final String _avgDiv;
        private final String _formGroupDiv;

        /** 順位の基準点: 総合点=01 / 平均点=02 / 偏差値=03 / 傾斜総合点=11 */
        private final String _rankDataDiv;
        /** 平均の基準点: 得点=1 / 傾斜総合点=2 */
        private final String _avgDataDiv;

        private final String _nendo;
        private final String _printDate;

        private String _testname;
        private String _semesterName;
        private final String _useFormName;
        private final int _maxSubClsCnt;
        private final String _schoolKind;
        private final boolean _isTosajoForm;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");
            _schoolCd = request.getParameter("SCHOOLCD");
            _selectSchoolKind = request.getParameter("SELECT_SCHOOLKIND");
            _cmd = request.getParameter("cmd");
            _grdTerm = getGrdTerm(db2);
            _z010 = getZ010(db2);

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
            _formGroupDiv = request.getParameter("FORM_GROUP_DIV");

            _isChuKoIkkan = getChuKoIkkanFlg(db2);
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";
            _printDate = KNJ_EditDate.getAutoFormatDate(db2, _loginDate);
            _proficiencydiv = request.getParameter("PROFICIENCYDIV");
            _proficiencycd = request.getParameter("PROFICIENCYCD");
            final String juni = request.getParameter("JUNI");
            if ("4".equals(juni)) {
                _rankDataDiv = "11";
                _avgDataDiv = "2";
            } else {
                final String rankDivTemp = request.getParameter("useKnjd106cJuni" + juni);
                final String rankDataDiv = (rankDivTemp == null) ? juni : rankDivTemp;
                _rankDataDiv = (null != rankDataDiv && rankDataDiv.length() < 2 ? "0" : "") + rankDataDiv;
                _avgDataDiv = "1";
            }

            _testname = getTestName(db2);
            _semesterName = getSemesterName(db2);
            _useFormName = StringUtils.isBlank(request.getParameter("useFormNameH561")) ? FORM_NAME : request.getParameter("useFormNameH561");
            _maxSubClsCnt = FORM_NAME2.equals(_useFormName) ? MAX_SUBCLASS2 : MAX_SUBCLASS1;
            _schoolKind = getSchoolKind(db2);
            _isTosajoForm = FORM_NAME2.equals(_useFormName);
        }

        private String getSchoolKind(DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' ");
            stb.append("     AND GRADE = '" + _grade + "' ");
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString())));
        }

        private String getZ010(DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = 'Z010' ");
            stb.append("     AND NAMECD2 = '00' ");
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString())));
        }

        private String getGrdTerm(final DB2UDB db2) {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                final int setYear = Integer.parseInt(_year) - Integer.parseInt(_grade);
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHOOL_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + setYear + "' ");
                if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                    stb.append("     AND SCHOOLCD = '" + _schoolCd + "' ");
                    stb.append("     AND SCHOOL_KIND = '" + _selectSchoolKind + "' ");
                }

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("PRESENT_EST");
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
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
        public String getSortTypeName(final StudentGroup group) {
            switch( _param._sortType) {
            case SORT_TYPE_5SUBCLASSES:
                return group.getGroupDivName("5") + "順";
            case SORT_TYPE_3SUBCLASSES:
                return group.getGroupDivName("3") + "順";
            case SORT_TYPE_ATTENDNO:
                return "年組番順";
            default:
                return "";
            }
        }

        /**
         * 中高一貫高か
         * @param db2 DB
         * @return 中高一貫高ならtrue、そうでなければfalse
         */
        private boolean getChuKoIkkanFlg(DB2UDB db2) {
            String sql = "SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'Z010' ";
            return null != KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        }

        private String getTestName(final DB2UDB db2) {
            final String sql = " select PROFICIENCYNAME1 from PROFICIENCY_MST where PROFICIENCYDIV = '" + _proficiencydiv + "' AND PROFICIENCYCD = '" + _proficiencycd + "' ";
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));
        }

        private String getSemesterName(final DB2UDB db2) {
            final String sql = " select SEMESTERNAME from SEMESTER_MST where YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));
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

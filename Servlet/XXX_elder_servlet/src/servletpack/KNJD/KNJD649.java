package servletpack.KNJD;

import java.io.IOException;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * 学力テスト 学力定着テスト科目別素点分布表
 *
 * $Id: 3c900290be28ef2ce8e5ccea9393c9b8c359620a $
 *
 */

public class KNJD649 {


    private static final Log log = LogFactory.getLog(KNJD649.class);
    private static final String FORM_NAME1 = "KNJD649_1.frm";
    private static final String FORM_NAME2 = "KNJD649_2.frm";
    private boolean _hasData;
    private Param _param;
    
    private static String _000000 = "000000";
    private static String _333333 = "333333";
    private static String _555555 = "555555";
    private static String _999999 = "999999";
    
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
     * 印刷処理メイン
     * @param db2   ＤＢ接続オブジェクト
     * @param svf   帳票オブジェクト
     * @return      対象データ存在フラグ(有り：TRUE、無し：FALSE)
     * @throws Exception
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        boolean rtnFlg = false;
        
        rtnFlg = outPutPrint1(db2, svf, _000000) || rtnFlg;

        rtnFlg = outPutPrint2(db2, svf, _555555) || rtnFlg;

        rtnFlg = outPutPrint2(db2, svf, _333333) || rtnFlg;

        return rtnFlg;
    }

    /**
     * 帳票出力処理
     * @param db2       ＤＢ接続オブジェクト
     * @param svf       帳票オブジェクト
     * @param scoreDistributionList   帳票出力対象クラスオブジェクト
     * @return      対象データ存在フラグ(有り：TRUE、無し：FALSE)
     */
    private boolean outPutPrint1(final DB2UDB db2, final Vrw32alp svf, final String outputSubclassCd) throws SQLException {
        final int kizami = 5;
        final String sql = getScoreDistributionCommonSql("INT(AVG)", kizami) + getScoreDistributionForSubclassSql();
        log.debug("帳票出力対象データ抽出(各科目) の SQL=" + sql);
        
        final List selectTypeList = createScoreDistribution(db2, outputSubclassCd, sql);
        
        final int max = 100;
        final int size = max / kizami + 1;
        boolean hasdataAll = false;
        
        // 出力用にデータを [select][科目][行][0:全体 1:男 2:女]配列に変換する
        for (final Iterator it0 = selectTypeList.iterator(); it0.hasNext();) { // [クラス/コースコード/学年] リスト

            final SelectType selectType = (SelectType) it0.next();
            final DisplaySubclasses ds = new DisplaySubclasses();
            
            ds.setSubclass(db2, selectType, _param);
            final int subclassesPerPage = 6;

            for (int it = 0; it < (ds._index + subclassesPerPage - 1) / subclassesPerPage; it++) {
                setHead(svf, outputSubclassCd);
                svf.VrsOut("SELECT_NAME", selectType.getSelectName());
                log.debug("選択名称=" + selectType.getSelectName());

                final int minus = it * subclassesPerPage;
                final int pagemax = Math.min(ds._indexSubclass.length, minus + subclassesPerPage);

                // ヘッダー出力 (科目名)                    
                for (int j = minus; j < pagemax; j++) {
                    final int dispIndex = j + 1 - minus;
                    final String subclassname = ds.getSubclassname(ds._indexSubclass[j]);
                    svf.VrsOut("SUBCLASS" + dispIndex + "_" + (getMS932ByteLength(subclassname) < 20 ? "1" : "2"), subclassname);
                }
                for (int i = 0; i < 2; i++) {
                    final String subclassCd = new String[]{_555555, _333333}[i]; 
                    final String subclassname = _param.getSubclassGroupName(db2, subclassCd, selectType._courseCode);
                    final String field = "GROUP_NAME" + (_333333.equals(subclassCd) ? "1_" : "2_");
                    if (getMS932ByteLength(subclassname) <= 6) {
                        svf.VrsOut(field + "1", subclassname);
                    } else {
                        svf.VrsOut(field + "2", subclassname.substring(0, 3));
                        svf.VrsOut(field + "3", subclassname.substring(3));
                    }
                }

                for(int distindex = size - 1; distindex >= 0 ; distindex--) {
                    final Integer score = new Integer(distindex * kizami);
                    svf.VrsOut("SCORE", score.toString());
                    // 人数出力
                    for (int j = minus; j < pagemax; j++) {
                        if (ds._notDisplay[j]) {
                            continue;
                        }
                        final String subclassCd = ds._indexSubclass[j];
                        final int dispIndex = j + 1 - minus;
                        printCount(svf, dispIndex, selectType.getCount(subclassCd, score));
                    }
                    for (int j = 0; j < 2; j++) {
                        final String subclassCd = new String[]{_555555, _333333}[j];
                        final int dispIndex = (_333333.equals(subclassCd)) ? 7 : 8;
                        printCount(svf, dispIndex, selectType.getCount(subclassCd, score));
                    }
                    hasdataAll = true;
                    svf.VrEndRecord();
                }

                // フッター出力 (総合計)
                svf.VrsOut("ITEM", "受験者数");
                for (int j = minus; j < pagemax; j++) {
                    if (ds._notDisplay[j]) {
                        continue;
                    }
                    final String subclassCd = ds._indexSubclass[j];
                    final int dispIndex = j + 1 - minus;
                    printCount(svf, dispIndex, selectType.getTotalCount(subclassCd));
                }
                for (int j = 0; j < 2; j++) {
                    final String subclassCd = new String[]{_555555, _333333}[j];
                    final int dispIndex = (_333333.equals(subclassCd)) ? 7 : 8;
                    printCount(svf, dispIndex, selectType.getTotalCount(subclassCd));
                }
                svf.VrEndRecord();
                svf.VrEndPage();
            }
        }
        log.debug("nonedataAll="+hasdataAll);
        return hasdataAll;
    }

    private void printCount(final Vrw32alp svf, final int dispIndex, final Count count) {
        svf.VrsOut("TOTAL" + dispIndex, String.valueOf(count._all));
        svf.VrsOut("BOY" + dispIndex, String.valueOf(count._man));
        svf.VrsOut("GIRL" + dispIndex, String.valueOf(count._woman));
//        log.debug("total(" + dispIndex + ") = " + count);
    }

    /**
     * 帳票出力処理
     * @param db2       ＤＢ接続オブジェクト
     * @param svf       帳票オブジェクト
     * @param selectTypeList   帳票出力対象クラスオブジェクト
     * @return      対象データ存在フラグ(有り：TRUE、無し：FALSE)
     */
    private boolean outPutPrint2(final DB2UDB db2, final Vrw32alp svf, final String outputSubclassCd) throws SQLException {
        final int kizami = 10;
        final String sql = getScoreDistributionCommonSql("SCORE", kizami) + getScoreDistributionForAllSql(outputSubclassCd);
        log.debug("帳票出力対象データ抽出(総合) の SQL=" + sql);

        final List selectTypeList = createScoreDistribution(db2, outputSubclassCd, sql);

        int max = -1;
        if (_555555.equals(outputSubclassCd)) {
            max = 500;
        }
        if (_333333.equals(outputSubclassCd)) {
            max = 300;
        }
        final int size = max / kizami + 1;
        boolean hasdata = false;
        String courseCode = null;
        // ヘッダー出力 (科目名)
        for (int s = 0; s < selectTypeList.size(); s++) {
            final SelectType selectType = (SelectType) selectTypeList.get(s);
            hasdata = hasdata || !selectType.getSubclassDistribution(outputSubclassCd).isEmpty();
            courseCode = selectType._courseCode;
        }
        if (!hasdata) {
            return false;
        }
        
        setHead(svf, outputSubclassCd);
        svf.VrsOut("SUBTITLE", _param.getSubclassGroupName(db2, outputSubclassCd, courseCode));

        // ヘッダー出力 (科目名)
        for (int s = 0; s < selectTypeList.size(); s++) {
            final SelectType selectType = (SelectType) selectTypeList.get(s);
            svf.VrsOut("SELECT_NAME" + (s + 1) + "_" + (getMS932ByteLength(selectType.getSelectName()) < 10 ? "1" : "2"), selectType.getSelectName());
        }

        // ヘッダー出力 (科目名)
        for (int distindex = size - 1; distindex >= 0 ; distindex--) {
            final Integer score = new Integer(distindex * kizami);
            svf.VrsOut("SCORE", score.toString());
            // 人数出力
            for (int s = 0; s < selectTypeList.size(); s++) {
                final SelectType selectType = (SelectType) selectTypeList.get(s);
                final Count c = selectType.getCount(outputSubclassCd, score);
                svf.VrsOut("STUDENT" + (s + 1), String.valueOf(c._all));
            }
            hasdata = true;
            svf.VrEndRecord();
        }
        // フッター出力 (総合計)
        svf.VrsOut("ITEM", "受験者数");
        for (int s = 0; s < selectTypeList.size(); s++) {
            final SelectType selectType = (SelectType) selectTypeList.get(s);
            final Count t = selectType.getTotalCount(outputSubclassCd);
            svf.VrsOut("STUDENT" + (s + 1), String.valueOf(t._all));
        }
        svf.VrEndRecord();
        log.debug("nonedata="+hasdata);
        return hasdata;
    }
    
    /**
     * ヘッダ部の出力を行う
     * @param svf       帳票オブジェクト
     * @param outputSubclassCd 出力する科目コード(総合でない科目コードは'000000')
     */
    private void setHead(final Vrw32alp svf, final String outputSubclassCd) {
        // 年度
        if (_000000.equals(outputSubclassCd)) {
            svf.VrSetForm(FORM_NAME1, 4);
        } else if (_333333.equals(outputSubclassCd) || _555555.equals(outputSubclassCd)) {
            svf.VrSetForm(FORM_NAME2, 4);
        } 

        svf.VrsOut("NENDO", _param.getYear());
        svf.VrsOut("TITLE", "学力定着テスト科目別素点分布表");
        svf.VrsOut("DATE", _param.getDate());
        svf.VrsOut("TESTNAME", _param._mockName);

        String attrfield = null;
        if (_param.PRINT_TYPE_CLASS == _param._printType) {
            attrfield = "CLASS1";
        } else if (_param.PRINT_TYPE_COURSE == _param._printType) {
            attrfield = "COURSECODE1";
        } else if (_param.PRINT_TYPE_GRADE == _param._printType) {
            attrfield = "GRADE";
        }
        if (null != attrfield) {
            svf.VrAttribute(attrfield, "FF=1");
        }
    }
    
    private int getMS932ByteLength(final String str) {
        int bytes = 0;
        if (null != str) {
            try {
                bytes = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return bytes;
    }

    /**
     * @param db2           ＤＢ接続オブジェクト
     * @return              帳票出力対象データマップ
     * @throws Exception
     */
    private List createScoreDistribution(final DB2UDB db2, final String outputSubclassCd, final String sql) throws SQLException {
        
        final List selectTypeList = new ArrayList(); // [クラス/コースコード/学年 (select)] リスト

        final PreparedStatement ps = db2.prepareStatement(sql);
        final ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                
                final String grade = rs.getString("GRADE");
                final String gradeClass;
                final String hrName;
                final String courseCdMajorCdCourseCode;
                final String courseCodeName;
                if (_param.PRINT_TYPE_CLASS == _param._printType) {
                    gradeClass = rs.getString("CLASS1");
                    hrName = rs.getString("HR_NAME");
                } else {
                    gradeClass = "00000";
                    hrName = null;
                }
                if(_param.PRINT_TYPE_COURSE == _param._printType) {
                    courseCdMajorCdCourseCode = rs.getString("COURSECODE1");
                    courseCodeName = rs.getString("COURSECODENAME");
                } else {
                    courseCdMajorCdCourseCode = "00000000";
                    courseCodeName = null;
                }
                final String mockSubclassCd = rs.getString("MOCK_SUBCLASS_CD"); 
                final Integer distScore = Integer.valueOf(rs.getString("DISTSCORE"));
                final Count distCount = new Count();
                distCount._all = Integer.parseInt(rs.getString("DISTCOUNT_ALL"));
                if (!_333333.equals(outputSubclassCd) && !_555555.equals(outputSubclassCd)) {
                    distCount._man = Integer.parseInt(rs.getString("DISTCOUNT_MAN"));
                    distCount._woman = Integer.parseInt(rs.getString("DISTCOUNT_WOMAN"));
                }
                
                SelectType selectType = getSelectType(selectTypeList, grade, gradeClass, courseCdMajorCdCourseCode);
                if (null == selectType) {
                    selectType = new SelectType(
                            _param,
                            grade,
                            gradeClass,
                            hrName,
                            courseCdMajorCdCourseCode,
                            courseCodeName
                    );
                    selectTypeList.add(selectType);
                }
                
                final Map subclassDistribution = selectType.getSubclassDistribution(mockSubclassCd);
                
                final Count befCount = null != subclassDistribution.get(distScore) ? ((ScoreDistribution) subclassDistribution.get(distScore))._distCount : new Count();
                distCount.add(befCount);

                final ScoreDistribution scoreDistribution = new ScoreDistribution(
                        selectType,
                        mockSubclassCd,
                        distScore,
                        distCount
                );
                subclassDistribution.put(scoreDistribution._distScore, scoreDistribution);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return selectTypeList;
        
        
    }

    private SelectType getSelectType(
            final List selectTypeList,
            final String grade,
            final String gradeClass,
            final String courseCdMajorCdCourseCode
    ) {
        SelectType selectType = null;
        for (final Iterator it = selectTypeList.iterator(); it.hasNext();) {
            final SelectType st = (SelectType) it.next();
            if (grade.equals(st._grade) && gradeClass.equals(st._classNo) && courseCdMajorCdCourseCode.equals(st._courseCode)) {
                selectType = st;
                break;
            }
        }
        return selectType;
    }

    private static class SelectType implements Comparable {
        final Param _param;
        final String _grade;
        final String _classNo;
        final String _homeRoomName;
        final String _courseCode;
        final String _courseCodeName;
        final Map _scoreDistributionMap;

        public SelectType(
                final Param param,
                final String grade,
                final String classNo,
                final String homeRoomName,
                final String courseCode,
                final String courseCodeName
        ) {
            _param = param;
            _grade = grade;
            _classNo = classNo;
            _homeRoomName = homeRoomName;
            _courseCode = courseCode;
            _courseCodeName = courseCodeName;
            _scoreDistributionMap = new HashMap();
        }

        public Map getSubclassDistribution(final String subclassCd) {
            if (null == _scoreDistributionMap.get(subclassCd)) {
                _scoreDistributionMap.put(subclassCd, new HashMap());
            }
            return (Map) _scoreDistributionMap.get(subclassCd);
        }
        
        private static Count getCount(final Map scoreDistributions, final Integer score) {
            if (null == scoreDistributions.get(score)) {
                return new Count();
            }
            return ((ScoreDistribution) scoreDistributions.get(score))._distCount;
        }
        
        public Count getCount(final String subclassCd, final Integer score) {
            return getCount(getSubclassDistribution(subclassCd), score);
        }
        
        public Count getTotalCount(final String subclassCd) {
            final Map scoreDistributions = getSubclassDistribution(subclassCd);
            final Count count1 = new Count();
            for (final Iterator it = scoreDistributions.keySet().iterator(); it.hasNext();) {
                final Integer score = (Integer) it.next();
                count1.add(getCount(scoreDistributions, score));
            }
            return count1;
        }

        public int hashCode() {
            if (_param.PRINT_TYPE_GRADE == _param._printType) {
                return (_param.PRINT_TYPE_GRADE + ":" + _grade).hashCode();
            } else if (_param.PRINT_TYPE_CLASS == _param._printType) {
                return (_param.PRINT_TYPE_CLASS + ":" + _classNo).hashCode();
            } else if (_param.PRINT_TYPE_COURSE == _param._printType) {
                return (_param.PRINT_TYPE_COURSE + ":" + _courseCode).hashCode();
            }
            return super.hashCode();
        }
        
        public int compareTo(Object o) {
            if (!(o instanceof SelectType)) return 0;
            SelectType another = (SelectType) o;
            if (_param.PRINT_TYPE_GRADE == _param._printType) {
                return _grade.compareTo(another._grade);
            } else if (_param.PRINT_TYPE_CLASS == _param._printType) {
                return _classNo.compareTo(another._classNo);
            } else if (_param.PRINT_TYPE_COURSE == _param._printType) {
                return _courseCode.compareTo(another._courseCode);
            }
            return 1;
        }

        public String getSelectName() {
            if (_param.PRINT_TYPE_CLASS == _param._printType) {
                return _homeRoomName;
            } else if (_param.PRINT_TYPE_COURSE == _param._printType) {
                return _courseCodeName;
            } else if (_param.PRINT_TYPE_GRADE == _param._printType) {
                return Integer.valueOf(_grade).toString() + "年生";
            }
            return null;
        }
        
        public String toString() {
            return "(" + _grade + " , " + _classNo + " , " + _homeRoomName + " , " + _courseCode + ", " + _courseCodeName + ")";
        }
    }
    
    private static class Count {
        int _all;
        int _man;
        int _woman;
        public void add(Count c) {
            _all += c._all;
            _man += c._man;
            _woman += c._woman;
        }
        public String toString() {
            return "[all=" + _all + ", man=" + _man + ", woman=" + _woman + "]";
        }
    }

    /** 生徒クラス */
    private class ScoreDistribution {
        final SelectType _selectType;
        final String _mockSubclassCd;
        final Integer _distScore;
        final Count _distCount;

        ScoreDistribution(
                final SelectType selectType,
                final String mockSubclassCd,
                final Integer distScore,
                final Count distCount
        ) {
            _selectType = selectType;
            _mockSubclassCd = mockSubclassCd;
            _distScore = distScore;
            _distCount = distCount;
        }
        
        public boolean isAll(String subclassCd) {
            return _333333.equals(subclassCd) || _555555.equals(subclassCd) || _999999.equals(subclassCd);
        }
        
        public String toString() {
            return "(" + _selectType._grade + "," + _selectType._classNo + "," + _selectType._courseCode + ")," + _mockSubclassCd + ",[score=" + _distScore + ", all=" + _distCount._all + ", man=" + _distCount._man + ", woman=" + _distCount._woman + "]";
        }
    }

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理（共通部分）
     * @return              SQL文字列
     * @throws Exception
     */    
    private String getScoreDistributionCommonSql(final String fieldAll, final int kizami) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" with t_distindex as (SELECT ");
        stb.append("     w1.YEAR, ");
        stb.append("     w1.MOCKCD, ");
        stb.append("     w2.GRADE, ");
        stb.append("     w2.GRADE || w2.HR_CLASS AS CLASS1, ");
        stb.append("     w5.HR_NAME, ");
        stb.append("     w2.COURSECD || w2.MAJORCD || w2.COURSECODE AS COURSECODE1, ");
        stb.append("     w4.COURSECODENAME, ");
        stb.append("     w1.SCHREGNO, ");
        stb.append("     w3.SEX, ");
        stb.append("     w1.MOCK_SUBCLASS_CD, ");
        stb.append("     w1.SCORE, ");
        stb.append("     (case w1.MOCK_SUBCLASS_CD ");
        stb.append("             when '" + _333333 + "' then " + fieldAll + " / " + kizami + " * " + kizami + " ");
        stb.append("             when '" + _555555 + "' then " + fieldAll + " / " + kizami + " * " + kizami + " ");
        stb.append("             when '" + _999999 + "' then 999999 ");
        stb.append("             else SCORE / " + kizami + " * " + kizami + " ");
        stb.append("          end) AS DISTSCORE, ");
        stb.append("     (case w1.MOCK_SUBCLASS_CD ");
        stb.append("             when '" + _333333 + "' then " + fieldAll + " / " + kizami + " ");
        stb.append("             when '" + _555555 + "' then " + fieldAll + " / " + kizami + " ");
        stb.append("             when '" + _999999 + "' then 999999 ");
        stb.append("             else SCORE / " + kizami + " ");
        stb.append("          end) AS DISTINDEX ");
        stb.append(" FROM ");
        stb.append("     MOCK_RANK_DAT w1 ");
        stb.append("         left join SCHREG_REGD_DAT w2 on ");
        stb.append("             w1.YEAR = w2.YEAR and ");
        stb.append("             w1.SCHREGNO = w2.SCHREGNO ");
        stb.append("         left join SCHREG_BASE_MST w3 on ");
        stb.append("             w1.SCHREGNO = w3.SCHREGNO ");
        stb.append("         left join COURSECODE_MST w4 on ");
        stb.append("             w2.COURSECODE = w4.COURSECODE ");
        stb.append("         left join SCHREG_REGD_HDAT w5 on ");
        stb.append("             w2.YEAR = w5.YEAR and ");
        stb.append("             w2.SEMESTER = w5.SEMESTER and ");
        stb.append("             w2.GRADE = w5.GRADE and ");
        stb.append("             w2.HR_CLASS = w5.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     w1.YEAR = '" + _param._year + "' AND ");
        stb.append("     w2.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     w1.MOCKDIV = '1' AND ");
        stb.append("     w1.MOCKCD = '" + _param._mockCd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     w1.YEAR, ");
        stb.append("     w1.MOCKCD, ");
        stb.append("     w1.MOCK_SUBCLASS_CD, ");
        stb.append("     w1.SCORE DESC ");
        stb.append(" )  ");
        return stb.toString();
    }
    
    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理（各科目用）
     * @return              SQL文字列
     * @throws Exception
     */    
    private String getScoreDistributionForSubclassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" , t_distindex_all as(select ");
        stb.append("     t1.GRADE, ");
        if (_param.PRINT_TYPE_CLASS == _param._printType) {
            stb.append("     t1.CLASS1, ");
            stb.append("     t1.HR_NAME, ");
        } else if(_param.PRINT_TYPE_COURSE == _param._printType) {
            stb.append("     t1.COURSECODE1, ");
            stb.append("     t1.COURSECODENAME, ");
        }
        stb.append("     t1.MOCK_SUBCLASS_CD, ");
        stb.append("     t1.DISTSCORE, ");
        stb.append("     t1.DISTINDEX, ");
        stb.append("     count(*) as DISTCOUNT_ALL ");
        stb.append(" from ");
        stb.append("     t_distindex t1 ");
        stb.append(" group by ");
        stb.append("     t1.GRADE, ");
        if (_param.PRINT_TYPE_CLASS == _param._printType) {
            stb.append("     t1.CLASS1, ");
            stb.append("     t1.HR_NAME, ");
        } else if(_param.PRINT_TYPE_COURSE == _param._printType) {
            stb.append("     t1.COURSECODE1, ");
            stb.append("     t1.COURSECODENAME, ");
        }
        stb.append("     t1.MOCK_SUBCLASS_CD, ");
        stb.append("     t1.DISTSCORE, ");
        stb.append("     t1.DISTINDEX ");
        stb.append(" ), t_distindex_man as(select ");
        stb.append("     t1.GRADE, ");
        if (_param.PRINT_TYPE_CLASS == _param._printType) {
            stb.append("     t1.CLASS1, ");
        } else if(_param.PRINT_TYPE_COURSE == _param._printType) {
            stb.append("     t1.COURSECODE1, ");
        }
        stb.append("     t1.MOCK_SUBCLASS_CD, ");
        stb.append("     t1.DISTSCORE, ");
        stb.append("     t1.DISTINDEX, ");
        stb.append("     count(*) as DISTCOUNT_MAN ");
        stb.append(" from ");
        stb.append("     t_distindex t1 ");
        stb.append(" where ");
        stb.append("     t1.sex = '1' ");
        stb.append(" group by ");
        stb.append("     t1.GRADE, ");
        if (_param.PRINT_TYPE_CLASS == _param._printType) {
            stb.append("     t1.CLASS1, ");
        } else if(_param.PRINT_TYPE_COURSE == _param._printType) {
            stb.append("     t1.COURSECODE1, ");
        }
        stb.append("     t1.MOCK_SUBCLASS_CD, ");
        stb.append("     t1.DISTSCORE, ");
        stb.append("     t1.DISTINDEX ");
        stb.append(" ), t_distindex_woman as(select ");
        stb.append("     t1.GRADE, ");
        if (_param.PRINT_TYPE_CLASS == _param._printType) {
            stb.append("     t1.CLASS1, ");
        } else if(_param.PRINT_TYPE_COURSE == _param._printType) {
            stb.append("     t1.COURSECODE1, ");
        }
        stb.append("     t1.MOCK_SUBCLASS_CD, ");
        stb.append("     t1.DISTSCORE, ");
        stb.append("     t1.DISTINDEX, ");
        stb.append("     count(*) as DISTCOUNT_WOMAN ");
        stb.append(" from ");
        stb.append("     t_distindex t1 ");
        stb.append(" where ");
        stb.append("     t1.sex = '2' ");
        stb.append(" group by ");
        stb.append("     t1.GRADE, ");
        if (_param.PRINT_TYPE_CLASS == _param._printType) {
            stb.append("     t1.CLASS1, ");
        } else if(_param.PRINT_TYPE_COURSE == _param._printType) {
            stb.append("     t1.COURSECODE1, ");
        }
        stb.append("     t1.MOCK_SUBCLASS_CD, ");
        stb.append("     t1.DISTSCORE, ");
        stb.append("     t1.DISTINDEX ");
        stb.append(" ) ");
        stb.append(" select ");
        stb.append("     t1.GRADE, ");
        if (_param.PRINT_TYPE_CLASS == _param._printType) {
            stb.append("     t1.CLASS1, ");
            stb.append("     t1.HR_NAME, ");
        } else if(_param.PRINT_TYPE_COURSE == _param._printType) {
            stb.append("     t1.COURSECODE1, ");
            stb.append("     t1.COURSECODENAME, ");
        }
        stb.append("     t1.MOCK_SUBCLASS_CD, ");
        stb.append("     t1.DISTSCORE, ");
        stb.append("     t1.DISTINDEX, ");
        stb.append("     VALUE(t2.DISTCOUNT_MAN, 0) AS DISTCOUNT_MAN, ");
        stb.append("     VALUE(t3.DISTCOUNT_WOMAN, 0) AS DISTCOUNT_WOMAN, ");
        stb.append("     t1.DISTCOUNT_ALL ");
        stb.append(" from ");
        stb.append("     t_distindex_all t1 ");
        stb.append("     left join t_distindex_man t2 on ");
        stb.append("         t1.GRADE = t2.GRADE and ");
        if (_param.PRINT_TYPE_CLASS == _param._printType) {
            stb.append("         t1.CLASS1 = t2.CLASS1 and ");
        }
        if (_param.PRINT_TYPE_COURSE == _param._printType) {
            stb.append("         t1.COURSECODE1 = t2.COURSECODE1 and ");
        }
        stb.append("         t1.MOCK_SUBCLASS_CD = t2.MOCK_SUBCLASS_CD and ");
        stb.append("         t1.DISTINDEX = t2.DISTINDEX ");
        stb.append("     left join t_distindex_woman t3 on ");
        stb.append("         t1.GRADE = t3.GRADE and ");
        if (_param.PRINT_TYPE_CLASS == _param._printType) {
            stb.append("         t1.CLASS1 = t3.CLASS1 and ");
        }
        if (_param.PRINT_TYPE_COURSE == _param._printType) {
            stb.append("         t1.COURSECODE1 = t3.COURSECODE1 and ");
        }
        stb.append("         t1.MOCK_SUBCLASS_CD = t3.MOCK_SUBCLASS_CD and ");
        stb.append("         t1.DISTINDEX = t3.DISTINDEX ");
        stb.append(" where  ");
        stb.append("     t1.GRADE = '"+_param._grade+"' ");
        if (_param.PRINT_TYPE_CLASS == _param._printType) {
            stb.append("     and t1.CLASS1 in " + SQLUtils.whereIn(true, _param._gradeClass) + " ");
        }
        if (_param.PRINT_TYPE_COURSE == _param._printType) {
            stb.append("     and t1.COURSECODE1 in " + SQLUtils.whereIn(true, _param._courseCdMajorCdCourseCode) + " ");
        }
        stb.append(" order by ");
        stb.append("     GRADE, ");
        if (_param.PRINT_TYPE_CLASS == _param._printType) {
            stb.append("     CLASS1, ");
        }
        if (_param.PRINT_TYPE_COURSE == _param._printType) {
            stb.append("     COURSECODE1, ");
        }
        stb.append("     MOCK_SUBCLASS_CD ");
        return stb.toString();
    }

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理（総合用）
     * @return              SQL文字列
     * @throws Exception
     */    
    private String getScoreDistributionForAllSql(final String subClassCdAll) {
        final StringBuffer stb = new StringBuffer();
        if (_param.PRINT_TYPE_GRADE == _param._printType) {
            stb.append(" select ");
            stb.append("     t1.GRADE, ");
            stb.append("     t1.MOCK_SUBCLASS_CD, ");
            stb.append("     t1.DISTSCORE, ");
            stb.append("     t1.DISTINDEX, ");
            stb.append("     count(*) as DISTCOUNT_ALL ");
            stb.append(" from ");
            stb.append("     t_distindex t1 ");
            stb.append(" where ");
            stb.append("     t1.MOCK_SUBCLASS_CD = '" + subClassCdAll + "' ");
            stb.append("     and t1.GRADE = '"+_param._grade+"' ");
            stb.append(" group by ");
            stb.append("     t1.GRADE,      t1.MOCK_SUBCLASS_CD,      t1.DISTSCORE,      t1.DISTINDEX      ");

        } else {
            stb.append(" select ");
            stb.append("     t1.GRADE, ");
            if (_param.PRINT_TYPE_CLASS == _param._printType) {
                stb.append("     t1.CLASS1, ");
                stb.append("     t1.HR_NAME, ");
            }
            stb.append("     t1.COURSECODE1, ");
            stb.append("     t1.COURSECODENAME, ");
            stb.append("     t1.MOCK_SUBCLASS_CD, ");
            stb.append("     t1.DISTSCORE, ");
            stb.append("     t1.DISTINDEX, ");
            stb.append("     count(*) as DISTCOUNT_ALL ");
            stb.append(" from ");
            stb.append("     t_distindex t1 ");
            stb.append(" where ");
            stb.append("     t1.MOCK_SUBCLASS_CD = '"+subClassCdAll+"' ");
            stb.append("     and t1.GRADE = '"+_param._grade+"' ");
            if( _param.PRINT_TYPE_CLASS == _param._printType)
                stb.append("     and t1.CLASS1 in "+SQLUtils.whereIn(true, _param._gradeClass)+" ");
            if( _param.PRINT_TYPE_COURSE == _param._printType)
                stb.append("     and t1.COURSECODE1 in "+SQLUtils.whereIn(true, _param._courseCdMajorCdCourseCode)+" ");
            stb.append(" group by ");
            stb.append("     t1.GRADE, ");
            if (_param.PRINT_TYPE_CLASS == _param._printType) {
                stb.append("     t1.CLASS1, ");
                stb.append("     t1.HR_NAME, ");
            }
            stb.append("     t1.COURSECODE1, ");
            stb.append("     t1.COURSECODENAME, ");
            stb.append("     t1.MOCK_SUBCLASS_CD, ");
            stb.append("     t1.DISTSCORE, ");
            stb.append("     t1.DISTINDEX ");
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
    
    private static class DisplaySubclasses {

        /** 科目のインデックスが無効 */
        public static final int INVALID_SUBCLASS_INDEX = -1;
        
        private String[] _subclassCds;
        private Map _subclassCdNameMap; // 科目コードと科目名のマップ
        
        private int _index = 0;
        private String[] _indexSubclass; // 表示される順番と科目の対応
        private boolean[] _notDisplay;

        /**
         * 科目コードのセットされているとき、科目表示のインデクスを返す。
         * @param subclassCd
         * @return 科目表示のインデクス(表示しない場合は無効なインデクスを返す。)
         */
        private int getSubclassIndex(final String subclassCd) {
            for (int i = 0; i < _subclassCds.length; i++) {
                if (_subclassCds[i].equals(subclassCd)) {
                    //log.debug(subclassCd+" subclassIndex found="+i);
                    return i;
                }
            }
            return INVALID_SUBCLASS_INDEX;
        }
        
        public String getSubclassname(final String subclassCd) {
            return (String) _subclassCdNameMap.get(subclassCd);
        }
        
        /**
         * 科目コードをセットする
         * @param db2   ＤＢ接続オブジェクト
         */
        private void setSubclass(final DB2UDB db2, final SelectType st, final Param param) {
            final int grade = Integer.valueOf(st._grade).intValue();
            final String sql = getSubclassSql(grade, st._classNo, st._courseCode, param);
            final List subclassList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            log.debug("中高一貫? " + param._isChuKoIkkan);
            _subclassCdNameMap = new TreeMap();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String mockSubclassCd = rs.getString("MOCK_SUBCLASS_CD");
                    if (!_555555.equals(mockSubclassCd) && !_333333.equals(mockSubclassCd) && null == rs.getString("SUBCLASS_NAME")) {
                        continue;
                    }
                    
                    if (subclassList.contains(mockSubclassCd)) {
                        continue;
                    }
                    subclassList.add(mockSubclassCd);
                    _subclassCdNameMap.put(mockSubclassCd, rs.getString("SUBCLASS_NAME"));
//                    log.debug("テスト科目名=" + rs.getString("SUBCLASS_NAME") + "(" + rs.getString("MOCK_SUBCLASS_CD") + ")");
                }
                _subclassCds = new String[subclassList.size()];
                for (int i = 0; i < subclassList.size(); i++) {
                    _subclassCds[i] = (String) subclassList.get(i);
                    //log.debug(i+","+_subclassCds[i]);
                }
                
            } catch (Exception ex) {
                log.debug("setSubclass exception", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            int index = 0;
            String[] indexSubclass = new String[_subclassCds.length]; // 表示される順番と科目の対応
            boolean[] notDisplay = new boolean[_subclassCds.length];

            for (int i = 0; i < _subclassCds.length; i++) {
                final String mockSubclassCd = _subclassCds[i];

                if (st.getSubclassDistribution(mockSubclassCd).isEmpty()) {
                    if(param._isChuKoIkkan && Integer.parseInt(param._grade) < 4 ) {
                        notDisplay[index] = true;
                    } else {
                        // 高校はデータがない場合は科目名を表示しない
                        log.debug(" skip1 index = " + index + " = " + mockSubclassCd + " = " + getSubclassname(mockSubclassCd));
                        continue;
                    }
                }
                
                if (getSubclassIndex(mockSubclassCd) == DisplaySubclasses.INVALID_SUBCLASS_INDEX) {
                    log.debug(" skip2 index = " + index + " = " + mockSubclassCd + " = " + getSubclassname(mockSubclassCd));
                    continue;
                }

                // 中学校はデータがなくても科目名を表示する
                indexSubclass[index] = mockSubclassCd;
                log.debug(" set index = " + index + " = " + mockSubclassCd + " = " + getSubclassname(mockSubclassCd));
                index += 1;
            }
            _index = index;
            _indexSubclass = indexSubclass;
            _notDisplay = notDisplay;
        }
        
        /**
         * 科目コードを読み込むSQL
         * @return              SQL文字列
         */
        private String getSubclassSql(int grade, final String gradeClass, final String courseCdMajorCdCourseCode, final Param param) {
            boolean isHighschool = !param._isChuKoIkkan || param._isChuKoIkkan && grade >= 4 ;
            boolean outputBefore = false;
            StringBuffer stb = new StringBuffer();
            if (param.PRINT_TYPE_CLASS == param._printType) {
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
                stb.append("     t1.YEAR='"+param._year+"' and ");
                stb.append("     t1.SEMESTER = '"+param._semester+"' and ");
                stb.append("     t1.GRADE || t1.HR_CLASS = '" + gradeClass + "' ");
                stb.append("   group by ");
                stb.append("     t1.GRADE, t1.COURSECD || t1.MAJORCD || t1.COURSECODE ");
                stb.append(" ) ");
            }
            if (param.PRINT_TYPE_COURSE == param._printType || param.PRINT_TYPE_CLASS == param._printType) {
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
                stb.append("     t1.MOCKCD = '"+param._mockCd+"' and ");
                stb.append("     t1.YEAR='"+param._year+"' and ");
                if (param.PRINT_TYPE_CLASS == param._printType) {
                    stb.append("    exists (select 'X' from T_SCHREG where grade = t1.GRADE and code = t1.COURSECD || t1.MAJORCD || t1.COURSECODE) ");
                } else if (param.PRINT_TYPE_COURSE == param._printType) {
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
                stb.append("        t2.SEMESTER = '"+param._semester+"' and");
                stb.append("        t2.SCHREGNO = t1.SCHREGNO and");
                stb.append("        t2.GRADE = '" +param._grade+ "'");
                stb.append("   where ");
                stb.append("     t1.YEAR = '" +param._year+ "' ");
                if (param.PRINT_TYPE_CLASS == param._printType) {
                    stb.append("    and t2.GRADE || t2.HR_CLASS = '" + gradeClass + "' ");
                } else if (param.PRINT_TYPE_COURSE == param._printType) {
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
            if (param.PRINT_TYPE_COURSE == param._printType || param.PRINT_TYPE_CLASS == param._printType) {
                stb.append("  inner join TA on t1.MOCK_SUBCLASS_CD = TA.MOCK_SUBCLASS_CD and t1.GROUP_DIV = TA.GROUP_DIV ");
            }
            if (isHighschool) {
                stb.append("  inner join T_HIGH on ");
                stb.append("    t1.MOCK_SUBCLASS_CD = T_HIGH.MOCK_SUBCLASS_CD ");
            }
            stb.append(" left join MOCK_SUBCLASS_MST t2 on ");
            stb.append("   t1.MOCK_SUBCLASS_CD = t2.MOCK_SUBCLASS_CD ");
            stb.append(" where ");
            stb.append("   t1.GRADE = '" + param._grade + "' ");
            stb.append(" group by ");
            stb.append("     t1.MOCK_SUBCLASS_CD, t2.SUBCLASS_ABBV, t1.GROUP_DIV ");
            stb.append(" order by ");
            stb.append("     t1.MOCK_SUBCLASS_CD ");

            return stb.toString();
        }
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
        
        /** 印刷種別:クラス */
        private final int PRINT_TYPE_CLASS = 1;
        /** 印刷種別:コース */
        private final int PRINT_TYPE_COURSE = 2;
        /** 印刷種別:学年 */
        private final int PRINT_TYPE_GRADE = 3;
        
        private final boolean _isChuKoIkkan;
        private final boolean _seirekiFlg;
        private final String _mockCd;
        private final String _mockName;
        
        private final String _avgDiv;
        
        private final String _subclassGroup;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _programid = request.getParameter("PRGID");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");
            
            if ("1".equals(request.getParameter("SELECT_DIV"))) {
                log.debug("選択：クラス");
                _printType = PRINT_TYPE_CLASS;
                _gradeClass = request.getParameterValues("CATEGORY_SELECTED");
                _courseCdMajorCdCourseCode = null; 
                _avgDiv = "2";
            } else if ("2".equals(request.getParameter("SELECT_DIV"))) { 
                log.debug("選択：コース");
                _printType = PRINT_TYPE_COURSE;
                _gradeClass = null; 
                _courseCdMajorCdCourseCode = request.getParameterValues("CATEGORY_SELECTED");
                _avgDiv = "3";
            } else { // _printType == '3'(grade) or default
                log.debug("選択：学年");
                _printType = PRINT_TYPE_GRADE;
                _gradeClass = null; 
                _courseCdMajorCdCourseCode = null; 
                _avgDiv = "1";
            }

            _isChuKoIkkan = getChuKoIkkanFlg(db2);
            _seirekiFlg = getSeirekiFlg(db2);
            _mockCd = request.getParameter("MOCKCD");
            _mockName = getMockName(db2, _mockCd);
            
            _subclassGroup = request.getParameter("SUBCLASS_GROUP");
            
            log.debug(" _param="+toString());
        };
        
        public String toString() {
            return "Param(" + _year + "," + _semester + "," + _loginDate + "," + _grade + "," + _printType + "," + _isChuKoIkkan + "," + _mockCd + ")";
        }

        /**
         * 中高一貫フラグを得る
         * @param db2
         * @return 中高一貫フラグ 
         */
        private boolean getChuKoIkkanFlg(final DB2UDB db2) {
            boolean chukoIkkanFlg = true;
            final String sql = "SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'Z010' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAMESPARE2") == null) {
                        chukoIkkanFlg = false;
                    }
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return chukoIkkanFlg ;
        }
        
        /**
         * 日付表示の和暦(年号)/西暦使用フラグを得る
         * @param db2
         * @return 日付表示の和暦(年号)/西暦使用フラグ
         */
        private boolean getSeirekiFlg(final DB2UDB db2) {
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ";
            try {
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
         * 年度名称を得る
         * @return 年度名称
         */
        private String getYear() {
            return _seirekiFlg ? _year+"年度" : KNJ_EditDate.h_format_JP_N(_year+"-01-01")+"度";
        }
        
        /**
         * 日付名称を得る
         * @return 日付名称
         */
        private String getDate() {
            return _seirekiFlg ?
                    (_loginDate.substring(0,4) + "年" + KNJ_EditDate.h_format_JP_MD(_loginDate)) :
                        (KNJ_EditDate.h_format_JP( _loginDate));
        }
        
        /**
         * 模試科目グループマスタの名称を使用するか
         * @return 使用するならtrue、そうでなければfalse
         */
        private boolean useSubclassGroup() {
            return "1".equals(_subclassGroup);
        }
        
        /**
         * 模試科目グループマスタのグループ区分と名称のマップ
         * @param db2 DB
         * @return グループ区分と名称のマップ
         */
        private Map getSubclassGroupNameMap(final DB2UDB db2, final String courseCdMajorCdCourseCode) {
            final Map rtnMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT GROUP_DIV, GROUP_NAME FROM MOCK_SUBCLASS_GROUP_MST "
                    + "WHERE YEAR = '" + _year + "' AND "
                    + "      MOCKCD = '" + _mockCd + "' AND "
                    + "      GRADE = '" + _grade + "' AND "
                    + "      COURSECD || MAJORCD || COURSECODE = '" + courseCdMajorCdCourseCode + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while(rs.next()) {
                    final String groupDiv = rs.getString("GROUP_DIV");
                    final String groupName = rs.getString("GROUP_NAME");
                    rtnMap.put(groupDiv, groupName);
                }
            } catch (SQLException ex) {
                log.error("getSubclassGroupNameMap excetpion! ", ex);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnMap;
        }

        /**
         * 模試科目グループ名を取得する
         * @param db2 DB
         * @param subclassCd 総合の科目コード(グループ区分決定に使用)
         * @param courseCode 課程コード・専攻コード・コースコード
         * @return 模試科目グループ名
         */
        private String getSubclassGroupName(final DB2UDB db2, final String subclassCd, final String courseCode) {
            Map subclassGroupNameMap = null;
            log.debug("useSubG="+useSubclassGroup()+","+(PRINT_TYPE_COURSE == _printType ));
            if (useSubclassGroup() && PRINT_TYPE_COURSE == _printType) {
                subclassGroupNameMap = getSubclassGroupNameMap(db2, courseCode);
            } else {
                subclassGroupNameMap = new TreeMap();
                subclassGroupNameMap.put("3", "３教科");
                subclassGroupNameMap.put("5", "５教科");
            }
            String groupDiv = "";
            if (_333333.equals(subclassCd)) {
                groupDiv = "3";
            } else if (_555555.equals(subclassCd)) {
                groupDiv = "5";
            }
            final String subclassGroupName = (String) subclassGroupNameMap.get(groupDiv);
            log.debug("模試科目グループ名称="+subclassGroupName);
            return subclassGroupName;
        }
        

        /**
         * 模試名称を返す
         * @param db2   ＤＢ接続オブジェクト
         * @param mockCd   模試コード
         * @return 模試名称
         */
        private String getMockName(final DB2UDB db2, final String mockCd) {
            final String sql = " select MOCKNAME1 from MOCK_MST where MOCKCD = '" + mockCd + "' ";
            log.debug("模試名称 SQL=" + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            String mockName = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    mockName = rs.getString("MOCKNAME1");
                }
            } catch (Exception ex) {
                log.debug("setMockName exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug("模試名称="+mockName);
            return mockName;
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

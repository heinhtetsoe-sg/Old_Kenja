package servletpack.KNJD.detail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KnjDbUtils;

// 関西学院
// RECORD_RANK_SDIV_SOUTEN_DAT
public class RecordRankSdivSoutenDat {

    private static final Log log = LogFactory.getLog(RecordRankSdivSoutenDat.class);

    public String _classcd;
    public String _schoolKind;
    public String _curriculumCd;
    public String _subclasscd;
    public String _schregno;
    public Integer _totalCredit;
    public Integer _totalPoint;
    public Integer _gradeRank;
    public Integer _courseRank;
    public Integer _summaryCredit;
    public Integer _summaryPoint;
    public Integer _summaryGradeRank;
    public Integer _summaryCourseRank;
    public Integer _totalPointIgGs;
    public Integer _totalPointHissu;

    public String _grade;
    public String _hrClass;
    public String _course;
    public String _major;
    public Integer _classRank;
    public Integer _majorRank;

    /**
     *  総点合計 / 単位数合計
     * @param sishagonyuKeta 四捨五入する桁
     * @return　総点合計 / 単位数合計
     */
    public String getTotalAvg(final int sishagonyuKeta) {
        if (null == _totalCredit || null == _totalPoint) {
            return null;
        }
        if (_totalCredit == 0) {
            log.warn(" totalCredit 0 : subclasscd = " + _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd + ", schregno = " + _schregno + ", totalPoint = " + _totalPoint);
            return null;
        }
        return new BigDecimal(_totalPoint).divide(new BigDecimal(_totalCredit), sishagonyuKeta, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     *
     * @param db2
     * @param year 年度
     * @param semetestcd SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV
     * @param regdSemester SCHREG_REGD_DATに指定する学期
     * @param grade SCHREG_REGD_DATに指定する学年
     * @return
     */
    private static Map<String, Map<String, RecordRankSdivSoutenDat>> loadSchregSubclassRecordRankSdivSoutenDatMap(final DB2UDB db2, final String year, final String semetestcd, final String regdSemester, final String grade) {
        final Map<String, Map<String, RecordRankSdivSoutenDat>> map = new HashMap<String, Map<String, RecordRankSdivSoutenDat>>();

        final StringBuffer sql = new StringBuffer();
        sql.append(" SELECT  ");
        sql.append("  T1.CLASSCD, ");
        sql.append("  T1.SCHOOL_KIND, ");
        sql.append("  T1.CURRICULUM_CD, ");
        sql.append("  T1.SUBCLASSCD, ");
        sql.append("  T1.SCHREGNO, ");
        sql.append("  T1.TOTAL_CREDIT, ");
        sql.append("  T1.TOTAL_POINT, ");
        sql.append("  T1.GRADE_RANK, ");
        sql.append("  T1.COURSE_RANK, ");
        sql.append("  T1.SUMMARY_CREDIT, ");
        sql.append("  T1.SUMMARY_POINT, ");
        sql.append("  T1.SUMMARY_GRADE_RANK, ");
        sql.append("  T1.SUMMARY_COURSE_RANK, ");
        sql.append("  T1.TOTAL_POINT_IG_GS, ");
        sql.append("  T1.TOTAL_POINT_HISSU, ");
        sql.append("  REGD.HR_CLASS, ");
        sql.append("  REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE, ");
        sql.append("  REGD.COURSECD || REGD.MAJORCD AS MAJOR, ");
        sql.append("  RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, REGD.GRADE, REGD.HR_CLASS               ORDER BY VALUE(TOTAL_POINT, -1) DESC) AS CLASS_RANK, ");
        sql.append("  RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, REGD.GRADE, REGD.COURSECD, REGD.MAJORCD ORDER BY VALUE(TOTAL_POINT, -1) DESC) AS MAJOR_RANK ");
        sql.append(" FROM  ");
        sql.append("  RECORD_RANK_SDIV_SOUTEN_DAT T1 ");
        sql.append("  INNER JOIN SCHREG_REGD_DAT REGD ");
        sql.append("     ON REGD.SCHREGNO = T1.SCHREGNO ");
        sql.append("    AND REGD.YEAR = '" + year + "' ");
        sql.append("    AND REGD.SEMESTER = '" + regdSemester + "' ");
        sql.append("    AND REGD.GRADE = '" + grade + "' ");
        sql.append(" WHERE ");
        sql.append("   T1.YEAR = '" + year + "' ");
        sql.append("   AND T1.SEMESTER = '" + semetestcd.substring(0, 1) + "' ");
        sql.append("   AND T1.TESTKINDCD = '" + semetestcd.substring(1, 3) + "' ");
        sql.append("   AND T1.TESTITEMCD = '" + semetestcd.substring(3, 5) + "' ");
        sql.append("   AND T1.SCORE_DIV = '" + semetestcd.substring(5) + "' ");

        for (final Map<String, String> row : KnjDbUtils.query(db2, sql.toString())) {
            final String subclassKey;
            if ("333333".equals(KnjDbUtils.getString(row, "SUBCLASSCD")) || "333333".equals(KnjDbUtils.getString(row, "SUBCLASSCD")) || KnjDbUtils.getString(row, "SUBCLASSCD").startsWith("99999")) {
                subclassKey = KnjDbUtils.getString(row, "SUBCLASSCD");
            } else {
                subclassKey = KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD");
            }

            final RecordRankSdivSoutenDat soutenDat = new RecordRankSdivSoutenDat();
            soutenDat._classcd = KnjDbUtils.getString(row, "CLASSCD");
            soutenDat._schoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
            soutenDat._curriculumCd = KnjDbUtils.getString(row, "CURRICULUM_CD");
            soutenDat._subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
            soutenDat._schregno = KnjDbUtils.getString(row, "SCHREGNO");
            soutenDat._totalCredit = KnjDbUtils.getInt(row, "TOTAL_CREDIT", null);
            soutenDat._totalPoint = KnjDbUtils.getInt(row, "TOTAL_POINT", null);
            soutenDat._gradeRank = KnjDbUtils.getInt(row, "GRADE_RANK", null);
            soutenDat._courseRank = KnjDbUtils.getInt(row, "COURSE_RANK", null);
            soutenDat._summaryCredit = KnjDbUtils.getInt(row, "SUMMARY_CREDIT", null);
            soutenDat._summaryPoint = KnjDbUtils.getInt(row, "SUMMARY_POINT", null);
            soutenDat._summaryGradeRank = KnjDbUtils.getInt(row, "SUMMARY_GRADE_RANK", null);
            soutenDat._summaryCourseRank = KnjDbUtils.getInt(row, "SUMMARY_COURSE_RANK", null);
            soutenDat._totalPointIgGs = KnjDbUtils.getInt(row, "TOTAL_POINT_IG_GS", null);
            soutenDat._totalPointHissu = KnjDbUtils.getInt(row, "TOTAL_POINT_HISSU", null);

            soutenDat._grade = grade;
            soutenDat._hrClass = KnjDbUtils.getString(row, "HR_CLASS");
            soutenDat._course = KnjDbUtils.getString(row, "COURSE");
            soutenDat._major = KnjDbUtils.getString(row, "MAJOR");
            soutenDat._classRank = KnjDbUtils.getInt(row, "CLASS_RANK", null);
            soutenDat._majorRank = KnjDbUtils.getInt(row, "MAJOR_RANK", null);

            getMappedHashMap(map, subclassKey).put(soutenDat._schregno, soutenDat);
        }

        return map;
    }

    private static <A, B, C> Map<B, C> getMappedHashMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap<B, C>());
        }
        return map.get(key1);
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    // 格納用クラス
    public static class Store {
        static final String AVG_FLG_HR = "HR";
        static final String AVG_FLG_GRADE = "GRADE";
        static final String AVG_FLG_COURSE = "COURSE";
        static final String AVG_FLG_MAJOR = "MAJOR";

        final Map<String, Map<String, RecordRankSdivSoutenDat>> _map; // Map<subclassKey, Map<schregno, RecordRankSdivSoutenDat>>
        final Map<String, Map<String, Map<String, Average>>> _avgFlgAverageMap; // Map<subclassKey, Map<flg, Map<flg value, RecordRankSdivSoutenDat>>>

        private Store(final Map<String, Map<String, RecordRankSdivSoutenDat>> map) {
            _map = map;
            _avgFlgAverageMap = calcAvg(_map);
        }

        /**
         * RECORD_RANK_SDIV_SOUTEN_DAT
         * @param schregno 学籍番号
         * @param subclassKey 科目のキー
         * @return RECORD_RANK_SDIV_SOUTEN_DAT
         */
        public RecordRankSdivSoutenDat getRecordRankSdivSoutenDat(final String schregno, final String subclassKey) {
            if (null == subclassKey || null == schregno || !_map.containsKey(subclassKey)) {
                return null;
            }
            return _map.get(subclassKey).get(schregno);
        }

        /**
         * flgでグループ化した平均のマップ
         * @param flg 母集団フラグ クラス:"HR", 学年:"GRADE", コース:"COURSE", 学科:"MAJOR"
         * @param subclassKey 科目のキー
         * @return flgでグループ化した平均のマップ
         */
        public Map<String, Average> getAverageMap(final String flg, final String subclassKey) {
            if (null == subclassKey || null == flg || !_map.containsKey(subclassKey)) {
                return new HashMap<String, Average>();
            }
            return new HashMap<String, Average>(_avgFlgAverageMap.get(subclassKey).get(flg));
        }

        private static Map<String, Map<String, Map<String, Average>>> calcAvg(Map<String, Map<String, RecordRankSdivSoutenDat>> _map) {
            Map<String, Map<String, Map<String, List<RecordRankSdivSoutenDat>>>> list = new HashMap<String, Map<String, Map<String, List<RecordRankSdivSoutenDat>>>>();
            for (final String flg : Arrays.asList(AVG_FLG_HR, AVG_FLG_GRADE, AVG_FLG_COURSE, AVG_FLG_MAJOR)) {
                for (final String subclassKey : _map.keySet()) {
                    for (final RecordRankSdivSoutenDat soutenDat : _map.get(subclassKey).values()) {
                        String flgVal = "";
                        if (AVG_FLG_HR.equals(flg)) {
                            flgVal = soutenDat._hrClass;
                        } else if (AVG_FLG_COURSE.equals(flg)) {
                            flgVal = soutenDat._course;
                        } else if (AVG_FLG_MAJOR.equals(flg)) {
                            flgVal = soutenDat._major;
                        } else if (AVG_FLG_GRADE.equals(flg)) {
                            flgVal = soutenDat._grade;
                        } else {
                            continue;
                        }
                        getMappedList(getMappedHashMap(getMappedHashMap(list, subclassKey), flg), flgVal).add(soutenDat);
                    }
                }
            }
            Map<String, Map<String, Map<String, Average>>> rtn = new HashMap<String, Map<String, Map<String, Average>>>();
            for (final String subclassKey : list.keySet()) {
                for (final String flg : list.get(subclassKey).keySet()) {
                    for (final String flgVal : list.get(subclassKey).get(flg).keySet()) {
                        getMappedHashMap(getMappedHashMap(rtn, subclassKey), flg).put(flgVal, new Average(list.get(subclassKey).get(flg).get(flgVal)));
                    }
                }
            }
            return rtn;
        }

        public static Store load(final DB2UDB db2, final String year, final String semetestcd, final String regdSemester, final String grade) {
            return new Store(RecordRankSdivSoutenDat.loadSchregSubclassRecordRankSdivSoutenDatMap(db2, year, semetestcd, regdSemester, grade));
        }
    }

    public static class Average {
        List<RecordRankSdivSoutenDat> _list;
        private Average(List<RecordRankSdivSoutenDat> list) {
            _list = list;
        }
        public int getCount() {
            int count = _list.size();
            return count;
        }

        /**
         * 得点の合計
         * @return 得点の合計
         */
        public String getTotal() {
            if (0 == getCount()) {
                return null;
            }
            Integer total = 0;
            for (RecordRankSdivSoutenDat soutenDat : _list) {
                total += soutenDat._totalPoint;
            }
            return total.toString();
        }

        /**
         * 得点の平均
         * @return 得点の平均
         */
        public String getAvg(final int sishagonyuKeta) {
            if (0 == getCount()) {
                return null;
            }
            Integer total = 0;
            for (RecordRankSdivSoutenDat soutenDat : _list) {
                total += soutenDat._totalPoint;
            }
            return new BigDecimal(total).divide(new BigDecimal(getCount()), sishagonyuKeta, BigDecimal.ROUND_HALF_UP).toString();
        }

        /**
         * 平均点の平均
         * @return 平均点の平均
         */
        public String getAvgAvg(final int sishagonyuKeta) {
            if (0 == getCount()) {
                return null;
            }
            BigDecimal avgSum = BigDecimal.ZERO;
            for (RecordRankSdivSoutenDat soutenDat : _list) {
                avgSum = avgSum.add(new BigDecimal(soutenDat.getTotalAvg(10)));
            }
            return avgSum.divide(new BigDecimal(getCount()), sishagonyuKeta, BigDecimal.ROUND_HALF_UP).toString();
        }

        /**
         * 最高点
         * @return 最高点
         */
        public String getMax() {
            if (0 == getCount()) {
                return null;
            }
            Integer max = 0;
            for (RecordRankSdivSoutenDat soutenDat : _list) {
                if (max < soutenDat._totalPoint) {
                    max = soutenDat._totalCredit;
                }
            }
            return max.toString();
        }

        /**
         * 最低点
         * @return 最低点
         */
        public String getMin() {
            if (0 == getCount()) {
                return null;
            }
            Integer min = 9999;
            for (RecordRankSdivSoutenDat soutenDat : _list) {
                if (min > soutenDat._totalPoint) {
                    min = soutenDat._totalCredit;
                }
            }
            return min.toString();
        }

        public String toString() {
            return "Average(count = " + getCount() + ", total = " + getTotal() + ", avg = " + getAvg(1) + ", avgAvg = " + getAvgAvg(1) + ")";
        }
    }
}

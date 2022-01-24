// kanji=漢字
/*
 * $Id: c2464af5ff1ae07503caa9ca01a4f353da5b9e93 $
 *
 * 作成日: 2009/06/03
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.BufferedOutputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 学校教育システム 賢者 [成績管理] KNJD193 学年別序列票
 */

public class KNJD193 {

    private static final Log log = LogFactory.getLog(KNJD193.class);
    private final int maxLine = 100;

    private Param _param;

    /**
     * KNJD.classから最初に起動されるクラス
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception IO例外
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        final Vrw32alp svf = new Vrw32alp(); // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        boolean nonedata = false;
        DB2UDB db2 = null;
        BufferedOutputStream outstrm = null;

        try {
            // print svf設定
            response.setContentType("application/pdf");
            outstrm = new BufferedOutputStream(response.getOutputStream());
            svf.VrInit();
            svf.VrSetSpoolFileStream(outstrm);

            // ＤＢ接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (db2 == null) {
                log.error("db error");
                return;
            }
            db2.open();

            // パラメータの取得
            _param = createParam(request);
            _param.load(db2);

            // 印刷処理
            nonedata = printSvf(db2, svf);
        } catch (Exception ex) {
            log.debug("exception!", ex);
        }

        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note", "note");
            svf.VrEndPage();
        }
        svf.VrQuit();
        outstrm.close();
        svf.close();

        db2.commit();
        db2.close();
    }

    /** 印刷処理 */
    private boolean printSvf(final DB2UDB db2, final Vrw32alp svf) throws Exception {

        final Set rankSet = createRankSet(db2);

        boolean nonedata = printMain(db2, svf, rankSet);

        return nonedata;
    }

    /** 平均点を小数点1桁で丸めたBigDecimalを得る */
    private BigDecimal getAverageBigDecimal(BigDecimal avgValue) {
        if (avgValue == null) {
            return null;
        }
        return avgValue.setScale(1, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Rankオブジェクトのマップを得る
     * 
     * @param db2
     * @return Rankオブジェクトのマップ
     */
    private Set createRankSet(final DB2UDB db2) {
        final Set rtn = _param.createInitialRankSet();

        try {
            final String sql = sqlRecordRank(_param);
            log.debug(" RECORD_RANK_DAT sql = " + sql);
            db2.query(sql);
            final ResultSet rs = db2.getResultSet();

            while (rs.next()) {
                
                final String schregno = rs.getString("SCHREGNO");
                final String attendno = rs.getString("ATTENDNO");
                final String studentName = rs.getString("NAME");
                final String remark = rs.getString("REMARK3");
                final String hrClass = rs.getString("HR_CLASS");
                if (!_param._homeRoomMap.keySet().contains(hrClass)) {
                    log.debug(" HR " + hrClass + " が見つかりません。 ( " + schregno + " , " + studentName + ")");
                    continue;
                }
                Integer gradeRank = null;
                if (_param.kijuntenIsAverage()) {
                    gradeRank = new Integer(rs.getInt("GRADE_AVG_RANK"));
                } else if (_param.kijuntenIsTotal()) {
                    gradeRank = new Integer(rs.getInt("GRADE_RANK"));
                }
                final HomeRoom homeRoom = _param.getHomeRoom(hrClass);
                final Student student = new Student(homeRoom, attendno, studentName, remark, gradeRank);

                
                BigDecimal key = null;
                final BigDecimal avg = getAverageBigDecimal(rs.getBigDecimal("AVG"));
                if (_param.kijuntenIsAverage()) {
                    key = avg;
                } else if (_param.kijuntenIsTotal()) {
                    key = rs.getBigDecimal("SCORE");
                }
                
                if (key == null) { // 欠点者の対処
                    key = _param.kettenKey;
                } else {
                    // 平均3.5未満の対処
                    final BigDecimal limit = new BigDecimal(3.5);
                    if (avg.compareTo(limit) < 0) {
                        key = _param.avgMimanKey;
                    }
                }

                final Rank rank = getRank(key, rtn);
                if (rank == null) {
                    log.error(" Rank(key '" + key + "') が見つかりません。 ( " + schregno + " , " + studentName + ")");
                    continue;
                }

                rank.addStudent(hrClass, student);
            }

        } catch (SQLException e) {
            log.debug("sql exception!", e);
        }

        return rtn;
    }
    
    private Rank getRank(final BigDecimal key, final Set set) {
        for (final Iterator it=set.iterator(); it.hasNext();) {
            final Rank rank = (Rank) it.next();
            if (rank._key.equals(key)) {
                return rank;
            }
        }
        return null;
    }

    private String sqlRecordRank(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH STD_LIST AS( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.TESTKINDCD, ");
        stb.append("     T1.TESTITEMCD, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T3.NAME, ");
        stb.append("     T3.REMARK3 ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("           AND T2.YEAR = T1.YEAR ");
        stb.append("           AND T2.SEMESTER = '" + _param._schregSemester + "' ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T1.SCHREGNO = T3.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + param._testKindCd + "' ");
        stb.append("     AND T2.GRADE = '" + param._grade + "' ");
        stb.append("     AND NOT EXISTS ");
        stb.append("      (SELECT 'X' FROM NAME_MST WHERE NAMECD1 = 'D107' AND T1.SUBCLASSCD = NAME1 ) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.TESTKINDCD, ");
        stb.append("     T1.TESTITEMCD, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.REMARK3, ");
        stb.append("     T2.SCORE, ");
        stb.append("     T2.AVG, ");
        stb.append("     T2.GRADE_RANK, ");
        stb.append("     T2.GRADE_AVG_RANK ");
        stb.append(" FROM ");
        stb.append("     STD_LIST T1 ");
        stb.append("     LEFT JOIN RECORD_RANK_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("         AND T2.SUBCLASSCD = '999999' ");
        stb.append(" ORDER BY ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO ");
        return stb.toString();
    }

    private void setHead(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("NENDO", _param._year + "年度");
        svf.VrsOut("GRADE", _param.getGradeName());
        svf.VrsOut("SEMESTER", _param.getSemesterName());
        svf.VrsOut("DATE", _param.getDateString());

        for (final Iterator it = _param._homeRoomMap.keySet().iterator(); it.hasNext();) {
            String hrClass = (String) it.next();
            HomeRoom hr = _param.getHomeRoom(hrClass);
            svf.VrsOut("HR_NAME" + hr._index.toString(), hr._name);
            log.debug(hr + " (" + hr._index + ")");
        }
    }

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf, final Set rankSet) {
        boolean nonedata = false;
        int line = 1;
        int page = 1;
        
        final int size = rankSet.size();
        final int maxPage = size / maxLine + ((size % maxLine <= 1) ? 0 : 1); 

        if (page == maxPage) {
            svf.VrSetForm("KNJD193_2.frm", 4);
            printKetten(svf, rankSet); // 欠点者出力のメソッド
        } else {
            svf.VrSetForm("KNJD193.frm", 4);
        }
        setHead(db2, svf); // 見出し出力のメソッド

        for (final Iterator it = rankSet.iterator(); it.hasNext();) {
            nonedata = true;
            
            final Rank rank = (Rank) it.next();
            log.debug(" rank = " + rank);

            //ここでは、欠点者を出力しない
            if (rank._key.equals(_param.kettenKey)) { 
                continue;
            } 
            
            if (line > maxLine) {
                line -= maxLine;
                page += 1;
                log.debug(" new page = " + page);

                if (page == maxPage) {
                    log.debug(" max page! (" + page +")");
                    svf.VrSetForm("KNJD193_2.frm", 4);
                    printKetten(svf, rankSet); // 欠点者出力のメソッド
                } else {
                    svf.VrSetForm("KNJD193.frm", 4);
                }
                setHead(db2, svf); // 見出し出力のメソッド
            }

            printRankHead(svf, rank);
            printRankHomeRoom(svf, rank, "NAME", line);
            svf.VrEndRecord();
            line += 1;
        }
        return nonedata;
    }

    /** 欠点者を出力する */
    private void printKetten(final Vrw32alp svf, final Set rankSet) {
        //最終ページの場合、最初に出力データをセットしておく
        for (final Iterator it = rankSet.iterator(); it.hasNext();) {
            final Rank rank = (Rank) it.next();
            if (rank._key.equals(_param.kettenKey)) { 
                log.debug(" rank = " + rank);
                printRankHomeRoom(svf, rank, "FNAME", -1);
            } 
        }
    }

    /** 総合点・平均点・序列を表示する */
    private void printRankHead(final Vrw32alp svf, final Rank rank) {
        String suffix = (rank._key.equals(_param.avgMimanKey)) ? "" : "1"; //1:6桁用 空白:8桁用
        String keyField = null;
        String keyValue = null;
        String avgValue = null;
        String avgMiman = _param.avgMiman.toString() + "未満";
        String scoreMiman = _param.scoreMiman.toString() + "未満";

        if (_param.kijuntenIsAverage()) {
            keyField = "AVERAGE";
            keyValue = (rank._key.equals(_param.avgMimanKey)) ? avgMiman : rank._key.toString();
        } else if (_param.kijuntenIsTotal()) {
            keyField = "TOTAL";
            keyValue = (rank._key.equals(_param.avgMimanKey)) ? scoreMiman : rank._key.toString();
            avgValue = (rank._key.equals(_param.avgMimanKey)) ? avgMiman : rank._avg.toString();
        }

        svf.VrsOut(keyField + suffix, keyValue);

        // (総合点表示の場合、平均点も表示する)
        if (_param.kijuntenIsTotal()) {
            svf.VrsOut("AVERAGE" + suffix, avgValue);
        }
        
        if (rank.getCount() != 0) {
            svf.VrsOut("RANK" + suffix, rank.getRank().toString());
        }
    }

    /** HR毎の生徒を表示する */
    private void printRankHomeRoom(final Vrw32alp svf, final Rank rank, final String fieldHead, final int line) {

        // クリア処理
        if (line == 1) {
            for(int l=1; l<=maxLine; l++) {
                for (int hrIndex = 1; hrIndex <= _param.hrColumnCount; hrIndex++) {
                    String alphabet = new String[]{"","A","B","C","D","E"}[hrIndex]; // フィールド
                    for (int index = 1; index <= _param.maxCountPerHR; index++) {
                        String field = fieldHead + alphabet + index;
                        svf.VrsOutn(field + "-1", l, "");
                        svf.VrsOutn(field + "-2", l, "");
                        svf.VrsOutn(field + "-3", l, "");
                        svf.VrsOut(field, "");
                    }
                }
            }
        }

        boolean needEndRecord = false;
        for (final Iterator it2 = rank.hrClassMap.keySet().iterator(); it2.hasNext();) {
            String hrClass = (String) it2.next();

            final int hrIndex = _param.getHomeRoom(hrClass)._index.intValue(); // HRのインデックス
            String alphabet = new String[]{"","A","B","C","D","E"}[hrIndex]; // フィールド
            final List studentList = (List) rank.hrClassMap.get(hrClass); // HRの生徒のリスト
            int index = 1;

            for (final Iterator stIte = studentList.iterator(); stIte.hasNext();) {
                Student student = (Student) stIte.next();
                String suffix = "";
                if (student.getName() != null && student.getName().length() > 6) {
                    suffix = "-2";//１０文字
                } else if (student.getName() != null && student.getName().length() > 3) {
                    suffix = "-1";//６文字
                } else {
                    suffix = "-3";//３文字
                }
                final String field = fieldHead + alphabet + index + suffix;

                if ("FNAME".equals(fieldHead)) {
                    log.debug("[" + field + "] " + student.getName());
                    svf.VrsOut(field, student.getName());
                    needEndRecord = true;
                } else {
                    svf.VrsOutn(field, line, student.getName());
                }
                index += 1;
            }
        }
        if (needEndRecord) {
//            svf.VrEndRecord();
        }
    }

    /**
     * パラメータ作成
     * 
     * @param req
     * @return パラメータ
     */
    private Param createParam(final HttpServletRequest req) {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        for (final Enumeration en = req.getParameterNames(); en.hasMoreElements();) {
            String parameterName = (String) en.nextElement();
            log.debug("parameter " + parameterName + " = " + req.getParameter(parameterName));
        }
        final String year = req.getParameter("CTRL_YEAR");
        final String semester = req.getParameter("SEMESTER");
        final String ctrlSemester = req.getParameter("CTRL_SEMESTER");
        final String testKindCdItemCd = req.getParameter("TESTKINDCD");
        final String grade = req.getParameter("GRADE_HRCLASS");
        final String loginDate = req.getParameter("CTRL_DATE");
        final int kijunten = Integer.parseInt(req.getParameter("JORETU_DIV"));
        final String subClassCount = req.getParameter("KAMOKU_SU");

        return new Param(year, semester, ctrlSemester, testKindCdItemCd, grade, loginDate, kijunten, subClassCount);
    }

    /**
     * 総合点または平均点をキーとして、該当する各クラスの生徒を保持する。
     */
    private class Rank implements Comparable {
        /** 総合点 または 平均点 */
        final BigDecimal _key;

        /** (総合点表示の場合、平均点も表示する) */
        final BigDecimal _avg;

        /** クラスのマップ */
        final Map hrClassMap;

        public Rank(BigDecimal key, BigDecimal avg) {
            _key = key;
            _avg = avg;
            hrClassMap = new TreeMap();
        }

        public int getCount() {
            int count = 0;
            for (final Iterator it=hrClassMap.keySet().iterator(); it.hasNext();) {
                String hrClass = (String) it.next();
                List studentList = (List) hrClassMap.get(hrClass);
                count += studentList.size();
            }
            return count;
        }
        
        public Integer getRank() {
            Integer rank = new Integer(Integer.MAX_VALUE);
            for (final Iterator it=hrClassMap.keySet().iterator(); it.hasNext();) {
                String hrClass = (String) it.next();
                List studentList = (List) hrClassMap.get(hrClass);
                for (Iterator it2 = studentList.iterator(); it2.hasNext();) {
                    Student student = (Student) it2.next();
                    if (student._gradeRank.compareTo(rank) < 0) {
                        rank = student._gradeRank;
                    }
                }
            }
            return rank;
        }

        public void addStudent(final String hrClass, final Student student) {
            List list = getHrClassList(hrClass);
            if (list.size() >= _param.maxCountPerHR) { // 人数がオーバーしていたら追加欄に追加する
                list = getHrClassList(_param.appendedHrClass);
            }
            if (list.size() >= _param.maxCountPerHR) {
                log.debug("追加欄の人数がオーバーしています。(" + student + ")");
            }
            list.add(student);
        }

        private List getHrClassList(final String hrClass) {
            if (hrClassMap.get(hrClass) == null) {
                hrClassMap.put(hrClass, new ArrayList());
            }
            return (List) hrClassMap.get(hrClass);
        }

        /**
         * @return 序列の行に含まれるホームルームと学生名のリストの文字列
         */
        public String toString() {
            final StringBuffer stb = new StringBuffer(_key + ":");
            for (Iterator it = hrClassMap.keySet().iterator(); it.hasNext();) {
                String hrClass = (String) it.next();
                stb.append("\n " + _param.getHomeRoom(hrClass) + " name = ");
                List studentList = (List) hrClassMap.get(hrClass);
                String comma = " ";
                for (Iterator it2 = studentList.iterator(); it2.hasNext();) {
                    Student student = (Student) it2.next();
                    stb.append(comma + student);
                    comma = " , ";
                }
            }
            return stb.toString();
        }
        
        public int compareTo(Object o) {
            if (o instanceof Rank) {
                Rank other = (Rank) o;
                return -_key.compareTo(other._key);
            }
            return -1;
        }
    }

    /**
     * ホームルーム。名称と列のインデックスを保持する。
     */
    private class HomeRoom {
        final String _hrClass;

        final String _name;

        final Integer _index;

        public HomeRoom(final String hrClass, final String name, final Integer index) {
            _hrClass = hrClass;
            _name = name;
            _index = index;
        }

        public String toString() {
            return "HomeRoom " + _name;
        }
    }

    /**
     * 生徒。
     */
    private class Student {
        private final HomeRoom _hr;

        private final String _attendno;

        private final String _name;
        private final String _remark;
        
        private final Integer _gradeRank;

        public Student(final HomeRoom hr, final String attendno, final String name, final String remark, Integer gradeRank) {
            _hr = hr;
            _attendno = attendno;
            _name = name;
            _remark = remark;
            _gradeRank = gradeRank;
        }

        public String getName() {
            if (null != _remark) return _remark; //同姓同名
            if (null != _name) {
                int endIndex = _name.indexOf("　");
                if (0 < endIndex) return _name.substring(0, endIndex); //名字
            }
            return _name;
        }

        public String toString() {
            return _hr + " " + _attendno + " " + _name + " " + _gradeRank;
        }
    }

    private class Param {

        private final String _year;

        private final String _semester;

        private final String _schregSemester;

        private final String _testKindCd;

        private final String _grade;

        private final String _loginDate;

        private final int _kijunten;

        /** 全ホームルームのマップ */
        private Map _homeRoomMap = null;
        
        private String _gradeName;
        
        private String _semesterName;

        /** 追加欄のクラスコード (処理用) */
        final String appendedHrClass = "9999";

        /** 各クラス欄の最大生徒数 */
        final int maxCountPerHR = 5;

        /** ホームルームの列数 */
        final int hrColumnCount = 5;
        
        /** 平均3.5未満リスト用のキー */
        final BigDecimal avgMimanKey = new BigDecimal(-35);
        BigDecimal scoreMiman;
        BigDecimal avgMiman;
        
        /** 欠点者リスト用のキー */
        final BigDecimal kettenKey = new BigDecimal(-99999);

        final int _kamokuSu; 

        public Param(final String year,
                final String semester, 
                final String ctrlSemester, 
                final String testKindCd, 
                final String grade, 
                final String loginDate, 
                final int kijunten,
                final String kamokuSu) {
            _year = year;
            _semester = semester;
            _schregSemester = "9".equals(semester) ? ctrlSemester : semester;
            _testKindCd = testKindCd;
            _grade = grade;
            _loginDate = loginDate;
            _kijunten = kijunten;
            
            if (kijuntenIsTotal()) {
                _kamokuSu = Integer.parseInt(kamokuSu);;
            } else {
                _kamokuSu = -1;
            }
        }

        public String getDateString() {
            return _loginDate.substring(0,4) + "年" + KNJ_EditDate.h_format_JP_MD(_loginDate);
        }

        public void load(DB2UDB db2) {
            try {
                setSemester(db2);
                setHomeRoom(db2);
                setGradeName(db2);
            } catch (Exception e) {
                log.debug("exception!", e);
            }
        }

        private void setSemester(DB2UDB db2)
        throws Exception {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT DISTINCT ");
            sql.append("   T1.SEMESTER, ");
            sql.append("   T1.SEMESTERNAME ");
            sql.append(" FROM ");
            sql.append("   SEMESTER_MST T1 ");
            sql.append(" WHERE ");
            sql.append("   T1.YEAR = '" + _param._year + "' ");
            sql.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
            db2.query(sql.toString());

            final ResultSet rs = db2.getResultSet();
            if (rs.next()) {
                _semesterName = rs.getString("SEMESTERNAME");
            }
        }

        /**
         * 表示するホームルーム(名称、インデックス)をマップにセットする
         */
        private void setHomeRoom(DB2UDB db2)
        throws Exception {
            _homeRoomMap = new TreeMap();
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT DISTINCT ");
            sql.append("   T1.HR_CLASS, ");
            sql.append("   T1.HR_NAME ");
            sql.append(" FROM ");
            sql.append("   SCHREG_REGD_HDAT T1 ");
            sql.append(" WHERE ");
            sql.append("   T1.YEAR = '" + _param._year + "' ");
            sql.append("   AND T1.SEMESTER = '" + _param._schregSemester + "' ");
            sql.append("   AND T1.GRADE = '" + _param._grade + "' ");
            db2.query(sql.toString());

            final ResultSet rs = db2.getResultSet();
            int dispIndex = 1;
            while (rs.next()) {
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");

                if (dispIndex < _param.hrColumnCount) {
                    HomeRoom hr = new HomeRoom(hrClass, hrName, new Integer(dispIndex));
                    _homeRoomMap.put(hrClass, hr);
                    dispIndex += 1;
                } else {
                    log.debug("ホームルームの列数がオーバーしています。(HR_CLASS = " + hrClass + ")");
                }
            }

            // 最後の列は追加欄
            _homeRoomMap.put(_param.appendedHrClass, new HomeRoom(_param.appendedHrClass, "追加", new Integer(dispIndex)));
        }

        /**
         * ホームルームを得る
         * 
         * @param hrClass ホームルームのコード
         * @return ホームルーム
         */
        HomeRoom getHomeRoom(String hrClass) {
            return (HomeRoom) _homeRoomMap.get(hrClass);
        }

        /** ソートに総合点を使用する (総合点、平均点を表示する) */
        public boolean kijuntenIsTotal() {
            return _kijunten == 1;
        }

        /** ソートに平均点を使用する (平均点のみを表示する = 科目数が生徒によって異なる) */
        public boolean kijuntenIsAverage() {
            return _kijunten == 2;
        }

        /**
         * 総点または平均点をキーとするマップを作成する
         * 
         * @return 総点または平均点をキーとするマップ
         */
        public Set createInitialRankSet() {
            final Set rtn = new TreeSet();
            final BigDecimal limit = new BigDecimal(3.5);

            if (kijuntenIsAverage()) { // 平均点0.1点刻み
                final BigDecimal num10 = new BigDecimal(10.0);
                for(int i=100; i>=0; i--) {
                    final BigDecimal score = new BigDecimal(i);
                    final BigDecimal avg = score.divide(num10, 1, BigDecimal.ROUND_HALF_UP);
                    if (avg.compareTo(limit) < 0) break;
                    final Rank rank = new Rank(avg, null);
                    rtn.add(rank);
                    scoreMiman = score;
                    avgMiman = avg;
                }

            } else if (kijuntenIsTotal()) { // 総点1点刻み (ただし平均点が3.5以上のみ)
                final BigDecimal subclassCount = new BigDecimal(_kamokuSu);
                final int totalScoreMax = _kamokuSu * 10;
                for(int i=totalScoreMax;; i--) {
                    final BigDecimal score = new BigDecimal(i);
                    final BigDecimal avg = score.divide(subclassCount, 1, BigDecimal.ROUND_HALF_UP);
                    if (avg.compareTo(limit) < 0) break;
                    final Rank rank = new Rank(score, avg);
                    rtn.add(rank);
                    scoreMiman = score;
                    avgMiman = avg;
                }
            }
            
            rtn.add(new Rank(avgMimanKey, null)); // 平均3.5未満
            rtn.add(new Rank(kettenKey, null)); // 欠点者
            return rtn;
        }
        
        private void setGradeName(final DB2UDB db2) 
        throws Exception {
            String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            if (rs.next()) {
                _gradeName = rs.getString("GRADE_NAME1");
            }
        }
        
        public String getGradeName() {
            return _gradeName;
        }
        
        public String getSemesterName() {
            return _semesterName;
        }
    }
}

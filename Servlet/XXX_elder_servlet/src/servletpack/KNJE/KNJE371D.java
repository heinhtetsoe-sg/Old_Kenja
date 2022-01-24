// kanji=漢字
package servletpack.KNJE;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJE.KNJE070_1.HyoteiHeikin;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id$
 */
public class KNJE371D {

    private static final Log log = LogFactory.getLog("KNJE371D.class");

    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);
            _hasData = false;

            //科目参照に必須、存在しなければ印字しない
            if(_param._nameMstE069Check) {
                _hasData = printMain(db2, svf);
            } else {
                log.error("名称マスタE069が存在しません。");
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map<String, Student> studentList = getStudentList(db2);

        if (studentList.isEmpty()) {
            return false;
        }
        final TreeMap<Integer, Student> printStudentList = getPrintStudentList(db2, studentList);
        final TreeMap<String, Printclass> printSubclassList = getPrintSubClassList(db2, printStudentList);
        svf.VrSetForm("KNJE371D.frm", 4);
        svf.VrsOut("TITLE",_param._facultyName + _param._departmentName + "　成績一覧表");

        String classcd = "";

        printALLmain(svf, printSubclassList, printStudentList);

        if (_param._isOutputDebug) {
            for (final Student student : printStudentList.values()) {
                log.info(" " + student._schregno + "  "  + student._name + ", 単位数 " + student._knje070Credit + "(評定平均母集団単位数 " + student._allCredit + "), 評定平均 " + student._allAvg + " (評定平均分子 " + student._allAvgBunshi + ", 分母 " + student._allAvgBunbo + ", baseDetailMst = " + student._baseDetailMst + ")");
            }
        }

        //教科毎のループ
        for (final String key1 : printSubclassList.keySet()) {
            final Printclass printclass = printSubclassList.get(key1);
            final int subclassMaxSize = printclass._subclassList.size() + 2;
            final int nameSize = printclass._className.length();

            List<String> strList = null;
            BigDecimal calcWakuSize = new BigDecimal(Math.ceil(subclassMaxSize / 2.0));
            int calcwk = nameSize - ((subclassMaxSize + 1) % 2);
            BigDecimal calcStrLen = new BigDecimal(Math.floor(calcwk / 2.0));
            int strtPt = calcWakuSize.subtract(calcStrLen).intValue();
            strtPt = strtPt == 0 ? 1 : strtPt;
            for (int cnt = 0; cnt < nameSize; cnt++) {
                if (cnt < subclassMaxSize) {
                    if (strList == null ) strList = new ArrayList();
                    strList.add(String.valueOf(strtPt + cnt));
                }
            }
            int nameIdx = 0;
            int rcnt = 1; //ループ回数
            final int listMax = strList.size();

            //科目毎のループ
            for (final String subkey : printclass._subclassList.keySet()) {
                final Subclass subclass = printclass._subclassList.get(subkey);
                classcd = printclass._classCd;
                svf.VrsOut("GRPCD1", classcd);

                if (listMax > nameIdx) {
                    if (Integer.parseInt(strList.get(nameIdx)) == rcnt) {
                        svf.VrsOut("course1", printclass._className.substring(nameIdx, nameIdx + 1)); //教科
                        nameIdx++;
                    }
                }
                if (null != subclass._subclassName) {
                    svf.VrsOut("SUBCLASS1", subclass._subclassName.substring(0, Math.min(subclass._subclassName.length(), 14))); //科目
                }
                String Grade = subclass._annual.replace("0", "");
                Grade = "6".equals(Grade) ? "3" : "5".equals(Grade) ? "2" : "4".equals(Grade) ? "1" : Grade;
                svf.VrsOut("GRADE1", Grade); //学年
                svf.VrsOut("CREDIT1", subclass._credit); //単位

                int stuCnt = 1;

                //生徒名、評定
                for (final Student student : printStudentList.values()) {

                    svf.VrsOutn("name1", stuCnt, student._name);

                    if(student._score.containsKey(subkey)) {
                        if (student._score.get(subkey) != null) {
                            svf.VrsOutn("SCORE1_1", stuCnt, student._score.get(subkey));

                            // 表示される評定の数をカウント
                            if (student._scoreCnt.containsKey(classcd)) {
                                int cnt = student._scoreCnt.get(classcd) == null ? 0 : student._scoreCnt.get(classcd).intValue();
                                student._scoreCnt.put(classcd, ++cnt);
                            } else {
                                student._scoreCnt.put(classcd, 1);
                            }

                            // 教科評定平均の計算
                            final int score = Integer.parseInt(student._score.get(subkey));
                            final int tani = subclass._credit == null ? 1 : Integer.parseInt(subclass._credit);
                            int val = 0;

                            val = score;
                            if (student._totalScore.containsKey(classcd + "tanjun")) {
                                int total = Integer.parseInt(student._totalScore.get(classcd + "tanjun"));
                                total += val;
                                student._totalScore.put(classcd + "tanjun", String.valueOf(total));
                            } else {
                                student._totalScore.put(classcd + "tanjun", String.valueOf(val));
                            }
                            val = score * tani;

                            if (student._totalScore.containsKey(classcd + "kajuu")) {
                                int total = Integer.parseInt(student._totalScore.get(classcd + "kajuu"));
                                total += val;
                                student._totalScore.put(classcd + "kajuu", String.valueOf(total));
                            } else {
                                student._totalScore.put(classcd + "kajuu", String.valueOf(val));
                            }

                            // 教科修得単位数の計算
                            if (subclass._credit != null) {
                                if (student._totalCredit.containsKey(classcd)) {
                                    int total = Integer.parseInt(student._totalCredit.get(classcd));
                                    total += Integer.parseInt(subclass._credit);
                                    student._totalCredit.put(classcd, String.valueOf(total));
                                } else {
                                    student._totalCredit.put(classcd, subclass._credit);
                                }
                            }
                        }
                    }
                    stuCnt++;
                }
                rcnt++;
                svf.VrEndRecord();
            }

            //教科取得単位数の計、教科評定平均
            svf.VrsOut("GRPCD2", classcd);
            svf.VrsOut("SUBCLASS2", "教科取得単位数の計");
            int stuCnt = 1;
            for (final Student student : printStudentList.values()) {

                if(student._totalCredit.containsKey(classcd)) {
                    svf.VrsOutn("SCORE2_1", stuCnt, student._totalCredit.get(classcd));
                }
                stuCnt++;
            }
            svf.VrEndRecord();
            svf.VrsOut("GRPCD2", classcd);
            svf.VrsOut("SUBCLASS2", "教科評定平均");


            kyoukaHyouteiAvg(svf, printStudentList, classcd, 1);
            svf.VrEndRecord();
        }

        svf.VrEndPage();

        return true;
    }

    //6,全教科
    private void printALLmain(final Vrw32alp svf, final TreeMap<String, Printclass> printSubclassList, final TreeMap<Integer, Student> studentList) {
        int cnt = 1;
        for (final Student student : studentList.values()) {

            svf.VrsOutn("TOTAL_CREDIT1", cnt, student._6Credit); //6教科修得単位数の計
            svf.VrsOutn("TOTAL_CREDIT2", cnt, student._knje070Credit); //全教科修得単位数の計
            svf.VrsOutn("TOTAL_AVERAGE1", cnt, student._6Avg); //6教科評定平均
            svf.VrsOutn("TOTAL_AVERAGE2", cnt, student._allAvg); //全教科評定平均
            svf.VrsOutn("TOTAL_RANK", cnt, student._sekiji); // 席次
            cnt++;
        }
    }

    //教科評定平均
    private void kyoukaHyouteiAvg(final Vrw32alp svf, final TreeMap<Integer, Student> studentList, final String classcd, int cnt) {
        for (final Student student : studentList.values()) {
            if(_param._doushisyaFlg) { // 単位加重平均 : 単位数＊評定の教科内合計 ÷ 修得単位数の計
                if(student._totalScore.containsKey(classcd + "kajuu") ) {
                    BigDecimal totalScore =  new BigDecimal(student._totalScore.get(classcd + "kajuu"));
                    if(student._totalCredit.containsKey(classcd)) {
                        BigDecimal totalCredit =  new BigDecimal(student._totalCredit.get(classcd));
                        svf.VrsOutn("SCORE2_2", cnt, String.valueOf( totalScore.divide(totalCredit, 1, BigDecimal.ROUND_DOWN)));
                    }
                }
            } else { // 単純平均 : 評定の教科内合計 ÷ 評定の数
                if(student._totalScore.containsKey(classcd + "tanjun") ) {
                    BigDecimal totalScore =  new BigDecimal(student._totalScore.get(classcd + "tanjun"));
                    if(student._scoreCnt.containsKey(classcd)) {
                        BigDecimal scoreCnt =  new BigDecimal(student._scoreCnt.get(classcd));
                        svf.VrsOutn("SCORE2_1", cnt, String.valueOf( totalScore.divide(scoreCnt, 0, BigDecimal.ROUND_HALF_UP)));
                    }
                }
            }
            cnt++;
        }
    }

    private Map<String, Student> getStudentList(final DB2UDB db2) throws SQLException {
        Map<String, Student> retMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.ANNUAL, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.NAME_ENG, ");
            stb.append("     ROW_NUMBER() OVER(ORDER BY T2.NAME_ENG, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO) AS ALPHABET_ORDER ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1   ");
            stb.append(" LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' AND ");
            stb.append("     T1.SCHREGNO IN (SELECT ");
            stb.append("                         SCHREGNO ");
            stb.append("                     FROM ");
            stb.append("                         AFT_GRAD_COURSE_DAT ");
            stb.append("                     WHERE ");
            stb.append(" STAT_CD = '" + _param._schoolCd + "' AND ");
            stb.append(" FACULTYCD = '" + _param._facultyCd + "' AND ");
            stb.append(" DEPARTMENTCD = '" + _param._departmentCd + "' AND ");
            stb.append(" PLANSTAT = '1' "); //進路状況 決定者のみ
            stb.append("                     )  AND ");
            stb.append("     T1.SEMESTER = '" + _param._semester + "' AND ");
            stb.append("     T1.GRADE >= '03' ");

            log.debug(" studentList sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String annual = rs.getString("ANNUAL");
                final String name = rs.getString("NAME");
                final String name_eng = rs.getString("NAME_ENG");
                final int alphabetOrder = rs.getInt("ALPHABET_ORDER");

                final Student student = new Student(schregno, grade, annual, name, name_eng, alphabetOrder);


                retMap.put(schregno, student);
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private TreeMap<Integer, Student> getPrintStudentList(final DB2UDB db2, final Map<String, Student> studentList) throws SQLException {
        TreeMap<Integer, Student> retMap = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String schregNoList = "";
        for (final String getKey : studentList.keySet()) {

            if(!"".equals(schregNoList)){
                schregNoList +=  ",";
            }
            final Student student = studentList.get(getKey);
            schregNoList +=  "'" + student._schregno + "'";
        }
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ALLTOTAL AS(SELECT ");
        stb.append("     T3.SCHREGNO, ");
        stb.append("     SUM(T3.GET_CREDIT) AS TOTAL_CREDIT, ");
        stb.append("     SUM(T3.VALUATION * T3.GET_CREDIT) AS KAJUU, ");
        stb.append("     AVG(DECIMAL(T3.VALUATION, 5, 1)) AS HYOTEI_HEIKIN, ");
        stb.append("     SUM(T3.VALUATION) AS HYOTEI_GOUKEI, ");
        stb.append("     COUNT(T3.VALUATION) AS HYOTEI_COUNT ");
        stb.append(" FROM ");
        stb.append("     CLASS_MST T1    ");
        stb.append(" LEFT JOIN SUBCLASS_MST T2 ON T1.CLASSCD = T2.CLASSCD AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append(" LEFT JOIN SCHREG_STUDYREC_DAT T3 ON T2.CLASSCD = T3.CLASSCD AND T2.SUBCLASSCD = T3.SUBCLASSCD AND T2.SCHOOL_KIND = T3.SCHOOL_KIND AND T2.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append(" WHERE ");
        stb.append("     T3.SCHREGNO IN (" + schregNoList + ") AND ");
        stb.append("     T3.VALUATION <> 0 "); // null以外かつ0以外
        stb.append(" GROUP BY ");
        stb.append("     T3.SCHREGNO ");
        stb.append(" ), ALLAVG AS ( ");
        stb.append("  SELECT ");
        stb.append("      T1.*,  ");
        if (_param._doushisyaFlg) {
            stb.append("      DECIMAL(DECIMAL(FLOOR(10 * (DECIMAL(T1.KAJUU,5,1) / DECIMAL(T1.TOTAL_CREDIT,5,1)))) / 10, 5, 1) AS ALLAVG "); // 小数第2位切捨
        } else {
            stb.append("      DECIMAL(ROUND(T1.HYOTEI_HEIKIN, 0), 1) AS ALLAVG "); // 小数第1位四捨五入
        }
        stb.append(" FROM ALLTOTAL T1 ");
        stb.append(" ORDER BY ALLAVG DESC ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     BD_RANK.BASE_REMARK1 AS SEKIJI, ");
        stb.append("     ROW_NUMBER() OVER(ORDER BY INT(BD_RANK.BASE_REMARK1), T2.GRADE, T2.HR_CLASS, T2.ATTENDNO) AS SEKIJI_ORDER, ");
        if (_param._doushisyaFlg) {
            stb.append("     T1.KAJUU AS ALLAVG_BUNSHI, ");
            stb.append("     T1.TOTAL_CREDIT AS ALLAVG_BUNBO, ");
        } else {
            stb.append("     T1.HYOTEI_GOUKEI AS ALLAVG_BUNSHI, ");
            stb.append("     T1.HYOTEI_COUNT AS ALLAVG_BUNBO, ");
        }
        stb.append("     BD_RANK.BASE_REMARK2 AS KNJE065B_AVG, ");
        stb.append("     BD_RANK.BASE_REMARK3 AS KNJE065B_SUM, ");
        stb.append("     BD_RANK.BASE_REMARK4 AS KNJE065B_COUNT, ");
        stb.append("     BD_RANK.BASE_REMARK5 AS KNJE065B_BUNBO ");
        stb.append(" FROM ALLAVG T1 ");
        stb.append(" LEFT JOIN SCHREG_BASE_DETAIL_MST BD_RANK ON BD_RANK.SCHREGNO = T1.SCHREGNO ");
        if (_param._doushisyaFlg) {
            // 多重平均
            stb.append("     AND BD_RANK.BASE_SEQ = '017' ");
        } else {
            // 単純平均
            stb.append("     AND BD_RANK.BASE_SEQ = '018' ");
        }
        stb.append(" LEFT JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T2.YEAR = '" + _param._year + "' ");
        stb.append("     AND T2.SEMESTER = '" + _param._semester + "' ");


        log.debug(" All sql =" + stb.toString());

        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final Student student = studentList.get(schregno);
                student._allCredit = rs.getString("TOTAL_CREDIT");
                student._allAvg = rs.getString("ALLAVG");
                student._sekiji = rs.getString("SEKIJI");
                student._sekijiOrder = rs.getInt("SEKIJI_ORDER");
                student._allAvgBunshi = rs.getString("ALLAVG_BUNSHI");
                student._allAvgBunbo = rs.getString("ALLAVG_BUNBO");
                student._baseDetailMst.put("AVG", rs.getString("KNJE065B_AVG"));
                student._baseDetailMst.put("SUM", rs.getString("KNJE065B_SUM"));
                student._baseDetailMst.put("COUNT", rs.getString("KNJE065B_COUNT"));
                student._baseDetailMst.put("BUNBO", rs.getString("KNJE065B_BUNBO"));
            }
        } catch (final SQLException e) {
            log.error("全教科の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        for (final Student student : studentList.values()) {
            if("1".equals(_param._sort)) { //アルファベット順、席次順
                retMap.put(student._alphabetOrder, student);
            } else {
                retMap.put(student._sekijiOrder, student);
            }
        }

        final StringBuffer stb2 = new StringBuffer();
        stb2.append(" WITH ALLTOTAL AS(SELECT ");
        stb2.append("     T3.SCHREGNO, ");
        stb2.append("     SUM(T3.GET_CREDIT) AS TOTAL_CREDIT, ");
        stb2.append("      AVG(DECIMAL(T3.VALUATION, 5, 1)) AS HYOTEI_HEIKIN, ");
        stb2.append("     DECIMAL(SUM(T3.VALUATION * T3.GET_CREDIT),5,1) AS KAJUU ");
        stb2.append(" FROM ");
        stb2.append("     CLASS_MST T1    ");
        stb2.append(" LEFT JOIN SUBCLASS_MST T2 ON T1.CLASSCD = T2.CLASSCD AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb2.append(" LEFT JOIN SCHREG_STUDYREC_DAT T3 ON T2.CLASSCD = T3.CLASSCD AND T2.SUBCLASSCD = T3.SUBCLASSCD AND T2.SCHOOL_KIND = T3.SCHOOL_KIND AND T2.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb2.append(" WHERE ");
        stb2.append("     T3.SCHREGNO IN (" + schregNoList + ") AND ");
        stb2.append("     T3.VALUATION IS NOT NULL AND ");
        stb2.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND IN (SELECT ");
        stb2.append("                          NAME1 ");
        stb2.append("                      FROM ");
        stb2.append("                          NAME_MST ");
        stb2.append("                      WHERE ");
        stb2.append("                          NAMECD1 = 'E069' ");
        stb2.append("                      ) ");
        stb2.append(" GROUP BY ");
        stb2.append("     T3.SCHREGNO ");
        stb2.append(" ) SELECT T1.*,  ");
        if (_param._doushisyaFlg) {
            stb2.append("      DECIMAL(FLOOR(10 * T1.KAJUU / T1.TOTAL_CREDIT)) / 10 AS ALLAVG "); // 小数第2位切捨
        } else {
            stb2.append("      DECIMAL(ROUND(T1.HYOTEI_HEIKIN, 0), 1) AS ALLAVG "); // 小数第1位四捨五入
        }
        stb2.append(" FROM ALLTOTAL T1 ");
        stb2.append(" ORDER BY ALLAVG DESC ");

        log.debug(" 6 sql =" + stb2.toString());

        try {
            ps = db2.prepareStatement(stb2.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final Student student = studentList.get(schregno);
                student._6Credit = rs.getString("TOTAL_CREDIT");
                student._6Avg = rs.getString("ALLAVG");
            }
        } catch (final SQLException e) {
            log.error("6教科の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        KNJDefineSchool defineSchool = new KNJDefineSchool();
        defineSchool.defineCode(db2, _param._year);
        final KNJE070_1 knje070_1 = new KNJE070_1(db2, (Vrw32alp) null, defineSchool, (String) null);
        try {
            _param._knje070Paramap.put("DOCUMENTROOT", _param._documentroot);
            _param._knje070Paramap.put("totalOnly", "1");
            for (final Student student : studentList.values()) {
                final List<HyoteiHeikin> hyoteiHeikinList = knje070_1.getHyoteiHeikinList(student._schregno, _param._year, _param._semester, _param._knje070Paramap);
                for (final KNJE070_1.HyoteiHeikin heikin : hyoteiHeikinList) {
                    if ("TOTAL".equals(heikin.classkey())) {
                        student._knje070Credit = heikin.credit();
                    }
                }
            }
        } catch (Throwable t) {
            log.warn("KNJE070 failed.", t);
        }
        knje070_1.pre_stat_f();
        return retMap;
    }


    private TreeMap<String, Printclass> getPrintSubClassList(final DB2UDB db2, final TreeMap<Integer, Student> studentList) throws SQLException {
        TreeMap<String, Printclass> classList = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        for (final Student student : studentList.values()) {
            log.debug(student);

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     CLM.CLASSCD || '-' || CLM.SCHOOL_KIND || '-' || SUBM.CURRICULUM_CD || '-' || SUBM.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     VALUE(CLM.CLASSORDERNAME2, CLM.CLASSNAME) AS CLASSNAME, ");
                stb.append("     VALUE(SUBM2.SUBCLASSORDERNAME2, SUBM2.SUBCLASSNAME, SUBM.SUBCLASSORDERNAME2,SUBM.SUBCLASSNAME) AS SUBCLASSNAME , ");
                stb.append("     CLM.CLASSCD || '-' || CLM.SCHOOL_KIND || '-' || SUBM.CURRICULUM_CD || '-' || SUBM.SUBCLASSCD2 AS SUBCLASSCD2, ");
                stb.append("     T3.YEAR, ");
                stb.append("     T3.SCHREGNO, ");
                stb.append("     T3.ANNUAL, ");
                stb.append("     T3.VALUATION, ");
                stb.append("     T3.GET_CREDIT AS CREDITS ");
                stb.append(" FROM ");
                stb.append("     SCHREG_STUDYREC_DAT T3 ");
                stb.append(" LEFT JOIN CLASS_MST CLM ON CLM.CLASSCD = T3.CLASSCD AND CLM.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON T3.CLASSCD = SUBM.CLASSCD AND T3.SCHOOL_KIND = SUBM.SCHOOL_KIND AND T3.CURRICULUM_CD = SUBM.CURRICULUM_CD AND T3.SUBCLASSCD = SUBM.SUBCLASSCD ");
                stb.append(" LEFT JOIN SUBCLASS_MST SUBM2 ON T3.CLASSCD = SUBM2.CLASSCD AND T3.SCHOOL_KIND = SUBM2.SCHOOL_KIND AND T3.CURRICULUM_CD = SUBM2.CURRICULUM_CD AND SUBM.SUBCLASSCD2 = SUBM2.SUBCLASSCD ");
                stb.append(" WHERE ");
                stb.append("     CLM.CLASSCD || '-' || CLM.SCHOOL_KIND IN (SELECT ");
                stb.append("                                                 NAME1 ");
                stb.append("                                             FROM ");
                stb.append("                                                 NAME_MST ");
                stb.append("                                             WHERE ");
                stb.append("                                                 NAMECD1 = 'E069' ");
                stb.append("                                             ) AND ");
                stb.append("     T3.SCHREGNO = '" + student._schregno + "' ");

                log.debug(" subClass sql =" + stb.toString());

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String className = rs.getString("CLASSNAME");
                    final String annual = rs.getString("ANNUAL");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String groupCd = rs.getString("SUBCLASSCD2");
                    final String year = rs.getString("YEAR");
                    final String credit = rs.getString("CREDITS");
                    final String valuation = rs.getString("VALUATION");

                    final String classcd = subclassCd.substring(0, 2);
                    if (!classList.containsKey(classcd)) {
                        classList.put(classcd, new Printclass(classcd, className));
                    }
                    final Printclass printclass = classList.get(classcd);

                    final String printSubclassKey = StringUtils.defaultString(groupCd, subclassCd) + "-" + annual + "-" + year + "-" + StringUtils.defaultString(credit);
                    if(!printclass._subclassList.containsKey(printSubclassKey)) {
                        printclass._subclassList.put(printSubclassKey, new Subclass(subclassCd, annual, subclassName, groupCd, year, credit));
                    }
                    student._score.put(printSubclassKey, valuation);
                }
            } catch (final SQLException e) {
                log.error("科目の基本情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        return classList;
    }

    public static <T, K> List<T> getMappedList(final Map<K, List<T>> map, final K key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<T>());
        }
        return map.get(key1);
    }

    private class Student {
        final String _schregno;
        final String _grade;
        final String _annual;
        final String _name;
        final String _name_Eng;
        final int _alphabetOrder; //アルファベット順
        final Map<String,String> _score =  new HashMap(); //評定
        final Map<String,String> _totalCredit =  new HashMap(); //単位
        final Map<String,String> _totalScore =  new HashMap(); //合計評定
        final Map<String,Integer> _scoreCnt =  new HashMap(); //評定数
        String _knje070Credit; //全教科修得単位数の計
        String _allCredit;
        String _allAvg; //全教科評定平均
        String _6Credit; //6教科修得単位数の計
        String _6Avg; //6教科評定平均
        String _sekiji; //席次
        int _sekijiOrder; //席次表示順
        final Map<String, String> _baseDetailMst =  new TreeMap(); //SCHREG_BASE_DETAIL_MST
        String _allAvgBunshi; //全教科評定平均 分子
        String _allAvgBunbo; //全教科評定平均 分母

        public Student(
            final String schregno, final String grade, final String annual, final String name, final String name_Eng, final int alphabetOrder) {
                _schregno = schregno;
                _grade = grade;
                _annual = annual;
                _name = name;
                _name_Eng = name_Eng;
                _alphabetOrder = alphabetOrder;
        }
    }

    private class Subclass {
        final String _subclassCd;
        final String _subclassName;
        final String _groupCd;
        final String _year;
        final String _annual;
        final String _credit;
        final List<Subclass> _subclassList = new ArrayList<Subclass>();

        public Subclass(
            final String subclassCd, final String annual, final String subclassName, final String groupCd, final String year, final String credit) {
                _subclassCd = subclassCd;
                _annual = annual;
                _subclassName = subclassName;
                _groupCd = groupCd;
                _year = year;
                _credit = credit;
        }
    }

    private class Printclass {
        final String _classCd;
        final String _className;
        final TreeMap<String,Subclass> _subclassList;

        public Printclass(
            final String classCd, final String className) {
                _classCd = classCd;
                _className = className;
                _subclassList = new TreeMap<String, Subclass>();
        }
    }


    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75174 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _sort; // 1:アルファベット順,2:席次順
        final String _schoolCd;
        final String _facultyCd;
        final String _facultyName;
        final String _departmentCd;
        final String _departmentName;
        final boolean _doushisyaFlg; //true:同志社大学、false:同志社女子大学
        final boolean _nameMstE069Check; //名称マスタE069存在チェック
        Map<String,Map<String,String>>  _classList = new HashMap();
        final String _documentroot;
        final Map _knje070Paramap;
        final boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("CTRL_SEME");
            _sort = request.getParameter("RADIO");
            _schoolCd = request.getParameter("SCHOOL_CD");
            _facultyCd = request.getParameter("FACULTY_CD");
            _departmentCd = request.getParameter("DEPARTMENT_CD");

            _doushisyaFlg =  "00003602".equals(_schoolCd);
            if (_doushisyaFlg) {
                log.info("加重平均出力");
            } else {
                log.info("単純平均出力");
            }
            _facultyName = getFacultyName(db2);
            _departmentName = getDepartmentName(db2);
            _nameMstE069Check = checkNameMstE069(db2);
            _documentroot = request.getParameter("DOCUMENTROOT");
            _knje070Paramap = new KNJE070().createParamMap(request);
            _isOutputDebug = "1".equals(KnjDbUtils.getDbPrginfoProperties(db2, "KNJE371D", "outputDebug"));
        }

        private String getDepartmentName(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT DEPARTMENTNAME from COLLEGE_DEPARTMENT_MST WHERE SCHOOL_CD = '" + _schoolCd + "' AND FACULTYCD = '" + _facultyCd + "' AND DEPARTMENTCD = '" + _departmentCd + "'"));
        }

        private String getFacultyName(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT FACULTYNAME from COLLEGE_FACULTY_MST WHERE SCHOOL_CD = '" + _schoolCd + "' AND FACULTYCD = '" + _facultyCd + "'"));
        }

        private boolean checkNameMstE069(final DB2UDB db2) {
            final String count = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT COUNT(*) FROM NAME_MST WHERE NAMECD1 = 'E069'"));
            return !"0".equals(count);
        }
    }
}

// eof

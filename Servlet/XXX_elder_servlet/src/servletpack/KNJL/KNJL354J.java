// kanji=漢字
/*
 * $Id: KNJL354J.java
 *
 * 作成日: 2009/01/29 18:00:00 - JST
 * 作成者: maesiro
 *
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

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
 *  特待生候補者リスト
 *  $Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $
 */
public class KNJL354J {
    static final Log log = LogFactory.getLog(KNJL354J.class);

    private Vrw32alp _svf;

    private DB2UDB db2;

    Param _param;
    
    private static final String TEST_NATIONAL_LANGUAGE="国語";
    private static final String TEST_MATHEMATICS="算数";
    private static final String TEST_SCIENCE="理科";
    private static final String TEST_SOCIETY="社会";
    private static final String TEST_ENGLISH="英語";
    private String[] _testSubclassOrder = null; // 試験科目の並び順

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        _param =  new Param(request);
        
        if (_param._testDiv.equals("5")) { // 帰国生
            _testSubclassOrder = new String[]{TEST_NATIONAL_LANGUAGE, TEST_MATHEMATICS, TEST_ENGLISH};
        } else { // 帰国生以外
            _testSubclassOrder = new String[]{TEST_NATIONAL_LANGUAGE, TEST_MATHEMATICS, TEST_SCIENCE, TEST_SOCIETY};
        }

        // 帳票初期化処理
        _svf = new Vrw32alp();
        _svf.VrInit();
        response.setContentType("application/pdf");
        _svf.VrSetSpoolFileStream(response.getOutputStream());
        _svf.VrSetForm("KNJL354J.frm", 4);

        // DB初期化処理
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return ;
        }

        // 帳票出力処理
        log.debug(">>入試区分=" + _param._testDiv);
        boolean hasData = printMain(db2, _param._testDiv);
        
        // 帳票終了処理
        if (!hasData) {
            _svf.VrSetForm("MES001.frm", 0);
            _svf.VrsOut("note", "note");
            _svf.VrEndPage();
        }

        final int ret = _svf.VrQuit();
        log.info("===> VrQuit():" + ret);

        db2.commit();
        db2.close();
    }
    
    /** 
     * 試験科目の並び順を返す
     * @param subclassName 試験科目名
     * @return 並び順
     */
    private Integer getTestSubclassShowOrder(String subclassName) {
        for(int i=0; i<_testSubclassOrder.length; i++) {
            if (_testSubclassOrder[i].equals(subclassName)) {
                return new Integer(i+1);
            }
        }
        return new Integer(0); // 受験する試験科目ではない
    }
    
    private boolean printMain(DB2UDB db2, String testDiv) {

        final List applicants = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean hasData = false;
        
        try {
            String sql=sqlApplicant(testDiv);
            log.debug("sqlEntexamDesireDats sql="+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String oldExamno = "";
            Applicant applicant1 = null;
            
            while (rs.next()) {
                if (!oldExamno.equals(rs.getString("EXAMNO"))) {
                    applicant1 = new Applicant(
                            rs.getString("APPLICANTDIV"),
                            rs.getString("TESTDIV"),
                            rs.getString("EXAM_TYPE"),
                            rs.getString("EXAMNO"),
                            rs.getString("NAME"),
                            rs.getString("NAME_KANA"),
                            rs.getString("SEX"),
                            rs.getString("SEX_NAME"),
                            rs.getString("RECEPTNO"),
                            rs.getString("TOTAL_SCORE"),
                            rs.getString("ADDRESS1"),
                            rs.getString("ADDRESS2"),
                            rs.getString("ZIPCD"),
                            rs.getString("TELNO"),
                            rs.getString("REMARK1"),
                            rs.getString("REMARK2")
                    );

                    applicants.add(applicant1);
                }
                
                if (rs.getString("SCORE") == null || rs.getString("LOWER_SCORE") == null){
                    log.debug(rs.getString("EXAMNO")+"...(recept="+rs.getString("RECEPTNO")+")"+rs.getString("TESTSUBCLASSCD")+" : "+rs.getString("TESTSUBCLASSNAME")+" : score="+rs.getString("SCORE")+", lower_score="+rs.getString("LOWER_SCORE"));
                    continue;
                }
                
                applicant1.addTestSubclass(new TestSubclass(
                        rs.getString("TESTSUBCLASSCD"),
                        rs.getString("TESTSUBCLASSNAME"),
                        Integer.valueOf(rs.getString("LOWER_SCORE")),
                        Integer.valueOf(rs.getString("SCORE")),
                        getTestSubclassShowOrder(rs.getString("TESTSUBCLASSNAME"))));
                
                oldExamno = rs.getString("EXAMNO");
            }

            // 得点画面から指定された各科目の最低点より、志願者の各試験科目のうちの一つでも低ければ
            // その志願者を出力するリストから省く
            boolean hasUnderLowScore = true;
            while(hasUnderLowScore) {
                hasUnderLowScore = false;
                for(int i=0; i<applicants.size(); i++ ) {
                    Applicant applicant = (Applicant) applicants.get(i);
                    
                    if (applicant._testSubclassList.size()==0 || applicant.hasLower()) {
                        applicants.remove(applicant);
                        hasUnderLowScore = true;
                        continue;
                    }
                }
            }
            
            
            // 特待生候補者出力
            final int DETAILS_MAX = 20; // 伝票明細件数MAX
            int i = 0; // １ページあたり件数
            int no = 0; // №

            int manNum = 0;
            int womanNum = 0;
            int totalNum = 0;

            int page = 0;
            int totalPage = 0;

            for (Iterator it = applicants.iterator(); it.hasNext();) {
                
                final Applicant applicant = (Applicant) it.next();
                
                if (hasData == false) {
                    printHeader(db2, testDiv);
                    hasData = true;
                }
                
                i++;
                no++;

                // 性別カウント
                if (applicant._sex.equals("1")) {
                    manNum++;
                } else {
                    womanNum++;
                }
                totalNum++;

                if (totalPage == 0) {
                    totalPage = getTotalPage(applicants, applicant._testDiv, DETAILS_MAX);
                }

                if (no%20==1) {
                    _svf.VrsOut("PAGE", String.valueOf(++page)); // ページ
                    _svf.VrsOut("TOTAL_PAGE", String.valueOf(totalPage)); // 総ページ
                }

                if (totalPage == page) {
                    _svf.VrsOut("NOTE",
                            "計" + (Integer.toString(totalNum)) + "名、"
                            + "男" + Integer.toString(manNum)+ "名、"
                            + "女" + Integer.toString(womanNum)+ "名");
                }

                _svf.VrsOut("NUMBER", String.valueOf(no)); // №
                _svf.VrsOut("EXAMNO", applicant._examNo); // 受験番号
                _svf.VrsOut((applicant._name != null && applicant._name.length() > 12) ? "NAME2" : "NAME1",  applicant._name);
                _svf.VrsOut("KANA", applicant._kana);
                _svf.VrsOut("SEX", applicant._sexName); // 性別 

                // 各試験科目得点
                for(int j=0; j<_testSubclassOrder.length; j++) {
                    Integer score = applicant.getSubclassTestScore(_testSubclassOrder[j]);
                    _svf.VrsOut("SCORE"+String.valueOf(j+1), (score == null) ? "" : score.toString());
                }
                
                _svf.VrsOut("TOTAL_SCORE", applicant._totalScore); // 総合得点
             
                String addressField = "";
                if (applicant._address.length() > 41) {
                    addressField = "ADDRESS3";
                } else if (applicant._address.length() > 25) {
                    addressField = "ADDRESS2";
                } else {
                    addressField = "ADDRESS1";
                }
                _svf.VrsOut(addressField, applicant._address); // 住所
                _svf.VrsOut("ZIPCODE", applicant._zipCd); // 郵便番号
                _svf.VrsOut("TELNO", applicant._telNo); // 電話番号
                _svf.VrsOut(applicant._remark.length() > 12 ? "REMARK2" : "REMARK1", applicant._remark); // 備考
                _svf.VrEndRecord();
                
                if (i >= DETAILS_MAX) {
                    i = 0;
                }
            }
            
        } catch (SQLException e) {
            log.error("svfMain error!", e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        
        return hasData;
    }

    private void printHeader(DB2UDB db2, String testDiv) {
        _svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "/" + "1/1") + "度"); // 年度
        String testDivName = _param.getNameMst(db2, "L004", testDiv);
        log.debug("testDiv="+testDiv+", testDiv name="+testDivName);
        _svf.VrsOut("TESTDIV", testDivName);        // 入試区分
        _svf.VrsOut("DATE", getJDate(_param._loginDate));
        _svf.VrsOut("CONDITION", "すべての科目" + _param._lowPercentage + "％以上");

        for(int i=0; i<_testSubclassOrder.length; i++) {
            _svf.VrsOut("SUBCLASS"+String.valueOf(i+1), _testSubclassOrder[i]);
        }
    }
    

    private static String getJDate(String date) {
        try {
            final Calendar cal = KNJServletUtils.parseDate(date);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int dom = cal.get(Calendar.DAY_OF_MONTH);
            
            return nao_package.KenjaProperties.gengou(year, month, dom);

        } catch (final Exception e) {
            return null;
        }
    }
    
    private int getTotalPage(List applicants, String testDiv, int max) {
        int cnt = 0;
        int totalPage = 0;

        for (Iterator it = applicants.iterator(); it.hasNext();) {
            final Applicant applicant = (Applicant) it.next();

            if (testDiv.equals(applicant._testDiv)) {
                cnt++;
            }
        }

        totalPage = cnt / max;
        if (cnt % max != 0) {
            totalPage++;
        }

        return totalPage;
    }

    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _prgrId;
        private final String _dbName;
        private final String _loginDate;
        private final String _testDiv;
        private final String _sort;
        private final String _lowPercentage; // 試験得点最低得点率
        private final double _lowPercentageValue; // 試験得点最低得点率

        public Param(HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _prgrId = request.getParameter("PRGID");
            _dbName = request.getParameter("DBNAME");
            _loginDate = request.getParameter("LOGIN_DATE");
            _testDiv = request.getParameter("TESTDIV");
            _sort = request.getParameter("SORT");
            _lowPercentage = request.getParameter("EACH_SCORE");
            _lowPercentageValue = Double.valueOf(_lowPercentage).doubleValue() / 100.0;
        }
        
        /**
         * 名称マスタのnamecd1とnamecd2でselectしたNAME1を返す
         * @param db2
         * @param namecd1 NAME_MST に指定する NAMECD1
         * @param namecd2 NAME_MST に指定する NAMECD2
         * @return NAME1 
         */
        private String getNameMst(DB2UDB db2, String namecd1,String namecd2) {
            
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME1 ");
            sql.append(" FROM NAME_MST ");
            sql.append(" WHERE NAMECD1 = '"+namecd1+"' AND NAMECD2 = '"+namecd2+"' ");
            String name = null;
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                   name = rs.getString("NAME1");
                }
            } catch(SQLException ex) {
                log.debug(ex);
            }
            
            return name;
        }
    }

    // ======================================================================
    class Applicant {
        private final String _applicantDiv;     // 入試制度
        private final String _testDiv;              // 入試区分
        private final String _examType;         // 受験型
        private final String _examNo;               // 受験番号
        private final String _name;             // 名前
        private final String _kana;             // かな
        private final String _sex;              // 性別
        private final String _sexName;          // 性別名称
        private final String _receptNo;         // 受付№
        private final String _totalScore;       // 総合点
        private final String _address;          // 住所
        private final String _zipCd;            // 郵便番号
        private final String _telNo;            // 電話番号
        private final String _remark;           // 備考
        
        private final List _testSubclassList; // 科目リスト
        
        Applicant(
                final String applicantDiv,
                final String testDiv,
                final String examType,
                final String examNo,
                final String name,
                final String kana,
                final String sex,
                final String sexName,
                final String receptNo,
                final String totalScore,
                final String address1,
                final String address2,
                final String zipCd,
                final String telNo,
                final String remark1,
                final String remark2
        ) {
            _applicantDiv = applicantDiv;
            _testDiv = testDiv;
            _examType = examType;
            _examNo = examNo;
            _name = name;
            _kana = kana;
            _sex = sex;
            _sexName = sexName;
            _receptNo = receptNo;
            _totalScore = totalScore;
            String address_1 = (address1!=null) ? address1 : "";
            String address_2 = (address2!=null) ? " " + address2 : "";
            _address = address_1 + address_2;
            _zipCd = zipCd;
            _telNo = telNo;
            String remark_1 = (remark1!=null) ? remark1 : "";
            String remark_2 = (remark2!=null) ? remark2 : "";
            _remark = remark_1 + remark_2;
            
            _testSubclassList = new ArrayList();
        }
        
        public void addTestSubclass(TestSubclass subclassTest) {
            _testSubclassList.add(subclassTest);
        }
        
        /**
         * 引数の試験科目名の試験得点を返す
         * @param subclassName 試験科目名
         * @return 試験得点
         */
        public Integer getSubclassTestScore(String subclassName) {
            for(Iterator it=_testSubclassList.iterator(); it.hasNext(); ) {
                TestSubclass ts = (TestSubclass) it.next();
                if (subclassName.equals(ts._subclassName)){ 
                    return ts._score;
                }
            }
            return null; // 受験している科目が見つからない
        }
        
        public String toString() {
            StringBuffer stb = new StringBuffer();
            stb.append("examNo="+_examNo+" (receptno="+_receptNo+")");
            for(Iterator it=_testSubclassList.iterator(); it.hasNext(); ) {
                TestSubclass ts = (TestSubclass) it.next();
                stb.append("["+ts._subclassName+", score="+ts._score+", lowerScore="+ts._lowerScore+"]");
            }
            return stb.toString();
        }
        
        // 指定された得点率より、低い得点率の科目があるかを返す
        public boolean hasLower() {
            for(Iterator it=_testSubclassList.iterator(); it.hasNext();) {
                TestSubclass ts = (TestSubclass) it.next();
                if (ts._score.intValue() < ts._lowerScore.intValue())
                    return true;
            }
            return false;
            
        }
        
    }

    private String sqlApplicant(String testDiv) {

        StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCORE AS ( SELECT");
        stb.append("    T1.ENTEXAMYEAR,");
        stb.append("    T1.APPLICANTDIV,");
        stb.append("    T1.TESTDIV,");
        stb.append("    T1.EXAM_TYPE,");
        stb.append("    T1.EXAMNO,");
        stb.append("    T1.RECEPTNO,");
        stb.append("    T3.TESTSUBCLASSCD,");
        stb.append("    T4.NAME1 AS TESTSUBCLASSNAME,");
        stb.append("    T5.PERFECT,");
        stb.append("    CEIL(T5.PERFECT * "+_param._lowPercentageValue+") AS LOWER_SCORE,"); // 満点 x 最低得点率
        stb.append("    T3.SCORE");
        stb.append("    FROM ENTEXAM_RECEPT_DAT T1 ");
        stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT T3 ON ");
        stb.append("        T3.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("        AND T3.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("        AND T3.TESTDIV = T1.TESTDIV");
        stb.append("        AND T3.EXAM_TYPE = T1.EXAM_TYPE");
        stb.append("        AND T3.RECEPTNO = T1.RECEPTNO");
        stb.append("    LEFT JOIN NAME_MST T4 ON ");
        stb.append("        T4.NAMECD1 = 'L009'");
        stb.append("        AND T4.NAMECD2 = T3.TESTSUBCLASSCD");
        stb.append("    INNER JOIN ENTEXAM_PERFECT_MST T5 ON ");
        stb.append("        T5.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("        AND T5.TESTDIV = T1.TESTDIV");
        stb.append("        AND T5.TESTSUBCLASSCD = T3.TESTSUBCLASSCD");
        stb.append("    WHERE ");
        stb.append("        T1.ENTEXAMYEAR = '" + _param._year + "'");
        stb.append("        AND T1.TESTDIV = '"+testDiv+"'");
        stb.append(" ) SELECT");
        stb.append("    T1.ENTEXAMYEAR,");
        stb.append("    T1.APPLICANTDIV,");
        stb.append("    T1.TESTDIV,");
        stb.append("    T1.EXAM_TYPE,");
        stb.append("    T1.EXAMNO,");
        stb.append("    T5.NAME,");
        stb.append("    T5.NAME_KANA,");
        stb.append("    T5.SEX,");
        stb.append("    T6.ABBV1 AS SEX_NAME,");
        stb.append("    T1.RECEPTNO,");
        stb.append("    T3.TESTSUBCLASSCD,");
        stb.append("    T3.TESTSUBCLASSNAME,");
        stb.append("    T3.LOWER_SCORE,");
        stb.append("    T3.SCORE,");
        stb.append("    T1.TOTAL4 AS TOTAL_SCORE,");
        stb.append("    T7.ADDRESS1,");
        stb.append("    T7.ADDRESS2,");
        stb.append("    T7.ZIPCD,");
        stb.append("    T7.GTELNO AS TELNO,");
        stb.append("    T5.REMARK1,");
        stb.append("    T5.REMARK2");
        stb.append(" FROM");
        stb.append("    ENTEXAM_RECEPT_DAT T1 ");
        stb.append("    LEFT JOIN T_SCORE T3 ON ");
        stb.append("        T3.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("        AND T3.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("        AND T3.TESTDIV = T1.TESTDIV");
        stb.append("        AND T3.EXAM_TYPE = T1.EXAM_TYPE");
        stb.append("        AND T3.EXAMNO = T1.EXAMNO");
        stb.append("        AND T3.RECEPTNO = T1.RECEPTNO");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T5 ON ");
        stb.append("        T5.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("        AND T5.TESTDIV" + testDiv + " IS NOT NULL");
        stb.append("        AND T5.EXAMNO = T1.EXAMNO");
        stb.append("    LEFT JOIN NAME_MST T6 ON");
        stb.append("        T6.NAMECD1 = 'Z002'");
        stb.append("        AND T6.NAMECD2 = T5.SEX");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T7 ON ");
        stb.append("        T7.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("        AND T7.EXAMNO = T1.EXAMNO");
        stb.append(" WHERE");
        stb.append("    T1.ENTEXAMYEAR = '" + _param._year + "'");
        stb.append("    AND T1.TESTDIV = '"+testDiv+"'");
        
        stb.append(" ORDER BY"); 
        if (_param._sort.equals("1")) { // ソート条件
            stb.append("    T1.TOTAL4 DESC,");
        }
        stb.append("    T1.EXAMNO");

        return stb.toString();
    }
    
    private class TestSubclass {
        private final String _subclassCd;
        private final String _subclassName;
        private final Integer _lowerScore;
        private final Integer _score;
        private final Integer _showOrder;
        TestSubclass(
                final String subclassCd,
                final String subclassName,
                final Integer lowerScore,
                final Integer score,
                final Integer showOrder) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _lowerScore = lowerScore;
            _score = score;
            _showOrder = showOrder;
        }
        
        
    }

} // KNJL354J

// eof

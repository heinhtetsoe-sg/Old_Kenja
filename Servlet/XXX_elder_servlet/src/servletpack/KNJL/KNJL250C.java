package servletpack.KNJL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ２５０Ｃ＞  プレテスト得点成績一覧
 **/
public class KNJL250C {

    private static final Log log = LogFactory.getLog(KNJL250C.class);
    private Param _param;

    private final String SORT_SEISEKI = "1";
    private final String SORT_TOTAL2 = "1";
    private final String SORT_TOTAL4 = "2";
    private final String SORT_TOTAL3 = "3";
    private final String SORT_JUKENNO = "2";
    private final String FRM_NAME = "KNJL250C.frm";
    private final String FRM_NAME_GOJO = "KNJL250C_GOJO.frm";

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) {

        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }
        log.fatal("$Revision: 70283 $ $Date: 2019-10-18 12:25:47 +0900 (金, 18 10 2019) $"); // CVSキーワードの取り扱いに注意
        _param = new Param(db2, request);

        //  print設定
        response.setContentType("application/pdf");

        //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ

        //SQL作成
        try {
            //  svf設定
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

            //SVF出力
            if (setSvfMain(db2, svf)) {
                nonedata = true;
            }
            if (!nonedata) { //  該当データ無し
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("DB2 prepareStatement set error!", ex);
        } finally {
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();
        }

    }//doGetの括り

    private static int getMS932ByteCount(final String str) {
        if (null == str) return 0;
        int ret = 0;
        try {
            ret = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return ret;
    }

    private String getDateString(final DB2UDB db2, final String date) {
        if (null == date) {
            return null;
        }
        return _param._seirekiFlg ? date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date) : KNJ_EditDate.h_format_JP(db2, date) ;
    }

    /**
     *  svf print 印刷処理
     */
    private boolean setSvfMain(
            DB2UDB db2,
            Vrw32alp svf)
    {
        //表紙
        svf.VrSetForm("KNJLCVR002C.frm", 1);
        svf.VrsOut("NENDO", _param.getNendo(db2));
        svf.VrsOut("APPLICANTDIV", _param._applicantdivname);
        svf.VrsOut("PRE_TESTDIV", _param._preTestDivName);
        svf.VrsOut("TITLE", "【" + _param.getTitle() + "】");
        svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));
        svf.VrEndPage();

        //成績一覧
        if (_param.isGojo()) {
            svf.VrSetForm(FRM_NAME_GOJO, 4);
        } else {
            svf.VrSetForm(FRM_NAME, 4);
        }
        svf.VrsOut("NENDO", _param.getNendo(db2));
        svf.VrsOut("APPLICANTDIV", _param._applicantdivname);
        svf.VrsOut("TITLE", _param.getTitle());
        svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));
        svf.VrsOut("DATE", getDateString(db2, _param._loginDate));

        int subcnt = 1;
        final List testSubList = getTestSubList(db2);
        for (final Iterator itSub = testSubList.iterator(); itSub.hasNext();) {
            final TestSub testSub = (TestSub) itSub.next();

            if (_param.isGojo()) {
                svf.VrsOut("SUBCLASS" + String.valueOf(subcnt), testSub._name);
            } else {
                svf.VrsOut("SUBCLASS" + String.valueOf(subcnt) + (getMS932ByteCount(testSub._name) > 8 ? "_3" : getMS932ByteCount(testSub._name) > 6 ? "_2" : ""), testSub._name);
            }
            subcnt++;
        }

        boolean nonedata = false;

        final List cntAvgList = getCntAvgList(db2);
        final List list = getExamineeList(db2);
        final int LINE_MAX = 50;
        final String pageMax = String.valueOf(list.size() / LINE_MAX + (list.size() % LINE_MAX == 0 ? 0 : 1));
        int c = 0;
        int sexCountAll = 0;
        int sexCount1 = 0;
        int sexCount2 = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final PretestExaminee e = (PretestExaminee) it.next();

            c += 1;
            svf.VrsOut("PAGE", String.valueOf(c / LINE_MAX + (c % LINE_MAX == 0 ? 0 : 1)));
            svf.VrsOut("TOTAL_PAGE", pageMax);

            svf.VrsOut("NUMBER", String.valueOf(c));
            svf.VrsOut("EXAMNO", e._preReceptno);
            svf.VrsOut("EXAMCOURSE_NAME1", e._preExamTypeName);
            if (_param._output) {
                svf.VrsOut("NAME" + (getMS932ByteCount(e._name) > 20 ? "2" : "1"), e._name);
            }
            svf.VrsOut("SEX", e._sexname);
            svf.VrsOut("PREF", e._finschoolPref);
            if (_param.isGojo()) {
                svf.VrsOut("FINSCHOOL" + (getMS932ByteCount(e._finschoolName) > 36 ? "3" : getMS932ByteCount(e._finschoolName) > 22 ? "2" : "1"), e._finschoolName);
            } else {
                svf.VrsOut("FINSCHOOL" + (getMS932ByteCount(e._finschoolName) > 26 ? "2" : "1"), e._finschoolName);
            }
            //各科目
            subcnt = 1;
            for (final Iterator itSub = testSubList.iterator(); itSub.hasNext();) {
                final TestSub testSub = (TestSub) itSub.next();
                final TestScore testScore = (TestScore) e._testScoreMap.get(testSub._cd);

                String score = null;
                //未受験科目
                if (null != testScore) {
                    score = "1".equals(testScore._attendFlg) ? testScore._score : "*";
                }
                svf.VrsOut("SCORE" + String.valueOf(subcnt), score);
                subcnt++;
            }
            //２科目
            svf.VrsOut("TWO_TOTAL", null == e._total2 ? "*" : e._total2);
            svf.VrsOut("TWO_AVE"  , null == e._total2 ? "*" : e._avarage2);
            svf.VrsOut("TWO_RANK" , null == e._total2 ? "*" : e._rank2);
            //３科目は、１型の受験者のみ出力
            if (_param.isCollege() && "1".equals(e._preExamType)) {
                svf.VrsOut("THREE_TOTAL", null == e._total4 ? "*" : e._total4);
                svf.VrsOut("THREE_AVE"  , null == e._total4 ? "*" : e._avarage4);
                svf.VrsOut("THREE_RANK" , null == e._total4 ? "*" : e._rank4);
            }
            //４教科と国算英は、２型の受験者のみ出力
            if (_param.isGojo() && "2".equals(e._preExamType)) {
                svf.VrsOut("THREE_TOTAL", null == e._total4 ? "*" : e._total4);
                svf.VrsOut("THREE_AVE"  , null == e._total4 ? "*" : e._avarage4);
                svf.VrsOut("THREE_RANK" , null == e._total4 ? "*" : e._rank4);
                svf.VrsOut("FOUR_TOTAL", null == e._total3 ? "*" : e._total3);
                svf.VrsOut("FOUR_AVE"  , null == e._total3 ? "*" : e._avarage3);
                svf.VrsOut("FOUR_RANK" , null == e._total3 ? "*" : e._rank3);
            }
            svf.VrsOut("EXAMNO2", e._recomExamno);
            svf.VrsOut("JUKU" + (getMS932ByteCount(e._prischoolName) > 35 ? "3" : getMS932ByteCount(e._prischoolName) > 20 ? "2" : ""), e._prischoolName);
            svf.VrsOut("CONNECT", "1".equals(e._psContact) ? "○" : "");
            svf.VrsOut("REMARK" + (getMS932ByteCount(e._remark) > 24 ? "2" : "1"), e._remark);

            if ("1".equals(e._sex)) {
                sexCount1 += 1;
            }
            if ("2".equals(e._sex)) {
                sexCount2 += 1;
            }
            sexCountAll += 1;
            svf.VrEndRecord();
            if (c % LINE_MAX == 0 && c != list.size()) {
                svf.VrEndRecord();
                svf.VrEndRecord();
            }
            if (c == list.size()) {
                svf.VrsOut("NOTE", "男"+String.valueOf(sexCount1)+"名,女"+String.valueOf(sexCount2)+"名,合計"+String.valueOf(sexCountAll)+"名");
                for (int i = (c % LINE_MAX == 0 ? LINE_MAX : c % LINE_MAX); i < LINE_MAX; i++) {
                    svf.VrEndRecord();
                }
                setSvfCnt(svf, cntAvgList, testSubList);
                svf.VrEndRecord();
                setSvfAvg(svf, cntAvgList, testSubList);
                svf.VrEndRecord();
            }
            nonedata = true;
        }
        return nonedata;
    }

    private void setSvfCnt(Vrw32alp svf, final List cntAvgList, final List testSubList) {
        int subcnt = 1;
        for (final Iterator it = cntAvgList.iterator(); it.hasNext();) {
            final CntAvg cntAvg = (CntAvg) it.next();

            if ("S".equals(cntAvg._div)) {
                for (final Iterator itSub = testSubList.iterator(); itSub.hasNext();) {
                    final TestSub testSub = (TestSub) itSub.next();
                    if (testSub._cd.equals(cntAvg._cd)) {
                        svf.VrsOut("SCORE" + String.valueOf(subcnt), cntAvg._cnt);
                        subcnt++;
                    }
                }
            } else if ("2".equals(cntAvg._cd)) {
                if ("T".equals(cntAvg._div)) svf.VrsOut("TWO_TOTAL", cntAvg._cnt);
                if ("A".equals(cntAvg._div)) svf.VrsOut("TWO_AVE", cntAvg._cnt);
            } else if ("3".equals(cntAvg._cd)) {
                if ("T".equals(cntAvg._div)) svf.VrsOut("THREE_TOTAL", cntAvg._cnt);
                if ("A".equals(cntAvg._div)) svf.VrsOut("THREE_AVE", cntAvg._cnt);
            } else if ("4".equals(cntAvg._cd)) {
                if ("T".equals(cntAvg._div)) svf.VrsOut("FOUR_TOTAL", cntAvg._cnt);
                if ("A".equals(cntAvg._div)) svf.VrsOut("FOUR_AVE", cntAvg._cnt);
            }
        }
    }

    private void setSvfAvg(Vrw32alp svf, final List cntAvgList, final List testSubList) {
        int subcnt = 1;
        for (final Iterator it = cntAvgList.iterator(); it.hasNext();) {
            final CntAvg cntAvg = (CntAvg) it.next();

            if ("S".equals(cntAvg._div)) {
                for (final Iterator itSub = testSubList.iterator(); itSub.hasNext();) {
                    final TestSub testSub = (TestSub) itSub.next();
                    if (testSub._cd.equals(cntAvg._cd)) {
                        svf.VrsOut("SCORE" + String.valueOf(subcnt), cntAvg._avg);
                        subcnt++;
                    }
                }
            } else if ("2".equals(cntAvg._cd)) {
                if ("T".equals(cntAvg._div)) svf.VrsOut("TWO_TOTAL", cntAvg._avg);
                if ("A".equals(cntAvg._div)) svf.VrsOut("TWO_AVE", cntAvg._avg);
            } else if ("3".equals(cntAvg._cd)) {
                if ("T".equals(cntAvg._div)) svf.VrsOut("THREE_TOTAL", cntAvg._avg);
                if ("A".equals(cntAvg._div)) svf.VrsOut("THREE_AVE", cntAvg._avg);
            } else if ("4".equals(cntAvg._cd)) {
                if ("T".equals(cntAvg._div)) svf.VrsOut("FOUR_TOTAL", cntAvg._avg);
                if ("A".equals(cntAvg._div)) svf.VrsOut("FOUR_AVE", cntAvg._avg);
            }
        }
    }

    private List getExamineeList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            final String sql = sqlEntexamApplicantBasePreDat();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String preReceptno = rs.getString("PRE_RECEPTNO");
                final String recomExamno = rs.getString("RECOM_EXAMNO");
                final String preExamType = rs.getString("PRE_EXAM_TYPE");
                final String preExamTypeName = rs.getString("PRE_EXAM_TYPE_NAME");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String sexname = rs.getString("SEX_NAME");
                final String total2 = rs.getString("TOTAL2");
                final String avarage2 = rs.getString("AVARAGE2");
                final String rank2 = rs.getString("RANK2");
                final String total4 = rs.getString("TOTAL4");
                final String avarage4 = rs.getString("AVARAGE4");
                final String rank4 = rs.getString("RANK4");
                final String total3 = rs.getString("TOTAL3");
                final String avarage3 = rs.getString("AVARAGE3");
                final String rank3 = rs.getString("RANK3");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String finschoolPref = rs.getString("FINSCHOOL_PREF");
                final String psContact = rs.getString("PS_CONTACT");
                final String prischoolName = rs.getString("PRISCHOOL_NAME");
                final String remark = rs.getString("REMARK");

                final PretestExaminee examinee = new PretestExaminee(preReceptno, recomExamno, preExamType, preExamTypeName, name, sex, sexname, total2, avarage2, rank2, total4, avarage4, rank4, total3, avarage3, rank3, finschoolName, finschoolPref, psContact, prischoolName, remark);
                examinee.setTestScoreMap(db2);
                list.add(examinee);
            }

        } catch (Exception ex) {
            log.error("setSvfMain set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String sqlEntexamApplicantBasePreDat() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.PRE_RECEPTNO, ");
        stb.append("     T1.RECOM_EXAMNO, ");
        stb.append("     T1.PRE_EXAM_TYPE, ");
        stb.append("     NML105.NAME1 AS PRE_EXAM_TYPE_NAME, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.SEX, ");
        stb.append("     NMZ002.ABBV1 AS SEX_NAME, ");
        stb.append("     T2.TOTAL2, ");
        stb.append("     T2.AVARAGE2, ");
        if (SORT_SEISEKI.equals(_param._sort) && (SORT_TOTAL4.equals(_param._goukei) || SORT_TOTAL3.equals(_param._goukei))) {
            stb.append("     T2.DIV_RANK2 AS RANK2, ");
        } else {
            stb.append("     T2.TOTAL_RANK2 AS RANK2, ");
        }
        if (_param.isGojo()) {
            stb.append("     CASE WHEN T1.PRE_EXAM_TYPE = '2' THEN T2.TOTAL4 END AS TOTAL4, ");
            stb.append("     CASE WHEN T1.PRE_EXAM_TYPE = '2' THEN T2.AVARAGE4 END AS AVARAGE4, ");
            stb.append("     CASE WHEN T1.PRE_EXAM_TYPE = '2' THEN T2.DIV_RANK4 END AS RANK4, ");
            stb.append("     CASE WHEN T1.PRE_EXAM_TYPE = '2' THEN T2.TOTAL3 END AS TOTAL3, ");
            stb.append("     CASE WHEN T1.PRE_EXAM_TYPE = '2' THEN T2.AVARAGE3 END AS AVARAGE3, ");
            stb.append("     CASE WHEN T1.PRE_EXAM_TYPE = '2' THEN T2.DIV_RANK3 END AS RANK3, ");
        } else {
            stb.append("     CASE WHEN T1.PRE_EXAM_TYPE = '1' THEN T2.TOTAL4 END AS TOTAL4, ");
            stb.append("     CASE WHEN T1.PRE_EXAM_TYPE = '1' THEN T2.AVARAGE4 END AS AVARAGE4, ");
            stb.append("     CASE WHEN T1.PRE_EXAM_TYPE = '1' THEN T2.DIV_RANK4 END AS RANK4, ");
            stb.append("     CASE WHEN T1.PRE_EXAM_TYPE = '1' THEN T2.TOTAL3 END AS TOTAL3, "); //ダミー
            stb.append("     CASE WHEN T1.PRE_EXAM_TYPE = '1' THEN T2.AVARAGE3 END AS AVARAGE3, "); //ダミー
            stb.append("     CASE WHEN T1.PRE_EXAM_TYPE = '1' THEN T2.DIV_RANK3 END AS RANK3, "); //ダミー
        }
        stb.append("     T1.FS_CD, ");
        stb.append("     VALUE(L1.FINSCHOOL_NAME_ABBV, L1.FINSCHOOL_NAME) AS FINSCHOOL_NAME, ");
        stb.append("     T1.ZIPCD, ");
        stb.append("     L3.PREF AS FINSCHOOL_PREF, ");
        stb.append("     T1.PS_CONTACT, ");
        stb.append("     T1.PS_CD, ");
        stb.append("     L2.PRISCHOOL_NAME, ");
        stb.append("     T1.REMARK ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_PRE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_PRE_DAT T2 ");
        stb.append("         ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T2.PRE_RECEPTNO = T1.PRE_RECEPTNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST L1 ON L1.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     LEFT JOIN PRISCHOOL_MST L2 ON L2.PRISCHOOLCD = T1.PS_CD ");
        stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T1.SEX ");
        stb.append("     LEFT JOIN NAME_MST NML105 ON NML105.NAMECD1 = 'L105' AND NML105.NAMECD2 = T1.PRE_EXAM_TYPE ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             NEW_ZIPCD, ");
        stb.append("             PREF ");
        stb.append("         FROM ");
        stb.append("             ZIPCD_MST ");
        stb.append("         GROUP BY ");
        stb.append("             NEW_ZIPCD, ");
        stb.append("             PREF ");
        stb.append("         ) L3 ON L3.NEW_ZIPCD = T1.ZIPCD ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND T1.PRE_TESTDIV  = '" + _param._preTestDiv + "' ");
        if (SORT_SEISEKI.equals(_param._sort) && (SORT_TOTAL4.equals(_param._goukei) || SORT_TOTAL3.equals(_param._goukei))) {
            if (_param.isGojo()) {
                stb.append("     AND T1.PRE_EXAM_TYPE = '2' ");
            } else {
                stb.append("     AND T1.PRE_EXAM_TYPE = '1' ");
            }
        }
        stb.append(" ORDER BY ");
        if (SORT_SEISEKI.equals(_param._sort)) {
            if (SORT_TOTAL2.equals(_param._goukei)) {
                stb.append("     VALUE(T2.TOTAL2, -1) DESC, ");
            } else if (SORT_TOTAL3.equals(_param._goukei)) {
                stb.append("     VALUE(T2.TOTAL3, -1) DESC, ");
            } else {
                stb.append("     VALUE(T2.TOTAL4, -1) DESC, ");
            }
        }
        stb.append("     T1.PRE_RECEPTNO ");
        return stb.toString();
    }

    private class PretestExaminee {
        final String _preReceptno;
        final String _recomExamno;
        final String _preExamType;
        final String _preExamTypeName;
        final String _name;
        final String _sex;
        final String _sexname;
        final String _total2;
        final String _avarage2;
        final String _rank2;
        final String _total4;
        final String _avarage4;
        final String _rank4;
        final String _total3;
        final String _avarage3;
        final String _rank3;
        final String _finschoolName;
        final String _finschoolPref;
        final String _psContact;
        final String _prischoolName;
        final String _remark;
        final Map _testScoreMap;

        PretestExaminee(
                final String preReceptno,
                final String recomExamno,
                final String preExamType,
                final String preExamTypeName,
                final String name,
                final String sex,
                final String sexname,
                final String total2,
                final String avarage2,
                final String rank2,
                final String total4,
                final String avarage4,
                final String rank4,
                final String total3,
                final String avarage3,
                final String rank3,
                final String finschoolName,
                final String finschoolPref,
                final String psContact,
                final String prischoolName,
                final String remark
        ) {
            _preReceptno = preReceptno;
            _recomExamno = recomExamno;
            _preExamType = preExamType;
            _preExamTypeName = preExamTypeName;
            _name = name;
            _sex = sex;
            _sexname = sexname;
            _total2 = total2;
            _avarage2 = avarage2;
            _rank2 = rank2;
            _total4 = total4;
            _avarage4 = avarage4;
            _rank4 = rank4;
            _total3 = total3;
            _avarage3 = avarage3;
            _rank3 = rank3;
            _finschoolName = finschoolName;
            _finschoolPref = finschoolPref;
            _psContact = psContact;
            _prischoolName = prischoolName;
            _remark = remark;
            _testScoreMap = new HashMap();
        }

        private void setTestScoreMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getTestScoreSql();
                //log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd = rs.getString("TESTSUBCLASSCD");
                    final String attendFlg = rs.getString("ATTEND_FLG");
                    final String score = rs.getString("SCORE");

                    final TestScore testScore = new TestScore(cd, attendFlg, score);
                    _testScoreMap.put(cd, testScore);
                }

            } catch (Exception ex) {
                log.error("getTestScoreSql error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getTestScoreSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTSUBCLASSCD, ");
            stb.append("     ATTEND_FLG, ");
            stb.append("     SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_PRE_DAT ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND APPLICANTDIV = '" + _param._applicantdiv + "' ");
            stb.append("     AND PRE_RECEPTNO = '" + _preReceptno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     TESTSUBCLASSCD ");
            return stb.toString();
        }
    }

    private class TestScore {
        final String _cd;
        final String _attendFlg;
        final String _score;

        TestScore(
                final String cd,
                final String attendFlg,
                final String score
        ) {
            _cd = cd;
            _attendFlg = attendFlg;
            _score = score;
        }
    }

    private List getTestSubList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            final String sql = getTestSubSql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String cd = rs.getString("SUBCLASSCD");
                final String name = rs.getString("SUBCLASSNAME");

                final TestSub testSub = new TestSub(cd, name);
                list.add(testSub);
            }

        } catch (Exception ex) {
            log.error("getTestSubSql error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String getTestSubSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     NAMECD2 AS SUBCLASSCD, ");
        stb.append("     NAME1 AS SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("     V_NAME_MST ");
        stb.append(" WHERE ");
        stb.append("         YEAR    = '" + _param._year + "' ");
        stb.append("     AND NAMECD1 = 'L109' ");
        stb.append("     AND ABBV3   = '" + _param._preTestDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     NAMECD2 ");
        return stb.toString();
    }

    private class TestSub {
        final String _cd;
        final String _name;

        TestSub(
                final String cd,
                final String name
        ) {
            _cd = cd;
            _name = name;
        }
    }

    private List getCntAvgList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            final String sql = getCntAvgSql();
            log.debug(" getCntAvgSql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String div = rs.getString("DIV");
                final String cd = rs.getString("TESTSUBCLASSCD");
                final String cnt = rs.getString("CNT");
                final String avg = rs.getString("AVG");

                final CntAvg cntAvg = new CntAvg(div, cd, cnt, avg);
                list.add(cntAvg);
            }

        } catch (Exception ex) {
            log.error("getCntAvgSql error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String getCntAvgSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_RECEPT AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.PRE_RECEPTNO, ");
        stb.append("         T1.PRE_EXAM_TYPE, ");
        stb.append("         T2.TOTAL2, ");
        stb.append("         T2.AVARAGE2, ");
        if (_param.isGojo()) {
            stb.append("         CASE WHEN T1.PRE_EXAM_TYPE = '2' THEN T2.TOTAL3 END AS TOTAL3, ");
            stb.append("         CASE WHEN T1.PRE_EXAM_TYPE = '2' THEN T2.AVARAGE3 END AS AVARAGE3, ");
            stb.append("         CASE WHEN T1.PRE_EXAM_TYPE = '2' THEN T2.TOTAL4 END AS TOTAL4, ");
            stb.append("         CASE WHEN T1.PRE_EXAM_TYPE = '2' THEN T2.AVARAGE4 END AS AVARAGE4 ");
        } else {
            stb.append("         CASE WHEN T1.PRE_EXAM_TYPE = '1' THEN T2.TOTAL3 END AS TOTAL3, "); //ダミー
            stb.append("         CASE WHEN T1.PRE_EXAM_TYPE = '1' THEN T2.AVARAGE3 END AS AVARAGE3, "); //ダミー
            stb.append("         CASE WHEN T1.PRE_EXAM_TYPE = '1' THEN T2.TOTAL4 END AS TOTAL4, ");
            stb.append("         CASE WHEN T1.PRE_EXAM_TYPE = '1' THEN T2.AVARAGE4 END AS AVARAGE4 ");
        }
        stb.append("     FROM ");
        stb.append("         ENTEXAM_APPLICANTBASE_PRE_DAT T1 ");
        stb.append("         LEFT JOIN ENTEXAM_RECEPT_PRE_DAT T2 ");
        stb.append("             ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("             AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("             AND T2.PRE_RECEPTNO = T1.PRE_RECEPTNO ");
        stb.append("     WHERE ");
        stb.append("         T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("         AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("         AND T1.PRE_TESTDIV  = '" + _param._preTestDiv + "' ");
        if (SORT_SEISEKI.equals(_param._sort) && (SORT_TOTAL4.equals(_param._goukei) || SORT_TOTAL3.equals(_param._goukei))) {
            if (_param.isGojo()) {
                stb.append("         AND T1.PRE_EXAM_TYPE = '2' ");
            } else {
                stb.append("         AND T1.PRE_EXAM_TYPE = '1' ");
            }
        }
        stb.append("     ) ");

        stb.append(" , T_SCORE AS ( ");
        stb.append("     SELECT ");
        stb.append("         PRE_RECEPTNO, ");
        stb.append("         TESTSUBCLASSCD, ");
        stb.append("         SCORE ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_SCORE_PRE_DAT ");
        stb.append("     WHERE ");
        stb.append("         ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("         AND APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     'S' AS DIV, ");
        stb.append("     T2.TESTSUBCLASSCD, ");
        stb.append("     COUNT(T2.SCORE) AS CNT, ");
        stb.append("     DECIMAL(ROUND(AVG(FLOAT(T2.SCORE)),1),5,1) AS AVG ");
        stb.append(" FROM ");
        stb.append("     T_RECEPT T1 ");
        stb.append("     LEFT JOIN T_SCORE T2 ON T2.PRE_RECEPTNO = T1.PRE_RECEPTNO ");
        stb.append(" GROUP BY ");
        stb.append("     T2.TESTSUBCLASSCD ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     'T' AS DIV, ");
        stb.append("     '2' AS TESTSUBCLASSCD, ");
        stb.append("     COUNT(T1.TOTAL2) AS CNT, ");
        stb.append("     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL2)),1),5,1) AS AVG ");
        stb.append(" FROM ");
        stb.append("     T_RECEPT T1 ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     'A' AS DIV, ");
        stb.append("     '2' AS TESTSUBCLASSCD, ");
        stb.append("     COUNT(T1.AVARAGE2) AS CNT, ");
        stb.append("     DECIMAL(ROUND(AVG(FLOAT(T1.AVARAGE2)),1),5,1) AS AVG ");
        stb.append(" FROM ");
        stb.append("     T_RECEPT T1 ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     'T' AS DIV, ");
        stb.append("     '3' AS TESTSUBCLASSCD, ");
        stb.append("     COUNT(T1.TOTAL4) AS CNT, ");
        stb.append("     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL4)),1),5,1) AS AVG ");
        stb.append(" FROM ");
        stb.append("     T_RECEPT T1 ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     'A' AS DIV, ");
        stb.append("     '3' AS TESTSUBCLASSCD, ");
        stb.append("     COUNT(T1.AVARAGE4) AS CNT, ");
        stb.append("     DECIMAL(ROUND(AVG(FLOAT(T1.AVARAGE4)),1),5,1) AS AVG ");
        stb.append(" FROM ");
        stb.append("     T_RECEPT T1 ");
        if (_param.isGojo()) {
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     'T' AS DIV, ");
            stb.append("     '4' AS TESTSUBCLASSCD, ");
            stb.append("     COUNT(T1.TOTAL3) AS CNT, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(T1.TOTAL3)),1),5,1) AS AVG ");
            stb.append(" FROM ");
            stb.append("     T_RECEPT T1 ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     'A' AS DIV, ");
            stb.append("     '4' AS TESTSUBCLASSCD, ");
            stb.append("     COUNT(T1.AVARAGE3) AS CNT, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(T1.AVARAGE3)),1),5,1) AS AVG ");
            stb.append(" FROM ");
            stb.append("     T_RECEPT T1 ");
        }
        return stb.toString();
    }

    private class CntAvg {
        final String _div;
        final String _cd;
        final String _cnt;
        final String _avg;

        CntAvg(
                final String div,
                final String cd,
                final String cnt,
                final String avg
        ) {
            _div = div;
            _cd = cd;
            _cnt = cnt;
            _avg = avg;
        }
    }

    private class Param {
        final String _year;
        final String _applicantdiv;
        final String _applicantdivname;
        final String _loginDate;
        final boolean _seirekiFlg;
        final String _preTestDiv;
        final String _preTestDivName;
        final String _sort;
        final String _goukei;
        final boolean _output;
        private final String _z010SchoolCode;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _applicantdiv = "1";
            _applicantdivname = getNameMst(db2, "L103", _applicantdiv);
            _loginDate = request.getParameter("LOGIN_DATE");
            _preTestDiv = request.getParameter("PRE_TESTDIV");
            _preTestDivName = getNameMst(db2, "L104", _preTestDiv);
            _sort = request.getParameter("SORT");
            _goukei = request.getParameter("GOKEI");
            _output = !"1".equals(request.getParameter("OUTPUT"));
            // 西暦使用フラグ
            _seirekiFlg = getSeirekiFlg(db2);
            _z010SchoolCode = getSchoolCode(db2);
        }

        public String getNendo(final DB2UDB db2) {
            return _seirekiFlg ? _year + "年度" : KNJ_EditDate.h_format_JP_N(db2, _year + "-01-01") + "度";
        }

        private String getTitle() {
            if (SORT_SEISEKI.equals(_sort)) {
                if (isGojo()) {
                    if (SORT_TOTAL2.equals(_goukei)) {
                        return "成績一覧（国算理合計成績順）";
                    } else if (SORT_TOTAL4.equals(_goukei)) {
                        return "成績一覧（４教科合計成績順）";
                    } else {
                        return "成績一覧（国算英合計成績順）";
                    }
                } else {
                    if (SORT_TOTAL2.equals(_goukei)) {
                        return "成績一覧（２教科合計成績順）";
                    } else {
                        return "成績一覧（３教科合計成績順）";
                    }
                }
            } else {
                    return "成績一覧（受験番号順）";
            }
        }

        private boolean getSeirekiFlg(final DB2UDB db2) {
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return seirekiFlg;
        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {

            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME1 ");
            sql.append(" FROM NAME_MST ");
            sql.append(" WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
            String name = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try{
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                   name = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

        /*
         * 年度と入試制度から学校名を返す
         */
        private String getSchoolName(DB2UDB db2) {
            String certifKindCd = null;
            if ("1".equals(_applicantdiv)) certifKindCd = "105";
            if ("2".equals(_applicantdiv)) certifKindCd = "106";
            if (certifKindCd == null) return null;

            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME ");
            sql.append(" FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '"+_year+"' AND CERTIF_KINDCD = '"+certifKindCd+"' ");
            String name = null;
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                   name = rs.getString("SCHOOL_NAME");
                }
            } catch (SQLException ex) {
                log.error(ex);
            }

            return name;
        }

        private String getSchoolCode(DB2UDB db2) {
            String schoolCode = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   CASE WHEN NAME1 = 'CHIBEN' THEN NAME2 ELSE NULL END AS SCHOOLCODE ");
                stb.append(" FROM ");
                stb.append("   NAME_MST T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.NAMECD1 = 'Z010' ");
                stb.append("   AND T1.NAMECD2 = '00' ");

                PreparedStatement ps = db2.prepareStatement(stb.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    schoolCode = rs.getString("SCHOOLCODE");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSchoolCode Exception", e);
            }
            return schoolCode;
        }

        boolean isGojo() {
            return "30290053001".equals(_z010SchoolCode);
        }

        boolean isCollege() {
            return "30290086001".equals(_z010SchoolCode);
        }

    }
}//クラスの括り

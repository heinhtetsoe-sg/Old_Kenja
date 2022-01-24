package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL335Q {

    private static final Log log = LogFactory.getLog(KNJL335Q.class);

    private final SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final DecimalFormat _df02 = new DecimalFormat("00");

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        log.fatal("$Revision: 64692 $ $Date: 2019-01-16 19:15:02 +0900 (水, 16 1 2019) $"); // CVSキーワードの取り扱いに注意

        //print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //svf設定
        svf.VrInit();    //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        try {
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            printMain(db2, svf);
        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            //終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();       //DBを閉じる
            outstrm.close();   //ストリームを閉じる
        }
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        for (int i = 0; i < _param._testdivList.size(); i++) {
            final Testdiv testdiv = (Testdiv) _param._testdivList.get(i);

            log.info(" testdiv = " + testdiv._testdiv);

            testdiv.setData(db2, _param);

            if (testdiv._applicantList.size() == 0) {
                log.info("対象データ 0 : testdiv = " + testdiv._testdiv);
                continue;
            }

            printHyoshi(testdiv, svf);
            printMeibo(testdiv, svf);
        }
    }

    private void printHyoshi(final Testdiv testdiv, final Vrw32alp svf) {

        String form = null;
        boolean kotyoSuisen = false;
        if ("1".equals(_param._outputSelect)) {
            kotyoSuisen = "2".equals(_param._testdiv0) && "3".equals(testdiv._testdiv);
            if (kotyoSuisen) {
                form = "KNJL335Q_1_1.frm";
            } else {
                form = "KNJL335Q_1_2.frm";
            }
        } else if ("2".equals(_param._outputSelect)) {
            form = "KNJL335Q_1_3.frm";
        } else {
            return;
        }

        svf.VrSetForm(form, 1);

        svf.VrsOut("NENDO", StringUtils.defaultString(_param._entexamyear) + "年度"); // 年度
        if ("1".equals(_param._outputSelect)) {
            svf.VrsOut("TITLE", String.valueOf(testdiv._testAbbv1) + " 入学試験"); // タイトル
            svf.VrsOut("SUBTITLE", "志願者クラス別名簿"); // サブタイトル
        } else if ("2".equals(_param._outputSelect)) {
            svf.VrsOut("TITLE", "入学試験 ＆ 基準試験名簿"); // タイトル
            if ("1".equals(_param._taisyou)) {
                svf.VrsOut("SUBTITLE", "試験区分別"); // サブタイトル
            } else if ("2".equals(_param._taisyou)) {
                svf.VrsOut("SUBTITLE", "音順"); // サブタイトル
            }
        }
        svf.VrsOut("DATE", StringUtils.defaultString(_param._loginDate).replace('-', '/') + "版"); // 日付
        svf.VrsOut("EXAM_DATE", testdiv._jisshiHidukeString); // 試験日

        if ("1".equals(_param._outputSelect)) {
            int total = 0;
            final List applicantCourseList = Applicant.groupByCourse(testdiv._applicantList);
            for (int i = 0; i < applicantCourseList.size(); i++) {
                final List applicantList = (List) applicantCourseList.get(i);
                final Applicant appl = (Applicant) applicantList.get(0);

                svf.VrsOut("COURSE_NAME" + String.valueOf(i + 1), appl._dai1CourseName); // コース名
                svf.VrsOut("COURSE_NUM" + String.valueOf(i + 1), String.valueOf(applicantList.size())); // 人数
                total += applicantList.size();
            }
            svf.VrsOut("TOTAL", String.valueOf(total)); // 人数

            if (kotyoSuisen) {
                // TODO:
                svf.VrsOut("HOPE_NUM1", null); // 人数
                svf.VrsOut("HOPE_NUM2", null); // 人数
                svf.VrsOut("HOPE_NUM3", null); // 人数
                svf.VrsOut("HOPE_NUM4", null); // 人数
            }

        } else if ("2".equals(_param._outputSelect)) {

            int total = 0;
            final List applicantTestdivList = Applicant.groupByTestdiv(testdiv._applicantList);

            for (int i = 0; i < applicantTestdivList.size(); i++) {
                final List applicantList = (List) applicantTestdivList.get(i);
                final Applicant appl = (Applicant) applicantList.get(0);
//                if ("1".equals(appl._testdiv0)) {
//                    // 海外入試は対象外
//                    continue;
//                }

                String name = StringUtils.defaultString(appl._testdivName1);
                if (getMS932Bytecount(name) > 8 && name.endsWith("入試")) {
                    name = name.substring(0, name.indexOf("入試"));
                }
                svf.VrsOut("EXAM_DIV" + String.valueOf(i + 1), name); // 人数
                svf.VrsOut("HOPE_NUM" + String.valueOf(i + 1), String.valueOf(applicantList.size()) + " 名"); // 人数
                total += applicantList.size();
            }
//            svf.VrsOut("HOPE_NUM1", null); // 人数
//            svf.VrsOut("HOPE_NUM2", null); // 人数
//            svf.VrsOut("HOPE_NUM3", null); // 人数
//            svf.VrsOut("HOPE_NUM4", null); // 人数
//            svf.VrsOut("HOPE_NUM5", null); // 人数
            svf.VrsOut("TOTAL", String.valueOf(total) + " 名"); // 人数

        }
//        svf.VrsOut("SCHOOL_NAME", null); // 学校名

        svf.VrEndPage();
    }

    private void printMeibo(final Testdiv testdiv, final Vrw32alp svf) {
        final String form;
        final int maxLine;
        if ("1".equals(_param._outputSelect)) {
            form = "KNJL335Q_2_1.frm";
            maxLine = 40;
        } else if ("2".equals(_param._outputSelect)) {
            form = "KNJL335Q_2_2.frm";
            maxLine = 40;
        } else {
            return;
        }

        if ("1".equals(_param._outputSelect)) {
            // コース毎に改ページして出力
            int totalPage = 0;
            final List applicantCourseList = Applicant.groupByCourse(testdiv._applicantList);
            for (int crsi = 0; crsi < applicantCourseList.size(); crsi++) {
                final List applicantAllList = (List) applicantCourseList.get(crsi);

                final List pageList = getPageList(applicantAllList, maxLine);
                totalPage += pageList.size();
            }
            int page = 0;
            for (int crsi = 0; crsi < applicantCourseList.size(); crsi++) {
                final List applicantAllList = (List) applicantCourseList.get(crsi);

                final List pageList = getPageList(applicantAllList, maxLine);

                for (int pi = 0; pi < pageList.size(); pi++) {

                    final List applicantList = (List) pageList.get(pi);

                    svf.VrSetForm(form, 4);
                    page += 1;

//                    svf.VrsOut("SUBTITLE", null); // サブタイトル
//                    svf.VrsOut("DATE", null); // 日付
                    svf.VrsOut("PAGE1", String.valueOf(page)); // ページ
                    svf.VrsOut("PAGE2", String.valueOf(totalPage)); // ページ

                    for (int i = 0; i < applicantList.size(); i++) {
                        final Applicant appl = (Applicant) applicantList.get(i);

                        svf.VrsOut("TITLE", _param._entexamyear + "年度　" + StringUtils.defaultString(testdiv._testname) + "　" + StringUtils.defaultString(appl._dai1CourseName)); // タイトル

                        svf.VrsOut("NO", String.valueOf(pi * maxLine + i + 1)); // 連番
                        svf.VrsOut("EXAM_NO", appl._examno); // 受験番号
                        svf.VrsOut("NAME", appl._name); // 氏名
                        svf.VrsOut("SEX", appl._sexName); // 性別
                        svf.VrsOut("BIRTHDAY", formatBirthday(appl._birthday)); // 生年月日
                        svf.VrsOut("FINSCHOOL_NAME", appl._finschoolName); // 出身中学名
                        svf.VrsOut("PREF_NAME", appl._prefName); // 県名
                        svf.VrsOut("KANA", appl._nameKana); // ふりがな名
                        svf.VrsOut("DIV", appl._testdivName1); // 区分
                        svf.VrEndRecord();
                        _hasData = true;
                    }
                }
            }
        } else if ("2".equals(_param._outputSelect)) {
            int page = 0;
            int totalPage = 0;
            List kaiPageList = Collections.EMPTY_LIST;
            if ("1".equals(_param._taisyou)) {
                kaiPageList = Applicant.groupByTestdiv(testdiv._applicantList);
            } else if ("2".equals(_param._taisyou)) {
                final List dummyNameKana = new ArrayList();
                dummyNameKana.addAll(testdiv._applicantList);
                kaiPageList = new ArrayList();
                kaiPageList.add(dummyNameKana);
            }

            for (int crsi = 0; crsi < kaiPageList.size(); crsi++) {
                final List applicantAllList = (List) kaiPageList.get(crsi);

                final List pageList = getPageList(applicantAllList, maxLine);
                totalPage += pageList.size();
            }

            for (int crsi = 0; crsi < kaiPageList.size(); crsi++) {
                final List applicantAllList = (List) kaiPageList.get(crsi);

                final List pageList = getPageList(applicantAllList, maxLine);

                for (int pi = 0; pi < pageList.size(); pi++) {

                    final List applicantList = (List) pageList.get(pi);

                    svf.VrSetForm(form, 4);
                    page += 1;

//                    svf.VrsOut("SUBTITLE", null); // サブタイトル
//                    svf.VrsOut("DATE", null); // 日付
                    svf.VrsOut("PAGE1", String.valueOf(page)); // ページ
                    svf.VrsOut("PAGE2", String.valueOf(totalPage)); // ページ

                    for (int i = 0; i < applicantList.size(); i++) {
                        final Applicant appl = (Applicant) applicantList.get(i);

                        if ("1".equals(_param._taisyou)) {
                            svf.VrsOut("TITLE", _param._entexamyear + "年度　" + StringUtils.defaultString(testdiv._testname) + "　" + StringUtils.defaultString(appl._testdivName1)); // タイトル
                        } else if ("2".equals(_param._taisyou)) {
                            svf.VrsOut("TITLE", _param._entexamyear + "年度　" + StringUtils.defaultString(testdiv._testname) + "　基準試験"); // タイトル
                        }

                        svf.VrsOut("NO", String.valueOf(pi * maxLine + i + 1)); // 連番
                        svf.VrsOut("EXAM_NO", appl._examno); // 受験番号
                        svf.VrsOut("NAME", appl._name); // 氏名
                        svf.VrsOut("SEX", appl._sexName); // 性別
                        svf.VrsOut("BIRTHDAY", formatBirthday(appl._birthday)); // 生年月日
                        svf.VrsOut("FINSCHOOL_NAME", appl._finschoolName); // 出身中学名
                        svf.VrsOut("PREF_NAME", appl._prefName); // 県名
                        svf.VrsOut("TEL_NO", appl._telno); // 電話番号
                        svf.VrsOut("PLACE_NAME", appl._examhallName); // 会場
//                            svf.VrsOut("METHOD", null); // 方式
                        svf.VrsOut("CLASS", appl._dai1CourseName); // クラス
                        svf.VrsOut("DIV", appl._testdivName1); // 区分
                        svf.VrEndRecord();
                        _hasData = true;
                    }
                }
            }
        }
    }

    private String formatBirthday(final String birthday) {
        final StringBuffer stb = new StringBuffer();
        if (null != birthday) {
            try {
                final Date d = _dateFormat.parse(birthday);
                final Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                stb.append(_df02.format(cal.get(Calendar.YEAR) % 100));
                stb.append(_df02.format(cal.get(Calendar.MONTH) + 1));
                stb.append(_df02.format(cal.get(Calendar.DAY_OF_MONTH)));
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return stb.toString();
    }

    private static class Testdiv {
        String _testdiv;
        String _testname;
        String _testAbbv1;
        String _jisshiHiduke;
        String _jisshiHidukeString;
        List _applicantList = Collections.EMPTY_LIST;

        public void setData(final DB2UDB db2, final Param param) {
            _applicantList = Applicant.getApplicantList(db2, param, this);
        }
    }

    private static class Applicant {
        final String _examno;
        final String _name;
        final String _nameKana;
        final String _sex;
        final String _sexName;
        final String _birthday;
        final String _telno;
        final String _fsCd;
        final String _finschoolName;
        final String _prefName;
        final String _testdiv0;
        final String _testdiv;
//        final String _testdiv0Abbv1;
//        final String _testdiv0Name1;
//        final String _testdivAbbv1;
        final String _testdivName1;
        final String _dai1Course;
        final String _dai1CourseName;
        final String _judgement;
        final String _judgementName;
        final String _examhallcd;
        final String _examhallName;

        Applicant(
            final String examno,
            final String name,
            final String nameKana,
            final String sex,
            final String sexName,
            final String birthday,
            final String telno,
            final String fsCd,
            final String finschoolName,
            final String prefName,
            final String testdiv0,
            final String testdiv,
            final String testdiv0Abbv1,
            final String testdiv0Name1,
            final String testdivAbbv1,
            final String testdivName1,
            final String dai1Course,
            final String dai1CourseName,
            final String judgement,
            final String judgementName,
            final String examhallcd,
            final String examhallName
        ) {
            _examno = examno;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _sexName = sexName;
            _birthday = birthday;
            _telno = telno;
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _prefName = prefName;
            _testdiv0 = testdiv0;
            _testdiv = testdiv;
//            _testdiv0Abbv1 = testdiv0Abbv1;
//            _testdiv0Name1 = testdiv0Name1;
//            _testdivAbbv1 = testdivAbbv1;
            _testdivName1 = testdivName1;
            _dai1Course = dai1Course;
            _dai1CourseName = dai1CourseName;
            _judgement = judgement;
            _judgementName = judgementName;
            _examhallcd = examhallcd;
            _examhallName = examhallName;
        }

        private static class TestdivComparator implements Comparator {
            public int compare(final Object o1, final Object o2) {
                final Applicant a1 = (Applicant) o1;
                final Applicant a2 = (Applicant) o2;
                if (null != a1._testdiv && null != a2._testdiv) {
                    return a1._testdiv.compareTo(a2._testdiv);
                } else if (null != a1._testdiv) {
                    return -1;
                } else if (null != a2._testdiv) {
                    return 1;
                }
                return 0;
            }
        }

        private static List groupByTestdiv(final List applicantList0) {
            final List rtn = new ArrayList();
            final List applicantList = new ArrayList(applicantList0);
            Collections.sort(applicantList, new Applicant.TestdivComparator());
            List current = null;
            String testdivOld = null;
            for (final Iterator it = applicantList.iterator(); it.hasNext();) {
                final Applicant appl = (Applicant) it.next();
                if (null == current || null != appl._testdiv && !appl._testdiv.equals(testdivOld)){
                    current = new ArrayList();
                    rtn.add(current);
                }
                current.add(appl);
                testdivOld = appl._testdiv;
            }
            return rtn;
        }

        private static List groupByCourse(final List applicantList) {
            final List rtn = new ArrayList();
            List current = null;
            String courseOld = null;
            for (final Iterator it = applicantList.iterator(); it.hasNext();) {
                final Applicant appl = (Applicant) it.next();
                if (null == current || null != appl._dai1Course && !appl._dai1Course.equals(courseOld)){
                    current = new ArrayList();
                    rtn.add(current);
                }
                current.add(appl);
                courseOld = appl._dai1Course;
            }
            return rtn;
        }

        public static List getApplicantList(final DB2UDB db2, final Param param, final Testdiv testdiv_) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List noReceptDat = new ArrayList();
            try {
                final String sql = sql(param, testdiv_);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    if (null == rs.getString("RECEPT_DAT_RECEPTNO")) {
                        noReceptDat.add(examno);
                        continue;
                    }

                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String sex = rs.getString("SEX");
                    final String sexName = rs.getString("SEX_NAME");
                    final String birthday = rs.getString("BIRTHDAY");
                    final String telno = rs.getString("TELNO");
                    final String fsCd = rs.getString("FS_CD");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String prefName = rs.getString("PREF_NAME");
                    final String testdiv = rs.getString("TESTDIV");
                    final String testdiv0 = rs.getString("TESTDIV0");
                    final String testdiv0Abbv1 = rs.getString("TESTDIV0_ABBV1");
                    final String testdiv0Name1 = rs.getString("TESTDIV0_NAME1");
                    final String testdivAbbv1 = rs.getString("TESTDIV_ABBV1");
                    final String testdivName1 = rs.getString("TESTDIV_NAME1");
                    final String dai1Course = rs.getString("DAI1_COURSE");
                    final String dai1CourseName = rs.getString("DAI1_COURSE_NAME");
                    final String judgement = rs.getString("JUDGEMENT");
                    final String judgementName = rs.getString("JUDGEMENT_NAME");
                    final String examhallcd = rs.getString("EXAMHALLCD");
                    final String examhallName = rs.getString("EXAMHALL_NAME");
                    final Applicant applicant = new Applicant(examno, name, nameKana, sex, sexName, birthday, telno, fsCd, finschoolName, prefName, testdiv0, testdiv, testdiv0Abbv1, testdiv0Name1, testdivAbbv1, testdivName1, dai1Course, dai1CourseName, judgement, judgementName, examhallcd, examhallName);
                    list.add(applicant);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.info(" recept_datがない : examno = " + noReceptDat);
            return list;
        }

        private static String sql(final Param param, final Testdiv testdiv) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("      B1.EXAMNO, ");
            stb.append("      B1.NAME, ");
            stb.append("      B1.NAME_KANA, ");
            stb.append("      B1.SEX, ");
            stb.append("      Z002.NAME2 AS SEX_NAME, ");
            stb.append("      B1.BIRTHDAY,  ");
            stb.append("      TADDR.TELNO,  ");
            stb.append("      B1.FS_CD,  ");
            stb.append("      TFIN.FINSCHOOL_NAME, ");
            stb.append("      PREF.PREF_NAME,  ");
            stb.append("      B1.TESTDIV0,  ");
            stb.append("      B1.TESTDIV,  ");
            stb.append("      N1.ABBV1 AS TESTDIV0_ABBV1,  ");
            stb.append("      N1.NAME1 AS TESTDIV0_NAME1,  ");
            stb.append("      L004.ABBV1 AS TESTDIV_ABBV1,  ");
            stb.append("      L004.NAME1 AS TESTDIV_NAME1,  ");
            stb.append("      B1.DAI1_COURSECD || B1.DAI1_MAJORCD || B1.DAI1_COURSECODE AS DAI1_COURSE,  ");
            stb.append("      C1.EXAMCOURSE_NAME AS DAI1_COURSE_NAME, ");
            stb.append("      B1.JUDGEMENT, ");
            stb.append("      L013.NAME1 AS JUDGEMENT_NAME, ");
            stb.append("      HALLY.EXAMHALLCD, ");
            stb.append("      HALLY.EXAMHALL_NAME, ");
            stb.append("      TREC.RECEPTNO AS RECEPT_DAT_RECEPTNO ");
            stb.append("  FROM  ");
            stb.append("      V_ENTEXAM_APPLICANTBASE_DAT B1  ");
            stb.append("      LEFT JOIN ENTEXAM_APPLICANTADDR_DAT A1 ON A1.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
            stb.append("              AND A1.APPLICANTDIV = B1.APPLICANTDIV  ");
            stb.append("              AND A1.EXAMNO = B1.EXAMNO  ");
            stb.append("      LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
            stb.append("              AND C1.APPLICANTDIV = B1.APPLICANTDIV  ");
            stb.append("              AND C1.TESTDIV = B1.TESTDIV  ");
            stb.append("              AND C1.COURSECD = B1.DAI1_COURSECD  ");
            stb.append("              AND C1.MAJORCD = B1.DAI1_MAJORCD  ");
            stb.append("              AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE  ");
            stb.append("      LEFT JOIN ENTEXAM_APPLICANTADDR_DAT TADDR ON TADDR.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("         AND TADDR.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("         AND TADDR.EXAMNO = B1.EXAMNO ");
            stb.append("      LEFT JOIN FINSCHOOL_MST TFIN ON TFIN.FINSCHOOLCD = B1.FS_CD ");
            stb.append("      LEFT  JOIN PREF_MST PREF ON TFIN.FINSCHOOL_PREF_CD = PREF.PREF_CD ");
            stb.append("      LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L045' AND N1.NAMECD2 = B1.TESTDIV0  ");
            stb.append("      LEFT JOIN V_NAME_MST L013 ON L013.YEAR = B1.ENTEXAMYEAR AND L013.NAMECD1 = 'L013' AND L013.NAMECD2 = B1.JUDGEMENT  ");
            stb.append("      LEFT JOIN V_NAME_MST Z002 ON Z002.YEAR = B1.ENTEXAMYEAR AND Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = B1.SEX  ");
            stb.append("      LEFT JOIN V_NAME_MST L004 ON L004.YEAR = B1.ENTEXAMYEAR AND L004.NAMECD1 = 'L004' AND L004.NAMECD2 = B1.TESTDIV  ");
            stb.append("      LEFT JOIN (SELECT DISTINCT ENTEXAMYEAR, APPlICANTDIV, EXAMNO, RECEPTNO ");
            stb.append("                 FROM ENTEXAM_RECEPT_DAT ");
            if ("1".equals(param._outputSelect)) {
                stb.append("      WHERE TESTDIV = '" + testdiv._testdiv + "'  ");
            } else if ("2".equals(param._outputSelect)) {
                stb.append("      WHERE TESTDIV = '5'  ");
            }
            stb.append("                ) TREC ON TREC.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("                AND TREC.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("                AND TREC.EXAMNO = B1.EXAMNO ");
            stb.append("      LEFT JOIN ENTEXAM_HALL_YDAT HALLY ON HALLY.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("          AND HALLY.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("          AND HALLY.TESTDIV = B1.TESTDIV ");
            stb.append("          AND TREC.RECEPTNO BETWEEN HALLY.S_RECEPTNO AND HALLY.E_RECEPTNO ");
            stb.append("  WHERE  ");
            stb.append("      B1.ENTEXAMYEAR = '" + param._entexamyear + "'  ");
            stb.append("      AND B1.APPLICANTDIV = '" + param._applicantdiv + "'  ");
            if ("1".equals(param._outputSelect)) {
                stb.append("      AND B1.TESTDIV0 = '" + param._testdiv0 + "'  ");
                stb.append("  ORDER BY  ");
                stb.append("      B1.TESTDIV  ");
                stb.append("      , DAI1_COURSE  ");
                stb.append("      , B1.EXAMNO  ");

            } else if ("2".equals(param._outputSelect)) {
                if ("1".equals(param._taisyou)) {
                    stb.append("  ORDER BY  ");
                    stb.append("      B1.TESTDIV  ");
                    stb.append("      , B1.EXAMNO  ");
                } else if ("2".equals(param._taisyou)) {
                    stb.append("  ORDER BY  ");
                    stb.append("      B1.NAME_KANA  ");
                }
            }
            return stb.toString();
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _outputSelect; // 1:入学試験 2:入学試験&基準試験名簿
        final String _testdiv0;
        final String _loginDate;
        final String _taisyou;
        final boolean _seirekiFlg;
        final List _testdivList;

        private String _principalName;
        private String _jobName;
        private String _schoolName;
        private String _printPrincpalName;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _outputSelect = request.getParameter("OUTPUT_SELECT");
            if ("2".equals(_outputSelect)) {
                _testdiv0 = "3"; // 一般入試
            } else {
                _testdiv0 = request.getParameter("TESTDIV");
            }
            _loginDate    = request.getParameter("LOGIN_DATE");
            _taisyou   = request.getParameter("TAISYOU");
            _seirekiFlg = getSeirekiFlg(db2);
            setCertifSchoolDat(db2);

            _testdivList = getTestdivList(db2);
        }

        /* 西暦表示にするのかのフラグ  */
        private boolean getSeirekiFlg(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ";
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
        }

        /* 入試区分 */
        private List getTestdivList(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT T1.*, DAYOFWEEK(DATE(REPLACE(T1.NAMESPARE1, '/', '-'))) AS YOUBI_ID FROM V_NAME_MST T1 ";
            sql += " WHERE YEAR = '" + _entexamyear + "' ";
            sql += "   AND NAMECD1 = 'L004' ";
            sql += "   AND ABBV3 = '" + _testdiv0 +"' ";
            sql += " ORDER BY NAMECD2 ";
            List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                log.debug(" testdiv sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Testdiv t = new Testdiv();
                    t._testdiv = rs.getString("NAMECD2");
                    t._testname = rs.getString("NAME1");
                    t._testAbbv1 = rs.getString("ABBV1");
                    t._jisshiHiduke = StringUtils.replace(rs.getString("NAMESPARE1"), "/", "-");
                    final String youbiId = rs.getString("YOUBI_ID");
                    if (NumberUtils.isDigits(youbiId)) {
                        final String youbi = new String[] {"", "日", "月", "火", "水", "木", "金", "土"}[Integer.parseInt(youbiId)];
                        t._jisshiHidukeString = StringUtils.defaultString(rs.getString("NAMESPARE1")) + "(" + youbi + ")実施";
                    }
                    list.add(t);
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private String gethiduke(final String inputDate) {
            // 西暦か和暦はフラグで判断
            String date;
            if (null != inputDate) {
                if (_seirekiFlg) {
                    date = inputDate.toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(inputDate);
                } else {
                    date = KNJ_EditDate.h_format_JP(inputDate);
                }
                return date;
            }
            return null;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindCd;
//            if (_applicantdiv.equals(APPLICANTDIV1)) {
//                certifKindCd = "105";
//            } else {
                certifKindCd = "106";
//            }

            final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '" + certifKindCd + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _jobName = rs.getString("JOB_NAME");
                    _schoolName = rs.getString("SCHOOL_NAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _jobName = trim(_jobName);
            _principalName = trim(_principalName);
            _printPrincpalName = (StringUtils.isEmpty(_jobName) ? "" : StringUtils.defaultString(_jobName) + "　") + StringUtils.defaultString(_principalName);
        }

        private static String trim(final String s) {
            if (null == s) {
                return s;
            }
            int st = 0, ed = s.length();
            for (int i = 0; i < s.length(); i++) {
                final char ch = s.charAt(i);
                if (ch == ' ' || ch == '　') {
                    st = i + 1;
                } else {
                    break;
                }
            }
            for (int i = s.length() - 1; i >= 0; i--) {
                final char ch = s.charAt(i);
                if (ch == ' ' || ch == '　') {
                    ed = i;
                } else {
                    break;
                }
            }
            if (st < ed) {
                return s.substring(st, ed);
            }
            return s;
        }
    }
}//クラスの括り

// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2020/03/15
 * 作成者: Nutec
 *
 */
package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [成績管理]
 *
 *                  ＜ＫＮＪⅮ１５７Ｂ＞  成績個人票（全考査）
 */
public class KNJD157B {
    private static final Log log = LogFactory.getLog(KNJD157B.class);

    private boolean _nonedata = false;//該当データなしフラグ
    private KNJServletpacksvfANDdb2 _sd = new KNJServletpacksvfANDdb2();
    private Param _param = null;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {
        final Vrw32alp svf = new Vrw32alp();//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;//Databaseクラスを継承したクラス

        PrintWriter outstrm = null;
        try {
            //  print設定
            response.setContentType("application/pdf");
            outstrm = new PrintWriter (response.getOutputStream());

            //  svf設定
            svf.VrInit();                                          //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());  //PDFファイル名の設定

            _sd.setSvfInit(request, response, svf);
            db2 = _sd.setDb(request);
            if (_sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }

            _param = getParam(db2, request);

            printSvfMain(db2, svf);
        } catch (Exception e) {
            log.error("svf_out exception!", e);
        } finally {
            //  該当データ無し
            if (!_nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();    //DBを閉じる
            outstrm.close();//ストリームを閉じる
        }
    }//doGetの括り

    private void setForm(final Vrw32alp svf, final String form, final int data) {
        svf.VrSetForm(form, data);
        if (_param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }
    private static int toInt(final String s, final int def) {
        if (!NumberUtils.isDigits(s)) {
            return def;
        }
        return Integer.parseInt(s);
    }

    /** 帳票出力 **/
    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql   = null;
        int MAX_TEST_ITEM  = 6;//テスト種別最大数値

        String title = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度 素点個人票 (全考査)";
        String hrName = "";
        String name = "";
        String notice = "";
        final String SONZAI_TRUE = "1";
        try {
            setForm(svf, "KNJD157B.xml", 1);
            for (int i = 0; i < _param._schregnos.length; i++) {
                String schregno = "";
                schregno = _param._schregnos[i];

                //生徒情報
                sql = sqlGetHeaderData(schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                //タイトル
                svf.VrsOut("TITLE", title);
                if ("1".equals(_param._type) == true) {
                    notice = "平均点と順位は講座で算出しています";
                    svf.VrsOut("NOTICE", notice);
                } else if ("2".equals(_param._type) == true) {
                    notice = "平均点と順位は類型で算出しています";
                    svf.VrsOut("NOTICE", notice);
                }

                //生徒基本情報
                while (rs.next()) {
                    hrName = rs.getString("GRADE_NAME2") + rs.getString("HR_CLASS_NAME1") + toInt(rs.getString("ATTENDNO"), 0) + "番";
                    name = rs.getString("NAME");
                    svf.VrsOut("HR_NAME", hrName);
                    svf.VrsOut("NAME", name);
                }

                //受講科目名情報
                sql = sqlgetSubclassData(schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ArrayList subjectList      = new ArrayList();
                ArrayList subjectNameList = new ArrayList();
                int count = 1;

                while (rs.next()) {
                    if (rs.getString("SUBCLASSCD") != null && count <= 20) {
                        subjectList.add(rs.getString("SUBCLASSCD"));
                        subjectNameList.add(rs.getString("SUBCLASSABBV"));

                        {
                            final int subClassAbbvLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("SUBCLASSABBV"));
                            final String subClassAbbvField = (subClassAbbvLen <= 6)? "1": "2";
                            svf.VrsOutn("SUBCLASS_NAME" + subClassAbbvField, count, rs.getString("SUBCLASSABBV"));
                        }
                        count++;
                    }
                }

                //平均点、クラス順位、学年順位
                sql = sqlgetSkiCd(schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ArrayList skiList = new ArrayList();
                ArrayList semesterList = new ArrayList();

                while (rs.next()) {
                    skiList.add(rs.getString("SEMESTER") + rs.getString("TESTKINDCD") + rs.getString("TESTITEMCD"));
                    semesterList.add(rs.getString("SEMESTER"));
                }

                //受講科目素点・平均点・講座順位又は類型グループ順位
                sql = sqlGetRankData(schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ArrayList totalAveList = new ArrayList();
                ArrayList classAveRankList = new ArrayList();
                ArrayList gradeAveRankList = new ArrayList();
                ArrayList skiCheckList = new ArrayList();
                ArrayList sonzaiList = new ArrayList();
                while (rs.next()) {
                    for (int j = 0; j < skiList.size(); j++) {
                        if (((String)skiList.get(j)).equals(rs.getString("SKICD")) == true) {
                            skiCheckList.add(rs.getString("SKICD"));
                            if (rs.getInt("SEMESTER")  <= Integer.parseInt(_param._semester)) {
                                if (SONZAI_TRUE.equals(rs.getString("SONZAI_FLG"))) {
                                    totalAveList.add(RoundHalfUp(rs.getString("AVG")));
                                    classAveRankList.add(rs.getString("CLASS_AVG_RANK"));
                                    gradeAveRankList.add(rs.getString("GRADE_AVG_RANK"));
                                } else {
                                    totalAveList.add(null);
                                    classAveRankList.add(null);
                                    gradeAveRankList.add(null);
                                }
                                sonzaiList.add(rs.getString("SONZAI_FLG"));
                            }
                        }
                    }
                }

                //受講科目素点・平均点・講座順位又は類型グループ順位
                sql = sqlScoreData(schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                count = 1;
                int testItemCount = 1;   //テスト種別・順位・平均点(講座・類型)・クラス順位・学年順位の表示する段数
                int scoreCount = 1;      //素点・平均点・順位(講座・類型)の表示する段数
                int skiCount = 0;        //skiListの要素番号
                int countAgeAndRank = 0;//totalAveList・classAveRankList・gradeAveRankListの要素番号
                int countPrintAgeAndRank = 0;  //実際に印字に使用するcountAgeAndRank
                                                   //考査数と登録されている平均点等の数が違う場合、countAgeAndRankを使用すると
                                                   //IndexOutOfBoundsExceptionが発生するため、元の処理をあまり変更せずに追加
                String  selectName = "";
                String selectNameType = "";

                if ("1".equals(_param._type) == true) {
                    selectName = "講座順位";
                    selectNameType = "_1";
                } else if ("2".equals(_param._type) == true) {
                    selectName ="類型グループ順位";
                    selectNameType = "_2";
                }

                while (rs.next()) {
                    //一回目
                    if (testItemCount == 1) {
                        //テスト種別・順位名
                        if (rs.getString("TESTITEMNAME") != null) {
                            svf.VrsOut("TESTITEMNAME" + testItemCount, rs.getString("TESTITEMNAME"));
                        }
                        svf.VrsOut("SELECT_NAME" + testItemCount + selectNameType, selectName);

                        //平均点・クラス順位・学年順位
                        if (skiList.size() != skiCheckList.size()) {
                            break;
                        }
                        if (((String)skiList.get(countAgeAndRank)).equals(skiCheckList.get(countAgeAndRank)) == true) {
                            if (Integer.parseInt((String)semesterList.get(countAgeAndRank)) <= Integer.parseInt(_param._semester)) {
                                //平均点・クラス順位・学年順位が存在するものだけ印字
                                if (SONZAI_TRUE.equals(sonzaiList.get(countAgeAndRank))) {
                                    printAgeAndRank(svf, totalAveList, classAveRankList, gradeAveRankList, sonzaiList, testItemCount, countPrintAgeAndRank);
                                }
                            }
                        }
                        testItemCount++;
                        countAgeAndRank++;
                    }
                    //テスト種別が変わったとき
                    if (((String)skiList.get(skiCount)).equals(rs.getString("SKICD")) == false) {
                        //テスト種別による改ページ
                        if (testItemCount == MAX_TEST_ITEM + 1) {
                            svf.VrEndPage();
                            scoreCount = 0;
                            testItemCount = 1;

                            //タイトル・名前・学年情報を表示
                            svf.VrsOut("TITLE", title);
                            svf.VrsOut("NOTICE", notice);
                            svf.VrsOut("HR_NAME", hrName);
                            svf.VrsOut("NAME", name);

                            //受講科目名を表示
                            for (int j =0; j < subjectNameList.size(); j++) {
                                final String subjectName = (String)subjectNameList.get(j);
                                final int subjectNameLen = KNJ_EditEdit.getMS932ByteLength(subjectName);
                                final String subjectNameField = (subjectNameLen <= 6)? "1": "2";
                                svf.VrsOutn("SUBCLASS_NAME" + subjectNameField, count, subjectName);

                                count++;
                            }
                            count = 1;
                        }

                        //テスト種別・順位名
                        if (rs.getString("TESTITEMNAME") != null) {
                            svf.VrsOut("TESTITEMNAME" + testItemCount, rs.getString("TESTITEMNAME"));
                        }
                        svf.VrsOut("SELECT_NAME" + testItemCount + selectNameType, selectName);

                        //平均点・クラス順位・学年順位
                        if (skiList.size() != skiCheckList.size()) {
                            break;
                        }
                        if (((String)skiList.get(countAgeAndRank)).equals(skiCheckList.get(countAgeAndRank)) == true) {
                            if (Integer.parseInt((String)semesterList.get(countAgeAndRank))  <= Integer.parseInt(_param._semester)) {
                                //平均点・クラス順位・学年順位が存在するものだけ印字
                                if (SONZAI_TRUE.equals(sonzaiList.get(countAgeAndRank))) {
                                    countPrintAgeAndRank++;
                                    printAgeAndRank(svf, totalAveList, classAveRankList, gradeAveRankList, sonzaiList, testItemCount, countPrintAgeAndRank);
                                }
                            }
                        }

                        skiCount++;
                        testItemCount++;
                        countAgeAndRank++;
                        scoreCount++;
                    }

                    //素点・平均点・順位(講座・類型)の表示
                    for (int j = 0; j < subjectList.size(); j++) {
                        if (rs.getString("SUBCLASSCD").equals(subjectList.get(j)) == true &&
                            rs.getInt("SEMESTER") <= Integer.parseInt(_param._semester)) {

                            String score = "";
                            //素点
                            if (rs.getString("SCORE") != null) {
                                score = rs.getString("SCORE");
                            }

                            if (rs.getString("KEIKOKUTEN") != null && rs.getString("SCORE") != null &&
                                rs.getInt("SCORE")  <= rs.getInt("KEIKOKUTEN")) {

                                //警告点以下のとき
                                score = "*" + score;
                            }
                            svf.VrsOutn("SCORE" + scoreCount , j + 1, score);

                            //平均・順位
                            if (rs.getString("AVG") != null) {
                                svf.VrsOutn("AVE"  + scoreCount, j + 1, RoundHalfUp(rs.getString("AVG")));
                            }
                            if (rs.getString("RANK") != null) {
                                svf.VrsOutn("RANK" + scoreCount, j + 1, rs.getString("RANK"));
                            }
                        }
                    }
                }
                _nonedata = true;
                svf.VrEndPage();
            }
        } catch (Exception ex) {
            log.error("printSvfMain set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /**四捨五入**/
    private String RoundHalfUp(String str) {
        BigDecimal bg = new BigDecimal(str);
        bg = bg.setScale(1, BigDecimal.ROUND_HALF_UP);
        return String.format("%.1f", bg);
    }

    /**平均点・クラス順位・学年順位の表示**/
    private void printAgeAndRank(
        Vrw32alp svf,
        ArrayList totalAveList,
        ArrayList classAveRankList,
        ArrayList gradeAveRankList,
        ArrayList sonzaiList,
        int testItemCount,
        int countAgeAndRank) {

        if ((String)totalAveList.get(countAgeAndRank) != null) {
            svf.VrsOut("TOTAL_AVE" + testItemCount, (String)totalAveList.get(countAgeAndRank));
        }

        if ((String)classAveRankList.get(countAgeAndRank) != null) {
            svf.VrsOut("HR_RANK" + testItemCount, (String)classAveRankList.get(countAgeAndRank));
        }

        if ((String)gradeAveRankList.get(countAgeAndRank) != null) {
            svf.VrsOut("GRADE_RANK" + testItemCount, (String)gradeAveRankList.get(countAgeAndRank));
        }
    }

    /**受講科目名**/
    private String sqlgetSubclassData(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH REC_SCORE AS ( ");
        stb.append("      SELECT CLASSCD ");
        stb.append("           , SCHOOL_KIND ");
        stb.append("           , CURRICULUM_CD ");
        stb.append("           , SUBCLASSCD ");
        stb.append("        FROM RECORD_SCORE_DAT ");
        stb.append("       WHERE YEAR      = '" + _param._year + "' ");
        stb.append("         AND SCHREGNO  = '" + schregno + "' ");
        stb.append("         AND SCORE_DIV = '01' ");
        stb.append("         AND SEMESTER <= " + _param._semester);
        stb.append("         AND SCORE IS NOT NULL ");
        stb.append("    GROUP BY CLASSCD ");
        stb.append("           , SCHOOL_KIND ");
        stb.append("           , CURRICULUM_CD ");
        stb.append("           , SUBCLASSCD ");
        stb.append(" ) ");
        stb.append("    SELECT REC_SCORE.CLASSCD ");
        stb.append("         , REC_SCORE.SCHOOL_KIND ");
        stb.append("         , REC_SCORE.CURRICULUM_CD ");
        stb.append("         , REC_SCORE.SUBCLASSCD ");
        stb.append("         , CASE WHEN SUB_M.SUBCLASSABBV IS NOT NULL ");
        stb.append("                THEN SUB_M.SUBCLASSABBV ");
        stb.append("                ELSE SUB_M.SUBCLASSNAME ");
        stb.append("           END AS SUBCLASSABBV ");
        stb.append("      FROM REC_SCORE ");
        stb.append("INNER JOIN SUBCLASS_MST  SUB_M ");
        stb.append("        ON SUB_M.CLASSCD       = REC_SCORE.CLASSCD ");
        stb.append("       AND SUB_M.SCHOOL_KIND   = REC_SCORE.SCHOOL_KIND ");
        stb.append("       AND SUB_M.CURRICULUM_CD = REC_SCORE.CURRICULUM_CD ");
        stb.append("       AND SUB_M.SUBCLASSCD    = REC_SCORE.SUBCLASSCD ");
        stb.append("  ORDER BY REC_SCORE.CLASSCD ");
        stb.append("         , REC_SCORE.SCHOOL_KIND ");
        stb.append("         , REC_SCORE.CURRICULUM_CD ");
        stb.append("         , REC_SCORE.SUBCLASSCD ");

        return stb.toString();
    }

    /**各科目の点数・平均**/
    private String sqlScoreData(final String schregno) {
        final StringBuffer stb = new StringBuffer();

        if ("1".equals(_param._type) == true) {
            //講座を選択したとき
            stb.append(" WITH KEIKOKU_JURAI AS ( ");
            stb.append("        SELECT ASSESSHIGH  ");
            stb.append("          FROM ASSESS_MST  ");
            stb.append("         WHERE ASSESSCD    = '2' ");
            stb.append("           AND ASSESSLEVEL = '1' ");
            stb.append(" ), SRD AS ( ");
            stb.append("        SELECT YEAR ");
            stb.append("             , SCHREGNO ");
            stb.append("             , GRADE ");
            stb.append("             , HR_CLASS ");
            stb.append("             , COURSECD ");
            stb.append("             , MAJORCD ");
            stb.append("             , COURSECODE ");
            stb.append("          FROM SCHREG_REGD_DAT ");
            stb.append("         WHERE YEAR     = '" + _param._year + "' ");
            stb.append("           AND SCHREGNO = '" + schregno + "' ");
            stb.append("      GROUP BY YEAR ");
            stb.append("             , SCHREGNO ");
            stb.append("             , GRADE ");
            stb.append("             , HR_CLASS ");
            stb.append("             , COURSECD ");
            stb.append("             , MAJORCD ");
            stb.append("             , COURSECODE ");
            stb.append(" ), ");
            stb.append(" RSD AS ( ");
            stb.append("        SELECT * ");
            stb.append("          FROM RECORD_SCORE_DAT ");
            stb.append("         WHERE YEAR      = '" + _param._year + "' ");
            stb.append("           AND SCORE_DIV = '01' ");
            stb.append("           AND SCHREGNO  = '" + schregno + "' ");
            stb.append(" ),  ");
            stb.append(" RRCS AS ( ");
            stb.append("        SELECT * ");
            stb.append("          FROM RECORD_RANK_CHAIR_SDIV_DAT ");
            stb.append("         WHERE YEAR     = '" + _param._year + "' ");
            stb.append("           AND SCHREGNO = '" + schregno + "' ");
            stb.append(" ), ");
            stb.append(" SDD AS ( ");
            stb.append("        SELECT * ");
            stb.append("          FROM SCHOOL_DETAIL_DAT ");
            stb.append("         WHERE SCHOOLCD   = '000000000000'  ");
            stb.append("           AND SCHOOL_SEQ = '009' ");
            stb.append(" ) ");
            stb.append("     SELECT RSD.SEMESTER  ");
            stb.append("          , RSD.SEMESTER || RSD.TESTKINDCD || RSD.TESTITEMCD AS SKICD "); //行特定用
            stb.append("          , TMC.TESTITEMNAME ");
            stb.append("          , RSD.SUBCLASSCD ");                                            //列特定用
            stb.append("          , RSD.SCORE ");
            stb.append("          , RAC.AVG AS AVG ");             //講座平均
            stb.append("          , RRCS.GRADE_AVG_RANK AS RANK"); //講座順位
            stb.append("          , CASE SDD.SCHOOL_REMARK1 ");    //警告点区分
            stb.append("                 WHEN '1' THEN CDD.REMARK1 ");                                         //講座警告点
            stb.append("                 WHEN '2' THEN SDD.SCHOOL_REMARK2 / SDD.SCHOOL_REMARK3 * RAS.AVG ");   //類型警告点
            stb.append("                 ELSE (SELECT ASSESSHIGH FROM KEIKOKU_JURAI) ");                       //従来警告点
            stb.append("            END AS KEIKOKUTEN ");
            stb.append("       FROM RSD ");
            stb.append("  LEFT JOIN SRD ");
            stb.append("         ON SRD.YEAR          = RSD.YEAR ");
            stb.append("        AND SRD.SCHREGNO      = RSD.SCHREGNO ");
            stb.append("  LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TMC ");
            stb.append("         ON TMC.YEAR          = RSD.YEAR ");
            stb.append("        AND TMC.SEMESTER      = RSD.SEMESTER ");
            stb.append("        AND TMC.TESTKINDCD    = RSD.TESTKINDCD ");
            stb.append("        AND TMC.TESTITEMCD    = RSD.TESTITEMCD ");
            stb.append("        AND TMC.SCORE_DIV     = RSD.SCORE_DIV ");
            stb.append("  LEFT JOIN RECORD_AVERAGE_CHAIR_SDIV_DAT RAC ");
            stb.append("         ON RAC.YEAR          = SRD.YEAR ");
            stb.append("        AND RAC.SEMESTER      = TMC.SEMESTER ");
            stb.append("        AND RAC.TESTKINDCD    = TMC.TESTKINDCD ");
            stb.append("        AND RAC.TESTITEMCD    = TMC.TESTITEMCD ");
            stb.append("        AND RAC.SCORE_DIV     = RSD.SCORE_DIV ");
            stb.append("        AND RAC.CLASSCD       = RSD.CLASSCD ");
            stb.append("        AND RAC.CURRICULUM_CD = RSD.CURRICULUM_CD ");
            stb.append("        AND RAC.SUBCLASSCD    = RSD.SUBCLASSCD ");
            stb.append("        AND RAC.CHAIRCD       = RSD.CHAIRCD ");
            stb.append("        AND RAC.AVG_DIV       = '1' ");
            stb.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT RAS ");
            stb.append("         ON RAS.YEAR          = RSD.YEAR ");
            stb.append("        AND RAS.SEMESTER      = RSD.SEMESTER ");
            stb.append("        AND RAS.TESTKINDCD    = RSD.TESTKINDCD ");
            stb.append("        AND RAS.TESTITEMCD    = RSD.TESTITEMCD ");
            stb.append("        AND RAS.SCORE_DIV     = RSD.SCORE_DIV ");
            stb.append("        AND RAS.CLASSCD       = RSD.CLASSCD ");
            stb.append("        AND RAS.SCHOOL_KIND   = RSD.SCHOOL_KIND ");
            stb.append("        AND RAS.CURRICULUM_CD = RSD.CURRICULUM_CD ");
            stb.append("        AND RAS.SUBCLASSCD    = RSD.SUBCLASSCD ");
            stb.append("        AND RAS.AVG_DIV       = '6' ");
            stb.append("        AND RAS.GRADE         = SRD.GRADE ");
            stb.append("        AND RAS.HR_CLASS      = SRD.HR_CLASS ");
            stb.append("        AND RAS.COURSECD      = SRD.COURSECD ");
            stb.append("        AND RAS.MAJORCD       = SRD.MAJORCD ");
            stb.append("        AND RAS.COURSECODE    = SRD.COURSECODE ");
            stb.append("  LEFT JOIN RRCS ");
            stb.append("         ON RRCS.YEAR          = RSD.YEAR ");
            stb.append("        AND RRCS.SEMESTER      = RSD.SEMESTER ");
            stb.append("        AND RRCS.TESTKINDCD    = RSD.TESTKINDCD ");
            stb.append("        AND RRCS.TESTITEMCD    = RSD.TESTITEMCD ");
            stb.append("        AND RRCS.CLASSCD       = RSD.CLASSCD ");
            stb.append("        AND RRCS.SCHOOL_KIND   = RSD.SCHOOL_KIND ");
            stb.append("        AND RRCS.CURRICULUM_CD = RSD.CURRICULUM_CD ");
            stb.append("        AND RRCS.SUBCLASSCD    = RSD.SUBCLASSCD ");
            stb.append("        AND RRCS.CHAIRCD       = RSD.CHAIRCD ");
            stb.append("        AND RRCS.SCHREGNO      = RSD.SCHREGNO ");
            stb.append("  LEFT JOIN CHAIR_DETAIL_DAT  CDD ");
            stb.append("         ON CDD.YEAR          = RSD.YEAR ");
            stb.append("        AND CDD.SEMESTER      = RSD.SEMESTER ");
            stb.append("        AND CDD.CHAIRCD       = RSD.CHAIRCD ");
            stb.append("        AND CDD.SEQ           = '003' ");
            stb.append("  LEFT JOIN SDD ");
            stb.append("         ON SDD.YEAR          = RSD.YEAR ");
            stb.append("        AND SDD.SCHOOL_KIND   = RSD.SCHOOL_KIND ");
            stb.append("   ORDER BY RSD.SEMESTER ");
            stb.append("          , RSD.TESTKINDCD ");
            stb.append("          , RSD.TESTITEMCD ");
            stb.append("          , RSD.SUBCLASSCD ");
        } else if ("2".equals(_param._type) == true) {
            //類型を選択したとき
            stb.append(" WITH KEIKOKU_JURAI AS ( ");
            stb.append("   SELECT ");
            stb.append("     ASSESSHIGH ");
            stb.append("   FROM ");
            stb.append("     ASSESS_MST  ");
            stb.append("   WHERE ");
            stb.append("     ASSESSCD = '2'  ");
            stb.append("     AND ASSESSLEVEL = '1' ");
            stb.append(" )  ");
            stb.append(" , SRD AS (  ");
            stb.append("   SELECT ");
            stb.append("     YEAR ");
            stb.append("     , SCHREGNO ");
            stb.append("     , GRADE ");
            stb.append("     , HR_CLASS ");
            stb.append("     , COURSECD ");
            stb.append("     , MAJORCD ");
            stb.append("     , COURSECODE  ");
            stb.append("   FROM ");
            stb.append("     SCHREG_REGD_DAT  ");
            stb.append("   WHERE ");
            stb.append("         YEAR     = '" + _param._year + "' ");
            stb.append("     AND SCHREGNO = '" + schregno + "' ");
            stb.append("   GROUP BY ");
            stb.append("     YEAR ");
            stb.append("     , SCHREGNO ");
            stb.append("     , GRADE ");
            stb.append("     , HR_CLASS ");
            stb.append("     , COURSECD ");
            stb.append("     , MAJORCD ");
            stb.append("     , COURSECODE ");
            stb.append(" )  ");
            stb.append(" , RSD AS (  ");
            stb.append("   SELECT ");
            stb.append("     *  ");
            stb.append("   FROM ");
            stb.append("     RECORD_SCORE_DAT  ");
            stb.append("   WHERE ");
            stb.append("         YEAR      = '" + _param._year + "' ");
            stb.append("     AND SCORE_DIV = '01'  ");
            stb.append("     AND SCHREGNO = '" + schregno + "' ");
            stb.append(" )  ");
            stb.append(" , RRS AS (  ");
            stb.append("   SELECT ");
            stb.append("     *  ");
            stb.append("   FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT  ");
            stb.append("   WHERE ");
            stb.append("         YEAR      = '" + _param._year + "' ");
            stb.append("     AND SCHREGNO = '" + schregno + "' ");
            stb.append(" )  ");
            stb.append(" , SDD AS (  ");
            stb.append("   SELECT ");
            stb.append("     *  ");
            stb.append("   FROM ");
            stb.append("     SCHOOL_DETAIL_DAT  ");
            stb.append("   WHERE ");
            stb.append("     SCHOOLCD = '000000000000'  ");
            stb.append("     AND SCHOOL_SEQ = '009' ");
            stb.append(" )  ");
            stb.append(" SELECT ");
            stb.append("   RSD.SEMESTER ");
            stb.append("   , RSD.SEMESTER || RSD.TESTKINDCD || RSD.TESTITEMCD AS SKICD ");
            stb.append("   , TMC.TESTITEMNAME ");
            stb.append("   , RSD.SUBCLASSCD ");
            stb.append("   , RSD.SCORE ");
            stb.append("   , RRS.CHAIR_GROUP_RANK AS RANK ");
            stb.append("   , CASE  ");
            stb.append("     WHEN CHRGRP_2.CHAIR_GROUP_CD IS NOT NULL  ");
            stb.append("       THEN RAS2_2.AVG  ");
            stb.append("     ELSE RAS2_1.AVG  ");
            stb.append("     END AS AVG ");
            stb.append("   , CASE SDD.SCHOOL_REMARK1  ");
            stb.append("     WHEN '1' THEN CDD.REMARK1  ");
            stb.append("     WHEN '2' THEN SDD.SCHOOL_REMARK2 / SDD.SCHOOL_REMARK3 * RAS2_2.AVG  ");
            stb.append("     ELSE (SELECT ASSESSHIGH FROM KEIKOKU_JURAI)  ");
            stb.append("     END AS KEIKOKUTEN  ");
            stb.append(" FROM ");
            stb.append("   RSD  ");
            stb.append("   LEFT JOIN SRD  ");
            stb.append("     ON SRD.YEAR = RSD.YEAR  ");
            stb.append("     AND SRD.SCHREGNO = RSD.SCHREGNO  ");
            stb.append("   LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TMC  ");
            stb.append("     ON TMC.YEAR = RSD.YEAR  ");
            stb.append("     AND TMC.SEMESTER = RSD.SEMESTER  ");
            stb.append("     AND TMC.TESTKINDCD = RSD.TESTKINDCD  ");
            stb.append("     AND TMC.TESTITEMCD = RSD.TESTITEMCD  ");
            stb.append("     AND TMC.SCORE_DIV = RSD.SCORE_DIV  ");
            stb.append("   LEFT JOIN CHAIR_GROUP_SDIV_DAT CHRGRP_1  ");
            stb.append("     ON CHRGRP_1.YEAR = TMC.YEAR  ");
            stb.append("     AND CHRGRP_1.SEMESTER = TMC.SEMESTER  ");
            stb.append("     AND CHRGRP_1.TESTKINDCD = TMC.TESTKINDCD  ");
            stb.append("     AND CHRGRP_1.TESTITEMCD = TMC.TESTITEMCD  ");
            stb.append("     AND CHRGRP_1.SCORE_DIV = RSD.SCORE_DIV  ");
            stb.append("     AND CHRGRP_1.CHAIRCD = RSD.CHAIRCD  ");
            stb.append("   LEFT JOIN RECORD_AVERAGE_SDIV_DAT RAS2_1  ");
            stb.append("     ON RAS2_1.YEAR = SRD.YEAR  ");
            stb.append("     AND RAS2_1.SEMESTER = TMC.SEMESTER  ");
            stb.append("     AND RAS2_1.TESTKINDCD = TMC.TESTKINDCD  ");
            stb.append("     AND RAS2_1.TESTITEMCD = TMC.TESTITEMCD  ");
            stb.append("     AND RAS2_1.SCORE_DIV = RSD.SCORE_DIV  ");
            stb.append("     AND RAS2_1.CLASSCD = RSD.CLASSCD  ");
            stb.append("     AND RAS2_1.SCHOOL_KIND = RSD.SCHOOL_KIND  ");
            stb.append("     AND RAS2_1.CURRICULUM_CD = RSD.CURRICULUM_CD  ");
            stb.append("     AND RAS2_1.SUBCLASSCD = RSD.SUBCLASSCD  ");
            stb.append("     AND RAS2_1.AVG_DIV = '6'  ");
            stb.append("     AND RAS2_1.GRADE = SRD.GRADE  ");
            stb.append("     AND RAS2_1.HR_CLASS = '000'  ");
            stb.append("     AND RAS2_1.MAJORCD = CHRGRP_1.CHAIR_GROUP_CD  ");
            stb.append("   LEFT JOIN CHAIR_GROUP_SDIV_DAT CHRGRP_2  ");
            stb.append("     ON CHRGRP_2.YEAR = TMC.YEAR  ");
            stb.append("     AND CHRGRP_2.SEMESTER = TMC.SEMESTER  ");
            stb.append("     AND CHRGRP_2.TESTKINDCD = '00'  ");
            stb.append("     AND CHRGRP_2.TESTITEMCD = '00'  ");
            stb.append("     AND CHRGRP_2.SCORE_DIV = '00'  ");
            stb.append("     AND CHRGRP_2.CHAIRCD = RSD.CHAIRCD  ");
            stb.append("   LEFT JOIN RECORD_AVERAGE_SDIV_DAT RAS2_2  ");
            stb.append("     ON RAS2_2.YEAR = SRD.YEAR  ");
            stb.append("     AND RAS2_2.SEMESTER = TMC.SEMESTER  ");
            stb.append("     AND RAS2_2.TESTKINDCD = TMC.TESTKINDCD  ");
            stb.append("     AND RAS2_2.TESTITEMCD = TMC.TESTITEMCD  ");
            stb.append("     AND RAS2_2.SCORE_DIV = RSD.SCORE_DIV  ");
            stb.append("     AND RAS2_2.CLASSCD = RSD.CLASSCD  ");
            stb.append("     AND RAS2_2.SCHOOL_KIND = RSD.SCHOOL_KIND  ");
            stb.append("     AND RAS2_2.CURRICULUM_CD = RSD.CURRICULUM_CD  ");
            stb.append("     AND RAS2_2.SUBCLASSCD = RSD.SUBCLASSCD  ");
            stb.append("     AND RAS2_2.AVG_DIV = '6'  ");
            stb.append("     AND RAS2_2.GRADE = SRD.GRADE  ");
            stb.append("     AND RAS2_2.HR_CLASS = '000'  ");
            stb.append("     AND RAS2_2.MAJORCD = CHRGRP_2.CHAIR_GROUP_CD  ");
            stb.append("   LEFT JOIN RRS  ");
            stb.append("     ON RRS.YEAR = SRD.YEAR  ");
            stb.append("     AND RRS.SEMESTER = TMC.SEMESTER  ");
            stb.append("     AND RRS.TESTKINDCD = TMC.TESTKINDCD  ");
            stb.append("     AND RRS.TESTITEMCD = TMC.TESTITEMCD  ");
            stb.append("     AND RRS.SCORE_DIV = TMC.SCORE_DIV  ");
            stb.append("     AND RRS.CLASSCD = RSD.CLASSCD  ");
            stb.append("     AND RRS.SCHOOL_KIND = RSD.SCHOOL_KIND  ");
            stb.append("     AND RRS.CURRICULUM_CD = RSD.CURRICULUM_CD  ");
            stb.append("     AND RRS.SUBCLASSCD = RSD.SUBCLASSCD  ");
            stb.append("     AND RRS.SCHREGNO = RSD.SCHREGNO  ");
            stb.append("   LEFT JOIN CHAIR_DETAIL_DAT CDD  ");
            stb.append("     ON CDD.YEAR = RSD.YEAR  ");
            stb.append("     AND CDD.SEMESTER = RSD.SEMESTER  ");
            stb.append("     AND CDD.CHAIRCD = RSD.CHAIRCD  ");
            stb.append("     AND CDD.SEQ = '003'  ");
            stb.append("   LEFT JOIN SDD  ");
            stb.append("     ON SDD.YEAR = RSD.YEAR  ");
            stb.append("     AND SDD.SCHOOL_KIND = RSD.SCHOOL_KIND  ");
            stb.append(" ORDER BY ");
            stb.append("   RSD.SEMESTER ");
            stb.append("   , RSD.TESTKINDCD ");
            stb.append("   , RSD.TESTITEMCD ");
            stb.append("   , RSD.SUBCLASSCD ");
        }

        return stb.toString();
    }

    /**クラス順位・学年順位・平均点**/
    private String sqlGetRankData(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SRD AS (");
        stb.append("      SELECT SRD.YEAR ");
        stb.append("           , SRD.SCHREGNO ");
        stb.append("           , SRD.SEMESTER ");
        stb.append("        FROM SCHREG_REGD_DAT  SRD ");
        stb.append("       WHERE ");
        stb.append("             SRD.YEAR = '" + _param._year + "' ");
        stb.append("         AND SRD.SCHREGNO = '" + schregno + "' ");
        stb.append(" )  ");
        stb.append("    SELECT TMC.SEMESTER || TMC.TESTKINDCD || TMC.TESTITEMCD  AS SKICD ");  //行特定
        stb.append("         , SRD.SEMESTER ");
        stb.append("         , RRS.AVG  ");             //平均点
        stb.append("         , RRS.CLASS_AVG_RANK ");   //クラス順位
        stb.append("         , RRS.GRADE_AVG_RANK ");   //学年順位
        stb.append("         , CASE WHEN RRS.YEAR IS NOT NULL THEN '1'");
        stb.append("                ELSE '0' ");
        stb.append("           END AS SONZAI_FLG ");
        stb.append("      FROM SRD ");
        stb.append(" LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TMC ");
        stb.append("        ON TMC.YEAR          = SRD.YEAR ");
        stb.append("       AND TMC.SEMESTER      = SRD.SEMESTER ");
        stb.append("       AND TMC.SCORE_DIV     = '01' ");
        stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT RRS ");
        stb.append("        ON RRS.YEAR          = SRD.YEAR ");
        stb.append("       AND RRS.SEMESTER      = SRD.SEMESTER ");
        stb.append("       AND RRS.SCHREGNO      = SRD.SCHREGNO ");
        stb.append("       AND RRS.SUBCLASSCD    = '999999' ");
        stb.append("       AND RRS.TESTKINDCD    = TMC.TESTKINDCD  ");
        stb.append("       AND RRS.TESTITEMCD    = TMC.TESTITEMCD ");
        stb.append("       AND RRS.SCORE_DIV     = TMC.SCORE_DIV ");
        stb.append("     WHERE SRD.YEAR          = '" + _param._year + "' ");
        stb.append("       AND SRD.SCHREGNO      = '" + schregno + "' ");
        stb.append("  ORDER BY SRD.SCHREGNO ");
        stb.append("         , SRD.SEMESTER ");
        stb.append("         , TMC.TESTKINDCD ");
        stb.append("         , TMC.TESTITEMCD ");

        return stb.toString();
    }

    /**テスト種別取得**/
    private String sqlgetSkiCd(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT REC_SCORE.SEMESTER ");
        stb.append("         , REC_SCORE.TESTKINDCD ");
        stb.append("         , REC_SCORE.TESTITEMCD  ");
        stb.append("         , TMC.TESTITEMNAME ");
        stb.append("      FROM RECORD_SCORE_DAT REC_SCORE  ");
        stb.append(" LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TMC  ");
        stb.append("        ON TMC.YEAR            = REC_SCORE.YEAR  ");
        stb.append("       AND TMC.SEMESTER        = REC_SCORE.SEMESTER  ");
        stb.append("       AND TMC.TESTKINDCD      = REC_SCORE.TESTKINDCD  ");
        stb.append("       AND TMC.TESTITEMCD      = REC_SCORE.TESTITEMCD  ");
        stb.append("       AND TMC.SCORE_DIV       = REC_SCORE.SCORE_DIV  ");
        stb.append("     WHERE REC_SCORE.YEAR      = '" + _param._year + "'  ");
        stb.append("       AND REC_SCORE.SCHREGNO  = '" + schregno + "' ");
        stb.append("       AND REC_SCORE.SCORE_DIV = '01'  ");
        stb.append("  GROUP BY REC_SCORE.SEMESTER ");
        stb.append("         , REC_SCORE.TESTKINDCD ");
        stb.append("         , REC_SCORE.TESTITEMCD  ");
        stb.append("         , TMC.TESTITEMNAME ");
        stb.append("  ORDER BY REC_SCORE.SEMESTER ");
        stb.append("         , REC_SCORE.TESTKINDCD ");
        stb.append("         , REC_SCORE.TESTITEMCD ");

        return stb.toString();
    }

    /**クラスに在籍する学籍番号取得**/
    private String getClassSchregnos(String year, String semester, String[] categorySelected) {
        final StringBuffer stb = new StringBuffer();

        stb.append("   SELECT ");
        stb.append("          SRD.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("          SCHREG_REGD_DAT SRD ");
        stb.append("    WHERE ");
        stb.append("          SRD.YEAR = '" + year + "' ");
        stb.append("      AND SRD.SEMESTER = '" + semester + "' ");
        stb.append("      AND SRD.GRADE || '-' || SRD.HR_CLASS IN " + SQLUtils.whereIn(true, categorySelected));
        stb.append(" ORDER BY ");
        stb.append("          SRD.GRADE ");
        stb.append("        , SRD.HR_CLASS ");
        stb.append("        , SRD.ATTENDNO ");

        return stb.toString();
    }

    /**生徒基本情報**/
    private String sqlGetHeaderData(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT SRD.SCHREGNO ");
        stb.append("         , SRG.GRADE_NAME2 ");
        stb.append("         , SRH.HR_CLASS_NAME1 ");
        stb.append("         , SRD.ATTENDNO ");
        stb.append("         , SBM.NAME ");
        stb.append("      FROM  SCHREG_REGD_DAT SRD ");
        stb.append(" LEFT JOIN SCHREG_REGD_GDAT SRG ");
        stb.append("        ON SRG.YEAR  = SRD.YEAR ");
        stb.append("       AND SRG.GRADE = SRD.GRADE ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("        ON SRH.YEAR     = SRD.YEAR ");
        stb.append("       AND SRH.SEMESTER = SRD.SEMESTER ");
        stb.append("       AND SRH.GRADE    = SRD.GRADE ");
        stb.append("       AND SRH.HR_CLASS = SRD.HR_CLASS ");
        stb.append(" LEFT JOIN SCHREG_BASE_MST  SBM ");
        stb.append("        ON SBM.SCHREGNO = SRD.SCHREGNO ");
        stb.append("     WHERE SRD.YEAR     = '" + _param._year + "'  ");
        stb.append("       AND SRD.SEMESTER ='"  + _param._semester + "'"); //画面から受け取った学期
        stb.append("       AND SRD.SCHREGNO ='"  + schregno + "'");
        stb.append("  ORDER BY SRD.GRADE ");
        stb.append("         , SRD.HR_CLASS ");
        stb.append("         , SRD.ATTENDNO ");

        return stb.toString();
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision$ $Date$"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

    private class Param {
        private final String _year;
        private final String _loginDate;
        private final String _documentRoot;
        private String _imagepath;
        private String _extension;
        final boolean _isOutputDebug;
        private final String _semester;
        private final String _grade;
        private final String _hrClass;
        private final String _kubun;
        private final String _type;
        private final String[] _categorySelected;
        private final String[] _schregnos;

        public Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year             = request.getParameter("YEAR");                      //年度
            _semester         = request.getParameter("SEMESTER");                  //学期
            _grade            = request.getParameter("GRADE");                     //学年
            _hrClass          = request.getParameter("HR_CLASS");                  //クラス
            _kubun            = request.getParameter("CATEGORY_IS_CLASS");         //表示区分
            _type             = request.getParameter("type_course");               //順位・類型ラジオボタン
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");   //選択クラスまたは学籍番号
            _loginDate        = request.getParameter("LOGIN_DATE");                //ログイン日付
            _documentRoot     = request.getParameter("DOCUMENTROOT");

            PreparedStatement ps = null;
            ResultSet rs = null;
            String sql = null;
            String _categorySelectes = "";

            if ("1".equals(_kubun) == true) {
                //クラスを選択したとき
                sql = getClassSchregnos(_year, _semester, _categorySelected);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int count = 0;
                while (rs.next()) {
                    if (count == 0) {
                        _categorySelectes = rs.getString("SCHREGNO");
                    } else {
                        _categorySelectes += "," + rs.getString("SCHREGNO");
                    }
                    count++;
                }
                _schregnos = _categorySelectes.split(",");
            } else {
                //個人を選択したとき
                _schregnos = _categorySelected;
            }

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJA157BH' AND NAME = '" + propName + "' "));
        }
    }
}

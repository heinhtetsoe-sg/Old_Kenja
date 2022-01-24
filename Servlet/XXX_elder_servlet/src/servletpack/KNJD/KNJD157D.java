// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2021/03/19
 * 作成者: Nutec
 *
 */
package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;

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
 *                  ＜ＫＮＪⅮ１５７Ⅾ＞  成績不振一覧表
 */
public class KNJD157D {

    private static final Log log = LogFactory.getLog(KNJD157D.class);

    private boolean nonedata = false;                               //該当データなしフラグ
    private Param _param = null;

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
                     throws ServletException, IOException
    {
        final Vrw32alp svf = new Vrw32alp();            //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        PrintWriter outstrm = null;
        try {
            //  print設定
            response.setContentType("application/pdf");
            outstrm = new PrintWriter (response.getOutputStream());

            //  svf設定
            svf.VrInit();                               //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());       //PDFファイル名の設定

            sd.setSvfInit(request, response, svf);
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }

            _param = getParam(db2, request);

            printSvfMain(db2, svf);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();                //DBを閉じる
            outstrm.close();            //ストリームを閉じる
        }

    }//doGetの括り

    private static int toInt(final String s, final int def) {
        if (!NumberUtils.isDigits(s)) {
            return def;
        }
        return Integer.parseInt(s);
    }

    private void setForm(final Vrw32alp svf, final String form, final int data) {
        svf.VrSetForm(form, data);
        log.info(" form = " + form);
        if (_param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }

    /** 帳票出力 **/
    private void printSvfMain(
        final DB2UDB db2,
        final Vrw32alp svf
    ) {

        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql   = null;

        try {


            setForm(svf, "KNJD157D.xml", 1);

            nonedata = false;
            boolean noclassdate = true;

            ArrayList<String> stamp                     = new ArrayList<String>();
            ArrayList<ArrayList<String>> gradeClass     = new ArrayList<ArrayList<String>>();
            ArrayList<ArrayList<String>> subclass    = new ArrayList<ArrayList<String>>();
            ArrayList<ArrayList<String>> studentDate    = new ArrayList<ArrayList<String>>();
            ArrayList<ArrayList<String>> subclassScore = new ArrayList<ArrayList<String>>();
            ArrayList<String> work;  //一時保管用


            //押印欄
            sql = getStampData();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                stamp.add(rs.getString("TITLE"));  //役職名
            }
            if (stamp.size() == 0) {
                stamp.add("校長");
                stamp.add("教頭");
                stamp.add("教務主任");
                stamp.add("学年主任");
                stamp.add("担任");
            }

            //学年クラス名
            sql = sqlGradeClass();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                work = new ArrayList<String>();
                work.add(rs.getString("GRADE"));     //学年
                work.add(rs.getString("HR_CLASS"));  //クラス
                work.add(rs.getString("HR_NAME"));   //クラス名
                gradeClass.add(work);
            }

            //テスト種別
            sql = sqlTestType();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();
            String title = rs.getString("TESTITEMNAME");
            String div = "";
            if ("01".equals(rs.getString("SCORE_DIV"))) {
                div = "素点";
            }
            else if ("08".equals(rs.getString("SCORE_DIV"))) {
                div = "評価";
            }
            else if ("09".equals(rs.getString("SCORE_DIV"))) {
                div = "評定";
            }

            //警告点以下の科目
            sql = sqlKeikokuSubclass();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                work = new ArrayList<String>();
                work.add(rs.getString("GRADE"));           //学年
                work.add(rs.getString("HR_CLASS"));        //クラス
                work.add(rs.getString("SUBCLASSCD"));      //科目コード
                work.add(rs.getString("SUBCLASSABBV"));    //科目名
                work.add(rs.getString("STAFFNAME"));       //科目担任名
                work.add(rs.getString("CREDITS"));         //単位数
                subclass.add(work);
            }

            //生徒情報(欠点講座数等含む)
            sql = sqlStudentData();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                work = new ArrayList<String>();
                work.add(rs.getString("SCHREGNO"));       //学籍番号
                work.add(rs.getString("GRADE"));          //学年
                work.add(rs.getString("HR_CLASS"));       //クラス
                work.add(rs.getString("HR_CLASS_NAME2"));  //クラス名
                work.add(rs.getString("ATTENDNO"));       //番号
                work.add(rs.getString("NAME"));           //氏名
                work.add(rs.getString("MIJUKEN_COUNT"));  //未受験講座数
                work.add(rs.getString("KETTEN_COUNT"));   //欠点講座数
                work.add(rs.getString("CREDITS_TOTAL"));  //欠点合計単位
                studentDate.add(work);
            }

            //生徒の科目ごとの点数(未受検か警告点以下のデータのみ)
            sql = sqlSubclassScore();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                work = new ArrayList<String>();
                work.add(rs.getString("SCHREGNO"));       //学籍番号
                work.add(rs.getString("SUBCLASSCD"));     //科目コード
                work.add(rs.getString("SCORE"));          //点数
                work.add(rs.getString("VALUE_DI"));       //未受検かどうか
                subclassScore.add(work);
            }

            //日付の和暦変換に使用
            Calendar calendar = Calendar.getInstance();
            calendar.set(toInt(_param._year, 0), 4, 1);

            String grade    = "";
            String hrClass  = "";
            String schregno = "";
            ArrayList<String> sub;
            int row;

            //帳票に印字
            for (int dateCnt = 0; dateCnt < gradeClass.size(); dateCnt++) {
                if (noclassdate == false) {
                    svf.VrEndPage();
                }
                noclassdate = true;
                //学年、クラスを保持
                grade   = ((ArrayList<String>)gradeClass.get(dateCnt)).get(0);
                hrClass = ((ArrayList<String>)gradeClass.get(dateCnt)).get(1);

                //ヘッダー
                String dateStr = KNJ_EditDate.gengou(db2, calendar.get(Calendar.YEAR)) + "年度";

                svf.VrsOut("TITLE", dateStr + "　" + title + "　成績不振一覧表(" + div + ")");
                //クラス名
                svf.VrsOut("HR_NAME", ((ArrayList<String>)gradeClass.get(dateCnt)).get(2));

                //押印欄
                for (int i = 0; i < stamp.size(); i++) {
                    log.info(stamp.get(i));
                    svf.VrsOut("JOB_NAME" + (i + 1), stamp.get(i));
                }

                //科目名
                sub = new ArrayList<String>();
                row = 1;
                for (int cnt = 0; cnt < subclass.size(); cnt++) {  //科目
                    //学年、クラスが同じ
                    if (   grade.equals(((ArrayList<String>)subclass.get(cnt)).get(0))
                        && hrClass.equals(((ArrayList<String>)subclass.get(cnt)).get(1))) {
                        //科目名
                        String subclassName = ((ArrayList<String>)subclass.get(cnt)).get(3);
                        final int subclassNameLen = KNJ_EditEdit.getMS932ByteLength(subclassName);
                        final String subclassNameField = (subclassNameLen <= 8)? "1": "2";
                        svf.VrsOutn("SUBCLASS_NAME" + subclassNameField, row, subclassName);
                        //科目担任名
                        svf.VrsOutn("TR_NAME", row, ((ArrayList<String>)subclass.get(cnt)).get(4));
                        //科目単位数
                        svf.VrsOutn("CREDIT", row, ((ArrayList<String>)subclass.get(cnt)).get(5));
                        //得点名称
                        svf.VrsOutn("SCORE_DIV_NAME", row, div);
                        //科目コードを保持
                        sub.add(((ArrayList<String>)subclass.get(cnt)).get(2));
                        row++;
                    }
                }

                int[] totalKettenCnt = new int[sub.size()];
                for (int cnt = 0; cnt < sub.size(); cnt++) {
                    totalKettenCnt[cnt] = 0;
                }

                //生徒情報
                row = 1;
                for (int stdCnt = 0; stdCnt < studentDate.size(); stdCnt++) {  //生徒情報
                    //学年、クラスが同じ
                    if (   grade.equals(((ArrayList<String>)studentDate.get(stdCnt)).get(1))
                        && hrClass.equals(((ArrayList<String>)studentDate.get(stdCnt)).get(2))) {
                        //組名称
                        svf.VrsOutn("HR_CLASS_NAME", row, ((ArrayList<String>)studentDate.get(stdCnt)).get(3));
                        //番号
                        svf.VrsOutn("ATTENDNO", row, String.valueOf(toInt(((ArrayList<String>)studentDate.get(stdCnt)).get(4), 0)));
                        //氏名
                        svf.VrsOutn("NAME1", row, ((ArrayList<String>)studentDate.get(stdCnt)).get(5));
                        //未受験講座数
                        svf.VrsOutn("NO_EXAM_NUM", row, ((ArrayList<String>)studentDate.get(stdCnt)).get(6));
                        //欠点講座数
                        svf.VrsOutn("DEFECT_CHAIR_NUM", row, ((ArrayList<String>)studentDate.get(stdCnt)).get(7));
                        //欠点合計単位
                        svf.VrsOutn("DEFECT_CREDIT_TOTAL", row, ((ArrayList<String>)studentDate.get(stdCnt)).get(8));
                        //学籍番号を保持
                        schregno = ((ArrayList<String>)studentDate.get(stdCnt)).get(0);

                        //生徒の科目ごとの点数
                        for (int subCnt = 0; subCnt < sub.size(); subCnt++) {  //科目
                            for (int ssCnt = 0; ssCnt < subclassScore.size(); ssCnt++) {  //生徒の科目毎の点数
                                //学籍番号、科目が同じ
                                if (   schregno.equals(((ArrayList<String>)subclassScore.get(ssCnt)).get(0))
                                    && sub.get(subCnt).equals(((ArrayList<String>)subclassScore.get(ssCnt)).get(1))) {

                                    if ("*".equals(((ArrayList<String>)subclassScore.get(ssCnt)).get(3))) {
                                        //VALUE_DI = '*'の場合は!を印字する
                                        svf.VrsOutn("WARNING_MARK" + row, (subCnt + 1), "!");
                                    }
                                    else {
                                        //先頭に*を印字する
                                        svf.VrsOutn("WARNING_MARK" + row, (subCnt + 1), "*");
                                        totalKettenCnt[subCnt]++;
                                        //得点
                                        svf.VrsOutn("SCORE" + row, (subCnt + 1), ((ArrayList<String>)subclassScore.get(ssCnt)).get(2));
                                    }
                                }
                                nonedata    = true;
                                noclassdate = false;
                            }
                        }
                        row++;
                    }
                }
                for (int subCnt = 0; subCnt < totalKettenCnt.length; subCnt++) {
                    //欠点人数
                    svf.VrsOutn("DEFECT_NUM", (subCnt + 1), String.valueOf(totalKettenCnt[subCnt]));
                }
            }

            if (nonedata) {
                svf.VrEndPage();
                svf.VrsOut("DUMMY", "　");
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("printSvfMain set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }

    /**押印枠**/
    private String getStampData() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT SEQ");
        stb.append("      , FILE_NAME ");
        stb.append("      , TITLE ");
        stb.append("   FROM PRG_STAMP_DAT");
        stb.append("  WHERE YEAR        = '" + _param._year + "' ");
        stb.append("    AND SEMESTER    = '9' ");
        stb.append("    AND SCHOOLCD    = '" + _param._schoolcd + "' ");
        stb.append("    AND SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
        stb.append("    AND PROGRAMID   = 'KNJD157D' ");

        return stb.toString();
    }

    /**学年クラス名**/
    private String sqlGradeClass() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("        GRADE ");
        stb.append("      , HR_CLASS ");
        stb.append("      , HR_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT");
        stb.append(" WHERE ");
        stb.append("         YEAR         = '" + _param._year + "' ");
        stb.append("     AND SEMESTER     = '" + _param._semester + "' ");
        stb.append("     AND GRADE || HR_CLASS IN " + SQLUtils.whereIn(true, _param._class) + " ");

        return stb.toString();
    }

    /**テスト種別**/
    private String sqlTestType() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("        T1.TESTITEMNAME ");
        stb.append("      , T1.SCORE_DIV ");
        stb.append(" FROM ");
        stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV T1");
        stb.append(" WHERE ");
        stb.append("         T1.YEAR         = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER     = '" + _param._semester + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._TEST_KIND + "' ");

        return stb.toString();
    }

    /**選択クラス内で警告点以下の科目を取得**/
    private String sqlKeikokuSubclass()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH SUB_MIN_SCORE AS ( ");
        stb.append("    SELECT RSD.YEAR ");
        stb.append("         , RSD.SEMESTER ");
        stb.append("         , SRD.GRADE ");
        stb.append("         , SRD.HR_CLASS ");
        stb.append("         , RSD.TESTKINDCD ");
        stb.append("         , RSD.TESTITEMCD ");
        stb.append("         , RSD.SCORE_DIV ");
        stb.append("         , RSD.CURRICULUM_CD ");
        stb.append("         , RSD.CHAIRCD ");
        stb.append("         , RSD.SUBCLASSCD ");
        stb.append("         , RSD.CLASSCD ");
        stb.append("         , RSD.SCHOOL_KIND ");
        stb.append("         , SRD.COURSECD ");
        stb.append("         , SRD.MAJORCD ");
        stb.append("         , SRD.COURSECODE ");
        stb.append("         , MIN(RSD.SCORE) AS SCORE ");
        stb.append("      FROM RECORD_SCORE_DAT RSD ");
        stb.append(" LEFT JOIN SCHREG_REGD_DAT SRD ");
        stb.append("        ON RSD.YEAR         = SRD.YEAR ");
        stb.append("       AND RSD.SEMESTER     = SRD.SEMESTER ");
        stb.append("       AND RSD.SCHREGNO     = SRD.SCHREGNO ");
        stb.append("     WHERE RSD.YEAR         = '" + _param._year + "' ");
        stb.append("       AND RSD.SEMESTER     = '" + _param._semester + "' ");
        stb.append("       AND    RSD.TESTKINDCD ");
        stb.append("           || RSD.TESTITEMCD ");
        stb.append("           || RSD.SCORE_DIV = '" + _param._TEST_KIND + "' ");
        stb.append("       AND RSD.SCHREGNO IN ");
        stb.append("           ( ");
        stb.append("            SELECT SCHREGNO ");
        stb.append("              FROM SCHREG_REGD_DAT ");
        stb.append("             WHERE YEAR     = '" + _param._year + "' ");
        stb.append("               AND SEMESTER = '" + _param._semester + "' ");
        stb.append("               AND GRADE || HR_CLASS IN " + SQLUtils.whereIn(true, _param._class) + " ");
        stb.append("           ) ");
        stb.append("  GROUP BY RSD.YEAR ");
        stb.append("         , RSD.SEMESTER ");
        stb.append("         , SRD.GRADE ");
        stb.append("         , SRD.HR_CLASS ");
        stb.append("         , RSD.TESTKINDCD ");
        stb.append("         , RSD.TESTITEMCD ");
        stb.append("         , RSD.SCORE_DIV ");
        stb.append("         , RSD.CURRICULUM_CD ");
        stb.append("         , RSD.SUBCLASSCD ");
        stb.append("         , RSD.CLASSCD ");
        stb.append("         , RSD.SCHOOL_KIND ");
        stb.append("         , SRD.COURSECD ");
        stb.append("         , SRD.MAJORCD ");
        stb.append("         , SRD.COURSECODE ");
        stb.append("         , RSD.CHAIRCD ");
        stb.append("), SRD AS ( ");
        stb.append("    SELECT YEAR ");
        stb.append("         , GRADE ");
        stb.append("         , HR_CLASS ");
        stb.append("         , COURSECD ");
        stb.append("         , MAJORCD ");
        stb.append("         , COURSECODE ");
        stb.append("      FROM SCHREG_REGD_DAT ");
        stb.append("     WHERE YEAR     = '" + _param._year + "' ");
        stb.append("       AND SEMESTER = '" + _param._semester + "' ");
        stb.append("       AND GRADE || HR_CLASS IN " + SQLUtils.whereIn(true, _param._class) + " ");
        stb.append("  GROUP BY YEAR ");
        stb.append("         , GRADE ");
        stb.append("         , HR_CLASS ");
        stb.append("         , COURSECD ");
        stb.append("         , MAJORCD ");
        stb.append("         , COURSECODE ");
        stb.append("  ORDER BY GRADE ");
        stb.append("         , HR_CLASS ");
        stb.append("), A AS ( ");
        stb.append("    SELECT SUB_MIN_SCORE.GRADE ");
        stb.append("         , SUB_MIN_SCORE.HR_CLASS ");
        stb.append("         , SUB_MIN_SCORE.CHAIRCD ");
        stb.append("         , SUB_MIN_SCORE.SUBCLASSCD ");
        stb.append("         , SUB.SUBCLASSABBV ");
        stb.append("         , STM.STAFFNAME  ");
        stb.append("      FROM SUB_MIN_SCORE ");
        stb.append(" LEFT JOIN CHAIR_DETAIL_DAT CDD ");
        stb.append("        ON CDD.YEAR          = SUB_MIN_SCORE.YEAR ");
        stb.append("       AND CDD.SEMESTER      = SUB_MIN_SCORE.SEMESTER ");
        stb.append("       AND CDD.CHAIRCD       = SUB_MIN_SCORE.CHAIRCD ");
        stb.append("       AND CDD.SEQ           = '003' ");
        stb.append(" LEFT JOIN SUBCLASS_MST SUB ");
        stb.append("        ON SUB.SUBCLASSCD    = SUB_MIN_SCORE.SUBCLASSCD ");
        stb.append("       AND SUB.CURRICULUM_CD = SUB_MIN_SCORE.CURRICULUM_CD ");
        stb.append(" LEFT JOIN CHAIR_STF_DAT CSD ");
        stb.append("        ON CSD.YEAR = SUB_MIN_SCORE.YEAR ");
        stb.append("       AND CSD.SEMESTER      = SUB_MIN_SCORE.SEMESTER ");
        stb.append("       AND CSD.CHAIRCD       = SUB_MIN_SCORE.CHAIRCD ");
        stb.append(" LEFT JOIN STAFF_MST STM ");
        stb.append("        ON STM.STAFFCD       = CSD.STAFFCD ");
        stb.append(" LEFT JOIN SCHOOL_DETAIL_DAT SDD ");
        stb.append("        ON SDD.YEAR          = SUB_MIN_SCORE.YEAR ");
        stb.append("       AND SDD.SCHOOLCD      = '000000000000' ");
        stb.append("       AND SDD.SCHOOL_SEQ    = '009' ");
        stb.append(" LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD ");
        stb.append("        ON RASD.YEAR           = SUB_MIN_SCORE.YEAR ");
        stb.append("       AND RASD.SEMESTER       = SUB_MIN_SCORE.SEMESTER ");
        stb.append("       AND RASD.TESTKINDCD     = SUB_MIN_SCORE.TESTKINDCD ");
        stb.append("       AND RASD.TESTITEMCD     = SUB_MIN_SCORE.TESTITEMCD ");
        stb.append("       AND RASD.SCORE_DIV      = SUB_MIN_SCORE.SCORE_DIV ");
        stb.append("       AND RASD.CLASSCD        = SUB_MIN_SCORE.CLASSCD ");
        stb.append("       AND RASD.SCHOOL_KIND    = SUB_MIN_SCORE.SCHOOL_KIND ");
        stb.append("       AND RASD.CURRICULUM_CD  = SUB_MIN_SCORE.CURRICULUM_CD ");
        stb.append("       AND RASD.SUBCLASSCD     = SUB_MIN_SCORE.SUBCLASSCD ");
        stb.append("       AND RASD.AVG_DIV        = '6' ");
        stb.append("       AND RASD.GRADE          = SUB_MIN_SCORE.GRADE ");
        stb.append("       AND RASD.HR_CLASS       = SUB_MIN_SCORE.HR_CLASS ");
        stb.append("       AND RASD.COURSECD       = SUB_MIN_SCORE.COURSECD ");
        stb.append("       AND RASD.MAJORCD        = SUB_MIN_SCORE.MAJORCD ");
        stb.append("       AND RASD.COURSECODE     = SUB_MIN_SCORE.COURSECODE ");
        stb.append("     WHERE CASE SDD.SCHOOL_REMARK1 ");
        stb.append("                WHEN '1' THEN SUB_MIN_SCORE.SCORE <= CDD.REMARK1 ");
        stb.append("                WHEN '2' THEN SUB_MIN_SCORE.SCORE <= RASD.AVG * CDD.REMARK2 / CDD.REMARK3 ");
        stb.append("                ELSE( CASE WHEN RASD.SCORE_DIV = '09' THEN SUB_MIN_SCORE.SCORE <= 1 ");  //評定の場合、警告点は1
        stb.append("                           ELSE SUB_MIN_SCORE.SCORE <= (SELECT ASSESSHIGH FROM ASSESS_MST WHERE ASSESSCD = '2' AND ASSESSLEVEL = '1') ");
        stb.append("                      END) ");
        stb.append("           END ");
        stb.append("       AND SUB.SUBCLASSCD IS NOT NULL ");
        stb.append("  ORDER BY SUB_MIN_SCORE.SUBCLASSCD ");
        stb.append("), B AS ( ");
        stb.append("    SELECT SRD.GRADE ");
        stb.append("         , SRD.HR_CLASS ");
        stb.append("    	 , CRM.SUBCLASSCD ");
        stb.append("    	 , CRM.CREDITS ");
        stb.append("    FROM SRD ");
        stb.append("    LEFT JOIN CREDIT_MST CRM ");
        stb.append("           ON CRM.YEAR        = SRD.YEAR ");
        stb.append("          AND CRM.COURSECD    = SRD.COURSECD ");
        stb.append("          AND CRM.MAJORCD     = SRD.MAJORCD ");
        stb.append("          AND CRM.GRADE       = SRD.GRADE ");
        stb.append("          AND CRM.COURSECODE  = SRD.COURSECODE ");
        stb.append("          AND CRM.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
        stb.append("     ORDER BY GRADE ");
        stb.append("            , HR_CLASS ");
        stb.append("            , CRM.SUBCLASSCD ");
        stb.append(") ");
        stb.append("SELECT B.GRADE ");
        stb.append("     , B.HR_CLASS ");
        stb.append("     , A.CHAIRCD ");
        stb.append("     , A.SUBCLASSCD ");
        stb.append("     , A.SUBCLASSABBV ");
        stb.append("     , A.STAFFNAME ");
        stb.append("     , B.CREDITS ");
        stb.append("FROM A ");
        stb.append("LEFT JOIN B ");
        stb.append("       ON A.GRADE      = B.GRADE ");
        stb.append("      AND A.HR_CLASS   = B.HR_CLASS ");
        stb.append("      AND A.SUBCLASSCD = B.SUBCLASSCD ");
        stb.append(" ORDER BY B.GRADE ");
        stb.append("        , B.HR_CLASS ");
        stb.append("        , A.SUBCLASSCD ");

        return stb.toString();
    }

    /**生徒情報(欠点講座数等含む)を取得**/
    private String sqlStudentData()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH MIJUKEN AS ( ");
        stb.append("   SELECT RSD.SCHREGNO ");
        stb.append("        , COUNT(*) AS MIJUKEN_COUNT ");
        stb.append("     FROM RECORD_SCORE_DAT RSD ");
        stb.append("    WHERE RSD.YEAR             = '" + _param._year + "' ");
        stb.append("      AND RSD.SEMESTER         = '" + _param._semester + "' ");
        stb.append("      AND    RSD.TESTKINDCD ");
        stb.append("          || RSD.TESTITEMCD ");
        stb.append("          || RSD.SCORE_DIV     = '" + _param._TEST_KIND + "' ");
        stb.append("      AND RSD.VALUE_DI         = '*' ");
        stb.append(" GROUP BY RSD.SCHREGNO ");
        stb.append("), KETTEN_KOUZA AS ( ");
        stb.append("   SELECT RSD.SCHREGNO ");
        stb.append("        , COUNT(*) AS KETTEN_COUNT ");
        stb.append("     FROM RECORD_SCORE_DAT RSD ");
        stb.append("LEFT JOIN SCHREG_REGD_DAT SRD ");
        stb.append("       ON SRD.YEAR             = RSD.YEAR ");
        stb.append("      AND SRD.SEMESTER         = RSD.SEMESTER ");
        stb.append("      AND SRD.SCHREGNO         = RSD.SCHREGNO ");
        stb.append("LEFT JOIN CHAIR_DETAIL_DAT CDD ");
        stb.append("       ON CDD.YEAR             = RSD.YEAR ");
        stb.append("      AND CDD.SEMESTER         = RSD.SEMESTER ");
        stb.append("      AND CDD.CHAIRCD          = RSD.CHAIRCD ");
        stb.append("      AND CDD.SEQ              = '003' ");
        stb.append("LEFT JOIN SCHOOL_DETAIL_DAT SDD ");
        stb.append("       ON SDD.YEAR             = RSD.YEAR ");
        stb.append("      AND SDD.SCHOOLCD         = '000000000000' ");
        stb.append("      AND SDD.SCHOOL_SEQ       = '009' ");
        stb.append("LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD ");
        stb.append("       ON RASD.YEAR            = RSD.YEAR ");
        stb.append("      AND RASD.SEMESTER        = RSD.SEMESTER ");
        stb.append("      AND RASD.TESTKINDCD      = RSD.TESTKINDCD ");
        stb.append("      AND RASD.TESTITEMCD      = RSD.TESTITEMCD ");
        stb.append("      AND RASD.SCORE_DIV       = RSD.SCORE_DIV ");
        stb.append("      AND RASD.CLASSCD         = RSD.CLASSCD ");
        stb.append("      AND RASD.SCHOOL_KIND     = RSD.SCHOOL_KIND ");
        stb.append("      AND RASD.CURRICULUM_CD   = RSD.CURRICULUM_CD ");
        stb.append("      AND RASD.SUBCLASSCD      = RSD.SUBCLASSCD ");
        stb.append("      AND RASD.AVG_DIV         = '6' ");
        stb.append("      AND RASD.GRADE           = SRD.GRADE ");
        stb.append("      AND RASD.HR_CLASS        = SRD.HR_CLASS ");
        stb.append("      AND RASD.COURSECD        = SRD.COURSECD ");
        stb.append("      AND RASD.MAJORCD         = SRD.MAJORCD ");
        stb.append("      AND RASD.COURSECODE      = SRD.COURSECODE ");
        stb.append("    WHERE CASE SDD.SCHOOL_REMARK1 ");
        stb.append("               WHEN '1' THEN RSD.SCORE <= CDD.REMARK1 ");
        stb.append("               WHEN '2' THEN RSD.SCORE <= RASD.AVG * CDD.REMARK2 / CDD.REMARK3 ");
        stb.append("               ELSE( CASE WHEN RSD.SCORE_DIV = '09' THEN RSD.SCORE <= 1 ");  //評定の場合、警告点は1
        stb.append("                          ELSE RSD.SCORE <= (SELECT ASSESSHIGH FROM ASSESS_MST WHERE ASSESSCD = '2' AND ASSESSLEVEL = '1') ");
        stb.append("                     END) ");
        stb.append("          END ");
        stb.append("      AND RSD.YEAR             = '" + _param._year + "' ");
        stb.append("      AND RSD.SEMESTER         = '" + _param._semester + "' ");
        stb.append("      AND    RSD.TESTKINDCD ");
        stb.append("          || RSD.TESTITEMCD ");
        stb.append("          || RSD.SCORE_DIV     = '" + _param._TEST_KIND + "' ");
        stb.append(" GROUP BY RSD.SCHREGNO ");
        stb.append("), KETTEN_CREDITS AS ( ");
        stb.append("   SELECT RSD.SCHREGNO ");
        stb.append("        , SUM(CRM.CREDITS) AS CREDITS_TOTAL ");
        stb.append("     FROM RECORD_SCORE_DAT RSD ");
        stb.append("LEFT JOIN CHAIR_DETAIL_DAT CDD ");
        stb.append("       ON CDD.YEAR            = RSD.YEAR ");
        stb.append("      AND CDD.SEMESTER        = RSD.SEMESTER ");
        stb.append("      AND CDD.CHAIRCD         = RSD.CHAIRCD ");
        stb.append("      AND CDD.SEQ             = '003' ");
        stb.append("LEFT JOIN SCHREG_REGD_DAT SRD ");
        stb.append("       ON SRD.SCHREGNO        = RSD.SCHREGNO ");
        stb.append("      AND SRD.YEAR            = RSD.YEAR ");
        stb.append("      AND SRD.SEMESTER = RSD.SEMESTER ");
        stb.append("LEFT JOIN CREDIT_MST CRM ");
        stb.append("       ON CRM.YEAR            = RSD.YEAR ");
        stb.append("      AND CRM.COURSECD        = SRD.COURSECD ");
        stb.append("      AND CRM.MAJORCD         = SRD.MAJORCD ");
        stb.append("      AND CRM.GRADE           = SRD.GRADE ");
        stb.append("      AND CRM.COURSECODE      = SRD.COURSECODE ");
        stb.append("      AND CRM.CLASSCD         = RSD.CLASSCD ");
        stb.append("      AND CRM.SCHOOL_KIND     = RSD.SCHOOL_KIND ");
        stb.append("      AND CRM.CURRICULUM_CD   = RSD.CURRICULUM_CD ");
        stb.append("      AND CRM.SUBCLASSCD      = RSD.SUBCLASSCD ");
        stb.append("LEFT JOIN SCHOOL_DETAIL_DAT SDD ");
        stb.append("       ON SDD.YEAR             = RSD.YEAR ");
        stb.append("      AND SDD.SCHOOLCD         = '000000000000' ");
        stb.append("      AND SDD.SCHOOL_SEQ       = '009' ");
        stb.append("LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD ");
        stb.append("       ON RASD.YEAR            = RSD.YEAR ");
        stb.append("      AND RASD.SEMESTER        = RSD.SEMESTER ");
        stb.append("      AND RASD.TESTKINDCD      = RSD.TESTKINDCD ");
        stb.append("      AND RASD.TESTITEMCD      = RSD.TESTITEMCD ");
        stb.append("      AND RASD.SCORE_DIV       = RSD.SCORE_DIV ");
        stb.append("      AND RASD.CLASSCD         = RSD.CLASSCD ");
        stb.append("      AND RASD.SCHOOL_KIND     = RSD.SCHOOL_KIND ");
        stb.append("      AND RASD.CURRICULUM_CD   = RSD.CURRICULUM_CD ");
        stb.append("      AND RASD.SUBCLASSCD      = RSD.SUBCLASSCD ");
        stb.append("      AND RASD.AVG_DIV         = '6' ");
        stb.append("      AND RASD.GRADE           = SRD.GRADE ");
        stb.append("      AND RASD.HR_CLASS        = SRD.HR_CLASS ");
        stb.append("      AND RASD.COURSECD        = SRD.COURSECD ");
        stb.append("      AND RASD.MAJORCD         = SRD.MAJORCD ");
        stb.append("      AND RASD.COURSECODE      = SRD.COURSECODE ");
        stb.append("    WHERE RSD.YEAR            = '" + _param._year + "' ");
        stb.append("      AND RSD.SEMESTER        = '" + _param._semester + "' ");
        stb.append("      AND    RSD.TESTKINDCD ");
        stb.append("          || RSD.TESTITEMCD ");
        stb.append("          || RSD.SCORE_DIV    = '" + _param._TEST_KIND + "' ");
        stb.append("      AND CASE SDD.SCHOOL_REMARK1 ");
        stb.append("               WHEN '1' THEN RSD.SCORE <= CDD.REMARK1 ");
        stb.append("               WHEN '2' THEN RSD.SCORE <= RASD.AVG * CDD.REMARK2 / CDD.REMARK3 ");
        stb.append("               ELSE( CASE WHEN RSD.SCORE_DIV = '09' THEN RSD.SCORE <= 1 ");  //評定の場合、警告点は1
        stb.append("                          ELSE RSD.SCORE <= (SELECT ASSESSHIGH FROM ASSESS_MST WHERE ASSESSCD = '2' AND ASSESSLEVEL = '1') ");
        stb.append("                     END) ");
        stb.append("          END ");
        stb.append(" GROUP BY RSD.SCHREGNO ");
        stb.append(") ");
        stb.append("   SELECT SRH.GRADE ");
        stb.append("        , SRH.HR_CLASS ");
        stb.append("        , SRH.HR_CLASS_NAME2 ");
        stb.append("        , SRD.ATTENDNO ");
        stb.append("        , SBM.NAME ");
        stb.append("        , SBM.SCHREGNO ");
        stb.append("        , MIJUKEN.MIJUKEN_COUNT ");
        stb.append("        , KETTEN_KOUZA.KETTEN_COUNT ");
        stb.append("        , KETTEN_CREDITS.CREDITS_TOTAL ");
        stb.append("     FROM SCHREG_REGD_DAT SRD ");
        stb.append("LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("       ON SRH.YEAR                        = SRD.YEAR ");
        stb.append("      AND SRH.SEMESTER                    = SRD.SEMESTER ");
        stb.append("      AND SRH.GRADE                       = SRD.GRADE ");
        stb.append("      AND SRH.HR_CLASS                    = SRD.HR_CLASS ");
        stb.append("LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("       ON SBM.SCHREGNO                    = SRD.SCHREGNO ");
        stb.append("LEFT JOIN MIJUKEN ");
        stb.append("       ON MIJUKEN.SCHREGNO                = SRD.SCHREGNO ");
        stb.append("LEFT JOIN KETTEN_KOUZA ");
        stb.append("       ON KETTEN_KOUZA.SCHREGNO           = SRD.SCHREGNO ");
        stb.append("LEFT JOIN KETTEN_CREDITS ");
        stb.append("       ON KETTEN_CREDITS.SCHREGNO = SRD.SCHREGNO ");
        stb.append("    WHERE SRD.YEAR                  = '" + _param._year + "' ");
        stb.append("      AND SRD.SEMESTER              = '" + _param._semester + "' ");
        stb.append("      AND KETTEN_KOUZA.KETTEN_COUNT IS NOT NULL ");
        stb.append("      AND SRD.GRADE || SRD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._class) + " ");
        stb.append(" ORDER BY SRH.GRADE ");
        stb.append("        , SRH.HR_CLASS_NAME1 ");
        stb.append("        , SRD.ATTENDNO ");


        return stb.toString();
    }

    /**生徒の科目ごとの点数を取得(未受検か警告点以下のデータのみ)**/
    private String sqlSubclassScore() {
        final StringBuffer stb = new StringBuffer();
        stb.append("  SELECT SRD.SCHREGNO ");
        stb.append("        , RSD.SUBCLASSCD ");
        stb.append("        , RSD.SCORE ");
        stb.append("        , RSD.VALUE_DI ");
        stb.append("     FROM SCHREG_REGD_DAT  SRD ");
        stb.append("LEFT JOIN RECORD_SCORE_DAT RSD ");
        stb.append("       ON RSD.YEAR             = SRD.YEAR ");
        stb.append("      AND RSD.SEMESTER         = SRD.SEMESTER ");
        stb.append("      AND RSD.SCHREGNO         = SRD.SCHREGNO ");
        stb.append("LEFT JOIN CHAIR_DETAIL_DAT CDD ");
        stb.append("       ON CDD.YEAR             = SRD.YEAR ");
        stb.append("      AND CDD.SEMESTER         = SRD.SEMESTER ");
        stb.append("      AND CDD.CHAIRCD          = RSD.CHAIRCD ");
        stb.append("      AND CDD.SEQ              = '003' ");
        stb.append("LEFT JOIN SCHOOL_DETAIL_DAT SDD ");
        stb.append("       ON SDD.YEAR             = RSD.YEAR ");
        stb.append("      AND SDD.SCHOOLCD         = '000000000000' ");
        stb.append("      AND SDD.SCHOOL_SEQ       = '009' ");
        stb.append("LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD ");
        stb.append("       ON RASD.YEAR            = RSD.YEAR ");
        stb.append("      AND RASD.SEMESTER        = RSD.SEMESTER ");
        stb.append("      AND RASD.TESTKINDCD      = RSD.TESTKINDCD ");
        stb.append("      AND RASD.TESTITEMCD      = RSD.TESTITEMCD ");
        stb.append("      AND RASD.SCORE_DIV       = RSD.SCORE_DIV ");
        stb.append("      AND RASD.CLASSCD         = RSD.CLASSCD ");
        stb.append("      AND RASD.SCHOOL_KIND     = RSD.SCHOOL_KIND ");
        stb.append("      AND RASD.CURRICULUM_CD   = RSD.CURRICULUM_CD ");
        stb.append("      AND RASD.SUBCLASSCD      = RSD.SUBCLASSCD ");
        stb.append("      AND RASD.AVG_DIV         = '6' ");
        stb.append("      AND RASD.GRADE           = SRD.GRADE ");
        stb.append("      AND RASD.HR_CLASS        = SRD.HR_CLASS ");
        stb.append("      AND RASD.COURSECD        = SRD.COURSECD ");
        stb.append("      AND RASD.MAJORCD         = SRD.MAJORCD ");
        stb.append("      AND RASD.COURSECODE      = SRD.COURSECODE ");
        stb.append("    WHERE SRD.YEAR             = '" + _param._year + "' ");
        stb.append("      AND SRD.SEMESTER         = '" + _param._semester + "' ");
        stb.append("      AND    RSD.TESTKINDCD  ");
        stb.append("          || RSD.TESTITEMCD  ");
        stb.append("          || RSD.SCORE_DIV     = '" + _param._TEST_KIND + "' ");
        stb.append("      AND SRD.GRADE || SRD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._class) + " ");
        stb.append("      AND (   CASE SDD.SCHOOL_REMARK1 ");
        stb.append("                   WHEN '1' THEN RSD.SCORE <= CDD.REMARK1 ");
        stb.append("                   WHEN '2' THEN RSD.SCORE <= RASD.AVG * CDD.REMARK2 / CDD.REMARK3 ");
        stb.append("                   ELSE( CASE WHEN RSD.SCORE_DIV = '09' THEN RSD.SCORE <= 1 ");  //評定の場合、警告点は1
        stb.append("                              ELSE RSD.SCORE <= (SELECT ASSESSHIGH FROM ASSESS_MST WHERE ASSESSCD = '2' AND ASSESSLEVEL = '1') ");
        stb.append("                         END) ");
        stb.append("              END ");
        stb.append("           OR RSD.VALUE_DI = '*'");
        stb.append("          )");
        stb.append(" ORDER BY SRD.SCHREGNO ");
        stb.append("        , RSD.SUBCLASSCD ");

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
        private final String _documentRoot;
        private String _imagepath;
        private String _extension;
        final boolean _isOutputDebug;
        private final String _semester;
        private final String _SCHOOL_KIND;
        private final String _TEST_KIND;
        private final String _schoolcd;
        private final String[] _class;


        public Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year          = request.getParameter("YEAR");                   //年度
            _semester      = request.getParameter("SEMESTER");               //学期
            _SCHOOL_KIND   = request.getParameter("SCHOOLKIND");             //学校種別
            _TEST_KIND     = request.getParameter("TESTKINDCD");             //テスト種別
            _schoolcd      = request.getParameter("SCHOOLCD");               //学校CD
            _class         = request.getParameterValues("CLASS_SELECTED");   //選択クラス

            _documentRoot  = request.getParameter("DOCUMENTROOT");

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD157D' AND NAME = '" + propName + "' "));
        }
    }

}//クラスの括り

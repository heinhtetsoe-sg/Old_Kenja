package servletpack.KNJM;

import java.io.OutputStream;
import java.util.HashMap;
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
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＭ４３２＞  成績一覧表
 *
 *  2005/09/15 m-yama 作成日
 *  @version $Id: f98c2614cf51a4acee2da3b7a303da1cd0f81fae $
 */

public class KNJM432 {

    private static final Log log = LogFactory.getLog(KNJM432.class);

    /**
     * KNJM.classから最初に呼ばれる処理。
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception IO例外
     */
    // CSOFF: ExecutableStatementCount
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        Vrw32alp svf = null;
        OutputStream outstrm = null;
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        PreparedStatement ps1 = null;
        PreparedStatement ps5 = null;
        try {
            svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            final String[] param = new String[12];

            //  パラメータの取得
            final String[] classcd = request.getParameterValues("CATEGORY_NAME"); //クラス
            param[0] = request.getParameter("YEAR"); //年度
            param[1] = request.getParameter("SEMESTER"); //学期
            param[11] = request.getParameter("useCurriculumcd"); //教育課程

            //  print設定
            response.setContentType("application/pdf");
            outstrm = response.getOutputStream();

            //  svf設定
            svf.VrInit(); //クラスの初期化
            svf.VrSetSpoolFileStream(outstrm); //PDFファイル名の設定

            //  ＤＢ接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            //  ＳＶＦ作成処理
            boolean nonedata = false; //該当データなしフラグ
            //SQL作成
            ps1 = db2.prepareStatement(preStat1(param)); //生徒及び公欠・欠席者
            ps5 = db2.prepareStatement(preStat5(param)); //HR出席数

            //SVF出力
            for (int ia = 0; ia < classcd.length; ia++) {
                if (mainProcess(db2, svf, param, classcd[ia], ps1, ps5)) {
                    nonedata = true;
                }
            }

            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            if (null != svf) {
                svf.VrQuit();
            }
            DbUtils.closeQuietly(ps1);
            DbUtils.closeQuietly(ps5);
            if (null != db2) {
                db2.commit();
                db2.close(); //DBを閉じる
            }
            if (null != outstrm) {
                outstrm.close(); //ストリームを閉じる
            }
        }


    } //doGetの括り
    // CSON: ExecutableStatementCount



    /** SVF-FORM **/
    private void setHead(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String[] param,
            final String classcd
    ) throws Exception {

        final KNJ_Get_Info getinfo = new KNJ_Get_Info();

        svf.VrSetForm("KNJM432.frm", 4);              //共通フォーム
        svf.VrsOut("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度"); //年度

        //  作成日(現在処理日)の取得
        final KNJ_Get_Info.ReturnVal dateInfo = getinfo.Control(db2);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_thi(dateInfo.val3, 0));       //作成日

        //  学期名称の取得
        final KNJ_Get_Info.ReturnVal semeInfo = getinfo.Semester(db2, param[0], param[1]);
        svf.VrsOut("SEMESTER", semeInfo.val1);   //学期名称

        //クラス名称の取得
        ResultSet rs = null;
        try {
            final String sql = "SELECT HR_NAME FROM SCHREG_REGD_HDAT WHERE GRADE || HR_CLASS = '" + classcd + "' ";
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                svf.VrsOut("HR_NAME", rs.getString("HR_NAME"));  //クラス名称
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }

        for (int ia = 0; ia < param.length; ia++) { log.debug("param[" + ia + "]=" + param[ia]); }

    } //Set_Head()の括り



    /** メイン処理 **/
    // CSOFF: ExecutableStatementCount|MethodLength
    private boolean mainProcess(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String[] param,
            final String classcd,
            final PreparedStatement ps1,
            final PreparedStatement ps5
    ) throws Exception {
        boolean nonedata = false;
        int hr_attendcnt = 0;
        ResultSet rs = null;
        try {
            int pp = 1;
            ps1.setString(pp++, classcd);                                    //生徒抽出
            rs = ps1.executeQuery();

            final Map hm1 = new HashMap();              //学籍番号と行番号
            final Map hm2 = new HashMap();              //学籍番号とHR出席数
            final Map hmMtCredit = new HashMap();       //学籍番号と既修得単位数
            final Map hmSdCredit = new HashMap();       //学籍番号と予定単位数
            final Map hmAqCredit = new HashMap();       //学籍番号と習得単位数
            final Map hmTtCredit = new HashMap();       //学籍番号と合計単位数
            int schno = 0;
            final String[] scharay = new String[55];
            while (rs.next()) {
                schno++;
                hm1.put(rs.getString("SCHREGNO"), new Integer(schno));     //学籍番号に行番号を付ける
                if (schno == 1) {
                    setHead(db2, svf, param, classcd);                        //見出し出力のメソッド
                    param[7] = rs.getString("SCHREGNO");                    //開始生徒
                    param[5] = "('" + rs.getString("SCHREGNO") + "'";
                } else {
                    param[5] = param[5] + ",'" + rs.getString("SCHREGNO") + "'";
                }
                //HR出席数
                ps5.setString(1, rs.getString("SCHREGNO"));  //学籍番号
                final ResultSet rs5 = ps5.executeQuery();
                while (rs5.next()) {
                    hr_attendcnt++;
                }
                //既修得単位数取得
                final int mtcredit = getCredit(db2, param, rs.getString("SCHREGNO"), "MASTER");
                hmMtCredit.put(rs.getString("SCHREGNO"), new Integer(mtcredit));        //学籍番号に既修得単位数を付ける

                //予定単位数取得
                final int sdcredit = getCredit(db2, param, rs.getString("SCHREGNO"), "SCHEDULE");
                hmSdCredit.put(rs.getString("SCHREGNO"), new Integer(sdcredit));        //学籍番号に既修得単位数を付ける

                //習得単位数取得
                final int aqcredit = getCredit(db2, param, rs.getString("SCHREGNO"), "ACQUISITION");
                hmAqCredit.put(rs.getString("SCHREGNO"), new Integer(aqcredit));        //学籍番号に既修得単位数を付ける

                //合計単位数取得(既修得単位＋習得単位)
                final int ttcredit = mtcredit + aqcredit;
                hmTtCredit.put(rs.getString("SCHREGNO"), new Integer(ttcredit));        //学籍番号に既修得単位数を付ける

                rs5.close();
                hm2.put(rs.getString("SCHREGNO"), new Integer(hr_attendcnt));        //学籍番号にHR出席数を付ける
                hr_attendcnt = 0;
                setSchData(svf, rs, schno);                              //生徒名等出力のメソッド
                param[8] = rs.getString("ATTENDNO");                        //終了生徒
                scharay[schno - 1] = rs.getString("SCHREGNO");
                if (schno == 55) {
                    param[5] = param[5] + ")";
                    //印字メイン
                    if (printMain(db2, svf, param, hm1, hm2, hmMtCredit, hmSdCredit, hmAqCredit, hmTtCredit, classcd, schno, scharay)) {
                        nonedata = true;
                    }
                    hm1.clear();                                            //行番号情報を削除
                    schno = 0;
                    param[7] = null;                                        //開始生徒
                    param[8] = null;                                        //終了生徒
                }
            }
            if (schno > 0) {
                param[5] = param[5] + ")";
                //印字メイン
                if (printMain(db2, svf, param, hm1, hm2, hmMtCredit, hmSdCredit, hmAqCredit, hmTtCredit, classcd, schno, scharay)) {
                    nonedata = true;
                }
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return nonedata;

    } //boolean mainProcess()の括り
    // CSON: ExecutableStatementCount|MethodLength

    /** 生徒名等出力 **/
    private void setSchData(final Vrw32alp svf, final ResultSet rs, final int ia) throws SQLException {

        svf.VrsOutn("ATTENDNO", ia, rs.getString("ATTENDNO"));             //出席番号
        svf.VrsOutn("SCHREGNO", ia, rs.getString("SCHREGNO"));             //学籍番号
        svf.VrsOutn("NAME"    , ia, rs.getString("NAME_SHOW"));            //生徒名

    } //Set_SchData()の括り

    /**
     * 既修得単位数取得
     * @param db2
     * @param param
     * @param schno
     * @param sqlpattern
     * @return
     * @throws Exception
     */
    private int getCredit(
            final DB2UDB db2,
            final String[] param,
            final String schno,
            final String sqlpattern
    ) throws Exception {

        int credit = 0;
        ResultSet rsGetCredit = null;
        try {
            if (sqlpattern.equals("MASTER")) {
                //既修得単位数
                db2.query(sqlGetCredit(param, schno));
            } else if (sqlpattern.equals("SCHEDULE")) {
                //予定単位数
                db2.query(sqlGetSdCredit(param, schno));
            } else {
                //習得単位数
                db2.query(sqlGetAqCredit(param, schno));
            }
            rsGetCredit = db2.getResultSet();
            while (rsGetCredit.next()) {
                credit = rsGetCredit.getInt("CREDIT_CNT");
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsGetCredit);
        }

        return credit;

    }

    /** 印字メイン */
    // CSOFF: ExecutableStatementCount
    // CSOFF: ParameterNumber
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String[] param,
            final Map hm1,
            final Map hm2,
            final Map hmMtCredit,
            final Map hmSdCredit,
            final Map hmAqCredit,
            final Map hmTtCredit,
            final String classcd,
            final int schno,
            final String[] scharay
    ) throws Exception {
        // CSON: ParameterNumber
        boolean nonedata = false;
        final PreparedStatement ps2 = db2.prepareStatement(preStat2(param)); //科目
        ResultSet rs = null; //クラスの生徒が選択している科目
        try {
            int pp = 1;
            ps2.setString(pp++, classcd); //クラス
            rs = ps2.executeQuery(); //クラスの生徒が選択している科目
            int lcount = 0; //列出力カウント]
            //科目でループ
            while (rs.next()) {

                if (lcount == 0) {
                    //固定フィールド印字
                    printFixationFieldData(svf, hm1, hmMtCredit, "MASTER_CREDIT", schno, scharay);
                    printFixationFieldData(svf, hm1, hmSdCredit, "SCHEDULE_CREDIT", schno, scharay);
                }

                svf.VrsOut("CLASS", rs.getString("CLASSABBV"));
                setSubclassDeta(db2, svf, param, hm1, classcd, rs.getString("SUBCLASSCD")); //列レコードセットのメソッド
                setCredit(db2, svf, param, classcd, rs.getString("SUBCLASSCD"));
                lcount++;

                svf.VrsOut("SUBCLASS", rs.getString("SUBCLASSABBV"));
                svf.VrsOut("CREDIT", param[6]);
                svf.VrEndRecord();
                if (lcount == 22) {
                    //固定フィールド印字
                    printFixationFieldData(svf, hm1, hm2, "HR_ATTEND", schno, scharay);
                    printFixationFieldData(svf, hm1, hmAqCredit, "GET_CREDIT", schno, scharay);
                    printFixationFieldData(svf, hm1, hmTtCredit, "TOTAL_CREDIT", schno, scharay);
                    lcount = 0;
                }
                nonedata = true;
            } //while End
            //最終列出力
            if (lcount > 0) {
                for (int meidocnt = lcount; meidocnt < 22; meidocnt++) {
                    svf.VrAttribute("CLASS", "Meido=100");
                    svf.VrsOut("CLASS", String.valueOf(meidocnt));
                    svf.VrEndRecord();
                }
                //固定フィールド印字
                printFixationFieldData(svf, hm1, hm2, "HR_ATTEND", schno, scharay);
                printFixationFieldData(svf, hm1, hmAqCredit, "GET_CREDIT", schno, scharay);
                printFixationFieldData(svf, hm1, hmTtCredit, "TOTAL_CREDIT", schno, scharay);
                lcount = 0;
            }
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps2);
            db2.commit();
        }

        return nonedata;

    } //boolean printMain()の括り
    // CSON: ExecutableStatementCount

    /** 固定フィールド印字処理 */
    private void printFixationFieldData(
            final Vrw32alp svf,
            final Map mpLine,
            final Map mpData,
            final String fieldName,
            final int schno,
            final String[] scharay
    ) {
        for (int hrcnt = 0; hrcnt < schno; hrcnt++) {
            final Integer iLine = (Integer) mpLine.get(scharay[hrcnt]);
            final Integer iData = (Integer) mpData.get(scharay[hrcnt]);
            svf.VrsOutn(fieldName, iLine.intValue(), String.valueOf(iData.intValue()));
        }
        svf.VrEndRecord();
    }

    /** 単位取得 */
    private void setCredit(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String[] param,
            final String classcd,
            final String subclass
    ) throws Exception {

        final PreparedStatement ps4 = db2.prepareStatement(preStat4(param));
        ResultSet rs = null; //科目単位
        try {
            //クラス名称の取得
            int pp = 1;
            ps4.setString(pp++, param[7]); //最初の生徒
            ps4.setString(pp++, classcd.substring(0, 2)); //学年
            ps4.setString(pp++, "1".equals(param[11]) ? subclass.substring(4, 6) : subclass.substring(0, 2)); //教科コード
            ps4.setString(pp++, subclass); //科目コード
            log.debug("生徒 = " + param[7] + "学年 = " + classcd.substring(0, 2) + "教科 = " + subclass.substring(0, 2) + "教科 = " + subclass);
            rs = ps4.executeQuery(); //科目単位
            while (rs.next()) {
                param[6] = rs.getString("CREDITS"); //単位
            }
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps4);
            db2.commit();
        }

    } //Set_Credit()の括り

    /** 科目単位でのデータ出力 */
    private void setSubclassDeta(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String[] param,
            final Map hm1,
            final String classcd,
            final String subcd
    ) throws Exception {

        final PreparedStatement ps3 = db2.prepareStatement(preStat3(param)); //科目
        ResultSet rs = null; //クラスの生徒が選択している科目
        try {
            int pp = 1;
            ps3.setString(pp++, subcd); //科目コード
            rs = ps3.executeQuery(); //クラスの生徒が選択している科目
            while (rs.next()) {
                final Integer int1 = (Integer) hm1.get(rs.getString("SCHREGNO"));
                svf.VrsOutn("POINT", int1.intValue(), rs.getString("SETPOINT"));
                svf.VrsOutn("VALUE", int1.intValue(), rs.getString("SETVALUE"));
            }
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps3);
            db2.commit();
        }

    } //Set_SubclassDeta()の括り

    /** 生徒抽出 */
    private String preStat1(final String[] param) {

        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    t1.ATTENDNO,t1.SCHREGNO,t2.NAME_SHOW ");
        stb.append("FROM ");
        stb.append("    SCHREG_REGD_DAT t1 ");
        stb.append("    LEFT JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO ");
        stb.append("WHERE ");
        stb.append("    t1.YEAR = '" + param[0] + "' ");
        stb.append("    AND t1.SEMESTER = '" + param[1] + "' ");
        stb.append("    AND t1.GRADE || HR_CLASS = ? ");
        stb.append("ORDER BY ");
        stb.append("    t1.ATTENDNO ");

        return stb.toString();

    } //Pre_Stat1()の括り

    /** 科目抽出 */
    private String preStat2(final String[] param) {
        final StringBuffer stb = new StringBuffer();
        //対象生徒
        stb.append("WITH SCHTBL AS ( ");
        stb.append("SELECT ");
        stb.append("    t1.SCHREGNO,t1.ATTENDNO,t2.NAME_SHOW ");
        stb.append("FROM ");
        stb.append("    SCHREG_REGD_DAT t1 ");
        stb.append("    LEFT JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO ");
        stb.append("WHERE ");
        stb.append("    t1.YEAR = '" + param[0] + "' ");
        stb.append("    AND t1.SEMESTER = '" + param[1] + "' ");
        stb.append("    AND t1.GRADE || HR_CLASS = ? ");
        stb.append(") ");
        //メイン
        stb.append("SELECT ");
        stb.append("    t1.CHAIRCD,s2.CLASSCD,s2.CLASSABBV, ");
        if ("1".equals(param[11])) {
            stb.append("    s1.CLASSCD || s1.SCHOOL_KIND || s1.CURRICULUM_CD || s1.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("    s1.SUBCLASSCD, ");
        }
        stb.append("    s1.SUBCLASSABBV ");
        stb.append("FROM ");
        stb.append("    CHAIR_STD_DAT t1 ");
        stb.append("    LEFT JOIN CHAIR_DAT c1 ON c1.YEAR = t1.YEAR ");
        stb.append("    AND c1.SEMESTER = '1' ");
        stb.append("    AND c1.CHAIRCD = t1.CHAIRCD ");
        stb.append("    LEFT JOIN SUBCLASS_MST s1 ON s1.SUBCLASSCD = c1.SUBCLASSCD ");
        if ("1".equals(param[11])) {
            stb.append("    AND s1.CLASSCD = c1.CLASSCD ");
            stb.append("    AND s1.SCHOOL_KIND = c1.SCHOOL_KIND ");
            stb.append("    AND s1.CURRICULUM_CD = c1.CURRICULUM_CD ");
        }
        stb.append("    LEFT JOIN CLASS_MST s2 ON s2.CLASSCD = SUBSTR(s1.SUBCLASSCD,1,2) ");
        if ("1".equals(param[11])) {
            stb.append("    AND s2.SCHOOL_KIND = s1.SCHOOL_KIND ");
        }
        stb.append("    , SCHTBL t2 ");
        stb.append("WHERE ");
        stb.append("    t1.YEAR = '" + param[0] + "' ");
        stb.append("    AND t1.SEMESTER = '" + param[1] + "' ");
        stb.append("    AND t1.CHAIRCD NOT LIKE '92%' ");
        stb.append("    AND t1.SCHREGNO IN t2.SCHREGNO ");
        stb.append("GROUP BY ");
        stb.append("    t1.CHAIRCD,s2.CLASSCD,s2.CLASSABBV, ");
        if ("1".equals(param[11])) {
            stb.append("    s1.CLASSCD || s1.SCHOOL_KIND || s1.CURRICULUM_CD || s1.SUBCLASSCD, ");
        } else {
            stb.append("    s1.SUBCLASSCD, ");
        }
        stb.append("    s1.SUBCLASSABBV ");

        return stb.toString();

    } //Pre_Stat2()の括り

    /** 成績抽出 */
    private String preStat3(final String[] param) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    SCHREGNO, ");
        if (param[1].equals("1")) {
            stb.append("    SEM1_TERM_SCORE AS SETPOINT, ");
            stb.append("    SEM1_VALUE AS SETVALUE ");
        } else {
            stb.append("    SEM2_TERM_SCORE AS SETPOINT, ");
            stb.append("    GRAD_VALUE AS SETVALUE ");
        }
        stb.append("FROM ");
        stb.append("    RECORD_DAT ");
        stb.append("WHERE ");
        stb.append("    YEAR = '" + param[0] + "' ");
        if ("1".equals(param[11])) {
            stb.append("    AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = ? ");
        } else {
            stb.append("    AND SUBCLASSCD = ? ");
        }
        stb.append("    AND TAKESEMES = '0' ");
        stb.append("    AND SCHREGNO IN " + param[5] + " ");
//log.debug(stb);
        return stb.toString();

    } //Pre_Stat3()の括り

    /** 単位抽出 */
    private String preStat4(final String[] param) {
        final StringBuffer stb = new StringBuffer();
        //対象生徒
        stb.append("WITH SCHTBL AS ( ");
        stb.append("SELECT ");
        stb.append("    SCHREGNO,COURSECD,MAJORCD,COURSECODE ");
        stb.append("FROM ");
        stb.append("    SCHREG_REGD_DAT ");
        stb.append("WHERE ");
        stb.append("    YEAR = '" + param[0] + "' ");
        stb.append("    AND SEMESTER = '" + param[1] + "' ");
        stb.append("    AND SCHREGNO = ? ");
        stb.append(") ");
        //メイン
        stb.append("SELECT ");
        stb.append("    t1.CREDITS ");
        stb.append("FROM ");
        stb.append("    CREDIT_MST t1,SCHTBL t2 ");
        stb.append("WHERE ");
        stb.append("    t1.YEAR = '" + param[0] + "' ");
        stb.append("    AND t1.COURSECD = t2.COURSECD ");
        stb.append("    AND t1.MAJORCD = t2.MAJORCD ");
        stb.append("    AND t1.GRADE = ? ");
        stb.append("    AND t1.COURSECODE = t2.COURSECODE ");
        stb.append("    AND t1.CLASSCD = ? ");
        if ("1".equals(param[11])) {
            stb.append("    AND t1.CLASSCD || t1.SCHOOL_KIND || t1.CURRICULUM_CD || t1.SUBCLASSCD = ? ");
        } else {
            stb.append("    AND t1.SUBCLASSCD = ? ");
        }

        return stb.toString();

    } //Pre_Stat3()の括り

    /** HR出席数 **/
    private String preStat5(final String[] param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     EXECUTEDATE, PERIODCD ");
        stb.append(" FROM ");
        stb.append("     HR_ATTEND_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + param[0] + "' AND ");
        stb.append("     SCHREGNO = ? AND ");
//        stb.append("     EXECUTEDATE <= '" + param[4].replace('/', '-') + "' AND ");
        stb.append("     CHAIRCD LIKE '92%' ");
        stb.append(" GROUP BY ");
        stb.append("     EXECUTEDATE, PERIODCD ");
        //log.debug(stb);
        return stb.toString();

    } //Pre_Stat5()の括り

    /** 既修得単位数を取得 */
    private String sqlGetCredit(final String[] param, final String schno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SCHREGNO,sum(value(GET_CREDIT,0)) + sum(value(ADD_CREDIT,0)) as CREDIT_CNT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT ");
        stb.append(" WHERE ");
        stb.append("     SCHREGNO = '" + schno + "' ");
        stb.append("     AND YEAR < '" + param[0] + "' ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");

        return stb.toString();
    } //sqlGetCredit()の括り

    /** 予定単位数を取得 */
    // CSOFF: ExecutableStatementCount
    private String sqlGetSdCredit(final String[] param, final String schno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHTBL AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     t1.COURSECD, ");
        stb.append("     t1.MAJORCD, ");
        stb.append("     t1.COURSECODE, ");
        stb.append("     t1.GRADE, ");
        stb.append("     l1.CHAIRCD, ");
        if ("1".equals(param[11])) {
            stb.append("     l2.CLASSCD, ");
            stb.append("     l2.SCHOOL_KIND, ");
            stb.append("     l2.CURRICULUM_CD, ");
        } else {
            stb.append("     SUBSTR(l2.SUBCLASSCD,1,2) AS CLASSCD, ");
        }
        stb.append("     l2.SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT t1 ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT DISTINCT ");
        stb.append("             SCHREGNO, ");
        stb.append("             CHAIRCD ");
        stb.append("         FROM ");
        stb.append("             CHAIR_STD_DAT ");
        stb.append("         WHERE ");
        stb.append("             YEAR = '" + param[0] + "' ");
        stb.append("             AND SEMESTER = '" + param[1] + "' ");
        stb.append("             AND SCHREGNO = '" + schno + "' ");
        stb.append("             AND CHAIRCD NOT LIKE '92%' ");
        stb.append("     ) l1 ON l1.SCHREGNO = t1.SCHREGNO ");
        stb.append("     LEFT JOIN CHAIR_DAT l2 ON l2.YEAR = t1.YEAR ");
        stb.append("     AND l2.SEMESTER = t1.SEMESTER ");
        stb.append("     AND l2.CHAIRCD = l1.CHAIRCD ");
        stb.append(" WHERE ");
        stb.append("     t1.YEAR = '" + param[0] + "' ");
        stb.append("     AND t1.SEMESTER = '" + param[1] + "' ");
        stb.append("     AND t1.SCHREGNO = '" + schno + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SUM(t1.CREDITS) AS CREDIT_CNT ");
        stb.append(" FROM ");
        stb.append("     CREDIT_MST t1,SCHTBL t2 ");
        stb.append(" WHERE ");
        stb.append("     t1.YEAR = '" + param[0] + "' ");
        stb.append("     AND t1.COURSECD = t2.COURSECD ");
        stb.append("     AND t1.MAJORCD = t2.MAJORCD ");
        stb.append("     AND t1.GRADE = t2.GRADE ");
        stb.append("     AND t1.COURSECODE = t2.COURSECODE ");
        stb.append("     AND t1.CLASSCD = t2.CLASSCD ");
        stb.append("     AND t1.SUBCLASSCD = t2.SUBCLASSCD ");
        if ("1".equals(param[11])) {
            stb.append("     AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ");
            stb.append("     AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ");
        }

        return stb.toString();
    }
    // CSON: ExecutableStatementCount

    /** 習得単位数を取得 */
    // CSOFF: ExecutableStatementCount|MethodLength
    private String sqlGetAqCredit(final String[] param, final String schno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHTBL AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     t1.COURSECD, ");
        stb.append("     t1.MAJORCD, ");
        stb.append("     t1.COURSECODE, ");
        stb.append("     t1.GRADE, ");
        stb.append("     l1.CHAIRCD, ");
        if ("1".equals(param[11])) {
            stb.append("     l2.CLASSCD, ");
            stb.append("     l2.SCHOOL_KIND, ");
            stb.append("     l2.CURRICULUM_CD, ");
        } else {
            stb.append("     SUBSTR(l2.SUBCLASSCD,1,2) AS CLASSCD, ");
        }
        stb.append("     l2.SUBCLASSCD, ");
        stb.append("     CASE WHEN VALUE(l3.GRAD_VALUE,0) BETWEEN 2 AND 5 ");
        stb.append("          THEN 1 ");
        stb.append("          ELSE 0 END AS GRAD_VALUE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT t1 ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT DISTINCT ");
        stb.append("             SCHREGNO, ");
        stb.append("             CHAIRCD ");
        stb.append("         FROM ");
        stb.append("             CHAIR_STD_DAT ");
        stb.append("         WHERE ");
        stb.append("             YEAR = '" + param[0] + "' ");
        stb.append("             AND SEMESTER = '" + param[1] + "' ");
        stb.append("             AND SCHREGNO = '" + schno + "' ");
        stb.append("             AND CHAIRCD NOT LIKE '92%' ");
        stb.append("     ) l1 ON l1.SCHREGNO = t1.SCHREGNO ");
        stb.append("     LEFT JOIN CHAIR_DAT l2 ON l2.YEAR = t1.YEAR ");
        stb.append("     AND l2.SEMESTER = t1.SEMESTER ");
        stb.append("     AND l2.CHAIRCD = l1.CHAIRCD ");
        stb.append("     LEFT JOIN RECORD_DAT l3 ON l3.YEAR = t1.YEAR ");
        if ("1".equals(param[11])) {
            stb.append("     AND l3.CLASSCD = l2.CLASSCD ");
            stb.append("     AND l3.SCHOOL_KIND = l2.SCHOOL_KIND ");
            stb.append("     AND l3.CURRICULUM_CD = l2.CURRICULUM_CD ");
        }
        stb.append("     AND l3.SUBCLASSCD = l2.SUBCLASSCD ");
        stb.append("     AND l3.TAKESEMES = '0' ");
        stb.append("     AND l3.SCHREGNO = t1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     t1.YEAR = '" + param[0] + "' ");
        stb.append("     AND t1.SEMESTER = '" + param[1] + "' ");
        stb.append("     AND t1.SCHREGNO = '" + schno + "' ");
        stb.append(" ), SEQMAX AS ( ");
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");
        if ("1".equals(param[11])) {
            stb.append("     t1.CLASSCD, ");
            stb.append("     t1.SCHOOL_KIND, ");
            stb.append("     t1.CURRICULUM_CD, ");
        } else {
            stb.append("     t2.CLASSCD, ");
        }
        stb.append("     t1.SUBCLASSCD, ");
        stb.append("     t1.CHAIRCD, ");
        stb.append("     t1.REP_SEQ_ALL, ");
        stb.append("     t1.SCH_SEQ_MIN, ");
        stb.append("     t2.SCHREGNO, ");
        stb.append("     t2.COURSECD, ");
        stb.append("     t2.MAJORCD, ");
        stb.append("     t2.COURSECODE, ");
        stb.append("     t2.GRADE, ");
        stb.append("     t2.GRAD_VALUE ");
        stb.append(" FROM ");
        stb.append("     CHAIR_CORRES_DAT t1, ");
        stb.append("     SCHTBL t2 ");
        stb.append(" WHERE ");
        stb.append("     t1.YEAR = t2.YEAR ");
        stb.append("     AND t1.CHAIRCD = t2.CHAIRCD ");
        if ("1".equals(param[11])) {
            stb.append("     AND t1.CLASSCD = t2.CLASSCD ");
            stb.append("     AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ");
            stb.append("     AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ");
        }
        stb.append("     AND t1.SUBCLASSCD = t2.SUBCLASSCD ");
        stb.append("     AND t2.CLASSCD = '90' ");
        stb.append(" ), SEQMST AS ( ");
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.SCHREGNO, ");
        if ("1".equals(param[11])) {
            stb.append("     t1.CLASSCD, ");
            stb.append("     t1.SCHOOL_KIND, ");
            stb.append("     t1.CURRICULUM_CD, ");
        }
        stb.append("     t1.SUBCLASSCD, ");
        stb.append("     t2.CHAIRCD, ");
        stb.append("     STANDARD_SEQ, ");
        stb.append("     MAX(RECEIPT_DATE) AS MAXDATE ");
        stb.append(" FROM ");
        stb.append("     REP_PRESENT_DAT t1, ");
        stb.append("     SEQMAX t2 ");
        stb.append(" WHERE ");
        stb.append("     t1.YEAR = t2.YEAR ");
        if ("1".equals(param[11])) {
            stb.append("     AND t1.CLASSCD = t2.CLASSCD ");
            stb.append("     AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ");
            stb.append("     AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ");
        }
        stb.append("     AND t1.SUBCLASSCD = t2.SUBCLASSCD ");
        stb.append("     AND t1.STANDARD_SEQ <= t2.REP_SEQ_ALL ");
        stb.append("     AND t1.SCHREGNO = t2.SCHREGNO ");
        stb.append(" GROUP BY ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.SCHREGNO, ");
        if ("1".equals(param[11])) {
            stb.append("     t1.CLASSCD, ");
            stb.append("     t1.SCHOOL_KIND, ");
            stb.append("     t1.CURRICULUM_CD, ");
        }
        stb.append("     t1.SUBCLASSCD, ");
        stb.append("     t2.CHAIRCD, ");
        stb.append("     STANDARD_SEQ ");
        stb.append(" ), MAX_PRESENT_DAT AS ( ");
        stb.append(" SELECT ");
        stb.append("     t1.* ");
        stb.append(" FROM ");
        stb.append("     REP_PRESENT_DAT t1, ");
        stb.append("     SEQMST t2 ");
        stb.append(" WHERE ");
        stb.append("     t1.YEAR = t2.YEAR ");
        if ("1".equals(param[11])) {
            stb.append("     AND t1.CLASSCD = t2.CLASSCD ");
            stb.append("     AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ");
            stb.append("     AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ");
        }
        stb.append("     AND t1.SUBCLASSCD = t2.SUBCLASSCD ");
        stb.append("     AND t1.STANDARD_SEQ = t2.STANDARD_SEQ ");
        stb.append("     AND t1.SCHREGNO = t2.SCHREGNO ");
        stb.append("     AND t1.CHAIRCD = t2.CHAIRCD ");
        stb.append("     AND RECEIPT_DATE = MAXDATE ");
        stb.append("     AND int(VALUE(t1.GRAD_VALUE,'0')) BETWEEN 2 AND 5 ");
        stb.append(" ), MAX_PRESENT_CNT AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        if ("1".equals(param[11])) {
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
        }
        stb.append("     SUBCLASSCD, ");
        stb.append("     CHAIRCD, ");
        stb.append("     SCHREGNO, ");
        stb.append("     COUNT(*) AS PRE_CNT ");
        stb.append(" FROM ");
        stb.append("     MAX_PRESENT_DAT ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        if ("1".equals(param[11])) {
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
        }
        stb.append("     SUBCLASSCD, ");
        stb.append("     CHAIRCD, ");
        stb.append("     SCHREGNO ");
        stb.append(" ), SEQBASE AS ( ");
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.COURSECD, ");
        stb.append("     t1.MAJORCD, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     t1.COURSECODE, ");
        stb.append("     t1.GRADE, ");
        stb.append("     t1.CLASSCD, ");
        if ("1".equals(param[11])) {
            stb.append("     t1.SCHOOL_KIND, ");
            stb.append("     t1.CURRICULUM_CD, ");
        }
        stb.append("     t1.SUBCLASSCD, ");
        stb.append("     t1.CHAIRCD, ");
        stb.append("     t1.REP_SEQ_ALL, ");
        stb.append("     t1.SCH_SEQ_MIN ");
        stb.append(" FROM ");
        stb.append("     SEQMAX t1, ");
        stb.append("     MAX_PRESENT_CNT t2 ");
        stb.append(" WHERE ");
        stb.append("     t1.YEAR = t2.YEAR ");
        if ("1".equals(param[11])) {
            stb.append("     AND t1.CLASSCD = t2.CLASSCD ");
            stb.append("     AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ");
            stb.append("     AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ");
        }
        stb.append("     AND t1.SUBCLASSCD = t2.SUBCLASSCD ");
        stb.append("     AND t1.CHAIRCD = t2.CHAIRCD ");
        stb.append("     AND t1.SCHREGNO = t2.SCHREGNO ");
        stb.append("     AND t2.PRE_CNT >= t1.REP_SEQ_ALL ");
        stb.append(" ), ATTEND_SUB AS ( ");
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.CHAIRCD, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     VALUE(l1.ATTEND_CNT,0) AS ATTEND_CNT, ");
        stb.append("     l1.SCHOOLINGKINDCD ");
        stb.append(" FROM ");
        stb.append("     SCHTBL t1 ");
        stb.append("     LEFT JOIN (SELECT ");
        stb.append("                    t2.YEAR, ");
        stb.append("                    t2.SCHREGNO, ");
        stb.append("                    t2.CHAIRCD, ");
        stb.append("                    t2.SCHOOLINGKINDCD, ");
        stb.append("                    COUNT(distinct SCHOOLING_SEQ) AS ATTEND_CNT ");
        stb.append("                FROM ");
        stb.append("                    SEQBASE w1, ");
        stb.append("                    SCH_ATTEND_DAT t2 ");
        stb.append("                WHERE ");
        stb.append("                    t2.YEAR = w1.YEAR ");
        stb.append("                    AND t2.SCHREGNO = w1.SCHREGNO ");
        stb.append("                    AND t2.CHAIRCD = w1.CHAIRCD ");
        stb.append("                    AND t2.SCHOOLINGKINDCD = '1' ");
        stb.append("                GROUP BY ");
        stb.append("                    t2.YEAR, ");
        stb.append("                    t2.SCHREGNO, ");
        stb.append("                    t2.CHAIRCD, ");
        stb.append("                    t2.SCHOOLINGKINDCD ");
        stb.append("     ) l1 ON l1.YEAR = t1.YEAR ");
        stb.append("     AND l1.SCHREGNO = t1.SCHREGNO ");
        stb.append("     AND l1.CHAIRCD = t1.CHAIRCD ");
        stb.append(" WHERE ");
        stb.append("     t1.CLASSCD = '90' ");
        stb.append("     AND l1.SCHOOLINGKINDCD = '1' ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.CHAIRCD, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     VALUE(l1.ATTEND_CNT,0) AS ATTEND_CNT, ");
        stb.append("     l1.SCHOOLINGKINDCD ");
        stb.append(" FROM ");
        stb.append("     SCHTBL t1 ");
        stb.append("     LEFT JOIN (SELECT ");
        stb.append("                    t2.YEAR, ");
        stb.append("                    t2.SCHREGNO, ");
        stb.append("                    t2.CHAIRCD, ");
        stb.append("                    '0' AS SCHOOLINGKINDCD, ");
        stb.append("                    COUNT(*) AS ATTEND_CNT ");
        stb.append("                FROM ");
        stb.append("                    SEQBASE w1, ");
        stb.append("                    SCH_ATTEND_DAT t2 ");
        stb.append("                WHERE ");
        stb.append("                    t2.YEAR = w1.YEAR ");
        stb.append("                    AND t2.SCHREGNO = w1.SCHREGNO ");
        stb.append("                    AND t2.CHAIRCD = w1.CHAIRCD ");
        stb.append("                    AND VALUE(t2.SCHOOLINGKINDCD,'0') <> '1' ");
        stb.append("                GROUP BY ");
        stb.append("                    t2.YEAR, ");
        stb.append("                    t2.SCHREGNO, ");
        stb.append("                    t2.CHAIRCD ");
        stb.append("     ) l1 ON l1.YEAR = t1.YEAR ");
        stb.append("     AND l1.SCHREGNO = t1.SCHREGNO ");
        stb.append("     AND l1.CHAIRCD = t1.CHAIRCD ");
        stb.append(" WHERE ");
        stb.append("     t1.CLASSCD = '90' ");
        stb.append("     AND l1.SCHOOLINGKINDCD = '0' ");
        stb.append(" ), ATTEND AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     CHAIRCD, ");
        stb.append("     SCHREGNO, ");
        stb.append("     SUM(VALUE(ATTEND_CNT,0)) AS ATTEND_CNT ");
        stb.append(" FROM ");
        stb.append("     ATTEND_SUB ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     CHAIRCD, ");
        stb.append("     SCHREGNO ");
        stb.append(" ), SCHTBL2 AS ( ");
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.COURSECD, ");
        stb.append("     t1.MAJORCD, ");
        stb.append("     t1.COURSECODE, ");
        stb.append("     t1.GRADE, ");
        stb.append("     t1.CLASSCD, ");
        if ("1".equals(param[11])) {
            stb.append("     t1.SCHOOL_KIND, ");
            stb.append("     t1.CURRICULUM_CD, ");
        }
        stb.append("     t1.SUBCLASSCD, ");
        stb.append("     t1.CHAIRCD, ");
        stb.append("     t1.REP_SEQ_ALL, ");
        stb.append("     t1.SCH_SEQ_MIN ");
        stb.append(" FROM ");
        stb.append("     SEQMAX t1, ");
        stb.append("     ATTEND t2 ");
        stb.append(" WHERE ");
        stb.append("     t2.YEAR = t1.YEAR ");
        stb.append("     AND t2.CHAIRCD = t1.CHAIRCD ");
        stb.append("     AND t2.ATTEND_CNT >= t1.SCH_SEQ_MIN ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SUM(VALUE(t1.CREDITS,0)) AS CREDIT_CNT ");
        stb.append(" FROM ");
        stb.append("     (SELECT ");
        stb.append("          SUM(t1.CREDITS) AS CREDITS ");
        stb.append("      FROM ");
        stb.append("          CREDIT_MST t1,SCHTBL t2 ");
        stb.append("      WHERE ");
        stb.append("          t1.YEAR = t2.YEAR ");
        stb.append("          AND t1.COURSECD = t2.COURSECD ");
        stb.append("          AND t1.MAJORCD = t2.MAJORCD ");
        stb.append("          AND t1.GRADE = t2.GRADE ");
        stb.append("          AND t1.COURSECODE = t2.COURSECODE ");
        stb.append("          AND t1.CLASSCD = t2.CLASSCD ");
        if ("1".equals(param[11])) {
            stb.append("          AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ");
            stb.append("          AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ");
        }
        stb.append("          AND t1.SUBCLASSCD = t2.SUBCLASSCD ");
        stb.append("          AND (t1.CLASSCD < '90' OR t1.CLASSCD > '90') ");
        stb.append("          AND GRAD_VALUE = 1 ");
        stb.append("      UNION ALL ");
        stb.append("      SELECT ");
        stb.append("          SUM(t1.CREDITS) AS CREDITS ");
        stb.append("      FROM ");
        stb.append("          CREDIT_MST t1,SCHTBL2 t2 ");
        stb.append("      WHERE ");
        stb.append("          t1.YEAR = t2.YEAR ");
        stb.append("          AND t1.COURSECD = t2.COURSECD ");
        stb.append("          AND t1.MAJORCD = t2.MAJORCD ");
        stb.append("          AND t1.GRADE = t2.GRADE ");
        stb.append("          AND t1.COURSECODE = t2.COURSECODE ");
        stb.append("          AND t1.CLASSCD = t2.CLASSCD ");
        if ("1".equals(param[11])) {
            stb.append("          AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ");
            stb.append("          AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ");
        }
        stb.append("          AND t1.SUBCLASSCD = t2.SUBCLASSCD ");
        stb.append("     ) t1 ");

        return stb.toString();
    }
    // CSON: ExecutableStatementCount|MethodLength
} //クラスの括り

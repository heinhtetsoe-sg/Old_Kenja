package servletpack.KNJA;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Semester;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *    学校教育システム 賢者 [学籍管理]
 *
 *                     ＜ＫＮＪＡ１７０Ｋ＞ 生徒基本データ(生徒名簿)
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJA170K {

    private static final Log log = LogFactory.getLog(KNJA170K.class);

    Vrw32alp svf = new Vrw32alp(); // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

    DB2UDB db2; // Databaseクラスを継承したクラス

    String dbname;

    boolean nonedata; // 該当データなしフラグ

    String pyear, psemester, pgrade, phr_class, pschregno, date1, date2, change;

    boolean _isTokubetsuShien = false;
    String _gakunenKongou = "";
    String _dispMTokuHouJituGrdMixChkRad = "";
    boolean _isFi = false;
    boolean _isGhr = false;
    boolean _isGakunenKongou = false;
    boolean _isHoutei = false;
    boolean _use_finSchool_teNyuryoku_P = false;

    KNJ_EditDate editdate = new KNJ_EditDate(); // 和暦変換取得クラスのインスタンス作成

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        // パラメータの取得
        try {
            dbname = request.getParameter("DBNAME"); // データベース名
            pyear = request.getParameter("YEAR"); // 年度
            psemester = request.getParameter("OUTPUT"); // 学期
            change = request.getParameter("CHANGE"); // NO001 1:法定クラス 2:個人 3:実クラス
            _isTokubetsuShien = "1".equals(request.getParameter("useSpecial_Support_Hrclass"));
            _gakunenKongou = request.getParameter("GAKUNEN_KONGOU");
            _dispMTokuHouJituGrdMixChkRad = request.getParameter("dispMTokuHouJituGrdMixChkRad");
            _use_finSchool_teNyuryoku_P = "1".equals(request.getParameter("use_finSchool_teNyuryoku_P"));

            if ("1".equals(_dispMTokuHouJituGrdMixChkRad)) {
                if ("3".equals(change) && "1".equals(request.getParameter("useFi_Hrclass"))) {
                    _isFi = true;
                } else if ("3".equals(change) && _isTokubetsuShien) {
                    _isGhr = true;
                } else if ("1".equals(change) && "1".equals(_gakunenKongou) && _isTokubetsuShien) {
                    _isGakunenKongou = true;
                } else {
                    _isHoutei = true;
                }
            } else {
                _isHoutei = true;
            }

            // '学年＋組'パラメータを分解
            if (change.equals("2")) {
                String strx = request.getParameter("GRADE_HR_CLASS"); // 学年＋組
                pgrade = strx.substring(0,2); // 学年
                phr_class = strx.substring(2); // 組
            }

            // 学籍番号
            String c_select[] = request.getParameterValues("category_name"); // 学籍番号
            if (_isGakunenKongou) {
                for (int ia = 0; ia < c_select.length; ia++) {
                    if (c_select[ia] == null) {
                        break;
                    }
                    //1文字目はソート用のコードなので、除外
                    c_select[ia] = ("".equals(c_select[ia]) ? "" : StringUtils.substring(c_select[ia], 1));
                }
            }

            pschregno = "(";
            for (int ia = 0; ia < c_select.length; ia++) {
                if (c_select[ia] == null) {
                    break;
                }
                if (ia > 0) {
                    pschregno = pschregno + ",";
                }
                pschregno = pschregno + "'" + c_select[ia] + "'";
            }
            pschregno = pschregno + ")";

        } catch (Exception ex) {
            log.error("parameter error!", ex);
        }

        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        svf.VrInit(); // クラスの初期化
        svf.VrSetSpoolFileStream(outstrm); // PDFファイル名の設定
        svf.VrSetForm("KNJA170K.frm", 4); // SuperVisualFormadeで設計したレイアウト定義態の設定

        // ＤＢ接続
        db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
        }

        // 作成日(現在処理日)の取得
        try {
            KNJ_Control control = new KNJ_Control(); // クラスのインスタンス作成
            KNJ_Control.ReturnVal returnval = control.Control(db2);
            svf.VrsOut("YMD", KNJ_EditDate.h_format_JP(db2, returnval.val3)); // 現在処理日
        } catch (Exception e) {
            log.error("[KNZ170]ctrl_date get error!", e);
        }

        // 学期期間の取得(住所取得用)
        try {
            KNJ_Semester semester = new KNJ_Semester(); // クラスのインスタンス作成
            KNJ_Semester.ReturnVal returnval = semester.Semester(db2, pyear, psemester);
            date1 = returnval.val2; // 学期開始日
            date2 = returnval.val3; // 学期終了日
        } catch (Exception e) {
            log.error("[KNA170]Semester sdate get error!", e);
        }

        log.debug("pyear=" + pyear);
        log.debug("psemester=" + psemester);
        log.debug("pgrade=" + pgrade);
        log.debug("phr_class=" + phr_class);
        log.debug("pschregno=" + pschregno);

        // ＳＱＬ作成
        nonedata = false;
        PreparedStatement ps1 = null;
        // SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(pyear, psemester, pgrade, phr_class, pschregno, date1, date2, change)); // 設定データpreparestatement
        } catch (Exception ex) {
            log.error("SQL read error!", ex);
        }

        // 照会結果の取得およびＳＶＦへ出力
        try {
            String pagechange = "*";
            int gyo = 0;
            int cnt = 1;

            ResultSet rs = ps1.executeQuery();

            String gkCourseName = "";
            while (rs.next()) {
                final String grCdStr;
                final String hrCdStr;
                if (_isGhr) {
                    grCdStr = rs.getString("GHR_CD");
                    hrCdStr = "";
                } else if (_isGakunenKongou) {
                    grCdStr = rs.getString("SCHOOL_KIND");
                    hrCdStr = rs.getString("GK_HR_CLASS");
                    if (cnt == 1) {
                        gkCourseName = StringUtils.defaultString(rs.getString("COURSENAME")) + " " + StringUtils.defaultString(rs.getString("GK_CLASSNAME"));  //先頭にいる生徒の学部名
                    }
                } else {
                    grCdStr = rs.getString("GRADE");
                    hrCdStr = rs.getString("HR_CLASS");
                }
                if (pagechange.equals("*")) {
                    Set_Head(db2, svf, pyear, psemester, grCdStr, hrCdStr, gkCourseName);
                }
                if (!pagechange.equals("*") && !pagechange.equals(grCdStr + hrCdStr) || 15 <= gyo) {
                    for (; gyo < 15; gyo++) {
                        svf.VrEndRecord();
                    }
                    gyo = 0;
                    if (!pagechange.equals(grCdStr + hrCdStr)) {
                        cnt = 1;
                        if (_isGakunenKongou) {
                            gkCourseName = StringUtils.defaultString(rs.getString("COURSENAME")) + " " + StringUtils.defaultString(rs.getString("GK_CLASSNAME"));  //先頭にいる生徒の学部名
                        }
                        Set_Head(db2, svf, pyear, psemester, grCdStr, hrCdStr, gkCourseName);
                    }
                }
                // svfフォームのフィールドへ出力
                if (rs.getString("ATTENDNO") != null) {
                    svf.VrsOut("gakkyuubango", _isGakunenKongou ? String.valueOf(cnt) : String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))));
                }
                svf.VrsOut("gakusekibango", rs.getString("SCHREGNO"));
                svf.VrsOut("seito_kana", rs.getString("SEITO_KANA"));
                svf.VrsOut("NAME", rs.getString("SEITO_KANJI"));
                svf.VrsOut("birthday1", KNJ_EditDate.h_format_JP_N(db2, rs.getString("BIRTHDAY")));
                svf.VrsOut("birthday2", KNJ_EditDate.h_format_JP_MD(rs.getString("BIRTHDAY")));
                svf.VrsOut("seito_jyusho", rs.getString("ADDRESS1"));
                svf.VrsOut("seito_jyusho2", rs.getString("ADDRESS2"));
                svf.VrsOut("hogosha_kanji", rs.getString("GUARD_NAME"));
                svf.VrsOut("yuubinbango", rs.getString("ZIPCD"));
                svf.VrsOut("hogosha_jyusho", rs.getString("GUARD_ADDRESS1"));
                svf.VrsOut("hogosha_jyusho2", rs.getString("GUARD_ADDRESS2"));
                svf.VrsOut("hogosha_shoku", rs.getString("WORK_NAME"));
                svf.VrsOut("denwabango", rs.getString("TELNO"));
                svf.VrsOut("syussinko", rs.getString("J_NAME"));
                svf.VrEndRecord();
                pagechange = grCdStr + hrCdStr;
                gyo++;
                cnt++;
                nonedata = true; // 該当データなしフラグ
            }
            db2.commit();
            ps1.close();
            rs.close();
        } catch (Exception e) {
            log.error("DB2 query error!", e);
        }
        db2.close(); // DBを閉じる

        /* 該当データ無し */
        if (nonedata == false) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note", "note");
            svf.VrEndRecord();
            svf.VrEndPage();
        }

        svf.VrPrint();
        svf.VrQuit();

        outstrm.close(); // ストリームを閉じる
    }

    // 組名称及び担任名の取得
    private void Set_Head(DB2UDB db2, Vrw32alp svf, String pyear, String psemester, String pgrade, String phr_class, final String gkCourseName) {
        try {
            ReturnVal returnval = Hrclass_Staff(db2, pyear, psemester, pgrade, phr_class);

            svf.VrsOut("nendo", KNJ_EditDate.gengou(db2, Integer.parseInt(pyear)) + "年度");
            if (!_isGakunenKongou) {
                svf.VrsOut("HR_NAME", returnval.val1); // 組名称
                svf.VrsOut("tannin_mei", returnval.val3); // 担任名
            } else {
                svf.VrsOut("HR_NAME", gkCourseName); // 組名称
            }
        } catch (Exception e) {
            log.error("hrclass_staff error!", e);
        }
    }

    /** ＤＢより組名称及び担任名を取得するメソッド **/
    public ReturnVal Hrclass_Staff(DB2UDB db2, String year, String semester, String grade, String hr_class){

        String hrclass_name = new String();     //組名称
        String hrclass_abbv = new String();     //組略称
        String staff_name = new String();       //担任名
        String classweeks = new String();       //授業週数
        String classdays = new String();        //授業日数

        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT DISTINCT ");
        if (_isGhr) {
            sql.append("   W2.GHR_NAME AS HR_NAME, ");
            sql.append("   W2.GHR_NAMEABBV AS HR_NAMEABBV, ");
        } else {
            sql.append("   W2.HR_NAME, ");
            sql.append("   W2.HR_NAMEABBV, ");
            sql.append("   W2.CLASSWEEKS, ");
            sql.append("   W2.CLASSDAYS, ");
        }
        sql.append("   W1.STAFFNAME ");

        sql.append(" FROM ");
        if (_isGhr) {
            sql.append(" SCHREG_REGD_GHR_DAT TF1 ");
            sql.append("     INNER JOIN SCHREG_REGD_GHR_HDAT W2 ON W2.YEAR = TF1.YEAR ");
            sql.append("     AND W2.SEMESTER = TF1.SEMESTER ");
            sql.append("     AND W2.GHR_CD = TF1.GHR_CD ");
        } else if (_isGakunenKongou) {
            sql.append(" V_STAFF_HR_DAT TG1 ");
            sql.append("     INNER JOIN SCHREG_REGD_HDAT W2 ON W2.YEAR = TG1.YEAR ");
            sql.append("     AND W2.SEMESTER = TG1.SEMESTER");
            sql.append("     AND W2.GRADE = TG1.GRADE");
            sql.append("     AND W2.HR_CLASS = TG1.HR_CLASS");
        } else {
            sql.append("   SCHREG_REGD_HDAT W2 ");
        }
        sql.append(" LEFT JOIN STAFF_MST W1 ON W1.STAFFCD = W2.TR_CD1 ");
        sql.append(" WHERE ");
        final String setSemesterStr;
        if (_isGhr) {
            sql.append("  TF1.YEAR = '" + year + "' ");
            sql.append("  AND TF1.GHR_CD = '" + grade + "' ");
            setSemesterStr = "TF1.SEMESTER";
        } else if (_isGakunenKongou) {
            sql.append("  TG1.YEAR = '" + year + "' ");
            sql.append("  AND TG1.SCHOOL_KIND || '-' || TG1.HR_CLASS IN '" + grade + '-' + hr_class + "' ");
            setSemesterStr = "TG1.SEMESTER";
        } else {
            sql.append("  W2.YEAR = '" + year + "' ");
            sql.append("  AND W2.GRADE || W2.HR_CLASS = '" + grade + hr_class + "' ");
            setSemesterStr = "W2.SEMESTER";
        }
        if ( !semester.equals("9") ) { //学期指定の場合
            sql.append("  AND " + setSemesterStr + " = '" + semester + "' ");
        } else {  //学年指定の場合
            sql.append("  AND " + setSemesterStr + " = (SELECT ");
            sql.append("                       MAX(W3.SEMESTER) ");
            sql.append("                     FROM ");
            sql.append("                       SCHREG_REGD_HDAT W3 ");
            sql.append("                     WHERE ");
            sql.append("                       W2.YEAR = W3.YEAR ");
            sql.append("                       AND W2.GRADE || W2.HR_CLASS = W3.GRADE || W3.HR_CLASS ");
            sql.append("                    ) ");
        }

        try{
            db2.query(sql.toString());
            ResultSet rs = db2.getResultSet();

            if ( rs.next() ){
                hrclass_name = rs.getString("HR_NAME");
                hrclass_abbv = rs.getString("HR_NAMEABBV");
                staff_name = rs.getString("STAFFNAME");
                if (!_isGhr) {
                    classweeks = rs.getString("CLASSWEEKS");
                    classdays = rs.getString("CLASSDAYS");
                }
            }

            rs.close();
            System.out.println("[KNJ_Grade_Hrclass]Hrclass_Staff ok!");
        } catch( Exception ex ){
            System.out.println("[KNJ_Grade_Hrclass]Hrclass_Staff error!");
            System.out.println( ex );
        }

        return (new ReturnVal(hrclass_name,hrclass_abbv,staff_name,classweeks,classdays));
    }

    /** <<< return値を返す内部クラス >>> **/
    private class ReturnVal{

        public final String val1,val2,val3,val4,val5;

        public ReturnVal(String val1,String val2,String val3,String val4,String val5){
            this.val1 = val1;
            this.val2 = val2;
            this.val3 = val3;
            this.val4 = val4;
            this.val5 = val5;
        }
    }

    /** PrepareStatement作成* */
    private String Pre_Stat1(String pyear, String psemester, String pgrade, String phr_class, String pschregno, String date1, String date2, String change) {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append(" SELECT DISTINCT ");
            stb.append("     DB1.YEAR, ");
            if (_isGhr) {
                stb.append("  TF1.GHR_CD, ");
            } else if (_isGakunenKongou) {
                stb.append("  TG1.SCHOOL_KIND, ");
                stb.append("  TG1.HR_CLASS AS GK_HR_CLASS, ");
                stb.append("  TG1.HR_CLASS_NAME1 AS GK_CLASSNAME, ");
                stb.append("  DB20.COURSENAME, ");
            }
            stb.append("     DB1.GRADE, ");
            stb.append("     DB1.HR_CLASS, ");
            stb.append("     DB1.SEMESTER, ");
            if (_isGhr) {
                stb.append("     TF1.GHR_ATTENDNO AS ATTENDNO, ");
            } else {
                stb.append("     DB1.ATTENDNO, ");
            }
            stb.append("     DB1.SCHREGNO, ");
            stb.append("     DB2.NAME AS SEITO_KANJI, ");
            stb.append("     VALUE(DB2.NAME_KANA,'') AS SEITO_KANA, ");
            stb.append("     VALUE(CHAR(DB2.BIRTHDAY),'') AS BIRTHDAY, ");
            stb.append("     VALUE(DB3.ADDR1,'') AS ADDRESS1, ");
            stb.append("     value(DB3.ADDR2,'') AS ADDRESS2, ");
            stb.append("     VALUE(DB4.GUARD_NAME,'') AS GUARD_NAME, ");
            stb.append("     VALUE(DB4.GUARD_ZIPCD,'') AS ZIPCD, ");
            stb.append("     VALUE(DB4.GUARD_ADDR1,'') AS GUARD_ADDRESS1, ");
            stb.append("     value(DB4.GUARD_ADDR2,'') AS GUARD_ADDRESS2, ");
            stb.append("     VALUE(DB8.NAME1,'') AS WORK_NAME, ");
            stb.append("     VALUE(DB4.GUARD_TELNO,'') AS TELNO, ");
            if (_use_finSchool_teNyuryoku_P) {
                stb.append("     DB9.SCHOOL_KIND, ");
                stb.append("     CASE WHEN DB9.SCHOOL_KIND = 'P' THEN DB10.BASE_REMARK1 ");
                stb.append("          ELSE VALUE(DB6.FINSCHOOL_NAME,'') END AS J_NAME ");
            } else {
                stb.append(" VALUE(DB6.FINSCHOOL_NAME,'') AS J_NAME ");
            }
            stb.append(" FROM ");
            if (_isFi) {
                stb.append(" SCHREG_REGD_FI_DAT TF1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_DAT DB1 ON DB1.SCHREGNO = TF1.SCHREGNO ");
                stb.append("     AND DB1.YEAR = TF1.YEAR ");
                stb.append("     AND DB1.SEMESTER = TF1.SEMESTER ");
                stb.append("     INNER JOIN SCHREG_REGD_HDAT DB7 ON DB1.YEAR = DB7.YEAR ");
                stb.append("     AND DB1.SEMESTER = DB7.SEMESTER ");
                stb.append("     AND DB1.GRADE = DB7.GRADE ");
                stb.append("     AND DB1.HR_CLASS = DB7.HR_CLASS ");
            } else if (_isGhr) {
                stb.append(" SCHREG_REGD_GHR_DAT TF1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_DAT DB1 ON DB1.SCHREGNO = TF1.SCHREGNO ");
                stb.append("     AND DB1.YEAR = TF1.YEAR ");
                stb.append("     AND DB1.SEMESTER = TF1.SEMESTER ");
                stb.append("     INNER JOIN SCHREG_REGD_HDAT DB7 ON DB1.YEAR = DB7.YEAR ");
                stb.append("     AND DB1.SEMESTER = DB7.SEMESTER ");
                stb.append("     AND DB1.GRADE = DB7.GRADE ");
                stb.append("     AND DB1.HR_CLASS = DB7.HR_CLASS ");
            } else if (_isGakunenKongou) {
                stb.append(" V_STAFF_HR_DAT TG1 ");
                stb.append("     INNER JOIN SCHREG_REGD_HDAT DB7 ON DB7.YEAR = TG1.YEAR ");
                stb.append("     AND DB7.SEMESTER = TG1.SEMESTER");
                stb.append("     AND DB7.GRADE = TG1.GRADE");
                stb.append("     AND DB7.HR_CLASS = TG1.HR_CLASS");
                stb.append("     INNER JOIN SCHREG_REGD_DAT DB1 ON DB1.YEAR = TG1.YEAR ");
                stb.append("     AND DB1.SEMESTER = TG1.SEMESTER ");
                stb.append("     AND DB1.GRADE = DB7.GRADE ");
                stb.append("     AND DB1.HR_CLASS = DB7.HR_CLASS ");
            } else {
                stb.append("     SCHREG_REGD_DAT DB1 ");
                stb.append("     INNER JOIN SCHREG_REGD_HDAT DB7 ON DB1.YEAR = DB7.YEAR ");
                stb.append("     AND DB1.SEMESTER = DB7.SEMESTER ");
                stb.append("     AND DB1.GRADE = DB7.GRADE ");
                stb.append("     AND DB1.HR_CLASS = DB7.HR_CLASS ");
            }
            stb.append("     INNER JOIN SCHREG_REGD_GDAT     DB9 ON DB1.YEAR = DB9.YEAR AND DB1.GRADE = DB9.GRADE ");
            stb.append("     INNER JOIN SCHREG_BASE_MST      DB2 ON DB1.SCHREGNO = DB2.SCHREGNO ");
            stb.append("     LEFT  JOIN SCHREG_BASE_DETAIL_MST DB10 ON DB10.SCHREGNO = DB2.SCHREGNO AND DB10.BASE_SEQ = '002' ");
            stb.append("     LEFT  JOIN GUARDIAN_DAT           DB4 ON DB2.SCHREGNO = DB4.SCHREGNO ");
            stb.append("     LEFT  JOIN FINSCHOOL_MST     DB6 ON DB2.FINSCHOOLCD = DB6.FINSCHOOLCD ");
            stb.append("     LEFT JOIN ( ");
            stb.append("                SELECT ");
            stb.append("                    SCHREGNO, ");
            stb.append("                    ADDR1, ");
            stb.append("                    ADDR2 ");
            stb.append("                FROM ");
            stb.append("                    SCHREG_ADDRESS_DAT W1 ");
            stb.append("                WHERE ");
            if (change.equals("2")) {
                stb.append("                    (W1.SCHREGNO,W1.ISSUEDATE) IN ( ");
                stb.append("                                                   SELECT SCHREGNO,MAX(ISSUEDATE) ");
                stb.append("                                                   FROM   SCHREG_ADDRESS_DAT W2 ");
                stb.append("                                                   WHERE  W2.ISSUEDATE <= '" + date2 + "' ");
                stb.append("                                                          AND (W2.EXPIREDATE IS NULL ");
                stb.append("                                                          OR W2.EXPIREDATE >= '" + date1 + "') ");
                stb.append("                                                          AND W2.SCHREGNO IN " + pschregno);
                stb.append("                                                          GROUP BY SCHREGNO ) ");
            } else {
                stb.append("                    (W1.SCHREGNO,W1.ISSUEDATE) IN ( ");
                stb.append("                                                   SELECT SCHREGNO,MAX(ISSUEDATE) ");
                stb.append("                                                   FROM   SCHREG_ADDRESS_DAT W2 ");
                stb.append("                                                   WHERE  W2.ISSUEDATE <= '" + date2 + "' ");
                stb.append("                                                          AND (W2.EXPIREDATE IS NULL ");
                stb.append("                                                          OR W2.EXPIREDATE >= '" + date1 + "') ");
                stb.append("                                                          AND W2.SCHREGNO IN ");
                if (_isFi) {
                    stb.append(" (SELECT DBW1.SCHREGNO FROM SCHREG_REGD_FI_DAT TFW1 ");
                    stb.append("     LEFT JOIN SCHREG_REGD_DAT DBW1 ON DBW1.SCHREGNO = TFW1.SCHREGNO ");
                    stb.append("     AND DBW1.YEAR = TFW1.YEAR ");
                    stb.append("     AND DBW1.SEMESTER = TFW1.SEMESTER ");
                    stb.append("  WHERE TFW1.GRADE || TFW1.HR_CLASS IN " + pschregno + " AND TFW1.YEAR = '" + pyear + "' AND TFW1.SEMESTER = '" + psemester + "') ");
                } else if (_isGhr) {
                    stb.append(" (SELECT DBW1.SCHREGNO FROM SCHREG_REGD_GHR_DAT TFW1 ");
                    stb.append("     LEFT JOIN SCHREG_REGD_DAT DBW1 ON DBW1.SCHREGNO = TFW1.SCHREGNO ");
                    stb.append("     AND DBW1.YEAR = TFW1.YEAR ");
                    stb.append("     AND DBW1.SEMESTER = TFW1.SEMESTER ");
                    stb.append("  WHERE TFW1.GHR_CD IN " + pschregno + " AND TFW1.YEAR = '" + pyear + "' AND TFW1.SEMESTER = '" + psemester + "') ");
                } else if (_isGakunenKongou) {
                    stb.append(" (SELECT DBW1.SCHREGNO FROM V_STAFF_HR_DAT TFW1 ");
                    stb.append("     LEFT JOIN SCHREG_REGD_DAT DBW1 ON DBW1.YEAR = TFW1.YEAR AND DBW1.SEMESTER = TFW1.SEMESTER AND DBW1.GRADE = TFW1.GRADE AND DBW1.HR_CLASS = TFW1.HR_CLASS ");
                    stb.append("  WHERE TFW1.SCHOOL_KIND || '-' || TFW1.HR_CLASS IN " + pschregno + " AND TFW1.YEAR = '" + pyear + "' AND TFW1.SEMESTER = '" + psemester + "') ");
                } else {
                    stb.append("                                                          (SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE GRADE || HR_CLASS IN " + pschregno + " AND YEAR = '" + pyear
                        + "' AND SEMESTER = '" + psemester + "') ");
                }
                stb.append("                                                          GROUP BY SCHREGNO ) ");
            }
            stb.append("     )DB3 ON DB3.SCHREGNO = DB1.SCHREGNO ");
            stb.append("     LEFT  JOIN NAME_MST DB8 ON DB8.NAMECD1 = 'H202' ");
            stb.append("     AND DB8.NAMECD2 = DB4.GUARD_JOBCD ");
            stb.append("     LEFT JOIN COURSE_MST DB20 ON DB20.COURSECD = DB1.COURSECD ");
            stb.append(" WHERE ");
            stb.append("     DB1.YEAR     = '" + pyear + "' ");
            stb.append("     AND DB1.SEMESTER = '" + psemester + "' ");
            if (change.equals("2")) {
                stb.append("     AND DB1.GRADE    = '" + pgrade + "' ");
                stb.append("     AND DB1.HR_CLASS = '" + phr_class + "' ");
                stb.append("     AND DB1.SCHREGNO IN " + pschregno);
                stb.append("     ORDER BY DB1.GRADE,DB1.HR_CLASS,DB1.ATTENDNO ");
            } else {
                stb.append("     AND DB1.SCHREGNO IN ");
                if (_isFi) {
                    stb.append(" (SELECT DBW1.SCHREGNO FROM SCHREG_REGD_FI_DAT TFW1 ");
                    stb.append("     LEFT JOIN SCHREG_REGD_DAT DBW1 ON DBW1.SCHREGNO = TFW1.SCHREGNO ");
                    stb.append("     AND DBW1.YEAR = TFW1.YEAR ");
                    stb.append("     AND DBW1.SEMESTER = TFW1.SEMESTER ");
                    stb.append("  WHERE TFW1.GRADE || TFW1.HR_CLASS IN " + pschregno + " AND TFW1.YEAR = '" + pyear + "' AND TFW1.SEMESTER = '" + psemester + "') ");
                    stb.append("     ORDER BY DB1.GRADE,DB1.HR_CLASS,DB1.ATTENDNO ");
                } else if (_isGhr) {
                    stb.append(" (SELECT DBW1.SCHREGNO FROM SCHREG_REGD_GHR_DAT TFW1 ");
                    stb.append("     LEFT JOIN SCHREG_REGD_DAT DBW1 ON DBW1.SCHREGNO = TFW1.SCHREGNO ");
                    stb.append("     AND DBW1.YEAR = TFW1.YEAR ");
                    stb.append("     AND DBW1.SEMESTER = TFW1.SEMESTER ");
                    stb.append("  WHERE TFW1.GHR_CD IN " + pschregno + " AND TFW1.YEAR = '" + pyear + "' AND TFW1.SEMESTER = '" + psemester + "') ");
                    stb.append("     ORDER BY DB1.GRADE,DB1.HR_CLASS,TF1.GHR_ATTENDNO ");
                } else if (_isGakunenKongou) {
                    stb.append(" (SELECT DBW1.SCHREGNO FROM V_STAFF_HR_DAT TFW1 ");
                    stb.append("     LEFT JOIN SCHREG_REGD_DAT DBW1 ON DBW1.YEAR = TFW1.YEAR AND DBW1.SEMESTER = TFW1.SEMESTER AND DBW1.GRADE = TFW1.GRADE AND DBW1.HR_CLASS = TFW1.HR_CLASS ");
                    stb.append("  WHERE TFW1.SCHOOL_KIND || '-' || TFW1.HR_CLASS IN " + pschregno + " AND TFW1.YEAR = '" + pyear + "' AND TFW1.SEMESTER = '" + psemester + "') ");
                    stb.append("  AND TG1.SCHOOL_KIND || '-' || TG1.HR_CLASS IN " + pschregno + " AND TG1.YEAR = '" + pyear + "' AND TG1.SEMESTER = '" + psemester + "' ");
                    stb.append("     ORDER BY TG1.SCHOOL_KIND, TG1.HR_CLASS, DB1.GRADE,DB1.HR_CLASS,DB1.ATTENDNO ");
                } else {
                    stb.append("     (SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE GRADE || HR_CLASS IN " + pschregno + " AND YEAR = '" + pyear + "' AND SEMESTER = '" + psemester + "') ");
                    stb.append("     AND DB1.GRADE || DB1.HR_CLASS IN " + pschregno);
                    stb.append("     ORDER BY DB1.GRADE,DB1.HR_CLASS,DB1.ATTENDNO ");
                }
            }

        } catch (Exception e) {
            log.error("Pre_Stat1 error!", e);
        }
        log.debug(stb.toString());
        return stb.toString();

    }// Pre_Stat1()の括り
}

package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Grade_Hrclass;
import servletpack.KNJZ.detail.KNJ_Semester;
import servletpack.KNJZ.detail.StaffInfo;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *    学校教育システム 賢者 [学籍管理]
 *                                                               　                *
 *                  ＜ＫＮＪＡ２００＞    クラス名簿印刷　　　　　　　　           *
 *                                                                                  *
 * 2003/11/12 nakamoto 和暦変換に対応                                               *
 * 2005/05/23 yamasiro 電話番号出力指定                                               *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJA200 {

    private static final Log log = LogFactory.getLog(KNJA200.class);

    private Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private String documentroot = new String();
    boolean nonedata = false; //該当データなしフラグ
    private String tel_flg;

    private String param_0;
    private String param_1;
    private String param_2;
    private String param_3;
    private String param_4;
    private String param_5;
    private String param_6;
    private String param_7;
    private String param_8;
    private String param_9;
    private String param_10;
    private String param_11;
    private String param_12;
    private String param_13;
    private String param_14;
    private String param_15;
    private String _staffCd;
    private StaffInfo _staffInfo;
    private String _z010;

    String _hrClsType = "";   //1:法定 2:実クラス
    boolean _isTokubetsuShien = false;
    String _gakunenKongou = "";  //1:学年混合
    String _dispMTokuHouJituGrdMixChkRad = "";  //クラスタイプ選択表示プロパティ
    boolean _isFi = false;
    boolean _isGhr = false;
    boolean _isGakunenKongou = false;
    boolean _isHoutei = false;
    boolean _use_finSchool_teNyuryoku_P = false;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        KNJServletUtils.debugParam(request, log);

        // パラメータの取得
        String dbname = null;
        DB2UDB    db2;      // Databaseクラスを継承したクラス

        try {
            dbname   = request.getParameter("DBNAME");                          //データベース名
            log.debug("[KNJA200]dbname= " + dbname);
            documentroot   = request.getParameter("DOCUMENTROOT");              //定数
            log.debug("[KNJA200]documentroot= " + documentroot);

            param_0  = request.getParameter("YEAR");                             //年度
            param_1  = request.getParameter("SEMESTER");                          //学期（学籍処理日から取得）

            _hrClsType = request.getParameter("HR_CLASS_TYPE");
            _isTokubetsuShien = "1".equals(request.getParameter("useSpecial_Support_Hrclass"));
            _gakunenKongou = request.getParameter("GAKUNEN_KONGOU");
            _dispMTokuHouJituGrdMixChkRad = request.getParameter("dispMTokuHouJituGrdMixChkRad");
            _use_finSchool_teNyuryoku_P = "1".equals(request.getParameter("use_finSchool_teNyuryoku_P"));

            if ("1".equals(_dispMTokuHouJituGrdMixChkRad)) {
                if ("2".equals(_hrClsType) && "1".equals(request.getParameter("useFi_Hrclass"))) {
                    _isFi = true;
                } else if ("2".equals(_hrClsType) && _isTokubetsuShien) {
                    _isGhr = true;
                } else if ("1".equals(_hrClsType) && "1".equals(_gakunenKongou) && _isTokubetsuShien) {
                    _isGakunenKongou = true;
                } else {
                    _isHoutei = true;
                }
            } else {
                _isHoutei = true;
            }

            //    '学年＋組'パラメータを分解
            //学年＋組
            final String grHrClsStr = request.getParameter("GRADE_HR_CLASS");
            if (_isGhr) {
                param_2 = grHrClsStr;                 //学年
                param_3 = null;                      //組
            } else if (_isGakunenKongou) {
                param_2 = grHrClsStr.substring(1,2);  //学年
                param_3 = grHrClsStr.substring(3);    //組
            } else {
                KNJ_Grade_Hrclass gradehrclass = new KNJ_Grade_Hrclass();            //取得クラスのインスタンス作成
                KNJ_Grade_Hrclass.ReturnVal returnval = gradehrclass.Grade_Hrclass(grHrClsStr);
                param_2 = returnval.val1;                                            //学年
                param_3 = returnval.val2;                                            //組
            }
            //    電話番号出力フラグ
            tel_flg = request.getParameter("TEL");           //電話番号出力フラグ
            //    日付型を変換
            param_4 = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));        //学籍処理日
            param_13 = request.getParameter("FORM_SENTAKU");                           //フォーム選択 (1:5列×5行 2:6列×7行)
            param_14 = request.getParameter("SCHOOLNAME");                           //学校名
            param_15 = StringUtils.defaultString(request.getParameter("PRINT_INFO"), "");                           //出身学校出力/ふりがな出力

            //    職員コード取得
            _staffCd = request.getParameter("PRINT_LOG_STAFFCD");

            log.debug("[KNJA200]parameter ok!");
        } catch (Exception ex) {
            log.error("parameter:", ex);
        }

        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定


        // ＤＢ接続
        db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
            log.debug("[KNJA200]DB2 opened ok");
        } catch (Exception ex) {
            log.error("DB2 open:", ex);
        }

        //    学期名称の取得
        try {
            KNJ_Semester semester = new KNJ_Semester();                            //クラスのインスタンス作成
            KNJ_Semester.ReturnVal returnval = semester.Semester(db2,param_0,param_1);
            param_12 = returnval.val1;                                        //学期名称

        } catch (Exception e) {
            log.error("semester read:", e);
        }


        /* 写真データ格納フォルダ・写真データの拡張子の取得 */
        try {
            KNJ_Control imagepath_extension = new KNJ_Control();            //取得クラスのインスタンス作成
            KNJ_Control.ReturnVal returnval = imagepath_extension.Control(db2);
            param_5 = returnval.val4;                                            //写真データ格納フォルダ
            param_10 = returnval.val5;                                            //写真データの拡張子

        } catch (Exception e) {
            log.error("photodata read:", e);
        }

        //    組名称及び担任名の取得
        try {
            ReturnVal returnval = Hrclass_Staff(db2,param_0,param_1,param_2,param_3);
            param_7 = returnval.val1;                                            //組名称
            param_8 = returnval.val2;                                            //組略称
            param_6 = returnval.val3;                                            //担任名

        } catch (Exception e) {
            log.error("hrclass_staff read:", e);
        }

        //生徒氏名（英語・日本語）切替処理用クラスのインスタンス作成
        _staffInfo = new StaffInfo(db2, _staffCd);

        final String[] param = new String[15];     /** 0-4:画面より受取 5:写真データ格納フォルダ 6:担任名 */
        param[0] = param_0;
        param[1] = param_1;
        param[2] = param_2;
        param[3] = param_3;
        param[4] = param_4;
        param[5] = param_5;
        param[6] = param_6;
        param[7] = param_7;
        param[8] = param_8;
        param[9] = param_9;
        param[10] = param_10;
        param[11] = param_11;
        param[12] = param_12;
        param[13] = param_13;
        param[14] = _staffCd;

        for (int ia = 0; ia < param.length; ia++) {
            log.debug("[KNJA200]param[" + ia + "]= " + param[ia]);
        }

        _z010 = setZ010Name1(db2);
        log.info(" z010 = " + _z010);

        /*-----------------------------------------------------------------------------
        ＳＶＦ作成処理
          -----------------------------------------------------------------------------*/


        // ＳＶＦフォーム出力
        /*クラス名簿印刷*/
        nonedata = false ; //該当データなしフラグ
        set_meisai(db2);

        /*該当データ無し*/
        log.debug("[KNJA200]nonedata=" + nonedata);
        if (nonedata == false) {
            int ret = 0;
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndRecord();
            log.debug("[KNJA200]nonedata VrEndRecord ret=" + ret);
            svf.VrEndPage();
        }

        svf.VrPrint();

        // 終了処理
        db2.close();        // DBを閉じる
        svf.VrQuit();
        outstrm.close();    // ストリームを閉じる

    }    //doGetの括り

    /** ＤＢより組名称及び担任名を取得するメソッド **/
    public ReturnVal Hrclass_Staff(DB2UDB db2,String year,String semester,String grade,String hr_class){

        String hrclass_name = new String();     //組名称
        String hrclass_abbv = new String();     //組略称
        String staff_name = new String();       //担任名
        String classweeks = new String();       //授業週数
        String classdays = new String();        //授業日数

        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT ");
        if (_isGakunenKongou) { //学年混合ではコース＋クラス名のみ必要
            sql.append("   W3.COURSECD, ");
            sql.append("   M1.COURSENAME, ");
            sql.append("   W2.HR_CLASS_NAME1 ");
        } else if (_isGhr) {
            sql.append("   W2.GHR_NAME AS HR_NAME, ");
            sql.append("   W2.GHR_NAMEABBV AS HR_NAMEABBV, ");
            sql.append("   W1.STAFFNAME ");
        } else {
            sql.append("   W2.HR_NAME, ");
            sql.append("   W2.HR_NAMEABBV, ");
            sql.append("   W1.STAFFNAME, ");
            sql.append("   W2.CLASSWEEKS, ");
            sql.append("   W2.CLASSDAYS ");
        }
        sql.append(" FROM ");
        if (_isGhr) {
            sql.append(" SCHREG_REGD_GHR_DAT TF1 ");
            sql.append("     INNER JOIN SCHREG_REGD_GHR_HDAT W2 ON W2.YEAR = TF1.YEAR ");
            sql.append("     AND W2.SEMESTER = TF1.SEMESTER ");
            sql.append("     AND W2.GHR_CD = TF1.GHR_CD ");
        } else if (_isGakunenKongou) {
            sql.append(" V_STAFF_HR_DAT TG1 ");
            sql.append("     INNER JOIN SCHREG_REGD_HDAT W2 ON W2.YEAR = TG1.YEAR ");
            sql.append("     AND W2.SEMESTER = TG1.SEMESTER ");
            sql.append("     AND W2.GRADE = TG1.GRADE ");
            sql.append("     AND W2.HR_CLASS = TG1.HR_CLASS ");
            sql.append("     INNER JOIN SCHREG_REGD_DAT W3 ON W3.YEAR = W2.YEAR ");
            sql.append("     AND W3.SEMESTER = W2.SEMESTER ");
            sql.append("     AND W3.GRADE = W2.GRADE ");
            sql.append("     AND W3.HR_CLASS = W2.HR_CLASS ");
            sql.append("     INNER JOIN V_COURSE_MST M1 ON M1.YEAR = W3.YEAR ");
            sql.append("     AND M1.COURSECD = W3.COURSECD ");
        } else {
            sql.append("   SCHREG_REGD_HDAT W2 ");
        }
        sql.append("   LEFT JOIN STAFF_MST W1 ON W1.STAFFCD = W2.TR_CD1 ");
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
        if (_isGakunenKongou ) {
            sql.append(" ORDER BY W3.COURSECD "); //最小値のみ取得
        }

        try{

            db2.query(sql.toString());
            ResultSet rs = db2.getResultSet();

            if ( rs.next() ){

                if (_isGakunenKongou) {
                    hrclass_name = rs.getString("COURSENAME") + rs.getString("HR_CLASS_NAME1");
                } else if (_isGhr) {
                    hrclass_name = rs.getString("HR_NAME");
                    hrclass_abbv = rs.getString("HR_NAMEABBV");
                    staff_name = rs.getString("STAFFNAME");
                } else {
                    hrclass_name = rs.getString("HR_NAME");
                    hrclass_abbv = rs.getString("HR_NAMEABBV");
                    staff_name = rs.getString("STAFFNAME");
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

    private String getString(final Map map, final String field) {
        if (map.isEmpty()) {
            return null;
        }
        if (map.containsKey(field)) {
            return (String) map.get(field);
        }
        throw new IllegalArgumentException("no such field : \"" + field + "\" in " + map);
    }

    private static List getCountList(final List list, final int max) {
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

    public List getStudentList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            StringBuffer stb = new StringBuffer();
            String sql = new String();
            stb.append(" WITH SCHBASE_T AS ( ");
            stb.append("    SELECT ");
            stb.append("      DB1.YEAR, ");
            stb.append("      DB1.SEMESTER, ");
            stb.append("      DB1.SCHREGNO, ");
            stb.append("      DB1.GRADE, ");
            stb.append("      DB1.HR_CLASS, ");
            if (_isGhr) {
                stb.append(" TF1.GHR_ATTENDNO AS ATTENDNO ");
            } else {
                stb.append(" DB1.ATTENDNO ");
            }
            stb.append("    FROM ");
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
                stb.append("      SCHREG_REGD_DAT DB1 ");
            }
            stb.append("    WHERE ");
            stb.append("      DB1.YEAR         = '" +  param_0 + "' ");
            stb.append("      AND DB1.SEMESTER = '" +  param_1 + "' ");
            if (_isGhr) {
                stb.append("      AND TF1.GHR_CD      = '" +  param_2 + "' ");
            } else if (_isGakunenKongou) {
                stb.append("      AND TG1.SCHOOL_KIND = '" +  param_2 + "' ");
                stb.append("      AND TG1.HR_CLASS    = '" +  param_3 + "' ");
            } else {
                stb.append("      AND DB1.GRADE       = '" +  param_2 + "' ");
                stb.append("      AND DB1.HR_CLASS    = '" +  param_3 + "' ");
            }

            stb.append(" ) ");
            stb.append(" SELECT DISTINCT ");
            stb.append("   T1.SCHREGNO,");
            stb.append("   T1.NAME,");
            stb.append("   T1.NAME_KANA,");
            stb.append("   T1.NAME_ENG,");
            stb.append("   T2.GRADE,");
            stb.append("   T2.HR_CLASS,");
            stb.append("   T2.ATTENDNO,");
            stb.append("   T7.TELNO, ");
            stb.append("   SCHOOL.FINSCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("   SCHBASE_T T2 ");
            stb.append("   INNER JOIN SCHREG_REGD_HDAT T6 ON (T6.YEAR = T2.YEAR AND T6.SEMESTER = T2.SEMESTER ");
            stb.append("   AND T6.GRADE = T2.GRADE AND T6.HR_CLASS = T2.HR_CLASS) ");
            stb.append("   LEFT JOIN ");
            stb.append("   ( ");
            stb.append("     SELECT ");
            stb.append("       SCHREGNO,TELNO ");
            stb.append("     FROM ");
            stb.append("       SCHREG_ADDRESS_DAT ");
            stb.append("     WHERE ");
            stb.append("       (SCHREGNO,ISSUEDATE) IN ");
            stb.append("         (");
            stb.append("          SELECT ");
            stb.append("            SCHREGNO,MAX(ISSUEDATE) ");
            stb.append("          FROM ");
            stb.append("            SCHREG_ADDRESS_DAT ");
            stb.append("          GROUP BY SCHREGNO ");
            stb.append("          HAVING ");
            stb.append("            SCHREGNO IN (");
            stb.append("                         SELECT ");
            stb.append("                           SCHREGNO ");
            stb.append("                         FROM ");
            stb.append("                           SCHBASE_T ");
            stb.append("                        )");
            stb.append("         ) ");
            stb.append("   ) T7 ON (T2.SCHREGNO = T7.SCHREGNO) ");
            stb.append("   INNER JOIN SCHREG_BASE_MST T1 ON (T1.SCHREGNO  = T2.SCHREGNO) ");
            stb.append("   LEFT JOIN FINSCHOOL_MST SCHOOL ON T1.FINSCHOOLCD  = SCHOOL.FINSCHOOLCD ");
            stb.append("   ORDER BY T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");

            sql = stb.toString();
            log.debug("[KNJA200]set_meisai sql="+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Map student = new HashMap();
                student.put("SCHREGNO", rs.getString("SCHREGNO"));
                student.put("NAME", rs.getString("NAME"));
                student.put("NAME_KANA", rs.getString("NAME_KANA"));
                student.put("NAME_ENG", rs.getString("NAME_ENG"));
                student.put("GRADE", rs.getString("GRADE"));
                student.put("HR_CLASS", rs.getString("HR_CLASS"));
                student.put("ATTENDNO", rs.getString("ATTENDNO"));
                student.put("TELNO", rs.getString("TELNO"));
                student.put("FINSCHOOL_NAME", rs.getString("FINSCHOOL_NAME"));
                list.add(student);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    /**
     * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
     */
    private String setZ010Name1(DB2UDB db2) {
        String name1 = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
            rs = ps.executeQuery();
            if (rs.next()) {
                name1 = rs.getString("NAME1");
            }
        } catch (SQLException ex) {
            log.debug("getZ010 exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return name1;
    }

    /*------------------------------------*
     * ４．明細ＳＶＦ出力　　　           *
     *------------------------------------*/
    public void set_meisai(final DB2UDB db2)
                     throws ServletException, IOException
    {
        try {
            String formName;
            if ("kumamoto".equals(_z010)) {
                formName = "3".equals(param_13) ? "KNJA200_3.frm" : "2".equals(param_13) ? "KNJA200_2_2.frm" : "KNJA200_1_2.frm";
            } else {
                formName = "3".equals(param_13) ? "KNJA200_3.frm" : "2".equals(param_13) ? "KNJA200_2.frm" : "KNJA200.frm";
            }
            svf.VrSetForm(formName, 1);

            final List studentList = getStudentList(db2);

                /** SVFフォームへデータをセット **/
            int maxNo = "3".equals(param_13) ? 8 : "2".equals(param_13) ? 6 : 5;
            int maxLine = "3".equals(param_13) ? 6 : "2".equals(param_13) ? 7 : 5;

            int recCnt = 0;
            final List pageList = getCountList(getCountList(studentList, maxNo), maxLine);
            for (final Iterator pit = pageList.iterator(); pit.hasNext();) {
                final List lineList = (List) pit.next();

                set_head(db2);

                for (int li = 0; li < lineList.size(); li++) {
                    final int line = li + 1;
                    final List sList = (List) lineList.get(li);

                    for (int ci = 0; ci < sList.size(); ci++) {
                        int no = ci + 1;
                        final Map rs = (Map) sList.get(ci);
                        String strdir1 = "P" + getString(rs, ("SCHREGNO")) + "." + param_10;  //本番用
                        String strdir2 = documentroot + "/" + param_5 + "/" + strdir1;   //本番用
                        File f1 = new File(strdir2);   //写真データ存在チェック用

                        recCnt++;
                        String studentName = _staffInfo.getStrEngOrJp(getString(rs, ("NAME")), getString(rs, ("NAME_ENG")));

                        if ("3".equals(param_13)) {
                            //氏名
                            if (_isGakunenKongou) {
                                svf.VrsOutn("NO"               + no    , line, String.valueOf(recCnt));
                            } else {
                                svf.VrsOutn("NO"               + no    , line, String.valueOf(Integer.parseInt(getString(rs, "ATTENDNO"))));
                            }
                            final String outstr = studentName;
                            final String outtype = getMS932ByteLength(outstr) > 20 ? "4" : getMS932ByteLength(outstr) > 14 ? "3" : getMS932ByteLength(outstr) > 10 ? "2" : "1";
                            svf.VrsOutn("NAME_SHOW_"       + no + "_" + outtype  , line, studentName);
                        } else {
                            //出身学校
                            if ("1".equals(param_14)) {
                                //氏名
                                if (_isGakunenKongou) {
                                    svf.VrsOutn("field3_"          + no    , line, String.valueOf(recCnt) + "番");
                                } else if (_isGhr) {
                                    svf.VrsOutn("field3_"          + no    , line, String.valueOf(Integer.parseInt(getString(rs, "ATTENDNO"))) + "番");
                                }
                                else {
                                    svf.VrsOutn("field3_"          + no    , line, param_8 + "-" + String.valueOf(Integer.parseInt(getString(rs, "ATTENDNO"))));
                                }
                                svf.VrsOutn("field4_"          + no    , line, "(" + getString(rs, ("SCHREGNO")) + ")");
                                final int nameByte = KNJ_EditEdit.getMS932ByteLength(studentName);
                                final String nameField = nameByte > 30 ? "_3" : nameByte > 20 ? "_2" : "";
                                svf.VrsOutn("NAME_SHOW_"       + no + "_2" + nameField, line, studentName);
                                if ("2".equals(param_15)) {
                                    String kanafield = "";
                                    String kfield = "";
                                    String telfield = "";
                                    String tfield = "";
                                    String kana = StringUtils.defaultString(getString(rs, ("NAME_KANA")));
                                    if (!"".equals(kana) ) {
                                        kanafield = "PHONE";
                                        kfield = tel_flg != null ? "_2" : "";
                                        //ふりがな
                                        svf.VrsOutn(kanafield + no + kfield, line, kana);
                                        telfield = "FINSCHOOL";
                                        tfield = "";
                                    } else {
                                        telfield = "PHONE";
                                        tfield = "";
                                    }
                                    //電話番号
                                    if (tel_flg != null) {
                                        svf.VrsOutn(telfield        + no + tfield    , line, "TEL：" + StringUtils.defaultString(getString(rs, "TELNO")));
                                    }
                                } else {
                                    //電話番号
                                    if (tel_flg != null) {
                                        svf.VrsOutn("PHONE"        + no + "_2"    , line, "TEL：" + StringUtils.defaultString(getString(rs, "TELNO")));
                                    }
                                    //出身学校
                                    String finschool = StringUtils.defaultString(getString(rs, ("FINSCHOOL_NAME")));
                                    final String finSchoolField = getMS932ByteLength(finschool) > 20 ? "_2" : "";
                                    svf.VrsOutn("FINSCHOOL" + no + finSchoolField, line, finschool);
                                }
                            } else {
                                //氏名
                                if (_isGakunenKongou) {
                                    svf.VrsOutn("field1_"          + no    , line, String.valueOf(recCnt) + "番");
                                } else if (_isGhr) {
                                    svf.VrsOutn("field1_"          + no    , line, String.valueOf(Integer.parseInt(getString(rs, "ATTENDNO"))) + "番");
                                }
                                else {
                                    svf.VrsOutn("field1_"          + no    , line, param_8 + "-" + String.valueOf(Integer.parseInt(getString(rs, "ATTENDNO"))));
                                }
                                svf.VrsOutn("field2_"          + no    , line, "(" + getString(rs, ("SCHREGNO")) + ")");
                                final int nameByte = KNJ_EditEdit.getMS932ByteLength(studentName);
                                final String nameField = nameByte > 30 ? "_1_3" : nameByte > 20 ? "_1_2" : "";
                                svf.VrsOutn("NAME_SHOW_"       + no + nameField, line, studentName);
                                //電話番号
                                if (tel_flg != null) {
                                    svf.VrsOutn("PHONE"        + no    , line, "TEL：" + StringUtils.defaultString(getString(rs, "TELNO")));
                                }
                            }
                        }
                        //写真
                        if (f1.exists()) {
                            svf.VrsOutn("Bitmap_Field" + no    , line, strdir2);
                            svf.VrsOutn("NO_DATA"      + no    , line, "");
                            svf.VrsOutn("ATTENDNO"     + no    , line, "");
                        } else{
                            svf.VrsOutn("Bitmap_Field" + no    , line, "");
                            svf.VrsOutn("NO_DATA"      + no    , line, "イメージデータなし");
                            svf.VrsOutn("ATTENDNO"     + no    , line, strdir1);
                        }

                    }
                }
                svf.VrEndPage();
                nonedata = true;
            }

        } catch (Exception ex) {
            log.error("set_meisai read:", ex);
        }
        log.debug("[KNJA200]set_meisai path!");

    }  //set_meisaiの括り

    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }

    /*------------------------------------*
     * 見出し項目のセット                 *
     *------------------------------------*/
    public void set_head(final DB2UDB db2)
                     throws ServletException, IOException
    {
        try {
            svf.VrsOut("TODAY"          , KNJ_EditDate.h_format_JP(db2, param_4));
            final int gradeHrClassByte = KNJ_EditEdit.getMS932ByteLength(param_7);
            final String gradeHrClassFieldName = gradeHrClassByte > 10 ? "_2" : "";
            svf.VrsOut("GRADE_HR_CLASS" + gradeHrClassFieldName, param_7);
            if (!_isGakunenKongou) {
                svf.VrsOut("STAFFNAME"      , param_6);
            }
            svf.VrsOut("NENDO"    , KNJ_EditDate.gengou(db2, Integer.parseInt(param_0)) + "年度");
            svf.VrsOut("SEMESTER"       , param_12);
            log.debug("[KNJA200]set_head read ok!");
        } catch (Exception ex) {
            log.error("set_head read:", ex);
        }

    }  //set_headの括り


}  //クラスの括り


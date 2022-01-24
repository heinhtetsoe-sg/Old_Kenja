// kanji=漢字
/*
 * $Id: f8d09a9d1ea9b69a2d32b5fb4f498fbc7fbee1e3 $
 *
 * 作成日: 2005/04/21
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJC053_BASE;
import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJD.detail.KNJ_Testname;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJSchoolMst;

/**
 *  学校教育システム 賢者 [成績管理] 成績個人票
 *
 *  2005/04/21 yamashiro・新規作成
 *  2005/06/11 yamashiro・履修科目を全て出力
 *                      ・集計カウントフラグ参照を追加
 *  2005/06/20 yamashiro・ペナルティ欠課の算出式を修正
 *  2005/07/06 yamashiro・欠課累積は異動基準日までとする
 *                      ・学期評定および学年評定のSVF-FORMフィールド名を修正
 *  2005/10/31 yamashiro・出欠の累積情報の日付を出力
 *                      ・出欠の累積情報の締日において「指定学期まで」の条件を除外
 *                      ・出欠の累積情報の遅刻数は遅早数に変更
 *  2005/12/16 yamashiro・評価、評定において記号フィールドを追加( => prestatementRecordメソッドで対応 )
 *                        表には数値を優先して出力
 *                        フォームにおいて評価、評定は右寄せで出力
 *  2006/02/01 yamasihro・2学期制の場合、1月〜3月の出欠集計データのソート順による集計の不具合を修正 --
 */

public class KNJD151 {

    private static final Log log = LogFactory.getLog(KNJD151.class);
    
    protected DecimalFormat dmf1 = new DecimalFormat("0");
    protected DecimalFormat dmf2 = new DecimalFormat("0.0");
    private String schno[];                 //学籍番号
    protected boolean nonedata;
    protected KNJDefineSchool definecode;  // 各学校における定数等設定

    private static final String FORM_FILE = "KNJD151.frm";
    private static final String FORM_FILE2 = "KNJD151_2.frm";
    private static final String SUBJECT_D = "01";  //教科コード
    private static final String SUBJECT_U = "89";  //教科コード
    private static final String SUBJECT_T = "90";  //総合的な学習の時間
    
    private final Map _chairmap = new HashMap();  // 講座内順位を格納
    protected List _testkindlist;  // テスト種別を格納
    protected List _recordfieldlist;  // 使用する成績データのフィールド名を格納
    protected KNJSchoolMst _knjSchoolMst; // 学校マスタ
    
    /**
     *  KNJD.classから最初に起動されるクラス
     */
    public void svf_out(
            HttpServletRequest request,
            HttpServletResponse response)
    throws ServletException, IOException {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

        MyVrw32alp svf = new MyVrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                  // Databaseクラスを継承したクラス
        
        // パラメータの取得
        final Map paramap = getParam(request);
        // print svf設定
        sd.setSvfInit(request, response, svf);
        // ＤＢ接続
        db2 = sd.setDb(request);
        if( sd.openDb(db2) ){
            log.error("db open error");
            return;
        }
        try {
            _knjSchoolMst = new KNJSchoolMst(db2, (String) paramap.get("YEAR"));
        } catch (SQLException ex) {
            log.error("load KNJSchoolMst exception!", ex);
        }
        // 印刷処理
        printSvf(db2,svf,paramap);
        // 終了処理
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }

    /**
     *  印刷処理
     */
    private void printSvf(
            DB2UDB db2,
            Vrw32alp svf,
            Map paramap
    ) {
        _recordfieldlist = new ArrayList(setRecordList(paramap));
        _testkindlist = new ArrayList(setTestKindList(paramap));

        try {
            final InnerChair innerchair = new InnerChair();
            innerchair.createInnerChair(db2,paramap);
        } catch(Exception ex){
            log.error("error! ",ex);
        }
        
        setHead(db2,svf,paramap);         //見出し項目
        getDivideAttendDate(db2,paramap);  //出欠用日付等取得 05/02/17
        printSvfMain(db2,svf,paramap);        //SVF-FORM出力処理
    }

    protected String setSvfForm(final Map paramap) { return ((String)paramap.get("DEVIATION") != null) ? FORM_FILE2 : FORM_FILE; }
    
    /** 
     *  SVF-FORMセット＆見出し項目 
     */
    private void setHead(
            DB2UDB db2,
            Vrw32alp svf,
            Map paramap
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm(setSvfForm(paramap), 4);
        ret = svf.VrAttribute("NAME", "FF=1"); // 生徒で改ページ

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;

        ret = svf.VrsOut("NENDO",  nao_package.KenjaProperties.gengou(Integer.parseInt((String)paramap.get("YEAR"))) + "年度");

        ret = svf.VrsOut("P_DATE", KNJ_EditDate.h_format_thi((String)paramap.get("DATE"),0)); // 累積情報日

        //  担任名
        List arrstaffname = getinfo.Staff_name(db2,(String)paramap.get("YEAR"),(String)paramap.get("SEMESTER"),(String)paramap.get("GRADE_HR_CLASS"));
        for(int i = 0; i < arrstaffname.size(); i++){
            ret = svf.VrsOut("STAFFNAME" + (i+1), (String)arrstaffname.get(i));
            if (i != 0) ret = svf.VrsOut("COMMA" + i, ",");
        }

        //  作成日(現在処理日)
        returnval = getinfo.Control(db2);
        ret = svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));

        //  欠課数換算定数取得 => KNJDefineCodeImpを実装したオブジェクトを作成
        definecode = new KNJDefineSchool();
        definecode.defineCode(db2,(String)paramap.get("YEAR")); // 各学校における定数等設定
        
        // テスト種別名を出力
        final String sql = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + (String)paramap.get("YEAR") + "' AND SEMESTER = '" + (String)paramap.get("SEMESTER") + "'";
        String semesterName = "";
        try {
            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            if (rs.next() && rs.getString("SEMESTERNAME") != null) {
                semesterName = rs.getString("SEMESTERNAME");
            }
            db2.commit();
            rs.close();
        } catch (SQLException e) {
             log.error("SQLException", e);
        } catch (Exception e) {
             log.error("Exception", e);
        }

        final String testSql = KNJ_Testname.getTestNameSql((String) paramap.get("COUNTFLG"), (String) paramap.get("YEAR"), (String) paramap.get("SEMESTER"), (String) paramap.get("TESTKINDCD"));
        String testName = "";
        try {
            db2.query(testSql);
            final ResultSet rs = db2.getResultSet();
            if (rs.next() && rs.getString("TESTITEMNAME") != null) {
                testName = rs.getString("TESTITEMNAME");
            }
            db2.commit();
            rs.close();
        } catch (SQLException e) {
             log.error("SQLException", e);
        } catch (Exception e) {
             log.error("Exception", e);
        }

        ret = svf.VrsOut("TESTNAME",  semesterName + " " + testName);
        
        try {
            String[] semester = new String[]{"1","2","3"};
            String countFlgTable = (String) paramap.get("COUNTFLG");
            String year = (String) paramap.get("YEAR");
            for (int si=0; si<semester.length; si++) {
                db2.query( getTestNameSql(countFlgTable, year, semester[si]));

                final ResultSet rs = db2.getResultSet();
                int kindi=1;
                while (rs.next() && rs.getString("TESTITEMNAME") != null) {
                    ret = svf.VrsOut("TEST"+semester[si]+"_"+String.valueOf(kindi), rs.getString("TESTITEMNAME"));
                    kindi++;
                }
                db2.commit();
                rs.close();
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } catch (Exception e) {
            log.error("Exception", e);
        }

    }

    public static String getTestNameSql(
            final String countFlgTable,
            final String year,
            final String semester
    ) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("    TESTITEMNAME ");
        stb.append(" FROM ");
        stb.append("    " + countFlgTable + " ");
        stb.append(" WHERE ");
        stb.append("    YEAR = '" + year + "' ");
        stb.append("    AND TESTKINDCD IN ('01','02') ");
        if ("TESTITEM_MST_COUNTFLG_NEW".equals(countFlgTable)) {
            stb.append("    AND SEMESTER = '" + semester + "' ");
        } 
        stb.append(" ORDER BY ");
        stb.append("    TESTKINDCD, TESTITEMCD");
        return stb.toString();
    }

    /**
     *  出欠集計テーブルをみる最終月と出欠データをみる開始日を取得する
     */
    private void getDivideAttendDate(
            final DB2UDB db2,
            final Map paramap
    ) {
        final KNJDivideAttendDate obj = new KNJDivideAttendDate();
        obj.getDivideAttendDate(db2, (String) paramap.get("YEAR"), (String) paramap.get("SEMESTER"), (String) paramap.get("DATE"));
        paramap.put("DIVIDEATTENDDATE", obj.date);  //最終集計日の翌日
        paramap.put("DIVIDEATTENDMONTH", obj.month);  //最終集計学期＋月
        paramap.put("SEMES_MONTH", KNJC053_BASE.retSemesterMonthValue((String) paramap.get("DIVIDEATTENDMONTH")));
    }

    /** 
     *  SVF-FORM メイン出力処理 
     */
    private void printSvfMain(
            DB2UDB db2,
            Vrw32alp svf,
            Map paramap
    ) {
        //定義
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet rs = null;

        //RecordSet作成
        try {
            final String sql = prestatementRecord(db2,paramap);
            // log.debug(" sql = " + sql);
            db2.query(sql);       //生徒別成績データ
            rs = db2.getResultSet();
            int linex = 0;                                      //１ページ当り出力行数
            while( rs.next() ){
                printSvfOutMeisai(svf,rs,paramap);            //SVF-FORMへ出力---NO001
                linex++;
            }
        } catch( Exception ex ) { log.error("[KNJD151]printSvfMain read error! ", ex);  }
    }

    /** 
     *   ＨＲ成績生徒別明細を出力 => VrEndRecord()
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
    protected void printSvfOutMeisai(
            Vrw32alp svf,
            ResultSet rs,
            Map paramap
    ) {
        try {
            int ret = 0;
            ret = svf.VrsOut("NAME", rs.getString("HR_NAME") + " " + String.valueOf( Integer.parseInt( rs.getString("ATTENDNO") ) ) + "番   " + rs.getString("NAME") );  // 生徒名

            String subclassName = rs.getString("SUBCLASSNAME");
            ret = svf.VrsOut("SUBCLASS"+(subclassName != null && subclassName.length() > 8 ? "2" : ""), subclassName);  // 科目名
            ret = svf.VrsOut("CREDIT", rs.getString("CREDITS"));  // 単位数

            printSvfOutScoreAndValue(svf, rs);

            if (rs.getString("ABSENT") != null  &&  0 < Integer.parseInt(rs.getString("ABSENT")))
                ret = svf.VrsOut("KEKKA1", rs.getString("ABSENT"));  // 欠時数
            
            if (rs.getString("LATE_EARLY") != null  &&  0 < Integer.parseInt( rs.getString("LATE_EARLY")))
                ret = svf.VrsOut("KEKKA2",rs.getString("LATE_EARLY"));  // 遅早数

            if(rs.getString("ABSENT2") != null && 0 < Float.parseFloat(rs.getString("ABSENT2"))) {
                if (definecode.absent_cov == 3 || definecode.absent_cov == 4) {
                    ret = svf.VrsOut("KEKKA3", String.valueOf(dmf2.format(rs.getFloat("ABSENT2"))));  // 欠時数
                } else {
                    ret = svf.VrsOut("KEKKA3", String.valueOf(dmf1.format(rs.getInt("ABSENT2"))));  // 欠時数
                }
            }

            printSvfOutInnerChair(svf,rs,paramap);  // 講座に基づく出力
            
            ret = svf.VrEndRecord();
            if(ret == 0)nonedata = true;
        } catch( SQLException ex ){
            log.error("[KNJD151]printSvfOutMeisai error!", ex );
        }
    }

    protected void printSvfOutScoreAndValue(Vrw32alp svf, ResultSet rs) throws SQLException {
        for (Iterator i = _recordfieldlist.iterator(); i.hasNext();) {
            final List l = (List)i.next();
            final String scorefield = (String)l.get(1);
            final String valuefield = (String)l.get(2);
            final String kindfield = (String)l.get(l.size()-1);
            
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            if (kindfield.equals("9-0000")) {
                ret = svf.VrsOut ("RATE", rs.getString(valuefield));  // 学年評定
            } else if (-1 < kindfield.indexOf("0000")) {
                ret = svf.VrsOut ("RECORD" + kindfield.substring(0, 1) + "_9", rs.getString(valuefield));  //評価
            } else if (-1 < kindfield.indexOf("0101")) {
                ret = svf.VrsOut ("POINT" + kindfield.substring(0, 1) + "_1", rs.getString(scorefield));  //中間素点
                ret = svf.VrsOut ("RECORD" + kindfield.substring(0, 1) + "_1", rs.getString(valuefield));  //中間評価
            } else if (-1 < kindfield.indexOf("0201")) {
                ret = svf.VrsOut ("POINT" + kindfield.substring(0, 1) + "_2", rs.getString(scorefield));  //期末素点
                ret = svf.VrsOut ("RECORD" + kindfield.substring(0, 1) + "_2", rs.getString(valuefield));  //期末評価
            } else {
                ret = svf.VrsOut ("POINT" + kindfield.substring(0, 1) + "_3", rs.getString(scorefield));  //期末２素点
                ret = svf.VrsOut ("RECORD" + kindfield.substring(0, 1) + "_3", rs.getString(valuefield));  //期末２評価
            }
        }
    }
    
    /*
     * ＨＲ成績生徒別明細を出力 (講座に基づく出力)
     */
    protected void printSvfOutInnerChair (
            final Vrw32alp svf,
            final ResultSet rs,
            Map paramap
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        final String key = getKeyForInnerChair(rs, paramap);
        final InnerChair innerchair = (InnerChair)_chairmap.get(key);
        if (innerchair != null) {
            final String score = getScoreForInnerChair(rs,paramap);
            if (score != null) {
                ret = svf.VrsOut("CHAIRNAME", innerchair._chairname);  // 講座名
                ret = svf.VrsOut("NUMBER",  innerchair._schnum);  // 生徒数
                ret = svf.VrsOut("AVERAGE", innerchair._average);  // 平均
                final Map map = (Map)innerchair._rankmap;
                final String rank = (String)map.get(score);
                ret = svf.VrsOut("ORDER", rank);  // 順位
                if (innerchair._stddev != null) {
                    final Map devmap = (Map)innerchair._deviationmap;
                    final String deviation = (String)devmap.get(score);
                    ret = svf.VrsOut("DEVIATION", deviation);  // 偏差値
                }
            }
        }
    }
    
    /*
     * 講座内ＸＸのキーをセット
     */
    private String getKeyForInnerChair(
            final ResultSet rs,
            Map paramap
    ) {
        try {
            String subclasscd = rs.getString("SUBCLASSCD");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + subclasscd;
            }
            for (int i = _testkindlist.size() - 1; -1 < i; i--) {
                final List list = (ArrayList)_testkindlist.get(i);
                final String semester = (String)list.get(0);
                final String test = (String)list.get(1);
                final String chaircd = (String)list.get(2);
                final String score = (String)list.get(3);
                if (rs.getString(score) != null) {
                    final String str =  semester + "-" + test + "-" + subclasscd + "-" + rs.getString(chaircd);
                    return str;
                }
            }
        } catch (NumberFormatException e) {
             log.error("NumberFormatException", e);
        } catch (SQLException e) {
             log.error("SQLException", e);
        }
        return null;
    }

    /*
     * 講座内順位の対象得点をセット
     */
    private String getScoreForInnerChair(
            final ResultSet rs,
            Map paramap
    ) {
        try {
            for (int i = _testkindlist.size() - 1; -1 < i; i--) {
                final List list = (ArrayList)_testkindlist.get(i);
                final String score = (String)list.get(3);
                if (rs.getString(score) != null) {
                    return rs.getString(score);
                }
            }
        } catch (NumberFormatException e) {
             log.error("NumberFormatException", e);
        } catch (SQLException e) {
             log.error("SQLException", e);
        }
        return null;
    }

    /* 
     *  SQLStatement作成 成績データ
     *     ・講座内生徒数・席次・平均の対象となる成績について（暫定）
     *         ３学期成績 > ３学期期末素点 > ３学期中間素点 > ２学期成績 > ２学期期末素点 > ２学期中間素点
     *          > １学期成績 > １学期期末素点 > １学期中間素点 の順に成績が入力されているもので処理
     *  2005/06/20 yamashiro・ペナルティ欠課の算出式を修正
     */
    private String prestatementRecord(
            DB2UDB db2,
            Map paramap
    ) {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        //学籍の表
        stb.append(" SCHNO AS(");
        stb.append(" SELECT  T2.SCHREGNO,T3.NAME,T4.HR_NAME,T2.GRADE,T2.HR_CLASS,T2.ATTENDNO,T2.COURSECD,T2.MAJORCD,T2.COURSECODE");
        stb.append(" FROM    SCHREG_REGD_DAT T2");
        stb.append("        ,SCHREG_BASE_MST T3");
        stb.append("        ,SCHREG_REGD_HDAT T4");
        stb.append(" WHERE   T2.YEAR = '" + (String) paramap.get("YEAR") + "'");
        stb.append("     AND T2.GRADE = '" + ((String) paramap.get("GRADE_HR_CLASS")).substring( 0, 2 ) + "'");
        stb.append("     AND T2.HR_CLASS = '" + ((String) paramap.get("GRADE_HR_CLASS")).substring( 2 ) + "'");
        stb.append("     AND T2.SCHREGNO IN " + (String) paramap.get("SCHNOLIST"));
        stb.append("     AND T2.SEMESTER = (SELECT  MAX(SEMESTER)");
        stb.append("                        FROM    SCHREG_REGD_DAT W2");
        stb.append("                        WHERE   W2.YEAR = '" + (String) paramap.get("YEAR") + "'");
        stb.append("                            AND W2.SEMESTER <= '" + (String) paramap.get("SEMESTER") + "'");
        stb.append("                            AND W2.SCHREGNO = T2.SCHREGNO)");
        stb.append("     AND T2.SCHREGNO = T3.SCHREGNO");
        stb.append("     AND T4.YEAR = '" + (String) paramap.get("YEAR") + "'");
        stb.append("     AND T4.SEMESTER = T2.SEMESTER");
        stb.append("     AND T4.GRADE = T2.GRADE");
        stb.append("     AND T4.HR_CLASS = T2.HR_CLASS");
        stb.append(" )");

        //講座の表
        //05/10/31Modiy CHAIR_Aの学期条件を外した表
        stb.append(" ,CHAIR_S AS(");
        stb.append(" SELECT  S1.SCHREGNO,S1.SEMESTER,S1.CHAIRCD, ");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("    S2.CLASSCD,S2.SCHOOL_KIND,S2.CURRICULUM_CD,S2.SUBCLASSCD ");
        } else {
            stb.append("    S2.SUBCLASSCD ");
        }
        stb.append(" FROM    CHAIR_STD_DAT S1");
        stb.append("        ,CHAIR_DAT S2");
        stb.append(" WHERE   S1.YEAR = '" + (String) paramap.get("YEAR") + "'");
        stb.append("     AND S2.YEAR = S1.YEAR");
        stb.append("     AND S2.SEMESTER = S1.SEMESTER");
        stb.append("     AND S2.CHAIRCD = S1.CHAIRCD");
        stb.append("     AND (SUBSTR(SUBCLASSCD,1,2) <= '" + SUBJECT_U + "' OR SUBSTR(SUBCLASSCD,1,2)  = '" + SUBJECT_T + "')");
        stb.append("     AND EXISTS(SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO GROUP BY SCHREGNO)");
        stb.append(" GROUP BY S1.SCHREGNO,S1.SEMESTER,S1.CHAIRCD, ");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("    S2.CLASSCD,S2.SCHOOL_KIND,S2.CURRICULUM_CD,S2.SUBCLASSCD ");
        } else {
            stb.append("    S2.SUBCLASSCD ");
        }
        stb.append(" )");

        // テスト項目マスタの集計フラグ
        stb.append(" ,TEST_COUNTFLG AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         '2' AS DATADIV ");
        stb.append("     FROM ");
        stb.append("         SCH_CHR_TEST T1, ");
        if ("TESTITEM_MST_COUNTFLG_NEW".equals(paramap.get("COUNTFLG"))) {
            stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
        } else {
            stb.append("         TESTITEM_MST_COUNTFLG T2 ");
        }
        stb.append("     WHERE ");
        stb.append("         T2.YEAR       = T1.YEAR ");
        if ("TESTITEM_MST_COUNTFLG_NEW".equals(paramap.get("COUNTFLG"))) {
            stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
        }
        stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("         AND T2.COUNTFLG   = '0' ");
        stb.append(" ) ");

        //講座の表
        //05/10/31Modiy CHAIR_Sに学期条件入れた表
//        stb.append(" ,CHAIR_A AS(");
//        stb.append(" SELECT  SCHREGNO,SEMESTER,CHAIRCD,SUBCLASSCD,CHAIRNAME");
//        stb.append(" FROM    CHAIR_S");
//        stb.append(" WHERE   SEMESTER <= '" + (String) paramap.get("SEMESTER") + "'");
//        stb.append(" )");

        //出欠集計データの表
        stb.append(" ,ATTEND_CALC AS(");
        stb.append(" SELECT  T1.SCHREGNO,T1.SEMESTER, ");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("    T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD ");
        } else {
            stb.append("    T1.SUBCLASSCD ");
        }
        stb.append("        ,SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0)");
        if ("1".equals(_knjSchoolMst._subAbsent)) {
            stb.append("       + VALUE(ABSENT,0)");
        }
        if ("1".equals(_knjSchoolMst._subSuspend)) {
            stb.append("       + VALUE(SUSPEND,0)");
        }
        if ("1".equals(_knjSchoolMst._subMourning)) {
            stb.append("       + VALUE(MOURNING,0)");
        }
        if ("1".equals(_knjSchoolMst._subOffDays)) {
            stb.append("       + VALUE(OFFDAYS,0)");
        }
        if ("1".equals(_knjSchoolMst._subVirus)) {
            stb.append("       + VALUE(VIRUS,0)");
        }
        stb.append("         ) AS ABSENT1 ");        
        stb.append("        ,SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY");
        stb.append(" FROM    ATTEND_SUBCLASS_DAT T1");
        stb.append(" WHERE   YEAR = '" + (String) paramap.get("YEAR") + "'");
        stb.append("     AND (CASE WHEN INT(T1.MONTH) < 4 THEN RTRIM(CHAR(INT(T1.SEMESTER) + 1 )) ELSE T1.SEMESTER END )||T1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue ((String)paramap.get("DIVIDEATTENDMONTH")) + "'");
        stb.append("     AND EXISTS(SELECT 'X'  FROM SCHNO T2  WHERE T2.SCHREGNO = T1.SCHREGNO  GROUP BY SCHREGNO) ");
        stb.append(" GROUP BY T1.SCHREGNO,T1.SEMESTER, ");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("    T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD ");
        } else {
            stb.append("    T1.SUBCLASSCD ");
        }
        stb.append(") ");

        //出欠集計データの表
        stb.append(" ,ATTEND_A AS(");
        stb.append(" SELECT  S1.SCHREGNO,S1.SEMESTER, ");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("    S1.CLASSCD, S1.SCHOOL_KIND, S1.CURRICULUM_CD, S1.SUBCLASSCD ");
        } else {
            stb.append("    S1.SUBCLASSCD ");
        }
        stb.append("        ,SUM(CASE WHEN (CASE WHEN ATDD.REP_DI_CD IN ('29','30','31') THEN VALUE(ATDD.ATSUB_REPL_DI_CD, ATDD.REP_DI_CD) ELSE ATDD.REP_DI_CD END) IN('4','5','6','14','11','12','13'");
        if ("1".equals(_knjSchoolMst._subAbsent)) {
            stb.append(                           ",'1','8'");
        }
        if ("1".equals(_knjSchoolMst._subSuspend)) {
            stb.append(                           ",'2','9'");
        }
        if ("1".equals(_knjSchoolMst._subMourning)) {
            stb.append(                           ",'3','10'");
        }
        if ("1".equals(_knjSchoolMst._subVirus)) {
            stb.append(                           ",'19','20'");
        }
        stb.append(                               ") ");
        if ("1".equals(_knjSchoolMst._subOffDays)) {
            stb.append(                           "OR (IS_OFFDAYS IS NOT NULL)");
        }
        stb.append(             " THEN 1 ELSE 0 END)AS ABSENT1 ");
        stb.append("        ,SUM(CASE WHEN ATDD.REP_DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(ATDD.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
        stb.append(" FROM(");
        stb.append("         SELECT  T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD,T3.SEMESTER,T5.DI_CD,T4.SCHREGNO AS IS_OFFDAYS, ");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("        T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, T3.SUBCLASSCD ");
        } else {
            stb.append("        T3.SUBCLASSCD ");
        }
        stb.append("         FROM    CHAIR_S T3");    //05/10/31 CHAIR_A -> CHAIR_S
        stb.append("                INNER JOIN SCH_CHR_DAT T1 ON T1.SEMESTER = T3.SEMESTER");
        stb.append("                    AND T1.CHAIRCD = T3.CHAIRCD");
        stb.append("                    AND T1.SEMESTER = T3.SEMESTER");
        stb.append("                INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("                    AND T2.CHAIRCD = T3.CHAIRCD ");
        stb.append("                    AND T2.SCHREGNO = T3.SCHREGNO ");
        stb.append("                    AND T2.SEMESTER = T3.SEMESTER ");
        stb.append("                LEFT JOIN SCHREG_TRANSFER_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO ");
        stb.append("                    AND T1.EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ");
        stb.append("                LEFT JOIN ATTEND_DAT T5 ON T2.SCHREGNO = T5.SCHREGNO ");
        stb.append("                    AND T1.EXECUTEDATE = T5.ATTENDDATE ");
        stb.append("                    AND T1.PERIODCD = T5.PERIODCD ");
        stb.append("         WHERE   T1.YEAR = '" + (String) paramap.get("YEAR") + "'");
        stb.append("             AND T1.EXECUTEDATE BETWEEN '" + (String) paramap.get("DIVIDEATTENDDATE") + "' AND '" + (String) paramap.get("DATE") + "'");
        stb.append("             AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
        if( definecode.useschchrcountflg ){  //05/06/11Build
            stb.append("         AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4");
            stb.append("                        WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE");
            stb.append("                            AND T4.PERIODCD = T1.PERIODCD");
            stb.append("                            AND T4.CHAIRCD = T1.CHAIRCD");
            stb.append("                            AND T1.DATADIV IN ('0', '1') ");
            stb.append("                            AND T4.GRADE||T4.HR_CLASS = '" + (String) paramap.get("GRADE_HR_CLASS") + "'");
            stb.append("                            AND T4.COUNTFLG = '0')");
            stb.append("         AND NOT EXISTS(SELECT   'X' FROM TEST_COUNTFLG TEST ");
            stb.append("                        WHERE ");
            stb.append("                            TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
            stb.append("                            AND TEST.PERIODCD = T1.PERIODCD ");
            stb.append("                            AND TEST.CHAIRCD  = T1.CHAIRCD ");
            stb.append("                            AND TEST.DATADIV  = T1.DATADIV) ");
        }
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + (String) paramap.get("YEAR") + "' ");
        stb.append("                             AND ATDD.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND ATDD.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        stb.append("                  ) ");
        // 勤怠コード'28'は時間割にカウントしない
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + (String) paramap.get("YEAR") + "' ");
        stb.append("                             AND ATDD.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND ATDD.REP_DI_CD = '28' ");
        stb.append("                  ) ");
        stb.append("         GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD,T3.SEMESTER,T5.DI_CD,T4.SCHREGNO, ");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("    T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, T3.SUBCLASSCD ");
        } else {
            stb.append("    T3.SUBCLASSCD ");
        }
        stb.append( ")S1 ");
        stb.append("               LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + (String) paramap.get("YEAR") + "' ");
        stb.append("                             AND ATDD.DI_CD = S1.DI_CD ");
        stb.append(    "GROUP BY S1.SCHREGNO,S1.SEMESTER,");  //05/06/20Modify
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("    S1.CLASSCD, S1.SCHOOL_KIND, S1.CURRICULUM_CD, S1.SUBCLASSCD ");
        } else {
            stb.append("    S1.SUBCLASSCD ");
        }

        stb.append(" UNION ALL ");
        stb.append(" SELECT  T1.SCHREGNO,T1.SEMESTER,");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
        } else {
            stb.append("    T1.SUBCLASSCD ");
        }
        stb.append("        ,SUM(ABSENT1) AS ABSENT1");
        stb.append("        ,SUM(LATE_EARLY) AS LATE_EARLY");
        stb.append(" FROM    ATTEND_CALC T1");
        stb.append(" GROUP BY T1.SCHREGNO,T1.SEMESTER,");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
        } else {
            stb.append("    T1.SUBCLASSCD ");
        }
        stb.append(" )");
        
        stb.append(" ,ATTEND_B AS(");
        if (definecode.absent_cov == 1 || definecode.absent_cov == 3) {
            //学期でペナルティ欠課を算出する場合
            stb.append(" SELECT  T1.SCHREGNO,");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
            } else {
                stb.append("    T1.SUBCLASSCD ");
            }
            stb.append("        ,VALUE(SUM(ABSENT),0) AS ABSENT");
            stb.append("        ,VALUE(SUM(ABSENT2),0) AS ABSENT2");
            stb.append("        ,VALUE(SUM(LATE_EARLY),0) AS LATE_EARLY");
            stb.append(" FROM(");
            stb.append("         SELECT  T1.SCHREGNO,");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
            } else {
                stb.append("    T1.SUBCLASSCD ");
            }
            stb.append("                ,VALUE(SUM(ABSENT1),0) AS ABSENT");
            if (definecode.absent_cov == 1) {
                stb.append("            ,VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + definecode.absent_cov_late + " AS ABSENT2");
            } else {
                stb.append("            ,FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + definecode.absent_cov_late + ",5,1)) AS ABSENT2");
            }
                                            //遅刻・早退はペナルティ欠課に換算分を引いて出力する場合
            stb.append("                ,VALUE(SUM(LATE_EARLY),0) - VALUE(SUM(LATE_EARLY),0) / " + definecode.absent_cov_late + " * " + definecode.absent_cov_late + " AS LATE_EARLY");
            stb.append("         FROM    ATTEND_A T1");
            stb.append("         GROUP BY T1.SCHREGNO,T1.SEMESTER,");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("        T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
            } else {
                stb.append("        T1.SUBCLASSCD ");
            }
            stb.append(" )T1");
            stb.append(" GROUP BY T1.SCHREGNO,");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
            } else {
                stb.append("    T1.SUBCLASSCD ");
            }
        } else if (definecode.absent_cov == 2 || definecode.absent_cov == 4) {
            //通年でペナルティ欠課を算出する場合
            stb.append(" SELECT  T1.SCHREGNO, ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
            } else {
                stb.append("    T1.SUBCLASSCD ");
            }
            stb.append("        ,VALUE(SUM(ABSENT1),0) AS ABSENT");
            if (definecode.absent_cov == 2) {
                stb.append("    ,VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + definecode.absent_cov_late + " AS ABSENT2");
            } else {
                stb.append("    ,FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + definecode.absent_cov_late + ",5,1)) AS ABSENT2");
            }
                                //遅刻・早退はペナルティ欠課に換算分を引いて出力する場合
            stb.append("        ,VALUE(SUM(LATE_EARLY),0) - VALUE(SUM(LATE_EARLY),0) / " + definecode.absent_cov_late + " * " + definecode.absent_cov_late + " AS LATE_EARLY");
            stb.append(" FROM    ATTEND_A T1");
            stb.append(" GROUP BY T1.SCHREGNO, ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
            } else {
                stb.append("    T1.SUBCLASSCD ");
            }
        } else {
            //ペナルティ欠課なしの場合
            stb.append(" SELECT  T1.SCHREGNO, ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
            } else {
                stb.append("    T1.SUBCLASSCD ");
            }
            stb.append("        ,VALUE(SUM(ABSENT1),0) AS ABSENT");
            stb.append("        ,VALUE(SUM(ABSENT1),0) AS ABSENT2");
            stb.append("        ,VALUE(SUM(LATE_EARLY),0) AS LATE_EARLY");
            stb.append(" FROM    ATTEND_A T1");
            stb.append(" GROUP BY T1.SCHREGNO, ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
            } else {
                stb.append("    T1.SUBCLASSCD ");
            }
        }
        stb.append(" )");

        //メイン表
        stb.append(" SELECT T2.HR_NAME,T2.ATTENDNO,T2.NAME, ");
        stb.append("       T4.SUBCLASSNAME, ");
        stb.append("       T6.CREDITS AS CREDITS, ");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
        } else {
            stb.append("    T1.SUBCLASSCD ");
        }

        for (Iterator i = _recordfieldlist.iterator(); i.hasNext();) {
            final List l = (List)i.next();
            final String chaircd = (String) l.get(0);
            final String score = (String) l.get(1);
            final String value = (String) l.get(2);
            final String value_di = (String) l.get(3);
            if (chaircd != null) {
                stb.append(" ," + chaircd);
            }
            if (score != null) {
                stb.append(" ," + score);
            }
            if (value != null) {
                if (value_di != null) {
                    stb.append(" ,CASE WHEN " + value + " IS NOT NULL THEN RTRIM(CHAR(" + value + ")) ELSE " + value_di + " END AS " + value);
                } else {
                    stb.append(" ," + value);
                }
            }
        }
        
        stb.append("       ,T3.ABSENT,T3.ABSENT2,T3.LATE_EARLY");
        stb.append(" FROM(");
        stb.append("      SELECT  SCHREGNO, ");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("    CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        } else {
            stb.append("    SUBCLASSCD ");
        }
        stb.append("      FROM    CHAIR_S");
        stb.append("      WHERE   SEMESTER <= '" + (String) paramap.get("SEMESTER") + "'");
        stb.append("      GROUP BY SCHREGNO, ");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("    CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        } else {
            stb.append("    SUBCLASSCD ");
        }
        stb.append(" ) T5");

        stb.append(" LEFT JOIN RECORD_DAT T1 ON T1.YEAR = '" + (String) paramap.get("YEAR") + "'");
        stb.append("                        AND T1.SCHREGNO = T5.SCHREGNO");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("                        AND T1.CLASSCD = T5.CLASSCD");
            stb.append("                        AND T1.SCHOOL_KIND = T5.SCHOOL_KIND");
            stb.append("                        AND T1.CURRICULUM_CD = T5.CURRICULUM_CD");
            stb.append("                        AND T1.SUBCLASSCD = T5.SUBCLASSCD");
        } else {
            stb.append("                        AND T1.SUBCLASSCD = T5.SUBCLASSCD");
        }
        stb.append(" LEFT JOIN SCHNO T2 ON T2.SCHREGNO = T5.SCHREGNO");
        stb.append(" LEFT JOIN ATTEND_B T3 ON T3.SCHREGNO = T5.SCHREGNO ");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("                        AND T3.CLASSCD = T5.CLASSCD");
            stb.append("                        AND T3.SCHOOL_KIND = T5.SCHOOL_KIND");
            stb.append("                        AND T3.CURRICULUM_CD = T5.CURRICULUM_CD");
            stb.append("                        AND T3.SUBCLASSCD = T5.SUBCLASSCD");
        } else {
            stb.append("                        AND T3.SUBCLASSCD = T5.SUBCLASSCD");
        }
        stb.append(" LEFT JOIN CREDIT_MST T6 ON T6.YEAR = '" + (String) paramap.get("YEAR") + "'");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("                        AND T6.CLASSCD = T5.CLASSCD");
            stb.append("                        AND T6.SCHOOL_KIND = T5.SCHOOL_KIND");
            stb.append("                        AND T6.CURRICULUM_CD = T5.CURRICULUM_CD");
            stb.append("                        AND T6.SUBCLASSCD = T5.SUBCLASSCD");
        } else {
            stb.append("                        AND T6.SUBCLASSCD = T5.SUBCLASSCD");
        }
        stb.append("                        AND T6.GRADE = T2.GRADE");
        stb.append("                        AND T6.COURSECD = T2.COURSECD");
        stb.append("                        AND T6.MAJORCD = T2.MAJORCD");
        stb.append("                        AND T6.COURSECODE = T2.COURSECODE");
        stb.append(" LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T5.SUBCLASSCD");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("                        AND T4.CLASSCD = T5.CLASSCD");
            stb.append("                        AND T4.SCHOOL_KIND = T5.SCHOOL_KIND");
            stb.append("                        AND T4.CURRICULUM_CD = T5.CURRICULUM_CD");
        }

        stb.append(" ORDER BY T2.ATTENDNO, ");
        if ("1".equals((String) paramap.get("useCurriculumcd"))) {
            stb.append("                        T5.CLASSCD, ");
            stb.append("                        T5.SCHOOL_KIND, ");
            stb.append("                        T5.CURRICULUM_CD, ");
            stb.append("                        T5.SUBCLASSCD ");
        } else {
            stb.append("                        T5.SUBCLASSCD ");
        }
        
        return stb.toString();
    }

    /*
     *  対象生徒学籍番号編集(SQL用) 
     */
    private String Set_Schno(String schno[]) {

        StringBuffer stb = new StringBuffer();

        for (int ia=0; ia<schno.length; ia++) {
            if( ia==0 ) stb.append("('");
            else        stb.append("','");
            stb.append(schno[ia]);
        }
        stb.append("')");

        return stb.toString();
    }
    
    /* 
     *  get parameter doGet()パラメータ受け取り 
     */
    private Map getParam (final HttpServletRequest request) {

        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }

        final Map paramap = new HashMap();
        paramap.put("YEAR", request.getParameter("YEAR"));  //年度
        paramap.put("SEMESTER", request.getParameter("GAKKI"));  //学期
        paramap.put("GRADE_HR_CLASS", request.getParameter("GRADE_HR_CLASS"));  //学年・組
        paramap.put("DATE", KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE")));  //異動基準日
        paramap.put("COUNTFLG", request.getParameter("COUNTFLG"));
        schno = request.getParameterValues("category_selected");  //学籍番号
        paramap.put("SCHNOLIST", Set_Schno(schno));  //学籍番号の編集(SQL用)

        paramap.put("TESTKINDCD", request.getParameter("TESTKINDCD"));  //テスト種別
        paramap.put("DEVIATION", request.getParameter("DEVIATION"));  //偏差値欄を出力する:on／しない:null
        paramap.put("useCurriculumcd", request.getParameter("useCurriculumcd"));  //1:教育課程コード使用

        return paramap;
    }

    protected List setTestKindList(final Map paramap) {
        final List list = new ArrayList();
        if ((String) paramap.get("TESTKINDCD") == null) { return list; }
        if (((String) paramap.get("TESTKINDCD")).equals("0202")) { return list; }

        list.add (setTestKindListDetail (paramap));
        return list;
    }
    
    protected List setTestKindListDetail(final Map paramap) {
        final List list = new ArrayList();

        final String semester = (String) paramap.get("SEMESTER");
        final String testkind = (String) paramap.get("TESTKINDCD");
        final String kindname;
        if (((String) paramap.get("TESTKINDCD")).substring(0, 2).equals("01")) kindname = "INTR";
        else if ((semester.equals("1") || semester.equals("2")) && testkind.equals("0202")) kindname = "TERM2";
        else kindname = "TERM";
        final String fieldofchaircd = "SEM" + semester + "_" + kindname + "_CHAIRCD";
        final String fieldofscore = "SEM" + semester + "_" + kindname + "_SCORE";

        list.add(semester);
        list.add(testkind);
        list.add(fieldofchaircd);
        list.add(fieldofscore);
        return list;
    }

    protected List setRecordList(final Map paramap) {

        final List list = new ArrayList();
        list.add(setRecordFieldListDetail("SEM1_INTR_CHAIRCD","SEM1_INTR_SCORE","SEM1_INTR_VALUE","SEM1_INTR_VALUE_DI","1-0101"));  // １学期中間テスト
        list.add(setRecordFieldListDetail("SEM1_TERM_CHAIRCD","SEM1_TERM_SCORE","SEM1_TERM_VALUE","SEM1_TERM_VALUE_DI","1-0201"));  // １学期中間テスト
        list.add(setRecordFieldListDetail(null,null,"SEM1_VALUE","SEM1_VALUE_DI","1-0000"));  // １学期評価

        final int s = Integer.parseInt((String)paramap.get("SEMESTER"));
        if (1 < s) {
            list.add(setRecordFieldListDetail("SEM2_INTR_CHAIRCD","SEM2_INTR_SCORE","SEM2_INTR_VALUE","SEM2_INTR_VALUE_DI","2-0101"));  // ２学期中間テスト
            list.add(setRecordFieldListDetail("SEM2_TERM_CHAIRCD","SEM2_TERM_SCORE","SEM2_TERM_VALUE","SEM2_TERM_VALUE_DI","2-0201"));  // ２学期中間テスト
            list.add(setRecordFieldListDetail(null,null,"SEM2_VALUE","SEM2_VALUE_DI","2-0000"));  // ２学期評価
        }
        if (2 < s && 2 < definecode.semesdiv) {
            list.add(setRecordFieldListDetail("SEM3_INTR_CHAIRCD","SEM3_INTR_SCORE","SEM3_INTR_VALUE","SEM3_INTR_VALUE_DI","3-0101"));  // ３学期中間テスト
            list.add(setRecordFieldListDetail("SEM3_TERM_CHAIRCD","SEM3_TERM_SCORE","SEM3_TERM_VALUE","SEM3_TERM_VALUE_DI","3-0201"));  // ３学期中間テスト
            list.add(setRecordFieldListDetail(null,null,"SEM3_VALUE","SEM3_VALUE_DI","3-0000"));  // ３学期評価
        }
        list.add(setRecordFieldListDetail(null,null,"GRAD_VALUE","GRAD_VALUE_DI","9-0000"));  // 学年評定

        return list;
    }

    protected List setRecordFieldListDetail(
            final String chaircd,
            final String score,
            final String value,
            final String value_di,
            final String kind
    ) {
        final List list = new ArrayList();
        list.add(chaircd);
        list.add(score);
        list.add(value);
        list.add(value_di);
        list.add(kind);
        return list;
    }

    //--- 内部クラス -------------------------------------------------------
    private class InnerChair{
        private String _semes;
        private String _test;
        private String _classcd;
        private String _schoolKind;
        private String _curriculumCd;
        private String _subclasscd;
        private String _subclassname;
        private String _chaircd;
        private String _chairname;
        private String _average;
        private String _schnum;
        private String _stddev;
        private Map _rankmap = new HashMap();
        private Map _deviationmap = new HashMap();
        
        private InnerChair() {}

        private void createInnerChair(
                final DB2UDB db2,
                final Map paramap
        ) throws Exception {
            setChairAverage(db2,paramap);
        }

        /*
         * 講座内項目の各値をセット
         */
        private void setChairAverage(
                final DB2UDB db2,
                final Map paramap
        ) throws Exception {

            for (Iterator i = _testkindlist.iterator(); i.hasNext();) {
                final List l = (List)i.next();
                paramap.put("SEMES_FOR_CHAIR", (String)l.get(0));
                paramap.put("TEST_FOR_CHAIR", (String)l.get(1));
                paramap.put("CHAIRCD_FIELD_FOR_CHAIR", (String)l.get(2));
                paramap.put("SCORE_FIELD_FOR_CHAIR", (String)l.get(3));
                loadChairAverage(db2, paramap);
            }
        }

        /*
         * 講座内生徒数・平均を取り出す
         */
        private void loadChairAverage(
                final DB2UDB db2,
                final Map paramap
        ) throws Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlChairAverage(paramap);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _semes = (String)paramap.get("SEMES_FOR_CHAIR");
                    _test = (String)paramap.get("TEST_FOR_CHAIR");
                    if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                        _classcd = rs.getString("CLASSCD");
                        _schoolKind = rs.getString("SCHOOL_KIND");
                        _curriculumCd = rs.getString("CURRICULUM_CD");
                    }
                    _subclasscd = rs.getString("SUBCLASSCD");
                    _chaircd = rs.getString("CHAIRCD");
                    _subclassname = rs.getString("SUBCLASSNAME");
                    _chairname = rs.getString("CHAIRNAME");
                    _average = rs.getString("AVERAGE");
                    _schnum = rs.getString("SCHNUM");
                    _stddev = rs.getString("STDDEV");

                    _rankmap = loadChairRank(db2,paramap,this);
                    if (this._stddev != null) _deviationmap = loadChairDeviation(db2,paramap,this);
                    
                    String str = _semes + "-" + _test + "-" + _subclasscd + "-" + _chaircd;
                    if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                        str = _semes + "-" + _test + "-" + _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd + "-" + _chaircd;
                    }
                    final InnerChair innerchair = new InnerChair();
                    innerchair._semes = this._semes;
                    innerchair._test = this._test;
                    innerchair._classcd = this._classcd;
                    innerchair._schoolKind = this._schoolKind;
                    innerchair._curriculumCd = this._curriculumCd;
                    innerchair._subclasscd = this._subclasscd;
                    innerchair._chaircd = this._chaircd;
                    innerchair._subclassname = this._subclassname;
                    innerchair._chairname = this._chairname;
                    innerchair._average = this._average;
                    innerchair._schnum = this._schnum;
                    innerchair._stddev = this._stddev;
                    innerchair._rankmap = this._rankmap;
                    if (this._stddev != null) innerchair._deviationmap = this._deviationmap;
                    _chairmap.put(str, innerchair);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /*
         * 講座内成績順位を取り出す
         */
        private Map loadChairRank(
                final DB2UDB db2,
                final Map paramap,
                final InnerChair innerchair
        ) throws Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map map = new HashMap();
            try {
                final String sql = sqlChairRank(paramap,innerchair);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!map.containsKey(rs.getString("SCORE"))) {
                        map.put(rs.getString("SCORE"), rs.getString("RANK"));
                    }
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        /*
         * 講座内成績偏差値を取り出す
         */
        private Map loadChairDeviation(
                final DB2UDB db2,
                final Map paramap,
                final InnerChair innerchair
        ) throws Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map map = new HashMap();
            try {
                final String sql = sqlChairDeviation(paramap,innerchair);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!map.containsKey(rs.getString("SCORE"))) {
                        map.put(rs.getString("SCORE"), rs.getString("DEVIATION"));
                    }
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }
        
        /* 
         * SQLStatement作成 講座内生徒数・平均を取得
         */
        private String sqlChairAverage(Map paramap) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH AVERAGE_DATA AS(");
            stb.append(" SELECT SUBCLASSCD");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("    , CLASSCD, SCHOOL_KIND, CURRICULUM_CD");
            }
            stb.append("       ," + (String)paramap.get("CHAIRCD_FIELD_FOR_CHAIR") + " AS CHAIRCD");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(" + (String)paramap.get("SCORE_FIELD_FOR_CHAIR") + "))*10,0)/10,5,1) AS AVERAGE");
            stb.append("       ,DECIMAL(ROUND(STDDEV(FLOAT(" + (String)paramap.get("SCORE_FIELD_FOR_CHAIR") + "))*10,0)/10,5,1) AS STDDEV");
            stb.append("       ,SUM(CASE WHEN " + (String)paramap.get("SCORE_FIELD_FOR_CHAIR") + " IS NOT NULL THEN 1 ELSE NULL END) AS SCHNUM");
            stb.append(" FROM RECORD_DAT W1");
            stb.append(" WHERE YEAR = '" + (String)paramap.get("YEAR") + "'");
            stb.append("   AND EXISTS (SELECT 'X' FROM RECORD_DAT W2");
            stb.append("               WHERE YEAR = '" + (String)paramap.get("YEAR") + "'");
            stb.append("                 AND SCHREGNO IN " + (String)paramap.get("SCHNOLIST") );
            stb.append("                 AND W2.SUBCLASSCD = W1.SUBCLASSCD");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("                 AND W2.CLASSCD = W1.CLASSCD");
                stb.append("                 AND W2.SCHOOL_KIND = W1.SCHOOL_KIND");
                stb.append("                 AND W2.CURRICULUM_CD = W1.CURRICULUM_CD");
            }
            stb.append("                 AND W2." + (String)paramap.get("CHAIRCD_FIELD_FOR_CHAIR") + " = W1." + (String)paramap.get("CHAIRCD_FIELD_FOR_CHAIR") + ")");
            stb.append(" GROUP BY ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("    CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, ");
            } else {
                stb.append("    SUBCLASSCD, ");
            }
            stb.append(" " + (String)paramap.get("CHAIRCD_FIELD_FOR_CHAIR"));
//            stb.append(" HAVING " + (String)paramap.get("CHAIRCD_FIELD_FOR_CHAIR") + " IS NOT NULL AND SUM(" + (String)paramap.get("SCORE_FIELD_FOR_CHAIR") + ") IS NOT NULL");
            stb.append(" )");
            stb.append(" SELECT ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, ");
            } else {
                stb.append("    T1.SUBCLASSCD, ");
            }
            stb.append("    T1.CHAIRCD,T1.AVERAGE,T1.SCHNUM,T2.CHAIRNAME,T3.SUBCLASSABBV AS SUBCLASSNAME");
            stb.append("        ,case when value(T1.STDDEV,0) = 0 then null else T1.STDDEV end AS STDDEV");
            stb.append(" FROM AVERAGE_DATA T1");
            stb.append(" LEFT JOIN CHAIR_DAT T2 ON T2.YEAR = '" + (String)paramap.get("YEAR") + "' AND T2.SEMESTER = '" + (String)paramap.get("SEMES_FOR_CHAIR") + "' AND T2.CHAIRCD = T1.CHAIRCD");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("                 AND T3.CLASSCD = T1.CLASSCD");
                stb.append("                 AND T3.SCHOOL_KIND = T1.SCHOOL_KIND");
                stb.append("                 AND T3.CURRICULUM_CD = T1.CURRICULUM_CD");
            }
//            log.debug("stb="+stb.toString());
            return stb.toString();
        }

        /* 
         * SQLStatement作成 講座内成績順位を取得
         */
        private String sqlChairRank(
                final Map paramap,
                final InnerChair innerchair
        ) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("    CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
            } else {
                stb.append("    SUBCLASSCD ");
            }
            stb.append("       ," + (String)paramap.get("CHAIRCD_FIELD_FOR_CHAIR") + " AS CHAIRCD");
            stb.append("       ," + (String)paramap.get("SCORE_FIELD_FOR_CHAIR") + " AS SCORE");
            stb.append("       ,RANK() OVER(ORDER BY " + (String)paramap.get("SCORE_FIELD_FOR_CHAIR") + " DESC) AS RANK");
            stb.append(" FROM RECORD_DAT W1");
            stb.append(" WHERE YEAR = '" + (String)paramap.get("YEAR") + "'");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("   AND CLASSCD = '" + innerchair._classcd + "'");
                stb.append("   AND SCHOOL_KIND = '" + innerchair._schoolKind + "'");
                stb.append("   AND CURRICULUM_CD = '" + innerchair._curriculumCd + "'");
                stb.append("   AND SUBCLASSCD = '" + innerchair._subclasscd + "'");
            } else {
                stb.append("   AND SUBCLASSCD = '" + innerchair._subclasscd + "'");
            }
            stb.append("   AND " + (String)paramap.get("CHAIRCD_FIELD_FOR_CHAIR") + " = '" + innerchair._chaircd + "'");
            stb.append("   AND " + (String)paramap.get("SCORE_FIELD_FOR_CHAIR") + " IS NOT NULL");
            stb.append("   AND EXISTS (SELECT 'X' FROM RECORD_DAT W2");
            stb.append("               WHERE YEAR = '" + (String)paramap.get("YEAR") + "'");
            stb.append("                 AND SCHREGNO IN " + (String)paramap.get("SCHNOLIST") );
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("                 AND W2.CLASSCD = W1.CLASSCD");
                stb.append("                 AND W2.SCHOOL_KIND = W1.SCHOOL_KIND");
                stb.append("                 AND W2.CURRICULUM_CD = W1.CURRICULUM_CD");
                stb.append("                 AND W2.SUBCLASSCD = W1.SUBCLASSCD");
            } else {
                stb.append("                 AND W2.SUBCLASSCD = W1.SUBCLASSCD");
            }
            stb.append("                 AND W2." + (String)paramap.get("CHAIRCD_FIELD_FOR_CHAIR") + " = W1." + (String)paramap.get("CHAIRCD_FIELD_FOR_CHAIR") + ")");
//            log.debug("stb="+stb.toString());
            return stb.toString();
        }

        /* 
         * SQLStatement作成 講座内成績偏差値を取得
         */
        private String sqlChairDeviation(
                final Map paramap,
                final InnerChair innerchair
        ) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("    CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
            } else {
                stb.append("    SUBCLASSCD ");
            }
            stb.append("       ," + (String)paramap.get("CHAIRCD_FIELD_FOR_CHAIR") + " AS CHAIRCD");
            stb.append("       ," + (String)paramap.get("SCORE_FIELD_FOR_CHAIR") + " AS SCORE");
            stb.append("       ,DECIMAL(ROUND((" + (String)paramap.get("SCORE_FIELD_FOR_CHAIR") + "-" + innerchair._average + ")/" + innerchair._stddev + "*100,0)/10+50,5,1) AS DEVIATION");
            stb.append(" FROM RECORD_DAT W1");
            stb.append(" WHERE YEAR = '" + (String)paramap.get("YEAR") + "'");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("   AND CLASSCD = '" + innerchair._classcd + "'");
                stb.append("   AND SCHOOL_KIND = '" + innerchair._schoolKind + "'");
                stb.append("   AND CURRICULUM_CD = '" + innerchair._curriculumCd + "'");
                stb.append("   AND SUBCLASSCD = '" + innerchair._subclasscd + "'");
            } else {
                stb.append("   AND SUBCLASSCD = '" + innerchair._subclasscd + "'");
            }
            stb.append("   AND " + (String)paramap.get("CHAIRCD_FIELD_FOR_CHAIR") + " = '" + innerchair._chaircd + "'");
            stb.append("   AND " + (String)paramap.get("SCORE_FIELD_FOR_CHAIR") + " IS NOT NULL");
            stb.append("   AND EXISTS (SELECT 'X' FROM RECORD_DAT W2");
            stb.append("               WHERE YEAR = '" + (String)paramap.get("YEAR") + "'");
            stb.append("                 AND SCHREGNO IN " + (String)paramap.get("SCHNOLIST") );
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("                 AND W2.CLASSCD = W1.CLASSCD");
                stb.append("                 AND W2.SCHOOL_KIND = W1.SCHOOL_KIND");
                stb.append("                 AND W2.CURRICULUM_CD = W1.CURRICULUM_CD");
                stb.append("                 AND W2.SUBCLASSCD = W1.SUBCLASSCD");
            } else {
                stb.append("                 AND W2.SUBCLASSCD = W1.SUBCLASSCD");
            }
            stb.append("                 AND W2." + (String)paramap.get("CHAIRCD_FIELD_FOR_CHAIR") + " = W1." + (String)paramap.get("CHAIRCD_FIELD_FOR_CHAIR") + ")");
//            log.debug("stb="+stb.toString());
            return stb.toString();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class MyVrw32alp extends Vrw32alp {
        public int VrsOut(String field, String data) {
            if (null == field || null == data) return 0;
            return super.VrsOut(field, data);
        }

        public int VrsOutn(String field, int gyo, String data) {
            if (null == field || null == data) return 0;
            return super.VrsOutn(field, gyo, data);
        }

        public void doSvfOutNonZero(
                final String str1,
                final String str2
        ) {
            if (null == str1 || null == str2) return;
            if (str2.equals("0")) return;

            VrsOut(str1, str2);
        }

        public void doSvfOutNonZero(
                final String str1,
                final int val
        ) {
            if (null == str1) return;
            if (0 == val) return;

            VrsOut(str1, String.valueOf(val));
        }
    }

}

// kanji=漢字
/*
 * $Id: 5226249e7a4caa85fc8aaa7b346e9475893ab8d2 $
 *
 * 作成日: 2005/05/08
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJM;

import java.io.IOException;
import java.util.ArrayList;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;



public class KNJM437D {

    private static final Log log = LogFactory.getLog(KNJM437D.class);
    //private DecimalFormat dmf1 = new DecimalFormat("00");
    private PreparedStatement ps1, ps2;
    private int maxline = 45;
    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定
    private KNJDefineSchool definecode;       //各学校における定数等設定

    /**
     *
     *  KNJD.classから最初に起動されるクラス
     *
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Vrw32alp svf = new Vrw32alp();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                  //Databaseクラスを継承したクラス
        Map paramap = new HashMap();        //リクエストパラメータ保管用
        List schlist = new ArrayList();     //リクエストパラメータ保管用
        boolean nonedata = false;

    // パラメータの取得
        getParam( request, paramap, schlist );
    // print svf設定
        sd.setSvfInit( request, response, svf);
    // ＤＢ接続
        db2 = sd.setDb(request);
        if( sd.openDb(db2)) {
            log.error("db open error");
            return;
        }
    // 印刷処理
        nonedata = printSvf( db2, svf, paramap, schlist );
    // 終了処理
        sd.closeSvf( svf, nonedata );
        sd.closeDb(db2);
    }


    /**
     *
     *  印刷処理
     *
     */
    private boolean printSvf( DB2UDB db2, Vrw32alp svf, Map paramap, List schlist )
    {
        boolean nonedata = false;
        try {
            setHead( db2, svf, paramap );           //見出し項目
            //NO002 getDivideAttendDate( db2, paramap );  //
            ps1 = db2.prepareStatement( prestatementSchreg( paramap ) );
            ps2 = db2.prepareStatement( prestatementRecord( paramap ) );

            for( Iterator i = schlist.iterator() ; i.hasNext() ; )
                if( printSvfMain( db2, svf, paramap, (String)i.next() ) ) nonedata = true;  //SVF-FORM出力処理
        } catch( Exception ex) {
            log.error("error! ",ex);
        }
        return nonedata;
    }


    /**
     *
     *  SVF-FORMセット＆見出し項目
     *
     */
    private void setHead( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        svf.VrSetForm("KNJM437D.frm", 4);
        svf.VrAttribute("NAME", "FF=1");          //生徒で改ページ

        try {
            definecode = new KNJDefineSchool();
            definecode.defineCode( db2, (String)paramap.get("YEAR") );      //各学校における定数等設定
        } catch( Exception ex) {
            log.warn("semesterdiv-get error!",ex);
        }

    }//setHead()の括り


    /**
     *
     *  SVF-FORM メイン出力処理
     *
     */
    private boolean printSvfMain( DB2UDB db2, Vrw32alp svf, Map paramap, String pschno )
    {
        //定義
        ResultSet rs = null;
        boolean nonedata = false;
log.debug("pschno :" + pschno);
        //RecordSet作成
        try {
        	List gradenamelist = getGradeName(db2, paramap, pschno);
            List semesternamelist = getSemesterName(db2, paramap);
            int pi = 0;
            ps1.setString( ++pi, pschno );      //学生番号
            rs = ps1.executeQuery();
            if( rs.next() ) printSvfOutSchreg( svf, rs, paramap, gradenamelist, semesternamelist);//学籍データをSVF-FORMへ出力
            if( rs != null )rs.close();
            db2.commit();

            pi = 0;
            ps2.setString( ++pi, pschno );      //学生番号
            rs = ps2.executeQuery();
            nonedata = printSvfOutMeisai( svf, rs, paramap );               //成績データをSVF-FORMへ出力
            if( rs != null )rs.close();
            db2.commit();
        } catch( Exception ex ) { log.error("printSvfMain read error! ", ex);   }
        return nonedata;
    }


    /**
     *   学籍データを出力
     */
    private void printSvfOutSchreg( Vrw32alp svf, ResultSet rs, Map paramap, List gradenamelist, List semesternamelist)
    {
        try {
            svf.VrsOut( "COURSE",    ( rs.getString("MAJORNAME") != null )? rs.getString("MAJORNAME") : ""   );
            svf.VrsOut( "SCHREGNO",  rs.getString("SCHREGNO") );
            svf.VrsOut( "NAME",      ( rs.getString("NAME") != null )? rs.getString("NAME") : ""   );
            svf.VrsOut( "BIRTHDAY",  ( rs.getString("BIRTHDAY") != null )? KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")) : ""   );
            //NO001Add-----↓-----
            svf.VrsOut( "HR_CLASS",  ( rs.getString("HR_NAME") != null )? rs.getString("HR_NAME") + " " + String.valueOf(rs.getInt("ATTENDNO")) + "番" : ""   );
            svf.VrsOut( "DATE",      ( (String)paramap.get("DATE") != null )? KNJ_EditDate.h_format_JP((String)paramap.get("DATE")) : ""   );
            //NO001Add-----↑-----
            svf.VrsOut( "MARK",      ( (String)paramap.get("SUB_CHK") != null )? "※は今年度の履修科目です。" : "" );//NO003

            svf.VrsOut( "MARK",      ( (String)paramap.get("SUB_CHK") != null )? "※は今年度の履修科目です。" : "" );//NO003
            svf.VrsOut( "MARK",      ( (String)paramap.get("SUB_CHK") != null )? "※は今年度の履修科目です。" : "" );//NO003
            svf.VrsOut( "MARK",      ( (String)paramap.get("SUB_CHK") != null )? "※は今年度の履修科目です。" : "" );//NO003

            int cnt1 = 1;
            for( Iterator i = gradenamelist.iterator() ; i.hasNext() ; ) {
                svf.VrsOut("GRADE"+cnt1, (String)i.next());
                int cnt2 = 1;
                for( Iterator j = semesternamelist.iterator() ; j.hasNext() ; ) {
                    svf.VrsOut("SEMESTER" + cnt1 + "_" + cnt2, (String)j.next());
                    cnt2++;
                }
                cnt1++;
            }
        } catch( Exception ex ) { log.error("printSvfOutSchreg read error! ", ex);  }
    }


    /**
     *   ＨＲ成績生徒別明細を出力 => VrEndRecord()
     */
    private boolean printSvfOutMeisai( Vrw32alp svf, ResultSet rs, Map paramap )
    {
        boolean nonedata = false;
        try {
            int linex = 0;                                      //１ページ当り出力行数
            String subclasscd = null;
            int scredits = 0;
            int totalcredits[] = {0,0,0,0,0};               //修得単位数計

            while( rs.next()) {
                //科目のbreak
                if( subclasscd == null  ||  ! rs.getString("SUBCLASSCD").equals(subclasscd)) {
                    if( subclasscd != null) {
                        printSvfOutMeisaiScredits( svf, linex, scredits, totalcredits );        //SVF-FORMへ出力
                        int ret = svf.VrEndRecord();
                        if( ret == 0 )nonedata = true;
                        linex++;
                        scredits = 0;
                    }
                    subclasscd = rs.getString("SUBCLASSCD");
                    printSvfOutMeisaiKamoku( svf, rs, linex );      //SVF-FORMへ出力
                }
                //明細の出力
                scredits += printSvfOutMeisaiTani( svf, rs, linex, scredits, totalcredits );        //SVF-FORMへ出力
//log.debug("subclasscd="+subclasscd+"   scredits="+scredits);
            }
            if( subclasscd != null )printSvfOutMeisaiScredits( svf, linex, scredits, totalcredits );        //SVF-FORMへ出力
            svf.VrEndRecord();
            linex++;  //NO002
            if( subclasscd != null )printSvfOutTotalcredits( svf, linex, totalcredits );        //SVF-FORMへ出力
        } catch( SQLException ex) {
            log.error("printSvfOutMeisai error!", ex );
        }
        return nonedata;
    }


    /**
     *   科目のデータを出力
     */
    private void printSvfOutMeisaiKamoku( Vrw32alp svf, ResultSet rs, int linex )
    {
        try {
//log.debug("classname="+rs.getString("CLASSNAME"));
            int i = 1;
            svf.VrsOut( "CLASS"    + i,  rs.getString("CLASSNAME") );         //教科名
            svf.VrsOut( "SUBCLASS" + i,  rs.getString("SUBCLASSNAME") );      //科目名
            svf.VrsOut( "R_CREDIT" + i,  rs.getString("SUBCLASS_CREDITS") );  //登録単位数
            svf.VrsOut( "SUBJECT"  + i,  ( rs.getString("ELECTDIV") != null  &&  Integer.parseInt( rs.getString("ELECTDIV") ) == 0 )? "○" : "" );    //必修科目
        } catch( SQLException ex) {
            log.error("printSvfOutMeisaiKamoku error! ", ex );
        }
    }


    /**
     *   年次の単位を出力
     */
    private int printSvfOutMeisaiTani( Vrw32alp svf, ResultSet rs, int linex, int scredits, int totalcredits[] )
    {
        int retvalue = 0;
        try {
            if( rs.getString("CREDITS") != null  &&  rs.getString("GRADE") != null  &&  rs.getString("SEMESTER") != null) {
                int grade = Integer.parseInt( rs.getString("GRADE") );
            	int semester = Integer.parseInt( rs.getString("SEMESTER") );
                if( 1 <= grade  &&  grade <= 3 && 1 <= semester && semester <= 4) {
                    if (Integer.parseInt( rs.getString("CREDITS") ) < 0) {
                        svf.VrsOut( "CREDIT" + grade + "_" + semester,  "※" );  //NO003
                    } else {
                    	//annualで出力位置を指定していたが、GRADEで1-3の範囲ならOK。
                    	//SEMESTERで、さらに1-4の範囲で出力位置を特定する。
                        svf.VrsOut( "CREDIT" + grade + "_" + semester,  rs.getString("CREDITS") );
                        retvalue =  Integer.parseInt( rs.getString("CREDITS") );
                        totalcredits[semester] += Integer.parseInt( rs.getString("CREDITS") );    //年次計
                    }
                }
            }
        } catch( SQLException ex) {
            log.error("printSvfOutMeisaiTani error! ", ex );
        }
        return retvalue;
    }


    /**
     *   科目別修得単位数を出力
     */
    private void printSvfOutMeisaiScredits( Vrw32alp svf, int linex, int scredits, int totalcredits[] )
    {
        try {
            int i = 1;
            if( 0 < scredits) {
                svf.VrsOut( "F_CREDIT" + i,  String.valueOf( scredits ) );        //修得単位数
                totalcredits[totalcredits.length - 1] += scredits;                                          //総計
            }
        } catch( Exception ex) {
            log.error("printSvfOutMeisaiScredits error! ", ex );
        }
    }


    /**
     *   修得単位数計の行を出力
     */
    private void printSvfOutTotalcredits( Vrw32alp svf, int linex, int totalcredits[] )
    {
        try {
            int k = linex % (maxline * 2);  //NO002 現ページの出力済行数
            for( int j = k; j < maxline * 2 - 1 ; j++) {  //NO002
//log.debug("k="+k+"  j="+j+"  linex="+linex);
            //for(  ; linex < maxline * 2 - 2 ; linex++) {
                if( linex % (maxline * 2) == 0  ||  linex % (maxline * 2) < 45) {  //NO002
                //if( linex / maxline == 0  ||  linex / maxline % 2 == 0){
                    svf.VrAttribute( "RECORD1", "Print=1");
                    svf.VrsOut( "CLASS1",  String.valueOf( linex ) );         //教科名---NO001Modify
                    svf.VrAttribute( "CLASS1", "Meido=100");
                    svf.VrEndRecord();
                    linex++;  //NO002
                } else{
                    svf.VrAttribute( "RECORD2", "Print=1");
                    svf.VrAttribute( "CLASS2", "Meido=100");
                    svf.VrsOut( "CLASS2",  String.valueOf( linex ) );         //教科名---NO001Modify
                    svf.VrEndRecord();
                    linex++;  //NO002
                }
            }

            for( int i = 0 ; i < totalcredits.length - 1 ; i++ )
                svf.VrsOut( "TOTAL_CREDIT" + i,  String.valueOf( totalcredits[i] ) );     //修得単位数
            svf.VrsOut( "F_TOTAL_CREDIT",  String.valueOf( totalcredits[totalcredits.length - 1] ) );     //修得単位数
            svf.VrEndRecord();

        } catch( Exception ex) {
            log.error("printSvfOutMeisaiScredits error! ", ex );
        }
    }


    /**
     *
     *   SQLStatement作成 生徒学籍情報を取得
     *
     */
    String prestatementSchreg( Map paramap ) {

        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  T1.SCHREGNO, MAJORNAME, NAME, BIRTHDAY ");
            stb.append(        ",T4.HR_NAME ,ATTENDNO ");//NO001Add
            stb.append("FROM    SCHREG_REGD_DAT T1 ");
            stb.append(        "INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(        "LEFT JOIN MAJOR_MST T3 ON T3.MAJORCD = T1.MAJORCD ");
            //NO001Add-----↓-----
            stb.append(        "LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append(                                     "AND T4.SEMESTER = T1.SEMESTER ");
            stb.append(                                     "AND T4.GRADE = T1.GRADE ");
            stb.append(                                     "AND T4.HR_CLASS = T1.HR_CLASS ");
            //NO001Add-----↑-----
            stb.append("WHERE   T1.SCHREGNO = ? AND T1.YEAR = '" + (String)paramap.get("YEAR") + "' AND ");
            stb.append(        "T1.SEMESTER = (SELECT  MAX(SEMESTER) ");
            stb.append(                       "FROM    SCHREG_REGD_DAT T2 ");
            stb.append(                       "WHERE   T2.YEAR = T1.YEAR AND ");
            stb.append(                               "T2.SCHREGNO = T1.SCHREGNO) ");
        } catch( Exception ex) {
            log.error("prestatementSchreg error! ", ex );
        }
//log.debug("prestatementSchreg = "+stb.toString());
        return stb.toString();
    }


    private List getSemesterName(final DB2UDB db2, Map paramap) {
    	List retlist = new ArrayList();
    	boolean retflg = false;

    	PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sqlSemesterName(paramap);
//            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	retlist.add(rs.getString("SEMESTERNAME"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    	return retlist;
    }

    /*
     * 学期文字列
     */
    private String sqlSemesterName(Map paramap) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" select SEMESTER, SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + (String)paramap.get("YEAR") + "' AND SEMESTER <> '9' ORDER BY SEMESTER ");
        return stb.toString();
    }

    private List getGradeName(final DB2UDB db2, Map paramap, final String schregno) {
    	List retlist = new ArrayList();
    	boolean retflg = false;

    	PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sqlGradeName(paramap, schregno);
//            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	retlist.add(rs.getString("GRADE_NAME1"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    	return retlist;
    }
    /*
     * 学年文字列
     */
    String sqlGradeName(Map paramap, final String schregno)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(   "SELECT DISTINCT T1.SCHREGNO, T1.YEAR, T1.GRADE, T3.GRADE_NAME1 ");
        stb.append(   "FROM    SCHREG_REGD_DAT T1 ");
        stb.append(   " LEFT JOIN SCHREG_REGD_GDAT T3 ");
        stb.append(   "   ON  T3.YEAR = T1.YEAR ");
        stb.append(   "   AND T3.GRADE = T1.GRADE ");
        stb.append(   "WHERE   T1.SCHREGNO = '" + schregno + "' ");
        stb.append(   "  AND T1.YEAR <= '" + (String)paramap.get("YEAR") + "' ");
        stb.append(   "  AND T1.SEMESTER = ( ");
        stb.append(   "    SELECT  MAX(SEMESTER) ");
        stb.append(   "    FROM    SCHREG_REGD_DAT T2 ");
        stb.append(   "    WHERE   T2.YEAR = T1.YEAR ");
        stb.append(   "      AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(   " ) ");
        stb.append(   "ORDER BY T1.YEAR, T1.GRADE");
        return stb.toString();
    }
    /**
     *   SQLStatement作成 学習記録データ
     *   NO002 Modify
     */
    String prestatementRecord( Map paramap )
    {
//log.debug("paramap="+paramap);
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH ");
            //学籍の表
            stb.append("SCHNO_A AS(");
            stb.append(   "SELECT  T1.SCHREGNO, T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.SEMESTER, ");
            stb.append(           "T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append(   "FROM    SCHREG_REGD_DAT T1 ");
            stb.append(   "WHERE   T1.SCHREGNO = ? ");
            stb.append(       "AND T1.YEAR <= '" + (String)paramap.get("YEAR") + "' ");
            stb.append(       "AND T1.SEMESTER = (SELECT  MAX(SEMESTER) ");
            stb.append(                          "FROM    SCHREG_REGD_DAT T2 ");
            stb.append(                          "WHERE   T2.YEAR = T1.YEAR ");
            stb.append(                              "AND T2.SCHREGNO = T1.SCHREGNO) ");
            stb.append(   ") ");

            //該当する単位マスターの表
            stb.append(",SUBCLASS_CREDIT AS(");
            stb.append(   "SELECT  T4.YEAR, T4.GRADE, T4.SEMESTER, ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append(           "T6.CLASSCD, ");
                stb.append(           "T6.SCHOOL_KIND, ");
                stb.append(           "T6.CURRICULUM_CD, ");
            }
            stb.append(           "T6.SUBCLASSCD, ");
            stb.append(           "T6.CREDITS ");
            stb.append(   "FROM    CREDIT_MST T6 ");
            stb.append(          ",SCHNO_A T4 ");
//            stb.append(          ",SCHREG_STUDYREC_DAT T2 ");
            stb.append(          ",RECORD_SCORE_HIST_DAT T2 ");
            stb.append(   "WHERE   T6.YEAR = T4.YEAR ");
            stb.append(       "AND T6.GRADE = T4.GRADE ");
            stb.append(       "AND T6.COURSECD = T4.COURSECD ");
            stb.append(       "AND T6.MAJORCD = T4.MAJORCD ");
            stb.append(       "AND T6.COURSECODE = T4.COURSECODE ");
            stb.append(       "AND T6.YEAR = T2.YEAR ");
            stb.append(       "AND T4.SCHREGNO = T2.SCHREGNO ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append(       "AND T6.CLASSCD = T2.CLASSCD ");
                stb.append(       "AND T6.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append(       "AND T6.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append(       "AND T6.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("             AND  T2.TESTKINDCD = '99' ");
            stb.append("             AND T2.TESTITEMCD = '00' ");
            stb.append("             AND T2.SCORE_DIV = '09' ");
            stb.append("             AND T2.SEQ = ( ");
            stb.append("               SELECT ");
            stb.append("                  MAX(RSHD.SEQ) ");
            stb.append("               FROM ");
            stb.append("                  RECORD_SCORE_HIST_DAT RSHD ");
            stb.append("               WHERE ");
            stb.append("                  RSHD.YEAR = T2.YEAR ");
            stb.append("                  AND RSHD.SCHREGNO = T2.SCHREGNO ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append("                  AND RSHD.CLASSCD = T2.CLASSCD ");
                stb.append("                  AND RSHD.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("                  AND RSHD.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("                  AND RSHD.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("                  AND RSHD.SEMESTER = T2.SEMESTER ");
            stb.append("                  AND RSHD.TESTKINDCD = T2.TESTKINDCD ");
            stb.append("                  AND RSHD.TESTITEMCD = T2.TESTITEMCD ");
            stb.append("                  AND RSHD.SCORE_DIV = T2.SCORE_DIV ");
            stb.append("             )");
            stb.append(   ") ");

            //単位数合計の表
            stb.append(",SUBCLASS_CREDIT_TOTAL AS(");
            stb.append(   "SELECT  SUBCLASSCD, ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append(           "CLASSCD, ");
                stb.append(           "SCHOOL_KIND, ");
                stb.append(           "CURRICULUM_CD, ");
            }
            stb.append(" SUM(CREDITS) AS CREDITS ");
            stb.append(   "FROM    SUBCLASS_CREDIT ");
            stb.append(   "GROUP BY SUBCLASSCD ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append(           ", CLASSCD ");
                stb.append(           ", SCHOOL_KIND ");
                stb.append(           ", CURRICULUM_CD ");
            }
            stb.append(   ") ");

            //今年度の履修科目（講座名簿に登録されている科目）の表 NO003 Add
            stb.append(",SUBCLASS_STD AS(");
            stb.append(   "SELECT  T1.SCHREGNO, ");
            stb.append(           "T4.GRADE, ");
            stb.append(           "T4.SEMESTER, ");
            stb.append(           "T2.SUBCLASSCD ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append(           ", T2.CLASSCD ");
                stb.append(           ", T2.SCHOOL_KIND ");
                stb.append(           ", T2.CURRICULUM_CD ");
            }
            stb.append(   "FROM    CHAIR_STD_DAT T1 ");
            stb.append(          ",SCHNO_A T4 ");
            stb.append(          ",CHAIR_DAT T2 ");
            stb.append(   "WHERE   T1.YEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(       "AND T1.YEAR = T4.YEAR ");
            stb.append(       "AND T1.SCHREGNO = T4.SCHREGNO ");
            stb.append(       "AND T1.YEAR = T2.YEAR ");
            stb.append(       "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(       "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(       "AND SUBSTR(T2.SUBCLASSCD,1,2) <= '90' ");//NO003
            stb.append(       "AND NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_DAT R1 ");//NO003
            stb.append(                      " WHERE R1.YEAR = T1.YEAR ");
            stb.append(                        " AND R1.ANNUAL = T4.GRADE ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append(                        " AND R1.ATTEND_CLASSCD = T2.CLASSCD ");
                stb.append(                        " AND R1.ATTEND_SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append(                        " AND R1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append(                        " AND R1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD) ");
            stb.append(   ") ");

            //履修済み科目の表 NO003 Modify
            stb.append(",STUDYREC AS(");
            stb.append(   "SELECT  T1.SCHREGNO, ");
            stb.append(           "T4.GRADE, ");
            stb.append(           "T1.SEMESTER, ");
            stb.append(           "T1.CLASSCD, ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append(           "T1.SCHOOL_KIND, ");
                stb.append(           "T1.CURRICULUM_CD, ");
            }
            stb.append(           "T1.SUBCLASSCD, ");
            stb.append(           "min(T9.CLASSABBV) AS CLASSNAME, ");
            stb.append(           "min(T10.SUBCLASSABBV) AS SUBCLASSNAME, ");
            stb.append(           "sum(T1.GET_CREDIT) AS GET_CREDIT, ");
            stb.append(           "sum(T1.ADD_CREDIT) AS ADD_CREDIT ");
//            stb.append(   "FROM    SCHREG_STUDYREC_DAT T1 ");
            stb.append(    " FROM RECORD_SCORE_HIST_DAT T1 ");
            stb.append(    " LEFT JOIN SCHNO_A T4 ON T1.SCHREGNO = T4.SCHREGNO AND T1.YEAR = T4.YEAR AND T1.SEMESTER = T4.SEMESTER ");
            stb.append(    " LEFT JOIN CLASS_MST T9 ON T9.CLASSCD = T1.CLASSCD AND T9.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append(    " LEFT JOIN SUBCLASS_MST T10 ON T10.CLASSCD = T1.CLASSCD AND T10.SCHOOL_KIND = T1.SCHOOL_KIND AND T10.CURRICULUM_CD = T1.CURRICULUM_CD AND T10.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(   "WHERE   T1.SCHREGNO = T4.SCHREGNO AND smallint(T4.GRADE) > 0 AND smallint(T1.SEMESTER) > 0 ");
            stb.append(     "AND   T4.YEAR = '" + (String)paramap.get("YEAR") + "' ");////
            stb.append(     "AND   SUBSTR(T1.SUBCLASSCD,1,2) <= '90' ");//NO003
            stb.append(     "AND   NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_DAT R1 ");//NO003
            stb.append(                      " WHERE R1.YEAR = T1.YEAR ");
            stb.append(                        " AND R1.ANNUAL = T4.GRADE ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append(                        " AND R1.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append(                        " AND R1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(                        " AND R1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(                        " AND R1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ");
            if (paramap.get("SUB_CHK") != null) //履修科目
                stb.append( "AND T1.YEAR <> '" + (String)paramap.get("YEAR") + "' ");
            stb.append("             AND T1.TESTKINDCD = '99' ");
            stb.append("             AND T1.TESTITEMCD = '00' ");
            stb.append("             AND T1.SCORE_DIV = '09' ");
            stb.append("             AND T1.SEQ = ( ");
            stb.append("               SELECT ");
            stb.append("                 MAX(RSHD.SEQ) ");
            stb.append("               FROM ");
            stb.append("                 RECORD_SCORE_HIST_DAT RSHD ");
            stb.append("               WHERE ");
            stb.append("                 RSHD.YEAR = T1.YEAR ");
            stb.append("                 AND RSHD.SCHREGNO = T1.SCHREGNO ");
            stb.append("                 AND RSHD.CLASSCD = T1.CLASSCD ");
            stb.append("                 AND RSHD.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("                 AND RSHD.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("                 AND RSHD.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("                 AND RSHD.SEMESTER = T1.SEMESTER ");
            stb.append("                 AND RSHD.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("                 AND RSHD.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("                 AND RSHD.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("             )");
            stb.append(   "GROUP BY T1.SCHREGNO, T4.GRADE, T1.SEMESTER, T1.CLASSCD, T1.SUBCLASSCD ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append(           ", T1.SCHOOL_KIND ");
                stb.append(           ", T1.CURRICULUM_CD ");
            }
            stb.append(   ") ");

            //メイン表
            stb.append("SELECT  T1.GRADE, T1.SEMESTER, T1.CLASSCD, ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append(           "T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append(           "T1.SUBCLASSCD, ");
            }
            stb.append(        "VALUE(T3.ELECTDIV,'0') AS ELECTDIV, ");
            stb.append(        "VALUE(T1.CLASSNAME,T2.CLASSABBV) AS CLASSNAME, ");
            stb.append(        "VALUE(T1.SUBCLASSNAME,T3.SUBCLASSABBV) AS SUBCLASSNAME, ");
            stb.append(        "CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.ADD_CREDIT,0) + VALUE(T1.GET_CREDIT,0) ");
            stb.append(             "ELSE T1.GET_CREDIT END AS CREDITS, ");
            stb.append(        "T6.CREDITS AS SUBCLASS_CREDITS ");
            stb.append("FROM    STUDYREC T1 ");
            //stb.append("INNER JOIN SCHNO_A T4 ON T1.SCHREGNO = T4.SCHREGNO AND T1.ANNUAL = T4.ANNUAL ");
            stb.append("LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append(           " AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append("LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append(           " AND T3.CLASSCD = T1.CLASSCD ");
                stb.append(           " AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(           " AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("LEFT JOIN SUBCLASS_CREDIT_TOTAL T6 ON T6.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                stb.append(           " AND T6.CLASSCD = T1.CLASSCD ");
                stb.append(           " AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(           " AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }

            //履修科目NO003Add
            if (paramap.get("SUB_CHK") != null) {
                stb.append("WHERE   NOT EXISTS(SELECT 'X' FROM SUBCLASS_STD T2 ");
                stb.append(                   " WHERE T2.GRADE = T1.GRADE AND T2.SEMESTER = T1.SEMESTER AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
                if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                    stb.append(           " AND T2.CLASSCD = T1.CLASSCD ");
                    stb.append(           " AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append(           " AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("                  ) ");
                stb.append("UNION ");
                stb.append("SELECT  T1.GRADE, T1.SEMESTER, SUBSTR(T1.SUBCLASSCD,1,2) AS CLASSCD, ");
                if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                    stb.append(           "T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
                } else {
                    stb.append(           "T1.SUBCLASSCD, ");
                }
                stb.append(        "VALUE(T3.ELECTDIV,'0') AS ELECTDIV, ");
                stb.append(        "T2.CLASSABBV AS CLASSNAME, ");
                stb.append(        "T3.SUBCLASSABBV AS SUBCLASSNAME, ");
                stb.append(        "CASE WHEN T1.SUBCLASSCD IS NULL THEN NULL ELSE -1 END AS CREDITS, ");
                stb.append(        "T6.CREDITS AS SUBCLASS_CREDITS ");
                stb.append("FROM    SUBCLASS_STD T1 ");
                stb.append("LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
                if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                    stb.append(        " AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                }
                stb.append("LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
                if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                    stb.append(        " AND T3.CLASSCD = T1.CLASSCD ");
                    stb.append(        " AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append(        " AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("LEFT JOIN SUBCLASS_CREDIT_TOTAL T6 ON T6.SUBCLASSCD = T1.SUBCLASSCD ");
                if ("1".equals((String) paramap.get("useCurriculumcd"))) {
                    stb.append(        " AND T6.CLASSCD = T1.CLASSCD ");
                    stb.append(        " AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append(        " AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
            }

            stb.append("ORDER BY 2,4,3,1 ");
        } catch( Exception ex) {
            log.error("prestatementRecord error! ", ex );
        }
log.debug("prestatementRecord = "+stb.toString());
        return stb.toString();
    }

    /**
     *  get parameter doGet()パラメータ受け取り
     */
    private void getParam( HttpServletRequest request, Map paramap, List schlist )
    {
        log.fatal("$Revision: 59384 $"); // CVSキーワードの取り扱いに注意
        try {
            paramap.put( "YEAR",       request.getParameter("YEAR") );  //年度                        0
            paramap.put( "GAKKI",      request.getParameter("GAKKI"));  //現在学期                    1
            paramap.put( "GRADE_HR_CLASS",      request.getParameter("GRADE_HR_CLASS"));  //学年・組  2
            paramap.put( "DATE",       request.getParameter("DATE"));   //作成日---NO001Add
            paramap.put( "SUB_CHK",    request.getParameter("SUB_CHK"));//履修科目---NO003Add on:出力する null:出力しない
            paramap.put( "useCurriculumcd",    request.getParameter("useCurriculumcd"));

            String schno[] = request.getParameterValues("category_selected");       //学籍番号
            for( int i = 0 ; i < schno.length ; i++ )schlist.add( schno[i] );

        } catch( Exception ex ) {
            log.error("request.getParameter error!",ex);
        }
    }

}

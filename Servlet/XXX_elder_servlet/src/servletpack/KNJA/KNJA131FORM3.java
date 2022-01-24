/*
 * $Id: 3371368c293874361d478f8abdae140e2d2ce892 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;

/**
 *
 *  学校教育システム 賢者 [学籍管理]  生徒指導要録  中等教育学校用（千代田区立九段）
 *                                                    各教科・科目の学習の記録（前期課程）
 *
 *  2005/12/27 Build yamashiro
 *  2006/03/23 yamashiro ○評定は学習記録データから出力する --NO001
 *  2006/04/24 yamashiro・名称マスターに表示用組( 'A021'+HR_CLASS で検索 )の処理を追加  --NO003
 *                            => 無い場合は従来通りHR_CLASSを出力
 */

public class KNJA131FORM3 extends KNJA131BASE
{
    private static final Log log = LogFactory.getLog(KNJA131FORM3.class);

    private StringBuffer stb;
    private ResultSet rs;

    int totalcredit;                //総合修得単位数
    private Map hmap;       //NO003


    /**
     *  SVF-FORM 印刷処理
     */
    boolean printSvf( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            setSvfForm( svf, paramap );
            printHeader( db2, svf, paramap );  //年次・ホームルーム・整理番号
            printSvfDetail_1( db2, svf, paramap );  //観点・評定
            ret = svf.VrEndPage();
            nonedata = true;
        } catch( Exception ex ){
            log.debug( "printSvf error!", ex );
        }
        return nonedata;
    }


    /**
     *  SVF-FORM設定
     */
    void setSvfForm( Vrw32alp svf, Map paramap )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            ret = svf.VrSetForm("KNJA131_3.frm", 4);
        } catch( Exception ex ){
            log.debug( "svf.VrSetForm error!", ex );
        }
    }


    /**
     *  SVF-FORM 印刷処理 明細
     *  観点
     */
    void printSvfDetail_1( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            int p = 0;
            ( ( PreparedStatement )paramap.get("PS_VIEW_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_VIEW_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );  //年度
            ( ( PreparedStatement )paramap.get("PS_VIEW_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_VIEW_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );  //年度
            ( ( PreparedStatement )paramap.get("PS_VIEW_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_VIEW_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_VIEW_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );  //年度
            rs = ( ( PreparedStatement )paramap.get("PS_VIEW_RECORD") ).executeQuery();

            int g = 0;               //年次
            int requireviewnum = 0;  //必修科目観点の行番号
            int requirevaluenum = 0; //必修科目評定の行番号
            String classcd = null;   //教科コードの保管
            String viewcd = null;    //観点コードの保管
            final Map sentakuKamoku = new TreeMap();
            while( rs.next() ){
                g = Integer.parseInt( rs.getString("GRADE") );
                if( rs.getString("ELECTDIV") != null  &&  rs.getString("ELECTDIV").equals("1") ){  //06/03/23 ELECTDIV=NULLに対応
                    if (sentakuKamoku.containsKey(rs.getString("CLASSCD"))) {
                        PrintClass printClass = (PrintClass) sentakuKamoku.get(rs.getString("CLASSCD"));
                        printClass.setViewClass(rs.getString("VIEWCD"), rs.getString("VIEWNAME"), g, rs.getString("VIEW"), rs.getString("NAME1"));
                    } else {
                        sentakuKamoku.put(rs.getString("CLASSCD"), new PrintClass(rs.getString("CLASSNAME"),
                                rs.getString("VIEWCD"),
                                rs.getString("VIEWNAME"),
                                g,
                                rs.getString("VIEW"),
                                rs.getString("NAME1")));
                    }
                } else{
                    //if( 8 < requirevaluenum )continue;  //06/03/23
//log.debug("requirevaluenum="+requirevaluenum);
                    if( classcd == null  ||  !rs.getString("CLASSCD").equals(classcd) ){
                        requireviewnum = GetViewNum( requireviewnum, requirevaluenum );  //必修科目観点の行設定
                        printSvfSubjectName( svf, rs, ++requirevaluenum, 0 );  //必修教科名出力
                        classcd = rs.getString("CLASSCD");
                    }
                    if( viewcd == null  ||  !rs.getString("VIEWCD").equals(viewcd) ){
                        ++requireviewnum;  //必修科目観点の行番号加算
                        viewcd = rs.getString("VIEWCD");
                    }
                    printSvfRequireView( svf, rs, g, requireviewnum );  //必修教科の観点 06/03/23Modify
                    printSvfSubjectValue( svf, rs, g, requirevaluenum, 0 );  //必修教科の評定 06/03/23Modify
                }
            }
            rs.close();
            for (final Iterator iter = sentakuKamoku.keySet().iterator(); iter.hasNext();) {
                final String key = (String) iter.next();
                final PrintClass printClass = (PrintClass) sentakuKamoku.get(key);
                printSentakuKamoku(svf, printClass, ++requirevaluenum);
            }
        } catch( Exception ex ){
            log.debug( "printSvfDetail_1 error!", ex );
        }
    }

    private void printSentakuKamoku(final Vrw32alp svf, final PrintClass printClass, final int n) {
        final String className = printClass._className;

        int cnt = 0;
        svf.VrAttribute("CLASS2", "Keta=10");

        svf.VrsOutn("CLASS3_1_2", n, className.substring(0, 2));    //評定教科名
        svf.VrsOutn("CLASS3_1_3", n, className.substring(2));       //評定教科名

        svf.VrsOutn("ASSESS3_1",  n,  printClass._assess1); //評定
        svf.VrsOutn("ASSESS3_2",  n,  printClass._assess2); //評定
        svf.VrsOutn("ASSESS3_3",  n,  printClass._assess3); //評定

        for (final Iterator iter = printClass._viewClassMap.keySet().iterator(); iter.hasNext();) {
            svf.VrsOut("CLASS2", className);
            final String key = (String) iter.next();
            final ViewClass viewClass = (ViewClass) printClass._viewClassMap.get(key);
            svf.VrsOut("VIEW2", viewClass._viewName);
            svf.VrsOut("ASSESS2_1", viewClass._view1);
            svf.VrsOut("ASSESS2_2", viewClass._view2);
            svf.VrsOut("ASSESS2_3", viewClass._view3);
            svf.VrEndRecord();
            cnt++;
        }
        for (int i = cnt; i < printClass.getLineCnt(); i++) {
            svf.VrsOut("CLASS2", className);
            svf.VrsOut("VIEW2", "");
            svf.VrsOut("ASSESS2_1", "");
            svf.VrsOut("ASSESS2_2", "");
            svf.VrsOut("ASSESS2_3", "");
            svf.VrEndRecord();
        }
    }

    /**
     *  SVF-FORM 印刷処理 明細  必修教科名出力
     *
     */
    void printSvfSubjectName( Vrw32alp svf, ResultSet rs, int n, int electdiv )
    {
        if( 9 < n ) return;  //06/03/23
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        int num = 0;
        try {
            if( electdiv == 1 )ret = svf.VrsOut( "GRADE" + n,  String.valueOf( Integer.parseInt( rs.getString("GRADE") ) ) );  //選択教科年次

            num = rs.getString("CLASSNAME").length();
            ret = svf.VrsOut( "CLASS" + ( ( 0 == electdiv )? 1: 2 ) + "_" + ( ( 5 < num )? 2: 1 ) + "_" + n,  rs.getString("CLASSNAME") );    //観点教科名

            if( electdiv == 1 )n += 9;  //選択教科の場合

            if( num <= 3 ){
                ret = svf.VrsOutn( "CLASS3_1_1",  n,  rs.getString("CLASSNAME") );  //評定教科名
            } else if( num <= 4 ){
                ret = svf.VrsOutn( "CLASS3_1_2",  n,  rs.getString("CLASSNAME").substring( 0, 2 ) );  //評定教科名
                ret = svf.VrsOutn( "CLASS3_1_3",  n,  rs.getString("CLASSNAME").substring( 2 )    );  //評定教科名
            } else if( num <= 6 ){
                ret = svf.VrsOutn( "CLASS3_1_2",  n,  rs.getString("CLASSNAME").substring( 0, 3 ) );  //評定教科名
                ret = svf.VrsOutn( "CLASS3_1_3",  n,  rs.getString("CLASSNAME").substring( 3 )    );  //評定教科名
            } else if( num <= 8 ){
                ret = svf.VrsOutn( "CLASS3_1_2",  n,  rs.getString("CLASSNAME").substring( 0, 4 ) );  //評定教科名
                ret = svf.VrsOutn( "CLASS3_1_3",  n,  rs.getString("CLASSNAME").substring( 4 )    );  //評定教科名
            } else{
                ret = svf.VrsOutn( "CLASS3_1_2",  n,  rs.getString("CLASSNAME").substring( 0, 5 ) );  //評定教科名
                ret = svf.VrsOutn( "CLASS3_1_3",  n,  rs.getString("CLASSNAME").substring( 5 )    );  //評定教科名
            }

        } catch( Exception ex ){
            log.debug( "printSvfRequireView error!", ex );
        }
    }


    /**
     *  SVF-FORM 印刷処理 明細  必修教科の観点
     *
     */
    void printSvfRequireView( Vrw32alp svf, ResultSet rs, int g, int n )
    {
        if( 37 < n ) return;  //06/03/23
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            ret = svf.VrsOutn( "VIEW1",  n,  rs.getString("VIEWNAME") );  //観点項目
            if( rs.getString("VIEW") != null )ret = svf.VrsOutn( "ASSESS1_" + g,  n,  rs.getString("VIEW") );  //評価
        } catch( Exception ex ){
            log.debug( "printSvfRequireView error!", ex );
        }
    }


    /**
     * 必修教科および選択教科の評定を印字します。
     * @param svf
     * @param rs
     * @param g 出力列（学年）
     * @param n 出力行
     * @param electdiv 必修科目=0 選択科目=1
     */
    void printSvfSubjectValue(
            final Vrw32alp svf,
            final ResultSet rs,
            final int g,
            int n,
            final int electdiv
    ) {
        if (9 < n) { return; }
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        String value = null;
        try {
            if (1 == electdiv) {
                n += 9;
                value = rs.getString("NAME1");
            } else {
                value = rs.getString("VALUE");
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
        if (null != value) { ret = svf.VrsOutn("ASSESS3_" + g,  n,  value); }
    }

    /**
     * 教科ごとに、必修教科および選択教科の観点の行開始位置を取得します。
     * @param viewnum
     * @param valuenum
     * @return
     */
    private int GetViewNum(
            final int viewnum,
            final int valuenum
    ) {
        if (0 == valuenum) { return 0; }
        return ((valuenum - 1) * 4 + 5);
    }


    /**
     *  PrepareStatementオブジェクト作成
     */
    public void prepareSqlState( DB2UDB db2, Map paramap, KNJDefineCode definecode )
    {
        try {
            if( paramap.containsKey("PS_VIEW_RECORD") )return;
            //成績データ
            if( ! paramap.containsKey("PS_VIEW_RECORD") )
                paramap.put("PS_VIEW_RECORD",  db2.prepareStatement( prestatViewRecord( paramap ) ) );
            prepareSqlStateSub(db2, paramap);
        } catch( Exception ex ){
            log.debug("prepareSqlState", ex );
        }
    }

    /** 教科クラス */
    private class PrintClass {
        final String _className;
        final int _nameLen;
        final Map _viewClassMap = new TreeMap();
        String _assess1;
        String _assess2;
        String _assess3;

        public PrintClass(
                final String className,
                final String viewCd,
                final String viewName,
                final int grade,
                final String view,
                final String name1
        ) {
            _className = className;
            _nameLen = className.length();
            setViewClass(viewCd, viewName, grade, view, name1);
        }

        public void setViewClass(
                final String viewCd,
                final String viewName,
                final int grade,
                final String view,
                final String name1
        ) {
            setAssess(grade, name1);
            if (_viewClassMap.containsKey(viewCd)) {
                ViewClass viewClass = (ViewClass) _viewClassMap.get(viewCd);
                viewClass.setView(grade, view);
            } else {
                _viewClassMap.put(viewCd, new ViewClass(viewName, grade, view));
            }
        }

        private void setAssess(final int grade, final String assess) {
            switch (grade) {
            case 1:
                _assess1 = assess != null ? assess : _assess1;
                break;
            case 2:
                _assess2 = assess != null ? assess : _assess2;
                break;
            case 3:
                _assess3 = assess != null ? assess : _assess3;
                break;
            default:
                break;
            }
        }

        public String getName() {
            return _className;
        }

        public int getDataCnt() {
            return _viewClassMap.size();
        }

        public int getLineCnt() {
            return _nameLen < getDataCnt() ? getDataCnt() : _nameLen;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            String val = "";
            for (final Iterator iter = _viewClassMap.keySet().iterator(); iter.hasNext();) {
                final String key = (String) iter.next();
                final ViewClass viewClass = (ViewClass) _viewClassMap.get(key);
                val += "\n" + _className + " 名前長さ：" + _nameLen + "とデータ長:" + _viewClassMap.size() + "\n" + " 観点：" + viewClass.toString() + "\n";
            }
            return val;
        }
    }

    /** 観点クラス */
    private class ViewClass {
        final String _viewName;
        String _view1;
        String _view2;
        String _view3;

        public ViewClass(
                final String viewName,
                final int grade,
                final String view
        ) {
            _viewName = viewName;
            setView(grade, view);
        }

        public void setView(final int grade, final String view) {
            switch (grade) {
            case 1:
                _view1 = view;
                break;
            case 2:
                _view2 = view;
                break;
            case 3:
                _view3 = view;
                break;
            default:
                break;
            }
        }

        public String getName() {
            return _viewName;
        }

        public String getView(final int grade) {
            switch (grade) {
            case 1:
                return _view1;
            case 2:
                return _view2;
            case 3:
                return _view3;
            default:
                return "";
            }
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "名称:" + _viewName + " 1年:" + _view1 + " 2年:" + _view2 + " 3年:" + _view3;
        }
    }

    /**
     *  PrepareStatement close
     */
    public void closePrepareState( DB2UDB db2, Map paramap )
    {
        try {
            if( ! paramap.containsKey("PS_VIEW_RECORD") )( ( PreparedStatement )paramap.get("PS_VIEW_RECORD") ).close();
            if( ! paramap.containsKey("PS_SCHOOL_INFO") )( ( PreparedStatement )paramap.get("PS_SCHOOL_INFO") ).close();
            if( ! paramap.containsKey("PS_SCHREG_INFO") )( ( PreparedStatement )paramap.get("PS_SCHREG_INFO") ).close();
            if( ! paramap.containsKey("PS_REGD_RECORD") )( ( PreparedStatement )paramap.get("PS_REGD_RECORD") ).close();
        } catch( Exception ex ){
            log.debug( "closePrepareState error!", ex );
        }
    }

    
    /**
     * 作成  観点・評定取得のＳＱＬ文を戻します。
     */

    private String prestatViewRecord( final Map paramap ) {

        StringBuffer stb = new StringBuffer();

        stb.append("WITH ");
        stb.append("VIEW_DATA AS( ");
        stb.append("SELECT  VIEWCD, YEAR, STATUS AS VIEW ");
        stb.append("FROM    JVIEWSTAT_DAT T1 ");
        stb.append("WHERE   T1.SCHREGNO = ? ");
        stb.append(    "AND T1.YEAR <= ? ");
        stb.append(    "AND T1.SEMESTER = '9' ");
        stb.append(    "AND SUBSTR(T1.VIEWCD,3,2) <> '99' ");
        stb.append(") ");

        stb.append(",VALUE_DATA AS( ");
        stb.append("SELECT  CLASSCD, YEAR, MAX(VALUATION) AS VALUE ");
        stb.append("FROM    SCHREG_STUDYREC_DAT T1 ");
        stb.append("WHERE   T1.SCHREGNO = ? ");
        stb.append(    "AND T1.YEAR <= ? ");
        stb.append("GROUP BY YEAR, CLASSCD ");
        stb.append(") ");

        stb.append(",SCHREG_DATA AS( ");
        stb.append("SELECT  YEAR,GRADE  ");
        stb.append("FROM    SCHREG_REGD_DAT  ");
        stb.append("WHERE   SCHREGNO = ? ");
        stb.append(    "AND YEAR IN (SELECT  MAX(YEAR)  ");
        stb.append(                 "FROM    SCHREG_REGD_DAT  ");
        stb.append(                 "WHERE   SCHREGNO = ? ");
        stb.append(                     "AND YEAR <= ? ");
        stb.append(                 "GROUP BY GRADE)  ");
        stb.append("GROUP BY YEAR,GRADE  ");
        stb.append(") ");

        // 選択教科「国語(31)・数学(33)・英語(39)」以外の表
        stb.append("SELECT  T2.GRADE, T3.ELECTDIV, T3.CLASSCD, T3.CLASSNAME ");
        stb.append(       ",CASE WHEN T3.SHOWORDER IS NOT NULL THEN T3.SHOWORDER ELSE -1 END AS SHOWORDERCLASS ");
        stb.append(       ",T2.VIEWCD,T2.VIEWNAME,T2.SHOWORDERVIEW ");
        stb.append(       ",T1.VIEW,T5.VALUE,T6.NAME1 ");
        stb.append("FROM  ( SELECT  W2.YEAR,W2.GRADE,W1.VIEWCD,VIEWNAME ");
        stb.append(               ",CASE WHEN W1.SHOWORDER IS NOT NULL THEN W1.SHOWORDER ELSE -1 END AS SHOWORDERVIEW ");
        stb.append(        "FROM    JVIEWNAME_MST W1 ");
        stb.append(               ",SCHREG_DATA W2 ");
        stb.append(        "WHERE  SUBSTR(W1.VIEWCD,1,2) NOT IN('31','33','39') ");
        stb.append(      ")T2 ");
        stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T2.VIEWCD,1,2)  ");
        stb.append("LEFT JOIN VIEW_DATA T1 ON T1.YEAR = T2.YEAR AND T1.VIEWCD = T2.VIEWCD  ");
        stb.append("LEFT JOIN VALUE_DATA T5 ON T5.YEAR = T2.YEAR AND T5.CLASSCD = SUBSTR(T2.VIEWCD,1,2) ");
        stb.append("LEFT JOIN NAME_MST T6 ON T6.NAMECD1 = 'D001' AND T6.NAMECD2 = CHAR(T5.VALUE) ");

        // 選択教科「国語(31)・数学(33)・英語(39)」の表・・・必修教科「国語(11)・数学(15)・英語(22)」を元に生成。
        stb.append("UNION ALL ");
        stb.append("SELECT  T2.GRADE, '1' AS ELECTDIV, T3.CLASSCD, T3.CLASSNAME ");
        stb.append(       ",CASE WHEN T3.SHOWORDER IS NOT NULL THEN T3.SHOWORDER ELSE -1 END AS SHOWORDERCLASS ");
        stb.append(       ",T2.VIEWCD,T2.VIEWNAME,T2.SHOWORDERVIEW ");
        stb.append(       ",T1.VIEW,T5.VALUE ");
        stb.append(       ",CASE WHEN T5.VALUE IN(1,2) THEN 'C' ");
        stb.append(             "WHEN T5.VALUE IN(3) THEN 'B' ");
        stb.append(             "WHEN T5.VALUE IN(4,5) THEN 'A' ");
        stb.append(             "END AS NAME1 ");
        stb.append("FROM  ( SELECT  W2.YEAR,W2.GRADE,W1.VIEWCD AS VIEWCD2,W1.VIEWNAME ");
        stb.append(               ",CASE WHEN SUBSTR(W1.VIEWCD,1,2) = '11' THEN '31' || SUBSTR(W1.VIEWCD,3,2) ");
        stb.append(                     "WHEN SUBSTR(W1.VIEWCD,1,2) = '15' THEN '33' || SUBSTR(W1.VIEWCD,3,2) ");
        stb.append(                     "WHEN SUBSTR(W1.VIEWCD,1,2) = '22' THEN '39' || SUBSTR(W1.VIEWCD,3,2) ");
        stb.append(                     "END AS VIEWCD ");
        stb.append(               ",CASE WHEN W1.SHOWORDER IS NOT NULL THEN W1.SHOWORDER ELSE -1 END AS SHOWORDERVIEW ");
        stb.append(        "FROM    JVIEWNAME_MST W1 ");
        stb.append(               ",SCHREG_DATA W2 ");
        stb.append(        "WHERE  SUBSTR(W1.VIEWCD,1,2) IN('11','15','22') ");
        stb.append(          "AND  W2.GRADE IN('02','03') ");
        stb.append(      ")T2 ");
        stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T2.VIEWCD,1,2)  ");
        stb.append("LEFT JOIN VIEW_DATA T1 ON T1.YEAR = T2.YEAR AND T1.VIEWCD = T2.VIEWCD2  ");
        stb.append("LEFT JOIN VALUE_DATA T5 ON T5.YEAR = T2.YEAR AND T5.CLASSCD = SUBSTR(T2.VIEWCD2,1,2) ");

        stb.append("ORDER BY SHOWORDERCLASS, SHOWORDERVIEW, VIEWCD, GRADE ");

        return stb.toString();
    }

}

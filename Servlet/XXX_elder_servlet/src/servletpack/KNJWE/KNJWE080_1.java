// kanji=漢字
/*
 * $Id: 7f25e8025598239384f1c1925911b88fc0aa6dc0 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2009-2015 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJWE;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJE.detail.KNJ_AttendrecSql;
import servletpack.KNJE.detail.KNJ_ExamremarkSql;
import servletpack.KNJWE.detail.KNJ_StudyrecSql;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJSvfFieldModify;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;

/*
 *  学校教育システム 賢者 [進路情報管理]  学業成績証明書（日本語）
 *
 *  2004/04/09 yamashiro・忌引日数が出力されない不具合を修正
 *  2004/11/05 yamashiro・記載責任者表記の不具合を修正
 *  2004/11/15 yamashiro・科目名を文字数(=>byte)により大きさを変えて出力する
 *  2004/11/19 yamashiro・学習の欄において教科間の空白行を除外する
 *  2005/07/19 yamashiro・学習の記録欄において右側に教科のみ出力される不具合を修正
 *                        nullデータの処理を修正
 *  2005/08/04 yamashiro
 *  2005/08/26 yamashiro 近大付属版の校長名出力仕様変更
 *  2005/09/07 yamashiro 2005/08/26の変更を無効とする
 *  2005/09/13 yamashiro 学習の欄において最後に出力する教科の空白行を除外する 04/11/19における積み残し
 *  2005/11/18 yamashiro  「処理日付をブランクで出力する」仕様の追加による修正
 *                          => 年度の算出は、処理日付がブランクの場合は印刷指示画面から受け取る「今年度」、処理日付がある場合は処理日付から割り出した年度
 *                        学校情報を'今年度'と'卒業年度'の２種類を取得( =>Edit_SchoolInfoSqlにおいて )
 */

public class KNJWE080_1 {

    private static final Log log = LogFactory.getLog(KNJWE080_1.class);

    public Vrw32alp svf = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    public DB2UDB db2;                      //Databaseクラスを継承したクラス
    public boolean nonedata;
    public PreparedStatement ps1,ps2,ps3,ps4,ps5,ps6,ps7;
    int ret;                            //ＳＶＦ応答値
    private int gline = 39;             //学習記録評価出力MAX行数
    private StringBuffer stb;           // 04/11/05Add
    public KNJEditString knjobj = new KNJEditString();  //05/07/12Build
    private KNJWE080_1.outDetailCommonClass outobj;  //各出力様式のクラス 05/08/04 Build
    private KNJSvfFieldModify svfobj;   //フォームのフィールド属性変更 05/12/14 Build
    public final KNJDefineSchool _definecode;  // 各学校における定数等設定
    public boolean _isJuniorHiSchool;
    public boolean _isChristianEra;
    public boolean _isHosei;
    public boolean _isJisyuukan;
    public boolean _isYuushinkan;
    public boolean _isHiroshima;
    private boolean _seirekiFlg = false;
    TreeMap _gradeMap = new TreeMap();  // 学年（年度）出力列
    TreeMap _titleMap = new TreeMap(); // 学年（年度）項目名
    TreeMap _titleYear = new TreeMap(); // 学年（年度）項目名
    private static Map _w029Map;

    public KNJWE080_1(){
        _definecode = new KNJDefineSchool();  // 各学校における定数等設定
    }

    public KNJWE080_1(
            final DB2UDB db2,
            final Vrw32alp svf,
            final KNJDefineSchool definecode
    ) throws SQLException{
        this.db2 = db2;
        this.svf = svf;
        nonedata = false;
        _definecode = definecode;
        setJuniorHiSchool();
        _isHiroshima = ("HIRO".equals(definecode.schoolmark) && !_isYuushinkan);
        setChristianEra();
        _w029Map = loadW029Map(db2);
    }

    private Map loadW029Map(final DB2UDB db2) throws SQLException {
        final Map retMap = new HashMap();
        ResultSet rs = null;
        String sql = "SELECT * FROM V_NAME_MST WHERE NAMECD1 = 'W029' ORDER BY YEAR, NAMECD1 ";
        try {
            db2.query(sql);
            rs = db2.getResultSet();
            Map setNamecd2Map = new HashMap();
            String befYear = "";
            while (rs.next()) {
                if (!befYear.equals(rs.getString("YEAR"))) {
                    setNamecd2Map = new HashMap();
                }
                setNamecd2Map.put(rs.getString("NAME1"), rs.getString("NAMECD2"));
                retMap.put(rs.getString("YEAR"), setNamecd2Map);
                befYear = rs.getString("YEAR");
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return retMap;
    }

    private boolean isW029(final String year, final String subclassCd) {
        if (!_w029Map.containsKey(year)) {
            return false;
        }
        final Map setNamecd2Map = (Map) _w029Map.get(year);
        return setNamecd2Map.containsKey(subclassCd);
    }

    /*
     *  PrepareStatement作成
     */
    public void pre_stat(final String hyotei) {
        try {
            setSeirekiFlg();
            // 学習記録データ
            KNJ_StudyrecSql obj_StudyrecSql = new KNJ_StudyrecSql("off","off",2, false, _isHosei);
            ps1 = db2.prepareStatement(obj_StudyrecSql.pre_sql_new("SELECT"));
            // 出欠記録データ
            KNJ_AttendrecSql obj_AttendrecSql = new KNJ_AttendrecSql();
            ps2 = db2.prepareStatement(obj_AttendrecSql.pre_sql());
            // 所見データ
            KNJ_ExamremarkSql obj_ExamremarkSql = new KNJ_ExamremarkSql();
            ps4 = db2.prepareStatement(obj_ExamremarkSql.pre_sql_ent(""));
            // 個人データ
            KNJ_PersonalinfoSql obj_Personalinfo = new KNJ_PersonalinfoSql();
            ps6 = db2.prepareStatement(obj_Personalinfo.sql_info_reg("1111001000"));
            // 学校データ
            ps7 = getPreStatementSchoolInfo();
        } catch( Exception e ){
            log.error("[KNJE080_1]pre_stat error! ", e );
        }
    }

    /*
     *  PrepareStatement作成
     */
    public void pre_stat(final String schregno, final String year, final String hyotei) {
        try {
            setSeirekiFlg();
            // 学習記録データ
            KNJ_StudyrecSql obj_StudyrecSql = new KNJ_StudyrecSql("off","off",2, false, _isHosei);
            ps1 = db2.prepareStatement(obj_StudyrecSql.pre_sql_new("SELECT"));
            // 出欠記録データ
            KNJ_AttendrecSql obj_AttendrecSql = new KNJ_AttendrecSql();
            ps2 = db2.prepareStatement(obj_AttendrecSql.pre_sql(db2, year));
            // 所見データ
            KNJ_ExamremarkSql obj_ExamremarkSql = new KNJ_ExamremarkSql();
            ps4 = db2.prepareStatement(obj_ExamremarkSql.pre_sql_ent(""));
            // 個人データ
            KNJ_PersonalinfoSql obj_Personalinfo = new KNJ_PersonalinfoSql();
            ps6 = db2.prepareStatement(obj_Personalinfo.sql_info_reg("1111001000"));
            // 学校データ
            ps7 = getPreStatementSchoolInfo();
            //タイトル
            _titleMap.clear();
            _gradeMap.clear();
            _titleYear.clear();
            setGradeTitleMain(schregno, year);
        } catch( Exception e ){
            log.error("[KNJE080_1]pre_stat error! ", e );
        }
    }

    /**
     * 有効な[年度/学年]をメンバ変数 Map _gradeMap に追加するメソッドを呼んでいます。<br>
     * 学習の記録欄・出欠の記録欄・所見等の欄における[年度/学年]列名を印字するメソッドを呼んでいます。
     * 
     * @param schregno
     * @param year
     * @param object
     */
    protected void setGradeTitleMain(final String schregno, final String year) {

        PreparedStatement ps = null;
        try {
            // 学習データ
            KNJ_StudyrecSql obj_StudyrecSql = new KNJ_StudyrecSql("off","off",2, false, _isHosei);
            ps = db2.prepareStatement(obj_StudyrecSql.pre_sql_new("TITLE"));

            setGradeTitleGakunensei(schregno, year, ps, "STUDY_REC");
        } catch (final SQLException e) {
            log.error("SQLException : 出欠", e);
        }

        try {
            // 出欠データ
            KNJ_AttendrecSql obj_AttendrecSql = new KNJ_AttendrecSql();
            ps = db2.prepareStatement(obj_AttendrecSql.preSqlTitle(db2, year));

            setGradeTitleGakunensei(schregno, year, ps, "ATTEND");
        } catch (final SQLException e) {
            log.error("SQLException : 出欠", e);
        }

        final Map title = new TreeMap(_titleYear);
        int fieldCnt = 1;
        _titleYear.clear();
        for (final Iterator iter = title.keySet().iterator(); iter.hasNext();) {
            final String keyYear = (String) iter.next();
            final Integer valAnnual = (Integer) title.get(keyYear);
            log.debug(keyYear);
            final String keyStrAnnual = "0" + valAnnual;
            final Integer field = new Integer(fieldCnt);

            _titleYear.put(keyYear, field);

            String nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(keyYear));
            _titleMap.put(field, nendo + "年度");

            _gradeMap.put(keyStrAnnual, valAnnual);

            fieldCnt++;
        }
    }

    /**
     * 学年制の場合、 有効な[年度/学年]をメンバ変数 Map _gradeMap に追加します。<br>
     * 学習の記録欄・出欠の記録欄・所見等の欄における[年度/学年]列名を印字するメソッドを呼んでいます。
     * 
     * @param schregno
     * @param year
     * @param ps
     */
    protected void setGradeTitleGakunensei(
            final String schregno,
            final String year,
            final PreparedStatement ps,
            final String dataDiv
        ) {
        int p = 1;
        ResultSet rs = null;
        try {
            if (dataDiv.equals("STUDY_REC")) {
                ps.setString(p++, schregno);
                ps.setString(p++, schregno);
            }

            if (dataDiv.equals("ATTEND")) {
                ps.setString(p++, schregno);
                ps.setString(p++, year);
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                final String keyYear = rs.getString("YEAR");
                final String keyStrAnnual = rs.getString("ANNUAL");
                final Integer keyIntAnnual = Integer.valueOf(keyStrAnnual);
                if (!_titleYear.containsKey(keyYear)) {
                    _titleYear.put(keyYear, keyIntAnnual);
                }
            }
            rs.close();
        } catch (NumberFormatException e) {
            log.error("NumberFormatException", e);
        } catch (SQLException e) {
            log.error(dataDiv + " : SQLException", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        db2.commit();
    }

    private void setSeirekiFlg() {
        try {
            String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                if (rs.getString("NAME1").equals("2")) _seirekiFlg = true; //西暦
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
        }
    }

    /**
     * @return 学校情報
     * @throws Exception
     */
    public PreparedStatement getPreStatementSchoolInfo() throws Exception {
        if (_definecode.schoolmark.equals("KIN") || _definecode.schoolmark.equals("KINJUNIOR")) {
            servletpack.KNJZ.detail.KNJ_SchoolinfoSql obj_SchoolinfoSql = null;
            obj_SchoolinfoSql = new servletpack.KNJZ.detail.KNJ_SchoolinfoSql("12100");
            return db2.prepareStatement( obj_SchoolinfoSql.pre_sql() );
        } else if (_isHiroshima) {
            servletpack.KNJG.detail.KNJ_SchoolinfoSql obj_SchoolinfoSql = null;
            obj_SchoolinfoSql = new servletpack.KNJG.detail.KNJ_SchoolinfoSql("12100");
            return db2.prepareStatement( obj_SchoolinfoSql.pre_sql() );
        } else {
            servletpack.KNJG.detail.KNJ_SchoolinfoSql obj_SchoolinfoSql = null;
            obj_SchoolinfoSql = new servletpack.KNJG.detail.KNJ_SchoolinfoSql("12000");
            return db2.prepareStatement( obj_SchoolinfoSql.pre_sql() );
        }
    }

    /*
     *  PrepareStatement close <- KNJD070_1
     */
    public void pre_stat_f() {
        try {
            ps1.close();
            ps2.close();
            ps4.close();
            ps6.close();
            ps7.close();
        } catch( Exception e ){
            log.error("[KNJE080_1]pre_stat_f error! ", e );
        }
    }

    /**
     * 単位制の場合、
     * 学習の記録欄・出欠の記録欄等の欄における[年度/学年]列名を印字します。
     * @param _titlemap
     */
    private void printGradeTitleTanisei() {
        int ret = 0;
        if (false && 0 != ret) {
            ret = 0;
        }
        Set mapi = _titleMap.keySet();
        for (Iterator t = mapi.iterator(); t.hasNext();) {
            Integer value = (Integer) t.next();
            String str = (String) _titleMap.get(value);
            ret = svf.VrsOut("GRADE1_" + value.toString() + "_1", str.substring(0, 2)); // 学習の記録
                                                                                        // 左
            ret = svf.VrsOut("GRADE1_" + value.toString() + "_2", str.substring(2, str.length() - 2));
            ret = svf.VrsOut("GRADE1_" + value.toString() + "_3", str.substring(str.length() - 2));
            ret = svf.VrsOut("GRADE2_" + value.toString() + "_1", str.substring(0, 2)); // 学習の記録
                                                                                        // 右
            ret = svf.VrsOut("GRADE2_" + value.toString() + "_2", str.substring(2, str.length() - 2));
            ret = svf.VrsOut("GRADE2_" + value.toString() + "_3", str.substring(str.length() - 2));

            ret = svf.VrsOut("GRADE3_" + value.toString() + "_1", str.substring(0, 2)); // 出欠の記録
                                                                                        // 左
            ret = svf.VrsOut("GRADE3_" + value.toString() + "_2", str.substring(2, str.length() - 2));
            ret = svf.VrsOut("GRADE3_" + value.toString() + "_3", str.substring(str.length() - 2));
            ret = svf.VrsOut("GRADE6_" + value.toString() + "_1", str.substring(0, 2)); // 出欠の記録
                                                                                        // 左
            ret = svf.VrsOut("GRADE6_" + value.toString() + "_2", str.substring(2, str.length() - 2));
            ret = svf.VrsOut("GRADE6_" + value.toString() + "_3", str.substring(str.length() - 2));

            ret = svf.VrsOut("GRADE5_" + value.toString() + "_1", str.substring(0, 2)); // 指導上参考となる諸事項
            ret = svf.VrsOut("GRADE5_" + value.toString() + "_2", str.substring(2, str.length() - 2));
            ret = svf.VrsOut("GRADE5_" + value.toString() + "_3", str.substring(str.length() - 2));
            ret = svf.VrsOut("GRADE4_" + value.toString(), str); // 特別活動の記録

            ret = svf.VrsOutn("GRADE3", value.intValue(), str); // 出欠の記録
        }
    }

    /*
     *  学校情報
     *  2005/08/26 Modify 校長名を'X X(姓)   X X(名)'と編集して出力
     *  2005/11/18 Modify 処理日付の「入力なし」に対応
     */
    public void head_out(
            final String year,
            final String date,
            final String staffcd,
            final int annual,
            final Map paramap
    ) {
        setClasscode(year);
        int gradeHigh = (_isJuniorHiSchool && (3 < annual)) ? annual - 3 : annual;
        outobj.svfPrintSchoolInfo(year, date, staffcd, gradeHigh, paramap);  // 学校情報出力
        printGradeTitleTanisei();
    }

    /*
     *  個人情報
     *  2005/07/12 Modify BIRTHDAY出力を追加
     */
    public void address_out(
            final String schregno,
            final String year,
            final String semes,
            final String kanji,
            final String number
    ) {
        try {
//            if( _definecode == null )setClasscode( year );
        } catch( Exception ex ){
            log.error( "definecode error!", ex );
        }

        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            int pp = 0;
            ps6.setString( ++pp, schregno );
            ps6.setString( ++pp, year     );
            ps6.setString( ++pp, semes    );
            ps6.setString( ++pp, schregno );
            ps6.setString( ++pp, year );
            ResultSet rs = ps6.executeQuery();

//            if( number != null )ret = svf.VrsOut( "CERTIFNO",  number);     //証明書番号
            if ( rs.next() )outobj.address_out_Detail( schregno, year, semes, number, rs );  //05/08/04 Modify
            if( rs != null )rs.close();
            db2.commit();
        } catch( Exception e ){
            log.error("address_out error! ", e );
        }
    }

    /*
     *  出欠データ
     *  2005/07/12 Modify
     *  2005/07/19 Modify nullデータの処理を修正
     **/
    public void attend_out(
            final String schregno,
            final String year
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            ps2.setString(1,schregno);
            ps2.setString(2,year);
            ResultSet rs = ps2.executeQuery();
            int i = 0;

            while ( rs.next() ){
                Integer position = null;
                String strKey;
                strKey = rs.getString("YEAR");
                if (null == strKey) {
                    continue;
                }
                int intKey = Integer.parseInt(strKey);
                if (0 == intKey) {
                    strKey = "0";
                }
                if (!_titleYear.containsKey(strKey))
                    continue;
                position = (Integer) _titleYear.get(strKey);
                i = position.intValue();

                if( rs.getString("ATTEND_1")       != null )ret = svf.VrsOutn( "attend_1",  i,  rs.getString("ATTEND_1") );         //授業日数
                if( rs.getString("SUSPEND")        != null )ret = svf.VrsOutn( "attend_2",  i,  rs.getString("SUSPEND") );          //出停
                if( rs.getString("MOURNING")       != null )ret = svf.VrsOutn( "attend_3",  i,  rs.getString("MOURNING") );         //忌引 04/04/09修正
                if( rs.getString("ABROAD")         != null )ret = svf.VrsOutn( "attend_4",  i,  rs.getString("ABROAD") );           //留学
                if( rs.getString("REQUIREPRESENT") != null )ret = svf.VrsOutn( "attend_5",  i,  rs.getString("REQUIREPRESENT") );   //要出席
                if( rs.getString("ATTEND_6")       != null )ret = svf.VrsOutn( "attend_6",  i,  rs.getString("ATTEND_6") );         //欠席
                if( rs.getString("PRESENT")        != null )ret = svf.VrsOutn( "attend_7",  i,  rs.getString("PRESENT") );          //出席
            }
            if( rs != null )rs.close();
            db2.commit();
        } catch( Exception e ){
            log.error("[KNJE080_1]attend_out error! ", e );
        }
    }

    /*
     *  所見データ
     *  2005/07/19 Modify
     */
    public void exam_out(
            final String schregno,
            final String year
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            int pp = 0;
            ps4.setString( ++pp, schregno );
            ps4.setString( ++pp, schregno );
            ps4.setString( ++pp, year );
            ResultSet rs = ps4.executeQuery();
            KNJ_EditEdit edit = null;
            Vector v_edit = null;
            int ia = 0;

            while( rs.next() ){
                if ("0".equals(_definecode.schooldiv)) {
                    ia = rs.getInt("ANNUAL");       //年次
                    if (_isJuniorHiSchool && (3 < ia)) ia = ia - 3;
                } else {
                    Integer position = null;
                    String strKey = rs.getString("YEAR");
                    if (null == strKey) { continue; }
                    int intKey = Integer.parseInt(strKey);
                    if (0 == intKey) { strKey = "0"; }
                    if (!_gradeMap.containsKey(strKey)) continue;
                    position = (Integer) _gradeMap.get(strKey);
                    ia = position.intValue();
                }
                edit = new KNJ_EditEdit(rs.getString("ATTENDREC_REMARK"));
                v_edit = edit.get_token(40,1,1);
                for ( Enumeration e_edit = v_edit.elements(); e_edit.hasMoreElements(); ) {
                    ret = svf.VrsOutn( "attend_8_1", ia, e_edit.nextElement().toString() ); //出欠備考
                }
            }
            if( rs != null )rs.close();
            db2.commit();
        } catch( Exception e ){
            log.error("[KNJE080_1]exam_out error! ", e );
        }
    }

    /*
     *  学習の記録
     *  2005/07/19 Modify 行数カウントを修正
     *                    nullデータの処理を修正
     */
    public void study_out(
            final String schregno,
            final String year
    ) throws SQLException {
        ResultSet rs = null;
        try {
            int pp = 1;
            ps1.setString(pp++, schregno);
            ps1.setString(pp++, schregno);
            rs = ps1.executeQuery();

            final String sougouClassCd = "11";
            final List printList = new ArrayList();
            final PrintTotalData printTotalData = new PrintTotalData();
            int classValution = 0;
            int classCnt = 0;
            int classField = 1;
            int totalClassValution = 0;
            int totalClassCnt = 0;
            boolean hasData = false;
            String befClassCd = "";
            String befSubclassCd = "";
            LineData lineData = null;
            while (rs.next()) {

                final String valution = getValuation(rs.getString("SCHOOLCD"), String.valueOf(rs.getInt("VALUATION")));
                final int getCredit = null == rs.getString("GET_CREDIT") ? 0 : rs.getInt("GET_CREDIT");
                final String rsYear = rs.getString("YEAR");
                printTotalData.setTotalCredit(rsYear, rs.getString("CLASSCD"), getCredit);
                if (rs.getString("CLASSCD").equals(sougouClassCd)) {
                    continue;
                }
                if (!hasData || !befClassCd.equals(rs.getString("CLASSCD")) || !befSubclassCd.equals(rs.getString("SUBCLASSCD"))) {
                    if (hasData && (!befClassCd.equals(rs.getString("CLASSCD")) || !befSubclassCd.equals(rs.getString("SUBCLASSCD")))) {
                        printList.add(lineData);
                    }
                    if (hasData && !befClassCd.equals(rs.getString("CLASSCD"))) {
                        classValution = 0;
                        classCnt = 0;
                        classField++;
                    }
                    lineData = new LineData(rs.getString("CLASSCD"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSCD"), rs.getString("SUBCLASSNAME"), 0);
                }
                if (rs.getInt("SCHOOLCD") < 2 && null != rs.getString("VALUATION")) {
                    final int valuation = null == rs.getString("VALUATION") ? 0 : rs.getInt("VALUATION");
                    classValution += valuation;
                    classCnt++;
                    totalClassValution += valuation;
                    totalClassCnt++;
                }
                lineData.setPrintData(getCredit, rsYear, rs.getString("ANNUAL"), valution);
                befClassCd = rs.getString("CLASSCD");
                befSubclassCd = rs.getString("SUBCLASSCD");
                nonedata = true;
                hasData = true;
            }
            if (hasData) {
                printList.add(lineData);
            } else {
                // 学習情報がない場合の処理
                svf.VrsOut("CLASSCD", "A"); // 教科コード
                svf.VrEndRecord();
                nonedata = true;
            }

            //明細印字
            final int fieldCnt = meisaiPrintOut(printList, printTotalData);

            final int fieldMax = fieldCnt > 1 ? 23 : 22;
            for (int i = fieldCnt; i < fieldMax; i++) {
                if (i == fieldCnt) {
                    svf.VrsOut("CLASSCD", ""); // 教科コード
                } else {
                    svf.VrsOut("CLASSCD", String.valueOf(i)); // 教科コード
                }
                svf.VrEndRecord();
                nonedata = true;
            }

        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    public class LineData {
        final String _classCd;
        final String _className;
        final String _subclassCd;
        final String _subclassName;
        final List _printData;
        int _totalCredit = 0;
        public LineData(
                final String classCd,
                final String className,
                final String subclassCd,
                final String subclassName,
                final int totalCredit
        ) {
            _classCd = classCd;
            _className = className;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _printData = new ArrayList();
            _totalCredit += totalCredit;
        }
        public void setPrintData(final int credit, final String year, final String annual, final String value) {
            _totalCredit += credit;
            PrintData printData = new PrintData(year, annual, value, credit);
            _printData.add(printData);
        }
    }

    private class PrintData {
        final String _year;
        final String _annual;
        final String _value;
        final int _credit;

        public PrintData(final String year, final String annual, final String value, final int credit) {
            _year = year;
            _annual = annual;
            _value = value;
            _credit = credit;
        }
    }

    public class PrintTotalData {
        final String sougouClassCd = "11";
        Map _syouKeiMap = new TreeMap();
        Map _sougouMap = new TreeMap();
        Map _ryugakuMap = new TreeMap();
        Map _goukeiMap = new TreeMap();

        public PrintTotalData() {
        }

        public void setTotalCredit(final String year, final String classCd, final int credit) {
            if (sougouClassCd.equals(classCd)) {
                Integer sougou = _sougouMap.containsKey(year) ? (Integer) _sougouMap.get(year) : new Integer(0);
                _sougouMap.put(year, new Integer(sougou.intValue() + credit));
            } else {
                Integer syoukei = _syouKeiMap.containsKey(year) ? (Integer) _syouKeiMap.get(year) : new Integer(0);
                _syouKeiMap.put(year, new Integer(syoukei.intValue() + credit));
            }
            Integer goukei = _goukeiMap.containsKey(year) ? (Integer) _goukeiMap.get(year) : new Integer(0);
            _goukeiMap.put(year, new Integer(goukei.intValue() + credit));
        }
    }

    /**
     * 評定データ
     */
    public String getValuation(final String schoolCd, final String valuation) {
        if (schoolCd.equals("0")) {
            return null != valuation ? valuation : "";
        } else if (schoolCd.equals("1")) {
            return null != valuation ? valuation : "";
        } else if (schoolCd.equals("2")) {
            return "大";
        } else if (schoolCd.equals("3")) {
            return "高";
        } else if (schoolCd.equals("4")) {
            return "技";
        } else {
            return "履中";
        }
    }

    public int meisaiPrintOut(final List printList, final PrintTotalData printTotalData) {
        printKei("subtotal", printTotalData._syouKeiMap);
        printKei("CREDIT_3_", printTotalData._sougouMap);
        printKei("total", printTotalData._goukeiMap);

        int fieldCnt = 1;
        String befClassCd = "";
        for (final Iterator itLine = printList.iterator(); itLine.hasNext();) {
            final LineData lineData = (LineData) itLine.next();
            svf.VrsOut("CLASSCD", lineData._classCd); // 教科コード
            if (!befClassCd.equals(lineData._classCd)) {
                final String field = lineData._className.length() > 5 ? "2" : "1";
                svf.VrsOut("CLASSNAME" + field, lineData._className);
            }
            if (null != lineData._subclassName) {
                final String field = lineData._subclassName.length() > 10 ? "2" : "1";
                svf.VrsOut("SUBCLASSNAME" + field, lineData._subclassName);
            }
            for (final Iterator itPrint = lineData._printData.iterator(); itPrint.hasNext();) {
                final PrintData printData = (PrintData) itPrint.next();
                final Integer fieldNo = (Integer) _titleYear.get(printData._year);
                if (!isW029(printData._year, lineData._subclassCd)) {
                    svf.VrsOut("GRADES" + fieldNo, String.valueOf(printData._value));
                } else {
                    svf.VrsOut("GRADES" + fieldNo, "-");
                }
                svf.VrsOut("TANI" + fieldNo, String.valueOf(printData._credit));
            }
            svf.VrsOut("CREDIT", String.valueOf(lineData._totalCredit));
            befClassCd = lineData._classCd;
            fieldCnt++;
            svf.VrEndRecord();
        }

        nonedata = true;
        return fieldCnt;
    }

    public void printKei(final String fieldName, final Map printData) {
        int totalVal = 0;
        for (final Iterator iter = printData.keySet().iterator(); iter.hasNext();) {
            final String key = (String) iter.next();
            Integer val = (Integer) printData.get(key);
            final Integer fieldNo = (Integer) _titleYear.get(key);
            svf.VrsOut(fieldName + fieldNo, val.toString());
            totalVal += val.intValue();
        }
        svf.VrsOut(fieldName + "5", String.valueOf(totalVal));
    }

    /*
     *  クラス内で使用する定数設定
     *    2005/08/04 Build
     */
    public void setClasscode(final String year) {
        try {
            _definecode.defineCode(db2, year);  // 各学校における定数等設定
            log.debug("schoolmark="+_definecode.schoolmark);

            if (_definecode.schoolmark.equals("TOK") || _isYuushinkan) {
                outobj = new outDetailTokyoClass();
            } else {
                outobj = new outDetailCommonClass();
            }
        } catch( Exception ex ){
            log.warn("defineCode error! ",ex);
        }
    }

    /**
     * @param isJuniorHiSchool 設定する isJuniorHiSchool。
     */
    void setJuniorHiSchool() {
        try {
            db2.query("SELECT NAMESPARE2,NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2 = '00'");
            ResultSet rs = db2.getResultSet();
            if (rs.next()) {
                String str = rs.getString("NAMESPARE2");
                if (str != null) {
                    _isJuniorHiSchool = true;
                }

                str = rs.getString("NAME1");
                if ("HOUSEI".equals(str)) {
                    _isHosei = true;
                }
                if ("Yuushinkan".equals(str)) {
                    _isYuushinkan = true;
                }
                if ("jisyukan".equals(str)) {
                    _isJisyuukan = true;
                }
            }
            db2.commit();
            rs.close();
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
    }

    /**
     * 西暦フラグをセット。
     */
    void setChristianEra() {
        try {
            db2.query("SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2 = '00' AND NAME1='2'");
            ResultSet rs = db2.getResultSet();
            if (rs.next()) {
                String str = rs.getString("NAME1");
                if (str != null) {
                    _isChristianEra = true;
                }
            }
            db2.commit();
            rs.close();
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
    }

    /**
     * 西暦に変換。
     * 
     * @param  strx     : '2008/03/07' or '2008-03-07'
     * @param  pattern  : 'yyyy年M月d日生'
     * @return hdate    : '2008年3月7日生'
     */
    String getChristianEra(String strx, String pattern) {
        String hdate = new String();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern("yyyy-MM-dd");
                dat = sdf.parse( strx );
            } catch ( Exception e ) {
                try {
                    sdf.applyPattern("yyyy/MM/dd");
                    dat = sdf.parse( strx );
                } catch ( Exception e2 ) {
                    hdate = "";
                    return hdate;
                }
            }
            SimpleDateFormat sdfseireki = new SimpleDateFormat(pattern);
            hdate = sdfseireki.format(dat);
        } catch ( Exception e3 ) {
            hdate = "";
        }
        return hdate;
    }

    //--- 内部クラス -------------------------------------------------------
    // 標準出力仕様のクラス
    private class outDetailCommonClass {

        /*
         *  学校情報
         */
        void svfPrintSchoolInfo(
                final String year,
                final String date,
                final String staffcd,
                final int annual,
                Map paramap
        ) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            try {
                int pp = 0;
                String year2 = (null != date)? servletpack.KNJG.KNJG010_1.b_year(date): (String)paramap.get("CTRL_YEAR");
                ps7.setString( ++pp, year2);  // 年度
                ps7.setString( ++pp, "006" );   //証明書種別
                ps7.setString( ++pp, "006" );   //証明書種別
                ps7.setString( ++pp, year2);  // 年度
                ps7.setString( ++pp, staffcd );    //記載責任者 05/12/06 Build
                ps7.setString( ++pp, year  );  //対象年度
                ResultSet rs = ps7.executeQuery();
                if ( rs.next() ){
                    if( rs.getString("SCHOOLNAME1") != null )   ret = svf.VrsOut( "school_name",  rs.getString("SCHOOLNAME1") );      //学校名
                    if (_seirekiFlg && null != date) {
                        svf.VrsOut("DATE", date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date));
                    } else {
                        svf.VrsOut( "DATE", ( date != null )? KNJ_EditDate.h_format_JP(date): "　　年 　月 　日" );   //記載日 05/11/18
                    }

                    outobj.outPrincipalName( rs );  //05/08/26Modify

                    // 記載責任者出力 04/11/05 Modify
                    if( stb == null )stb = new StringBuffer();
                    else             stb.delete(0,stb.length());
                    if( rs.getString("STAFF2_JOBNAME") != null )stb.append(rs.getString("STAFF2_JOBNAME"));
                    stb.append("  ");
                    if( rs.getString("STAFF2_NAME") != null )stb.append(rs.getString("STAFF2_NAME"));
                    ret = svf.VrsOut("STAFFNAME_2"      ,stb.toString());                               //記載責任者

                    String anname = null;
                    if( rs.getString("T4SCHOOLDIV").equals("0") )anname = "学年";   //05/11/18 Modify
                    else                                         anname = "年次";
                    for( int ia=1 ; ia<=annual ; ia++ ){
                        for( int ib=1 ; ib<=2 ; ib++ )
                            ret = svf.VrsOut( "GRADE" + ib +"_" + ia,  String.valueOf(ia) + anname );   //学年見出し
                        ret = svf.VrsOutn( "GRADE3",  ia,  String.valueOf(ia) + anname );               //学年見出し
                    }
                }
                if( rs != null )rs.close();
                db2.commit();
            } catch( Exception e ){
                log.error("[KNJE080_1]head_out error! ", e );
            }
        }
        
        /*
         *  個人情報印刷 共通項目および出力仕様分岐
         */
        void address_out_Detail(
                final String schregno, 
                final String year, 
                final String semes, 
                final String number,
                final ResultSet rs
        ) {
            try {
                ret = svf.VrsOut( "SCHREGNO",   schregno );
                if( rs.getString("COURSENAME") != null )ret = svf.VrsOut( "katei",      rs.getString("COURSENAME") );
                if( rs.getString("MAJORNAME")  != null )ret = svf.VrsOut( "gakka",      rs.getString("MAJORNAME") );
                if (rs.getString("ANNUAL")     != null) {
                    int ia = rs.getInt("ANNUAL");
                    if (_isJuniorHiSchool && (3 < ia)) ia = ia - 3;
                    ret = svf.VrsOut( "GRADE",      String.valueOf(ia) );
                }
                if( rs.getString("NAME")       != null )ret = svf.VrsOut( "NAME",       rs.getString("NAME") );
                if (null != rs.getString("ENT_DATE")) {
                    if (_seirekiFlg) {
                        svf.VrsOut("YEAR1", rs.getString("ENT_DATE").substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(rs.getString("ENT_DATE")));
                    } else {
                        svf.VrsOut("YEAR1", KNJ_EditDate.h_format_JP_M( rs.getString("ENT_DATE") ) );
                    }
                }
                if( rs.getString("ENTER_NAME") != null ) {
                    ret = svf.VrsOut( "TRANSFER1", rs.getString("ENTER_NAME"));
                }
                if (null != rs.getString("GRADU_DATE")) {
                    if(_seirekiFlg) {
                        svf.VrsOut("YEAR2", rs.getString("GRADU_DATE").substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(rs.getString("GRADU_DATE")));
                    } else {
                        svf.VrsOut("YEAR2", KNJ_EditDate.h_format_JP_M( rs.getString("GRADU_DATE") ) );
                    }
                }
                if( rs.getString("GRADU_NAME") != null )ret = svf.VrsOut( "TRANSFER2",  rs.getString("GRADU_NAME") );
                if (_seirekiFlg && null != rs.getString("BIRTHDAY")) {
                    svf.VrsOut("BIRTHDAY", rs.getString("BIRTHDAY").substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(rs.getString("BIRTHDAY")));
                } else {
                    if( rs.getString("BIRTHDAY")   != null )ret = svf.VrsOut( "BIRTHDAY",   KNJ_EditDate.h_format_JP_Bth( rs.getString("BIRTHDAY") ) );  //05/07/12Build
                }
            } catch( Exception e ){
                log.error("error! " + e );
            }
        }
    
        /*
         *  校長名の出力
         *  2005/08/26 Build
         */
        void outPrincipalName(final ResultSet rs) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            try {
                if( rs.getString("PRINCIPAL_NAME") != null )
                    ret = svf.VrsOut( "STAFFNAME_1", rs.getString("PRINCIPAL_NAME") );  //校長名
            } catch( Exception ex ) {
                log.error("error! " + ex);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    //  東京都専用出力仕様のクラス
        private class outDetailTokyoClass extends outDetailCommonClass {
        private String syoshonum;
        private String syoshoname;
        private boolean _isOutputCertifNo;
    
        /*
         *  [東京都用様式] 学校情報
         *  2005/10/23 Build  メソッドhead_outの一部分をここへ移動
         *  2005/12/06 Modify 記載責任者の出力を追加（再）
         */
        void svfPrintSchoolInfo(
                final String year,
                final String date,
                final String staffcd,
                final int annual,
                Map paramap
        ) {
            String year2 = null;
            if (date != null) {
                year2 = servletpack.KNJG.KNJG010_1.b_year(date);//過卒生対応年度取得->掲載日より年度を算出
            } else {
                year2 = (String)paramap.get("CTRL_YEAR");
            }
            ResultSet rs = null;
            try {
//                String year2 = servletpack.KNJG.KNJG010_1.b_year(date);//過卒生対応年度取得->掲載日より年度を算出

                int p = 0;
                ps7.setString( ++p, year2  );  //対象年度
                ps7.setString( ++p, "006" );   //証明書種別
                ps7.setString( ++p, "006" );   //証明書種別
                ps7.setString( ++p, year );    //現年度
                ps7.setString( ++p, staffcd );    //記載責任者 05/12/06 Build
                rs = ps7.executeQuery();

                if ( rs.next() ){
                    ret = svf.VrsOut( "NENDO",  nao_package.KenjaProperties.gengou( Integer.parseInt( year ) ) + "年度" );    //年度
                    if ( rs.getString("SYOSYO_NAME") != null )this.syoshoname = rs.getString("SYOSYO_NAME");
                    else                                      this.syoshoname = "";
                    ret = svf.VrsOut("SYOSYO_NAME",  rs.getString("SYOSYO_NAME") );  //証書名
                    ret = svf.VrsOut("SYOSYO_NAME2", rs.getString("SYOSYO_NAME2") ); //証書名２
                    if (rs.getString("CERTIF_NO") != null && rs.getString("CERTIF_NO").equals("0")) {
                        this._isOutputCertifNo = true;  //証書番号の印刷 0:あり,1:なし
                    }
//                    ret = svf.VrsOut("DATE", (date != null)? KNJ_EditDate.h_format_JP(date): "　　年　 月　 日" );   //記載日 05/11/18
                    if (date != null) { // 記載日
                        if (_isChristianEra) {
                            ret = svf.VrsOut( "DATE",   getChristianEra(date, "yyyy年M月d日") );
                        } else {
                            ret = svf.VrsOut( "DATE",   KNJ_EditDate.h_format_JP(date) );
                        }
                    } else {
                            ret = svf.VrsOut( "DATE",   "　　年　 月　 日" );
                    }
                    ret = svf.VrsOut("SCHOOLNAME",  rs.getString("SCHOOLNAME1") );             //学校名
                    if( rs.getString("PRINCIPAL_JOBNAME") != null )ret = svf.VrsOut( "JOBNAME",   rs.getString("PRINCIPAL_JOBNAME") );   //校長職名
                    
                    // 画面のパラメータ"プログラムID=KNJE080"かつ"校長名を出力しない"とき校長名を出力しない
                    boolean notOutputPrincipal = "KNJE080".equals(paramap.get("PRGID")) && "2".equals(paramap.get("OUTPUT_PRINCIPAL")); 
                    if( rs.getString("PRINCIPAL_NAME")    != null && !notOutputPrincipal)ret = svf.VrsOut( "STAFFNAME", rs.getString("PRINCIPAL_NAME")    );   //校長名

                    if (_definecode.schooldiv.equals("0")) {
                        String anname = null;
                        if( rs.getString("SCHOOLDIV").equals("0") ) anname = "学年";
                        else                                        anname = "年次";
                        for( int ia=1 ; ia<=annual ; ia++ ){
                            for( int ib=1 ; ib<=2 ; ib++ )
                                ret = svf.VrsOut( "GRADE" + ib +"_" + ia,  String.valueOf(ia) + anname );   //学年見出し
                            ret = svf.VrsOutn( "GRADE3",  ia,  String.valueOf(ia) + anname );               //学年見出し
                        }
                    }

                    String staffname2 = "";
                    if( rs.getString("REMARK2") != null ) {
                        staffname2 = rs.getString("REMARK2");
                        if( rs.getString("STAFF2_NAME") != null ) {
                            staffname2 = staffname2 + rs.getString("STAFF2_NAME");
                        }
                    }
                    svf.VrsOut( "STAFFNAME2", staffname2);  //記載責任者

                    //学校住所 05/12/13 Build
                    if( rs.getString("REMARK1") != null ) {
                        svf.VrsOut("SCHOOLADDRESS", rs.getString("REMARK1")); //住所
                    }
                    //if( rs.getString("SCHOOLZIPCD")!=null )
                    //  ret = svf.VrsOut("SCHOOLZIP"        ,"〒"+rs.getString("SCHOOLZIPCD"));          //郵便番号

                }
            } catch( Exception e ){
                log.error("head_out error! " , e );
            } finally{
                try { if( rs != null )rs.close(); db2.commit(); } catch( Exception e ){ log.error("error! " + e ); }
            }
        }

        /*
         *  [東京都用様式] 個人情報印刷 共通項目および出力仕様分岐
         */
        void address_out_Detail(
                final String schregno, 
                final String year, 
                final String semes, 
                final String number,
                final ResultSet rs
        ) {
            try {
                if (_isOutputCertifNo) ret = svf.VrsOut("CERTIF_NO",  number );  //証書番号
                ret = svf.VrsOut( "NENDO_NAME",  number + syoshoname );  //証明書番号
                if( schregno != null )ret = svf.VrsOut( "schregno",   schregno );
                if( rs.getString("NAME") != null )ret = svf.VrsOut( "NAME",  rs.getString("NAME") );  //生徒氏名
                address_out_Option( schregno, year, rs );  //近大付属用様式のメソッドを使用
            } catch( Exception e ){
                log.error("error! " + e );
            }
        }

        /*
         *  [東京都用様式] 個人情報印刷
         */
        void address_out_Option(
                final String schregno, 
                final String year, 
                final ResultSet rs
        ) {
            try {
//                if( rs.getString("BIRTHDAY")   != null ) ret = svf.VrsOut( "BIRTHDAY",   KNJ_EditDate.h_format_JP_Bth( rs.getString("BIRTHDAY") ) );    //生年月日
                if (rs.getString("BIRTHDAY") != null) { // 生年月日
                    if (_isChristianEra) {
                        ret = svf.VrsOut( "BIRTHDAY",   getChristianEra(rs.getString("BIRTHDAY"), "yyyy年M月d日生") );
                    } else {
                        ret = svf.VrsOut( "BIRTHDAY",   KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY")) );
                    }
                }
                if( rs.getString("COURSENAME") != null ) ret = svf.VrsOut( "KATEI",      rs.getString("COURSENAME") );
                if( rs.getString("MAJORNAME")  != null ) {
                    // 1行（中央）に6文字と　6文字を超えたら上下に2行で印字する。
                    String formFieldName = (6 < rs.getString("MAJORNAME").length()) ? "GAKKA2" : "GAKKA";
                    ret = svf.VrsOut( formFieldName,      rs.getString("MAJORNAME") );
                }
                if (rs.getString("ANNUAL")     != null) {
                    int ia = rs.getInt("ANNUAL");
                    if (_isJuniorHiSchool && (3 < ia)) ia = ia - 3;
                    ret = svf.VrsOut( "GRADE",      String.valueOf(ia) + ( (_definecode.schooldiv.equals("0")) ? "年生" : "年次" ) );
                }
//                if( rs.getString("ENT_DATE")   != null ) ret = svf.VrsOut( "YEAR1",      KNJ_EditDate.h_format_JP_M( rs.getString("ENT_DATE")) );
                if (rs.getString("ENT_DATE") != null) { // 入学年月
                    if (_isYuushinkan) {
                        ret = svf.VrsOut( "YEAR1",   KNJ_EditDate.h_format_JP(rs.getString("ENT_DATE")));
                    } else if (_isChristianEra) {
                        ret = svf.VrsOut( "YEAR1",   getChristianEra(rs.getString("ENT_DATE"), "yyyy年M月") );
                    } else {
                        ret = svf.VrsOut( "YEAR1",   KNJ_EditDate.h_format_JP_M(rs.getString("ENT_DATE")) );
                    }
                }

                if( rs.getString("ENTER_NAME") != null ){
                    if (_isYuushinkan) {
                        svf.VrsOut( "TRANSFER1", "入学日");
                    } else if ("4".equals(rs.getString("ENT_DIV")) || "5".equals(rs.getString("ENT_DIV"))) {
                        svf.VrsOut("TRANSFER1",  rs.getString("ENTER_NAME"));
                    } else if (_isJisyuukan) {
                        if (-1 < rs.getString("ENTER_NAME").indexOf("入学")) {
                            svf.VrsOut("TRANSFER1",  "後期課程開始");
                        } else if (-1 == rs.getString("ENTER_NAME").indexOf("入学")) {
                            svf.VrsOut("TRANSFER1",  rs.getString("ENTER_NAME"));
                        }
                    } else {
                        svf.VrsOut("TRANSFER1",  "入学");
                    }
                }
        
                if( rs.getString("GRADU_NAME") != null  &&  !rs.getString("GRADU_NAME").equals("卒業見込み") ){
                    ret = svf.VrsOut( "TRANSFER2",  rs.getString("GRADU_NAME") );
//                    if( rs.getString("GRADU_DATE") != null )ret = svf.VrsOut( "YEAR2", KNJ_EditDate.h_format_JP_M( rs.getString("GRADU_DATE")) );
                    if (rs.getString("GRADU_DATE") != null) { // 卒業年月
                        if (_isYuushinkan) {
                            ret = svf.VrsOut( "YEAR2",   KNJ_EditDate.h_format_JP(rs.getString("GRADU_DATE")));
                        } else if (_isChristianEra) {
                            ret = svf.VrsOut( "YEAR2",   getChristianEra(rs.getString("GRADU_DATE"), "yyyy年M月") );
                        } else {
                            ret = svf.VrsOut( "YEAR2",   KNJ_EditDate.h_format_JP_M(rs.getString("GRADU_DATE")) );
                        }
                    }
                    ret = svf.VrsOut( "GRADE",  "" ); //卒業生は学年を表示しない
                }

                if( rs.getString("NAME_KANA") != null ) ret = svf.VrsOut( "KANA",  rs.getString("NAME_KANA") );
            } catch( Exception e ){
                log.error("error! " + e );
            }
        }

        /*
         * SVF-FORM フィールド属性変更(RECORD) => 文字数により文字ピッチ及びＹ軸を変更する
         */
        void svfFieldAttribute(
                final String subclassname,
                final int ln
        ) {
            if (svfobj == null) { svfobj = new KNJSvfFieldModify(); }
            svfobj.width = 388;     //フィールドの幅(ドット)
            svfobj.height = 80;     //フィールドの高さ(ドット)
            svfobj.ystart = 1102;   //開始位置(ドット)
            svfobj.minnum = 14;     //最小設定文字数
            svfobj.maxnum = 40;     //最大設定文字数
            svfobj.setRetvalue( subclassname, ( ( ln < 23 )? ln: ln - 23 ) );
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            ret = svf.VrAttribute("SUBCLASSNAME" , "Y="+ svfobj.jiku );
            ret = svf.VrAttribute("SUBCLASSNAME" , "Size=" + svfobj.size );
        }
    }
}

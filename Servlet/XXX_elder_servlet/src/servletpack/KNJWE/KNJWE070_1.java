// kanji=漢字
/*
 * $Id: cccef2f2cc65f141153c671cb3062929786b2b12 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJWE;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJE.detail.KNJ_AttendrecSql;
import servletpack.KNJE.detail.KNJ_ExamremarkSql;
import servletpack.KNJE.detail.KNJ_GeneviewmbrSql;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJSvfFieldModify;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.Vrw32alpWrap;
import servletpack.KNJZ.detail.WithusUtils;

/*
 * 学校教育システム 賢者 [進路情報管理] 進学用高校調査書
 * 2004/03/16 yamashiro・学校住所２がnullで出力される不具合を修正
 * 2004/03/19 yamashiro・結核の個所に聴力が出力される不具合を修正
 * 2004/04/09 yamashiro・評定の学年表示に’第’を入れる
 * 2004/04/27 yamashiro・学習成績概評の学年人数の出力条件を追加 -> 指示画面より受取
 * 2004/06/28 yamashiro・所見等の文字列編集の仕様変更->改行は改行マークがない場合は文字数で行う
 * 2004/08/17 yamashiro・進学用に関して保健データを表示しない
 *                     ・出欠記録備考、特別活動記録、総合学習活動＆評価、備考の１行あたりの出力文字数変更
 *                     ・住所１と住所２は別段に出力。長さに応じて文字の大きさを代える。
 *                     ・科目名は長さに応じて文字の大きさを代える。
 *                     ・転入学以外の入学種別は”入学”と表記する。
 * 2004/08/19 yamashiro・調査書所見データのフィールド名変更に伴い修正。
 * 2004/08/24 yamashiro・所見等の出力におけて、改行マークと文字数で改行する。<--04/06/28の変更を元に戻す
 *                       KNJZ/KNJ_EditEditにおいて処理
 * 2004/08/30 yamashiro・転入生は、入学欄の学年を括弧で囲んで表示する。
 * 2004/09/13 yamashiro・所見等の出力文字数をＯＳ区分により変更する-->(XP,WINDOWS2000で文字数を変える）
 * ・学習の記録欄において科目名を文字数により大きさを変えて出力する
 * 2004/09/22 yamashiro・所見等の出力の不具合を修正-->KNJZ/detail/KNJ_EditEditを修正
 * ・入学(転入学)の学年は入学日ENTER_DATEより算出した年度の年次とする-->KNJZ/detail/KNJ_PersonalinfoSqlを修正
 * 2005/07/10 yamashiro・成績段階別人数に近大付属高校用の処理を追加
 *                     ・出欠の記録に近大付属高校用の処理を追加
 * ・SVF-FORMへのデータ出力におけるNULLの処理を修正 => KNJEditStringのretStringNullToBlankを使用
 * 2005/07/14 yamashiro・記載責任者の職名と担当者名を分けて出力（職名の固定に対応）
 * 2005/07/19 yamashiro・HEXAM_ENTREMARK_HDATが存在しない場合を考慮して修正
 * 2005/11/18 yamashiro 「処理日付をブランクで出力する」仕様の追加による修正
 *                       => 年度の算出は、処理日付がブランクの場合は印刷指示画面から受け取る「今年度」、
 *                       処理日付がある場合は処理日付から割り出した年度
 *                       学校情報を'今年度'と'卒業年度'の２種類を取得( =>Edit_SchoolInfoSqlにおいて ) 
 * 2005/12/08 yamashiro・卒業日が学籍処理日の後なら卒業見込とする
 * 2006/03/22 yamashiro・就職用に学校電話番号を追加のため、メソッドhead_out_Sub1を作成 --NO001
 * 2006/04/14 yamashiro・KNJ_GeneviewmbrSqlクラスのインスタンス作成を元に戻す --NO002
 */

public class KNJWE070_1 {

    private static final Log log = LogFactory.getLog(KNJWE070_1.class);

    public Vrw32alpWrap svf = new Vrw32alpWrap(); // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    public DB2UDB db2; // Databaseクラスを継承したクラス

    public boolean nonedata;

    public PreparedStatement ps1, ps2, ps3, ps4, ps5, ps6, ps7, ps8;

    static int aline = 20; // 各教科評定平均値出力行数
    public static String SCHOOLCD_WI = "35080006001";
    public static String SCHOOLCD_NV = "00350800060";
    private String _schoolCd = "";

    String anname; // 学年・年次名称

    private KNJEditString knjobj = new KNJEditString(); // 05/07/10Build

    public final KNJDefineSchool _definecode; // 各学校における定数等設定

    TreeMap _gradeMap = new TreeMap(); // 学年（年度）出力列
    TreeMap _titleMap = new TreeMap(); // 学年（年度）項目名
    TreeMap _titleYear = new TreeMap(); // 学年（年度）項目名

    String shoshoname; // 証書番号
    String remark5; // 備考５
    String remark7; // 備考５
    String remark8; // 備考５

    private KNJSvfFieldModify svfobj; // フォームのフィールド属性変更
    protected boolean _hasCertifSchool; // "CERTIF_SCHOOL_DAT"の存在チェック
    boolean _isJuniorHiSchool; // 中高一貫の場合はTrue
    boolean _isJisyuukan; // 自修館はTrue
    boolean _isHosei; // 法政はTrue
    boolean _isKokubunji; // 国分寺はTrue
    boolean _isTokyoto; // 東京都はTrue
    boolean _isOutputCertifNo;
    private boolean _seirekiFlg = false;
    private static Map _w029Map;

    public KNJWE070_1() {
        _definecode = new KNJDefineSchool(); // 各学校における定数等設定
    }

    public KNJWE070_1(final DB2UDB db2, final Vrw32alpWrap svf, final KNJDefineSchool definecode) throws SQLException {
        this.db2 = db2;
        this.svf = svf;
        nonedata = false;
        _definecode = definecode;
        _hasCertifSchool = definecode.hasTableHasField(db2, "CERTIF_SCHOOL_DAT", null);
        setJuniorHiSchool();
        _w029Map = loadW029Map(db2);
    }

    /**
     * PrepareStatement作成
     */
    public void pre_stat(final String hyotei) {
        try {
            setSeirekiFlg();
            log.debug("=== 在学生用 ===");

            // 所見データ
            KNJ_ExamremarkSql obj_ExamremarkSql = new KNJ_ExamremarkSql();
            ps4 = db2.prepareStatement(obj_ExamremarkSql.pre_sql_ent("1"));

            // 成績概評人数データ
            // NO002により、元に戻す
            KNJ_GeneviewmbrSql obj_GeneviewmbrSql = new KNJ_GeneviewmbrSql();
            ps5 = db2.prepareStatement(obj_GeneviewmbrSql.pre_sql());

            // 個人データ
            KNJ_PersonalinfoSql obj_Personalinfo = new KNJ_PersonalinfoSql();
            ps6 = db2.prepareStatement(obj_Personalinfo.studentInfoSql(true));
            log.debug(obj_Personalinfo.studentInfoSql(true));
//            KNJ_PersonalinfoSql obj_Personalinfo = new KNJ_PersonalinfoSql();
//            ps6 = db2.prepareStatement(obj_Personalinfo.sql_info_reg("1111001000"));

            // 学校データ
            ps7 = getPreStatementSchoolInfo();
        } catch (Exception e) {
            log.error("[KNJWE070_1]pre_stat error! ", e);
        }
        if (log.isErrorEnabled()) {
            log.debug("USE KNJWE070_1");
        }
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

    private boolean isW029(final String subclassCd) {
        for (final Iterator iter = _w029Map.keySet().iterator(); iter.hasNext();) {
            final String year = (String) iter.next();
            final Map setNamecd2Map = (Map) _w029Map.get(year);
            if (setNamecd2Map.containsKey(subclassCd)) {
                return true;
            }
        }
        return false;
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
     * 学校データ等 の java.sql.PreparedStatement オブジェクトを戻します。
     * 
     * @return
     * @throws Exception
     */
    public PreparedStatement getPreStatementSchoolInfo() throws Exception {
        final String preSql;

        if (!_hasCertifSchool || isKindaifuzoku()) {
            servletpack.KNJZ.detail.KNJ_SchoolinfoSql obj_SchoolinfoSql = null;
            obj_SchoolinfoSql = new servletpack.KNJZ.detail.KNJ_SchoolinfoSql("12100");
            preSql = obj_SchoolinfoSql.pre_sql();
        } else {
            servletpack.KNJG.detail.KNJ_SchoolinfoSql obj_SchoolinfoSql = null;
            obj_SchoolinfoSql = new servletpack.KNJG.detail.KNJ_SchoolinfoSql("12100");
            obj_SchoolinfoSql.setHasCertifSchoolName(true);
            preSql = obj_SchoolinfoSql.pre_sql();
        }

        return db2.prepareStatement(preSql);
    }

    /**
     * 学習の記録データ の java.sql.PreparedStatement オブジェクトをメンバ変数 ps1 にセットします。<br>
     * [年度/学年]列名の印字 および 有効[年度/学年]をメンバ変数 _gradeMap にセットするメソッドを呼んでいます。
     * 
     * @param hyotei：印刷指示画面で指定する「評定１を２と表記する」フラグ
     * @return
     * @throws Exception
     */
    protected void getPreStatementStudyrec(final String schregno, final String year, final String hyotei, final Map paramap) throws Exception {
        String hyotei2 = hyotei;
        if (null == hyotei2) {
            hyotei2 = "off";
        }
        SqlStudyrec object = new SqlStudyrec();
        object.setHyoutei(hyotei2);
        object.setTokuA(true);
        object.setStype(1);
        object.setDaiken_div_code();
        object.setZensekiSubclassCd();
        object.setDefinecode(_definecode);
        ps1 = db2.prepareStatement(object.pre_sql(paramap, "SELECT"));
        setGradeTitleMain(schregno, year, paramap, object);
    }

    /**
     * 有効な[年度/学年]をメンバ変数 Map _gradeMap に追加するメソッドを呼んでいます。<br>
     * 学習の記録欄・出欠の記録欄・所見等の欄における[年度/学年]列名を印字するメソッドを呼んでいます。
     * 
     * @param schregno
     * @param year
     * @param object
     */
    protected void setGradeTitleMain(final String schregno, final String year, final Map paramap, SqlStudyrec object) {
        PreparedStatement ps = null;
        try {
            ps = db2.prepareStatement(object.pre_sql(paramap, "TITLE"));

            setGradeTitleGakunensei(schregno, year, paramap, ps, "STUDY_REC");
        } catch (final SQLException e) {
            log.error("SQLException ： 学習", e);
        }

        try {
            // 所見データ
            KNJ_ExamremarkSql obj_ExamremarkSql = new KNJ_ExamremarkSql();
            ps = db2.prepareStatement(obj_ExamremarkSql.preSqlEntTitle());

            setGradeTitleGakunensei(schregno, year, paramap, ps, "HEXAM");
        } catch (final SQLException e) {
            log.error("SQLException ： 所見", e);
        }

        try {
            // 出欠データ
            KNJ_AttendrecSql obj_AttendrecSql = new KNJ_AttendrecSql();
            ps = db2.prepareStatement(obj_AttendrecSql.preSqlTitle(db2, year));

            setGradeTitleGakunensei(schregno, year, paramap, ps, "ATTEND");
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
            final Map paramap,
            final PreparedStatement ps,
            final String dataDiv
        ) {
        int p = 1;
        ResultSet rs = null;
        try {
            if (dataDiv.equals("STUDY_REC")) {
                ps.setString(p++, schregno);
                ps.setString(p++, schregno);
                if (((String) paramap.get("RISYU")).equals("1")) {
                    ps.setString(p++, year);
                    ps.setString(p++, schregno);
                }
            }

            if (dataDiv.equals("HEXAM")) {
                ps.setString(p++, schregno);
                ps.setString(p++, schregno);
                ps.setString(p++, year);
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

    /**
     * 学年制の場合、 学習の記録欄・出欠の記録欄・所見等の欄における[年度/学年]列名を印字します。
     * 
     * @param _titlemap
     */
    protected void printGradeTitleGakunensei() {
        int ret = 0;
        if (false && 0 != ret) {
            ret = 0;
        }
        Set mapi = _titleMap.keySet();
        for (Iterator t = mapi.iterator(); t.hasNext();) {
            Integer value = (Integer) t.next();
            String str = (String) _titleMap.get(value);
            int i = value.intValue();
            if ("入学前".equals(str)) {
                printGradeTitleGakunenseiNyuugakumae(i, str);
                continue;
            }
            ret = svf.VrsOut("GRADE1_" + i + "_1", "第"); // 学習の記録 左
            ret = svf.VrsOut("GRADE1_" + i + "_2", str.substring(0, str.length() - 2));
            ret = svf.VrsOut("GRADE1_" + i + "_3", str.substring(str.length() - 2));
            ret = svf.VrsOut("GRADE2_" + i + "_1", "第"); // 学習の記録 右
            ret = svf.VrsOut("GRADE2_" + i + "_2", str.substring(0, str.length() - 2));
            ret = svf.VrsOut("GRADE2_" + i + "_3", str.substring(str.length() - 2));

            ret = svf.VrsOut("GRADE3_" + i + "_2", str.substring(0, str.length() - 2)); // 出欠の記録
                                                                                        // 左
            ret = svf.VrsOut("GRADE6_" + i + "_2", str.substring(0, str.length() - 2)); // 出欠の記録
                                                                                        // 左

            ret = svf.VrsOut("GRADE5_" + i + "_2", "第" + str); // 指導上参考となる諸事項
            ret = svf.VrsOut("GRADE4_" + i, "第" + str); // 特別活動の記録
        }
    }

    /**
     * 入学前 学習の記録欄・出欠の記録欄・所見等の欄における[年度/学年]列名を印字します。
     * 
     * @param i
     * @param str
     */
    protected void printGradeTitleGakunenseiNyuugakumae(final int i, final String str) {
        svf.VrsOut("GRADE1_" + i + "_1", "入"); // 学習の記録 左
        svf.VrsOut("GRADE1_" + i + "_2", "学");
        svf.VrsOut("GRADE1_" + i + "_3", "前");
        svf.VrsOut("GRADE2_" + i + "_1", "入"); // 学習の記録 右
        svf.VrsOut("GRADE2_" + i + "_2", "学");
        svf.VrsOut("GRADE2_" + i + "_3", "前");

        svf.VrsOut("GRADE3_" + i + "_1", "入"); // 出欠の記録 左
        svf.VrsOut("GRADE3_" + i + "_2", "学");
        svf.VrsOut("GRADE3_" + i + "_3", "前");
        svf.VrsOut("GRADE6_" + i + "_1", "入"); // 出欠の記録 右
        svf.VrsOut("GRADE6_" + i + "_2", "学");
        svf.VrsOut("GRADE6_" + i + "_3", "前");

        svf.VrsOut("GRADE5_" + i + "_2", "入学前"); // 指導上参考となる諸事項
        svf.VrsOut("GRADE4_" + i, "入学前"); // 特別活動の記録
    }

    /**
     * 単位制の場合、 学習の記録欄・出欠の記録欄・所見等の欄における[年度/学年]列名を印字します。
     * 
     * @param _titlemap
     */
    protected void printGradeTitleTanisei() {
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
        }
    }

    /**
     * PrepareStatement close
     */
    public void pre_stat_f() {
        try {
            if (ps1 != null)
                ps1.close();
            if (ps2 != null)
                ps2.close();
            if (ps3 != null)
                ps3.close();
            if (ps4 != null)
                ps4.close();
            if (ps5 != null)
                ps5.close();
            if (ps6 != null)
                ps6.close();
            if (ps7 != null)
                ps7.close();
            if (ps8 != null)
                ps8.close();
        } catch (Exception e) {
            log.error("[KNJWE070_1]pre_stat_f error! ", e);
        }
    }

    /**
     * 学校情報 2005/11/18 Modify 処理日付の「入力なし」に対応
     */
    public void head_out(final String year, final String date, final String staffcd, final Map paramap) {
        try {
            // _definecode.schooldiv = "0";
            int pp = 0;
            ps7.setString(++pp, (String) paramap.get("CTRL_YEAR")); // 05/11/18
                                                                    // Modify
            if (_hasCertifSchool && !isKindaifuzoku()) {
                ps7.setString(++pp, (String) paramap.get("CERTIFKIND")); // 証明書種別
                ps7.setString(++pp, (String) paramap.get("CERTIFKIND")); // 証明書種別
            }
            ps7.setString(++pp, (date != null) ? servletpack.KNJG.KNJG010_1.b_year(date) : (String) paramap.get("CTRL_YEAR")); // 05/11/18
            ps7.setString(++pp, staffcd);
            ps7.setString(++pp, year); // 05/11/18 過年度の学校名取得
            if (_hasCertifSchool && !isKindaifuzoku()) {
                ps7.setString(++pp, (String) paramap.get("CERTIFKIND")); // 証明書種別
            }
            ResultSet rs = ps7.executeQuery();
            if (rs.next()) {
                svf.VrsOut("school_name_2", knjobj.retStringNullToBlank(rs.getString("SCHOOLNAME1")));

                StringBuffer stb = new StringBuffer(); // 04/03/16住所編集を修正
                if (rs.getString("SCHOOLADDR1") != null)
                    stb.append(rs.getString("SCHOOLADDR1"));
                if (rs.getString("SCHOOLADDR2") != null)
                    stb.append(rs.getString("SCHOOLADDR2"));
                if (stb.toString() != null) {
                    svf.VrsOut("school_address", stb.toString()); // 住所
                }
                svf.VrsOut("TEL_NO", rs.getString("SCHOOLTELNO")); // 電話番号

                if (rs.getString("SCHOOLZIPCD") != null) {
                    svf.VrsOut("SCHOOLZIP", "〒" + rs.getString("SCHOOLZIPCD")); // 郵便番号
                }

                svf.VrsOut("NAMESPARE", knjobj.retStringNullToBlank(rs.getString("T4CLASSIFICATION"))); // 種別 05/11/18

                svf.VrsOut("DATE", (date != null) ? KNJ_EditDate.h_format_JP(date) : "　　年　 月　 日"); // 記載日 05/11/18

                svf.VrsOut("STAFFNAME_1", knjobj.retStringNullToBlank(rs.getString("PRINCIPAL_NAME"))); // 校長名

                if (rs.getString("SCHOOLDIV").equals("0")) {
                    anname = "学年";
                } else {
                    anname = "年次";
                }

                if (_hasCertifSchool) {
                    svf.VrsOut("SCHOOL_NAME1", knjobj.retStringNullToBlank(rs.getString("REMARK4"))); // 05/11/18
                    shoshoname = rs.getString("SYOSYO_NAME"); // 証書番号
                    remark5 = rs.getString("REMARK5"); // 備考５
                    remark7 = rs.getString("REMARK7"); // 備考５
                    remark8 = rs.getString("REMARK8"); // 備考５
                    svf.VrsOut("SYOSYO_NAME", rs.getString("SYOSYO_NAME")); // 証書名
                    svf.VrsOut("SYOSYO_NAME2", rs.getString("SYOSYO_NAME2")); // 証書名２
                    if (rs.getString("CERTIF_NO") != null && rs.getString("CERTIF_NO").equals("0")) {
                        _isOutputCertifNo = true; // 証書番号の印刷 0:あり,1:なし
                    }
                }
                head_out_Sub1(rs);
            }
            rs.close();
        } catch (Exception e) {
            log.error("[KNJWE070_1]head_out error! ", e);
        }
    }

    /**
     * 学校情報 その２（進学用と就職用で異なる仕様）
     */
    public void head_out_Sub1(final ResultSet rs) {
        if (_hasCertifSchool && !isKindaifuzoku()) {
            svf.VrsOut("JOBNAME", "教諭");  // 記載責任者職名
            try {
                if (rs.getString("STAFF2_NAME") != null) {
                    svf.VrsOut("STAFFNAME_2", rs.getString("STAFF2_NAME") + "　印");  // 記載責任者名
                } else {
                    svf.VrsOut("STAFFNAME_2", "　　　　　　　　　　　印");  // 記載責任者名
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        } else {
              try {
                  svf.VrsOut("STAFFNAME_2", knjobj.retStringNullToBlank(rs.getString("STAFF2_NAME")));  // 記載責任者名
                  if (null != rs.getString("STAFF2_NAME")) {
                      final String jobName = rs.getString("STAFF2_JOBNAME");
                      final String setJobName = null != jobName ? jobName : "教諭 ";
                      if (!isKindaifuzoku()) {
                          svf.VrsOut("JOBNAME", setJobName); // 記載責任者職名
                      } else {
                          svf.VrsOut("JOBNAME", "教諭"); // 記載責任者職名
                      }
                  }
              } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }

        head_out_Sub1_Yobi(rs); // 予備１から３の出力
    }

    /**
     * @param rs
     */
    protected void head_out_Sub1_Yobi(final ResultSet rs) {
        if (!_hasCertifSchool || isKindaifuzoku()) {
            return;
        }
        try {
            // 予備１から予備２を出力
            for (int i = 1; i < 3; i++) {
                if (rs.getString("REMARK" + i) != null) {
                    svf.VrsOut("REMARK" + i, rs.getString("REMARK" + i));
                }
            }
        } catch (SQLException e) {
            log.error("head_out_Sub1 error! ", e);
        }
    }

    /**
     * 個人情報
     */
    public void address_out(final String schregno, final String year, final String semes, final String kanji, final String number, final Map paramap) {
        try {
            ps6.setString(1, schregno);
            ps6.setInt(2, Integer.parseInt(year));
            ps6.setString(3, schregno);
            ps6.setString(4, year);
            ps6.setString(5, semes);
            ResultSet rs = ps6.executeQuery();

            printSvfSyoushoNum(number, shoshoname); // 証書番号を出力

            if (rs.next()) {
                svf.VrsOut("KANA", knjobj.retStringNullToBlank(rs.getString("NAME_KANA")));
                if (kanji.equals("1")) {
                    final String name = knjobj.retStringNullToBlank(rs.getString("NAME"));
                    final int nameField = name.length() <= 10 ? 1 : name.length() <= 15 ? 2 : 3;
                    svf.VrsOut("NAME" + nameField, name);
                }
                svf.VrsOut("BIRTHDAY", changePrintDate(rs.getString("BIRTHDAY")) + "生");
                svf.VrsOut("SEX", knjobj.retStringNullToBlank(rs.getString("SEX")));

                int add1len = 0;
                int add2len = 0;

                final String addr = rs.getString("PREF_NAME") + rs.getString("ADDR1") + rs.getString("ADDR2");
                if (addr != null) {
                    byte addbyte[] = addr.getBytes();
                    add1len = addbyte.length;
                }
                if (add1len > 34) {
                    svf.VrsOut("GUARD_ADDRESS1_2", knjobj.retStringNullToBlank(addr));
                } else if (add1len > 0) {
                    svf.VrsOut("GUARD_ADDRESS1", knjobj.retStringNullToBlank(addr));
                }

                if (rs.getString("ADDR3") != null) {
                    byte addbyte[] = rs.getString("ADDR3").getBytes();
                    add2len = addbyte.length;
                }
                if (add1len > 34 || add2len > 34) {
                    svf.VrsOut("GUARD_ADDRESS2_2", knjobj.retStringNullToBlank(rs.getString("ADDR3")));
                } else if (add2len > 0) {
                    svf.VrsOut("GUARD_ADDRESS2", knjobj.retStringNullToBlank(rs.getString("ADDR3")));
                }

                svf.VrsOut("katei", knjobj.retStringNullToBlank(rs.getString("COURSENAME")));
                svf.VrsOut("gakka", knjobj.retStringNullToBlank(rs.getString("MAJORNAME")));
                svf.VrsOut("ENTERDATE", changePrintDate(rs.getString("ENT_DATE")));

                // 転入学以外の入学種別(1, 2, 3)は”入学”と表記する
                final String entdiv = rs.getString("ENT_DIV");
                if (entdiv != null) {
                    final String entDate = KNJ_EditDate.h_format_JP_M(rs.getString("ENT_DATE"));
                    final String entYear = KNJ_EditDate.b_year(rs.getString("ENT_DATE"));
                    final String entNendo = nao_package.KenjaProperties.gengou(Integer.parseInt(entYear)) + "年度";
                    final String entName = entdiv.equals("1") || entdiv.equals("2") || entdiv.equals("3") ? "入学" : rs.getString("ENT_NAME");
                    svf.VrsOut("ENTERDATE", entDate);
                    svf.VrsOut("GRADE", "(" + entNendo + ")");
                    svf.VrsOut("ENTER1", entName);
                }

                // 卒業項目
                String grdDate = rs.getString("GRD_DATE");
                String grdName = rs.getString("GRD_NAME");
                if (null == grdDate) {
                    grdDate = rs.getString("GRD_SCHEDULE_DATE");
                    grdName = "卒業見込";
                }
                svf.VrsOut("TRANSFER_DATE", knjobj.retStringNullToBlank(KNJ_EditDate.h_format_JP_M(grdDate)));
                svf.VrsOut("TRANSFER1", knjobj.retStringNullToBlank(grdName));
            }
            rs.close();
        } catch (Exception e) {
            log.error("[KNJWE070_1]address_out error!", e);
        }
    }

    /**
     * SVF-FORM 出欠データ出力 2005/07/10 Modify
     */
    public void attend_out(final String schregno, final String year) {
        try {
            // 05/07/10 Build pre_statメソッドから処理を移動
            if (ps2 == null) {
                KNJ_AttendrecSql obj_AttendrecSql = new KNJ_AttendrecSql();
                ps2 = db2.prepareStatement(obj_AttendrecSql.pre_sql(db2, year));
            }
        } catch (Exception e) {
            log.error("[KNJWE070_1]geneviewmbr_out GeneviewmbrSql error! ", e);
        }
        try {
            ps2.setString(1, schregno);
            ps2.setString(2, year);
            ResultSet rs = ps2.executeQuery();

            while (rs.next()) {
                Integer position = null;
                int i = 0;
                if (log.isDebugEnabled()) {
                    log.debug("学制区分=" + _definecode.schooldiv + " / 年次=" + rs.getString("ANNUAL") + " / 年度=" + rs.getString("YEAR"));
                }
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
                svf.VrsOutn("LESSON", i, getAttendShowData(rs.getString("ATTEND_1"))); // 授業日数
                svf.VrsOutn("SPECIAL", i, getAttendShowData(rs.getString("SUSP_MOUR"))); // 出停・忌引
                svf.VrsOutn("ABROAD", i, getAttendShowData(rs.getString("ABROAD"))); // 留学
                svf.VrsOutn("PRESENT", i, getAttendShowData(rs.getString("REQUIREPRESENT"))); // 要出席
                svf.VrsOutn("ABSENCE", i, getAttendShowData(rs.getString("ATTEND_6"))); // 欠席
                svf.VrsOutn("ATTEND", i, getAttendShowData(rs.getString("PRESENT"))); // 出席
            }
            rs.close();
        } catch (Exception e) {
            log.error("[KNJWE070_1]attend_out error!", e);
        }
    }

    private String getAttendShowData(final String str) throws SQLException {
        final String changeStr = knjobj.retStringNullToBlank(str);
        if (changeStr.equalsIgnoreCase("")) {
            return changeStr;
        } else {
            return "(" + changeStr + ")";
        }
    }

    /**
     * 所見データ 2004/09/13 Modify 引数にString oadivを追加 2005/07/19 Modify
     * 「HEXAM_ENTREMARK_HDATは必須ではない」と判明したことに対応 => 処理を別のメソッドへ分離
     */
    public void exam_out(final String schregno, final String year, final Map paramap) {
        // ＤＢ検索（所見データ出力）
        try {
            final String oadiv = (String) paramap.get("OS");

            for (final Iterator iter = _titleYear.keySet().iterator(); iter.hasNext();) {
                final String titleYear = (String) iter.next();
                examOutNoAttend("NOTE", 12, 4, ((Integer) _titleYear.get(titleYear)).intValue(), titleYear, schregno, paramap);
            }

            int pp = 0;
            ps4.setString(++pp, schregno);
            ps4.setString(++pp, schregno);
            ps4.setString(++pp, year);
            ResultSet rs = ps4.executeQuery();

            boolean first = true; // 初回フラグ
            while (rs.next()) {
                if (first) {
                    exam_out_only(rs, Integer.parseInt(oadiv));
                    first = false;
                }
                Integer position = null;
                int i = 0;
                if (log.isDebugEnabled()) {
                    log.debug("学制区分=" + _definecode.schooldiv + " / 年次=" + rs.getString("ANNUAL") + " / 年度=" + rs.getString("YEAR"));
                }
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
                if (i > 0) {
                    exam_out_annual(i, rs, Integer.parseInt(oadiv), schregno, paramap);
                }
            }
            rs.close();
        } catch (Exception e) {
            log.error("[KNJWE070_1]exam_out error!", e);
        }
    }

    /**
     * 所見データ 生徒単位(生徒固有）のデータ出力 引数の説明 int intoadiv : OS区分 2005/07/19 Build
     */
    private void exam_out_only(final ResultSet rs, final int intoadiv) {
        String a_str[] = null; // フィールドの文字列を配列へ
        try {
            // 総合的な学習の時間の内容・評価 04/08/17Modify-->文字数を38->41へ
            // 04/08/19Modify-->FIELD名変更
            // 04/09/13Modify-->再び文字数変更 & SVF-FIELD変更
            a_str = KNJ_EditEdit.get_token(rs.getString("TOTALSTUDYACT"), 90, 2);
            if (a_str != null)
                for (int i = 0; i < a_str.length; i++)
                    svf.VrsOut("ACTION" + (i + 1) + ((intoadiv == 1) ? "" : "_2K"), a_str[i]);
            a_str = null;
            a_str = KNJ_EditEdit.get_token(rs.getString("TOTALSTUDYVAL"), 90, 3);
            if (a_str != null)
                for (int i = 0; i < a_str.length; i++)
                    svf.VrsOut("ASSESS" + (i + 1) + ((intoadiv == 1) ? "" : "_2K"), a_str[i]);
            a_str = null;

            // 備考 04/08/17Modify-->文字数を38->41へ
            // 04/09/13Modify-->再び文字数変更 & SVF-FIELD変更
            a_str = KNJ_EditEdit.get_token(rs.getString("REMARK"), 90, 5);
            if (a_str != null)
                for (int i = 0; i < a_str.length; i++)
                    svf.VrsOut("field9_" + (i + 1) + ((intoadiv == 1) ? "" : "_2K"), a_str[i]);
            a_str = null;

        } catch (Exception e) {
            log.error("[KNJWE070_1]exam_out_only error!", e);
        }
    }

    /**
     * 所見データ 生徒年次単位(年次ごとのデータ有り）のデータ出力 引数の説明 int g : 学年 / int intoadiv : OS区分
     * 2005/07/19 Build
     */
    private void exam_out_annual(final int g, final ResultSet rs, final int intoadiv, final String schregno, final Map paramap) {
        String a_str[] = null; // フィールドの文字列を配列へ
        try {
            // 出欠備考 04/08/17Modify-->文字数を4->5へ
            a_str = null != rs ? KNJ_EditEdit.get_token(rs.getString("ATTENDREC_REMARK"), 12, 4) : null;
            if (a_str != null) {
                printNoteClear("NOTE", g, 4);
                printNote("NOTE", g, a_str);
            }
            a_str = null;

            // 特別活動の記録 04/08/17Modify-->文字数を13->11へ
            // 04/09/13Modify-->再び文字数変更 & SVF-FIELD変更
            a_str = KNJ_EditEdit.get_token(rs.getString("SPECIALACTREC"), 24, 8);
            if (a_str != null)
                for (int i = 0; i < a_str.length; i++)
                    svf.VrsOutn("field7_" + (i + 1), g, a_str[i]);
            a_str = null;

            //  指導上参考となる諸事項1
            a_str = KNJ_EditEdit.get_token(rs.getString("TRAIN_REF1"), 82, 7);
            final String strField1 = "1_";
            if (a_str != null)
                for (int i = 0; i < a_str.length; i++) {
                    svf.VrsOutn("field8_" + strField1 + (i + 1) + ((intoadiv == 1) ? "" : "_2K"), g, a_str[i]);
                }
            a_str = null;
            //  指導上参考となる諸事項2
            a_str = KNJ_EditEdit.get_token(rs.getString("TRAIN_REF2"), 82, 7);
            final String strField2 = "2_";
            if (a_str != null)
                for (int i = 0; i < a_str.length; i++) {
                    svf.VrsOutn("field8_" + strField2 + (i + 1) + ((intoadiv == 1) ? "" : "_2K"), g, a_str[i]);
                }
            a_str = null;
            //  指導上参考となる諸事項3
            a_str = KNJ_EditEdit.get_token(rs.getString("TRAIN_REF3"), 82, 7);
            final String strField3 = "3_";
            if (a_str != null)
                for (int i = 0; i < a_str.length; i++) {
                    svf.VrsOutn("field8_" + strField3 + (i + 1) + ((intoadiv == 1) ? "" : "_2K"), g, a_str[i]);
                }
            a_str = null;

        } catch (Exception e) {
            log.error("[KNJWE070_1]exam_out_annual error!", e);
        }
    }

    public void examOutNoAttend(
            final String fieldName,
            final int fieldSize,
            final int fieldCnt,
            final int g,
            final String year,
            final String schregno,
            final Map paramap
    ) throws SQLException {
        String a_str[] = null; // フィールドの文字列を配列へ
        final String sqlRegd = "SELECT COUNT(*) AS CNT FROM SCHREG_REGD_DAT WHERE SCHREGNO = '" + schregno + "' AND YEAR = '" + year + "'";
        db2.query(sqlRegd);
        ResultSet rsRegd = db2.getResultSet();
        ResultSet rsCertif = null;
        try {
            int regdCnt = 0;
            while (rsRegd.next()) {
                regdCnt = rsRegd.getInt("CNT");
            }
            String remark7 = "";
            String remark8 = "";
            String certifKind = (String) paramap.get("CERTIFKIND");
            final String sqlCertif = "SELECT REMARK7, REMARK8 FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '" + certifKind + "'";
            db2.query(sqlCertif);
            rsCertif = db2.getResultSet();
            while (rsCertif.next()) {
                remark7 = rsCertif.getString("REMARK7");
                remark8 = rsCertif.getString("REMARK8");
            }

            if (regdCnt > 0) {
                a_str = KNJ_EditEdit.get_token(remark7, fieldSize, fieldCnt);
            } else {
                a_str = KNJ_EditEdit.get_token(remark8, fieldSize, fieldCnt);
            }
            if (a_str != null) {
                printNote(fieldName, g, a_str);
            }
        } finally {
            DbUtils.closeQuietly(rsRegd);
            DbUtils.closeQuietly(rsCertif);
            db2.commit();
        }
    }

    public void printNoteClear(final String fieldName, final int g, final int cnt) {
        for (int i = 1; i <= cnt; i++) {
            svf.VrsOutn(fieldName + (i + 1), g, "");
        }
    }

    public void printNote(final String fieldName, final int g, final String[] a_str) {
        for (int i = 0; i < a_str.length; i++) {
            svf.VrsOutn(fieldName + (i + 1), g, a_str[i]);
        }
    }

    /**
     * SVF-FORM 成績段階別人数をフォームへ出力 2005/07/10 Modify
     * グループの段階別人数SQL取得のメソッドを追加したことによる修正
     */
    public void geneviewmbr_out(final String schregno, final String year, final String semes, final String comment) {
        try {
            int pp = 0;
            ps5.setString(++pp, schregno);
            ps5.setString(++pp, year);
            ps5.setString(++pp, semes);
            ResultSet rs = ps5.executeQuery();

            if (rs.next()) {
                svf.VrsOutn("level", 1, knjobj.retStringNullToBlank(rs.getString("MEMBER5")));
                svf.VrsOutn("level", 2, knjobj.retStringNullToBlank(rs.getString("MEMBER4")));
                svf.VrsOutn("level", 3, knjobj.retStringNullToBlank(rs.getString("MEMBER3")));
                svf.VrsOutn("level", 4, knjobj.retStringNullToBlank(rs.getString("MEMBER2")));
                svf.VrsOutn("level", 5, knjobj.retStringNullToBlank(rs.getString("MEMBER1")));
                svf.VrsOut("level_2", knjobj.retStringNullToBlank(rs.getString("MEMBER0")));
                if (comment == null)
                    svf.VrsOut("level_3", "");
                else
                    svf.VrsOut("level_3", " ( " + rs.getInt("MEMBER6") + " 人)"); // 学年
                                                                                    // 04/04/27変更
            }
            rs.close();
        } catch (Exception e) {
            log.error("[KNJWE070_1]geneviewmbr_out error!", e);
        }
    }

    /**
     * SVF-FORM 学習の記録出力 04/09/13 Modify 引数にmaxgradeを追加
     */
    public void study_out(final String schregno, final String year, final Map paramap) throws SQLException {

        ResultSet rs = null;
        try {
            int pp = 1;
            ps1.setString(pp++, schregno);
            ps1.setString(pp++, schregno);
            if (((String) paramap.get("RISYU")).equals("1")) {
                ps1.setString(pp++, year);
                ps1.setString(pp++, schregno);
            }
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
            String befClassName = "";
            String befSubclassCd = "";
            String befSubclassName = "";
            LineData lineData = null;
            while (rs.next()) {

                final String valution = getValuation(rs.getString("SCHOOLCD"), String.valueOf(rs.getInt("VALUATION")));
                final int getCredit = null == rs.getString("GET_CREDIT") ? 0 : rs.getInt("GET_CREDIT");
                printTotalData.setTotalCredit(rs.getString("CLASSCD"), getCredit);
                if (rs.getString("CLASSCD").equals(sougouClassCd)) {
                    continue;
                }
                if (!hasData || !befClassCd.equals(rs.getString("CLASSCD")) || !befSubclassCd.equals(rs.getString("SUBCLASSCD")) || !befSubclassName.equals(rs.getString("SUBCLASSNAME"))) {
                    if (hasData && (!befClassCd.equals(rs.getString("CLASSCD")) || !befSubclassCd.equals(rs.getString("SUBCLASSCD")) || !befSubclassName.equals(rs.getString("SUBCLASSNAME")))) {
                        printList.add(lineData);
                    }
                    if (hasData && !befClassCd.equals(rs.getString("CLASSCD"))) {
                        if (!isW029(rs.getString("SUBCLASSCD"))) {
                            setValuationAvg("subject", befClassName, "average_", classValution, classCnt, String.valueOf(classField));
                            classValution = 0;
                            classCnt = 0;
                            classField++;
                        }
                    }
                    lineData = new LineData(rs.getString("CLASSCD"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSCD"), rs.getString("SUBCLASSNAME"), 0);
                }
                if (rs.getInt("SCHOOLCD") < 2 && null != rs.getString("VALUATION")) {
                    if (!isW029(rs.getString("SUBCLASSCD"))) {
                        final int valuation = null == rs.getString("VALUATION") ? 0 : rs.getInt("VALUATION");
                        classValution += valuation;
                        classCnt++;
                        totalClassValution += valuation;
                        totalClassCnt++;
                    }
                }
                lineData.setPrintData(getCredit, rs.getString("YEAR"), rs.getString("ANNUAL"), valution);
                if (!isW029(rs.getString("SUBCLASSCD"))) {
                    befClassCd = rs.getString("CLASSCD");
                    befClassName = rs.getString("CLASSNAME");
                    befSubclassCd = rs.getString("SUBCLASSCD");
                    befSubclassName = rs.getString("SUBCLASSNAME");
                }
                nonedata = true;
                hasData = true;
            }
            if (hasData) {
                printList.add(lineData);
                if (!isW029(befSubclassCd)) {
                    setValuationAvg("subject", befClassName, "average_", classValution, classCnt, String.valueOf(classField));
                    setValuationAvg("", "", "average", totalClassValution, totalClassCnt, "");
                }
                svf.VrEndRecord(); // 最後のレコード出力
            } else {
                // 学習情報がない場合の処理
                svf.VrsOut("CLASSCD", "A"); // 教科コード
                svf.VrEndRecord();
                nonedata = true;
            }

            //明細印字
            final int fieldCnt = meisaiPrintOut(printList, printTotalData);

            final int fieldMax = fieldCnt > 1 ? 52 : 51;
            for (int i = fieldCnt; i < fieldMax; i++) {
                if (i == fieldCnt) {
                    svf.VrsOut("CLASSCD", ""); // 教科コード
                } else {
                    svf.VrsOut("CLASSCD", String.valueOf(i)); // 教科コード
                }
                svf.VrEndRecord();
                nonedata = true;
            }

            printKei("ITEM", "総合的な学習の時間", "TOTAL_CREDIT", String.valueOf(printTotalData._sougouCredit));

            printKei("ITEM", "計", "TOTAL_CREDIT", String.valueOf(printTotalData._totalCredit));

        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    /**
     * 評定データ
     */
    public String getValuation(final String schoolCd, final String valuation) {
        if (schoolCd.equals("0")) {
            return null != valuation ? valuation : "";
        } else if (schoolCd.equals("1")) {
            return null != valuation ? "(" + valuation + ")" : "( )";
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

    public void setValuationAvg(
            final String field1,
            final String className,
            final String field2,
            final int classValution,
            final int classCnt,
            final String classField
    ) throws SQLException {
        svf.VrsOut(field1 + classField, className);

        if (classValution == 0) {
            svf.VrsOut(field2 + classField, "");
        } else {
            final BigDecimal classValBig = new BigDecimal(classValution);
            final BigDecimal classCntBig = new BigDecimal(classCnt);
            final BigDecimal avg = classValBig.divide(classCntBig, 1, BigDecimal.ROUND_HALF_UP);
            svf.VrsOut(field2 + classField, avg.toString());
            if (field2.equals("average")) {
                final String sql = "SELECT * FROM ASSESS_MST WHERE ASSESSCD = '4' AND " + avg.toString() + " BETWEEN ASSESSLOW AND ASSESSHIGH";
                db2.query(sql);
                ResultSet rs = null;
                try {
                    String assessMark = "";
                    rs = db2.getResultSet();
                    while (rs.next()) {
                        assessMark = rs.getString("ASSESSMARK");
                    }
                    svf.VrsOut("R2", assessMark);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(rs);
                }
            }
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
            PrintData printData = new PrintData(year, annual, value);
            _printData.add(printData);
        }
    }

    private class PrintData {
        final String _year;
        final String _annual;
        final String _value;

        public PrintData(final String year, final String annual, final String value) {
            _year = year;
            _annual = annual;
            _value = value;
        }
    }

    public class PrintTotalData {
        final String sougouClassCd = "11";
        int _sougouCredit = 0;
        int _totalCredit = 0;

        public PrintTotalData() {
        }

        public void setTotalCredit(final String classCd, final int credit) {
            if (sougouClassCd.equals(classCd)) {
                _sougouCredit += credit;
            }
            _totalCredit += credit;
        }
    }

    public int meisaiPrintOut(final List printList, final PrintTotalData printTotalData) {

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
                if (!isW029(printData._year, lineData._subclassCd) || "履中".equals(printData._value)) {
                    svf.VrsOut("GRADES" + fieldNo, printData._value);
                } else if (isW029(printData._year, lineData._subclassCd)) {
                    svf.VrsOut("GRADES" + fieldNo, "-");
                }
            }
            svf.VrsOut("CREDIT", String.valueOf(lineData._totalCredit));
            befClassCd = lineData._classCd;
            fieldCnt++;
            svf.VrEndRecord();
        }

        nonedata = true;
        return fieldCnt;
    }

    public void printKei(final String fieldName1, final String fieldVal1, final String fieldName2, final String fieldVal2) {
        svf.VrsOut(fieldName1, fieldVal1);
        svf.VrsOut(fieldName2, fieldVal2);
        svf.VrEndRecord();
    }

    /**
     * SVF-FORMフィールド初期化
     */
    public void svf_int(final String schregno, final String year, final Map paramap) {
        _definecode.setSchoolCode(db2, year);
        _titleMap.clear();
        _gradeMap.clear();
        _titleYear.clear();
        try {
            getPreStatementStudyrec(schregno, year, (String) paramap.get("HYOTEI"), paramap);
        } catch (Exception e1) {
            log.error("Exception", e1);
        }

        setSchoolCd(db2, year);
        setSvfForm(paramap);
        svf.VrsOut("SCHREGNO", schregno);
        if ("0".equals(_definecode.schooldiv)) {
            printGradeTitleGakunensei();
        } else {
            printGradeTitleTanisei();
        }
    }

    private void setSchoolCd(final DB2UDB db2, final String year) {
        final String sql = "SELECT SCHOOLCD FROM SCHOOL_MST WHERE YEAR = '" + year + "'";
        ResultSet rs = null;
        try {
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                _schoolCd = rs.getString("SCHOOLCD");
            }
            rs.close();
        } catch (final SQLException e) {
            log.error("SCHOOL_MST取得エラー", e);
        } finally {
            db2.commit();
        }
    }

    /*
     * 日付の大小チェック 2005/12/08 Build 引数について int div == 1 String d1 < String d2 なら
     * true int div == 2 String d1 > String d2 なら true int div == 0 String d1 ==
     * String d2 なら true
     */
    private boolean getDateLargeSmall(final String d1, final String d2, final int div) {
        log.debug("d1=" + d1 + "  d2=" + d2 + "  div=" + div);

        if (d1 == null)
            return false;
        if (d2 == null)
            return false;
        if (d1.length() < 10)
            return false;
        if (d2.length() < 10)
            return false;

        boolean ret = false;
        Calendar cals = Calendar.getInstance();
        Calendar cale = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            cals.setTime(sdf.parse(KNJ_EditDate.H_Format_Haifun(d1)));
            cale.setTime(sdf.parse(d2));

            if (div == 1 && cals.before(cale))
                ret = true;
            if (div == 2 && cals.after(cale))
                ret = true;
            if (div == 0 && !cals.before(cale) && !cals.after(cale))
                ret = true;

        } catch (Exception ex) {
            log.warn("periodname-get error!", ex);
        }
        log.debug("d1=" + d1 + "  d2=" + d2 + "   ret=" + ret);
        return ret;
    }

    /**
     * SVF-FORM-OUT 健康状況データ
     */
    public void medexam_out(final String schregno, final String year) {
        return;
    }

    /*
     * 証書番号の出力 @param param @throws SQLException
     */
    private void printSvfSyoushoNum(String number, String syousyoname) throws SQLException {
        if (null == number) {
            number = "";
        }
        if (null == syousyoname) {
            syousyoname = "";
        }
        if (_isKokubunji) {
            svf.VrsOut("NENDO_NAME", remark5); // 備考５
        } else {
            svf.VrsOut("NENDO_NAME", number + syousyoname + "証第       号"); // 証明書番号
        }
        if (_isOutputCertifNo)
            svf.VrsOut("CERTIF_NO", number); // 証書番号
    }

    /**
     * 近大付属とその他で処理が異なる場合、近大付属を判別するために用います。
     * 
     * @return KNJDefineSchool.schoolmarkが"KIN"または"KINJUNIOR"はtrueを戻します。
     */
    protected boolean isKindaifuzoku() {
        if ("KIN".equals(_definecode.schoolmark)) {
            return true;
        }
        if ("KINJUNIOR".equals(_definecode.schoolmark)) {
            return true;
        }
        return false;
    }

    /**
     * @param isJuniorHiSchool 設定する isJuniorHiSchool。
     */
    void setJuniorHiSchool() {
        try {
            db2.query("SELECT NAME1,NAMESPARE2 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2 = '00'");
            ResultSet rs = db2.getResultSet();
            if (rs.next()) {
                String str = rs.getString("NAMESPARE2");
                if ("1".equals(str)) {
                    _isJuniorHiSchool = true;
                }

                str = rs.getString("NAME1");
                if ("jisyukan".equals(str)) {
                    _isJisyuukan = true;
                } else if ("HOUSEI".equals(str)) {
                    _isHosei = true;
                } else if ("kokubunji".equals(str)) {
                    _isKokubunji = true;
                } else if ("tokyoto".equals(str)) {
                    _isTokyoto = true;
                }
            }
            rs.close();
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
        db2.commit();
    }

    public String changePrintDate(final String date) {
        if (null != date) {
            if (_seirekiFlg) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);
            }
        } else {
            return "";
        }
    }

    public String changePrintYear(final String year) {
        if (_seirekiFlg) {
            return year + "年度";
        } else {
            return nao_package.KenjaProperties.gengou(Integer.parseInt(year)) + "年度";
        }
    }

    /**
     * 中高一貫の場合、4年生用を使用します。(フォームを再設定)<br>
     * 但し、印刷指示画面において"6年用フォーム"がチェックされていない場合です。
     */
    protected void setSvfForm(final Map paramap) {
        if (paramap.containsKey("FORM6")) {
            svf.VrSetForm(getSvfForm6(), 4);
            return;
        }

        if (_isJuniorHiSchool) {
            svf.VrSetForm(getSvfForm4(), 4);
            return;
        }

        if (4 < _gradeMap.size()) {
            svf.VrSetForm(getSvfForm6(), 4);
        } else {
            svf.VrSetForm(getSvfForm4(), 4);
        }
    }

    protected String getSvfForm4() {
        return "KNJWE070_1B.frm";
    }

    protected String getSvfForm6() {
        return "KNJWE070_2B.frm";
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<クラスの説明>>。
     * 
     * @author yamasiro
     * @version $Id: cccef2f2cc65f141153c671cb3062929786b2b12 $
     */
    public class SqlStudyrec {

        private static final String WISOUGOU = "11";

        private String _hyoutei; // 評定の読替え １を２と評定

        private boolean _isTokuA; // 特Ａ付き

        private int _stype; // 総合的な学習の時間、留学単位、修得単位の集計区分

        private String _TableName_StudyRec; // SCHREG_STUDYREC_DAT

        private String _TableName_Transfer; // SCHREG_TRANSFER_DAT

        private String _TableName_Schreg_Regd; // SCHREG_REGD_DAT

        private KNJDefineSchool _definecode; // 各学校における定数等設定

        private int _daiken_div_code; // 大検の集計方法 0:合計 1:明細

        private String _zensekiSubclassCd; // 前籍校の成績専用科目コード

        /**
         * 前籍校の成績専用科目コードを設定します。<br>
         * 名称マスター'E011'のコード'01'のレコード予備１をセットします。。
         * 
         * @param zensekiSubclassCd 設定する zensekiSubclassCd。
         */
        public void setZensekiSubclassCd() {
            String str = "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E011' AND NAMECD2 = '01'";
            try {
                db2.query(str);
                ResultSet rs = db2.getResultSet();
                if (rs.next() && null != rs.getString("NAMESPARE1")) {
                    _zensekiSubclassCd = rs.getString("NAMESPARE1");
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        }

        /**
         * 高等学校卒業程度認定単位（大検）の印刷方法を設定します。<br>
         * 名称マスター'E011'のコード'02'のレコードが'Y'の場合は0を以外は1を設定します。
         * 
         * @param daiken_div_code 設定する daiken_div_code。
         */
        public void setDaiken_div_code() {
            int daiken = 1;
            String str = "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E011' AND NAMECD2 = '02'";
            try {
                db2.query(str);
                ResultSet rs = db2.getResultSet();
                if (rs.next() && null != rs.getString("NAMESPARE1") && "Y".equals(rs.getString("NAMESPARE1"))) {
                    daiken = 0;
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
            _daiken_div_code = daiken;
        }

        /**
         * @param daiken_div_code 設定する daiken_div_code。
         */
        public void setDaiken_div_code(int daiken_div_code) {
            _daiken_div_code = daiken_div_code;
        }

        /**
         * @param _isTokuA 設定する _isTokuA。
         */
        public void setTokuA(boolean atype) {
            this._isTokuA = atype;
        }

        /**
         * @param _definecode 設定する _definecode。
         */
        public void setDefinecode(KNJDefineSchool definecode) {
            this._definecode = definecode;
        }

        /**
         * @param _hyoutei 設定する _hyoutei。
         */
        public void setHyoutei(String hyoutei) {
            this._hyoutei = hyoutei;
        }

        /**
         * @param _stype 設定する _stype。
         */
        public void setStype(int stype) {
            this._stype = stype;
        }

        /**
         * @param _TableName_StudyRec 設定する _TableName_StudyRec。
         */
        public void setTableName_StudyRec(String tname1) {
            this._TableName_StudyRec = tname1;
        }

        /**
         * @param _TableName_Transfer 設定する _TableName_Transfer。
         */
        public void setTableName_Transfer(String tname2) {
            this._TableName_Transfer = tname2;
        }

        /**
         * @param _TableName_Schreg_Regd 設定する _TableName_Schreg_Regd。
         */
        public void setTableName_Schreg_Regd(String tname3) {
            this._TableName_Schreg_Regd = tname3;
        }

        /**
         * 学習記録データ(全て)の SQL SELECT 文を戻します。
         * 
         * @return
         */
        public String pre_sql(final Map paramap, final String selectDiv) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH STUDY_REC AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     CASE WHEN T1.CLASSNAME IS NOT NULL ");
            stb.append("          THEN T1.CLASSNAME ");
            stb.append("          ELSE CASE WHEN L1.CLASSORDERNAME1 IS NOT NULL ");
            stb.append("               THEN L1.CLASSORDERNAME1 ");
            stb.append("               ELSE L1.CLASSNAME ");
            stb.append("         END ");
            stb.append("     END AS CLASSNAME, ");
            stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L1.SHOWORDER2 ");
            stb.append("          ELSE 999 ");
            stb.append("     END AS CLASSORDER2, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     L2.SUBCLASSCD2, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     CASE WHEN T1.SUBCLASSNAME IS NOT NULL ");
            stb.append("          THEN T1.SUBCLASSNAME ");
            stb.append("          ELSE CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
            stb.append("               THEN L2.SUBCLASSORDERNAME1 ");
            stb.append("               ELSE L2.SUBCLASSNAME ");
            stb.append("         END ");
            stb.append("     END AS SUBCLASSNAME, ");
            stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L2.SHOWORDER2 ");
            stb.append("          ELSE 999999 ");
            stb.append("     END AS SUBCLASSORDER2, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SCHOOLCD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.GET_CREDIT AS GET_CREDIT, ");
            stb.append("     T1.VALUATION AS VALUATION ");
            stb.append(" FROM ");
            stb.append("     SCHREG_STUDYREC_DAT T1 ");
            stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
            stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
            stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
            stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? ");

            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     CASE WHEN L1.CLASSORDERNAME1 IS NOT NULL ");
            stb.append("          THEN L1.CLASSORDERNAME1 ");
            stb.append("          ELSE L1.CLASSNAME ");
            stb.append("     END AS CLASSNAME, ");
            stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L1.SHOWORDER2 ");
            stb.append("          ELSE 999 ");
            stb.append("     END AS CLASSORDER2, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     L2.SUBCLASSCD2, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     CASE WHEN T1.SUBCLASSNAME IS NOT NULL ");
            stb.append("          THEN T1.SUBCLASSNAME ");
            stb.append("          ELSE CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
            stb.append("               THEN L2.SUBCLASSORDERNAME1 ");
            stb.append("               ELSE L2.SUBCLASSNAME ");
            stb.append("         END ");
            stb.append("     END AS SUBCLASSNAME, ");
            stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L2.SHOWORDER2 ");
            stb.append("          ELSE 999 ");
            stb.append("     END AS SUBCLASSORDER2, ");
            stb.append("     L3.SCHREGNO, ");
            stb.append("     CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) AS SCHOOLCD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.GET_CREDIT, ");
            stb.append("     T1.VALUATION ");
            stb.append(" FROM ");
            stb.append("     ANOTHER_SCHOOL_GETCREDITS_DAT T1 ");
            stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
            stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
            stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
            stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST L3 ON T1.APPLICANTNO = L3.APPLICANTNO ");
            stb.append(" WHERE ");
            stb.append("     L3.SCHREGNO = ? ");
            if (((String) paramap.get("MIRISYU")).equals("2")) {
                stb.append("     AND ( ");
                stb.append("          (CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) = '2' ");
                stb.append("           OR ");
                stb.append("           CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) = '3' ");
                stb.append("          ) ");
                stb.append("          OR ");
                stb.append("          ( ");
                stb.append("           VALUE(T1.GET_CREDIT, 0) > 0 ");
                stb.append("          ) ");
                stb.append("         ) ");
            }
            if (((String) paramap.get("RISYU")).equals("1")) {
                stb.append(" ), RISYU_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     L1.CLASSNAME, ");
                stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L1.SHOWORDER2 ");
                stb.append("          ELSE 999 ");
                stb.append("     END AS CLASSORDER2, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     L2.SUBCLASSCD2, ");
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     L2.SUBCLASSNAME, ");
                stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L2.SHOWORDER2 ");
                stb.append("          ELSE 999999 ");
                stb.append("     END AS SUBCLASSORDER2, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     '9' AS SCHOOLCD, ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.COMP_CREDIT AS GET_CREDIT, ");
                stb.append("     CAST(NULL AS SMALLINT) AS VALUATION ");
                stb.append(" FROM ");
                stb.append("     COMP_REGIST_DAT T1 ");
                stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
                stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
                stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
                stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = ? ");
                stb.append("     AND T1.SCHREGNO = ? ");
                stb.append("     AND T1.YEAR || T1.CLASSCD || T1.CURRICULUM_CD || CASE WHEN L2.SUBCLASSCD2 IS NOT NULL AND T1.CLASSCD = '" + WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + "' ");
                stb.append("                                                           THEN L2.SUBCLASSCD2 ");
                stb.append("                                                           ELSE T1.SUBCLASSCD "); 
                stb.append("                                                      END ");
                stb.append("         NOT IN (SELECT ");
                stb.append("                     T2.YEAR || T2.CLASSCD || T2.CURRICULUM_CD || T2.SUBCLASSCD ");
                stb.append("                 FROM ");
                stb.append("                     STUDY_REC T2 ");
                stb.append("                 WHERE ");
                stb.append("                     T2.SCHOOLCD = '0' ");
                stb.append("                ) ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.CLASSNAME, ");
                stb.append("     T1.CLASSORDER2, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD2, ");
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     T1.SUBCLASSNAME, ");
                stb.append("     T1.SUBCLASSORDER2, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.SCHOOLCD, ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.GET_CREDIT, ");
                stb.append("     T1.VALUATION ");
                stb.append(" FROM ");
                stb.append("     STUDY_REC T1 ");
            }
            stb.append(" ), ANNUAL_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     row_number() over (order by T1.YEAR) AS ANNUAL, ");
            stb.append("     T1.YEAR ");
            stb.append(" FROM ");
            if (((String) paramap.get("RISYU")).equals("1")) {
                stb.append("     RISYU_T T1 ");
            } else {
                stb.append("     STUDY_REC T1 ");
            }
            if (((String) paramap.get("MIRISYU")).equals("2")) {
                stb.append(" WHERE ");
                stb.append("    (T1.SCHOOLCD = '0' ");
                stb.append("    AND VALUE(T1.GET_CREDIT, 0) > 0) ");
                stb.append("    OR T1.SCHOOLCD <> '0' ");
            }
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR ");
            stb.append(" ) ");
            if (selectDiv.equals("TITLE")) {
                stb.append(" SELECT ");
                stb.append("     ANNUAL, ");
                stb.append("     YEAR ");
                stb.append(" FROM ");
                stb.append("     ANNUAL_T ");
                stb.append(" ORDER BY ");
                stb.append("     YEAR ");
            } else {
                stb.append(" , FUKUSU_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
                stb.append("          THEN L2.CLASSCD ");
                stb.append("          ELSE T1.CLASSCD ");
                stb.append("     END AS CLASSCD, ");
                stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
                stb.append("          THEN L2.CLASSNAME ");
                stb.append("          ELSE T1.CLASSNAME ");
                stb.append("     END AS CLASSNAME, ");
                stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L2.SHOWORDER2 ");
                stb.append("          ELSE T1.CLASSORDER2 ");
                stb.append("     END AS CLASSORDER2, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSCD ");
                stb.append("          ELSE T1.SUBCLASSCD ");
                stb.append("     END AS SUBCLASSCD, ");
                stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L1.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END AS SUBCLASSNAME, ");
                stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L1.SHOWORDER2 ");
                stb.append("          ELSE T1.SUBCLASSORDER2 ");
                stb.append("     END AS SUBCLASSORDER2, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     MAX(T1.SCHOOLCD) AS SCHOOLCD, ");
                stb.append("     L3.ANNUAL, ");
                stb.append("     T1.YEAR, ");
                stb.append("     SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
                stb.append("     ROUND(AVG(FLOAT(T1.VALUATION)) * 10, -1) / 10 AS VALUATION ");
                stb.append(" FROM ");
                if (((String) paramap.get("RISYU")).equals("1")) {
                    stb.append("     RISYU_T T1 ");
                } else {
                    stb.append("     STUDY_REC T1 ");
                }
                stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
                stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
                stb.append("          AND T1.SUBCLASSCD2 = L1.SUBCLASSCD ");
                stb.append("     LEFT JOIN CLASS_MST L2 ON L1.CLASSCD = L2.CLASSCD ");
                stb.append("     LEFT JOIN ANNUAL_T L3 ON T1.YEAR = L3.YEAR ");
                if (((String) paramap.get("MIRISYU")).equals("2")) {
                    stb.append(" WHERE ");
                    stb.append("    (T1.SCHOOLCD = '0' ");
                    stb.append("    AND VALUE(T1.GET_CREDIT, 0) > 0) ");
                    stb.append("    OR T1.SCHOOLCD <> '0' ");
                }
                stb.append(" GROUP BY ");
                stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
                stb.append("          THEN L2.CLASSCD ");
                stb.append("          ELSE T1.CLASSCD ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
                stb.append("          THEN L2.CLASSNAME ");
                stb.append("          ELSE T1.CLASSNAME ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L2.SHOWORDER2 ");
                stb.append("          ELSE T1.CLASSORDER2 ");
                stb.append("     END, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSCD ");
                stb.append("          ELSE T1.SUBCLASSCD ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L1.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L1.SHOWORDER2 ");
                stb.append("          ELSE T1.SUBCLASSORDER2 ");
                stb.append("     END, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     L3.ANNUAL, ");
                stb.append("     T1.YEAR ");
                stb.append(" HAVING ");
                stb.append("     COUNT(*) > 1 ");
                stb.append("     AND SUM(CASE WHEN VALUE(T1.VALUATION, 0) > 1 THEN 1 ELSE 0 END) > 0 ");
                stb.append(" ) ");

                stb.append(" , MAIN_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
                stb.append("          THEN L2.CLASSCD ");
                stb.append("          ELSE T1.CLASSCD ");
                stb.append("     END AS CLASSCD, ");
                stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
                stb.append("          THEN L2.CLASSNAME ");
                stb.append("          ELSE T1.CLASSNAME ");
                stb.append("     END AS CLASSNAME, ");
                stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L2.SHOWORDER2 ");
                stb.append("          ELSE T1.CLASSORDER2 ");
                stb.append("     END AS CLASSORDER2, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSCD ");
                stb.append("          ELSE T1.SUBCLASSCD ");
                stb.append("     END AS SUBCLASSCD, ");
                stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L1.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END AS SUBCLASSNAME, ");
                stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L1.SHOWORDER2 ");
                stb.append("          ELSE T1.SUBCLASSORDER2 ");
                stb.append("     END AS SUBCLASSORDER2, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     MAX(T1.SCHOOLCD) AS SCHOOLCD, ");
                stb.append("     L3.ANNUAL, ");
                stb.append("     T1.YEAR, ");
                stb.append("     SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
                stb.append("     ROUND(AVG(FLOAT(T1.VALUATION)) * 10, -1) / 10 AS VALUATION ");
                stb.append(" FROM ");
                if (((String) paramap.get("RISYU")).equals("1")) {
                    stb.append("     RISYU_T T1 ");
                } else {
                    stb.append("     STUDY_REC T1 ");
                }
                stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
                stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
                stb.append("          AND T1.SUBCLASSCD2 = L1.SUBCLASSCD ");
                stb.append("     LEFT JOIN CLASS_MST L2 ON L1.CLASSCD = L2.CLASSCD ");
                stb.append("     LEFT JOIN ANNUAL_T L3 ON T1.YEAR = L3.YEAR, ");
                stb.append("     FUKUSU_T ");
                stb.append(" WHERE ");
                stb.append("     FUKUSU_T.CLASSCD = CASE WHEN L2.CLASSCD IS NOT NULL ");
                stb.append("          THEN L2.CLASSCD ");
                stb.append("          ELSE T1.CLASSCD ");
                stb.append("     END ");
                stb.append("     AND FUKUSU_T.CLASSNAME = CASE WHEN L2.CLASSNAME IS NOT NULL ");
                stb.append("                                 THEN L2.CLASSNAME ");
                stb.append("                                 ELSE T1.CLASSNAME ");
                stb.append("                            END ");
                stb.append("     AND FUKUSU_T.CLASSORDER2 = CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("                                   THEN L2.SHOWORDER2 ");
                stb.append("                                   ELSE T1.CLASSORDER2 ");
                stb.append("                              END ");
                stb.append("     AND FUKUSU_T.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("     AND FUKUSU_T.SUBCLASSCD = CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
                stb.append("                                  THEN L1.SUBCLASSCD ");
                stb.append("                                  ELSE T1.SUBCLASSCD ");
                stb.append("                             END ");
                stb.append("     AND FUKUSU_T.SUBCLASSNAME = CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("                                  THEN L1.SUBCLASSORDERNAME1 ");
                stb.append("                                  ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
                stb.append("                                            THEN L1.SUBCLASSNAME ");
                stb.append("                                            ELSE T1.SUBCLASSNAME ");
                stb.append("                                       END ");
                stb.append("                             END ");
                stb.append("     AND FUKUSU_T.SUBCLASSORDER2 = CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("                                      THEN L1.SHOWORDER2 ");
                stb.append("                                      ELSE T1.SUBCLASSORDER2 ");
                stb.append("                                 END ");
                stb.append("     AND FUKUSU_T.SCHREGNO = T1.SCHREGNO ");
                stb.append("     AND FUKUSU_T.ANNUAL = L3.ANNUAL ");
                stb.append("     AND FUKUSU_T.YEAR = T1.YEAR ");
                stb.append("     AND VALUE(T1.VALUATION, 0) > 1 ");
                if (((String) paramap.get("MIRISYU")).equals("2")) {
                    stb.append("    AND ((T1.SCHOOLCD = '0' AND VALUE(T1.GET_CREDIT, 0) > 0) ");
                    stb.append("          OR ");
                    stb.append("         (T1.SCHOOLCD <> '0') ");
                    stb.append("        ) ");
                }
                stb.append(" GROUP BY ");
                stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
                stb.append("          THEN L2.CLASSCD ");
                stb.append("          ELSE T1.CLASSCD ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
                stb.append("          THEN L2.CLASSNAME ");
                stb.append("          ELSE T1.CLASSNAME ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L2.SHOWORDER2 ");
                stb.append("          ELSE T1.CLASSORDER2 ");
                stb.append("     END, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSCD ");
                stb.append("          ELSE T1.SUBCLASSCD ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L1.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L1.SHOWORDER2 ");
                stb.append("          ELSE T1.SUBCLASSORDER2 ");
                stb.append("     END, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     L3.ANNUAL, ");
                stb.append("     T1.YEAR ");

                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
                stb.append("          THEN L2.CLASSCD ");
                stb.append("          ELSE T1.CLASSCD ");
                stb.append("     END AS CLASSCD, ");
                stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
                stb.append("          THEN L2.CLASSNAME ");
                stb.append("          ELSE T1.CLASSNAME ");
                stb.append("     END AS CLASSNAME, ");
                stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L2.SHOWORDER2 ");
                stb.append("          ELSE T1.CLASSORDER2 ");
                stb.append("     END AS CLASSORDER2, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSCD ");
                stb.append("          ELSE T1.SUBCLASSCD ");
                stb.append("     END AS SUBCLASSCD, ");
                stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L1.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END AS SUBCLASSNAME, ");
                stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L1.SHOWORDER2 ");
                stb.append("          ELSE T1.SUBCLASSORDER2 ");
                stb.append("     END AS SUBCLASSORDER2, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     MAX(T1.SCHOOLCD) AS SCHOOLCD, ");
                stb.append("     L3.ANNUAL, ");
                stb.append("     T1.YEAR, ");
                stb.append("     SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
                stb.append("     ROUND(AVG(FLOAT(T1.VALUATION)) * 10, -1) / 10 AS VALUATION ");
                stb.append(" FROM ");
                if (((String) paramap.get("RISYU")).equals("1")) {
                    stb.append("     RISYU_T T1 ");
                } else {
                    stb.append("     STUDY_REC T1 ");
                }
                stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
                stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
                stb.append("          AND T1.SUBCLASSCD2 = L1.SUBCLASSCD ");
                stb.append("     LEFT JOIN CLASS_MST L2 ON L1.CLASSCD = L2.CLASSCD ");
                stb.append("     LEFT JOIN ANNUAL_T L3 ON T1.YEAR = L3.YEAR ");
                if (((String) paramap.get("MIRISYU")).equals("2")) {
                    stb.append(" WHERE ");
                    stb.append("    (T1.SCHOOLCD = '0' ");
                    stb.append("    AND VALUE(T1.GET_CREDIT, 0) > 0) ");
                    stb.append("    OR T1.SCHOOLCD <> '0' ");
                }
                stb.append(" GROUP BY ");
                stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
                stb.append("          THEN L2.CLASSCD ");
                stb.append("          ELSE T1.CLASSCD ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
                stb.append("          THEN L2.CLASSNAME ");
                stb.append("          ELSE T1.CLASSNAME ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L2.SHOWORDER2 ");
                stb.append("          ELSE T1.CLASSORDER2 ");
                stb.append("     END, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSCD ");
                stb.append("          ELSE T1.SUBCLASSCD ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L1.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END, ");
                stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
                stb.append("          THEN L1.SHOWORDER2 ");
                stb.append("          ELSE T1.SUBCLASSORDER2 ");
                stb.append("     END, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     L3.ANNUAL, ");
                stb.append("     T1.YEAR ");
                stb.append(" HAVING ");
                stb.append("     COUNT(*) = 1 ");
                stb.append("     OR SUM(CASE WHEN VALUE(T1.VALUATION, 0) > 1 THEN 1 ELSE 0 END) = 0 ");
                stb.append(" ) ");

                stb.append(" , SELECT_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.CLASSNAME, ");
                stb.append("     T1.CLASSORDER2, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     T1.SUBCLASSNAME, ");
                stb.append("     T1.SUBCLASSORDER2, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.SCHOOLCD, ");
                stb.append("     T1.ANNUAL, ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.GET_CREDIT, ");
                stb.append("     T1.VALUATION ");
                stb.append(" FROM ");
                stb.append("     MAIN_T T1 ");
                stb.append(" ) ");

                stb.append(" , CURRICULUM_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     T1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     SELECT_T T1 ");
                stb.append(" GROUP BY ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     T1.SCHREGNO ");
                stb.append(" ) ");

                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.CLASSNAME, ");
                stb.append("     T1.CLASSORDER2, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L2.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L2.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L2.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END AS SUBCLASSNAME, ");
                stb.append("     L2.SHOWORDER2, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.SCHOOLCD, ");
                stb.append("     T1.ANNUAL, ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.GET_CREDIT, ");
                stb.append("     T1.VALUATION ");
                stb.append(" FROM ");
                stb.append("     SELECT_T T1 ");
                stb.append("     LEFT JOIN CURRICULUM_T L1 ON T1.CLASSCD = L1.CLASSCD ");
                stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
                stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
                stb.append("          AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
                stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
                stb.append("          AND L1.CURRICULUM_CD = L2.CURRICULUM_CD ");
                stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
                stb.append(" ORDER BY ");
                stb.append("     T1.CLASSORDER2, ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     L2.SHOWORDER2, ");
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
                stb.append("          THEN L2.SUBCLASSORDERNAME1 ");
                stb.append("          ELSE CASE WHEN L2.SUBCLASSNAME IS NOT NULL ");
                stb.append("               THEN L2.SUBCLASSNAME ");
                stb.append("               ELSE T1.SUBCLASSNAME ");
                stb.append("               END ");
                stb.append("     END, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.YEAR ");
            }
            log.debug(stb);
            return stb.toString();
        }

        /**
         * <pre>
         *  学習記録データ・異動データ・学籍データのテーブルを設定します。
         *  ・在校生用と卒業生用のテーブル名を設定
         * </pre>
         */
        private void setFieldName() {
            _TableName_StudyRec = "SCHREG_STUDYREC_DAT";
            _TableName_Transfer = "SCHREG_TRANSFER_DAT";
            _TableName_Schreg_Regd = "SCHREG_REGD_DAT";
            log.debug("学習記録データは " + _TableName_StudyRec + " を使用。異動データは " + _TableName_Transfer + " を使用。学籍データは " + _TableName_Schreg_Regd + " を使用。");
        }
    }
}

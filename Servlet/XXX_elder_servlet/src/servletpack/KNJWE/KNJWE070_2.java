// kanji=漢字
/*
 * $Id: e07ea366e6f97b37de6a9a339f435e03b76b8cea $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJWE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJE.detail.KNJ_AttendrecSql;
import servletpack.KNJE.detail.KNJ_ExamremarkSql;
import servletpack.KNJE.detail.KNJ_MedexamSql;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSvfFieldModify;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.Vrw32alpWrap;

/*
 *  学校教育システム 賢者 [進路情報管理]  高校用調査書  就職用
 *
 *  2004/04/06 yamashiro・検査日の和暦変換を追加
 *  2004/08/17 yamashiro・住所１と住所２は別段に出力。長さに応じて文字の大きさを代える。
 *                      ・科目名は長さに応じて文字の大きさを代える。
 *  2004/09/06 yamashiro・学習の記録の行数をフォームに合わせるため変更
 *  2004/09/13 yamashiro・学習の記録欄において科目名を文字数により大きさを変えて出力する
 *                      ・教科(教科名および科目)が左右の欄に跨らないようにする
 *  2004/10/19 yamashiro・所見等において１行目が出力されない不具合を修正
 *  2004/12/14 yamashiro・成績がない場合出力されない不具合を修正
 *                      ・特別活動、欠席の理由、身体状況備考、長所をWIN2000とXP用の文字数に対応
 *  2005/11/18 yamashiro・学校情報を'今年度'と'卒業年度'の２種類を取得( =>Edit_SchoolInfoSqlにおいて )
 *  2006/03/20 yamashiro・全国高等学校統一用紙の改定に対応 --NO001
 */

public class KNJWE070_2 extends KNJWE070_1 {

    private static final Log log = LogFactory.getLog(KNJWE070_2.class);
    private int ret;                    //ＳＶＦ応答値
    static int line_max = 60;           //学習の記録の行数 実際の行数はline_max-3 2004/09/06Add
    private StringTokenizer st;
    private KNJSvfFieldModify svfobj;   //フォームのフィールド属性変更

    public KNJWE070_2(){
        super();
    }

    public KNJWE070_2(
            final DB2UDB db2,
            final Vrw32alpWrap svf,
            final KNJDefineSchool definecode
    ) throws SQLException{
        super(db2,svf,definecode);
    }

    /**
     *  PrepareStatement作成
     */
    public void pre_stat(final String hyotei) {
        try {
            log.debug("=== 在学生用 ===");
            // 学習記録データ
//            ps1 = getPreStatementStudyrec(hyotei);
            // 出欠記録データ
            KNJ_AttendrecSql obj_AttendrecSql = new KNJ_AttendrecSql();
            ps2 = db2.prepareStatement(obj_AttendrecSql.pre_sql());
            // 健康診断データ
            KNJ_MedexamSql obj_MedexamSql = new KNJ_MedexamSql();
            ps3 = db2.prepareStatement(obj_MedexamSql.preSqlMark());
            log.debug(obj_MedexamSql.preSqlMark());
            // 所見データ
            KNJ_ExamremarkSql obj_ExamremarkSql = new KNJ_ExamremarkSql();
            ps4 = db2.prepareStatement(obj_ExamremarkSql.pre_sql_empHdatNew());
            log.debug(obj_ExamremarkSql.pre_sql_empHdatNew());
            ps8 = db2.prepareStatement(obj_ExamremarkSql.pre_sql_empDatNew());
            log.debug(obj_ExamremarkSql.pre_sql_empDatNew());
            // 個人データ
            KNJ_PersonalinfoSql obj_Personalinfo = new KNJ_PersonalinfoSql();
            ps6 = db2.prepareStatement(obj_Personalinfo.studentInfoSql(true));
            log.debug(obj_Personalinfo.studentInfoSql(true));
            // 学校データ
            ps7 = getPreStatementSchoolInfo();
        } catch( Exception e ){
            log.error("pre_stat error!" + e );
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void printGradeTitleGakunenseiNyuugakumae(
            final int i,
            final String str
    ) {
        for (int j = 1; j <= 3; j++) {
            ret = svf.VrsOut("GRADE" + j + "_" + i + "_1" , "入");  // 学習の記録
            ret = svf.VrsOut("GRADE" + j + "_" + i + "_2" , "学");
            ret = svf.VrsOut("GRADE" + j + "_" + i + "_3" , "前");
        }

        ret = svf.VrsOut("GRADE4" + "_" + i + "_1" , "入");  // 出欠の記録
        ret = svf.VrsOut("GRADE4" + "_" + i + "_2" , "学");
        ret = svf.VrsOut("GRADE4" + "_" + i + "_3" , "前");
    }

    /**
     * {@inheritDoc}
     */
    protected void printGradeTitleTanisei() {
        Set mapi = _titleMap.keySet();
        for (Iterator t = mapi.iterator(); t.hasNext();) {
            Integer value = (Integer) t.next();
            String str = (String) _titleMap.get(value);
            svf.VrsOut("GRADE1_" + value.toString() + "_1", str.substring(0, 2)); // 学習の記録
            svf.VrsOut("GRADE1_" + value.toString() + "_2", str.substring(2, str.length() - 2));
            svf.VrsOut("GRADE1_" + value.toString() + "_3", str.substring(str.length() - 2));

            svf.VrsOut("GRADE2_" + value.toString() + "_1", str.substring(0, 2)); // 学習の記録
            svf.VrsOut("GRADE2_" + value.toString() + "_2", str.substring(2, str.length() - 2));
            svf.VrsOut("GRADE2_" + value.toString() + "_3", str.substring(str.length() - 2));

            svf.VrsOutn("GRADE4", value.intValue() , str);
            svf.VrsOutn("GRADE5", value.intValue() , str);
        }
    }    

    /**
     *  PrepareStatement close
     */
    public void pre_stat_f() {
        try {
            if( ps1 != null ) ps1.close();
            if( ps2 != null ) ps2.close();
            if( ps3 != null ) ps3.close();
            if( ps4 != null ) ps4.close();
            if( ps5 != null ) ps5.close();
            if( ps6 != null ) ps6.close();
            if( ps7 != null ) ps7.close();
            if( ps8 != null ) ps8.close();
        } catch( Exception e ){
            log.error("pre_stat_f error!" + e );
        }
    }

    /**
     *  学校情報 その２（進学用と就職用で異なる仕様） --NO001
     */
    public void head_out_Sub1(final ResultSet rs) {
        try {
            if (rs.getString("SCHOOLTELNO") != null) ret = svf.VrsOut("SCHOOL_PHONE", rs.getString("SCHOOLTELNO"));  // 学校電話番号
            if (rs.getString("SCHOOLNAME1") != null) ret = svf.VrsOut("SCHOOLNAME", rs.getString("SCHOOLNAME1"));  // 学校名
            if (rs.getString("PRINCIPAL_NAME") != null) ret = svf.VrsOut("STAFFNAME", rs.getString("PRINCIPAL_NAME"));  // 校長名
            if (null != rs.getString("STAFF2_NAME")) {
                svf.VrsOut( "STAFFNAME_2", rs.getString("STAFF2_NAME"));
                final String jobName = rs.getString("STAFF2_JOBNAME");
                final String setJobName = null != jobName ? jobName : "教諭";
                if (!isKindaifuzoku()) {
                    svf.VrsOut("JOBNAME", setJobName); // 記載責任者職名
                }
            }
        } catch( SQLException e ){
            log.error("[KNJWE070_2]head_out_Sub1 error! ", e );
        }
        head_out_Sub1_Yobi(rs);  // 予備１から３の出力
    }

    /** 
     *  SVF-FORM 学習の記録出力
     *  04/09/13 Modify 引数にmaxgradeを追加 
     *  NO001 Modify
     */
    public void study_out(
            final String schregno,
            final String year,
            final Map paramap
    ) {
        try {
            study_out_Sub1(schregno, year, paramap);
        } catch( Exception e ){
            log.error("[KNJWE070_2]study_out error!", e );
        }
    }

    /** 
     *  SVF-FORM 学習の記録出力
     *  04/09/13 Modify 引数にmaxgradeを追加 
     *  NO001 Modify
     */
    public void study_out_Sub1(
            final String schregno,
            final String year,
            final Map paramap
    ) throws SQLException {

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
                        setValuationAvg("subject", befClassName, "average_", classValution, classCnt, String.valueOf(classField));
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
                lineData.setPrintData(getCredit, rs.getString("YEAR"), rs.getString("ANNUAL"), valution);
                befClassCd = rs.getString("CLASSCD");
                befClassName = rs.getString("CLASSNAME");
                befSubclassCd = rs.getString("SUBCLASSCD");
                befSubclassName = rs.getString("SUBCLASSNAME");
                nonedata = true;
                hasData = true;
            }
            if (hasData) {
                printList.add(lineData);
                setValuationAvg("subject", befClassName, "average_", classValution, classCnt, String.valueOf(classField));
                setValuationAvg("", "", "average", totalClassValution, totalClassCnt, "");
                svf.VrEndRecord(); // 最後のレコード出力
            } else {
                // 学習情報がない場合の処理
                svf.VrsOut("CLASSCD", "A"); // 教科コード
                svf.VrEndRecord();
                nonedata = true;
            }

            //明細印字
            final int fieldCnt = meisaiPrintOut(printList, printTotalData);

            for (int i = fieldCnt; i < 57; i++) {
                if (i == fieldCnt) {
                    svf.VrsOut("CLASSCD", ""); // 教科コード
                } else {
                    svf.VrsOut("CLASSCD", String.valueOf(i)); // 教科コード
                }
                svf.VrEndRecord();
                nonedata = true;
            }

            printKei("SUBCLASS", "総合的な学習の時間", "GRADES", String.valueOf(printTotalData._sougouCredit));
        } finally {
            DbUtils.closeQuietly(rs);
        }


    }

    /**
     * 科目コード・教科コード・学校コードのブレイク時、Trueを戻します。<br>
     * 学校コードの場合、前後どちらかの SCHOOLCD が 2 であることを条件とします。<br>
     * つまり、大検は一行、前籍校(明細行の)は同一科目一行です。
     * @param rs
     * @param s_subclasscd
     * @param s_classcd
     * @param s_schoolcd
     * @return
     * @throws SQLException
     */
    private boolean isSubclasscdBreak(
            final ResultSet rs, 
            final String s_subclasscd, 
            final String s_classcd, 
            final String s_schoolcd
    ) throws SQLException {
        if (!rs.getString("SUBCLASSCD").equals(s_subclasscd)) { return true; }
        if (!rs.getString("CLASSCD").equals(s_classcd)) { return true; }
        if (!rs.getString("SCHOOLCD").equals(s_schoolcd)) {
            if ("2".equals(s_schoolcd) || "2".equals(rs.getString("SCHOOLCD"))) { return true; }
        }
        return false;
    }
    
    /* 
     *  残り空行出力処理
     */
    private void study_out_kara_out(
            int linex,
            String s_classcd
    ) {
        for (int i = linex; i < 55; i++) {
            if (i == linex) {
                ret = svf.VrsOut("CLASSCD",s_classcd);  // 教科コード
            } else {
                ret = svf.VrsOut("CLASSCD",String.valueOf(i));  // 教科コード
            }
            ret = svf.VrsOut("RECORD_MASK", "1"); //評定 MASK
            ret = svf.VrEndRecord();
            nonedata = true;
        }
    }

    /**
     *  SVF-FORM-OUT 健康状況データ
     **/
    public void medexam_out(
            final String schregno,
            final String year
    ) {
        try {
            ps3.setString(1,schregno);
            ps3.setString(2,year);
            ResultSet rs = ps3.executeQuery();

            if( rs.next() ){
                ret = svf.VrsOut("ymd4"         ,KNJ_EditDate.h_format_JP_M(rs.getString("DATE"))); // 04/04/06
                ret = svf.VrsOut("height"       ,rs.getString("HEIGHT"));
                ret = svf.VrsOut("weight"       ,rs.getString("WEIGHT"));
                ret = svf.VrsOut("R_BAREVISION" ,rs.getString("R_BAREVISION_MARK"));
                ret = svf.VrsOut("L_BAREVISION" ,rs.getString("L_BAREVISION_MARK"));
                ret = svf.VrsOut("R_VISION"     ,rs.getString("R_VISION_MARK"));
                ret = svf.VrsOut("L_VISION"     ,rs.getString("L_VISION_MARK"));
                ret = svf.VrsOut("R_EAR"        ,rs.getString("R_EAR"));
                ret = svf.VrsOut("L_EAR"        ,rs.getString("L_EAR"));
            }
            rs.close();
        } catch( Exception e ){
            log.error("medexam_out error!" + e );
        }
    }
    
    /** 
     *  SVF-FORM-OUT  所見データ出力
     *    04/10/19Modify 所見等において１行目が出力されない不具合を修正
     *    04/12/14Modify 特別活動、欠席の理由、身体状況備考、長所をwin2000とxp用の文字数に対応
     */
    public void exam_out(final String schregno, final String year, final Map paramap) {
    // ＤＢ検索（所見データ出力）
        ResultSet rs4 = null;
        ResultSet rs8 = null;
        try {

            for (final Iterator iter = _titleYear.keySet().iterator(); iter.hasNext();) {
                final String titleYear = (String) iter.next();
                examOutNoAttend("JOBHUNT_ABSENCE", 10, 7, ((Integer) _titleYear.get(titleYear)).intValue(), titleYear, schregno, paramap);
            }

            ps4.setString(1,schregno);
            rs4 = ps4.executeQuery();

            String arrRecom[];
            String arrHealth[];
            if (rs4.next()) {
                arrRecom = KNJ_EditEdit.get_token(rs4.getString("JOBHUNT_RECOMMEND") , 76, 10);
                if (arrRecom != null) {
                    for (int i = 0; i < arrRecom.length; i++) {
                        ret = svf.VrsOut("point" + (i+1), arrRecom[i]);
                    }
                }
                arrHealth = KNJ_EditEdit.get_token(rs4.getString("JOBHUNT_HEALTHREMARK") ,28 , 3);
                if (arrHealth != null) {
                    for (int i = 0; i < arrHealth.length; i++) {
                        ret = svf.VrsOut("note" + (i+1), arrHealth[i]);
                    }
                }
            }

            ps8.setString(1, schregno);
            rs8 = ps8.executeQuery();

            String arrRec[];
            String arrAbsence[];
            while (rs8.next()) {
                String strKey;
                strKey = rs8.getString("YEAR");
                if (null == strKey) {
                    continue;
                }
                int intKey = Integer.parseInt(strKey);
                if (0 == intKey) {
                    strKey = "0";
                }
                if (!_titleYear.containsKey(strKey)) {
                    continue;
                }
                Integer position = null;
                int fieldRetsu = 0;
                position = (Integer) _titleYear.get(strKey);
                fieldRetsu = position.intValue();

                arrRec = KNJ_EditEdit.get_token(rs8.getString("JOBHUNT_REC") , 10, 10);
                if (arrRec != null) {
                    for (int i = 0; i < arrRec.length; i++) {
                        ret = svf.VrsOutn( "JOBHUNT_REC" + (i+1), fieldRetsu, arrRec[i] );
                    }
                }
                arrAbsence = KNJ_EditEdit.get_token(rs8.getString("JOBHUNT_ABSENCE") , 10, 7);
                if (arrAbsence != null) {
                    printNoteClear("JOBHUNT_ABSENCE", fieldRetsu, 7);
                    for (int i = 0; i < arrAbsence.length; i++) {
                        svf.VrsOutn( "JOBHUNT_ABSENCE" + (i+1), fieldRetsu, arrAbsence[i] );
                    }
                }
            }
        } catch( Exception e ){
            log.error("exam_out error!" + e );
        } finally {
            DbUtils.closeQuietly(rs4);
        }
    }

    /*
     * ＳＶＦ−ＦＯＲＭフィールド属性変更(RECORD) => 文字数により文字ピッチ及びＹ軸を変更する  --NO001
     */
    private void svfFieldAttribute_CLASS(
            final Vrw32alpWrap svf,
            final String name,
            final int ln,
            final String fieldname
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            if( svfobj == null )svfobj = new KNJSvfFieldModify();
            svfobj.width = 216;      //フィールドの幅(ドット)
            svfobj.height = 60;      //フィールドの高さ(ドット)
            svfobj.ystart = 1100;    //開始位置(ドット)
            svfobj.minnum = 10;      //最小設定文字数
            svfobj.maxnum = 20;      //最大設定文字数
            svfobj.setRetvalue( name, ( ln % 30 == 0 )? 30: ln % 30 );

            if( ln <= 30 )ret = svf.VrAttribute(fieldname , "X="+ ( 58 + 10 ) );  //左列の開始Ｘ軸
            else ret = svf.VrAttribute(fieldname , "X="+ ( 1102 + 10 ) );  //右列の開始Ｘ軸

            ret = svf.VrAttribute(fieldname , "Y="+ svfobj.jiku );  //開始Ｙ軸
            ret = svf.VrAttribute(fieldname , "Size=" + svfobj.size );  //文字サイズ
            ret = svf.VrsOut(fieldname,  name );
        } catch( Exception e ){
            log.error("svf.VrAttribute error! ", e);
        }
    }

    /*
     * ＳＶＦ−ＦＯＲＭフィールド属性変更(RECORD) => 文字数により文字ピッチ及びＹ軸を変更する --NO001
     */
    private void svfFieldAttribute_SUBCLASS(
            final Vrw32alpWrap svf,
            final String name,
            final int ln,
            final String fieldname
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            if( svfobj == null )svfobj = new KNJSvfFieldModify();
            svfobj.width = 482;      //フィールドの幅(ドット)
            svfobj.height = 60;      //フィールドの高さ(ドット)
            svfobj.ystart = 1100;    //開始位置(ドット)
            svfobj.minnum = 20;      //最小設定文字数
            svfobj.maxnum = 40;      //最大設定文字数
            svfobj.setRetvalue( name, ( ln % 30 == 0 )? 30: ln % 30 );

            if( ln <= 30 )ret = svf.VrAttribute(fieldname, "X="+ ( 294 + 10 ) );  //左列の開始Ｘ軸
            else ret = svf.VrAttribute(fieldname, "X="+ ( 1338 + 10 ) );  //右列の開始Ｘ軸

            ret = svf.VrAttribute(fieldname, "Y="+ svfobj.jiku );  //開始Ｙ軸
            ret = svf.VrAttribute(fieldname, "Size=" + svfobj.size );  //文字サイズ
            ret = svf.VrsOut(fieldname,  name );
        } catch( Exception e ){
            log.error("svf.VrAttribute error! ", e);
        }
    }
    
    /**
     * @return
     */
    protected String getSvfForm4() {
        return "KNJWE070_3.frm";
    }

    /**
     * @return
     */
    protected String getSvfForm6() {
        return "KNJWE070_4.frm";
    }

}

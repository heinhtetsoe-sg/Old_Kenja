// kanji=漢字
/*
 * $Id: 6fe7db38775b938e86595790851b68165e677e07 $
 *
 * 作成日: 2007/01/30
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJC053_BASE;
import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJSvfFieldModify;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.Vrw32alpWrap;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票 <br>
 * KNJD173の成績処理改訂版として作成。
 */

public class KNJD173A {

    private static final int SUBJECT_LINE = 7;
    private static final int S_SUBCLASS_LINE = 7;

    private static final int RECORD_MAX_LINE = 25;
    
    private static final int REPLACE_FLG0 = 0;
    private static final int REPLACE_FLG1 = 1;
    private static final int REPLACE_FLG2 = 2;
    private static final int REPLACE_FLG9 = 9;
    private static final String KNJD173_1FORM = "KNJD173_1.frm";

    private static final Log log = LogFactory.getLog(KNJD173A.class);

    private int scredits;                   //修得済み単位数
    private int scredits2;                  //修得済み単位数  NO001
    private KNJObjectAbs knjobj;            //編集用クラス
    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定
    private KNJSvfFieldModify svfobj;   //フォームのフィールド属性変更 05/12/22 Build

    /**
     * D008の科目。
     */
    private final Map _subClassD008 = new HashMap();

    /**
     * D016：通知票・合併元科目の表示／非表示
     * _isNoPrintMoto：値が「Ｙ」の場合通知票に合併元科目を印刷しない
     * __isMirishuu  ：印刷しない場合未履修の欠時を合計に含まない
     */
    private boolean _isNoPrintMoto;
    private boolean _isMirishuu;
    private String _certifSchoolRemark1;
    
    /** 学校マスタ読み込み */
    private KNJSchoolMst _knjSchoolMst;

    /**
     *  KNJD.classから最初に起動されるクラス。
     */
    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        Vrw32alpWrap svf = new Vrw32alpWrap();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        boolean hasData = false;

        // print svf設定
        sd.setSvfInit( request, response, svf);
        // ＤＢ接続
        db2 = sd.setDb(request);
        if( sd.openDb(db2) ){
            log.error("db open error! ");
            return;
        }
        // 印刷処理
        hasData = printSvf(request, db2, svf);
        // 終了処理
        sd.closeSvf( svf, hasData );
        sd.closeDb(db2);
    }


    /**
     *  印刷処理のメソッドを実行し、正常に印刷されると true を戻します。
     */
    private boolean printSvf (
            final HttpServletRequest request,
            final DB2UDB db2,
            final Vrw32alpWrap svf
    ) {
        final Param parameter = new Param(request);
        try {
            loadNameMst(db2, parameter);
        } catch (SQLException e) {
            log.error("D008読込失敗", e);
        }
        try {
            loadNameMstD016(db2, parameter);
        } catch (SQLException e) {
            log.error("D016読込失敗", e);
        }
        try {
            loadCertifSchoolRemark1(db2, parameter);
        } catch (SQLException e) {
            log.error("証明書学校データ", e);
        }
        parameter.setSchregno();
        parameter.setAttendDate(db2);
        parameter.setHead(db2);
        parameter.loadAttendSemesArgument(db2);
        if (printSvfMain(db2, svf, parameter)) return true;
        return false;
    }

    private void loadNameMstD016(final DB2UDB db2, final Param parameter) throws SQLException {
        _isNoPrintMoto = false;
        _isMirishuu = false;
        final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + parameter._year + "' AND NAMECD1 = 'D016'";
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        while (rs.next()) {
            final String name = rs.getString("NAMESPARE1");
            final String name2 = rs.getString("NAMESPARE2");
            if ("Y".equals(name)) _isNoPrintMoto = true;
            if ("Y".equals(name2)) _isMirishuu = true;
        }
        db2.commit();
        DbUtils.closeQuietly(rs);
        log.debug("(名称マスタ D016):合併元科目を表示しない=" + _isNoPrintMoto);
        log.debug("(名称マスタ D016):未履修の欠時を合計に含まない=" + _isMirishuu);
    }

    private void loadCertifSchoolRemark1(final DB2UDB db2, final Param parameter) throws SQLException {
        final String sql = "SELECT REMARK1 FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + parameter._year + "' AND CERTIF_KINDCD = '104'";
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        while (rs.next()) {
            _certifSchoolRemark1 = rs.getString("REMARK1");
        }
        db2.commit();
        DbUtils.closeQuietly(rs);
    }

    private void loadNameMst(final DB2UDB db2, final Param parameter) throws SQLException {
        final String sql;
        if ("1".equals(parameter._useCurriculumcd) && "1".equals(parameter._useClassDetailDat)) {
            sql = "SELECT CLASSCD || SCHOOL_KIND AS NAMECD2, CLASS_REMARK1 AS NAME1 FROM CLASS_DETAIL_DAT WHERE YEAR = '" + parameter._year + "' AND CLASS_SEQ = '003'";
        } else {
            sql = "SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + parameter._year + "' AND NAMECD1 = 'D008'";
        }
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        while (rs.next()) {
            final String classCd = rs.getString("NAMECD2");
            final String name = rs.getString("NAME1");
            log.debug("(名称マスタ D008):教科コード=" + classCd);
            _subClassD008.put(classCd, name);
        }
        db2.commit();
        DbUtils.closeQuietly(rs);
    }

    /** 
     *  フォームを設定します。
     */
    private String setSvfForm2 (
            final Param parameter,
            final PreparedStatement ps,
            final String schregno
    ) {
        final String form;
        if (parameter.isPattarnC()) {
            form = "KNJD173_6_3.frm"; // 高等学校卒業程度認定単位欄 無　総合的な学習の時間　有
        } else 
        if (hasQualified(ps, schregno)) {
            form = "KNJD173_6.frm"; // 高等学校卒業程度認定単位欄 有
        } else {
            form = "KNJD173_6_2.frm"; // 高等学校卒業程度認定単位欄 無
        }
        return form;
    }
    
    /** 
     *  フォームを設定します。
     */
    private String setSvfForm (
            final Param parameter
    ) {
        String form = null;
        if (2 < parameter.definecode.semesdiv) {
            if (parameter._isprintB5) {
                if (parameter._frmNo == 1) {
                    form = "KNJD173_2_5.frm"; // ３学期用宛先無し
                } else {
                    form = "KNJD173_2_" + (4 + parameter._frmNo) + ".frm"; // ３学期用Ｂ５サイズ
                }
            } else if (!parameter._isprintGardianaAddress) {
                if (parameter._frmNo == 1) {
                    form = "KNJD173_2.frm"; // ３学期用宛先無し
                } else {
                    form = "KNJD173_2_" + parameter._frmNo + ".frm"; // ３学期用宛先無し
                }
            }
        } else {
            if (parameter._isprintB5) {
                if (parameter._frmNo == 1) {
                    form = "KNJD173_4.frm"; // ２学期用Ｂ５サイズ
                } else {
                    form = "KNJD173_4_" + parameter._frmNo + ".frm"; // ２学期用Ｂ５サイズ
                }
            } else if (!parameter._isprintGardianaAddress) {
                if (parameter._frmNo == 1) {
                    form = "KNJD173_3.frm"; // ２学期用宛先無し
                } else {
                    form = "KNJD173_3_" + parameter._frmNo + ".frm"; // ２学期用宛先無し
                }
            } else {
                form = KNJD173_1FORM;  // ２学期用宛先有り
            }
        }
        return form;
    }

    /** 
     * 印刷処理。
     */
    private boolean printSvfMain (
            final DB2UDB db2,
            final Vrw32alpWrap svf,
            final Param parameter
    ) {
        knjobj = new KNJEditString();
        PreparedStatement arrps[] = new PreparedStatement[6];
        MyDateFormat mydf = new MyDateFormat();

        try {
            _knjSchoolMst = new KNJSchoolMst(db2, parameter._year);

            arrps[0] = db2.prepareStatement(prestatementRegd(parameter));           //学籍データ
            arrps[2] = db2.prepareStatement(prestatementSubclass(parameter));       //成績明細データ

            String prestatementAttendSemes = AttendAccumulate.getAttendSemesSql(
                    parameter._semesFlg,
                    parameter._defineSchool,
                    parameter._knjSchoolMst,
                    parameter._year,
                    parameter.SSEMESTER,
                    parameter._semester,
                    (String) parameter._hasuuMap.get("attendSemesInState"),
                    parameter._periodInState,
                    (String) parameter._hasuuMap.get("befDayFrom"),
                    (String) parameter._hasuuMap.get("befDayTo"),
                    (String) parameter._hasuuMap.get("aftDayFrom"),
                    (String) parameter._hasuuMap.get("aftDayTo"),
                    parameter._grade_hr_class.substring(0, 2),
                    parameter._grade_hr_class.substring(2, 5),
                    "?",
                    "SEMESTER",
                    parameter._useCurriculumcd,
                    parameter._useVirus,
                    parameter._useKoudome
            );
            arrps[3] = db2.prepareStatement(prestatementAttendSemes);                        //出欠データ
            arrps[4] = db2.prepareStatement(prestatementGetCredits(parameter));     //過去の修得単位数取得 05/08/15Build
            //Ｂ５サイズでは否出力
            if (!parameter._isprintB5 || parameter.isPattarnB() || parameter.isPattarnC()) {
                arrps[1] = db2.prepareStatement(prestatementQualified(parameter));      //学校外における学修の単位認定データ
                arrps[5] = db2.prepareStatement(prestatementCommitAndClub(parameter));  //委員会、クラブ活動 05/09/12Build
            }
        } catch (Exception e2) {
            log.error("Exception", e2);
            return false;
        }

        // データ読み込み＆ＳＶＦ出力
        boolean hasData = false;
        try {
            ResultSet rs1 = arrps[0].executeQuery();
            while( rs1.next() ){
                
                final String form;
                if (parameter.isPattarnB() || parameter.isPattarnC()) {
                    form = setSvfForm2(parameter, arrps[1], rs1.getString("SCHREGNO"));
                    svf.VrSetForm(form, 4); // 高等学校卒業程度認定単位欄 有
                    svf.VrAttribute("ATTENDNO", "FF=1");  // 出席番号で改ページ
                    svf.VrsOut("NOTE", _certifSchoolRemark1);

                } else {
                    form = setSvfForm(parameter);
                    svf.VrSetForm(form, 4);
                    svf.VrAttribute("ATTENDNO", "FF=1");  // 出席番号で改ページ
                }
                log.debug(" form = " + form);
                    
                // 学籍情報等の印刷
                printSvfRegdOut( db2, svf, rs1, parameter, form);
                // 修得済み単位数の集計
                scredits2 = 0;
                doAddScredits(svf, arrps[4], Integer.parseInt(parameter._semester), rs1.getString("SCHREGNO"));
                // 出欠データの印刷
                printSvfAttend(svf, arrps[3], rs1.getString("SCHREGNO"), Integer.parseInt( parameter._semester ), parameter);
                if (!parameter._isprintB5 || parameter.isPattarnB() || parameter.isPattarnC()) {
                    // 学校外における学修の単位認定データの印刷
                    printSvfQualified(svf, arrps[1], rs1.getString("SCHREGNO"), mydf);
                    // 委員会・部活動の印刷
                    printSvfCommitAndClub(svf, arrps[5], rs1.getString("SCHREGNO"));
                }
                // 成績明細データの印刷
                scredits = 0;
                printSvfRecDetail(svf, arrps[2], rs1.getString("SCHREGNO"), parameter);
                // 修得済み単位数の印刷
                printSvfScreditsOut(svf, parameter);
                svf.VrEndRecord();
                hasData = true;
            }
            db2.commit();
            rs1.close();
        } catch( Exception ex ) { log.error("printSvfMain read error! "+ex);    }

        try {
            prestatementClose( arrps );
        } catch (SQLException e) {
            log.error("SQLException", e);
        }

        return hasData;
    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    /**
     * <pre> 
     * 年度・学校名・担任名・生徒氏名等を印字します。
     * 総合的な学習の時間の所見・通信欄を印字します。
     * 
     * 2005/08/17 あて先出力の有無を追加
     * </pre>
     */
    private void printSvfRegdOut (
            final DB2UDB db2,
            final Vrw32alpWrap svf,
            final ResultSet rs,
            final Param parameter,
            final String form
    ) throws Exception {
        svf.VrsOut("NENDO", parameter._nendo);  // 年度
        svf.VrsOut("SCHOOLNAME", parameter._schoolName);  // 学校名
        svf.VrsOut("STAFFNAME1", parameter._principalName);  // 校長名
        for( int i = 1 ; i <= parameter._arrstaffName.size() ; i++ )
            svf.VrsOut("STAFFNAME" + (i+1),   (String)parameter._arrstaffName.get(i-1) ); //担任名 05/12/16 Modify
        if (parameter._isprintGardianaAddress){
            svf.VrsOut("ZIPCODE",      rs.getString("GUARD_ZIPCD"));      //郵便番号
            final String addr1 = rs.getString("GUARD_ADDR1");
            final String addr2 = rs.getString("GUARD_ADDR2");
            if (KNJD173_1FORM.equals(form) && "1".equals(parameter._useAddrField2) && (getMS932ByteLength(addr1) > 50 || getMS932ByteLength(addr2) > 50)) {
                svf.VrsOut("ADDRESS1_3",   addr1);      //住所1
                svf.VrsOut("ADDRESS2_3",   addr2);      //住所2
            } else if (KNJD173_1FORM.equals(form) && "1".equals(parameter._useAddrField2) && (getMS932ByteLength(addr1) > 40 || getMS932ByteLength(addr2) > 40)) {
                svf.VrsOut("ADDRESS1_2",   addr1);      //住所1
                svf.VrsOut("ADDRESS2_2",   addr2);      //住所2
            } else {
                svf.VrsOut("ADDRESS1",     addr1);      //住所1
                svf.VrsOut("ADDRESS2",     addr2);      //住所2
            }
        }
        svf.VrsOut("GUARD_NAME",   rs.getString("GUARD_NAME"));       //保護者氏名
        svf.VrsOut("COURSE",       rs.getString("COURSENAME"));       //課程
        svf.VrsOut("MAJOR",        rs.getString("MAJORNAME"));        //学科
        svf.VrsOut("HR_NAME",      rs.getString("HR_NAME"));          //組名称
        svf.VrsOut("ATTENDNO",     rs.getString("ATTENDNO"));         //出席番号
        String name = null != rs.getString("NAME") ? rs.getString("NAME") : "";
        String nameNo = 10 < name.length() ? "2" : "";
        svf.VrsOut("NAME" + nameNo,         name);             //生徒氏名
 
        //注釈を表示
        db2.query("SELECT VALUE(NAMESPARE1,'') || ':' || VALUE(NAME1,'') AS NOTE "
                + "FROM V_NAME_MST "
                + "WHERE YEAR = '" + parameter._year + "' AND NAMECD1 = 'Z011' "
                + "ORDER BY NAMECD1");

        final ResultSet rsName = db2.getResultSet();
        String note = "※必履修区分…";
        String noteSep = "";
        while (rsName.next()) {
            note += noteSep + rsName.getString("NOTE");
            noteSep = "、";
        }
        db2.commit();
        if (!noteSep.equals("") && !parameter.isPattarnB() && !parameter.isPattarnC()) {
            svf.VrsOut("NOTE", note);
        }

        if (parameter.isPattarnB() || parameter.isPattarnC()) {
            setHreport(svf, rs, 1, "COMMUNICATION1", "通信欄");
            setHreport(svf, rs, 2, "COMMUNICATION2", "通信欄");
            setHreport(svf, rs, 3, "COMMUNICATION3", "通信欄");
            for (int i = 0; i < parameter._arrsemesName.size(); i++) {
                svf.VrsOut("SEMES1_" + (i + 1), (String) parameter._arrsemesName.get(i));        //学習の記録学期名称
                svf.VrsOut("SEMES2_" + (i + 1), (String) parameter._arrsemesName.get(i));        //学習の記録学期名称
                svf.VrsOut("SEMES3_" + (i + 1), (String) parameter._arrsemesName.get(i));        //出欠の記録学期名称
            }
            if (parameter.isPattarnC()) {
                setHreportPattarnC(svf, rs, 2, "TOTALSTUDYTIME", "総合的な学習の時間");
            }
        } else {
            int item = 1;
            if (parameter._isTotalstudytime) {
                setHreport(svf, rs, item, "TOTALSTUDYTIME", "総合的な学習の時間");
                item++;
            }
            if (parameter._isSpecialactremark) {
                setHreport(svf, rs, item, "SPECIALACTREMARK", "奉仕");
                item++;
            }
            if (parameter._isCommunication) {
                setHreport(svf, rs, item, "COMMUNICATION", "通信欄");
                item++;
            }

            for( int i = 0 ; i < parameter._arrsemesName.size() ; i++ ){
                svf.VrsOut("SEMES1_" + (i+1),   (String)parameter._arrsemesName.get(i) );            //学習の記録学期名称
                if( knjobj.retStringByteValue( (String)parameter._arrsemesName.get(i), 10 ) < 7 )
                    svf.VrsOut("SEMES2_" + (i+1),   (String)parameter._arrsemesName.get(i) );        //出欠の記録学期名称
                else
                    svf.VrsOut("SEMES3_" + (i+1),   (String)parameter._arrsemesName.get(i) );        //出欠の記録学期名称

            }
        }
    }

    private void setHreport(
            final Vrw32alpWrap svf,
            final ResultSet rs,
            final int item,
            final String rsName,
            final String fieldTitle
    ) throws Exception {
        final String fieldName = item == 1 ? "TOTAL_STUDY" : item == 2 ? "SERVICE" : "CORRESPONDENCE";
        svf.VrsOut("ITEM" + item, fieldTitle);

        ArrayList arrlist;
        arrlist = knjobj.retDividString( rs.getString(rsName), 42, 5 );
        if ( arrlist != null ) {
            for( int i = 0 ; i < arrlist.size() ; i++ ){
                svf.VrsOut(fieldName+ ( i+1 ),  (String)arrlist.get(i) );         //総合的な学習
            }
        }
    }

    private void setHreportPattarnC(
            final Vrw32alpWrap svf,
            final ResultSet rs,
            final int item,
            final String rsName,
            final String fieldTitle
    ) throws Exception {
        final String fieldName = "TOTAL_STUDY1";
        svf.VrsOut("ITEM" + item, fieldTitle);

        ArrayList arrlist;
        arrlist = knjobj.retDividString( rs.getString(rsName), 42, 5 );
        if ( arrlist != null ) {
            for( int i = 0 ; i < arrlist.size() ; i++ ){
                svf.VrsOut(fieldName+ ( i+1 ),  (String)arrlist.get(i) );         //総合的な学習
            }
        }
    }


    /**
     * <pre> 
     * 成績明細を印字するメソッドを実行します。
     * </pre>
     */
    private boolean printSvfRecDetail (
            final Vrw32alpWrap svf,
            final PreparedStatement ps,
            final String schregno,
            final Param parameter
    ) {
        boolean hasData = false;
        try {
            int pp = 0;
            ps.setString(++pp, schregno);
            ps.setString(++pp, schregno);
            ps.setString(++pp, schregno);
            ps.setString(++pp, schregno);
            ps.setString(++pp, schregno);
            ResultSet rs = ps.executeQuery();
            boolean bsubclass90 = false;
            int i = 0;
            while (rs.next()) {
                // 合併元科目の非表示
                int replaceflg = rs.getInt("REPLACEFLG");  // 合併先=9 合併元=1 合併先＆元=2
                if (REPLACE_FLG1 == replaceflg && _isNoPrintMoto) { continue; }
//                if (REPLACE_FLG1 == parseInt(rs.getString("PRINT_FLG"))) { continue; }
                if (hasData) {
                    svf.VrEndRecord();
                    i++;
                }
                final String classCd = "1".equals(parameter._useCurriculumcd) ? rs.getString("SUBCLASSCD").substring(4, 6) : rs.getString("SUBCLASSCD").substring(0, 2);
                if ((KNJDefineSchool.subject_T.equals(classCd) || rs.getString("NUM90_OTHER") != null) && ! bsubclass90) {
                    //総合的な学習の時間は１行空けて出力する
                    i += printBlankLine(svf, rs.getString("NUM90"), i);
                    bsubclass90 = true;
                }
                if (printSvfRecDetailOut(svf, rs, (i + 1), parameter)) hasData = true;
            }
            rs.close();
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
        return hasData;
    }


    /** 
     * <pre>
     * 成績明細を印字します。
     * ・履修単位：欠課が授業時数の１／３を超えない場合 => 欠課時数 ＜＝ 単位数×absence_high×学期／３ <= これは間違い
     * ・修得単位：履修科目において評定が１ではない場合
     * ・総合的な学習の時間を最終行に出力する。１ページを超えるデータは出力しない。(2005/09/12)
     * ・教科名５文字以上科目名１５文字以上（全角）の文字サイズを調整して出力(2005/09/20)
     * </pre> 
     */
    private boolean printSvfRecDetailOut (
            final Vrw32alpWrap svf,
            final ResultSet rs,
            int line,
            final Param parameter
    ) throws SQLException {
        if (RECORD_MAX_LINE < line) {
            line = (line % RECORD_MAX_LINE == 0)? RECORD_MAX_LINE: line % RECORD_MAX_LINE;
        }
        svfFieldAttribute_CLASS(svf, rs.getString("CLASSNAME"), line);
        svfFieldAttribute_SUBCLASS(svf, rs.getString("SUBCLASSNAME"), line);

        final int replaceflg = rs.getInt("REPLACEFLG");  // 合併先=9 合併元=1 合併先＆元=2
        final String calculate_credit_flg = StringUtils.defaultString(rs.getString("CALCULATE_CREDIT_FLG"), "0");  // 固定=1　加算=2

        doOutCredits(svf, replaceflg, calculate_credit_flg, rs);
        // 学期の評価・欠課：合併科目は非表示
        if (REPLACE_FLG0 == replaceflg  || REPLACE_FLG1 == replaceflg  || REPLACE_FLG9 == replaceflg) {
            // １学期
            printSvfGrading(svf, "GRADING1", rs.getString("SEM1_VALUE"));
            if (!parameter.isPattarnB() && !parameter.isPattarnC()) {
                printSvfKintai(svf, "KEKKA1", replaceflg, calculate_credit_flg, (REPLACE_FLG9 == replaceflg) ? rs.getString("REPLACE_ABSENT_SEM1"): rs.getString("ABSENT_SEM1"));
            }
//          log.debug("replaceflg="+replaceflg+", calculate_credit_flg="+calculate_credit_flg+", SUBCLASSNAME="+rs.getString("SUBCLASSNAME"));
            // ２学期
            int semeInt = Integer.parseInt(parameter._semester);
            if (1 < semeInt) {
                printSvfGrading(svf, "GRADING2", rs.getString("SEM2_VALUE"));
                if (!parameter.isPattarnB() && !parameter.isPattarnC()) {
                    printSvfKintai(svf, "KEKKA2", replaceflg, calculate_credit_flg, (REPLACE_FLG9 == replaceflg) ? rs.getString("REPLACE_ABSENT_SEM2"): rs.getString("ABSENT_SEM2"));
                }
            }

            // ３学期
            if (2 < semeInt) {
                printSvfGrading(svf, "GRADING3", rs.getString("SEM3_VALUE"));
                if (!parameter.isPattarnB() && !parameter.isPattarnC()) {
                    printSvfKintai(svf, "KEKKA3", replaceflg, calculate_credit_flg, (REPLACE_FLG9 == replaceflg) ? rs.getString("REPLACE_ABSENT_SEM3"): rs.getString("ABSENT_SEM3"));
                }
            }
            if (parameter.isPattarnB() || parameter.isPattarnC()) {
                printSvfKintai(svf, "CHIKOKU", replaceflg, calculate_credit_flg, rs.getString(REPLACE_FLG9 == replaceflg ? "REPLACE_LATE_TOTAL" : "LATE_TOTAL"));
                printSvfKintai(svf, "SOUTAI", replaceflg, calculate_credit_flg, rs.getString(REPLACE_FLG9 == replaceflg ? "REPLACE_EARLY_TOTAL" : "EARLY_TOTAL"));
                printSvfKintai(svf, "KETUJI", replaceflg, calculate_credit_flg, rs.getString(REPLACE_FLG9 == replaceflg ? "REPLACE_KETUJI_TOTAL" : "KETUJI_TOTAL"));
                printSvfKintai(svf, "KEKKA", replaceflg, calculate_credit_flg, rs.getString(REPLACE_FLG9 == replaceflg ? "REPLACE_ABSENT_TOTAL" : "ABSENT_TOTAL"));
            }
        }
        if (parameter.definecode.semesdiv == Integer.parseInt(parameter._semester)) {
            String grad_value = null;
            try {
                grad_value = rs.getString("GRAD_VALUE");
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
            final String classCd;
            if ("1".equals(parameter._useCurriculumcd)) {
                if ("1".equals(parameter._useClassDetailDat)) {
                    classCd = rs.getString("SUBCLASSCD").substring(0, 3);
                } else {
                    classCd = rs.getString("SUBCLASSCD").substring(4, 6);
                }
            } else {
                classCd = rs.getString("SUBCLASSCD").substring(0, 2);
            }
            if (null != grad_value) {
                doOutGradGrades(svf, replaceflg, calculate_credit_flg, parameter, classCd, grad_value);
            }
            
            String absent = null;
            try {
                absent = REPLACE_FLG9 == replaceflg ? rs.getString("REPLACE_ABSENT_TOTAL"): rs.getString("ABSENT_TOTAL");
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
            if (null != absent) {
                doOutGradAbsent(svf, replaceflg, calculate_credit_flg, absent);
            }
            
            String credits = null;
            try {
                credits = rs.getString("COMP_CREDIT");
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
            if (null != credits) { 
                doOutComp_Credits(svf, replaceflg, calculate_credit_flg, credits);
            }
            
            String gcredits = null;
            try {
                gcredits = rs.getString("GET_CREDIT");
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
            if (null != gcredits) {
                doOutAddGet_Credits(svf, replaceflg, calculate_credit_flg, gcredits);
            }
        }

        return true;
    }
    
    private void printSvfGrading(final Vrw32alpWrap svf, final String field, String value) {
        if (isCharacter(value)) {
            svf.VrAttribute(field, "Hensyu=3");
        }
        svf.VrsOut(field, value);
    }
    
    private void printSvfKintai(final Vrw32alpWrap svf, final String field, final int replaceflg, final String calculate_credit_flg, String count) {
        if (REPLACE_FLG9 == replaceflg && "2".equals(calculate_credit_flg) && !_isNoPrintMoto) count = "";
        svf.doSvfOutNonZero(field, count);
    }

    
    /**
     * <pre>
     * 単位数を印字します。
     * ・合併先科目の表示について
     *   科目の合併で加算の場合は表示しない。科目の合併で固定の場合は単位マスタの単位をセットする。(原文)
     * ・合併元科目の単位数は、単位マスタから単位数を表示する。(原文)
     * </pre>
     * @param svf
     * @param rs
     * @since 2007/02/27
     */
    private void doOutCredits (
            final Vrw32alpWrap svf,
            final int replaceflg,
            final String calculate_credit_flg,
            final ResultSet rs
    ){
        String credits = null;
        String combinedCredits = null;

        try {
            svf.VrsOut("R_DIV", rs.getString("NAMESPARE1"));

            credits = rs.getString("CREDITS");
            combinedCredits = rs.getString("COMBINED_CREDITS");
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
        if (null == credits) { return; }
        
        if (REPLACE_FLG9 == replaceflg) {
            if ("1".equals(calculate_credit_flg)) {
                svf.VrsOut("CREDIT", credits);
            } else if ("2".equals(calculate_credit_flg)) {
                svf.VrsOut("CREDIT", combinedCredits);
            }
        } else {
            svf.VrsOut("CREDIT", credits);
        }
    }

    
    /**
     * <pre>
     * 学年評定を印字します。
     * ・最終学期のみ表示。
     * ・合併元科目の表示について科目の合併が
     * 　・加算タイプの場合学年の評定は表示する。(ほぼ原文)
     * 　・固定の場合学年の評定は表示しない（入力されない）(原文)
     * </pre>        
     * @param svf
     * @since 2007/02/27
     */
    private void doOutGradGrades (
            final Vrw32alpWrap svf,
            final int replaceflg,
            final String calculate_credit_flg,
            final Param paramater,
            final String classCd,
            final String grad_value
    ) {
        
        if (REPLACE_FLG1 == replaceflg || REPLACE_FLG2 == replaceflg) {
            return;
        }
        if (!_subClassD008.containsKey(classCd)) {
            svf.VrsOut("TOTAL_GRADING",  grad_value);
        }
    }

    
    /**
     * <pre>
     * 学年欠課を印字します。
     * ・最終学期のみ表示。
     * ・合併先科目の表示について
     *   ・学年の欠課時数は出力しない。(原文)
     * ・合併元科目の表示について科目の合併が
     *   ・加算タイプの場合学年の欠課時数は表示する。(ほぼ原文)
     *   ・固定の場合学年の学年の欠課時数は表示する。(原文)
     * </pre>        
     * @param svf
     * @since 2007/02/27
     */
    private void doOutGradAbsent (
            final Vrw32alpWrap svf,
            final int replaceflg,
            final String calculate_credit_flg,
            final String absent
    ){
        if (REPLACE_FLG9 == replaceflg && "2".equals(calculate_credit_flg) && !_isNoPrintMoto) { return; }
        
        svf.doSvfOutNonZero("TOTAL_KEKKA",  absent);
    }

    
    /**
     * @param str
     * @return strが文字列の場合はtrueを返す。
     */
    boolean isCharacter (final String str) {
        String value = str;
        return !StringUtils.isEmpty(value) && !StringUtils.isNumeric(value);
    }


    /** 
     * <pre>
     * メンバ変数 int scredits2 へ前年度までの修得済み単位数をセットします。
     * ・int scredits は今年度の修得単位数
     * ・definecode.semesdiv は学校マスタの学期区分
     * 
     * 2005/08/16 前期は前年度までの修得単位を表記、後期は当年度と昨年度までの単位数を合算して表記(08/06仕様決定)
     * </pre>
     */
    private void doAddScredits (
            final Vrw32alpWrap svf,
            final PreparedStatement ps,
            final int semes,
            final String schno
    ) {
        try {
            int pp = 0;
            ps.setString(++pp, schno);
            ps.setString(++pp, schno);
            ps.setString(++pp, schno);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                scredits2 = parseInt(rs.getString("CREDITS"));
            }
            rs.close();
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
    }


    /** 
     * <pre>
     * 修得済み単位数(合計)を印字します。
     * ・int scredits は今年度の修得単位数
     * ・int scredits2 は前年度までの修得単位
     * </pre>
     */
    private void printSvfScreditsOut (
            final Vrw32alpWrap svf,
            final Param parameter
    ) {
        if (Integer.parseInt(parameter._semester) == parameter.definecode.semesdiv) {
            svf.doSvfOutNonZero("M_CREDIT",  scredits + scredits2);
        } else {
            svf.doSvfOutNonZero("M_CREDIT",  scredits2);
        }
    }


    /**
     * <pre>
     * 履修単位数を印刷します。
     * ・最終学期のみ表示。
     * ・合併元科目の表示について科目の合併が
     *   ・加算タイプの場合履修、修得単位数を括弧付きで表示する。(原文)
     *   ・固定の場合学年履修、修得単位数は表示しない(原文)
     * </pre>
     */
    private void doOutComp_Credits (
            final Vrw32alpWrap svf,
            final int replaceflg,
            final String calculate_credit_flg,
            String credits
    ) {
        if (REPLACE_FLG1 == replaceflg || REPLACE_FLG2 == replaceflg) {
            if ("2".equals(calculate_credit_flg)) {
                credits = "(" + credits + ")";
                switch (credits.length()) {
                    case 3: svf.VrsOut("R_CREDIT",  " " + credits); break;
                    case 2: svf.VrsOut("R_CREDIT",  "  " + credits); break;
                    default: svf.VrsOut("R_CREDIT",  credits); break;
                }
            }
            return;
        }
        switch (credits.length()) {
            case 1: svf.VrsOut("R_CREDIT",  "  " + credits); break;
            case 2: svf.VrsOut("R_CREDIT",  " " + credits); break;
            default: svf.VrsOut("R_CREDIT",  credits); break;
        }
    }


    /**
     * <pre>
     * 修得単位数を印刷し、修得済単位数(今年度)へ累積します。
     * ・最終学期のみ表示。
     * ・合併元科目の表示について科目の合併が
     *   ・加算タイプの場合履修、修得単位数を括弧付きで表示する。(原文)
     *   ・固定の場合学年履修、修得単位数は表示しない(原文)
     * ・修得済単位数の表示について
     *   ・合併元に表示した修得済単位数（括弧付き）は加算しない。(原文)
     * </pre>
     */
    private void doOutAddGet_Credits (
            final Vrw32alpWrap svf,
            final int replaceflg,
            final String calculate_credit_flg,
            String credits
    ) {
        if (REPLACE_FLG1 == replaceflg || REPLACE_FLG2 == replaceflg) {
            if ("2".equals(calculate_credit_flg)) {
                credits = "(" + credits + ")";
                switch (credits.length()) {
                    case 3: svf.VrsOut("S_CREDIT",  " " + credits); break;
                    case 2: svf.VrsOut("S_CREDIT",  "  " + credits); break;
                    default: svf.VrsOut("S_CREDIT",  credits); break;
                }
            }
        } else {
            switch (credits.length()) {
            case 1: svf.VrsOut("S_CREDIT",  "  " + credits); break;
            case 2: svf.VrsOut("S_CREDIT",  " " + credits); break;
            default: svf.VrsOut("S_CREDIT",  credits); break;
            }
            scredits += parseInt(credits);
        }
    }


    /**
     * <pre>
     * 文字列を整数に変換して戻します。
     * ・null、ブランク、数値以外は０を戻します。
     * </pre>
     * @param str
     * @return 
     */
    private int parseInt(final String str) {
        if (null == str) return 0;
        if (StringUtils.isEmpty(str)) return 0;
        if (!StringUtils.isNumeric(str)) return 0;
        return Integer.parseInt(str);
    }

    /** 
     * 該当学期までに欠課時数があれば true を戻します。
     */
    private boolean checkTotalAbsent (
            final ResultSet rs,
            final int semes
    ) throws SQLException {
        if (null != rs.getString("ABSENT_SEM1")) return true;
        if (1 < semes  &&  null != rs.getString("ABSENT_SEM2")) return true;
        if (2 < semes  &&  null != rs.getString("ABSENT_SEM3")) return true;
        return false;
    }


    /** 
     * 学校外における学修の単位認定・高等学校卒業程度認定単位印字するメソッドを実行します。
     */
    private boolean hasQualified (
            final PreparedStatement ps,
            final String schregno
    ) {
        boolean ret = false;
        ResultSet rs = null;
        try {
            int pp = 0;
            ps.setString( ++pp, schregno );  // 生徒番号
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString("CONDITION_DIV").equals("1") || rs.getString("CONDITION_DIV").equals("2")) {
                } else {
                    ret = true;
                }
            }
        } catch( SQLException ex ){
            log.error( "error! ", ex );
        } finally{
            DbUtils.closeQuietly(rs);
        }
        return ret;
    }


    /** 
     * 学校外における学修の単位認定・高等学校卒業程度認定単位印字するメソッドを実行します。
     */
    private void printSvfQualified (
            final Vrw32alpWrap svf,
            final PreparedStatement ps,
            final String schregno,
            final MyDateFormat mydf
    ) {
        ResultSet rs = null;
        try {
            int pp = 0;
            ps.setString( ++pp, schregno );  // 生徒番号
            rs = ps.executeQuery();
            int i = 0;
            int j = 0;
            while (rs.next()) {
                if (rs.getString("CONDITION_DIV").equals("1") || rs.getString("CONDITION_DIV").equals("2")) {
                    i++;
                    printSvfQualifiedOut1(svf,rs,i);
                } else {
                    j++;
                    try {
                        printSvfQualifiedOut2(svf,rs,j,mydf);
                    } catch (ParseException e) {
                         log.error("ParseException", e);
                    }
                }
            }
        } catch( SQLException ex ){
            log.error( "error! ", ex );
        } finally{
            try { rs.close(); } catch( SQLException ex ){ log.error( "error!", ex ); }
        }
    }


    /**
     * 学校外における学修の単位認定を印字します。
     */
    private void printSvfQualifiedOut1 (
            final Vrw32alpWrap svf,
            final ResultSet rs, int i
    ) throws SQLException {
        if (SUBJECT_LINE < i) { return; }
        if (rs.getString("SUBCLASSNAME") != null  &&  10 < knjobj.retStringByteValue(rs.getString("SUBCLASSNAME"),12)) {
            svf.VrsOutn("SUBJECT2", i, rs.getString("SUBCLASSNAME"));  // 科目名
        } else {
            svf.VrsOutn("SUBJECT1", i, rs.getString("SUBCLASSNAME"));  // 科目名
        }
        svf.VrsOutn("CONTENTS", i, rs.getString("NAME1"));  // 内容・資格
        svf.VrsOutn("CREDIT3", i, rs.getString("CREDITS"));  // 単位数
    }

    
    /**
     * 高等学校卒業程度認定単位を印字します。
     */
    private void printSvfQualifiedOut2 (
            final Vrw32alpWrap svf,
            final ResultSet rs, int i,
            final MyDateFormat mydate
    ) throws SQLException, ParseException {
        if (S_SUBCLASS_LINE < i) { return; }
        if (rs.getString("CONTENTS") != null  &&  12 < knjobj.retStringByteValue(rs.getString("CONTENTS"),14)) {
            svf.VrsOutn("S_SUBCLASS2", i, rs.getString("CONTENTS"));  // 合格科目名
        } else {
            svf.VrsOutn("S_SUBCLASS1", i, rs.getString("CONTENTS"));  // 合格科目名
        }
        if (rs.getString("SUBCLASSNAME") != null  &&  12 < knjobj.retStringByteValue(rs.getString("SUBCLASSNAME"),14)) {
            svf.VrsOutn("C_SUBCLASS2", i, rs.getString("SUBCLASSNAME"));  // 互換科目名
        } else {
            svf.VrsOutn("C_SUBCLASS1", i, rs.getString("SUBCLASSNAME"));  // 互換科目名
        }
        svf.VrsOutn("C_CREDIT", i, rs.getString("CREDITS"));  // 互換単位数

        String st = mydate.format(rs.getString("REGDDATE"));
        svf.VrsOutn("DATE", i, st);  // 年月日
    }

    
    /** 
     * 出欠明細を印字するメソッドを実行します。
     */
    private void printSvfAttend (
            final Vrw32alpWrap svf,
            final PreparedStatement ps,
            final String schregno,
            final int sem,
            final Param parameter
    ) {
        try {
            int pp = 0;
            ps.setString( ++pp, schregno );  // 生徒番号
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                printSvfAttendOut(svf, rs, parameter);
            }
            rs.close();
        } catch (NumberFormatException e) {
            log.error("NumberFormatException", e);
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
    }


    /** 
     * 出欠明細を印字します。
     */
    private void printSvfAttendOut (
            final Vrw32alpWrap svf,
            final ResultSet rs,
            final Param parameter
    ) throws NumberFormatException, SQLException {
        
        int i = Integer.parseInt(rs.getString("SEMESTER"));
        if (i == 9) { i = parameter.definecode.semesdiv + 1; }

        svf.VrsOutn("LESSON", i, rs.getString("LESSON") );  // 授業日数
        svf.VrsOutn("KIBIKI", i, String.valueOf(rs.getInt("MOURNING") + rs.getInt("SUSPEND") +  ("true".equals(parameter._useVirus) ? rs.getInt("VIRUS") : 0) + ("true".equals(parameter._useKoudome) ? rs.getInt("KOUDOME") : 0) ) );  // 出停・忌引日数
        svf.VrsOutn("ABROAD", i, rs.getString("TRANSFER_DATE") );  // 留学中の授業日数
        svf.VrsOutn("PRESENT", i, rs.getString("MLESSON") );  // 出席しなければならない日数
        svf.VrsOutn("ABSENCE", i, rs.getString("SICK") );  // 欠席日数
        svf.VrsOutn("ATTEND", i, rs.getString("PRESENT") );  // 出席日数

        if (parameter._isprintLateEarly) {
            svf.VrsOutn("LATE", i, rs.getString("LATE"));
            svf.VrsOutn("LEAVE", i, rs.getString("EARLY"));
        }
    }


    /** 
     * 委員会・クラブ活動を印字するメソッドを実行します。
     */
    private void printSvfCommitAndClub (
            final Vrw32alpWrap svf,
            final PreparedStatement ps,
            final String schregno
    ) {
        try {
            int pp = 0;
            ps.setString( ++pp, schregno );
            ps.setString( ++pp, schregno );
            ResultSet rs = ps.executeQuery();
            int i = 0;
            int div = 0;
            while (rs.next()) {
                if (div != rs.getInt("DIV")) { i = 0; }
                if (1 == rs.getInt("DIV")) {
                    printSvfCommitOut(svf, rs, ++i);
                } else {
                    printSvfClubOut(svf, rs, ++i);
                }
                div = rs.getInt("DIV");
            }
            rs.close();
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
    }


    /** 
     * 委員会明細を印字します。
     */
    private void printSvfCommitOut (
            final Vrw32alpWrap svf,
            final ResultSet rs,
            final int i
    ) throws SQLException {
        svf.VrsOutn( "COMMITTEE",  i,  rs.getString("NAME") );
    }


    /** 
     * クラブ活動明細を印字します。
     */
    private void printSvfClubOut (
            final Vrw32alpWrap svf,
            final ResultSet rs,
            final int i
    ) throws SQLException {
        svf.VrsOutn( "CLUB",  i,  rs.getString("NAME") );
    }


    /** 
     * <pre>
     * 生徒の学籍等の情報および総合的な学習の時間の所見・通信欄を取得するＳＱＬ文を戻します。
     * ・指定された生徒全員を対象とします。
     * </pre>
     */
    String prestatementRegd (final Param parameter) {
        StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append(    "SELECT  T1.SCHREGNO, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD ");
        stb.append(    "FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
        stb.append(    "WHERE   T1.YEAR = '" + parameter._year + "' ");
        stb.append(        "AND T1.SEMESTER = '"+parameter._semester+"' ");
        stb.append(        "AND T1.YEAR = T2.YEAR ");
        stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
        stb.append(        "AND T1.GRADE||T1.HR_CLASS = '" + parameter._grade_hr_class + "' ");
        stb.append(        "AND T1.SCHREGNO IN" + parameter._schnoString + " ");
        //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合 
        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append(                           "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + parameter._date + "' THEN T2.EDATE ELSE '" + parameter._date + "' END) ");
        stb.append(                             "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + parameter._date + "' THEN T2.EDATE ELSE '" + parameter._date + "' END)) ) ");
//        //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
//        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
//        stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//        stb.append(                           "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + parameter._date + "' THEN T2.EDATE ELSE '" + parameter._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        //                      異動者チェック：休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append(                           "AND S1.TRANSFERCD IN ('2') AND CASE WHEN T2.EDATE < '" + parameter._date + "' THEN T2.EDATE ELSE '" + parameter._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append(    ") ");

        //メイン表
        stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T2.HR_NAME, ");
        stb.append(        "T5.NAME, T3.COURSENAME, T4.MAJORNAME, ");
        stb.append(        "T6.GUARD_NAME, T6.GUARD_ADDR1, T6.GUARD_ADDR2, T6.GUARD_ZIPCD, ");
        stb.append(        "T7.TOTALSTUDYTIME, T7.SPECIALACTREMARK, T7.COMMUNICATION  ");
        stb.append(        ",T8.COMMUNICATION AS COMMUNICATION1 ");
        stb.append(        ",T9.COMMUNICATION AS COMMUNICATION2 ");
        stb.append(        ",T10.COMMUNICATION AS COMMUNICATION3 ");
        stb.append("FROM    SCHNO_A T1 ");
        stb.append(        "INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
        stb.append(        "INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = '" + parameter._year + "' AND ");
        stb.append(                                          "T2.SEMESTER = T1.SEMESTER AND ");
        stb.append(                                          "T2.GRADE || T2.HR_CLASS = '" + parameter._grade_hr_class + "' ");
        stb.append(        "LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
        stb.append(        "LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
        stb.append(        "LEFT JOIN GUARDIAN_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO ");
        stb.append(        "LEFT JOIN HREPORTREMARK_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO ");
        stb.append(                                      "AND T7.YEAR = '" + parameter._year + "' ");
        stb.append(                                      "AND T7.SEMESTER = '" + parameter._semester + "' ");
        stb.append(        "LEFT JOIN HREPORTREMARK_DAT T8 ON T8.SCHREGNO = T1.SCHREGNO ");
        stb.append(                                      "AND T8.YEAR = '" + parameter._year + "' ");
        stb.append(                                      "AND T8.SEMESTER = '1' ");
        stb.append(        "LEFT JOIN HREPORTREMARK_DAT T9 ON T9.SCHREGNO = T1.SCHREGNO ");
        stb.append(                                      "AND T9.YEAR = '" + parameter._year + "' ");
        stb.append(                                      "AND T9.SEMESTER = '2' ");
        stb.append(        "LEFT JOIN HREPORTREMARK_DAT T10 ON T10.SCHREGNO = T1.SCHREGNO ");
        stb.append(                                      "AND T10.YEAR = '" + parameter._year + "' ");
        stb.append(                                      "AND T10.SEMESTER = '3' ");
        stb.append("ORDER BY ATTENDNO");
        return stb.toString();
    }


    /** 
     * <pre>
     * 成績明細を取得するＳＱＬ文を戻します。
     * ・生徒個人を対象とします。
     * 
     * 2005/09/12 
     * 2005/09/26 科目読替処理追加
     * 2005/09/30 評価の欠課時数情報('-','=')を追加
     * 2005/12/22 学年評定を記号対応
     * </pre>
     */
    private String prestatementSubclass (final Param parameter) {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        // 科目別単位数の表
        stb.append(" SUBCLASS_CREDITS AS(");
        stb.append("   SELECT SUBCLASSCD, CREDITS, L1.NAMESPARE1 ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , CLASSCD ");
            stb.append("   , SCHOOL_KIND ");
            stb.append("   , CURRICULUM_CD ");
        }
        stb.append("   FROM   CREDIT_MST T1");
        stb.append("          LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z011' ");
        stb.append("               AND L1.NAMECD2 = T1.REQUIRE_FLG ");
        stb.append("        , (SELECT  T3.GRADE, T3.COURSECD, T3.MAJORCD, T3.COURSECODE");
        stb.append("           FROM    SCHREG_REGD_DAT T3");
        stb.append("           WHERE   T3.SCHREGNO = ?");
        stb.append("               AND T3.YEAR = '" + parameter._year + "'");
        stb.append("               AND T3.GRADE = '" + parameter._grade_hr_class.substring(0, 2) + "'");
        stb.append("               AND T3.HR_CLASS = '" + parameter._grade_hr_class.substring(2) + "'");
        stb.append("               AND T3.SEMESTER = (SELECT  MAX(SEMESTER)");
        stb.append("                                  FROM    SCHREG_REGD_DAT T4");
        stb.append("                                  WHERE   T4.YEAR = '" + parameter._year + "'");
        stb.append("                                      AND T4.SEMESTER <= '" + parameter._semester + "'");
        stb.append("                                      AND T4.SCHREGNO = T3.SCHREGNO)");
        stb.append("          )T2 ");
        stb.append("   WHERE T1.YEAR = '" + parameter._year + "'");
        stb.append("     AND T1.GRADE = T2.GRADE");
        stb.append("     AND T1.COURSECD = T2.COURSECD");
        stb.append("     AND T1.MAJORCD = T2.MAJORCD");
        stb.append("     AND T1.COURSECODE = T2.COURSECODE");
        stb.append(" ) ");

        // 合併先科目の表
        stb.append(" ,COMBINED_SUBCLASSCD AS(");
        stb.append("   SELECT COMBINED_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , COMBINED_CLASSCD AS CLASSCD ");
            stb.append("   , COMBINED_SCHOOL_KIND AS SCHOOL_KIND ");
            stb.append("   , COMBINED_CURRICULUM_CD AS CURRICULUM_CD ");
        }
        stb.append("   FROM   SUBCLASS_REPLACE_COMBINED_DAT");
        stb.append("   WHERE  YEAR = '" + parameter._year + "' ");
        stb.append("   GROUP BY COMBINED_SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , COMBINED_CLASSCD ");
            stb.append("   , COMBINED_SCHOOL_KIND ");
            stb.append("   , COMBINED_CURRICULUM_CD ");
        }
        stb.append(" )");
        
        // 合併元科目の表
        stb.append(" ,ATTEND_SUBCLASSCD AS(");
        stb.append("   SELECT ATTEND_SUBCLASSCD AS SUBCLASSCD, ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   ATTEND_CLASSCD AS CLASSCD, ");
            stb.append("   ATTEND_SCHOOL_KIND AS SCHOOL_KIND, ");
            stb.append("   ATTEND_CURRICULUM_CD AS CURRICULUM_CD, ");
        }
        stb.append("   MAX(PRINT_FLG1) AS PRINT_FLG, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG, ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   MAX(COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD) AS COMBINED_SUBCLASSCD ");
        } else {
            stb.append("   MAX(COMBINED_SUBCLASSCD) AS COMBINED_SUBCLASSCD ");
        }
        stb.append("   FROM   SUBCLASS_REPLACE_COMBINED_DAT");
        stb.append("   WHERE  YEAR = '" + parameter._year + "' ");
        stb.append("   GROUP BY ATTEND_SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , ATTEND_CLASSCD ");
            stb.append("   , ATTEND_SCHOOL_KIND ");
            stb.append("   , ATTEND_CURRICULUM_CD ");
        }
        stb.append(" )");
        
        // 講座の表
        stb.append(", CHAIR_A AS(");
        stb.append("   SELECT  T2.SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , T2.CLASSCD ");
            stb.append("   , T2.SCHOOL_KIND ");
            stb.append("   , T2.CURRICULUM_CD ");
        }
        stb.append("   FROM    CHAIR_STD_DAT T1, CHAIR_DAT T2");
        stb.append("   WHERE   T1.SCHREGNO = ?");
        stb.append("       AND T1.YEAR = '" + parameter._year + "'");
        stb.append("       AND T1.SEMESTER <= '" + parameter._semester + "'");
        stb.append("       AND T2.YEAR  = '" + parameter._year + "'");
        stb.append("       AND T2.SEMESTER <= '" + parameter._semester + "'");
        stb.append("       AND T2.SEMESTER = T1.SEMESTER");
        stb.append("       AND T2.CHAIRCD = T1.CHAIRCD");
        stb.append("       AND (SUBSTR(T2.SUBCLASSCD,1,2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(T2.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "')");
        stb.append("   GROUP BY T2.SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , T2.CLASSCD ");
            stb.append("   , T2.SCHOOL_KIND ");
            stb.append("   , T2.CURRICULUM_CD ");
        }
        stb.append(" )");
        
        // 合併先科目の単位
        stb.append(" ,COMBINED_CREDITS AS(");
        stb.append("   SELECT T1.COMBINED_SUBCLASSCD ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , T1.COMBINED_CLASSCD ");
            stb.append("   , T1.COMBINED_SCHOOL_KIND ");
            stb.append("   , T1.COMBINED_CURRICULUM_CD ");
        }
        stb.append("   , SUM(CREDITS) AS COMBINED_CREDITS ");
        stb.append("   FROM   SUBCLASS_REPLACE_COMBINED_DAT T1");
        stb.append("   INNER JOIN CHAIR_A T2 ON T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("      AND T2.SCHOOL_KIND = T1.ATTEND_SCHOOL_KIND ");
            stb.append("      AND T2.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ");
            stb.append("      AND T2.CLASSCD = T1.ATTEND_CLASSCD ");
        }
        stb.append("   INNER JOIN SUBCLASS_CREDITS T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("      AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("      AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("      AND T3.CLASSCD = T2.CLASSCD ");
        }
        stb.append("   WHERE  YEAR = '" + parameter._year + "' AND CALCULATE_CREDIT_FLG = '2' ");
        stb.append("   GROUP BY T1.COMBINED_SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , T1.COMBINED_CLASSCD ");
            stb.append("   , T1.COMBINED_SCHOOL_KIND ");
            stb.append("   , T1.COMBINED_CURRICULUM_CD ");
        }
        stb.append(" )");


        // 成績データの表
        stb.append(", RECORD AS(");
        stb.append("   SELECT  SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , CLASSCD ");
            stb.append("   , SCHOOL_KIND ");
            stb.append("   , CURRICULUM_CD ");
        }
        stb.append("         , COMP_CREDIT, GET_CREDIT, ADD_CREDIT");
        stb.append("         , SEM1_INTR_SCORE, SEM1_INTR_VALUE, SEM1_TERM_SCORE, SEM1_TERM_VALUE");
        stb.append("         , CASE WHEN SEM1_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM1_VALUE)) ELSE SEM1_VALUE_DI END AS SEM1_VALUE");
        stb.append("         , SEM2_INTR_SCORE, SEM2_INTR_VALUE, SEM2_TERM_SCORE, SEM2_TERM_VALUE");
        stb.append("         , CASE WHEN SEM2_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM2_VALUE)) ELSE SEM2_VALUE_DI END AS SEM2_VALUE");
        stb.append("         , SEM3_INTR_SCORE, SEM3_INTR_VALUE, SEM3_TERM_SCORE");
        stb.append("         , SEM3_TERM_VALUE");
        stb.append("         , CASE WHEN SEM3_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM3_VALUE)) ELSE SEM3_VALUE_DI END AS SEM3_VALUE");
        stb.append("         , CASE WHEN GRAD_VALUE IS NOT NULL THEN RTRIM(CHAR(GRAD_VALUE)) ELSE GRAD_VALUE_DI END AS GRAD_VALUE");
        stb.append("   FROM    RECORD_DAT");
        stb.append("   WHERE   SCHREGNO = ?");
        stb.append("       AND YEAR = '" + parameter._year + "'");
        stb.append("       AND (SUBSTR(SUBCLASSCD,1,2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "')");
        stb.append(" )");

        // テスト項目マスタの集計フラグ
        stb.append(" , TEST_COUNTFLG AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         '2' AS DATADIV ");
        stb.append("     FROM ");
        stb.append("         SCH_CHR_TEST T1, ");
        stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
        stb.append("     WHERE ");
        stb.append("         T2.YEAR       = T1.YEAR ");
        stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
        stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("         AND T2.COUNTFLG   = '0' ");
        stb.append(" ) ");

        // 時間割データの表
        stb.append(", T_SCH_CHR_DAT AS(");
        stb.append(    "SELECT  T0.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.CHAIRCD, T1.DATADIV, T5.DI_CD, (CASE WHEN T4.SCHREGNO IS NOT NULL THEN '1' ELSE '0' END) AS IS_OFFDAYS ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , T2.CLASSCD ");
            stb.append("   , T2.SCHOOL_KIND ");
            stb.append("   , T2.CURRICULUM_CD ");
        }
        stb.append(    "FROM    SCH_CHR_DAT T1 ");
        stb.append(    "INNER JOIN SEMESTER_MST T3 ON T3.YEAR = T1.YEAR ");
        stb.append(        "AND T3.SEMESTER <> '9' ");
        stb.append(        "AND T1.EXECUTEDATE BETWEEN T3.SDATE AND T3.EDATE ");
        stb.append(    "INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append(        "AND T2.SEMESTER = T3.SEMESTER ");
        stb.append(        "AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append(    "INNER JOIN CHAIR_STD_DAT T0 ON T0.YEAR = T1.YEAR ");
        stb.append(        "AND T0.SEMESTER = T2.SEMESTER ");
        stb.append(        "AND T0.CHAIRCD = T2.CHAIRCD ");
        stb.append(        "AND T1.EXECUTEDATE BETWEEN T0.APPDATE AND T0.APPENDDATE ");
        stb.append(    "LEFT JOIN ATTEND_DAT T5 ON T5.SCHREGNO = T0.SCHREGNO ");
        stb.append(        "AND T1.EXECUTEDATE = T5.ATTENDDATE ");
        stb.append(        "AND T1.PERIODCD = T5.PERIODCD ");
        stb.append(        "AND T1.CHAIRCD = T5.CHAIRCD ");
        stb.append(    "LEFT JOIN SCHREG_TRANSFER_DAT T4 ON T4.SCHREGNO = T0.SCHREGNO ");
        stb.append(        "AND T1.EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ");
        stb.append(        "AND T4.TRANSFERCD = '2' ");
        stb.append(    "WHERE   T1.YEAR = '" +  parameter._year + "' ");
        stb.append(        "AND T1.EXECUTEDATE BETWEEN '" + parameter._attend_date + "' AND '" + parameter._date + "' ");
        stb.append(        "AND T0.SCHREGNO = ? ");
        stb.append(        "AND T3.SEMESTER <= '" + parameter._semester + "' ");
        //                      出欠カウントフラグのチェック
        if( parameter.definecode.useschchrcountflg ){
            stb.append(    "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
            stb.append(                    "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE ");
            stb.append(                        "AND T4.PERIODCD = T1.PERIODCD ");
            stb.append(                        "AND T4.CHAIRCD = T1.CHAIRCD ");
            stb.append(                        "AND T1.DATADIV IN ('0', '1') ");
            stb.append(                        "AND T4.GRADE = '" + parameter._grade_hr_class.substring(0, 2) + "' ");
            stb.append(                        "AND T4.HR_CLASS = '" + parameter._grade_hr_class.substring(2) + "' ");
            stb.append(                        "AND T4.COUNTFLG = '0') ");
            stb.append("    AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
            stb.append("                       WHERE ");
            stb.append("                           TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
            stb.append("                           AND TEST.PERIODCD = T1.PERIODCD ");
            stb.append("                           AND TEST.CHAIRCD  = T1.CHAIRCD ");
            stb.append("                           AND TEST.DATADIV  = T1.DATADIV) ");
        }
        //                      学籍不在日を除外
        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ");
        stb.append(                       "WHERE   T6.SCHREGNO = T0.SCHREGNO ");
        stb.append(                           "AND (( T6.ENT_DIV IN('4','5') AND T1.EXECUTEDATE < T6.ENT_DATE ) ");
        stb.append(                             "OR ( T6.GRD_DIV IN('2','3') AND T1.EXECUTEDATE > T6.GRD_DATE )) ) ");
        // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + parameter._year + "' ");
        stb.append("                             AND ATDD.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T0.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND ATDD.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        stb.append("                  ) ");
        // 勤怠コード'28'は時間割にカウントしない
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + parameter._year + "' ");
        stb.append("                             AND ATDD.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T0.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND ATDD.REP_DI_CD = '28' ");
        stb.append("                  ) ");
        //                      留学日、休学日を除外
        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ");
        stb.append(                       "WHERE   T7.SCHREGNO = T0.SCHREGNO ");
        stb.append(                           "AND T7.TRANSFERCD <> '2' AND T1.EXECUTEDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ");
        stb.append(") ");

        // 生徒・科目・学期別欠課集計の表（出欠データと集計テーブルを合算）
        stb.append(", SCH_ATTEND_SUM AS(");
        stb.append("    SELECT  T1.SUBCLASSCD, T1.SEMESTER ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , T1.CLASSCD ");
            stb.append("   , T1.SCHOOL_KIND ");
            stb.append("   , T1.CURRICULUM_CD ");
        }
        stb.append("           ,SUM(CASE WHEN (CASE WHEN ATDD.REP_DI_CD IN ('29','30','31') THEN VALUE(ATDD.ATSUB_REPL_DI_CD, ATDD.REP_DI_CD) ELSE ATDD.REP_DI_CD END) IN('4','5','6','14','11','12','13'");
        if ("1".equals(_knjSchoolMst._subAbsent)) {
            stb.append(          ",'1','8'");
        }
        if ("1".equals(_knjSchoolMst._subSuspend)) {
            stb.append(          ",'2','9'");
        }
        if ("1".equals(_knjSchoolMst._subMourning)) {
            stb.append(          ",'3','10'");
        }
        if ("true".equals(parameter._useVirus)) {
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append(          ",'19','20'");
            }
        }
        if ("true".equals(parameter._useKoudome)) {
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append(          ",'25','26'");
            }
        }
        stb.append(                ") ");
        if ("1".equals(_knjSchoolMst._subOffDays)) {
            stb.append(            "OR (IS_OFFDAYS = '1')");
        }
        stb.append(            " THEN 1 ELSE 0 END)AS ABSENT1 ");
        stb.append("           ,SUM(CASE WHEN ATDD.DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(ATDD.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
        stb.append("           ,SUM(CASE WHEN ATDD.REP_DI_CD IN('15','23','24') THEN SMALLINT(VALUE(ATDD.MULTIPLY, '1')) ELSE 0 END)AS LATE ");
        stb.append("           ,SUM(CASE WHEN ATDD.REP_DI_CD IN('16') THEN SMALLINT(VALUE(ATDD.MULTIPLY, '1')) ELSE 0 END)AS EARLY ");
        stb.append("    FROM    T_SCH_CHR_DAT T1 ");
        stb.append("            LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + parameter._year + "' AND ATDD.DI_CD = T1.DI_CD ");
        stb.append("    GROUP BY T1.SEMESTER, T1.SUBCLASSCD ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , T1.CLASSCD ");
            stb.append("   , T1.SCHOOL_KIND ");
            stb.append("   , T1.CURRICULUM_CD ");
        }

        stb.append("    UNION ALL ");
        stb.append("    SELECT  T1.SUBCLASSCD, SEMESTER ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , T1.CLASSCD ");
            stb.append("   , T1.SCHOOL_KIND ");
            stb.append("   , T1.CURRICULUM_CD ");
        }
        stb.append(           ",SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0)");
        if ("1".equals(_knjSchoolMst._subAbsent)) {
            stb.append(          "+ VALUE(ABSENT,0)");
        }
        if ("1".equals(_knjSchoolMst._subSuspend)) {
            stb.append(          "+ VALUE(SUSPEND,0)");
        }
        if ("1".equals(_knjSchoolMst._subMourning)) {
            stb.append(          "+ VALUE(MOURNING,0)");
        }
        if ("1".equals(_knjSchoolMst._subOffDays)) {
            stb.append(          "+ VALUE(OFFDAYS,0)");
        }
        if ("true".equals(parameter._useVirus)) {
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append(          "+ VALUE(VIRUS,0)");
            }
        }
        if ("true".equals(parameter._useKoudome)) {
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append(          "+ VALUE(KOUDOME,0)");
            }
        }
        stb.append(               ") AS ABSENT1 ");
        
        stb.append("           ,SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
        stb.append("           ,SUM(VALUE(LATE,0)) AS LATE ");
        stb.append("           ,SUM(VALUE(EARLY,0)) AS EARLY ");
        stb.append("    FROM    ATTEND_SUBCLASS_DAT T1 ");
        stb.append("    WHERE   SCHREGNO = ? ");
        stb.append("        AND YEAR = '" + parameter._year + "' ");
        stb.append("        AND SEMESTER <= '" + parameter._semester + "' ");
        stb.append("        AND (CASE WHEN INT(T1.MONTH) < 4 THEN RTRIM(CHAR(INT(T1.SEMESTER) + 1 )) ELSE T1.SEMESTER END )||T1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue (parameter._attend_month) + "' ");   //--NO004 NO007
        stb.append("    GROUP BY T1.SEMESTER, T1.SUBCLASSCD ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , T1.CLASSCD ");
            stb.append("   , T1.SCHOOL_KIND ");
            stb.append("   , T1.CURRICULUM_CD ");
        }
        stb.append(" ) ");

        // ペナルティー欠課を加味した欠課集計の表（出欠データと集計テーブルを合算）
        if (parameter.definecode.absent_cov == 1 || parameter.definecode.absent_cov == 3) {
            //学期でペナルティ欠課を算出する場合
            stb.append(", ATTEND_B AS(");
            stb.append(       "SELECT  SUBCLASSCD ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("   , CLASSCD ");
                stb.append("   , SCHOOL_KIND ");
                stb.append("   , CURRICULUM_CD ");
            }
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN LATE ELSE NULL END),0) AS LATE_SEM1 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN LATE ELSE NULL END),0) AS LATE_SEM2 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN LATE ELSE NULL END),0) AS LATE_SEM3 ");
            stb.append(              ",VALUE(SUM(LATE),0) AS LATE_SEM9 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN EARLY ELSE NULL END),0) AS EARLY_SEM1 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN EARLY ELSE NULL END),0) AS EARLY_SEM2 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN EARLY ELSE NULL END),0) AS EARLY_SEM3 ");
            stb.append(              ",VALUE(SUM(EARLY),0) AS EARLY_SEM9 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN KETUJI ELSE NULL END),0) AS KETUJI_SEM1 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN KETUJI ELSE NULL END),0) AS KETUJI_SEM2 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN KETUJI ELSE NULL END),0) AS KETUJI_SEM3 ");
            stb.append(              ",VALUE(SUM(KETUJI),0) AS KETUJI_SEM9 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM1 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM2 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM3 ");
            stb.append(              ",VALUE(SUM(ABSENT),0) AS ABSENT_SEM9 ");
            stb.append(       "FROM(   SELECT  SUBCLASSCD, SEMESTER ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("   , CLASSCD ");
                stb.append("   , SCHOOL_KIND ");
                stb.append("   , CURRICULUM_CD ");
            }
            stb.append(                      ",VALUE(SUM(LATE),0) AS LATE ");
            stb.append(                      ",VALUE(SUM(EARLY),0) AS EARLY ");
            stb.append(                      ",VALUE(SUM(ABSENT1),0) AS KETUJI ");
            stb.append(                      ",VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + parameter.definecode.absent_cov_late + " AS ABSENT ");
            stb.append(               "FROM    SCH_ATTEND_SUM T1 ");
            stb.append(               "GROUP BY SUBCLASSCD, SEMESTER ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("   , CLASSCD ");
                stb.append("   , SCHOOL_KIND ");
                stb.append("   , CURRICULUM_CD ");
            }
            stb.append(           ")T1 ");
            stb.append(       "GROUP BY SUBCLASSCD ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("   , CLASSCD ");
                stb.append("   , SCHOOL_KIND ");
                stb.append("   , CURRICULUM_CD ");
            }
            stb.append(") ");
        } else if (parameter.definecode.absent_cov == 2 || parameter.definecode.absent_cov == 4) {
            //通年でペナルティ欠課を算出する場合 
            //05/09/28Modify 学期の欠課時数は学期別で換算したペナルティ欠課を加算、学年の欠課時数は年間で換算する
            stb.append(", ATTEND_B AS(");
            stb.append(       "SELECT  T1.SUBCLASSCD, T1.LATE_SEM9, T1.EARLY_SEM9, T1.KETUJI_SEM9, T1.ABSENT_SEM9 ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("   , T1.CLASSCD ");
                stb.append("   , T1.SCHOOL_KIND ");
                stb.append("   , T1.CURRICULUM_CD ");
            }
            stb.append(              ",T2.LATE_SEM1, T2.LATE_SEM2, T2.LATE_SEM3 ");
            stb.append(              ",T2.EARLY_SEM1, T2.EARLY_SEM2, T2.EARLY_SEM3 ");
            stb.append(              ",T2.KETUJI_SEM1, T2.KETUJI_SEM2, T2.KETUJI_SEM3 ");
            stb.append(              ",T2.ABSENT_SEM1, T2.ABSENT_SEM2, T2.ABSENT_SEM3 ");
            stb.append(       "FROM (");
            stb.append(            "SELECT  SUBCLASSCD ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("   , CLASSCD ");
                stb.append("   , SCHOOL_KIND ");
                stb.append("   , CURRICULUM_CD ");
            }
            stb.append(                   ",VALUE(SUM(LATE),0) AS LATE_SEM9 ");
            stb.append(                   ",VALUE(SUM(EARLY),0) AS EARLY_SEM9 ");
            stb.append(                   ",VALUE(SUM(ABSENT1),0) AS KETUJI_SEM9 ");
            stb.append(                   ",VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + parameter.definecode.absent_cov_late + " AS ABSENT_SEM9 ");
            stb.append(            "FROM    SCH_ATTEND_SUM T1 ");
            stb.append(            "GROUP BY SUBCLASSCD ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("   , CLASSCD ");
                stb.append("   , SCHOOL_KIND ");
                stb.append("   , CURRICULUM_CD ");
            }
            stb.append(       ")T1, (");
            stb.append(            "SELECT  SUBCLASSCD ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("   , CLASSCD ");
                stb.append("   , SCHOOL_KIND ");
                stb.append("   , CURRICULUM_CD ");
            }
            stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN LATE ELSE NULL END),0) AS LATE_SEM1 ");
            stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN LATE ELSE NULL END),0) AS LATE_SEM2 ");
            stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN LATE ELSE NULL END),0) AS LATE_SEM3 ");
            stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN EARLY ELSE NULL END),0) AS EARLY_SEM1 ");
            stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN EARLY ELSE NULL END),0) AS EARLY_SEM2 ");
            stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN EARLY ELSE NULL END),0) AS EARLY_SEM3 ");
            stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN KETUJI ELSE NULL END),0) AS KETUJI_SEM1 ");
            stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN KETUJI ELSE NULL END),0) AS KETUJI_SEM2 ");
            stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN KETUJI ELSE NULL END),0) AS KETUJI_SEM3 ");
            stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM1 ");
            stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM2 ");
            stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM3 ");
            stb.append(            "FROM(   SELECT  SUBCLASSCD, SEMESTER ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("   , CLASSCD ");
                stb.append("   , SCHOOL_KIND ");
                stb.append("   , CURRICULUM_CD ");
            }
            stb.append(                           ",VALUE(SUM(LATE),0) AS LATE ");
            stb.append(                           ",VALUE(SUM(EARLY),0) AS EARLY ");
            stb.append(                           ",VALUE(SUM(ABSENT1),0) AS KETUJI ");
            stb.append(                           ",VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + parameter.definecode.absent_cov_late + " AS ABSENT ");
            stb.append(                    "FROM    SCH_ATTEND_SUM T1 ");
            stb.append(                    "GROUP BY SUBCLASSCD, SEMESTER ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("   , CLASSCD ");
                stb.append("   , SCHOOL_KIND ");
                stb.append("   , CURRICULUM_CD ");
            }
            stb.append(                ")T1 ");
            stb.append(            "GROUP BY SUBCLASSCD ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("   , CLASSCD ");
                stb.append("   , SCHOOL_KIND ");
                stb.append("   , CURRICULUM_CD ");
            }
            stb.append(       ")T2 ");
            stb.append(       "WHERE T1.SUBCLASSCD = T2.SUBCLASSCD ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("   AND T1.CLASSCD = T2.CLASSCD ");
                stb.append("   AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("   AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append(") ");
        } else{
            //ペナルティ欠課なしの場合
            stb.append(", ATTEND_B AS(");
            stb.append(       "SELECT  SUBCLASSCD ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("   , CLASSCD ");
                stb.append("   , SCHOOL_KIND ");
                stb.append("   , CURRICULUM_CD ");
            }
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN LATE ELSE NULL END),0) AS LATE_SEM1 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN LATE ELSE NULL END),0) AS LATE_SEM2 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN LATE ELSE NULL END),0) AS LATE_SEM3 ");
            stb.append(              ",VALUE(SUM(LATE),0) AS LATE_SEM9 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN EARLY ELSE NULL END),0) AS EARLY_SEM1 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN EARLY ELSE NULL END),0) AS EARLY_SEM2 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN EARLY ELSE NULL END),0) AS EARLY_SEM3 ");
            stb.append(              ",VALUE(SUM(EARLY),0) AS EARLY_SEM9 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT1 ELSE NULL END),0) AS KETUJI_SEM1 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT1 ELSE NULL END),0) AS KETUJI_SEM2 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT1 ELSE NULL END),0) AS KETUJI_SEM3 ");
            stb.append(              ",VALUE(SUM(ABSENT1),0) AS KETUJI_SEM9 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM1 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM2 ");
            stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM3 ");
            stb.append(              ",VALUE(SUM(ABSENT1),0) AS ABSENT_SEM9 ");
            stb.append(       "FROM    SCH_ATTEND_SUM T1 ");
            stb.append(       "GROUP BY SUBCLASSCD ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("   , CLASSCD ");
                stb.append("   , SCHOOL_KIND ");
                stb.append("   , CURRICULUM_CD ");
            }
            stb.append(") ");
        }

        // 合併科目のペナルティー欠課を加味した欠課集計の表
        stb.append(", ATTEND_B_REPLACE AS(");
        stb.append("   SELECT  T1.COMBINED_SUBCLASSCD AS SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , T1.COMBINED_CLASSCD AS CLASSCD ");
            stb.append("   , T1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND ");
            stb.append("   , T1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD ");
        }
        stb.append("         , SUM(T2.LATE_SEM1) AS LATE_SEM1");
        stb.append("         , SUM(T2.LATE_SEM2) AS LATE_SEM2");
        stb.append("         , SUM(T2.LATE_SEM3) AS LATE_SEM3");
        stb.append("         , SUM(T2.LATE_SEM9) AS LATE_SEM9");
        stb.append("         , SUM(T2.EARLY_SEM1) AS EARLY_SEM1");
        stb.append("         , SUM(T2.EARLY_SEM2) AS EARLY_SEM2");
        stb.append("         , SUM(T2.EARLY_SEM3) AS EARLY_SEM3");
        stb.append("         , SUM(T2.EARLY_SEM9) AS EARLY_SEM9");
        stb.append("         , SUM(T2.KETUJI_SEM1) AS KETUJI_SEM1");
        stb.append("         , SUM(T2.KETUJI_SEM2) AS KETUJI_SEM2");
        stb.append("         , SUM(T2.KETUJI_SEM3) AS KETUJI_SEM3");
        stb.append("         , SUM(T2.KETUJI_SEM9) AS KETUJI_SEM9");
        stb.append("         , SUM(T2.ABSENT_SEM1) AS ABSENT_SEM1");
        stb.append("         , SUM(T2.ABSENT_SEM2) AS ABSENT_SEM2");
        stb.append("         , SUM(T2.ABSENT_SEM3) AS ABSENT_SEM3");
        stb.append("         , SUM(T2.ABSENT_SEM9) AS ABSENT_SEM9");
        stb.append("   FROM    SUBCLASS_REPLACE_COMBINED_DAT T1, ATTEND_B T2");
        stb.append("   WHERE   T1.YEAR = '" + parameter._year + "'");
        stb.append("       AND T1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   AND T1.ATTEND_CLASSCD = T2.CLASSCD ");
            stb.append("   AND T1.ATTEND_SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("   AND T1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        if (_isNoPrintMoto && _isMirishuu) {
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("       AND (T1.ATTEND_CLASSCD || T1.ATTEND_SCHOOL_KIND || T1.ATTEND_CURRICULUM_CD || T1.ATTEND_SUBCLASSCD,T1.CALCULATE_CREDIT_FLG) not in (SELECT CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD,'2' FROM RECORD WHERE COMP_CREDIT = 0 or COMP_CREDIT is null)");
            } else {
                stb.append("       AND (T1.ATTEND_SUBCLASSCD,T1.CALCULATE_CREDIT_FLG) not in (SELECT SUBCLASSCD,'2' FROM RECORD WHERE COMP_CREDIT = 0 or COMP_CREDIT is null)");
            }
        }
        stb.append("   GROUP BY T1.COMBINED_SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , T1.COMBINED_CLASSCD ");
            stb.append("   , T1.COMBINED_SCHOOL_KIND ");
            stb.append("   , T1.COMBINED_CURRICULUM_CD ");
        }
        stb.append(" )");

        //科目数カウント
        stb.append(", SUBCLASSNUM AS(");
        stb.append("   SELECT  SUM(CASE WHEN SUBSTR(S1.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "' OR T1.NAMECD2 IS NOT NULL THEN 1 ELSE NULL END) AS NUM90");
        stb.append("         , SUM(CASE WHEN SUBSTR(S1.SUBCLASSCD,1,2) != '" + KNJDefineSchool.subject_T + "' AND T1.NAMECD2 IS NULL THEN 1 ELSE NULL END) AS NUMTOTAL");
        stb.append("   FROM    CHAIR_A S1");
        if ("1".equals(parameter._useCurriculumcd) && "1".equals(parameter._useClassDetailDat)) {
            stb.append(" LEFT JOIN (SELECT CLASSCD || '-' || SCHOOL_KIND AS NAMECD2 FROM CLASS_DETAIL_DAT N1 WHERE YEAR = '" + parameter._year + "' AND CLASS_SEQ = '003') T1 ON T1.NAMECD2 = S1.CLASSCD || '-' || S1.SCHOOL_KIND ");
        } else {
            stb.append(" LEFT JOIN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1='D008') T1 ON T1.NAMECD2 = SUBSTR(S1.SUBCLASSCD,1,2)");
        }
        stb.append(" )");

        //メイン表
        stb.append(" SELECT ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD ");
        } else {
            stb.append("   T2.SUBCLASSCD ");
        }
        stb.append("       , T7.CLASSNAME, VALUE(T4.SUBCLASSORDERNAME2,T4.SUBCLASSNAME) AS SUBCLASSNAME");
        stb.append("       , T6.CREDITS, T6.NAMESPARE1, T10.COMBINED_CREDITS ");
        stb.append("       , T1.COMP_CREDIT, T1.GET_CREDIT, T1.ADD_CREDIT");
        stb.append("       , T1.SEM1_INTR_SCORE, T1.SEM1_INTR_VALUE, T1.SEM1_TERM_SCORE, T1.SEM1_TERM_VALUE, T1.SEM1_VALUE");
        stb.append("       , T1.SEM2_INTR_SCORE, T1.SEM2_INTR_VALUE, T1.SEM2_TERM_SCORE, T1.SEM2_TERM_VALUE, T1.SEM2_VALUE");
        stb.append("       , T1.SEM3_INTR_SCORE, T1.SEM3_INTR_VALUE, T1.SEM3_TERM_SCORE, T1.SEM3_TERM_VALUE, T1.SEM3_VALUE");
                            // GRAD_VALUEの記号対応 <= RECORD表でGRAD_VALUEはCHARとしている
        stb.append("       , CASE WHEN SUBSTR(T1.SUBCLASSCD,1,2) <> '" + KNJDefineSchool.subject_T + "' THEN T1.GRAD_VALUE");
        stb.append("              WHEN T1.GRAD_VALUE IN('-','=') THEN T1.GRAD_VALUE");
        stb.append("              ELSE (SELECT  S1.ASSESSMARK FROM ASSESS_MST S1");
        stb.append("                    WHERE   S1.ASSESSCD = '3'");
        stb.append("                        AND CHAR(S1.ASSESSLEVEL) = T1.GRAD_VALUE)END AS GRAD_VALUE");
        stb.append("       , T3.LATE_SEM1, T3.LATE_SEM2, T3.LATE_SEM3, T3.LATE_SEM9 AS LATE_TOTAL");
        stb.append("       , T3.EARLY_SEM1, T3.EARLY_SEM2, T3.EARLY_SEM3, T3.EARLY_SEM9 AS EARLY_TOTAL");
        stb.append("       , T3.KETUJI_SEM1, T3.KETUJI_SEM2, T3.KETUJI_SEM3, T3.KETUJI_SEM9 AS KETUJI_TOTAL");
        stb.append("       , T3.ABSENT_SEM1, T3.ABSENT_SEM2, T3.ABSENT_SEM3, T3.ABSENT_SEM9 AS ABSENT_TOTAL");
        stb.append("       , T8.LATE_SEM1 AS REPLACE_LATE_SEM1");
        stb.append("       , T8.LATE_SEM2 AS REPLACE_LATE_SEM2");
        stb.append("       , T8.LATE_SEM3 AS REPLACE_LATE_SEM3");
        stb.append("       , T8.LATE_SEM9 AS REPLACE_LATE_TOTAL");
        stb.append("       , T8.EARLY_SEM1 AS REPLACE_EARLY_SEM1");
        stb.append("       , T8.EARLY_SEM2 AS REPLACE_EARLY_SEM2");
        stb.append("       , T8.EARLY_SEM3 AS REPLACE_EARLY_SEM3");
        stb.append("       , T8.EARLY_SEM9 AS REPLACE_EARLY_TOTAL");
        stb.append("       , T8.KETUJI_SEM1 AS REPLACE_KETUJI_SEM1");
        stb.append("       , T8.KETUJI_SEM2 AS REPLACE_KETUJI_SEM2");
        stb.append("       , T8.KETUJI_SEM3 AS REPLACE_KETUJI_SEM3");
        stb.append("       , T8.KETUJI_SEM9 AS REPLACE_KETUJI_TOTAL");
        stb.append("       , T8.ABSENT_SEM1 AS REPLACE_ABSENT_SEM1");
        stb.append("       , T8.ABSENT_SEM2 AS REPLACE_ABSENT_SEM2");
        stb.append("       , T8.ABSENT_SEM3 AS REPLACE_ABSENT_SEM3");
        stb.append("       , T8.ABSENT_SEM9 AS REPLACE_ABSENT_TOTAL");
        stb.append("       , CASE WHEN T5.SUBCLASSCD IS NOT NULL AND T9.SUBCLASSCD IS NOT NULL THEN " + REPLACE_FLG2 + " ");  // 合併先かつ合併元
        stb.append("              WHEN T5.SUBCLASSCD IS NOT NULL THEN " + REPLACE_FLG9 + " ");  // 合併先
        stb.append("              WHEN T9.SUBCLASSCD IS NOT NULL THEN " + REPLACE_FLG1 + " ");  // 合併元
        stb.append("              ELSE " + REPLACE_FLG0 + " END AS REPLACEFLG");  // 通常
        stb.append("       , T9.PRINT_FLG");
        stb.append("       , N1.NAMECD2 AS NUM90_OTHER");
        stb.append("       , (SELECT NUM90 FROM SUBCLASSNUM) AS NUM90");
        stb.append("       , (SELECT NUMTOTAL FROM SUBCLASSNUM) AS NUMTOTAL");
        stb.append("       , CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN T9.COMBINED_SUBCLASSCD ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("                                             ELSE T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD END AS ORDER4"); //(4)科目コード
        } else {
            stb.append("                                             ELSE T2.SUBCLASSCD END AS ORDER4"); //(4)科目コード
        }
        stb.append("       , CASE WHEN T5.SUBCLASSCD IS NOT NULL THEN 1 WHEN T9.SUBCLASSCD IS NOT NULL THEN 2 ELSE 0 END AS ORDER5"); //(5)合併先(6)合併元・・・並べて表示
        stb.append("       , CASE WHEN T5.CALCULATE_CREDIT_FLG IS NOT NULL THEN T5.CALCULATE_CREDIT_FLG");
        stb.append("              WHEN T9.CALCULATE_CREDIT_FLG IS NOT NULL THEN T9.CALCULATE_CREDIT_FLG");
        stb.append("              ELSE NULL END AS CALCULATE_CREDIT_FLG");
        stb.append("       , VALUE(T7.SHOWORDER3, 99) AS ORDER1"); //(1)(通)表示順(教科)
        stb.append("       , T7.CLASSCD AS ORDER2"); //(2)教科コード
        stb.append("       , VALUE(T4.SHOWORDER3, 99) AS ORDER3"); //(3)(通)表示順(科目)

        stb.append(" FROM    CHAIR_A T2");
        stb.append(" LEFT JOIN RECORD T1 ON T1.SUBCLASSCD = T2.SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("                 AND T1.CLASSCD = T2.CLASSCD");
            stb.append("                 AND T1.SCHOOL_KIND = T2.SCHOOL_KIND");
            stb.append("                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD");
        }
        stb.append(" LEFT JOIN ATTEND_B T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("                 AND T3.CLASSCD = T2.CLASSCD");
            stb.append("                 AND T3.SCHOOL_KIND = T2.SCHOOL_KIND");
            stb.append("                 AND T3.CURRICULUM_CD = T2.CURRICULUM_CD");
        }
        stb.append(" LEFT JOIN ATTEND_B_REPLACE T8 ON T8.SUBCLASSCD = T2.SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("                 AND T8.CLASSCD = T2.CLASSCD");
            stb.append("                 AND T8.SCHOOL_KIND = T2.SCHOOL_KIND");
            stb.append("                 AND T8.CURRICULUM_CD = T2.CURRICULUM_CD");
        }
        stb.append(" LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T2.SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("                 AND T4.CLASSCD = T2.CLASSCD");
            stb.append("                 AND T4.SCHOOL_KIND = T2.SCHOOL_KIND");
            stb.append("                 AND T4.CURRICULUM_CD = T2.CURRICULUM_CD");
        }
        stb.append(" LEFT JOIN CLASS_MST T7 ON T7.CLASSCD = SUBSTR(T2.SUBCLASSCD,1,2)");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("                 AND T7.SCHOOL_KIND = T2.SCHOOL_KIND");
        }
        stb.append(" LEFT JOIN SUBCLASS_CREDITS T6 ON T6.SUBCLASSCD = T2.SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("                 AND T6.CLASSCD = T2.CLASSCD");
            stb.append("                 AND T6.SCHOOL_KIND = T2.SCHOOL_KIND");
            stb.append("                 AND T6.CURRICULUM_CD = T2.CURRICULUM_CD");
        }
        stb.append(" LEFT JOIN COMBINED_SUBCLASSCD T5 ON T5.SUBCLASSCD = T2.SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("                 AND T5.CLASSCD = T2.CLASSCD");
            stb.append("                 AND T5.SCHOOL_KIND = T2.SCHOOL_KIND");
            stb.append("                 AND T5.CURRICULUM_CD = T2.CURRICULUM_CD");
        }
        stb.append(" LEFT JOIN ATTEND_SUBCLASSCD T9 ON T9.SUBCLASSCD = T2.SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("                 AND T9.CLASSCD = T2.CLASSCD");
            stb.append("                 AND T9.SCHOOL_KIND = T2.SCHOOL_KIND");
            stb.append("                 AND T9.CURRICULUM_CD = T2.CURRICULUM_CD");
        }
        if ("1".equals(parameter._useCurriculumcd) && "1".equals(parameter._useClassDetailDat)) {
            stb.append(" LEFT JOIN (SELECT CLASSCD || '-' || SCHOOL_KIND AS NAMECD2 FROM CLASS_DETAIL_DAT N1 WHERE YEAR = '" + parameter._year + "' AND CLASS_SEQ = '003') N1 ON N1.NAMECD2 = T2.CLASSCD || '-' || T2.SCHOOL_KIND ");
        } else {
            stb.append(" LEFT JOIN NAME_MST N1 ON N1.NAMECD1='D008' AND N1.NAMECD2 = SUBSTR(T2.SUBCLASSCD,1,2)");
        }
        stb.append(" LEFT JOIN COMBINED_CREDITS T10 ON T10.COMBINED_SUBCLASSCD = T2.SUBCLASSCD");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("                 AND T10.COMBINED_CLASSCD = T2.CLASSCD");
            stb.append("                 AND T10.COMBINED_SCHOOL_KIND = T2.SCHOOL_KIND");
            stb.append("                 AND T10.COMBINED_CURRICULUM_CD = T2.CURRICULUM_CD");
        }

        stb.append(" ORDER BY ORDER1, ORDER2, ORDER3, ORDER4, ORDER5");
        return stb.toString();
    }

    /**
     * <pre>
     * 学校外における学修の単位数を取得するＳＱＬ文を戻します。
     * ・生徒個人を対象とします。
     * </pre>
     */
    private String prestatementQualified (final Param parameter) {
        
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT W1.CONDITION_DIV,W1.REGDDATE,W1.SUBCLASSCD,W1.CONTENTS,W1.CREDITS,W2.SUBCLASSNAME");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , W1.CLASSCD ");
            stb.append("   , W1.SCHOOL_KIND ");
            stb.append("   , W1.CURRICULUM_CD ");
        }
        stb.append(",CASE W1.CONDITION_DIV WHEN '1' then W4.NAME1 ELSE W5.NAME1 END AS NAME1");
        stb.append(" FROM SCHREG_QUALIFIED_DAT W1 ");
        stb.append(" LEFT JOIN SUBCLASS_MST W2 ON W2.SUBCLASSCD = W1.SUBCLASSCD ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   AND W2.CLASSCD = W1.CLASSCD ");
            stb.append("   AND W2.SCHOOL_KIND = W1.SCHOOL_KIND ");
            stb.append("   AND W2.CURRICULUM_CD = W1.CURRICULUM_CD ");
        }
        stb.append(" LEFT JOIN NAME_MST W4 ON W4.NAMECD1 = 'H305' AND W4.NAMECD2 = W1.CONTENTS AND W1.CONDITION_DIV = '1'");
        stb.append(" LEFT JOIN NAME_MST W5 ON W5.NAMECD1 = 'H306' AND W5.NAMECD2 = W1.CONTENTS AND W1.CONDITION_DIV = '2'");
        stb.append(" WHERE   W1.YEAR ='" + parameter._year + "' AND W1.SCHREGNO = ? ");
        stb.append(" ORDER BY W1.SUBCLASSCD ");
        if ("1".equals(parameter._useCurriculumcd)) {
            stb.append("   , W1.CLASSCD ");
            stb.append("   , W1.SCHOOL_KIND ");
            stb.append("   , W1.CURRICULUM_CD ");
        }
        return stb.toString();
    }


    /** 
     * <pre>
     * 過去の修得単位数を取得するＳＱＬ文を戻します。
     * ・生徒個人を対象とします。
     * </pre>
     */
    String prestatementGetCredits (final Param parameter) {
        StringBuffer  stb = new StringBuffer();

        stb.append(" WITH ");
        // 学習記録データ（前年度まで）の単位の表
        stb.append("  STUDYREC AS(");
        stb.append("   SELECT CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDITS");
        stb.append("   FROM   SCHREG_STUDYREC_DAT T1");
        stb.append("   WHERE  T1.SCHREGNO = ? AND YEAR < '" + parameter._year + "')");
        
        // 学外学修単位数（前期・後期とも今年度）の表
        stb.append(" ,QUALIFIED AS(");
        stb.append("   SELECT T1.CREDITS");
        stb.append("   FROM   SCHREG_QUALIFIED_DAT T1");
        stb.append("   WHERE  T1.SCHREGNO = ?");
        stb.append("  AND YEAR = '" + parameter._year + "'");
        stb.append(" )");

        // 留学単位数（前期は前年度まで、後期は今年度まで）の表
        stb.append(" ,ABLOAD AS(");
        stb.append("   SELECT ABROAD_CREDITS AS CREDITS");
        stb.append("   FROM   SCHREG_TRANSFER_DAT");
        stb.append("   WHERE  SCHREGNO = ? AND TRANSFERCD = '1'");
        if (Integer.parseInt(parameter._semester) == parameter.definecode.semesdiv) {
            stb.append("  AND FISCALYEAR(TRANSFER_SDATE) <= '" + parameter._year + "'");
        } else {
            stb.append("  AND FISCALYEAR(TRANSFER_SDATE) < '" + parameter._year + "'");
        }
        stb.append(" )");
        
        // メイン表
        stb.append(" SELECT SUM(CREDITS) AS CREDITS");
        stb.append(" FROM(SELECT CREDITS FROM STUDYREC");
        stb.append(" UNION ALL SELECT CREDITS FROM QUALIFIED");
        stb.append(" UNION ALL SELECT CREDITS FROM ABLOAD");
        stb.append(" )T1");

        // NO012<修得済単位数の仕様変更> 以前のSQLは削除するが、リビション1.12に残っている。

        return stb.toString();
    }


    /** 
     * <pre>
     * 委員会・クラブ活動履歴を取得するＳＱＬ文を戻します。
     * ・生徒個人を対象とします。
     * ・クラブは異動指定日が属するデータ。
     * </pre>
     */
    String prestatementCommitAndClub (final Param parameter) {
        StringBuffer stb = new StringBuffer();
        //委員会の表
        stb.append("SELECT  1 AS DIV,T1.SCHREGNO, T2.COMMITTEENAME AS NAME ");
        stb.append("FROM    SCHREG_COMMITTEE_HIST_DAT T1 ");
        stb.append("INNER JOIN COMMITTEE_MST T2 ON T2.COMMITTEECD = T1.COMMITTEECD ");
        stb.append(                           "AND T2.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
        stb.append("WHERE   T1.YEAR = '" + parameter._year + "' ");
        stb.append(    "AND T1.SCHREGNO = ? ");
        //クラブの表
        stb.append("UNION ");
        stb.append("SELECT  2 AS DIV,T1.SCHREGNO,T2.CLUBNAME AS NAME ");
        stb.append("FROM    SCHREG_CLUB_HIST_DAT T1 ");
        stb.append("INNER JOIN CLUB_MST T2 ON T1.CLUBCD = T2.CLUBCD ");
        stb.append("WHERE   '" + parameter._date + "' BETWEEN T1.SDATE AND VALUE(T1.EDATE,'" + parameter._date + "') ");
        stb.append(     "AND T1.SCHREGNO = ? ");
        stb.append("ORDER BY SCHREGNO,DIV,NAME ");
        return stb.toString();
    }


    /**
     * Prestatementのインスタンスをクローズします。
     * @param arrps
     * @throws SQLException
     */
    private void prestatementClose (
            final PreparedStatement arrps[]
    ) throws SQLException {
        for (int i = 0 ; i < arrps.length ; i++) {
            if (arrps[i] != null) arrps[i].close();
        }
    }


    /**
     * <pre>
     * 教科名を印字します。
     * ・SVFフォームのフィールド属性を変更します。(RECORD)
     * 　・文字数により文字ピッチ及びＹ軸を変更します。
     * </pre>
     */
    private void svfFieldAttribute_CLASS (
            final Vrw32alpWrap svf,
            final String name,
            final int ln
    ) {
        if( svfobj == null )svfobj = new KNJSvfFieldModify();
        svfobj.width = 250;     //フィールドの幅(ドット)
        svfobj.height = 58;     //フィールドの高さ(ドット)
        svfobj.ystart = 1044;   //開始位置(ドット)
        svfobj.minnum = 10;     //最小設定文字数
        svfobj.maxnum = 40;     //最大設定文字数
        svfobj.setRetvalue( name, ln );
        svf.VrAttribute("CLASS" , "Y="+ svfobj.jiku );
        svf.VrAttribute("CLASS" , "Size=" + svfobj.size );
        svf.VrsOut("CLASS",  name );
    }


    /**
     * <pre>
     * 科目名を印字します。
     * ・SVFフォームのフィールド属性を変更します。(RECORD)
     * 　・文字数により文字ピッチ及びＹ軸を変更します。
     * </pre>
     */
    private void svfFieldAttribute_SUBCLASS (
            final Vrw32alpWrap svf,
            final String name,
            final int ln
    ) {
        if( svfobj == null )svfobj = new KNJSvfFieldModify();
        svfobj.width = 740;     //フィールドの幅(ドット)
        svfobj.height = 58;     //フィールドの高さ(ドット)
        svfobj.ystart = 1044;   //開始位置(ドット)
        svfobj.minnum = 30;     //最小設定文字数
        svfobj.maxnum = 40;     //最大設定文字数
        svfobj.setRetvalue( name, ln );
        svf.VrAttribute("SUBCLASS" , "Y="+ svfobj.jiku );
        svf.VrAttribute("SUBCLASS" , "Size=" + svfobj.size );
        svf.VrsOut("SUBCLASS",  name );
    }


    /**
     * 総合的な学習の時間において空行を印字します。
     */
    private int printBlankLine (
            final Vrw32alpWrap svf,
            final String num90,
            final int line
    ) {
        int intnum90 = parseInt(num90);
        if (0 == intnum90) return 0;
        // 空行なしでページ内に収まる場合は空行を印字しない。
        if (0 == (line + intnum90) % RECORD_MAX_LINE) return 0;
        int i = line + 1;
        i = (i % RECORD_MAX_LINE == 0)? RECORD_MAX_LINE: i % RECORD_MAX_LINE;
        // １行目は空行を印字しない。
        if (1 == i) return 0;
        svf.VrEndRecord();
        return 1;
    }

    
    //--- 内部クラス -------------------------------------------------------
    /**
     * <<クラスの説明>>。
     * 日付の表示形式のクラスです。
     * @author yamasiro
     * @version $Id: 6fe7db38775b938e86595790851b68165e677e07 $
     */
    private static class MyDateFormat {

        private static final DecimalFormat df = new DecimalFormat("00");
        private static final SimpleDateFormat sd = new SimpleDateFormat ("yyyy-MM-dd");
        private static final Map gmap = new HashMap();

        private MyDateFormat () { init(); }

        private void init () {
            final String kanji[] = {"平成","昭和","大正","明治"};
            final String eiji[] = {"H","S","T","M"};
            for (int i = 0; i < kanji.length; i++) {
                gmap.put(kanji[i], eiji[i]);
            }
        }
        
        /**
         * 平成18年6月1日 => H18.06.01
         */
        private String format (final String bdate) throws ParseException, SQLException {
            if (bdate == null)return null;
            final Calendar cal = Calendar.getInstance();
            cal.setTime (sd.parse(bdate));
            final String st = nao_package.KenjaProperties.gengou(cal.get(Calendar.YEAR));
            final StringBuffer sb = new StringBuffer();
            if (st != null && 1 < st.length()) {
                String sm = (String) gmap.get(st.substring(0, 2));
                if (sm != null) sb.append(sm);
            }
            int nn = Integer.parseInt(st.substring(2));
            sb.append(df.format(nn));
            sb.append("." + df.format(cal.get(Calendar.MONTH) + 1));
            sb.append("." + df.format(cal.get(Calendar.DATE)));
            return sb.toString();
        }
    }
  
    
    /**
     * <<クラスの説明>>。
     * クラス内で使用するHttpServletRequestのリクエストパラメータや
     * その他パラメータを持っています。<br>
     * 以前は String param[] を使用していました。
     * @author yamasiro
     * @version $Id: 6fe7db38775b938e86595790851b68165e677e07 $
     */
    private static class Param {

        private final String _year;
        private final String _semester;
        private final String _grade_hr_class;
        private final String _date;
        private final boolean _isprintB5;
        private final boolean _isprintGardianaAddress;
        private final boolean _isprintLateEarly;
        private final boolean _isTotalstudytime;
        private final boolean _isSpecialactremark;
        private final boolean _isCommunication;
        private final String _patarn;

        private int _frmNo = 4;
        private String _attend_date;
        private String _attend_month;
        
        private String _nendo;
        private String _schoolName;
        private String _principalName;
        private List _arrstaffName;
        private List _arrsemesName;
        
        private String[] _selectSchregno;
        private String _schnoString;
        
        final private KNJDefineSchool _defineSchool = new KNJDefineSchool();
        private KNJSchoolMst _knjSchoolMst;
        private KNJDefineSchool definecode;       //各学校における定数等設定
        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        private final String _useClassDetailDat;
        private final String _useVirus;
        private final String _useKoudome;
        private final String _useAddrField2;

        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private String _sDate;
        final private String SSEMESTER = "1";

         /**
         * コンストラクタ。
         * @param request
         */
        Param(final HttpServletRequest request) {
            _year = request.getParameter("YEAR");  // 年度
            _semester = request.getParameter("GAKKI");  // 1-3:学期
            _grade_hr_class = request.getParameter("GRADE_HR_CLASS");  // 学年・組
            _date = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE") );  //異動基準日
            _isprintGardianaAddress = null != request.getParameter("OUTPUT");  // param[13] = request.getParameter("OUTPUT"); //保護者の住所を出力する
            _isprintB5 = null != request.getParameter("OUTPUT2");  // param[15] = request.getParameter("OUTPUT2"); //用紙サイズ
            _isprintLateEarly = null == request.getParameter("OUTPUT3");  // param[16] = request.getParameter("OUTPUT3"); //遅刻・早退の有無
            _isTotalstudytime = null != request.getParameter("OUTPUT4");
            if (_isTotalstudytime) {
                _frmNo--;
            }
            _isSpecialactremark = null != request.getParameter("OUTPUT5");
            if (_isSpecialactremark) {
                _frmNo--;
            }
            _isCommunication = null != request.getParameter("OUTPUT6");
            if (_isCommunication) {
                _frmNo--;
            }
            _selectSchregno = request.getParameterValues("category_selected");  // 学籍番号
            _patarn = request.getParameter("PATARN");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useVirus  =request.getParameter("useVirus");
            _useKoudome  =request.getParameter("useKoudome");
            _useAddrField2 = request.getParameter("useAddrField2");
        }

        private KNJDefineCode setClasscode0(final DB2UDB db2) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
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

        private void loadAttendSemesArgument(DB2UDB db2) {
            
            try {
                loadSemester(db2);
                // 出欠の情報
                _defineSchool.defineCode(db2, _year);
                try {
                    _knjSchoolMst = new KNJSchoolMst(db2, _year);
                } catch (SQLException e) {
                    log.warn("学校マスタ取得でエラー", e);
                }
                final KNJDefineCode definecode0 = setClasscode0(db2);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _date); // _sDate: 年度開始日, _date: LOGIN_DATE
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }

        /**
         *  出欠集計端数処理用の日 _attend_date と月 _attend_month をセットします。
         */
        void setAttendDate (final DB2UDB db2) {
            KNJDivideAttendDate obj = new KNJDivideAttendDate();
            obj.getDivideAttendDate(db2, _year, _semester, _date);
            _attend_date = obj.date;
            _attend_month = obj.month;
        }
        boolean isPattarnB() {
            return "2".equals(_patarn);
        }

        boolean isPattarnC() {
            return "3".equals(_patarn);
        }

        /**
         * 年度 _nendo、学校名 _schoolName、校長名 _principalName、担任名 _arrstaffName、学期名称 _arrsemesName をセットします。
         */
        private void setHead (final DB2UDB db2) {

            // 年度
            _nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
            final KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            // 学校名・校長名
            returnval = getinfo.getSchoolName(db2, _year);
            _schoolName = returnval.val1;  // 学校名
            _principalName = returnval.val2;  // 校長名

            // 学級担任名を取得
            _arrstaffName = getinfo.Staff_name(db2, _year, _semester, _grade_hr_class);

            // 欠課数換算定数取得 => KNJDefineCodeImpを実装したオブジェクトを作成
            definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);  // 各学校における定数等設定
            if (log.isDebugEnabled()) {
                log.debug("schoolmark=" + definecode.schoolmark + " *** semesdiv=" + definecode.semesdiv + " *** absent_cov=" + definecode.absent_cov + " *** absent_cov_late=" + definecode.absent_cov_late);
            }
            
            // 学期名称
            _arrsemesName = new ArrayList(definecode.semesdiv);
            try {
                db2.query("SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER < '9' ORDER BY SEMESTER");
                final ResultSet rs = db2.getResultSet();
                while (rs.next()) {
                    _arrsemesName.add(rs.getString("SEMESTERNAME"));
                }
                db2.commit();
                rs.close();
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }
        
        /**
         * 指定された複数の学籍番号を文字列に編集し、_schnoString へセットします。(SQL用) 
         */
        private void setSchregno () {

            StringBuffer stb = new StringBuffer();
            for (int i = 0; i < _selectSchregno.length; i++) {
                if (0 == i) stb.append("('");
                else        stb.append("','");
                stb.append(_selectSchregno[i]);
            }
            stb.append("')");
            _schnoString = stb.toString();
        }

        /**
         * 年度の開始日を取得する 
         */
        private void loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    map.put(semester, name);
                    final String sDate = rs.getString("SDATE");
                    list.add(sDate);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            if (!list.isEmpty()) {
                _sDate = (String) list.get(0);
            }
            log.debug("年度の開始日=" + _sDate);
        }
        private String sqlSemester() {
            final String sql;
            sql = "select"
                + "   SEMESTER,"
                + "   SEMESTERNAME,"
                + "   SDATE"
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR='" + _year + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }
        
        private static boolean setTableColumnCheck(final DB2UDB db2, final String tabname, final String colname) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT 1 FROM ");
            if (StringUtils.isBlank(colname)) {
                stb.append("SYSCAT.TABLES");
            } else {
                stb.append("SYSCAT.COLUMNS");
            }
            stb.append(" WHERE TABNAME = '" + tabname + "' ");
            if (!StringUtils.isBlank(colname)) {
                stb.append(" AND COLNAME = '" + colname + "' ");
            }
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean hasTableColumn = false;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    hasTableColumn = true;
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.fatal(" hasTableColumn " + tabname + (null == colname ? "" :  "." + colname) + " = " + hasTableColumn);
            return hasTableColumn;
        }
    }
}

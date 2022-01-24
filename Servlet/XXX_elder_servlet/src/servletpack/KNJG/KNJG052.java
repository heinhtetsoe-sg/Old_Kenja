// kanji=漢字
/*
 * $Id: 5b1a6c564e917bafd8fa265a9be70feeb250d80d $
 *
 * 作成日: 2007/03/13
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJG;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;

/**
 *  学校教育システム 賢者 [XXXX管理] 修了・卒業証明書（中高一貫）
 *
 *  2007/03/13 nakamoto・新規作成
 */

public class KNJG052 {

    private static final Log log = LogFactory.getLog(KNJG052.class);

    private String schno[];
    private boolean nonedata;

    private static final String FORM_FILE  = "KNJG010_9.frm";  // 修了
    private static final String FORM_FILE_KYOAI  = "KNJG010_9KYOAI.frm";  // 修了

    private static final String FORM_FILE2 = "KNJG010_1.frm";  // 卒業
    private static final String FORM_FILE2_CHIBEN1 = "KNJG010_1A.frm"; // 智辯卒業証明書
    private static final String FORM_FILE2_CHIBEN2 = "KNJG010_1A2.frm"; // 智辯卒業証明書2
    private static final String FORM_FILE2_MUSASHI1 = "KNJG010_1_1MUSA.frm";
    private static final String FORM_FILE2_MUSASHI2 = "KNJG010_1_2MUSA.frm";
    private static final String FORM_FILE2_TORI = "KNJG010_1TORI.frm";
    private static final String FORM_FILE2_KYOAI = "KNJG010_1KYOAI.frm";
    private static final String FORM_FILE2_CYUKYO = "KNJG010_1CYUKYO.frm";
    
    /**
     *  KNJD.classから最初に起動されるクラス
     */
    public void svf_out(
            HttpServletRequest request,
            HttpServletResponse response)
    throws ServletException, IOException {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                  // Databaseクラスを継承したクラス
        
        // print svf設定
        sd.setSvfInit(request, response, svf);
        // ＤＢ接続
        db2 = sd.setDb(request);
        if( sd.openDb(db2) ){
            log.error("db open error");
            return;
        }
        // パラメータの取得
        final Param paramap = getParam(request, db2);

        paramap.setHead(db2);         //見出し項目
        // 印刷処理
        printSvfMain(db2,svf,paramap);        //SVF-FORM出力処理
        // 終了処理
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }

    protected String setSvfForm(final Param paramap) { 
        int kind = Integer.parseInt(paramap._kind);
        
        final String cmbClass = paramap._cmbClass;
        int grade = -1;
        if (cmbClass != null) {
            grade = Integer.parseInt("1".equals(paramap._output) ? cmbClass : cmbClass.substring(0, 2));
        }

        if (kind == 2) { // 卒業証明書
            if (grade == 3) { // 学年==3
                kind = 22;
            } else if (grade >= 11) { // 学年>=11
                kind = 23;
            }
        }

        String filename = "";
        if (kind == 1) { // 修了証
            filename = paramap._isKyoai ? FORM_FILE_KYOAI : FORM_FILE;
        } else if (kind == 2) { // 卒業証明書
            filename =  (paramap._isChiben) ? FORM_FILE2_CHIBEN1 : FORM_FILE2; 
        } else if (kind == 22) { // 卒業証明書
            filename = (paramap._isChiben) ? FORM_FILE2_CHIBEN2 : FORM_FILE2; 
        } else if (kind == 23) {  // 卒業証明書
            filename = (paramap._isChiben) ? FORM_FILE2_CHIBEN2 : FORM_FILE2;
        }
        log.debug(" grade = " + grade + ", kind = " + kind + ", filename = " + filename);
        return filename;
    }

    /** 
     *  SVF-FORM メイン出力処理 
     */
    private void printSvfMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param
    ) {
        //定義
        PreparedStatement ps = null;
        ResultSet rs = null;

        //RecordSet作成
        try {
            String sql = sqlSchregno(param); //学籍データ
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while( rs.next() ){
                //生徒出力処理
                if ((param._isMusashi || param._isTottori || param._isKyoai || param._isChukyo || true) && "2".equals(param._kind)) {
                    if (printPrivate(svf, db2, param, rs.getString("SCHREGNO"))) {
                        printSchool(svf, db2, param, rs);
                        if ("1".equals(param._printStamp)) {
                        	printStamp(svf, param);
                        }
                        svf.VrEndPage();
                        nonedata = true;
                    }
                } else {
                    printSvfOutSchregno(svf,rs,param);
                }
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);  
        }
    }
    
    private int byteCountMS932(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("EncodingException!", e);
                count = str.length();
            }
        }
        return count;
    }
    
    private String getBirthdayFormattedString(final Param param, final String rsBirthday, final String birthdayFlg) {
        final String birthday;
        if (param.seirekiFlg || (!param.seirekiFlg && "1".equals(birthdayFlg))) {
            birthday = rsBirthday.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(rsBirthday) + "生";
        } else {
            birthday = KNJ_EditDate.h_format_JP_Bth( rsBirthday );
        }
        return birthday;
    }
    
    private boolean printPrivate(final Vrw32alp svf, final DB2UDB db2, final Param param, final String schregno) {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map map = new HashMap();
        try {
            String sql = "SELECT SCHOOLDIV, SCHOOLTELNO, SCHOOLFAXNO FROM SCHOOL_MST WHERE YEAR = '" + param._year+"' ";
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                map.put("SCHOOLTELNO", rs.getString("SCHOOLTELNO"));
                map.put("SCHOOLFAXNO", rs.getString("SCHOOLFAXNO"));
            }
            
        } catch (Exception e) {
            log.warn("ctrl_date get error!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        PreparedStatement ps6 = null;
        ResultSet rs6 = null;
        try {
            // 個人データ
            final String sql1 = param._personalInfoSql.sql_info_reg("11110011111");
            ps6 = db2.prepareStatement(sql1);
            int p = 0;
            ps6.setString( ++p, schregno );     //学籍番号
            ps6.setString( ++p, param._year );     //対象年度
            ps6.setString( ++p, param._gakki );     //対象学期
            ps6.setString( ++p, schregno );     //学籍番号
            ps6.setString( ++p, param._year ); //対象年度
            
            rs6 = ps6.executeQuery();
            if (rs6.next()) {
                boolean nonedata_ = privateobjPrintInfo(svf, db2, rs6, param, map); // 武蔵高校の卒業証明書
                nonedata = nonedata_;
            }
        } catch (Exception e) {
            log.error("rs.next()! ", e);
        } finally {
            DbUtils.closeQuietly(null, ps6, rs6);
            db2.commit();
        }
        return nonedata;
    }
    
    /*
     *  学校情報出力
     */
    private boolean printSchool(final Vrw32alp svf, final DB2UDB db2, final Param param, ResultSet rs1) {
        String certifNo = null;
        try {
            final String rsCertifNo = "1".equals(param._certifNoSyudou) ? rs1.getString("REMARK1") : rs1.getString("ISSUE_CERTIF_NO");
            certifNo = "0".equals(rs1.getString("CERTIF_NO")) && rsCertifNo != null ? rsCertifNo : "     ";
        } catch (Exception e) {
            log.error(e);
        }
        
        int p = 0;
        PreparedStatement ps7 = null;
        ResultSet rs = null;
        try {
            final String sql = new servletpack.KNJG.detail.KNJ_SchoolinfoSql("12000").pre_sql();
            ps7 = db2.prepareStatement(sql);
            ps7.setString(++p, param._year);  // 対象年度
            ps7.setString(++p, param.certifKindcd);  // 証明書種別
            ps7.setString(++p, param.certifKindcd);  // 証明書種別
            ps7.setString(++p, param._year);  // 現年度
            ps7.setString(++p, "00000000");  // SQL選択フラグ
            rs = ps7.executeQuery();
            if (rs.next()) {
                svf.VrsOut("SYOSYO_NAME",  rs.getString("SYOSYO_NAME") );  //証書名
                svf.VrsOut("SYOSYO_NAME2", rs.getString("SYOSYO_NAME2") ); //証書名２
                //証書番号の印刷 0:あり,1:なし
                if (rs.getString("CERTIF_NO") != null && rs.getString("CERTIF_NO").equals("0")) {
                    if (null == certifNo || "".equals(certifNo)) {
                        certifNo = "     "; // 証明書番号の印刷が0:あり かつ証明書番号が無い場合 5スペース挿入
                    }
                    svf.VrsOut("CERTIF_NO",  certifNo );
                } else {
                    svf.VrsOut("CERTIF_NO",  "     ");
                }
                String str = printSyoushoNum(certifNo, rs.getString("SYOSYO_NAME"));  // 証書番号を出力
                if (str != null) {
                    svf.VrsOut("NENDO_NAME",  str);
                }
                schoolobjPrintInfo(svf, param, rs);
            }
        } catch(Exception e) {
            log.error("rs.next()! ", e);
        } finally {
            DbUtils.closeQuietly(null, ps7, rs);
            db2.commit();
        }
        return true;
    }
    
    /* 
     * 学校情報の出力
     */
    public void schoolobjPrintInfo (final Vrw32alp svf, final Param param, final ResultSet rs) {
        String date = printsvfDate(param, param._noticeday);  // 証明日付
        if (null != date) {
            svf.VrsOut("DATE", date);
        } else {
            svf.VrAttribute("DATE", "Hensyu=1");
            svf.VrsOut("DATE", "年　 月　 日");
        }
        
        try {
            svf.VrsOut( "SCHOOLNAME",  rs.getString("SCHOOLNAME1") );             //学校名
            if (rs.getString("PRINCIPAL_JOBNAME") != null) { svf.VrsOut("JOBNAME", rs.getString("PRINCIPAL_JOBNAME")); }  // 校長名
            if (rs.getString("PRINCIPAL_NAME") != null) { svf.VrsOut("STAFFNAME", rs.getString("PRINCIPAL_NAME")); }  // 校長名
        } catch (Exception e) {
             log.error("Exception", e);
        }
    }
    

    /*
     *  証明日付の出力
     */
    private String printsvfDate(
            final Param param,
            final String date
    ) {
        if (date != null && 0 < date.length()) {
            if (param.seirekiFlg) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);  // 証明日付
            } else {
                return KNJ_EditDate.h_format_JP(date);  // 証明日付
            }
        }
        return null;
    }

    /*
     * @param param
     * @throws SQLException
     */
    private String printSyoushoNum(
            String number,
            String syousyoname
    ) throws SQLException {
        if (number == null) return null;
        final String str;
        if (syousyoname != null) {
            str = number + syousyoname;  // 証明書番号
        }else {
            str = number;  // 証明書番号
        }
        return str;
    }
    
    /**
     * 卒業証明書出力用
     * @param svf
     * @param db2
     * @param rs
     * @param param
     * @return
     */
    private boolean privateobjPrintInfo(final Vrw32alp svf, final DB2UDB db2, final ResultSet rs, final Param param, final Map map) {
        boolean nonedata = true;
        try {
//            if (param._isMusashi) {
//                final String grdDate = rs.getString("GRD_DATE");
//                if (StringUtils.isBlank(grdDate)) {
//                    return false;
//                }
//                final boolean noticeDayisOld = (StringUtils.isBlank(param._noticeday)) ? false : param._noticeday.replace('/','-').compareTo(grdDate) < 0;
//                if (noticeDayisOld) {
//                    return false;
//                }
//                final String formName;
//                if (Integer.parseInt(KNJ_EditDate.b_year(grdDate)) < Integer.parseInt(KNJ_EditDate.b_year(param._noticeday))) { // 記載日付の年度以前に卒業
//                    formName = FORM_FILE2_MUSASHI2;
//                } else {
//                    formName = FORM_FILE2_MUSASHI1;
//                }
//                // 卒業年度の3月31日の年を表示
//                final String graduation = hankakuToZenkaku(KenjaProperties.gengou(Integer.parseInt(KNJ_EditDate.b_year(grdDate)) + 1));
//                log.debug(" form = " + formName);
//                svf.VrSetForm(formName, 1);
//                svf.VrsOut("GRADUATION", graduation);  //卒業年月
//            } else if (param._isTottori || param._isChukyo || param._isKyoai) {
            if (param._isTottori || param._isChukyo || param._isKyoai) {
                final String graduDate = rs.getString("GRADU_DATE");
                final String graduation;
                if (param.seirekiFlg) {
                    graduation = graduDate.substring(0, 4) + "年" + KNJ_EditDate.h_format_S(graduDate,"M") + "月" + KNJ_EditDate.h_format_S(graduDate, "d") + "日";
                } else {
                    graduation = KNJ_EditDate.h_format_JP( graduDate );
                }
                
                String major1 = null == rs.getString("MAJORNAME") ? "" : rs.getString("MAJORNAME");
                String major2 = "";
                final int spaceIndex = -1 != major1.indexOf(' ') ? major1.indexOf(' ') : major1.indexOf('　'); // 学科名に全角スペースまたは半角スペースがあるか
                final String form = param._isChukyo ? FORM_FILE2_CYUKYO : param._isKyoai ? FORM_FILE2_KYOAI : FORM_FILE2_TORI;
                svf.VrSetForm(form, 1);
                final String courseName = rs.getString("COURSENAME") != null ? rs.getString("COURSENAME") + "課程" : "";
                if (spaceIndex != -1 && spaceIndex != major1.length() - 1) {
                    major2 = major1.substring(spaceIndex + 1);
                    major1 = major1.substring(0, spaceIndex);
                    
                    svf.VrsOut("MAIN1", "　上記の者は" + graduation + courseName + major1);
                    svf.VrsOut("MAIN2", major2 + "を卒業したことを証明します");
                } else {
                    svf.VrsOut("MAIN1", "　上記の者は " + graduation + " " + courseName);
                    svf.VrsOut("MAIN2", major1 + " を卒業したことを証明します");
                }
            } else {
                svf.VrSetForm(setSvfForm(param), 1);
                svf.VrsOut("PHONE", (String) map.get("SCHOOLTELNO"));
                svf.VrsOut("FAX", (String) map.get("SCHOOLFAXNO"));
                svf.VrsOut( "COURSE_NAME",     rs.getString("COURSENAME") + rs.getString("COURSECODENAME")); //課程+コース
                svf.VrsOut( "COURSECODE_NAME", rs.getString("COURSECODENAME")); //コース
                if (null != rs.getString("GRADU_DATE")) {  
                    final String graduation;
                    final String graduDate = rs.getString("GRADU_DATE");
                    if (param.seirekiFlg) {
                        graduation = graduDate.substring(0, 4) + "年" + KNJ_EditDate.h_format_S(graduDate,"M") + "月";
                    } else {
                        graduation = KNJ_EditDate.h_format_JP_M( graduDate );
                    }
                    svf.VrsOut( "GRADUATION", graduation );  //卒業年月
                }
            }
                
            final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
            svf.VrsOut(byteCountMS932(name) > 24 ? "NAME2" : "NAME1", name );                                         //氏名
            
            if (null != rs.getString("BIRTHDAY")) {
                svf.VrsOut("BIRTHDAY", getBirthdayFormattedString(param, rs.getString("BIRTHDAY"), rs.getString("BIRTHDAY_FLG")));  // 証明日付
            }
            if (null != rs.getString("ENT_DATE")) {
                final String entDate = rs.getString("ENT_DATE");
                final String entDateStr;
                if (param.seirekiFlg) {
                    entDateStr = entDate.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(entDate);
                } else {
                    entDateStr = KNJ_EditDate.h_format_JP(entDate);
                }
                svf.VrsOut("ENT_DATE", entDateStr);
            }
            //句点出力
            if (param._point != null) {
                svf.VrsOut("POINT", "。" );
            }
        } catch (Exception e) {
            log.error("exception!", e);
        }
        // log.debug(" isJunior = " + isJunior + " , isGrdOld = " + isGrdOld + ", GRADU_YEAR = " + KNJ_EditDate.b_year(gradu_date) + ", year = "+ param._year);
        return nonedata;
    }

    /** 
     *   生徒出力処理
     */
    private void printSvfOutSchregno(
            final Vrw32alp svf,
            final ResultSet rs,
            final Param param
    ) {
        try {
            svf.VrSetForm(setSvfForm(param), 1);
            // 生徒名
            final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
            if (name != null  &&  12 < name.length()) {
                svf.VrsOut("NAME2", name );
            } else {
                svf.VrsOut("NAME1", name );
            }
            // 誕生日
            if (rs.getString("BIRTHDAY") != null) {
                final String birthdayStr;
                if (param.seirekiFlg) {
                    birthdayStr = getSeireki_yyyyMd(rs.getString("BIRTHDAY"), "yyyy年M月d日生");
                } else {
                    birthdayStr = KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY"));
                }
                svf.VrsOut("BIRTHDAY", birthdayStr);
            }
            //印刷種別 1:修了 2:卒業
            if (param._kind.equals("1")) {
                svf.VrsOut("COURSE", rs.getString("COURSENAME") );  // 課程名
            } else {
                svf.VrsOut("GRADUATION", param.graduatemonth );  // 卒業年月
                svf.VrsOut("SYOSYO_NAME", rs.getString("SYOSYO") );  // 証書名
                final String rsCertifNo = "1".equals(param._certifNoSyudou) ? rs.getString("REMARK1") : rs.getString("ISSUE_CERTIF_NO");
                String certifNo = "0".equals(rs.getString("CERTIF_NO")) && rsCertifNo != null ? rsCertifNo : "     ";
                svf.VrsOut("CERTIF_NO", certifNo );  // 証書番号
                svf.VrsOut("SYOSYO_NAME2", rs.getString("SYOSYO2") );  // 証書名2
            }
            svf.VrsOut("SCHOOLNAME",  rs.getString("SCHOOL") );  // 学校名
            svf.VrsOut("JOBNAME",  rs.getString("JOB") );  // 役職名
            svf.VrsOut("STAFFNAME",  rs.getString("PRINCIPAL") );  // 校長名
            svf.VrsOut("DATE",  param.kisaidate );  // 記載日付
            //句点出力
            if (param._point != null) {
                svf.VrsOut("POINT", "。" );
            }
            if ("1".equals(param._printStamp)) {
            	printStamp(svf, param);
            }

            int ret = svf.VrEndPage();
            if(ret == 0)nonedata = true;
        } catch( Exception ex ){
            log.error("printSvfOutSchregno error!", ex );
        }
    }

	private void printStamp(final Vrw32alp svf, final Param param) {
		final String path;
		if ("J".equals(param._schoolKind)) {
			path = param._certifSchoolstampJImagePath;
		} else {
			path = param._certifSchoolstampHImagePath;
		}
		if (null != path) {
			svf.VrsOut("STAMP", path);
		}
	}

    /* 
     *  SQLStatement作成 学籍データ
     */
    private String sqlSchregno(final Param paramap) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO AS ( ");
        stb.append("     SELECT T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.YEAR, T2.ENT_DATE, ");
        stb.append("            case when T2.GRD_DIV = '1' then T2.GRD_DATE end as GRD_DATE, ");
        stb.append("            T2.NAME, T2.REAL_NAME, T2.BIRTHDAY, T1.COURSECD ");
        stb.append("     FROM   SCHREG_REGD_DAT T1, ");
        stb.append("            SCHREG_BASE_MST T2 ");
        stb.append("     WHERE  T1.YEAR = '" + paramap._year + "' AND ");
        stb.append("            T1.SEMESTER = '" + paramap._gakki + "' AND ");
        //カテゴリ区分 1:クラス 2:個人
        if (paramap._output.equals("1")) {
            stb.append("        T1.GRADE = '" + paramap._cmbClass + "' AND ");
            stb.append("        T1.GRADE || T1.HR_CLASS IN " + paramap._schnoList + " AND ");
        } else {
            stb.append("        T1.GRADE || T1.HR_CLASS = '" + paramap._cmbClass + "' AND ");
            stb.append("        T1.ATTENDNO IN " + paramap._schnoList + " AND ");
        }
        stb.append("            T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     ) ");
        stb.append(" , T_CERTIF AS ( ");
        stb.append("     SELECT YEAR, CERTIF_NO, ");
        stb.append("            CERTIF_KINDCD as KINDCD, ");
        stb.append("            SYOSYO_NAME as SYOSYO, ");
        stb.append("            SYOSYO_NAME2 as SYOSYO2, ");
        stb.append("            SCHOOL_NAME as SCHOOL, ");
        stb.append("            JOB_NAME as JOB, ");
        stb.append("            PRINCIPAL_NAME as PRINCIPAL ");
        stb.append("     FROM   CERTIF_SCHOOL_DAT ");
        stb.append("     WHERE  YEAR = '" + paramap._year + "' AND ");
        //印刷種別 1:修了 2:卒業
        if (paramap._kind.equals("1")) {
            stb.append("        CERTIF_KINDCD = '" + paramap.certifKindcd + "' ");
        } else {
            stb.append("        CERTIF_KINDCD = '" + paramap.certifKindcd + "' ");
        }
        stb.append("     ) ");
        //印刷種別 1:修了 2:卒業
        if (paramap._kind.equals("2")) {
            stb.append(" , T_ISSUE AS ( ");
            stb.append("     SELECT SCHREGNO,CERTIF_KINDCD,MIN(CERTIF_NO) AS CERTIF_NO ");
            stb.append("     FROM   CERTIF_ISSUE_DAT T1 ");
            stb.append("     WHERE  YEAR = '" + paramap._year + "' AND ");
            stb.append("            CERTIF_KINDCD = '" + paramap.certifKindcd + "' ");
            stb.append("     GROUP BY SCHREGNO,CERTIF_KINDCD ");
            stb.append("     ) ");
            stb.append(" , T_ISSUE2 AS ( ");
            stb.append("     SELECT T1.SCHREGNO, T1.CERTIF_INDEX, T1.CERTIF_NO ");
            stb.append("     FROM   CERTIF_ISSUE_DAT T1 ");
            stb.append("     INNER JOIN T_ISSUE T2 ON ");
            stb.append("         T2.SCHREGNO = T1.SCHREGNO AND T2.CERTIF_NO = T1.CERTIF_NO ");
            stb.append("     WHERE  T1.YEAR = '" + paramap._year + "' AND ");
            stb.append("            T1.CERTIF_KINDCD = '" + paramap.certifKindcd + "' ");
            stb.append("     ) ");
            stb.append(" , T_DETAIL AS ( ");
            stb.append("     SELECT T1.SCHREGNO, T1.REMARK1 ");
            stb.append("     FROM   CERTIF_DETAIL_EACHTYPE_DAT T1 ");
            stb.append("     INNER JOIN T_ISSUE2 T2 ON ");
            stb.append("         T2.SCHREGNO = T1.SCHREGNO AND T2.CERTIF_INDEX = T1.CERTIF_INDEX ");
            stb.append("     WHERE  T1.YEAR = '" + paramap._year + "' ");
            stb.append("     ) ");
        }

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.ENT_DATE, ");
        stb.append("     T1.GRD_DATE, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.REAL_NAME, ");
        stb.append("     T1.BIRTHDAY, ");
        stb.append("     T2.COURSENAME || '課程' AS COURSENAME, ");
        stb.append("     T3.CERTIF_NO, ");
        stb.append("     T3.KINDCD, ");
        stb.append("     T3.SYOSYO, ");
        stb.append("     T3.SYOSYO2, ");
        stb.append("     T3.SCHOOL, ");
        stb.append("     T3.JOB, ");
        stb.append("     T3.PRINCIPAL ");
        stb.append("     , (CASE WHEN T5.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME ");
        if (paramap._kind.equals("2")) {
            stb.append("   , T4.CERTIF_NO AS ISSUE_CERTIF_NO ");
            stb.append("   , T6.REMARK1 ");
        }
        stb.append("        , T21.BIRTHDAY_FLG ");
        stb.append(" FROM   SCHNO T1 ");
        stb.append("        LEFT JOIN COURSE_MST T2 ON T2.COURSECD = T1.COURSECD ");
        stb.append("        LEFT JOIN T_CERTIF T3 ON T3.YEAR = T1.YEAR ");
        //印刷種別 1:修了 2:卒業
        if (paramap._kind.equals("2")) {
            stb.append("    LEFT JOIN T_ISSUE T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("    LEFT JOIN T_DETAIL T6 ON T6.SCHREGNO = T1.SCHREGNO ");
        }
        stb.append("    LEFT JOIN SCHREG_NAME_SETUP_DAT T5 ON T5.SCHREGNO = T1.SCHREGNO AND T5.DIV = '01' ");
        stb.append("    LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT T21 ON T21.SCHREGNO = T1.SCHREGNO AND T21.BIRTHDAY_FLG = '1' ");
        stb.append(" ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        return stb.toString();
    }

    /* 
     *  get parameter doGet()パラメータ受け取り 
     */
    private Param getParam(final HttpServletRequest request, final DB2UDB db2) {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private static String getSeireki_yyyyMd(final String strx, final String pattern){
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
    
    public String hankakuToZenkaku(final String name) {
        if (null == name) {
            return null;
        }
        final Map henkanMap = new HashMap();
        henkanMap.put("1", "１");
        henkanMap.put("2", "２");
        henkanMap.put("3", "３");
        henkanMap.put("4", "４");
        henkanMap.put("5", "５");
        henkanMap.put("6", "６");
        henkanMap.put("7", "７");
        henkanMap.put("8", "８");
        henkanMap.put("9", "９");
        henkanMap.put("0", "０");
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < name.length(); i++) {
            final String key = name.substring(i, i + 1);
            final String val = henkanMap.containsKey(key) ? (String) henkanMap.get(key) : key;
            sb.append(val);
        }
        return sb.toString();
    }
    
    private static class Param {
        final String _year;  //年度
        final String _gakki;  //学期
        final String _cmbClass;  //学年or学年・組
        final String _kind;  //印刷種別 1:修了 2:卒業
        final String _output;  //カテゴリ区分 1:クラス 2:個人
        final String _noticeday;  //記載日付
        final String _graduateDate;  //記載日付
        final String _point;  //句点あり
        final String[] schno;  //出席番号または学年・組
        final String _schnoList;  //番号の編集(SQL用)
        final String _certifNoSyudou; //証明書発行番号は手入力の値を表示するか
        final String _documentRoot;
        final String _printStamp;
        final String _schoolKind;
        
        private boolean ikkanFlg;
        private String nendo;
        private String kisaidate;
        private String graduatemonth;
        private boolean seirekiFlg;
        private String certifKindcd;
        private String nameZ010;
        private boolean _isChiben;
        private boolean _isTottori;
        private boolean _isChukyo;
        private boolean _isMusashi;
        private boolean _isKyoai;
        private KNJ_PersonalinfoSql _personalInfoSql;
        private String _imagePath = null;
        private String _extension = null;
        private String _certifSchoolstampHImagePath = null;
        private String _certifSchoolstampJImagePath = null;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _cmbClass = request.getParameter("CMBCLASS");
            _kind = request.getParameter("KIND");
            _output = request.getParameter("OUTPUT");
            _noticeday = request.getParameter("NOTICEDAY");
            _graduateDate = request.getParameter("GRADUATE_DATE");
            _point = request.getParameter("POINT");
            _certifNoSyudou = request.getParameter("certifNoSyudou");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _printStamp = request.getParameter("PRINT_STAMP");
            
            schno = request.getParameterValues("CLASS_SELECTED");
            _schnoList = Set_Schno(schno);
            
            _schoolKind = getSchoolKind(db2);
            _personalInfoSql = new KNJ_PersonalinfoSql();
            setImagePath(db2);
            if ("1".equals(_printStamp)) {
                _certifSchoolstampHImagePath = getImagePath(_documentRoot, "CERTIF_SCHOOLSTAMP_H.jpg", false);
                _certifSchoolstampJImagePath = getImagePath(_documentRoot, "CERTIF_SCHOOLSTAMP_J.jpg", false);
            }
        }
        
        public String getSchoolKind(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String grade = "1".equals(_output) ? _cmbClass : _cmbClass.substring(0, 2);
            String schoolKind = null;
            try {
                final String sql = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year +"' AND GRADE = '" + grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                	schoolKind = rs.getString("SCHOOL_KIND");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return schoolKind;
        }

        public void setImagePath(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                    _extension = rs.getString("EXTENSION");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        
        public String getImagePath(final String documentRoot, final String filename, final boolean setExtension) {
            final String path = documentRoot + "/" + (null == _imagePath ? "" : _imagePath + "/") + filename + (setExtension ? "." + _extension : "");
            final File file = new File(path);
            if (!file.exists()) {
                log.info(" file " + file.getPath() + " exists? " + file.exists());
            }
            if (!file.exists()) {
                return null;
            }
            return file.getPath();
        }
        
        /*
         *  対象生徒番号編集(SQL用) 
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
        
        /** 
         *  SVF-FORMセット＆見出し項目 
         */
        public void setHead(
                final DB2UDB db2
        ) {
            setNameZ010(db2); // 名称マスタZ010をセット
            ikkanFlg = false; //中高一貫
            PreparedStatement ps = null;
            ResultSet rs = null;
            // 証明書種類コードの設定
            try {
                String tmpkind   = _kind;     // 印刷種別 1:修了 2:卒業
                String tmpOutput = _output;   // 印刷指定 1:クラス 2:個人
                String tmpCmbCls = _cmbClass; // コンボ '03' '03002'
                String tmpGrade  = (tmpOutput.equals("1")) ? tmpCmbCls : tmpCmbCls.substring(0, 2);
                if (tmpkind.equals("1")) {
                    certifKindcd = "015";
                } else {
                    certifKindcd = "001";
                    String sql = "SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' AND NAMESPARE2 IS NOT NULL ";
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while( rs.next() ){
                        if (tmpGrade.equals("03")) certifKindcd = "022";
                        ikkanFlg = true;
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            // 西暦または和暦の出力フラグ
            setSeirekiFlg(db2);

            // 日付を出力
            try {
                // 記載日付
                if (seirekiFlg) {
                    kisaidate = getSeireki_yyyyMd(_noticeday, "yyyy年M月d日");
                } else {
                    kisaidate = KNJ_EditDate.h_format_JP(_noticeday);
                }
                // 年度
                String warekinendo = nao_package.KenjaProperties.gengou(Integer.parseInt(_year));
                nendo = warekinendo.substring(2);
                //印刷種別 1:修了 2:卒業
                if (_kind.equals("2")) {
                    // 卒業年月
                    if (seirekiFlg) {
                        graduatemonth = getSeireki_yyyyMd(_graduateDate, "yyyy年M月");
                    } else {
                        graduatemonth = KNJ_EditDate.h_format_JP_M(_graduateDate);
                    }
                }
            } catch (Exception e) {
                 log.error("Exception", e);
            }
            
        }
        
        private void setNameZ010(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while( rs.next() ){
                    nameZ010 = rs.getString("NAME1");
                    _isChiben = "CHIBEN".equals(nameZ010);
                    _isTottori = "tottori".equals(nameZ010);
                    _isChukyo = "chukyo".equals(nameZ010);
                    _isMusashi = "MUSASHI".equals(nameZ010);
                    _isKyoai = "kyoai".equals(nameZ010);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        
        private void setSeirekiFlg(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                seirekiFlg = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}

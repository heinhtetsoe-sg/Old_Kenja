// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2021/04/28
 * 作成者: Nutec
 */
package servletpack.KNJF;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_Schoolinfo;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;

/**
 *
 *  学校教育システム 賢者 [保健管理]
 *
 *                  ＜ＫＮＪＦ０３０Ｊ＞  保健各種帳票印刷（クラス・個人）
 */
public class KNJF030J {
    private static final Log log = LogFactory.getLog(KNJF030J.class);
    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

    /** HTTP Get リクエストの処理 */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception
    {
        log.fatal("$Revision$");             // CVSキーワードの取り扱いに注意
        final Vrw32alp svf = new Vrw32alp();//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                  //Databaseクラスを継承したクラス

        KNJServletUtils.debugParam(request, log);

        //SVF作成処理
        boolean noData = true;//該当データなしフラグ
        Param param = null;

        PrintWriter outstrm = null;
        try {
            response.setContentType("application/pdf");
            outstrm = new PrintWriter (response.getOutputStream());

            //svf設定
            svf.VrInit();                           //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

            //print svf設定
            sd.setSvfInit(request, response, svf);

            //SVF出力
            {
                //DB接続
                db2 = sd.setDb(request);
                if (sd.openDb(db2)) {
                    log.error("db open error");
                    return;
                }
                param = new Param(db2, request);

                //SVF出力
                //※各帳票デザインそれぞれで指定した個人別にループ

                Form form = null;
                if (param._kensin_ryomen) {
                    //両面印刷チェックオン

                    form = new Form(svf, param);
                    final List<Map<String, String>> schnoMapList = KnjDbUtils.query(db2, KensinCommon.getSqlSchno(param));
                    for (final Map rsSchno : schnoMapList) {
                        if (param._isOutputDebug) {
                            log.info("両面印刷、学籍番号：" + getString("SCHREGNO", rsSchno));
                        }

                        //１）生徒学生健康診断票（一般）
                        KensinIppan kensinIppan = new KensinIppan(param, db2, form);
                        boolean printOmoteOk = kensinIppan.printMain(rsSchno);
                        if (printOmoteOk == false) {
                            setForm(param, svf, "BLANK_A4_TATE.frm", 1); // データがなければ空白ページ
                            svf.VrsOut("BLANK", "BLANK");
                            svf.VrEndPage();
                        }

                        //２）生徒学生健康診断票（歯・口腔）
                        KensinHa kensinHa = new KensinHa(param, db2, form);
                        boolean printUraOk = kensinHa.printMain(rsSchno);
                        if (printUraOk == false) {
                            setForm(param, svf, "BLANK_A4_TATE.frm", 1); // データがなければ空白ページ
                            svf.VrsOut("BLANK", "BLANK");
                            svf.VrEndPage();
                        }

                        if (printOmoteOk || printUraOk) {
                            noData = false;
                        }
                    }
                } else {
                    //両面印刷チェックオフ

                    form = new Form(svf, param);
                    final List<Map<String, String>> schnoMapList = KnjDbUtils.query(db2, KensinCommon.getSqlSchno(param));
                    //１）生徒学生健康診断票（一般）
                    if (param._printflgKensinIppan) {
                        for (final Map rsSchno : schnoMapList) {
                            if (param._isOutputDebug) {
                                log.info("通常印刷(一般)、学籍番号：" + getString("SCHREGNO", rsSchno));
                            }

                            KensinIppan kensinIppan = new KensinIppan(param, db2, form);
                            if (kensinIppan.printMain(rsSchno)) {
                                noData = false;
                            }
                        }
                    }

                    //２）生徒学生健康診断票（歯・口腔）
                    if (param._printflgKensinHa) {
                        for (final Map rsSchno : schnoMapList) {
                            if (param._isOutputDebug) {
                                log.info("通常印刷(歯・口腔)、学籍番号：" + getString("SCHREGNO", rsSchno));
                            }

                            KensinHa kensinHa = new KensinHa(param, db2, form);
                            if (kensinHa.printMain(rsSchno)) {
                                noData = false;
                            }
                        }
                    }
                }

                //健康診断結果通知書（一覧）
                if (param._printflgItiran) {
                    TutiItiran tutiItiran = new TutiItiran(param, db2, svf);
                    if (tutiItiran.printMain()) {
                        noData = false;
                    }
                }
                //健康診断結果通知書（運動器）
                if (param._printflgUndouki ) {
                    TutiUndouki tutiUndouki = new TutiUndouki(param, db2, svf);
                    if (tutiUndouki.printMain()) {
                        noData = false;
                    }
                }
                //健康診断結果通知書（眼科）
                if (param._printflgGanka) {
                    TutiGanka tutiGanka = new TutiGanka(param, db2, svf);
                    if (tutiGanka.printMain()) {
                        noData = false;
                    }
                }
                //健康診断結果通知書（結核）
                if (param._printflgKekkaku) {
                    TutiKekkaku tutiKekkaku = new TutiKekkaku(param, db2, svf);
                    if (tutiKekkaku.printMain()) {
                        noData = false;
                    }
                }
                //健康診断結果通知書（視力）
                if (param._printflgSiryoku) {
                    TutiSiryoku tutiSiryoku = new TutiSiryoku(param, db2, svf);
                    if (tutiSiryoku.printMain()) {
                        noData = false;
                    }
                }
                //健康診断結果通知書（歯科）
                if (param._printflgSika) {
                    TutiSika tutiSika = new TutiSika(param, db2, svf);
                    if (tutiSika.printMain()) {
                        noData = false;
                    }
                }
                //健康診断結果通知書（耳鼻科）
                if (param._printflgJibika) {
                    TutiJibika tutiJibika = new TutiJibika(param, db2, svf);
                    if (tutiJibika.printMain()) {
                        noData = false;
                    }
                }
                //健康診断結果通知書（色覚）
                if (param._printflgSikikaku) {
                    TutiSikikaku tutiSikikaku = new TutiSikikaku(param, db2, svf);
                    if (tutiSikikaku.printMain()) {
                        noData = false;
                    }
                }
                //健康診断結果通知書（心電図）
                if (param._printflgSindenzu) {
                    TutiSindenzu tutiSindenzu = new TutiSindenzu(param, db2, svf);
                    if (tutiSindenzu.printMain()) {
                        noData = false;
                    }
                }
                //健康診断結果通知書（聴力）
                if (param._printflgTyoryoku) {
                    TutiTyoryoku tutiTyoryoku = new TutiTyoryoku(param, db2, svf);
                    if (tutiTyoryoku.printMain()) {
                        noData = false;
                    }
                }
                //健康診断結果通知書（内科）
                if (param._printflgNaika) {
                    TutiNaika tutiNaika = new TutiNaika(param, db2, svf);
                    if (tutiNaika.printMain()) {
                        noData = false;
                    }
                }
                //健康診断結果通知書（尿）
                if (param._printflgNyou) {
                    TutiNyou tutiNyou = new TutiNyou(param, db2, svf);
                    if (tutiNyou.printMain()) {
                        noData = false;
                    }
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            log.debug("noData=" + noData);

            //  該当データ無し
            if (noData == true) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            if (null != param) {
                for (final PreparedStatement ps : param._psMap.values()) {
                    DbUtils.closeQuietly(ps);
                }
            }

            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();    //DBを閉じる
            outstrm.close();//ストリームを閉じる
        }

    }//doGetの括り

    //========================================================================
    /** 生徒学生健康診断票の全般共通処理 */
    private static class KensinCommon {
        /** SQL取得：出力対象の生徒を抽出 */
        public static String getSqlSchno(Param param)
        {
            final StringBuffer stb = new StringBuffer();
            //在籍
            stb.append("WITH SCHNO AS ( ");
            stb.append("    SELECT ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.YEAR, ");
            stb.append("        T1.SEMESTER, ");
            stb.append("        T1.GRADE, ");
            stb.append("        T1.HR_CLASS, ");
            stb.append("        T1.ATTENDNO, ");
            stb.append("        L1.SCHOOL_KIND ");
            stb.append("    FROM ");
            stb.append("        SCHREG_REGD_DAT T1 ");
            stb.append("        LEFT JOIN SCHREG_REGD_GDAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("             AND T1.GRADE = L1.GRADE ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR = '"+param._year+"' ");
            stb.append("        AND T1.SEMESTER='"+param._gakki+"' ");
            if (param._kubun.equals("1")) { //1:クラス
                stb.append("       AND T1.GRADE || T1.HR_CLASS IN "+param._selectInState+" ");
            } else if (param._kubun.equals("2")) { //2:個人
                stb.append("       AND T1.SCHREGNO IN "+param._selectInState+" ");
            }
            stb.append(" ) ");
            //メイン
            stb.append("SELECT T2.SCHREGNO, T4.NAME, HR_NAME, T2.GRADE, T2.HR_CLASS, T3.HR_CLASS_NAME1, GDAT.GRADE_NAME1, ATTENDNO, T2.SCHOOL_KIND ");
            stb.append("      ,(SELECT NAME2 FROM NAME_MST WHERE NAMECD1='Z002' AND NAMECD2=SEX) AS SEX, BIRTHDAY ");
            stb.append("      ,CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR(T2.YEAR || '-04-01' - BIRTHDAY) END AS AGE ");
            stb.append("      ,T_ENT_YEAR_GRADE_CD.GRADE_CD AS ENT_YEAR_GRADE_CD ");
            stb.append("FROM   SCHNO T2 ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T2.YEAR AND T3.SEMESTER=T2.SEMESTER AND ");
            stb.append("                                        T3.GRADE=T2.GRADE AND T3.HR_CLASS=T2.HR_CLASS ");
            stb.append("       LEFT JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO=T2.SCHREGNO ");
            stb.append("       LEFT JOIN (SELECT I1.SCHREGNO, I2.SCHOOL_KIND, MAX(I2.GRADE_CD) AS GRADE_CD  ");
            stb.append("                FROM SCHREG_REGD_DAT I1 ");
            stb.append("                INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR AND I2.GRADE = I1.GRADE ");
            stb.append("                INNER JOIN SCHREG_ENT_GRD_HIST_DAT I3 ON I3.SCHREGNO = I1.SCHREGNO ");
            stb.append("                    AND I3.SCHOOL_KIND = I2.SCHOOL_KIND ");
            stb.append("                WHERE FISCALYEAR(I3.ENT_DATE) = I1.YEAR ");
            stb.append("                  AND I3.SCHOOL_KIND = I2.SCHOOL_KIND ");
            stb.append("                GROUP BY I1.SCHREGNO, I2.SCHOOL_KIND ");
            stb.append("               ) T_ENT_YEAR_GRADE_CD ON T_ENT_YEAR_GRADE_CD.SCHREGNO = T2.SCHREGNO AND T_ENT_YEAR_GRADE_CD.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("       LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR=T2.YEAR AND GDAT.GRADE=T2.GRADE ");

            stb.append("ORDER BY T2.GRADE, T2.HR_CLASS, ATTENDNO ");
            return stb.toString();
        }
    }

    //健康診断票フォームの種別
    public enum KensinFormKind {
        /** 4年用フォーム（全日制、定時制） */
        Form4,

        /** 6年用フォーム（通信制） */
        Form6,

        /** 9年用フォーム（小・中のみ） */
        Form9,
    }

    //========================================================================
    /** 生徒学生健康診断票の共通処理 */
    private class KensinAbstract {
        public static final int HYOU_TYPE_IPPAN = 1;/** 一般    */
        public static final int HYOU_TYPE_HA    = 2;/** 歯・口腔 */

        protected final Param  _param;
        protected final DB2UDB _db2;
        protected final Form   _form;
        protected        int   _check_no;
        protected        SchoolInfo _schoolInfo;

        /** コンストラクタ */
        public KensinAbstract(final Param param, final DB2UDB db2, final Form form) {
            _param = param;
            _db2   = db2;
            _form  = form;
            _check_no = 0;
            _schoolInfo = new SchoolInfo(db2, _param._year, null, null);
        }

        /**
         *  SQL取得：１or２）生徒学生健康診断票（一般or歯・口腔）
         */
        protected String getSqlResult() {
            final StringBuffer stb = new StringBuffer();
            //在籍（現在年度）
            stb.append("WITH SCHNO AS ( ");
            stb.append("    SELECT ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.YEAR, ");
            stb.append("        T1.SEMESTER, ");
            stb.append("        T1.GRADE, ");
            stb.append("        T1.HR_CLASS, ");
            stb.append("        T1.ATTENDNO ");
            stb.append("    FROM ");
            stb.append("        SCHREG_REGD_DAT T1 ");
            stb.append("    WHERE  T1.YEAR = '"+_param._year+"' AND T1.SEMESTER = '"+_param._gakki+"' AND ");
            stb.append("           T1.SCHREGNO = ? ");//学籍番号
            stb.append("    ) ");
            //現在年度以外の学期を取得
            stb.append(",SCHNO_MIN AS ( ");
            stb.append("    SELECT SCHREGNO, YEAR, MIN(SEMESTER) AS SEMESTER ");
            stb.append("    FROM   SCHREG_REGD_DAT W1 ");
            stb.append("    WHERE  EXISTS(SELECT 'X' FROM SCHNO W2 WHERE W2.SCHREGNO=W1.SCHREGNO AND W2.YEAR<>W1.YEAR) ");
            stb.append("    GROUP BY SCHREGNO, YEAR ");
            stb.append("    ) ");
            //在籍（現在年度以外）
            stb.append(",SCHNO_ALL AS ( ");
            stb.append("    SELECT ");
            stb.append("        W1.SCHREGNO, ");
            stb.append("        W1.YEAR, ");
            stb.append("        W1.SEMESTER, ");
            stb.append("        W1.GRADE, ");
            stb.append("        W1.HR_CLASS, ");
            stb.append("        W1.ATTENDNO, ");
            stb.append("        L1.SCHOOL_KIND, ");
            stb.append("        L1.GRADE_CD, ");
            stb.append("        L1.GRADE_NAME1 ");
            stb.append("    FROM ");
            stb.append("        SCHREG_REGD_DAT W1 ");
            stb.append("        LEFT JOIN SCHREG_REGD_GDAT L1 ON W1.YEAR = L1.YEAR ");
            stb.append("             AND W1.GRADE = L1.GRADE ");
            stb.append("    WHERE  EXISTS(SELECT 'X' FROM SCHNO_MIN W2 WHERE W2.SCHREGNO=W1.SCHREGNO AND W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER) ");
            stb.append("    UNION ");
            stb.append("    SELECT ");
            stb.append("        W1.SCHREGNO, ");
            stb.append("        W1.YEAR, ");
            stb.append("        W1.SEMESTER, ");
            stb.append("        W1.GRADE, ");
            stb.append("        W1.HR_CLASS, ");
            stb.append("        W1.ATTENDNO, ");
            stb.append("        L1.SCHOOL_KIND, ");
            stb.append("        L1.GRADE_CD, ");
            stb.append("        L1.GRADE_NAME1 ");
            stb.append("    FROM ");
            stb.append("        SCHNO W1 ");
            stb.append("        LEFT JOIN SCHREG_REGD_GDAT L1 ON W1.YEAR = L1.YEAR ");
            stb.append("             AND W1.GRADE = L1.GRADE ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.*, T2.HR_NAME, T2.HR_CLASS_NAME1, T3.NAME, ");
            stb.append("       (SELECT NAME2 FROM NAME_MST WHERE NAMECD1='Z002' AND NAMECD2=SEX) AS SEX, BIRTHDAY, ");
            stb.append("       CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR(T2.YEAR || '-04-01' - BIRTHDAY) END AS AGE, ");
            if (_check_no == HYOU_TYPE_IPPAN) {//一般
                stb.append("   HEIGHT, WEIGHT, SITHEIGHT, R_BAREVISION, L_BAREVISION, R_VISION, L_VISION, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F017' AND NAMECD2=R_BAREVISION_MARK) AS R_BAREVISION_MARK, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F017' AND NAMECD2=L_BAREVISION_MARK) AS L_BAREVISION_MARK, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F017' AND NAMECD2=R_VISION_MARK) AS R_VISION_MARK, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F017' AND NAMECD2=L_VISION_MARK) AS L_VISION_MARK, ");
                stb.append("   R_VISION_CANTMEASURE, L_VISION_CANTMEASURE, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA1CD) AS ALBUMINURIA1CD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F019' AND NAMECD2=URICSUGAR1CD) AS URICSUGAR1CD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F018' AND NAMECD2=URICBLEED1CD) AS URICBLEED1CD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA2CD) AS ALBUMINURIA2CD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F019' AND NAMECD2=URICSUGAR2CD) AS URICSUGAR2CD, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F018' AND NAMECD2=URICBLEED2CD) AS URICBLEED2CD, ");
                stb.append("   URICOTHERTEST, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F023' AND NAMECD2=PARASITE) AS PARASITE, ");
                //栄養状態
                stb.append("   NUTRITIONCD AS NUTRITION_CD, ");                                                           //斜線を引く判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F030' AND NAMECD2=NUTRITIONCD) AS NUTRITION, ");//選択肢
                stb.append("   NUTRITIONCD_REMARK AS NUTRITION_REMARK, ");                                                //所見
                //脊柱・胸郭・四肢
                stb.append("   SPINERIBCD AS SPINERIB_CD, ");                                                            //斜線を引く判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F040' AND NAMECD2=SPINERIBCD)  AS SPINERIB, ");//選択肢
                stb.append("   SPINERIBCD_REMARK AS SPINERIB_REMARK, ");                                                 //所見
                stb.append("   SPINERIBCD1 AS SPINERIB_CD1, ");                                                          //疾患1が登録されているかどうかの判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F041' AND NAMECD2=SPINERIBCD1) AS SPINERIB1,");//疾患1
                stb.append("   SPINERIBCD_REMARK1 AS SPINERIB_REMARK1, ");                                               //疾患1の所見
                stb.append("   SPINERIBCD2 AS SPINERIB_CD2, ");                                                          //疾患2が登録されているかどうかの判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F041' AND NAMECD2=SPINERIBCD2) AS SPINERIB2,");//疾患2
                stb.append("   SPINERIBCD_REMARK2 AS SPINERIB_REMARK2, ");                                               //疾患2の所見
                stb.append("   SPINERIBCD3 AS SPINERIB_CD3, ");                                                          //疾患3が登録されているかどうかの判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F041' AND NAMECD2=SPINERIBCD3) AS SPINERIB3,");//疾患3
                stb.append("   SPINERIBCD_REMARK3 AS SPINERIB_REMARK3, ");                                               //疾患3の所見
                //眼の疾病及び異常
                stb.append("   EYEDISEASECD AS EYEDISEASE_CD, ");                                                           //斜線を引く判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F050' AND NAMECD2=EYEDISEASECD) AS EYEDISEASE, ");//選択肢
                stb.append("   EYE_TEST_RESULT, ");                                                                         //所見
                //聴力
                stb.append("   R_EAR AS R_EAR_CD, ");                                                           //斜線を引く判定に使用
                stb.append("   L_EAR AS L_EAR_CD, ");                                                           //斜線を引く判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=R_EAR) AS R_EAR, ");//状態(右)
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=L_EAR) AS L_EAR, ");//状態(左)
                stb.append("   R_EAR_DB, ");
                stb.append("   L_EAR_DB, ");
                //耳鼻咽頭疾患
                stb.append("   NOSEDISEASECD AS NOSEDISEASE_CD, ");                                                            //斜線を引く判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F060' AND NAMECD2=NOSEDISEASECD) AS NOSEDISEASE, "); //選択肢
                stb.append("   NOSEDISEASECD_REMARK AS NOSEDISEASE_REMARK, ");                                                 //所見
                stb.append("   NOSEDISEASECD5 AS NOSEDISEASE_CD1, ");                                                          //疾患1が登録されているかどうかの判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F061' AND NAMECD2=NOSEDISEASECD5) AS NOSEDISEASE1,");//疾患1
                stb.append("   NOSEDISEASECD_REMARK1 AS NOSEDISEASE_REMARK1, ");                                               //疾患1の所見
                stb.append("   NOSEDISEASECD6 AS NOSEDISEASE_CD2, ");                                                          //疾患2が登録されているかどうかの判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F061' AND NAMECD2=NOSEDISEASECD6) AS NOSEDISEASE2,");//疾患2
                stb.append("   NOSEDISEASECD_REMARK2 AS NOSEDISEASE_REMARK2, ");                                               //疾患2の所見
                stb.append("   NOSEDISEASECD7 AS NOSEDISEASE_CD3, ");                                                          //疾患3が登録されているかどうかの判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F061' AND NAMECD2=NOSEDISEASECD7) AS NOSEDISEASE3,");//疾患3
                stb.append("   NOSEDISEASECD_REMARK3 AS NOSEDISEASE_REMARK3, ");                                               //疾患3の所見
                //皮膚疾患
                stb.append("   SKINDISEASECD AS SKINDISEASE_CD, ");                                                           //斜線を引く判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F070' AND NAMECD2=SKINDISEASECD) AS SKINDISEASE, ");//選択肢
                stb.append("   SKINDISEASECD_REMARK AS SKINDISEASE_REMARK, ");                                                //所見
                //結核
                stb.append("   TB_FILMDATE, ");                                                                                 //間接撮影＞撮影日
                stb.append("   TB_FILMNO, ");                                                                                   //間接撮影＞フィルム番号
                stb.append("   TB_REMARKCD AS TB_REMARK_CD, ");                                                                 //斜線を引く判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F100' AND NAMECD2=TB_REMARKCD) AS TB_REMARK, ");      //間接撮影＞所見(選択肢)
                stb.append("   TB_X_RAY, ");                                                                                    //間接撮影＞所見(所見)
                //  ----
                stb.append("   TB_OTHERTESTCD AS TB_OTHERTEST_CD, ");                                                           //斜線を引く判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F110' AND NAMECD2=TB_OTHERTESTCD) AS TB_OTHERTEST, ");//その他の検査
                //  ----
                stb.append("   TB_NAMECD AS TB_NAME_CD, ");                                                                     //斜線を引く判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F120' AND NAMECD2=TB_NAMECD) AS TB_NAME, ");          //病名(選択肢)
                stb.append("   TB_NAME_REMARK1, ");                                                                             //病名(所見)
                //  ----
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F130' AND NAMECD2=TB_ADVISECD) AS TB_ADVISECD, ");    //指導区分
                //心臓
                stb.append("   HEART_MEDEXAM AS HEART_MEDEXAM_CD, ");                                                           //斜線を引く判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F080' AND NAMECD2=HEART_MEDEXAM) AS HEART_MEDEXAM, ");//心電図＞選択肢
                stb.append("   HEART_MEDEXAM_REMARK, ");                                                                        //心電図＞所見
                //  ----
                stb.append("   HEARTDISEASECD AS HEARTDISEASE_CD, ");                                                           //斜線を引く判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F090' AND NAMECD2=HEARTDISEASECD) AS HEARTDISEASE, ");//疾病及び異常＞選択肢
                stb.append("   HEARTDISEASECD_REMARK AS HEARTDISEASE_REMARK, ");                                                //疾病及び異常＞所見
                //
                stb.append("   ANEMIA_REMARK, HEMOGLOBIN, ");
                stb.append("   OTHER_REMARK, ");
                stb.append("   DOC_REMARK, DOC_DATE, ");
                //事後措置
                stb.append("   TREATCD AS TREAT_CD1, ");                                                           //事後措置1が登録されているかどうかの判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F150' AND NAMECD2=TREATCD) AS TREAT1,"); //事後措置1
                stb.append("   TREAT_REMARK1, ");                                                                  //事後措置1の所見
                stb.append("   TREATCD2 AS TREAT_CD2, ");                                                          //事後措置2が登録されているかどうかの判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F150' AND NAMECD2=TREATCD2) AS TREAT2,");//事後措置2
                stb.append("   TREATCD2_REMARK1 AS TREAT_REMARK2, ");                                              //事後措置2の所見
                //備考
                stb.append("   REMARK,  ");
                //
                stb.append("   T5.DATE ");
            } else if (_check_no == HYOU_TYPE_HA) {//歯・口腔
                //歯式
                stb.append("   CASE WHEN JAWS_JOINTCD IS NOT NULL THEN CASE WHEN JAWS_JOINTCD = '01' THEN '○' END END AS JAWS_JOINTCD1, ");
                stb.append("   CASE WHEN JAWS_JOINTCD IS NOT NULL THEN CASE WHEN JAWS_JOINTCD = '02' THEN '○' END END AS JAWS_JOINTCD2, ");
                stb.append("   CASE WHEN JAWS_JOINTCD IS NOT NULL THEN CASE WHEN JAWS_JOINTCD = '03' THEN '○' END END AS JAWS_JOINTCD3, ");
                stb.append("   CASE WHEN JAWS_JOINTCD2 IS NOT NULL THEN CASE WHEN JAWS_JOINTCD2 = '01' THEN '○' END END AS JAWS_JOINTCD21, ");
                stb.append("   CASE WHEN JAWS_JOINTCD2 IS NOT NULL THEN CASE WHEN JAWS_JOINTCD2 = '02' THEN '○' END END AS JAWS_JOINTCD22, ");
                stb.append("   CASE WHEN JAWS_JOINTCD2 IS NOT NULL THEN CASE WHEN JAWS_JOINTCD2 = '03' THEN '○' END END AS JAWS_JOINTCD23, ");
                stb.append("   CASE WHEN JAWS_JOINTCD3 IS NOT NULL THEN CASE WHEN JAWS_JOINTCD3 = '01' THEN '○' END END AS JAWS_JOINTCD31, ");
                stb.append("   CASE WHEN JAWS_JOINTCD3 IS NOT NULL THEN CASE WHEN JAWS_JOINTCD3 = '02' THEN '○' END END AS JAWS_JOINTCD32, ");
                stb.append("   CASE WHEN JAWS_JOINTCD3 IS NOT NULL THEN CASE WHEN JAWS_JOINTCD3 = '03' THEN '○' END END AS JAWS_JOINTCD33, ");
                stb.append("   CASE WHEN PLAQUECD IS NOT NULL THEN CASE WHEN PLAQUECD = '01' THEN '○' END END AS PLAQUECD1, ");
                stb.append("   CASE WHEN PLAQUECD IS NOT NULL THEN CASE WHEN PLAQUECD = '02' THEN '○' END END AS PLAQUECD2, ");
                stb.append("   CASE WHEN PLAQUECD IS NOT NULL THEN CASE WHEN PLAQUECD = '03' THEN '○' END END AS PLAQUECD3, ");
                stb.append("   CASE WHEN GUMCD IS NOT NULL THEN CASE WHEN GUMCD = '01' THEN '○' END END AS GUMCD1, ");
                stb.append("   CASE WHEN GUMCD IS NOT NULL THEN CASE WHEN GUMCD = '02' THEN '○' END END AS GUMCD2, ");
                stb.append("   CASE WHEN GUMCD IS NOT NULL THEN CASE WHEN GUMCD = '03' THEN '○' END END AS GUMCD3, ");
                stb.append("   VALUE(ORTHODONTICS,'0') AS ORTHODONTICS, ");
                stb.append("   UP_R_ADULT8, UP_R_ADULT7, UP_R_ADULT6, UP_R_ADULT5, UP_R_ADULT4, UP_R_ADULT3, UP_R_ADULT2, UP_R_ADULT1, ");
                stb.append("   UP_L_ADULT1, UP_L_ADULT2, UP_L_ADULT3, UP_L_ADULT4, UP_L_ADULT5, UP_L_ADULT6, UP_L_ADULT7, UP_L_ADULT8, ");
                stb.append("   UP_R_BABY5, UP_R_BABY4, UP_R_BABY3, UP_R_BABY2, UP_R_BABY1, ");
                stb.append("   UP_L_BABY1, UP_L_BABY2, UP_L_BABY3, UP_L_BABY4, UP_L_BABY5, ");
                stb.append("   LW_R_BABY5, LW_R_BABY4, LW_R_BABY3, LW_R_BABY2, LW_R_BABY1, ");
                stb.append("   LW_L_BABY1, LW_L_BABY2, LW_L_BABY3, LW_L_BABY4, LW_L_BABY5, ");
                stb.append("   LW_R_ADULT8, LW_R_ADULT7, LW_R_ADULT6, LW_R_ADULT5, LW_R_ADULT4, LW_R_ADULT3, LW_R_ADULT2, LW_R_ADULT1, ");
                stb.append("   LW_L_ADULT1, LW_L_ADULT2, LW_L_ADULT3, LW_L_ADULT4, LW_L_ADULT5, LW_L_ADULT6, LW_L_ADULT7, LW_L_ADULT8, ");
                //歯の状態
                stb.append("   BABYTOOTH,REMAINBABYTOOTH,TREATEDBABYTOOTH,BRACK_BABYTOOTH, ");
                stb.append("   ADULTTOOTH,REMAINADULTTOOTH,TREATEDADULTTOOTH,LOSTADULTTOOTH,BRACK_ADULTTOOTH,CHECKADULTTOOTH,");
                //その他疾病及び異常
                stb.append("   OTHERDISEASECD AS OTHERDISEASE_CD, ");                                                          //斜線を引く判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F530' AND NAMECD2=OTHERDISEASECD) AS OTHERDISEASE,");//選択肢
                stb.append("   OTHERDISEASE AS OTHERDISEASE_REMARK, ");                                                        //所見
                //口腔の疾病及び異常
                stb.append("   OTHERDISEASECD2 AS ORALDISEASE_CD, ");                                                          //斜線を引く判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F531' AND NAMECD2=OTHERDISEASECD2) AS ORALDISEASE,");//選択肢
                stb.append("   OTHERDISEASE2 AS ORALDISEASE_REMARK, ");                                                        //所見
                //学校歯科医（所見）
                stb.append("   VALUE(CALCULUS, '') AS CALCULUS_CD, ");                                                                    //歯石沈着
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F521' AND NAMECD2=VALUE(CALCULUS, '')) AS CALCULUS, ");         //歯石沈着の名称
                stb.append("   DENTISTREMARKCD AS DENTISTREMARK_CD1, ");                                                                  //所見1(選択肢)が登録されているかどうかの判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F540' AND NAMECD2=DENTISTREMARKCD) AS DENTISTREMARK_CHOICE1,"); //所見1(選択肢)
                stb.append("   DENTISTREMARK AS DENTISTREMARK1, ");                                                                       //所見1(所見)
                stb.append("   DENTISTREMARKCD2 AS DENTISTREMARK_CD2, ");                                                                 //所見2(選択肢)が登録されているかどうかの判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F540' AND NAMECD2=DENTISTREMARKCD2) AS DENTISTREMARK_CHOICE2,");//所見2(選択肢)
                stb.append("   DENTISTREMARK2, ");                                                                                        //所見2(所見)
                stb.append("   DENTISTREMARKCD3 AS DENTISTREMARK_CD3, ");                                                                 //所見3(選択肢)が登録されているかどうかの判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F540' AND NAMECD2=DENTISTREMARKCD3) AS DENTISTREMARK_CHOICE3,");//所見3(選択肢)
                stb.append("   DENTISTREMARK3, ");                                                                                        //所見3(所見)
                //学校歯科医＞月日
                stb.append("   DENTISTREMARKDATE,");
                stb.append("   MONTH(DENTISTREMARKDATE) AS DENTISTREMARKMONTH,");
                stb.append("     DAY(DENTISTREMARKDATE) AS DENTISTREMARKDAY, ");
                //事後措置
                stb.append("   DENTISTTREATCD AS DENTISTTREAT_CD1, ");                                                           //事後措置1(選択肢)が登録されているかどうかの判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F541' AND NAMECD2=DENTISTTREATCD) AS DENTISTTREAT1,"); //事後措置1(選択肢)
                stb.append("   DENTISTTREAT AS DENTISTTREAT_REMARK1, ");                                                                         //事後措置1(所見)
                stb.append("   DENTISTTREATCD2 AS DENTISTTREAT_CD2, ");                                                          //事後措置2(選択肢)が登録されているかどうかの判定に使用
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F541' AND NAMECD2=DENTISTTREATCD2) AS DENTISTTREAT2,");//事後措置2(選択肢)
                stb.append("   DENTISTTREAT2_1 AS DENTISTTREAT_REMARK2 ");                                                       //事後措置2(所見)
            }
            stb.append("FROM   SCHNO_ALL T1 ");
            stb.append("       INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR=T1.YEAR AND T2.SEMESTER=T1.SEMESTER AND T2.GRADE=T1.GRADE AND T2.HR_CLASS=T1.HR_CLASS ");
            stb.append("       INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO=T1.SCHREGNO ");
            if (_check_no == HYOU_TYPE_IPPAN) { //一般
                stb.append("   INNER JOIN V_MEDEXAM_DET_DAT T4 ON T4.YEAR=T1.YEAR AND T4.SCHREGNO=T1.SCHREGNO ");
            } else if (_check_no == HYOU_TYPE_HA) {//歯・口腔{
                stb.append("   INNER JOIN V_MEDEXAM_TOOTH_DAT T4 ON T4.YEAR=T1.YEAR AND T4.SCHREGNO=T1.SCHREGNO ");
            }
            stb.append("   LEFT JOIN MEDEXAM_HDAT T5 ON T5.YEAR=T1.YEAR AND T5.SCHREGNO=T1.SCHREGNO ");
            stb.append("   LEFT JOIN NAME_MST VN ON VN.NAMECD1 = 'A023' ");
            stb.append("        AND VN.NAME1 = ? ");
            stb.append(" WHERE ");
            stb.append("    T1.GRADE BETWEEN VN.NAME2 AND VN.NAME3 ");
            stb.append("ORDER BY T1.YEAR ");
            return stb.toString();
        }

        /**
         *  帳票タイトルの決定
         */
        protected String getReportTitle(final String schoolKind) {
            //帳票名
            final String title =
                ("P".equals(schoolKind) || "J".equals(schoolKind)) && (_param._useForm_Ippan == KensinFormKind.Form9) ?
                        "児童生徒健康診断票" :
                        "K".equals(schoolKind) ?
                                "園児健康診断票" :
                                "P".equals(schoolKind) ?
                                        "児童健康診断票" :
                                        _param.isPrintKenkouSindanIppan() ?
                                                "生徒健康診断票" :
                                                "生徒学生健康診断票";
            return title;

        }

        /**
         *  健康診断表、対象学校種別取得
         */
        protected String[] getTargetSchoolKind(final KensinFormKind formKind) {
            String[] schoolKinds = null;
            switch (formKind) {
            case Form4:
                schoolKinds = new String[] { "H" };
                break;
            case Form6:
                schoolKinds = new String[] { "H" };
                break;
            case Form9:
                schoolKinds = new String[] {"P", "J"};
                break;
            default:
                schoolKinds = new String[] { "H" };
                break;
            }

            return schoolKinds;
        }

        protected void printSlash(final String field) {
            final String slashFile = _param.getImageFile("slash.jpg");
            if (null != slashFile) {
                _form.VrsOut(field, slashFile);
            }
        }
    }

    //========================================================================
    /** 生徒学生健康診断票（一般） */
    private class KensinIppan extends KensinAbstract {
        //コンストラクタ
        public KensinIppan(final Param param, final DB2UDB db2, final Form form) {
            super(param, db2, form);
            super._check_no = HYOU_TYPE_IPPAN;
        }

        //使用するフォーム名取得
        private String getFormName(final String schoolKind) {
            String formName = null;

            switch (_param._useForm_Ippan) {
            case Form4:
                formName = "KNJF030J_1.xml";
                break;
            case Form6:
                formName = "KNJF030J_1_2.xml";
                break;
            case Form9:
                formName = "KNJF030J_1_3.xml";
                break;
            default:
                formName = "KNJF030J_1.xml";
                break;
            }

            return formName;
        }

        //帳票出力
        public boolean printMain(final Map schnoMap) {
            boolean printOk = false;
            final String schregno = getString("SCHREGNO", schnoMap);

            final String[] schoolKinds = getTargetSchoolKind(_param._useForm_Ippan);

            for (int si = 0; si < schoolKinds.length; si++) {
                final String formName = getFormName(schoolKinds[si]);
                if ("J".equals(schoolKinds[si]) && _param._useForm_Ippan == KensinFormKind.Form9) {
                    // 小中学校で9年用フォームを使用する時、改ページ防止のため中学の時はsetFormしない。
                } else {
                    _form.setForm(formName, 4);//一般
                }

                ResultSet rs = null;
                int prevDataCnt = 0;
                try {
                    final String psKey = "statementResult1";
                    if (null == _param._psMap.get(psKey)) {
                        _param._psMap.put(psKey, _db2.prepareStatement(getSqlResult()));
                    }
                    final PreparedStatement ps = _param._psMap.get(psKey);
                    ps.setString(1, schregno);
                    ps.setString(2, schoolKinds[si]);
                    rs = ps.executeQuery();
                    int dataCnt = "J".equals(schoolKinds[si]) && _param._useForm_Ippan == KensinFormKind.Form9 ? prevDataCnt : 0;
                    while (rs.next()) {
                        dataCnt++;
                        if (("P".equals(schoolKinds[si]) || "J".equals(schoolKinds[si])) && _param._useForm_Ippan == KensinFormKind.Form9 && NumberUtils.isDigits(rs.getString("GRADE_CD"))) {
                            final int line = ("J".equals(schoolKinds[si]) ? 6 : 0) + Integer.parseInt(rs.getString("GRADE_CD"));
                            for (int i = dataCnt; i < line; i++) {
                                _form.VrsOut("SCHREGNO"   ,  rs.getString("SCHREGNO"));   //改ページ用
                                _form.VrEndRecord();
                            }
                            dataCnt = line;
                        }
                        if (printMainSvf(rs, dataCnt, schoolKinds[si], formName) == true) {
                            printOk = true;
                        }
                    }
                    prevDataCnt = dataCnt;
                } catch (Exception ex) {
                    log.warn("printMain read error!", ex);
                } finally {
                    DbUtils.closeQuietly(rs);
                    _db2.commit();
                }
            }
            return printOk;
        }

        //帳票出力(サブ処理)
        private boolean printMainSvf(final ResultSet rs, final int dataCnt, final String schoolKind, final String formName)
        {
            boolean printOk = false;
            try {
                _form.VrsOut("SCHREGNO", rs.getString("SCHREGNO"));//改ページ用

                //帳票名
                _form.VrsOut("TITLE",  getReportTitle(schoolKind));

                //右上の各学年の年度・年組・番号
                if (("P".equals(schoolKind) || "J".equals(schoolKind)) && _param._useForm_Ippan == KensinFormKind.Form9) {
                    _form.VrsOutn("YEAR", dataCnt, rs.getString("GRADE_NAME1"));
                } else {
                    _form.VrsOutn("YEAR", dataCnt, nendo(_db2, _param, rs.getString("YEAR")));
                }
                _form.VrsOutn("HR_NAME",  dataCnt, rs.getString("HR_NAME"));
                _form.VrsOutn("ATTENDNO", dataCnt, rs.getString("ATTENDNO"));
                for (int i = dataCnt + 1; i < dataCnt + 3; i++) {
                    _form.VrsOutn("YEAR", i, "");
                    _form.VrsOutn("HR_NAME", i, "");
                    _form.VrsOutn("ATTENDNO", i, "");
                }

                //氏名
                {
                    final int nameLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("NAME"));
                    final String nameField = (nameLen <= 24)? "": "_2";
                    _form.VrsOut("NAME_SHOW" + nameField, rs.getString("NAME"));
                }

                //性別
                _form.VrsOut("SEX", rs.getString("SEX"));

                //生年月日
                _form.VrsOut("BIRTHDAY", formatDate(_db2, _param, rs.getString("BIRTHDAY")));

                //学校名
                _form.VrsOut("SCHOOL_NAME", _schoolInfo.getName(schoolKind, SchoolInfo.SCHOOL_NAME2));

                //------------------------------------------------
                //※以降、学年ごとに異なる列に出力する項目

                //年度
                final String nendoYear = rs.getString("DATE").substring(0, 4);
                _form.VrsOut("M_DATE", nendo(_db2, _param, nendoYear));

                //年齢
                _form.VrsOut("AGE", rs.getString("AGE"));//4月1日現在の年齢

                //身長
                _form.VrsOut("HEIGHT", rs.getString("HEIGHT"));

                //体重
                _form.VrsOut("WEIGHT", rs.getString("WEIGHT"));

                //栄養状態
                if (_param.nameMstNamespare1Is1(_db2, "F030", rs.getString("NUTRITION_CD")) == true) {
                    printSlash("SLASH_NUTRITION");
                } else {
                    _form.VrsOut("NUTRITION", rs.getString("NUTRITION"));

                    String nutritionRemark = SafeSubstring(rs.getString("NUTRITION_REMARK"), 20);
                    if (!nutritionRemark.equals(""))
                    {
                        _form.VrsOut("NUTRITION_REMARK", "（" + nutritionRemark + "）");
                    }
                }

                // 脊柱・胸郭・四肢
                if (_param.nameMstNamespare1Is1(_db2, "F040", rs.getString("SPINERIB_CD")) == true) {
                    //異常なしの場合

                    //斜線を印字
                    printSlash("SLASH_SPINERIB");
                } else {
                    String spineribCd = rs.getString("SPINERIB_CD");
                    String spineribText = "";

                    if ("00".equals(spineribCd)) {
                        //未受験の場合

                        //名称マスタの値を印字する
                        spineribText = rs.getString("SPINERIB");
                    } else if ("02".equals(spineribCd)) {
                        //異常ありの場合

                        //疾患1～3の内容を順に印字する
                        final List listSpineribSikkan = new ArrayList();
                        for (int i = 0; i < 3; i++) {
                            String spineribCdN     = rs.getString("SPINERIB_CD" + (i + 1));
                            String spineribN       = rs.getString("SPINERIB" + (i + 1));
                            String spineribRemarkN = rs.getString("SPINERIB_REMARK" + (i + 1));

                            String temp = "";
                            if (null != spineribCdN) {
                                temp = spineribN;

                                //所見が登録されていれば、所見もつなげる
                                if (null != spineribRemarkN && !spineribRemarkN.equals("")) {
                                    temp = temp + "（" + spineribRemarkN + "）";
                                }

                                //配列に保持
                                if (temp != "") {
                                    listSpineribSikkan.add(temp);
                                }
                            }
                        }

                        //各疾患を「、」で連結
                        spineribText = StringUtils.join(listSpineribSikkan, "、");
                    }

                    final int spineribTextLen = KNJ_EditEdit.getMS932ByteLength(spineribText);
                    final String spineribTextField = (spineribTextLen <= 40)? "1": (spineribTextLen <= 60)? "2": (spineribTextLen <= 120)? "3": "4";
                    _form.VrsOut("SPINERIB" + spineribTextField, spineribText);
                }

                //視力
                final String sokuteiKonnan = "測定困難";
                //視力・右
                if ("1".equals(rs.getString("R_VISION_CANTMEASURE"))) {
                    _form.VrsOut("R_BAREVISION_2", sokuteiKonnan);
                } else {
                    //裸眼
                    final String rBarVisionMark = rs.getString("R_BAREVISION_MARK");
                    if (rBarVisionMark != null  && !("".equals(rBarVisionMark))) {
                        _form.VrsOut("R_BAREVISION", rBarVisionMark);  //記号
                    } else {
                        _form.VrsOut("R_BAREVISION", rs.getString("R_BAREVISION"));  //数字
                    }
                    //矯正
                    final String rVisionMark = rs.getString("R_VISION_MARK");
                    if (rVisionMark != null  && !("".equals(rVisionMark))) {
                        _form.VrsOut("R_VISION", rVisionMark);  //記号
                    } else {
                        _form.VrsOut("R_VISION", rs.getString("R_VISION"));  //数字
                    }
                }
                //視力・左
                if ("1".equals(rs.getString("L_VISION_CANTMEASURE"))) {
                    _form.VrsOut("L_BAREVISION_2", sokuteiKonnan);
                } else {
                    //裸眼
                    final String lBarVisionMark = rs.getString("L_BAREVISION_MARK");
                    if (lBarVisionMark != null  && !("".equals(lBarVisionMark))) {
                        _form.VrsOut("L_BAREVISION", lBarVisionMark);  //記号
                    } else {
                        _form.VrsOut("L_BAREVISION", rs.getString("L_BAREVISION"));  //数字
                    }
                    //矯正
                    final String lVisionMark = rs.getString("L_VISION_MARK");
                    if (lVisionMark != null  && !("".equals(lVisionMark))) {
                        _form.VrsOut("L_VISION", lVisionMark);  //記号
                    } else {
                        _form.VrsOut("L_VISION", rs.getString("L_VISION"));  //数字
                    }
                }

                //眼の疾病及び異常
                if (_param.nameMstNamespare1Is1(_db2, "F050", rs.getString("EYEDISEASE_CD")) == true) {
                    printSlash("SLASH_EYEDISEASE");
                } else {
                    _form.VrsOut("EYEDISEASE", rs.getString("EYEDISEASE"));

                    String eyeTestResult = rs.getString("EYE_TEST_RESULT");
                    if (eyeTestResult != null && !eyeTestResult.equals(""))
                    {
                        _form.VrsOut("EYE_TEST_RESULT", "（" + eyeTestResult + "）");
                    }
                }

                //聴力(右)
                final String rEarCd = rs.getString("R_EAR_CD");
                if (_param.nameMstNamespare1Is1(_db2, "F010", rEarCd) == true) {
                    printSlash("SLASH_R_EAR");
                } else {
                    String rEar = "";
                    if ("05".equals(rEarCd)) {
                        //「○」の場合

                        //ブランク表示(何もしない)
                    } else {
                        rEar = rs.getString("R_EAR");
                    }

                    final int rEarLen = KNJ_EditEdit.getMS932ByteLength(rEar);
                    final String rEarField = (rEarLen <= 20)? "" : (_param._useForm_Ippan != KensinFormKind.Form4) || (rEarLen <= 30)? "_2" : "_3";
                    _form.VrsOut("R_EAR" + rEarField, rEar);
                }
                //聴力(左)
                final String lEarCd = rs.getString("L_EAR_CD");
                if (_param.nameMstNamespare1Is1(_db2, "F010", lEarCd) == true) {
                    printSlash("SLASH_L_EAR");
                } else {
                    String lEar = "";
                    if ("05".equals(lEarCd)) {
                        //「○」の場合→ブランク表示(何もしない)
                    } else {
                        lEar = rs.getString("L_EAR");
                    }

                    final int lEarLen = KNJ_EditEdit.getMS932ByteLength(lEar);
                    final String lEarField = (lEarLen <= 20)? "" : (_param._useForm_Ippan != KensinFormKind.Form4) || (lEarLen <= 30)? "_2" : "_3";
                    _form.VrsOut("L_EAR" + lEarField, lEar);
                }

                //耳鼻咽頭疾患
                if (_param.nameMstNamespare1Is1(_db2, "F060", rs.getString("NOSEDISEASE_CD")) == true) {
                    //異常なしの場合

                    //斜線を印字
                    printSlash("SLASH_NOSEDISEASE");
                } else {
                    String noseDiseaseCd = rs.getString("NOSEDISEASE_CD");
                    String noseDiseaseText = "";

                    if ("00".equals(noseDiseaseCd)) {
                        //未受験の場合

                        //名称マスタの値を印字する
                        noseDiseaseText = rs.getString("NOSEDISEASE");
                    } else if ("02".equals(noseDiseaseCd)) {
                        //異常ありの場合

                        //疾患1～3の内容を順に印字する
                        final List listNoseDiseaseSikkan = new ArrayList();
                        for (int i = 0; i < 3; i++) {
                            String noseDiseaseCdN     = rs.getString("NOSEDISEASE_CD" + (i + 1));
                            String noseDiseaseN       = rs.getString("NOSEDISEASE" + (i + 1));
                            String noseDiseaseRemarkN = rs.getString("NOSEDISEASE_REMARK" + (i + 1));

                            String temp = "";
                            if (null != noseDiseaseCdN) {
                                temp = noseDiseaseN;

                                //所見が登録されていれば、所見もつなげる
                                if (null != noseDiseaseRemarkN && !noseDiseaseRemarkN.equals("")) {
                                    temp = temp + "（" + noseDiseaseRemarkN + "）";
                                }

                                //配列に保持
                                if (temp != "") {
                                    listNoseDiseaseSikkan.add(temp);
                                }
                            }
                        }

                        //各疾患を「、」で連結
                        noseDiseaseText = StringUtils.join(listNoseDiseaseSikkan, "、");
                    }

                    final int noseDiseaseTextLen = KNJ_EditEdit.getMS932ByteLength(noseDiseaseText);
                    final String noseDiseaseTextField = (noseDiseaseTextLen <= 40)? "1": (noseDiseaseTextLen <= 60)? "2": (noseDiseaseTextLen <= 120)? "3": "4";
                    _form.VrsOut("NOSEDISEASE" + noseDiseaseTextField, noseDiseaseText);
                }

                //皮膚疾患
                if (_param.nameMstNamespare1Is1(_db2, "F070", rs.getString("SKINDISEASE_CD")) == true) {
                    printSlash("SLASH_SKINDISEASE");
                } else {
                    final String skinDiseaseText = rs.getString("SKINDISEASE");

                    final int skinDiseaseTextLen = KNJ_EditEdit.getMS932ByteLength(skinDiseaseText);
                    final String skinDiseaseTextField = (skinDiseaseTextLen <= 10)? "": "1_2";
                    _form.VrsOut("SKINDISEASE" + skinDiseaseTextField, skinDiseaseText);

                    String skinDiseaseRemark = SafeSubstring(rs.getString("SKINDISEASE_REMARK"), 20);
                    if (!skinDiseaseRemark.equals("")) {
                        _form.VrsOut("SKINDISEASE2_1", "（" + skinDiseaseRemark + "）");
                    }
                }

                //結核
                _form.VrsOut("PHOTO_DATE"  , formatDate(_db2, _param, rs.getString("TB_FILMDATE")));//間接撮影＞撮影日
                _form.VrsOut("FILMNO"      , rs.getString("TB_FILMNO"));//間接撮影＞フィルム番号
                //結核＞間接撮影＞所見
                if (_param.nameMstNamespare1Is1(_db2, "F100", rs.getString("TB_REMARK_CD")) == true) {
                    printSlash("SLASH_VIEWS1_1");
                } else {
                    //所見(選択肢)
                    _form.VrsOut("VIEWS1_1", rs.getString("TB_REMARK"));

                    //所見(所見)
                    String tbXRay = SafeSubstring(rs.getString("TB_X_RAY"), 20);
                    if (!tbXRay.equals("")) {
                        _form.VrsOut("TB_X_RAY", "（" + tbXRay + "）");
                    }
                }
                //結核＞その他の検査
                if (_param.nameMstNamespare1Is1(_db2, "F110", rs.getString("TB_OTHERTEST_CD")) == true) {
                    printSlash("SLASH_OTHERS");
                } else {
                    final String tbOtherTestText = rs.getString("TB_OTHERTEST");

                    final int tbOtherTestTextLen = KNJ_EditEdit.getMS932ByteLength(tbOtherTestText);
                    final String tbOtherTestTextField = (tbOtherTestTextLen <= 20)? "1": "2";
                    _form.VrsOut("OTHERS" + tbOtherTestTextField, tbOtherTestText);
                }
                //結核＞病名
                if (_param.nameMstNamespare1Is1(_db2, "F120", rs.getString("TB_NAME_CD")) == true) {
                    printSlash("SLASH_DISEASE_NAME");
                } else {
                    //病名
                    String tbName = SafeSubstring(rs.getString("TB_NAME"), 20);
                    _form.VrsOut("DISEASE_NAME", tbName);
                    //所見
                    String tbNameRemark1 = SafeSubstring(rs.getString("TB_NAME_REMARK1"), 20);
                    if (!tbNameRemark1.equals("")) {
                        _form.VrsOut("DISEASE_NAME2", "（" + tbNameRemark1 + "）");
                    }
                }
                _form.VrsOut("GUIDANCE"    , rs.getString("TB_ADVISECD"));//指導区分

                //心臓＞臨床医学的検査(心電図)
                if (_param.nameMstNamespare1Is1(_db2, "F080", rs.getString("HEART_MEDEXAM_CD")) == true) {
                    printSlash("SLASH_HEART_MEDEXAM");
                } else {
                    //所見(選択肢)
                    _form.VrsOut("HEART_MEDEXAM", rs.getString("HEART_MEDEXAM"));

                    //所見(所見)
                    String heartMedexamRemark = SafeSubstring(rs.getString("HEART_MEDEXAM_REMARK"), 20);
                    if (!heartMedexamRemark.equals("")) {
                        _form.VrsOut("HEARTDISEASECD_REMARK", "（" + heartMedexamRemark + "）");
                    }
                }

                //心臓＞疾病及び異常
                if (_param.nameMstNamespare1Is1(_db2, "F090", rs.getString("HEARTDISEASE_CD")) == true) {
                    printSlash("SLASH_HEARTDISEASE");
                } else {
                    String heartDisease = rs.getString("HEARTDISEASE");//選択肢
                    String heartDiseaseRemark = SafeSubstring(rs.getString("HEARTDISEASE_REMARK"), 20);//所見
                    final int heartDiseaseLen = KNJ_EditEdit.getMS932ByteLength(heartDisease);
                    final String heartDiseaseField = (heartDiseaseLen <= 20)? "": "_2";

                    if (!heartDiseaseRemark.equals("")) {
                        //所見あり
                        _form.VrsOut("HEARTDISEASE_REMARK", "（" + heartDiseaseRemark + "）");

                        _form.VrsOut("HEARTDISEASE1" + heartDiseaseField, heartDisease);
                    } else {
                        //所見なし
                        _form.VrsOut("HEARTDISEASE2" + heartDiseaseField, heartDisease);
                    }
                }

                //尿
                _form.VrsOut("ALBUMINURIA" , rs.getString("ALBUMINURIA1CD"));//1次＞蛋白
                _form.VrsOut("URICSUGAR"   , rs.getString("URICSUGAR1CD"));//1次＞糖
                _form.VrsOut("URICBLEED"   , rs.getString("URICBLEED1CD"));//1次＞潜血
                _form.VrsOut("ALBUMINURIA2", rs.getString("ALBUMINURIA2CD"));//再検査＞蛋白
                _form.VrsOut("URICSUGAR2"  , rs.getString("URICSUGAR2CD"));//再検査＞糖
                _form.VrsOut("URICBLEED2"  , rs.getString("URICBLEED2CD"));//再検査＞潜血

                //その他の疾病及び異常
                final String otherRemark = rs.getString("OTHER_REMARK");
                final int otherRemarkLen = KNJ_EditEdit.getMS932ByteLength(otherRemark);
                final String otherRemarkField = (otherRemarkLen <= 20)? "": "2";
                _form.VrsOut("OTHERDISEASE" + otherRemarkField,  otherRemark);

                //学校医
                _form.VrsOut("VIEWS2_1", rs.getString("DOC_REMARK"));//所見
                _form.VrsOut("DOC_DATE", formatDate(_db2, _param, rs.getString("DOC_DATE")));//月日

                //事後処置
                if (_param.nameMstNamespare1Is1(_db2, "F150", rs.getString("TREAT_CD1")) == true) {
                    //異常なしの場合

                    //斜線を印字
                    printSlash("SLASH_DOC_TREAT");
                } else {
                    //事後措置1、2の内容を順に印字する
                    final List listTreat = new ArrayList();
                    for (int i = 0; i < 2; i++) {
                        String treatCdN     = rs.getString("TREAT_CD" + (i + 1));
                        String treatN       = rs.getString("TREAT" + (i + 1));
                        String treatRemarkN = rs.getString("TREAT_REMARK" + (i + 1));

                        String temp = "";
                        if (null != treatCdN) {
                            temp = treatN;

                            //所見が登録されていれば、所見もつなげる
                            if (null != treatRemarkN && !treatRemarkN.equals("")) {
                                temp = temp + "（" + treatRemarkN + "）";
                            }

                            //配列に保持
                            if (temp != "") {
                                listTreat.add(temp);
                            }
                        }
                    }

                    //各事後措置を「、」で連結
                    final String treatText = StringUtils.join(listTreat, "、");
                    final int treatTextLen = KNJ_EditEdit.getMS932ByteLength(treatText);
                    final String treatTextField = (treatTextLen <= 20)? "1": (treatTextLen <= 30)? "2": "3";
                    _form.VrsOut("DOC_TREAT" + treatTextField, treatText);
                }

                //備考
                {
                    String remark1 = "";
                    if (null != rs.getString("SITHEIGHT")) {
                        final int ketaMax = (_param._useForm_Ippan == KensinFormKind.Form9)? 14 : 20;
                        remark1 = "座高（" + rs.getString("SITHEIGHT") + "cm）";
                        remark1 += StringUtils.repeat(" ", ketaMax - KNJ_EditEdit.getMS932ByteLength(remark1));
                        remark1 += StringUtils.defaultString(rs.getString("REMARK"));
                    } else {
                        remark1 = rs.getString("REMARK");
                    }

                    final int remark1TextLen = KNJ_EditEdit.getMS932ByteLength(remark1);
                    final String remark1TextField = (remark1TextLen <= 120)? "1": "2_1";
                    _form.VrsOut("NOTE" + remark1TextField, remark1);
                }

                _form.VrEndRecord();
                printOk = true;
            } catch (Exception ex) {
                log.warn("printMainSvf read error!", ex);
            }
            return printOk;
        }
    }

    //========================================================================
    /** 生徒学生健康診断票（歯・口腔） */

    private class KensinHa extends KensinAbstract {
        //コンストラクタ
        public KensinHa(final Param param, final DB2UDB db2, final Form form) {
            super(param, db2, form);
            super._check_no = HYOU_TYPE_HA;
        }

        //使用するフォーム名取得
        private String getFormName(final String schoolKind) {
            String formName = null;

            switch (_param._useForm_Ha) {
            case Form4:
                formName = "KNJF030J_2.xml";
                break;
            case Form6:
                formName = "KNJF030J_2_2.xml";
                break;
            case Form9:
                formName = "KNJF030J_2_3.xml";
                break;
            default:
                formName = "KNJF030J_2.xml";
                break;
            }

            return formName;
        }

        //帳票出力
        public boolean printMain(final Map schnoMap) {
            boolean printOk = false;
            final String schregno = getString("SCHREGNO", schnoMap);
            final String[] schoolKinds = getTargetSchoolKind(_param._useForm_Ha);
            if (_param._isOutputDebug) {
                log.info(" printMain schoolKinds = " + ArrayUtils.toString(schoolKinds));
            }

            int prevDataCnt = 0;
            for (int si = 0; si < schoolKinds.length; si++) {
                final String schoolKind = schoolKinds[si];
                final String formName = getFormName(schoolKind);
                _form.setForm(formName, 4);
                try {
                    final String psKey = "statementResult2";
                    if (null == _param._psMap.get(psKey)) {
                        final String sql2 = getSqlResult();
                        if (_param._isOutputDebug) {
                            log.info(" sql2 = " + sql2);
                        }
                        _param._psMap.put(psKey, _db2.prepareStatement(sql2));
                    }
                    int dataCnt = "J".equals(schoolKind) && _param._useForm_Ha == KensinFormKind.Form9 ? prevDataCnt : 0;
                    for (final Map rs : KnjDbUtils.query(_db2, _param._psMap.get(psKey), new Object[] {schregno, schoolKind})) {
                        dataCnt++;
                        if (("P".equals(schoolKind) || "J".equals(schoolKind)) && _param._useForm_Ha == KensinFormKind.Form9 && NumberUtils.isDigits(getString("GRADE_CD", rs))) {
                            final int line = ("J".equals(schoolKind) ? 6 : 0) + Integer.parseInt(getString("GRADE_CD", rs));
                            for (int i = dataCnt; i < line; i++) {
                                _form.VrsOut("SCHREGNO", getString("SCHREGNO", rs));   //改ページ用
                                _form.VrEndRecord();
                            }
                            dataCnt = line;
                        }
                        if ("J".equals(schoolKind)) {
                            final int line = ("H".equals(schoolKind) ? 3 : 0) + Integer.parseInt(getString("GRADE_CD", rs));
                            for (int i = dataCnt; i < line; i++) {
                                _form.VrsOut("SCHREGNO", getString("SCHREGNO", rs));   //改ページ用
                                _form.VrEndRecord();
                            }
                            dataCnt = line;
                        }
                        if (printMainSvf(rs, dataCnt, schoolKind)) {
                            printOk = true;
                        }
                    }
                    prevDataCnt = dataCnt;
                } catch (Exception ex) {
                    log.warn("printMain read error!", ex);
                }
            }
            return printOk;
        }

        //帳票出力(サブ処理)
        private boolean printMainSvf(final Map rs, final int dataCnt, final String schoolKind) {
            final String delim = "／";
            boolean printOk = false;
            try {
                _form.VrsOut("SCHREGNO", getString("SCHREGNO", rs));   //改ページ用


                //右上の各学年の学年・組・番号
                _form.VrsOut("HR_NAME"   ,  getString("HR_NAME", rs));
                _form.VrsOut("ATTENDNO"  ,  getString("ATTENDNO", rs));

                //氏名
                {
                    final int nameLen = KNJ_EditEdit.getMS932ByteLength(getString("NAME", rs));
                    final String nameField = (nameLen <= 24)? "": "_2";
                    _form.VrsOut("NAME_SHOW" + nameField,  getString("NAME", rs));
                }

                //性別
                _form.VrsOut("SEX", getString("SEX", rs));

                //生年月日
                _form.VrsOut("BIRTHDAY", formatDate(_db2, _param, getString("BIRTHDAY", rs)));

                //学校名
                _form.VrsOut("SCHOOL_NAME", _schoolInfo.getName(schoolKind, SchoolInfo.SCHOOL_NAME2));

                //------------------------------------------------
                //※以降、学年ごとに異なる段に出力する項目

                //年齢
                _form.VrsOut("AGE",  getString("AGE", rs));//4月1日現在の年齢

                //年度
                if (_param._isSeireki) {
                    _form.VrsOut("NENDO1", getString("YEAR", rs));
                } else {
                    final String gengou = nendo(_db2, _param, getString("YEAR", rs));
                    _form.VrsOut("NENDO1", gengou.substring(0,2));
                    _form.VrsOut("NENDO2", gengou.substring(2));
                }

                //顎関節
                _form.VrsOut("JAWS_JOINTCD20", getString("JAWS_JOINTCD21", rs));
                _form.VrsOut("JAWS_JOINTCD21", getString("JAWS_JOINTCD22", rs));
                _form.VrsOut("JAWS_JOINTCD22", getString("JAWS_JOINTCD23", rs));

                //歯列・咬合
                _form.VrsOut("JAWS_JOINTCD0", getString("JAWS_JOINTCD1", rs));
                _form.VrsOut("JAWS_JOINTCD1", getString("JAWS_JOINTCD2", rs));
                _form.VrsOut("JAWS_JOINTCD2", getString("JAWS_JOINTCD3", rs));

                //歯垢の状態
                _form.VrsOut("PLAQUECD0", getString("PLAQUECD1", rs));
                _form.VrsOut("PLAQUECD1", getString("PLAQUECD2", rs));
                _form.VrsOut("PLAQUECD2", getString("PLAQUECD3", rs));

                //歯肉の状態
                _form.VrsOut("GUMCD0", getString("GUMCD1", rs));
                _form.VrsOut("GUMCD1", getString("GUMCD2", rs));
                _form.VrsOut("GUMCD2", getString("GUMCD3", rs));

                //歯式
                svfVrsOutTooth("UP_L_ADULT8", getString("UP_L_ADULT8", rs));
                svfVrsOutTooth("UP_L_ADULT7", getString("UP_L_ADULT7", rs));
                svfVrsOutTooth("UP_L_ADULT6", getString("UP_L_ADULT6", rs));
                svfVrsOutTooth("UP_L_ADULT5", getString("UP_L_ADULT5", rs));
                svfVrsOutTooth("UP_L_ADULT4", getString("UP_L_ADULT4", rs));
                svfVrsOutTooth("UP_L_ADULT3", getString("UP_L_ADULT3", rs));
                svfVrsOutTooth("UP_L_ADULT2", getString("UP_L_ADULT2", rs));
                svfVrsOutTooth("UP_L_ADULT1", getString("UP_L_ADULT1", rs));
                svfVrsOutTooth("UP_R_ADULT1", getString("UP_R_ADULT1", rs));
                svfVrsOutTooth("UP_R_ADULT2", getString("UP_R_ADULT2", rs));
                svfVrsOutTooth("UP_R_ADULT3", getString("UP_R_ADULT3", rs));
                svfVrsOutTooth("UP_R_ADULT4", getString("UP_R_ADULT4", rs));
                svfVrsOutTooth("UP_R_ADULT5", getString("UP_R_ADULT5", rs));
                svfVrsOutTooth("UP_R_ADULT6", getString("UP_R_ADULT6", rs));
                svfVrsOutTooth("UP_R_ADULT7", getString("UP_R_ADULT7", rs));
                svfVrsOutTooth("UP_R_ADULT8", getString("UP_R_ADULT8", rs));
                svfVrsOutTooth("UP_L_BABY5", getString("UP_L_BABY5", rs));
                svfVrsOutTooth("UP_L_BABY4", getString("UP_L_BABY4", rs));
                svfVrsOutTooth("UP_L_BABY3", getString("UP_L_BABY3", rs));
                svfVrsOutTooth("UP_L_BABY2", getString("UP_L_BABY2", rs));
                svfVrsOutTooth("UP_L_BABY1", getString("UP_L_BABY1", rs));
                svfVrsOutTooth("UP_R_BABY1", getString("UP_R_BABY1", rs));
                svfVrsOutTooth("UP_R_BABY2", getString("UP_R_BABY2", rs));
                svfVrsOutTooth("UP_R_BABY3", getString("UP_R_BABY3", rs));
                svfVrsOutTooth("UP_R_BABY4", getString("UP_R_BABY4", rs));
                svfVrsOutTooth("UP_R_BABY5", getString("UP_R_BABY5", rs));
                svfVrsOutTooth("LW_L_BABY5", getString("LW_L_BABY5", rs));
                svfVrsOutTooth("LW_L_BABY4", getString("LW_L_BABY4", rs));
                svfVrsOutTooth("LW_L_BABY3", getString("LW_L_BABY3", rs));
                svfVrsOutTooth("LW_L_BABY2", getString("LW_L_BABY2", rs));
                svfVrsOutTooth("LW_L_BABY1", getString("LW_L_BABY1", rs));
                svfVrsOutTooth("LW_R_BABY1", getString("LW_R_BABY1", rs));
                svfVrsOutTooth("LW_R_BABY2", getString("LW_R_BABY2", rs));
                svfVrsOutTooth("LW_R_BABY3", getString("LW_R_BABY3", rs));
                svfVrsOutTooth("LW_R_BABY4", getString("LW_R_BABY4", rs));
                svfVrsOutTooth("LW_R_BABY5", getString("LW_R_BABY5", rs));
                svfVrsOutTooth("LW_L_ADULT8", getString("LW_L_ADULT8", rs));
                svfVrsOutTooth("LW_L_ADULT7", getString("LW_L_ADULT7", rs));
                svfVrsOutTooth("LW_L_ADULT6", getString("LW_L_ADULT6", rs));
                svfVrsOutTooth("LW_L_ADULT5", getString("LW_L_ADULT5", rs));
                svfVrsOutTooth("LW_L_ADULT4", getString("LW_L_ADULT4", rs));
                svfVrsOutTooth("LW_L_ADULT3", getString("LW_L_ADULT3", rs));
                svfVrsOutTooth("LW_L_ADULT2", getString("LW_L_ADULT2", rs));
                svfVrsOutTooth("LW_L_ADULT1", getString("LW_L_ADULT1", rs));
                svfVrsOutTooth("LW_R_ADULT1", getString("LW_R_ADULT1", rs));
                svfVrsOutTooth("LW_R_ADULT2", getString("LW_R_ADULT2", rs));
                svfVrsOutTooth("LW_R_ADULT3", getString("LW_R_ADULT3", rs));
                svfVrsOutTooth("LW_R_ADULT4", getString("LW_R_ADULT4", rs));
                svfVrsOutTooth("LW_R_ADULT5", getString("LW_R_ADULT5", rs));
                svfVrsOutTooth("LW_R_ADULT6", getString("LW_R_ADULT6", rs));
                svfVrsOutTooth("LW_R_ADULT7", getString("LW_R_ADULT7", rs));
                svfVrsOutTooth("LW_R_ADULT8", getString("LW_R_ADULT8", rs));

                //歯の状態
                _form.VrsOut("BABYTOOTH"        , getString("BABYTOOTH",         rs));//乳歯＞現在歯数
                _form.VrsOut("REMAINBABYTOOTH"  , getString("REMAINBABYTOOTH",   rs));//乳歯＞未処置数
                _form.VrsOut("TREATEDBABYTOOTH" , getString("TREATEDBABYTOOTH",  rs));//乳歯＞処置数
                _form.VrsOut("BRACKBABYTOOTH"   , getString("BRACK_BABYTOOTH",   rs));//乳歯＞要注意乳歯数
                _form.VrsOut("ADULTTOOTH"       , getString("ADULTTOOTH",        rs));//永久歯＞現在歯数
                _form.VrsOut("REMAINADULTTOOTH" , getString("REMAINADULTTOOTH",  rs));//永久歯＞未処置数
                _form.VrsOut("TREATEDADULTTOOTH", getString("TREATEDADULTTOOTH", rs));//永久歯＞処置数
                _form.VrsOut("LOSTADULTTOOTH"   , getString("LOSTADULTTOOTH",    rs));//永久歯＞喪失歯数

                //その他の疾病及び異常
                {
                    final int blackBabyTooth = toInt(getString("BRACK_BABYTOOTH", rs), 0);
                    final String otherDiseaseCd = getString("OTHERDISEASE_CD", rs);
                    final String oralDiseaseCd  = getString("ORALDISEASE_CD", rs);
                    if (blackBabyTooth == 0 &&
                        "01".equals(otherDiseaseCd) &&
                        "01".equals(oralDiseaseCd)) {//要注意乳歯数無し＆その他の疾病及び異常無し＆口腔の疾病及び異常無し
                        printSlash("SLASH_TOOTHOTHERDISEASE");
                    } else {
                        final String otherDisease = getString("OTHERDISEASE", rs);
                        final String otherDiseaseRemark = SafeSubstring(getString("OTHERDISEASE_REMARK", rs), 20);
                        final String oralDisease = getString("ORALDISEASE", rs);
                        final String oralDiseaseRemark = SafeSubstring(getString("ORALDISEASE_REMARK", rs), 20);


                        //印字する文字列を作成
                        String toothOtherDiseaseText = "";
                        final List tempList = new ArrayList();
                        if (blackBabyTooth >= 1) {
                            tempList.add("要注意乳歯あり");
                        }
                        if (null != otherDiseaseCd && !otherDiseaseCd.equals("")) {
                            if ("00".equals(otherDiseaseCd)) {//未受験
                                //名称マスタの値を表示
                                tempList.add(otherDisease);
                            } else if ("01".equals(otherDiseaseCd)) {//異常なし
                                //異常なしという文字列は表示しない
                            } else if ("02".equals(otherDiseaseCd)) {//異常あり
                                //異常ありという文字列は表示しない

                                //所見があれば表示
                                if (!otherDiseaseRemark.equals("")) {
                                    tempList.add(otherDiseaseRemark);
                                }
                            }
                        }
                        if (null != oralDiseaseCd && !oralDiseaseCd.equals("")) {
                            if ("00".equals(oralDiseaseCd)) {//未受験
                                //名称マスタの値を表示
                                tempList.add(oralDisease);
                            } else if ("01".equals(oralDiseaseCd)) {//異常なし
                                //異常なしという文字列は表示しない
                            } else  {
                                //名称マスタの値を追加
                                String tempOralDisase = oralDisease;

                                //所見があれば追加
                                if (!oralDiseaseRemark.equals("")) {
                                    tempOralDisase += "（" + oralDiseaseRemark + "）";
                                }

                                tempList.add(tempOralDisase);
                            }
                        }
                        toothOtherDiseaseText = StringUtils.join(tempList, delim);

                        final int toothOtherDiseaseTextLen = KNJ_EditEdit.getMS932ByteLength(toothOtherDiseaseText);
                        final String toothOtherDiseaseTextField = (toothOtherDiseaseTextLen <= 66)? "1": "2";
                        final int toothOtherDiseaseTextLineSize = (toothOtherDiseaseTextLen <= 66)? 11: 18;
                        final int toothOtherDiseaseMaxLine = 3;
                        final String[] splitToothOtherDiseaseText = splitByLength(toothOtherDiseaseText, toothOtherDiseaseTextLineSize);
                        for (int sc = 0; sc < splitToothOtherDiseaseText.length; sc++) {
                            _form.VrsOut("TOOTHOTHERDISEASE" + toothOtherDiseaseTextField + "_" + (sc + 1), splitToothOtherDiseaseText[sc]);
                            if (toothOtherDiseaseMaxLine <= sc + 1) {
                                break;
                            }
                        }
                    }
                }

                //学校歯科医＞所見
                {
                    final String calculusCd = getString("CALCULUS_CD", rs);
                    final String dentistRemarkCd1 = getString("DENTISTREMARK_CD1", rs);
                    final String dentistRemarkCd2 = getString("DENTISTREMARK_CD2", rs);
                    final String dentistRemarkCd3 = getString("DENTISTREMARK_CD3", rs);
                    final String dentistRemarkChoice1 = getString("DENTISTREMARK_CHOICE1", rs);
                    final String dentistRemarkChoice2 = getString("DENTISTREMARK_CHOICE2", rs);
                    final String dentistRemarkChoice3 = getString("DENTISTREMARK_CHOICE3", rs);
                    final String dentistRemark1 = SafeSubstring(getString("DENTISTREMARK1", rs), 20);
                    final String dentistRemark2 = SafeSubstring(getString("DENTISTREMARK2", rs), 20);
                    final String dentistRemark3 = SafeSubstring(getString("DENTISTREMARK3", rs), 20);

                    //印字する文字列を作成
                    final List tempList = new ArrayList();
                    String dentistRemarkText = "";
                    if ("02".equals(calculusCd)) {
                        tempList.add("歯石沈着");
                    }
                    if (null != dentistRemarkCd1 && !dentistRemarkCd1.equals("")) {//所見1
                        if ("99".equals(dentistRemarkCd1)) {//その他
                            //「その他」を表示せず、所見のみ表示
                            tempList.add(dentistRemark1);
                        } else {
                            tempList.add(dentistRemarkChoice1);
                        }
                    }
                    if (null != dentistRemarkCd2 && !dentistRemarkCd2.equals("")) {//所見2
                        if ("99".equals(dentistRemarkCd2)) {//その他
                            //「その他」を表示せず、所見のみ表示
                            tempList.add(dentistRemark2);
                        } else {
                            tempList.add(dentistRemarkChoice2);
                        }
                    }
                    if (null != dentistRemarkCd3 && !dentistRemarkCd3.equals("")) {//所見3
                        if ("99".equals(dentistRemarkCd3)) {//その他
                            //「その他」を表示せず、所見のみ表示
                            tempList.add(dentistRemark3);
                        } else {
                            tempList.add(dentistRemarkChoice3);
                        }
                    }
                    dentistRemarkText = StringUtils.join(tempList, delim);

                    final int dentistRemarkTextLen = KNJ_EditEdit.getMS932ByteLength(dentistRemarkText);
                    final String dentistRemarkTextField = (dentistRemarkTextLen <= 66)? "1": (dentistRemarkTextLen <= 108)? "2": "3";
                    final int dentistRemarkLineSize     = (dentistRemarkTextLen <= 66)? 11: (dentistRemarkTextLen <= 108)? 18: 30;
                    final int dentistRemarkMaxLine      = (dentistRemarkTextLen <= 108)? 3 : (_param._useForm_Ha == KensinFormKind.Form4)? 4 : 5;

                    final String[] splitDentistRemark = splitByLength(dentistRemarkText, dentistRemarkLineSize);
                    for (int sc = 0; sc < splitDentistRemark.length; sc++) {
                        _form.VrsOut("DENTISTREMARK" + dentistRemarkTextField + "_" + (sc + 1),  splitDentistRemark[sc]);
                        if (dentistRemarkMaxLine <= sc + 1) {
                            break;
                        }
                    }
                }

                //学校歯科医＞月日
                _form.VrsOut("month"  ,  getString("DENTISTREMARKMONTH", rs));
                _form.VrsOut("day"    ,  getString("DENTISTREMARKDAY", rs));

                //事後処置
                {
                    final String dentistTreatCd1 = getString("DENTISTTREAT_CD1", rs);
                    final String dentistTreatCd2 = getString("DENTISTTREAT_CD2", rs);
                    if ("01".equals(dentistTreatCd1)) {//事後措置1無し
                        printSlash("SLASH_DENTISTTREAT");
                    } else {
                        final String dentistTreat1 = getString("DENTISTTREAT1", rs);
                        final String dentistTreat2 = getString("DENTISTTREAT2", rs);
                        final String dentistTreatRemark1 = SafeSubstring(getString("DENTISTTREAT_REMARK1", rs), 20);
                        final String dentistTreatRemark2 = SafeSubstring(getString("DENTISTTREAT_REMARK2", rs), 20);

                        //印字する文字列を作成
                        final List tempList = new ArrayList();
                        String dentistTreatText = "";
                        if (null != dentistTreatCd1 && !dentistTreatCd1.equals("")) {
                            if (!dentistTreatRemark1.equals("")) {
                                tempList.add(dentistTreat1 + "（" + dentistTreatRemark1 + "）");
                            } else {
                                tempList.add(dentistTreat1);
                            }
                        }
                        if (null != dentistTreatCd2 && !dentistTreatCd2.equals("")) {
                            if (!dentistTreatRemark2.equals("")) {
                                tempList.add(dentistTreat2 + "（" + dentistTreatRemark2 + "）");
                            } else {
                                tempList.add(dentistTreat2);
                            }
                        }
                        dentistTreatText = StringUtils.join(tempList, delim);

                        final int dentistTreatTextLen = KNJ_EditEdit.getMS932ByteLength(dentistTreatText);
                        final String dentistTreatTextField = (dentistTreatTextLen <= 66)? "1": "2";
                        final int dentistTreatLineSize  = (dentistTreatTextLen <= 66)? 11:18;
                        final int dentistTreatMaxLine = 3;
                        final String[] splitDentistTreat = KNJ_EditEdit.splitByLength(dentistTreatText, dentistTreatLineSize);
                        for (int sc = 0; sc < splitDentistTreat.length; sc++) {
                            _form.VrsOut("DENTISTTREAT" + dentistTreatTextField + "_" + (sc + 1),  splitDentistTreat[sc]);
                            if (dentistTreatMaxLine <= sc + 1) {
                                break;
                            }
                        }
                    }
                }

                _form.VrEndRecord();
                printOk = true;
            } catch (Exception ex) {
                log.warn("printMainSvf read error!", ex);
            }
            return printOk;
        }

        private void svfVrsOutTooth(final String field, final String code) {
            if (code == null) {
                return;
            }

            String mark = null;
            if ("01".equals(code)) {       mark = "-"; }  // 現在歯
            else if ("02".equals(code)) { mark = "Ｃ"; } // 未処置歯
            else if ("03".equals(code)) { mark = "○"; } // 処置歯
            else if ("04".equals(code)) { mark = "△"; } // 喪失歯（永久歯）
            else if ("05".equals(code)) { mark = "×"; } // 要注意歯
            else if ("06".equals(code)) { mark = "C0"; } // 要観察歯
            else if ("07".equals(code)) { mark = "CS"; } // 要精検歯

            _form.VrsOut(field, mark);
            if ("04".equals(code)) {
                return;
            }
            _form.VrsOut("NOW_"+field, "／");
        }

        /**
         * dataをcount文字数で分割し配列を返す
         * 主に縦書き用
         */
        public String[] splitByLength(final String data, int count) {
            if (null == data || data.length() == 0) {
                return new String[] {};
            }
            final int cnt = data.length() / count;
            final int cntAmari = data.length() % count;
            final int forCnt = cntAmari > 0 ? cnt + 1 : cnt;
            final String[] retStr = new String[forCnt];
            String dataHoge = data;
            for (int i = 0; i < forCnt; i++) {
                if (dataHoge.length() < count) {
                    retStr[i] = dataHoge.substring(0, dataHoge.length());
                    dataHoge = dataHoge.substring(dataHoge.length());
                } else {
                    retStr[i] = dataHoge.substring(0, count);
                    dataHoge = dataHoge.substring(count);
                }
            }
            return retStr;
        }
    }

    //========================================================================
    /** 健康診断結果通知書の共通処理 */
    private abstract class TutiAbstract {
        protected final Param    _param;
        protected final DB2UDB   _db2;
        protected final Vrw32alp _svf;
        protected final String   _documentCd;

        //通知書のお知らせ件名、文面のコード
        protected final static String TITLE_CD_TUTI_ITIRAN   = "01";
        protected final static String TITLE_CD_TUTI_UNDOUKI  = "02";
        protected final static String TITLE_CD_TUTI_GANKA    = "03";
        protected final static String TITLE_CD_TUTI_KEKKAKU  = "04";
        protected final static String TITLE_CD_TUTI_SIRYOKU  = "05";
        protected final static String TITLE_CD_TUTI_SIKA     = "06";
        protected final static String TITLE_CD_TUTI_JIBIKA   = "07";
        protected final static String TITLE_CD_TUTI_SIKIKAKU = "08";
        protected final static String TITLE_CD_TUTI_SINDENZU = "09";
        protected final static String TITLE_CD_TUTI_TYORYOKU = "10";
        protected final static String TITLE_CD_TUTI_NAIKA    = "11";
        protected final static String TITLE_CD_TUTI_NYOU     = "12";

        /** コンストラクタ */
        public TutiAbstract(final Param param, final DB2UDB db2, final Vrw32alp svf, final String documentCd) {
            _param      = param;
            _db2        = db2;
            _svf        = svf;
            _documentCd = documentCd;
        }

        /** 右上の日付をセット */
        protected void printHeadDate(final String dateString) {
            printDate(dateString, "DATE");
        }

        /** 指定したフィールドに日付をセット */
        protected void printDate(final String dateString, final String fieldName) {
            _svf.VrsOut(fieldName,  formatDate(_db2, _param, dateString));
        }

        /** 校長名を取得 */
        protected String getPrincipalName(final String year) {
            String principalName = "";

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final StringBuffer stb = new StringBuffer();

                stb.append("    SELECT PRINCIPAL_NAME ");
                stb.append("      FROM CERTIF_SCHOOL_DAT CSD" );
                stb.append("     WHERE CSD.YEAR = '" + year + "'" );
                stb.append("       AND CSD.CERTIF_KINDCD = '125'" );

                log.info(stb.toString());

                ps = _db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    principalName    = rs.getString("PRINCIPAL_NAME");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }

            return principalName;
        }

        /** 学校名を取得 */
        protected String getSchoolName(final String year) {
            String schoolName = "";

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final StringBuffer stb = new StringBuffer();

                stb.append("    SELECT SCHOOL_NAME ");
                stb.append("      FROM CERTIF_SCHOOL_DAT CSD" );
                stb.append("     WHERE CSD.YEAR = '" + year + "'" );
                stb.append("       AND CSD.CERTIF_KINDCD = '125'" );

                log.info(stb.toString());

                ps = _db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    schoolName    = rs.getString("SCHOOL_NAME");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
            return schoolName;
        }

        /** 年度から元号を返す */
        protected String getGengo(final String year) {
            return KNJ_EditDate.gengou(_db2, toInt(year, 0), 4, 1);
        }

        /** 生徒名を取得 */
        protected String getStudentName(final String year, final String semester, final String schRegNo) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String studentName = "";

            try {
                final StringBuffer stb = new StringBuffer();

                stb.append("    SELECT GDAT.GRADE_NAME1||'　'||HDAT.HR_CLASS_NAME1||'組　'||INTEGER(REGD.ATTENDNO)||'番　'||B.NAME||'　さん' AS STUDENT_NAME");
                stb.append("      FROM SCHREG_BASE_MST B");
                stb.append(" LEFT JOIN SCHREG_REGD_DAT REGD");
                stb.append("        ON (REGD.SCHREGNO = B.SCHREGNO");
                stb.append("       AND  REGD.YEAR     = '" + year + "'");
                stb.append("       AND  REGD.SEMESTER = '" + semester + "')");
                stb.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT");
                stb.append("        ON (GDAT.YEAR  = REGD.YEAR");
                stb.append("       AND  GDAT.GRADE = REGD.GRADE)");
                stb.append(" LEFT JOIN SCHREG_REGD_HDAT HDAT");
                stb.append("        ON (HDAT.YEAR  = GDAT.YEAR");
                stb.append("       AND  HDAT.GRADE = GDAT.GRADE");
                stb.append("       AND  HDAT.SEMESTER = REGD.SEMESTER");
                stb.append("       AND  HDAT.HR_CLASS = REGD.HR_CLASS)");
                stb.append("     WHERE B.SCHREGNO = '" + schRegNo + "'");

                log.info(stb.toString());

                ps = _db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    studentName    = rs.getString("STUDENT_NAME");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }

            return studentName;
        }

        /** 文面マスタからタイトルと本文をセット 共通 */
        protected void printTitle() {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(statementTitle());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getString("TEXT") != null) {
                        StringTokenizer st = new StringTokenizer(rs.getString("TEXT"), "\n");
                        int j = 1;
                        while (st.hasMoreTokens()) {
                            _svf.VrsOut("TEXT" + j, st.nextToken());// 本文
                            j++;
                        }
                    }
                    _svf.VrsOut("TITLE", rs.getString("TITLE")); // タイトル
                }
                rs.close();
                ps.close();
                _db2.commit();
            } catch (Exception ex) {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
        }

        /** 文面マスタ情報 文面マスタからタイトルと本文を取得 */
        private String statementTitle() {
            StringBuffer stb = new StringBuffer();
            try {
                stb.append("SELECT TITLE, TEXT FROM DOCUMENT_MST WHERE DOCUMENTCD = '" + _documentCd + "'");
            } catch (Exception e) {
                log.warn("statementTitle error!", e);
            }
            return stb.toString();
        }

        //----ここから肥満度計算start

        protected Map getHimando(final String schRegNo) {
            final Map physAvgMap = HexamPhysicalAvgDat.getHexamPhysicalAvgMap(_db2, _param);

            String height  = "";
            String weight  = "";
            String himando = "";
            int    age     = 0;
            String bmi     = "";
            String obesity = "";

            try {
                final String psKey = "getHimandoDataSql";
                _param._psMap.remove(psKey);
                final String sql = getHimandoDataSql(schRegNo);
                if (_param._isOutputDebug) log.info(" sql = " + sql);

                try {
                    _param._psMap.put(psKey, _db2.prepareStatement(sql));
                } catch (SQLException e) {
                    log.error("error!", e);
                }

                for (final Map rs : KnjDbUtils.query(_db2, _param._psMap.get(psKey), new Object[] { /*プリペアドステートメントの引数をセットする*/ })) {
                    height  =  getString("HEIGHT", rs);
                    weight  =  getString("WEIGHT", rs);
                    age     = (int)getNenrei(rs, _param._year + "-04-01", _param._year, _param._year);
                    himando = calcHimando(rs, physAvgMap, _param);
                    bmi     =  getString("BMI", rs);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
            }

            if (age <= 17) {
                //年齢が17以下
                bmi     = "";//BMIは使用しない
                obesity = himando + "％";//帳票には肥満度を表示する
                _svf.VrsOut("OBESITY_NAME", "肥満度");
            } else {
                //年齢が17より大きい
                himando = "";//肥満度は使用しない
                obesity = bmi;//帳票にはBMIを表示する
                _svf.VrsOut("OBESITY_NAME", "BMI");
            }

            final Map m = new TreeMap();
            m.put("AGE", age);
            m.put("HEIGHT", height);
            m.put("WEIGHT", weight);
            m.put("OBESITY", obesity);
            return m;
        }

        //出力データ取得
        private String getHimandoDataSql(final String schRegNo) {
             final StringBuffer stb = new StringBuffer();

             stb.append("    SELECT T1.SCHREGNO AS SCHREGNO");
             stb.append("         , T3.WEIGHT");
             stb.append("         , T3.HEIGHT");
             stb.append("         , T2.SEX");
             stb.append("         , T2.BIRTHDAY");
             stb.append("         , T1.GRADE");
             stb.append("         , CASE WHEN VALUE(T3.HEIGHT,0) > 0 THEN DECIMAL(ROUND(T3.WEIGHT/T3.HEIGHT/T3.HEIGHT*10000,1),4,1) END AS BMI");
             stb.append("      FROM SCHREG_REGD_DAT T1");
             stb.append(" LEFT JOIN SCHREG_BASE_MST T2");
             stb.append("        ON (T2.SCHREGNO = T1.SCHREGNO)");
             stb.append(" LEFT JOIN MEDEXAM_DET_DAT T3");
             stb.append("        ON (T3.SCHREGNO = T1.SCHREGNO");
             stb.append("       AND  T3.YEAR     = '" + _param._year + "')");
             stb.append("     WHERE T1.YEAR     = '" + _param._year + "'");
             stb.append("       AND T1.SEMESTER = '" + _param._gakki + "'");
             stb.append("       AND T1.SCHREGNO = '" + schRegNo + "'");

             return stb.toString();
        }

        // 肥満度計算
        //  肥満度（過体重度）= 100 × (測定された体重 - 標準体重) / 標準体重
        private String calcHimando(Map rs, final Map physAvgMap, final Param param) throws SQLException {
            if (null == getString("WEIGHT", rs)) {
                log.debug(" " + getString("SCHREGNO", rs) + ", " + param._year + " 体重がnull");
                return null;
            }
            BigDecimal weightAvg = null;
            final boolean isUseMethod2 = true;
            if (isUseMethod2) {
                final BigDecimal weightAvg2 = getWeightAvgMethod2(rs, physAvgMap, param);
                log.fatal(" (schregno, attendno, weight2) = (" + getString("SCHREGNO", rs) + ", " + getString("ATTENDNO", rs) + ", " + weightAvg2 + ")");
                weightAvg = weightAvg2;
            }
            if (null == weightAvg) {
                return null;
            }
            final BigDecimal himando = new BigDecimal(100).multiply(new BigDecimal(Double.parseDouble(getString("WEIGHT", rs))).subtract(weightAvg)).divide(weightAvg, 1, BigDecimal.ROUND_HALF_UP);
            log.fatal(" himando = 100 * (" + getString("WEIGHT", rs) + " - " + weightAvg + ") / " + weightAvg + " = " + himando);
            return himando.toString();
        }

        private BigDecimal getWeightAvgMethod2(final Map rs, final Map physAvgMap, final Param param) throws SQLException {
            if (null == getString("HEIGHT", rs)) {
                log.debug(" " + getString("SCHREGNO", rs) + ", " + param._year + " 身長がnull");
                return null;
            }
            if (null == getString("BIRTHDAY", rs)) {
                log.debug(" " + getString("SCHREGNO", rs) + ", " + param._year + " 生年月日がnull");
                return null;
            }
            // 日本小児内分泌学会 (http://jspe.umin.jp/)
            // http://jspe.umin.jp/ipp_taikaku.htm ２．肥満度 ２）性別・年齢別・身長別標準体重（５歳以降）のデータによる
            // ａ＝ HEXAM_PHYSICAL_AVG_DAT.STD_WEIGHT_KEISU_A
            // ｂ＝ HEXAM_PHYSICAL_AVG_DAT.STD_WEIGHT_KEISU_B　
            // 標準体重＝ａ×身長（cm）- ｂ 　 　
            final BigDecimal height = new BigDecimal(getString("HEIGHT", rs));
            final String kihonDate = param._year + "-04-01";
            final int iNenrei = (int) getNenrei(rs, kihonDate, param._year, param._year);
            final HexamPhysicalAvgDat hpad = getPhysicalAvgDatNenrei(iNenrei, (List) physAvgMap.get(getString("SEX", rs)));
            if (null == hpad || null == hpad._stdWeightKeisuA || null == hpad._stdWeightKeisuB) {
                return null;
            }
            final BigDecimal a = hpad._stdWeightKeisuA;
            final BigDecimal b = hpad._stdWeightKeisuB;
            final BigDecimal avgWeight = a.multiply(height).subtract(b);
            log.fatal(" method2 avgWeight = " + a + " * " + height + " - " + b + " = " + avgWeight);
            return avgWeight;
        }

        // 学年から年齢を計算する
        private double getNenrei2(Map rs, final String year1, final String year2) throws NumberFormatException, SQLException {
            return 5.0 + Integer.parseInt(getString("GRADE", rs)) - (StringUtils.isNumeric(year1) && StringUtils.isNumeric(year2) ? Integer.parseInt(year1) - Integer.parseInt(year2) : 0); // 1年生:6才、2年生:7才、...6年生:11才
        }

        // 生年月日と対象日付から年齢を計算する
        private double getNenrei(Map rs, final String date, final String year1, final String year2) throws NumberFormatException, SQLException {
            if (null == getString("BIRTHDAY", rs)) {
                return getNenrei2(rs, year1, year2);
            }
            final Calendar calBirthDate = Calendar.getInstance();
            calBirthDate.setTime(Date.valueOf(getString("BIRTHDAY", rs)));
            final int birthYear = calBirthDate.get(Calendar.YEAR);
            final int birthDayOfYear = calBirthDate.get(Calendar.DAY_OF_YEAR);

            final Calendar calTestDate = Calendar.getInstance();
            calTestDate.setTime(Date.valueOf(date));
            final int testYear = calTestDate.get(Calendar.YEAR);
            final int testDayOfYear = calTestDate.get(Calendar.DAY_OF_YEAR);

            int nenreiYear = testYear - birthYear + (testDayOfYear - birthDayOfYear < 0 ? -1 : 0);
            final int nenreiDateOfYear = testDayOfYear - birthDayOfYear + (testDayOfYear - birthDayOfYear < 0 ? 365 : 0);
            final double nenrei = nenreiYear + nenreiDateOfYear / 365.0;
            return nenrei;
        }

        // 年齢の平均データを得る
        private HexamPhysicalAvgDat getPhysicalAvgDatNenrei(final int nenrei, final List physAvgList) {
            HexamPhysicalAvgDat tgt = null;
            if (null != physAvgList) {
                for (final Iterator it = physAvgList.iterator(); it.hasNext();) {
                    final HexamPhysicalAvgDat hpad = (HexamPhysicalAvgDat) it.next();
                    if (hpad._nenrei <= nenrei) {
                        tgt = hpad;
                        if (hpad._nenreiYear == nenrei) {
                            break;
                        }
                    }
                }
            }
            return tgt;
        }

        //----ここまで肥満度計算end

        /** 「医療機関向け」か「保護者向け」かを返す */
        protected PageKind getPageKind() {
            if (this instanceof TutiUndouki ) { return _param._pageKindUndouki ; }
            if (this instanceof TutiGanka   ) { return _param._pageKindGanka   ; }
            if (this instanceof TutiKekkaku ) { return _param._pageKindKekkaku ; }
            if (this instanceof TutiSiryoku ) { return _param._pageKindSiryoku ; }
            if (this instanceof TutiSika    ) { return _param._pageKindSika    ; }
            if (this instanceof TutiJibika  ) { return _param._pageKindJibika  ; }
            if (this instanceof TutiSikikaku) { return _param._pageKindSikikaku; }
            if (this instanceof TutiSindenzu) { return _param._pageKindSindenzu; }
            if (this instanceof TutiTyoryoku) { return _param._pageKindTyoryoku; }
            if (this instanceof TutiNaika   ) { return _param._pageKindNaika   ; }
            if (this instanceof TutiNyou    ) { return _param._pageKindNyou    ; }

            //TutiAbstractやTutiItiranには存在しない項目
            //→取り急ぎ「医療機関向け」を返す
            return PageKind.Medical;
        }

    }

    //========================================================================
    //「医療機関向け」「保護者向け」
    public enum PageKind {
        /** 医療機関向け */
        Medical,

        /** 保護者向け */
        Parent,
    }

    //========================================================================
    //標準体重取得
    static class HexamPhysicalAvgDat {
        final String _sex;
        final int _nenreiYear;
        final int _nenreiMonth;
        final double _nenrei;
        final BigDecimal _heightAvg;
        final BigDecimal _heightSd;
        final BigDecimal _weightAvg;
        final BigDecimal _weightSd;
        final BigDecimal _stdWeightKeisuA;
        final BigDecimal _stdWeightKeisuB;

        HexamPhysicalAvgDat(
            final String sex,
            final int nenreiYear,
            final int nenreiMonth,
            final BigDecimal heightAvg,
            final BigDecimal heightSd,
            final BigDecimal weightAvg,
            final BigDecimal weightSd,
            final BigDecimal stdWeightKeisuA,
            final BigDecimal stdWeightKeisuB
        ) {
            _sex = sex;
            _nenreiYear = nenreiYear;
            _nenreiMonth = nenreiMonth;
            _nenrei = _nenreiYear + (_nenreiMonth / 12.0);
            _heightAvg = heightAvg;
            _heightSd = heightSd;
            _weightAvg = weightAvg;
            _weightSd = weightSd;
            _stdWeightKeisuA = stdWeightKeisuA;
            _stdWeightKeisuB = stdWeightKeisuB;
        }

        public static Map getHexamPhysicalAvgMap(final DB2UDB db2, final Param param) {
            final Map m = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String sex                 = rs.getString("SEX");
                    final int nenreiYear             = rs.getInt("NENREI_YEAR");
                    final int nenreiMonth            = rs.getInt("NENREI_MONTH");
                    final BigDecimal heightAvg       = rs.getBigDecimal("HEIGHT_AVG");
                    final BigDecimal heightSd        = rs.getBigDecimal("HEIGHT_SD");
                    final BigDecimal weightAvg       = rs.getBigDecimal("WEIGHT_AVG");
                    final BigDecimal weightSd        = rs.getBigDecimal("WEIGHT_SD");
                    final BigDecimal stdWeightKeisuA = rs.getBigDecimal("STD_WEIGHT_KEISU_A");
                    final BigDecimal stdWeightKeisuB = rs.getBigDecimal("STD_WEIGHT_KEISU_B");
                    final HexamPhysicalAvgDat testheightweight =
                        new HexamPhysicalAvgDat(sex, nenreiYear, nenreiMonth, heightAvg, heightSd, weightAvg, weightSd, stdWeightKeisuA, stdWeightKeisuB);
                    if (null == m.get(rs.getString("SEX"))) {
                        m.put(rs.getString("SEX"), new ArrayList());
                    }
                    final List list = (List)m.get(rs.getString("SEX"));
                    list.add(testheightweight);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return m;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH MAX_YEAR AS ( ");
            stb.append("     SELECT MAX(YEAR) AS YEAR ");
            stb.append("       FROM HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("      WHERE T1.YEAR <= '" + param._year + "' ");
            stb.append(" ), MIN_YEAR AS ( ");
            stb.append("     SELECT MIN(YEAR) AS YEAR ");
            stb.append("       FROM HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("      WHERE  T1.YEAR >= '" + param._year + "' ");
            stb.append(" ), MAX_MIN_YEAR AS ( ");
            stb.append("     SELECT MIN(T1.YEAR) AS YEAR ");
            stb.append("       FROM ( ");
            stb.append("           SELECT YEAR FROM MAX_YEAR T1 ");
            stb.append("           UNION ");
            stb.append("           SELECT YEAR FROM MIN_YEAR T1 ");
            stb.append("       ) T1 ");
            stb.append(" ) ");
            stb.append("     SELECT T1.SEX, ");
            stb.append("            T1.NENREI_YEAR, ");
            stb.append("            T1.NENREI_MONTH, ");
            stb.append("            T1.HEIGHT_AVG, ");
            stb.append("            T1.HEIGHT_SD, ");
            stb.append("            T1.WEIGHT_AVG, ");
            stb.append("            T1.WEIGHT_SD, ");
            stb.append("            T1.STD_WEIGHT_KEISU_A, ");
            stb.append("            T1.STD_WEIGHT_KEISU_B ");
            stb.append("       FROM HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append(" INNER JOIN MAX_MIN_YEAR T2 ");
            stb.append("         ON T2.YEAR = T1.YEAR ");
            stb.append("   ORDER BY T1.SEX");
            stb.append("          , T1.NENREI_YEAR");
            stb.append("          , T1.NENREI_MONTH ");
            return stb.toString();
        }
    }

    //========================================================================
    /** 健康診断結果通知書（一覧） */
    private class TutiItiran extends TutiAbstract {
        //コンストラクタ
        public TutiItiran(final Param param, final DB2UDB db2, final Vrw32alp svf) {
            super(param, db2, svf, TITLE_CD_TUTI_ITIRAN);
        }

        //帳票出力
        public boolean printMain() {
            boolean printOk = false;

            setForm(_param, _svf, "KNJF030J_3.xml", 4);

            final String psKey = "getDataSql43_2";
            if (null == _param._psMap.get(psKey)) {
                final String sql = getDataSql();
                if (_param._isOutputDebug) log.info(" sql = " + sql);
                log.info(" sql = " + sql);

                try {
                    _param._psMap.put(psKey, _db2.prepareStatement(sql));
                } catch (SQLException e) {
                    log.error("error!", e);
                }
            }

            //見つかった生徒の数だけループ
            for (final Map rs : KnjDbUtils.query(_db2, _param._psMap.get(psKey), new Object[] { /*プリペアドステートメントの引数をセットする*/ })) {
                {
                    String studentName = getStudentName(_param._year, _param._gakki, getString("SCHREGNO", rs));
                    final int studentNameLen = KNJ_EditEdit.getMS932ByteLength(studentName);
                    final String studentNameField = (studentNameLen <= 40)? "1":
                                                           (studentNameLen <= 50)? "2":
                                                               (studentNameLen <= 60)? "3": "4";
                    _svf.VrsOut("NAME1_" + studentNameField, studentName);
                }
                printHeadDate(_param._sendDate);
                _svf.VrsOut("SCHOOL_NAME"   ,  getSchoolName(_param._year));
                _svf.VrsOut("PRINCIPAL_NAME",  getSchoolName(_param._year) + "長");
                printTitle();

                //身長、体重、肥満度(※18歳以上はBMI)
                final Map mapHimando = getHimando(getString("SCHREGNO", rs));
                if ((String)mapHimando.get("HEIGHT") != null) {
                    _svf.VrsOut("HEIGHT",  (String)mapHimando.get("HEIGHT") + "cm");
                }
                if ((String)mapHimando.get("WEIGHT") != null) {
                    _svf.VrsOut("WEIGHT",  (String)mapHimando.get("WEIGHT") + "kg");
                }
                if ((String)mapHimando.get("OBESITY") != null &&
                     (String)mapHimando.get("HEIGHT") != null &&
                     (String)mapHimando.get("WEIGHT") != null) {
                    _svf.VrsOut("OBESITY", (String)mapHimando.get("OBESITY"));
                }

                //脊柱胸郭
                String SPINERIB = "";
                //脊柱胸郭四肢の全般が未受験または異常なしのとき
                if ("00".equals(getString("SPINERIBCD",rs )) == true || "01".equals(getString("SPINERIBCD",rs )) == true) {
                    _svf.VrsOut("SPINERIB1_1"         , getString("SPINERIBCD_NAME",rs ));
                }
                //脊柱胸郭四肢の全般が空白ではないとき
                else if ( getString("SPINERIBCD",rs ) != null){
                    //脊柱胸郭四肢疾患1のコンボが13,22,33の時
                    if ("13".equals(getString("SPINERIBCD1",rs )) == true || "22".equals(getString("SPINERIBCD1",rs )) == true || "33".equals(getString("SPINERIBCD1",rs )) == true){
                        if ( getString("SPINERIBCD_REMARK1",rs ) != null){
                            if ("".equals(SPINERIB) == true) {
                                SPINERIB += getString("SPINERIBCD_REMARK1",rs );
                            } else {
                                SPINERIB += "、" + getString("SPINERIBCD_REMARK1",rs );
                            }
                        }
                    }
                    else if ( getString("SPINERIBCD1",rs ) != null){
                        if ("".equals(SPINERIB) == true) {
                            SPINERIB += getString("SPINERIBCD_NAME1",rs );
                        } else {
                            SPINERIB += "、" + getString("SPINERIBCD_NAME1",rs );
                        }
                    }
                    //脊柱胸郭四肢疾患2のコンボが13,22,33の時
                    if ("13".equals(getString("SPINERIBCD2",rs )) == true|| "22".equals(getString("SPINERIBCD2",rs )) == true || "33".equals(getString("SPINERIBCD2",rs )) == true){
                        if ( getString("SPINERIBCD_REMARK2",rs ) != null){
                            if ("".equals(SPINERIB) == true) {
                                SPINERIB += getString("SPINERIBCD_REMARK2",rs );
                            } else {
                                SPINERIB += "、" + getString("SPINERIBCD_REMARK2",rs );
                            }
                        }
                    }
                    else if ( getString("SPINERIBCD2",rs ) != null){
                        if ("".equals(SPINERIB) == true) {
                            SPINERIB += getString("SPINERIBCD_NAME2",rs );
                        } else {
                            SPINERIB += "、" + getString("SPINERIBCD_NAME2",rs );
                        }
                    }
                    //脊柱胸郭四肢疾患3のコンボが13,22,33の時
                    if ("13".equals(getString("SPINERIBCD3",rs )) == true || "22".equals(getString("SPINERIBCD3",rs )) == true || "33".equals(getString("SPINERIBCD3",rs )) == true){
                        if ( getString("SPINERIBCD_REMARK3",rs ) != null){
                            if ("".equals(SPINERIB) == true) {
                                SPINERIB += getString("SPINERIBCD_REMARK3",rs );
                            } else {
                                SPINERIB += "、" + getString("SPINERIBCD_REMARK3",rs );
                            }
                        }
                    }
                    else if ( getString("SPINERIBCD3",rs ) != null){
                        if ("".equals(SPINERIB) == true) {
                            SPINERIB += getString("SPINERIBCD_NAME3",rs );
                        } else {
                            SPINERIB += "、" + getString("SPINERIBCD_NAME3",rs );
                        }
                    }
                    final int spineribLen = KNJ_EditEdit.getMS932ByteLength(SPINERIB);
                    final String spineribField = (spineribLen <= 48)? "1_1" : "2_1" ;
                    _svf.VrsOut("SPINERIB" + spineribField, SPINERIB);
                } else {
                    _svf.VrsOut("SPINERIB1_1", "");
                }

                //皮膚疾患
                String SKINDISEASE = "";
                SKINDISEASE = chengeDisplay(getString("SKINDISEASECD",rs ),getString("SKINDISEASECD_REMARK",rs ), getString("SKINDISEASECD_NAME",rs ));

                if ( SKINDISEASE != null) {
                    _svf.VrsOut("SKINDISEASE1",SKINDISEASE);
                }

                //心電図
                //高校2,3年生のとき
                if ("02".equals(getString("GRADE",rs )) == true || "03".equals(getString("GRADE",rs )) == true) {
                    _svf.VrsOut("HEART_MEDEXAM","*");
                }
                //高校1年生のとき
                else if ("01".equals(getString("GRADE",rs )) == true){
                    if (getString("HEART_MEDEXAM",rs ) != null) {
                        _svf.VrsOut("HEART_MEDEXAM"     , getString("HEART_MEDEXAM_NAME",rs ));
                    }
                }

                //視力
                _svf.VrsOut("R_BAREVISION_MARK" , getString("R_BAREVISION_MARK",rs ));
                _svf.VrsOut("R_VISION_MARK"     , getString("R_VISION_MARK",rs ));
                _svf.VrsOut("L_BAREVISION_MARK" , getString("L_BAREVISION_MARK",rs ));
                _svf.VrsOut("L_VISION_MARK"     , getString("L_VISION_MARK",rs ));

                //眼科検診
                String EYEDISEASE ="";
                EYEDISEASE = chengeDisplay(getString("EYEDISEASECD",rs ),getString("EYE_TEST_RESULT",rs ), getString("EYEDISEASECD_NAME",rs ));
                    _svf.VrsOut("EYEDISEASE1",EYEDISEASE);

                //聴力
                if ("1".equals(getString("R_EAR_ABBV3",rs )) == true) {
                    _svf.VrsOut("L_EAR1", "");
                } else if (getString("R_EAR_CD",rs ) != null) {
                    //未受験、所見なしのとき
                    if ("00".equals(getString("R_EAR_CD",rs )) == true || "01".equals(getString("R_EAR_CD",rs )) == true) {
                        final int rightEarLen = KNJ_EditEdit.getMS932ByteLength(getString("R_EAR", rs));
                        final String rightEarField = (rightEarLen <= 24)? "1": "2";
                        _svf.VrsOut("R_EAR" + rightEarField, getString("R_EAR", rs));
                    } else if ("02".equals(getString("R_EAR_CD",rs )) == true ||
                                "03".equals(getString("R_EAR_CD",rs )) == true ||
                                "04".equals(getString("R_EAR_CD",rs )) == true) {
                        //所見ありのとき
                        _svf.VrsOut("R_EAR1", "所見あり");
                    }
                }

                if ("1".equals(getString("L_EAR_ABBV3",rs )) == true) {
                    _svf.VrsOut("L_EAR1", "");
                } else if (getString("L_EAR_CD",rs ) != null) {
                    //未受験、所見なしのとき
                    if ("00".equals(getString("L_EAR_CD",rs )) == true || "01".equals(getString("L_EAR_CD",rs )) == true) {
                        final int rightEarLen = KNJ_EditEdit.getMS932ByteLength(getString("L_EAR", rs));
                        final String rightEarField = (rightEarLen <= 24)? "1": "2";
                        _svf.VrsOut("L_EAR" + rightEarField, getString("L_EAR", rs));
                    } else if ("02".equals(getString("L_EAR_CD",rs )) == true ||
                                "03".equals(getString("L_EAR_CD",rs )) == true ||
                                "04".equals(getString("L_EAR_CD",rs )) == true) {
                        //所見ありのとき
                        _svf.VrsOut("L_EAR1", "所見あり");
                    }
                }

                //耳鼻科検診
                String NOSEDISEASE = "";
                //耳鼻咽喉疾患の全般が未受験または異常なしのとき
                if ("00".equals(getString("NOSEDISEASECD",rs )) == true || "01".equals(getString("NOSEDISEASECD",rs )) == true) {
                    _svf.VrsOut("NOSEDISEASE1_1"         , getString("NOSEDISEASECD_NAME",rs ));
                } else if ( getString("NOSEDISEASECD",rs ) != null){
                    //耳鼻咽喉疾患の全般が空白ではないとき
                    //耳鼻咽喉疾患1のコンボが99の時
                    if ("99".equals(getString("NOSEDISEASECD5",rs )) == true ){
                        if ( getString("NOSEDISEASECD_REMARK1",rs ) != null){
                            if ("".equals(NOSEDISEASE) == true) {
                                NOSEDISEASE += getString("NOSEDISEASECD_REMARK1",rs );
                            } else {
                                NOSEDISEASE += "、" + getString("NOSEDISEASECD_REMARK1",rs );
                            }
                        }
                    } else if ( getString("NOSEDISEASECD5",rs ) != null){
                        if ("".equals(NOSEDISEASE) == true) {
                            NOSEDISEASE += getString("NOSEDISEASECD5_NAME",rs );
                        } else {
                            NOSEDISEASE += "、" + getString("NOSEDISEASECD5_NAME",rs );
                        }
                    }
                    //耳鼻咽喉疾患2のコンボが99の時
                    if ("99".equals(getString("NOSEDISEASECD6",rs )) == true ){
                        if ( getString("NOSEDISEASECD_REMARK2",rs ) != null){
                            if ("".equals(NOSEDISEASE) == true) {
                                NOSEDISEASE += getString("NOSEDISEASECD_REMARK2",rs );
                            } else {
                                NOSEDISEASE += "、" + getString("NOSEDISEASECD_REMARK2",rs );
                            }
                        }
                    } else if ( getString("NOSEDISEASECD6",rs ) != null){
                        if ("".equals(NOSEDISEASE) == true) {
                            NOSEDISEASE += getString("NOSEDISEASECD6_NAME",rs );
                        } else {
                            NOSEDISEASE += "、" + getString("NOSEDISEASECD6_NAME",rs );
                        }
                    }
                    //耳鼻咽喉疾患3のコンボが99の時
                    if ("99".equals(getString("NOSEDISEASECD7",rs )) == true ){
                        if ( getString("NOSEDISEASECD_REMARK3",rs ) != null){
                            if ("".equals(NOSEDISEASE) == true) {
                                NOSEDISEASE += getString("NOSEDISEASECD_REMARK3",rs );
                            } else {
                                NOSEDISEASE += "、" + getString("NOSEDISEASECD_REMARK3",rs );
                            }
                        }
                    } else if ( getString("NOSEDISEASECD7",rs ) != null){
                        if ("".equals(NOSEDISEASE) == true) {
                            NOSEDISEASE += getString("NOSEDISEASECD7_NAME",rs );
                        } else {
                            NOSEDISEASE += "、" + getString("NOSEDISEASECD7_NAME",rs );
                        }
                    }
                    //耳鼻咽喉疾患1の文字が半角24文字より大きいとき
                    {
                        final int noseDiseaseLen = KNJ_EditEdit.getMS932ByteLength(NOSEDISEASE);
                        final String noseDiseaseField = (noseDiseaseLen <= 24)? "1": "2";
                        _svf.VrsOut("NOSEDISEASE" + noseDiseaseField + "_1", NOSEDISEASE);
                    }
                } else {
                    _svf.VrsOut("NOSEDISEASE1_1", "");
                }

                //うしの状況
                //乳歯
                if (getString("REMAINBABYTOOTH", rs) != null) {
                    if ("0".equals(getString("REMAINBABYTOOTH",rs )) == true ) {
                        _svf.VrsOut("REMAINBABYTOOTH"   , "う歯なし");
                    } else if (Integer.parseInt(getString("REMAINBABYTOOTH",rs )) >= 1) {
                        _svf.VrsOut("REMAINBABYTOOTH"   , "う歯あり");
                    }
                }
                //永久歯
                if (getString("REMAINADULTTOOTH",rs ) != null) {
                    if ("0".equals(getString("REMAINADULTTOOTH",rs )) == true ) {
                        _svf.VrsOut("REMAINADULTTOOTH"   , "う歯なし");
                    } else if (Integer.parseInt(getString("REMAINADULTTOOTH",rs )) >= 1) {
                        _svf.VrsOut("REMAINADULTTOOTH"   , "う歯あり");
                    }
                }

                //歯列・咬合の状態
                if (getString("JAWS_JOINTCD", rs) != null) {
                    final int jawsJointCdLen = KNJ_EditEdit.getMS932ByteLength(getString("JAWS_JOINTCD", rs));
                    final String jawsJointCdField = (jawsJointCdLen <= 24)? "1": "2";
                    _svf.VrsOut("JAWS_JOINT1_" + jawsJointCdField, getString("JAWS_JOINTCD", rs));
                }
                //顎関節の状態
                if (getString("JAWS_JOINTCD2", rs) != null) {
                    final int jawsJointCd2Len = KNJ_EditEdit.getMS932ByteLength(getString("JAWS_JOINTCD2", rs));
                    final String jawsJointCd2Field = (jawsJointCd2Len <= 24)? "1": "2";
                    _svf.VrsOut("JAWS_JOINT2_" + jawsJointCd2Field, getString("JAWS_JOINTCD2", rs));
                }

                //歯垢の状態
                if (getString("PLAQUECD",rs ) != null) {
                    _svf.VrsOut("PLAQUE"            , getString("PLAQUECD",rs ));
                }

                //歯肉の状態
                if (getString("GUMCD",rs ) != null) {
                    final int gumCdLen = KNJ_EditEdit.getMS932ByteLength(getString("GUMCD", rs));
                    final String gumCdField = (gumCdLen <= 24)? "1": "2";
                    _svf.VrsOut("GUM" + gumCdField, getString("GUMCD", rs));
                }

                //要注意乳歯
                if (getString("BRACK_BABYTOOTH",rs ) != null) {
                    if ("0".equals(getString("BRACK_BABYTOOTH",rs )) == true ) {
                        _svf.VrsOut("BRACKBABYTOOTH"   , "なし");
                    } else if (Integer.parseInt(getString("BRACK_BABYTOOTH",rs )) >= 1) {
                        _svf.VrsOut("BRACKBABYTOOTH"   , "あり");
                    }
                }

                //要観察歯(CO)
                if (getString("BRACK_ADULTTOOTH",rs ) != null) {
                    if ("0".equals(getString("BRACK_ADULTTOOTH",rs )) == true ) {
                        _svf.VrsOut("BRACKADULTTOOTH"   , "なし");
                    } else if (Integer.parseInt(getString("BRACK_ADULTTOOTH",rs )) >= 1) {
                        _svf.VrsOut("BRACKADULTTOOTH"   , "あり");
                    }
                }

                //歯石沈着
                if (getString("CALCULUS",rs ) != null) {
                    _svf.VrsOut("TARTAR_DEPOSITS"   , getString("CALCULUSNAME",rs ));
                }

                //その他
                if (getString("OTHERDISEASECD",rs ) != null) {
                    if ("00".equals(getString("OTHERDISEASECD",rs )) == true ) {
                        _svf.VrsOut("TOOTHOTHERDISEASE1"   , "未受験");
                    } else if ("01".equals(getString("OTHERDISEASECD",rs )) == true ) {
                        _svf.VrsOut("TOOTHOTHERDISEASE1"   , "所見なし");
                    } else if ("02".equals(getString("OTHERDISEASECD",rs )) == true ) {
                        _svf.VrsOut("TOOTHOTHERDISEASE1"   , "所見あり");
                    }
                }

                //尿検査
                //尿(蛋白)1次
                if (getString("ALBUMINURIA1CD",rs ) != null) {
                    _svf.VrsOut("ALBUMINURIA"       , getString("ALBUMINURIA1CD",rs ));
                }
                //尿(糖)1次
                if (getString("URICSUGAR1CD",rs ) != null) {
                    _svf.VrsOut("URICSUGAR"         , getString("URICSUGAR1CD",rs ));
                }
                //尿(潜血)1次
                if (getString("URICBLEED1CD",rs ) != null) {
                    _svf.VrsOut("URICBLEED"         , getString("URICBLEED1CD",rs ));
                }
                //尿(蛋白)再検査
                if (getString("ALBUMINURIA2CD",rs ) != null) {
                    _svf.VrsOut("ALBUMINURIA2"      , getString("ALBUMINURIA2CD",rs ));
                }
                //尿(糖)再検査
                if (getString("URICSUGAR2CD",rs ) != null) {
                    _svf.VrsOut("URICSUGAR2"        , getString("URICSUGAR2CD",rs ));
                }
                //尿(潜)再検査
                if (getString("URICBLEED2CD",rs ) != null) {
                    _svf.VrsOut("URICBLEED2"        , getString("URICBLEED2CD",rs ));
                }

                //胸部X線
                //高校2,3年生のとき
                if ("02".equals(getString("GRADE",rs )) == true || "03".equals(getString("GRADE",rs )) == true) {
                    _svf.VrsOut("TB_X_RAY1","*");
                } else if ("01".equals(getString("GRADE",rs )) == true){
                    //高校1年生のとき
                    if (getString("TB_REMARKCD",rs ) != null) {
                        if ("99".equals(getString("TB_REMARKCD", rs)) && getString("TB_X_RAY", rs) != null) {
                            //その他が選択されているとき
                            final int tbXRayLen = KNJ_EditEdit.getMS932ByteLength(getString("TB_X_RAY", rs));
                            final String tbXRayField = (tbXRayLen <= 24)? "1": "2";
                            _svf.VrsOut("TB_X_RAY" + tbXRayField, getString("TB_X_RAY", rs));
                        } else {
                            //その他以外が選択されているとき
                            _svf.VrsOut("TB_X_RAY1", getString("TB_REMARKCD_NAME", rs));
                        }
                    }
                }

                //連絡欄
                String REMARK = "";
                if (getString("REMARK", rs) != null) {
                    REMARK = getString("REMARK", rs);
                    _svf.VrsOut("REMARK1",REMARK);
                }
                _svf.VrEndRecord();
            }

            printOk = true;
            return printOk;
        }

        //出力データ取得
        private String getDataSql() {
             final StringBuffer stb = new StringBuffer();

             stb.append("    SELECT SRD.SCHREGNO ");
             stb.append("         , MD.HEIGHT ");
             stb.append("         , MD.WEIGHT ");
             stb.append("         , MD.SPINERIBCD ");
             stb.append("         , NM12.NAME1       AS SPINERIBCD_NAME ");
             stb.append("         , MD14.DET_REMARK1 AS SPINERIBCD1 ");
             stb.append("         , NM13.NAME1       AS SPINERIBCD_NAME1 ");
             stb.append("         , MD14.DET_REMARK2 AS SPINERIBCD_REMARK1 ");
             stb.append("         , MD14.DET_REMARK3 AS SPINERIBCD2 ");
             stb.append("         , NM14.NAME1       AS SPINERIBCD_NAME2 ");
             stb.append("         , MD14.DET_REMARK4 AS SPINERIBCD_REMARK2 ");
             stb.append("         , MD14.DET_REMARK5 AS SPINERIBCD3 ");
             stb.append("         , NM15.NAME1       AS SPINERIBCD_NAME3 ");
             stb.append("         , MD14.DET_REMARK6 AS SPINERIBCD_REMARK3 ");
             stb.append("         , MD.SKINDISEASECD ");
             stb.append("         , NM16.NAME1       AS SKINDISEASECD_NAME ");
             stb.append("         , MD06.DET_REMARK1 AS SKINDISEASECD_REMARK ");
             stb.append("         , MD.HEART_MEDEXAM ");
             stb.append("         , NM17.NAME1       AS HEART_MEDEXAM_NAME ");
             stb.append("         , SRD.GRADE ");
             stb.append("         , NM18.NAME1       AS R_BAREVISION_MARK ");
             stb.append("         , NM19.NAME1       AS L_BAREVISION_MARK ");
             stb.append("         , NM20.NAME1       AS R_VISION_MARK ");
             stb.append("         , NM21.NAME1       AS L_VISION_MARK ");
             stb.append("         , MD.EYEDISEASECD ");
             stb.append("         , NM22.NAME1       AS EYEDISEASECD_NAME ");
             stb.append("         , MD.EYE_TEST_RESULT ");
             stb.append("         , MD.R_EAR         AS R_EAR_CD ");
             stb.append("         , MD.L_EAR         AS L_EAR_CD ");
             stb.append("         , NM.NAME1         AS R_EAR ");
             stb.append("         , NM1.NAME1        AS L_EAR ");
             stb.append("         , NM.ABBV3         AS R_EAR_ABBV3 ");
             stb.append("         , NM1.ABBV3        AS L_EAR_ABBV3 ");
             stb.append("         , MD.NOSEDISEASECD ");
             stb.append("         , NM23.NAME1       AS NOSEDISEASECD_NAME ");
             stb.append("         , MD03.DET_REMARK7 AS NOSEDISEASECD5 ");
             stb.append("         , NM24.NAME1       AS NOSEDISEASECD5_NAME ");
             stb.append("         , MD03.DET_REMARK1 AS NOSEDISEASECD_REMARK1 ");
             stb.append("         , MD03.DET_REMARK8 AS NOSEDISEASECD6 ");
             stb.append("         , NM25.NAME1       AS NOSEDISEASECD6_NAME ");
             stb.append("         , MD03.DET_REMARK2 AS NOSEDISEASECD_REMARK2 ");
             stb.append("         , MD03.DET_REMARK9 AS NOSEDISEASECD7 ");
             stb.append("         , NM26.NAME1       AS NOSEDISEASECD7_NAME ");
             stb.append("         , MD03.DET_REMARK3 AS NOSEDISEASECD_REMARK3 ");
             stb.append("         , MT.REMAINBABYTOOTH ");
             stb.append("         , MT.REMAINADULTTOOTH ");
             stb.append("         , NM2.NAME1        AS JAWS_JOINTCD ");
             stb.append("         , NM3.NAME1        AS JAWS_JOINTCD2 ");
             stb.append("         , NM4.NAME1        AS PLAQUECD ");
             stb.append("         , NM5.NAME1        AS GUMCD ");
             stb.append("         , MT.BRACK_ADULTTOOTH ");
             stb.append("         , MT.BRACK_BABYTOOTH ");
             stb.append("         , MT.CALCULUS ");
             stb.append("         , NM28.NAME1        AS CALCULUSNAME ");
             stb.append("         , MT.OTHERDISEASECD ");
             stb.append("         , NM6.NAME1        AS ALBUMINURIA1CD ");
             stb.append("         , NM7.NAME1        AS URICSUGAR1CD ");
             stb.append("         , NM8.NAME1        AS URICBLEED1CD ");
             stb.append("         , NM9.NAME1        AS ALBUMINURIA2CD ");
             stb.append("         , NM10.NAME1       AS URICSUGAR2CD ");
             stb.append("         , NM11.NAME1       AS URICBLEED2CD ");
             stb.append("         , MD.TB_REMARKCD ");
             stb.append("         , NM27.NAME1       AS TB_REMARKCD_NAME ");
             stb.append("         , MD.TB_X_RAY ");
             stb.append("         , MD.REMARK ");
             stb.append("         , MH.DATE ");
             stb.append("      FROM SCHREG_REGD_DAT SRD ");
             stb.append(" LEFT JOIN MEDEXAM_HDAT MH ");
             stb.append("        ON MH.YEAR     = SRD.YEAR     ");
             stb.append("       AND MH.SCHREGNO = SRD.SCHREGNO ");
             stb.append(" LEFT JOIN MEDEXAM_DET_DAT  MD ");
             stb.append("        ON SRD.SCHREGNO = MD.SCHREGNO ");
             stb.append("       AND SRD.YEAR     = MD.YEAR ");
             stb.append(" LEFT JOIN MEDEXAM_DET_DETAIL_DAT MD14 ");
             stb.append("        ON SRD.YEAR     = MD14.YEAR ");
             stb.append("       AND SRD.SCHREGNO = MD14.SCHREGNO ");
             stb.append("       AND MD14.DET_SEQ = '014' ");
             stb.append(" LEFT JOIN MEDEXAM_DET_DETAIL_DAT MD06 ");
             stb.append("        ON SRD.YEAR     = MD06.YEAR ");
             stb.append("       AND SRD.SCHREGNO = MD06.SCHREGNO ");
             stb.append("       AND MD06.DET_SEQ = '006' ");
             stb.append(" LEFT JOIN NAME_MST NM ");
             stb.append("        ON MD.R_EAR     = NM.NAMECD2 ");
             stb.append("       AND NM.NAMECD1   = 'F010' ");
             stb.append(" LEFT JOIN NAME_MST NM1 ");
             stb.append("        ON MD.L_EAR     = NM1.NAMECD2 ");
             stb.append("       AND NM1.NAMECD1  = 'F010' ");
             stb.append(" LEFT JOIN MEDEXAM_DET_DETAIL_DAT  MD03 ");
             stb.append("        ON SRD.YEAR     = MD03.YEAR ");
             stb.append("       AND SRD.SCHREGNO = MD03.SCHREGNO ");
             stb.append("       AND MD03.DET_SEQ  = '003' ");
             stb.append(" LEFT JOIN MEDEXAM_TOOTH_DAT MT ");
             stb.append("        ON SRD.YEAR      = MT.YEAR ");
             stb.append("       AND SRD.SCHREGNO  = MT.SCHREGNO ");
             stb.append(" LEFT JOIN NAME_MST NM2 ");
             stb.append("        ON MT.JAWS_JOINTCD = NM2.NAMECD2 ");
             stb.append("       AND NM2.NAMECD1     = 'F510' ");
             stb.append(" LEFT JOIN NAME_MST NM3 ");
             stb.append("        ON MT.JAWS_JOINTCD2 = NM3.NAMECD2 ");
             stb.append("       AND NM3.NAMECD1      = 'F511' ");
             stb.append(" LEFT JOIN NAME_MST NM4 ");
             stb.append("        ON MT.PLAQUECD = NM4.NAMECD2 ");
             stb.append("       AND NM4.NAMECD1 = 'F520' ");
             stb.append(" LEFT JOIN NAME_MST NM5 ");
             stb.append("        ON MT.GUMCD    = NM5.NAMECD2 ");
             stb.append("       AND NM5.NAMECD1 = 'F513' ");
             stb.append(" LEFT JOIN NAME_MST NM6 ");
             stb.append("        ON MD.ALBUMINURIA1CD = NM6.NAMECD2 ");
             stb.append("       AND NM6.NAMECD1       = 'F020' ");
             stb.append(" LEFT JOIN NAME_MST NM7 ");
             stb.append("        ON MD.URICSUGAR1CD = NM7.NAMECD2 ");
             stb.append("       AND NM7.NAMECD1     = 'F019' ");
             stb.append(" LEFT JOIN NAME_MST  NM8 ");
             stb.append("        ON MD.URICBLEED1CD = NM8.NAMECD2 ");
             stb.append("       AND NM8.NAMECD1     = 'F018' ");
             stb.append(" LEFT JOIN NAME_MST  NM9 ");
             stb.append("        ON MD.ALBUMINURIA2CD = NM9.NAMECD2 ");
             stb.append("       AND NM9.NAMECD1 = 'F020' ");
             stb.append(" LEFT JOIN NAME_MST  NM10 ");
             stb.append("        ON MD.URICSUGAR2CD = NM10.NAMECD2 ");
             stb.append("       AND NM10.NAMECD1 = 'F019' ");
             stb.append(" LEFT JOIN NAME_MST  NM11 ");
             stb.append("        ON MD.URICBLEED2CD = NM11.NAMECD2 ");
             stb.append("       AND NM11.NAMECD1 = 'F018' ");
             stb.append(" LEFT JOIN NAME_MST  NM12 ");
             stb.append("        ON MD.SPINERIBCD = NM12.NAMECD2 ");
             stb.append("       AND NM12.NAMECD1 = 'F040' ");
             stb.append(" LEFT JOIN NAME_MST  NM13 ");
             stb.append("        ON MD14.DET_REMARK1 = NM13.NAMECD2 ");
             stb.append("       AND NM13.NAMECD1 = 'F041' ");
             stb.append(" LEFT JOIN NAME_MST  NM14 ");
             stb.append("        ON MD14.DET_REMARK3 = NM14.NAMECD2 ");
             stb.append("       AND NM14.NAMECD1 = 'F041' ");
             stb.append(" LEFT JOIN NAME_MST  NM15 ");
             stb.append("        ON MD14.DET_REMARK5 = NM15.NAMECD2 ");
             stb.append("       AND NM15.NAMECD1 = 'F041' ");
             stb.append(" LEFT JOIN NAME_MST  NM16 ");
             stb.append("        ON MD.SKINDISEASECD = NM16.NAMECD2 ");
             stb.append("       AND NM16.NAMECD1 = 'F070' ");
             stb.append(" LEFT JOIN NAME_MST  NM17 ");
             stb.append("        ON MD.HEART_MEDEXAM = NM17.NAMECD2 ");
             stb.append("       AND NM17.NAMECD1 = 'F080' ");
             stb.append(" LEFT JOIN NAME_MST  NM18 ");
             stb.append("        ON MD.R_BAREVISION_MARK = NM18.NAMECD2 ");
             stb.append("       AND NM18.NAMECD1 = 'F017' ");
             stb.append(" LEFT JOIN NAME_MST  NM19 ");
             stb.append("        ON MD.L_BAREVISION_MARK = NM19.NAMECD2 ");
             stb.append("       AND NM19.NAMECD1 = 'F017' ");
             stb.append(" LEFT JOIN NAME_MST  NM20 ");
             stb.append("        ON MD.R_VISION_MARK = NM20.NAMECD2 ");
             stb.append("       AND NM20.NAMECD1 = 'F017' ");
             stb.append(" LEFT JOIN NAME_MST  NM21 ");
             stb.append("        ON MD.L_VISION_MARK = NM21.NAMECD2 ");
             stb.append("       AND NM21.NAMECD1 = 'F017' ");
             stb.append(" LEFT JOIN NAME_MST  NM22 ");
             stb.append("        ON MD.EYEDISEASECD = NM22.NAMECD2 ");
             stb.append("       AND NM22.NAMECD1 = 'F050' ");
             stb.append(" LEFT JOIN NAME_MST  NM23 ");
             stb.append("        ON MD.NOSEDISEASECD = NM23.NAMECD2 ");
             stb.append("       AND NM23.NAMECD1 = 'F060' ");
             stb.append(" LEFT JOIN NAME_MST  NM24 ");
             stb.append("        ON MD03.DET_REMARK7 = NM24.NAMECD2 ");
             stb.append("       AND NM24.NAMECD1 = 'F061' ");
             stb.append(" LEFT JOIN NAME_MST  NM25 ");
             stb.append("        ON MD03.DET_REMARK8 = NM25.NAMECD2 ");
             stb.append("       AND NM25.NAMECD1 = 'F061' ");
             stb.append(" LEFT JOIN NAME_MST  NM26 ");
             stb.append("        ON MD03.DET_REMARK9 = NM26.NAMECD2 ");
             stb.append("       AND NM26.NAMECD1 = 'F061' ");
             stb.append(" LEFT JOIN NAME_MST  NM27 ");
             stb.append("        ON MD.TB_REMARKCD = NM27.NAMECD2 ");
             stb.append("       AND NM27.NAMECD1 = 'F100' ");
             stb.append(" LEFT JOIN NAME_MST  NM28 ");
             stb.append("        ON MT.CALCULUS = NM28.NAMECD2 ");
             stb.append("       AND NM28.NAMECD1 = 'F521' ");
             stb.append("     WHERE SRD.YEAR     = '" + _param._year + "'");
             stb.append("       AND SRD.SEMESTER = '" + _param._gakki + "'");
             if (_param._kubun.equals("1")) { //1:クラス
                 stb.append("       AND SRD.GRADE || SRD.HR_CLASS IN " + _param._selectInState + " ");
             }else if (_param._kubun.equals("2")) { //2:個人
                 stb.append("       AND SRD.SCHREGNO IN " + _param._selectInState + " ");
             }
             stb.append("  ORDER BY SRD.GRADE ");
             stb.append("         , SRD.HR_CLASS ");
             stb.append("         , SRD.ATTENDNO ");

             return stb.toString();
        }
        /** 表示内容を切替 **/
        private String  chengeDisplay(String cd, String remark, String name) {
            String str = "";
            if ("99".equals(cd)) {
                if (remark != null) {
                    str += remark;
                }
            } else if (cd != null) {
                str += name ;
            }
            return str;
        }
    }

    //========================================================================
    /** 健康診断結果通知書（運動器） */
    private class TutiUndouki extends TutiAbstract {
        //コンストラクタ
        public TutiUndouki(final Param param, final DB2UDB db2, final Vrw32alp svf) {
            super(param, db2, svf, TITLE_CD_TUTI_UNDOUKI);
        }

        //帳票出力
        public boolean printMain() {
            boolean printOk = false;

            switch (getPageKind())
            {
            case Medical:
                setForm(_param, _svf, "KNJF030J_4_1.xml", 4);
                break;
            case Parent:
                setForm(_param, _svf, "KNJF030J_4_2.xml", 4);
                break;
            }

            final String psKey = "getDataSql44";
            if (null == _param._psMap.get(psKey)) {
                final String sql = getDataSql(getPageKind());
                if (_param._isOutputDebug) {
                    log.info(" sql = " + sql);
                }

                try {
                    _param._psMap.put(psKey, _db2.prepareStatement(sql));
                } catch (SQLException e) {
                    log.error("error!", e);
                }
            }

            //見つかった生徒の数だけループ
            for (final Map rs : KnjDbUtils.query(_db2, _param._psMap.get(psKey), new Object[] { /*プリペアドステートメントの引数をセットする*/ })) {
                if (   getString("SPINERIBCD", rs) == null
                    || (getString("ABBV2", rs) != null && getString("ABBV2", rs).equals("1"))) {
                    continue;
                }
                printOk = true;

                {
                    String studentName = getStudentName(_param._year, _param._gakki, getString("SCHREGNO", rs));
                    final int studentNameLen = KNJ_EditEdit.getMS932ByteLength(studentName);
                    final String studentNameField = (studentNameLen <= 40)? "1":
                                                           (studentNameLen <= 50)? "2":
                                                               (studentNameLen <= 60)? "3": "4";
                    _svf.VrsOut("NAME1_" + studentNameField, studentName);
                    _svf.VrsOut("NAME2_" + studentNameField, studentName);
                }

                printHeadDate(_param._sendDate);
                _svf.VrsOut("SCHOOL_NAME"   ,  getSchoolName(_param._year));
                _svf.VrsOut("PRINCIPAL_NAME",  getSchoolName(_param._year) + "長");
                printTitle();

                _svf.VrsOut("SPINERIB1",        getString("SPINERIB1", rs));
                if (getString("SPINERIB_REMARK1", rs) != null) {
                    _svf.VrsOut("SPINERIB_REMARK1", "所見：" + getString("SPINERIB_REMARK1", rs));
                }
                _svf.VrsOut("SPINERIB2",        getString("SPINERIB2", rs));
                if (getString("SPINERIB_REMARK2", rs) != null) {
                    _svf.VrsOut("SPINERIB_REMARK2", "所見：" + getString("SPINERIB_REMARK2", rs));
                }
                _svf.VrsOut("SPINERIB3",        getString("SPINERIB3", rs));
                if (getString("SPINERIB_REMARK3", rs) != null) {
                    _svf.VrsOut("SPINERIB_REMARK3", "所見：" + getString("SPINERIB_REMARK3", rs));
                }
                _svf.VrsOut("ERA_NAME", getGengo(_param._year).substring(0, 2));
                _svf.VrEndRecord();
            }

            return printOk;
        }

        //出力データ取得SQL
        private String getDataSql(PageKind pageKind) {
             final StringBuffer stb = new StringBuffer();

             stb.append("       SELECT SRD.SCHREGNO");
             stb.append("            , MH.DATE");
             stb.append("            , MDD.SPINERIBCD");           //出力するかどうかの判断に使用する
             stb.append("            , NM1.ABBV2");                //出力するかどうかの判断に使用する
             stb.append("            , NM2.NAME1         AS SPINERIB1");
             stb.append("            , MD014.DET_REMARK2 AS SPINERIB_REMARK1");
             stb.append("            , NM3.NAME1         AS SPINERIB2");
             stb.append("            , MD014.DET_REMARK4 AS SPINERIB_REMARK2");
             stb.append("            , NM4.NAME1         AS SPINERIB3");
             stb.append("            , MD014.DET_REMARK6 AS SPINERIB_REMARK3");
             stb.append("         FROM SCHREG_REGD_DAT        SRD");
             stb.append("    LEFT JOIN MEDEXAM_HDAT           MH");
             stb.append("           ON SRD.SCHREGNO      = MH.SCHREGNO");
             stb.append("          AND SRD.YEAR          = MH.YEAR");
             stb.append("    LEFT JOIN MEDEXAM_DET_DAT        MDD");
             stb.append("           ON SRD.SCHREGNO      = MDD.SCHREGNO");
             stb.append("          AND SRD.YEAR          = MDD.YEAR");
             stb.append("    LEFT JOIN MEDEXAM_DET_DETAIL_DAT MD014");
             stb.append("           ON SRD.SCHREGNO      = MD014.SCHREGNO");
             stb.append("          AND SRD.YEAR  = MD014.YEAR");
             stb.append("          AND MD014.DET_SEQ     = '014'");
             stb.append("    LEFT JOIN NAME_MST               NM1");
             stb.append("           ON MDD.SPINERIBCD    = NM1.NAMECD2");
             stb.append("          AND NM1.NAMECD1       = 'F040'");
             stb.append("    LEFT JOIN NAME_MST               NM2");
             stb.append("           ON MD014.DET_REMARK1 = NM2.NAMECD2");
             stb.append("          AND NM2.NAMECD1       = 'F041'");
             stb.append("    LEFT JOIN NAME_MST               NM3");
             stb.append("           ON MD014.DET_REMARK3 = NM3.NAMECD2");
             stb.append("          AND NM3.NAMECD1       = 'F041'");
             stb.append("    LEFT JOIN NAME_MST               NM4");
             stb.append("           ON MD014.DET_REMARK5 = NM4.NAMECD2");
             stb.append("          AND NM4.NAMECD1       = 'F041'");
             stb.append("        WHERE SRD.YEAR          = '" + _param._year + "'");
             stb.append("          AND SRD.SEMESTER      = '" + _param._gakki + "'");
             if (_param._kubun.equals("1")) { //1:クラス
                 stb.append("        AND SRD.GRADE || SRD.HR_CLASS IN " + _param._selectInState + " ");
             } else if (_param._kubun.equals("2")) { //2:個人
                 stb.append("        AND SRD.SCHREGNO IN " + _param._selectInState + " ");
             }
             stb.append(" ORDER BY SRD.GRADE");
             stb.append("        , SRD.HR_CLASS");
             stb.append("        , SRD.ATTENDNO");

             return stb.toString();
        }
    }

    //========================================================================
    /** 健康診断結果通知書（眼科） */
    private class TutiGanka extends TutiAbstract {
        //コンストラクタ
        public TutiGanka(final Param param, final DB2UDB db2, final Vrw32alp svf) {
            super(param, db2, svf, TITLE_CD_TUTI_GANKA);
        }

        //帳票出力
        public boolean printMain() {
            boolean printOk = false;

            switch (getPageKind())
            {
            case Medical:
                setForm(_param, _svf, "KNJF030J_5_1.xml", 4);
                break;
            case Parent:
                setForm(_param, _svf, "KNJF030J_5_2.xml", 4);
                break;
            }

            final String psKey = "getDataSql45";
            if (null == _param._psMap.get(psKey)) {
                final String sql = getDataSql(getPageKind());
                if (_param._isOutputDebug) {
                    log.info(" sql = " + sql);
                }

                try {
                    _param._psMap.put(psKey, _db2.prepareStatement(sql));
                } catch (SQLException e) {
                    log.error("error!", e);
                }
            }

            //見つかった生徒の数だけループ
            for (final Map rs : KnjDbUtils.query(_db2, _param._psMap.get(psKey), new Object[] { /*プリペアドステートメントの引数をセットする*/ })) {
                if (   getString("EYEDISEASECD", rs) == null
                    || (getString("ABBV2", rs) != null && getString("ABBV2", rs).equals("1"))) {
                    continue;
                }
                printOk = true;
                {
                    String studentName = getStudentName(_param._year, _param._gakki, getString("SCHREGNO", rs));
                    final int studentNameLen = KNJ_EditEdit.getMS932ByteLength(studentName);
                    final String studentNameField = (studentNameLen <= 40)? "1":
                                                           (studentNameLen <= 50)? "2":
                                                               (studentNameLen <= 60)? "3": "4";
                    _svf.VrsOut("NAME1_" + studentNameField, studentName);
                    _svf.VrsOut("NAME2_" + studentNameField, studentName);
                }
                printHeadDate(_param._sendDate);
                _svf.VrsOut("SCHOOL_NAME"   ,  getSchoolName(_param._year));
                _svf.VrsOut("PRINCIPAL_NAME",  getSchoolName(_param._year) + "長");
                printTitle();

                _svf.VrsOut("EYEDISEASE",      getString("EYEDISEASE", rs));
                _svf.VrsOut("EYE_TEST_RESULT", getString("EYE_TEST_RESULT", rs));
                _svf.VrsOut("ERA_NAME", getGengo(_param._year).substring(0, 2));
                _svf.VrEndRecord();
            }

             return printOk;
        }

        //出力データ取得SQL
        private String getDataSql(PageKind pageKind) {
             final StringBuffer stb = new StringBuffer();

             stb.append("     SELECT SRD.SCHREGNO");
             stb.append("          , MH.DATE");
             stb.append("          , MDD.EYEDISEASECD");           //出力するかどうかの判断に使用する
             stb.append("          , NM1.ABBV2");                  //出力するかどうかの判断に使用する
             stb.append("          , CASE");
             stb.append("                 WHEN MDD.EYEDISEASECD = '99' THEN NULL");    //「99：その他」
             stb.append("                 ELSE NM1.NAME1");
             stb.append("            END AS EYEDISEASE");
             stb.append("          , CASE");
             stb.append("                 WHEN MDD.EYE_TEST_RESULT IS NOT NULL THEN '所見：' || MDD.EYE_TEST_RESULT");
             stb.append("                 ELSE NULL");
             stb.append("            END AS EYE_TEST_RESULT");
             stb.append("       FROM SCHREG_REGD_DAT   SRD");
             stb.append("  LEFT JOIN MEDEXAM_HDAT      MH");
             stb.append("         ON SRD.SCHREGNO      = MH.SCHREGNO");
             stb.append("        AND SRD.YEAR          = MH.YEAR");
             stb.append("  LEFT JOIN MEDEXAM_DET_DAT   MDD");
             stb.append("         ON SRD.SCHREGNO      = MDD.SCHREGNO");
             stb.append("        AND SRD.YEAR          = MDD.YEAR");
             stb.append("  LEFT JOIN NAME_MST          NM1");
             stb.append("         ON MDD.EYEDISEASECD  = NM1.NAMECD2");
             stb.append("        AND NM1.NAMECD1       = 'F050'");
             stb.append("      WHERE SRD.YEAR          = '" + _param._year + "'");
             stb.append("        AND SRD.SEMESTER      = '" + _param._gakki + "'");
             if (_param._kubun.equals("1")) { //1:クラス
                 stb.append("        AND SRD.GRADE || SRD.HR_CLASS IN " + _param._selectInState + " ");
             } else if (_param._kubun.equals("2")) { //2:個人
                 stb.append("        AND SRD.SCHREGNO IN " + _param._selectInState + " ");
             }
             stb.append("   ORDER BY SRD.GRADE");
             stb.append("          , SRD.HR_CLASS");
             stb.append("          , SRD.ATTENDNO");

             return stb.toString();
        }
    }

    //========================================================================
    /** 健康診断結果通知書（結核） */
    private class TutiKekkaku extends TutiAbstract {
        //コンストラクタ
        public TutiKekkaku(final Param param, final DB2UDB db2, final Vrw32alp svf) {
            super(param, db2, svf, TITLE_CD_TUTI_KEKKAKU);
        }

        //帳票出力
        public boolean printMain() {
            boolean printOk = false;

            switch (getPageKind())
            {
            case Medical:
                setForm(_param, _svf, "KNJF030J_6_1.xml", 4);
                break;
            case Parent:
                setForm(_param, _svf, "KNJF030J_6_2.xml", 4);
                break;
            }

            final String psKey = "getDataSql46";
            if (null == _param._psMap.get(psKey)) {
                final String sql = getDataSql(getPageKind());
                if (_param._isOutputDebug) {
                    log.info(" sql = " + sql);
                }

                try {
                    _param._psMap.put(psKey, _db2.prepareStatement(sql));
                } catch (SQLException e) {
                    log.error("error!", e);
                }
            }

            //見つかった生徒の数だけループ
            for (final Map rs : KnjDbUtils.query(_db2, _param._psMap.get(psKey), new Object[] { /*プリペアドステートメントの引数をセットする*/ })) {
                if (   getString("TB_REMARKCD", rs) == null
                    || (getString("ABBV2", rs) != null && getString("ABBV2", rs).equals("1"))) {
                    continue;
                }
                printOk = true;

                {
                    String studentName = getStudentName(_param._year, _param._gakki, getString("SCHREGNO", rs));
                    final int studentNameLen = KNJ_EditEdit.getMS932ByteLength(studentName);
                    final String studentNameField = (studentNameLen <= 40)? "1":
                                                           (studentNameLen <= 50)? "2":
                                                               (studentNameLen <= 60)? "3": "4";
                    //生徒名
                    _svf.VrsOut("NAME1_" + studentNameField, studentName);
                    _svf.VrsOut("NAME2_" + studentNameField, studentName);
                }
                //右上の日付
                printHeadDate(_param._sendDate);

                //学校名
                _svf.VrsOut("SCHOOL_NAME"   ,  getSchoolName(_param._year));

                //学校長名
                _svf.VrsOut("PRINCIPAL_NAME",  getSchoolName(_param._year) + "長");
                printTitle();

                //受診報告日付元号
                _svf.VrsOut("ERA_NAME",  getGengo(_param._year).substring(0, 2));

                _svf.VrEndRecord();
            }

            return printOk;
        }

        //出力データ取得SQL
        private String getDataSql(PageKind pageKind) {
             final StringBuffer stb = new StringBuffer();

             stb.append("     SELECT SRD.SCHREGNO");
             stb.append("          , MH.DATE");
             stb.append("          , MDD.TB_REMARKCD");           //出力するかどうかの判断に使用する
             stb.append("          , NM1.ABBV2");                 //出力するかどうかの判断に使用する
             stb.append("          , CASE");
             stb.append("                 WHEN MDD.TB_REMARKCD = '99' THEN NULL");    //「99：その他」
             stb.append("                 ELSE NM1.NAME1");
             stb.append("            END AS TB_REMARK");
             stb.append("          , CASE");
             stb.append("                 WHEN MDD.TB_X_RAY IS NOT NULL THEN '所見：' || MDD.TB_X_RAY");
             stb.append("                 ELSE NULL");
             stb.append("            END AS TB_X_RAY");
             stb.append("       FROM SCHREG_REGD_DAT   SRD");
             stb.append("  LEFT JOIN MEDEXAM_HDAT      MH");
             stb.append("         ON SRD.SCHREGNO      = MH.SCHREGNO");
             stb.append("        AND SRD.YEAR          = MH.YEAR");
             stb.append("  LEFT JOIN MEDEXAM_DET_DAT   MDD");
             stb.append("         ON SRD.SCHREGNO      = MDD.SCHREGNO");
             stb.append("        AND SRD.YEAR          = MDD.YEAR");
             stb.append("  LEFT JOIN NAME_MST          NM1");
             stb.append("         ON MDD.TB_REMARKCD   = NM1.NAMECD2");
             stb.append("        AND NM1.NAMECD1       = 'F100'");
             stb.append("      WHERE SRD.YEAR          = '" + _param._year + "'");
             stb.append("        AND SRD.SEMESTER      = '" + _param._gakki + "'");
             if (_param._kubun.equals("1")) { //1:クラス
                 stb.append("        AND SRD.GRADE || SRD.HR_CLASS IN " + _param._selectInState + " ");
             } else if (_param._kubun.equals("2")) { //2:個人
                 stb.append("        AND SRD.SCHREGNO IN " + _param._selectInState + " ");
             }
             stb.append("   ORDER BY SRD.GRADE");
             stb.append("          , SRD.HR_CLASS");
             stb.append("          , SRD.ATTENDNO");

             return stb.toString();
        }
    }

    //========================================================================
    /** 健康診断結果通知書（視力） */
    private class TutiSiryoku extends TutiAbstract {
        //コンストラクタ
        public TutiSiryoku(final Param param, final DB2UDB db2, final Vrw32alp svf) {
            super(param, db2, svf, TITLE_CD_TUTI_SIRYOKU);
        }

        //帳票出力
        public boolean printMain() {
            boolean printOk = false;

            switch (getPageKind())
            {
            case Medical:
                setForm(_param, _svf, "KNJF030J_7_1.xml", 4);
                break;
            case Parent:
                setForm(_param, _svf, "KNJF030J_7_2.xml", 4);
                break;
            }

            final String psKey = "getDataSql47";
            if (null == _param._psMap.get(psKey)) {
                final String sql = getDataSql(getPageKind());
                if (_param._isOutputDebug) {
                    log.info(" sql = " + sql);
                }

                try {
                    _param._psMap.put(psKey, _db2.prepareStatement(sql));
                } catch (SQLException e) {
                    log.error("error!", e);
                }
            }

            //見つかった生徒の数だけループ
            for (final Map rs : KnjDbUtils.query(_db2, _param._psMap.get(psKey), new Object[] { /*プリペアドステートメントの引数をセットする*/ })) {
                if (   getString("R_BAREVISION_MARK", rs) == null && getString("L_BAREVISION_MARK", rs) == null
                    && getString("R_VISION_MARK",     rs) == null && getString("L_VISION_MARK",     rs) == null) {
                    continue;
                } else if (isCheckOutput(rs) == false) {
                    continue;
                } else if (   (getString("R_BAREVISION_MARK", rs) != null && getString("R_BAREVISION_MARK", rs).equals("01"))
                          && (getString("L_BAREVISION_MARK", rs) != null && getString("L_BAREVISION_MARK", rs).equals("01"))) {
                    continue;
                } else if (   (getString("R_VISION_MARK",     rs) != null && getString("R_VISION_MARK",     rs).equals("01"))
                          && (getString("L_VISION_MARK",     rs) != null && getString("L_VISION_MARK",     rs).equals("01"))) {
                    continue;
                }
                printOk = true;

                {
                    String studentName = getStudentName(_param._year, _param._gakki, getString("SCHREGNO", rs));
                    final int studentNameLen = KNJ_EditEdit.getMS932ByteLength(studentName);
                    final String studentNameField = (studentNameLen <= 40)? "1":
                                                           (studentNameLen <= 50)? "2":
                                                               (studentNameLen <= 60)? "3": "4";
                    _svf.VrsOut("NAME1_" + studentNameField, studentName);
                    _svf.VrsOut("NAME2_" + studentNameField, studentName);
                }

                printHeadDate(_param._sendDate);
                _svf.VrsOut("SCHOOL_NAME"   ,  getSchoolName(_param._year));
                _svf.VrsOut("PRINCIPAL_NAME",  getSchoolName(_param._year) + "長");
                printTitle();

                _svf.VrsOut("R_BAREVISION_MARK", getString("R_BAREVISION", rs));
                _svf.VrsOut("L_BAREVISION_MARK", getString("L_BAREVISION", rs));
                _svf.VrsOut("R_VISION_MARK",     getString("R_VISION", rs));
                _svf.VrsOut("L_VISION_MARK",     getString("L_VISION", rs));
                _svf.VrsOut("ERA_NAME",          getGengo(_param._year).substring(0, 2));
                _svf.VrEndRecord();
            }

            return printOk;
        }

        //出力データ取得SQL
        private String getDataSql(PageKind pageKind) {
             final StringBuffer stb = new StringBuffer();

             stb.append("     SELECT SRD.SCHREGNO");
             stb.append("          , MH.DATE");
             stb.append("          , MDD.R_BAREVISION_MARK");            //出力するかどうかの判断に使用する
             stb.append("          , MDD.L_BAREVISION_MARK");            //出力するかどうかの判断に使用する
             stb.append("          , MDD.R_VISION_MARK");                //出力するかどうかの判断に使用する
             stb.append("          , MDD.L_VISION_MARK");                //出力するかどうかの判断に使用する
             stb.append("          , NM1.ABBV2 AS R_BAREVISION_ABBV2");  //出力するかどうかの判断に使用する
             stb.append("          , NM2.ABBV2 AS L_BAREVISION_ABBV2");  //出力するかどうかの判断に使用する
             stb.append("          , NM3.ABBV2 AS R_VISION_ABBV2");      //出力するかどうかの判断に使用する
             stb.append("          , NM4.ABBV2 AS L_VISION_ABBV2");      //出力するかどうかの判断に使用する
             stb.append("          , NM1.NAME1 AS R_BAREVISION");
             stb.append("          , NM2.NAME1 AS L_BAREVISION");
             stb.append("          , NM3.NAME1 AS R_VISION");
             stb.append("          , NM4.NAME1 AS L_VISION");
             stb.append("       FROM SCHREG_REGD_DAT   SRD");
             stb.append("  LEFT JOIN MEDEXAM_HDAT      MH");
             stb.append("         ON SRD.SCHREGNO          = MH.SCHREGNO");
             stb.append("        AND SRD.YEAR              = MH.YEAR");
             stb.append("  LEFT JOIN MEDEXAM_DET_DAT   MDD");
             stb.append("         ON SRD.SCHREGNO          = MDD.SCHREGNO");
             stb.append("        AND SRD.YEAR              = MDD.YEAR");
             stb.append("  LEFT JOIN NAME_MST          NM1");
             stb.append("         ON MDD.R_BAREVISION_MARK = NM1.NAMECD2");
             stb.append("        AND NM1.NAMECD1           = 'F017'");
             stb.append("  LEFT JOIN NAME_MST          NM2");
             stb.append("         ON MDD.L_BAREVISION_MARK = NM2.NAMECD2");
             stb.append("        AND NM2.NAMECD1           = 'F017'");
             stb.append("  LEFT JOIN NAME_MST          NM3");
             stb.append("         ON MDD.R_VISION_MARK     = NM3.NAMECD2");
             stb.append("        AND NM3.NAMECD1           = 'F017'");
             stb.append("  LEFT JOIN NAME_MST          NM4");
             stb.append("         ON MDD.L_VISION_MARK     = NM4.NAMECD2");
             stb.append("        AND NM4.NAMECD1           = 'F017'");
             stb.append("      WHERE SRD.YEAR          = '" + _param._year + "'");
             stb.append("        AND SRD.SEMESTER      = '" + _param._gakki + "'");
             if (_param._kubun.equals("1")) { //1:クラス
                 stb.append("        AND SRD.GRADE || SRD.HR_CLASS IN " + _param._selectInState + " ");
             } else if (_param._kubun.equals("2")) { //2:個人
                 stb.append("        AND SRD.SCHREGNO IN " + _param._selectInState + " ");
             }
             stb.append("   ORDER BY SRD.GRADE");
             stb.append("          , SRD.HR_CLASS");
             stb.append("          , SRD.ATTENDNO");

             return stb.toString();
        }

        //出力するかのチェック
        private boolean isCheckOutput(Map rs) {
            //記号が入力されている項目のABBV2が全て1の場合は帳票を出力しない
            String[] columns = {  "R_BAREVISION"   //右裸眼
                                 , "L_BAREVISION"   //左裸眼
                                 , "R_VISION"       //右矯正
                                 , "L_VISION"       //左矯正
                                 };
            for (String col : columns) {
                if (getString(col + "_MARK", rs) != null) {
                    if (   getString(col + "_ABBV2", rs) != null
                        && getString(col + "_ABBV2", rs).equals("1")) {
                        continue;
                    } else {
                        return true;
                    }
                }
            }
            return false;    //帳票を出力しない
        }
    }

    //========================================================================
    /** 健康診断結果通知書（歯科） */
    private class TutiSika extends TutiAbstract {
        //コンストラクタ
        public TutiSika(final Param param, final DB2UDB db2, final Vrw32alp svf) {
            super(param, db2, svf, TITLE_CD_TUTI_SIKA);
        }

        //帳票出力
        public boolean printMain() {
            boolean printOk = false;
            final String JAWS_PRESENT_TEXT   = "特に気になるようでしたら、かかりつけ歯科医や専門医で相談してください。　＊矯正治療中の方もこの項目に含まれます。";
            final String CONSUL_PRESENT_TEXT = "のことで相談し、必要があれば検査・治療を受けましょう。";
            final String TOOTH_REMARK_TEXT   = "のため、検査または治療を受けてください。";
            String mark = "";
            boolean npMarkFlg = true;

            switch (getPageKind())
            {
            case Medical:
                setForm(_param, _svf, "KNJF030J_8_1.xml", 4);
                break;
            case Parent:
                setForm(_param, _svf, "KNJF030J_8_2.xml", 4);
                break;
            }

            final String psKey = "getDataSql48";
            if (null == _param._psMap.get(psKey)) {
                final String sql = getDataSql(getPageKind());
                if (_param._isOutputDebug) {
                    log.info(" sql = " + sql);
                }

                try {
                    _param._psMap.put(psKey, _db2.prepareStatement(sql));
                } catch (SQLException e) {
                    log.error("error!", e);
                }
            }

            //見つかった生徒の数だけループ
            for (final Map rs : KnjDbUtils.query(_db2, _param._psMap.get(psKey), new Object[] { /*プリペアドステートメントの引数をセットする*/ })) {
                printOk = true;
                npMarkFlg = true;

                {
                    String studentName = getStudentName(_param._year, _param._gakki, getString("SCHREGNO", rs));
                    final int studentNameLen = KNJ_EditEdit.getMS932ByteLength(studentName);
                    final String studentNameField = (studentNameLen <= 40)? "1":
                                                           (studentNameLen <= 50)? "2":
                                                               (studentNameLen <= 60)? "3": "4";
                    _svf.VrsOut("NAME1_" + studentNameField, studentName);
                    _svf.VrsOut("NAME2_" + studentNameField, studentName);
                }

                printHeadDate(_param._sendDate);
                _svf.VrsOut("PRINCIPAL_NAME",  getSchoolName(_param._year) + "長");
                printTitle();

                //経過観察・受診のすすめ（CO）
                mark = "";
                //(14)(15)(16)のいずれかに「01」が格納されている場合
                if (   (getString("DENTISTREMARKCD", rs) != null && (getString("DENTISTREMARKCD", rs).equals("01")))
                    || (getString("TOOTH_REMARK1",   rs) != null && (getString("TOOTH_REMARK1",   rs).equals("01")))
                    || (getString("TOOTH_REMARK3",   rs) != null && (getString("TOOTH_REMARK3",   rs).equals("01")))) {
                    mark = "〇";
                    npMarkFlg = false;
                }
                _svf.VrsOut("CO_MARK", mark);

                //経過観察（GO）
                mark = "";
                //(17)に「02」（定期的観察が必要）が格納されている場合
                if (   (getString("GUMCD",  rs) != null
                    && (getString("GUMCD",  rs).equals("02")))) {
                    mark = "〇";
                    npMarkFlg = false;
                }
                //(14)(15)(16)のいずれかに「03」(GO（歯周疾患要観察者）)が格納されている場合
                else if (   (getString("DENTISTREMARKCD", rs) != null && (getString("DENTISTREMARKCD", rs).equals("03")))
                          || (getString("TOOTH_REMARK1",   rs) != null && (getString("TOOTH_REMARK1",   rs).equals("03")))
                          || (getString("TOOTH_REMARK3",   rs) != null && (getString("TOOTH_REMARK3",   rs).equals("03")))) {
                    mark = "〇";
                    npMarkFlg = false;
                }
                _svf.VrsOut("GO_MARK", mark);

                //経過観察（歯垢の付着）
                mark = "";
                //(18)に「02」（若干の付着あり）や「03」（相当の付着あり）が格納されている場合
                if (      (getString("PLAQUECD",  rs) != null
                    && (   (getString("PLAQUECD",  rs).equals("02"))
                        || (getString("PLAQUECD",  rs).equals("03"))))) {
                    mark = "〇";
                    npMarkFlg = false;
                }
                _svf.VrsOut("PLAQUE_MARK", mark);

                //経過観察（歯列・咬合顎関節）
                String jaws_present = "";
                mark = "";
                //(9)(10)の両方に「02」（定期的な観察が必要）が格納されている場合
                if (   (getString("JAWS_JOINTCD2",  rs) != null && (getString("JAWS_JOINTCD2",  rs).equals("02")))
                    && (getString("JAWS_JOINTCD",   rs) != null && (getString("JAWS_JOINTCD",   rs).equals("02")))) {
                    jaws_present = "歯並び・かみ合わせに不正が見られる、あごの関節が痛くて開きづらいなどの症状が見られます。";
                    mark = "〇";
                    npMarkFlg = false;
                } else if (   (getString("JAWS_JOINTCD2",  rs) != null
                          && (getString("JAWS_JOINTCD2",  rs).equals("02")))) {
                    //(9)に「02」（定期的な観察が必要）が格納されている場合
                    jaws_present = "あごの関節が痛くて開きづらい症状が見られます。";
                    mark = "〇";
                    npMarkFlg = false;
                } else if (   (getString("JAWS_JOINTCD",  rs) != null
                          && (getString("JAWS_JOINTCD",  rs).equals("02")))) {
                    //(10)に「02」（定期的な観察が必要）が格納されている場合
                    jaws_present = "歯並び・かみ合わせに不正が見られます。";
                    mark = "〇";
                    npMarkFlg = false;
                } else {
                    //(9)(10)のいずれも「02」（定期的な観察が必要）が格納されていない場合
                    jaws_present = "歯並び・かみ合わせに不正が見られる、あごの関節が痛くて開きづらいなどの症状が見られます。";
                }
                if (jaws_present.equals("")) {
                } else {
                    jaws_present += JAWS_PRESENT_TEXT;
                    _svf.VrsOut("JAWS_PRESENT1", jaws_present);
                }
                _svf.VrsOut("JAWS_MARK", mark);

                //受診のおすすめ（むし歯（C）があります）
                mark = "";
                //(19)(20)の両方に1以上の数が格納されている場合
                if (    1 <= toInt((getString("REMAINBABYTOOTH", rs)), 0)
                     && 1 <= toInt((getString("REMAINADULTTOOTH", rs)), 0)) {
                    mark = "〇";
                    npMarkFlg = false;
                } else if ( 1 <= toInt((getString("REMAINBABYTOOTH", rs)), 0)) {
                    //(19)に1以上の数が格納されている場合
                    mark = "〇";
                    npMarkFlg = false;
                } else if ( 1 <= toInt((getString("REMAINADULTTOOTH", rs)), 0)) {
                    //(20)に1以上の数が格納されている場合
                    mark = "〇";
                    npMarkFlg = false;
                }
                _svf.VrsOut("BT_MARK", mark);

                //受診のおすすめ（歯肉の病気があります）
                mark = "";
                //(17)に「03」（専門医（歯科医師）による診断が必要）が格納されている場合
                if (   (getString("GUMCD",  rs) != null
                    && (getString("GUMCD",  rs).equals("03")))) {
                    mark = "〇";
                    npMarkFlg = false;
                }
                _svf.VrsOut("GUM_MARK", mark);

                //受診のおすすめ（検査が必要な歯）の文言
                String exam_item = "";
                mark = "";
                //(14)(15)(16)のいずれかに「02」（CO要相談)が格納されている
                //  AND
                //(13)に格納されている数が1以上の場合
                if (  ((getString("DENTISTREMARKCD", rs) != null && (getString("DENTISTREMARKCD", rs).equals("02")))
                       || (getString("TOOTH_REMARK1", rs) != null && (getString("TOOTH_REMARK1",   rs).equals("02")))
                       || (getString("TOOTH_REMARK3", rs) != null && (getString("TOOTH_REMARK3",   rs).equals("02"))) )
                    && 1 <= toInt((getString("BRACK_BABYTOOTH", rs)), 0)) {
                    exam_item = "CO要相談、要注意乳歯";
                    mark = "〇";
                    npMarkFlg = false;
                } else if (1 <= toInt((getString("BRACK_BABYTOOTH", rs)), 0)) {
                    //(13)に格納されている数字が1以上の場合
                    exam_item = "要注意乳歯";
                    mark = "〇";
                    npMarkFlg = false;
                } else if (   (getString("DENTISTREMARKCD", rs) != null && (getString("DENTISTREMARKCD", rs).equals("02")))
                          || (getString("TOOTH_REMARK1",   rs) != null && (getString("TOOTH_REMARK1",   rs).equals("02")))
                          || (getString("TOOTH_REMARK3",   rs) != null && (getString("TOOTH_REMARK3",   rs).equals("02")))) {
                    //(14)(15)(16)のいずれかに「02」(CO要相談)が格納されている場合
                    exam_item = "CO要相談";
                    mark = "〇";
                    npMarkFlg = false;
                }
                if (exam_item.equals("")) {
                } else {
                    _svf.VrsOut("EXAM_ITEM", "（" + exam_item + "）");
                }
                _svf.VrsOut("EXAM_MARK", mark);

                //受診のおすすめ（相談が必要）
                String consul_present = "";
                mark = "";
                //(9)(10)の両方に「03」（専門医（歯科医師）による診断が必要）が格納されている場合
                if (   (getString("JAWS_JOINTCD2",  rs) != null && (getString("JAWS_JOINTCD2",  rs).equals("03")))
                    && (getString("JAWS_JOINTCD",   rs) != null && (getString("JAWS_JOINTCD",   rs).equals("03")))) {
                    consul_present = "あご／かみ合わせ・歯並び";
                    mark = "〇";
                    npMarkFlg = false;
                } else if (   (getString("JAWS_JOINTCD2",  rs) != null
                          && (getString("JAWS_JOINTCD2",  rs).equals("03")))) {
                    //(9)に「03」（専門医（歯科医師）による診断が必要）が格納されている場合
                    consul_present = "あご";
                    mark = "〇";
                    npMarkFlg = false;
                } else if (   (getString("JAWS_JOINTCD",  rs) != null
                          && (getString("JAWS_JOINTCD",  rs).equals("03")))) {
                    //(10)に「03」（専門医（歯科医師）による診断が必要）が格納されている場合
                    consul_present = "かみ合わせ・歯並び";
                    mark = "〇";
                    npMarkFlg = false;
                } else {
                    //(9)(10)いずれにも「03」（専門医（歯科医師）による診断が必要）が格納されていない場合
                    consul_present = "あご／かみ合わせ・歯並び";
                }
                if (consul_present.equals("")) {
                } else {
                    consul_present = "（" + consul_present + "）" + CONSUL_PRESENT_TEXT;
                    _svf.VrsOut("CONSUL_PRESENT1", consul_present);
                }
                _svf.VrsOut("CONSUL_MARK", mark);

                //受診のおすすめ（歯石の沈着があります）
                mark = "";
                //(21)に「02」（あり）が格納されている場合
                if (   (getString("CALCULUS",  rs) != null
                    && (getString("CALCULUS",  rs).equals("02")))) {
                    mark = "〇";
                    npMarkFlg = false;
                }
                _svf.VrsOut("TARTAR_MARK", mark);
                mark = "";
                //受診のおすすめ（その他）
                //(11)に「02」（異常あり）が格納されている場合
                if (   (getString("OTHERDISEASECD",  rs) != null
                    && (getString("OTHERDISEASECD",  rs).equals("02")))) {
                    mark = "〇";
                    npMarkFlg = false;
                }
                _svf.VrsOut("ETC_MARK", mark);

                //異常なし
                if (npMarkFlg == true) {
                    _svf.VrsOut("NP_MARK", "〇");
                }

                String tooth_remark = "";
                if (getString("OTHERDISEASE", rs) != null) {
                    tooth_remark = "（" + getString("OTHERDISEASE", rs) + "）" + TOOTH_REMARK_TEXT;
                } else {
                    tooth_remark = "（　　　　　　　　　　　　　　　　　　　　）" + TOOTH_REMARK_TEXT;
                }

                if (KNJ_EditEdit.getMS932ByteLength(tooth_remark) <= 56) {
                    _svf.VrsOut("TOOTH_REMARK1", tooth_remark);
                } else {
                    _svf.VrsOut("TOOTH_REMARK1", tooth_remark);
                }
                _svf.VrsOut("ERA_NAME" , getGengo(_param._year).substring(0, 2));
                _svf.VrsOut("ERA_NAME2", getGengo(_param._year).substring(0, 2));
                _svf.VrEndRecord();
            }

            return printOk;
        }

        //出力データ取得SQL
        private String getDataSql(PageKind pageKind) {
             final StringBuffer stb = new StringBuffer();

             stb.append("     SELECT SRD.SCHREGNO");
             stb.append("          , MH.DATE");
             stb.append("          , MTD.JAWS_JOINTCD2");         //(9)
             stb.append("          , MTD.JAWS_JOINTCD");          //(10)
             stb.append("          , MTD.OTHERDISEASECD");        //(11)
             stb.append("          , MTD.OTHERDISEASE");
             stb.append("          , MTD.BRACK_BABYTOOTH");       //(13)
             stb.append("          , MTD.DENTISTREMARKCD");       //(14)
             stb.append("          , MT005.TOOTH_REMARK1");       //(15)
             stb.append("          , MT005.TOOTH_REMARK3");       //(16)
             stb.append("          , MTD.GUMCD");                 //(17)
             stb.append("          , MTD.PLAQUECD");              //(18)
             stb.append("          , MTD.REMAINBABYTOOTH");       //(19)
             stb.append("          , MTD.REMAINADULTTOOTH");      //(20)
             stb.append("          , MTD.CALCULUS");              //(21)
             stb.append("  FROM      MEDEXAM_TOOTH_DAT        MTD");
             stb.append("  LEFT JOIN SCHREG_REGD_DAT          SRD");
             stb.append("         ON MTD.SCHREGNO      = SRD.SCHREGNO");
             stb.append("        AND MTD.YEAR          = SRD.YEAR");
             stb.append("  LEFT JOIN SCHREG_REGD_GDAT         SRG");
             stb.append("         ON MTD.YEAR          = SRG.YEAR");
             stb.append("        AND SRD.GRADE         = SRG.GRADE");
             stb.append("  LEFT JOIN MEDEXAM_HDAT             MH");
             stb.append("         ON MTD.SCHREGNO      = MH.SCHREGNO");
             stb.append("        AND MTD.YEAR          = MH.YEAR");
             stb.append("  LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT MT005");
             stb.append("         ON MTD.SCHREGNO      = MT005.SCHREGNO");
             stb.append("        AND MTD.YEAR          = MT005.YEAR");
             stb.append("        AND MT005.TOOTH_SEQ   = '005'");
             stb.append("      WHERE SRD.YEAR          = '" + _param._year + "'");
             stb.append("        AND SRD.SEMESTER      = '" + _param._gakki + "'");
             if (_param._kubun.equals("1")) { //1:クラス
                 stb.append("        AND SRD.GRADE || SRD.HR_CLASS IN " + _param._selectInState + " ");
             } else if (_param._kubun.equals("2")) { //2:個人
                 stb.append("        AND SRD.SCHREGNO IN " + _param._selectInState + " ");
             }
             stb.append("   ORDER BY SRD.GRADE");
             stb.append("          , SRD.HR_CLASS");
             stb.append("          , SRD.ATTENDNO");

             return stb.toString();
        }
    }

    //========================================================================
    /** 健康診断結果通知書（耳鼻科） */
    private class TutiJibika extends TutiAbstract {
        //コンストラクタ
        public TutiJibika(final Param param, final DB2UDB db2, final Vrw32alp svf) {
            super(param, db2, svf, TITLE_CD_TUTI_JIBIKA);
        }

        //帳票出力
        public boolean printMain() {
            boolean printOk = false;

            switch (getPageKind())
            {
            case Medical:
                setForm(_param, _svf, "KNJF030J_9_1.xml", 4);
                break;
            case Parent:
                setForm(_param, _svf, "KNJF030J_9_2.xml", 4);
                break;
            }

            final String psKey = "getDataSql49";
            if (null == _param._psMap.get(psKey)) {
                final String sql = getDataSql(getPageKind());
                if (_param._isOutputDebug) {
                    log.info(" sql = " + sql);
                }

                try {
                    _param._psMap.put(psKey, _db2.prepareStatement(sql));

                } catch (SQLException e) {
                    log.error("error!", e);
                }
            }

            //見つかった生徒の数だけループ
            for (final Map rs : KnjDbUtils.query(_db2, _param._psMap.get(psKey), new Object[] { /*プリペアドステートメントの引数をセットする*/ })) {
                if (   getString("NOSEDISEASECD", rs) == null
                    || (getString("ABBV2", rs) != null && getString("ABBV2", rs).equals("1"))) {
                    continue;
                }

                {
                    String studentName = getStudentName(_param._year, _param._gakki, getString("SCHREGNO", rs));
                    final int studentNameLen = KNJ_EditEdit.getMS932ByteLength(studentName);
                    final String studentNameField = (studentNameLen <= 40)? "1":
                                                           (studentNameLen <= 50)? "2":
                                                               (studentNameLen <= 60)? "3": "4";
                    _svf.VrsOut("NAME1_" + studentNameField, studentName);
                    _svf.VrsOut("NAME2_" + studentNameField, studentName);
                }

                printOk = true;
                printHeadDate(_param._sendDate);
                _svf.VrsOut("SCHOOL_NAME"   ,  getSchoolName(_param._year));
                _svf.VrsOut("PRINCIPAL_NAME",  getSchoolName(_param._year) + "長");
                printTitle();

                _svf.VrsOut("SPINERIB1",        getString("SPINERIB1", rs));
                _svf.VrsOut("SPINERIB_REMARK1", getString("SPINERIB_REMARK1", rs));
                _svf.VrsOut("SPINERIB2",        getString("SPINERIB2", rs));
                _svf.VrsOut("SPINERIB_REMARK2", getString("SPINERIB_REMARK2", rs));
                _svf.VrsOut("SPINERIB3",        getString("SPINERIB3", rs));
                _svf.VrsOut("SPINERIB_REMARK3", getString("SPINERIB_REMARK3", rs));
                _svf.VrsOut("ERA_NAME",         getGengo(_param._year).substring(0, 2));
                _svf.VrEndRecord();
            }

            return printOk;
        }

        //出力データ取得SQL
        private String getDataSql(PageKind pageKind) {
             final StringBuffer stb = new StringBuffer();

             stb.append("     SELECT SRD.SCHREGNO");
             stb.append("          , MH.DATE");
             stb.append("          , MDD.NOSEDISEASECD");           //出力するかどうかの判断に使用する
             stb.append("          , NM1.ABBV2");                   //出力するかどうかの判断に使用する
             stb.append("          , NM1.NAME1 AS TB_REMARK");
             stb.append("          , NM2.NAME1 AS SPINERIB1");
             stb.append("          , CASE");
             stb.append("                 WHEN MD003.DET_REMARK1 IS NOT NULL THEN '所見：' || MD003.DET_REMARK1");
             stb.append("                 ELSE NULL");
             stb.append("            END       AS SPINERIB_REMARK1");
             stb.append("          , NM3.NAME1 AS SPINERIB2");
             stb.append("          , CASE");
             stb.append("                 WHEN MD003.DET_REMARK2 IS NOT NULL THEN '所見：' || MD003.DET_REMARK2");
             stb.append("                 ELSE NULL");
             stb.append("            END       AS SPINERIB_REMARK2");
             stb.append("          , NM4.NAME1 AS SPINERIB3");
             stb.append("          , CASE");
             stb.append("                 WHEN MD003.DET_REMARK3 IS NOT NULL THEN '所見：' || MD003.DET_REMARK3");
             stb.append("                 ELSE NULL");
             stb.append("            END       AS SPINERIB_REMARK3");
             stb.append("       FROM SCHREG_REGD_DAT        SRD");
             stb.append("  LEFT JOIN MEDEXAM_DET_DAT        MDD");
             stb.append("         ON SRD.SCHREGNO      = MDD.SCHREGNO");
             stb.append("        AND SRD.YEAR          = MDD.YEAR");
             stb.append("  LEFT JOIN MEDEXAM_DET_DETAIL_DAT MD003");
             stb.append("         ON SRD.SCHREGNO      = MD003.SCHREGNO");
             stb.append("        AND SRD.YEAR          = MD003.YEAR");
             stb.append("        AND MD003.DET_SEQ     = '003'");
             stb.append("  LEFT JOIN MEDEXAM_HDAT           MH");
             stb.append("         ON SRD.SCHREGNO      = MH.SCHREGNO");
             stb.append("        AND SRD.YEAR          = MH.YEAR");
             stb.append("  LEFT JOIN NAME_MST               NM1");
             stb.append("         ON MDD.NOSEDISEASECD = NM1.NAMECD2");
             stb.append("        AND NM1.NAMECD1       = 'F060'");
             stb.append("  LEFT JOIN NAME_MST               NM2");
             stb.append("         ON MD003.DET_REMARK7 = NM2.NAMECD2");
             stb.append("        AND NM2.NAMECD1       = 'F061'");
             stb.append("  LEFT JOIN NAME_MST               NM3");
             stb.append("         ON MD003.DET_REMARK8 = NM3.NAMECD2");
             stb.append("        AND NM3.NAMECD1       = 'F061'");
             stb.append("  LEFT JOIN NAME_MST               NM4");
             stb.append("         ON MD003.DET_REMARK9 = NM4.NAMECD2");
             stb.append("        AND NM4.NAMECD1       = 'F061'");
             stb.append("      WHERE SRD.YEAR          = '" + _param._year + "'");
             stb.append("        AND SRD.SEMESTER      = '" + _param._gakki + "'");
             if (_param._kubun.equals("1")) { //1:クラス
                 stb.append("        AND SRD.GRADE || SRD.HR_CLASS IN " + _param._selectInState + " ");
             } else if (_param._kubun.equals("2")) { //2:個人
                 stb.append("        AND SRD.SCHREGNO IN " + _param._selectInState + " ");
             }
             stb.append("   ORDER BY SRD.GRADE");
             stb.append("          , SRD.HR_CLASS");
             stb.append("          , SRD.ATTENDNO");

             return stb.toString();
        }
    }

    //========================================================================
    /** 健康診断結果通知書（色覚） */
    private class TutiSikikaku extends TutiAbstract {
        //コンストラクタ
        public TutiSikikaku(final Param param, final DB2UDB db2, final Vrw32alp svf) {
            super(param, db2, svf, TITLE_CD_TUTI_SIKIKAKU);
        }

        //帳票出力
        public boolean printMain() {
            boolean printOk = false;

            switch (getPageKind())
            {
            case Medical:
                setForm(_param, _svf, "KNJF030J_10_1.xml", 4);
                break;
            case Parent:
                setForm(_param, _svf, "KNJF030J_10_2.xml", 4);
                break;
            }

            final String psKey = "getDataSql50";
            if (null == _param._psMap.get(psKey)) {
                final String sql = getDataSql(getPageKind());
                if (_param._isOutputDebug) {
                    log.info(" sql = " + sql);
                }

                try {
                    _param._psMap.put(psKey, _db2.prepareStatement(sql));
                } catch (SQLException e) {
                    log.error("error!", e);
                }
            }
            //見つかった生徒の数だけループ
            for (final Map rs : KnjDbUtils.query(_db2, _param._psMap.get(psKey), new Object[] { /*プリペアドステートメントの引数をセットする*/ })) {
                if (getString("EYEDISEASE5",rs ) == null || "1".equals(getString("EYEDISEASE5_ABBV2",rs )) == true) {
                    continue;
                }
                //マスク
                if (_param._whiteSpaceImagePath != null) {
                    if ("01".equals(getString("EYEDISEASE5",rs)) == true) {
                        _svf.VrsOut("MASK", _param._whiteSpaceImagePath);
                    }
                }
                printOk = true;

                {
                    String studentName = getStudentName(_param._year, _param._gakki, getString("SCHREGNO", rs));
                    final int studentNameLen = KNJ_EditEdit.getMS932ByteLength(studentName);
                    final String studentNameField = (studentNameLen <= 40)? "1":
                                                           (studentNameLen <= 50)? "2":
                                                               (studentNameLen <= 60)? "3": "4";
                    _svf.VrsOut("NAME1_" + studentNameField, studentName);
                    _svf.VrsOut("NAME2_" + studentNameField, studentName);
                }

                printHeadDate(_param._sendDate);
                _svf.VrsOut("SCHOOL_NAME"   ,  getSchoolName(_param._year));
                _svf.VrsOut("PRINCIPAL_NAME",  getSchoolName(_param._year) + "長");
                printTitle();

                if (getString("EYEDISEASE5",rs ) != null) {
                    if ("01".equals(getString("EYEDISEASE5",rs )) == true ) {
                        _svf.VrsOut("DET_REMARK1"   , "色覚検査表の誤読や読みにくさは認められませんでした。");
                    } else if ("02".equals(getString("EYEDISEASE5",rs )) == true ) {
                        _svf.VrsOut("DET_REMARK1"     , "色覚調査表の誤読や読みにくさが、認められました。");
                        _svf.VrsOut("DET_REMARK2", "眼科への受診をおすすめします。");
                    } else if ("99".equals(getString("EYEDISEASE5",rs )) == true ) {
                        _svf.VrsOut("DET_REMARK1"   , getString("EYE_TEST_RESULT5",rs ));
                    }
                }
                _svf.VrsOut("ERA_NAME", getGengo(_param._year).substring(0, 2));
                _svf.VrEndRecord();
            }

            return printOk;
        }

        //出力データ取得SQL
        private String getDataSql(PageKind pageKind) {
             final StringBuffer stb = new StringBuffer();

             switch (getPageKind())
             {
             case Medical:
                 //・・・
                 break;
             case Parent:
                 //・・・
                 break;
             }
             stb.append("    SELECT SRD.SCHREGNO" );
             stb.append("         , NM1.NAME1 AS EYEDISEASE5_NAME ");
             stb.append("         , NM1.ABBV2 AS EYEDISEASE5_ABBV2 ");
             stb.append("         , MD02.DET_REMARK7 AS EYEDISEASE5 ");
             stb.append("         , MD02.DET_REMARK8 AS EYE_TEST_RESULT5 ");
             stb.append("         , MH.DATE ");
             stb.append("      FROM SCHREG_REGD_DAT  SRD ");
             stb.append(" LEFT JOIN MEDEXAM_HDAT MH ");
             stb.append("        ON SRD.YEAR = MH.YEAR ");
             stb.append("       AND SRD.SCHREGNO = MH.SCHREGNO ");
             stb.append(" LEFT JOIN MEDEXAM_DET_DAT  MD ");
             stb.append("        ON SRD.SCHREGNO = MD.SCHREGNO ");
             stb.append("       AND SRD.YEAR = MD.YEAR ");
             stb.append(" LEFT JOIN MEDEXAM_DET_DETAIL_DAT  MD02 ");
             stb.append("        ON SRD.YEAR     = MD02.YEAR ");
             stb.append("       AND SRD.SCHREGNO = MD02.SCHREGNO ");
             stb.append("       AND MD02.DET_SEQ = '002' ");
             stb.append(" LEFT JOIN NAME_MST  NM1 ");
             stb.append("        ON MD02.DET_REMARK7 = NM1.NAMECD2 ");
             stb.append("       AND NM1.NAMECD1      = 'F051' ");
             stb.append("     WHERE SRD.YEAR     = '" + _param._year + "'");
             stb.append("       AND SRD.SEMESTER = '" + _param._gakki + "'");
             if (_param._kubun.equals("1")) { //1:クラス
                 stb.append("       AND SRD.GRADE || SRD.HR_CLASS IN " + _param._selectInState + " ");
             }else if (_param._kubun.equals("2")) { //2:個人
                 stb.append("       AND SRD.SCHREGNO IN " + _param._selectInState + " ");
             }
             stb.append("  ORDER BY SRD.GRADE ");
             stb.append("         , SRD.HR_CLASS ");
             stb.append("         , SRD.ATTENDNO ");

             return stb.toString();
        }
    }

    //========================================================================
    /** 健康診断結果通知書（心電図） */
    private class TutiSindenzu extends TutiAbstract {
        //コンストラクタ
        public TutiSindenzu(final Param param, final DB2UDB db2, final Vrw32alp svf) {
            super(param, db2, svf, TITLE_CD_TUTI_SINDENZU);
        }

        //帳票出力
        public boolean printMain() {
            boolean printOk = false;

            switch (getPageKind())
            {
            case Medical:
                setForm(_param, _svf, "KNJF030J_11_1.xml", 4);
                break;
            case Parent:
                setForm(_param, _svf, "KNJF030J_11_2.xml", 4);
                break;
            }

            final String psKey = "getDataSql51";
            if (null == _param._psMap.get(psKey)) {
                final String sql = getDataSql(getPageKind());
                if (_param._isOutputDebug) {
                    log.info(" sql = " + sql);
                }

                try {
                    _param._psMap.put(psKey, _db2.prepareStatement(sql));
                } catch (SQLException e) {
                    log.error("error!", e);
                }
            }

            //見つかった生徒の数だけループ
            for (final Map rs : KnjDbUtils.query(_db2, _param._psMap.get(psKey), new Object[] { /*プリペアドステートメントの引数をセットする*/ })) {
                if (getString("HEART_MEDEXAM",rs ) == null || "1".equals(getString("HEART_MEDEXAM_ABBV2",rs )) == true) {
                    continue;
                }
                printOk = true;

                {
                    String studentName = getStudentName(_param._year, _param._gakki, getString("SCHREGNO", rs));
                    final int studentNameLen = KNJ_EditEdit.getMS932ByteLength(studentName);
                    final String studentNameField = (studentNameLen <= 40)? "1":
                                                           (studentNameLen <= 50)? "2":
                                                               (studentNameLen <= 60)? "3": "4";
                    _svf.VrsOut("NAME1_" + studentNameField, studentName);
                    _svf.VrsOut("NAME2_" + studentNameField, studentName);
                }

                printHeadDate(_param._sendDate);
                _svf.VrsOut("SCHOOL_NAME"   ,  getSchoolName(_param._year));
                _svf.VrsOut("PRINCIPAL_NAME",  getSchoolName(_param._year) + "長");
                printTitle();

                if (getString("HEART_MEDEXAM",rs ) != null) {
                    if ("99".equals(getString("HEART_MEDEXAM",rs )) == true && getString("HEART_MEDEXAM_REMARK",rs ) != null) {
                        _svf.VrsOut("HEART_MEDEXAM_REMARK" , "所見：" + getString("HEART_MEDEXAM_REMARK",rs ));
                    } else {
                        _svf.VrsOut("HEART_MEDEXAM"        , getString("HEART_MEDEXAM_NAME",rs ));
                        if (getString("HEART_MEDEXAM_REMARK",rs ) != null) {
                            _svf.VrsOut("HEART_MEDEXAM_REMARK"   , "所見：" + getString("HEART_MEDEXAM_REMARK",rs ));
                        }
                    }
                }
                _svf.VrsOut("ERA_NAME", getGengo(_param._year).substring(0, 2));
                _svf.VrEndRecord();
            }

            return printOk;
        }

        //出力データ取得SQL
        private String getDataSql(PageKind pageKind) {
             final StringBuffer stb = new StringBuffer();

             switch (getPageKind())
             {
             case Medical:
                 //・・・
                 break;
             case Parent:
                 //・・・
                 break;
             }

             stb.append("    SELECT SRD.SCHREGNO ");
             stb.append("         , NM1.NAME1 AS HEART_MEDEXAM_NAME ");
             stb.append("         , NM1.ABBV2 AS HEART_MEDEXAM_ABBV2 ");
             stb.append("         , MD.HEART_MEDEXAM ");
             stb.append("         , MD.HEART_MEDEXAM_REMARK ");
             stb.append("         , MH.DATE ");
             stb.append("      FROM SCHREG_REGD_DAT  SRD ");
             stb.append(" LEFT JOIN MEDEXAM_HDAT MH ");
             stb.append("        ON SRD.YEAR = MH.YEAR ");
             stb.append("       AND SRD.SCHREGNO = MH.SCHREGNO ");
             stb.append(" LEFT JOIN MEDEXAM_DET_DAT  MD ");
             stb.append("        ON SRD.SCHREGNO = MD.SCHREGNO ");
             stb.append("       AND SRD.YEAR = MD.YEAR ");
             stb.append(" LEFT JOIN NAME_MST  NM1 ");
             stb.append("        ON MD.HEART_MEDEXAM = NM1.NAMECD2 ");
             stb.append("       AND NM1.NAMECD1 = 'F080' ");
             stb.append("     WHERE SRD.YEAR     = '" + _param._year + "'");
             stb.append("       AND SRD.SEMESTER = '" + _param._gakki + "'");
             if (_param._kubun.equals("1")) { //1:クラス
                 stb.append("       AND SRD.GRADE || SRD.HR_CLASS IN " + _param._selectInState + " ");
             } else if (_param._kubun.equals("2")) { //2:個人
                 stb.append("       AND SRD.SCHREGNO IN " + _param._selectInState + " ");
             }
             stb.append("  ORDER BY SRD.GRADE ");
             stb.append("         , SRD.HR_CLASS ");
             stb.append("         , SRD.ATTENDNO ");

             return stb.toString();
        }
    }

    //========================================================================
    /** 健康診断結果通知書（聴力） */
    private class TutiTyoryoku extends TutiAbstract {
        //コンストラクタ
        public TutiTyoryoku(final Param param, final DB2UDB db2, final Vrw32alp svf) {
            super(param, db2, svf, TITLE_CD_TUTI_TYORYOKU);
        }

        //帳票出力
        public boolean printMain() {
            boolean printOk = false;

            switch (getPageKind())
            {
            case Medical:
                setForm(_param, _svf, "KNJF030J_12_1.xml", 4);
                break;
            case Parent:
                setForm(_param, _svf, "KNJF030J_12_2.xml", 4);
                break;
            }

            final String psKey = "getDataSql52";
            if (null == _param._psMap.get(psKey)) {
                final String sql = getDataSql(getPageKind());
                if (_param._isOutputDebug) {
                    log.info(" sql = " + sql);
                }

                try {
                    _param._psMap.put(psKey, _db2.prepareStatement(sql));

                } catch (SQLException e) {
                    log.error("error!", e);
                }
            }

            //見つかった生徒の数だけループ
            for (final Map rs : KnjDbUtils.query(_db2, _param._psMap.get(psKey), new Object[] { /*プリペアドステートメントの引数をセットする*/ })) {
                if (getString("R_EAR", rs) == null && getString("L_EAR", rs) == null) {
                    continue;
                } else if (isCheckOutput(rs) == false) {
                    continue;
                }

                {
                    String studentName = getStudentName(_param._year, _param._gakki, getString("SCHREGNO", rs));
                    final int studentNameLen = KNJ_EditEdit.getMS932ByteLength(studentName);
                    final String studentNameField = (studentNameLen <= 40)? "1":
                                                           (studentNameLen <= 50)? "2":
                                                               (studentNameLen <= 60)? "3": "4";
                    _svf.VrsOut("NAME1_" + studentNameField, studentName);
                    _svf.VrsOut("NAME2_" + studentNameField, studentName);
                }

                printOk = true;
                printHeadDate(_param._sendDate);
                _svf.VrsOut("SCHOOL_NAME"   ,  getSchoolName(_param._year));
                _svf.VrsOut("PRINCIPAL_NAME",  getSchoolName(_param._year) + "長");
                printTitle();

                _svf.VrsOut("R_EAR",            getString("R_EAR_NAME", rs));
                _svf.VrsOut("L_EAR",            getString("L_EAR_NAME", rs));
                _svf.VrsOut("ERA_NAME",         getGengo(_param._year).substring(0, 2));
                _svf.VrEndRecord();
            }

            return printOk;
        }

        //出力データ取得SQL
        private String getDataSql(PageKind pageKind) {
             final StringBuffer stb = new StringBuffer();

             stb.append("    SELECT SRD.SCHREGNO");
             stb.append("         , MH.DATE");
             stb.append("         , MDD.R_EAR");                 //出力するかどうかの判断に使用する
             stb.append("         , MDD.L_EAR");                 //出力するかどうかの判断に使用する
             stb.append("         , NM1.ABBV2 AS R_EAR_ABBV2");  //出力するかどうかの判断に使用する
             stb.append("         , NM2.ABBV2 AS L_EAR_ABBV2");  //出力するかどうかの判断に使用する
             stb.append("         , CASE");
             stb.append("                WHEN NM1.ABBV3 = '1' THEN NULL");
             stb.append("                ELSE NM1.NAME1");
             stb.append("           END       AS R_EAR_NAME");
             stb.append("         , CASE");
             stb.append("                WHEN NM2.ABBV3 = '1' THEN NULL");
             stb.append("                ELSE NM2.NAME1");
             stb.append("           END       AS L_EAR_NAME");
             stb.append("      FROM SCHREG_REGD_DAT   SRD");
             stb.append(" LEFT JOIN MEDEXAM_HDAT      MH");
             stb.append("        ON SRD.SCHREGNO          = MH.SCHREGNO");
             stb.append("       AND SRD.YEAR              = MH.YEAR");
             stb.append(" LEFT JOIN MEDEXAM_DET_DAT   MDD");
             stb.append("        ON SRD.SCHREGNO          = MDD.SCHREGNO");
             stb.append("       AND SRD.YEAR              = MDD.YEAR");
             stb.append(" LEFT JOIN NAME_MST          NM1");
             stb.append("        ON MDD.R_EAR             = NM1.NAMECD2");
             stb.append("       AND NM1.NAMECD1           = 'F010'");
             stb.append(" LEFT JOIN NAME_MST          NM2");
             stb.append("        ON MDD.L_EAR             = NM2.NAMECD2");
             stb.append("       AND NM2.NAMECD1           = 'F010'");
             stb.append("     WHERE SRD.YEAR          = '" + _param._year + "'");
             stb.append("       AND SRD.SEMESTER      = '" + _param._gakki + "'");
             if (_param._kubun.equals("1")) { //1:クラス
                 stb.append("        AND SRD.GRADE || SRD.HR_CLASS IN " + _param._selectInState + " ");
             } else if (_param._kubun.equals("2")) { //2:個人
                 stb.append("        AND SRD.SCHREGNO IN " + _param._selectInState + " ");
             }
             stb.append("  ORDER BY SRD.GRADE");
             stb.append("         , SRD.HR_CLASS");
             stb.append("         , SRD.ATTENDNO");

             return stb.toString();
        }

        //出力するかのチェック
        private boolean isCheckOutput(Map rs) {
            //記号が入力されている項目のABBV2が全て1の場合は帳票を出力しない
            String[] columns = {  "R_EAR"   //右聴力
                                 , "L_EAR"   //左聴力
                                 };
            for (String col : columns) {
                if (getString(col, rs) != null) {
                    if (   getString(col + "_ABBV2", rs) != null
                        && getString(col + "_ABBV2", rs).equals("1")) {
                        continue;
                    } else {
                        return true;
                    }
                }
            }
            return false;    //帳票を出力しない
        }
    }

    //========================================================================
    /** 健康診断結果通知書（内科） */
    private class TutiNaika extends TutiAbstract {
        //コンストラクタ
        public TutiNaika(final Param param, final DB2UDB db2, final Vrw32alp svf) {
            super(param, db2, svf, TITLE_CD_TUTI_NAIKA);
        }

        //帳票出力
        public boolean printMain() {
            boolean printOk = false;

            switch (getPageKind())
            {
            case Medical:
                setForm(_param, _svf, "KNJF030J_13_1.xml", 4);
                break;
            case Parent:
                setForm(_param, _svf, "KNJF030J_13_2.xml", 4);
                break;
            }

            final String psKey = "getDataSql53";
            if (null == _param._psMap.get(psKey)) {
                final String sql = getDataSql(getPageKind());
                if (_param._isOutputDebug) {
                    log.info(" sql = " + sql);
                }

                try {
                    _param._psMap.put(psKey, _db2.prepareStatement(sql));
                } catch (SQLException e) {
                    log.error("error!", e);
                }
            }

            for (final Map rs : KnjDbUtils.query(_db2, _param._psMap.get(psKey), new Object[] { /*プリペアドステートメントの引数をセットする*/ })) {
                if (("1".equals(getString("NUTRITIONCD_ABBV2", rs))   == true || getString("NUTRITIONCD", rs) == null)
                     && ("1".equals(getString("SKINDISEASECD_ABBV2", rs)) == true || getString("SKINDISEASECD", rs) == null)
                     && ("1".equals(getString("HEARTDISEASECD_ABBV2", rs)) == true || getString("HEARTDISEASECD", rs) == null)
                     && getString("OTHER_REMARK", rs) == null) {
                    continue;
                }
                printOk = true;

                {
                    String studentName = getStudentName(_param._year, _param._gakki, getString("SCHREGNO", rs));
                    final int studentNameLen = KNJ_EditEdit.getMS932ByteLength(studentName);
                    final String studentNameField = (studentNameLen <= 40)? "1":
                                                           (studentNameLen <= 50)? "2":
                                                               (studentNameLen <= 60)? "3": "4";
                    _svf.VrsOut("NAME1_" + studentNameField, studentName);
                    _svf.VrsOut("NAME2_" + studentNameField, studentName);
                }

                printHeadDate(_param._sendDate);
                _svf.VrsOut("SCHOOL_NAME"   ,  getSchoolName(_param._year));
                _svf.VrsOut("PRINCIPAL_NAME",  getSchoolName(_param._year) + "長");
                printTitle();

                int count = 1;
                //栄養状態
                String NUTRITION = "";
                //栄養状態が未受験・異常なし以外のとき
                if ("00".equals(getString("NUTRITIONCD",rs )) == false && "01".equals(getString("NUTRITIONCD",rs )) == false ) {
                    if ( getString("NUTRITIONCD",rs ) != null) {
                        NUTRITION =" 栄養状態：" + getString("NUTRITIONCD_NAME",rs );
                        if (getString("NUTRITIONCD_REMARK",rs ) != null) {
                            NUTRITION += " 所見：" + getString("NUTRITIONCD_REMARK",rs );
                        }
                        _svf.VrsOut("DET_REMARK" + count       , NUTRITION );
                        count++;
                    }
                }

                //皮膚疾患
                String SKINDISEASE = "";
                //皮膚疾患が未受験・異常なし以外のとき
                if ("00".equals(getString("SKINDISEASECD",rs )) == false && "01".equals(getString("SKINDISEASECD",rs )) == false ) {
                    if ("99".equals(getString("SKINDISEASECD",rs )) == false && getString("SKINDISEASECD",rs ) != null) {
                        SKINDISEASE =" 皮膚：" + getString("SKINDISEASECD_NAME",rs );
                        if (getString("SKINDISEASECD_REMARK",rs ) != null) {
                            SKINDISEASE += " 所見：" + getString("SKINDISEASECD_REMARK",rs );
                        }
                        _svf.VrsOut("DET_REMARK" + count       , SKINDISEASE );
                        count++;
                    } else {
                        if (getString("SKINDISEASECD_REMARK",rs ) != null) {
                            SKINDISEASE += " 皮膚：" + getString("SKINDISEASECD_REMARK",rs );
                            _svf.VrsOut("DET_REMARK" + count       , SKINDISEASE );
                            count++;
                        }
                    }
                }
                //心臓
                String HEARTDISEASE = "";
                //心臓の疾病及び異常が未受験・異常なし以外のとき
                if ("00".equals(getString("HEARTDISEASECD",rs )) == false && "01".equals(getString("HEARTDISEASECD",rs )) == false ) {
                    if ("99".equals(getString("HEARTDISEASECD",rs )) == false && getString("HEARTDISEASECD",rs ) != null) {
                        HEARTDISEASE =" 心臓：" + getString("HEARTDISEASECD_NAME",rs );
                        if (getString("HEARTDISEASECD_REMARK",rs ) != null) {
                            HEARTDISEASE += " 所見：" + getString("HEARTDISEASECD_REMARK",rs );
                        }
                        _svf.VrsOut("DET_REMARK" + count , HEARTDISEASE );
                        count++;
                    } else {
                        if (getString("HEARTDISEASECD_REMARK",rs ) != null) {
                            HEARTDISEASE += " 心臓：" + getString("HEARTDISEASECD_REMARK",rs );
                            _svf.VrsOut("DET_REMARK" + count , HEARTDISEASE );
                            count++;
                        }
                    }
                }

                //その他の疾病及び異常
                if (getString("OTHER_REMARK",rs ) != null) {
                    _svf.VrsOut("DET_REMARK" + count , " その他：" + getString("OTHER_REMARK",rs ) );
                }
                _svf.VrsOut("ERA_NAME", getGengo(_param._year).substring(0, 2));
                _svf.VrEndRecord();
            }

            return printOk;
        }

        //出力データ取得SQL
        private String getDataSql(PageKind pageKind) {
             final StringBuffer stb = new StringBuffer();

             switch (getPageKind())
             {
             case Medical:
                 //・・・
                 break;
             case Parent:
                 //・・・
                 break;
             }

             stb.append("    SELECT SRD.SCHREGNO ");
             stb.append("         , MD.NUTRITIONCD ");
             stb.append("         , MD09.DET_REMARK1 AS NUTRITIONCD_REMARK ");
             stb.append("         , NM1.NAME1 AS NUTRITIONCD_NAME ");
             stb.append("         , NM1.ABBV2 AS NUTRITIONCD_ABBV2 ");
             stb.append("         , MD.SKINDISEASECD ");
             stb.append("         , MD06.DET_REMARK1 AS SKINDISEASECD_REMARK ");
             stb.append("         , NM2.NAME1 AS SKINDISEASECD_NAME ");
             stb.append("         , NM2.ABBV2 AS SKINDISEASECD_ABBV2 ");
             stb.append("         , MD.HEARTDISEASECD ");
             stb.append("         , MD.HEARTDISEASECD_REMARK ");
             stb.append("         , NM3.NAME1 AS HEARTDISEASECD_NAME ");
             stb.append("         , NM3.ABBV2 AS HEARTDISEASECD_ABBV2 ");
             stb.append("         , MD.OTHER_REMARK ");
             stb.append("         , MH.DATE ");
             stb.append("      FROM SCHREG_REGD_DAT  SRD ");
             stb.append(" LEFT JOIN MEDEXAM_HDAT MH ");
             stb.append("        ON  SRD.YEAR = MH.YEAR ");
             stb.append("       AND SRD.SCHREGNO = MH.SCHREGNO ");
             stb.append(" LEFT JOIN MEDEXAM_DET_DAT  MD ");
             stb.append("        ON SRD.SCHREGNO = MD.SCHREGNO ");
             stb.append("       AND SRD.YEAR = MD.YEAR ");
             stb.append(" LEFT JOIN MEDEXAM_DET_DETAIL_DAT MD09 ");
             stb.append("        ON SRD.YEAR      = MD09.YEAR ");
             stb.append("       AND SRD.SCHREGNO  = MD09.SCHREGNO ");
             stb.append("       AND MD09.DET_SEQ  = '009' ");
             stb.append(" LEFT JOIN NAME_MST NM1 ");
             stb.append("        ON MD.NUTRITIONCD = NM1.NAMECD2 ");
             stb.append("       AND NM1.NAMECD1    = 'F030' ");
             stb.append(" LEFT JOIN MEDEXAM_DET_DETAIL_DAT MD06 ");
             stb.append("        ON SRD.YEAR      = MD06.YEAR ");
             stb.append("       AND SRD.SCHREGNO  = MD06.SCHREGNO ");
             stb.append("       AND MD06.DET_SEQ  = '006' ");
             stb.append(" LEFT JOIN NAME_MST NM2 ");
             stb.append("        ON MD.SKINDISEASECD = NM2.NAMECD2 ");
             stb.append("       AND NM2.NAMECD1      = 'F070' ");
             stb.append(" LEFT JOIN NAME_MST NM3 ");
             stb.append("        ON MD.HEARTDISEASECD = NM3.NAMECD2 ");
             stb.append("       AND NM3.NAMECD1       = 'F090' ");
             stb.append("     WHERE SRD.YEAR     = '" + _param._year + "'");
             stb.append("       AND SRD.SEMESTER = '" + _param._gakki + "'");
             if (_param._kubun.equals("1")) { //1:クラス
                 stb.append("       AND SRD.GRADE || SRD.HR_CLASS IN " + _param._selectInState + " ");
             }else if (_param._kubun.equals("2")) { //2:個人
                 stb.append("       AND SRD.SCHREGNO IN " + _param._selectInState + " ");
             }
             stb.append("  ORDER BY SRD.GRADE ");
             stb.append("         , SRD.HR_CLASS ");
             stb.append("         , SRD.ATTENDNO ");

             return stb.toString();
        }
    }

    //========================================================================
    /** 健康診断結果通知書（尿） */
    private class TutiNyou extends TutiAbstract {
        //コンストラクタ
        public TutiNyou(final Param param, final DB2UDB db2, final Vrw32alp svf) {
            super(param, db2, svf, TITLE_CD_TUTI_NYOU);
        }

        //帳票出力
        public boolean printMain() {
            boolean printOk = false;

            switch (getPageKind())
            {
            case Medical:
                setForm(_param, _svf, "KNJF030J_14_1.xml", 4);
                break;
            case Parent:
                setForm(_param, _svf, "KNJF030J_14_2.xml", 4);
                break;
            }

            final String psKey = "getDataSql54";
            if (null == _param._psMap.get(psKey)) {
                final String sql = getDataSql(getPageKind());
                if (_param._isOutputDebug) {
                    log.info(" sql = " + sql);
                }

                try {
                    _param._psMap.put(psKey, _db2.prepareStatement(sql));
                } catch (SQLException e) {
                    log.error("error!", e);
                }
            }

            //見つかった生徒の数だけループ
            for (final Map rs : KnjDbUtils.query(_db2, _param._psMap.get(psKey), new Object[] { /*プリペアドステートメントの引数をセットする*/ })) {
                int kensa;        //1:一次検査   2:再検査
                if ((   getString("ALBUMINURIA2CD", rs) != null
                     || getString("URICSUGAR2CD",   rs) != null
                     || getString("URICBLEED2CD",   rs) != null)
                     && ("00".equals(getString("ALBUMINURIA2CD", rs)) == false)
                     && ("00".equals(getString("URICSUGAR2CD", rs)) == false)
                     && ("00".equals(getString("URICBLEED2CD", rs)) == false)) {
                     kensa = 2;
                } else {
                    kensa = 1;
                }
                if (   getString("ALBUMINURIA1CD", rs) == null
                    && getString("URICSUGAR1CD",   rs) == null
                    && getString("URICBLEED1CD",   rs) == null
                    && getString("ALBUMINURIA2CD", rs) == null
                    && getString("URICSUGAR2CD",   rs) == null
                    && getString("URICBLEED2CD",   rs) == null) {
                    continue;
                } else if (isCheckOutput(rs, kensa) == false) {
                    continue;
                }
                printOk = true;

                {
                    String studentName = getStudentName(_param._year, _param._gakki, getString("SCHREGNO", rs));
                    final int studentNameLen = KNJ_EditEdit.getMS932ByteLength(studentName);
                    final String studentNameField = (studentNameLen <= 40)? "1":
                                                           (studentNameLen <= 50)? "2":
                                                               (studentNameLen <= 60)? "3": "4";
                    _svf.VrsOut("NAME1_" + studentNameField, studentName);
                    _svf.VrsOut("NAME2_" + studentNameField, studentName);
                }

                printHeadDate(_param._sendDate);
                _svf.VrsOut("SCHOOL_NAME"   ,  getSchoolName(_param._year));
                _svf.VrsOut("PRINCIPAL_NAME",  getSchoolName(_param._year) + "長");
                printTitle();

                _svf.VrsOut("ALBUMINURIA1CD", getString("ALBUMINURIA" + kensa, rs));
                _svf.VrsOut("URICSUGAR1CD",   getString("URICSUGAR"   + kensa, rs));
                _svf.VrsOut("URICBLEED1CD",   getString("URICBLEED"   + kensa, rs));
                _svf.VrsOut("ERA_NAME",       getGengo(_param._year).substring(0, 2));
                _svf.VrEndRecord();
            }
            return printOk;
        }

        //出力データ取得SQL
        private String getDataSql(PageKind pageKind) {
             final StringBuffer stb = new StringBuffer();

             stb.append("     SELECT SRD.SCHREGNO");
             stb.append("          , MH.DATE");
             stb.append("          , MDD.ALBUMINURIA1CD");
             stb.append("          , MDD.URICSUGAR1CD");
             stb.append("          , MDD.URICBLEED1CD");
             stb.append("          , MDD.ALBUMINURIA2CD");
             stb.append("          , MDD.URICSUGAR2CD");
             stb.append("          , MDD.URICBLEED2CD");
             stb.append("          , NM1.ABBV2 AS ALBUMINURIA1_ABBV2");    //出力するかどうかの判断に使用する
             stb.append("          , NM2.ABBV2 AS URICSUGAR1_ABBV2");      //出力するかどうかの判断に使用する
             stb.append("          , NM3.ABBV2 AS URICBLEED1_ABBV2");      //出力するかどうかの判断に使用する
             stb.append("          , NM4.ABBV2 AS ALBUMINURIA2_ABBV2");    //出力するかどうかの判断に使用する
             stb.append("          , NM5.ABBV2 AS URICSUGAR2_ABBV2");      //出力するかどうかの判断に使用する
             stb.append("          , NM6.ABBV2 AS URICBLEED2_ABBV2");      //出力するかどうかの判断に使用する
             stb.append("          , NM1.NAME1 AS ALBUMINURIA1");
             stb.append("          , NM2.NAME1 AS URICSUGAR1");
             stb.append("          , NM3.NAME1 AS URICBLEED1");
             stb.append("          , NM4.NAME1 AS ALBUMINURIA2");
             stb.append("          , NM5.NAME1 AS URICSUGAR2");
             stb.append("          , NM6.NAME1 AS URICBLEED2");
             stb.append("       FROM SCHREG_REGD_DAT   SRD");
             stb.append("  LEFT JOIN MEDEXAM_HDAT      MH");
             stb.append("         ON SRD.SCHREGNO          = MH.SCHREGNO");
             stb.append("        AND SRD.YEAR              = MH.YEAR");
             stb.append("  LEFT JOIN MEDEXAM_DET_DAT   MDD");
             stb.append("         ON SRD.SCHREGNO          = MDD.SCHREGNO");
             stb.append("        AND SRD.YEAR              = MDD.YEAR");
             stb.append("  LEFT JOIN NAME_MST          NM1");
             stb.append("         ON MDD.ALBUMINURIA1CD    = NM1.NAMECD2");
             stb.append("        AND NM1.NAMECD1           = 'F020'");
             stb.append("  LEFT JOIN NAME_MST          NM2");
             stb.append("         ON MDD.URICSUGAR1CD      = NM2.NAMECD2");
             stb.append("        AND NM2.NAMECD1           = 'F019'");
             stb.append("  LEFT JOIN NAME_MST          NM3");
             stb.append("         ON MDD.URICBLEED1CD      = NM3.NAMECD2");
             stb.append("        AND NM3.NAMECD1           = 'F018'");
             stb.append("  LEFT JOIN NAME_MST          NM4");
             stb.append("         ON MDD.ALBUMINURIA2CD    = NM4.NAMECD2");
             stb.append("        AND NM4.NAMECD1           = 'F020'");
             stb.append("  LEFT JOIN NAME_MST          NM5");
             stb.append("         ON MDD.URICSUGAR2CD      = NM5.NAMECD2");
             stb.append("        AND NM5.NAMECD1           = 'F019'");
             stb.append("  LEFT JOIN NAME_MST          NM6");
             stb.append("         ON MDD.URICBLEED2CD      = NM6.NAMECD2");
             stb.append("        AND NM6.NAMECD1           = 'F018'");
             stb.append("      WHERE SRD.YEAR              = '" + _param._year + "'");
             stb.append("        AND SRD.SEMESTER          = '" + _param._gakki + "'");
             if (_param._kubun.equals("1")) { //1:クラス
                 stb.append("        AND SRD.GRADE || SRD.HR_CLASS IN " + _param._selectInState + " ");
             } else if (_param._kubun.equals("2")) { //2:個人
                 stb.append("        AND SRD.SCHREGNO IN " + _param._selectInState + " ");
             }
             stb.append("   ORDER BY SRD.GRADE");
             stb.append("          , SRD.HR_CLASS");
             stb.append("          , SRD.ATTENDNO");

             return stb.toString();
        }

        //出力するかのチェック
        private boolean isCheckOutput(Map rs, int kensa) {
            //記号が入力されている項目のABBV2が全て1の場合は帳票を出力しない
            String[] columns = {  "ALBUMINURIA"   //蛋白
                                 , "URICSUGAR"     //糖
                                 , "URICBLEED"     //潜血
                                 };
            for (String col : columns) {
                String columnName = col + kensa;
                if (getString(columnName + "CD", rs) != null) {
                    if (  (   (kensa == 1 && _param._pm_saikensa1 == true)
                           || (kensa == 2 && _param._pm_saikensa2 == true))
                        && "05".equals(getString(columnName + "CD", rs))) {
                        return true;
                    } else if ("05".equals(getString(columnName + "CD", rs))) {
                        continue;
                    } else if (   getString(columnName + "_ABBV2", rs) != null
                        && getString(columnName + "_ABBV2", rs).equals("1")) {
                        continue;
                    } else {
                        return true;
                    }
                }
            }
            return false;    //帳票を出力しない
        }
    }

    //========================================================================
    //(帳票出力と引数に関連した汎用的な関数)

    //年度を文字列を取得
    private static String nendo(final DB2UDB db2, final Param param, final String year) {
        //if (NumberUtils.isDigits(year)) {//元の処理：全て数字なら印字可能なのだから判定が逆ではないのか？
        if (NumberUtils.isDigits(year) == false) {
            return "";
        }
        if (param._isSeireki) {
            return String.valueOf(Integer.parseInt(year)) + "年度";
        }
        return KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度";
    }

    //日付を返す
    private static String formatDate(final DB2UDB db2, final Param param, final String date) {
        if (null == date) {
            return "";
        }
        if (param._isSeireki) {
            return KNJ_EditDate.h_format_SeirekiJP(StringUtils.replace(date, "/", "-"));
        }
        return KNJ_EditDate.h_format_JP(db2, StringUtils.replace(date, "/", "-"));
    }

    private static void setForm(final Param param, final Vrw32alp svf, final String form, final int n) {
        svf.VrSetForm(form, n);
        if (param._isOutputDebug) {
            if (null == param._currentForm || !param._currentForm.equals(form)) {
                log.info(" setForm " + form);
            }
            param._currentForm = form;
        }
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static String getString(final String field, final Map map) {
        try {
            if (null == field || !map.containsKey(field)) {
                // フィールド名が間違い
                throw new RuntimeException("指定されたフィールドのデータがありません。フィールド名：'" + field + "' :" + map);
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        }
        if (null == field) {
            return null;
        }
        return (String) map.get(field);
    }

    private String getTextOrName(final String text, final String useFieldName) {
        if (null != text && text.length() > 0) {
            return text;
        }
        return useFieldName;
    }

    private static int getMS932ByteLength2(final String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private static int toInt(final String s, final int def) {
        if (!NumberUtils.isDigits(s)) {
            return def;
        }
        return Integer.parseInt(s);
    }

    private static double toDouble(final String s, final double def) {
        if (!NumberUtils.isNumber(s)) {
            return def;
        }
        return Double.parseDouble(s);
    }

    private static String SafeSubstring(final String s, int count) {
        if (null == s) {
            return "";
        }

        String ret = "";
        if (s.length() > count) {
            ret = s.substring(0, count);
        } else {
            //指定文字数より短ければ引数の文字列をそのまま返す
            ret = s;
        }
        return ret;
    }

    //------------------------------------------------------------------------

    //========================================================================
    //(Vrw32alpとParamのラップクラス)
    private static class Form {
        String _currentForm;
        final Map _fieldInfoMap = new HashMap();
        final Vrw32alp _svf;
        final Param _param;
        Form(final Vrw32alp svf, final Param param) {
            _svf = svf;
            _param = param;
        }
        public void setForm(final String form, final int n) {
            _currentForm = form;
            if (_param._isOutputDebug) {
                if (null == _currentForm || !_currentForm.equals(form)) {
                    log.info(" setForm " + form);
                }
            }
            _svf.VrSetForm(_currentForm, n);
            if (null != _currentForm && !_fieldInfoMap.containsKey(_currentForm)) {
                _fieldInfoMap.put(_currentForm, new HashMap(SvfField.getSvfFormFieldInfoMapGroupByName(_svf)));
            }
        }

        public static String concatIfNotBlank(final String a, final String concat, final String b) {
            return null == b ? a : ((null == a ? "" : a + concat) + b);
        }

        public SvfField getSvfField(final String field) {
            return (SvfField) getMappedMap(_fieldInfoMap, _currentForm).get(field);
        }

        public boolean hasField(final String field) {
            return null != getSvfField(field);
        }

        public int VrsOut(final String field, final String data) {
            if (null == field) {
                return -1;
            }
            if (!hasField(field)) {
                if (_param._isOutputDebug) {
                    log.warn("no field " + field + " : " + data);
                }
                return -1;
            }
            if (_param._isOutputDebugField) {
                log.info("VrsOut(\"" + field + "\", " + data + ")");
            }
            return _svf.VrsOut(field, data);
        }

        /**
         * データの桁数に合うフィールドを自動で選択して出力
         * @param fields フィールド候補（指定フィールドがフォームに無ければ無視）
         * @param data データ
         * @return
         */
        public int VrsOutSelectField(final String[] fields, final String data) {
            return VrsOut(getSelectField(fields, data), data);
        }

        public int VrsOutnSelectField(final String[] fields, final int gyo, final String data) {
            return VrsOutn(getSelectField(fields, data), gyo, data);
        }

        /**
         * フィールド候補からデータの桁数に合うフィールドを得る
         * @param fields フィールド候補
         * @param data データ
         * @return
         */
        public String getSelectField(final String[] fields, final String data) {
            String candField = null;
            String selectField = null;
            for (int i = 0; i < fields.length; i++) {
                if (!hasField(fields[i])) {
                    if (_param._isOutputDebug) {
                        log.warn("no field " + fields[i] + " : " + data);
                    }
                    continue;
                }
                candField = fields[i];

                SvfField svfField = getSvfField(fields[i]);
                final String direction = (String) svfField.getAttributeMap().get(SvfField.AttributeDirection);
                if ("1".equals(direction)) { // 縦
                    if (StringUtils.defaultString(data).length() <= svfField._fieldLength / 2) {
                        selectField = fields[i];
                        break;
                    }
                } else { // 横
                    if (KNJ_EditEdit.getMS932ByteLength(data) <= svfField._fieldLength) {
                        selectField = fields[i];
                        break;
                    }
                }
            }
            if (null == selectField) {
                selectField = candField;
            }
            return selectField;
        }

        protected int VrsOutSplit(final String[] fields, final String data) {
            final List<String> split = splitToFieldSize(fields, data);
            int rtn = 0;
            for (int i = 0; i < Math.min(fields.length, split.size()); i++) {
                rtn = VrsOut(fields[i], split.get(i));
            }
            return rtn;
        }

        protected int VrsOutGroupForData(final String[][] fieldGroups, final String data) {
            return VrsOutSplit(getFieldGroupForData(fieldGroups, data), data);
        }

        protected String[] getFieldGroupForData(final String[][] fieldGroups, final String data) {
            String[] fieldGroupFound = {};
            searchFieldGroup:
            for (int i = 0; i < fieldGroups.length; i++) {
                final String[] fieldGroup = fieldGroups[i];
                for (final String fieldname : fieldGroup) {
                    final SvfField svfField = getSvfField(fieldname);
                    if (null == svfField) {
                        continue searchFieldGroup;
                    }
                }
                fieldGroupFound = fieldGroup;
                if (dataFitsFieldGroup(data, fieldGroup)) {
                    return fieldGroup;
                }
            }
            return fieldGroupFound;
        }

        protected boolean dataFitsFieldGroup(final String data, final String[] fieldGroup) {
            List<String> splitToFieldSize = splitToFieldSize(fieldGroup, data);
            final boolean isFits = splitToFieldSize.size() <= fieldGroup.length;
            return isFits;
        }

        protected List<String> splitToFieldSize(final String[] fields, final String data) {
            final List<Integer> ketas = getFieldKetaList(fields);
            if (ketas.size() == 0) {
                return Collections.emptyList();
            }
            final List<StringBuffer> wrk = new ArrayList<StringBuffer>();
            StringBuffer currentLine = null;
            for (final char ch : data.toCharArray()) {
                if (null == currentLine) {
                    currentLine = new StringBuffer();
                    wrk.add(currentLine);
                }
                if (ch == '\n') {
                    currentLine = new StringBuffer();
                    wrk.add(currentLine);
                    continue;
                }

                if (wrk.size() <= ketas.size()) {
                    final String chs = String.valueOf(ch);
                    final int lineKeta = wrk.size() < ketas.size() ? ketas.get(wrk.size() - 1) : ketas.get(ketas.size() - 1); // 行あふれした場合最後のフィールドを使用しておく
                    if (lineKeta < KNJ_EditEdit.getMS932ByteLength(currentLine.toString() + chs)) {
                        currentLine = new StringBuffer();
                        wrk.add(currentLine);
                    }
                    currentLine.append(chs);
                } else {
                    break;
                }
            }
            final List<String> rtn = new ArrayList<String>();
            for (final StringBuffer stb : wrk) {
                rtn.add(stb.toString());
            }
            return rtn;
        }

        protected List<Integer> getFieldKetaList(final String[] fields) {
            final List<Integer> ketas = new ArrayList<Integer>();
            for (final String fieldname : fields) {
                final SvfField svfField = getSvfField(fieldname);
                if (null == svfField) {
                    continue;
                }
                ketas.add(svfField._fieldLength);
            }
            return ketas;
        }

        public int VrsOutn(final String field, final int gyo, final String data) {
            if (!hasField(field)) {
                if (_param._isOutputDebug) {
                    log.warn("no field " + field + " : " + data);
                }
                return -1;
            }
            if (_param._isOutputDebugField) {
                log.info("VrsOutn(\"" + field + "\", " + gyo + ", " + data + ")");
            }
            return _svf.VrsOutn(field, gyo, data);
        }

        protected int VrEndRecord() {
            if (_param._isOutputDebug) {
                log.info("VrEndRecord()");
            }
            return _svf.VrEndRecord();
        }
    }

    //========================================================================
    //(PHPから渡される引数を取得)
    private class Param {
        final String _year;
        final String _gakki;
        final String _kubun;
        final String _selectInState;
        final boolean _kensin_ryomen;

        final String _documentroot;
        final String _imagepath;
        final String _whiteSpaceImagePath;

        final boolean _printflgKensinIppan;
        final KensinFormKind _useForm_Ippan;
        final boolean _printflgKensinHa;
        final KensinFormKind _useForm_Ha;
        final String   _sendDate;//提出日
        final boolean _printflgItiran;
        final boolean _printflgUndouki;
        final boolean _printflgGanka;
        final boolean _printflgKekkaku;
        final boolean _printflgSiryoku;
        final boolean _printflgSika;
        final boolean _printflgJibika;
        final boolean _printflgSikikaku;
        final boolean _printflgSindenzu;
        final boolean _printflgTyoryoku;
        final boolean _printflgNaika;
        final boolean _printflgNyou;
        final boolean _pm_saikensa1;
        final boolean _pm_saikensa2;

        //健康診断結果通知書の「医療機関向け」「保護者向け」
        final PageKind _pageKindUndouki;
        final PageKind _pageKindGanka;
        final PageKind _pageKindKekkaku;
        final PageKind _pageKindSiryoku;
        final PageKind _pageKindSika;
        final PageKind _pageKindJibika;
        final PageKind _pageKindSikikaku;
        final PageKind _pageKindSindenzu;
        final PageKind _pageKindTyoryoku;
        final PageKind _pageKindNaika;
        final PageKind _pageKindNyou;

        final String _printKenkouSindanIppan;

        final boolean _isSeireki;
        final Map<String, PreparedStatement> _psMap = new HashMap();
        private String _currentForm;

        final boolean _isOutputDebug;
        final boolean _isOutputDebugField;

        final String _ctrlDate;
        final String _ctrlDateString;
        final String _knjf030jPrintVisionNumber;
        private Map _nameMstMap;
        /** 写真データ格納フォルダ */
        private final String _imageDir;
        /** 写真データの拡張子 */
        private final String _imageExt;
        /** 陰影保管場所(陰影出力に関係する) */
        private final String _documentRoot;
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final String _use_prg_schoolkind;
        final String[] _selectSchoolKind;
        private Map _yearF242name1Map;
        final Map<String, String> _minGradeMap; // 校種ごとの最小学年取得
        private String _time;

        private final Map _nameMstCache = new HashMap();

        Param(final DB2UDB db2, final HttpServletRequest request) {
            //パラメータの取得
            _year  = request.getParameter("YEAR");    //年度
            _gakki = request.getParameter("SEMESTER");//学期 1,2,3
            _kubun = request.getParameter("KUBUN");   //1:クラス,2:個人

            _documentroot = request.getParameter("DOCUMENTROOT");
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' "));
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");

            //１）健康診断票（一般）
            _printflgKensinIppan = convertPrintFlg(request.getParameter("CHECK1"));        //(出力対象チェック)
            _useForm_Ippan       = convertKensinFormKind(request.getParameter("RADIO1"));//4/6/9年用フォーム
            //２）健康診断票（歯・口腔）
            _printflgKensinHa    = convertPrintFlg(request.getParameter("CHECK2"));        //(出力対象チェック)
            _useForm_Ha          = convertKensinFormKind(request.getParameter("RADIO2"));//4/6/9年用フォーム
            //健康診断票　両面印刷
            _kensin_ryomen       = convertPrintFlg(request.getParameter("CHECK1_2"));
            //健康診断結果通知書　提出日
            _sendDate            = request.getParameter("SEND_DATE");
            //３）　健康診断結果通知書（一覧）
            _printflgItiran      = convertPrintFlg(request.getParameter("CHECK3"));  //(出力対象チェック)
            //４）　健康診断結果通知書（運動器）
            _printflgUndouki     = convertPrintFlg(request.getParameter("CHECK4"));  //(出力対象チェック)
            _pageKindUndouki     = convertPageKind(request.getParameter("RADIO4")); //医療機関向けor保護者向け
            //５）　健康診断結果通知書（眼科）
            _printflgGanka       = convertPrintFlg(request.getParameter("CHECK5"));  //(出力対象チェック)
            _pageKindGanka       = convertPageKind(request.getParameter("RADIO5")); //医療機関向けor保護者向け
            //６）　健康診断結果通知書（結核）
            _printflgKekkaku     = convertPrintFlg(request.getParameter("CHECK6"));  //(出力対象チェック)
            _pageKindKekkaku     = convertPageKind(request.getParameter("RADIO6")); //医療機関向けor保護者向け
            //７）　健康診断結果通知書（視力）
            _printflgSiryoku     = convertPrintFlg(request.getParameter("CHECK7"));  //(出力対象チェック)
            _pageKindSiryoku     = convertPageKind(request.getParameter("RADIO7")); //医療機関向けor保護者向け
            //８）　健康診断結果通知書（歯科）
            _printflgSika        = convertPrintFlg(request.getParameter("CHECK8"));  //(出力対象チェック)
            _pageKindSika        = convertPageKind(request.getParameter("RADIO8")); //医療機関向けor保護者向け
            //９）　健康診断結果通知書（耳鼻科）
            _printflgJibika      = convertPrintFlg(request.getParameter("CHECK9"));  //(出力対象チェック)
            _pageKindJibika      = convertPageKind(request.getParameter("RADIO9")); //医療機関向けor保護者向け
            //１０）健康診断結果通知書（色覚）
            _printflgSikikaku    = convertPrintFlg(request.getParameter("CHECK10")); //(出力対象チェック)
            _pageKindSikikaku    = convertPageKind(request.getParameter("RADIO10"));//医療機関向けor保護者向け
            //１１）健康診断結果通知書（心電図）
            _printflgSindenzu    = convertPrintFlg(request.getParameter("CHECK11")); //(出力対象チェック)
            _pageKindSindenzu    = convertPageKind(request.getParameter("RADIO11"));//医療機関向けor保護者向け
            //１２）健康診断結果通知書（聴力）
            _printflgTyoryoku    = convertPrintFlg(request.getParameter("CHECK12")); //(出力対象チェック)
            _pageKindTyoryoku    = convertPageKind(request.getParameter("RADIO12"));//医療機関向けor保護者向け
            //１３）健康診断結果通知書（内科）
            _printflgNaika       = convertPrintFlg(request.getParameter("CHECK13")); //(出力対象チェック)
            _pageKindNaika       = convertPageKind(request.getParameter("RADIO13"));//医療機関向けor保護者向け
            //１４）健康診断結果通知書（尿）
            _printflgNyou        = convertPrintFlg(request.getParameter("CHECK14")); //(出力対象チェック)
            _pageKindNyou        = convertPageKind(request.getParameter("RADIO14"));//医療機関向けor保護者向け
            _pm_saikensa1        = convertPrintFlg(request.getParameter("CHECK15")); //一次検査の±を再検査対象とする
            _pm_saikensa2        = convertPrintFlg(request.getParameter("CHECK16")); //再検査の±を再検査対象とする

            //クラス選択or個人選択に応じて
            String classCdString = "";
            {
                String classCd[] = request.getParameterValues("CLASS_SELECTED");

                //各要素をシングルクォーテーションで囲む
                if (_kubun.equals("1"))
                {
                    //クラス(学年・組)

                    for (int i=0 ; i<classCd.length ; i++) {
                        classCd[i] = "'" + classCd[i] + "'";
                    }
                } else if (_kubun.equals("2")) {
                    //学籍番号
                    for (int i=0 ; i<classCd.length ; i++) {
                        //ハイフンで区切ったものの左側のみを対象とする
                        classCd[i] = "'" + (classCd[i]).substring(0,(classCd[i]).indexOf("-")) + "'";
                    }
                }

                //カンマ区切りの文字列を作成、さらに外側をカッコで囲む
                for (int i=0 ; i<classCd.length ; i++) {
                    if (i > 0)  classCdString += ",";
                    classCdString += classCd[i];
                }
                classCdString = "(" + classCdString + ")";
            }
            _selectInState = classCdString;

            _printKenkouSindanIppan = request.getParameter("printKenkouSindanIppan");

            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ")));
            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugField = ArrayUtils.contains(outputDebug, "field");

            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = StringUtils.split(request.getParameter("selectSchoolKind"), ":");
            _knjf030jPrintVisionNumber = request.getParameter("knjf030jPrintVisionNumber");
            _nameMstMap = getNameMstMap(db2);
            _minGradeMap = getMinGradeMap(db2);
            _yearF242name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT YEAR, NAME1 FROM V_NAME_MST WHERE NAMECD1 = 'F242' AND NAMECD2 = '01' "), "YEAR", "NAME1");


            //  作成日(現在処理日)
            if (null != request.getParameter("CTRL_DATE")) {
                _ctrlDate = request.getParameter("CTRL_DATE");
            } else {
                KNJ_Get_Info getinfo = new KNJ_Get_Info();
                KNJ_Get_Info.ReturnVal returnval = null;

                returnval = getinfo.Control(db2);
                _ctrlDate = returnval.val3;

                getinfo = null;
                returnval = null;
            }
            _ctrlDateString = formatDate(db2, this, _ctrlDate);

            _documentRoot = request.getParameter("DOCUMENTROOT");//陰影保管場所 NO001
            _imageDir = "image/stamp";
            _imageExt = "bmp";

            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            DecimalFormat df = new DecimalFormat("00");
            _time = df.format(hour)+"時"+df.format(minute)+"分";
        }
        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJF030J' AND NAME = '" + propName + "' "));
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.info(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        public boolean isPrintKenkouSindanIppan() {
            return "1".equals(_printKenkouSindanIppan) ||
                    "2".equals(_printKenkouSindanIppan) ||
                    "3".equals(_printKenkouSindanIppan);
        }

        private boolean convertPrintFlg(String printFlg) {
            return "on".equals(printFlg);
        }

        private PageKind convertPageKind(String val) {
            //医療機関向け
            if ("1".equals(val)) {
                return PageKind.Medical;
            } else {
                //保護者向け
                return PageKind.Parent;
            }
        }

        private KensinFormKind convertKensinFormKind(String val) {
            if ("1".equals(val)) {
                return KensinFormKind.Form4;
            } else if ("2".equals(val)) {
                return KensinFormKind.Form6;
            } else if ("3".equals(val)) {
                return KensinFormKind.Form9;
            }

            return KensinFormKind.Form4;
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFile(final String filename) {
            final String imageDir = "image";
            if (null == _documentRoot) {
                return null;
            } // DOCUMENTROOT
            if (null == _imageExt) {
                return null;
            }
            if (null == imageDir) {
                return null;
            }
            if (null == filename) {
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(_documentRoot);
            stb.append("/");
            stb.append(imageDir);
            stb.append("/");
            stb.append(filename);
            File file1 = new File(stb.toString());
            if (!file1.exists()) {
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
        }

        private String getNameMstValue(final String namecd1, final String namecd2, final String field) {
            if (null == namecd1 || null == namecd2 || null == field) {
                return null;
            }
            final Map namecd1Map = getMappedMap(_nameMstMap, namecd1);
            if (null == namecd1Map.get(namecd2)) {
                log.info(" name_mst null: NAMECD1 = " + namecd1 + ", NAMECD2 = " + namecd2);
            }
            return getString(field, getMappedMap(namecd1Map, namecd2));
        }

        /**
         * 写真データファイルの取得
         */
        private Map getNameMstMap(final DB2UDB db2) {
            final Map rtn = new TreeMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT * ");
            stb.append(" FROM NAME_MST T1 ");
            stb.append(" WHERE NAMECD1 LIKE 'F%' ");

            final List rowList = KnjDbUtils.query(db2, stb.toString());
            for (final Iterator it = rowList.iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                getMappedMap(rtn, getString("NAMECD1", row)).put(getString("NAMECD2", row), row);
            }
            return rtn;
        }

        /**
         * 校種ごとの最小学年取得
         */
        private Map getMinGradeMap(final DB2UDB db2) {
            final Map rtn = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 AS SCHOOL_KIND, NAME2 AS MIN_GRADE FROM NAME_MST WHERE NAMECD1 = 'A023' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("SCHOOL_KIND"), rs.getString("MIN_GRADE"));
                }
            } catch (SQLException ex) {
                log.debug("getA023 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        // 名称マスタの名称予備1が1ならtrue、それ以外はfalse
        boolean nameMstNamespare1Is1(final DB2UDB db2, final String nameCd1, final String nameCd2) throws SQLException {
            return "1".equals(getNameMst(db2, nameCd1, nameCd2, "NAMESPARE1"));
        }

        // 名称マスタの名称予備2が1ならtrue、それ以外はfalse
        boolean nameMstNamespare2Is1(final DB2UDB db2, final String nameCd1, final String nameCd2) throws SQLException {
            return "1".equals(getNameMst(db2, nameCd1, nameCd2, "NAMESPARE2"));
        }

        private String nameMstKey(final String nameCd1, final String nameCd2, final String fieldname) {
            return "NAME_MST." + nameCd1 + "." + nameCd2 + "." + fieldname;
        }

        String getNameMst(final DB2UDB db2, final String nameCd1, final String nameCd2, final String fieldname) throws SQLException {
            if (null == nameCd2) {
                return null;
            }
            final String cacheKey = nameMstKey(nameCd1, nameCd2, fieldname);
            if (_nameMstCache.containsKey(cacheKey)) {
                return (String) _nameMstCache.get(cacheKey);
            }
            final String sql = "SELECT " + fieldname + " FROM NAME_MST WHERE NAMECD1 = '" + nameCd1 + "' AND NAMECD2 = '" + nameCd2 + "'";
            ResultSet rs = null;
            PreparedStatement ps = null;
            String rtn = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(fieldname);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _nameMstCache.put(cacheKey, rtn);
            return rtn;
        }
    }

    //========================================================================
    //(学校情報(校長名、学校名)を取得)
    private class SchoolInfo {
        public static final String SCHOOL_NAME1 = "SCHOOL_NAME1";
        public static final String SCHOOL_NAME2 = "SCHOOL_NAME2";
        public static final String PRINCIPAL_NAME = "PRINCIPAL_NAME";
        public static final String PRINCIPAL_JOBNAME = "PRINCIPAL_JOBNAME";

        private final KNJ_Schoolinfo.ReturnVal _returnval2;
        private String _certifSchoolDatSchoolName;
        private String _certifSchoolDatJobName;
        private String _certifSchoolDatPrincipalName;
        private String _certifSchoolDatRemark1;
        private String _certifSchoolDatRemark2;
        private String _certifSchoolDatRemark3;
        private String _certifSchoolDatRemark4;
        private String _certifSchoolDatRemark5;
        private String _certifSchoolDatRemark6;

        private String _schoolKind;

        public SchoolInfo(final DB2UDB db2, final String year, final String semester, final String schRegNo) {
            KNJ_Schoolinfo schoolinfo = new KNJ_Schoolinfo(year);
            _returnval2 = schoolinfo.get_info(db2);

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT * ");
                stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
                stb.append(" WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '125' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _certifSchoolDatSchoolName    = rs.getString("SCHOOL_NAME");
                    _certifSchoolDatJobName       = rs.getString("JOB_NAME");
                    _certifSchoolDatPrincipalName = rs.getString("PRINCIPAL_NAME");
                    _certifSchoolDatRemark1       = rs.getString("REMARK1");
                    _certifSchoolDatRemark2       = rs.getString("REMARK2");
                    _certifSchoolDatRemark3       = rs.getString("REMARK3");
                    _certifSchoolDatRemark4       = rs.getString("REMARK4");
                    _certifSchoolDatRemark5       = rs.getString("REMARK5");
                    _certifSchoolDatRemark6       = rs.getString("REMARK6");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        public String getName(final String schoolKind, final String field) {
            final Map map = new HashMap();
            if ("H".equals(schoolKind)) {
                map.put(SCHOOL_NAME1,      _certifSchoolDatSchoolName); //学校名１
                map.put(SCHOOL_NAME2,      _certifSchoolDatSchoolName); //学校名２
                map.put(PRINCIPAL_NAME,    _certifSchoolDatPrincipalName); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatJobName);
            } else if ("J".equals(schoolKind)) {
                map.put(SCHOOL_NAME1,      _certifSchoolDatRemark1); //学校名１
                map.put(SCHOOL_NAME2,      _certifSchoolDatRemark1); //学校名２
                map.put(PRINCIPAL_NAME,    _certifSchoolDatRemark2); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatRemark3);
            } else if ("P".equals(schoolKind) || "K".equals(schoolKind)) {
                map.put(SCHOOL_NAME1,      _certifSchoolDatRemark4); //学校名１
                map.put(SCHOOL_NAME2,      _certifSchoolDatRemark4); //学校名２
                map.put(PRINCIPAL_NAME,    _certifSchoolDatRemark5); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatRemark6);
            }
            if (null == map.get(SCHOOL_NAME1)     ) map.put(SCHOOL_NAME1,       _returnval2.SCHOOL_NAME1); //学校名１
            if (null == map.get(SCHOOL_NAME2)     ) map.put(SCHOOL_NAME2,       _returnval2.SCHOOL_NAME2); //学校名２
            if (null == map.get(PRINCIPAL_NAME)   ) map.put(PRINCIPAL_NAME,    _returnval2.PRINCIPAL_NAME); //校長名
            if (null == map.get(PRINCIPAL_JOBNAME)) map.put(PRINCIPAL_JOBNAME, _returnval2.PRINCIPAL_JOBNAME);

            return (String)map.get(field);
        }

        public String getName(final String field) {
            if ("".equals(_schoolKind) == true) return "";

            return this.getName(_schoolKind, field);
        }
    }
}
//クラスの括り

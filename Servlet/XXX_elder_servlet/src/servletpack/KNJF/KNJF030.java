// kanji=漢字
/*
 * 作成日: 2005/06/24
 * 作成者: nakamoto
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
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
import java.util.Enumeration;
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
 *                  ＜ＫＮＪＦ０３０＞  保健各種帳票印刷（クラス／個人）
 *
 *  2006/06/02 nakamoto 作成日---KNJF030,KNJF040を統合。
 *  2006/07/28 nakamoto NO001:コード２未満とNULLは空白表示とする。--定期健康診断結果のお知らせ(KNJF030_7)
 *                            コード２未満とNULLは出力しない。------内科健診所見(KNJF030_8)
 */

public class KNJF030 {

    private static final Log log = LogFactory.getLog(KNJF030.class);

    private static final int check_no1 = 1;
    private static final int check_no2 = 2;
    private static final int check_no3 = 3;
    private static final int check_no4 = 4;
    private static final int check_no5 = 5;
    private static final int check_no6 = 6;
    private static final int check_no7 = 7;
    private static final int check_no7Kuma = 1007;
    private static final int check_no8 = 8;
    private static final int check_no9 = 9;
    private static final int check_no10 = 10;
    private static final int check_no11 = 11;
    private static final int check_no12 = 12;
    private static final int check_no13 = 13;
    private static final String SCHOOL_NAME1 = "SCHOOL_NAME1";
    private static final String SCHOOL_NAME2 = "SCHOOL_NAME2";
    private static final String PRINCIPAL_NAME = "PRINCIPAL_NAME";
    private static final String PRINCIPAL_JOBNAME = "PRINCIPAL_JOBNAME";

    private String FORM_KNJF030A_1_5 = "KNJF030A_1_5.frm";
    private String FORM_KNJF030A_1P_5 = "KNJF030A_1P_5.frm";

    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
                     throws Exception
    {

        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

    //  print設定
        final PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

        log.fatal("$Revision: 77168 $"); // CVSキーワードの取り扱いに注意

        KNJServletUtils.debugParam(request, log);

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }


    //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ
        Param param = null;
        try {

            param = new Param(db2, request);
            Form form = new Form(svf, param);

            //SVF出力

            //-----結果印刷-----//
            // TODO:年度分のみを印字プログラムを使用するのであれば、trueとし
            // データ作成してKNJF030Cを参考にプログラム作成
            boolean mekeMedexamFlg = false;

            if (param._check1_2 != null) {
                final List<Map<String, String>> schnoMapList = KnjDbUtils.query(db2, statementSchno(param));

                final KNJF030C knjf030c = new KNJF030C();
                knjf030c.makeData(request, db2, mekeMedexamFlg);
                knjf030c._param.setOtherInjiParam("OUTPUTA", String.valueOf(Integer.parseInt(param._outputA)));

                for (final Map rs : schnoMapList) {
                    boolean printOmote = false;
                    //１）生徒学生健康診断票（一般）
                    if (isPrintKenkouSindanIppan(param) && !"1".equals(param._KenkouSindan_Ippan_Pattern)) {
                        final List<String> schregnoList = new ArrayList<String>();
                        schregnoList.add(getString("SCHREGNO", rs));
                        if (knjf030c.printKenkouSindanIppanAschregnoList(db2, svf, schregnoList)) {
                            printOmote = true;
                        }
                    } else {
                        if (printMain1Result(db2, svf, param, rs)) {
                            printOmote = true;
                        }
                    }
                    if (!printOmote) {
                        setForm(param, svf, "BLANK_A4_TATE.frm", 1); // データがなければ空白ページ
                        svf.VrsOut("BLANK", "BLANK");
                        svf.VrEndPage();
                    }
                    //２）生徒学生健康診断票（歯・口腔）
                    boolean printUra = printMain2Result(db2, form, param, rs);
                    if (!printUra) {
                        setForm(param, svf, "BLANK_A4_TATE.frm", 1); // データがなければ空白ページ
                        svf.VrsOut("BLANK", "BLANK");
                        svf.VrEndPage();
                    }
                    if (printOmote || printUra) {
                        nonedata = true;
                    }
                }
            } else {
                //１）生徒学生健康診断票（一般）
                if (param._check1 != null) {
                    if (isPrintKenkouSindanIppan(param) && !"1".equals(param._KenkouSindan_Ippan_Pattern)) {
                        final KNJF030C knjf030c = new KNJF030C();
                        knjf030c.makeData(request, db2, mekeMedexamFlg);
                        knjf030c._param.setOtherInjiParam("OUTPUTA", String.valueOf(Integer.parseInt(param._outputA)));
                        if (knjf030c.printKenkouSindanIppanA(db2, svf)) nonedata = true;
                    } else {
                        final List<Map<String, String>> schnoMapList = KnjDbUtils.query(db2, statementSchno(param));
                        for (final Map rs : schnoMapList) {
                            if (printMain1Result(db2, svf, param, rs)) {
                                nonedata = true;
                            }
                        }
                    }
                }

                //２）生徒学生健康診断票（歯・口腔）
                if (param._check2 != null) {
                    final List<Map<String, String>> schnoMapList = KnjDbUtils.query(db2, statementSchno(param));
                    for (final Map rs : schnoMapList) {
                        if (printMain2Result(db2, form, param, rs)) {
                            nonedata = true;
                        }
                    }
                }
            }

            //３）健康診断の未受検項目のある生徒へ
            if (param._check3 != null) {
                if (printMain3(db2, svf, param)) {
                    nonedata = true;
                }
            }

            //４）眼科検診のお知らせ
            if (param._check4 != null) {
                if (printMain4(db2, svf, param)) {
                    nonedata = true;
                }
            }

            //５）検診結果のお知らせ（歯・口腔）
            if (param._check5 != null) {
                if (param._isMeiji) {
                    if (printMain5Meiji(db2, svf, param)) {
                        nonedata = true;
                    }
                } else {
                    if (printMain5(db2, svf, param)) {
                        nonedata = true;
                    }
                }
            }

            //６）定期健康診断結果
            if (param._check7 != null) {
                Form form6 = new Form(svf, param);
                if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
                    if (printMain6_Pattern1(db2, form6, svf, param)) {
                        nonedata = true;
                    }
                } else {
                    if (printMain6(db2, form6, svf, param)) {
                        nonedata = true;
                    }
                }
            }

            //７）尿検査診断結果のお知らせ
            if (param._urinalysis_check != null) {
                if (printUrinalysis(db2, svf, param)) {
                    nonedata = true;
                }
            }

            //以下、「or」は、共通or熊本で指示画面表示番号を記載。
            //８or７）検診結果のお知らせ（一般）
            if (param._check6 != null) {
                if (param._isKumamoto) {
                    if (printMain7Kumamoto(db2, svf, param)) {
                        nonedata = true;
                    }
                } else {
                    if (printMain7(db2, svf, param)) {
                        nonedata = true;
                    }
                }
            }

            //９or８）内科検診所見あり生徒の名簿
            if (param._check8 != null) {
                if (printMain8(db2, svf, param)) {
                    nonedata = true;
                }
            }

            //１０or９）定期健康診断異常者一覧表
            if (param._check9 != null && !param._select1.equals("17") && !param._select1.equals("18")) {
                if (printMain9(db2,svf,param)) {
                    nonedata = true;
                }
            }

            // １１or１０）尿検査結果のお知らせ
            if (param._check10 != null) {
                if (printMain10(db2, svf, param)) {
                    nonedata = true;
                }
            }

            // １２or１１）視力の検査結果のお知らせ
            if (param._check11 != null) {
                if (printMain11(db2, svf, param)) {
                    nonedata = true;
                }
            }

            // １３or１２）聴力の検査結果のお知らせ
            if (param._check12 != null) {
                if (printMain12(db2, svf, param)) {
                    nonedata = true;
                }
            }

            // １４or１３）定期健康診断結果一覧
            if (param._check13 != null) {
                if (printMain13(db2, svf, param)) {
                    nonedata = true;
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            log.debug("nonedata=" + nonedata);

            //  該当データ無し
            if (!nonedata) {
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
            db2.close();                //DBを閉じる
            outstrm.close();            //ストリームを閉じる
        }

    }//doGetの括り

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

    private static boolean isPrintKenkouSindanIppan(final Param param) {
        return "1".equals(param._printKenkouSindanIppan) || "2".equals(param._printKenkouSindanIppan) || "3".equals(param._printKenkouSindanIppan);
    }

    private static int getMS932ByteLength(final String str) {
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

    private String getForm1(final Param param, final String schoolKind) {
        String form = null;
        if (("P".equals(schoolKind) || "J".equals(schoolKind)) && "1".equals(param._useForm9_PJ_Ippan) && ("1".equals(param._useParasite_P) || "1".equals(param._useParasite_J))) {
            form = FORM_KNJF030A_1P_5;
        } else if (("P".equals(schoolKind) || "J".equals(schoolKind)) && "1".equals(param._useForm9_PJ_Ippan)) {
            form = FORM_KNJF030A_1_5;
        } else if (("J".equals(schoolKind) || "H".equals(schoolKind)) && "1".equals(param._useForm7_JH_Ippan)) {
            form = "KNJF030A_1_6";
        } else if (("K".equals(schoolKind) || "P".equals(schoolKind)) && "1".equals(param._useParasite_P)) {
            form =  "KNJF030_1P_2.frm";
        } else if ("K".equals(schoolKind) || "P".equals(schoolKind)) {
            form =  "KNJF030_1_2.frm";
        } else if ("J".equals(schoolKind) && "1".equals(param._useParasite_J)) {
            form =  "KNJF030A_1PJ.frm";
        } else if ("J".equals(schoolKind)) {
            form =  "KNJF030A_1J.frm";
        } else if ("H".equals(schoolKind) && "1".equals(param._useParasite_H)) {
            if ("1".equals(param._useForm5_H_Ippan)) {
                form =  "KNJF030_1P_5G.frm";
            } else {
                form =  "KNJF030_1P.frm";
            }
        } else {
            if ("1".equals(param._useForm5_H_Ippan)) {
                form =  "KNJF030_1_5G.frm";
            } else {
                form =  "KNJF030_1.frm";
            }
        }
        if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
            form = "KNJF030_1_3.frm";
        }
        return form;
    }

    private static String nendo(final DB2UDB db2, final Param param, final String year) {
        if (NumberUtils.isDigits(year)) {
            return "";
        }
        if (param._isSeireki) {
            return String.valueOf(Integer.parseInt(year)) + "年度";
        }
        return KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度";
    }

    private static String formatDate(final DB2UDB db2, final Param param, final String date) {
        if (null == date) {
            return "";
        }
        if (param._isSeireki) {
            return KNJ_EditDate.h_format_SeirekiJP(StringUtils.replace(date, "/", "-"));
        }
        return KNJ_EditDate.h_format_JP(db2, StringUtils.replace(date, "/", "-"));
    }

    /**１）生徒学生健康診断票（一般）結果*/
    private boolean printMain1Result(final DB2UDB db2, final Vrw32alp svf, final Param param, final Map schnoMap) {
        boolean nonedata = false;
        if (Integer.parseInt(param._outputA) == 2) {
            final String schoolKind = getString("SCHOOL_KIND", schnoMap);
            final String form1 = getForm1(param, schoolKind);
            setForm(param, svf, form1, 4);

            svf.VrsOut("SCHOOL_NAME",  param.getSchoolInfo(schoolKind, SCHOOL_NAME2));
            svf.VrsOut("YEAR", nendo(db2, param, param._year));
            svf.VrsOut("NAME_SHOW" + (getMS932ByteLength(getString("NAME", schnoMap)) > 24 ? "_2" : "")  ,  getString("NAME", schnoMap));

            final String title =  ("P".equals(schoolKind) || "J".equals(schoolKind)) && "1".equals(param._useForm9_PJ_Ippan) ? "児童生徒健康診断票" : "K".equals(schoolKind) ? "園児健康診断票" : "P".equals(schoolKind) ? "児童健康診断票" : isPrintKenkouSindanIppan(param) ? "生徒健康診断票" : "生徒学生健康診断票";
            svf.VrsOut("TITLE",  title);
            svf.VrsOut("SCHREGNO"   ,  getString("SCHREGNO", schnoMap));   //改ページ用
            if ("on".equals(param._printSchregNo1)) {
                svf.VrsOut("SCHREGNO2"   ,  getString("SCHREGNO", schnoMap));
            }
            svf.VrsOut("SEX"        ,  getString("SEX", schnoMap));
            svf.VrsOut("BIRTHDAY"     ,  formatDate(db2, param, getString("BIRTHDAY", schnoMap)));

            if (param._isMieken) {
                svf.VrsOutn("GRADE"  , 1, getString("GRADE_NAME1", schnoMap));
                final String hrClassName1 = getString("HR_CLASS_NAME1", schnoMap);
                svf.VrsOutn(null != hrClassName1 && hrClassName1.length() > 4 ? "HR_NAME2_1" : "HR_NAME1", 1, hrClassName1);
                svf.VrsOutn("ATTENDNO"  , 1, getString("ATTENDNO", schnoMap));
            } else {
                svf.VrsOut("HR_NAME"    ,  getString("HR_NAME", schnoMap));
                svf.VrsOut("ATTENDNO"   ,  getString("ATTENDNO", schnoMap));
            }
            svf.VrsOut("AGE"        ,  getString("AGE", schnoMap));

            svf.VrEndRecord();
            nonedata = true;
            return nonedata;
        }

        final String schregno = getString("SCHREGNO", schnoMap);
        final String[] schoolKinds = {"K", "P", "J", "H"};
        String form1;
        for (int si = 0; si < schoolKinds.length; si++) {
            form1 = getForm1(param, schoolKinds[si]);
            if ("J".equals(schoolKinds[si]) && "1".equals(param._useForm9_PJ_Ippan)) {
                // 小中学校で9年用フォームを使用する時、改ページ防止のため中学の時はVrSetFormしない。
            } else if ("H".equals(schoolKinds[si]) && "1".equals(param._useForm7_JH_Ippan)) {
                // 中学高校で7年用フォームを使用する時、改ページ防止のため高校の時はVrSetFormしない。
            } else {
                setForm(param, svf, form1, 4);//一般
                if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
                    svf.VrsOut("NAME_HEADER", "氏名");
                    svf.VrsOut("GRADENAME_TITLE", "学年");
                }
            }
            //log.debug(" schregno = " + schregno + ", schoolKind = " + schoolKinds[si] + ", form = " + form1);

            ResultSet rs = null;
            int prevDataCnt = 0;
            try {
                final String psKey = "statementResult1";
                if (null == param._psMap.get(psKey)) {
                    param._psMap.put(psKey, db2.prepareStatement(statementResult(param, check_no1)));
                }
                final PreparedStatement ps = param._psMap.get(psKey);
                ps.setString(1, schregno);
                ps.setString(2, schoolKinds[si]);
                rs = ps.executeQuery();
                int dataCnt = "J".equals(schoolKinds[si]) && "1".equals(param._useForm9_PJ_Ippan) ? prevDataCnt : 0;
                while (rs.next()) {
                    dataCnt++;
                    if (("P".equals(schoolKinds[si]) || "J".equals(schoolKinds[si])) && "1".equals(param._useForm9_PJ_Ippan) && NumberUtils.isDigits(rs.getString("GRADE_CD"))) {
                        final int line = ("J".equals(schoolKinds[si]) ? 6 : 0) + Integer.parseInt(rs.getString("GRADE_CD"));
                        for (int i = dataCnt; i < line; i++) {
                            svf.VrsOut("SCHREGNO"   ,  rs.getString("SCHREGNO"));   //改ページ用
                            svf.VrEndRecord();
                        }
                        dataCnt = line;
                    }
                    if (("J".equals(schoolKinds[si]) || "H".equals(schoolKinds[si])) && "1".equals(param._useForm7_JH_Ippan) && NumberUtils.isDigits(rs.getString("GRADE_CD"))) {
                        final int line = ("H".equals(schoolKinds[si]) ? 3 : 0) + Integer.parseInt(rs.getString("GRADE_CD"));
                        for (int i = dataCnt; i < line; i++) {
                            svf.VrsOut("SCHREGNO"   ,  rs.getString("SCHREGNO"));   //改ページ用
                            svf.VrEndRecord();
                        }
                        dataCnt = line;
                    }
                    if (printMain1ResultSvf(db2, svf, param, rs, dataCnt, schoolKinds[si], form1)) {
                        nonedata = true;
                    }
                }
                prevDataCnt = dataCnt;
            } catch (Exception ex) {
                log.warn("printMain1Result read error!", ex);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }
        return nonedata;
    }

    /**１）生徒学生健康診断票（一般）結果*/
    private boolean printMain1ResultSvf(final DB2UDB db2, final Vrw32alp svf, final Param param, final ResultSet rs, final int dataCnt, final String schoolKind, final String form)
    {
        boolean nonedata = false;
        try {
            svf.VrsOut("SCHREGNO"   ,  rs.getString("SCHREGNO"));   //改ページ用
            svf.VrsOut("NAME_SHOW" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "_2" : "")  ,  rs.getString("NAME"));
            svf.VrsOut("SEX"        ,  rs.getString("SEX"));
            svf.VrsOut("BIRTHDAY"   ,  formatDate(db2, param, rs.getString("BIRTHDAY")));
            svf.VrsOut("AGE"        ,  rs.getString("AGE"));        //４月１日現在の年齢
            if (param._isMieken) {
                svf.VrsOutn("GRADE"  , dataCnt, rs.getString("GRADE_NAME1"));
                final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                svf.VrsOutn(null != hrClassName1 && hrClassName1.length() > 4 ? "HR_NAME2_1" : "HR_NAME1", dataCnt, hrClassName1);
                svf.VrsOutn("ATTENDNO"  , dataCnt, rs.getString("ATTENDNO"));
                log.info(" " + rs.getString("GRADE_NAME1") + " / " + rs.getString("HR_CLASS_NAME1") + " / " + rs.getString("ATTENDNO"));
            } else {
                if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
                    svf.VrsOut("COURSE_NAME", rs.getString("COURSECODENAME"));
                    final String gnPutStr = rs.getString("GRADE_NAME1");
                    final int gnLen = KNJ_EditEdit.getMS932ByteLength(gnPutStr);
                    if (gnLen > 8) {
                        svf.VrsOutn("GRADE_2"  , dataCnt, gnPutStr);
                    } else {
                        svf.VrsOutn("GRADE"  , dataCnt, gnPutStr);
                    }
                    final String hnPutStr = rs.getString("HR_CLASS_NAME1");
                    final int hnLen = KNJ_EditEdit.getMS932ByteLength(hnPutStr);
                    if (hnLen > 8) {
                        final String[] hnCutStr = KNJ_EditEdit.get_token(hnPutStr, 8, 2);
                        svf.VrsOutn("HR_NAME2_1", dataCnt,  hnCutStr[0]);
                        svf.VrsOutn("HR_NAME2_2", dataCnt,  hnCutStr[1]);
                    } else {
                        svf.VrsOutn("HR_NAME1", dataCnt,  hnPutStr);
                    }
                    svf.VrsOutn("ATTENDNO", dataCnt,  rs.getString("ATTENDNO"));
                } else {
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"  ,  rs.getString("ATTENDNO"));
                }
            }
            if (("P".equals(schoolKind) || "J".equals(schoolKind)) && "1".equals(param._useForm9_PJ_Ippan)) {
                svf.VrsOutn("YEAR"      , dataCnt,  rs.getString("GRADE_NAME1"));
            } else {
                svf.VrsOutn("YEAR"      , dataCnt,  nendo(db2, param, rs.getString("YEAR")));
            }
            for (int i = dataCnt + 1; i < dataCnt + 3; i++) {
                svf.VrsOutn("HR_NAME", i, "");
                svf.VrsOutn("ATTENDNO", i, "");
                svf.VrsOutn("YEAR", i, "");
            }
            svf.VrsOut("SCHOOL_NAME"  , param.getSchoolInfo(schoolKind, SCHOOL_NAME2));
            final String title =  ("P".equals(schoolKind) || "J".equals(schoolKind)) && "1".equals(param._useForm9_PJ_Ippan) ? "児童生徒健康診断票" : "P".equals(rs.getString("SCHOOL_KIND")) ? "児童健康診断票" : isPrintKenkouSindanIppan(param) ? "生徒健康診断票" : "生徒学生健康診断票";
            svf.VrsOut("TITLE",  title);
            svf.VrsOut("HEIGHT"         ,  rs.getString("HEIGHT"));
            svf.VrsOut("WEIGHT"         ,  rs.getString("WEIGHT"));
            if (!"J".equals(schoolKind)) {
                svf.VrsOut("SIT_HEIGHT"     ,  rs.getString("SITHEIGHT"));
            }
            final String sokuteiHunou = "測定不能";
            if ("1".equals(param._knjf030PrintVisionNumber) ||
                isNumber(rs.getString("R_BAREVISION")) || isNumber(rs.getString("L_BAREVISION")) ||
                    isNumber(rs.getString("R_VISION")) || isNumber(rs.getString("L_VISION"))) {
                if ("1".equals(rs.getString("VISION_CANTMEASURE"))) {
                    svf.VrsOut("R_BAREVISION_2"   ,  sokuteiHunou); // 視力
                    svf.VrsOut("L_BAREVISION_2"   ,  sokuteiHunou);
                } else {
                    svf.VrsOut("R_BAREVISION"   ,  rs.getString("R_BAREVISION")); // 視力
                    svf.VrsOut("L_BAREVISION"   ,  rs.getString("L_BAREVISION"));
                }
                svf.VrsOut("R_VISION"       ,  rs.getString("R_VISION"));
                svf.VrsOut("L_VISION"       ,  rs.getString("L_VISION"));
            } else {
                if ("1".equals(rs.getString("VISION_CANTMEASURE"))) {
                    svf.VrsOut("R_BAREVISION_2"   ,  sokuteiHunou); // 視力
                    svf.VrsOut("L_BAREVISION_2"   ,  sokuteiHunou);
                } else {
                    svf.VrsOut("R_BAREVISION"   ,  rs.getString("R_BAREVISION_MARK"));
                    svf.VrsOut("L_BAREVISION"   ,  rs.getString("L_BAREVISION_MARK"));
                }
                svf.VrsOut("R_VISION"       ,  rs.getString("R_VISION_MARK"));
                svf.VrsOut("L_VISION"       ,  rs.getString("L_VISION_MARK"));
            }
            if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
                svf.VrsOut("R_EAR1"          ,  rs.getString("R_EAR_DB1_NAME")); // 聴力
                svf.VrsOut("R_EAR2"          ,  rs.getString("R_EAR_DB4_NAME"));
                svf.VrsOut("L_EAR1"          ,  rs.getString("L_EAR_DB1_NAME")); // 聴力
                svf.VrsOut("L_EAR2"          ,  rs.getString("L_EAR_DB4_NAME"));
            } else {
                svf.VrsOut("R_EAR"          ,  rs.getString("R_EAR")); // 聴力
                svf.VrsOut("R_EAR_DB"       ,  rs.getString("R_EAR_DB"));
                svf.VrsOut("L_EAR"          ,  rs.getString("L_EAR"));
                svf.VrsOut("L_EAR_DB"       ,  rs.getString("L_EAR_DB"));
            }
            svf.VrsOut("ALBUMINURIA"    ,  rs.getString("ALBUMINURIA1CD")); // 尿
            svf.VrsOut("URICSUGAR"      ,  rs.getString("URICSUGAR1CD"));
            svf.VrsOut("URICBLEED"      ,  rs.getString("URICBLEED1CD"));
            svf.VrsOut("PH"             ,  rs.getString("DET017_REMARK1"));
            svf.VrsOut("ALBUMINURIA2"   ,  rs.getString("ALBUMINURIA2CD"));
            svf.VrsOut("URICSUGAR2"     ,  rs.getString("URICSUGAR2CD"));
            svf.VrsOut("URICBLEED2"     ,  rs.getString("URICBLEED2CD"));
            svf.VrsOut("PH2"            ,  rs.getString("DET017_REMARK2"));
            svf.VrsOut("URINE_OTHERS1"  ,  rs.getString("URICOTHERTEST"));
            if ("K".equals(schoolKind) || "P".equals(schoolKind) || "J".equals(schoolKind) && "1".equals(param._useParasite_J) || "H".equals(schoolKind) && "1".equals(param._useParasite_H)) {
                svf.VrsOut("PARASITE"   ,  rs.getString("PARASITE"));
            }
            svf.VrsOut("NUTRITION"      ,  rs.getString("NUTRITIONCD")); // 栄養状態
            svf.VrsOut("SPINERIB"       ,  rs.getString("SPINERIBCD")); // 脊柱・胸郭・四肢
            svf.VrsOut("EYEDISEASE"     ,  rs.getString("EYEDISEASECD")); // 眼の疾病及び異常
            svf.VrsOut("NOSEDISEASE"    ,  rs.getString("NOSEDISEASECD")); // 耳鼻咽頭疾患
            svf.VrsOut("SKINDISEASE"    ,  rs.getString("SKINDISEASECD")); // 皮膚疾患
            svf.VrsOut("HEART_MEDEXAM"  ,  rs.getString("HEART_MEDEXAM")); // 心臓
            svf.VrsOut("HEARTDISEASE1"  ,  rs.getString("HEARTDISEASECD"));
            svf.VrsOut("PHOTO_DATE"     ,  formatDate(db2, param, rs.getString("TB_FILMDATE"))); // 結核
            svf.VrsOut("FILMNO"         ,  rs.getString("TB_FILMNO"));
            svf.VrsOut("VIEWS1_1"       ,  rs.getString("TB_REMARKCD"));
            svf.VrsOut("OTHERS"         ,  rs.getString("TB_OTHERTESTCD"));
            svf.VrsOut("DISEASE_NAME"   ,  rs.getString("TB_NAMECD"));
            svf.VrsOut("GUIDANCE"       ,  rs.getString("TB_ADVISECD"));
            svf.VrsOut("ANEMIA"         ,  rs.getString("ANEMIA_REMARK")); // 貧血
            svf.VrsOut("HEMOGLOBIN"     ,  rs.getString("HEMOGLOBIN"));
            svf.VrsOut("OTHERDISEASE"   ,  rs.getString("OTHERDISEASECD")); // その他の疾病及び異常
            svf.VrsOut("VIEWS2_1"       ,  rs.getString("DOC_REMARK")); // // 学校医
            svf.VrsOut("DOC_DATE"       ,  formatDate(db2, param, rs.getString("DOC_DATE")));
            if (null != rs.getString("DOC_REMARK") && null != rs.getString("DOC_DATE") && null != param._printStamp) {
                svf.VrsOut("STAFFBTMC"  ,  param.getStampImageFile(rs.getString("YEAR"), "1"));
            }
            svf.VrsOut("DOC_TREAT1"     ,  rs.getString("TREATCD")); // 事後処置

            if (null != rs.getString("SITHEIGHT")) {
                final int ketaMax = FORM_KNJF030A_1_5.equals(form) || FORM_KNJF030A_1P_5.equals(form) ? 14 : 20;
                String remark1 = "座高（" + rs.getString("SITHEIGHT") + "cm）";
                remark1 += StringUtils.repeat(" ", ketaMax - getMS932ByteLength(remark1));
                remark1 += StringUtils.defaultString(rs.getString("REMARK"));
                svf.VrsOut("NOTE1",  remark1); // 備考
            } else {
                svf.VrsOut("NOTE1",  rs.getString("REMARK")); // 備考
            }
            svf.VrsOut("M_DATE",  formatDate(db2, param, rs.getString("DATE")));

            svf.VrEndRecord();
            nonedata = true;
        } catch (Exception ex) {
            log.warn("printMain12ResultSvf read error!", ex);
        }
        return nonedata;
    }

    private static String getForm2(final Param param, final String schoolKind) {
        String form = null;
        if (("J".equals(schoolKind) || "H".equals(schoolKind)) && "1".equals(param._useForm7_JH_Ha)) {
            form = "KNJF030A_2_6.frm";
        } else if (("P".equals(schoolKind) || "J".equals(schoolKind)) && "1".equals(param._useForm9_PJ_Ha)) {
            form = (param._isMiyagiken) ? "KNJF030A_2_8.frm": "KNJF030A_2_5.frm";
        } else if ("J".equals(schoolKind)) {
            form = (param._isMiyagiken) ? "KNJF030A_2_7J.frm": "KNJF030A_2J.frm";
        } else if (isPrintKenkouSindanIppan(param)) {
            if ("P".equals(schoolKind)) {
                form = "KNJF030A_2_2.frm";
            } else if ("2".equals(param._printKenkouSindanIppan)) {
                form = "KNJF030A_2_3.frm";
            } else if ("3".equals(param._printKenkouSindanIppan)) {
                form = "KNJF030A_2_4.frm";
            } else {
                if ("1".equals(param._useForm5_H_Ha)) {
                    form = "KNJF030A_2_5G.frm";
                } else {
                    form = (param._isMiyagiken) ? "KNJF030A_2_7.frm": "KNJF030A_2.frm";
                }
            }
        } else {
            if ("P".equals(schoolKind)) {
                form = "KNJF030_2_2.frm";
            } else {
                form = "KNJF030_2.frm";
            }
        }
        if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
            form = "KNJF030_2_3.frm";
        }
        return form;
    }

    /**２）生徒学生健康診断票（歯・口腔）結果*/
    private boolean printMain2Result(final DB2UDB db2, final Form form, final Param param, final Map schnoMap) {
        boolean nonedata = false;
        if (Integer.parseInt(param._outputB) == 2) {
            final String schoolKind = getString("SCHOOL_KIND", schnoMap);
            String form2 = "";
            if (param._isKumamoto) {
                form2 = "KNJF030A_2_3_2.frm";
                if (2016 > Integer.parseInt(param._year)) {
                    form2 = getForm2(param, schoolKind);
                }
                final int minGrade = Integer.parseInt(param._minGradeMap.get(schoolKind));
                final int setGrade = (Integer.parseInt(param._year) > 2022) ? 0: Integer.parseInt(param._year) - 2016 + minGrade;
                if (setGrade > 0 && Integer.parseInt(getString("GRADE", schnoMap)) > setGrade) {
                    form2 = getForm2(param, schoolKind);
                }
                if (!"H".equals(schoolKind)) {
                    form2 = getForm2(param, schoolKind);
                }
            } else if (param._isChiyodaKudan) {
                form2 = "KNJF030_2_6G.frm";
            } else {
                form2 = getForm2(param, schoolKind);
            }
            form.setForm(form2, 4);

            if (param._isSeireki) {
                form.VrsOut("NENDO1", param._year);
            } else {
                final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(param._year));
                form.VrsOut("NENDO1", gengou.substring(0,2));
                form.VrsOut("NENDO2", gengou.substring(2));
            }
            form.VrsOut("NAME_SHOW"  ,  getString("NAME", schnoMap));

            final String title =  ("P".equals(schoolKind) || "J".equals(schoolKind)) && "1".equals(param._useForm9_PJ_Ha) ? "児童生徒健康診断票" : "P".equals(schoolKind) ? "児童健康診断票" : isPrintKenkouSindanIppan(param) ? "生徒健康診断票" : "生徒学生健康診断票";
            form.VrsOut("TITLE",  title);
            form.VrsOut("SCHREGNO"   ,  getString("SCHREGNO", schnoMap));   //改ページ用
            if ("on".equals(param._printSchregNo2)) {
                form.VrsOut("SCHREGNO2"   ,  getString("SCHREGNO", schnoMap));
            }
            form.VrsOut("SEX"        ,  getString("SEX", schnoMap));
            form.VrsOut("BIRTHDAY"     ,  formatDate(db2, param, getString("BIRTHDAY", schnoMap)));

            if (param._isChiyodaKudan) {
                final String entYearGradeCd = getString("ENT_YEAR_GRADE_CD", schnoMap);
                if (NumberUtils.isDigits(entYearGradeCd)) {
                    for (int cd = 1; cd < ("H".equals(schoolKind) ? 3 : 0) + Integer.parseInt(entYearGradeCd); cd++) {
                        form.VrsOut("SCHREGNO", getString("SCHREGNO", schnoMap));   //改ページ用
                        form.VrEndRecord();
                    }
                }
            }

            if (param._isMieken || "1".equals(param._useForm5_H_Ha)) {
                form.VrsOutn("GRADE"  , 1, getString("GRADE_NAME1", schnoMap));
                form.VrsOutSelectField(new String[] {"HR_NAME1", "HR_NAME2_1"}, getString("HR_CLASS_NAME1", schnoMap));
                form.VrsOutn("ATTENDNO"  , 1, getString("ATTENDNO", schnoMap));
            } else {
                form.VrsOut("HR_NAME"   ,  getString("HR_NAME", schnoMap));
                form.VrsOut("ATTENDNO"  ,  getString("ATTENDNO", schnoMap));
            }
            form.VrsOut("SCHOOL_NAME"  , param.getSchoolInfo(schoolKind, SCHOOL_NAME2));
            form.VrsOut("AGE"        ,  getString("AGE", schnoMap));

            form.VrEndRecord();
            nonedata = true;
            return nonedata;
        }

        final String schregno = getString("SCHREGNO", schnoMap);
        final String[] schoolKinds;
        final String[] all = {"K", "P", "J", "H", "A"};
        if (param._isChiyodaKudan) {
            schoolKinds = new String[] {"J", "H"};
        } else if ("1".equals(param._use_prg_schoolkind)) {
            if ("1".equals(param._useForm9_PJ_Ha) && (ArrayUtils.isEmpty(param._selectSchoolKind) || ArrayUtils.contains(param._selectSchoolKind, "P") || ArrayUtils.contains(param._selectSchoolKind, "J"))) {
                schoolKinds = new String[] {"P", "J"};
            } else if ("1".equals(param._useForm7_JH_Ha) && (ArrayUtils.isEmpty(param._selectSchoolKind) || ArrayUtils.contains(param._selectSchoolKind, "J") || ArrayUtils.contains(param._selectSchoolKind, "H"))) {
                schoolKinds = new String[] {"J", "H"};
            } else {
                schoolKinds = ArrayUtils.isEmpty(param._selectSchoolKind) ? all : param._selectSchoolKind;
            }
        } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOLKIND)) {
            if ("1".equals(param._useForm9_PJ_Ha)) {
                schoolKinds = new String[] {"P", "J"};
            } else if ("1".equals(param._useForm7_JH_Ha)) {
                schoolKinds = new String[] {"J", "H"};
            } else {
                schoolKinds = new String[] {param._SCHOOLKIND};
            }
        } else {
            final List list = new ArrayList();
            for (int i = 0; i < all.length; i++) {
                if (null != param._minGradeMap.get(all[i])) {
                    list.add(all[i]);
                }
            }
            schoolKinds = new String[list.size()];
            list.toArray(schoolKinds);
        }
        if (param._isOutputDebug) {
            log.info(" main2 schoolKinds = " + ArrayUtils.toString(schoolKinds));
        }

        int prevDataCnt = 0;
        boolean hasDataP = false;
        for (int si = 0; si < schoolKinds.length; si++) {
            final String schoolKind = schoolKinds[si];
            String form2 = "";
            if (param._isKumamoto) {
                form2 = "KNJF030A_2_3_2.frm";
                if (2016 > Integer.parseInt(param._year)) {
                    form2 = getForm2(param, schoolKind);
                }
                final int minGrade = Integer.parseInt(param._minGradeMap.get(schoolKind));
                final int setGrade = (Integer.parseInt(param._year) > 2022) ? 0: Integer.parseInt(param._year) - 2016 + minGrade;
                if (setGrade > 0 && Integer.parseInt(getString("GRADE", schnoMap)) > setGrade) {
                    form2 = getForm2(param, schoolKind);
                }
                if (!"H".equals(schoolKind)) {
                    form2 = getForm2(param, schoolKind);
                }
            } else if (param._isChiyodaKudan) {
                form2 = "KNJF030_2_6G.frm";
            } else {
                form2 = getForm2(param, schoolKind);
            }
            if ("J".equals(schoolKind) && "1".equals(param._useForm9_PJ_Ha) && hasDataP || param._isChiyodaKudan && "H".equals(schoolKind)) {
                // 小中学校で9年用フォームを使用する時、改ページ防止のため中学の時はVrSetFormしない。
            } else {
                form.setForm(form2, 4);
                if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
                    form.VrsOut("NAME_HEADER", "氏名");
                    form.VrsOut("GRADENAME_TITLE", "学年");
                }
            }

            try {
                final String psKey = "statementResult2";
                if (null == param._psMap.get(psKey)) {
                    final String sql2 = statementResult(param, check_no2);
                    if (param._isOutputDebug) {
                        log.info(" sql2 = " + sql2);
                    }
                    param._psMap.put(psKey, db2.prepareStatement(sql2));
                }
                int dataCnt = "J".equals(schoolKind) && "1".equals(param._useForm9_PJ_Ha) || param._isChiyodaKudan && "H".equals(schoolKind) ? prevDataCnt : 0;
                boolean isFirst = true;
                int lastGradeCd = 1;
                for (final Map rs : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {schregno, schoolKind})) {
                    if (isFirst) {
                        if ("2".equals(param._printKenkouSindanIppan) || "1".equals(param._knjf030addBlankGradeColumn)) {
                            final String entYearGradeCd = getString("ENT_YEAR_GRADE_CD", schnoMap);
                            if (NumberUtils.isDigits(entYearGradeCd)) {
                                for (int cd = 1; cd < Integer.parseInt(entYearGradeCd); cd++) {
                                    form.VrsOut("SCHREGNO", getString("SCHREGNO", schnoMap));   //改ページ用
                                    form.VrEndRecord();
                                    dataCnt += 1;
                                }
                                lastGradeCd = Integer.parseInt(entYearGradeCd) - 1;
                            }
                        }
                    }
                    isFirst = false;
                    if ("2".equals(param._printKenkouSindanIppan) || "1".equals(param._knjf030addBlankGradeColumn)) {
                        final String gradeCd = getString("GRADE_CD", rs);
                        if (NumberUtils.isDigits(gradeCd)) {
                            for (int cd = lastGradeCd; cd < Integer.parseInt(gradeCd) - 1; cd++) {
                                form.VrsOut("SCHREGNO", getString("SCHREGNO", schnoMap));   //改ページ用
                                form.VrEndRecord();
                                dataCnt += 1;
                            }
                            lastGradeCd = Integer.parseInt(gradeCd) - 1;
                        }
                    }
                    dataCnt++;
                    if (("P".equals(schoolKind) || "J".equals(schoolKind)) && "1".equals(param._useForm9_PJ_Ha) && NumberUtils.isDigits(getString("GRADE_CD", rs))) {
                        final int line = ("J".equals(schoolKind) ? 6 : 0) + Integer.parseInt(getString("GRADE_CD", rs));
                        for (int i = dataCnt; i < line; i++) {
                            form.VrsOut("SCHREGNO"   ,  getString("SCHREGNO", rs));   //改ページ用
                            form.VrEndRecord();
                        }
                        dataCnt = line;
                    }
                    if (("J".equals(schoolKind) || "H".equals(schoolKind)) && ("1".equals(param._useForm7_JH_Ha) || param._isChiyodaKudan) && NumberUtils.isDigits(getString("GRADE_CD", rs))) {
                        final int line = ("H".equals(schoolKind) ? 3 : 0) + Integer.parseInt(getString("GRADE_CD", rs));
                        for (int i = dataCnt; i < line; i++) {
                            form.VrsOut("SCHREGNO"   ,  getString("SCHREGNO", rs));   //改ページ用
                            form.VrEndRecord();
                        }
                        dataCnt = line;
                    }
                    if (printMain2ResultSvf(db2, form, param, rs, dataCnt, schoolKind)) {
                        nonedata = true;
                        if ("P".equals(schoolKind) && "1".equals(param._useForm9_PJ_Ha)) {
                            hasDataP = true;
                        }
                    }
                    if ("2".equals(param._printKenkouSindanIppan) || "1".equals(param._knjf030addBlankGradeColumn)) {
                        final String gradeCd = getString("GRADE_CD", rs);
                        if (NumberUtils.isDigits(gradeCd)) {
                            lastGradeCd = Integer.parseInt(gradeCd);
                        }
                    }
                }
                prevDataCnt = dataCnt;
            } catch (Exception ex) {
                log.warn("printMain12Result read error!", ex);
            }
        }
        return nonedata;
    }

    /**２）生徒学生健康診断票（歯・口腔）結果*/
    private boolean printMain2ResultSvf(final DB2UDB db2, final Form form, final Param param, final Map rs, final int dataCnt, final String schoolKind) {
        boolean nonedata = false;
        try {
            final String year = getString("YEAR", rs);
            form.VrsOut("SCHREGNO"   ,  getString("SCHREGNO", rs));   //改ページ用
            if ("on".equals(param._printSchregNo2)) {
                form.VrsOut("SCHREGNO2"   ,  getString("SCHREGNO", rs));
            }
            form.VrsOut("NAME_SHOW" + (getMS932ByteLength(getString("NAME", rs)) > 24 ? "_2" : "")  ,  getString("NAME", rs));
            form.VrsOut("SEX"        ,  getString("SEX", rs));
            form.VrsOut("BIRTHDAY"   ,  formatDate(db2, param, getString("BIRTHDAY", rs)));
            form.VrsOut("AGE"        ,  getString("AGE", rs));        //４月１日現在の年齢
            if (param._isMieken || "1".equals(param._useForm5_H_Ha) || param._isChiyodaKudan) {
                form.VrsOutn("GRADE"  , dataCnt, getString("GRADE_NAME1", rs));
                form.VrsOutnSelectField(new String[] {"HR_NAME1", "HR_NAME2_1"}, dataCnt, getString("HR_CLASS_NAME1", rs));
                form.VrsOutn("ATTENDNO"  , dataCnt, getString("ATTENDNO", rs));
            } else {
                if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
                    form.VrsOut("COURSE_NAME", getString("COURSECODENAME", rs));
                    final String gnPutStr = getString("GRADE_NAME1", rs);
                    final int gnLen = KNJ_EditEdit.getMS932ByteLength(gnPutStr);
                    if (gnLen > 8) {
                        form.VrsOutn("GRADE_2"  , dataCnt, gnPutStr);
                    } else {
                        form.VrsOutn("GRADE"  , dataCnt, gnPutStr);
                    }
                    final String hnPutStr = getString("HR_CLASS_NAME1", rs);
                    final int hnLen = KNJ_EditEdit.getMS932ByteLength(hnPutStr);
                    if (hnLen > 8) {
                        final String[] hnCutStr = KNJ_EditEdit.get_token(hnPutStr, 8, 2);
                        form.VrsOutn("HR_NAME2_1", dataCnt,  hnCutStr[0]);
                        form.VrsOutn("HR_NAME2_2", dataCnt,  hnCutStr[1]);
                    } else {
                        form.VrsOutn("HR_NAME1", dataCnt,  hnPutStr);
                    }
                    form.VrsOutn("ATTENDNO", dataCnt,  getString("ATTENDNO", rs));
                } else {
                    form.VrsOut("HR_NAME"   ,  getString("HR_NAME", rs));
                    form.VrsOut("ATTENDNO"  ,  getString("ATTENDNO", rs));
                }
            }
            form.VrsOut("SCHOOL_NAME"  , param.getSchoolInfo(schoolKind, SCHOOL_NAME2));
            if (param._isSeireki) {
                form.VrsOut("NENDO1", getString("YEAR", rs));
            } else {
                final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(getString("YEAR", rs)));
                form.VrsOut("NENDO1", gengou.substring(0,2));
                form.VrsOut("NENDO2", gengou.substring(2));
            }
            if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
                form.VrsOut("JAWSCD0"  ,  getString("JAWS_JOINTCD1", rs)); //歯列 1
                form.VrsOut("JAWSCD1"  ,  getString("JAWS_JOINTCD2", rs)); //歯列 2
                form.VrsOut("JAWSCD2"  ,  getString("JAWS_JOINTCD3", rs)); //歯列 3
                form.VrsOut("JAWSCD3"  ,  getString("JAWS_JOINTCD31", rs)); //咬合 1
                form.VrsOut("JAWSCD4"  ,  getString("JAWS_JOINTCD32", rs)); //咬合 2
                form.VrsOut("JAWSCD5"  ,  getString("JAWS_JOINTCD33", rs)); //咬合 3
                form.VrsOut("JAWS_JOINTCD20"  ,  getString("JAWS_JOINTCD21", rs));  //顎関節1
                form.VrsOut("JAWS_JOINTCD21"  ,  getString("JAWS_JOINTCD22", rs));  //顎関節2
                form.VrsOut("JAWS_JOINTCD22"  ,  getString("JAWS_JOINTCD23", rs));  //顎関節3
            } else {
                form.VrsOut("JAWS_JOINTCD0"  ,  getString("JAWS_JOINTCD1", rs));
                form.VrsOut("JAWS_JOINTCD1"  ,  getString("JAWS_JOINTCD2", rs));
                form.VrsOut("JAWS_JOINTCD2"  ,  getString("JAWS_JOINTCD3", rs));
                form.VrsOut("JAWS_JOINTCD20"  ,  getString("JAWS_JOINTCD21", rs));
                form.VrsOut("JAWS_JOINTCD21"  ,  getString("JAWS_JOINTCD22", rs));
                form.VrsOut("JAWS_JOINTCD22"  ,  getString("JAWS_JOINTCD23", rs));
                form.VrsOut("JAWS_JOINTCD30"  ,  getString("JAWS_JOINTCD31", rs));
                form.VrsOut("JAWS_JOINTCD31"  ,  getString("JAWS_JOINTCD32", rs));
                form.VrsOut("JAWS_JOINTCD32"  ,  getString("JAWS_JOINTCD33", rs));
            }
            form.VrsOut("PLAQUECD0"  ,  getString("PLAQUECD1", rs));
            form.VrsOut("PLAQUECD1"  ,  getString("PLAQUECD2", rs));
            form.VrsOut("PLAQUECD2"  ,  getString("PLAQUECD3", rs));
            form.VrsOut("GUMCD0"  ,  getString("GUMCD1", rs));
            form.VrsOut("GUMCD1"  ,  getString("GUMCD2", rs));
            form.VrsOut("GUMCD2"  ,  getString("GUMCD3", rs));
            form.VrsOut("ORTHODONTICS"+("1".equals(getString("ORTHODONTICS", rs)) ? "1" : "0"), "○");
            svfVrsOutTooth(form, param, "UP_L_ADULT8", getString("UP_L_ADULT8", rs));
            svfVrsOutTooth(form, param,  "UP_L_ADULT7", getString("UP_L_ADULT7", rs));
            svfVrsOutTooth(form, param,  "UP_L_ADULT6", getString("UP_L_ADULT6", rs));
            svfVrsOutTooth(form, param,  "UP_L_ADULT5", getString("UP_L_ADULT5", rs));
            svfVrsOutTooth(form, param,  "UP_L_ADULT4", getString("UP_L_ADULT4", rs));
            svfVrsOutTooth(form, param,  "UP_L_ADULT3", getString("UP_L_ADULT3", rs));
            svfVrsOutTooth(form, param,  "UP_L_ADULT2", getString("UP_L_ADULT2", rs));
            svfVrsOutTooth(form, param,  "UP_L_ADULT1", getString("UP_L_ADULT1", rs));
            svfVrsOutTooth(form, param,  "UP_R_ADULT1", getString("UP_R_ADULT1", rs));
            svfVrsOutTooth(form, param,  "UP_R_ADULT2", getString("UP_R_ADULT2", rs));
            svfVrsOutTooth(form, param,  "UP_R_ADULT3", getString("UP_R_ADULT3", rs));
            svfVrsOutTooth(form, param,  "UP_R_ADULT4", getString("UP_R_ADULT4", rs));
            svfVrsOutTooth(form, param,  "UP_R_ADULT5", getString("UP_R_ADULT5", rs));
            svfVrsOutTooth(form, param,  "UP_R_ADULT6", getString("UP_R_ADULT6", rs));
            svfVrsOutTooth(form, param,  "UP_R_ADULT7", getString("UP_R_ADULT7", rs));
            svfVrsOutTooth(form, param,  "UP_R_ADULT8", getString("UP_R_ADULT8", rs));
            svfVrsOutTooth(form, param,  "UP_L_BABY5", getString("UP_L_BABY5", rs));
            svfVrsOutTooth(form, param,  "UP_L_BABY4", getString("UP_L_BABY4", rs));
            svfVrsOutTooth(form, param,  "UP_L_BABY3", getString("UP_L_BABY3", rs));
            svfVrsOutTooth(form, param,  "UP_L_BABY2", getString("UP_L_BABY2", rs));
            svfVrsOutTooth(form, param,  "UP_L_BABY1", getString("UP_L_BABY1", rs));
            svfVrsOutTooth(form, param,  "UP_R_BABY1", getString("UP_R_BABY1", rs));
            svfVrsOutTooth(form, param,  "UP_R_BABY2", getString("UP_R_BABY2", rs));
            svfVrsOutTooth(form, param,  "UP_R_BABY3", getString("UP_R_BABY3", rs));
            svfVrsOutTooth(form, param,  "UP_R_BABY4", getString("UP_R_BABY4", rs));
            svfVrsOutTooth(form, param,  "UP_R_BABY5", getString("UP_R_BABY5", rs));
            svfVrsOutTooth(form, param,  "LW_L_BABY5", getString("LW_L_BABY5", rs));
            svfVrsOutTooth(form, param,  "LW_L_BABY4", getString("LW_L_BABY4", rs));
            svfVrsOutTooth(form, param,  "LW_L_BABY3", getString("LW_L_BABY3", rs));
            svfVrsOutTooth(form, param,  "LW_L_BABY2", getString("LW_L_BABY2", rs));
            svfVrsOutTooth(form, param,  "LW_L_BABY1", getString("LW_L_BABY1", rs));
            svfVrsOutTooth(form, param,  "LW_R_BABY1", getString("LW_R_BABY1", rs));
            svfVrsOutTooth(form, param,  "LW_R_BABY2", getString("LW_R_BABY2", rs));
            svfVrsOutTooth(form, param,  "LW_R_BABY3", getString("LW_R_BABY3", rs));
            svfVrsOutTooth(form, param,  "LW_R_BABY4", getString("LW_R_BABY4", rs));
            svfVrsOutTooth(form, param,  "LW_R_BABY5", getString("LW_R_BABY5", rs));
            svfVrsOutTooth(form, param,  "LW_L_ADULT8", getString("LW_L_ADULT8", rs));
            svfVrsOutTooth(form, param,  "LW_L_ADULT7", getString("LW_L_ADULT7", rs));
            svfVrsOutTooth(form, param,  "LW_L_ADULT6", getString("LW_L_ADULT6", rs));
            svfVrsOutTooth(form, param,  "LW_L_ADULT5", getString("LW_L_ADULT5", rs));
            svfVrsOutTooth(form, param,  "LW_L_ADULT4", getString("LW_L_ADULT4", rs));
            svfVrsOutTooth(form, param,  "LW_L_ADULT3", getString("LW_L_ADULT3", rs));
            svfVrsOutTooth(form, param,  "LW_L_ADULT2", getString("LW_L_ADULT2", rs));
            svfVrsOutTooth(form, param,  "LW_L_ADULT1", getString("LW_L_ADULT1", rs));
            svfVrsOutTooth(form, param,  "LW_R_ADULT1", getString("LW_R_ADULT1", rs));
            svfVrsOutTooth(form, param,  "LW_R_ADULT2", getString("LW_R_ADULT2", rs));
            svfVrsOutTooth(form, param,  "LW_R_ADULT3", getString("LW_R_ADULT3", rs));
            svfVrsOutTooth(form, param,  "LW_R_ADULT4", getString("LW_R_ADULT4", rs));
            svfVrsOutTooth(form, param,  "LW_R_ADULT5", getString("LW_R_ADULT5", rs));
            svfVrsOutTooth(form, param,  "LW_R_ADULT6", getString("LW_R_ADULT6", rs));
            svfVrsOutTooth(form, param,  "LW_R_ADULT7", getString("LW_R_ADULT7", rs));
            svfVrsOutTooth(form, param,  "LW_R_ADULT8", getString("LW_R_ADULT8", rs));
            form.VrsOut("BABYTOOTH"          ,  getString("BABYTOOTH", rs));
            form.VrsOut("REMAINBABYTOOTH"    ,  getString("REMAINBABYTOOTH", rs));
            form.VrsOut("TREATEDBABYTOOTH"   ,  getString("TREATEDBABYTOOTH", rs));
            form.VrsOut("BRACKBABYTOOTH"     ,  getString("BRACK_BABYTOOTH", rs));//Add
            form.VrsOut("ADULTTOOTH"         ,  getString("ADULTTOOTH", rs));
            form.VrsOut("REMAINADULTTOOTH"   ,  getString("REMAINADULTTOOTH", rs));
            form.VrsOut("TREATEDADULTTOOTH"  ,  getString("TREATEDADULTTOOTH", rs));
            form.VrsOut("LOSTADULTTOOTH"     ,  getString("LOSTADULTTOOTH", rs));

            String coComment = "";
            if (NumberUtils.isDigits(getString("BRACK_ADULTTOOTH", rs)) || NumberUtils.isDigits(getString("CHECKADULTTOOTH", rs))) {
                final int brackAdultTooth = NumberUtils.isDigits(getString("BRACK_ADULTTOOTH", rs)) ? Integer.parseInt(getString("BRACK_ADULTTOOTH", rs)) : 0;
                final int checkAdultTooth = NumberUtils.isDigits(getString("CHECKADULTTOOTH", rs)) ? Integer.parseInt(getString("CHECKADULTTOOTH", rs)) : 0;
                form.VrsOut("BRACKADULTTOOTH"    ,  String.valueOf(brackAdultTooth + checkAdultTooth));//Add
                coComment = (param._isMiyagiken && (brackAdultTooth + checkAdultTooth) > 0) ? "ＣＯ要観察": "";
            } else {
                form.VrsOut("BRACKADULTTOOTH"    ,  "");//Add
            }

            // その他の疾病及び異常
            if ("2".equals(param._printKenkouSindanIppan)) {
                if ("1".equals(getString("OTHERDISEASECD_NAMESPARE2", rs))) {
                    form.VrsOutSelectField(new String[] {"TOOTHOTHERDISEASE3", "TOOTHOTHERDISEASE4"}, getString("OTHERDISEASE_TEXT", rs));
                } else {
                    form.VrsOutSelectField(new String[] {"TOOTHOTHERDISEASE3", "TOOTHOTHERDISEASE4"}, getString("OTHERDISEASECD", rs));
                }
            } else {
                if (param._isMiyagiken) {
                    final String setOther1 = getTextOrName(StringUtils.defaultString(getString("OTHERDISEASE_TEXT", rs)), StringUtils.defaultString(getString("OTHERDISEASECD", rs)));
                    final String setOther2 = StringUtils.defaultString(getString("OTHERDISEASECD3", rs));
                    final String setOther3 = StringUtils.defaultString(getString("OTHERDISEASECD4", rs));
                    final String setOtherdisease = setOther1 + ("".equals(setOther1) ? "": "　") + setOther2 + ("".equals(setOther2) ? "": "　") + setOther3;
                    form.VrsOut("TOOTHOTHERDISEASE",  setOtherdisease);
                } else {
                    if ("1".equals(getString("OTHERDISEASECD_NAMESPARE2", rs)) && !form.hasField("TOOTHOTHERDISEASE3")) {
                        if ("KINDAI".equals(param._namemstZ010Name1)) {
                            if ("1".equals(getString("OTHERDISEASECD_NAMESPARE1", rs))) {
                                printSlash(param, form, "SLASH_TOOTHOTHERDISEASE");
                            } else {
                                form.VrsOutSelectField(new String[] {"TOOTHOTHERDISEASE", "TOOTHOTHERDISEASE2"}, Form.concatIfNotBlank(StringUtils.defaultString(getString("OTHERDISEASE_TEXT", rs), getString("OTHERDISEASECD", rs)), "／", getString("CALCULUS", rs)));
                            }
                        } else {
                            form.VrsOutSelectField(new String[] {"TOOTHOTHERDISEASE", "TOOTHOTHERDISEASE2"}, Form.concatIfNotBlank(StringUtils.defaultString(getString("OTHERDISEASE_TEXT", rs), getString("OTHERDISEASECD", rs)), "／", getString("CALCULUS", rs)));
                        }
                    } else {
                        if ("KINDAI".equals(param._namemstZ010Name1)) {
                            if ("1".equals(getString("OTHERDISEASECD_NAMESPARE1", rs))) {
                                printSlash(param, form, "SLASH_TOOTHOTHERDISEASE");
                            } else {
                                form.VrsOutSelectField(new String[] {"TOOTHOTHERDISEASE", "TOOTHOTHERDISEASE2"}, Form.concatIfNotBlank(getString("OTHERDISEASECD", rs), "／", getString("CALCULUS", rs)));
                            }
                        } else {
                            form.VrsOutSelectField(new String[] {"TOOTHOTHERDISEASE", "TOOTHOTHERDISEASE2"}, Form.concatIfNotBlank(getString("OTHERDISEASECD", rs), "／", getString("CALCULUS", rs)));
                        }
                        form.VrsOutSelectField(new String[] {"TOOTHOTHERDISEASE3", "TOOTHOTHERDISEASE4"}, getString("OTHERDISEASE_TEXT", rs));
                    }
                }
            }
            form.VrsOut("C0"     ,  getString("DENTISTREMARK_CO", rs));
            form.VrsOut("G0"     ,  getString("DENTISTREMARK_GO", rs));
            form.VrsOut("G"      ,  getString("DENTISTREMARK_G", rs));
            String yearF242name1 = "";
            if (null != getString("DENTISTREMARKDATE", rs) && null != param._yearF242name1Map.get(year)) {
                yearF242name1 = (String) param._yearF242name1Map.get(year);
            }
            // 学校歯科医所見
            if ("2".equals(param._printKenkouSindanIppan)) {
                boolean isSisikiInput = false;
                final String[] sisiki = {"UP_L_ADULT8","UP_L_ADULT7","UP_L_ADULT6","UP_L_ADULT5","UP_L_ADULT4","UP_L_ADULT3","UP_L_ADULT2","UP_L_ADULT1","UP_R_ADULT1","UP_R_ADULT2","UP_R_ADULT3","UP_R_ADULT4","UP_R_ADULT5","UP_R_ADULT6","UP_R_ADULT7","UP_R_ADULT8","UP_L_BABY5","UP_L_BABY4","UP_L_BABY3","UP_L_BABY2","UP_L_BABY1","UP_R_BABY1","UP_R_BABY2","UP_R_BABY3","UP_R_BABY4","UP_R_BABY5","LW_L_BABY5","LW_L_BABY4","LW_L_BABY3","LW_L_BABY2","LW_L_BABY1","LW_R_BABY1","LW_R_BABY2","LW_R_BABY3","LW_R_BABY4","LW_R_BABY5","LW_L_ADULT8","LW_L_ADULT7","LW_L_ADULT6","LW_L_ADULT5","LW_L_ADULT4","LW_L_ADULT3","LW_L_ADULT2","LW_L_ADULT1","LW_R_ADULT1","LW_R_ADULT2","LW_R_ADULT3","LW_R_ADULT4","LW_R_ADULT5","LW_R_ADULT6","LW_R_ADULT7","LW_R_ADULT8"};
                for (int i = 0; i < sisiki.length; i++) {
                    if (null != getString(sisiki[i], rs)) {
                        isSisikiInput = true;
                        break;
                    }
                }
                if (isSisikiInput == false && null != getString("DENTISTREMARKMONTH", rs)) {
                    // 歯式がすべてnullで日付がnullではない場合に"未検査"を表示
                    form.VrsOut("DENTISTREMARK", "未検査");
                }
            } else {

                final String dentistremark;
                if ("1".equals(getString("DENTISTREMARKCD_NAMESPARE2", rs))) {
                    dentistremark = coComment + StringUtils.defaultString(getString("DENTISTREMARK", rs));
                } else {
                    dentistremark = coComment + StringUtils.defaultString(getString("DENTISTREMARKCD", rs));
                }
                form.VrsOut("DENTISTREMARK",  dentistremark + (StringUtils.isBlank(yearF242name1) ? "" : " " + yearF242name1));
            }
            // 学校歯科医月日
            form.VrsOut("month"  ,  getString("DENTISTREMARKMONTH", rs));
            form.VrsOut("day"    ,  getString("DENTISTREMARKDAY", rs));
            // 事後処置
            if ("2".equals(param._printKenkouSindanIppan)) {
                if ("1".equals(getString("DENTISTTREAT_NAMESPARE2", rs))) {
                    final String dentistTreatText = getString("DENTISTTREAT_TEXT", rs);
                    form.VrsOut(null != dentistTreatText && dentistTreatText.length() > 10 ? "DENTISTTREAT4_1" : "DENTISTTREAT3_1", dentistTreatText);
                    final String dentistTreatText2 = getString("DENTISTTREAT_TEXT2", rs);
                    form.VrsOut(null != dentistTreatText2 && dentistTreatText2.length() > 10 ? "DENTISTTREAT4_2" : "DENTISTTREAT3_2", dentistTreatText2);
                    final String dentistTreatText3 = getString("DENTISTTREAT_TEXT3", rs);
                    form.VrsOut(null != dentistTreatText3 && dentistTreatText3.length() > 10 ? "DENTISTTREAT4_3" : "DENTISTTREAT3_3", dentistTreatText3);
                } else {
                    form.VrsOutSelectField(new String[] {"DENTISTTREAT3_2", "DENTISTTREAT4_2"}, getString("DENTISTTREAT", rs));
                }
            } else {
                if ("1".equals(getString("DENTISTTREAT_NAMESPARE2", rs))) {
                    form.VrsOutSelectField(new String[] {"DENTISTTREAT", "DENTISTTREAT2"}, getString("DENTISTTREAT_TEXT", rs));
                } else {
                    form.VrsOut("DENTISTTREAT", getString("DENTISTTREAT", rs));
                    form.VrsOutSelectField(new String[] {"DENTISTTREAT3", "DENTISTTREAT4"}, getString("DENTISTTREAT_TEXT", rs));
                }
            }
            //"2"(熊本)以外は出力するよう、変更
            if (!"2".equals(param._printKenkouSindanIppan) && null != getString("DENTISTREMARKDATE", rs) && null != param._printStamp2) {
                form.VrsOut("STAFFBTMC"  ,  param.getStampImageFile(param._year, "2"));
            }
            form.VrEndRecord();
            nonedata = true;
        } catch (Exception ex) {
            log.warn("printMain12ResultSvf read error!", ex);
        }
        return nonedata;
    }

    /** 文字列strが数値でない、または数値が0のときにfalse、それ以外(strが0以外の数値)でtrue を返す */
    public static boolean isNumber(final String str) {
        return NumberUtils.isNumber(str) && 0.0 != Double.parseDouble(str);
    }

    private void svfVrsOutTooth(final Form form, final Param param, final String field, final String code) {
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

        if (param._isMiyagiken && "01".equals(code)) { // 宮城県は "-" を印字しない
        } else {
            form.VrsOut(field, mark);
        }
        if ("04".equals(code)) {
            return;
        }
        form.VrsOut("NOW_"+field, "／");
    }

    /**３）健康診断の未受検項目のある生徒へ*/
    private boolean printMain3(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String form = param._isKumamoto ? "KNJF030_3KUMA.frm" : "KNJF030_3.frm";
            final String text = getDocumentMst(db2, param, "03", "TEXT");
            final String title = getDocumentMst(db2, param, "03", "TITLE");

            setForm(param, svf, form, 4);
            final String statementMeisai3 = statementMeisai(param, check_no3);
            //log.debug(" sql3 = " + statementMeisai3);
            ps = db2.prepareStatement(statementMeisai3);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (param._isKumamoto) {
                    final String[][] checks = {
                            {"CHECK_SHIRYOKU", "視力検査"},
                            {"CHECK_GANKA",    "眼科検診"},
                            {"CHECK_CHORYOKU", "聴力検査"},
                            {"CHECK_JIBIKA",   "耳鼻科検診"},
                            {"CHECK_NAKIKA",   "内科検診"},
                            {"KOUBU_RENNTOGEN_SATSUEI",         "Ｘ線"},
                            {"CHECK_HEART",    "心電図検査"},
                            {"CHECK_NYO",      "尿検査"},
                            {"SHIKA_KENSHIN",         "歯科検診"},
                    };
                    for (int i = 0; i < checks.length; i++) {

                        if (null == rs.getString(checks[i][0])) {
                            continue;
                        }
                        log.debug(" " + rs.getString("SCHREGNO") + " " + checks[i][1]);

                        svf.VrsOut("SCHOOLNAME"     , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME2));
                        svf.VrsOut("post"           , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_JOBNAME));
                        svf.VrsOut("STAFFNAME1"     , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_NAME));

                        svf.VrsOut("HR_NAME1"   ,  rs.getString("HR_NAME"));
                        svf.VrsOut("HR_NAME2"   ,  rs.getString("HR_NAME"));
                        svf.VrsOut("ATTENDNO1"  ,  rs.getString("ATTENDNO"));
                        svf.VrsOut("ATTENDNO2"  ,  rs.getString("ATTENDNO"));
                        svf.VrsOut("NAME1" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "_2" : ""),  rs.getString("NAME"));
                        svf.VrsOut("NAME2" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "_2" : ""),  rs.getString("NAME"));

                        printTitle(svf, text, title);

                        //printTitle(svf, text, title);
                        svf.VrsOut("UNTEST1"  ,  checks[i][1]);

                        putGengou1(db2, svf, "ERA_NAME", param);

                        svf.VrEndRecord();
                        nonedata = true;
                    }

                } else {

                    final List mijukenItemList = new ArrayList();
                    log.info(" param._useMijukenDefault = " + param._useMijukenDefault);
                    if (param._useMijukenDefault) {
                        mijukenItemList.add("NYOU_KENSA");
                        mijukenItemList.add("HINKETSU_KENSA");
                        mijukenItemList.add("NAIKA_KOUI_KENSHIN");
                        mijukenItemList.add("SHIKA_KENSHIN");
                        mijukenItemList.add("KOUBU_RENNTOGEN_SATSUEI");
                        mijukenItemList.add("SHINDENZU_KENSA");
                    } else {
                        // 指定された項目のみで対象かチェックする
                        if (null != param._mijukenItem01) { mijukenItemList.add("NYOU_KENSA"); }
                        if (null != param._mijukenItem02) { mijukenItemList.add("HINKETSU_KENSA"); }
                        if (null != param._mijukenItem03) { mijukenItemList.add("NAIKA_KOUI_KENSHIN"); }
                        if (null != param._mijukenItem04) { mijukenItemList.add("SHIKA_KENSHIN"); }
                        if (null != param._mijukenItem05) { mijukenItemList.add("KOUBU_RENNTOGEN_SATSUEI"); }
                        if (null != param._mijukenItem06) { mijukenItemList.add("SHINDENZU_KENSA"); }
                    }

                    List studentCheck = new ArrayList();
                    for (int i = 0; i < mijukenItemList.size(); i++) {
                        final String item = (String) mijukenItemList.get(i);
                        if (rs.getString(item) != null) {
                            studentCheck.add(item);
                        }
                    }
                    if (studentCheck.isEmpty()) {
                        // 1個も引っかからなかった生徒は対象外
                        continue;
                    }
                    log.info(" schregno = " + rs.getString("SCHREGNO") + ", mijuken check = " + studentCheck);

                    svf.VrsOut("SCHOOLNAME"     , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME2));
                    svf.VrsOut("DATE"           , KNJ_EditDate.h_format_JP_MD(param._date));
                    svf.VrsOut("WEEK"           , "( " + KNJ_EditDate.h_format_W(param._date) + " )");
                    printTitle(svf, text, title);

                    svf.VrsOut("HR_NAME1"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO1"  ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME1" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "_2" : ""),  rs.getString("NAME"));
                    svf.VrsOut("HR_NAME2"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO2"  ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME2" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "_2" : ""),  rs.getString("NAME"));

                    final DecimalFormat df = new DecimalFormat("00");
                    final String[] zenkakuSuuji = {"\uFF10", "\uFF11", "\uFF12", "\uFF13", "\uFF14", "\uFF15", "\uFF16", "\uFF17", "\uFF18", "\uFF19"};
                    for (int i = 0; i < mijukenItemList.size(); i++) {
                        final String item = (String) mijukenItemList.get(i);
                        final String itemTitle;
                        if ("NYOU_KENSA".equals(item)) {
                            itemTitle = zenkakuSuuji[(i + 1) % 10] + "．尿検査（蛋白、糖、潜血）";
                        } else if ("HINKETSU_KENSA".equals(item)) {
                            itemTitle = zenkakuSuuji[(i + 1) % 10] + "．貧血検査";
                        } else if ("NAIKA_KOUI_KENSHIN".equals(item)) {
                            itemTitle = zenkakuSuuji[(i + 1) % 10] + "．内科（校医）検診";
                        } else if ("SHIKA_KENSHIN".equals(item)) {
                            itemTitle = zenkakuSuuji[(i + 1) % 10] + "．歯科検診";
                        } else if ("KOUBU_RENNTOGEN_SATSUEI".equals(item)) {
                            itemTitle = zenkakuSuuji[(i + 1) % 10] + "．胸部レントゲン撮影";
                        } else if ("SHINDENZU_KENSA".equals(item)) {
                            itemTitle = zenkakuSuuji[(i + 1) % 10] + "．心電図検査";
                        } else {
                            continue;
                        }

                        svf.VrsOut("MIJUKEN_ITEM" + df.format(i + 1), itemTitle);
                        svf.VrsOut("CHECK" + String.valueOf(i + 1),  rs.getString(item));
                    }

                    putGengou1(db2, svf, "ERA_NAME", param);

                    svf.VrEndRecord();
                    nonedata = true;
                }
            }
        } catch (Exception ex) {
            log.warn("printMain3 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }


    /**４）眼科検診のお知らせ*/
    private boolean printMain4(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String text = getDocumentMst(db2, param, "02", "TEXT");
            final String title = getDocumentMst(db2, param, "02", "TITLE");

            final String form = param._isKumamoto ? "KNJF030_4KUMA.frm" : "KNJF030_4.frm";
            setForm(param, svf, form, 4);
            String sql = statementMeisai(param, check_no4);
            //log.debug("printMain4 sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("schoolname1"    , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME1));
                svf.VrsOut("schoolname2"    , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME2));
                svf.VrsOut("post"           , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_JOBNAME));
                svf.VrsOut("staff1"         , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_NAME));
                svf.VrsOut("DATE"           , KNJ_EditDate.h_format_JP_MD(param._date));
                svf.VrsOut("WEEK"           , "( " + KNJ_EditDate.h_format_W(param._date) + " )");

                printTitle(svf, text, title);

                svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO"  ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "2" : ""),  rs.getString("NAME"));
                if (isNumber(rs.getString("R_BAREVISION")) || isNumber(rs.getString("L_BAREVISION")) ||
                        isNumber(rs.getString("R_VISION")) || isNumber(rs.getString("L_VISION"))) {
                    svf.VrsOut("SIGHT_R1"   ,  rs.getString("R_BAREVISION"));
                    svf.VrsOut("SIGHT_R2"   ,  rs.getString("R_VISION"));
                    svf.VrsOut("SIGHT_L1"   ,  rs.getString("L_BAREVISION"));
                    svf.VrsOut("SIGHT_L2"   ,  rs.getString("L_VISION"));
                } else {
                    svf.VrsOut("SIGHT_R1"   ,  rs.getString("R_BAREVISION_MARK"));
                    svf.VrsOut("SIGHT_R2"   ,  rs.getString("R_VISION_MARK"));
                    svf.VrsOut("SIGHT_L1"   ,  rs.getString("L_BAREVISION_MARK"));
                    svf.VrsOut("SIGHT_L2"   ,  rs.getString("L_VISION_MARK"));
                }
                svf.VrsOut("DISEASE"   ,  rs.getString("EYEDISEASECD"));

                putGengou1(db2, svf, "ERA_NAME", param);

                svf.VrEndRecord();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.warn("printMain4 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }

    /**５）検診結果のお知らせ（歯・口腔）*/
    private boolean printMain5Meiji(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = statementMeijiResult(param);
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {

                int youkansatu = 0; // COの数
                int youchuuiNyuushi = 0; // COの乳歯の数
//                final String F550_x = "05";
                final String F550_CO = "06";
                final String[] toothField = {
                        "UP_R_BABY5", "UP_R_BABY4", "UP_R_BABY3", "UP_R_BABY2", "UP_R_BABY1",
                        "UP_L_BABY1", "UP_L_BABY2", "UP_L_BABY3", "UP_L_BABY4", "UP_L_BABY5",
                        "LW_R_BABY5", "LW_R_BABY4", "LW_R_BABY3", "LW_R_BABY2", "LW_R_BABY1",
                        "LW_L_BABY1", "LW_L_BABY2", "LW_L_BABY3", "LW_L_BABY4", "LW_L_BABY5",
                        "UP_R_ADULT8", "UP_R_ADULT7", "UP_R_ADULT6", "UP_R_ADULT5", "UP_R_ADULT4", "UP_R_ADULT3", "UP_R_ADULT2", "UP_R_ADULT1",
                        "UP_L_ADULT1", "UP_L_ADULT2", "UP_L_ADULT3", "UP_L_ADULT4", "UP_L_ADULT5", "UP_L_ADULT6", "UP_L_ADULT7", "UP_L_ADULT8",
                        "LW_R_ADULT8", "LW_R_ADULT7", "LW_R_ADULT6", "LW_R_ADULT5", "LW_R_ADULT4", "LW_R_ADULT3", "LW_R_ADULT2", "LW_R_ADULT1",
                        "LW_L_ADULT1", "LW_L_ADULT2", "LW_L_ADULT3", "LW_L_ADULT4", "LW_L_ADULT5", "LW_L_ADULT6", "LW_L_ADULT7", "LW_L_ADULT8"};
                for (int i = 0; i < toothField.length; i++) {
                    if (F550_CO.equals(rs.getString(toothField[i]))) {
                        youkansatu += 1;
                    }
//                    if (F550_x.equals(rs.getString(toothField[i]))) {
//                        youchuuiNyuushi += 1;
//                    }
                }
//                youkansatu = Integer.parseInt(StringUtils.defaultString(rs.getString("BRACK_ADULTTOOTH"), "0"));
                youchuuiNyuushi = Integer.parseInt(StringUtils.defaultString(rs.getString("BRACK_BABYTOOTH"), "0"));

                setForm(param, svf, "KNJF030_5_2.frm", 1);

                svf.VrsOut("TITLE", "歯科検診結果のお知らせ");
                svf.VrsOut("DATE", formatDate(db2, param, param._date5));
                svf.VrsOut("SCHOOLNAME1", param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME1));
                svf.VrsOut("TEACHER_NAME", (String) param._yearRemark6Staffname.get(param._year));
                svf.VrsOut("HR_NAME",  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO",  NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO"));
                final int namelen = getMS932ByteLength(rs.getString("NAME"));
                svf.VrsOut("NAME" + (namelen > 34 ? "3" : namelen > 20 ? "2" : ""), rs.getString("NAME"));

                final int mishochiNyushi = Integer.parseInt(StringUtils.defaultString(rs.getString("REMAINBABYTOOTH"), "0"));
                final int mishochiEikyushi = Integer.parseInt(StringUtils.defaultString(rs.getString("REMAINADULTTOOTH"), "0"));
                final boolean notKyosei = "0".equals(rs.getString("ORTHODONTICS"));

                final boolean a2 = youkansatu > 0;
                final boolean a3 = "02".equals(rs.getString("GUMCD"));
                final boolean a4 = "02".equals(rs.getString("PLAQUECD")) || "03".equals(rs.getString("PLAQUECD"));
                final boolean a5 = "02".equals(rs.getString("JAWS_JOINTCD2"));
                final boolean a6 = "02".equals(rs.getString("JAWS_JOINTCD")) && notKyosei;
                final boolean a7 = "99".equals(rs.getString("OTHERDISEASECD"));
                final boolean b1_2 = mishochiEikyushi > 0;
                final boolean b1_3 = mishochiNyushi > 0;
                final boolean b2 = youchuuiNyuushi > 0;
                final boolean b3 = "03".equals(rs.getString("GUMCD"));
                final boolean b4 = "02".equals(rs.getString("CALCULUS"));
                final boolean b5 = "03".equals(rs.getString("JAWS_JOINTCD2"));
                final boolean b6 = "03".equals(rs.getString("JAWS_JOINTCD")) && notKyosei;
                final boolean a1 = !(a2 || a3 || a4 || a5 || a6 || a7 || b1_2 || b1_3 || b2 || b3 || b4 || b5 || b6);

                svf.VrsOut("A1", a1 ? "○" : ""); // う歯（むし歯）がありません
                svf.VrsOut("A2", a2 ? "○" : ""); // う歯になる恐れがあります（ＣＯ：要観察歯）
                svf.VrsOut("A3", a3 ? "○" : ""); // 軽い歯肉炎です（ＧＯ：要注意歯肉）
                svf.VrsOut("A4", a4 ? "○" : ""); // 歯の汚れがあります
                svf.VrsOut("A5", a5 ? "○" : ""); // 顎間接に少し問題があるようです
                svf.VrsOut("A6", a6 ? "○" : ""); // 歯並びや咬み合わせに少し問題があるようです
                svf.VrsOut("A7", a7 ? "○" : ""); // その他
                svf.VrsOut("A7OTHER", a7 ? rs.getString("OTHERDISEASE") : "");
                svf.VrsOut("B1", b1_2 || b1_3 ? "○" : ""); // う歯があります（永久歯・乳歯）
                svf.VrsOut("B1_2", b1_2 ? "○" : ""); // う歯・永久歯
                svf.VrsOut("B1_3", b1_3 ? "○" : ""); // う歯・乳歯
                svf.VrsOut("B2", b2 ? "○" : ""); // 要注意乳歯があります
                svf.VrsOut("B3", b3 ? "○" : ""); // 歯肉（歯周）の病気です
                svf.VrsOut("B4", b4 ? "○" : ""); // 歯石の沈着があります
                svf.VrsOut("B5", b5 ? "○" : ""); // 顎間接に問題があります
                svf.VrsOut("B6", b6 ? "○" : ""); // 歯並びや咬み合わせに問題があります
                // svf.VrsOut("B7", (false) ? "○" : "");

                putGengou1(db2, svf, "ERA_NAME", param);

                svf.VrEndPage();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.warn("printMain5 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }

    /**５）検診結果のお知らせ（歯・口腔）*/
    private boolean printMain5(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean nonedata = false;
        try {
            final String text = getDocumentMst(db2, param, "04", "TEXT");
            final String title = getDocumentMst(db2, param, "04", "TITLE");

            final String form = param._isNishiyama ? "KNJF030_5_4.frm" : param._isKumamoto ? "KNJF030_5KUMA.frm" : param._isTokiwa ? "KNJF030_5_3.frm" : "KNJF030_5.frm";
            setForm(param, svf, form, 4);
            final String sql = statementMeisai(param, check_no5);
            log.fatal("printMain5 sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("YMD"            , formatDate(db2, param, param._date5));
                final int schoolnameKeta = getMS932ByteLength(param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME1));
                final String sfx = schoolnameKeta > 36 ? "_3" : schoolnameKeta >= 34 ? "_2" : "";
                svf.VrsOut("schoolname1"    + sfx, param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME1));
                svf.VrsOut("post1"          + sfx, param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_JOBNAME));
                svf.VrsOut("staff1"         + sfx, param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_NAME));
                svf.VrsOut("schoolname2"    + sfx, param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME1));
                svf.VrsOut("post2"          + sfx, param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_JOBNAME));
                svf.VrsOut("staff2"         + sfx, param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_NAME));
                svf.VrsOut("SCHOOLNAME3"         , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME2));

                final String student_SKTypeStr = "K".equals(rs.getString("SCHOOL_KIND")) ? "園児" : "P".equals(rs.getString("SCHOOL_KIND")) ? "児童" : "生徒";
                svf.VrsOut("STUDENT", student_SKTypeStr);

                svf.VrsOut("HR_NAME1"   ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO1"  ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME1" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "_2" : ""),  rs.getString("NAME"));
                svf.VrsOut("HR_NAME2"   ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO2"  ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME2" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "_2" : ""),  rs.getString("NAME"));

                if (param._isKumamoto) {
                    printTitle(svf, text, title);

                    final List results = new ArrayList();
                    final int entnendo = Integer.parseInt(rs.getString("ENT_NENDO"));

                    //2016年度以降入学者の場合、異常2のコードが変わる
                    final String namecd2 = (entnendo >= 2016) ? "03" : "04";

                    if (toInt(rs.getString("BRACK_BABYTOOTH"), 0) > 0) {
                        results.add("要注意乳歯があります。");
                    }
                    if (toInt(rs.getString("MISYOCHI_SU"), 0) > 0) {
                        results.add("う歯（むし歯）　未処置歯があります。");
                    }
                    if (toInt(rs.getString("KANSATSU_SU"), 0) > 0) {
                        results.add("ＣＯ（むし歯になりかけの歯）があります。");
                    }
                    if (namecd2.equals(rs.getString("JAWS_JOINTCD2"))) {
                        results.add("顎関節に異常があります。");
                    }
                    if (entnendo < 2016) {
                        if (namecd2.equals(rs.getString("JAWS_JOINTCD"))) {
                            results.add("歯列の状態に異常があります。");
                        }
                        if (namecd2.equals(rs.getString("JAWS_JOINTCD3"))) {
                            results.add("咬合の状態に異常があります。");
                        }
                    } else {
                        if (namecd2.equals(rs.getString("JAWS_JOINTCD"))) {
                            results.add("歯列・咬合の状態に異常があります。");
                        }
                    }
                    if (namecd2.equals(rs.getString("PLAQUECD"))) {
                        results.add("歯垢が付着しています。");
                    }
                    if (namecd2.equals(rs.getString("GUMCD"))) {
                        results.add("歯肉の炎症があります。");
                    }
                    if (!"01".equals(rs.getString("OTHERDISEASECD"))) {
                        if ("99".equals(rs.getString("OTHERDISEASECD"))) {
                            if (rs.getString("OTHERDISEASE") != null && !rs.getString("OTHERDISEASE").isEmpty()) {
                            results.add(rs.getString("OTHERDISEASE") + "があります。");
                            }
                        } else {
                            if (!"".equals(StringUtils.defaultString(rs.getString("OTHERDISEASECD_NAME1")))) {
                            results.add(rs.getString("OTHERDISEASECD_NAME1") + "があります。");
                            }
                        }
                    }
                    for (int i = 0; i < results.size(); i++) {
                        svf.VrsOut("RESULT" + String.valueOf(i + 1), (String) results.get(i));
                    }

                } else {
                    printTitle(svf, text, title);

                    svf.VrsOut("TEETHLINE"  ,  rs.getString("JAWS_JOINTCD_NAME1"));
                    svf.VrsOut("PLAQUE"     ,  rs.getString("PLAQUECD_NAME1"));
                    svf.VrsOut("GUM"        ,  rs.getString("GUMCD_NAME1"));
                    svf.VrsOut("OTHERS"     ,  rs.getString("OTHERDISEASECD_NAME1"));
                    svf.VrsOut("VIEWS"      ,  rs.getString("DENTISTREMARKCD_NAME1"));
                    svf.VrsOut("TEETHNO"    ,  rs.getString("GENZAI_SU"));
                    svf.VrsOut("DECAYED1"   ,  rs.getString("SYOCHI_SU"));
                    svf.VrsOut("DECAYED2"   ,  rs.getString("MISYOCHI_SU"));
                    svf.VrsOut("DECAYED3"   ,  rs.getString("KANSATSU_SU"));
                    svf.VrsOut("LOST"       ,  rs.getString("SOSHITSU_SU"));
                    //未処置歯=0なら処置完了
                    if (rs.getString("MISYOCHI_SU") != null && (rs.getString("MISYOCHI_SU")).equals("0")) {
                        svf.VrsOut("COMPLETION" ,  "完　了");
                    } else {
                        svf.VrsOut("COMPLETION" ,  "");
                    }
                }

                putGengou1(db2, svf, "ERA_NAME", param);

                svf.VrEndRecord();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.warn("printMain5 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }


    /**６）定期健康診断結果*/
    private boolean printMain6(final DB2UDB db2, final Form form, final Vrw32alp svf, final Param param) throws SQLException {
        final Map physAvgMap;
        if (param._isKumamoto) {
            physAvgMap = new TreeMap();
        } else {
            physAvgMap = HexamPhysicalAvgDat.getHexamPhysicalAvgMap(db2, param);
        }
        final String CD_TAISHOUGAI = "98";
        boolean nonedata = false;
        String formname = (param._isMiyagiken) ? "KNJF030_6_2.frm": (param._isChiyodaKudan) ? "KNJF030_6_3.frm": (param._isKindai) ? "KNJF030_6KIN.frm" : "KNJF030_6.frm";

        form.setForm(formname, 1);

        String sql = statementMeisai(param, check_no6);
        log.debug("printMain6 sql = " + sql);
        final List rowList = KnjDbUtils.query(db2, sql);
        for (final Iterator it = rowList.iterator(); it.hasNext();) {
            final Map rs = (Map) it.next();
            if (param._isKumamoto) {
                formname = ("on".equals(param._familyContactComment)) ? "KNJF030_6_4KUMA.frm": "KNJF030_6_3KUMA.frm";
                if (2016 > Integer.parseInt(param._year)) {
                    formname = ("on".equals(param._familyContactComment)) ? "KNJF030_6_2KUMA.frm": "KNJF030_6KUMA.frm";
                }
                final int minGrade = Integer.parseInt(param._minGradeMap.get(getString("SCHOOL_KIND", rs)));
                final int setGrade = (Integer.parseInt(param._year) > 2022) ? 0: Integer.parseInt(param._year) - 2016 + minGrade;
                if (setGrade > 0 && Integer.parseInt(getString("GRADE", rs)) > setGrade) {
                    formname = ("on".equals(param._familyContactComment)) ? "KNJF030_6_2KUMA.frm": "KNJF030_6KUMA.frm";
                }
                if (!"H".equals(getString("SCHOOL_KIND", rs))) {
                    formname = "KNJF030_6KUMA.frm";
                }
                setForm(param, svf, formname, 1);
                if ("on".equals(param._familyContactComment)) {
                    String text = getDocumentDetailMst(db2, param, "01", "TEXT", param._documentCd);
                    String title = getDocumentDetailMst(db2, param, "01", "TITLE", param._documentCd);
                    if (null == text && null == title) {
                        text = getDocumentMst(db2, param, "01", "TEXT");
                        title = getDocumentMst(db2, param, "01", "TITLE");
                    }

                    printTitle(svf, text, title);
                }
            }

            final String title;
            if (param._isChiyodaKudan) {
                title = KNJ_EditDate.h_format_JP_N(db2, param._ctrlDate) + "度　定期健康診断結果";
            } else {
                title = KNJ_EditDate.h_format_JP_N(db2, param._year + "-04-01") + "度　定期健康診断結果";
            }
            svf.VrsOut("TITLE",  title); // KNJF030_6_2.frmのみ

            final String endStr = (param._isMiyagiken) ? "長": "";
            svf.VrsOut("SCHOOLNAME"     , param.getSchoolInfo(getString("SCHOOL_KIND", rs), SCHOOL_NAME1) + endStr);
            svf.VrsOut("STAFFNAME1"     , param.getSchoolInfo(getString("SCHOOL_KIND", rs), PRINCIPAL_NAME));
            svf.VrsOut("HR_NAME"   ,  getString("HR_NAME", rs));
            svf.VrsOut("ATTENDNO"  ,  getString("ATTENDNO", rs));
            svf.VrsOut("NAME"      ,  getString("NAME", rs));

            if (param._isKumamoto) {
                //svf.VrsOut("STAFFNAME2", null); // 養護教諭名
                svf.VrsOut("BMI"        ,  getString("BMI", rs));
            } else {
                if (param._isMiyagiken) {
                    svf.VrsOut("YMD1"           , KNJ_EditDate.h_format_JP(db2, param._date7));
                } else if (param._isKindai) {
                    svf.VrsOut("YMD1"           , KNJ_EditDate.h_format_JP_M(db2, param._ctrlDate));
                    log.warn("param._ctrlDate:"+param._ctrlDate);
                } else {
                    svf.VrsOut("YMD1"           , param._ctrlDateString);
                }
                svf.VrsOut("DATE"           , param._ctrlDateString);

                //ヘッダ
                svf.VrsOut("YMD2"       , formatDate(db2, param, getString("DATE", rs)));

                try {
                    //肥満度
                    svf.VrsOut("BMI"        , calcHimando(rs, physAvgMap, param));
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }

            //詳細
            svf.VrsOut("HEIGHT"     ,  getString("HEIGHT", rs));
            svf.VrsOut("WEIGHT"     ,  getString("WEIGHT", rs));
            try {
                final BigDecimal weightAvg = getWeightAvgMethod2(rs, physAvgMap, param).setScale(1, BigDecimal.ROUND_HALF_UP);//小数第二位で四捨五入
                svf.VrsOut("STANDARD_WEIGHT" , String.valueOf(weightAvg));
            } catch (Exception e) {
                log.error("exception!", e);
            }
            svf.VrsOut("SITHEIGHT"  ,  getString("SITHEIGHT", rs));
            if (isNumber(getString("R_BAREVISION", rs)) || isNumber(getString("L_BAREVISION", rs)) ||
                    isNumber(getString("R_VISION", rs)) || isNumber(getString("L_VISION", rs))) {
                svf.VrsOut("SIGHT_R1"   ,  getString("R_BAREVISION", rs));
                svf.VrsOut("SIGHT_R2"   ,  getString("R_VISION", rs));
                svf.VrsOut("SIGHT_L1"   ,  getString("L_BAREVISION", rs));
                svf.VrsOut("SIGHT_L2"   ,  getString("L_VISION", rs));
            } else {
                svf.VrsOut("SIGHT_R1"   ,  getString("R_BAREVISION_MARK", rs));
                svf.VrsOut("SIGHT_R2"   ,  getString("R_VISION_MARK", rs));
                svf.VrsOut("SIGHT_L1"   ,  getString("L_BAREVISION_MARK", rs));
                svf.VrsOut("SIGHT_L2"   ,  getString("L_VISION_MARK", rs));
            }

            if (param._isKumamoto) {
                final String rEar = getString("R_EAR", rs);
                final String lEar = getString("L_EAR", rs);
                svf.VrsOut("EYES"       ,  CD_TAISHOUGAI.equals(getString("EYEDISEASECD", rs)) ? "" : getString("EYEDISEASECD_NAME1", rs));
                svf.VrsOut("EYE_TEST_RESULT"  ,  kakko(getString("EYE_TEST_RESULT", rs)));
                svf.VrsOut("HEARING_R2"     ,  "01".equals(rEar) || "05".equals(rEar) ? getString("R_EAR_NAME1", rs): "");//01か05のみ出力
                svf.VrsOut("HEARING_L2"     ,  "01".equals(lEar) || "05".equals(lEar) ? getString("L_EAR_NAME1", rs): "");//01か05のみ出力
                svf.VrsOut("NOSE"       ,  CD_TAISHOUGAI.equals(getString("NOSEDISEASECD", rs)) ? "" : getString("NOSEDISEASECD_NAME1", rs));
                svf.VrsOut("NOSEDISEASECD_REMARK"  , kakko(getString("NOSEDISEASECD_REMARK", rs)));

                //脊柱・胸郭・四肢
                final String spineribCd = getString("SPINERIBCD", rs);
                String spinerib = "";
                if (isSonota(param, db2, "F040", spineribCd)) {
                    spinerib = StringUtils.defaultString(getString("SPINERIBCD_REMARK", rs), "");
                } else {
                    spinerib = getString("SPINERIBCD_NAME1", rs) + (getString("SPINERIBCD_REMARK", rs) != null ? kakko(getString("SPINERIBCD_REMARK", rs)) : "");
                }
                final String spineribIdx = KNJ_EditEdit.getMS932ByteLength(spinerib) > 40 ? "2": "1";
                svf.VrsOut("SPINERIB" + spineribIdx, CD_TAISHOUGAI.equals(spineribCd) ? "" : spinerib);
                //皮膚疾患
                final String skindiseaseCd = getString("SKINDISEASECD", rs);
                String skindisease = "";
                if (isSonota(param, db2, "F070", skindiseaseCd)) {
                    skindisease = StringUtils.defaultString(getString("SKINDISEASECD_REMARK", rs), "");
                } else {
                    skindisease = getString("SKINDISEASECD_NAME1", rs) + (getString("SKINDISEASECD_REMARK", rs) != null ? kakko(getString("SKINDISEASECD_REMARK", rs)) : "");
                }
                final String skindiseaseIdx = KNJ_EditEdit.getMS932ByteLength(skindisease) > 40 ? "2": "1";
                svf.VrsOut("SKINDISEASE" + skindiseaseIdx, CD_TAISHOUGAI.equals(skindiseaseCd) ? "" : skindisease);

                svf.VrsOut("OTHERS1"    ,  getString("OTHERDISEASECD_NAME1", rs));//内科・その他の疾病及び異常
                svf.VrsOut("XRAY", CD_TAISHOUGAI.equals(getString("TB_REMARKCD", rs)) ? "" : getString("TB_REMARKCD_NAME1", rs)); // X線

                svf.VrsOut("INSPECTION" ,  CD_TAISHOUGAI.equals(getString("HEART_MEDEXAM", rs)) ? "" : getString("HEART_MEDEXAM_NAME1", rs));
                svf.VrsOut("UNUSUAL" ,  CD_TAISHOUGAI.equals(getString("HEARTDISEASECD", rs)) ? "" : getString("HEARTDISEASECD_NAME1", rs));

                svf.VrsOut("ALBUMIN1"       ,  CD_TAISHOUGAI.equals(getString("ALBUMINURIA1CD", rs)) ? "" : "02".equals(getString("ALBUMINURIA1CD", rs)) ? "異常なし" : getString("ALBUMINURIA1CD_NAME1", rs));
                svf.VrsOut("SACCHARIDE1"    ,  CD_TAISHOUGAI.equals(getString("URICSUGAR1CD", rs))   ? "" : "02".equals(getString("URICSUGAR1CD", rs))   ? "異常なし" : getString("URICSUGAR1CD_NAME1", rs));
                svf.VrsOut("BLOOD1"         ,  CD_TAISHOUGAI.equals(getString("URICBLEED1CD", rs))   ? "" : "02".equals(getString("URICBLEED1CD", rs))   ? "異常なし" : getString("URICBLEED1CD_NAME1", rs));
                svf.VrsOut("ALBUMIN2"       ,  CD_TAISHOUGAI.equals(getString("ALBUMINURIA2CD", rs)) ? "" : "02".equals(getString("ALBUMINURIA2CD", rs)) ? "異常なし" : getString("ALBUMINURIA2CD_NAME1", rs));
                svf.VrsOut("SACCHARIDE2"    ,  CD_TAISHOUGAI.equals(getString("URICSUGAR2CD", rs))   ? "" : "02".equals(getString("URICSUGAR2CD", rs))   ? "異常なし" : getString("URICSUGAR2CD_NAME1", rs));
                svf.VrsOut("BLOOD2"         ,  CD_TAISHOUGAI.equals(getString("URICBLEED2CD", rs))   ? "" : "02".equals(getString("URICBLEED2CD", rs))   ? "異常なし" : getString("URICBLEED2CD_NAME1", rs));

                final int remainadultTooth = Integer.parseInt(StringUtils.defaultString(getString("REMAINADULTTOOTH", rs), "0"));
                final int brackAdultTooth = Integer.parseInt(StringUtils.defaultString(getString("BRACK_ADULTTOOTH", rs), "0"));
                final String chk = "レ";
                svf.VrsOut("CHECK1", (remainadultTooth + brackAdultTooth) == 0 ? chk : ""); // チェック う歯・C0（むし歯になりかけの歯）なし
                svf.VrsOut("CHECK2", remainadultTooth > 0 ? chk : ""); // チェック う歯（むし歯）があります
                svf.VrsOut("CHECK3", brackAdultTooth > 0 ? chk : ""); // チェック C0（むし歯になりかけの歯）があります
                svf.VrsOut("CHECK4", "02".equals(getString("JAWS_JOINTCD", rs)) ? chk : ""); // チェック 歯列異常１(歯列・咬合異常１)
                svf.VrsOut("CHECK5", "03".equals(getString("JAWS_JOINTCD", rs)) ? chk : ""); // チェック 歯列異常２(歯列・咬合異常２)
                svf.VrsOut("CHECK6", "02".equals(getString("JAWS_JOINTCD3", rs)) ? chk : ""); // チェック 咬合異常１
                svf.VrsOut("CHECK7", "03".equals(getString("JAWS_JOINTCD3", rs)) ? chk : ""); // チェック 咬合異常２
                svf.VrsOut("CHECK8", "02".equals(getString("JAWS_JOINTCD2", rs)) ? chk : ""); // チェック 顎関節異常１
                svf.VrsOut("CHECK9", "03".equals(getString("JAWS_JOINTCD2", rs)) ? chk : ""); // チェック 顎関節異常２
                svf.VrsOut("CHECK10", "02".equals(getString("PLAQUECD", rs)) ? chk : ""); // チェック 歯垢異常１
                svf.VrsOut("CHECK11", "03".equals(getString("PLAQUECD", rs)) ? chk : ""); // チェック 歯垢異常２
                svf.VrsOut("CHECK12", "02".equals(getString("GUMCD", rs)) ? chk : ""); // チェック 歯肉異常１
                svf.VrsOut("CHECK13", "03".equals(getString("GUMCD", rs)) ? chk : ""); // チェック 歯肉異常２

                final String[] message = KNJ_EditEdit.get_token(getString("DET008_REMARK1", rs), 21 * 2, 4);
                if (null != message) {
                    for (int i = 0; i < message.length; i++) {
                        svf.VrsOut("MESSAGE" + String.valueOf(i + 1), message[i]); // メッセージ
                    }
                }
            } else {
                // 目の疾病及び異常
                if (isSonota(param, db2, "F050", getString("EYEDISEASECD", rs))) {
                    svf.VrsOut("EYES", getString("EYE_TEST_RESULT", rs));
                } else {
                    svf.VrsOut("EYES"       ,  getString("EYEDISEASECD_NAME1", rs));
                    svf.VrsOut("EYE_TEST_RESULT"  ,  kakko(getString("EYE_TEST_RESULT", rs)));
                }
                // 聴力
                svf.VrsOut("HEARING_R1"     ,  getString("R_EAR_DB", rs));
                svf.VrsOut("HEARING_R2"     ,  getString("R_EAR_NAME1", rs));
                svf.VrsOut("HEARING_L1"     ,  getString("L_EAR_DB", rs));
                svf.VrsOut("HEARING_L2"     ,  getString("L_EAR_NAME1", rs));
                // 栄養状態  //近大では内科検診
                final String eiyouFieldName = (param._isKindai ? "INTERNAL" : "NUTRITION");
                if (isSonota(param, db2, "F030", getString("NUTRITIONCD", rs))) {
                    svf.VrsOut(eiyouFieldName, getString("NUTRITIONCD_REMARK", rs));
                } else {
                    svf.VrsOut(eiyouFieldName     ,  param.getNameMstValue("F030", getString("NUTRITIONCD", rs), "NAME1"));
                }
                // 脊柱・胸部・四肢  //近大では運動器検診(脊椎・胸郭・四肢)
                if (isSonota(param, db2, "F040", getString("SPINERIBCD", rs))) {
                    svf.VrsOut("SPINERIB", getString("SPINERIBCD_REMARK", rs));
                } else {
                    svf.VrsOut("SPINERIB"     ,  param.getNameMstValue("F040", getString("SPINERIBCD", rs), "NAME1"));
                }
                // 耳鼻咽頭疾患
                if (isSonota(param, db2, "F060", getString("NOSEDISEASECD", rs))) {
                    svf.VrsOut("NOSE", getString("NOSEDISEASECD_REMARK", rs));
                } else {
                    svf.VrsOut("NOSE"       ,  getString("NOSEDISEASECD_NAME1", rs));
                    svf.VrsOut("NOSEDISEASECD_REMARK"  , kakko(getString("NOSEDISEASECD_REMARK", rs)));
                }
                // 皮膚疾患
                if (isSonota(param, db2, "F070", getString("SKINDISEASECD", rs))) {
                    svf.VrsOut("SKIN", getString("SKINDISEASECD_REMARK", rs));
                } else {
                    svf.VrsOut("SKIN"       ,  getString("SKINDISEASECD_NAME1", rs));
                }
                // 結核 病名 指導区分
                if ("1".equals(getString("TB_NAMECD_NAMESPARE2", rs))) {
                    svf.VrsOut("DISEASE"    ,  getString("TB_NAME_REMARK1", rs));
                } else {
                    svf.VrsOut("DISEASE"    ,  getString("TB_NAMECD_NAME1", rs));
                }
                svf.VrsOut("GUIDE"      ,  getString("TB_ADVISECD_NAME1", rs));
                // 心臓 臨床医学的検査
                if (isSonota(param, db2, "F080", getString("HEART_MEDEXAM", rs)) && !StringUtils.isEmpty(getString("HEART_MEDEXAM_REMARK", rs))) {
                    form.VrsOutGroupForData(new String[][] {{"INSPECTION", "HEARTDISEASECD_REMARK0"}, {"INSPECTION"}, {"INSPECTION2"}}, getString("HEART_MEDEXAM_REMARK", rs));
                } else {
                    form.VrsOutSelectField(new String[] {"INSPECTION", "INSPECTION2"}, getString("HEART_MEDEXAM_NAME1", rs));
                    form.VrsOutSelectField(new String[] {"HEARTDISEASECD_REMARK0", "HEARTDISEASECD_REMARK0_2", "HEARTDISEASECD_REMARK"}, kakko(getString("HEART_MEDEXAM_REMARK", rs)));
                }
                // 心臓 疾病及び異常
                if (isSonota(param, db2, "F090", getString("HEARTDISEASECD", rs)) && !StringUtils.isEmpty(getString("HEARTDISEASECD_REMARK", rs))) {
                    form.VrsOutGroupForData(new String[][] {{"UNUSUAL", "HEARTDISEASE2_0"}, {"UNUSUAL"}, {"UNUSUAL2"}}, getString("HEARTDISEASECD_REMARK", rs));
                } else {
                    form.VrsOutSelectField(new String[] {"UNUSUAL", "UNUSUAL2"}, param.getNameMstValue("F090", getString("HEARTDISEASECD", rs), "NAME1"));
                    form.VrsOutSelectField(new String[] {"HEARTDISEASE2_0", "HEARTDISEASE2_1", "HEARTDISEASE2"}, kakko(getString("HEARTDISEASECD_REMARK", rs)));
                }
                // 尿
                svf.VrsOut("ALBUMIN1"       ,  getString("ALBUMINURIA1CD_NAME1", rs));
                svf.VrsOut("SACCHARIDE1"    ,  getString("URICSUGAR1CD_NAME1", rs));
                svf.VrsOut("BLOOD1"         ,  getString("URICBLEED1CD_NAME1", rs));
                svf.VrsOut("ALBUMIN2"       ,  getString("ALBUMINURIA2CD_NAME1", rs));
                svf.VrsOut("SACCHARIDE2"    ,  getString("URICSUGAR2CD_NAME1", rs));
                svf.VrsOut("BLOOD2"         ,  getString("URICBLEED2CD_NAME1", rs));

                if (param._isMiyagiken) {
                    if ("1".equals(getString("OTHERDISEASECD_NAMESPARE2", rs))) {
                        svf.VrsOut("OTHERS1"    ,  getString("OTHER_REMARK2", rs));
                    } else {
                        svf.VrsOut("OTHERS1"    ,  getString("OTHERDISEASECD_NAME1", rs));
                        svf.VrsOut("OTHER_ADVISECD2"  ,  kakko(getString("OTHER_ADVISECD_NAME1", rs)));
                    }
                } else {
                    if (isSonota(param, db2, "F140", getString("OTHERDISEASECD", rs))) {
                        svf.VrsOut("OTHERS1", getString("OTHER_REMARK2", rs));
                    } else {
                        svf.VrsOut("OTHERS1"    ,  getString("OTHERDISEASECD_NAME1", rs));
                    }
                    svf.VrsOut("OTHER_ADVISECD2"  ,  kakko(getString("OTHER_ADVISECD_NAME1", rs)));
                }
                String views21 = StringUtils.defaultString(param.getNameMstValue("F144", getString("DOC_CD", rs), "NAME1"));
                final String docRemark = getString("DOC_REMARK", rs);
                if (null != views21 && null != docRemark) {
                    views21 += " ";
                }
                views21 += StringUtils.defaultString(docRemark);
                svf.VrsOut("VIEWS2_1"    ,   views21);
                svf.VrsOut("NOTE1"      ,  getString("REMARK", rs));

                //歯科検診(近大用)
                if (param._isKindai) {
                    final int remainadultTooth = Integer.parseInt(StringUtils.defaultString(getString("REMAINADULTTOOTH", rs), "0"));
                    final int remainbabyTooth = Integer.parseInt(StringUtils.defaultString(getString("REMAINBABYTOOTH", rs), "0"));
                    ////う歯(むし歯)
                    svf.VrsOut("DECAY", ((remainadultTooth > 0 || remainbabyTooth > 0) ? "あり" : "なし"));
                    ////歯肉の状態
                    svf.VrsOut("GUM", StringUtils.defaultString(param.getNameMstValue((Integer.parseInt(param._year)>= 2016 ? "F517" : "F513"), getString("GUMCD", rs), "NAME1")));
                    ////歯垢の状態
                    svf.VrsOut("PLAQUE", StringUtils.defaultString(param.getNameMstValue((Integer.parseInt(param._year)>= 2016 ? "F516" : "F520"), getString("PLAQUECD", rs), "NAME1")));
                    ////顎関節
                    String JJStr1 = "異常なし";
                    if (!"01".equals(StringUtils.defaultString(getString("JAWS_JOINTCD", rs)))) {
                        JJStr1 = param.getNameMstValue("F510", getString("JAWS_JOINTCD", rs), "NAME1");
                    }
                    svf.VrsOut("JAWS", StringUtils.defaultString(JJStr1));
                    ////歯列・咬合
                    String JJStr2 = "異常なし";
                    if (!"01".equals(StringUtils.defaultString(getString("JAWS_JOINTCD2", rs)))) {
                        JJStr2 = param.getNameMstValue("F510", getString("JAWS_JOINTCD", rs), "NAME1");
                    }
                    svf.VrsOut("JAWS_JOINT", StringUtils.defaultString(JJStr1));
                    ////★その他疾病及び異常
                    if (!"".equals(getString("TOOTH_OTHERDISEASECD", rs))) {
                        if (isSonota(param, db2, "F530", getString("TOOTH_OTHERDISEASECD", rs))) {
                            svf.VrsOut("TOOTHOTHERDISEASE"    ,  StringUtils.defaultString(param.getNameMstValue("F530", getString("TOOTH_OTHERDISEASECD", rs), "NAME1")) + " " + StringUtils.defaultString(getString("TOOTH_OTHERDISEASE", rs)));
                        } else {
                            svf.VrsOut("TOOTHOTHERDISEASE"    ,  StringUtils.defaultString(getString("TOOTH_OTHERDISEASECD_NAME1", rs)));
                        }
                    }
                }
                if (param._isChiyodaKudan) {
                    final int remainadultTooth = Integer.parseInt(StringUtils.defaultString(getString("REMAINADULTTOOTH", rs), "0"));
                    final int brackAdultTooth = Integer.parseInt(StringUtils.defaultString(getString("BRACK_ADULTTOOTH", rs), "0"));
                    final String chk = "レ";
                    svf.VrsOut("CHECK1", (remainadultTooth + brackAdultTooth) == 0 ? chk : ""); // チェック う歯・C0（むし歯になりかけの歯）なし
                    svf.VrsOut("CHECK2", remainadultTooth > 0 ? chk : ""); // チェック う歯（むし歯）があります
                    svf.VrsOut("CHECK3", brackAdultTooth > 0 ? chk : ""); // チェック C0（むし歯になりかけの歯）があります
                    svf.VrsOut("CHECK4", "02".equals(getString("JAWS_JOINTCD", rs)) ? chk : ""); // チェック 歯列異常１(歯列・咬合異常１)
                    svf.VrsOut("CHECK5", "03".equals(getString("JAWS_JOINTCD", rs)) ? chk : ""); // チェック 歯列異常２(歯列・咬合異常２)
                    svf.VrsOut("CHECK6", "02".equals(getString("JAWS_JOINTCD3", rs)) ? chk : ""); // チェック 咬合異常１
                    svf.VrsOut("CHECK7", "03".equals(getString("JAWS_JOINTCD3", rs)) ? chk : ""); // チェック 咬合異常２
                    svf.VrsOut("CHECK8", "02".equals(getString("JAWS_JOINTCD2", rs)) ? chk : ""); // チェック 顎関節異常１
                    svf.VrsOut("CHECK9", "03".equals(getString("JAWS_JOINTCD2", rs)) ? chk : ""); // チェック 顎関節異常２
                    svf.VrsOut("CHECK10", "02".equals(getString("PLAQUECD", rs)) ? chk : ""); // チェック 歯垢異常１
                    svf.VrsOut("CHECK11", "03".equals(getString("PLAQUECD", rs)) ? chk : ""); // チェック 歯垢異常２
                    svf.VrsOut("CHECK12", "02".equals(getString("GUMCD", rs)) ? chk : ""); // チェック 歯肉異常１
                    svf.VrsOut("CHECK13", "03".equals(getString("GUMCD", rs)) ? chk : ""); // チェック 歯肉異常２
                }
            }

            svf.VrEndPage();
            nonedata = true;
        }
        return nonedata;
    }

    /**６）定期健康診断結果のお知らせ*/
    private boolean printMain6_Pattern1(final DB2UDB db2, final Form form, final Vrw32alp svf, final Param param) throws SQLException {
        final Map physAvgMap;
        if (param._isKumamoto) {
            physAvgMap = new TreeMap();
        } else {
            physAvgMap = HexamPhysicalAvgDat.getHexamPhysicalAvgMap(db2, param);
        }
        final String CD_TAISHOUGAI = "98";
        boolean nonedata = false;
        final String formname = ("".equals(StringUtils.defaultString(param._standard_notshow, ""))) ? "KNJF030_6_5_2.frm": "KNJF030_6_5_1.frm";

        form.setForm(formname, 1);

        String sql = statementMeisai(param, check_no6);
        log.debug("printMain6_Pattern1 sql = " + sql);
        final List rowList = KnjDbUtils.query(db2, sql);
        for (final Iterator it = rowList.iterator(); it.hasNext();) {
            final Map rs = (Map) it.next();
            if (param._isKumamoto) {
                if ("on".equals(param._familyContactComment)) {
                    String text = getDocumentDetailMst(db2, param, "01", "TEXT", param._documentCd);
                    String title = getDocumentDetailMst(db2, param, "01", "TITLE", param._documentCd);
                    if (null == text && null == title) {
                        text = getDocumentMst(db2, param, "01", "TEXT");
                        title = getDocumentMst(db2, param, "01", "TITLE");
                    }

                    printTitle(svf, text, title);
                }
            }

            final String endStr = (param._isMiyagiken) ? "長": "";
            svf.VrsOut("schoolname1"     , param.getSchoolInfo(getString("SCHOOL_KIND", rs), SCHOOL_NAME1) + endStr);
            svf.VrsOut("staff1"     , param.getSchoolInfo(getString("SCHOOL_KIND", rs), PRINCIPAL_NAME));
            svf.VrsOut("post1"     , param.getSchoolInfo(getString("SCHOOL_KIND", rs), PRINCIPAL_JOBNAME));
            svf.VrsOut("HR_NAME"   ,  getString("HR_NAME", rs));
            svf.VrsOut("ATTENDNO"  ,  getString("ATTENDNO", rs));
            svf.VrsOut("NAME"      ,  getString("NAME", rs));

            //作成日
            svf.VrsOut("DATE", formatDate(db2, param, getString("DATE", rs)));

            svf.VrsOut("HEIGHT",  getString("HEIGHT", rs)); //身長
            svf.VrsOut("WEIGHT",  getString("WEIGHT", rs)); //体重

            //「標準体重・肥満度を出さない」がチェックOFFの場合
            if ("".equals(StringUtils.defaultString(param._standard_notshow, ""))) {
                try {
                    //肥満度（過体重度）= 100 × (測定された体重 - 標準体重) / 標準体重
                    if (null != getString("WEIGHT", rs)) {
                        BigDecimal weightAvg = null;
                        final boolean isUseMethod2 = true;
                        if (isUseMethod2) {
                            final BigDecimal weightCalOrg = getWeightAvgMethod2(rs, physAvgMap, param);
                            if (weightCalOrg != null) {
                                final BigDecimal weightAvg2 = weightCalOrg.setScale(1, BigDecimal.ROUND_HALF_UP);//小数第二位で四捨五入
                                log.fatal(" (schregno, attendno, weight2) = (" + getString("SCHREGNO", rs) + ", " + getString("ATTENDNO", rs) + ", " + weightAvg2 + ")");
                                weightAvg = weightAvg2;
                            }
                        }
                        if (null != weightAvg) {
                            final BigDecimal himando = new BigDecimal(100).multiply(new BigDecimal(Double.parseDouble(getString("WEIGHT", rs))).subtract(weightAvg)).divide(weightAvg, 1, BigDecimal.ROUND_HALF_UP);
                            log.fatal(" himando = 100 * (" + getString("WEIGHT", rs) + " - " + weightAvg + ") / " + weightAvg + " = " + himando);
                            svf.VrsOut("STANDARD_WEIGHT", String.valueOf(weightAvg)); //標準体重
                            svf.VrsOut("FAT", himando.toString()); //肥満度
                        }
                    }

                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }

            // 視力
            svf.VrsOut("SIGHT_R1"   ,  getString("R_BAREVISION_MARK", rs)); //視力・右
            svf.VrsOut("SIGHT_R2"   ,  getString("R_VISION_MARK", rs)); //視力・右・矯正
            svf.VrsOut("SIGHT_L1"   ,  getString("L_BAREVISION_MARK", rs)); //視力・左
            svf.VrsOut("SIGHT_L2"   ,  getString("L_VISION_MARK", rs)); //視力・左・矯正

            // 聴力
            final String cantMeasure = "測定困難";
            if ("1".equals(getString("R_EAR_CANTMEASURE", rs))) {
                svf.VrsOut("HEARING_R2_1", cantMeasure); // 測定困難
                svf.VrsOut("HEARING_R2_2", cantMeasure); // 測定困難
            } else {
                if (!"".equals(StringUtils.defaultString(getString("R_EAR_DB1_NAME", rs), ""))) {
                    final String gStr = StringUtils.defaultString(getString("R_EAR_DB1_NAME", rs), "");
                    svf.VrsOut("HEARING_R2_1", !"".equals(gStr) ? gStr : "  ");
                } else {
                    svf.VrsOut("HEARING_R2_1",  "  ");
                }
                if (!"".equals(StringUtils.defaultString(getString("R_EAR_DB4_NAME", rs), ""))) {
                    final String gStr = StringUtils.defaultString(getString("R_EAR_DB4_NAME", rs), "");
                    svf.VrsOut("HEARING_R2_2", !"".equals(gStr) ? gStr : "  ");
                } else {
                    svf.VrsOut("HEARING_R2_2",  "  ");
                }
            }
            if ("1".equals(getString("L_EAR_CANTMEASURE", rs))) {
                svf.VrsOut("HEARING_L2_1", cantMeasure); // 測定困難
                svf.VrsOut("HEARING_L2_2", cantMeasure); // 測定困難
            } else {
                if (!"".equals(StringUtils.defaultString(getString("L_EAR_DB1_NAME", rs), ""))) {
                    final String gStr = StringUtils.defaultString(getString("L_EAR_DB1_NAME", rs), "");
                    svf.VrsOut("HEARING_L2_1", !"".equals(gStr) ? gStr : "  ");
                } else {
                    svf.VrsOut("HEARING_L2_1",  "  ");
                }
                if (!"".equals(StringUtils.defaultString(getString("L_EAR_DB4_NAME", rs), ""))) {
                    final String gStr = StringUtils.defaultString(getString("L_EAR_DB4_NAME", rs), "");
                    svf.VrsOut("HEARING_L2_2", !"".equals(gStr) ? gStr : "  ");
                } else {
                    svf.VrsOut("HEARING_L2_2",  "  ");
                }
            }

            // 栄養状態
            final String nutritionCd = getString("NUTRITIONCD", rs);
            String nutrition = "";
            if (isSonota(param, db2, "F030", nutritionCd)) {
                nutrition = getString("NUTRITIONCD_REMARK", rs);
            } else {
                nutrition = param.getNameMstValue("F030", getString("NUTRITIONCD", rs), "NAME1");
            }
            svf.VrsOut("NUTRITION", nutrition);

            // 脊柱・胸部・四肢
            final String spineribCd = getString("SPINERIBCD", rs);
            String spinerib = "";
            if (isSonota(param, db2, "F040", spineribCd)) {
                spinerib = StringUtils.defaultString(getString("SPINERIBCD_REMARK", rs), "");
            } else {
                spinerib = StringUtils.defaultString(param.getNameMstValue("F040", getString("SPINERIBCD", rs), "NAME1"), "");
                if (param._isKumamoto) spinerib = StringUtils.defaultString(getString("SPINERIBCD_NAME1", rs), "") + (getString("SPINERIBCD_REMARK", rs) != null ? kakko(getString("SPINERIBCD_REMARK", rs)) : "");
            }
            if (param._isKumamoto) spinerib = CD_TAISHOUGAI.equals(spineribCd) ? "" : spinerib;
            final String spineribField = KNJ_EditEdit.getMS932ByteLength(spinerib) > 40 ? "2" : "1";
            svf.VrsOut("SPINERIB" + spineribField, spinerib);

            // 皮膚疾患
            final String skindiseaseCd = getString("SKINDISEASECD", rs);
            String skindisease = "";
            if (isSonota(param, db2, "F070", skindiseaseCd)) {
                skindisease = StringUtils.defaultString(getString("SKINDISEASECD_REMARK", rs), "");
            } else {
                skindisease = StringUtils.defaultString(getString("SKINDISEASECD_NAME1", rs), "");
                if (param._isKumamoto) skindisease = StringUtils.defaultString(getString("SKINDISEASECD_NAME1", rs), "") + (getString("SKINDISEASECD_REMARK", rs) != null ? kakko(getString("SKINDISEASECD_REMARK", rs)) : "");
            }
            if (param._isKumamoto) skindisease = CD_TAISHOUGAI.equals(skindiseaseCd) ? "" : skindisease;
            final String skindiseaseField = KNJ_EditEdit.getMS932ByteLength(skindisease) > 40 ? "2" : "1";
            svf.VrsOut("SKINDISEASE" + skindiseaseField, skindisease);

            // その他の疾病及び異常
            final String othersCd = getString("OTHERDISEASECD", rs);
            String others = "";
            if (isSonota(param, db2, "F140", othersCd)) {
                others = StringUtils.defaultString(getString("OTHER_REMARK2", rs), "");
            } else {
                others = StringUtils.defaultString(getString("OTHERDISEASECD_NAME1", rs), "");
            }
            final String otherAdCdNameWk = kakko(getString("OTHER_ADVISECD_NAME1", rs));
            others = StringUtils.defaultString(others, "") + (otherAdCdNameWk == null ? "" : "　" + otherAdCdNameWk);
            //others = ("".equals(others)) ? kakko(getString("OTHER_ADVISECD_NAME1", rs)) : others + "　" + kakko(getString("OTHER_ADVISECD_NAME1", rs));
            final String othersField = KNJ_EditEdit.getMS932ByteLength(others) > 40 ? "2" : "1";
            svf.VrsOut("OTHERS" + othersField, others);

            // 尿
            svf.VrsOut("ALBUMIN1"       ,  getString("ALBUMINURIA1CD_NAME1", rs)); //1次 蛋白
            svf.VrsOut("SACCHARIDE1"    ,  getString("URICSUGAR1CD_NAME1", rs)); //1次 糖
            svf.VrsOut("BLOOD1"         ,  getString("URICBLEED1CD_NAME1", rs)); //1次 潜血
            svf.VrsOut("PH1"            ,  getString("DET017_REMARK1", rs)); //1次 PH
            svf.VrsOut("ALBUMIN2"       ,  getString("ALBUMINURIA2CD_NAME1", rs)); //2次 蛋白
            svf.VrsOut("SACCHARIDE2"    ,  getString("URICSUGAR2CD_NAME1", rs)); //2次 糖
            svf.VrsOut("BLOOD2"         ,  getString("URICBLEED2CD_NAME1", rs)); //2次 潜血
            svf.VrsOut("PH2"            ,  getString("DET017_REMARK2", rs)); //2次 PH

            ////歯科検診
            final int remainadultTooth = Integer.parseInt(StringUtils.defaultString(getString("REMAINADULTTOOTH", rs), "0")); //う歯（虫歯）永久歯
            final int treatedAdultTooth = Integer.parseInt(StringUtils.defaultString(getString("TREATEDADULTTOOTH", rs), "0")); //う歯（虫歯）永久歯

            final int remainbabyTooth = Integer.parseInt(StringUtils.defaultString(getString("REMAINBABYTOOTH", rs), "0")); //う歯（虫歯）乳歯
            final int treatedBabyTooth = Integer.parseInt(StringUtils.defaultString(getString("TREATEDBABYTOOTH", rs), "0")); //う歯（虫歯）永久歯
            svf.VrsOut("DECAYEDADULTTOOTH", String.valueOf(remainadultTooth + treatedAdultTooth)); //う歯（虫歯）永久歯
            svf.VrsOut("DECAYEDBABYTOOTH", String.valueOf(remainbabyTooth + treatedBabyTooth)); //う歯（虫歯）乳歯
            final int brackAdultTooth = Integer.parseInt(StringUtils.defaultString(getString("BRACK_ADULTTOOTH", rs), "0")); //要観察歯
            final int brackBabyTooth = Integer.parseInt(StringUtils.defaultString(getString("BRACK_BABYTOOTH", rs), "0")); //要注意乳歯
            svf.VrsOut("BRACKADULTTOOTH", String.valueOf(brackAdultTooth)); //要観察歯 永久歯
            svf.VrsOut("BRACKBABYTOOTH", String.valueOf(brackBabyTooth)); //要観察歯 乳歯
            svf.VrsOut("CAREFULBABYTOOTH", String.valueOf(brackBabyTooth)); //要注意乳歯

            ////顎関節
            String JJStr2 = "異常なし";
            if (!"01".equals(StringUtils.defaultString(getString("JAWS_JOINTCD2", rs)))) {
                JJStr2 = param.getNameMstValue("F510", getString("JAWS_JOINTCD2", rs), "NAME1");
            }
            svf.VrsOut("JAWS", StringUtils.defaultString(JJStr2));
            ////歯列
            String JJStr1 = "異常なし";
            if (!"01".equals(StringUtils.defaultString(getString("JAWS_JOINTCD", rs)))) {
                JJStr1 = param.getNameMstValue("F510", getString("JAWS_JOINTCD", rs), "NAME1");
            }
            svf.VrsOut("JAWS_JOINT1", StringUtils.defaultString(JJStr1));
            ////咬合
            String JJStr3 = "異常なし";
            if (!"01".equals(StringUtils.defaultString(getString("JAWS_JOINTCD3", rs)))) {
                JJStr3 = param.getNameMstValue("F510", getString("JAWS_JOINTCD3", rs), "NAME1");
            }
            svf.VrsOut("JAWS_JOINT2", StringUtils.defaultString(JJStr3));

            ////歯垢の状態
            svf.VrsOut("PLAQUE", StringUtils.defaultString(param.getNameMstValue((Integer.parseInt(param._year)>= 2016 ? "F516" : "F520"), getString("PLAQUECD", rs), "NAME1")));
            ////歯肉の状態
            svf.VrsOut("GUM", StringUtils.defaultString(param.getNameMstValue((Integer.parseInt(param._year)>= 2016 ? "F517" : "F513"), getString("GUMCD", rs), "NAME1")));

            svf.VrEndPage();
            nonedata = true;
        }
        return nonedata;
    }


    private String kakko(final String s) {
        if (null == s) {
            return s;
        }
        return "(" + s + ")";
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
            // final BigDecimal weightAvg1 = getWeightAvgMethod1(student, mdnd, param);
            final BigDecimal weightAvg2 = getWeightAvgMethod2(rs, physAvgMap, param).setScale(1, BigDecimal.ROUND_HALF_UP);//小数第二位で四捨五入
            // log.fatal(" (schregno, attendno, weight1, weight2) = (" + getString("SCHREGNO") + ", " + getString("ATTENDNO") + ", " + weightAvg1 + ", " + weightAvg2 + ")");
            log.fatal(" (schregno, attendno, weight2) = (" + getString("SCHREGNO", rs) + ", " + getString("ATTENDNO", rs) + ", " + weightAvg2 + ")");
            weightAvg = weightAvg2;
        } else {
            // weightAvg = null; getWeightAvgMethod0(student, mdnd, physAvgMap);
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
//        final int iNenrei = (int) getNenrei2(rs, param._year, param._year);
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


    /**７）尿検査診断結果のお知らせ */
    private boolean printUrinalysis(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            setForm(param, svf, "KNJF030_7_2.frm", 4);
            ps = db2.prepareStatement(statementMeisai(param, check_no7));
            rs = ps.executeQuery();
            while (rs.next()) {

                //出力
                svf.VrsOut("SCHOOL_NAME"    , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME1));
                svf.VrsOut("SCHOOL_NAME2"   , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME1) + " 長様");
                final String staffName = param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_JOBNAME) + "　" + param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_NAME);
                final String staffField = getMS932ByteLength(staffName) > 30 ? "3" : getMS932ByteLength(staffName) > 26 ? "2" : "1";
                svf.VrsOut("STAFF_NAME" + staffField, staffName);
                putGengou1(db2, svf, "DATE", param);

                svf.VrsOut("TITLE", "尿精密検査のすすめ");
                svf.VrsOut("TEXT1", "本年度の健康診断によって、下記のような判断がありましたのでお知らせします。");
                svf.VrsOut("TEXT2", "早期に専門医の検診・治療を受けられますよう、おすすめいたします。");

                //年組 番 氏名
                String name = "".equals(StringUtils.defaultString(rs.getString("HR_NAME"))) ? "" : StringUtils.defaultString(rs.getString("HR_NAME")) + "　" ;
                name = "".equals(StringUtils.defaultString(rs.getString("ATTENDNO"))) ? name : name + (NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番　" : rs.getString("ATTENDNO")) + "　";
                name = name + StringUtils.defaultString(rs.getString("NAME"));
                final String field = getMS932ByteLength(name) > 60 ? "3" : getMS932ByteLength(name) > 48 ? "2" : "1";
                svf.VrsOut("NAME1_" + field, name);
                svf.VrsOut("NAME2_" + field, name);

                // 尿
                svf.VrsOut("ALBUMIN1"       ,  StringUtils.defaultString(rs.getString("ALBUMINURIA1CD_NAME1"))); //1次 蛋白
                svf.VrsOut("SACCHARIDE1"    ,  StringUtils.defaultString(rs.getString("URICSUGAR1CD_NAME1"))); //1次 糖
                svf.VrsOut("BLOOD1"         ,  StringUtils.defaultString(rs.getString("URICBLEED1CD_NAME1"))); //1次 潜血
                svf.VrsOut("PH1"            ,  StringUtils.defaultString(rs.getString("DET017_REMARK1"))); //1次 PH
                if ("2".equals(param._urinalysis_output)) {
                    //2次検査受信者（1次検査結果含む）
                    svf.VrsOut("ALBUMIN2"       ,  StringUtils.defaultString(rs.getString("ALBUMINURIA2CD_NAME1"))); //2次 蛋白
                    svf.VrsOut("SACCHARIDE2"    ,  StringUtils.defaultString(rs.getString("URICSUGAR2CD_NAME1"))); //2次 糖
                    svf.VrsOut("BLOOD2"         ,  StringUtils.defaultString(rs.getString("URICBLEED2CD_NAME1"))); //2次 潜血
                    svf.VrsOut("PH2"            ,  StringUtils.defaultString(rs.getString("DET017_REMARK2"))); //2次 PH
                }
                svf.VrEndRecord();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.warn("printMain7 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }



    /**８）検診結果のお知らせ（一般）*/
    private boolean printMain7(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String text = getDocumentMst(db2, param, "01", "TEXT");
            final String title = getDocumentMst(db2, param, "01", "TITLE");
            setForm(param, svf, "KNJF030_7.frm", 4);
            ps = db2.prepareStatement(statementMeisai(param, check_no7));
            rs = ps.executeQuery();
            while (rs.next()) {
                String[] output = new String[] {
                        "Naika", //内科
                        "Hifuka", //皮膚科
                        "Ganka", //眼科
                        "Sekichu_Kyokaku", //胸郭・脊柱
                        "Jibika", //耳鼻科
                        "TB_X_RAY", //結核検診（Ｘ線）
                        "MANAGEMENT_REMARK", //心電図検査
                };

                final Map field = new HashMap();
                field.put("Naika", "RESULT1_1");
                field.put("Hifuka", "RESULT4_1");
                field.put("Ganka", "RESULT2_1");
                field.put("Sekichu_Kyokaku", "RESULT5_1");
                field.put("Jibika", "RESULT3_1");
                field.put("TB_X_RAY", "RESULT6");
                field.put("MANAGEMENT_REMARK", "RESULT7");

                final Map nameCd1 = new HashMap();
                nameCd1.put("Naika",           "F030");
                nameCd1.put("Hifuka",          "F070");
                nameCd1.put("Ganka",           "F050");
                nameCd1.put("Sekichu_Kyokaku", "F040");
                nameCd1.put("Jibika",          "F060");

                if ("1".equals(param._output)) {
                    //１人で１枚にまとめて出力
                    final String schoolName = param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME1);
                    if (getMS932ByteLength(schoolName) > 24 && formHasField(svf, "schoolname2")) {
                        svf.VrsOut("schoolname2"    , schoolName);
                    } else {
                        svf.VrsOut("schoolname1"    , schoolName);
                    }
                    svf.VrsOut("post"           , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_JOBNAME));
                    svf.VrsOut("staff1"         , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_NAME));

                    printTitle(svf, text, title);
                    svf.VrsOut("HR_NAME"    ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "2" : ""),  rs.getString("NAME"));
                    for (int i = 0; i < output.length; i++) {
                        if (i < 5 && isSonota(param, db2, (String) nameCd1.get(output[i]), rs.getString(output[i] + "_cd"))) {
                            svf.VrsOut((String) field.get(output[i]), rs.getString(output[i] + "_remark"));
                        } else {
                            if (rs.getString(output[i]) != null) {
                                svf.VrsOut((String) field.get(output[i]),  rs.getString(output[i]));
                            }
                        }
                    }
                    putGengou1(db2, svf, "ERA_NAME", param);

                    svf.VrEndRecord();
                    nonedata = true;
                } else if ("2".equals(param._output)) {

                    for (int i = 0; i < output.length; i++) {
                        if (rs.getString(output[i]) == null) {
                            continue;
                        }
                        //１人で各種類ごとに出力
                        final String schoolName = param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME1);
                        if (getMS932ByteLength(schoolName) > 24 && formHasField(svf, "schoolname2")) {
                            svf.VrsOut("schoolname2"    , schoolName);
                        } else {
                            svf.VrsOut("schoolname1"    , schoolName);
                        }
                        svf.VrsOut("post"           , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_JOBNAME));
                        svf.VrsOut("staff1"         , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_NAME));

                        printTitle(svf, text, title);
                        svf.VrsOut("HR_NAME"    ,  rs.getString("HR_NAME"));
                        svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                        svf.VrsOut("NAME_SHOW" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "2" : ""),  rs.getString("NAME"));

                        if (i < 5 && isSonota(param, db2, (String) nameCd1.get(output[i]), rs.getString(output[i] + "_cd"))) {
                            svf.VrsOut((String) field.get(output[i]), rs.getString(output[i] + "_remark"));
                        } else {
                            svf.VrsOut((String) field.get(output[i]),  rs.getString(output[i]));
                        }
                        putGengou1(db2, svf, "ERA_NAME", param);

                        svf.VrEndRecord();
                        svf.VrsOut((String) field.get(output[i]),  "");
                        nonedata = true;
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("printMain7 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }

    private boolean formHasField(final Vrw32alp svf, final String fieldname) {
        try {
            SvfField fieldSchoolname2 = (SvfField) SvfField.getSvfFormFieldInfoMapGroupByName(svf).get(fieldname);
            if (null != fieldSchoolname2) {
                return true;
            }
        } catch (Exception e) {
            log.error("exception: ", e);
        } catch (Throwable t) {
            log.warn("SvfField not found.");
        }
        return false;
    }

    /**７）検診結果のお知らせ（一般）(熊本) */
    private boolean printMain7Kumamoto(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String text = getDocumentMst(db2, param, "07", "TEXT");
            final String title = getDocumentMst(db2, param, "07", "TITLE");

            setForm(param, svf, "KNJF030_7KUMA.frm", 4);
            ps = db2.prepareStatement(statementMeisai(param, check_no7Kuma));
            rs = ps.executeQuery();

            String ten = "、";
            while (rs.next()) {

                final List result = new ArrayList();
                StringBuffer resstb = null;
                for (int i = 0; i < 4; i++) {
                    final StringBuffer stb = new StringBuffer();
                    String item = null;
                    if (i == 0) { // 内科
                        item = "内科";
                        String c = "";
                        if (toInt(rs.getString("OTHERDISEASECD"), -1) > 2) {
                            stb.append(c).append(rs.getString("OTHERDISEASECD_NAME1"));
                            c = ten;
                        }
                        String[] othrem = {"OTHER_REMARK", "OTHER_REMARK2", "OTHER_REMARK3"};
                        for (int j = 0; j < othrem.length; j++) {
                            if (null != rs.getString(othrem[j])) {
                                stb.append(c).append(rs.getString(othrem[j]));
                                c = ten;
                            }
                        }
                        if (toInt(rs.getString("SPINERIBCD"), -1) > 2) {
                            stb.append(c).append(rs.getString("SPINERIBCD_NAME1"));
                            c = ten;
                        }
                        String[] sprem = {"SPINERIBCD_REMARK"};
                        for (int j = 0; j < sprem.length; j++) {
                            if (null != rs.getString(sprem[j])) {
                                stb.append(c).append(rs.getString(sprem[j]));
                                c = ten;
                            }
                        }
                    } else if (i == 1) { // 皮膚疾患
                        item = "皮膚疾患";
                        String c = "";
                        if (toInt(rs.getString("SKINDISEASECD"), -1) > 2) {
                            stb.append(c).append(rs.getString("SKINDISEASECD_NAME1"));
                            c = ten;
                        }
                        String[] rem2 = {"SKINDISEASECD_REMARK"};
                        for (int j = 0; j < rem2.length; j++) {
                            if (null != rs.getString(rem2[j])) {
                                stb.append(c).append(rs.getString(rem2[j]));
                                c = ten;
                            }
                        }

                    } else if (i == 2) { // 眼科
                        item = "眼科";
                        String c = "";
                        String[] eyediscd = {"EYEDISEASECD", "EYEDISEASECD2", "EYEDISEASECD3", "EYEDISEASECD4"};
                        for (int j = 0; j < eyediscd.length; j++) {
                            if (toInt(rs.getString(eyediscd[j]), -1) > 2) {
                                stb.append(c).append(rs.getString(eyediscd[j] + "_NAME1"));
                                c = ten;
                            }
                        }
                        String[] eyetres = {"EYE_TEST_RESULT", "EYE_TEST_RESULT2", "EYE_TEST_RESULT3"};
                        for (int j = 0; j < eyetres.length; j++) {
                            if (null != rs.getString(eyetres[j])) {
                                stb.append(c).append(rs.getString(eyetres[j]));
                                c = ten;
                            }
                        }

                    } else if (i == 3) { // 耳鼻科
                        item = "耳鼻科";
                        String c = "";
                        String[] nosediscd = {"NOSEDISEASECD", "NOSEDISEASECD2", "NOSEDISEASECD3", "NOSEDISEASECD4"};
                        for (int j = 0; j < nosediscd.length; j++) {
                            if (toInt(rs.getString(nosediscd[j]), -1) > 2) {
                                stb.append(c).append(rs.getString(nosediscd[j] + "_NAME1"));
                                c = ten;
                            }
                        }
                        String[] noserem = {"NOSEDISEASECD_REMARK", "NOSEDISEASECD_REMARK2", "NOSEDISEASECD_REMARK3"};
                        for (int j = 0; j < noserem.length; j++) {
                            if (null != rs.getString(noserem[j])) {
                                stb.append(c).append(rs.getString(noserem[j]));
                                c = ten;
                            }
                        }
                    }
                    if (stb.length() != 0) {
                        stb.insert(0, item + "（");
                        stb.append("）\n");

                        if (null == resstb || !"1".equals(param._output)) {
                            resstb = new StringBuffer();
                            result.add(resstb);
                        }
                        resstb.append(stb);
                    }
                }
                if (result.isEmpty()) {
                    continue;
                }

                for (final Iterator it = result.iterator(); it.hasNext();) {

                    final StringBuffer data = (StringBuffer) it.next();
                    svf.VrsOut("schoolname1"    , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME1));
                    svf.VrsOut("post"           , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_JOBNAME));
                    svf.VrsOut("staff1"         , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_NAME));

                    printTitle(svf, text, title);

                    svf.VrsOut("HR_NAME"    ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "2" : ""),  rs.getString("NAME"));

                    final String[] token = KNJ_EditEdit.get_token(data.toString(), 60, 4);
                    if (null != token) {
                        for (int i = 0; i < token.length; i++) {
                            svf.VrsOut("RESULT" + String.valueOf(i + 1), token[i]);
                        }
                    }

                    putGengou1(db2, svf, "ERA_NAME", param);

                    svf.VrEndRecord();
                    nonedata = true;
                }
            }

        } catch (Exception ex) {
            log.warn("printMain7 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }


    /**９or８）内科検診所見あり生徒の名簿*/
    private boolean printMain8(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String nendo = nendo(db2, param, param._year);
            int count = 0;
            setForm(param, svf, "KNJF030_8.frm", 4);
            ps = db2.prepareStatement(statementMeisai(param, check_no8));
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("NENDO"  , nendo);
                svf.VrsOut("DATE"   , param._ctrlDateString);

                svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO"  ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME_SHOW" + (getMS932ByteLength(rs.getString("NAME")) > 34 ? "3" : getMS932ByteLength(rs.getString("NAME")) > 20 ? "2" : ""),  rs.getString("NAME"));
                //内科
                if (rs.getString("Naika")!=null) {
                    svf.VrsOut("CHECKUP"  ,  rs.getString("Naika"));                //内科検診
                    svf.VrsOut("RESULT"  ,  rs.getString("NUTRITION_RESULT"));      //病院受診結果
                    svf.VrEndRecord();
                    count++;
                }
                if (count==25) {
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    count = 0;
                }
                //皮膚科
                if (rs.getString("Hifuka")!=null) {
                    svf.VrsOut("CHECKUP"  ,  rs.getString("Hifuka"));               //内科検診
                    svf.VrsOut("RESULT"  ,  rs.getString("SKINDISEASE_RESULT"));    //病院受診結果
                    svf.VrEndRecord();
                    count++;
                }
                if (count==25) {
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    count = 0;
                }
                //眼科
                if (rs.getString("Ganka")!=null) {
                    svf.VrsOut("CHECKUP"  ,  rs.getString("Ganka"));                //内科検診
                    svf.VrsOut("RESULT"  ,  rs.getString("EYEDISEASE_RESULT"));     //病院受診結果
                    svf.VrEndRecord();
                    count++;
                }
                if (count==25) {
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    count = 0;
                }
                //胸郭・脊柱
                if (rs.getString("Sekichu_Kyokaku")!=null) {
                    svf.VrsOut("CHECKUP"  ,  rs.getString("Sekichu_Kyokaku"));      //内科検診
                    svf.VrsOut("RESULT"  ,  rs.getString("SPINERIB_RESULT"));       //病院受診結果
                    svf.VrEndRecord();
                    count++;
                }
                if (count==25) {
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    count = 0;
                }
                //耳鼻科
                if (rs.getString("Jibika")!=null) {
                    svf.VrsOut("CHECKUP"  ,  rs.getString("Jibika"));               //内科検診
                    svf.VrsOut("RESULT"  ,  rs.getString("NOSEDISEASE_RESULT"));    //病院受診結果
                    svf.VrEndRecord();
                    count++;
                }
                if (count==25) {
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    count = 0;
                }
                //その他
                if (rs.getString("Sonota")!=null) {
                    svf.VrsOut("CHECKUP"    ,  rs.getString("Sonota"));             //内科検診
                    svf.VrsOut("RESULT"  ,  rs.getString("OTHERDISEASE_RESULT"));   //病院受診結果
                    svf.VrEndRecord();
                    count++;
                }
                if (count==25) {
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    count = 0;
                }
                //心臓検診
                if (rs.getString("Shinzou_Kenshin")!=null) {
                    svf.VrsOut("CHECKUP"    ,  rs.getString("Shinzou_Kenshin"));    //内科検診
                    svf.VrsOut("RESULT"  ,  rs.getString("HEARTDISEASE_RESULT"));   //病院受診結果
                    svf.VrEndRecord();
                    count++;
                }
                if (count==25) {
                    svf.VrsOut("HR_NAME"   ,  rs.getString("HR_NAME"));
                    svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                    svf.VrsOut("NAME_SHOW"  ,  rs.getString("NAME"));
                    count = 0;
                }

                nonedata = true;
            }
        } catch (Exception ex) {
            log.warn("printMain8 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }


    /**１０or９）定期健康診断異常者一覧表*/
    private boolean printMain9(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String nendo = nendo(db2, param, param._year);
            int count = 0;
            setForm(param, svf, "KNJF030_9.frm", 4);
            String sql = statementMeisai(param, check_no9);
            log.debug("printMain9 sql="+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("CHOICE"     , rs.getString("CHOICE"));//一般条件
                svf.VrsOut("CONDITIONS" , rs.getString("CONDITIONS"));//歯・口腔条件
                svf.VrsOut("NENDO"  , nendo);
                svf.VrsOut("YMD"    , param._ctrlDateString);
                count++;
                svf.VrlOut("NUMBER"     ,  count);
                svf.VrsOut("HR_NAME"    ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                svf.VrsOut("SCHOOLNO"   ,  rs.getString("SCHREGNO"));
                svf.VrsOut("NAME" + (getMS932ByteLength(rs.getString("NAME")) > 34 ? "3" : getMS932ByteLength(rs.getString("NAME")) > 20 ? "2" : ""),  rs.getString("NAME"));
                if (isNumber(rs.getString("R_BAREVISION")) || isNumber(rs.getString("L_BAREVISION"))) {
                    svf.VrsOut("SIGHT_R1"   ,  rs.getString("R_BAREVISION"));//右裸眼
                    svf.VrsOut("SIGHT_L1"   ,  rs.getString("L_BAREVISION"));//左裸眼
                } else {
                    svf.VrsOut("SIGHT_R1"   ,  rs.getString("R_BAREVISION_MARK"));//右裸眼
                    svf.VrsOut("SIGHT_L1"   ,  rs.getString("L_BAREVISION_MARK"));//左裸眼
                }
//                  svf.VrsOut("SIGHT_R2"   ,  (rs.getString("R_VISION") != null) ? rs.getString("R_VISION") : "(" + rs.getString("R_VISION") + ")");//右矯正
//              svf.VrsOut("POINT"  ,  ",");//POINT
//                  svf.VrsOut("SIGHT_L2"   ,  (rs.getString("L_VISION") != null) ? rs.getString("L_VISION") : "(" + rs.getString("L_VISION") + ")");//左矯正
                svf.VrsOut("URINE"      ,  rs.getString("URINE"));
                svf.VrsOut("ANEMIA"         ,  rs.getString("Hinketsu"));
                svf.VrsOut("TUBERCULOSIS"   ,  rs.getString("TUBERCULOSIS"));
                svf.VrsOut("HEART"          ,  rs.getString("Ekibyo_Shinzo"));
                svf.VrsOut("HEART"          ,  rs.getString("Kensa_Shinzo"));
                svf.VrsOut("NOURISHMENT"    ,  rs.getString("Eiyo_Jyotai"));
                svf.VrsOut("SPINE"  ,  rs.getString("Sekichu_Kyokaku"));
                svf.VrsOut("EYES"   ,  rs.getString("Meekibyo_Ijyo"));
                svf.VrsOut("NOSE"   ,  rs.getString("Jibi_Shikkan"));
                svf.VrsOut("SKIN"   ,  rs.getString("Hifu_Shikkan"));
                svf.VrsOut("OTHERS1"    ,  rs.getString("Sonota"));
                svf.VrsOut("TEETH1"     ,  rs.getString("TEETH1"));
                svf.VrsOut("TEETH2"     ,  rs.getString("TEETH2"));
                svf.VrsOut("TEETH3"     ,  rs.getString("Shiretsu_Ago"));
                svf.VrsOut("SIKOU"      ,  rs.getString("Shikou"));
                svf.VrsOut("SINIKU"     ,  rs.getString("Shiniku"));
                svf.VrsOut("OTHERS2"    ,  rs.getString("Sonota_Ha"));

                svf.VrEndRecord();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.warn("printMain9 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }

    /**１１or１０）尿検査結果のお知らせ*/
    private boolean printMain10(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String text = getDocumentMst(db2, param, "10", "TEXT");
        final String title = getDocumentMst(db2, param, "10", "TITLE");
        try {
            setForm(param, svf, "KNJF030_10KUMA.frm", 4);
            String sql = statementMeisai(param, check_no10);
            log.debug("printMain10 sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (toInt(rs.getString("ALBUMINURIA1CD"), -1) < 3 && toInt(rs.getString("URICSUGAR1CD"), -1) < 3 && toInt(rs.getString("URICBLEED1CD"), -1) < 3 &&
                    toInt(rs.getString("ALBUMINURIA2CD"), -1) < 3 && toInt(rs.getString("URICSUGAR2CD"), -1) < 3 && toInt(rs.getString("URICBLEED2CD"), -1) < 3) {
                    continue;
                }

                svf.VrsOut("schoolname1"    , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME1));
                svf.VrsOut("post1"          , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_JOBNAME));
                svf.VrsOut("staff1"         , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_NAME));

                printTitle(svf, text, title);

                svf.VrsOut("HR_NAME1"    ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO1"   ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME1" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "_2" : ""),  rs.getString("NAME"));
                svf.VrsOut("HR_NAME2"    ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO2"   ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME2" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "_2" : ""),  rs.getString("NAME"));

                svf.VrsOut("ALBUMIN1"   , rs.getString("ALBUMINURIA1CD_NAME1"));
                svf.VrsOut("SACCHARIDE1", rs.getString("URICSUGAR1CD_NAME1"));
                svf.VrsOut("BLOOD1"     , rs.getString("URICBLEED1CD_NAME1"));
                svf.VrsOut("ALBUMIN2"   , rs.getString("ALBUMINURIA2CD_NAME1"));
                svf.VrsOut("SACCHARIDE2", rs.getString("URICSUGAR2CD_NAME1"));
                svf.VrsOut("BLOOD2"     , rs.getString("URICBLEED2CD_NAME1"));
                putGengou1(db2, svf, "ERA_NAME", param);

                svf.VrEndRecord();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.warn("printMain10 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }

    /**１２or１１）視力の検査結果のお知らせ */
    private boolean printMain11(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String text = getDocumentMst(db2, param, "11", "TEXT");
        final String title = getDocumentMst(db2, param, "11", "TITLE");
        try {
            setForm(param, svf, "KNJF030_11KUMA.frm", 4);//フォーム
            String sql = statementMeisai(param, check_no11);
            log.debug("printMain11 sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("schoolname1"    , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME1));
                svf.VrsOut("post"           , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_JOBNAME));
                svf.VrsOut("staff1"         , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_NAME));

                printTitle(svf, text, title);

                svf.VrsOut("HR_NAME1"    ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO1"   ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME1" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "_2" : ""),  rs.getString("NAME"));
                svf.VrsOut("HR_NAME"    ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "2" : ""),  rs.getString("NAME"));

                svf.VrsOut("SIGHT_R1" , rs.getString("R_BAREVISION_MARK"));//視力（右
                svf.VrsOut("SIGHT_R2" , rs.getString("R_VISION_MARK"));    //視力（右 矯正
                svf.VrsOut("SIGHT_L1" , rs.getString("L_BAREVISION_MARK"));//視力（左
                svf.VrsOut("SIGHT_L2" , rs.getString("L_VISION_MARK"));    //視力（左 矯正
                putGengou1(db2, svf, "ERA_NAME", param);

                svf.VrEndRecord();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.warn("printMain11 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }

    /**１３or１２）聴力の検査結果のお知らせ */
    private boolean printMain12(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String text = getDocumentMst(db2, param, "12", "TEXT");
        final String title = getDocumentMst(db2, param, "12", "TITLE");
        try {
            setForm(param, svf, "KNJF030_12KUMA.frm", 4);//フォーム
            String sql = statementMeisai(param, check_no12);
            log.debug("printMain12 sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("schoolname1"    , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), SCHOOL_NAME1));
                svf.VrsOut("post"           , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_JOBNAME));
                svf.VrsOut("staff1"         , param.getSchoolInfo(rs.getString("SCHOOL_KIND"), PRINCIPAL_NAME));

                printTitle(svf, text, title);

                String setText12 = "";
                if ("03".equals(rs.getString("R_EAR")) && "03".equals(rs.getString("L_EAR"))) {
                    setText12 = "（聴力右、聴力左）";
                } else if ("03".equals(rs.getString("R_EAR"))) {
                    setText12 = "（聴力右）";
                } else if ("03".equals(rs.getString("L_EAR"))) {
                    setText12 = "（聴力左）";
                }
                svf.VrsOut("RESULT1" , setText12 + "要精密 です");//結果

                svf.VrsOut("HR_NAME1"    ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO1"   ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME1" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "_2" : ""),  rs.getString("NAME"));
                svf.VrsOut("HR_NAME"    ,  rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO"   ,  rs.getString("ATTENDNO"));
                svf.VrsOut("NAME" + (getMS932ByteLength(rs.getString("NAME")) > 24 ? "2" : ""),  rs.getString("NAME"));
                putGengou1(db2, svf, "ERA_NAME", param);

                svf.VrEndRecord();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.warn("printMain12 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }

    /**１４or１３）定期健康診断結果一覧 */
    private boolean printMain13(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            setForm(param, svf, "KNJF030_13KUMA.frm", 4);//フォーム
            String sql = statementMeisai(param, check_no13);
            log.debug("printMain13 sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {

                svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年");
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, param._ctrlDate));
                svf.VrsOut("TIME", param._time);
                svf.VrsOut("HR_NAME", rs.getString("HR_NAME"));
                svf.VrsOut("MAJOR", rs.getString("MAJORNAME"));

                //生徒情報
                svf.VrsOut("ATTENDNO", rs.getString("ATTENDNO"));
                svf.VrsOut("ANNUAL", rs.getString("ANNUAL"));
                svf.VrsOut("SCHREGNO", rs.getString("SCHREGNO"));
                final String name = rs.getString("NAME");
                final String fieldNo = KNJ_EditEdit.getMS932ByteLength(name) > 34 ? "3": KNJ_EditEdit.getMS932ByteLength(name) > 20 ? "2": "";
                svf.VrsOut("NAME" + fieldNo, name);
                svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(db2, rs.getString("BIRTHDAY")));
                svf.VrsOut("SEX", rs.getString("SEX_NAME"));

                //健康診断（一般）
                svf.VrsOut("HEIGHT", rs.getString("HEIGHT"));
                svf.VrsOut("WEIGHT", rs.getString("WEIGHT"));
                setNameSonota(param, db2, svf, "NUTRITION", "F030", rs.getString("NUTRITIONCD"), rs.getString("NUTRITIONCD_NAME1"), rs.getString("NUTRITIONCD_REMARK"));
                setNameSonota(param, db2, svf, "SPINERIB", "F040", rs.getString("SPINERIBCD"), rs.getString("SPINERIBCD_NAME1"), rs.getString("SPINERIBCD_REMARK"));
                svf.VrsOut("R_BAREVISION_MARK", rs.getString("R_BAREVISION_MARK"));
                svf.VrsOut("R_VISION_MARK",     rs.getString("R_VISION_MARK"));
                svf.VrsOut("L_BAREVISION_MARK", rs.getString("L_BAREVISION_MARK"));
                svf.VrsOut("L_VISION_MARK",     rs.getString("L_VISION_MARK"));
                setNameSonota(param, db2, svf, "EYEDISEASE", "F050", rs.getString("EYEDISEASECD"), rs.getString("EYEDISEASECD_NAME1"), rs.getString("EYE_TEST_RESULT"));
                svf.VrsOut("R_EAR",     rs.getString("R_EAR_NAME1"));
                svf.VrsOut("L_EAR",     rs.getString("L_EAR_NAME1"));
                setNameSonota(param, db2, svf, "NOSEDISEASE", "F060", rs.getString("NOSEDISEASECD"), rs.getString("NOSEDISEASECD_NAME1"), rs.getString("NOSEDISEASECD_REMARK"));
                setNameSonota(param, db2, svf, "NOSEDISEASE2", "F060", rs.getString("NOSEDISEASECD2"), rs.getString("NOSEDISEASECD2_NAME1"), rs.getString("NOSEDISEASECD_REMARK2"));
                setNameSonota(param, db2, svf, "SKINDISEASE", "F070", rs.getString("SKINDISEASECD"), rs.getString("SKINDISEASECD_NAME1"), rs.getString("SKINDISEASECD_REMARK"));

                svf.VrsOut("PHOTO_DATE", KNJ_EditDate.h_format_JP(db2, rs.getString("TB_FILMDATE")));
                setNameSonota(param, db2, svf, "TB_NAME", "F100", rs.getString("TB_REMARKCD"), rs.getString("TB_REMARKCD_NAME1"), rs.getString("TB_NAME_REMARK1"));
                setNameSonota(param, db2, svf, "OTHERS", "F110", rs.getString("TB_OTHERTESTCD"), rs.getString("TB_OTHERTESTCD_NAME1"), rs.getString("TB_OTHERTEST_REMARK1"));
                setNameSonota(param, db2, svf, "DISEASE_NAME", "F120", rs.getString("TB_NAMECD"), rs.getString("TB_NAMECD_NAME1"), rs.getString("TB_NAME_REMARK1"));
                setNameSonota(param, db2, svf, "GUIDANCE", "F130", rs.getString("TB_ADVISECD"), rs.getString("TB_ADVISECD_NAME1"), rs.getString("TB_ADVISE_REMARK1"));
                setNameSonota(param, db2, svf, "HEART_MEDEXAM", "F080", rs.getString("HEART_MEDEXAM"), rs.getString("HEART_MEDEXAM_NAME1"), rs.getString("HEART_MEDEXAM_REMARK"));
                setNameSonota(param, db2, svf, "HEARTDISEASE1", "F090", rs.getString("HEARTDISEASECD"), rs.getString("HEARTDISEASECD_NAME1"), rs.getString("HEARTDISEASECD_REMARK"));
                svf.VrsOut("ALBUMINURIA1", rs.getString("ALBUMINURIA1CD_NAME1"));
                svf.VrsOut("URICSUGAR1",   rs.getString("URICSUGAR1CD_NAME1"));
                svf.VrsOut("URICBLEED1",   rs.getString("URICBLEED1CD_NAME1"));

                svf.VrsOut("ALBUMINURIA2", rs.getString("ALBUMINURIA2CD_NAME1"));
                svf.VrsOut("URICSUGAR2",   rs.getString("URICSUGAR2CD_NAME1"));
                svf.VrsOut("URICBLEED2",   rs.getString("URICBLEED2CD_NAME1"));
                svf.VrsOut("URINE_OTHERS",   rs.getString("URICOTHERTESTCD_NAME1"));
                setNameSonota(param, db2, svf, "OTHERDISEASE1", "F140", rs.getString("OTHERDISEASECD"), rs.getString("OTHERDISEASECD_NAME1"), rs.getString("OTHER_REMARK"));
                svf.VrsOut("DOC_DATE1", KNJ_EditDate.h_format_JP(db2, rs.getString("DOC_DATE")));
                setNameSonota(param, db2, svf, "DOC_TREAT1", "F150", rs.getString("TREATCD"), rs.getString("TREATCD_NAME1"), rs.getString("TREAT_REMARK1"));
                svf.VrsOut("NOTE1",   rs.getString("REMARK"));

                //健康診断（歯）
                final String j2Idx = KNJ_EditEdit.getMS932ByteLength(rs.getString("JAWS_JOINTCD2_NAME1")) > 20 ? "_1": "";
                svf.VrsOut("JAWS_JOINT2" + j2Idx, rs.getString("JAWS_JOINTCD2_NAME1"));
                final String j1IdxF514 = KNJ_EditEdit.getMS932ByteLength(rs.getString("JAWS_JOINTCD_NAME1_F514")) > 20 ? "_1": "";
                final String j1IdxF510 = KNJ_EditEdit.getMS932ByteLength(rs.getString("JAWS_JOINTCD_NAME1_F510")) > 20 ? "_1": "";
                svf.VrsOut("JAWS_JOINT1" + j1IdxF514,   rs.getString("JAWS_JOINTCD_NAME1_F514"));
                if (2016 > Integer.parseInt(param._year)) {
                    svf.VrsOut("JAWS_JOINT1" + j1IdxF510,   rs.getString("JAWS_JOINTCD_NAME1_F510"));
                }
                final int minGrade = Integer.parseInt(param._minGradeMap.get(rs.getString("SCHOOL_KIND")));
                final int setGrade = (Integer.parseInt(param._year) > 2022) ? 0: Integer.parseInt(param._year) - 2016 + minGrade;
                if (setGrade > 0 && Integer.parseInt(rs.getString("GRADE")) > setGrade) {
                    svf.VrsOut("JAWS_JOINT1" + j1IdxF510,   rs.getString("JAWS_JOINTCD_NAME1_F510"));
                }
                if (!"H".equals(rs.getString("SCHOOL_KIND"))) {
                    svf.VrsOut("JAWS_JOINT1" + j1IdxF510,   rs.getString("JAWS_JOINTCD_NAME1_F510"));
                }
                final String plIdxF516 = KNJ_EditEdit.getMS932ByteLength(rs.getString("PLAQUECD_NAME1_F516")) > 20 ? "_1": "";
                final String plIdxF520 = KNJ_EditEdit.getMS932ByteLength(rs.getString("PLAQUECD_NAME1_F520")) > 20 ? "_1": "";
                if (Integer.parseInt(param._year) >= 2016) {
                    svf.VrsOut("PLAQUE" + plIdxF516,   rs.getString("PLAQUECD_NAME1_F516"));
                } else {
                    svf.VrsOut("PLAQUE" + plIdxF520,   rs.getString("PLAQUECD_NAME1_F520"));
                }
                final String gumIdxF517 = KNJ_EditEdit.getMS932ByteLength(rs.getString("GUMCD_NAME1_F517")) > 20 ? "_1": "";
                final String gumIdxF513 = KNJ_EditEdit.getMS932ByteLength(rs.getString("GUMCD_NAME1_F513")) > 20 ? "_1": "";
                if (Integer.parseInt(param._year)>= 2016) {
                    svf.VrsOut("GUM" + gumIdxF517,   rs.getString("GUMCD_NAME1_F517"));
                } else {
                    svf.VrsOut("GUM" + gumIdxF513,   rs.getString("GUMCD_NAME1_F513"));
                }
                svf.VrsOut("STRAGHTTTOOTH",  "1".equals(rs.getString("ORTHODONTICS")) ? "有": "無");

                svf.VrsOut("BABYTOOTH",        rs.getString("BABYTOOTH"));
                svf.VrsOut("REMAINBABYTOOTH",  rs.getString("REMAINBABYTOOTH"));
                svf.VrsOut("TREATEDBABYTOOTH", rs.getString("TREATEDBABYTOOTH"));
                svf.VrsOut("BRACKBABYTOOTH",   rs.getString("BRACK_BABYTOOTH"));

                svf.VrsOut("ADULTTOOTH",        rs.getString("ADULTTOOTH"));
                svf.VrsOut("REMAINADULTTOOTH",  rs.getString("REMAINADULTTOOTH"));
                svf.VrsOut("TREATEDADULTTOOTH", rs.getString("TREATEDADULTTOOTH"));
                svf.VrsOut("LOSTADULTTOOTH",    rs.getString("LOSTADULTTOOTH"));
                svf.VrsOut("BRACKADULTTOOTH",   rs.getString("BRACK_ADULTTOOTH"));

                setNameSonota(param, db2, svf, "OTHERDISEASE2", "F530", rs.getString("T_OTHERDISEASECD"), rs.getString("T_OTHERDISEASECD_NAME1"), rs.getString("T_OTHERDISEASE"));
                svf.VrsOut("DENTISTREMARK1", rs.getString("DENTISTREMARK_CO"));
                svf.VrsOut("DENTISTREMARK2", "1".equals(rs.getString("DENTISTREMARK_GO")) ? "有": "無");
                svf.VrsOut("DENTISTREMARK3", "1".equals(rs.getString("DENTISTREMARK_G")) ? "有": "無");
                svf.VrsOut("DOC_DATE2", KNJ_EditDate.h_format_JP(db2, rs.getString("DENTISTREMARKDATE")));
                setNameSonota(param, db2, svf, "DOC_TREAT2", "F541", rs.getString("DENTISTTREATCD"), rs.getString("DENTISTTREATCD_NAME1"), rs.getString("DENTISTTREAT"));

                svf.VrEndRecord();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.warn("printMain13 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }

    /**
     *
     * @param param
     * @param db2
     * @param svf
     * @param fieldName
     * @param nameCd1
     * @param nameCd2
     * @param name1
     * @param bikou
     * @throws SQLException
     */
    private void setNameSonota(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf,
            final String fieldName,
            final String nameCd1,
            final String nameCd2,
            final String name1,
            final String bikou
    ) throws SQLException {
        if (isSonota(param, db2, nameCd1, nameCd2)) {
            svf.VrsOut(fieldName, bikou);
        } else {
            svf.VrsOut(fieldName, name1);
        }
    }

    /**
     * コードが「その他」（名称マスタの予備2が'1'）ならtrue、それ以外はfalse
     */
    private boolean isSonota(
            final Param param,
            final DB2UDB db2,
            final String nameCd1,
            final String nameCd2
    ) throws SQLException {
        return param.nameMstNamespare2Is1(db2, nameCd1, nameCd2);
    }

    /**文面マスタからタイトルと本文をセット　共通*/
    private String getDocumentMst(final DB2UDB db2, final Param param, final String documentcd, final String field)
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String rtn = null;
        try {
            ps = db2.prepareStatement("SELECT " + field + " FROM DOCUMENT_MST WHERE DOCUMENTCD = ?");
            ps.setString(1, documentcd);
            rs = ps.executeQuery();
            while (rs.next()) {
                rtn = rs.getString(field);
            }
        } catch (Exception ex) {
            log.warn("printTitle read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtn;
    }

    /**文面詳細マスタからタイトルと本文をセット　共通*/
    private String getDocumentDetailMst(final DB2UDB db2, final Param param, final String documentcd, final String field, final String seq) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String rtn = null;
        try {
            ps = db2.prepareStatement("SELECT " + field + " FROM DOCUMENT_DETAIL_MST WHERE DOCUMENTCD = ? AND SEQ = ?");
            ps.setString(1, documentcd);
            ps.setString(2, seq);
            rs = ps.executeQuery();
            log.debug("sql =" + rs);
            while (rs.next()) {
                rtn = rs.getString(field);
            }
        } catch (Exception ex) {
            log.warn("printDetailTitle read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtn;
    }

    /**文面マスタからタイトルと本文をセット　共通*/
    private void printTitle(final Vrw32alp svf, final String text, final String title)
    {
        if (text != null) {
            StringTokenizer st = new StringTokenizer(text, "\n");
            int j = 1;
            while(st.hasMoreTokens()) {
                svf.VrsOut("TEXT" + j ,   st.nextToken());//本文
                j++;
            }
        }
        svf.VrsOut("TITLE"  , title);     //タイトル
    }

    /**
     *  １or２）生徒学生健康診断票（一般or歯・口腔）結果
     *
     */
    private static String statementResult(final Param param, final int check_no) {
        final StringBuffer stb = new StringBuffer();
        //在籍（現在年度）
        stb.append("WITH SCHNO AS ( ");
        stb.append("    SELECT ");
        stb.append("        T1.SCHREGNO, ");
        stb.append("        T1.YEAR, ");
        stb.append("        T1.SEMESTER, ");
        if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
            stb.append("        T1.COURSECODE, ");
            stb.append("        L2.COURSECODENAME, ");
        }
        stb.append("        T1.GRADE, ");
        stb.append("        T1.HR_CLASS, ");
        stb.append("        T1.ATTENDNO ");
        stb.append("    FROM ");
        stb.append("        SCHREG_REGD_DAT T1 ");
        if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
            stb.append(" LEFT JOIN COURSECODE_MST L2 ");
            stb.append("   ON L2.COURSECODE = T1.COURSECODE ");
        }
        stb.append("    WHERE  T1.YEAR = '"+param._year+"' AND T1.SEMESTER = '"+param._gakki+"' AND ");
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
        if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
            stb.append("        W1.COURSECODE, ");
            stb.append("        L2.COURSECODENAME, ");
        }
        stb.append("        L1.SCHOOL_KIND, ");
        stb.append("        L1.GRADE_CD, ");
        stb.append("        L1.GRADE_NAME1 ");
        stb.append("    FROM ");
        stb.append("        SCHREG_REGD_DAT W1 ");
        stb.append("        LEFT JOIN SCHREG_REGD_GDAT L1 ON W1.YEAR = L1.YEAR ");
        stb.append("             AND W1.GRADE = L1.GRADE ");
        if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
            stb.append(" LEFT JOIN COURSECODE_MST L2 ");
            stb.append("   ON L2.COURSECODE = W1.COURSECODE ");
        }
        stb.append("    WHERE  EXISTS(SELECT 'X' FROM SCHNO_MIN W2 WHERE W2.SCHREGNO=W1.SCHREGNO AND W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER) ");
        stb.append("    UNION ");
        stb.append("    SELECT ");
        stb.append("        W1.SCHREGNO, ");
        stb.append("        W1.YEAR, ");
        stb.append("        W1.SEMESTER, ");
        stb.append("        W1.GRADE, ");
        stb.append("        W1.HR_CLASS, ");
        stb.append("        W1.ATTENDNO, ");
        if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
            stb.append("        W1.COURSECODE, ");
            stb.append("        L2.COURSECODENAME, ");
        }
        stb.append("        L1.SCHOOL_KIND, ");
        stb.append("        L1.GRADE_CD, ");
        stb.append("        L1.GRADE_NAME1 ");
        stb.append("    FROM ");
        stb.append("        SCHNO W1 ");
        stb.append("        LEFT JOIN SCHREG_REGD_GDAT L1 ON W1.YEAR = L1.YEAR ");
        stb.append("             AND W1.GRADE = L1.GRADE ");
        if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
            stb.append(" LEFT JOIN COURSECODE_MST L2 ");
            stb.append("   ON L2.COURSECODE = W1.COURSECODE ");
        }
        stb.append("    ) ");

        //メイン
        stb.append("SELECT T1.*, T2.HR_NAME, T2.HR_CLASS_NAME1, T3.NAME, ");
        stb.append("       (SELECT NAME2 FROM NAME_MST WHERE NAMECD1='Z002' AND NAMECD2=SEX) AS SEX, BIRTHDAY, ");
        stb.append("       CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR(T2.YEAR || '-04-01' - BIRTHDAY) END AS AGE, ");
        if (check_no == check_no1) {//一般
            stb.append("   HEIGHT, WEIGHT, SITHEIGHT, R_BAREVISION, L_BAREVISION, R_VISION, L_VISION, R_BAREVISION_MARK, L_BAREVISION_MARK, R_VISION_MARK, L_VISION_MARK, ");
            stb.append("   VISION_CANTMEASURE, ");
            if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
                stb.append("  R_EAR_DB_1000, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=R_EAR_DB_1000) AS R_EAR_DB1_NAME, ");
                stb.append("  L_EAR_DB_1000, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=L_EAR_DB_1000) AS L_EAR_DB1_NAME, ");
                stb.append("  R_EAR_DB_4000, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=R_EAR_DB_4000) AS R_EAR_DB4_NAME, ");
                stb.append("  L_EAR_DB_4000, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=L_EAR_DB_4000) AS L_EAR_DB4_NAME, ");
            }
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=R_EAR) AS R_EAR, R_EAR_DB, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=L_EAR) AS L_EAR, L_EAR_DB, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA1CD) AS ALBUMINURIA1CD, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F019' AND NAMECD2=URICSUGAR1CD) AS URICSUGAR1CD, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F018' AND NAMECD2=URICBLEED1CD) AS URICBLEED1CD, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA2CD) AS ALBUMINURIA2CD, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F019' AND NAMECD2=URICSUGAR2CD) AS URICSUGAR2CD, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F018' AND NAMECD2=URICBLEED2CD) AS URICBLEED2CD, ");
            if ("2".equals(param._printKenkouSindanIppan)) {
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F022' AND NAMECD2=URICOTHERTESTCD) AS URICOTHERTEST, ");
            } else {
                stb.append("   URICOTHERTEST, ");
            }
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F023' AND NAMECD2=PARASITE) AS PARASITE, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F030' AND NAMECD2=NUTRITIONCD) AS NUTRITIONCD, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F040' AND NAMECD2=SPINERIBCD) AS SPINERIBCD, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F050' AND NAMECD2=EYEDISEASECD) AS EYEDISEASECD, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F060' AND NAMECD2=NOSEDISEASECD) AS NOSEDISEASECD, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F070' AND NAMECD2=SKINDISEASECD) AS SKINDISEASECD, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F080' AND NAMECD2=HEART_MEDEXAM) AS HEART_MEDEXAM, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F090' AND NAMECD2=HEARTDISEASECD) AS HEARTDISEASECD, ");
            stb.append("   TB_FILMDATE, TB_FILMNO, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F100' AND NAMECD2=TB_REMARKCD) AS TB_REMARKCD, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F110' AND NAMECD2=TB_OTHERTESTCD) AS TB_OTHERTESTCD, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F120' AND NAMECD2=TB_NAMECD) AS TB_NAMECD, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F130' AND NAMECD2=TB_ADVISECD) AS TB_ADVISECD, ");
            stb.append("   ANEMIA_REMARK, HEMOGLOBIN, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F140' AND NAMECD2=OTHERDISEASECD) AS OTHERDISEASECD, ");
            stb.append("   DOC_REMARK, DOC_DATE, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F150' AND NAMECD2=TREATCD) AS TREATCD, ");
            stb.append("   REMARK,  ");
            stb.append("   T5.DATE ");
        } else if (check_no == check_no2) {//歯・口腔
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
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F521' AND NAMECD2 = VALUE(CALCULUS, '')) AS CALCULUS, ");
            stb.append("   VALUE(ORTHODONTICS,'0') AS ORTHODONTICS, ");
            stb.append("   UP_R_ADULT8, UP_R_ADULT7, UP_R_ADULT6, UP_R_ADULT5, UP_R_ADULT4, UP_R_ADULT3, UP_R_ADULT2, UP_R_ADULT1, ");
            stb.append("   UP_L_ADULT1, UP_L_ADULT2, UP_L_ADULT3, UP_L_ADULT4, UP_L_ADULT5, UP_L_ADULT6, UP_L_ADULT7, UP_L_ADULT8, ");
            stb.append("   UP_R_BABY5, UP_R_BABY4, UP_R_BABY3, UP_R_BABY2, UP_R_BABY1, ");
            stb.append("   UP_L_BABY1, UP_L_BABY2, UP_L_BABY3, UP_L_BABY4, UP_L_BABY5, ");
            stb.append("   LW_R_BABY5, LW_R_BABY4, LW_R_BABY3, LW_R_BABY2, LW_R_BABY1, ");
            stb.append("   LW_L_BABY1, LW_L_BABY2, LW_L_BABY3, LW_L_BABY4, LW_L_BABY5, ");
            stb.append("   LW_R_ADULT8, LW_R_ADULT7, LW_R_ADULT6, LW_R_ADULT5, LW_R_ADULT4, LW_R_ADULT3, LW_R_ADULT2, LW_R_ADULT1, ");
            stb.append("   LW_L_ADULT1, LW_L_ADULT2, LW_L_ADULT3, LW_L_ADULT4, LW_L_ADULT5, LW_L_ADULT6, LW_L_ADULT7, LW_L_ADULT8, ");
            stb.append("   BABYTOOTH,REMAINBABYTOOTH,TREATEDBABYTOOTH,BRACK_BABYTOOTH, ");
            stb.append("   ADULTTOOTH,REMAINADULTTOOTH,TREATEDADULTTOOTH,LOSTADULTTOOTH,BRACK_ADULTTOOTH,CHECKADULTTOOTH,");
            stb.append("   (SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1='F530' AND NAMECD2=OTHERDISEASECD) AS OTHERDISEASECD_NAMESPARE1, ");
            stb.append("   (SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1='F530' AND NAMECD2=OTHERDISEASECD) AS OTHERDISEASECD_NAMESPARE2, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F530' AND NAMECD2=OTHERDISEASECD) AS OTHERDISEASECD, ");
            stb.append("   DENTISTREMARK_CO, ");
            stb.append("   DENTISTREMARK, ");
            stb.append("   CASE WHEN DENTISTREMARK_GO = '1' THEN '○' END AS DENTISTREMARK_GO, ");
            stb.append("   CASE WHEN DENTISTREMARK_G = '1' THEN '○' END AS DENTISTREMARK_G, ");
            stb.append("   OTHERDISEASE AS OTHERDISEASE_TEXT, ");
            if (param._isMiyagiken) {
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F530' AND NAMECD2=OTHERDISEASECD3) AS OTHERDISEASECD3, ");
                stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F530' AND NAMECD2=OTHERDISEASECD4) AS OTHERDISEASECD4, ");
            }
            stb.append("   DENTISTTREAT AS DENTISTTREAT_TEXT, ");
            if ("2".equals(param._printKenkouSindanIppan)) {
                stb.append("   DENTISTTREAT2 AS DENTISTTREAT_TEXT2, ");
                stb.append("   DENTISTTREAT3 AS DENTISTTREAT_TEXT3, ");
            }
            stb.append("   (SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1='F540' AND NAMECD2=DENTISTREMARKCD) AS DENTISTREMARKCD_NAMESPARE2, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F540' AND NAMECD2=DENTISTREMARKCD) AS DENTISTREMARKCD, ");
            stb.append("   DENTISTREMARKDATE,");
            stb.append("   MONTH(DENTISTREMARKDATE) AS DENTISTREMARKMONTH,");
            stb.append("     DAY(DENTISTREMARKDATE) AS DENTISTREMARKDAY, ");
            stb.append("   (SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1='F541' AND NAMECD2=DENTISTTREATCD) AS DENTISTTREAT_NAMESPARE2, ");
            stb.append("   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F541' AND NAMECD2=DENTISTTREATCD) AS DENTISTTREAT ");
        }
        stb.append("  , DET017.DET_REMARK1 AS DET017_REMARK1 ");
        stb.append("  , DET017.DET_REMARK2 AS DET017_REMARK2 ");
        stb.append("FROM   SCHNO_ALL T1 ");
        stb.append("       INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR=T1.YEAR AND T2.SEMESTER=T1.SEMESTER AND T2.GRADE=T1.GRADE AND T2.HR_CLASS=T1.HR_CLASS ");
        stb.append("       INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO=T1.SCHREGNO ");
        if (check_no == check_no1) { //一般
            stb.append("   INNER JOIN V_MEDEXAM_DET_DAT T4 ON T4.YEAR=T1.YEAR AND T4.SCHREGNO=T1.SCHREGNO ");
        } else { //歯・口腔
            if ("2".equals(param._printKenkouSindanIppan) || param._isMiyagiken) {
                stb.append("   INNER JOIN V_MEDEXAM_TOOTH_DAT T4 ON T4.YEAR=T1.YEAR AND T4.SCHREGNO=T1.SCHREGNO ");
            } else {
                stb.append("   INNER JOIN MEDEXAM_TOOTH_DAT T4 ON T4.YEAR=T1.YEAR AND T4.SCHREGNO=T1.SCHREGNO ");
            }
        }
        stb.append("   LEFT JOIN MEDEXAM_HDAT T5 ON T5.YEAR=T1.YEAR AND T5.SCHREGNO=T1.SCHREGNO ");
        stb.append("   LEFT JOIN MEDEXAM_DET_DETAIL_DAT DET017 ON DET017.YEAR = T1.YEAR AND DET017.DET_SEQ = '017' AND DET017.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN NAME_MST VN ON VN.NAMECD1 = 'A023' ");
        stb.append("        AND VN.NAME1 = ? ");
        stb.append(" WHERE ");
        stb.append("    T1.GRADE BETWEEN VN.NAME2 AND VN.NAME3 ");
        stb.append("ORDER BY T1.YEAR ");
        return stb.toString();
    }


    /**
     *  在籍情報
     *
     *  フォーム印刷
     */
    private String statementSchno(final Param param)
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


    /**
     *  ４〜１４）各種帳票
     *
     */
    private String statementMeisai(final Param param, final int check_no)
    {
        final StringBuffer stb = new StringBuffer();
        //在籍
        stb.append("WITH SCHNO AS ( ");
        stb.append("    SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS,ATTENDNO, COURSECD, MAJORCD, ANNUAL ");
        stb.append("    FROM   SCHREG_REGD_DAT ");
        stb.append("    WHERE  YEAR='"+param._year+"' AND SEMESTER='"+param._gakki+"' AND ");
        if ("1".equals(param._kubun)) { //1:クラス
            stb.append("       GRADE||HR_CLASS IN "+param._selectInState+" ");
        } else if ("2".equals(param._kubun)) { //2:個人
            stb.append("       SCHREGNO IN "+param._selectInState+" ");
        }
        stb.append(" ) ");

        //メイン
        stb.append("SELECT T2.SCHREGNO,T4.NAME,T4.SEX,T4.BIRTHDAY, FISCALYEAR(ENT_DATE) AS ENT_NENDO, HR_NAME,T2.GRADE,T2.HR_CLASS, T8.SCHOOL_KIND, ATTENDNO ");
        stb.append("  ,HEIGHT, WEIGHT, SITHEIGHT ");
        stb.append("  ,R_BAREVISION ,L_BAREVISION ,R_VISION ,L_VISION, R_BAREVISION_MARK ,L_BAREVISION_MARK ,R_VISION_MARK ,L_VISION_MARK ");
        stb.append("  ,MAJORNAME, ANNUAL, (SELECT NAME2 FROM NAME_MST WHERE NAMECD1='Z002' AND NAMECD2=T4.SEX) AS SEX_NAME ");
        if (check_no == check_no3) {//健康診断の未受検項目のある生徒へ
            stb.append("  ,CASE WHEN ALBUMINURIA1CD > '0' OR URICSUGAR1CD > '0' OR ");
            if ("2".equals(param._printKenkouSindanIppan)) {
                stb.append("             URICBLEED1CD > '0' OR VALUE(URICOTHERTESTCD,'') > '0' THEN NULL ");
            } else {
                stb.append("             URICBLEED1CD > '0' OR VALUE(URICOTHERTEST,'') > '0' THEN NULL ");
            }
            stb.append("        ELSE '○' END AS NYOU_KENSA ");
            stb.append("  ,CASE WHEN TREATCD = '04' OR TREATCD = '05' THEN '○' END AS HINKETSU_KENSA ");
            stb.append("  ,CASE WHEN NUTRITIONCD > '0' AND SPINERIBCD > '0' AND NOSEDISEASECD > '0' AND SKINDISEASECD > '0' THEN NULL ");
            stb.append("        ELSE '○' END AS NAIKA_KOUI_KENSHIN ");
            stb.append("  ,CASE WHEN DENTISTREMARKDATE IS NOT NULL THEN NULL ELSE '○' END AS SHIKA_KENSHIN ");
            stb.append("  ,CASE WHEN TB_FILMDATE IS NOT NULL THEN NULL ELSE '○' END AS KOUBU_RENNTOGEN_SATSUEI ");
            stb.append("  ,CASE WHEN HEART_MEDEXAM > '0' THEN NULL ELSE '○' END AS SHINDENZU_KENSA");
            if (param._isKumamoto) {
                stb.append("  ,CASE WHEN R_BAREVISION IS NULL AND L_BAREVISION IS NULL AND R_VISION IS NULL AND L_VISION IS NULL AND ");
                stb.append("             R_BAREVISION_MARK IS NULL AND L_BAREVISION_MARK IS NULL AND R_VISION_MARK IS NULL AND L_VISION_MARK IS NULL ");
                stb.append("        THEN '○' END AS CHECK_SHIRYOKU "); // 視力
                stb.append("  ,EYEDISEASECD, CASE WHEN VALUE(EYEDISEASECD, '02') = '02' THEN '○' END AS CHECK_GANKA "); // 眼科
                stb.append("  ,R_EAR, L_EAR, CASE WHEN VALUE(R_EAR, '02') = '02' OR VALUE(L_EAR, '02') = '02' THEN '○' END AS CHECK_CHORYOKU "); // 聴力
                stb.append("  ,NOSEDISEASECD, CASE WHEN VALUE(NOSEDISEASECD, '02') = '02' THEN '○' END AS CHECK_JIBIKA "); // 耳鼻科
                stb.append("  ,CASE WHEN VALUE(NUTRITIONCD, '02') = '02' AND VALUE(SPINERIBCD, '02') = '02' AND VALUE(SKINDISEASECD, '02') = '02' THEN '○' END AS CHECK_NAKIKA "); // 内科
                stb.append("  ,CASE WHEN VALUE(HEART_MEDEXAM, '02') = '02' THEN '○' END AS CHECK_HEART "); // 心電図
                stb.append("  ,CASE WHEN VALUE(ALBUMINURIA1CD, '02') = '02' OR VALUE(URICSUGAR1CD, '02') = '02' OR ");
                stb.append("             VALUE(URICBLEED1CD, '02') = '02' ");
                if ("2".equals(param._printKenkouSindanIppan)) {
                    stb.append("             OR VALUE(URICOTHERTESTCD,'02') = '02' ");
                } else {
                    stb.append("             OR VALUE(URICOTHERTEST,'02') = '02' ");
                }
                stb.append("        THEN '○' END AS CHECK_NYO "); // 尿
            }
        } else if (check_no == check_no4) {//眼科検診のお知らせ
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F050' AND NAMECD2=EYEDISEASECD) AS EYEDISEASECD ");
        } else if (check_no == check_no5) {//検診結果のお知らせ（歯・口腔）

            stb.append("  ,BABYTOOTH + ADULTTOOTH AS GENZAI_SU ");
            stb.append("  ,TREATEDADULTTOOTH + TREATEDBABYTOOTH AS SYOCHI_SU ");
            stb.append("  ,REMAINADULTTOOTH + REMAINBABYTOOTH AS MISYOCHI_SU ");
            stb.append("  ,BRACK_ADULTTOOTH AS KANSATSU_SU ");//Modify
            stb.append("  ,LOSTADULTTOOTH AS SOSHITSU_SU ");
            stb.append("  ,BRACK_BABYTOOTH ");
            stb.append("  ,JAWS_JOINTCD,      (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F510' AND NAMECD2 = JAWS_JOINTCD) AS JAWS_JOINTCD_NAME1 ");
            stb.append("  ,JAWS_JOINTCD2,     (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F515' AND NAMECD2 = JAWS_JOINTCD) AS JAWS_JOINTCD2_NAME1 ");
            stb.append("  ,JAWS_JOINTCD3,     (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F510' AND NAMECD2 = JAWS_JOINTCD3) AS JAWS_JOINTCD3_NAME1 ");
            stb.append("  ,PLAQUECD,          (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F520' AND NAMECD2 = PLAQUECD) AS PLAQUECD_NAME1 ");
            stb.append("  ,GUMCD,             (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F513' AND NAMECD2 = GUMCD) AS GUMCD_NAME1 ");
            stb.append("  ,T6.OTHERDISEASECD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F530' AND NAMECD2 = T6.OTHERDISEASECD) AS OTHERDISEASECD_NAME1 ");
            stb.append("  ,T6.OTHERDISEASE ");
            stb.append("  ,DENTISTREMARKCD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F540' AND NAMECD2 = DENTISTREMARKCD) AS DENTISTREMARKCD_NAME1 ");
        } else if (check_no == check_no6) {//定期健康診断結果
            stb.append("  ,T7.DATE, R_EAR_DB, L_EAR_DB, REMARK ");
            stb.append("  ,CASE WHEN VALUE(HEIGHT,0) > 0 THEN DECIMAL(ROUND(WEIGHT/HEIGHT/HEIGHT*10000,1),4,1) END AS BMI ");
            stb.append("  ,EYE_TEST_RESULT, HEART_MEDEXAM_REMARK, NOSEDISEASECD_REMARK, HEARTDISEASECD_REMARK ");
            stb.append("  ,JAWS_JOINTCD, JAWS_JOINTCD2, JAWS_JOINTCD3");
            stb.append("  ,PLAQUECD ");
            stb.append("  ,GUMCD, REMAINADULTTOOTH, REMAINBABYTOOTH, BRACK_ADULTTOOTH, BRACK_BABYTOOTH ");
            if ("1".equals(param._KenkouSindan_Ippan_Pattern)) {
                stb.append(" ,TREATEDADULTTOOTH, TREATEDBABYTOOTH ");
                stb.append("  ,T5.R_EAR_DB_1000, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=T5.R_EAR_DB_1000) AS R_EAR_DB1_NAME ");
                stb.append("  ,T5.L_EAR_DB_1000, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=T5.L_EAR_DB_1000) AS L_EAR_DB1_NAME ");
                stb.append("  ,T5.R_EAR_DB_4000, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=T5.R_EAR_DB_4000) AS R_EAR_DB4_NAME ");
                stb.append("  ,T5.L_EAR_DB_4000, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=T5.L_EAR_DB_4000) AS L_EAR_DB4_NAME ");
                stb.append("  ,T5.R_EAR_CANTMEASURE ");
                stb.append("  ,T5.L_EAR_CANTMEASURE ");
            } else {
                stb.append("  ,R_EAR, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=R_EAR) AS R_EAR_NAME1 ");
                stb.append("  ,L_EAR, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=L_EAR) AS L_EAR_NAME1 ");
            }
            stb.append("  ,ALBUMINURIA1CD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA1CD) AS ALBUMINURIA1CD_NAME1 "); //1次 蛋白
            stb.append("  ,URICSUGAR1CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F019' AND NAMECD2=URICSUGAR1CD) AS URICSUGAR1CD_NAME1 "); //1次 糖
            stb.append("  ,URICBLEED1CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F018' AND NAMECD2=URICBLEED1CD) AS URICBLEED1CD_NAME1 "); //1次 潜血
            stb.append("  ,ALBUMINURIA2CD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA2CD) AS ALBUMINURIA2CD_NAME1 "); //2次 蛋白
            stb.append("  ,URICSUGAR2CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F019' AND NAMECD2=URICSUGAR2CD) AS URICSUGAR2CD_NAME1 "); //2次 糖
            stb.append("  ,URICBLEED2CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F018' AND NAMECD2=URICBLEED2CD) AS URICBLEED2CD_NAME1 "); //2次 潜血
            stb.append("  ,EYEDISEASECD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F050' AND NAMECD2=EYEDISEASECD) AS EYEDISEASECD_NAME1 ");
            stb.append("  ,NOSEDISEASECD,  (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F060' AND NAMECD2=NOSEDISEASECD) AS NOSEDISEASECD_NAME1 ");
            stb.append("  ,NUTRITIONCD ");
            stb.append("  ,SPINERIBCD,     (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F040' AND NAMECD2=SPINERIBCD) AS SPINERIBCD_NAME1 ");
            stb.append("  ,DOC_CD ");
            stb.append("  ,DOC_REMARK ");
            stb.append("  ,SKINDISEASECD,  (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F070' AND NAMECD2=SKINDISEASECD) AS SKINDISEASECD_NAME1 ");
            stb.append("  ,NUTRITIONCD_REMARK ");
            stb.append("  ,SPINERIBCD_REMARK ");
            stb.append("  ,SKINDISEASECD_REMARK ");
            stb.append("  ,HEART_MEDEXAM, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F080' AND NAMECD2=HEART_MEDEXAM) AS HEART_MEDEXAM_NAME1 ");
            stb.append("  ,HEARTDISEASECD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F090' AND NAMECD2=HEARTDISEASECD) AS HEARTDISEASECD_NAME1 ");
            stb.append("  ,TB_REMARKCD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F100' AND NAMECD2=TB_REMARKCD) AS TB_REMARKCD_NAME1 ");
            stb.append("  ,TB_NAME_REMARK1 ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F120' AND NAMECD2=TB_NAMECD) AS TB_NAMECD_NAME1 ");
            stb.append("  ,(SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1='F120' AND NAMECD2=TB_NAMECD) AS TB_NAMECD_NAMESPARE2 ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F130' AND NAMECD2=TB_ADVISECD) AS TB_ADVISECD_NAME1 ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F145' AND NAMECD2=OTHER_ADVISECD) AS OTHER_ADVISECD_NAME1 ");
            stb.append("  ,T5.OTHERDISEASECD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F140' AND NAMECD2=T5.OTHERDISEASECD) AS OTHERDISEASECD_NAME1 ");
            stb.append("  ,(SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1='F140' AND NAMECD2=T5.OTHERDISEASECD) AS OTHERDISEASECD_NAMESPARE2 ");
            stb.append("  ,OTHER_REMARK2 ");
            if (param._isKindai) {
                stb.append("  ,T6.REMAINBABYTOOTH  ");
                stb.append("  ,T6.OTHERDISEASECD AS TOOTH_OTHERDISEASECD ");
                stb.append("  ,T6.OTHERDISEASE AS TOOTH_OTHERDISEASE ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F530' AND NAMECD2 > '01' AND NAMECD2=T6.OTHERDISEASECD) AS TOOTH_OTHERDISEASECD_NAME1 ");
                stb.append("  ,(SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1='F530' AND NAMECD2 > '01' AND NAMECD2=T6.OTHERDISEASECD) AS TOOTH_OTHERDISEASECD_NAMESPARE2 ");
            }
        } else if (check_no == check_no7 || check_no == check_no8) {//検診結果のお知らせ（一般）or 内科検診所見あり生徒の名簿
            if (check_no == check_no7) {
                stb.append("  ,TB_X_RAY,MANAGEMENT_REMARK ");
                stb.append("  ,ALBUMINURIA1CD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA1CD) AS ALBUMINURIA1CD_NAME1 "); //1次 蛋白
                stb.append("  ,URICSUGAR1CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F019' AND NAMECD2=URICSUGAR1CD) AS URICSUGAR1CD_NAME1 "); //1次 糖
                stb.append("  ,URICBLEED1CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F018' AND NAMECD2=URICBLEED1CD) AS URICBLEED1CD_NAME1 "); //1次 潜血
                stb.append("  ,ALBUMINURIA2CD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA2CD) AS ALBUMINURIA2CD_NAME1 "); //2次 蛋白
                stb.append("  ,URICSUGAR2CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F019' AND NAMECD2=URICSUGAR2CD) AS URICSUGAR2CD_NAME1 "); //2次 糖
                stb.append("  ,URICBLEED2CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F018' AND NAMECD2=URICBLEED2CD) AS URICBLEED2CD_NAME1 "); //2次 潜血
            } else if (check_no == check_no8) {
                stb.append("  ,NUTRITION_RESULT ,EYEDISEASE_RESULT ,SKINDISEASE_RESULT ,SPINERIB_RESULT ,NOSEDISEASE_RESULT ,OTHERDISEASE_RESULT ,HEARTDISEASE_RESULT ");
            }
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F030' AND NAMECD2 > '01' AND NAMECD2=NUTRITIONCD) AS Naika ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F040' AND NAMECD2 > '01' AND NAMECD2=SPINERIBCD) AS Sekichu_Kyokaku ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F050' AND NAMECD2 > '01' AND NAMECD2=EYEDISEASECD) AS Ganka ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F060' AND NAMECD2 > '01' AND NAMECD2=NOSEDISEASECD) AS Jibika ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F070' AND NAMECD2 > '01' AND NAMECD2=SKINDISEASECD) AS Hifuka ");
            stb.append("  ,(case when NUTRITIONCD   > '01' then NUTRITIONCD   else '' end) AS Naika_cd ");
            stb.append("  ,(case when SPINERIBCD    > '01' then SPINERIBCD    else '' end) AS Sekichu_Kyokaku_cd ");
            stb.append("  ,(case when EYEDISEASECD  > '01' then EYEDISEASECD  else '' end) AS Ganka_cd ");
            stb.append("  ,(case when NOSEDISEASECD > '01' then NOSEDISEASECD else '' end) AS Jibika_cd ");
            stb.append("  ,(case when SKINDISEASECD > '01' then SKINDISEASECD else '' end) AS Hifuka_cd ");
            stb.append("  ,NUTRITIONCD_REMARK   AS Naika_remark ");
            stb.append("  ,SPINERIBCD_REMARK    AS Sekichu_Kyokaku_remark ");
            stb.append("  ,EYE_TEST_RESULT      AS Ganka_remark ");
            stb.append("  ,NOSEDISEASECD_REMARK AS Jibika_remark ");
            stb.append("  ,SKINDISEASECD_REMARK AS Hifuka_remark ");
            if (check_no == check_no8) {
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F090' AND NAMECD2 > '01' AND NAMECD2=HEARTDISEASECD) AS Shinzou_Kenshin ");
                stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F140' AND NAMECD2 > '01' AND NAMECD2=T5.OTHERDISEASECD) AS Sonota ");
            }
        } else if (check_no == check_no7Kuma) {//検診結果のお知らせ（一般）熊本
            // 内科
            stb.append("  ,T5.OTHERDISEASECD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F140' AND NAMECD2 = T5.OTHERDISEASECD) AS OTHERDISEASECD_NAME1 ");
            stb.append("  ,T5.OTHER_REMARK, T5.OTHER_REMARK2, T5.OTHER_REMARK3 ");
            stb.append("  ,T5.SPINERIBCD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F040' AND NAMECD2 = T5.SPINERIBCD) AS SPINERIBCD_NAME1 ");
            stb.append("  ,T5.SPINERIBCD_REMARK ");
            // 皮膚疾患
            stb.append("  ,T5.SKINDISEASECD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F070' AND NAMECD2 = T5.SKINDISEASECD) AS SKINDISEASECD_NAME1 ");
            stb.append("  ,T5.SKINDISEASECD_REMARK ");
            // 眼科
            stb.append("  ,T5.EYEDISEASECD,  (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F050' AND NAMECD2 = T5.EYEDISEASECD) AS EYEDISEASECD_NAME1 ");
            stb.append("  ,T5.EYEDISEASECD2, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F050' AND NAMECD2 = T5.EYEDISEASECD2) AS EYEDISEASECD2_NAME1 ");
            stb.append("  ,T5.EYEDISEASECD3, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F050' AND NAMECD2 = T5.EYEDISEASECD3) AS EYEDISEASECD3_NAME1 ");
            stb.append("  ,T5.EYEDISEASECD4, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F050' AND NAMECD2 = T5.EYEDISEASECD4) AS EYEDISEASECD4_NAME1 ");
            stb.append("  ,T5.EYE_TEST_RESULT, T5.EYE_TEST_RESULT2, T5.EYE_TEST_RESULT3 ");
            // 耳鼻科
            stb.append("  ,T5.NOSEDISEASECD,  (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F060' AND NAMECD2 = T5.NOSEDISEASECD) AS NOSEDISEASECD_NAME1 ");
            stb.append("  ,T5.NOSEDISEASECD2, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F060' AND NAMECD2 = T5.NOSEDISEASECD2) AS NOSEDISEASECD2_NAME1 ");
            stb.append("  ,T5.NOSEDISEASECD3, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F060' AND NAMECD2 = T5.NOSEDISEASECD3) AS NOSEDISEASECD3_NAME1 ");
            stb.append("  ,T5.NOSEDISEASECD4, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F060' AND NAMECD2 = T5.NOSEDISEASECD4) AS NOSEDISEASECD4_NAME1 ");
            stb.append("  ,T5.NOSEDISEASECD_REMARK ,T5.NOSEDISEASECD_REMARK2 ,T5.NOSEDISEASECD_REMARK3 ");
        } else if (check_no == check_no9) {//定期健康診断異常者一覧表
            stb.append("  ,CASE WHEN (ALBUMINURIA1CD > '01' AND ALBUMINURIA1CD <> '05') OR ");
            stb.append("             (URICSUGAR1CD > '01' AND URICSUGAR1CD <> '05') OR ");
            stb.append("             (URICBLEED1CD > '01' AND URICBLEED1CD <> '05') THEN '○' END AS URINE ");
            stb.append("  ,CASE WHEN TB_REMARKCD = '02' THEN '○' END AS TUBERCULOSIS ");
            stb.append("  ,CASE WHEN REMAINBABYTOOTH > 0 THEN REMAINBABYTOOTH END AS TEETH1 ");
            stb.append("  ,CASE WHEN REMAINADULTTOOTH > 0 THEN REMAINADULTTOOTH END AS TEETH2 ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F030' AND NAMECD2 > '01' AND NAMECD2=NUTRITIONCD) AS Eiyo_Jyotai ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F040' AND NAMECD2 > '01' AND NAMECD2=SPINERIBCD) AS Sekichu_Kyokaku ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F050' AND NAMECD2 > '01' AND NAMECD2=EYEDISEASECD) AS Meekibyo_Ijyo ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F060' AND NAMECD2 > '01' AND NAMECD2=NOSEDISEASECD) AS Jibi_Shikkan ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F070' AND NAMECD2 > '01' AND NAMECD2=SKINDISEASECD) AS Hifu_Shikkan ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F080' AND NAMECD2 > '01' AND NAMECD2=HEART_MEDEXAM) AS Kensa_Shinzo ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F090' AND NAMECD2 > '01' AND NAMECD2=HEARTDISEASECD) AS Ekibyo_Shinzo ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F140' AND NAMECD2 > '01' AND NAMECD2=T5.OTHERDISEASECD) AS Sonota ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F150' AND NAMECD2 IN ('04','05') AND NAMECD2=TREATCD) AS Hinketsu ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F510' AND NAMECD2 > '01' AND NAMECD2=JAWS_JOINTCD) AS Shiretsu_Ago ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F520' AND NAMECD2 > '01' AND NAMECD2=PLAQUECD) AS Shikou ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F513' AND NAMECD2 > '01' AND NAMECD2=GUMCD) AS Shiniku ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F530' AND NAMECD2 > '01' AND NAMECD2=T6.OTHERDISEASECD) AS Sonota_Ha ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F610' AND NAMECD2='"+param._select1+"') AS CHOICE ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F620' AND NAMECD2='"+param._select2+"') AS CONDITIONS ");
        } else if (check_no == check_no10) {//尿検査結果のお知らせ
            stb.append("  ,ALBUMINURIA1CD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA1CD) AS ALBUMINURIA1CD_NAME1 ");
            stb.append("  ,URICSUGAR1CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F019' AND NAMECD2=URICSUGAR1CD) AS URICSUGAR1CD_NAME1 ");
            stb.append("  ,URICBLEED1CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F018' AND NAMECD2=URICBLEED1CD) AS URICBLEED1CD_NAME1 ");
            stb.append("  ,ALBUMINURIA2CD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA2CD) AS ALBUMINURIA2CD_NAME1 ");
            stb.append("  ,URICSUGAR2CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F019' AND NAMECD2=URICSUGAR2CD) AS URICSUGAR2CD_NAME1 ");
            stb.append("  ,URICBLEED2CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F018' AND NAMECD2=URICBLEED2CD) AS URICBLEED2CD_NAME1 ");
            stb.append("  ,EYEDISEASECD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F050' AND NAMECD2=EYEDISEASECD) AS EYEDISEASECD_NAME1 ");
            stb.append("  ,NOSEDISEASECD,  (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F060' AND NAMECD2=NOSEDISEASECD) AS NOSEDISEASECD_NAME1 ");
        } else if (check_no == check_no12) {//聴力の検査結果のお知らせ
            stb.append("  ,R_EAR ");
            stb.append("  ,L_EAR ");
        } else if (check_no == check_no13) {//定期健康診断結果一覧
            //栄養状態
            stb.append("  ,NUTRITIONCD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F030' AND NAMECD2=NUTRITIONCD) AS NUTRITIONCD_NAME1 ");
            stb.append("  ,NUTRITIONCD_REMARK ");
            //脊柱・胸郭・四肢
            stb.append("  ,SPINERIBCD,     (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F040' AND NAMECD2=SPINERIBCD) AS SPINERIBCD_NAME1 ");
            stb.append("  ,SPINERIBCD_REMARK ");
            //目の疾病
            stb.append("  ,EYEDISEASECD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F050' AND NAMECD2=EYEDISEASECD) AS EYEDISEASECD_NAME1 ");
            stb.append("  ,EYE_TEST_RESULT ");
            //聴力
            stb.append("  ,R_EAR, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=R_EAR) AS R_EAR_NAME1 ");
            stb.append("  ,L_EAR, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F010' AND NAMECD2=L_EAR) AS L_EAR_NAME1 ");
            //耳鼻１
            stb.append("  ,NOSEDISEASECD,  (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F060' AND NAMECD2=NOSEDISEASECD) AS NOSEDISEASECD_NAME1 ");
            stb.append("  ,NOSEDISEASECD_REMARK ");
            //耳鼻２
            stb.append("  ,T5.NOSEDISEASECD2, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F060' AND NAMECD2 = T5.NOSEDISEASECD2) AS NOSEDISEASECD2_NAME1 ");
            stb.append("  ,T5.NOSEDISEASECD_REMARK2 ");
            //皮膚
            stb.append("  ,SKINDISEASECD,  (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F070' AND NAMECD2=SKINDISEASECD) AS SKINDISEASECD_NAME1 ");
            stb.append("  ,SKINDISEASECD_REMARK ");
            //結核
            stb.append("  ,TB_FILMDATE ");
            stb.append("  ,TB_REMARKCD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F100' AND NAMECD2=TB_REMARKCD) AS TB_REMARKCD_NAME1 ");
            stb.append("  ,TB_NAME_REMARK1 ");
            stb.append("  ,TB_OTHERTESTCD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F110' AND NAMECD2=TB_OTHERTESTCD) AS TB_OTHERTESTCD_NAME1 ");
            stb.append("  ,T5.TB_OTHERTEST_REMARK1 ");
            stb.append("  ,TB_NAMECD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F120' AND NAMECD2=TB_NAMECD) AS TB_NAMECD_NAME1 ");
            stb.append("  ,T5.TB_NAME_REMARK1 ");
            stb.append("  ,TB_ADVISECD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F130' AND NAMECD2=TB_ADVISECD) AS TB_ADVISECD_NAME1 ");
            stb.append("  ,T5.TB_ADVISE_REMARK1 ");
            //心臓
            stb.append("  ,HEART_MEDEXAM, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F080' AND NAMECD2=HEART_MEDEXAM) AS HEART_MEDEXAM_NAME1 ");
            stb.append("  ,HEART_MEDEXAM_REMARK ");
            stb.append("  ,HEARTDISEASECD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F090' AND NAMECD2=HEARTDISEASECD) AS HEARTDISEASECD_NAME1 ");
            stb.append("  ,HEARTDISEASECD_REMARK ");
            //その他疾病
            stb.append("  ,T5.OTHERDISEASECD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F140' AND NAMECD2=T5.OTHERDISEASECD) AS OTHERDISEASECD_NAME1 ");
            stb.append("  ,OTHER_REMARK ");
            //尿
            stb.append("  ,ALBUMINURIA1CD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA1CD) AS ALBUMINURIA1CD_NAME1 ");
            stb.append("  ,URICSUGAR1CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F019' AND NAMECD2=URICSUGAR1CD) AS URICSUGAR1CD_NAME1 ");
            stb.append("  ,URICBLEED1CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F018' AND NAMECD2=URICBLEED1CD) AS URICBLEED1CD_NAME1 ");
            stb.append("  ,ALBUMINURIA2CD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F020' AND NAMECD2=ALBUMINURIA2CD) AS ALBUMINURIA2CD_NAME1 ");
            stb.append("  ,URICSUGAR2CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F019' AND NAMECD2=URICSUGAR2CD) AS URICSUGAR2CD_NAME1 ");
            stb.append("  ,URICBLEED2CD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F018' AND NAMECD2=URICBLEED2CD) AS URICBLEED2CD_NAME1 ");
            stb.append("  ,URICOTHERTESTCD,   (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F022' AND NAMECD2=URICOTHERTESTCD) AS URICOTHERTESTCD_NAME1 ");
            //学校医
            stb.append("  ,DOC_DATE ");
            stb.append("  ,TREATCD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F150' AND NAMECD2=TREATCD) AS TREATCD_NAME1 ");
            stb.append("  ,TREAT_REMARK1 ");
            stb.append("  ,REMARK ");
            //歯科
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F515' AND NAMECD2=JAWS_JOINTCD2) AS JAWS_JOINTCD2_NAME1 ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F514' AND NAMECD2=JAWS_JOINTCD) AS JAWS_JOINTCD_NAME1_F514 ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F510' AND NAMECD2=JAWS_JOINTCD) AS JAWS_JOINTCD_NAME1_F510 ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F516' AND NAMECD2=PLAQUECD) AS PLAQUECD_NAME1_F516 ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F520' AND NAMECD2=PLAQUECD) AS PLAQUECD_NAME1_F520 ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F513' AND NAMECD2=GUMCD) AS GUMCD_NAME1_F513 ");
            stb.append("  ,(SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F517' AND NAMECD2=GUMCD) AS GUMCD_NAME1_F517 ");
            stb.append("  ,ORTHODONTICS ");
            stb.append("  ,BABYTOOTH, REMAINBABYTOOTH, TREATEDBABYTOOTH, BRACK_BABYTOOTH ");
            stb.append("  ,ADULTTOOTH, REMAINADULTTOOTH, TREATEDADULTTOOTH, LOSTADULTTOOTH, BRACK_ADULTTOOTH ");
            stb.append("  ,T6.OTHERDISEASECD as T_OTHERDISEASECD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F530' AND NAMECD2=T6.OTHERDISEASECD) AS T_OTHERDISEASECD_NAME1 ");
            stb.append("  ,T6.OTHERDISEASE as T_OTHERDISEASE ");
            stb.append("  ,DENTISTREMARK_CO, DENTISTREMARK_GO, DENTISTREMARK_G, DENTISTREMARKDATE ");
            stb.append("  ,T6.DENTISTTREATCD, (SELECT NAME1 FROM NAME_MST WHERE NAMECD1='F541' AND NAMECD2=T6.DENTISTTREATCD) AS DENTISTTREATCD_NAME1 ");
            stb.append("  ,T6.DENTISTTREAT ");

            String a;

        }
        stb.append("  , DET008.DET_REMARK1 AS DET008_REMARK1 ");
        stb.append("  , DET017.DET_REMARK1 AS DET017_REMARK1 ");
        stb.append("  , DET017.DET_REMARK2 AS DET017_REMARK2 ");
        stb.append("FROM   SCHNO T2 ");
        stb.append("       LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T2.YEAR AND T3.SEMESTER=T2.SEMESTER AND ");
        stb.append("                                        T3.GRADE=T2.GRADE AND T3.HR_CLASS=T2.HR_CLASS ");
        stb.append("       LEFT JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO=T2.SCHREGNO ");
        stb.append("       LEFT JOIN V_MEDEXAM_DET_DAT T5 ON T5.YEAR=T2.YEAR AND T5.SCHREGNO=T2.SCHREGNO ");
        stb.append("       LEFT JOIN MEDEXAM_TOOTH_DAT T6 ON T6.YEAR=T2.YEAR AND T6.SCHREGNO=T2.SCHREGNO ");
        stb.append("       LEFT JOIN MEDEXAM_HDAT T7 ON T7.YEAR=T2.YEAR AND T7.SCHREGNO=T2.SCHREGNO ");
        stb.append("       LEFT JOIN SCHREG_REGD_GDAT T8 ON T8.YEAR = T2.YEAR AND T8.GRADE = T2.GRADE ");
        stb.append("       LEFT JOIN MEDEXAM_DET_DETAIL_DAT DET008 ON DET008.YEAR = T2.YEAR AND DET008.DET_SEQ = '008' AND DET008.SCHREGNO = T2.SCHREGNO ");
        stb.append("       LEFT JOIN MEDEXAM_DET_DETAIL_DAT DET017 ON DET017.YEAR = T2.YEAR AND DET017.DET_SEQ = '017' AND DET017.SCHREGNO = T2.SCHREGNO ");
        stb.append("       LEFT JOIN MAJOR_MST MAJ ON MAJ.COURSECD = T2.COURSECD AND MAJ.MAJORCD = T2.MAJORCD ");
        if (check_no == check_no4) {//眼科検診のお知らせ
            stb.append("WHERE  T5.YEAR='"+param._year+"' AND ");
            if (param._isKumamoto) {
                // 裸眼・矯正視力ともにC以下(0.7未満)
                stb.append("      ( ");
                stb.append("       ((0 < LENGTH(RTRIM(R_BAREVISION)) AND R_BAREVISION < '0.7') AND ");
                stb.append("        (0 < LENGTH(RTRIM(L_BAREVISION)) AND L_BAREVISION < '0.7') AND ");
                stb.append("        (0 < LENGTH(RTRIM(R_VISION)) AND R_VISION < '0.7') AND ");
                stb.append("        (0 < LENGTH(RTRIM(L_VISION)) AND L_VISION < '0.7')) ");
                stb.append("       OR ");
                stb.append("       ((0 < LENGTH(RTRIM(R_BAREVISION_MARK)) AND R_BAREVISION_MARK <> 'A' AND R_BAREVISION_MARK <> 'B') AND ");
                stb.append("        (0 < LENGTH(RTRIM(L_BAREVISION_MARK)) AND L_BAREVISION_MARK <> 'A' AND L_BAREVISION_MARK <> 'B') AND ");
                stb.append("        (0 < LENGTH(RTRIM(R_VISION_MARK)) AND R_VISION_MARK <> 'A' AND R_VISION_MARK <> 'B') AND ");
                stb.append("        (0 < LENGTH(RTRIM(L_VISION_MARK)) AND L_VISION_MARK <> 'A' AND L_VISION_MARK <> 'B')) ) ");
            } else {
                stb.append("      (EYEDISEASECD >= '02' OR ");
                stb.append("       ((0 < LENGTH(RTRIM(R_BAREVISION)) AND R_BAREVISION < '1.0') OR ");
                stb.append("        (0 < LENGTH(RTRIM(L_BAREVISION)) AND L_BAREVISION < '1.0') OR ");
                stb.append("        (0 < LENGTH(RTRIM(R_VISION)) AND R_VISION < '1.0') OR ");
                stb.append("        (0 < LENGTH(RTRIM(L_VISION)) AND L_VISION < '1.0')) ");
                stb.append("       OR ");
                stb.append("       ((0 < LENGTH(RTRIM(R_BAREVISION_MARK)) AND R_BAREVISION_MARK <> 'A') OR ");
                stb.append("        (0 < LENGTH(RTRIM(L_BAREVISION_MARK)) AND L_BAREVISION_MARK <> 'A') OR ");
                stb.append("        (0 < LENGTH(RTRIM(R_VISION_MARK)) AND R_VISION_MARK <> 'A') OR ");
                stb.append("        (0 < LENGTH(RTRIM(L_VISION_MARK)) AND L_VISION_MARK <> 'A')) ) ");
            }
        } else if (check_no == check_no5 && param._isKumamoto) {//検診結果のお知らせ（歯・口腔）
            stb.append(" WHERE ");
            stb.append("       (REMAINBABYTOOTH  > 0 ");   //う歯（虫歯）乳歯
            stb.append("     OR REMAINADULTTOOTH > 0 ");   //う歯（虫歯）永久歯
            stb.append("     OR BRACK_ADULTTOOTH > 0 ");   //要観察歯
            stb.append("     OR BRACK_BABYTOOTH  > 0 ");   //要注意乳歯
            stb.append("     OR JAWS_JOINTCD2   = CASE WHEN FISCALYEAR(ENT_DATE) >= '2016' THEN '03' ELSE '04' END "); //顎関節異常２
            stb.append("     OR JAWS_JOINTCD    = CASE WHEN FISCALYEAR(ENT_DATE) >= '2016' THEN '03' ELSE '04' END "); //歯列異常２
            stb.append("     OR JAWS_JOINTCD3   = CASE WHEN FISCALYEAR(ENT_DATE) >= '2016' THEN '03' ELSE '04' END "); //咬合異常２
            stb.append("     OR PLAQUECD        = CASE WHEN FISCALYEAR(ENT_DATE) >= '2016' THEN '03' ELSE '04' END "); //歯垢異常２
            stb.append("     OR GUMCD           = CASE WHEN FISCALYEAR(ENT_DATE) >= '2016' THEN '03' ELSE '04' END "); //歯肉異常２
            stb.append("     OR T6.OTHERDISEASECD <> '01') "); //その他疾病及び異常（01 異常なしは除く）
        } else if (check_no == check_no7 || check_no == check_no8) {//検診結果のお知らせ（一般）or 内科検診所見あり生徒の名簿
            stb.append("WHERE  T5.YEAR='" + param._year + "' AND ");
            if (check_no == check_no7 && "1".equals(param._KenkouSindan_Ippan_Pattern)) {
                stb.append("   ((ALBUMINURIA1CD > '01' AND ALBUMINURIA1CD <> '98') OR (URICSUGAR1CD > '01' AND URICSUGAR1CD <> '98') OR (URICBLEED1CD > '01' AND URICBLEED1CD <> '98') OR (VALUE(DET017.DET_REMARK1,'') <> '' AND (VALUE(DET017.DET_REMARK1, 0) < 4.5 OR 8.0 < VALUE(DET017.DET_REMARK1, 0)))) ");
            } else {
                stb.append("      (SPINERIBCD >= '02' OR ");
                stb.append("       EYEDISEASECD >= '02' OR ");
                stb.append("       NOSEDISEASECD >= '02' OR ");
                if (check_no == check_no7) {
                    stb.append("   SKINDISEASECD >= '02' OR ");
                    stb.append("   TB_X_RAY IS NOT NULL OR ");
                    stb.append("   MANAGEMENT_REMARK IS NOT NULL OR ");
                } else if (check_no == check_no8) {
                    stb.append("   SKINDISEASECD IN('02','03') OR ");
                    stb.append("   HEARTDISEASECD >= '02' OR ");
                    stb.append("   T5.OTHERDISEASECD >= '02' OR ");
                }
                stb.append("       NUTRITIONCD >= '02' ) ");
            }
        } else if (check_no == check_no7Kuma) {//検診結果のお知らせ（一般）熊本
            stb.append("WHERE  T5.YEAR='"+param._year+"' ");
        } else if (check_no == check_no9) {//定期健康診断異常者一覧表
            stb.append("WHERE  T2.YEAR='"+param._year+"' ");
            // 一般条件
            if (!param._select1.equals("01")) {
                stb.append(" AND T5.YEAR='"+param._year+"' ");
            }
            if (param._select1.equals("02")) {
                // 02．異常者全部
                stb.append(" AND ((('0.0' < R_BAREVISION AND R_BAREVISION < '1.0') OR ('0.0' < L_BAREVISION AND L_BAREVISION < '1.0')) ");
                stb.append("   OR ((R_BAREVISION_MARK <> 'A') OR (L_BAREVISION_MARK <> 'A')) ");
                stb.append("   OR ((ALBUMINURIA1CD > '01' AND ALBUMINURIA1CD <> '05') OR (URICSUGAR1CD > '01' AND URICSUGAR1CD <> '05') OR (URICBLEED1CD > '01' AND URICBLEED1CD <> '05')) ");
                stb.append("   OR (TREATCD = '05') ");
                stb.append("   OR (TREATCD = '04') ");
                stb.append("   OR (TB_REMARKCD = '02') ");
                stb.append("   OR (HEART_MEDEXAM > '01') ");
                stb.append("   OR (HEARTDISEASECD > '01') ");
                stb.append("   OR (NUTRITIONCD > '01') ");
                stb.append("   OR (SPINERIBCD > '01') ");
                stb.append("   OR (EYEDISEASECD > '01') ");
                stb.append("   OR (NOSEDISEASECD > '01') ");
                stb.append("   OR (SKINDISEASECD > '01') ");
                stb.append("   OR (T5.OTHERDISEASECD > '01')) ");
            } else if (param._select1.equals("03")) {
                // 03．視力 0.9〜0.7
                stb.append(" AND (('0.7' <= R_BAREVISION AND R_BAREVISION < '1.0') ");
                stb.append("  OR  ('0.7' <= L_BAREVISION AND L_BAREVISION < '1.0') ");
                stb.append("  OR  ( R_BAREVISION_MARK = 'B') ");
                stb.append("  OR  ( L_BAREVISION_MARK = 'B')) ");
            } else if (param._select1.equals("04")) {
                // 04．視力 0.6〜0.3
                stb.append(" AND (('0.3' <= R_BAREVISION AND R_BAREVISION < '0.7') ");
                stb.append("  OR  ('0.3' <= L_BAREVISION AND L_BAREVISION < '0.7') ");
                stb.append("  OR  ( R_BAREVISION_MARK = 'C') ");
                stb.append("  OR  ( L_BAREVISION_MARK = 'C')) ");
            } else if (param._select1.equals("05")) {
                // 05．視力 0.2以下
                stb.append(" AND (('0.0' < R_BAREVISION AND R_BAREVISION < '0.3') ");
                stb.append("  OR  ('0.0' < L_BAREVISION AND L_BAREVISION < '0.3') ");
                stb.append("  OR  ( R_BAREVISION_MARK = 'D') ");
                stb.append("  OR  ( L_BAREVISION_MARK = 'D')) ");
            } else if (param._select1.equals("06")) {
                // 06．尿　陽性者
                stb.append(" AND ((ALBUMINURIA1CD > '01' AND ALBUMINURIA1CD <> '05') OR (URICSUGAR1CD > '01' AND URICSUGAR1CD <> '05') OR (URICBLEED1CD > '01' AND URICBLEED1CD <> '05')) ");
            } else if (param._select1.equals("07")) {
                // 07．貧血　要食事指導
                stb.append(" AND (TREATCD = '05') ");
            } else if (param._select1.equals("08")) {
                // 08．貧血　要治療
                stb.append(" AND (TREATCD = '04') ");
            } else if (param._select1.equals("09")) {
                // 09．結核　要再検者
                stb.append(" AND (TB_REMARKCD = '02') ");
            } else if (param._select1.equals("10")) {
                // 10．心臓　要再検者
                stb.append(" AND (HEART_MEDEXAM > '01' OR HEARTDISEASECD > '01') ");
            } else if (param._select1.equals("11")) {
                // 11．栄養状態異常
                stb.append(" AND (NUTRITIONCD > '01') ");
            } else if (param._select1.equals("12")) {
                // 12．脊柱・胸郭異常
                stb.append(" AND (SPINERIBCD > '01') ");
            } else if (param._select1.equals("13")) {
                // 13．目の疫病及び異常
                stb.append(" AND (EYEDISEASECD > '01') ");
            } else if (param._select1.equals("14")) {
                // 14．耳鼻異常
                stb.append(" AND (NOSEDISEASECD > '01') ");
            } else if (param._select1.equals("15")) {
                // 15．皮膚疾患異常
                stb.append(" AND (SKINDISEASECD > '01') ");
            } else if (param._select1.equals("16")) {
                // 16．その他の疫病及び異常
                stb.append(" AND (T5.OTHERDISEASECD > '01') ");
            }
            if (!param._select2.equals("01")) {
                // 歯・口腔条件
                stb.append(" AND T6.YEAR='"+param._year+"' ");
            }
            if (param._select2.equals("02")) {
                // 02．異常者全部
                stb.append(" AND ((REMAINBABYTOOTH > 0) ");
                stb.append("   OR (REMAINADULTTOOTH > 0) ");
                stb.append("   OR (JAWS_JOINTCD > '01') ");
                stb.append("   OR (PLAQUECD > '01') ");
                stb.append("   OR (GUMCD > '01') ");
                stb.append("   OR (T6.OTHERDISEASECD > '01')) ");
            } else if (param._select2.equals("03")) {
                // 03．'未処置
                stb.append(" AND (REMAINBABYTOOTH > 0 OR REMAINADULTTOOTH > 0) ");
            } else if (param._select2.equals("04") || param._select2.equals("05")) {
                // 04．05．'歯列・咬合・歯顎関節
                stb.append(" AND (JAWS_JOINTCD > '01') ");
            } else if (param._select2.equals("06")) {
                // 06．'歯垢状態
                stb.append(" AND (PLAQUECD > '01') ");
            } else if (param._select2.equals("07")) {
                // 07．'歯肉状態
                stb.append(" AND (GUMCD > '01') ");
            } else if (param._select2.equals("08")) {
                // 08．'歯その他疾病及異常
                stb.append(" AND (T6.OTHERDISEASECD > '01') ");
            }
        } else if (check_no == check_no11) {//視力の検査結果のお知らせ
            final String setMarkText = ("02".equals(param._sightCondition)) ? "A": "03".equals(param._sightCondition) ? "B": "";
            stb.append(" WHERE ");
            stb.append("        (R_VISION_MARK is not null AND R_VISION_MARK > '"+setMarkText+"') ");
            stb.append("     OR (L_VISION_MARK is not null AND L_VISION_MARK > '"+setMarkText+"') ");
            stb.append("     OR (R_VISION_MARK is null AND R_BAREVISION_MARK > '"+setMarkText+"') ");
            stb.append("     OR (L_VISION_MARK is null AND L_BAREVISION_MARK > '"+setMarkText+"') ");
        } else if (check_no == check_no12) {//聴力の検査結果のお知らせ
            stb.append(" WHERE ");
            stb.append("        R_EAR = '03' ");
            stb.append("     OR L_EAR = '03' ");
        }
        stb.append("ORDER BY T2.GRADE,T2.HR_CLASS,ATTENDNO ");
        return stb.toString();
    }

    private String statementMeijiResult(final Param param) {
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
        stb.append("    WHERE  T1.YEAR = '" + param._year + "' AND T1.SEMESTER = '" + param._gakki + "' ");
        if (param._kubun.equals("1")) { //1:クラス
            stb.append("       AND T1.GRADE || T1.HR_CLASS IN " + param._selectInState + " ");
        } else if (param._kubun.equals("2")) { //2:個人
            stb.append("       AND T1.SCHREGNO IN " + param._selectInState + " ");
        }
        stb.append("    ) ");
        //メイン
        stb.append("SELECT T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T5.SCHOOL_KIND, T1.ATTENDNO, T2.HR_NAME, T3.NAME, ");
        stb.append("       JAWS_JOINTCD, JAWS_JOINTCD2, PLAQUECD, GUMCD, CALCULUS, ORTHODONTICS, ");
        stb.append("       UP_R_ADULT8, UP_R_ADULT7, UP_R_ADULT6, UP_R_ADULT5, UP_R_ADULT4, UP_R_ADULT3, UP_R_ADULT2, UP_R_ADULT1, ");
        stb.append("       UP_L_ADULT1, UP_L_ADULT2, UP_L_ADULT3, UP_L_ADULT4, UP_L_ADULT5, UP_L_ADULT6, UP_L_ADULT7, UP_L_ADULT8, ");
        stb.append("       UP_R_BABY5, UP_R_BABY4, UP_R_BABY3, UP_R_BABY2, UP_R_BABY1, ");
        stb.append("       UP_L_BABY1, UP_L_BABY2, UP_L_BABY3, UP_L_BABY4, UP_L_BABY5, ");
        stb.append("       LW_R_BABY5, LW_R_BABY4, LW_R_BABY3, LW_R_BABY2, LW_R_BABY1, ");
        stb.append("       LW_L_BABY1, LW_L_BABY2, LW_L_BABY3, LW_L_BABY4, LW_L_BABY5, ");
        stb.append("       LW_R_ADULT8, LW_R_ADULT7, LW_R_ADULT6, LW_R_ADULT5, LW_R_ADULT4, LW_R_ADULT3, LW_R_ADULT2, LW_R_ADULT1, ");
        stb.append("       LW_L_ADULT1, LW_L_ADULT2, LW_L_ADULT3, LW_L_ADULT4, LW_L_ADULT5, LW_L_ADULT6, LW_L_ADULT7, LW_L_ADULT8, ");
        stb.append("       BABYTOOTH, REMAINBABYTOOTH, TREATEDBABYTOOTH, BRACK_BABYTOOTH, ");
        stb.append("       ADULTTOOTH, REMAINADULTTOOTH, TREATEDADULTTOOTH, LOSTADULTTOOTH, BRACK_ADULTTOOTH, CHECKADULTTOOTH,");
        stb.append("       OTHERDISEASECD, ");
        stb.append("       OTHERDISEASE ");
        stb.append("FROM   SCHNO T1 ");
        stb.append("       INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append("       INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("       LEFT JOIN SCHREG_REGD_GDAT T5 ON T5.YEAR = T1.YEAR AND T5.GRADE = T1.GRADE ");
        stb.append("   LEFT JOIN MEDEXAM_TOOTH_DAT T4 ON T4.YEAR = T1.YEAR AND T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("ORDER BY T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        return stb.toString();
    }

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
                    final String sex = rs.getString("SEX");
                    final int nenreiYear = rs.getInt("NENREI_YEAR");
                    final int nenreiMonth = rs.getInt("NENREI_MONTH");
                    // if (ageMonth % 3 != 0) { continue; }
                    final BigDecimal heightAvg = rs.getBigDecimal("HEIGHT_AVG");
                    final BigDecimal heightSd = rs.getBigDecimal("HEIGHT_SD");
                    final BigDecimal weightAvg = rs.getBigDecimal("WEIGHT_AVG");
                    final BigDecimal weightSd = rs.getBigDecimal("WEIGHT_SD");
                    final BigDecimal stdWeightKeisuA = rs.getBigDecimal("STD_WEIGHT_KEISU_A");
                    final BigDecimal stdWeightKeisuB = rs.getBigDecimal("STD_WEIGHT_KEISU_B");
                    final HexamPhysicalAvgDat testheightweight = new HexamPhysicalAvgDat(sex, nenreiYear, nenreiMonth, heightAvg, heightSd, weightAvg, weightSd, stdWeightKeisuA, stdWeightKeisuB);
                    if (null == m.get(rs.getString("SEX"))) {
                        m.put(rs.getString("SEX"), new ArrayList());
                    }
                    final List list = (List) m.get(rs.getString("SEX"));
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
            stb.append("   SELECT ");
            stb.append("       MAX(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR <= '" + param._year + "' ");
            stb.append(" ), MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR >= '" + param._year + "' ");
            stb.append(" ), MAX_MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(T1.YEAR) AS YEAR ");
            stb.append("   FROM ( ");
            stb.append("       SELECT YEAR FROM MAX_YEAR T1 ");
            stb.append("       UNION ");
            stb.append("       SELECT YEAR FROM MIN_YEAR T1 ");
            stb.append("   ) T1 ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SEX, ");
            stb.append("     T1.NENREI_YEAR, ");
            stb.append("     T1.NENREI_MONTH, ");
            stb.append("     T1.HEIGHT_AVG, ");
            stb.append("     T1.HEIGHT_SD, ");
            stb.append("     T1.WEIGHT_AVG, ");
            stb.append("     T1.WEIGHT_SD, ");
            stb.append("     T1.STD_WEIGHT_KEISU_A, ");
            stb.append("     T1.STD_WEIGHT_KEISU_B ");
            stb.append(" FROM ");
            stb.append("    HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("    INNER JOIN MAX_MIN_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SEX, T1.NENREI_YEAR, T1.NENREI_MONTH ");
            return stb.toString();
        }
    }

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
                    //log.info(" data = " + data + ", len = " + StringUtils.defaultString(data).length() + " / " + fields[i] + " : " + (svfField._fieldLength / 2));
                    if (StringUtils.defaultString(data).length() <= svfField._fieldLength / 2) {
                        selectField = fields[i];
                        break;
                    }
                } else { // 横
                    if (getMS932ByteLength(data) <= svfField._fieldLength) {
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
                    if (lineKeta < getMS932ByteLength(currentLine.toString() + chs)) {
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

    private void printSlash(final Param _param, final Form form, final String field) {
        final String slashStr  = (_param._isMiyagiken) ? "slash_bs.jpg": "slash.jpg";
        final String slashFile = _param.getImageFile(slashStr);
        if (null != slashFile) {
            form.VrsOut(field, slashFile);
        }
    }

    private class Param {
        final String _year;
        final String _gakki;
        final String _selectInState;
        final String _ctrlDate;
        final String _kubun;
        final String _schoolJudge;
        final String _outputA;
        final String _outputB;
        final String _date;
        final String _date5;
        final String _output;
        final String _select1;
        final String _select2;
        final String _check1;
        final String _check2;
        final String _check1_2;
        final String _check3;
        final String _check4;
        final String _check5;
        final String _check7;
        final String _date7;
        final String _standard_notshow;
        final String _urinalysis_check;
        final String _urinalysis_output;
        final String _familyContactComment;
        final String _documentCd;
        final String _check6;
        final String _check8;
        final String _check9;
        final String _check10;
        final String _check11;
        final String _check12;
        final String _check13;
        final String _sightCondition;
        final String _mijukenItem01; // 未受検項目 尿検査
        final String _mijukenItem02; // 未受検項目 貧血検査
        final String _mijukenItem03; // 未受検項目 内科（校医）検診
        final String _mijukenItem04; // 未受検項目 歯科検診
        final String _mijukenItem05; // 未受検項目 胸部レントゲン撮影
        final String _mijukenItem06; // 未受検項目 心電図検査
        final String _ctrlDateString;
        final String _printKenkouSindanIppan;
        final String _useParasite_P;
        final String _useParasite_J;
        final String _useParasite_H;
        final String _useForm9_PJ_Ippan;
        final String _useForm9_PJ_Ha;
        final String _useForm7_JH_Ippan;
        final String _useForm7_JH_Ha;
        final String _useForm5_H_Ha;
        final String _useForm5_H_Ippan;
        final String _knjf030PrintVisionNumber;
        final String _knjf030addBlankGradeColumn;
        final boolean _useMijukenDefault;
        private String _namemstZ010Name1;
        final boolean _isSeireki;
        final boolean _isKumamoto;
        final boolean _isMiyagiken;
        final boolean _isChiyodaKudan;
        final boolean _isMeiji;
        final boolean _isMieken;
        final boolean _isTokiwa;
        final boolean _isKindai;
        final boolean _isNishiyama;
        final String _printStamp;
        final String _printStamp2;
        final String _printSchregNo1;
        final String _printSchregNo2;
        private Map _yearKouiStampNo; // 学校医印鑑
        private Map _yearKouiStampNo2; // 学校医印鑑(歯・口腔用)
        private Map _yearRemark6Staffname; // 校長名
        private Map _nameMstMap;
        /** 写真データ格納フォルダ */
        private final String _imageDir;
        /** 写真データの拡張子 */
        private final String _imageExt;
        /** 陰影保管場所(陰影出力に関係する) */
        private final String _documentRoot;
        private KNJ_Schoolinfo.ReturnVal _returnval2;
        private String _certifSchoolDatSchoolName;
        private String _certifSchoolDatJobName;
        private String _certifSchoolDatPrincipalName;
        private String _certifSchoolDatRemark1;
        private String _certifSchoolDatRemark2;
        private String _certifSchoolDatRemark3;
        private String _certifSchoolDatRemark4;
        private String _certifSchoolDatRemark5;
        private String _certifSchoolDatRemark6;
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final String _use_prg_schoolkind;
        final String[] _selectSchoolKind;
        private Map _yearF242name1Map;
        final Map<String, PreparedStatement> _psMap = new HashMap();
        final Map<String, String> _minGradeMap; // 校種ごとの最小学年取得
        private String _currentForm;
        private String _time;

        final boolean _isOutputDebug;
        final boolean _isOutputDebugField;

        final String _KenkouSindan_Ippan_Pattern;

        private final Map _nameMstCache = new HashMap();

        Param(final DB2UDB db2, final HttpServletRequest request) {
            //  パラメータの取得
            _year = request.getParameter("YEAR");                        //年度
            _gakki = request.getParameter("GAKKI");                       //学期 1,2,3
            _kubun = request.getParameter("KUBUN");                       //1:クラス,2:個人
            _schoolJudge = request.getParameter("SCHOOL_JUDGE");                //H:高校、J:中学
            //学年・組or学籍番号
            String classcd[] = request.getParameterValues("CLASS_SELECTED");
            String _2_ = "(";
            for (int ia=0 ; ia<classcd.length ; ia++) {
                if (ia > 0) _2_ = _2_ + ",";
                if (_kubun.equals("1")) _2_ = _2_ + "'" + classcd[ia] + "'";
                if (_kubun.equals("2")) _2_ = _2_ + "'" + (classcd[ia]).substring(0,(classcd[ia]).indexOf("-")) + "'";
            }
            _2_ = _2_ + ")";
            _selectInState = _2_;
            _check1 = request.getParameter("CHECK1");     // １）生徒学生健康診断票（一般）
            _outputA = request.getParameter("OUTPUTA");     // 1:結果 2:フォーム
            _printSchregNo1 = request.getParameter("PRINT_SCHREGNO1"); // 学籍番号印字
            _check2 = request.getParameter("CHECK2");     // ２）生徒学生健康診断票（歯・口腔）
            _outputB = request.getParameter("OUTPUTB");     // 1:結果 2:フォーム
            _printSchregNo2 = request.getParameter("PRINT_SCHREGNO2"); // 学籍番号印字
            _check1_2 = request.getParameter("CHECK1_2"); // 健康診断票 両面印刷
            _check3 = request.getParameter("CHECK3");     // ３）健康診断の未受検項目のある生徒へ
            _check4 = request.getParameter("CHECK4");     // ４）眼科検診のお知らせ
            _date = request.getParameter("DATE");        // 学校への提出日
            _check5 = request.getParameter("CHECK5");     // ５）検診結果のお知らせ（歯・口腔）
            _date5 = request.getParameter("DATE5");        // ５）の作成日
            _check7 = request.getParameter("CHECK7");     // ６）定期健康診断結果
            _date7 = request.getParameter("DATE7");        // ６）の作成日
            _standard_notshow = request.getParameter("STANDARD_NOTSHOW"); // 標準体重・肥満度を出さない
            _urinalysis_check = request.getParameter("URINALYSIS_CHECK");   // ７）尿検査診断結果のお知らせ
            _urinalysis_output = request.getParameter("URINALYSIS_OUTPUT"); // 1:１次検査陽性
                                                                            // 2:２次検査受信者（１次検査結果含む）
            _familyContactComment = request.getParameter("FAMILY_CONTACT_COMMENT"); // 家庭連絡コメント記入
            _documentCd = request.getParameter("DOCUMENTCD"); // 家庭連絡コメントCD
            //※以下、「or」は、共通or熊本で指示画面表示番号を記載。
            _check6 = request.getParameter("CHECK6");     // ８or７）検診結果のお知らせ（一般）
            _output = request.getParameter("OUTPUT");      // 1:１人で１枚にまとめて出力
                                                            // 2:１人で各種類ごとに出力
            _check8 = request.getParameter("CHECK8");     // ９or８）内科検診所見あり生徒の名簿
            _check9 = request.getParameter("CHECK9");     // １０or９）定期健康診断異常者一覧表
            _check10 = request.getParameter("CHECK10");   // １１or１０）尿検査結果のお知らせ
            _select1 = request.getParameter("SELECT1");     // 一般条件
            _select2 = request.getParameter("SELECT2");    // 歯・口腔条件
            _check11 = request.getParameter("CHECK11");   // １２or１１）視力の検査結果のお知らせ
            _sightCondition = request.getParameter("SIGHT_CONDITION");   // 視力（裸眼及び矯正）条件
            _check12 = request.getParameter("CHECK12");   // １３or１２）聴力の検査結果のお知らせ
            _check13 = request.getParameter("CHECK13");   // １４or１３）定期健康診断結果一覧
            _mijukenItem01 = request.getParameter("MIJUKEN_ITEM01");
            _mijukenItem02 = request.getParameter("MIJUKEN_ITEM02");
            _mijukenItem03 = request.getParameter("MIJUKEN_ITEM03");
            _mijukenItem04 = request.getParameter("MIJUKEN_ITEM04");
            _mijukenItem05 = request.getParameter("MIJUKEN_ITEM05");
            _mijukenItem06 = request.getParameter("MIJUKEN_ITEM06");
            _printKenkouSindanIppan = request.getParameter("printKenkouSindanIppan"); // 1:Aパターン使用
            _useParasite_P = request.getParameter("useParasite_P");
            _useParasite_J = request.getParameter("useParasite_J"); // 中学校で寄生虫卵を使用するか
            _useParasite_H = request.getParameter("useParasite_H"); // 高校で寄生虫卵を使用するか
            _printStamp = request.getParameter("PRINT_STAMP");
            _printStamp2 = request.getParameter("PRINT_STAMP2");
            _useForm9_PJ_Ippan = request.getParameter("useForm9_PJ_Ippan"); // 健康診断票・小中学校で9年用フォームを使用するか
            _useForm9_PJ_Ha    = request.getParameter("useForm9_PJ_Ha");    // 健康診断票・小中学校で9年用フォームを使用するか
            _useForm7_JH_Ippan = request.getParameter("useForm7_JH_Ippan"); // 健康診断票・中学高校で7年用フォームを使用するか
            _useForm7_JH_Ha    = request.getParameter("useForm7_JH_Ha");    // 健康診断票・中学高校で7年用フォームを使用するか
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = StringUtils.split(request.getParameter("selectSchoolKind"), ":");
            _useForm5_H_Ha = request.getParameter("useForm5_H_Ha");
            _useForm5_H_Ippan = request.getParameter("useForm5_H_Ippan");
            _knjf030PrintVisionNumber = request.getParameter("knjf030PrintVisionNumber");
            _knjf030addBlankGradeColumn = request.getParameter("knjf030addBlankGradeColumn");
            _useMijukenDefault = useMijukenDefault(request);
            _namemstZ010Name1 = getNamemstZ010(db2);
            log.info(" _namemstZ010Name1 = " + _namemstZ010Name1);
            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ")));
            _nameMstMap = getNameMstMap(db2);
            _isKumamoto = "kumamoto".equals(_namemstZ010Name1);
            _isMiyagiken = "miyagiken".equals(_namemstZ010Name1);
            _isMeiji = "meiji".equals(_namemstZ010Name1);
            _isMieken = "mieken".equals(_namemstZ010Name1);
            _isTokiwa = "tokiwa".equals(_namemstZ010Name1);
            _isChiyodaKudan = "chiyoda".equals(_namemstZ010Name1);
            _isKindai = ("KINDAI".equals(_namemstZ010Name1) || "KINJUNIOR".equals(_namemstZ010Name1));
            _isNishiyama = "nishiyama".equals(_namemstZ010Name1);
            _minGradeMap = getMinGradeMap(db2);
            _yearF242name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT YEAR, NAME1 FROM V_NAME_MST WHERE NAMECD1 = 'F242' AND NAMECD2 = '01' "), "YEAR", "NAME1");

            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            //  作成日(現在処理日)
            if (null != request.getParameter("CTRL_DATE")) {
                _ctrlDate = request.getParameter("CTRL_DATE");
            } else {
                returnval = getinfo.Control(db2);
                _ctrlDate = returnval.val3;
            }
            _ctrlDateString = formatDate(db2, this, _ctrlDate);

            //  学校名・学校住所・校長名の取得
            KNJ_Schoolinfo schoolinfo = new KNJ_Schoolinfo(_year);   //取得クラスのインスタンス作成
            _returnval2 = schoolinfo.get_info(db2);

            getinfo = null;
            returnval = null;

            _documentRoot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所 NO001
            _imageDir = "image/stamp";
            _imageExt = "bmp";
            setCertifSchoolDat(db2);

            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugField = ArrayUtils.contains(outputDebug, "field");

            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            DecimalFormat df = new DecimalFormat("00");
            _time = df.format(hour)+"時"+df.format(minute)+"分";

            _KenkouSindan_Ippan_Pattern = request.getParameter("KenkouSindan_Ippan_Pattern"); //パターン
        }

        private String getSchoolInfo(final String schoolKind, final String field) {
            final Map map = new HashMap();
            if ("H".equals(schoolKind)) {
                map.put(SCHOOL_NAME1, _certifSchoolDatSchoolName); //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDatSchoolName); //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDatPrincipalName); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatJobName);
            } else if ("J".equals(schoolKind)) {
                map.put(SCHOOL_NAME1, _certifSchoolDatRemark1); //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDatRemark1); //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDatRemark2); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatRemark3);
            } else if ("P".equals(schoolKind) || "K".equals(schoolKind)) {
                map.put(SCHOOL_NAME1, _certifSchoolDatRemark4); //学校名１
                map.put(SCHOOL_NAME2, _certifSchoolDatRemark4); //学校名２
                map.put(PRINCIPAL_NAME, _certifSchoolDatRemark5); //校長名
                map.put(PRINCIPAL_JOBNAME, _certifSchoolDatRemark6);
            }
            if (null == map.get(SCHOOL_NAME1)) map.put(SCHOOL_NAME1, _returnval2.SCHOOL_NAME1); //学校名１
            if (null == map.get(SCHOOL_NAME2)) map.put(SCHOOL_NAME2, _returnval2.SCHOOL_NAME2); //学校名２
            if (null == map.get(PRINCIPAL_NAME)) map.put(PRINCIPAL_NAME, _returnval2.PRINCIPAL_NAME); //校長名
            if (null == map.get(PRINCIPAL_JOBNAME)) map.put(PRINCIPAL_JOBNAME, _returnval2.PRINCIPAL_JOBNAME);
            return (String) map.get(field);
        }

        // 画面に指定項目がなければデフォルトの項目を使用する
        private boolean useMijukenDefault(final HttpServletRequest request) {
            for (final Enumeration enums = request.getParameterNames(); enums.hasMoreElements();) {
                final String parameterName = (String) enums.nextElement();
                if ("MIJUKEN_ITEM01".equals(parameterName)
                 || "MIJUKEN_ITEM02".equals(parameterName)
                 || "MIJUKEN_ITEM03".equals(parameterName)
                 || "MIJUKEN_ITEM04".equals(parameterName)
                 || "MIJUKEN_ITEM05".equals(parameterName)
                 || "MIJUKEN_ITEM06".equals(parameterName)) {
                    return false;
                }

            }
            return true;
        }

        private String getNamemstZ010(final DB2UDB db2) {
            String namemstZ010Name1 = "";
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    namemstZ010Name1 = rs.getString("NAME1");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
            return namemstZ010Name1;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _yearKouiStampNo = new HashMap();
            _yearKouiStampNo2 = new HashMap();
            _yearRemark6Staffname = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH T_INKAN AS ( ");
                stb.append("     SELECT ");
                stb.append("         MAX(STAMP_NO) AS STAMP_NO, ");
                stb.append("         STAFFCD ");
                stb.append("     FROM ");
                stb.append("         ATTEST_INKAN_DAT ");
                stb.append("     GROUP BY ");
                stb.append("         STAFFCD ");
                stb.append(" ) ");
                stb.append(" SELECT T1.YEAR, T1.REMARK5, T2.STAMP_NO, T1.REMARK6, T3.STAFFNAME AS REMARK6_STAFFNAME ");
                stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
                stb.append(" LEFT JOIN T_INKAN T2 ON T2.STAFFCD = T1.REMARK5 ");
                stb.append(" LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = T1.REMARK6 ");
                stb.append(" WHERE CERTIF_KINDCD = '124' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _yearKouiStampNo.put(rs.getString("YEAR"), rs.getString("STAMP_NO"));
                    _yearRemark6Staffname.put(rs.getString("YEAR"), rs.getString("REMARK6_STAFFNAME"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH T_INKAN AS ( ");
                stb.append("     SELECT ");
                stb.append("         MAX(STAMP_NO) AS STAMP_NO, ");
                stb.append("         STAFFCD ");
                stb.append("     FROM ");
                stb.append("         ATTEST_INKAN_DAT ");
                stb.append("     GROUP BY ");
                stb.append("         STAFFCD ");
                stb.append(" ) ");
                stb.append(" SELECT T1.YEAR, T1.REMARK10, T2.STAMP_NO, T1.REMARK6, T3.STAFFNAME AS REMARK6_STAFFNAME ");
                stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
                stb.append(" LEFT JOIN T_INKAN T2 ON T2.STAFFCD = T1.REMARK10 ");
                stb.append(" LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = T1.REMARK6 ");
                stb.append(" WHERE CERTIF_KINDCD = '124' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _yearKouiStampNo2.put(rs.getString("YEAR"), rs.getString("STAMP_NO"));
                    //_yearRemark6Staffname.put(rs.getString("YEAR"), rs.getString("REMARK6_STAFFNAME"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT * ");
                stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
                stb.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '125' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _certifSchoolDatSchoolName = rs.getString("SCHOOL_NAME");
                    _certifSchoolDatJobName = rs.getString("JOB_NAME");
                    _certifSchoolDatPrincipalName = rs.getString("PRINCIPAL_NAME");
                    _certifSchoolDatRemark1 = rs.getString("REMARK1");
                    _certifSchoolDatRemark2 = rs.getString("REMARK2");
                    _certifSchoolDatRemark3 = rs.getString("REMARK3");
                    _certifSchoolDatRemark4 = rs.getString("REMARK4");
                    _certifSchoolDatRemark5 = rs.getString("REMARK5");
                    _certifSchoolDatRemark6 = rs.getString("REMARK6");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 写真データファイルの取得
         */
        private String getStampImageFile(final String year, final String stampType) {
            if (null == year) {
                return null;
            }
            if (null == _documentRoot) {
                return null;
            } // DOCUMENTROOT
            if (null == _imageDir) {
                return null;
            }
            if (null == _imageExt) {
                return null;
            }
            final String filename = (String) ("2".equals(stampType) ?  _yearKouiStampNo2.get(year) : _yearKouiStampNo.get(year));
            if (null == filename) {
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(_documentRoot);
            stb.append("/");
            stb.append(_imageDir);
            stb.append("/");
            stb.append(filename);
            stb.append(".");
            stb.append(_imageExt);
            File file1 = new File(stb.toString());
            if (!file1.exists()) {
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
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

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJF030' AND NAME = '" + propName + "' "));
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

    private void putGengou1(final DB2UDB db2, final Vrw32alp svf, final String field, final Param param) {
        //元号(記入項目用)
        String[] dwk;
        if (param._ctrlDate.indexOf('/') >= 0) {
            dwk = StringUtils.split(param._ctrlDate, '/');
        } else if (param._ctrlDate.indexOf('-') >= 0) {
            dwk = StringUtils.split(param._ctrlDate, '-');
        } else {
            //ありえないので、固定値で設定。
            dwk = new String[1];
        }
        if (dwk.length >= 3) {
            final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(dwk[0]), Integer.parseInt(dwk[1]), Integer.parseInt(dwk[2]));
            svf.VrsOut(field, gengou);
        }
    }

}//クラスの括り

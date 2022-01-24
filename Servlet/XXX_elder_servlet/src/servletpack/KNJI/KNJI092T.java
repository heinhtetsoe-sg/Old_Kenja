// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2004/10/08 13:22:00 - JST
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.Date;
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;


/**
 *
 *  学校教育システム 賢者 [卒業生管理]
 *
 *                  ＜ＫＮＪＩ０９２Ｔ＞  卒業生台帳
 *
 *  2004/10/08
 *  2004/12/08 yamashiro 名簿の年・組の見出しを算用数字に変更
 *  2004/12/18 yamashiro 証書番号をテーブルから取得する場合、後ブランクを削除する
 *                       卒業生フラグを必ず条件とする => 処理年度とcontrol_mstの年度の比較をしない
 *  2004/12/21 yamashiro 西暦生年月日において元号が出力される不具合を修正
 *                       複数組出力した際、組の最終頁が出力されない不具合を修正
 *  2005/10/11 yamashiro 指示画面の卒業見込みがチェックされておれば今学期の3年生で除籍区分がnullの生徒を出力
 *  2005/10/13 yamashiro 出席番号の表記を追加(残?16)。  組・担任名の表記を大きくするに伴う修正(残?17)
 *                       生年月日において'元年'が出力されない不具合を修正
 *  2005/10/17 yamashiro MAJOR_MSTのリンクを修正
 */

public class KNJI092T {

    private static final Log log = LogFactory.getLog(KNJI092T.class);

    private Param param;
    private boolean nonedata;

    private static String arraykansuuji[] = {"〇","一","二","三","四","五","六","七","八","九","十"};

    private final java.text.SimpleDateFormat _sdf = new java.text.SimpleDateFormat("yyyy年M月d日");

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス

        // print svf設定
        response.setContentType("application/pdf");
        svf.VrInit();                                           //クラスの初期化
        try {
            svf.VrSetSpoolFileStream(response.getOutputStream());       //PDFファイル名の設定
        } catch (Exception ex) {
            log.error("db new error:", ex);
        }

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception e) {
            log.error("exception!", e);
            if (db2 != null) {
                db2.close();
            }
            return;
        }
        // パラメータの取得
        param = getParam(request, db2);

        try {
            // 印刷処理
            if (param.isTate()) {
                final FormTate form = new FormTate();
                form.knj = this;
                form.hmm = new HashMap();    //組アルファベットの変換用
                final String obj1[] = {"J","P","Q","S","A","B","C","D"};
                final String obj2[] = {"Ｊ","Ｐ","Ｑ","Ｓ","Ａ","Ｂ","Ｃ","Ｄ"};
                for (int i = 0; i < obj1.length; i++) {
                    form.hmm.put(obj1[i], obj2[i]);
                }

                // 縦書き
                if (param._output1 != null) {
                    form.printHyoushiTate(db2, svf);            //表紙を印刷
                }

                if (param._output2 != null) {
                    form.printMeiboTate(db2, svf);         //名簿を印刷
                }
            } else {
                printMeiboYoko(db2, svf);         //名簿を印刷
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            // 終了処理
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }   //doGetの括り

    private class FormTate {
        private KNJI092T knj;
        private final int COUNT = 15;
        private String ASTAR = "*";
        private Map hmm;
        private String FORM1 = "KNJI092_1.frm"; // 表紙 縦
        private String FORM2 = "KNJI092_2.frm"; // 名簿 縦

        /**
         *  svf print 表紙印刷
         */
        private void printHyoushiTate(final DB2UDB db2, final Vrw32alp svf) {
            PreparedStatement ps1 = null;
            ResultSet rs = null;
            try{
                svf.VrSetForm(FORM1, 1);
                final String sql = sqlHyoushi(param);
                log.debug(" sql = " + sql);
                ps1 = db2.prepareStatement(sql);
                rs = ps1.executeQuery();
                int count = 0;
                while (rs.next()) {
                    if ("ZZZ".equals(rs.getString("HR_CLASS"))) {
                        printHyoushiSvf(svf, rs.getString("CNT_SCH"), rs.getString("CNT_SCH2"), rs.getString("MIN_GRD_NO"), rs.getString("MAX_GRD_NO"));
                    } else {
                        count++;
                        printHyoushiSvf(db2, svf, rs.getString("HR_CLASS"), rs.getString("HR_CLASS_NAME1"), rs.getString("STAFFNAME"), rs.getString("CNT_SCH"), rs.getString("CNT_SCH2"), count);
                    }
                    if (count == 30) {
                        svf.VrsOut("TITLE", param._certifSchoolDatRemark10);
                        svf.VrsOut("SCHOOL_NAME", param.getSchoolName(null));
                        svf.VrEndPage();
                        knj.nonedata = true;
                        count = 0;
                    }
                }
                if (count > 0) {
                    svf.VrsOut("TITLE", param._certifSchoolDatRemark10);
                    svf.VrsOut("SCHOOL_NAME", param.getSchoolName(null));
                    svf.VrEndPage();
                    knj.nonedata = true;
                }
            } catch (Exception ex) {
                log.error("printHyoushi error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps1, rs);
                db2.commit();
            }
        }


        /**
         *  svf print 表紙.合計出力
         */
        private void printHyoushiSvf(final Vrw32alp svf, final String cntSch, final String cntSch2, final String minGrdNo, final String maxGrdNo) {
            try{
                if (cntSch != null) {
                    svf.VrsOut("TOTAL_NUMBER", cntSch);
                }
                if (cntSch2 != null) {
                    svf.VrsOut( "TOTAL_GIRL", "(" + cntSch2 + ")");
                }
                final String text;
                if (minGrdNo != null) {
                    final String sno = insertBlankMoji(new StringBuffer().append(convertKansuuji(minGrdNo)));
                    final String eno = insertBlankMoji(new StringBuffer().append(convertKansuuji(maxGrdNo)));

                    text = new StringBuffer().append("第").append(sno).append("号から").append("第").append(eno).append("号").toString();
                } else {
                    text = "第      号から第      号";
                }
                svf.VrsOut("CERTIF_NO", text);
            } catch (Exception ex) {
                log.error("printHyoushiSvf.total error!", ex);
            }
        }


        /**
         *  svf print 表紙.明細出力
         */
        private void printHyoushiSvf(final DB2UDB db2, final Vrw32alp svf, final String hrClass, final String hrClassName1, final String staffName,
                final String cntSch, final String cntSch2, final int count) {
            try{
                if (count == 1) {
                    svf.VrsOut("NENDO" , param.getNendo(db2, param));
                }
                final int kurikaeshi = Math.abs(((count > COUNT) ? count - COUNT : count) - (COUNT + 1));
                final String pn = (count > COUNT) ? "2" : "1";
                final String hrName;
                if (null != hrClassName1) {
                    hrName = hrClassName1;
                } else if (NumberUtils.isDigits(hrClass)) {
                    hrName = String.valueOf(Integer.parseInt(hrClass));
                } else {
                    hrName = hrClass;
                }
                svf.VrsOutn("HR_CLASS"  + pn, kurikaeshi, hrName);
                if (staffName != null) {
                    svf.VrsOutn("NAME" + pn, kurikaeshi, staffName);
                }
                if (cntSch != null) {
                    svf.VrsOutn("NUMBER" + pn, kurikaeshi, cntSch);
                }
                if (cntSch2!= null) {
                    svf.VrsOutn("GIRL" + pn, kurikaeshi, "(" + cntSch2 + ")");
                }
            } catch (Exception ex) {
                log.error("printHyoushiSvf.total error!", ex);
            }
        }

        /**
         *  svf print 名簿印刷処理
         */
        private void printMeiboTate(final DB2UDB db2, final Vrw32alp svf) {
            if (param._isPrintGakka) {
                final List majorCdNameList = new ArrayList();
                PreparedStatement ps1 = null;
                ResultSet rs1 = null;
                try{
                    final String sql1 = sqlMeiboHeaderGakka(param._year, param._semester, param._gradeHrClass);
                    ps1 = db2.prepareStatement(sql1);
                    rs1 = ps1.executeQuery();

                    while (rs1.next()) {
                        /* クラス見出しのセット => 課程から卒業日まで一つの文字列に編集後、組をスペースに置き換え、縦で出力する。
                                                   組は文字列を別にして横に出力する。その際、上述文字列の組位置を算出して出力する。
                        */
                        final String majorCd = rs1.getString("MAJORCD");
                        final String majorName = rs1.getString("MAJORNAME"); //クラス見出し 2004/12/09Modify
                        majorCdNameList.add(new String[]{majorCd, majorName});
                    }
                } catch (Exception ex) {
                    log.error("printMeiboGakka error!", ex);
                } finally{
                    DbUtils.closeQuietly(null, ps1, rs1);
                    db2.commit();
                }

                try{
                    svf.VrSetForm(FORM2, 1);
                    final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(param._year));
                    for (Iterator it = majorCdNameList.iterator(); it.hasNext();) {
                        final String[] majorCdName = (String[]) it.next();
                        final String majorCd = majorCdName[0];
                        final String majorName = majorCdName[1]; //クラス見出し

                        final String meiboheader = majorName; //クラス見出し
                        final String sql2 = sqlMeiboMeisaiGakka(param);
                        PreparedStatement ps2 = null;
                        ResultSet rs2 = null;
                        int count = 0;

                        try {
                            ps2 = db2.prepareStatement(sql2);
                            ps2.setString(1, majorCd);
                            rs2 = ps2.executeQuery();
                            String grade = null;
                            while (rs2.next()) {
                                count += 1;
                                final boolean useRealName = "1".equals(rs2.getString("USE_REAL_NAME"));
                                final String realName = rs2.getString("REAL_NAME");
                                final String realNameKana = rs2.getString("REAL_NAME_KANA");
                                final String nameOutputFlg = rs2.getString("NAME_OUTPUT_FLG");
                                final String name = rs2.getString("NAME");
                                final String nameKana = rs2.getString("NAME_KANA");
                                final String sex = rs2.getString("SEX_NAME");
                                final Date dbirthday = rs2.getDate("BIRTHDAY");
                                final String birthdiv = rs2.getString("BIRTHDIV");
                                final String grdNo = rs2.getString("GRD_NO");
                                grade = rs2.getString("GRADE");
                                if (count == 1) {
                                    svf.VrsOut("NENDO", nendo + "年度");
                                    svf.VrsOut("HR_CLASS", meiboheader);
                                    svf.VrsOut("GRADUATE", "（" + param._graduateDate + " 卒業）");
                                }

                                final int column = Math.abs(count - (12 + 1));
                                if (grdNo != null) {
                                    svf.VrsOutn("CERTIFNO", column, "第" + insertBlankMoji(new StringBuffer().append(convertKansuuji(grdNo))) + "号");
                                }
                                if (param._output3 != null) {
                                    svf.VrsOutn("REMARK", column, sex);
                                }

                                String kana = useRealName ? realNameKana : nameKana;
                                if (null != kana && kana.length() > 25) {
                                	kana = kana.substring(0, 25);
                                }
                                svf.VrsOutn("KANA" + (kana != null && kana.length() < 14 ? "" : kana != null && kana.length() < 20 ? "2" : "3"), column, kana);
                                printNameTate(svf, param, column, name, null, realName, null, useRealName, nameOutputFlg, 10, 10, 20);

                                final String[] arrstr = arrayBirth(db2, dbirthday, (birthdiv != null ? 1 : 0));
                                if (arrstr != null) {
                                    printBirthday(svf, column, arrstr);
                                }

                                log.debug("count = " + count + " name = " + name);
                                if (count == 12) {
                                    svf.VrsOut("TITLE", param._certifSchoolDatRemark10);
                                    svf.VrsOut("SCHOOL_NAME", param.getSchoolName(grade));
                                    svf.VrEndPage();
                                    knj.nonedata = true;
                                    count = 0;
                                }
                            }

                            if (count > 0) {
                                svf.VrsOut("TITLE", param._certifSchoolDatRemark10);
                                svf.VrsOut("SCHOOL_NAME", param.getSchoolName(grade));
                                svf.VrEndPage();
                                svf.VrPrint();
                                knj.nonedata = true;
                            }
                        } catch (SQLException e) {
                            log.error("exception!", e);
                        } finally {
                            DbUtils.closeQuietly(null, ps2, rs2);
                            db2.commit();
                        }
                    }
                } catch (Exception ex) {
                    log.error("printMeiboGakka error!", ex);
                }
            } else {
                final int HR_CLASS_FIELD_SIZE = 51;
                final List list = new ArrayList();
                PreparedStatement ps1 = null;
                ResultSet rs1 = null;
                try{
                    ps1 = db2.prepareStatement(sqlMeiboHeader(param));
                    rs1 = ps1.executeQuery();

                    while (rs1.next()) {
                        /* クラス見出しのセット => 課程から卒業日まで一つの文字列に編集後、組をスペースに置き換え、縦で出力する。
                                                   組は文字列を別にして横に出力する。その際、上述文字列の組位置を算出して出力する。
                        */

                        final String majorName = rs1.getString("MAJORNAME");
                        final String staffname = rs1.getString("STAFFNAME");
                        final String rs1Grade = rs1.getString("GRADE");
                        final String rs1GradeCd = rs1.getString("GRADE_CD");
                        final String gradehrclass = rs1.getString("GRADE_HR_CLASS");
                        final String hrClass = rs1.getString("HR_CLASS");

                        String meiboheader = getMeiboHeaderSec(majorName, staffname, rs1Grade, rs1GradeCd, gradehrclass, hrClass); //クラス見出し
                        int meiboheaderpoint = 0;
                        Boolean useReplaceHrClass = Boolean.FALSE;
                        //鳥取はクラスは表示しない
                        if (!param._isTottori) {
                            //HR_CLASS2の出力位置設定
                            meiboheaderpoint = null == meiboheader ? 0 : Math.max(HR_CLASS_FIELD_SIZE - meiboheader.length() + meiboheader.indexOf(ASTAR) + 1, 0);
                            int idxAstar = meiboheader.indexOf(ASTAR);                            //SVF-FIELD HR_CLASS2 での組の位置を取得
                            if (idxAstar != -1) {
                                String str1 = meiboheader.substring(0, idxAstar) + " " + meiboheader.substring(idxAstar + 1);  // '*'をスペースに置き換える
                                meiboheader = str1;                                         //クラス見出しを置き換える
                                useReplaceHrClass = Boolean.TRUE;
                            }
                        }
                        list.add(new Object[]{meiboheader, rs1.getString("GRADE_HR_CLASS"), new Integer(meiboheaderpoint), useReplaceHrClass});
                    }
                } catch (Exception ex) {
                    log.error("printMeibo error!", ex);
                } finally {
                    DbUtils.closeQuietly(null, ps1, rs1);
                    db2.commit();
                }

                try{
                    PreparedStatement ps2 = null;
                    ResultSet rs2 = null;
                    svf.VrSetForm(FORM2, 1);
                    String grade = null;
                    final String sql = sqlMeiboMeisai(param);
                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final Object[] headerGradehrclass = (Object[]) it.next();
                        final String meiboheader = (String) headerGradehrclass[0];
                        final String gradehrClass = (String) headerGradehrclass[1];
                        final int meiboheaderpoint = ((Integer) headerGradehrclass[2]).intValue();
                        final Boolean useReplaceHrClass = (Boolean) headerGradehrclass[3];

                        try {
                            ps2 = db2.prepareStatement(sql);
                            ps2.setString(1, gradehrClass);  //組
                            rs2 = ps2.executeQuery();
                            int count = 0;
                            while (rs2.next()) {

                                final boolean useRealName = "1".equals(rs2.getString("USE_REAL_NAME"));
                                final String realName = rs2.getString("REAL_NAME");
                                final String realNameKana = rs2.getString("REAL_NAME_KANA");
                                final String nameOutputFlg = rs2.getString("NAME_OUTPUT_FLG");
                                final String hrclass= rs2.getString("HR_CLASS");
                                final String grdNo = rs2.getString("GRD_NO");
                                final String nameKana = rs2.getString("NAME_KANA");
                                final String abbv1 = rs2.getString("ABBV1");
                                final String attendno = rs2.getString("ATTENDNO");
                                final String name = rs2.getString("NAME");
                                final Date birthday= rs2.getDate("BIRTHDAY");
                                final String birthdiv = rs2.getString("BIRTHDIV");
                                final String gradehrclass = rs2.getString("GRADE_HR_CLASS");
                                grade = rs2.getString("GRADE");

                                count++;
                                printMeiboMeisaiTateSvf(db2, svf, hrclass, grdNo, nameKana, abbv1, attendno, name, birthday, birthdiv, gradehrclass,
                                        useRealName, realName, realNameKana, nameOutputFlg,
                                        count, meiboheader, meiboheaderpoint, useReplaceHrClass); //生徒名簿を出力するメソッド
                                log.debug("count = " + count + "   name = " + rs2.getString("NAME"));
                                if (count == 12) {
                                    svf.VrsOut("TITLE", param._certifSchoolDatRemark10);
                                    svf.VrsOut("SCHOOL_NAME", param.getSchoolName(grade));
                                    svf.VrEndPage();
                                    knj.nonedata = true;
                                    count = 0;
                                }
                            }
                            if (count > 0) {
                                svf.VrsOut("TITLE", param._certifSchoolDatRemark10);
                                svf.VrsOut("SCHOOL_NAME", param.getSchoolName(grade));
                                svf.VrEndPage();
                                knj.nonedata = true;
                            }
                        } catch (Exception e) {
                            log.error("exception!", e);
                        } finally {
                            DbUtils.closeQuietly(null, ps2, rs2);
                            db2.commit();
                        }
                    }
                } catch (Exception ex) {
                    log.error("printMeibo error!", ex);
                }
            }
        }

        /**
         *   svf print 名簿.組見出し取得
         *             年、組を漢数字ではなく算用数字で出力
         */
        private String getMeiboHeaderSec(final String majorName, final String staffname, final String rsGrade, final String rsGradeCd,
                final String gradehrclass, final String hrClass)
        {
            final StringBuffer stb = new StringBuffer();
            try{
                if (majorName != null) {
                    stb.append(majorName).append(" ");
                }
                //鳥取はクラスは表示しない
                if (!param._isTottori) {
                    if (StringUtils.isNumeric(rsGradeCd)) {
                        stb.append("第").append(rsGradeCd).append("学年");
                    } else if (StringUtils.isNumeric(rsGrade)) {
                        stb.append("第").append(rsGrade).append("学年");
                    }

                    String hrName = null;
                    if (param._useSchregRegdHdat) {
                        if (param.getGradeHrclassName2(gradehrclass) != null && false) {
                            hrName = param.getGradeHrclassName2(gradehrclass);
                        } else if (param.getGradeHrclassName(gradehrclass) != null) {
                            final String name1 = param.getGradeHrclassName(gradehrclass);
                            hrName = null == name1 ? null : (name1 + "組");
                        }
                    } else if (param.getA021Hrname(hrClass) != null) {
                        hrName = param.getA021Hrname(hrClass);
                    }

                    if (hrName != null && !"".equals(hrName)) {
                        stb.append(hrName).append(" ");
                    } else {
                        stb.append(ASTAR).append("組").append(" ");
                    }
                    log.debug(" hrName = " + stb.toString());
                }
                if (staffname != null) {
                    stb.append("担任名 ").append(staffname).append(" ");
                }
            } catch (Exception ex) {
                log.error("getMeiboHeader error!", ex);
            }
            return stb.toString();
        }

        /**
         *  svf print 名簿.明細
         *            引数について int count : 出力済生徒数
         *                         String meiboheader : 課程から卒業日までのページ見出し
         *                         int meiboheaderpoint : ページ見出しにおける組の位置
         */
        private void printMeiboMeisaiTateSvf(final DB2UDB db2, final Vrw32alp svf, final String hrclass, final String grdNo, final String nameKana, final String abbv1,
                final String attendno, final String name,
                final Date birthday, final String birthdiv, final String gradehrclass,
                final boolean useRealName, final String realName, final String realNameKana, final String nameOutputFlg,
                final int count, final String meiboheader, final int meiboheaderpoint, final Boolean useReplaceHrClass) {
            final StringBuffer stb = new StringBuffer();
            try{
                if (count == 1) {
                    svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度");
                    //鳥取はクラスは表示しない
                    if (!param._isTottori) {
                        svf.VrsOut("HR_CLASS", meiboheader);
                        if (useReplaceHrClass.booleanValue()) {
                            log.debug(gradehrclass + " :クラス見出しを置き換える。");
                            svf.VrAttribute("HR_CLASS2", "Y=" + ( -360 + 67 * meiboheaderpoint));  // 組の出力位置を変更
                            svf.VrsOut("HR_CLASS2", NumberUtils.isDigits(hrclass) ? String.valueOf(Integer.parseInt(hrclass)) : hrclass);
                        }
                    }
                    svf.VrsOut("GRADUATE", "（" + param._graduateDate + " 卒業" + "）");
                }

                final int kurikaeshi = Math.abs(count - (12 + 1));

                if (grdNo != null) {
                    svf.VrsOutn("CERTIFNO", kurikaeshi, "第" + insertBlankMoji(stb.append(convertKansuuji(grdNo))) + "号");
                }

                svf.VrsOutn("ATTENDNO", kurikaeshi, String.valueOf(Integer.parseInt(attendno)));
                if (param._output3 != null) {
                    svf.VrsOutn( "REMARK", kurikaeshi, abbv1);
                }

                final String kana = useRealName ? realNameKana : nameKana;
                svf.VrsOutn("KANA" + (kana != null && kana.length() < 14 ? "" : kana != null && kana.length() < 20 ? "2" : "3"), kurikaeshi, kana);
                printNameTate(svf, param, kurikaeshi, name, null, realName, null, useRealName, nameOutputFlg, 10, 10, 20);

                final String[] arrstr = arrayBirth(db2, birthday, ((birthdiv != null) ? 1 : 0));
                if (arrstr != null) {
                    printBirthday(svf, kurikaeshi, arrstr);
                }
            } catch (Exception ex) {
                log.error("printMeibo error!", ex);
            }
        }

        /**
         *  svf print 数字を漢数字へ変換(文字単位)
         */
        private String convertKansuuji(final String suuji) {
            final StringBuffer stb = new StringBuffer();
            for (int i = 0; i < suuji.length(); i++) {
                final String n = suuji.substring(i, i + 1);
                final String cov;
                if (NumberUtils.isDigits(n)) {
                    cov = arraykansuuji[Integer.parseInt(n)];
                } else {
                    cov = hmm.get(n) == null ? n : hmm.get(n).toString();
                }
                stb.append(cov);
            }
            return stb.toString();
        }

        private void printBirthday(final Vrw32alp svf, final int column, final String[] arrstr) {
            for (int i = 1; i < 8; i++) {
                svf.VrsOutn("BIRTHDAY" + i, column, "");
            }
            for (int i = (arrstr.length - 1), j = 7; i >= 0; i--, j--) {
                svf.VrsOutn("BIRTHDAY" + j, column, arrstr[i]);
            }
        }

        /* svf print 年月日の編集 */
        private String[] arrayBirth(final DB2UDB db2, final java.util.Date pdate, final int condiv) {
            String arr_date[] = null;
            if (condiv == 0) {
                arr_date = new String[7];
            } else {
                arr_date = new String[6];
            }
            try{
                final String hdate;
                if (condiv == 0) {
                    final Calendar cal = new GregorianCalendar();
                    cal.setTime(pdate);
                    hdate = KNJ_EditDate.gengou(db2, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));
                } else {
                    hdate = _sdf.format(pdate);
                }
                //log.debug(" hdate = " + hdate);
                boolean dflg = false;       //数値？
                int ia = 0;
                int ib = 0;
                for (int i = 0; i < hdate.length(); i++) {
                    //if ((Character.isDigit(hdate.charAt(i)) && !dflg) || (!Character.isDigit(hdate.charAt(i)) && dflg)) {
                    boolean cflg = hdate.charAt(i) == '元' || Character.isDigit(hdate.charAt(i));
                    if ((cflg  && !dflg) || (!cflg && dflg)) {
                        //log.debug("hdate.charAt(i) = " + hdate.charAt(i) + "  dflg=" + dflg);
                        if (i > 0) {
                            arr_date[ib++] = hdate.substring(ia,i);
                        }
                        //log.debug("ia=" + ia + "   ib = " + ib);
                        ia = i;
                        //dflg = Character.isDigit(hdate.charAt(i));
                        dflg = cflg;   //10/05/13
                    }
                }
                if (ia > 0) {
                    arr_date[ib] = hdate.substring(ia);
                }
            } catch (Exception ex) {
                log.error("printMeibo error!", ex);
            }
            return arr_date;
        }

        /* 文字列ブランク挿入 */
        private String insertBlankMoji(final StringBuffer stb) {
            stb.insert(0, "      ");     //６文字に満たない場合はブランクを挿入！
            if (stb.length() > 6) {
                stb.delete(0, stb.length() - 6);
            }
            return stb.toString();
        }
    }

    /**
     *  svf print 名簿印刷処理
     */
    private void printMeiboYoko(final DB2UDB db2, final Vrw32alp svf)
    {
        final List list = new ArrayList();
        PreparedStatement ps1 = null;
        ResultSet rs1 = null;
        try{
            if (param._isPrintGakka) {
                final String sql1 = sqlMeiboHeaderGakka(param._year, param._semester, param._gradeHrClass);
                ps1 = db2.prepareStatement(sql1);
                rs1 = ps1.executeQuery();

                while (rs1.next()) {
                    /* クラス見出しのセット => 課程から卒業日まで一つの文字列に編集後、組をスペースに置き換え、縦で出力する。
                                               組は文字列を別にして横に出力する。その際、上述文字列の組位置を算出して出力する。
                    */

                    final String majorCd = rs1.getString("MAJORCD");
                    list.add(new String[] {majorCd});
                }
            } else {
                ps1 = db2.prepareStatement(sqlMeiboHeader(param));
                rs1 = ps1.executeQuery();

                while (rs1.next()) {
                    /* クラス見出しのセット => 課程から卒業日まで一つの文字列に編集後、組をスペースに置き換え、縦で出力する。
                                               組は文字列を別にして横に出力する。その際、上述文字列の組位置を算出して出力する。
                    */
                    final String majorName = rs1.getString("MAJORNAME");
                    final String courseName = rs1.getString("COURSENAME");
                    final String gradeHrclass = rs1.getString("GRADE_HR_CLASS");
                    list.add(new String[]{majorName, courseName, gradeHrclass});
                }
            }
        } catch (Exception ex) {
            log.error("printMeibo error!", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps1, rs1);
        }

        final int maxLine;
        final String form;
        if (param._isKyoto) {
            form = "2".equals(param._pattern) ? "KNJI092_4KYOTO.frm" : "KNJI092_3KYOTO.frm";
            maxLine = 8;
        } else {
            form = "2".equals(param._pattern) ? "KNJI092_4.frm" : "KNJI092_3.frm";
            maxLine = 10;
        }
        svf.VrSetForm(form, 1);

        final String entdateName = "on".equals(param._entGrdDateFormat) ? "入学年月" : "入学年月日";
        final String graddateattenddateName = "on".equals(param._entGrdDateFormat) ? "卒業年月" : "卒業年月日";
        svf.VrsOut("ENTDATE_NAME", entdateName);
        svf.VrsOut("GRADDATEENTDATE_NAME", graddateattenddateName);

        PreparedStatement ps2 = null;
        ResultSet rs2 = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {

            svf.VrsOut("ENTDATE_NAME", entdateName);
            svf.VrsOut("GRADDATEENTDATE_NAME", graddateattenddateName);

            try {
                int count = 0;
                final String[] args = (String[]) it.next();
                if (param._isPrintGakka) {
                    final String majorCd = (String) args[0];
                    final String sql2 = sqlMeiboMeisaiGakka(param);
                    ps2 = db2.prepareStatement(sql2);
                    ps2.setString(1, majorCd);
                    rs2 = ps2.executeQuery();
                } else {
                    final String gradeHrclass = args[2];
                    final String sql2 = sqlMeiboMeisai(param);
                    ps2 = db2.prepareStatement(sql2);
                    ps2.setString(1, gradeHrclass);  //組
                    rs2 = ps2.executeQuery();
                }
                while (rs2.next()) {
                    final boolean useRealName = "1".equals(rs2.getString("USE_REAL_NAME"));
                    final String grade = rs2.getString("GRADE");
                    final String realName = rs2.getString("REAL_NAME");
                    final String realNameKana = rs2.getString("REAL_NAME_KANA");
                    final String nameOutputFlg = rs2.getString("NAME_OUTPUT_FLG");
                    final String majorName;
                    final String courseName;
                    if (param._isPrintGakka) {
                        majorName = rs2.getString("MAJORNAME");
                        courseName = rs2.getString("COURSENAME");
                    } else {
                        majorName = args[0];
                        courseName = args[1];
                    }
                    count = printMeiboMeisaiYokoSvf(db2, svf,
                            rs2.getString("GRD_NO"), rs2.getString("NAME"), rs2.getString("NAME_KANA"), rs2.getString("BIRTHDAY"), rs2.getString("BIRTHDIV"), rs2.getString("ENT_DATE"),
                            rs2.getString("ENT_DIV"), rs2.getString("ENT_NAME"),
                            useRealName, realName, realNameKana, nameOutputFlg,
                            count, grade, courseName, majorName);
                    if (count == maxLine) {
                        svf.VrEndPage();

                        svf.VrsOut("ENTDATE_NAME", entdateName);
                        svf.VrsOut("GRADDATEENTDATE_NAME", graddateattenddateName);

                        nonedata = true;
                        count = 0;
                    }
                }
                if (count > 0) {
                    svf.VrEndPage();
                    nonedata = true;
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps2, rs2);
                db2.commit();
            }
        }
    }

    private int printMeiboMeisaiYokoSvf(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String grdNo,
            final String name,
            final String nameKana,
            final String birthday,
            final String birthdiv,
            final String rsEntDate,
            final String entDiv,
            final String entName,
            final boolean useRealName,
            final String realName,
            final String realNameKana,
            final String nameOutputFlg,
            final int count,
            final String grade,
            final String courseName,
            final String majorName
    ) {
        int retCnt = count + 1;
        try {
            String title = StringUtils.defaultString(param.getSchoolMstSchoolName1(grade));
            title = title + "　第" + param._gradu + "回卒業生";
            title = title + "　" + courseName + "課程";
            if ((title + "　" + majorName).length() > 35) {
                svf.VrsOut("SCHOOL_NAME2", title);
                svf.VrsOut("SCHOOL_NAME3", majorName);
            } else {
                svf.VrsOut("SCHOOL_NAME1", (title + "　" + majorName));
            }

            svf.VrsOut("ENTDATE", "on".equals(param._entGrdDateFormat) ? KNJ_EditDate.h_format_JP_M(db2, param._entDate) : KNJ_EditDate.h_format_JP(db2, param._entDate));
            final String graduateDate = param.getSchoolMstGraduateDate(grade);
            svf.VrsOut("GRADDATE", "on".equals(param._entGrdDateFormat) ? KNJ_EditDate.h_format_JP_M(db2, graduateDate) : KNJ_EditDate.h_format_JP(db2, graduateDate));

            if (grdNo != null) {
                svf.VrsOutn("CERTIFNO", retCnt, "第" + grdNo + "号");
            }

            printNameYoko(svf, param, retCnt, name, nameKana, realName, realNameKana, useRealName, nameOutputFlg);

            if (birthday != null) {
                svf.VrsOutn("BIRTHDAY", retCnt, null != birthdiv ? _sdf.format(Date.valueOf(birthday)) : KNJ_EditDate.h_format_JP(db2, birthday));
            }

            if (!"2".equals(param._pattern)) {
                if (rsEntDate != null) {
                    if (!rsEntDate.equals(param._entDate) || entDiv.equals("4") || entDiv.equals("5")) {
                        svf.VrsOutn("REMARK", retCnt, KNJ_EditDate.h_format_JP(db2, rsEntDate) + " " + entName);
                    }
                }
            }
        } catch (final Exception e) {
            log.error("printMeiboMeisaiYokoSvf = ", e);
        }
        return retCnt;
    }

    private static void printNameTate(final Vrw32alp svf, final Param param, final int column,
            final String name, final String nameKana, final String realName, final String realNameKana,
            final boolean useRealName, final String nameOutputFlg,
            final int use2345Count, final int splitCount, final int splitCount2) {
        if (useRealName && "1".equals(nameOutputFlg)) {
            final String realName0 = realName == null ? "" : realName;
            final String name0 = name == null ? "" : name;
            final String field1;
            final String field2;
            final int sp;
            if (realName0.length() > splitCount || ("（" + name0 + "）").length() > splitCount) {
                sp = splitCount2;
                field1 = "NAME4";
                field2 = "NAME5";
            } else {
                sp = splitCount;
                field1 = "NAME2";
                field2 = "NAME3";
            }
            if (null != realName && realName.length() != 0) {
                if (param.isTate()) {
                    final boolean isJapaneseUse = isJapaneaseUse(realName.charAt(0)); //最初の文字で縦・横を判断する
                    if (!isJapaneseUse) {
                        svf.VrAttributen(field1, column, "Rotation=270");
                    }
                }
                svf.VrsOutn(field1, column, realName);
            }
            if (null != name && name.length() != 0) {
                if (param.isTate()) {
                    final boolean isJapaneseUse = isJapaneaseUse(name.charAt(0)); //最初の文字で縦・横を判断する
                    if (!isJapaneseUse) {
                        svf.VrAttributen(field2, column, "Rotation=270");
                    }
                }
                // "（"、"）"が表示されるように編集する
                svf.VrsOutn(field2, column,  "（" + (name0.length() > sp - 2 ? name0.substring(0, sp - 2) : name0)  + "）");
            }

        } else {
            final String name0 = useRealName ? realName : name;
            final int mojisuu = name0 == null ? 0 : name0.length();               //文字数カウント
            if (mojisuu > 0) {
                final boolean isJapaneseUse = isJapaneaseUse(name0.charAt(0)); //最初の文字で縦・横を判断する
                final int m = (!param.isTate() && !isJapaneseUse) ? 2 : 1; // 英語は縦書きの場合全角、横書きの場合は半角でカウントする
                if (use2345Count * m < mojisuu) {
                    final int sp;
                    final String field1;
                    final String field2;
                    if ((splitCount * m) * 2 < mojisuu) {
                        sp = splitCount2 * m;
                        field1 = "NAME4";
                        field2 = "NAME5";
                    } else {
                        sp = splitCount * m;
                        field1 = "NAME2";
                        field2 = "NAME3";
                    }
                    final int endIndex = sp * 2 < mojisuu ? sp * 2 : mojisuu;
                    if (param.isTate()) {
                        if (!isJapaneseUse) {
                            svf.VrAttributen(field1, column, "Rotation=270");
                            svf.VrAttributen(field2, column, "Rotation=270");
                        }
                    }
                    if (sp >= mojisuu) {
                        svf.VrsOutn(field1, column, name0);
                    } else {
                        final String name1 = name0.substring(0, sp);
                        final String name2 = name0.substring(sp, endIndex);
//                        log.debug(" name1 = \"" + name1 + "\" bytesize = " + byteSize(name1) + ", name2 = \"" + name2 + "\" bytesize = " + byteSize(name2) + ", bytesize = " + byteSize(name0));
                        svf.VrsOutn(field1, column, name1);
                        svf.VrsOutn(field2, column, name2);
                    }
                } else {
                    final String field1 = "NAME1";
                    if (param.isTate()) {
                        if (!isJapaneseUse) {
                            svf.VrAttributen(field1, column, "Rotation=270");
                        }
                    }
//                    log.debug(" name0 = \"" + name0 + "\" bytesize = " + byteSize(name0));
                    svf.VrsOutn(field1, column, name0);
                }
            }
        }
    }

    private static void printNameYoko(final Vrw32alp svf, final Param param, final int column,
            final String name, final String nameKana, final String realName, final String realNameKana,
            final boolean useRealName, final String nameOutputFlg) {

        final int splitCount1 = 30;
        final int splitCount2 = 40;
        if (useRealName && "1".equals(nameOutputFlg)) {
            final String field1;
            final String field2;
            final int sp;
            if (getMS932ByteLength(realName) > splitCount1 || null != name && getMS932ByteLength("（" + name + "）") > splitCount1) {
                sp = splitCount2;
                field1 = "NAME4";
                field2 = "NAME5";
            } else {
                sp = splitCount1;
                field1 = "NAME2";
                field2 = "NAME3";
            }
            svf.VrsOutn(field1, column, realName);
            if (null != name && name.length() != 0) { // "（"、"）"が表示されるように編集する
                svf.VrsOutn(field2, column,  "（" + (getTokenList(name, sp).get(0))  + "）");
            }
            if ("2".equals(param._pattern)) {
                // かな表示
                svf.VrsOutn("KANA4", column, realNameKana);
                if (null != nameKana && nameKana.length() != 0) {
                    // "（"、"）"が表示されるように編集する
                    svf.VrsOutn("KANA5", column,  "（" + (getTokenList(nameKana, 30 - 4).get(0))  + "）");
                }
            }
        } else {
            final String name0 = useRealName ? realName : name;
            final int namelen = getMS932ByteLength(name0);
            if ("2".equals(param._pattern) && namelen <= 22 || !"2".equals(param._pattern) && namelen <= 24) {
                svf.VrsOutn("NAME1", column, name0);
            } else if ("2".equals(param._pattern) && namelen <= 30) {
                svf.VrsOutn("NAME1_2", column, name0);
            } else if (namelen <= splitCount1 * 2) {
                // 30桁2行
                final String[] fields = {"NAME2", "NAME3"};
                final List tokenList = getTokenList(name0, splitCount1);
                for (int i = 0; i < Math.min(fields.length, tokenList.size()); i++) {
                    svf.VrsOutn(fields[i], column, (String) tokenList.get(i));
                }
            } else {
                // 40桁2行
                final String[] fields = {"NAME4", "NAME5"};
                final List tokenList = getTokenList(name0, splitCount2);
                for (int i = 0; i < Math.min(fields.length, tokenList.size()); i++) {
                    svf.VrsOutn(fields[i], column, (String) tokenList.get(i));
                }
            }

            if ("2".equals(param._pattern)) {
                // かな表示
                final String kana0 = useRealName ? realNameKana : nameKana;
                final int kanalen = getMS932ByteLength(kana0);
                if (kanalen <= 20) {
                    svf.VrsOutn("KANA1", column, kana0);
                } else if (kanalen <= 30) {
                    svf.VrsOutn("KANA1_2", column, kana0);
                } else if (kanalen <= 40) {
                    svf.VrsOutn("KANA1_3", column, kana0);
                } else {
                    final String[] fields = {"KANA4", "KANA5"};
                    final List tokenList = getTokenList(kana0, 30);
                    for (int i = 0; i < Math.min(fields.length, tokenList.size()); i++) {
                        svf.VrsOutn(fields[i], column, (String) tokenList.get(i));
                    }
                }
            }
        }
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    /**
     * @param source 元文字列
     * @param bytePerLine 1行あたりのバイト数
     * @return bytePerLineのバイト数ごとの文字列リスト
     */
    private static List getTokenList(final String source0, final int bytePerLine) {

        if (source0 == null || source0.length() == 0) {
            return Collections.EMPTY_LIST;
        }
        final String source = StringUtils.replace(StringUtils.replace(source0, "\r\n", "\n"), "\r", "\n");

        final List tokenList = new ArrayList();        //分割後の文字列の配列
        int startIndex = 0;                         //文字列の分割開始位置
        int byteLengthInLine = 0;                   //文字列の分割開始位置からのバイト数カウント
        for (int idx = 0; idx < source.length(); idx += 1) {
            //改行マークチェック
            if (source.charAt(idx) == '\r') {
                continue;
            }
            if (source.charAt(idx) == '\n') {
                byteLengthInLine = 0;
                startIndex = idx + 1;
            } else {
                final int sbytelen = getMS932ByteLength(source.substring(idx, idx + 1));
                byteLengthInLine += sbytelen;
                if (byteLengthInLine > bytePerLine) {
                    tokenList.add(source.substring(startIndex, idx));
                    byteLengthInLine = sbytelen;
                    startIndex = idx;
                }
            }
        }
        if (byteLengthInLine > 0) {
            tokenList.add(source.substring(startIndex));
        }

        return tokenList;
    } //String get_token()の括り

    /* ひらがな、かたかな、漢字判定 */
    private static boolean isJapaneaseUse(char ch) {
        boolean hantei = false;
/* ***** 日本語特有の文字かどうかの判断は困難！
        hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
        if (!hantei) hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.HIRAGANA);
        if (!hantei) hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.KATAKANA);
        if (!hantei) hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
*******  アルファベットかどうかの判断において、一部のカタカナがアルファベットと認識される */
        if (!hantei) hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.BASIC_LATIN);
        if (!hantei) hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.LATIN_1_SUPPLEMENT);
        if (!hantei) hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.LATIN_EXTENDED_A);
        if (!hantei) hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.LATIN_EXTENDED_B);
        if (!hantei) hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS);

        return !hantei;
    }

    /* get parameter doGet()パラメータ受け取り */
    private Param getParam(final HttpServletRequest request, final DB2UDB db2) {
        log.info("$Revision: 73859 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    /* DB STATEMENT 表紙 */
    private static String sqlHyoushi(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH T_HRCLASS(GRADE, HR_CLASS, HR_CLASS_NAME1, TR_CD1) AS(");
        stb.append("  SELECT GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         HR_CLASS_NAME1, ");
        stb.append("         TR_CD1 ");
        stb.append("  FROM   SCHREG_REGD_HDAT ");
        stb.append("  WHERE  YEAR = '" + param._year + "' ");
        stb.append("         AND SEMESTER = '" + param._semester + "' ");
        stb.append("         AND GRADE || HR_CLASS IN " + param._gradeHrClass + " ");
        stb.append("  UNION  VALUES('YYY', 'ZZZ', '', '') ");
        stb.append(") ");

        stb.append("SELECT W1.GRADE, ");
        stb.append("       W1.HR_CLASS, ");
        stb.append("       W1.HR_CLASS_NAME1, ");
        stb.append("       W2.STAFFNAME, ");
        stb.append("       W3.CNT_SCH, ");
        stb.append("       W3.CNT_SCH2,");
        stb.append("       W3.MIN_GRD_NO,");
        stb.append("       W3.MAX_GRD_NO ");
        stb.append("FROM   T_HRCLASS W1 ");
        stb.append("LEFT   JOIN STAFF_MST W2 ON W2.STAFFCD = W1.TR_CD1 ");

        stb.append("LEFT JOIN(");
        stb.append("  SELECT S1.GRADE, ");
        stb.append("         S1.HR_CLASS, ");
        stb.append("         S2.HR_CLASS_NAME1, ");
        stb.append("         COUNT(S1.SCHREGNO) AS CNT_SCH,");
        stb.append("         SUM(CASE S3.SEX WHEN '2' THEN 1 ELSE NULL END) AS CNT_SCH2,");
        stb.append("         '' AS MIN_GRD_NO, ");
        stb.append("         '' AS MAX_GRD_NO ");
        stb.append("  FROM   SCHREG_REGD_DAT S1");
        stb.append("         INNER JOIN T_HRCLASS S2 ON S2.GRADE = S1.GRADE AND S2.HR_CLASS = S1.HR_CLASS ");
        stb.append("         INNER JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO = S1.SCHREGNO ");
        stb.append("         LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = S1.YEAR AND GDAT.GRADE = S1.GRADE ");
        stb.append("         LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENT_GRD ON S3.SCHREGNO = ENT_GRD.SCHREGNO ");
        if (null != param._SCHOOL_KIND) {
        	stb.append("              AND ENT_GRD.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
        } else {
        	stb.append("              AND ENT_GRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        }
        stb.append("         LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B_DETAIL ON S3.SCHREGNO = B_DETAIL.SCHREGNO ");
        stb.append("              AND B_DETAIL.YEAR = '" + param._year + "' ");
        stb.append("              AND B_DETAIL.BASE_SEQ = '007' ");
        stb.append("  WHERE  S1.YEAR = '" + param._year + "' ");
        stb.append("         AND S1.SEMESTER = '" + param._semester + "' ");
        stb.append("         AND S1.GRADE || S1.HR_CLASS IN " + param._gradeHrClass + " ");
        stb.append("         AND S1.GRADE = S2.GRADE ");
        stb.append("         AND S1.HR_CLASS = S2.HR_CLASS ");
        if (param._mikomi == null) {
            stb.append("     AND (VALUE(ENT_GRD.GRD_DIV, '0') = '1' ");
            stb.append("          OR ");
            stb.append("          (ENT_GRD.GRD_DIV IS NULL AND VALUE(B_DETAIL.BASE_REMARK1, '0') = '1')) ");
        } else {
            stb.append("     AND ENT_GRD.GRD_DIV IS NULL ");
        }
        stb.append("  GROUP BY S1.GRADE, S1.HR_CLASS, S2.HR_CLASS_NAME1 ");
        stb.append("  UNION ");
        stb.append("  SELECT 'YYY' AS GRADE, ");
        stb.append("         'ZZZ'AS HR_CLASS, ");
        stb.append("         'ZZZ'AS HR_CLASS_NAME1, ");
        stb.append("         COUNT(SCHREGNO) AS CNT_SCH,");
        stb.append("         SUM(CASE SEX WHEN '2' THEN 1 ELSE NULL END) AS CNT_SCH2,");
        stb.append("         MIN(GRD_NO) AS MIN_GRD_NO,");
        stb.append("         MAX(GRD_NO) AS MAX_GRD_NO ");
        stb.append("  FROM   (SELECT S1.SCHREGNO,SEX,");
        stb.append("                (SUBSTR('      '||S3.GRD_NO,LENGTH('      '||RTRIM(S3.GRD_NO))-5,6)) AS GRD_NO ");
        stb.append("          FROM  SCHREG_REGD_DAT S1 ");
        stb.append("                INNER JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO = S1.SCHREGNO ");
        stb.append("                LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = S1.YEAR AND GDAT.GRADE = S1.GRADE ");
        stb.append("                LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENT_GRD ON S3.SCHREGNO = ENT_GRD.SCHREGNO ");
        if (null != param._SCHOOL_KIND) {
        	stb.append("              AND ENT_GRD.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
        } else {
        	stb.append("              AND ENT_GRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        }
        stb.append("                LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B_DETAIL ON S3.SCHREGNO = B_DETAIL.SCHREGNO ");
        stb.append("                     AND B_DETAIL.YEAR = '" + param._year + "' ");
        stb.append("                     AND B_DETAIL.BASE_SEQ = '007' ");
        stb.append("          WHERE  S1.YEAR = '" + param._year + "' ");
        stb.append("             AND S1.SEMESTER = '" + param._semester + "' ");
        stb.append("             AND S1.GRADE || S1.HR_CLASS IN " + param._gradeHrClass + " ");
        if (param._mikomi == null) {
            stb.append("     AND (VALUE(ENT_GRD.GRD_DIV, '0') = '1' ");
            stb.append("          OR ");
            stb.append("          (ENT_GRD.GRD_DIV IS NULL AND VALUE(B_DETAIL.BASE_REMARK1, '0') = '1')) ");
        } else {
            stb.append("     AND ENT_GRD.GRD_DIV IS NULL ");
        }
        stb.append("       ) S1 ");
        stb.append("   ) W3 ON W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ");

        stb.append("ORDER BY W1.GRADE, W1.HR_CLASS ");
        return stb.toString();
    }


    /**
     *  DB STATEMENT 名簿.学科見出し
     */
    private static String sqlMeiboHeaderGakka(final String year, final String semester, final String majorCds) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT COURSECD || MAJORCD AS MAJORCD, ");
        stb.append("      W4.MAJORNAME ");
        stb.append("FROM ");
        stb.append("    MAJOR_MST W4 ");
        stb.append("WHERE ");
        stb.append("    COURSECD || MAJORCD IN "+ majorCds +" ");
        stb.append("ORDER BY COURSECD || MAJORCD ");
        return stb.toString();
    }



    /* DB STATEMENT 名簿.明細 */
    private static String sqlMeiboMeisaiGakka(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT RTRIM(S3.GRD_NO) AS GRD_NO, ");
        stb.append("       S3.NAME, ");
        stb.append("       S3.NAME_KANA, ");
        stb.append("       S3.REAL_NAME, ");
        stb.append("       S3.REAL_NAME_KANA, ");
        stb.append("       S3.BIRTHDAY, ");
        stb.append("       N1.ABBV1 AS SEX_NAME, ");
        stb.append("       S3.ENT_DATE, ");
        stb.append("       S3.ENT_DIV, ");
        stb.append("       N2.NAME1 AS ENT_NAME, ");
        stb.append("       L1.MAJORNAME, ");
        stb.append("       L2.COURSENAME, ");
        stb.append("       (SELECT SCHREGNO FROM KIN_GRD_LEDGER_SETUP_DAT S2 WHERE S1.SCHREGNO = S2.SCHREGNO) AS BIRTHDIV, ");
        stb.append("       (CASE WHEN L3.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
        stb.append("       L3.NAME_OUTPUT_FLG, ");
        stb.append("       S1.GRADE ");
        stb.append("FROM   SCHREG_REGD_DAT S1 ");
        stb.append("        LEFT JOIN MAJOR_MST L1 ON S1.COURSECD || S1.MAJORCD = L1.COURSECD || L1.MAJORCD ");
        stb.append("        LEFT JOIN COURSE_MST L2 ON S1.COURSECD = L2.COURSECD ");
        stb.append("        LEFT JOIN SCHREG_NAME_SETUP_DAT L3 ON L3.SCHREGNO = S1.SCHREGNO AND L3.DIV = '06' ");
        stb.append("        LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = S1.YEAR AND GDAT.GRADE = S1.GRADE ");
        stb.append("        LEFT JOIN NAME_MST NMA023 ON NMA023.NAMECD1 = 'A023' AND NMA023.NAME1 = GDAT.SCHOOL_KIND ");
        stb.append("        INNER JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO = S1.SCHREGNO ");
        stb.append("        LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = S1.YEAR AND GDAT.GRADE = S1.GRADE ");
        stb.append("        LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENT_GRD ON S3.SCHREGNO = ENT_GRD.SCHREGNO ");
        if (null != param._SCHOOL_KIND) {
        	stb.append("             AND ENT_GRD.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
        } else {
        	stb.append("             AND ENT_GRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        }
        stb.append("        LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B_DETAIL ON S3.SCHREGNO = B_DETAIL.SCHREGNO ");
        stb.append("             AND B_DETAIL.YEAR = '" + param._year + "' ");
        stb.append("             AND B_DETAIL.BASE_SEQ = '007' ");
        stb.append("        LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z002' AND N1.NAMECD2 = S3.SEX ");
        stb.append("        LEFT JOIN NAME_MST N2 ON N2.NAMECD1='A002' AND N2.NAMECD2 = S3.ENT_DIV ");
        stb.append("WHERE  S1.YEAR = '" + param._year + "' AND ");
        stb.append("       S1.SEMESTER = '" + param._semester + "' AND ");
        stb.append("       S1.COURSECD || S1.MAJORCD = ? AND ");
        if (param._mikomi == null) {
            stb.append("     AND (VALUE(ENT_GRD.GRD_DIV, '0') = '1' ");
            stb.append("          OR ");
            stb.append("          (ENT_GRD.GRD_DIV IS NULL AND VALUE(B_DETAIL.BASE_REMARK1, '0') = '1')) ");
        } else {
            stb.append(  "AND ENT_GRD.GRD_DIV IS NULL ");
        }
        if (param._z010Namespare2 != null) {
            stb.append(  "AND (INT(S1.GRADE) = 3 OR INT(S1.GRADE) >= 6) ");
        } else {
            stb.append(  "AND S1.GRADE BETWEEN NMA023.NAMESPARE2 AND NMA023.NAMESPARE3 ");
        }
       stb.append(" ORDER BY ");
       if (param._isGakkaOrderGradeHrclassAttendnoKana) {
           stb.append("     S1.GRADE, ");
           stb.append("     S1.HR_CLASS, ");
           stb.append("     S1.ATTENDNO, ");
           stb.append("     TRANSLATE_KANA(CASE WHEN L3.SCHREGNO IS NOT NULL THEN VALUE(REAL_NAME_KANA, '') ELSE VALUE(NAME_KANA, '') END), ");
           stb.append("     CASE WHEN L3.SCHREGNO IS NOT NULL THEN VALUE(REAL_NAME_KANA, '') ELSE VALUE(NAME_KANA, '') END ");
       } else {
           stb.append("     TRANSLATE_KANA(CASE WHEN L3.SCHREGNO IS NOT NULL THEN VALUE(REAL_NAME_KANA, '') ELSE VALUE(NAME_KANA, '') END), ");
           stb.append("     CASE WHEN L3.SCHREGNO IS NOT NULL THEN VALUE(REAL_NAME_KANA, '') ELSE VALUE(NAME_KANA, '') END, ");
           stb.append("     S1.GRADE, ");
           stb.append("     S1.HR_CLASS, ");
           stb.append("     S1.ATTENDNO ");
       }
       return stb.toString();
    }

    /**
     *  DB STATEMENT 名簿.クラス見出し
     */
    private static String sqlMeiboHeader(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    INT(W1.GRADE) AS GRADE, ");
        stb.append("    CASE WHEN W6.GRADE_CD IS NULL THEN NULL ELSE INT(W6.GRADE_CD) END AS GRADE_CD, ");
        stb.append("    W1.HR_CLASS, ");
        stb.append("    (W1.GRADE || W1.HR_CLASS) AS GRADE_HR_CLASS, ");
        stb.append("    W2.STAFFNAME, ");
        stb.append("    VALUE(W4.MAJORNAME, '') AS MAJORNAME, ");
        stb.append("    VALUE(W5.COURSENAME, '') AS COURSENAME, ");
        stb.append("    W3.MAJORCD, ");
        stb.append("    W3.COURSECD ");
        stb.append("FROM ");
        stb.append("    SCHREG_REGD_HDAT W1 ");
        stb.append("LEFT JOIN STAFF_MST W2 ON W2.STAFFCD=W1.TR_CD1 ");
        stb.append("LEFT JOIN ( ");
        stb.append("    SELECT GRADE, HR_CLASS, MAJORCD, COURSECD ");
        stb.append("    FROM   SCHREG_REGD_DAT S1 ");
        stb.append("    WHERE  YEAR = '" + param._year + "' ");
        stb.append("           AND SEMESTER = '" + param._semester + "' ");
        stb.append("           AND GRADE || HR_CLASS IN " + param._gradeHrClass + " ");
        stb.append("           AND ATTENDNO = (SELECT MIN(ATTENDNO) FROM SCHREG_REGD_DAT S2 ");
        stb.append("                     WHERE  S2.YEAR = S1.YEAR ");
        stb.append("                            AND S2.SEMESTER = S1.SEMESTER ");
        stb.append("                            AND S1.GRADE = S2.GRADE ");
        stb.append("                            AND S1.HR_CLASS = S2.HR_CLASS ");
        stb.append("                            AND S2.MAJORCD IS NOT NULL) ");
        stb.append("    ) W3 ON W3.GRADE || W3.HR_CLASS = W1.GRADE || W1.HR_CLASS ");
        stb.append("LEFT JOIN MAJOR_MST W4 ON W4.MAJORCD = W3.MAJORCD ");
        stb.append("        AND W4.COURSECD = W3.COURSECD ");
        stb.append("LEFT JOIN COURSE_MST W5 ON W5.COURSECD = W3.COURSECD ");
        stb.append("LEFT JOIN SCHREG_REGD_GDAT W6 ON W6.YEAR = W1.YEAR AND W6.GRADE = W1.GRADE ");
        stb.append("WHERE ");
        stb.append("    W1.YEAR = '" + param._year + "' ");
        stb.append("    AND W1.SEMESTER = '" + param._semester + "' ");
        stb.append("    AND W1.GRADE || W1.HR_CLASS IN " + param._gradeHrClass + " ");
        stb.append("ORDER BY ");
        stb.append(" W1.GRADE, W1.HR_CLASS");
        return stb.toString();
    }


    /* DB STATEMENT 名簿.明細 */
    private static String sqlMeiboMeisai(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT RTRIM(S3.GRD_NO) AS GRD_NO, ");
        stb.append("       S3.NAME, ");
        stb.append("       S3.NAME_KANA, ");
        stb.append("       S3.REAL_NAME, ");
        stb.append("       S3.REAL_NAME_KANA, ");
        stb.append("       BIRTHDAY, ");
        stb.append("       S1.GRADE || S1.HR_CLASS AS GRADE_HR_CLASS, ");
        stb.append("       S1.GRADE, ");
        stb.append("       S1.HR_CLASS, ");
        stb.append("       ATTENDNO, ");
        stb.append("       N1.NAME1, ");
        stb.append("       N1.ABBV1, ");
        stb.append("       S3.ENT_DATE, ");
        stb.append("       N2.NAME1 AS ENT_NAME, ");
        stb.append("       S3.ENT_DATE, ");
        stb.append("       S3.ENT_DIV, ");
        stb.append("       L1.MAJORNAME, ");
        stb.append("       L2.COURSENAME, ");
        stb.append("       (SELECT SCHREGNO FROM KIN_GRD_LEDGER_SETUP_DAT S2 WHERE S1.SCHREGNO = S2.SCHREGNO) AS BIRTHDIV, ");
        stb.append("       (CASE WHEN L3.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
        stb.append("       L3.NAME_OUTPUT_FLG ");
        stb.append("FROM   SCHREG_REGD_DAT S1 ");
        stb.append("        LEFT JOIN MAJOR_MST L1 ON S1.COURSECD || S1.MAJORCD = L1.COURSECD || L1.MAJORCD ");
        stb.append("        LEFT JOIN COURSE_MST L2 ON S1.COURSECD = L2.COURSECD ");
        stb.append("        LEFT JOIN SCHREG_NAME_SETUP_DAT L3 ON L3.SCHREGNO = S1.SCHREGNO AND L3.DIV = '06' ");
        stb.append("        INNER JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO = S1.SCHREGNO ");
        stb.append("        LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = S1.YEAR AND GDAT.GRADE = S1.GRADE ");
        stb.append("        LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENT_GRD ON S3.SCHREGNO = ENT_GRD.SCHREGNO ");
        if (null != param._SCHOOL_KIND) {
        	stb.append("             AND ENT_GRD.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
        } else {
        	stb.append("             AND ENT_GRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        }
        stb.append("        LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B_DETAIL ON S3.SCHREGNO = B_DETAIL.SCHREGNO ");
        stb.append("             AND B_DETAIL.YEAR = '" + param._year + "' ");
        stb.append("             AND B_DETAIL.BASE_SEQ = '007' ");
        stb.append("        LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = S3.SEX ");
        stb.append("        LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'A002' AND N2.NAMECD2 = S3.ENT_DIV ");
        stb.append("WHERE  S1.YEAR = '" + param._year + "' ");
        stb.append("       AND S1.SEMESTER = '" + param._semester + "' ");
        stb.append("       AND S1.GRADE || S1.HR_CLASS IN ? ");
        if (param._mikomi == null) {
            stb.append("   AND (VALUE(ENT_GRD.GRD_DIV, '0') = '1' ");
            stb.append("        OR ");
            stb.append("        (ENT_GRD.GRD_DIV IS NULL AND VALUE(B_DETAIL.BASE_REMARK1, '0') = '1')) ");
        } else {
            stb.append("   AND ENT_GRD.GRD_DIV IS NULL ");
        }
       stb.append("ORDER BY S1.ATTENDNO");
       return stb.toString();
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _grade;
        final String _gradeHrClass;
        final String _output1;
        final String _output2;
        final String _mikomi;
        final String _output3;
        final boolean _isPrintGakka;
        final boolean _isGakkaOrderGradeHrclassAttendnoKana;
        final boolean _useSchregRegdHdat;
        final String _writeDiv;
        final String _entDate;
        final String _entGrdDateFormat;
        final String _pattern;
        final String _useSchool_KindField;
        final String _SCHOOLCD;
        final String _SCHOOLKIND;
        final String _SCHOOL_KIND;

        String _graduateDate;
        String _gradu;
        Map _hrNamemap;
        Map _hrNamemap2;
        Map _A021map;
        boolean _isTottori;
        boolean _isKyoto;
        String _z010Namespare2;
        private Map<String, String> _gradeSchoolkindMap = Collections.EMPTY_MAP;
        private Map<String, String> _schoolMstSchoolName1Map = Collections.EMPTY_MAP;
        private Map<String, String> _schoolMstGraduateDate = Collections.EMPTY_MAP;
        final boolean _hasSCHOOL_MST_SCHOOL_KIND;
        private String _certifSchoolDatSchoolNameH;
        private String _certifSchoolDatSchoolNameJ;
        private String _certifSchoolDatSchoolNameP;
        private String _certifSchoolDatRemark10;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");                    //卒業年度
            _semester = request.getParameter("GAKKI");           //学期

            String hrclass[] = request.getParameterValues("CLASS_SELECTED");        //対象クラス
            StringBuffer stb = new StringBuffer();
            String grade = null;
            stb.append("(");
            for (int i = 0; i < hrclass.length; i++) {
                if (i > 0) {
                    stb.append(",");
                }
                grade = hrclass[i].substring(0, 2);               //対象学年
                stb.append("'").append(hrclass[i]).append("'");
            }
            stb.append(")");
            _grade = grade == null ? "03" : grade;
            final String gradeHrClass = stb.toString();                                  //対象組(カンマで接続)
            _gradeHrClass = gradeHrClass == null ? "00" : gradeHrClass;

            _output1 = request.getParameter("OUTPUT1");                 //表紙印刷
            _output2 = request.getParameter("OUTPUT2");                 //名簿印刷
            _mikomi = request.getParameter("MIKOMI");                  //卒業見込み出力の選択を追加 05/10/11
            _output3 = request.getParameter("OUTPUT3");                 //性別印刷
            _isPrintGakka = "2".equals(request.getParameter("CLASS_MAJOR"));
            _isGakkaOrderGradeHrclassAttendnoKana = "2".equals(request.getParameter("MAJOR_ORDER"));
            _useSchregRegdHdat = "1".equals(request.getParameter("useSchregRegdHdat"));
            _writeDiv = request.getParameter("WRITE_DIV");
            _entDate = null == request.getParameter("ENT_DATE") ? request.getParameter("ENT_DATE") : request.getParameter("ENT_DATE").replace('/', '-');
            _pattern = request.getParameter("PATTERN");
            _entGrdDateFormat = "2".equals(request.getParameter("PATTERN")) ? request.getParameter("ENT_GRD_DATE_FORMAT") : null;
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _SCHOOL_KIND = request.getParameter("SCHOOL_KIND");

            setNameMstZ010(db2);
            setGraduateDate(db2);    //卒業年月日
            _hrNamemap = setHrnameMap(db2, _year, _semester, "HR_CLASS_NAME1");
            _hrNamemap2 = setHrnameMap(db2, _year, _semester, "HR_CLASS_NAME2");
            log.debug(" SCHREG_REGD_HDAT:" + _hrNamemap);

            _A021map = KNJ_Get_Info.getMapForHrclassName(db2);
            if (_A021map == null) {
                _A021map = Collections.EMPTY_MAP;
            }
            log.debug(" A021:" + _A021map);
            setZ010Namespare2(db2);

            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            setGradeSchoolKindMap(db2);
            setCertifschool(db2);
        }

        public String getSchoolMstSchoolName1(final String grade) {
        	if (_hasSCHOOL_MST_SCHOOL_KIND) {
        		final String schoolKind = _gradeSchoolkindMap.get(grade);
        		return _schoolMstSchoolName1Map.get(schoolKind);
        	}
			return _schoolMstSchoolName1Map.get("0");
		}

        public String getSchoolMstGraduateDate(final String grade) {
        	if (_hasSCHOOL_MST_SCHOOL_KIND) {
        		final String schoolKind = _gradeSchoolkindMap.get(grade);
        		return _schoolMstGraduateDate.get(schoolKind);
        	}
			return _schoolMstGraduateDate.get("0");
		}

		public boolean isTate() {
            return _writeDiv.equals("1");
        }

        public String getGradeHrclassName2(String gradeHrclass) {
            return (String) _hrNamemap2.get(gradeHrclass);
        }

        public String getGradeHrclassName(String gradeHrclass) {
            return (String) _hrNamemap.get(gradeHrclass);
        }

        public String getA021Hrname(String hrclass) {
            return (String) _A021map.get(hrclass);
        }

        private String getNendo(final DB2UDB db2, final Param param) {
            final int nen = param.getWarekiNen(db2, Integer.parseInt(_year));

            if(nen == 1) {
                return KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            } else {
                //日付を漢数字変換
                return getKansuujiWareki(KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度");
            }
        }


        /**
         *  svf print 卒業日付取得
         */
        private void setGraduateDate(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String hdate = null;
            String gradu = null;
            try{
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   GRADUATE_DATE ");
                stb.append("  ,INT(FISCALYEAR(GRADUATE_DATE)) - INT(FOUNDEDYEAR) - (CASE WHEN MONTH(GRADUATE_DATE) < 4 THEN 1 ELSE 2 END) AS GRADU");
                stb.append("  ,PRESENT_EST");
                stb.append(" FROM SCHOOL_MST ");
                stb.append(" WHERE YEAR= '" + _year + "' ");
                if ("1".equals(_useSchool_KindField)) {
                    stb.append("        AND SCHOOL_KIND = '" + _SCHOOLKIND + "'  ");
                }
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final Calendar cal = new GregorianCalendar();
                    cal.setTime(rs.getDate("GRADUATE_DATE"));
                    hdate = getKansuujiWareki(KNJ_EditDate.gengou(db2, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)));
                    if (_isKyoto) {
                        final String presentEst = rs.getString("PRESENT_EST");
                        gradu = NumberUtils.isDigits(presentEst) ? String.valueOf(Integer.parseInt(presentEst)) : presentEst;
                    } else {
                        gradu = rs.getString("GRADU");
                    }
                }
            } catch (Exception ex) {
                log.error("getGraduateDate error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            _graduateDate = null == hdate ? "" : hdate;
            _gradu = null == gradu ? "" : gradu;
        }

        /**
         *  svf print 日付を漢数字変換
         */
        private String getKansuujiWareki(final String hdate)
        {
            final StringBuffer stb = new StringBuffer();
            try{
                boolean dflg = false;       //数値？
                int ia = 0;
                for (int i = 0; i < hdate.length(); i++) {
                    if ((Character.isDigit(hdate.charAt(i)) && !dflg) || (!Character.isDigit(hdate.charAt(i)) && dflg)) {
                        if (i == 0) {
                            continue;
                        }
                        if (!dflg) {
                            stb.append(hdate.substring(ia, i));
                        } else {
                            stb.append(convertKansuuji(Integer.parseInt(hdate.substring(ia, i))));
                        }
                        ia = i;
                        dflg = Character.isDigit(hdate.charAt(i));
                    }
                }
                if (ia > 0) {
                    stb.append(hdate.substring(ia));
                }
            } catch (Exception ex) {
                log.error("getKansuujiWareki error!", ex);
            }
            return stb.toString();
        }

        /**
         *  svf print 数字を漢数字へ変換.百の位まで(数値単位)
         */
        private static String convertKansuuji(final int suuji)
        {
            final StringBuffer stb = new StringBuffer();
            int kurai = String.valueOf(suuji).length();
            if (kurai > 0) {
                if (Integer.parseInt((String.valueOf(suuji)).substring(kurai - 1)) > 0) {
                    stb.append(arraykansuuji[Integer.parseInt((String.valueOf(suuji)).substring(kurai-1))]);
                }
            }
            if (kurai >= 2) {
                stb.insert(0, "十");
//    log.debug("suuji="+Integer.parseInt((String.valueOf(suuji)).substring(kurai-2,kurai-1)));
                if (Integer.parseInt((String.valueOf(suuji)).substring(kurai - 2, kurai - 1)) > 1) {
                    stb.insert(0, arraykansuuji[Integer.parseInt((String.valueOf(suuji)).substring(kurai - 2, kurai - 1))]);
                }
            }
            if (kurai >= 3) {
                stb.insert(0, "百");
                if (Integer.parseInt((String.valueOf(suuji)).substring(kurai - 3, kurai - 2)) > 1) {
                    stb.insert(0, arraykansuuji[Integer.parseInt((String.valueOf(suuji)).substring(kurai - 3, kurai - 2))]);
                }
            }
            stb.append("");
//    log.debug("stb="+stb.toString());

            return stb.toString();
        }

        private void setNameMstZ010(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                _isTottori = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String str = rs.getString("NAME1");
                    _isTottori = "tottori".equals(str);
                    _isKyoto = "kyoto".equals(str);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private Map setHrnameMap(final DB2UDB db2, final String year ,final String semester, final String fieldName) {
            final Map map = new HashMap();
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT GRADE || HR_CLASS AS GRADE_HR_CLASS, " + fieldName + " ");
            sql.append(" FROM SCHREG_REGD_HDAT ");
            sql.append(" WHERE ");
            sql.append("   YEAR = '" + year + "' AND SEMESTER = '" + semester + "' ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("GRADE_HR_CLASS"), rs.getString(fieldName));
                }
            } catch (Exception e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }

        private void setZ010Namespare2(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try{
                ps = db2.prepareStatement("SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    _z010Namespare2 = rs.getString("NAMESPARE2");
                }
            } catch (Exception ex) {
                log.error("setZ010Namespare2 error!", ex);
            } finally{
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public String getSchoolName(String grade) {
            if (null == grade) {
                grade = _grade;
            }
            final String schoolKind = (String) _gradeSchoolkindMap.get(grade);
            if ("H".equals(schoolKind)) {
                return _certifSchoolDatSchoolNameH;
            } else if ("J".equals(schoolKind)) {
                return _certifSchoolDatSchoolNameJ;
            } else if ("P".equals(schoolKind)) {
                return _certifSchoolDatSchoolNameP;
            }
            return null;
        }

        private void setGradeSchoolKindMap(final DB2UDB db2) {
            _gradeSchoolkindMap = new HashMap();
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT GRADE, SCHOOL_KIND ");
            sql.append(" FROM SCHREG_REGD_GDAT ");
            sql.append(" WHERE ");
            sql.append("   YEAR = '" + _year + "' ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _gradeSchoolkindMap.put(rs.getString("GRADE"), rs.getString("SCHOOL_KIND"));
                }
            } catch (Exception e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            final String sql1;
            final String sql2;
            if (_hasSCHOOL_MST_SCHOOL_KIND) {
            	sql1 = " SELECT SCHOOL_KIND, SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
            	sql2 = " SELECT SCHOOL_KIND, GRADUATE_DATE FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
            } else {
            	sql1 = " SELECT '0' AS SCHOOL_KIND, SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
            	sql2 = " SELECT '0' AS SCHOOL_KIND, GRADUATE_DATE FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
            }
            _schoolMstSchoolName1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql1), "SCHOOL_KIND", "SCHOOLNAME1");
            _schoolMstGraduateDate = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql2), "SCHOOL_KIND", "GRADUATE_DATE");
        }

        private void setCertifschool(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT * ");
                stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
                stb.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '138' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _certifSchoolDatSchoolNameH = rs.getString("SCHOOL_NAME");
                    _certifSchoolDatSchoolNameJ = rs.getString("REMARK1");
                    _certifSchoolDatSchoolNameP = rs.getString("REMARK4");
                    _certifSchoolDatRemark10 = rs.getString("REMARK10");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public int getWarekiNen(final DB2UDB db2, int seireki){
            int rtnStr = 0;

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  *  FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1='L007' ");
            stb.append("     AND '" + seireki + "' BETWEEN NAMESPARE1 AND ABBV3 ");

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String ganNen = rs.getString("NAMESPARE1");
                    // 和暦の年を取得
                    rtnStr = seireki - Integer.parseInt(ganNen) + 1;
                }
            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return rtnStr;
        }

    }
}//クラスの括り

package servletpack.KNJL;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
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
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfForm;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL326F {

    private static final Log log = LogFactory.getLog(KNJL326F.class);

    private static final String APPLICANTDIV1 = "1";
    private static final String APPLICANTDIV2 = "2";
    private final String OUTPUT1_GOKAKU = "1";
    private final String OUTPUT2_FUGOKAKU = "2";
    private final String OUTPUT3_HOKETSU_GOKAKU = "3";
    private final String OUTPUT4_TOKUTAI_GOKAKU_TUUCHI = "4";
    private final String OUTPUT5_TOKUTAI_TUUCHI = "5";
    private final String OUTPUT6_FURIKOMI = "6";
    private final String OUTPUT7_SHODAKU = "7";
    private final String OUTPUT8_SHODAKU_FUTO = "8";
    private static final String OUTPUT_DIV1_GOUKAKUSHA = "1";
    private static final String OUTPUT_DIV2_JUKENSHA = "2";
    private static final String OUTPUT_DIV3_SHITEI = "3";
    private final String OUTPUT_DIV4_TOKUTAI_KOUHO = "4";

    private boolean _hasData;
    private Param _param;

    private static final String FROM_TO_MARK = "\uFF5E";

    private static final String PRGID_KNJL327F = "KNJL327F";

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        //print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //svf設定
        svf.VrInit();    //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        try {
            log.fatal("$Revision: 72176 $ $Date: 2020-02-04 17:57:27 +0900 (火, 04 2 2020) $"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            //SVF出力
            setSvfMain(db2, svf); //帳票出力のメソッド

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            if (null != _param) {
                _param.close();
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            //終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();       //DBを閉じる
            outstrm.close();   //ストリームを閉じる
        }
    }

    private void setForm(final Vrw32alp svf, final String form) {
        _param.logOnce("setForm " + form);
        svf.VrSetForm(form, 1);
    }

    /**
     * フォームの出力
     * @param db2
     * @param svf
     * @return
     */
    private void setSvfMain(final DB2UDB db2, final Vrw32alp svf) {
        final Form form = new Form(_param, svf);
        final String sql = sql(_param);
        if (_param._isOutputDebug) {
            log.info(" sql = " + sql);
        }
        final List list = KnjDbUtils.query(db2, sql);
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map m = (Map) it.next();
            //log.debug(" m = " + m);
            if (OUTPUT1_GOKAKU.equals(_param._output)) {
                printGoukaku(db2, svf, m);
            } else if (OUTPUT2_FUGOKAKU.equals(_param._output)) {
                printFugoukaku(db2, svf, m);
            } else if (OUTPUT3_HOKETSU_GOKAKU.equals(_param._output)) {
                printHoketsu(db2, svf, m);
            } else if (OUTPUT4_TOKUTAI_GOKAKU_TUUCHI.equals(_param._output) || OUTPUT5_TOKUTAI_TUUCHI.equals(_param._output)) {
                printTokutaiseiGoukakuTuuchi(db2, form, m);
            } else if (OUTPUT6_FURIKOMI.equals(_param._output)) {
                if (APPLICANTDIV1.equals(_param._applicantdiv)) {
                    printNyugakukinFurikomiJ(db2, form, m);
                } else {
                    printNyugakukinFurikomiH(db2, form, m);
                }
            } else if (OUTPUT7_SHODAKU.equals(_param._output)) {
                printNyugakuShoudakusho(db2, svf, m);
            } else if (OUTPUT8_SHODAKU_FUTO.equals(_param._output)) {
                printNyugakuShodakushoYoFuto(svf, m);
            }
        }
    }

    private static String[] getBeforeReceptnoHonordiv(final Map m) {
        String beforeReceptno = null;
        String beforeHonordiv = null;
        int i = -1;
        for (i = 4; i >= 1; i--) {
            beforeHonordiv = getString(m, "BEFORE_HONORDIV" + String.valueOf(i));
            if (null != beforeHonordiv) {
                beforeReceptno = getString(m, "BEFORE_RECEPTNO" + String.valueOf(i));
                break;
            }
        }
        log.info(" beforeReceptno = " + beforeReceptno + ", beforeHonordiv = " + beforeHonordiv + " (" + i + ")");
        return new String[] { beforeReceptno, beforeHonordiv};
    }

    private void printSchoolNameImage(final Vrw32alp svf) {
        if (null != _param._schoolNamePath) {
            if  (APPLICANTDIV2.equals(_param._applicantdiv)) {
                svf.VrsOut("SCHOOL_NAME_H", _param._schoolNamePath); //
            } else {
                svf.VrsOut("SCHOOL_NAME_J", _param._schoolNamePath); //
            }
        }
    }

    private void printSchoolStampImage(final Vrw32alp svf) {
        if (null != _param._schoolStampPath) {
            svf.VrsOut("SCHOOL_STAMP", _param._schoolStampPath); //
        }
    }

    private void printSchoolLogoImage(final Vrw32alp svf) {
        if (null != _param._schoolLogoPath) {
            svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoPath); //
        }
    }

    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private static String addString(final String s1, final String s2) {
        if (null == s1) {
            return null;
        }
        return s1 + s2;
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    // 合格通知
    private void printGoukaku(final DB2UDB db2, final Vrw32alp svf, final Map m) {
        final String coursename = StringUtils.defaultString(getString(m, "SUC_EXAMCOURSE_NAME"), getString(m, "DET001_EXAMCOURSE_NAME"));
        if (APPLICANTDIV1.equals(_param._applicantdiv)) {
            final String form = "KNJL326F_J1.frm";
            setForm(svf, form);

            printSchoolLogoImage(svf);
            //svf.VrsOut("COURSE_NAME", coursename); // コース名
            svf.VrsOut("EXAM_NO", getString(m, "RECEPTNO")); // 受験番号
            svf.VrsOut("NAME", addString(getString(m, "NAME"), "　殿")); // 氏名
            svf.VrsOut("NENDO", toZenkaku(_param._outputNendo)); // 年度
            svf.VrsOut("JUDGE1", "合　　格"); // 判定
//            svf.VrsOut("JUDGE2_1", null); // 判定
//            svf.VrsOut("JUDGE2_2", null); // 判定
            svf.VrsOut("DATE", _param.gethiduke(db2, _param._printDate)); // 日付
            printSchoolLogoImage(svf);
            printSchoolNameImage(svf);
            printSchoolStampImage(svf);
            svf.VrsOut("STAFF_NAME", _param._principalName); // 職員名
            svf.VrsOut("JOB_NAME", _param._jobName); // 役職名
        } else if (APPLICANTDIV2.equals(_param._applicantdiv)) {
            final boolean isIkkansei = "6".equals(_param._testdiv);
            final String form = isIkkansei ? "KNJL326F_H1_2.frm" : "KNJL326F_H1_1.frm";
            setForm(svf, form);

            svf.VrsOut("EXAM_NO", getString(m, "RECEPTNO")); // 受験番号
            svf.VrsOut("NAME", addString(getString(m, "NAME"), "　殿")); // 氏名
            svf.VrsOut("COURSE_NAME", coursename); // コース名
            svf.VrsOut("NENDO", toZenkaku(_param._outputNendo)); // 年度
            if (isIkkansei) {
//                svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
            } else {
                svf.VrsOut("JUDGE1", "合　　格"); // 判定
//                svf.VrsOut("JUDGE2_1", null); // 判定
//                svf.VrsOut("JUDGE2_2", null); // 判定
            }
            printSchoolLogoImage(svf);
            printSchoolNameImage(svf);
            printSchoolStampImage(svf);
            svf.VrsOut("DATE", _param.gethiduke(db2, _param._printDate)); // 日付
            svf.VrsOut("STAFF_NAME", _param._principalName); // 職員名
            svf.VrsOut("JOB_NAME", _param._jobName); // 役職名
        }

        svf.VrEndPage();
        _hasData = true;
    }

    // 不合格通知
    private void printFugoukaku(final DB2UDB db2, final Vrw32alp svf, final Map m) {
        final String form = APPLICANTDIV1.equals(_param._applicantdiv) ? "KNJL326F_J2.frm" : "KNJL326F_H2.frm";
        setForm(svf, form);

        printSchoolLogoImage(svf);
        if (APPLICANTDIV2.equals(_param._applicantdiv)) {
            svf.VrsOut("COURSE_NAME", StringUtils.defaultString(getString(m, "SUC_EXAMCOURSE_NAME"), getString(m, "DET001_EXAMCOURSE_NAME"))); // コース名
        }
        svf.VrsOut("EXAM_NO", getString(m, "RECEPTNO")); // 受験番号
        svf.VrsOut("NAME", addString(getString(m, "NAME"), "　殿")); // 氏名
        svf.VrsOut("NENDO", toZenkaku(_param._outputNendo)); // 年度
//        svf.VrsOut("JUDGE1", null); // 判定
        svf.VrsOut("JUDGE2_1", "残念ながらご希望に"); // 判定
        svf.VrsOut("JUDGE2_2", "添えませんでした"); // 判定
        svf.VrsOut("DATE", _param.gethiduke(db2, _param._printDate)); // 日付
//        svf.VrsOut("TEXT1", null); // 本文
//        svf.VrsOut("TEXT2", null); // 本文
//        svf.VrsOut("TEXT3", null); // 本文
        printSchoolLogoImage(svf);
        printSchoolNameImage(svf);
        printSchoolStampImage(svf);
        svf.VrsOut("STAFF_NAME", _param._principalName); // 職員名
        svf.VrsOut("JOB_NAME", _param._jobName); // 役職名

        svf.VrEndPage();
        _hasData = true;
    }

    // 補欠合格
    private void printHoketsu(final DB2UDB db2, final Vrw32alp svf, final Map m) {
        final String form = APPLICANTDIV1.equals(_param._applicantdiv) ? "KNJL326F_J3.frm" : "KNJL326F_H3.frm";
        setForm(svf, form);

        printSchoolLogoImage(svf);
        if (APPLICANTDIV2.equals(_param._applicantdiv)) {
            svf.VrsOut("COURSE_NAME", StringUtils.defaultString(getString(m, "SUC_EXAMCOURSE_NAME"), getString(m, "DET001_EXAMCOURSE_NAME"))); // コース名
        }
        svf.VrsOut("EXAM_NO", getString(m, "RECEPTNO")); // 受験番号
        svf.VrsOut("NAME", addString(getString(m, "NAME"), "　殿")); // 氏名
        svf.VrsOut("NENDO", toZenkaku(_param._outputNendo)); // 年度
//        svf.VrsOut("JUDGE1", null); // 判定
        svf.VrsOut("JUDGE2_1", "補欠"); // 判定
        svf.VrsOut("JUDGE2_2", "繰り上げ入学候補者"); // 判定
        svf.VrsOut("DATE", _param.gethiduke(db2, _param._printDate)); // 日付
//        svf.VrsOut("TEXT1", null); // 本文
//        svf.VrsOut("TEXT2", null); // 本文
//        svf.VrsOut("TEXT3", null); // 本文
        printSchoolLogoImage(svf);
        printSchoolNameImage(svf);
        printSchoolStampImage(svf);
        svf.VrsOut("STAFF_NAME", _param._principalName); // 職員名
        svf.VrsOut("JOB_NAME", _param._jobName); // 役職名


        svf.VrEndPage();
        _hasData = true;
    }

    // 特待生合格通知 or 特待生通知
    private void printTokutaiseiGoukakuTuuchi(final DB2UDB db2, final Form form, final Map m) {
        String menjogaku1nenjiJ = "";
        String menjogaku1nenji = "";
        String menjogaku23nenji = "";
        final int FORM1J = 11;
        final int FORM2J = 12;
        final int FORM3J = 13;
        final int FORM1H = 1;
        final int FORM2H = 2;
        final int FORM3H = 3;
        final int FORM4 = 4;
        final int FORM5 = 5;
        final int FORM6 = 6;
        final int FORM7H = 7;
        final int FORM8H = 8;
        String honordiv;
        String honordivName;
        if (OUTPUT_DIV4_TOKUTAI_KOUHO.equals(_param._outputDiv)) {
            // 特待生合格通知かつ特待生候補
            honordiv = getString(m, "RECR_JUDGE_KIND");
            honordivName = getString(m, "RECR_JUDGE_KIND_NAME");
        } else {
            if ((OUTPUT_DIV2_JUKENSHA.equals(_param._outputDiv) || OUTPUT_DIV3_SHITEI.equals(_param._outputDiv)) && null == getString(m, "JUDGEDIV")) {
                // 受験者全員を選択した際、特待生候補は特待フォーム
                honordiv = getString(m, "RECR_JUDGE_KIND");
                honordivName = getString(m, "RECR_JUDGE_KIND_NAME");
            } else {
                honordiv = getString(m, "HONORDIV");
                honordivName = getString(m, "HONORDIV_NAME");
            }
        }
        String receptno = getString(m, "RECEPTNO");
        String exemptionCd = m.containsKey("EXEMPTION_CD") ? (String) m.get("EXEMPTION_CD") : null;
        int formdiv = -1;

        int nyugakukin = getIntCheckNyugakukin(m, "ITEM_MONEY01", 250000);
        int kyoikuJujituhi = getIntCheckNyugakukin(m, "ITEM_MONEY04", 135000);
        int nyugakukinKyoikuJujituhi = nyugakukin + kyoikuJujituhi; // 385000

        if ("1".equals(_param._applicantdiv)) {
            if ("5".equals(_param._testdiv) && "1".equals(getString(m, "GENERAL_FLG"))) {
                if ("4".equals(honordiv)) {
                    honordiv = "1";
                }
                final String[] beforeReceptnoHonordiv = getBeforeReceptnoHonordiv(m);
//                final String beforeReceptno = beforeReceptnoHonordiv[0];
                final String beforeHonordiv = beforeReceptnoHonordiv[1];
                if (null == beforeHonordiv || NumberUtils.isDigits(beforeHonordiv) && Integer.parseInt(beforeHonordiv) > Integer.parseInt(honordiv)) {
                    if ("1".equals(honordiv)) {
                        formdiv = FORM4;
//                        if (null != beforeReceptno) {
//                        	receptno = beforeReceptno;
//                        }
                        menjogaku1nenjiJ = String.valueOf(nyugakukinKyoikuJujituhi);
                    } else if ("2".equals(honordiv)) {
                        formdiv = FORM5;
//                        if (null != beforeReceptno) {
//                        	receptno = beforeReceptno;
//                        }
                        menjogaku1nenjiJ = String.valueOf(nyugakukin);
                    } else if ("3".equals(honordiv)) {
                        formdiv = FORM6;
//                        if (null != beforeReceptno) {
//                        	receptno = beforeReceptno;
//                        }
                        menjogaku1nenjiJ = String.valueOf(kyoikuJujituhi);
                    }
                }
            } else if (!"6".equals(_param._testdiv) && !"7".equals(_param._testdiv)) {
                if ("1".equals(honordiv) || "4".equals(honordiv)) {
                    formdiv = FORM1J;
                    menjogaku1nenjiJ = String.valueOf(nyugakukinKyoikuJujituhi);
                } else if ("2".equals(honordiv)) {
                    formdiv = FORM2J;
                    menjogaku1nenjiJ = String.valueOf(nyugakukin);
                } else if ("3".equals(honordiv)) {
                    formdiv = FORM3J;
                    menjogaku1nenjiJ = String.valueOf(kyoikuJujituhi);
                }
            }
        } else if ("2".equals(_param._applicantdiv)) {
            int jugyoryo = getIntCheckNyugakukin(m, "ITEM_MONEY02", 402000);
            int ijikanrihi = getIntCheckNyugakukin(m, "ITEM_MONEY03", 114000);
            final int tokubetsuShisetuhi = 60000; // 特別施設費?
            int all = 0;
            all = nyugakukinKyoikuJujituhi + jugyoryo + ijikanrihi; // 385000 + 402000 + 114000 = 901000
            int jugyoryoIjikanrihiTokubetsuShisetuhi = jugyoryo + ijikanrihi + tokubetsuShisetuhi; // 576000

            if ("1".equals(honordiv)) { // T85
                formdiv = FORM1H;
                menjogaku1nenji = String.valueOf(all);
                menjogaku23nenji = String.valueOf(jugyoryoIjikanrihiTokubetsuShisetuhi);
            } else if ("4".equals(honordiv)) { // T100
                formdiv = FORM7H;
                menjogaku1nenji = String.valueOf(all);
                menjogaku23nenji = String.valueOf(jugyoryoIjikanrihiTokubetsuShisetuhi);
            } else if ("5".equals(honordiv)) { // T0
                formdiv = FORM8H;
                menjogaku1nenji = String.valueOf(nyugakukin);
                menjogaku23nenji = "";
            } else if ("2".equals(honordiv)) { // T3
                formdiv = FORM2H;
                menjogaku1nenji = String.valueOf(nyugakukinKyoikuJujituhi);
                menjogaku23nenji = String.valueOf(jugyoryo);
            } else if ("3".equals(honordiv)) { // T1
                formdiv = FORM3H;
                menjogaku1nenji = String.valueOf(nyugakukinKyoikuJujituhi);
                menjogaku23nenji = String.valueOf(jugyoryo);
            }
        }
        log.info(" receptno = " + receptno + ", formdiv = " + formdiv + " (honordiv = " + honordiv + ", excemption_cd = " + exemptionCd + ")");
        if (-1 == formdiv) {
            return;
        }
        if (OUTPUT4_TOKUTAI_GOKAKU_TUUCHI.equals(_param._output)) {
            // 特待生合格通知
            String formname;
            switch (formdiv) {
            case FORM1J:
            case FORM2J:
            case FORM3J:
                if (formdiv == FORM1J) {
                    formname = "KNJL326F_J4_1.frm";
                } else if (formdiv == FORM2J) {
                    formname = "KNJL326F_J4_2.frm";
                } else {
                    formname = "KNJL326F_J4_3.frm";
                }
                form.setForm(formname);
                break;
            case FORM1H:
            case FORM2H:
            case FORM3H:
            case FORM7H:
            case FORM8H:
                if (formdiv == FORM1H) {
                    formname = "KNJL326F_H4_1.frm";
                } else if (formdiv == FORM2H) {
                    formname = "KNJL326F_H4_2.frm";
                } else if (formdiv == FORM3H) {
                    formname = "KNJL326F_H4_3.frm";
                } else if (formdiv == FORM7H) {
                    formname = "KNJL326F_H4_7.frm";
                } else {
                    formname = "KNJL326F_H4_8.frm";
                }
                form.setForm(formname);
                break;
            case FORM4:
            case FORM5:
            case FORM6:
                if (formdiv == FORM4) {
                    formname = "KNJL326F_J4_4.frm";
                } else if (formdiv == FORM5) {
                    formname = "KNJL326F_J4_5.frm";
                } else {
                    formname = "KNJL326F_J4_6.frm";
                }
                form.setForm(formname);

                form.VrsOut("EXAM_NUM", _param._testdiv); // 試験回数
                form.VrsOut("LIMIT", _param.gethidukeYoubi(db2, _param._teishutsuDate) + "までに、"); // 期限
                break;
            }
            form.VrsOut("DATE", _param.gethiduke(db2, _param._printDate)); // 日付
            VrsOutUnderline(form._svf, "EXAM_NO", receptno); // 受験番号
            VrsOutUnderline(form._svf, "NAME1", addString(getString(m, "NAME"), "　様")); // 氏名
            if (APPLICANTDIV1.equals(_param._applicantdiv)) {
                form.VrsOut("MONEY1", formatCurrency(menjogaku1nenjiJ)); // 免除額
            } else {
                form.VrsOut("MONEY1", formatCurrency(menjogaku1nenji)); // 免除額
                form.VrsOut("MONEY2", formatCurrency(menjogaku23nenji)); // 免除額
            }
            if (formdiv == FORM1H || formdiv == FORM7H || formdiv == FORM8H) {
                form.VrsOut("HONORDIV_MARK", honordivName);
                if (formdiv == FORM8H) { // T0
                    form.VrsOut("UCHIWAKE", "（入学金相当額）");
                }
            }
            printSchoolStampImage(form._svf);
            form.VrsOut("TEACHER_NAME2", _param._goukakutuuchiNyushiTantou); // 担当者
            form.VrsOut("TELNO", _param._goukakutuuchiTelno); // 電話番号
            form.VrsOut("SCHOOL_NAME", ltrim(_param._schoolName)); // 学校名
            form.VrsOut("STAFF_NAME1", StringUtils.defaultString(ltrim(_param._jobName)) + "　　" + StringUtils.defaultString(ltrim(_param._principalName))); // 校長名
            form._svf.VrEndPage();
            _hasData = true;
        } else if (OUTPUT5_TOKUTAI_TUUCHI.equals(_param._output)) {
            // 特待生申請書
            if (APPLICANTDIV1.equals(_param._applicantdiv)) {
                final String formname = "KNJL326F_J5.frm";
                form.setForm(formname);

                if (null != honordivName && honordivName.length() > 0) {
                    form.VrsOut("SCHOLARSHIP_DIV", honordivName.substring(0, 1)); // 年度
                }
                if (_param._seirekiFlg) {
                    form.VrAttribute("ERA_NAME", "UnderLine=(0,2,1),Keta=4," + form.attributeIntPlus("ERA_NAME", "Y", -6) + "," + form.attributeIntPlus("ERA_NAME", "X", 10));
                } else {
                    putGengou1(db2, form._svf, "ERA_NAME");
                }
            } else {
                final String formname;
                if (formdiv == 1) {
                    formname = "KNJL326F_H5_1.frm";
                } else if (formdiv == 2) {
                    formname = "KNJL326F_H5_2.frm";
                } else if (formdiv == 3) {
                    formname = "KNJL326F_H5_3.frm";
                } else if (formdiv == 7) {
                    formname = "KNJL326F_H5_7.frm";
                } else {
                    formname = "KNJL326F_H5_8.frm";
                }
                form.setForm(formname);
                if (_param._seirekiFlg) {
                    form.VrAttribute("ERA_NAME", "UnderLine=(0,2,1),Keta=4," + form.attributeIntPlus("ERA_NAME", "Y", -6) + "," + form.attributeIntPlus("ERA_NAME", "X", 10));
                } else {
                    putGengou1(db2, form._svf, "ERA_NAME");
                }
            }
            form.VrsOut("NENDO", _param._outputNendo); // 年度
            if (APPLICANTDIV1.equals(_param._applicantdiv)) {
                form.VrsOut("MONEY", menjogaku1nenjiJ); // 金額
            } else {
                form.VrsOut("MONEY", menjogaku1nenji); // 金額
            }

            form.VrsOut("CORP_NAME", StringUtils.defaultString(_param._certifSchoolDatRemark6) + "　" + StringUtils.defaultString(_param._certifSchoolDatRemark7)); // 法人名
            form.VrsOut("CHIEF_NAME", StringUtils.defaultString(_param._certifSchoolDatRemark8) + "　" + StringUtils.defaultString(_param._certifSchoolDatRemark9)); // 理事長名
            form.VrsOut("EXAM_NO", receptno); // 受験番号
            if (getMS932Bytecount(getString(m, "NAME")) > 20) {
                form.VrsOut("NAME2", getString(m, "NAME")); // 氏名
            } else {
                form.VrsOut("NAME1", getString(m, "NAME")); // 氏名
            }
            if (null != getString(m, "ZIPCD")) {
                form.VrsOut("ZIPNO", "〒" + getString(m, "ZIPCD")); // 氏名
            }
            final String address1 = getString(m, "ADDRESS1");
            final String address2 = getString(m, "ADDRESS2");
            if (getMS932Bytecount(address1) > 40 || getMS932Bytecount(address2) > 40) {
                if (StringUtils.isBlank(address2)) {
                    form.VrsOut("ADDR2_2", address1); // 住所
                } else {
                    form.VrsOut("ADDR1_2", address1); // 住所
                    form.VrsOut("ADDR2_2", address2); // 住所
                }
            } else {
                if (StringUtils.isBlank(address2)) {
                    form.VrsOut("ADDR2_1", address1); // 住所
                } else {
                    form.VrsOut("ADDR1_1", address1); // 住所
                    form.VrsOut("ADDR2_1", address2); // 住所
                }
            }

            form._svf.VrEndPage();
            _hasData = true;
        }
    }

    private void putGengou1(final DB2UDB db2, final Vrw32alp svf, final String field) {
        if (_param._seirekiFlg) {
            return;
        }
        //元号(記入項目用)
        String[] dwk;
        if (_param._loginDate.indexOf('/') >= 0) {
            dwk = StringUtils.split(_param._loginDate, '/');
        } else if (_param._loginDate.indexOf('-') >= 0) {
            dwk = StringUtils.split(_param._loginDate, '-');
        } else {
            //ありえないので、固定値で設定。
            dwk = new String[1];
        }
        if (dwk.length >= 3) {
            final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(dwk[0]), Integer.parseInt(dwk[1]), Integer.parseInt(dwk[2]));
            svf.VrsOut(field, gengou);
        }
    }

    private void putGengou2(final DB2UDB db2, final Vrw32alp svf, final List<String> fieldList) {
        if (_param._seirekiFlg) {
            return;
        }
        //元号(記入項目用)
        String[] dwk;
        if (_param._loginDate.indexOf('/') >= 0) {
            dwk = StringUtils.split(_param._loginDate, '/');
        } else if (_param._loginDate.indexOf('-') >= 0) {
            dwk = StringUtils.split(_param._loginDate, '-');
        } else {
            //ありえないので、固定値で設定。
            dwk = new String[1];
        }
        if (dwk.length >= 3) {
            final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(dwk[0]), Integer.parseInt(dwk[1]), Integer.parseInt(dwk[2]));
            for (final String setFieldStr : fieldList) {
                svf.VrsOut(setFieldStr, gengou);
            }
        }
    }

    private static String ltrim(final String s) {
        if (null == s) {
            return null;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ' || s.charAt(i) == '　') {
                continue;
            }
            return s.substring(i);
        }
        return "";
    }

    private void VrsOutUnderline(final Vrw32alp svf, final String field, final String data) {
        if (null == data) {
            return;
        }
        svf.VrsOut(field, data);
        svf.VrAttribute(field, "UnderLine=(0,1,1),KETA=" + getMS932Bytecount(data));
    }

    private String formatCurrency(final String money) {
        if (!NumberUtils.isDigits(money)) {
            return null;
        }
        final DecimalFormat df = new DecimalFormat("#,###");
        return toZenkaku(df.format(Long.parseLong(money)));
    }

    private int getIntCheckNyugakukin(final Map m, final String field, final int defval) {
        int rtn = defval;
        if (m.containsKey("ITEM_MONEY01") && NumberUtils.isDigits(getString(m, "ITEM_MONEY01"))) {
            final String v = getString(m, field);
            if (NumberUtils.isDigits(v)) {
                rtn = Integer.parseInt(v);
            }
            log.info(" " + field + " = " + rtn);
        }
        return rtn;
    }

    // 入学金振込み中学
    private void printNyugakukinFurikomiJ(final DB2UDB db2, final Form form, final Map m) {
        final String TEST_DIV6 = "6";
        final String TEST_DIV7 = "7";
        final String TEST_DIV15 = "15";

        final String receptno = getString(m, "RECEPTNO");

        //String title = toZenkaku(_param._gengou) + "年度　入学手続要項";
        String title = toZenkaku(_param._entexamyear) + "年度　入学手続要項";
        final boolean iskikokusei = TEST_DIV6.equals(_param._testdiv) || TEST_DIV7.equals(_param._testdiv);
        String tokutaikubun;
        if ((OUTPUT_DIV2_JUKENSHA.equals(_param._outputDiv) || OUTPUT_DIV3_SHITEI.equals(_param._outputDiv)) && null == getString(m, "JUDGEDIV")) {
            // 受験者全員を選択した際、特待生候補は特待フォーム
            tokutaikubun = getString(m, "RECR_JUDGE_KIND");
        } else {
            tokutaikubun = getString(m, "HONORDIV");
        }
        final boolean isTokutaisei = null != tokutaikubun && !"3".equals(tokutaikubun);

        boolean shimaigenmen = false;
        boolean shijogenmen = false;
        if (!isTokutaisei) {
            shimaigenmen = null != getString(m, "SISTER_FLG");
            shijogenmen = null != getString(m, "MOTHER_FLG");
        }
        log.info(" receptno = " + receptno + ", tokutai = " + isTokutaisei + ", shimai = " + shimaigenmen + ", shijo = " + shijogenmen);

        final String money;
        if (m.containsKey("ITEM_MONEY01") && NumberUtils.isDigits(getString(m, "ITEM_MONEY01"))) {
            final int money0Int = getIntCheckNyugakukin(m, "ITEM_MONEY01", 0) - getIntCheckNyugakukin(m, "EXEMPTION_MONEY01", 0);
            if (money0Int == 0) {
                money = "";
            } else {
                money = String.valueOf(money0Int);
            }
        } else {
            if (iskikokusei) {
                money = "250000";
            } else {
                if (isTokutaisei) {
                    money = "";
                } else {
                    if (shimaigenmen) {
                        money = "125000";
                    } else if (shijogenmen) {
                        money = "125000";
                    } else {
                        money = "250000";
                    }
                }
            }
        }

        final String[] nameMstTestdiv;
        String formname;
        final String footername = "KNJL326F_J6_FOOTER.frm";
        final boolean isGenmen = shimaigenmen || shijogenmen;
        final boolean isGenmenForm = isGenmen && Integer.parseInt(_param._entexamyear) <= 2020;
        if (iskikokusei) {
            nameMstTestdiv = new String[] {TEST_DIV6, TEST_DIV7, TEST_DIV15};
            formname = "KNJL326F_J6_K.frm";
            form.setMergeForm(_param, formname, footername, new String[] {Form.FLG_TATESEN});
            title += "(海外帰国生入試)";

            form.VrsOut("TITLE", title); // タイトル
            form.VrsOut("MONEY1", money); // 納入金額
            form.VrsOut("MONEY1_DEATAIL", ""); // 納入金額詳細
            putGengou2(db2, form._svf, Arrays.asList("ERA_NAME", "ERA_NAME2", "ERA_NAME3"));
            for (int i = 0; i < nameMstTestdiv.length; i++) {
                final String testdiv = nameMstTestdiv[i];
                final Map row = _param.getFurikomiTextDat(testdiv, null, formname);
                final String remark2 = StringUtils.defaultString(getString(row, "REMARK2"));
                final String date1 = StringUtils.defaultString(getString(row, "TETSUZUKI_KIGEN1"));
                final String date2 = StringUtils.defaultString(getString(row, "TETSUZUKI_KIGEN2"));
                form.VrsOut("PAY_TESTDIV_NAME" + String.valueOf(i + 1), remark2); // 手続期限テスト名
                form.VrsOut("PAY_LIMIT" + String.valueOf(i + 1) + "_1", date1); // 手続期限
                form.VrsOut("PAY_LIMIT" + String.valueOf(i + 1) + "_2", date2); // 手続期限
            }
            form.VrsOut("MONEY2", money); // 金額
            form.VrsOut("MONEY3", money); // 金額
            form.VrsOut("MONEY4", money); // 金額

            form.VrsOutn("TEXT_PROC_METHOD", 1, "①　「入学金振込受付証明書」（学校提出用）");
            form.VrsOutn("TEXT_PROC_METHOD", 2, "②　「通知書」");
            form.VrsOutn("TEXT_PROC_METHOD", 3, "");
            form.VrsOutn("TEXT_PROC_METHOD", 4, "① ②を本校事務室へご提出ください。");

            form.VrsOutn("TEXT_PROC_COMPL", 1, "本校では「入学金振込受付証明書」をいただき、合格通知書の下欄に入学許可印");
            form.VrsOutn("TEXT_PROC_COMPL", 2, "を押印し、入学を許可いたします。以上のことがすべて終ると入学手続きが完了し");
            form.VrsOutn("TEXT_PROC_COMPL", 3, "たことになります。");
            form.VrsOutn("TEXT_PROC_COMPL", 4, "金融機関へ振り込み後、ご来校いただかない場合、入学手続きが完了されたこ");
            form.VrsOutn("TEXT_PROC_COMPL", 5, "とになりませんのでご注意ください。");
            form.VrsOutn("TEXT_PROC_COMPL", 6, "");
            form.VrsOutn("TEXT_PROC_COMPL", 7, "※　一度受け付けた入学金は、お返しできませんので予めご了承ください。");

        } else {
            nameMstTestdiv = new String[] {"1", "16", "18", "9", "12", "2", "3", "10", "13", "17"};

            if (isTokutaisei) {
                formname = "KNJL326F_J6_TOKUTAI.frm";
                form.setMergeForm(_param, formname, footername, new String[] {Form.FLG_TOKUTAI});

                form.VrsOut("TITLE", title); // タイトル

                form.VrAttribute("MONEY2", "X=10000");
                form.VrAttribute("MONEY3", "X=10000");
                form.VrAttribute("MONEY4", "X=10000");
                form.VrsOut("MENJO1", "入学金免除");
                form.VrsOut("MENJO2", "入学金免除");
                form.VrsOut("MENJO3", "入学金免除");
                form.VrAttribute("MENJO1", "Palette=15,Italic=1"); // 赤字、斜体
                form.VrAttribute("MENJO2", "Palette=15,Italic=1"); // 赤字、斜体
                form.VrAttribute("MENJO3", "Palette=15,Italic=1"); // 赤字、斜体

            } else {
                String genmen = null;
                if (isGenmenForm) {
                    genmen = "減免";
                    formname = "KNJL326F_J6_1_GENMEN.frm";
                    form.setMergeForm(_param, formname, footername, new String[] {Form.FLG_TATESEN});
                    form.VrsOut("MONEY1_DEATAIL", "(" + genmen + "対象)"); // 納入金額詳細
//                    svf.VrsOut("GENMEN_TITLE", genmen);
                } else {
                    formname = "KNJL326F_J6_1.frm";
                    form.setMergeForm(_param, formname, footername, new String[] {Form.FLG_TATESEN});
                }

                form.VrsOut("TITLE", title); // タイトル
                form.VrsOut("MONEY1", money); // 納入金額

                form.VrsOut("MONEY2", money); // 金額
                form.VrsOut("MONEY3", money); // 金額
                form.VrsOut("MONEY4", money); // 金額

            }
            putGengou2(db2, form._svf, Arrays.asList("ERA_NAME", "ERA_NAME2", "ERA_NAME3"));

            for (int i = 0; i < nameMstTestdiv.length; i++) {
                final String testdiv = nameMstTestdiv[i];
                final Map row = _param.getFurikomiTextDat(testdiv, null, formname);
                final String remark2 = StringUtils.defaultString(getString(row, "REMARK2"));
                final String date1 = StringUtils.defaultString(getString(row, "TETSUZUKI_KIGEN1"));
                final String date2 = StringUtils.defaultString(getString(row, "TETSUZUKI_KIGEN2"));
                form.VrsOutn("PAY_TESTDIV_NAME", i + 1, remark2); // 手続期限テスト名
                form.VrsOutn("PAY_LIMIT", i + 1, date1 + date2); // 手続期限
            }

            if (isTokutaisei) {
                form.VrsOutn("TEXT_PROC_METHOD", 1, "①　本紙下部、入学金振込用紙");
                form.VrsOutn("TEXT_PROC_METHOD", 2, "②　「通知書」　");
                form.VrsOutn("TEXT_PROC_METHOD", 3, "③　特待生申請書");
                form.VrsOutn("TEXT_PROC_METHOD", 4, "");
                form.VrsOutn("TEXT_PROC_METHOD", 5, "①・②・③を本校事務室へご提出ください。");

            } else if (isGenmenForm) {
                form.VrsOutn("TEXT_PROC_METHOD", 1, "①　「入学金振込受付証明書」（学校提出用）");
                form.VrsOutn("TEXT_PROC_METHOD", 2, "②　「通知書」");
                form.VrsOutn("TEXT_PROC_METHOD", 3, "③　文京学院大学女子高等学校・中学校入学金減免申請書");
                form.VrsOutn("TEXT_PROC_METHOD", 4, "④　入学者と文京学園鏡友会・校友会会員または在校生との関係を");
                form.VrsOutn("TEXT_PROC_METHOD", 5, "　　証する書類（住民票または戸籍謄本等）");
                form.VrsOutn("TEXT_PROC_METHOD", 6, "①～④を本校事務室へご提出ください。");
                form.VrsOutn("TEXT_PROC_METHOD", 7, "※　③④は７日以内であれば手続き後のご提出でも結構です。");
            } else {
                form.VrsOutn("TEXT_PROC_METHOD", 1, "①　「入学金振込受付証明書」（学校提出用）");
                form.VrsOutn("TEXT_PROC_METHOD", 2, "②　「通知書」");
                form.VrsOutn("TEXT_PROC_METHOD", 3, "");
                form.VrsOutn("TEXT_PROC_METHOD", 4, "① ②を本校事務室へご提出ください。");
            }

            if (isTokutaisei) {
                form.VrsOutn("TEXT_PROC_COMPL", 1, "本校では「入学金振込受付証明書」をいただき、合格通知書の下欄に入学許可印");
                form.VrsOutn("TEXT_PROC_COMPL", 2, "を押印し、入学を許可いたします。以上のことがすべて終ると入学手続きが完了し");
                form.VrsOutn("TEXT_PROC_COMPL", 3, "たことになります。");
            } else {
                form.VrsOutn("TEXT_PROC_COMPL", 1, "本校では「入学金振込受付証明書」をいただき、合格通知書の下欄に入学許可印");
                form.VrsOutn("TEXT_PROC_COMPL", 2, "を押印し、入学を許可いたします。以上のことがすべて終ると入学手続きが完了し");
                form.VrsOutn("TEXT_PROC_COMPL", 3, "たことになります。");
                form.VrsOutn("TEXT_PROC_COMPL", 4, "金融機関へ振り込み後、ご来校いただかない場合、入学手続きが完了されたこ");
                form.VrsOutn("TEXT_PROC_COMPL", 5, "とになりませんのでご注意ください。");
                form.VrsOutn("TEXT_PROC_COMPL", 6, "");
                form.VrsOutn("TEXT_PROC_COMPL", 7, "※　一度受け付けた入学金は、お返しできませんので予めご了承ください。");
            }
        }

        String setsumeikaiHiduke = null;
        String setsumeikaiBasho = null;
        String hanbaiHiduke = null;
        String hanbaiBasho = null;
        String remark3 = null;
        String remark4 = null;
        String remark5 = null;
        for (int i = 0; i < nameMstTestdiv.length; i++) {
            final Map row = _param.getFurikomiTextDat(nameMstTestdiv[i], null, formname);
            if (StringUtils.isBlank(setsumeikaiHiduke)) {
                setsumeikaiHiduke = StringUtils.defaultString(getString(row, "SETSUMEIKAI_NICHIJI"));
            }
            if (StringUtils.isBlank(setsumeikaiBasho)) {
                setsumeikaiBasho = getString(row, "SETSUMEIKAI_BASHO");
            }
            if (StringUtils.isBlank(hanbaiHiduke)) {
                hanbaiHiduke = StringUtils.defaultString(getString(row, "HANBAI_NICHIJI"));
            }
            if (StringUtils.isBlank(hanbaiBasho)) {
                hanbaiBasho = getString(row, "HANBAI_BASHO");
            }
            if (StringUtils.isBlank(remark3)) {
                remark3 = getString(row, "REMARK3");
            }
            if (StringUtils.isBlank(remark4)) {
                remark4 = getString(row, "REMARK4");
            }
            if (StringUtils.isBlank(remark5)) {
                remark5 = getString(row, "REMARK5");
            }
        }

        // ３.　制服・体操着等　出張採寸日
        form.VrsOut("TEXT_MEASURE_TITLE", "３.　採寸確認会　（完全予約制）");
        form.VrAttribute("TEXT_MEASURE_TITLE", "Bold=1");
        form.VrsOutn("TEXT_MEASURE", 1, remark3);
        form.VrsOutn("TEXT_MEASURE", 2, remark4);
        form.VrsOutn("TEXT_MEASURE", 3, remark5);

        // ４.　入学準備説明会（新入生および保護者対象）
        form.VrsOut("TEXT_MEETING_TITLE", "４.　入学準備説明会（新入生および保護者対象）");
        form.VrAttribute("TEXT_MEETING_TITLE", "Bold=1");
        form.VrsOutn("TEXT_MEETING", 1, setsumeikaiHiduke);
        form.VrsOutn("TEXT_MEETING", 2, setsumeikaiBasho);
        form.VrsOutn("TEXT_MEETING", 3, "※　新型コロナウイルス感染拡大に伴い、入学準備説明会の日程が変更に");
        form.VrsOutn("TEXT_MEETING", 4, "なる場合があります。");
        form.VrsOutn("TEXT_MEETING", 5, "その際には追ってご連絡を差し上げます。");
        form.VrAttributen("TEXT_MEETING", 3, "Bold=1");
        form.VrAttributen("TEXT_MEETING", 4, "Bold=1");
        form.VrAttributen("TEXT_MEETING", 5, "Bold=1");

        // ５.　学用品販売
        form.VrsOut("TEXT_SELL_TITLE", "５.　学用品販売");
        form.VrAttribute("TEXT_SELL_TITLE", "Bold=1");
        form.VrsOutn("TEXT_SELL", 1, hanbaiHiduke);
        form.VrsOutn("TEXT_SELL", 2, hanbaiBasho);


        form.VrsOut("EXAM_NO", receptno); // 受験番号
        form.VrsOut("EXAM_NO2", receptno); // 受験番号
        form.VrsOut("EXAM_NO3", receptno); // 受験番号
        form.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名

        form.VrsOut("NAME", getString(m, "NAME"));
        form.VrsOut("NAME2", getString(m, "NAME"));
        form.VrsOut("NAME3", getString(m, "NAME"));
        form.VrsOut("KANA3", getString(m, "NAME_KANA_HK"));
//        form.VrsOut("GNAME", getString(m, "GNAME"));
//        form.VrsOut("GNAME2", getString(m, "GNAME"));
//        final String addr = StringUtils.defaultString(getString(m, "ADDRESS1")) + StringUtils.defaultString(getString(m, "ADDRESS2"));
//        if (getMS932Bytecount(addr) > 50) {
//            final String[] split = KNJ_EditEdit.get_token(addr, 50, 2);
//            if (null != split) {
//                for (int i = 0; i < split.length; i++) {
//                    form.VrsOut("ADDR_" + String.valueOf(i + 1), split[i]);
//                }
//            }
//        } else {
//            form.VrsOut("ADDR", addr);
//        }
//        if (getMS932Bytecount(addr) > 44) {
//            final String[] split = KNJ_EditEdit.get_token(addr, 30, 4);
//            if (null != split) {
//                for (int i = 0; i < split.length; i++) {
//                    form.VrsOut("ADDR3_" + String.valueOf(i + 1), split[i]);
//                }
//            }
//        } else if (getMS932Bytecount(addr) > 22) {
//            final String[] split = KNJ_EditEdit.get_token(addr, 22, 2);
//            if (null != split) {
//                for (int i = 0; i < split.length; i++) {
//                    form.VrsOut("ADDR2_" + String.valueOf(i + 1), split[i]);
//                }
//            }
//        } else {
//            form.VrsOut("ADDR2", addr);
//        }
        form._svf.VrEndPage();
        _hasData = true;
    }

//    private String formatDateTime2(final String date, final String youbiAppend, final String comment) {
//        if (null == date) {
//            return null;
//        }
//        try {
//            final StringBuffer stb = new StringBuffer();
//            final Calendar cal = Calendar.getInstance();
//            cal.setTime(Date.valueOf(date));
//            stb.append(KNJ_EditDate.h_format_JP(date));
//            stb.append("(").append(" 日月火水木金土".charAt(cal.get(Calendar.DAY_OF_WEEK))).append(youbiAppend).append(")");
//            stb.append(comment);
//            return stb.toString();
//        } catch (Exception e) {
//            log.error("exception!", e);
//        }
//        return null;
//    }

//    private String formatDateTime(final String date, final int startHour, final String startComment, final int endHour, final String endComment) {
//        if (null == date) {
//            return null;
//        }
//        try {
//            final StringBuffer stb = new StringBuffer();
//            final Calendar cal = Calendar.getInstance();
//            cal.setTime(Date.valueOf(date));
//            stb.append(KNJ_EditDate.h_format_JP(date));
//            stb.append("(").append(" 日月火水木金土".charAt(cal.get(Calendar.DAY_OF_WEEK))).append(")");
//            stb.append(startHour < 12 ? "午前" : "午後").append(toZenkaku(String.valueOf(startHour < 12 ? startHour : (startHour - 12)))).append("時").append(startComment);
//            stb.append(endHour < 12 ? "午前" : "午後").append(toZenkaku(String.valueOf(endHour < 12 ? endHour : (endHour - 12)))).append("時").append(endComment);
//            return stb.toString();
//        } catch (Exception e) {
//            log.error("exception!", e);
//        }
//        return null;
//    }

//    private String formatDateMDDow(final String date) {
//        if (null == date) {
//            return null;
//        }
//        try {
//            final StringBuffer stb = new StringBuffer();
//            final Calendar cal = Calendar.getInstance();
//            cal.setTime(Date.valueOf(date));
//            stb.append(cal.get(Calendar.MONTH) + 1).append("月");
//            stb.append(cal.get(Calendar.DAY_OF_MONTH)).append("日");
//            stb.append("(").append(" 日月火水木金土".charAt(cal.get(Calendar.DAY_OF_WEEK))).append(")");
//            return stb.toString();
//        } catch (Exception e) {
//            log.error("exception!", e);
//        }
//        return null;
//    }

    // 入学金振込み高校
    private void printNyugakukinFurikomiH(final DB2UDB db2, final Form form, final Map m) {
        final String TESTDIV1_A_SUISEN = "1";
        final String TESTDIV2_B_SUISEN = "2";
        final String TESTDIV3_IPPAN = "3";
        final String TESTDIV4_KIKOKUSEI_A = "4";
        final String TESTDIV5_KIKOKUSEI_B = "5";
        final String TESTDIV6_IKKANSEI = "6";
        // 特待生候補
        boolean isPrintStudySupport = false;
        final String tokutaikubun;
        if ((OUTPUT_DIV2_JUKENSHA.equals(_param._outputDiv) || OUTPUT_DIV3_SHITEI.equals(_param._outputDiv)) && null == getString(m, "JUDGEDIV")) {
            // 受験者全員を選択した際、特待生候補は特待フォーム
            tokutaikubun = getString(m, "RECR_JUDGE_KIND");
        } else {
            tokutaikubun = getString(m, "HONORDIV");
        }
        final boolean isTokyoto = "1".equals(getString(m, "ENTRANCE_FLG"));
        final boolean isTokutai = null != tokutaikubun;
        boolean shimaigenmen = false;
        boolean shijogenmen = false;
        if (null == tokutaikubun) {
            shimaigenmen = null != getString(m, "SISTER_FLG");
            shijogenmen = null != getString(m, "MOTHER_FLG");
        }
        final boolean isGenmen = shimaigenmen || shijogenmen;
        if (_param._isOutputDebug) {
            final String examno = getString(m, "EXAMNO");
            log.info(" examno = " + examno + ", tokutai = " + isTokutai + ", shimai genmen = " + shimaigenmen + ", shijo genmen = " + shijogenmen);
        }
        final String receptno = getString(m, "RECEPTNO");
        final String money; // 入学金免除
        if (isTokyoto) {
            money = "0";
        } else if (m.containsKey("ITEM_MONEY01") && NumberUtils.isDigits(getString(m, "ITEM_MONEY01"))) {
            final int money0Int = getIntCheckNyugakukin(m, "ITEM_MONEY01", 0) - getIntCheckNyugakukin(m, "EXEMPTION_MONEY01", 0);
            if (money0Int == 0) {
                money = "";
            } else {
                money = String.valueOf(money0Int);
            }
        } else {
            if (isTokutai) {
                money = null;
            } else if (isGenmen) {
                money = "125000";
            } else {
                money = "250000";
            }
        }
        final String formname;
        final String footername = "KNJL326F_H6_FOOTER.frm";
        final String[] furikomiTextTestdiv;
        if (TESTDIV4_KIKOKUSEI_A.equals(_param._testdiv) || TESTDIV5_KIKOKUSEI_B.equals(_param._testdiv) || TESTDIV6_IKKANSEI.equals(_param._testdiv)) {
            furikomiTextTestdiv = new String[] {_param._testdiv};
            if (TESTDIV4_KIKOKUSEI_A.equals(_param._testdiv)) { // 帰国生A
                if (isTokutai) {
                    formname = "KNJL326F_H6_KA_TOKUTAI.frm";
                } else if (isGenmen) {
                    formname = "KNJL326F_H6_KA_GENMEN.frm";
                } else if (isTokyoto) {
                    formname = "KNJL326F_H6_KA_TOKYO.frm";
                } else {
                    formname = "KNJL326F_H6_KA_1.frm";
                }
                isPrintStudySupport = true;
                form.setForm(formname);
            } else if (TESTDIV5_KIKOKUSEI_B.equals(_param._testdiv)) { // 帰国生B
                if (isTokutai) {
                    formname = "KNJL326F_H6_KB_TOKUTAI.frm";
                } else if (isGenmen) {
                    formname = "KNJL326F_H6_KB_GENMEN.frm";
                } else if (isTokyoto) {
                    formname = "KNJL326F_H6_KB_TOKYO.frm";
                } else {
                    formname = "KNJL326F_H6_KB_1.frm";
                }
                isPrintStudySupport = true;
                form.setForm(formname);
            } else { // if (TESTDIV6.equals(_param._testdiv)) { // 一貫生
                final String[] flg;
                if (isTokutai) {
                    formname = "KNJL326F_H6_IKKAN_TOKUTAI.frm";
                    flg = new String[] {Form.FLG_TOKUTAI};
                } else if (isTokyoto) {
                    formname = "KNJL326F_H6_IKKAN_TOKYO.frm";
                    flg = new String[] {Form.FLG_SHITAKUKIN};
                } else {
                    formname = "KNJL326F_H6_IKKAN_1.frm";
                    if (isGenmen) {
                        flg = new String[] {Form.FLG_TATESEN, Form.FLG_GENMEN};
                    } else {
                        flg = new String[] {Form.FLG_TATESEN};
                    }
                }
                form.setMergeForm(_param, formname, footername, flg);
                if (isTokutai || isTokyoto) {
                    form.VrAttribute("MONEY2", "X=10000");
                    form.VrAttribute("MONEY3", "X=10000");
                    form.VrAttribute("MONEY4", "X=10000");
                    if (isTokutai) {
                        for (int i = 1; i <= 3; i++) {
                            final String field = "MONTXT" + String.valueOf(i);
                            form.VrsOut(field, "入学金免除");
                            form.VrAttribute(field, "Palette=15"); // 赤字、斜体
                        }
                    } else if (isTokyoto) {
                        for (int i = 1; i <= 3; i++) {
                            final String field = "MONTXT" + String.valueOf(i);
                            form.VrsOut(field, "入学支度金利用");
                            form.VrAttribute(field, "Palette=14"); // 青字、斜体
                        }
                    }
                }
            }
            putGengou2(db2, form._svf, Arrays.asList("ERA_NAME", "ERA_NAME2", "ERA_NAME3"));
            if (TESTDIV5_KIKOKUSEI_B.equals(_param._testdiv)) { // 帰国生B
                for (int iTestDiv0 = 1; iTestDiv0 <= 2; iTestDiv0++) {
                    final String testCount = String.valueOf(iTestDiv0);
                    final Map dat = _param.getFurikomiTextDat(_param._testdiv, testCount, formname);
                    if (!isTokutai) {
                        form.VrsOut("PAY_LIMIT" + testCount + "_1", addString(getString(dat, "NOUNYU_KIGEN1"), FROM_TO_MARK)); // 納入期限
                        form.VrsOut("PAY_LIMIT" + testCount + "_2",           getString(dat, "NOUNYU_KIGEN2")); // 納入期限
                    }
                    form.VrsOut("PROCEDURE_LIMIT" + testCount, getString(dat, "TETSUZUKI_KIGEN1")); // 入学手続期限
                }
            } else {
                if (!isTokutai) {
                    form.VrsOut("PAY_LIMIT1_1", getString(_param.getFurikomiTextDat(_param._testdiv, null, formname), "NOUNYU_KIGEN1")); // 納入期限
                }
                form.VrsOut("PROCEDURE_LIMIT1", getString(_param.getFurikomiTextDat(_param._testdiv, null, formname), "TETSUZUKI_KIGEN1")); // 入学手続期限
            }
        } else { // 入試区分 1:A推薦 2:B推薦 3:一般
            furikomiTextTestdiv = new String[] {TESTDIV1_A_SUISEN, TESTDIV2_B_SUISEN, TESTDIV3_IPPAN};

            final String[] flg;
            if (isTokutai) {
                formname = "KNJL326F_H6_1_TOKUTAI.frm";
                flg = new String[] {Form.FLG_TOKUTAI};
            } else if (isGenmen) {
                formname = "KNJL326F_H6_1_GENMEN.frm";
                flg = new String[] {Form.FLG_TATESEN, Form.FLG_GENMEN};
            } else if (isTokyoto) {
                formname = "KNJL326F_H6_1_TOKYO.frm";
                flg = new String[] {Form.FLG_SHITAKUKIN};
            } else {
                formname = "KNJL326F_H6_1_1.frm";
                flg = new String[] {Form.FLG_TATESEN};
            }
            form.setMergeForm(_param, formname, footername, flg);
            if (isTokutai || isTokyoto) {
                form.VrAttribute("MONEY2", "X=10000");
                form.VrAttribute("MONEY3", "X=10000");
                form.VrAttribute("MONEY4", "X=10000");
                if (isTokutai) {
                    for (int i = 1; i <= 3; i++) {
                        final String field = "MONTXT" + String.valueOf(i);
                        form.VrsOut(field, "入学金免除");
                        form.VrAttribute(field, "Palette=15"); // 赤字、斜体
                    }
                } else if (isTokyoto) {
                    for (int i = 1; i <= 3; i++) {
                        final String field = "MONTXT" + String.valueOf(i);
                        form.VrsOut(field, "入学支度金利用");
                        form.VrAttribute(field, "Palette=14"); // 青字、斜体
                    }
                }
            }
            isPrintStudySupport = true;
            final Map datTestdiv1 = _param.getFurikomiTextDat(TESTDIV1_A_SUISEN, null, formname);
            final Map datTestdiv2 = _param.getFurikomiTextDat(TESTDIV2_B_SUISEN, null, formname);
            final Map datTestdiv3 = _param.getFurikomiTextDat(TESTDIV3_IPPAN, null, formname);
            if (!isTokutai) {
                form.VrsOut("PAY_LIMIT1_1", addString(getString(datTestdiv1, "NOUNYU_KIGEN1"), FROM_TO_MARK)); // 納入期限
                form.VrsOut("PAY_LIMIT1_2",           getString(datTestdiv1, "NOUNYU_KIGEN2")); // 納入期限
                if (null != getString(datTestdiv1, "TETSUZUKI_KIGEN2")) {
                    form.VrsOut("PAY_LIMIT1_3", "＊入学手続きは、" + getString(datTestdiv1, "TETSUZUKI_KIGEN2") + "まで"); // 納入期限
                }
                form.VrsOut("PAY_LIMIT2_1", addString(getString(datTestdiv2, "NOUNYU_KIGEN1"), FROM_TO_MARK)); // 納入期限
                form.VrsOut("PAY_LIMIT2_2",           getString(datTestdiv2, "NOUNYU_KIGEN2")); // 納入期限
                form.VrsOut("PAY_LIMIT3_1", addString(getString(datTestdiv3, "NOUNYU_KIGEN1"), FROM_TO_MARK)); // 納入期限
                form.VrsOut("PAY_LIMIT3_2",           getString(datTestdiv3, "NOUNYU_KIGEN2")); // 納入期限
            }
            putGengou2(db2, form._svf, Arrays.asList("ERA_NAME", "ERA_NAME2", "ERA_NAME3"));
            form.VrsOut("PROCEDURE_LIMIT1", getString(datTestdiv1, "TETSUZUKI_KIGEN1")); // 入学手続期限
            form.VrsOut("PROCEDURE_LIMIT2", getString(datTestdiv2, "TETSUZUKI_KIGEN1")); // 入学手続期限
            form.VrsOut("PROCEDURE_LIMIT3", getString(datTestdiv3, "TETSUZUKI_KIGEN1")); // 入学手続期限
        }
        form.VrsOut("BANKNAME", "三菱ＵＦＪ銀行　駒込支店");
        form.VrsOut("BANKNAME2", "三菱ＵＦＪ銀行");
        if (null != money) {
            form.VrsOut("MONEY1", money); // 納入金額
            form.VrsOut("MONEY2", money); // 金額
            form.VrsOut("MONEY3", money); // 金額
            form.VrsOut("MONEY4", money); // 金額
        }
        String testNichiji = null;
        //String testBasho = null;
        String setsumeikaiNichiji = null;
        //String setsumeikaiBasho = null;
        String hanbaiNichiji = null;
        //String hanbaiBasho = null;
        String studySupportNichiji = null;
        String remark1 = null;
        for (int i = 0; i < furikomiTextTestdiv.length; i++) {
            final Map dat = _param.getFurikomiTextDat(furikomiTextTestdiv[i], null, formname);
            if (StringUtils.isBlank(testNichiji)) {
                testNichiji = StringUtils.defaultString(getString(dat, "TEST_NICHIJI"));
            }
//            if (StringUtils.isBlank(testBasho)) {
//                testBasho = getString(dat, "TEST_BASHO");
//            }
            if (StringUtils.isBlank(setsumeikaiNichiji)) {
                setsumeikaiNichiji = StringUtils.defaultString(getString(dat, "SETSUMEIKAI_NICHIJI"));
            }
//            if (StringUtils.isBlank(setsumeikaiBasho)) {
//                setsumeikaiBasho = getString(dat, "SETSUMEIKAI_BASHO");
//            }
            if (StringUtils.isBlank(hanbaiNichiji)) {
                hanbaiNichiji = StringUtils.defaultString(getString(dat, "HANBAI_NICHIJI"));
            }
//            if (StringUtils.isBlank(hanbaiBasho)) {
//                hanbaiBasho = getString(dat, "HANBAI_BASHO");
//            }
            if (isPrintStudySupport && StringUtils.isBlank(studySupportNichiji)) {
                studySupportNichiji = getString(dat, "STUDY_SUPPORT_NICHIJI");
            }
            if (StringUtils.isBlank(remark1)) {
                remark1 = getString(dat, "REMARK1");
            }
        }

        if (TESTDIV6_IKKANSEI.equals(_param._testdiv)) {

            final String tetsuzukiKigen1 = StringUtils.defaultString(getString(_param.getFurikomiTextDat(_param._testdiv, null, formname), "TETSUZUKI_KIGEN1"));
            if (isTokutai) {
                form.VrsOutn("TEXT_PROC_METHOD",  1, "①「入学金振込受付証明書」（学校提出用）");
                form.VrsOutn("TEXT_PROC_METHOD",  2, "② 特待生申請書");
                form.VrsOutn("TEXT_PROC_METHOD",  3, "");
                form.VrsOutn("TEXT_PROC_METHOD",  4, "");
                form.VrsOutn("TEXT_PROC_METHOD2", 5, "①②を事務室にご提出ください。　　提出期限：　" + tetsuzukiKigen1);
                form.VrAttributen("TEXT_PROC_METHOD2", 5, "ZenFont=0"); // 明朝体

            } else if (isTokyoto) {
                form.VrsOutn("TEXT_PROC_METHOD",  1, "①「入学金振込受付証明書」（学校提出用）");
                form.VrsOutn("TEXT_PROC_METHOD",  2, "② 住民票1通（申請から3カ月以内、世帯全員の記載・続柄の入ったもの）");
                form.VrsOutn("TEXT_PROC_METHOD",  3, "③ 入学支度金借入申込書");
                form.VrsOutn("TEXT_PROC_METHOD",  4, "④ 入学支度金借用証書（収入印紙を貼付した物）");
                form.VrsOutn("TEXT_PROC_METHOD",  5, "");
                form.VrsOutn("TEXT_PROC_METHOD",  6, "");
                form.VrsOutn("TEXT_PROC_METHOD2", 7, "※　①を本校事務室にご提出ください。　提出期限：　" + tetsuzukiKigen1);
                form.VrsOutn("TEXT_PROC_METHOD2", 8, "※　③④は入学手続き時に配布いたします。入学手続き後3日以内に②～④をご提出ください。");
                form.VrAttributen("TEXT_PROC_METHOD2", 7, "ZenFont=0"); // 明朝体
                form.VrAttributen("TEXT_PROC_METHOD2", 8, "ZenFont=0"); // 明朝体
                form.VrsOutn("TEXT_PROC_METHOD",  9, "");
                form.VrsOutn("TEXT_PROC_METHOD", 10, "※①～④の提出は、生徒本人が直接事務室窓口へご提出いただいても差し支えありません。");
            } else {
                if (isGenmen) {
                    form.VrsOutn("TEXT_PROC_METHOD",  1, "①「入学金振込受付証明書」（学校提出用）");
                    form.VrsOutn("TEXT_PROC_METHOD",  2, "② 文京学院大学女子中学校高等学校入学金減免申請書");
                    form.VrsOutn("TEXT_PROC_METHOD",  3, "③ 入学者と文京学園鏡友会・校友会会員または在校生との関係を");
                    form.VrsOutn("TEXT_PROC_METHOD",  4, "　　証する書類（住民票または戸籍謄本等）");
                    form.VrsOutn("TEXT_PROC_METHOD2", 5, "①②③を事務室にご提出ください。　提出期限：　" + tetsuzukiKigen1);
                    form.VrAttributen("TEXT_PROC_METHOD2", 5, "ZenFont=0"); // 明朝体
                    form.VrsOutn("TEXT_PROC_METHOD",  6, "③は7日以内であれば手続後のご提出でも差し支えありません。");
                    form.VrsOutn("TEXT_PROC_METHOD",  7, "");
                    form.VrsOutn("TEXT_PROC_METHOD",  8, "");
                    form.VrsOutn("TEXT_PROC_METHOD",  9, "※①②③の提出は、生徒本人が直接事務室窓口へご提出いただいても差し支えありません。");
                } else {
                    form.VrsOutn("TEXT_PROC_METHOD" , 1, "「入学金振込受付証明書」（学校提出用）を本校事務室にご提出ください。");
                    form.VrsOutn("TEXT_PROC_METHOD" , 2, "");
                    form.VrsOutn("TEXT_PROC_METHOD" , 3, "※提出期限：　" + tetsuzukiKigen1);
                    form.VrsOutn("TEXT_PROC_METHOD" , 4, "※「入学金振込受付証明書」は、生徒本人が直接事務室窓口へご提出いただいても差し支えありません。");
                }
            }

//        	form.VrsOut("MOCK_DATE", testNichiji); // 学力診断テスト日時
//      form.VrsOut("MOCK_PLACE", testBasho); // 学力診断テスト場所
//        	form.VrsOut("MEETING_DATE", setsumeikaiNichiji); // 入学準備説明会日時
//      form.VrsOut("MEETING_PLACE", setsumeikaiBasho); // 入学準備説明会場所
//        	form.VrsOut("SELL_DATE", hanbaiNichiji); // 学用品販売日時
//      form.VrsOut("SELL_PLACE", hanbaiBasho); // 学用品販売場所
//        	form.VrsOut("SUPPORT_DATE", studySupportNichiji); // スタディーサポート日時
//        	form.VrsOut("MEASURE_DATE", remark1); // 採寸日時
        } else {
            if (TESTDIV4_KIKOKUSEI_A.equals(_param._testdiv)) {
                form.VrsOut("PROCEDURE_TIME", "受付時間：月曜～土曜　9:00～15:30");
            } else if (TESTDIV5_KIKOKUSEI_B.equals(_param._testdiv)) {
                form.VrsOut("PROCEDURE_TIME", "受付時間：平日　9:00～15:30・土曜　9:00～14:00");
            } else {
                form.VrsOut("PROCEDURE_TIME", "受付時間：月曜～土曜　9:00～15:30・日曜　9:00～14:00");
            }

            // 入学手続方法
            form.VrsOutn("TEXT_PROC_METHOD" , 1, "①「入学金振込受付証明書」（学校提出用）");
            form.VrsOutn("TEXT_PROC_METHOD" , 2, "②「通知書」");
            form.VrsOutn("TEXT_PROC_METHOD2", 2, "　　　　　　＊合格発表時に発送");
            form.VrAttributen("TEXT_PROC_METHOD2", 2, "ZenFont=0"); // 明朝体
            if (isTokutai) {
                form.VrsOutn("TEXT_PROC_METHOD", 3, "③ 特待生申請書");
                form.VrsOutn("TEXT_PROC_METHOD", 4, "");
                form.VrsOutn("TEXT_PROC_METHOD", 5, "①②③を本校事務室にご提出ください。");
                for (int gyo = 5; gyo <= 5; gyo++) {
                    form.VrAttributen("TEXT_PROC_METHOD", gyo, "ZenFont=0"); // 明朝体
                }
            } else if (isGenmen) {
                form.VrsOutn("TEXT_PROC_METHOD", 3, "③ 文京学院大学女子中学校高等学校入学金減免申請書");
                form.VrsOutn("TEXT_PROC_METHOD", 4, "④ 入学者と文京学園鏡友会・校友会会員または在校生との関係を");
                form.VrsOutn("TEXT_PROC_METHOD", 5, "　 証する書類（住民票または戸籍謄本等）");
                form.VrsOutn("TEXT_PROC_METHOD", 6, "①～④を本校事務室にご提出ください。");
                form.VrsOutn("TEXT_PROC_METHOD", 7, "③④は7日以内であれば手続後のご提出でも差し支えありません。");
                for (int gyo = 6; gyo <= 7; gyo++) {
                    form.VrAttributen("TEXT_PROC_METHOD", gyo, "ZenFont=0"); // 明朝体
                }
            } else if (isTokyoto) {
                form.VrsOutn("TEXT_PROC_METHOD", 3, "③ 住民票１通（申請から3カ月以内、世帯全員の記載・続柄の入ったもの）");
                form.VrsOutn("TEXT_PROC_METHOD", 4, "④ 入学支度金借入申込書");
                form.VrsOutn("TEXT_PROC_METHOD", 5, "⑤ 入学支度金借用証書（収入印紙を貼付したもの）");
                if (TESTDIV4_KIKOKUSEI_A.equals(_param._testdiv)) {
                    form.VrsOutn("TEXT_PROC_METHOD", 6, "");
                    form.VrsOutn("TEXT_PROC_METHOD", 7, "①②を本校事務室にご提出ください。");
                    form.VrsOutn("TEXT_PROC_METHOD", 8, "④⑤は入学手続き時に配布いたします。入学手続後3日以内に③～⑤をまとめてご提出ください。");
                    for (int gyo = 7; gyo <= 8; gyo++) {
                        form.VrAttributen("TEXT_PROC_METHOD", gyo, "ZenFont=0"); // 明朝体
                    }
                } else {
                    form.VrsOutn("TEXT_PROC_METHOD", 6, "※　①②を本校事務室にご提出ください。");
                    form.VrsOutn("TEXT_PROC_METHOD", 7, "※　④⑤は入学手続き時に配布いたします。入学手続後3日以内に③～⑤をご提出ください。");
                    for (int gyo = 6; gyo <= 7; gyo++) {
                        form.VrAttributen("TEXT_PROC_METHOD", gyo, "ZenFont=0"); // 明朝体
                    }
                }
            } else {
                form.VrsOutn("TEXT_PROC_METHOD", 3, "");
                form.VrsOutn("TEXT_PROC_METHOD", 4, "①②を本校事務室にご提出ください。");
                for (int gyo = 3; gyo <= 4; gyo++) {
                    form.VrAttributen("TEXT_PROC_METHOD", gyo, "ZenFont=0"); // 明朝体
                }
            }

            // 入学手続期限
            if (!TESTDIV4_KIKOKUSEI_A.equals(_param._testdiv)) {
                form.VrsOutn("TEXT_PROC_LIMIT_REMARK", 1, "※　国立・公立・私立高校を併願受験し、「入学手続延期願（出願の際に提出）」");
                form.VrsOutn("TEXT_PROC_LIMIT_REMARK", 2, "　　を提出した場合は、その併願校の合格発表翌日15:30まで入学手続延期を");
                form.VrsOutn("TEXT_PROC_LIMIT_REMARK", 3, "　　認めます。（合格発表が金曜日の15:30以降または、土曜日の場合は月曜日");
                form.VrsOutn("TEXT_PROC_LIMIT_REMARK", 4, "　　の15:30まで）");
                form.VrsOutn("TEXT_PROC_LIMIT_REMARK", 5, "※　併願校の2次募集は延期の期間に含みません。");
                for (int i = 0; i < 5; i++) {
                    form.VrAttributen("TEXT_PROC_LIMIT_REMARK", i + 1, "ZenFont=0"); // 明朝体
                }
            }

            // 入学手続の完了
            form.VrsOutn("TEXT_PROC_COMPL", 1, "本校では「入学金振込受付証明書」をいただき、合格通知書の下欄に入学許可印を");
            form.VrsOutn("TEXT_PROC_COMPL", 2, "押印し、入学を許可いたします。以上のことがすべて終ると入学手続きが完了したこと");
            form.VrsOutn("TEXT_PROC_COMPL", 3, "になります。");
            if (isTokutai || isTokyoto) {
                form.VrsOutn("TEXT_PROC_COMPL", 4, "ご来校いただかない場合、入学手続きが完了されたことになりませんのでご注意ください。");
            } else {
                form.VrsOutn("TEXT_PROC_COMPL", 4, "金融機関へ振り込み後、ご来校いただかない場合、入学手続きが完了されたことになり");
                form.VrsOutn("TEXT_PROC_COMPL", 5, "ませんのでご注意ください。");
                form.VrsOutn("TEXT_PROC_COMPL", 6, "※　一度受け付けた入学金は、お返しできませんので予めご了承ください。");
            }
            for (int i = 0; i < 6; i++) {
                form.VrAttributen("TEXT_PROC_COMPL", i + 1, "ZenFont=0"); // 明朝体
            }

            int procline = 1;
            final String fontSize = "Size=9.5";
            // 入学手続後のご来校日について
            if (Arrays.asList(TESTDIV4_KIKOKUSEI_A, TESTDIV5_KIKOKUSEI_B).contains(_param._testdiv)) {
                form.VrsOutn("TEXT_AFTER_PROC", procline, "・学力診断テスト（A方式手続者のみ対象）… " + StringUtils.defaultString(testNichiji)); // 学力診断テスト日時
                form.VrAttributen("TEXT_AFTER_PROC", procline, fontSize);
            } else {
                form.VrsOutn("TEXT_AFTER_PROC", procline, "・学力診断テスト（A推薦・推薦手続者のみ対象）… " + StringUtils.defaultString(testNichiji)); // 学力診断テスト日時
                form.VrAttributen("TEXT_AFTER_PROC", procline, fontSize);
            }
            procline += 2;
            form.VrsOutn("TEXT_AFTER_PROC", procline, "・入学準備説明会（すべての新入生および保護者対象）… " + StringUtils.defaultString(setsumeikaiNichiji)); // 入学準備説明会日時
            form.VrAttributen("TEXT_AFTER_PROC", procline, fontSize);
            procline += 2;
            form.VrsOutn("TEXT_AFTER_PROC", procline, "・学用品販売（すべての新入生および保護者対象）… " + StringUtils.defaultString(hanbaiNichiji)); // 学用品販売日時
            form.VrAttributen("TEXT_AFTER_PROC", procline, fontSize);
            procline += 2;
            form.VrsOutn("TEXT_AFTER_PROC", procline, "・実力テスト(スタディープログラム)，「すらら」受講ガイダンス　（すべての新入生対象）… " + StringUtils.defaultString(studySupportNichiji)); //  スタディーサポート日時
            form.VrAttributen("TEXT_AFTER_PROC", procline, fontSize);

            procline += 2;
            if (TESTDIV5_KIKOKUSEI_B.equals(_param._testdiv)) {
//                String study = "　※スタディープログラム，「すらら」受講ガイダンスの時間割に沿って、入学予定コース別に販売時間を指定いたします。";
//                if (null != study) {
//                    form.VrsOutn("TEXT_AFTER_PROC", procline, study);
//                    form.VrAttributen("TEXT_AFTER_PROC", procline, "ZenFont=0,Size=9"); // 明朝体
//                    procline += 2;
//                }
//                String saisun = "・制服・体操着等　出張採寸日 … " + StringUtils.defaultString(remark1);
//                if (null != saisun) {
//                    form.VrsOutn("TEXT_AFTER_PROC", procline, saisun); // 採寸日時
//                    form.VrAttributen("TEXT_AFTER_PROC", procline, fontSize);
//                    procline += 2;
//                }
            } else {
                String study = null;
                if (!Arrays.asList(TESTDIV1_A_SUISEN, TESTDIV2_B_SUISEN, TESTDIV3_IPPAN).contains(_param._testdiv)) {
                    study = "　※スタディープログラム，「すらら」受講ガイダンスの時間割に沿って、入学予定コース別に販売時間を設定いたします。";
                }
                if (null != study) {
                    form.VrsOutn("TEXT_AFTER_PROC", procline, study);
                    form.VrAttributen("TEXT_AFTER_PROC", procline, "ZenFont=0,Size=9"); // 明朝体
                    procline += 2;
                }
                String saisun = null;
                if (!Arrays.asList(TESTDIV1_A_SUISEN, TESTDIV2_B_SUISEN, TESTDIV3_IPPAN).contains(_param._testdiv)) {
                    saisun = "・制服・体操着等　採寸日 … " + StringUtils.defaultString(remark1);
                }
                if (null != saisun) {
                    form.VrsOutn("TEXT_AFTER_PROC", procline, saisun); // 採寸日時
                    form.VrAttributen("TEXT_AFTER_PROC", procline, fontSize);
                    procline += 2;
                }
            }

            final String text7 = "※詳細は、入学手続後にお渡しする書類にてご案内いたします。";
            form.VrsOutn("TEXT_AFTER_PROC", procline, text7);
            form.VrAttributen("TEXT_AFTER_PROC", procline, "UnderLine=(0,2,1), Keta=" + KNJ_EditEdit.getMS932ByteLength(text7));

        }

        final String title;
        if (TESTDIV6_IKKANSEI.equals(_param._testdiv)) { // 一貫生
            title = toZenkaku(_param._outputNendo) + "年度　高等学校進級に関する手続きについて";
        } else {
            title = toZenkaku(_param._outputNendo) + "年度　入学手続要項";
        }

        form.VrsOut("TITLE", title); // タイトル

        form.VrsOut("EXAM_NO", receptno); // 受験番号
        form.VrsOut("EXAM_NO2", receptno); // 受験番号
        form.VrsOut("EXAM_NO3", receptno); // 受験番号
        form.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名

        form.VrsOut("NAME", getString(m, "NAME"));
        form.VrsOut("NAME2", getString(m, "NAME"));
        form.VrsOut("NAME3", getString(m, "NAME"));
        form.VrsOut("KANA3", getString(m, "NAME_KANA_HK"));
//        form.VrsOut("GNAME", getString(m, "GNAME"));
//        form.VrsOut("GNAME2", getString(m, "GNAME"));
//        final String addr = StringUtils.defaultString(getString(m, "ADDRESS1")) + StringUtils.defaultString(getString(m, "ADDRESS2"));
//        if (getMS932Bytecount(addr) > 50) {
//            final String[] split = KNJ_EditEdit.get_token(addr, 50, 2);
//            if (null != split) {
//                for (int i = 0; i < split.length; i++) {
//                    form.VrsOut("ADDR_" + String.valueOf(i + 1), split[i]);
//                }
//            }
//        } else {
//            form.VrsOut("ADDR", addr);
//        }
//        if (getMS932Bytecount(addr) > 44) {
//            final String[] split = KNJ_EditEdit.get_token(addr, 30, 3);
//            if (null != split) {
//                for (int i = 0; i < split.length; i++) {
//                    form.VrsOut("ADDR3_" + String.valueOf(i + 1), split[i]);
//                }
//            }
//        } else if (getMS932Bytecount(addr) > 22) {
//            final String[] split = KNJ_EditEdit.get_token(addr, 22, 2);
//            if (null != split) {
//                for (int i = 0; i < split.length; i++) {
//                    form.VrsOut("ADDR2_" + String.valueOf(i + 1), split[i]);
//                }
//            }
//        } else {
//            form.VrsOut("ADDR2", addr);
//        }

        form._svf.VrEndPage();
        _hasData = true;
    }

    // 入学承諾書
    private void printNyugakuShoudakusho(final DB2UDB db2, final Vrw32alp svf, final Map m) {
        final String form = "KNJL326F_J7.frm";
        setForm(svf, form);

        svf.VrsOut("EXAM_NO", getString(m, "RECEPTNO")); // 受験番号
        svf.VrsOut("NAME" + (getMS932Bytecount(getString(m, "NAME")) > 30 ? "2" : "1"), getString(m, "NAME")); // 氏名
        svf.VrsOut("BIRTHDAY", _param.gethiduke(db2, getString(m, "BIRTHDAY"))); // 生年月日
        svf.VrsOut("NENDO", toZenkaku(_param._outputNendo) + "年度"); // 年度
        svf.VrsOut("DATE", _param.gethiduke(db2, _param._printDate)); // 日付
        svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
        printSchoolStampImage(svf);
        svf.VrsOut("STAFF_NAME", _param._principalName); // 職員名
        svf.VrsOut("JOB_NAME", _param._jobName); // 役職名

        svf.VrEndPage();
        _hasData = true;
    }

    // 入学承諾書用封筒
    private void printNyugakuShodakushoYoFuto(final Vrw32alp svf, final Map m) {
        final String form = "KNJL326F_J8.frm";
        setForm(svf, form);

        svf.VrsOut("EDBOAD_NAME", "教育委員会 御中"); // 教育委員会名
        svf.VrsOut("NAME", getString(m, "NAME")); // 氏名
        svf.VrsOut("EXAM_NO", getString(m, "RECEPTNO")); // 受験番号

        svf.VrEndPage();
        _hasData = true;
    }

    private static String toZenkaku(final String s) {
        if (null == s) {
            return s;
        }
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            switch (c) {
            case ',': stb.append('，'); break;
            case '0': stb.append('０'); break;
            case '1': stb.append('１'); break;
            case '2': stb.append('２'); break;
            case '3': stb.append('３'); break;
            case '4': stb.append('４'); break;
            case '5': stb.append('５'); break;
            case '6': stb.append('６'); break;
            case '7': stb.append('７'); break;
            case '8': stb.append('８'); break;
            case '9': stb.append('９'); break;
            default: stb.append(c);
            }
        }
        return stb.toString();
    }

    private static int getMS932Bytecount(String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private String sql(final Param param) {
        final StringBuffer stb = new StringBuffer();

        if ((OUTPUT_DIV2_JUKENSHA.equals(param._outputDiv) || OUTPUT_DIV3_SHITEI.equals(param._outputDiv) && (OUTPUT1_GOKAKU.equals(param._output) || OUTPUT2_FUGOKAKU.equals(param._output))) && APPLICANTDIV1.equals(param._applicantdiv)) {
            stb.append(" SELECT ");
            stb.append("     APBASE.NAME ");
            stb.append("     ,APD012.REMARK" + param._testdiv + " AS RECEPTNO ");
            stb.append("     ,APBASE.EXAMNO ");
            stb.append("     ,CAST(NULL AS VARCHAR(1)) AS JUDGEDIV ");
            stb.append("     ,CAST(NULL AS VARCHAR(1)) AS HONORDIV ");
            stb.append("     ,CAST(NULL AS VARCHAR(1)) AS HONORDIV_NAME ");
            stb.append("     ,VALUE(L1.NAME1, '') AS TESTDIVNAME ");
            stb.append("     ,APBASE.NAME_KANA ");
            stb.append("     ,translate_h_hk(APBASE.NAME_KANA) AS NAME_KANA_HK ");
            stb.append("     ,L3.ABBV1 AS SEX_NAME ");
            stb.append("     ,APBASE.BIRTHDAY ");
            stb.append("     ,APBASE.GENERAL_FLG ");
            stb.append("     ,APADDR.GNAME ");
            stb.append("     ,APADDR.ZIPCD ");
            stb.append("     ,APADDR.ADDRESS1 ");
            stb.append("     ,APADDR.ADDRESS2 ");
            stb.append("     ,L4.FINSCHOOL_NAME ");
            stb.append("     ,APBASE.SUC_COURSECD || APBASE.SUC_MAJORCD || APBASE.SUC_COURSECODE AS SUC_EXAMCOURSE ");
            stb.append("     ,ECMS.EXAMCOURSE_NAME AS SUC_EXAMCOURSE_NAME ");
            stb.append("     ,APD001.REMARK8 || APD001.REMARK9 || APD001.REMARK10 AS DET001_EXAMCOURSE ");
            stb.append("     ,APD002.REMARK1 AS RECRUIT_NO ");
            stb.append("     ,TRECV.JUDGE_KIND AS RECR_JUDGE_KIND ");
            if (APPLICANTDIV1.equals(_param._applicantdiv)) {
                stb.append("     ,NML025_TRECV_JK.NAME1 AS RECR_JUDGE_KIND_NAME ");
            } else {
                stb.append("     ,NML025_TRECV_JK.NAME2 AS RECR_JUDGE_KIND_NAME ");
            }
            stb.append("     ,VALUE(APD014.REMARK1, APD014.REMARK2) AS SISTER_FLG ");
            stb.append("     ,VALUE(APD015.REMARK5, APD015.REMARK1) AS MOTHER_FLG ");
            stb.append("     ,ECMD.EXAMCOURSE_NAME AS DET001_EXAMCOURSE_NAME ");
            //中学の時のみ通る箇所のため、そのまま変更。
            stb.append("     ,REC1.RECEPTNO AS BEFORE_RECEPTNO1 ");
            stb.append("     ,REC16.RECEPTNO AS BEFORE_RECEPTNO2 ");
            stb.append("     ,REC2.RECEPTNO AS BEFORE_RECEPTNO3 ");
            stb.append("     ,REC3.RECEPTNO AS BEFORE_RECEPTNO4 ");
            stb.append("     ,REC1.HONORDIV AS BEFORE_HONORDIV1 ");
            stb.append("     ,REC16.HONORDIV AS BEFORE_HONORDIV2 ");
            stb.append("     ,REC2.HONORDIV AS BEFORE_HONORDIV3 ");
            stb.append("     ,REC3.HONORDIV AS BEFORE_HONORDIV4 ");
            stb.append("     ,EMON.ENTRANCE_FLG ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT APBASE ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT APD012 ON APD012.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("                                                           AND APD012.EXAMNO = APBASE.EXAMNO ");
            stb.append("                                                           AND APD012.SEQ = '012' ");
            stb.append("                                                           AND APD012.REMARK" + param._testdiv + " IS NOT NULL ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD002 ON APD002.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("         AND APD002.EXAMNO = APBASE.EXAMNO ");
            stb.append("         AND APD002.SEQ = '002' ");
            stb.append("     LEFT JOIN RECRUIT_VISIT_DAT TRECV ON TRECV.YEAR = APBASE.ENTEXAMYEAR ");
            stb.append("         AND TRECV.RECRUIT_NO = APD002.REMARK1 ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT APADDR ON APADDR.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("                                    AND APADDR.EXAMNO = APBASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD001 ON APD001.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("         AND APD001.EXAMNO = APBASE.EXAMNO");
            stb.append("         AND APD001.SEQ = '001' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD014 ON APD014.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("         AND APD014.EXAMNO = APBASE.EXAMNO ");
            stb.append("         AND APD014.SEQ = '014' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD015 ON APD015.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("         AND APD015.EXAMNO = APBASE.EXAMNO ");
            stb.append("         AND APD015.SEQ = '015' ");
            stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L004' AND L1.NAMECD2 = '" + param._testdiv + "' ");
            stb.append("     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'Z002' AND L3.NAMECD2 = APBASE.SEX ");
            stb.append("     LEFT JOIN FINSCHOOL_MST L4 ON L4.FINSCHOOLCD = APBASE.FS_CD ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECMS ON ECMS.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("         AND ECMS.APPLICANTDIV = APBASE.APPLICANTDIV ");
            stb.append("         AND ECMS.TESTDIV = '1' ");
            stb.append("         AND ECMS.COURSECD = APBASE.SUC_COURSECD ");
            stb.append("         AND ECMS.MAJORCD = APBASE.SUC_MAJORCD ");
            stb.append("         AND ECMS.EXAMCOURSECD = APBASE.SUC_COURSECODE ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECMD ON ECMD.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("         AND ECMD.APPLICANTDIV = APBASE.APPLICANTDIV ");
            stb.append("         AND ECMD.TESTDIV = '1' ");
            stb.append("         AND ECMD.COURSECD = APD001.REMARK8 ");
            stb.append("         AND ECMD.MAJORCD = APD001.REMARK9 ");
            stb.append("         AND ECMD.EXAMCOURSECD = APD001.REMARK10 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT REC1 ON REC1.ENTEXAMYEAR = APBASE.ENTEXAMYEAR AND REC1.EXAMNO = APBASE.EXAMNO AND REC1.RECEPTNO = APD012.REMARK1 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT REC2 ON REC2.ENTEXAMYEAR = APBASE.ENTEXAMYEAR AND REC2.EXAMNO = APBASE.EXAMNO AND REC2.RECEPTNO = APD012.REMARK2 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT REC3 ON REC3.ENTEXAMYEAR = APBASE.ENTEXAMYEAR AND REC3.EXAMNO = APBASE.EXAMNO AND REC3.RECEPTNO = APD012.REMARK3 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT REC16 ON REC16.ENTEXAMYEAR = APBASE.ENTEXAMYEAR AND REC16.EXAMNO = APBASE.EXAMNO AND REC16.RECEPTNO = APD012.REMARK16 ");
            stb.append("     LEFT JOIN ENTEXAM_MONEY_DAT EMON ON EMON.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("         AND EMON.APPLICANTDIV = APBASE.APPLICANTDIV ");
            stb.append("         AND EMON.EXAMNO = APBASE.EXAMNO ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT REC_HON ON REC_HON.ENTEXAMYEAR = APBASE.ENTEXAMYEAR AND REC_HON.EXAMNO = APBASE.EXAMNO AND REC_HON.RECEPTNO = APD012.REMARK" + param._testdiv + " ");
            stb.append("     LEFT JOIN NAME_MST NML025_TRECV_JK ON NML025_TRECV_JK.NAMECD1 = 'L025' AND NML025_TRECV_JK.NAMECD2 = TRECV.JUDGE_KIND ");
            stb.append(" WHERE ");
            stb.append("         APBASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND APBASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            if (!OUTPUT_DIV3_SHITEI.equals(param._outputDiv)) {
                stb.append("     AND VALUE(REC_HON.JUDGEDIV, '') <> '4' ");
            }
            if (OUTPUT_DIV3_SHITEI.equals(param._outputDiv) && (OUTPUT1_GOKAKU.equals(param._output) || OUTPUT2_FUGOKAKU.equals(param._output))) {
                stb.append("     AND APD012.REMARK" + param._testdiv + " = '" + param._outputExamno + "' ");
            }
            if (OUTPUT7_SHODAKU.equals(_param._output) || OUTPUT8_SHODAKU_FUTO.equals(_param._output)) {
                stb.append("     AND REC_HON.RECEPTNO IS NOT NULL ");
            }
            stb.append(" ORDER BY int(APD012.REMARK" + param._testdiv + ") ");
        } else {

            //免除額を取得（免除額設定）
            stb.append(" WITH T_EXEMPTION_TOTAL AS ( ");
            stb.append("     SELECT ");
            stb.append("         P2.EXEMPTION_CD, ");
            stb.append("         SUM(P1.ITEM_MONEY) AS EXEMPTION_MONEY ");
            stb.append("     FROM ");
            stb.append("         ENTEXAM_PAYMENT_ITEM_MST P1 ");
            stb.append("         INNER JOIN ENTEXAM_PAYMENT_EXEMPTION_MST P2 ");
            stb.append("              ON P2.ENTEXAMYEAR = P1.ENTEXAMYEAR ");
            stb.append("             AND P2.APPLICANTDIV = P1.APPLICANTDIV ");
            stb.append("             AND P2.DIV = P1.DIV ");
            stb.append("             AND P2.ITEM_CD = P1.ITEM_CD ");
            stb.append("             AND P2.KIND_CD = '1' "); //入学金
            stb.append("     WHERE ");
            stb.append("         P1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("         AND P1.APPLICANTDIV = '" + param._applicantdiv +"' ");
            stb.append("         AND P1.DIV = '0' ");
            stb.append("     GROUP BY ");
            stb.append("         P2.EXEMPTION_CD ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     APBASE.NAME ");
            stb.append("     ,T1.RECEPTNO ");
            stb.append("     ,T1.EXAMNO ");
            stb.append("     ,T1.JUDGEDIV ");
            stb.append("     ,T1.HONORDIV ");
            if (APPLICANTDIV1.equals(_param._applicantdiv)) {
                stb.append("     ,NML025.NAME1 AS HONORDIV_NAME ");
            } else {
                stb.append("     ,NML025.NAME2 AS HONORDIV_NAME ");
            }
            stb.append("     ,VALUE(L1.NAME1, '') AS TESTDIVNAME ");
            stb.append("     ,APBASE.NAME_KANA ");
            stb.append("     ,translate_h_hk(APBASE.NAME_KANA) AS NAME_KANA_HK ");
            stb.append("     ,L3.ABBV1 AS SEX_NAME ");
            stb.append("     ,APBASE.BIRTHDAY ");
            stb.append("     ,APBASE.GENERAL_FLG ");
            stb.append("     ,APADDR.GNAME ");
            stb.append("     ,APADDR.ZIPCD ");
            stb.append("     ,APADDR.ADDRESS1 ");
            stb.append("     ,APADDR.ADDRESS2 ");
            stb.append("     ,L4.FINSCHOOL_NAME ");
            stb.append("     ,APBASE.SUC_COURSECD || APBASE.SUC_MAJORCD || APBASE.SUC_COURSECODE AS SUC_EXAMCOURSE ");
            stb.append("     ,ECMS.EXAMCOURSE_NAME AS SUC_EXAMCOURSE_NAME ");
            stb.append("     ,APD001.REMARK8 || APD001.REMARK9 || APD001.REMARK10 AS DET001_EXAMCOURSE ");
            stb.append("     ,APD002.REMARK1 AS RECRUIT_NO ");
            stb.append("     ,TRECV.JUDGE_KIND AS RECR_JUDGE_KIND ");
            if (APPLICANTDIV1.equals(_param._applicantdiv)) {
                stb.append("     ,NML025_TRECV_JK.NAME1 AS RECR_JUDGE_KIND_NAME ");
            } else {
                stb.append("     ,NML025_TRECV_JK.NAME2 AS RECR_JUDGE_KIND_NAME ");
            }
            stb.append("     ,VALUE(APD014.REMARK1, APD014.REMARK2) AS SISTER_FLG ");
            stb.append("     ,VALUE(APD015.REMARK5, APD015.REMARK1) AS MOTHER_FLG ");
            stb.append("     ,ECMD.EXAMCOURSE_NAME AS DET001_EXAMCOURSE_NAME ");
            if (APPLICANTDIV1.equals(param._applicantdiv)) {
                //中学の時だけ追加された試験を加味する。高校は既存
                stb.append("     ,REC1.RECEPTNO AS BEFORE_RECEPTNO1 ");
                stb.append("     ,REC16.RECEPTNO AS BEFORE_RECEPTNO2 ");
                stb.append("     ,REC2.RECEPTNO AS BEFORE_RECEPTNO3 ");
                stb.append("     ,REC3.RECEPTNO AS BEFORE_RECEPTNO4 ");
                stb.append("     ,REC1.HONORDIV AS BEFORE_HONORDIV1 ");
                stb.append("     ,REC16.HONORDIV AS BEFORE_HONORDIV2 ");
                stb.append("     ,REC2.HONORDIV AS BEFORE_HONORDIV3 ");
                stb.append("     ,REC3.HONORDIV AS BEFORE_HONORDIV4 ");
            } else {
                stb.append("     ,REC1.RECEPTNO AS BEFORE_RECEPTNO1 ");
                stb.append("     ,REC2.RECEPTNO AS BEFORE_RECEPTNO2 ");
                stb.append("     ,REC3.RECEPTNO AS BEFORE_RECEPTNO3 ");
                stb.append("     ,REC4.RECEPTNO AS BEFORE_RECEPTNO4 ");
                stb.append("     ,REC1.HONORDIV AS BEFORE_HONORDIV1 ");
                stb.append("     ,REC2.HONORDIV AS BEFORE_HONORDIV2 ");
                stb.append("     ,REC3.HONORDIV AS BEFORE_HONORDIV3 ");
                stb.append("     ,REC4.HONORDIV AS BEFORE_HONORDIV4 ");
            }
            stb.append("     ,EMON.ENTRANCE_FLG ");
            stb.append("     ,PITEM01.ITEM_MONEY AS ITEM_MONEY01 ");
            stb.append("     ,PITEM02.ITEM_MONEY AS ITEM_MONEY02 ");
            stb.append("     ,PITEM03.ITEM_MONEY AS ITEM_MONEY03 ");
            stb.append("     ,PITEM04.ITEM_MONEY AS ITEM_MONEY04 ");
            stb.append("     ,VX.EXEMPTION_CD ");
            stb.append("     ,CASE WHEN PX1.EXEMPTION_MONEY IS NULL AND PX1_2.EXEMPTION_MONEY IS NULL THEN CAST(NULL AS INTEGER) ELSE VALUE(PX1.EXEMPTION_MONEY, 0) + VALUE(PX1_2.EXEMPTION_MONEY, 0) END AS EXEMPTION_MONEY01 ");
            stb.append("     ,PX2.EXEMPTION_MONEY AS EXEMPTION_MONEY02 ");
            stb.append("     ,PX3.EXEMPTION_MONEY AS EXEMPTION_MONEY03 ");
            stb.append("     ,PX4.EXEMPTION_MONEY AS EXEMPTION_MONEY04 ");
            stb.append("     ,L2.EXEMPTION_MONEY AS TOTAL_MENJOGAKU ");

            stb.append(" FROM ");
            stb.append("     V_ENTEXAM_RECEPT_DAT T1 ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT APBASE ON APBASE.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("                                    AND APBASE.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("                                    AND APBASE.EXAMNO       = T1.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT APADDR ON APADDR.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("                                    AND APADDR.EXAMNO = APBASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT APD012 ON APD012.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("                                                          AND APD012.EXAMNO = APBASE.EXAMNO ");
            stb.append("                                                          AND APD012.SEQ = '012' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD002 ON APD002.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND APD002.EXAMNO = APBASE.EXAMNO ");
            stb.append("         AND APD002.SEQ = '002' ");
            stb.append("     LEFT JOIN RECRUIT_VISIT_DAT TRECV ON TRECV.YEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND TRECV.RECRUIT_NO = APD002.REMARK1 ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD014 ON APD014.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("         AND APD014.EXAMNO = APBASE.EXAMNO ");
            stb.append("         AND APD014.SEQ = '014' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD015 ON APD015.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("         AND APD015.EXAMNO = APBASE.EXAMNO ");
            stb.append("         AND APD015.SEQ = '015' ");
            stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L004' ");
            stb.append("                              AND L1.NAMECD2 = T1.TESTDIV ");
            stb.append("     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'Z002' AND L3.NAMECD2 = APBASE.SEX ");
            stb.append("     LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' AND NML025.NAMECD2 = T1.HONORDIV ");
            stb.append("     LEFT JOIN FINSCHOOL_MST L4 ON L4.FINSCHOOLCD = APBASE.FS_CD ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD001 ON APD001.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("         AND APD001.EXAMNO = APBASE.EXAMNO");
            stb.append("         AND APD001.SEQ = '001' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECMS ON ECMS.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("         AND ECMS.APPLICANTDIV = APBASE.APPLICANTDIV ");
            stb.append("         AND ECMS.TESTDIV = '1' ");
            stb.append("         AND ECMS.COURSECD = APBASE.SUC_COURSECD ");
            stb.append("         AND ECMS.MAJORCD = APBASE.SUC_MAJORCD ");
            stb.append("         AND ECMS.EXAMCOURSECD = APBASE.SUC_COURSECODE ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECMD ON ECMD.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("         AND ECMD.APPLICANTDIV = APBASE.APPLICANTDIV ");
            stb.append("         AND ECMD.TESTDIV = '1' ");
            stb.append("         AND ECMD.COURSECD = APD001.REMARK8 ");
            stb.append("         AND ECMD.MAJORCD = APD001.REMARK9 ");
            stb.append("         AND ECMD.EXAMCOURSECD = APD001.REMARK10 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT REC1 ON REC1.ENTEXAMYEAR = APBASE.ENTEXAMYEAR AND REC1.EXAMNO = APBASE.EXAMNO AND REC1.RECEPTNO = APD012.REMARK1 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT REC2 ON REC2.ENTEXAMYEAR = APBASE.ENTEXAMYEAR AND REC2.EXAMNO = APBASE.EXAMNO AND REC2.RECEPTNO = APD012.REMARK2 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT REC3 ON REC3.ENTEXAMYEAR = APBASE.ENTEXAMYEAR AND REC3.EXAMNO = APBASE.EXAMNO AND REC3.RECEPTNO = APD012.REMARK3 ");
            if (APPLICANTDIV1.equals(param._applicantdiv)) {
                //中学の時だけ追加された試験を加味する。高校は既存
                stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT REC16 ON REC16.ENTEXAMYEAR = APBASE.ENTEXAMYEAR AND REC16.EXAMNO = APBASE.EXAMNO AND REC16.RECEPTNO = APD012.REMARK16 ");
            } else {
                stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT REC4 ON REC4.ENTEXAMYEAR = APBASE.ENTEXAMYEAR AND REC4.EXAMNO = APBASE.EXAMNO AND REC4.RECEPTNO = APD012.REMARK4 ");
            }
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RPD001 ON RPD001.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND RPD001.APPLICANTDIV = T1.APPLICANTDIV");
            stb.append("         AND RPD001.TESTDIV = T1.TESTDIV");
            stb.append("         AND RPD001.EXAM_TYPE = T1.EXAM_TYPE");
            stb.append("         AND RPD001.RECEPTNO = T1.RECEPTNO");
            stb.append("         AND RPD001.SEQ = '003' ");
            stb.append("     LEFT JOIN ENTEXAM_MONEY_DAT EMON ON EMON.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
            stb.append("         AND EMON.APPLICANTDIV = APBASE.APPLICANTDIV ");
            stb.append("         AND EMON.EXAMNO = APBASE.EXAMNO ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_EXEMPTION_DAT VX ON VX.ENTEXAMYEAR = T1.ENTEXAMYEAR AND VX.APPLICANTDIV = T1.APPLICANTDIV AND VX.TESTDIV = T1.TESTDIV AND VX.RECEPTNO = T1.RECEPTNO AND VX.EXAMNO = T1.EXAMNO ");

            stb.append("     LEFT JOIN ENTEXAM_PAYMENT_ITEM_MST PITEM01 ON PITEM01.ENTEXAMYEAR = T1.ENTEXAMYEAR AND PITEM01.APPLICANTDIV = T1.APPLICANTDIV AND PITEM01.DIV = '0' AND PITEM01.ITEM_CD = '01' ");
            stb.append("     LEFT JOIN ENTEXAM_PAYMENT_ITEM_MST PITEM02 ON PITEM02.ENTEXAMYEAR = T1.ENTEXAMYEAR AND PITEM02.APPLICANTDIV = T1.APPLICANTDIV AND PITEM02.DIV = '0' AND PITEM02.ITEM_CD = '02' ");
            stb.append("     LEFT JOIN ENTEXAM_PAYMENT_ITEM_MST PITEM03 ON PITEM03.ENTEXAMYEAR = T1.ENTEXAMYEAR AND PITEM03.APPLICANTDIV = T1.APPLICANTDIV AND PITEM03.DIV = '0' AND PITEM03.ITEM_CD = '03' ");
            stb.append("     LEFT JOIN ENTEXAM_PAYMENT_ITEM_MST PITEM04 ON PITEM04.ENTEXAMYEAR = T1.ENTEXAMYEAR AND PITEM04.APPLICANTDIV = T1.APPLICANTDIV AND PITEM04.DIV = '0' AND PITEM04.ITEM_CD = '04' ");
            stb.append("     LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PX1 ON PX1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND PX1.APPLICANTDIV = T1.APPLICANTDIV AND PX1.DIV = '0' AND PX1.ITEM_CD = '01' AND PX1.KIND_CD = '1' AND PX1.EXEMPTION_CD = VX.EXEMPTION_CD ");
            stb.append("     LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PX2 ON PX2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND PX2.APPLICANTDIV = T1.APPLICANTDIV AND PX2.DIV = '0' AND PX2.ITEM_CD = '02' AND PX2.KIND_CD = '1' AND PX2.EXEMPTION_CD = VX.EXEMPTION_CD ");
            stb.append("     LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PX3 ON PX3.ENTEXAMYEAR = T1.ENTEXAMYEAR AND PX3.APPLICANTDIV = T1.APPLICANTDIV AND PX3.DIV = '0' AND PX3.ITEM_CD = '03' AND PX3.KIND_CD = '1' AND PX3.EXEMPTION_CD = VX.EXEMPTION_CD ");
            stb.append("     LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PX4 ON PX4.ENTEXAMYEAR = T1.ENTEXAMYEAR AND PX4.APPLICANTDIV = T1.APPLICANTDIV AND PX4.DIV = '0' AND PX4.ITEM_CD = '04' AND PX4.KIND_CD = '1' AND PX4.EXEMPTION_CD = VX.EXEMPTION_CD ");
            stb.append("     LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PX1_2 ON PX1_2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND PX1_2.APPLICANTDIV = T1.APPLICANTDIV AND PX1_2.DIV = '0' AND PX1_2.ITEM_CD = '01' AND PX1_2.KIND_CD = '1' AND PX1_2.EXEMPTION_CD = VX.EXEMPTION_CD2 ");
            stb.append("     LEFT JOIN T_EXEMPTION_TOTAL L2 ON L2.EXEMPTION_CD = VX.EXEMPTION_CD ");
            stb.append("     LEFT JOIN NAME_MST NML025_TRECV_JK ON NML025_TRECV_JK.NAMECD1 = 'L025' AND NML025_TRECV_JK.NAMECD2 = TRECV.JUDGE_KIND ");
            stb.append(" WHERE ");
            stb.append("         T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
            if (PRGID_KNJL327F.equals(_param._prgid)) {
                stb.append("     AND RPD001.REMARK1 = '" + param._testdiv0 + "' ");
            }
            stb.append("     AND T1.EXAM_TYPE = '1' ");
            if (!OUTPUT_DIV3_SHITEI.equals(param._outputDiv)) {
                stb.append("     AND VALUE(T1.JUDGEDIV, '') <> '4' ");
            }

            if (OUTPUT_DIV4_TOKUTAI_KOUHO.equals(param._outputDiv)) {
                // OUTPUT4_TOKUTAI_GOKAKU_TUUCHI.equals(param._output) || OUTPUT5_TOKUTAI_TUUCHI.equals(param._output)
                stb.append("     AND TRECV.JUDGE_KIND IS NOT NULL ");
            } else if (OUTPUT_DIV3_SHITEI.equals(param._outputDiv)) {
                // 受験番号指定
                stb.append("     AND T1.RECEPTNO = '" + param._outputExamno + "' ");
            } else if (OUTPUT_DIV2_JUKENSHA.equals(param._outputDiv)) {
                // 志願者全員 条件無し
            } else if ("1".equals(param._outputDiv)) {
                if (OUTPUT1_GOKAKU.equals(param._output) || OUTPUT6_FURIKOMI.equals(param._output) || OUTPUT7_SHODAKU.equals(param._output) || OUTPUT8_SHODAKU_FUTO.equals(param._output)) {
                    stb.append("     AND T1.JUDGEDIV = '1' ");
                } else if (OUTPUT2_FUGOKAKU.equals(param._output)) {
                    stb.append("     AND T1.JUDGEDIV = '2' ");
                } else if (OUTPUT3_HOKETSU_GOKAKU.equals(param._output)) {
                    stb.append("     AND T1.JUDGEDIV = '3' ");
                } else if (OUTPUT4_TOKUTAI_GOKAKU_TUUCHI.equals(param._output) || OUTPUT5_TOKUTAI_TUUCHI.equals(param._output)) {
                    stb.append("     AND T1.JUDGEDIV = '1' ");
                    stb.append("     AND T1.HONORDIV IS NOT NULL ");
                }
            }
            if (OUTPUT1_GOKAKU.equals(param._output) || OUTPUT2_FUGOKAKU.equals(param._output) || OUTPUT3_HOKETSU_GOKAKU.equals(param._output) || OUTPUT6_FURIKOMI.equals(param._output)) {
                if (APPLICANTDIV1.equals(param._applicantdiv) && "5".equals(param._testdiv)) {
                    stb.append("     AND VALUE(APBASE.GENERAL_FLG, '') <> '1' ");
                } else if (APPLICANTDIV2.equals(param._applicantdiv) && "3".equals(param._testdiv)) {
                    stb.append("     AND VALUE(APBASE.GENERAL_FLG, '') <> '1' ");
                }
            }
            stb.append(" ORDER BY T1.RECEPTNO ");
        }
        return stb.toString();
    }

    private static class Form {
        final Param _param;
        final Vrw32alp _svf;
        String _currentFormname;
        Map _fieldInfoMap = new HashMap();

        private static String FLG_TATESEN = "FLG_TATESEN";
        private static String FLG_TOKUTAI = "FLG_TOKUTAI";
        private static String FLG_GENMEN = "FLG_GENMEN";
        private static String FLG_SHITAKUKIN = "FLG_SHITAKUKIN";

        public Form(final Param param, final Vrw32alp svf) {
            _param = param;
            _svf = svf;
        }

        public void setForm(final String formname) {
            _svf.VrSetForm(formname, 1);
            _currentFormname = formname;
            log.info(" set form " + formname);
            if (!_fieldInfoMap.containsKey(_currentFormname)) {
                _fieldInfoMap.put(_currentFormname, SvfField.getSvfFormFieldInfoMapGroupByName(_svf));
            }
        }

        public void setMergeForm(final Param param, final String formname, final String formname2, final String[] flg) {
            final String key = formname + " / " + formname2 + ArrayUtils.toString(flg);
            _param.logOnce(" set form " + key);
            if (!param._mergedFormMap.containsKey(key)) {
                File newFile = null;
                try {
                    final String formPath = _svf.getPath(formname);
                    final File formFile = new File(formPath);
                    final String formPath2 = _svf.getPath(formname2);
                    final File formFile2 = new File(formPath2);
                    if (_param._isOutputDebug) {
                        _param.logOnce(" form path = " + formPath + " / " + formPath2);
                    }
                    if (formFile.exists()) {
                        SvfForm svfForm = new SvfForm(formFile);
                        svfForm._debug = _param._isOutputDebugForm;
                        if (svfForm.readFile()) {

                            // 振込用紙設定
                            if (ArrayUtils.contains(flg, FLG_TATESEN)) {
                                final int y1 = 3128, y2 = 3266;
                                for (int i = 1; i <= 7; i++) {
                                    final int x = 3128 - (3128 - 2018) / 7 * i;
                                    svfForm.addLine(new SvfForm.Line(new SvfForm.Point(x, y1), new SvfForm.Point(x, y2)));
                                }

                                for (int i = 1; i <= 6; i++) {
                                    final int x2 = 4356 - (4356 - 3800) / 7 * i;
                                    svfForm.addLine(new SvfForm.Line(new SvfForm.Point(x2, y1), new SvfForm.Point(x2, y2)));
                                }
                            }

                            for (int i = 7; i <= 7; i++) {
                                final int y1 = 3128, y2 = 3266;
                                final int x2 = 4356 - (4356 - 3800) / 7 * i;
                                svfForm.addLine(new SvfForm.Line(new SvfForm.Point(x2, y1), new SvfForm.Point(x2, y2)));
                            }

                            for (int i = 1; i <= 6; i++) {
                                final int x = 6454 - (6454 - 5900) / 7 * i;
                                final int y1 = ArrayUtils.contains(flg, FLG_TOKUTAI) || ArrayUtils.contains(flg, FLG_SHITAKUKIN) ? 3266 : 3128;
                                final int y2 = 3642;
                                svfForm.addLine(new SvfForm.Line(new SvfForm.Point(x, y1), new SvfForm.Point(x, y2)));
                            }

                            final SvfForm.Font font = SvfForm.Font.Gothic;
                            if (APPLICANTDIV1.equals(_param._applicantdiv)) {
                                if (ArrayUtils.contains(flg, FLG_TOKUTAI)) {
                                    final String comment = "入学金免除";

                                    svfForm.addField(new SvfForm.Field(null, "MENJO1", font, 10, 2515, false, new SvfForm.Point(1960, 3140), 200, comment).setItalic(true));
                                    svfForm.addField(new SvfForm.Field(null, "MENJO2", font, 10, 3891, false, new SvfForm.Point(3871, 3154), 150, comment).setItalic(true));
                                    svfForm.addField(new SvfForm.Field(null, "MENJO3", font, 10, 5971, false, new SvfForm.Point(5971, 3154), 150, comment).setItalic(true));
                                }
                            } else {
                                if (ArrayUtils.contains(flg, FLG_TOKUTAI)) {
                                    final String comment = "入学金免除";
                                    final int keta = KNJ_EditEdit.getMS932ByteLength("入学金免除");
                                    svfForm.addField(new SvfForm.Field(null, "MONTXT1", font, keta, 2515, false, new SvfForm.Point(1960, 3140), 200, comment).setItalic(true));
                                    svfForm.addField(new SvfForm.Field(null, "MONTXT2", font, keta, 4247, false, new SvfForm.Point(3831, 3154), 150, comment).setItalic(true));
                                    svfForm.addField(new SvfForm.Field(null, "MONTXT3", font, keta, 6387, false, new SvfForm.Point(5971, 3154), 150, comment).setItalic(true));

                                    svfForm.addBox(new SvfForm.Box(SvfForm.LineKind.SOLID, SvfForm.LineWidth.THINEST, new SvfForm.Point(156, 2800), new SvfForm.Point(472, 2900)));
                                    svfForm.addKoteiMoji(new SvfForm.KoteiMoji("　特待", new SvfForm.Point(171, 2815), 130).setFont(font));

                                } else if (ArrayUtils.contains(flg, FLG_SHITAKUKIN)) {
                                    final String comment = "入学支度金利用";
                                    final int keta = KNJ_EditEdit.getMS932ByteLength("入学支度金利用");
                                    svfForm.addField(new SvfForm.Field(null, "MONTXT1", font, keta, 2645, false, new SvfForm.Point(1868, 3140), 200, comment).setItalic(true));
                                    svfForm.addField(new SvfForm.Field(null, "MONTXT2", font, keta, 4333, false, new SvfForm.Point(3808, 3154), 135, comment).setItalic(true));
                                    svfForm.addField(new SvfForm.Field(null, "MONTXT3", font, keta, 6433, false, new SvfForm.Point(5908, 3154), 135, comment).setItalic(true));

                                    svfForm.addBox(new SvfForm.Box(SvfForm.LineKind.SOLID, SvfForm.LineWidth.THINEST, new SvfForm.Point(168, 2803), new SvfForm.Point(565, 2903)));
                                    svfForm.addKoteiMoji(new SvfForm.KoteiMoji("入学支度金", new SvfForm.Point(183, 2815), 130).setFont(font));

                                } else if (ArrayUtils.contains(flg, FLG_GENMEN)) {

                                    svfForm.addBox(new SvfForm.Box(SvfForm.LineKind.SOLID, SvfForm.LineWidth.THINEST, new SvfForm.Point(168, 2800), new SvfForm.Point(404, 2898)));
                                    svfForm.addKoteiMoji(new SvfForm.KoteiMoji("減免", new SvfForm.Point(215, 2815), 130).setFont(font));
                                }
                            }

                            if (formFile2.exists()) {
                                SvfForm svfForm2 = new SvfForm(formFile2);
                                svfForm2._debug = _param._isOutputDebugForm;
                                if (svfForm2.readFile()) {

                                    svfForm.addAllElement(svfForm2.getAllElementList());

                                    newFile = svfForm.writeTempFile();
                                    log.info(" create file " + newFile.getPath());
                                }
                            }

                        } else {
                            log.error("read file error: " + formPath);
                        }
                    }
                } catch (Throwable e) {
                    log.error("throwed ", e);
                }
                param._mergedFormMap.put(key, newFile);
                if (null == newFile) {
                    throw new IllegalStateException(" form = " + formname + " / " + formname2);
                }
            }
            _currentFormname = param._mergedFormMap.get(key).getName();
            _param.logOnce(" merged = " + _currentFormname);
            _svf.VrSetForm(_currentFormname, 1);
            if (!_fieldInfoMap.containsKey(_currentFormname)) {
                _fieldInfoMap.put(_currentFormname, SvfField.getSvfFormFieldInfoMapGroupByName(_svf));
            }

        }

        public SvfField getField(final String fieldname) {
            if (!_fieldInfoMap.containsKey(_currentFormname)) {
                log.error(" not set form ! : " + _currentFormname);
                return null;
            }
            return (SvfField) getMappedMap(_fieldInfoMap, _currentFormname).get(fieldname);
        }


        public int VrsOut(final String fieldname, final String data) {
            SvfField field = getField(fieldname);
            if (null == field) {
                log.warn(" no such field : " + fieldname + " (data = '" + data + "')");
                return -1;
            }
            if (_param._isOutputDebugVrsOut) {
                log.info("VrsOut('" + fieldname + "', " + (null == data ? data : "'" + data + "'") + ");");
            }
            return _svf.VrsOut(fieldname, data);
        }

        public int VrsOutn(final String fieldname, final int gyo, final String data) {
            SvfField field = getField(fieldname);
            if (null == field) {
                log.warn(" no such field : " + fieldname + " (data = '" + data + "')");
                return -1;
            }
            if (_param._isOutputDebugVrsOut) {
                log.info("VrsOutn('" + fieldname + "', " + gyo + ", " + (null == data ? data : "'" + data + "'") + ");");
            }
            return _svf.VrsOutn(fieldname, gyo, data);
        }

        public int VrAttribute(final String fieldname, final String attribute) {
            SvfField field = getField(fieldname);
            if (null == field) {
                log.warn(" no such field : " + fieldname + " (attribute = '" + attribute + "')");
                return -1;
            }
            if (_param._isOutputDebugVrsOut) {
                log.info("VrAttribute('" + fieldname + "', " + (null == attribute ? attribute : "'" + attribute + "'") + ");");
            }
            return _svf.VrAttribute(fieldname, attribute);
        }

        public int VrAttributen(final String fieldname, final int gyo, final String attribute) {
            SvfField field = getField(fieldname);
            if (null == field) {
                log.warn(" no such field : " + fieldname + " (attribute = '" + attribute + "')");
                return -1;
            }
            if (_param._isOutputDebugVrsOut) {
                log.info("VrAttributen('" + fieldname + "', " + gyo + ", " + (null == attribute ? attribute : "'" + attribute + "'") + ");");
            }
            return _svf.VrAttributen(fieldname, gyo, attribute);
        }

        private String attributeIntPlus(final String fieldname, final String intProperty, final int plus) {
            SvfField field = getField(fieldname);
            if (null == field) {
                log.warn(" no such field : " + fieldname + " (attribute = '" + intProperty + "')");
                return "";
            }

            final int propVal = toInt((String) field.getAttributeMap().get(intProperty), 10000);
            return intProperty + "=" + String.valueOf(propVal + plus);
        }

        static int toInt(final String str, final int def) {
            return NumberUtils.isNumber(str) ? new BigDecimal(str).intValue() : def;
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _loginDate;
        final String _applicantdiv;
        final String _testdiv;
        final String _testdiv0; // 高校のみ
        final String _printDate; // 通知日付
        final String _teishutsuDate; // 提出日付
        final String _output; // 1:合格通知書 2:不合格通知書 3:補欠合格通知書 4:特待生合格通知 5:特待生通知書 6:入学金振込 7:入学承諾書 8:入学承諾書用封筒
        String _outputDiv = ""; // 1:合格者全員 2:志願者全員（合格通知書、不合格通知書のみ）3:受験番号指定 4:特待生候補
        String _outputExamno = ""; // 指定受験番号
        final String _goukakutuuchiTelno;
        final String _prgid;

        final Map _schoolBankMst;
        final String _outputNendo;

        private boolean _seirekiFlg;
        private String _principalName = "";
        private String _jobName = "";
        private String _schoolName = "";
        private String _schoolNamePath;
        private String _schoolStampPath;
        private String _schoolLogoPath;
        private String _imagePath;
        private String _documentRoot;
        private String _certifSchoolDatRemark6 = "";
        private String _certifSchoolDatRemark7 = "";
        private String _certifSchoolDatRemark8 = "";
        private String _certifSchoolDatRemark9 = "";
        private String _goukakutuuchiNyushiTantou;

        private final boolean _isOutputDebug;
        private final boolean _isOutputDebugVrsOut;
        private final boolean _isOutputDebugForm;
        private List _furikomiTextList;
        private Map<String, File> _mergedFormMap = new HashMap<String, File>();
        private Set<String> _logOnce = new TreeSet<String>();

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("YEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv      = request.getParameter("TESTDIV");
            _testdiv0     = request.getParameter("TESTDIV0");
            _loginDate    = request.getParameter("LOGIN_DATE");
            _printDate   = request.getParameter("PRINT_DATE");
            _teishutsuDate = request.getParameter("TEISHUTSU_DATE");
            _output = request.getParameter("OUTPUT");
            _goukakutuuchiTelno = request.getParameter("GOUKAKUTUUCHI_TELNO");
            _prgid = request.getParameter("PRGID");

            try {
                if (null != request.getParameter("GOUKAKUTUUCHI_NYUSHITANTOU")) {
                    _goukakutuuchiNyushiTantou = new String(request.getParameter("GOUKAKUTUUCHI_NYUSHITANTOU").getBytes("ISO8859-1"));
                }
                log.info(" _goukakutuuchiNyushiTantou = " + _goukakutuuchiNyushiTantou);
            } catch (Exception e) {
                log.error("exception!", e);
            }

            log.debug(" 対象帳票 = " + _output);
            if (NumberUtils.isDigits(_output)) {
                final String outputA = " ABCDEFGH";
                final String ab = String.valueOf(outputA.charAt(Integer.parseInt(_output)));
                _outputDiv = request.getParameter("OUTPUT" + ab);
                log.info(" 対象者区分 (" + ab + ") = " + _outputDiv);
                if (OUTPUT_DIV3_SHITEI.equals(_outputDiv)) {
                    _outputExamno = request.getParameter("EXAMNO" + ab);
                    log.info(" 対象受検者番号 = " + _outputExamno);
                }
            }

            setCertifSchoolDat(db2);
            _seirekiFlg = true; // getSeirekiFlg(db2);
            _outputNendo = _seirekiFlg ? _entexamyear : KNJ_EditDate.gengou(db2, Integer.parseInt(_entexamyear));
            _schoolBankMst = getSchoolBankMst(db2);
            _documentRoot = request.getParameter("DOCUMENTROOT");
            loadControlMst(db2);
            final String jorh = APPLICANTDIV1.equals(_applicantdiv) ? "J" : "H";
            _schoolStampPath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLSTAMP_" + jorh + ".bmp");
            _schoolLogoPath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLLOGO_" + jorh + ".jpg");
            _schoolNamePath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLNAME_" + jorh + ".jpg");
            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugVrsOut = ArrayUtils.contains(outputDebug, "VrsOut");
            _isOutputDebugForm = ArrayUtils.contains(outputDebug, "form");
            _furikomiTextList = getFurikomiTextList(db2);
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJL326F' AND NAME = '" + propName + "' "));
        }

        private String checkFilePath(final String path) {
            final boolean exists = new File(path).exists();
            if (!exists) {
                log.info("file not found:" + path);
                return null;
            }
            log.info("exists:" + path);
            return path;
        }

        public void close() {
            for (final File file : _mergedFormMap.values()) {
                log.info(" file " + file.getPath() + " : delete? " + file.delete());
            }
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindCd;
            if (_applicantdiv.equals(APPLICANTDIV1)) {
                certifKindCd = "105";
            } else {
                certifKindCd = "106";
            }

            final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '" + certifKindCd + "' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            _principalName = KnjDbUtils.getString(row, "PRINCIPAL_NAME");
            _jobName = KnjDbUtils.getString(row, "JOB_NAME");
            _schoolName = KnjDbUtils.getString(row, "SCHOOL_NAME");
            _certifSchoolDatRemark6 = KnjDbUtils.getString(row, "REMARK6");
            _certifSchoolDatRemark7 = KnjDbUtils.getString(row, "REMARK7");
            _certifSchoolDatRemark8 = KnjDbUtils.getString(row, "REMARK8");
            _certifSchoolDatRemark9 = KnjDbUtils.getString(row, "REMARK9");
        }

        /* 西暦表示にするのかのフラグ  */
        private boolean getSeirekiFlg(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
            final boolean seirekiFlg = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));
            return seirekiFlg;
        }

        private String gethidukeYoubi(final DB2UDB db2, final String inputDate) {
            // 西暦か和暦はフラグで判断
            if (null != inputDate) {
                return gethiduke(db2, inputDate) + "(" + KNJ_EditDate.h_format_W(inputDate) + ")";
            }
            return null;
        }

        private String gethiduke(final DB2UDB db2, final String inputDate) {
            // 西暦か和暦はフラグで判断
            String date;
            if (null != inputDate) {
                if (_seirekiFlg) {
                    date = inputDate.toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(inputDate);
                } else {
                    date = KNJ_EditDate.h_format_JP(db2, inputDate);
                }
                return date;
            }
            return null;
        }

        private Map getSchoolBankMst(final DB2UDB db2) {
            final String cd = APPLICANTDIV1.equals(_applicantdiv) ? "1020" : "2020";
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT NMG203.ABBV1 AS DEPOSIT_ITEM_NAME, T1.* "
                                 + " FROM SCHOOL_BANK_MST T1 "
                                 + " LEFT JOIN NAME_MST NMG203 ON NMG203.NAMECD1 = 'G203' AND NMG203.NAMECD2 = T1.DEPOSIT_ITEM "
                                 + " WHERE BANKTRANSFERCD = '" + cd + "' ";

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                final ResultSetMetaData meta = rs.getMetaData();
                if (rs.next()) {
                    for (int i = 1; i < meta.getColumnCount(); i++) {
                        rtn.put(meta.getColumnLabel(i), rs.getString(meta.getColumnLabel(i)));
                    }
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }


        private void loadControlMst(final DB2UDB db2) {
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * 引数に対応したENTEXAM_FURIKOMI_TEXT_DATのマップ
         * @param testdiv 入試区分
         * @param testdiv0 入試回数
         * @param form フォーム
         * @return 引数に対応したENTEXAM_FURIKOMI_TEXT_DATのマップ
         */
        private Map getFurikomiTextDat(final String testdiv, final String testdiv0, final String form) {
            for (int i = 0; i < _furikomiTextList.size(); i++) {
                final Map row = (Map) _furikomiTextList.get(i);
                if (null != testdiv && !testdiv.equals(row.get("TESTDIV"))) {
                    continue;
                }
                if (null != testdiv0 && !testdiv0.equals(row.get("TESTDIV0"))) {
                    continue;
                }
                if (null != row.get("FORM")) {
                    final List forms = Arrays.asList(StringUtils.split(getString(row, "FORM"), ","));
                    if (!forms.contains(form)) {
                        // レコードにFORMが設定されている場合、指定formが含まれていなければ対象外
                        continue;
                    }
                }
                return row;
            }
            logOnce(" no record for : testdiv = " + testdiv + ", testCount = " + testdiv0);
            return new HashMap();
        }

        private void logOnce(final String info) {
            if (!_logOnce.contains(info)) {
                log.info(info);
                _logOnce.add(info);
            }
        }
        private List getFurikomiTextList(final DB2UDB db2) {
            final String sql = "SELECT * FROM ENTEXAM_FURIKOMI_TEXT_DAT WHERE ENTEXAMYEAR = '" + _entexamyear + "' AND APPLICANTDIV = '" + _applicantdiv + "' ";
            return KnjDbUtils.query(db2, sql);
        }

    }
}//クラスの括り

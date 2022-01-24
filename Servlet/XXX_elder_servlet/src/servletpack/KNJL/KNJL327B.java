/*
 * $Id: 703d6916930bb9db52f368a51918f61af28592eb $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２７Ｂ＞  各種通知書（個人）
 **/
public class KNJL327B {

    private static final Log log = LogFactory.getLog(KNJL327B.class);
    
    private static final String GOUKAKU_SEIKI = "1";
    private static final String GOUKAKU_TANKIRI = "2";

    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
	        response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }
    
    public void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List list = Applicant.load(db2, _param);
        final Map paymentMoneyMap = PaymentMoney.load(db2, _param);
        
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Applicant appl = (Applicant) it.next();
            if ("1".equals(_param._form)) {
                printGoukaku(svf, db2, appl);
            } else if ("2".equals(_param._form)) {
                printTankiriGoukaku(svf, db2, appl);
            } else if ("3".equals(_param._form)) {
                printKakuyaku(svf, db2, appl);
            } else if ("4".equals(_param._form)) {
                printFurikomi(svf, db2, appl, paymentMoneyMap);
            }
        }
    }
    
    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }

    /*
     * 合格通知書
     */
    public void printGoukaku(final Vrw32alp svf, final DB2UDB db2, final Applicant appl) {
        final String form = "KNJL327B_1.frm";
        svf.VrSetForm(form, 1);
        
        svf.VrsOut("NENDO", _param._nendo); // 年度
        svf.VrsOut("DATE", _param._dateStr); // 日付
        svf.VrsOut("KIND1", _param._testdivAbbv1); // 入試制度
        svf.VrsOut("FINSCHOOL_NAME", StringUtils.defaultString(appl._finschoolName)); // 出身中学校名
        svf.VrsOut("NAME", appl._name); // 氏名
        svf.VrsOut("EXAM_NO", appl._examno); // 受験番号
        svf.VrsOut("KIND2", _param._testdivAbbv1); // 入試制度
        svf.VrsOut("JUDGE", "合格"); // 判定結果
        svf.VrsOut("PASS_COURSE", StringUtils.defaultString(appl._seikiCourseName) + ("1".equals(appl._tankiriGoukakuFlg) ? "*" : "")); // 合格区分
        svf.VrEndPage();
        _hasData = true;
    }
    
    /*
     * 単願切換合格通知書
     */
    public void printTankiriGoukaku(final Vrw32alp svf, final DB2UDB db2, final Applicant appl) {
        final String form = "KNJL327B_2.frm";
        svf.VrSetForm(form, 1);
        
        svf.VrsOut("NENDO", _param._nendo); // 年度
        svf.VrsOut("DATE", _param._dateStr); // 日付
        svf.VrsOut("KIND1", _param._testdivAbbv1); // 入試制度
        svf.VrsOut("FINSCHOOL_NAME", StringUtils.defaultString(appl._finschoolName)); // 出身中学校名
        svf.VrsOut("EXAM_NO", appl._examno); // 受験番号
        svf.VrsOut("NAME", appl._name); // 氏名
        svf.VrsOut("KIND2", _param._testdivAbbv1); // 入試制度
        svf.VrsOut("JUDGE", "単願切換合格"); // 判定結果
        svf.VrsOut("PASS_COURSE", appl._tankiriCourseName); // 合格区分
        
        svf.VrsOut("TEXT1", "単願切換" + StringUtils.defaultString(appl._tankiriCourseName) + "合格は" + _param.format(true, _param._earlySDate, _param._earlySTime) + "から"); // 本本
        svf.VrsOut("TEXT2", _param.format(false, _param._earlyEDate, _param._earlyETime) + "までに入学手続きを完了しない場合は"); // 本文
        svf.VrsOut("TEXT3", "無効になりますのでご注意下さい。"); // 本文

        svf.VrEndPage();
        _hasData = true;
    }
    
    /*
     * 振込金依頼書
     */
    public void printFurikomi(final Vrw32alp svf, final DB2UDB db2, final Applicant appl, final Map paymentMoneyMap) {
        final PaymentMoney paymentMoney = (PaymentMoney) paymentMoneyMap.get(StringUtils.defaultString(appl._specialDiv, "0"));
        if (null == paymentMoney) {
            return;
        }
        final int entMoney = Integer.parseInt(StringUtils.defaultString(paymentMoney._entMoney, "0"));
        final int facMoney = Integer.parseInt(StringUtils.defaultString(paymentMoney._facMoney, "0"));
        final int[] money = new int[] {entMoney, facMoney };
        final String[] items = {paymentMoney._entMoneyName, paymentMoney._facMoneyName};
        final int total = money[0] + money[1];
        if (0 == total) {
            return;
        }
        
        final String form = "KNJL327B_3.frm";
        svf.VrSetForm(form, 1);
        
        svf.VrsOut("ZIPNO", "310-0036"); // 郵便番号
        svf.VrsOut("SCHOOL_ADDR", "茨城県水戸市新荘3-2-28"); // 学校住所
        svf.VrsOut("SCHOOL_NAME", "常磐大学高等学校"); // 学校名
        svf.VrsOut("TELNO", "(029)224-1707"); // 電話番号
        final int nameByteLen = getMS932ByteLength(appl._name);
        svf.VrsOut("NAME1_" + (nameByteLen <= 20 ? "1" : nameByteLen <= 30 ? "2" : "3"), appl._name); // 氏名
        svf.VrsOut("NAME2_" + (nameByteLen <= 16 ? "1" : nameByteLen <= 30 ? "2" : "3"), appl._name); // 氏名
        svf.VrsOut("NAME3", appl._name); // 氏名
        svf.VrsOut("LIMIT_DATE", _param.getDateStr2(_param._nounyuDate)); // 納入期限
        svf.VrsOut("KANA", appl._nameKanaKatakana); // かな氏名
        svf.VrsOut("EXAM_NO", appl._examno); // 受験番号
        svf.VrsOut("PAY_MONEY", String.valueOf(total)); // 納入金額
        svf.VrsOut("BANK_NO", "9009191"); // 口座番号
        svf.VrsOut("BANK_NAME", "常陽銀行末広町支店"); // 銀行名
        svf.VrsOut("BANK_RECEIPT", "常磐大学高等学校"); // 受取人
        svf.VrsOut("PAY_NO", "010" + appl._examno + "00"); // 納金番号
        svf.VrsOut("COURSE_NAME", "普通科"); // コース名
        
        int l = 1;
        for (int i = 0; i < items.length; i++) {
            if (0 != money[i]) {
                svf.VrsOutn("ITEM_NAME",  l, items[i]); // 品目名
                svf.VrsOutn("ITEM_MONEY", l, String.valueOf(money[i])); // 品目金額
                l++;
            }
        }
        
        svf.VrEndPage();
        _hasData = true;
    }
    
    /*
     * 入学確約書
     */
    public void printKakuyaku(final Vrw32alp svf, final DB2UDB db2, final Applicant appl) {
        final String form = "KNJL327B_4.frm";
        svf.VrSetForm(form, 1);
        String kind = "", courseName = "", aster = "";
        if (GOUKAKU_SEIKI.equals(_param._goukaku)) {
            kind = null;
            courseName = appl._seikiCourseName;
            aster = "1".equals(appl._tankiriGoukakuFlg) ? "*" : "";
        } else if (GOUKAKU_TANKIRI.equals(_param._goukaku)) {
            kind = "単願切換用";
            courseName = appl._tankiriCourseName;
            aster = "";
        }
        if (null != kind) {
            svf.VrsOut("KIND", kind); // 単願切換用
            svf.VrAttribute("KIND", "UnderLine=(0,2,3)"); // 単願切換用
        }
        svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
        svf.VrsOut("PRINCIPAL_NAME", null == _param._principalName ? "" : trim(_param._jobName) + "　" + StringUtils.defaultString(trim(_param._principalName) + "　殿")); // 校長名
        svf.VrsOut("TEXT", "貴校" + "普通科" + "第１学年に入学いたします。"); // 文言
        svf.VrsOut("FINSCHOOL_NAME", StringUtils.defaultString(appl._finschoolName)); // 出身中学校名
        svf.VrsOut("EXAM_NO", appl._examno); // 受験番号
        svf.VrsOut("PASS_COURSE", courseName + aster); // 合格区分
        // svf.VrsOut("GURD_NAME", appl._gname); // 保護者氏名
        svf.VrsOut("NAME", appl._name); // 氏名
        
        final String[] kakuyakuDateStr = null == _param._logindate ? null : KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(_param._logindate.replace('/', '-')));
        if (null != kakuyakuDateStr && kakuyakuDateStr.length > 1) {
            svf.VrsOut("DATE", StringUtils.defaultString(kakuyakuDateStr[0]) + "　　年　　月　　日"); // 日付
        }
        
        svf.VrEndPage();
        _hasData = true;
    }
    
    private static String trim(final String s) {
        if (null == s) {
            return s;
        }
        final StringBuffer stb = new StringBuffer(s);
        // 後ろのスペースをカット
        int k = stb.length();
        for (int i = stb.length() - 1; i >= 0; i--) {
            if (stb.charAt(i) == ' ' || stb.charAt(i) == '　') {
                k = i;
            } else {
                break;
            }
        }
        stb.delete(k, stb.length());
        // 前のスペースをカット
        int j = 0;
        for (int i = 0; i < stb.length(); i++) {
            if (stb.charAt(i) == ' ' || stb.charAt(i) == '　') {
                j = i + 1;
            } else {
                break;
            }
        }
        stb.delete(0, j);
        return stb.toString();
    }
    
    private static class Applicant {
        final String _receptno;
        final String _examno;
        final String _fsCd;
        final String _finschoolName;
        final String _name;
        final String _nameKanaKatakana;
        final String _seikiCourseName;
        final String _tankiriGoukakuFlg;
        final String _tankiriCourseName;
        final String _specialDiv;
        final String _gname;

        Applicant(
            final String receptno,
            final String examno,
            final String fsCd,
            final String finschoolName,
            final String name,
            final String nameKanaKatakana,
            final String seikiCourseName,
            final String tankiriGoukakuFlg,
            final String tankiriCourseName,
            final String specialDiv,
            final String gname
        ) {
            _receptno = receptno;
            _examno = examno;
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _name = name;
            _nameKanaKatakana = nameKanaKatakana;
            _seikiCourseName = seikiCourseName;
            _tankiriGoukakuFlg = tankiriGoukakuFlg;
            _tankiriCourseName = tankiriCourseName;
            _specialDiv = specialDiv;
            _gname = gname;
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String receptno = rs.getString("RECEPTNO");
                    final String examno = rs.getString("EXAMNO");
                    final String fsCd = rs.getString("FS_CD");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String name = rs.getString("NAME");
                    final String nameKanaKatakana = rs.getString("NAME_KANA_KATAKANA");
                    final String seikiCourseName = rs.getString("SEIKI_COURSE_NAME");
                    final String tankiriGoukakuFlg = rs.getString("TANKIRI_GOUKAKU_FLG");
                    final String tankiriCourseName = rs.getString("TANKIRI_COURSE_NAME");
                    final String specialDiv = rs.getString("SPECIAL_DIV");
                    final String gname = rs.getString("GNAME");
                    final Applicant applicant = new Applicant(receptno, examno, fsCd, finschoolName, name, nameKanaKatakana, seikiCourseName, tankiriGoukakuFlg, tankiriCourseName, specialDiv, gname);
                    list.add(applicant);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     RECEPT.RECEPTNO, ");
            stb.append("     RECEPT.EXAMNO, ");
            stb.append("     BASE.FS_CD, ");
            stb.append("     FIN.FINSCHOOL_NAME, ");
            stb.append("     BASE.NAME, ");
            stb.append("     TRANSLATE_H_K(BASE.NAME_KANA) AS NAME_KANA_KATAKANA, ");
            stb.append("     CRS1.EXAMCOURSE_NAME AS SEIKI_COURSE_NAME, ");
            stb.append("     RDET2.REMARK4 AS TANKIRI_GOUKAKU_FLG, ");
            stb.append("     CRS2.EXAMCOURSE_NAME AS TANKIRI_COURSE_NAME, ");
            stb.append("     CRSJG.SPECIAL_DIV, ");
            stb.append("     ADDR.GNAME ");
            stb.append(" FROM ENTEXAM_RECEPT_DAT RECEPT ");
            stb.append(" LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RDET1 ON RDET1.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
            stb.append("     AND RDET1.APPLICANTDIV = RECEPT.APPLICANTDIV ");
            stb.append("     AND RDET1.TESTDIV = RECEPT.TESTDIV ");
            stb.append("     AND RDET1.EXAM_TYPE = RECEPT.EXAM_TYPE ");
            stb.append("     AND RDET1.RECEPTNO = RECEPT.RECEPTNO ");
            stb.append("     AND RDET1.SEQ = '001' ");
            stb.append("     AND RDET1.REMARK4 = '1' ");
            stb.append(" LEFT JOIN ENTEXAM_COURSE_MST CRS1 ON CRS1.ENTEXAMYEAR = RDET1.ENTEXAMYEAR ");
            stb.append("     AND CRS1.APPLICANTDIV = RDET1.APPLICANTDIV ");
            stb.append("     AND CRS1.TESTDIV = RDET1.TESTDIV ");
            stb.append("     AND CRS1.COURSECD = RDET1.REMARK1 ");
            stb.append("     AND CRS1.MAJORCD = RDET1.REMARK2 ");
            stb.append("     AND CRS1.EXAMCOURSECD = RDET1.REMARK3 ");
            stb.append(" LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RDET2 ON RDET2.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
            stb.append("     AND RDET2.APPLICANTDIV = RECEPT.APPLICANTDIV ");
            stb.append("     AND RDET2.TESTDIV = RECEPT.TESTDIV ");
            stb.append("     AND RDET2.EXAM_TYPE = RECEPT.EXAM_TYPE ");
            stb.append("     AND RDET2.RECEPTNO = RECEPT.RECEPTNO ");
            stb.append("     AND RDET2.SEQ = '002' ");
            stb.append("     AND RDET2.REMARK4 = '1' ");
            stb.append(" LEFT JOIN ENTEXAM_COURSE_MST CRS2 ON CRS2.ENTEXAMYEAR = RDET2.ENTEXAMYEAR ");
            stb.append("     AND CRS2.APPLICANTDIV = RDET2.APPLICANTDIV ");
            stb.append("     AND CRS2.TESTDIV = RDET2.TESTDIV ");
            stb.append("     AND CRS2.COURSECD = RDET2.REMARK1 ");
            stb.append("     AND CRS2.MAJORCD = RDET2.REMARK2 ");
            stb.append("     AND CRS2.EXAMCOURSECD = RDET2.REMARK3 ");
            stb.append(" INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
            stb.append("     AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
            stb.append("     AND BASE.TESTDIV = RECEPT.TESTDIV ");
            stb.append("     AND BASE.EXAMNO = RECEPT.EXAMNO ");
            stb.append(" LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("     AND ADDR.EXAMNO = BASE.EXAMNO ");
            stb.append(" LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST CRSJG ON CRSJG.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            if (GOUKAKU_SEIKI.equals(param._goukaku)) {
                stb.append("     AND CRSJG.NORMAL_PASSCOURSECD = RDET1.REMARK1 ");
                stb.append("     AND CRSJG.NORMAL_PASSMAJORCD = RDET1.REMARK2 ");
                stb.append("     AND CRSJG.NORMAL_PASSEXAMCOURSECD = RDET1.REMARK3 ");
                stb.append("     AND CRSJG.EARLY_PASSCOURSECD IS NULL ");
                stb.append("     AND CRSJG.EARLY_PASSMAJORCD IS NULL ");
                stb.append("     AND CRSJG.EARLY_PASSEXAMCOURSECD IS NULL ");
            } else if (GOUKAKU_TANKIRI.equals(param._goukaku)) {
                stb.append("     AND CRSJG.EARLY_PASSCOURSECD = RDET2.REMARK1 ");
                stb.append("     AND CRSJG.EARLY_PASSMAJORCD = RDET2.REMARK2 ");
                stb.append("     AND CRSJG.EARLY_PASSEXAMCOURSECD = RDET2.REMARK3 ");
            }
            stb.append(" LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ");
            stb.append(" WHERE ");
            stb.append("     RECEPT.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND RECEPT.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND RECEPT.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
            if ("2".equals(param._output)) {
                if (!"".equals(param._examnoS) && param._examnoS != null) {
                    stb.append("     AND RECEPT.EXAMNO >= '" + param._examnoS + "' ");
                }
                if (!"".equals(param._examnoE) && param._examnoE != null) {
                    stb.append("     AND RECEPT.EXAMNO <= '" + param._examnoE + "' ");
                }
            }
            if (GOUKAKU_SEIKI.equals(param._goukaku)) {
                stb.append("     AND RDET1.SEQ = '001' ");
            } else if (GOUKAKU_TANKIRI.equals(param._goukaku)) {
                stb.append("     AND RDET2.SEQ = '002' ");
                if (!"9999".equals(param._earlyPassexamcoursecd)) {
                    stb.append("     AND RDET2.REMARK3 = '" + param._earlyPassexamcoursecd + "' ");
                }
            }
            stb.append(" ORDER BY ");
            stb.append("     BASE.EXAMNO ");
            return stb.toString();
        }
    }
    
    private static class PaymentMoney {
        final String _judgeKind;
        final String _entMoney;
        final String _entMoneyName;
        final String _facMoney;
        final String _facMoneyName;
        final String _lessonMoney;
        final String _lessonMoneyName;
        final String _facMntMoney;
        final String _facMntMoneyName;

        PaymentMoney(
            final String judgeKind,
            final String entMoney,
            final String entMoneyName,
            final String facMoney,
            final String facMoneyName,
            final String lessonMoney,
            final String lessonMoneyName,
            final String facMntMoney,
            final String facMntMoneyName
        ) {
            _judgeKind = judgeKind;
            _entMoney = entMoney;
            _entMoneyName = entMoneyName;
            _facMoney = facMoney;
            _facMoneyName = facMoneyName;
            _lessonMoney = lessonMoney;
            _lessonMoneyName = lessonMoneyName;
            _facMntMoney = facMntMoney;
            _facMntMoneyName = facMntMoneyName;
        }

        public static Map load(final DB2UDB db2, final Param param) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String judgeKind = rs.getString("JUDGE_KIND");
                    final String entMoney = rs.getString("ENT_MONEY");
                    final String entMoneyName = rs.getString("ENT_MONEY_NAME");
                    final String facMoney = rs.getString("FAC_MONEY");
                    final String facMoneyName = rs.getString("FAC_MONEY_NAME");
                    final String lessonMoney = rs.getString("LESSON_MONEY");
                    final String lessonMoneyName = rs.getString("LESSON_MONEY_NAME");
                    final String facMntMoney = rs.getString("FAC_MNT_MONEY");
                    final String facMntMoneyName = rs.getString("FAC_MNT_MONEY_NAME");
                    final PaymentMoney paymentmoney = new PaymentMoney(judgeKind, entMoney, entMoneyName, facMoney, facMoneyName, lessonMoney, lessonMoneyName, facMntMoney, facMntMoneyName);
                    map.put(judgeKind, paymentmoney);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.JUDGE_KIND, ");
            stb.append("     T1.ENT_MONEY, ");
            stb.append("     T1.ENT_MONEY_NAME, ");
            stb.append("     T1.FAC_MONEY, ");
            stb.append("     T1.FAC_MONEY_NAME, ");
            stb.append("     T1.LESSON_MONEY, ");
            stb.append("     T1.LESSON_MONEY_NAME, ");
            stb.append("     T1.FAC_MNT_MONEY, ");
            stb.append("     T1.FAC_MNT_MONEY_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_PAYMENT_MONEY_YMST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            return stb.toString();
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _earlyPassexamcoursecd;
        final String _nendo;
        final String _date;
        final String _logindate;
        final String _output; // 出力範囲 1:全員 2:指定
        final String _examnoS; // 出力範囲指定・開始受験番号
        final String _examnoE; // 出力範囲指定・終了受験番号
        final String _goukaku; // 1:正規合格 2:単願切換合格 
        final String _form; // 正規合格(1:合格通知書 3:入学確約書 4:振込依頼書) 単願切換合格(2:単願切換合格通知書 3:入学確約書 4:振込依頼書)
        final String _nounyuDate; // 納入期限 (振込依頼書のみ)
        
        // 以下単願切換合格 単願切換合格通知書のみ
        final String _earlySDate; // 早期入学手続期間（自）
        final String _earlySTime; // 時から
        final String _earlyEDate; // 早期入学手続期間（至）
        final String _earlyETime; // 時まで

        final String _documentroot;

        final String _applicantdivName;
        final String _testdivName1;
        final String _testdivAbbv1;
        final String _dateStr;
        final String _schoolName;
        final String _jobName;
        final String _principalName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _earlyPassexamcoursecd = request.getParameter("EARLY_PASSEXAMCOURSECD");
            _nendo = KenjaProperties.gengou(Integer.parseInt(_entexamyear)) + "年度";
            _date = request.getParameter("TSUCHI_DATE");
            _logindate = request.getParameter("LOGIN_DATE");
            _dateStr = null == _date ? null : getDateStr(_date.replace('/', '-'));
            _goukaku = request.getParameter("GOUKAKU");
            _output = request.getParameter("OUTPUT");
            _examnoS = request.getParameter("EXAMNO_S");
            _examnoE = request.getParameter("EXAMNO_E");
            _form = request.getParameter("FORM");
            _nounyuDate = request.getParameter("NOUNYU_DATE");
            _earlySDate = request.getParameter("EARLY_S_DATE");
            _earlySTime = request.getParameter("EARLY_S_TIME");
            _earlyEDate = request.getParameter("EARLY_E_DATE");
            _earlyETime = request.getParameter("EARLY_E_TIME");
            _documentroot = request.getParameter("DOCUMENTROOT");
            
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivName1 = getNameMst(db2, "NAME1", "L004", _testdiv);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
            
            _schoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _principalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _jobName = StringUtils.defaultString(getCertifSchoolDat(db2, "JOB_NAME"), "校長");
        }
        
        public String format(final boolean printNengo, final String date, final String hour) {
            String rtn = "";
            if (!StringUtils.isBlank(date)) {
                if (printNengo) {
                    rtn += KenjaProperties.gengou(Integer.parseInt(date.substring(0, 4))) + "年";
                }
                rtn = hankakuToZenkaku(rtn + KNJ_EditDate.h_format_JP_MD(date));
                final Calendar cal = Calendar.getInstance();
                cal.setTime(Date.valueOf(date.replace('/', '-')));
                final String youbi = "（" + new String[] {null, "日", "月", "火", "水", "木", "金", "土"} [cal.get(Calendar.DAY_OF_WEEK)] + "）";
                rtn += youbi;
            }
            if (!StringUtils.isBlank(hour)) {
                rtn += hankakuToZenkaku(hour) + "時";
            }
            return rtn;
        }
        
        private String hankakuToZenkaku(final String s) {
            final StringBuffer stb = new StringBuffer();
            for (int i = 0; i < s.length(); i++) {
                final char ch = s.charAt(i);
                if ('0' <= ch && ch <= '9') {
                    stb.append((char) ('０' + (ch - '0')));
                } else {
                    stb.append(ch);
                }
            }
            return stb.toString();
        }

        private String getDateStr(final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(date);
        }
        
        
        public String getDateStr2(final String date) {
            String rtn = "";
            if (!StringUtils.isBlank(date)) {
                rtn = KNJ_EditDate.h_format_JP(date);
                final Calendar cal = Calendar.getInstance();
                cal.setTime(Date.valueOf(date.replace('/', '-')));
                final String youbi = "（" + new String[] {null, "日", "月", "火", "水", "木", "金", "土"} [cal.get(Calendar.DAY_OF_WEEK)] + "）";
                rtn += youbi;
            }
            return rtn;
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
        
        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT " + field + " ");
                sql.append(" FROM CERTIF_SCHOOL_DAT ");
                sql.append(" WHERE YEAR = '" + _entexamyear + "' ");
                sql.append("   AND CERTIF_KINDCD = '106' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof


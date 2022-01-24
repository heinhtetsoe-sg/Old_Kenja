/*
 * $Id: 03c89551f1d8e979eb031dfeaf736d6d7ea8f13e $
 *
 * 作成日: 2011/10/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJA.detail.KNJ_AddressRecSql;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;

/**
 * 学校教育システム 賢者 [進路情報管理] 共愛用中学調査書（共愛高校以外進学者用）
 */
public class KNJE075Y {

    private static final Log log = LogFactory.getLog(KNJE075Y.class);

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定

    private static final KNJ_PersonalinfoSql personalinfoSql = new KNJ_PersonalinfoSql();
    private static final KNJ_AddressRecSql addressRecSql = new KNJ_AddressRecSql();
    
    private static final String FORM_NAME = "KNJE075Y.frm";
    private static final String FORM_NAME2 = "KNJE075Y_2.frm";

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        boolean nonedata = false;

        // print svf設定
        sd.setSvfInit(request, response, svf);

        // ＤＢ接続
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
            return;
        }

        final Param param = getParam(request, db2);

        nonedata = printSvf(request, db2, svf, param);

        // 終了処理
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }

    private Param getParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private boolean printSvf(final HttpServletRequest request, final DB2UDB db2, final Vrw32alp svf, final Param param) {
        boolean nonedata = false;

        final List selectList = Arrays.asList(param._categorySelected); // 出力対象学籍番号を格納

        for (final Iterator t = selectList.iterator(); t.hasNext();) { // --学籍番号の繰り返し
            final String schregno = (String) (t.next());
            final Student student = new Student(schregno);
            student.load(db2, param);

            final Form knj131 = new Form();
            if (knj131.printSvf(db2, svf, param, student)) {
                nonedata = true;
            }
        }

        return nonedata;
    }
    
    private static int getMS932ByteLength(final String str) {
        int keta = 0;
        if (null != str) {
            try {
                keta = str.getBytes("MS932").length;
            } catch (UnsupportedEncodingException e) {
                log.error("exception!", e);
            }
        }
        return keta;
    }

    private static class Student {
        final String _schregno;
        String _name;
        String _kana;
        String _sexName;
        String _birthday;
        String _grddate;
        String _grddiv;
        String _birthdayFlg;
        List _addressList = Collections.EMPTY_LIST;
        Map _attendanceMap = Collections.EMPTY_MAP;
        List _behaviorList = Collections.EMPTY_LIST;
        Map _hexamentremarkDatMap = Collections.EMPTY_MAP;
        HexamentremarkHDat _hexamentremarkHDat;
        public Student(final String schregno) {
            _schregno = schregno;
        }
        
        public void load(final DB2UDB db2, final Param param) {
            loadName(db2, param);
            _addressList = Address.load(db2, param, _schregno);
            _attendanceMap = Attendance.load(db2, param, _schregno);
            _behaviorList = BehaviorDat.load(db2, param, _schregno);
            _hexamentremarkDatMap = HexamentremarkDat.load(db2, param, _schregno);
            _hexamentremarkHDat = HexamentremarkHDat.load(db2, param, _schregno);
        }
        
        private void loadName(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                // 生徒名取得および印刷
                int p = 0;
                ps = db2.prepareStatement(personalinfoSql.sql_info_reg("1111111000"));
                ps.setString(++p, _schregno); // 学籍番号
                ps.setString(++p, param._year); // 年度
                ps.setString(++p, param._gakki); // 学期
                ps.setString(++p, _schregno); // 学籍番号
                ps.setString(++p, param._year); // 年度
                rs = ps.executeQuery();
                if (rs.next()) {
                    _name = rs.getString("NAME");
                    _kana = rs.getString("NAME_KANA");
                    _sexName = rs.getString("SEX");
                    _birthday = rs.getString("BIRTHDAY");
                    _grddate = rs.getString("GRD_DATE");
                    _grddiv = rs.getString("GRD_DIV");
                    _birthdayFlg = rs.getString("BIRTHDAY_FLG");
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_5 SCHREG_INFO error!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }
    
    private static class Attendance {
        
        final String _lesson;
        final String _suspendMourning;
        final String _abroad;
        final String _requirePresent;
        final String _present;
        final String _absent;

        public Attendance(String lesson, String suspendMourning, String abroad, String requirePresent, String present, String absent) {
            _lesson = lesson;
            _suspendMourning = suspendMourning;
            _abroad = abroad;
            _requirePresent = requirePresent;
            _present = present;
            _absent = absent;
        }

        public static Map load(final DB2UDB db2, Param param, final String schregno) {
            final Map dataMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int p = 0;
                ps = db2.prepareStatement(Attendance.sql());
                ps.setString(++p, param._year); // 年度
                ps.setString(++p, schregno); // 学籍番号
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Integer g = Integer.valueOf(rs.getString("ANNUAL"));
                    final String lesson = rs.getString("LESSON"); // 授業日数
                    final String suspendMourning = rs.getString("SUSPEND_MOURNING"); // 出停・忌引
                    final String abroad = rs.getString("ABROAD"); // 留学
                    final String requirePresent = rs.getString("REQUIREPRESENT"); // 要出席
                    final String present = rs.getString("PRESENT"); // 出席
                    final String absent = rs.getString("ABSENT"); // 欠席
                    final Attendance a = new Attendance(lesson, suspendMourning, abroad, requirePresent, present, absent);
                    dataMap.put(g, a);
                }
            } catch (Exception ex) {
                log.debug("printSvfDetail_4 error!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return dataMap;
        }

        private static String sql() {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  ANNUAL, ");
            stb.append("  VALUE(CLASSDAYS,0) AS CLASSDAYS, ");
            stb.append("  CASE WHEN S1.SEM_OFFDAYS = '1'THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) ");
            stb.append("    ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            stb.append("  END AS LESSON, ");
            stb.append("  VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSPEND_MOURNING, ");
            stb.append("  VALUE(SUSPEND,0) AS SUSPEND, ");
            stb.append("  VALUE(MOURNING,0) AS MOURNING, ");
            stb.append("  VALUE(ABROAD,0) AS ABROAD, ");
            stb.append("  CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append("    THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
            stb.append("    ELSE VALUE(REQUIREPRESENT,0) ");
            stb.append("  END AS REQUIREPRESENT, ");
            stb.append("  VALUE(PRESENT,0) AS PRESENT, ");
            stb.append("  CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append("    THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
            stb.append("    ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
            stb.append("  END AS ABSENT ");
            stb.append("FROM    SCHREG_ATTENDREC_DAT T1 ");
            stb.append("LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
            stb.append("WHERE   T1.YEAR <= ? ");
            stb.append("  AND SCHREGNO = ? ");
            // NO001 stb.append("ORDER BY ANNUAL ");
            return stb.toString();
        }
    }
    
    private static class Address {

        final String _addr1;
        final String _addr2;
        final String _addrFlg;
        final String _zipCd;
        
        public Address(final String addr1, final String addr2, final String addrFlg, final String zipCd) {
            _addr1 = addr1;
            _addr2 = addr2;
            _addrFlg = addrFlg;
            _zipCd = zipCd;
        }
        
        private static List load(final DB2UDB db2, final Param param, final String schregno) {
            final List addressList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int p = 0;
                ps = db2.prepareStatement(addressRecSql.sql_state());
                ps.setString(++p, schregno); // 学籍番号
                ps.setString(++p, param._year); // 年度
                ps.setString(++p, schregno); // 学籍番号
                ps.setString(++p, param._year); // 年度
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String addr1 = rs.getString("ADDR1");
                    final String addr2 = rs.getString("ADDR2");
                    final String addrFlg = rs.getString("ADDR_FLG");
                    final String zipCd = rs.getString("ZIPCD");
                    
                    final Address address = new Address(addr1, addr2, addrFlg, zipCd);
                    addressList.add(address);
                }
            } catch (Exception ex) {
                log.debug("printSvfDetail_3 error!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return addressList;
        }
    }
    
    private static class BehaviorDat {

        final Integer _annual;
        final String _record;
        final String _div;
        final String _code;
        
        public BehaviorDat(final Integer annual, final String record, final String div, final String code) {
            _annual = annual;
            _record = record;
            _div = div;
            _code = code;
        }
        
        public static List load(final DB2UDB db2, final Param param, final String schregno) {
            final List dataList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int p = 0;
                final String sql = BehaviorDat.sql();
                ps = db2.prepareStatement(sql);
                ps.setString(++p, param._year); // 年度
                ps.setString(++p, schregno); // 学籍番号
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Integer annual = Integer.valueOf(rs.getString("ANNUAL"));
                    final String record = rs.getString("RECORD");
                    final String div = rs.getString("DIV");
                    final String code = rs.getString("CODE");
                    final BehaviorDat bd = new BehaviorDat(annual, record, div, code);
                    dataList.add(bd);
                }
            } catch (Exception ex) {
                log.debug("printSvfDetail_3 error!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return dataList;
        }

        private static String sql() {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  DIV, CODE, ANNUAL, RECORD ");
            stb.append("FROM    BEHAVIOR_DAT T1 ");
            stb.append("WHERE   YEAR <= ? ");
            stb.append("  AND SCHREGNO = ? ");
            // NO001 stb.append("ORDER BY DIV, CODE, ANNUAL ");
            return stb.toString();
        }
    }
    
    private static class HexamentremarkDat {

        final Integer _annual;
        final String _attendrecRemark;
        final String _totalstudyval;
        final String _calssact;
        final String _studentact;
        final String _clubact;
        final String _schoolevent;

        public HexamentremarkDat(final Integer annual, final String attendrecRemark, final String totalstudyval, final String calssact, final String studentact, final String clubact, final String schoolevent) {
            _annual = annual;
            _attendrecRemark = attendrecRemark;
            _totalstudyval = totalstudyval;
            _calssact = calssact;
            _studentact = studentact;
            _clubact = clubact;
            _schoolevent = schoolevent;
        }
        
        public static Map load(final DB2UDB db2, final Param param, final String schregno) {
            final Map dataMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int p = 0;
                ps = db2.prepareStatement(sql());
                ps.setString(++p, param._year); // 年度
                ps.setString(++p, schregno); // 学籍番号
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Integer annual = Integer.valueOf(rs.getString("ANNUAL"));
                    final String attendrecRemark = rs.getString("ATTENDREC_REMARK");
                    final String totalstudyval = rs.getString("TOTALSTUDYVAL");
                    final String calssact = rs.getString("CALSSACT");
                    final String studentact = rs.getString("STUDENTACT");
                    final String clubact = rs.getString("CLUBACT");
                    final String schoolevent = rs.getString("SCHOOLEVENT");
                    
                    final HexamentremarkDat hd = new HexamentremarkDat(annual, attendrecRemark, totalstudyval, calssact, studentact, clubact, schoolevent);
                    dataMap.put(annual, hd);
                }
            } catch (Exception ex) {
                log.debug("printSvfDetail_1 error!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return dataMap;
        }

        private static String sql() {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("  ANNUAL, ATTENDREC_REMARK, TOTALSTUDYVAL, CALSSACT, STUDENTACT, CLUBACT, SCHOOLEVENT ");
            stb.append("FROM    HEXAM_ENTREMARK_DAT T1 ");
            stb.append("WHERE   YEAR <= ? ");
            stb.append("  AND SCHREGNO = ? ");
            return stb.toString();
        }
    }
    
    private static class HexamentremarkHDat {

        final String _remark;

        public HexamentremarkHDat(final String remark) {
            _remark = remark;
        }
        
        public static HexamentremarkHDat load(final DB2UDB db2, final Param param, final String schregno) {
            HexamentremarkHDat hexamhdat = new HexamentremarkHDat("");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int p = 0;
                ps = db2.prepareStatement(sql());
                ps.setString(++p, schregno); // 学籍番号
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String remark = rs.getString("REMARK");
                    
                    hexamhdat = new HexamentremarkHDat(remark);
                }
            } catch (Exception ex) {
                log.debug("printSvfDetail_1 error!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return hexamhdat;
        }

        private static String sql() {
            StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("  REMARK ");
            stb.append("FROM ");
            stb.append("  HEXAM_ENTREMARK_J_HDAT ");
            stb.append("WHERE ");
            stb.append("  SCHREGNO = ? ");
            return stb.toString();
        }
    }
    
    /**
     * 評定データ
     */
    private static class Hyotei {
        final String _classCd;

        final String _electDiv;

        final String _className;

        final Map _gradeHyoteis;

        public Hyotei(final String classCd, final String electDiv, final String className) {
            _classCd = classCd;
            _electDiv = electDiv;
            _className = className;
            _gradeHyoteis = new HashMap();
        }
        
        public String toString() {
            return "Hyotei(" + _classCd + ", " + _gradeHyoteis + ")";
        }

        public static List load(final DB2UDB db2, final Param param, final String schregno) {
            final List hyoteiList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                // log.debug(" hyotei sql = " + sql);
                ps = db2.prepareStatement(sql);
                int p = 0;
                ps.setString(++p, schregno);
                ps.setString(++p, schregno);
                ps.setString(++p, schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    // 教科コードの変わり目
                    final int g = param.getGradeCd(rs.getString("YEAR"), rs.getString("GRADE")); // 学年
                    final String electDiv = rs.getString("ELECTDIV");
                    final String classCd = rs.getString("CLASSCD");
                    final String className = rs.getString("CLASSNAME");
                    // 評定出力
                    final String value = rs.getString("VALUE");

                    Hyotei hyotei = null;

                    for (final Iterator it = hyoteiList.iterator(); it.hasNext();) {
                        final Hyotei h = (Hyotei) it.next();
                        if (h._classCd != null && h._classCd.equals(classCd)) {
                            hyotei = h;
                            break;
                        }
                    }

                    if (null == hyotei) {
                        hyotei = new Hyotei(classCd, electDiv, className);
                        hyoteiList.add(hyotei);
                    }
                    hyotei._gradeHyoteis.put(new Integer(g), value);
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_1 error!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return hyoteiList;
        }

        /**
         * priparedstatement作成 成績データ（評定）
         */
        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            // 評定の表
            stb.append(" VALUE_DATA AS( ");
            stb.append("   SELECT ");
            stb.append("        ANNUAL ");
            stb.append("       ,CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,SCHOOL_KIND ");
                stb.append("       ,CURRICULUM_CD ");
            }
            stb.append("       ,SUBCLASSCD ");
            stb.append("       ,YEAR ");
            stb.append("       ,VALUATION AS VALUE ");
            stb.append("   FROM ");
            stb.append("       SCHREG_STUDYREC_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.SCHREGNO = ? ");
            stb.append("       AND T1.YEAR <= '" + param._year + "' ");
            stb.append(" ) ");

            // 学籍の表
            stb.append(",SCHREG_DATA AS( ");
            stb.append("  SELECT ");
            stb.append("      YEAR ");
            stb.append("     ,ANNUAL  ");
            stb.append("     ,GRADE  ");
            stb.append("  FROM ");
            stb.append("     SCHREG_REGD_DAT ");
            stb.append("  WHERE ");
            stb.append("      SCHREGNO = ? ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = ? ");
            stb.append("                     AND YEAR <= '" + param._year + "' ");
            stb.append("                 GROUP BY GRADE) ");
            stb.append("  GROUP BY ");
            stb.append("      YEAR ");
            stb.append("      ,ANNUAL ");
            stb.append("      ,GRADE ");
            stb.append(") ");

            // メイン表
            stb.append("SELECT ");
            stb.append("     T2.YEAR ");
            stb.append("    ,T2.GRADE ");
            stb.append("    ,T3.ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T3.CLASSCD || '-' || T3.SCHOOL_KIND AS CLASSCD ");
            } else {
                stb.append("    ,T3.CLASSCD ");
            }
            stb.append("    ,CASE WHEN T3.CLASSORDERNAME1 IS NOT NULL THEN T3.CLASSORDERNAME1 ELSE T3.CLASSNAME END AS CLASSNAME ");
            stb.append("    ,CASE WHEN T3.SHOWORDER2 IS NOT NULL THEN T3.SHOWORDER2 ELSE -1 END AS SHOWORDERCLASS ");
            stb.append("    ,T5.VALUE ");
            stb.append("FROM  SCHREG_DATA T2 ");
            stb.append("INNER JOIN VALUE_DATA T5 ON T5.YEAR = T2.YEAR ");
            stb.append("       AND T5.ANNUAL = T2.ANNUAL ");
            stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T5.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    AND T3.SCHOOL_KIND = T5.SCHOOL_KIND ");
            }
            stb.append("ORDER BY ");
            stb.append("    SHOWORDERCLASS, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD, ");
            } else {
                stb.append("    T5.CLASSCD, ");
            }
            stb.append("    T3.ELECTDIV, ");
            stb.append("    T2.GRADE ");
            return stb.toString();
        }
    }

    /**
     * 観点の教科（JVIEWNAME_SUB_MST、JVIEWSTAT_SUB_DAT）
     */
    private static class ClassView {
        final String _classcd; // 教科コード
        final String _classname; // 教科名称
        final int _e; // 選択
        final boolean _notPrint;
        final List _views;
        Hyotei _hyotei = null;

        public ClassView(final String classcd, final String classname, final int e, final boolean notPrint) {
            _classcd = classcd;
            _classname = classname;
            _e = e;
            _notPrint = notPrint;
            _views = new ArrayList();
        }
        public void addView(final View view) {
            _views.add(view);
        }
        public int getViewNum() {
            int c = 0;
            String viewcdOld = "";
            for (Iterator it = _views.iterator(); it.hasNext();) {
                final View view = (View) it.next();
                if (view._viewcd != null && !viewcdOld.equals(view._viewcd)) {
                    c += 1;
                    viewcdOld = view._viewcd;
                }
            }
            return c;
        }

        // 教科名のセット
        private String setClassname(final String classname) {
            if (classname == null) {
                return "";
            }
            final int viewnum = getViewNum();
            if (viewnum == 0) {
                return classname;
            }
            final int newviewnum = (classname.length() <= viewnum) ? viewnum + 1 : viewnum; // 教科間の観点行に１行ブランクを挿入
            final String newclassname;

            if (classname.length() < newviewnum) {
                final int i = (newviewnum - classname.length()) / 2 - ((newviewnum - classname.length()) % 2 == 0 ? 1 : 0);
                String space = "";
                for (int j = 0; j < i; j++) {
                    space = " " + space;
                } // 教科名のセンタリングのため、空白を挿入
                newclassname = space + classname;
            } else {
                newclassname = classname;
            }
            return newclassname;
        }

        /**
         * 評定か観点のデータがあるか
         * @return 評定か観点のデータがあればtrue、そうでなければfalse
         */
        public boolean hasData() {
            if (null != _hyotei) {
                for (final Iterator vlit = _hyotei._gradeHyoteis.keySet().iterator(); vlit.hasNext();) {
                    final Integer gi = (Integer) vlit.next();
                    final String value = (String) _hyotei._gradeHyoteis.get(gi);
                    if (null != value) {
                        return true;
                    }
                }
            }
            for (final Iterator vsit = _views.iterator(); vsit.hasNext();) {
                final View view = (View) vsit.next();
                for (final Iterator vit = view._gradeStatus.keySet().iterator(); vit.hasNext();) {
                    final Integer gi = (Integer) vit.next();
                    final String status = (String) view._gradeStatus.get(gi);
                    if (null != status) {
                        return true;
                    }
                }
            }
            return false;
        }

        private static ClassView getClassView(final List classViewList, final String classcd, final String classname, final int e) {
            ClassView classView = null;
            for (final Iterator it = classViewList.iterator(); it.hasNext();) {
                final ClassView classView0 = (ClassView) it.next();
                if (classView0._classcd.equals(classcd) && classView0._classname.equals(classname) && classView0._e == e) {
                    classView = classView0;
                    break;
                }
            }
            return classView;
        }

        private static View getView(final List viewList, final String subclasscd, final String viewcd) {
            View view = null;
            for (final Iterator it = viewList.iterator(); it.hasNext();) {
                final View view0 = (View) it.next();
                if (view0._subclasscd.equals(subclasscd) && view0._viewcd.equals(viewcd)) {
                    view = view0;
                    break;
                }
            }
            return view;
        }

        /**
         * 観点のリストを得る
         * 
         * @param db2
         * @param param
         * @param schregno
         * @return
         */
        public static List load(final DB2UDB db2, final Param param, final String schregno) {
            final List classViewList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                int p = 0;
                ps.setString(++p, schregno);
                ps.setString(++p, schregno);
                ps.setString(++p, schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    // 教科コードの変わり目
                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final boolean notPrint = "1".equals(rs.getString("NOT_PRINT"));
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");
                    final String status = rs.getString("STATUS");
                    final int e = "1".equals(rs.getString("ELECTDIV")) ? 2 : 1; // 必修:1
                                                                                // 選択:2
                    final int g = param.getGradeCd(rs.getString("YEAR"), rs.getString("GRADE")); // 学年

                    ClassView classView = getClassView(classViewList, classcd, classname, e);
                    if (null == classView) {
                        classView = new ClassView(classcd, classname, e, notPrint);
                        classViewList.add(classView);
                    }
                    View view = getView(classView._views, subclasscd, viewcd);
                    if (null == view) {
                        view = new View(subclasscd, viewcd, viewname);
                        classView.addView(view);
                    }
                    view._gradeStatus.put(new Integer(g), status);
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_1 error!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
//            for (final Iterator it = classViewList.iterator(); it.hasNext();) {
//                final ClassView cv = (ClassView) it.next();
//                log.debug(" classview " + cv._classcd + ": " + cv._classname);
//                for (Iterator itv = cv._views.iterator(); itv.hasNext();) {
//                    final View view = (View) itv.next();
//                    log.debug("   view " + view._subclasscd + ":" + view._viewcd + ": " + view._viewname);
//                }
//            }
            return classViewList;
        }

        /**
         * priparedstatement作成 成績データ（観点）
         */
        private static String sql(final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            // 観点の表
            stb.append("VIEW_DATA AS( ");
            stb.append("  SELECT ");
            stb.append("      SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,CLASSCD ");
                stb.append("       ,SCHOOL_KIND ");
                stb.append("       ,CURRICULUM_CD ");
            }
            stb.append("     ,VIEWCD ");
            stb.append("     ,YEAR ");
            stb.append("     ,STATUS ");
            stb.append("  FROM ");
            stb.append("     JVIEWSTAT_SUB_DAT T1 ");
            stb.append("  WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append("    AND T1.YEAR <= '" + param._year + "' ");
            stb.append("    AND T1.SEMESTER = '9' ");
            stb.append("    AND SUBSTR(T1.VIEWCD,3,2) <> '99' ");
            stb.append(") ");

            // 学籍の表
            stb.append(",SCHREG_DATA AS( ");
            stb.append("  SELECT  YEAR ");
            stb.append(",GRADE  ");
            stb.append("  FROM    SCHREG_REGD_DAT  ");
            stb.append("  WHERE   SCHREGNO = ?  ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = ? ");
            stb.append("                     AND YEAR <= '" + param._year + "' ");
            stb.append("                 GROUP BY GRADE)  ");
            stb.append("  GROUP BY YEAR,GRADE  ");
            stb.append(") ");

            // メイン表
            stb.append("SELECT ");
            stb.append("    T2.YEAR ");
            stb.append("   ,T2.GRADE ");
            stb.append("   ,T3.ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   ,T3.CLASSCD || '-' || T3.SCHOOL_KIND AS CLASSCD");
                stb.append("   ,T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD");
            } else {
                stb.append("   ,T3.CLASSCD");
                stb.append("   ,T2.SUBCLASSCD");
            }
            stb.append("   ,CASE WHEN T3.CLASSORDERNAME1 IS NOT NULL THEN T3.CLASSORDERNAME1 ELSE T3.CLASSNAME END AS CLASSNAME ");
            stb.append("   ,CASE WHEN T4.NAME1 IS NOT NULL THEN 1 ELSE 0 END NOT_PRINT");
            stb.append("   ,CASE WHEN T3.SHOWORDER2 IS NOT NULL THEN T3.SHOWORDER2 ELSE -1 END AS SHOWORDERCLASS ");
            stb.append("   ,T2.VIEWCD ");
            stb.append("   ,T2.VIEWNAME ");
            stb.append("   ,T1.STATUS ");
            stb.append("FROM  ( SELECT ");
            stb.append("            W2.YEAR ");
            stb.append("          , W2.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   ,W1.CLASSCD");
                stb.append("   ,W1.SCHOOL_KIND");
                stb.append("   ,W1.CURRICULUM_CD");
            }
            stb.append("          , W1.SUBCLASSCD ");
            stb.append("          , W1.VIEWCD ");
            stb.append("          , VIEWNAME ");
            stb.append("          , CASE WHEN W1.SHOWORDER IS NOT NULL THEN W1.SHOWORDER ELSE -1 END AS SHOWORDERVIEW ");
            stb.append("        FROM    JVIEWNAME_SUB_MST W1 ");
            stb.append("               ,SCHREG_DATA W2 ");
            stb.append("        WHERE W1.SCHOOL_KIND = 'J' ");
            stb.append("      ) T2 ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T2.CLASSCD ");
                stb.append("         AND T3.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            } else {
                stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T2.SUBCLASSCD, 1, 2) ");
            }
            stb.append("LEFT JOIN VIEW_DATA T1 ON T1.YEAR = T2.YEAR ");
            stb.append("    AND T1.VIEWCD = T2.VIEWCD  ");
            stb.append("    AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    AND T1.CLASSCD = T2.CLASSCD ");
                stb.append("    AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("    AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            if ("1".equals(param._useCurriculumcd) && "1".equals(param._useClassDetailDat)) {
                stb.append("LEFT JOIN (SELECT YEAR, CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS NAME1 ");
                stb.append("           FROM SUBCLASS_DETAIL_DAT WHERE SUBCLASS_SEQ = '006') T4 ON T4.YEAR = T2.YEAR ");
                stb.append("    AND T4.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
            } else {
                stb.append("LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'D020'  "); // D020に登録された科目は表示しない
                stb.append("    AND T4.NAME1 = T2.SUBCLASSCD  ");
            }
            stb.append("ORDER BY ");
            stb.append("    VALUE(SHOWORDERCLASS, -1), ");
            stb.append("    T3.CLASSCD, ");
            stb.append("    VALUE(T3.ELECTDIV, '0'), ");
            stb.append("    VALUE(T2.SHOWORDERVIEW, -1), ");
            stb.append("    T2.VIEWCD, ");
            stb.append("    T2.GRADE ");
            return stb.toString();
        }

        public String toString() {
            return "[" + _classcd + ":" + _classname + " e = " + _e + "]";
        }
    }

    /**
     * 観点データ
     */
    private static class View {
        final String _subclasscd; // 科目コード
        final String _viewcd; // 観点コード
        final String _viewname; // 観点名称
        final Map _gradeStatus; // 年次と観点のマップ

        public View(final String subclasscd, final String viewcd, final String viewname) {
            _subclasscd = subclasscd;
            _viewcd = viewcd;
            _viewname = viewname;
            _gradeStatus = new HashMap();
        }
    }

    private static class Form {
        
        private static final int VIEW_LINE1_MAX = 49;

        private static final int VIEW_LINE2_MAX = 49;

        public boolean printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student) {
            boolean nonedata = false;
            final String form = param._isNewForm ? FORM_NAME2 : FORM_NAME;
            svf.VrSetForm(form, 4);
            
            printHeader(svf, student, param);
            printSogo(svf, student, param);
            printSpecialAct(svf, student);
            printKodo(svf, student);
            printShukketsu(svf, student);
            printSyoken(svf, student);
            
            if (printKantenHyotei(db2, svf, student, param)) {
                nonedata = true;
            }

            nonedata = true;
            return nonedata;
        }

        /**
         * １．学籍等履歴情報
         */
        private void printHeader(final Vrw32alp svf, final Student student, final Param param) {
            // 学校名称の出力
            svf.VrsOut("school_name_2", param._schoolname);
            // 校長名の出力
            svf.VrsOut("STAFFNAME_1", param._principalname);
            
            svf.VrsOut("DATE", dateFormat(param, param._date, null));
            
            final int n = getMS932ByteLength(student._name);
            if (n <= 24) {
                svf.VrsOut("NAME" + "1", student._name);
            } else {
                svf.VrsOut("NAME" + "2", student._name.substring(0, 12));
                svf.VrsOut("NAME" + "3", student._name.substring(12));
            }
            svf.VrsOut("KANA", student._kana);
            svf.VrsOut("SEX", student._sexName);
            svf.VrsOut("BIRTHDAY", dateFormat(param, student._birthday, student._birthdayFlg) + "生");

            final String grddate = (null == student._grddiv || "4".equals(student._grddiv)) ? param._schoolMstGraduateDate : student._grddate;
            svf.VrsOut("TRANSFER_DATE", dateFormat(param, grddate, null));

            if ("1".equals(student._grddiv)) {
                svf.VrsOut("GRADNAME", "卒業見込・卒業");
                svf.VrAttribute("LINE1", "UnderLine=(0,3,5)"); // 打ち消し線
            } else if (null == student._grddiv || "4".equals(student._grddiv)) {
                svf.VrsOut("GRADNAME", "卒業見込・卒業");
                svf.VrAttribute("LINE2", "UnderLine=(0,3,5)"); // 打ち消し線
            }
            
            // 住所
            final int num = student._addressList.size();
            if (num != 0) {
                final Address address = (Address) student._addressList.get(num - 1);
                svf.VrsOut("ZIPCODE", address._zipCd);
                if ("1".equals(param._useAddrField2) && getMS932ByteLength(address._addr1) > 50) {
                    svf.VrsOut("GUARD_ADDRESS1_2", address._addr1);
                } else {
                    svf.VrsOut("GUARD_ADDRESS1", address._addr1);
                }
                if ("1".equals(address._addrFlg)) {
                    if ("1".equals(param._useAddrField2) && getMS932ByteLength(address._addr2) > 50) {
                        svf.VrsOut("GUARD_ADDRESS2_2", address._addr2);
                    } else {
                        svf.VrsOut("GUARD_ADDRESS2", address._addr2);
                    }
                }
            }
        }
        
        private String dateFormat(final Param param, final String date, final String birthdayFlg) {
//            if (param._isSeireki) {
            if ("1".equals(birthdayFlg)) {
                return (null == date) ? "    年  月  日" : date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return (null == date) ? "平成  年  月  日" : KNJ_EditDate.h_format_JP(date);
            }
        }

        /**
         * ２．学習の記録
         */
        private boolean printKantenHyotei(final DB2UDB db2, final Vrw32alp svf, final Student student, final Param param) {

            final List classViewList = ClassView.load(db2, param, student._schregno);
            final List hyoteiList = Hyotei.load(db2, param, student._schregno);
            for (final Iterator it = hyoteiList.iterator(); it.hasNext();) {
                final Hyotei hyotei = (Hyotei) it.next();
                for (final Iterator itv = classViewList.iterator(); itv.hasNext();) {
                    final ClassView classView = (ClassView) itv.next();
                    if (classView._classcd.equals(hyotei._classCd)) {
                        classView._hyotei = hyotei;
                    }
                }
            }

            final List classViewHisshu = new ArrayList();
            final List classViewSentaku = new ArrayList();
            for (final Iterator itv = classViewList.iterator(); itv.hasNext();) {
                final ClassView classView = (ClassView) itv.next();
                if (classView._e == 1) {
                    classViewHisshu.add(classView);
                } else if (classView._e == 2) {
                    classViewSentaku.add(classView);
                }
            }
            
            if (param._isNewForm) {
                // 新フォームは出力欄なし
            } else {
                // 選択教科出力
                printClassSentaku(svf, classViewSentaku);
            }
            
            // 必修教科出力
            printClassHisshu(svf, classViewHisshu);

            return true;
        }

        /**
         * ２．学習の記録 選択教科出力
         * @param svf
         * @param classViewSentaku
         */
        private void printClassSentaku(final Vrw32alp svf, final List classViewSentaku) {
            int line = 1;
            for (final Iterator it = classViewSentaku.iterator(); it.hasNext();) {
                final ClassView classview = (ClassView) it.next();
                if (classview._notPrint || !classview.hasData()) {
                    continue;
                }

                final Set gs = new TreeSet(); // 学年
                for (final Iterator vsit = classview._views.iterator(); vsit.hasNext();) {
                    final View view = (View) vsit.next();
                    gs.addAll(view._gradeStatus.keySet());
                }
                if (null != classview._hyotei) {
                    gs.addAll(classview._hyotei._gradeHyoteis.keySet());
                }
                
                // 教科の学年ごとに表示
                for (final Iterator itg = gs.iterator(); itg.hasNext();) {
                    final Integer gi = (Integer) itg.next();
                    final int g = gi.intValue();

                    boolean hasData = false;
                    for (final Iterator vsit = classview._views.iterator(); vsit.hasNext();) {
                        final View view = (View) vsit.next();
                        
                        final String status = (String) view._gradeStatus.get(gi);
                        if (null != status) {
                            if ("A".equals(status)) {
                                svf.VrsOutn("VIEWNAME3_" + Integer.parseInt(view._viewcd.substring(2)), line, view._viewname); // 観点
                            }
                            hasData = true;
                        }
                    }
                    if (null != classview._hyotei) {
                        final String v = (String) classview._hyotei._gradeHyoteis.get(gi);
                        
                        if (null != v) {
                            final String value = "11".equals(v) ? "A" : "22".equals(v) ? "B"  : "33".equals(v) ? "C" : null;
                            svf.VrsOutn("RATE4", line, value); // 評定
                            hasData = true;
                        }
                    }
                    
                    // データがあるなら改行
                    if (hasData) {
                        svf.VrsOutn("GRADE2", line, g + "年"); // 学年
                        svf.VrsOutn("SUBJECT4", line, classview._classname);
                        svf.VrsOut("CLASSGRP", classview._classcd); // 教科コード
                        line += 1;
                    }
                }
            }
        }

        /**
         * ２．学習の記録 必修教科出力
         * @param svf
         * @param classViewHisshu
         */
        private void printClassHisshu(final Vrw32alp svf, final List classViewHisshu) {
            final int max = 19;
            // 観点名
            final String[] viewname = {"ア", "イ", "ウ", "エ", "オ", "カ", "キ", "ク", "ケ", "コ", "サ", "シ", "ス", "セ", "ソ"};
            int record = 0;
            for (final Iterator it = classViewHisshu.iterator(); it.hasNext();) {
                final ClassView classview = (ClassView) it.next();
                if (classview._notPrint || !classview.hasData()) {
                    continue;
                }
                final String sclassname = null == classview._classname ? "" : classview._classname;
                final int viewlen = Math.max(classview._views.size(), sclassname.length()) + (Math.max(classview._views.size(), sclassname.length()) % 2 == 1 ? 1 : 0);
                
                for (int line = 0; line < viewlen; line++) {
                    svf.VrsOut("CLASSGRP", classview._classcd); // 教科コード
                    svf.VrsOut("SUBJECT" + (line % 2 + 1), StringUtils.center(sclassname, viewlen).substring(line)); // 教科名

                    if (null != classview._hyotei) {
                        for (final Iterator vlit = classview._hyotei._gradeHyoteis.keySet().iterator(); vlit.hasNext();) {
                            final Integer gi = (Integer) vlit.next();
                            final int g  = gi.intValue();
                            final String value = (String) classview._hyotei._gradeHyoteis.get(gi);
                            svf.VrsOut("GRADE1_" + g + "_2", g + "年"); // 学年
                            svf.VrsOut("RATE" + g, value); // 評定
                        }
                    }

                    if (line < classview._views.size()) {
                        svf.VrsOut("VIEWNAME" + (line % 2 + 1), viewname[line]); // 観点名
                        final View view = (View) classview._views.get(line);
                        for (final Iterator vit = view._gradeStatus.keySet().iterator(); vit.hasNext();) {
                            final Integer gi = (Integer) vit.next();
                            final int g = gi.intValue();
                            final String status = (String) view._gradeStatus.get(gi);
                            svf.VrsOut("GRADE1_" + g + "_1", g + "年"); // 学年
                            if ("A".equals(status)) {
                                svf.VrsOut("VIEW" + g + "_" + (line % 2 + 1), "○"); // 観点
                              }
                        }
                    }
                    if (line % 2 == 1) {
                        svf.VrEndRecord();
                        record += 1;
                    }
                }
            }
            
            for (int i = record; i < max; i++) {
                svf.VrsOut("CLASSGRP", "A"); // 教科コード
                svf.VrEndRecord();
            }
        }

        /**
         * ３．総合的な学習の時間の記録
         */
        private void printSyoken(final Vrw32alp svf, final Student student) {
            for (final Iterator it = student._hexamentremarkDatMap.keySet().iterator(); it.hasNext();) {
                
                final Integer gi = (Integer) it.next();
                final int g = gi.intValue();
                final HexamentremarkDat hexamentremark = (HexamentremarkDat) student._hexamentremarkDatMap.get(gi);

                svf.VrsOutn("GRADE3", g, gi.toString() + "年"); // 評価
                final List list = retDividString(hexamentremark._totalstudyval, 62, 4);
                for (int i = 0; i < list.size(); i++) {
                    final String data = (String) list.get(i);
                    svf.VrsOutn("ACTION1" + (i + 1), g, data);
                }
            }
        }
        

        /**
         * ４．特別活動等の記録
         */
        private void printSpecialAct(final Vrw32alp svf, final Student student) {
            for (final Iterator it = student._hexamentremarkDatMap.keySet().iterator(); it.hasNext();) {
                
                final Integer gi = (Integer) it.next();
                final int g = gi.intValue();
                final HexamentremarkDat hexamentremark = (HexamentremarkDat) student._hexamentremarkDatMap.get(gi);

                svf.VrsOut("GRADE3_" + g, gi.toString() + "年");
                
                // 学級活動
                printSpecialActDetail(svf, g, 1, hexamentremark._calssact);
                // 生徒会部活 
                printSpecialActDetail(svf, g, 2, hexamentremark._studentact);
                // 学校行事
                printSpecialActDetail(svf, g, 3, hexamentremark._schoolevent);
                // その他の活動
                printSpecialActDetail(svf, g, 4, hexamentremark._clubact);
            }
        }

        private void printSpecialActDetail(final Vrw32alp svf, final int g, final int j, final String s) {
            final List list = retDividString(s, 16, 4);
            for (int i = 0; i < list.size(); i++) {
                final String data = (String) list.get(i);
                svf.VrsOutn("field7_" + g + "_" + (i + 1), j, data);
            }
        }

        /**
         * ５．行動の記録・特別活動の記録
         */
        private void printKodo(final Vrw32alp svf, final Student student) {
            for (final Iterator it = student._behaviorList.iterator(); it.hasNext();) {
                final BehaviorDat bd = (BehaviorDat) it.next();
                final int g = bd._annual.intValue();
                svf.VrsOut("GRADE4_1_" + g, g + "年");
                svf.VrsOut("GRADE4_2_" + g, g + "年");
                if ("1".equals(bd._record)) {
                    if ("1".equals(bd._div)) {
                        final int code = Integer.parseInt(bd._code);
                        final int line =  (code > 5) ? code - 5 : code;
                        final int col =  (code > 5) ? 2 : 1;
                        svf.VrsOutn("ACTION" + col + "_" + g, line, "○"); // 行動の記録
                    }
                }
            }
        }

        /**
         * ６．出欠の記録
         */
        private void printShukketsu(final Vrw32alp svf, final Student student) {
            for (final Iterator it = student._attendanceMap.keySet().iterator(); it.hasNext();) {
                final Integer gi = (Integer) it.next();
                final int g = gi.intValue();
                final Attendance a = (Attendance) student._attendanceMap.get(gi);
                svf.VrsOutn("GRADE5", g, gi.toString() + "年");
                svf.VrsOutn("ABSENCE", g, a._absent); // 欠席
                //「５．行動の記録」の学年
                svf.VrsOut("GRADE4_1_" + g, g + "年");
                svf.VrsOut("GRADE4_2_" + g, g + "年");
            }

            // 主な欠席理由
            for (final Iterator it = student._hexamentremarkDatMap.keySet().iterator(); it.hasNext();) {
                final Integer gi = (Integer) it.next();
                final int g = gi.intValue();
                final HexamentremarkDat hexamentremark = (HexamentremarkDat) student._hexamentremarkDatMap.get(gi);
                
                svf.VrsOutn("GRADE5", g, gi.toString() + "年");
                
                final int keta = getMS932ByteLength(hexamentremark._attendrecRemark);
                svf.VrsOutn("ABSENCE_REASON" + (keta > 24 ? "2" : "1"), g, hexamentremark._attendrecRemark);
            }
        }
        

        /**
         * ７．総合的な学習の時間の記録・総合所見
         */
        private void printSogo(final Vrw32alp svf, final Student student, final Param param) {

            final List list = retDividString(student._hexamentremarkHDat._remark, param._sogoKeta, param._sogoGyo);
            for (int i = 0; i < list.size(); i++) {
                svf.VrsOut("field8_1_" + (i + 1), (String) list.get(i)); // 総合所見
            }
        }
        
        /**
        *
        *  文字列を改行マークおよび文字数で区切って返す処理
        *
        */
       public List retDividString(final String src, final int dividlen, final int dividnum) {
           if (src == null) {
               return Collections.EMPTY_LIST;
           }
           final List newlines = new ArrayList();
           newlines.add("\r\n");
           newlines.add("\r");
           newlines.add("\n");
           final List rtn = new ArrayList(dividnum);         //編集後文字列を格納する配列
           StringBuffer temp = new StringBuffer();                     //SVF出力用
           int clen = 0;                                                //１回分の文字列の長さ
           final String separated = StringUtils.replace(src, (String) newlines.get(0), (String) newlines.get(1));
           for (int j = 0; j < separated.length(); j++) {
               final String chr = separated.substring(j, j + 1);             //1文字を取り出す
               if (newlines.contains(chr)) {       //改行マークがある場合、強制的に次行へ
                   rtn.add(temp.toString());            //１行文字列
                   clen = 0;
                   temp = new StringBuffer();
               } else {
                   final int keta = getMS932ByteLength(chr);
                   if ((clen + keta) > dividlen) {
                       rtn.add(temp.toString());            //１行文字列
                       clen = 0;
                       temp = new StringBuffer();
                   }
                   temp.append(chr);
                   clen = clen + keta;
               }
           }
           if (0 < clen) {
               rtn.add(temp.toString());
           }
           return (rtn.size() <= dividnum) ? rtn : rtn.subList(0, dividnum + 1);
       }
    }

    private static class Param {
        private static final KNJ_SchoolinfoSql schooinfoSql = new KNJ_SchoolinfoSql("10000");
        
        final String _year;
        final String _gakki;
        final String _gradehrclass;
        final String[] _categorySelected;
        final String _grade;
        final String _date;
        final Map _gradeCdMap;
        final String _d016Namespare1;
        final String _prgid;
        final String _z010;
//        final boolean _isSeireki;
        
        final KNJDefineCode definecode = new KNJDefineCode(); // 各学校における定数等設定
        String _schoolname = null;
        String _principalname = null;
        String _schoolMstGraduateDate = null;
        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final String _useAddrField2;

        final boolean _isNewForm;
        final int _sogoKeta;
        final int _sogoGyo;

        Param(final DB2UDB db2, final HttpServletRequest request) {

            _year = request.getParameter("YEAR"); // 年度
            _gakki = request.getParameter("GAKKI"); // 学期
            _gradehrclass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");
            _grade = null != _gradehrclass && 2 <= _gradehrclass.length() ? _gradehrclass.substring(0, 2) : null;
            _date = request.getParameter("DATE").replace('/', '-');

            _gradeCdMap = getGradeCdMap(db2);
            _d016Namespare1 = getNameMst(db2, _year, "D016", "01", "NAMESPARE1");
            _prgid = request.getParameter("PRGID");
            _z010 = getZ010(db2);
//            _isSeireki = "2".equals(getNameMst(db2, _year, "Z012", "00", "NAME1"));

            definecode.setSchoolCode(db2, _year);
            loadSchool(db2);
            _schoolMstGraduateDate = getSchoolMstGraduateDate(db2, _year);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useAddrField2 = request.getParameter("useAddrField2");
            _isNewForm = Integer.parseInt(_year) >= 2014; // 2014年度以降は新フォーム
            if (_isNewForm) {
                _sogoKeta = 116;
                _sogoGyo = 8;
            } else {
                _sogoKeta = 62;
                _sogoGyo = 12;
            }
        }

        private String loadCertifSchool(final DB2UDB db2, final String year, final String field) {
            String rtn = null;

            final String certifKindCd = "108";
            final String sql = "SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '" + certifKindCd + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (final SQLException e) {
                log.error("証明書学校データ取得エラー:" + sql, e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private void loadSchool(final DB2UDB db2) {
            
            _principalname = loadCertifSchool(db2, _year, "PRINCIPAL_NAME");

            _schoolname = null;
            if (isJuniorHighSchool(db2)) {
                _schoolname = loadCertifSchool(db2, _year, "SCHOOL_NAME");
                log.debug("CERTIF_SCHOOL_DAT の学校名称=[" + _schoolname + "]");
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int p = 0;
                ps = db2.prepareStatement(schooinfoSql.pre_sql());
                ps.setString(++p, _year);
                ps.setString(++p, _year);
                rs = ps.executeQuery();

                if (rs.next() && StringUtils.isEmpty(_schoolname)) {
                    _schoolname =  rs.getString("SCHOOLNAME1");
                }
            } catch (final Exception e) {
                log.error("printSvfDetail_1 error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * 中高一貫か?
         * 
         * @param db2 DB2UDB
         * @return 中高一貫ならtrue
         */
        private boolean isJuniorHighSchool(final DB2UDB db2) {
            boolean isJuniorHighSchool = false;
            isJuniorHighSchool = true;
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement("SELECT NAMESPARE2,NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2 = '00'");
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    if (rs.getString("NAMESPARE2") != null) {
//                        isJuniorHighSchool = true;
//                    }
//                }
//            } catch (SQLException e) {
//                log.error("SQLException", e);
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
            return isJuniorHighSchool;
        }

        private String getZ010(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("exception! ", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return null;
        }

        public int getGradeCd(final String year, final String grade) {
            final String gradeCd = (String) _gradeCdMap.get(year + grade);
            int n = -1;
            try {
                n = Integer.parseInt(gradeCd);
            } catch (Exception e) {
                log.error("SCHREG_REGD_GDAT.GRADE_CD IS NOT NUMBER. value = '" + gradeCd + "'");
            }
            return n;
        }

        private Map getGradeCdMap(final DB2UDB db2) {
            final Map gdatMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHOOL_KIND = 'J' ");
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String grade = rs.getString("GRADE");
                    gdatMap.put(year + grade, rs.getString("GRADE_CD"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return gdatMap;
        }
        
        private String getNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    " + field + " ");
                sql.append(" FROM NAME_MST T1 ");
                sql.append("    INNER JOIN NAME_YDAT T2 ON T2.YEAR = '" + year + "' AND T2.NAMECD1 = T1.NAMECD1 AND T2.NAMECD2 = T1.NAMECD2 ");
                sql.append(" WHERE ");
                sql.append("    T1.NAMECD1 = '" + namecd1 + "' ");
                sql.append("    AND T1.NAMECD2 = '" + namecd2 + "' ");
                sql.append("   ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
        
        private String getSchoolMstGraduateDate(final DB2UDB db2, final String year) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("   GRADUATE_DATE ");
                sql.append(" FROM SCHOOL_MST T1 ");
                sql.append(" WHERE T1.YEAR = '" + year + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("GRADUATE_DATE")) {
                    final Calendar cal = Calendar.getInstance();
                    cal.setTime(java.sql.Date.valueOf(rs.getString("GRADUATE_DATE")));
                    cal.set(Calendar.DATE, 1);
                    cal.add(Calendar.MONTH, 1);
                    cal.add(Calendar.DATE, -1);
                    rtn = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
    }
}

// eof

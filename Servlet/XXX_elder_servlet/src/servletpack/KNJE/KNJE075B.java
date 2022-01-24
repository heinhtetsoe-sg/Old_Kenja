/*
 * $Id$
 *
 * 作成日: 2019/10/01
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJA.detail.KNJ_AddressRecSql;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

public class KNJE075B {
    private static final KNJ_PersonalinfoSql personalinfoSql = new KNJ_PersonalinfoSql();
    private static final KNJ_AddressRecSql addressRecSql = new KNJ_AddressRecSql();

    private static final Log log = LogFactory.getLog(KNJE075B.class);
    private static final String CONST_SELALL = "99999";
    private static final String FORM_NAME = "KNJE075B.frm";
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List selectList = Arrays.asList(_param._categorySelected); // 出力対象学籍番号を格納
        for (final Iterator t = selectList.iterator(); t.hasNext();) { // --学籍番号の繰り返し
            final String schregno = (String) (t.next());
            final Student student = new Student(schregno);
            student.load(db2, _param);

            final Form knj131 = new Form();
            if (knj131.printSvf(db2, svf, _param, student)) {
                _hasData = true;
            }
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }


    private static class Student {
        final String _schregno;
        String _name;
        String _kana;
        String _sexName;
        String _birthday;
        String _entdate;
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
                final String sql = personalinfoSql.sql_info_reg("1111111000");
                ps = db2.prepareStatement(sql);
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
                    _entdate = rs.getString("ENT_DATE");
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
        final String _late;
        final String _early;

        public Attendance(String lesson, String suspendMourning, String abroad, String requirePresent, String present, String absent, String late, String early) {
            _lesson = lesson;
            _suspendMourning = suspendMourning;
            _abroad = abroad;
            _requirePresent = requirePresent;
            _present = present;
            _absent = absent;
            _late = late;
            _early = early;
        }

        public static Map load(final DB2UDB db2, Param param, final String schregno) {
            final Map dataMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int p = 0;
                final String sql = Attendance.sql(param);
                ps = db2.prepareStatement(sql);
                ps.setString(++p, schregno); // 学籍番号
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
                    final String late = rs.getString("LATE"); // 遅刻
                    final String early = rs.getString("EARLY"); // 早退
                    final Attendance a = new Attendance(lesson, suspendMourning, abroad, requirePresent, present, absent, late, early);
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

        private static String sql(Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SEMES AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            stb.append("         YEAR, ");
            stb.append("         SUM(LATE) AS LATE, ");
            stb.append("         SUM(EARLY) AS EARLY ");
            stb.append("     FROM ");
            stb.append("         ATTEND_SEMES_DAT ");
            stb.append("     WHERE ");
            stb.append("         SCHREGNO = ? ");
            stb.append("     GROUP BY ");
            stb.append("         SCHREGNO, ");
            stb.append("         YEAR ");
            stb.append("   ) ");
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
            stb.append("  END AS ABSENT, ");
            stb.append("  VALUE(SEMES.LATE, 0) AS LATE, ");
            stb.append("  VALUE(SEMES.EARLY, 0) AS EARLY ");
            stb.append("FROM    SCHREG_ATTENDREC_DAT T1 ");
            stb.append("LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR AND S1.SCHOOLCD = " + param._schoolcd + " AND SCHOOL_KIND = 'J' ");
            stb.append("LEFT JOIN SEMES ON SEMES.SCHREGNO = T1.SCHREGNO AND SEMES.YEAR = T1.YEAR ");

            stb.append("WHERE ");
            stb.append("  T1.YEAR <= ? ");
            stb.append("  AND T1.SCHREGNO = ? ");
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
            stb.append("   SELECT ");
            stb.append("       SPECIALACTREC, ");
            stb.append("       CLUBACT ");
            stb.append("   FROM ");
            stb.append("       HEXAM_ENTREMARK_DAT ");
            stb.append("   WHERE ");
            stb.append("       YEAR <= ? ");
            stb.append("       AND SCHREGNO = ? ");
            return stb.toString();
        }
    }

    private static class HexamentremarkDat {

        final Integer _annual;
        final String _attendrecRemark;
        final String _specialActRec;
        final String _totalstudyval;
        final String _calssact;
        final String _studentact;
        final String _clubact;
        final String _schoolevent;

        public HexamentremarkDat(final Integer annual, final String attendrecRemark, final String specialActRec, final String totalstudyval, final String calssact, final String studentact, final String clubact, final String schoolevent) {
            _annual = annual;
            _attendrecRemark = attendrecRemark;
            _specialActRec = specialActRec;
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
                final String sql = sql();
                log.fatal("ATTNERECE_REMARK:" + sql);
                ps = db2.prepareStatement(sql);
                ps.setString(++p, param._year); // 年度
                ps.setString(++p, schregno); // 学籍番号
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Integer annual = Integer.valueOf(rs.getString("ANNUAL"));
                    final String attendrecRemark = rs.getString("ATTENDREC_REMARK");
                    final String specialActRec = rs.getString("SPECIALACTREC");
                    final String totalstudyval = rs.getString("TOTALSTUDYVAL");
                    final String calssact = rs.getString("CALSSACT");
                    final String studentact = rs.getString("STUDENTACT");
                    final String clubact = rs.getString("CLUBACT");
                    final String schoolevent = rs.getString("SCHOOLEVENT");

                    final HexamentremarkDat hd = new HexamentremarkDat(annual, attendrecRemark, specialActRec, totalstudyval, calssact, studentact, clubact, schoolevent);
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
            stb.append("  ANNUAL, ATTENDREC_REMARK, SPECIALACTREC, TOTALSTUDYVAL, CALSSACT, STUDENTACT, CLUBACT, SCHOOLEVENT ");
            stb.append("FROM    HEXAM_ENTREMARK_DAT T1 ");
            stb.append("WHERE   YEAR <= ? ");
            stb.append("  AND SCHREGNO = ? ");
            return stb.toString();
        }
    }

    private static class HexamentremarkHDat {

        final String _remark;
        final String _remark2;

        public HexamentremarkHDat(final String remark, final String remark2) {
            _remark = remark;
            _remark2 = remark2;
        }

        public static HexamentremarkHDat load(final DB2UDB db2, final Param param, final String schregno) {
            HexamentremarkHDat hexamhdat = new HexamentremarkHDat("", "");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int p = 0;
                ps = db2.prepareStatement(sql());
                ps.setString(++p, schregno); // 学籍番号
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String remark = rs.getString("REMARK");
                    final String remark2 = rs.getString("REMARK2");

                    hexamhdat = new HexamentremarkHDat(remark, remark2);
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
            stb.append("  REMARK, ");
            stb.append("  REMARK2 ");
            stb.append("FROM ");
            stb.append("  HEXAM_ENTREMARK_HDAT ");
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

    private static class Form {

        public boolean printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student) {
            boolean _hasData = false;
            final String form = FORM_NAME;
            svf.VrSetForm(form, 1);

            printHeader(svf, student, param);
            printEiken(svf, student, param);
            printSpecialAct(svf, student);
            printShukketsu(svf, student);
            printSyoken(svf, student, param);
            printHyotei(db2, svf, student, param);
            printFooter(svf, student, param);

            _hasData = true;

            svf.VrEndPage();
            return _hasData;
        }

        /**
         * ヘッダ
         */
        private void printHeader(final Vrw32alp svf, final Student student, final Param param) {
            // 年度出力
            svf.VrsOut("NENDO", param._year + "年度");
            // 学校名称の出力
            final String schoolName = (String)param._certifSchoolMap.get("SCHOOL_NAME");
            svf.VrsOut("school_name_1", schoolName);

            svf.VrsOut("KANA", student._kana);

            final int n = getMS932ByteLength(student._name);
            if (n <= 36) {
                svf.VrsOut("NAME" + "1", student._name);
            } else {
                svf.VrsOut("NAME" + "2", student._name.substring(0, 18));
                svf.VrsOut("NAME" + "3", student._name.substring(18));
            }
            svf.VrsOut("KANA", student._kana);

            String entdate = "";
            if (student._entdate != null) {
                final int tmpYear  = Integer.parseInt(student._entdate.substring(0, 4));
                final int tmpMonth = Integer.parseInt(student._entdate.substring(5, 7));
                final int tmpDay   = Integer.parseInt(student._entdate.substring(8, 10));
                entdate = String.format("%4d年%2d月%2d日", tmpYear, tmpMonth, tmpDay);
            }
            svf.VrsOut("ENT_DATE", entdate);
            svf.VrsOut("ENTNAME", "入学");
            String birthdate = "";
            if (student._entdate != null) {
                final int tmpYear  = Integer.parseInt(student._birthday.substring(0, 4));
                final int tmpMonth = Integer.parseInt(student._birthday.substring(5, 7));
                final int tmpDay   = Integer.parseInt(student._birthday.substring(8, 10));
                birthdate = String.format("%4d年%2d月%2d日", tmpYear, tmpMonth, tmpDay);
            }
            svf.VrsOut("BIRTHDAY", birthdate + "生");

            final String tmpGrddate = (null == student._grddiv || "4".equals(student._grddiv)) ? param._schoolMstGraduateDate : student._grddate;
            String grddate = "";
            String grdDiv = "";
            if (!"".equals(tmpGrddate)) {
                final int tmpYear  = Integer.parseInt(tmpGrddate.substring(0, 4));
                final int tmpMonth = Integer.parseInt(tmpGrddate.substring(5, 7));
                if ("1".equals(student._grddiv)) {
                  grdDiv = "卒業";
                } else if (null == student._grddiv || "4".equals(student._grddiv)) {
                  grdDiv = "卒業見込";
                }

                grddate = String.format("%4d年%2d月", tmpYear, tmpMonth);
            }
            svf.VrsOut("TRANSFER_DATE", grddate);
            svf.VrsOut("GRADNAME", grdDiv);
        }

        /**
         * 学習評定の記録
         */
        private boolean printHyotei(final DB2UDB db2, final Vrw32alp svf, final Student student, final Param param) {

            int col = 1;
            final List hyoteiList = Hyotei.load(db2, param, student._schregno);
            for (final Iterator it = hyoteiList.iterator(); it.hasNext();) {
                Hyotei hyotei = (Hyotei)it.next();
                final int n = getMS932ByteLength(hyotei._className);
                if (n <= 4) {
                    svf.VrsOutn("SUBCLASS_NAME1", col, hyotei._className);
                } else {
                    svf.VrsOutn("SUBCLASS_NAME2_1", col, hyotei._className);
                }
                for (final Iterator it2 = hyotei._gradeHyoteis.keySet().iterator(); it2.hasNext();) {
                    final Integer g = (Integer)it2.next();
                    final String value = (String)hyotei._gradeHyoteis.get(g);
                    svf.VrsOutn("RATE" + g, col, value);
                }

                col++;
            }

            return true;
        }

        /**
         * 特別活動の記録
         */
        private void printSpecialAct(final Vrw32alp svf, final Student student) {
            for (final Iterator it = student._hexamentremarkDatMap.keySet().iterator(); it.hasNext();) {

                final Integer gi = (Integer) it.next();
                final int g = gi.intValue();
                final HexamentremarkDat hexamentremark = (HexamentremarkDat) student._hexamentremarkDatMap.get(gi);

                //生徒会・学校幼児・HR活動
                final List list = retDividString(hexamentremark._specialActRec, 16, 4);
                for (int i = 0; i < list.size(); i++) {
                    final String data = (String) list.get(i);
                    svf.VrsOutn("field7_1_" + (i + 1), g, data);
                }

                //クラブ活動
                final List list2 = retDividString(hexamentremark._clubact, 16, 4);
                for (int i = 0; i < list2.size(); i++) {
                    final String data = (String) list2.get(i);
                    svf.VrsOutn("field7_2_" + (i + 1), g, data);
                }

                //特別活動全体の総評(3年次のみ)
                if (g == 3) {
                    //生徒会活動
                    if ("1".equals(hexamentremark._studentact)) {
                        svf.VrsOut("field7_3_1", "〇");
                    }
                    //学級活動
                    if ("1".equals(hexamentremark._calssact)) {
                        svf.VrsOut("field7_3_2", "〇");
                    }
                    //学校行事
                    if ("1".equals(hexamentremark._schoolevent)) {
                        svf.VrsOut("field7_3_3", "〇");
                    }
                }

            }
        }

        /**
         * フッタ
         */
        private void printFooter(final Vrw32alp svf, final Student student, final Param param) {
            //出力日付
            String ctrlDate = "";
            if (param._date != null && !"".equals(param._date)) {
                final int tmpYear  = Integer.parseInt(param._date.substring(0, 4));
                final int tmpMonth = Integer.parseInt(param._date.substring(5, 7));
                final int tmpDay   = Integer.parseInt(param._date.substring(8, 10));

                ctrlDate = String.format("%4d年%2d月%2d日", tmpYear, tmpMonth, tmpDay);
                ctrlDate = StringUtils.upperCase(ctrlDate);
            }
            svf.VrsOut("DATE", ctrlDate);

            // 学校名称の出力
            final String schoolName = (String)param._certifSchoolMap.get("SCHOOL_NAME");
            svf.VrsOut("school_name_2", schoolName);

            // 学校所在地
            final String schoolLoc = (String)param._certifSchoolMap.get("SCHOOL_LOCATION");
            svf.VrsOut("SCHOOL_ADDRESS", schoolLoc);

            // 学校長名
            final String principalName = (String)param._certifSchoolMap.get("PRINCIPAL_NAME");
            printFooterName(svf, "STAFFNAME", principalName);

            // 記載帰任者
            final String jobName = (String)param._responsibleMap.get("JOBNAME");
            final String staffName = (String)param._responsibleMap.get("STAFFNAME");
            svf.VrsOut("JOB_NAME", jobName);
            printFooterName(svf, "RESPONSIBLE_NAME", staffName);

        }

        private void printFooterName(final Vrw32alp svf, String fieldName, String name) {
            final int n = getMS932ByteLength(name);
            int suffixNo = 0;
            if (n <= 20) {
                suffixNo = 1;
            } else if (n <= 30) {
                suffixNo = 2;
            } else {
                suffixNo = 3;
            }
            svf.VrsOut(fieldName + "_" + suffixNo, name);
        }

        /**
         * 出欠の記録
         */
        private void printShukketsu(final Vrw32alp svf, final Student student) {
            for (final Iterator it = student._attendanceMap.keySet().iterator(); it.hasNext();) {
                final Integer gi = (Integer) it.next();
                final int g = gi.intValue();
                final Attendance a = (Attendance) student._attendanceMap.get(gi);
                svf.VrsOutn("ABSENCE", g, a._absent); // 欠席
                //「５．行動の記録」の学年
                final int late = Integer.parseInt(StringUtils.defaultIfEmpty(a._late, "0"));
                final int early = Integer.parseInt(StringUtils.defaultIfEmpty(a._early, "0"));
                final String lateEarly = String.valueOf(late + early);
                svf.VrsOutn("LATE", g, lateEarly);

                // 主な欠席理由
                final HexamentremarkDat hexamentremark = (HexamentremarkDat) student._hexamentremarkDatMap.get(gi);
                if (hexamentremark != null && hexamentremark._attendrecRemark != null) {
                    List<String> tokenList = KNJ_EditKinsoku.getTokenList(hexamentremark._attendrecRemark, 8);
                    int gyo = 1;
                    for (final String token : tokenList) {
                        svf.VrsOutn("ABSENCE_REASON" + gyo, g, token);
                        gyo++;
                    }
                }
            }
        }

        /**
         * 総合所見
         */
        private void printSyoken(final Vrw32alp svf, final Student student, final Param param) {

            final List list = retDividString(student._hexamentremarkHDat._remark, param._syokenKeta, param._syokenGyo);
            for (int i = 0; i < list.size(); i++) {
                svf.VrsOutn("field8_1_1", i + 1, (String) list.get(i));
            }
        }

        /**
         * 文部科学省実用英語検定試験等
         */
        private void printEiken(final Vrw32alp svf, final Student student, final Param param) {

            final List list = retDividString(student._hexamentremarkHDat._remark2, param._eikenKeta, param._eikenGyo);
            for (int i = 0; i < list.size(); i++) {
                svf.VrsOutn("ENGLISH_TEST", i + 1, (String) list.get(i));
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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70851 $");
        KNJServletUtils.debugParam(request, log);
        return param;
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
        final String _schoolcd;
        final String _responsibleStaffCd;

        final KNJDefineCode definecode = new KNJDefineCode(); // 各学校における定数等設定
        String _principalname = null;
        String _schoolMstGraduateDate = null;
        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final String _useAddrField2;

        final boolean _isNewForm;
        final int _syokenKeta;
        final int _syokenGyo;
        final int _eikenKeta;
        final int _eikenGyo;

        Map _responsibleMap;
        Map _certifSchoolMap;

        Param(final DB2UDB db2, final HttpServletRequest request) {

            _year = request.getParameter("YEAR"); // 年度
            _gakki = request.getParameter("GAKKI"); // 学期
            _gradehrclass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");
            _grade = null != _gradehrclass && 2 <= _gradehrclass.length() ? _gradehrclass.substring(0, 2) : null;
            _date = request.getParameter("DATE");
            _responsibleStaffCd = request.getParameter("SEKI");

            _gradeCdMap = getGradeCdMap(db2);
            _responsibleMap = getResponsibleMap(db2, _year);
            _certifSchoolMap = loadCertifSchoolMap(db2, _year);
            _d016Namespare1 = getNameMst(db2, _year, "D016", "01", "NAMESPARE1");
            _prgid = request.getParameter("PRGID");
            _z010 = getZ010(db2);
//            _isSeireki = "2".equals(getNameMst(db2, _year, "Z012", "00", "NAME1"));

            _schoolcd = request.getParameter("SCHOOLCD"); // 学校コード

            definecode.setSchoolCode(db2, _year);
            _schoolMstGraduateDate = getSchoolMstGraduateDate(db2, _year);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useAddrField2 = request.getParameter("useAddrField2");
            _isNewForm = Integer.parseInt(_year) >= 2014; // 2014年度以降は新フォーム
            _syokenKeta = 32;
            _syokenGyo  = 30;
            _eikenKeta  = 30;
            _eikenGyo   = 10;
        }

        private Map loadCertifSchoolMap(final DB2UDB db2, final String year) {
            Map<String, String> rtn = new LinkedHashMap();

            final String certifKindCd = "115";
            final String sql = "SELECT SCHOOL_NAME, PRINCIPAL_NAME, REMARK4 AS SCHOOL_LOCATION FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '" + certifKindCd + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn.put("SCHOOL_NAME", StringUtils.defaultIfEmpty(rs.getString("SCHOOL_NAME"), ""));
                    rtn.put("PRINCIPAL_NAME", StringUtils.defaultIfEmpty(rs.getString("PRINCIPAL_NAME"), ""));
                    rtn.put("SCHOOL_LOCATION", StringUtils.defaultIfEmpty(rs.getString("SCHOOL_LOCATION"), ""));
                }
            } catch (final SQLException e) {
                log.error("証明書学校データ取得エラー:" + sql, e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
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
                sql.append(" WHERE T1.YEAR = '" + year + "' AND ");
                sql.append(" 	   T1.SCHOOLCD = '" + _schoolcd + "' AND ");
                sql.append(" 	   T1.SCHOOL_KIND = 'J' ");
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

        //記載責任者
        private Map getResponsibleMap(final DB2UDB db2, final String year) {
            Map<String, String> rtn = new LinkedHashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append("   SELECT ");
                stb.append("       JOB.JOBNAME, ");
                stb.append("       STF.STAFFNAME ");
                stb.append("   FROM ");
                stb.append("       V_STAFF_MST STF ");
                stb.append("       LEFT JOIN JOB_MST JOB ");
                stb.append("           ON JOB.JOBCD = STF.JOBCD ");
                stb.append("   WHERE ");
                stb.append("       STF.YEAR = '" + year + "' AND ");
                stb.append("       STF.STAFFCD = '" + _responsibleStaffCd + "' ");
                log.fatal(stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn.put("JOBNAME", StringUtils.defaultIfEmpty(rs.getString("JOBNAME"), ""));
                    rtn.put("STAFFNAME", StringUtils.defaultIfEmpty(rs.getString("STAFFNAME"), ""));
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

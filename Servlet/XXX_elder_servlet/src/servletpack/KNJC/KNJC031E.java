//kanji=漢字
/*
 * $Id: d26773a21423c1294c84e3e2cffe970d1d103ee0 $
 *
 * 作成日: 2010/05/21 22:22:22 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJC031E {

    private static final Log log = LogFactory.getLog(KNJC031E.class);

    private boolean _hasData;

    private Param _param;

    private static final String KEY_SUM = "_SUM";
    private static final String KEY_CD = "CD";
    private static final String KEY_ITEM = "ITEM";
    private static final String KEY_CHECK_NAME_MST = "CHEKCK_NAME_MST"; // 表示の際名称マスタをチェックするか true: チェックして表示 true以外: チェックせずに固定表示

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List attendlist = Attend.getAttendList(db2, _param);

        svf.VrSetForm("KNJC031E.frm", 4);

        printHeader(svf, attendlist);

        final List attendNameList = Attend.getAttendNameList(_param);

        for (final Iterator it = attendNameList.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            final String item = (String) map.get(KEY_ITEM);
            final String cd = (String) map.get(KEY_CD);
            printSvfRecord(svf, item, cd, attendlist, attendNameList);
            svf.VrEndRecord();
        }

        final int subformWidth = 3296 - 910;
        int width = 0;
        for (final Iterator it = attendNameList.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            final String cd = (String) map.get(KEY_CD);
            final int colwidth;
            if (cd.endsWith(KEY_SUM)) {
                colwidth = 98;
            } else {
                colwidth = 100;
            }
            // log.fatal(" cd = " + cd + " width = " + width + " / " + subformWidth);
            if (width + colwidth > subformWidth) {
                width = 0;
            }
            width += colwidth;
        }
        log.fatal(" width = " + width);
        int bikoMaxCol = 0; // 備考の行数
        for (int line = 1; line <= attendlist.size(); line++) {
            final Attend att = (Attend) attendlist.get(line - 1);
            att.setRemarkList(4);
            bikoMaxCol = Math.max(att._remarkList.size(), bikoMaxCol);
        }
        if (bikoMaxCol > (subformWidth - width) / 100) {
            // 残りの行で収まらなければ改ページして表示
            svf.VrSetForm("KNJC031E.frm", 4);
        }
        // 備考
        for (int i = 0; i < bikoMaxCol; i++) {

            printHeader(svf, attendlist);

            svf.VrsOut("GRP" + ((i == bikoMaxCol - 1) ? "3" : "2"), "G");
            if (i == bikoMaxCol / 2) {
                svf.VrsOut("ITEM3_" + ((i == bikoMaxCol - 1) ? "2" : "1"), "備考");
            }
            for (int line = 1; line <= attendlist.size(); line++) {
                final Attend att = (Attend) attendlist.get(line - 1);
                svf.VrsOutn("REMARK_" + ((i == bikoMaxCol - 1) ? "2" : "1"), line, att.remarkKeta(i));
            }
            svf.VrEndRecord();
        }
        _hasData = true;
    }

    private void printHeader(final Vrw32alp svf, final List attendlist) {
        svf.VrsOut("HR_CLASS", _param._hrName);
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
        svf.VrsOut("SEMESTER", _param._semesterName);

        for (int line = 1; line <= attendlist.size(); line++) {
            final Attend att = (Attend) attendlist.get(line - 1);
            svf.VrsOutn("ATTENDNO", line, att._attendno);
            if ("1".equals(_param._use_SchregNo_hyoji)) {
                svf.VrsOutn("SCHREGNO", line, att._schregno);
                svf.VrsOutn("NAME2", line, att._name);
            } else  {
                svf.VrsOutn("NAME", line, att._name);
            }
        }
    }

    private void printSvfRecord(final Vrw32alp svf, final String item0, String cd, final List attendlist, final List attendNameList) {
        final String item;
//        if (Attend.C001_2.equals(cd) || (Attend.C001_2 + KEY_SUM).equals(cd)) {
//            final String repl = null != _param._nameMst.get(Attend.C001_25) ? "法止" : "出停";
//            item = StringUtils.replace(item0, "%s", repl); // %sの箇所を法止で置き換え
//        } else if (-1 != StringUtils.defaultString(item0, "").indexOf("%s")) {
        if (-1 != StringUtils.defaultString(item0, "").indexOf("%s")) {
            final String cdForName = cd.endsWith(KEY_SUM) ? cd.substring(0, cd.length() - KEY_SUM.length()) : cd;
            final String repl = (String) _param._nameMst.get(cdForName); // %sの箇所を名称マスタの名称1で置き換え
            item = StringUtils.replace(item0, "%s", repl);
        } else {
            item = item0;
        }
        final String nameField;
        final String field;
        if (cd.endsWith(KEY_SUM)) {
            nameField = "ITEM2" + (null != item && item.length() > 5 ? "_2" : "_1");
            field = "ACCUM_LESSON";
            svf.VrsOut("GRP1", "1");
            svf.VrsOut("TOTAL_TITLE", getTitleSubstring(cd, attendNameList));
        } else {
            nameField = "ITEM1" + (null != item && item.length() > 5 ? "_2" : "_1");
            field = "APPOINTED_DAY";
        }
        svf.VrsOut(nameField, item);
        for (int line = 1; line <= attendlist.size(); line++) {
            final Attend att = (Attend) attendlist.get(line - 1);
            svf.VrsOutn(field, line, getAttendZeroHyoji(att.getValue(cd), cd));
        }
    }

    //プロパティ「use_Attend_zero_hyoji」= '1'　または　累積のとき、データの通りにゼロ、NULLを表示
    //それ以外のとき、ゼロは表示しない
    private String getAttendZeroHyoji(final String val, String cd) {
        if ("1".equals(_param._use_Attend_zero_hyoji) || cd.endsWith(KEY_SUM)) return val;
        if ("0".equals(val) || "0.0".equals(val)) return "";
        return val;
    }

    private String getTitleSubstring(final String cd, final List attendNameList) {
        // 後ろに"_SUM"が付くコードのリストを得る
        final List sumCdList = new ArrayList();
        for (final Iterator it = attendNameList.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            final String cd0 = (String) map.get(KEY_CD);
            if (cd0.endsWith(KEY_SUM)) {
                sumCdList.add(cd0);
            }
        }

        final String title = StringUtils.center("累積", sumCdList.size(), '　');
        final int idx = sumCdList.indexOf(cd);
        final String substring = title.substring(idx, idx + 1);
        return substring;
    }

    private static class Attend {
        final String _schregno;
        final String _attendno;
        final String _grade;
        final String _coursecd;
        final String _majorcd;
        final String _schoolKind;
        final String _name;
        final String _appointedDay;
        final String _lesson;
        final String _offdays;
        final String _abroad;
        final String _absent;
        final String _suspend;
        final String _koudome;
        final String _virus;
        final String _mourning;
        final String _classdays2;
        final String _sick;
        final String _notice;
        final String _nonotice;
        final String _classdays3;
        final String _late;
        final String _early;
        final String _detail001;
        final String _detail002;
        final String _detail003;
        final String _detail004;
        final String _detail101;
        final String _sumClassdays;
        final String _sumSuspend;
        final String _sumKoudome;
        final String _sumVirus;
        final String _sumMourning;
        final String _sumClassdays2;
        final String _sumSick;
        final String _sumClassdays3;
        final String _sumLate;
        final String _sumEarly;
        final String _sumDetail001;
        final String _sumDetail002;
        final String _sumDetail003;
        final String _sumDetail004;
        final String _sumDetail101;
        final String _defLesson;
        final String _remark;

        List _remarkList = Collections.EMPTY_LIST;

        Attend(
            final String schregno,
            final String attendno,
            final String grade,
            final String coursecd,
            final String majorcd,
            final String schoolKind,
            final String name,
            final String appointedDay,
            final String lesson,
            final String offdays,
            final String abroad,
            final String absent,
            final String suspend,
            final String koudome,
            final String virus,
            final String mourning,
            final String classdays2,
            final String sick,
            final String notice,
            final String nonotice,
            final String classdays3,
            final String late,
            final String early,
            final String detail001,
            final String detail002,
            final String detail003,
            final String detail004,
            final String detail101,
            final String sumClassdays,
            final String sumSuspend,
            final String sumKoudome,
            final String sumVirus,
            final String sumMourning,
            final String sumClassdays2,
            final String sumSick,
            final String sumClassdays3,
            final String sumLate,
            final String sumEarly,
            final String sumDetail001,
            final String sumDetail002,
            final String sumDetail003,
            final String sumDetail004,
            final String sumDetail101,
            final String defLesson,
            final String remark
        ) {
            _schregno = schregno;
            _attendno = attendno;
            _grade = grade;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _schoolKind = schoolKind;
            _name = name;
            _appointedDay = appointedDay;
            _lesson = lesson;
            _offdays = offdays;
            _abroad = abroad;
            _absent = absent;
            _suspend = suspend;
            _koudome = koudome;
            _virus = virus;
            _mourning = mourning;
            _classdays2 = classdays2;
            _sick = sick;
            _notice = notice;
            _nonotice = nonotice;
            _classdays3 = classdays3;
            _late = late;
            _early = early;
            _detail001 = detail001;
            _detail002 = detail002;
            _detail003 = detail003;
            _detail004 = detail004;
            _detail101 = detail101;
            _sumClassdays = sumClassdays;
            _sumSuspend = sumSuspend;
            _sumKoudome = sumKoudome;
            _sumVirus = sumVirus;
            _sumMourning = sumMourning;
            _sumClassdays2 = sumClassdays2;
            _sumSick = sumSick;
            _sumClassdays3 = sumClassdays3;
            _sumLate = sumLate;
            _sumEarly = sumEarly;
            _sumDetail001 = sumDetail001;
            _sumDetail002 = sumDetail002;
            _sumDetail003 = sumDetail003;
            _sumDetail004 = sumDetail004;
            _sumDetail101 = sumDetail101;
            _defLesson = defLesson;
            _remark = remark;
        }

        /**
         * 備考を表示行ごとに分割してセットする
         * @param ketaPerLine 1行あたりの桁
         */
        public void setRemarkList(final int ketaPerLine) {
            if (null == _remark) {
                return;
            }
            _remarkList = new ArrayList();
            final StringBuffer mojiretuTotal = new StringBuffer();
            int ketaTotal = 0;
            for (int i = 0; i < _remark.length(); i++) {
                final String moji = String.valueOf(_remark.charAt(i));
                try {
                    final int mojinoKeta = moji.getBytes("MS932").length;
                    if (ketaTotal + mojinoKeta > ketaPerLine) {
                        _remarkList.add(mojiretuTotal.toString());
                        mojiretuTotal.delete(0, mojiretuTotal.length());
                        ketaTotal = 0;
                    }
                    mojiretuTotal.append(moji);
                    ketaTotal += mojinoKeta;
                } catch (Exception e) {
                }
            }
            if (mojiretuTotal.length() > 0) {
                _remarkList.add(mojiretuTotal.toString());
            }
            log.info(" remark " + _schregno + " = " + _remarkList);
        }

        public String remarkKeta(final int idx) {
            return _remarkList.size() <= idx ? null : (String) _remarkList.get(idx);
        }

        public static List getAttendList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = selectAttendQuery(param);
                log.fatal(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String attendno = rs.getString("ATTENDNO");
                    final String grade = rs.getString("GRADE");
                    final String coursecd = rs.getString("COURSECD");
                    final String majorcd = rs.getString("MAJORCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String name = rs.getString("NAME");
                    final String appointedDay = rs.getString("APPOINTED_DAY");
                    final String lesson = rs.getString("LESSON");
                    final String offdays = rs.getString("OFFDAYS");
                    final String abroad = rs.getString("ABROAD");
                    final String absent = rs.getString("ABSENT");
                    final String suspend = rs.getString("SUSPEND");
                    final String koudome = "true".equals(param._useKoudome) ? rs.getString("KOUDOME") : null;
                    final String virus = "true".equals(param._useVirus) ? rs.getString("VIRUS") : null;
                    final String mourning = rs.getString("MOURNING");
                    final String classdays2 = rs.getString("CLASSDAYS2");
                    final String sick = rs.getString("SICK");
                    final String notice = rs.getString("NOTICE");
                    final String nonotice = rs.getString("NONOTICE");
                    final String classdays3 = rs.getString("CLASSDAYS3");
                    final String late = rs.getString("LATE");
                    final String early = rs.getString("EARLY");
                    final String detail001 = rs.getString("DETAIL_001");
                    final String detail002 = rs.getString("DETAIL_002");
                    final String detail003 = rs.getString("DETAIL_003");
                    final String detail004 = rs.getString("DETAIL_004");
                    final String detail101 = rs.getString("DETAIL_101");
                    final String sumClassdays = rs.getString("SUM_CLASSDAYS");
                    final String sumSuspend = rs.getString("SUM_SUSPEND");
                    final String sumKoudome = "true".equals(param._useKoudome) ? rs.getString("SUM_KOUDOME") : null;
                    final String sumVirus = "true".equals(param._useVirus) ? rs.getString("SUM_VIRUS") : null;
                    final String sumMourning = rs.getString("SUM_MOURNING");
                    final String sumClassdays2 = rs.getString("SUM_CLASSDAYS2");
                    final String sumSick = rs.getString("SUM_SICK");
                    final String sumClassdays3 = rs.getString("SUM_CLASSDAYS3");
                    final String sumLate = rs.getString("SUM_LATE");
                    final String sumEarly = rs.getString("SUM_EARLY");
                    final String sumDetail001 = rs.getString("SUM_DETAIL_001");
                    final String sumDetail002 = rs.getString("SUM_DETAIL_002");
                    final String sumDetail003 = rs.getString("SUM_DETAIL_003");
                    final String sumDetail004 = rs.getString("SUM_DETAIL_004");
                    final String sumDetail101 = rs.getString("SUM_DETAIL_101");
                    final String defLesson = rs.getString("DEF_LESSON");
                    final String remark = rs.getString("REMARK");
                    final Attend attend = new Attend(schregno, attendno, grade, coursecd, majorcd, schoolKind, name, appointedDay, lesson, offdays, abroad, absent, suspend, koudome, virus, mourning, classdays2, sick, notice, nonotice, classdays3, late, early, detail001, detail002, detail003, detail004, detail101, sumClassdays, sumSuspend, sumKoudome, sumVirus, sumMourning, sumClassdays2, sumSick, sumClassdays3, sumLate, sumEarly, sumDetail001, sumDetail002, sumDetail003, sumDetail004, sumDetail101, defLesson, remark);
                    list.add(attend);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }


        /* 出欠月別累積データ・出欠累積データ・出欠科目別累積データ・学籍異動データ */
        private static String selectAttendQuery(final Param param)
        {
            final String[] monthsem = StringUtils.split(param._month, "-");
            //累積期間月を配列にする。2004/08/27 arakaki
            final Map range_month = new HashMap();
            range_month.put("04", "'04'");
            range_month.put("05", "'04','05'");
            range_month.put("06", "'04','05','06'");
            range_month.put("07", "'04','05','06','07'");
            range_month.put("08", "'04','05','06','07','08'");
            range_month.put("09", "'04','05','06','07','08','09'");
            range_month.put("10", "'04','05','06','07','08','09','10'");
            range_month.put("11", "'04','05','06','07','08','09','10','11'");
            range_month.put("12", "'04','05','06','07','08','09','10','11','12'");
            range_month.put("01", "'04','05','06','07','08','09','10','11','12','01'");
            range_month.put("02", "'04','05','06','07','08','09','10','11','12','01','02'");
            range_month.put("03", "'04','05','06','07','08','09','10','11','12','01','02','03'");

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     sd.schregno, ");
            stb.append("     sd.attendno, ");
            stb.append("     sd.GRADE, ");
            stb.append("     sd.COURSECD, ");
            stb.append("     sd.MAJORCD, ");
            stb.append("     SG.SCHOOL_KIND, ");
            stb.append("     sm.NAME, ");
            stb.append("     am.APPOINTED_DAY, ");
            stb.append("     ad.LESSON, ");
            stb.append("     ad.OFFDAYS, ");
            stb.append("     ad.ABROAD, ");
            stb.append("     ad.ABSENT, ");
            stb.append("     ad.suspend, ");
            if ("true".equals(param._useKoudome)) {
                stb.append("     ad.KOUDOME, ");
            }
            if ("true".equals(param._useVirus)) {
                stb.append("     ad.VIRUS, ");
            }
            stb.append("     ad.mourning, ");
            stb.append("     VALUE(ad.LESSON, 0) - VALUE(ad.suspend, 0) - VALUE(ad.mourning, 0) - VALUE(ad.ABROAD, 0) - VALUE(ad.OFFDAYS, 0) ");
            if ("true".equals(param._useKoudome)) {
                stb.append("     - VALUE(ad.KOUDOME, 0) ");
            }
            if ("true".equals(param._useVirus)) {
                stb.append("     - VALUE(ad.VIRUS, 0) ");
            }
            if ("1".equals(param._knjSchoolMst._semOffDays)) {
                stb.append("     + VALUE(ad.OFFDAYS, 0) ");
            }
            stb.append("     AS classdays2, ");
            stb.append("     ad.sick, ");
            stb.append("     ad.NOTICE, ");
            stb.append("     ad.NONOTICE, ");
            stb.append("     VALUE(ad.LESSON, 0) - VALUE(ad.suspend, 0) - VALUE(ad.mourning, 0) - VALUE(ad.ABROAD, 0) - VALUE(ad.OFFDAYS, 0) ");
            if ("true".equals(param._useKoudome)) {
                stb.append("     - VALUE(ad.KOUDOME, 0) ");
            }
            if ("true".equals(param._useVirus)) {
                stb.append("     - VALUE(ad.VIRUS, 0) ");
            }
            stb.append("               - VALUE(ad.sick, 0) - VALUE(ad.NOTICE, 0) - VALUE(ad.NONOTICE, 0) ");
            stb.append("     AS classdays3, ");
            stb.append("     ad.late, ");
            stb.append("     ad.early, ");
            if ("1".equals(param._use_Attend_zero_hyoji)) {
                stb.append("     L1.CNT AS DETAIL_001, ");
                stb.append("     L2.CNT AS DETAIL_002, ");
                stb.append("     L3.CNT AS DETAIL_003, ");
                stb.append("     L4.CNT AS DETAIL_004, ");
                stb.append("     L101.CNT_DECIMAL AS DETAIL_101, ");
            } else {
                stb.append("     ad.REIHAI_KEKKA AS DETAIL_001, ");
                stb.append("     ad.M_KEKKA_JISU AS DETAIL_002, ");
                stb.append("     ad.REIHAI_TIKOKU AS DETAIL_003, ");
                stb.append("     ad.JYUGYOU_TIKOKU AS DETAIL_004, ");
                stb.append("     ad.JYUGYOU_JISU_DECIMAL AS DETAIL_101, ");
            }
            stb.append("     sumad.sum_classdays, ");
            stb.append("     sumad.SUM_SUSPEND, ");
            if ("true".equals(param._useKoudome)) {
                stb.append("     sumad.SUM_KOUDOME, ");
            }
            if ("true".equals(param._useVirus)) {
                stb.append("     sumad.SUM_VIRUS, ");
            }
            stb.append("     sumad.sum_mourning, ");
            stb.append("     VALUE(sumad.sum_classdays, 0) - VALUE(sumad.sum_suspend, 0) - VALUE(sumad.sum_mourning, 0) - VALUE(sumad.SUM_OFFDAYS, 0) - VALUE(sumad.SUM_ABROAD, 0) ");
            if ("true".equals(param._useKoudome)) {
                stb.append("     - VALUE(sumad.SUM_KOUDOME, 0) ");
            }
            if ("true".equals(param._useVirus)) {
                stb.append("     - VALUE(sumad.SUM_VIRUS, 0) ");
            }
            if ("1".equals(param._knjSchoolMst._semOffDays)) {
                stb.append("     + VALUE(sumad.SUM_OFFDAYS, 0) ");
            }
            stb.append("     AS sum_classdays2, ");
            stb.append("     (VALUE(sumad.sum_sick, 0) + VALUE(sumad.SUM_NOTICE, 0) + VALUE(sumad.SUM_NONOTICE, 0) ");
            if ("1".equals(param._knjSchoolMst._semOffDays)) {
                stb.append("     + VALUE(sumad.SUM_OFFDAYS, 0) ");
            }
            stb.append("     ) AS SUM_SICK, ");
            stb.append("     ((VALUE(sumad.sum_classdays, 0) - (VALUE(sumad.sum_suspend, 0) ");
            if ("true".equals(param._useKoudome)) {
                stb.append("     + VALUE(sumad.SUM_KOUDOME, 0) ");
            }
            if ("true".equals(param._useVirus)) {
                stb.append("     + VALUE(sumad.SUM_VIRUS, 0) ");
            }
            stb.append("      + VALUE(sumad.sum_mourning, 0) + VALUE(sumad.SUM_OFFDAYS, 0) + VALUE(sumad.SUM_ABROAD, 0))) - (VALUE(sumad.sum_sick, 0) + VALUE(sumad.SUM_NOTICE, 0) + VALUE(sumad.SUM_NONOTICE, 0))) AS sum_classdays3, ");
            stb.append("     sumad.sum_late, ");
            stb.append("     sumad.sum_early, ");
            stb.append("     sumad.SUM_DETAIL_001, ");
            stb.append("     sumad.SUM_DETAIL_002, ");
            stb.append("     sumad.SUM_DETAIL_003, ");
            stb.append("     sumad.SUM_DETAIL_004, ");
            stb.append("     sumad.SUM_DETAIL_101, ");
            stb.append("     CASE WHEN ATLESSON_1.LESSON IS NOT NULL ");
            stb.append("          THEN ATLESSON_1.LESSON ");
            stb.append("          ELSE ATLESSON_0.LESSON ");
            stb.append("     END AS DEF_LESSON, ");
            stb.append("     RMK.REMARK1 AS REMARK ");
            stb.append(" FROM ");
            stb.append("     schreg_regd_dat sd ");
            stb.append(" LEFT JOIN SCHREG_REGD_GDAT SG ON sd.YEAR = SG.YEAR ");
            stb.append("      AND sd.GRADE = SG.GRADE ");
            stb.append(" LEFT OUTER JOIN ");
            stb.append("     schreg_base_mst sm ");
            stb.append(" ON ");
            stb.append("     sd.schregno = sm.schregno ");
            stb.append(" LEFT OUTER JOIN ");
            stb.append("     (SELECT ");
            stb.append("         * ");
            stb.append("      FROM ");
            stb.append("         V_ATTEND_SEMES_DAT ");
            stb.append("      WHERE ");
            stb.append("         year = '" + param._ctrlYear + "' ");
            stb.append("         AND month = '" + monthsem[0] + "' ");
            stb.append("         AND SEMESTER = '" + monthsem[1] + "' ) AS ad ");
            stb.append(" ON ");
            stb.append("     ad.schregno = sd.schregno ");

            stb.append(" LEFT JOIN ATTEND_SEMES_DETAIL_DAT L1 ");
            stb.append("      ON ad.COPYCD   = L1.COPYCD ");
            stb.append("     AND ad.YEAR     = L1.YEAR ");
            stb.append("     AND ad.MONTH    = L1.MONTH ");
            stb.append("     AND ad.SEMESTER = L1.SEMESTER ");
            stb.append("     AND ad.SCHREGNO = L1.SCHREGNO ");
            stb.append("     AND L1.SEQ      = '001' ");
            stb.append(" LEFT JOIN ATTEND_SEMES_DETAIL_DAT L2 ");
            stb.append("      ON ad.COPYCD   = L2.COPYCD ");
            stb.append("     AND ad.YEAR     = L2.YEAR ");
            stb.append("     AND ad.MONTH    = L2.MONTH ");
            stb.append("     AND ad.SEMESTER = L2.SEMESTER ");
            stb.append("     AND ad.SCHREGNO = L2.SCHREGNO ");
            stb.append("     AND L2.SEQ      = '002' ");
            stb.append(" LEFT JOIN ATTEND_SEMES_DETAIL_DAT L3 ");
            stb.append("      ON ad.COPYCD   = L3.COPYCD ");
            stb.append("     AND ad.YEAR     = L3.YEAR ");
            stb.append("     AND ad.MONTH    = L3.MONTH ");
            stb.append("     AND ad.SEMESTER = L3.SEMESTER ");
            stb.append("     AND ad.SCHREGNO = L3.SCHREGNO ");
            stb.append("     AND L3.SEQ      = '003' ");
            stb.append(" LEFT JOIN ATTEND_SEMES_DETAIL_DAT L4 ");
            stb.append("      ON ad.COPYCD   = L4.COPYCD ");
            stb.append("     AND ad.YEAR     = L4.YEAR ");
            stb.append("     AND ad.MONTH    = L4.MONTH ");
            stb.append("     AND ad.SEMESTER = L4.SEMESTER ");
            stb.append("     AND ad.SCHREGNO = L4.SCHREGNO ");
            stb.append("     AND L4.SEQ      = '004' ");
            stb.append(" LEFT JOIN ATTEND_SEMES_DETAIL_DAT L101 ");
            stb.append("      ON ad.COPYCD   = L101.COPYCD ");
            stb.append("     AND ad.YEAR     = L101.YEAR ");
            stb.append("     AND ad.MONTH    = L101.MONTH ");
            stb.append("     AND ad.SEMESTER = L101.SEMESTER ");
            stb.append("     AND ad.SCHREGNO = L101.SCHREGNO ");
            stb.append("     AND L101.SEQ    = '101' ");

            stb.append(" LEFT OUTER JOIN ");
 
            if ("1".equals(param._useSchool_KindField)) {
 	            stb.append("     SCHREG_REGD_GDAT SGD ");
	            stb.append(" ON ");
	            stb.append("       SD.YEAR = SGD.YEAR ");
	            stb.append("   AND SD.GRADE = SGD.GRADE ");
	            stb.append(" LEFT OUTER JOIN ");
            }
            stb.append("     APPOINTED_DAY_MST am ");
            stb.append(" ON ");
            stb.append("       ad.year = am.year ");
            if ("1".equals(param._useSchool_KindField)) {
            	stb.append("   AND SGD.SCHOOL_KIND = am.SCHOOL_KIND ");
            }
            stb.append("   AND ad.month = am.month ");
            stb.append("   AND ad.SEMESTER = am.semester ");
            

            stb.append(" LEFT OUTER JOIN ");
            stb.append("     (SELECT ");
            stb.append("         SCHREGNO, ");
            stb.append("         SUM(LESSON) AS SUM_CLASSDAYS, ");
            stb.append("         SUM(OFFDAYS) AS SUM_OFFDAYS, ");
            stb.append("         SUM(ABROAD) AS SUM_ABROAD, ");
            stb.append("         SUM(SUSPEND) AS SUM_SUSPEND, ");
            if ("true".equals(param._useKoudome)) {
                stb.append("         SUM(KOUDOME) AS SUM_KOUDOME, ");
            }
            if ("true".equals(param._useVirus)) {
                stb.append("         SUM(VIRUS) AS SUM_VIRUS, ");
            }
            stb.append("         SUM(MOURNING) AS SUM_MOURNING, ");
            stb.append("         SUM(SICK) AS SUM_SICK, ");
            stb.append("         SUM(NOTICE) AS SUM_NOTICE, ");
            stb.append("         SUM(NONOTICE) AS SUM_NONOTICE, ");
            stb.append("         SUM(LATE) AS SUM_LATE, ");
            stb.append("         SUM(EARLY) AS SUM_EARLY, ");
            stb.append("         SUM(REIHAI_KEKKA) AS SUM_DETAIL_001, ");
            stb.append("         SUM(M_KEKKA_JISU) AS SUM_DETAIL_002, ");
            stb.append("         SUM(REIHAI_TIKOKU) AS SUM_DETAIL_003, ");
            stb.append("         SUM(JYUGYOU_TIKOKU) AS SUM_DETAIL_004, ");
            stb.append("         SUM(JYUGYOU_JISU_DECIMAL) AS SUM_DETAIL_101 ");
            stb.append("      FROM ");
            stb.append("         V_ATTEND_SEMES_DAT ");
            stb.append("      WHERE ");
            stb.append("            YEAR = '" + param._ctrlYear + "' ");
            if(!"".equals(monthsem[0]) && monthsem[0] != null){
                stb.append("        AND MONTH IN( " + range_month.get(monthsem[0]) + ")");    //2004/08/27 arakaki
                stb.append("        AND SEMESTER <= '" + monthsem[1] + "' ");
            }
            stb.append("      GROUP BY ");
            stb.append("         SCHREGNO) AS sumad ");
            stb.append(" ON ");
            stb.append("     sumad.schregno = sd.schregno ");

            stb.append(" LEFT JOIN ATTEND_LESSON_MST ATLESSON_0 ON sd.YEAR = ATLESSON_0.YEAR ");
            stb.append("      AND sd.GRADE = ATLESSON_0.GRADE ");
            stb.append("      AND ATLESSON_0.COURSECD = '0' ");
            stb.append("      AND ATLESSON_0.MAJORCD = '000' ");
            stb.append("      AND ATLESSON_0.SEMESTER = '" + monthsem[1] + "' ");
            stb.append("      AND ATLESSON_0.MONTH = '" + monthsem[0] + "' ");
            stb.append(" LEFT JOIN ATTEND_LESSON_MST ATLESSON_1 ON sd.YEAR = ATLESSON_1.YEAR ");
            stb.append("      AND sd.GRADE = ATLESSON_1.GRADE ");
            stb.append("      AND sd.COURSECD = ATLESSON_1.COURSECD ");
            stb.append("      AND sd.MAJORCD = ATLESSON_1.MAJORCD ");
            stb.append("      AND ATLESSON_1.SEMESTER = '" + monthsem[1] + "' ");
            stb.append("      AND ATLESSON_1.MONTH = '" + monthsem[0] + "' ");
            stb.append(" LEFT JOIN ATTEND_SEMES_REMARK_DAT RMK ON sd.YEAR = RMK.YEAR ");
            stb.append("      AND sd.SEMESTER = RMK.SEMESTER ");
            stb.append("      AND RMK.MONTH = '" + monthsem[0] + "' ");
            stb.append("      AND sd.SCHREGNO = RMK.SCHREGNO ");

            stb.append(" WHERE ");
            stb.append("     sd.year = '" + param._ctrlYear + "' AND ");

            // 2005/05/11 attend_semes_datにデータが存在しない場合でも、表示可能へ変更
            if(!"".equals(monthsem[0]) && monthsem[0] != null){
                    stb.append("     sd.semester = '" + monthsem[1] + "' AND");
            }else{
                    stb.append("     sd.semester IS NULL  AND");
            }

            stb.append("     sd.grade = '" + param._hrClass.substring(0, 2) + "' AND ");
            stb.append("     sd.hr_class = '" + param._hrClass.substring(3) + "' ");
            stb.append(" ORDER BY ");
            stb.append("     sd.attendno ");
            return stb.toString();
        }

        private static final String SHIMEBI = "SHIMEBI";
        private static final String LESSON = "LESSON";
        private static final String A004_2 = "A004_2";
        private static final String A004_1 = "A004_1";
        private static final String C001_1 = "C001_1";
        private static final String SUBEKI = "SUBEKI";
        private static final String C001_2 = "C001_2";
        private static final String C001_25 = "C001_25";
        private static final String C001_19 = "C001_19";
        private static final String C001_3 = "C001_3";
        private static final String C001_4 = "C001_4";
        private static final String C001_5 = "C001_5";
        private static final String C001_6 = "C001_6";
        private static final String SHUSSEKI = "SHUSSEKI";
        private static final String C001_15 = "C001_15";
        private static final String C001_16 = "C001_16";
        private static final String C002_001 = "C002_001";
        private static final String C002_002 = "C002_002";
        private static final String C002_003 = "C002_003";
        private static final String C002_004 = "C002_004";
        private static final String C002_101 = "C002_101";

        private Map _attendMap = null;

        public String getValue(final String cd) {
            if (null == _attendMap) {
                final Map m = new HashMap();
                m.put(SHIMEBI, _appointedDay);
                m.put(LESSON, _lesson);
                m.put(A004_2, _offdays);
                m.put(A004_1, _abroad);
                m.put(C001_1, _absent);
                m.put(SUBEKI, _classdays2);
                m.put(C001_2, _suspend);
                m.put(C001_25, _koudome);
                m.put(C001_19, _virus);
                m.put(C001_3, _mourning);
                m.put(C001_4, _sick);
                m.put(C001_5, _notice);
                m.put(C001_6, _nonotice);
                m.put(SHUSSEKI, _classdays3);
                m.put(C001_15, _late);
                m.put(C001_16, _early);
                m.put(C002_001, _detail001);
                m.put(C002_002, _detail002);
                m.put(C002_003, _detail003);
                m.put(C002_004, _detail004);
                m.put(C002_101, _detail101);

                m.put(LESSON  + KEY_SUM, _sumClassdays);
                m.put(C001_2  + KEY_SUM, _sumSuspend);
                m.put(C001_25 + KEY_SUM, _sumKoudome);
                m.put(C001_19 + KEY_SUM, _sumVirus);
                m.put(C001_3  + KEY_SUM, _sumMourning);
                m.put(SUBEKI  + KEY_SUM, _sumClassdays2);
                m.put(C001_6  + KEY_SUM, _sumSick);
                m.put(SHUSSEKI + KEY_SUM, _sumClassdays3);
                m.put(C001_15 + KEY_SUM, _sumLate);
                m.put(C001_16 + KEY_SUM, _sumEarly);
                m.put(C002_001 + KEY_SUM, _sumDetail001);
                m.put(C002_002 + KEY_SUM, _sumDetail002);
                m.put(C002_003 + KEY_SUM, _sumDetail003);
                m.put(C002_004 + KEY_SUM, _sumDetail004);
                m.put(C002_101 + KEY_SUM, _sumDetail101);

                _attendMap = m;
            }
            return (String) _attendMap.get(cd);
        }

        private static List getAttendNameList(final Param param) {
            final List allName = new ArrayList();
            allName.add(createMap(new String[]{KEY_CD, SHIMEBI, KEY_ITEM, "締め日"}));
            allName.add(createMap(new String[]{KEY_CD, LESSON,  KEY_ITEM, "授業日数"}));
            allName.add(createMap(new String[]{KEY_CD, A004_2,  KEY_ITEM, "休学日数", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, A004_1,  KEY_ITEM, "留学日数", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C001_1,  KEY_ITEM, "公欠日数", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C001_2,  KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C001_25, KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C001_19, KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C001_3,  KEY_ITEM, "忌引", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, SUBEKI,  KEY_ITEM, "出席すべき日数"}));
            allName.add(createMap(new String[]{KEY_CD, C001_4,  KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C001_5,  KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C001_6,  KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, SHUSSEKI,KEY_ITEM, "出席日数"}));
            allName.add(createMap(new String[]{KEY_CD, C001_15, KEY_ITEM, "遅刻", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C001_16, KEY_ITEM, "早退", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C002_001, KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C002_002, KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C002_003, KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C002_004, KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C002_101, KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));

            allName.add(createMap(new String[]{KEY_CD, LESSON  + KEY_SUM, KEY_ITEM, "授業日数"}));
            allName.add(createMap(new String[]{KEY_CD, C001_2  + KEY_SUM, KEY_ITEM, "%s"}));
            allName.add(createMap(new String[]{KEY_CD, C001_25 + KEY_SUM, KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C001_19 + KEY_SUM, KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C001_3  + KEY_SUM, KEY_ITEM, "忌引"}));
            allName.add(createMap(new String[]{KEY_CD, SUBEKI  + KEY_SUM, KEY_ITEM, "出席すべき日数"}));
            allName.add(createMap(new String[]{KEY_CD, C001_6  + KEY_SUM, KEY_ITEM, "欠席日数"}));
            allName.add(createMap(new String[]{KEY_CD, SHUSSEKI + KEY_SUM, KEY_ITEM, "出席日数"}));
            allName.add(createMap(new String[]{KEY_CD, C001_15 + KEY_SUM, KEY_ITEM, "遅刻"}));
            allName.add(createMap(new String[]{KEY_CD, C001_16 + KEY_SUM, KEY_ITEM, "早退"}));
            allName.add(createMap(new String[]{KEY_CD, C002_001 + KEY_SUM, KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C002_002 + KEY_SUM, KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C002_003 + KEY_SUM, KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C002_004 + KEY_SUM, KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));
            allName.add(createMap(new String[]{KEY_CD, C002_101 + KEY_SUM, KEY_ITEM, "%s", KEY_CHECK_NAME_MST, "true"}));

            final List rtn = new ArrayList();
            for (final Iterator it = allName.iterator(); it.hasNext();) {
                final Map map = (Map) it.next();
                if ("true".equals(map.get(KEY_CHECK_NAME_MST))) {
                    // 名称マスタ年度データに登録されている勤怠のみ表示
                    String cd = (String) map.get(KEY_CD);
                    if (null == cd) {
                        continue;
                    }
                    if (cd.endsWith(KEY_SUM)) {
                        cd = cd.substring(0, cd.length() - KEY_SUM.length());
                    }
                    if (!param._nameMst.keySet().contains(cd)) {
                        continue;
                    }
                }
                rtn.add(map);
            }

            return rtn;
        }

        private static Map createMap(final String[] array) {
            final Map rtn = new HashMap();
            for (int i = 0; i < array.length; i+= 2) {
                rtn.put(array[i], array[i + 1]);
            }
            return rtn;
        }
    }

    /** パラメータ取得 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 69939 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _hrClass;
        final String _month;
        final String _semesterName;
        final String _ctrlDate;
        final String _useVirus;
        final String _useKoudome;
        final String _useSchool_KindField;
        final String _unUseOffdays;
        final String _unUseAbroad;
        final String _unUseAbsent;
        final String _use_Attend_zero_hyoji;
        final String _use_SchregNo_hyoji;

        final KNJSchoolMst _knjSchoolMst;
        final String _hrName;
        final Map _nameMst;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _hrClass = request.getParameter("HR_CLASS");
            _month = request.getParameter("MONTH");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _unUseOffdays = request.getParameter("unUseOffdays");
            _unUseAbroad = request.getParameter("unUseAbroad");
            _unUseAbsent = request.getParameter("unUseAbsent");
            _use_Attend_zero_hyoji = request.getParameter("use_Attend_zero_hyoji");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");

            _semesterName = getSemestername(db2, StringUtils.split(_month, "-")[0], StringUtils.split(_month, "-")[1]);
            _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            _hrName = getHrName(db2);
            _nameMst = new HashMap();
            _nameMst.putAll(getNameMst(db2, "A004"));;
            _nameMst.putAll(getNameMst(db2, "C001"));
            _nameMst.putAll(getNameMst(db2, "C002"));
        }

        private String getSemestername(final DB2UDB db2, final String month, final String semester) throws SQLException {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     T1.NAMECD2, T1.NAME1, T1.NAMESPARE1, T2.SEMESTERNAME ");
            sql.append(" FROM ");
            sql.append("     NAME_MST T1, SEMESTER_MST T2");
            sql.append(" WHERE ");
            sql.append("     T1.NAMECD1 = 'Z005' ");
            sql.append("     AND T1.NAMECD2 = '" + month + "' ");
            sql.append("     AND T2.YEAR  = '" + _ctrlYear + "' ");
            sql.append("     AND T2.SEMESTER  = '" + semester + "' ");
            sql.append(" ORDER BY ");
            sql.append("     NAMESPARE1 ");

            String rtn = null;
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rtn = StringUtils.defaultString(rs.getString("SEMESTERNAME")) + " " + StringUtils.defaultString(rs.getString("NAME1"));
            }
            DbUtils.closeQuietly(null, ps, rs);
            return rtn;
        }

        private String getHrName(DB2UDB db2) throws SQLException {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT HR_NAME ");
            sql.append(" FROM SCHREG_REGD_HDAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' ");
            sql.append("   AND SEMESTER = '" + _ctrlSemester + "' ");
            sql.append("   AND GRADE || '-' || HR_CLASS = '" + _hrClass + "' ");

            String hrName = null;
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                hrName = rs.getString("HR_NAME");
            }
            return hrName;
        }

        private Map getNameMst(final DB2UDB db2, final String namecd1) throws SQLException {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     T1.NAMECD1 || '_' || T1.NAMECD2 AS CD, T1.NAME1 ");
            sql.append(" FROM ");
            sql.append("     V_NAME_MST T1 ");
            sql.append(" WHERE ");
            sql.append("     T1.YEAR = '" + _ctrlYear + "' ");
            sql.append("     AND T1.NAMECD1 = '" + namecd1 + "' ");
            sql.append(" ORDER BY ");
            sql.append("     NAMESPARE1, NAMECD2 ");

            final Map rtn = new HashMap();
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rtn.put(rs.getString(KEY_CD), rs.getString("NAME1"));
            }
            DbUtils.closeQuietly(null, ps, rs);
            return rtn;
        }
    }
}

// eof


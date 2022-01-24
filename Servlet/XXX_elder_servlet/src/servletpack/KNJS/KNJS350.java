/*
 * $Id: 62836264900e38da31c44d7f57d348a0a7ca05d2 $
 *
 * 作成日: 2011/09/20
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJS;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 *  学校教育システム 賢者 [小学校プログラム] 出席統計表
 *
 */
public class KNJS350 {

    private static final Log log = LogFactory.getLog(KNJS350.class);
    private static final Integer maxCd = new Integer(999);

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

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return map.get(key1);
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return map.get(key1);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJS350.frm", 4);

        svfPrintHead(svf);

        final List<String> monthList = new ArrayList();
        final DecimalFormat df = new DecimalFormat("00");
        for (int m = 4, i = 0; i < 12; i++) {
            final String month = df.format(m + i - (m + i > 12 ? 12 : 0));
            monthList.add(month);
        }
        final Map<String, String> monthTitle = new HashMap();
        monthTitle.put("04", "４月");
        monthTitle.put("05", "５月");
        monthTitle.put("06", "６月");
        monthTitle.put("07", "７月");
        monthTitle.put("08", "８月");
        monthTitle.put("09", "９月");
        monthTitle.put("10", "１０月");
        monthTitle.put("11", "１１月");
        monthTitle.put("12", "１２月");
        monthTitle.put("01", "１月");
        monthTitle.put("02", "２月");
        monthTitle.put("03", "３月");

        final Map<String, List<MonthSemesAttend>> attendSemesDatMap = getAttendSemesDatMap(db2);

        final List<Student> studentList = getStudentAttendSemesDatList(db2);

        int maxLastMonthEnroll = 0;
        int maxZaisekisu = 0;
        int sumTyoketuSu = 0;
        int maxZengetuSu = 0;

        int lastMonthEnroll = get4GatuZaisekisu(db2, attendSemesDatMap, "");        // 前月末在籍数（初期値は4月の月初め在籍数）
        int lastMonthEnrollMale = get4GatuZaisekisu(db2, attendSemesDatMap, "1");   // 前月末在籍数(男)（初期値は4月の月初め在籍数）
        int lastMonthEnrollFemale = get4GatuZaisekisu(db2, attendSemesDatMap, "2"); // 前月末在籍数(女)（初期値は4月の月初め在籍数）
        final MonthSemesAttend total = new MonthSemesAttend(null, null, new AttendSemesDat());
        for (final String month : monthList) {
            svf.VrsOut("MONTH", monthTitle.get(month));

            final List<MonthSemesAttend> attendSemesList;
            final int monthi = Integer.parseInt(month);
            final int tmonthi = Integer.parseInt(_param._targetMonth);
            if ((tmonthi >= 4 && monthi <= tmonthi || tmonthi < 4 && (monthi <= 12 || monthi <= tmonthi)) && null != attendSemesDatMap.get(month)) {
                attendSemesList = attendSemesDatMap.get(month);
            } else {
                attendSemesList = Collections.EMPTY_LIST;
            }

            for (final MonthSemesAttend attend : attendSemesList) {
                svf.VrsOut("MONTH", monthTitle.get(month));

                final String lastMonthEnrollStr = "04".equals(month) ? "--" : String.valueOf(lastMonthEnroll);

                final int zaisekisu = lastMonthEnroll + attend._tennyu.size() - attend._tenshutu.size();
                final int zaisekisuMale = lastMonthEnrollMale + attend._tennyuMale.size() - attend._tenshutuMale.size();
                final int zaisekisuFemale = lastMonthEnrollFemale + attend._tennyuFemale.size() - attend._tenshutuFemale.size();
                svf.VrsOut("LAST_MONTH_ENROLL", lastMonthEnrollStr);               // [2] 前月末在籍数※4月は横線。
                svf.VrsOut("IN_ENROLL", String.valueOf(attend._tennyu.size()));    // [3] 今月の転入数
                svf.VrsOut("OUT_ENROLL", String.valueOf(attend._tenshutu.size())); // [4] 今月の転出数
                svf.VrsOut("THIS_MONTH_ENROLL", String.valueOf(zaisekisu));        // [5] 今月末在籍数 ([2] + [3] - [4])

                if (null != attend._attendSemes) {
                    final int jugyoNissu = attend._attendSemes._jugyoNissu;
                    final int shutteiKibiki = attend._attendSemes._sumShutteiKibiki;
                    final int shutteiKibikiMale = attend._attendSemes._sumShutteiKibikiMale;
                    final int shutteiKibikiFemale = attend._attendSemes._sumShutteiKibikiFemale;
                    final int kessekiAll = attend._attendSemes._sumKesseki;
                    final int kessekiMale = attend._attendSemes._sumKessekiMale;
                    final int kessekiFemale = attend._attendSemes._sumKessekiFemale;
                    final int allSubeki = 0 == jugyoNissu ? 0 : jugyoNissu * zaisekisu  - shutteiKibiki;
                    final int allSubekiMale = 0 == jugyoNissu ? 0 : jugyoNissu * zaisekisuMale  - shutteiKibikiMale;
                    final int allSubekiFemale = 0 == jugyoNissu ? 0 : jugyoNissu * zaisekisuFemale  - shutteiKibikiFemale;
                    final int allShusseki = 0 == jugyoNissu ? 0 : allSubeki - kessekiAll;
                    final int allShussekiMale = 0 == jugyoNissu ? 0 : allSubekiMale - kessekiMale;
                    final int allShussekiFemale = 0 == jugyoNissu ? 0 : allSubekiFemale - kessekiFemale;
                    final String percentage = (0 == jugyoNissu || 0 == allSubeki) ? "0.0" : new BigDecimal(100 * allShusseki).divide(new BigDecimal(allSubeki), 1, BigDecimal.ROUND_HALF_UP).toString();
                    final String percentageMale = (0 == jugyoNissu || 0 == allSubekiMale) ? "0.0" : new BigDecimal(100 * allShussekiMale).divide(new BigDecimal(allSubekiMale), 1, BigDecimal.ROUND_HALF_UP).toString();
                    final String percentageFemale = (0 == jugyoNissu || 0 == allSubekiFemale) ? "0.0" : new BigDecimal(100 * allShussekiFemale).divide(new BigDecimal(allSubekiFemale), 1, BigDecimal.ROUND_HALF_UP).toString();
                    total._attendSemes._jugyoNissu = total._attendSemes._jugyoNissu + jugyoNissu;
                    total._attendSemes._sumShutteiKibiki = total._attendSemes._sumShutteiKibiki + shutteiKibiki;
                    total._attendSemes._sumShutteiKibikiMale = total._attendSemes._sumShutteiKibikiMale + shutteiKibikiMale;
                    total._attendSemes._sumShutteiKibikiFemale = total._attendSemes._sumShutteiKibikiFemale + shutteiKibikiFemale;
                    total._attendSemes._sumKesseki = total._attendSemes._sumKesseki + kessekiAll;
                    total._attendSemes._sumKessekiMale = total._attendSemes._sumKessekiMale + kessekiMale;
                    total._attendSemes._sumKessekiFemale = total._attendSemes._sumKessekiFemale + kessekiFemale;
                    total._attendSemes._allSubeki = total._attendSemes._allSubeki + allSubeki;
                    total._attendSemes._allSubekiMale = total._attendSemes._allSubekiMale + allSubekiMale;
                    total._attendSemes._allSubekiFemale = total._attendSemes._allSubekiFemale + allSubekiFemale;
                    total._attendSemes._allShusseki = total._attendSemes._allShusseki + allShusseki;
                    total._attendSemes._allShussekiMale = total._attendSemes._allShussekiMale + allShussekiMale;
                    total._attendSemes._allShussekiFemale = total._attendSemes._allShussekiFemale + allShussekiFemale;

                    final int tyoketuSu = 0 == jugyoNissu ? 0 : getTyoketuStudent(studentList, key(attend._semester, attend._month), total._attendSemes._jugyoNissu);
                    final int zengetuSu = 0 == jugyoNissu ? 0 : getZengetuStudent(studentList, key(attend._semester, attend._month), jugyoNissu);
                    sumTyoketuSu = sumTyoketuSu + tyoketuSu;
                    maxZengetuSu = Math.max(maxZengetuSu, zengetuSu);

                    svf.VrsOut("CLASS_DAY", String.valueOf(jugyoNissu));               // [1] 授業日数
                    svf.VrsOut("SUSPEND", String.valueOf(shutteiKibiki));              // [6] 出席停止・忌引等の総日数
                    svf.VrsOut("MUST", String.valueOf(allSubeki));                     // [7] 出席しなければならない総日数 ([1] * [5] - [6])
                    svf.VrsOut("ABSENCE", String.valueOf(kessekiAll));                 // [8] 欠席総日数
                    svf.VrsOut("ABSENCE_MALE", String.valueOf(kessekiMale));           // [8] 欠席総日数(男)
                    svf.VrsOut("ABSENCE_FEMALE", String.valueOf(kessekiFemale));       // [8] 欠席総日数(女)
                    svf.VrsOut("ATTEND", String.valueOf(allShusseki));                 // [9] 出席総日数 ([7] - [8])
                    svf.VrsOut("ATTEND_MALE", String.valueOf(allShussekiMale));        // [9] 出席総日数(男) ([7] - [8])
                    svf.VrsOut("ATTEND_FEMALE", String.valueOf(allShussekiFemale));    // [9] 出席総日数(女) ([7] - [8])
                    svf.VrsOut("ATTEND_PERCENT", percentage);                          // [10] 出席百分率 (([9] / [7]) * 100)
                    svf.VrsOut("ATTEND_PERCENT_MALE", percentageMale);                 // [10] 出席百分率(男) (([9] / [7]) * 100)
                    svf.VrsOut("ATTEND_PERCENT_FEMALE", percentageFemale);             // [10] 出席百分率(女) (([9] / [7]) * 100)
                    svf.VrsOut("LONG_ABSENCE", String.valueOf(tyoketuSu));             // [11-1] 長欠者数
                    svf.VrsOut("LAST_MONTH_ABSENCE", String.valueOf(zengetuSu));       // [11-2] 全月欠席者数

                    // 備考
                    final List<String> attendRemarkList = 0 == jugyoNissu ? Collections.EMPTY_LIST : getTokenList(getAttendRemark(studentList, key(attend._semester, attend._month)), 40);
                    for (int i = 0; i < 5 && i < attendRemarkList.size(); i++) {
                        final String remark = attendRemarkList.get(i);
                        svf.VrsOut("REMARK" + (i + 1), remark);
                    }
                }

                lastMonthEnroll = zaisekisu;
                lastMonthEnrollMale = zaisekisuMale;
                lastMonthEnrollFemale = zaisekisuFemale;
                maxLastMonthEnroll = Math.max(maxLastMonthEnroll, lastMonthEnroll);

                total._tennyu.addAll(attend._tennyu);
                total._tennyuMale.addAll(attend._tennyuMale);
                total._tennyuFemale.addAll(attend._tennyuFemale);
                total._tenshutu.addAll(attend._tenshutu);
                total._tenshutuMale.addAll(attend._tenshutuMale);
                total._tenshutuFemale.addAll(attend._tenshutuFemale);
                maxZaisekisu = Math.max(maxZaisekisu, zaisekisu);
            }
            svf.VrEndRecord();
        }

        final String percentage = (total._attendSemes._jugyoNissu == 0 || total._attendSemes._allSubeki == 0) ? "0.0" : new BigDecimal(100 * total._attendSemes._allShusseki).divide(new BigDecimal(total._attendSemes._allSubeki), 1, BigDecimal.ROUND_HALF_UP).toString();
        final String percentageMale = (total._attendSemes._jugyoNissu == 0 || total._attendSemes._allSubekiMale == 0) ? "0.0" : new BigDecimal(100 * total._attendSemes._allShussekiMale).divide(new BigDecimal(total._attendSemes._allSubekiMale), 1, BigDecimal.ROUND_HALF_UP).toString();
        final String percentageFemale = (total._attendSemes._jugyoNissu == 0 || total._attendSemes._allSubekiFemale == 0) ? "0.0" : new BigDecimal(100 * total._attendSemes._allShussekiFemale).divide(new BigDecimal(total._attendSemes._allSubekiFemale), 1, BigDecimal.ROUND_HALF_UP).toString();
        svf.VrsOut("MONTH", "計");
        svf.VrsOut("CLASS_DAY", String.valueOf(total._attendSemes._jugyoNissu));     // [1] 授業日数
        svf.VrsOut("LAST_MONTH_ENROLL", String.valueOf(maxLastMonthEnroll));         // [2] 前月末在籍数
        svf.VrsOut("IN_ENROLL", String.valueOf(total._tennyu.size()));               // [3] 今月の転入数
        svf.VrsOut("OUT_ENROLL", String.valueOf(total._tenshutu.size()));            // [4] 今月の転出数
        svf.VrsOut("THIS_MONTH_ENROLL", String.valueOf(maxZaisekisu));               // [5] 今月末在籍数 ([2] + [3] - [4])
        svf.VrsOut("SUSPEND", String.valueOf(total._attendSemes._sumShutteiKibiki)); // [6] 出席停止・忌引等の総日数
        svf.VrsOut("MUST", String.valueOf(total._attendSemes._allSubeki));           // [7] 出席しなければならない総日数 ([1] * [5] - [6])
        svf.VrsOut("ABSENCE", String.valueOf(total._attendSemes._sumKesseki));               // [8] 欠席総日数
        svf.VrsOut("ABSENCE_MALE", String.valueOf(total._attendSemes._sumKessekiMale));      // [8] 欠席総日数(男)
        svf.VrsOut("ABSENCE_FEMALE", String.valueOf(total._attendSemes._sumKessekiFemale));  // [8] 欠席総日数(女)
        svf.VrsOut("ATTEND", String.valueOf(total._attendSemes._allShusseki));               // [9] 出席総日数 ([7] - [8])
        svf.VrsOut("ATTEND_MALE", String.valueOf(total._attendSemes._allShussekiMale));      // [9] 出席総日数(男) ([7] - [8])
        svf.VrsOut("ATTEND_FEMALE", String.valueOf(total._attendSemes._allShussekiFemale));  // [9] 出席総日数(女) ([7] - [8])
        svf.VrsOut("ATTEND_PERCENT", percentage);                                            // [10] 出席百分率 (([9] / [7]) * 100)
        svf.VrsOut("ATTEND_PERCENT_MALE", percentageMale);                                   // [10] 出席百分率(男) (([9] / [7]) * 100)
        svf.VrsOut("ATTEND_PERCENT_FEMALE", percentageFemale);                               // [10] 出席百分率(女) (([9] / [7]) * 100)
        svf.VrsOut("LONG_ABSENCE", String.valueOf(sumTyoketuSu));             // [11-1] 長欠者数
        svf.VrsOut("LAST_MONTH_ABSENCE", String.valueOf(maxZengetuSu));       // [11-2] 全月欠席者数
        svf.VrEndRecord();
        _hasData = true;
    }

    private int get4GatuZaisekisu(final DB2UDB db2, final Map<String, List<MonthSemesAttend>> attendSemesDatMap, final String getType) {
        // 4月の月初め在籍数 = 1学期の在籍データの生徒数 - 1学期の転入生徒数 + 1学期の転出生徒数
        final Set<String> schregno1gakkiSet = new HashSet();
        final String checkSemester = "1";
        Set tennyu1gakki = new HashSet();
        Set tenshutu1gakki = new HashSet();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "";
            sql += " SELECT T1.SCHREGNO ";
            if (_param._isFi) {
                sql += " FROM SCHREG_REGD_FI_DAT T1 ";
            } else if (_param._isGhr) {
                sql += " FROM SCHREG_REGD_GHR_DAT T1 ";
            } else if (_param._isGakunenKongou) {
                sql += " FROM V_STAFF_HR_DAT T0 ";
                sql += " INNER JOIN SCHREG_REGD_DAT T1 ON T1.YEAR = T0.YEAR ";
                sql += "     AND T1.SEMESTER = T0.SEMESTER ";
                sql += "     AND T1.GRADE = T0.GRADE ";
                sql += "     AND T1.HR_CLASS = T0.HR_CLASS ";
            } else {
                sql += " FROM SCHREG_REGD_DAT T1 ";
            }
            if (!"".equals(getType)) {
                sql += " INNER JOIN SCHREG_BASE_MST SBM ON SBM.SCHREGNO = T1.SCHREGNO ";
                //SEX=1なら男子、それ以外は女子として計上
                if ("1".equals(getType)) {
                    sql += "   AND SBM.SEX = '" + getType + "' ";
                } else {
                    sql += "   AND SBM.SEX <> '1' ";
                }
            }
            sql += " WHERE T1.YEAR = '" + _param._ctrlYear + "' AND T1.SEMESTER = '" + checkSemester + "' ";
            if (_param._isGhr) {
                sql += "   AND T1.GHR_CD = '" + _param._gradeHrclass + "' ";
            } else if (_param._isGakunenKongou) {
                sql += "   AND T0.SCHOOL_KIND || '-' || T0.HR_CLASS = '" + _param._gradeHrclass + "' ";
                if ("1".equals(_param._restrictFlg)) {
                    sql += "   AND T0.STAFFCD = '" + _param._printLogStaffcd + "' ";
                }
            } else {
                sql += "   AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrclass + "' ";
            }
            log.debug(" 4GatuZaiseki:" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                schregno1gakkiSet.add(rs.getString("SCHREGNO"));
            }

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        for (final String month : attendSemesDatMap.keySet()) {
            final List<MonthSemesAttend> list = attendSemesDatMap.get(month);
            if (null != list) {
                for (final MonthSemesAttend msa : list) {
                    if (checkSemester.equals(msa._semester)) {
                        tennyu1gakki.addAll(msa._tennyu);
                        tenshutu1gakki.addAll(msa._tenshutu);
                    }
                }
            }
        }

        schregno1gakkiSet.addAll(tenshutu1gakki);
        schregno1gakkiSet.removeAll(tennyu1gakki);
        return  schregno1gakkiSet.size();
    }

    /**
     * 勤怠備考を得る
     * @param studentList 生徒のリスト
     * @param semeMonth 学期月
     * @return
     */
    private String getAttendRemark(final List<Student> studentList, final String semeMonth) {
        final StringBuffer stb = new StringBuffer();
        final Set<Integer> diCds = new TreeSet(); // 表示対象の勤怠コード
        for (final Student student : studentList) {
            final Map<Integer, ?> dicdSublMap = student._semeMonthDicdMap.get(semeMonth);
            if (null != dicdSublMap) {
                diCds.addAll(dicdSublMap.keySet());
            }
        }

        for (final Integer diCd : diCds) {
            final String diName = _param._nameMstC001.get(diCd); // 勤怠名称

            final StringBuffer substb = new StringBuffer();
            for (final Student student : studentList) {
                final StringBuffer stbr = student.getRemark(semeMonth, _param, diCd); // 生徒ごとの備考
                if (stbr.length() != 0) {
                    substb.append(stbr).append("\n");
                }
            }
            if (substb.length() > 0) {
                stb.append(diName).append("\n");
                stb.append(substb);
            }
        }
        return stb.toString();
    }

    private int getZengetuStudent(final List<Student> studentList, final String semeMonth, final int jugyoNissu) {
        final List<Student> list = new ArrayList();
        for (final Student student : studentList) {
            if (student.getKesseki(semeMonth) >= jugyoNissu) { // 欠席数が授業日数が対象
                list.add(student);
            }
        }
        return list.size();
    }

    private int getTyoketuStudent(final List<Student> studentList, final String semeMonth, final int ruisekiSyusseki) {
        final List<Student> list = new ArrayList();
        final int limit = ruisekiSyusseki / 3;
        // log.debug(" 長欠 sememonth = " + semeMonth + " ,  長欠判定上限欠席数 = " + limit);
        for (final Student student : studentList) {
            final int ruisekiKesseki = student.getRuisekiKesseki(semeMonth);
            if (ruisekiKesseki >= limit) { // 累積欠席が累積出席数1/3以上が対象
                // log.debug("  " + student + " , 累積欠席 = " + ruisekiKesseki);
                list.add(student);
            }
        }
        return list.size();
    }

    /**
     * @param source 元文字列
     * @param bytePerLine 1行あたりのバイト数
     * @return bytePerLineのバイト数ごとの文字列リスト
     */
    private static List getTokenList(final String source, final int bytePerLine) {

        if (source == null || source.length() == 0) {
            return Collections.EMPTY_LIST;
        }

        // String stoken[] = new String[f_cnt];
        final List tokenList = new ArrayList();        //分割後の文字列の配列
//        int lines = 0;                              // == stoken.size
        int startIndex = 0;                         //文字列の分割開始位置
        int byteLengthInLine = 0;                   //文字列の分割開始位置からのバイト数カウント
        // for (int s_cur = 0; s_cur < strx.length() && ib < f_cnt; s_cur++) {
        for (int idx = 0; idx < source.length(); idx += 1) {
            //改行マークチェック    04/09/28Modify
            if (source.charAt(idx) == '\r') {
                continue;
            }
            if (source.charAt(idx) == '\n') {
                // stoken[ib] = strx.substring(s_sta, s_cur);
                tokenList.add(source.substring(startIndex, idx));
//                lines += 1;
                byteLengthInLine = 0;
                startIndex = idx + 1;
            } else {
                final int sbytelen = KNJ_EditEdit.getMS932ByteLength(source.substring(idx, idx + 1));
                byteLengthInLine += sbytelen;
                if (byteLengthInLine > bytePerLine) {
                    // stoken[ib] = strx.substring(s_sta, s_cur);     // 04/09/22Modify
                    tokenList.add(source.substring(startIndex, idx));
//                    lines += 1;
                    byteLengthInLine = sbytelen;
                    startIndex = idx;
                }
            }
        }
        if (byteLengthInLine > 0) {
            // stoken[lines] = strx.substring(s_sta);
            tokenList.add(source.substring(startIndex));
        }

        return tokenList;
    }

    private List<Student> getStudentAttendSemesDatList(final DB2UDB db2) {
        final List<Student> studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map<String, Student> studentMap = new HashMap();
        try {
            final String sql = getStudentAttendSemesSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                if (null == studentMap.get(rs.getString("SCHREGNO"))) {
                    final Student student = new Student(rs.getString("SCHREGNO"), rs.getString("NAME"));
                    studentList.add(student);
                    studentMap.put(rs.getString("SCHREGNO"), student);
                }
                final Student student = studentMap.get(rs.getString("SCHREGNO"));
                final String semester = rs.getString("SEMESTER");
                final String month = rs.getString("MONTH");

                student.setKesseki(key(semester, month), Integer.valueOf(rs.getString("KESSEKI")));

                if (null != rs.getString("DI_CD")) {
                    final Integer diCd = new Integer(rs.getString("DI_CD"));
                    final Integer sublCd = null == rs.getString("SUBL_CD") ? maxCd : new Integer(rs.getString("SUBL_CD"));
                    final Integer submCd = null == rs.getString("SUBM_CD") ? maxCd : new Integer(rs.getString("SUBM_CD"));
                    final String cnt = null == rs.getString("SUBM_CD") ? rs.getString("LCNT") : rs.getString("MCNT");

                    student.setRemark(key(semester, month), diCd, sublCd, submCd, Integer.valueOf(cnt));
                }
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }

    private void svfPrintHead(final Vrw32alp svf) {
        svf.VrsOut("HR_NAME", _param._hrname);
        svf.VrsOut("TEACHER", _param._staffname);
        svf.VrsOut("NENDO", _param._nendo + "　出席統計表");
        svf.VrsOut("PERIOD", _param._period);
        svf.VrsOut("ymd1", _param._ctrlDateFormat);
    }

    private String getTennyuTenshutuSql() {
        // 在籍データ
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH REGD AS ( ");
        stb.append("   SELECT ");
        stb.append("       T1.SCHREGNO, ");
        stb.append("       T2.SEX,");
        stb.append("       T1.YEAR, ");
        stb.append("       T1.SEMESTER, ");
        stb.append("       T4.ENT_DIV, ");
        stb.append("       T4.ENT_DATE, ");
        stb.append("       T4.GRD_DIV, ");
        stb.append("       T4.GRD_DATE ");
        if (_param._isFi) {
            stb.append(" FROM SCHREG_REGD_FI_DAT T1 ");
        } else if (_param._isGhr) {
            stb.append(" FROM SCHREG_REGD_GHR_DAT T1 ");
        } else if (_param._isGakunenKongou) {
            stb.append(" FROM V_STAFF_HR_DAT T0 ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT T1 ON T1.YEAR = T0.YEAR ");
            stb.append("     AND T1.SEMESTER = T0.SEMESTER ");
            stb.append("     AND T1.GRADE = T0.GRADE ");
            stb.append("     AND T1.HR_CLASS = T0.HR_CLASS ");
        } else {
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
        }
        stb.append("       INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
        stb.append("       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("       INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("           AND T3.GRADE = REGD.GRADE ");
        stb.append("       LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("           AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("   WHERE ");
        stb.append("       T1.YEAR = '" + _param._ctrlYear + "' ");
        if (_param._isGhr) {
            stb.append("   AND T1.GHR_CD = '" + _param._gradeHrclass + "' ");
        } else if (_param._isGakunenKongou) {
            stb.append("   AND T0.SCHOOL_KIND || '-' || T0.HR_CLASS = '" + _param._gradeHrclass + "' ");
            if ("1".equals(_param._restrictFlg)) {
                stb.append("   AND T0.STAFFCD = '" + _param._printLogStaffcd + "' ");
            }
        } else {
            stb.append("   AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrclass + "' ");
        }
        stb.append(" ) ");
        // 転入
        stb.append("   SELECT ");
        stb.append("     'TENNYU' AS DIV, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     CASE WHEN MONTH(T1.ENT_DATE) < 10 THEN '0' || CAST(MONTH(T1.ENT_DATE) AS CHAR(1)) ELSE CAST(MONTH(T1.ENT_DATE) AS CHAR(2)) END AS MONTH, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.SCHREGNO ");
        stb.append("   FROM ");
        stb.append("     REGD T1 ");
        stb.append("     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.SEMESTER <> '9' ");
        stb.append("       AND T1.ENT_DATE BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("   WHERE ");
        stb.append("       VALUE(T1.ENT_DIV, '') IN ('4') AND T2.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("       AND T1.ENT_DATE <= '" + _param._edate + "' ");
        stb.append("   GROUP BY ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     CASE WHEN MONTH(T1.ENT_DATE) < 10 THEN '0' || CAST(MONTH(T1.ENT_DATE) AS CHAR(1)) ELSE CAST(MONTH(T1.ENT_DATE) AS CHAR(2)) END, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.SCHREGNO ");
        stb.append("  UNION ALL ");
        // 転出
        stb.append("   SELECT ");
        stb.append("     'TENSHUTU' AS DIV, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     CASE WHEN MONTH(T1.GRD_DATE + 1 DAY) < 10 THEN '0' || CAST(MONTH(T1.GRD_DATE + 1 DAY) AS CHAR(1)) ELSE CAST(MONTH(T1.GRD_DATE + 1 DAY) AS CHAR(2)) END AS MONTH, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.SCHREGNO ");
        stb.append("   FROM ");
        stb.append("     REGD T1 ");
        stb.append("     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.SEMESTER <> '9' ");
        stb.append("       AND T1.GRD_DATE + 1 DAY BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("   WHERE ");
        stb.append("       VALUE(T1.GRD_DIV, '') IN ('2', '3') AND T2.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("       AND T1.GRD_DATE + 1 DAY <= '" + _param._edate + "' ");
        stb.append("   GROUP BY ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     CASE WHEN MONTH(T1.GRD_DATE + 1 DAY) < 10 THEN '0' || CAST(MONTH(T1.GRD_DATE + 1 DAY) AS CHAR(1)) ELSE CAST(MONTH(T1.GRD_DATE + 1 DAY) AS CHAR(2)) END, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.SCHREGNO ");

        return stb.toString();
    }

    private Map getAttendSemesDatMap(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map<String, List<MonthSemesAttend>> attendSemesDatMap = new HashMap();
        try {
            final String sql = getAttendSemesDatSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String semester = rs.getString("SEMESTER");
                final String month = rs.getString("MONTH");

                final AttendSemesDat semes;
                if (null == rs.getString("JUGYO_NISSU")) {
                    semes = null;
                } else {
                    semes = new AttendSemesDat();
                    semes._jugyoNissu = rs.getInt("JUGYO_NISSU");
                    semes._sumLesson = rs.getInt("SUM_LESSON");
                    if (0 != semes._jugyoNissu) {
                        semes._sumShutteiKibiki = rs.getInt("SUM_SHUTTEI_KIBIKI");
                        semes._sumShutteiKibikiMale = rs.getInt("SUM_SHUTTEI_KIBIKI_MALE");
                        semes._sumShutteiKibikiFemale = rs.getInt("SUM_SHUTTEI_KIBIKI_FEMALE");
                        semes._sumKesseki = rs.getInt("SUM_KESSEKI");
                        semes._sumKessekiMale = rs.getInt("SUM_KESSEKI_MALE");
                        semes._sumKessekiFemale = rs.getInt("SUM_KESSEKI_FEMALE");
                    }

//                    final int shutteiKibiki = 0 == jugyoNissu ? 0 : attend._semes._sumShutteiKibiki;
//                    final int subekiAll = 0 == jugyoNissu ? 0 : jugyoNissu * zaisekisu  - shutteiKibiki;
//                    final int kessekiAll = 0 == jugyoNissu ? 0 : attend._semes._sumKesseki;
//                    final int shussekiAll = 0 == jugyoNissu ? 0 : subekiAll - kessekiAll;
//                    final String percentage = (0 == jugyoNissu || 0 == subekiAll) ? "0.0" : new BigDecimal(100 * shussekiAll).divide(new BigDecimal(subekiAll), 1, BigDecimal.ROUND_HALF_UP).toString();

                }

                final MonthSemesAttend attendSemesDat = new MonthSemesAttend(semester, month, semes);

                getMappedList(attendSemesDatMap, month).add(attendSemesDat);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        try {
            final String sql = getTennyuTenshutuSql();
            log.debug(" tennyu tenshutu sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String semester = rs.getString("SEMESTER");
                final String month = rs.getString("MONTH");

                final List<MonthSemesAttend> attendSemesDatList = getMappedList(attendSemesDatMap, month);
                MonthSemesAttend attendSemesDat = null;
                if (!attendSemesDatList.isEmpty()) {
                    for (final MonthSemesAttend msa : attendSemesDatList) {
                        if (msa._semester.equals(semester) && msa._month.equals(month)) {
                            attendSemesDat = msa;
                            break;
                        }
                    }
                }
                if (null == attendSemesDat) {
                	attendSemesDat = new MonthSemesAttend(semester, month, null);
                	attendSemesDatList.add(attendSemesDat);
                }
                final String schregno = rs.getString("SCHREGNO");
				if ("TENNYU".equals(rs.getString("DIV"))) {
                    attendSemesDat._tennyu.add(schregno);
                	//男子指定以外の人は女子として計上
                    if ("1".equals(rs.getString("SEX"))) {
                    	attendSemesDat._tennyuMale.add(schregno);
                    } else {
                        attendSemesDat._tennyuFemale.add(schregno);
                    }
                } else if ("TENSHUTU".equals(rs.getString("DIV"))) {
                    attendSemesDat._tenshutu.add(schregno);
                	//男子指定以外の人は女子として計上
                    if ("1".equals(rs.getString("SEX"))) {
                        attendSemesDat._tenshutuMale.add(schregno);
                    } else {
                        attendSemesDat._tenshutuFemale.add(schregno);
                    }
                }
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return attendSemesDatMap;
    }

    private String getAttendSemesDatSql() {

        final boolean semesFlg = ((Boolean) _param._hasuuMap.get("semesFlg")).booleanValue();
        final String attendSemesInState = (String) _param._hasuuMap.get("attendSemesInState");
        final String befDayFrom = (String) _param._hasuuMap.get("befDayFrom");
        final String befDayTo = (String) _param._hasuuMap.get("befDayTo");
        final String aftDayFrom = (String) _param._hasuuMap.get("aftDayFrom");
        final String aftDayTo = (String) _param._hasuuMap.get("aftDayTo");

        final StringBuffer stb = new StringBuffer();
        // 在籍データ
        stb.append(" WITH REGD AS ( ");
        stb.append("   SELECT ");
        stb.append("       T1.SCHREGNO, ");
        stb.append("       T1.YEAR, ");
        stb.append("       T1.SEMESTER ");
        if (_param._isFi) {
            stb.append(" FROM SCHREG_REGD_FI_DAT T1 ");
        } else if (_param._isGhr) {
            stb.append(" FROM SCHREG_REGD_GHR_DAT T1 ");
        } else if (_param._isGakunenKongou) {
            stb.append(" FROM V_STAFF_HR_DAT T0 ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT T1 ON T1.YEAR = T0.YEAR ");
            stb.append("     AND T1.SEMESTER = T0.SEMESTER ");
            stb.append("     AND T1.GRADE = T0.GRADE ");
            stb.append("     AND T1.HR_CLASS = T0.HR_CLASS ");
        } else {
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
        }
        stb.append("       INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
        stb.append("   WHERE ");
        stb.append("       T1.YEAR = '" + _param._ctrlYear + "' ");
        if (_param._isGhr) {
            stb.append("   AND T1.GHR_CD = '" + _param._gradeHrclass + "' ");
        } else if (_param._isGakunenKongou) {
            stb.append("   AND T0.SCHOOL_KIND || '-' || T0.HR_CLASS = '" + _param._gradeHrclass + "' ");
            if ("1".equals(_param._restrictFlg)) {
                stb.append("   AND T0.STAFFCD = '" + _param._printLogStaffcd + "' ");
            }
        } else {
            stb.append("   AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrclass + "' ");
        }
        if (null != befDayFrom || null != aftDayFrom) {
            stb.append(" ), SCHEDULES AS ( ");
            stb.append("   SELECT DISTINCT ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     T1.EXECUTEDATE ");
            stb.append("    FROM  ");
            stb.append("        SCH_CHR_DAT T1 ");
            stb.append("        INNER JOIN CHAIR_STD_DAT T2 ON T2.CHAIRCD = T1.CHAIRCD AND T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("        INNER JOIN REGD T3 ON T3.SCHREGNO = T2.SCHREGNO AND T3.YEAR = T2.YEAR AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("   WHERE ");
            stb.append("      T1.YEAR = '" + _param._ctrlYear + "' ");
            if (befDayFrom != null && aftDayFrom != null) {
                stb.append("    AND (T1.EXECUTEDATE BETWEEN '" + befDayFrom + "' AND '" + befDayTo + "' ");
                stb.append("         OR T1.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "') ");
            } else if (befDayFrom != null) {
                stb.append("    AND T1.EXECUTEDATE BETWEEN '" + befDayFrom + "' AND '" + befDayTo + "' ");
            } else if (aftDayFrom != null) {
                stb.append("    AND T1.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "' ");
            }
            stb.append(" ), SCHREG_ATTEND_DAY AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     MONTH(T1.EXECUTEDATE) AS MONTH, ");
            stb.append("     SUM(CASE WHEN VALUE(T2.DI_CD, '0') = '0' THEN 1 ELSE 0 END) AS LESSON, ");
            stb.append("     SUM(CASE WHEN VALUE(T2.DI_CD, '0') IN ('2','9') THEN 1 ELSE 0 END) AS SUSPEND, ");
            if ("true".equals(_param._useKoudome)) {
                stb.append("     SUM(CASE WHEN VALUE(T2.DI_CD, '0') IN ('25','26') THEN 1 ELSE 0 END) AS KOUDOME, ");
            } else {
                stb.append("     0 AS KOUDOME, ");
            }
            if ("true".equals(_param._useVirus)) {
                stb.append("     SUM(CASE WHEN VALUE(T2.DI_CD, '0') IN ('19','20') THEN 1 ELSE 0 END) AS VIRUS, ");
            } else {
                stb.append("     0 AS VIRUS, ");
            }
            stb.append("     SUM(CASE WHEN VALUE(T2.DI_CD, '0') IN ('3','10') THEN 1 ELSE 0 END) AS MOURNING, ");
            stb.append("     SUM(CASE WHEN VALUE(T2.DI_CD, '0') IN ('4','11') THEN 1 ELSE 0 END) AS SICK, ");
            stb.append("     SUM(CASE WHEN VALUE(T2.DI_CD, '0') IN ('5','12') THEN 1 ELSE 0 END) AS NOTICE, ");
            stb.append("     SUM(CASE WHEN VALUE(T2.DI_CD, '0') IN ('6','13') THEN 1 ELSE 0 END) AS NONOTICE ");
            stb.append("   FROM ");
            stb.append("       SCHEDULES T1 ");
            stb.append("       LEFT JOIN ATTEND_DAY_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("   GROUP BY ");
            stb.append("       T1.SCHREGNO, T1.SEMESTER, MONTH(T1.EXECUTEDATE) ");
        }
        // ATTEND_SEMES_DAT
        stb.append(" ), ATTEND_SEMES AS ( ");
        String unionAll = "";
        if (null != befDayFrom || null != aftDayFrom) {
            stb.append("   SELECT ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     CASE WHEN T1.MONTH < 10 THEN '0' || CAST(T1.MONTH AS CHAR(1)) ELSE CAST(T1.MONTH AS CHAR(2)) END AS MONTH, ");
            stb.append("     MAX(VALUE(T1.LESSON, 0)) AS JUGYO_NISSU, ");
            stb.append("     SUM(VALUE(T1.LESSON, 0)) AS SUM_LESSON, ");
            stb.append("     SUM(VALUE(T1.SUSPEND, 0)) + SUM(VALUE(T1.MOURNING,0)) ");
            if ("true".equals(_param._useKoudome)) {
                stb.append("     + SUM(VALUE(T1.KOUDOME,0)) ");
            }
            if ("true".equals(_param._useVirus)) {
                stb.append("     + SUM(VALUE(T1.VIRUS,0)) ");
            }
            stb.append("     AS SUM_SHUTTEI_KIBIKI, ");
            //SEX=1なら男子、それ以外は女子として計上
            stb.append("     SUM(CASE WHEN SBM.SEX = '1' THEN ");
            stb.append("          ");
            stb.append("           VALUE(T1.SUSPEND, 0) + VALUE(T1.MOURNING,0) ");
            if ("true".equals(_param._useKoudome)) {
                stb.append("     + VALUE(T1.KOUDOME,0) ");
            }
            if ("true".equals(_param._useVirus)) {
                stb.append("     + VALUE(T1.VIRUS,0) ");
            }
            stb.append("          ELSE 0 ");
            stb.append("     END ) ");
            stb.append("     AS SUM_SHUTTEI_KIBIKI_MALE, ");
            //SEX=1なら男子、それ以外は女子として計上
            stb.append("     SUM(CASE WHEN SBM.SEX <> '1' THEN ");
            stb.append("           VALUE(T1.SUSPEND, 0) + VALUE(T1.MOURNING,0) ");
            if ("true".equals(_param._useKoudome)) {
                stb.append("     + VALUE(T1.KOUDOME,0) ");
            }
            if ("true".equals(_param._useVirus)) {
                stb.append("     + VALUE(T1.VIRUS,0) ");
            }
            stb.append("          ELSE 0 ");
            stb.append("     END ) ");
            stb.append("     AS SUM_SHUTTEI_KIBIKI_FEMALE, ");
            stb.append("     SUM(VALUE(T1.SICK, 0)) + SUM(VALUE(T1.NOTICE,0)) + SUM(VALUE(T1.NONOTICE,0)) AS SUM_KESSEKI, ");
            //SEX=1なら男子、それ以外は女子として計上
            stb.append("     SUM(CASE WHEN SBM.SEX = '1' THEN VALUE(T1.SICK, 0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE,0) ELSE 0 END) AS SUM_KESSEKI_MALE, ");
            stb.append("     SUM(CASE WHEN SBM.SEX <> '1' THEN VALUE(T1.SICK, 0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE,0) ELSE 0 END) AS SUM_KESSEKI_FEMALE ");
            stb.append("   FROM ");
            stb.append("       SCHREG_ATTEND_DAY T1 ");
            stb.append("       INNER JOIN SCHREG_BASE_MST SBM ON SBM.SCHREGNO = T1.SCHREGNO ");
            stb.append("   GROUP BY ");
            stb.append("       T1.SEMESTER, T1.MONTH ");
            unionAll = " UNION ALL ";
        }
        if (semesFlg) {
            stb.append(unionAll);
            stb.append("   SELECT ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.MONTH, ");
            stb.append("     MAX(VALUE(T1.LESSON, 0)) AS JUGYO_NISSU, ");
            stb.append("     SUM(VALUE(T1.LESSON, 0)) AS SUM_LESSON, ");
            stb.append("     SUM(VALUE(T1.SUSPEND, 0)) + SUM(VALUE(T1.MOURNING,0)) ");
            if ("true".equals(_param._useKoudome)) {
                stb.append("     + SUM(VALUE(T1.KOUDOME,0)) ");
            }
            if ("true".equals(_param._useVirus)) {
                stb.append("     + SUM(VALUE(T1.VIRUS,0)) ");
            }
            stb.append("     AS SUM_SHUTTEI_KIBIKI, ");
            //SEX=1なら男子、それ以外は女子として計上
            stb.append("     SUM(CASE WHEN SBM.SEX = '1' THEN ");
            stb.append("         VALUE(T1.SUSPEND, 0) + VALUE(T1.MOURNING,0) ");
            if ("true".equals(_param._useKoudome)) {
                stb.append("     + VALUE(T1.KOUDOME,0) ");
            }
            if ("true".equals(_param._useVirus)) {
                stb.append("     + VALUE(T1.VIRUS,0) ");
            }
            stb.append("         ELSE 0 ");
            stb.append("     END ) ");
            stb.append("     AS SUM_SHUTTEI_KIBIKI_MALE, ");
            //SEX=1なら男子、それ以外は女子として計上
            stb.append("     SUM(CASE WHEN SBM.SEX <> '1' THEN ");
            stb.append("         VALUE(T1.SUSPEND, 0) + VALUE(T1.MOURNING,0) ");
            if ("true".equals(_param._useKoudome)) {
                stb.append("     + VALUE(T1.KOUDOME,0) ");
            }
            if ("true".equals(_param._useVirus)) {
                stb.append("     + VALUE(T1.VIRUS,0) ");
            }
            stb.append("         ELSE 0 ");
            stb.append("     END ) ");
            stb.append("     AS SUM_SHUTTEI_KIBIKI_FEMALE, ");
            stb.append("     SUM(VALUE(T1.SICK, 0)) + SUM(VALUE(T1.NOTICE,0)) + SUM(VALUE(T1.NONOTICE,0)) AS SUM_KESSEKI, ");
            //SEX=1なら男子、それ以外は女子として計上
            stb.append("     SUM(CASE WHEN SBM.SEX = '1' THEN VALUE(T1.SICK, 0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE,0) ELSE 0 END) AS SUM_KESSEKI_MALE, ");
            stb.append("     SUM(CASE WHEN SBM.SEX <> '1' THEN VALUE(T1.SICK, 0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE,0) ELSE 0 END) AS SUM_KESSEKI_FEMALE ");
            stb.append("   FROM ");
            stb.append("       ATTEND_SEMES_DAT T1 ");
            stb.append("   INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("   INNER JOIN SCHREG_BASE_MST SBM ON SBM.SCHREGNO = T2.SCHREGNO ");
            stb.append("   WHERE ");
            stb.append("      T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("      AND T1.SEMESTER || T1.MONTH IN " + attendSemesInState + " ");
            stb.append("   GROUP BY ");
            stb.append("       T1.SEMESTER, T1.MONTH ");
        }
        stb.append(" ), ATTEND_SEMES_SUM AS ( ");
        stb.append("   SELECT ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.MONTH, ");
        stb.append("     MAX(VALUE(T1.JUGYO_NISSU, 0)) AS JUGYO_NISSU, ");
        stb.append("     SUM(VALUE(T1.SUM_LESSON, 0)) AS SUM_LESSON, ");
        stb.append("     SUM(VALUE(T1.SUM_SHUTTEI_KIBIKI, 0)) AS SUM_SHUTTEI_KIBIKI, ");
        stb.append("     SUM(VALUE(T1.SUM_SHUTTEI_KIBIKI_MALE, 0)) AS SUM_SHUTTEI_KIBIKI_MALE, ");
        stb.append("     SUM(VALUE(T1.SUM_SHUTTEI_KIBIKI_FEMALE, 0)) AS SUM_SHUTTEI_KIBIKI_FEMALE, ");
        stb.append("     SUM(VALUE(T1.SUM_KESSEKI, 0)) AS SUM_KESSEKI, ");
        stb.append("     SUM(VALUE(T1.SUM_KESSEKI_MALE, 0)) AS SUM_KESSEKI_MALE, ");
        stb.append("     SUM(VALUE(T1.SUM_KESSEKI_FEMALE, 0)) AS SUM_KESSEKI_FEMALE ");
        stb.append("   FROM ");
        stb.append("       ATTEND_SEMES T1 ");
        stb.append("   GROUP BY ");
        stb.append("       T1.SEMESTER, T1.MONTH ");
        stb.append(" )  ");
        stb.append("   SELECT ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.MONTH, ");
        stb.append("     T1.JUGYO_NISSU, ");
        stb.append("     T1.SUM_LESSON, ");
        stb.append("     T1.SUM_SHUTTEI_KIBIKI, ");
        stb.append("     T1.SUM_SHUTTEI_KIBIKI_MALE, ");
        stb.append("     T1.SUM_SHUTTEI_KIBIKI_FEMALE, ");
        stb.append("     T1.SUM_KESSEKI, ");
        stb.append("     T1.SUM_KESSEKI_MALE, ");
        stb.append("     T1.SUM_KESSEKI_FEMALE ");
        stb.append("   FROM ");
        stb.append("     ATTEND_SEMES_SUM T1 ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SEMESTER, INT(T1.MONTH) + CASE WHEN (INT(T1.MONTH) < 4) THEN 12 ELSE 0 END ");
        return stb.toString();
    }

    private static class AttendSemesDat {
        int _jugyoNissu;
        int _sumLesson;
        int _sumShutteiKibiki;
        int _sumShutteiKibikiMale;
        int _sumShutteiKibikiFemale;
        int _sumKesseki;
        int _sumKessekiMale;
        int _sumKessekiFemale;
        //
        int _allSubeki;
        int _allSubekiMale;
        int _allSubekiFemale;
        int _allShusseki;
        int _allShussekiMale;
        int _allShussekiFemale;
    }

    private static class MonthSemesAttend {
        final String _semester;
        final String _month;
        final Set<String> _tennyu = new HashSet();
        final Set<String> _tennyuMale = new HashSet();
        final Set<String> _tennyuFemale = new HashSet();
        final Set<String> _tenshutu = new HashSet();
        final Set<String> _tenshutuMale = new HashSet();
        final Set<String> _tenshutuFemale = new HashSet();
        final AttendSemesDat _attendSemes;
        public MonthSemesAttend(
                final String semester,
                final String month,
                final AttendSemesDat semes) {
            _semester = semester;
            _month = month;
            _attendSemes = semes;
        }
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final Map<String, Integer> _semeMonthAbsenceMap = new TreeMap(); // "[SEMESTER]:[MONTH]"をキーとする欠席(SICK+NOTICE+NONOTICE)のマップ
        final Map<String, Map<Integer, Map<Integer, Map<Integer, Integer>>>> _semeMonthDicdMap = new TreeMap(); // Map<SemeMonth, Map<DI_CD, Map<SUBL_CD, Map<SUBM_CD, CNT>>>>
        Student(final String schregno, final String name) {
            _schregno = schregno;
            _name = name;
        }

        public void setKesseki(final String semeMonth, final Integer kesseki) {
            _semeMonthAbsenceMap.put(semeMonth, kesseki);
        }

        private int getRuisekiKesseki(final String semeMonth) {
            int ruisekiKesseki = 0;
            for (final String sm : _semeMonthAbsenceMap.keySet()) {
                if (sm.compareTo(semeMonth) <= 0) {
                    ruisekiKesseki += getKesseki(sm);
                }
            }
            return ruisekiKesseki;
        }

        private int getKesseki(final String semeMonth) {
            final Integer kesseki = _semeMonthAbsenceMap.get(semeMonth);
            if (null != kesseki) {
                return kesseki.intValue();
            }
            return 0;
        }

        /**
         * 備考をセットする
         * @param semeMonth
         * @param diCd DI_CD
         * @param sublCd SUBL_CD
         * @param submCd SUBM_CD
         * @param cnt 日数
         */
        private void setRemark(final String semeMonth, final Integer diCd, final Integer sublCd, final Integer submCd, final Integer cnt) {
            getMappedMap(getMappedMap(getMappedMap(_semeMonthDicdMap, semeMonth), diCd), sublCd).put(submCd, cnt);
        }

        /**
         * 指定された勤怠コードの備考を得る。（データが無い場合はブランク）
         * @param semeMonth 学期・月
         * @param param
         * @param diCd 勤怠コード
         * @return
         */
        private StringBuffer getRemark(final String semeMonth, final Param param, final Integer diCd) {
            final StringBuffer stb = new StringBuffer();
            final Map<Integer, Map<Integer, Map<Integer, Integer>>> dicdSublMap = _semeMonthDicdMap.get(semeMonth);
            if (null != dicdSublMap) {
                final Map<Integer, Map<Integer, Integer>> sublMap = dicdSublMap.get(diCd);
                if (null != sublMap) {
                    final StringBuffer stbDiRemark = getSublcdSubmcdRemark(param, sublMap);
                    stb.append(stbDiRemark);
                }
            }
            return stb;
        }

        private StringBuffer getSublcdSubmcdRemark(final Param param, final Map<Integer, Map<Integer, Integer>> sublMap) {
            final StringBuffer stb = new StringBuffer();
            String sep = "";
            int sum = 0;
            for (final Integer sublCd : sublMap.keySet()) {
                final String sublname = param._nameMstC006.get(sublCd); // SUBL_CDの名称

                final Map<Integer, Integer> submMap = sublMap.get(sublCd);
                if (submMap.containsKey(maxCd)) { // ATTEND_SEMES_SUBM_DATのレコードは使用無
                    if (null == sublname) {
                        log.info(" C006 名称なし" + sublCd);
                        continue;
                    }
                    stb.append(sep).append(sublname);
                    final Integer cnt = submMap.get(maxCd);
                    if (null != cnt) {
                        sum += cnt.intValue();
                    }
                    sep = "、";
                } else {
                    for (final Integer submCd : submMap.keySet()) {
                        final String submname = param._nameMstC007.get(submCd); // SUBM_CDの名称
                        if (null == submname) {
                            log.info(" C007 名称なし" + submCd);
                            continue;
                        }
                        stb.append(sep).append(submname);
                        final Integer cnt = submMap.get(submCd);
                        if (null != cnt) {
                            sum += cnt.intValue();
                        }
                        sep = "、";
                    }
                }
            }
            if (0 != stb.length()) {
                stb.insert(0, _name + "（").append("）");
            }
            if (0 != sum) {
                stb.insert(0, sum + "日 ");
            }
            return stb;
        }

        public String toString() {
            return "Student(" + _schregno + ":" + _name + ")";
        }
    }

    private static String key(final String semester, final String month) {
        return semester + ":" + month;
    }

    private String getStudentAttendSemesSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T5.NAME, ");
        stb.append("     T2.MONTH, ");
        stb.append("     T2.SEMESTER, ");
        stb.append("     VALUE(T2.SICK, 0) + VALUE(T2.NOTICE, 0) + VALUE(T2.NONOTICE, 0) AS KESSEKI, ");
        stb.append("     T3.DI_CD, ");
        stb.append("     T3.SUBL_CD, ");
        stb.append("     T3.CNT AS LCNT, ");
        stb.append("     T4.SUBM_CD, ");
        stb.append("     T4.CNT AS MCNT ");
        if (_param._isFi) {
            stb.append(" FROM SCHREG_REGD_FI_DAT T1 ");
        } else if (_param._isGhr) {
            stb.append(" FROM SCHREG_REGD_GHR_DAT T1 ");
        } else if (_param._isGakunenKongou) {
            stb.append(" FROM V_STAFF_HR_DAT T0 ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT T1 ON T1.YEAR = T0.YEAR ");
            stb.append("     AND T1.SEMESTER = T0.SEMESTER ");
            stb.append("     AND T1.GRADE = T0.GRADE ");
            stb.append("     AND T1.HR_CLASS = T0.HR_CLASS ");
        } else {
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
        }
        stb.append("     INNER JOIN SCHREG_BASE_MST T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN ATTEND_SEMES_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN ATTEND_SEMES_SUBL_DAT T3 ON T3.YEAR = T2.YEAR ");
        stb.append("         AND T3.MONTH = T2.MONTH ");
        stb.append("         AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("         AND T3.SCHREGNO = T2.SCHREGNO ");
        stb.append("         AND T3.CNT <> 0 ");
        stb.append("     LEFT JOIN ATTEND_SEMES_SUBM_DAT T4 ON T4.YEAR = T2.YEAR ");
        stb.append("         AND T4.MONTH = T3.MONTH ");
        stb.append("         AND T4.SEMESTER = T3.SEMESTER ");
        stb.append("         AND T4.SCHREGNO = T3.SCHREGNO ");
        stb.append("         AND T4.SUBL_CD = T3.SUBL_CD ");
        stb.append("         AND T4.CNT <> 0 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        if (_param._isGhr) {
            stb.append("   AND T1.GHR_CD = '" + _param._gradeHrclass + "' ");
        } else if (_param._isGakunenKongou) {
            stb.append("   AND T0.SCHOOL_KIND || '-' || T0.HR_CLASS = '" + _param._gradeHrclass + "' ");
            if ("1".equals(_param._restrictFlg)) {
                stb.append("   AND T0.STAFFCD = '" + _param._printLogStaffcd + "' ");
            }
        } else {
            stb.append("   AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrclass + "' ");
        }
        stb.append(" ORDER BY ");
        if (_param._isGhr) {
            stb.append("     T1.GHR_ATTENDNO, ");
        } else if (_param._isGakunenKongou) {
            stb.append("     T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, ");
        } else {
            stb.append("     T1.ATTENDNO, ");
        }
        stb.append("     INT(T2.MONTH) + CASE WHEN INT(T2.MONTH) < 4 THEN 12 ELSE 0 END ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 73713 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _hrClassType;
        final String _gakunenKongou;
        final String _gradeHrclass;
        final String _targetMonth;
        final String _targetSemes;
        final String _targetDay;
        final String _edate;
        final String _useVirus;
        final String _useKoudome;
        final String _restrictFlg;
        final String _printLogStaffcd;
        final String _ctrlDateFormat;
        final String _period;
        final String _nendo;

        final String _monthFirstDate;
        final String _hrname;
        final String _staffname;
        final boolean _seirekiFlg;
        final Map<Integer, String> _nameMstC001;
        final Map<Integer, String> _nameMstC006;
        final Map<Integer, String> _nameMstC007;

        final Map _attendSemesMap;
        final Map _hasuuMap;
        boolean _isFi = false;
        boolean _isGhr = false;
        boolean _isGakunenKongou = false;
        boolean _isHoutei = false;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _hrClassType = request.getParameter("HR_CLASS_TYPE");
            _gakunenKongou = request.getParameter("GAKUNEN_KONGOU");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            final String[] targetMonth = StringUtils.split(request.getParameter("TARGET_MONTH"), "-");
            _targetMonth = new DecimalFormat("00").format(Integer.parseInt(targetMonth[0]));
            _targetSemes = targetMonth[1];
            _targetDay = request.getParameter("TARGET_DAY");
            _edate = (Integer.parseInt(_ctrlYear) + (Integer.parseInt(_targetMonth) < 4 ? 1  : 0)) + "-" + _targetMonth + "-" + _targetDay;
            _monthFirstDate = (Integer.parseInt(_ctrlYear) + (Integer.parseInt(_targetMonth) < 4 ? 1  : 0)) + "-" + _targetMonth + "-" + "01";
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _restrictFlg = request.getParameter("RESTRICT_FLG");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _ctrlDateFormat = KNJ_EditDate.getAutoFormatDate(db2, _ctrlDate);
            if ("2".equals(_hrClassType) && "1".equals(request.getParameter("useFi_Hrclass"))) {
                _isFi = true;
            } else if ("2".equals(_hrClassType) && "1".equals(request.getParameter("useSpecial_Support_Hrclass"))) {
                _isGhr = true;
            } else if ("1".equals(_hrClassType) && "1".equals(_gakunenKongou) && "1".equals(request.getParameter("useSpecial_Support_Hrclass"))) {
                _isGakunenKongou = true;
            } else {
                _isHoutei = true;
            }
            log.debug(" fi? " + _isFi + ", ghr? " + _isGhr + ", gakunenKongou? " + _isGakunenKongou);

            _hrname = getHrname(db2);
            _staffname = getStaffname(db2);
            _seirekiFlg = KNJ_EditDate.isSeireki(db2);
            _nameMstC001 = getNameMst(db2, "C001");
            _nameMstC006 = getNameMst(db2, "C006");
            _nameMstC007 = getNameMst(db2, "C007");

            final String thisYear = _seirekiFlg ? _ctrlYear + "年" : KNJ_EditDate.h_format_JP_N(db2, _ctrlYear + "-04-30");
            final String nextYear = _seirekiFlg ? String.valueOf(Integer.parseInt(_ctrlYear) + 1) + "年" : KNJ_EditDate.h_format_JP_N(db2, String.valueOf(Integer.parseInt(_ctrlYear) + 1) + "-03-31");
            _period = "（" + thisYear + "4月" + "〜" + nextYear + "3月）";
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_ctrlYear)) + "年度";

            final String sdate = getYearSdate(db2, _ctrlYear);
            _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, null, _ctrlYear);
            _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, sdate, _edate);
        }

        private String getHrname(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String hrName = null;
            try {
                final StringBuffer sql = new StringBuffer();
                if (_isFi) {
                    sql.append(" SELECT HR_NAME ");
                    sql.append(" FROM SCHREG_REGD_FI_HDAT T1 ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _ctrlSemester + "' ");
                    sql.append("   AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                } else if (_isGhr) {
                    sql.append(" SELECT GHR_NAME AS HR_NAME ");
                    sql.append(" FROM SCHREG_REGD_GHR_HDAT T1 ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _ctrlSemester + "' ");
                    sql.append("   AND T1.GHR_CD = '" + _gradeHrclass + "' ");
                } else if (_isGakunenKongou) {
                    sql.append(" SELECT  ");
                    sql.append("   T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ");
                    sql.append("   MAX(T1.HR_CLASS_NAME1) AS HR_NAME ");
                    sql.append(" FROM V_STAFF_HR_DAT T1 ");
                    sql.append(" WHERE ");
                    sql.append("   T1.YEAR = '" + _ctrlYear + "' ");
                    sql.append("   AND T1.SEMESTER = '" + _ctrlSemester + "' ");
                    sql.append("   AND T1.SCHOOL_KIND || '-' || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                    if ("1".equals(_restrictFlg)) {
                        sql.append("   AND T1.STAFFCD = '" + _printLogStaffcd + "' ");
                    }
                    sql.append(" GROUP BY ");
                    sql.append("   T1.SCHOOL_KIND || '-' || T1.HR_CLASS ");
                    sql.append(" ORDER BY ");
                    sql.append("   T1.SCHOOL_KIND || '-' || T1.HR_CLASS ");
                } else {
                    sql.append(" SELECT HR_NAME ");
                    sql.append(" FROM SCHREG_REGD_HDAT T1 ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _ctrlSemester + "' ");
                    sql.append("   AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                }
                log.debug(" staffname sql = " + sql.toString());

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    hrName = rs.getString("HR_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return hrName;
        }

        private String getStaffname(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String staffname = null;
            try {
                final StringBuffer sql = new StringBuffer();
                if (_isFi) {
                    sql.append(" SELECT VALUE(T2.STAFFNAME, T3.STAFFNAME, T4.STAFFNAME) AS STAFFNAME ");
                    sql.append(" FROM SCHREG_REGD_FI_HDAT T1 ");
                    sql.append("     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = TR_CD1 ");
                    sql.append("     LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = TR_CD2 ");
                    sql.append("     LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = TR_CD3 ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _ctrlSemester + "' ");
                    sql.append("   AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                } else if (_isGhr) {
                    sql.append(" SELECT VALUE(T2.STAFFNAME, T3.STAFFNAME, T4.STAFFNAME) AS STAFFNAME ");
                    sql.append(" FROM SCHREG_REGD_GHR_HDAT T1 ");
                    sql.append("     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = TR_CD1 ");
                    sql.append("     LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = TR_CD2 ");
                    sql.append("     LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = TR_CD3 ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _ctrlSemester + "' ");
                    sql.append("   AND T1.GHR_CD = '" + _gradeHrclass + "' ");
                } else if (_isGakunenKongou) {
                    sql.append(" SELECT T1.STAFFCD, T2.STAFFNAME  ");
                    sql.append(" FROM V_STAFF_HR_DAT T1 ");
                    sql.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
                    sql.append(" WHERE ");
                    sql.append("   T1.YEAR = '" + _ctrlYear + "' ");
                    sql.append("   AND T1.SEMESTER = '" + _ctrlSemester + "' ");
                    sql.append("   AND T1.SCHOOL_KIND || '-' || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                    if ("1".equals(_restrictFlg)) {
                        sql.append("   AND T1.STAFFCD = '" + _printLogStaffcd + "' ");
                    }
                    sql.append(" ORDER BY ");
                    sql.append("   T1.STAFFCD ");
                } else {
                    sql.append(" SELECT VALUE(T2.STAFFNAME, T3.STAFFNAME, T4.STAFFNAME) AS STAFFNAME ");
                    sql.append(" FROM SCHREG_REGD_HDAT T1 ");
                    sql.append("     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = TR_CD1 ");
                    sql.append("     LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = TR_CD2 ");
                    sql.append("     LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = TR_CD3 ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _ctrlSemester + "' ");
                    sql.append("   AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                }
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (null != rs.getString("STAFFNAME")) {
                        staffname = rs.getString("STAFFNAME");
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                if (_isGakunenKongou || _isHoutei) {
                    final StringBuffer sql = new StringBuffer();
                    sql.append(" SELECT T1.TR_DIV, T1.FROM_DATE, T1.TO_DATE, T1.STAFFCD, T2.STAFFNAME, T1.GRADE, T1.HR_CLASS ");
                    sql.append(" FROM STAFF_CLASS_HIST_DAT T1 ");
                    sql.append(" INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _ctrlSemester + "' ");
                    sql.append("   AND T1.FROM_DATE <= '" + _edate + "' ");
                    sql.append("   AND VALUE(T1.TO_DATE, '9999-12-31') >= '" + _monthFirstDate + "' ");
                    if (_isGakunenKongou) {
                        sql.append("   AND (T1.GRADE, T1.HR_CLASS) IN ( ");
                        sql.append("     SELECT T1.GRADE, T1.HR_CLASS  ");
                        sql.append("     FROM V_STAFF_HR_DAT T1 ");
                        sql.append("     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
                        sql.append("     WHERE ");
                        sql.append("       T1.YEAR = '" + _ctrlYear + "' ");
                        sql.append("       AND T1.SEMESTER = '" + _ctrlSemester + "' ");
                        sql.append("       AND T1.SCHOOL_KIND || '-' || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                        if ("1".equals(_restrictFlg)) {
                            sql.append("   AND T1.STAFFCD = '" + _printLogStaffcd + "' ");
                        }
                        sql.append("   ) ");
                    } else {
                        sql.append("   AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                    }
                    sql.append(" ORDER BY T1.TR_DIV, T1.FROM_DATE, VALUE(T1.TO_DATE, '9999-12-31'), T1.STAFFCD, T1.GRADE, T1.HR_CLASS ");

                    ps = db2.prepareStatement(sql.toString());
                    rs = ps.executeQuery();

                    String rsStaffname = null;

                    String firstTrDiv = null;
                    while (rs.next()) {

                        if (null == firstTrDiv) {
                            firstTrDiv = rs.getString("TR_DIV"); // 最小のTR_DIV
                        } else {
                            if (!firstTrDiv.equals(rs.getString("TR_DIV"))) { // 最小のTR_DIVのみ処理
                                break;
                            }
                        }
                        log.debug(" trDiv = " + rs.getString("TR_DIV") + ", fromDate = " + rs.getString("FROM_DATE") + ", toDate = " + rs.getString("TO_DATE") + ", staffcd = " + rs.getString("STAFFCD") + ", (grade || hrClass = " + rs.getString("GRADE") + " || " + rs.getString("HR_CLASS") + ")");
                        rsStaffname = rs.getString("STAFFNAME");
                    }
                    if (null != rsStaffname) {
                        // STAFF_CLASS_HIST_DATがあれば優先して表示
                        staffname = rsStaffname;
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return staffname;
        }

        private Map getNameMst(final DB2UDB db2, final String namecd1) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map rtn = new HashMap();
            try {
                final String sqlNameMstC001 = " SELECT NAMECD2, NAME1 FROM V_NAME_MST T1 WHERE T1.NAMECD1 = '" + namecd1 + "' AND  T1.YEAR = '" + _ctrlYear + "' ";
                ps = db2.prepareStatement(sqlNameMstC001);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(Integer.valueOf(rs.getString("NAMECD2")), rs.getString("NAME1"));
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getYearSdate(final DB2UDB db2, final String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = year + "-04-01";
            try {
                final String sqlNameMstC001 = " SELECT SDATE FROM SEMESTER_MST T1 WHERE T1.YEAR = '" + year + "' AND T1.SEMESTER = '1' ";
                ps = db2.prepareStatement(sqlNameMstC001);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SDATE");
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}

// eof
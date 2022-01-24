/*
 * $Id: 882725c0b292d1c76e32a106cf1d4db55ff55d55 $
 *
 * 作成日: 2012/12/05
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;


import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 成績概況（クラス別）
 */
public class KNJM833 {

    private static final Log log = LogFactory.getLog(KNJM833.class);

    private static final String SEME1 = "1";
    private static final String SEME2 = "2";
    private static final String SEMEALL = "9";

    private static final String RISHU = "1";
    private static final String KOUNIN = "2";
    private static final String ZOUTAN = "3";

    private static final int TOUROKU = 1;
    private static final int KAKUTEI = 2;

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

    private static String addNumber(final String num1, final String num2) {
        if (!NumberUtils.isNumber(num1)) {
            return num2;
        }
        if (!NumberUtils.isNumber(num2)) {
            return num1;
        }
        return new BigDecimal(num1).add(new BigDecimal(num2)).toString();
    }

    private static Student getStudent(final List list, final String schregno) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
    }

    private static int getMS932ByteLength(final String name) {
        return KNJ_EditEdit.getMS932ByteLength(name);
    }

    private static String getDispNum(final String num, final String defVal) {
        if (num == null || !NumberUtils.isNumber(num)) {
            return defVal;
        }
        final BigDecimal bd = new BigDecimal(num);
        if (bd.setScale(0, BigDecimal.ROUND_UP).equals(bd.setScale(0, BigDecimal.ROUND_DOWN))) {
            // 切り上げでも切り下げでも値が変わらない = 小数点以下が0
            return bd.setScale(0).toString();
        }
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String def0(final String s) {
        return null == s ? "0" : s;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final String form;
        if (_param._isFukuiken) {
            form = "KNJM834_FUKUIKEN.frm";
        } else {
            form = "KNJM834.frm";
        }
        final List studentAllList = getStudentList(db2);
        final List<List<Student>> pagelist = getPageList(studentAllList, _param._isFukuiken ? 2 : 4); // ひとりの生徒に2段使用し、1段目に前期科目、2段目に後期科目を出力する
        final int totalPage = pagelist.size();

        final String title;
        if ("KNJM834".equals(_param._prgId)) {
            title = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　成績一覧表";
        } else {
            title = "成 績 概 評";
        }

        final String printDate = KNJ_EditDate.h_format_JP(db2, _param._loginDate);

        for (int pi = 0; pi < pagelist.size(); pi++) {
            final int page = pi + 1;
            final List<Student> studentList = pagelist.get(pi);

            svf.VrSetForm(form, 1);
            svf.VrsOut("TITLE", title);
            svf.VrsOut("PRGID", _param._prgId);
            svf.VrsOut("TOTAL_PAGE", String.valueOf(totalPage)); // 総ページ数
            svf.VrsOut("PAGE", String.valueOf(page)); // ページ

            // ページごとに生徒表示
            for (int stline = 0; stline < studentList.size(); stline++) {
                final Student student = studentList.get(stline);
                final int danMax;
                if (_param._isFukuiken) {
                    danMax = 2;
                } else {
                    danMax = 1;
                }

                for (int dan = 1; dan <= danMax; dan++) {
                    final int line = stline * danMax + dan;

                    final String suf2 = getMS932ByteLength(student._name) > 30 ? "3" : getMS932ByteLength(student._name) > 20 ? "2" : "1";
                    svf.VrsOutn("NAME" + suf2, line, student._name); // 氏名

                    final String suf1 = getMS932ByteLength(student._trcd1Staffname) > 20 ? "2" : "1";
                    svf.VrsOutn("TEACHER" + suf1, line, student._trcd1Staffname); // 科目担当

                    svf.VrsOutn("SCHREG_NO1", line, student._schregno); // 学籍番号
                    if (dan == 1) {
                        svf.VrsOutn("SEM1_AVERAGE", line, Student.getHeikin(student._zenkiScore, student._zenkiCount)); // 前期平均点
                    }
                    if (dan == danMax - 1) {
                        svf.VrsOutn("SEM2_AVERAGE", line, Student.getHeikin(student._koukiScore, student._koukiCount)); // 後期平均点
                    }

                    svf.VrsOutn("GET_CREDIT1", line, def0(student._shutokuzumiTanni)); // 登録時修得済単位
                    final String tourokuKonnnenndoTanni = addNumber(student._tourokuKonnendoTanni, student.getKougaiTanni(TOUROKU));
                    svf.VrsOutn("GET_CREDIT2", line, def0(tourokuKonnnenndoTanni)); // 登録時今年度単位
                    svf.VrsOutn("GET_TOTAL_CREDIT", line, def0(addNumber(student._shutokuzumiTanni, tourokuKonnnenndoTanni))); // 登録時修得済累計単位
                    svf.VrsOutn("DEC_CREDIT1", line, def0(student._shutokuzumiTanni)); // 確定修得済単位
                    final String kakuteiKonnnenndoTanni = addNumber(student._kakuteiKonnendoTanni, student.getKougaiTanni(KAKUTEI));
                    svf.VrsOutn("DEC_CREDIT2", line, def0(kakuteiKonnnenndoTanni)); // 確定今年度単位
                    svf.VrsOutn("DEC_TOTAL_CREDIT", line, def0(addNumber(student._shutokuzumiTanni, kakuteiKonnnenndoTanni))); // 確定累計単位
                    if ("4".equals(student._entDiv) || "5".equals(student._entDiv)) { // 転入 or 編入
                        svf.VrsOutn("ENT_NAME", line, student._entDivName);
                    }
                    if ("1".equals(student._baseRemark1)) {
                        svf.VrsOutn("GRAD", line, "卒予");
                    }

                    svf.VrsOutn("DATE", line, printDate); // 印刷日時

                    if (_param._isFukuiken) {
                        if (dan == 1) {
                            svf.VrsOutn("SEMESTER1", line, _param._semesterName.get(SEME1));
                            svf.VrAttributen("SEMESTER2_2", line, "X=10000");
                        } else if (dan == 2) {
                            svf.VrsOutn("SEMESTER2", line, _param._semesterName.get(SEME2));
                            svf.VrAttributen("SEMESTER1_2", line, "X=10000");
                        }
                    } else {
                        svf.VrsOutn("SEMESTER1", line, _param._semesterName.get(SEME1));
                        svf.VrsOutn("SEMESTER2", line, _param._semesterName.get(SEME2));
                    }
                    svf.VrsOutn("SEMESTER1_3", line, _param._semesterName.get(SEME1));
                    svf.VrsOutn("SEMESTER3", line, _param._semesterName.get(SEMEALL));
                    String defStr1 = "0";
                    if (!SEME1.equals(_param._semester) && SEME2.equals(student._entDateSemester) &&
                            (null == student.getAttendNissu(SEME1, null) && null == getDispNum(student._at93seme1, null) && null == getDispNum(student._at94seme1, null) && null == getDispNum(addNumber(student._at93seme1, student._at94seme1), null))) {
                        // 前期以外に出力した際、入学日付が今年度後期かつ前期の出欠の値が0の生徒は前期の出欠欄はブランクで表示する
                        defStr1 = null;
                    }

                    svf.VrsOutn("OP_DAY1", line, student.getAttendNissu(SEME1, defStr1)); // 出校日数
                    svf.VrsOutn("SP_HR1", line, getDispNum(student._at93seme1, defStr1)); // 特活HR
                    svf.VrsOutn("SP_EVENT1", line, getDispNum(student._at94seme1, defStr1)); // 特活行事
                    svf.VrsOutn("SP_TOTAL1", line, getDispNum(addNumber(student._at93seme1, student._at94seme1), defStr1)); // 特活計
                    if (_param.isPrintGakunenmatsu()) {
                        svf.VrsOutn("OP_DAY2", line, student.getAttendNissu(SEMEALL, "0")); // 出校日数
                        svf.VrsOutn("SP_HR2", line, getDispNum(student._at93seme9, "0")); // 特活HR
                        svf.VrsOutn("SP_EVENT2", line, getDispNum(student._at94seme9, "0")); // 特活行事
                        svf.VrsOutn("SP_TOTAL2", line, getDispNum(addNumber(student._at93seme9, student._at94seme9), "0")); // 特活計
                    }

                    String markFugoukaku = "否";
                    final boolean zenkisotuTenTaigaku = "1".equals(student._grdDiv) && SEME1.equals(student._grdDateSemester) || "2".equals(student._grdDiv) || "3".equals(student._grdDiv);
                    if (zenkisotuTenTaigaku && null != student._grdDate) {
                        final Calendar grdDateCal = Calendar.getInstance();
                        grdDateCal.setTime(Date.valueOf(student._grdDate));
                        final Calendar loginDateCal = Calendar.getInstance();
                        loginDateCal.setTime(Date.valueOf(_param._loginDate));
                        if (grdDateCal.before(loginDateCal)) {
                            markFugoukaku = null;
                        }
                    }

                    // 科目ごとに行表示
                    final List<Subclass> printSubclassList;
                    if (_param._isFukuiken) {
                        printSubclassList = new ArrayList<Subclass>();
                        for (final Subclass subclass : student._subclassList) {
                            if (dan == 1) {
                                if (_param.isZenkiKamoku(subclass)) {
                                    printSubclassList.add(subclass);
                                }
                            } else if (dan == 2) {
                                if (_param.isKoukiKamoku(subclass)) {
                                    printSubclassList.add(subclass);
                                }
                            }
                        }
                    } else {
                        printSubclassList = student._subclassList;
                    }
                    for (int subline = 0; subline < Math.min(printSubclassList.size(), 11); subline++) {
                        final Subclass subclass = printSubclassList.get(subline);
                        final int subcline = subline + 1;
                        final String suf3 = getMS932ByteLength(subclass._subclassname) > 30 ? "3" : getMS932ByteLength(subclass._subclassname) > 20 ? "2" : "1";
                        svf.VrsOutn("SUBCLASS_NAME" + subcline + "_" + suf3, line, subclass._subclassname); // 科目名

                        svf.VrsOutn("CREDIT" + subcline, line, subclass._credits); // 単位

                        final String defVal = KOUNIN.equals(subclass._kind) || ZOUTAN.equals(subclass._kind) ? null : SEMEALL.equals(_param._semester) ? "0" : null;

                        if (!_param.isKoukiKamoku(subclass)) {
                            svf.VrsOutn("REP_SUC" + subcline + "_1", line, StringUtils.defaultString(subclass._repgou1Count, defVal)); // レポート合格数
                            svf.VrsOutn("INT_ATTEND" + subcline + "_1", line, getDispNum(subclass._at1shussekiCount, defVal)); // 面接出席
                            svf.VrsOutn("INT_BROAD" + subcline + "_1", line, getDispNum(subclass._at1housouCountOrg, null)); // 面接放送視聴
                            svf.VrsOutn("SCORE" + subcline + "_1", line, subclass._sem1IntrValue); // 試験
                            svf.VrsOutn("LEAD" + subcline + "_1", line, subclass._sem1TermValue); // 補充指導
                        }

                        if (_param.isPrintKouki() && !_param.isZenkiKamoku(subclass)) {
                            // 後期
                            svf.VrsOutn("REP_SUC" + subcline + "_2", line, StringUtils.defaultString(subclass._repgou9Count, defVal)); // レポート合格数
                            svf.VrsOutn("INT_ATTEND" + subcline + "_2", line, getDispNum(subclass._at9shussekiCount, defVal)); // 面接出席
                            svf.VrsOutn("INT_BROAD" + subcline + "_2", line, getDispNum(subclass._at9housouCountOrg, null)); // 面接放送視聴
                            // 年間
                            svf.VrsOutn("SCORE" + subcline + "_3", line, subclass._sem2IntrValue); // 試験
                            svf.VrsOutn("LEAD" + subcline + "_3", line, subclass._sem2TermValue); // 補充指導
                        }
                        if (_param.isPrintGakunenmatsu() && !"8".equals(student._inoutcd)) { // 内外区分='8'(聴講生)はブランク
                            // 学年末
                            svf.VrsOutn("LAST_SCORE" + subcline, line, subclass._gradValue2); // 成績
                            svf.VrsOutn("LAST_VALUE" + subcline, line, subclass._gradValue); // 評定
                            svf.VrsOutn("LAST_INTERVIEW" + subcline, line, getDispNum(subclass._at9totalCount, defVal)); // 面接時数
                            svf.VrsOutn("LAST_CREDIT" + subcline, line, null == subclass._getCredit ? "0" : subclass._getCredit); // 単位
                            final String mark;
                            if (null == subclass._getCredit || Integer.parseInt(subclass._getCredit) == 0) {
                                mark = markFugoukaku; // ログイン日付以前に転学退学した生徒はブランク
                            } else {
                                mark = "合";
                            }
                            svf.VrsOutn("LAST_JUDGE" + subcline, line, mark); // 合否
                        }
                        String remark = "", space = "";
                        if (subline == 0 && (zenkisotuTenTaigaku && null != student._grdDivName)) { // 1行目に転学 or 退学
                            remark += space + (null == student._grdDate ? "" : KNJ_EditDate.h_format_JP(db2, student._grdDate) + "付") + student._grdDivName;
                            space = " ";
                        }
                        if (KOUNIN.equals(subclass._kind)) {
                            remark += space + "高認";
                            space = " ";
                        }
                        if (ZOUTAN.equals(subclass._kind)) {
                            remark += space + "技能審査";
                            space = " ";
                        }
                        svf.VrsOutn("REMARK" + subcline, line, remark); // 備考
                    }
                }

                _hasData = true;
            }
            svf.VrEndPage();
        }
    }

    private <T> List<List<T>> getPageList(final List<T> list, final int size) {
        final List<List<T>> pagelist = new ArrayList();
        List<T> current = null;
        for (final T o : list) {
            if (null == current || current.size() >= size) {
                current = new ArrayList();
                pagelist.add(current);
            }
            current.add(o);
        }
        return pagelist;
    }

    private List<Student> getStudentList(final DB2UDB db2) {
        final List<Student> list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
             final String studentSql = getStudentSql();
             log.debug(" student sql = " + studentSql);
             ps = db2.prepareStatement(studentSql);
             rs = ps.executeQuery();
             while (rs.next()) {
                 final String schregno = rs.getString("SCHREGNO");
                 final String name = rs.getString("NAME");
                 final String inoutcd = rs.getString("INOUTCD");
                 final String sotuyo = rs.getString("BASE_REMARK1");
                 final String kakuteiKonnendoTanni = rs.getString("KAKUTEI_KONNENDO_TANNI");
                 final String tourokuKonnendoTanni = rs.getString("TOUROKU_KONNENDO_TANNI");
                 final String shutokuzumiTanni = addNumber(rs.getString("SHUTOKUZUMI_TANNI"), rs.getString("SHUTOKUZUMI_TANNI_KASAN"));
                 final String zenkiScore = rs.getString("ZENKI_SCORE");
                 final String zenkiCount = rs.getString("ZENKI_COUNT");
                 final String koukiScore = rs.getString("KOUKI_SCORE");
                 final String koukiCount = rs.getString("KOUKI_COUNT");
                 final String at93seme1 = rs.getString("AT93SEME1");
                 final String at94seme1 = rs.getString("AT94SEME1");
                 final String at93seme9 = rs.getString("AT93SEME9");
                 final String at94seme9 = rs.getString("AT94SEME9");
                 final String entDiv = rs.getString("ENT_DIV");
                 final String entDivName = rs.getString("ENT_DIV_NAME");
                 final String entDate = rs.getString("ENT_DATE");
                 final String entDateSemester = rs.getString("ENT_DATE_SEMESTER");
                 final String grdDiv = rs.getString("GRD_DIV");
                 final String grdDate = rs.getString("GRD_DATE");
                 final String grdDateSemester = rs.getString("GRD_DATE_SEMESTER");
                 final String grdDivName;
                 if ("1".equals(grdDiv) && SEME1.equals(grdDateSemester)) {
                     grdDivName = StringUtils.defaultString(rs.getString("GRD_DATE_SEMESTERNAME")) + StringUtils.defaultString(rs.getString("GRD_DIV_NAME"));
                 } else {
                     grdDivName = rs.getString("GRD_DIV_NAME");
                 }
                 final String trcd1Staffname = rs.getString("TR_CD1_STAFFNAME");
                 final Student student = new Student(schregno, name, inoutcd, sotuyo, kakuteiKonnendoTanni, tourokuKonnendoTanni, shutokuzumiTanni, zenkiScore, zenkiCount, koukiScore, koukiCount, at93seme1, at94seme1, at93seme9, at94seme9, entDiv, entDivName, entDate, entDateSemester, grdDiv, grdDivName, grdDate, grdDateSemester, trcd1Staffname);
                 list.add(student);
             }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }

        try {
            ps = db2.prepareStatement(getSchAttendSql());
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student student = getStudent(list, rs.getString("SCHREGNO"));
                if (null == student) {
                    continue;
                }
                student._attendList.add(new Attend(rs.getString("SEMESTER"), rs.getString("SCHOOLINGKINDCD"), rs.getString("NAMESPARE1"), rs.getString("EXECUTEDATE"), rs.getString("PERIODCD"), rs.getBigDecimal("CREDIT_TIME")));
            }

        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }

        try {
             final String subclassSql = getSubclassSql();
             log.info(" subclass sql = " + subclassSql);
             ps = db2.prepareStatement(subclassSql);
             rs = ps.executeQuery();
             while (rs.next()) {
                 final Student student = getStudent(list, rs.getString("SCHREGNO"));
                 if (null == student) {
                     continue;
                 }

                 final String year = rs.getString("YEAR");
                 final String classcd = rs.getString("CLASSCD");
                 final String schoolKind = rs.getString("SCHOOL_KIND");
                 final String curriculumCd = rs.getString("CURRICULUM_CD");
                 final String subclasscd = rs.getString("SUBCLASSCD");
                 final String subclassname = rs.getString("SUBCLASSNAME");
                 final String schSeqMin = rs.getString("SCH_SEQ_MIN");
                 final String zenkiFlg = rs.getString("ZENKI_FLG");
                 final String koukiFlg = rs.getString("KOUKI_FLG");
                 final String credits = rs.getString("CREDITS");

                 final String at11Count = rs.getString("AT11_COUNT");
                 final String at13Count = rs.getString("AT13_COUNT");
                 final String at1shussekiCount = sum(new String[] {at11Count, at13Count});
                 final String at91Count = rs.getString("AT91_COUNT");
                 final String at93Count = rs.getString("AT93_COUNT");
                 final String at9shussekiCount = sum(new String[] {at91Count, at93Count});

                 String at1housouCountOrg = rs.getString("AT12_COUNT_ORG");
                 String at9housouCountOrg = rs.getString("AT92_COUNT_ORG");
                 String at1housouCount = rs.getString("AT12_COUNT");
                 String at9housouCount = rs.getString("AT92_COUNT");
                 if (NumberUtils.isNumber(schSeqMin)) {
                     // 最低出席回数 - スクーリング数（スクーリング種別2以外）の合計 = 不足のスクーリング数 (マイナスの場合は0とする)
                     //  不足のスクーリング数 >= 放送で認められる数 の場合は放送で認められる数を加算する。
                     //  不足のスクーリング数 <  放送で認められる数 の場合は不足のスクーリング数を加算する。
                     final int div6 = Integer.parseInt(schSeqMin) * Integer.parseInt(StringUtils.defaultString(_param._m020Name1, "6")) / 10;
                     final double at1shusseki = NumberUtils.isNumber(at1shussekiCount) ? Double.parseDouble(at1shussekiCount) : 0;
                     final double housouDeMitomerareruMax1 = Math.min(div6, Math.max(0.0, Integer.parseInt(schSeqMin) - at1shusseki)); // 放送で認められる最大数
                     if (NumberUtils.isNumber(at1housouCount) && Double.parseDouble(at1housouCount) > housouDeMitomerareruMax1) {
                         at1housouCount = String.valueOf(Integer.parseInt(schSeqMin) - housouDeMitomerareruMax1);
                     }
                     final double at9shusseki = NumberUtils.isNumber(at9shussekiCount) ? Double.parseDouble(at9shussekiCount) : 0;
                     final double housouDeMitomerareruMax9 = Math.min(div6, Math.max(0.0, Integer.parseInt(schSeqMin) - at9shusseki)); // 放送で認められる最大数
                     if (NumberUtils.isNumber(at9housouCount) && Double.parseDouble(at9housouCount) > housouDeMitomerareruMax9) {
                         at9housouCount = String.valueOf(housouDeMitomerareruMax9);
                     }
                 }
                 if (NumberUtils.isNumber(at1housouCountOrg) && 0.0 == Double.parseDouble(at1housouCountOrg)) at1housouCountOrg = null;
                 if (NumberUtils.isNumber(at9housouCountOrg) && 0.0 == Double.parseDouble(at9housouCountOrg)) at9housouCountOrg = null;
                 if (NumberUtils.isNumber(at1housouCount) && 0.0 == Double.parseDouble(at1housouCount)) at1housouCount = null;
                 if (NumberUtils.isNumber(at9housouCount) && 0.0 == Double.parseDouble(at9housouCount)) at9housouCount = null;

                 final String at9totalCount = sum(new String[] {at91Count, at9housouCount, at93Count});
                 final String repgou1Count = rs.getString("REPGOU1_COUNT");
                 final String repgou9Count = rs.getString("REPGOU9_COUNT");
                 final String sem1IntrValue = rs.getString("SEM1_INTR_VALUE");
                 final String sem1TermValue = rs.getString("SEM1_TERM_VALUE");
                 final String sem2IntrValue = rs.getString("SEM2_INTR_VALUE");
                 final String sem2TermValue = rs.getString("SEM2_TERM_VALUE");
                 final String gradValue2 = rs.getString("GRAD_VALUE2");
                 final String gradValue = rs.getString("GRAD_VALUE");
                 final String getCredit = rs.getString("GET_CREDIT");
                 final String kind = rs.getString("KIND");
                 final Subclass subclass = new Subclass(year, classcd, schoolKind, curriculumCd, subclasscd, subclassname, zenkiFlg, koukiFlg, credits, at1shussekiCount, at1housouCountOrg, repgou1Count, at9shussekiCount, at9housouCountOrg, at9totalCount, repgou9Count, sem1IntrValue, sem1TermValue, sem2IntrValue, sem2TermValue, gradValue2, gradValue, getCredit, kind);
                 student._subclassList.add(subclass);
             }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }
        return list;
    }

    private static String sum(final String[] nums) {
        String sum = null;
        for (int i = 0; i < nums.length; i++) {
            if (null == sum) {
                sum = nums[i];
            } else {
                sum = String.valueOf((Double.parseDouble(NumberUtils.isNumber(sum) ? sum : "0.0")) +
                                     (Double.parseDouble(NumberUtils.isNumber(nums[i]) ? nums[i] : "0.0")));
            }
        }
        return sum;
    }

    private String getSchAttendSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T2.SEMESTER, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.SCHOOLINGKINDCD, ");
        stb.append("         T4.NAMESPARE1, ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CREDIT_TIME ");
        stb.append("     FROM ");
        stb.append("         SCH_ATTEND_DAT T1 ");
        stb.append("         INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.SEMESTER <> '9' ");
        stb.append("             AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("         INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("             AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("             AND T3.CHAIRCD = T1.CHAIRCD ");
        stb.append("         INNER JOIN NAME_MST T4 ON T4.NAMECD1 = 'M001' ");
        stb.append("             AND T4.NAMECD2 = T1.SCHOOLINGKINDCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("         AND T4.NAMESPARE1 = '1' ");
        stb.append(" UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.SEMESTER, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         CAST(NULL AS VARCHAR(1)) AS SCHOOLINGKINDCD, ");
        stb.append("         CAST(NULL AS VARCHAR(1)) AS NAMESPARE1, ");
        stb.append("         T1.ATTENDDATE AS EXECUTEDATE, ");
        stb.append("         T1.PERIODF AS PERIODCD, ");
        stb.append("         T1.CREDIT_TIME ");
        stb.append("     FROM ");
        stb.append("         SPECIALACT_ATTEND_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     SCHREGNO, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOLINGKINDCD, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS NAMESPARE1, ");
        stb.append("     INPUT_DATE AS EXECUTEDATE, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS PERIODCD, ");
        stb.append("     CAST(NULL AS DECIMAL(5, 1)) AS CREDIT_TIME ");
        stb.append(" FROM ");
        stb.append("     TEST_ATTEND_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     SCHREGNO, ");
        stb.append("     INPUT_DATE ");
        return stb.toString();
    }

    private static class Attend {
        final String _semester;
        final String _schoolingkindcd;
        final String _namespare1;
        final String _executedate;
        final String _periodcd;
        final BigDecimal _creditTime;
        public Attend(final String semester, final String schoolingkindcd, final String namespare1, final String executedate, final String periodcd, final BigDecimal creditTime) {
            _semester = semester;
            _schoolingkindcd = schoolingkindcd;
            _namespare1 = namespare1;
            _executedate = executedate;
            _periodcd = periodcd;
            _creditTime = creditTime;
        }
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("    WITH SCHREGNOS AS ( ");
        stb.append("        SELECT ");
        stb.append("            T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, T2.SCHOOL_KIND, T4.STAFFNAME AS TR_CD1_STAFFNAME ");
        stb.append("        FROM ");
        stb.append("            SCHREG_REGD_DAT T1 ");
        stb.append("            LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
        stb.append("            LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("            LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = T3.TR_CD1 ");
        stb.append("        WHERE ");
        stb.append("            T1.YEAR = '" + _param._year + "' ");
        stb.append("            AND T1.SEMESTER = '" + _param.getRegdSemester() + "' ");
        stb.append("            AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("    ), SPECIAL_ATTEND AS ( ");
        stb.append("        SELECT ");
        stb.append("            VALUE(SEMESTER, '9') AS SEMESTER, SCHREGNO, CLASSCD, SUM(CREDIT_TIME) AS CREDIT_TIME ");
        stb.append("        FROM ");
        stb.append("            SPECIALACT_ATTEND_DAT T1 ");
        stb.append("        WHERE ");
        stb.append("            YEAR = '" + _param._year + "' ");
        stb.append("            AND CLASSCD IN ('93', '94') ");
        stb.append("        GROUP BY ");
        stb.append("            GROUPING SETS((SCHREGNO, CLASSCD), (SEMESTER, SCHREGNO, CLASSCD)) ");
        stb.append("    ), THIS_YEAR_SUBCLASS AS ( ");
        stb.append("        SELECT DISTINCT ");
        stb.append("            T1.YEAR, T1.SCHREGNO, CLASSCD, T1.SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        if (_param._isFukuiken) {
            stb.append("        FROM RECORD_SCORE_HIST_DAT T1 ");
        } else {
            stb.append("        FROM RECORD_DAT T1 ");
        }
        stb.append("        INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("            AND T2.YEAR = T1.YEAR ");
        stb.append("    ), TANNI_RUIKEI AS ( ");
        stb.append("        SELECT ");
        stb.append("            'KAKUTEI_KONNENDO' AS DIV, T1.SCHREGNO, ");
        stb.append("            SUM((CASE WHEN T4.ADD_CREDIT IS NULL THEN T4.GET_CREDIT  ");
        stb.append("                           ELSE VALUE(T4.ADD_CREDIT, 0) + VALUE(T4.GET_CREDIT, 0) END) ");
        stb.append("                      ) AS GET_CREDIT ");
        stb.append("        FROM ");
        stb.append("            THIS_YEAR_SUBCLASS T1 ");
        stb.append("            INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        if (_param._isFukuiken) {
            stb.append("            LEFT JOIN RECORD_SCORE_HIST_DAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("                AND T4.SEMESTER = '9' ");
            stb.append("                AND T4.TESTKINDCD = '99' ");
            stb.append("                AND T4.TESTITEMCD = '00' ");
            stb.append("                AND T4.SCORE_DIV = '09' ");
            stb.append("                AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("                AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("                AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("                AND T4.SCHREGNO = T1.SCHREGNO ");
        } else {
            stb.append("            LEFT JOIN RECORD_DAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("                AND T4.CLASSCD = T1.CLASSCD ");
            stb.append("                AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("                AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("                AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("                AND T4.SCHREGNO = T1.SCHREGNO ");
        }
        stb.append("        GROUP BY ");
        stb.append("            T1.SCHREGNO ");
        stb.append("        UNION ALL ");
        stb.append("        SELECT ");
        stb.append("            'SHUTOKUZUMI' AS DIV, T1.SCHREGNO, ");
        stb.append("             SUM(CASE WHEN T1.ADD_CREDIT IS NULL THEN T1.GET_CREDIT  ");
        stb.append("                      ELSE VALUE(T1.ADD_CREDIT, 0) + VALUE(T1.GET_CREDIT, 0) ");
        stb.append("             END) AS GET_CREDIT ");
        stb.append("        FROM ");
        stb.append("            SCHREG_STUDYREC_DAT T1 ");
        stb.append("            INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("        WHERE ");
        stb.append("            T1.YEAR < '" + _param._year + "' ");
        stb.append("            AND T1.SCHOOLCD <> '1' ");
        stb.append("        GROUP BY ");
        stb.append("            T1.SCHREGNO ");
        stb.append("        UNION ALL ");
        stb.append("        SELECT ");
        stb.append("            'ZENSEKIKOU' AS DIV, T1.SCHREGNO, ");
        stb.append("             SUM(CASE WHEN T1.ADD_CREDIT IS NULL THEN T1.GET_CREDIT  ");
        stb.append("                      ELSE VALUE(T1.ADD_CREDIT, 0) + VALUE(T1.GET_CREDIT, 0) ");
        stb.append("             END) AS GET_CREDIT ");
        stb.append("        FROM ");
        stb.append("            SCHREG_STUDYREC_DAT T1 ");
        stb.append("            INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("        WHERE ");
        stb.append("            T1.SCHOOLCD = '1' ");
        stb.append("        GROUP BY ");
        stb.append("            T1.SCHREGNO ");
        stb.append("        UNION ALL ");
        stb.append("        SELECT ");
        stb.append("            'SATEI' AS DIV, T1.SCHREGNO, ");
        stb.append("             SMALLINT(BASE_REMARK1) AS GET_CREDIT ");
        stb.append("        FROM ");
        stb.append("            SCHREG_BASE_DETAIL_MST T1 ");
        stb.append("        WHERE ");
        stb.append("            T1.BASE_SEQ = '004' ");
        stb.append("        UNION ALL ");
        stb.append("        SELECT ");
        stb.append("            'TOUROKU_KONNENDO' AS DIV, T1.SCHREGNO, ");
        stb.append("             SUM(CREDITS) AS GET_CREDIT ");
        stb.append("        FROM ");
        stb.append("            SUBCLASS_STD_SELECT_DAT T1 ");
        stb.append("            INNER JOIN SCHREGNOS T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("                AND T3.YEAR = T1.YEAR ");
        stb.append("                AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("            INNER JOIN CREDIT_MST T2 ON T2.YEAR = T3.YEAR ");
        stb.append("                AND T2.GRADE = T3.GRADE ");
        stb.append("                AND T2.COURSECD = T3.COURSECD ");
        stb.append("                AND T2.MAJORCD = T3.MAJORCD ");
        stb.append("                AND T2.COURSECODE = T3.COURSECODE ");
        stb.append("                AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("                AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("                AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("                AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("        GROUP BY ");
        stb.append("            T1.SCHREGNO ");
        stb.append("    ), SHIKEN_KAMOKU AS ( ");
        stb.append("        SELECT ");
        stb.append("            T1.SCHREGNO, T1.YEAR, T1.SEMESTER, ");
        stb.append("            T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD ");
        stb.append("        FROM ");
        stb.append("            CHAIR_STD_DAT T1 ");
        stb.append("            INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("                AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("                AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("            INNER JOIN SCHREGNOS T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("                AND T3.YEAR = T1.YEAR ");
        if (!_param._isFukuiken) {
            stb.append("            INNER JOIN RECORD_CHKFIN_DAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("                AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("                AND T4.CLASSCD = T2.CLASSCD ");
            stb.append("                AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("                AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("                AND T4.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("                AND T4.TESTKINDCD = '99' ");
            stb.append("                AND T4.TESTITEMCD = '00' ");
            stb.append("                AND T4.RECORD_DIV = '2' ");
        }
        stb.append("        WHERE ");
        stb.append("            T1.SEMESTER <= '" + _param._semester + "' ");
        stb.append("    ), HEIKIN AS ( ");
        stb.append("        SELECT ");
        stb.append("            T1.SCHREGNO, 'ZENKI' AS DIV, ");
        if (_param._isFukuiken) {
            stb.append("            SUM(T2.VALUE) AS SCORE, ");
        } else {
            stb.append("            SUM(T2.SEM1_INTR_VALUE) AS SCORE, ");
        }
        stb.append("            COUNT(*) AS COUNT ");
        stb.append("        FROM SHIKEN_KAMOKU T1 ");
        if (_param._isFukuiken) {
            stb.append("        INNER JOIN RECORD_SCORE_HIST_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("            AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("            AND T2.TESTKINDCD = '99' AND T2.TESTITEMCD = '00' AND T2.SCORE_DIV = '08' ");
            stb.append("            AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("            AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("            AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("            AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("            AND T2.SEQ = 1 ");
            stb.append("            AND T2.SCHREGNO = T1.SCHREGNO ");
        } else {
            stb.append("        INNER JOIN RECORD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("            AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("            AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("            AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("            AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("            AND T2.SCHREGNO = T1.SCHREGNO ");
        }
        stb.append("        WHERE ");
        stb.append("            T1.SEMESTER = '1' ");
        if (_param._isFukuiken) {
            stb.append("            AND T2.VALUE IS NOT NULL ");
        } else {
            stb.append("            AND T2.SEM1_INTR_VALUE IS NOT NULL ");
        }
        stb.append("        GROUP BY ");
        stb.append("            T1.SCHREGNO ");
        stb.append("        UNION ALL ");
        stb.append("        SELECT ");
        stb.append("            T1.SCHREGNO, 'KOUKI' AS DIV, ");
        if (_param._isFukuiken) {
            stb.append("            SUM(T2.VALUE) AS SCORE, ");
        } else {
            stb.append("            SUM(T2.SEM2_INTR_VALUE) AS SCORE, ");
        }
        stb.append("            COUNT(*) AS COUNT ");
        stb.append("        FROM SHIKEN_KAMOKU T1 ");
        if (_param._isFukuiken) {
            stb.append("        INNER JOIN RECORD_SCORE_HIST_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("            AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("            AND T2.TESTKINDCD = '99' AND T2.TESTITEMCD = '00' AND T2.SCORE_DIV = '08' ");
            stb.append("            AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("            AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("            AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("            AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("            AND T2.SEQ = 1 ");
            stb.append("            AND T2.SCHREGNO = T1.SCHREGNO ");
        } else {
            stb.append("        INNER JOIN RECORD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("            AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("            AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("            AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("            AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("            AND T2.SCHREGNO = T1.SCHREGNO ");
        }
        stb.append("        WHERE ");
        stb.append("            T1.SEMESTER = '2' ");
        if (_param._isFukuiken) {
            stb.append("            AND T2.VALUE IS NOT NULL ");
        } else {
            stb.append("            AND T2.SEM2_INTR_VALUE IS NOT NULL ");
        }
        stb.append("        GROUP BY ");
        stb.append("            T1.SCHREGNO ");
        stb.append("    ) ");
        stb.append("    SELECT ");
        stb.append("        T1.SCHREGNO, ");
        stb.append("        T2.NAME, ");
        stb.append("        T2.INOUTCD, ");
        stb.append("        T7.BASE_REMARK1, ");
        stb.append("        KAKUTEI_KONNENDO.GET_CREDIT AS KAKUTEI_KONNENDO_TANNI, ");
        stb.append("        TOUROKU_KONNENDO.GET_CREDIT AS TOUROKU_KONNENDO_TANNI, ");
        stb.append("        SHUTOKUZUMI.GET_CREDIT AS SHUTOKUZUMI_TANNI, ");
        stb.append("        VALUE(ZENSEKIKOU.GET_CREDIT, SATEI.GET_CREDIT) AS SHUTOKUZUMI_TANNI_KASAN, ");
        stb.append("        H1.SCORE AS ZENKI_SCORE, ");
        stb.append("        H1.COUNT AS ZENKI_COUNT, ");
        stb.append("        H2.SCORE AS KOUKI_SCORE, ");
        stb.append("        H2.COUNT AS KOUKI_COUNT, ");
        stb.append("        AT93SEM1.CREDIT_TIME AS AT93SEME1, ");
        stb.append("        AT94SEM1.CREDIT_TIME AS AT94SEME1, ");
        stb.append("        AT93SEM9.CREDIT_TIME AS AT93SEME9, ");
        stb.append("        AT94SEM9.CREDIT_TIME AS AT94SEME9, ");
        stb.append("        T8.ENT_DIV, ");
        stb.append("        NMA002.NAME1 AS ENT_DIV_NAME, ");
        stb.append("        T8.ENT_DATE, ");
        stb.append("        T10.SEMESTER AS ENT_DATE_SEMESTER, ");
        stb.append("        T10.SEMESTERNAME AS ENT_DATE_SEMESTERNAME, ");
        stb.append("        T8.GRD_DIV, ");
        stb.append("        NMA003.NAME1 AS GRD_DIV_NAME, ");
        stb.append("        T8.GRD_DATE, ");
        stb.append("        T9.SEMESTER AS GRD_DATE_SEMESTER, ");
        stb.append("        T9.SEMESTERNAME AS GRD_DATE_SEMESTERNAME, ");
        stb.append("        T1.TR_CD1_STAFFNAME ");
        stb.append("    FROM ");
        stb.append("        SCHREGNOS T1 ");
        stb.append("        INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("        LEFT JOIN TANNI_RUIKEI KAKUTEI_KONNENDO ON KAKUTEI_KONNENDO.SCHREGNO = T1.SCHREGNO AND KAKUTEI_KONNENDO.DIV = 'KAKUTEI_KONNENDO' ");
        stb.append("        LEFT JOIN TANNI_RUIKEI TOUROKU_KONNENDO ON TOUROKU_KONNENDO.SCHREGNO = T1.SCHREGNO AND TOUROKU_KONNENDO.DIV = 'TOUROKU_KONNENDO' ");
        stb.append("        LEFT JOIN TANNI_RUIKEI SHUTOKUZUMI ON SHUTOKUZUMI.SCHREGNO = T1.SCHREGNO AND SHUTOKUZUMI.DIV = 'SHUTOKUZUMI' ");
        stb.append("        LEFT JOIN TANNI_RUIKEI ZENSEKIKOU ON ZENSEKIKOU.SCHREGNO = T1.SCHREGNO AND ZENSEKIKOU.DIV = 'ZENSEKIKOU' ");
        stb.append("        LEFT JOIN TANNI_RUIKEI SATEI ON SATEI.SCHREGNO = T1.SCHREGNO AND SATEI.DIV = 'SATEI' ");
        stb.append("        LEFT JOIN HEIKIN H1 ON H1.SCHREGNO = T1.SCHREGNO AND H1.DIV = 'ZENKI' ");
        stb.append("        LEFT JOIN HEIKIN H2 ON H2.SCHREGNO = T1.SCHREGNO AND H2.DIV = 'KOUKI' ");
        stb.append("        LEFT JOIN SPECIAL_ATTEND AT93SEM1 ON AT93SEM1.SEMESTER = '1' AND AT93SEM1.SCHREGNO = T1.SCHREGNO AND AT93SEM1.CLASSCD = '93' ");
        stb.append("        LEFT JOIN SPECIAL_ATTEND AT94SEM1 ON AT94SEM1.SEMESTER = '1' AND AT94SEM1.SCHREGNO = T1.SCHREGNO AND AT94SEM1.CLASSCD = '94' ");
        stb.append("        LEFT JOIN SPECIAL_ATTEND AT93SEM9 ON AT93SEM9.SEMESTER = '9' AND AT93SEM9.SCHREGNO = T1.SCHREGNO AND AT93SEM9.CLASSCD = '93' ");
        stb.append("        LEFT JOIN SPECIAL_ATTEND AT94SEM9 ON AT94SEM9.SEMESTER = '9' AND AT94SEM9.SCHREGNO = T1.SCHREGNO AND AT94SEM9.CLASSCD = '94' ");
        stb.append("        LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST T7 ON T7.SCHREGNO = T1.SCHREGNO ");
        stb.append("            AND T7.YEAR = T1.YEAR ");
        stb.append("            AND T7.BASE_SEQ = '001' ");
        stb.append("        LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T8 ON T8.SCHREGNO = T1.SCHREGNO ");
        stb.append("            AND T8.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("        LEFT JOIN SEMESTER_MST T9 ON T9.YEAR = T1.YEAR ");
        stb.append("            AND T9.SEMESTER <> '9' ");
        stb.append("            AND T8.GRD_DATE BETWEEN T9.SDATE AND T9.EDATE ");
        stb.append("        LEFT JOIN SEMESTER_MST T10 ON T10.YEAR = T1.YEAR ");
        stb.append("            AND T10.SEMESTER <> '9' ");
        stb.append("            AND T8.ENT_DATE BETWEEN T10.SDATE AND T10.EDATE ");
        stb.append("        LEFT JOIN NAME_MST NMA002 ON NMA002.NAMECD1 = 'A002' AND NMA002.NAMECD2 = T8.ENT_DIV ");
        stb.append("        LEFT JOIN NAME_MST NMA003 ON NMA003.NAMECD1 = 'A003' AND NMA003.NAMECD2 = T8.GRD_DIV ");
        stb.append("    ORDER BY ");
        stb.append("        T1.SCHREGNO ");
        return stb.toString();
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _inoutcd;
        final String _baseRemark1;
        final String _kakuteiKonnendoTanni;
        final String _tourokuKonnendoTanni;
        final String _shutokuzumiTanni;
        final String _zenkiScore;
        final String _zenkiCount;
        final String _koukiScore;
        final String _koukiCount;
        final String _at93seme1;
        final String _at94seme1;
        final String _at93seme9;
        final String _at94seme9;
        final String _entDiv;
        final String _entDivName;
        final String _entDate;
        final String _entDateSemester;
        final String _grdDiv;
        final String _grdDivName;
        final String _grdDate;
        final String _grdDateSemester;
        final String _trcd1Staffname;
        final List<Subclass> _subclassList = new ArrayList();
        final List<Attend> _attendList = new ArrayList();

        Student(
                final String schregno,
                final String name,
                final String inoutcd,
                final String baseRemark1,
                final String kakuteiKonnendoTanni,
                final String tourokuKonnendoTanni,
                final String shutokuzumiTanni,
                final String zenkiScore,
                final String zenkiCount,
                final String koukiScore,
                final String koukiCount,
                final String at93seme1,
                final String at94seme1,
                final String at93seme9,
                final String at94seme9,
                final String entDiv,
                final String entDivName,
                final String entDate,
                final String entDateSemester,
                final String grdDiv,
                final String grdDivName,
                final String grdDate,
                final String grdDateSemester,
                final String trcd1Staffname) {
            _schregno = schregno;
            _name = name;
            _inoutcd = inoutcd;
            _baseRemark1 = baseRemark1;
            _kakuteiKonnendoTanni = kakuteiKonnendoTanni;
            _tourokuKonnendoTanni = tourokuKonnendoTanni;
            _shutokuzumiTanni = shutokuzumiTanni;
            _zenkiScore = zenkiScore;
            _zenkiCount = zenkiCount;
            _koukiScore = koukiScore;
            _koukiCount = koukiCount;
            _at93seme1 = at93seme1;
            _at94seme1 = at94seme1;
            _at93seme9 = at93seme9;
            _at94seme9 = at94seme9;
            _entDiv = entDiv;
            _entDivName = entDivName;
            _entDate = entDate;
            _entDateSemester = entDateSemester;
            _grdDiv = grdDiv;
            _grdDivName = grdDivName;
            _grdDate = grdDate;
            _grdDateSemester = grdDateSemester;
            _trcd1Staffname = trcd1Staffname;
        }

        /**
         * @param flg 1:登録時 2:確定時
         * @return 高認・増単の単位
         */
        public String getKougaiTanni(final int flg) {
            String rtn = null;
            for (final Subclass s : _subclassList) {
                if (KOUNIN.equals(s._kind) || ZOUTAN.equals(s._kind)) {
                    rtn = addNumber(rtn, TOUROKU == flg ? s._credits : KAKUTEI == flg ? s._getCredit : null);
                }
            }
            return rtn;
        }

        private static String getHeikin(final String score, final String count) {
            if (!NumberUtils.isDigits(score) || !NumberUtils.isDigits(count) || 0 == Integer.parseInt(count)) {
                return null;
            }
            final BigDecimal scoreBd = new BigDecimal(score);
            final BigDecimal countBd = new BigDecimal(count);
            final String val = scoreBd.divide(countBd, 1, BigDecimal.ROUND_HALF_UP).toString();
            log.debug(" heikin = " + scoreBd + " / " + countBd + " = " + val);
            return val;
        }

        public String getAttendNissu(final String semester, final String defStr) {
            final Set set = new HashSet();
            for (final Attend at : _attendList) {
                if (!SEMEALL.equals(semester) && !semester.equals(at._semester)) { // 9学期を指定する場合はすべてが対象
                    continue;
                }
                set.add(at._executedate);
            }
            return set.isEmpty() ? defStr : String.valueOf(set.size());
        }
    }

    private String getSubclassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREGNOS(SCHREGNO) AS ( ");
        String union = "";
        for (int i = 0; i < _param._categorySelected.length; i++) {
            stb.append(union);
            stb.append(" VALUES(CAST('" + _param._categorySelected[i] + "' AS VARCHAR(8)))");
            union = " UNION ALL ";
        }
        stb.append(" ), CHAIR AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.SEMESTER, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T2.REP_SEQ_ALL, ");
        stb.append("         T2.SCH_SEQ_ALL, ");
        stb.append("         T2.SCH_SEQ_MIN, ");
        stb.append("         CASE WHEN (T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHOOL_KIND) ");
        stb.append("                IN (SELECT NAME1, NAME2, NAME3, VALUE(ABBV1, 'H') FROM NAME_MST WHERE NAMECD1 = 'M015') THEN '1' ");
        stb.append("         END AS ZENKI_FLG, ");
        stb.append("         CASE WHEN (T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHOOL_KIND) ");
        stb.append("                IN (SELECT NAME1, NAME2, NAME3, VALUE(ABBV1, 'H') FROM NAME_MST WHERE NAMECD1 = 'M016') THEN '1' ");
        stb.append("         END AS KOUKI_FLG, ");
        stb.append("         MAX(T6.CREDITS) AS CREDITS, ");
        stb.append("         T3.SCHREGNO ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("       , T5.COURSECD ");
            stb.append("       , T5.MAJORCD ");
            stb.append("       , T5.COURSECODE ");
        }

        stb.append("     FROM ");
        stb.append("         CHAIR_DAT T1 ");
        stb.append("     INNER JOIN CHAIR_CORRES_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("     INNER JOIN CHAIR_STD_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T3.CHAIRCD = T1.CHAIRCD ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T5 ON T5.SCHREGNO = T3.SCHREGNO ");
        stb.append("         AND T5.YEAR = T1.YEAR ");
        stb.append("         AND T5.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN CREDIT_MST T6 ON T6.YEAR = T1.YEAR ");
        stb.append("         AND T6.COURSECD = T5.COURSECD ");
        stb.append("         AND T6.GRADE = T5.GRADE ");
        stb.append("         AND T6.MAJORCD = T5.MAJORCD ");
        stb.append("         AND T6.COURSECODE = T5.COURSECODE ");
        stb.append("         AND T6.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER <= '" + _param._semester + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("         AND EXISTS (SELECT 'X' FROM SCHREGNOS WHERE SCHREGNO = T3.SCHREGNO) ");
        stb.append("     GROUP BY ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.SEMESTER, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T2.REP_SEQ_ALL, ");
        stb.append("         T2.SCH_SEQ_ALL, ");
        stb.append("         T2.SCH_SEQ_MIN, ");
        stb.append("         T3.SCHREGNO ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("       , T5.COURSECD ");
            stb.append("       , T5.MAJORCD ");
            stb.append("       , T5.COURSECODE ");
        }

        stb.append(" ), CHAIR2 AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.REP_SEQ_ALL, ");
        stb.append("         T1.SCH_SEQ_ALL, ");
        stb.append("         T1.SCH_SEQ_MIN, ");
        stb.append("         T1.CREDITS, ");
        stb.append("         T1.ZENKI_FLG, ");
        stb.append("         T1.KOUKI_FLG, ");
        stb.append("         T1.SCHREGNO ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("       , T1.COURSECD ");
            stb.append("       , T1.MAJORCD ");
            stb.append("       , T1.COURSECODE ");
        }
        stb.append("     FROM ");
        stb.append("         CHAIR T1 ");

        stb.append(" ), KOUGAI AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T6.CREDITS, "); // 高認の場合、単位マスタの単位
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T2.CREDITS AS GET_CREDIT, ");
        stb.append("         '" + KOUNIN + "' AS KIND ");
        stb.append("     FROM ");
        stb.append("         SCH_COMP_DETAIL_DAT T1 ");
        stb.append("     LEFT JOIN ( SELECT ");
        stb.append("                     SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SUM(CREDITS) AS CREDITS ");
        stb.append("                 FROM SCHREG_QUALIFIED_DAT T2 ");
        stb.append("                 WHERE YEAR = '" + _param._year + "' ");
        stb.append("                       AND CONDITION_DIV = '3' ");
        stb.append("                       AND EXISTS (SELECT 'X' FROM SCHREGNOS WHERE SCHREGNO = T2.SCHREGNO) ");
        stb.append("                 GROUP BY ");
        stb.append("                     SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        stb.append("               ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("                   AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T5.YEAR = T1.YEAR ");
        stb.append("         AND T5.SEMESTER = '" + _param.getRegdSemester() + "' ");
        stb.append("     LEFT JOIN CREDIT_MST T6 ON T6.YEAR = T1.YEAR ");
        stb.append("         AND T6.COURSECD = T5.COURSECD ");
        stb.append("         AND T6.GRADE = T5.GRADE ");
        stb.append("         AND T6.MAJORCD = T5.MAJORCD ");
        stb.append("         AND T6.COURSECODE = T5.COURSECODE ");
        stb.append("         AND T6.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("         AND EXISTS (SELECT 'X' FROM SCHREGNOS WHERE SCHREGNO = T1.SCHREGNO) ");
        stb.append("         AND T1.KOUNIN = '1' ");
        stb.append(" UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.ADD_CREDIT AS CREDITS, "); // 増単の場合、ADD_CREDIT
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T2.CREDITS AS GET_CREDIT, ");
        stb.append("         '" + ZOUTAN + "' AS KIND ");
        stb.append("     FROM ");
        stb.append("         SCH_COMP_DETAIL_DAT T1 ");
        stb.append("         LEFT JOIN ( SELECT ");
        stb.append("                         SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SUM(CREDITS) AS CREDITS ");
        stb.append("                     FROM SCHREG_QUALIFIED_DAT T2 ");
        stb.append("                     WHERE YEAR = '" + _param._year + "' ");
        stb.append("                           AND CONDITION_DIV = '1' ");
        stb.append("                           AND EXISTS (SELECT 'X' FROM SCHREGNOS WHERE SCHREGNO = T2.SCHREGNO) ");
        stb.append("                     GROUP BY ");
        stb.append("                         SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        stb.append("                   ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("                       AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("         AND EXISTS (SELECT 'X' FROM SCHREGNOS WHERE SCHREGNO = T1.SCHREGNO) ");
        stb.append("         AND T1.KOUNIN IS NULL ");

        stb.append(" ), ATTEND_ALL AS ( ");
        stb.append("   SELECT  ");
        stb.append("       T1.YEAR,  ");
        stb.append("       T2.SEMESTER,  ");
        stb.append("       T3.CLASSCD,  ");
        stb.append("       T3.SCHOOL_KIND,  ");
        stb.append("       T3.CURRICULUM_CD,  ");
        stb.append("       T3.SUBCLASSCD,  ");
        stb.append("       T1.SCHREGNO,  ");
        stb.append("       T1.SCHOOLINGKINDCD,  ");
        stb.append("       T4.NAMESPARE1,  ");
        stb.append("       T1.EXECUTEDATE,  ");
        stb.append("       T1.PERIODCD,  ");
        stb.append("       T1.CREDIT_TIME, ");
        stb.append("       T3.SCH_SEQ_MIN ");
        stb.append("   FROM  ");
        stb.append("       SCH_ATTEND_DAT T1  ");
        stb.append("       INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR  ");
        stb.append("           AND T2.SEMESTER <> '9'  ");
        stb.append("           AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE  ");
        stb.append("       INNER JOIN CHAIR T3 ON T3.YEAR = T1.YEAR  ");
        stb.append("           AND T3.SEMESTER = T2.SEMESTER  ");
        stb.append("           AND T3.CHAIRCD = T1.CHAIRCD  ");
        stb.append("           AND T3.SCHREGNO = T1.SCHREGNO  ");
        stb.append("       LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'M001'  ");
        stb.append("           AND T4.NAMECD2 = T1.SCHOOLINGKINDCD  ");

        stb.append(" ), ATTEND_KIND1 AS ( ");
        stb.append("     SELECT  ");
        stb.append("         1 AS KIND, ");
        stb.append("         YEAR, VALUE(SEMESTER, '9') AS SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
        stb.append("         MAX(SCH_SEQ_MIN) AS SCH_SEQ_MIN, ");
        stb.append("         COUNT(DISTINCT EXECUTEDATE) AS JISU1, ");
        stb.append("         COUNT(DISTINCT EXECUTEDATE) AS JISU2 ");
        stb.append("     FROM ATTEND_ALL ");
        stb.append("     WHERE NAMESPARE1 = '1' AND SCHOOLINGKINDCD = '1' ");
        stb.append("     GROUP BY ");
        stb.append("         GROUPING SETS ((YEAR, SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO), ");
        stb.append("                        (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO)) ");
        stb.append(" ), ATTEND_KIND2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         2 AS KIND, ");
        stb.append("         YEAR, VALUE(SEMESTER, '9') AS SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
        stb.append("         SCH_SEQ_MIN, ");
        stb.append("         SCH_SEQ_MIN * INT(VALUE(L1.NAME1,'6')) / 10 AS LIMIT, ");
        stb.append("         SUM(CREDIT_TIME) AS JISU1, ");
        stb.append("         VALUE(INT(MIN(SCH_SEQ_MIN * INT(VALUE(L1.NAME1,'6')) / 10, SUM(CREDIT_TIME))), 0) AS JISU2 ");
        stb.append("     FROM ATTEND_ALL ");
        stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'M020' AND L1.NAMECD2 = '01'");
        stb.append("     WHERE SCHOOLINGKINDCD = '2' ");
        stb.append("     GROUP BY ");
        stb.append("         GROUPING SETS ((YEAR, SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, SCH_SEQ_MIN, L1.NAME1), ");
        stb.append("                        (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, SCH_SEQ_MIN, L1.NAME1)) ");
        stb.append(" ), ATTEND_KIND3 AS ( ");
        stb.append("     SELECT  ");
        stb.append("         3 AS KIND, ");
        stb.append("         YEAR, VALUE(SEMESTER, '9') AS SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
        stb.append("         MAX(SCH_SEQ_MIN) AS SCH_SEQ_MIN, ");
        stb.append("         SUM(CREDIT_TIME) AS JISU1, ");
        stb.append("         SUM(CREDIT_TIME) AS JISU2 ");
        stb.append("     FROM ATTEND_ALL ");
        stb.append("     WHERE NAMESPARE1 = '1' AND SCHOOLINGKINDCD <> '1' ");
        stb.append("     GROUP BY ");
        stb.append("         GROUPING SETS ((YEAR, SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, SCH_SEQ_MIN), ");
        stb.append("                        (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, SCH_SEQ_MIN)) ");

        stb.append(" ), MAX_REPRESENT_SEQ AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         MAX(T1.REPRESENT_SEQ) AS REPRESENT_SEQ ");
        stb.append("     FROM ");
        stb.append("         REP_PRESENT_DAT T1 ");
        stb.append("     INNER JOIN CHAIR T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");

        stb.append("     GROUP BY ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO ");
        stb.append(" ), MAX_RECEIPT_DATE AS ( ");
        stb.append("     SELECT  ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.REPRESENT_SEQ, ");
        stb.append("         MAX(T1.RECEIPT_DATE) AS RECEIPT_DATE ");
        stb.append("     FROM ");
        stb.append("         REP_PRESENT_DAT T1 ");
        stb.append("     INNER JOIN MAX_REPRESENT_SEQ T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ ");
        stb.append("     GROUP BY ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.REPRESENT_SEQ ");
        stb.append(" ), MAX_REP_PRESENT AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.REPRESENT_SEQ, ");
        stb.append("         T1.RECEIPT_DATE ");
        stb.append("     FROM ");
        stb.append("         REP_PRESENT_DAT T1 ");
        stb.append("     INNER JOIN MAX_RECEIPT_DATE T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND T2.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ ");
        stb.append("         AND T2.RECEIPT_DATE = T1.RECEIPT_DATE ");
        stb.append("     INNER JOIN NAME_MST T3 ON T3.NAMECD1 = 'M003' ");
        stb.append("         AND T3.NAMECD2 = T1.GRAD_VALUE ");
        stb.append("     WHERE ");
        stb.append("         T3.NAMESPARE1 = '1' ");
        stb.append(" ), REP_GOUKAKU_COUNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T2.SEMESTER, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SCHREGNO, ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         T3.COURSECD, ");
            stb.append("         T3.MAJORCD, ");
            stb.append("         T3.COURSECODE, ");
        }
        stb.append("         COUNT(*) AS COUNT ");
        stb.append("     FROM ");
        stb.append("         MAX_REP_PRESENT T1 ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         INNER JOIN REP_STANDARDDATE_COURSE_DAT T3 ON T3.YEAR = T1.YEAR ");
        } else {
            stb.append("         INNER JOIN REP_STANDARDDATE_DAT T3 ON T3.YEAR = T1.YEAR ");
        }
        stb.append("             AND T3.CLASSCD = T1.CLASSCD ");
        stb.append("             AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("             AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("             AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND T3.STANDARD_SEQ = T1.STANDARD_SEQ ");

        stb.append("         INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
        stb.append("             AND T3.STANDARD_DATE BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("     GROUP BY ");
        stb.append("         T1.YEAR, ");
        stb.append("         T2.SEMESTER, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SCHREGNO ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("       , T3.COURSECD ");
            stb.append("       , T3.MAJORCD ");
            stb.append("       , T3.COURSECODE ");
        }
        if (_param._isFukuiken) {
            stb.append(" ), RECORD_SCORE_HIST AS ( ");
            stb.append("   SELECT ");
            stb.append("       T1.YEAR, ");
            stb.append("       T1.CLASSCD, ");
            stb.append("       T1.SCHOOL_KIND, ");
            stb.append("       T1.CURRICULUM_CD, ");
            stb.append("       T1.SUBCLASSCD, ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       S1.VALUE   AS SEM1_INTR_VALUE, ");
            stb.append("       S1_2.VALUE AS SEM1_TERM_VALUE, ");
            stb.append("       S2.VALUE   AS SEM2_INTR_VALUE, ");
            stb.append("       S2_2.VALUE AS SEM2_TERM_VALUE, ");
            stb.append("       S98.VALUE  AS GRAD_VALUE2, ");
            stb.append("       S99.VALUE  AS GRAD_VALUE, ");
            stb.append("       S99.GET_CREDIT ");
            stb.append("   FROM ");
            stb.append("     CHAIR2 T1 ");
            stb.append("         LEFT JOIN RECORD_SCORE_HIST_DAT S1 ON S1.YEAR = T1.YEAR ");
            stb.append("             AND S1.SEMESTER = '1' ");
            stb.append("             AND S1.TESTKINDCD = '99' AND S1.TESTITEMCD = '00' AND S1.SCORE_DIV = '08' ");
            stb.append("             AND S1.CLASSCD = T1.CLASSCD ");
            stb.append("             AND S1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("             AND S1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("             AND S1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("             AND S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND S1.SEQ = 1 ");
            stb.append("         LEFT JOIN V_RECORD_SCORE_HIST_DAT S1_2 ON S1_2.YEAR = T1.YEAR ");
            stb.append("             AND S1_2.SEMESTER = '1' ");
            stb.append("             AND S1_2.TESTKINDCD = '99' AND S1_2.TESTITEMCD = '00' AND S1_2.SCORE_DIV = '08' ");
            stb.append("             AND S1_2.CLASSCD = T1.CLASSCD ");
            stb.append("             AND S1_2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("             AND S1_2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("             AND S1_2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("             AND S1_2.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND S1_2.SEQ <> 1 ");
            stb.append("         LEFT JOIN RECORD_SCORE_HIST_DAT S2 ON S2.YEAR = T1.YEAR ");
            stb.append("             AND S2.SEMESTER = '2' ");
            stb.append("             AND S2.TESTKINDCD = '99' AND S2.TESTITEMCD = '00' AND S2.SCORE_DIV = '08' ");
            stb.append("             AND S2.CLASSCD = T1.CLASSCD ");
            stb.append("             AND S2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("             AND S2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("             AND S2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("             AND S2.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND S2.SEQ = 1 ");
            stb.append("         LEFT JOIN V_RECORD_SCORE_HIST_DAT S2_2 ON S2_2.YEAR = T1.YEAR ");
            stb.append("             AND S2_2.SEMESTER = '2' ");
            stb.append("             AND S2_2.TESTKINDCD = '99' AND S2_2.TESTITEMCD = '00' AND S2_2.SCORE_DIV = '08' ");
            stb.append("             AND S2_2.CLASSCD = T1.CLASSCD ");
            stb.append("             AND S2_2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("             AND S2_2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("             AND S2_2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("             AND S2_2.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND S2_2.SEQ <> 1 ");
            stb.append("         LEFT JOIN V_RECORD_SCORE_HIST_DAT S98 ON S98.YEAR = T1.YEAR ");
            stb.append("             AND S98.SEMESTER = '9' ");
            stb.append("             AND S98.TESTKINDCD = '99' AND S98.TESTITEMCD = '00' AND S98.SCORE_DIV = '08' ");
            stb.append("             AND S98.CLASSCD = T1.CLASSCD ");
            stb.append("             AND S98.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("             AND S98.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("             AND S98.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("             AND S98.SCHREGNO = T1.SCHREGNO ");
            stb.append("         LEFT JOIN V_RECORD_SCORE_HIST_DAT S99 ON S99.YEAR = T1.YEAR ");
            stb.append("             AND S99.SEMESTER = '9' ");
            stb.append("             AND S99.TESTKINDCD = '99' AND S99.TESTITEMCD = '00' AND S99.SCORE_DIV = '09' ");
            stb.append("             AND S99.CLASSCD = T1.CLASSCD ");
            stb.append("             AND S99.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("             AND S99.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("             AND S99.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("             AND S99.SCHREGNO = T1.SCHREGNO ");
        }
        stb.append(" ), SUBCLASS AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.REP_SEQ_ALL, ");
        stb.append("     T1.SCH_SEQ_ALL, ");
        stb.append("     T1.SCH_SEQ_MIN, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ZENKI_FLG, ");
        stb.append("     T1.KOUKI_FLG, ");
        stb.append("     T1.CREDITS, ");
        stb.append("     '" + RISHU + "' AS KIND, ");
        stb.append("     AT11.JISU2 AS AT11_COUNT, ");
        stb.append("     AT21.JISU1 AS AT12_COUNT_ORG, ");
        stb.append("     AT21.JISU2 AS AT12_COUNT, ");
        stb.append("     AT31.JISU2 AS AT13_COUNT, ");
        stb.append("     REPGOU1.COUNT AS REPGOU1_COUNT, ");
        stb.append("     AT19.JISU2 AS AT91_COUNT, ");
        stb.append("     AT29.JISU1 AS AT92_COUNT_ORG, ");
        stb.append("     AT29.JISU2 AS AT92_COUNT, ");
        stb.append("     AT39.JISU2 AS AT93_COUNT, ");
        stb.append("     REPGOU9.COUNT AS REPGOU9_COUNT, ");
        stb.append("     T2.SEM1_INTR_VALUE, ");
        stb.append("     T2.SEM1_TERM_VALUE, ");
        stb.append("     T2.SEM2_INTR_VALUE, ");
        stb.append("     T2.SEM2_TERM_VALUE, ");
        stb.append("     T2.GRAD_VALUE2, ");
        stb.append("     T2.GRAD_VALUE, ");
        stb.append("     T2.GET_CREDIT ");
        stb.append(" FROM ");
        stb.append("     CHAIR2 T1 ");
        if (_param._isFukuiken) {
            stb.append("     LEFT JOIN RECORD_SCORE_HIST T2 ON T2.YEAR = T1.YEAR ");
        } else {
            stb.append("     LEFT JOIN RECORD_DAT T2 ON T2.YEAR = T1.YEAR ");
        }
        stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN REP_GOUKAKU_COUNT REPGOU1 ON REPGOU1.YEAR = T1.YEAR ");
        stb.append("         AND REPGOU1.CLASSCD = T1.CLASSCD ");
        stb.append("         AND REPGOU1.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND REPGOU1.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND REPGOU1.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND REPGOU1.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND REPGOU1.SEMESTER = '1' ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         AND REPGOU1.COURSECD = T1.COURSECD ");
            stb.append("         AND REPGOU1.MAJORCD = T1.MAJORCD ");
            stb.append("         AND REPGOU1.COURSECODE = T1.COURSECODE ");
        }

        stb.append("     LEFT JOIN REP_GOUKAKU_COUNT REPGOU9 ON REPGOU9.YEAR = T1.YEAR ");
        stb.append("         AND REPGOU9.CLASSCD = T1.CLASSCD ");
        stb.append("         AND REPGOU9.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND REPGOU9.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND REPGOU9.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND REPGOU9.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND REPGOU9.SEMESTER = '9' ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         AND REPGOU9.COURSECD = T1.COURSECD ");
            stb.append("         AND REPGOU9.MAJORCD = T1.MAJORCD ");
            stb.append("         AND REPGOU9.COURSECODE = T1.COURSECODE ");
        }
        stb.append("     LEFT JOIN ATTEND_KIND1 AT11 ON AT11.YEAR = T1.YEAR ");
        stb.append("         AND AT11.CLASSCD = T1.CLASSCD ");
        stb.append("         AND AT11.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND AT11.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND AT11.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND AT11.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND AT11.SEMESTER = '1' ");
        stb.append("     LEFT JOIN ATTEND_KIND2 AT21 ON AT21.YEAR = T1.YEAR ");
        stb.append("         AND AT21.CLASSCD = T1.CLASSCD ");
        stb.append("         AND AT21.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND AT21.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND AT21.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND AT21.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND AT21.SEMESTER = '1' ");
        stb.append("     LEFT JOIN ATTEND_KIND3 AT31 ON AT31.YEAR = T1.YEAR ");
        stb.append("         AND AT31.CLASSCD = T1.CLASSCD ");
        stb.append("         AND AT31.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND AT31.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND AT31.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND AT31.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND AT31.SEMESTER = '1' ");
        stb.append("     LEFT JOIN ATTEND_KIND1 AT19 ON AT19.YEAR = T1.YEAR ");
        stb.append("         AND AT19.CLASSCD = T1.CLASSCD ");
        stb.append("         AND AT19.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND AT19.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND AT19.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND AT19.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND AT19.SEMESTER = '9' ");
        stb.append("     LEFT JOIN ATTEND_KIND2 AT29 ON AT29.YEAR = T1.YEAR ");
        stb.append("         AND AT29.CLASSCD = T1.CLASSCD ");
        stb.append("         AND AT29.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND AT29.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND AT29.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND AT29.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND AT29.SEMESTER = '9' ");
        stb.append("     LEFT JOIN ATTEND_KIND3 AT39 ON AT39.YEAR = T1.YEAR ");
        stb.append("         AND AT39.CLASSCD = T1.CLASSCD ");
        stb.append("         AND AT39.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND AT39.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND AT39.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND AT39.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND AT39.SEMESTER = '9' ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     CAST(NULL AS SMALLINT) AS REP_SEQ_ALL, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SCH_SEQ_ALL, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SCH_SEQ_MIN, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS ZENKI_FLG, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS KOUKI_FLG, ");
        stb.append("     T1.CREDITS, ");
        stb.append("     T1.KIND, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT11_COUNT, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT12_COUNT_ORG, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT12_COUNT, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT13_COUNT, ");
        stb.append("     CAST(NULL AS INT) AS REPGOU1_COUNT, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT91_COUNT, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT92_COUNT_ORG, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT92_COUNT, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT93_COUNT, ");
        stb.append("     CAST(NULL AS INT) AS REPGOU9_COUNT, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SEM1_INTR_VALUE, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SEM1_TERM_VALUE, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SEM2_INTR_VALUE, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SEM2_TERM_VALUE, ");
        stb.append("     CAST(NULL AS SMALLINT) AS GRAD_VALUE2, ");
        stb.append("     CAST(NULL AS SMALLINT) AS GRAD_VALUE, ");
        stb.append("     T1.GET_CREDIT ");
        stb.append("     FROM ");
        stb.append("         KOUGAI T1 ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     T4.INOUTCD, ");
        stb.append("     T4.NAME, ");
        stb.append("     T8.SUBCLASSNAME ");
        stb.append("     FROM SUBCLASS T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SUBCLASS_MST T8 ON T8.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T8.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T8.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T8.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.KIND ");
        return stb.toString();
    }

    private static class Subclass {
        final String _year;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final String _zenkiFlg;
        final String _koukiFlg;
        final String _credits;
        final String _at1shussekiCount;
        final String _at1housouCountOrg;
        final String _repgou1Count;
        final String _at9shussekiCount;
        final String _at9housouCountOrg;
        final String _at9totalCount;
        final String _repgou9Count;
        final String _sem1IntrValue;
        final String _sem1TermValue;
        final String _sem2IntrValue;
        final String _sem2TermValue;
        final String _gradValue2;
        final String _gradValue;
        final String _getCredit;
        final String _kind;

        Subclass(
                final String year,
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname,
                final String zenkiFlg,
                final String koukiFlg,
                final String credits,
                final String at1shussekiCount,
                final String at1housouCountOrg,
                final String repgou1Count,
                final String at9shussekiCount,
                final String at9housouCountOrg,
                final String at9totalCount,
                final String repgou9Count,
                final String sem1IntrValue,
                final String sem1TermValue,
                final String sem2IntrValue,
                final String sem2TermValue,
                final String gradValue2,
                final String gradValue,
                final String getCredit,
                final String kind
        ) {
            _year = year;
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _zenkiFlg = zenkiFlg;
            _koukiFlg = koukiFlg;
            _credits = credits;
            _at1shussekiCount = at1shussekiCount;
            _at1housouCountOrg = at1housouCountOrg;
            _repgou1Count = repgou1Count;
            _at9shussekiCount = at9shussekiCount;
            _at9housouCountOrg = at9housouCountOrg;
            _at9totalCount = at9totalCount;
            _repgou9Count = repgou9Count;
            _sem1IntrValue = sem1IntrValue;
            _sem1TermValue = sem1TermValue;
            _sem2IntrValue = sem2IntrValue;
            _sem2TermValue = sem2TermValue;
            _gradValue2 = gradValue2;
            _gradValue = gradValue;
            _getCredit = getCredit;
            _kind = kind;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74305 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _gradeHrclass;
        final String _prgId;
        final String _loginDate;
        final Map<String, String> _semesterName;
        final String[] _categorySelected; // 科目コード
        final String _m020Name1;
        final String _useRepStandarddateCourseDat;
        final String _z010;
        final boolean _isFukuiken;
        final Map<String, List<String>> _subclassSemesterListMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _prgId = request.getParameter("PRGID");
            _loginDate = request.getParameter("LOGIN_DATE");
            _categorySelected = request.getParameterValues("category_selected");
            _useRepStandarddateCourseDat = request.getParameter("useRepStandarddateCourseDat");
            _semesterName = getSemesterNameMap(db2);
            _m020Name1 = getNameMstM020Name1(db2);
            _z010 = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            _isFukuiken = "fukuiken".equals(_z010);
            _subclassSemesterListMap = getSubclassSemesterListMap(db2);
        }

        private boolean isPrintKouki() {
            return SEME2.equals(_semester) || SEMEALL.equals(_semester);
        }

        private boolean isPrintGakunenmatsu() {
            return SEMEALL.equals(_semester);
        }

        private String getRegdSemester() {
            return SEMEALL.equals(_semester) ? _ctrlSemester : _semester;
        }

        private Map<String, String> getSemesterNameMap(final DB2UDB db2) {
            Map<String, String> rtn = new HashMap<String, String>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"));
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getNameMstM020Name1(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'M020' AND NAMECD2 = '01' "));
        }

        protected static <T, K> List<T> getMappedList(final Map<K, List<T>> map, final K key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new ArrayList<T>());
            }
            return map.get(key1);
        }

        private boolean isZenkiKamoku(final Subclass subclass) {
            if (_isFukuiken) {
                final List<String> semesterList = getMappedList(_subclassSemesterListMap, subclass._classcd + "-" + subclass._schoolKind + "-" + subclass._curriculumCd + "-" + subclass._subclasscd);
                return semesterList.contains("1") && !semesterList.contains("2");
            }
            return "1".equals(subclass._zenkiFlg);
        }
        private boolean isKoukiKamoku(final Subclass subclass) {
            if (_isFukuiken) {
                final List<String> semesterList = getMappedList(_subclassSemesterListMap, subclass._classcd + "-" + subclass._schoolKind + "-" + subclass._curriculumCd + "-" + subclass._subclasscd);
                return !semesterList.contains("1") && semesterList.contains("2");
            }
            return "1".equals(subclass._koukiFlg);
        }
        private Map<String, List<String>> getSubclassSemesterListMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T2.SEMESTER ");
            stb.append(" FROM ");
            stb.append("     REP_STANDARDDATE_DAT T1 ");
            stb.append("     INNER JOIN NAME_MST M002 ");
            stb.append("         ON M002.NAMECD1 = 'M002' ");
            stb.append("        AND M002.NAMECD2 = T1.REPORTDIV ");
            stb.append("     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ");
            stb.append("         ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SEMESTER || '-' || T2.TESTKINDCD || '-' || T2.TESTITEMCD || '-' || T2.SCORE_DIV = M002.NAMESPARE1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");

            final Map<String, List<String>> subclassSemesterListMap = new HashMap<String, List<String>>();
            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                getMappedList(subclassSemesterListMap, KnjDbUtils.getString(row, "SUBCLASSCD")).add(KnjDbUtils.getString(row, "SEMESTER"));
            }
            return subclassSemesterListMap;
        }
    }
}

// eof


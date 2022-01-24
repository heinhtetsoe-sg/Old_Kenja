//kanji=漢字
/*
 * 個人別出欠状況チェックリスト
 *
 * $Id: 5042e63fdb920eb15321cec17c6476c7f2878bda $
 *
 * 作成日: 2009/01/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *   学校教育システム 賢者 [出欠管理]
 *
 *                   ＜ＫＮＪＣ０３７＞  個人別出欠状況チェックリスト
 */

public class KNJC037 {

    private static final Log log = LogFactory.getLog(KNJC037.class);
    private Param _param;
    private PreparedStatement ps0, ps1;

    // 勤怠コード
    private final Integer diCdAttend         = new Integer(0);  // 出席
    private final Integer diCdAbsent         = new Integer(1);  // 公欠
    private final Integer diCdSuspend        = new Integer(2);  // 出停
    private final Integer diCdMourning       = new Integer(3);  // 忌引
    private static final Integer diCdSick           = new Integer(4);  // 欠席1
    private static final Integer diCdNotice         = new Integer(5);  // 欠席2
    private static final Integer diCdNonotice       = new Integer(6);  // 欠席3(届無)
    private static final Integer diCdAbsentOneday   = new Integer(8);  // 1日公欠
    private static final Integer diCdSuspendOneday  = new Integer(9);  // 1日出停
    private static final Integer diCdMourningOneday = new Integer(10); // 1日忌引
    private static final Integer diCdSickOneday     = new Integer(11); // 1日欠席1
    private static final Integer diCdNoticeOneday   = new Integer(12); // 1日欠席2
    private static final Integer diCdNonoticeOneday = new Integer(13); // 1日欠席3(届無)
    private final Integer diCdNurseOff       = new Integer(14); // 保健室欠席
    private final Integer diCdLate           = new Integer(21); // 遅刻
    private final Integer diCdLateNonotice   = new Integer(15); // 遅刻(届無)
    private final Integer diCdNone           = new Integer(-1); // 無効
    private final Integer diCdLate2          = new Integer(23); // 遅刻2
    private final Integer diCdLate3          = new Integer(24); // 遅刻3

    // 勤怠コードのリスト (Nonotice = 届出無しのコード)
    private List diCdListAllCheck;       // チェックする全ての勤怠
    private List diCdListAbsent;         // 欠席
    private List diCdListAbsentNonotice; // 欠席 (届出無)
    private List diCdListLate;           // 遅刻
    private List diCdListLateNonotice;   // 遅刻 (届出無)
    private List diCdListKekka;          // 欠課
    private List diCdListKekkaNonotice;  // 欠課 (届出無)

    // 1日をあらわす勤怠を読み替えるテーブル
    private static final Integer[][] replaceKintai = new Integer[][]{{diCdSickOneday, diCdSick}, {diCdNoticeOneday, diCdNotice}, {diCdNonoticeOneday, diCdNonotice}};

    // 時間割の月日リスト
    private List _scheduleDatesList;

    final String OUTPUT_KESSEKI = "1";
    final String OUTPUT_LATE = "2";
    final String OUTPUT_KEKKA = "3";


    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        diCdListAllCheck = Arrays.asList(new Integer[]{diCdAttend, diCdAbsent, diCdSick, diCdNotice, diCdNonotice, diCdSickOneday, diCdNoticeOneday, diCdNonoticeOneday, diCdNurseOff, diCdLate, diCdLateNonotice});
        diCdListAbsent = Arrays.asList(new Integer[]{diCdSick, diCdNotice, diCdNonotice, diCdSickOneday, diCdNoticeOneday, diCdNonoticeOneday});
        diCdListAbsentNonotice = Arrays.asList(new Integer[]{diCdNonotice, diCdNonoticeOneday});
        diCdListLate = Arrays.asList(new Integer[]{diCdLate, diCdLateNonotice, diCdLate2, diCdLate3});
        diCdListLateNonotice = Arrays.asList(new Integer[]{diCdLateNonotice});
        diCdListKekka = Arrays.asList(new Integer[]{diCdSick, diCdNotice, diCdNonotice, diCdSickOneday, diCdNoticeOneday, diCdNonoticeOneday, diCdNurseOff});
        diCdListKekkaNonotice = Arrays.asList(new Integer[]{diCdNonotice, diCdNonoticeOneday});

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        // ＤＢ接続

        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        // パラメータの取得
        _param = new Param(db2, request);

        // print svf設定
        response.setContentType("application/pdf");
        svf.VrInit();                             //クラスの初期化

        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
        svf.VrSetForm("KNJC037.frm", 4);

        // 印刷処理
        boolean nonedata = false;
        for (int i = 0; i < _param._schregno.length; i++) {
            nonedata = printMain(db2, svf, _param._schregno[i]) || nonedata;
        }

        // 終了処理
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }
        svf.VrQuit();


        db2.commit();
        db2.close();
    }

    /*
     * 印刷処理
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf, final String schregno) {

        try {
            ps0 = db2.prepareStatement(prestatementScheduleDate(schregno));
            log.debug("ps0 prestatementScheduleDate sql = " + prestatementScheduleDate(schregno));
            ps1 = db2.prepareStatement(prestatementStudent(schregno));
            log.debug("ps1 prestatementStudent sql = " + prestatementStudent(schregno));
        } catch (SQLException ex) {
            log.debug("preparedStatement setting error!",ex);
        }

        Student student = null;
        ResultSet rs = null;
        try{
            // 学生の年組番号、名前、組主任
            rs = ps1.executeQuery();
            if (rs.next()) {
                student = new Student(
                        rs.getString("NAME"),
                        rs.getString("SCHREGNO"),
                        rs.getString("ATTENDNO"),
                        rs.getString("GRADE"),
                        rs.getString("HR_CLASS"),
                        rs.getString("HR_NAME"),
                        rs.getString("STAFFNAME"),
                        rs.getString("COUNTFLG"));
            }
        } catch (Exception ex) {
            log.debug("printHeader exception ",ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }

        if (!student._isTarget) {
            log.debug("対象の生徒ではありません。" + student._schregno);
            return false;
        }

        setScheduleList();
        setAccumulateCount(db2, student);

        printHeader(db2, svf, student);
        final boolean hasData = printSvf(db2,svf);

        DbUtils.closeQuietly(ps0);
        DbUtils.closeQuietly(ps1);
        return hasData;
    }

    /**
     * 時間割の日付校時をセットする
     */
    private List setScheduleList() {
        _scheduleDatesList = new ArrayList();

        ResultSet rs = null;
        try{
            rs = ps0.executeQuery();
            AttendDat attendDat = null;
            while(rs.next()) {
                if (attendDat == null || !attendDat._date.equals(rs.getString("EXECUTEDATE"))) { // 月日が変化したら新規作成
                    attendDat = new AttendDat(
                            rs.getString("SEMESTER"),
                            rs.getString("EXECUTEDATE"),
                            "1".equals(rs.getString("IN_CHECK_AREA")));
                    _scheduleDatesList.add(attendDat);
                }
                final String periodCd = rs.getString("PERIODCD");
                final Integer diCd = (rs.getString("DI_CD") == null ? diCdAttend : Integer.valueOf(rs.getString("DI_CD")));
                final Integer diCdYomikaeMae = (rs.getString("DI_CD_YOMIKAEMAE") == null ? diCdAttend : Integer.valueOf(rs.getString("DI_CD_YOMIKAEMAE")));
                attendDat.addPeriod( new Period(periodCd, diCd, diCdYomikaeMae));
            }
        } catch (Exception ex) {
            log.debug("setAbsentList error!", ex);
            DbUtils.closeQuietly(rs);
        }

        for(final Iterator it = _scheduleDatesList.iterator(); it.hasNext();) {
            final AttendDat attendDat = (AttendDat) it.next();
            if (!_param.containsKintai(attendDat._periodList, diCdListAllCheck)) { // チェックする勤怠以外のデータは不要なので削除する
                //log.debug("全出席 : " + attendDat);
                it.remove();
            } else {
                //attendDat.debugOutput();
            }
        }
        return _scheduleDatesList;
    }

    /**
     * 学期クラスに時間割日時リストから欠席日数・遅刻回数・欠時数・欠課回数をセットする
     * @param scheduleDatesList 時間割の月日リスト
     */
    private void setAccumulateCount(final DB2UDB db2, final Student student) {

        final int[] absentCount = new int[10];
        final int[] lateCount = new int[10];
        final int[] lacktime = new int[10];
        final int[] kekkaCount = new int[10];

        PreparedStatement ps = null;
        ResultSet rs = null;

        final StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SEMESTER, SUM(SICK + NOTICE + NONOTICE) AS ABSENT, SUM(LATEDETAIL) AS LATE,  SUM(KEKKA_JISU) AS KEKKA_JISU, SUM(KEKKA) AS KEKKA ");
        sql.append(" FROM ATTEND_SEMES_DAT ");
        sql.append(" WHERE SCHREGNO = '" + student._schregno + "' AND YEAR = '" + _param._year + "' AND SEMESTER || MONTH IN " + NameMstC040.sqlSemeMonth());
        sql.append(" GROUP BY SEMESTER ");

        try {
            log.debug(" attendsemes sql = " + sql.toString());
            ps = db2.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final int semes = Integer.parseInt(rs.getString("SEMESTER"));
                absentCount[semes - 1] += rs.getInt("ABSENT");
                kekkaCount[semes - 1] +=  rs.getInt("KEKKA");
                lateCount[semes - 1] +=  rs.getInt("LATE");
                lacktime[semes - 1] +=  rs.getInt("KEKKA_JISU");
            }
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        for(final Iterator it = _scheduleDatesList.iterator(); it.hasNext();) {
            final AttendDat attendDat = (AttendDat) it.next();

            if (attendDat._date.compareTo(NameMstC040.getScheduleStartDate()) < 0) {
                continue;
            }

            final int semes = Integer.parseInt(attendDat._semester);

            absentCount[semes - 1] += (attendDat.isOnedayAbsent()) ? 1 : 0;
            lateCount[semes - 1] += attendDat.getLateCount();
            if (attendDat.getOnedayDiCd().equals(diCdNone) || _param._isOutputKekka) {
                lacktime[semes - 1] += attendDat.getLacktime();
            }
            kekkaCount[semes - 1] += attendDat.getKekkaCount();
        }

        for(final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
            final Semester semester = (Semester) _param._semesterMap.get((Integer) it.next());
            final int semesi = semester._semes.intValue() - 1;
            semester._absentCount = new Integer(absentCount[semesi]);
            semester._lateCount = new Integer(lateCount[semesi]);
            semester._lacktime = new Integer(lacktime[semesi]);
            semester._kekkaCount = new Integer(kekkaCount[semesi]);
        }
    }

    /**
     * ヘッダを出力する
     */
    private void printHeader(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        // 年度
        svf.VrsOut("NENDO", _param._year+"年度");
        svf.VrsOut("NAME", student._name);
        final int attendno = Integer.parseInt(student._attendno);
        DecimalFormat df4 = new DecimalFormat("   0");
        svf.VrsOut("HR_CLASS", student._hrName + df4.format(attendno)+"番");
        svf.VrsOut("STAFFNAME", student._staffName);

        // 集計表の集計範囲
        final String accumSdate = _param._accum_sdate.substring(0,4) + "年"+ KNJ_EditDate.h_format_JP_MD(_param._accum_sdate);
        final String accumEdate = _param._edate.substring(0,4) + "年"+ KNJ_EditDate.h_format_JP_MD(_param._edate);
        svf.VrsOut("TERM", accumSdate+" 〜 "+accumEdate);

        // 集計表(学期名、欠席日数、遅刻回数、欠時数、欠課回数)
        int totalAbsent = 0;
        int totalLate = 0;
        int totalLacktime = 0;
        int totalKekka = 0;
        for (final Iterator it=_param._semesterMap.keySet().iterator(); it.hasNext();) {
            final Integer semes = (Integer) it.next();
            final Semester semester = (Semester) _param._semesterMap.get(semes);
            svf.VrsOut("SEMESTER1_"+semester, semester._name);
            svf.VrsOut("SEMESTER2_"+semester, semester._name);
            svf.VrsOut("SEMESTER3_"+semester, semester._name);

            // 指定範囲外の学期は出力しない
            if (semes.intValue() > Integer.parseInt(_param._semester)) {
                continue;
            }
            svf.VrsOut("ABSENT" + semester, semester._absentCount.toString());
            svf.VrsOut("LACKTIME" + semester, semester._lacktime.toString());
            svf.VrsOut("LATE" + semester, semester._lateCount.toString());
            svf.VrsOut("KEKKA" + semester, semester._kekkaCount.toString());

            totalAbsent += semester._absentCount.intValue();
            totalLacktime += semester._lacktime.intValue();
            totalLate += semester._lateCount.intValue();
            totalKekka += semester._kekkaCount.intValue();
        }


        svf.VrsOut("TOTAL_ABSENT", String.valueOf(totalAbsent));
        svf.VrsOut("TOTAL_LATE",   String.valueOf(totalLate));
        svf.VrsOut("TOTAL_LACKTIME", String.valueOf(totalLacktime));
        svf.VrsOut("TOTAL_KEKKA", String.valueOf(totalKekka));

        // 出欠状況出力範囲
        final String attendSdate = _param._sdate.substring(0, 4) + "年"+ KNJ_EditDate.h_format_JP_MD(_param._sdate);
        final String attendEdate = _param._edate.substring(0, 4) + "年"+ KNJ_EditDate.h_format_JP_MD(_param._edate);
        svf.VrsOut("ATTEND_TERM", attendSdate+" 〜 "+attendEdate);
    }

    /**
     * 印刷処理
     * @param db2
     * @param svf
     */
    private boolean printSvf (
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        //SQL作成
        int line=1; // 行
        final int LINE_PER_PAGE = 40;
        final List outputLine = new ArrayList();
        line = addAttendDatOutput(outputLine, line, false, "欠席", diCdListAbsent, diCdListAbsentNonotice, OUTPUT_KESSEKI);
        line = addAttendDatOutput(outputLine, line, true, "遅刻", diCdListLate, diCdListLateNonotice, OUTPUT_LATE);
        line = addAttendDatOutput(outputLine, line, true, "欠課", diCdListKekka, diCdListKekkaNonotice, OUTPUT_KEKKA);
        int linePage = line / LINE_PER_PAGE + (line % LINE_PER_PAGE != 0 ? 1 : 0);
        log.debug("リスト = " + line + "行 , リストPage数 = " + linePage);

        final int RECORD_PER_PAGE = 20;
        final List scheduleDatesInCheckArea = new ArrayList(); // 出欠状況出力範囲のみの月日のリスト
        for (final Iterator it = _scheduleDatesList.iterator(); it.hasNext();) {
            final AttendDat attendDat = (AttendDat) it.next();
            if (!attendDat._isInCheckArea) continue;
            if (!attendDat.containsNonotice()) continue;
            scheduleDatesInCheckArea.add(attendDat);
        }
        final int recordPage = scheduleDatesInCheckArea.size() / RECORD_PER_PAGE + (scheduleDatesInCheckArea.size() % RECORD_PER_PAGE != 0 ? 1 : 0 );
        log.debug("解答欄 = " + scheduleDatesInCheckArea.size() + "レコード , 解答欄Page数 = " + recordPage);

        // ページごとに出欠状況と解答欄を出力する
        for (int p = 0, maxpage = Math.max(linePage, recordPage); p < maxpage; p++) {
            boolean outputtedLine = false;
            boolean outputtedReply = false;

            for(int l = 1; l <= LINE_PER_PAGE; l++) { // クリア
                svf.VrsOutn("ATTEND_KIND", l, "");
                svf.VrsOutn("ATTENDDATE", l, "");
                svf.VrsOutn("PERIOD", l, "");
            }

            // 出欠状況の出力
            if (outputLine.size() != 0) {
                for (int l = 1; l <= LINE_PER_PAGE && outputLine.size() > 0; l++) {
                    outputtedLine = true;
                    final List lineList = (List) outputLine.remove(0);

                    for (final Iterator it = lineList.iterator(); it.hasNext();) {
                        final SvfOutput svfElement = (SvfOutput) it.next();
                        final int outputl = (l % LINE_PER_PAGE == 0 ) ? LINE_PER_PAGE : l % LINE_PER_PAGE;
                        if (svfElement._fieldname.equals("")) { continue; }
                        svf.VrsOutn(svfElement._fieldname, outputl, svfElement._output);
                        //log.debug("帳票出力: " + l + " , " + svfElement);
                    }
                }
            }

            // 解答欄の出力
            if (scheduleDatesInCheckArea.size() > 0) {
                // 対象: 欠席/遅刻/欠課のいずれかがある日付
                final int max = Math.min(RECORD_PER_PAGE, scheduleDatesInCheckArea.size());
                for (int i = 0; i < max; i++) {
                    outputtedReply = true;
                    final AttendDat attendDat = (AttendDat) scheduleDatesInCheckArea.remove(0);
                    final String date = _param.getFormattedDate(attendDat._date, true);
                    svf.VrsOut("GRD_ATTENDDATE", date);
                    //log.debug("帳票保護者出力欄: " + (i+1) + " , " + date);
                    svf.VrEndRecord();
                }
                if (scheduleDatesInCheckArea.size()==0) {
                    for (int i = max; i < RECORD_PER_PAGE; i++) {
                        svf.VrsOut("GRD_ATTENDDATE", "\b");
                        //log.debug("  " + (i+1) + " [改行]");
                        svf.VrEndRecord();
                    }
                }
            }

            // 出力が出欠状況しかない場合
            if (outputtedLine && !outputtedReply) {
                for (int i = 0; i < RECORD_PER_PAGE; i++) {
                    svf.VrsOut("GRD_ATTENDDATE", "\b");
                    svf.VrEndRecord();
                }
            }
        }
        return line > 1;
    }

    /**
     * AttendDatの日付(と校時のリスト)の出力をtotalOutputLineに追加する
     * @param totalOutputLine 全ての出力行リスト
     * @param line 開始行
     * @param outputPeriod 校時を出力するときはtrue, そうでなければfalse ("遅刻"と"欠課"はtrue,"欠席"はfalse)
     * @param kindTitle　勤怠種別タイトル ("欠席"/"遅刻"/"欠課")
     * @param diCd 勤怠リスト
     * @param diCdNonoticeList 無届の勤怠リスト(勤怠リストに含まれているべき)
     * @param debugComment　デバッグ用のコメント
     * @return 次の開始行
     */
    private int addAttendDatOutput(final List totalOutputLine, int line, final boolean outputPeriod, final String kindTitle, final List diCd, final List diCdNonoticeList, final String debugComment) {
        final int maxPeriodCountInLine = 5; // 一行に表示する校時の数
        boolean outputKind = false;

        if (line != 1) { // lineが1でなければ空白行を挿入する
            addSvfOutputList(totalOutputLine, line, "", null);
            line += 1; // 空白行
        }

        for(final Iterator it = _scheduleDatesList.iterator(); it.hasNext();) {
            if (outputKind == false) { // 出力の1行目でデータの種類を印字
                addSvfOutputList(totalOutputLine, line, "ATTEND_KIND", kindTitle);
                line += 1;
                outputKind = true;
            }
            final AttendDat attendDat = (AttendDat) it.next();
            if (attendDat._isInCheckArea == false) { // 出力範囲外
                continue;
            }

            final Iterator periodIterator = attendDat.getPeriodIteratorOf(diCd);
            if (periodIterator == null) { // 対象勤怠の校時は無し
                continue;
            }

            // 画面で「1日欠席の時は欠課を出力する」がチェックされておらず、1日欠席の場合は欠課を出力しない。
            if (OUTPUT_KEKKA.equals(debugComment) && !_param._isOutputKekka && !attendDat.getOnedayDiCd().equals(diCdNone)) {
                continue;
            }

            String outputAttendDat = _param.getFormattedDate(attendDat._date, false);

            // 校時を表示しない(欠席)なら届出有/無の文字列を付加する
            if (!outputPeriod) {
                final Integer onedayDiCd = attendDat.getOnedayDiCd();
                if (onedayDiCd.equals(diCdNone)) {
                    continue;
                }
                outputAttendDat += _param.getNoticeString(onedayDiCd, diCdNonoticeList);
            }

            addSvfOutputList(totalOutputLine, line, "ATTENDDATE", outputAttendDat);

            //log.debug("出力 : " + line + " , " + outputAttendDat);

            if (!outputPeriod) {
                line += 1;
                continue;
            }

            // 校時出力
            int periodCount = 0; // 出力ラインの校時数
            String periodLineString = ""; // 出力ライン
            for (; periodIterator.hasNext();) {
                if (!"".equals(periodLineString)) {
                    periodLineString +=", ";
                }
                final Period period = (Period) periodIterator.next();
                periodLineString += period.getNameForOutput(diCdNonoticeList);
                periodCount += 1;

                if (periodCount >= maxPeriodCountInLine) { // 改行処理
                    addSvfOutputList(totalOutputLine, line, "PERIOD", periodLineString);
                    line += 1;
                    periodCount = 1;
                    periodLineString = "";
                }
            }
            if (!"".equals(periodLineString)) {
                addSvfOutputList(totalOutputLine, line, "PERIOD", periodLineString);
                line += 1;
                periodLineString = "";
            }
        }
        return line;
    }

    /**
     * 出力する文字列をSvf出力文字列のリストに追加する
     * @param totalSvfOutputList Svf出力文字列のリスト
     * @param line 出力する行数
     * @param fieldName フィールド名
     * @param output 出力する文字列
     */
    private void addSvfOutputList(final List totalSvfOutputList, final int line, final String fieldName, final String output) {

        for (int i = totalSvfOutputList.size(); i < line; i++) {
            totalSvfOutputList.add(new ArrayList());
        }
        final List svfOutputList = (List) totalSvfOutputList.get(line - 1);

        svfOutputList.add(new SvfOutput(fieldName, output));
    }

    /**
     *  preparedStatement作成 出欠データ
     *    時間割の日付校時を取得する
     */
    private String prestatementScheduleDate(final String schregno) {

        final StringBuffer stb = new StringBuffer();
        // 対象の学生データ
        stb.append(" WITH SCHNO AS(SELECT SCHREGNO ");
        stb.append( " FROM ");
        stb.append(         " SCHREG_REGD_DAT W1 ");
        stb.append( " WHERE ");
        stb.append(         " W1.YEAR = '"+_param._year+"'     AND ");
        stb.append(         " W1.SEMESTER <= '"+_param._semester+"' AND ");
        stb.append(         " W1.SCHREGNO = '"+schregno+"' ");
        stb.append(" ), SCHEDULE_SCHREG AS(SELECT ");
        stb.append(         " T2.SCHREGNO, ");
        stb.append(         " T1.YEAR, ");
        stb.append(         " T1.SEMESTER, ");
        stb.append(         " T1.EXECUTEDATE, ");
        stb.append(         " T1.PERIODCD ");
        stb.append( " FROM ");
        stb.append(         " SCH_CHR_DAT T1 ");
        stb.append(         " INNER JOIN CHAIR_STD_DAT T2 ON ");
        stb.append(         " T1.YEAR = T2.YEAR AND ");
        stb.append(         " T1.SEMESTER = T2.SEMESTER AND ");
        stb.append(         " T1.CHAIRCD = T2.CHAIRCD ");
        stb.append( " WHERE ");
        stb.append(         " T1.YEAR = '"+_param._year+"' AND ");
        stb.append(         " T1.SEMESTER BETWEEN '1' AND '"+_param._semester+"' AND ");
        stb.append(         " T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ");
        stb.append(         " T2.SCHREGNO IN(SELECT ");
        stb.append(                     " SCHREGNO ");
        stb.append(             " FROM ");
        stb.append(                     " SCHNO ");
        stb.append(             " GROUP BY ");
        stb.append(                     " SCHREGNO ");
        stb.append(      " ) AND NOT EXISTS(SELECT ");
        stb.append(             " ' X' ");
        stb.append(             " FROM ");
        stb.append(                     " SCHREG_BASE_MST T3 ");
        stb.append(             " WHERE ");
        stb.append(                     " T3.SCHREGNO = T2.SCHREGNO    AND ");
        stb.append(                     " ((ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE) ");
        stb.append(                      " OR (GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE)) ");
        stb.append(      " ) AND NOT EXISTS(SELECT ");
        stb.append(                  " 'X' ");
        stb.append(             " FROM ");
        stb.append(                     " SCHREG_TRANSFER_DAT T3 ");
        stb.append(             " WHERE ");
        stb.append(                     " T3.SCHREGNO = T2.SCHREGNO AND ");
        stb.append(                     " TRANSFERCD IN('1','2') AND ");
        stb.append(                     " EXECUTEDATE BETWEEN TRANSFER_SDATE AND ");
        stb.append(                     " TRANSFER_EDATE ");
        stb.append(     " ) ");
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        stb.append("                  ) ");
        // 勤怠コード'28'は時間割にカウントしない
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND L1.REP_DI_CD = '28' ");
        stb.append("                  ) ");
        if (_param._hasSchChrDatExecutediv) {
            stb.append("        AND VALUE(T1.EXECUTEDIV, '0') <> '2' "); // 休講は対象外
        }
        stb.append(  " GROUP BY ");
        stb.append(       " T2.SCHREGNO, ");
        stb.append(       " T1.YEAR, ");
        stb.append(       " T1.SEMESTER, ");
        stb.append(       " T1.EXECUTEDATE, ");
        stb.append(       " T1.PERIODCD ");
        stb.append(" ) SELECT ");
        stb.append(         " T0.SCHREGNO, ");
        stb.append(         " T0.SEMESTER, ");
        stb.append(         " T0.EXECUTEDATE, ");
        stb.append(         " T0.PERIODCD, ");
        stb.append(         " CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END AS DI_CD, ");
        stb.append(         " L1.REP_DI_CD AS DI_CD_YOMIKAEMAE, ");
        stb.append(         " (CASE WHEN T0.EXECUTEDATE BETWEEN '"+_param._sdate+"' AND '"+_param._edate+"' THEN '1' ELSE '0' END) AS IN_CHECK_AREA ");
        stb.append( " FROM ");
        stb.append(         " SCHEDULE_SCHREG T0 ");
        stb.append(         " LEFT JOIN ATTEND_DAT T1 ON ");
        stb.append(         " T0.SCHREGNO = T1.SCHREGNO AND ");
        stb.append(         " T0.EXECUTEDATE = T1.ATTENDDATE AND ");
        stb.append(         " T0.PERIODCD = T1.PERIODCD ");
        stb.append(         " LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T1.YEAR AND L1.DI_CD = T1.DI_CD ");
        stb.append( " WHERE ");
        stb.append(         " T0.YEAR = '"+_param._year+"' AND ");
        stb.append(         " T0.EXECUTEDATE BETWEEN '"+_param._accum_sdate+"' AND '"+_param._edate+"' ");
        stb.append( " ORDER BY ");
        stb.append(         " T0.SCHREGNO, T0.SEMESTER, T0.EXECUTEDATE, T0.PERIODCD, L1.REP_DI_CD ");
        return stb.toString();
    }

    /*
     *  preparedStatement作成 学籍基礎、学籍在籍、
     *    年組番号名前、組主任を取得する
     */
    private String prestatementStudent(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.NAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T3.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T3.TR_CD1, ");
        stb.append("     T4.STAFFNAME, ");
        stb.append("     T5.COUNTFLG ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ON ");
        stb.append("         T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T3 ON ");
        stb.append("         T1.YEAR = T3.YEAR ");
        stb.append("         AND T1.GRADE = T3.GRADE ");
        stb.append("         AND T1.SEMESTER = T3.SEMESTER ");
        stb.append("         AND T1.HR_CLASS = T3.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST T4 ON ");
        stb.append("         T3.TR_CD1 = T4.STAFFCD ");
        stb.append("     LEFT JOIN SCHREG_REGD_DETAIL T5 ON ");
        stb.append("         T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T5.YEAR = T1.YEAR ");
        stb.append("         AND T5.SEMESTER = T1.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '"+_param._year+"' ");
        stb.append("     AND T1.SEMESTER = '"+_param._semester+"' ");
        stb.append("     AND T1.SCHREGNO = '"+schregno+"' ");
        stb.append(" ORDER BY ");
        stb.append("     GRADE, HR_CLASS, ATTENDNO ");
        return stb.toString();
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _sdate;
        final String _edate;
        final String[] _schregno;
        final String[] dayOfWeekAbbv = new String[]{"", "(日)", "(月)", "(火)", "(水)", "(木)", "(金)", "(土)"};
        final boolean _isOutputKekka; // trueのとき、1日欠席は欠課を表示する。
        String _accum_sdate;
        Map _semesterMap;  // 学期のマップ
        Map _periodMap;    // 校時名マップ
        // 遅刻コードごとの回数
        Map _lateCountMap; // 遅刻勤怠コード毎の遅刻回数


        final KNJDefineSchool _defineCode = new KNJDefineSchool();

        KNJSchoolMst _knjSchoolMst;
        final boolean _hasSchChrDatExecutediv;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            if (log.isDebugEnabled()) {
                final Enumeration enum1 = request.getParameterNames();
                while (enum1.hasMoreElements()) {
                    final String name = (String) enum1.nextElement();
                    final String[] values = request.getParameterValues(name);
                    log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
                }
            }

            _year  = request.getParameter("YEAR");           //年度
            _semester  = request.getParameter("GAKKI");       //学期
            //日付型を変換
            String sdate = request.getParameter("S_DATE");            //印刷範囲開始
            _sdate = StringUtils.replace(sdate, "/", "-");
            String edate = request.getParameter("E_DATE");            //印刷範囲終了
            _edate = StringUtils.replace(edate, "/", "-");
            _schregno = request.getParameterValues("SELECTED_DATA");

            loadSemester(db2);
            loadPeriod(db2);
            loadLateCount(db2);

            // 年度開始日を取得
            final Semester s1 = (Semester) _semesterMap.get(new Integer(1));
            _accum_sdate = s1._sdate;

            _isOutputKekka = "1".equals(request.getParameter("OUT_PUT_KEKKA"));

            try {
                _defineCode.defineCode(db2, _year);

                _knjSchoolMst = new KNJSchoolMst(db2, _year);

                NameMstC040.load(db2, _year, _accum_sdate);
            } catch (Exception e) {
                log.debug("Param const. Exception!", e);
            }
            _hasSchChrDatExecutediv = setTableColumnCheck(db2, "SCH_CHR_DAT", "EXECUTEDIV");
        }

        private boolean setTableColumnCheck(final DB2UDB db2, final String tabname, final String colname) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT 1 FROM ");
            if (StringUtils.isBlank(colname)) {
                stb.append("SYSCAT.TABLES");
            } else {
                stb.append("SYSCAT.COLUMNS");
            }
            stb.append(" WHERE TABNAME = '" + tabname + "' ");
            if (!StringUtils.isBlank(colname)) {
                stb.append(" AND COLNAME = '" + colname + "' ");
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean hasTableColumn = false;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    hasTableColumn = true;
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.fatal(" hasTableColumn " + tabname + (null == colname ? "" :  "." + colname) + " = " + hasTableColumn);
            return hasTableColumn;
        }

        boolean kekkaContainsAbsent() {
            return "1".equals(_knjSchoolMst._subAbsent);
        }
        boolean kekkaContainsSuspend() {
            return "1".equals(_knjSchoolMst._subSuspend);
        }
        boolean kekkaContainsMourning() {
            return "1".equals(_knjSchoolMst._subMourning);
        }
        boolean kekkaContainsVirus() {
            return "1".equals(_knjSchoolMst._subVirus);
        }

        /**
         * 学期をロードする
         */
        private void loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sqlSemester = new StringBuffer();
                sqlSemester.append(" SELECT ");
                sqlSemester.append("     SEMESTER, SEMESTERNAME, SDATE, EDATE ");
                sqlSemester.append(" FROM ");
                sqlSemester.append("     SEMESTER_MST ");
                sqlSemester.append(" WHERE ");
                sqlSemester.append("     YEAR = '"+_year+"' ");

                _semesterMap = new HashMap();
                ps = db2.prepareStatement(sqlSemester.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Integer semes = new Integer(rs.getInt("SEMESTER"));
                    final String semesterName = rs.getString("SEMESTERNAME");
                    final Semester semester = new Semester(semes, semesterName, rs.getString("SDATE"), rs.getString("EDATE"));
                    _semesterMap.put(semes, semester);
                    //log.debug(semester.toDebugString());
                }
            } catch (SQLException ex) {
                log.debug("loadSemester exception ", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 校時名称をロードする
         */
        private void loadPeriod(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sqlPeriod = new StringBuffer();
                sqlPeriod.append(" SELECT ");
                sqlPeriod.append("     NAMECD2 AS CD, NAME1 AS NAME ");
                sqlPeriod.append(" FROM ");
                sqlPeriod.append("     NAME_MST ");
                sqlPeriod.append(" WHERE ");
                sqlPeriod.append("     NAMECD1 = 'B001' ");

                _periodMap = new HashMap();
                ps = db2.prepareStatement(sqlPeriod.toString());
                rs = ps.executeQuery();
                while(rs.next()) {
                    String cd = rs.getString("CD");
                    String name = rs.getString("NAME");
                    _periodMap.put(cd, name);
                    //log.debug("[period] " + cd + " , " + name);
                }
            } catch (SQLException ex) {
                log.debug("loadSemester exception ", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 遅刻勤怠コードごとの遅刻回数をロードする
         */
        private void loadLateCount(final DB2UDB db2) {
            _lateCountMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = " SELECT NAMECD2 AS DI_CD, INT(VALUE(ABBV2, '1')) AS COUNT FROM NAME_MST WHERE NAMECD1 = 'C001' ";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Integer diCd = Integer.valueOf(rs.getString("DI_CD"));
                    final Integer count = Integer.valueOf(rs.getString("COUNT"));
                    _lateCountMap.put(diCd, count);
                }
            } catch (Exception e) {
                log.error("Exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 勤怠コードに対応した遅刻の回数を得る
         * @param diCd 遅刻の勤怠コード
         */
        private int getDiCdCount(final Integer diCd, final Map diCdCountMap) {
            final int rtn;
            if (diCdCountMap.containsKey(diCd)) {
                final Integer count = (Integer) diCdCountMap.get(diCd);
                rtn = (null == count) ? 1 : count.intValue();
            } else {
                rtn = 1;
            }
            return rtn;
        }

        /**
         * 校時名称を得る
         * @param periodCd 校時コード
         * @return 校時名称
         */
        String getPeriodName(String periodCd) {
            return (String) _periodMap.get(periodCd);
        }

        /**
         * '-'を区切り文字とする月日をフォーマットして返す。 (例:2009-03-06 => " 3月 6日(金)")
         * @param hyphenDate '-'が区切り文字の月日
         * @param outputDayOfWeek 曜日を出力するか否かのフラグ。trueなら曜日を出力する。
         * @return フォーマットされた月日曜日
         */
        private String getFormattedDate(final String hyphenDate, final boolean outputDayOfWeek) {
            final int year = Integer.parseInt(hyphenDate.substring(0, hyphenDate.indexOf('-')));
            final int month = Integer.parseInt(hyphenDate.substring(hyphenDate.indexOf('-') + 1, hyphenDate.lastIndexOf('-')));
            final int date   = Integer.parseInt(hyphenDate.substring(hyphenDate.lastIndexOf('-') + 1));

            final String monthSpace = month < 10 ? " " : "";
            final String daySpace = date < 10 ? " " : "";
            String dayOfWeek = "";
            if (outputDayOfWeek) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month-1, date);
                dayOfWeek = dayOfWeekAbbv[cal.get(Calendar.DAY_OF_WEEK)];
            }
            return monthSpace + String.valueOf(month) + "月" + daySpace + String.valueOf(date) + "日" + dayOfWeek;
        }

        /*
         * count個の" "の文字列を得る
         */
        private String getSpace(final int count) {
            String spaces = "";
            for(int i = 0; i < count; i++) {
                spaces += " ";
            }
            return spaces;
        }

        /**
         * 校時リストのkintaiが1つでもkintaiListに含まれているかチェックする
         * @param periodList 校時リスト
         * @param kintaiList 勤怠リスト
         * @return 校時リストのkintaiが1つでもKintaiListに含まれていたらtrue、それ以外はfalse
         */
        private boolean containsKintai(final List periodList, final List kintaiList) {
            if (kintaiList == null || periodList == null) {
                return false;
            }

            for (final Iterator it = periodList.iterator(); it.hasNext();) {
                final Period period = (Period) it.next();
                if (kintaiList.contains(period._kintai)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * kintaiがkintaiListに含まれているかチェックする
         * @param periodList 校時リスト
         * @param kintaiList 勤怠リスト
         * @return kintaiがKintaiListに含まれていたらtrue、それ以外はfalse
         */
        private int countKintai(final List periodList, final List kintaiList, final Map kintaiCountMap) {
            if (periodList == null) {
                return 0;
            }
            int count = 0;
            for (final Iterator it = periodList.iterator(); it.hasNext();) {
                final Period period = (Period) it.next();
                final int c;
                if (kintaiList.contains(period._kintai)) {
                    c = getDiCdCount(period._kintai, kintaiCountMap);
                } else {
                    c = 0;
                }
                count += c;
            }
            return count;
        }

        /**
         * 勤怠が無届とする勤怠に含まれているかによって帳票に出力する届出有/無の文字列を返す。
         * @param kintai 勤怠
         * @param nonoticeKintaiList 無届とする勤怠
         * @return 帳票に出力する届出有/無の文字列
         */
        private String getNoticeString(final Integer kintai, final List nonoticeKintaiList) {
            return nonoticeKintaiList.contains(kintai) ? "(無)" : "  　";
        }

        /**
         * 読替勤怠を返す
         * @param source 読替元勤怠
         * @return 読替先勤怠。無ければ読替元勤怠を返す
         */
        private Integer getReplacedKintai(final Integer source) {
            for (int i = 0, len = replaceKintai.length; i < len; i++) {
                for (int s = 0, slen = replaceKintai[i].length - 1; s < slen; s++) {
                    if (source.equals(replaceKintai[i][s])) {
                        return replaceKintai[i][slen];
                    }
                }
            }
            return source;
        }
    }

    private class Student {

        final String _name;
        final String _schregno;
        final String _attendno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _staffName;
        /** true なら対象のデータ。*/
        final boolean _isTarget;

        public Student(final String name, final String schregno, final String attendno, final String grade, final String hrClass, final String hrName, final String statffName, final String countFlg) {
            _name = name;
            _schregno = schregno;
            _attendno = attendno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _staffName = statffName;
            _isTarget = countFlg == null || "1".equals(countFlg);
        }
    }

    /* 校時クラス */
    private class Period {
        final String _periodCd; // 校時コード
        final Integer _kintai;  // 勤怠コード読替
        final Integer _diCdYomikaeMae; //勤怠コード元

        Period(final String period,
                final Integer diCd,
                final Integer diCdYomikaeMae
        ) {
            _periodCd = period;
            _kintai = diCd;
            _diCdYomikaeMae = diCdYomikaeMae;
        }

        public String getNameForOutput(final List nonoticeKintaiList) {
            final int maxPeriodStringLength = 10;
            String name = _param.getPeriodName(_periodCd) + _param.getNoticeString(_kintai, nonoticeKintaiList);
            name += _param.getSpace(maxPeriodStringLength - name.getBytes().length);
            return name;
        }

        public String toString() {
            return _param.getPeriodName(_periodCd) + " : " + _kintai;
        }
    }

    /* 時間割 */
    private class AttendDat {
        String _date;
        String _semester;
        final List _periodList; // Periodクラスのリスト
        boolean _isInCheckArea; // 出力するチェック範囲に含まれているか

        public AttendDat(
                final String semester,
                final String day,
                final boolean isInCheckArea) {
            _semester = semester;
            _date = day;
            _isInCheckArea = isInCheckArea;
            _periodList = new ArrayList();
        }

        /**
         * dateの校時を追加する
         * @param period 校時
         */
        public void addPeriod(Period period) {
            _periodList.add(period);
        }

        public String toString() {
            return _date;
        }

        /**
         * 指定の勤怠に含まれる校時のイテレータを返す
         * @param kintai 指定の勤怠
         * @return 指定の勤怠に含まれる校時のイテレータ(サイズが0ならnull)
         */
        public Iterator getPeriodIteratorOf(List kintai) {
            if (kintai == null || _periodList == null)
                return null;
            final List rtn = new ArrayList();
            for(final Iterator it = _periodList.iterator(); it.hasNext(); ) {
                final Period period = (Period) it.next();
                for(int i = 0, len = kintai.size(); i < len; i++) {
                    if (period._kintai.equals(kintai.get(i))) {
                        rtn.add(period);
                        break;
                    }
                }
            }
            return (rtn.size() == 0) ? null : rtn.iterator();
        }

        /**
         * @return 1日の勤怠コードがすべて同一ならそのコードを返す。同一でなければ-1を返す。
         */
        public Integer getOnedayDiCd() {
            if (_periodList == null || _periodList.isEmpty()) {
                return diCdNone;
            }
            Integer dicd1 = ((Period) _periodList.get(0))._diCdYomikaeMae;
            dicd1 = _param.getReplacedKintai(dicd1);

            for (final Iterator it = _periodList.iterator(); it.hasNext();) {
                final Period period = (Period) it.next();
                final Integer replaced =_param.getReplacedKintai(period._diCdYomikaeMae);
                if (!dicd1.equals(replaced)) {
                    return diCdNone;
                }
            }
            return dicd1;
        }

        /**
         * 一日欠席か。
         * @return 一日欠席ならtrue、そうでなければfalseを返す。
         */
        public boolean isOnedayAbsent() {
            return diCdListAbsent.contains(getOnedayDiCd());
        }

        /**
         * 校時遅刻数を返す。
         * @return 校時遅刻数
         */
        public int getLateCount() {
            return _param.countKintai(_periodList, diCdListLate, _param._lateCountMap);
        }

        /**
         * 無届の欠席/遅刻/欠課が含まれているか。
         * @return 無届の欠席/遅刻/欠課が含まれているならtrue、そうでなければfalseを返す。
         */
        public boolean containsNonotice() {
            // 1日の勤怠コードが欠席(無届)コードリストに含まれていればtrue
            if (diCdListAbsentNonotice.contains(getOnedayDiCd())) return true;
            for (final Iterator it = _periodList.iterator(); it.hasNext();) {
                // それぞれの校時の勤怠コードが遅刻(無届)/欠課(無届)コードリストに含まれていればtrue
                final Period period = (Period) it.next();
                if (diCdListKekkaNonotice.contains(period._kintai)) return true;
                if (diCdListLateNonotice.contains(period._kintai)) return true;
            }
            return false;
        }

        /**
         * 欠時数を返す。
         * @return 欠時数
         */
        public int getLacktime() {
            return _param.countKintai(_periodList, diCdListKekka, Collections.EMPTY_MAP);
        }

        /**
         * 欠課回数を返す。
         * @return 欠課回数
         */
        public int getKekkaCount() {
            boolean wasKekka = false;
            int count = 0;

            if (isOnedayAbsent()) {
                return count;
            }

            for (final Iterator it = _periodList.iterator(); it.hasNext();) {
                final Period period = (Period) it.next();
                final boolean isKekka = diCdListKekka.contains(period._kintai);
                if (!wasKekka && isKekka) {
                    count += 1;
                }
                wasKekka = isKekka;
            }
            return count;
        }

        /**
         * デバッグ用出力 月日と校時を出力する
         */
        public void debugOutput() {
            log.debug("date = " + _date);
            for (final Iterator it = _periodList.iterator(); it.hasNext();) {
                log.debug("  " + it.next());
            }
            log.debug("");
        }
    }

    static class NameMstC040 {
        private static List _C040List = Collections.EMPTY_LIST;
        private static String _scheduleStartDay = "";

        final String _year;
        final String _semester;
        final String _month;

        public static String getScheduleStartDate() {
            return _scheduleStartDay;
        }

        public static String sqlSemeMonth() {
            final StringBuffer stb = new StringBuffer();
            stb.append("(");
            String comma = "";
            for (final Iterator it = _C040List.iterator(); it.hasNext();) {
                final NameMstC040 c040 = (NameMstC040) it.next();
                stb.append(comma + "'" + c040._semester + c040._month + "'");
                comma = ",";
            }
            stb.append(")");
            return stb.toString();
        }

        public static void load(final DB2UDB db2, final String year, final String accumulateStartDate) throws SQLException {
            PreparedStatement ps;
            ResultSet rs;

            _C040List = new ArrayList(10);

            ps = db2.prepareStatement(sqlC040(year));
            rs = ps.executeQuery();
            while (rs.next()) {
                NameMstC040 c040 = new NameMstC040(rs.getString("YEAR"), rs.getString("SEMESTER"), rs.getString("MONTH"));
                _C040List.add(c040);
            }
            DbUtils.closeQuietly(null, ps, rs);

            if (_C040List.isEmpty()) {
                _scheduleStartDay = accumulateStartDate;
            } else {
                final NameMstC040 last = (NameMstC040) _C040List.get(_C040List.size() - 1);
                final String sqlAppoitedDay = sqlAppointedDay(last._year, last._semester, last._month);
                log.debug(" sql appointedday = " + sqlAppoitedDay);
                ps = db2.prepareStatement(sqlAppoitedDay);
                rs = ps.executeQuery();
                String appointedDay = "";
                if (rs.next()) {
                    appointedDay = rs.getString("APPOINTED_DAY");
                }
                DbUtils.closeQuietly(null, ps, rs);

                final java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(java.sql.Date.valueOf(last._year + "-" + last._month + "-" + appointedDay));
                cal.add(Calendar.DATE, 1);

                final DecimalFormat df4 = new DecimalFormat("0000");
                final DecimalFormat df2 = new DecimalFormat("00");
                final String y = df4.format(cal.get(Calendar.YEAR));
                final String m = df2.format(cal.get(Calendar.MONTH) + 1);
                final String d = df2.format(cal.get(Calendar.DATE));

                _scheduleStartDay =  y + "-" + m + "-" + d;
            }
            log.debug(" SCHEDULE START = " + _scheduleStartDay);
        }

        private static String sqlC040(final String year) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   NAME1 AS YEAR, ");
            stb.append("   NAME2 AS SEMESTER, ");
            stb.append("   NAME3 AS MONTH ");
            stb.append(" FROM ");
            stb.append("   NAME_MST ");
            stb.append(" WHERE ");
            stb.append("   NAMECD1 = 'C040' AND NAME1 = '" + year + "' ");
            stb.append(" ORDER BY ");
            stb.append("   NAMECD2 ");
            return stb.toString();
        }

        private static String sqlAppointedDay(final String year, final String semester, final String month) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   MIN(APPOINTED_DAY) AS APPOINTED_DAY ");
            stb.append(" FROM ");
            stb.append("   ATTEND_SEMES_DAT ");
            stb.append(" WHERE ");
            stb.append("   YEAR = '" + year +  "' AND SEMESTER = '" + semester + "' AND MONTH = '" + month + "' ");
            return stb.toString();
        }

        NameMstC040(final String year, final String semester, final String month) {
            _year = year;
            _semester = semester;
            _month = month;
        }
    }

    /* 学期 */
    private static class Semester {
        final Integer _semes;
        final String _name;
        final String _sdate;
        final String _edate;

        /* 表に出力する欠席日数、遅刻回数、欠時数、欠課回数 */
        Integer _absentCount;
        Integer _lateCount;
        Integer _lacktime;
        Integer _kekkaCount;

        public Semester(final Integer semes, final String name, final String sdate, final String edate) {
            _semes = semes;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }

        public String toString() {
            return _semes.toString();
        }

        public String toDebugString() {
            return "("+_semes+","+_name+","+_sdate+","+_edate+")";
        }
    }

    /* 帳票出力のためのオブジェクト */
    private class SvfOutput {
        final String _fieldname;
        final String _output;

        SvfOutput(final String fieldname,
                    final String output) {
            _fieldname = fieldname;
            _output = output;
        }

        public String toString() {
            return "("+_fieldname + " , " + _output + ")";
        }
    }
}


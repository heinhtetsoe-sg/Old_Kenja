// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2021/03/19
 * 作成者: Nutec
 *
 */
package servletpack.KNJC;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [出欠管理]
 *
 *                  ＜ＫＮＪＣ１２０Ａ＞  個人別出席簿
 */
public class KNJC120A {

    private static final Log log = LogFactory.getLog(KNJC120A.class);
    private static final String AMIKAKE_ATTR = "Paint=(1,80,2),Bold=1";
    private boolean _hasData;
    private Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException, IOException {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); //帳票におけるＳＶＦおよびＤＢ２の設定
        Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        // print svf設定
        sd.setSvfInit(request, response, svf);
        // ＤＢ接続
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error");
            return;
        }
        // 印刷処理
        _param = createParam(db2, request);
        _hasData = false;
        printMain(db2, svf);
        // 終了処理
        sd.closeSvf(svf, _hasData);
        sd.closeDb(db2);
    }

    //  ＳＶＦ作成処理
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        //SVF出力
        setForm(svf, "KNJC120A.xml", 1);

        for (int i = 0; i < _param._schregnos.length; i++) {
            final String schregno = _param._schregnos[i];
            printHeader(db2, svf, schregno); //生徒名等出力
            printMonth(db2,  svf, schregno); //月別出欠表出力
            printHoliday(db2, svf); //土日出力
            printPublicHoliday(db2, svf); //祝日出力
            printLongHoliday(db2,   svf); //長期休み出力
            printYear(db2, svf, schregno); //全体出欠表出力
            _hasData = true;
            svf.VrEndPage();
        }

        DbUtils.closeQuietly(_param.ps1);
        DbUtils.closeQuietly(_param.ps2);
        DbUtils.closeQuietly(_param.ps3);
        db2.commit();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision$ $Date$");
        final Param paramap = new Param(db2, request);
        return paramap;
    }

    /*----------------*
     * 見出し等の出力
     *----------------*/
    private void printHeader(final DB2UDB db2, final Vrw32alp svf, String schregno) {
        try {
            if (null == _param.ps1) {
                final String sql = sqlStudentInfo(schregno);
                PreparedStatement ps = null;
                ResultSet rs = null;
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                //月
                for (int i = 1; i <= 12; i++) {
                    if (i <= 3) {
                        svf.VrsOutn("MONTH", (i + 9), new Integer(i).toString());
                    } else {
                        svf.VrsOutn("MONTH", (i - 3), new Integer(i).toString());
                    }
                }
                //日にち
                for (int i = 1; i <= 31; i++) {
                    svf.VrsOutn("DAY", i, new Integer(i).toString());
                }
                //欠課コードサンプル
                svf.VrsOut("DI_MARK_SAMPLE1", "・：出席  公：公欠  ／：欠席      忌：忌引き  停：出席停止  遅：遅刻");
                svf.VrsOut("DI_MARK_SAMPLE2", "早：早退  留：留学  ◆：遅刻早退  休：休学    除：出欠統計から除外");

                while (rs.next()) {
                    //年度
                    svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(rs.getString("YEAR"))) + "年度");

                    //学部
                    if ("H".equals(_param._schoolkind) == true) {
                        svf.VrsOut("SCHOOL_KIND", "高");
                    } else if ("J".equals(_param._schoolkind) == true) {
                        svf.VrsOut("SCHOOL_KIND", "中");
                    } else if ("P".equals(_param._schoolkind) == true) {
                        svf.VrsOut("SCHOOL_KIND", "小");
                    } else if ("K".equals(_param._schoolkind) == true) {
                        svf.VrsOut("SCHOOL_KIND", "幼");
                    }

                    //学年・クラス・出席番号
                    svf.VrsOut("GRADE", rs.getString("GRADE_NAME"));
                    svf.VrsOut("HR_CLASS", rs.getString("HR_CLASS_NAME2"));
                    svf.VrsOut("ATTENDNO", rs.getString("ATTENDNO"));

                    //氏名かな
                    {
                        int nameKanaLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("NAME_KANA"));
                        String nameKanaField = (nameKanaLen <= 30)? "1" : "2";
                        svf.VrsOut("KANA" + nameKanaField, rs.getString("NAME_KANA"));
                    }

                    //氏名
                    {
                        int nameLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("NAME"));
                        String nameField = (nameLen <= 20)? "1" : (nameLen <= 30)? "2" : "3";
                        svf.VrsOut("NAME" + nameField, rs.getString("NAME"));
                    }

                    //担任氏名
                    {
                        int staffNameLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("STAFFNAME"));
                        String staffNameField = (staffNameLen <= 20)? "1" : (staffNameLen <= 30)? "2" : "3";
                        svf.VrsOut("TR_NAME" + staffNameField, rs.getString("STAFFNAME"));
                    }
                }
            }
        } catch (Exception ex) {
            log.error("[KNJC120A]printHeader read error!", ex);
        }
    }

    /*------------------*
     * 月別集計表の出力
     *------------------*/
    private void printMonth(final DB2UDB db2, final Vrw32alp svf, String schregno) {
        try {
            if (null == _param.ps2) {
                final String monthSql = getMonthSql(schregno);
                PreparedStatement ps = null;
                ResultSet rs = null;
                ps = db2.prepareStatement(monthSql);
                rs = ps.executeQuery();
                String[] endAttendDate = _param._endAttendance.split("-");
                int endAttendM = Integer.parseInt(endAttendDate[1]);
                int endAttendD = Integer.parseInt(endAttendDate[2]);

                while (rs.next()) {
                    String[] attendDate = rs.getString("ATTENDDATE").split("-");
                    int attendM = Integer.parseInt(attendDate[1]);
                    int attendD = Integer.parseInt(attendDate[2]);
NEXT_MONTH_LABEL:
                     for (int i = 0; i < _param._monthArray.length; i++) {
                        final int month = Integer.parseInt(_param._monthArray[i]);
                        for (int j = 1; j <= 31; j++) {
                            //出力対象日付に達したとき
                            if (endAttendM == month && endAttendD == j) {
                                if (attendM == month && attendD == j && rs.getString("DI_MARK") != null) {
                                    svf.VrsOutn("attend" + j, (i + 1), rs.getString("DI_MARK"));
                                }
                                break NEXT_MONTH_LABEL;
                            }

                            int y = Integer.parseInt(_param._year);
                            if (1 <= month && month <= 3)
                            {
                                //1～3月は翌年の日付とする
                                y++;
                            }
                            int d = (j + 1);

                            if (isValidDate(y, month - 1, d) == false)
                            {
                                //存在しない日付はスキップ
                                continue;
                            }

                            //出欠記号出力
                            if (attendM == month && attendD == j && rs.getString("DI_MARK") != null) {
                                svf.VrsOutn("attend" + j, (i + 1), rs.getString("DI_MARK"));
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("[KNJC120A]printMonth read error!", ex);
        }
    }

    //土日出力
    private void printHoliday(final DB2UDB db2, final Vrw32alp svf) {
        try {
            if (null == _param.ps2) {
                final String holidaySql = getHolidayDate();
                PreparedStatement ps = null;
                ResultSet rs = null;
                ps = db2.prepareStatement(holidaySql);
                rs = ps.executeQuery();
                rs.next();
                HolidayBase hB = new HolidayBase(rs);
                Calendar cal = Calendar.getInstance();

                for (int i = 0; i < _param._monthArray.length; i++) {
                    final int month = Integer.parseInt(_param._monthArray[i]);
                    for (int j = 0; j < 31; j++) {
                        int y = Integer.parseInt(_param._year);
                        if (1 <= month && month <= 3)
                        {
                            //1～3月は翌年の日付とする
                            y++;
                        }
                        int d = (j + 1);
                        if (isValidDate(y, month - 1, d) == false)
                        {
                            //存在しない日付はスキップ
                            continue;
                        }
                        cal.set(y, month - 1, d);

                        //土曜日のとき
                        if ("1".equals(isSubHolidaySatSun(cal, hB)) == true) {
                            svf.VrsOutn("attend" + (j + 1), (i + 1), "土");
                        }

                        //日曜日のとき
                        if ("2".equals(isSubHolidaySatSun(cal, hB)) == true) {
                            svf.VrsOutn("attend" + (j + 1), (i + 1), "日");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("[KNJC120A]printHoliday read error!", ex);
        }
    }

    //祝日を出力
    private void printPublicHoliday(final DB2UDB db2, final Vrw32alp svf) {
        try {
            if (null == _param.ps2) {
                final String publicHolidaySql = getPublicHolidayDate();
                PreparedStatement ps = null;
                ResultSet rs = null;
                ps = db2.prepareStatement(publicHolidaySql);
                rs = ps.executeQuery();
                Calendar cal = Calendar.getInstance();

                while (rs.next()) {
                    String day = "";
                    if ("1".equals(rs.getString("HOLIDAY_DIV")) == true) {

                        int holidayYear = Integer.parseInt(rs.getString("YEAR"));
                        if ("01".equals(rs.getString("HOLIDAY_MONTH")) == true ||
                            "02".equals(rs.getString("HOLIDAY_MONTH")) == true ||
                            "03".equals(rs.getString("HOLIDAY_MONTH")) == true) {

                            holidayYear++;
                        }
                        day = holidayYear + "-" + rs.getString("HOLIDAY_MONTH") + "-" + rs.getString("HOLIDAY_DAY");
                    }
                    for (int i = 0; i < _param._monthArray.length; i++) {
                        final int month = Integer.parseInt(_param._monthArray[i]);
                        for (int j = 0; j < 31; j++) {
                            int y = Integer.parseInt(_param._year);
                            if (1 <= month && month <= 3)
                            {
                                //1～3月は翌年の日付とする
                                y++;
                            }
                            int d = (j + 1);
                            if (isValidDate(y, month - 1, d) == false)
                            {
                                //存在しない日付は網掛けして次の日付へ
                                svf.VrAttributen("attend" + (j + 1), (i + 1), AMIKAKE_ATTR);
                                continue;
                            }
                            cal.set(y, month - 1, d);

                            if ("1".equals(rs.getString("HOLIDAY_DIV")) == true && isSubHoliday(cal, day) == true) {
                                svf.VrsOutn("attend" + (j + 1), (i + 1), "□");
                            }

                            if ("2".equals(rs.getString("HOLIDAY_DIV")) == true &&
                                isSubHolidayKaisu(cal, rs.getString("HOLIDAY_MONTH"), rs.getString("HOLIDAY_WEEKDAY"), rs.getString("HOLIDAY_WEEK_PERIOD")) == true) {

                                svf.VrsOutn("attend" + (j + 1), (i + 1), "□");
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("[KNJC120A]printPublicHoliday read error!", ex);
        }
    }

    //長期休みを出力
    private void printLongHoliday(final DB2UDB db2, final Vrw32alp svf) {
        try {
            if (null == _param.ps2) {
                final String holidaySql = getHolidayDate();
                PreparedStatement ps = null;
                ResultSet rs = null;
                ps = db2.prepareStatement(holidaySql);
                rs = ps.executeQuery();
                rs.next();
                HolidayBase hB = new HolidayBase(rs);
                Calendar cal = Calendar.getInstance();

                for (int i = 0; i < _param._monthArray.length; i++) {
                    final int month = Integer.parseInt(_param._monthArray[i]);
                    for (int j = 0; j < 31; j++) {
                        int y = Integer.parseInt(_param._year);
                        if (1 <= month && month <= 3)
                        {
                            //1～3月は翌年の日付とする
                            y++;
                        }
                        int d = (j + 1);
                        cal.set(y, month - 1, d);

                        //長期休みのとき
                        if (isSubHolidayVacation(cal, hB) == true) {
                            svf.VrsOutn("attend" + (j + 1), (i + 1), "―");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("[KNJC120A]printLongHoliday read error!", ex);
        }
    }

    /*----------------*
     * 全体表の出力
     *----------------*/
    private void printYear(final DB2UDB db2, final Vrw32alp svf, String schregno) {
        try {
            if (null == _param.ps3) {
                String[] startAettendDate = _param._startAttendance.split("-");
                String[] endAettendDate   = _param._endAttendance.split("-");
                final String yearSql      = getYearSql(schregno, startAettendDate[1], endAettendDate[1]);

                PreparedStatement ps = null;
                ResultSet rs = null;
                ps = db2.prepareStatement(yearSql);
                rs = ps.executeQuery();
                int[][] semesterDate = new int[4][7];
                final Map<String, AttendData> attendDataMap = new HashMap();

                while (rs.next()) {

                    final AttendData attendData = new AttendData(rs.getString("MONTH"),
                            rs.getInt("LESSON"),
                            rs.getInt("SUSPEND"),
                            rs.getInt("MOURNING"),
                            rs.getInt("SICK"),
                            rs.getInt("LATE"),
                            rs.getInt("EARLY"),
                            rs.getString("REMARK1")
                            );
                    attendDataMap.put(rs.getString("MONTH"), attendData);

                    //学期毎の集計
                    {
                        int semiIdx = 0;
                        if ("1".equals(rs.getString("SEMESTER")) == true) {
                            semiIdx = 0;//1学期
                        } else if ("2".equals(rs.getString("SEMESTER")) == true) {
                            semiIdx = 1;//2学期
                        } else if ("3".equals(rs.getString("SEMESTER")) == true) {
                            semiIdx = 2;//3学期
                        }

                        if (rs.getString("LESSON") != null) {
                            semesterDate[semiIdx][0] += Integer.parseInt(rs.getString("LESSON"));
                        }
                        if (rs.getString("SUSPEND") != null) {
                            semesterDate[semiIdx][1] += Integer.parseInt(rs.getString("SUSPEND"));
                        }
                        if (rs.getString("MOURNING") != null) {
                            semesterDate[semiIdx][2] += Integer.parseInt(rs.getString("MOURNING"));
                        }
                        if (rs.getString("SICK") != null) {
                            semesterDate[semiIdx][3] += Integer.parseInt(rs.getString("SICK"));
                        }
                        if (rs.getString("LATE") != null) {
                            semesterDate[semiIdx][4] += Integer.parseInt(rs.getString("LATE"));
                        }
                        if (rs.getString("EARLY") != null) {
                            semesterDate[semiIdx][5] += Integer.parseInt(rs.getString("EARLY"));
                        }
                    }
                }

                //年度合計
                for (int i = 0; i < 6; i++) {
                    int temp = 0;
                    for (int semiIdx=0; semiIdx<3; semiIdx++) {
                        temp += semesterDate[semiIdx][i];
                    }
                    semesterDate[3][i] = temp;
                }

                for (int i = 0; i < _param._monthArray.length; i++) {
                    final String month = _param._monthArray[i];
                    final AttendData attendData = (AttendData) attendDataMap.get(month);

                    final int lesson        = attendData != null ? attendData._lesson   : 0;
                    final int suspend       = attendData != null ? attendData._suspend  : 0;
                    final int mourning      = attendData != null ? attendData._mourning : 0;
                    final int sick           = attendData != null ? attendData._sick     : 0;
                    final int late           = attendData != null ? attendData._late     : 0;
                    final int early          = attendData != null ? attendData._early    : 0;
                    final int syutteikibiki = suspend + mourning;
                    final int kasyusseki     = lesson - syutteikibiki;
                    final int syusseki       = kasyusseki - sick;
                    final String remark        = attendData != null ? attendData._remark    : "";

                    svf.VrsOutn("LESSON" , (i + 1), String.valueOf(lesson));
                    svf.VrsOutn("SUSPEND", (i + 1), String.valueOf(syutteikibiki));
                    svf.VrsOutn("SUBEKI" , (i + 1), String.valueOf(kasyusseki));
                    svf.VrsOutn("ATTEND" , (i + 1), String.valueOf(syusseki));
                    svf.VrsOutn("SICK"   , (i + 1), String.valueOf(sick));
                    svf.VrsOutn("LATE"   , (i + 1), String.valueOf(late));
                    svf.VrsOutn("EARLY"  , (i + 1), String.valueOf(early));
                    svf.VrsOutn("REMARK" , (i + 1), remark);
                }

                //学期・年間合計表示
                for (int i = 0; i < 4; i++) {
                    int syutteikibikiTotal = semesterDate[i][1] + semesterDate[i][2];
                    int kasyussekiTotal     = semesterDate[i][0] -(semesterDate[i][1] + semesterDate[i][2]);
                    int syussekiTotal       = semesterDate[i][0] - (semesterDate[i][1] + semesterDate[i][2]) - semesterDate[i][3];
                    svf.VrsOutn("TOTAL_LESSON" , (i+1),  new Integer(semesterDate[i][0]).toString());
                    svf.VrsOutn("TOTAL_SUSPEND", (i+1),  new Integer(syutteikibikiTotal).toString());
                    svf.VrsOutn("TOTAL_SUBEKI" , (i+1),  new Integer(kasyussekiTotal).toString());
                    svf.VrsOutn("TOTAL_ATTEND" , (i+1),  new Integer(syussekiTotal).toString());
                    svf.VrsOutn("TOTAL_SICK"   , (i+1),  new Integer(semesterDate[i][3]).toString());
                    svf.VrsOutn("TOTAL_LATE"   , (i+1),  new Integer(semesterDate[i][4]).toString());
                    svf.VrsOutn("TOTAL_EARLY"  , (i+1),  new Integer(semesterDate[i][5]).toString());
                }
            }
        } catch (Exception ex) {
            log.error("[KNJC120A]printYear error!", ex);
        }
    }

    private void setForm(final Vrw32alp svf, final String form, final int data) {
        svf.VrSetForm(form, data);
        log.info(" form = " + form);
        if (_param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }

    private static int toInt(final String s, final int def) {
        if (!NumberUtils.isDigits(s)) {
            return def;
        }
        return Integer.parseInt(s);
    }

    /**長期休暇かチェック**/
    private boolean isSubHolidayVacation(Calendar cal, HolidayBase holidaBase) {
        Calendar sdate = Calendar.getInstance();
        Calendar edate = Calendar.getInstance();
        //春休み（始業式前）
        if (holidaBase.beforeSpringVacationFlg == 1) {
            sdate = setCalendar(holidaBase.beforeSpringVacationSdate);
            edate = setCalendar(holidaBase.beforeSpringVacationEdate);
            if (isDateRange(cal, sdate, edate) == true) {
                return true;
            }
        }
        //夏休み
        if (holidaBase.summerVacationFlg == 1) {
            sdate = setCalendar(holidaBase.summerVacationSdate);
            edate = setCalendar(holidaBase.summerVacationEdate);
            if (isDateRange(cal, sdate, edate) == true) {
                return true;
            }
        }
        //秋休み
        if (holidaBase.autumnVacationFlg == 1) {
            sdate = setCalendar(holidaBase.autumnVacationSdate);
            edate = setCalendar(holidaBase.autumnVacationEdate);
            if (isDateRange(cal, sdate, edate) == true) {
                return true;
            }
        }
        //冬休み
        if (holidaBase.winterVacationFlg == 1) {
            sdate = setCalendar(holidaBase.winterVacationSdate);
            edate = setCalendar(holidaBase.winterVacationEdate);
            if (isDateRange(cal, sdate, edate) == true) {
                return true;
            }
        }
        //春休み（終了式後）
        if (holidaBase.afterSpringVacationFlg == 1) {
            sdate = setCalendar(holidaBase.afterSpringVacationSdate);
            edate = setCalendar(holidaBase.afterSpringVacationEdate);
            if (isDateRange(cal, sdate, edate) == true) {
                return true;
            }
        }
        return false;
    }

    /**日付文字列を分割してCalendarクラスにセットする**/
    private Calendar setCalendar(String date) {
        Calendar calendar = Calendar.getInstance();
        //日付を分割
        String[] dateArr = date.split("-");
        int y = Integer.parseInt(dateArr[0]);
        int m = Integer.parseInt(dateArr[1]);
        int d = Integer.parseInt(dateArr[2]);

        calendar.set(y, m - 1, d);

        return calendar;
    }
    /**
     * 指定日付が妥当な日付かチェック
     *
     * 引数はCalendar.setでセットする値と同じ(monthが0-based、yearとdateは1-based)
     */
    private boolean isValidDate(int year, int month, int date) {
        //1～12月以外はNGとする
        if (month < 0 && 12 < month)
        {
            return false;
        }

        //28日まではOKとする
        if (date <= 28)
        {
            return true;
        }

        //上記日付以外はCalendarクラスを使って判定
        boolean ret = true;
        try{
            Calendar calTest = Calendar.getInstance();

            //非厳密モードがオフの場合、存在しない日付が
            //セットされた状態で呼び出されると例外が発生することを利用する
            calTest.setLenient(false);
            calTest.set(year, month, date);
            calTest.getTime();
        } catch (Exception ex){
            ret = false;
        }

        return ret;
    }

    /**指定日付が範囲内かチェック**/
    private boolean isDateRange(Calendar target, Calendar sdate, Calendar edate) {
        //判定のために調整
        if (   sdate.compareTo(target) <= 0
            && target.compareTo(edate) <= 0) {

            return true;
        } else {
            return false;
        }
    }

    /**土、日曜日かチェック**/
    private String isSubHolidaySatSun(Calendar cal, HolidayBase holidaBase) {
        String satSunFlag = "";
        //土曜日
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            //第1土曜日
            if (cal.get(Calendar.WEEK_OF_MONTH) == 1 && holidaBase.firstSaturdayFlg == 1) {
                return satSunFlag = "1";
            }
            //第2土曜日
            else if (cal.get(Calendar.WEEK_OF_MONTH) == 2 && holidaBase.secondSaturdayFlg == 1) {
                return satSunFlag = "1";
            }
            //第3土曜日
            else if (cal.get(Calendar.WEEK_OF_MONTH) == 3 && holidaBase.thirdSaturdayFlg == 1) {
                return satSunFlag = "1";
            }
            //第4土曜日
            else if (cal.get(Calendar.WEEK_OF_MONTH) == 4 && holidaBase.fourSaturdayFlg == 1) {
                return satSunFlag = "1";
            }
            //第5土曜日
            else if (cal.get(Calendar.WEEK_OF_MONTH) == 5 && holidaBase.fiveSaturdayFlg == 1) {
                return satSunFlag = "1";
            }
        }
        //日曜日
        else if (holidaBase.legalHolidayFlg == 1) {
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                return satSunFlag = "2";
            }
        }
        return satSunFlag;
    }

    /**祝祭日かチェック**/
    private boolean isSubHoliday(Calendar cal, String holiday) {
        // yyyy-MM-dd形式へ
        String strDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
        //祝祭日
        if (strDate.equals(holiday)) {
            return true;
        }
        return false;
    }

    /**回数曜日指定チェック**/
    private boolean isSubHolidayKaisu(Calendar cal, String month, String day, String week) {
        if ((cal.get(Calendar.MONTH) + 1) == Integer.parseInt(month)) {
            //曜日
            if (cal.get(Calendar.DAY_OF_WEEK) == Integer.parseInt(day)) {
                //第何曜日か判定
                if (cal.get(Calendar.WEEK_OF_MONTH) == Integer.parseInt(week)) {
                    return true;
                }
            }
        }
        return false;

    }

    //生徒基本情報取得
    private String sqlStudentInfo(final String schregno) {

        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT SRD.SCHREGNO ");
        stb.append("         , SRD.YEAR ");
        stb.append("         , SRD.GRADE ");
        stb.append("         , CASE ");
        stb.append("           WHEN SRD.GRADE = '01' THEN '1' ");
        stb.append("           WHEN SRD.GRADE = '02' THEN '2' ");
        stb.append("           WHEN SRD.GRADE = '03' THEN '3' ");
        stb.append("           ELSE NULL ");
        stb.append("           END AS GRADE_NAME ");
        stb.append("         , SRD.HR_CLASS  ");
        stb.append("         , SRH.HR_CLASS_NAME2 ");
        stb.append("         , SRD.ATTENDNO ");
        stb.append("         , SBM.NAME ");
        stb.append("         , SBM.NAME_KANA ");
        stb.append("         , SM.STAFFNAME ");
        stb.append("      FROM SCHREG_REGD_DAT SRD ");
        stb.append(" LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("        ON SRD.SCHREGNO = SBM.SCHREGNO ");
        stb.append(" LEFT JOIN STAFF_CLASS_HIST_DAT SCHD");
        stb.append("        ON SRD.YEAR = SCHD.YEAR ");
        stb.append("       AND SRD.GRADE = SCHD.GRADE ");
        stb.append("       AND SRD.SEMESTER = SCHD.SEMESTER ");
        stb.append("       AND SRD.HR_CLASS = SCHD.HR_CLASS ");
        stb.append("       AND SCHD.TR_DIV = '1' ");
        stb.append(" LEFT JOIN STAFF_MST SM");
        stb.append("	   ON SCHD.STAFFCD = SM.STAFFCD");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("        ON SRH.YEAR = SRD.YEAR ");
        stb.append("       AND SRH.GRADE = SRD.GRADE ");
        stb.append("       AND SRH.HR_CLASS = SRD.HR_CLASS ");
        stb.append("       AND SRH.SEMESTER = SRD.SEMESTER ");
        stb.append("     WHERE SRD.SCHREGNO = '" + schregno + "' ");
        stb.append("       AND SRD.YEAR     = '" + _param._year + "' ");
        stb.append("       AND SRD.SEMESTER = '" + _param._semester + "' ");

        return stb.toString();
    }

    /*------------------------*
     * PrepareStatement SQL
     *------------------------*/
    private String getMonthSql(final String schregno) {

        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT ADAY.YEAR ");
        stb.append("         , ADCD.DI_MARK ");
        stb.append("         , ADAY.ATTENDDATE ");
        stb.append("         , ADAY.DI_CD ");
        stb.append("      FROM ATTEND_DAY_DAT ADAY ");
        stb.append(" LEFT JOIN ATTEND_DI_CD_DAT ADCD ");
        stb.append("        ON ADCD.YEAR = ADAY.YEAR ");
        stb.append("       AND ADCD.DI_CD = ADAY.DI_CD ");
        stb.append("     WHERE ADAY.SCHREGNO = '" + schregno + "' ");
        stb.append("       AND ADAY.YEAR = '" + _param._year + "' ");
        stb.append("       AND ADAY.ATTENDDATE BETWEEN '" + _param._startAttendance + "' AND '" + _param._endAttendance
                + "' ");

        return stb.toString();
    }

    /*------------------------*
     * PrepareStatement SQL
     *------------------------*/
    private String getYearSql(final String schregno ,String startAettendDate, String endAettendDate) {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT ASD.YEAR ");
        stb.append("         , ASD.MONTH ");
        stb.append("         , ASD.SEMESTER ");
        stb.append("         , ASD.SCHREGNO ");
        stb.append("         , ASD.LESSON ");
        stb.append("         , NVL(ASD.SUSPEND, 0) AS SUSPEND ");
        stb.append("         , NVL(ASD.MOURNING, 0) AS MOURNING ");
        stb.append("         , (NVL(ASD.SICK, 0) + NVL(ASD.NOTICE, 0) + NVL(ASD.NONOTICE, 0)) AS SICK ");
        stb.append("         , NVL(ASD.LATE, 0) AS LATE ");
        stb.append("         , NVL(ASD.EARLY, 0) AS EARLY ");
        stb.append("         , ASR.REMARK1 ");
        stb.append("      FROM ATTEND_SEMES_DAT ASD ");
        stb.append(" LEFT JOIN ATTEND_SEMES_REMARK_DAT ASR ");
        stb.append("        ON ASR.COPYCD = ASD.COPYCD ");
        stb.append("       AND ASR.YEAR = ASD.YEAR ");
        stb.append("       AND ASR.MONTH = ASD.MONTH ");
        stb.append("       AND ASR.SEMESTER = ASD.SEMESTER ");
        stb.append("       AND ASR.SCHREGNO = ASD.SCHREGNO ");
        stb.append("     WHERE ASD.SCHREGNO = '" + schregno + "' ");
        stb.append("       AND ASD.YEAR = '" + _param._year + "' ");

        stb.append( splMonthBetween("ASD.MONTH", startAettendDate, endAettendDate));

        return stb.toString();
    }

    /**クラスに在籍する学籍番号取得**/
    private String getClassSchregnos(String year, String semester, String[] categorySelected) {

        final StringBuffer stb = new StringBuffer();

        stb.append("   SELECT ");
        stb.append("          SRD.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("          SCHREG_REGD_DAT SRD ");
        stb.append("    WHERE SRD.YEAR = '" + year + "' ");
        stb.append("      AND SRD.SEMESTER = '" + semester + "' ");
        stb.append("      AND SRD.GRADE || SRD.HR_CLASS IN " + SQLUtils.whereIn(true, categorySelected));
        stb.append(" ORDER BY ");
        stb.append("          SRD.GRADE ");
        stb.append("        , SRD.HR_CLASS ");
        stb.append("        , SRD.ATTENDNO ");

        return stb.toString();
    }

    /**土日・長期休み取得**/
    private String getHolidayDate() {

        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT HBM.LEGAL_HOLIDAY_FLG ");
        stb.append("      , HBM.FIRST_SATURDAY_FLG ");
        stb.append("      , HBM.SECOND_SATURDAY_FLG ");
        stb.append("      , HBM.THIRD_SATURDAY_FLG ");
        stb.append("      , HBM.FOUR_SATURDAY_FLG ");
        stb.append("      , HBM.FIVE_SATURDAY_FLG ");
        stb.append("      , HBM.BEFORE_SPRING_VACATION_FLG ");
        stb.append("      , HBM.BEFORE_SPRING_VACATION_SDATE ");
        stb.append("      , HBM.BEFORE_SPRING_VACATION_EDATE ");
        stb.append("      , HBM.SUMMER_VACATION_FLG ");
        stb.append("      , HBM.SUMMER_VACATION_SDATE ");
        stb.append("      , HBM.SUMMER_VACATION_EDATE ");
        stb.append("      , HBM.AUTUMN_VACATION_FLG ");
        stb.append("      , HBM.AUTUMN_VACATION_SDATE ");
        stb.append("      , HBM.AUTUMN_VACATION_EDATE ");
        stb.append("      , HBM.WINTER_VACATION_FLG ");
        stb.append("      , HBM.WINTER_VACATION_SDATE ");
        stb.append("      , HBM.WINTER_VACATION_EDATE ");
        stb.append("      , HBM.AFTER_SPRING_VACATION_FLG ");
        stb.append("      , HBM.AFTER_SPRING_VACATION_SDATE ");
        stb.append("      , HBM.AFTER_SPRING_VACATION_EDATE ");
        stb.append("   FROM HOLIDAY_BASE_MST HBM ");
        stb.append("  WHERE HBM.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        stb.append("    AND HBM.YEAR        = '" + _param._year + "' ");

        return stb.toString();
    }

    /**祝日取得**/
    private String getPublicHolidayDate() {

        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT PHM.YEAR ");
        stb.append("      , PHM.HOLIDAY_DIV ");
        stb.append("      , PHM.HOLIDAY_MONTH ");
        stb.append("      , PHM.HOLIDAY_DAY ");
        stb.append("      , PHM.HOLIDAY_WEEK_PERIOD ");
        stb.append("      , PHM.HOLIDAY_WEEKDAY ");
        stb.append("   FROM PUBLIC_HOLIDAY_MST PHM ");
        stb.append("  WHERE PHM.YEAR = '" + _param._year + "' ");

        return stb.toString();
    }

    /**SQLで月の絞り込みを行うBETWEEN句を作成**/
    private String splMonthBetween(final String targetDate, final String fromMonth, final String toMonth) {
        //月の大小関係を統一
        int startMon =  toInt(fromMonth, 0);
        int endMon   =  toInt(toMonth, 0);
        boolean changFlg = false;
        if (startMon > endMon) {
            int swapTemp = startMon;
            startMon = endMon;
            endMon = swapTemp;
            changFlg = true;
        }

        //開始年月日、終了年月日の(「YYYY/MM/DD」形式)文字列を作成
        final String startMM =  String.format("%02d", startMon);
        final String endMM   =  String.format("%02d", endMon);

        //WHERE句条件追加
        String sql = "";
        if (changFlg == true) {
            sql  = "       AND (   " + targetDate + " BETWEEN '01' AND '" + startMM + "'";
            sql += "            OR " + targetDate + " BETWEEN '" + endMM + "' AND '12')";
        }else {
            sql = "       AND " + targetDate + " BETWEEN '" + startMM + "' AND '" + endMM + "'";
        }

        return sql;
    }

    private class HolidayBase {
        //基本休日
        int legalHolidayFlg; //法定休日・日曜日
        int firstSaturdayFlg; //第1土曜
        int secondSaturdayFlg; //第2土曜
        int thirdSaturdayFlg; //第3土曜
        int fourSaturdayFlg; //第4土曜
        int fiveSaturdayFlg; //第5土曜
        int beforeSpringVacationFlg; //春休み（始業式前）フラグ
        String beforeSpringVacationSdate; //春休み（始業式前）開始日
        String beforeSpringVacationEdate; //春休み（始業式前）終了日
        int summerVacationFlg; //夏休みフラグ
        String summerVacationSdate; //夏休み開始日
        String summerVacationEdate; //夏休み終了日
        int autumnVacationFlg; //秋休みフラグ
        String autumnVacationSdate; //秋休み開始日
        String autumnVacationEdate; //秋休み終了日
        int winterVacationFlg; //冬休み終了日
        String winterVacationSdate; //冬休み開始日
        String winterVacationEdate; //冬休み終了日
        int afterSpringVacationFlg; //春休み（終了式後）フラグ
        String afterSpringVacationSdate; //春休み（終了式後）開始日
        String afterSpringVacationEdate; //春休み（終了式後）終了日

        public HolidayBase(ResultSet rs) {
            try {
                legalHolidayFlg = rs.getInt("LEGAL_HOLIDAY_FLG");
                firstSaturdayFlg = rs.getInt("FIRST_SATURDAY_FLG");
                secondSaturdayFlg = rs.getInt("SECOND_SATURDAY_FLG");
                thirdSaturdayFlg = rs.getInt("THIRD_SATURDAY_FLG");
                fourSaturdayFlg = rs.getInt("FOUR_SATURDAY_FLG");
                fiveSaturdayFlg = rs.getInt("FIVE_SATURDAY_FLG");
                beforeSpringVacationFlg = rs.getInt("BEFORE_SPRING_VACATION_FLG");
                beforeSpringVacationSdate = rs.getString("BEFORE_SPRING_VACATION_SDATE");
                beforeSpringVacationEdate = rs.getString("BEFORE_SPRING_VACATION_EDATE");
                summerVacationFlg = rs.getInt("SUMMER_VACATION_FLG");
                summerVacationSdate = rs.getString("SUMMER_VACATION_SDATE");
                summerVacationEdate = rs.getString("SUMMER_VACATION_EDATE");
                autumnVacationFlg = rs.getInt("AUTUMN_VACATION_FLG");
                autumnVacationSdate = rs.getString("AUTUMN_VACATION_SDATE");
                autumnVacationEdate = rs.getString("AUTUMN_VACATION_EDATE");
                winterVacationFlg = rs.getInt("WINTER_VACATION_FLG");
                winterVacationSdate = rs.getString("WINTER_VACATION_SDATE");
                winterVacationEdate = rs.getString("WINTER_VACATION_EDATE");
                afterSpringVacationFlg = rs.getInt("AFTER_SPRING_VACATION_FLG");
                afterSpringVacationSdate = rs.getString("AFTER_SPRING_VACATION_SDATE");
                afterSpringVacationEdate = rs.getString("AFTER_SPRING_VACATION_EDATE");

            } catch (SQLException e) {
                log.error("HolidayBase error!", e);
            }
        }
    }

    private class AttendData {
        final String _month;
        int _lesson;
        int _suspend;
        int _mourning;
        int _sick;
        int _late;
        int _early;
        String _remark;

        public AttendData(
                final String month,
                final int lesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int late,
                final int early,
                final String remark
        ) {
            _month = month;
            _lesson = lesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _late = late;
            _early = early;
            _remark = remark;
        }
    }

    private class Param {
        final String _year;
        final String _semester;
        final String _kubun;
        final String _startAttendance;
        final String _endAttendance;
        final String _ctrlDate;
        final String _gradeHrclass;
        final String _schoolkind;
        final String _schoolcd;
        final String[] _category_selected;
        final String[] _schregnos;
        final String _monthArray[] = { "04","05","06","07","08","09","10","11","12","01","02","03"};
        final boolean _isOutputDebug;

        //SQL作成
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        PreparedStatement ps4 = null;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year              = request.getParameter("CTRL_YEAR");              //年度
            _semester          = request.getParameter("CTRL_SEMESTER");          //学期
            _kubun             = request.getParameter("KUBUN");                  //表示切替区分
            _startAttendance   = KNJ_EditDate.H_Format_Haifun(request.getParameter("START_ATTENDANCE"));//出欠集計開始日付
            _endAttendance     = KNJ_EditDate.H_Format_Haifun(request.getParameter("END_ATTENDANCE"));  //出欠集計終了日付
            _ctrlDate          = request.getParameter("CTRL_DATE");              //学籍処理日
            _gradeHrclass      = request.getParameter("GRADE_HR_CLASS");         //学年・組
            _category_selected = request.getParameterValues("CATEGORY_SELECTED");//対象学籍番号;
            _schoolkind        = request.getParameter("SCHOOLKIND");             //学校種別
            _schoolcd          = request.getParameter("SCHOOLCD");               //学校コード

            PreparedStatement ps = null;
            ResultSet rs = null;
            String sql = null;
            String _categorySelectes = "";

            if ("2".equals(_kubun) == true) {
                try {
                    sql = getClassSchregnos(_year, _semester, _category_selected);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    int count = 0;
                    while (rs.next()) {
                        if (count == 0) {
                            _categorySelectes = rs.getString("SCHREGNO");
                        } else {
                            _categorySelectes += "," + rs.getString("SCHREGNO");
                        }
                        count++;
                    }
                } catch (SQLException e) {
                    log.error("Param (_kubun==2) error!", e);
                }
                _schregnos = _categorySelectes.split(",");
            } else {
                String[] sno = new String[_category_selected.length];
                for (int i = 0; i < _category_selected.length; i++) {
                    String[] no = _category_selected[i].split("-");
                    sno[i] = no[0];
                }
                _schregnos = sno;
            }
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2,
                    " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJAC120A' AND NAME = '" + propName + "' "));
        }
    }
}
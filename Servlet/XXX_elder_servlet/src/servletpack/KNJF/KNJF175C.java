// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2021/04/28
 * 作成者: Nutec
 *
 */
package servletpack.KNJF;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [保健管理]
 *
 *                  ＜ＫＮＪＦ１７５Ｃ＞  保健室日誌印刷
 */
public class KNJF175C {
    private static final Log log = LogFactory.getLog(KNJF175C.class);

    private boolean nonedata = false;                               //該当データなしフラグ
    private static final String DICD_KOUKETSU = "1";   //公欠
    private static final String DICD_SYUTTEI  = "2";   //出席停止
    private static final String DICD_KIBIKI   = "3";   //忌引き
    private static final String DICD_KESSEKI  = "6";   //欠席
    private static final String DICD_RYUGAKU  = "34";  //留学
    private static final String DICD_KYUGAKU  = "36";  //休学
    private static final String DICD_TYOUKAI  = "51";  //出席停止(懲戒)

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
    private Param _param = null;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
                     throws ServletException, IOException
    {
        final Vrw32alp svf = new Vrw32alp();            //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        PrintWriter outstrm = null;
        try {
            //  print設定
            response.setContentType("application/pdf");
            outstrm = new PrintWriter (response.getOutputStream());

            //  svf設定
            svf.VrInit();                               //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());       //PDFファイル名の設定

            sd.setSvfInit(request, response, svf);
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }

            _param = getParam(db2, request);

            printSvfMain(db2, svf);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();                //DBを閉じる
            outstrm.close();            //ストリームを閉じる
        }

    }//doGetの括り

    private static int toInt(final String s, final int def) {
        if (!NumberUtils.isDigits(s)) {
            return def;
        }
        return Integer.parseInt(s);
    }

    private void setForm(final Vrw32alp svf, final String form, final int data) {
        svf.VrSetForm(form, data);
        if (_param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }

    /** 帳票出力 **/
    private void printSvfMain(
        final DB2UDB db2,
        final Vrw32alp svf
    ) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql   = null;

        try {

            setForm(svf, "KNJF175C_1.xml", 1);

            sql = sqlHolidayBase();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();
            HolidayBase holidayBase = new HolidayBase(rs);

            //祝祭日
            ArrayList<String> holiday = new ArrayList<String>();
            sql = sqlHoliday();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                holiday.add(rs.getString("HOLIDAY"));
            }

            //日付の年の表示法上
            String yearType = "";
            sql = sqlYearType();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();
            yearType = rs.getString("NAME1");

            ArrayList<ArrayList<String>> header          = new ArrayList<ArrayList<String>>();
            ArrayList<ArrayList<String>> visitCnt        = new ArrayList<ArrayList<String>>();
            ArrayList<ArrayList<String>> visiter         = new ArrayList<ArrayList<String>>();
            ArrayList<ArrayList<String>> kessekiIchiran = new ArrayList<ArrayList<String>>();
            ArrayList<String> work;  //一時保管用

            //ヘッダー
            sql = sqlHeader();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                work = new ArrayList<String>();
                work.add(rs.getString("DATE"));               //日付
                work.add(rs.getString("WEATER"));             //天気
                work.add(rs.getString("TEMPERATURE"));        //気温
                work.add(rs.getString("HUMIDITY"));           //湿度
                work.add(rs.getString("CHECK_TIME"));         //水質検査時間
                work.add(rs.getString("COLOR"));              //水質検査(色)
                work.add(rs.getString("TURBIDITY"));          //水質検査(濁り)
                work.add(rs.getString("SMELL"));              //水質検査(臭い)
                work.add(rs.getString("TASTE"));              //水質検査(味)
                work.add(rs.getString("RESIDUAL_CHLORINE"));  //水質検査(残留塩素)
                work.add(rs.getString("WATER_REMARK"));       //水質検査(特記事項)
                work.add(rs.getString("EVENT"));              //行事
                work.add(rs.getString("DIARY"));              //日誌
                work.add(rs.getString("AED"));                //AED
                header.add(work);
            }

            //来室人数
            sql = sqlVisitCnt();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                work = new ArrayList<String>();
                work.add(rs.getString("ATTENDDATE"));    //日付
                work.add(rs.getString("GRADE"));         //学年
                work.add(rs.getString("GRADE_NAME2"));   //学年
                work.add(rs.getString("KESSEKI_COUNT")); //欠席
                work.add(rs.getString("SYUTTEI_COUNT")); //出席停止
                work.add(rs.getString("KIBIKI_COUNT"));  //忌引
                work.add(rs.getString("NAIKA_COUNT"));   //内科
                work.add(rs.getString("GEKA_COUNT"));    //外科
                work.add(rs.getString("SOUDAN_COUNT"));  //健康相談
                work.add(rs.getString("SONOTA_COUNT"));  //その他
                visitCnt.add(work);
            }

            //来室記録
            sql = sqlRaishitsu();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                work = new ArrayList<String>();
                work.add(rs.getString("VISIT_DATE"));     //日付
                work.add(rs.getString("GRADE_NAME1"));     //年
                work.add(rs.getString("HR_CLASS_NAME1")); //組
                work.add(rs.getString("ATTENDNO"));       //出席番号
                work.add(rs.getString("NAME"));           //名前
                work.add(rs.getString("SEX"));            //性別
                work.add(rs.getString("VISIT_TIME"));     //来室時間
                work.add(rs.getString("VISIT_REASON1"));  //来室理由1
                work.add(rs.getString("VISIT_REASON2"));  //来室理由2
                work.add(rs.getString("VISIT_REASON3"));  //来室理由3
                visiter.add(work);
            }

            //欠席者一覧
            sql = sqlKessekiIchiran();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                work = new ArrayList<String>();
                work.add(rs.getString("DATE"));            //日付
                work.add(rs.getString("SCHREGNO"));        //学籍番号
                work.add(rs.getString("GRADE_NAME1"));      //学年
                work.add(rs.getString("HR_CLASS_NAME1"));  //組
                work.add(rs.getString("ATTENDNO"));        //番号
                work.add(rs.getString("NAME"));            //氏名
                work.add(rs.getString("SEX"));             //性別
                work.add(rs.getString("DI_CD"));           //欠席理由
                kessekiIchiran.add(work);
            }

            //保健日誌登録日
            ArrayList<String> attenddate = new ArrayList<String>();
            sql = sqlAttenddate();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                attenddate.add(rs.getString("ATTENDDATE"));
            }

            //日付の和暦変換に使用
            Locale locale     = new Locale("ja", "JP", "JP");
            Calendar calendar = Calendar.getInstance();
            DateFormat japaneseFormat = new SimpleDateFormat("M月d日", locale);

            //帳票に印字
            String targetDate   = "";      //日付
            for (int dateCnt = 0; dateCnt < header.size(); dateCnt++) {
                try {
                    svf.VrEndPage();
                    setForm(svf, "KNJF175C_1.xml", 1);
                    //日付を保持
                    targetDate = ((ArrayList<String>)header.get(dateCnt)).get(0);
                    calendar   = setCalendar(targetDate);

                    //ヘッダー
                    String dateStr = "";
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                    if ("1".equals(yearType)) {
                        //和暦
                        dateStr = KNJ_EditDate.gengou(db2, calendar.get(Calendar.YEAR)) + "年" + japaneseFormat.format(calendar.getTime());
                    }
                    else {
                        //西暦
                        dateStr = sdf.format(calendar.getTime());
                    }
                    svf.VrsOut("NENDO", dateStr);

                    //曜日
                    String week = "";
                    switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.SUNDAY :
                        week = "日";
                        break;
                    case Calendar.MONDAY :
                        week = "月";
                        break;
                    case Calendar.TUESDAY :
                        week = "火";
                        break;
                    case Calendar.WEDNESDAY :
                        week = "水";
                        break;
                    case Calendar.THURSDAY :
                        week = "木";
                        break;
                    case Calendar.FRIDAY :
                        week = "金";
                        break;
                    case Calendar.SATURDAY :
                        week = "土";
                        break;
                    }
                    svf.VrsOut("WEEK", week + "曜日");

                    //天気
                    svf.VrsOut("WEATHER",      ((ArrayList<String>)header.get(dateCnt)).get(1));
                    //気温
                    svf.VrsOut("DEGREE",       ((ArrayList<String>)header.get(dateCnt)).get(2));
                    //湿度
                    svf.VrsOut("HUMIDITY",     ((ArrayList<String>)header.get(dateCnt)).get(3));
                    //水質検査時間
                    svf.VrsOut("INSPECT_TIME", ((ArrayList<String>)header.get(dateCnt)).get(4));
                    //水質検査(色)
                    svf.VrsOut("COLOR",        ((ArrayList<String>)header.get(dateCnt)).get(5));
                    //水質検査(濁り)
                    svf.VrsOut("TERBILITY",    ((ArrayList<String>)header.get(dateCnt)).get(6));
                    //水質検査(臭い)
                    svf.VrsOut("ODOR",         ((ArrayList<String>)header.get(dateCnt)).get(7));
                    //水質検査(味)
                    svf.VrsOut("TASTE",        ((ArrayList<String>)header.get(dateCnt)).get(8));
                    //水質検査(残留塩素)
                    svf.VrsOut("CHLORINE",     ((ArrayList<String>)header.get(dateCnt)).get(9));
                    //水質検査(特記事項)
                    svf.VrsOut("MENTION",      ((ArrayList<String>)header.get(dateCnt)).get(10));
                    //行事(改行して印字)
                    printReport(svf, "EVENT",  ((ArrayList<String>)header.get(dateCnt)).get(11), 46, 12);
                    //日誌(改行して印字)
                    printReport(svf, "DIARY",  ((ArrayList<String>)header.get(dateCnt)).get(12), 100, 7);
                    //AED
                    if (((ArrayList<String>)header.get(dateCnt)).get(13) != null) {
                        svf.VrsOutn("DIARY", 7, "AED　" + ((ArrayList<String>)header.get(dateCnt)).get(13));
                    }

                    //押印役職名
                    printStamp(svf);

                    //来室人数
                    int gradeColumn    = 0;
                    int totalSick      = 0;
                    int totalSuspend   = 0;
                    int totalMourning  = 0;
                    int totalType1     = 0;
                    int totalType2     = 0;
                    int totalType5     = 0;
                    int totalType3     = 0;
                    for (int vc = 0; vc < visitCnt.size(); vc++) {
                        if (targetDate.equals(((ArrayList<String>)visitCnt.get(vc)).get(0))) {
                            gradeColumn ++;
                            //学年
                            svf.VrsOutn("GRADE",    gradeColumn, ((ArrayList<String>)visitCnt.get(vc)).get(2));
                            //欠席
                            svf.VrsOutn("SICK",     gradeColumn, ((ArrayList<String>)visitCnt.get(vc)).get(3));
                            totalSick    += toInt(((ArrayList<String>)visitCnt.get(vc)).get(3), 0);
                            //出席停止
                            svf.VrsOutn("SUSPEND",  gradeColumn, ((ArrayList<String>)visitCnt.get(vc)).get(4));
                            totalSuspend += toInt(((ArrayList<String>)visitCnt.get(vc)).get(4), 0);
                            //忌引
                            svf.VrsOutn("MOURNING", gradeColumn, ((ArrayList<String>)visitCnt.get(vc)).get(5));
                            totalMourning += toInt(((ArrayList<String>)visitCnt.get(vc)).get(5), 0);
                            //内科
                            svf.VrsOutn("TYPE1",    gradeColumn, ((ArrayList<String>)visitCnt.get(vc)).get(6));
                            totalType1    += toInt(((ArrayList<String>)visitCnt.get(vc)).get(6), 0);
                            //外科
                            svf.VrsOutn("TYPE2",    gradeColumn, ((ArrayList<String>)visitCnt.get(vc)).get(7));
                            totalType2    += toInt(((ArrayList<String>)visitCnt.get(vc)).get(7), 0);
                            //健康相談
                            svf.VrsOutn("TYPE5",    gradeColumn, ((ArrayList<String>)visitCnt.get(vc)).get(8));
                            totalType5    += toInt(((ArrayList<String>)visitCnt.get(vc)).get(8), 0);
                            //その他
                            svf.VrsOutn("TYPE3",    gradeColumn, ((ArrayList<String>)visitCnt.get(vc)).get(9));
                            totalType3    += toInt(((ArrayList<String>)visitCnt.get(vc)).get(9), 0);
                        }
                    }
                    //合計欠席
                    svf.VrsOut("TOTAL_SICK",     String.valueOf(totalSick));
                    //合計出席停止
                    svf.VrsOut("TOTAL_SUSPEND",  String.valueOf(totalSuspend));
                    //合計忌引
                    svf.VrsOut("TOTAL_MOURNING", String.valueOf(totalMourning));
                    //合計内科
                    svf.VrsOut("TOTAL_TYPE1",    String.valueOf(totalType1));
                    //合計外科
                    svf.VrsOut("TOTAL_TYPE2",    String.valueOf(totalType2));
                    //合計健康相談
                    svf.VrsOut("TOTAL_TYPE5",    String.valueOf(totalType5));
                    //合計その他
                    svf.VrsOut("TOTAL_TYPE3",    String.valueOf(totalType3));

                    //来室記録
                    int gyoMax = 20;
                    int gyo = 0;
                    for (int visit = 0; visit < visiter.size(); visit++) {
                        if (targetDate.equals(((ArrayList<String>)visiter.get(visit)).get(0))) {
                            gyo++;
                            if (gyoMax < gyo) {
                                svf.VrEndPage();
                                svf.VrSetForm("KNJF175C_2.xml", 1);  //来室記録2枚目以降の帳票をセット
                                gyo -= gyoMax;
                                gyoMax = 40;
                            }
                            //年
                            svf.VrsOutn("GRADE_NAME", gyo, ((ArrayList<String>)visiter.get(visit)).get(1));
                            //組
                            svf.VrsOutn("HR_CLASS",   gyo, ((ArrayList<String>)visiter.get(visit)).get(2));
                            //番
                            svf.VrsOutn("ATTENDNO",   gyo, String.valueOf(toInt(((ArrayList<String>)visiter.get(visit)).get(3), 0)));
                            //氏名
                            svf.VrsOutn("NAME",       gyo, ((ArrayList<String>)visiter.get(visit)).get(4));
                            //性別
                            svf.VrsOutn("SEX",        gyo,  ((ArrayList<String>)visiter.get(visit)).get(5));
                            //来室時間
                            svf.VrsOutn("TIME",       gyo, ((ArrayList<String>)visiter.get(visit)).get(6));
                            //理由
                            String reason = "";
                            for (int col = 7; col < 10; col++) {
                                if (((ArrayList<String>)visiter.get(visit)).get(col) != null) {
                                    if ("".equals(reason) == false) {
                                        reason += "、";
                                    }
                                    reason += ((ArrayList<String>)visiter.get(visit)).get(col);
                                }
                            }
                            //理由
                            final int reasonLen = KNJ_EditEdit.getMS932ByteLength(reason);
                            final String reasonField = (reasonLen <= 46)? "": (reasonLen <= 60)? "_2" : "_3";
                            svf.VrsOutn("PROCESS" + reasonField, gyo, reason);
                        }
                    }

                    //欠席者一覧
                    if (_param._isPrintAttend == true) {
                        svf.VrEndPage();
                        setForm(svf, "KNJF175C_3.xml", 4);  //欠席者一覧の帳票をセット(4：レポートライターモード)
                        int recodeCnt = 0;  //来室件数
                        String schregno = "";

                        for (int kesseki = 0; kesseki < kessekiIchiran.size(); kesseki++) {
                            svf.VrEndPage();
                            if (targetDate.equals(((ArrayList<String>)kessekiIchiran.get(kesseki)).get(0))) {
                                recodeCnt++;
                                //来室件数
                                svf.VrsOut("COUNT", String.valueOf(recodeCnt));
                                //年
                                svf.VrsOut("GRADE_NAME", ((ArrayList<String>)kessekiIchiran.get(kesseki)).get(2));
                                //組
                                svf.VrsOut("HR_CLASS",   ((ArrayList<String>)kessekiIchiran.get(kesseki)).get(3));
                                //番
                                String attendno =       ((ArrayList<String>)kessekiIchiran.get(kesseki)).get(4);
                                svf.VrsOut("ATTENDNO",   String.valueOf(toInt(attendno, 0)));
                                //氏名
                                svf.VrsOut("NAME",       ((ArrayList<String>)kessekiIchiran.get(kesseki)).get(5));
                                //性別
                                svf.VrsOut("SEX",        ((ArrayList<String>)kessekiIchiran.get(kesseki)).get(6));
                                //欠席理由
                                svf.VrsOut("PROCESS",    ((ArrayList<String>)kessekiIchiran.get(kesseki)).get(7));

                                schregno = ((ArrayList<String>)kessekiIchiran.get(kesseki)).get(1);  //学籍番号を保持
                                String oneDayAgo = "";
                                String twoDayAgo = "";
                                calendar = setCalendar(targetDate);
                                while("".equals(twoDayAgo)) {
                                    calendar.add(Calendar.DAY_OF_MONTH, -1);  //1日前にする
                                    String strDate = new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(calendar.getTime());
                                    //土曜、日曜、祝祭日、長期休暇、公欠じゃない場合(公欠があるため生徒毎に日付を判断する)
                                    if (isHoliday(db2, schregno, calendar, holiday, holidayBase, attenddate) == false) {
                                        if ("".equals(oneDayAgo)) {
                                            oneDayAgo = strDate;
                                        } else {
                                            twoDayAgo = strDate;
                                        }
                                    }
                                }
                                //連続3日休んでいる場合、記号表示(出力日を1日とカウント)
                                sql = sqlKessekiCnt(oneDayAgo, twoDayAgo, schregno);
                                ps = db2.prepareStatement(sql);
                                rs = ps.executeQuery();
                                rs.next();
                                if ("2".equals(rs.getString("CNT"))) {
                                    //当日公欠でないか
                                    Calendar toDay = setCalendar(targetDate);
                                    if (isSubHolidayKouketsu(db2, toDay, schregno) == false) {
                                        //記号表示
                                        svf.VrsOut("HOLIDAY", "●");
                                    }
                                }
                                svf.VrsOut("DATE", dateStr);
                                svf.VrEndRecord();
                            }
                        }  //欠席者一覧のforの括り
                    }
                    nonedata = true;
                }catch (Exception e) {
                    //印字中にエラーが発生した場合、その日付を飛ばして印字する
                    continue;
                }
            }  //日付のforの括り
            if (nonedata) {
                svf.VrEndPage();
                svf.VrsOut("DUMMY", "　");
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("printSvfMain set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }

    /**押印役職名**/
    private void printStamp(final Vrw32alp svf) {
        StampData stampData = null;
        for (Iterator iterator = _param._stampData.keySet().iterator(); iterator.hasNext();) {
            final String key = (String) iterator.next();
            stampData =  (StampData) _param._stampData.get(key);
            //役職名
            svf.VrsOut("JOB_NAME" + stampData._seq, stampData._title);
            if (_param._printStamp01 && "1".equals(stampData._seq)) {
                svf.VrsOut("STAFFBTM_1", _param.getStampImageFile(stampData._stampName));
            }
            if (_param._printStamp02 && "2".equals(stampData._seq)) {
                svf.VrsOut("STAFFBTM_2", _param.getStampImageFile(stampData._stampName));
            }
            if (_param._printStamp03 && "3".equals(stampData._seq)) {
                svf.VrsOut("STAFFBTM_3", _param.getStampImageFile(stampData._stampName));
            }
            if (_param._printStamp04 && "4".equals(stampData._seq)) {
                svf.VrsOut("STAFFBTM_4", _param.getStampImageFile(stampData._stampName));
            }
            if (_param._printStamp05 && "5".equals(stampData._seq)) {
                svf.VrsOut("STAFFBTM_5", _param.getStampImageFile(stampData._stampName));
            }
        }
    }

    /**日付文字列を分割してCalendarクラスにセットする**/
    private Calendar setCalendar(String date) {
        Calendar calendar = Calendar.getInstance();
        //日付を分割
        int y = toInt(date.substring(0, 4),  0);
        int m = toInt(date.substring(5, 7),  0);
        int d = toInt(date.substring(8, 10), 0);

        calendar.set(y, m -1 , d, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    /**改行して印字する**/
    private void printReport(
            final Vrw32alp svf,
            final String fieldName,
            final String str,
            final int size,
            final int lineCnt
    ) {
        KNJObjectAbs knjobj = new KNJEditString();
        ArrayList arrlist = knjobj.retDividString(str, size, lineCnt);
        if ( arrlist != null ) {
            for (int i = 0; i < arrlist.size(); i++) {
                svf.VrsOutn(fieldName, i+1,  (String)arrlist.get(i) );
            }
        }
    }

    /**保健日誌登録日、土、日、祝祭日、長期休暇、公欠判定**/
    //true：カウント対象でない日
    //false：カウント対象日
    private boolean isHoliday(final DB2UDB db2, String schregno, Calendar cal, ArrayList<String> holiday, HolidayBase holidaBase, ArrayList<String> attenddate) {
        //長期休暇
        if (isSubHolidayVacation(cal, holidaBase) == true) {
            return true;
        }
        //公欠
        if (isSubHolidayKouketsu(db2, cal, schregno) == true) {
            return false;
        }
        //保健日誌登録日かチェック
        if (isAttenddate(cal, attenddate) == true) {
            return false;
        }
        //土、日曜日かチェック
        if (isSubHolidaySatSun(cal, holidaBase) == true) {
            return true;
        }
        //祝祭日かチェック
        if (isSubHoliday(cal, holiday) == true) {
            return true;
        }
        return false;
    }

    /**保健日誌登録日かチェック**/
    private boolean isAttenddate(Calendar cal, ArrayList<String> attenddate) {
        // yyyy-MM-dd形式へ
        String strDate = new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cal.getTime());
        //保健日誌登録日
        if (attenddate.contains(strDate)) {
            return true;
        }
        return false;
    }

    /**土、日曜日かチェック**/
    private boolean isSubHolidaySatSun(Calendar cal, HolidayBase holidaBase) {
        //土曜日
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            //第1土曜日
            if (cal.get(Calendar.WEEK_OF_MONTH) == 1 && holidaBase.first_saturday_flg == 1) {
                return true;
            }
            //第2土曜日
            else if (cal.get(Calendar.WEEK_OF_MONTH) == 2 && holidaBase.second_saturday_flg == 1) {
                return true;
            }
            //第3土曜日
            else if (cal.get(Calendar.WEEK_OF_MONTH) == 3 && holidaBase.third_saturday_flg == 1) {
                return true;
            }
            //第4土曜日
            else if (cal.get(Calendar.WEEK_OF_MONTH) == 4 && holidaBase.four_saturday_flg == 1) {
                return true;
            }
            //第5土曜日
            else if (cal.get(Calendar.WEEK_OF_MONTH) == 5 && holidaBase.five_saturday_flg == 1) {
                return true;
            }
        }
        //日曜日
        else if (holidaBase.legal_holiday_flg == 1) {
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                return true;
            }
        }
        return false;
    }

    /**祝祭日かチェック**/
    private boolean isSubHoliday(Calendar cal, ArrayList<String> holiday) {
        // yyyy-MM-dd形式へ
        String strDate = new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cal.getTime());
        for (int i = 0; i < holiday.size(); i++) {
            //祝祭日
            if (strDate.equals(holiday.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**長期休暇かチェック**/
    private boolean isSubHolidayVacation(Calendar cal, HolidayBase holidaBase) {
        Calendar sdate = Calendar.getInstance();
        Calendar edate = Calendar.getInstance();
        //春休み（始業式前）
        if (holidaBase.before_spring_vacation_flg == 1) {
            sdate = setCalendar(holidaBase.before_spring_vacation_sdate);
            edate = setCalendar(holidaBase.before_spring_vacation_edate);
            if (isDateRange(cal, sdate, edate) == true) {
                return true;
            }
        }
        //夏休み
        if (holidaBase.summer_vacation_flg == 1) {
            sdate = setCalendar(holidaBase.summer_vacation_sdate);
            edate = setCalendar(holidaBase.summer_vacation_edate);
            if (isDateRange(cal, sdate, edate) == true) {
                return true;
            }
        }
        //秋休み
        if (holidaBase.autumn_vacation_flg == 1) {
            sdate = setCalendar(holidaBase.autumn_vacation_sdate);
            edate = setCalendar(holidaBase.autumn_vacation_edate);
            if (isDateRange(cal, sdate, edate) == true) {
                return true;
            }
        }
        //冬休み
        if (holidaBase.winter_vacation_flg == 1) {
            sdate = setCalendar(holidaBase.winter_vacation_sdate);
            edate = setCalendar(holidaBase.winter_vacation_edate);
            if (isDateRange(cal, sdate, edate) == true) {
                return true;
            }
        }
        //春休み（終了式後）
        if (holidaBase.after_spring_vacation_flg == 1) {
            sdate = setCalendar(holidaBase.after_spring_vacation_sdate);
            edate = setCalendar(holidaBase.after_spring_vacation_edate);
            if (isDateRange(cal, sdate, edate) == true) {
                return true;
            }
        }
        return false;
    }

    /**公欠かチェック**/
    private boolean isSubHolidayKouketsu(final DB2UDB db2, Calendar cal, String schregno) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql   = null;

        // yyyy-MM-dd形式へ
        String strDate = new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cal.getTime());
        try {
            sql = sqlGetDI_CD(strDate, schregno);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                //「1：公欠」
                if (DICD_KOUKETSU.equals(rs.getString("DI_CD")) == true) {
                    return true;
                }
            }
        } catch (SQLException e) {
            log.error("isSubHolidayKouketsu error!", e);
        }

        return false;
    }

    /**指定日付が範囲内かチェック**/
    private boolean isDateRange(Calendar target, Calendar sdate, Calendar edate) {
        //判定のために調整
        if (   sdate.compareTo(target) <= 0
            && target.compareTo(edate) <= 0) {

            return true;
        }
        else {
            return false;
        }
    }

    /**基本休日**/
    private String sqlHolidayBase()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT * ");
        stb.append("  FROM HOLIDAY_BASE_MST ");
        stb.append(" WHERE YEAR        = '" + _param._year + "' ");
        stb.append("   AND SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");

        return stb.toString();
    }

    /**祝祭日**/
    private String sqlHoliday()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT HOLIDAY ");
        stb.append("  FROM HOLIDAY_MST ");

        return stb.toString();
    }

    /**日付の年の表示方法**/
    private String sqlYearType()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT NAME1 ");
        stb.append("  FROM NAME_MST ");
        stb.append(" WHERE NAMECD1 = 'Z012' ");
        stb.append("   AND NAMECD2 = '00' ");

        return stb.toString();
    }

    /**ヘッダー**/
    private String sqlHeader()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("   SELECT NDD.DATE ");
        stb.append("        , NM1.NAME1 AS WEATER ");
        stb.append("		, NDD.TEMPERATURE ");
        stb.append("		, NDD.HUMIDITY ");
        stb.append("		, NDD.CHECK_HOUR  || ':' || NDD.CHECK_MINUTE AS CHECK_TIME ");
        stb.append("		, NM2.NAME1 AS COLOR ");
        stb.append("		, NM3.NAME1 AS TURBIDITY ");
        stb.append("		, NM4.NAME1 AS SMELL ");
        stb.append("		, NM5.NAME1 AS TASTE ");
        stb.append("		, NDD.RESIDUAL_CHLORINE || 'mg/L' AS RESIDUAL_CHLORINE ");
        stb.append("		, NDD.WATER_REMARK ");
        stb.append("		, NDD.EVENT ");
        stb.append("		, NDD.DIARY ");
        stb.append("		, CASE  ");
        stb.append("		  WHEN NDD.AED IS NULL THEN NULL ");
        stb.append("		  ELSE NM6.NAME1 ");
        stb.append("		  END AS AED ");
        stb.append("     FROM NURSEOFF_DIARY_DAT NDD ");
        stb.append("LEFT JOIN NAME_MST NM1 ");
        stb.append("       ON NDD.WEATHER   = NM1.NAMECD2 ");
        stb.append("	  AND NM1.NAMECD1   = 'A006' ");
        stb.append("LEFT JOIN NAME_MST NM2 ");
        stb.append("       ON NDD.COLOR     = NM2.NAMECD2 ");
        stb.append("	  AND NM2.NAMECD1   = 'F152' ");
        stb.append("LEFT JOIN NAME_MST NM3 ");
        stb.append("       ON NDD.TURBIDITY = NM3.NAMECD2 ");
        stb.append("	  AND NM3.NAMECD1   = 'F153' ");
        stb.append("LEFT JOIN NAME_MST NM4 ");
        stb.append("       ON NDD.SMELL     = NM4.NAMECD2 ");
        stb.append("	  AND NM4.NAMECD1   = 'F154' ");
        stb.append("LEFT JOIN NAME_MST NM5 ");
        stb.append("       ON NDD.TASTE     = NM5.NAMECD2 ");
        stb.append("	  AND NM5.NAMECD1   = 'F155' ");
        stb.append("LEFT JOIN NAME_MST NM6 ");
        stb.append("       ON NDD.AED     = NM6.NAMECD2 ");
        stb.append("	  AND NM6.NAMECD1   = 'F156' ");
        stb.append("    WHERE NDD.DATE BETWEEN '" + _param._diaryDateFrom + "' ");
        stb.append("	                   AND '" + _param._diaryDateTo   + "' ");
        stb.append(" ORDER BY NDD.DATE");

        return stb.toString();
    }

    /**優先するDI_CD**/
    private String sqlYuusenDiCd()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("   SELECT n.DATE ");
        stb.append("        , n.YEAR ");
        stb.append("        , s.SCHREGNO ");
        stb.append("        , CASE WHEN c.ATTENDDATE IS NOT NULL ");
        stb.append("               THEN c.DI_CD ");
        stb.append("               WHEN b.ATTENDDATE IS NOT NULL ");
        stb.append("               THEN b.DI_CD ");
        stb.append("               WHEN a.ATTENDDATE IS NOT NULL ");
        stb.append("               THEN a.DI_CD ");
        stb.append("               ELSE NULL ");
        stb.append("          END DI_CD ");
        stb.append("     FROM NURSEOFF_DIARY_DAT n ");
        stb.append("LEFT JOIN (SELECT YEAR ");
        stb.append("                , SCHREGNO ");
        stb.append("             FROM SCHREG_REGD_DAT ");
        stb.append("         GROUP BY YEAR ");
        stb.append("                , SCHREGNO ");
        stb.append("          ) s ");
        stb.append("          ON n.YEAR = s.YEAR ");
        stb.append("LEFT JOIN ( ");
        stb.append("             SELECT x1.YEAR ");
        stb.append("                  , x1.SCHREGNO ");
        stb.append("                  , x1.ATTENDDATE ");
        stb.append("                  , x1.DI_CD ");
        stb.append("               FROM ATTEND_PETITION_DAT x1 ");
        stb.append("              WHERE x1.UPDATED >= ( ");
        stb.append("                                  SELECT MAX(x2.UPDATED) ");
        stb.append("                                    FROM ATTEND_PETITION_DAT x2 ");
        stb.append("                                   WHERE x2.YEAR       = x1.YEAR ");
        stb.append("                                     AND x2.SCHREGNO   = x1.SCHREGNO ");
        stb.append("                                     AND x2.ATTENDDATE = x1.ATTENDDATE ");
        stb.append("                                  )  ");
        stb.append("           GROUP BY x1.YEAR ");
        stb.append("                  , x1.SCHREGNO ");
        stb.append("                  , x1.ATTENDDATE ");
        stb.append("                  , x1.DI_CD ");
        stb.append("          ) a ");
        stb.append("       ON s.SCHREGNO = a.SCHREGNO ");
        stb.append("      AND n.YEAR     = a.YEAR ");
        stb.append("      AND n.DATE     = a.ATTENDDATE ");
        stb.append("LEFT JOIN ( ");
        stb.append("             SELECT y1.YEAR ");
        stb.append("                  , y1.SCHREGNO ");
        stb.append("                  , y1.ATTENDDATE ");
        stb.append("                  , y1.DI_CD ");
        stb.append("               FROM ATTEND_DAT y1 ");
        stb.append("              WHERE y1.UPDATED >= ( ");
        stb.append("                                  SELECT MAX(y2.UPDATED) ");
        stb.append("                                    FROM ATTEND_DAT y2 ");
        stb.append("                                   WHERE y2.YEAR       = y1.YEAR ");
        stb.append("                                     AND y2.SCHREGNO   = y1.SCHREGNO ");
        stb.append("                                     AND y2.ATTENDDATE = y1.ATTENDDATE ");
        stb.append("                                  )  ");
        stb.append("           GROUP BY y1.YEAR ");
        stb.append("                  , y1.SCHREGNO ");
        stb.append("                  , y1.ATTENDDATE ");
        stb.append("                  , y1.DI_CD ");
        stb.append("          ) b ");
        stb.append("       ON s.SCHREGNO = b.SCHREGNO ");
        stb.append("      AND n.YEAR     = b.YEAR ");
        stb.append("      AND n.DATE     = b.ATTENDDATE ");
        stb.append("LEFT JOIN ATTEND_DAY_DAT c ");
        stb.append("       ON s.SCHREGNO = c.SCHREGNO ");
        stb.append("      AND n.year     = c.YEAR ");
        stb.append("      AND n.DATE     = c.ATTENDDATE ");
        stb.append("    WHERE n.YEAR     = '" + _param._year + "' ");
        stb.append("      AND (   a.ATTENDDATE IS NOT NULL ");
        stb.append("           OR b.ATTENDDATE IS NOT NULL ");
        stb.append("           OR c.ATTENDDATE IS NOT NULL) ");
        stb.append("ORDER BY n.DATE ");
        stb.append("       , s.SCHREGNO ");

        return stb.toString();
    }

    /**欠席数**/
    //引数のdiCdで取得結果が変化
    //6:欠席数　2,51:出席停止数　3:忌引
    private String sqlKesseki(final String diCd)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("   SELECT APD.DATE AS ATTENDDATE ");
        stb.append("        , SRD.GRADE ");
        stb.append("        , COUNT(*) AS COUNT ");
        stb.append("     FROM ( ");
        stb.append("	         SELECT YEAR ");
        stb.append("			      , DATE ");
        stb.append("			      , DI_CD ");
        stb.append("			      , SCHREGNO ");
        stb.append("		       FROM YUUSEN  ");
        stb.append("			  WHERE DATE BETWEEN '" + _param._diaryDateFrom + "' ");
        stb.append("	                         AND '" + _param._diaryDateTo   + "' ");
        stb.append("                AND DI_CD IN ('" + diCd + "') ");
        stb.append("	      ) APD ");
        stb.append("LEFT JOIN ATTEND_DAY_DAT ADA ");
        stb.append("       ON ADA.YEAR        = APD.YEAR ");
        stb.append("	  AND ADA.ATTENDDATE  = APD.DATE ");
        stb.append("	  AND ADA.DI_CD       = APD.DI_CD ");
        stb.append("	  AND ADA.SCHREGNO    = APD.SCHREGNO ");
        stb.append("LEFT JOIN ( ");
        stb.append("              SELECT SCHREGNO ");
        stb.append("			       , YEAR ");
        stb.append("			       , GRADE ");
        stb.append("				FROM SCHREG_REGD_DAT ");
        stb.append("			   WHERE YEAR = '" + _param._year + "' ");
        stb.append("			GROUP BY SCHREGNO ");
        stb.append("			       , YEAR ");
        stb.append("			       , GRADE ");
        stb.append("          ) SRD ");
        stb.append("       ON SRD.YEAR        = APD.YEAR ");
        stb.append("	  AND SRD.SCHREGNO    = APD.SCHREGNO ");
        stb.append(" GROUP BY APD.DATE ");
        stb.append("        , SRD.GRADE ");

        return stb.toString();
    }

    /**来室**/
    //引数のtypeで取得結果が変化
    //1:内科　2:外科　5:健康相談　3,4:その他
    private String sqlVisitType(final String type)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("   SELECT NVD.VISIT_DATE ");
        stb.append("        , GRADE ");
        stb.append("        , COUNT(*) AS COUNT ");
        stb.append("     FROM NURSEOFF_VISITREC_DAT NVD ");
        stb.append("LEFT JOIN ( ");
        stb.append("              SELECT SCHREGNO ");
        stb.append("			       , GRADE ");
        stb.append("				FROM SCHREG_REGD_DAT ");
        stb.append("			   WHERE YEAR = '" + _param._year + "' ");
        stb.append("			GROUP BY SCHREGNO ");
        stb.append("			       , GRADE ");
        stb.append("          ) SRD ");
        stb.append("	   ON SRD.SCHREGNO = NVD.SCHREGNO ");
        stb.append("    WHERE NVD.TYPE     IN ('" + type + "') ");
        stb.append("	  AND NVD.VISIT_DATE BETWEEN '" + _param._diaryDateFrom + "' ");
        stb.append("	                         AND '" + _param._diaryDateTo   + "' ");
        stb.append(" GROUP BY NVD.VISIT_DATE ");
        stb.append("        , SRD.GRADE ");

        return stb.toString();
    }

    /**来室人数**/
    private String sqlVisitCnt()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("   WITH  YUUSEN AS ( ");
        stb.append(sqlYuusenDiCd());
        stb.append("),       KESSEKI AS ( ");
        stb.append(sqlKesseki(DICD_KESSEKI));
        stb.append("),       SYUTTEI AS ( ");
        stb.append(sqlKesseki(DICD_SYUTTEI + "', '" + DICD_TYOUKAI));
        stb.append("),        KIBIKI AS ( ");
        stb.append(sqlKesseki(DICD_KIBIKI));
        stb.append("),         NAIKA AS ( ");
        stb.append(sqlVisitType("1"));
        stb.append("),          GEKA AS ( ");
        stb.append(sqlVisitType("2"));
        stb.append("),        SOUDAN AS ( ");
        stb.append(sqlVisitType("5"));
        stb.append("),        SONOTA AS ( ");
        stb.append(sqlVisitType("3, 4"));
        stb.append("),          BASE AS ( ");
        stb.append("   SELECT NDD.DATE AS ATTENDDATE ");
        stb.append("        , SRG.GRADE ");
        stb.append("        , SRG.GRADE_NAME2 ");
        stb.append("     FROM NURSEOFF_DIARY_DAT NDD ");
        stb.append("        , SCHREG_REGD_GDAT SRG  ");
        stb.append("    WHERE SRG.YEAR           = '" + _param._year + "' ");
        stb.append("	  AND SRG.SCHOOL_KIND    = '" + _param._SCHOOL_KIND + "' ");
        stb.append("	  AND NDD.DATE BETWEEN '" + _param._diaryDateFrom + "' ");
        stb.append("	                   AND '" + _param._diaryDateTo   + "' ");
        stb.append(" GROUP BY NDD.DATE");
        stb.append("        , SRG.GRADE ");
        stb.append("        , SRG.GRADE_NAME2 ");
        stb.append(") ");
        stb.append("   SELECT BASE.ATTENDDATE ");
        stb.append("        , BASE.GRADE ");
        stb.append("        , BASE.GRADE_NAME2 ");
        stb.append("        , NVL(KESSEKI.COUNT, 0) AS KESSEKI_COUNT ");
        stb.append("        , NVL(SYUTTEI.COUNT, 0) AS SYUTTEI_COUNT ");
        stb.append("        , NVL(KIBIKI.COUNT , 0) AS KIBIKI_COUNT ");
        stb.append("        , NVL(NAIKA.COUNT  , 0) AS NAIKA_COUNT  ");
        stb.append("        , NVL(GEKA.COUNT   , 0) AS GEKA_COUNT ");
        stb.append("        , NVL(SOUDAN.COUNT , 0) AS SOUDAN_COUNT ");
        stb.append("        , NVL(SONOTA.COUNT , 0) AS SONOTA_COUNT ");
        stb.append("     FROM BASE ");
        stb.append("LEFT JOIN KESSEKI ");
        stb.append("	   ON KESSEKI.ATTENDDATE = BASE.ATTENDDATE ");
        stb.append("	  AND KESSEKI.GRADE      = BASE.GRADE ");
        stb.append("LEFT JOIN SYUTTEI ");
        stb.append("	   ON SYUTTEI.ATTENDDATE = BASE.ATTENDDATE ");
        stb.append("	  AND SYUTTEI.GRADE      = BASE.GRADE ");
        stb.append("LEFT JOIN KIBIKI ");
        stb.append("	   ON KIBIKI.ATTENDDATE  = BASE.ATTENDDATE ");
        stb.append("	  AND KIBIKI.GRADE       = BASE.GRADE ");
        stb.append("LEFT JOIN NAIKA ");
        stb.append("	   ON NAIKA.VISIT_DATE   = BASE.ATTENDDATE ");
        stb.append("	  AND NAIKA.GRADE        = BASE.GRADE ");
        stb.append("LEFT JOIN GEKA ");
        stb.append("	   ON GEKA.VISIT_DATE    = BASE.ATTENDDATE ");
        stb.append("	  AND GEKA.GRADE         = BASE.GRADE ");
        stb.append("LEFT JOIN SOUDAN ");
        stb.append("	   ON SOUDAN.VISIT_DATE  = BASE.ATTENDDATE ");
        stb.append("	  AND SOUDAN.GRADE       = BASE.GRADE ");
        stb.append("LEFT JOIN SONOTA ");
        stb.append("	   ON SONOTA.VISIT_DATE  = BASE.ATTENDDATE ");
        stb.append("	  AND SONOTA.GRADE       = BASE.GRADE ");
        stb.append(" ORDER BY BASE.ATTENDDATE");
        stb.append("        , BASE.GRADE ");

        return stb.toString();
    }

    /**来室記録**/
    private String sqlRaishitsu()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("   SELECT NVD.VISIT_DATE ");
        stb.append("        , SRG.GRADE_NAME1 ");
        stb.append("        , SRH.HR_CLASS_NAME1 ");
        stb.append("		, SRD.ATTENDNO ");
        stb.append("		, SBM.NAME ");
        stb.append("		, NM.NAME1 AS SEX ");
        stb.append("		, NVD.VISIT_HOUR || ':' || NVD.VISIT_MINUTE AS VISIT_TIME ");
        stb.append("		, CASE NVD.TYPE ");
        stb.append("		  WHEN 1 THEN NM1_1.NAME1 ");
        stb.append("		  WHEN 2 THEN NM1_2.NAME1 ");
        stb.append("		  WHEN 3 THEN NM1_3.NAME1 ");
        stb.append("		  WHEN 4 THEN NM1_4.NAME1 ");
        stb.append("		  WHEN 5 THEN NM1_5.NAME1 ");
        stb.append("		  END AS VISIT_REASON1 ");
        stb.append("		, CASE NVD.TYPE ");
        stb.append("		  WHEN 1 THEN NM2_1.NAME1 ");
        stb.append("		  WHEN 2 THEN NM2_2.NAME1 ");
        stb.append("		  WHEN 3 THEN NM2_3.NAME1 ");
        stb.append("		  WHEN 4 THEN NM2_4.NAME1 ");
        stb.append("		  WHEN 5 THEN NM2_5.NAME1 ");
        stb.append("		  END AS VISIT_REASON2 ");
        stb.append("		, CASE NVD.TYPE ");
        stb.append("		  WHEN 1 THEN NM3_1.NAME1 ");
        stb.append("		  WHEN 2 THEN NM3_2.NAME1 ");
        stb.append("		  WHEN 3 THEN NM3_3.NAME1 ");
        stb.append("		  WHEN 4 THEN NM3_4.NAME1 ");
        stb.append("		  WHEN 5 THEN NM3_5.NAME1 ");
        stb.append("		  END AS VISIT_REASON3 ");
        stb.append("     FROM NURSEOFF_VISITREC_DAT NVD ");
        stb.append("LEFT JOIN ( ");
        stb.append("              SELECT SCHREGNO ");
        stb.append("			       , YEAR ");
        stb.append("			       , GRADE ");
        stb.append("			       , HR_CLASS ");
        stb.append("			       , ATTENDNO ");
        stb.append("				FROM SCHREG_REGD_DAT ");
        stb.append("			   WHERE YEAR = '" + _param._year + "' ");
        stb.append("			GROUP BY SCHREGNO ");
        stb.append("			       , YEAR ");
        stb.append("			       , GRADE ");
        stb.append("			       , HR_CLASS ");
        stb.append("			       , ATTENDNO ");
        stb.append("          ) SRD ");
        stb.append("       ON SRD.SCHREGNO    = NVD.SCHREGNO ");
        stb.append("LEFT JOIN ( ");
        stb.append("              SELECT YEAR ");
        stb.append("			       , GRADE ");
        stb.append("			       , HR_CLASS ");
        stb.append("			       , GRADE_NAME ");
        stb.append("			       , HR_CLASS_NAME1 ");
        stb.append("			       , SEMESTER ");
        stb.append("				FROM SCHREG_REGD_HDAT ");
        stb.append("			   WHERE YEAR = '" + _param._year + "' ");
        stb.append("			GROUP BY YEAR ");
        stb.append("			       , GRADE ");
        stb.append("			       , HR_CLASS ");
        stb.append("			       , GRADE_NAME ");
        stb.append("			       , HR_CLASS_NAME1 ");
        stb.append("			       , SEMESTER ");
        stb.append("          ) SRH ");
        stb.append("       ON SRH.YEAR        = SRD.YEAR ");
        stb.append("      AND SRH.GRADE       = SRD.GRADE ");
        stb.append("      AND SRH.HR_CLASS    = SRD.HR_CLASS ");
        stb.append("LEFT JOIN SEMESTER_MST SSM ");
        stb.append("       ON SSM.YEAR        = SRD.YEAR ");
        stb.append("      AND SSM.SEMESTER    = SRH.SEMESTER ");
        stb.append("      AND SSM.SEMESTER    <> '9' ");
        stb.append("LEFT JOIN SCHREG_REGD_GDAT SRG ");
        stb.append("       ON SRG.YEAR        = SRD.YEAR ");
        stb.append("	  AND SRG.GRADE       = SRD.GRADE ");
        stb.append("	  AND SRG.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
        stb.append("LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("       ON SBM.SCHREGNO    = NVD.SCHREGNO ");
        stb.append("LEFT JOIN NAME_MST NM ");
        stb.append("       ON NM.NAMECD2     = SBM.SEX ");
        stb.append("	  AND NM.NAMECD1     = 'Z002' ");
        stb.append("LEFT JOIN NAME_MST NM1_1 ");
        stb.append("       ON NM1_1.NAMECD2  = NVD.VISIT_REASON1 ");
        stb.append("	  AND NM1_1.NAMECD1  = 'F200' ");
        stb.append("LEFT JOIN NAME_MST NM1_2 ");
        stb.append("       ON NM1_2.NAMECD2  = NVD.VISIT_REASON1 ");
        stb.append("	  AND NM1_2.NAMECD1  = 'F201' ");
        stb.append("LEFT JOIN NAME_MST NM1_3 ");
        stb.append("       ON NM1_3.NAMECD2  = NVD.VISIT_REASON1 ");
        stb.append("	  AND NM1_3.NAMECD1  = 'F203' ");
        stb.append("LEFT JOIN NAME_MST NM1_4 ");
        stb.append("       ON NM1_4.NAMECD2  = NVD.VISIT_REASON1 ");
        stb.append("	  AND NM1_4.NAMECD1  = 'F202' ");
        stb.append("LEFT JOIN NAME_MST NM1_5 ");
        stb.append("       ON NM1_5.NAMECD2  = NVD.VISIT_REASON1 ");
        stb.append("	  AND NM1_5.NAMECD1  = 'F219' ");
        stb.append("LEFT JOIN NAME_MST NM2_1 ");
        stb.append("       ON NM2_1.NAMECD2  = NVD.VISIT_REASON2 ");
        stb.append("	  AND NM2_1.NAMECD1  = 'F200' ");
        stb.append("LEFT JOIN NAME_MST NM2_2 ");
        stb.append("       ON NM2_2.NAMECD2  = NVD.VISIT_REASON2 ");
        stb.append("	  AND NM2_2.NAMECD1  = 'F201' ");
        stb.append("LEFT JOIN NAME_MST NM2_3 ");
        stb.append("       ON NM2_3.NAMECD2  = NVD.VISIT_REASON2 ");
        stb.append("	  AND NM2_3.NAMECD1  = 'F203' ");
        stb.append("LEFT JOIN NAME_MST NM2_4 ");
        stb.append("       ON NM2_4.NAMECD2  = NVD.VISIT_REASON2 ");
        stb.append("	  AND NM2_4.NAMECD1  = 'F202' ");
        stb.append("LEFT JOIN NAME_MST NM2_5 ");
        stb.append("       ON NM2_5.NAMECD2  = NVD.VISIT_REASON2 ");
        stb.append("	  AND NM2_5.NAMECD1  = 'F219' ");
        stb.append("LEFT JOIN NAME_MST NM3_1 ");
        stb.append("       ON NM3_1.NAMECD2  = NVD.VISIT_REASON3 ");
        stb.append("	  AND NM3_1.NAMECD1  = 'F200' ");
        stb.append("LEFT JOIN NAME_MST NM3_2 ");
        stb.append("       ON NM3_2.NAMECD2  = NVD.VISIT_REASON3 ");
        stb.append("	  AND NM3_2.NAMECD1  = 'F201' ");
        stb.append("LEFT JOIN NAME_MST NM3_3 ");
        stb.append("       ON NM3_3.NAMECD2  = NVD.VISIT_REASON3 ");
        stb.append("	  AND NM3_3.NAMECD1  = 'F203' ");
        stb.append("LEFT JOIN NAME_MST NM3_4 ");
        stb.append("       ON NM3_4.NAMECD2  = NVD.VISIT_REASON3 ");
        stb.append("	  AND NM3_4.NAMECD1  = 'F202' ");
        stb.append("LEFT JOIN NAME_MST NM3_5 ");
        stb.append("       ON NM3_5.NAMECD2  = NVD.VISIT_REASON3 ");
        stb.append("	  AND NM3_5.NAMECD1  = 'F219' ");
        stb.append("    WHERE NVD.VISIT_DATE BETWEEN SSM.SDATE AND SSM.EDATE ");
        stb.append(" ORDER BY NVD.VISIT_DATE ");
        stb.append("        , NVD.VISIT_HOUR ");
        stb.append("        , VISIT_MINUTE ");

        return stb.toString();
    }

    /**欠席者一覧**/
    private String sqlKessekiIchiran()
    {
        final String[] whereInList = {DICD_KOUKETSU, DICD_KESSEKI, DICD_KIBIKI, DICD_SYUTTEI, DICD_TYOUKAI, DICD_RYUGAKU, DICD_KYUGAKU};
        final String whereIn = SQLUtils.whereIn(true, whereInList);

        final StringBuffer stb = new StringBuffer();
        stb.append("   WITH  YUUSEN AS ( ");
        stb.append(sqlYuusenDiCd());
        stb.append(") ");
        stb.append("   SELECT APD.DATE ");
        stb.append("        , APD.SCHREGNO ");
        stb.append("		, SRG.GRADE_NAME1 ");
        stb.append("		, SRH.HR_CLASS_NAME1 ");
        stb.append("		, SRD.ATTENDNO ");
        stb.append("		, SBM.NAME ");
        stb.append("		, NM.NAME1 AS SEX ");
        stb.append("		, DI.DI_NAME1 AS DI_CD ");
        stb.append("     FROM YUUSEN APD ");
        stb.append("LEFT JOIN ( ");
        stb.append("              SELECT SCHREGNO ");
        stb.append("			       , YEAR ");
        stb.append("			       , GRADE ");
        stb.append("			       , HR_CLASS ");
        stb.append("			       , ATTENDNO ");
        stb.append("				FROM SCHREG_REGD_DAT ");
        stb.append("			   WHERE YEAR = '" + _param._year + "' ");
        stb.append("			GROUP BY SCHREGNO ");
        stb.append("			       , YEAR ");
        stb.append("			       , GRADE ");
        stb.append("			       , HR_CLASS ");
        stb.append("			       , ATTENDNO ");
        stb.append("          ) SRD ");
        stb.append("       ON SRD.SCHREGNO    = APD.SCHREGNO ");
        stb.append("LEFT JOIN ( ");
        stb.append("              SELECT YEAR ");
        stb.append("			       , GRADE ");
        stb.append("			       , HR_CLASS ");
        stb.append("			       , GRADE_NAME ");
        stb.append("			       , HR_CLASS_NAME1 ");
        stb.append("			       , SEMESTER ");
        stb.append("				FROM SCHREG_REGD_HDAT ");
        stb.append("			   WHERE YEAR = '" + _param._year + "' ");
        stb.append("			GROUP BY YEAR ");
        stb.append("			       , GRADE ");
        stb.append("			       , HR_CLASS ");
        stb.append("			       , GRADE_NAME ");
        stb.append("			       , HR_CLASS_NAME1 ");
        stb.append("			       , SEMESTER ");
        stb.append("          ) SRH ");
        stb.append("       ON SRH.YEAR        = SRD.YEAR ");
        stb.append("	  AND SRH.GRADE       = SRD.GRADE ");
        stb.append("	  AND SRH.HR_CLASS    = SRD.HR_CLASS ");
        stb.append("LEFT JOIN SEMESTER_MST SSM ");
        stb.append("       ON SSM.YEAR        = SRD.YEAR ");
        stb.append("      AND SSM.SEMESTER    = SRH.SEMESTER ");
        stb.append("      AND SSM.SEMESTER    <> '9' ");
        stb.append("LEFT JOIN SCHREG_REGD_GDAT SRG ");
        stb.append("       ON SRG.YEAR        = SRD.YEAR ");
        stb.append("	  AND SRG.GRADE       = SRD.GRADE ");
        stb.append("	  AND SRG.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
        stb.append("LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("       ON SBM.SCHREGNO    = APD.SCHREGNO ");
        stb.append("LEFT JOIN NAME_MST NM ");
        stb.append("       ON NM.NAMECD2      = SBM.SEX ");
        stb.append("	  AND NM.NAMECD1      = 'Z002' ");
        stb.append("LEFT JOIN ATTEND_DI_CD_DAT DI ");
        stb.append("       ON DI.YEAR         = APD.YEAR ");
        stb.append("	  AND DI.DI_CD        = APD.DI_CD ");
        stb.append("    WHERE SRD.YEAR        = '" + _param._year + "' ");
        //「1:公欠」「6:欠席」「3:忌引き」「2:出席停止」「51:出席停止(懲戒)」「34:留学」「36:休学」
        stb.append("	  AND APD.DI_CD IN " + whereIn + " ");
        stb.append("	  AND APD.DATE BETWEEN '" + _param._diaryDateFrom + "' ");
        stb.append("	                   AND '" + _param._diaryDateTo   + "' ");
        stb.append("	  AND APD.DATE BETWEEN SSM.SDATE AND SSM.EDATE ");
        stb.append(" GROUP BY APD.DATE ");
        stb.append("        , APD.SCHREGNO ");
        stb.append("		, SRG.GRADE_NAME1 ");
        stb.append("		, SRH.HR_CLASS_NAME1 ");
        stb.append("        , SRD.GRADE ");
        stb.append("        , SRD.HR_CLASS ");
        stb.append("		, SRD.ATTENDNO ");
        stb.append("		, SBM.NAME ");
        stb.append("		, NM.NAME1 ");
        stb.append("		, DI.DI_NAME1 ");
        stb.append(" ORDER BY APD.DATE ");
        stb.append("        , SRD.GRADE ");
        stb.append("        , SRD.HR_CLASS ");
        stb.append("		, SRD.ATTENDNO ");

        return stb.toString();
    }

    /**欠席理由(公欠かどうかのチェックに使用)**/
    private String sqlGetDI_CD(String date, String schregno)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("   WITH  YUUSEN AS ( ");
        stb.append(sqlYuusenDiCd());
        stb.append(") ");
        stb.append("   SELECT DATE ");
        stb.append("        , SCHREGNO ");
        stb.append("        , DI_CD");
        stb.append("     FROM YUUSEN ");
        stb.append("    WHERE DATE = '" + date + "'  ");
        stb.append("	  AND SCHREGNO   = '" + schregno + "' ");

        return stb.toString();
    }

    /**保健日誌登録日**/
    private String sqlAttenddate()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("  SELECT ATTENDDATE ");
        stb.append("    FROM ATTEND_DAY_DAT ");
        stb.append("   WHERE YEAR = '" + _param._year + "'  ");
        stb.append("   UNION ");
        stb.append("  SELECT DISTINCT ATTENDDATE ");
        stb.append("    FROM  ATTEND_DAT ");
        stb.append("   WHERE YEAR = '" + _param._year + "'  ");
        stb.append("   UNION ");
        stb.append("  SELECT DISTINCT ATTENDDATE ");
        stb.append("    FROM  ATTEND_PETITION_DAT ");
        stb.append("   WHERE YEAR = '" + _param._year + "'  ");
        stb.append("   UNION ");
        stb.append("  SELECT DATE ");
        stb.append("    FROM  NURSEOFF_DIARY_DAT ");
        stb.append("   WHERE YEAR = '" + _param._year + "'  ");
        stb.append("ORDER BY ATTENDDATE ");

        return stb.toString();
    }

    /**連続欠席日数**/
    //保健日誌がない日付も対象
    private String sqlKessekiCnt(String date1, String date2, String schregno)
    {
        final String[] whereInList = {DICD_KESSEKI, DICD_KIBIKI, DICD_SYUTTEI, DICD_TYOUKAI, DICD_RYUGAKU, DICD_KYUGAKU};
        final String whereIn = SQLUtils.whereIn(true, whereInList);

        final StringBuffer stb = new StringBuffer();
        stb.append("   SELECT COUNT(*) AS CNT");
        stb.append("     FROM ");
        stb.append("          ( ");
        stb.append("           SELECT ATTENDDATE ");
        stb.append("                , SCHREGNO ");
        stb.append("             FROM ATTEND_DAY_DAT ");
        stb.append("            WHERE ATTENDDATE IN ('" + date1 + "', '" + date2 + "') ");
        stb.append("	          AND SCHREGNO   = '" + schregno + "' ");
        stb.append("	          AND DI_CD IN " + whereIn + " ");
        stb.append("	       UNION");
        stb.append("           SELECT DISTINCT ATTENDDATE ");
        stb.append("                , SCHREGNO ");
        stb.append("             FROM ATTEND_DAT ");
        stb.append("            WHERE ATTENDDATE IN ('" + date1 + "', '" + date2 + "') ");
        stb.append("	          AND SCHREGNO   = '" + schregno + "' ");
        stb.append("	          AND DI_CD IN " + whereIn + " ");
        stb.append("	       UNION");
        stb.append("          SELECT DISTINCT ATTENDDATE ");
        stb.append("               , SCHREGNO ");
        stb.append("            FROM ATTEND_PETITION_DAT ");
        stb.append("           WHERE ATTENDDATE IN ('" + date1 + "', '" + date2 + "') ");
        stb.append("	         AND SCHREGNO   = '" + schregno + "' ");
        stb.append("	          AND DI_CD IN " + whereIn + " ");
        stb.append("	       ) ");

        return stb.toString();
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision$ $Date$"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

    private class Param {
        private final String _year;
        private final String _documentRoot;
        private String _imagepath;
        private String _extension;
        final boolean _isOutputDebug;
        private final String _diaryDateFrom;
        private final String _diaryDateTo;
        private final boolean _isPrintAttend;
        final String _SCHOOL_KIND;
        /** 写真データ格納フォルダ */
        private final String _imageDir;
        /** 写真データの拡張子 */
        private final String _imageExt;
        private final Map    _stampData;
        private boolean      _printStamp01 = false;
        private boolean      _printStamp02 = false;
        private boolean      _printStamp03 = false;
        private boolean      _printStamp04 = false;
        private boolean      _printStamp05 = false;
        private String        _remark6StampNo;
        private String        _remark7StampNo;
        private String        _remark8StampNo;
        private String        _remark9StampNo;
        private String        _ineiNotData;

        public Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year          = request.getParameter("YEAR");               //年度
            _diaryDateFrom = request.getParameter("SDATE").replace('/', '-');
            _diaryDateTo   = request.getParameter("EDATE").replace('/', '-');
            _isPrintAttend = request.getParameter("PRINT") != null;     //欠席者一覧印刷
            _SCHOOL_KIND   = request.getParameter("SCHKIND");
            _ineiNotData   = request.getParameter("INEINOTDATA");          //役職名をテーブルから取得したか

            _documentRoot  = request.getParameter("DOCUMENTROOT");

            _imageDir = "image/stamp";
            _imageExt = "bmp";
            _stampData = getStampData(db2);
            for (Iterator iterator = _stampData.keySet().iterator(); iterator.hasNext();) {
                final String key = (String) iterator.next();
                if ("1".equals(key)) {
                    _printStamp01 = null != request.getParameter("PRINT_STAMP_" + key);
                }
                if ("2".equals(key)) {
                    _printStamp02 = null != request.getParameter("PRINT_STAMP_" + key);
                }
                if ("3".equals(key)) {
                    _printStamp03 = null != request.getParameter("PRINT_STAMP_" + key);
                }
                if ("4".equals(key)) {
                    _printStamp04 = null != request.getParameter("PRINT_STAMP_" + key);
                }
                if ("5".equals(key)) {
                    _printStamp05 = null != request.getParameter("PRINT_STAMP_" + key);
                }
            }

            //  写真データ
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            try {
                returnval  = getinfo.Control(db2);
                _imagepath = returnval.val4;      //格納フォルダ
                _extension = returnval.val5;      //拡張子
            } catch (Exception e) {
                log.error("setHeader set error!", e);
            } finally {
                getinfo   = null;
                returnval = null;
            }
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

        }

        private Map<String, StampData> getStampData(final DB2UDB db2) throws SQLException {
            final Map retMap            = new TreeMap();
            PreparedStatement ps         = null;
            ResultSet rs                 = null;
            PreparedStatement psStampNo = null;
            ResultSet rsStampNo         = null;
            final String stamPSql      = getStampData();

            try {
                ps = db2.prepareStatement(stamPSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String stampNoSql = getMaxStampNo(rs.getString("FILE_NAME"));
                    psStampNo = db2.prepareStatement(stampNoSql);
                    rsStampNo = psStampNo.executeQuery();
                    rsStampNo.next();
                    final String stampNo = rsStampNo.getString("STAMP_NO");
                    final StampData stampData = new StampData(rs.getString("SEQ"), rs.getString("TITLE"), stampNo);
                    retMap.put(rs.getString("SEQ"), stampData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                DbUtils.closeQuietly(null, psStampNo, rsStampNo);
                db2.commit();
            }
            if (retMap.isEmpty()) {
                retMap.put("1", new StampData("1", "校長",     _remark6StampNo));
                retMap.put("2", new StampData("2", "教頭",     _remark7StampNo));
                retMap.put("3", new StampData("3", "保健主事", _remark8StampNo));
                retMap.put("4", new StampData("4", "養護教諭", ""));
            }
            return retMap;
        }

        private String getStampData() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT SEQ");
            stb.append("      , FILE_NAME ");
            stb.append("      , TITLE ");
            stb.append("   FROM PRG_STAMP_DAT");
            stb.append("  WHERE YEAR        = '" + _year + "' ");
            stb.append("    AND SEMESTER    = '9' ");
            stb.append("    AND SCHOOLCD    = '000000000000' ");
            stb.append("    AND SCHOOL_KIND = '" + _SCHOOL_KIND + "' ");
            stb.append("    AND SEQ IN (1,2,3,4,5) ");
            stb.append("    AND PROGRAMID   = 'KNJF175C' ");

            return stb.toString();
        }

        private String getMaxStampNo(final String staffcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT MAX(STAMP_NO) AS STAMP_NO");
            stb.append("   FROM ATTEST_INKAN_DAT");
            stb.append("  WHERE STAFFCD = '" + staffcd + "' ");

            return stb.toString();
        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJF175C' AND NAME = '" + propName + "' "));
        }

        /**
         * 写真データファイルの取得
         */
        private String getStampImageFile(final String filename) {
            if (null == filename) {
                return null;
            }
            if (null == _documentRoot) {
                return null;
            } // DOCUMENTROOT
            if (null == _imageDir) {
                return null;
            }
            if (null == _imageExt) {
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(_documentRoot);
            stb.append("/");
            stb.append(_imageDir);
            stb.append("/");
            stb.append(filename);
            stb.append(".");
            stb.append(_imageExt);
            File file1 = new File(stb.toString());
            if (!file1.exists()) {
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
        }
    }

    private class StampData {
        private final String _seq;
        private final String _title;
        private final String _stampName;
        public StampData(
                final String seq,
                final String title,
                final String stampName
                ) {
            _seq = seq;
            _title = title;
            _stampName = stampName;
        }
    }

    private class HolidayBase {
        //基本休日
        int    legal_holiday_flg;               //法定休日・日曜日
        int    first_saturday_flg;              //第1土曜
        int    second_saturday_flg;             //第2土曜
        int    third_saturday_flg;              //第3土曜
        int    four_saturday_flg;               //第4土曜
        int    five_saturday_flg;               //第5土曜
        int    before_spring_vacation_flg;      //春休み（始業式前）フラグ
        String before_spring_vacation_sdate;    //春休み（始業式前）開始日
        String before_spring_vacation_edate;    //春休み（始業式前）終了日
        int    summer_vacation_flg;             //夏休みフラグ
        String summer_vacation_sdate;           //夏休み開始日
        String summer_vacation_edate;           //夏休み終了日
        int    autumn_vacation_flg;             //秋休みフラグ
        String autumn_vacation_sdate;           //秋休み開始日
        String autumn_vacation_edate;           //秋休み終了日
        int    winter_vacation_flg;             //冬休み終了日
        String winter_vacation_sdate;            //冬休み開始日
        String winter_vacation_edate;            //冬休み終了日
        int    after_spring_vacation_flg;       //春休み（終了式後）フラグ
        String after_spring_vacation_sdate;      //春休み（終了式後）開始日
        String after_spring_vacation_edate;      //春休み（終了式後）終了日

        public HolidayBase(ResultSet rs) {
            try {
                legal_holiday_flg            = rs.getInt("LEGAL_HOLIDAY_FLG");
                first_saturday_flg           = rs.getInt("FIRST_SATURDAY_FLG");
                second_saturday_flg          = rs.getInt("SECOND_SATURDAY_FLG");
                third_saturday_flg           = rs.getInt("THIRD_SATURDAY_FLG");
                four_saturday_flg            = rs.getInt("FOUR_SATURDAY_FLG");
                five_saturday_flg            = rs.getInt("FIVE_SATURDAY_FLG");
                before_spring_vacation_flg   = rs.getInt("BEFORE_SPRING_VACATION_FLG");
                before_spring_vacation_sdate = rs.getString("BEFORE_SPRING_VACATION_SDATE");
                before_spring_vacation_edate = rs.getString("BEFORE_SPRING_VACATION_EDATE");
                summer_vacation_flg          = rs.getInt("SUMMER_VACATION_FLG");
                summer_vacation_sdate        = rs.getString("SUMMER_VACATION_SDATE");
                summer_vacation_edate        = rs.getString("SUMMER_VACATION_EDATE");
                autumn_vacation_flg          = rs.getInt("AUTUMN_VACATION_FLG");
                autumn_vacation_sdate        = rs.getString("AUTUMN_VACATION_SDATE");
                autumn_vacation_edate        = rs.getString("AUTUMN_VACATION_EDATE");
                winter_vacation_flg          = rs.getInt("WINTER_VACATION_FLG");
                winter_vacation_sdate        = rs.getString("WINTER_VACATION_SDATE");
                winter_vacation_edate        = rs.getString("WINTER_VACATION_EDATE");
                after_spring_vacation_flg    = rs.getInt("AFTER_SPRING_VACATION_FLG");
                after_spring_vacation_sdate  = rs.getString("AFTER_SPRING_VACATION_SDATE");
                after_spring_vacation_edate  = rs.getString("AFTER_SPRING_VACATION_EDATE");
            } catch (SQLException e) {
                log.error("HolidayBase error!", e);
            }
        }
    }
}//クラスの括り

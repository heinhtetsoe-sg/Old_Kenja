// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2021/04/30
 * 作成者: Nutec
 *
 */
package servletpack.KNJC;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [出欠管理]
 *
 *                  ＜ＫＮＪＣ１００Ａ＞  出欠簿（日報・週報・月報）
 */
public class KNJC100A {

    private static final Log log = LogFactory.getLog(KNJC100A.class);
    private boolean nonedata = false;
    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
    final int MAX_ROW = 45;
    final int MAX_PERIOD = 9;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static String FROM_TO_MARK = "\uFF5E";
    private static Param _param = null;
    private static final String NONE_GROUP_CODE = "0000";

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
    }

    //  ＳＶＦ作成処理
    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf) {

        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql   = null;

        try {
            //SVF出力
            setForm(svf, "KNJC100A_" + _param._disp + ".xml", 1);

            ArrayList<ArrayList<String>> periodChair  = new ArrayList<ArrayList<String>>();
            ArrayList<ArrayList<String>> diMark        = new ArrayList<ArrayList<String>>();
            ArrayList<String> work;  //一時保管用

            //生徒基本情報取得
            sql = sqlGradeClass();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ArrayList<Student> student = new ArrayList<Student>();
            while (rs.next()) {
                student.add(new Student(
                          rs.getString("SCHREGNO")       //学籍番号
                        , rs.getString("GRADE_CLASS")    //学年クラス
                        , rs.getString("GRADE_NAME")     //学年名
                        , rs.getString("HR_CLASS_NAME1") //クラス名
                        , rs.getString("ATTENDNO")       //出席番号
                        , rs.getString("NAME")           //生徒名
                        , rs.getString("NAME_KANA")      //生徒名かな
                        , rs.getString("STAFFNAME")      //担任名
                ));
            }

            //日毎の時限の科目
            ArrayList<String> printDate = new ArrayList<String>();
            sql = sqlPeriodChair();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                work = new ArrayList<String>();
                work.add(rs.getString("EXECUTEDATE"));   //日付
                work.add(rs.getString("GRADE_CLASS"));   //学年クラス
                work.add(rs.getString("PERIODCD"));      //時限
                work.add(rs.getString("CHAIRCD"));       //科目CD
                work.add(rs.getString("SUBCLASSABBV"));  //科目名
                if ("1".equals(_param._disp)) {
                    //日報の場合は生徒毎に科目を取得する
                    work.add(rs.getString("SCHREGNO"));  //学籍番号
                }
                if ("2".equals(_param._disp) == true) {
                    //週報
                    boolean addFlg = true;
                    //同じ日付、同じクラス、同じ時限にある科目名がperiodChairにあるものと違う場合、連結するしてaddしない
                    //科目名が同じ場合は連結とaddしない
                    for (ArrayList<String> pc : periodChair) {
                        if (   (0 <= pc.get(0).indexOf(rs.getString("EXECUTEDATE")))
                            && (0 <= pc.get(1).indexOf(rs.getString("GRADE_CLASS")))
                            && (0 <= pc.get(2).indexOf(rs.getString("PERIODCD")))) {
                            if (NONE_GROUP_CODE.equals(pc.get(5))) {
                                //群以外を入れている場合、群で更新する
                                pc.set(4, rs.getString("SUBCLASSABBV"));
                            }
                            if (pc.get(4).indexOf(rs.getString("SUBCLASSABBV")) < 0) {
                                //群を／で区切り結合
                                pc.set(4, pc.get(4) + "／" + rs.getString("SUBCLASSABBV"));
                            }
                            addFlg = false;
                            break;
                        }
                    }
                    if (addFlg) {
                        work.add(rs.getString("GROUPCD"));  //グループCD
                        periodChair.add(work);
                    }
                } else {
                    periodChair.add(work);
                }
                if (   "1".equals(_param._disp) == true
                    && printDate.indexOf(rs.getString("EXECUTEDATE")) < 0) {
                    //日報
                    printDate.add(rs.getString("EXECUTEDATE"));
                }
                else if ("3".equals(_param._disp) == true) {
                    //月報
                    String[] executedate = rs.getString("EXECUTEDATE").split("-");
                    if (printDate.indexOf(executedate[1]) < 0) {
                        printDate.add(executedate[1]);
                    }
                }
            }

            //日毎、生徒毎、時限と一日の出欠区分
            sql = sqlDiMark();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                work = new ArrayList<String>();
                work.add(rs.getString("ATTENDDATE"));  //日付
                work.add(rs.getString("SCHREGNO"));    //学籍番号
                work.add(rs.getString("PERIODCD"));    //時限
                work.add(rs.getString("CHAIRCD"));     //科目CD
                work.add(rs.getString("DI_MARK"));     //出欠区分
                diMark.add(work);
            }

            //生徒毎、月毎と年間の出席日数等
            sql = sqlSyusseki();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ArrayList<AttendData> attendData = new ArrayList<AttendData>();
            while (rs.next()) {
                attendData.add(new AttendData(
                          rs.getString("MONTH")          //月
                        , rs.getString("SCHREGNO")       //学籍番号
                        , rs.getString("OFFDAYS")        //休学
                        , rs.getString("LESSON")         //授業日数
                        , rs.getString("MOURNING")       //忌引
                        , rs.getString("SUSPEND")        //出席停止
                        , rs.getString("ABROAD")         //留学
                        , rs.getString("SICK")           //欠席
                        , rs.getString("LATE")           //遅刻
                        , rs.getString("EARLY")          //早退
                        , rs.getString("YEAR_OFFDAYS")   //年間休学
                        , rs.getString("YEAR_LESSON")    //年間授業日数
                        , rs.getString("YEAR_MOURNING")  //年間忌引
                        , rs.getString("YEAR_SUSPEND")   //年間出席停止
                        , rs.getString("YEAR_ABROAD")    //年間留学
                        , rs.getString("YEAR_SICK")      //年間欠席
                        , rs.getString("YEAR_LATE")      //年間遅刻
                        , rs.getString("YEAR_EARLY")     //年間早退
              ));
            }

            if ("1".equals(_param._disp)) {
                //日報
                printDisp1(svf, db2, printDate, student, periodChair, diMark);
            }
            else if ("2".equals(_param._disp)) {
                //週報
                 printDisp2(svf, db2, printDate, student, periodChair, diMark, attendData);
            }
            else {
                //月報
                printDisp3(svf, db2, printDate, student, periodChair, diMark, attendData);
            }

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

    //日報
    private void printDisp1(final Vrw32alp svf,
                              final DB2UDB db2,
                              ArrayList<String> printDate,
                              ArrayList<Student> student,
                              ArrayList<ArrayList<String>> periodChair,
                              ArrayList<ArrayList<String>> diMark) {

        for (int dateCnt = 0; dateCnt < printDate.size(); dateCnt++) {
            svf.VrEndPage();

            String targetDate  = printDate.get(dateCnt);
            String targetClass = "";
            int row = 0;
            for (int classCnt = 0; classCnt < student.size(); classCnt++) {
                if (  targetClass.equals(student.get(classCnt)._gradeClass) == false
                    || MAX_ROW <= row) {
                    //クラスが変わった時 or 1ページの最大数を超えるとき改ページする
                    if ("".equals(targetClass) == false) {
                        svf.VrEndPage();
                    }
                    targetClass = student.get(classCnt)._gradeClass;
                    //ヘッダー
                    printHeader(svf, 1, db2, student, classCnt, dateFormatConv(db2, targetDate));
                    row = 0;
                }
                row++;
                //学籍番号を保持、出席番号と生徒氏名を印字
                String schregno = setStudent(svf, student, classCnt, row);

                for (int periodCnt = 0; periodCnt < periodChair.size(); periodCnt++) {
                    if (   targetDate.equals(((ArrayList<String>)periodChair.get(periodCnt)).get(0)) == true
                        && targetClass.equals(((ArrayList<String>)periodChair.get(periodCnt)).get(1)) == true
                        && schregno.equals(((ArrayList<String>)periodChair.get(periodCnt)).get(5)) == true) {
                        //時限
                        String periodcd = ((ArrayList<String>)periodChair.get(periodCnt)).get(2);
                        int period = (toInt(periodcd, 0) - 1);
                        svf.VrsOut("PERIOD_NAME" + period, period + "時限");
                        //科目
                        svf.VrsOutn("SUBCLASSABBV" + period, row, ((ArrayList<String>)periodChair.get(periodCnt)).get(4));
                        //時限ごとの出欠区分
                        svf.VrsOutn("DI_MARK" + period, row, getDiMark(diMark, targetDate, schregno, periodcd));
                    }
                }  //periodCntの括り

                //一日出席区分
                svf.VrsOutn("DI_MARK11" , row, getDiMark(diMark, targetDate, schregno, "1日"));
                nonedata = true;

            }  //classCntの括り
        }  //dateCntの括り
    }

    //週報
    private void printDisp2(final Vrw32alp svf,
                              final DB2UDB db2,
                              ArrayList<String> printDate,
                              ArrayList<Student> student,
                              ArrayList<ArrayList<String>> periodChair,
                              ArrayList<ArrayList<String>> diMark,
                              ArrayList<AttendData> attendData) {

        //印刷範囲の再設定
        final Term term = Term.getTermValue(db2);
        final List<Week> weekList = getWeekList(db2, term);

        String targetClass = "";
        int row = 0;
        for (int wi = 0; wi < weekList.size(); wi++) {
            final Week week = weekList.get(wi);
            for (int classCnt = 0; classCnt < student.size(); classCnt++) {
                if (  targetClass.equals(student.get(classCnt)._gradeClass) == false
                    || MAX_ROW <= row) {
                    //クラスが変わった時 or 1ページの最大数を超えるとき改ページする
                    if ("".equals(targetClass) == false) {
                        svf.VrEndPage();
                    }
                    targetClass = student.get(classCnt)._gradeClass;
                    //ヘッダー
                    final int weekCnt = countWeek(_param._year, week._dateList.get(0));
                    String title = "第" + weekCnt + "週";
                    printHeader(svf, 1, db2, student, classCnt, title);
                    row = 0;
                }
                row++;
                //学籍番号を保持、出席番号と生徒氏名を印字
                String schregno = setStudent(svf, student, classCnt, row);

                // 1週間
                int cnt = 0;
                for (final String date : week._dateList) {
                    boolean classFlg = false;  //授業フラグ
                    cnt++;
                    //日付
                    svf.VrsOut("hymd" + cnt, dateFormatConv(db2, date));

                    for (int periodCnt = 0; periodCnt < periodChair.size(); periodCnt++) {
                        if (   date.equals(((ArrayList<String>)periodChair.get(periodCnt)).get(0)) == true
                            && targetClass.equals(((ArrayList<String>)periodChair.get(periodCnt)).get(1)) == true) {
                            //時限
                            String periodcd = ((ArrayList<String>)periodChair.get(periodCnt)).get(2);
                            int period = (toInt(periodcd, 0) - 1);
                            int col = (period +  (cnt - 1) * MAX_PERIOD);
                            svf.VrsOutn("KOUJI", col, String.valueOf(period));
                            //科目
                            svf.VrsOutn("subject", col, ((ArrayList<String>)periodChair.get(periodCnt)).get(4));
                            //時限ごとの出欠区分
                            svf.VrsOutn("attend" + col, row, getDiMark(diMark, date, schregno, periodcd));
                            classFlg = true;
                        }
                    }  //periodCntの括り

                    //一日出席区分
                    svf.VrsOutn("subject",  cnt * MAX_PERIOD, "1日の出欠");
                    if (classFlg == true) {
                        //授業があった日のみ、1日出欠区分を印字する
                        svf.VrsOutn("attend" +  cnt * MAX_PERIOD, row, getDiMark(diMark, date, schregno, "1日"));
                    }
                }
                //年度内累計
                printNenkanruikei(svf, schregno, attendData, row);
            }  //classCntの括り
            svf.VrEndPage();
            row = 0;
            targetClass = "";
            nonedata = true;
        }  //wiの括り
    }

    //月報
    private void printDisp3(final Vrw32alp svf,
                              final DB2UDB db2,
                              ArrayList<String> printDate,
                              ArrayList<Student> student,
                              ArrayList<ArrayList<String>> periodChair,
                              ArrayList<ArrayList<String>> diMark,
                              ArrayList<AttendData> attendData) {

        String targetClass = "";
        ArrayList<String> targetDate = new ArrayList<String>();
        int row = 0;
        for (int monthCnt = 0; monthCnt < printDate.size(); monthCnt++) {
            String month = printDate.get(monthCnt);
            int m = toInt(month, 0);
            for (int classCnt = 0; classCnt < student.size(); classCnt++) {
                if (  targetClass.equals(student.get(classCnt)._gradeClass) == false
                    || MAX_ROW <= row) {
                    //クラスが変わった時 or 1ページの最大数を超えるとき改ページする
                    if ("".equals(targetClass) == false) {
                        svf.VrEndPage();
                    }
                    targetClass = student.get(classCnt)._gradeClass;
                    //ヘッダー
                    int y = toInt(_param._year, 0);
                    if (1 <= m && m <= 3) {
                        y++;
                    }
                    String title = KNJ_EditDate.h_format_JP_N(db2, y + "/" + month + "/01");
                    title += m + "月";
                    printHeader(svf, 3, db2, student, classCnt, title);

                    targetDate = new ArrayList<String>();
                    for (int day = 0; day <= 31; day++) {
                        int d = (day + 1);

                        if (isValidDate(y, m - 1, d) == false) {
                            //存在しない日付はスキップ
                            continue;
                        }
                        svf.VrsOutn("DAY",  d, String.valueOf(d));
                        targetDate.add(y + "-" + month + "-" + String.format("%02d", d));
                        svf.VrsOutn("WEEK", d, KNJ_EditDate.h_format_W(y + "-" + m + "-" + d));  //曜日
                    }
                    row = 0;
                }
                row++;
                //学籍番号を保持、出席番号と生徒氏名を印字
                String schregno = setStudent(svf, student, classCnt, row);

                //月の日付
                for (int td = 0; td < targetDate.size(); td++) {
                    boolean classFlg = false;  //授業フラグ
                    for (int periodCnt = 0; periodCnt < periodChair.size(); periodCnt++) {

                        if (   targetDate.get(td).equals(((ArrayList<String>)periodChair.get(periodCnt)).get(0)) == true
                            && targetClass.equals(((ArrayList<String>)periodChair.get(periodCnt)).get(1)) == true) {
                            classFlg = true;
                            break;
                        }
                    }  //periodCntの括り
                    //一日出席区分
                    if (classFlg == true) {
                        //授業があった日のみ、1日出欠区分を印字する
                        svf.VrsOutn("attend" +  (td + 1), row, getDiMark(diMark, targetDate.get(td), schregno, "1日"));
                    }
                }

                //月内累計
                printGetsunairuikei(svf, schregno, attendData, row, month);

                //年度内累計
                printNenkanruikei(svf, schregno, attendData, row);
            }  //classCntの括り
            svf.VrEndPage();
            row = 0;
            targetClass = "";
            nonedata = true;
        }  //monthCntの括り

    }

    //ヘッダー
    private void printHeader(final Vrw32alp svf, int disp, final DB2UDB db2,
                               ArrayList<Student> student, int classCnt, String title) {
        String diMarkSample1 = "・：出席　公：公欠　／：欠席　忌：忌引き　停：出席停止";
        String diMarkSample2 = "遅：遅刻　早：早退　留：留学　◆：遅刻早退　休：休学";
        String diMarkSample3 = "徐：出欠統計から除外";
        if (disp != 3) {
            diMarkSample1 += "　懲：出席停止（懲戒）";
            diMarkSample2 += "　△：出席（中抜け）";
        }

        //印字日付
        if (disp != 3) {
            svf.VrsOut("DATE", title);
        }
        else {
            svf.VrsOut("MONTH", title);
        }

        //出欠記号
        svf.VrsOut("DI_MARK_SAMPLE1", diMarkSample1);
        svf.VrsOut("DI_MARK_SAMPLE2", diMarkSample2);
        svf.VrsOut("DI_MARK_SAMPLE3", diMarkSample3);

        //年組
        String hrName = student.get(classCnt)._gradeName + "年　";
        hrName += student.get(classCnt)._hrClassName + "組";
        svf.VrsOut("HR_NAME", hrName);
        //担任名
        svf.VrsOut("TR_NAME", student.get(classCnt)._staffName);
    }

    //出席番号と生徒氏名を印字し、学籍番号を返す
    private String setStudent(final Vrw32alp svf, ArrayList<Student> student, int classCnt, int row) {
        //学籍番号を保持
        String schregno = student.get(classCnt)._schregno;
        //出席番号
        svf.VrsOutn("NO",    row, String.valueOf(toInt(student.get(classCnt)._attendno, 0)));
        //生徒氏名
        String name = student.get(classCnt)._name;
        int nameLen = KNJ_EditEdit.getMS932ByteLength(name);
        String nameField = (nameLen <= 20)? "1" : (nameLen <= 30)? "2" : "3";
        svf.VrsOutn("NAME" + nameField, row, name);

        return schregno;
    }

    //指定の日付、学籍番号、時限(または1日)の出欠区分を取得
    private String getDiMark(ArrayList<ArrayList<String>> diMark, String targetDate, String schregno, String periodcd) {
        String dm = "・";
        for (int diCnt = 0; diCnt < diMark.size(); diCnt++) {
            if (   targetDate.equals(((ArrayList<String>)diMark.get(diCnt)).get(0)) == true
                && 	schregno.equals(((ArrayList<String>)diMark.get(diCnt)).get(1)) == true
                && 	periodcd.equals(((ArrayList<String>)diMark.get(diCnt)).get(2)) == true) {
                dm = ((ArrayList<String>)diMark.get(diCnt)).get(4);
                break;
            }
        }
        return dm;
    }

    private void printGetsunairuikei(final Vrw32alp svf, String schregno, ArrayList<AttendData> attendData, int row, String month) {
        //月内累計
        for(int syussekiCnt = 0; syussekiCnt < attendData.size(); syussekiCnt++) {
            if (   schregno.equals(attendData.get(syussekiCnt)._schregno)
                && month.equals(attendData.get(syussekiCnt)._month)) {
                //休学
                svf.VrsOutn("MONTH_OFFDAYS", row, attendData.get(syussekiCnt)._offdays);
                //忌引
                svf.VrsOutn("MONTH_KIBIKI",  row, attendData.get(syussekiCnt)._mourning);
                //出席停止
                svf.VrsOutn("MONTH_SUSPEND", row, attendData.get(syussekiCnt)._suspend);
                //留学
                svf.VrsOutn("MONTH_ABROAD",  row, attendData.get(syussekiCnt)._abroad);
                //欠席
                svf.VrsOutn("MONTH_ABSENCE", row, attendData.get(syussekiCnt)._sick);
                //遅刻
                svf.VrsOutn("MONTH_LATE",    row, attendData.get(syussekiCnt)._late);
                //早退
                svf.VrsOutn("MONTH_LEAVE",   row, attendData.get(syussekiCnt)._early);
                break;
            }
        }
    }

    private void printNenkanruikei(final Vrw32alp svf, String schregno, ArrayList<AttendData> attendData, int row) {
        //年度内累計
        for(int syussekiCnt = 0; syussekiCnt < attendData.size(); syussekiCnt++) {
            if (schregno.equals(attendData.get(syussekiCnt)._schregno)) {
                //休学
                String offdays = attendData.get(syussekiCnt)._yearOffdays;
                svf.VrsOutn("OFFDAYS", row, offdays);

                //授業日数
                String lesson = attendData.get(syussekiCnt)._yearLesson;
                svf.VrsOutn("LESSON",  row, lesson);
                //忌引
                String kibiki = attendData.get(syussekiCnt)._yearMourning;
                svf.VrsOutn("KIBIKI",  row, kibiki);
                //出席停止
                String suspend = attendData.get(syussekiCnt)._yearSuspend;
                svf.VrsOutn("SUSPEND", row, suspend);
                //留学
                String abroad = attendData.get(syussekiCnt)._yearAbroad;
                svf.VrsOutn("ABROAD",  row, abroad);
                //出席すべき
                int subeki = toInt(lesson, 0) - (toInt(offdays, 0) + toInt(kibiki, 0) + toInt(suspend, 0) + toInt(abroad, 0));
                svf.VrsOutn("SUBEKI",  row, String.valueOf(subeki));
                //欠席
                svf.VrsOutn("ABSENCE", row, attendData.get(syussekiCnt)._yearSick);
                //遅刻
                svf.VrsOutn("LATE",    row, attendData.get(syussekiCnt)._yearLate);
                //早退
                svf.VrsOutn("LEAVE",   row, attendData.get(syussekiCnt)._yearEarly);
                break;
            }
        }
    }

    //日付を和暦 + 月 + 日 + 曜日に変換する
    private String dateFormatConv(final DB2UDB db2, String date) {
        String d[] = date.split("-");
        int year  = toInt(d[0], 0);
        int month = toInt(d[1], 0);
        int day   = toInt(d[2], 0);
        String title = KNJ_EditDate.gengou(db2, year, month, day);
        String w = KNJ_EditDate.h_format_W(date);  //曜日
        title += "(" + w + ")";

        return title;
    }

    //指定日付が妥当な日付かチェック
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

    private static int countWeek(final String nendo, final String yyyy_mm_dd)
    {
        //何周目かをカウントする対象の日付
        final Calendar targetDate = KNJ_EditDate.getCalendar(yyyy_mm_dd);

        //カウントする基準となる日付(=年始=4月1日)
        final Calendar startDate = KNJ_EditDate.getCalendar(String.format("%04d-04-01", toInt(nendo, 0)));

        if (targetDate.compareTo(startDate) == 0) {
            //カウント対象の日付が年始と同じなら第1週確定
            return 1;
        } else if (targetDate.compareTo(startDate) < 0) {
            //カウント対象の日付が年始より前は処理しない(フォールバックとして第1週とする)
            return 1;
        }

        int weekCount = 0;
        if (startDate.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            //4月1日が月曜日でない
            //→月曜日が見つかるまでの期間が第1週とカウントされるため、プラス1
            weekCount++;
        }

        final Calendar temp = startDate;
        while (temp.compareTo(targetDate) <= 0) {
            if (temp.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                //月曜日が見つかった場合は新しい週なので週数をプラス1
                weekCount++;
            }

            //1日足す
            temp.add(Calendar.DATE, 1);
        }

        return weekCount;
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision$ $Date$");
        final Param paramap = new Param(db2, request);
        return paramap;
    }

    private static int toInt(final String s, final int def) {
        if (!NumberUtils.isDigits(s)) {
            return def;
        }
        return Integer.parseInt(s);
    }

    private void setForm(final Vrw32alp svf, final String form, final int data) {
        svf.VrSetForm(form, data);
        log.info(" form = " + form);
        if (_param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }

    private static Date toDate(final String date) {
        if (null != date) {
            try {
                return sdf.parse(date);
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return null;
    }

    private static Calendar toCalendar(final String date) {
        final Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(toDate(date));
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return cal;
    }

    private static String toDateString(final Date date) {
        return sdf.format(date);
    }

    /**生徒基本情報取得**/
    private String sqlGradeClass() {
        final StringBuffer stb = new StringBuffer();
        stb.append("   SELECT SRD.SCHREGNO ");
        stb.append("        , SRD.YEAR ");
        stb.append("        , SRD.GRADE || SRD.HR_CLASS AS GRADE_CLASS ");
        stb.append("        , CASE ");
        stb.append("          WHEN SRD.GRADE = '01' THEN '1' ");
        stb.append("          WHEN SRD.GRADE = '02' THEN '2' ");
        stb.append("          WHEN SRD.GRADE = '03' THEN '3' ");
        stb.append("          ELSE SRD.GRADE ");
        stb.append("          END AS GRADE_NAME ");
        stb.append("        , SRH.HR_CLASS_NAME1 ");
        stb.append("        , SRD.ATTENDNO ");
        stb.append("        , SBM.NAME ");
        stb.append("        , SBM.NAME_KANA ");
        stb.append("        , SM.STAFFNAME ");
        stb.append("     FROM ( ");
        stb.append("             SELECT YEAR ");
        stb.append("                  , GRADE ");
        stb.append("                  , HR_CLASS ");
        stb.append("                  , ATTENDNO ");
        stb.append("                  , SCHREGNO ");
        stb.append("               FROM SCHREG_REGD_DAT ");
        stb.append("              WHERE YEAR = '" + _param._year + "' ");
        stb.append("                AND GRADE || HR_CLASS IN " + SQLUtils.whereIn(true, _param._class));
        stb.append("           GROUP BY YEAR ");
        stb.append("                  , GRADE ");
        stb.append("                  , HR_CLASS ");
        stb.append("                  , ATTENDNO ");
        stb.append("                  , SCHREGNO ");
        stb.append("          ) SRD ");
        stb.append("LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("       ON SRD.SCHREGNO = SBM.SCHREGNO ");
        stb.append("LEFT JOIN ( ");
        stb.append("             SELECT MAIN.YEAR ");
        stb.append("                  , MAIN.GRADE ");
        stb.append("                  , MAIN.HR_CLASS ");
        stb.append("                  , MAIN.STAFFCD ");
        stb.append("               FROM STAFF_CLASS_HIST_DAT MAIN ");
        stb.append("              WHERE MAIN.TR_DIV = '1'  ");
        stb.append("                AND NOT EXISTS ( ");
        stb.append("                                SELECT 'X' ");
        stb.append("                                  FROM STAFF_CLASS_HIST_DAT TMP  ");
        stb.append("                                 WHERE TMP.TR_DIV   = '1' ");
        stb.append("                                   AND TMP.YEAR     = MAIN.YEAR ");
        stb.append("                                   AND TMP.GRADE    = MAIN.GRADE ");
        stb.append("                                   AND TMP.HR_CLASS = MAIN.HR_CLASS ");
        stb.append("                                   AND TMP.SEMESTER > MAIN.SEMESTER ");
        stb.append("                                ) ");
        stb.append("           GROUP BY MAIN.YEAR ");
        stb.append("                  , MAIN.GRADE ");
        stb.append("                  , MAIN.HR_CLASS ");
        stb.append("                  , MAIN.STAFFCD ");
        stb.append("          ) SCHD ");
        stb.append("       ON SCHD.YEAR     = SRD.YEAR ");
        stb.append("      AND SCHD.GRADE    = SRD.GRADE ");
        stb.append("      AND SCHD.HR_CLASS = SRD.HR_CLASS ");
        stb.append("LEFT JOIN STAFF_MST SM ");
        stb.append("       ON SCHD.STAFFCD  = SM.STAFFCD ");
        stb.append("LEFT JOIN ( ");
        stb.append("             SELECT YEAR ");
        stb.append("                  , GRADE ");
        stb.append("                  , HR_CLASS ");
        stb.append("                  , HR_CLASS_NAME1 ");
        stb.append("               FROM SCHREG_REGD_HDAT ");
        stb.append("           GROUP BY YEAR ");
        stb.append("                  , GRADE ");
        stb.append("                  , HR_CLASS ");
        stb.append("                  , HR_CLASS_NAME1 ");
        stb.append("          ) SRH ");
        stb.append("       ON SRH.YEAR     = SRD.YEAR ");
        stb.append("      AND SRH.GRADE    = SRD.GRADE ");
        stb.append("      AND SRH.HR_CLASS = SRD.HR_CLASS ");
        stb.append(" ORDER BY SRD.GRADE ");
        stb.append("        , SRD.HR_CLASS ");
        stb.append("        , SRD.ATTENDNO ");

        return stb.toString();
    }

    /**日毎の時限の科目**/
    private String sqlPeriodChair() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CHAIR AS ( ");
        stb.append("     SELECT CHAIR_DAT.CHAIRCD ");
        stb.append("          , CHAIR_DAT.CLASSCD ");
        stb.append("          , CHAIR_DAT.SCHOOL_KIND ");
        stb.append("          , CHAIR_DAT.CURRICULUM_CD ");
        stb.append("          , CHAIR_DAT.SUBCLASSCD ");
        stb.append("          , CHAIR_DAT.CHAIRABBV");
        stb.append("          , CHAIR_CLS_DAT.TRGTGRADE ");
        stb.append("          , CHAIR_CLS_DAT.TRGTCLASS ");
        stb.append("          , CHAIR_CLS_DAT.GROUPCD ");
        stb.append("          , CHAIR_CLS_DAT.YEAR ");
        stb.append("          , CHAIR_CLS_DAT.SEMESTER ");
        stb.append("       FROM CHAIR_DAT ");
        stb.append("       JOIN CHAIR_CLS_DAT ");
        stb.append("         ON ( ");
        stb.append("                 CHAIR_DAT.YEAR        = CHAIR_CLS_DAT.YEAR ");
        stb.append("             AND CHAIR_DAT.SEMESTER    = CHAIR_CLS_DAT.SEMESTER ");
        stb.append("             AND CHAIR_DAT.GROUPCD     = CHAIR_CLS_DAT.GROUPCD ");
        stb.append("             AND CHAIR_CLS_DAT.CHAIRCD = '0000000' ");
        stb.append("             AND CHAIR_CLS_DAT.GROUPCD <> '" + NONE_GROUP_CODE + "' ");
        stb.append("            ) ");
        stb.append("      UNION ");
        stb.append("     SELECT CHAIR_DAT.CHAIRCD ");
        stb.append("          , CHAIR_DAT.CLASSCD ");
        stb.append("          , CHAIR_DAT.SCHOOL_KIND ");
        stb.append("          , CHAIR_DAT.CURRICULUM_CD ");
        stb.append("          , CHAIR_DAT.SUBCLASSCD ");
        stb.append("          , CHAIR_DAT.CHAIRABBV");
        stb.append("          , CHAIR_CLS_DAT.TRGTGRADE ");
        stb.append("          , CHAIR_CLS_DAT.TRGTCLASS ");
        stb.append("          , CHAIR_CLS_DAT.GROUPCD ");
        stb.append("          , CHAIR_CLS_DAT.YEAR ");
        stb.append("          , CHAIR_CLS_DAT.SEMESTER ");
        stb.append("       FROM CHAIR_DAT ");
        stb.append("       JOIN CHAIR_CLS_DAT ");
        stb.append("         ON ( ");
        stb.append("                 CHAIR_DAT.YEAR     = CHAIR_CLS_DAT.YEAR ");
        stb.append("             AND CHAIR_DAT.SEMESTER = CHAIR_CLS_DAT.SEMESTER ");
        stb.append("             AND CHAIR_DAT.CHAIRCD  = CHAIR_CLS_DAT.CHAIRCD ");
        stb.append("             AND CHAIR_DAT.GROUPCD  = '" + NONE_GROUP_CODE + "' ");
        stb.append("            ) ");
        stb.append(" ) ");
        stb.append("   SELECT SCD.EXECUTEDATE ");
        stb.append("        , CHAIR.TRGTGRADE || CHAIR.TRGTCLASS  AS GRADE_CLASS ");
        stb.append("        , SCD.PERIODCD ");
        stb.append("        , SCD.CHAIRCD ");
        if ("2".equals(_param._disp)) {
            //週報の場合、選択科目は群名称を取得する
            stb.append("        , CASE WHEN ECD.GROUPABBV IS NOT NULL ");
            stb.append("               THEN ECD.GROUPABBV ");
            stb.append("               ELSE SBM.SUBCLASSABBV ");
            stb.append("               END SUBCLASSABBV ");
            stb.append("        , CHAIR.GROUPCD ");
        } else {
            stb.append("        , SBM.SUBCLASSABBV ");
        }
        if ("1".equals(_param._disp)) {
            //日報の場合は生徒毎に科目を取得する
            stb.append("    , CSD.SCHREGNO ");
        }
        stb.append("     FROM SCH_CHR_DAT   SCD ");
        stb.append("LEFT JOIN CHAIR ");
        stb.append("       ON CHAIR.CHAIRCD     = SCD.CHAIRCD  ");
        stb.append("      AND CHAIR.YEAR        = SCD.YEAR ");
        stb.append("      AND CHAIR.SEMESTER    = SCD.SEMESTER ");
        if ("1".equals(_param._disp)) {
            //日報
            stb.append("LEFT JOIN CHAIR_STD_DAT CSD ");
            stb.append("       ON CSD.YEAR      = SCD.YEAR ");
            stb.append("      AND CSD.SEMESTER  = SCD.SEMESTER ");
            stb.append("      AND CSD.CHAIRCD   = CHAIR.CHAIRCD ");
        } else if ("2".equals(_param._disp)) {
            //週報
            stb.append("LEFT JOIN ELECTCLASS_DAT ECD ");
            stb.append("       ON ECD.YEAR      = SCD.YEAR ");
            stb.append("      AND ECD.GROUPCD   = CHAIR.GROUPCD ");
        }
        stb.append("LEFT JOIN SUBCLASS_MST  SBM ");
        stb.append("       ON SBM.CLASSCD       = CHAIR.CLASSCD ");
        stb.append("      AND SBM.SCHOOL_KIND   = CHAIR.SCHOOL_KIND ");
        stb.append("      AND SBM.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
        stb.append("      AND SBM.SUBCLASSCD    = CHAIR.SUBCLASSCD ");
        stb.append("LEFT JOIN NAME_MST      NM ");
        stb.append("       ON NM.NAMECD1        = 'B001' ");
        stb.append("      AND NM.NAMECD2        = SCD.PERIODCD ");
        stb.append("    WHERE CHAIR.TRGTGRADE || CHAIR.TRGTCLASS IN " + SQLUtils.whereIn(true, _param._class));
        if ("3".equals(_param._disp)) {
            //月報
            stb.append(splDateBetween("SCD.EXECUTEDATE", _param._year, _param._startMonReport, _param._endMonReport));
        }
        else {
            //日報・週報
            stb.append("      AND SCD.EXECUTEDATE BETWEEN '" + _param._startDwReport + "' AND '" + _param._endDwReport + "'");
        }
        if ("1".equals(_param._disp)) {
            //日報
            stb.append("      AND SCD.EXECUTEDATE BETWEEN CSD.APPDATE AND CSD.APPENDDATE ");
        }
        stb.append("      AND NM.NAMESPARE2 IS NULL ");
        stb.append(" ORDER BY SCD.EXECUTEDATE ");
        stb.append("        , CHAIR.TRGTGRADE ");
        stb.append("        , CHAIR.TRGTCLASS ");
        stb.append("        , SCD.PERIODCD ");
        stb.append("        , CHAIR.GROUPCD ");

        return stb.toString();
    }

    /**日毎、生徒毎、時限と一日の出欠区分**/
    private String sqlDiMark() {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT ADAT.YEAR ");
        stb.append("         , ADAT.ATTENDDATE ");
        stb.append("         , ADAT.SCHREGNO ");
        stb.append("         , ADAT.PERIODCD ");
        stb.append("         , ADAT.CHAIRCD ");
        stb.append("         , ADAT.DI_CD ");
        stb.append("         , ADCD.DI_MARK ");
        stb.append("      FROM ATTEND_DI_CD_DAT ADCD ");
        stb.append(" LEFT JOIN ATTEND_DAT       ADAT ");
        stb.append("        ON ADAT.YEAR  = ADCD.YEAR ");
        stb.append("       AND ADAT.DI_CD = ADCD.DI_CD ");
        stb.append("     WHERE ADAT.YEAR  = '" + _param._year + "' ");
        if ("3".equals(_param._disp)) {
            //月報
            stb.append(splDateBetween("ADAT.ATTENDDATE", _param._year, _param._startMonReport, _param._endMonReport));
        }
        else {
            //日報・週報
            stb.append("    AND ADAT.ATTENDDATE BETWEEN '" + _param._startDwReport + "' AND '" + _param._endDwReport + "'");
        }
        stb.append("UNION ");
        stb.append("    SELECT ADAY.YEAR ");
        stb.append("         , ADAY.ATTENDDATE ");
        stb.append("         , ADAY.SCHREGNO ");
        stb.append("         , '1日' AS PERIODCD ");
        stb.append("         , NULL AS CHAIRCD ");
        stb.append("         , ADAY.DI_CD ");
        stb.append("         , ADCD.DI_MARK ");
        stb.append("      FROM ATTEND_DI_CD_DAT ADCD ");
        stb.append(" LEFT JOIN ATTEND_DAY_DAT   ADAY ");
        stb.append("        ON ADAY.YEAR  = ADCD.YEAR ");
        stb.append("       AND ADAY.DI_CD = ADCD.DI_CD ");
        stb.append("     WHERE ADAY.YEAR  = '" + _param._year + "' ");
        if ("3".equals(_param._disp)) {
            //月報
            stb.append(splDateBetween("ADAY.ATTENDDATE", _param._year, _param._startMonReport, _param._endMonReport));
        }
        else {
            //日報・週報
            stb.append("    AND ADAY.ATTENDDATE BETWEEN '" + _param._startDwReport + "' AND '" + _param._endDwReport + "'");
        }

        return stb.toString();
    }

    /**生徒毎、月毎と年間の出席日数等**/
    private String sqlSyusseki() {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH BASE AS ( ");
        stb.append("    SELECT ASD.YEAR ");
        stb.append("         , ASD.MONTH ");
        stb.append("         , ASD.SEMESTER ");
        stb.append("         , ASD.SCHREGNO ");
        stb.append("         , NVL(ASD.OFFDAYS,  0) AS OFFDAYS ");   //休学
        stb.append("         , NVL(ASD.LESSON,   0) AS LESSON ");    //授業日数
        stb.append("         , NVL(ASD.MOURNING, 0) AS MOURNING ");  //忌引
        stb.append("         , NVL(ASD.SUSPEND,  0) AS SUSPEND ");   //出席停止
        stb.append("         , NVL(ASD.ABROAD,   0) AS ABROAD ");    //留学
        stb.append("         , (NVL(ASD.SICK, 0)+NVL(ASD.NOTICE, 0)+NVL(ASD.NONOTICE, 0)) AS SICK ");
        stb.append("         , NVL(ASD.LATE,     0) AS LATE ");      //遅刻
        stb.append("         , NVL(ASD.EARLY,    0) AS EARLY ");     //早退
        stb.append("      FROM ( ");
        stb.append("               SELECT YEAR ");
        stb.append("                     , SCHREGNO ");
        stb.append("                 FROM SCHREG_REGD_DAT ");
        stb.append("                WHERE YEAR = '" + _param._year + "' ");
        stb.append("                  AND GRADE || HR_CLASS IN " + SQLUtils.whereIn(true, _param._class));
        stb.append("             GROUP BY YEAR ");
        stb.append("                    , SCHREGNO ");
        stb.append("           ) SRD ");
        stb.append(" LEFT JOIN ATTEND_SEMES_DAT ASD ");
        stb.append("        ON ASD.YEAR     = SRD.YEAR ");
        stb.append("       AND ASD.SCHREGNO = SRD.SCHREGNO ");
        stb.append("     WHERE ASD.YEAR = '" + _param._year + "' ");
        stb.append("ORDER BY ASD.SEMESTER ");
        stb.append("), MONTH_DATA AS ( ");
        stb.append("SELECT * ");
        stb.append("  FROM BASE ");
        stb.append("     WHERE BASE.YEAR = '" + _param._year + "' ");
        stb.append("), YEAR_DATA AS ( ");
        stb.append("  SELECT YEAR ");
        stb.append("       , SCHREGNO ");
        stb.append("       , SUM(OFFDAYS)  AS YEAR_OFFDAYS ");
        stb.append("       , SUM(LESSON)   AS YEAR_LESSON ");
        stb.append("       , SUM(MOURNING) AS YEAR_MOURNING ");
        stb.append("       , SUM(SUSPEND)  AS YEAR_SUSPEND ");
        stb.append("       , SUM(ABROAD)   AS YEAR_ABROAD ");
        stb.append("       , SUM(SICK)     AS YEAR_SICK ");
        stb.append("       , SUM(LATE)     AS YEAR_LATE ");
        stb.append("       , SUM(EARLY)    AS YEAR_EARLY ");
        stb.append("    FROM BASE ");
        stb.append("GROUP BY YEAR ");
        stb.append("       , SCHREGNO ");
        stb.append(") ");
        stb.append("   SELECT MD.YEAR ");
        stb.append("        , MD.MONTH ");
        stb.append("        , MD.SCHREGNO ");
        stb.append("        , MD.OFFDAYS ");
        stb.append("        , MD.LESSON ");
        stb.append("        , MD.MOURNING ");
        stb.append("        , MD.SUSPEND ");
        stb.append("        , MD.ABROAD ");
        stb.append("        , MD.SICK ");
        stb.append("        , MD.LATE ");
        stb.append("        , MD.EARLY ");
        stb.append("        , YD.YEAR_OFFDAYS ");
        stb.append("        , YD.YEAR_LESSON ");
        stb.append("        , YD.YEAR_MOURNING ");
        stb.append("        , YD.YEAR_SUSPEND ");
        stb.append("        , YD.YEAR_ABROAD ");
        stb.append("        , YD.YEAR_SICK ");
        stb.append("        , YD.YEAR_LATE ");
        stb.append("        , YD.YEAR_EARLY ");
        stb.append("     FROM MONTH_DATA MD ");
        stb.append("LEFT JOIN YEAR_DATA  YD ");
        stb.append("       ON YD.YEAR     = MD.YEAR ");
        stb.append("      AND YD.SCHREGNO = MD.SCHREGNO ");
        stb.append(" ORDER BY MD.YEAR ");
        stb.append("        , MD.MONTH ");
        stb.append("        , MD.SCHREGNO ");

        return stb.toString();
    }

    /**SQLで日付の絞り込みを行うBETWEEN句を作成**/
    private String splDateBetween(final String targetDate, final String nendoYear, final String fromMonth, final String toMonth) {
        //月の大小関係を統一
        int startMon =  toInt(fromMonth, 0);
        int endMon   =  toInt(toMonth, 0);

        //開始年月日、終了年月日の(「YYYY/MM/DD」形式)文字列を作成
        final int currentYear = toInt(nendoYear, 0);
        final int nextYear    = toInt(nendoYear, 0) + 1;
        final String startYYYYMMDD =  String.format("%04d/%02d/01", (startMon < 4) ? nextYear : currentYear, startMon);
        final String endYYYYMMDD   =  String.format("%04d/%02d/01", (endMon   < 4) ? nextYear : currentYear, endMon);

        //WHERE句条件追加
        String sql = "       AND " + targetDate + " BETWEEN TO_DATE('" + startYYYYMMDD + "', 'YYYY/MM/DD') AND (TO_DATE('" + endYYYYMMDD + "', 'YYYY/MM/DD') + 1 months - 1 days)";
        return sql;
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

        //開始月、終了月の(「MM」形式)文字列を作成
        final String startMM =  String.format("%02d", startMon);
        final String endMM   =  String.format("%02d", endMon);

        //WHERE句条件追加
        String sql = "";
        if (changFlg == true) {
            sql  = "       AND (   " + targetDate + " BETWEEN '01' AND '" + startMM + "'";
            sql += "            OR " + targetDate + " BETWEEN '" + endMM + "' AND '12')";
        }
        else {
            sql = "       AND " + targetDate + " BETWEEN '" + startMM + "' AND '" + endMM + "'";
        }

        return sql;
    }

    /**学期取得**/
    private static int getSemesterValue2(final String dateStr) {
        int rtn = 0;
        if (null == dateStr) {
            return rtn;
        }
        final Date date = java.sql.Date.valueOf(dateStr);
        for (final Map.Entry<String, Term> e : _param._semesterDates.entrySet()) {
            final String semester = e.getKey();
            if ("9".equals(semester)) {
                continue;
            }
            final Term semeTerm = _param._semesterDates.get(semester);
            if (null == semeTerm) {
                log.warn(" null semester : " + semester);
            }
            final Date sdate = toDate(semeTerm._sdate);
            final Date edate = toDate(semeTerm._edate);
            if (null == sdate || null == edate) {
                log.warn(" null semester range: " + semeTerm);
            }
            if (sdate.compareTo(date) <= 0 && date.compareTo(edate) <= 0) {
                rtn = Integer.parseInt(semester);
            }
        }
        return rtn;
    }

    private static class Term {
        final String _key;
        final String _sdate;
        final String _edate;
        final String _dateFromToString;
        Term(final String key, final String sdate, final String edate, final String dateFromToString) {
            _key = key;
            _sdate = sdate;
            _edate = edate;
            _dateFromToString = dateFromToString;
        }

        public static String dateFromToString(final DB2UDB db2, final String sdate, final String edate) {
            String dateFromTo = null;
            try {
                dateFromTo = KNJ_EditDate.h_format_JP(db2, sdate) + " " + FROM_TO_MARK + " " + KNJ_EditDate.h_format_JP(db2, edate);
            } catch (Exception ex) {
                log.warn("now date-get error!", ex);
            }
            return dateFromTo;
        }

        String dateRangeKey() {
            return _sdate + ":" + _edate;
        }

        private static int getCweek(final Calendar cal) {
            int cweek = cal.get(Calendar.DAY_OF_WEEK); // calの曜日を取得
            if (cweek == 1) {
                cweek = 8; // 日曜日は８と設定
            }
            return cweek;
        }

        /**
        *
        *  印刷範囲期間の再設定->指定日を含む月〜日まで->指定日を含む学期の範囲内
        *
        */
        private static Term getTermValue(final DB2UDB db2) {

            // sdayを含む学期の開始日
            final String sqlSsemeSdate = " SELECT SDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + _param._year + "' AND GRADE = '" + _param._grade + "' AND '" + _param._startDwReport + "' BETWEEN SDATE AND EDATE AND SEMESTER <> '9' ";
            Date ssemeSdate = toDate(KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlSsemeSdate)));

            // edayを含む学期の終了日
            final String sqlEsemeEdate = " SELECT EDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + _param._year + "' AND GRADE = '" + _param._grade + "' AND '" + _param._endDwReport + "' BETWEEN SDATE AND EDATE AND SEMESTER <> '9'";
            Date esemeEdate = toDate(KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlEsemeEdate)));

            String startD = null;
            try {
                final Calendar cal = Calendar.getInstance();
                try {
                    if (ssemeSdate == null) {
                        ssemeSdate = toDate(_param._startDwReport); // 範囲開始日
                    }
                    cal.setTime(toDate(_param._startDwReport)); // 受け取った開始日付をCalendar calに変換
                } catch (Exception ex) {
                    log.error("exception!", ex);
                }
                int cweek1 = getCweek(cal); // calの曜日を取得
                if (Calendar.MONDAY < cweek1) {
                    cal.add(Calendar.DATE, - (cweek1 - Calendar.MONDAY)); // calが月曜日を越えたら去る月曜日をセット
                }
                startD = toDateString(max(cal.getTime(), ssemeSdate)); // dateとssemeSdateを比較し、dateが学期開始日より前なら学期開始日を範囲開始日とする
            } catch (Exception ex) {
                log.error("ReturnVal getTermValue 2 error!", ex);
            }

            String endD = null;
            try {
                Calendar cal = Calendar.getInstance();
                try {
                    if (esemeEdate == null) {
                        esemeEdate = toDate(_param._endDwReport); // 範囲終了日
                    }
                    cal = toCalendar(_param._endDwReport); // 受け取った終了日付をCalendar calに変換
                } catch (Exception ex) {
                    log.error("exception!", ex);
                }
                final int cweek2 = getCweek(cal); // calの曜日を取得
                if (cweek2 < 8) {
                    cal.add(Calendar.DATE, (8 - cweek2)); // calが日曜日でなければ来る日曜日をセット
                }
                endD = toDateString(min(cal.getTime(), esemeEdate)); // dateとesemeEdateを比較し、dateが学期終了日より後なら学期終了日を範囲終了日とする
            } catch (Exception ex) {
                log.error("ReturnVal getTermValue 2 error!", ex);
            }

            final Term term = new Term("TERM", startD, endD, Term.dateFromToString(db2, startD, endD));
            return term;
        }

        private static Date min(final Date date1, final Date date2) {
            return date1.before(date2) ? date1 : date2;
        }

        private static Date max(final Date date1, final Date date2) {
            return date1.after(date2) ? date1 : date2;
        }
    }

    private static class Week {
        final Term _term;
        List<Integer> _periodStartList;
        List<String> _dateList;
        Map<String, Integer> _dateDayofweekMap;
        int _weekint;
        Map<Integer, String> _subjectNameMap;
        Week(final Term weekTerm) {
            _term = weekTerm;
        }
    }

    private List<Week> getWeekList(final DB2UDB db2, final Term term) {
        final Calendar cals = toCalendar(term._sdate);      //開始日付(週)
        final Calendar cale = toCalendar(term._edate);      //終了日付(週)

        final List<Week> weekList = new ArrayList<Week>();
        for (;;) {
            final Week week = setWeekList(db2, cals, cale);    //一週間の日付をセット
            if (week._weekint == 9) {
                break;                //戻り値が9なら終了
            }
            if (week._weekint == 1) {
                continue;             //戻り値が1なら出力無し
            }

            weekList.add(week);
        }
        return weekList;
    }

    /**
     *  一週間の日付の取得
     *
     */
    private Week setWeekList(final DB2UDB db2, final Calendar cals, final Calendar cale) {
        String scheSdate;
        String scheEdate;
        final List dateList = new ArrayList();
        final List<Integer> printPeriodStartList = new ArrayList<Integer>();
        final Map<String, Integer> dateDayofweekMap = new HashMap();
        scheSdate = toDateString(cals.getTime());         //ページ週の月曜日をセット
        scheEdate = null;
        try {
            if (cals.after(cale)) {
                final Week week = new Week(null);
                week._weekint = 9;
                return week;                 //日付が印刷範囲終了日を越えたら終了！
            }
            for (int j = 0; j < 8; cals.add(Calendar.DATE, 1), j++) { //月〜日まで
                if (cals.after(cale)) {
                    break;                //日付が印刷範囲終了日を越えたら終了！
                }
                final String datestr = toDateString(cals.getTime());
                //getSemesterValueでcalsの属する学期を取得し学期内なら日付を出力する->夏休み等は除外
                final int semesterValue2 = getSemesterValue2(datestr);
                if (semesterValue2 > 0) {
                    int cweek = cals.get(Calendar.DAY_OF_WEEK);
                    if (cweek == Calendar.SUNDAY) {
                        cweek = 8;
                    }
                    dateList.add(datestr);
                    scheEdate = datestr; //ページ週の最終日をセット
                    printPeriodStartList.add(new Integer(cweek - 2));
                    dateDayofweekMap.put(datestr, new Integer(cweek - 1));
                }
                if (cals.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    break;
                }
            }
            //次の出力日付をセット
            if (!cals.after(cale)) {
                final int cweek = cals.get(Calendar.DAY_OF_WEEK);
                if (cweek == Calendar.SUNDAY) {
                    cals.add(Calendar.DATE,1);                //日曜日は翌月曜日をセット
                } else if (cweek > Calendar.MONDAY) {
                    cals.add(Calendar.DATE, 9 - cweek);        //月曜日を越えたら来る月曜日をセット
                }
            }
        } catch (Exception ex) {
            log.warn("svf-out error!",ex);
            final Week week = new Week(null);
            week._weekint = 9;
            return week;
        }
        if (scheEdate == null) {
            final Week week = new Week(null);
            week._weekint = 1;
            return week;                      //出力無し
        }
        final Week week = new Week(new Term("WEEK_TERM", scheSdate, scheEdate, Term.dateFromToString(db2, scheSdate, scheEdate)));
        week._dateList = dateList;
        week._periodStartList = printPeriodStartList;
        week._dateDayofweekMap = dateDayofweekMap;
        week._weekint = 0;
        return week; //継続
    }

    private static class Student {
        final String _schregno;
        final String _gradeClass;
        final String _gradeName;
        final String _hrClassName;
        final String _attendno;
        final String _name;
        final String _nameKana;
        final String _staffName;

        public Student(
                final String schregno
              , final String gradeClass
              , final String gradeName
              , final String hrClassName
              , final String attendno
              , final String name
              , final String nameKana
              , final String staffName
                ) {
            _schregno    = schregno;
            _gradeClass  = gradeClass;
            _gradeName   = gradeName;
            _hrClassName = hrClassName;
            _attendno    = attendno;
            _name        = name;
            _nameKana    = nameKana;
            _staffName   = staffName;
        }
    }

    private class AttendData {
        final String _month;
        final String _schregno;
        final String _offdays;
        final String _lesson;
        final String _mourning;
        final String _suspend;
        final String _abroad;
        final String _sick;
        final String _late;
        final String _early;
        final String _yearOffdays;
        final String _yearLesson;
        final String _yearMourning;
        final String _yearSuspend;
        final String _yearAbroad;
        final String _yearSick;
        final String _yearLate;
        final String _yearEarly;

        public AttendData(
                final String month,
                final String schregno,
                final String offdays,
                final String lesson,
                final String mourning,
                final String suspend,
                final String abroad,
                final String sick,
                final String late,
                final String early,
                final String yearOffdays,
                final String yearLesson,
                final String yearMourning,
                final String yearSuspend,
                final String yearAbroad,
                final String yearSick,
                final String yearLate,
                final String yearEarly
        ) {
            _month = month;
            _schregno = schregno;
            _offdays = offdays;
            _lesson = lesson;
            _mourning = mourning;
            _suspend = suspend;
            _abroad = abroad;
            _sick = sick;
            _late = late;
            _early = early;
            _yearOffdays = yearOffdays;
            _yearLesson = yearLesson;
            _yearMourning = yearMourning;
            _yearSuspend = yearSuspend;
            _yearAbroad = yearAbroad;
            _yearSick = yearSick;
            _yearLate = yearLate;
            _yearEarly = yearEarly;
        }
    }

    private class Param {
        private final String _year;
        private final String _semester;
        private final String _disp;
        private final String _grade;
        private final String[] _class;
        private final String _startDwReport;
        private final String _endDwReport;
        private final String _startMonReport;
        private final String _endMonReport;

        private Map<String, Term> _semesterDates;

        final boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year              = request.getParameter("CTRL_YEAR");                //年度
            _semester          = request.getParameter("CTRL_SEMESTER");            //学期
            _disp              = request.getParameter("DISP");                     //出席簿種類(1:日報、2:週報、3:月報)
            _grade              = request.getParameter("GRADE_NAME");              //学年
            _class             = request.getParameterValues("CATEGORY_SELECTED");  //選択クラス
            _startDwReport     = KNJ_EditDate.H_Format_Haifun(request.getParameter("START_DW_REPORT"));        //開始日(日報・週報)
            _endDwReport       = KNJ_EditDate.H_Format_Haifun(request.getParameter("END_DW_REPORT"));          //終了日(日報・週報)
            _startMonReport    = request.getParameter("START_MON_REPORT");         //開始月(月報)
            _endMonReport      = request.getParameter("END_MON_REPORT");           //終了月(月報)

            _semesterDates = setSemesterDates(db2);

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2,
                    " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJC100A' AND NAME = '" + propName + "' "));
        }

        private Map setSemesterDates(final DB2UDB db2) {
            final Map rtn = new TreeMap();
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT SEMESTER, SDATE, EDATE FROM V_SEMESTER_GRADE_MST ");
            stb.append(" WHERE YEAR = '" + _year + "' ");
            stb.append("   AND GRADE = '" + _grade + "' ");

            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                final String semester = KnjDbUtils.getString(row, "SEMESTER");
                if (null == semester) {
                    continue;
                }
                final String sdate = KnjDbUtils.getString(row, "SDATE");
                final String edate = KnjDbUtils.getString(row, "EDATE");
                rtn.put(semester, new Term("SEMESTER_" + semester + "_TERM", sdate, edate, Term.dateFromToString(db2, sdate, edate)));
            }
            return rtn;
        }
    }
}
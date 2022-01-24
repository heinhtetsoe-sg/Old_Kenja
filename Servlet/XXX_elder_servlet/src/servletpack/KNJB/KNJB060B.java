// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2021/04/28
 * 作成者: Nutec
 *
 */
package servletpack.KNJB;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [講座・時間割管理]
 *
 *                  ＜ＫＮＪＢ０６０Ｂ＞  変更時間割表
 */
public class KNJB060B {
    private static final Log log = LogFactory.getLog(KNJB060B.class);
    private static final String NONE_GROUP_CODE = "0000";

    private boolean nonedata = false; //該当データなしフラグ
    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
    private Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        PrintWriter outstrm = null;
        try {
            //print設定
            response.setContentType("application/pdf");
            outstrm = new PrintWriter(response.getOutputStream());

            //svf設定
            svf.VrInit();//クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());//PDFファイル名の設定

            sd.setSvfInit(request, response, svf);
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }
            _param = getParam(db2, request);

            printSvfMain(db2, svf);
        } catch (Exception e) {
            log.error("svf_out exception!", e);
        } finally {
            //該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            //終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();    //DBを閉じる
            outstrm.close();//ストリームを閉じる
        }
    }//doGetの括り

    private void setForm(final Vrw32alp svf, final Param param, final String form, final int data) {
        svf.VrSetForm(form, data);
        if (param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }

    private static int toInt(final String s, final int def) {
        if (!NumberUtils.isDigits(s)) {
            return def;
        }
        return Integer.parseInt(s);
    }

    /** 帳票出力 **/
    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String sql;

            //日付ごとにデータを保持
            LinkedHashMap<String, PrintData> datePrintData = new LinkedHashMap<String, PrintData>();

            //時間割1
            sql = sqlTimetable();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ArrayList<TimeTable> timeTableList = new ArrayList<TimeTable>();
            while (rs.next()) {
                boolean dupCheck = false;
                for (int i = 0; i < timeTableList.size(); i++) {
                    TimeTable temp = timeTableList.get(i);
                    int choiceCount = temp._choiceCount;
                    if (temp._executeDate.equals(rs.getString("EXECUTEDATE")) &&
                        temp._grade.equals(rs.getString("GRADE")) &&
                        temp._hrClass.equals(rs.getString("HR_CLASS")) &&
                        temp._periodCd == toInt(rs.getString("PERIODCD"), 0) ) {

                        if (choiceCount < 3 && rs.getString("SUBCLASSABBV") != null) {
                            //3科目までは連結
                            temp._subclassAbbv += " " + rs.getString("SUBCLASSABBV");
                        }
                        temp._choiceCount++;
                        timeTableList.set(i, temp);
                        dupCheck = true;
                        break;
                    }
                }
                if (dupCheck == false) {
                    timeTableList.add(new TimeTable(
                            rs.getString("EXECUTEDATE")
                          , rs.getString("UPDATED")
                          , toInt(rs.getString("PERIODCD"), 0)
                          , rs.getString("CHAIRCD")
                          , rs.getString("GRADE")
                          , rs.getString("HR_CLASS")
                          , rs.getString("HR_NAMEABBV")
                          , rs.getString("GROUPCD")
                          , rs.getString("SUBCLASSABBV")
                          , 1
                    ));
                }
                nonedata = true;
            }

            //変更前情報
            sql = sqlBeforeTimetable();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ArrayList<BeforeTimeTable> beforeList = new ArrayList<BeforeTimeTable>();
            while (rs.next()) {
                boolean dupCheck = false;
                for (int i = 0; i < beforeList.size(); i++) {
                    BeforeTimeTable temp = beforeList.get(i);
                    int choiceCount = temp._choiceCount;
                    if (temp._fromDate.equals(rs.getString("FROM_DATE")) &&
                        temp._grade.equals(rs.getString("GRADE")) &&
                        temp._hrClass.equals(rs.getString("HR_CLASS")) &&
                        temp._fromPeriodCd == toInt(rs.getString("FROM_PERIODCD"), 0) ) {

                        if (choiceCount < 3 && rs.getString("SUBCLASSABBV") != null) {
                            //3科目までは連結
                            temp._subclassAbbv += " " + rs.getString("SUBCLASSABBV");
                        }
                        temp._choiceCount++;
                        beforeList.set(i, temp);
                        dupCheck = true;
                        break;
                    }
                }
                if (dupCheck == false) {
                    beforeList.add(new BeforeTimeTable(
                            rs.getString("GRADE")
                          , rs.getString("HR_CLASS")
                          , rs.getString("HR_NAMEABBV")
                          , rs.getString("GROUPCD")
                          , rs.getString("FROM_DATE")
                          , toInt(rs.getString("FROM_PERIODCD"), 0)
                          , rs.getString("FROM_CHAIRCD")
                          , rs.getString("SUBCLASSABBV")
                          , 1
                    ));
                }
            }

            //変更後情報
            sql = sqlAfterTimetable();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ArrayList<AfterTimeTable> subAfterList = new ArrayList<AfterTimeTable>();
            while (rs.next()) {
                boolean dupCheck = false;
                for (int i = 0; i < subAfterList.size(); i++) {
                    AfterTimeTable temp = subAfterList.get(i);
                    int choiceCount = temp._choiceCount;
                    if (temp._toDate.equals(rs.getString("TO_DATE")) &&
                        temp._grade.equals(rs.getString("GRADE")) &&
                        temp._hrClass.equals(rs.getString("HR_CLASS")) &&
                        temp._toPeriodCd == toInt(rs.getString("TO_PERIODCD"), 0) ) {

                        if (choiceCount < 3 && rs.getString("TOSUBCLASSNAME") != null) {
                            //3科目までは連結
                            temp._toSubclassName += " " + rs.getString("TOSUBCLASSNAME");
                        }

                        temp._choiceCount++;
                        subAfterList.set(i, temp);
                        dupCheck = true;
                        break;
                    }
                }
                if (dupCheck == false) {
                    subAfterList.add(new AfterTimeTable(
                            rs.getString("GRADE")
                          , rs.getString("HR_CLASS")
                          , rs.getString("HR_NAMEABBV")
                          , rs.getString("GROUPCD")
                          , rs.getString("GRADE")
                          , rs.getString("FROM_DATE")
                          , rs.getString("TO_DATE")
                          , toInt(rs.getString("TO_PERIODCD"), 0)
                          , rs.getString("TO_CHAIRCD")
                          , rs.getString("TOSUBCLASSNAME")
                          , 1
                    ));
                }
            }

            if(nonedata) {
                //時間割の表示
                printBasic(svf, timeTableList, beforeList, subAfterList, datePrintData);
            }

            for (String date : datePrintData.keySet()) {
                PrintData pd = datePrintData.get(date);
                //1ページ目印字
                pd.printPage1(svf, _param);
                svf.VrEndPage();
                //2ページ目印字
                pd.printPage2(svf, _param);
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("printSvfMain set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getWeek(String date) {
        String ret = "";
        try {
            final String[] executeDate = date.split("-", 0);
            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(executeDate[0]), Integer.parseInt(executeDate[1]) - 1, Integer.parseInt(executeDate[2]));

            switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                ret = Integer.parseInt(executeDate[1]) + "月" + Integer.parseInt(executeDate[2]) + "日 (日)";
                break;
            case Calendar.MONDAY:
                ret = Integer.parseInt(executeDate[1]) + "月" + Integer.parseInt(executeDate[2]) + "日 (月)";
                break;
            case Calendar.TUESDAY:
                ret = Integer.parseInt(executeDate[1]) + "月" + Integer.parseInt(executeDate[2]) + "日 (火)";
                break;
            case Calendar.WEDNESDAY:
                ret = Integer.parseInt(executeDate[1]) + "月" + Integer.parseInt(executeDate[2]) + "日 (水)";
                break;
            case Calendar.THURSDAY:
                ret = Integer.parseInt(executeDate[1]) + "月" + Integer.parseInt(executeDate[2]) + "日 (木)";
                break;
            case Calendar.FRIDAY:
                ret = Integer.parseInt(executeDate[1]) + "月" + Integer.parseInt(executeDate[2]) + "日 (金)";
                break;
            case Calendar.SATURDAY:
                ret = Integer.parseInt(executeDate[1]) + "月" + Integer.parseInt(executeDate[2]) + "日 (土)";
                break;
            }

        } catch (Exception ex) {
            log.error("getWeek set error!", ex);
        }
        return ret;
    }

    private void printBasic(Vrw32alp svf, ArrayList<TimeTable> basicList, ArrayList<BeforeTimeTable> beforeList, ArrayList<AfterTimeTable> afterList, Map<String, PrintData> datePrintData) {
        String ClassName = "";
        String date = "";
        String upDate = "";
        PrintData pd = new PrintData();
        LecturesByClass lbc;
        try {
            for (TimeTable basic : basicList) {
                lbc = new LecturesByClass();
                lbc._hrClass = basic._hrNameAbbv;
                lbc._hrClassName = (basic._hrNameAbbv).replace('-', '年') + "組";
                int pdGrade = toInt(basic._grade, 0) - 1;

                //一番初めの処理
                if ("".equals(ClassName)) {
                    if (basic._subclassAbbv != null) {
                        lbc._lectures[basic._periodCd - 1]._beforeSubclassName = basic._subclassAbbv;
                        if (pd.grade[pdGrade]._lecturesByGrade.containsKey(lbc._hrClassName) == false) {
                            pd.grade[pdGrade]._lecturesByGrade.put(lbc._hrClassName, lbc);
                        } else {
                            pd.grade[pdGrade]._lecturesByGrade.get(lbc._hrClassName)._lectures[basic._periodCd - 1]._beforeSubclassName = basic._subclassAbbv;
                        }
                    }
                    ClassName = basic._hrClass;
                    lbc = pd.grade[pdGrade]._lecturesByGrade.get(lbc._hrClassName);
                    if (lbc != null) {
                        //変更前科目に取り消し線
                        printBefore(basic, beforeList, lbc);
                        //変更後科目を印字
                        printAfter(basic, afterList, lbc);
                    }
                } else {
                    //日付が同じとき
                    if (date.equals(basic._executeDate)) {
                        //クラスが同じとき
                        if (ClassName.equals(basic._hrClass)) {
                            lbc._lectures[basic._periodCd - 1]._beforeSubclassName = basic._subclassAbbv;
                            if (pd.grade[pdGrade]._lecturesByGrade.containsKey(lbc._hrClassName) == false) {
                                pd.grade[pdGrade]._lecturesByGrade.put(lbc._hrClassName, lbc);
                            } else if (basic._subclassAbbv != null) {
                                pd.grade[pdGrade]._lecturesByGrade.get(lbc._hrClassName)._lectures[basic._periodCd - 1]._beforeSubclassName = basic._subclassAbbv;
                            }
                        } else {
                            lbc._lectures[basic._periodCd - 1]._beforeSubclassName = basic._subclassAbbv;
                            if (pd.grade[pdGrade]._lecturesByGrade.containsKey(lbc._hrClassName) == false) {
                                pd.grade[pdGrade]._lecturesByGrade.put(lbc._hrClassName, lbc);
                            } else if (basic._subclassAbbv != null) {
                                pd.grade[pdGrade]._lecturesByGrade.get(lbc._hrClassName)._lectures[basic._periodCd - 1]._beforeSubclassName = basic._subclassAbbv;
                            }
                        }
                        lbc = pd.grade[pdGrade]._lecturesByGrade.get(lbc._hrClassName);
                        if (lbc != null) {
                            //変更前科目に取り消し線
                            printBefore(basic, beforeList, lbc);
                            //変更後科目を印字
                            printAfter(basic, afterList, lbc);
                        }
                    }
                }
                ClassName = basic._hrClass;

                //日付が変わったとき
                if (date.equals(basic._executeDate) == false && "".equals(date) == false) {
                    if (upDate != null && upDate.length() == 26) {
                        upDate = upDate.substring(5);
                        upDate = upDate.substring(0,11);
                    } else {
                        upDate = "";
                    }
                    pd._printDate = getWeek(date);
                    pd._updateDate = "最終更新：" + upDate.replace('-', '/');
                    if (datePrintData.containsKey(date) == false) {
                        datePrintData.put(date, pd);
                    }
                    pd = new PrintData();
                }

                date = basic._executeDate;
                upDate = basic._updated;
            }

            if (upDate != null && upDate.length() == 26) {
                upDate = upDate.substring(5);
                upDate = upDate.substring(0,11);
            } else {
                upDate = "";
            }
            pd._printDate = getWeek(date);
            pd._updateDate = "最終更新：" + upDate.replace('-', '/');
            if (datePrintData.containsKey(date) == false) {
                datePrintData.put(date, pd);
            }

        } catch (Exception ex) {
            log.error("printBasic set error!", ex);
        }
    }

    private void printAfter(TimeTable basic, ArrayList<AfterTimeTable> afterList, LecturesByClass lbc) {
        try {
            for (AfterTimeTable after : afterList) {
                if (   (basic._executeDate).equals(after._toDate)
                    && (basic._grade).equals(after._grade)
                    && (basic._hrClass).equals(after._hrClass)) {
                    //変更後のデータを保持
                    if (   (after._fromDate != null)
                        && (after._toDate != null)
                        && (after._fromDate).equals(after._toDate) == false) {
                        lbc._lectures[after._toPeriodCd - 1]._beforeAmikakeFlg = true;
                    }
                    if ((basic._chairCd).equals(after._toChairCd) == false) {
                        lbc._lectures[after._toPeriodCd - 1]._afterSubclassName = after._toSubclassName;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("printAfter set error!", ex);
        }
    }

    private void printBefore(TimeTable basic, ArrayList<BeforeTimeTable> beforeList, LecturesByClass lbc) {
        try {
            //変更前科目に取り消し線印字
            for (BeforeTimeTable before : beforeList) {
                if (   (before._fromDate != null)
                    && (basic._executeDate).equals(before._fromDate)
                    && (basic._grade).equals(before._grade)
                    && (basic._hrClass).equals(before._hrClass)) {
                    lbc._lectures[before._fromPeriodCd - 1]._beforeTorikesiFlg = true;
                    lbc._lectures[before._fromPeriodCd - 1]._beforeSubclassName = before._subclassAbbv;
                }
            }
        } catch (Exception ex) {
            log.error("printBefore set error!", ex);

        }
    }

    /** 時間割情報 **/
    private String sqlTimetable() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH PERIOD AS ( ");
        stb.append("   SELECT NAMECD2 AS PERIODCD ");
        stb.append("     FROM V_NAME_MST ");
        stb.append("    WHERE YEAR       = '" + _param._year + "' ");
        stb.append("      AND NAMECD1 = 'B001' ");
        stb.append("      AND NAMESPARE2 IS NOT NULL ");
        stb.append(" ) ");
        stb.append(" , CHAIR AS ( ");
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
        stb.append("    SELECT SCD.EXECUTEDATE ");
        stb.append("         , SCD.UPDATED ");
        stb.append("         , SCD.PERIODCD ");
        stb.append("         , SCD.CHAIRCD ");
        stb.append("         , SRH.GRADE ");
        stb.append("         , SRH.HR_CLASS ");
        stb.append("         , SRH.HR_NAMEABBV ");
        stb.append("         , CHAIR.GROUPCD ");
        stb.append("         , SM.SUBCLASSABBV ");
        stb.append("      FROM SCH_CHR_DAT SCD ");
        stb.append(" LEFT JOIN CHAIR CHAIR ");
        stb.append("        ON CHAIR.CHAIRCD     = SCD.CHAIRCD ");
        stb.append("       AND CHAIR.YEAR        = SCD.YEAR ");
        stb.append("       AND CHAIR.SEMESTER    = SCD.SEMESTER ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("        ON SCD.YEAR          = SRH.YEAR ");
        stb.append("       AND SCD.SEMESTER      = SRH.SEMESTER ");
        stb.append("       AND CHAIR.TRGTGRADE   = SRH.GRADE ");
        stb.append("       AND CHAIR.TRGTCLASS   = SRH.HR_CLASS ");
        stb.append(" LEFT JOIN SUBCLASS_MST SM ");
        stb.append("        ON SM.CLASSCD        = CHAIR.CLASSCD ");
        stb.append("       AND SM.SCHOOL_KIND    = CHAIR.SCHOOL_KIND ");
        stb.append("       AND SM.CURRICULUM_CD  = CHAIR.CURRICULUM_CD ");
        stb.append("       AND SM.SUBCLASSCD     = CHAIR.SUBCLASSCD ");
        stb.append("     WHERE SCD.EXECUTEDATE  BETWEEN '" + _param._startDate + "' AND '" + _param._endDate + "' ");
        stb.append("   AND SCD.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ");
        stb.append("   AND CHAIR.SCHOOL_KIND = 'H' ");
        stb.append(" GROUP BY SCD.EXECUTEDATE ");
        stb.append("        , SCD.UPDATED ");
        stb.append("        , SCD.PERIODCD ");
        stb.append("        , SCD.CHAIRCD ");
        stb.append("        , SRH.GRADE ");
        stb.append("        , SRH.HR_CLASS ");
        stb.append("        , SRH.HR_NAMEABBV ");
        stb.append("        , CHAIR.GROUPCD ");
        stb.append("        , CHAIR.CHAIRABBV");
        stb.append("        , SM.SUBCLASSABBV ");
        stb.append("ORDER BY SCD.EXECUTEDATE ");
        stb.append("       , SRH.HR_CLASS ");
        stb.append("       , SRH.GRADE ");
        stb.append("       , SCD.PERIODCD ");

        return stb.toString();
    }

    /** 変更前時間割情報 **/
    private String sqlBeforeTimetable() {

        final StringBuffer stb = new StringBuffer();
        stb.append("  WITH PERIOD AS ( ");
        stb.append("    SELECT NAMECD2 AS PERIODCD ");
        stb.append("      FROM V_NAME_MST ");
        stb.append("    WHERE YEAR       = '" + _param._year + "' ");
        stb.append("       AND NAMECD1 = 'B001' ");
        stb.append("       AND NAMESPARE2 IS NOT NULL ");
        stb.append("  ) ");
        stb.append("  , CHAIR AS ( ");
        stb.append("      SELECT CHAIR_DAT.CHAIRCD ");
        stb.append("           , CHAIR_DAT.CLASSCD ");
        stb.append("           , CHAIR_DAT.SCHOOL_KIND ");
        stb.append("           , CHAIR_DAT.CURRICULUM_CD ");
        stb.append("           , CHAIR_DAT.SUBCLASSCD ");
        stb.append("           , CHAIR_DAT.CHAIRABBV");
        stb.append("           , CHAIR_CLS_DAT.TRGTGRADE ");
        stb.append("           , CHAIR_CLS_DAT.TRGTCLASS ");
        stb.append("           , CHAIR_CLS_DAT.GROUPCD ");
        stb.append("           , CHAIR_CLS_DAT.YEAR ");
        stb.append("           , CHAIR_CLS_DAT.SEMESTER ");
        stb.append("        FROM CHAIR_DAT ");
        stb.append("        JOIN CHAIR_CLS_DAT ");
        stb.append("          ON ( ");
        stb.append("                  CHAIR_DAT.YEAR        = CHAIR_CLS_DAT.YEAR ");
        stb.append("              AND CHAIR_DAT.SEMESTER    = CHAIR_CLS_DAT.SEMESTER ");
        stb.append("              AND CHAIR_DAT.GROUPCD     = CHAIR_CLS_DAT.GROUPCD ");
        stb.append("              AND CHAIR_CLS_DAT.CHAIRCD = '0000000' ");
        stb.append("              AND CHAIR_CLS_DAT.GROUPCD <> '" + NONE_GROUP_CODE + "' ");
        stb.append("             ) ");
        stb.append("       UNION ");
        stb.append("      SELECT CHAIR_DAT.CHAIRCD ");
        stb.append("           , CHAIR_DAT.CLASSCD ");
        stb.append("           , CHAIR_DAT.SCHOOL_KIND ");
        stb.append("           , CHAIR_DAT.CURRICULUM_CD ");
        stb.append("           , CHAIR_DAT.SUBCLASSCD ");
        stb.append("           , CHAIR_DAT.CHAIRABBV");
        stb.append("           , CHAIR_CLS_DAT.TRGTGRADE ");
        stb.append("           , CHAIR_CLS_DAT.TRGTCLASS ");
        stb.append("           , CHAIR_CLS_DAT.GROUPCD ");
        stb.append("           , CHAIR_CLS_DAT.YEAR ");
        stb.append("           , CHAIR_CLS_DAT.SEMESTER ");
        stb.append("        FROM CHAIR_DAT ");
        stb.append("        JOIN CHAIR_CLS_DAT ");
        stb.append("          ON ( ");
        stb.append("                  CHAIR_DAT.YEAR     = CHAIR_CLS_DAT.YEAR ");
        stb.append("              AND CHAIR_DAT.SEMESTER = CHAIR_CLS_DAT.SEMESTER ");
        stb.append("              AND CHAIR_DAT.CHAIRCD  = CHAIR_CLS_DAT.CHAIRCD ");
        stb.append("              AND CHAIR_DAT.GROUPCD  = '" + NONE_GROUP_CODE + "' ");
        stb.append("             ) ");
        stb.append("  ) ");
        stb.append("     SELECT SRH.GRADE ");
        stb.append("          , SRH.HR_CLASS ");
        stb.append("          , SRH.HR_NAMEABBV ");
        stb.append("          , CHAIR.GROUPCD ");
        stb.append("          , NMD.FROM_DATE ");
        stb.append("          , NMD.FROM_PERIODCD ");
        stb.append("          , NMD.FROM_CHAIRCD  ");
        stb.append("          , SM.SUBCLASSABBV ");
        stb.append("       FROM NOTICE_MESSAGE_DAT NMD ");
        stb.append("  LEFT JOIN CHAIR CHAIR ");
        stb.append("         ON CHAIR.CHAIRCD     = NMD.FROM_CHAIRCD ");
        stb.append("        AND CHAIR.YEAR        = '" + _param._year + "' ");
        stb.append("        AND CHAIR.SEMESTER    = '" + _param._gakki + "' ");
        stb.append("  LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("         ON CHAIR.YEAR        = SRH.YEAR ");
        stb.append("        AND CHAIR.SEMESTER    = SRH.SEMESTER ");
        stb.append("        AND CHAIR.TRGTGRADE   = SRH.GRADE ");
        stb.append("        AND CHAIR.TRGTCLASS   = SRH.HR_CLASS  ");
        stb.append("  LEFT JOIN SUBCLASS_MST SM ");
        stb.append("         ON SM.CLASSCD        = CHAIR.CLASSCD ");
        stb.append("        AND SM.SCHOOL_KIND    = CHAIR.SCHOOL_KIND ");
        stb.append("        AND SM.CURRICULUM_CD  = CHAIR.CURRICULUM_CD ");
        stb.append("        AND SM.SUBCLASSCD     = CHAIR.SUBCLASSCD ");
        stb.append("      WHERE NMD.FROM_DATE  BETWEEN '" + _param._startDate + "' AND '" + _param._endDate + "' ");
        stb.append("        AND NMD.FROM_PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ");
        stb.append("        AND VALUE (NMD.CANCEL_FLG, '0') = '0' ");
        stb.append("        AND CHAIR.SCHOOL_KIND = 'H' ");
        stb.append("   GROUP BY SRH.GRADE ");
        stb.append("          , SRH.HR_CLASS ");
        stb.append("          , SRH.HR_NAMEABBV ");
        stb.append("          , CHAIR.GROUPCD ");
        stb.append("          , NMD.FROM_DATE ");
        stb.append("          , NMD.FROM_PERIODCD ");
        stb.append("          , NMD.FROM_CHAIRCD  ");
        stb.append("          , SM.SUBCLASSABBV ");
        stb.append("          , CHAIR.CHAIRABBV");
        stb.append("   ORDER BY SRH.HR_CLASS ");
        stb.append("          , SRH.GRADE  ");

        return stb.toString();
    }

    /** 変更後時間割情報 **/
    private String sqlAfterTimetable() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH PERIOD AS ( ");
        stb.append("   SELECT NAMECD2 AS PERIODCD ");
        stb.append("     FROM V_NAME_MST ");
        stb.append("    WHERE YEAR       = '" + _param._year + "' ");
        stb.append("      AND NAMECD1 = 'B001' ");
        stb.append("      AND NAMESPARE2 IS NOT NULL ");
        stb.append(" ) ");
        stb.append(" , CHAIR AS ( ");
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
        stb.append("    SELECT SRH.GRADE ");
        stb.append("         , SRH.HR_CLASS ");
        stb.append("         , SRH.HR_NAMEABBV ");
        stb.append("         , CHAIR.GROUPCD ");
        stb.append("         , NMD.TO_PERIODCD ");
        stb.append("         , NMD.FROM_DATE ");
        stb.append("         , NMD.TO_DATE ");
        stb.append("         , NMD.TO_CHAIRCD ");
        stb.append("         , SM.SUBCLASSABBV AS TOSUBCLASSNAME ");
        stb.append("      FROM NOTICE_MESSAGE_DAT NMD ");
        stb.append(" LEFT JOIN CHAIR CHAIR ");
        stb.append("        ON CHAIR.CHAIRCD     = NMD.TO_CHAIRCD ");
        stb.append("       AND CHAIR.YEAR        = '" + _param._year + "' ");
        stb.append("       AND CHAIR.SEMESTER    = '" + _param._gakki + "' ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("        ON CHAIR.YEAR        = SRH.YEAR ");
        stb.append("       AND CHAIR.SEMESTER    = SRH.SEMESTER ");
        stb.append("       AND CHAIR.TRGTGRADE   = SRH.GRADE ");
        stb.append("       AND CHAIR.TRGTCLASS   = SRH.HR_CLASS  ");
        stb.append(" LEFT JOIN SUBCLASS_MST SM ");
        stb.append("        ON SM.CLASSCD        = CHAIR.CLASSCD ");
        stb.append("       AND SM.SCHOOL_KIND    = CHAIR.SCHOOL_KIND ");
        stb.append("       AND SM.CURRICULUM_CD  = CHAIR.CURRICULUM_CD ");
        stb.append("       AND SM.SUBCLASSCD     = CHAIR.SUBCLASSCD ");
        stb.append("     WHERE NMD.TO_DATE  BETWEEN '" + _param._startDate + "' AND '" + _param._endDate + "' ");
        stb.append("       AND NMD.TO_CHAIRCD  NOT IN (SELECT PERIODCD FROM PERIOD) ");
        stb.append("       AND VALUE (NMD.CANCEL_FLG, '0') = '0' ");
        stb.append("       AND CHAIR.SCHOOL_KIND = 'H' ");
        stb.append("  GROUP BY SRH.GRADE ");
        stb.append("         , SRH.HR_CLASS ");
        stb.append("         , SRH.HR_NAMEABBV ");
        stb.append("         , CHAIR.GROUPCD ");
        stb.append("         , NMD.TO_PERIODCD ");
        stb.append("         , NMD.FROM_DATE ");
        stb.append("         , NMD.TO_DATE ");
        stb.append("         , NMD.TO_CHAIRCD ");
        stb.append("         , SM.SUBCLASSABBV ");
        stb.append("         , CHAIR.CHAIRABBV");
        stb.append("  ORDER BY SRH.HR_CLASS ");
        stb.append("         , SRH.GRADE  ");

        return stb.toString();
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision$ $Date$"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        private final String _year;
        private final String _gakki;
        private final String _startDate;
        private final String _endDate;
        private final String _loginDate;
        private final String _lastDate;
        final boolean _isOutputDebug;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("YEAR"); //年度
            _gakki     = request.getParameter("SEMESTER"); //学期
            _loginDate = request.getParameter("LOGIN_DATE").replace('/', '-');// ログイン日付
            _lastDate  = request.getParameter("LAST_DATE").replace('/', '-'); // 学期の最終日
            _startDate = request.getParameter("START_DATE").replace('/', '-');// 時間割指定日付(開始日)
            _endDate   = request.getParameter("END_DATE").replace('/', '-'); // 時間割指定日付(終了日)

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2,
                        " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJB060B' AND NAME = '" + propName + "' "));
        }
    }

    /** 時間割 */
    private class TimeTable {
        public String _executeDate;
        public String _updated;
        public int    _periodCd;
        public String _chairCd;
        public String _grade;
        public String _hrClass;
        public String _hrNameAbbv;
        public String _groupCd;
        public String _subclassAbbv;
        public int    _choiceCount;

        public TimeTable(
              final String executeDate
            , final String updated
            , final int    periodCd
            , final String chairCd
            , final String grade
            , final String hrClass
            , final String hrNameAbbv
            , final String groupCd
            , final String subclassAbbv
            , final int    choiceCount
        ) {
            _executeDate  = executeDate;
            _updated      = updated;
            _periodCd     = periodCd;
            _chairCd      = chairCd;
            _grade        = grade;
            _hrClass      = hrClass;
            _hrNameAbbv   = hrNameAbbv;
            _groupCd      = groupCd;
            _subclassAbbv = subclassAbbv;
            _choiceCount  = choiceCount;
        }
    }

    /** 変更前情報 */
    private class BeforeTimeTable {
        public String _grade;
        public String _hrClass;
        public String _hrNameAbbv;
        public String _groupCd;
        public String _fromDate;
        public int    _fromPeriodCd;
        public String _fromChairCd;
        public String _subclassAbbv;
        public int    _choiceCount;

        public BeforeTimeTable(
              final String grade
            , final String hrClass
            , final String hrNameAbbv
            , final String groupCd
            , final String fromDate
            , final int    fromPeriodCd
            , final String fromChairCd
            , final String subclassAbbv
            , final int    choiceCount
        ) {
            _grade        = grade;
            _hrClass      = hrClass;
            _hrNameAbbv   = hrNameAbbv;
            _groupCd      = groupCd;
            _fromDate     = fromDate;
            _fromPeriodCd = fromPeriodCd;
            _fromChairCd  = fromChairCd;
            _subclassAbbv = subclassAbbv;
            _choiceCount  = choiceCount;
        }
    }

    /** 変更後情報 */
    private class AfterTimeTable {
        public String _grade;
        public String _hrClass;
        public String _hrNameAbbv;
        public String _groupCd;
        public String _grade2;
        public String _fromDate;
        public String _toDate;
        public int    _toPeriodCd;
        public String _toChairCd;
        public String _toSubclassName;
        public int    _choiceCount;

        public AfterTimeTable(
              final String grade
            , final String hrClass
            , final String hrNameAbbv
            , final String groupCd
            , final String grade2
            , final String fromDate
            , final String toDate
            , final int    toPeriodCd
            , final String toChairCd
            , final String toSubclassName
            , final int    choiceCount
        ) {
            _grade          = grade;
            _hrClass        = hrClass;
            _hrNameAbbv     = hrNameAbbv;
            _groupCd        = groupCd;
            _grade2         = grade2;
            _fromDate       = fromDate;
            _toDate         = toDate;
            _toPeriodCd     = toPeriodCd;
            _toChairCd      = toChairCd;
            _toSubclassName = toSubclassName;
            _choiceCount    = choiceCount;
        }
    }

    //特定クラスの1日の授業
    class LecturesByClass {
        //特定クラス・特定時限の授業情報
        class Lecture {
            String  _beforeSubclassName;//変更前：科目名
            Boolean _beforeAmikakeFlg;  //変更前：網掛けするか
            Boolean _beforeTorikesiFlg; //変更前：取り消し線を印字するか
            String  _afterSubclassName; //変更後：科目名
            Boolean _afterAmikakeFlg;   //変更後：網掛けするか
        }

        String  _hrClass;      //学年クラス
        String  _hrClassName;  //クラス名
        Lecture[] _lectures = new Lecture[9];//9限目まで保持

        public LecturesByClass() {
            for (int i = 0; i < _lectures.length; i++) {
                _lectures[i] = new Lecture();
            }
        }
    }

    //学年ごとの授業
    class LecturesByGrade {
        Map<String, LecturesByClass> _lecturesByGrade = new HashMap<String, LecturesByClass>(){};
    }

    //帳票印字データ用クラス
    class PrintData {
        private static final String ATTR_AMIKAKE = "Paint=(1,80,2), Bold=1";
        private static final String ATTR_TORIKESHI = "UnderLine=(0,1,5)";

        String _printDate;//該当日付
        String _updateDate;//最終更新日時
        LecturesByGrade[] grade = new LecturesByGrade[3];  //3学年分を保持

        public PrintData() {
            for (int i = 0; i < grade.length; i++) {
                grade[i] = new LecturesByGrade();
            }
        }

        //1ページ目印字
        void printPage1(final Vrw32alp svf, final Param param){
            int[] gradePrintCnt = new int[]{0, 0, 0};  //1ページに印字した1学年のクラス数
            boolean[] endPrintFlg = new boolean[] {false, false, false};
            String[][] className = new String[grade.length][];  //学年毎のクラス

            final String form = "KNJB060B_1.xml";
            setForm(svf, param, form, 1);

            for (int grd = 0; grd < grade.length;  grd++) {
                int cnt = 0;
                className[grd] = new String[grade[grd]._lecturesByGrade.size()];
                for (String cn : grade[grd]._lecturesByGrade.keySet()) {
                    className[grd][cnt] = cn;
                    cnt++;
                }
                Arrays.sort(className[grd]);
            }

            //時間割を印字
            while (true) {
                boolean endFlg = true;
                for (int grd = 0; grd < grade.length;  grd++) {
                    if (endPrintFlg[grd] == false) {
                        printPage1Body(svf, gradePrintCnt, className, endPrintFlg);
                        svf.VrEndPage();
                        endFlg = false;
                        break;
                    }
                }
                if (endFlg) {
                    //全てのクラスを印字したら終了
                    break;
                }
            }
        }

        //受け取った文字を印字するfieldを返す
        String getPrintField(String printName) {
            final int printNameLen = KNJ_EditEdit.getMS932ByteLength(printName);
            final String printField = (printNameLen <= 6)  ? "1" :
                                         (printNameLen <= 8)  ? "2" :
                                         (printNameLen <= 16) ? "3" :
                                         (printNameLen <= 30) ? "4" : "5";
            return printField;
        }

        //科目の印字
        void printSubclassName(final Vrw32alp svf, final String col, final int period, final String subclassName, final String type, final boolean torikesiFlg) {
            final String printField = getPrintField(subclassName);
            int size = 0;  //1行に印字する文字数(半角)
            int line = 0;  //行数

            if (("1").equals(printField) == true) {
                size = 6;
                line = 1;
            } else if (("2").equals(printField) == true) {
                size = 8;
                line = 1;
            } else if (("3").equals(printField) == true) {
                size = 8;
                line = 2;
            } else if (("4").equals(printField) == true) {
                size = 10;
                line = 3;
            } else if (("5").equals(printField) == true) {
                size = 12;
                line = 3;
            }

            KNJObjectAbs knjobj = new KNJEditString();
            ArrayList subclasslist = knjobj.retDividString(subclassName, size, line);

            if ( subclasslist != null ) {
                String rowField = "";
                for (int i = 0; i < subclasslist.size(); i++) {
                    if (0 < i) {
                        rowField = "_" + (i + 1);
                    }
                    //科目
                    svf.VrsOutn(type + "_SUBCLASSABBV" + col + "_" + printField + rowField, period, (String)subclasslist.get(i));
                    if (type.equals("BEFORE") && torikesiFlg == true) {
                        //取り消し線の印字
                        svf.VrAttributen(type + "_SUBCLASSABBV" + col + "_" + printField + rowField, period, ATTR_TORIKESHI);
                    }
                }
            }
        }

        //時間割を印字
        private void printPage1Body(final Vrw32alp svf, int[] gradePrintCnt, String[][] className, boolean[] endPrintFlg) {
            final int GRADE_MAX_CLASS = 8;  //1ページに1学年最大8クラスまで印字
            //ヘッダー・フッター
            svf.VrsOut("EXECUTEDATE", _printDate);   //該当日付
            svf.VrsOut("UPDATED"    , _updateDate);  //最終更新日時
            svf.VrAttribute("MOVE_MARK", ATTR_AMIKAKE);

            //学年
            for (int grd = 0; grd < grade.length; grd++) {
                //クラス
                int classCnt = 0;
                endPrintFlg[grd] = true;

                for (int cnt = gradePrintCnt[grd]; cnt < grade[grd]._lecturesByGrade.size(); cnt++) {
                    LecturesByClass lbc = grade[grd]._lecturesByGrade.get(className[grd][cnt]);

                    final int hrNameLen = KNJ_EditEdit.getMS932ByteLength(lbc._hrClassName);
                    final String hrNameField = (hrNameLen <= 6) ? "1": (hrNameLen <= 8) ? "2" : "3" ;
                    //クラス名
                    svf.VrsOutn("HR_NAME" + (grd + 1) + "_" + hrNameField, (classCnt + 1), lbc._hrClassName);

                    //時限
                    for (int period = 1; period < 9; period++) {
                        //変更前科目
                        final String beforeSubclassName = lbc._lectures[period]._beforeSubclassName;
                        final String col = (grd + 1) + "_" + (classCnt + 1);
                        final boolean torikesiFlg = lbc._lectures[period]._beforeTorikesiFlg != null ? true : false;
                        printSubclassName(svf, col, period, beforeSubclassName, "BEFORE", torikesiFlg);

                        //変更後科目
                        final String afterSubclassName = lbc._lectures[period]._afterSubclassName;
                        if (   (beforeSubclassName != null)
                            && (afterSubclassName != null)
                            && (beforeSubclassName.equals(afterSubclassName) == false)) {
                            printSubclassName(svf, col, period, afterSubclassName, "AFTER", false);

                            if (lbc._lectures[period]._beforeAmikakeFlg != null) {
                                //網掛け表示
                                svf.VrAttributen("BEFORE_SUBCLASSABBV" + col + "_1", period, ATTR_AMIKAKE);
                            }
                        }
                    }
                    classCnt++;
                    if (GRADE_MAX_CLASS <= classCnt) {
                        endPrintFlg[grd] = false;
                        break;
                    }
                }
                gradePrintCnt[grd] = classCnt;
            }
        }

        //2ページ目印字
        void printPage2(final Vrw32alp svf, final Param param) {
            final int GRADE_MAX_CLASS = 20;  //1ページに1学年最大20クラスまで印字
            String[][] className = new String[grade.length][];  //学年毎のクラス
            ArrayList<LecturesByClass> lectires = new ArrayList<LecturesByClass>();

            final String form2 = "KNJB060B_2.xml";
            setForm(svf, param, form2, 1);

            for (int grd = 0; grd < grade.length; grd++) {
                int cnt = 0;
                className[grd] = new String[grade[grd]._lecturesByGrade.size()];
                for (String cn : grade[grd]._lecturesByGrade.keySet()) {
                    className[grd][cnt] = cn;
                    cnt++;
                }
                Arrays.sort(className[grd]);
            }

            //学年、クラス別のデータを1つにまとめる
            for (int grd = 0; grd < grade.length; grd++) {
                for (int cnt = 0; cnt < grade[grd]._lecturesByGrade.size(); cnt++) {
                    LecturesByClass lbc = grade[grd]._lecturesByGrade.get(className[grd][cnt]);
                    lectires.add(lbc);
                }
            }

            //ヘッダー・フッター
            svf.VrsOut("EXECUTEDATE", _printDate);   //該当日付
            svf.VrsOut("UPDATED"    , _updateDate);  //最終更新日時
            svf.VrsOut("MOVE_MARK", "●");
            svf.VrAttribute("MOVE_MARK", ATTR_AMIKAKE);

            int classCnt = 0;
            for (int gradeClass = 0; gradeClass < lectires.size(); gradeClass++) {
                LecturesByClass lbc = lectires.get(gradeClass);
                String[] gradeHrClassName = lbc._hrClass.split("-");
                //クラス名
                svf.VrsOutn("GRADE"   , (classCnt + 1), gradeHrClassName[0] + "年");
                svf.VrsOutn("HR_NAME" , (classCnt + 1), gradeHrClassName[1] + "組");

                //時限
                for (int period = 1; period < 9; period++) {
                    //変更前科目
                    final String beforeSubclassName = lbc._lectures[period]._beforeSubclassName;
                    final String col = String.valueOf(classCnt + 1);
                    final boolean torikesiFlg = lbc._lectures[period]._beforeTorikesiFlg != null ? true : false;
                    printSubclassName(svf, col, period, beforeSubclassName, "BEFORE", torikesiFlg);

                    //変更後科目
                    final String afterSubclassName = lbc._lectures[period]._afterSubclassName;
                    if (   (beforeSubclassName != null)
                        && (afterSubclassName != null)
                        && (beforeSubclassName.equals(afterSubclassName) == false)) {
                        final String afterField = getPrintField(afterSubclassName);
                        final String fieldName = "AFTER_SUBCLASSABBV" + col + "_" + afterField;

                        if (lbc._lectures[period]._beforeAmikakeFlg != null) {
                            //網掛け表示
                            printSubclassName(svf, col, period, "●" + afterSubclassName, "AFTER", false);
                            svf.VrAttributen(fieldName, period, ATTR_AMIKAKE);
                            svf.VrAttributen("BEFORE_SUBCLASSABBV"+ col + "_1", period, ATTR_AMIKAKE);
                        } else {
                            printSubclassName(svf, col, period, afterSubclassName, "AFTER", false);
                        }
                    }
                }
                classCnt++;
                if (GRADE_MAX_CLASS <= classCnt) {
                    classCnt = 0;
                    svf.VrEndPage();

                    //ヘッダー・フッター
                    svf.VrsOut("EXECUTEDATE", _printDate);   //該当日付
                    svf.VrsOut("UPDATED"    , _updateDate);  //最終更新日時
                    svf.VrsOut("MOVE_MARK", "●");
                    svf.VrAttribute("MOVE_MARK", ATTR_AMIKAKE);
                }
            }
            svf.VrEndPage();
        }
    }
}//クラスの括り

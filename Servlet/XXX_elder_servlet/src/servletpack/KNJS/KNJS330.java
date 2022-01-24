/*
 * $Id: a1514aa6f48147cea3383af3bd7cbe6aeb1494da $
 *
 * 作成日: 2011/11/02
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJS;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

/**
 *  学校教育システム 賢者 [小学校プログラム] 指導計画表
 *
 */
public class KNJS330 {

    private static final Log log = LogFactory.getLog(KNJS330.class);

    private static final String[] youbi = new String[]{null, "日", "月", "火", "水", "木", "金", "土"};
    
    private static final String DIV_YOTEI = "TOTAL_SCHEDULE";
    private static final String DIV_JISSI = "TOTAL_WEEK";
    private static final String DIV_RUIKEI = "TOTAL_YEAR";
    private static final String DIV_HYOJUN = "TOTAL_DEF";

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svfPrintHead(svf);
        svfPrintDayList(svf);
        svfPrintClass(svf);
    }
    
    private void svfPrintHead(final Vrw32alp svf) {
        svf.VrSetForm(_param.getFormName(), 4);
        svf.VrsOut("NENDO", _param.getNendo() + "　" + _param.getTitle());
//        svf.VrsOut("PERIOD", "（" + _param.getTermDate() + "）");
        svf.VrsOut("HR_NAME", _param._hrname);
        svf.VrsOut("TEACHER", _param._staffname);
        svf.VrsOut("ymd1", _param.getCtrlDate());
        for (final Iterator it = _param._periodList.iterator(); it.hasNext();) {
            final Period period = (Period) it.next();
            svf.VrsOut("HOUR" + period._line, period._name);
        }
    }

    /**
     * 日付ごとの行事・備考を出力(レコード)
     * @param svf
     */
    private void svfPrintDayList(final Vrw32alp svf) {
        for (final Iterator dateit = _param._dates.keySet().iterator(); dateit.hasNext();) { // 日付ごとにレコード出力
            final String date = (String) dateit.next();
            final PrintDay printDay = (PrintDay) _param._dates.get(date);

            final int dayLine = printDay.getDayLine();

            svf.VrsOutn("DATE", dayLine, printDay.getDateWeek()); //日付・曜日
            svf.VrsOutn("EVENT1", dayLine, printDay._remark1); //行事
            for (final Iterator it = _param._periodList.iterator(); it.hasNext();) {
                final Period period = (Period) it.next();
                final SchChrDat schChrDat = (SchChrDat) printDay._schChrDatMap.get(period._periodCd);
                if (null != schChrDat) {
                    svf.VrsOutn("CLASS1_" + period._line, dayLine, "2".equals(_param._targetJisu) ? schChrDat._className : schChrDat._subclassName); //科目
                    svf.VrsOutn("UNIT_NAME" + period._line + "_1", dayLine, schChrDat._unitName); //単元
                    svf.VrsOutn("UNIT_NAME" + period._line + "_4", dayLine, schChrDat._remark); //備考
                }
            }

            _hasData = true;
        }
    }

    /**
     * 教科ごとの時数を出力(レコード)
     * @param svf
     */
    private void svfPrintClass(final Vrw32alp svf) {
        final int MAX_LINE = 22;
        String groupCode = "";
        int groupLine = 0;
        int line = 0;
        int totalYotei = 0;
        int totalJissi = 0;
        int totalRuikei = 0;
        int totalFusoku = 0;
        int totalHyojun = 0;
        for (final Iterator codeit = _param._chairMap.keySet().iterator(); codeit.hasNext();) { // 日付ごとにレコード出力
            final String code = (String) codeit.next();
            final ClassData classData = (ClassData) _param._chairMap.get(code);

            line++;
            groupLine++;
            if (!groupCode.equals(classData.getClassGroupCode())) {
                //小計
                if (!"11".equals(classData.getClassGroupCode()) && "11".equals(groupCode)) {
                    svf.VrsOut("CLASS_NAME_GRP", groupCode); //教科グループ
                    svf.VrsOut("CLASS2", "小計"); //科目
                    svf.VrsOut("SCHEDULE", String.valueOf(totalYotei)); //予定
                    svf.VrsOut("RECORD", String.valueOf(totalJissi)); //実施
                    svf.VrsOut("SUBTOTAL", String.valueOf(totalRuikei)); //累計
                    svf.VrsOut("DEF", String.valueOf(totalFusoku)); //過不足
                    svf.VrEndRecord();
                    line++;
                    groupLine++;
                    //空行
                    svf.VrsOut("CLASS_NAME_GRP", "99"); //教科グループ
                    svf.VrEndRecord();
                    line++;
                    groupLine++;
                }
                groupCode = classData.getClassGroupCode();
                groupLine = 1;
            }
            svf.VrsOut("CLASS_NAME_GRP", groupCode); //教科グループ
            svf.VrsOut("CLASS_NAME", classData.getClassGroupNameOne(groupLine)); //教科
            svf.VrsOut("CLASS2", classData._name); //科目

            int cntRuikei = 0;
            int cntHyojun = 0;
            for (final Iterator it = classData._jisuMap.keySet().iterator(); it.hasNext();) {
                final String div = (String) it.next();

                final String cnt = (String) classData._jisuMap.get(div);
                if (DIV_YOTEI.equals(div)) {
                    svf.VrsOut("SCHEDULE", cnt); //予定
                    totalYotei += Integer.parseInt(cnt);
                }
                if (DIV_JISSI.equals(div)) {
                    svf.VrsOut("RECORD", cnt); //実施
                    totalJissi += Integer.parseInt(cnt);
                }
                if (DIV_RUIKEI.equals(div)) {
                    svf.VrsOut("SUBTOTAL", cnt); //累計
                    cntRuikei = Integer.parseInt(cnt);
                    totalRuikei += Integer.parseInt(cnt);
                }
                if (DIV_HYOJUN.equals(div)) {
                    cntHyojun = Integer.parseInt(cnt);
                    totalHyojun += Integer.parseInt(cnt);
                }
            }
            if (0 < cntHyojun || 0 < cntRuikei) {
                svf.VrsOut("DEF", String.valueOf(cntHyojun - cntRuikei)); //過不足
                totalFusoku += cntHyojun - cntRuikei;
            }

            svf.VrEndRecord();
            _hasData = true;
        }
        if (0 == line) {
            svf.VrEndRecord();
        } else {
            //総計
            svf.VrsOut("TOTAL_SCHEDULE", String.valueOf(totalYotei)); //予定
            svf.VrsOut("TOTAL_RECORD", String.valueOf(totalJissi)); //実施
            svf.VrsOut("TOTAL_SUBTOTAL", String.valueOf(totalRuikei)); //累計
            svf.VrsOut("TOTAL_DEF", String.valueOf(totalFusoku)); //過不足
            //小計
            if ("11".equals(groupCode)) {
                svf.VrsOut("CLASS_NAME_GRP", groupCode); //教科グループ
                svf.VrsOut("CLASS2", "小計"); //科目
                svf.VrsOut("SCHEDULE", String.valueOf(totalYotei)); //予定
                svf.VrsOut("RECORD", String.valueOf(totalJissi)); //実施
                svf.VrsOut("SUBTOTAL", String.valueOf(totalRuikei)); //累計
                svf.VrsOut("DEF", String.valueOf(totalFusoku)); //過不足
                svf.VrEndRecord();
                line++;
                groupLine++;
                //空行
                svf.VrsOut("CLASS_NAME_GRP", "99"); //教科グループ
                svf.VrEndRecord();
                line++;
                groupLine++;
            }
            for (int i = line; i < MAX_LINE; i++) {
                svf.VrsOut("CLASS_NAME_GRP", "99"); //教科グループ
                svf.VrEndRecord();
            }
        }
    }

    private String getDateString(final Calendar cal) {
        final DecimalFormat df = new DecimalFormat("00");
        return cal.get(Calendar.YEAR) + "-" + df.format(cal.get(Calendar.MONTH) + 1) + "-" + df.format(cal.get(Calendar.DATE));
    }

    /**
     * 表示する日
     */
    private class PrintDay {
        final String _date;
        final String _month;
        final String _dayOfMonth;
        final String _youbi;
        final int _dayOfWeek;

        String _remark1;
        boolean _isPrintTarget = false;

        final Map _schChrDatMap;

        PrintDay(final Calendar cal) {
            _date = getDateString(cal);
            _month = String.valueOf(cal.get(Calendar.MONTH) + 1);
            _dayOfMonth = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            _dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1:日 2:月 3:火 4:水 5:木 6:金 7:土
            _youbi = youbi[_dayOfWeek];
            _remark1 = "";
            _schChrDatMap = new HashMap();
        }

        private String getDateWeek() {
            if (null == _date) return "";
            final String[] date = StringUtils.split(_date, "-");
            return date[0] + "年" + date[1] + "月" + date[2] + "日" + "(" + _youbi + ")";
        }

        private int getDayLine() {
            if (null == _date) return 0;
            return _dayOfWeek == 1 ? 7 : _dayOfWeek - 1;
        }

        public String toString() {
            return "PrintDay(" + _date + ")";
        }
    }

    /**
     * 校時
     */
    private class Period {
        final String _periodCd;
        final String _name;
        final int _line;

        Period(final String periodCd, final String name, final int line) {
            _periodCd = periodCd;
            _name = name;
            _line = line;
        }
    }

    /**
     * 時間割
     */
    private class SchChrDat {
        final String _executeDate;
        final String _periodCd;
        final String _subclassCd;
        final String _subclassName;
        final String _classCd;
        final String _className;
        final String _unitName;
        final String _remark;

        SchChrDat(final String executeDate, final String periodCd, final String subclassCd, final String subclassName, final String classCd, final String className, final String unitName, final String remark) {
            _executeDate = executeDate;
            _periodCd = periodCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _classCd = classCd;
            _className = className;
            _unitName = unitName;
            _remark = remark;
        }
    }

    private class ClassData {
        final String _code;
        final String _name;
        final String _classCd;
        final Map _jisuMap;

        ClassData(final String code, final String name, final String classCd) {
            _code = code;
            _name = name;
            _classCd = classCd;
            _jisuMap = new HashMap();
        }

        private String getClassGroupCode() {
            if (parseIntClasscd() < 90) return "11";
            return _classCd;
        }
        private int parseIntClasscd() {
            final String classcd;
            if ("1".equals(_param._useCurriculumcd)) {
                classcd = StringUtils.split(_classCd, "-")[0];
            } else {
                classcd = _classCd;
            }
            return Integer.parseInt(classcd);
        }

        private String getClassGroupName() {
            if (parseIntClasscd() < 90) return "教科";
            if ("90".equals(_classCd)) return "総合学習";
            if ("93".equals(_classCd)) return "特別活動";
            return "";
        }

        private String getClassGroupNameOne(final int line) {
            final int len = getClassGroupName().length();//文字数
            if (len < line) return "";
            final String str = getClassGroupName().substring(line - 1, line);
            return str;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }
    
    /** パラメータクラス */
    private class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _gradeHrclass;     //年組
        final String _targetMonth;      //月
        final String _targetWeek;       //週 1:1週 〜 6:6週
        final String _targetFirstDate;  //週の最初の日（日曜日）
        final String _targetJisu;       //教科名 1:時間割上教科名 2:時数集計教科名
        final String _targetUnit;       //単元名 1:最下位 2:大単元
        final String _targetRemark;     //備考欄 1:メモ 2:授業内容 3:目標
        final String _targetForm;       //フォーム 1:月〜金 2:月〜日
        final String _useCurriculumcd;  //教育課程コード

        final String _hrname;
        final String _staffname;
        final boolean _seirekiFlg;
        final TreeMap _dates = new TreeMap();
        final List _periodList;
        final TreeMap _chairMap = new TreeMap();
        
        String _weekSdate = null;
        String _weekEdate = null;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _targetMonth = request.getParameter("MONTH");
            _targetWeek = request.getParameter("WEEK");
            _targetFirstDate = request.getParameter("FIRSTDATE" + _targetWeek);
            _targetJisu = request.getParameter("JISU");
            _targetUnit = request.getParameter("UNIT");
            _targetRemark = request.getParameter("REMARK");
            _targetForm = request.getParameter("FORM");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            
            _hrname = getHrname(db2);
            _staffname = getStaffname(db2);
            _seirekiFlg = getSeirekiFlg(db2);
            setDates(db2);
            setEventDat(db2);
            _periodList = getPeriodList(db2);
            setSchChrDatMap(db2);
            setChairMap(db2);
            for (final Iterator it = _chairMap.keySet().iterator(); it.hasNext();) {
                final String code = (String) it.next();
                setJisuMap(db2, code, DIV_YOTEI);
                setJisuMap(db2, code, DIV_JISSI);
                setJisuMap(db2, code, DIV_RUIKEI);
                setJisuMap(db2, code, DIV_HYOJUN);
            }
        }

        /**
         * 日付を[月 火 水 木 金 土 日]の順で_datesマップにセット
         */
        private void setDates(final DB2UDB db2) {
            //月〜金or土
            final Calendar cal = Calendar.getInstance();
            cal.setTime(java.sql.Date.valueOf(_targetFirstDate));
            int imax = "2".equals(_targetForm) ? 6 : 5;
            for (int i = 0; i < imax; i++) {
                cal.add(Calendar.DATE, 1);
                final PrintDay printDay = new PrintDay(cal);
                _dates.put(getDateString(cal), printDay);
                if ("1".equals(_targetForm) && i == 0) _weekSdate = getDateString(cal);//月
                _weekEdate = getDateString(cal);//金or土
            }
            //日
            if ("2".equals(_targetForm)) {
                final Calendar calFirstDate = Calendar.getInstance();
                calFirstDate.setTime(java.sql.Date.valueOf(_targetFirstDate));
                final PrintDay printDay = new PrintDay(calFirstDate);
                _dates.put(getDateString(calFirstDate), printDay);
                _weekSdate = getDateString(calFirstDate);//日
            }
        }

        private void setEventDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   T1.EXECUTEDATE, T1.HOLIDAY_FLG, VALUE(T1.REMARK1, '') AS REMARK1, VALUE(T1.REMARK2, '') AS REMARK2 ");
                stb.append(" FROM ");
                stb.append("   EVENT_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                stb.append("   AND T1.EXECUTEDATE BETWEEN DATE('" + _weekSdate + "') AND DATE('" + _weekEdate + "') ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final PrintDay printDay = (PrintDay) _dates.get(rs.getString("EXECUTEDATE"));
                    if (null == printDay) {
                        log.error("printDay無し :" + rs.getString("EXECUTEDATE"));
                    } else {
                        printDay._remark1 = rs.getString("REMARK1");
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setSchChrDatMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHrUnitDat("", "");
//                log.debug("getHrUnitDat = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String executeDate = rs.getString("EXECUTEDATE");
                    final String periodCd = rs.getString("PERIODCD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String classCd = rs.getString("CLASSCD");
                    final String className = rs.getString("CLASSNAME");
                    final String unitName = rs.getString("UNIT_NAME");
                    final String remark = rs.getString("REMARK");

                    final PrintDay printDay = (PrintDay) _dates.get(executeDate);
                    if (null == printDay) {
                        log.error("printDay無し :" + executeDate);
                    } else {
                        final SchChrDat schChrDat = new SchChrDat(executeDate, periodCd, subclassCd, subclassName, classCd, className, unitName, remark);
                        printDay._schChrDatMap.put(periodCd, schChrDat);
                    }
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getHrUnitDat(final String code, final String div) {
            final StringBuffer stb = new StringBuffer();
            
            stb.append(" WITH MAIN_T AS ( ");
            if ("".equals(div)) {
                stb.append(" SELECT DISTINCT ");
                stb.append("     SCH_CDAT.EXECUTEDATE, ");
                stb.append("     SCH_CDAT.PERIODCD, ");
                stb.append("     SCH_CDAT.CHAIRCD, ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append("     SUBD.SCHOOL_KIND, ");
                    stb.append("     SUBD.CURRICULUM_CD, ");
                }
                stb.append("     SUBD.SUBCLASSCD, ");
                stb.append("     SUBD.SUBCLASSNAME, ");
                stb.append("     SUBD.SUBCLASSABBV, ");
                stb.append("     CLASSD.CLASSCD, ");
                stb.append("     CLASSD.CLASSNAME, ");
                stb.append("     CLASSD.CLASSABBV, ");
                if ("1".equals(_targetUnit)) {
                    stb.append("     CASE WHEN UNITD.UNIT_S_NAME IS NOT NULL ");
                    stb.append("          THEN UNITD.UNIT_S_NAME ");
                    stb.append("          ELSE CASE WHEN UNITD.UNIT_M_NAME IS NOT NULL ");
                    stb.append("                    THEN UNITD.UNIT_M_NAME ");
                    stb.append("                    ELSE UNITD.UNIT_L_NAME ");
                    stb.append("               END ");
                    stb.append("     END AS UNIT_NAME, ");
                } else {
                    stb.append("     UNITD.UNIT_L_NAME AS UNIT_NAME, ");
                }
                if ("1".equals(_targetRemark)) {
                    stb.append("     UNITD.REMARK1 AS REMARK ");
                } else if ("2".equals(_targetRemark)) {
                    stb.append("     UNITD.REMARK2 AS REMARK ");
                } else {
                    stb.append("     UNITD.REMARK3 AS REMARK ");
                }
            } else {
                stb.append(" SELECT DISTINCT ");
                stb.append("     SCH_CDAT.EXECUTEDATE, ");
                stb.append("     SCH_CDAT.PERIODCD, ");
                stb.append("     SCH_CDAT.CHAIRCD, ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append("     SUBD.CLASSCD, ");
                    stb.append("     SUBD.SCHOOL_KIND, ");
                    stb.append("     SUBD.CURRICULUM_CD, ");
                }
                stb.append("     SUBD.SUBCLASSCD ");
            }
            stb.append(" FROM ");
            stb.append("     UNIT_SCH_CHR_RANK_DAT SCH_CDAT ");
            stb.append("     INNER JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = SCH_CDAT.YEAR ");
            stb.append("                AND CHAIR.SEMESTER = SCH_CDAT.SEMESTER ");
            stb.append("                AND CHAIR.CHAIRCD = SCH_CDAT.CHAIRCD ");
            stb.append("     INNER JOIN SUBCLASS_MST SUBD ON SUBD.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("     AND SUBD.CLASSCD = CHAIR.CLASSCD ");
                stb.append("     AND SUBD.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
                stb.append("     AND SUBD.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            }
            stb.append("     INNER JOIN CLASS_MST CLASSD ON ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("     CLASSD.CLASSCD = SUBD.CLASSCD ");
                stb.append("     AND CLASSD.SCHOOL_KIND = SUBD.SCHOOL_KIND ");
            } else {
                stb.append("       CLASSD.CLASSCD = SUBSTR(SUBD.SUBCLASSCD, 1, 2) ");
            }
            stb.append("     INNER JOIN UNIT_STUDY_TEXT_BOOK_DAT UNIT_TEXT ON UNIT_TEXT.YEAR = CHAIR.YEAR ");
            stb.append("                AND UNIT_TEXT.GRADE = '" + _gradeHrclass.substring(0, 2) + "' ");
            stb.append("                AND UNIT_TEXT.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          AND UNIT_TEXT.CLASSCD = CHAIR.CLASSCD ");
                stb.append("          AND UNIT_TEXT.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
                stb.append("          AND UNIT_TEXT.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN UNIT_DAT UNITD ON UNITD.YEAR = SCH_CDAT.YEAR ");
            stb.append("          AND UNITD.DATA_DIV = '2' ");
            stb.append("          AND UNITD.GRADE || UNITD.HR_CLASS = '" + _gradeHrclass + "' ");
            stb.append("          AND UNITD.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          AND UNITD.CLASSCD = CHAIR.CLASSCD ");
                stb.append("          AND UNITD.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
                stb.append("          AND UNITD.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            }
            stb.append("          AND UNITD.SEQ = SCH_CDAT.RANK ");
            stb.append("          AND UNITD.ISSUECOMPANYCD = UNIT_TEXT.ISSUECOMPANYCD ");
            stb.append(" WHERE ");
            if (!DIV_RUIKEI.equals(div)) {
                stb.append("     SCH_CDAT.EXECUTEDATE BETWEEN '" + _weekSdate + "' AND '" + _weekEdate + "' AND ");
            } else {
                final String[] date = StringUtils.split(_weekSdate, "-");
                final String setUpdDate = Integer.parseInt(date[1]) > 3 ? date[0] + "-04-01" : String.valueOf(Integer.parseInt(date[0]) - 1) + "-04-01";
                stb.append("     SCH_CDAT.EXECUTEDATE BETWEEN '" + setUpdDate + "' AND '" + _weekEdate + "' AND ");
            }
            stb.append("         SCH_CDAT.CHAIRCD IN ( ");
            stb.append("         SELECT DISTINCT ");
            stb.append("             CSTD.CHAIRCD ");
            stb.append("         FROM ");
            stb.append("             SCHREG_REGD_DAT REGD, ");
            stb.append("             CHAIR_STD_DAT CSTD ");
            stb.append("         WHERE ");
            stb.append("             REGD.YEAR = '" + _ctrlYear + "' ");
            stb.append("             AND REGD.SEMESTER IN ( ");
            stb.append("                 SELECT ");
            stb.append("                     II1.SEMESTER ");
            stb.append("                 FROM ");
            stb.append("                     SEMESTER_MST II1 ");
            stb.append("                 WHERE ");
            stb.append("                     II1.YEAR = '" + _ctrlYear + "' ");
            stb.append("                     AND II1.SEMESTER < '9' ");
            if (!DIV_RUIKEI.equals(div)) {
                stb.append("                     AND ( ");
                String setOr = "";
                for (final Iterator dateit = _dates.keySet().iterator(); dateit.hasNext();) {
                    final String date = (String) dateit.next();
                    stb.append("                     " + setOr + " '" + date + "' BETWEEN II1.SDATE AND II1.EDATE ");
                    setOr = " OR ";
                }
                stb.append("                     ) ");
            }
            stb.append("             ) ");
            stb.append("             AND REGD.GRADE || REGD.HR_CLASS = '" + _gradeHrclass + "' ");
            stb.append("             AND REGD.YEAR = CSTD.YEAR ");
            stb.append("             AND REGD.SEMESTER = CSTD.SEMESTER ");
            stb.append("             AND REGD.SCHREGNO = CSTD.SCHREGNO ");
            if (!DIV_RUIKEI.equals(div)) {
                stb.append("             AND ( ");
                String setOr = "";
                for (final Iterator dateit = _dates.keySet().iterator(); dateit.hasNext();) {
                    final String date = (String) dateit.next();
                    stb.append("                     " + setOr + " '" + date + "' BETWEEN CSTD.APPDATE AND CSTD.APPENDDATE ");
                    setOr = " OR ";
                }
                stb.append("             ) ");
            }
            stb.append("     ) ");
            stb.append(" ) ");
            
            stb.append(" SELECT ");
            if ("".equals(div)) {
                stb.append("     * ");
            } else {
                stb.append("     COUNT(*) AS CNT ");
            }
            stb.append(" FROM ");
            stb.append("     MAIN_T ");
            if (!"".equals(div)) {
                stb.append(" WHERE ");
                if ("2".equals(_targetJisu)) {
                    if ("1".equals(_useCurriculumcd)) {
                        stb.append("     CLASSCD || '-' || SCHOOL_KIND = '" + code + "' ");
                    } else {
                        stb.append("     SUBSTR(SUBCLASSCD, 1, 2) = '" + code + "' ");
                    }
                } else {
                    stb.append("     CHAIRCD = '" + code + "' ");
                }
            }
            
            return stb.toString();
        }

        private void setChairMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getChair();
//                log.debug("getChair = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = "1".equals(_targetJisu) ? rs.getString("CHAIRCD") : rs.getString("CLASSCD");
                    final String name = "1".equals(_targetJisu) ? rs.getString("SUBCLASSABBV") : rs.getString("CLASSABBV");
                    final String classCd = rs.getString("CLASSCD");

                    if (!_chairMap.containsKey(code)) {
                        final ClassData classData = new ClassData(code, name, classCd);
                        _chairMap.put(code, classData);
                    }
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getChair() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     CSTD.CHAIRCD, ");
            stb.append("     SUBD.SUBCLASSABBV, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND AS CLASSCD, ");
            } else {
                stb.append("     substr(CHAIR.SUBCLASSCD, 1, 2) AS CLASSCD, ");
            }
            stb.append("     CLASS_M.CLASSABBV ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD, ");
            stb.append("     CHAIR_STD_DAT CSTD ");
            stb.append("     LEFT JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = CSTD.YEAR ");
            stb.append("          AND CHAIR.SEMESTER = CSTD.SEMESTER ");
            stb.append("          AND CHAIR.CHAIRCD = CSTD.CHAIRCD ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBD ON SUBD.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("         AND SUBD.CLASSCD = CHAIR.CLASSCD ");
                stb.append("         AND SUBD.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
                stb.append("         AND SUBD.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN CLASS_MST CLASS_M ON ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("         CLASS_M.CLASSCD = CHAIR.CLASSCD ");
                stb.append("         AND CLASS_M.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            } else {
                stb.append("         substr(CHAIR.SUBCLASSCD, 1, 2) = CLASS_M.CLASSCD ");
            }
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER IN ( ");
            stb.append("         SELECT ");
            stb.append("             II1.SEMESTER ");
            stb.append("         FROM ");
            stb.append("             SEMESTER_MST II1 ");
            stb.append("         WHERE ");
            stb.append("             II1.YEAR = '" + _ctrlYear + "' ");
            stb.append("             AND II1.SEMESTER < '9' ");
            stb.append("             AND ( ");
            String setOr = "";
            for (final Iterator dateit = _dates.keySet().iterator(); dateit.hasNext();) {
                final String date = (String) dateit.next();
                stb.append("             " + setOr + " '" + date + "' BETWEEN II1.SDATE AND II1.EDATE ");
                setOr = " OR ";
            }
            stb.append("         ) ");
            stb.append("     ) ");
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS = '" + _gradeHrclass + "' ");
            stb.append("     AND REGD.YEAR = CSTD.YEAR ");
            stb.append("     AND REGD.SEMESTER = CSTD.SEMESTER ");
            stb.append("     AND REGD.SCHREGNO = CSTD.SCHREGNO ");
            stb.append("     AND EXISTS (SELECT ");
            stb.append("                     'x' ");
            stb.append("                 FROM ");
            stb.append("                     UNIT_STUDY_TEXT_BOOK_DAT UNIT_TEXT ");
            stb.append("                 WHERE ");
            stb.append("                     UNIT_TEXT.YEAR = CHAIR.YEAR ");
            stb.append("                     AND UNIT_TEXT.GRADE = REGD.GRADE ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("                     AND UNIT_TEXT.CLASSCD = CHAIR.CLASSCD ");
                stb.append("                     AND UNIT_TEXT.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
                stb.append("                     AND UNIT_TEXT.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            }
            stb.append("                     AND UNIT_TEXT.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            stb.append("     ) ");
            stb.append(" ORDER BY ");
            stb.append("     CSTD.CHAIRCD ");
            return stb.toString();
        }

        private void setJisuMap(final DB2UDB db2, final String code, final String div) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = DIV_YOTEI.equals(div) ? getTotalSchedule(code) : DIV_HYOJUN.equals(div) ? getTotalJisu(code) : getHrUnitDat(code, div);
//                log.debug("code = " + code + ", div = " + div);
//                log.debug("sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cnt = rs.getString("CNT");
                    if (null == cnt) continue;

                    final ClassData classData = (ClassData) _chairMap.get(code);
                    if (null == classData) {
                        log.error("classData無し :" + code);
                    } else {
                        classData._jisuMap.put(div, cnt);
                    }
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        //標準時数
        private String getTotalJisu(final String code) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     SUM(INT(T1.STANDARD_TIME)) AS CNT ");
            stb.append(" FROM ");
            stb.append("     UNIT_CLASS_LESSON_SCHOOL_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHOOL_KIND IN (SELECT ");
            stb.append("                         I1.SCHOOL_KIND ");
            stb.append("                     FROM ");
            stb.append("                         SCHREG_REGD_GDAT I1 ");
            stb.append("                     WHERE ");
            stb.append("                         I1.YEAR = '" + _ctrlYear + "' ");
            stb.append("                         AND I1.GRADE = '" + _gradeHrclass.substring(0, 2) + "' ");
            stb.append("                     ) ");
            stb.append("     AND T1.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND T1.CURRICULUM_CD = '1' ");
            stb.append("     AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("                     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("         T1.SUBCLASSCD IN (SELECT DISTINCT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("                     I2.CLASSCD || '-' || I2.SCHOOL_KIND || '-' || I2.CURRICULUM_CD || '-' || ");
            }
            stb.append("                         I2.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("                     FROM ");
            stb.append("                         CHAIR_DAT I2 ");
            stb.append("                     WHERE ");
            stb.append("                         I2.YEAR = '" + _ctrlYear + "' ");
            if ("2".equals(_targetJisu)) {
                if ("1".equals(_useCurriculumcd)) {
                    stb.append("                         AND I2.CLASSCD || '-' || I2.SCHOOL_KIND = '" + code + "' ");
                } else {
                    stb.append("                         AND SUBSTR(I2.SUBCLASSCD, 1, 2) = '" + code + "' ");
                }
            } else {
                stb.append("                         AND I2.CHAIRCD = '" + code + "' ");
            }
            stb.append("                     ) ");
            stb.append("     AND T1.SEMESTER = '9' ");
            stb.append("     AND T1.GRADE = '" + _gradeHrclass.substring(0, 2) + "' ");
            stb.append("     AND T1.TIME_DIV = '1' ");

            return stb.toString();
        }

        private String getTotalSchedule(final String code) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_SEQ AS ( ");
            stb.append("     SELECT ");
            stb.append("         CHAIRCD, ");
            stb.append("         MAX(BSCSEQ) AS BSCSEQ ");
            stb.append("     FROM ");
            stb.append("         SCH_PTRN_DAT ");
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + _ctrlYear + "'      ");
            stb.append("         AND SEMESTER IN ( ");
            stb.append("             SELECT ");
            stb.append("                 II1.SEMESTER ");
            stb.append("             FROM ");
            stb.append("                 SEMESTER_MST II1 ");
            stb.append("             WHERE ");
            stb.append("                 II1.YEAR = '" + _ctrlYear + "' ");
            stb.append("                 AND II1.SEMESTER < '9' ");
            stb.append("                 AND ( ");
            String setOr = "";
            for (final Iterator dateit = _dates.keySet().iterator(); dateit.hasNext();) {
                final String date = (String) dateit.next();
                stb.append("             " + setOr + " '" + date + "' BETWEEN II1.SDATE AND II1.EDATE ");
                setOr = " OR ";
            }
            stb.append("                 ) ");
            stb.append("         )      ");
            if ("2".equals(_targetJisu)) {
                stb.append("     AND CHAIRCD IN (" + getChair(code) + ") ");
            } else {
                stb.append("     AND CHAIRCD = '" + code + "' ");
            }
            stb.append("     GROUP BY ");
            stb.append("         CHAIRCD ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM ");
            stb.append("     SCH_PTRN_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND EXISTS (SELECT ");
            stb.append("                     'x' ");
            stb.append("                 FROM ");
            stb.append("                     MAX_SEQ T2 ");
            stb.append("                 WHERE ");
            stb.append("                     T2.BSCSEQ = T1.BSCSEQ ");
            stb.append("                     AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append("     ) ");
            return stb.toString();
        }

        private String getChair(final String code) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     CSTD.CHAIRCD ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD, ");
            stb.append("     CHAIR_STD_DAT CSTD ");
            stb.append("     LEFT JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = CSTD.YEAR ");
            stb.append("          AND CHAIR.SEMESTER = CSTD.SEMESTER ");
            stb.append("          AND CHAIR.CHAIRCD = CSTD.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER IN ( ");
            stb.append("         SELECT ");
            stb.append("             II1.SEMESTER ");
            stb.append("         FROM ");
            stb.append("             SEMESTER_MST II1 ");
            stb.append("         WHERE ");
            stb.append("             II1.YEAR = '" + _ctrlYear + "' ");
            stb.append("             AND II1.SEMESTER < '9' ");
            stb.append("             AND ( ");
            String setOr = "";
            for (final Iterator dateit = _dates.keySet().iterator(); dateit.hasNext();) {
                final String date = (String) dateit.next();
                stb.append("             " + setOr + " '" + date + "' BETWEEN II1.SDATE AND II1.EDATE ");
                setOr = " OR ";
            }
            stb.append("         ) ");
            stb.append("     ) ");
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS = '" + _gradeHrclass + "' ");
            stb.append("     AND REGD.YEAR = CSTD.YEAR ");
            stb.append("     AND REGD.SEMESTER = CSTD.SEMESTER ");
            stb.append("     AND REGD.SCHREGNO = CSTD.SCHREGNO ");
            stb.append("     AND EXISTS (SELECT ");
            stb.append("                     'x' ");
            stb.append("                 FROM ");
            stb.append("                     UNIT_STUDY_TEXT_BOOK_DAT UNIT_TEXT ");
            stb.append("                 WHERE ");
            stb.append("                     UNIT_TEXT.YEAR = CHAIR.YEAR ");
            stb.append("                     AND UNIT_TEXT.GRADE = REGD.GRADE ");
            stb.append("                     AND UNIT_TEXT.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            stb.append("     ) ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("     AND CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND = '" + code + "' ");
            } else {
                stb.append("     AND SUBSTR(CHAIR.SUBCLASSCD, 1, 2) = '" + code + "' ");
            }
            return stb.toString();
        }

        private String getHrname(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                final String sql = " SELECT HR_NAME FROM SCHREG_REGD_HDAT T1 WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _ctrlSemester + "' AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("HR_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getStaffname(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT VALUE(T2.STAFFNAME, VALUE(T3.STAFFNAME, T4.STAFFNAME)) AS STAFFNAME FROM SCHREG_REGD_HDAT T1 ");
                sql.append("     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = TR_CD1 ");
                sql.append("     LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = TR_CD2 ");
                sql.append("     LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = TR_CD3 ");
                sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _ctrlSemester + "' AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("STAFFNAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private boolean getSeirekiFlg(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean seirekiFlg = false;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if ("2".equals(rs.getString("NAME1"))) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
        }

        private String getTermDate() {
            if (_seirekiFlg) {
                return KNJ_EditDate.h_format_JP(_weekSdate) + " \uFF5E " + KNJ_EditDate.h_format_JP(_weekEdate);
            } else {
                return KNJ_EditDate.h_format_JP(_weekSdate) + " \uFF5E " + KNJ_EditDate.h_format_JP(_weekEdate);
            }
        }

        private String getCtrlDate() {
            if (_seirekiFlg) {
                return _ctrlYear + "年" + KNJ_EditDate.h_format_JP_N(_ctrlDate);
            } else {
                return KNJ_EditDate.h_format_JP(_param._ctrlDate);
            }
        }

        private String getNendo() {
            if (_seirekiFlg) {
                return _ctrlYear + "年度";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(_ctrlYear)) + "年度";
            }
        }

        private String getTitle() {
            return "指導計画表";
        }

        private String getFormName() {
            return "1".equals(_targetForm) ? "KNJS330_1.frm" : "KNJS330_2.frm";
        }

        /**
         * 校時のリストを得る。
         * @param db2
         * @return
         */
        private List getPeriodList(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List periodList = new ArrayList();
            int line = 0;
            try {
                final String sql = getPeriodSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String periodCd = rs.getString("NAMECD2");
                    final String name = rs.getString("NAME1");

                    line++;
                    final Period period = new Period(periodCd, name, line);
                    periodList.add(period);
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return periodList;
        }

        private String getPeriodSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMECD2, ");
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _ctrlYear + "' AND ");
            stb.append("     T1.NAMECD1 = 'B001' ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }
    }
}

// eof
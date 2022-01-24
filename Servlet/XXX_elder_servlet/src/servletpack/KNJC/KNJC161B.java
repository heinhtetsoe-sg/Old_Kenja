// kanji=漢字
/*
 * $Id: e23bda74f7af5ee5808d4bc944a8895cc3a8d57c $
 *
 */
package servletpack.KNJC;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [出欠管理] 欠課状況集計表
 */

public class KNJC161B {

    private static final Log log = LogFactory.getLog(KNJC161B.class);

    private static final int PERIODMAX = 12;
    private boolean _hasdata = false;

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        try {
            response.setContentType("application/pdf");
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
        } catch (java.io.IOException ex) {
            log.error("svf instancing exception! ", ex);
        }

        // ＤＢ接続
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db2 instancing exception! ", ex);
            return;
        }

        _hasdata = false;
        Param param = null;
        try {
            param = createParam(db2, request);

            // 印刷処理
            printMain(db2, param, svf);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            // 終了処理
            if (!_hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    //印刷処理
    private void printMain(
            final DB2UDB db2,
            final Param param,
            final Vrw32alp svf
    ) {

        final Map schMap = getStudentInfo(db2, param);
        final Map remarkMap = getRemarkInfo(db2, param);
        final Map schChrMap = getSchChrInfo(db2, param);
        final Map attRuikeiMap = loadMonths(db2, param, true);
        final Map subclsMstMap = getSubclsMst(db2, param);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
        final String nowDate = format.format(cal.getTime());  //現在時刻を取得

        //出力月の初日に設定
        cal.clear();
        cal.set(Integer.parseInt(param._year), Integer.parseInt(param._month)-1, 1);
        SimpleDateFormat youbiFstFmt = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat youbiSndFmt = new SimpleDateFormat("E");

        String[] chkCdStr = {"4", "5", "6"};
        String[] attendChkStr = {"1", "2", "3", "4", "5", "6", "15", "16"};
        List absList = Arrays.asList(chkCdStr);
        List attChkList = Arrays.asList(attendChkStr);
        int pageNo = 0;
        //生徒単位にループ
        for (Iterator ite = schMap.keySet().iterator();ite.hasNext();) {
            final String schregNo = (String)ite.next();
            final StudentInfo student = (StudentInfo)schMap.get(schregNo);
            log.info("student:"+schregNo);
            final Map ruikeiMap = attRuikeiMap.containsKey(schregNo) ? (Map)attRuikeiMap.get(schregNo) : new LinkedMap();
            pageNo++;

            int lessonCnt = 0;
            int absentCnt = 0;
            int lessonDCnt = 0;
            int absentDCnt = 0;

            svf.VrSetForm("KNJC161B.frm", 4);
            //ヘッダ/フッタを出力
            setTitle(db2, param, svf, pageNo, student, nowDate);

            //日別出欠表を出力(日単位にループ)
            int lineCnt = 1;
            for (Iterator itr = param._domList.iterator();itr.hasNext();) {
                final String dStr = (String)itr.next();
                final String ymdKey = param.concatYMD(Integer.parseInt(dStr));
                final Date youbiDObj;
                try {
                    youbiDObj = youbiFstFmt.parse(ymdKey);
                    svf.VrsOutn("WEEK", lineCnt, youbiSndFmt.format(youbiDObj));
                }catch (ParseException e) {
                }finally {
                }
                svf.VrsOutn("DAY", lineCnt, dStr);
                final String rKey = schregNo + "-" + ymdKey;
                final List diCdOneDayList = new ArrayList();
                int pNo = 0;
                int pMax = calcPeriodMax(student._e_PeriodCd);
                for (Iterator itpd = param._periodMap.keySet().iterator();itpd.hasNext();) {
                    final String pCd = (String)itpd.next();
                    if (pNo > pMax) {
                        continue;
                    }
                    final String scKey = rKey + "-" + Integer.toHexString(pNo).toUpperCase();
                    if (schChrMap.containsKey(scKey)) {
                        final SchChrInfo scWk = (SchChrInfo)schChrMap.get(scKey);
                        svf.VrsOutn("ATTENDMARK"+pNo, lineCnt, scWk._val);
                        lessonCnt++;
                        if (absList.contains(scWk._di_Cd)) {
                            absentCnt++;
                        }
                        if (!diCdOneDayList.contains(scWk._di_Cd)) {
                            if (!attChkList.contains(scWk._di_Cd)) {
                                if ("1".equals(scWk._executed)) {
                                    diCdOneDayList.add("");   //_executedが"1"で、想定外のコードは"○"になっているはず。未は登録しない。
                                }
                            } else {
                                diCdOneDayList.add(StringUtils.defaultString(scWk._di_Cd, ""));
                            }
                        }
                    }
                    pNo++;
                }
                if (remarkMap.containsKey(ymdKey)) {
                    final RemarkInfo rPutWk = (RemarkInfo)remarkMap.get(ymdKey);
                    final int rlen = KNJ_EditEdit.getMS932ByteLength(rPutWk._remark1);
                    final String rfield = rlen > 90 ? "6" : rlen > 80 ? "5" : rlen > 70 ? "4" : rlen > 60 ? "3" : rlen > 52 ? "2" : "1";
                    svf.VrsOutn("EVENT_REMARK" + rfield, lineCnt, rPutWk._remark1);  //学校行事
                }
                String putStatStr = "";
                if (diCdOneDayList.contains("")) {
                    putStatStr = "出席";
                    lessonDCnt++;
                } else if (diCdOneDayList.contains("1")) {
                    putStatStr = "公欠";
                    lessonDCnt++;
                } else if (diCdOneDayList.contains("2") || diCdOneDayList.contains("3")) {
                    putStatStr = "出停忌引き";
                    lessonDCnt++;
                } else if (diCdOneDayList.contains("4") || diCdOneDayList.contains("5") || diCdOneDayList.contains("6")) {
                    putStatStr = "欠席";
                    absentDCnt++;
                }
                svf.VrsOutn("STATUS", lineCnt, putStatStr);
                lineCnt++;
            }

            //下部の出力(全てjavaでカウント)
            svf.VrsOut("LESSON_DAYCNT", String.valueOf(lessonDCnt + absentDCnt));
            svf.VrsOut("KESSEKI_DAYCNT", String.valueOf(absentDCnt));
            svf.VrsOut("LESSON_TIMECNT", String.valueOf(lessonCnt));
            svf.VrsOut("KESSEKI_TIMECNT", String.valueOf(absentCnt));

            //科目別出欠表を出力
            for (Iterator its = ruikeiMap.keySet().iterator();its.hasNext();) {
                final String subclsCd = (String)its.next();
                if (subclsCd.startsWith("9") && subclsCd.length() > 2) {  //9x系の科目は除外
                    continue;
                }
                final Map ruikei = (Map)ruikeiMap.get(subclsCd);
                if (subclsMstMap.containsKey(subclsCd)) {
                    final SubclsInfo subclsObj = (SubclsInfo)subclsMstMap.get(subclsCd);
                    svf.VrsOut("SUBCLASSNAME", subclsObj._subclassAbbv);
                }
                //log.info("SUBCLS:" + subclsCd + "(" + subclsMstMap.containsKey(subclsCd) + ")");
                svf.VrsOut("KESSEKI_CNT", getStrVal(ruikei, "SICK2"));
                svf.VrsOut("LATE_CNT",  getStrVal(ruikei, "LATE"));
                svf.VrsOut("EARLY_CNT", getStrVal(ruikei, "EARLY"));
                this._hasdata = true;
                svf.VrEndRecord();
            }
            log.info("END:"+schregNo);
            svf.VrEndPage();
        }
    }

    private String getStrVal(final Map getMap, final String keyStr) {
        return getMap.containsKey(keyStr) ? (String)getMap.get(keyStr) : "";
    }
    private void setTitle(final DB2UDB db2, final Param param, final Vrw32alp svf, final int page, final StudentInfo student, final String nowDate) {
        svf.VrsOut("MONTH", String.valueOf(Integer.parseInt(param._month)));
        //svf.VrsOut("TITLE", data);
        svf.VrsOut("DATETIME", nowDate);
        svf.VrsOut("PAGE", String.valueOf(page));
        svf.VrsOut("MAJORNAME", student._majorName);
        svf.VrsOut("ATTENDNO", Integer.parseInt(student._attendNo) + "番");
        svf.VrsOut("HR_NAME", student._hr_Name);
        final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nfield = nlen > 70 ? "4" : nlen > 60 ? "3" : nlen > 50 ? "2": "1";
        svf.VrsOut("NAME" + nfield, student._name);

        int pNo = 0;
        int pMax = calcPeriodMax(student._e_PeriodCd);
        for (Iterator itpd = param._periodMap.keySet().iterator();itpd.hasNext();) {
            final String pCd = (String)itpd.next();
            final String abbv = (String)param._periodMap.get(pCd);
            pNo++;
            if (pNo > pMax) {
                continue;
            }
            svf.VrsOut("PERIOD" + pNo, abbv);
        }
        svf.VrsOut("SCHOOLNAME", param._schoolName);
        svf.VrsOut("SCHOOL_LOGO", param._logoPathFN);
    }

    private int calcPeriodMax(final String ePeriod) {
        final int ePVal = Integer.parseInt(ePeriod, 16);
        return ePVal > PERIODMAX ? PERIODMAX : ePVal;
    }

    private static Map loadMonths(final DB2UDB db2, final Param param, final boolean ruisekiFlg) {
        final Map retMap = new LinkedMap();
        Map subMap = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String monthStartDate = ruisekiFlg ? param._year + "-04-01" : param.getYMD(false);
        final String endDate = param.getYMD(true);
        log.debug(" month = " + param._month + ", " + monthStartDate + " - " + endDate);

        try {
            final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        param._semester,
                        monthStartDate,
                        endDate,
                        param._attendParamMapMonth
                        );
            ps = db2.prepareStatement(sql);

            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while(rs.next()) {
                final Map m = new HashMap();
                if (!"9".equals(rs.getString("SEMESTER"))) { //集計以外は除外
                    continue;
                }
                for (int i = 1; i<= meta.getColumnCount(); i++) {
                    final String columnName = meta.getColumnLabel(i);
                    final String data = rs.getString(columnName);
                    m.put(columnName, data);
                }
                final String fstKey = rs.getString("SCHREGNO");
                if (!retMap.containsKey(fstKey)) {
                    subMap = new LinkedMap();
                    retMap.put(fstKey, subMap);
                } else {
                    subMap = (Map)retMap.get(fstKey);
                }
                final String sndKey = rs.getString("SUBCLASSCD");
                subMap.put(sndKey, m);
            }
        } catch (Exception ex) {
            log.error("svfPrintGrade exception!", ex);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
        return retMap;
    }

    private Map getStudentInfo(final DB2UDB db2, final Param param) {
        final Map retMap = new LinkedMap();
        final String sql = getStudentInfo(param);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()) {
                final String attendNo = rs.getString("ATTENDNO");
                final String schregNo = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hr_Class = rs.getString("HR_CLASS");
                final String hr_Name = rs.getString("HR_NAME");
                final String name = rs.getString("NAME");
                final String majorName = rs.getString("MAJORNAME");
                final String s_PeriodCd = rs.getString("S_PERIODCD");
                final String e_PeriodCd = rs.getString("E_PERIODCD");
                StudentInfo addWk = new StudentInfo(attendNo, schregNo, grade, hr_Class, hr_Name, name, majorName, s_PeriodCd, e_PeriodCd);
                retMap.put(schregNo, addWk);
            }
        } catch (Exception ex) {
            log.error("svfPrintGrade exception!", ex);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
        return retMap;
    }

    private String getStudentInfo(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T2.GRADE, ");
        stb.append("   T3.HR_CLASS, ");
        stb.append("   T3.HR_NAME, ");
        stb.append("   T0.NAME, ");
        stb.append("   MM.MAJORNAME, ");
        stb.append("   CM.S_PERIODCD, ");
        stb.append("   CM.E_PERIODCD ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T0 ");
        stb.append("     ON T0.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.GRADE = T1.GRADE ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT T3 ");
        stb.append("     ON T3.YEAR = T1.YEAR ");
        stb.append("    AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T3.GRADE = T1.GRADE ");
        stb.append("    AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("   LEFT JOIN MAJOR_MST MM ");
        stb.append("     ON MM.COURSECD = T1.COURSECD ");
        stb.append("    AND MM.MAJORCD = T1.MAJORCD ");
        stb.append("   LEFT JOIN COURSE_MST CM ");
        stb.append("     ON CM.COURSECD = T1.COURSECD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + param._semester + "' ");
        if (param._category_selected != null) {
            stb.append("   AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
        } else {
            stb.append("   AND T1.GRADE || T1.HR_CLASS = '" + param._grade_hr_class + "' ");
        }
        //転学(2)・退学(3)者 但し異動日が当月初日より小さい場合
        //転入(4)・編入(5)者 但し異動日が当月終日より大きい場合
        stb.append("        AND ( NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                     WHERE S1.SCHREGNO = T1.SCHREGNO AND ");
        stb.append("                           ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < '" + param.getYMD(false) + "' ) ");
        stb.append("                           OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > '" + param.getYMD(true) + "' )) ");
        stb.append("                    ) ");
        stb.append("              AND ");
        //留学(1)・休学(2)者
        stb.append("              NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("                     WHERE S1.SCHREGNO = T1.SCHREGNO AND ");
        stb.append("                           S1.TRANSFERCD IN ('1','2') AND ");
        stb.append("                           '" + param.getYMD(false) + "' > S1.TRANSFER_SDATE AND ");
        stb.append("                           '" + param.getYMD(true) + "' < S1.TRANSFER_EDATE ");
        stb.append("                    ) ");
        stb.append("        ) ");
        return stb.toString();
    }

    private class StudentInfo {
        final String _attendNo;
        final String _schregNo;
        final String _grade;
        final String _hr_Class;
        final String _hr_Name;
        final String _name;
        final String _majorName;
        final String _s_PeriodCd;
        final String _e_PeriodCd;
        public StudentInfo (final String attendNo, final String schregNo, final String grade, final String hr_Class,
                             final String hr_Name, final String name, final String majorName, final String s_PeriodCd, final String e_PeriodCd)
        {
            _attendNo = attendNo;
            _schregNo = schregNo;
            _grade = grade;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _name = name;
            _majorName = majorName;
            _s_PeriodCd = s_PeriodCd;
            _e_PeriodCd = e_PeriodCd;
        }
    }

    private Map getRemarkInfo(final DB2UDB db2, final Param param) {
        final Map retMap = new LinkedMap();
        final String sql = getRemarkInfoSql(param);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()) {
                final String executedate = rs.getString("EXECUTEDATE");
                final String remark1 = rs.getString("REMARK1");
                RemarkInfo addWk = new RemarkInfo(executedate, remark1);
                retMap.put(executedate, addWk);
            }
        } catch (Exception ex) {
            log.error("getRemarkInfo exception!", ex);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
        return retMap;
    }

    private String getRemarkInfoSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T4.EXECUTEDATE, ");
        stb.append("   T4.REMARK1 ");
        stb.append(" FROM ");
        stb.append("   EVENT_MST T4 ");
        stb.append(" WHERE ");
        stb.append("   T4.GRADE = '00' ");
        stb.append("   AND T4.HR_CLASS = '000' ");
        stb.append("   AND T4.EXECUTEDATE BETWEEN '" + param.getYMD(false) + "' AND '" + param.getYMD(true) + "' ");
        stb.append("   AND T4.DATA_DIV = '1' ");
        stb.append("   AND T4.HR_CLASS_DIV = '1' ");
        stb.append("   AND T4.COURSECD = '0' ");
        stb.append("   AND T4.MAJORCD = '000' ");
        stb.append("   AND T4.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append(" ORDER BY ");
        stb.append("   T4.EXECUTEDATE ");
        return stb.toString();
    }

    private class RemarkInfo {
        final String _executedate;
        final String _remark1;
        public RemarkInfo (final String executedate, final String remark1)
        {
            _executedate = executedate;
            _remark1 = remark1;
        }
    }

    private Map getSchChrInfo(final DB2UDB db2, final Param param) {
        final Map retMap = new LinkedMap();
        final String sql = getSchChrInfoSql(param);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hr_Class = rs.getString("HR_CLASS");
                final String executedate = rs.getString("EXECUTEDATE");
                final String periodcd = rs.getString("PERIODCD");
                final String di_Cd = rs.getString("DI_CD");
                final String executed = rs.getString("EXECUTED");
                final String val = rs.getString("VAL");
                SchChrInfo addWk = new SchChrInfo(schregno, grade, hr_Class, executedate, periodcd, di_Cd, executed, val);
                retMap.put(schregno+"-"+executedate+"-"+periodcd, addWk);
            }
        } catch (Exception ex) {
            log.error("getSchChrInfo exception!", ex);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
        return retMap;
    }


    private String getSchChrInfoSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" select distinct ");
        stb.append("  T1.SCHREGNO, ");
        stb.append("  T1.GRADE, ");
        stb.append("  T1.HR_CLASS, ");
        stb.append("  T6.EXECUTEDATE, ");
        stb.append("  T6.PERIODCD, ");
        stb.append("  T8.DI_CD, ");
        stb.append("  CASE WHEN T7.EXECUTED = '1' OR T6.EXECUTED = '1' THEN '1' ELSE '0' END AS EXECUTED, ");
        stb.append("  CASE WHEN T8.DI_CD IN ('4', '5', '6') THEN '／' ");
        stb.append("       WHEN T8.DI_CD = '15' THEN '遅' ");
        stb.append("       WHEN T8.DI_CD = '16' THEN '早' ");
        stb.append("       WHEN T8.DI_CD = '1' THEN '公' ");
        stb.append("       WHEN T8.DI_CD IN ('2', '3') THEN '停' ");
        stb.append("       WHEN T7.EXECUTED = '1' OR T6.EXECUTED = '1' THEN '○' ");
        stb.append("       ELSE '未' END AS VAL ");
        stb.append(" from ");
        stb.append("   schreg_regd_dat T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.GRADE = T1.GRADE ");
        stb.append("   LEFT JOIN CHAIR_STD_DAT T4 ");
        stb.append("     ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T4.YEAR = T1.YEAR ");
        stb.append("    AND T4.SEMESTER = T1.SEMESTER ");
        stb.append("   LEFT JOIN CHAIR_DAT T5 ");
        stb.append("     ON T5.YEAR = T4.YEAR ");
        stb.append("    AND T5.SEMESTER = T4.SEMESTER ");
        stb.append("    AND T5.CHAIRCD = T4.CHAIRCD ");
        stb.append("    AND T5.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("   LEFT JOIN SCH_CHR_DAT T6 ");
        stb.append("     ON T6.YEAR = T5.YEAR ");
        stb.append("    AND T6.SEMESTER = T5.SEMESTER ");
        stb.append("    AND T6.CHAIRCD = T5.CHAIRCD ");
        stb.append("    AND T6.EXECUTEDATE BETWEEN '" + param.getYMD(false) + "' AND '" + param.getYMD(true) + "' ");
        stb.append("   LEFT JOIN SCH_CHR_HRATE_DAT T7 ");
        stb.append("     ON T7.CHAIRCD = T5.CHAIRCD ");
        stb.append("    AND T7.EXECUTEDATE BETWEEN '" + param.getYMD(false) + "' AND '" + param.getYMD(true) + "' ");
        stb.append("    AND T7.EXECUTEDATE = T6.EXECUTEDATE ");
        stb.append("    AND T7.GRADE = T1.GRADE ");
        stb.append("    AND T7.HR_CLASS = T1.HR_CLASS ");
        stb.append("    AND T7.CHAIRCD = T5.CHAIRCD ");
        stb.append("   LEFT JOIN ATTEND_DAT T8 ");
        stb.append("     ON T8.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T8.ATTENDDATE = T6.EXECUTEDATE ");
        stb.append("    AND T8.PERIODCD = T6.PERIODCD ");
        stb.append("    AND T8.CHAIRCD = T6.CHAIRCD ");
        stb.append("    AND T8.YEAR = T6.YEAR ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + param._semester + "' ");
        if (param._category_selected != null) {
            stb.append("   AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
        } else {
            stb.append("   AND T1.GRADE || T1.HR_CLASS = '" + param._grade_hr_class + "' ");
        }
        stb.append("   AND T6.EXECUTEDATE IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T6.EXECUTEDATE, ");
        stb.append("   T6.PERIODCD ");
        return stb.toString();
    }

    private class SchChrInfo {
        final String _schregno;
        final String _grade;
        final String _hr_Class;
        final String _executedate;
        final String _periodcd;
        final String _di_Cd;
        final String _executed;
        final String _val;
        public SchChrInfo (final String schregno, final String grade, final String hr_Class, final String executedate, final String periodcd, final String di_Cd, final String executed, final String val)
        {
            _schregno = schregno;
            _grade = grade;
            _hr_Class = hr_Class;
            _executedate = executedate;
            _periodcd = periodcd;
            _di_Cd = di_Cd;
            _executed = executed;
            _val = val;
        }
    }

    private Map getSubclsMst(final DB2UDB db2, final Param param) {
        final Map retMap = new LinkedMap();
        final String sql = getSubclsMstSql(param);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()) {
                final String classcd = rs.getString("CLASSCD");
                final String school_Kind = rs.getString("SCHOOL_KIND");
                final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String subclassabbv = rs.getString("SUBCLASSABBV");
                SubclsInfo addWk = new SubclsInfo(classcd, school_Kind, curriculum_Cd, subclasscd, subclassabbv);
                retMap.put(addWk.getSubclsCd(), addWk);
            }
        } catch (Exception ex) {
            log.error("getSubclsMst exception!", ex);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
        return retMap;
    }

    private String getSubclsMstSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" select ");
        stb.append("   T1.SCHOOL_KIND, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.CURRICULUM_CD, ");
        stb.append("   T2.SUBCLASSCD, ");
        stb.append("   T1.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("   SUBCLASS_MST T1 ");
        stb.append("   INNER JOIN SUBCLASS_YDAT T2 ");
        stb.append("     ON T2.CLASSCD = T1.CLASSCD ");
        stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("   T2.YEAR = '" + param._year + "' ");
        stb.append("   AND T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append(" ORDER BY ");
        stb.append("   T2.SUBCLASSCD ");
        return stb.toString();
    }

    private class SubclsInfo {
        final String _schoolKind;
        final String _classCd;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassAbbv;
        public SubclsInfo (final String classCd, final String schoolKind, final String curriculumCd, final String subclassCd, final String subclassAbbv)
        {
            _schoolKind = schoolKind;
            _classCd = classCd;
            _subclassCd = subclassCd;
            _curriculumCd = curriculumCd;
            _subclassAbbv = subclassAbbv;
        }
        public String getSubclsCd() {
            return _classCd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclassCd;
        }
    }

    // パラメータ取得処理
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static class Param {
        final List _domList = new ArrayList();

        final Map _attendParamMapMonth;

        final String _year;
        final int _realYear;
        final String _semester;
        private final String _month;
        private final String _selDispType;
        private final String _grade_hr_class;
        private final String _grade;
        private final String _hrCls;
        private final String _schoolKind;
        private final String _schoolName;
        private final String[] _category_selected;
        private final String _usecurriculumcd;
        private final String _documentroot;
        private String _imagePath;
        private String _extension;
        private final String _logoPathFN;
        private final Map _periodMap;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _selDispType = request.getParameter("selDispType");
            _grade_hr_class = request.getParameter("GRADE_HR_CLASS");
            if (_grade_hr_class == null || "".equals(_grade_hr_class)) {
                _grade = "";
                _hrCls = "";
            } else {
                _grade = _grade_hr_class.substring(0,2);
                _hrCls = _grade_hr_class.substring(2);
            }
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _attendParamMapMonth = new HashMap();
            _documentroot = request.getParameter("DOCUMENTROOT");

            _schoolKind = getSchoolKind(db2);
            _schoolName = getSchoolName(db2);
            if ("2".equals(_selDispType)) {
                _category_selected = request.getParameterValues("CATEGORY_SELECTED");
            } else {
                _category_selected = null;
            }

            String[] cutWk = StringUtils.split(request.getParameter("month"), "-");  //3番目には学期開始/終了月フラグ(未使用)
            if (cutWk.length > 0) {
                _semester = cutWk[0];
            } else {
                _semester = null;
                _month = null;
                _realYear = Integer.parseInt(_year);
                _logoPathFN = null;
                _periodMap = null;
                return;
            }
            if (cutWk.length > 1) {
                _month = cutWk[1];
            } else {
                _month = null;
                _realYear = Integer.parseInt(_year);
                _logoPathFN = null;
                _periodMap = null;
                return;
            }

            _realYear = Integer.parseInt(_year) + (Integer.parseInt(_month) < 4 ? 1 : 0);
            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.set(Integer.parseInt(_year), Integer.parseInt(_month)-1, 1);
            final int lday = cal.getActualMaximum(Calendar.DATE);

            _attendParamMapMonth.put("DB2UDB", db2);
            _attendParamMapMonth.put("HttpServletRequest", request);
            _attendParamMapMonth.put("grade", _grade);
            _attendParamMapMonth.put("hrClass", _hrCls);

            for (int m = 1; m <= lday; m++) {
                _domList.add(String.valueOf(m));
            }
            _imagePath = "";
            _extension = "";
            loadControlMst(db2);
            _logoPathFN = getImageFilePath("SCHOOLLOGO");

            _periodMap = getPeriodMap(db2);

        }
        private Map getPeriodMap(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   NAMECD2 AS PERIODCD, ");
            stb.append("   ABBV1 ");
            stb.append(" FROM ");
            stb.append("   V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("   YEAR = '" + _year + "' AND ");
            stb.append("   NAMECD1 = 'B001' AND ");
            stb.append("   NAMESPARE2 IS NULL ");
            stb.append(" ORDER BY ");
            stb.append("   NAMECD2 ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String periodCd = rs.getString("PERIODCD");
                    final String abbv = rs.getString("ABBV1");
                    retMap.put(periodCd, abbv);
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        public String getYMD(final boolean lastFlg) {
            String setDVal = "";
            if (lastFlg) {
                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(_realYear, Integer.parseInt(_month)-1, 1);
                setDVal = String.valueOf(cal.getActualMaximum(Calendar.DATE));
            } else {
                setDVal = "01";
            }
            return _realYear + "-" + _month + "-" + setDVal;
        }
        public String concatYMD(final int setDVal) {
            return _realYear + "-" + _month + "-" + String.format("%02d", setDVal);
        }
        public String getSchoolKind(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
        }
        public String getSchoolName(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' AND SCHOOLCD = '000000000000' AND SCHOOL_KIND = '" + _schoolKind + "' "));
        }
        private void loadControlMst(final DB2UDB db2) {
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                    _extension = rs.getString("EXTENSION");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + name + "." + _extension;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }
    }
}
